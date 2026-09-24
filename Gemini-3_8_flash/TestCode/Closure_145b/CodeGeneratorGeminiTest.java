package com.google.javascript.jscomp;

import com.google.common.base.Charsets;
import com.google.javascript.rhino.Node;
import com.google.javascript.rhino.Token;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.google.javascript.jscomp.CodeGenerator
 *
 * 1. Defect-Targeted Branches (Defects4J ground truth):
 *    - isOneExactlyFunctionOrDo(Node n):
 *      In defective versions, this helper only checks `n.getType() == Token.FUNCTION || n.getType() == Token.DO`.
 *      When a function declaration or do-while loop is enclosed in a labeled statement inside an `if` block,
 *      e.g. `if (e1) { A: function goo() { return true; } }` or `if (x) { A: do { foo(); } while (y); }`,
 *      the single child is Token.LABEL rather than Token.FUNCTION/DO. Extraneous block stripping then incorrectly
 *      removes the curly braces, violating Safari and IE compatibility constraints.
 *      Targeted by: testFunctionSafariCompatibility() and testDoLoopIECompatibility().
 *
 * 2. Binary Operators & Associativity:
 *    - Child count == 2 check (IllegalStateException on invalid count)
 *    - Associative binary op (e.g. a * (b * c)) vs non-associative op (e.g. a - (b - c))
 *    - Assignment operator right-associativity (a = b = c)
 *    - Precedence nesting and parenthesis insertion
 *
 * 3. Statement & Expression Branches:
 *    - TRY / CATCH / FINALLY: with catch block, without catch block, with finally, empty catch condition error
 *    - FOR: 4-children standard for loop (with VAR vs EXPR init), 3-children for-in loop, Token.IN inside init clause
 *    - IF / ELSE: ambiguous else clause (Context.BEFORE_DANGLING_ELSE) triggering artificial block wrapping
 *    - OBJECTLIT: quoted vs unquoted keys (keywords, non-latin characters, non-identifiers), START_OF_EXPR wrapping
 *    - CALL: direct eval vs indirect eval ((0,eval))
 *    - NEW: constructor call with vs without arguments, containsCall forcing higher precedence
 *    - GETPROP: number receiver requiring parentheses `(1).toString()`
 *    - INC / DEC: prefix vs postfix operators (INCRDECR_PROP)
 *    - ARRAYLIT: empty, populated, and with skipped indices (Node.SKIP_INDEXES_PROP)
 *
 * 4. Escaping Logic:
 *    - jsString / strEscape: quote selection (single vs double quote count heuristic)
 *    - Escaping rules for `\n`, `\r`, `\t`, `\\`, `"`, `'`, `-->`, `]]>`, `</script>` (case-insensitive)
 *    - CharsetEncoder active (UTF-8, ISO-8859-1) vs ASCII fallback with Unicode hex escapes
 *    - identifierEscape: Latin vs non-Latin characters
 *
 * 5. Defensive Guard Paths & Exceptions:
 *    - Catch conditions netscape feature Error
 *    - Unexpected Node subclass on FUNCTION, SCRIPT, BLOCK
 *    - Unexpected token type on BREAK, CONTINUE, LABEL
 *    - Token.EXPR_VOID Error
 *    - Token.REGEXP non-string children Error
 *    - Missing BLOCK child Error
 *    - Unknown token type Error
 */
public class CodeGeneratorGeminiTest {

  private Node parse(String js) {
    Compiler compiler = new Compiler();
    return compiler.parseTestCode(js);
  }

  private String printNode(Node n) {
    return new CodePrinter.Builder(n).setPrettyPrint(false).build();
  }

  private String parsePrint(String js) {
    return printNode(parse(js));
  }

  // =========================================================================
  // Partition A: Core Functional Logic & State Transitions
  // =========================================================================

