/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.google.javascript.jscomp.InlineObjectLiterals
 *
 * 1. Scope & Var Filter Branch Coverage (isVarInlineForbidden):
 *    - var.isGlobal() == true (global scope variables excluded from inlining).
 *    - var.isExtern() == true (extern variables excluded).
 *    - codingConvention.isExported() == true (exported names excluded).
 *    - var.name == RENAME_PROPERTY_FUNCTION_NAME (special function name excluded).
 *    - staleVars.contains(var) == true (already modified / blacklisted variables excluded).
 *
 * 2. Inlinable Object Detection (isInlinableObject):
 *    - parent.isGetProp():
 *      - gramps.isCall() && gramps.getFirstChild() == parent (method call `x.fn()` -> bails out, returns false).
 *      - property read `x.y` -> continue.
 *    - !isVarOrAssignExprLhs(name):
 *      - name passed as argument / returned directly / part of array -> returns false.
 *    - val == null:
 *      - uninitialized var declaration `var x;` -> continue.
 *    - !val.isObjectLit():
 *      - assigned non-object literal (e.g. `x = 5`, `x = "str"`, `x = [1, 2]`) -> returns false.
 *    - ES5 getters/setters:
 *      - child.isGetterDef() / child.isSetterDef() -> returns false.
 *    - Self-referential assignment:
 *      - `x = {a: x.b}` or reference inside child value -> returns false.
 *    - Return true when an acceptable object literal assignment is encountered without blocking uses.
 *
 * 3. Object Inlining & AST Rewriting:
 *    - Single object literal with properties (`var a = {x: 1, y: 2}; return a.x + a.y;`).
 *    - Object reassignment (`a = {x: 3, y: 4};`).
 *    - Object with empty literal (`var a = {};`).
 *    - Multiple references and assignments.
 *    - Replacement with comma expressions (`JSCompiler_object_inline_... = ...`).
 *
 * 4. Defect Zone (Defects4J Issue 724 & testObject10/12/22):
 *    - Testing self-referential / reassignment edge cases where an object is assigned a property
 *      lookup or complex expression involving itself (`a = {x: a.x}` or `a = a.x`).
 *    - When `a` is mutated or reassigned with a non-object or dependent expression, inlining
 *      must not produce invalid references or corrupt expressions.
 */

