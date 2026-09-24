package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * - Typeof folding branches:
 *   STRING -> "string", NUMBER -> "number", TRUE/FALSE -> "boolean",
 *   NULL/ARRAYLIT/OBJECTLIT -> "object", NAME-"undefined" -> "undefined".
 *   Defect target: Token.REGEXP is a literal value but is missing from the
 *   switch, so `typeof /a/` is not folded to "object".
 *
 * - Unary operator branches:
 *   NOT on known booleans, NEG on constants, BITNOT on integer range,
 *   fractional and out-of-range bitwise operands.
 *
 * - Binary operator branches:
 *   ADD/SUB/MUL/DIV with divide-by-zero guard, BITAND/BITOR, shift bounds,
 *   GETPROP/GETELEM, AND/OR in plain and conditional contexts, comparisons,
 *   instanceof, string concatenation, and compound assignment.
 */
public class PeepholeFoldConstantsDeepseekTest {

  private static class TestPeepholeFoldConstants extends PeepholeFoldConstants {
    int errorCount = 0;
    DiagnosticType lastError = null;

    @Override
    protected void reportCodeChange() {
      // No-op; we inspect the AST directly.
    }

    @Override
    protected void error(DiagnosticType diagnosticType, Node n) {
      errorCount++;
      lastError = diagnosticType;
    }
  }

  private TestPeepholeFoldConstants optimizer;

  @Before
  public void setUp() {
    optimizer = new TestPeepholeFoldConstants();
  }

  private Node fold(Node node) {
    Node parent = new Node(Token.SCRIPT);
    parent.addChildToBack(node);
    return optimizer.optimizeSubtree(node);
  }

  private void assertFoldNumber(int operator, double left, double right, double expected) {
    Node node = new Node(operator, Node.newNumber(left), Node.newNumber(right));
    Node result = fold(node);
    assertEquals(Token.NUMBER, result.getType());
    assertEquals(expected, result.getDouble(), 0.0);
  }

  private void assertFoldComparison(int operator, double left, double right, boolean expected) {
    Node node = new Node(operator, Node.newNumber(left), Node.newNumber(right));
    Node result = fold(node);
    assertEquals(expected ? Token.TRUE : Token.FALSE, result.getType());
  }

  @Test(timeout = 4000)
  public void testFoldTypeofStringAndNumber() {
    Node typeofString = new Node(Token.TYPEOF, Node.newString("foo"));
    Node result = fold(typeofString);
    assertEquals(Token.STRING, result.getType());
    assertEquals("string", result.getString());

    Node typeofNumber = new Node(Token.TYPEOF, Node.newNumber(42));
    result = fold(typeofNumber);
    assertEquals(Token.STRING, result.getType());
    assertEquals("number", result.getString());
  }

  @Test(timeout = 4000)
  public void testFoldTypeofBooleanNullArrayObject() {
    Node typeofTrue = new Node(Token.TYPEOF, new Node(Token.TRUE));
    Node result = fold(typeofTrue);
    assertEquals(Token.STRING, result.getType());
    assertEquals("boolean", result.getString());

    Node typeofFalse = new Node(Token.TYPEOF, new Node(Token.FALSE));
    result = fold(typeofFalse);
    assertEquals("boolean", result.getString());

    Node typeofNull = new Node(Token.TYPEOF, new Node(Token.NULL));
    result = fold(typeofNull);
    assertEquals("object", result.getString());

    Node array = new Node(Token.ARRAYLIT);
    Node typeofArray = new Node(Token.TYPEOF, array);
    result = fold(typeofArray);
    assertEquals("object", result.getString());

    Node object = new Node(Token.OBJECTLIT);
    Node typeofObject = new Node(Token.TYPEOF, object);
    result = fold(typeofObject);
    assertEquals("object", result.getString());
  }

  @Test(timeout = 4000)
  public void testFoldTypeofRegexLiteral() {
    // Defect target: a REGEXP literal is a valid typeof operand and should
    // fold to the string "object". The buggy implementation omits the
    // Token.REGEXP branch, so this assertion fails on the defective version.
    Node regexp = new Node(Token.REGEXP, Node.newString("a"));
    Node typeofRegexp = new Node(Token.TYPEOF, regexp);
    Node result = fold(typeofRegexp);
    assertEquals(Token.STRING, result.getType());
    assertEquals("object", result.getString());
  }

  @Test(timeout = 4000)
  public void testFoldTypeofUndefinedName() {
    Node undefinedName = Node.newString(Token.NAME, "undefined");
    Node typeofUndefined = new Node(Token.TYPEOF, undefinedName);
    Node result = fold(typeofUndefined);
    assertEquals(Token.STRING, result.getType());
    assertEquals("undefined", result.getString());
  }

