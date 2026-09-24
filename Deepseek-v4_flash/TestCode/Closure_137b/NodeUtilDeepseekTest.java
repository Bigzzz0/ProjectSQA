package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted branch/defect zones:
 *  - NodeUtil.isStatement: CASE and DEFAULT parents must be treated as statement
 *    containers. The original implementation only handled SCRIPT, BLOCK, and LABEL.
 *  - NodeUtil.removeChild: removing a statement child from a CASE node should be
 *    valid; the original implementation threw IllegalStateException.
 *  - NodeUtil.isVarArgsFunction: a "var arguments;" declaration is not a reference
 *    to the implicit arguments object; the original implementation counted it.
 *  - Function declaration classification in switch cases: a function child of a
 *    CASE node is a declaration, not an anonymous function expression.
 *
 * The tests below also exercise literal conversion, string conversion, operator
 * properties, side-effect analysis, traversal helpers, and AST utility methods.
 */
public class NodeUtilDeepseekTest {

  @Test(timeout = 4000)
  public void testGetBooleanValue() {
    assertTrue(NodeUtil.getBooleanValue(Node.newString(Token.STRING, "a")));
    assertFalse(NodeUtil.getBooleanValue(Node.newString(Token.STRING, "")));
    assertFalse(NodeUtil.getBooleanValue(Node.newNumber(0)));
    assertTrue(NodeUtil.getBooleanValue(Node.newNumber(1)));
    assertTrue(NodeUtil.getBooleanValue(Node.newNumber(-0.5)));
    assertFalse(NodeUtil.getBooleanValue(new Node(Token.NULL)));
    assertFalse(NodeUtil.getBooleanValue(new Node(Token.FALSE)));
    assertFalse(NodeUtil.getBooleanValue(new Node(Token.VOID)));
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.OBJECTLIT)));
    assertTrue(NodeUtil.getBooleanValue(new Node(Token.REGEXP)));
    assertFalse(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertFalse(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "NaN")));
    assertTrue(NodeUtil.getBooleanValue(Node.newString(Token.NAME, "Infinity")));
  }

  @Test(timeout = 4000)
  public void testGetStringValue() {
    assertEquals("foo", NodeUtil.getStringValue(Node.newString(Token.STRING, "foo")));
    assertEquals("foo", NodeUtil.getStringValue(Node.newString(Token.NAME, "foo")));
    assertEquals("1", NodeUtil.getStringValue(Node.newNumber(1.0)));
    assertEquals("1.5", NodeUtil.getStringValue(Node.newNumber(1.5)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID)));
    assertNull(NodeUtil.getStringValue(new Node(Token.BLOCK)));
  }

  @Test(timeout = 4000)
  public void testGetFunctionName() {
    Node fn = fn("g");
    Node parentName = Node.newString(Token.NAME, "f");
    parentName.addChildToBack(fn);
    assertEquals("f", NodeUtil.getFunctionName(fn, parentName));

    Node fn2 = fn("g");
    Node assign = new Node(Token.ASSIGN);
    assign.addChildToBack(NodeUtil.newQualifiedNameNode("a.b", 0, 0));
    assign.addChildToBack(fn2);
    assertEquals("a.b", NodeUtil.getFunctionName(fn2, assign));

    Node script = new Node(Token.SCRIPT);
    Node fn3 = fn("named");
    script.addChildToBack(fn3);
    assertEquals("named", NodeUtil.getFunctionName(fn3, script));

    Node fn4 = fn("");
    script.addChildToBack(fn4);
    assertNull(NodeUtil.getFunctionName(fn4, script));
  }

  @Test(timeout = 4000)
  public void testIsImmutableAndLiteralValue() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.STRING, "x")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(1)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID)));
    assertFalse(NodeUtil.isImmutableValue(new Node(Token.BLOCK)));

    Node neg = new Node(Token.NEG);
    neg.addChildToBack(Node.newNumber(5));
    assertTrue(NodeUtil.isImmutableValue(neg));

    Node array = new Node(Token.ARRAYLIT);
    array.addChildToBack(Node.newNumber(1));
    array.addChildToBack(Node.newString(Token.STRING, "x"));
    assertTrue(NodeUtil.isLiteralValue(array));

    array.addChildToBack(new Node(Token.CALL));
    assertFalse(NodeUtil.isLiteralValue(array));
  }

  @Test(timeout = 4000)
  public void testIsValidDefineValue() {
    java.util.Set<String> defines = Collections.singleton("FOO");
    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.STRING, "x"), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(1), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));

    Node neg = new Node(Token.NEG);
    neg.addChildToBack(Node.newNumber(1));
    assertTrue(NodeUtil.isValidDefineValue(neg, defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "FOO"), defines));
    assertFalse(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "BAR"), defines));
    assertTrue(NodeUtil.isValidDefineValue(NodeUtil.newQualifiedNameNode("FOO.bar", 0, 0), defines));
  }

  @Test(timeout = 4000)
  public void testIsEmptyBlockAndNewExpr() {
    Node block = new Node(Token.BLOCK);
    assertTrue(NodeUtil.isEmptyBlock(block));
    block.addChildToBack(new Node(Token.EMPTY));
    assertTrue(NodeUtil.isEmptyBlock(block));
    block.addChildToBack(new Node(Token.BLOCK));
    assertFalse(NodeUtil.isEmptyBlock(block));

    Node expr = NodeUtil.newExpr(Node.newNumber(1));
    assertEquals(Token.EXPR_RESULT, expr.getType());
    assertEquals(Token.NUMBER, expr.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testPrecedenceAndOperators() {
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(11, NodeUtil.precedence(Token.ADD));
    assertEquals(15, NodeUtil.precedence(Token.CALL));

    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertFalse(NodeUtil.isAssociative(Token.ADD));

    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertFalse(NodeUtil.isAssignmentOp(new Node(Token.ADD)));
    assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(new Node(Token.ASSIGN_ADD)));

    try {
      NodeUtil.getOpFromAssignmentOp(new Node(Token.ADD));
      fail("Expected IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
      // expected
    }
  }

  @Test(timeout = 4000)
  public void testSideEffects() {
    assertFalse(NodeUtil.mayHaveSideEffects(Node.newNumber(1)));
    assertTrue(NodeUtil.mayHaveSideEffects(new Node(Token.THROW)));
    assertTrue(NodeUtil.mayHaveSideEffects(new Node(Token.CALL)));
    assertFalse(NodeUtil.mayEffectMutableState(Node.newNumber(1)));
    assertTrue(NodeUtil.mayEffectMutableState(new Node(Token.OBJECTLIT)));
    assertTrue(NodeUtil.mayEffectMutableState(new Node(Token.CALL)));
  }

  @Test(timeout = 4000)
  public void testConstructorAndFunctionCallSideEffects() {
    Node newArray = new Node(Token.NEW);
    newArray.addChildToBack(Node.newString(Token.NAME, "Array"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newArray));

    Node newFoo = new Node(Token.NEW);
    newFoo.addChildToBack(Node.newString(Token.NAME, "Foo"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(newFoo));

    Node callString = new Node(Token.CALL);
    callString.addChildToBack(Node.newString(Token.NAME, "String"));
    assertFalse(NodeUtil.functionCallHasSideEffects(callString));

    Node callFoo = new Node(Token.CALL);
    callFoo.addChildToBack(Node.newString(Token.NAME, "foo"));
    assertTrue(NodeUtil.functionCallHasSideEffects(callFoo));

    Node callMath = new Node(Token.CALL);
    callMath.addChildToBack(NodeUtil.newQualifiedNameNode("Math.abs", 0, 0));
    assertFalse(NodeUtil.functionCallHasSideEffects(callMath));
  }

  @Test(timeout = 4000)
  public void testNodeTypeMayHaveSideEffectsAndCanBeSideEffected() {
    assertFalse(NodeUtil.nodeTypeMayHaveSideEffects(Node.newNumber(1)));
    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.CALL)));
    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.NEW)));
    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(new Node(Token.THROW)));

    Node nameWithChild = Node.newString(Token.NAME, "x");
    nameWithChild.addChildToBack(Node.newNumber(1));
    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(nameWithChild));

    Node assign = new Node(Token.ASSIGN);
    assign.addChildToBack(Node.newString(Token.NAME, "x"));
    assign.addChildToBack(Node.newNumber(1));
    assertTrue(NodeUtil.nodeTypeMayHaveSideEffects(assign));

    assertFalse(NodeUtil.canBeSideEffected(Node.newNumber(1)));
    assertTrue(NodeUtil.canBeSideEffected(new Node(Token.CALL)));
    assertTrue(NodeUtil.canBeSideEffected(Node.newString(Token.NAME, "x")));
    assertTrue(NodeUtil.canBeSideEffected(NodeUtil.newQualifiedNameNode("a.b", 0, 0)));
    assertFalse(NodeUtil.canBeSideEffected(new Node(Token.BLOCK)));
  }

  @Test(timeout = 4000)
  public void testExpressionAndAssignPredicates() {
    assertTrue(NodeUtil.isExpressionNode(NodeUtil.newExpr(Node.newNumber(1))));
    assertFalse(NodeUtil.isExpressionNode(new Node(Token.BLOCK)));

    Node assign = new Node(Token.ASSIGN);
    assign.addChildToBack(Node.newString(Token.NAME, "x"));
    assign.addChildToBack(Node.newNumber(1));
    assertTrue(NodeUtil.isAssign(assign));
    assertTrue(NodeUtil.isExprAssign(NodeUtil.newExpr(assign)));
    assertEquals(Token.NUMBER, NodeUtil.getAssignedValue(assign.getFirstChild()).getType());

    Node var = NodeUtil.newVarNode("x", Node.newNumber(2));
    assertTrue(NodeUtil.isVar(var));
    assertTrue(NodeUtil.isVarDeclaration(var.getFirstChild()));
    assertEquals(Token.NUMBER, NodeUtil.getAssignedValue(var.getFirstChild()).getType());

    Node call = new Node(Token.CALL);
    call.addChildToBack(Node.newString(Token.NAME, "f"));
    assertTrue(NodeUtil.isExprCall(NodeUtil.newExpr(call)));

    assertTrue(NodeUtil.isGet(NodeUtil.newQualifiedNameNode("a.b", 0, 0)));
    assertTrue(NodeUtil.isGetProp(NodeUtil.newQualifiedNameNode("a.b", 0, 0)));
    assertTrue(NodeUtil.isName(Node.newString(Token.NAME, "x")));
    assertTrue(NodeUtil.isNew(new Node(Token.NEW)));
    assertTrue(NodeUtil.isString(Node.newString(Token.STRING, "x")));
  }

  @Test(timeout = 4000)
  public void testLoopAndControlStructure() {
    Node forIn = new Node(Token.FOR);
    forIn.addChildToBack(Node.newString(Token.NAME, "x"));
    forIn.addChildToBack(Node.newString(Token.NAME, "obj"));
    forIn.addChildToBack(new Node(Token.BLOCK));
    assertTrue(NodeUtil.isForIn(forIn));

    assertTrue(NodeUtil.isLoopStructure(new Node(Token.FOR)));
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.WHILE)));
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.DO)));
    assertFalse(NodeUtil.isLoopStructure(new Node(Token.IF)));

    Node whileNode = new Node(Token.WHILE);
    Node cond = Node.newString(Token.NAME, "c");
    whileNode.addChildToBack(cond);
    Node whileBody = new Node(Token.BLOCK);
    whileNode.addChildToBack(whileBody);
    assertEquals(whileBody, NodeUtil.getLoopCodeBlock(whileNode));

    Node doNode = new Node(Token.DO);
    Node doBody = new Node(Token.BLOCK);
    doNode.addChildToBack(doBody);
    doNode.addChildToBack(cond);
    assertEquals(doBody, NodeUtil.getLoopCodeBlock(doNode));

    Node ifNode = new Node(Token.IF);
    ifNode.addChildToBack(cond);
    Node ifBody = new Node(Token.BLOCK);
    ifNode.addChildToBack(ifBody);
    assertTrue(NodeUtil.isControlStructure(ifNode));
    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, ifBody));
    assertEquals(cond, NodeUtil.getConditionExpression(ifNode));

    Node forNode = new Node(Token.FOR);
    forNode.addChildToBack(new Node(Token.EMPTY));
    forNode.addChildToBack(cond);
    forNode.addChildToBack(new Node(Token.EMPTY));
    forNode.addChildToBack(new Node(Token.BLOCK));
    assertEquals(cond, NodeUtil.getConditionExpression(forNode));
  }

  @Test(timeout = 4000)
  public void testStatementPredicates() {
    assertTrue(NodeUtil.isStatementBlock(new Node(Token.SCRIPT)));
    assertTrue(NodeUtil.isStatementBlock(new Node(Token.BLOCK)));
    assertTrue(NodeUtil.isSwitchCase(new Node(Token.CASE)));
    assertTrue(NodeUtil.isSwitchCase(new Node(Token.DEFAULT)));

    Node label = new Node(Token.LABEL);
    Node labelName = Node.newString(Token.NAME, "l");
    label.addChildToBack(labelName);
    label.addChildToBack(new Node(Token.BLOCK));
    assertTrue(NodeUtil.isLabelName(labelName));

    Node tryNode = new Node(Token.TRY);
    tryNode.addChildToBack(new Node(Token.BLOCK));
    tryNode.addChildToBack(new Node(Token.CATCH));
    Node finallyBlock = new Node(Token.BLOCK);
    tryNode.addChildToBack(finallyBlock);
    assertTrue(NodeUtil.isTryFinallyNode(tryNode, finallyBlock));

    // Defect target: CASE is a valid statement parent.
    Node caseNode = new Node(Token.CASE);
    caseNode.addChildToBack(Node.newNumber(1));
    Node varInCase = NodeUtil.newVarNode("x", null);
    caseNode.addChildToBack(varInCase);
    assertTrue(NodeUtil.isStatement(varInCase));

    Node defaultNode = new Node(Token.DEFAULT);
    Node varInDefault = NodeUtil.newVarNode("y", null);
    defaultNode.addChildToBack(varInDefault);
    assertTrue(NodeUtil.isStatement(varInDefault));
  }

  @Test(timeout = 4000)
  public void testRemoveChild() {
    Node block = new Node(Token.BLOCK);
    Node child = Node.newNumber(1);
    block.addChildToBack(child);
    NodeUtil.removeChild(block, child);
    assertNull(child.getParent());
    assertEquals(0, block.getChildCount());

    // Defect target: removing a statement from a CASE node is valid.
    Node caseNode = new Node(Token.CASE);
    caseNode.addChildToBack(Node.newNumber(1));
    Node varNode = NodeUtil.newVarNode("x", null);
    caseNode.addChildToBack(varNode);
    NodeUtil.removeChild(caseNode, varNode);
    assertNull(varNode.getParent());
    assertEquals(1, caseNode.getChildCount());
  }

  @Test(timeout = 4000)
  public void testTryMergeBlock() {
    Node parentBlock = new Node(Token.BLOCK);
    Node inner = new Node(Token.BLOCK);
    Node number = Node.newNumber(1);
    inner.addChildToBack(number);
    parentBlock.addChildToBack(inner);
    assertTrue(NodeUtil.tryMergeBlock(inner));
    assertEquals(number, parentBlock.getFirstChild());
    assertEquals(1, parentBlock.getChildCount());

    Node label = new Node(Token.LABEL);
    label.addChildToBack(Node.newString(Token.NAME, "l"));
    Node inner2 = new Node(Token.BLOCK);
    Node number2 = Node.newNumber(2);
    inner2.addChildToBack(number2);
    label.addChildToBack(inner2);
    assertTrue(NodeUtil.tryMergeBlock(inner2));
    assertEquals(number2, label.getLastChild());
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationPredicates() {
    Node script = new Node(Token.SCRIPT);
    Node fnDecl = fn("f");
    script.addChildToBack(fnDecl);
    assertFalse(NodeUtil.isFunctionAnonymous(fnDecl));
    assertTrue(NodeUtil.isFunctionDeclaration(fnDecl));
    assertTrue(NodeUtil.isHoistedFunctionDeclaration(fnDecl));

    Node fnExpr = fn("g");
    Node call = new Node(Token.CALL);
    call.addChildToBack(fnExpr);
    assertTrue(NodeUtil.isFunctionAnonymous(fnExpr));
    assertFalse(NodeUtil.isFunctionDeclaration(fnExpr));

    // Defect target: a function in a CASE is a declaration, not anonymous.
    Node caseNode = new Node(Token.CASE);
    caseNode.addChildToBack(Node.newNumber(1));
    Node fnCase = fn("h");
    caseNode.addChildToBack(fnCase);
    assertFalse(NodeUtil.isFunctionAnonymous(fnCase));
    assertTrue(NodeUtil.isFunctionDeclaration(fnCase));
  }

  @Test(timeout = 4000)
  public void testFunctionObjectCalls() {
    Node call = new Node(Token.CALL);
    call.addChildToBack(NodeUtil.newQualifiedNameNode("x.call", 0, 0));
    assertTrue(NodeUtil.isObjectCallMethod(call, "call"));
    assertTrue(NodeUtil.isFunctionObjectCall(call));
    assertFalse(NodeUtil.isFunctionObjectApply(call));
    assertTrue(NodeUtil.isSimpleFunctionObjectCall(call));

    Node applyCall = new Node(Token.CALL);
    applyCall.addChildToBack(NodeUtil.newQualifiedNameNode("x.apply", 0, 0));
    assertTrue(NodeUtil.isFunctionObjectApply(applyCall));
    assertFalse(NodeUtil.isFunctionObjectCall(applyCall));
  }

  @Test(timeout = 4000)
  public void testLhsAndObjectLitKey() {
    Node assign = new Node(Token.ASSIGN);
    Node lhs = Node.newString(Token.NAME, "x");
    assign.addChildToBack(lhs);
    assign.addChildToBack(Node.newNumber(1));
    assertTrue(NodeUtil.isLhs(lhs, assign));
    assertFalse(NodeUtil.isLhs(assign.getLastChild(), assign));

    Node var = NodeUtil.newVarNode("y", null);
    assertTrue(NodeUtil.isLhs(var.getFirstChild(), var));

    Node obj = new Node(Token.OBJECTLIT);
    Node key1 = Node.newString(Token.STRING, "a");
    Node val1 = Node.newNumber(1);
    Node key2 = Node.newString(Token.STRING, "b");
    Node val2 = Node.newNumber(2);
    obj.addChildToBack(key1);
    obj.addChildToBack(val1);
    obj.addChildToBack(key2);
    obj.addChildToBack(val2);
    assertTrue(NodeUtil.isObjectLitKey(key1, obj));
    assertTrue(NodeUtil.isObjectLitKey(key2, obj));
    assertFalse(NodeUtil.isObjectLitKey(val1, obj));
  }

  @Test(timeout = 4000)
  public void testNameAndTypeReferenceUtilities() {
    Node root = new Node(Token.BLOCK);
    root.addChildToBack(Node.newString(Token.NAME, "x"));
    root.addChildToBack(NodeUtil.newVarNode("y", null));
    assertTrue(NodeUtil.containsType(root, Token.NAME));
    assertFalse(NodeUtil.containsType(root, Token.CALL));
    assertEquals(2, NodeUtil.getNodeTypeReferenceCount(root, Token.NAME));
    assertTrue(NodeUtil.isNameReferenced(root, "x"));
    assertTrue(NodeUtil.isNameReferenced(root, "y"));
    assertFalse(NodeUtil.isNameReferenced(root, "z"));
    assertEquals(1, NodeUtil.getNameReferenceCount(root, "y"));

    Node outer = new Node(Token.BLOCK);
    outer.addChildToBack(fn("inner"));
    assertFalse(NodeUtil.containsTypeInOuterScope(outer, Token.NAME));
    assertTrue(NodeUtil.containsTypeInOuterScope(outer, Token.FUNCTION));
  }

  @Test(timeout = 4000)
  public void testVarArgsFunctionIgnoresVarDeclarations() {
    Node body = new Node(Token.BLOCK);
    body.addChildToBack(NodeUtil.newVarNode("arguments", null));
    Node fn1 = NodeUtil.newFunctionNode("f", Collections.<Node>emptyList(), body, 0, 0);
    assertFalse(NodeUtil.isVarArgsFunction(fn1));

    Node body2 = new Node(Token.BLOCK);
    body2.addChildToBack(NodeUtil.newExpr(Node.newString(Token.NAME, "arguments")));
    Node fn2 = NodeUtil.newFunctionNode("g", Collections.<Node>emptyList(), body2, 0, 0);
    assertTrue(NodeUtil.isVarArgsFunction(fn2));
  }

  @Test(timeout = 4000)
  public void testQualifiedNameAndLatin() {
    Node qualified = NodeUtil.newQualifiedNameNode("foo.bar.baz", 0, 0);
    assertEquals(Token.GETPROP, qualified.getType());
    assertEquals("foo.bar.baz", qualified.getQualifiedName());
    Node simple = NodeUtil.newQualifiedNameNode("foo", 0, 0);
    assertEquals(Token.NAME, simple.getType());
    assertEquals("foo", simple.getQualifiedName());

    assertTrue(NodeUtil.isLatin("abc"));
    assertFalse(NodeUtil.isLatin("a\u00e9"));
    assertTrue(NodeUtil.isValidPropertyName("foo"));
    assertFalse(NodeUtil.isValidPropertyName("for"));
    assertFalse(NodeUtil.isValidPropertyName("a b"));
    assertFalse(NodeUtil.isValidPropertyName("a\u00e9"));
  }

  @Test(timeout = 4000)
  public void testPrototypeProperty() {
    Node q = NodeUtil.newQualifiedNameNode("Foo.prototype.bar", 0, 0);
    assertTrue(NodeUtil.isPrototypeProperty(q));
    assertEquals("Foo", NodeUtil.getPrototypeClassName(q).getQualifiedName());
    assertEquals("bar", NodeUtil.getPrototypePropertyName(q));

    Node assign = new Node(Token.ASSIGN);
    assign.addChildToBack(q);
    assign.addChildToBack(Node.newNumber(1));
    assertTrue(NodeUtil.isPrototypePropertyDeclaration(NodeUtil.newExpr(assign)));

    Node nonProto = NodeUtil.newQualifiedNameNode("Foo.bar", 0, 0);
    assertFalse(NodeUtil.isPrototypeProperty(nonProto));
  }

  @Test(timeout = 4000)
  public void testTryAndMisc() {
    Node tryNode = new Node(Token.TRY);
    tryNode.addChildToBack(new Node(Token.BLOCK));
    Node catchBlock = new Node(Token.BLOCK);
    tryNode.addChildToBack(catchBlock);
    Node finallyBlock = new Node(Token.BLOCK);
    tryNode.addChildToBack(finallyBlock);
    assertTrue(NodeUtil.hasFinally(tryNode));
    assertEquals(catchBlock, NodeUtil.getCatchBlock(tryNode));

    Node catchBlockWithHandler = new Node(Token.BLOCK);
    catchBlockWithHandler.addChildToBack(new Node(Token.CATCH));
    assertTrue(NodeUtil.hasCatchHandler(catchBlockWithHandler));
    assertFalse(NodeUtil.hasCatchHandler(new Node(Token.BLOCK)));

    List<Node> params = Collections.<Node>singletonList(Node.newString(Token.NAME, "a"));
    Node fnNode = NodeUtil.newFunctionNode("f", params, new Node(Token.BLOCK), 0, 0);
    Node lp = NodeUtil.getFnParameters(fnNode);
    assertEquals(Token.LP, lp.getType());
    assertEquals(1, lp.getChildCount());

    Node constName = Node.newString(Token.NAME, "CONST");
    constName.putBooleanProp(Node.IS_CONSTANT_NAME, true);
    assertTrue(NodeUtil.isConstantName(constName));
    assertFalse(NodeUtil.isConstantName(Node.newString(Token.NAME, "x")));

    Node parent = new Node(Token.SCRIPT);
    parent.putProp(Node.SOURCENAME_PROP, "test.js");
    Node child = new Node(Token.BLOCK);
    parent.addChildToBack(child);
    assertEquals("test.js", NodeUtil.getSourceName(child));
  }

  @Test(timeout = 4000)
  public void testVarsDeclaredInBranch() {
    Node branch = new Node(Token.BLOCK);
    branch.addChildToBack(NodeUtil.newVarNode("a", Node.newNumber(1)));
    Node innerFunction = fn("f");
    innerFunction.getLastChild().addChildToBack(NodeUtil.newVarNode("b", null));
    branch.addChildToBack(innerFunction);

    Collection<Node> vars = NodeUtil.getVarsDeclaredInBranch(branch);
    assertEquals(1, vars.size());
    assertEquals("a", vars.iterator().next().getString());
  }

  private static Node name(String value) {
    return Node.newString(Token.NAME, value);
  }

  private static Node fn(String functionName) {
    Node n = new Node(Token.FUNCTION);
    n.addChildToBack(Node.newString(Token.NAME, functionName == null ? "" : functionName));
    n.addChildToBack(new Node(Token.LP));
    n.addChildToBack(new Node(Token.BLOCK));
    return n;
  }
}