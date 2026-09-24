/*
 * [Branch & Defect Analysis Matrix]
 * Target: com.google.javascript.jscomp.PureFunctionIdentifier
 *
 * Decision / Condition Coverage:
 * 1. process(externs, root):
 *    - externs != null || root != null guard -> throw IllegalStateException on second process() call.
 *    - propagation of side effects to fixed point and marking pure function calls.
 * 2. FunctionAnalyzer:
 *    - shouldTraverse: NodeUtil.isFunction(node) -> calls visitFunction.
 *    - visit: inExterns early return; nodeTypeMayHaveSideEffects check.
 *    - CALL / NEW tracking -> added to allFunctionCalls.
 *    - Enclosing function side effects tracking:
 *        * Assignment ops: LHS name in local scope vs outer/global scope (var == null || var.scope != scope).
 *        * LHS GETPROP: this.prop (taints this) vs obj.prop (taints unknown/global).
 *        * Unary ops (INC, DEC, DELPROP).
 *        * THROW token -> setFunctionThrows.
 *        * VAR name declaration (not a side effect, checked with Preconditions).
 *    - hasNoSideEffectsAnnotation:
 *        * JSDoc on function node with @nosideeffects.
 *        * JSDoc on parent name node (var f = function(){}).
 *        * JSDoc on parent assign node (f = function(){}).
 *        * @nosideeffects in non-externs -> traversal.report(INVALID_NO_SIDE_EFFECT_ANNOTATION).
 *        * @nosideeffects in externs -> setIsPure().
 * 3. SideEffectPropagationCallback:
 *    - Caller inherits mutatesGlobalState, functionThrows from callee.
 *    - Callee mutatesThis:
 *        * Regular call (not NEW) -> getCallThisObject:
 *            - obj is 'this' -> caller.setTaintsThis().
 *            - obj is not 'this' -> caller.setTaintsGlobalState().
 *            - '.call' or '.apply' invocation unwrapping.
 * 4. markPureFunctionCalls:
 *    - Call nodes with pure functions -> setIsNoSideEffectsCall().
 *    - NEW nodes -> pure if not mutating global state and not throwing (mutatesThis is allowed for NEW).
 * 5. Defect Zone (Defects4J):
 *    - Calls where the callee is an expression such as hook/ternary `(c ? f : g)()` or logical OR `(f || g)()`.
 *    - In the buggy implementation, getCallableDefinitions only permits NAME and GETPROP, returning null
 *      for OR/HOOK callee expressions, preventing call nodes from being marked as side-effect-free.
 */

package com.google.javascript.jscomp;

