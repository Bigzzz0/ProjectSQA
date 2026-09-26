package com.google.javascript.jscomp.parsing;

import static org.junit.Assert.*;

import com.google.javascript.jscomp.parsing.Config.LanguageMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import com.google.javascript.rhino.head.CompilerEnvirons;
import com.google.javascript.rhino.head.ErrorReporter;
import com.google.javascript.rhino.head.EvaluatorException;
import com.google.javascript.rhino.head.Parser;
import com.google.javascript.rhino.head.ast.AstRoot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 * - handleBlockComment: original heuristic only matches "/* @" and "\n * @";
 *   it fails to warn for extra spaces after "*", tabs, multiple spaces after
 *   the opening "/*", and annotation lines without a leading "*".
 * - transformDispatcher branches covered by real syntax snippets:
 *   assignments, infix/bitwise/shift/logical/comparison operators, unary ops,
 *   functions, if/else, do/while, for, for-in, switch, labels, try/catch,
 *   object/array literals, getters/setters, regexps, new, delete, with.
 * - defensive paths: invalid ES3 property names, unsupported getters/setters,
 *   getter/setter parameter validation, rejected const, invalid delete operand.
 */
public class IRFactoryDeepseekTest {

  private static class RecordingReporter implements ErrorReporter {
    final List<String> warnings = new ArrayList<String>();
    final List<String> errors = new ArrayList<String>();

    @Override
    public void warning(String message, String sourceName, int line,
        String lineSource, int lineOffset) {
      warnings.add(message);
    }

    @Override
    public void error(String message, String sourceName, int line,
        String lineSource, int lineOffset) {
      errors.add(message);
    }

    @Override
    public EvaluatorException runtimeError(String message, String sourceName,
        int line, String lineSource, int lineOffset) {
      return null;
    }
  }

  private static class TransformResult {
    final Node root;
    final RecordingReporter reporter;

    TransformResult(Node root, RecordingReporter reporter) {
      this.root = root;
      this.reporter = reporter;
    }
  }

  private static boolean containsMessage(List<String> messages, String fragment) {
    for (String message : messages) {
      if (message.contains(fragment)) {
        return true;
      }
    }
    return false;
  }

  private TransformResult transform(String source, LanguageMode languageMode,
      boolean acceptConstKeyword) {
    RecordingReporter reporter = new RecordingReporter();
    CompilerEnvirons env = new CompilerEnvirons();
    env.setRecordingComments(true);
    env.setRecordingLocalJsDocComments(true);
    env.setErrorReporter(reporter);

    Parser parser = new Parser(env, reporter);
    AstRoot ast = parser.parse(source, "test.js", 1);

    Config config = new Config(languageMode, false, acceptConstKeyword);
    Node root = IRFactory.transformTree(ast, null, source, config, reporter);
    return new TransformResult(root, reporter);
  }

  private TransformResult transform(String source, LanguageMode languageMode) {
    return transform(source, languageMode, true);
  }

  @Test(timeout = 4000)
  public void testSuspiciousBlockCommentWithMultipleSpacesAfterOpening() {
    String source = "/*  @type {number} */\nvar x = 3;";
    TransformResult result = transform(source, LanguageMode.ECMASCRIPT5);
    assertTrue(result.reporter.warnings.toString(),
        result.reporter.warnings.contains(IRFactory.SUSPICIOUS_COMMENT_WARNING));
  }

  @Test(timeout = 4000)
  public void testSuspiciousBlockCommentWithMultipleSpacesAfterAsterisk() {
    String source = "/*\n *  @type {number}\n */\nvar x = 3;";
    TransformResult result = transform(source, LanguageMode.ECMASCRIPT5);
    assertTrue(result.reporter.warnings.toString(),
        result.reporter.warnings.contains(IRFactory.SUSPICIOUS_COMMENT_WARNING));
  }

  @Test(timeout = 4000)
  public void testSuspiciousBlockCommentWithTabAfterAsterisk() {
    String source = "/*\n *\t@type {number}\n */\nvar x = 3;";
    TransformResult result = transform(source, LanguageMode.ECMASCRIPT5);
    assertTrue(result.reporter.warnings.toString(),
        result.reporter.warnings.contains(IRFactory.SUSPICIOUS_COMMENT_WARNING));
  }

