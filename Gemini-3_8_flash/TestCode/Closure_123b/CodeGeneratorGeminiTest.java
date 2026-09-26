package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.jscomp.CompilerOptions.LanguageMode;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Targeted Defect (Closure / CodePrinterTest::testPrintInOperatorInForLoop):
 *    - In Token.HOOK inside a for-init clause (Context.IN_FOR_INIT_CLAUSE), CodeGenerator
 *      resets rhsContext to Context.OTHER instead of preserving the NO_IN constraint via
 *      getContextForNoInOperator(context).
 *    - When an array literal or expression in the ternary branch contains an 'in' operator,
 *      e.g., 'for (a = c ? 0 : [0 in d];; ) foo();', the 'in' expression must be parenthesized
 *      as '[(0 in d)]' to avoid syntax ambiguity with 'for (.. in ..)'.
 *    - On the defective CodeGenerator, it outputs 'for(a=c?0:[0 in d];;)foo()', causing a failure.
 *
 * 2. Binary Operators & Associativity:
 *    - Associative operators (e.g., '*' where last.getType() == type) vs non-associative ('-').
 *    - Assignment operator right-associativity (e.g., 'a = b = c').
 *    - Binary operator unrolling for left-recursive chains.
 *
 * 3. Statement & Expression Branches:
 *    - Control flow: IF with/without ELSE, dangling else disambiguation (BEFORE_DANGLING_ELSE).
 *    - Loops: FOR (3 and 4 children), WHILE, DO-WHILE, WITH.
 *    - Exception handling: TRY (with CATCH, FINALLY, or both), CATCH, THROW.
 *    - Jumps: RETURN (with/without expr), BREAK/CONTINUE (labeled/unlabeled), DEBUGGER.
 *    - Calls: Normal call, indirect eval preserved as (0, eval)(), free calls on property access.
 *    - Literals: NULL, THIS, TRUE, FALSE, NUMBER, STRING, ARRAYLIT (with holes), OBJECTLIT.
 *    - Getters/Setters: GETTER_DEF, SETTER_DEF with identifier, simple number, and string names.
 *    - Unary ops: TYPEOF, VOID, NOT, BITNOT, POS, NEG (-5 vs -x), INC, DEC (pre/post).
 *    - Casts: CAST ((x)).
 *    - Property/Element access: GETPROP (number parens: (1).toString(), ES3 keywords: a['default']), GETELEM.
 *    - Browser bug workarounds: Safari function declarations in blocks, IE6/7 do-while in blocks.
 *
 * 4. Helper & Escaping Coverage:
 *    - isSimpleNumber & getSimpleNumber boundaries (0, MAX_POSITIVE_INTEGER_NUMBER, long overflow).
 *    - identifierEscape: Latin fast path, non-Latin unicode escaping, supplementary characters.
 *    - strEscape: Quote preferences (preferSingleQuotes), control characters (\0, \b, \f, \n, \r, \t, \\, \v),
 *      HTML security breakers (</script, <!--, -->, ]]>), trustedStrings flag, charset encoders (ASCII vs UTF-8).
 *    - String cache memoization.
 *
 * 5. Defensive Exception Guards:
 *    - Invalid child counts, unexpected AST node subclasses, missing BLOCK children.
 */
public class CodeGeneratorGeminiTest {

  /**
   * Minimal test consumer collecting generator output for deterministic unit assertions.
   */
  private static class TestConsumer extends CodeConsumer {
    final StringBuilder sb = new StringBuilder();
    boolean continueProcessing = true;
    boolean preserveExtraBlocks = false;

    @Override
    void append(String str) {
      sb.append(str);
    }

    @Override
    char getLastChar() {
      return sb.length() > 0 ? sb.charAt(sb.length() - 1) : '\0';
    }

    @Override
    boolean continueProcessing() {
      return continueProcessing;
    }

    @Override
    boolean shouldPreserveExtraBlocks() {
      return preserveExtraBlocks;
    }

    String getOutput() {
      return sb.toString();
    }
  }

  private String printJs(String js) {
    Compiler compiler = new Compiler();
    Node n = compiler.parseTestCode(js);
    return new CodePrinter.Builder(n).build();
  }

