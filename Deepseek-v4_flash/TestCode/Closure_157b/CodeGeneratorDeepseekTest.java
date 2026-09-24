package com.google.javascript.jscomp;

import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * This suite targets CodeGenerator with emphasis on:
 *  - Object literal key emission, including the known numeric-key defect where a
 *    numeric-looking key is emitted as a quoted string instead of being preserved as
 *    a numeric key.
 *  - Getter/setter object-literal paths.
 *  - Binary/unary/ternary expression precedence and parenthesization.
 *  - Call special cases: direct eval, indirect eval, free calls.
 *  - Function expression/statement contexts.
 *  - Control flow constructs: IF, dangling ELSE, FOR, FOR-IN, DO, WHILE, WITH,
 *    TRY/CATCH/FINALLY, LABEL, THROW, RETURN, BREAK, CONTINUE.
 *  - Static escaping helpers: jsString, regexpEscape, identifierEscape, and the
 *    HTML/Unicode-safe string escaping branches.
 *
 * The consumer deliberately inserts a space between two adjacent identifier-like
 * tokens so keyword/identifier boundaries are preserved in the emitted minified code.
 */
public class CodeGeneratorDeepseekTest {

  private static class Cc extends CodeConsumer {
    final StringBuilder sb = new StringBuilder();

    @Override
    public void add(String str) {
      if (sb.length() > 0 && str.length() > 0) {
        char last = sb.charAt(sb.length() - 1);
        char first = str.charAt(0);
        if (isWord(last) && isWord(first)) {
          sb.append(' ');
        }
      }
      sb.append(str);
    }

    private static boolean isWord(char c) {
      return Character.isLetterOrDigit(c) || c == '_' || c == '$';
    }
  }

  private static String render(Node n) {
    Cc cc = new Cc();
    new CodeGenerator(cc).add(n);
    return cc.sb.toString();
  }

  private static String render(Node n, CodeGenerator.Context context) {
    Cc cc = new Cc();
    new CodeGenerator(cc).add(n, context);
    return cc.sb.toString();
  }

  private static String renderExpr(Node n, int minPrecedence) {
    Cc cc = new Cc();
    new CodeGenerator(cc).addExpr(n, minPrecedence);
    return cc.sb.toString();
  }

  private static Node node(int type, Node... children) {
    Node n = new Node(type);
    for (Node child : children) {
      n.addChildToBack(child);
    }
    return n;
  }

  private static Node name(String s) {
    return Node.newString(Token.NAME, s);
  }

  private static Node string(String s) {
    return Node.newString(Token.STRING, s);
  }

  private static Node labelName(String s) {
    return Node.newString(Token.LABEL_NAME, s);
  }

  private static Node number(double d) {
    return Node.newNumber(d);
  }

  private static Node empty() {
    return new Node(Token.EMPTY);
  }

  private static Node block(Node... stmts) {
    Node b = new Node(Token.BLOCK);
    for (Node s : stmts) {
      b.addChildToBack(s);
    }
    return b;
  }

  private static Node exprResult(Node expr) {
    return node(Token.EXPR_RESULT, expr);
  }

  private static Node returnStmt(Node expr) {
    return node(Token.RETURN, expr);
  }

  private static Node function(String fnName, Node params, Node body) {
    return node(Token.FUNCTION, name(fnName), params, body);
  }

  private static Node objLit(Node... entries) {
    return node(Token.OBJECTLIT, entries);
  }

  private static Node strKey(String key, Node value) {
    Node k = Node.newString(Token.STRING, key);
    k.addChildToBack(value);
    return k;
  }

  private static Node numKey(double key, Node value) {
    Node k = Node.newNumber(key);
    k.addChildToBack(value);
    return k;
  }

  private static Node array(Node... elements) {
    return node(Token.ARRAYLIT, elements);
  }

  private static Node getter(String propName, Node body) {
    Node fn = function("", new Node(Token.LP), body);
    Node get = node(Token.GET, fn);
    get.setString(propName);
    return get;
  }

  private static Node setter(String propName, String paramName, Node body) {
    Node params = node(Token.LP, name(paramName));
    Node fn = function("", params, body);
    Node set = node(Token.SET, fn);
    set.setString(propName);
    return set;
  }

  @Test(timeout = 4000)
  public void testTagAsStrict() {
    Cc cc = new Cc();
    new CodeGenerator(cc).tagAsStrict();
    assertEquals("'use strict';", cc.sb.toString());
  }

