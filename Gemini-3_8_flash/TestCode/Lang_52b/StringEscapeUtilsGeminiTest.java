package org.apache.commons.lang;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Targets & Decision Branches Analyzed:
 * 1. Constructor: Public default constructor instantiated for full bytecode coverage.
 * 2. escapeJavaStyleString(Writer, String, boolean):
 *    - null writer check: throws IllegalArgumentException.
 *    - null string check: returns early (no-op).
 *    - Unicode escaping branches:
 *      * ch > 0xfff: e.g. '\u1234' -> "\\u1234"
 *      * ch > 0xff:  e.g. '\u0123' -> "\\u0123"
 *      * ch > 0x7f:  e.g. '\u0080' -> "\\u0080"
 *    - Low ASCII control characters (ch < 32):
 *      * '\b', '\n', '\t', '\f', '\r'
 *      * default (ch > 0xf): e.g. 0x10 -> "\\u0010"
 *      * default (ch <= 0xf): e.g. 0x05 -> "\\u0005"
 *    - Printable characters & delimiter escaping:
 *      * '\'': escapes single quote with '\\' if escapeSingleQuote == true (JavaScript), else preserves it (Java).
 *      * '\"': escapes double quote with '\\\"'.
 *      * '\\': escapes backslash with '\\\\'.
 *      * '/' (Defect Check): In JavaScript, forward slash escaping prevents inline </script> attacks.
 *        Targeting Defects4J bug: StringEscapeUtils.escapeJavaScript("<script>alert('aaa');</script>';")
 *        Ground truth expects escaping forward slash: "<\/script>" to prevent script tag injection.
 * 3. unescapeJava(Writer, String):
 *    - null writer check: throws IllegalArgumentException.
 *    - null string check: returns early.
 *    - Unicode reading mode (inUnicode == true):
 *      * accumulation of 4 hex digits, parseInt with radix 16.
 *      * NumberFormatException handling: wraps in NestableRuntimeException for invalid hex (e.g. "\\u00ZZ").
 *    - Escape handling (hadSlash == true):
 *      * '\\', '\'', '\"', 'r', 'f', 't', 'n', 'b', 'u'
 *      * default: verbatim character output.
 *    - Trailing slash handling (hadSlash == true at string end): writes '\\'.
 * 4. HTML & XML escaping / unescaping:
 *    - escapeHtml(Writer, String), unescapeHtml(Writer, String)
 *    - escapeXml(Writer, String), unescapeXml(Writer, String)
 *    - null Writer check throws IllegalArgumentException
 *    - null String returns null or no-op
 *    - Standard entities (&amp;, &lt;, &gt;, &quot;, &apos;)
 * 5. SQL escaping:
 *    - null input -> null
 *    - single quote doubling: "McHale's Navy" -> "McHale''s Navy"
 */
