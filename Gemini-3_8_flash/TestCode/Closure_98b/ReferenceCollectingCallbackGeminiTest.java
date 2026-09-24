package com.google.javascript.jscomp;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Maps;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.ReferenceCollectingCallback
 *
 * Branches & Conditions Tested:
 * 1. isBlockBoundary(Node, Node):
 *    - parent == null -> return n.getType() == Token.CASE
 *    - parent != null: Token.DO, Token.FOR, Token.TRY, Token.WHILE, Token.WITH -> true
 *    - Token.AND, Token.HOOK, Token.IF, Token.OR -> n != parent.getFirstChild() (true and false paths)
 *    - default -> return n.getType() == Token.CASE
 * 2. ReferenceCollection.isWellDefined():
 *    - references.size() == 0 -> false
 *    - init == null -> false
 *    - provablyExecutesBefore loop (both true and false outcomes)
 * 3. ReferenceCollection.isEscaped():
 *    - 0 references -> false
 *    - single scope -> false
 *    - multiple scopes -> true
 * 4. ReferenceCollection.getInitializingReference():
 *    - isInitializingDeclarationAt(0) == true
 *    - isInitializingAssignmentAt(1) == true
 *    - neither -> null
 * 5. ReferenceCollection.getInitializingReferenceForConstants():
 *    - declaration at i, assignment at i, or none
 * 6. ReferenceCollection.isAssignedOnceInLifetime():
 *    - getOneAndOnlyAssignment() == null -> false
 *    - assignment inside loop vs not in loop (DEFECT-TARGETED)
 * 7. ReferenceCollection.isNeverAssigned():
 *    - never assigned -> true
 *    - assigned via VAR init, INC, DEC, ASSIGN, FOR-IN -> false
 * 8. ReferenceCollection.firstReferenceIsAssigningDeclaration():
 *    - size == 0 -> false
 *    - init declaration at 0 -> true
 *    - uninitialized declaration at 0 -> false
 * 9. BasicBlock.provablyExecutesBefore(BasicBlock):
 *    - thatBlock == null -> false
 *    - thatBlock == this -> true
 *    - thatBlock descendant with hoisted block in chain -> false
 *    - thatBlock descendant without hoisted block -> true
 *    - thatBlock unrelated / ancestor -> false
 * 10. Reference.isDeclaration(), isVarDeclaration(), isInitializingDeclaration(),
 *     isSimpleAssignmentToName(), isLvalue(), getAssignedValue(), newBleedingFunction().
 *
 * Known Defect (Defects4J):
 * InlineVariablesTest::testNoInlineAliasesInLoop
 * -> ReferenceCollection.isAssignedOnceInLifetime() misses verification that the
 *    assignment reference is not inside a loop (BasicBlock.isLoop check is omitted),
 *    erroneously returning true for variables assigned inside loops.
 */
public class ReferenceCollectingCallbackGeminiTest {

  private static class CollectResult {
    final Map<String, ReferenceCollectingCallback.ReferenceCollection> collections = Maps.newHashMap();
    final Map<String, Scope.Var> vars = Maps.newHashMap();
    ReferenceCollectingCallback callback;
  }

  private CollectResult collect(String js) {
    return collect(js, Predicates.<Scope.Var>alwaysTrue());
  }

  private CollectResult collect(String js, Predicate<Scope.Var> filter) {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode(js);
    final CollectResult result = new CollectResult();
    ReferenceCollectingCallback.Behavior behavior = new ReferenceCollectingCallback.Behavior() {
      @Override
      public void afterExitScope(NodeTraversal t,
          Map<Scope.Var, ReferenceCollectingCallback.ReferenceCollection> map) {
        for (Map.Entry<Scope.Var, ReferenceCollectingCallback.ReferenceCollection> entry : map.entrySet()) {
          result.collections.put(entry.getKey().getName(), entry.getValue());
          result.vars.put(entry.getKey().getName(), entry.getKey());
        }
      }
    };
    result.callback = new ReferenceCollectingCallback(compiler, behavior, filter);
    result.callback.process(null, root);
    return result;
  }

