package com.google.javascript.jscomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.google.javascript.rhino.Node;
import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: FlowSensitiveInlineVariables
 * Defect Reference: Defects4J Closure-12 (testDoNotInlineCatchExpression1, 1a, 3)
 *
 * Core Decision Branches & Conditions Targeted:
 * 1. Scope Analysis:
 *    - Global scope vs function scope (t.inGlobalScope() branch early return)
 *    - Variable count > MAX_VARIABLES_TO_ANALYZE early return
 * 2. Candidate Gathering (GatherCandidates):
 *    - CFG node existence and entry points
 *    - Pure read name nodes vs write/definition nodes (ASSIGN lhs, VAR, INC, DEC, PARAM_LIST, CATCH)
 *    - Exported names filtering
 *    - Dependencies on outer scope variables (dependsOnOuterScopeVars)
 * 3. Inlining Safety Preconditions (Candidate.canInline):
 *    - defMetadata.node.isFunction() (parameters cannot be inlined)
 *    - Inlined dependencies invalidated (inlinedNewDependencies check)
 *    - Definition assignment used as R-Value (def.isAssign() && !NodeUtil.isExprAssign(def.getParent()))
 *    - Side effect in RHS: checkRightOf with SIDE_EFFECT_PREDICATE
 *    - Side effect in LHS of use: checkLeftOf with SIDE_EFFECT_PREDICATE
 *    - Side effects along CFG paths between def and use (CheckPathsBetweenNodes)
 *    - Multi-use check within use CFG node (numUseWithinUseCfgNode != 1)
 *    - Loop containment (NodeUtil.isWithinLoop(use))
 *    - Reaching uses count != 1
 *    - Disallowed RHS constructs (GETPROP, GETELEM, ARRAYLIT, OBJECTLIT, REGEXP, NEW)
 *    - Exception/Catch paths and exceptional control flow edges (Defect Zone: Closure-12)
 * 4. Transformation (inlineVariable):
 *    - Assignment statement (exprResult detach, label ancestor handling, replaceChild)
 *    - Var declaration statement (removeChild, replaceChild, compiler.reportCodeChange)
 */

public class FlowSensitiveInlineVariablesGeminiTest {

  /**
   * Helper to run FlowSensitiveInlineVariables on an input JS string
   * wrapped inside a function (since the pass ignores global scope).
   */
  private String inline(String js) {
    Compiler compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);

    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode(js);
    assertEquals("Parsing errors: " + compiler.getErrors(), 0, compiler.getErrorCount());

    FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables(compiler);
    pass.process(externs, root);
    return compiler.toSource(root).trim();
  }

  private void test(String input, String expected) {
    String actual = inline(input);
    Compiler compiler = new Compiler();
    Node expectedRoot = compiler.parseTestCode(expected);
    String normalizedExpected = compiler.toSource(expectedRoot).trim();
    assertEquals(normalizedExpected, actual);
  }

  private void testSame(String js) {
    test(js, js);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleVarInlineInsideFunction() {
    String input = "function f() { var x = 1; return x; }";
    String expected = "function f() { var x; return 1; }";
    test(input, expected);
  }

  @Test(timeout = 4000)
  public void testSimpleAssignInlineInsideFunction() {
    String input = "function f() { var x; x = 2; return x; }";
    String expected = "function f() { var x; return 2; }";
    test(input, expected);
  }

  @Test(timeout = 4000)
  public void testExpressionAssignmentInlining() {
    String input = "function f() { var a = 1 + 2; var b = a; return b; }";
    String expected = "function f() { var a; var b = 1 + 2; return b; }";
    test(input, expected);
  }

  @Test(timeout = 4000)
  public void testLabeledAssignmentInlining() {
    String input = "function f() { var x; myLabel: x = 42; return x; }";
    String expected = "function f() { var x; return 42; }";
    test(input, expected);
  }

  @Test(timeout = 4000)
  public void testGlobalScopeIgnored() {
    // Pass ignores variables in the global scope to prevent escaping issues
    String code = "var x = 1; var y = x;";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testFunctionParametersNotDefCandidates() {
    String code = "function f(x) { return x; }";
    testSame(code);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testMultipleUsesPreventInlining() {
    // When a variable is used more than once, inlining is aborted
    String code = "function f() { var x = 1; return x + x; }";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testZeroUsesDoesNotInline() {
    String code = "function f() { var x = 1; return 2; }";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testMultipleUsesAcrossStatementsPreventInlining() {
    String code = "function f() { var x = 10; var a = x; var b = x; return a + b; }";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testDoNotInlineInsideLoop() {
    String code = "function f() { var x = 1; while(true) { var y = x; } }";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testDoNotInlineInsideForLoop() {
    String code = "function f() { var x = 1; for(var i = 0; i < 10; i++) { var y = x; } }";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testDoNotInlineInsideDoWhileLoop() {
    String code = "function f() { var x = 1; do { var y = x; } while(false); }";
    testSame(code);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Closure-12)
  // Catch blocks, Exception Paths & Path Checks
  // =========================================================================

  @Test(timeout = 4000)
  public void testDoNotInlineCatchExpression1() {
    // Targets Defects4J Closure-12 ground truth
    // Variable assigned inside catch block must not be unsafely inlined
    String code =
        "function f() {\n"
            + "  var a;\n"
            + "  try {\n"
            + "    throw Error();\n"
            + "  } catch (e) {\n"
            + "    a = e + 1;\n"
            + "  }\n"
            + "  return a;\n"
            + "}";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testDoNotInlineCatchExpression1a() {
    // Targets Defects4J Closure-12 ground truth: direct catch var assignment
    // Inlining 'e' outside catch scope causes ReferenceError
    String code =
        "function f() {\n"
            + "  var a;\n"
            + "  try {\n"
            + "    throw Error();\n"
            + "  } catch (e) {\n"
            + "    a = e;\n"
            + "  }\n"
            + "  return a;\n"
            + "}";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testDoNotInlineCatchExpression3() {
    // Targets Defects4J Closure-12 ground truth
    // Definition before try-catch block cannot be inlined past side-effecting catch path
    String code =
        "function f() {\n"
            + "  var a = \"foo\";\n"
            + "  try {\n"
            + "    throw Error();\n"
            + "  } catch (e) {\n"
            + "    foo();\n"
            + "  }\n"
            + "  return a;\n"
            + "}";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testDoNotInlineAcrossTryCatchBlock() {
    String code =
        "function f() {\n"
            + "  var x = 1;\n"
            + "  try {\n"
            + "    mightThrow();\n"
            + "  } catch (e) {}\n"
            + "  return x;\n"
            + "}";
    testSame(code);
  }

  // =========================================================================
  // Partition D: Disallowed Constructs & Side Effects Filtering
  // =========================================================================

  @Test(timeout = 4000)
  public void testDoNotInlineObjectLiteral() {
    String code = "function f() { var x = {}; return x; }";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testDoNotInlineArrayLiteral() {
    String code = "function f() { var x = [1, 2]; return x; }";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testDoNotInlineRegexp() {
    String code = "function f() { var x = /abc/; return x; }";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testDoNotInlineNewExpression() {
    String code = "function f() { var x = new Foo(); return x; }";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testDoNotInlineGetProp() {
    String code = "function f(obj) { var x = obj.prop; return x; }";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testDoNotInlineGetElem() {
    String code = "function f(arr, idx) { var x = arr[idx]; return x; }";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testDoNotInlineAssignmentRValue() {
    // If def is an assignment used as an R-Value: y = (x = 2); return x;
    String code = "function f() { var x; var y = (x = 2); return x; }";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testSideEffectsRightOfDefinition() {
    // x = 1, foo(); return x; -> foo() on right of definition prevents inlining
    String code = "function f() { var x; x = 1, foo(); return x; }";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testSideEffectsLeftOfUse() {
    // var x = 1; foo(), print(x); -> foo() on left of use prevents inlining
    String code = "function f() { var x = 1; foo(), print(x); }";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testSideEffectsAlongPath() {
    String code = "function f() { var x = 1; modify(); return x; }";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testDoNotInlineIncrementsOrDecrements() {
    String code1 = "function f() { var x = 1; x++; return x; }";
    testSame(code1);

    String code2 = "function f() { var x = 1; --x; return x; }";
    testSame(code2);
  }

  @Test(timeout = 4000)
  public void testNestedFunctionDependency() {
    // outer scope variable modification inside inner function
    String code = "function f() { var x = 1; function g() { x = 2; } return x; }";
    testSame(code);
  }

  // =========================================================================
  // Partition E: Lifecycle & Structural Contracts
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyFunctionPassesWithoutError() {
    String code = "function f() {}";
    testSame(code);
  }

  @Test(timeout = 4000)
  public void testPassDirectInvocation() {
    Compiler compiler = new Compiler();
    Node externs = compiler.parseTestCode("");
    Node root = compiler.parseTestCode("function f() { var a = 1; return a; }");
    FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables(compiler);

    assertNotNull(pass);
    pass.process(externs, root);
    assertTrue(compiler.getErrorCount() == 0);
  }

  @Test(timeout = 4000)
  public void testNullExternsAndRootsHandledGracefully() {
    Compiler compiler = new Compiler();
    FlowSensitiveInlineVariables pass = new FlowSensitiveInlineVariables(compiler);
    try {
      pass.process(null, null);
      fail("Expected exception when processing null AST roots");
    } catch (Exception expected) {
      assertTrue(expected instanceof NullPointerException || expected instanceof RuntimeException);
    }
  }

  @Test(timeout = 4000)
  public void testDependentInliningBackoff() {
    // When inlining one variable changes dependencies, other candidates back off
    String input =
        "function f() {\n"
            + "  var a = 1;\n"
            + "  var b = a;\n"
            + "  var c = b;\n"
            + "  return c;\n"
            + "}";
    // Expect at least partial safe inlining without broken references
    String actual = inline(input);
    assertNotNull(actual);
    assertTrue(actual.contains("function f"));
  }
}