  @Test(timeout = 4000)
  public void testBinaryOperatorsAndAssociativity() {
    // Associative operator: * does not need parens
    assertEquals("a*b*c", parsePrint("a * (b * c)"));
    // Non-associative operator: - requires parens on right-hand side
    assertEquals("a-(b-c)", parsePrint("a - (b - c)"));
    // Assignment is right-associative
    assertEquals("a=b=c", parsePrint("a = (b = c)"));
  }

  @Test(timeout = 4000)
  public void testTryCatchFinallyVariations() {
    // try-catch
    assertEquals("try{x()}catch(e){y()}", parsePrint("try { x(); } catch (e) { y(); }"));
    // try-finally without catch
    assertEquals("try{x()}finally{z()}", parsePrint("try { x(); } finally { z(); }"));
    // try-catch-finally
    assertEquals("try{x()}catch(e){y()}finally{z()}", parsePrint("try { x(); } catch (e) { y(); } finally { z(); }"));
  }

  @Test(timeout = 4000)
  public void testControlFlowStatements() {
    // Return with and without value
    assertEquals("return", parsePrint("return;"));
    assertEquals("return 1", parsePrint("return 1;"));

    // Throw
    assertEquals("throw\"err\";", parsePrint("throw 'err';"));

    // While and Do-While
    assertEquals("while(true);", parsePrint("while(true);"));
    assertEquals("while(x){y()}", parsePrint("while(x) { y(); }"));
    assertEquals("do{foo()}while(y);", parsePrint("do { foo(); } while (y);"));

    // With
    assertEquals("with(obj){foo()}", parsePrint("with (obj) { foo(); }"));

    // Switch, case, default
    assertEquals("switch(x){case 1:foo();break;default:bar()}",
        parsePrint("switch(x) { case 1: foo(); break; default: bar(); }"));

    // Break & Continue with labels
    assertEquals("foo:while(true){break foo;continue foo}",
        parsePrint("foo: while (true) { break foo; continue foo; }"));
  }

  @Test(timeout = 4000)
  public void testForLoopVariations() {
    // Standard for loop with var
    assertEquals("for(var i=0;i<10;i++);", parsePrint("for (var i = 0; i < 10; i++);"));
    // Standard for loop with expr
    assertEquals("for(i=0;i<10;i++);", parsePrint("for (i = 0; i < 10; i++);"));
    // For-in loop
    assertEquals("for(var k in obj);", parsePrint("for (var k in obj);"));
    assertEquals("for(k in obj);", parsePrint("for (k in obj);"));
    // Context.IN_FOR_INIT_CLAUSE requiring parens around 'in' operator
    assertEquals("for(var x=(a in b);;);", parsePrint("for (var x = a in b; ; );"));
  }

  @Test(timeout = 4000)
  public void testIfElseAmbiguousDanglingElse() {
    // Dangling else resolution: inner if must be wrapped in block
    assertEquals("if(a){if(b)c()}else d()", parsePrint("if (a) if (b) c(); else d();"));
    // Regular if-else
    assertEquals("if(a)b();else c()", parsePrint("if (a) b(); else c();"));
  }

  @Test(timeout = 4000)
  public void testUnaryAndIncDecOperators() {
    assertEquals("!x", parsePrint("!x"));
    assertEquals("~x", parsePrint("~x"));
    assertEquals("+x", parsePrint("+x"));
    assertEquals("-x", parsePrint("-x"));
    assertEquals("typeof x", parsePrint("typeof x"));
    assertEquals("void 0", parsePrint("void 0"));

    // Pre and Post increment/decrement
    assertEquals("++x", parsePrint("++x"));
    assertEquals("x++", parsePrint("x++"));
    assertEquals("--x", parsePrint("--x"));
    assertEquals("x--", parsePrint("x--"));
  }

  @Test(timeout = 4000)
  public void testHookTernaryOperator() {
    assertEquals("a?b:c", parsePrint("a ? b : c"));
    assertEquals("a?b?c:d:e", parsePrint("a ? (b ? c : d) : e"));
  }

