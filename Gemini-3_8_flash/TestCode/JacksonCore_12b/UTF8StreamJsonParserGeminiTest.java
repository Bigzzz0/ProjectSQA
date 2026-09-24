package com.fasterxml.jackson.core.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: UTF8StreamJsonParser
 * ----------------------------------------------------------------------------------------------------
 * Partition A: Core Functional Logic & State Transitions
 *  - Token iteration: START_OBJECT, FIELD_NAME, VALUE_STRING, VALUE_NUMBER_INT/FLOAT, START_ARRAY,
 *    END_ARRAY, END_OBJECT, VALUE_TRUE, VALUE_FALSE, VALUE_NULL.
 *  - Value accessors: getText(), getValueAsString(), getValueAsInt(), getTextCharacters(),
 *    getTextLength(), getTextOffset().
 *  - nextFieldName(), nextTextValue(), nextIntValue(), nextLongValue(), nextBooleanValue().
 *  - Life-cycle overrides: releaseBuffered(), getInputSource(), close(), loadMore(), _loadToHaveAtLeast().
 *
 * Partition B: Boundary Value Analysis & Extremes
 *  - Fast vs slow name parsing: short names (1-4 bytes), medium names (5-8, 9-12 bytes),
 *    long names (>12 bytes), boundary buffers triggering growArrayBy and slowParseName.
 *  - Binary base64 decoding: padding variants (0, 1, 2 pad chars), whitespace within base64,
 *    streaming binary read via readBinaryValue(Base64Variant, OutputStream).
 *  - Number boundaries: positive, negative, leading zeros, exponents ('e', 'E', +/-), decimal fractions.
 *
 * Partition C: Defect-Targeted Branch Zone (Location / Column Offsets in Object Contexts)
 *  - Defects4J JacksonCore location defect: Location in object fields (testOffsetWithObjectFields).
 *    Validates getTokenLocation() when _currToken == JsonToken.FIELD_NAME to ensure correct column
 *    and byte offset tracking (targeting _nameInputCol vs _tokenInputCol in UTF8StreamJsonParser).
 *
 * Partition D: Exception & Defensive Guard Paths
 *  - Unexpected characters, unclosed strings, unclosed comments (C-style and line comments).
 *  - Disallowed features: single quotes, unquoted names, non-numeric numbers (NaN/Infinity),
 *    comments when disabled.
 *  - UTF-8 validation: invalid start bytes, invalid continuation bytes (2-byte, 3-byte, 4-byte).
 *
 * Partition E: Buffer Lifecycle & Codec State
 *  - Setting/getting ObjectCodec.
 *  - Buffer release on recyclable and non-recyclable buffers.
 * ----------------------------------------------------------------------------------------------------
 */
public class UTF8StreamJsonParserGeminiTest {

    private UTF8StreamJsonParser _createParser(byte[] input, boolean bufferRecyclable) {
        BufferRecycler br = new BufferRecycler();
        IOContext ctxt = new IOContext(br, "testSource", false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(1)
                .makeChild(JsonFactory.Feature.collectDefaults());
        return new UTF8StreamJsonParser(ctxt, 0, new ByteArrayInputStream(input),
                null, sym, input, 0, input.length, bufferRecyclable);
    }

    private UTF8StreamJsonParser _createParser(String json) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        return _createParser(bytes, false);
    }

