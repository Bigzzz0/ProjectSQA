package com.google.javascript.jscomp;

import com.google.common.base.Supplier;
import com.google.javascript.rhino.Node;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: InlineObjectLiterals
 * Primary Branches & Logic Targeted:
 * 1. isVarInlineForbidden(Var var):
 *    - var.isGlobal() -> non-global variables in functions vs global scope
 *    - var.isExtern() -> extern variables
 *    - compiler.getCodingConvention().isExported(var.name) -> exported names
 *    - RenameProperties.RENAME_PROPERTY_FUNCTION_NAME.equals(var.name) -> JSCompiler_renameProperty
 *    - staleVars.contains(var) -> variables referenced inside replacement trees or already modified
 * 2. isInlinableObject(List<Reference> refs):
 *    - parent.getType() == Token.GETPROP -> property access
 *    - gramps.getType() == Token.CALL && gramps.getFirstChild() == parent -> x.fn() call target disqualifies inlining (preserves 'this')
 *    - !isVarOrAssignExprLhs(name) -> not VAR or direct assignment LHS (e.g., function arg, operand, in expr)
 *    - val == null -> var without assignment statement (e.g. var a;)
 *    - val.getType() != Token.OBJECTLIT -> assigned to non-object literal (e.g. a = 5)
 *    - child.getType() == Token.GET / Token.SET -> getter / setter ES5 properties prevent inlining
 *    - Self-referential detection: childVal has reference as parent -> e.g. a = {x: a.y}
 * 3. computeVarList(Var v, ReferenceCollection referenceInfo):
 *    - Gathers all property names from assignments and getprop accesses across references.
 *    - Creates unique variable names via safeNameIdSupplier.
 * 4. replaceAssignmentExpression(Var v, Reference ref, Map<String, String> varmap):
 *    - Transforms `x = {a: 1}` into comma expression `(JSCompiler_object_inline_a_0 = 1, true)`
 *    - Leaves missing keys set to `void 0` / undefined.
 * 5. splitObject(Var v, Reference declaration, Reference init, ReferenceCollection referenceInfo):
 *    - Handles well-defined VAR initialization vs separate assignment expressions.
 *    - Replaces GETPROP nodes (`a.x`) with NAME nodes (`JSCompiler_object_inline_x_0`).
 * 6. Defects4J Ground Truth Defect (Closure-87 / Issue 545):
 *    - testBug545: When an object property assignment is made inside conditions or complex expressions,
 *      or when an object literal contains property checks or isn't well defined (`var a; if (...) a = {...}`),
 *      or in a loop, or `var a = {a: foo()}` where unexpected AST structures interact with inlining.
 *      Specifically, Issue 545 is tested via `testBug545` in InlineObjectLiteralsTest:
 *      Inlining object literal when an uninitialized variable has an assignment or conditional access.
 */
public class InlineObjectLiteralsGeminiTest extends CompilerTestCase {

  private static final String PREFIX = InlineObjectLiterals.VAR_PREFIX;

  public InlineObjectLiteralsGeminiTest() {
    super();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    enableNormalize();
  }

  @Override
  protected CompilerPass getProcessor(final Compiler compiler) {
    return new InlineObjectLiterals(compiler, new Supplier<String>() {
      private int id = 0;
      @Override
      public String get() {
        return String.valueOf(id++);
      }
    });
  }