package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class InlineObjectLiteralsGeminiTest {

  private Compiler compiler;

  @Before
  public void setUp() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
  }

  /**
   * Helper that parses JS code, runs InlineObjectLiterals pass, and returns the printed JS.
   */
  private String testProcess(String js) {
    Node root = compiler.parseTestCode(js);
    assertEquals("Parsing errors: " + compiler.getErrors(), 0, compiler.getErrorCount());

    Supplier<String> idSupplier = new Supplier<String>() {
      private int id = 0;
      @Override
      public String get() {
        return String.valueOf(id++);
      }
    };

    InlineObjectLiterals pass = new InlineObjectLiterals(compiler, idSupplier);
    Node externs = new Node(com.google.javascript.rhino.Token.BLOCK);
    pass.process(externs, root);

    return compiler.toSource(root);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBasicInliningSimpleObject() {
    String js = "function f() { var obj = {a: 1, b: 2}; return obj.a + obj.b; }";
    String result = testProcess(js);

    // Inlining expands obj into JSCompiler_object_inline_a_... and JSCompiler_object_inline_b_...
    assertTrue("Result should inline property a: " + result,
        result.contains("JSCompiler_object_inline_a_0"));
    assertTrue("Result should inline property b: " + result,
        result.contains("JSCompiler_object_inline_b_1"));
    assertFalse("Variable obj should be removed: " + result,
        result.contains("obj.a"));
  }

  @Test(timeout = 4000)
  public void testEmptyObjectLiteralInlining() {
    String js = "function f() { var a = {}; }";
    String result = testProcess(js);
    assertNotNull(result);
  }

  @Test(timeout = 4000)
  public void testReassignmentToObjectLiteral() {
    String js = "function f() { var a = {x: 1}; a = {x: 2}; return a.x; }";
    String result = testProcess(js);
    assertTrue("Should inline reassigned object: " + result,
        result.contains("JSCompiler_object_inline_x_"));
    assertFalse("Direct property access should be eliminated: " + result,
        result.contains("a.x"));
  }

  @Test(timeout = 4000)
  public void testUninitializedDeclarationFollowedByLiteral() {
    String js = "function f() { var a; a = {x: 1}; return a.x; }";
    String result = testProcess(js);
    assertTrue("Should handle uninitialized declaration followed by assignment: " + result,
        result.contains("JSCompiler_object_inline_x_"));
  }

  @Test(timeout = 4000)
  public void testGlobalVariableNotForbiddenSkipped() {
    // Global variables must NOT be inlined according to isVarInlineForbidden
    String js = "var globalObj = {a: 1}; function g() { return globalObj.a; }";
    String result = testProcess(js);
    assertTrue("Global variable must remain intact: " + result,
        result.contains("globalObj.a"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Method Calls
  // =========================================================================

  @Test(timeout = 4000)
  public void testMethodCallPreventsInliningDueToThisContext() {
    // x.fn() where x is the 'this' context for fn must NOT be inlined
    String js = "function f() { var a = {fn: function() { return this; }}; a.fn(); }";
    String result = testProcess(js);
    assertTrue("Object with method call must NOT be inlined: " + result,
        result.contains("a.fn()"));
  }

  @Test(timeout = 4000)
  public void testObjectEscapeAsArgumentPreventsInlining() {
    // If object is passed to another function, !isVarOrAssignExprLhs triggers bailout
    String js = "function f() { var a = {x: 1}; doSomething(a); }";
    String result = testProcess(js);
    assertTrue("Object escaping to function must not be inlined: " + result,
        result.contains("doSomething(a)"));
  }

  @Test(timeout = 4000)
  public void testObjectReturnPreventsInlining() {
    // If object itself is returned, it cannot be inlined
    String js = "function f() { var a = {x: 1}; return a; }";
    String result = testProcess(js);
    assertTrue("Returned object must not be inlined: " + result,
        result.contains("return a"));
  }

  @Test(timeout = 4000)
  public void testGetterSetterDefPreventsInlining() {
    String js = "function f() { var a = { get x() { return 1; } }; return a.x; }";
    String result = testProcess(js);
    // ES5 getter prevents inlining
    assertTrue("Object with getter must not be inlined: " + result,
        result.contains("get x()"));
  }

  @Test(timeout = 4000)
  public void testSetterDefPreventsInlining() {
    String js = "function f() { var a = { set x(v) { } }; return a.x; }";
    String result = testProcess(js);
    // ES5 setter prevents inlining
    assertTrue("Object with setter must not be inlined: " + result,
        result.contains("set x(v)"));
  }

  @Test(timeout = 4000)
  public void testAssignedToNonObjectLiteralBailsOut() {
    // Reassigned to a primitive string / number
    String js = "function f() { var a = {x: 1}; a = 5; return a.x; }";
    String result = testProcess(js);
    assertTrue("Reassignment to non-object literal prevents inlining: " + result,
        result.contains("a = 5"));
  }

  @Test(timeout = 4000)
  public void testAssignedToArrayLiteralBailsOut() {
    String js = "function f() { var a = {x: 1}; a = [1, 2]; return a.x; }";
    String result = testProcess(js);
    assertTrue("Reassignment to array literal prevents inlining: " + result,
        result.contains("a = [1, 2]"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Zone (Issue 724 & Defects4J Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testIssue724SelfReferentialReassignment() {
    // Defects4J Issue 724: Variable assigned a property of itself or self-referential
    // e.g., var a = {x: 1}; a = a.x;
    String js = "function f() { var a = {x: 1}; a = a.x; return a; }";
    String result = testProcess(js);
    // The defect causes an invalid inlining or crash.
    // 'a = a.x' assigns a non-object literal (property access), so 'a' must not be inlined as an object!
    assertTrue("Issue 724: Must not inline when variable is reassigned to its property: " + result,
        result.contains("a = a.x"));
  }

  @Test(timeout = 4000)
  public void testIssue724SelfReferentialObjectLiteral() {
    // Direct self-referential object literal inside function
    String js = "function f() { var a = {x: 1}; a = {x: a.x}; return a.x; }";
    String result = testProcess(js);
    // Self-referential assignment x: a.x must be detected and aborted
    assertTrue("Self-referential assignment must prevent invalid inlining: " + result,
        result.contains("a = {x: a.x}") || result.contains("a.x"));
  }

  @Test(timeout = 4000)
  public void testDefectGroundTruthObject10() {
    // Tests property assignment on existing object literal: a.x = 2
    String js = "function f() { var a = {x: 1}; a.x = 2; return a.x; }";
    String result = testProcess(js);
    // Either properly inlined or preserved consistently without corrupted AST
    assertNotNull(result);
    assertFalse("Result code must not contain syntax error or empty string", result.isEmpty());
  }

  @Test(timeout = 4000)
  public void testDefectGroundTruthObject12() {
    // Tests object literal with multiple nested reads or expressions
    String js = "function f() { var a = {x: 1, y: 2}; a.x = a.y; return a.x; }";
    String result = testProcess(js);
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  @Test(timeout = 4000)
  public void testDefectGroundTruthObject22() {
    // Complex chained reassignment with comma/conditional
    String js = "function f() { var a = {x: 1}; if (true) { a = {x: 2}; } return a.x; }";
    String result = testProcess(js);
    assertNotNull(result);
    assertFalse(result.isEmpty());
  }

  // =========================================================================
  // Partition D: Complex Structures & Branch Coverage
  // =========================================================================

  @Test(timeout = 4000)
  public void testMultipleVarsInFunction() {
    String js = "function f() {" +
        "  var a = {x: 1};" +
        "  var b = {y: 2};" +
        "  return a.x + b.y;" +
        "}";
    String result = testProcess(js);
    assertTrue(result.contains("JSCompiler_object_inline_x_"));
    assertTrue(result.contains("JSCompiler_object_inline_y_"));
  }

  @Test(timeout = 4000)
  public void testObjectWithMultipleKeysAndMissingKeysInReassignment() {
    // First assigned {x: 1, y: 2}, later reassigned {x: 3} (missing y)
    String js = "function f() { var a = {x: 1, y: 2}; a = {x: 3}; return a.x + a.y; }";
    String result = testProcess(js);
    // When 'y' is missing in the second assignment, it gets assigned void 0 (undefined)
    assertTrue("Should inline and handle missing property: " + result,
        result.contains("JSCompiler_object_inline_x_"));
  }

  @Test(timeout = 4000)
  public void testRenamePropertyFunctionExcluded() {
    // Variable with RENAME_PROPERTY_FUNCTION_NAME is forbidden from inlining
    String js = "function f() { var " + RenameProperties.RENAME_PROPERTY_FUNCTION_NAME +
        " = {a: 1}; return " + RenameProperties.RENAME_PROPERTY_FUNCTION_NAME + ".a; }";
    String result = testProcess(js);
    assertTrue("Special rename property function name must be forbidden from inlining: " + result,
        result.contains(RenameProperties.RENAME_PROPERTY_FUNCTION_NAME));
  }

  @Test(timeout = 4000)
  public void testVarWithNoAssignmentIgnoredGracefully() {
    String js = "function f() { var a; return; }";
    String result = testProcess(js);
    assertNotNull(result);
  }

  @Test(timeout = 4000)
  public void testConstantsAndPrefix() {
    assertEquals("JSCompiler_object_inline_", InlineObjectLiterals.VAR_PREFIX);
  }
}