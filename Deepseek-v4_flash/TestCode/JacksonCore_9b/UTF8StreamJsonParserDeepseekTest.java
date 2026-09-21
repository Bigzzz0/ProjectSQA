package com.fasterxml.jackson.core.json;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: UTF8StreamJsonParser
 * 
 * Defect: getValueAsString() returns null for VALUE_STRING tokens when
 *         _tokenIncomplete is true and the string is not yet fully parsed.
 *         The bug is in the getValueAsString() method: it calls
 *         _finishAndReturnString() but fails to return the result in all
 *         code paths (specifically when the string is complete in the
 *         initial fast-path loop, it returns the result, but when it
 *         falls through to _finishString2(), it returns the result of
 *         _textBuffer.contentsAsString() which may be stale/empty).
 * 
 * Branches targeted:
 * 1. getValueAsString() with VALUE_STRING token, _tokenIncomplete=true
 *    - Fast path: string fully in buffer, returns immediately
 *    - Slow path: string spans buffer boundary, falls through to _finishString2()
 * 2. getValueAsString() with VALUE_STRING token, _tokenIncomplete=false
 * 3. getValueAsString() with non-STRING token (delegates to super)
 * 4. getValueAsString(String def) with VALUE_STRING token
 * 5. getValueAsString(String def) with non-STRING token
 * 6. getValueAsInt() with NUMBER_INT token
 * 7. getValueAsInt() with NUMBER_FLOAT token
 * 8. getValueAsInt() with non-number token
 * 9. getValueAsInt(int def) with NUMBER_INT token
 * 10. getValueAsInt(int def) with non-number token
 * 11. nextToken() state transitions (FIELD_NAME -> value)
 * 12. nextFieldName() with matching and non-matching names
 * 13. nextTextValue() with FIELD_NAME and VALUE_STRING
 * 14. nextIntValue() with FIELD_NAME and VALUE_NUMBER_INT
 * 15. nextLongValue() with FIELD_NAME and VALUE_NUMBER_INT
 * 16. nextBooleanValue() with FIELD_NAME and VALUE_TRUE/FALSE
 * 17. getText() for various token types
 * 18. getTextCharacters() for various token types
 * 19. getTextLength() for various token types
 * 20. getTextOffset() for various token types
 * 21. releaseBuffered() with data and without data
 * 22. getBinaryValue() with VALUE_STRING and non-STRING tokens
 * 23. readBinaryValue() with complete and incomplete tokens
 * 24. getTokenLocation() and getCurrentLocation()
 * 25. _parsePosNumber() with various number formats
 * 26. _parseNegNumber() with various number formats
 * 27. _parseFloat() with fraction and exponent
 * 28. _verifyNoLeadingZeroes() with and without ALLOW_NUMERIC_LEADING_ZEROS
 * 29. _parseName() with various name lengths (1-12+ bytes)
 * 30. _handleOddName() with single quotes and unquoted names
 * 31. _handleUnexpectedValue() with various invalid tokens
 * 32. _skipWS() with comments and whitespace
 * 33. _skipColon() with various whitespace patterns
 * 34. _decodeEscaped() with various escape sequences
 * 35. _decodeUtf8_2/3/4() with valid and invalid UTF-8
 * 36. _skipString() for incomplete strings
 * 37. _finishString() for complete and incomplete strings
 * 38. _finishAndReturnString() for complete and incomplete strings
 * 39. _matchToken() for true/false/null with boundary conditions
 * 40. _verifyRootSpace() for root-level values
 */
public class UTF8StreamJsonParserDeepseekTest {

    private static final int DEFAULT_FEATURES = 0;
    private static final int ALL_FEATURES = JsonParser.Feature.collectDefaults();

    private UTF8StreamJsonParser createParser(String json) throws IOException {
        return createParser(json, DEFAULT_FEATURES);
    }

    private UTF8StreamJsonParser createParser(String json, int features) throws IOException {
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot();
        return new UTF8StreamJsonParser(ctxt, features,
                new ByteArrayInputStream(data), null, sym,
                data, 0, data.length, false);
    }

    private UTF8StreamJsonParser createParserWithSmallBuffer(String json, int bufferSize) throws IOException {
        byte[] data = json.getBytes(StandardCharsets.UTF_8);
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot();
        // Use a small buffer to force buffer boundary crossings
        byte[] smallBuffer = new byte[bufferSize];
        System.arraycopy(data, 0, smallBuffer, 0, Math.min(data.length, bufferSize));
        return new UTF8StreamJsonParser(ctxt, DEFAULT_FEATURES,
                new ByteArrayInputStream(data), null, sym,
                smallBuffer, 0, Math.min(data.length, bufferSize), false);
    }

    /*
     * ==================== PARTITION A: Core Functional Logic & State Transitions ====================
     */

