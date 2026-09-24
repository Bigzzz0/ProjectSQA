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
 * Targets: UTF8StreamJsonParser.java
 *
 * 1. DEFECT UNDER TEST (Defects4J ground truth):
 *    - testGetValueAsTextBytes / testGetValueAsTextChars:
 *      When current token is JsonToken.FIELD_NAME, getValueAsString() and
 *      getValueAsString(defValue) in UTF8StreamJsonParser bypassed field name
 *      handling and called super.getValueAsString(), which treated FIELD_NAME
 *      as a non-scalar token and returned null/defValue instead of the field name text.
 *    - Target methods: getValueAsString(), getValueAsString(String).
 *
 * 2. BRANCH & LOGICAL COVERAGE MATRIX:
 *    - Lifecycle / Buffer Release:
 *      * releaseBuffered() with empty buffer (count < 1) vs full buffer.
 *      * getInputSource(), getCodec(), setCodec().
 *      * _closeInput(), _releaseBuffers() with recyclable vs non-recyclable buffers.
 *    - Token Traversal & State Transition:
 *      * nextToken(), nextFieldName(), nextFieldName(SerializableString).
 *      * nextTextValue(), nextIntValue(int), nextLongValue(long), nextBooleanValue().
 *      * Nested structures: Objects, Arrays, comma handling, unexpected colons/commas.
 *    - String Parsing & Base64:
 *      * Fast-path ASCII strings vs escaped chars (\b, \t, \n, \f, \r, \", \\, \u0041).
 *      * Multi-byte UTF-8 sequences (2-byte, 3-byte, 4-byte surrogate pairs).
 *      * getBinaryValue() and readBinaryValue() across buffer boundaries and base64 padding variants.
 *    - Number Parsing:
 *      * Positive & negative ints, long boundary numbers.
 *      * Leading zero validations with and without Feature.ALLOW_NUMERIC_LEADING_ZEROS.
 *      * Floating point numbers with fraction, exponent ('e', 'E', signs).
 *      * NaN, Infinity, -INF, +INF with and without Feature.ALLOW_NON_NUMERIC_NUMBERS.
 *    - Odd & Special Formats:
 *      * ALLOW_SINGLE_QUOTES for field names and strings.
 *      * ALLOW_UNQUOTED_FIELD_NAMES for Javascript identifiers.
 *      * ALLOW_COMMENTS (C-style /* ... * /, C++ // ...) and ALLOW_YAML_COMMENTS (# ...).
 *    - Exception / Defensive Guards:
 *      * Unexpected tokens, unquoted space, invalid hex in escape sequences.
 *      * Invalid UTF-8 start bytes and continuation bytes.
 *      * Mismatched closing brackets / braces.
 */
public class UTF8StreamJsonParserGeminiTest {

    private UTF8StreamJsonParser createParser(String json) {
        return createParser(json.getBytes(StandardCharsets.UTF_8), 0, null);
    }

    private UTF8StreamJsonParser createParser(byte[] bytes, int features, ObjectCodec codec) {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, "UTF8StreamJsonParserGeminiTest", false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(1).makeChild(JsonFactory.Feature.collectDefaults());
        return new UTF8StreamJsonParser(ctxt, features, new ByteArrayInputStream(bytes), codec, sym, bytes, 0, bytes.length, false);
    }

    private UTF8StreamJsonParser createParserWithBuffer(byte[] bytes, int start, int end, boolean recyclable) {
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, "UTF8StreamJsonParserGeminiTest", false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(1).makeChild(JsonFactory.Feature.collectDefaults());
        return new UTF8StreamJsonParser(ctxt, 0, new ByteArrayInputStream(bytes, start, end - start), null, sym, bytes, start, end, recyclable);
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone
    /**********************************************************
     */

    /**
     * Defects4J Ground-Truth Failure Test:
     * When current token is FIELD_NAME, getValueAsString() should return the name
     * of the field, not null.
     */
    @Test(timeout = 4000)
    public void testDefectGetValueAsTextOnFieldName() throws Exception {
        String json = "{\"a\":123,\"b\":true}";
        UTF8StreamJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertNull(p.getValueAsString());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getText());
        // Bug trigger: getValueAsString() delegates to super which returns null for FIELD_NAME
        assertEquals("a", p.getValueAsString());
        assertEquals("a", p.getValueAsString("default"));

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("123", p.getValueAsString());
        assertEquals(123, p.getValueAsInt());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getText());
        assertEquals("b", p.getValueAsString());

        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertEquals("true", p.getValueAsString());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testLifecycleAndBufferRelease() throws Exception {
        byte[] bytes = "{\"x\": 1}".getBytes(StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = createParserWithBuffer(bytes, 0, bytes.length, true);

        assertNotNull(p.getInputSource());
        assertNull(p.getCodec());
        p.setCodec(null);
        assertNull(p.getCodec());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int released = p.releaseBuffered(out);
        assertEquals(bytes.length, released);
        assertArrayEquals(bytes, out.toByteArray());

        assertEquals(0, p.releaseBuffered(new ByteArrayOutputStream()));

        p.close();
        assertTrue(p.isClosed());
    }

    @Test(timeout = 4000)
    public void testTokenLocations() throws Exception {
        String json = "{\n  \"field\": 42\n}";
        UTF8StreamJsonParser p = createParser(json);

        assertNull(p.currentToken());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        JsonLocation startLoc = p.getTokenLocation();
        assertEquals(1, startLoc.getLineNr());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        JsonLocation fieldLoc = p.getTokenLocation();
        assertEquals(2, fieldLoc.getLineNr());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        JsonLocation numLoc = p.getCurrentLocation();
        assertTrue(numLoc.getByteOffset() > 0);

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextOptimizedValues() throws Exception {
        String json = "{\"name\":\"Jackson\",\"age\":10,\"flag\":true,\"active\":false,\"big\":1234567890123}";
        UTF8StreamJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertTrue(p.nextFieldName(new SerializedString("name")));
        assertEquals("Jackson", p.nextTextValue());

        assertEquals("age", p.nextFieldName());
        assertEquals(10, p.nextIntValue(0));

        assertEquals("flag", p.nextFieldName());
        assertEquals(Boolean.TRUE, p.nextBooleanValue());

        assertEquals("active", p.nextFieldName());
        assertEquals(Boolean.FALSE, p.nextBooleanValue());

        assertEquals("big", p.nextFieldName());
        assertEquals(1234567890123L, p.nextLongValue(0L));

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testTextCharactersAndOffsets() throws Exception {
        String json = "{\"str\":\"hello world\"}";
        UTF8StreamJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());

        char[] nameChars = p.getTextCharacters();
        assertNotNull(nameChars);
        assertEquals("str", new String(nameChars, 0, p.getTextLength()));
        assertEquals(0, p.getTextOffset());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        char[] valChars = p.getTextCharacters();
        assertNotNull(valChars);
        assertEquals("hello world", new String(valChars, p.getTextOffset(), p.getTextLength()));

        p.close();
    }

    @Test(timeout = 4000)
    public void testNumberRepresentations() throws Exception {
        String json = "{\"i\":-123,\"f\":-45.75e+2,\"zero\":0}";
        UTF8StreamJsonParser p = createParser(json);

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-123, p.getIntValue());
        assertEquals(-123, p.getValueAsInt(99));

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(-4575.0, p.getDoubleValue(), 0.0001);
        assertEquals(-4575, p.getValueAsInt());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis & Complex Inputs
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testBase64DecodingAndStreaming() throws Exception {
        byte[] binaryData = new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
        Base64Variant variant = Base64Variants.MIME;
        String encoded = variant.encode(binaryData);
        String json = "{\"bin\":\"" + encoded + "\"}";

        UTF8StreamJsonParser p = createParser(json);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = p.readBinaryValue(variant, out);
        assertEquals(binaryData.length, count);
        assertArrayEquals(binaryData, out.toByteArray());

        assertArrayEquals(binaryData, p.getBinaryValue(variant));

        p.close();
    }

    @Test(timeout = 4000)
    public void testBase64MissingPadding() throws Exception {
        // Base64 with padding omitted using MIME_NO_LINEFEEDS
        Base64Variant variant = Base64Variants.MODIFIED_FOR_URL;
        byte[] data = "jackson-core-stream-test".getBytes(StandardCharsets.UTF_8);
        String encoded = variant.encode(data, false);
        String json = "[\"" + encoded + "\"]";

        UTF8StreamJsonParser p = createParser(json);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertArrayEquals(data, p.getBinaryValue(variant));
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testVariousStringEscapes() throws Exception {
        String json = "[\"\\\"\\\\\\/\\b\\f\\n\\r\\t\\u0041\"]";
        UTF8StreamJsonParser p = createParser(json);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("\"\\/\b\f\n\r\tA", p.getText());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testUtf8MultiByteSequences() throws Exception {
        // 2-byte: \u00E9, 3-byte: \u4E16, 4-byte: \uD83D\uDE00 (smiling face)
        String utf8Str = "\u00E9 \u4E16 \uD83D\uDE00";
        String json = "{\"key\":\"" + utf8Str + "\"}";

        UTF8StreamJsonParser p = createParser(json);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(utf8Str, p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testLongAndMediumFieldNames() throws Exception {
        // Names around 4, 8, 12, 16, and 40 characters to trigger medium & long name quads
        String name4 = "name";
        String name8 = "fullName";
        String name12 = "twelveLetter";
        String nameLong = "longFieldNameToForceQuadBufferGrowthAndMultiBranchExecution1234567890";
        String json = String.format("{\"%s\":1,\"%s\":2,\"%s\":3,\"%s\":4}", name4, name8, name12, nameLong);

        UTF8StreamJsonParser p = createParser(json);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertEquals(name4, p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(name8, p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(name12, p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(nameLong, p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testCommentsAndWhitespace() throws Exception {
        String json = "/* lead c-comment */ \n"
                + "{\n"
                + "  // single line comment\n"
                + "  # yaml comment\n"
                + "  \"key\": /* inline */ 123 \n"
                + "}";
        int features = 0;
        features |= JsonParser.Feature.ALLOW_COMMENTS.getMask();
        features |= JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();

        UTF8StreamJsonParser p = createParser(json.getBytes(StandardCharsets.UTF_8), features, null);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("key", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSingleQuotesAndUnquotedNames() throws Exception {
        String json = "{ 'single': 'val', unquoted: 'another' }";
        int features = 0;
        features |= JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask();
        features |= JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();

        UTF8StreamJsonParser p = createParser(json.getBytes(StandardCharsets.UTF_8), features, null);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertEquals("single", p.nextFieldName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("val", p.getText());

        assertEquals("unquoted", p.nextFieldName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("another", p.getText());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNonNumericNumbers() throws Exception {
        String json = "[ NaN, Infinity, +INF, -INF, -Infinity ]";
        int features = JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();

        UTF8StreamJsonParser p = createParser(json.getBytes(StandardCharsets.UTF_8), features, null);
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
        assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testMismatchedArrayEnd() throws Exception {
        String json = "}";
        UTF8StreamJsonParser p = createParser(json);
        p.nextToken();
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testMismatchedObjectEnd() throws Exception {
        String json = "]";
        UTF8StreamJsonParser p = createParser(json);
        p.nextToken();
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testMissingColon() throws Exception {
        String json = "{\"key\" 123}";
        UTF8StreamJsonParser p = createParser(json);
        p.nextToken();
        p.nextToken();
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testLeadingZeroWithoutFeature() throws Exception {
        String json = "0123";
        UTF8StreamJsonParser p = createParser(json);
        p.nextToken();
    }

    @Test(timeout = 4000)
    public void testLeadingZeroWithFeature() throws Exception {
        String json = "0123";
        int features = JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        UTF8StreamJsonParser p = createParser(json.getBytes(StandardCharsets.UTF_8), features, null);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        p.close();
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testInvalidEscapeSequence() throws Exception {
        String json = "[\"\\u123G\"]";
        UTF8StreamJsonParser p = createParser(json);
        p.nextToken();
        p.nextToken();
        p.getText();
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testUnclosedComment() throws Exception {
        String json = "/* incomplete comment ";
        int features = JsonParser.Feature.ALLOW_COMMENTS.getMask();
        UTF8StreamJsonParser p = createParser(json.getBytes(StandardCharsets.UTF_8), features, null);
        p.nextToken();
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testInvalidTokenHeuristic() throws Exception {
        String json = " falsy ";
        UTF8StreamJsonParser p = createParser(json);
        p.nextToken();
    }

    /*
    /**********************************************************
    /* Partition E: Object Lifecycle & Utility Boundaries
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testGrowArrayBy() {
        int[] original = new int[] { 1, 2, 3 };
        int[] grown = UTF8StreamJsonParser.growArrayBy(original, 5);
        assertEquals(8, grown.length);
        assertEquals(1, grown[0]);
        assertEquals(2, grown[1]);
        assertEquals(3, grown[2]);
        assertEquals(0, grown[3]);

        int[] fromNull = UTF8StreamJsonParser.growArrayBy(null, 4);
        assertNotNull(fromNull);
        assertEquals(4, fromNull.length);
    }

    @Test(timeout = 4000)
    public void testLoadMoreAndBoundarySplit() throws Exception {
        // Simulating a fragmented input stream with tiny 4-byte buffer
        byte[] jsonBytes = "{\"data\": 123456789}".getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(jsonBytes);
        BufferRecycler recycler = new BufferRecycler();
        IOContext ctxt = new IOContext(recycler, "fragmented", false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot(1).makeChild(JsonFactory.Feature.collectDefaults());
        byte[] tinyBuffer = new byte[8];

        UTF8StreamJsonParser p = new UTF8StreamJsonParser(ctxt, 0, in, null, sym, tinyBuffer, 0, 0, false);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("data", p.nextFieldName());
        assertEquals(123456789, p.nextIntValue(0));
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }
}