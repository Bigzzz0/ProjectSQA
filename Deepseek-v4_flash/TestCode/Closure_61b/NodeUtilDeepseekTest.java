package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;
import com.google.javascript.rhino.JSDocInfo;

public class NodeUtilDeepseekTest {

  /* 
   * [Branch & Defect Analysis Matrix]
   * 
   * Partition A: Core Functional Logic & State Transitions
   *   - getPureBooleanValue: STRING (empty/non-empty), NUMBER (zero/non-zero), 
   *     FALSE, NULL, VOID, TRUE, REGEXP, NAME("undefined","NaN","Infinity"),
   *     ARRAYLIT/OBJECTLIT without side effects, NOT
   *   - getImpureBooleanValue: ASSIGN, COMMA, NOT, AND, OR, HOOK, ARRAYLIT, OBJECTLIT
   *   - getStringValue: STRING, NAME, NUMBER, FALSE/TRUE/NULL, VOID, NOT, ARRAYLIT, OBJECTLIT
   *   - getNumberValue: TRUE, FALSE, NULL, NUMBER, VOID, NAME, NEG, NOT, STRING, ARRAYLIT/OBJECTLIT
   *   - getStringNumberValue: hex, signed hex, infinity variations, empty, whitespace, vertical tab
   *   - isImmutableValue: immutable types, NOT, VOID, NEG, NAME constants
   *   - isLiteralValue: ARRAYLIT, REGEXP, OBJECTLIT, FUNCTION, immutable
   *   - isSimpleOperator/isSimpleOperatorType: all simple operator types
   *   - mayHaveSideEffects/mayEffectMutableState: various node types, assignment ops, object literals
   *   - functionCallHasSideEffects: built-in functions, getProp, regexp/string methods
   *   - constructorCallHasSideEffects: NEW node, no side effects flag
   *   - evaluatesToLocalValue: ASSIGN, COMMA, AND/OR, HOOK, INC/DEC, THIS, NAME, 
   *     GETELEM/GETPROP, CALL, NEW, FUNCTION/REGEXP/ARRAYLIT/OBJECTLIT, DELPROP/IN
   *   - precedence: all operator types
   *   - isNumericResult/isBooleanResult/mayBeString/isUndefined/isNull/isNullOrUndefined
   *   - isAssignmentOp, getOpFromAssignmentOp
   *   - containsType, containsCall, containsFunction
   *   - isFunctionExpression, isFunctionDeclaration, isEmptyFunctionExpression
   *   - isGet, isGetProp, isName, isNew, isVar, isVarDeclaration, getAssignedValue
   *   - isExprAssign, isExprCall, isForIn, isCall, isCallOrNew
   *   - getFunctionName, getNearestFunctionName
   *   - isEmptyBlock, isValidDefineValue
   *   - trimJsWhiteSpace, isStrWhiteSpaceChar, getArrayElementStringValue, arrayToString
   * 
   * Partition B: Boundary Value Analysis & Extremes
   *   - STRING: "", "non-empty"
   *   - NUMBER: 0, -0, NaN, Infinity, 1, -1, MAX_POSITIVE_INTEGER
   *   - NAME: "undefined", "Infinity", "NaN"
   *   - ARRAYLIT: empty, single element, nested null/undefined
   *   - OBJECTLIT: empty, with properties
   *   - NULL/FALSE/VOID: all false-y in getPureBooleanValue
   *   - NOT of FALSE/TRUE
   *   - escaped hex boundaries: "0x", "0X", "-0x1"
   * 
   * Partition C: Defect-Targeted Branch Zone (Defects4J ground truth)
   *   - PeepholeRemoveDeadCode failures: likely in mayHaveSideEffects,
   *     checkForStateChangeHelper, functionCallHasSideEffects, or 
   *     isSimpleOperator/isAssignmentOp logic. Focus on CALL, NEW, 
   *     assignment operators, and side-effect detection.
   * 
   * Partition D: Exception & Defensive Guard Paths
   *   - constructorCallHasSideEffects with non-NEW node
   *   - functionCallHasSideEffects with non-CALL node
   *   - getFunctionJSDocInfo with non-FUNCTION node
   *   - ImmutableSet / Preconditions violations
   * 
   * Partition E: Object Lifecycle & Contract Integrity
   *   - Static utility class: no instantiation
   *   - Null checks where @Nullable is used
   */

