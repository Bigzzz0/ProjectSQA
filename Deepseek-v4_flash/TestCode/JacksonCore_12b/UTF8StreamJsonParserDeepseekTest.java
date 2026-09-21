package com.fasterxml.jackson.core.json;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - nextToken() for FIELD_NAME, VALUE_STRING, START_ARRAY, START_OBJECT, VALUE_NUMBER, VALUE_TRUE/FALSE/NULL
 *   - getText(), getValueAsString(), getValueAsInt() for various token types
 *   - _finishString(), _finishAndReturnString() for string parsing
 *   - _parsePosNumber(), _parseNegNumber(), _parseFloat() for number parsing
 *   - _skipColon(), _skipWS(), _skipWSOrEnd() for whitespace handling
 *   - _matchToken() for literal matching
 *   - _parseName() for field name parsing (fast path, medium, long)
 *   - _decodeEscaped() for escape sequences
 *   - _decodeUtf8_2, _decodeUtf8_3, _decodeUtf8_4 for multi-byte UTF-8
 *   - _skipString() for skipping incomplete strings
 *   - _nextAfterName() for transitioning after field name
 *   - nextFieldName(), nextTextValue(), nextIntValue(), nextLongValue(), nextBooleanValue()
 *   - getTextCharacters(), getTextLength(), getTextOffset()
 *   - getBinaryValue(), readBinaryValue(), _readBinary(), _decodeBase64()
 *   - releaseBuffered(), getInputSource(), loadMore(), _loadToHaveAtLeast()
 *   - _closeInput(), _releaseBuffers()
 *   - getTokenLocation(), getCurrentLocation(), _updateLocation(), _updateNameLocation()
 * 
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 *   - Empty input, empty string "", empty field name
 *   - Single character field names, very long field names (>12 bytes)
 *   - Numbers: 0, leading zeros (with/without ALLOW_NUMERIC_LEADING_ZEROS), negative, MAX_INT, MIN_INT
 *   - Floating point: 0.0, 1e10, 1.5e-3, NaN, Infinity (with/without ALLOW_NON_NUMERIC_NUMBERS)
 *   - Strings with escape sequences: \n, \t, \\, \", \uXXXX
 *   - Strings with multi-byte UTF-8 characters (2-byte, 3-byte, 4-byte/surrogate)
 *   - Base64: empty, single char, padding variants, no padding
 *   - Comments: //, /* * /, YAML # (with/without ALLOW_COMMENTS, ALLOW_YAML_COMMENTS)
 *   - Single quotes (with/without ALLOW_SINGLE_QUOTES)
 *   - Unquoted field names (with/without ALLOW_UNQUOTED_FIELD_NAMES)
 *   - Buffer boundary conditions: input split across buffer, _loadToHaveAtLeast
 *   - Root value spacing verification (_verifyRootSpace)
 *   - _quadBuffer growth (growArrayBy)
 *   - _nameCopyBuffer allocation
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Defect: Location tracking for field names in objects
 *     * _updateNameLocation() vs _updateLocation() usage
 *     * getTokenLocation() returns _nameInputTotal/_nameInputRow for FIELD_NAME
 *     * getCurrentLocation() uses _currInputProcessed + _inputPtr
 *     * The known defect: "expected:<6> but was:<1>" indicates column offset miscalculation
 *     * Target: _tokenInputCol vs _nameInputCol, _currInputRowStart handling
 *     * Specific scenario: object with multiple fields, checking token column offset
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Invalid UTF-8 bytes (initial, middle)
 *   - Unexpected end of input in various contexts
 *   - Invalid number formats (leading zeros disallowed, no digits after sign)
 *   - Invalid escape sequences
 *   - Mismatched closing brackets
 *   - Unexpected characters where value expected
 *   - Invalid base64 characters
 *   - InputStream.read() returning 0
 *   - Null token access
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Constructor with various parameters
 *   - getCodec()/setCodec()
 *   - close() and subsequent operations
 *   - _releaseBuffers() and symbol table release
 */
public class UTF8StreamJsonParserDeepseekTest {