public class StringEscapeUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructor() {
        StringEscapeUtils instance = new StringEscapeUtils();
        assertNotNull("Instance should not be null", instance);
    }

    @Test(timeout = 4000)
    public void testEscapeJavaCore() {
        assertEquals("Hello World", StringEscapeUtils.escapeJava("Hello World"));
        assertEquals("Line1\\nLine2", StringEscapeUtils.escapeJava("Line1\nLine2"));
        assertEquals("Col1\\tCol2", StringEscapeUtils.escapeJava("Col1\tCol2"));
        assertEquals("Backslash: \\\\", StringEscapeUtils.escapeJava("Backslash: \\"));
        assertEquals("Quote: \\\"", StringEscapeUtils.escapeJava("Quote: \""));
        assertEquals("Single Quote: '", StringEscapeUtils.escapeJava("Single Quote: '"));
        assertEquals("Carriage\\rReturn", StringEscapeUtils.escapeJava("Carriage\rReturn"));
        assertEquals("Form\\fFeed", StringEscapeUtils.escapeJava("Form\fFeed"));
        assertEquals("Backspace\\bChar", StringEscapeUtils.escapeJava("Backspace\bChar"));
    }

    @Test(timeout = 4000)
    public void testEscapeJavaUnicodeBoundaries() {
        // ch > 0xfff
        assertEquals("\\u1234", StringEscapeUtils.escapeJava("\u1234"));
        assertEquals("\\uFFFF", StringEscapeUtils.escapeJava("\uffff"));

        // ch > 0xff (0x0100 - 0x0fff)
        assertEquals("\\u0123", StringEscapeUtils.escapeJava("\u0123"));
        assertEquals("\\u0FFF", StringEscapeUtils.escapeJava("\u0fff"));

        // ch > 0x7f (0x0080 - 0x00ff)
        assertEquals("\\u0080", StringEscapeUtils.escapeJava("\u0080"));
        assertEquals("\\u00FF", StringEscapeUtils.escapeJava("\u00ff"));

        // ch < 32 non-standard control chars
        // ch > 0xf (0x10 - 0x1f)
        assertEquals("\\u0010", StringEscapeUtils.escapeJava("\u0010"));
        assertEquals("\\u001F", StringEscapeUtils.escapeJava("\u001f"));

        // ch <= 0xf (0x00 - 0x0f) excluding \b (0x8), \t (0x9), \n (0xa), \f (0xc), \r (0xd)
        assertEquals("\\u0000", StringEscapeUtils.escapeJava("\u0000"));
        assertEquals("\\u0001", StringEscapeUtils.escapeJava("\u0001"));
        assertEquals("\\u000E", StringEscapeUtils.escapeJava("\u000e"));
    }

    @Test(timeout = 4000)
    public void testUnescapeJavaCore() {
        assertEquals("Line1\nLine2", StringEscapeUtils.unescapeJava("Line1\\nLine2"));
        assertEquals("Col1\tCol2", StringEscapeUtils.unescapeJava("Col1\\tCol2"));
        assertEquals("Backslash: \\", StringEscapeUtils.unescapeJava("Backslash: \\\\"));
        assertEquals("Quote: \"", StringEscapeUtils.unescapeJava("Quote: \\\""));
        assertEquals("Single Quote: '", StringEscapeUtils.unescapeJava("Single Quote: \\'"));
        assertEquals("Carriage\rReturn", StringEscapeUtils.unescapeJava("Carriage\\rReturn"));
        assertEquals("Form\fFeed", StringEscapeUtils.unescapeJava("Form\\fFeed"));
        assertEquals("Backspace\bChar", StringEscapeUtils.unescapeJava("Backspace\\bChar"));
        assertEquals("\u1234", StringEscapeUtils.unescapeJava("\\u1234"));
        assertEquals("\u0041", StringEscapeUtils.unescapeJava("\\u0041")); // 'A'
        assertEquals("A", StringEscapeUtils.unescapeJava("\\u0041"));
    }

    @Test(timeout = 4000)
    public void testUnescapeJavaTrailingSlashAndUnknownEscapes() {
        // Trailing single backslash
        assertEquals("Test\\", StringEscapeUtils.unescapeJava("Test\\"));
        // Unknown escaped character \z -> z
        assertEquals("testz", StringEscapeUtils.unescapeJava("test\\z"));
    }

    @Test(timeout = 4000)
    public void testJavaScriptEscaping() {
        // In JavaScript, single quotes must be escaped
        assertEquals("He didn\\'t say", StringEscapeUtils.escapeJavaScript("He didn't say"));
        assertEquals("He didn\\'t say, \\\"Stop!\\\"", StringEscapeUtils.escapeJavaScript("He didn't say, \"Stop!\""));
        assertEquals("He didn't say", StringEscapeUtils.unescapeJavaScript("He didn\\'t say"));
    }

    @Test(timeout = 4000)
    public void testEscapeAndUnescapeHtml() {
        String input = "\"bread\" & 'butter'";
        String escaped = StringEscapeUtils.escapeHtml(input);
        assertEquals("&quot;bread&quot; &amp; 'butter'", escaped);
        assertEquals(input, StringEscapeUtils.unescapeHtml(escaped));

        assertEquals("&lt;Fran&ccedil;ais&gt;", StringEscapeUtils.escapeHtml("<Fran\u00e7ais>"));
        assertEquals("<Fran\u00e7ais>", StringEscapeUtils.unescapeHtml("&lt;Fran&ccedil;ais&gt;"));
    }

    @Test(timeout = 4000)
    public void testEscapeAndUnescapeXml() {
        String input = "\"bread\" & 'butter' <tag>";
        String escaped = StringEscapeUtils.escapeXml(input);
        assertEquals("&quot;bread&quot; &amp; &apos;butter&apos; &lt;tag&gt;", escaped);
        assertEquals(input, StringEscapeUtils.unescapeXml(escaped));
    }

    @Test(timeout = 4000)
    public void testEscapeSql() {
        assertEquals("McHale''s Navy", StringEscapeUtils.escapeSql("McHale's Navy"));
        assertEquals("No quotes", StringEscapeUtils.escapeSql("No quotes"));
        assertEquals("''''", StringEscapeUtils.escapeSql("''"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullInputs() {
        assertNull(StringEscapeUtils.escapeJava(null));
        assertNull(StringEscapeUtils.escapeJavaScript(null));
        assertNull(StringEscapeUtils.unescapeJava(null));
        assertNull(StringEscapeUtils.unescapeJavaScript(null));
        assertNull(StringEscapeUtils.escapeHtml(null));
        assertNull(StringEscapeUtils.unescapeHtml(null));
        assertNull(StringEscapeUtils.escapeXml(null));
        assertNull(StringEscapeUtils.unescapeXml(null));
        assertNull(StringEscapeUtils.escapeSql(null));
    }

    @Test(timeout = 4000)
    public void testEmptyStrings() throws IOException {
        assertEquals("", StringEscapeUtils.escapeJava(""));
        assertEquals("", StringEscapeUtils.escapeJavaScript(""));
        assertEquals("", StringEscapeUtils.unescapeJava(""));
        assertEquals("", StringEscapeUtils.unescapeJavaScript(""));
        assertEquals("", StringEscapeUtils.escapeHtml(""));
        assertEquals("", StringEscapeUtils.unescapeHtml(""));
        assertEquals("", StringEscapeUtils.escapeXml(""));
        assertEquals("", StringEscapeUtils.unescapeXml(""));
        assertEquals("", StringEscapeUtils.escapeSql(""));

        StringWriter sw = new StringWriter();
        StringEscapeUtils.escapeJava(sw, "");
        assertEquals("", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.escapeJavaScript(sw, "");
        assertEquals("", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.unescapeJava(sw, "");
        assertEquals("", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.unescapeJavaScript(sw, "");
        assertEquals("", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.escapeHtml(sw, "");
        assertEquals("", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.unescapeHtml(sw, "");
        assertEquals("", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.escapeXml(sw, "");
        assertEquals("", sw.toString());

        sw = new StringWriter();
        StringEscapeUtils.unescapeXml(sw, "");
        assertEquals("", sw.toString());
    }

    @Test(timeout = 4000)
    public void testWriterWithNullStringNoOp() throws IOException {
        StringWriter sw = new StringWriter();
        StringEscapeUtils.escapeJava(sw, null);
        StringEscapeUtils.escapeJavaScript(sw, null);
        StringEscapeUtils.unescapeJava(sw, null);
        StringEscapeUtils.unescapeJavaScript(sw, null);
        StringEscapeUtils.escapeHtml(sw, null);
        StringEscapeUtils.unescapeHtml(sw, null);
        StringEscapeUtils.escapeXml(sw, null);
        StringEscapeUtils.unescapeXml(sw, null);
        assertEquals("", sw.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Fault)
    // =========================================================================

    /**
     * Targets Defects4J known fault in StringEscapeUtils.escapeJavaScript:
     * Forward slash ('/') must be escaped as '\/' in JavaScript strings to avoid
     * prematurely terminating HTML script tags (e.g., "</script>").
     */
    @Test(timeout = 4000)
    public void testEscapeJavaScriptScriptTagDefect() {
        String input = "<script>alert('aaa');</script>';";
        String expected = "<script>alert(\\'aaa\\');<\\/script>\\';";
        String actual = StringEscapeUtils.escapeJavaScript(input);
        assertEquals("Defects4J defect: escapeJavaScript must escape forward slashes", expected, actual);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEscapeJavaNullWriter() throws IOException {
        StringEscapeUtils.escapeJava((Writer) null, "test");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEscapeJavaScriptNullWriter() throws IOException {
        StringEscapeUtils.escapeJavaScript((Writer) null, "test");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUnescapeJavaNullWriter() throws IOException {
        StringEscapeUtils.unescapeJava((Writer) null, "test");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUnescapeJavaScriptNullWriter() throws IOException {
        StringEscapeUtils.unescapeJavaScript((Writer) null, "test");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEscapeHtmlNullWriter() throws IOException {
        StringEscapeUtils.escapeHtml((Writer) null, "test");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUnescapeHtmlNullWriter() throws IOException {
        StringEscapeUtils.unescapeHtml((Writer) null, "test");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEscapeXmlNullWriter() throws IOException {
        StringEscapeUtils.escapeXml((Writer) null, "test");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUnescapeXmlNullWriter() throws IOException {
        StringEscapeUtils.unescapeXml((Writer) null, "test");
    }

    @Test(timeout = 4000, expected = NestableRuntimeException.class)
    public void testUnescapeJavaMalformedUnicode() {
        StringEscapeUtils.unescapeJava("\\u00ZZ");
    }

    @Test(timeout = 4000, expected = NestableRuntimeException.class)
    public void testUnescapeJavaIncompleteUnicodeAtEof() {
        StringEscapeUtils.unescapeJava("\\u001");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testWriterDelegationConsistency() throws IOException {
        String testStr = "This is a \"test\" string with \n and \t and '";

        StringWriter sw1 = new StringWriter();
        StringEscapeUtils.escapeJava(sw1, testStr);
        assertEquals(StringEscapeUtils.escapeJava(testStr), sw1.toString());

        StringWriter sw2 = new StringWriter();
        StringEscapeUtils.escapeJavaScript(sw2, testStr);
        assertEquals(StringEscapeUtils.escapeJavaScript(testStr), sw2.toString());

        StringWriter sw3 = new StringWriter();
        StringEscapeUtils.unescapeJava(sw3, sw1.toString());
        assertEquals(StringEscapeUtils.unescapeJava(sw1.toString()), sw3.toString());

        StringWriter sw4 = new StringWriter();
        StringEscapeUtils.unescapeJavaScript(sw4, sw2.toString());
        assertEquals(StringEscapeUtils.unescapeJavaScript(sw2.toString()), sw4.toString());
    }
}