  private String printJsWithOptions(String js, CompilerOptions options) {
    Compiler compiler = new Compiler();
    Node n = compiler.parseTestCode(js);
    CodePrinter.Builder builder = new CodePrinter.Builder(n);
    builder.setCompilerOptions(options);
    return builder.build();
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
  // =========================================================================

  @Test(timeout = 4000)
  public void testPrintInOperatorInForLoop_defects4jDefect() {
    Compiler compiler = new Compiler();
    Node n = compiler.parseTestCode("for (a = (c ? 0 : [0 in d]);; ) foo();");
    String result = new CodePrinter.Builder(n).build();
    // Exposes known defect: on defective version, prints "for(a=c?0:[0 in d];;)foo()"
    assertEquals("for(a=c?0:[(0 in d)];;)foo()", result);
  }

  @Test(timeout = 4000)
  public void testPrintInOperatorInForLoop_hookVariations() {
    Compiler compiler = new Compiler();
    Node n1 = compiler.parseTestCode("for (a = (c ? 0 : (0 in d));; ) foo();");
    String result1 = new CodePrinter.Builder(n1).build();
    assertEquals("for(a=c?0:(0 in d);;)foo()", result1);

    Node n2 = compiler.parseTestCode("for (a = (c ? (0 in d) : 0);; ) foo();");
    String result2 = new CodePrinter.Builder(n2).build();
    assertEquals("for(a=c?(0 in d):0;;)foo()", result2);
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testTagAsStrict() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    cg.tagAsStrict();
    assertEquals("'use strict';", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testContinueProcessingHalt() {
    TestConsumer consumer = new TestConsumer();
    consumer.continueProcessing = false;
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node n = new Node(Token.EMPTY);
    cg.add(n);
    assertEquals("", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testBinaryOperatorsAssociativity() {
    // Associative operator unrolling: a * (b * c) -> a * b * c
    assertEquals("a*b*c", printJs("a * (b * c)"));

    // Non-associative operator must preserve parentheses: a - (b - c)
    assertEquals("a-(b-c)", printJs("a - (b - c)"));

    // Right-associative assignment: a = (b = c) -> a = b = c
    assertEquals("a=b=c", printJs("a = (b = c)"));

    // Deeply nested binary expression unrolling
    assertEquals("1+2+3+4", printJs("1 + 2 + 3 + 4"));
  }

  @Test(timeout = 4000)
  public void testUnaryOperators() {
    assertEquals("+x", printJs("+x"));
    assertEquals("-x", printJs("-x"));
    assertEquals("-2", printJs("-2")); // NEG with number optimizes directly
    assertEquals("!x", printJs("!x"));
    assertEquals("~x", printJs("~x"));
    assertEquals("typeof x", printJs("typeof x"));
    assertEquals("void 0", printJs("void 0"));
  }

  @Test(timeout = 4000)
  public void testIncDecPrefixAndPostfix() {
    assertEquals("x++", printJs("x++"));
    assertEquals("x--", printJs("x--"));
    assertEquals("++x", printJs("++x"));
    assertEquals("--x", printJs("--x"));
  }

  @Test(timeout = 4000)
  public void testTryCatchFinally() {
    assertEquals("try{foo()}catch(e){bar()}", printJs("try { foo(); } catch(e) { bar(); }"));
    assertEquals("try{foo()}finally{baz()}", printJs("try { foo(); } finally { baz(); }"));
    assertEquals("try{foo()}catch(e){bar()}finally{baz()}",
        printJs("try { foo(); } catch(e) { bar(); } finally { baz(); }"));
  }

  @Test(timeout = 4000)
  public void testControlFlowStatements() {
    assertEquals("while(true)foo()", printJs("while(true) foo();"));
    assertEquals("do foo();while(true)", printJs("do foo(); while(true);"));
    assertEquals("for(var i=0;i<10;i++)foo()", printJs("for(var i = 0; i < 10; i++) foo();"));
    assertEquals("for(i=0;i<10;i++)foo()", printJs("for(i = 0; i < 10; i++) foo();"));
    assertEquals("for(var x in y)foo()", printJs("for(var x in y) foo();"));
    assertEquals("with(x)foo()", printJs("with(x) foo();"));
    assertEquals("switch(x){case 1:foo();break;default:bar()}",
        printJs("switch(x) { case 1: foo(); break; default: bar(); }"));
  }

  @Test(timeout = 4000)
  public void testIfElseAndDanglingElseDisambiguation() {
    assertEquals("if(x)foo()", printJs("if(x) foo();"));
    assertEquals("if(x)foo();else bar()", printJs("if(x) foo(); else bar();"));
    assertEquals("if(x){if(y)foo()}else bar()", printJs("if(x) { if(y) foo(); } else bar();"));
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationsAndExpressions() {
    assertEquals("function f(a,b){return a+b}", printJs("function f(a, b) { return a + b; }"));
    assertEquals("(function(){})", printJs("(function() {})"));
  }

  @Test(timeout = 4000)
  public void testObjectLiteralAndGettersSetters() {
    assertEquals("({a:1,b:2})", printJs("({a: 1, b: 2});"));
    assertEquals("({get foo(){return 1}})", printJs("({get foo() { return 1; }});"));
    assertEquals("({set foo(x){}})", printJs("({set foo(x) { }});"));
    assertEquals("({1:\"one\"})", printJs("({1: 'one'});"));
    assertEquals("({\"a b\":1})", printJs("({'a b': 1});"));
  }

  @Test(timeout = 4000)
  public void testArrayLiteralWithHoles() {
    assertEquals("[]", printJs("[]"));
    assertEquals("[1,2]", printJs("[1, 2]"));
    assertEquals("[,]", printJs("[,]"));
    assertEquals("[1,,2]", printJs("[1, , 2]"));
    assertEquals("[1,2,]", printJs("[1, 2, ]"));
  }

  @Test(timeout = 4000)
  public void testPropertyAccessAndKeywords() {
    assertEquals("a.b", printJs("a.b"));
    assertEquals("(1).toString()", printJs("(1).toString()"));

    CompilerOptions optionsEs3 = new CompilerOptions();
    optionsEs3.setLanguageOut(LanguageMode.ECMASCRIPT3);
    assertEquals("a[\"default\"]", printJsWithOptions("a.default", optionsEs3));

    CompilerOptions optionsEs5 = new CompilerOptions();
    optionsEs5.setLanguageOut(LanguageMode.ECMASCRIPT5);
    assertEquals("a.default", printJsWithOptions("a.default", optionsEs5));
  }

  @Test(timeout = 4000)
  public void testCallsAndEvalPreservation() {
    assertEquals("eval(x)", printJs("eval(x)"));
    assertEquals("foo(a,b)", printJs("foo(a, b)"));

    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node evalName = Node.newString(Token.NAME, "eval");
    Node call = new Node(Token.CALL, evalName, Node.newString("x"));
    // evalName not marked DIRECT_EVAL triggers indirect eval wrapper
    cg.add(call);
    assertEquals("(0,eval)(\"x\")", consumer.getOutput());

    TestConsumer consumerFreeCall = new TestConsumer();
    CodeGenerator cgFreeCall = CodeGenerator.forCostEstimation(consumerFreeCall);
    Node getprop = new Node(Token.GETPROP, Node.newString(Token.NAME, "o"), Node.newString("f"));
    Node freeCall = new Node(Token.CALL, getprop);
    freeCall.putBooleanProp(Node.FREE_CALL, true);
    cgFreeCall.add(freeCall);
    assertEquals("(0,o.f)()", consumerFreeCall.getOutput());
  }

  @Test(timeout = 4000)
  public void testStatementsAndLiterals() {
    assertEquals("loop:while(true){break loop;continue loop}",
        printJs("loop: while(true) { break loop; continue loop; }"));
    assertEquals("while(true){break;continue}", printJs("while(true) { break; continue; }"));
    assertEquals("debugger", printJs("debugger;"));
    assertEquals("throw\"err\"", printJs("throw 'err';"));
    assertEquals("null", printJs("null"));
    assertEquals("this", printJs("this"));
    assertEquals("true", printJs("true"));
    assertEquals("false", printJs("false"));
    assertEquals("new Foo", printJs("new Foo"));
    assertEquals("new Foo(1,2)", printJs("new Foo(1, 2)"));
    assertEquals("new (foo())()", printJs("new (foo())()"));
    assertEquals("delete a.b", printJs("delete a.b"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testIsSimpleNumber() {
    assertFalse(CodeGenerator.isSimpleNumber(""));
    assertTrue(CodeGenerator.isSimpleNumber("0"));
    assertTrue(CodeGenerator.isSimpleNumber("7"));
    assertTrue(CodeGenerator.isSimpleNumber("123456789"));
    assertFalse(CodeGenerator.isSimpleNumber("01"));
    assertFalse(CodeGenerator.isSimpleNumber("00"));
    assertFalse(CodeGenerator.isSimpleNumber("-1"));
    assertFalse(CodeGenerator.isSimpleNumber("12a3"));
    assertFalse(CodeGenerator.isSimpleNumber(" 10"));
  }

  @Test(timeout = 4000)
  public void testGetSimpleNumber() {
    assertEquals(0.0, CodeGenerator.getSimpleNumber("0"), 0.0);
    assertEquals(123.0, CodeGenerator.getSimpleNumber("123"), 0.0);
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("01")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("99999999999999999999999999999999999999999")));
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber(String.valueOf(NodeUtil.MAX_POSITIVE_INTEGER_NUMBER))));
    assertEquals((double) (NodeUtil.MAX_POSITIVE_INTEGER_NUMBER - 1),
        CodeGenerator.getSimpleNumber(String.valueOf(NodeUtil.MAX_POSITIVE_INTEGER_NUMBER - 1)), 0.0);
  }

  @Test(timeout = 4000)
  public void testIdentifierEscape() {
    assertEquals("validIdent_123", CodeGenerator.identifierEscape("validIdent_123"));
    assertEquals("foo\\u00a0bar", CodeGenerator.identifierEscape("foo\u00A0bar"));
    assertEquals("\\u0000", CodeGenerator.identifierEscape("\0"));
    assertEquals("\\u001f", CodeGenerator.identifierEscape("\u001F"));
    assertEquals("\\u007f", CodeGenerator.identifierEscape("\u007F"));

    String supplementary = new String(Character.toChars(0x10000));
    assertEquals("\\ud800\\udc00", CodeGenerator.identifierEscape(supplementary));
  }

  @Test(timeout = 4000)
  public void testStringEscapingControlCharacters() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);

    assertEquals("\"\\x00\"", cg.escapeToDoubleQuotedJsString("\0"));
    assertEquals("\"\\x0B\"", cg.escapeToDoubleQuotedJsString("\u000B"));
    assertEquals("\"\\b\"", cg.escapeToDoubleQuotedJsString("\b"));
    assertEquals("\"\\f\"", cg.escapeToDoubleQuotedJsString("\f"));
    assertEquals("\"\\n\"", cg.escapeToDoubleQuotedJsString("\n"));
    assertEquals("\"\\r\"", cg.escapeToDoubleQuotedJsString("\r"));
    assertEquals("\"\\t\"", cg.escapeToDoubleQuotedJsString("\t"));
    assertEquals("\"\\\\\"", cg.escapeToDoubleQuotedJsString("\\"));
    assertEquals("\"\\\"\"", cg.escapeToDoubleQuotedJsString("\""));
    assertEquals("\"'\"", cg.escapeToDoubleQuotedJsString("'"));
    assertEquals("\"\\u2028\"", cg.escapeToDoubleQuotedJsString("\u2028"));
    assertEquals("\"\\u2029\"", cg.escapeToDoubleQuotedJsString("\u2029"));
  }

  @Test(timeout = 4000)
  public void testHtmlSecurityBreakerEscaping() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);

    assertEquals("\"\\x3c/script\"", cg.escapeToDoubleQuotedJsString("</script"));
    assertEquals("\"\\x3c/SCRIPT\"", cg.escapeToDoubleQuotedJsString("</SCRIPT"));
    assertEquals("\"\\x3c!--\"", cg.escapeToDoubleQuotedJsString("<!--"));
    assertEquals("\"--\\x3e\"", cg.escapeToDoubleQuotedJsString("-->"));
    assertEquals("\"]]\\x3e\"", cg.escapeToDoubleQuotedJsString("]]>"));

    CompilerOptions untrustedOptions = new CompilerOptions();
    untrustedOptions.trustedStrings = false;
    CodeGenerator cgUntrusted = new CodeGenerator(consumer, untrustedOptions);
    assertEquals("\"\\x3d\"", cgUntrusted.escapeToDoubleQuotedJsString("="));
    assertEquals("\"\\x26\"", cgUntrusted.escapeToDoubleQuotedJsString("&"));
    assertEquals("\"\\x3e\"", cgUntrusted.escapeToDoubleQuotedJsString(">"));
    assertEquals("\"\\x3c\"", cgUntrusted.escapeToDoubleQuotedJsString("<"));
  }

  @Test(timeout = 4000)
  public void testPreferSingleQuotesOptimization() {
    TestConsumer consumer = new TestConsumer();
    CompilerOptions options = new CompilerOptions();
    options.preferSingleQuotes = true;
    CodeGenerator cg = new CodeGenerator(consumer, options);

    Node str = Node.newString("hello \"world\"");
    cg.add(str);
    assertEquals("'hello \"world\"'", consumer.getOutput());

    TestConsumer consumer2 = new TestConsumer();
    CodeGenerator cg2 = new CodeGenerator(consumer2, options);
    Node str2 = Node.newString("it's 'great'");
    cg2.add(str2);
    assertEquals("\"it's 'great'\"", consumer2.getOutput());
  }

  @Test(timeout = 4000)
  public void testRegexpEscapingHelper() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);

