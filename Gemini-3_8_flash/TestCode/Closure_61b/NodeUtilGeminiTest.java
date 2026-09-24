package com.google.javascript.jscomp;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.jstype.TernaryValue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target: com.google.javascript.jscomp.NodeUtil
 *
 * Defect Ground Truth:
 * - PeepholeRemoveDeadCodeTest::testCall1, testCall2, testRemoveUselessOps failed because
 *   calls in the "Math" namespace (such as Math.sin(0)) were incorrectly classified as
 *   having side effects due to missing logic under Token.GETPROP in functionCallHasSideEffects().
 *   The comment "// Functions in the "Math" namespace have no side effects." existed without
 *   the corresponding implementation.
 *
 * Equivalence Partitioning & Decision Branch Matrix:
 * - Partition A (Core Logic):
 *   * getImpureBooleanValue / getPureBooleanValue (ASSIGN, COMMA, NOT, AND, OR, HOOK, Literals, Names)
 *   * getStringValue / arrayToString / getArrayElementStringValue
 *   * getNumberValue / getStringNumberValue / trimJsWhiteSpace / isStrWhiteSpaceChar
 *   * getFunctionName / getNearestFunctionName (NAME, ASSIGN, Object lit keys)
 *   * isImmutableValue / isLiteralValue / isValidDefineValue
 *   * isSimpleOperator / isSimpleOperatorType / precedence across all 15 precedence levels
 *   * isNumericResult / isBooleanResult / mayBeString / isUndefined / isNull / isNullOrUndefined
 *   * isAssociative / isCommutative / isAssignmentOp / getOpFromAssignmentOp
 * - Partition B (Boundary & Extremes):
 *   * Long string conversions, double-to-integer string representation ("1" vs "1.0")
 *   * Hex conversions ("0x10", "0X1F", "0xZZ"), signed hex ("-0x10", "+0x10"), case insensitivity ("Infinity")
 *   * Whitespace chars: vertical tab (\u000B), BOM (\uFEFF), line separators (\u2028, \u2029), nbsp (\u00A0)
 *   * isLatin / isValidPropertyName on ASCII vs multi-byte Unicode
 * - Partition C (Defects4J Regression Zone):
 *   * functionCallHasSideEffects / mayHaveSideEffects for Math namespace calls (Math.sin, Math.cos)
 * - Partition D (Defensive & Guard Paths):
 *   * constructorCallHasSideEffects with non-NEW tokens -> IllegalStateException
 *   * functionCallHasSideEffects with non-CALL tokens -> IllegalStateException
 *   * callHasLocalResult with non-CALL tokens -> IllegalStateException / Precondition failure
 *   * newHasLocalResult with non-NEW tokens -> IllegalStateException / Precondition failure
 *   * getOpFromAssignmentOp with non-assignment tokens -> IllegalArgumentException
 *   * precedence with unknown token types -> Error
 *   * opToStrNoFail with unknown operator tokens -> Error
 *   * removeChild with invalid AST hierarchies -> IllegalStateException
 * - Partition E (AST Traversal & Structural Manipulation):
 *   * removeChild for Try-Catch-Finally, blocks, VARs, Labels, FOR-loops
 *   * tryMergeBlock for block inlining into statement blocks
 *   * newQualifiedNameNode, getRootOfQualifiedName, newFunctionNode, newVarNode, newCallNode
 *   * visitPreOrder, visitPostOrder, getCount, has, isNameReferenced, getNodeTypeReferenceCount
 */
public class NodeUtilGeminiTest {