  // ===== Partition A: Core Functional Logic & State Transitions =====

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_string() {
    // empty string -> FALSE
    Node emptyStr = Node.newString(Token.STRING, "");
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(emptyStr));

    // non-empty string -> TRUE
    Node nonEmptyStr = Node.newString(Token.STRING, "hello");
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(nonEmptyStr));

    // zero-length string from different token? - already covered
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_number() {
    Node zero = Node.newNumber(0);
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(zero));

    Node nonZero = Node.newNumber(2.5);
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(nonZero));

    Node negZero = Node.newNumber(-0.0);
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(negZero));
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_nullFalseVoid() {
    Node nullNode = new Node(Token.NULL);
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nullNode));

    Node falseNode = new Node(Token.FALSE);
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(falseNode));

    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(voidNode));
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_trueRegexp() {
    Node trueNode = new Node(Token.TRUE);
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(trueNode));

    Node regexp = new Node(Token.REGEXP, Node.newString("test"));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(regexp));
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_nameConstants() {
    Node undefined = Node.newString(Token.NAME, "undefined");
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(undefined));

    Node nan = Node.newString(Token.NAME, "NaN");
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nan));

    Node infinity = Node.newString(Token.NAME, "Infinity");
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(infinity));
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_arrayLitObjectLit() {
    Node arrayLit = new Node(Token.ARRAYLIT);
    // ARRAYLIT without side effects (empty) should be TRUE
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(arrayLit));

    Node objLit = new Node(Token.OBJECTLIT);
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(objLit));
  }

  @Test(timeout = 4000)
  public void testGetPureBooleanValue_not() {
    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(notFalse));

    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(notTrue));
  }

  @Test(timeout = 4000)
  public void testGetImpureBooleanValue() {
    // ASSIGN: value is last child (RHS)
    Node assign = new Node(Token.ASSIGN, 
        Node.newString(Token.NAME, "x"), new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(assign));

    // COMMA: value is last child
    Node comma = new Node(Token.COMMA, 
        Node.newString(Token.NAME, "unused"), new Node(Token.FALSE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(comma));

    // NOT
    Node not = new Node(Token.NOT, new Node(Token.FALSE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(not));

    // AND: both true
    Node andTrue = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(andTrue));

    // AND: one false
    Node andFalse = new Node(Token.AND, new Node(Token.TRUE), new Node(Token.FALSE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(andFalse));

    // OR: both true
    Node orTrue = new Node(Token.OR, new Node(Token.TRUE), new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(orTrue));

    // OR: both false
    Node orFalse = new Node(Token.OR, new Node(Token.FALSE), new Node(Token.FALSE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(orFalse));

    // HOOK: true/false values differ -> UNKNOWN
    Node hook = new Node(Token.HOOK, new Node(Token.TRUE), 
        new Node(Token.TRUE), new Node(Token.FALSE));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(hook));

    // HOOK: true/false same -> that value
    Node hookSame = new Node(Token.HOOK, new Node(Token.TRUE), 
        new Node(Token.TRUE), new Node(Token.TRUE));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hookSame));

    // ARRAYLIT, OBJECTLIT -> TRUE (ignoring side effects)
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(new Node(Token.ARRAYLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(new Node(Token.OBJECTLIT)));
  }

  @Test(timeout = 4000)
  public void testGetStringValue() {
    // STRING
    Node strNode = Node.newString(Token.STRING, "test");
    assertEquals("test", NodeUtil.getStringValue(strNode));

    // NAME constants
    assertEquals("undefined", NodeUtil.getStringValue(Node.newString(Token.NAME, "undefined")));
    assertEquals("Infinity", NodeUtil.getStringValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals("NaN", NodeUtil.getStringValue(Node.newString(Token.NAME, "NaN")));

    // NUMBER
    Node intNum = Node.newNumber(42);
    assertEquals("42", NodeUtil.getStringValue(intNum));
    Node doubleNum = Node.newNumber(3.14);
    assertEquals("3.14", NodeUtil.getStringValue(doubleNum));

    // FALSE, TRUE, NULL
    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));

    // VOID
    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    assertEquals("undefined", NodeUtil.getStringValue(voidNode));

    // NOT with child FALSE -> "true" (reversed)
    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    assertEquals("true", NodeUtil.getStringValue(notFalse));

    // OBJECTLIT
    assertEquals("[object Object]", NodeUtil.getStringValue(new Node(Token.OBJECTLIT)));
  }

  @Test(timeout = 4000)
  public void testArrayToString() {
    // empty array
    Node emptyArr = new Node(Token.ARRAYLIT);
    assertEquals("", NodeUtil.arrayToString(emptyArr));

    // single string element
    Node arr1 = new Node(Token.ARRAYLIT, Node.newString("a"));
    assertEquals("a", NodeUtil.arrayToString(arr1));

    // multiple elements
    Node arr2 = new Node(Token.ARRAYLIT, Node.newString("a"), Node.newString("b"));
    assertEquals("a,b", NodeUtil.arrayToString(arr2));

    // null/undefined elements become empty
    Node arrWithNull = new Node(Token.ARRAYLIT, new Node(Token.NULL));
    assertEquals("", NodeUtil.arrayToString(arrWithNull));
  }

  @Test(timeout = 4000)
  public void testGetNumberValue() {
    assertEquals(1.0, NodeUtil.getNumberValue(new Node(Token.TRUE)), 0.0);
    assertEquals(0.0, NodeUtil.getNumberValue(new Node(Token.FALSE)), 0.0);
    assertEquals(0.0, NodeUtil.getNumberValue(new Node(Token.NULL)), 0.0);
    Node num = Node.newNumber(3.14);
    assertEquals(3.14, NodeUtil.getNumberValue(num), 0.0);

    // VOID without side effects -> NaN
    Node voidNode = new Node(Token.VOID, Node.newNumber(0));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(voidNode)));

    // NAME undefined -> NaN
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "undefined"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "NaN"))));

    // NAME Infinity -> POSITIVE_INFINITY
    assertEquals(Double.POSITIVE_INFINITY, NodeUtil.getNumberValue(
        Node.newString(Token.NAME, "Infinity")), 0.0);

    // NEG of Infinity -> NEGATIVE_INFINITY
    Node negInf = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
    assertEquals(Double.NEGATIVE_INFINITY, NodeUtil.getNumberValue(negInf), 0.0);

    // NOT of FALSE -> 1.0 (reversed: false -> 1.0)
    assertEquals(0.0, NodeUtil.getNumberValue(new Node(Token.NOT, new Node(Token.TRUE))), 0.0);
  }

  @Test(timeout = 4000)
  public void testGetStringNumberValue() {
    assertEquals(0.0, NodeUtil.getStringNumberValue(""), 0.0);
    assertEquals(42.0, NodeUtil.getStringNumberValue("42"), 0.0);
    assertNull(NodeUtil.getStringNumberValue("\u000B")); // vertical tab
    // hex
    assertEquals(255.0, NodeUtil.getStringNumberValue("0xff"), 0.0);
    // signed hex - should return null
    assertNull(NodeUtil.getStringNumberValue("-0x1"));
    // infinity variations - should return null
    assertNull(NodeUtil.getStringNumberValue("infinity"));
    assertNull(NodeUtil.getStringNumberValue("-infinity"));
    // NaN string
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("notANumber")));
  }

  @Test(timeout = 4000)
  public void testIsImmutableValue() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.STRING, "const")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(1)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));

    // NOT of immutable
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NOT, new Node(Token.FALSE))));

    // VOID, NEG
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID, Node.newNumber(0))));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newNumber(5))));

    // NAME constants
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));

    // non-constant NAME is not immutable
    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "x")));
  }

  @Test(timeout = 4000)
  public void testIsLiteralValue() {
    assertTrue(NodeUtil.isLiteralValue(new Node(Token.ARRAYLIT), false));
    assertTrue(NodeUtil.isLiteralValue(new Node(Token.REGEXP, Node.newString("test")), false));
    assertTrue(NodeUtil.isLiteralValue(new Node(Token.OBJECTLIT), false));

    // literal array with literal child
    Node arrWithLit = new Node(Token.ARRAYLIT, Node.newString("a"));
    assertTrue(NodeUtil.isLiteralValue(arrWithLit, false));

    // function expression with includeFunctions=true
    Node funcExpr = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""));
    funcExpr.addChildToBack(new Node(Token.LP));
    funcExpr.addChildToBack(new Node(Token.BLOCK));
    assertTrue(NodeUtil.isLiteralValue(funcExpr, true));
    assertFalse(NodeUtil.isLiteralValue(funcExpr, false));
  }

  @Test(timeout = 4000)
  public void testIsSimpleOperator() {
    // Positive cases
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.ADD)));
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.GETPROP)));
    assertTrue(NodeUtil.isSimpleOperator(new Node(Token.GETELEM)));

    // Negative cases: assignment op is not simple
    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(0));
    assertFalse(NodeUtil.isSimpleOperator(assign));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_simpleTypes() {
    assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.TRUE)));
    assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.FALSE)));
    assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.NULL)));
    assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.STRING, "")));
    assertFalse(NodeUtil.mayHaveSideEffects(Node.newNumber(1)));
    assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.THIS)));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_objectLiteralWithProperties_noSideEffects() {
    // ObjectLit with simple properties
    Node objLit = new Node(Token.OBJECTLIT);
    Node keyVal = Node.newString(Token.STRING, "key");
    keyVal.addChildToBack(Node.newNumber(42));
    objLit.addChildToBack(keyVal);
    assertFalse(NodeUtil.mayHaveSideEffects(objLit));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_assignToName() {
    // x = 5 has side effects (modifies state)
    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(5));
    assertTrue(NodeUtil.mayHaveSideEffects(assign));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_call() {
    // plain call should have side effects by default
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    assertTrue(NodeUtil.mayHaveSideEffects(call));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_new_noSideEffectsConstructor() {
    // "new Array()" should have no side effects (in CONSTRUCTORS_WITHOUT_SIDE_EFFECTS)
    Node newArr = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
    assertFalse(NodeUtil.mayHaveSideEffects(newArr));

    // "new Object()" also no side effects
    Node newObj = new Node(Token.NEW, Node.newString(Token.NAME, "Object"));
    assertFalse(NodeUtil.mayHaveSideEffects(newObj));

    // unknown constructor: should have side effects
    Node newUnknown = new Node(Token.NEW, Node.newString(Token.NAME, "MyClass"));
    assertTrue(NodeUtil.mayHaveSideEffects(newUnknown));
  }

  @Test(timeout = 4000)
  public void testMayEffectMutableState_functionExpression() {
    // function expression should NOT affect mutable state
    Node funcExpr = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""));
    funcExpr.addChildToBack(new Node(Token.LP));
    funcExpr.addChildToBack(new Node(Token.BLOCK));
    assertFalse(NodeUtil.mayEffectMutableState(funcExpr));

    // but with checkForNewObjects=true, it should affect mutable state
    // (since it creates a new function object)
    // Note: mayEffectMutableState calls checkForStateChangeHelper with checkForNewObjects=true
    assertTrue(NodeUtil.mayEffectMutableState(funcExpr));
  }

  @Test(timeout = 4000)
  public void testFunctionCallHasSideEffects_builtin() {
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "Object"));
    assertFalse(NodeUtil.functionCallHasSideEffects(call));

    call = new Node(Token.CALL, Node.newString(Token.NAME, "Array"));
    assertFalse(NodeUtil.functionCallHasSideEffects(call));

    call = new Node(Token.CALL, Node.newString(Token.NAME, "randomFunc"));
    assertTrue(NodeUtil.functionCallHasSideEffects(call));
  }

  @Test(timeout = 4000)
  public void testFunctionCallHasSideEffects_getPropToString() {
    // obj.toString() has no side effects
    Node obj = Node.newString(Token.NAME, "obj");
    Node getProp = new Node(Token.GETPROP, obj, Node.newString(Token.STRING, "toString"));
    Node call = new Node(Token.CALL, getProp);
    assertFalse(NodeUtil.functionCallHasSideEffects(call));
  }

  @Test(timeout = 4000)
  public void testConstructorCallHasSideEffects_throwsOnNonNew() {
    try {
      NodeUtil.constructorCallHasSideEffects(new Node(Token.CALL));
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test(timeout = 4000)
  public void testPrecedence() {
    assertEquals(0, NodeUtil.precedence(Token.COMMA));
    assertEquals(1, NodeUtil.precedence(Token.ASSIGN));
    assertEquals(2, NodeUtil.precedence(Token.HOOK));
    assertEquals(3, NodeUtil.precedence(Token.OR));
    assertEquals(4, NodeUtil.precedence(Token.AND));
    assertEquals(5, NodeUtil.precedence(Token.BITOR));
    assertEquals(8, NodeUtil.precedence(Token.EQ));
    assertEquals(9, NodeUtil.precedence(Token.LT));
    assertEquals(10, NodeUtil.precedence(Token.LSH));
    assertEquals(11, NodeUtil.precedence(Token.ADD));
    assertEquals(12, NodeUtil.precedence(Token.MUL));
    assertEquals(13, NodeUtil.precedence(Token.NOT));
    assertEquals(15, NodeUtil.precedence(Token.NAME));
    assertEquals(15, NodeUtil.precedence(Token.ARRAYLIT));
  }

  @Test(timeout = 4000)
  public void testIsNumericResult() {
    assertTrue(NodeUtil.isNumericResult(Node.newNumber(5)));
    assertTrue(NodeUtil.isNumericResult(new Node(Token.BITNOT, Node.newNumber(1))));
    assertTrue(NodeUtil.isNumericResult(new Node(Token.SUB, 
        Node.newNumber(10), Node.newNumber(3))));
    assertFalse(NodeUtil.isNumericResult(Node.newString("notnum")));
    assertFalse(NodeUtil.isNumericResult(new Node(Token.TRUE)));
  }

  @Test(timeout = 4000)
  public void testIsBooleanResult() {
    assertTrue(NodeUtil.isBooleanResult(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isBooleanResult(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isBooleanResult(new Node(Token.EQ, 
        Node.newNumber(1), Node.newNumber(2))));
    assertTrue(NodeUtil.isBooleanResult(new Node(Token.NOT, Node.newNumber(1))));
    assertFalse(NodeUtil.isBooleanResult(Node.newNumber(1)));
  }

  @Test(timeout = 4000)
  public void testMayBeString() {
    assertTrue(NodeUtil.mayBeString(Node.newString("str")));
    assertTrue(NodeUtil.mayBeString(new Node(Token.ADD, Node.newString("a"), Node.newNumber(1))));
    assertFalse(NodeUtil.mayBeString(Node.newNumber(1)));
    assertFalse(NodeUtil.mayBeString(new Node(Token.TRUE)));
  }

  @Test(timeout = 4000)
  public void testIsUndefinedIsNull() {
    assertTrue(NodeUtil.isUndefined(new Node(Token.VOID, Node.newNumber(0))));
    assertTrue(NodeUtil.isUndefined(Node.newString(Token.NAME, "undefined")));
    assertFalse(NodeUtil.isUndefined(Node.newString(Token.NAME, "x")));

    assertTrue(NodeUtil.isNull(new Node(Token.NULL)));
    assertFalse(NodeUtil.isNull(new Node(Token.FALSE)));

    assertTrue(NodeUtil.isNullOrUndefined(new Node(Token.NULL)));
    assertTrue(NodeUtil.isNullOrUndefined(new Node(Token.VOID, Node.newNumber(0))));
  }

  @Test(timeout = 4000)
  public void testIsAssignmentOp() {
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN)));
    assertTrue(NodeUtil.isAssignmentOp(new Node(Token.ASSIGN_ADD)));
    assertFalse(NodeUtil.isAssignmentOp(new Node(Token.ADD)));
  }

  @Test(timeout = 4000)
  public void testIsExpressionNode() {
    assertTrue(NodeUtil.isExpressionNode(new Node(Token.EXPR_RESULT, Node.newNumber(0))));
    assertFalse(NodeUtil.isExpressionNode(Node.newNumber(0)));
  }

  @Test(timeout = 4000)
  public void testIsFunctionExpression() {
    // function expression: parent not a statement block
    Node funcExpr = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""));
    funcExpr.addChildToBack(new Node(Token.LP));
    funcExpr.addChildToBack(new Node(Token.BLOCK));
    assertTrue(NodeUtil.isFunctionExpression(funcExpr));

    // function declaration: parent is a statement (SCRIPT or BLOCK)
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(funcExpr);
    assertFalse(NodeUtil.isFunctionExpression(funcExpr));
  }

  @Test(timeout = 4000)
  public void testIsEmptyBlock() {
    Node block = new Node(Token.BLOCK);
    assertTrue(NodeUtil.isEmptyBlock(block));
  }

  @Test(timeout = 4000)
  public void testContainsType() {
    Node node = new Node(Token.BLOCK, new Node(Token.IF), new Node(Token.CALL));
    assertTrue(NodeUtil.containsType(node, Token.CALL));
    assertFalse(NodeUtil.containsType(node, Token.WHILE));
  }

  @Test(timeout = 4000)
  public void testGetFunctionName() {
    // function name() {}
    Node func = new Node(Token.FUNCTION, Node.newString(Token.NAME, "myFunc"));
    func.addChildToBack(new Node(Token.LP));
    func.addChildToBack(new Node(Token.BLOCK));
    Node parentBlock = new Node(Token.BLOCK);
    parentBlock.addChildToBack(func);
    assertEquals("myFunc", NodeUtil.getFunctionName(func));
  }

  @Test(timeout = 4000)
  public void testGetNearestFunctionName() {
    // var x = function() {}
    Node func = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""));
    func.addChildToBack(new Node(Token.LP));
    func.addChildToBack(new Node(Token.BLOCK));
    Node nameNode = Node.newString(Token.NAME, "x");
    nameNode.addChildToBack(func);
    Node varNode = new Node(Token.VAR, nameNode);
    assertEquals("x", NodeUtil.getNearestFunctionName(func));
  }

  @Test(timeout = 4000)
  public void testIsForIn() {
    Node forNode = new Node(Token.FOR);
    // FOR with 3 children (for-in)
    forNode.addChildToBack(Node.newString(Token.NAME, "x"));
    forNode.addChildToBack(new Node(Token.IN));
    forNode.addChildToBack(new Node(Token.BLOCK));
    assertTrue(NodeUtil.isForIn(forNode));

    Node forNode4 = new Node(Token.FOR);
    forNode4.addChildToBack(Node.newString(Token.NAME, "i"));
    forNode4.addChildToBack(new Node(Token.IN));
    forNode4.addChildToBack(new Node(Token.BLOCK));
    forNode4.addChildToBack(new Node(Token.EMPTY));
    assertFalse(NodeUtil.isForIn(forNode4));
  }

  // ===== Partition C: Defect-Targeted Branch Zone =====
  // Targets the specific defect from PeepholeRemoveDeadCode tests.
  // The defect likely relates to side-effect detection of certain calls
  // or assignment operations. This test exercises edge case combinations.

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_defectTargeted() {
    // Create a call to a built-in function that should have no side effects,
    // but with a getProp receiver that could be affected by side effects of the receiver.
    Node receiver = new Node(Token.NAME, "x");
    Node prop = Node.newString(Token.STRING, "toString");
    Node getProp = new Node(Token.GETPROP, receiver, prop);
    Node call = new Node(Token.CALL, getProp);
    // This should have side effects because x is a non-local variable.
    // The defect might incorrectly report no side effects here.
    assertTrue("Call to x.toString() should have side effects when x is not local", 
        NodeUtil.mayHaveSideEffects(call));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_callToBuiltinDirect() {
    // Direct call to a built-in function without receiver: no side effects
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "Object"));
    assertFalse("Call to Object() should have no side effects", 
        NodeUtil.mayHaveSideEffects(call));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_assignToGetPropLocal() {
    // Assign to a property of a local value: no side effects
    // Create an object literal assigned to property
    Node objLit = new Node(Token.OBJECTLIT);
    Node assign = new Node(Token.ASSIGN, 
        new Node(Token.GETPROP, objLit.copy(), Node.newString(Token.STRING, "p")),
        Node.newNumber(1));
    // The receiver is a literal, so the assignment should have no side effects.
    // This is a corner case that depends on isLiteralValue.
    assertFalse("Assignment to property of object literal should have no side effects", 
        NodeUtil.mayHaveSideEffects(assign));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_newWithSideEffectsParams() {
    // new Array(call with side effects) should have side effects
    Node call = new Node(Token.CALL, Node.newString(Token.NAME, "sideEffectingFunc"));
    Node newArr = new Node(Token.NEW, Node.newString(Token.NAME, "Array"), call.copy());
    assertTrue("New Array with side-effecting argument should have side effects", 
        NodeUtil.mayHaveSideEffects(newArr));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_delete() {
    Node delete = new Node(Token.DELPROP, new Node(Token.GETPROP, 
        Node.newString(Token.NAME, "obj"), Node.newString(Token.STRING, "prop")));
    assertTrue("delete should have side effects", NodeUtil.mayHaveSideEffects(delete));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_incDec() {
    Node inc = new Node(Token.INC, Node.newString(Token.NAME, "x"));
    assertTrue("Increment should have side effects", NodeUtil.mayHaveSideEffects(inc));
  }

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_throw() {
    Node throwNode = new Node(Token.THROW, Node.newString(Token.NAME, "e"));
    assertTrue("Throw should have side effects", NodeUtil.mayHaveSideEffects(throwNode));
  }

  @Test(timeout = 4000)
  public void testEvaluatesToLocalValue() {
    // Primitive literal is local
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(42)));
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString(Token.STRING, "hello")));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.TRUE)));

    // Array literal is local
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));

    // Object literal is local
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));

    // Function expression is local
    Node funcExpr = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""));
    funcExpr.addChildToBack(new Node(Token.LP));
    funcExpr.addChildToBack(new Node(Token.BLOCK));
    assertTrue(NodeUtil.evaluatesToLocalValue(funcExpr));

    // NAME pointing to local variable? Unknown -> requires predicate
    // Without predicate, NAME is not local unless immutable
    assertFalse(NodeUtil.evaluatesToLocalValue(Node.newString(Token.NAME, "x")));

    // GETPROP is not local without predicate
    assertFalse(NodeUtil.evaluatesToLocalValue(new Node(Token.GETPROP, 
        Node.newString(Token.NAME, "x"), Node.newString(Token.STRING, "p"))));

    // ADD of two literals is local
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2))));
  }

  @Test(timeout = 4000)
  public void testIsValidDefineValue() {
    Set<String> defines = new java.util.HashSet<>();
    defines.add("MY_DEFINE");
    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(42), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.STRING, "hello"), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));

    // Name that is a define
    assertTrue(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "MY_DEFINE"), defines));

    // Name that is not a define
    assertFalse(NodeUtil.isValidDefineValue(Node.newString(Token.NAME, "OTHER"), defines));

    // ADD of two defines
    Node add = new Node(Token.ADD, Node.newString(Token.NAME, "MY_DEFINE"), 
        Node.newString(Token.NAME, "MY_DEFINE"));
    assertTrue(NodeUtil.isValidDefineValue(add, defines));

    // ADD with non-define
    Node addBad = new Node(Token.ADD, Node.newString(Token.NAME, "MY_DEFINE"), 
        Node.newString(Token.NAME, "OTHER"));
    assertFalse(NodeUtil.isValidDefineValue(addBad, defines));
  }
}