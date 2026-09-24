/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.ReferenceCollectingCallback
 * Target Defect: Defects4J Closure-120 (InlineVariablesTest::testExternalIssue1053)
 *
 * 1. Defect Specifics:
 *    - In `ReferenceCollection.isAssignedOnceInLifetime()`, when checking whether the variable's
 *      one and only assignment is inside a loop, encountering `block.isFunction` causes an immediate
 *      break returning `true`.
 *    - However, if the variable was declared outside that function (e.g., global scope or outer function),
 *      the function can be invoked multiple times or recursively, meaning the variable is NOT assigned
 *      once in its lifetime.
 *    - Targeted by `testExternalIssue1053_variableAssignedInFunctionScopeIsNotAssignedOnceInLifetime`.
 *
 * 2. Decision Branches Covered:
 *    - `isBlockBoundary`: DO, FOR, TRY, WHILE, WITH (all return true);
 *      AND, HOOK, IF, OR (child == firstChild vs. child != firstChild); CASE statements; null parents.
 *    - `visit`: n.isName() branch; "arguments" variable resolution vs. general var resolution;
 *      varFilter rejection vs. acceptance; blockStack popping on boundary.
 *    - `enterScope` & `exitScope`: blockStack empty vs. non-empty; global scope (updating compiler
 *      global var refs) vs. local scope (wrapping referenceMap in ReferenceMapWrapper).
 *    - `ReferenceCollection.isWellDefined()`: size == 0; init == null; provablyExecutesBefore loop checks.
 *    - `ReferenceCollection.isEscaped()`: single vs. multiple scopes.
 *    - `ReferenceCollection.isAssignedOnceInLifetime()`: 0 assignments, 1 assignment (loop vs. non-loop vs. func), 2+ assignments.
 *    - `ReferenceCollection.isNeverAssigned()`: unassigned vs. assigned.
 *    - `ReferenceCollection.firstReferenceIsAssigningDeclaration()`: size 0, var with value, var without value.
 *    - `ReferenceCollection.getInitializingReference()` and `getInitializingReferenceForConstants()`.
 *    - `Reference`: isDeclaration, isVarDeclaration, isHoistedFunction, isInitializingDeclaration,
 *      getAssignedValue, isSimpleAssignmentToName, isLvalue (INC, DEC, VAR with child, assign ops, LHS of for-in).
 *    - `BasicBlock`: isGlobalScopeBlock, isHoisted, isLoop, isFunction, provablyExecutesBefore (ancestor/descendant,
 *      hoisted interruptions, global block equivalence).
 */

package com.google.javascript.jscomp;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.InputId;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

public class ReferenceCollectingCallbackGeminiTest {

  private ReferenceCollectingCallback analyze(String js) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(
        compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    callback.hotSwapScript(root, null);
    return callback;
  }

  private Var findVar(ReferenceCollectingCallback callback, String name) {
    for (Var v : callback.getAllSymbols()) {
      if (name.equals(v.getName())) {
        return v;
      }
    }
    return null;
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure-120 / Issue 1053)
  // =========================================================================

  /**
   * Targets Defects4J Closure-120 (InlineVariablesTest::testExternalIssue1053).
   * When a global variable is assigned inside a function, that function could be executed
   * multiple times throughout the variable's lifetime. Therefore, `isAssignedOnceInLifetime()`
   * must evaluate to false.
   */
  @Test(timeout = 4000)
  public void testExternalIssue1053_variableAssignedInFunctionScopeIsNotAssignedOnceInLifetime() {
    String js = "var u; function f() { u = Random(); var x = u; f(); alert(x === u); }";
    ReferenceCollectingCallback callback = analyze(js);

    Var uVar = findVar(callback, "u");
    assertNotNull("Variable 'u' should be recorded in symbols", uVar);

    ReferenceCollectingCallback.ReferenceCollection uRefs = callback.getReferences(uVar);
    assertNotNull("ReferenceCollection for 'u' must exist", uRefs);

    // On defective versions, block.isFunction causes an early break, returning true.
    // The correct behavior is that 'u' is NOT assigned once in its lifetime.
    assertFalse("Variable declared in global scope and assigned in an inner function " +
            "must NOT be considered assigned once in lifetime",
        uRefs.isAssignedOnceInLifetime());
  }