  @Test(timeout = 4000)
  public void testFoldTypeofUnknownDoesNotFold() {
    Node unknownName = Node.newString(Token.NAME, "x");
    Node typeofUnknown = new Node(Token.TYPEOF, unknownName);
    Node result = fold(typeofUnknown);
    assertSame(typeofUnknown, result);
  }

  @Test(timeout = 4000)
  public void testFoldNotOperator() {
    Node notTrue = new Node(Token.NOT, new Node(Token.TRUE));
    Node result = fold(notTrue);
    assertEquals(Token.FALSE, result.getType());

    Node notFalse = new Node(Token.NOT, new Node(Token.FALSE));
    result = fold(notFalse);
    assertEquals(Token.TRUE, result.getType());
  }

  @Test(timeout = 4000)
  public void testFoldNegation() {
    Node negPositive = new Node(Token.NEG, Node.newNumber(5));
    Node result = fold(negPositive);
    assertEquals(Token.NUMBER, result.getType());
    assertEquals(-5.0, result.getDouble(), 0.0);

    Node negNegative = new Node(Token.NEG, Node.newNumber(-2));
    result = fold(negNegative);
    assertEquals(2.0, result.getDouble(), 0.0);

    Node infinity = new Node(Token.NEG, Node.newString(Token.NAME, "Infinity"));
    result = fold(infinity);
    assertSame(infinity, result);
  }

  @Test(timeout = 4000)
  public void testFoldBitNot() {
    Node bitnot = new Node(Token.BITNOT, Node.newNumber(5));
    Node result = fold(bitnot);
    assertEquals(Token.NUMBER, result.getType());
    assertEquals(-6.0, result.getDouble(), 0.0);

    Node fractional = new Node(Token.BITNOT, Node.newNumber(1.5));
    result = fold(fractional);
    assertSame(fractional, result);
    assertEquals(FRACTIONAL_BITWISE_OPERAND, optimizer.lastError);

    Node outOfRange = new Node(Token.BITNOT, Node.newNumber(2147483648.0));
    result = fold(outOfRange);
    assertSame(outOfRange, result);
    assertEquals(BITWISE_OPERAND_OUT_OF_RANGE, optimizer.lastError);
  }

  @Test(timeout = 4000)
  public void testFoldArithmeticAddSubMulDiv() {
    assertFoldNumber(Token.ADD, 1.0, 2.0, 3.0);
    assertFoldNumber(Token.SUB, 5.0, 2.0, 3.0);
    assertFoldNumber(Token.MUL, 3.0, 4.0, 12.0);
    assertFoldNumber(Token.DIV, 8.0, 2.0, 4.0);
  }

  @Test(timeout = 4000)
  public void testFoldArithmeticDivideByZero() {
    Node div = new Node(Token.DIV, Node.newNumber(1), Node.newNumber(0));
    Node result = fold(div);
    assertSame(div, result);
    assertEquals(DIVIDE_BY_0_ERROR, optimizer.lastError);
  }

  @Test(timeout = 4000)
  public void testFoldBitAndOr() {
    assertFoldNumber(Token.BITAND, 5.0, 3.0, 1.0);
    assertFoldNumber(Token.BITOR, 5.0, 3.0, 7.0);
  }

  @Test(timeout = 4000)
  public void testFoldShiftOperations() {
    assertFoldNumber(Token.LSH, 1.0, 2.0, 4.0);
    assertFoldNumber(Token.RSH, 8.0, 1.0, 4.0);
    assertFoldNumber(Token.URSH, -1.0, 1.0, 2147483647.0);
  }

  @Test(timeout = 4000)
  public void testFoldShiftOutOfRangeErrors() {
    Node badLeft = new Node(Token.LSH, Node.newNumber(2147483648.0), Node.newNumber(1));
    Node result = fold(badLeft);
    assertSame(badLeft, result);
    assertEquals(BITWISE_OPERAND_OUT_OF_RANGE, optimizer.lastError);

    Node badAmount = new Node(Token.LSH, Node.newNumber(1), Node.newNumber(32));
    result = fold(badAmount);
    assertSame(badAmount, result);
    assertEquals(SHIFT_AMOUNT_OUT_OF_BOUNDS, optimizer.lastError);

    Node fractional = new Node(Token.RSH, Node.newNumber(1.5), Node.newNumber(1));
    result = fold(fractional);
    assertSame(fractional, result);
    assertEquals(FRACTIONAL_BITWISE_OPERAND, optimizer.lastError);
  }