  // =========================================================================
  // PARTITION A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetPureAndImpureBooleanValuePrimitives() {
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(new Node(Token.TRUE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.FALSE)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.NULL)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(new Node(Token.VOID, Node.newNumber(0))));

    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newString("hello")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString("")));

    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newNumber(42)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newNumber(0)));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newNumber(-0.0)));

    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "undefined")));
    assertEquals(TernaryValue.FALSE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "NaN")));
    assertEquals(TernaryValue.TRUE, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getPureBooleanValue(Node.newString(Token.NAME, "otherVar")));
  }

  @Test(timeout = 4000)
  public void testGetImpureBooleanValueComplexExpressions() {
    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(notTrue));

    Node andNode = new Node(Token.AND, new Node(Token.TRUE), Node.newNumber(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(andNode));

    Node orNode = new Node(Token.OR, new Node(Token.FALSE), Node.newString("val"));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(orNode));

    Node hookBothTrue = new Node(Token.HOOK,
        Node.newString(Token.NAME, "cond"),
        new Node(Token.TRUE),
        Node.newNumber(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(hookBothTrue));

    Node hookDivergent = new Node(Token.HOOK,
        Node.newString(Token.NAME, "cond"),
        new Node(Token.TRUE),
        new Node(Token.FALSE));
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.getImpureBooleanValue(hookDivergent));

    Node commaNode = new Node(Token.COMMA, Node.newNumber(0), Node.newNumber(1));
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(commaNode));

    Node assignNode = new Node(Token.ASSIGN, Node.newString(Token.NAME, "x"), new Node(Token.FALSE));
    assertEquals(TernaryValue.FALSE, NodeUtil.getImpureBooleanValue(assignNode));

    Node arrLit = new Node(Token.ARRAYLIT);
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(arrLit));
    Node objLit = new Node(Token.OBJECTLIT);
    assertEquals(TernaryValue.TRUE, NodeUtil.getImpureBooleanValue(objLit));
  }

  @Test(timeout = 4000)
  public void testGetStringValueConversions() {
    assertEquals("test", NodeUtil.getStringValue(Node.newString("test")));
    assertEquals("undefined", NodeUtil.getStringValue(Node.newString(Token.NAME, "undefined")));
    assertEquals("Infinity", NodeUtil.getStringValue(Node.newString(Token.NAME, "Infinity")));
    assertEquals("NaN", NodeUtil.getStringValue(Node.newString(Token.NAME, "NaN")));
    assertNull(NodeUtil.getStringValue(Node.newString(Token.NAME, "foo")));

    assertEquals("true", NodeUtil.getStringValue(new Node(Token.TRUE)));
    assertEquals("false", NodeUtil.getStringValue(new Node(Token.FALSE)));
    assertEquals("null", NodeUtil.getStringValue(new Node(Token.NULL)));
    assertEquals("undefined", NodeUtil.getStringValue(new Node(Token.VOID, Node.newNumber(0))));

    assertEquals("100", NodeUtil.getStringValue(100.0));
    assertEquals("100.5", NodeUtil.getStringValue(100.5));
    assertEquals("100", NodeUtil.getStringValue(Node.newNumber(100.0)));

    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    assertEquals("true", NodeUtil.getStringValue(notFalse));
    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    assertEquals("false", NodeUtil.getStringValue(notTrue));

    Node arrayLit = new Node(Token.ARRAYLIT,
        Node.newNumber(1),
        new Node(Token.NULL),
        new Node(Token.EMPTY),
        Node.newString("end"));
    assertEquals("1,,,end", NodeUtil.getStringValue(arrayLit));

    Node emptyArray = new Node(Token.ARRAYLIT);
    assertEquals("", NodeUtil.getStringValue(emptyArray));

    Node objLit = new Node(Token.OBJECTLIT);
    assertEquals("[object Object]", NodeUtil.getStringValue(objLit));
  }

  @Test(timeout = 4000)
  public void testGetNumberValueConversions() {
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(new Node(Token.TRUE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.FALSE)));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(new Node(Token.NULL)));
    assertEquals(Double.valueOf(3.14), NodeUtil.getNumberValue(Node.newNumber(3.14)));

    Node voidPure = new Node(Token.VOID, Node.newNumber(0));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(voidPure)));

    Node voidImpure = new Node(Token.VOID, new Node(Token.CALL, Node.newString(Token.NAME, "sideEffect")));
    assertNull(NodeUtil.getNumberValue(voidImpure));

    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "undefined"))));
    assertTrue(Double.isNaN(NodeUtil.getNumberValue(Node.newString(Token.NAME, "NaN"))));
    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY),
        NodeUtil.getNumberValue(Node.newString(Token.NAME, "Infinity")));
    assertNull(NodeUtil.getNumberValue(Node.newString(Token.NAME, "other")));

    Node negInf = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
    assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), NodeUtil.getNumberValue(negInf));
    Node negOther = new Node(Token.NEG, Node.newString(Token.NAME, "Foo"));
    assertNull(NodeUtil.getNumberValue(negOther));

    Node notZero = new Node(Token.NOT, Node.newNumber(0));
    assertEquals(Double.valueOf(0.0), NodeUtil.getNumberValue(notZero));
    Node notOne = new Node(Token.NOT, Node.newNumber(1));
    assertEquals(Double.valueOf(1.0), NodeUtil.getNumberValue(notOne));
  }

  @Test(timeout = 4000)
  public void testGetFunctionNameAndNearestFunctionName() {
    Node fn = new Node(Token.FUNCTION, Node.newString(Token.NAME, "foo"), new Node(Token.LP), new Node(Token.BLOCK));
    assertEquals("foo", NodeUtil.getFunctionName(fn));

    Node anonFn = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    assertNull(NodeUtil.getFunctionName(anonFn));

    Node varName = Node.newString(Token.NAME, "v");
    varName.addChildToBack(anonFn);
    Node varNode = new Node(Token.VAR, varName);
    assertEquals("v", NodeUtil.getFunctionName(anonFn));

    Node anonFn2 = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node qName = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newString(Token.STRING, "b"));
    Node assign = new Node(Token.ASSIGN, qName, anonFn2);
    assertEquals("a.b", NodeUtil.getFunctionName(anonFn2));

    Node anonFn3 = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node strKey = Node.newString(Token.STRING, "keyProp");
    strKey.addChildToBack(anonFn3);
    Node objLit = new Node(Token.OBJECTLIT, strKey);
    assertEquals("keyProp", NodeUtil.getNearestFunctionName(anonFn3));

    Node anonFn4 = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    Node numKey = Node.newNumber(123);
    numKey.addChildToBack(anonFn4);
    Node objLit2 = new Node(Token.OBJECTLIT, numKey);
    assertEquals("123", NodeUtil.getNearestFunctionName(anonFn4));
  }

  @Test(timeout = 4000)
  public void testIsImmutableAndLiteralValue() {
    assertTrue(NodeUtil.isImmutableValue(Node.newString("str")));
    assertTrue(NodeUtil.isImmutableValue(Node.newNumber(10)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NULL)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.FALSE)));
    assertTrue(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "undefined")));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NOT, Node.newNumber(1))));
    assertTrue(NodeUtil.isImmutableValue(new Node(Token.NEG, Node.newNumber(1))));
    assertFalse(NodeUtil.isImmutableValue(Node.newString(Token.NAME, "x")));

    Node arrLit = new Node(Token.ARRAYLIT, Node.newNumber(1), Node.newString("s"));
    assertTrue(NodeUtil.isLiteralValue(arrLit, false));

    Node arrLitNonConst = new Node(Token.ARRAYLIT, Node.newString(Token.NAME, "variable"));
    assertFalse(NodeUtil.isLiteralValue(arrLitNonConst, false));

    Node regexLit = new Node(Token.REGEXP, Node.newString("pattern"));
    assertTrue(NodeUtil.isLiteralValue(regexLit, false));

    Node fnExp = new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK));
    new Node(Token.EXPR_RESULT, fnExp);
    assertTrue(NodeUtil.isLiteralValue(fnExp, true));
    assertFalse(NodeUtil.isLiteralValue(fnExp, false));
  }

  @Test(timeout = 4000)
  public void testIsValidDefineValue() {
    Set<String> defines = new HashSet<>(Arrays.asList("DEF_A", "ns.DEF_B"));

    assertTrue(NodeUtil.isValidDefineValue(Node.newNumber(42), defines));
    assertTrue(NodeUtil.isValidDefineValue(Node.newString("text"), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.TRUE), defines));
    assertTrue(NodeUtil.isValidDefineValue(new Node(Token.FALSE), defines));

    Node add = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isValidDefineValue(add, defines));

    Node neg = new Node(Token.NEG, Node.newNumber(5));
    assertTrue(NodeUtil.isValidDefineValue(neg, defines));

    Node defA = Node.newString(Token.NAME, "DEF_A");
    assertTrue(NodeUtil.isValidDefineValue(defA, defines));

    Node defB = new Node(Token.GETPROP, Node.newString(Token.NAME, "ns"), Node.newString(Token.STRING, "DEF_B"));
    assertTrue(NodeUtil.isValidDefineValue(defB, defines));

    Node unknownName = Node.newString(Token.NAME, "UNKNOWN");
    assertFalse(NodeUtil.isValidDefineValue(unknownName, defines));

    Node invalidOp = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    assertFalse(NodeUtil.isValidDefineValue(invalidOp, defines));
  }

  @Test(timeout = 4000)
  public void testPrecedenceAndSimpleOperators() {
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
    assertEquals(8, NodeUtil.precedence(Token.SHEQ));
    assertEquals(9, NodeUtil.precedence(Token.LT));
    assertEquals(9, NodeUtil.precedence(Token.INSTANCEOF));
    assertEquals(9, NodeUtil.precedence(Token.IN));
    assertEquals(10, NodeUtil.precedence(Token.LSH));
    assertEquals(10, NodeUtil.precedence(Token.URSH));
    assertEquals(11, NodeUtil.precedence(Token.ADD));
    assertEquals(11, NodeUtil.precedence(Token.SUB));
    assertEquals(12, NodeUtil.precedence(Token.MUL));
    assertEquals(12, NodeUtil.precedence(Token.DIV));
    assertEquals(12, NodeUtil.precedence(Token.MOD));
    assertEquals(13, NodeUtil.precedence(Token.NOT));
    assertEquals(13, NodeUtil.precedence(Token.VOID));
    assertEquals(13, NodeUtil.precedence(Token.DELPROP));
    assertEquals(15, NodeUtil.precedence(Token.CALL));
    assertEquals(15, NodeUtil.precedence(Token.NUMBER));

    assertTrue(NodeUtil.isSimpleOperatorType(Token.ADD));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.TYPEOF));
    assertTrue(NodeUtil.isSimpleOperatorType(Token.INSTANCEOF));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.HOOK));
    assertFalse(NodeUtil.isSimpleOperatorType(Token.ASSIGN));
  }

  @Test(timeout = 4000)
  public void testNumericBooleanAndStringPredicates() {
    Node addNum = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    assertTrue(NodeUtil.isNumericResult(addNum));
    assertFalse(NodeUtil.mayBeString(addNum));

    Node addStr = new Node(Token.ADD, Node.newString("a"), Node.newNumber(2));
    assertFalse(NodeUtil.isNumericResult(addStr));
    assertTrue(NodeUtil.mayBeString(addStr));

    assertTrue(NodeUtil.isNumericResult(new Node(Token.BITNOT, Node.newNumber(1))));
    assertTrue(NodeUtil.isNumericResult(Node.newString(Token.NAME, "NaN")));
    assertTrue(NodeUtil.isNumericResult(Node.newString(Token.NAME, "Infinity")));
    assertFalse(NodeUtil.isNumericResult(Node.newString(Token.NAME, "other")));

    assertTrue(NodeUtil.isBooleanResult(new Node(Token.TRUE)));
    assertTrue(NodeUtil.isBooleanResult(new Node(Token.EQ, Node.newNumber(1), Node.newNumber(2))));
    assertTrue(NodeUtil.isBooleanResult(new Node(Token.NOT, Node.newNumber(1))));
    assertTrue(NodeUtil.isBooleanResult(new Node(Token.DELPROP, Node.newString(Token.NAME, "x"))));
    assertFalse(NodeUtil.isBooleanResult(Node.newNumber(1)));

    assertTrue(NodeUtil.isUndefined(new Node(Token.VOID, Node.newNumber(0))));
    assertTrue(NodeUtil.isUndefined(Node.newString(Token.NAME, "undefined")));
    assertFalse(NodeUtil.isUndefined(Node.newString(Token.NAME, "defined")));

    assertTrue(NodeUtil.isNull(new Node(Token.NULL)));
    assertFalse(NodeUtil.isNull(new Node(Token.VOID, Node.newNumber(0))));

    assertTrue(NodeUtil.isNullOrUndefined(new Node(Token.NULL)));
    assertTrue(NodeUtil.isNullOrUndefined(new Node(Token.VOID, Node.newNumber(0))));
    assertFalse(NodeUtil.isNullOrUndefined(Node.newNumber(0)));
  }

  @Test(timeout = 4000)
  public void testAssociativeCommutativeAndAssignmentOps() {
    assertTrue(NodeUtil.isAssociative(Token.MUL));
    assertTrue(NodeUtil.isAssociative(Token.AND));
    assertTrue(NodeUtil.isAssociative(Token.OR));
    assertFalse(NodeUtil.isAssociative(Token.ADD));
    assertFalse(NodeUtil.isAssociative(Token.SUB));

    assertTrue(NodeUtil.isCommutative(Token.MUL));
    assertTrue(NodeUtil.isCommutative(Token.BITOR));
    assertFalse(NodeUtil.isCommutative(Token.AND));
    assertFalse(NodeUtil.isCommutative(Token.ADD));

    Node assignAdd = new Node(Token.ASSIGN_ADD, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertTrue(NodeUtil.isAssignmentOp(assignAdd));
    assertEquals(Token.ADD, NodeUtil.getOpFromAssignmentOp(assignAdd));

    Node assignBitOr = new Node(Token.ASSIGN_BITOR, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.BITOR, NodeUtil.getOpFromAssignmentOp(assignBitOr));
    Node assignBitXor = new Node(Token.ASSIGN_BITXOR, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.BITXOR, NodeUtil.getOpFromAssignmentOp(assignBitXor));
    Node assignBitAnd = new Node(Token.ASSIGN_BITAND, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.BITAND, NodeUtil.getOpFromAssignmentOp(assignBitAnd));
    Node assignLsh = new Node(Token.ASSIGN_LSH, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.LSH, NodeUtil.getOpFromAssignmentOp(assignLsh));
    Node assignRsh = new Node(Token.ASSIGN_RSH, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.RSH, NodeUtil.getOpFromAssignmentOp(assignRsh));
    Node assignUrsh = new Node(Token.ASSIGN_URSH, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.URSH, NodeUtil.getOpFromAssignmentOp(assignUrsh));
    Node assignSub = new Node(Token.ASSIGN_SUB, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.SUB, NodeUtil.getOpFromAssignmentOp(assignSub));
    Node assignMul = new Node(Token.ASSIGN_MUL, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.MUL, NodeUtil.getOpFromAssignmentOp(assignMul));
    Node assignDiv = new Node(Token.ASSIGN_DIV, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.DIV, NodeUtil.getOpFromAssignmentOp(assignDiv));
    Node assignMod = new Node(Token.ASSIGN_MOD, Node.newString(Token.NAME, "x"), Node.newNumber(1));
    assertEquals(Token.MOD, NodeUtil.getOpFromAssignmentOp(assignMod));
  }

  // =========================================================================
  // PARTITION B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testGetStringNumberValueBoundaries() {
    assertNull(NodeUtil.getStringNumberValue("abc\u000Bdef"));
    assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue(""));
    assertEquals(Double.valueOf(0.0), NodeUtil.getStringNumberValue("   \t\n\r  "));

    assertEquals(Double.valueOf(16.0), NodeUtil.getStringNumberValue("0x10"));
    assertEquals(Double.valueOf(31.0), NodeUtil.getStringNumberValue("0X1f"));
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("0xZZZZ")));

    assertNull(NodeUtil.getStringNumberValue("+0x10"));
    assertNull(NodeUtil.getStringNumberValue("-0x10"));

    assertNull(NodeUtil.getStringNumberValue("infinity"));
    assertNull(NodeUtil.getStringNumberValue("-infinity"));
    assertNull(NodeUtil.getStringNumberValue("+infinity"));

    assertEquals(Double.valueOf(Double.POSITIVE_INFINITY), NodeUtil.getStringNumberValue("Infinity"));
    assertEquals(Double.valueOf(Double.NEGATIVE_INFINITY), NodeUtil.getStringNumberValue("-Infinity"));
    assertEquals(Double.valueOf(123.45), NodeUtil.getStringNumberValue("  123.45  "));
    assertTrue(Double.isNaN(NodeUtil.getStringNumberValue("not-a-number")));
  }

  @Test(timeout = 4000)
  public void testIsStrWhiteSpaceCharBoundaries() {
    assertEquals(TernaryValue.UNKNOWN, NodeUtil.isStrWhiteSpaceChar('\u000B'));
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
    assertEquals(TernaryValue.FALSE, NodeUtil.isStrWhiteSpaceChar('0'));
  }

  @Test(timeout = 4000)
  public void testIsLatinAndPropertyNameBoundaries() {
    assertTrue(NodeUtil.isLatin("ValidIdentifier123_$"));
    assertFalse(NodeUtil.isLatin("IdentifierWithUnicode\u4e16\u754c"));
    assertFalse(NodeUtil.isLatin("\u0080"));

    assertTrue(NodeUtil.isValidPropertyName("validProp"));
    assertTrue(NodeUtil.isValidPropertyName("$foo_bar"));
    assertFalse(NodeUtil.isValidPropertyName("class")); // JS keyword
    assertFalse(NodeUtil.isValidPropertyName("123bad")); // Starts with digit
    assertFalse(NodeUtil.isValidPropertyName("unicode\u0100")); // Non-latin
  }

  @Test(timeout = 4000)
  public void testOperatorToStringConversions() {
    assertEquals("+", NodeUtil.opToStr(Token.ADD));
    assertEquals("-", NodeUtil.opToStr(Token.SUB));
    assertEquals("===", NodeUtil.opToStr(Token.SHEQ));
    assertEquals("!==", NodeUtil.opToStr(Token.SHNE));
    assertEquals("void", NodeUtil.opToStr(Token.VOID));
    assertEquals("typeof", NodeUtil.opToStr(Token.TYPEOF));
    assertEquals("instanceof", NodeUtil.opToStr(Token.INSTANCEOF));
    assertNull(NodeUtil.opToStr(Token.BLOCK));

    assertEquals("+", NodeUtil.opToStrNoFail(Token.ADD));
  }

  // =========================================================================
  // PARTITION C: Defect-Targeted Branch Zone
  // Ground Truth: PeepholeRemoveDeadCodeTest::testCall1, testCall2, testRemoveUselessOps
  // Target: NodeUtil.functionCallHasSideEffects / NodeUtil.mayHaveSideEffects
  // for calls in the Math namespace (e.g. Math.sin(0))
  // =========================================================================

  @Test(timeout = 4000)
  public void testDefectMathFunctionCallHasNoSideEffects() {
    Node mathName = Node.newString(Token.NAME, "Math");
    Node sinProp = Node.newString(Token.STRING, "sin");
    Node getProp = new Node(Token.GETPROP, mathName, sinProp);
    Node callNode = new Node(Token.CALL, getProp, Node.newNumber(0));

    // The specification mandates that Math namespace functions have no side effects.
    // In the defective version, missing handling under GETPROP causes this to return true.
    assertFalse("Math.sin(0) must be recognized as having no side effects",
        NodeUtil.functionCallHasSideEffects(callNode));
    assertFalse("mayHaveSideEffects should return false for Math.sin(0)",
        NodeUtil.mayHaveSideEffects(callNode));
  }

  @Test(timeout = 4000)
  public void testDefectMathFunctionCallInBinaryOpNoSideEffects() {
    Node mathName = Node.newString(Token.NAME, "Math");
    Node cosProp = Node.newString(Token.STRING, "cos");
    Node getProp = new Node(Token.GETPROP, mathName, cosProp);
    Node mathCall = new Node(Token.CALL, getProp, Node.newNumber(1.0));
    Node addExpr = new Node(Token.ADD, Node.newNumber(1), mathCall);

    // Expressions containing only pure Math calls and pure literals have no side effects.
    assertFalse("1 + Math.cos(1) should have no side effects",
        NodeUtil.mayHaveSideEffects(addExpr));
  }

  // =========================================================================
  // PARTITION D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testConstructorCallHasSideEffectsThrowsOnNonNew() {
    Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "MyClass"));
    NodeUtil.constructorCallHasSideEffects(callNode);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testFunctionCallHasSideEffectsThrowsOnNonCall() {
    Node newNode = new Node(Token.NEW, Node.newString(Token.NAME, "MyClass"));
    NodeUtil.functionCallHasSideEffects(newNode);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testCallHasLocalResultThrowsOnNonCall() {
    Node newNode = new Node(Token.NEW, Node.newString(Token.NAME, "Foo"));
    NodeUtil.callHasLocalResult(newNode);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testNewHasLocalResultThrowsOnNonNew() {
    Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "foo"));
    NodeUtil.newHasLocalResult(callNode);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetOpFromAssignmentOpThrowsOnNonAssignOp() {
    Node addNode = new Node(Token.ADD, Node.newNumber(1), Node.newNumber(2));
    NodeUtil.getOpFromAssignmentOp(addNode);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testPrecedenceThrowsOnUnknownToken() {
    NodeUtil.precedence(-9999);
  }

  @Test(expected = Error.class, timeout = 4000)
  public void testOpToStrNoFailThrowsOnNonOpToken() {
    NodeUtil.opToStrNoFail(Token.SCRIPT);
  }

  @Test(expected = IllegalStateException.class, timeout = 4000)
  public void testRemoveChildThrowsOnInvalidHierarchy() {
    Node root = new Node(Token.NAME, "root");
    Node child = new Node(Token.NAME, "child");
    root.addChildToBack(child);
    NodeUtil.removeChild(root, child);
  }

  @Test(expected = IllegalArgumentException.class, timeout = 4000)
  public void testGetConditionExpressionThrowsOnInvalidNode() {
    Node nameNode = Node.newString(Token.NAME, "a");
    NodeUtil.getConditionExpression(nameNode);
  }

  // =========================================================================
  // PARTITION E: AST Lifecycle, Control Structures & Traversal Integrity
  // =========================================================================

  @Test(timeout = 4000)
  public void testControlStructuresAndConditionExpression() {
    assertTrue(NodeUtil.isControlStructure(new Node(Token.IF)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.WHILE)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.FOR)));
    assertTrue(NodeUtil.isControlStructure(new Node(Token.SWITCH)));
    assertFalse(NodeUtil.isControlStructure(new Node(Token.CALL)));

    assertTrue(NodeUtil.isLoopStructure(new Node(Token.FOR)));
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.DO)));
    assertTrue(NodeUtil.isLoopStructure(new Node(Token.WHILE)));
    assertFalse(NodeUtil.isLoopStructure(new Node(Token.IF)));

    Node cond = Node.newString(Token.NAME, "c");
    Node thenBlock = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF, cond, thenBlock);
    assertEquals(cond, NodeUtil.getConditionExpression(ifNode));

    Node whileBody = new Node(Token.BLOCK);
    Node whileNode = new Node(Token.WHILE, cond, whileBody);
    assertEquals(cond, NodeUtil.getConditionExpression(whileNode));
    assertEquals(whileBody, NodeUtil.getLoopCodeBlock(whileNode));

    Node doBody = new Node(Token.BLOCK);
    Node doNode = new Node(Token.DO, doBody, cond);
    assertEquals(cond, NodeUtil.getConditionExpression(doNode));
    assertEquals(doBody, NodeUtil.getLoopCodeBlock(doNode));

    Node forInit = new Node(Token.EMPTY);
    Node forCond = Node.newString(Token.NAME, "forCond");
    Node forInc = new Node(Token.EMPTY);
    Node forBody = new Node(Token.BLOCK);
    Node forNode4 = new Node(Token.FOR, forInit, forCond, forInc, forBody);
    assertEquals(forCond, NodeUtil.getConditionExpression(forNode4));
    assertEquals(forBody, NodeUtil.getLoopCodeBlock(forNode4));

    Node forInVar = Node.newString(Token.NAME, "i");
    Node forInObj = Node.newString(Token.NAME, "obj");
    Node forInBody = new Node(Token.BLOCK);
    Node forInNode = new Node(Token.FOR, forInVar, forInObj, forInBody);
    assertTrue(NodeUtil.isForIn(forInNode));
    assertNull(NodeUtil.getConditionExpression(forInNode));
  }

  @Test(timeout = 4000)
  public void testTryCatchFinallyOperations() {
    Node tryBlock = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), new Node(Token.BLOCK));
    Node catchBlock = new Node(Token.BLOCK, catchNode);
    Node finallyBlock = new Node(Token.BLOCK);
    Node tryNode = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);

    assertTrue(NodeUtil.hasFinally(tryNode));
    assertEquals(catchBlock, NodeUtil.getCatchBlock(tryNode));
    assertTrue(NodeUtil.hasCatchHandler(catchBlock));
    assertTrue(NodeUtil.isTryFinallyNode(tryNode, finallyBlock));
    assertTrue(NodeUtil.isTryCatchNodeContainer(catchBlock));

    Node tryWithoutFinally = new Node(Token.TRY, tryBlock.cloneTree(), catchBlock.cloneTree());
    assertFalse(NodeUtil.hasFinally(tryWithoutFinally));
    NodeUtil.maybeAddFinally(tryWithoutFinally);
    assertTrue(NodeUtil.hasFinally(tryWithoutFinally));
  }

  @Test(timeout = 4000)
  public void testRemoveChildScenarios() {
    // 1. Remove finally when catch exists
    Node tryBlock = new Node(Token.BLOCK);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"), new Node(Token.BLOCK));
    Node catchBlock = new Node(Token.BLOCK, catchNode);
    Node finallyBlock = new Node(Token.BLOCK);
    Node tryNode = new Node(Token.TRY, tryBlock, catchBlock, finallyBlock);

    NodeUtil.removeChild(tryNode, finallyBlock);
    assertEquals(2, tryNode.getChildCount());

    // 2. Remove statement from statement block
    Node block = new Node(Token.BLOCK);
    Node stmt1 = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    Node stmt2 = new Node(Token.EXPR_RESULT, Node.newNumber(2));
    block.addChildToBack(stmt1);
    block.addChildToBack(stmt2);

    NodeUtil.removeChild(block, stmt1);
    assertEquals(1, block.getChildCount());
    assertEquals(stmt2, block.getFirstChild());

    // 3. Remove VAR with multiple declarations
    Node v1 = Node.newString(Token.NAME, "x");
    Node v2 = Node.newString(Token.NAME, "y");
    Node varNode = new Node(Token.VAR, v1, v2);
    Node parentBlock = new Node(Token.BLOCK, varNode);

    NodeUtil.removeChild(varNode, v1);
    assertEquals(1, varNode.getChildCount());
    assertEquals(v2, varNode.getFirstChild());

    // 4. Remove VAR with single declaration (cascades to removing VAR itself)
    NodeUtil.removeChild(varNode, v2);
    assertEquals(0, parentBlock.getChildCount());
  }

  @Test(timeout = 4000)
  public void testTryMergeBlock() {
    Node script = new Node(Token.SCRIPT);
    Node block = new Node(Token.BLOCK);
    Node stmt1 = new Node(Token.EXPR_RESULT, Node.newNumber(1));
    Node stmt2 = new Node(Token.EXPR_RESULT, Node.newNumber(2));
    block.addChildToBack(stmt1);
    block.addChildToBack(stmt2);
    script.addChildToBack(block);

    boolean merged = NodeUtil.tryMergeBlock(block);
    assertTrue(merged);
    assertEquals(2, script.getChildCount());
    assertEquals(stmt1, script.getFirstChild());
    assertEquals(stmt2, script.getLastChild());

    // Non-mergeable: parent is not statement block
    Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "cond"), new Node(Token.BLOCK));
    assertFalse(NodeUtil.tryMergeBlock(ifNode.getLastChild()));
  }

  @Test(timeout = 4000)
  public void testQualifiedNameAndPrototypeUtilities() {
    CodingConvention convention = new DefaultCodingConvention();
    Node qNameNode = NodeUtil.newQualifiedNameNode(convention, "a.b.c", 1, 2);
    assertEquals(Token.GETPROP, qNameNode.getType());
    assertEquals("a.b.c", qNameNode.getQualifiedName());

    Node root = NodeUtil.getRootOfQualifiedName(qNameNode);
    assertEquals(Token.NAME, root.getType());
    assertEquals("a", root.getString());

    Node protoProp = NodeUtil.newQualifiedNameNode(convention, "MyClass.prototype.render", 1, 1);
    assertTrue(NodeUtil.isPrototypeProperty(protoProp));
    assertEquals("render", NodeUtil.getPrototypePropertyName(protoProp));
    assertEquals("MyClass", NodeUtil.getPrototypeClassName(protoProp).getQualifiedName());

    Node assignExpr = new Node(Token.EXPR_RESULT,
        new Node(Token.ASSIGN, protoProp, new Node(Token.FUNCTION, Node.newString(Token.NAME, ""), new Node(Token.LP), new Node(Token.BLOCK))));
    assertTrue(NodeUtil.isPrototypePropertyDeclaration(assignExpr));
  }

  @Test(timeout = 4000)
  public void testTreeTraversalAndQueryCounters() {
    Node fnBody = new Node(Token.BLOCK);
    fnBody.addChildToBack(new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "target")));
    fnBody.addChildToBack(new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "target")));
    fnBody.addChildToBack(new Node(Token.EXPR_RESULT, Node.newString(Token.NAME, "other")));

    assertTrue(NodeUtil.isNameReferenced(fnBody, "target"));
    assertEquals(2, NodeUtil.getNameReferenceCount(fnBody, "target"));
    assertEquals(1, NodeUtil.getNameReferenceCount(fnBody, "other"));
    assertEquals(0, NodeUtil.getNameReferenceCount(fnBody, "missing"));

    assertEquals(3, NodeUtil.getNodeTypeReferenceCount(fnBody, Token.EXPR_RESULT, Predicates.<Node>alwaysTrue()));

    final List<Integer> visitedPreOrder = new ArrayList<>();
    NodeUtil.visitPreOrder(fnBody, new NodeUtil.Visitor() {
      @Override
      public void visit(Node node) {
        visitedPreOrder.add(node.getType());
      }
    }, Predicates.<Node>alwaysTrue());
    assertEquals(Token.BLOCK, (int) visitedPreOrder.get(0));

    final List<Integer> visitedPostOrder = new ArrayList<>();
    NodeUtil.visitPostOrder(fnBody, new NodeUtil.Visitor() {
      @Override
      public void visit(Node node) {
        visitedPostOrder.add(node.getType());
      }
    }, Predicates.<Node>alwaysTrue());
    assertEquals(Token.BLOCK, (int) visitedPostOrder.get(visitedPostOrder.size() - 1));
  }

  @Test(timeout = 4000)
  public void testEvaluatesToLocalValue() {
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newNumber(10)));
    assertTrue(NodeUtil.evaluatesToLocalValue(Node.newString("literal")));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.ARRAYLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.OBJECTLIT)));
    assertTrue(NodeUtil.evaluatesToLocalValue(new Node(Token.REGEXP, Node.newString("x"))));

    Node comma = new Node(Token.COMMA, Node.newString(Token.NAME, "external"), Node.newNumber(1));
    assertTrue(NodeUtil.evaluatesToLocalValue(comma));

    Node assignLocal = new Node(Token.ASSIGN, Node.newString(Token.NAME, "local"), Node.newNumber(5));
    assertTrue(NodeUtil.evaluatesToLocalValue(assignLocal));
  }

  @Test(timeout = 4000)
  public void testArgumentExtractionForCallAndFunction() {
    Node param1 = Node.newString(Token.NAME, "p1");
    Node param2 = Node.newString(Token.NAME, "p2");
    Node fn = NodeUtil.newFunctionNode("testFn", Arrays.asList(param1, param2), new Node(Token.BLOCK), 1, 0);

    assertEquals(param1, NodeUtil.getArgumentForFunction(fn, 0));
    assertEquals(param2, NodeUtil.getArgumentForFunction(fn, 1));
    assertNull(NodeUtil.getArgumentForFunction(fn, 2));

    Node arg1 = Node.newNumber(100);
    Node arg2 = Node.newNumber(200);
    Node call = NodeUtil.newCallNode(Node.newString(Token.NAME, "testFn"), arg1, arg2);

    assertEquals(arg1, NodeUtil.getArgumentForCallOrNew(call, 0));
    assertEquals(arg2, NodeUtil.getArgumentForCallOrNew(call, 1));
    assertNull(NodeUtil.getArgumentForCallOrNew(call, 2));
  }
}