  @Test(timeout = 4000)
  public void testVariableAssignedInNestedFunctionScopeIsNotAssignedOnceInLifetime() {
    String js = "function outer() { var u; function inner() { u = 10; } }";
    ReferenceCollectingCallback callback = analyze(js);

    Var uVar = findVar(callback, "u");
    assertNotNull(uVar);
    ReferenceCollectingCallback.ReferenceCollection uRefs = callback.getReferences(uVar);
    assertNotNull(uRefs);

    assertFalse("Variable declared in outer function but assigned in inner function " +
            "must not be considered assigned once in lifetime",
        uRefs.isAssignedOnceInLifetime());
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testLocalVariableAssignedOnceInLifetime() {
    String js = "function f() { var x = 1; return x; }";
    ReferenceCollectingCallback callback = analyze(js);

    Var xVar = findVar(callback, "x");
    assertNotNull(xVar);
    ReferenceCollectingCallback.ReferenceCollection xRefs = callback.getReferences(xVar);
    assertNotNull(xRefs);

    assertTrue("Local variable assigned at declaration should be assigned once in lifetime",
        xRefs.isAssignedOnceInLifetime());
    assertTrue("Local variable initialized at declaration should be well-defined",
        xRefs.isWellDefined());
    assertFalse("Local variable is not escaped", xRefs.isEscaped());
  }

  @Test(timeout = 4000)
  public void testMultipleAssignmentsNotAssignedOnceInLifetime() {
    String js = "var a = 1; a = 2;";
    ReferenceCollectingCallback callback = analyze(js);

    Var aVar = findVar(callback, "a");
    assertNotNull(aVar);
    ReferenceCollectingCallback.ReferenceCollection aRefs = callback.getReferences(aVar);

    assertFalse("Variable with multiple assignments is not assigned once in lifetime",
        aRefs.isAssignedOnceInLifetime());
    assertFalse("Variable is assigned", aRefs.isNeverAssigned());
  }

  @Test(timeout = 4000)
  public void testNeverAssignedVariable() {
    String js = "var a; alert(a);";
    ReferenceCollectingCallback callback = analyze(js);

    Var aVar = findVar(callback, "a");
    assertNotNull(aVar);
    ReferenceCollectingCallback.ReferenceCollection aRefs = callback.getReferences(aVar);

    assertTrue("Uninitialized variable should be reported as never assigned",
        aRefs.isNeverAssigned());
    assertFalse("Never assigned variable is not assigned once in lifetime",
        aRefs.isAssignedOnceInLifetime());
    assertFalse("Uninitialized variable is not well-defined",
        aRefs.isWellDefined());
    assertFalse(aRefs.firstReferenceIsAssigningDeclaration());
  }

  @Test(timeout = 4000)
  public void testAssignmentImmediatelyFollowingDeclarationIsWellDefined() {
    String js = "var a; a = 1; alert(a);";
    ReferenceCollectingCallback callback = analyze(js);

    Var aVar = findVar(callback, "a");
    assertNotNull(aVar);
    ReferenceCollectingCallback.ReferenceCollection aRefs = callback.getReferences(aVar);

    assertTrue("Variable assigned immediately following declaration is well-defined",
        aRefs.isWellDefined());
    assertEquals(aRefs.references.get(1), aRefs.getInitializingReference());
    assertFalse(aRefs.firstReferenceIsAssigningDeclaration());
  }

  @Test(timeout = 4000)
  public void testAssignmentInConditionalBranchIsNotWellDefined() {
    String js = "var a; if (true) { a = 1; } alert(a);";
    ReferenceCollectingCallback callback = analyze(js);

    Var aVar = findVar(callback, "a");
    assertNotNull(aVar);
    ReferenceCollectingCallback.ReferenceCollection aRefs = callback.getReferences(aVar);

    assertFalse("Assignment inside conditional block cannot guarantee well-defined state",
        aRefs.isWellDefined());
  }

  @Test(timeout = 4000)
  public void testVariableEscapedIntoInnerScope() {
    String js = "var a = 1; function f() { alert(a); }";
    ReferenceCollectingCallback callback = analyze(js);

    Var aVar = findVar(callback, "a");
    assertNotNull(aVar);
    ReferenceCollectingCallback.ReferenceCollection aRefs = callback.getReferences(aVar);

    assertTrue("Variable referenced in outer and inner scope must be escaped",
        aRefs.isEscaped());
  }

  @Test(timeout = 4000)
  public void testConstantsAssignedAfterUse() {
    String js = "alert(A); var A = 10;";
    ReferenceCollectingCallback callback = analyze(js);

    Var aVar = findVar(callback, "A");
    assertNotNull(aVar);
    ReferenceCollectingCallback.ReferenceCollection aRefs = callback.getReferences(aVar);

    assertNull("Initializing reference for normal read order is null",
        aRefs.getInitializingReference());
    assertEquals("Constants allow initializing reference after first read",
        aRefs.references.get(1), aRefs.getInitializingReferenceForConstants());
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmptyReferenceCollection() {
    ReferenceCollectingCallback.ReferenceCollection collection =
        new ReferenceCollectingCallback.ReferenceCollection();

    assertEquals(0, collection.references.size());
    assertFalse("Empty collection is not well defined", collection.isWellDefined());
    assertFalse("Empty collection is not escaped", collection.isEscaped());
    assertFalse("Empty collection is not assigned once", collection.isAssignedOnceInLifetime());
    assertTrue("Empty collection is never assigned", collection.isNeverAssigned());
    assertFalse("Empty collection has no first assigning decl",
        collection.firstReferenceIsAssigningDeclaration());
    assertNull("Empty collection initializing reference is null",
        collection.getInitializingReference());
    assertNull("Empty collection constant initializing reference is null",
        collection.getInitializingReferenceForConstants());

    Iterator<ReferenceCollectingCallback.Reference> iterator = collection.iterator();
    assertNotNull(iterator);
    assertFalse(iterator.hasNext());
  }

  @Test(timeout = 4000)
  public void testAssignmentsInsideLoopsAreNotAssignedOnceInLifetime() {
    // WHILE loop
    ReferenceCollectingCallback cbWhile = analyze("var a; while (true) { a = 1; }");
    Var vWhile = findVar(cbWhile, "a");
    assertFalse("Assignment inside WHILE loop is not assigned once in lifetime",
        cbWhile.getReferences(vWhile).isAssignedOnceInLifetime());

    // DO-WHILE loop
    ReferenceCollectingCallback cbDo = analyze("var a; do { a = 1; } while (false);");
    Var vDo = findVar(cbDo, "a");
    assertFalse("Assignment inside DO loop is not assigned once in lifetime",
        cbDo.getReferences(vDo).isAssignedOnceInLifetime());

    // FOR loop
    ReferenceCollectingCallback cbFor = analyze("var a; for (var i = 0; i < 1; i++) { a = 1; }");
    Var vFor = findVar(cbFor, "a");
    assertFalse("Assignment inside FOR loop is not assigned once in lifetime",
        cbFor.getReferences(vFor).isAssignedOnceInLifetime());
  }

  // =========================================================================
  // Partition D: Control Structure Branches & Scope Traversal
  // =========================================================================

  @Test(timeout = 4000)
  public void testBlockBoundariesTraversal() {
    String js =
        "var x = 0;\n" +
            "if (x > 0) { x = 1; } else { x = 2; }\n" +
            "x > 1 ? (x = 3) : (x = 4);\n" +
            "x && (x = 5);\n" +
            "x || (x = 6);\n" +
            "try { x = 7; } catch (e) { x = 8; } finally { x = 9; }\n" +
            "with ({}) { x = 10; }\n" +
            "switch (x) { case 1: x = 11; break; default: x = 12; }\n";

    ReferenceCollectingCallback callback = analyze(js);
    Var xVar = findVar(callback, "x");
    assertNotNull(xVar);
    ReferenceCollectingCallback.ReferenceCollection xRefs = callback.getReferences(xVar);
    assertTrue("Should collect numerous references across diverse basic blocks",
        xRefs.references.size() >= 10);
  }

  @Test(timeout = 4000)
  public void testArgumentsKeywordReference() {
    String js = "function f() { return arguments.length; }";
    ReferenceCollectingCallback callback = analyze(js);

    Var argsVar = findVar(callback, "arguments");
    assertNotNull("arguments variable must be resolved", argsVar);

    ReferenceCollectingCallback.ReferenceCollection refs = callback.getReferences(argsVar);
    assertNotNull(refs);
    assertEquals(1, refs.references.size());
    assertEquals("arguments", refs.references.get(0).getNode().getString());
  }

  @Test(timeout = 4000)
  public void testVarFilterBehavior() {
    Predicate<Var> filterOnlyX = new Predicate<Var>() {
      @Override
      public boolean apply(Var input) {
        return input != null && "x".equals(input.getName());
      }
    };

    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var x = 1; var y = 2;");
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(
        compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR, filterOnlyX);
    callback.hotSwapScript(root, null);

    assertNotNull("Filtered variable 'x' should be tracked", findVar(callback, "x"));
    assertNull("Non-matching variable 'y' should be filtered out", findVar(callback, "y"));
  }

  @Test(timeout = 4000)
  public void testCustomBehaviorInvocationsOnScopeExit() {
    final int[] exitCounts = new int[2]; // [0] = global, [1] = inner

    ReferenceCollectingCallback.Behavior countingBehavior = new ReferenceCollectingCallback.Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, ReferenceCollectingCallback.ReferenceMap referenceMap) {
        if (t.getScope().isGlobal()) {
          exitCounts[0]++;
        } else {
          exitCounts[1]++;
        }
        assertNotNull(referenceMap);
      }
    };

    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var a = 1; function f() { var b = 2; }");
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(compiler, countingBehavior);
    callback.hotSwapScript(root, null);

    assertEquals("Global scope exit must be triggered once", 1, exitCounts[0]);
    assertEquals("Function scope exit must be triggered once", 1, exitCounts[1]);
  }

