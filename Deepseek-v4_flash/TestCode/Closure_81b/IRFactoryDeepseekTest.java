package com.google.javascript.jscomp.parsing;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.javascript.jscomp.mozilla.rhino.ErrorReporter;
import com.google.javascript.jscomp.mozilla.rhino.Parser;
import com.google.javascript.jscomp.mozilla.rhino.ast.AstRoot;
import com.google.javascript.jscomp.mozilla.rhino.ast.Block;
import com.google.javascript.jscomp.mozilla.rhino.ast.FunctionNode;
import com.google.javascript.jscomp.parsing.IRFactory;
import com.google.javascript.rhino.Node;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

public class IRFactoryDeepseekTest {

  private static class TestErrorReporter implements ErrorReporter {
    final List<String> errors = new ArrayList<String>();
    final List<String> warnings = new ArrayList<String>();

    @Override
    public void error(String message, String sourceName, int line, int lineOffset) {
      errors.add(message);
    }

    @Override
    public void warning(String message, String sourceName, int line, int lineOffset) {
      warnings.add(message);
    }
  }

  private Node transform(String source) {
    TestErrorReporter reporter = new TestErrorReporter();
    Parser parser = new Parser(reporter, "test");
    AstRoot ast = parser.parse(source, "test", 0);
    Node result = IRFactory.transformTree(ast, source, reporter, "test");
    assertNotNull(result);
    return result;
  }

  @Test
  public void testUnnamedFunctionStatementReportsError() {
    TestErrorReporter reporter = new TestErrorReporter();
    AstRoot root = new AstRoot();
    root.setSourceName("test");

    FunctionNode fn = new FunctionNode();
    fn.setFunctionType(FunctionNode.FUNCTION_STATEMENT);
    fn.setBody(new Block());
    root.addChild(fn);

    Node result = IRFactory.transformTree(root, "function() {}", reporter, "test");

    assertNotNull(result);
    assertTrue("Expected 'unnamed function statement' error",
        reporter.errors.contains("unnamed function statement"));
  }

  @Test
  public void testUnnamedFunctionExpressionAllowed() {
    TestErrorReporter reporter = new TestErrorReporter();
    AstRoot root = new AstRoot();
    root.setSourceName("test");

    FunctionNode fn = new FunctionNode();
    fn.setFunctionType(FunctionNode.FUNCTION_EXPRESSION);
    fn.setBody(new Block());
    root.addChild(fn);

    Node result = IRFactory.transformTree(root, "var x = function() {}", reporter, "test");

    assertNotNull(result);
    assertFalse("Anonymous function expression should not report an error",
        reporter.errors.contains("unnamed function statement"));
  }

  @Test
  public void testEmptyProgram() {
    transform("");
  }

  @Test
  public void testVariableDeclaration() {
    transform("var x = 1;");
  }

  @Test
  public void testFunctionDeclaration() {
    transform("function f() {}");
  }

  @Test
  public void testFunctionExpression() {
    transform("var f = function() {};");
  }

  @Test
  public void testObjectLiteral() {
    transform("var o = {a: 1, 'b': 2, 3: 3};");
  }

  @Test
  public void testArrayLiteral() {
    transform("var a = [1, , 2];");
  }

  @Test
  public void testIfStatement() {
    transform("if (a) b; else c;");
  }

  @Test
  public void testWhileStatement() {
    transform("while(a) b;");
  }

  @Test
  public void testDoWhileStatement() {
    transform("do b; while(a);");
  }

  @Test
  public void testForStatement() {
    transform("for(var i=0;i<10;i++) b;");
  }

  @Test
  public void testForInStatement() {
    transform("for (var k in o) b;");
  }

  @Test
  public void testSwitchStatement() {
    transform("switch(a){case 1: b; break; default: c;}");
  }

  @Test
  public void testTryCatchStatement() {
    transform("try { a; } catch (e) { b; } finally { c; }");
  }

  @Test
  public void testThrowStatement() {
    transform("throw e;");
  }

  @Test
  public void testReturnStatement() {
    transform("function f(){ return 1; }");
  }

  @Test
  public void testNewAndCall() {
    transform("var d = new Date(); var x = f(a,b);");
  }

  @Test
  public void testUnaryOperators() {
    transform("var a = !b; var c = ~d; var e = -f; var g = +h; var i = typeof j; var k = void 0;");
  }

  @Test
  public void testBinaryOperators() {
    transform("var a = b + c - d * e / f % g;"
        + " var h = i & j | k ^ l;"
        + " var m = n << o >> p >>> q;"
        + " var r = s < t <= u > v >= w;"
        + " var x = y == z != a === b !== c;"
        + " var i = j && k || l;"
        + " var m = n in o;"
        + " var p = q instanceof r;");
  }

  @Test
  public void testAssignmentOperators() {
    transform("a = b; a += b; a -= b; a *= b; a /= b; a %= b;"
        + " a <<= b; a >>= b; a >>>= b; a &= b; a |= b; a ^= b;");
  }

  @Test
  public void testIncDec() {
    transform("a++; a--; ++a; --a;");
  }

  @Test
  public void testDelete() {
    transform("delete a.b;");
  }

  @Test
  public void testThis() {
    transform("this;");
  }

  @Test
  public void testLiterals() {
    transform("var a = null; var b = true; var c = false; var d = 'str'; var e = 123;");
  }

  @Test
  public void testRegexp() {
    transform("var r = /abc/g;");
  }

  @Test
  public void testGetterSetter() {
    transform("var o = {get a() { return 1; }, set a(v) {}};");
  }

  @Test
  public void testLabel() {
    transform("label: for(;;) break label;");
  }

  @Test
  public void testWith() {
    transform("with (o) { a; }");
  }
}
