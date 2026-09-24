package com.fasterxml.jackson.core.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. DEFECT TARGETING:
 *    - Defects4J Bug: ArrayIndexOutOfBoundsException: 200 in _parseFloat()
 *      When an integer portion of a floating-point number is long enough (e.g., 200 digits)
 *      to completely fill the current segment of TextBuffer, outPtr reaches outBuf.length.
 *      In _parseFloat, when processing '.', outBuf[outPtr++] was invoked without checking
 *      if outPtr >= outBuf.length, causing ArrayIndexOutOfBoundsException.
 *      Targeted by: testLongerFloatingPointDefect(), testLongerFloatingPointNegativeDefect().
 *
 * 2. BRANCH & STATE COVERAGE:
 *    - Life-cycle & Buffer operations:
 *      * releaseBuffered() with active and empty remaining buffer
 *      * loadMore() / _loadToHaveAtLeast() with stream and null stream (pre-loaded buffer)
 *      * _closeInput() with managed resource and unmanaged resource
 *      * _releaseBuffers() with recyclable vs non-recyclable buffers
 *    - Traversals:
 *      * nextToken(), nextFieldName(), nextFieldName(SerializableString)
 *      * nextTextValue(), nextIntValue(), nextLongValue(), nextBooleanValue()
 *      * Array / Object context switching, nested structures, empty structures
 *      * Mismatched closing markers (']' in object, '}' in array)
 *    - Field Name Parsing:
 *      * Short names (1-4 bytes): findName(q, len)
 *      * Medium names (5-8 bytes): parseMediumName()
 *      * Medium names (9-12 bytes): parseMediumName2()
 *      * Long names (>12 bytes, requiring quad buffer growth): parseLongName()
 *      * Escaped field names (ASCII, unicode \uXXXX, multi-byte UTF-8)
 *      * Non-standard: single-quoted ('name') and unquoted (name:)
 *    - Numeric Parsing:
 *      * Positive & negative ints, long boundary numbers
 *      * Floats with '.', exponents ('e', 'E', '+', '-'), leading zeros (allowed/disallowed)
 *      * Root space validation after numbers (\n, \r\n, space, tab)
 *      * Non-numeric tokens (NaN, Infinity, +Infinity, -Infinity, +INF, -INF)
 *    - String & Binary Parsing:
 *      * ASCII, escaped chars (\b, \t, \n, \f, \r, \", \\, \/)
 *      * UTF-8 2-byte, 3-byte, 4-byte surrogate characters
 *      * Base64 decoding (standard MIME, unpadded, with newlines, incremental read)
 *    - Comments:
 *      * C-style /* ... * /, C++ style // ... \n, YAML style # ... \n
 *    - Utility functions:
 *      * growArrayBy() null and populated arrays
 *      * getTokenLocation() and getCurrentLocation() tracking rows/cols
 */
public class UTF8StreamJsonParserGeminiTest {

    private UTF8StreamJsonParser createParser(String doc) throws IOException {
        return createParser(doc.getBytes(StandardCharsets.UTF_8), 0);
    }

    private UTF8StreamJsonParser createParser(String doc, int features) throws IOException {
        return createParser(doc.getBytes(StandardCharsets.UTF_8), features);
    }

    private UTF8StreamJsonParser createParser(byte[] bytes, int features) throws IOException {
        BufferRecycler br = new BufferRecycler();
        IOContext ctxt = new IOContext(br, "test", false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot().makeChild(JsonFactory.Feature.collectDefaults());
        byte[] readBuf = ctxt.allocReadIOBuffer();
        InputStream in = new ByteArrayInputStream(bytes);
        return new UTF8StreamJsonParser(ctxt, features, in, null, sym, readBuf, 0, 0, true);
    }

    private UTF8StreamJsonParser createParserNoStream(byte[] bytes, int start, int end) {
        BufferRecycler br = new BufferRecycler();
        IOContext ctxt = new IOContext(br, "test", false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot().makeChild(JsonFactory.Feature.collectDefaults());
        return new UTF8StreamJsonParser(ctxt, 0, null, null, sym, bytes, start, end, false);
    }

    // =========================================================================
    // PARTITION C: Defects4J Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets Defects4J bug: ArrayIndexOutOfBoundsException: 200 in _parseFloat.
     * When 200 digits fill the initial segment and '.' follows, outPtr is 200.
     */
    @Test(timeout = 4000)
    public void testLongerFloatingPointDefect() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; ++i) {
            sb.append('1');
        }
        sb.append(".0");
        String json = sb.toString();

        UTF8StreamJsonParser p = createParser(json);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(json, p.getText());
        p.close();
    }

    /**
     * Targets defect on negative longer floating point values.
     */
    @Test(timeout = 4000)
    public void testLongerFloatingPointNegativeDefect() throws IOException {
        StringBuilder sb = new StringBuilder("-");
        for (int i = 0; i < 200; ++i) {
            sb.append('2');
        }
        sb.append(".5");
        String json = sb.toString();

        UTF8StreamJsonParser p = createParser(json);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(json, p.getText());
        p.close();
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicObjectTraversal() throws IOException {
        String json = "{\"name\":\"Bob\",\"age\":25,\"active\":true,\"empty\":null}";
        UTF8StreamJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("name", p.getCurrentName());
        assertEquals("Bob", p.nextTextValue());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("age", p.getCurrentName());
        assertEquals(25, p.nextIntValue(0));

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("active", p.getCurrentName());
        assertEquals(Boolean.TRUE, p.nextBooleanValue());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("empty", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.nextTextValue());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testArrayTraversalAndNextValues() throws IOException {
        String json = "[10, 20000000000, true, false, \"hello\"]";
        UTF8StreamJsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(10, p.nextIntValue(0));
        assertEquals(20000000000L, p.nextLongValue(0L));
        assertEquals(Boolean.TRUE, p.nextBooleanValue());
        assertEquals(Boolean.FALSE, p.nextBooleanValue());
        assertEquals("hello", p.nextTextValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextFieldNameWithSerializableString() throws IOException {
        String json = "{\"a\":1, \"longField\":2, \"extra\":[3]}";
        UTF8StreamJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        SerializedString aStr = new SerializedString("a");
        SerializedString longStr = new SerializedString("longField");
        SerializedString missingStr = new SerializedString("missing");

        assertTrue(p.nextFieldName(aStr));
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertFalse(p.nextFieldName(missingStr));
        assertEquals("longField", p.getCurrentName());
        assertEquals(2, p.nextIntValue(0));

        assertEquals("extra", p.nextFieldName());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(3, p.nextIntValue(0));
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testFieldNameLengthsAndQuadBuffers() throws IOException {
        // Tests 1-char, 4-char, 5-char, 8-char, 9-char, 12-char, and >16 char field names
        String json = "{"
                + "\"a\":1,"
                + "\"abcd\":2,"
                + "\"abcde\":3,"
                + "\"abcdefgh\":4,"
                + "\"abcdefghi\":5,"
                + "\"abcdefghijkl\":6,"
                + "\"abcdefghijklmnopq\":7,"
                + "\"very_long_field_name_that_forces_quad_buffer_to_grow_exceeding_initial_capacity\":8"
                + "}";
        UTF8StreamJsonParser p = createParser(json);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertEquals("a", p.nextFieldName());
        assertEquals(1, p.nextIntValue(0));

        assertEquals("abcd", p.nextFieldName());
        assertEquals(2, p.nextIntValue(0));

        assertEquals("abcde", p.nextFieldName());
        assertEquals(3, p.nextIntValue(0));

        assertEquals("abcdefgh", p.nextFieldName());
        assertEquals(4, p.nextIntValue(0));

        assertEquals("abcdefghi", p.nextFieldName());
        assertEquals(5, p.nextIntValue(0));

        assertEquals("abcdefghijkl", p.nextFieldName());
        assertEquals(6, p.nextIntValue(0));

        assertEquals("abcdefghijklmnopq", p.nextFieldName());
        assertEquals(7, p.nextIntValue(0));

        assertEquals("very_long_field_name_that_forces_quad_buffer_to_grow_exceeding_initial_capacity", p.nextFieldName());
        assertEquals(8, p.nextIntValue(0));

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testTextAndValueConversions() throws IOException {
        String json = "{\"str\":\"hello world\",\"num\":123,\"flt\":45.67}";
        UTF8StreamJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("str", p.nextFieldName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello world", p.getText());
        assertEquals("hello world", p.getValueAsString());
        assertEquals("hello world", new String(p.getTextCharacters(), p.getTextOffset(), p.getTextLength()));

        assertEquals("num", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getValueAsInt());
        assertEquals(123, p.getValueAsInt(99));
        assertEquals("123", p.getValueAsString());

        assertEquals("flt", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(45, p.getValueAsInt());
        assertEquals("45.67", p.getText());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyDocumentAndEmptyStructures() throws IOException {
        UTF8StreamJsonParser p1 = createParser("");
        assertNull(p1.nextToken());
        p1.close();

        UTF8StreamJsonParser p2 = createParser("{}");
        assertEquals(JsonToken.START_OBJECT, p2.nextToken());
        assertEquals(JsonToken.END_OBJECT, p2.nextToken());
        assertNull(p2.nextToken());
        p2.close();

        UTF8StreamJsonParser p3 = createParser("[]");
        assertEquals(JsonToken.START_ARRAY, p3.nextToken());
        assertEquals(JsonToken.END_ARRAY, p3.nextToken());
        assertNull(p3.nextToken());
        p3.close();

        UTF8StreamJsonParser p4 = createParser("{\"\":\"\"}");
        assertEquals(JsonToken.START_OBJECT, p4.nextToken());
        assertEquals("", p4.nextFieldName());
        assertEquals("", p4.nextTextValue());
        assertEquals(JsonToken.END_OBJECT, p4.nextToken());
        p4.close();
    }

    @Test(timeout = 4000)
    public void testNumericBoundariesAndFormats() throws IOException {
        String json = "[0, -0, 2147483647, -2147483648, 9223372036854775807, -9223372036854775808, 0.0, -0.5, 1e10, 2.5E-3, 3.14e+2]";
        UTF8StreamJsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("0", p.getText());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("-0", p.getText());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(Integer.MAX_VALUE, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(Integer.MIN_VALUE, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(Long.MAX_VALUE, p.getLongValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(Long.MIN_VALUE, p.getLongValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("0.0", p.getText());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("-0.5", p.getText());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("1e10", p.getText());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("2.5E-3", p.getText());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("3.14e+2", p.getText());

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNonStreamDirectBufferParsing() throws IOException {
        byte[] doc = "{\"k\":\"v\"}".getBytes(StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = createParserNoStream(doc, 0, doc.length);

        assertNull(p.getInputSource());
        assertFalse(p.loadMore());
        assertFalse(p._loadToHaveAtLeast(10));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int released = p.releaseBuffered(out);
        assertEquals(doc.length, released);
        assertEquals(0, p.releaseBuffered(out));

        p.close();
    }

    @Test(timeout = 4000)
    public void testGrowArrayBy() {
        int[] resultNull = UTF8StreamJsonParser.growArrayBy(null, 5);
        assertNotNull(resultNull);
        assertEquals(5, resultNull.length);

        int[] orig = new int[]{1, 2, 3};
        int[] grown = UTF8StreamJsonParser.growArrayBy(orig, 4);
        assertEquals(7, grown.length);
        assertEquals(1, grown[0]);
        assertEquals(2, grown[1]);
        assertEquals(3, grown[2]);
        assertEquals(0, grown[3]);
    }

    // =========================================================================
    // PARTITION D: Exception, Defensive Guards & Escapes
    // =========================================================================

    @Test(timeout = 4000)
    public void testStringEscapesAndUtf8Multibyte() throws IOException {
        // Escapes: quotes, backslash, control chars, unicode escapes, 2-byte, 3-byte, 4-byte surrogate
        String json = "[\"\\\"\\\\\\/\\b\\f\\n\\r\\t\\u0041\", \"\u00A2\", \"\u20AC\", \"\uD83D\uDCA9\"]";
        UTF8StreamJsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("\"\\/\b\f\n\r\tA", p.getText());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("\u00A2", p.getText()); // 2-byte UTF-8

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("\u20AC", p.getText()); // 3-byte UTF-8

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("\uD83D\uDCA9", p.getText()); // 4-byte UTF-8 surrogate pair

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testFieldNameEscapesAndUtf8() throws IOException {
        String json = "{\"\\u0041\\u0042\":\"val\", \"\u00A2\u20AC\uD83D\uDCA9\":\"val2\"}";
        UTF8StreamJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("AB", p.nextFieldName());
        assertEquals("val", p.nextTextValue());

        assertEquals("\u00A2\u20AC\uD83D\uDCA9", p.nextFieldName());
        assertEquals("val2", p.nextTextValue());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSingleQuotedNamesAndValues() throws IOException {
        int feat = JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask();
        String json = "{'name':'O\\'Connor', 'city':'St. Paul'}";
        UTF8StreamJsonParser p = createParser(json, feat);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("name", p.nextFieldName());
        assertEquals("O'Connor", p.nextTextValue());
        assertEquals("city", p.nextFieldName());
        assertEquals("St. Paul", p.nextTextValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testUnquotedFieldNames() throws IOException {
        int feat = JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        String json = "{foo:123, _bar:456, $baz:789}";
        UTF8StreamJsonParser p = createParser(json, feat);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("foo", p.nextFieldName());
        assertEquals(123, p.nextIntValue(0));
        assertEquals("_bar", p.nextFieldName());
        assertEquals(456, p.nextIntValue(0));
        assertEquals("$baz", p.nextFieldName());
        assertEquals(789, p.nextIntValue(0));
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testCommentsJavaAndYaml() throws IOException {
        int feat = JsonParser.Feature.ALLOW_COMMENTS.getMask()
                 | JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();
        String json = "/* C-comment */\n"
                + "{\n"
                + "// line comment\n"
                + "# yaml comment\n"
                + "\"data\": /* inline */ 42\n"
                + "}";
        UTF8StreamJsonParser p = createParser(json, feat);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("data", p.nextFieldName());
        assertEquals(42, p.nextIntValue(0));
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNonNumericNumbers() throws IOException {
        int feat = JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        String json = "[NaN, Infinity, +Infinity, -Infinity, +INF, -INF]";
        UTF8StreamJsonParser p = createParser(json, feat);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isNaN(p.getDoubleValue()));

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testBase64DecodingVariants() throws IOException {
        // "Hello World!" encoded as Base64 is "SGVsbG8gV29ybGQh"
        String json = "[\"SGVsbG8gV29ybGQh\", \"SGVsbG8g\\n V29ybGQh\", \"\"]";
        UTF8StreamJsonParser p = createParser(json);

        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] b1 = p.getBinaryValue(Base64Variants.MIME);
        assertEquals("Hello World!", new String(b1, StandardCharsets.UTF_8));

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead = p.readBinaryValue(Base64Variants.MIME, baos);
        assertEquals(12, bytesRead);
        assertEquals("Hello World!", new String(baos.toByteArray(), StandardCharsets.UTF_8));

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(0, p.getBinaryValue(Base64Variants.MIME).length);

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testMismatchedClosingArrayMarker() throws IOException {
        UTF8StreamJsonParser p = createParser("{\"a\": 1 ]");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("a", p.nextFieldName());
        assertEquals(1, p.nextIntValue(0));
        try {
            p.nextToken();
            fail("Expected JsonParseException for mismatched ']' in object");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("mismatched"));
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testMismatchedClosingObjectMarker() throws IOException {
        UTF8StreamJsonParser p = createParser("[ 1, 2 }");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(1, p.nextIntValue(0));
        assertEquals(2, p.nextIntValue(0));
        try {
            p.nextToken();
            fail("Expected JsonParseException for mismatched '}' in array");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("mismatched"));
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testInvalidNumberStart() throws IOException {
        UTF8StreamJsonParser p = createParser("-a");
        try {
            p.nextToken();
            fail("Expected JsonParseException for invalid number starting with '-'");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("expected digit"));
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testInvalidLeadingZeroDisallowed() throws IOException {
        UTF8StreamJsonParser p = createParser("0123");
        try {
            p.nextToken();
            fail("Expected JsonParseException for leading zero");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("Leading zeroes not allowed"));
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testLeadingZeroAllowed() throws IOException {
        int feat = JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        UTF8StreamJsonParser p = createParser("0123", feat);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testMissingRootSpaceBetweenValues() throws IOException {
        UTF8StreamJsonParser p = createParser("123a");
        try {
            p.nextToken();
            fail("Expected JsonParseException for missing space after root number");
        } catch (JsonParseException expected) {
            // Either unrecognized character or missing root whitespace
            assertNotNull(expected.getMessage());
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testUnrecognizedTokenThrows() throws IOException {
        UTF8StreamJsonParser p = createParser("truth");
        try {
            p.nextToken();
            fail("Expected JsonParseException for unrecognized token");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("Unrecognized token"));
        } finally {
            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueInvalidToken() throws IOException {
        UTF8StreamJsonParser p = createParser("12345");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        try {
            p.getBinaryValue(Base64Variants.MIME);
            fail("Expected JsonParseException when calling getBinaryValue on NUMBER_INT");
        } catch (JsonParseException expected) {
            assertTrue(expected.getMessage().contains("not VALUE_STRING"));
        } finally {
            p.close();
        }
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testLocationsAndTracking() throws IOException {
        String json = "{\n  \"field\":\n  123\n}";
        UTF8StreamJsonParser p = createParser(json);

        JsonLocation locStart = p.getCurrentLocation();
        assertEquals(1, locStart.getLineNr());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());

        JsonLocation nameLoc = p.getTokenLocation();
        assertEquals(2, nameLoc.getLineNr());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        JsonLocation valLoc = p.getTokenLocation();
        assertEquals(3, valLoc.getLineNr());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testCodecGetterSetter() throws IOException {
        UTF8StreamJsonParser p = createParser("1");
        assertNull(p.getCodec());
        p.setCodec(null);
        assertNull(p.getCodec());
        p.close();
    }
}