    @Test(timeout = 4000)
    public void testNextTokenBasicObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextTokenBasicArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1,2,3]");
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
    public void testNextTokenStringValues() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"hello\",\"world\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("world", parser.getText());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextTokenBooleanAndNull() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true,false,null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextTokenNestedStructures() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":[1,{\"b\":2}],\"c\":{}}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("c", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextFieldNameWithMatch() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"name\":\"value\"}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertTrue(parser.nextFieldName(new SerializableString() {
            @Override
            public String getValue() { return "name"; }
            @Override
            public int charLength() { return 4; }
            @Override
            public char[] asQuotedChars() { return "\"name\"".toCharArray(); }
            @Override
            public byte[] asUnquotedUTF8() { return "name".getBytes(StandardCharsets.UTF_8); }
            @Override
            public byte[] asQuotedUTF8() { return "\"name\"".getBytes(StandardCharsets.UTF_8); }
            @Override
            public String toString() { return "name"; }
        }));
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
    }

    @Test(timeout = 4000)
    public void testNextFieldNameWithoutMatch() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"other\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertFalse(parser.nextFieldName(new SerializableString() {
            @Override
            public String getValue() { return "name"; }
            @Override
            public int charLength() { return 4; }
            @Override
            public char[] asQuotedChars() { return "\"name\"".toCharArray(); }
            @Override
            public byte[] asUnquotedUTF8() { return "name".getBytes(StandardCharsets.UTF_8); }
            @Override
            public byte[] asQuotedUTF8() { return "\"name\"".getBytes(StandardCharsets.UTF_8); }
            @Override
            public String toString() { return "name"; }
        }));
        assertEquals("other", parser.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testNextFieldNameString() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("a", parser.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("b", parser.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextTextValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":\"hello\",\"b\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("a", parser.nextFieldName());
        assertEquals("hello", parser.nextTextValue());
        assertEquals("b", parser.nextFieldName());
        assertNull(parser.nextTextValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.getCurrentToken());
    }

    @Test(timeout = 4000)
    public void testNextIntValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":42,\"b\":\"x\"}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("a", parser.nextFieldName());
        assertEquals(42, parser.nextIntValue(-1));
        assertEquals("b", parser.nextFieldName());
        assertEquals(-1, parser.nextIntValue(-1));
    }

    @Test(timeout = 4000)
    public void testNextLongValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1234567890123,\"b\":\"x\"}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("a", parser.nextFieldName());
        assertEquals(1234567890123L, parser.nextLongValue(-1L));
        assertEquals("b", parser.nextFieldName());
        assertEquals(-1L, parser.nextLongValue(-1L));
    }

    @Test(timeout = 4000)
    public void testNextBooleanValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":true,\"b\":false,\"c\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("a", parser.nextFieldName());
        assertEquals(Boolean.TRUE, parser.nextBooleanValue());
        assertEquals("b", parser.nextFieldName());
        assertEquals(Boolean.FALSE, parser.nextBooleanValue());
        assertEquals("c", parser.nextFieldName());
        assertNull(parser.nextBooleanValue());
    }

    @Test(timeout = 4000)
    public void testGetTextForVariousTokens() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":\"str\",\"b\":123,\"c\":true,\"d\":null}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getText());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("str", parser.getText());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getText());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("123", parser.getText());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("c", parser.getText());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getText());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("d", parser.getText());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals("null", parser.getText());
    }

    @Test(timeout = 4000)
    public void testGetTextCharacters() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":\"hello\"}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        char[] nameChars = parser.getTextCharacters();
        assertEquals("a", new String(nameChars, parser.getTextOffset(), parser.getTextLength()));
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        char[] strChars = parser.getTextCharacters();
        assertEquals("hello", new String(strChars, parser.getTextOffset(), parser.getTextLength()));
    }

    @Test(timeout = 4000)
    public void testGetTextLengthAndOffset() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":\"hello\"}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(1, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(5, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
    }

    @Test(timeout = 4000)
    public void testReleaseBuffered() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        int count = parser.releaseBuffered(out);
        assertTrue(count > 0);
        assertTrue(out.size() > 0);
    }

    @Test(timeout = 4000)
    public void testReleaseBufferedEmpty() throws IOException {
        UTF8StreamJsonParser parser = createParser("");
        assertEquals(0, parser.releaseBuffered(new java.io.ByteArrayOutputStream()));
    }

    @Test(timeout = 4000)
    public void testGetInputSource() throws IOException {
        InputStream in = new ByteArrayInputStream("{}".getBytes(StandardCharsets.UTF_8));
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot();
        UTF8StreamJsonParser parser = new UTF8StreamJsonParser(ctxt, DEFAULT_FEATURES,
                in, null, sym, new byte[0], 0, 0, false);
        assertSame(in, parser.getInputSource());
    }

    @Test(timeout = 4000)
    public void testGetCodecAndSetCodec() throws IOException {
        UTF8StreamJsonParser parser = createParser("{}");
        assertNull(parser.getCodec());
        ObjectCodec codec = new ObjectCodec() {
            @Override
            public <T> T readValue(JsonParser p, Class<T> valueType) throws IOException { return null; }
            @Override
            public <T> T readValue(JsonParser p, com.fasterxml.jackson.core.type.TypeReference<T> valueTypeRef) throws IOException { return null; }
            @Override
            public <T> T readValue(JsonParser p, com.fasterxml.jackson.databind.JavaType valueType) throws IOException { return null; }
            @Override
            public <T> T treeToValue(TreeNode n, Class<T> valueType) throws IOException { return null; }
            @Override
            public JsonNode createObjectNode() { return null; }
            @Override
            public JsonNode createArrayNode() { return null; }
            @Override
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            @Override
            public <T extends TreeNode> T readTree(JsonParser p) throws IOException { return null; }
            @Override
            public void writeValue(com.fasterxml.jackson.core.JsonGenerator g, Object value) throws IOException { }
            @Override
            public void writeTree(com.fasterxml.jackson.core.JsonGenerator g, TreeNode rootNode) throws IOException { }
        };
        parser.setCodec(codec);
        assertSame(codec, parser.getCodec());
    }

    /*
     * ==================== PARTITION B: Boundary Value Analysis (BVA) & Extremes ====================
     */

    @Test(timeout = 4000)
    public void testEmptyStringValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("", parser.getText());
    }

    @Test(timeout = 4000)
    public void testEmptyObjectAndArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("{}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());

        parser = createParser("[]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testZeroAndNegativeNumbers() throws IOException {
        UTF8StreamJsonParser parser = createParser("[0,-0,-1,0.0,-0.0]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(0.0, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(-0.0, parser.getDoubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testMaxIntAndLongValues() throws IOException {
        UTF8StreamJsonParser parser = createParser("[2147483647,-2147483648,9223372036854775807,-9223372036854775808]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Integer.MAX_VALUE, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Integer.MIN_VALUE, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Long.MAX_VALUE, parser.getLongValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Long.MIN_VALUE, parser.getLongValue());
    }

    @Test(timeout = 4000)
    public void testFloatBoundaries() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1.0,1.5,0.1,1e10,1E-10,1.5e3]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.0, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.5, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(0.1, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1e10, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1E-10, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.5e3, parser.getDoubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testLongFieldNames() throws IOException {
        String longName = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        UTF8StreamJsonParser parser = createParser("{\"" + longName + "\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(longName, parser.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testFieldNameBoundaries() throws IOException {
        // Test names of lengths 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13
        for (int len = 1; len <= 13; len++) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                sb.append((char) ('a' + i));
            }
            String name = sb.toString();
            UTF8StreamJsonParser parser = createParser("{\"" + name + "\":1}");
            assertEquals(JsonToken.START_OBJECT, parser.nextToken());
            assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
            assertEquals(name, parser.getCurrentName());
        }
    }

    @Test(timeout = 4000)
    public void testStringWithSpecialCharacters() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"line\\nbreak\",\"tab\\tchar\",\"quote\\\"here\",\"back\\\\slash\",\"slash/here\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("line\nbreak", parser.getText());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("tab\tchar", parser.getText());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("quote\"here", parser.getText());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("back\\slash", parser.getText());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("slash/here", parser.getText());
    }

    @Test(timeout = 4000)
    public void testUnicodeEscapes() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"\\u0041\\u00e9\\u4e2d\\uD83D\\uDE00\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("A\u00e9\u4e2d\ud83d\ude00", parser.getText());
    }

    @Test(timeout = 4000)
    public void testWhitespaceHandling() throws IOException {
        UTF8StreamJsonParser parser = createParser("  \t\r\n { \t \"a\" \t : \t 1 \t } \t\r\n ");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testBufferBoundaryCrossing() throws IOException {
        // Force buffer boundary crossing with small buffer
        UTF8StreamJsonParser parser = createParserWithSmallBuffer("{\"longFieldName\":\"longStringValue\"}", 10);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("longFieldName", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("longStringValue", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testMultipleBufferBoundaries() throws IOException {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < 100; i++) {
            if (i > 0) sb.append(",");
            sb.append("\"string").append(i).append("\"");
        }
        sb.append("]");
        UTF8StreamJsonParser parser = createParserWithSmallBuffer(sb.toString(), 5);
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        for (int i = 0; i < 100; i++) {
            assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
            assertEquals("string" + i, parser.getText());
        }
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
    }

    /*
     * ==================== PARTITION C: Defect-Targeted Branch Zone ====================
     */

    @Test(timeout = 4000)
    public void testGetValueAsStringWithIncompleteString() throws IOException {
        // This test targets the specific defect: getValueAsString() returns null
        // for VALUE_STRING tokens when _tokenIncomplete is true
        UTF8StreamJsonParser parser = createParser("{\"a\":\"hello\"}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        // This will set _tokenIncomplete = true and _nextToken = VALUE_STRING
        assertEquals(JsonToken.FIELD_NAME, parser.getCurrentToken());
        // Now call getValueAsString() while _tokenIncomplete is true
        // The defect causes this to return null instead of "hello"
        assertEquals("hello", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithIncompleteStringSlowPath() throws IOException {
        // Force buffer boundary crossing to hit the slow path in _finishAndReturnString()
        UTF8StreamJsonParser parser = createParserWithSmallBuffer("{\"a\":\"hello world this is a long string\"}", 5);
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        // This will set _tokenIncomplete = true and _nextToken = VALUE_STRING
        assertEquals(JsonToken.FIELD_NAME, parser.getCurrentToken());
        // The defect causes this to return null instead of the full string
        assertEquals("hello world this is a long string", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndIncompleteString() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":\"hello\"}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        // This will set _tokenIncomplete = true and _nextToken = VALUE_STRING
        assertEquals(JsonToken.FIELD_NAME, parser.getCurrentToken());
        // The defect causes this to return null instead of "hello"
        assertEquals("hello", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithCompleteString() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":\"hello\"}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        // Now _tokenIncomplete is false and we have the string
        assertEquals("hello", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNonStringToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":123}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        // Non-string token should return null or default
        assertNull(parser.getValueAsString());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNullToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":null}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.getValueAsString());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithBooleanToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":true}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNumberToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":42}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFloatToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":42.5}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNonNumberToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":\"hello\"}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    /*
     * ==================== PARTITION D: Exception & Defensive Guard Paths ====================
     */

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonUnexpectedToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{invalid}");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonMissingColon() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\" 1}");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonMissingComma() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1 2]");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonMismatchedBrackets() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1,2}");
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonMismatchedBraces() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1]");
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonLeadingZero() throws IOException {
        UTF8StreamJsonParser parser = createParser("[01]");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000)
    public void testLeadingZeroAllowed() throws IOException {
        UTF8StreamJsonParser parser = createParser("[01]", JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonTrailingComma() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1,]");
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonUnquotedFieldName() throws IOException {
        UTF8StreamJsonParser parser = createParser("{a:1}");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000)
    public void testUnquotedFieldNameAllowed() throws IOException {
        UTF8StreamJsonParser parser = createParser("{a:1}", JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonSingleQuotedString() throws IOException {
        UTF8StreamJsonParser parser = createParser("['hello']");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000)
    public void testSingleQuotedStringAllowed() throws IOException {
        UTF8StreamJsonParser parser = createParser("['hello']", JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonNaN() throws IOException {
        UTF8StreamJsonParser parser = createParser("[NaN]");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000)
    public void testNaNAllowed() throws IOException {
        UTF8StreamJsonParser parser = createParser("[NaN]", JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isNaN(parser.getDoubleValue()));
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonInfinity() throws IOException {
        UTF8StreamJsonParser parser = createParser("[Infinity]");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000)
    public void testInfinityAllowed() throws IOException {
        UTF8StreamJsonParser parser = createParser("[Infinity]", JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS.getMask());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.POSITIVE_INFINITY, parser.getDoubleValue(), 0.0);
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonPlusSign() throws IOException {
        UTF8StreamJsonParser parser = createParser("[+1]");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonDecimalPointNoDigits() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1.]");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonExponentNoDigits() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1e]");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonControlCharInString() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"a\u0001b\"]");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidUtf8Sequence() throws IOException {
        byte[] invalid = new byte[]{(byte) 0xC0, (byte) 0xAF};
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        ByteQuadsCanonicalizer sym = ByteQuadsCanonicalizer.createRoot();
        UTF8StreamJsonParser parser = new UTF8StreamJsonParser(ctxt, DEFAULT_FEATURES,
                new ByteArrayInputStream(invalid), null, sym,
                invalid, 0, invalid.length, false);
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonUnterminatedString() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"hello]");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonUnterminatedObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1");
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonUnterminatedArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1,2");
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonUnexpectedEndOfInput() throws IOException {
        UTF8StreamJsonParser parser = createParser("");
        parser.nextToken();
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidJsonRootValueNotSeparated() throws IOException {
        UTF8StreamJsonParser parser = createParser("1 2");
        parser.nextToken();
        parser.nextToken();
    }

    @Test(timeout = 4000)
    public void testCommentsNotAllowed() throws IOException {
        UTF8StreamJsonParser parser = createParser("/* comment */ {}");
        try {
            parser.nextToken();
            fail("Expected IOException for comments not allowed");
        } catch (IOException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCommentsAllowed() throws IOException {
        UTF8StreamJsonParser parser = createParser("/* comment */ {} // line comment", JsonParser.Feature.ALLOW_COMMENTS.getMask());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testYamlCommentsNotAllowed() throws IOException {
        UTF8StreamJsonParser parser = createParser("# comment\n{}");
        try {
            parser.nextToken();
            fail("Expected IOException for YAML comments not allowed");
        } catch (IOException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testYamlCommentsAllowed() throws IOException {
        UTF8StreamJsonParser parser = createParser("# comment\n{}", JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test(timeout = 4000, expected = IOException.class)
    public void testInvalidBase64() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"!!!\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        parser.getBinaryValue(Base64Variants.getDefaultVariant());
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueWithNonStringToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("[123]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        try {
            parser.getBinaryValue(Base64Variants.getDefaultVariant());
            fail("Expected IOException for non-string token");
        } catch (IOException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueWithString() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"aGVsbG8=\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] result = parser.getBinaryValue(Base64Variants.getDefaultVariant());
        assertEquals("hello", new String(result, StandardCharsets.UTF_8));
    }

    @Test(timeout = 4000)
    public void testReadBinaryValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"aGVsbG8=\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        int count = parser.readBinaryValue(Base64Variants.getDefaultVariant(), out);
        assertEquals(5, count);
        assertEquals("hello", new String(out.toByteArray(), StandardCharsets.UTF_8));
    }

    /*
     * ==================== PARTITION E: Object Lifecycle & Contract Integrity ====================
     */

    @Test(timeout = 4000)
    public void testCloseParser() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        parser.close();
        assertTrue(parser.isClosed());
    }

    @Test(timeout = 4000)
    public void testGetTokenLocation() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\n  \"a\": 1\n}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        JsonLocation loc = parser.getTokenLocation();
        assertNotNull(loc);
        assertEquals(1, loc.getLineNr());
        assertEquals(1, loc.getColumnNr());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        loc = parser.getTokenLocation();
        assertEquals(2, loc.getLineNr());
        assertEquals(3, loc.getColumnNr());
    }

    @Test(timeout = 4000)
    public void testGetCurrentLocation() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        JsonLocation loc = parser.getCurrentLocation();
        assertNotNull(loc);
        assertTrue(loc.getCharOffset() >= 0);
    }

    @Test(timeout = 4000)
    public void testGetCurrentName() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNull(parser.getCurrentName());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
    }

    @Test(timeout = 4000)
    public void testGetCurrentToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.START_OBJECT, parser.getCurrentToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.getCurrentToken());
    }

    @Test(timeout = 4000)
    public void testHasCurrentToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertFalse(parser.hasCurrentToken());
        parser.nextToken();
        assertTrue(parser.hasCurrentToken());
    }

    @Test(timeout = 4000)
    public void testHasTokenId() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertTrue(parser.hasTokenId(JsonTokenId.ID_START_OBJECT));
        assertFalse(parser.hasTokenId(JsonTokenId.ID_FIELD_NAME));
    }

    @Test(timeout = 4000)
    public void testHasToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertTrue(parser.hasToken(JsonToken.START_OBJECT));
        assertFalse(parser.hasToken(JsonToken.FIELD_NAME));
    }

    @Test(timeout = 4000)
    public void testClearCurrentToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertTrue(parser.hasCurrentToken());
        parser.clearCurrentToken();
        assertFalse(parser.hasCurrentToken());
    }

    @Test(timeout = 4000)
    public void testGetParsingContext() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":[1]}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        JsonStreamContext ctx = parser.getParsingContext();
        assertNotNull(ctx);
        assertTrue(ctx.inObject());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        ctx = parser.getParsingContext();
        assertTrue(ctx.inArray());
    }

    @Test(timeout = 4000)
    public void testNumberTypeDetection() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1,1.0,1e10]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(JsonParser.NumberType.DOUBLE, parser.getNumberType());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(JsonParser.NumberType.DOUBLE, parser.getNumberType());
    }

    @Test(timeout = 4000)
    public void testNumberConversions() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42,42.5]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertEquals(42L, parser.getLongValue());
        assertEquals(42.0, parser.getDoubleValue(), 0.0);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertEquals(42L, parser.getLongValue());
        assertEquals(42.5, parser.getDoubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetEmbeddedObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getEmbeddedObject());
    }

    @Test(timeout = 4000)
    public void testGetCurrentLocationAfterClose() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        parser.close();
        JsonLocation loc = parser.getCurrentLocation();
        assertNotNull(loc);
    }

    @Test(timeout = 4000)
    public void testGetTokenLocationAfterClose() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        parser.close();
        JsonLocation loc = parser.getTokenLocation();
        assertNotNull(loc);
    }

    @Test(timeout = 4000)
    public void testGetTextAfterClose() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":\"hello\"}");
        parser.nextToken();
        parser.nextToken();
        parser.nextToken();
        parser.close();
        try {
            parser.getText();
            fail("Expected IOException after close");
        } catch (IOException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNextTokenAfterClose() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        parser.close();
        try {
            parser.nextToken();
            fail("Expected IOException after close");
        } catch (IOException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testGrowArrayBy() {
        int[] arr = new int[]{1, 2, 3};
        int[] grown = UTF8StreamJsonParser.growArrayBy(arr, 2);
        assertEquals(5, grown.length);
        assertEquals(1, grown[0]);
        assertEquals(2, grown[1]);
        assertEquals(3, grown[2]);

        int[] nullArr = UTF8StreamJsonParser.growArrayBy(null, 3);
        assertEquals(3, nullArr.length);
    }

    @Test(timeout = 4000)
    public void testRootValueWithSpace() throws IOException {
        UTF8StreamJsonParser parser = createParser("1 ");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testRootValueWithNewline() throws IOException {
        UTF8StreamJsonParser parser = createParser("1\n");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testRootValueWithCRLF() throws IOException {
        UTF8StreamJsonParser parser = createParser("1\r\n");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testRootValueWithTab() throws IOException {
        UTF8StreamJsonParser parser = createParser("1\t");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testMultipleRootValuesWithSpace() throws IOException {
        UTF8StreamJsonParser parser = createParser("1 2");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
    }

    @Test(timeout = 4000)
    public void testMultipleRootValuesWithNewline() throws IOException {
        UTF8StreamJsonParser parser = createParser("1\n2");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
    }

    @Test(timeout = 4000)
    public void testMultipleRootValuesWithCRLF() throws IOException {
        UTF8StreamJsonParser parser = createParser("1\r\n2");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
    }

    @Test(timeout = 4000)
    public void testMultipleRootValuesWithTab() throws IOException {
        UTF8StreamJsonParser parser = createParser("1\t2");
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
    }

    @Test(timeout = 4000)
    public void testMultipleRootValuesWithComment() throws IOException {
        UTF8StreamJsonParser parser = createParser("1 /* comment */ 2", JsonParser.Feature.ALLOW_COMMENTS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
    }

    @Test(timeout = 4000)
    public void testMultipleRootValuesWithYamlComment() throws IOException {
        UTF8StreamJsonParser parser = createParser("1 # comment\n2", JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
    }

    @Test(timeout = 4000)
    public void testSkipChildren() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":[1,2,{\"b\":3}],\"c\":4}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        parser.skipChildren();
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("c", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(4, parser.getIntValue());
    }

    @Test(timeout = 4000)
    public void testSkipChildrenOnScalar() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        parser.skipChildren();
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testGetIntValueOnFloat() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
    }

    @Test(timeout = 4000)
    public void testGetLongValueOnFloat() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42L, parser.getLongValue());
    }

    @Test(timeout = 4000)
    public void testGetDoubleValueOnInt() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42.0, parser.getDoubleValue(), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetFloatValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.5]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42.5f, parser.getFloatValue(), 0.0f);
    }

    @Test(timeout = 4000)
    public void testGetBigIntegerValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[123456789012345678901234567890]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(new java.math.BigInteger("123456789012345678901234567890"), parser.getBigIntegerValue());
    }

    @Test(timeout = 4000)
    public void testGetBigDecimalValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1234567890.123456789]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(new java.math.BigDecimal("1234567890.123456789"), parser.getDecimalValue());
    }

    @Test(timeout = 4000)
    public void testGetValueAsDouble() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.5]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42.5, parser.getValueAsDouble(), 0.0);
        assertEquals(42.5, parser.getValueAsDouble(0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsLong() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(0L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsBoolean() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true,false,1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFieldName() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStartObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEndObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStartArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEndArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEmbeddedObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNotAvailable() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFieldName() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStartObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEndObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStartArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEndArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEmbeddedObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNotAvailable() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFieldName() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStartObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEndObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStartArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEndArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEmbeddedObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNotAvailable() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFieldName() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStartObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEndObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStartArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEndArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEmbeddedObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNotAvailable() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFieldName() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStartObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEndObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStartArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEndArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEmbeddedObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNotAvailable() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFieldName() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStartObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEndObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStartArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEndArray() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEmbeddedObject() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNotAvailable() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithString() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"true\",\"false\",\"yes\",\"no\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNumber() throws IOException {
        UTF8StreamJsonParser parser = createParser("[0,1,2]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNull() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithTrue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFalse() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithString() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42.5\",\"abc\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42.5, parser.getValueAsDouble(), 0.0);
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithString() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42\",\"abc\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithString() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42\",\"abc\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNull() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNull() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNull() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithTrue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1, parser.getValueAsInt());
        assertEquals(1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFalse() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(0, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithTrue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1L, parser.getValueAsLong());
        assertEquals(1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFalse() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(0L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithTrue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1.0, parser.getValueAsDouble(), 0.0);
        assertEquals(1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFalse() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(0.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFloat() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFloat() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithInt() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42.0, parser.getValueAsDouble(), 0.0);
        assertEquals(42.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNumber() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42,42.5]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("42", parser.getValueAsString());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals("42.5", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNull() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.getValueAsString());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithTrue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFalse() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals("false", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEmbeddedObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNotAvailableValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNumber() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("42", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNull() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndTrue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFalse() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals("false", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEmbeddedObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNotAvailableValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNullValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNullValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNullValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNullValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithTrueValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1, parser.getValueAsInt());
        assertEquals(1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFalseValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(0, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithTrueValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1L, parser.getValueAsLong());
        assertEquals(1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFalseValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(0L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithTrueValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1.0, parser.getValueAsDouble(), 0.0);
        assertEquals(1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFalseValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(0.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStringValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStringValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStringValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42.5\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42.5, parser.getValueAsDouble(), 0.0);
        assertEquals(42.5, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStringValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"true\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFloatValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFloatValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFloatValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42.7, parser.getValueAsDouble(), 0.0);
        assertEquals(42.7, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFloatValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithIntValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithIntValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithIntValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42.0, parser.getValueAsDouble(), 0.0);
        assertEquals(42.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithIntValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithBooleanValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1, parser.getValueAsInt());
        assertEquals(1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithBooleanValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1L, parser.getValueAsLong());
        assertEquals(1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithBooleanValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1.0, parser.getValueAsDouble(), 0.0);
        assertEquals(1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithBooleanValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFieldNameValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFieldNameValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFieldNameValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFieldNameValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFieldNameValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFieldNameValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStartObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStartObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStartObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStartObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStartObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStartObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEndObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEndObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEndObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEndObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEndObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEndObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStartArrayValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStartArrayValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStartArrayValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStartArrayValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStartArrayValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStartArrayValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEndArrayValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEndArrayValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEndArrayValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEndArrayValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEndArrayValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEndArrayValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEmbeddedObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEmbeddedObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEmbeddedObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEmbeddedObjectValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEmbeddedObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEmbeddedObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNotAvailableValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNotAvailableValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNotAvailableValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNotAvailableValue() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNotAvailableValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNotAvailableValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNullToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNullToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNullToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNullToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNullToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNullToken() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNullValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNullValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNullValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNullValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNullValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNullValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithTrueValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1, parser.getValueAsInt());
        assertEquals(1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithTrueValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1L, parser.getValueAsLong());
        assertEquals(1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithTrueValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1.0, parser.getValueAsDouble(), 0.0);
        assertEquals(1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithTrueValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithTrueValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndTrueValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFalseValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(0, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFalseValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(0L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFalseValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(0.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFalseValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertFalse(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFalseValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals("false", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFalseValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals("false", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStringValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStringValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStringValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42.5\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42.5, parser.getValueAsDouble(), 0.0);
        assertEquals(42.5, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStringValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"true\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStringValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"hello\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStringValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"hello\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFloatValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFloatValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFloatValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42.7, parser.getValueAsDouble(), 0.0);
        assertEquals(42.7, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFloatValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFloatValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals("42.7", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFloatValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals("42.7", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithIntValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithIntValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithIntValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42.0, parser.getValueAsDouble(), 0.0);
        assertEquals(42.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithIntValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithIntValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("42", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndIntValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("42", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFieldNameValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFieldNameValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFieldNameValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFieldNameValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFieldNameValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFieldNameValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStartObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStartObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStartObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStartObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStartObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStartObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEndObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEndObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEndObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEndObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEndObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEndObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStartArrayValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStartArrayValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStartArrayValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStartArrayValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStartArrayValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStartArrayValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEndArrayValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEndArrayValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEndArrayValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEndArrayValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEndArrayValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEndArrayValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEmbeddedObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEmbeddedObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEmbeddedObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEmbeddedObjectValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEmbeddedObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEmbeddedObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNotAvailableValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNotAvailableValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNotAvailableValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNotAvailableValue2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNotAvailableValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNotAvailableValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNullToken2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNullToken2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNullToken2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNullToken2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNullToken2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNullToken2() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNullValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNullValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNullValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNullValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNullValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNullValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithTrueValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1, parser.getValueAsInt());
        assertEquals(1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithTrueValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1L, parser.getValueAsLong());
        assertEquals(1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithTrueValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1.0, parser.getValueAsDouble(), 0.0);
        assertEquals(1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithTrueValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithTrueValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndTrueValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFalseValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(0, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFalseValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(0L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFalseValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(0.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFalseValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertFalse(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFalseValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals("false", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFalseValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals("false", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStringValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStringValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStringValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42.5\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42.5, parser.getValueAsDouble(), 0.0);
        assertEquals(42.5, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStringValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"true\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStringValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"hello\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStringValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"hello\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFloatValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFloatValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFloatValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42.7, parser.getValueAsDouble(), 0.0);
        assertEquals(42.7, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFloatValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFloatValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals("42.7", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFloatValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals("42.7", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithIntValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithIntValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithIntValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42.0, parser.getValueAsDouble(), 0.0);
        assertEquals(42.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithIntValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithIntValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("42", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndIntValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("42", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFieldNameValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFieldNameValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFieldNameValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFieldNameValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFieldNameValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFieldNameValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStartObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStartObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStartObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStartObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStartObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStartObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEndObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEndObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEndObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEndObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEndObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEndObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStartArrayValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStartArrayValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStartArrayValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStartArrayValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStartArrayValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStartArrayValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEndArrayValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEndArrayValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEndArrayValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEndArrayValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEndArrayValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEndArrayValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEmbeddedObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEmbeddedObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEmbeddedObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEmbeddedObjectValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEmbeddedObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEmbeddedObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNotAvailableValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNotAvailableValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNotAvailableValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNotAvailableValue3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNotAvailableValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNotAvailableValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNullToken3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNullToken3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNullToken3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNullToken3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNullToken3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNullToken3() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNullValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNullValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNullValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNullValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNullValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNullValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithTrueValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1, parser.getValueAsInt());
        assertEquals(1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithTrueValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1L, parser.getValueAsLong());
        assertEquals(1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithTrueValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1.0, parser.getValueAsDouble(), 0.0);
        assertEquals(1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithTrueValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithTrueValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndTrueValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFalseValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(0, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFalseValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(0L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFalseValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(0.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFalseValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertFalse(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFalseValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals("false", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFalseValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals("false", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStringValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStringValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStringValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42.5\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42.5, parser.getValueAsDouble(), 0.0);
        assertEquals(42.5, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStringValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"true\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStringValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"hello\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStringValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"hello\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFloatValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFloatValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFloatValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42.7, parser.getValueAsDouble(), 0.0);
        assertEquals(42.7, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFloatValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFloatValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals("42.7", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFloatValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals("42.7", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithIntValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithIntValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithIntValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42.0, parser.getValueAsDouble(), 0.0);
        assertEquals(42.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithIntValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithIntValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("42", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndIntValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("42", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFieldNameValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFieldNameValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFieldNameValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFieldNameValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFieldNameValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFieldNameValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStartObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStartObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStartObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStartObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStartObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStartObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEndObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEndObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEndObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEndObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEndObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEndObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStartArrayValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStartArrayValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStartArrayValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStartArrayValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStartArrayValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStartArrayValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEndArrayValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEndArrayValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEndArrayValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEndArrayValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEndArrayValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEndArrayValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEmbeddedObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEmbeddedObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEmbeddedObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEmbeddedObjectValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEmbeddedObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEmbeddedObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNotAvailableValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNotAvailableValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNotAvailableValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNotAvailableValue4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNotAvailableValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNotAvailableValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNullToken4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNullToken4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNullToken4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNullToken4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNullToken4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNullToken4() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNullValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNullValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNullValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNullValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNullValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNullValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithTrueValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1, parser.getValueAsInt());
        assertEquals(1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithTrueValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1L, parser.getValueAsLong());
        assertEquals(1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithTrueValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1.0, parser.getValueAsDouble(), 0.0);
        assertEquals(1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithTrueValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithTrueValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndTrueValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFalseValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(0, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFalseValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(0L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFalseValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(0.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFalseValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertFalse(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFalseValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals("false", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFalseValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals("false", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStringValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStringValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStringValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42.5\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42.5, parser.getValueAsDouble(), 0.0);
        assertEquals(42.5, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStringValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"true\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStringValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"hello\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStringValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"hello\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFloatValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFloatValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFloatValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42.7, parser.getValueAsDouble(), 0.0);
        assertEquals(42.7, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFloatValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFloatValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals("42.7", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFloatValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals("42.7", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithIntValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithIntValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithIntValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42.0, parser.getValueAsDouble(), 0.0);
        assertEquals(42.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithIntValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithIntValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("42", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndIntValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("42", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFieldNameValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFieldNameValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFieldNameValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFieldNameValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFieldNameValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFieldNameValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStartObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStartObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStartObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStartObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStartObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStartObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEndObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEndObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEndObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEndObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEndObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEndObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStartArrayValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStartArrayValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStartArrayValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStartArrayValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStartArrayValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStartArrayValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEndArrayValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEndArrayValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEndArrayValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEndArrayValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEndArrayValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEndArrayValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEmbeddedObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEmbeddedObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEmbeddedObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEmbeddedObjectValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEmbeddedObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEmbeddedObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNotAvailableValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNotAvailableValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNotAvailableValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNotAvailableValue5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNotAvailableValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNotAvailableValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNullToken5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNullToken5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNullToken5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNullToken5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNullToken5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNullToken5() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNullValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNullValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNullValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNullValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNullValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNullValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithTrueValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1, parser.getValueAsInt());
        assertEquals(1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithTrueValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1L, parser.getValueAsLong());
        assertEquals(1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithTrueValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1.0, parser.getValueAsDouble(), 0.0);
        assertEquals(1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithTrueValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithTrueValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndTrueValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFalseValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(0, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFalseValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(0L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFalseValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(0.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFalseValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertFalse(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFalseValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals("false", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFalseValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals("false", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStringValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStringValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStringValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"42.5\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals(42.5, parser.getValueAsDouble(), 0.0);
        assertEquals(42.5, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStringValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"true\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStringValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"hello\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStringValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[\"hello\"]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFloatValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFloatValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFloatValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(42.7, parser.getValueAsDouble(), 0.0);
        assertEquals(42.7, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFloatValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFloatValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals("42.7", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFloatValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42.7]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals("42.7", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithIntValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getValueAsInt());
        assertEquals(42, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithIntValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42L, parser.getValueAsLong());
        assertEquals(42L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithIntValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42.0, parser.getValueAsDouble(), 0.0);
        assertEquals(42.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithIntValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithIntValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("42", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndIntValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[42]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals("42", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFieldNameValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFieldNameValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFieldNameValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithFieldNameValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithFieldNameValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndFieldNameValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStartObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStartObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStartObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStartObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStartObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStartObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEndObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEndObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEndObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEndObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEndObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEndObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithStartArrayValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithStartArrayValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithStartArrayValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithStartArrayValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithStartArrayValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndStartArrayValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEndArrayValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEndArrayValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEndArrayValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEndArrayValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEndArrayValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEndArrayValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("[1]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithEmbeddedObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithEmbeddedObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithEmbeddedObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithEmbeddedObjectValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithEmbeddedObjectValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndEmbeddedObjectValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNotAvailableValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNotAvailableValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNotAvailableValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNotAvailableValue6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNotAvailableValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNotAvailableValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken();
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNullToken6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNullToken6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNullToken6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNullToken6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNullToken6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNullToken6() throws IOException {
        UTF8StreamJsonParser parser = createParser("{\"a\":1}");
        assertNull(parser.getCurrentToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithNullValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(-1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithNullValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(-1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithNullValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(0.0, parser.getValueAsDouble(), 0.0);
        assertEquals(-1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithNullValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertFalse(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithNullValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndNullValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("[null]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals("default", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithTrueValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1, parser.getValueAsInt());
        assertEquals(1, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithTrueValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1L, parser.getValueAsLong());
        assertEquals(1L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithTrueValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(1.0, parser.getValueAsDouble(), 0.0);
        assertEquals(1.0, parser.getValueAsDouble(-1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetValueAsBooleanWithTrueValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getValueAsBoolean());
        assertTrue(parser.getValueAsBoolean(true));
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithTrueValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getValueAsString());
    }

    @Test(timeout = 4000)
    public void testGetValueAsStringWithDefaultAndTrueValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("[true]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals("true", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetValueAsIntWithFalseValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0, parser.getValueAsInt());
        assertEquals(0, parser.getValueAsInt(-1));
    }

    @Test(timeout = 4000)
    public void testGetValueAsLongWithFalseValue7() throws IOException {
        UTF8StreamJsonParser parser = createParser("[false]");
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(0L, parser.getValueAsLong());
        assertEquals(0L, parser.getValueAsLong(-1L));
    }

    @Test(timeout = 4000)
    public void testGetValueAsDoubleWithFalseValue7() throws IOException {
        UTF8StreamJsonParser