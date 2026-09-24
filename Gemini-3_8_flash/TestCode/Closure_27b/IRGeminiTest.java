/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.rhino.IR
 * Known Defect (Defects4J): Issue 727 (testIssue727_1, testIssue727_2, testIssue727_3)
 * Root Cause: IR.tryFinally erroneously checked tryBody.isLabelName() and finallyBody.isLabelName()
 * instead of validating block statements, causing IllegalStateException when passed valid Block nodes.
 *
 * Test Partition Breakdown:
 * 1. Defect Targeting (Issue 727):
 *    - testIssue727_1: IR.tryFinally with empty blocks
 *    - testIssue727_2: IR.tryFinally with populated blocks
 *    - testIssue727_3: IR.tryFinally node structure (catch block synthetic generation)
 * 2. Structure & AST Helper Validation:
 *    - Function, paramList (empty, single, varargs, list), block, script
 *    - Var declarations (with/without init), returnNode, throwNode, exprResult
 *    - Control flow: ifNode (2 and 3 params), doNode, forIn, forNode (empty & expr variants)
 *    - Switch, caseNode, defaultCase (SYNTHETIC_BLOCK_PROP validation)
 *    - Labels, labelName (empty string precondition), breakNode, continueNode
 *    - Try-catch-finally variations (tryCatch, tryCatchFinally, catchNode)
 * 3. Expressions & Operators:
 *    - Call, newNode, getprop, getelem, assign (name, getprop, getelem targets)
 *    - Ternary hook, binary ops (comma, and, or, eq, sheq, add, sub)
 *    - Unary ops (not, voidNode, neg, pos)
 * 4. Literals:
 *    - objectlit, propdef (stringKey, getter, setter), arraylit (with empty nodes)
 *    - regexp (single and dual param), string, stringKey, number, thisNode, trueNode, falseNode, nullNode
 * 5. Exception & Defensive Guard Boundaries:
 *    - Invalid node types passed to statements and expressions
 *    - Non-assignment targets passed to assign()
 *    - Incompatible nodes passed to case/default/var/paramList
 */

package com.google.javascript.rhino;

