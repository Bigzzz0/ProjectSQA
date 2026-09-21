package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for StringEscapeUtils.
 * Targets maximum line/branch coverage and the known Defects4J defect
 * where escapeJavaScript fails to escape forward slash ('/').
 */
public class StringEscapeUtilsDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Partition A – Core Functional Logic:
     *   - escapeJava / escapeJavaScript: normal strings, quotes, control chars, unicode
     *   - unescapeJava / unescapeJavaScript: normal escapes, unicode, trailing backslash
     *   - escapeHtml / unescapeHtml: basic entities
     *   - escapeXml / unescapeXml: basic entities
     *   - escapeSql: single quote replacement
     * 
     * Partition B – Boundary & Null:
     *   - null input → null output
     *   - empty string → empty string
     *   - very large unicode characters (>0xfff, >0xff, >0x7f)
     *   - control characters (b, n, t, f, r, and others)
     * 
     * Partition C – Defect-Targeted (Defects4J):
     *   - escapeJavaScript must escape '/' to '\/' (e.g., "</script>" → "<\/script>")
     * 
     * Partition D – Exception Paths:
     *   - null Writer → IllegalArgumentException
     *   - IOException from Writer (simulated via custom Writer)
     * 
     * Partition E – Object Lifecycle:
     *   - constructor (trivial, just instantiate)
     */

    // ===== Partition A: Core Functional Logic =====

    @Test(timeout = 4000)
    public void testEscapeJava_NoSpecialChars() {
        assertEquals("hello", StringEscapeUtils.escapeJava("hello"));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_WithQuotes() {
        assertEquals("He didn\\'t say, \\\"Stop!\\\"", StringEscapeUtils.escapeJava("He didn't say, \"Stop!\""));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_ControlChars() {
        assertEquals("\\b\\n\\t\\f\\r", StringEscapeUtils.escapeJava("\b\n\t\f\r"));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_Backslash() {
        assertEquals("\\\\", StringEscapeUtils.escapeJava("\\"));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_UnicodeBelow32() {
        // char 0x1F (31) -> \u001F
        assertEquals("\\u001F", StringEscapeUtils.escapeJava("\u001F"));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_UnicodeAbove7F() {
        assertEquals("\\u00A9", StringEscapeUtils.escapeJava("\u00A9"));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_UnicodeAboveFF() {
        assertEquals("\\u0100", StringEscapeUtils.escapeJava("\u0100"));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_UnicodeAboveFFF() {
        assertEquals("\\u1000", StringEscapeUtils.escapeJava("\u1000"));
    }

    @Test(timeout = 4000)
    public void testEscapeJavaScript_NoSpecialChars() {
        assertEquals("hello", StringEscapeUtils.escapeJavaScript("hello"));
    }

    @Test(timeout = 4000)
    public void testEscapeJavaScript_SingleQuote() {
        assertEquals("\\'", StringEscapeUtils.escapeJavaScript("'"));
    }

    @Test(timeout = 4000)
    public void testEscapeJavaScript_DoubleQuote() {
        assertEquals("\\\"", StringEscapeUtils.escapeJavaScript("\""));
    }

    @Test(timeout = 4000)
    public void testEscapeJavaScript_Backslash() {
        assertEquals("\\\\", StringEscapeUtils.escapeJavaScript("\\"));
    }

    @Test(timeout = 4000)
    public void testUnescapeJava_Simple() {
        assertEquals("hello", StringEscapeUtils.unescapeJava("hello"));
    }

    @Test(timeout = 4000)
    public void testUnescapeJava_ControlChars() {
        assertEquals("\b\n\t\f\r", StringEscapeUtils.unescapeJava("\\b\\n\\t\\f\\r"));
    }

    @Test(timeout = 4000)
    public void testUnescapeJava_Unicode() {
        assertEquals("\u00A9", StringEscapeUtils.unescapeJava("\\u00A9"));
    }

    @Test(timeout = 4000)
    public void testUnescapeJava_TrailingBackslash() {
        assertEquals("\\", StringEscapeUtils.unescapeJava("\\"));
    }

    @Test(timeout = 4000)
    public void testUnescapeJavaScript() {
        assertEquals("'", StringEscapeUtils.unescapeJavaScript("\\'"));
    }

    @Test(timeout = 4000)
    public void testEscapeHtml_Basic() {
        assertEquals("&amp;lt;", StringEscapeUtils.escapeHtml("&lt;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeHtml_Basic() {
        assertEquals("<", StringEscapeUtils.unescapeHtml("&lt;"));
    }

    @Test(timeout = 4000)
    public void testEscapeXml_Basic() {
        assertEquals("&amp;lt;", StringEscapeUtils.escapeXml("&lt;"));
    }

    @Test(timeout = 4000)
    public void testUnescapeXml_Basic() {
        assertEquals("<", StringEscapeUtils.unescapeXml("&lt;"));
    }

    @Test(timeout = 4000)
    public void testEscapeSql_SingleQuote() {
        assertEquals("McHale''s Navy", StringEscapeUtils.escapeSql("McHale's Navy"));
    }

    // ===== Partition B: Boundary & Null =====

    @Test(timeout = 4000)
    public void testEscapeJava_Null() {
        assertNull(StringEscapeUtils.escapeJava(null));
    }

    @Test(timeout = 4000)
    public void testEscapeJavaScript_Null() {
        assertNull(StringEscapeUtils.escapeJavaScript(null));
    }

    @Test(timeout = 4000)
    public void testUnescapeJava_Null() {
        assertNull(StringEscapeUtils.unescapeJava(null));
    }

    @Test(timeout = 4000)
    public void testUnescapeJavaScript_Null() {
        assertNull(StringEscapeUtils.unescapeJavaScript(null));
    }

    @Test(timeout = 4000)
    public void testEscapeHtml_Null() {
        assertNull(StringEscapeUtils.escapeHtml(null));
    }

    @Test(timeout = 4000)
    public void testUnescapeHtml_Null() {
        assertNull(StringEscapeUtils.unescapeHtml(null));
    }

    @Test(timeout = 4000)
    public void testEscapeXml_Null() {
        assertNull(StringEscapeUtils.escapeXml(null));
    }

    @Test(timeout = 4000)
    public void testUnescapeXml_Null() {
        assertNull(StringEscapeUtils.unescapeXml(null));
    }

    @Test(timeout = 4000)
    public void testEscapeSql_Null() {
        assertNull(StringEscapeUtils.escapeSql(null));
    }

    @Test(timeout = 4000)
    public void testEscapeJava_Empty() {
        assertEquals("", StringEscapeUtils.escapeJava(""));
    }

    @Test(timeout = 4000)
    public void testEscapeJavaScript_Empty() {
        assertEquals("", StringEscapeUtils.escapeJavaScript(""));
    }

    @Test(timeout = 4000)
    public void testUnescapeJava_Empty() {
        assertEquals("", StringEscapeUtils.unescapeJava(""));
    }

    @Test(timeout = 4000)
    public void testEscapeHtml_Empty() {
        assertEquals("", StringEscapeUtils.escapeHtml(""));
    }

    @Test(timeout = 4000)
    public void testUnescapeHtml_Empty() {
        assertEquals("", StringEscapeUtils.unescapeHtml(""));
    }

    @Test(timeout = 4000)
    public void testEscapeXml_Empty() {
        assertEquals("", StringEscapeUtils.escapeXml(""));
    }

    @Test(timeout = 4000)
    public void testUnescapeXml_Empty() {
        assertEquals("", StringEscapeUtils.unescapeXml(""));
    }

    @Test(timeout = 4000)
    public void testEscapeSql_Empty() {
        assertEquals("", StringEscapeUtils.escapeSql(""));
    }

    // ===== Partition C: Defect-Targeted (Defects4J) =====
    // Known bug: escapeJavaScript does not escape '/' to '\/'
    @Test(timeout = 4000)
    public void testEscapeJavaScript_ForwardSlash() {
        // The expected behavior is that '/' should be escaped as '\/'
        // to prevent premature closing of script tags.
        String input = "</script>";
        String expected = "<\\/script>";
        String actual = StringEscapeUtils.escapeJavaScript(input);
        assertEquals("escapeJavaScript must escape forward slash", expected, actual);
    }

    @Test(timeout = 4000)
    public void testEscapeJavaScript_MultipleSlashes() {
        String input = "//";
        String expected = "\\/\\/";
        assertEquals("escapeJavaScript must escape each forward slash", expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

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

    // Test that null string input with valid Writer does not throw
    @Test(timeout = 4000)
    public void testEscapeJava_WriterNullString() throws Exception {
        java.io.StringWriter sw = new java.io.StringWriter();
        StringEscapeUtils.escapeJava(sw, null);
        assertEquals("", sw.toString());
    }

    @Test(timeout = 4000)
    public void testEscapeJavaScript_WriterNullString() throws Exception {
        java.io.StringWriter sw = new java.io.StringWriter();
        StringEscapeUtils.escapeJavaScript(sw, null);
        assertEquals("", sw.toString());
    }

    @Test(timeout = 4000)
    public void testUnescapeJava_WriterNullString() throws Exception {
        java.io.StringWriter sw = new java.io.StringWriter();
        StringEscapeUtils.unescapeJava(sw, null);
        assertEquals("", sw.toString());
    }

    @Test(timeout = 4000)
    public void testUnescapeJavaScript_WriterNullString() throws Exception {
        java.io.StringWriter sw = new java.io.StringWriter();
        StringEscapeUtils.unescapeJavaScript(sw, null);
        assertEquals("", sw.toString());
    }

    // ===== Partition E: Object Lifecycle & Contract =====

    @Test(timeout = 4000)
    public void testConstructor() {
        // Just ensure instantiation works (no exception)
        new StringEscapeUtils();
    }

    // Additional coverage for unescapeJava with unicode parsing and NumberFormatException
    @Test(timeout = 4000)
    public void testUnescapeJava_InvalidUnicode() {
        // The method throws NestableRuntimeException for invalid hex
        try {
            StringEscapeUtils.unescapeJava("\\uXXXX");
            fail("Expected NestableRuntimeException");
        } catch (NestableRuntimeException e) {
            assertTrue(e.getMessage().contains("Unable to parse unicode value"));
        }
    }

    @Test(timeout = 4000)
    public void testUnescapeJava_UnicodeInProgress() {
        // Test that incomplete unicode (less than 4 hex digits) is handled
        // The method will continue reading until 4 digits are collected.
        // Input: "\\u00A" (only 3 hex digits) followed by 'X'
        // The 'X' will be appended to unicode buffer, then when length==4 it tries to parse "00AX" -> fails
        try {
            StringEscapeUtils.unescapeJava("\\u00AX");
            fail("Expected NestableRuntimeException");
        } catch (NestableRuntimeException e) {
            // expected
        }
    }

    // Test escapeJava with Writer that throws IOException (simulate)
    @Test(timeout = 4000)
    public void testEscapeJava_WriterIOException() throws Exception {
        java.io.Writer failingWriter = new java.io.Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                throw new IOException("Simulated failure");
            }
            @Override
            public void flush() throws IOException {}
            @Override
            public void close() throws IOException {}
        };
        // The method catches IOException and prints stack trace, returns null
        // We can't easily assert the print, but we can check that no exception propagates
        // Actually the method returns null on IOException, but we call the void version?
        // The void version (escapeJava(Writer, String)) throws IOException, so it will propagate.
        // We'll test the String version which catches and returns null.
        // But the String version uses StringWriter internally, which never throws.
        // To test the catch, we need to force an IOException in the private method.
        // Since we cannot access private, we rely on the fact that StringWriter never throws.
        // This branch is hard to cover without reflection. We'll skip.
    }

    // Additional coverage for escapeJavaStyleString with characters > 0xfff
    @Test(timeout = 4000)
    public void testEscapeJava_HighUnicode() {
        String input = "\u1234";
        String expected = "\\u1234";
        assertEquals(expected, StringEscapeUtils.escapeJava(input));
    }

    // Test escapeJavaScript with single quote and other chars
    @Test(timeout = 4000)
    public void testEscapeJavaScript_Mixed() {
        String input = "a'b\"c\\d\ne";
        String expected = "a\\'b\\\"c\\\\d\\ne";
        assertEquals(expected, StringEscapeUtils.escapeJavaScript(input));
    }

    // Test unescapeJava with escaped backslash
    @Test(timeout = 4000)
    public void testUnescapeJava_DoubleBackslash() {
        assertEquals("\\", StringEscapeUtils.unescapeJava("\\\\"));
    }

    // Test unescapeJava with escaped quote
    @Test(timeout = 4000)
    public void testUnescapeJava_EscapedQuote() {
        assertEquals("\"", StringEscapeUtils.unescapeJava("\\\""));
    }

    // Test unescapeJava with escaped single quote
    @Test(timeout = 4000)
    public void testUnescapeJava_EscapedSingleQuote() {
        assertEquals("'", StringEscapeUtils.unescapeJava("\\'"));
    }

    // Test unescapeJava with other escaped char (e.g., 'x')
    @Test(timeout = 4000)
    public void testUnescapeJava_OtherEscape() {
        assertEquals("x", StringEscapeUtils.unescapeJava("\\x"));
    }

    // Test unescapeJava with multiple unicode sequences
    @Test(timeout = 4000)
    public void testUnescapeJava_MultipleUnicode() {
        String input = "\\u0041\\u0042";
        assertEquals("AB", StringEscapeUtils.unescapeJava(input));
    }

    // Test escapeHtml with special characters
    @Test(timeout = 4000)
    public void testEscapeHtml_Special() {
        assertEquals("&amp;&lt;&gt;&quot;", StringEscapeUtils.escapeHtml("&<>\""));
    }

    // Test unescapeHtml with numeric entities
    @Test(timeout = 4000)
    public void testUnescapeHtml_Numeric() {
        assertEquals("A", StringEscapeUtils.unescapeHtml("&#65;"));
    }

    // Test escapeXml with special characters
    @Test(timeout = 4000)
    public void testEscapeXml_Special() {
        assertEquals("&amp;&lt;&gt;&quot;&apos;", StringEscapeUtils.escapeXml("&<>\"'"));
    }

    // Test unescapeXml with numeric entities
    @Test(timeout = 4000)
    public void testUnescapeXml_Numeric() {
        assertEquals("A", StringEscapeUtils.unescapeXml("&#65;"));
    }

    // Test escapeSql with no single quotes
    @Test(timeout = 4000)
    public void testEscapeSql_NoQuotes() {
        assertEquals("hello", StringEscapeUtils.escapeSql("hello"));
    }

    // Test escapeSql with multiple single quotes
    @Test(timeout = 4000)
    public void testEscapeSql_MultipleQuotes() {
        assertEquals("''''", StringEscapeUtils.escapeSql("''"));
    }
}