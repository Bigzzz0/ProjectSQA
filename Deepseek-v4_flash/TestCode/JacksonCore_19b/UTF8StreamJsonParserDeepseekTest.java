package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;

import static org.junit.Assert.*;

/**
 * Comprehensive test suite for UTF8StreamJsonParser.
 * Targets line/branch coverage and the known Defects4J defect:
 * ArrayIndexOutOfBoundsException when parsing longer floating point numbers.
 */
public class UTF8StreamJsonParserDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     *
     * Partitions:
     * A: Core functional logic & state transitions (nextToken, getText, getIntValue, etc.)
     * B: Boundary value analysis (empty input, zero, negative, MAX values, long strings)
     * C: Defect-targeted branch: long floating point numbers (triggers ArrayIndexOutOfBounds)
     * D: Exception & defensive guard paths (invalid JSON, unquoted names, single quotes, comments)
     * E: Object lifecycle & contract integrity (getCodec, setCodec, releaseBuffered, close)
     *
     * Key branches:
     * - _parsePosNumber: leading zero handling, digit loops, float detection
     * - _parseFloat: fraction and exponent parsing, buffer overflow
     * - _parseName: short/medium/long names, escaped names, unquoted names
     * - _finishString: ASCII fast path, multi-byte UTF-8, escape sequences
     * - _skipWS, _skipColon, _skipComment: whitespace and comment handling
     * - nextFieldName, nextTextValue, nextIntValue: optimized traversal
     * - _decodeBase64: padding and non-padding variants
     */

    // Helper to create a parser from a JSON string
    private JsonParser createParser(String json) throws IOException {
        JsonFactory factory = new JsonFactory();
        return factory.createParser(new ByteArrayInputStream(json.getBytes("UTF-8")));
    }

    // Helper to create a parser with specific features
    private JsonParser createParser(String json, JsonParser.Feature... features) throws IOException {
        JsonFactory factory = new JsonFactory();
        for (JsonParser.Feature f : features) {
            factory.enable(f);
        }
        return factory.createParser(new ByteArrayInputStream(json.getBytes("UTF-8")));
    }

    // ============================================================
    // Partition A: Core Functional Logic & State Transitions
    // ============================================================

    @Test(timeout = 4000)
    public void testSimpleObjectParsing() throws IOException {
        String json = "{\"a\":1,\"b\":\"hello\",\"c\":true,\"d\":null}";
        JsonParser p = createParser(json);
        assertNull(p.getCurrentToken());
        assertNull(p.getCurrentName());

        // Start object
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.START_OBJECT, p.getCurrentToken());

        // Field "a"
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(1L, p.getLongValue());
        assertEquals(1.0, p.getDoubleValue(), 0.0);

        // Field "b"
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getCurrentName());
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());

        // Field "c"
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("c", p.getCurrentName());
        assertSame(JsonToken.VALUE_TRUE, p.nextToken());
        assertTrue(p.getBooleanValue());

        // Field "d"
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("d", p.getCurrentName());
        assertSame(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.getText());

        // End object
        assertSame(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testArrayParsing() throws IOException {
        String json = "[1,2.5,\"text\",false]";
        JsonParser p = createParser(json);
        assertSame(JsonToken.START_ARRAY, p.nextToken());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(2.5, p.getDoubleValue(), 0.0);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("text", p.getText());
        assertSame(JsonToken.VALUE_FALSE, p.nextToken());
        assertFalse(p.getBooleanValue());
        assertSame(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNestedStructures() throws IOException {
        String json = "{\"outer\":{\"inner\":[1,2]}}";
        JsonParser p = createParser(json);
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("outer", p.getCurrentName());
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("inner", p.getCurrentName());
        assertSame(JsonToken.START_ARRAY, p.nextToken());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertSame(JsonToken.END_ARRAY, p.nextToken());
        assertSame(JsonToken.END_OBJECT, p.nextToken());
        assertSame(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetValueAsString() throws IOException {
        String json = "{\"s\":\"value\",\"n\":42}";
        JsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // VALUE_STRING
        assertEquals("value", p.getValueAsString());
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // VALUE_NUMBER_INT
        assertEquals("42", p.getValueAsString());
        assertEquals("default", p.getValueAsString("default"));
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetValueAsInt() throws IOException {
        String json = "{\"i\":123,\"f\":45.6,\"s\":\"abc\"}";
        JsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // VALUE_NUMBER_INT
        assertEquals(123, p.getValueAsInt());
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // VALUE_NUMBER_FLOAT
        assertEquals(45, p.getValueAsInt());
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // VALUE_STRING
        assertEquals(0, p.getValueAsInt());
        assertEquals(99, p.getValueAsInt(99));
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetTextCharacters() throws IOException {
        String json = "\"hello\"";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        char[] chars = p.getTextCharacters();
        assertEquals("hello", new String(chars, p.getTextOffset(), p.getTextLength()));
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetTextLengthAndOffset() throws IOException {
        String json = "\"test\"";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(4, p.getTextLength());
        assertEquals(0, p.getTextOffset());
        p.close();
    }

    // ============================================================
    // Partition B: Boundary Value Analysis & Extremes
    // ============================================================

    @Test(timeout = 4000)
    public void testEmptyString() throws IOException {
        String json = "\"\"";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("", p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testEmptyObject() throws IOException {
        String json = "{}";
        JsonParser p = createParser(json);
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testEmptyArray() throws IOException {
        String json = "[]";
        JsonParser p = createParser(json);
        assertSame(JsonToken.START_ARRAY, p.nextToken());
        assertSame(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testZeroAndNegativeNumbers() throws IOException {
        String json = "[0,-0,-123,0.0,-0.0]";
        JsonParser p = createParser(json);
        p.nextToken(); // START_ARRAY
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue()); // -0 is still 0
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-123, p.getIntValue());
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(0.0, p.getDoubleValue(), 0.0);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(0.0, p.getDoubleValue(), 0.0);
        p.close();
    }

    @Test(timeout = 4000)
    public void testLargeInteger() throws IOException {
        String json = "1234567890123456789";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1234567890123456789L, p.getLongValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testMaxDouble() throws IOException {
        String json = "1.7976931348623157E308";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.MAX_VALUE, p.getDoubleValue(), 0.0);
        p.close();
    }

    @Test(timeout = 4000)
    public void testMinDouble() throws IOException {
        String json = "4.9E-324";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.MIN_VALUE, p.getDoubleValue(), 0.0);
        p.close();
    }

    @Test(timeout = 4000)
    public void testVeryLongString() throws IOException {
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < 1000; i++) {
            sb.append('a');
        }
        sb.append('"');
        JsonParser p = createParser(sb.toString());
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(1000, p.getTextLength());
        p.close();
    }

    @Test(timeout = 4000)
    public void testLongFieldName() throws IOException {
        StringBuilder sb = new StringBuilder("{\"");
        for (int i = 0; i < 200; i++) {
            sb.append('x');
        }
        sb.append("\":1}");
        JsonParser p = createParser(sb.toString());
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(200, p.getCurrentName().length());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        p.close();
    }

    // ============================================================
    // Partition C: Defect-Targeted Branch (Long Floating Point)
    // ============================================================

    @Test(timeout = 4000)
    public void testLongerFloatingPoint() throws IOException {
        // This test targets the known defect: ArrayIndexOutOfBoundsException
        // when parsing a floating point number with many digits.
        // The bug is likely in _parseFloat where the output buffer overflows.
        StringBuilder sb = new StringBuilder();
        sb.append("1.");
        for (int i = 0; i < 300; i++) {
            sb.append('0');
        }
        sb.append('1');
        String json = sb.toString();
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        // The expected value is 1.000...001 (with many zeros)
        BigDecimal expected = new BigDecimal("1." + "0".repeat(300) + "1");
        assertEquals(expected.doubleValue(), p.getDoubleValue(), 0.0);
        p.close();
    }

    @Test(timeout = 4000)
    public void testLongerFloatingPointWithExponent() throws IOException {
        // Another variant: long mantissa with exponent
        StringBuilder sb = new StringBuilder();
        sb.append("1.");
        for (int i = 0; i < 200; i++) {
            sb.append('9');
        }
        sb.append("E10");
        String json = sb.toString();
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        // Just ensure no exception and value is finite
        assertTrue(Double.isFinite(p.getDoubleValue()));
        p.close();
    }

    @Test(timeout = 4000)
    public void testLongNegativeFloatingPoint() throws IOException {
        StringBuilder sb = new StringBuilder("-0.");
        for (int i = 0; i < 250; i++) {
            sb.append('1');
        }
        String json = sb.toString();
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(p.getDoubleValue() < 0);
        p.close();
    }

    // ============================================================
    // Partition D: Exception & Defensive Guard Paths
    // ============================================================

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidToken() throws IOException {
        String json = "foo";
        JsonParser p = createParser(json);
        p.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnquotedFieldName() throws IOException {
        String json = "{foo:1}";
        JsonParser p = createParser(json);
        p.nextToken();
    }

    @Test(timeout = 4000)
    public void testUnquotedFieldNameWithFeature() throws IOException {
        String json = "{foo:1}";
        JsonParser p = createParser(json, JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("foo", p.getCurrentName());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSingleQuotes() throws IOException {
        String json = "{'a':1}";
        JsonParser p = createParser(json, JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testComments() throws IOException {
        String json = "/* comment */ 1";
        JsonParser p = createParser(json, JsonParser.Feature.ALLOW_COMMENTS);
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testYAMLComment() throws IOException {
        String json = "# yaml\n1";
        JsonParser p = createParser(json, JsonParser.Feature.ALLOW_YAML_COMMENTS);
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testLeadingZerosNotAllowed() throws IOException {
        String json = "0123";
        JsonParser p = createParser(json);
        p.nextToken();
    }

    @Test(timeout = 4000)
    public void testLeadingZerosAllowed() throws IOException {
        String json = "0123";
        JsonParser p = createParser(json, JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNaN() throws IOException {
        String json = "NaN";
        JsonParser p = createParser(json, JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isNaN(p.getDoubleValue()));
        p.close();
    }

    @Test(timeout = 4000)
    public void testInfinity() throws IOException {
        String json = "Infinity";
        JsonParser p = createParser(json, JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);
        p.close();
    }

    @Test(timeout = 4000)
    public void testNegativeInfinity() throws IOException {
        String json = "-Infinity";
        JsonParser p = createParser(json, JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);
        p.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidEscape() throws IOException {
        String json = "\"\\x\"";
        JsonParser p = createParser(json);
        p.nextToken();
    }

    @Test(timeout = 4000)
    public void testEscapeSequences() throws IOException {
        String json = "\"\\b\\t\\n\\f\\r\\\"\\\\\"";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("\b\t\n\f\r\"\\", p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testUnicodeEscape() throws IOException {
        String json = "\"\\u0041\\u0042\"";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("AB", p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testMultiByteUTF8String() throws IOException {
        String json = "\"\\u00e9\\u00e0\\u00fc\"";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("\u00e9\u00e0\u00fc", p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSurrogatePair() throws IOException {
        String json = "\"\\ud83d\\ude00\"";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("\ud83d\ude00", p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testBinaryBase64() throws IOException {
        String json = "\"SGVsbG8=\"";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        byte[] expected = "Hello".getBytes("UTF-8");
        assertArrayEquals(expected, p.getBinaryValue(Base64Variants.MIME));
        p.close();
    }

    @Test(timeout = 4000)
    public void testBinaryBase64NoPadding() throws IOException {
        String json = "\"SGVsbG8\"";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        byte[] expected = "Hello".getBytes("UTF-8");
        assertArrayEquals(expected, p.getBinaryValue(Base64Variants.MIME_NO_LINEFEEDS));
        p.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidBinaryChar() throws IOException {
        String json = "\"!!!\"";
        JsonParser p = createParser(json);
        p.nextToken();
        p.getBinaryValue(Base64Variants.MIME);
    }

    // ============================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ============================================================

    @Test(timeout = 4000)
    public void testGetCodec() throws IOException {
        JsonFactory factory = new JsonFactory();
        JsonParser p = factory.createParser(new ByteArrayInputStream("{}".getBytes()));
        assertNull(p.getCodec());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSetCodec() throws IOException {
        JsonFactory factory = new JsonFactory();
        JsonParser p = factory.createParser(new ByteArrayInputStream("{}".getBytes()));
        ObjectCodec codec = new ObjectMapper();
        p.setCodec(codec);
        assertSame(codec, p.getCodec());
        p.close();
    }

    @Test(timeout = 4000)
    public void testReleaseBuffered() throws IOException {
        String json = "12345";
        JsonParser p = createParser(json);
        p.nextToken(); // consume 12345
        // releaseBuffered should return remaining bytes (none)
        assertEquals(0, p.releaseBuffered(new java.io.ByteArrayOutputStream()));
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetInputSource() throws IOException {
        InputStream in = new ByteArrayInputStream("{}".getBytes());
        JsonFactory factory = new JsonFactory();
        JsonParser p = factory.createParser(in);
        assertSame(in, p.getInputSource());
        p.close();
    }

    @Test(timeout = 4000)
    public void testCloseReleasesResources() throws IOException {
        JsonParser p = createParser("{}");
        p.close();
        // After close, nextToken should return null
        assertNull(p.nextToken());
    }

    @Test(timeout = 4000)
    public void testTokenLocation() throws IOException {
        String json = "{\n  \"key\" : 1\n}";
        JsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        JsonLocation loc = p.getTokenLocation();
        assertEquals(1, loc.getLineNr());
        assertEquals(1, loc.getColumnNr());
        p.nextToken(); // FIELD_NAME
        loc = p.getTokenLocation();
        assertEquals(2, loc.getLineNr());
        assertEquals(3, loc.getColumnNr());
        p.close();
    }

    @Test(timeout = 4000)
    public void testCurrentLocation() throws IOException {
        String json = "{\"a\":1}";
        JsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        JsonLocation loc = p.getCurrentLocation();
        assertEquals(1, loc.getLineNr());
        assertEquals(2, loc.getColumnNr()); // after '{'
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextFieldName() throws IOException {
        String json = "{\"foo\":1,\"bar\":2}";
        JsonParser p = createParser(json);
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertTrue(p.nextFieldName(new SerializableString() {
            @Override public String getValue() { return "foo"; }
            @Override public int charLength() { return 3; }
            @Override public byte[] asQuotedUTF8() { return new byte[]{'f','o','o'}; }
            @Override public byte[] asUnquotedUTF8() { return new byte[]{'f','o','o'}; }
            @Override public char[] asQuotedChars() { return new char[]{'f','o','o'}; }
        }));
        assertEquals(1, p.nextIntValue(0));
        assertFalse(p.nextFieldName(new SerializableString() {
            @Override public String getValue() { return "baz"; }
            @Override public int charLength() { return 3; }
            @Override public byte[] asQuotedUTF8() { return new byte[]{'b','a','z'}; }
            @Override public byte[] asUnquotedUTF8() { return new byte[]{'b','a','z'}; }
            @Override public char[] asQuotedChars() { return new char[]{'b','a','z'}; }
        }));
        assertEquals(2, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextTextValue() throws IOException {
        String json = "{\"a\":\"hello\",\"b\":42}";
        JsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        assertEquals("hello", p.nextTextValue());
        p.nextToken(); // FIELD_NAME
        assertNull(p.nextTextValue());
        assertEquals(42, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextIntValue() throws IOException {
        String json = "{\"a\":123,\"b\":\"notint\"}";
        JsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        assertEquals(123, p.nextIntValue(0));
        p.nextToken(); // FIELD_NAME
        assertEquals(99, p.nextIntValue(99));
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextLongValue() throws IOException {
        String json = "{\"a\":9999999999}";
        JsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        assertEquals(9999999999L, p.nextLongValue(0));
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextBooleanValue() throws IOException {
        String json = "{\"a\":true,\"b\":false,\"c\":1}";
        JsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        assertEquals(Boolean.TRUE, p.nextBooleanValue());
        p.nextToken(); // FIELD_NAME
        assertEquals(Boolean.FALSE, p.nextBooleanValue());
        p.nextToken(); // FIELD_NAME
        assertNull(p.nextBooleanValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testReadBinaryValue() throws IOException {
        String json = "\"SGVsbG8=\"";
        JsonParser p = createParser(json);
        p.nextToken();
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        int count = p.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(5, count);
        assertArrayEquals("Hello".getBytes("UTF-8"), out.toByteArray());
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueWithTokenIncomplete() throws IOException {
        // This tests the _tokenIncomplete path in getBinaryValue
        String json = "{\"data\":\"SGVsbG8=\"}";
        JsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // VALUE_STRING (tokenIncomplete set)
        byte[] data = p.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals("Hello".getBytes("UTF-8"), data);
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetTextWithTokenIncomplete() throws IOException {
        String json = "{\"msg\":\"hello\"}";
        JsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // VALUE_STRING (tokenIncomplete)
        assertEquals("hello", p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithTokenIncomplete() throws IOException {
        String json = "\"test\"";
        JsonParser p = createParser(json);
        p.nextToken(); // VALUE_STRING (tokenIncomplete)
        assertEquals("test", p.getValueAsString());
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetTextCharactersWithTokenIncomplete() throws IOException {
        String json = "\"abc\"";
        JsonParser p = createParser(json);
        p.nextToken(); // VALUE_STRING (tokenIncomplete)
        char[] chars = p.getTextCharacters();
        assertEquals("abc", new String(chars, p.getTextOffset(), p.getTextLength()));
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetTextLengthWithTokenIncomplete() throws IOException {
        String json = "\"xyz\"";
        JsonParser p = createParser(json);
        p.nextToken(); // VALUE_STRING (tokenIncomplete)
        assertEquals(3, p.getTextLength());
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetTextOffsetWithTokenIncomplete() throws IOException {
        String json = "\"xyz\"";
        JsonParser p = createParser(json);
        p.nextToken(); // VALUE_STRING (tokenIncomplete)
        assertEquals(0, p.getTextOffset());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSkipString() throws IOException {
        // This tests the _skipString path when tokenIncomplete is true
        String json = "{\"a\":\"long string\",\"b\":2}";
        JsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // VALUE_STRING (tokenIncomplete)
        // Skip to next token without reading the string
        p.nextToken(); // should be FIELD_NAME for "b"
        assertEquals("b", p.getCurrentName());
        p.close();
    }

    @Test(timeout = 4000)
    public void testHandleUnexpectedValue() throws IOException {
        // Test _handleUnexpectedValue with various characters
        String json = "+123";
        JsonParser p = createParser(json, JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMismatchedArrayClose() throws IOException {
        String json = "[}";
        JsonParser p = createParser(json);
        p.nextToken();
        p.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMismatchedObjectClose() throws IOException {
        String json = "{]";
        JsonParser p = createParser(json);
        p.nextToken();
        p.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMissingColon() throws IOException {
        String json = "{\"a\" 1}";
        JsonParser p = createParser(json);
        p.nextToken();
        p.nextToken();
        p.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMissingComma() throws IOException {
        String json = "{\"a\":1 \"b\":2}";
        JsonParser p = createParser(json);
        p.nextToken();
        p.nextToken();
        p.nextToken();
        p.nextToken();
    }

    @Test(timeout = 4000)
    public void testRootValueSeparator() throws IOException {
        // As per #105, root values must be separated by whitespace
        String json = "1 2";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMissingRootSpace() throws IOException {
        String json = "1a";
        JsonParser p = createParser(json);
        p.nextToken();
    }

    @Test(timeout = 4000)
    public void testGrowArrayBy() {
        int[] arr = new int[]{1,2,3};
        int[] grown = UTF8StreamJsonParser.growArrayBy(arr, 2);
        assertEquals(5, grown.length);
        assertArrayEquals(new int[]{1,2,3,0,0}, grown);
    }

    @Test(timeout = 4000)
    public void testGrowArrayByNull() {
        int[] grown = UTF8StreamJsonParser.growArrayBy(null, 5);
        assertEquals(5, grown.length);
    }

    // Additional test for the specific defect: very long floating point with many digits after decimal
    @Test(timeout = 4000)
    public void testVeryLongFloatingPointWithManyDigits() throws IOException {
        // This is a more extreme version of the defect
        StringBuilder sb = new StringBuilder("0.");
        for (int i = 0; i < 500; i++) {
            sb.append('1');
        }
        String json = sb.toString();
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        // Should not throw ArrayIndexOutOfBoundsException
        double val = p.getDoubleValue();
        assertTrue(val > 0);
        p.close();
    }
}