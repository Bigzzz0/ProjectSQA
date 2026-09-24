package com.google.javascript.jscomp;

import static org.junit.Assert.*;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Known defect (testStringJoinAdd): tryFoldStringJoin loses a non-empty
 * separator when the previous array element stringifies to an empty string.
 * The implementation uses sb.length() > 0 to decide whether a separator is
 * pending, so for ["", "a"].join("+") it incorrectly folds to "a" instead of
 * "+a".
 *
 * Additional branches targeted:
 *   - Arithmetic folding: ADD/SUB/MUL/DIV, divide-by-zero guard.
 *   - Bitwise folding: BITAND/BITOR/BITNOT, fractional operand path.
 *   - Shift folding: LSH/RSH/URSH, out-of-bounds guard.
 *   - String folding: ADD, left-child ADD, array join, indexOf/lastIndexOf.
 *   - Comparison folding: EQ/LT, null/undefined equivalence.
 *   - Unary folding: TYPEOF, NOT, NEG, NOT minimization.
 *   - Object literal constructors: new Array/Object/RegExp.
 *   - Array GETELEM and GETPROP length folding.
 *   - Constant HOOK/IF branch selection.
 */
public class FoldConstantsDeepseekTest {

  private static Node string(String s) {
    return Node.newString(s);
  }

  private static Node name(String s) {
    return Node.newString(Token.NAME, s);
  }

  private static Node num(double d) {
    return Node.newNumber(d);
  }

  private static Node unary(int type, Node child) {
    return new Node(type, child);
  }

  private static Node binary(int type, Node left, Node right) {
    return new Node(type, left, right);
  }

  private static Node array(Node... elements) {
    Node n = new Node(Token.ARRAYLIT);
    for (Node element : elements) {
      n.addChildToBack(element);
    }
    return n;
  }

  private static Node joinCall(Node array, String separator) {
    return new Node(Token.CALL,
        new Node(Token.GETPROP, array, string("join")),
        string(separator));
  }

  private static Node methodCall(Node receiver, String method, Node... args) {
    Node getprop = new Node(Token.GETPROP, receiver, string(method));
    Node call = new Node(Token.CALL);
    call.addChildToBack(getprop);
    for (Node arg : args) {
      call.addChildToBack(arg);
    }
    return call;
  }

  private static Node buildScript(Node expr) {
    Node assign = new Node(Token.ASSIGN, name("result"), expr);
    Node exprResult = new Node(Token.EXPR_RESULT, assign);
    Node script = new Node(Token.SCRIPT);
    script.addChildToBack(exprResult);
    return script;
  }

  private static Node foldExpr(Node expr) {
    Compiler compiler = new Compiler();
    Node script = buildScript(expr);
    new FoldConstants(compiler).process(null, script);
    Node exprResult = script.getFirstChild();
    Node assign = exprResult.getFirstChild();
    return assign.getLastChild();
  }

  private static String foldedString(Node expr) {
    Node result = foldExpr(expr);
    assertEquals("Expected a folded string literal", Token.STRING, result.getType());
    return result.getString();
  }

  private static double foldedNumber(Node expr) {
    Node result = foldExpr(expr);
    assertEquals("Expected a folded number literal", Token.NUMBER, result.getType());
    return result.getDouble();
  }

  private static void assertFoldedTrue(Node expr) {
    assertEquals("Expected folded true literal", Token.TRUE, foldExpr(expr).getType());
  }

  private static void assertFoldedFalse(Node expr) {
    assertEquals("Expected folded false literal", Token.FALSE, foldExpr(expr).getType());
  }

  @Test(timeout = 4000)
  public void testStringJoinAdd() {
    // Defect-targeting test: ["", "a"].join("+") must fold to "+a".
    Node expr = joinCall(array(string(""), string("a")), "+");
    assertEquals("+a", foldedString(expr));
  }

  @Test(timeout = 4000)
  public void testStringJoinEmptySeparator() {
    Node expr = joinCall(array(string("a"), string("b")), "");
    assertEquals("ab", foldedString(expr));
  }

