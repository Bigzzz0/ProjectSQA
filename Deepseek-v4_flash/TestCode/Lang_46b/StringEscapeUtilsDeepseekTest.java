package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for StringEscapeUtils targeting the known defect
 * where escapeJava incorrectly escapes forward slash.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - escapeJava / escapeJavaScript: normal strings, quotes, control chars, unicode
 *   - unescapeJava / unescapeJavaScript: normal escapes, unicode, trailing backslash
 *   - escapeCsv / unescapeCsv: simple values, values needing quoting, embedded quotes
 *   - escapeHtml / unescapeHtml: basic delegation (null, non-null)
 *   - escapeXml / unescapeXml: basic delegation
 *   - escapeSql: single quote replacement
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - null input for all methods
 *   - empty string
 *   - string with only special characters
 *   - string with maximum unicode values (0xFFFF)
 *   - string with characters at boundaries (0x7F, 0xFF, 0xFFF)
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Forward slash '/' should NOT be escaped in Java/JavaScript strings
 *   - Known defect: escapeJava("a/b") returns "a\\/b" instead of "a/b"
 *   - Test both escapeJava and escapeJavaScript (they share the same worker)
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - null Writer argument throws IllegalArgumentException
 *   - IOException handling (simulated via StringWriter is safe, but we test the null Writer path)
 *   - unescapeJava with malformed unicode (NumberFormatException -> NestableRuntimeException)
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Constructor (trivial, just coverage)
 *   - CSV round-trip: escape then unescape yields original
 *   - Java round-trip: escape then unescape yields original (for non-special chars)
 */