    assertEquals("/abc/", cg.regexpEscape("abc"));
    assertEquals("/a=b&c/", cg.regexpEscape("a=b&c"));
  }

  @Test(timeout = 4000)
  public void testCharsetEncoderHandling() {
    TestConsumer consumerAscii = new TestConsumer();
    CompilerOptions optionsAscii = new CompilerOptions();
    optionsAscii.setOutputCharset("US-ASCII");
    CodeGenerator cgAscii = new CodeGenerator(consumerAscii, optionsAscii);
    assertEquals("\"\\u00e9\"", cgAscii.escapeToDoubleQuotedJsString("\u00E9"));

    TestConsumer consumerUtf8 = new TestConsumer();
    CompilerOptions optionsUtf8 = new CompilerOptions();
    optionsUtf8.setOutputCharset("UTF-8");
    CodeGenerator cgUtf8 = new CodeGenerator(consumerUtf8, optionsUtf8);
    assertEquals("\"\u00E9\"", cgUtf8.escapeToDoubleQuotedJsString("\u00E9"));
  }

  @Test(timeout = 4000)
  public void testSlashVPropertyAnnotation() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node n = Node.newString("\u000B");
    n.putBooleanProp(Node.SLASH_V, true);
    cg.add(n);
    assertEquals("\"\\v\"", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testStringEscapingCacheHit() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node n1 = Node.newString("cached_val");
    Node n2 = Node.newString("cached_val");
    cg.add(n1);
    cg.add(n2);
    assertEquals("\"cached_val\"\"cached_val\"", consumer.getOutput());
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testBinaryOperatorWrongChildCountThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node add = new Node(Token.ADD, Node.newString(Token.NAME, "a"));
    cg.add(add);
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testUnknownTokenTypeThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node unknown = new Node(999999);
    cg.add(unknown);
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testRegexpNonStringChildThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node regexp = new Node(Token.REGEXP, Node.newNumber(1.0));
    cg.add(regexp);
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testLabelFirstChildNotLabelNameThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node label = new Node(Token.LABEL, Node.newString(Token.NAME, "foo"), new Node(Token.BLOCK));
    cg.add(label);
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testContinueChildNotLabelNameThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node cont = new Node(Token.CONTINUE, Node.newString(Token.NAME, "foo"));
    cg.add(cont);
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testBreakChildNotLabelNameThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node brk = new Node(Token.BREAK, Node.newString(Token.NAME, "foo"));
    cg.add(brk);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testStringKeyInvalidChildCountThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node key = Node.newString(Token.STRING_KEY, "k");
    cg.add(key);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testStringWithChildrenThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node str = Node.newString("val");
    str.addChildToBack(new Node(Token.EMPTY));
    cg.add(str);
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testFunctionUnexpectedNodeSubclassThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node subFn = new Node(Token.FUNCTION) {};
    cg.add(subFn);
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testScriptUnexpectedNodeSubclassThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node subScript = new Node(Token.SCRIPT) {};
    cg.add(subScript);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testGetpropWrongChildCountThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node getprop = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"));
    cg.add(getprop);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testGetpropRhsNotStringThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node getprop = new Node(Token.GETPROP, Node.newString(Token.NAME, "a"), Node.newNumber(1.0));
    cg.add(getprop);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testGetelemWrongChildCountThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node getelem = new Node(Token.GETELEM, Node.newString(Token.NAME, "a"));
    cg.add(getelem);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testWithWrongChildCountThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node withNode = new Node(Token.WITH, Node.newString(Token.NAME, "a"));
    cg.add(withNode);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testDoWrongChildCountThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node doNode = new Node(Token.DO, new Node(Token.BLOCK));
    cg.add(doNode);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testWhileWrongChildCountThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node whileNode = new Node(Token.WHILE, Node.newString(Token.NAME, "a"));
    cg.add(whileNode);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testForWrongChildCountThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node forNode = new Node(Token.FOR, new Node(Token.EMPTY), new Node(Token.EMPTY));
    cg.add(forNode);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testTryNotBlockChildThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node tryNode = new Node(Token.TRY, new Node(Token.EMPTY), new Node(Token.EMPTY));
    cg.add(tryNode);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testCatchWrongChildCountThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node catchNode = new Node(Token.CATCH, Node.newString(Token.NAME, "e"));
    cg.add(catchNode);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testThrowWrongChildCountThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node throwNode = new Node(Token.THROW);
    cg.add(throwNode);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testReturnWrongChildCountThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node retNode = new Node(Token.RETURN, Node.newNumber(1), Node.newNumber(2));
    cg.add(retNode);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testLabelNameEmptyThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node labelName = Node.newString(Token.LABEL_NAME, "");
    cg.add(labelName);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testHookWrongChildCountThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node hook = new Node(Token.HOOK, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    cg.add(hook);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testCaseWrongChildCountThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node caseNode = new Node(Token.CASE, Node.newNumber(1));
    cg.add(caseNode);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testDefaultCaseWrongChildCountThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node defaultCase = new Node(Token.DEFAULT_CASE, new Node(Token.BLOCK), new Node(Token.BLOCK));
    cg.add(defaultCase);
  }

  @Test(timeout = 4000, expected = IllegalStateException.class)
  public void testDelpropWrongChildCountThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node del = new Node(Token.DELPROP);
    cg.add(del);
  }

  @Test(timeout = 4000, expected = Error.class)
  public void testAddNonEmptyStatementMissingBlockThrows() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node whileNode = new Node(Token.WHILE, Node.newString(Token.NAME, "a"), Node.newString(Token.NAME, "b"));
    cg.add(whileNode);
  }

  // =========================================================================
  // Partition E: Object Lifecycle, Low-Level Methods & Workaround Paths
  // =========================================================================

  @Test(timeout = 4000)
  public void testSafariFunctionInBlockWorkaround() {
    assertEquals("if(x){function f(){}}", printJs("if(x){function f(){}}"));
  }

  @Test(timeout = 4000)
  public void testEmptyBlockHandling() {
    assertEquals("if(x);", printJs("if(x);"));
    assertEquals("if(x);", printJs("if(x){}"));
  }

  @Test(timeout = 4000)
  public void testPreserveExtraBlocksInEmptyBlock() {
    TestConsumer consumer = new TestConsumer();
    consumer.preserveExtraBlocks = true;
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node block = new Node(Token.BLOCK);
    Node ifNode = new Node(Token.IF, Node.newString(Token.NAME, "cond"), block);
    cg.add(ifNode);
    assertTrue(consumer.getOutput().contains("if(cond)"));
  }

  @Test(timeout = 4000)
  public void testDirectListHelpers() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node n1 = Node.newNumber(1.0);
    Node n2 = Node.newNumber(2.0);
    n1.setNext(n2);
    cg.addList(n1);
    assertEquals("1,2", consumer.getOutput());

    TestConsumer consumerArray = new TestConsumer();
    CodeGenerator cgArray = CodeGenerator.forCostEstimation(consumerArray);
    Node e1 = Node.newNumber(1.0);
    Node empty = new Node(Token.EMPTY);
    e1.setNext(empty);
    cgArray.addArrayList(e1);
    assertEquals("1,,", consumerArray.getOutput());
  }

  @Test(timeout = 4000)
  public void testAddAllSiblingsAndCaseBody() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node n1 = Node.newNumber(1.0);
    Node n2 = Node.newNumber(2.0);
    n1.setNext(n2);
    cg.addAllSiblings(n1);
    assertEquals("12", consumer.getOutput());

    TestConsumer consumerCase = new TestConsumer();
    CodeGenerator cgCase = CodeGenerator.forCostEstimation(consumerCase);
    cgCase.addCaseBody(new Node(Token.BLOCK));
    assertNotNull(consumerCase.getOutput());
  }

  @Test(timeout = 4000)
  public void testCastTokenNode() {
    TestConsumer consumer = new TestConsumer();
    CodeGenerator cg = CodeGenerator.forCostEstimation(consumer);
    Node cast = new Node(Token.CAST, Node.newString(Token.NAME, "val"));
    cg.add(cast);
    assertEquals("(val)", consumer.getOutput());
  }

  @Test(timeout = 4000)
  public void testObjectLitKeywordsEs3VsEs5() {
    CompilerOptions optionsEs3 = new CompilerOptions();
    optionsEs3.setLanguageOut(LanguageMode.ECMASCRIPT3);
    assertEquals("({\"default\":1})", printJsWithOptions("({default: 1})", optionsEs3));

    CompilerOptions optionsEs5 = new CompilerOptions();
    optionsEs5.setLanguageOut(LanguageMode.ECMASCRIPT5);
    assertEquals("({default:1})", printJsWithOptions("({default: 1})", optionsEs5));
  }

  @Test(timeout = 4000)
  public void testObjectLitNonLatinKeyQuoted() {
    assertEquals("({\"\\u00e9\":1})", printJs("({\u00E9: 1})"));
  }

  @Test(timeout = 4000)
  public void testGetterSetterNumericAndStringNames() {
    assertEquals("({get 1(){return 0}})", printJs("({get 1() { return 0; }})"));
    assertEquals("({set 1(x){}})", printJs("({set 1(x) { }})"));
  }

  @Test(timeout = 4000)
  public void testIncDecPropertiesExplicit() {
    TestConsumer c1 = new TestConsumer();
    CodeGenerator cg1 = CodeGenerator.forCostEstimation(c1);
    Node preInc = new Node(Token.INC, Node.newString(Token.NAME, "i"));
    preInc.putIntProp(Node.INCRDECR_PROP, 0);
    cg1.add(preInc);
    assertEquals("++i", c1.getOutput());

    TestConsumer c2 = new TestConsumer();
    CodeGenerator cg2 = CodeGenerator.forCostEstimation(c2);
    Node postInc = new Node(Token.INC, Node.newString(Token.NAME, "i"));
    postInc.putIntProp(Node.INCRDECR_PROP, 1);
    cg2.add(postInc);
    assertEquals("i++", c2.getOutput());

    TestConsumer c3 = new TestConsumer();
    CodeGenerator cg3 = CodeGenerator.forCostEstimation(c3);
    Node preDec = new Node(Token.DEC, Node.newString(Token.NAME, "i"));
    preDec.putIntProp(Node.INCRDECR_PROP, 0);
    cg3.add(preDec);
    assertEquals("--i", c3.getOutput());

    TestConsumer c4 = new TestConsumer();
    CodeGenerator cg4 = CodeGenerator.forCostEstimation(c4);
    Node postDec = new Node(Token.DEC, Node.newString(Token.NAME, "i"));
    postDec.putIntProp(Node.INCRDECR_PROP, 1);
    cg4.add(postDec);
    assertEquals("i--", c4.getOutput());
  }

  @Test(timeout = 4000)
  public void testNameWithCommaChildPrecedence() {
    assertEquals("var a=(1,2)", printJs("var a = (1, 2);"));
  }
}
