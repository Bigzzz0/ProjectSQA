package com.google.javascript.jscomp;

import com.google.common.base.Predicates;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Methods & Decision Branches Checked:
 * 1. getExpressionBooleanValue / getBooleanValue:
 *    - ASSIGN, COMMA, NOT, AND, OR, HOOK, STRING, NUMBER, NULL, FALSE, VOID, TRUE, ARRAYLIT, OBJECTLIT,
 *      REGEXP, NAME (undefined, NaN, Infinity, unknown names).
 * 2. getStringValue / getNumberValue:
 *    - STRING, NAME (undefined, Infinity, NaN, regular), NUMBER (integer representation vs double),
 *      FALSE, TRUE, NULL, VOID.
 * 3. getFunctionName / getNearestFunctionName:
 *    - Anonymous vs named function declarations, VAR assignment, qualified property assignment,
 *      object literal string keys.
 * 4. isImmutableValue / isLiteralValue:
 *    - Primitives, unary NEG, VOID, NAMEs, ARRAYLIT, REGEXP, OBJECTLIT (recursive checks), FUNCTION.
 * 5. isValidDefineValue:
 *    - Literals, binary ops, unary ops, qualified names in define sets vs missing names.
 * 6. isEmptyBlock / isSimpleOperator / isSimpleOperatorType / precedence / associativity / commutativity:
 *    - Empty blocks, blocks with EMPTY nodes, blocks with statements, operator precendence tables,
 *      unknown token handling.
 * 7. mayEffectMutableState / mayHaveSideEffects / functionCallHasSideEffects / constructorCallHasSideEffects:
 *    - Built-in safe constructors (Array, Date, Error, Object, RegExp, XMLHttpRequest), safe built-in
 *      functions, Math methods, toString/valueOf, regexp methods, mutating assignments, THROW.
 * 8. AST structural inspection & query:
 *    - isGet, isGetProp, isName, isNew, isVar, isVarDeclaration, getAssignedValue, isExprAssign, isExprCall,
 *      isLoopStructure, getLoopCodeBlock, isWithinLoop, isControlStructure, isControlStructureCodeBlock,
 *      getConditionExpression, isStatement, isHoistedFunctionDeclaration, isFunctionExpression,
 *      isEmptyFunctionExpression, isVarArgsFunction, isObjectCallMethod, isSimpleFunctionObjectCall,
 *      isLhs, isObjectLitKey, isGetOrSetKey, opToStr, opToStrNoFail, containsType, redeclareVarsInsideBranch,
 *      newQualifiedNameNode, getRootOfQualifiedName, isLatin, isValidPropertyName, prototype helpers,
 *      traversals (visitPreOrder, visitPostOrder, getCount, has), try/catch/finally inspection,
 *      getArgumentForFunction, getArgumentForCallOrNew.
 * 9. AST mutations:
 *    - removeChild across STATEMENT_BLOCK, VAR (single vs multiple declarations), BLOCK, LABEL, FOR(4),
 *      and defensive exception path.
 *    - tryMergeBlock on SCRIPT/BLOCK vs non-statement block.
 * 10. CRITICAL KNOWN DEFECT ZONE:
 *    - evaluatesToLocalValue(Token.NEW):
 *      In the defective version, Token.NEW unconditionally returns true. However, constructors can leak
 *      'this' or alias external state, so evaluating a NEW expression cannot be assumed local.
 *      The test testEvaluatesToLocalValue_newOperatorDefect asserts assertFalse for 'new Foo()',
 *      exposing the defect documented in Defects4J (NodeUtilTest::testLocalValue1).
 * ---------------------------------------------------------------------------------------------------------
 */
