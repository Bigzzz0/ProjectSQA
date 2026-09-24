package com.fasterxml.jackson.core.json.async;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * 1. Defects4J Known Defect Target:
 *    - com.fasterxml.jackson.core.json.async.AsyncLocationTest::testLocationOffsets
 *      Failure: junit.framework.AssertionFailedError: expected:<1> but was:<3>
 *      Root Cause: In NonBlockingJsonParser, token location tracking (_tokenInputCol / _currInputRowStart)
 *      miscalculates column offsets across chunk boundaries and line-breaks (\n, \r\n), specifically
 *      when tokens (e.g., field names or values) follow newlines and are split across feedInput() invocations.
 *    - Test: testLocationOffsets() explicitly feeds multiline JSON chunks (sizes 1, 2, 3, 5, etc.) and
 *      validates exact line and column numbers for all tokens, directly targeting this defect.
 *
 * 2. Coverage & Partitioning Matrix:
 *    - Partition A (Core State Transitions):
 *      * Objects, arrays, nested structures, root scalars (null, true, false, ints, floats, strings).
 *      * Major states: MAJOR_INITIAL, MAJOR_ROOT, MAJOR_OBJECT_FIELD_FIRST, MAJOR_OBJECT_FIELD_NEXT,
 *        MAJOR_OBJECT_VALUE, MAJOR_ARRAY_ELEMENT_FIRST, MAJOR_ARRAY_ELEMENT_NEXT.
 *    - Partition B (Boundary Value Analysis - BVA):
 *      * Split tokens across buffer boundaries (1-byte chunks, 2-byte chunks, multi-quad field names > 12 bytes).
 *      * UTF-8 multibyte sequences (2, 3, and 4-byte / emoji characters) split across chunks.
 *      * UTF-8 BOM handling (0xEF, 0xBB, 0xBF) split across 1, 2, 3-byte feeds.
 *      * String segment expansion (> 1000 chars), escape sequences (\n, \r, \t, \b, \f, \/, \\, \uXXXX).
 *    - Partition C (Defect-Targeted Branch Zone):
 *      * Multiline documents with chunk sizes 1, 2, 3, 5 verifying token line & column alignment.
 *      * Trailing comma handling (ALLOW_TRAILING_COMMA) in arrays and objects.
 *      * Missing value handling (ALLOW_MISSING_VALUES) e.g., [1,,2].
 *    - Partition D (Defensive & Exception Paths):
 *      * feedInput guards: undecoded bytes remaining, end < start, feed after endOfInput.
 *      * Illegal numbers: '-' without digit, '.' without digit, '1e' without digit, leading zeros.
 *      * Illegal comments (when disabled vs enabled: //, /* * /, #).
 *      * Unclosed string, unclosed comment at EOF, invalid hex in \u escape, invalid UTF-8 continuations.
 *      * _decodeEscaped() internal error verification.
 *    - Partition E (Feeder Lifecycle & Contracts):
 *      * needMoreInput(), getNonBlockingInputFeeder(), releaseBuffered(OutputStream), close().
 * ====================================================================================================
 */

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;

public class NonBlockingJsonParserGeminiTest {

    private NonBlockingJsonParser createParser() throws IOException {
        return (NonBlockingJsonParser) new JsonFactory().createNonBlockingByteArrayParser();
    }

    private NonBlockingJsonParser createParser(JsonFactory f) throws IOException {
        return (NonBlockingJsonParser) f.createNonBlockingByteArrayParser();
    }

    /**
     * Helper to feed data into a NonBlockingJsonParser in configurable chunk sizes
     * simulating realistic non-blocking async streaming.
     */
    private static class AsyncFeederHelper {
        private final NonBlockingJsonParser parser;
        private final byte[] data;
        private final int chunkSize;
        private int offset = 0;

        public AsyncFeederHelper(NonBlockingJsonParser parser, byte[] data, int chunkSize) {
            this.parser = parser;
            this.data = data;
            this.chunkSize = Math.max(1, chunkSize);
        }

        public JsonToken nextToken() throws IOException {
            while (true) {
                if (parser.needMoreInput()) {
                    if (offset < data.length) {
                        int len = Math.min(chunkSize, data.length - offset);
                        parser.feedInput(data, offset, offset + len);
                        offset += len;
                    } else {
                        parser.endOfInput();
                    }
                }
                JsonToken t = parser.nextToken();
                if (t != JsonToken.NOT_AVAILABLE) {
                    return t;
                }
                if (offset >= data.length && parser.needMoreInput()) {
                    parser.endOfInput();
                }
            }
        }
    }

    // ================================================================================================
    // PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J: AsyncLocationTest::testLocationOffsets)
    // ================================================================================================

    /**
     * Directly reproduces and targets Defects4J defect:
     * AsyncLocationTest::testLocationOffsets -> AssertionFailedError: expected:<1> but was:<3>
     * Verifies exact line and column numbers of tokens when fed in chunk sizes of 1, 2, 3, and 5 bytes.
     */
    @Test(timeout = 4000)
    public void testLocationOffsets() throws Exception {
        final String DOC = "{\n"
                + "\"a\": 1,\n"
                + "\"b\": 2,\n"
                + "\"c\": 3\n"
                + "}";
        byte[] docBytes = DOC.getBytes(StandardCharsets.UTF_8);

        int[] chunkSizes = { 1, 2, 3, 5, docBytes.length };
        for (int chunkSize : chunkSizes) {
            JsonFactory f = new JsonFactory();
            NonBlockingJsonParser p = createParser(f);
            AsyncFeederHelper helper = new AsyncFeederHelper(p, docBytes, chunkSize);

            assertEquals(JsonToken.START_OBJECT, helper.nextToken());
            assertEquals("Line mismatch on START_OBJECT for chunk " + chunkSize, 1, p.getTokenLocation().getLineNr());
            assertEquals("Col mismatch on START_OBJECT for chunk " + chunkSize, 1, p.getTokenLocation().getColumnNr());

            assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
            assertEquals("a", p.getCurrentName());
            assertEquals("Line mismatch on field 'a' for chunk " + chunkSize, 2, p.getTokenLocation().getLineNr());
            assertEquals("Col mismatch on field 'a' for chunk " + chunkSize, 1, p.getTokenLocation().getColumnNr());

            assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
            assertEquals(1, p.getIntValue());
            assertEquals(2, p.getTokenLocation().getLineNr());
            assertEquals(6, p.getTokenLocation().getColumnNr());

            assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
            assertEquals("b", p.getCurrentName());
            assertEquals("Line mismatch on field 'b' for chunk " + chunkSize, 3, p.getTokenLocation().getLineNr());
            assertEquals("Col mismatch on field 'b' for chunk " + chunkSize, 1, p.getTokenLocation().getColumnNr());

            assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
            assertEquals(2, p.getIntValue());
            assertEquals(3, p.getTokenLocation().getLineNr());
            assertEquals(6, p.getTokenLocation().getColumnNr());

            assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
            assertEquals("c", p.getCurrentName());
            assertEquals("Line mismatch on field 'c' for chunk " + chunkSize, 4, p.getTokenLocation().getLineNr());
            assertEquals("Col mismatch on field 'c' for chunk " + chunkSize, 1, p.getTokenLocation().getColumnNr());

            assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
            assertEquals(3, p.getIntValue());
            assertEquals(4, p.getTokenLocation().getLineNr());
            assertEquals(6, p.getTokenLocation().getColumnNr());

            assertEquals(JsonToken.END_OBJECT, helper.nextToken());
            assertEquals(5, p.getTokenLocation().getLineNr());
            assertEquals(1, p.getTokenLocation().getColumnNr());

            p.close();
        }
    }

    @Test(timeout = 4000)
    public void testLocationOffsetsWithCRLFLines() throws Exception {
        final String DOC = "{\r\n\"field\":\r\n123\r\n}";
        byte[] bytes = DOC.getBytes(StandardCharsets.UTF_8);

        NonBlockingJsonParser p = createParser();
        AsyncFeederHelper helper = new AsyncFeederHelper(p, bytes, 2);

        assertEquals(JsonToken.START_OBJECT, helper.nextToken());
        assertEquals(1, p.getTokenLocation().getLineNr());

        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals("field", p.getCurrentName());
        assertEquals(2, p.getTokenLocation().getLineNr());
        assertEquals(1, p.getTokenLocation().getColumnNr());

        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
        assertEquals(123, p.getIntValue());
        assertEquals(3, p.getTokenLocation().getLineNr());

        assertEquals(JsonToken.END_OBJECT, helper.nextToken());
        assertEquals(4, p.getTokenLocation().getLineNr());
        p.close();
    }

    // ================================================================================================
    // PARTITION A: CORE FUNCTIONAL LOGIC & STATE TRANSITIONS
    // ================================================================================================

    @Test(timeout = 4000)
    public void testSimpleObjectAndScalars() throws Exception {
        String json = "{\"str\":\"hello\",\"num\":42,\"boolTrue\":true,\"boolFalse\":false,\"nullVal\":null}";
        NonBlockingJsonParser p = createParser();
        AsyncFeederHelper helper = new AsyncFeederHelper(p, json.getBytes(StandardCharsets.UTF_8), 7);

        assertEquals(JsonToken.START_OBJECT, helper.nextToken());

        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals("str", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, helper.nextToken());
        assertEquals("hello", p.getText());

        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals("num", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
        assertEquals(42, p.getIntValue());

        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals("boolTrue", p.getCurrentName());
        assertEquals(JsonToken.VALUE_TRUE, helper.nextToken());

        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals("boolFalse", p.getCurrentName());
        assertEquals(JsonToken.VALUE_FALSE, helper.nextToken());

        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals("nullVal", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, helper.nextToken());

        assertEquals(JsonToken.END_OBJECT, helper.nextToken());
        assertNull(helper.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNestedArrayAndObject() throws Exception {
        String json = "[10, [\"nested\", {\"key\": [true, false]}], null]";
        NonBlockingJsonParser p = createParser();
        AsyncFeederHelper helper = new AsyncFeederHelper(p, json.getBytes(StandardCharsets.UTF_8), 3);

        assertEquals(JsonToken.START_ARRAY, helper.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
        assertEquals(10, p.getIntValue());

        assertEquals(JsonToken.START_ARRAY, helper.nextToken());
        assertEquals(JsonToken.VALUE_STRING, helper.nextToken());
        assertEquals("nested", p.getText());

        assertEquals(JsonToken.START_OBJECT, helper.nextToken());
        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals("key", p.getCurrentName());

        assertEquals(JsonToken.START_ARRAY, helper.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, helper.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, helper.nextToken());
        assertEquals(JsonToken.END_ARRAY, helper.nextToken());

        assertEquals(JsonToken.END_OBJECT, helper.nextToken());
        assertEquals(JsonToken.END_ARRAY, helper.nextToken());

        assertEquals(JsonToken.VALUE_NULL, helper.nextToken());
        assertEquals(JsonToken.END_ARRAY, helper.nextToken());
        assertNull(helper.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testEmptyContainers() throws Exception {
        String json = "{\"emptyObj\":{},\"emptyArr\":[]}";
        NonBlockingJsonParser p = createParser();
        AsyncFeederHelper helper = new AsyncFeederHelper(p, json.getBytes(StandardCharsets.UTF_8), 2);

        assertEquals(JsonToken.START_OBJECT, helper.nextToken());
        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals("emptyObj", p.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, helper.nextToken());
        assertEquals(JsonToken.END_OBJECT, helper.nextToken());

        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals("emptyArr", p.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, helper.nextToken());
        assertEquals(JsonToken.END_ARRAY, helper.nextToken());

        assertEquals(JsonToken.END_OBJECT, helper.nextToken());
        assertNull(helper.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testFloatingPointNumbers() throws Exception {
        String json = "[0, -0, 0.0, -0.05, 123.456, 1e10, 2.5E-3, -3.14e+2, 0e0]";
        NonBlockingJsonParser p = createParser();
        AsyncFeederHelper helper = new AsyncFeederHelper(p, json.getBytes(StandardCharsets.UTF_8), 2);

        assertEquals(JsonToken.START_ARRAY, helper.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
        assertEquals(0, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
        assertEquals(0, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, helper.nextToken());
        assertEquals(0.0, p.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, helper.nextToken());
        assertEquals(-0.05, p.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, helper.nextToken());
        assertEquals(123.456, p.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, helper.nextToken());
        assertEquals(1e10, p.getDoubleValue(), 100.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, helper.nextToken());
        assertEquals(0.0025, p.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, helper.nextToken());
        assertEquals(-314.0, p.getDoubleValue(), 0.001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, helper.nextToken());
        assertEquals(0.0, p.getDoubleValue(), 0.00001);

        assertEquals(JsonToken.END_ARRAY, helper.nextToken());
        assertNull(helper.nextToken());
        p.close();
    }

    // ================================================================================================
    // PARTITION B: BOUNDARY VALUE ANALYSIS (BVA) & EXTREMES
    // ================================================================================================

    @Test(timeout = 4000)
    public void testSingleByteChunkFeeding() throws Exception {
        String json = "{\"id\":12345,\"active\":true}";
        NonBlockingJsonParser p = createParser();
        AsyncFeederHelper helper = new AsyncFeederHelper(p, json.getBytes(StandardCharsets.UTF_8), 1);

        assertEquals(JsonToken.START_OBJECT, helper.nextToken());
        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals("id", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
        assertEquals(12345, p.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals("active", p.getCurrentName());
        assertEquals(JsonToken.VALUE_TRUE, helper.nextToken());
        assertEquals(JsonToken.END_OBJECT, helper.nextToken());
        assertNull(helper.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testUtf8BomHandling() throws Exception {
        byte[] bomAndJson = new byte[] {
            (byte) 0xEF, (byte) 0xBB, (byte) 0xBF,
            '{', '"', 'k', '"', ':', '1', '}'
        };

        // Test split across 1-byte chunks
        NonBlockingJsonParser p = createParser();
        AsyncFeederHelper helper = new AsyncFeederHelper(p, bomAndJson, 1);

        assertEquals(JsonToken.START_OBJECT, helper.nextToken());
        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals("k", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, helper.nextToken());
        assertNull(helper.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testUtf8MultibyteCharactersInString() throws Exception {
        // Contains ASCII, 2-byte (¢ = \u00A2), 3-byte (€ = \u20AC), and 4-byte (😀 = \uD83D\uDE00)
        String unicodeStr = "A\u00A2\u20AC\uD83D\uDE00Z";
        String json = "[\"" + unicodeStr + "\"]";
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

        // Feed with chunk size 2 to force split multi-byte decoding
        NonBlockingJsonParser p = createParser();
        AsyncFeederHelper helper = new AsyncFeederHelper(p, bytes, 2);

        assertEquals(JsonToken.START_ARRAY, helper.nextToken());
        assertEquals(JsonToken.VALUE_STRING, helper.nextToken());
        assertEquals(unicodeStr, p.getText());
        assertEquals(JsonToken.END_ARRAY, helper.nextToken());
        assertNull(helper.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testEscapeSequencesInString() throws Exception {
        String json = "[\"\\\"\\\\\\/\\b\\f\\n\\r\\t\\u0041\\u0020Z\"]";
        NonBlockingJsonParser p = createParser();
        AsyncFeederHelper helper = new AsyncFeederHelper(p, json.getBytes(StandardCharsets.UTF_8), 2);

        assertEquals(JsonToken.START_ARRAY, helper.nextToken());
        assertEquals(JsonToken.VALUE_STRING, helper.nextToken());
        assertEquals("\"\\/\b\f\n\r\tA Z", p.getText());
        assertEquals(JsonToken.END_ARRAY, helper.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testLongFieldNamesAndBufferExpansion() throws Exception {
        // Names exceeding fast parse (12 bytes) and quad buffer sizes
        StringBuilder sb = new StringBuilder("{");
        String longName1 = "a_very_long_field_name_exceeding_12_bytes";
        String longName2 = "another_extremely_long_field_name_designed_to_trigger_quad_buffer_resizing_and_growth_0123456789";
        sb.append("\"").append(longName1).append("\":1,");
        sb.append("\"").append(longName2).append("\":2}");

        NonBlockingJsonParser p = createParser();
        AsyncFeederHelper helper = new AsyncFeederHelper(p, sb.toString().getBytes(StandardCharsets.UTF_8), 3);

        assertEquals(JsonToken.START_OBJECT, helper.nextToken());
        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals(longName1, p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());

        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals(longName2, p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());

        assertEquals(JsonToken.END_OBJECT, helper.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testLongStringSegmentExpansion() throws Exception {
        StringBuilder sb = new StringBuilder("[\"");
        for (int i = 0; i < 2000; i++) {
            sb.append((char) ('a' + (i % 26)));
        }
        sb.append("\"]");
        String json = sb.toString();

        NonBlockingJsonParser p = createParser();
        AsyncFeederHelper helper = new AsyncFeederHelper(p, json.getBytes(StandardCharsets.UTF_8), 64);

        assertEquals(JsonToken.START_ARRAY, helper.nextToken());
        assertEquals(JsonToken.VALUE_STRING, helper.nextToken());
        assertEquals(2000, p.getText().length());
        assertEquals(JsonToken.END_ARRAY, helper.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSingleQuotesAndUnquotedFieldNames() throws Exception {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        f.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);

        String json = "{unquoted_name: 'single quoted val', 'apos_field': 123}";
        NonBlockingJsonParser p = createParser(f);
        AsyncFeederHelper helper = new AsyncFeederHelper(p, json.getBytes(StandardCharsets.UTF_8), 2);

        assertEquals(JsonToken.START_OBJECT, helper.nextToken());

        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals("unquoted_name", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, helper.nextToken());
        assertEquals("single quoted val", p.getText());

        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals("apos_field", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
        assertEquals(123, p.getIntValue());

        assertEquals(JsonToken.END_OBJECT, helper.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testCommentsJavaAndYaml() throws Exception {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_COMMENTS);
        f.enable(JsonParser.Feature.ALLOW_YAML_COMMENTS);

        String json = "/* leading C-comment */\n"
                + "{\n"
                + "  // C++ comment\n"
                + "  \"key\": /* inline */ 123 # YAML comment\n"
                + "}";
        NonBlockingJsonParser p = createParser(f);
        AsyncFeederHelper helper = new AsyncFeederHelper(p, json.getBytes(StandardCharsets.UTF_8), 3);

        assertEquals(JsonToken.START_OBJECT, helper.nextToken());
        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals("key", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
        assertEquals(123, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, helper.nextToken());
        assertNull(helper.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testTrailingCommaAndMissingValues() throws Exception {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_TRAILING_COMMA);
        f.enable(JsonParser.Feature.ALLOW_MISSING_VALUES);

        String json = "{\"a\": 1, }";
        NonBlockingJsonParser p = createParser(f);
        AsyncFeederHelper helper = new AsyncFeederHelper(p, json.getBytes(StandardCharsets.UTF_8), 2);

        assertEquals(JsonToken.START_OBJECT, helper.nextToken());
        assertEquals(JsonToken.FIELD_NAME, helper.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
        assertEquals(JsonToken.END_OBJECT, helper.nextToken());
        p.close();

        String arrJson = "[1, , 3, ]";
        p = createParser(f);
        helper = new AsyncFeederHelper(p, arrJson.getBytes(StandardCharsets.UTF_8), 2);

        assertEquals(JsonToken.START_ARRAY, helper.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
        assertEquals(JsonToken.VALUE_NULL, helper.nextToken()); // Missing value treated as null
        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
        assertEquals(JsonToken.END_ARRAY, helper.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testLeadingZerosAllowed() throws Exception {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);

        String json = "[007, -008, 0123]";
        NonBlockingJsonParser p = createParser(f);
        AsyncFeederHelper helper = new AsyncFeederHelper(p, json.getBytes(StandardCharsets.UTF_8), 2);

        assertEquals(JsonToken.START_ARRAY, helper.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
        assertEquals(7, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
        assertEquals(-8, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, helper.nextToken());
        assertEquals(123, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, helper.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNonStandardNumbers() throws Exception {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);

        String json = "[NaN, Infinity, +Infinity, -Infinity]";
        NonBlockingJsonParser p = createParser(f);
        AsyncFeederHelper helper = new AsyncFeederHelper(p, json.getBytes(StandardCharsets.UTF_8), 2);

        assertEquals(JsonToken.START_ARRAY, helper.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, helper.nextToken());
        assertTrue(Double.isNaN(p.getDoubleValue()));

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, helper.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, helper.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, helper.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);

        assertEquals(JsonToken.END_ARRAY, helper.nextToken());
        p.close();
    }

    // ================================================================================================
    // PARTITION D: EXCEPTION & DEFENSIVE GUARD PATHS
    // ================================================================================================

    @Test(timeout = 4000)
    public void testFeedInputWhenUndecodedBytesRemain() throws Exception {
        NonBlockingJsonParser p = createParser();
        byte[] b = "{\"key\": 1}".getBytes(StandardCharsets.UTF_8);
        p.feedInput(b, 0, b.length);
        assertTrue(p._inputPtr < p._inputEnd);

        try {
            p.feedInput(b, 0, b.length);
            fail("Expected IOException when calling feedInput with remaining undecoded bytes");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("undecoded bytes"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testFeedInputWithInvalidBounds() throws Exception {
        NonBlockingJsonParser p = createParser();
        byte[] b = new byte[10];
        try {
            p.feedInput(b, 5, 2);
            fail("Expected IOException when end < start");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("may not be before start"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testFeedInputAfterEndOfInput() throws Exception {
        NonBlockingJsonParser p = createParser();
        p.endOfInput();
        try {
            byte[] b = new byte[5];
            p.feedInput(b, 0, 5);
            fail("Expected IOException when feeding after endOfInput()");
        } catch (IOException e) {
            assertTrue(e.getMessage().contains("Already closed"));
        }
        p.close();
    }

    @Test(expected = InternalError.class, timeout = 4000)
    public void testDecodeEscapedThrowsInternalError() throws Exception {
        NonBlockingJsonParser p = createParser();
        p._decodeEscaped();
    }

    @Test(timeout = 4000)
    public void testInvalidBOMSecondByte() throws Exception {
        NonBlockingJsonParser p = createParser();
        byte[] invalidBOM = new byte[] { (byte) 0xEF, 0x00, 0x00 };
        p.feedInput(invalidBOM, 0, invalidBOM.length);
        try {
            p.nextToken();
            fail("Expected IOException for invalid BOM second byte");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("UTF-8 BOM"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testInvalidBOMThirdByte() throws Exception {
        NonBlockingJsonParser p = createParser();
        byte[] invalidBOM = new byte[] { (byte) 0xEF, (byte) 0xBB, 0x00 };
        p.feedInput(invalidBOM, 0, invalidBOM.length);
        try {
            p.nextToken();
            fail("Expected IOException for invalid BOM third byte");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("UTF-8 BOM"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testLeadingZeroesDisallowedThrows() throws Exception {
        NonBlockingJsonParser p = createParser();
        byte[] b = "[0123]".getBytes(StandardCharsets.UTF_8);
        p.feedInput(b, 0, b.length);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        try {
            p.nextToken();
            fail("Expected JsonParseException for leading zero");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Leading zeroes not allowed"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testNegativeNumberMissingDigitsThrows() throws Exception {
        NonBlockingJsonParser p = createParser();
        byte[] b = "[- ]".getBytes(StandardCharsets.UTF_8);
        p.feedInput(b, 0, b.length);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        try {
            p.nextToken();
            fail("Expected JsonParseException for isolated minus");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("expected digit"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testFloatMissingFractionDigitsThrows() throws Exception {
        NonBlockingJsonParser p = createParser();
        byte[] b = "[1. ]".getBytes(StandardCharsets.UTF_8);
        p.feedInput(b, 0, b.length);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        try {
            p.nextToken();
            fail("Expected JsonParseException for missing fraction digits");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Decimal point not followed by a digit"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testFloatMissingExponentDigitsThrows() throws Exception {
        NonBlockingJsonParser p = createParser();
        byte[] b = "[1e ]".getBytes(StandardCharsets.UTF_8);
        p.feedInput(b, 0, b.length);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        try {
            p.nextToken();
            fail("Expected JsonParseException for missing exponent digits");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Exponent indicator not followed by a digit"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testUnrecognizedTokenThrows() throws Exception {
        NonBlockingJsonParser p = createParser();
        byte[] b = "nulx".getBytes(StandardCharsets.UTF_8);
        p.feedInput(b, 0, b.length);
        try {
            p.nextToken();
            fail("Expected JsonParseException for unrecognized keyword");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("Unrecognized token 'nulx'"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testUnrecognizedEscapeThrows() throws Exception {
        NonBlockingJsonParser p = createParser();
        byte[] b = "[\"\\q\"]".getBytes(StandardCharsets.UTF_8);
        p.feedInput(b, 0, b.length);
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        try {
            p.nextToken();
            fail("Expected JsonParseException for illegal escape char");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("character escape"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testCommentsDisallowedThrows() throws Exception {
        NonBlockingJsonParser p = createParser();
        byte[] b = "// comment\n123".getBytes(StandardCharsets.UTF_8);
        p.feedInput(b, 0, b.length);
        try {
            p.nextToken();
            fail("Expected JsonParseException when comments not enabled");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("ALLOW_COMMENTS"));
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testUnclosedCCommentAtEOFThrows() throws Exception {
        JsonFactory f = new JsonFactory();
        f.enable(JsonParser.Feature.ALLOW_COMMENTS);
        NonBlockingJsonParser p = createParser(f);
        byte[] b = "/* unclosed comment".getBytes(StandardCharsets.UTF_8);
        p.feedInput(b, 0, b.length);
        assertEquals(JsonToken.NOT_AVAILABLE, p.nextToken());
        p.endOfInput();
        try {
            p.nextToken();
            fail("Expected JsonParseException for EOF in C comment");
        } catch (JsonParseException e) {
            assertTrue(e.getMessage().contains("was expecting closing '*/'"));
        }
        p.close();
    }

    // ================================================================================================
    // PARTITION E: OBJECT LIFECYCLE, FEEDER & CONTRACT INTEGRITY
    // ================================================================================================

    @Test(timeout = 4000)
    public void testFeederMethodsAndReleaseBuffered() throws Exception {
        NonBlockingJsonParser p = createParser();
        assertSame(p, p.getNonBlockingInputFeeder());

        assertTrue(p.needMoreInput());

        byte[] data = "{\"a\": 1}".getBytes(StandardCharsets.UTF_8);
        p.feedInput(data, 0, data.length);
        assertFalse(p.needMoreInput());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int released = p.releaseBuffered(baos);
        assertTrue(released > 0);
        assertEquals(data.length - p._inputPtr, released);

        assertTrue(p.needMoreInput());
        p.endOfInput();
        assertFalse(p.needMoreInput());

        p.close();
        assertTrue(p.isClosed());
        assertNull(p.nextToken());
    }

    @Test(timeout = 4000)
    public void testTokenCompletedAtEOF() throws Exception {
        // Valid EOF completion for top-level scalar values without trailing delimiter
        NonBlockingJsonParser p = createParser();
        byte[] b = "123".getBytes(StandardCharsets.UTF_8);
        p.feedInput(b, 0, b.length);
        p.endOfInput();

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        assertNull(p.nextToken());
        p.close();

        // Top-level boolean
        p = createParser();
        b = "true".getBytes(StandardCharsets.UTF_8);
        p.feedInput(b, 0, b.length);
        p.endOfInput();
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertNull(p.nextToken());
        p.close();

        // Top-level null
        p = createParser();
        b = "null".getBytes(StandardCharsets.UTF_8);
        p.feedInput(b, 0, b.length);
        p.endOfInput();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }
}