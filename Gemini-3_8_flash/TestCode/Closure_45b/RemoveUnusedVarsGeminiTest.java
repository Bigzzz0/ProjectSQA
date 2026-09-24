package com.google.javascript.jscomp;

/* [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Target Class: com.google.javascript.jscomp.RemoveUnusedVars
 *
 * KEY DEFECT TARGETED (Defects4J ground truth: RemoveUnusedVarsTest::testIssue618_1):
 * - Issue 618: In interpretAssigns(), when a variable is initialized without an initial value
 *   (e.g., `var b;`) and subsequently assigned an object or array literal inside an aliasing
 *   expression (e.g., `a.push(b = [])`), Assign.maybeAliased is set to true. However,
 *   interpretAssigns() failed to check `assign.maybeAliased`, treating `b` as not assigned
 *   to an unknown value. When subsequent property assignments occur (e.g., `b[0] = 1`),
 *   `b` was erroneously deemed unused and eliminated, destroying valid modifications.
 *
 * COVERAGE ZONE MATRIX:
 * 1. Partition A: Core Functional Logic & Continuations
 *    - Unused local/global variable removal.
 *    - Unreferenced function declarations removed lazily via Continuation.
 *    - Unreferenced trailing function parameters removal.
 *    - Arguments array escape ("arguments" identifier marking parameters referenced).
 *    - Subclass relationship (goog.inherits) tracking and lazy continuation traversal.
 *    - Call site parameter optimization with CallSiteOptimizer (removals and signature rewrites).
 *
 * 2. Partition B: Boundary Value Analysis & Edge Cases
 *    - Empty AST / script.
 *    - Variable declarations without initial values (var a;).
 *    - Multi-variable var statements (var a, b, c; removing only unused ones).
 *    - Side-effect preserving var removals (var a = foo(); -> foo();).
 *    - For-in loop variable preservation.
 *    - Named function expressions with preserveFunctionExpressionNames = true vs false.
 *    - Object literal setter parameter preservation (NodeUtil.isGetOrSetKey).
 *    - Call site excess parameters removal (tryRemoveAllFollowingArgs).
 *    - Call site replacement of intermediate parameters with 0 (toReplaceWithZero).
 *
 * 3. Partition C: Defect-Targeted Branch Zone
 *    - testIssue618_1: Array literal aliased in expression with property mutation.
 *    - testIssue618_2: Object literal aliased in chained assignment with property mutation.
 *    - Prototype assignments considered property assignments.
 *    - Assign.remove() with GETELEM expressions retaining side-effects in comma expressions.
 *    - Coding convention exported variables immunity from removal.
 *
 * 4. Partition D: Exception & Defensive Guard Paths
 *    - Unnormalized AST lifecycle stage guard (checkState).
 *    - Null SimpleDefinitionFinder when modifyCallSites is enabled (checkNotNull).
 *
 * 5. Partition E: Object Lifecycle & Contract Integrity
 *    - Re-traversal with fixed-point assignment interpretation.
 * =========================================================================================
 */

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.rhino.IR;
import com.google.javascript.rhino.Node;

public class RemoveUnusedVarsGeminiTest {

  private Compiler compiler;