public class NodeUtilGeminiTest {

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetBooleanValue_literals() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString("non-empty")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString("")));

    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newNumber(1.5)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newNumber(-1)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newNumber(0)));

    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.NULL)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.VOID)));

    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.ARRAYLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.OBJECTLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.REGEXP)));

    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "NaN")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "customVar")));
  }

  @Test(timeout = 4000)
  public void testGetExpressionBooleanValue_complexExpressions() {
    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(assign));

    Node comma = new Node(Token.COMMA, Node.newNumber(1), Node.newString(""));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(comma));

    Node not = new Node(Token.NOT, new Node(Token.TRUE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(not));

    Node andTrue = new Node(Token.AND, new Node(Token.TRUE), Node.newNumber(5));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(andTrue));

    Node orFalse = new Node(Token.OR, new Node(Token.FALSE), Node.newNumber(0));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(orFalse));

    Node hookMatching = new Node(Token.HOOK,
        Node.newString(Token.NAME, "cond"),
        new Node(Token.TRUE),
        new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(hookMatching));

    Node hookDiverging = new Node(Token.HOOK,
        Node.newString(Token.NAME, "cond"),
        new Node(Token.TRUE),
        new Node(Token.FALSE));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getExpressionBooleanValue(hookDiverging));
  }

  @Test(timeout = 4000)
  public void testGetStringValue() {
    assertEquals("hello", NodeUtil.getStringValue(Node.newString("hello")));
    assertEquals("undefined", NodeUtil.getStringValue(Node.newString(Token.NAME, "undefined")));
    assertEquals("Infinity", NodeUtil.getStringValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals("NaN", NodeUtil.getStringValue(Node.newString(Token.NAME, "NaN")));
    assertNull(NodeUtil.getStringValue(Node.newString(Token.NAME, "other")));

    assertEquals("10", NodeUtil.getStringValue(Node.newNumber(10.0)));
    assertEquals("10.5", NodeUtil.getStringValue(Node.newNumber(10.5)));

    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID)));
    assertNull(NodeUtil.getStringValue(new Node(Token.OBJECTLIT)));
  }

  @Test(timeout = 4000)
  public void testGetNumberValue() {
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(new Node(Token.TRUE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.FALSE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.NULL)));
    assertEquals(Double.valueOf(42.5), NodeUtil.getNumberValue(Node.newNumber(42.5)));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(new Node(Token.VOID))));

    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "undefined"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "NaN"))));
    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY),
        NodeUtil.getNumberValue(Node.newString(Token.NAME, "Infinity")));
    assertNull(NodeUtil.getNumberValue(Node.newString(Token.NAME, "foo")));
    assertNull(NodeUtil.getNumberValue(Node.newString("123")));
  }

  @Test(timeout = 4000)
  public void testGetFunctionNameAndNearest() {
    Node fnNamed = NodeUtil.newFunctionNode("myFunc", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    Node block = new Node(Token.BLOCK, fnNamed);
    assertEquals("myFunc", NodeUtil.getFunctionName(fnNamed));
    assertEquals("myFunc", NodeUtil.getNearestFunctionName(fnNamed));

    Node fnAnon = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    Node varName = Node.newString(Token.NAME, "varFunc");
    varName.addChildToBack(fnAnon);
    Node varNode = new Node(Token.VAR, varName);
    assertEquals("varFunc", NodeUtil.getFunctionName(fnAnon));

    Node fnAnon2 = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    CodingConvention conv = new DefaultCodingConvention();
    Node qName = NodeUtil.newQualifiedNameNode(conv, "a.b.c", 1, 0);
    Node assign = new Node(Token.ASSIGN, qName, fnAnon2);
    assertEquals("a.b.c", NodeUtil.getFunctionName(fnAnon2));

    Node fnAnon3 = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    Node objKey = Node.newString("keyName");
    objKey.addChildToBack(fnAnon3);
    Node objLit = new Node(Token.OBJECTLIT, objKey);
    assertNull(NodeUtil.getFunctionName(fnAnon3));
    assertEquals("keyName", NodeUtil.getNearestFunctionName(fnAnon3));
  }

  @Test(timeout = 4000)
  public void testIsImmutableValueAndIsLiteralValue() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString("s")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(1)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID, Node.newNumber(0))));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newNumber(5))));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "normalVar")));

    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"));
    assertTrue(NodeUtil.isLiteralValue(arrayLit, false));

    Node nonLitArray = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "x"));
    assertFalse(NodeUtil.isLiteralValue(nonLitArray, false));

    Node key = Node.newString("k");
    key.addChildToBack(Node.newNumber(10));
    Node objLit = new Node(Token.OBJECTLIT, key);
    assertTrue(NodeUtil.isLiteralValue(objLit, false));

    Node fnExp = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    Node paren = new Node(Token.EXPR_RESULT, fnExp);
    assertTrue(NodeUtil.isLiteralValue(fnExp, true));
    assertFalse(NodeUtil.isLiteralValue(fnExp, false));
  }

  @Test(timeout = 4000)
  public void testIsValidDefineValue() {
    Set<String> defines = new HashSet<String>(Arrays.asList("DEF_A", "app.DEF_B"));

    assertTrue(NodeUtil.isValidDefineValue(Node.newString("val"), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(42), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isValidDefineValue(add, defines));

    Node not = new Node(Token.NOT, new Node(Token.TRUE));
    assertTrue(NodeUtil.isValidDefineValue(not, defines));

    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "DEF_A"), defines));
    assertFalse(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "UNKNOWN_DEF"), defines));

    CodingConvention conv = new DefaultCodingConvention();
    Node qName = NodeUtil.newQualifiedNameNode(conv, "app.DEF_B", 1, 0);
    assertTrue(NodeUtil.isValidDefineValue(qName, defines));

    Node invalidQName = NodeUtil.newQualifiedNameNode(conv, "app.UNKNOWN", 1, 0);
    assertFalse(NodeUtil.isValidDefineValue(invalidQName, defines));

    assertFalse(NodeUtil.isValidDefineValue(new Node(Token.ARRAYLIT), defines));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis & Edge Cases
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsEmptyBlock() {
    assertFalse(NodeUtil.isEmptyBlock(new Node(Token.EXPR_RESULT)));

    Node emptyBlock = new Node(Token.BLOCK);
    assertTrue(NodeUtil.isEmptyBlock(emptyBlock));

    Node blockWithEmptyNodes = new Node(Token.BLOCK, new Node(Token.EMPTY), new Node(Token.EMPTY));
    assertTrue(NodeUtil.isEmptyBlock(blockWithEmptyNodes));

    Node nonEmptyBlock = new Node(Token.BLOCK, new Node(Token.RETURN));
    assertFalse(NodeUtil.isEmptyBlock(nonEmptyBlock));
  }

  @Test(timeout = 4000)
  public void testIsSimpleOperator() {
    assertTrue(NodeUtil.isSimpleOperatorType(Token.ADD));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.SUB));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.TYPEOF));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.URSH));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.ASSIGN));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.CALL));

    Node addNode = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isSimpleOperator(addNode));
  }

  @Test(timeout = 4000)
  public void testAssociativityAndCommutativity() {
    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertTrue(NodeUtil.isAssociative(Token.BITOR));
    assertTrue(NodeUtil.isAssociative(Token.BITAND));
    assertFalse(NodeUtil.isAssociative(Token.ADD));
    assertFalse(NodeUtil.isAssociative(Token.SUB));

    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertTrue(NodeUtil.isCommutative(Token.BITOR));
    assertTrue(NodeUtil.isCommutative(Token.BITAND));
    assertFalse(NodeUtil.isCommutative(Token.ADD));
    assertFalse(NodeUtil.isCommutative(Token.DIV));
  }

  @Test(timeout = 4000)
  public void testPrecedence() {
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN_ADD));
    assertEquals(2, NodeUtil.precedence(Token.HOOK));
    assertEquals(3, NodeUtil.precedence(Token.OR));
    assertEquals(4, NodeUtil.precedence(Token.AND));
    assertEquals(5, NodeUtil.precedence(Token.BITOR));
    assertEquals(6, NodeUtil.precedence(Token.BITXOR));
    assertEquals(7, NodeUtil.precedence(Token.BITAND));
    assertEquals(8, NodeUtil.precedence(Token.EQ));
    assertEquals(9, NodeUtil.precedence(Token.LT));
    assertEquals(10, NodeUtil.precedence(Token.LSH));
    assertEquals(11, NodeUtil.precedence(Token.ADD));
    assertEquals(12, NodeUtil.precedence(Token.MUL));
    assertEquals(13, NodeUtil.precedence(Token.NOT));
    assertEquals(15, NodeUtil.precedence(Token.CALL));
    assertEquals(15, NodeUtil.precedence(Token.NAME));
  }

  @Test(timeout = 4000)
  public void testIsLatinAndValidPropertyName() {
    assertTrue(NodeUtil.isLatin("asciiOnly"));
    assertTrue(NodeUtil.isLatin(""));
    assertFalse(NodeUtil.isLatin("ascii\u0080extended"));
    assertFalse(NodeUtil.isLatin("Unicode\u2603"));

    assertTrue(NodeUtil.isValidPropertyName("validProp"));
    assertTrue(NodeUtil.isValidPropertyName("$dollar_123"));
    assertFalse(NodeUtil.isValidPropertyName("class"));
    assertFalse(NodeUtil.isValidPropertyName("123num"));
    assertFalse(NodeUtil.isValidPropertyName("prop\u2603"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Closure Known Defect)
  // =========================================================================

  /**
   * CRITICAL DEFECT TEST:
   * Target: evaluatesToLocalValue(Token.NEW)
   * Defects4J Failure: NodeUtilTest::testLocalValue1
   * The constructor call 'new Foo()' must NOT be assumed to evaluate to a local value,
   * because constructors can alias 'this' internally.
   */
  @Test(timeout = 4000)
  public void testEvaluatesToLocalValue_newOperatorDefect() {
    Node newNode = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"));
    assertFalse("new Foo() must NOT evaluate to local value",
        NodeUtil.evaluatesToLocalValue(newNode));
  }

  @Test(timeout = 4000)
  public void testEvaluatesToLocalValue_otherConstructs() {
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(123)));
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString("str")));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK))));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));

    Node comma = new Node(Token.COMMA, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertTrue(NodeUtil.evaluatesToLocalValue(comma));

    Node orNode = new Node(Token.OR, Node.newNumber(1), Node.newString("s"));
    assertTrue(NodeUtil.evaluatesToLocalValue(orNode));

    Node hookNode = new Node(Token.HOOK, Node.newString(Token.NAME, "c"), Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.evaluatesToLocalValue(hookNode));

    Node callToString = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString(Token.NAME, "x"), Node.newString("toString")));
    assertTrue(NodeUtil.evaluatesToLocalValue(callToString));

    Node incPost = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    incPost.putBooleanProp(Node.INCRDECR_PROP, false);
    assertTrue(NodeUtil.evaluatesToLocalValue(incPost));
  }

  // =========================================================================
  // Partition D: Structural Transformations & AST Modifications
  // =========================================================================

  @Test(timeout = 4000)
  public void testRemoveChild_statementBlockAndVars() {
    Node block = new Node(Token.BLOCK);
    Node stmt1 = new Node(Token.EMPTY);
    Node stmt2 = new Node(Token.EMPTY);
    block.addChildToBack(stmt1);
    block.addChildToBack(stmt2);

    NodeUtil.removeChild(block, stmt1);
    assertEquals(1, block.getChildCount());
    assertSame(stmt2, block.getFirstChild());

    Node parentBlock = new Node(Token.BLOCK);
    Node varNode = new Node(Token.VAR);
    Node v1 = Node.newString(Token.NAME, "a");
    Node v2 = Node.newString(Token.NAME, "b");
    varNode.addChildToBack(v1);
    varNode.addChildToBack(v2);
    parentBlock.addChildToBack(varNode);

    NodeUtil.removeChild(varNode, v1);
    assertEquals(1, varNode.getChildCount());

    NodeUtil.removeChild(varNode, v2);
    assertEquals(0, parentBlock.getChildCount());
  }

  @Test(timeout = 4000)
  public void testRemoveChild_blockAndLabelAndFor() {
    Node blockToEmpty = new Node(Token.BLOCK, new Node(Token.EMPTY));
    Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "c"), blockToEmpty);
    NodeUtil.removeChild(ifNode, blockToEmpty);
    assertEquals(0, blockToEmpty.getChildCount());

    Node parentBlock = new Node(Token.BLOCK);
    Node labelName = Node.newString(Token.LABEL_NAME, "lbl");
    Node labelTarget = new Node(Token.EMPTY);
    Node labelNode = new Node(Token.LABEL, labelName, labelTarget);
    parentBlock.addChildToBack(labelNode);

    NodeUtil.removeChild(labelNode, labelTarget);
    assertEquals(0, parentBlock.getChildCount());

    Node init = new Node(Token.EMPTY);
    Node cond = Node.newString(Token.NAME, "cond");
    Node inc = new Node(Token.EMPTY);
    Node body = new Node(Token.BLOCK);
    Node for4 = new Node(Token.FOR, init, cond, inc, body);
    NodeUtil.removeChild(for4, cond);
    assertEquals(Token.EMPTY, for4.getFirstChild().getNext().getType());
  }

  @Test(timeout = 4000)
  public void testTryMergeBlock() {
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    Node s1 = new Node(Token.EMPTY);
    Node s2 = new Node(Token.EMPTY);
    block.addChildToBack(s1);
    block.addChildToBack(s2);
    script.addChildToBack(block);

    assertTrue(NodeUtil.tryMergeBlock(block));
    assertEquals(2, script.getChildCount());
    assertSame(s1, script.getFirstChild());
    assertSame(s2, script.getLastChild());

    Node nonStatementParent = new Node(Token.EXPR_RESULT);
    Node orphanBlock = new Node(Token.BLOCK);
    nonStatementParent.addChildToBack(orphanBlock);
    assertFalse(NodeUtil.tryMergeBlock(orphanBlock));
  }

  @Test(timeout = 4000)
  public void testRedeclareVarsInsideBranch() {
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    Node varNode = new Node(Token.VAR, Node.newString(Token.NAME, "localVar"));
    block.addChildToBack(varNode);
    script.addChildToBack(block);

    NodeUtil.redeclareVarsInsideBranch(block);
    assertEquals(2, script.getChildCount());
    assertEquals(Token.VAR, script.getFirstChild().getType());
    assertEquals("localVar", script.getFirstChild().getFirstChild().getString());
  }

  // =========================================================================
  // Partition E: Control Flow, Loops, and Predicate Inspection
  // =========================================================================

  @Test(timeout = 4000)
  public void testLoopStructuresAndConditionExpressions() {
    Node forInit = new Node(Token.EMPTY);
    Node forCond = Node.newString(Token.NAME, "c");
    Node forInc = new Node(Token.EMPTY);
    Node forBody = new Node(Token.BLOCK);
    Node forNode = new Node(Token.FOR, forInit, forCond, forInc, forBody);

    assertTrue(NodeUtil.isLoopStructure(forNode));
    assertSame(forBody, NodeUtil.getLoopCodeBlock(forNode));
    assertSame(forCond, NodeUtil.getConditionExpression(forNode));

    Node whileCond = Node.newString(Token.NAME, "w");
    Node whileBody = new Node(Token.BLOCK);
    Node whileNode = new Node(Token.WHILE, whileCond, whileBody);
    assertTrue(NodeUtil.isLoopStructure(whileNode));
    assertSame(whileBody, NodeUtil.getLoopCodeBlock(whileNode));
    assertSame(whileCond, NodeUtil.getConditionExpression(whileNode));

    Node doBody = new Node(Token.BLOCK);
    Node doCond = Node.newString(Token.NAME, "d");
    Node doNode = new Node(Token.DO, doBody, doCond);
    assertTrue(NodeUtil.isLoopStructure(doNode));
    assertSame(doBody, NodeUtil.getLoopCodeBlock(doNode));
    assertSame(doCond, NodeUtil.getConditionExpression(doNode));

    Node forIn = new Node(Token.FOR, new Node(Token.VAR), Node.newString(Token.NAME, "obj"), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isForIn(forIn));
    assertNull(NodeUtil.getConditionExpression(forIn));

    Node nameInLoop = Node.newString(Token.NAME, "inside");
    whileBody.addChildToBack(nameInLoop);
    assertTrue(NodeUtil.isWithinLoop(nameInLoop));

    Node standalone = Node.newString(Token.NAME, "outside");
    assertFalse(NodeUtil.isWithinLoop(standalone));
  }

  @Test(timeout = 4000)
  public void testControlStructureCodeBlocks() {
    Node ifCond = Node.newString(Token.NAME, "cond");
    Node ifThen = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF, ifCond, ifThen);

    assertTrue(NodeUtil.isControlStructure(ifNode));
    assertFalse(NodeUtil.isControlStructureCodeBlock(ifNode, ifCond));
    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, ifThen));

    Node tryBody = new Node(Token.BLOCK);
    Node catchBlock = new Node(Token.BLOCK);
    Node finallyBlock = new Node(Token.BLOCK);
    Node tryNode = new Node(Token.TRY, tryBody, catchBlock, finallyBlock);

    assertTrue(NodeUtil.isControlStructureCodeBlock(tryNode, tryBody));
    assertTrue(NodeUtil.isControlStructureCodeBlock(tryNode, finallyBlock));
    assertFalse(NodeUtil.isControlStructureCodeBlock(tryNode, catchBlock));
  }

  @Test(timeout = 4000)
  public void testFunctionAndCallPredicates() {
    Node fn = NodeUtil.newFunctionNode("testFn",
        Arrays.asList(Node.newString(Token.NAME, "arg1"), Node.newString(Token.NAME, "arg2")),
        new Node(Token.BLOCK), 1, 0);

    assertTrue(NodeUtil.isFunction(fn));
    assertNotNull(NodeUtil.getFunctionBody(fn));
    assertEquals(2, NodeUtil.getFnParameters(fn).getChildCount());
    assertEquals("arg1", NodeUtil.getArgumentForFunction(fn, 0).getString());
    assertEquals("arg2", NodeUtil.getArgumentForFunction(fn, 1).getString());
    assertNull(NodeUtil.getArgumentForFunction(fn, 2));

    Node callTarget = Node.newString(Token.NAME, "caller");
    Node arg = Node.newNumber(99);
    Node call = NodeUtil.newCallNode(callTarget, arg);
    assertTrue(NodeUtil.isCall(call));
    assertTrue(NodeUtil.isCallOrNew(call));
    assertTrue(call.getBooleanProp(Node.FREE_CALL));
    assertSame(arg, NodeUtil.getArgumentForCallOrNew(call, 0));
    assertNull(NodeUtil.getArgumentForCallOrNew(call, 1));
  }

  @Test(timeout = 4000)
  public void testSideEffectsChecks() {
    assertTrue(NodeUtil.mayHaveSideEffects(new Node(Token.THROW, Node.newString("err"))));

    Node arrayLit = new Node(Token.ARRAYLIT);
    assertFalse(NodeUtil.mayHaveSideEffects(arrayLit));
    assertTrue(NodeUtil.mayEffectMutableState(arrayLit));

    Node newArray = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newArray));

    Node newCustom = new Node(Token.NEW, Node.newString(Token.NAME, "CustomClass"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(newCustom));

    Node callBuiltin = new Node(Token.CALL, Node.newString(Token.NAME, "String"));
    assertFalse(NodeUtil.functionCallHasSideEffects(callBuiltin));

    Node callMath = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString(Token.NAME, "Math"), Node.newString("sin")));
    assertFalse(NodeUtil.functionCallHasSideEffects(callMath));

    Node callRandom = new Node(Token.CALL, Node.newString(Token.NAME, "randomFunc"));
    assertTrue(NodeUtil.functionCallHasSideEffects(callRandom));
  }

  @Test(timeout = 4000)
  public void testPrototypeHelpers() {
    CodingConvention conv = new DefaultCodingConvention();
    Node qName = NodeUtil.newQualifiedNameNode(conv, "MyClass.prototype.myMethod", 1, 0);

    assertTrue(NodeUtil.isPrototypeProperty(qName));
    assertEquals("myMethod", NodeUtil.getPrototypePropertyName(qName));
    assertEquals("MyClass", NodeUtil.getPrototypeClassName(qName).getQualifiedName());

    Node assign = new Node(Token.ASSIGN, qName, Node.newNumber(1));
    Node expr = NodeUtil.newExpr(assign);
    assertTrue(NodeUtil.isPrototypePropertyDeclaration(expr));
  }

  @Test(timeout = 4000)
  public void testOpConversionAndAssignmentHelpers() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("typeof", NodeUtil.opToStr(Token.TYPEOF));
    assertNull(NodeUtil.opToStr(-999));
    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));

    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_SUB)));
    assertEquals(Token.MUL, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MUL)));
  }

  @Test(timeout = 4000)
  public void testTraversalsAndReferenceCounts() {
    Node root = new Node(Token.BLOCK);
    Node n1 = Node.newString(Token.NAME, "x");
    Node n2 = Node.newString(Token.NAME, "y");
    Node n3 = Node.newString(Token.NAME, "x");
    root.addChildToBack(n1);
    root.addChildToBack(n2);
    root.addChildToBack(n3);

    assertTrue(NodeUtil.isNameReferenced(root, "x"));
    assertFalse(NodeUtil.isNameReferenced(root, "z"));
    assertEquals(2, NodeUtil.getNameReferenceCount(root, "x"));
    assertEquals(1, NodeUtil.getNameReferenceCount(root, "y"));
    assertEquals(3, NodeUtil.getNodeTypeReferenceCount(root, Token.NAME, Predicates.<Node>alwaysTrue()));

    final int[] visitCount = new int[]{0};
    NodeUtil.visitPreOrder(root, new NodeUtil.Visitor() {
      public void visit(Node node) {
        visitCount[0]++;
      }
    }, Predicates.<Node>alwaysTrue());
    assertEquals(4, visitCount[0]);
  }

  // =========================================================================
  // Partition F: Exception Paths & Defensive Guard Paths
  // =========================================================================

  @Test(expected = Error.class, timeout = 4000)
  public void testPrecedence_unknownTokenThrows() {
    NodeUtil.precedence(-123);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testOpToStrNoFail_unknownTokenThrows() {
    NodeUtil.opToStrNoFail(-123);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetOpFromAssignmentOp_nonAssignThrows() {
    NodeUtil.getOpFromAssignmentOp(new Node(Token.ADD));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testRemoveChild_invalidTargetThrows() {
    Node expr = new Node(Token.EXPR_RESULT);
    Node num = Node.newNumber(1);
    expr.addChildToBack(num);
    NodeUtil.removeChild(expr, num);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testConstructorCallHasSideEffects_nonNewThrows() {
    Node call = new Node(Token.CALL);
    NodeUtil.constructorCallHasSideEffects(call);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testFunctionCallHasSideEffects_nonCallThrows() {
    Node newNode = new Node(Token.NEW);
    NodeUtil.functionCallHasSideEffects(newNode);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetConditionExpression_nonConditionalThrows() {
    Node retNode = new Node(Token.RETURN);
    NodeUtil.getConditionExpression(retNode);
  }
}