public class StringEscapeUtilsDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testEscapeJava_NormalString() {
        assertEquals("hello", StringEscapeUtils.escapeJava("hello"));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_WithQuotes() {
        assertEquals("\\\"quote\\\"", StringEscapeUtils.escapeJava("\"quote\""));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_WithSingleQuote_NotEscaped() {
        // Java escaping does not escape single quote by default
        assertEquals("'single'", StringEscapeUtils.escapeJava("'single'"));
    }

    @Test(timeout = 4000)
    public void testEscapeJavaScript_WithSingleQuote_Escaped() {
        assertEquals("\\'single\\'", StringEscapeUtils.escapeJavaScript("'single'"));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_WithBackslash() {
        assertEquals("\\\\\\\\", StringEscapeUtils.escapeJava("\\\\"));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_WithControlChars() {
        assertEquals("\\b\\n\\t\\f\\r", StringEscapeUtils.escapeJava("\b\n\t\f\r"));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_WithUnicodeBelow0x100() {
        // character 0x80 -> should be escaped as \\u0080
        String input = String.valueOf((char) 0x80);
        String expected = "\\u0080";
        assertEquals(expected, StringEscapeUtils.escapeJava(input));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_WithUnicodeBelow0x1000() {
        // character 0x100 -> should be escaped as \\u0100
        String input = String.valueOf((char) 0x100);
        String expected = "\\u0100";
        assertEquals(expected, StringEscapeUtils.escapeJava(input));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_WithUnicodeAbove0xFFF() {
        // character 0x1000 -> should be escaped as \\u1000
        String input = String.valueOf((char) 0x1000);
        String expected = "\\u1000";
        assertEquals(expected, StringEscapeUtils.escapeJava(input));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_WithLowControlChar() {
        // character 0x0F -> should be escaped as \\u000f
        String input = String.valueOf((char) 0x0F);
        String expected = "\\u000f";
        assertEquals(expected, StringEscapeUtils.escapeJava(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeJava_NormalString() {
        assertEquals("hello", StringEscapeUtils.unescapeJava("hello"));
    }

    @Test(timeout = 4000)
    public void testUnescapeJava_WithEscapes() {
        assertEquals("hello\nworld", StringEscapeUtils.unescapeJava("hello\\nworld"));
    }

    @Test(timeout = 4000)
    public void testUnescapeJava_WithUnicode() {
        assertEquals("A", StringEscapeUtils.unescapeJava("\\u0041"));
    }

    @Test(timeout = 4000)
    public void testUnescapeJava_TrailingBackslash() {
        // A backslash at the end should be output as-is
        assertEquals("\\", StringEscapeUtils.unescapeJava("\\"));
    }

    @Test(timeout = 4000)
    public void testUnescapeJava_WithEscapedBackslash() {
        assertEquals("\\", StringEscapeUtils.unescapeJava("\\\\"));
    }

    @Test(timeout = 4000)
    public void testUnescapeJavaScript_DelegatesToUnescapeJava() {
        assertEquals("test", StringEscapeUtils.unescapeJavaScript("test"));
    }

    // ========== Partition B: Boundary Value Analysis ==========

    @Test(timeout = 4000)
    public void testEscapeJava_NullInput() {
        assertNull(StringEscapeUtils.escapeJava(null));
    }

    @Test(timeout = 4000)
    public void testEscapeJavaScript_NullInput() {
        assertNull(StringEscapeUtils.escapeJavaScript(null));
    }

    @Test(timeout = 4000)
    public void testUnescapeJava_NullInput() {
        assertNull(StringEscapeUtils.unescapeJava(null));
    }

    @Test(timeout = 4000)
    public void testUnescapeJavaScript_NullInput() {
        assertNull(StringEscapeUtils.unescapeJavaScript(null));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_EmptyString() {
        assertEquals("", StringEscapeUtils.escapeJava(""));
    }

    @Test(timeout = 4000)
    public void testUnescapeJava_EmptyString() {
        assertEquals("", StringEscapeUtils.unescapeJava(""));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_AllSpecialChars() {
        String input = "\b\n\t\f\r\"\\'";
        String expected = "\\b\\n\\t\\f\\r\\\"\\\\'";
        assertEquals(expected, StringEscapeUtils.escapeJava(input));
    }

    @Test(timeout = 4000)
    public void testEscapeJavaScript_AllSpecialChars() {
        String input = "\b\n\t\f\r\"\\'";
        String expected = "\\b\\n\\t\\f\\r\\\"\\\\\\'";
        assertEquals(expected, StringEscapeUtils.escapeJavaScript(input));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_HighUnicode() {
        // character 0xFFFF
        String input = String.valueOf((char) 0xFFFF);
        String expected = "\\uFFFF";
        assertEquals(expected, StringEscapeUtils.escapeJava(input));
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Defect: escapeJava incorrectly escapes forward slash '/'.
     * Expected: forward slash should NOT be escaped in Java strings.
     * Buggy version returns "a\\/b" instead of "a/b".
     */
    @Test(timeout = 4000)
    public void testEscapeJava_ForwardSlash_NotEscaped() {
        assertEquals("a/b", StringEscapeUtils.escapeJava("a/b"));
    }

    /**
     * Same defect applies to escapeJavaScript.
     */
    @Test(timeout = 4000)
    public void testEscapeJavaScript_ForwardSlash_NotEscaped() {
        assertEquals("a/b", StringEscapeUtils.escapeJavaScript("a/b"));
    }

    /**
     * Additional test: multiple slashes.
     */
    @Test(timeout = 4000)
    public void testEscapeJava_MultipleForwardSlashes() {
        assertEquals("///", StringEscapeUtils.escapeJava("///"));
    }

    /**
     * Slash combined with other special characters.
     */
    @Test(timeout = 4000)
    public void testEscapeJava_SlashWithQuote() {
        assertEquals("\\\"/", StringEscapeUtils.escapeJava("\"/"));
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEscapeJava_WriterNull() throws Exception {
        StringEscapeUtils.escapeJava(null, "test");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEscapeJavaScript_WriterNull() throws Exception {
        StringEscapeUtils.escapeJavaScript(null, "test");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnescapeJava_WriterNull() throws Exception {
        StringEscapeUtils.unescapeJava(null, "test");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnescapeJavaScript_WriterNull() throws Exception {
        StringEscapeUtils.unescapeJavaScript(null, "test");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEscapeHtml_WriterNull() throws Exception {
        StringEscapeUtils.escapeHtml(null, "test");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnescapeHtml_WriterNull() throws Exception {
        StringEscapeUtils.unescapeHtml(null, "test");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEscapeXml_WriterNull() throws Exception {
        StringEscapeUtils.escapeXml(null, "test");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnescapeXml_WriterNull() throws Exception {
        StringEscapeUtils.unescapeXml(null, "test");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testEscapeCsv_WriterNull() throws Exception {
        StringEscapeUtils.escapeCsv(null, "test");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testUnescapeCsv_WriterNull() throws Exception {
        StringEscapeUtils.unescapeCsv(null, "test");
    }

    @Test(timeout = 4000)
    public void testEscapeJava_WriterNullStringNull() throws Exception {
        // Should not throw because str is null, but out is null -> exception
        try {
            StringEscapeUtils.escapeJava(null, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testUnescapeJava_MalformedUnicode() {
        // "\\uZZZZ" is not valid hex, should throw NestableRuntimeException
        try {
            StringEscapeUtils.unescapeJava("\\uZZZZ");
            fail("Expected NestableRuntimeException");
        } catch (NestableRuntimeException e) {
            assertTrue(e.getMessage().contains("Unable to parse unicode value"));
        }
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testConstructor() {
        // Just for coverage
        new StringEscapeUtils();
    }

    @Test(timeout = 4000)
    public void testEscapeCsv_SimpleValue() {
        assertEquals("simple", StringEscapeUtils.escapeCsv("simple"));
    }

    @Test(timeout = 4000)
    public void testEscapeCsv_ValueWithComma() {
        assertEquals("\"a,b\"", StringEscapeUtils.escapeCsv("a,b"));
    }

    @Test(timeout = 4000)
    public void testEscapeCsv_ValueWithQuote() {
        assertEquals("\"\"\"quoted\"\"\"", StringEscapeUtils.escapeCsv("\"quoted\""));
    }

    @Test(timeout = 4000)
    public void testEscapeCsv_ValueWithNewline() {
        assertEquals("\"line1\nline2\"", StringEscapeUtils.escapeCsv("line1\nline2"));
    }

    @Test(timeout = 4000)
    public void testUnescapeCsv_SimpleValue() {
        assertEquals("simple", StringEscapeUtils.unescapeCsv("simple"));
    }

    @Test(timeout = 4000)
    public void testUnescapeCsv_QuotedValue() {
        assertEquals("a,b", StringEscapeUtils.unescapeCsv("\"a,b\""));
    }

    @Test(timeout = 4000)
    public void testUnescapeCsv_QuotedWithEscapedQuote() {
        assertEquals("\"quoted\"", StringEscapeUtils.unescapeCsv("\"\"\"quoted\"\"\""));
    }

    @Test(timeout = 4000)
    public void testUnescapeCsv_ShortString() {
        assertEquals("a", StringEscapeUtils.unescapeCsv("a"));
        assertEquals("", StringEscapeUtils.unescapeCsv(""));
    }

    @Test(timeout = 4000)
    public void testUnescapeCsv_NullInput() {
        assertNull(StringEscapeUtils.unescapeCsv(null));
    }

    @Test(timeout = 4000)
    public void testEscapeCsv_NullInput() {
        assertNull(StringEscapeUtils.escapeCsv(null));
    }

    @Test(timeout = 4000)
    public void testEscapeSql_NullInput() {
        assertNull(StringEscapeUtils.escapeSql(null));
    }

    @Test(timeout = 4000)
    public void testEscapeSql_SingleQuote() {
        assertEquals("''", StringEscapeUtils.escapeSql("'"));
    }

    @Test(timeout = 4000)
    public void testEscapeSql_NoQuote() {
        assertEquals("hello", StringEscapeUtils.escapeSql("hello"));
    }

    // HTML/XML methods delegate to Entities; we can test basic null handling and non-null returns
    @Test(timeout = 4000)
    public void testEscapeHtml_NullInput() {
        assertNull(StringEscapeUtils.escapeHtml(null));
    }

    @Test(timeout = 4000)
    public void testUnescapeHtml_NullInput() {
        assertNull(StringEscapeUtils.unescapeHtml(null));
    }

    @Test(timeout = 4000)
    public void testEscapeXml_NullInput() {
        assertNull(StringEscapeUtils.escapeXml(null));
    }

    @Test(timeout = 4000)
    public void testUnescapeXml_NullInput() {
        assertNull(StringEscapeUtils.unescapeXml(null));
    }

    @Test(timeout = 4000)
    public void testEscapeHtml_NonNullInput() {
        // Just ensure no exception and returns something
        String result = StringEscapeUtils.escapeHtml("test");
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testUnescapeHtml_NonNullInput() {
        String result = StringEscapeUtils.unescapeHtml("test");
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testEscapeXml_NonNullInput() {
        String result = StringEscapeUtils.escapeXml("test");
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testUnescapeXml_NonNullInput() {
        String result = StringEscapeUtils.unescapeXml("test");
        assertNotNull(result);
    }

    // Round-trip tests for CSV and Java
    @Test(timeout = 4000)
    public void testCsvRoundTrip() {
        String original = "a,b\"c\nd";
        String escaped = StringEscapeUtils.escapeCsv(original);
        String unescaped = StringEscapeUtils.unescapeCsv(escaped);
        assertEquals(original, unescaped);
    }

    @Test(timeout = 4000)
    public void testJavaRoundTrip() {
        String original = "hello\nworld\t\"quote\"\\backslash";
        String escaped = StringEscapeUtils.escapeJava(original);
        String unescaped = StringEscapeUtils.unescapeJava(escaped);
        assertEquals(original, unescaped);
    }

    @Test(timeout = 4000)
    public void testJavaScriptRoundTrip() {
        String original = "hello\nworld\t\"quote\"'single'\\backslash";
        String escaped = StringEscapeUtils.escapeJavaScript(original);
        String unescaped = StringEscapeUtils.unescapeJavaScript(escaped);
        assertEquals(original, unescaped);
    }
}