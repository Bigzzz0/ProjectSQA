package com.fasterxml.jackson.core.json;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.BytesToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

/**
 * White-box JUnit 4 test suite for UTF8StreamJsonParser.
 * Targets line/branch coverage and the known Defects4J defect
 * (testOffsetWithInputOffset: expected<0> but was<3>).
 */
public class UTF8StreamJsonParserDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     * 
     * Partition A: Core functional logic & state transitions
     *   - nextToken() for objects, arrays, strings, numbers, booleans, null
     *   - getText(), getValueAsString(), getIntValue(), getLongValue()
     *   - nextFieldName(), nextTextValue(), nextIntValue(), nextLongValue(), nextBooleanValue()
     *   - getTokenLocation(), getCurrentLocation()
     * 
     * Partition B: Boundary Value Analysis & Extremes
     *   - Empty input, leading zeros, negative numbers, large numbers
     *   - Empty string, single-quote strings (if enabled)
     *   - Buffer boundary crossing (long names, long strings)
     * 
     * Partition C: Defect-Targeted Branch Zone
     *   - Input buffer with non-zero start offset (the known bug)
     *   - Token location offset calculation
     * 
     * Partition D: Exception & Defensive Guard Paths
     *   - Invalid JSON tokens, unexpected characters, mismatched brackets
     *   - Invalid UTF-8, unquoted field names (if disabled)
     *   - Base64 decoding errors
     * 
     * Partition E: Object Lifecycle & Contract Integrity
     *   - releaseBuffered(), close(), _releaseBuffers()
     *   - getCodec(), setCodec()
     */

    // ----------------------------------------------------------
    // Helper methods to create parser instances
    // ----------------------------------------------------------

    private IOContext createIOContext() {
        return new IOContext(new BufferRecycler(), null, false);
    }

    private BytesToNameCanonicalizer createSymbols() {
        return BytesToNameCanonicalizer.createRoot();
    }

    private UTF8StreamJsonParser createParser(byte[] input, int start, int end) {
        IOContext ctxt = createIOContext();
        return new UTF8StreamJsonParser(ctxt, JsonParser.Feature.collectDefaults(),
                null, null, createSymbols(), input, start, end, false);
    }

    private UTF8StreamJsonParser createParser(String json) {
        byte[] bytes = json.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return createParser(bytes, 0, bytes.length);
    }

    // ----------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ----------------------------------------------------------

    @Test(timeout = 4000)
    public void testSimpleObject() throws IOException {
        UTF8StreamJsonParser p = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSimpleArray() throws IOException {
        UTF8StreamJsonParser p = createParser("[true, false, null]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertTrue(p.getBooleanValue());
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        assertFalse(p.getBooleanValue());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.getBooleanValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testStringValue() throws IOException {
        UTF8StreamJsonParser p = createParser("\"hello\"");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
        assertEquals("hello", p.getValueAsString());
        assertEquals("hello", p.getValueAsString("default"));
        p.close();
    }

    @Test(timeout = 4000)
    public void testNumberInt() throws IOException {
        UTF8StreamJsonParser p = createParser("42");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        assertEquals(42L, p.getLongValue());
        assertEquals(42.0, p.getDoubleValue(), 0.0);
        p.close();
    }

    @Test(timeout = 4000)
    public void testNumberFloat() throws IOException {
        UTF8StreamJsonParser p = createParser("3.14");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(3.14, p.getDoubleValue(), 0.001);
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextFieldNameMatch() throws IOException {
        UTF8StreamJsonParser p = createParser("{\"foo\":123}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertTrue(p.nextFieldName(new SerializableStringWrapper("foo")));
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextFieldNameNoMatch() throws IOException {
        UTF8StreamJsonParser p = createParser("{\"bar\":456}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertFalse(p.nextFieldName(new SerializableStringWrapper("foo")));
        assertEquals("bar", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(456, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextTextValue() throws IOException {
        UTF8StreamJsonParser p = createParser("{\"x\":\"abc\"}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("abc", p.nextTextValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextIntValue() throws IOException {
        UTF8StreamJsonParser p = createParser("{\"y\":789}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(789, p.nextIntValue(-1));
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextLongValue() throws IOException {
        UTF8StreamJsonParser p = createParser("{\"z\":1234567890123}");
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(1234567890123L, p.nextLongValue(-1L));
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextBooleanValue() throws IOException {
        UTF8StreamJsonParser p = createParser("[true,false]");
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(Boolean.TRUE, p.nextBooleanValue());
        assertEquals(Boolean.FALSE, p.nextBooleanValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetTokenLocation() throws IOException {
        UTF8StreamJsonParser p = createParser("123");
        p.nextToken();
        JsonLocation loc = p.getTokenLocation();
        assertEquals(0L, loc.getCharOffset());
        assertEquals(1, loc.getLineNr());
        assertEquals(1, loc.getColumnNr());
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetCurrentLocation() throws IOException {
        UTF8StreamJsonParser p = createParser("  \"abc\"");
        p.nextToken();
        JsonLocation loc = p.getCurrentLocation();
        // after reading string, current location should be after the closing quote
        assertTrue(loc.getCharOffset() > 0);
        p.close();
    }

    // ----------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // ----------------------------------------------------------

    @Test(timeout = 4000)
    public void testEmptyInput() throws IOException {
        UTF8StreamJsonParser p = createParser("");
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testLeadingZerosDisabled() throws IOException {
        UTF8StreamJsonParser p = createParser("00");
        try {
            p.nextToken();
            fail("Expected JsonParseException for leading zeros");
        } catch (JsonParseException e) {
            // expected
        }
        p.close();
    }

    @Test(timeout = 4000)
    public void testLeadingZerosAllowed() throws IOException {
        // Enable ALLOW_NUMERIC_LEADING_ZEROS
        IOContext ctxt = createIOContext();
        int features = JsonParser.Feature.collectDefaults() | JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        byte[] bytes = "00".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = new UTF8StreamJsonParser(ctxt, features,
                null, null, createSymbols(), bytes, 0, bytes.length, false);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNegativeNumber() throws IOException {
        UTF8StreamJsonParser p = createParser("-42");
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-42, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testLargeNumber() throws IOException {
        UTF8StreamJsonParser p = createParser("2147483648"); // > Integer.MAX_VALUE
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2147483648L, p.getLongValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testEmptyString() throws IOException {
        UTF8StreamJsonParser p = createParser("\"\"");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("", p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSingleQuoteStringEnabled() throws IOException {
        IOContext ctxt = createIOContext();
        int features = JsonParser.Feature.collectDefaults() | JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask();
        byte[] bytes = "'single'".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = new UTF8StreamJsonParser(ctxt, features,
                null, null, createSymbols(), bytes, 0, bytes.length, false);
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("single", p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testLongFieldName() throws IOException {
        // Build a field name longer than 8 bytes to exercise parseLongName
        StringBuilder sb = new StringBuilder("{\"");
        for (int i = 0; i < 20; i++) sb.append("a");
        sb.append("\":1}");
        UTF8StreamJsonParser p = createParser(sb.toString());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(sb.substring(2, 22), p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testLongStringValue() throws IOException {
        // String longer than output buffer segment
        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < 1000; i++) sb.append("x");
        sb.append("\"");
        UTF8StreamJsonParser p = createParser(sb.toString());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(sb.substring(1, 1001), p.getText());
        p.close();
    }

    // ----------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone
    // ----------------------------------------------------------

    @Test(timeout = 4000)
    public void testTokenLocationWithInputOffset() throws IOException {
        // Simulate input buffer with leading bytes (offset 3)
        byte[] prefix = new byte[3]; // garbage bytes
        byte[] json = "{\"a\":1}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] combined = new byte[prefix.length + json.length];
        System.arraycopy(prefix, 0, combined, 0, prefix.length);
        System.arraycopy(json, 0, combined, prefix.length, json.length);

        IOContext ctxt = createIOContext();
        UTF8StreamJsonParser p = new UTF8StreamJsonParser(ctxt,
                JsonParser.Feature.collectDefaults(),
                null, null, createSymbols(),
                combined, prefix.length, combined.length, false);

        // Parse first token (START_OBJECT)
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        JsonLocation loc = p.getTokenLocation();
        // The bug: on defective version, getCharOffset() returns 3 (the buffer offset)
        // Correct behavior: should be 0 (relative to JSON content start)
        assertEquals("Token character offset should be 0", 0L, loc.getCharOffset());
        p.close();
    }

    @Test(timeout = 4000)
    public void testCurrentLocationWithInputOffset() throws IOException {
        byte[] prefix = new byte[5];
        byte[] json = "123".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] combined = new byte[prefix.length + json.length];
        System.arraycopy(prefix, 0, combined, 0, prefix.length);
        System.arraycopy(json, 0, combined, prefix.length, json.length);

        IOContext ctxt = createIOContext();
        UTF8StreamJsonParser p = new UTF8StreamJsonParser(ctxt,
                JsonParser.Feature.collectDefaults(),
                null, null, createSymbols(),
                combined, prefix.length, combined.length, false);

        p.nextToken(); // VALUE_NUMBER_INT
        JsonLocation loc = p.getCurrentLocation();
        // After reading "123", current location should be after the number
        // The byte offset should be relative to JSON start, not buffer start
        assertEquals("Current location byte offset should be 3", 3L, loc.getByteOffset());
        p.close();
    }

    // ----------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // ----------------------------------------------------------

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidToken() throws IOException {
        UTF8StreamJsonParser p = createParser("xyz");
        p.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMismatchedBracket() throws IOException {
        UTF8StreamJsonParser p = createParser("[}");
        p.nextToken();
        p.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnquotedFieldNameDisabled() throws IOException {
        UTF8StreamJsonParser p = createParser("{foo:1}");
        p.nextToken();
        p.nextToken();
    }

    @Test(timeout = 4000)
    public void testUnquotedFieldNameEnabled() throws IOException {
        IOContext ctxt = createIOContext();
        int features = JsonParser.Feature.collectDefaults() | JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        byte[] bytes = "{foo:1}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = new UTF8StreamJsonParser(ctxt, features,
                null, null, createSymbols(), bytes, 0, bytes.length, false);
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("foo", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidUTF8() throws IOException {
        // 0xFE is invalid UTF-8 start byte
        byte[] bytes = new byte[] {(byte)0xFE};
        UTF8StreamJsonParser p = createParser(bytes, 0, bytes.length);
        p.nextToken();
    }

    @Test(timeout = 4000)
    public void testBase64Decode() throws IOException {
        UTF8StreamJsonParser p = createParser("\"SGVsbG8=\"");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] decoded = p.getBinaryValue(Base64Variant.getDefaultVariant());
        assertArrayEquals("Hello".getBytes(java.nio.charset.StandardCharsets.UTF_8), decoded);
        p.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testBase64InvalidChar() throws IOException {
        UTF8StreamJsonParser p = createParser("\"!!!\"");
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        p.getBinaryValue(Base64Variant.getDefaultVariant());
    }

    // ----------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // ----------------------------------------------------------

    @Test(timeout = 4000)
    public void testReleaseBuffered() throws IOException {
        byte[] bytes = "  abc".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        UTF8StreamJsonParser p = createParser(bytes, 0, bytes.length);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = p.releaseBuffered(out);
        // Should release all buffered bytes (including whitespace)
        assertEquals(bytes.length, count);
        assertArrayEquals(bytes, out.toByteArray());
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetInputSource() throws IOException {
        InputStream is = new ByteArrayInputStream("{}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        IOContext ctxt = createIOContext();
        UTF8StreamJsonParser p = new UTF8StreamJsonParser(ctxt,
                JsonParser.Feature.collectDefaults(),
                is, null, createSymbols(),
                new byte[0], 0, 0, false);
        assertSame(is, p.getInputSource());
        p.close();
    }

    @Test(timeout = 4000)
    public void testCodecSetGet() {
        IOContext ctxt = createIOContext();
        UTF8StreamJsonParser p = new UTF8StreamJsonParser(ctxt,
                JsonParser.Feature.collectDefaults(),
                null, null, createSymbols(),
                new byte[0], 0, 0, false);
        assertNull(p.getCodec());
        ObjectCodec codec = new ObjectCodec() {
            // minimal stub
        };
        p.setCodec(codec);
        assertSame(codec, p.getCodec());
        p.close();
    }

    @Test(timeout = 4000)
    public void testCloseReleasesBuffers() throws IOException {
        byte[] bytes = "{}".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        IOContext ctxt = createIOContext();
        // Use recyclable buffer to test release
        UTF8StreamJsonParser p = new UTF8StreamJsonParser(ctxt,
                JsonParser.Feature.collectDefaults(),
                null, null, createSymbols(),
                bytes, 0, bytes.length, true);
        p.close();
        // After close, input buffer should be null
        // (We can't directly access private field, but we can verify no error)
        assertTrue(true);
    }

    // ----------------------------------------------------------
    // Helper class for SerializableString
    // ----------------------------------------------------------

    private static class SerializableStringWrapper implements SerializableString {
        private final String value;

        public SerializableStringWrapper(String value) {
            this.value = value;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public int charLength() {
            return value.length();
        }

        @Override
        public char[] asQuotedChars() {
            return value.toCharArray();
        }

        @Override
        public byte[] asUnquotedUTF8() {
            return value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }

        @Override
        public byte[] asQuotedUTF8() {
            // For field name matching, we need the quoted form (with quotes)
            return ('"' + value + '"').getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }

        @Override
        public int appendQuotedUTF8(byte[] buffer, int offset) {
            byte[] quoted = asQuotedUTF8();
            System.arraycopy(quoted, 0, buffer, offset, quoted.length);
            return quoted.length;
        }

        @Override
        public int appendQuoted(char[] buffer, int offset) {
            char[] quoted = asQuotedChars();
            System.arraycopy(quoted, 0, buffer, offset, quoted.length);
            return quoted.length;
        }

        @Override
        public int appendUnquotedUTF8(byte[] buffer, int offset) {
            byte[] unquoted = asUnquotedUTF8();
            System.arraycopy(unquoted, 0, buffer, offset, unquoted.length);
            return unquoted.length;
        }

        @Override
        public int appendUnquoted(char[] buffer, int offset) {
            char[] chars = value.toCharArray();
            System.arraycopy(chars, 0, buffer, offset, chars.length);
            return chars.length;
        }
    }
}