import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class PureFunctionIdentifierGeminiTest {

  private Node compileAndProcess(String externsJs, String srcJs,
                                 Compiler compiler,
                                 List<Node> callsOut,
                                 PureFunctionIdentifier[] pfiOut) {
    Node externsRoot = Node.newString(Token.BLOCK, "externsBlock");
    Node mainRoot = Node.newString(Token.BLOCK, "mainBlock");
    Node root = new Node(Token.BLOCK, externsRoot, mainRoot);

    Node externsTree = compiler.parseTestCode(externsJs);
    Node srcTree = compiler.parseTestCode(srcJs);

    while (externsTree.hasChildren()) {
      Node child = externsTree.removeFirstChild();
      externsRoot.addChildToBack(child);
    }
    while (srcTree.hasChildren()) {
      Node child = srcTree.removeFirstChild();
      mainRoot.addChildToBack(child);
    }

    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    defFinder.process(externsRoot, mainRoot);

    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    if (pfiOut != null && pfiOut.length > 0) {
      pfiOut[0] = pfi;
    }

    pfi.process(externsRoot, mainRoot);

    if (callsOut != null) {
      collectCalls(mainRoot, callsOut);
    }
    return mainRoot;
  }

  private void collectCalls(Node node, List<Node> calls) {
    if (node.isCall() || node.isNew()) {
      calls.add(node);
    }
    for (Node child = node.getFirstChild(); child != null; child = child.getNext()) {
      collectCalls(child, calls);
    }
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimplePureFunctionCallMarkedNoSideEffects() {
    Compiler compiler = new Compiler();
    List<Node> calls = new ArrayList<Node>();
    compileAndProcess(
        "",
        "function pureFunc(a) { return a + 1; }\n" +
        "var result = pureFunc(10);",
        compiler, calls, null);

    assertEquals(1, calls.size());
    Node callNode = calls.get(0);
    assertTrue("Call to pureFunc must be marked as having no side effects",
        callNode.isNoSideEffectsCall());
  }

  @Test(timeout = 4000)
  public void testImpureFunctionModifyingGlobalStateNotMarked() {
    Compiler compiler = new Compiler();
    List<Node> calls = new ArrayList<Node>();
    compileAndProcess(
        "",
        "var globalVar = 0;\n" +
        "function impureFunc() { globalVar++; }\n" +
        "impureFunc();",
        compiler, calls, null);

    assertEquals(1, calls.size());
    Node callNode = calls.get(0);
    assertFalse("Call to function modifying global state must not be no-side-effects",
        callNode.isNoSideEffectsCall());
  }

  @Test(timeout = 4000)
  public void testFunctionWithLocalMutationsIsPure() {
    Compiler compiler = new Compiler();
    List<Node> calls = new ArrayList<Node>();
    compileAndProcess(
        "",
        "function localPure(x) {\n" +
        "  var y = x;\n" +
        "  y++;\n" +
        "  y += 2;\n" +
        "  return y;\n" +
        "}\n" +
        "localPure(5);",
        compiler, calls, null);

    assertEquals(1, calls.size());
    assertTrue("Function modifying strictly local variables must remain pure",
        calls.get(0).isNoSideEffectsCall());
  }

  @Test(timeout = 4000)
  public void testConstructorModifyingThisIsPureForNew() {
    Compiler compiler = new Compiler();
    List<Node> calls = new ArrayList<Node>();
    compileAndProcess(
        "",
        "function MyClass(val) { this.val = val; }\n" +
        "var inst = new MyClass(42);",
        compiler, calls, null);

    assertEquals(1, calls.size());
    Node newCall = calls.get(0);
    assertTrue("NEW call to constructor modifying only 'this' has no side effects",
        newCall.isNoSideEffectsCall());
  }

  @Test(timeout = 4000)
  public void testConstructorWithThrowIsNotPureForNew() {
    Compiler compiler = new Compiler();
    List<Node> calls = new ArrayList<Node>();
    compileAndProcess(
        "",
        "function ThrowingClass() { throw 'error'; }\n" +
        "var inst = new ThrowingClass();",
        compiler, calls, null);

    assertEquals(1, calls.size());
    assertFalse("NEW call to throwing constructor must have side effects",
        calls.get(0).isNoSideEffectsCall());
  }

  @Test(timeout = 4000)
  public void testPropagationOfSideEffectsThroughCallChain() {
    Compiler compiler = new Compiler();
    List<Node> calls = new ArrayList<Node>();
    compileAndProcess(
        "",
        "var g = 1;\n" +
        "function leaf() { g = 2; }\n" +
        "function intermediate() { leaf(); }\n" +
        "function caller() { intermediate(); }\n" +
        "caller();",
        compiler, calls, null);

    for (Node call : calls) {
      assertFalse("All calls in chain to leaf mutating global should have side effects",
          call.isNoSideEffectsCall());
    }
  }

  @Test(timeout = 4000)
  public void testExternWithNoSideEffectsAnnotation() {
    Compiler compiler = new Compiler();
    List<Node> calls = new ArrayList<Node>();
    compileAndProcess(
        "/** @nosideeffects */ function Math_sin(x) {}\n",
        "var y = Math_sin(1.0);",
        compiler, calls, null);

    assertEquals(1, calls.size());
    assertTrue("Call to extern annotated with @nosideeffects must be marked pure",
        calls.get(0).isNoSideEffectsCall());
  }

  @Test(timeout = 4000)
  public void testExternWithoutNoSideEffectsAnnotationDefaultsToImpure() {
    Compiler compiler = new Compiler();
    List<Node> calls = new ArrayList<Node>();
    compileAndProcess(
        "function externImpure(x) {}\n",
        "externImpure(1);",
        compiler, calls, null);

    assertEquals(1, calls.size());
    assertFalse("Extern function without @nosideeffects must taint global state",
        calls.get(0).isNoSideEffectsCall());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyScriptsProduceEmptyReport() {
    Compiler compiler = new Compiler();
    PureFunctionIdentifier[] pfiArr = new PureFunctionIdentifier[1];
    compileAndProcess("", "", compiler, null, pfiArr);

    assertNotNull(pfiArr[0]);
    String report = pfiArr[0].getDebugReport();
    assertNotNull(report);
    assertTrue(report.contains("Pure functions:"));
  }

  @Test(timeout = 4000)
  public void testDebugReportWithPureAndImpureFunctions() {
    Compiler compiler = new Compiler();
    PureFunctionIdentifier[] pfiArr = new PureFunctionIdentifier[1];
    compileAndProcess(
        "/** @nosideeffects */ function extPure() {}",
        "var g = 0;\n" +
        "function p() { return 1; }\n" +
        "function imp() { g = 1; }\n" +
        "p(); imp(); extPure();",
        compiler, null, pfiArr);

    String report = pfiArr[0].getDebugReport();
    assertTrue("Debug report must list pure functions", report.contains("Pure functions:"));
    assertTrue("Debug report must mention 'p'", report.contains("p"));
    assertTrue("Debug report must mention 'imp'", report.contains("imp"));
  }

  @Test(timeout = 4000)
  public void testUnaryOperatorsOnUnknownObjectTaintsUnknown() {
    Compiler compiler = new Compiler();
    List<Node> calls = new ArrayList<Node>();
    compileAndProcess(
        "",
        "function mutateParam(obj) { delete obj.prop; }\n" +
        "mutateParam({});",
        compiler, calls, null);

    assertEquals(1, calls.size());
    assertFalse("DELPROP on non-this object taints unknown / has side effects",
        calls.get(0).isNoSideEffectsCall());
  }

  @Test(timeout = 4000)
  public void testThisCallPropagationWithCallAndApply() {
    Compiler compiler = new Compiler();
    List<Node> calls = new ArrayList<Node>();
    compileAndProcess(
        "",
        "function mutator() { this.x = 10; }\n" +
        "function helper() { mutator.call(this); }\n" +
        "function nonThisHelper(obj) { mutator.call(obj); }\n" +
        "helper(); nonThisHelper({});",
        compiler, calls, null);

    // helper called on global modifies global 'this', nonThisHelper modifies non-this obj (taints global)
    for (Node call : calls) {
      assertFalse(call.isNoSideEffectsCall());
    }
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J ground truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testCallFunctionFOrGOrHookPure() {
    // Directly reproduces the defect in PureFunctionIdentifier where callee
    // expressions like (f || g)() or (cond ? f : g)() were ignored because
    // getCallableDefinitions restricted callee to only NodeUtil.isName / isGetProp.
    Compiler compiler = new Compiler();
    List<Node> calls = new ArrayList<Node>();
    compileAndProcess(
        "",
        "function f() { return 1; }\n" +
        "function g() { return 2; }\n" +
        "function h() {\n" +
        "  return (f || g)();\n" +
        "}\n" +
        "h();",
        compiler, calls, null);

    // In a correct implementation, both f and g are pure, so (f || g)() has no side effects,
    // and call to h() has no side effects.
    boolean foundOrCall = false;
    for (Node call : calls) {
      if (call.getFirstChild().isOr()) {
        foundOrCall = true;
        assertTrue("(f || g)() must be recognized as having no side effects when both are pure",
            call.isNoSideEffectsCall());
      }
    }
    assertTrue("Must have encountered the OR call site", foundOrCall);
  }

  @Test(timeout = 4000)
  public void testCallFunctionHookPure() {
    Compiler compiler = new Compiler();
    List<Node> calls = new ArrayList<Node>();
    compileAndProcess(
        "",
        "function f() { return 1; }\n" +
        "function g() { return 2; }\n" +
        "function h(c) {\n" +
        "  return (c ? f : g)();\n" +
        "}\n" +
        "h(true);",
        compiler, calls, null);

    boolean foundHookCall = false;
    for (Node call : calls) {
      if (call.getFirstChild().isHook()) {
        foundHookCall = true;
        assertTrue("(c ? f : g)() must be marked no-side-effects when both branches are pure",
            call.isNoSideEffectsCall());
      }
    }
    assertTrue("Must have encountered the HOOK call site", foundHookCall);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testProcessTwiceThrowsIllegalStateException() {
    Compiler compiler = new Compiler();
    PureFunctionIdentifier[] pfiArr = new PureFunctionIdentifier[1];
    compileAndProcess("", "function a() {}", compiler, null, pfiArr);

    PureFunctionIdentifier pfi = pfiArr[0];
    assertNotNull(pfi);
    // Attempting to invoke process a second time must throw IllegalStateException
    pfi.process(new Node(Token.BLOCK), new Node(Token.BLOCK));
  }

  @Test(timeout = 4000)
  public void testInvalidNoSideEffectAnnotationInSourceReported() {
    Compiler compiler = new Compiler();
    compileAndProcess(
        "",
        "/** @nosideeffects */ function userFunc() { return 1; }",
        compiler, null, null);

    assertEquals("Invalid @nosideeffects annotation in source must trigger compiler error",
        1, compiler.getErrorCount());
    assertEquals(PureFunctionIdentifier.INVALID_NO_SIDE_EFFECT_ANNOTATION.key,
        compiler.getErrors()[0].getType().key);
  }

  @Test(timeout = 4000)
  public void testFunctionWithThrowIsImpure() {
    Compiler compiler = new Compiler();
    List<Node> calls = new ArrayList<Node>();
    compileAndProcess(
        "",
        "function mayThrow() { throw new Error('fail'); }\n" +
        "mayThrow();",
        compiler, calls, null);

    assertEquals(2, calls.size()); // new Error() and mayThrow()
    for (Node call : calls) {
      if (call.isCall() && call.getFirstChild().isName() &&
          call.getFirstChild().getString().equals("mayThrow")) {
        assertFalse("Function that throws must not be pure", call.isNoSideEffectsCall());
      }
    }
  }

  @Test(timeout = 4000)
  public void testVariableDeclarationsDoNotTaintFunction() {
    Compiler compiler = new Compiler();
    List<Node> calls = new ArrayList<Node>();
    compileAndProcess(
        "",
        "function withVars() {\n" +
        "  var a = 1, b = 2;\n" +
        "  return a + b;\n" +
        "}\n" +
        "withVars();",
        compiler, calls, null);

    assertEquals(1, calls.size());
    assertTrue("Function containing only variable declarations and return is pure",
        calls.get(0).isNoSideEffectsCall());
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Annotation Placements
  // =========================================================================

  @Test(timeout = 4000)
  public void testNoSideEffectsOnVarAndAssignInExterns() {
    Compiler compiler = new Compiler();
    List<Node> calls = new ArrayList<Node>();
    compileAndProcess(
        "/** @nosideeffects */ var extVarFunc = function() {};\n" +
        "var extObj = {};\n" +
        "/** @nosideeffects */ extObj.method = function() {};\n",
        "extVarFunc(); extObj.method();",
        compiler, calls, null);

    assertEquals(2, calls.size());
    for (Node call : calls) {
      assertTrue("Calls to externs annotated with @nosideeffects on var/assign should be pure",
          call.isNoSideEffectsCall());
    }
  }

  @Test(timeout = 4000)
  public void testGetDebugReportRequiresProcessCalledFirst() {
    Compiler compiler = new Compiler();
    SimpleDefinitionFinder defFinder = new SimpleDefinitionFinder(compiler);
    PureFunctionIdentifier pfi = new PureFunctionIdentifier(compiler, defFinder);
    try {
      pfi.getDebugReport();
      fail("getDebugReport() before process() must throw NullPointerException");
    } catch (NullPointerException expected) {
      // Expected precondition violation
    }
  }
}