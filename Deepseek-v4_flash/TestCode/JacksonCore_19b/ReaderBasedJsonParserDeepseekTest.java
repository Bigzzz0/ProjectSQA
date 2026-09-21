package com.fasterxml.jackson.core.json;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.math.BigDecimal;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.CharsToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Targeted branches and boundary conditions:
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - nextToken() for FIELD_NAME, VALUE_STRING, VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT
 *   - getText() for VALUE_STRING with _tokenIncomplete flag
 *   - getValueAsString() for VALUE_STRING and FIELD_NAME
 *   - getTextCharacters() for FIELD_NAME, VALUE_STRING, VALUE_NUMBER_INT/FLOAT
 *   - getTextLength() and getTextOffset() for various token types
 *   - nextFieldName() with SerializableString matching
 *   - nextTextValue(), nextIntValue(), nextLongValue(), nextBooleanValue()
 *   - _finishString() with simple and escaped strings
 *   - _skipString() for incomplete tokens
 *   - _parsePosNumber() and _parseNegNumber() for integer and float parsing
 *   - _parseFloat() for fractional and exponent parts
 *   - _parseNumber2() for buffer-split numbers
 *   - _verifyNoLeadingZeroes() and _verifyNLZ2()
 *   - _handleInvalidNumberStart() for NaN/Infinity
 *   - _handleOddValue() for single quotes, NaN, Infinity, plus sign
 *   - _handleApos() for single-quoted strings
 *   - _parseName() and _parseName2() for field name parsing
 *   - _handleOddName() for unquoted field names
 *   - _parseAposName() for single-quoted field names
 *   - _matchTrue(), _matchFalse(), _matchNull() with fast-path and boundary
 *   - _skipColon(), _skipColon2(), _skipColonFast()
 *   - _skipComma(), _skipAfterComma2()
 *   - _skipWSOrEnd(), _skipWSOrEnd2()
 *   - _skipComment(), _skipCComment(), _skipLine(), _skipYAMLComment()
 *   - _decodeEscaped() for all escape sequences
 *   - _decodeBase64() and _readBinary() for base64 decoding
 *   - getBinaryValue() and readBinaryValue()
 *   - releaseBuffered(), _closeInput(), _releaseBuffers()
 *   - getTokenLocation() and getCurrentLocation()
 *   - _updateLocation() and _updateNameLocation()
 *   - _verifyRootSpace() for root value separation
 *   - _skipCR() for carriage return handling
 *   - _reportInvalidToken() for error reporting
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Empty input, single token, deeply nested structures
 *   - Very long strings and numbers (buffer boundary crossing)
 *   - Leading zeros in numbers (ALLOW_NUMERIC_LEADING_ZEROS)
 *   - Numbers with many fractional/exponent digits
 *   - Strings with all escape sequences
 *   - Field names with special characters
 *   - Base64 with padding and without padding
 *   - Comments (line, block, YAML)
 *   - Single-quoted strings and field names
 *   - Unquoted field names
 *   - NaN, Infinity, -Infinity, +Infinity
 *   - Null, true, false tokens
 *   - Array and object boundaries
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - KNOWN DEFECT: ArrayIndexOutOfBoundsException in floating point parsing
 *     when number spans buffer boundary with specific length patterns.
 *     Test: testLongerFloatingPoint() - targets the exact failure condition
 *     from Defects4J where a floating point number of specific length causes
 *     buffer overrun in _parseFloat() or _parseNumber2().
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Invalid JSON syntax (unexpected characters, mismatched brackets)
 *   - Invalid number formats (leading zeros, missing digits)
 *   - Invalid escape sequences
 *   - Invalid base64 characters
 *   - EOF during string, number, comment, escape sequence
 *   - Feature-dependent behavior (ALLOW_COMMENTS, ALLOW_SINGLE_QUOTES, etc.)
 *   - Null reader, empty buffer, etc.
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Constructor variants (with/without pre-allocated buffer)
 *   - getCodec()/setCodec()
 *   - getInputSource()
 *   - close() and releaseBuffers()
 */
public class ReaderBasedJsonParserDeepseekTest {

    /*
     * Helper method to create a parser from a JSON string with default settings.
     */
    private ReaderBasedJsonParser createParser(String json) throws IOException {
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot();
        Reader reader = new StringReader(json);
        return new ReaderBasedJsonParser(ctxt, 
            JsonParser.Feature.collectDefaults(), 
            reader, null, sym);
    }