  @Test(timeout = 4000)
  public void testGetScopeDelegatesToVarScope() {
    ReferenceCollectingCallback callback = analyze("var x = 1;");
    Var xVar = findVar(callback, "x");
    assertNotNull(xVar);
    assertEquals("callback.getScope(var) must return var.scope",
        xVar.scope, callback.getScope(xVar));
  }

  // =========================================================================
  // Partition E: Reference & BasicBlock Low-Level Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testReferenceClassificationMethods() {
    String js =
        "var a = 1;\n" +
            "var b;\n" +
            "function hoisted() {}\n" +
            "try {} catch (err) {}\n" +
            "function g(param) {}\n" +
            "a++;\n" +
            "--b;\n" +
            "b += 5;\n" +
            "b = 10;\n" +
            "for (var prop in {}) {}\n";

    ReferenceCollectingCallback callback = analyze(js);

    // a = 1
    ReferenceCollectingCallback.Reference refA =
        callback.getReferences(findVar(callback, "a")).references.get(0);
    assertTrue(refA.isDeclaration());
    assertTrue(refA.isVarDeclaration());
    assertTrue(refA.isInitializingDeclaration());
    assertTrue(refA.isLvalue());
    assertFalse(refA.isSimpleAssignmentToName());
    assertEquals(1.0, refA.getAssignedValue().getDouble(), 0.0);

