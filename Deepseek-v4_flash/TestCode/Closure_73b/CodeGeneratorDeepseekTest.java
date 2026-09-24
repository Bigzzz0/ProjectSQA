package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

import com.google.javascript.jscomp.CodeConsumer;
import com.google.javascript.jscomp.CodeGenerator;
import com.google.javascript.rhino.Node;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: CodeGenerator (static escape helpers and core add(String) / tagAsStrict).
 * 
 * Decision branches covered:
 * - jsString quote selection (single vs double based on counts)
 * - strEscape special cases: \0, \n, \r, \t, backslash, quotes, '>', '<'
 * - '>' after "--" or "]]" escape
 * - '<' before "/script" or "!--" escape
 * - CharsetEncoder null vs non-null, canEncode vs cannotEncode
 * - default pass-through for ASCII 0x20..0x7e, else hex escape
 * - DEL (0x7f) must be escaped (defect regression)
 * - identifierEscape: Latin short-circuit; non-Latin per-char escape
 * - isSimpleNumber: empty / non-digit / digits
 * - getSimpleNumber: valid numbers, MAX_POSITIVE_INTEGER boundary, overflow
 * 
 * Instance methods:
 * - tagAsStrict adds "'use strict';"
 * - add(String) delegates to CodeConsumer.add
 */
public class CodeGeneratorDeepseekTest {

  /** Minimal CodeConsumer for testing instance methods. */
  private static class StringBuilderCodeConsumer extends CodeConsumer {
    final StringBuilder sb = new StringBuilder();
    @Override public void add(String str) { sb.append(str); }
    @Override public void addIdentifier(String identifier) { sb.append(identifier); }
    @Override public void addNumber(double x) { if (x == Math.floor(x) && !Double.isInfinite(x)) { sb.append((long)x); } else { sb.append(x); } }
    @Override public void addOp(String op, boolean binOp) { sb.append(op); }
    @Override public void startSourceMapping(Node n) {}
    @Override public void endSourceMapping(Node n) {}
    @Override public void beginBlock() { sb.append("{"); }
    @Override public void endBlock(boolean breakAfterBlock) { sb.append("}"); if (breakAfterBlock) sb.append("\n"); }
    @Override public void listSeparator() { sb.append(","); }
    @Override public void endStatement() { sb.append(";"); }
    @Override public void endStatement(boolean addSemicolon) { sb.append(";"); }
    @Override public boolean continueProcessing() { return true; }
    @Override public boolean shouldPreserveExtraBlocks() { return false; }
    @Override public void maybeLineBreak() { sb.append("\n"); }
    @Override public void notePreferredLineBreak() { sb.append("\n"); }
    @Override public boolean breakAfterBlockFor(Node n, boolean isStatementContext) { return false; }
    @Override public void beginCaseBody() {}
    @Override public void endCaseBody() {}
  }

  // --- jsString / strEscape tests ---

  @Test(timeout = 4000)
  public void testJsStringNormalAscii() {
    String s = "hello";
    assertEquals("\"hello\"", CodeGenerator.jsString(s, null));
  }

  @Test(timeout = 4000)
  public void testJsStringPrefersSingleQuoteWhenMoreDoubleQuotes() {
    String s = "a\"b\"c"; // 2 double quotes, 0 single
    assertEquals("'a\"b\"c'", CodeGenerator.jsString(s, null));
  }

  @Test(timeout = 4000)
  public void testJsStringPrefersDoubleQuoteWhenMoreSingleQuotes() {
    String s = "a'b'c"; // 2 single quotes, 0 double
    assertEquals("\"a'b'c\"", CodeGenerator.jsString(s, null));
  }

  @Test(timeout = 4000)
  public void testJsStringEscapesSpecialCharacters() {
    String s = "line1\nline2\t\"quoted\"\\";
    assertEquals("\"line1\\nline2\\t\\\"quoted\\\"\\\\\"", CodeGenerator.jsString(s, null));
  }

  /** Regression test for the known defect: DEL (0x7f) must be escaped. */
  @Test(timeout = 4000)
  public void testJsStringEscapesDelCharacter() {
    String input = "\u007f";
    String expected = "\"\\u007f\""; // escaped as \u007f, not literal
    assertEquals(expected, CodeGenerator.jsString(input, null));
  }

  @Test(timeout = 4000)
  public void testJsStringEscapesControlCharacters() {
    String s = "\0\b\f\n\r\t";
    assertEquals("\"\\0\\b\\f\\n\\r\\t\"", CodeGenerator.jsString(s, null));
  }

  @Test(timeout = 4000)
  public void testJsStringNoEncoderEscapesNonAscii() {
    String s = "\u00e9"; // é
    assertEquals("\"\\u00e9\"", CodeGenerator.jsString(s, null));
  }

