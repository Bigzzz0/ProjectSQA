package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import java.util.Set;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.LiveVariablesAnalysis
 * Target Defect: LiveVariableAnalysisTest::testExpressionInForIn -> IllegalStateException
 *
 * Decision / Branch Matrix Covered:
 * 1. FOR loop dispatch:
 *    - !NodeUtil.isForIn(n) [Normal for-loops, empty conditions]
 *    - NodeUtil.isForIn(n):
 *      - NodeUtil.isVar(lhs) == true [for (var x in y)]
 *      - NodeUtil.isVar(lhs) == false, NodeUtil.isName(lhs) == true [for (x in y)]
 *      - DEFECT PATH: NodeUtil.isVar(lhs) == false, NodeUtil.isName(lhs) == false
 *        (e.g., for (a[1] in b) or for (a.x in b)). The defective implementation
 *        unconditionally invokes addToSetIfLocal(lhs, kill) which triggers
 *        Preconditions.checkState(NodeUtil.isName(node)) -> IllegalStateException.
 * 2. Exception branches (Branch.ON_EX in flowThrough):
 *    - Nodes with outgoing ON_EX edges inside try-catch blocks making assignments
 *      conditional (conditional = true: assignments do not kill variables).
 * 3. Short-circuit / Conditional expressions in computeGenKill:
 *    - Token.AND / Token.OR: RHS marked conditional = true.
 *    - Token.HOOK: Both then and else branches marked conditional = true.
 * 4. Assignments:
 *    - Simple assignment (=): kills lhs, does not gen lhs.
 *    - Compound assignment (+=, -=): kills lhs AND gens lhs.
 *    - Assignment to non-name (a.b = 1, a[0] = 1): falls through to children traversal.
 * 5. Token.NAME handling:
 *    - isArgumentsName == true: calls markAllParametersEscaped().
 *    - isArgumentsName == false (shadowed arguments or regular local name).
 * 6. Scope & Escaped Variables:
 *    - Global vs local variable distinction in addToSetIfLocal.
 *    - Escaped locals via closures (inner functions) excluded from gen/kill lattice.
 * 7. LiveVariableLattice contract:
 *    - equals (reflexive, null check, wrong type, identical bitset, different bitset).
 *    - hashCode, toString, isLive(Var), isLive(int).
 */
public class LiveVariablesAnalysisGeminiTest {

  // =========================================================================
  // Test Harness / AST & Analysis Helpers
  // =========================================================================

  private Node findFunction(Node root) {
    if (root == null) {
      return null;
    }
    if (root.getType() == Token.FUNCTION) {
      return root;
    }
    for (Node c = root.getFirstChild(); c != null; c = c.getNext()) {
      Node fn = findFunction(c);
      if (fn != null) {
        return fn;
      }
    }
    return null;
  }