    // var b;
    ReferenceCollectingCallback.Reference refBDecl =
        callback.getReferences(findVar(callback, "b")).references.get(0);
    assertTrue(refBDecl.isDeclaration());
    assertTrue(refBDecl.isVarDeclaration());
    assertFalse(refBDecl.isInitializingDeclaration());
    assertFalse(refBDecl.isLvalue());
    assertNull(refBDecl.getAssignedValue());

    // function hoisted() {}
    ReferenceCollectingCallback.Reference refHoisted =
        callback.getReferences(findVar(callback, "hoisted")).references.get(0);
    assertTrue(refHoisted.isDeclaration());
    assertFalse(refHoisted.isVarDeclaration());
    assertTrue(refHoisted.isHoistedFunction());
    assertTrue(refHoisted.isInitializingDeclaration());
    assertTrue(refHoisted.getAssignedValue().isFunction());

    // catch (err) {}
    ReferenceCollectingCallback.Reference refErr =
        callback.getReferences(findVar(callback, "err")).references.get(0);
    assertTrue(refErr.isDeclaration());
    assertFalse(refErr.isVarDeclaration());
    assertTrue(refErr.isInitializingDeclaration());

    // function g(param) {}
    ReferenceCollectingCallback.Reference refParam =
        callback.getReferences(findVar(callback, "param")).references.get(0);
    assertTrue(refParam.isDeclaration());
    assertFalse(refParam.isVarDeclaration());
    assertTrue(refParam.isInitializingDeclaration());

    // a++
    ReferenceCollectingCallback.Reference refAInc =
        callback.getReferences(findVar(callback, "a")).references.get(1);
    assertTrue(refAInc.isLvalue());

