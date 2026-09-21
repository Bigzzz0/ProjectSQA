package org.apache.commons.lang;

import org.apache.commons.lang.exception.NestableRuntimeException;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Defect Target (Lang-D4J Ground Truth):
 *    - Method: escapeJavaStyleString(Writer, String, boolean)
 *    - Branch: case '/' (forward slash)
 *    - Defect: Forward slash '/' is unconditionally escaped to "\\/" when escaping Java strings.
 *      In valid Java syntax, '/' does not require escaping.
 *    - Target Test: testEscapeJavaWithSlash verifies that '/' is preserved as '/' in escapeJava.
 *
 * 2. Escape / Unescape Java & JavaScript:
 *    - Unicode ranges: ch > 0xfff, 0xff < ch <= 0xfff, 0x7f < ch <= 0xff, ch < 32 (control chars: \b, \n, \t, \f, \r,
 *      and other hex representation: > 0xf, <= 0xf).
 *    - Character escapes: single quote (') differentiated by escapeSingleQuote flag, double quote ("), backslash (\\).
 *    - Unescape escapes: '\\', '\'', '\"', 'r', 'f', 't', 'n', 'b', 'u' (4 hex digits parsing), default/unrecognized char.
 *    - Trailing backslash at end of input.
 *    - Malformed unicode: invalid hex sequence throws NestableRuntimeException.
 *
 * 3. HTML & XML Handling:
 *    - escapeHtml & unescapeHtml: standard entities (&amp;, &quot;, &lt;, &gt;), high Latin-1 / Unicode entities, unrecognized entities.
 *    - escapeXml & unescapeXml: 5 standard XML entities, null handling, writer delegation.
 *
 * 4. SQL & CSV Escaping:
 *    - escapeSql: single quote doubling, null handling.
 *    - escapeCsv: string containing CSV_DELIMITER, CSV_QUOTE, CR, LF vs safe strings.
 *    - unescapeCsv: length < 2, unquoted strings, quoted strings with and without internal delimiters/quotes.
 *
 * 5. Defensive / Exceptional Paths:
 *    - Null writer arguments across all Writer-based overloads throw IllegalArgumentException.
 *    - Null string arguments across all methods gracefully return null or do nothing.
 */
public class StringEscapeUtilsGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testEscapeJavaCoreCharacters() {
        String input = "Tab:\t Backspace:\b Newline:\n FormFeed:\f CR:\r Quote:\" Backslash:\\";
        String expected = "Tab:\\t Backspace:\\b Newline:\\n FormFeed:\\f CR:\\r Quote:\\\" Backslash:\\\\";
        assertEquals(expected, StringEscapeUtils.escapeJava(input));
    }

    @Test(timeout = 4000)
    public void testEscapeJavaUnicodeBoundaries() {
        // ch > 0xfff
        assertEquals("\\u1234", StringEscapeUtils.escapeJava("\u1234"));
        // 0xff < ch <= 0xfff
        assertEquals("\\u0123", StringEscapeUtils.escapeJava("\u0123"));
        // 0x7f < ch <= 0xff
        assertEquals("\\u0080", StringEscapeUtils.escapeJava("\u0080"));
        // Control chars < 32 not covered by explicit switches (e.g., 0x05 <= 0xf, 0x1B > 0xf)
        assertEquals("\\u0005", StringEscapeUtils.escapeJava("\u0005"));
        assertEquals("\\u001B", StringEscapeUtils.escapeJava("\u001B"));
    }

    @Test(timeout = 4000)
    public void testEscapeJavaScriptQuotes() {
        // Java leaves single quote as is; JavaScript escapes single quote
        String input = "He said 'hello'";
        assertEquals("He said 'hello'", StringEscapeUtils.escapeJava(input));
        assertEquals("He said \\'hello\\'", StringEscapeUtils.escapeJavaScript(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeJavaAllEscapeSequences() {
        String input = "\\b\\t\\n\\f\\r\\\'\\\"\\\\";
        String expected = "\b\t\n\f\r\'\"\\";
        assertEquals(expected, StringEscapeUtils.unescapeJava(input));
        assertEquals(expected, StringEscapeUtils.unescapeJavaScript(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeJavaUnicode() {
        String input = "\\u0041\\u0062\\u0043"; // "AbC"
        assertEquals("AbC", StringEscapeUtils.unescapeJava(input));
    }

    @Test(timeout = 4000)
    public void testUnescapeJavaNonEscapedAndTrailingBackslash() {
        assertEquals("test\\", StringEscapeUtils.unescapeJava("test\\"));
        assertEquals("test?char", StringEscapeUtils.unescapeJava("test\\?char"));
    }

    @Test(timeout = 4000)
    public void testEscapeAndUnescapeHtml() {
        String original = "<bread> & \"butter\" \u00E9";
        String escaped = StringEscapeUtils.escapeHtml(original);
        assertEquals("&lt;bread&gt; &amp; &quot;butter&quot; &eacute;", escaped);

        String unescaped = StringEscapeUtils.unescapeHtml(escaped);
        assertEquals(original, unescaped);

        // Entity left intact if unrecognized
        assertEquals("&zzzz;x", StringEscapeUtils.unescapeHtml("&zzzz;x"));
    }

    @Test(timeout = 4000)
    public void testEscapeAndUnescapeXml() {
        String original = "<tag attr='value' and \"quote\">&";
        String escaped = StringEscapeUtils.escapeXml(original);
        assertEquals("&lt;tag attr=&apos;value&apos; and &quot;quote&quot;&gt;&amp;", escaped);

        String unescaped = StringEscapeUtils.unescapeXml(escaped);
        assertEquals(original, unescaped);
    }

    @Test(timeout = 4000)
    public void testEscapeSql() {
        assertEquals("McHale''s Navy", StringEscapeUtils.escapeSql("McHale's Navy"));
        assertEquals("No quotes", StringEscapeUtils.escapeSql("No quotes"));
        assertEquals("''''", StringEscapeUtils.escapeSql("''"));
    }

    @Test(timeout = 4000)
    public void testEscapeAndUnescapeCsv() {
        // Safe string (contains none of delimiter, quotes, cr, lf)
        assertEquals("simple", StringEscapeUtils.escapeCsv("simple"));
        assertEquals("simple", StringEscapeUtils.unescapeCsv("simple"));

        // Value containing comma
        assertEquals("\"hello,world\"", StringEscapeUtils.escapeCsv("hello,world"));
        assertEquals("hello,world", StringEscapeUtils.unescapeCsv("\"hello,world\""));

        // Value containing quote
        assertEquals("\"hello \"\"world\"\"\"", StringEscapeUtils.escapeCsv("hello \"world\""));
        assertEquals("hello \"world\"", StringEscapeUtils.unescapeCsv("\"hello \"\"world\"\"\""));

        // Value containing newlines
        assertEquals("\"line1\r\nline2\"", StringEscapeUtils.escapeCsv("line1\r\nline2"));
        assertEquals("line1\r\nline2", StringEscapeUtils.unescapeCsv("\"line1\r\nline2\""));
    }

    @Test(timeout = 4000)
    public void testUnescapeCsvCornerCases() {
        // String shorter than 2 chars
        assertEquals("", StringEscapeUtils.unescapeCsv(""));
        assertEquals("a", StringEscapeUtils.unescapeCsv("a"));
        assertEquals("\"", StringEscapeUtils.unescapeCsv("\""));

        // String not starting or not ending with quote
        assertEquals("\"test", StringEscapeUtils.unescapeCsv("\"test"));
        assertEquals("test\"", StringEscapeUtils.unescapeCsv("test\""));

        // String enclosed in quotes but without search chars in the interior
        assertEquals("\"simple\"", StringEscapeUtils.unescapeCsv("\"simple\""));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testNullStringsReturnNull() {
        assertNull(StringEscapeUtils.escapeJava(null));
        assertNull(StringEscapeUtils.unescapeJava(null));
        assertNull(StringEscapeUtils.escapeJavaScript(null));
        assertNull(StringEscapeUtils.unescapeJavaScript(null));
        assertNull(StringEscapeUtils.escapeHtml(null));
        assertNull(StringEscapeUtils.unescapeHtml(null));
        assertNull(StringEscapeUtils.escapeXml(null));
        assertNull(StringEscapeUtils.unescapeXml(null));
        assertNull(StringEscapeUtils.escapeSql(null));
        assertNull(StringEscapeUtils.escapeCsv(null));
        assertNull(StringEscapeUtils.unescapeCsv(null));
    }

    @Test(timeout = 4000)
    public void testEmptyStringsReturnEmpty() {
        assertEquals("", StringEscapeUtils.escapeJava(""));
        assertEquals("", StringEscapeUtils.unescapeJava(""));
        assertEquals("", StringEscapeUtils.escapeJavaScript(""));
        assertEquals("", StringEscapeUtils.unescapeJavaScript(""));
        assertEquals("", StringEscapeUtils.escapeHtml(""));
        assertEquals("", StringEscapeUtils.unescapeHtml(""));
        assertEquals("", StringEscapeUtils.escapeXml(""));
        assertEquals("", StringEscapeUtils.unescapeXml(""));
        assertEquals("", StringEscapeUtils.escapeSql(""));
        assertEquals("", StringEscapeUtils.escapeCsv(""));
        assertEquals("", StringEscapeUtils.unescapeCsv(""));
    }

    @Test(timeout = 4000)
    public void testWriterDelegationWithNullStringDoesNothing() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJava(writer, null);
        StringEscapeUtils.unescapeJava(writer, null);
        StringEscapeUtils.escapeJavaScript(writer, null);
        StringEscapeUtils.unescapeJavaScript(writer, null);
        StringEscapeUtils.escapeHtml(writer, null);
        StringEscapeUtils.unescapeHtml(writer, null);
        StringEscapeUtils.escapeXml(writer, null);
        StringEscapeUtils.unescapeXml(writer, null);
        StringEscapeUtils.escapeCsv(writer, null);
        StringEscapeUtils.unescapeCsv(writer, null);
        assertEquals("", writer.toString());
    }

    @Test(timeout = 4000)
    public void testWriterDelegationNormalValues() throws IOException {
        StringWriter writer = new StringWriter();
        StringEscapeUtils.escapeJava(writer, "a\nb");
        assertEquals("a\\nb", writer.toString());

        writer = new StringWriter();
        StringEscapeUtils.unescapeJava(writer, "a\\nb");
        assertEquals("a\nb", writer.toString());

        writer = new StringWriter();
        StringEscapeUtils.escapeJavaScript(writer, "it's");
        assertEquals("it\\'s", writer.toString());

        writer = new StringWriter();
        StringEscapeUtils.unescapeJavaScript(writer, "it\\'s");
        assertEquals("it's", writer.toString());

        writer = new StringWriter();
        StringEscapeUtils.escapeHtml(writer, "1 < 2");
        assertEquals("1 &lt; 2", writer.toString());

        writer = new StringWriter();
        StringEscapeUtils.unescapeHtml(writer, "1 &lt; 2");
        assertEquals("1 < 2", writer.toString());

        writer = new StringWriter();
        StringEscapeUtils.escapeXml(writer, "<a&b>");
        assertEquals("&lt;a&amp;b&gt;", writer.toString());

        writer = new StringWriter();
        StringEscapeUtils.unescapeXml(writer, "&lt;a&amp;b&gt;");
        assertEquals("<a&b>", writer.toString());

        writer = new StringWriter();
        StringEscapeUtils.escapeCsv(writer, "a,b");
        assertEquals("\"a,b\"", writer.toString());

        writer = new StringWriter();
        StringEscapeUtils.unescapeCsv(writer, "\"a,b\"");
        assertEquals("a,b", writer.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Lang Ground Truth)
    // =========================================================================

    /**
     * Defects4J Defect Target:
     * escapeJava should NOT escape the forward slash '/' character into '\/'.
     */
    @Test(timeout = 4000)
    public void testEscapeJavaWithSlash() {
        String input = "string with a slash (/) in it";
        String expected = "string with a slash (/) in it";
        String actual = StringEscapeUtils.escapeJava(input);
        assertEquals(expected, actual);
    }

    @Test(timeout = 4000)
    public void testEscapeJavaWithMultipleSlashes() {
        String input = "/root/path/to/resource";
        String expected = "/root/path/to/resource";
        String actual = StringEscapeUtils.escapeJava(input);
        assertEquals(expected, actual);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEscapeJavaNullWriterThrows() throws IOException {
        StringEscapeUtils.escapeJava((Writer) null, "text");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUnescapeJavaNullWriterThrows() throws IOException {
        StringEscapeUtils.unescapeJava((Writer) null, "text");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEscapeJavaScriptNullWriterThrows() throws IOException {
        StringEscapeUtils.escapeJavaScript((Writer) null, "text");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUnescapeJavaScriptNullWriterThrows() throws IOException {
        StringEscapeUtils.unescapeJavaScript((Writer) null, "text");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEscapeHtmlNullWriterThrows() throws IOException {
        StringEscapeUtils.escapeHtml((Writer) null, "text");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUnescapeHtmlNullWriterThrows() throws IOException {
        StringEscapeUtils.unescapeHtml((Writer) null, "text");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testEscapeXmlNullWriterThrows() throws IOException {
        StringEscapeUtils.escapeXml((Writer) null, "text");
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUnescapeXmlNullWriterThrows() throws IOException {
        StringEscapeUtils.unescapeXml((Writer) null, "text");
    }

    @Test(timeout = 4000)
    public void testUnescapeJavaMalformedUnicodeThrowsNestableRuntimeException() {
        try {
            StringEscapeUtils.unescapeJava("\\uZZZZ");
            fail("Expected NestableRuntimeException when parsing invalid unicode digits");
        } catch (NestableRuntimeException expected) {
            assertTrue(expected.getMessage().contains("Unable to parse unicode value: ZZZZ"));
            assertTrue(expected.getCause() instanceof NumberFormatException);
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorInstantiation() {
        // Class is designed to be public to support JavaBean instances
        StringEscapeUtils instance = new StringEscapeUtils();
        assertNotNull(instance);
    }
}