  @Test(timeout = 4000)
  public void testSuspiciousBlockCommentWithoutLeadingStar() {
    String source = "/*\n   @type {number}\n */\nvar x = 3;";
    TransformResult result = transform(source, LanguageMode.ECMASCRIPT5);
    assertTrue(result.reporter.warnings.toString(),
        result.reporter.warnings.contains(IRFactory.SUSPICIOUS_COMMENT_WARNING));
  }

  @Test(timeout = 4000)
  public void testOrdinaryBlockCommentDoesNotWarn() {
    String source = "/* ordinary comment */\nvar x = 3;";
    TransformResult result = transform(source, LanguageMode.ECMASCRIPT5);
    assertFalse(result.reporter.warnings.toString(),
        result.reporter.warnings.contains(IRFactory.SUSPICIOUS_COMMENT_WARNING));
  }

  @Test(timeout = 4000)
  public void testJSDocCommentDoesNotTriggerSuspiciousWarning() {
    String source = "/** @type {number} */\nvar x = 3;";
    TransformResult result = transform(source, LanguageMode.ECMASCRIPT5);
    assertFalse(result.reporter.warnings.toString(),
        result.reporter.warnings.contains(IRFactory.SUSPICIOUS_COMMENT_WARNING));
  }

  @Test(timeout = 4000)
  public void testTransformExpressionStatement() {
    TransformResult result = transform("a = 1 + 2 * 3; foo();",
        LanguageMode.ECMASCRIPT5);
    Node root = result.root;
    assertEquals(Token.SCRIPT, root.getType());
    assertEquals(2, root.getChildCount());

    Node firstStmt = root.getFirstChild();
    assertEquals(Token.EXPR_RESULT, firstStmt.getType());
    Node assign = firstStmt.getFirstChild();
    assertEquals(Token.ASSIGN, assign.getType());
    assertEquals(Token.NAME, assign.getFirstChild().getType());
    assertEquals("a", assign.getFirstChild().getString());
    assertEquals(Token.ADD, assign.getSecondChild().getType());

    Node secondStmt = firstStmt.getNext();
    assertEquals(Token.EXPR_RESULT, secondStmt.getType());
    assertEquals(Token.CALL, secondStmt.getFirstChild().getType());
    assertEquals(Token.NAME,
        secondStmt.getFirstChild().getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testTransformVariableDeclarationWithNegativeNumbers() {
    TransformResult result = transform("var x = -1, y = 2;",
        LanguageMode.ECMASCRIPT5);
    Node var = result.root.getFirstChild();
    assertEquals(Token.VAR, var.getType());
    assertEquals(2, var.getChildCount());

    Node x = var.getFirstChild();
    assertEquals(Token.NAME, x.getType());
    assertEquals("x", x.getString());
    assertNotNull(x.getFirstChild());
    assertEquals(Token.NUMBER, x.getFirstChild().getType());
    assertEquals(-1.0, x.getFirstChild().getDouble(), 0.0);

    Node y = x.getNext();
    assertEquals(Token.NAME, y.getType());
    assertNotNull(y.getFirstChild());
    assertEquals(Token.NUMBER, y.getFirstChild().getType());
    assertEquals(2.0, y.getFirstChild().getDouble(), 0.0);
  }

  @Test(timeout = 4000)
  public void testTransformFunctionDeclaration() {
    TransformResult result = transform("function f(a, b) { return a + b; }",
        LanguageMode.ECMASCRIPT5);
    Node fn = result.root.getFirstChild();
    assertEquals(Token.FUNCTION, fn.getType());

    Node name = fn.getFirstChild();
    assertEquals(Token.NAME, name.getType());
    assertEquals("f", name.getString());

    Node params = name.getNext();
    assertEquals(Token.PARAM_LIST, params.getType());
    assertEquals(2, params.getChildCount());

    Node body = params.getNext();
    assertEquals(Token.BLOCK, body.getType());
    assertEquals(Token.RETURN, body.getFirstChild().getType());
    assertEquals(Token.ADD, body.getFirstChild().getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testTransformIfAndDoWhile() {
    TransformResult result = transform(
        "if (a) { b(); } else { c(); } do { d(); } while (e);",
        LanguageMode.ECMASCRIPT5);
    Node root = result.root;
    assertEquals(2, root.getChildCount());

    Node ifNode = root.getFirstChild();
    assertEquals(Token.IF, ifNode.getType());
    assertEquals(3, ifNode.getChildCount());
    assertEquals(Token.NAME, ifNode.getFirstChild().getType());
    assertEquals(Token.BLOCK, ifNode.getFirstChild().getNext().getType());
    assertEquals(Token.BLOCK, ifNode.getFirstChild().getNext().getNext().getType());

    Node doNode = ifNode.getNext();
    assertEquals(Token.DO, doNode.getType());
    assertEquals(2, doNode.getChildCount());
    assertEquals(Token.BLOCK, doNode.getFirstChild().getType());
    assertEquals(Token.NAME, doNode.getSecondChild().getType());
  }

  @Test(timeout = 4000)
  public void testTransformForLoops() {
    TransformResult result = transform(
        "for (var i = 0; i < 10; i++) { x += i; }"
        + "for (var k in obj) { use(k); }",
        LanguageMode.ECMASCRIPT5);
    Node root = result.root;
    assertEquals(2, root.getChildCount());

    Node standardFor = root.getFirstChild();
    assertEquals(Token.FOR, standardFor.getType());
    assertEquals(4, standardFor.getChildCount());
    assertEquals(Token.VAR, standardFor.getFirstChild().getType());

    Node forIn = standardFor.getNext();
    assertEquals(Token.FOR, forIn.getType());
    assertEquals(3, forIn.getChildCount());
    assertEquals(Token.NAME, forIn.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testTransformSwitchAndLabel() {
    TransformResult result = transform(
        "switch (x) { case 1: y(); break; default: z(); }"
        + "label: for (;;) { break label; }",
        LanguageMode.ECMASCRIPT5);
    Node root = result.root;
    assertEquals(2, root.getChildCount());

    Node switchNode = root.getFirstChild();
    assertEquals(Token.SWITCH, switchNode.getType());
    assertEquals(3, switchNode.getChildCount());
    assertEquals(Token.NAME, switchNode.getFirstChild().getType());
    assertEquals(Token.CASE, switchNode.getFirstChild().getNext().getType());
    assertEquals(Token.DEFAULT_CASE,
        switchNode.getFirstChild().getNext().getNext().getType());

    Node labelNode = switchNode.getNext();
    assertEquals(Token.LABEL, labelNode.getType());
    assertEquals(2, labelNode.getChildCount());
    assertEquals(Token.LABEL_NAME, labelNode.getFirstChild().getType());
    assertEquals(Token.FOR, labelNode.getSecondChild().getType());
  }

  @Test(timeout = 4000)
  public void testTransformTryCatchFinally() {
    TransformResult result = transform(
        "try { throw e; } catch (e) { handle(e); } finally { cleanup(); }",
        LanguageMode.ECMASCRIPT5);
    Node tryNode = result.root.getFirstChild();
    assertEquals(Token.TRY, tryNode.getType());
    assertEquals(3, tryNode.getChildCount());

    Node tryBlock = tryNode.getFirstChild();
    assertEquals(Token.BLOCK, tryBlock.getType());

    Node catchBlock = tryBlock.getNext();
    assertEquals(Token.BLOCK, catchBlock.getType());
    assertEquals(Token.CATCH, catchBlock.getFirstChild().getType());

    Node catchNode = catchBlock.getFirstChild();
    assertEquals(2, catchNode.getChildCount());
    assertEquals(Token.NAME, catchNode.getFirstChild().getType());
    assertEquals(Token.BLOCK, catchNode.getSecondChild().getType());

    Node finallyBlock = catchBlock.getNext();
    assertEquals(Token.BLOCK, finallyBlock.getType());
  }

  @Test(timeout = 4000)
  public void testTransformObjectLiteralGetterSetter() {
    TransformResult result = transform(
        "var o = {a: 1, get b() { return 2; }, set b(v) { v; }};",
        LanguageMode.ECMASCRIPT5);
    Node varNode = result.root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());

    Node objectLit = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.OBJECTLIT, objectLit.getType());
    assertEquals(3, objectLit.getChildCount());
    assertEquals(Token.STRING_KEY, objectLit.getFirstChild().getType());
    assertEquals(Token.GETTER_DEF,
        objectLit.getFirstChild().getNext().getType());
    assertEquals(Token.SETTER_DEF,
        objectLit.getFirstChild().getNext().getNext().getType());
  }

  @Test(timeout = 4000)
  public void testTransformArrayAndRegexp() {
    TransformResult result = transform(
        "var a = [1, , \"x\"]; var r = /ab/g;",
        LanguageMode.ECMASCRIPT5);
    Node root = result.root;
    assertEquals(2, root.getChildCount());

    Node varA = root.getFirstChild();
    assertEquals(Token.VAR, varA.getType());
    Node arrayLit = varA.getFirstChild().getFirstChild();
    assertEquals(Token.ARRAYLIT, arrayLit.getType());
    assertEquals(3, arrayLit.getChildCount());
    assertEquals(Token.EMPTY, arrayLit.getFirstChild().getNext().getType());

    Node varR = varA.getNext();
    assertEquals(Token.VAR, varR.getType());
    Node regexp = varR.getFirstChild().getFirstChild();
    assertEquals(Token.REGEXP, regexp.getType());
    assertEquals(Token.STRING, regexp.getFirstChild().getType());
    assertEquals("ab", regexp.getFirstChild().getString());
    assertEquals(Token.STRING, regexp.getSecondChild().getType());
    assertEquals("g", regexp.getSecondChild().getString());
  }

  @Test(timeout = 4000)
  public void testTransformNewAndDeleteError() {
    TransformResult result = transform("var x = new Foo(1); delete 1;",
        LanguageMode.ECMASCRIPT5);
    Node root = result.root;
    assertEquals(2, root.getChildCount());

    Node varNode = root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    Node newExpr = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.NEW, newExpr.getType());

    assertTrue(containsMessage(result.reporter.errors, "Invalid delete operand"));
  }

  @Test(timeout = 4000)
  public void testTransformKeywordLiterals() {
    TransformResult result = transform(
        "var a = true, b = false, c = null, d = this;",
        LanguageMode.ECMASCRIPT5);
    Node varNode = result.root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    assertEquals(4, varNode.getChildCount());

    assertEquals(Token.TRUE, varNode.getFirstChild().getFirstChild().getType());
    assertEquals(Token.FALSE,
        varNode.getFirstChild().getNext().getFirstChild().getType());
    assertEquals(Token.NULL,
        varNode.getFirstChild().getNext().getNext().getFirstChild().getType());
    assertEquals(Token.THIS,
        varNode.getFirstChild().getNext().getNext().getNext().getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testTransformUnaryKeywordOperators() {
    TransformResult result = transform(
        "var a = !b, c = ~d, e = -f, g = +h, i = typeof j;",
        LanguageMode.ECMASCRIPT5);
    Node varNode = result.root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    assertEquals(5, varNode.getChildCount());

    assertEquals(Token.NOT, varNode.getFirstChild().getFirstChild().getType());
    assertEquals(Token.BITNOT,
        varNode.getFirstChild().getNext().getFirstChild().getType());
    assertEquals(Token.NEG,
        varNode.getFirstChild().getNext().getNext().getFirstChild().getType());
    assertEquals(Token.POS,
        varNode.getFirstChild().getNext().getNext().getNext().getFirstChild().getType());
    assertEquals(Token.TYPEOF,
        varNode.getFirstChild().getNext().getNext().getNext().getNext().getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testTransformMiscOperators() {
    String source = "a = b & c | d ^ e;"
        + "f = g << 2 >> 3 >>> 4;"
        + "h = i == j && k != l || m <= n;"
        + "o += p;"
        + "q = r % s;"
        + "t = u instanceof v;"
        + "w = x in y;";
    TransformResult result = transform(source, LanguageMode.ECMASCRIPT5);
    assertEquals(Token.SCRIPT, result.root.getType());
    assertTrue(result.reporter.errors.toString(), result.reporter.errors.isEmpty());
  }

  @Test(timeout = 4000)
  public void testTransformWithStatement() {
    TransformResult result = transform("with (obj) { x; }",
        LanguageMode.ECMASCRIPT5);
    Node withNode = result.root.getFirstChild();
    assertEquals(Token.WITH, withNode.getType());
    assertEquals(Token.NAME, withNode.getFirstChild().getType());
    assertEquals(Token.BLOCK, withNode.getSecondChild().getType());
  }

  @Test(timeout = 4000)
  public void testTransformEmptyStatement() {
    TransformResult result = transform(";", LanguageMode.ECMASCRIPT5);
    assertEquals(Token.EMPTY, result.root.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testDirectivesAreEncodedAndRemoved() {
    TransformResult result = transform("\"use strict\"; var x = 1;",
        LanguageMode.ECMASCRIPT5);
    Node root = result.root;
    assertEquals(Token.SCRIPT, root.getType());
    assertEquals(1, root.getChildCount());
    Set<String> directives = root.getDirectives();
    assertNotNull(directives);
    assertTrue(directives.contains("use strict"));
  }

  @Test(timeout = 4000)
  public void testCastNodeInjectionWithInlineJSDoc() {
    TransformResult result = transform(
        "var x = /** @type {number} */ (y);",
        LanguageMode.ECMASCRIPT5);
    Node varNode = result.root.getFirstChild();
    assertEquals(Token.VAR, varNode.getType());
    Node cast = varNode.getFirstChild().getFirstChild();
    assertEquals(Token.CAST, cast.getType());
    assertEquals(Token.NAME, cast.getFirstChild().getType());
  }

  @Test(timeout = 4000)
  public void testInvalidES3PropertyNameWarning() {
    TransformResult result = transform("var x = obj.class;",
        LanguageMode.ECMASCRIPT3);
    assertTrue(containsMessage(result.reporter.warnings,
        IRFactory.INVALID_ES3_PROP_NAME));
  }

  @Test(timeout = 4000)
  public void testGetterNotSupportedInES3() {
    TransformResult result = transform("var o = {get a() { return 1; }};",
        LanguageMode.ECMASCRIPT3);
    assertTrue(containsMessage(result.reporter.errors,
        IRFactory.GETTER_ERROR_MESSAGE));
  }

  @Test(timeout = 4000)
  public void testSetterNotSupportedInES3() {
    TransformResult result = transform("var o = {set a(v) { v; }};",
        LanguageMode.ECMASCRIPT3);
    assertTrue(containsMessage(result.reporter.errors,
        IRFactory.SETTER_ERROR_MESSAGE));
  }

  @Test(timeout = 4000)
  public void testGetterWithParameterError() {
    TransformResult result = transform("var o = {get a(x) { return x; }};",
        LanguageMode.ECMASCRIPT5);
    assertTrue(containsMessage(result.reporter.errors,
        "getters may not have parameters"));
  }

  @Test(timeout = 4000)
  public void testSetterWithoutParameterError() {
    TransformResult result = transform("var o = {set a() { }};",
        LanguageMode.ECMASCRIPT5);
    assertTrue(containsMessage(result.reporter.errors,
        "setters must have exactly one parameter"));
  }

  @Test(timeout = 4000)
  public void testConstRejectedWithoutFlag() {
    TransformResult result = transform("const x = 1;",
        LanguageMode.ECMASCRIPT5, false);
    assertTrue(containsMessage(result.reporter.errors, "Unsupported syntax"));
  }

  @Test(timeout = 4000)
  public void testConstAcceptedWithFlag() {
    TransformResult result = transform("const x = 1;",
        LanguageMode.ECMASCRIPT5, true);
    assertFalse(containsMessage(result.reporter.errors, "Unsupported syntax"));
  }
}
