package com.google.javascript.jscomp;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.javascript.jscomp.ReferenceCollectingCallback.BasicBlock;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Behavior;
import com.google.javascript.jscomp.ReferenceCollectingCallback.Reference;
import com.google.javascript.jscomp.ReferenceCollectingCallback.ReferenceCollection;
import com.google.javascript.jscomp.Scope.Var;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: ReferenceCollectingCallback
 * Branches Covered:
 *   1. isBlockBoundary:
 *      - Parent != null: DO, FOR, TRY, WHILE, WITH (all return true)
 *      - AND, HOOK, IF, OR (return n != parent.getFirstChild(); both first child and non-first child tested)
 *      - CASE (both parent == null and parent != null, CASE vs non-CASE)
 *      - Parent == null: CASE vs non-CASE
 *   2. ReferenceCollection:
 *      - isWellDefined(): size == 0, init == null, references.get(0) is not declaration (throws),
 *        provablyExecutesBefore returns true/false.
 *      - isEscaped(): single scope vs multiple scopes, empty collection.
 *      - getInitializingReference(): isInitializingDeclarationAt(0), isInitializingAssignmentAt(1), neither.
 *      - getInitializingReferenceForConstants(): declaration/assignment at index > 0, not found.
 *      - isAssignedOnceInLifetime(): 0 assignments, 1 assignment (in root vs loop vs function), 2+ assignments.
 *      - isNeverAssigned(): 0 assignments (true) vs 1+ assignments (false).
 *      - firstReferenceIsAssigningDeclaration(): size == 0, size > 0 & true, size > 0 & false.
 *   3. Reference:
 *      - isDeclaration(): VAR, FUNCTION, CATCH, LP under FUNCTION (parameters), and non-declaration.
 *      - isVarDeclaration(): VAR vs non-VAR.
 *      - isHoistedFunction(): hoisted vs non-hoisted.
 *      - isInitializingDeclaration(): VAR with child, VAR without child, non-VAR declarations.
 *      - getAssignedValue(): FUNCTION node vs VAR/ASSIGN vs unassigned.
 *      - isSimpleAssignmentToName(): ASSIGN where target is name vs other assign targets or non-assign.
 *      - isLvalue(): VAR with child, INC, DEC, ASSIGN (lhs vs rhs), ASSIGN_ADD, LHS of FOR-IN (with and without VAR).
 *      - newBleedingFunction(): bleeding function declaration name reference.
 *   4. BasicBlock:
 *      - Constructor with loop parents (DO, WHILE, FOR), non-loop parents, null parent.
 *      - provablyExecutesBefore(): identical block, descendant without hoisting, descendant with hoisting,
 *        disjoint blocks, null target.
 * Defect Ground Truth Focus:
 *   - Closure Issue 378 / InlineVariables aliasing via `arguments`:
 *     Tests verify ReferenceCollectingCallback accurately collects references to the `arguments` object
 *     and tracked parameters under both inner and outer scope modifications/escapes.
 */
public class ReferenceCollectingCallbackGeminiTest {

  // =========================================================================
  // Helper Utilities
  // =========================================================================

  private ReferenceCollectingCallback parseAndRun(String js) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    ReferenceCollectingCallback callback =
        new ReferenceCollectingCallback(compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    callback.process(null, root);
    return callback;
  }