    /*
     * Helper method to create a UTF8StreamJsonParser from a JSON string.
     * Uses default factory features and a fresh IOContext.
     */
    private UTF8StreamJsonParser createParser(String json) throws IOException {
        return createParser(json, JsonParser.Feature.collectDefaults());
    }

    private UTF8StreamJsonParser createParser(String json, int features) throws IOException {
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        InputStream in = new ByteArrayInputStream(data);
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot();
        // Use a small buffer to force buffer boundary conditions
        byte[] inputBuffer = new byte[Math.min(data.length + 10, 64)];
        System.arraycopy(data, 0, inputBuffer, 0, data.length);
        return new UTF8StreamJsonParser(ctxt, features, in, null, sym,
                inputBuffer, 0, data.length, true);
    }

    /*
     * Helper to advance parser to a specific token and return it.
     */
    private JsonToken nextToken(UTF8StreamJsonParser parser) throws IOException {
        return parser.nextToken();
    }

    // ===================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ===================================================================

    @Test(timeout = 4000)
    public void testSimpleObjectParsing() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1,\"b\":\"hello\"}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        assertEquals("hello", parser.getText());
        assertEquals(JsonToken.END_OBJECT, nextToken(parser));
        assertNull(nextToken(parser));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testSimpleArrayParsing() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true, false, null]");
        assertEquals(JsonToken.START_ARRAY, nextToken(parser));
        assertEquals(JsonToken.VALUE_TRUE, nextToken(parser));
        assertTrue(parser.getBooleanValue());
        assertEquals(JsonToken.VALUE_FALSE, nextToken(parser));
        assertFalse(parser.getBooleanValue());
        assertEquals(JsonToken.VALUE_NULL, nextToken(parser));
        assertNull(parser.getEmbeddedObject());
        assertEquals(JsonToken.END_ARRAY, nextToken(parser));
        assertNull(nextToken(parser));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNestedStructures() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"x\":[1,{\"y\":2}]}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("x", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, nextToken(parser));
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("y", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, nextToken(parser));
        assertEquals(JsonToken.END_ARRAY, nextToken(parser));
        assertEquals(JsonToken.END_OBJECT, nextToken(parser));
        assertNull(nextToken(parser));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetTextForVariousTokens() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"str\" 42 3.14 true false null");
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        assertEquals("str", parser.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals("42", parser.getText());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, nextToken(parser));
        assertEquals("3.14", parser.getText());
        assertEquals(JsonToken.VALUE_TRUE, nextToken(parser));
        assertEquals("true", parser.getText());
        assertEquals(JsonToken.VALUE_FALSE, nextToken(parser));
        assertEquals("false", parser.getText());
        assertEquals(JsonToken.VALUE_NULL, nextToken(parser));
        assertEquals("null", parser.getText());
        assertNull(nextToken(parser));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetValueAsString() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"abc\" 123 true null");
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        assertEquals("abc", parser.getValueAsString());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals("123", parser.getValueAsString());
        assertEquals(JsonToken.VALUE_TRUE, nextToken(parser));
        assertEquals("true", parser.getValueAsString());
        assertEquals(JsonToken.VALUE_NULL, nextToken(parser));
        assertNull(parser.getValueAsString());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetValueAsInt() throws IOException {
        UTF8StreamJsonParser parser = createParser("42 3.14 \"notanumber\" null");
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(42, parser.getValueAsInt());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, nextToken(parser));
        assertEquals(3, parser.getValueAsInt()); // truncation
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        assertEquals(0, parser.getValueAsInt()); // default
        assertEquals(JsonToken.VALUE_NULL, nextToken(parser));
        assertEquals(0, parser.getValueAsInt());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithDefault() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"abc\"");
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        assertEquals(99, parser.getValueAsInt(99));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNextFieldName() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"foo\":1,\"bar\":2}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertTrue(parser.nextFieldName(new SerializableString() {
            public String getValue() { return "foo"; }
            public int charLength() { return 3; }
            public char[] asQuotedChars() { return "\"foo\"".toCharArray(); }
            public byte[] asUnquotedUTF8() { return "foo".getBytes(StandardCharsets.UTF_8); }
            public byte[] asQuotedUTF8() { return "\"foo\"".getBytes(StandardCharsets.UTF_8); }
            public int appendQuotedUTF8(byte[] buffer, int offset) { return 0; }
            public int appendQuoted(char[] buffer, int offset) { return 0; }
            public int appendUnquotedUTF8(byte[] buffer, int offset) { return 0; }
            public int appendUnquoted(char[] buffer, int offset) { return 0; }
        }));
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        assertFalse(parser.nextFieldName(new SerializableString() {
            public String getValue() { return "foo"; }
            public int charLength() { return 3; }
            public char[] asQuotedChars() { return "\"foo\"".toCharArray(); }
            public byte[] asUnquotedUTF8() { return "foo".getBytes(StandardCharsets.UTF_8); }
            public byte[] asQuotedUTF8() { return "\"foo\"".getBytes(StandardCharsets.UTF_8); }
            public int appendQuotedUTF8(byte[] buffer, int offset) { return 0; }
            public int appendQuoted(char[] buffer, int offset) { return 0; }
            public int appendUnquotedUTF8(byte[] buffer, int offset) { return 0; }
            public int appendUnquoted(char[] buffer, int offset) { return 0; }
        }));
        assertEquals(JsonToken.FIELD_NAME, parser.getCurrentToken());
        assertEquals("bar", parser.getCurrentName());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNextTextValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":\"hello\",\"b\":42}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals("hello", parser.nextTextValue());
        assertNull(parser.nextTextValue()); // b is number
        assertEquals(42, parser.getIntValue());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNextIntValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":42,\"b\":\"x\"}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(42, parser.nextIntValue(0));
        assertEquals(99, parser.nextIntValue(99)); // b is string, default returned
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNextLongValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1234567890123,\"b\":false}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(1234567890123L, parser.nextLongValue(0L));
        assertEquals(Long.MAX_VALUE, parser.nextLongValue(Long.MAX_VALUE));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNextBooleanValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":true,\"b\":42}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(Boolean.TRUE, parser.nextBooleanValue());
        assertNull(parser.nextBooleanValue());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetTextCharacters() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"test\"");
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        char[] chars = parser.getTextCharacters();
        assertEquals("test", new String(chars, parser.getTextOffset(), parser.getTextLength()));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetTextLength() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"hello\"");
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        assertEquals(5, parser.getTextLength());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetTextOffset() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"hello\"");
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        assertEquals(0, parser.getTextOffset());
        parser.close();
    }

    // ===================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ===================================================================

    @Test(timeout = 4000)
    public void testEmptyString() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"\"");
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        assertEquals("", parser.getText());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testEmptyFieldName() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"\":1}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testLongFieldName() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"");
        for (int i = 0; i < 20; i++) {
            sb.append((char) ('a' + (i % 26)));
        }
        sb.append("\":1}");
        UTF8StreamJsonParser parser = createParser(sb.toString());
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        String name = parser.getCurrentName();
        assertEquals(20, name.length());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNumberBoundaries() throws IOException {
        UTF8StreamJsonParser parser = createParser("0 -0 2147483647 -2147483648");
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(0, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(0, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(Integer.MAX_VALUE, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(Integer.MIN_VALUE, parser.getIntValue());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testLeadingZerosAllowed() throws IOException {
        int features = JsonParser.Feature.collectDefaults();
        features |= JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        UTF8StreamJsonParser parser = createParser("00123", features);
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(123, parser.getIntValue());
        parser.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testLeadingZerosNotAllowed() throws IOException {
        UTF8StreamJsonParser parser = createParser("00123");
        nextToken(parser);
    }

    @Test(timeout = 4000)
    public void testFloatingPointNumbers() throws IOException {
        UTF8StreamJsonParser parser = createParser("3.14 1e10 2.5e-3 -0.5");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, nextToken(parser));
        assertEquals(3.14, parser.getDoubleValue(), 1e-9);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, nextToken(parser));
        assertEquals(1e10, parser.getDoubleValue(), 1e-9);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, nextToken(parser));
        assertEquals(2.5e-3, parser.getDoubleValue(), 1e-9);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, nextToken(parser));
        assertEquals(-0.5, parser.getDoubleValue(), 1e-9);
        parser.close();
    }

    @Test(timeout = 4000)
    public void testNaNandInfinityAllowed() throws IOException {
        int features = JsonParser.Feature.collectDefaults();
        features |= JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask();
        UTF8StreamJsonParser parser = createParser("NaN Infinity -Infinity", features);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, nextToken(parser));
        assertTrue(Double.isNaN(parser.getDoubleValue()));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, nextToken(parser));
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, nextToken(parser));
        assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);
        parser.close();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testNaNNotAllowed() throws IOException {
        UTF8StreamJsonParser parser = createParser("NaN");
        nextToken(parser);
    }

    @Test(timeout = 4000)
    public void testStringWithEscapeSequences() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"\\n\\t\\\\\\\"\\/\\b\\f\\r\"");
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        assertEquals("\n\t\\\"/\b\f\r", parser.getText());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testStringWithUnicodeEscape() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"\\u0041\\u0042\\u0043\"");
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        assertEquals("ABC", parser.getText());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testStringWithMultiByteUTF8() throws IOException {
        // 2-byte: é (U+00E9), 3-byte: € (U+20AC), 4-byte: 𐍈 (U+10348)
        UTF8StreamJsonParser parser = createParser("\"\\u00E9\\u20AC\\uD800\\uDF48\"");
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        String text = parser.getText();
        assertEquals(3, text.length()); // 3 code points
        assertEquals('\u00E9', text.charAt(0));
        assertEquals('\u20AC', text.charAt(1));
        assertEquals('\uD800', text.charAt(2)); // high surrogate
        // The low surrogate is part of the same character
        parser.close();
    }

    @Test(timeout = 4000)
    public void testBase64Decoding() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"SGVsbG8gV29ybGQ=\"");
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        byte[] decoded = parser.getBinaryValue(Base64Variants.MIME);
        assertEquals("Hello World", new String(decoded, StandardCharsets.UTF_8));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testBase64NoPadding() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"SGVsbG8gV29ybGQ\"");
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        byte[] decoded = parser.getBinaryValue(Base64Variants.MIME_NO_LINEFEEDS);
        assertEquals("Hello World", new String(decoded, StandardCharsets.UTF_8));
        parser.close();
    }

    @Test(timeout = 4000)
    public void testCommentsAllowed() throws IOException {
        int features = JsonParser.Feature.collectDefaults();
        features |= JsonParser.Feature.ALLOW_COMMENTS.getMask();
        UTF8StreamJsonParser parser = createParser("/* comment */ 42", features);
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(42, parser.getIntValue());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testYAMLCommentsAllowed() throws IOException {
        int features = JsonParser.Feature.collectDefaults();
        features |= JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();
        UTF8StreamJsonParser parser = createParser("# yaml comment\n42", features);
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(42, parser.getIntValue());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testSingleQuotesAllowed() throws IOException {
        int features = JsonParser.Feature.collectDefaults();
        features |= JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask();
        UTF8StreamJsonParser parser = createParser("{'a':1}", features);
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testUnquotedFieldNamesAllowed() throws IOException {
        int features = JsonParser.Feature.collectDefaults();
        features |= JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        UTF8StreamJsonParser parser = createParser("{a:1}", features);
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testBufferBoundary() throws IOException {
        // Create a string that forces buffer boundary crossing
        StringBuilder sb = new StringBuilder();
        sb.append("{\"");
        for (int i = 0; i < 100; i++) {
            sb.append("x");
        }
        sb.append("\":1}");
        UTF8StreamJsonParser parser = createParser(sb.toString());
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        String name = parser.getCurrentName();
        assertEquals(100, name.length());
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        parser.close();
    }

    // ===================================================================
    // Partition C: Defect-Targeted Branch Zone
    // ===================================================================

    @Test(timeout = 4000)
    public void testFieldNameLocationTracking() throws IOException {
        // This test targets the known defect where token column offset is miscalculated
        // for field names in objects. The defect: expected:<6> but was:<1>
        // We need to verify that getTokenLocation() returns correct column for field names.
        UTF8StreamJsonParser parser = createParser("{\"abc\":1,\"def\":2}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        
        // First field name
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        JsonLocation loc1 = parser.getTokenLocation();
        // The field name "abc" starts at column 2 (after '{'), so column should be 2
        // (1-based column, offset from start of line)
        assertEquals(2, loc1.getColumnNr());
        
        // Second field name
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        JsonLocation loc2 = parser.getTokenLocation();
        // After ",1,\" the second field name starts at column 7 (1-based)
        // "{ \" a b c \" : 1 , \" d e f \" "
        //  1 2 3 4 5 6 7 8 9 ...
        assertEquals(7, loc2.getColumnNr());
        
        parser.close();
    }

    @Test(timeout = 4000)
    public void testOffsetWithObjectFieldsUsingReader() throws IOException {
        // This is the exact scenario from the Defects4J defect report:
        // "expected:<6> but was:<1>" for column offset
        // The JSON: {"a":1,"b":2} - the second field name "b" should have column 6
        UTF8StreamJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        
        // First field
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("a", parser.getCurrentName());
        JsonLocation locA = parser.getTokenLocation();
        assertEquals(2, locA.getColumnNr()); // column 2: '{' then '"'
        
        // Value
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        assertEquals(1, parser.getIntValue());
        
        // Second field - this is where the defect manifests
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("b", parser.getCurrentName());
        JsonLocation locB = parser.getTokenLocation();
        // Expected column: 6 (1-based: { " a " : 1 , " b " )
        //                   1 2 3 4 5 6 7 8 ...
        assertEquals(6, locB.getColumnNr());
        
        parser.close();
    }

    @Test(timeout = 4000)
    public void testTokenLocationForFieldNameVsValue() throws IOException {
        // Verify that getTokenLocation() returns different locations for field name vs value
        UTF8StreamJsonParser parser = createParser("{\"x\":42}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        JsonLocation fieldLoc = parser.getTokenLocation();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        JsonLocation valueLoc = parser.getTokenLocation();
        
        // Field name "x" starts at column 2, value 42 starts at column 5
        assertTrue(fieldLoc.getColumnNr() < valueLoc.getColumnNr());
        assertEquals(2, fieldLoc.getColumnNr());
        assertEquals(5, valueLoc.getColumnNr());
        
        parser.close();
    }

    @Test(timeout = 4000)
    public void testCurrentLocationAfterFieldName() throws IOException {
        // Verify getCurrentLocation() returns correct position after parsing field name
        UTF8StreamJsonParser parser = createParser("{\"abc\":1}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        JsonLocation currentLoc = parser.getCurrentLocation();
        // After reading "abc", the parser should be at position after the closing quote
        // which is column 6 (1-based): { " a b c " 
        //                              1 2 3 4 5 6
        assertEquals(6, currentLoc.getColumnNr());
        
        parser.close();
    }

    // ===================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ===================================================================

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidUTF8InitialByte() throws IOException {
        // Create a parser with invalid UTF-8 start byte (0xFE)
        byte[] data = new byte[] { (byte) 0xFE };
        InputStream in = new ByteArrayInputStream(data);
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot();
        UTF8StreamJsonParser parser = new UTF8StreamJsonParser(ctxt, 
                JsonParser.Feature.collectDefaults(), in, null, sym,
                data, 0, data.length, true);
        nextToken(parser);
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnexpectedEndOfInput() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":");
        nextToken(parser);
        nextToken(parser);
        nextToken(parser); // Should fail
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMismatchedBrackets() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1,2,3}");
        nextToken(parser);
        nextToken(parser);
        nextToken(parser);
        nextToken(parser); // Should fail
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidNumberFormat() throws IOException {
        UTF8StreamJsonParser parser = createParser("-");
        nextToken(parser);
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidEscapeSequence() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"\\x\"");
        nextToken(parser);
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnexpectedCharacter() throws IOException {
        UTF8StreamJsonParser parser = createParser("{!}");
        nextToken(parser);
        nextToken(parser);
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidBase64Character() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"!!!\"");
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        parser.getBinaryValue(Base64Variants.MIME);
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testTrailingCommaInObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1,}");
        nextToken(parser);
        nextToken(parser);
        nextToken(parser);
        nextToken(parser); // Should fail
    }

    // ===================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ===================================================================

    @Test(timeout = 4000)
    public void testCodecGetterSetter() throws IOException {
        UTF8StreamJsonParser parser = createParser("{}");
        assertNull(parser.getCodec());
        ObjectCodec dummy = new ObjectCodec() {
            @Override public JsonParser getFactory() { return null; }
            @Override public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
            @Override public JsonNode createObjectNode() { return null; }
            @Override public JsonNode createArrayNode() { return null; }
            @Override public JsonParser traverse(JsonNode node) { return null; }
            @Override public <T extends TreeNode> T readTree(JsonParser p) { return null; }
            @Override public void writeValue(JsonGenerator g, Object value) {}
        };
        parser.setCodec(dummy);
        assertSame(dummy, parser.getCodec());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testReleaseBuffered() throws IOException {
        UTF8StreamJsonParser parser = createParser("42");
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        int count = parser.releaseBuffered(out);
        assertTrue(count > 0);
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetInputSource() throws IOException {
        UTF8StreamJsonParser parser = createParser("42");
        assertTrue(parser.getInputSource() instanceof InputStream);
        parser.close();
    }

    @Test(timeout = 4000)
    public void testCloseAndReuse() throws IOException {
        UTF8StreamJsonParser parser = createParser("42");
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        parser.close();
        // After close, nextToken should return null
        assertNull(nextToken(parser));
        parser.close(); // Double close should be safe
    }

    @Test(timeout = 4000)
    public void testTokenIncompleteFlag() throws IOException {
        UTF8StreamJsonParser parser = createParser("\"hello\"");
        assertEquals(JsonToken.VALUE_STRING, nextToken(parser));
        // The token should be complete after nextToken
        assertEquals("hello", parser.getText());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetTokenLocationForStartObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        JsonLocation loc = parser.getTokenLocation();
        assertEquals(1, loc.getColumnNr());
        assertEquals(1, loc.getLineNr());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testGetCurrentLocation() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        JsonLocation loc = parser.getCurrentLocation();
        // After reading '{', current position is at column 2
        assertEquals(2, loc.getColumnNr());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testMultipleFieldNamesWithLocation() throws IOException {
        // Comprehensive test for field name location tracking
        UTF8StreamJsonParser parser = createParser("{\"a\":1,\"b\":2,\"c\":3}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        
        // Field "a" at column 2
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("a", parser.getCurrentName());
        assertEquals(2, parser.getTokenLocation().getColumnNr());
        
        // Value 1
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        
        // Field "b" at column 7
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("b", parser.getCurrentName());
        assertEquals(7, parser.getTokenLocation().getColumnNr());
        
        // Value 2
        assertEquals(JsonToken.VALUE_NUMBER_INT, nextToken(parser));
        
        // Field "c" at column 12
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("c", parser.getCurrentName());
        assertEquals(12, parser.getTokenLocation().getColumnNr());
        
        parser.close();
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedObjectLocation() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":{\"b\":{\"c\":1}}}");
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("a", parser.getCurrentName());
        assertEquals(2, parser.getTokenLocation().getColumnNr());
        
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("b", parser.getCurrentName());
        assertEquals(6, parser.getTokenLocation().getColumnNr());
        
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("c", parser.getCurrentName());
        assertEquals(10, parser.getTokenLocation().getColumnNr());
        
        parser.close();
    }

    @Test(timeout = 4000)
    public void testArrayWithObjectsLocation() throws IOException {
        UTF8StreamJsonParser parser = createParser("[{\"x\":1},{\"y\":2}]");
        assertEquals(JsonToken.START_ARRAY, nextToken(parser));
        
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("x", parser.getCurrentName());
        assertEquals(3, parser.getTokenLocation().getColumnNr());
        
        assertEquals(JsonToken.END_OBJECT, nextToken(parser));
        
        assertEquals(JsonToken.START_OBJECT, nextToken(parser));
        
        assertEquals(JsonToken.FIELD_NAME, nextToken(parser));
        assertEquals("y", parser.getCurrentName());
        assertEquals(9, parser.getTokenLocation().getColumnNr());
        
        parser.close();
    }
}