  private static boolean invokeIsBlockBoundary(Node n, Node parent) throws Exception {
    Method m = ReferenceCollectingCallback.class.getDeclaredMethod("isBlockBoundary", Node.class, Node.class);
    m.setAccessible(true);
    return (Boolean) m.invoke(null, n, parent);
  }

  /* =========================================================================
   * Partition A: Core Functional Logic & State Transitions
   * ========================================================================= */

  @Test(timeout = 4000)
  public void testCoreTraversalAndCollection() {
    String js = "var a = 1; var b = a + 2;";
    CollectResult res = collect(js);

    ReferenceCollectingCallback.ReferenceCollection aColl = res.collections.get("a");
    assertNotNull(aColl);
    assertEquals(2, aColl.references.size());
    assertTrue(aColl.firstReferenceIsAssigningDeclaration());
    assertTrue(aColl.isWellDefined());
    assertFalse(aColl.isNeverAssigned());
    assertFalse(aColl.isEscaped());

    ReferenceCollectingCallback.Reference ref0 = aColl.references.get(0);
    assertTrue(ref0.isDeclaration());
    assertTrue(ref0.isVarDeclaration());
    assertTrue(ref0.isInitializingDeclaration());
    assertTrue(ref0.isLvalue());
    assertNotNull(ref0.getAssignedValue());
    assertEquals(Token.NUMBER, ref0.getAssignedValue().getType());
    assertNotNull(ref0.getSourceName());

    ReferenceCollectingCallback.Reference ref1 = aColl.references.get(1);
    assertFalse(ref1.isDeclaration());
    assertFalse(ref1.isLvalue());
    assertNull(ref1.getAssignedValue());
    assertEquals("a", ref1.getNameNode().getString());
  }

  @Test(timeout = 4000)
  public void testGetReferenceCollectionViaCallback() {
    String js = "var x = 10;";
    CollectResult res = collect(js);
    Scope.Var varX = res.vars.get("x");
    assertNotNull(varX);

    ReferenceCollectingCallback.ReferenceCollection collection =
        res.callback.getReferenceCollection(varX);
    assertNotNull(collection);
    assertSame(res.collections.get("x"), collection);
  }

  @Test(timeout = 4000)
  public void testDoNothingBehaviorExecution() {
    Compiler compiler = new Compiler();
    Node root = compiler.parseTestCode("var k = 5;");
    ReferenceCollectingCallback callback = new ReferenceCollectingCallback(
        compiler, ReferenceCollectingCallback.DO_NOTHING_BEHAVIOR);
    callback.process(null, root);
    assertNotNull(callback);
  }

  @Test(timeout = 4000)
  public void testFilterRestrictsCollectedVariables() {
    String js = "var includeVar = 1; var excludeVar = 2;";
    CollectResult res = collect(js, new Predicate<Scope.Var>() {
      @Override
      public boolean apply(Scope.Var input) {
        return "includeVar".equals(input.getName());
      }
    });

    assertTrue(res.collections.containsKey("includeVar"));
    assertFalse(res.collections.containsKey("excludeVar"));
  }

  @Test(timeout = 4000)
  public void testEscapedVariableIntoInnerScope() {
    String js = "var outer = 1; function inner() { return outer; }";
    CollectResult res = collect(js);

    ReferenceCollectingCallback.ReferenceCollection coll = res.collections.get("outer");
    assertNotNull(coll);
    assertTrue(coll.isEscaped());
  }