  @Test(timeout = 4000)
  public void testGenerateSimpleNodes() {
    assertEquals("42", render(number(42)));
    assertEquals("foo", render(name("foo")));
    assertEquals("\"hello\"", render(string("hello")));
    assertEquals("this", render(new Node(Token.THIS)));
    assertEquals("true", render(new Node(Token.TRUE)));
    assertEquals("false", render(new Node(Token.FALSE)));
    assertEquals("null", render(new Node(Token.NULL)));
    assertEquals("", render(empty()));
    assertEquals("debugger;", render(new Node(Token.DEBUGGER)));
  }

  @Test(timeout = 4000)
  public void testNameInitialization() {
    Node n = name("x");
    n.addChildToBack(number(1));
    assertEquals("x=1", render(n));

    Node emptyName = name("x");
    emptyName.addChildToBack(empty());
    assertEquals("x", render(emptyName));

    Node varDecl = node(Token.VAR, name("x"));
    assertEquals("var x", render(varDecl));

    Node varInit = name("x");
    varInit.addChildToBack(number(1));
    assertEquals("var x=1", render(node(Token.VAR, varInit)));
  }

  @Test(timeout = 4000)
  public void testUnaryAndBinaryOperators() {
    assertEquals("!a", render(node(Token.NOT, name("a"))));
    assertEquals("-2", render(node(Token.NEG, number(2))));
    assertEquals("-a", render(node(Token.NEG, name("a"))));
    assertEquals("a+b+c",
        render(node(Token.ADD, name("a"), node(Token.ADD, name("b"), name("c")))));
  }

  @Test(timeout = 4000)
  public void testHookAndComma() {
    assertEquals("c?1:2",
        render(node(Token.HOOK, name("c"), number(1), number(2))));
    assertEquals("a,b",
        render(node(Token.COMMA, name("a"), name("b"))));
    assertEquals("(a,b)", renderExpr(node(Token.COMMA, name("a"), name("b")), 2));
  }

  @Test(timeout = 4000)
  public void testArrayLiteral() {
    assertEquals("[1,2]", render(array(number(1), number(2))));
    assertEquals("(1)", render(node(Token.LP, number(1))));
  }

  @Test(timeout = 4000)
  public void testObjectLitBasic() {
    assertEquals("{a:1}", render(objLit(strKey("a", number(1)))));
    assertEquals("{\"class\":1}", render(objLit(strKey("class", number(1)))));
  }

  @Test(timeout = 4000)
  public void testObjectLitNumericKeyDefect() {
    String out = render(objLit(strKey("1", number(1))));
    assertFalse("Numeric object literal key should not be quoted: " + out,
        out.contains("\"1\""));
  }

  @Test(timeout = 4000)
  public void testObjectLitHugeNumericKeyDefect() {
    String out = render(objLit(strKey("3000000000", number(1))));
    assertFalse("Huge numeric object literal key should not be quoted: " + out,
        out.contains("\"3000000000\""));
  }

  @Test(timeout = 4000)
  public void testObjectLitNumberKey() {
    assertEquals("{1:1}", render(objLit(numKey(1, number(1)))));
  }

  @Test(timeout = 4000)
  public void testObjectLitGetterSetter() {
    Node obj = objLit(
        getter("a", block(returnStmt(number(1)))),
        setter("b", "v", block(exprResult(number(1)))));
    assertEquals("{get a(){return 1;},set b(v){1;}}", render(obj));
  }

  @Test(timeout = 4000)
  public void testObjectLitStartOfExpr() {
    assertEquals("({a:1})",
        render(objLit(strKey("a", number(1))), CodeGenerator.Context.START_OF_EXPR));
  }

  @Test(timeout = 4000)
  public void testGetPropAndGetElem() {
    assertEquals("x.length",
        render(node(Token.GETPROP, name("x"), string("length"))));
    assertEquals("(1).toString",
        render(node(Token.GETPROP, number(1), string("toString"))));
    assertEquals("x[\"key\"]",
        render(node(Token.GETELEM, name("x"), string("key"))));
  }

  @Test(timeout = 4000)
  public void testCallForms() {
    assertEquals("f()", render(node(Token.CALL, name("f"))));
    assertEquals("(0,eval)()", render(node(Token.CALL, name("eval"))));

    Node directEval = name("eval");
    directEval.putBooleanProp(Node.DIRECT_EVAL, true);
    assertEquals("eval()", render(node(Token.CALL, directEval)));

    Node freeCall = node(Token.CALL, node(Token.GETPROP, name("x"), string("f")));
    freeCall.putBooleanProp(Node.FREE_CALL, true);
    assertEquals("(0,x.f)()", render(freeCall));
  }

  @Test(timeout = 4000)
  public void testFunctionExpressionAndStatement() {
    Node fn = function("f", new Node(Token.LP), block(returnStmt(number(1))));
    assertEquals("(function f(){return 1;})",
        render(fn, CodeGenerator.Context.START_OF_EXPR));
    assertEquals("function f(){return 1;};",
        render(fn, CodeGenerator.Context.STATEMENT));
  }