  @Test(timeout = 4000)
  public void testObjectLiterals() {
    // Valid latin identifier key does not need quotes
    assertEquals("({a:1})", parsePrint("({a: 1});"));
    // Reserved keyword key requires quotes
    assertEquals("({\"var\":1})", parsePrint("({'var': 1});"));
    // Non-identifier key requires quotes
    assertEquals("({\"a-b\":1})", parsePrint("({'a-b': 1});"));
    // Non-latin key requires quotes
    assertEquals("({\"\\u00e9\":1})", parsePrint("({'\\u00e9': 1});"));
    // Multiple properties
    assertEquals("({a:1,b:2})", parsePrint("({a: 1, b: 2});"));
  }

  @Test(timeout = 4000)
  public void testFunctionDeclarationsAndExpressions() {
    assertEquals("function foo(){}", parsePrint("function foo() {}"));
    assertEquals("(function(){})()", parsePrint("(function() {})();"));
    assertEquals("var f=function(a,b){return a+b}", parsePrint("var f = function(a, b) { return a + b; };"));
  }

  // =========================================================================
  // Partition B: Boundary Value Analysis (BVA) & Extremes
  // =========================================================================

  @Test(timeout = 4000)
  public void testDirectVsIndirectEval() {
    // Direct call to eval: preserved as eval(...)
    assertEquals("eval(\"x\")", parsePrint("eval('x')"));

    // Indirect eval: Node without DIRECT_EVAL property produces (0,eval)(...)
    Node callNode = new Node(Token.CALL, Node.newString(Token.NAME, "eval"), Node.newString("1"));
    assertEquals("(0,eval)(\"1\")", printNode(callNode));

    // Direct eval annotated
    Node directCallNode = new Node(Token.CALL, Node.newString(Token.NAME, "eval"), Node.newString("1"));
    directCallNode.getFirstChild().putBooleanProp(Node.DIRECT_EVAL, true);
    assertEquals("eval(\"1\")", printNode(directCallNode));
  }

  @Test(timeout = 4000)
  public void testGetPropNeedsParensOnNumber() {
    // Number followed by property access needs parentheses to avoid being parsed as decimal point
    assertEquals("(1).toString()", parsePrint("(1).toString()"));
    assertEquals("a.b", parsePrint("a.b"));
    assertEquals("a[b]", parsePrint("a[b]"));
  }

  @Test(timeout = 4000)
  public void testNewOperatorPrecedence() {
    assertEquals("new Foo", parsePrint("new Foo()"));
    assertEquals("new Foo(1)", parsePrint("new Foo(1)"));
    // new target containing call requires parens
    assertEquals("new (foo())()", parsePrint("new (foo())()"));
  }

  @Test(timeout = 4000)
  public void testArrayLiteralWithSkippedIndices() {
    assertEquals("[]", parsePrint("[]"));
    assertEquals("[1,2,3]", parsePrint("[1, 2, 3]"));
    // Holes in array literals
    assertEquals("[,1,,2]", parsePrint("[, 1, , 2]"));
  }

  @Test(timeout = 4000)
  public void testLiteralsNullThisBooleanDebugger() {
    assertEquals("null;this;true;false;debugger;", parsePrint("null; this; true; false; debugger;"));
  }

  @Test(timeout = 4000)
  public void testRegexpEscapes() {
    assertEquals("/abc/g", parsePrint("/abc/g"));
    assertEquals("/a\\/b/", parsePrint("/a\\/b/"));
    assertEquals("/<!--/", CodeGenerator.regexpEscape("<!--"));
    assertEquals("/--\\>/", CodeGenerator.regexpEscape("-->"));
    assertEquals("/<\\/script>/", CodeGenerator.regexpEscape("</script>"));
  }

  @Test(timeout = 4000)
  public void testJsStringOptimalQuoteSelection() {
    // More single quotes inside -> wrap in double quotes
    assertEquals("\"a'b'c\"", CodeGenerator.jsString("a'b'c", null));
    // More double quotes inside -> wrap in single quotes
    assertEquals("'a\"b\"c'", CodeGenerator.jsString("a\"b\"c", null));
    // Equal count -> default to double quotes
    assertEquals("\"a'b\\\"c\"", CodeGenerator.jsString("a'b\"c", null));
    // Empty string
    assertEquals("\"\"", CodeGenerator.jsString("", null));
  }