  @Test(timeout = 4000)
  public void testFunctionParametersAndCatchDeclarations() {
    String js = "function f(param) { try {} catch (err) { var localVar; } }";
    CollectResult res = collect(js);

    ReferenceCollectingCallback.ReferenceCollection paramColl = res.collections.get("param");
    assertNotNull(paramColl);
    ReferenceCollectingCallback.Reference paramRef = paramColl.references.get(0);
    assertTrue(paramRef.isDeclaration());
    assertFalse(paramRef.isVarDeclaration());
    assertTrue(paramRef.isInitializingDeclaration());

    ReferenceCollectingCallback.ReferenceCollection errColl = res.collections.get("err");
    assertNotNull(errColl);
    ReferenceCollectingCallback.Reference errRef = errColl.references.get(0);
    assertTrue(errRef.isDeclaration());
    assertFalse(errRef.isVarDeclaration());
    assertTrue(errRef.isInitializingDeclaration());

    ReferenceCollectingCallback.ReferenceCollection localColl = res.collections.get("localVar");
    assertNotNull(localColl);
    ReferenceCollectingCallback.Reference localRef = localColl.references.get(0);
    assertTrue(localRef.isDeclaration());
    assertTrue(localRef.isVarDeclaration());
    assertFalse(localRef.isInitializingDeclaration());
    assertNull(localRef.getAssignedValue());
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationAssignedValue() {
    String js = "function namedFunc() {}";
    CollectResult res = collect(js);

    ReferenceCollectingCallback.ReferenceCollection coll = res.collections.get("namedFunc");
    assertNotNull(coll);
    ReferenceCollectingCallback.Reference ref = coll.references.get(0);
    assertTrue(ref.isDeclaration());
    assertTrue(ref.isHoistedFunction());
    assertEquals(Token.FUNCTION, ref.getAssignedValue().getType());
  }

  @Test(timeout = 4000)
  public void testSeparateInitializingAssignment() {
    String js = "var sep; sep = 42; var read = sep;";
    CollectResult res = collect(js);

    ReferenceCollectingCallback.ReferenceCollection coll = res.collections.get("sep");
    assertNotNull(coll);
    assertEquals(3, coll.references.size());

    ReferenceCollectingCallback.Reference initRef = coll.getInitializingReference();
    assertNotNull(initRef);
    assertSame(coll.references.get(1), initRef);
    assertTrue(initRef.isSimpleAssignmentToName());
    assertTrue(coll.isWellDefined());
  }

  /* =========================================================================
   * Partition B: Boundary Value Analysis (BVA) & Extremes
   * ========================================================================= */

  @Test(timeout = 4000)
  public void testEmptyReferenceCollection() {
    ReferenceCollectingCallback.ReferenceCollection emptyColl =
        new ReferenceCollectingCallback.ReferenceCollection();
    assertEquals(0, emptyColl.references.size());
    assertFalse(emptyColl.isWellDefined());
    assertFalse(emptyColl.isEscaped());
    assertFalse(emptyColl.firstReferenceIsAssigningDeclaration());
    assertNull(emptyColl.getInitializingReference());
    assertNull(emptyColl.getInitializingReferenceForConstants());
    assertTrue(emptyColl.isNeverAssigned());
    assertFalse(emptyColl.isAssignedOnceInLifetime());
  }

  @Test(timeout = 4000)
  public void testNeverAssignedVariable() {
    String js = "var unassigned; var y = unassigned;";
    CollectResult res = collect(js);

    ReferenceCollectingCallback.ReferenceCollection coll = res.collections.get("unassigned");
    assertNotNull(coll);
    assertTrue(coll.isNeverAssigned());
    assertNull(coll.getInitializingReference());
    assertFalse(coll.isWellDefined());
    assertFalse(coll.isAssignedOnceInLifetime());
  }

  @Test(timeout = 4000)
  public void testMultipleAssignmentsBoundary() {
    String js = "var m = 1; m = 2; m = 3;";
    CollectResult res = collect(js);

    ReferenceCollectingCallback.ReferenceCollection coll = res.collections.get("m");
    assertNotNull(coll);
    assertFalse(coll.isNeverAssigned());
    assertFalse(coll.isAssignedOnceInLifetime());
  }

  @Test(timeout = 4000)
  public void testInitializingReferenceForConstants() {
    String js = "use(C); var C = 99;";
    CollectResult res = collect(js);

    ReferenceCollectingCallback.ReferenceCollection coll = res.collections.get("C");
    assertNotNull(coll);
    ReferenceCollectingCallback.Reference constInit = coll.getInitializingReferenceForConstants();
    assertNotNull(constInit);
    assertSame(coll.references.get(1), constInit);
    assertNull(coll.getInitializingReference());
  }

  @Test(timeout = 4000)
  public void testLvalueVariations() {
    String js = "var a = 0; a++; a--; a += 5; var obj = {}; for (a in obj) {}";
    CollectResult res = collect(js);

    ReferenceCollectingCallback.ReferenceCollection coll = res.collections.get("a");
    assertNotNull(coll);
    for (ReferenceCollectingCallback.Reference ref : coll.references) {
      assertTrue("Expected lvalue for: " + ref.getParent().getType(), ref.isLvalue());
    }
  }

  @Test(timeout = 4000)
  public void testForInWithVarDeclaration() {
    String js = "var obj = {}; for (var item in obj) { use(item); }";
    CollectResult res = collect(js);

    ReferenceCollectingCallback.ReferenceCollection coll = res.collections.get("item");
    assertNotNull(coll);
    ReferenceCollectingCallback.Reference declRef = coll.references.get(0);
    assertTrue(declRef.isLvalue());
    ReferenceCollectingCallback.Reference readRef = coll.references.get(1);
    assertFalse(readRef.isLvalue());
  }

  @Test(timeout = 4000)
  public void testBlockBoundaryDirectInvocations() throws Exception {
    assertTrue(invokeIsBlockBoundary(new Node(Token.CASE), null));
    assertFalse(invokeIsBlockBoundary(new Node(Token.NAME), null));

    assertTrue(invokeIsBlockBoundary(new Node(Token.BLOCK), new Node(Token.DO)));
    assertTrue(invokeIsBlockBoundary(new Node(Token.BLOCK), new Node(Token.FOR)));
    assertTrue(invokeIsBlockBoundary(new Node(Token.BLOCK), new Node(Token.TRY)));
    assertTrue(invokeIsBlockBoundary(new Node(Token.BLOCK), new Node(Token.WHILE)));
    assertTrue(invokeIsBlockBoundary(new Node(Token.BLOCK), new Node(Token.WITH)));

    Node ifNode = new Node(Token.IF, new Node(Token.TRUE), new Node(Token.BLOCK));
    assertFalse(invokeIsBlockBoundary(ifNode.getFirstChild(), ifNode));
    assertTrue(invokeIsBlockBoundary(ifNode.getLastChild(), ifNode));

    Node andNode = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.FALSE));
    assertFalse(invokeIsBlockBoundary(andNode.getFirstChild(), andNode));
    assertTrue(invokeIsBlockBoundary(andNode.getLastChild(), andNode));

