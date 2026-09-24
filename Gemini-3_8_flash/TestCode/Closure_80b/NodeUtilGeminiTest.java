package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.common.base.Predicates;
import com.google.javascript.rhino.JSDocInfo;
import com.google.javascript.rhino.JSDocInfoBuilder;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/*
 * [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: NodeUtil.java (Defects4J Closure Compiler)
 *
 * Targeted Defects:
 * 1. com.google.javascript.jscomp.NodeUtilTest::testIsBooleanResult
 *    - Cause: Token.DELPROP missing in isBooleanResultHelper() despite the comment
 *      "// delete operator returns a boolean."
 *    - Impact: isBooleanResult returns false for DELPROP nodes instead of true.
 *
 * 2. com.google.javascript.jscomp.NodeUtilTest::testLocalValue1
 *    - Cause: Token.DELPROP not handled in evaluatesToLocalValue() or isSimpleOperatorType().
 *    - Impact: evaluatesToLocalValue() falls through to default and throws
 *      IllegalStateException: "Unexpected expression nodeDELPROP ...".
 *
 * Coverage Target Areas:
 * - Partition A: Core Functional Logic & State Transitions (Boolean/Number/String evaluation,
 *   literal checks, side-effect checks, qualified names, AST traversal, operator conversions).
 * - Partition B: Boundary Value Analysis (Empty arrays, whitespace strings, hex numbers,
 *   infinity handling, empty blocks, recursion limits).
 * - Partition C: Defect-Targeted Branch Zone (DELPROP boolean result and DELPROP local value).
 * - Partition D: Exception & Defensive Guard Paths (Precondition failures, invalid node types,
 *   unsupported AST modifications).
 * - Partition E: Object Lifecycle & Contract Integrity (Private constructor reflection check).
 * -----------------------------------------------------------------------------------------
 */
public class NodeUtilGeminiTest {

  private final CodingConvention convention = new DefaultCodingConvention();

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testDelpropIsBooleanResult() {
    // Tests that DELPROP (delete expr) is correctly identified as yielding a boolean result.
    Node delpropNode = new Node(Token.DELPROP, Node.newString(Token.NAME, "foo"));
    assertTrue("delete operator (DELPROP) must be recognized as a boolean result",
        NodeUtil.isBooleanResult(delpropNode));
  }