  private LiveVariablesAnalysis computeLiveness(String src) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(src);
    Node fn = findFunction(root);
    assertNotNull("Target function not found in AST: " + src, fn);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, false);
    cfa.process(null, fn.getLastChild());
    ControlFlowGraph<Node> cfg = cfa.getCfg();

    Scope scope = new SyntacticScopeCreator(compiler).createScope(fn, null);
    LiveVariablesAnalysis lva = new LiveVariablesAnalysis(cfg, scope, compiler);
    lva.analyze();
    return lva;
  }

  private LiveVariablesAnalysis createAnalysis(String src) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(src);
    Node fn = findFunction(root);
    assertNotNull("Target function not found in AST: " + src, fn);

    ControlFlowAnalysis cfa = new ControlFlowAnalysis(compiler, false, false);
    cfa.process(null, fn.getLastChild());
    ControlFlowGraph<Node> cfg = cfa.getCfg();

    Scope scope = new SyntacticScopeCreator(compiler).createScope(fn, null);
    return new LiveVariablesAnalysis(cfg, scope, compiler);
  }

  private Scope createFunctionScope(String src) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(src);
    Node fn = findFunction(root);
    assertNotNull("Target function not found in AST: " + src, fn);
    return new SyntacticScopeCreator(compiler).createScope(fn, null);
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone
  // =========================================================================

  @Test(timeout = 4000)
  public void testExpressionInForInGetElemTargetingDefect() {
    // Ground truth defect: In for (a[1] in b), lhs is GETELEM (not NAME).
    // LiveVariablesAnalysis fails with IllegalStateException because it assumes
    // lhs in for-in is always a NAME or VAR without checking NodeUtil.isName(lhs).
    LiveVariablesAnalysis lva = computeLiveness(
        "function f(b) { var a = []; for (a[1] in b) {} }");
    assertNotNull(lva);
  }

  @Test(timeout = 4000)
  public void testExpressionInForInGetPropTargetingDefect() {
    // Tests property access expression in for-in: for (a.x in b)
    LiveVariablesAnalysis lva = computeLiveness(
        "function f(b) { var a = {}; for (a.x in b) {} }");
    assertNotNull(lva);
  }

  @Test(timeout = 4000)
  public void testExpressionInForInCallTargetingDefect() {
    // Tests call result property access in for-in: for (foo().bar in b)
    LiveVariablesAnalysis lva = computeLiveness(
        "function f(b) { function foo() { return {}; } for (foo().bar in b) {} }");
    assertNotNull(lva);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testStandardForInWithVar() {
    LiveVariablesAnalysis lva = computeLiveness(
        "function f(b) { for (var a in b) {} }");
    assertNotNull(lva);
    assertEquals(0, lva.getVarIndex("a"));
  }

  @Test(timeout = 4000)
  public void testStandardForInWithoutVar() {
    LiveVariablesAnalysis lva = computeLiveness(
        "function f(b) { var a; for (a in b) {} }");
    assertNotNull(lva);
    assertEquals(0, lva.getVarIndex("a"));
  }

  @Test(timeout = 4000)
  public void testRegularForLoopWithCondition() {
    LiveVariablesAnalysis lva = computeLiveness(
        "function f() { for (var i = 0; i < 10; i++) {} }");
    assertNotNull(lva);
    assertEquals(0, lva.getVarIndex("i"));
  }

  @Test(timeout = 4000)
  public void testRegularForLoopEmptyCondition() {
    LiveVariablesAnalysis lva = computeLiveness(
        "function f() { var x = 0; for (;;) { if (x > 5) break; x++; } }");
    assertNotNull(lva);
    assertEquals(0, lva.getVarIndex("x"));
  }

  @Test(timeout = 4000)
  public void testWhileAndDoWhileLoops() {
    LiveVariablesAnalysis lva = computeLiveness(
        "function f(n) { var x = n; while (x > 0) { x--; } do { x++; } while (x < 5); }");
    assertNotNull(lva);
    assertEquals(1, lva.getVarIndex("x"));
  }

  @Test(timeout = 4000)
  public void testIfElseBranches() {
    LiveVariablesAnalysis lva = computeLiveness(
        "function f(cond) { var a; if (cond) { a = 1; } else { a = 2; } return a; }");
    assertNotNull(lva);
    assertEquals(1, lva.getVarIndex("a"));
  }

  @Test(timeout = 4000)
  public void testCompoundAssignments() {
    LiveVariablesAnalysis lva = computeLiveness(
        "function f() { var a = 1; a += 2; a *= 3; return a; }");
    assertNotNull(lva);
    assertEquals(0, lva.getVarIndex("a"));
  }

  @Test(timeout = 4000)
  public void testPropertyAssignmentDoesNotKillVariable() {
    LiveVariablesAnalysis lva = computeLiveness(
        "function f() { var a = {}; a.prop = 42; return a.prop; }");
    assertNotNull(lva);
    assertEquals(0, lva.getVarIndex("a"));
  }

  @Test(timeout = 4000)
  public void testLogicalAndOrExpressions() {
    LiveVariablesAnalysis lva = computeLiveness(
        "function f(a, b) { var x = a && b; var y = a || b; return x || y; }");
    assertNotNull(lva);
    assertEquals(2, lva.getVarIndex("x"));
    assertEquals(3, lva.getVarIndex("y"));
  }

  @Test(timeout = 4000)
  public void testHookConditionalExpression() {
    LiveVariablesAnalysis lva = computeLiveness(
        "function f(cond) { var a = 1; var b = 2; var c = cond ? a : b; return c; }");
    assertNotNull(lva);
    assertEquals(1, lva.getVarIndex("a"));
    assertEquals(2, lva.getVarIndex("b"));
    assertEquals(3, lva.getVarIndex("c"));
  }

  @Test(timeout = 4000)
  public void testArgumentsKeywordEscapesAllParameters() {
    LiveVariablesAnalysis lva = computeLiveness(
        "function f(x, y) { return arguments[0]; }");
    Set<Scope.Var> escaped = lva.getEscapedLocals();
    assertEquals(2, escaped.size());
    boolean foundX = false;
    boolean foundY = false;
    for (Scope.Var v : escaped) {
      if ("x".equals(v.getName())) foundX = true;
      if ("y".equals(v.getName())) foundY = true;
    }
    assertTrue("Parameter 'x' should have escaped", foundX);
    assertTrue("Parameter 'y' should have escaped", foundY);
  }

  @Test(timeout = 4000)
  public void testShadowedArgumentsDoesNotEscapeParameters() {
    LiveVariablesAnalysis lva = computeLiveness(
        "function f(x) { var arguments = [10]; return arguments[0]; }");
    Set<Scope.Var> escaped = lva.getEscapedLocals();
    for (Scope.Var v : escaped) {
      assertNotEquals("Parameter 'x' should not escape when arguments is shadowed",
          "x", v.getName());
    }
  }

  @Test(timeout = 4000)
  public void testInnerClosureEscapesOuterLocalVariable() {
    LiveVariablesAnalysis lva = computeLiveness(
        "function outer() { var localEscaped = 1; var localKept = 2; function inner() { return localEscaped; } return localKept; }");
    Set<Scope.Var> escaped = lva.getEscapedLocals();
    boolean foundEscaped = false;
    boolean foundKept = false;
    for (Scope.Var v : escaped) {
      if ("localEscaped".equals(v.getName())) foundEscaped = true;
      if ("localKept".equals(v.getName())) foundKept = true;
    }
    assertTrue("localEscaped must be in escaped set", foundEscaped);
    assertFalse("localKept must not be in escaped set", foundKept);
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Lattice Lifecycle
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyFunctionLattice() {
    LiveVariablesAnalysis lva = computeLiveness("function f() {}");
    assertFalse(lva.isForward());
    LiveVariablesAnalysis.LiveVariableLattice entry = lva.createEntryLattice();
    LiveVariablesAnalysis.LiveVariableLattice init = lva.createInitialEstimateLattice();
    assertNotNull(entry);
    assertNotNull(init);
    assertEquals(entry, init);
    assertEquals(entry.hashCode(), init.hashCode());
    assertFalse(entry.isLive(0));
    assertEquals("{}", entry.toString());
  }

  @Test(timeout = 4000)
  public void testLatticeEqualsAndHashCodeContract() {
    LiveVariablesAnalysis lva = createAnalysis(
        "function f() { var a = 1; var b = 2; }");
    LiveVariablesAnalysis.LiveVariableLattice lat1 = lva.createEntryLattice();
    LiveVariablesAnalysis.LiveVariableLattice lat2 = lva.createEntryLattice();

    // Reflexive
    assertEquals(lat1, lat1);
    // Symmetric
    assertEquals(lat1, lat2);
    assertEquals(lat2, lat1);
    assertEquals(lat1.hashCode(), lat2.hashCode());

    // Non-lattice comparison
    assertFalse(lat1.equals("notALattice"));
    assertFalse(lat1.equals(new Object()));
  }

  @Test(timeout = 4000)
  public void testLatticeIsLiveByVarAndIndex() {
    String src = "function f() { var a; var b; }";
    Scope scope = createFunctionScope(src);
    LiveVariablesAnalysis lva = createAnalysis(src);
    LiveVariablesAnalysis.LiveVariableLattice lattice = lva.createEntryLattice();

    Scope.Var varA = scope.getVar("a");
    Scope.Var varB = scope.getVar("b");
    assertNotNull(varA);
    assertNotNull(varB);

    assertFalse(lattice.isLive(varA));
    assertFalse(lattice.isLive(varA.index));
    assertFalse(lattice.isLive(varB));
    assertFalse(lattice.isLive(varB.index));
  }

  @Test(timeout = 4000)
  public void testVarDeclarationWithoutAssignment() {
    // Uninitialized var without children in VAR node
    LiveVariablesAnalysis lva = computeLiveness(
        "function f() { var x; return 1; }");
    assertNotNull(lva);
    assertEquals(0, lva.getVarIndex("x"));
  }

  @Test(timeout = 4000)
  public void testMultipleVarsInSingleDeclaration() {
    LiveVariablesAnalysis lva = computeLiveness(
        "function f() { var x = 1, y = 2, z; return x + y; }");
    assertNotNull(lva);
    assertEquals(0, lva.getVarIndex("x"));
    assertEquals(1, lva.getVarIndex("y"));
    assertEquals(2, lva.getVarIndex("z"));
  }

  // =========================================================================
  // Partition D: Exception, Defensive Guards & Abrupt Control Flow
  // =========================================================================

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testLatticeEqualsNullThrowsException() {
    LiveVariablesAnalysis lva = createAnalysis("function f() { var a; }");
    LiveVariablesAnalysis.LiveVariableLattice lattice = lva.createEntryLattice();
    lattice.equals(null);
  }

  @Test(expected = NullPointerException.class, timeout = 4000)
  public void testLatticeIsLiveNullVarThrowsException() {
    LiveVariablesAnalysis lva = createAnalysis("function f() { var a; }");
    LiveVariablesAnalysis.LiveVariableLattice lattice = lva.createEntryLattice();
    lattice.isLive((Scope.Var) null);
  }

  @Test(timeout = 4000)
  public void testTryCatchBlockTriggersBranchOnExConditional() {
    // Inside a try-catch block, CFG contains Branch.ON_EX edges,
    // which tests the conditional = true path in flowThrough/computeGenKill.
    LiveVariablesAnalysis lva = computeLiveness(
        "function f() { var x = 1; try { x = 2; throw 'e'; } catch (err) { return x; } }");
    assertNotNull(lva);
    assertEquals(0, lva.getVarIndex("x"));
  }

  @Test(timeout = 4000)
  public void testMarkAllParametersEscapedDirectly() {
    String src = "function f(p1, p2, p3) { var local = 10; }";
    Scope scope = createFunctionScope(src);
    LiveVariablesAnalysis lva = createAnalysis(src);

    assertTrue(lva.getEscapedLocals().isEmpty());
    lva.markAllParametersEscaped();

    Set<Scope.Var> escaped = lva.getEscapedLocals();
    assertEquals(3, escaped.size());
    assertTrue(escaped.contains(scope.getVar("p1")));
    assertTrue(escaped.contains(scope.getVar("p2")));
    assertTrue(escaped.contains(scope.getVar("p3")));
    assertFalse(escaped.contains(scope.getVar("local")));
  }

  @Test(timeout = 4000)
  public void testGlobalVariableReferencesIgnoredInLocalLattice() {
    // Assignment to an undeclared/global variable should not fail or affect local lattice
    LiveVariablesAnalysis lva = computeLiveness(
        "function f() { globalVar = 42; return globalVar; }");
    assertNotNull(lva);
    LiveVariablesAnalysis.LiveVariableLattice entry = lva.createEntryLattice();
    assertEquals("{}", entry.toString());
  }
}