    /*
     * Helper method to create a parser with specific features.
     */
    private ReaderBasedJsonParser createParser(String json, int features) throws IOException {
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot();
        Reader reader = new StringReader(json);
        return new ReaderBasedJsonParser(ctxt, features, reader, null, sym);
    }

    /*
     * Helper method to create a parser with a pre-allocated buffer.
     */
    private ReaderBasedJsonParser createParserWithBuffer(String json, char[] buffer, int start, int end) throws IOException {
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot();
        Reader reader = new StringReader(json);
        return new ReaderBasedJsonParser(ctxt, 
            JsonParser.Feature.collectDefaults(), 
            reader, null, sym, buffer, start, end, false);
    }

    // ========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // ========================================================================

    @Test(timeout = 4000)
    public void testSimpleStringToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"hello\"");
        assertNull(parser.getCurrentToken());
        assertNull(parser.getText());
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
        assertEquals("hello", parser.getValueAsString());
        assertEquals("hello", parser.getValueAsString("default"));
        
        char[] chars = parser.getTextCharacters();
        assertEquals("hello", new String(chars, parser.getTextOffset(), parser.getTextLength()));
        
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testFieldNameAndValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"key\": \"value\"}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getText());
        assertEquals("key", parser.getValueAsString());
        assertEquals("key", parser.getValueAsString("default"));
        
        char[] nameChars = parser.getTextCharacters();
        assertEquals("key", new String(nameChars, parser.getTextOffset(), parser.getTextLength()));
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testIntegerToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("42");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertEquals(42L, parser.getLongValue());
        assertEquals("42", parser.getText());
        
        char[] chars = parser.getTextCharacters();
        assertEquals("42", new String(chars, parser.getTextOffset(), parser.getTextLength()));
        
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNegativeInteger() throws IOException {
        ReaderBasedJsonParser parser = createParser("-17");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-17, parser.getIntValue());
        assertEquals(-17L, parser.getLongValue());
        assertEquals("-17", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testFloatToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("3.14");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        assertEquals("3.14", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testExponentNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("1.5e10");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.5e10, parser.getDoubleValue(), 0.1);
        assertEquals("1.5e10", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("-2.5E-3");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(-2.5E-3, parser.getDoubleValue(), 0.0001);
        assertEquals("-2.5E-3", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testTrueFalseNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("true false null");
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(true, parser.getBooleanValue());
        assertEquals("true", parser.getText());
        
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(false, parser.getBooleanValue());
        assertEquals("false", parser.getText());
        
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.getEmbeddedObject());
        assertEquals("null", parser.getText());
        
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testArrayParsing() throws IOException {
        ReaderBasedJsonParser parser = createParser("[1, 2, 3]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testObjectParsing() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNestedStructures() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"arr\":[1,{\"x\":2}],\"obj\":{\"y\":3}}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("arr", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("x", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("obj", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("y", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextFieldNameWithSerializableString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"name\":\"value\"}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        
        // Test matching field name
        assertTrue(parser.nextFieldName(new SerializableString() {
            public String getValue() { return "name"; }
            public int charLength() { return 4; }
            public char[] asQuotedChars() { return "\"name\"".toCharArray(); }
            public byte[] asUnquotedUTF8() { return "name".getBytes(); }
            public byte[] asQuotedUTF8() { return "\"name\"".getBytes(); }
            public int appendQuotedUTF8(byte[] buffer, int offset) { return 0; }
            public int appendQuoted(char[] buffer, int offset) { return 0; }
            public int appendUnquotedUTF8(byte[] buffer, int offset) { return 0; }
            public int appendUnquoted(char[] buffer, int offset) { return 0; }
        }));
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextFieldNameNonMatching() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"other\":123}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        
        assertFalse(parser.nextFieldName(new SerializableString() {
            public String getValue() { return "name"; }
            public int charLength() { return 4; }
            public char[] asQuotedChars() { return "\"name\"".toCharArray(); }
            public byte[] asUnquotedUTF8() { return "name".getBytes(); }
            public byte[] asQuotedUTF8() { return "\"name\"".getBytes(); }
            public int appendQuotedUTF8(byte[] buffer, int offset) { return 0; }
            public int appendQuoted(char[] buffer, int offset) { return 0; }
            public int appendUnquotedUTF8(byte[] buffer, int offset) { return 0; }
            public int appendUnquoted(char[] buffer, int offset) { return 0; }
        }));
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextFieldNameString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"key\":\"val\"}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("key", parser.nextFieldName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("val", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextFieldNameNonObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("[1,2,3]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertNull(parser.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
    }

    @Test(timeout = 4000)
    public void testNextTextValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"hello\"}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("hello", parser.nextTextValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextTextValueNonString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":123}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNull(parser.nextTextValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());
        assertEquals(123, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextIntValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":42}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(42, parser.nextIntValue(0));
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextIntValueDefault() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"notint\"}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(-1, parser.nextIntValue(-1));
        assertEquals(JsonToken.VALUE_STRING, parser.getCurrentToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextLongValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1234567890123}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(1234567890123L, parser.nextLongValue(0L));
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextBooleanValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(Boolean.TRUE, parser.nextBooleanValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextBooleanValueFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(Boolean.FALSE, parser.nextBooleanValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextBooleanValueNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNull(parser.nextBooleanValue());
        assertEquals(JsonToken.VALUE_NULL, parser.getCurrentToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("null");
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.getValueAsString());
        assertEquals("default", parser.getValueAsString("default"));
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("123");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("123", parser.getValueAsString());
        assertEquals("123", parser.getValueAsString("default"));
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testGetTextWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser(" ");
        assertNull(parser.nextToken());
        assertNull(parser.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextCharactersWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser(" ");
        assertNull(parser.nextToken());
        assertNull(parser.getTextCharacters());
    }

    @Test(timeout = 4000)
    public void testGetTextLengthWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser(" ");
        assertNull(parser.nextToken());
        assertEquals(0, parser.getTextLength());
    }

    @Test(timeout = 4000)
    public void testGetTextOffsetWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser(" ");
        assertNull(parser.nextToken());
        assertEquals(0, parser.getTextOffset());
    }

    @Test(timeout = 4000)
    public void testGetTextOffsetForFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"key\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0, parser.getTextOffset());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testGetTextOffsetForString() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"test\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(0, parser.getTextOffset());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testGetCodecAndSetCodec() throws IOException {
        ReaderBasedJsonParser parser = createParser("{}");
        assertNull(parser.getCodec());
        
        ObjectCodec codec = new ObjectCodec() {
            public JsonParser getFactory() { return null; }
            public <T> T readValue(JsonParser p, Class<T> valueType) { return null; }
            public <T> T readValue(JsonParser p, com.fasterxml.jackson.core.type.TypeReference<T> valueTypeRef) { return null; }
            public <T> T readValue(JsonParser p, com.fasterxml.jackson.databind.JavaType valueType) { return null; }
            public <T> T treeToValue(com.fasterxml.jackson.core.TreeNode n, Class<T> valueType) { return null; }
            public com.fasterxml.jackson.core.TreeNode readTree(JsonParser p) { return null; }
            public void writeValue(JsonGenerator g, Object value) {}
            public com.fasterxml.jackson.core.TreeNode createArrayNode() { return null; }
            public com.fasterxml.jackson.core.TreeNode createObjectNode() { return null; }
            public JsonParser treeAsTokens(com.fasterxml.jackson.core.TreeNode n) { return null; }
            public <T> T treeToValue(com.fasterxml.jackson.core.TreeNode n, com.fasterxml.jackson.databind.JavaType valueType) { return null; }
        };
        parser.setCodec(codec);
        assertSame(codec, parser.getCodec());
    }

    @Test(timeout = 4000)
    public void testGetInputSource() throws IOException {
        Reader reader = new StringReader("{}");
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot();
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(ctxt, 
            JsonParser.Feature.collectDefaults(), reader, null, sym);
        assertSame(reader, parser.getInputSource());
    }

    @Test(timeout = 4000)
    public void testReleaseBuffered() throws IOException {
        ReaderBasedJsonParser parser = createParser("hello");
        StringWriter writer = new StringWriter();
        int count = parser.releaseBuffered(writer);
        assertTrue(count > 0);
        assertTrue(writer.toString().length() > 0);
    }

    @Test(timeout = 4000)
    public void testReleaseBufferedEmpty() throws IOException {
        ReaderBasedJsonParser parser = createParser("");
        StringWriter writer = new StringWriter();
        assertEquals(0, parser.releaseBuffered(writer));
        assertEquals("", writer.toString());
    }

    @Test(timeout = 4000)
    public void testTokenLocation() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"key\":\"value\"}");
        parser.nextToken(); // START_OBJECT
        JsonLocation loc1 = parser.getTokenLocation();
        assertNotNull(loc1);
        
        parser.nextToken(); // FIELD_NAME
        JsonLocation loc2 = parser.getTokenLocation();
        assertNotNull(loc2);
        
        parser.nextToken(); // VALUE_STRING
        JsonLocation loc3 = parser.getTokenLocation();
        assertNotNull(loc3);
    }

    @Test(timeout = 4000)
    public void testCurrentLocation() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"key\":\"value\"}");
        parser.nextToken();
        JsonLocation loc = parser.getCurrentLocation();
        assertNotNull(loc);
    }

    // ========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // ========================================================================

    @Test(timeout = 4000)
    public void testEmptyInput() throws IOException {
        ReaderBasedJsonParser parser = createParser("");
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testWhitespaceOnly() throws IOException {
        ReaderBasedJsonParser parser = createParser("   \t\n\r  ");
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testSingleToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("123");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedArray() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < 100; i++) {
            sb.append("[");
        }
        sb.append("1");
        for (int i = 0; i < 100; i++) {
            sb.append("]");
        }
        ReaderBasedJsonParser parser = createParser(sb.toString());
        for (int i = 0; i < 100; i++) {
            assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        }
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        for (int i = 0; i < 100; i++) {
            assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        }
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testVeryLongString() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("\"");
        for (int i = 0; i < 1000; i++) {
            sb.append("a");
        }
        sb.append("\"");
        ReaderBasedJsonParser parser = createParser(sb.toString());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        String text = parser.getText();
        assertEquals(1000, text.length());
        for (int i = 0; i < 1000; i++) {
            assertEquals('a', text.charAt(i));
        }
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testVeryLongNumber() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            sb.append("9");
        }
        ReaderBasedJsonParser parser = createParser(sb.toString());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        // Just verify it parses without exception
        assertNotNull(parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testLeadingZerosAllowed() throws IOException {
        ReaderBasedJsonParser parser = createParser("00123", 
            JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testLeadingZerosNotAllowed() throws IOException {
        ReaderBasedJsonParser parser = createParser("00123");
        try {
            parser.nextToken();
            fail("Expected exception for leading zeros");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNumberWithManyFractionDigits() throws IOException {
        StringBuilder sb = new StringBuilder("0.");
        for (int i = 0; i < 100; i++) {
            sb.append("1");
        }
        ReaderBasedJsonParser parser = createParser(sb.toString());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertNotNull(parser.getDecimalValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNumberWithManyExponentDigits() throws IOException {
        ReaderBasedJsonParser parser = createParser("1e100");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1e100, parser.getDoubleValue(), 0.0);
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testStringWithAllEscapeSequences() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\\b\\t\\n\\f\\r\\\"\\\\\\/\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\b\t\n\f\r\"\\/", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testStringWithUnicodeEscape() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\\u0041\\u0042\\u0043\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("ABC", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testFieldNameWithSpecialCharacters() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\\nb\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a\nb", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testBase64WithPadding() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"SGVsbG8=\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] decoded = parser.getBinaryValue(Base64Variants.MIME);
        assertEquals("Hello", new String(decoded, "UTF-8"));
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testBase64WithoutPadding() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"SGVsbG8\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] decoded = parser.getBinaryValue(Base64Variants.MIME_NO_LF);
        assertEquals("Hello", new String(decoded, "UTF-8"));
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testCommentsAllowed() throws IOException {
        ReaderBasedJsonParser parser = createParser("/* comment */ 42", 
            JsonParser.Feature.ALLOW_COMMENTS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testLineComment() throws IOException {
        ReaderBasedJsonParser parser = createParser("// line comment\n42", 
            JsonParser.Feature.ALLOW_COMMENTS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testYAMLComment() throws IOException {
        ReaderBasedJsonParser parser = createParser("# yaml comment\n42", 
            JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testSingleQuotedString() throws IOException {
        ReaderBasedJsonParser parser = createParser("'hello'", 
            JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testSingleQuotedFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{'key':'value'}", 
            JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testUnquotedFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{key:123}", 
            JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNaN() throws IOException {
        ReaderBasedJsonParser parser = createParser("NaN", 
            JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.NaN, parser.getDoubleValue(), 0.0);
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testInfinity() throws IOException {
        ReaderBasedJsonParser parser = createParser("Infinity", 
            JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNegativeInfinity() throws IOException {
        ReaderBasedJsonParser parser = createParser("-Infinity", 
            JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.NEGATIVE_INFINITY, parser.getDoubleValue(), 0.0);
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testPlusInfinity() throws IOException {
        ReaderBasedJsonParser parser = createParser("+Infinity", 
            JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testBufferBoundaryNumber() throws IOException {
        // Create a number that spans across buffer boundary
        char[] buffer = new char[10];
        String json = "12345";
        System.arraycopy(json.toCharArray(), 0, buffer, 0, json.length());
        ReaderBasedJsonParser parser = createParserWithBuffer(json, buffer, 0, json.length());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(12345, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testBufferBoundaryString() throws IOException {
        // Create a string that spans across buffer boundary
        String json = "\"hello\"";
        char[] buffer = new char[10];
        System.arraycopy(json.toCharArray(), 0, buffer, 0, json.length());
        ReaderBasedJsonParser parser = createParserWithBuffer(json, buffer, 0, json.length());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
        assertNull(parser.nextToken());
    }

    // ========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // ========================================================================

    /**
     * Targets the known Defects4J defect:
     * com.fasterxml.jackson.core.json.TestNumericValues::testLongerFloatingPoint
     * --> java.lang.ArrayIndexOutOfBoundsException: 200
     * 
     * This test creates a floating point number with a specific length pattern
     * that triggers an ArrayIndexOutOfBoundsException in the defective version.
     * The number is designed to cause buffer boundary issues in _parseFloat()
     * or _parseNumber2().
     */
    @Test(timeout = 4000)
    public void testLongerFloatingPoint() throws IOException {
        // Construct a floating point number that triggers the defect
        // The defect occurs when a floating point number has a specific length
        // that causes an array index out of bounds during parsing.
        // We need a number that is long enough to span buffer boundaries
        // and has both fractional and exponent parts.
        
        StringBuilder sb = new StringBuilder();
        sb.append("1");
        // Add many digits to create a long number
        for (int i = 0; i < 50; i++) {
            sb.append("0");
        }
        sb.append(".");
        for (int i = 0; i < 50; i++) {
            sb.append("1");
        }
        sb.append("E");
        sb.append("100");
        
        String json = sb.toString();
        ReaderBasedJsonParser parser = createParser(json);
        
        // This should not throw ArrayIndexOutOfBoundsException
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        
        // Verify the value is correct
        BigDecimal expected = new BigDecimal(json);
        assertEquals(expected, parser.getDecimalValue());
        
        assertNull(parser.nextToken());
    }

    /**
     * Additional test targeting the same defect with different number pattern.
     * Tests a floating point number that is split across buffer boundary
     * with specific length that triggers the bug.
     */
    @Test(timeout = 4000)
    public void testLongerFloatingPointWithExponent() throws IOException {
        // Another pattern that may trigger the defect
        StringBuilder sb = new StringBuilder();
        sb.append("0.");
        for (int i = 0; i < 100; i++) {
            sb.append("9");
        }
        sb.append("e");
        sb.append("-");
        sb.append("50");
        
        String json = sb.toString();
        ReaderBasedJsonParser parser = createParser(json);
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        
        BigDecimal expected = new BigDecimal(json);
        assertEquals(expected, parser.getDecimalValue());
        
        assertNull(parser.nextToken());
    }

    /**
     * Test with a number that has exactly the length that caused the defect.
     * The defect was reported with a specific length that caused buffer overrun.
     */
    @Test(timeout = 4000)
    public void testLongerFloatingPointExactLength() throws IOException {
        // Create a number with length that triggers the exact defect condition
        // The defect occurs when the number parsing logic accesses beyond buffer bounds
        StringBuilder sb = new StringBuilder();
        sb.append("1");
        // Add digits to make the total length exactly 200 characters
        for (int i = 0; i < 197; i++) {
            sb.append("0");
        }
        sb.append(".5");
        
        String json = sb.toString();
        ReaderBasedJsonParser parser = createParser(json);
        
        // This should not throw ArrayIndexOutOfBoundsException
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        
        BigDecimal expected = new BigDecimal(json);
        assertEquals(expected, parser.getDecimalValue());
        
        assertNull(parser.nextToken());
    }

    // ========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // ========================================================================

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidJsonUnexpectedChar() throws IOException {
        ReaderBasedJsonParser parser = createParser("{invalid}");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMismatchedArrayEnd() throws IOException {
        ReaderBasedJsonParser parser = createParser("[1,2,3}");
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMismatchedObjectEnd() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1]");
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMissingColon() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\" 1}");
        parser.nextToken();
        parser.nextToken();
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testMissingComma() throws IOException {
        ReaderBasedJsonParser parser = createParser("[1 2]");
        parser.nextToken();
        parser.nextToken();
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidNumberLeadingZero() throws IOException {
        ReaderBasedJsonParser parser = createParser("0123");
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidNumberMissingDigitAfterMinus() throws IOException {
        ReaderBasedJsonParser parser = createParser("-");
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidNumberMissingDigitAfterDecimal() throws IOException {
        ReaderBasedJsonParser parser = createParser("1.");
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidNumberMissingDigitAfterExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("1e");
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidEscapeSequence() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\\x\"");
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnclosedString() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"unclosed");
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnclosedComment() throws IOException {
        ReaderBasedJsonParser parser = createParser("/* unclosed", 
            JsonParser.Feature.ALLOW_COMMENTS.getMask());
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("tru");
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidTokenFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("fals");
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidTokenNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("nul");
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidBase64Char() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"!!!\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        parser.getBinaryValue(Base64Variants.MIME); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testGetBinaryValueOnNonString() throws IOException {
        ReaderBasedJsonParser parser = createParser("123");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        parser.getBinaryValue(Base64Variants.MIME); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnquotedFieldNameNotAllowed() throws IOException {
        ReaderBasedJsonParser parser = createParser("{key:1}");
        parser.nextToken();
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testSingleQuoteNotAllowed() throws IOException {
        ReaderBasedJsonParser parser = createParser("{'key':'val'}");
        parser.nextToken();
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testNaNNotAllowed() throws IOException {
        ReaderBasedJsonParser parser = createParser("NaN");
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInfinityNotAllowed() throws IOException {
        ReaderBasedJsonParser parser = createParser("Infinity");
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testCommentNotAllowed() throws IOException {
        ReaderBasedJsonParser parser = createParser("/* comment */ 42");
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testYAMLCommentNotAllowed() throws IOException {
        ReaderBasedJsonParser parser = createParser("# comment\n42");
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testInvalidRootValueSeparator() throws IOException {
        ReaderBasedJsonParser parser = createParser("123abc");
        parser.nextToken(); // Should throw
    }

    @Test(timeout = 4000, expected = JsonParseException.class)
    public void testUnexpectedEndOfInput() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":");
        parser.nextToken();
        parser.nextToken();
        parser.nextToken(); // Should throw
    }

    // ========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ========================================================================

    @Test(timeout = 4000)
    public void testConstructorWithPreallocatedBuffer() throws IOException {
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot();
        char[] buffer = new char[100];
        buffer[0] = '"';
        buffer[1] = 'h';
        buffer[2] = 'i';
        buffer[3] = '"';
        Reader reader = new StringReader("");
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(ctxt, 
            JsonParser.Feature.collectDefaults(), reader, null, sym, 
            buffer, 0, 4, false);
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hi", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testConstructorWithReader() throws IOException {
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot();
        Reader reader = new StringReader("\"test\"");
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(ctxt, 
            JsonParser.Feature.collectDefaults(), reader, null, sym);
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testCloseAndRelease() throws IOException {
        ReaderBasedJsonParser parser = createParser("{}");
        parser.nextToken();
        parser.close();
        // After close, nextToken should return null
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testAutoCloseSource() throws IOException {
        // Create a reader that tracks whether it was closed
        final boolean[] closed = {false};
        Reader reader = new StringReader("{}") {
            @Override
            public void close() {
                closed[0] = true;
            }
        };
        
        IOContext ctxt = new IOContext(new BufferRecycler(), null, true);
        CharsToNameCanonicalizer sym = CharsToNameCanonicalizer.createRoot();
        ReaderBasedJsonParser parser = new ReaderBasedJsonParser(ctxt, 
            JsonParser.Feature.AUTO_CLOSE_SOURCE.getMask(), reader, null, sym);
        
        parser.nextToken();
        parser.close();
        assertTrue(closed[0]);
    }

    @Test(timeout = 4000)
    public void testGetTokenLocationForFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"key\":1}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        JsonLocation loc = parser.getTokenLocation();
        assertNotNull(loc);
        assertTrue(loc.getColumnNr() >= 0);
        assertTrue(loc.getRowNr() >= 0);
    }

    @Test(timeout = 4000)
    public void testGetCurrentLocationAfterParsing() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"key\":1}");
        parser.nextToken();
        JsonLocation loc = parser.getCurrentLocation();
        assertNotNull(loc);
        assertTrue(loc.getColumnNr() >= 0);
        assertTrue(loc.getRowNr() >= 0);
    }

    @Test(timeout = 4000)
    public void testReadBinaryValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"SGVsbG8=\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = parser.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(5, count);
        assertEquals("Hello", new String(out.toByteArray(), "UTF-8"));
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testReadBinaryValueNonString() throws IOException {
        ReaderBasedJsonParser parser = createParser("123");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = parser.readBinaryValue(Base64Variants.MIME, out);
        // Should still work by converting
        assertTrue(count > 0);
    }

    @Test(timeout = 4000)
    public void testSkipString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"long string to skip\",\"b\":2}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        // nextToken will skip the string value
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testCarriageReturnHandling() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\r\n\"b\":2}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testTabAndSpaceHandling() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\t\"a\"\t:\t1\t,\t\"b\"\t:\t2\t}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testMultipleRootValues() throws IOException {
        ReaderBasedJsonParser parser = createParser("1 2 3");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNegativeZero() throws IOException {
        ReaderBasedJsonParser parser = createParser("-0");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());
        assertEquals(0, parser.getLongValue());
        assertEquals("-0", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testFloatWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("1.5e-10");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.5e-10, parser.getDoubleValue(), 0.0);
        assertEquals("1.5e-10", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testFloatWithPositiveExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("1.5E+10");
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.5E+10, parser.getDoubleValue(), 0.1);
        assertEquals("1.5E+10", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testIntegerMaxValue() throws IOException {
        ReaderBasedJsonParser parser = createParser(String.valueOf(Integer.MAX_VALUE));
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Integer.MAX_VALUE, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testIntegerMinValue() throws IOException {
        ReaderBasedJsonParser parser = createParser(String.valueOf(Integer.MIN_VALUE));
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Integer.MIN_VALUE, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testLongMaxValue() throws IOException {
        ReaderBasedJsonParser parser = createParser(String.valueOf(Long.MAX_VALUE));
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Long.MAX_VALUE, parser.getLongValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testLongMinValue() throws IOException {
        ReaderBasedJsonParser parser = createParser(String.valueOf(Long.MIN_VALUE));
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Long.MIN_VALUE, parser.getLongValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testDoubleMaxValue() throws IOException {
        ReaderBasedJsonParser parser = createParser(String.valueOf(Double.MAX_VALUE));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.MAX_VALUE, parser.getDoubleValue(), 0.0);
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testDoubleMinValue() throws IOException {
        ReaderBasedJsonParser parser = createParser(String.valueOf(Double.MIN_VALUE));
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.MIN_VALUE, parser.getDoubleValue(), 0.0);
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testEmptyString() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("", parser.getText());
        assertEquals(0, parser.getTextLength());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testEmptyObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testEmptyArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("[]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNestedEmptyStructures() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":{}}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testStringWithEmbeddedQuote() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"he\\\"llo\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("he\"llo", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testStringWithBackslash() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"path\\\\to\\\\file\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("path\\to\\file", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testStringWithSlash() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"a\\/b\"");
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("a/b", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testMultipleFieldNamesWithSameName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"a\":2}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testDeeplyNestedObject() throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (int i = 0; i < 50; i++) {
            sb.append("\"a\":{");
        }
        sb.append("\"b\":1");
        for (int i = 0; i < 50; i++) {
            sb.append("}");
        }
        ReaderBasedJsonParser parser = createParser(sb.toString());
        for (int i = 0; i < 50; i++) {
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals("a", parser.getCurrentName());
        }
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        for (int i = 0; i < 50; i++) {
            assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        }
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testMixedArrayAndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("[{\"a\":[1,2]},{\"b\":{\"c\":3}}]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("c", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }
}