  @Test(timeout = 4000)
  public void testDelpropEvaluatesToLocalValue() {
    // Tests that DELPROP evaluates to a local value (primitive boolean) without throwing IllegalStateException.
    Node delpropNode = new Node(Token.DELPROP, Node.newString(Token.NAME, "foo"));
    assertTrue("delete operator (DELPROP) must evaluate to a local value",
        NodeUtil.evaluatesToLocalValue(delpropNode));
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetBooleanValueLiterals() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString("non-empty")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString("")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newNumber(1.0)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newNumber(-0.5)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newNumber(0.0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.NULL)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(new Node(Token.VOID, Node.newNumber(0))));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.ARRAYLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.OBJECTLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(new Node(Token.REGEXP)));

    // NOT literal
    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(notTrue));

    // Known names
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "NaN")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getBooleanValue(Node.newString(Token.NAME, "unknownName")));
  }

  @Test(timeout = 4000)
  public void testGetExpressionBooleanValue() {
    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(assign));

    Node comma = new Node(Token.COMMA, new Node(Token.FALSE), new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(comma));

    Node andNode = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.FALSE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getExpressionBooleanValue(andNode));

    Node orNode = new Node(Token.OR, new Node(Token.TRUE), new Node(Token.FALSE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(orNode));

    Node hookEqual = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), new Node(Token.TRUE), new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getExpressionBooleanValue(hookEqual));

    Node hookDifferent = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), new Node(Token.TRUE), new Node(Token.FALSE));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getExpressionBooleanValue(hookDifferent));
  }

  @Test(timeout = 4000)
  public void testGetStringValue() {
    assertEquals("hello", NodeUtil.getStringValue(Node.newString("hello")));
    assertEquals("undefined", NodeUtil.getStringValue(Node.newString(Token.NAME, "undefined")));
    assertEquals("Infinity", NodeUtil.getStringValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals("NaN", NodeUtil.getStringValue(Node.newString(Token.NAME, "NaN")));
    assertNull(NodeUtil.getStringValue(Node.newString(Token.NAME, "customVar")));

    assertEquals("1", NodeUtil.getStringValue(Node.newNumber(1.0)));
    assertEquals("1.5", NodeUtil.getStringValue(Node.newNumber(1.5)));

    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID)));

    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    assertEquals("false", NodeUtil.getStringValue(notTrue));

    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    assertEquals("true", NodeUtil.getStringValue(notFalse));

    assertEquals("[object Object]", NodeUtil.getStringValue(new Node(Token.OBJECTLIT)));
    assertNull(NodeUtil.getStringValue(new Node(Token.EMPTY)));
  }

  @Test(timeout = 4000)
  public void testArrayToStringWithAndWithoutHoles() {
    Node array = new Node(Token.ARRAYLIT);
    assertEquals("", NodeUtil.getStringValue(array));

    array.addChildToBack(Node.newString("a"));
    array.addChildToBack(new Node(Token.NULL));
    array.addChildToBack(Node.newString(Token.NAME, "undefined"));
    array.addChildToBack(Node.newNumber(2.0));
    assertEquals("a,,,2", NodeUtil.getStringValue(array));

    // Sparse array with SKIP_INDEXES_PROP
    Node sparseArray = new Node(Token.ARRAYLIT);
    sparseArray.putProp(Node.SKIP_INDEXES_PROP, new int[]{1});
    sparseArray.addChildToBack(Node.newString("x"));
    sparseArray.addChildToBack(Node.newString("y"));
    assertEquals("x,,y", NodeUtil.getStringValue(sparseArray));
  }

  @Test(timeout = 4000)
  public void testGetNumberValue() {
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(new Node(Token.TRUE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.FALSE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.NULL)));
    assertEquals(Double.valueOf(42.5), NodeUtil.getNumberValue(Node.newNumber(42.5)));

    Node voidWithoutSideEffects = new Node(Token.VOID, Node.newNumber(0));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(voidWithoutSideEffects)));

    Node voidWithSideEffects = new Node(Token.VOID, new Node(Token.CALL, Node.newString(Token.NAME, "f")));
    assertNull(NodeUtil.getNumberValue(voidWithSideEffects));

    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "undefined"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "NaN"))));
    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getNumberValue(Node.newString(Token.NAME, "Infinity")));
    assertNull(NodeUtil.getNumberValue(Node.newString(Token.NAME, "other")));

    Node negInfinity = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
    assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), NodeUtil.getNumberValue(negInfinity));

    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(notTrue));

    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(notFalse));

    assertEquals(Double.valueOf(123.0), NodeUtil.getNumberValue(Node.newString("123")));
  }

  @Test(timeout = 4000)
  public void testGetFunctionNameAndNearest() {
    // 1. function foo() {}
    Node fn1 = NodeUtil.newFunctionNode("foo", Collections.<Node>emptyList(), new Node(Token.BLOCK), 0, 0);
    fn1.setParent(new Node(Token.BLOCK));
    assertEquals("foo", NodeUtil.getFunctionName(fn1));
    assertEquals("foo", NodeUtil.getNearestFunctionName(fn1));

    // 2. var bar = function() {}
    Node fn2 = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 0, 0);
    Node varName = Node.newString(Token.NAME, "bar");
    varName.addChildToBack(fn2);
    Node varNode = new Node(Token.VAR, varName);
    assertEquals("bar", NodeUtil.getFunctionName(fn2));
    assertEquals("bar", NodeUtil.getNearestFunctionName(fn2));

    // 3. qualified.prop = function() {}
    Node fn3 = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 0, 0);
    Node getprop = new Node(Token.GETPROP, Node.newString(Token.NAME, "qualified"), Node.newString(Token.STRING, "prop"));
    Node assign = new Node(Token.ASSIGN, getprop, fn3);
    assertEquals("qualified.prop", NodeUtil.getFunctionName(fn3));
    assertEquals("qualified.prop", NodeUtil.getNearestFunctionName(fn3));

    // 4. Object literal string key: {'keyStr': function() {}}
    Node fn4 = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 0, 0);
    Node strKey = Node.newString(Token.STRING, "keyStr");
    strKey.addChildToBack(fn4);
    new Node(Token.OBJECTLIT, strKey);
    assertNull(NodeUtil.getFunctionName(fn4));
    assertEquals("keyStr", NodeUtil.getNearestFunctionName(fn4));

    // 5. Object literal numeric key: {12: function() {}}
    Node fn5 = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 0, 0);
    Node numKey = Node.newNumber(12.0);
    numKey.addChildToBack(fn5);
    new Node(Token.OBJECTLIT, numKey);
    assertEquals("12", NodeUtil.getNearestFunctionName(fn5));
  }

  @Test(timeout = 4000)
  public void testIsImmutableAndLiteralValue() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString("str")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(1.0)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NOT, new Node(Token.TRUE))));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID, Node.newNumber(0))));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newNumber(1.0))));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "foo")));

    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"));
    assertTrue(NodeUtil.isLiteralValue(arrayLit, false));

    Node impureArray = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "x"));
    assertFalse(NodeUtil.isLiteralValue(impureArray, false));

    Node objLit = new Node(Token.OBJECTLIT);
    Node key = Node.newString(Token.STRING, "k");
    key.addChildToBack(Node.newNumber(1));
    objLit.addChildToBack(key);
    assertTrue(NodeUtil.isLiteralValue(objLit, false));

    Node fnExpr = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 0, 0);
    new Node(Token.EXPR_RESULT, fnExpr);
    assertTrue(NodeUtil.isLiteralValue(fnExpr, true));
    assertFalse(NodeUtil.isLiteralValue(fnExpr, false));
  }

  @Test(timeout = 4000)
  public void testIsValidDefineValue() {
    Set<String> defines = new HashSet<>(Arrays.asList("DEF_A", "DEF_B", "pkg.DEF_C"));

    assertTrue(NodeUtil.isValidDefineValue(Node.newString("val"), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(42), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));

    Node binaryAdd = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isValidDefineValue(binaryAdd, defines));

    Node unaryNeg = new Node(Token.NEG, Node.newNumber(5));
    assertTrue(NodeUtil.isValidDefineValue(unaryNeg, defines));

    Node defName = Node.newString(Token.NAME, "DEF_A");
    assertTrue(NodeUtil.isValidDefineValue(defName, defines));

    Node nonDefName = Node.newString(Token.NAME, "NON_DEF");
    assertFalse(NodeUtil.isValidDefineValue(nonDefName, defines));

    Node qualifiedDef = NodeUtil.newQualifiedNameNode(convention, "pkg.DEF_C", 0, 0);
    assertTrue(NodeUtil.isValidDefineValue(qualifiedDef, defines));
  }

  @Test(timeout = 4000)
  public void testSideEffectsAnalysis() {
    Node num = Node.newNumber(1);
    assertFalse(NodeUtil.mayHaveSideEffects(num));
    assertFalse(NodeUtil.mayEffectMutableState(num));

    Node throwNode = new Node(Token.THROW, Node.newString("err"));
    assertTrue(NodeUtil.mayHaveSideEffects(throwNode));

    Node newArray = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newArray));

    Node newCustom = new Node(Token.NEW, Node.newString(Token.NAME, "CustomClass"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(newCustom));

    Node mathCall = NodeUtil.newCallNode(new Node(Token.GETPROP, Node.newString(Token.NAME, "Math"), Node.newString(Token.STRING, "sin")));
    assertFalse(NodeUtil.functionCallHasSideEffects(mathCall));

    Node customCall = NodeUtil.newCallNode(Node.newString(Token.NAME, "myFunc"));
    assertTrue(NodeUtil.functionCallHasSideEffects(customCall));

    Node noSideEffectCall = NodeUtil.newCallNode(Node.newString(Token.NAME, "myFunc"));
    noSideEffectCall.setSideEffectFlags(Node.NO_SIDE_EFFECTS);
    assertFalse(NodeUtil.functionCallHasSideEffects(noSideEffectCall));
  }

  @Test(timeout = 4000)
  public void testPrecedenceAndOperators() {
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
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
    assertEquals(15, NodeUtil.precedence(Token.NAME));

    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertFalse(NodeUtil.isAssociative(Token.ADD));

    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertFalse(NodeUtil.isCommutative(Token.SUB));
    assertFalse(NodeUtil.isCommutative(Token.ADD));

    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertFalse(NodeUtil.isAssignmentOp(new Node(Token.ADD)));

    assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_SUB)));
    assertEquals(Token.MUL, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MUL)));
    assertEquals(Token.DIV, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_DIV)));
    assertEquals(Token.MOD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_MOD)));

    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("void", NodeUtil.opToStr(Token.VOID));
    assertNull(NodeUtil.opToStr(Token.FUNCTION));
    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  @Test(timeout = 4000)
  public void testNumericAndBooleanResultPredicates() {
    assertTrue(NodeUtil.isNumericResult(Node.newNumber(1.0)));
    assertTrue(NodeUtil.isNumericResult(new Node(Token.SUB, Node.newNumber(2), Node.newNumber(1))));
    assertTrue(NodeUtil.isNumericResult(new Node(Token.BITNOT, Node.newNumber(1))));
    assertTrue(NodeUtil.isNumericResult(Node.newString(Token.NAME, "NaN")));
    assertTrue(NodeUtil.isNumericResult(Node.newString(Token.NAME, "Infinity")));
    assertFalse(NodeUtil.isNumericResult(Node.newString("str")));

    Node numericAdd = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isNumericResult(numericAdd));

    Node stringAdd = new Node(Token.ADD, Node.newString("a"), Node.newNumber(2));
    assertFalse(NodeUtil.isNumericResult(stringAdd));

    assertTrue(NodeUtil.isBooleanResult(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isBooleanResult(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isBooleanResult(new Node(Token.EQ, Node.newNumber(1), Node.newNumber(2))));
    assertTrue(NodeUtil.isBooleanResult(new Node(Token.NOT, Node.newNumber(1))));
    assertTrue(NodeUtil.isBooleanResult(new Node(Token.INSTANCEOF, Node.newString(Token.NAME, "x"), Node.newString(Token.NAME, "y"))));
    assertFalse(NodeUtil.isBooleanResult(Node.newNumber(1.0)));
  }

  @Test(timeout = 4000)
  public void testControlStructuresAndConditionExpressions() {
    Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "cond"), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isControlStructure(ifNode));
    assertEquals("cond", NodeUtil.getConditionExpression(ifNode).getString());
    assertFalse(NodeUtil.isControlStructureCodeBlock(ifNode, ifNode.getFirstChild()));
    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, ifNode.getLastChild()));

    Node whileNode = new Node(Token.WHILE, Node.newString(Token.NAME, "wCond"), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isLoopStructure(whileNode));
    assertEquals(whileNode.getLastChild(), NodeUtil.getLoopCodeBlock(whileNode));
    assertEquals("wCond", NodeUtil.getConditionExpression(whileNode).getString());

    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), Node.newString(Token.NAME, "dCond"));
    assertTrue(NodeUtil.isLoopStructure(doNode));
    assertEquals(doNode.getFirstChild(), NodeUtil.getLoopCodeBlock(doNode));
    assertEquals("dCond", NodeUtil.getConditionExpression(doNode).getString());

    Node forNode = new Node(Token.FOR, new Node(Token.EMPTY), Node.newString(Token.NAME, "fCond"), new Node(Token.EMPTY), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isLoopStructure(forNode));
    assertEquals("fCond", NodeUtil.getConditionExpression(forNode).getString());

    Node forInNode = new Node(Token.FOR, new Node(Token.NAME), new Node(Token.NAME), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isForIn(forInNode));
    assertNull(NodeUtil.getConditionExpression(forInNode));
  }

  @Test(timeout = 4000)
  public void testRemoveChildAndTryMergeBlock() {
    // 1. Remove statement from statement block
    Node block = new Node(Token.BLOCK);
    Node stmt1 = NodeUtil.newExpr(Node.newNumber(1));
    Node stmt2 = NodeUtil.newExpr(Node.newNumber(2));
    block.addChildToBack(stmt1);
    block.addChildToBack(stmt2);
    NodeUtil.removeChild(block, stmt1);
    assertEquals(1, block.getChildCount());
    assertEquals(stmt2, block.getFirstChild());

    // 2. Remove from VAR with multiple children
    Node varNode = new Node(Token.VAR);
    Node v1 = Node.newString(Token.NAME, "a");
    Node v2 = Node.newString(Token.NAME, "b");
    varNode.addChildToBack(v1);
    varNode.addChildToBack(v2);
    block.addChildToBack(varNode);
    NodeUtil.removeChild(varNode, v1);
    assertEquals(1, varNode.getChildCount());

    // 3. Remove single variable declaration removes VAR from block
    NodeUtil.removeChild(varNode, v2);
    assertFalse(block.hasChildren());

    // 4. Try merge block
    Node parentBlock = new Node(Token.BLOCK);
    Node childBlock = new Node(Token.BLOCK);
    childBlock.addChildToBack(NodeUtil.newExpr(Node.newNumber(10)));
    parentBlock.addChildToBack(childBlock);
    assertTrue(NodeUtil.tryMergeBlock(childBlock));
    assertEquals(1, parentBlock.getChildCount());
    assertEquals(Token.EXPR_RESULT, parentBlock.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testQualifiedNamesAndPrototypes() {
    Node qname = NodeUtil.newQualifiedNameNode(convention, "a.b.c", 1, 2);
    assertEquals(Token.GETPROP, qname.getType());
    assertEquals("a.b.c", qname.getQualifiedName());

    Node root = NodeUtil.getRootOfQualifiedName(qname);
    assertEquals(Token.NAME, root.getType());
    assertEquals("a", root.getString());

    Node protoQName = NodeUtil.newQualifiedNameNode(convention, "MyClass.prototype.myMethod", 0, 0);
    assertTrue(NodeUtil.isPrototypeProperty(protoQName));
    assertEquals("MyClass", NodeUtil.getPrototypeClassName(protoQName).getString());
    assertEquals("myMethod", NodeUtil.getPrototypePropertyName(protoQName));

    Node exprAssign = NodeUtil.newExpr(new Node(Token.ASSIGN, protoQName, Node.newNumber(1)));
    assertTrue(NodeUtil.isPrototypePropertyDeclaration(exprAssign));
  }

  @Test(timeout = 4000)
  public void testTreeTraversalAndCounters() {
    Node root = new Node(Token.BLOCK);
    root.addChildToBack(Node.newString(Token.NAME, "target"));
    root.addChildToBack(Node.newNumber(1));
    Node inner = new Node(Token.EXPR_RESULT);
    inner.addChildToBack(Node.newString(Token.NAME, "target"));
    root.addChildToBack(inner);

    assertTrue(NodeUtil.isNameReferenced(root, "target"));
    assertFalse(NodeUtil.isNameReferenced(root, "notFound"));
    assertEquals(2, NodeUtil.getNameReferenceCount(root, "target"));
    assertEquals(2, NodeUtil.getNodeTypeReferenceCount(root, Token.NAME, Predicates.<Node>alwaysTrue()));

    List<Integer> visited = new ArrayList<>();
    NodeUtil.visitPreOrder(root, node -> visited.add(node.getType()), Predicates.<Node>alwaysTrue());
    assertTrue(visited.contains(Token.BLOCK));
    assertTrue(visited.contains(Token.NAME));
    assertTrue(visited.contains(Token.NUMBER));
  }

  @Test(timeout = 4000)
  public void testRedeclareVarsInsideBranch() {
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    script.addChildToBack(block);

    Node var = NodeUtil.newVarNode("declaredVar", Node.newNumber(1));
    block.addChildToBack(var);

    NodeUtil.redeclareVarsInsideBranch(block);
    // Redeclared var should be inserted into SCRIPT
    assertEquals(2, script.getChildCount());
    assertEquals(Token.VAR, script.getFirstChild().getType());
    assertEquals("declaredVar", script.getFirstChild().getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testFunctionMethodsAndParameters() {
    List<Node> params = Arrays.asList(Node.newString(Token.NAME, "p1"), Node.newString(Token.NAME, "p2"));
    Node fn = NodeUtil.newFunctionNode("testFn", params, new Node(Token.BLOCK), 1, 0);

    assertEquals("p1", NodeUtil.getArgumentForFunction(fn, 0).getString());
    assertEquals("p2", NodeUtil.getArgumentForFunction(fn, 1).getString());
    assertNull(NodeUtil.getArgumentForFunction(fn, 2));

    Node call = NodeUtil.newCallNode(Node.newString(Token.NAME, "fn"), Node.newNumber(100), Node.newNumber(200));
    assertEquals(100.0, NodeUtil.getArgumentForCallOrNew(call, 0).getDouble(), 0.0);
    assertEquals(200.0, NodeUtil.getArgumentForCallOrNew(call, 1).getDouble(), 0.0);
    assertNull(NodeUtil.getArgumentForCallOrNew(call, 2));

    // VarArgs function test
    Node fnBody = fn.getLastChild();
    fnBody.addChildToBack(Node.newString(Token.NAME, "arguments"));
    assertTrue(NodeUtil.isVarArgsFunction(fn));
  }

  @Test(timeout = 4000)
  public void testJSDocInfoRetrieval() {
    JSDocInfo info = new JSDocInfoBuilder(false).build(null);
    Node nameNode = Node.newString(Token.NAME, "myVar");
    nameNode.setJSDocInfo(info);
    assertSame(info, NodeUtil.getInfoForNameNode(nameNode));

    Node varParent = new Node(Token.VAR, Node.newString(Token.NAME, "v"));
    varParent.setJSDocInfo(info);
    assertSame(info, NodeUtil.getInfoForNameNode(varParent.getFirstChild()));

    Node fn = NodeUtil.newFunctionNode("fnWithDoc", Collections.<Node>emptyList(), new Node(Token.BLOCK), 0, 0);
    fn.setJSDocInfo(info);
    assertSame(info, NodeUtil.getFunctionInfo(fn));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetStringNumberValueBoundaries() {
    assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue(""));
    assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue("   \t\n\r  "));
    assertEquals(Double.valueOf(16.0), NodeUtil.getStringNumberValue("0x10"));
    assertEquals(Double.valueOf(255.0), NodeUtil.getStringNumberValue("0XFF"));
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("0xGG"))); // invalid hex

    assertNull(NodeUtil.getStringNumberValue("+0x10"));
    assertNull(NodeUtil.getStringNumberValue("-0x10"));
    assertNull(NodeUtil.getStringNumberValue("infinity"));
    assertNull(NodeUtil.getStringNumberValue("-infinity"));
    assertNull(NodeUtil.getStringNumberValue("+infinity"));

    assertEquals(Double.valueOf(123.45), NodeUtil.getStringNumberValue("  123.45  "));
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("not_a_num")));
  }

  @Test(timeout = 4000)
  public void testTrimJsWhiteSpaceCharacters() {
    String allWs = " \n\r\t\u00A0\u000C\u000B\u2028\u2029\uFEFF";
    assertEquals("middle", NodeUtil.trimJsWhiteSpace(allWs + "middle" + allWs));
    assertEquals("", NodeUtil.trimJsWhiteSpace(allWs));
    assertEquals("", NodeUtil.trimJsWhiteSpace(""));
  }

  @Test(timeout = 4000)
  public void testIsLatinAndValidPropertyName() {
    assertTrue(NodeUtil.isLatin("asciiOnly123_$"));
    assertFalse(NodeUtil.isLatin("nonAscii\u0100"));
    assertTrue(NodeUtil.isValidPropertyName("validPropName"));
    assertFalse(NodeUtil.isValidPropertyName("class")); // keyword
    assertFalse(NodeUtil.isValidPropertyName("123invalidStart"));
    assertFalse(NodeUtil.isValidPropertyName("prop\u0100")); // non-latin
  }

  @Test(timeout = 4000)
  public void testEmptyBlockAndUndefinedNode() {
    Node emptyBlock = new Node(Token.BLOCK);
    assertTrue(NodeUtil.isEmptyBlock(emptyBlock));

    Node blockWithEmpty = new Node(Token.BLOCK, new Node(Token.EMPTY));
    assertTrue(NodeUtil.isEmptyBlock(blockWithEmpty));

    Node blockWithStmt = new Node(Token.BLOCK, NodeUtil.newExpr(Node.newNumber(1)));
    assertFalse(NodeUtil.isEmptyBlock(blockWithStmt));

    Node undef = NodeUtil.newUndefinedNode(null);
    assertEquals(Token.VOID, undef.getType());
    assertEquals(0.0, undef.getFirstChild().getDouble(), 0.0);
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testConstructorCallHasSideEffectsThrowsOnNonNew() {
    NodeUtil.constructorCallHasSideEffects(new Node(Token.CALL));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testFunctionCallHasSideEffectsThrowsOnNonCall() {
    NodeUtil.functionCallHasSideEffects(new Node(Token.NEW));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCallHasLocalResultThrowsOnNonCall() {
    NodeUtil.callHasLocalResult(new Node(Token.NAME));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNewHasLocalResultThrowsOnNonNew() {
    NodeUtil.newHasLocalResult(new Node(Token.CALL));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testHasFinallyThrowsOnNonTry() {
    NodeUtil.hasFinally(new Node(Token.BLOCK));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetCatchBlockThrowsOnNonTry() {
    NodeUtil.getCatchBlock(new Node(Token.BLOCK));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetFnParametersThrowsOnNonFunction() {
    NodeUtil.getFnParameters(new Node(Token.BLOCK));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetOpFromAssignmentOpThrowsOnNonAssignOp() {
    NodeUtil.getOpFromAssignmentOp(new Node(Token.ADD));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testPrecedenceThrowsOnUnknown() {
    NodeUtil.precedence(Token.SCRIPT);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testOpToStrNoFailThrowsOnInvalid() {
    NodeUtil.opToStrNoFail(Token.FUNCTION);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testRemoveChildThrowsOnInvalidNode() {
    Node parent = Node.newNumber(1);
    Node child = Node.newNumber(2);
    parent.addChildToBack(child);
    NodeUtil.removeChild(parent, child);
  }

  // =========================================================================
  // Partition E: Object Lifecycle & Contract Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testPrivateConstructorViaReflection() throws Exception {
    Constructor<NodeUtil> constructor = NodeUtil.class.getDeclaredConstructor();
    constructor.setAccessible(true);
    NodeUtil instance = constructor.newInstance();
    assertNotNull(instance);
  }
}