  @Override
  protected int getNumRepetitions() {
    return 1;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleInlineObjectLiteral() {
    test(
        "function local() { var a = {x: 1}; return a.x; }",
        "function local() { var JSCompiler_object_inline_x_0 = 1; return JSCompiler_object_inline_x_0; }"
    );
  }

  @Test(timeout = 4000)
  public void testMultipleKeysInlined() {
    test(
        "function local() { var a = {x: 1, y: 2}; return a.x + a.y; }",
        "function local() {" +
            " var JSCompiler_object_inline_x_0 = 1;" +
            " var JSCompiler_object_inline_y_1 = 2;" +
            " return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1;" +
        "}"
    );
  }

  @Test(timeout = 4000)
  public void testReassignmentToObjectLiteral() {
    test(
        "function local() {" +
        "  var a = {x: 1};" +
        "  a = {x: 2};" +
        "  return a.x;" +
        "}",
        "function local() {" +
        "  var JSCompiler_object_inline_x_0 = 1;" +
        "  JSCompiler_object_inline_x_0 = 2, true;" +
        "  return JSCompiler_object_inline_x_0;" +
        "}"
    );
  }

  @Test(timeout = 4000)
  public void testReassignmentWithDifferentKeysSetsUndefined() {
    test(
        "function local() {" +
        "  var a = {x: 1};" +
        "  a = {y: 2};" +
        "  return a.x + a.y;" +
        "}",
        "function local() {" +
        "  var JSCompiler_object_inline_x_0 = 1;" +
        "  var JSCompiler_object_inline_y_1 = void 0;" +
        "  JSCompiler_object_inline_y_1 = 2, JSCompiler_object_inline_x_0 = void 0, true;" +
        "  return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1;" +
        "}"
    );
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Disqualifying Conditions
  // =========================================================================

  @Test(timeout = 4000)
  public void testGlobalVariablesNeverInlined() {
    testSame("var a = {x: 1}; a.x;");
  }

  @Test(timeout = 4000)
  public void testNonObjectLiteralAssignmentDisqualifies() {
    testSame("function local() { var a = {x: 1}; a = 5; return a.x; }");
  }

  @Test(timeout = 4000)
  public void testDirectReferenceToObjectDisqualifies() {
    testSame("function local() { var a = {x: 1}; use(a); return a.x; }");
  }

  @Test(timeout = 4000)
  public void testMethodCallOnObjectDisqualifiesDueToThisContext() {
    // Calling a method on the object like a.fn() passes `a` as `this`, so inlining is forbidden
    testSame("function local() { var a = {fn: function() { return this; }}; a.fn(); }");
  }

  @Test(timeout = 4000)
  public void testGetterSetterDisqualifies() {
    testSame("function local() { var a = { get x() { return 1; } }; return a.x; }");
    testSame("function local() { var a = { set x(v) { } }; a.x = 2; }");
  }

  @Test(timeout = 4000)
  public void testSelfReferentialObjectDisqualifies() {
    testSame("function local() { var a = {x: 1}; a = {x: a.x}; return a.x; }");
  }

  @Test(timeout = 4000)
  public void testEmptyObjectLiteral() {
    test(
        "function local() { var a = {}; a.x = 1; return a.x; }",
        "function local() { var a; a.x = 1; return a.x; }"
    );
  }

  @Test(timeout = 4000)
  public void testExportedOrSpecialNamedVariablesForbidden() {
    testSame("function local() { var JSCompiler_renameProperty = {x: 1}; return JSCompiler_renameProperty.x; }");
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets Closure Issue 545 / Defects4J bug condition.
   * Tests object inlining where an object variable is declared and later assigned
   * in conditionally executed blocks or multiple paths where properties are initialized
   * conditionally or in statements like `var a; if (c) { a = {foo: 1}; }`.
   */
  @Test(timeout = 4000)
  public void testBug545() {
    test(
        "function local() {" +
        "  var a;" +
        "  if (true) {" +
        "    a = {foo: 1};" +
        "  } else {" +
        "    a = {foo: 2};" +
        "  }" +
        "  return a.foo;" +
        "}",
        "function local() {" +
        "  var JSCompiler_object_inline_foo_0;" +
        "  if (true) {" +
        "    JSCompiler_object_inline_foo_0 = 1, true;" +
        "  } else {" +
        "    JSCompiler_object_inline_foo_0 = 2, true;" +
        "  }" +
        "  return JSCompiler_object_inline_foo_0;" +
        "}"
    );
  }

  @Test(timeout = 4000)
  public void testBug545VariantUnassignedVarWithUsage() {
    // When declared without initial assignment and modified later
    test(
        "function local() {" +
        "  var a;" +
        "  a = {x: 1};" +
        "  return a.x;" +
        "}",
        "function local() {" +
        "  var JSCompiler_object_inline_x_0;" +
        "  JSCompiler_object_inline_x_0 = 1, true;" +
        "  return JSCompiler_object_inline_x_0;" +
        "}"
    );
  }

  // =========================================================================
  // Partition D: Complex Expressions, Scopes & Chained Operations
  // =========================================================================

  @Test(timeout = 4000)
  public void testObjectLiteralWithMultipleAssignmentsAndReferences() {
    test(
        "function local() {" +
        "  var a = {x: 1, y: 2};" +
        "  var b = a.x;" +
        "  a.y = 3;" +
        "  return b + a.y;" +
        "}",
        "function local() {" +
        "  var JSCompiler_object_inline_x_0 = 1;" +
        "  var JSCompiler_object_inline_y_1 = 2;" +
        "  var b = JSCompiler_object_inline_x_0;" +
        "  JSCompiler_object_inline_y_1 = 3;" +
        "  return b + JSCompiler_object_inline_y_1;" +
        "}"
    );
  }

  @Test(timeout = 4000)
  public void testVariableReferencedInExpressionStatement() {
    test(
        "function local() {" +
        "  var a = {x: 1};" +
        "  a.x;" +
        "}",
        "function local() {" +
        "  var JSCompiler_object_inline_x_0 = 1;" +
        "  JSCompiler_object_inline_x_0;" +
        "}"
    );
  }

  @Test(timeout = 4000)
  public void testStaleVarsBlacklistingFromAssignments() {
    // When property assignment references an inner variable `y`, blacklisting prevents corrupted inlining of `y`
    test(
        "function local() {" +
        "  var y = 10;" +
        "  var a = {x: y};" +
        "  return a.x;" +
        "}",
        "function local() {" +
        "  var y = 10;" +
        "  var JSCompiler_object_inline_x_0 = y;" +
        "  return JSCompiler_object_inline_x_0;" +
        "}"
    );
  }

  @Test(timeout = 4000)
  public void testNestedFunctionDoesNotDisqualifyOuterVarIfNotEscaping() {
    test(
        "function local() {" +
        "  var a = {x: 1};" +
        "  function inner() {" +
        "    return 2;" +
        "  }" +
        "  return a.x + inner();" +
        "}",
        "function local() {" +
        "  var JSCompiler_object_inline_x_0 = 1;" +
        "  function inner() {" +
        "    return 2;" +
        "  }" +
        "  return JSCompiler_object_inline_x_0 + inner();" +
        "}"
    );
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Edge Boundaries
  // =========================================================================

  @Test(timeout = 4000)
  public void testVarPrefixIntegrity() {
    assertEquals("JSCompiler_object_inline_", InlineObjectLiterals.VAR_PREFIX);
  }

  @Test(timeout = 4000)
  public void testMultipleDeclarationsInSameScope() {
    test(
        "function local() {" +
        "  var a = {x: 1};" +
        "  var b = {y: 2};" +
        "  return a.x + b.y;" +
        "}",
        "function local() {" +
        "  var JSCompiler_object_inline_x_0 = 1;" +
        "  var JSCompiler_object_inline_y_1 = 2;" +
        "  return JSCompiler_object_inline_x_0 + JSCompiler_object_inline_y_1;" +
        "}"
    );
  }

  @Test(timeout = 4000)
  public void testUnreadPropertyAssignment() {
    test(
        "function local() {" +
        "  var a = {x: 1};" +
        "  a.x = 2;" +
        "}",
        "function local() {" +
        "  var JSCompiler_object_inline_x_0 = 1;" +
        "  JSCompiler_object_inline_x_0 = 2;" +
        "}"
    );
  }
}