  @Test(timeout = 4000)
  public void testStringEscapingSpecialSequences() {
    // Escaping control characters
    assertEquals("\"\\n\\r\\t\\\\\"", CodeGenerator.jsString("\n\r\t\\", null));
    // Breaking HTML comment and script tags
    assertEquals("\"--\\>\"", CodeGenerator.jsString("-->", null));
    assertEquals("\"]]\\>\"", CodeGenerator.jsString("]]>", null));
    assertEquals("\"<\\/script>\"", CodeGenerator.jsString("</script>", null));
    assertEquals("\"<\\/ScRiPt>\"", CodeGenerator.jsString("</ScRiPt>", null));
    assertEquals("\"<script>\"", CodeGenerator.jsString("<script>", null));
  }

  @Test(timeout = 4000)
  public void testStringEscapingWithCharsetEncoders() {
    CharsetEncoder asciiEncoder = Charsets.US_ASCII.newEncoder();
    CharsetEncoder utf8Encoder = Charsets.UTF_8.newEncoder();

    // ASCII encoder unicode-escapes non-ASCII characters
    assertEquals("\"\\u00e9\"", CodeGenerator.jsString("\u00e9", asciiEncoder));
    // UTF-8 encoder preserves non-ASCII characters that can be encoded
    assertEquals("\"\u00e9\"", CodeGenerator.jsString("\u00e9", utf8Encoder));

    // Without encoder, non-ASCII is escaped
    assertEquals("\"\\u00e9\"", CodeGenerator.jsString("\u00e9", null));
    // Control characters < 0x20 are escaped
    assertEquals("\"\\u0001\"", CodeGenerator.jsString("\u0001", null));
  }

  @Test(timeout = 4000)
  public void testEscapeToDoubleQuotedJsString() {
    assertEquals("\"foo\\\"bar\"", CodeGenerator.escapeToDoubleQuotedJsString("foo\"bar"));
    assertEquals("\"foo'bar\"", CodeGenerator.escapeToDoubleQuotedJsString("foo'bar"));
  }

  @Test(timeout = 4000)
  public void testIdentifierEscape() {
    // Latin identifier passes through untouched
    assertEquals("validIdent_1$", CodeGenerator.identifierEscape("validIdent_1$"));
    // Non-latin identifier has hex escape
    assertEquals("\\u00e9_var", CodeGenerator.identifierEscape("\u00e9_var"));
  }

  // =========================================================================
  // Partition C: Defect-Targeted Branch Zone (Safari / IE Compatibility)
  // =========================================================================

  /**
   * Targets Defects4J bug: Safari compatibility when a function declaration
   * is wrapped in a labeled statement inside an IF statement block.
   * Defective code strips the block, producing `if(e1)A:function goo(){return true}`
   * instead of `if(e1){A:function goo(){return true}}`.
   */
  @Test(timeout = 4000)
  public void testFunctionSafariCompatibility() {
    String js = "if (e1) {A: function goo() {return true;}}";
    String expected = "if(e1){A:function goo(){return true}}";
    assertEquals(expected, parsePrint(js));
  }

  /**
   * Targets Defects4J bug: IE6/7 compatibility when a DO-WHILE loop
   * is wrapped in a labeled statement inside an IF statement block.
   * Defective code strips the block, producing `if(x)A:do foo();while(y)`
   * instead of `if(x){A:do foo();while(y)}`.
   */
  @Test(timeout = 4000)
  public void testDoLoopIECompatibility() {
    String js = "if (x) {A: do { foo(); } while (y);}";
    String expected = "if(x){A:do foo();while(y)}";
    assertEquals(expected, parsePrint(js));
  }

  // =========================================================================
  // Partition D: Exception & Defensive Guard Paths
  // =========================================================================

  @Test(expected = Error.class, timeout = 4000)
  public void testCatchConditionUnsupportedError() {