  private Node compileAndRun(
      String js,
      boolean removeGlobals,
      boolean preserveFunctionExpressionNames,
      boolean modifyCallSites) {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention());
    compiler.initOptions(options);
    Node root = compiler.parseTestCode(js);
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    RemoveUnusedVars pass = new RemoveUnusedVars(
        compiler, removeGlobals, preserveFunctionExpressionNames, modifyCallSites);
    Node externs = IR.script();
    pass.process(externs, root);
    return root;
  }

  private static Node findNodeWithName(Node n, String name) {
    if (n.isName() && name.equals(n.getString())) {
      return n;
    }
    for (Node child = n.getFirstChild(); child != null; child = child.getNext()) {
      Node found = findNodeWithName(child, name);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private static Node findFunctionNode(Node n) {
    if (n.isFunction()) {
      return n;
    }
    for (Node c = n.getFirstChild(); c != null; c = c.getNext()) {
      Node res = findFunctionNode(c);
      if (res != null) {
        return res;
      }
    }
    return null;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & Continuations
  // =========================================================================

  @Test(timeout = 4000)
  public void testUnusedLocalVarRemoved() {
    String js = "function f() { var unused = 1; var used = 2; return used; }";
    Node root = compileAndRun(js, false, false, false);
    assertNull("Unused local variable should be removed", findNodeWithName(root, "unused"));
    assertNotNull("Used local variable should be preserved", findNodeWithName(root, "used"));
  }

  @Test(timeout = 4000)
  public void testUnusedGlobalVarRemovedWhenRemoveGlobalsTrue() {
    String js = "var unusedGlobal = 10; var usedGlobal = 20; alert(usedGlobal);";
    Node root = compileAndRun(js, true, false, false);
    assertNull("Unused global variable should be removed when removeGlobals is true",
        findNodeWithName(root, "unusedGlobal"));
    assertNotNull("Used global variable should be preserved", findNodeWithName(root, "usedGlobal"));
  }

  @Test(timeout = 4000)
  public void testUnusedGlobalVarRetainedWhenRemoveGlobalsFalse() {
    String js = "var unusedGlobal = 10;";
    Node root = compileAndRun(js, false, false, false);
    assertNotNull("Unused global variable must be kept when removeGlobals is false",
        findNodeWithName(root, "unusedGlobal"));
  }

  @Test(timeout = 4000)
  public void testUnusedFunctionDeclarationRemoved() {
    String js = "function unusedFn() {} function usedFn() {} usedFn();";
    Node root = compileAndRun(js, true, false, false);
    assertNull("Unused function declaration should be removed", findNodeWithName(root, "unusedFn"));
    assertNotNull("Used function declaration should be preserved", findNodeWithName(root, "usedFn"));
  }

  @Test(timeout = 4000)
  public void testUnreferencedFunctionArgsRemovedFromEnd() {
    String js = "function f(a, b, c) { return a; } f(1);";
    Node root = compileAndRun(js, false, false, false);
    assertNotNull("Referenced argument 'a' should be kept", findNodeWithName(root, "a"));
    assertNull("Trailing unreferenced argument 'c' should be stripped", findNodeWithName(root, "c"));
    assertNull("Trailing unreferenced argument 'b' should be stripped", findNodeWithName(root, "b"));
  }

  @Test(timeout = 4000)
  public void testArgumentsEscapedPreservesAllParameters() {
    String js = "function f(a, b) { return arguments[0]; } f(1, 2);";
    Node root = compileAndRun(js, false, false, false);
    assertNotNull("Param 'a' must be kept when 'arguments' is used", findNodeWithName(root, "a"));
    assertNotNull("Param 'b' must be kept when 'arguments' is used", findNodeWithName(root, "b"));
  }

  @Test(timeout = 4000)
  public void testInheritanceCallRemovedWhenSubclassUnused() {
    String js = "function Super() {} function Sub() {} goog.inherits(Sub, Super);";
    Node root = compileAndRun(js, true, false, false);
    assertNull("Unused subclass 'Sub' should be removed", findNodeWithName(root, "Sub"));
    assertNull("Unused superclass 'Super' should be removed", findNodeWithName(root, "Super"));
  }

  @Test(timeout = 4000)
  public void testInheritanceCallPreservedWhenSubclassUsed() {
    String js = "function Super() {} function Sub() {} goog.inherits(Sub, Super); new Sub();";
    Node root = compileAndRun(js, true, false, false);
    assertNotNull("Referenced subclass 'Sub' must be kept", findNodeWithName(root, "Sub"));
    assertNotNull("Superclass 'Super' referenced via goog.inherits must be kept",
        findNodeWithName(root, "Super"));
  }

  @Test(timeout = 4000)
  public void testModifyCallSitesOptimizesSignatureAndCallers() {
    String js = "function f(a, b) { return a; } f(1, 2);";
    Node root = compileAndRun(js, false, false, true);
    assertNotNull("Used parameter 'a' should be kept", findNodeWithName(root, "a"));
    assertNull("Unused parameter 'b' should be removed", findNodeWithName(root, "b"));
    String output = compiler.toSource(root);
    assertFalse("Call site should have parameter '2' removed", output.contains(", 2") || output.contains(",2"));
  }

  @Test(timeout = 4000)
  public void testModifyCallSitesReplacesUnremovableArgWithZero() {
    String js = "function f(a, b) { return b; } f(1, 2); window['f'] = f;";
    Node root = compileAndRun(js, false, false, true);
    String output = compiler.toSource(root);
    assertTrue("Intermediate argument should be replaced with 0: " + output,
        output.contains("f(0, 2)") || output.contains("f(0,2)"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Edge Cases
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScript() {
    Node root = compileAndRun("", true, false, false);
    assertNotNull("Root should remain valid for empty script", root);
  }

  @Test(timeout = 4000)
  public void testVarWithoutInitialValue() {
    String js = "var uninitialized;";
    Node root = compileAndRun(js, true, false, false);
    assertNull("Uninitialized unreferenced global var should be removed",
        findNodeWithName(root, "uninitialized"));
  }

  @Test(timeout = 4000)
  public void testMultipleVarDeclarations() {
    String js = "var a = 1, b = 2, c = 3; alert(a + c);";
    Node root = compileAndRun(js, true, false, false);
    assertNotNull("Variable 'a' must be kept", findNodeWithName(root, "a"));
    assertNull("Unused variable 'b' must be removed", findNodeWithName(root, "b"));
    assertNotNull("Variable 'c' must be kept", findNodeWithName(root, "c"));
  }

  @Test(timeout = 4000)
  public void testVarWithSideEffectInitializerPreservesSideEffect() {
    String js = "function sideEffect() {} function test() { var a = sideEffect(); } test();";
    Node root = compileAndRun(js, false, false, false);
    assertNull("Variable 'a' itself should be removed", findNodeWithName(root, "a"));
    assertNotNull("Call to sideEffect() must be preserved", findNodeWithName(root, "sideEffect"));
  }

  @Test(timeout = 4000)
  public void testForInLoopVarPreserved() {
    String js = "for (var prop in {x: 1}) {}";
    Node root = compileAndRun(js, true, false, false);
    assertNotNull("For-in loop variable must not be removed", findNodeWithName(root, "prop"));
  }

  @Test(timeout = 4000)
  public void testFunctionExpressionPreserveNameFalse() {
    String js = "var f = function myName() {}; alert(f);";
    Node root = compileAndRun(js, false, false, false);
    Node fn = findFunctionNode(root);
    assertNotNull(fn);
    assertEquals("Function expression name should be cleared when preserveFunctionExpressionNames=false",
        "", fn.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testFunctionExpressionPreserveNameTrue() {
    String js = "var f = function myName() {}; alert(f);";
    Node root = compileAndRun(js, false, true, false);
    Node fn = findFunctionNode(root);
    assertNotNull(fn);
    assertEquals("Function expression name should be preserved when preserveFunctionExpressionNames=true",
        "myName", fn.getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testObjectLiteralGetterSetterArgNotRemoved() {
    String js = "var obj = { set x(val) { alert(1); } }; alert(obj);";
    Node root = compileAndRun(js, false, false, false);
    assertNotNull("Setter parameter must not be removed", findNodeWithName(root, "val"));
  }

  @Test(timeout = 4000)
  public void testVarargsFunctionCannotModifyCallers() {
    String js = "function f(a) { return arguments.length; } f(1, 2);";
    Node root = compileAndRun(js, false, false, true);
    String output = compiler.toSource(root);
    assertTrue("Varargs function callers should not have arguments stripped: " + output,
        output.contains("1, 2") || output.contains("1,2"));
  }

  @Test(timeout = 4000)
  public void testExcessCallArgumentsRemoved() {
    String js = "function f(a) { return a; } f(1, 2, 3);";
    Node root = compileAndRun(js, false, false, true);
    String output = compiler.toSource(root);
    assertFalse("Excess argument 2 should be removed", output.contains(", 2") || output.contains(",2"));
    assertFalse("Excess argument 3 should be removed", output.contains(", 3") || output.contains(",3"));
  }

  @Test(timeout = 4000)
  public void testFunctionCallObjectCallFormat() {
    String js = "function f(a, b) { return a; } f.call(null, 1, 2);";
    Node root = compileAndRun(js, false, false, true);
    assertNotNull("Used parameter 'a' should be kept", findNodeWithName(root, "a"));
    assertNull("Unused parameter 'b' should be removed", findNodeWithName(root, "b"));
    String output = compiler.toSource(root);
    assertFalse("Excess argument '2' should be removed from f.call", output.contains(", 2") || output.contains(",2"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  /**
   * Targets Defects4J ground truth: RemoveUnusedVarsTest::testIssue618_1
   * Tests that an uninitialized variable aliased inside an expression with an
   * array literal retains its property assignments.
   */
  @Test(timeout = 4000)
  public void testIssue618_1() {
    String js = "function f() {\n" +
                "  var a = [], b;\n" +
                "  a.push(b = []);\n" +
                "  b[0] = 1;\n" +
                "  return a;\n" +
                "}";
    Node root = compileAndRun(js, false, false, false);
    String output = compiler.toSource(root);
    assertNotNull("Variable 'b' must not be removed when aliased in expression",
        findNodeWithName(root, "b"));
    assertTrue("Assignment b[0] = 1 must be preserved in output: " + output,
        output.contains("b[0]") || output.contains("b[0] = 1") || output.contains("b[0]=1"));
  }

  /**
   * Targets the object literal variant of Issue 618.
   */
  @Test(timeout = 4000)
  public void testIssue618_2() {
    String js = "function f() {\n" +
                "  var a = {}, b;\n" +
                "  a.b = b = {};\n" +
                "  b.c = 1;\n" +
                "  return a;\n" +
                "}";
    Node root = compileAndRun(js, false, false, false);
    String output = compiler.toSource(root);
    assertNotNull("Variable 'b' must not be removed when aliased in chained assignment",
        findNodeWithName(root, "b"));
    assertTrue("Assignment b.c = 1 must be preserved in output: " + output,
        output.contains("b.c") || output.contains("b.c = 1") || output.contains("b.c=1"));
  }

  @Test(timeout = 4000)
  public void testAssignRemoveWithSecondarySideEffectsAndGetElem() {
    String js = "function side() { return 0; } function test() { var a = []; a[side()] = 1; } test();";
    Node root = compileAndRun(js, false, false, false);
    assertNull("Unused local array 'a' should be removed", findNodeWithName(root, "a"));
    assertNotNull("side() expression inside GETELEM must be preserved", findNodeWithName(root, "side"));
  }

  @Test(timeout = 4000)
  public void testPrototypeAssignConsideredPropertyAssign() {
    String js = "function Foo() {} Foo.prototype.bar = function() {}; new Foo();";
    Node root = compileAndRun(js, true, false, false);
    assertNotNull("Foo constructor must be preserved", findNodeWithName(root, "Foo"));
  }

  @Test(timeout = 4000)
  public void testExportedVariableNotRemoved() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    options.setCodingConvention(new ClosureCodingConvention() {
      @Override
      public boolean isExported(String name) {
        return "EXPORTED_FLAG".equals(name) || super.isExported(name);
      }
    });
    compiler.initOptions(options);
    Node root = compiler.parseTestCode("var EXPORTED_FLAG = 42;");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(IR.script(), root);
    assertNotNull("Exported variable must not be removed", findNodeWithName(root, "EXPORTED_FLAG"));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testUnnormalizedAstThrowsIllegalStateException() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node root = compiler.parseTestCode("var x = 1;");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.RAW_AST);
    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, false);
    pass.process(IR.script(), root);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testModifyCallSitesWithNullDefinitionFinderThrowsNPE() {
    compiler = new Compiler();
    CompilerOptions options = new CompilerOptions();
    compiler.initOptions(options);
    Node root = compiler.parseTestCode("var x = 1;");
    compiler.setLifeCycleStage(AbstractCompiler.LifeCycleStage.NORMALIZED);
    RemoveUnusedVars pass = new RemoveUnusedVars(compiler, true, false, true);
    pass.process(IR.script(), root, null);
  }
}