    // --b
    ReferenceCollectingCallback.Reference refBDec =
        callback.getReferences(findVar(callback, "b")).references.get(1);
    assertTrue(refBDec.isLvalue());

    // b += 5
    ReferenceCollectingCallback.Reference refBAssignOp =
        callback.getReferences(findVar(callback, "b")).references.get(2);
    assertTrue(refBAssignOp.isLvalue());
    assertFalse(refBAssignOp.isSimpleAssignmentToName());

    // b = 10
    ReferenceCollectingCallback.Reference refBSimpleAssign =
        callback.getReferences(findVar(callback, "b")).references.get(3);
    assertTrue(refBSimpleAssign.isLvalue());
    assertTrue(refBSimpleAssign.isSimpleAssignmentToName());

    // for (var prop in {})
    ReferenceCollectingCallback.Reference refProp =
        callback.getReferences(findVar(callback, "prop")).references.get(0);
    assertTrue(refProp.isLvalue());
  }

  @Test(timeout = 4000)
  public void testReferenceBleedingFunctionAndCloneWithNewScope() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var x = function bleeding() {};");
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(
        compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    callback.hotSwapScript(root, null);

    Var xVar = findVar(callback, "x");
    assertNotNull(xVar);
    ReferenceCollectingCallback.Reference refX =
        callback.getReferences(xVar).references.get(0);

    // cloneWithNewScope
    Scope dummyScope = xVar.getScope();
    ReferenceCollectingCallback.Reference cloned = refX.cloneWithNewScope(dummyScope);
    assertEquals(refX.getNode(), cloned.getNode());
    assertEquals(dummyScope, cloned.getScope());
    assertEquals(refX.getBasicBlock(), cloned.getBasicBlock());
    assertEquals(refX.getInputId(), cloned.getInputId());

    // createRefForTest helper
    CompilerInput testInput = new CompilerInput(SourceFile.fromCode("sample.js", ""));
    ReferenceCollectingCallback.Reference testRef =
        ReferenceCollectingCallback.Reference.createRefForTest(testInput);
    assertEquals(new InputId("sample.js"), testRef.getInputId());
    assertTrue(testRef.getNode().isName());
  }

  @Test(timeout = 4000)
  public void testBasicBlockProvablyExecutesBeforeLogic() {
    Node script = new Node(Token.SCRIPT);
    Node block1 = new Node(Token.BLOCK);
    Node block2 = new Node(Token.BLOCK);

    ReferenceCollectingCallback.BasicBlock globalBlockA =
        new ReferenceCollectingCallback.BasicBlock(null, script);
    ReferenceCollectingCallback.BasicBlock globalBlockB =
        new ReferenceCollectingCallback.BasicBlock(null, script);

    assertTrue("Global blocks provably execute before one another",
        globalBlockA.provablyExecutesBefore(globalBlockB));
    assertTrue(globalBlockA.isGlobalScopeBlock());

    ReferenceCollectingCallback.BasicBlock childBlock =
        new ReferenceCollectingCallback.BasicBlock(globalBlockA, block1);
    assertFalse(childBlock.isGlobalScopeBlock());
    assertEquals(globalBlockA, childBlock.getParent());

    assertTrue("Parent block executes before descendant block",
        globalBlockA.provablyExecutesBefore(childBlock));
    assertFalse("Descendant block cannot execute before ancestor block",
        childBlock.provablyExecutesBefore(globalBlockA));

    // Hoisted function barrier
    Node hoistedFunction = new Node(Token.FUNCTION,
        Node.newString(Token.NAME, "hoistedFunc"),
        new Node(Token.PARAM_LIST),
        new Node(Token.BLOCK));
    new Node(Token.SCRIPT, hoistedFunction); // attach parent so it qualifies as hoisted

    ReferenceCollectingCallback.BasicBlock hoistedBlock =
        new ReferenceCollectingCallback.BasicBlock(globalBlockA, hoistedFunction);
    ReferenceCollectingCallback.BasicBlock descendantOfHoisted =
        new ReferenceCollectingCallback.BasicBlock(hoistedBlock, block2);

    assertFalse("Hoisted block in ancestry line invalidates provablyExecutesBefore",
        globalBlockA.provablyExecutesBefore(descendantOfHoisted));
  }
}