    Node hookNode = new Node(Token.HOOK, new Node(Token.TRUE), new Node(Token.NAME), new Node(Token.NAME));
    assertFalse(invokeIsBlockBoundary(hookNode.getFirstChild(), hookNode));
    assertTrue(invokeIsBlockBoundary(hookNode.getLastChild(), hookNode));

    Node orNode = new Node(Token.OR, new Node(Token.TRUE), new Node(Token.FALSE));
    assertFalse(invokeIsBlockBoundary(orNode.getFirstChild(), orNode));
    assertTrue(invokeIsBlockBoundary(orNode.getLastChild(), orNode));

    Node exprNode = new Node(Token.EXPR_RESULT, new Node(Token.NAME));
    assertFalse(invokeIsBlockBoundary(exprNode.getFirstChild(), exprNode));
  }

  /* =========================================================================
   * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
   * ========================================================================= */

  /**
   * Targets InlineVariablesTest::testNoInlineAliasesInLoop defect.
   * ReferenceCollection.isAssignedOnceInLifetime() must return false when the
   * assignment is executed inside a loop, because repeated iterations reassign it.
   */
  @Test(timeout = 4000)
  public void testAssignedOnceInLifetime_defectInWhileLoop() {
    String js = "var x; while (true) { x = 1; }";
    CollectResult res = collect(js);

    ReferenceCollectingCallback.ReferenceCollection col = res.collections.get("x");
    assertNotNull(col);
    assertEquals(2, col.references.size());
    assertFalse("Variable assigned inside loop must NOT be considered assigned once in lifetime",
        col.isAssignedOnceInLifetime());
  }

  @Test(timeout = 4000)
  public void testAssignedOnceInLifetime_defectInForLoop() {
    String js = "var a; for (var i = 0; i < 10; i++) { a = i; }";
    CollectResult res = collect(js);

    ReferenceCollectingCallback.ReferenceCollection col = res.collections.get("a");
    assertNotNull(col);
    assertFalse("Variable assigned inside for-loop must NOT be considered assigned once in lifetime",
        col.isAssignedOnceInLifetime());
  }

  @Test(timeout = 4000)
  public void testAssignedOnceInLifetime_defectInDoWhileLoop() {
    String js = "var y; do { y = 2; } while (false);";
    CollectResult res = collect(js);

    ReferenceCollectingCallback.ReferenceCollection col = res.collections.get("y");
    assertNotNull(col);
    assertFalse("Variable assigned inside do-while loop must NOT be considered assigned once in lifetime",
        col.isAssignedOnceInLifetime());
  }

  @Test(timeout = 4000)
  public void testAssignedOnceInLifetime_normalAssignmentOutsideLoop() {
    String js = "var valid = 10; var read = valid;";
    CollectResult res = collect(js);

    ReferenceCollectingCallback.ReferenceCollection col = res.collections.get("valid");
    assertNotNull(col);
    assertTrue("Variable initialized once outside loop should be assigned once in lifetime",
        col.isAssignedOnceInLifetime());
  }

  /* =========================================================================
   * Partition D: Exception & Defensive Guard Paths
   * ========================================================================= */

  @Test(timeout = 4000)
  public void testIsInitializingAssignmentAtPreconditionCheck() throws Exception {
    String js = "var initAtDecl = 1; initAtDecl = 2;";
    CollectResult res = collect(js);

    ReferenceCollectingCallback.ReferenceCollection coll = res.collections.get("initAtDecl");
    assertNotNull(coll);

    Method method = ReferenceCollectingCallback.ReferenceCollection.class
        .getDeclaredMethod("isInitializingAssignmentAt", int.class);
    method.setAccessible(true);

    try {
      method.invoke(coll, 1);
      fail("Expected InvocationTargetException wrapping IllegalStateException");
    } catch (InvocationTargetException ite) {
      assertTrue(ite.getCause() instanceof IllegalStateException);
    }
  }

  @Test(timeout = 4000)
  public void testIsInitializingAssignmentAtOutOfBounds() throws Exception {
    ReferenceCollectingCallback.ReferenceCollection coll =
        new ReferenceCollectingCallback.ReferenceCollection();
    Method method = ReferenceCollectingCallback.ReferenceCollection.class
        .getDeclaredMethod("isInitializingAssignmentAt", int.class);
    method.setAccessible(true);

    assertFalse((Boolean) method.invoke(coll, 0));
    assertFalse((Boolean) method.invoke(coll, -1));
    assertFalse((Boolean) method.invoke(coll, 5));
  }

  @Test(timeout = 4000)
  public void testIsNotWellDefinedWhenAssignedInBranch() {
    String js = "var v; if (true) { v = 1; } var r = v;";
    CollectResult res = collect(js);

    ReferenceCollectingCallback.ReferenceCollection coll = res.collections.get("v");
    assertNotNull(coll);
    assertFalse("Assignment inside conditional branch is not guaranteed before read",
        coll.isWellDefined());
  }

  /* =========================================================================
   * Partition E: Object Lifecycle & Contract Integrity
   * ========================================================================= */

  @Test(timeout = 4000)
  public void testBasicBlockProvablyExecutesBeforeRelations() {
    Node rootNode = new Node(Token.BLOCK);
    ReferenceCollectingCallback.BasicBlock rootBlock =
        new ReferenceCollectingCallback.BasicBlock(null, rootNode);
    assertNull(rootBlock.getParent());

    Node childNode1 = new Node(Token.BLOCK);
    ReferenceCollectingCallback.BasicBlock childBlock1 =
        new ReferenceCollectingCallback.BasicBlock(rootBlock, childNode1);
    assertSame(rootBlock, childBlock1.getParent());

    Node childNode2 = new Node(Token.BLOCK);
    ReferenceCollectingCallback.BasicBlock childBlock2 =
        new ReferenceCollectingCallback.BasicBlock(rootBlock, childNode2);

    Node grandChildNode = new Node(Token.BLOCK);
    ReferenceCollectingCallback.BasicBlock grandChildBlock =
        new ReferenceCollectingCallback.BasicBlock(childBlock1, grandChildNode);

    assertTrue(rootBlock.provablyExecutesBefore(rootBlock));
    assertTrue(rootBlock.provablyExecutesBefore(childBlock1));
    assertTrue(rootBlock.provablyExecutesBefore(grandChildBlock));

    assertFalse(childBlock1.provablyExecutesBefore(childBlock2));
    assertFalse(childBlock1.provablyExecutesBefore(rootBlock));
    assertFalse(rootBlock.provablyExecutesBefore(null));
  }

  @Test(timeout = 4000)
  public void testBasicBlockWithHoistedFunction() {
    Compiler compiler = new Compiler();
    Node script = compiler.parseTestCode("function hoistedFn() {}");
    Node funcNode = script.getFirstChild();
    assertTrue(NodeUtil.isHoistedFunctionDeclaration(funcNode));

    ReferenceCollectingCallback.BasicBlock rootBlock =
        new ReferenceCollectingCallback.BasicBlock(null, script);
    ReferenceCollectingCallback.BasicBlock hoistedBlock =
        new ReferenceCollectingCallback.BasicBlock(rootBlock, funcNode);
    ReferenceCollectingCallback.BasicBlock innerBlock =
        new ReferenceCollectingCallback.BasicBlock(hoistedBlock, new Node(Token.BLOCK));

    assertFalse("Hoisted block between root and inner prevents provablyExecutesBefore",
        rootBlock.provablyExecutesBefore(innerBlock));
  }

  @Test(timeout = 4000)
  public void testNewBleedingFunctionFactory() {
    Compiler compiler = new Compiler();
    final Node root = compiler.parseTestCode("var x = function bleed() {};");
    final ReferenceCollectingCallback.Reference[] holder =
        new ReferenceCollectingCallback.Reference[1];

    NodeTraversal.Callback cb = new NodeTraversal.AbstractPostOrderCallback() {
      @Override
      public void visit(NodeTraversal t, Node n, Node parent) {
        if (n.getType() == Token.FUNCTION && !n.getFirstChild().getString().isEmpty()) {
          ReferenceCollectingCallback.BasicBlock bb =
              new ReferenceCollectingCallback.BasicBlock(null, root);
          holder[0] = ReferenceCollectingCallback.Reference.newBleedingFunction(t, bb, n);
        }
      }
    };
    NodeTraversal.traverse(compiler, root, cb);

    assertNotNull(holder[0]);
    assertEquals("bleed", holder[0].getNameNode().getString());
    assertTrue(holder[0].isDeclaration());
    assertTrue(holder[0].isInitializingDeclaration());
    assertSame(holder[0].getParent(), holder[0].getAssignedValue());
    assertNotNull(holder[0].getGrandparent());
    assertNotNull(holder[0].getBasicBlock());
  }

  @Test(timeout = 4000)
  public void testAllBlockBoundaryTokensInTraversal() {
    String js =
        "do { var b1 = 1; } while (false);\n" +
        "for (var b2 = 0; b2 < 1; b2++) {}\n" +
        "try { var b3 = 1; } catch (e) { var b4 = 2; } finally { var b5 = 3; }\n" +
        "while (false) { var b6 = 1; }\n" +
        "with ({}) { var b7 = 1; }\n" +
        "var cond = 1;\n" +
        "cond && (cond = 2);\n" +
        "cond || (cond = 3);\n" +
        "cond ? (cond = 4) : (cond = 5);\n" +
        "if (cond) { cond = 6; } else { cond = 7; }\n" +
        "switch (cond) { case 1: cond = 8; break; }\n";

    CollectResult res = collect(js);
    assertTrue(res.collections.containsKey("b1"));
    assertTrue(res.collections.containsKey("b2"));
    assertTrue(res.collections.containsKey("b3"));
    assertTrue(res.collections.containsKey("b4"));
    assertTrue(res.collections.containsKey("b5"));
    assertTrue(res.collections.containsKey("b6"));
    assertTrue(res.collections.containsKey("b7"));
    assertTrue(res.collections.containsKey("cond"));
  }
}