  @Test(timeout = 4000)
  public void testStringJoinNonEmptySeparator() {
    Node expr = joinCall(array(string("a"), string("b"), string("c")), ",");
    assertEquals("a,b,c", foldedString(expr));
  }

  @Test(timeout = 4000)
  public void testStringJoinSingleElementAndEmptyArray() {
    assertEquals("", foldedString(joinCall(new Node(Token.ARRAYLIT), "+")));
    assertEquals("a", foldedString(joinCall(array(string("a")), "+")));
  }

  @Test(timeout = 4000)
  public void testStringJoinEmptyElements() {
    // Empty string elements must still be separated by the join separator.
    Node expr = joinCall(array(string(""), string("")), "+");
    assertEquals("+", foldedString(expr));
  }

  @Test(timeout = 4000)
  public void testStringAdd() {
    assertEquals("ab", foldedString(binary(Token.ADD, string("a"), string("b"))));
  }

  @Test(timeout = 4000)
  public void testLeftChildStringAdd() {
    Node fooCall = new Node(Token.CALL, name("foo"));
    Node expr = binary(Token.ADD, binary(Token.ADD, fooCall, string("a")), string("b"));
    Node result = foldExpr(expr);
    assertEquals(Token.ADD, result.getType());
    assertEquals("ab", result.getLastChild().getString());
  }

  @Test(timeout = 4000)
  public void testArithmeticFolding() {
    assertEquals(3.0, foldedNumber(binary(Token.ADD, num(1), num(2))), 0.0);
    assertEquals(3.0, foldedNumber(binary(Token.SUB, num(5), num(2))), 0.0);
    assertEquals(12.0, foldedNumber(binary(Token.MUL, num(3), num(4))), 0.0);
    assertEquals(4.0, foldedNumber(binary(Token.DIV, num(8), num(2))), 0.0);
  }

  @Test(timeout = 4000)
  public void testDivideByZeroNotFolded() {
    Node expr = binary(Token.DIV, num(1), num(0));
    assertEquals(Token.DIV, foldExpr(expr).getType());
  }

  @Test(timeout = 4000)
  public void testBitwiseAndOrFolding() {
    assertEquals(1.0, foldedNumber(binary(Token.BITAND, num(5), num(3))), 0.0);
    assertEquals(7.0, foldedNumber(binary(Token.BITOR, num(5), num(3))), 0.0);
  }

  @Test(timeout = 4000)
  public void testUnaryNegAndBitNot() {
    assertEquals(-3.0, foldedNumber(unary(Token.NEG, num(3))), 0.0);
    assertEquals(-6.0, foldedNumber(unary(Token.BITNOT, num(5))), 0.0);
    assertFoldedFalse(unary(Token.NOT, new Node(Token.TRUE)));
  }

  @Test(timeout = 4000)
  public void testFractionalBitNotNotFolded() {
    Node expr = unary(Token.BITNOT, num(1.5));
    assertEquals(Token.BITNOT, foldExpr(expr).getType());
  }

  @Test(timeout = 4000)
  public void testShiftFolding() {
    assertEquals(8.0, foldedNumber(binary(Token.LSH, num(1), num(3))), 0.0);
    assertEquals(-4.0, foldedNumber(binary(Token.RSH, num(-8), num(1))), 0.0);
    assertEquals(2147483647.0, foldedNumber(binary(Token.URSH, num(-1), num(1))), 0.0);
  }

  @Test(timeout = 4000)
  public void testShiftOutOfBoundsNotFolded() {
    assertEquals(Token.LSH, foldExpr(binary(Token.LSH, num(1), num(32))).getType());
    assertEquals(Token.LSH, foldExpr(binary(Token.LSH, num(1), num(-1))).getType());
  }

  @Test(timeout = 4000)
  public void testComparisonFolding() {
    assertFoldedTrue(binary(Token.LT, num(1), num(2)));
    assertFoldedFalse(binary(Token.EQ, num(1), num(2)));
    assertFoldedTrue(binary(Token.EQ, string("a"), string("a")));
    assertFoldedTrue(binary(Token.EQ, name("undefined"), new Node(Token.NULL)));
  }

