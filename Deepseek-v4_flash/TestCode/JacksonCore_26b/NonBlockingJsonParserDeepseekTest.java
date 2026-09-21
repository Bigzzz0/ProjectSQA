package com.fasterxml.jackson.core.json.async;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.async.ByteArrayFeeder;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.ByteQuadsCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Targeted Decision Branches:
 * 1. BOM handling: _startDocument with 0xEF, _finishBOM states 1,2,3
 * 2. Whitespace skipping: _skipWS with LF, CR, TAB, other control chars
 * 3. Field name parsing: _fastParseName, _parseEscapedName, _handleOddName
 * 4. Number parsing: leading zeros, negative numbers, exponents, fractions
 * 5. Keyword tokens: true/false/null with boundary conditions
 * 6. Comment handling: C-style, C++-style, YAML-style
 * 7. String parsing: regular, apostrophe, escape sequences, UTF-8 multi-byte
 * 8. Array/object scope transitions
 * 9. Trailing comma feature
 * 10. Missing values feature
 * 11. End-of-input handling in various states
 * 
 * Targeted Boundary Conditions:
 * - Empty input
 * - Single byte input
 * - Input at buffer boundaries
 * - Maximum/minimum integer values
 * - Zero and negative zero
 * - Empty strings and field names
 * - UTF-8 2/3/4 byte sequences at buffer boundaries
 * 
 * Defect Targeting (AsyncLocationTest::testLocationOffsets):
 * The defect involves incorrect location tracking when BOM is present.
 * _finishBOM adjusts _currInputProcessed by -3 but may not properly
 * account for the BOM bytes in row/column calculations.
 */
public class NonBlockingJsonParserDeepseekTest {
    
    private static final IOContext TEST_CTXT = new IOContext(
        new BufferRecycler(), null, false);
    
    private static final ByteQuadsCanonicalizer TEST_SYMBOLS = 
        ByteQuadsCanonicalizer.createRoot();
    
    private NonBlockingJsonParser createParser(int features) {
        return new NonBlockingJsonParser(TEST_CTXT, features, TEST_SYMBOLS);
    }
    
    private NonBlockingJsonParser createDefaultParser() {
        return createParser(JsonParser.Feature.STRICT_DUPLICATE_DETECTION.getMask());
    }
    
    private void feedAndAssert(NonBlockingJsonParser parser, byte[] input, 
            JsonToken expected) throws IOException {
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        assertEquals(expected, parser.nextToken());
    }
    
    // ========== Partition A: Core Functional Logic & State Transitions ==========
    