  @Test(timeout = 4000)
  public void testJsStringWithEncoderCanEncode() {
    Charset latin1 = StandardCharsets.ISO_8859_1;
    CodeGenerator cg = new CodeGenerator(new StringBuilderCodeConsumer(), latin1);
    String s = "abc";
    assertEquals("\"abc\"", cg.jsString(s, latin1.newEncoder()));
  }

  @Test(timeout = 4000)
  public void testJsStringWithEncoderCannotEncode() {
    Charset usAscii = StandardCharsets.US_ASCII;
    String s = "\u00e9"; // not in ASCII
    assertEquals("\"\\u00e9\"", CodeGenerator.jsString(s, usAscii.newEncoder()));
  }

  @Test(timeout = 4000)
  public void testRegexpEscape() {
    String s = "a/b\\c";
    assertEquals("/a\\/b\\\\c/", CodeGenerator.regexpEscape(s, null));
  }

  @Test(timeout = 4000)
  public void testEscapeToDoubleQuotedJsString() {
    String s = "a'b\"c\\";
    assertEquals("\"a'b\\\"c\\\\\"", CodeGenerator.escapeToDoubleQuotedJsString(s));
  }

  @Test(timeout = 4000)
  public void testStrEscapeEscapesForwardSlashScript() {
    String s = "</script>";
    assertEquals("\"<\\/script>\"", CodeGenerator.strEscape(s, '"', "\\\"", "'", "\\\\", null));
  }

  @Test(timeout = 4000)
  public void testStrEscapeEscapesHtmlCommentStart() {
    String s = "<!--";
    assertEquals("\"<\\!--\"", CodeGenerator.strEscape(s, '"', "\\\"", "'", "\\\\", null));
  }

  @Test(timeout = 4000)
  public void testStrEscapeEscapesGtAfterDoubleDash() {
    String s = "a-->";
    // '>' preceded by "--" gets escaped
    assertEquals("\"a--\\>\"", CodeGenerator.strEscape(s, '"', "\\\"", "'", "\\\\", null));
  }

  @Test(timeout = 4000)
  public void testStrEscapeEscapesGtAfterDoubleBracket() {
    String s = "]]>";
    assertEquals("\"]]\\>\"", CodeGenerator.strEscape(s, '"', "\\\"", "'", "\\\\", null));
  }

  // --- identifierEscape tests ---

  @Test(timeout = 4000)
  public void testIdentifierEscapeLatinStaysSame() {
    assertEquals("abc", CodeGenerator.identifierEscape("abc"));
  }

  @Test(timeout = 4000)
  public void testIdentifierEscapeNonLatin() {
    assertEquals("\\u00e9", CodeGenerator.identifierEscape("\u00e9"));
  }

  // --- isSimpleNumber & getSimpleNumber tests ---

  @Test(timeout = 4000)
  public void testIsSimpleNumberEmpty() {
    assertFalse(CodeGenerator.isSimpleNumber(""));
  }

  @Test(timeout = 4000)
  public void testIsSimpleNumberDigits() {
    assertTrue(CodeGenerator.isSimpleNumber("123"));
  }

  @Test(timeout = 4000)
  public void testIsSimpleNumberNonDigits() {
    assertFalse(CodeGenerator.isSimpleNumber("12a"));
  }

  @Test(timeout = 4000)
  public void testGetSimpleNumberValid() {
    assertEquals(123.0, CodeGenerator.getSimpleNumber("123"), 0.0);
  }

  @Test(timeout = 4000)
  public void testGetSimpleNumberMaxSafeInteger() {
    long max = 9007199254740991L; // 2^53 - 1
    assertEquals((double)max, CodeGenerator.getSimpleNumber(Long.toString(max)), 0.0);
  }

  @Test(timeout = 4000)
  public void testGetSimpleNumberOverflow() {
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("9007199254740992")));
  }

  @Test(timeout = 4000)
  public void testGetSimpleNumberNonNumeric() {
    assertTrue(Double.isNaN(CodeGenerator.getSimpleNumber("12a")));
  }

  // --- Instance method tests using StringBuilderCodeConsumer ---

  @Test(timeout = 4000)
  public void testTagAsStrict() {
    StringBuilderCodeConsumer consumer = new StringBuilderCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.tagAsStrict();
    assertEquals("'use strict';", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testAddString() {
    StringBuilderCodeConsumer consumer = new StringBuilderCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer);
    cg.add("var x=1;");
    assertEquals("var x=1;", consumer.sb.toString());
  }

  @Test(timeout = 4000)
  public void testConstructorWithCharset() {
    // Should not throw; verify no null encoder when charset is non-ASCII
    Charset utf8 = StandardCharsets.UTF_8;
    StringBuilderCodeConsumer consumer = new StringBuilderCodeConsumer();
    CodeGenerator cg = new CodeGenerator(consumer, utf8);
    assertNotNull(cg); // just ensure construction works
  }
}