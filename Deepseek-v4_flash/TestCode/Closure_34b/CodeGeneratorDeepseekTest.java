package com.google.javascript.jscomp;

import com.google.javascript.jscomp.CodePrinter;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test public class CodeGeneratorDeepseekTest CodeGenerator through the public CodePrinter API.
 *
 * <p>Defect targeted: deeply nested ADD chains must not throw StackOverflowError.
 * The original failure is CodePrinterTest::testManyAdds.
 *
 * <p>Branch & Defect Analysis Matrix:
 * <ul>
 *   <li>Defect: StackOverflowError on many left-associative ADD nodes.</li>
 *   <li>Expected: CodeGenerator should iterate over the ADD chain instead of recursing.</li>
 *   <li>This test builds a 50,000-deep ADD tree and asserts that code generation completes.</li>
 * </ul>
 */
public class CodeGeneratorDeepseekTest {

  private static Node num(double d) {
    return Node.newNumber(d);
  }

  private static Node name(String s) {
    return new Node(Token.NAME, s);
  }

  private static Node str(String s) {
    return Node.newString(s);
  }

  private static String print(Node n) {
    return new CodePrinter.Builder(n).build();
  }

  @Test(timeout = 4000)
  public void testManyAddsNoStackOverflow() {
    Node n = num(1);
    for (int i = 0; i < 50000; i++) {
      n = new Node(Token.ADD, n, num(1));
    }
    String result = print(n);
    assertNotNull(result);
    assertTrue(result.length() > 0);
    assertTrue(result.endsWith("+1"));
  }

  @Test
  public void testNumberLiteral() {
    assertEquals("1", print(num(1)));
    assertEquals("1.5", print(num(1.5)));
  }

  @Test
  public void testStringLiteral() {
    String result = print(str("abc"));
    assertTrue(result.contains("abc"));
  }

  @Test
  public void testNameExpression() {
    assertEquals("x", print(name("x")));
  }

  @Test
  public void testGetProp() {
    Node n = new Node(Token.GETPROP, name("a"), str("b"));
    assertEquals("a.b", print(n));
  }

  @Test
  public void testGetElem() {
    Node n = new Node(Token.GETELEM, name("a"), num(0));
    assertEquals("a[0]", print(n));
  }

  @Test
  public void testCall() {
    Node n = new Node(Token.CALL, name("f"), num(1), num(2));
    String result = print(n);
    assertTrue(result.startsWith("f("));
    assertTrue(result.endsWith(")"));
  }

  @Test
  public void testNewExpression() {
    Node n = new Node(Token.NEW, name("F"), num(1));
    String result = print(n);
    assertTrue(result.contains("new F"));
  }

  @Test
  public void testArrayLiteral() {
    Node n = new Node(Token.ARRAYLIT, num(1), num(2));
    assertEquals("[1,2]", print(n));
  }

  @Test
  public void testObjectLiteral() {
    Node key = Node.newString(Token.STRING, "a");
    key.addChildToBack(num(1));
    Node n = new Node(Token.OBJECTLIT, key);
    String result = print(n);
    assertTrue(result.contains("a"));
    assertTrue(result.contains("1"));
  }

  @Test
  public void testBinaryAdd() {
    Node n = new Node(Token.ADD, num(1), num(2));
    assertEquals("1+2", print(n));
  }

  @Test
  public void testUnaryNot() {
    Node n = new Node(Token.NOT, name("x"));
    assertEquals("!x", print(n));
  }

  @Test
  public void testHook() {
    Node n = new Node(Token.HOOK, name("c"), num(1), num(2));
    String result = print(n);
    assertTrue(result.contains("?"));
    assertTrue(result.contains(":"));
  }

  @Test
  public void testComma() {
    Node n = new Node(Token.COMMA, num(1), num(2));
    assertEquals("1,2", print(n));
  }

  @Test
  public void testIfStatement() {
    Node block = new Node(Token.BLOCK);
    Node n = new Node(Token.IF, name("x"), block);
    String result = print(n);
    assertTrue(result.contains("if"));
  }

  @Test
  public void testWhileStatement() {
    Node block = new Node(Token.BLOCK);
    Node n = new Node(Token.WHILE, name("x"), block);
    String result = print(n);
    assertTrue(result.contains("while"));
  }

  @Test
  public void testForStatement() {
    Node block = new Node(Token.BLOCK);
    Node n = new Node(Token.FOR, num(0), name("x"), num(1), block);
    String result = print(n);
    assertTrue(result.contains("for"));
  }
}