  @Test(timeout = 4000)
  public void testIfAndDanglingElse() {
    Node ifNode = node(Token.IF,
        name("c"),
        block(exprResult(number(1)), exprResult(number(2))),
        block(exprResult(number(3)), exprResult(number(4))));
    assertEquals("if(c){1;2;}else{3;4;}", render(ifNode));

    Node ifNoElse = node(Token.IF, name("c"), block(exprResult(number(1))));
    assertEquals("{if(c)1;}",
        render(ifNoElse, CodeGenerator.Context.BEFORE_DANGLING_ELSE));
  }

  @Test(timeout = 4000)
  public void testLoops() {
    Node forNode = node(Token.FOR,
        name("i"),
        node(Token.LT, name("i"), number(10)),
        node(Token.INC, name("i")),
        block(exprResult(number(1))));
    assertEquals("for(i;i<10;++i)1;", render(forNode));

    Node forIn = node(Token.FOR, name("a"), name("obj"), block(exprResult(number(1))));
    assertEquals("for(a in obj)1;", render(forIn));

    Node doNode = node(Token.DO, block(exprResult(number(1))), name("c"));
    assertEquals("do 1;while(c);", render(doNode));

    Node whileNode = node(Token.WHILE, name("c"), block(exprResult(number(1))));
    assertEquals("while(c)1;", render(whileNode));

    Node withNode = node(Token.WITH, name("o"), block(exprResult(number(1))));
    assertEquals("with(o)1;", render(withNode));
  }

  @Test(timeout = 4000)
  public void testTryCatchFinally() {
    Node catchNode = node(Token.CATCH, name("e"), block(exprResult(number(2))));
    Node catchBlock = new Node(Token.BLOCK);
    catchBlock.addChildToBack(catchNode);

    Node tryNode = node(Token.TRY,
        block(exprResult(number(1))),
        catchBlock,
        block(exprResult(number(3))));
    assertEquals("try{1;}catch(e){2;}finally{3;}", render(tryNode));
  }

  @Test(timeout = 4000)
  public void testVarLabelAndThrowReturn() {
    Node label = node(Token.LABEL, labelName("l"), block(exprResult(number(1))));
    assertEquals("l:1;", render(label));

    assertEquals("throw e;", render(node(Token.THROW, name("e"))));
    assertEquals("return 1;", render(node(Token.RETURN, number(1))));
    assertEquals("break;", render(new Node(Token.BREAK)));
    assertEquals("continue;", render(new Node(Token.CONTINUE)));
    assertEquals("break l;", render(node(Token.BREAK, labelName("l"))));
    assertEquals("continue l;", render(node(Token.CONTINUE, labelName("l"))));
  }

  @Test(timeout = 4000)
  public void testNewAndDelete() {
    assertEquals("new Foo", render(node(Token.NEW, name("Foo"))));
    assertEquals("new Foo(1,2)", render(node(Token.NEW, name("Foo"), number(1), number(2))));
    assertEquals("delete x", render(node(Token.DELPROP, name("x"))));
  }

  @Test(timeout = 4000)
  public void testRegexp() {
    assertEquals("/a+b/", CodeGenerator.regexpEscape("a+b"));
    assertEquals("/a+b/gi", render(node(Token.REGEXP, string("a+b"), string("gi"))));
  }

  @Test(timeout = 4000)
  public void testJsStringQuoteSelection() {
    assertEquals("\"a'b\"", CodeGenerator.jsString("a'b", null));
    assertEquals("'a\"b'", CodeGenerator.jsString("a\"b", null));
  }

  @Test(timeout = 4000)
  public void testEscapeToDoubleQuotedJsString() {
    assertEquals("\"a\\nb\"", CodeGenerator.escapeToDoubleQuotedJsString("a\nb"));
    assertEquals("\"<\\!--\"", CodeGenerator.escapeToDoubleQuotedJsString("<!--"));
    assertEquals("\"<\\/script>\"", CodeGenerator.escapeToDoubleQuotedJsString("</script>"));
    assertEquals("\"--\\>\"", CodeGenerator.escapeToDoubleQuotedJsString("-->"));
    assertEquals("\"\\u00e9\"", CodeGenerator.escapeToDoubleQuotedJsString("\u00e9"));
  }

  @Test(timeout = 4000)
  public void testIdentifierEscape() {
    assertEquals("abc", CodeGenerator.identifierEscape("abc"));
    assertEquals("a\\u00e9", CodeGenerator.identifierEscape("a\u00e9"));
    assertEquals("\\ud83d\\ude00", CodeGenerator.identifierEscape("\ud83d\ude00"));
  }
}