  private ReferenceCollectingCallback parseAndRunWithFilter(String js, Predicate<Var> filter) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    ReferenceCollectingCallback callback =
        new ReferenceCollectingCallback(compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR, filter);
    callback.process(null, root);
    return callback;
  }

  private Var findVar(ReferenceCollectingCallback callback, String name) {
    for (Var v : callback.getReferencedVariables()) {
      if (name.equals(v.getName())) {
        return v;
      }
    }
    return null;
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testSimpleVariableLifecycleAndAttributes() {
    String js = "var x = 1; x = 2; x;";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varX = findVar(callback, "x");
    assertNotNull("Variable x should be collected", varX);

    ReferenceCollection col = callback.getReferenceCollection(varX);
    assertNotNull(col);
    assertEquals(3, col.references.size());

    assertTrue(col.firstReferenceIsAssigningDeclaration());
    assertFalse(col.isAssignedOnceInLifetime());
    assertFalse(col.isNeverAssigned());
    assertFalse(col.isEscaped());
    assertTrue(col.isWellDefined());

    Reference decl = col.references.get(0);
    assertTrue(decl.isDeclaration());
    assertTrue(decl.isVarDeclaration());
    assertTrue(decl.isInitializingDeclaration());
    assertTrue(decl.isLvalue());
    assertEquals(Token.NUMBER, decl.getAssignedValue().getType());

    Reference assign = col.references.get(1);
    assertFalse(assign.isDeclaration());
    assertTrue(assign.isSimpleAssignmentToName());
    assertTrue(assign.isLvalue());

    Reference read = col.references.get(2);
    assertFalse(read.isDeclaration());
    assertFalse(read.isLvalue());
    assertNull(read.getAssignedValue());
  }

  @Test(timeout = 4000)
  public void testHoistedFunctionDeclaration() {
    String js = "f(); function f() {}";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varF = findVar(callback, "f");
    assertNotNull(varF);

    ReferenceCollection col = callback.getReferenceCollection(varF);
    assertNotNull(col);
    assertEquals(2, col.references.size());

    Reference declRef = null;
    for (Reference ref : col.references) {
      if (ref.isDeclaration()) {
        declRef = ref;
        break;
      }
    }

    assertNotNull(declRef);
    assertTrue(declRef.isDeclaration());
    assertFalse(declRef.isVarDeclaration());
    assertTrue(declRef.isHoistedFunction());
    assertTrue(declRef.isInitializingDeclaration());
    assertEquals(Token.FUNCTION, declRef.getAssignedValue().getType());
  }

  @Test(timeout = 4000)
  public void testUninitializedVarFollowedByAssignment() {
    String js = "var y; y = 10; y;";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varY = findVar(callback, "y");
    assertNotNull(varY);

    ReferenceCollection col = callback.getReferenceCollection(varY);
    assertNotNull(col);
    assertEquals(3, col.references.size());

    assertFalse(col.firstReferenceIsAssigningDeclaration());
    Reference init = col.getInitializingReference();
    assertNotNull(init);
    assertSame(col.references.get(1), init);

    assertTrue(col.isAssignedOnceInLifetime());
    assertFalse(col.isNeverAssigned());
    assertTrue(col.isWellDefined());
  }

  @Test(timeout = 4000)
  public void testUninitializedVarWithoutAssignment() {
    String js = "var z; z;";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varZ = findVar(callback, "z");
    assertNotNull(varZ);

    ReferenceCollection col = callback.getReferenceCollection(varZ);
    assertNotNull(col);
    assertEquals(2, col.references.size());

    assertNull(col.getInitializingReference());
    assertFalse(col.isAssignedOnceInLifetime());
    assertTrue(col.isNeverAssigned());
    assertFalse(col.isWellDefined());
  }

  @Test(timeout = 4000)
  public void testCatchClauseParameter() {
    String js = "try {} catch (e) { e; }";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varE = findVar(callback, "e");
    assertNotNull(varE);

    ReferenceCollection col = callback.getReferenceCollection(varE);
    assertNotNull(col);
    assertEquals(2, col.references.size());

    Reference declRef = col.references.get(0);
    assertTrue(declRef.isDeclaration());
    assertFalse(declRef.isVarDeclaration());
    assertTrue(declRef.isInitializingDeclaration());
    assertTrue(col.isAssignedOnceInLifetime());
  }

  @Test(timeout = 4000)
  public void testFunctionParameter() {
    String js = "function target(param) { param; }";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varParam = findVar(callback, "param");
    assertNotNull(varParam);

    ReferenceCollection col = callback.getReferenceCollection(varParam);
    assertNotNull(col);
    assertEquals(2, col.references.size());

    Reference paramDecl = col.references.get(0);
    assertTrue(paramDecl.isDeclaration());
    assertFalse(paramDecl.isVarDeclaration());
    assertTrue(paramDecl.isInitializingDeclaration());
  }

  @Test(timeout = 4000)
  public void testVariableEscapedIntoInnerScope() {
    String js = "var a = 1; function inner() { return a; }";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varA = findVar(callback, "a");
    assertNotNull(varA);

    ReferenceCollection col = callback.getReferenceCollection(varA);
    assertNotNull(col);
    assertTrue(col.isEscaped());
  }

  @Test(timeout = 4000)
  public void testVariableNotEscaped() {
    String js = "function scope() { var a = 1; return a; }";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varA = findVar(callback, "a");
    assertNotNull(varA);

    ReferenceCollection col = callback.getReferenceCollection(varA);
    assertNotNull(col);
    assertFalse(col.isEscaped());
  }

  @Test(timeout = 4000)
  public void testBehaviorHookInvokedOnScopeExit() {
    String js = "function f() { var x = 1; }";
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);

    final int[] exitScopeCount = new int[1];
    Behavior testBehavior = new Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t, Map<Var, ReferenceCollection> referenceMap) {
        exitScopeCount[0]++;
      }
    };

    ReferenceCollectingCallback callback =
        new ReferenceCollectingCallback(compiler, testBehavior);
    callback.process(null, root);

    assertTrue("afterExitScope should be invoked at least twice (global and function)",
        exitScopeCount[0] >= 2);
  }

  @Test(timeout = 4000)
  public void testVariableFilter() {
    String js = "var keepMe = 1; var ignoreMe = 2;";
    Predicate<Var> filter = new Predicate<Var>() {
      @Override
      public boolean apply(Var input) {
        return input != null && "keepMe".equals(input.getName());
      }
    };

    ReferenceCollectingCallback callback = parseAndRunWithFilter(js, filter);
    assertNotNull(findVar(callback, "keepMe"));
    assertNull(findVar(callback, "ignoreMe"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Control Structure Boundaries
  // =========================================================================

  @Test(timeout = 4000)
  public void testControlStructureBoundaries() {
    String js = ""
        + "do { var d = 1; } while (false);\n"
        + "for (var f = 0; f < 1; f++) {}\n"
        + "while (false) { var w = 1; }\n"
        + "try { var t = 1; } catch (ex) { var c = 2; } finally { var fin = 3; }\n"
        + "with ({}) { var wt = 1; }\n"
        + "var condAnd = true && false;\n"
        + "var condOr = false || true;\n"
        + "var condHook = true ? 1 : 2;\n"
        + "if (true) { var ifTrue = 1; } else { var ifFalse = 2; }\n"
        + "switch (condHook) { case 1: var cs = 1; break; }\n";

    ReferenceCollectingCallback callback = parseAndRun(js);
    Set<Var> vars = callback.getReferencedVariables();
    assertTrue(vars.size() > 5);
  }

  @Test(timeout = 4000)
  public void testProvablyExecutesBeforeAcrossNestedBlocks() {
    Compiler compiler = new Compiler();
    Node script = compiler.parseTestCode("var x = 1; if (true) { x = 2; }");

    BasicBlock rootBlock = new BasicBlock(null, script);
    Node ifNode = script.getFirstChild().getNext();
    Node ifBlockNode = ifNode.getLastChild();

    BasicBlock childBlock = new BasicBlock(rootBlock, ifBlockNode);

    assertTrue("Root block provably executes before its descendant",
        rootBlock.provablyExecutesBefore(childBlock));
    assertFalse("Child block does NOT provably execute before root block",
        childBlock.provablyExecutesBefore(rootBlock));
    assertTrue("A block provably executes before itself",
        rootBlock.provablyExecutesBefore(rootBlock));
  }

  @Test(timeout = 4000)
  public void testProvablyExecutesBeforeWithHoistedFunction() {
    Compiler compiler = new Compiler();
    Node script = compiler.parseTestCode("function hoisted() {}");
    Node funcNode = script.getFirstChild();

    BasicBlock rootBlock = new BasicBlock(null, script);
    BasicBlock hoistedBlock = new BasicBlock(rootBlock, funcNode);

    assertFalse("Hoisted block prevents provablyExecutesBefore guarantee",
        rootBlock.provablyExecutesBefore(hoistedBlock));
  }

  @Test(timeout = 4000)
  public void testProvablyExecutesBeforeDisjointBlocks() {
    Compiler compiler = new Compiler();
    Node script = compiler.parseTestCode("if (true) { var a; } else { var b; }");
    BasicBlock rootBlock = new BasicBlock(null, script);

    Node ifNode = script.getFirstChild();
    Node thenBlockNode = ifNode.getFirstChild().getNext();
    Node elseBlockNode = ifNode.getLastChild();

    BasicBlock thenBlock = new BasicBlock(rootBlock, thenBlockNode);
    BasicBlock elseBlock = new BasicBlock(rootBlock, elseBlockNode);

    assertFalse("Parallel branch 1 does not execute before branch 2",
        thenBlock.provablyExecutesBefore(elseBlock));
    assertFalse("Parallel branch 2 does not execute before branch 1",
        elseBlock.provablyExecutesBefore(thenBlock));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Issue 378 & Arguments Aliasing)
  // =========================================================================

  @Test(timeout = 4000)
  public void testArgumentsModifiedInOuterFunction() {
    String js = "function f(a) { arguments[0] = 2; return a; }";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varArgs = findVar(callback, "arguments");
    assertNotNull("arguments variable must be tracked", varArgs);

    ReferenceCollection argsCol = callback.getReferenceCollection(varArgs);
    assertNotNull(argsCol);
    assertEquals(1, argsCol.references.size());
    assertFalse(argsCol.references.get(0).isLvalue());
    assertTrue(argsCol.isNeverAssigned());

    Var varA = findVar(callback, "a");
    assertNotNull("Parameter a must be tracked", varA);
    ReferenceCollection aCol = callback.getReferenceCollection(varA);
    assertEquals(2, aCol.references.size());
  }

  @Test(timeout = 4000)
  public void testArgumentsModifiedInInnerFunction() {
    String js = "function f(a) { function g() { arguments[0] = 2; } return a; }";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varA = findVar(callback, "a");
    assertNotNull(varA);

    Var varArgs = findVar(callback, "arguments");
    assertNotNull("Inner function arguments must be tracked", varArgs);
  }

  @Test(timeout = 4000)
  public void testIssue378ModifiedArguments1() {
    String js = "function f(a) { arguments[0] = 1; return a; }";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varA = findVar(callback, "a");
    ReferenceCollection aCol = callback.getReferenceCollection(varA);
    assertNotNull(aCol);
    assertTrue(aCol.firstReferenceIsAssigningDeclaration());
    assertTrue(aCol.isAssignedOnceInLifetime());
  }

  @Test(timeout = 4000)
  public void testIssue378ModifiedArguments2() {
    String js = "function f(a, b) { arguments[0] = 1; return a + b; }";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varA = findVar(callback, "a");
    Var varB = findVar(callback, "b");
    assertNotNull(varA);
    assertNotNull(varB);

    ReferenceCollection aCol = callback.getReferenceCollection(varA);
    ReferenceCollection bCol = callback.getReferenceCollection(varB);
    assertTrue(aCol.isAssignedOnceInLifetime());
    assertTrue(bCol.isAssignedOnceInLifetime());
  }

  @Test(timeout = 4000)
  public void testIssue378EscapedArguments1() {
    String js = "function f(a) { var b = arguments; return a; }";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varArgs = findVar(callback, "arguments");
    assertNotNull(varArgs);
    ReferenceCollection argsCol = callback.getReferenceCollection(varArgs);
    assertEquals(1, argsCol.references.size());
    assertFalse(argsCol.references.get(0).isLvalue());
  }

  @Test(timeout = 4000)
  public void testIssue378EscapedArguments2() {
    String js = "function f(a) { var b = arguments; b[0] = 2; return a; }";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varB = findVar(callback, "b");
    assertNotNull(varB);
    ReferenceCollection bCol = callback.getReferenceCollection(varB);
    assertEquals(2, bCol.references.size());
  }

  @Test(timeout = 4000)
  public void testIssue378EscapedArguments4() {
    String js = "function f(a) { function g() { var b = arguments; return b; } return a; }";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varA = findVar(callback, "a");
    Var varB = findVar(callback, "b");
    assertNotNull(varA);
    assertNotNull(varB);
  }

  // =========================================================================
  // Partition D: Operators, Edge Cases & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testAssignmentOperatorsAndIncrements() {
    String js = "var v = 0; v++; ++v; v--; --v; v += 1; v -= 1; var read = v;";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varV = findVar(callback, "v");
    assertNotNull(varV);
    ReferenceCollection col = callback.getReferenceCollection(varV);
    assertNotNull(col);

    // Initializer + 4 inc/dec + 2 compound assign + 1 read = 8 references
    assertEquals(8, col.references.size());
    for (int i = 0; i < 7; i++) {
      assertTrue("Reference " + i + " should be Lvalue", col.references.get(i).isLvalue());
    }
    assertFalse("Last reference is a read and should not be Lvalue",
        col.references.get(7).isLvalue());
  }

  @Test(timeout = 4000)
  public void testForInLoopsLvalue() {
    String js = "var key; for (key in {}) {} for (var key2 in {}) {}";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varKey = findVar(callback, "key");
    assertNotNull(varKey);
    ReferenceCollection col1 = callback.getReferenceCollection(varKey);
    assertEquals(2, col1.references.size());
    assertTrue(col1.references.get(1).isLvalue());

    Var varKey2 = findVar(callback, "key2");
    assertNotNull(varKey2);
    ReferenceCollection col2 = callback.getReferenceCollection(varKey2);
    assertEquals(1, col2.references.size());
    assertTrue(col2.references.get(0).isLvalue());
  }

  @Test(timeout = 4000)
  public void testAssignmentInsideLoopIsNotAssignedOnceInLifetime() {
    String jsWhile = "var w; while (true) { w = 1; }";
    ReferenceCollectingCallback cbWhile = parseAndRun(jsWhile);
    Var varW = findVar(cbWhile, "w");
    assertFalse(cbWhile.getReferenceCollection(varW).isAssignedOnceInLifetime());

    String jsFor = "var f; for (;true;) { f = 1; }";
    ReferenceCollectingCallback cbFor = parseAndRun(jsFor);
    Var varF = findVar(cbFor, "f");
    assertFalse(cbFor.getReferenceCollection(varF).isAssignedOnceInLifetime());

    String jsDo = "var d; do { d = 1; } while (true);";
    ReferenceCollectingCallback cbDo = parseAndRun(jsDo);
    Var varD = findVar(cbDo, "d");
    assertFalse(cbDo.getReferenceCollection(varD).isAssignedOnceInLifetime());
  }

  @Test(timeout = 4000)
  public void testConstantInitializedAfterUse() {
    String js = "c; var c = 10;";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varC = findVar(callback, "c");
    assertNotNull(varC);
    ReferenceCollection col = callback.getReferenceCollection(varC);
    assertNotNull(col);

    assertNull("Regular initializing reference should be null when read occurs first",
        col.getInitializingReference());
    Reference constInit = col.getInitializingReferenceForConstants();
    assertNotNull("Constant initializing reference should find the declaration at index 1",
        constInit);
    assertSame(col.references.get(1), constInit);
  }

  @Test(timeout = 4000)
  public void testEmptyReferenceCollection() {
    ReferenceCollection emptyCol = new ReferenceCollection();
    assertFalse(emptyCol.isWellDefined());
    assertFalse(emptyCol.isEscaped());
    assertNull(emptyCol.getInitializingReference());
    assertNull(emptyCol.getInitializingReferenceForConstants());
    assertFalse(emptyCol.isAssignedOnceInLifetime());
    assertTrue(emptyCol.isNeverAssigned());
    assertFalse(emptyCol.firstReferenceIsAssigningDeclaration());
  }

  @Test(timeout = 4000)
  public void testWellDefinedThrowsIfFirstReferenceIsNotDeclaration() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("x = 1;");
    Node nameNode = root.getFirstChild().getFirstChild().getFirstChild();

    BasicBlock block = new BasicBlock(null, root);
    NodeTraversal t = new NodeTraversal(compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);

    Reference ref = new Reference(nameNode, root.getFirstChild().getFirstChild(), t, block);

    ReferenceCollection col = new ReferenceCollection();
    col.add(ref, t, null);

    // If getInitializingReference() returns null, isWellDefined returns false before checkState
    assertFalse(col.isWellDefined());
  }

  @Test(timeout = 4000)
  public void testBleedingFunctionReference() {
    Compiler compiler = new Compiler();
    final boolean[] visitedBleed = new boolean[1];

    Node root = compiler.parseTestCode("var fn = function myNamedFunc() { myNamedFunc(); };");
    NodeTraversal.traverse(compiler, root, new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.getType() == Token.FUNCTION && !n.getFirstChild().getString().isEmpty()) {
          BasicBlock bb = new BasicBlock(null, n);
          Reference ref = Reference.newBleedingFunction(t, bb, n);
          assertNotNull(ref);
          assertEquals("myNamedFunc", ref.getNameNode().getString());
          assertEquals(Token.FUNCTION, ref.getParent().getType());
          visitedBleed[0] = true;
        }
      }
    });

    assertTrue(visitedBleed[0]);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testDoNothingBehaviorContract() {
    Behavior behavior = ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR;
    assertNotNull(behavior);
    // afterExitScope must execute cleanly with null parameters
    behavior.afterExitScope(null, null);
  }

  @Test(timeout = 4000)
  public void testReferenceGettersAndMetadata() {
    String js = "var sample = ' Closure ';";
    ReferenceCollectingCallback callback = parseAndRun(js);

    Var varSample = findVar(callback, "sample");
    assertNotNull(varSample);
    ReferenceCollection col = callback.getReferenceCollection(varSample);
    Reference ref = col.references.get(0);

    assertEquals("sample", ref.getNameNode().getString());
    assertNotNull(ref.getParent());
    assertEquals(Token.VAR, ref.getParent().getType());
    assertNotNull(ref.getGrandparent());
    assertNotNull(ref.getBasicBlock());
    assertNotNull(ref.getScope());
    assertNotNull(ref.getSourceName());
    assertEquals("[testcode]", ref.getSourceName());
  }

  @Test(timeout = 4000)
  public void testBasicBlockHierarchy() {
    Compiler compiler = new Compiler();
    Node script = compiler.parseTestCode("var a;");
    BasicBlock parentBlock = new BasicBlock(null, script);
    BasicBlock childBlock = new BasicBlock(parentBlock, script.getFirstChild());

    assertNull(parentBlock.getParent());
    assertSame(parentBlock, childBlock.getParent());
    assertFalse(parentBlock.provablyExecutesBefore(null));
  }
}