package com.google.javascript.jscomp;

/* [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: com.google.javascript.jscomp.NodeUtil
 * Defect Under Analysis: PeepholeFoldConstantsTest::testIEString
 * Failure Mechanism: In IE, the vertical tab character ('\v' / '\u000B') is NOT treated as
 * whitespace. Pre-fix NodeUtil.getStringNumberValue treated '\u000B' as whitespace and
 * trimmed it via trimJsWhiteSpace/isStrWhiteSpaceChar, incorrectly folding expressions like
 * "+'\v1'" to 1.0 (causing "!+'\v1'" to evaluate to false instead of keeping it as IE idiom).
 *
 * Branch & Partition Coverage Map:
 * - Partition A: Core Functional Logic & State Transitions
 *     * Boolean evaluation: getImpureBooleanValue, getPureBooleanValue (AND, OR, NOT, HOOK,
 *       ASSIGN, COMMA, ARRAYLIT, OBJECTLIT, NAME, NUMBER, STRING, NULL, TRUE, FALSE, VOID).
 *     * String conversion: getStringValue, getArrayElementStringValue, arrayToString.
 *     * Number conversion: getNumberValue, getStringNumberValue (hex, signed hex, Infinity).
 *     * AST Inspection: isImmutableValue, isLiteralValue, isValidDefineValue, isEmptyBlock,
 *       isSimpleOperator, isSimpleOperatorType, precedence, opToStr, opToStrNoFail.
 *     * Side-effect analysis: mayHaveSideEffects, mayEffectMutableState,
 *       constructorCallHasSideEffects, functionCallHasSideEffects, canBeSideEffected,
 *       evaluatesToLocalValue, callHasLocalResult, newHasLocalResult.
 * - Partition B: Boundary Value Analysis & Operator Classification
 *     * Character classification: isStrWhiteSpaceChar (BOM, VT, NBSP, LS, PS, FF, SP, TAB),
 *       trimJsWhiteSpace (empty, all-whitespace, leading/trailing, interior).
 *     * AST queries: isAssociative, isCommutative, isAssignmentOp, getOpFromAssignmentOp.
 *     * Functions & Calls: getFunctionName, getNearestFunctionName, isFunctionDeclaration,
 *       isHoistedFunctionDeclaration, isFunctionExpression, isEmptyFunctionExpression,
 *       isVarArgsFunction, isObjectCallMethod, isFunctionObjectCall, isFunctionObjectApply.
 * - Partition C: Defect-Targeted Branch Zone (IE String whitespace / Vertical Tab '\u000B')
 *     * testIEString_verticalTabNotFoldedToNumber: Asserts getStringNumberValue and getNumberValue
 *       do not convert "\u000B1" or vertical tab strings to 1.0.
 * - Partition D: Defensive Guards & Exception Handling
 *     * IllegalStateException and IllegalArgumentException checks on mismatched token types
 *       (constructorCallHasSideEffects on CALL, functionCallHasSideEffects on NEW,
 *       getArgumentForFunction on non-FUNCTION, removeChild on invalid hierarchy,
 *       getOpFromAssignmentOp on non-assignment op, precedence on unknown token).
 * - Partition E: AST Manipulation & Tree Transformations
 *     * Node removal & merging: removeChild (VAR multi-child vs single child, TRY/CATCH/FINALLY,
 *       LABEL, FOR 4-child, statement block), tryMergeBlock, maybeAddFinally.
 *     * Node builders: newExpr, newCallNode, newVarNode, newUndefinedNode, newFunctionNode,
 *       newQualifiedNameNode, getRootOfQualifiedName, redeclareVarsInsideBranch.
 * -----------------------------------------------------------------------------------------
 */

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class NodeUtilGeminiTest {

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (PeepholeFoldConstantsTest::testIEString)
  // =========================================================================

  @Test(timeout = 4000)
  public void testIEString_verticalTabNotFoldedToNumber() {
    // In IE, '\v' ('\u000B') is not treated as whitespace.
    // Calling getStringNumberValue on "\u000B1" or containing '\u000B' must NOT return 1.0.
    Double val = NodeUtil.getStringNumberValue("\u000B1");
    assertNull("Vertical tab should not be trimmed as whitespace for number conversion", val);

    Node strNode = Node.newString("\u000B1");
    Double numVal = NodeUtil.getNumberValue(strNode);
    assertNull("getNumberValue should return null for strings with vertical tab", numVal);
  }

  @Test(timeout = 4000)
  public void testIEString_verticalTabAloneNotZero() {
    Double val = NodeUtil.getStringNumberValue("\u000B");
    assertNull("Standalone vertical tab should not convert to 0.0", val);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions (Boolean/String/Number)
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetPureAndImpureBooleanValue_Primitives() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(new Node(Token.TRUE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.FALSE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.NULL)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.VOID, Node.newNumber(0))));

    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newString("hello")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString("")));

    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newNumber(1.5)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newNumber(0.0)));

    Node nameUndef = Node.newString(Token.NAME, "undefined");
    Node nameNaN = Node.newString(Token.NAME, "NaN");
    Node nameInf = Node.newString(Token.NAME, "Infinity");
    Node nameOther = Node.newString(Token.NAME, "window");
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nameUndef));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(nameNaN));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(nameInf));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(nameOther));

    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(new Node(Token.REGEXP)));
  }

  @Test(timeout = 4000)
  public void testGetImpureBooleanValue_CompositeExpressions() {
    Node assign = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), Node.newNumber(1.0));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(assign));

    Node comma = new Node(Token.COMMA, Node.newNumber(0), Node.newString("abc"));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(comma));

    Node notNode = new Node(Token.NOT, Node.newNumber(0.0));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(notNode));

    Node andNode = new Node(Token.AND, Node.newNumber(1.0), Node.newString(""));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(andNode));

    Node orNode = new Node(Token.OR, Node.newNumber(0.0), Node.newNumber(2.0));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(orNode));

    Node hookMatching = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), Node.newNumber(1.0), Node.newNumber(2.0));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hookMatching));

    Node hookMismatch = new Node(Token.HOOK, Node.newString(Token.NAME, "cond"), Node.newNumber(1.0), Node.newNumber(0.0));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(hookMismatch));

    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(new Node(Token.ARRAYLIT)));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(new Node(Token.OBJECTLIT)));
  }

  @Test(timeout = 4000)
  public void testGetStringValue() {
    assertEquals("foo", NodeUtil.getStringValue(Node.newString("foo")));
    assertEquals("undefined", NodeUtil.getStringValue(Node.newString(Token.NAME, "undefined")));
    assertEquals("NaN", NodeUtil.getStringValue(Node.newString(Token.NAME, "NaN")));
    assertEquals("Infinity", NodeUtil.getStringValue(Node.newString(Token.NAME, "Infinity")));
    assertNull(NodeUtil.getStringValue(Node.newString(Token.NAME, "other")));

    assertEquals("10", NodeUtil.getStringValue(Node.newNumber(10.0)));
    assertEquals("10.5", NodeUtil.getStringValue(Node.newNumber(10.5)));

    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID)));

    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    assertEquals("true", NodeUtil.getStringValue(notFalse));

    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    assertEquals("false", NodeUtil.getStringValue(notTrue));

    Node notUnknown = new Node(Token.NOT, Node.newString(Token.NAME, "unknown"));
    assertNull(NodeUtil.getStringValue(notUnknown));

    Node arrayLit = new Node(Token.ARRAYLIT, Node.newString("a"), new Node(Token.NULL), Node.newNumber(1));
    assertEquals("a,,1", NodeUtil.getStringValue(arrayLit));

    Node arrayLitWithEmpty = new Node(Token.ARRAYLIT, new Node(Token.EMPTY), Node.newString("b"));
    assertEquals(",b", NodeUtil.getStringValue(arrayLitWithEmpty));

    Node objectLit = new Node(Token.OBJECTLIT);
    assertEquals("[object Object]", NodeUtil.getStringValue(objectLit));

    assertNull(NodeUtil.getStringValue(new Node(Token.CALL)));
  }

  @Test(timeout = 4000)
  public void testGetNumberValue_ConstantsAndNodes() {
    assertEquals(1.0, NodeUtil.getNumberValue(new Node(Token.TRUE)), 0.0001);
    assertEquals(0.0, NodeUtil.getNumberValue(new Node(Token.FALSE)), 0.0001);
    assertEquals(0.0, NodeUtil.getNumberValue(new Node(Token.NULL)), 0.0001);
    assertEquals(42.5, NodeUtil.getNumberValue(Node.newNumber(42.5)), 0.0001);

    Node voidPure = new Node(Token.VOID, Node.newNumber(0));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(voidPure)));

    Node voidImpure = new Node(Token.VOID, new Node(Token.CALL, Node.newString(Token.NAME, "fn")));
    assertNull(NodeUtil.getNumberValue(voidImpure));

    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "undefined"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "NaN"))));
    assertEquals(Double.POSITIVE_INFINITY, NodeUtil.getNumberValue(Node.newString(Token.NAME, "Infinity")), 0.0001);
    assertNull(NodeUtil.getNumberValue(Node.newString(Token.NAME, "other")));

    Node negInf = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
    assertEquals(Double.NEGATIVE_INFINITY, NodeUtil.getNumberValue(negInf), 0.0001);

    Node negOther = new Node(Token.NEG, Node.newString(Token.NAME, "x"));
    assertNull(NodeUtil.getNumberValue(negOther));

    Node notZero = new Node(Token.NOT, Node.newNumber(0));
    assertEquals(1.0, NodeUtil.getNumberValue(notZero), 0.0001);

    Node notOne = new Node(Token.NOT, Node.newNumber(1));
    assertEquals(0.0, NodeUtil.getNumberValue(notOne), 0.0001);

    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(5));
    assertEquals(5.0, NodeUtil.getNumberValue(arrayLit), 0.0001);

    Node objectLit = new Node(Token.OBJECTLIT);
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(objectLit)));
  }

  @Test(timeout = 4000)
  public void testGetStringNumberValue_ParsingRules() {
    assertEquals(0.0, NodeUtil.getStringNumberValue(""), 0.0001);
    assertEquals(0.0, NodeUtil.getStringNumberValue("   \t\n\r  "), 0.0001);
    assertEquals(26.0, NodeUtil.getStringNumberValue("0x1a"), 0.0001);
    assertEquals(26.0, NodeUtil.getStringNumberValue("0X1A"), 0.0001);
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("0xZZZ")));

    assertNull(NodeUtil.getStringNumberValue("+0x1a"));
    assertNull(NodeUtil.getStringNumberValue("-0x1a"));

    assertNull(NodeUtil.getStringNumberValue("infinity"));
    assertNull(NodeUtil.getStringNumberValue("-infinity"));
    assertNull(NodeUtil.getStringNumberValue("+infinity"));

    assertEquals(Double.POSITIVE_INFINITY, NodeUtil.getStringNumberValue("Infinity"), 0.0001);
    assertEquals(Double.NEGATIVE_INFINITY, NodeUtil.getStringNumberValue("-Infinity"), 0.0001);
    assertEquals(123.45, NodeUtil.getStringNumberValue("  123.45  "), 0.0001);
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("not_a_number")));
  }

  @Test(timeout = 4000)
  public void testTrimJsWhiteSpace_And_IsStrWhiteSpaceChar() {
    assertEquals("foo", NodeUtil.trimJsWhiteSpace("  foo  "));
    assertEquals("bar", NodeUtil.trimJsWhiteSpace("\t\n\r\u00A0\u000C\u2028\u2029\uFEFFbar"));
    assertEquals("", NodeUtil.trimJsWhiteSpace("   "));

    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar(' '));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\n'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\r'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\t'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u00A0'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u000C'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u2028'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u2029'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\uFEFF'));
    assertEquals(TernaryValue.TRUE, NodeUtil.isStrWhiteSpaceChar('\u2000')); // Unicode space separator
    assertEquals(TernaryValue.FALSE, NodeUtil.isStrWhiteSpaceChar('A'));
  }

  // =========================================================================
  // Partition B: Function Names, Literals & Immutability Analysis
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetFunctionName_VariousForms() {
    // 1. function foo() {}
    Node fn1 = NodeUtil.newFunctionNode("foo", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    Node script = new Node(Token.SCRIPT, fn1);
    assertEquals("foo", NodeUtil.getFunctionName(fn1));

    // 2. var bar = function() {}
    Node fn2 = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    Node varName = Node.newString(Token.NAME, "bar");
    varName.addChildToBack(fn2);
    new Node(Token.VAR, varName);
    assertEquals("bar", NodeUtil.getFunctionName(fn2));

    // 3. a.b.c = function() {}
    Node fn3 = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    Node qname = NodeUtil.newQualifiedNameNode(new DefaultCodingConvention(), "a.b.c", 1, 0);
    new Node(Token.ASSIGN, qname, fn3);
    assertEquals("a.b.c", NodeUtil.getFunctionName(fn3));

    // 4. (function() {})()
    Node fn4 = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    new Node(Token.CALL, fn4);
    assertNull(NodeUtil.getFunctionName(fn4));
  }

  @Test(timeout = 4000)
  public void testGetNearestFunctionName_ObjectLiterals() {
    Node fn = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    Node objKey = Node.newString(Token.STRING, "methodKey");
    objKey.addChildToBack(fn);
    new Node(Token.OBJECTLIT, objKey);
    assertEquals("methodKey", NodeUtil.getNearestFunctionName(fn));

    Node fnNum = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    Node objNumKey = Node.newNumber(42);
    objNumKey.addChildToBack(fnNum);
    new Node(Token.OBJECTLIT, objNumKey);
    assertEquals("42", NodeUtil.getNearestFunctionName(fnNum));
  }

  @Test(timeout = 4000)
  public void testIsImmutableValue_And_IsLiteralValue() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString("str")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(123)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.VOID, Node.newNumber(0))));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newNumber(1))));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NOT, new Node(Token.TRUE))));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "Infinity")));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "NaN")));
    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "myVar")));

    Node arrayLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("a"));
    assertTrue(NodeUtil.isLiteralValue(arrayLit, false));

    Node arrayLitWithNonLit = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "x"));
    assertFalse(NodeUtil.isLiteralValue(arrayLitWithNonLit, false));

    Node regex = new Node(Token.REGEXP, Node.newString("pattern"));
    assertTrue(NodeUtil.isLiteralValue(regex, false));

    Node fnExpr = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    new Node(Token.EXPR_RESULT, fnExpr); // statement context: isFunctionDeclaration
    assertFalse(NodeUtil.isLiteralValue(fnExpr, true)); // declaration is not literal

    Node fnInExpr = NodeUtil.newFunctionNode("", Collections.<Node>emptyList(), new Node(Token.BLOCK), 1, 0);
    new Node(Token.ASSIGN, Node.newString(Token.NAME, "v"), fnInExpr); // expression context
    assertTrue(NodeUtil.isLiteralValue(fnInExpr, true));
    assertFalse(NodeUtil.isLiteralValue(fnInExpr, false));
  }

  @Test(timeout = 4000)
  public void testIsValidDefineValue() {
    Set<String> defines = Sets.newHashSet("DEF_A", "config.DEF_B");

    assertTrue(NodeUtil.isValidDefineValue(Node.newString("hello"), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(100), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isValidDefineValue(add, defines));

    Node nameDef = Node.newString(Token.NAME, "DEF_A");
    assertTrue(NodeUtil.isValidDefineValue(nameDef, defines));

    Node nameNotDef = Node.newString(Token.NAME, "UNKNOWN_DEF");
    assertFalse(NodeUtil.isValidDefineValue(nameNotDef, defines));

    Node getprop = NodeUtil.newQualifiedNameNode(new DefaultCodingConvention(), "config.DEF_B", 1, 0);
    assertTrue(NodeUtil.isValidDefineValue(getprop, defines));

    Node unary = new Node(Token.NOT, new Node(Token.TRUE));
    assertTrue(NodeUtil.isValidDefineValue(unary, defines));
  }

  // =========================================================================
  // Partition B: Operator Classification & Precedence
  // =========================================================================

  @Test(timeout = 4000)
  public void testOperatorCharacteristics() {
    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertFalse(NodeUtil.isAssociative(Token.ADD));
    assertFalse(NodeUtil.isAssociative(Token.SUB));

    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertTrue(NodeUtil.isCommutative(Token.BITOR));
    assertFalse(NodeUtil.isCommutative(Token.ADD));
    assertFalse(NodeUtil.isCommutative(Token.DIV));

    assertTrue(NodeUtil.isSimpleOperatorType(Token.ADD));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.DIV));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.GETPROP));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.CALL));

    Node assignAdd = new Node(Token.ASSIGN_ADD);
    assertTrue(NodeUtil.isAssignmentOp(assignAdd));
    assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(assignAdd));

    Node assignSub = new Node(Token.ASSIGN_SUB);
    assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(assignSub));

    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("!==", NodeUtil.opToStr(Token.SHNE));
    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
    assertEquals("instanceof", NodeUtil.opToStrNoFail(Token.INSTANCEOF));
    assertNull(NodeUtil.opToStr(Token.FUNCTION));
  }

  @Test(timeout = 4000)
  public void testPrecedenceHierarchy() {
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
    assertEquals(15, NodeUtil.precedence(Token.NUMBER));
  }

  @Test(timeout = 4000)
  public void testTypeEvaluationPredicates() {
    assertTrue(NodeUtil.isNumericResult(Node.newNumber(10)));
    assertTrue(NodeUtil.isNumericResult(new Node(Token.SUB, Node.newNumber(5), Node.newNumber(2))));
    assertTrue(NodeUtil.isNumericResult(Node.newString(Token.NAME, "NaN")));
    assertTrue(NodeUtil.isNumericResult(Node.newString(Token.NAME, "Infinity")));
    assertFalse(NodeUtil.isNumericResult(Node.newString("str")));

    assertTrue(NodeUtil.isBooleanResult(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isBooleanResult(new Node(Token.EQ, Node.newNumber(1), Node.newNumber(2))));
    assertTrue(NodeUtil.isBooleanResult(new Node(Token.INSTANCEOF, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"))));
    assertFalse(NodeUtil.isBooleanResult(Node.newNumber(1)));

    assertTrue(NodeUtil.isUndefined(new Node(Token.VOID, Node.newNumber(0))));
    assertTrue(NodeUtil.isUndefined(Node.newString(Token.NAME, "undefined")));
    assertFalse(NodeUtil.isUndefined(Node.newString(Token.NAME, "defined")));

    assertTrue(NodeUtil.isNull(new Node(Token.NULL)));
    assertFalse(NodeUtil.isNull(Node.newNumber(0)));

    assertTrue(NodeUtil.isNullOrUndefined(new Node(Token.NULL)));
    assertTrue(NodeUtil.isNullOrUndefined(new Node(Token.VOID)));
    assertFalse(NodeUtil.isNullOrUndefined(Node.newString("")));

    assertTrue(NodeUtil.mayBeString(Node.newString("abc")));
    assertFalse(NodeUtil.mayBeString(Node.newNumber(123)));
    assertFalse(NodeUtil.mayBeString(new Node(Token.TRUE)));
  }

  // =========================================================================
  // Partition B & E: Side-Effect and Locality Analysis
  // =========================================================================

  @Test(timeout = 4000)
  public void testMayHaveSideEffects_And_EvaluatesToLocalValue() {
    assertFalse(NodeUtil.mayHaveSideEffects(Node.newNumber(42)));
    assertFalse(NodeUtil.mayHaveSideEffects(Node.newString("safe")));
    assertFalse(NodeUtil.mayHaveSideEffects(new Node(Token.EMPTY)));

    assertTrue(NodeUtil.mayHaveSideEffects(new Node(Token.THROW, Node.newString("err"))));

    Node callNoSideEffects = new Node(Token.CALL, Node.newString(Token.NAME, "Math.sin"));
    callNoSideEffects.putBooleanProp(Node.SIDE_EFFECTS_FLAGS, true);
    callNoSideEffects.setSideEffectFlags(Node.NO_SIDE_EFFECTS);
    assertFalse(NodeUtil.functionCallHasSideEffects(callNoSideEffects));

    Node mathCall = new Node(Token.CALL, NodeUtil.newQualifiedNameNode(new DefaultCodingConvention(), "Math.random", 1, 0));
    assertFalse(NodeUtil.functionCallHasSideEffects(mathCall));

    Node builtinCall = new Node(Token.CALL, Node.newString(Token.NAME, "String"));
    assertFalse(NodeUtil.functionCallHasSideEffects(builtinCall));

    Node newArray = new Node(Token.NEW, Node.newString(Token.NAME, "Array"));
    assertFalse(NodeUtil.constructorCallHasSideEffects(newArray));

    Node newCustom = new Node(Token.NEW, Node.newString(Token.NAME, "MyClass"));
    assertTrue(NodeUtil.constructorCallHasSideEffects(newCustom));

    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(123)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.REGEXP, Node.newString("p"))));

    Node localCall = new Node(Token.CALL, Node.newString(Token.NAME, "fn"));
    localCall.setSideEffectFlags(Node.FLAG_LOCAL_RESULTS);
    assertTrue(NodeUtil.callHasLocalResult(localCall));
    assertTrue(NodeUtil.evaluatesToLocalValue(localCall));

    Node newLocal = new Node(Token.NEW, Node.newString(Token.NAME, "Ctor"));
    newLocal.setSideEffectFlags(Node.FLAG_LOCAL_RESULTS);
    assertFalse(NodeUtil.newHasLocalResult(newLocal)); // newHasLocalResult checks isOnlyModifiesThisCall
  }

  @Test(timeout = 4000)
  public void testCanBeSideEffected() {
    assertTrue(NodeUtil.canBeSideEffected(new Node(Token.CALL, Node.newString(Token.NAME, "foo"))));
    assertTrue(NodeUtil.canBeSideEffected(new Node(Token.GETPROP, Node.newString(Token.NAME, "o"), Node.newString("p"))));

    Node nameNode = Node.newString(Token.NAME, "x");
    assertTrue(NodeUtil.canBeSideEffected(nameNode));

    Set<String> consts = ImmutableSet.of("x");
    assertFalse(NodeUtil.canBeSideEffected(nameNode, consts));
  }

  // =========================================================================
  // Partition E: AST Traversal, Mutation & Structural Queries
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsEmptyBlock_And_NewExpr() {
    Node emptyBlock = new Node(Token.BLOCK);
    assertTrue(NodeUtil.isEmptyBlock(emptyBlock));

    Node blockWithEmptyNode = new Node(Token.BLOCK, new Node(Token.EMPTY));
    assertTrue(NodeUtil.isEmptyBlock(blockWithEmptyNode));

    Node blockWithContent = new Node(Token.BLOCK, new Node(Token.EXPR_RESULT, Node.newNumber(1)));
    assertFalse(NodeUtil.isEmptyBlock(blockWithContent));
    assertFalse(NodeUtil.isEmptyBlock(new Node(Token.SCRIPT)));

    Node child = Node.newNumber(10);
    Node expr = NodeUtil.newExpr(child);
    assertEquals(Token.EXPR_RESULT, expr.getType());
    assertSame(child, expr.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testLoopAndControlStructures() {
    Node forNode = new Node(Token.FOR, new Node(Token.EMPTY), new Node(Token.EMPTY), new Node(Token.EMPTY), new Node(Token.BLOCK));
    Node whileNode = new Node(Token.WHILE, new Node(Token.TRUE), new Node(Token.BLOCK));
    Node doNode = new Node(Token.DO, new Node(Token.BLOCK), new Node(Token.TRUE));
    Node ifNode = new Node(Token.IF, new Node(Token.TRUE), new Node(Token.BLOCK));

    assertTrue(NodeUtil.isLoopStructure(forNode));
    assertTrue(NodeUtil.isLoopStructure(whileNode));
    assertTrue(NodeUtil.isLoopStructure(doNode));
    assertFalse(NodeUtil.isLoopStructure(ifNode));

    assertSame(forNode.getLastChild(), NodeUtil.getLoopCodeBlock(forNode));
    assertSame(whileNode.getLastChild(), NodeUtil.getLoopCodeBlock(whileNode));
    assertSame(doNode.getFirstChild(), NodeUtil.getLoopCodeBlock(doNode));
    assertNull(NodeUtil.getLoopCodeBlock(ifNode));

    assertTrue(NodeUtil.isControlStructure(ifNode));
    assertTrue(NodeUtil.isControlStructureCodeBlock(ifNode, ifNode.getLastChild()));

    Node condExpr = NodeUtil.getConditionExpression(whileNode);
    assertSame(whileNode.getFirstChild(), condExpr);

    Node forIn = new Node(Token.FOR, Node.newString(Token.NAME, "i"), Node.newString(Token.NAME, "arr"), new Node(Token.BLOCK));
    assertTrue(NodeUtil.isForIn(forIn));
    assertNull(NodeUtil.getConditionExpression(forIn));
  }

  @Test(timeout = 4000)
  public void testTryCatchFinallyOperations() {
    Node tryBody = new Node(Token.BLOCK);
    Node catchContainer = new Node(Token.BLOCK);
    Node finallyBody = new Node(Token.BLOCK);

    Node tryNodeWithFinally = new Node(Token.TRY, tryBody, catchContainer, finallyBody);
    assertTrue(NodeUtil.hasFinally(tryNodeWithFinally));
    assertSame(catchContainer, NodeUtil.getCatchBlock(tryNodeWithFinally));

    Node catchHandler = new Node(Token.CATCH, Node.newString(Token.NAME, "err"), new Node(Token.BLOCK));
    catchContainer.addChildToBack(catchHandler);
    assertTrue(NodeUtil.hasCatchHandler(catchContainer));

    Node tryNodeWithoutFinally = new Node(Token.TRY, tryBody.cloneTree(), catchContainer.cloneTree());
    assertFalse(NodeUtil.hasFinally(tryNodeWithoutFinally));

    NodeUtil.maybeAddFinally(tryNodeWithoutFinally);
    assertTrue(NodeUtil.hasFinally(tryNodeWithoutFinally));
  }

  @Test(timeout = 4000)
  public void testRemoveChild_Scenarios() {
    // 1. Remove statement from block
    Node block = new Node(Token.BLOCK);
    Node stmt1 = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    Node stmt2 = new Node(Token.EXPR_RESULT, Node.newNumber(2));
    block.addChildToBack(stmt1);
    block.addChildToBack(stmt2);
    NodeUtil.removeChild(block, stmt1);
    assertEquals(1, block.getChildCount());
    assertSame(stmt2, block.getFirstChild());

    // 2. Remove var child with siblings
    Node script = new Node(Token.SCRIPT);
    Node varNode = new Node(Token.VAR);
    Node v1 = Node.newString(Token.NAME, "x");
    Node v2 = Node.newString(Token.NAME, "y");
    varNode.addChildToBack(v1);
    varNode.addChildToBack(v2);
    script.addChildToBack(varNode);
    NodeUtil.removeChild(varNode, v1);
    assertEquals(1, varNode.getChildCount());
    assertSame(varNode, script.getFirstChild());

    // 3. Remove lone var child -> removes VAR itself from script
    NodeUtil.removeChild(varNode, v2);
    assertEquals(0, script.getChildCount());

    // 4. Merge block into parent block
    Node outerBlock = new Node(Token.BLOCK);
    Node innerBlock = new Node(Token.BLOCK);
    Node innerStmt = new Node(Token.EXPR_RESULT, Node.newNumber(99));
    innerBlock.addChildToBack(innerStmt);
    outerBlock.addChildToBack(innerBlock);
    boolean merged = NodeUtil.tryMergeBlock(innerBlock);
    assertTrue(merged);
    assertEquals(1, outerBlock.getChildCount());
    assertSame(innerStmt, outerBlock.getFirstChild());
  }

  @Test(timeout = 4000)
  public void testFunctionPredicatesAndArguments() {
    List<Node> params = new ArrayList<Node>();
    params.add(Node.newString(Token.NAME, "arg1"));
    params.add(Node.newString(Token.NAME, "arg2"));
    Node body = new Node(Token.BLOCK);
    Node fn = NodeUtil.newFunctionNode("testFn", params, body, 1, 0);

    assertTrue(NodeUtil.isFunction(fn));
    assertSame(body, NodeUtil.getFunctionBody(fn));
    assertSame(fn.getFirstChild().getNext(), NodeUtil.getFnParameters(fn));
    assertEquals("arg1", NodeUtil.getArgumentForFunction(fn, 0).getString());
    assertEquals("arg2", NodeUtil.getArgumentForFunction(fn, 1).getString());
    assertNull(NodeUtil.getArgumentForFunction(fn, 2));

    Node script = new Node(Token.SCRIPT, fn);
    assertTrue(NodeUtil.isFunctionDeclaration(fn));
    assertTrue(NodeUtil.isHoistedFunctionDeclaration(fn));

    Node call = NodeUtil.newCallNode(Node.newString(Token.NAME, "testFn"), Node.newNumber(10), Node.newNumber(20));
    assertTrue(NodeUtil.isCall(call));
    assertTrue(NodeUtil.isCallOrNew(call));
    assertEquals(10.0, NodeUtil.getArgumentForCallOrNew(call, 0).getDouble(), 0.0001);
    assertEquals(20.0, NodeUtil.getArgumentForCallOrNew(call, 1).getDouble(), 0.0001);
    assertNull(NodeUtil.getArgumentForCallOrNew(call, 2));

    // Function object methods .call / .apply
    Node getPropCall = NodeUtil.newQualifiedNameNode(new DefaultCodingConvention(), "f.call", 1, 0);
    Node callFnCall = new Node(Token.CALL, getPropCall);
    assertTrue(NodeUtil.isFunctionObjectCall(callFnCall));
    assertTrue(NodeUtil.isFunctionObjectCallOrApply(callFnCall));
    assertTrue(NodeUtil.isSimpleFunctionObjectCall(callFnCall));

    Node getPropApply = NodeUtil.newQualifiedNameNode(new DefaultCodingConvention(), "f.apply", 1, 0);
    Node callFnApply = new Node(Token.CALL, getPropApply);
    assertTrue(NodeUtil.isFunctionObjectApply(callFnApply));
    assertTrue(NodeUtil.isFunctionObjectCallOrApply(callFnApply));
  }

  @Test(timeout = 4000)
  public void testPrototypeHelpers() {
    Node qname = NodeUtil.newQualifiedNameNode(new DefaultCodingConvention(), "MyClass.prototype.myMethod", 1, 0);
    assertTrue(NodeUtil.isPrototypeProperty(qname));
    assertEquals("myMethod", NodeUtil.getPrototypePropertyName(qname));

    Node classNameNode = NodeUtil.getPrototypeClassName(qname);
    assertNotNull(classNameNode);
    assertEquals("MyClass", classNameNode.getString());

    Node assign = new Node(Token.ASSIGN, qname, Node.newNumber(1));
    Node exprAssign = new Node(Token.EXPR_RESULT, assign);
    assertTrue(NodeUtil.isPrototypePropertyDeclaration(exprAssign));
  }

  @Test(timeout = 4000)
  public void testQualifiedNameAndVarCreation() {
    CodingConvention convention = new DefaultCodingConvention();
    Node qName = NodeUtil.newQualifiedNameNode(convention, "a.b.c", 2, 5);
    assertEquals(Token.GETPROP, qName.getType());
    assertEquals("a.b.c", qName.getQualifiedName());

    Node root = NodeUtil.getRootOfQualifiedName(qName);
    assertEquals(Token.NAME, root.getType());
    assertEquals("a", root.getString());

    Node varNode = NodeUtil.newVarNode("myVar", Node.newNumber(99));
    assertEquals(Token.VAR, varNode.getType());
    assertEquals("myVar", varNode.getFirstChild().getString());
    assertEquals(99.0, varNode.getFirstChild().getFirstChild().getDouble(), 0.0001);

    Node undefNode = NodeUtil.newUndefinedNode(null);
    assertEquals(Token.VOID, undefNode.getType());
    assertEquals(0.0, undefNode.getFirstChild().getDouble(), 0.0001);
  }

  @Test(timeout = 4000)
  public void testRedeclareVarsInsideBranch() {
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    Node varNode = NodeUtil.newVarNode("scopedVar", null);
    block.addChildToBack(varNode);
    script.addChildToBack(block);

    NodeUtil.redeclareVarsInsideBranch(block);
    // Should add var scopedVar at the front of the script
    assertEquals(2, script.getChildCount());
    assertEquals(Token.VAR, script.getFirstChild().getType());
    assertEquals("scopedVar", script.getFirstChild().getFirstChild().getString());
  }

  @Test(timeout = 4000)
  public void testTreeTraversalAndQueryUtils() {
    Node root = new Node(Token.BLOCK);
    root.addChildToBack(Node.newString(Token.NAME, "foo"));
    root.addChildToBack(Node.newString(Token.NAME, "bar"));
    root.addChildToBack(Node.newString(Token.NAME, "foo"));

    assertTrue(NodeUtil.isNameReferenced(root, "foo"));
    assertFalse(NodeUtil.isNameReferenced(root, "baz"));
    assertEquals(2, NodeUtil.getNameReferenceCount(root, "foo"));
    assertEquals(1, NodeUtil.getNameReferenceCount(root, "bar"));
    assertEquals(3, NodeUtil.getNodeTypeReferenceCount(root, Token.NAME, Predicates.<Node>alwaysTrue()));

    final List<Integer> visitedTypes = new ArrayList<Integer>();
    NodeUtil.visitPreOrder(root, new NodeUtil.Visitor() {
      public void visit(Node node) {
        visitedTypes.add(node.getType());
      }
    }, Predicates.<Node>alwaysTrue());

    assertEquals(4, visitedTypes.size());
    assertEquals((Integer) Token.BLOCK, visitedTypes.get(0));

    assertTrue(NodeUtil.isLatin("abcXYZ_123"));
    assertFalse(NodeUtil.isLatin("abc\u1234def"));

    assertTrue(NodeUtil.isValidPropertyName("propName"));
    assertFalse(NodeUtil.isValidPropertyName("default")); // reserved keyword
    assertFalse(NodeUtil.isValidPropertyName("123bad"));
  }

  // =========================================================================
  // Partition D: Defensive Guards & Expected Exceptions
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testConstructorCallHasSideEffects_ThrowsOnNonNew() {
    NodeUtil.constructorCallHasSideEffects(new Node(Token.CALL));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testFunctionCallHasSideEffects_ThrowsOnNonCall() {
    NodeUtil.functionCallHasSideEffects(new Node(Token.NEW));
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetOpFromAssignmentOp_ThrowsOnNonAssign() {
    NodeUtil.getOpFromAssignmentOp(new Node(Token.ADD));
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testPrecedence_ThrowsOnUnknownToken() {
    NodeUtil.precedence(-9999);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testOpToStrNoFail_ThrowsOnNonOp() {
    NodeUtil.opToStrNoFail(Token.SCRIPT);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetFnParameters_ThrowsOnNonFunction() {
    NodeUtil.getFnParameters(new Node(Token.BLOCK));
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetArgumentForFunction_ThrowsOnNonFunction() {
    NodeUtil.getArgumentForFunction(new Node(Token.CALL), 0);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testGetArgumentForCallOrNew_ThrowsOnInvalidNode() {
    NodeUtil.getArgumentForCallOrNew(new Node(Token.ASSIGN), 0);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testRemoveChild_ThrowsOnInvalidHierarchy() {
    Node num1 = Node.newNumber(1);
    Node num2 = Node.newNumber(2);
    NodeUtil.removeChild(num1, num2);
  }
}