    @Test(timeout = 4000)
    public void testEmptyObject() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "{}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testEmptyArray() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "[]".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testSimpleStringValue() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "\"hello\"".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello", parser.getText());
    }
    
    @Test(timeout = 4000)
    public void testSimpleIntegerValue() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "42".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
    }
    
    @Test(timeout = 4000)
    public void testSimpleFloatValue() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "3.14".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
    }
    
    @Test(timeout = 4000)
    public void testTrueFalseNull() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "true false null".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testObjectWithField() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "{\"key\":\"value\"}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testArrayWithElements() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "[1,2,3]".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
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
    public void testNestedStructure() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "{\"a\":{\"b\":[1,2]}}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }
    
    // ========== Partition B: Boundary Value Analysis & Extremes ==========
    
    @Test(timeout = 4000)
    public void testEmptyInput() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        parser.endOfInput();
        assertNull(parser.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testWhitespaceOnly() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "   \t\n\r  ".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        assertNull(parser.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testNegativeInteger() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "-123".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-123, parser.getIntValue());
    }
    
    @Test(timeout = 4000)
    public void testZero() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "0".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());
    }
    
    @Test(timeout = 4000)
    public void testNegativeZero() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "-0".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(0, parser.getIntValue());
    }
    
    @Test(timeout = 4000)
    public void testLargeInteger() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "2147483647".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2147483647, parser.getIntValue());
    }
    
    @Test(timeout = 4000)
    public void testNegativeLargeInteger() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "-2147483648".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-2147483648, parser.getIntValue());
    }
    
    @Test(timeout = 4000)
    public void testFloatWithExponent() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "1.5e10".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.5e10, parser.getDoubleValue(), 0.0001);
    }
    
    @Test(timeout = 4000)
    public void testNegativeExponent() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "2.5e-3".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(2.5e-3, parser.getDoubleValue(), 0.0001);
    }
    
    @Test(timeout = 4000)
    public void testEmptyString() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "\"\"".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("", parser.getText());
    }
    
    @Test(timeout = 4000)
    public void testEmptyFieldName() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "{\"\":1}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testStringWithEscapedChars() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "\"hello\\nworld\\ttab\"".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello\nworld\ttab", parser.getText());
    }
    
    @Test(timeout = 4000)
    public void testStringWithUnicodeEscape() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "\"\\u0048\\u0065\\u006c\\u006c\\u006f\"".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("Hello", parser.getText());
    }
    
    @Test(timeout = 4000)
    public void testMultipleValues() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "1 2 3".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertNull(parser.nextToken());
    }
    
    // ========== Partition C: Defect-Targeted Branch Zone ==========
    
    @Test(timeout = 4000)
    public void testBOMWithLocationTracking() throws IOException {
        // This test targets the known defect: AsyncLocationTest::testLocationOffsets
        // The bug causes incorrect location tracking when BOM is present.
        // Expected: after BOM, token positions should be correct.
        NonBlockingJsonParser parser = createDefaultParser();
        
        // UTF-8 BOM: 0xEF, 0xBB, 0xBF followed by a simple JSON value
        byte[] input = {(byte)0xEF, (byte)0xBB, (byte)0xBF, (byte)'1', (byte)'2', (byte)'3'};
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        
        // After BOM, the token should be at the correct position
        // The bug would report incorrect token location
        JsonLocation loc = parser.getTokenLocation();
        assertNotNull("Token location should not be null", loc);
        // The BOM is 3 bytes, so the token should start at byte offset 3
        assertEquals("Token should start after BOM", 3L, loc.getByteOffset());
    }
    
    @Test(timeout = 4000)
    public void testBOMWithObject() throws IOException {
        // Additional BOM test with more complex structure
        NonBlockingJsonParser parser = createDefaultParser();
        
        byte[] input = {(byte)0xEF, (byte)0xBB, (byte)0xBF, 
                        (byte)'{', (byte)'"', (byte)'a', (byte)'"', (byte)':', 
                        (byte)'1', (byte)'}'};
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testBOMWithWhitespace() throws IOException {
        // Test BOM followed by whitespace
        NonBlockingJsonParser parser = createDefaultParser();
        
        byte[] input = {(byte)0xEF, (byte)0xBB, (byte)0xBF, 
                        (byte)' ', (byte)'\n', (byte)'\t',
                        (byte)'"', (byte)'h', (byte)'i', (byte)'"'};
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hi", parser.getText());
    }
    
    @Test(timeout = 4000)
    public void testPartialBOMThenComplete() throws IOException {
        // Test feeding BOM in multiple chunks
        NonBlockingJsonParser parser = createDefaultParser();
        
        byte[] part1 = {(byte)0xEF, (byte)0xBB};
        byte[] part2 = {(byte)0xBF, (byte)'4', (byte)'2'};
        
        parser.feedInput(part1, 0, part1.length);
        assertEquals(JsonToken.NOT_AVAILABLE, parser.nextToken());
        
        parser.feedInput(part2, 0, part2.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
    }
    
    // ========== Partition D: Exception & Defensive Guard Paths ==========
    
    @Test(expected = IOException.class, timeout = 4000)
    public void testFeedInputWithRemainingData() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "{}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        // Try to feed more without consuming
        parser.feedInput(input, 0, input.length);
    }
    
    @Test(expected = IOException.class, timeout = 4000)
    public void testFeedInputWithInvalidEnd() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "{}".getBytes("UTF-8");
        parser.feedInput(input, 5, 3); // end < start
    }
    
    @Test(expected = IOException.class, timeout = 4000)
    public void testFeedInputAfterEndOfInput() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "{}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        parser.nextToken(); // consume
        parser.feedInput(input, 0, input.length); // should fail
    }
    
    @Test(expected = IOException.class, timeout = 4000)
    public void testInvalidToken() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "xyz".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        parser.nextToken(); // should throw
    }
    
    @Test(expected = IOException.class, timeout = 4000)
    public void testUnexpectedEndOfInput() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "{\"key\":".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        parser.nextToken(); // start object
        parser.nextToken(); // field name
        parser.nextToken(); // should fail - expecting value
    }
    
    @Test(expected = IOException.class, timeout = 4000)
    public void testInvalidNumberFormat() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "12a".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        parser.nextToken(); // should fail
    }
    
    @Test(expected = IOException.class, timeout = 4000)
    public void testLeadingZerosNotAllowed() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "0123".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        parser.nextToken(); // should fail
    }
    
    @Test(expected = IOException.class, timeout = 4000)
    public void testSingleQuoteNotAllowed() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "{'key':'value'}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        parser.nextToken(); // should fail
    }
    
    @Test(expected = IOException.class, timeout = 4000)
    public void testUnquotedFieldNameNotAllowed() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "{key:1}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        parser.nextToken(); // should fail
    }
    
    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    
    @Test(timeout = 4000)
    public void testNeedMoreInput() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        assertTrue(parser.needMoreInput());
        
        byte[] input = "{}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        assertFalse(parser.needMoreInput());
        
        parser.nextToken();
        assertFalse(parser.needMoreInput());
        
        parser.nextToken();
        assertFalse(parser.needMoreInput());
        
        parser.endOfInput();
        assertTrue(parser.needMoreInput());
    }
    
    @Test(timeout = 4000)
    public void testReleaseBuffered() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "hello".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        int released = parser.releaseBuffered(out);
        assertEquals(5, released);
        assertArrayEquals(input, out.toByteArray());
    }
    
    @Test(timeout = 4000)
    public void testGetNonBlockingInputFeeder() {
        NonBlockingJsonParser parser = createDefaultParser();
        assertSame(parser, parser.getNonBlockingInputFeeder());
    }
    
    @Test(timeout = 4000)
    public void testClosedParserReturnsNull() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        parser.close();
        assertNull(parser.nextToken());
    }
    
    // ========== Additional Coverage Tests ==========
    
    @Test(timeout = 4000)
    public void testTrailingCommaInObject() throws IOException {
        int features = JsonParser.Feature.ALLOW_TRAILING_COMMA.getMask();
        NonBlockingJsonParser parser = createParser(features);
        byte[] input = "{\"a\":1,}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testTrailingCommaInArray() throws IOException {
        int features = JsonParser.Feature.ALLOW_TRAILING_COMMA.getMask();
        NonBlockingJsonParser parser = createParser(features);
        byte[] input = "[1,2,]".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testMissingValues() throws IOException {
        int features = JsonParser.Feature.ALLOW_MISSING_VALUES.getMask();
        NonBlockingJsonParser parser = createParser(features);
        byte[] input = "[1,,2]".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.START_ARRAY, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertEquals(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testSingleQuotesEnabled() throws IOException {
        int features = JsonParser.Feature.ALLOW_SINGLE_QUOTES.getMask();
        NonBlockingJsonParser parser = createParser(features);
        byte[] input = "{'a':'b'}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("b", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testUnquotedFieldNamesEnabled() throws IOException {
        int features = JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES.getMask();
        NonBlockingJsonParser parser = createParser(features);
        byte[] input = "{key:123}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testCommentsEnabled() throws IOException {
        int features = JsonParser.Feature.ALLOW_COMMENTS.getMask();
        NonBlockingJsonParser parser = createParser(features);
        byte[] input = "/* comment */ 42".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
    }
    
    @Test(timeout = 4000)
    public void testCppCommentsEnabled() throws IOException {
        int features = JsonParser.Feature.ALLOW_COMMENTS.getMask();
        NonBlockingJsonParser parser = createParser(features);
        byte[] input = "// line comment\n42".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
    }
    
    @Test(timeout = 4000)
    public void testYamlCommentsEnabled() throws IOException {
        int features = JsonParser.Feature.ALLOW_YAML_COMMENTS.getMask();
        NonBlockingJsonParser parser = createParser(features);
        byte[] input = "# yaml comment\n42".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
    }
    
    @Test(timeout = 4000)
    public void testLeadingZerosEnabled() throws IOException {
        int features = JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        NonBlockingJsonParser parser = createParser(features);
        byte[] input = "00123".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(123, parser.getIntValue());
    }
    
    @Test(timeout = 4000)
    public void testNegativeInfinity() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "-Infinity".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isInfinite(parser.getDoubleValue()));
        assertTrue(parser.getDoubleValue() < 0);
    }
    
    @Test(timeout = 4000)
    public void testNaN() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "NaN".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isNaN(parser.getDoubleValue()));
    }
    
    @Test(timeout = 4000)
    public void testPlusInfinity() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "+Infinity".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isInfinite(parser.getDoubleValue()));
        assertTrue(parser.getDoubleValue() > 0);
    }
    
    @Test(timeout = 4000)
    public void testUTF8TwoByteChar() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        // Unicode character U+00A9 (copyright sign) = 0xC2 0xA9 in UTF-8
        byte[] input = {(byte)'"', (byte)0xC2, (byte)0xA9, (byte)'"'};
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\u00A9", parser.getText());
    }
    
    @Test(timeout = 4000)
    public void testUTF8ThreeByteChar() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        // Unicode character U+20AC (euro sign) = 0xE2 0x82 0xAC in UTF-8
        byte[] input = {(byte)'"', (byte)0xE2, (byte)0x82, (byte)0xAC, (byte)'"'};
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\u20AC", parser.getText());
    }
    
    @Test(timeout = 4000)
    public void testUTF8FourByteChar() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        // Unicode character U+1F600 (grinning face emoji) = 0xF0 0x9F 0x98 0x80 in UTF-8
        byte[] input = {(byte)'"', (byte)0xF0, (byte)0x9F, (byte)0x98, (byte)0x80, (byte)'"'};
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("\uD83D\uDE00", parser.getText());
    }
    
    @Test(timeout = 4000)
    public void testFieldNameWithEscape() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "{\"key\\nname\":1}".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key\nname", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testLongFieldName() throws IOException {
        // Field name longer than 12 characters to test _parseMediumName2
        NonBlockingJsonParser parser = createDefaultParser();
        String longName = "abcdefghijklm"; // 13 chars
        byte[] input = ("{\"" + longName + "\":1}").getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(longName, parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testNegativeFloatWithExponent() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "-1.5e-2".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(-0.015, parser.getDoubleValue(), 0.0001);
    }
    
    @Test(timeout = 4000)
    public void testNumberWithLeadingZeroAndDecimal() throws IOException {
        int features = JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        NonBlockingJsonParser parser = createParser(features);
        byte[] input = "00.5".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(0.5, parser.getDoubleValue(), 0.0001);
    }
    
    @Test(timeout = 4000)
    public void testNegativeNumberWithLeadingZero() throws IOException {
        int features = JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS.getMask();
        NonBlockingJsonParser parser = createParser(features);
        byte[] input = "-00123".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-123, parser.getIntValue());
    }
    
    @Test(timeout = 4000)
    public void testStringWithBackslashEscape() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "\"back\\\\slash\"".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("back\\slash", parser.getText());
    }
    
    @Test(timeout = 4000)
    public void testStringWithForwardSlashEscape() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        byte[] input = "\"forward\\/slash\"".getBytes("UTF-8");
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("forward/slash", parser.getText());
    }
    
    @Test(timeout = 4000)
    public void testFeedInputInChunks() throws IOException {
        NonBlockingJsonParser parser = createDefaultParser();
        
        byte[] chunk1 = "{\"a\"".getBytes("UTF-8");
        byte[] chunk2 = ":\"b\"}".getBytes("UTF-8");
        
        parser.feedInput(chunk1, 0, chunk1.length);
        assertEquals(JsonToken.NOT_AVAILABLE, parser.nextToken());
        
        parser.feedInput(chunk2, 0, chunk2.length);
        parser.endOfInput();
        
        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("b", parser.getText());
        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }
    
    @Test(timeout = 4000)
    public void testTokenLocationAfterBOM() throws IOException {
        // Direct test for the known defect
        NonBlockingJsonParser parser = createDefaultParser();
        
        // BOM + simple value
        byte[] input = {(byte)0xEF, (byte)0xBB, (byte)0xBF, (byte)'1'};
        parser.feedInput(input, 0, input.length);
        parser.endOfInput();
        
        parser.nextToken();
        JsonLocation loc = parser.getTokenLocation();
        
        // The bug: location reports byte offset 3 instead of 1
        // Expected correct behavior: byte offset should be 3 (after 3-byte BOM)
        assertEquals("Token byte offset should account for BOM", 3L, loc.getByteOffset());
        
        // Also check character offset
        assertEquals("Token char offset should account for BOM", 3L, loc.getCharOffset());
    }
}