  @Test(timeout = 4000)
  public void testTypeOfFolding() {
    assertEquals("string", foldedString(unary(Token.TYPEOF, string("a"))));
    assertEquals("number", foldedString(unary(Token.TYPEOF, num(1))));
    assertEquals("boolean", foldedString(unary(Token.TYPEOF, new Node(Token.TRUE))));
    assertEquals("undefined", foldedString(unary(Token.TYPEOF, name("undefined"))));
  }

  @Test(timeout = 4000)
  public void testNotMinimization() {
    Node expr = unary(Token.NOT, binary(Token.EQ, name("a"), name("b")));
    assertEquals(Token.NE, foldExpr(expr).getType());
  }

  @Test(timeout = 4000)
  public void testGetElemAndLengthFolding() {
    Node arr = array(string("a"), string("b"), string("c"));
    Node elemResult = foldExpr(binary(Token.GETELEM, arr, num(1)));
    assertEquals(Token.STRING, elemResult.getType());
    assertEquals("b", elemResult.getString());

    Node arrLen = foldExpr(new Node(Token.GETPROP, array(num(1), num(2)), string("length")));
    assertEquals(Token.NUMBER, arrLen.getType());
    assertEquals(2.0, arrLen.getDouble(), 0.0);

    Node strLen = foldExpr(new Node(Token.GETPROP, string("abc"), string("length")));
    assertEquals(Token.NUMBER, strLen.getType());
    assertEquals(3.0, strLen.getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testGetElemOutOfBoundsNotFolded() {
    Node expr = binary(Token.GETELEM, array(num(1)), num(2));
    assertEquals(Token.GETELEM, foldExpr(expr).getType());
  }

  @Test(timeout = 4000)
  public void testStringIndexOfFolding() {
    assertEquals(2.0, foldedNumber(methodCall(string("abcdef"), "indexOf", string("cd"))), 0.0);
    assertEquals(1.0, foldedNumber(methodCall(string("abc"), "lastIndexOf", string("b"))), 0.0);
    assertEquals(-1.0, foldedNumber(methodCall(string("abc"), "indexOf", string("b"), num(2))), 0.0);
  }

  @Test(timeout = 4000)
  public void testLiteralConstructorFolding() {
    Node newArray = new Node(Token.NEW);
    newArray.addChildToBack(name("Array"));
    assertEquals(Token.ARRAYLIT, foldExpr(newArray).getType());

    Node newObject = new Node(Token.NEW);
    newObject.addChildToBack(name("Object"));
    assertEquals(Token.OBJECTLIT, foldExpr(newObject).getType());
  }

  @Test(timeout = 4000)
  public void testRegExpConstructorAndInvalidFlags() {
    Node regExp = new Node(Token.NEW);
    regExp.addChildToBack(name("RegExp"));
    regExp.addChildToBack(string("ab"));
    regExp.addChildToBack(string("i"));
    assertEquals(Token.REGEXP, foldExpr(regExp).getType());

    Node invalid = new Node(Token.NEW);
    invalid.addChildToBack(name("RegExp"));
    invalid.addChildToBack(string("ab"));
    invalid.addChildToBack(string("z"));
    assertEquals(Token.NEW, foldExpr(invalid).getType());
  }

  @Test(timeout = 4000)
  public void testHasBreakOrContinue() {
    FoldConstants pass = new FoldConstants(new Compiler());
    Node block = new Node(Token.BLOCK);
    block.addChildToBack(new Node(Token.BREAK));
    assertTrue(pass.hasBreakOrContinue(block));

    Node simpleBlock = new Node(Token.BLOCK);
    assertFalse(pass.hasBreakOrContinue(simpleBlock));
  }

  @Test(timeout = 4000)
  public void testHookConstantFolding() {
    Node hook = new Node(Token.HOOK);
    hook.addChildToBack(new Node(Token.TRUE));
    hook.addChildToBack(string("a"));
    hook.addChildToBack(string("b"));
    Node result = foldExpr(hook);
    assertEquals(Token.STRING, result.getType());
    assertEquals("a", result.getString());
  }
}