  @Test(timeout = 4000)
  public void testFoldGetPropArrayAndStringLength() {
    Node array = new Node(Token.ARRAYLIT);
    array.addChildToBack(Node.newNumber(1));
    array.addChildToBack(Node.newNumber(2));
    Node arrayLength = new Node(Token.GETPROP, array, Node.newString("length"));
    Node result = fold(arrayLength);
    assertEquals(Token.NUMBER, result.getType());
    assertEquals(2.0, result.getDouble(), 0.0);

    Node stringLength = new Node(Token.GETPROP, Node.newString("abcd"), Node.newString("length"));
    result = fold(stringLength);
    assertEquals(Token.NUMBER, result.getType());
    assertEquals(4.0, result.getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testFoldGetElemArrayIndex() {
    Node array = new Node(Token.ARRAYLIT);
    array.addChildToBack(Node.newNumber(10));
    array.addChildToBack(Node.newNumber(20));
    array.addChildToBack(Node.newNumber(30));
    Node getElem = new Node(Token.GETELEM, array, Node.newNumber(1));
    Node result = fold(getElem);
    assertEquals(Token.NUMBER, result.getType());
    assertEquals(20.0, result.getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testFoldGetElemIndexErrors() {
    Node array1 = new Node(Token.ARRAYLIT);
    array1.addChildToBack(Node.newNumber(1));
    Node negativeIndex = new Node(Token.GETELEM, array1, Node.newNumber(-1));
    Node result = fold(negativeIndex);
    assertSame(negativeIndex, result);
    assertEquals(INDEX_OUT_OF_BOUNDS_ERROR, optimizer.lastError);

    Node array2 = new Node(Token.ARRAYLIT);
    array2.addChildToBack(Node.newNumber(1));
    Node fractionalIndex = new Node(Token.GETELEM, array2, Node.newNumber(1.5));
    result = fold(fractionalIndex);
    assertSame(fractionalIndex, result);
    assertEquals(INVALID_GETELEM_INDEX_ERROR, optimizer.lastError);

    Node array3 = new Node(Token.ARRAYLIT);
    array3.addChildToBack(Node.newNumber(1));
    Node outOfBounds = new Node(Token.GETELEM, array3, Node.newNumber(2));
    result = fold(outOfBounds);
    assertSame(outOfBounds, result);
    assertEquals(INDEX_OUT_OF_BOUNDS_ERROR, optimizer.lastError);
  }

  @Test(timeout = 4000)
  public void testFoldAndOrFolding() {
    Node x = Node.newString(Token.NAME, "x");
    Node orTrue = new Node(Token.OR, new Node(Token.TRUE), x);
    Node result = fold(orTrue);
    assertEquals(Token.TRUE, result.getType());

    Node x2 = Node.newString(Token.NAME, "x");
    Node orFalse = new Node(Token.OR, new Node(Token.FALSE), x2);
    result = fold(orFalse);
    assertSame(x2, result);

    Node x3 = Node.newString(Token.NAME, "x");
    Node andTrue = new Node(Token.AND, new Node(Token.TRUE), x3);
    result = fold(andTrue);
    assertSame(x3, result);

    Node andFalse = new Node(Token.AND, new Node(Token.FALSE), Node.newString(Token.NAME, "x"));
    result = fold(andFalse);
    assertEquals(Token.FALSE, result.getType());

    Node cond1 = new Node(Token.OR, Node.newString(Token.NAME, "x"), new Node(Token.TRUE));
    new Node(Token.IF, cond1);
    result = optimizer.optimizeSubtree(cond1);
    assertEquals(Token.TRUE, result.getType());

    Node cond2 = new Node(Token.AND, Node.newString(Token.NAME, "y"), new Node(Token.FALSE));
    new Node(Token.IF, cond2);
    result = optimizer.optimizeSubtree(cond2);
    assertEquals(Token.FALSE, result.getType());
  }

  @Test(timeout = 4000)
  public void testFoldComparisonNumbersAndStrings() {
    assertFoldComparison(Token.LT, 1.0, 2.0, true);
    assertFoldComparison(Token.GT, 3.0, 4.0, false);
    assertFoldComparison(Token.LE, 2.0, 2.0, true);
    assertFoldComparison(Token.GE, 2.0, 3.0, false);
    assertFoldComparison(Token.EQ, 1.0, 1.0, true);
    assertFoldComparison(Token.NE, 1.0, 2.0, true);
    assertFoldComparison(Token.SHEQ, 1.0, 1.0, true);
    assertFoldComparison(Token.SHNE, 1.0, 1.0, false);

    Node strEq = new Node(Token.EQ, Node.newString("a"), Node.newString("a"));
    Node result = fold(strEq);
    assertEquals(Token.TRUE, result.getType());

    Node strNe = new Node(Token.NE, Node.newString("a"), Node.newString("b"));
    result = fold(strNe);
    assertEquals(Token.TRUE, result.getType());
  }

  @Test(timeout = 4000)
  public void testFoldComparisonUndefinedNull() {
    Node undef = Node.newString(Token.NAME, "undefined");
    Node eq = new Node(Token.EQ, undef, new Node(Token.NULL));
    Node result = fold(eq);
    assertEquals(Token.TRUE, result.getType());

    Node undef2 = Node.newString(Token.NAME, "undefined");
    Node sheq = new Node(Token.SHEQ, undef2, new Node(Token.NULL));
    result = fold(sheq);
    assertEquals(Token.FALSE, result.getType());

    Node voidZero = new Node(Token.VOID, Node.newNumber(0));
    Node voidEqNull = new Node(Token.EQ, voidZero, new Node(Token.NULL));
    result = fold(voidEqNull);
    assertEquals(Token.TRUE, result.getType());
  }

  @Test(timeout = 4000)
  public void testFoldInstanceof() {
    Node stringCase = new Node(Token.INSTANCEOF, Node.newString("x"),
        Node.newString(Token.NAME, "Object"));
    Node result = fold(stringCase);
    assertEquals(Token.FALSE, result.getType());

    Node array = new Node(Token.ARRAYLIT);
    Node arrayCase = new Node(Token.INSTANCEOF, array,
        Node.newString(Token.NAME, "Object"));
    result = fold(arrayCase);
    assertEquals(Token.TRUE, result.getType());

    Node unknown = new Node(Token.INSTANCEOF, Node.newString(Token.NAME, "x"),
        Node.newString(Token.NAME, "Foo"));
    result = fold(unknown);
    assertSame(unknown, result);
  }

  @Test(timeout = 4000)
  public void testFoldStringJoin() {
    Node array = new Node(Token.ARRAYLIT);
    array.addChildToBack(Node.newString("a"));
    array.addChildToBack(Node.newString("b"));
    Node call = new Node(Token.CALL,
        new Node(Token.GETPROP, array, Node.newString("join")),
        Node.newString(""));
    Node result = fold(call);
    assertEquals(Token.STRING, result.getType());
    assertEquals("ab", result.getString());

    Node emptyArray = new Node(Token.ARRAYLIT);
    Node emptyCall = new Node(Token.CALL,
        new Node(Token.GETPROP, emptyArray, Node.newString("join")),
        Node.newString(","));
    result = fold(emptyCall);
    assertEquals(Token.STRING, result.getType());
    assertEquals("", result.getString());
  }

  @Test(timeout = 4000)
  public void testFoldStringIndexOf() {
    Node call = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString("abcabc"), Node.newString("indexOf")),
        Node.newString("b"));
    Node result = fold(call);
    assertEquals(Token.NUMBER, result.getType());
    assertEquals(1.0, result.getDouble(), 0.0);

    Node callWithIndex = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString("abcabc"), Node.newString("indexOf")),
        Node.newString("b"));
    callWithIndex.addChildToBack(Node.newNumber(2));
    result = fold(callWithIndex);
    assertEquals(4.0, result.getDouble(), 0.0);

    Node lastIndexOfCall = new Node(Token.CALL,
        new Node(Token.GETPROP, Node.newString("abc"), Node.newString("lastIndexOf")),
        Node.newString("b"));
    result = fold(lastIndexOfCall);
    assertEquals(1.0, result.getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testFoldAssignCompound() {
    Node left = Node.newString(Token.NAME, "x");
    Node leftInRight = Node.newString(Token.NAME, "x");
    Node right = new Node(Token.ADD, leftInRight, Node.newString(Token.NAME, "y"));
    Node assign = new Node(Token.ASSIGN, left, right);
    Node result = fold(assign);
    assertEquals(Token.ASSIGN_ADD, result.getType());
    assertEquals("x", result.getFirstChild().getString());
    assertEquals("y", result.getLastChild().getString());
  }

  @Test(timeout = 4000)
  public void testFoldStringAddConstantAndLeftChild() {
    Node addConstant = new Node(Token.ADD, Node.newString("a"), Node.newString("b"));
    Node result = fold(addConstant);
    assertEquals(Token.STRING, result.getType());
    assertEquals("ab", result.getString());

    Node leftAdd = new Node(Token.ADD, Node.newString(Token.NAME, "a"), Node.newString("b"));
    Node outerAdd = new Node(Token.ADD, leftAdd, Node.newString("c"));
    result = fold(outerAdd);
    assertEquals(Token.ADD, result.getType());
    assertEquals(Token.NAME, result.getFirstChild().getType());
    assertEquals("a", result.getFirstChild().getString());
    assertEquals(Token.STRING, result.getLastChild().getType());
    assertEquals("bc", result.getLastChild().getString());
  }
}