import org.junit.Test;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class IRGeminiTest {

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Issue 727)
  // =========================================================================

  @Test(timeout = 4000)
  public void testIssue727_1() {
    Node tryBody = IR.block();
    Node finallyBody = IR.block();
    Node tryNode = IR.tryFinally(tryBody, finallyBody);

    assertNotNull(tryNode);
    assertEquals(Token.TRY, tryNode.getType());
    assertEquals(3, tryNode.getChildCount());
    assertSame(tryBody, tryNode.getFirstChild());
    assertEquals(Token.BLOCK, tryNode.getChildAtIndex(1).getType());
    assertSame(finallyBody, tryNode.getLastChild());
  }

  @Test(timeout = 4000)
  public void testIssue727_2() {
    Node stmt1 = IR.exprResult(IR.number(1));
    Node stmt2 = IR.exprResult(IR.number(2));
    Node tryBody = IR.block(stmt1);
    Node finallyBody = IR.block(stmt2);

    Node tryNode = IR.tryFinally(tryBody, finallyBody);
    assertNotNull(tryNode);
    assertEquals(Token.TRY, tryNode.getType());
    assertSame(tryBody, tryNode.getFirstChild());
    assertSame(finallyBody, tryNode.getLastChild());
  }

  @Test(timeout = 4000)
  public void testIssue727_3() {
    Node tryBody = IR.block();
    tryBody.setLineno(42);
    tryBody.setCharno(10);
    Node finallyBody = IR.block();

    Node tryNode = IR.tryFinally(tryBody, finallyBody);
    Node syntheticCatch = tryNode.getChildAtIndex(1);
    assertNotNull(syntheticCatch);
    assertEquals(Token.BLOCK, syntheticCatch.getType());
    assertEquals(0, syntheticCatch.getChildCount());
    assertEquals(42, syntheticCatch.getLineno());
    assertEquals(10, syntheticCatch.getCharno());
  }

  // =========================================================================
  // Partition A: Core Functional AST Construction
  // =========================================================================

  @Test(timeout = 4000)
  public void testEmpty() {
    Node node = IR.empty();
    assertNotNull(node);
    assertEquals(Token.EMPTY, node.getType());
    assertEquals(0, node.getChildCount());
  }

  @Test(timeout = 4000)
  public void testFunction() {
    Node name = IR.name("myFunc");
    Node params = IR.paramList(IR.name("a"), IR.name("b"));
    Node body = IR.block(IR.returnNode(IR.name("a")));

    Node func = IR.function(name, params, body);
    assertEquals(Token.FUNCTION, func.getType());
    assertEquals(3, func.getChildCount());
    assertSame(name, func.getFirstChild());
    assertSame(params, func.getChildAtIndex(1));
    assertSame(body, func.getLastChild());
  }

  @Test(timeout = 4000)
  public void testParamListVariations() {
    Node emptyList = IR.paramList();
    assertEquals(Token.PARAM_LIST, emptyList.getType());
    assertEquals(0, emptyList.getChildCount());

    Node singleList = IR.paramList(IR.name("p1"));
    assertEquals(Token.PARAM_LIST, singleList.getType());
    assertEquals(1, singleList.getChildCount());
    assertEquals("p1", singleList.getFirstChild().getString());

    Node varargsList = IR.paramList(IR.name("x"), IR.name("y"), IR.name("z"));
    assertEquals(3, varargsList.getChildCount());

    List<Node> list = new ArrayList<Node>();
    list.add(IR.name("l1"));
    list.add(IR.name("l2"));
    Node listParams = IR.paramList(list);
    assertEquals(2, listParams.getChildCount());
    assertEquals("l1", listParams.getFirstChild().getString());
    assertEquals("l2", listParams.getLastChild().getString());
  }

  @Test(timeout = 4000)
  public void testBlockAndScript() {
    Node emptyBlock = IR.block();
    assertEquals(Token.BLOCK, emptyBlock.getType());
    assertEquals(0, emptyBlock.getChildCount());

    Node singleStmt = IR.returnNode();
    Node singleBlock = IR.block(singleStmt);
    assertEquals(Token.BLOCK, singleBlock.getType());
    assertEquals(1, singleBlock.getChildCount());
    assertSame(singleStmt, singleBlock.getFirstChild());

    Node s1 = IR.var(IR.name("a"));
    Node s2 = IR.var(IR.name("b"));
    Node multiBlock = IR.block(s1, s2);
    assertEquals(2, multiBlock.getChildCount());

    Node scriptNode = IR.script(s1, s2);
    assertEquals(Token.SCRIPT, scriptNode.getType());
    assertEquals(2, scriptNode.getChildCount());
  }

  @Test(timeout = 4000)
  public void testVarDeclarations() {
    Node nameOnly = IR.var(IR.name("v1"));
    assertEquals(Token.VAR, nameOnly.getType());
    assertEquals(1, nameOnly.getChildCount());
    assertEquals("v1", nameOnly.getFirstChild().getString());

    Node nameWithVal = IR.var(IR.name("v2"), IR.number(42));
    assertEquals(Token.VAR, nameWithVal.getType());
    assertEquals(1, nameWithVal.getChildCount());
    Node nameNode = nameWithVal.getFirstChild();
    assertEquals(1, nameNode.getChildCount());
    assertEquals(Token.NUMBER, nameNode.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testReturnAndThrowAndExprResult() {
    Node emptyReturn = IR.returnNode();
    assertEquals(Token.RETURN, emptyReturn.getType());
    assertEquals(0, emptyReturn.getChildCount());

    Node valueReturn = IR.returnNode(IR.string("ret"));
    assertEquals(Token.RETURN, valueReturn.getType());
    assertEquals(1, valueReturn.getChildCount());
    assertEquals(Token.STRING, valueReturn.getFirstChild().getType());

    Node throwNode = IR.throwNode(IR.string("err"));
    assertEquals(Token.THROW, throwNode.getType());
    assertEquals(1, throwNode.getChildCount());

    Node exprResult = IR.exprResult(IR.nullNode());
    assertEquals(Token.EXPR_RESULT, exprResult.getType());
    assertEquals(1, exprResult.getChildCount());
  }

  @Test(timeout = 4000)
  public void testConditionalsAndLoops() {
    Node cond = IR.trueNode();
    Node thenBlock = IR.block();
    Node elseBlock = IR.block();

    Node if2 = IR.ifNode(cond, thenBlock);
    assertEquals(Token.IF, if2.getType());
    assertEquals(2, if2.getChildCount());

    Node if3 = IR.ifNode(cond, thenBlock, elseBlock);
    assertEquals(Token.IF, if3.getType());
    assertEquals(3, if3.getChildCount());

    Node doNode = IR.doNode(thenBlock, cond);
    assertEquals(Token.DO, doNode.getType());
    assertSame(thenBlock, doNode.getFirstChild());
    assertSame(cond, doNode.getLastChild());

    Node forInVar = IR.forIn(IR.var(IR.name("k")), IR.name("obj"), IR.block());
    assertEquals(Token.FOR, forInVar.getType());
    assertEquals(3, forInVar.getChildCount());

    Node forInExpr = IR.forIn(IR.name("k"), IR.name("obj"), IR.block());
    assertEquals(Token.FOR, forInExpr.getType());

    Node forFull = IR.forNode(IR.var(IR.name("i"), IR.number(0)),
                              IR.not(IR.name("stop")),
                              IR.assign(IR.name("i"), IR.number(1)),
                              IR.block());
    assertEquals(Token.FOR, forFull.getType());
    assertEquals(4, forFull.getChildCount());

    Node forEmptyParts = IR.forNode(IR.empty(), IR.empty(), IR.empty(), IR.block());
    assertEquals(Token.FOR, forEmptyParts.getType());
    assertEquals(4, forEmptyParts.getChildCount());
  }

  @Test(timeout = 4000)
  public void testSwitchAndCase() {
    Node case1 = IR.caseNode(IR.number(1), IR.block());
    assertEquals(Token.CASE, case1.getType());
    assertTrue(case1.getLastChild().getBooleanProp(Node.SYNTHETIC_BLOCK_PROP));

    Node defaultCase = IR.defaultCase(IR.block());
    assertEquals(Token.DEFAULT_CASE, defaultCase.getType());
    assertTrue(defaultCase.getFirstChild().getBooleanProp(Node.SYNTHETIC_BLOCK_PROP));

    Node switchNode = IR.switchNode(IR.name("val"), case1, defaultCase);
    assertEquals(Token.SWITCH, switchNode.getType());
    assertEquals(3, switchNode.getChildCount());
  }

  @Test(timeout = 4000)
  public void testLabelsAndJumps() {
    Node lblName = IR.labelName("myLabel");
    assertEquals(Token.LABEL_NAME, lblName.getType());
    assertEquals("myLabel", lblName.getString());

    Node labelStmt = IR.label(lblName, IR.block());
    assertEquals(Token.LABEL, labelStmt.getType());

    Node bareBreak = IR.breakNode();
    assertEquals(Token.BREAK, bareBreak.getType());
    assertEquals(0, bareBreak.getChildCount());

    Node targetBreak = IR.breakNode(IR.labelName("l1"));
    assertEquals(Token.BREAK, targetBreak.getType());
    assertEquals(1, targetBreak.getChildCount());

    Node bareCont = IR.continueNode();
    assertEquals(Token.CONTINUE, bareCont.getType());
    assertEquals(0, bareCont.getChildCount());

    Node targetCont = IR.continueNode(IR.labelName("l2"));
    assertEquals(Token.CONTINUE, targetCont.getType());
    assertEquals(1, targetCont.getChildCount());
  }

  @Test(timeout = 4000)
  public void testTryCatchCatchFinally() {
    Node tryBody = IR.block();
    Node catchBody = IR.block();
    Node catchNode = IR.catchNode(IR.name("err"), catchBody);
    assertEquals(Token.CATCH, catchNode.getType());
    assertEquals(2, catchNode.getChildCount());

    Node tryCatchNode = IR.tryCatch(tryBody, catchNode);
    assertEquals(Token.TRY, tryCatchNode.getType());
    assertEquals(2, tryCatchNode.getChildCount());

    Node finallyBody = IR.block();
    Node tryCatchFinallyNode = IR.tryCatchFinally(tryBody, catchNode, finallyBody);
    assertEquals(Token.TRY, tryCatchFinallyNode.getType());
    assertEquals(3, tryCatchFinallyNode.getChildCount());
    assertSame(finallyBody, tryCatchFinallyNode.getLastChild());
  }

  @Test(timeout = 4000)
  public void testExpressionsAndCalls() {
    Node target = IR.name("foo");
    Node callNode = IR.call(target, IR.number(1), IR.string("arg"));
    assertEquals(Token.CALL, callNode.getType());
    assertEquals(3, callNode.getChildCount());

    Node newNode = IR.newNode(target, IR.nullNode());
    assertEquals(Token.NEW, newNode.getType());
    assertEquals(2, newNode.getChildCount());

    Node getprop = IR.getprop(target, IR.string("bar"));
    assertEquals(Token.GETPROP, getprop.getType());

    Node getelem = IR.getelem(target, IR.number(0));
    assertEquals(Token.GETELEM, getelem.getType());

    Node assignName = IR.assign(target, IR.number(10));
    assertEquals(Token.ASSIGN, assignName.getType());

    Node assignProp = IR.assign(getprop, IR.number(20));
    assertEquals(Token.ASSIGN, assignProp.getType());

    Node assignElem = IR.assign(getelem, IR.number(30));
    assertEquals(Token.ASSIGN, assignElem.getType());

    Node hook = IR.hook(IR.trueNode(), IR.number(1), IR.number(2));
    assertEquals(Token.HOOK, hook.getType());
  }

  @Test(timeout = 4000)
  public void testOperators() {
    assertEquals(Token.COMMA, IR.comma(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.AND, IR.and(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.OR, IR.or(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.NOT, IR.not(IR.name("a")).getType());
    assertEquals(Token.EQ, IR.eq(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.SHEQ, IR.sheq(IR.name("a"), IR.name("b")).getType());
    assertEquals(Token.VOID, IR.voidNode(IR.number(0)).getType());
    assertEquals(Token.NEG, IR.neg(IR.number(1)).getType());
    assertEquals(Token.POS, IR.pos(IR.number(2)).getType());
    assertEquals(Token.ADD, IR.add(IR.number(1), IR.number(2)).getType());
    assertEquals(Token.SUB, IR.sub(IR.number(5), IR.number(3)).getType());
  }

  @Test(timeout = 4000)
  public void testLiterals() {
    Node prop = IR.propdef(IR.stringKey("key1"), IR.number(123));
    assertEquals(Token.STRING_KEY, prop.getType());
    assertEquals(1, prop.getChildCount());

    Node objLit = IR.objectlit(prop);
    assertEquals(Token.OBJECTLIT, objLit.getType());
    assertEquals(1, objLit.getChildCount());

    Node arrLit = IR.arraylit(IR.number(1), IR.empty(), IR.string("x"));
    assertEquals(Token.ARRAYLIT, arrLit.getType());
    assertEquals(3, arrLit.getChildCount());

    Node rx1 = IR.regexp(IR.string("pattern"));
    assertEquals(Token.REGEXP, rx1.getType());
    assertEquals(1, rx1.getChildCount());

    Node rx2 = IR.regexp(IR.string("pattern"), IR.string("g"));
    assertEquals(Token.REGEXP, rx2.getType());
    assertEquals(2, rx2.getChildCount());

    assertEquals("test", IR.string("test").getString());
    assertEquals(3.14, IR.number(3.14).getDouble(), 0.0001);
    assertEquals(Token.THIS, IR.thisNode().getType());
    assertEquals(Token.TRUE, IR.trueNode().getType());
    assertEquals(Token.FALSE, IR.falseNode().getType());
    assertEquals(Token.NULL, IR.nullNode().getType());
  }

  @Test(timeout = 4000)
  public void testObjectlitWithGetterSetter() {
    Node getter = new Node(Token.GETTER_DEF);
    getter.addChildToFront(IR.function(IR.name(""), IR.paramList(), IR.block()));
    Node setter = new Node(Token.SETTER_DEF);
    setter.addChildToFront(IR.function(IR.name(""), IR.paramList(IR.name("val")), IR.block()));

    Node obj = IR.objectlit(getter, setter);
    assertEquals(Token.OBJECTLIT, obj.getType());
    assertEquals(2, obj.getChildCount());
  }

  // =========================================================================
  // Partition B & D: Boundary Value Analysis & Exception Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testFunctionWithNonNameNode() {
    IR.function(IR.number(1), IR.paramList(), IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testFunctionWithNonParamList() {
    IR.function(IR.name("fn"), IR.block(), IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testFunctionWithNonBlockBody() {
    IR.function(IR.name("fn"), IR.paramList(), IR.name("invalid"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testParamListSingleWithNonName() {
    IR.paramList(IR.number(5));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testParamListVarargsWithNonName() {
    IR.paramList(IR.name("valid"), IR.number(5));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testParamListListWithNonName() {
    IR.paramList(Collections.singletonList(IR.string("bad")));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testBlockWithNonStatement() {
    IR.block(IR.string("notAStatement"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testScriptWithNonStatement() {
    IR.script(IR.name("notAStatement"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testVarWithNonName() {
    IR.var(IR.number(1));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testVarWithNameAlreadyHavingChildren() {
    Node n = IR.name("x");
    n.addChildToFront(IR.number(10));
    IR.var(n, IR.number(20));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testVarWithValueNotAnExpression() {
    IR.var(IR.name("x"), IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testReturnWithNonExpression() {
    IR.returnNode(IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testThrowWithNonExpression() {
    IR.throwNode(IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testExprResultWithNonExpression() {
    IR.exprResult(IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testIfNode2WithNonExpressionCondition() {
    IR.ifNode(IR.block(), IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testIfNode2WithNonBlockThen() {
    IR.ifNode(IR.trueNode(), IR.exprResult(IR.number(1)));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testIfNode3WithNonBlockElse() {
    IR.ifNode(IR.trueNode(), IR.block(), IR.name("bad"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testDoNodeWithNonBlock() {
    IR.doNode(IR.name("bad"), IR.trueNode());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testDoNodeWithNonExpressionCondition() {
    IR.doNode(IR.block(), IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testForInWithInvalidTarget() {
    IR.forIn(IR.block(), IR.name("obj"), IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testForNodeWithInvalidInit() {
    IR.forNode(IR.returnNode(), IR.empty(), IR.empty(), IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testSwitchWithNonCase() {
    IR.switchNode(IR.name("x"), IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCaseWithNonExpression() {
    IR.caseNode(IR.block(), IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCaseWithNonBlock() {
    IR.caseNode(IR.number(1), IR.returnNode());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testDefaultCaseWithNonBlock() {
    IR.defaultCase(IR.returnNode());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testLabelNameWithEmptyString() {
    IR.labelName("");
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testLabelWithNonLabelName() {
    IR.label(IR.name("lbl"), IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testLabelWithNonStatement() {
    IR.label(IR.labelName("lbl"), IR.number(1));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testTryCatchWithNonBlockTry() {
    IR.tryCatch(IR.name("bad"), IR.catchNode(IR.name("e"), IR.block()));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testTryCatchWithNonCatchNode() {
    IR.tryCatch(IR.block(), IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testTryCatchFinallyWithNonBlockFinally() {
    IR.tryCatchFinally(IR.block(), IR.catchNode(IR.name("e"), IR.block()), IR.number(1));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCatchNodeWithNonNameExpr() {
    IR.catchNode(IR.number(1), IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCatchNodeWithNonBlock() {
    IR.catchNode(IR.name("e"), IR.returnNode());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testBreakNodeWithNonLabelName() {
    IR.breakNode(IR.name("bad"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testContinueNodeWithNonLabelName() {
    IR.continueNode(IR.name("bad"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCallWithNonExpressionArg() {
    IR.call(IR.name("fn"), IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNewNodeWithNonExpressionArg() {
    IR.newNode(IR.name("Cls"), IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetpropWithNonStringProp() {
    IR.getprop(IR.name("a"), IR.number(1));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testAssignWithInvalidTarget() {
    IR.assign(IR.number(1), IR.number(2));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testHookWithNonExpressionCondition() {
    IR.hook(IR.block(), IR.number(1), IR.number(2));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testHookWithNonExpressionTrueval() {
    IR.hook(IR.trueNode(), IR.block(), IR.number(2));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testHookWithNonExpressionFalseval() {
    IR.hook(IR.trueNode(), IR.number(1), IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testBinaryOpWithNonExpression() {
    IR.add(IR.block(), IR.number(1));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testUnaryOpWithNonExpression() {
    IR.not(IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testObjectlitWithInvalidProp() {
    IR.objectlit(IR.number(1));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testObjectlitWithPropHavingNoChildren() {
    IR.objectlit(IR.stringKey("key"));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testPropdefWithNonStringKey() {
    IR.propdef(IR.name("bad"), IR.number(1));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testPropdefWithKeyAlreadyHavingChildren() {
    Node key = IR.stringKey("k");
    key.addChildToFront(IR.number(1));
    IR.propdef(key, IR.number(2));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testArraylitWithInvalidChild() {
    IR.arraylit(IR.block());
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testRegexpWithNonString() {
    IR.regexp(IR.number(123));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testRegexpWithNonStringFlags() {
    IR.regexp(IR.string("abc"), IR.number(1));
  }

  // =========================================================================
  // Partition E: Branch Coverage across mayBeStatement and mayBeExpression
  // =========================================================================

  @Test(timeout = 4000)
  public void testMayBeStatementBranches() {
    int[] stmtTokens = new int[] {
        Token.EMPTY, Token.BLOCK, Token.BREAK, Token.CONST, Token.CONTINUE,
        Token.DEBUGGER, Token.DO, Token.EXPR_RESULT, Token.FOR, Token.IF,
        Token.LABEL, Token.RETURN, Token.SWITCH, Token.THROW, Token.TRY,
        Token.VAR, Token.WHILE, Token.WITH
    };

    for (int t : stmtTokens) {
      Node n = new Node(t);
      if (t == Token.LABEL) {
        n.addChildToBack(IR.labelName("l"));
        n.addChildToBack(IR.block());
      }
      Node blk = IR.block(n);
      assertNotNull(blk);
    }

    Node funcStmt = IR.function(IR.name("f"), IR.paramList(), IR.block());
    Node blkFunc = IR.block(funcStmt);
    assertNotNull(blkFunc);
  }

  @Test(timeout = 4000)
  public void testMayBeExpressionBranches() {
    int[] exprTokens = new int[] {
        Token.ADD, Token.AND, Token.ARRAYLIT, Token.ASSIGN, Token.ASSIGN_BITOR,
        Token.ASSIGN_BITXOR, Token.ASSIGN_BITAND, Token.ASSIGN_LSH, Token.ASSIGN_RSH,
        Token.ASSIGN_URSH, Token.ASSIGN_ADD, Token.ASSIGN_SUB, Token.ASSIGN_MUL,
        Token.ASSIGN_DIV, Token.ASSIGN_MOD, Token.BITAND, Token.BITOR, Token.BITNOT,
        Token.BITXOR, Token.CALL, Token.COMMA, Token.DEC, Token.DELPROP,
        Token.DIV, Token.EQ, Token.FALSE, Token.GE, Token.GETPROP, Token.GETELEM,
        Token.GT, Token.HOOK, Token.IN, Token.INC, Token.INSTANCEOF, Token.LE,
        Token.LSH, Token.LT, Token.MOD, Token.MUL, Token.NAME, Token.NE, Token.NEG,
        Token.NEW, Token.NOT, Token.NUMBER, Token.NULL, Token.OBJECTLIT, Token.OR,
        Token.POS, Token.REGEXP, Token.RSH, Token.SHEQ, Token.SHNE, Token.STRING,
        Token.SUB, Token.THIS, Token.TYPEOF, Token.TRUE, Token.URSH, Token.VOID
    };

    for (int t : exprTokens) {
      Node n = (t == Token.NAME || t == Token.STRING) ? Node.newString(t, "s")
             : (t == Token.NUMBER) ? Node.newNumber(0)
             : new Node(t);
      Node exprRes = IR.exprResult(n);
      assertNotNull(exprRes);
      assertEquals(Token.EXPR_RESULT, exprRes.getType());
    }

    Node funcExpr = IR.function(IR.name("fn"), IR.paramList(), IR.block());
    Node res = IR.exprResult(funcExpr);
    assertNotNull(res);
  }
}