    private UTF8StreamJsonParser _createParser(String json, int features) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        BufferRecycler br = new BufferRecycler();
        IOContext ctxt = new IOContext(br, "testSource", false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(1)
                .makeChild(JsonFactory.Feature.collectDefaults());
        return new UTF8StreamJsonParser(ctxt, features, new ByteArrayInputStream(bytes),
                null, sym, bytes, 0, bytes.length, false);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicTokensAndGetters() throws IOException {
        String json = "{\"key\": \"val\", \"num\": 123, \"flag\": true, \"arr\": [false, null]}";
        try (UTF8StreamJsonParser parser = _createParser(json)) {
            assertNull(parser.getCurrentToken());
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals("{", parser.getText());

            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("key", parser.getCurrentName());
            assertEquals("key", parser.getText());
            assertEquals("key", parser.getValueAsString());

            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("val", parser.getText());
            assertEquals("val", parser.getValueAsString("default"));
            assertEquals(3, parser.getTextLength());
            assertEquals(0, parser.getTextOffset());
            assertNotNull(parser.getTextCharacters());

            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("num", parser.getCurrentName());
            assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(123, parser.getValueAsInt());
            assertEquals(123, parser.getValueAsInt(999));
            assertEquals(123, parser.getIntValue());
            assertEquals(123L, parser.getLongValue());

            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
            assertEquals(Boolean.TRUE, parser.getBooleanValue());

            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals(JsonToken.START_ARRAY, parser.nextToken());
            assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
            assertEquals(Boolean.FALSE, parser.getBooleanValue());
            assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
            assertEquals(JsonToken.END_ARRAY, parser.nextToken());

            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
            assertNull(parser.nextToken());
        }
    }

    @Test(timeout = 4000)
    public void testNextOptimizedMethods() throws IOException {
        String json = "{\"text\":\"hello\",\"num\":42,\"longNum\":9876543210,\"truth\":true,\"lie\":false}";
        try (UTF8StreamJsonParser p = _createParser(json)) {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());

            assertTrue(p.nextFieldName(new SerializedString("text")));
            assertEquals("hello", p.nextTextValue());

            assertTrue(p.nextFieldName(new SerializedString("num")));
            assertEquals(42, p.nextIntValue(0));

            assertTrue(p.nextFieldName(new SerializedString("longNum")));
            assertEquals(9876543210L, p.nextLongValue(0L));

            assertTrue(p.nextFieldName(new SerializedString("truth")));
            assertEquals(Boolean.TRUE, p.nextBooleanValue());

            assertFalse(p.nextFieldName(new SerializedString("notThere")));
            assertEquals(Boolean.FALSE, p.nextBooleanValue());

            assertNull(p.nextFieldName());
            assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());
        }
    }

    @Test(timeout = 4000)
    public void testNextFieldNameVariations() throws IOException {
        String json = "{\"a\": 1, \"b\": 2}";
        try (UTF8StreamJsonParser p = _createParser(json)) {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());

            String name1 = p.nextFieldName();
            assertEquals("a", name1);
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

            String name2 = p.nextFieldName();
            assertEquals("b", name2);
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

            assertNull(p.nextFieldName());
            assertEquals(JsonToken.END_OBJECT, p.getCurrentToken());
        }
    }

    @Test(timeout = 4000)
    public void testReleaseBufferedAndInputSource() throws IOException {
        byte[] bytes = "{\"x\": 1}  tail".getBytes(StandardCharsets.UTF_8);
        try (UTF8StreamJsonParser p = _createParser(bytes, false)) {
            assertNotNull(p.getInputSource());
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(JsonToken.END_OBJECT, p.nextToken());

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int released = p.releaseBuffered(out);
            assertTrue(released > 0);
            String releasedStr = out.toString("UTF-8");
            assertTrue(releasedStr.contains("tail"));

            assertEquals(0, p.releaseBuffered(out));
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testVariousNameLengths() throws IOException {
        // Test names of lengths 0, 1..4 (short), 5..8 (medium), 9..12 (medium2), >12 (long)
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"\": 0,");
        sb.append("\"a\": 1,");
        sb.append("\"ab\": 2,");
        sb.append("\"abc\": 3,");
        sb.append("\"abcd\": 4,");
        sb.append("\"abcde\": 5,");
        sb.append("\"abcdef\": 6,");
        sb.append("\"abcdefg\": 7,");
        sb.append("\"abcdefgh\": 8,");
        sb.append("\"abcdefghi\": 9,");
        sb.append("\"abcdefghij\": 10,");
        sb.append("\"abcdefghijk\": 11,");
        sb.append("\"abcdefghijkl\": 12,");
        sb.append("\"abcdefghijklm_long_name_across_quads\": 13");
        sb.append("}");

        try (UTF8StreamJsonParser p = _createParser(sb.toString())) {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            for (int i = 0; i <= 13; i++) {
                assertEquals(JsonToken.FIELD_NAME, p.nextToken());
                assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
                assertEquals(i, p.getIntValue());
            }
            assertEquals(JsonToken.END_OBJECT, p.nextToken());
        }
    }

    @Test(timeout = 4000)
    public void testNumberParsingBoundaries() throws IOException {
        String json = "[-0, 0, 10, -10, 0.0, -0.5, 12.34e2, -56.78E-2, 1e+3, 999999999999999999]";
        try (UTF8StreamJsonParser p = _createParser(json)) {
            assertEquals(JsonToken.START_ARRAY, p.nextToken());
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(0, p.getIntValue());

            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(0, p.getIntValue());

            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(10, p.getIntValue());

            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(-10, p.getIntValue());

            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertEquals(0.0, p.getDoubleValue(), 0.0001);

            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertEquals(-0.5, p.getDoubleValue(), 0.0001);

            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertEquals(1234.0, p.getDoubleValue(), 0.0001);

            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertEquals(-0.5678, p.getDoubleValue(), 0.0001);

            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertEquals(1000.0, p.getDoubleValue(), 0.0001);

            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(999999999999999999L, p.getLongValue());

            assertEquals(JsonToken.END_ARRAY, p.nextToken());
        }
    }

    @Test(timeout = 4000)
    public void testBase64DecodingAndStreaming() throws IOException {
        // "Hello Jackson UTF8!" in Base64 is "SGVsbG8gSmFja3NvbiBVVEY4IQ=="
        String json = "[\"SGVsbG8gSmFja3NvbiBVVEY4IQ==\", \"YQ==\", \"YWI=\", \"YWJj\"]";
        try (UTF8StreamJsonParser p = _createParser(json)) {
            assertEquals(JsonToken.START_ARRAY, p.nextToken());

            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            byte[] b1 = p.getBinaryValue(Base64Variants.MIME);
            assertEquals("Hello Jackson UTF8!", new String(b1, StandardCharsets.UTF_8));

            // Test readBinaryValue via OutputStream
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int count = p.readBinaryValue(Base64Variants.MIME, out);
            assertEquals(1, count);
            assertArrayEquals(new byte[]{'a'}, out.toByteArray());

            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertArrayEquals(new byte[]{'a', 'b'}, p.getBinaryValue(Base64Variants.MIME));

            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertArrayEquals(new byte[]{'a', 'b', 'c'}, p.getBinaryValue(Base64Variants.MIME));

            assertEquals(JsonToken.END_ARRAY, p.nextToken());
        }
    }

    @Test(timeout = 4000)
    public void testCommentsAndWhitespaceHandling() throws IOException {
        String json = "// comment line\n"
                + "/* C-style */ {\n"
                + "  # YAML comment\n"
                + "  \"name\": /* inline */ \"value\"\n"
                + "}";
        int features = JsonParser.Feature.ALLOW_COMMENTS.getMask()
                | JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();
        try (UTF8StreamJsonParser p = _createParser(json, features)) {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("name", p.getCurrentName());
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("value", p.getText());
            assertEquals(JsonToken.END_OBJECT, p.nextToken());
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Location in Object Contexts)
    // =========================================================================

    /**
     * Targets the Defects4J JacksonCore location defect where column offsets in object contexts
     * incorrectly reflect token boundaries vs field name boundaries:
     * (e.g. expected:<6> but was:<1> when querying field name column in object).
     */
    @Test(timeout = 4000)
    public void testDefectTokenLocationWithObjectFields() throws IOException {
        // Space before field name sets column offset
        String json = "{\n  \"field\": 123\n}";
        try (UTF8StreamJsonParser p = _createParser(json)) {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());

            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            JsonLocation nameLoc = p.getTokenLocation();
            assertNotNull(nameLoc);
            assertEquals("Line number for field name", 2, nameLoc.getLineNr());
            // Column 1 is '{', line 2 has 2 spaces before '"field"', so column should be 3
            assertEquals("Column number for field name", 3, nameLoc.getColumnNr());

            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            JsonLocation valLoc = p.getTokenLocation();
            assertNotNull(valLoc);
            assertEquals(2, valLoc.getLineNr());
            assertEquals(12, valLoc.getColumnNr());

            assertEquals(JsonToken.END_OBJECT, p.nextToken());
        }
    }

    @Test(timeout = 4000)
    public void testCurrentLocationAndTokenLocationSync() throws IOException {
        String json = "   \"str\"";
        try (UTF8StreamJsonParser p = _createParser(json)) {
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            JsonLocation tokenLoc = p.getTokenLocation();
            // 3 spaces -> 4th character is quote
            assertEquals(4, tokenLoc.getColumnNr());
            assertEquals(JsonToken.VALUE_STRING, p.getCurrentToken());
            assertEquals("str", p.getText());
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testMismatchedArrayClosing() throws IOException {
        try (UTF8StreamJsonParser p = _createParser("{\"a\": 1]")) {
            p.nextToken(); // {
            p.nextToken(); // "a"
            p.nextToken(); // 1
            p.nextToken(); // ] -> error
        }
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testMismatchedObjectClosing() throws IOException {
        try (UTF8StreamJsonParser p = _createParser("[1, 2}")) {
            p.nextToken(); // [
            p.nextToken(); // 1
            p.nextToken(); // 2
            p.nextToken(); // } -> error
        }
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testMissingComma() throws IOException {
        try (UTF8StreamJsonParser p = _createParser("[1 2]")) {
            p.nextToken(); // [
            p.nextToken(); // 1
            p.nextToken(); // 2 -> missing comma error
        }
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testUnquotedFieldNameDisabled() throws IOException {
        try (UTF8StreamJsonParser p = _createParser("{foo: 1}")) {
            p.nextToken();
            p.nextToken();
        }
    }

    @Test(timeout = 4000)
    public void testUnquotedFieldNameEnabled() throws IOException {
        int feat = JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        try (UTF8StreamJsonParser p = _createParser("{foo: 1}", feat)) {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("foo", p.getCurrentName());
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(1, p.getIntValue());
            assertEquals(JsonToken.END_OBJECT, p.nextToken());
        }
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testSingleQuoteStringDisabled() throws IOException {
        try (UTF8StreamJsonParser p = _createParser("{'foo': 'bar'}")) {
            p.nextToken();
            p.nextToken();
        }
    }

    @Test(timeout = 4000)
    public void testSingleQuoteStringEnabled() throws IOException {
        int feat = JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask();
        try (UTF8StreamJsonParser p = _createParser("{'foo': 'bar', 'empty': ''}", feat)) {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("foo", p.getCurrentName());
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("bar", p.getText());
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("empty", p.getCurrentName());
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("", p.getText());
            assertEquals(JsonToken.END_OBJECT, p.nextToken());
        }
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testNonNumericNumberDisabled() throws IOException {
        try (UTF8StreamJsonParser p = _createParser("[NaN]")) {
            p.nextToken(); // [
            p.nextToken(); // NaN -> should fail
        }
    }

    @Test(timeout = 4000)
    public void testNonNumericNumberEnabled() throws IOException {
        int feat = JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        try (UTF8StreamJsonParser p = _createParser("[NaN, Infinity, -Infinity]", feat)) {
            assertEquals(JsonToken.START_ARRAY, p.nextToken());
            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertTrue(Double.isNaN(p.getDoubleValue()));

            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertTrue(Double.isInfinite(p.getDoubleValue()));
            assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

            assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
            assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);

            assertEquals(JsonToken.END_ARRAY, p.nextToken());
        }
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testInvalidLeadingZeroes() throws IOException {
        try (UTF8StreamJsonParser p = _createParser("[0123]")) {
            p.nextToken();
            p.nextToken();
        }
    }

    @Test(timeout = 4000)
    public void testEscapedCharacters() throws IOException {
        String json = "[\"\\\"\", \"\\\\\", \"\\/\", \"\\b\", \"\\f\", \"\\n\", \"\\r\", \"\\t\", \"\\u0041\"]";
        try (UTF8StreamJsonParser p = _createParser(json)) {
            assertEquals(JsonToken.START_ARRAY, p.nextToken());
            assertEquals("\"", p.nextTextValue());
            assertEquals("\\", p.nextTextValue());
            assertEquals("/", p.nextTextValue());
            assertEquals("\b", p.nextTextValue());
            assertEquals("\f", p.nextTextValue());
            assertEquals("\n", p.nextTextValue());
            assertEquals("\r", p.nextTextValue());
            assertEquals("\t", p.nextTextValue());
            assertEquals("A", p.nextTextValue());
            assertEquals(JsonToken.END_ARRAY, p.nextToken());
        }
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testInvalidUtf8Continuation() throws IOException {
        // Invalid 2-byte UTF-8 sequence: 0xC3 followed by 0x28 (valid continuation is 0x80..0xBF)
        byte[] invalidUtf8 = new byte[]{'[', '"', (byte) 0xC3, 0x28, '"', ']'};
        try (UTF8StreamJsonParser p = _createParser(invalidUtf8, false)) {
            p.nextToken(); // [
            p.nextToken(); // VALUE_STRING
            p.getText();   // Triggers decoding and error
        }
    }

    @Test(timeout = 4000)
    public void testMultiByteUtf8InStringsAndNames() throws IOException {
        // 2-byte: \u00E9 (é), 3-byte: \u4E16 (世), 4-byte: \uD83D\uDE00 (😀)
        String json = "{\"é_世\": \"😀 world\"}";
        try (UTF8StreamJsonParser p = _createParser(json)) {
            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            assertEquals(JsonToken.FIELD_NAME, p.nextToken());
            assertEquals("é_世", p.getCurrentName());
            assertEquals(JsonToken.VALUE_STRING, p.nextToken());
            assertEquals("😀 world", p.getText());
            assertEquals(JsonToken.END_OBJECT, p.nextToken());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Utility Methods
    // =========================================================================

    @Test(timeout = 4000)
    public void testGrowArrayBy() {
        int[] original = new int[]{1, 2, 3};
        int[] grown = UTF8StreamJsonParser.growArrayBy(original, 5);
        assertEquals(8, grown.length);
        assertEquals(1, grown[0]);
        assertEquals(2, grown[1]);
        assertEquals(3, grown[2]);

        int[] fromNull = UTF8StreamJsonParser.growArrayBy(null, 4);
        assertNotNull(fromNull);
        assertEquals(4, fromNull.length);
    }

    @Test(timeout = 4000)
    public void testCodecHandlingAndBufferRecycling() throws IOException {
        byte[] input = "{\"x\": 10}".getBytes(StandardCharsets.UTF_8);
        try (UTF8StreamJsonParser p = _createParser(input, true)) {
            assertNull(p.getCodec());
            p.setCodec(null);
            assertNull(p.getCodec());

            assertEquals(JsonToken.START_OBJECT, p.nextToken());
            p.close();
            assertTrue(p.isClosed());
            // Calling close again should be harmless
            p.close();
        }
    }
}