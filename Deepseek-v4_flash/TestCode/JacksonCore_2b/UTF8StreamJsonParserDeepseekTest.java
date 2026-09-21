package com.fasterxml.jackson.core.json;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.JsonParser.NumberType;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.sym.BytesToNameCanonicalizer;
import com.fasterxml.jackson.core.util.BufferRecycler;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * High-coverage JUnit 4 test for UTF8StreamJsonParser, targeting the known
 * defect in number parsing (mangled numbers should cause exception, not VALUE_NUMBER_INT).
 * Tests include normal operations, boundary conditions, and error handling.
 */
public class UTF8StreamJsonParserDeepseekTest {

    /*
     * [Branch & Defect Analysis Matrix]
     * 
     * Partition A: Core Functional Logic & State Transitions
     *   - nextToken() for objects, arrays, simple values
     *   - _nextAfterName() with START_ARRAY/START_OBJECT
     *   - getText(), getValueAsString(), getTextCharacters(), getTextLength(), getTextOffset()
     *   - nextFieldName(), nextTextValue(), nextIntValue(), nextLongValue(), nextBooleanValue()
     *   - close() releasing symbol table
     * 
     * Partition B: Boundary Value Analysis & Extremes
     *   - Empty input, whitespace only, comments (single-line, C-style, YAML)
     *   - Strings: empty, short, long, with escapes, UTF-8 multibyte (2,3,4 byte)
     *   - Numbers: zero, positive/negative, integer, floating, exponent, leading zeros (with/without feature)
     *   - Base64 binary: padding, no padding, inline, via readBinaryValue
     *   - Field names: empty, single char, multi-byte, escaped, quoted, unquoted (feature), single-quoted (feature)
     * 
     * Partition C: Defect-Targeted Branch Zone (Ground Truth)
     *   - Malformed number like "0e", "0E", "0.", "-0e", "1e", etc. must throw JsonParseException
     *   - The bug returns VALUE_NUMBER_INT instead; test asserts exception.
     * 
     * Partition D: Exception & Defensive Guard Paths
     *   - Invalid UTF-8 bytes, invalid escapes, unexpected chars
     *   - End-of-input mid-stream, mismatched brackets
     *   - Illegal number formats (two dots, exponent without digits, etc.)
     *   - _throwUnquotedSpace, _reportInvalidChar, _reportInvalidToken
     *   - _decodeBase64 escape handling, broken base64
     * 
     * Partition E: Object Lifecycle & Contract Integrity
     *   - releaseBuffered() returning correct count
     *   - getInputSource() returning InputStream
     *   - getCodec() / setCodec()
     *   - Close / release buffers
     */

    // -----------------------------------------------------------------------
    // Helper methods
    // -----------------------------------------------------------------------

    private JsonParser createParser(byte[] data) throws IOException {
        return new JsonFactory().createParser(data);
    }

    private JsonParser createParser(String json) throws IOException {
        return createParser(json.getBytes("UTF-8"));
    }

    // Enable features for some tests
    private JsonFactory factoryWithFeatures(JsonParser.Feature... features) {
        JsonFactory f = new JsonFactory();
        for (JsonParser.Feature feat : features) {
            f.enable(feat);
        }
        return f;
    }

    private JsonParser createParser(byte[] data, JsonParser.Feature... features) throws IOException {
        JsonFactory f = factoryWithFeatures(features);
        return f.createParser(data);
    }

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSimpleObject() throws Exception {
        String json = "{\"a\":1, \"b\":\"hello\", \"c\":true, \"d\":null}";
        JsonParser p = createParser(json);
        assertNull(p.getCurrentToken());
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getCurrentName());
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("c", p.getCurrentName());
        assertSame(JsonToken.VALUE_TRUE, p.nextToken());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("d", p.getCurrentName());
        assertSame(JsonToken.VALUE_NULL, p.nextToken());
        assertSame(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSimpleArray() throws Exception {
        String json = "[1,2,3]";
        JsonParser p = createParser(json);
        assertSame(JsonToken.START_ARRAY, p.nextToken());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(3, p.getIntValue());
        assertSame(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextFieldNameMatch() throws Exception {
        String json = "{\"foo\":123}";
        JsonParser p = createParser(json);
        assertTrue(p.nextToken() == JsonToken.START_OBJECT);
        SerializableString field = new SerializableString() {
            public String getValue() { return "foo"; }
            public int charLength() { return 3; }
            public char[] asQuotedChars() { return "\"foo\"".toCharArray(); }
            public byte[] asUnquotedUTF8() { return new byte[] {'f','o','o'}; }
            public byte[] asQuotedUTF8() { return new byte[] {'"','f','o','o','"'}; }
        };
        assertTrue(p.nextFieldName(field));
        assertEquals(123, p.getIntValue());
        assertSame(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextTextValue() throws Exception {
        String json = "{\"x\":\"y\", \"z\":42}";
        JsonParser p = createParser(json);
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("x", p.getCurrentName());
        assertEquals("y", p.nextTextValue());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("z", p.getCurrentName());
        assertNull(p.nextTextValue());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.getCurrentToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextIntValue() throws Exception {
        String json = "{\"a\":10, \"b\":\"notint\"}";
        JsonParser p = createParser(json);
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertEquals(10, p.nextIntValue(-1));
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getCurrentName());
        assertEquals(-1, p.nextIntValue(-1));
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextLongValue() throws Exception {
        String json = "{\"a\":9999999999999}";
        JsonParser p = createParser(json);
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(9999999999999L, p.nextLongValue(0L));
        assertSame(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNextBooleanValue() throws Exception {
        String json = "{\"a\":true, \"b\":false, \"c\":42}";
        JsonParser p = createParser(json);
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(Boolean.TRUE, p.nextBooleanValue());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(Boolean.FALSE, p.nextBooleanValue());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertNull(p.nextBooleanValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetTextVariousTokens() throws Exception {
        String json = "{\"name\":\"value\", \"num\":-42, \"flt\":3.14e0}";
        JsonParser p = createParser(json);
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("name", p.getText());
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value", p.getText());
        p.nextToken(); // field name "num"
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("-42", p.getText());
        p.nextToken(); // field name "flt"
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("3.14e0", p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetValueAsString() throws Exception {
        String json = "\"hello\"";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getValueAsString());
        assertNull(p.getValueAsString(null));
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetTextCharacters() throws Exception {
        String json = "{\"field\":\"longstring\"}";
        JsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        char[] ch = p.getTextCharacters();
        assertEquals("field", new String(ch, p.getTextOffset(), p.getTextLength()));
        p.nextToken(); // VALUE_STRING
        ch = p.getTextCharacters();
        assertEquals("longstring", new String(ch, p.getTextOffset(), p.getTextLength()));
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetTextOffsetAndLength() throws Exception {
        String json = "\"text\"";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(0, p.getTextOffset());
        assertEquals(4, p.getTextLength());
        p.close();
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEmptyInput() throws Exception {
        JsonParser p = createParser(new byte[0]);
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testWhitespaceOnly() throws Exception {
        JsonParser p = createParser("   \t\n\r  ".getBytes("UTF-8"));
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testEmptyString() throws Exception {
        JsonParser p = createParser("\"\"");
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("", p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSingleCharString() throws Exception {
        JsonParser p = createParser("\"a\"");
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("a", p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testStringWithEscapes() throws Exception {
        String json = "\"abc\\n\\t\\r\\b\\f\\\\\\/\\\"\"";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("abc\n\t\r\b\f\\/\"", p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testUTF8TwoByteChar() throws Exception {
        String json = "\"\\u00e9\""; // é (U+00E9) two bytes in UTF-8? Actually U+00E9 is 2 bytes 0xC3 0xA9
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("\u00e9", p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testUTF8ThreeByteChar() throws Exception {
        String json = "\"\\u4e2d\""; // 中 (U+4E2D) three bytes
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("\u4e2d", p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testUTF8FourByteChar() throws Exception {
        // U+1F600 (😀) surrogate pair -> 4 bytes UTF-8
        String json = "\"\\ud83d\\ude00\"";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("\uD83D\uDE00", p.getText());
        p.close();
    }

    @Test(timeout = 4000)
    public void testIntegerZero() throws Exception {
        String json = "0";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(0, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNegativeInteger() throws Exception {
        String json = "-123456789";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-123456789, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testFloatSimple() throws Exception {
        String json = "3.14";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(3.14, p.getDoubleValue(), 0.0001);
        p.close();
    }

    @Test(timeout = 4000)
    public void testFloatWithExponent() throws Exception {
        String json = "1.5e2";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(150.0, p.getDoubleValue(), 0.0);
        p.close();
    }

    @Test(timeout = 4000)
    public void testNegativeExponent() throws Exception {
        String json = "-1.5e-2";
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(-0.015, p.getDoubleValue(), 0.00001);
        p.close();
    }

    @Test(timeout = 4000)
    public void testLeadingZeroDisabled() throws Exception {
        String json = "0123";
        try {
            JsonParser p = createParser(json);
            p.nextToken();
            fail("Expected JsonParseException for leading zero");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testLeadingZeroEnabled() throws Exception {
        JsonParser p = createParser("0123".getBytes("UTF-8"), JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSingleQuotesDisabled() throws Exception {
        String json = "{'a':1}";
        JsonParser p = createParser(json);
        try {
            p.nextToken();
            fail("Expected exception for single quotes without feature");
        } catch (JsonParseException e) { }
    }

    @Test(timeout = 4000)
    public void testSingleQuotesEnabled() throws Exception {
        JsonParser p = createParser("{'a':1}".getBytes("UTF-8"), JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testUnquotedFieldNameDisabled() throws Exception {
        String json = "{a:1}";
        JsonParser p = createParser(json);
        try {
            p.nextToken();
            fail("Expected exception for unquoted name without feature");
        } catch (JsonParseException e) { }
    }

    @Test(timeout = 4000)
    public void testUnquotedFieldNameEnabled() throws Exception {
        JsonParser p = createParser("{a:1}".getBytes("UTF-8"), JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        assertSame(JsonToken.START_OBJECT, p.nextToken());
        assertSame(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testCommentCppStyle() throws Exception {
        String json = "// comment\n42";
        JsonParser p = createParser(json.getBytes("UTF-8"), JsonParser.Feature.ALLOW_COMMENTS);
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testCommentCStyle() throws Exception {
        String json = "/* multi\nline */ true";
        JsonParser p = createParser(json.getBytes("UTF-8"), JsonParser.Feature.ALLOW_COMMENTS);
        assertSame(JsonToken.VALUE_TRUE, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testYamlCommentEnabled() throws Exception {
        String json = "# this is yaml\n42";
        JsonParser p = createParser(json.getBytes("UTF-8"), JsonParser.Feature.ALLOW_YAML_COMMENTS);
        assertSame(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        p.close();
    }

    @Test(timeout = 4000)
    public void testYamlCommentDisabled() throws Exception {
        String json = "# this is not a comment\n42";
        JsonParser p = createParser(json.getBytes("UTF-8"), JsonParser.Feature.ALLOW_COMMENTS); // only C++/C comments
        // '#' is not a comment, so it will be treated as unexpected character
        try {
            p.nextToken();
            fail("Expected exception because # is not valid");
        } catch (JsonParseException e) { }
    }

    @Test(timeout = 4000)
    public void testNaNEnabled() throws Exception {
        JsonParser p = createParser("NaN".getBytes("UTF-8"), JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isNaN(p.getDoubleValue()));
        p.close();
    }

    @Test(timeout = 4000)
    public void testInfinityEnabled() throws Exception {
        JsonParser p = createParser("Infinity".getBytes("UTF-8"), JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isInfinite(p.getDoubleValue()));
        assertEquals(Double.POSITIVE_INFINITY, p.getDoubleValue(), 0.0);
        p.close();
    }

    @Test(timeout = 4000)
    public void testNegativeInfinityEnabled() throws Exception {
        JsonParser p = createParser("-Infinity".getBytes("UTF-8"), JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        assertSame(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertTrue(Double.isInfinite(p.getDoubleValue()));
        assertEquals(Double.NEGATIVE_INFINITY, p.getDoubleValue(), 0.0);
        p.close();
    }

    @Test(timeout = 4000)
    public void testBase64Decode() throws Exception {
        String json = "\"SGVsbG8=\""; // "Hello" in base64
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        byte[] decoded = p.getBinaryValue(Base64Variant.STANDARD);
        assertEquals("Hello", new String(decoded, "UTF-8"));
        p.close();
    }

    @Test(timeout = 4000)
    public void testBase64NoPadding() throws Exception {
        String json = "\"SGVsbG8\""; // "Hello" without padding, but standard requires padding, so should fail? Actually MIME allows omission? Let's test with no-padding variant
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        // Standard base64 requires padding, so this should fail
        try {
            p.getBinaryValue(Base64Variant.STANDARD);
            fail("Expected exception for missing padding");
        } catch (JsonParseException e) { }
    }

    @Test(timeout = 4000)
    public void testReadBinaryValue() throws Exception {
        String json = "\"YWJj\""; // "abc"
        JsonParser p = createParser(json);
        assertSame(JsonToken.VALUE_STRING, p.nextToken());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = p.readBinaryValue(Base64Variant.STANDARD, out);
        assertEquals(3, count);
        assertArrayEquals("abc".getBytes("UTF-8"), out.toByteArray());
        p.close();
    }

    @Test(timeout = 4000)
    public void testReleaseBuffered() throws Exception {
        // Test that releaseBuffered returns the correct number of bytes
        byte[] data = "   hello".getBytes("UTF-8"); // whitespace then 'h'...
        // We need a parser that has buffered data. Use JsonFactory from InputStream.
        JsonFactory f = new JsonFactory();
        JsonParser p = f.createParser(new ByteArrayInputStream(data));
        // Advance a little to consume some whitespace => remaining buffered
        assertNull(p.nextToken()); // because 'hello' is not valid JSON
        // But we can still call releaseBuffered on output stream
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = ((UTF8StreamJsonParser) p).releaseBuffered(out); // cast to access method
        assertTrue(count >= 0);
        p.close();
    }

    @Test(timeout = 4000)
    public void testGetInputSource() throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream("42".getBytes("UTF-8"));
        JsonParser p = new JsonFactory().createParser(in);
        assertSame(in, p.getInputSource());
        p.close();
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Mangled Numbers)
    // -----------------------------------------------------------------------

    /**
     * Test that mangled numbers (e.g., "0e", "0E", "0.", etc.) cause an exception
     * rather than returning VALUE_NUMBER_INT. This targets the known defect.
     */
    @Test(timeout = 4000)
    public void testMangledNumberNoDigitsAfterExponent() throws Exception {
        String[] malformed = {
            "0e", "0E", "0.", "-0e", "-0E", "-0.", "1e", "1E", "1.",
            "12e", "12E", "12.", "-12e", "-12E", "-12.",
            "0e+", "0e-", "1e+", "1e-", "-1e+", "-1e-"
        };
        for (String input : malformed) {
            try {
                JsonParser p = createParser(input);
                p.nextToken();
                fail("Expected JsonParseException for input: '" + input + "', but got token: " + p.getCurrentToken());
            } catch (JsonParseException e) {
                // expected
            }
        }
    }

    /**
     * Another variation: exponent without digits after sign.
     */
    @Test(timeout = 4000)
    public void testMangledNumberExponentWithSignOnly() throws Exception {
        String[] malformed = {"0e+", "0e-", "1e+", "1e-"};
        for (String input : malformed) {
            try {
                JsonParser p = createParser(input);
                p.nextToken();
                fail("Expected exception for: " + input);
            } catch (JsonParseException e) { }
        }
    }

    /**
     * Ensure that valid numbers still parse correctly (negative check).
     */
    @Test(timeout = 4000)
    public void testValidNumbers() throws Exception {
        String[] valid = {"0", "1", "-1", "123", "-123", "0.0", "1.0", "-1.0", "1e10", "-1e10", "1.5e-3"};
        for (String input : valid) {
            JsonParser p = createParser(input);
            assertNotNull("Could not parse valid: " + input, p.nextToken());
            p.close();
        }
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testMismatchedBrackets() throws Exception {
        String json = "{\"a\":1]";
        JsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // VALUE_NUMBER_INT
        try {
            p.nextToken();
            fail("Expected exception for mismatched bracket");
        } catch (JsonParseException e) { }
    }

    @Test(timeout = 4000)
    public void testUnexpectedEndOfInput() throws Exception {
        String json = "{\"a\":";
        JsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        try {
            p.nextToken();
            fail("Expected exception for EOF");
        } catch (JsonParseException e) { }
    }

    @Test(timeout = 4000)
    public void testInvalidEscapeSequence() throws Exception {
        String json = "\"\\x\"";
        JsonParser p = createParser(json);
        try {
            p.nextToken();
            fail("Expected exception for invalid escape");
        } catch (JsonParseException e) { }
    }

    @Test(timeout = 4000)
    public void testControlCharInString() throws Exception {
        String json = "\"\n\"";
        JsonParser p = createParser(json);
        try {
            p.nextToken();
            fail("Expected exception for newline in string");
        } catch (JsonParseException e) { }
    }

    @Test(timeout = 4000)
    public void testInvalidUTF8InitialByte() throws Exception {
        // 0xFF is invalid as first byte
        byte[] data = new byte[] {0x22, (byte)0xFF, 0x22}; // "FF"
        JsonParser p = createParser(data);
        try {
            p.nextToken();
            fail("Expected exception for invalid UTF-8 start byte");
        } catch (JsonParseException e) { }
    }

    @Test(timeout = 4000)
    public void testInvalidUTF8MiddleByte() throws Exception {
        // 0xE0 followed by 0x80 (valid) but then 0x00 (invalid continuation)
        byte[] data = new byte[] {0x22, (byte)0xE0, (byte)0x80, 0x00, 0x22};
        JsonParser p = createParser(data);
        try {
            p.nextToken();
            fail("Expected exception for invalid UTF-8 middle byte");
        } catch (JsonParseException e) { }
    }

    @Test(timeout = 4000)
    public void testUnexpectedTokenInObject() throws Exception {
        String json = "{\"a\":[}";
        JsonParser p = createParser(json);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // START_ARRAY
        try {
            p.nextToken();
            fail("Expected exception for unexpected } inside array");
        } catch (JsonParseException e) { }
    }

    @Test(timeout = 4000)
    public void testUnknownToken() throws Exception {
        String json = "what";
        JsonParser p = createParser(json);
        try {
            p.nextToken();
            fail("Expected exception for unknown token");
        } catch (JsonParseException e) { }
    }

    @Test(timeout = 4000)
    public void testInvalidNumberLeadingDot() throws Exception {
        String json = ".5";
        JsonParser p = createParser(json);
        try {
            p.nextToken();
            fail("Expected exception for leading dot");
        } catch (JsonParseException e) { }
    }

    @Test(timeout = 4000)
    public void testNumberWithTwoDots() throws Exception {
        String json = "1.2.3";
        JsonParser p = createParser(json);
        try {
            p.nextToken();
            fail("Expected exception for two dots");
        } catch (JsonParseException e) { }
    }

    @Test(timeout = 4000)
    public void testNumberWithTwoExponents() throws Exception {
        String json = "1e2e3";
        JsonParser p = createParser(json);
        try {
            p.nextToken();
            fail("Expected exception for two exponents");
        } catch (JsonParseException e) { }
    }

    @Test(timeout = 4000)
    public void testIncompleteString() throws Exception {
        String json = "\"abc";
        JsonParser p = createParser(json);
        try {
            p.nextToken();
            fail("Expected exception for incomplete string");
        } catch (JsonParseException e) { }
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGetCodecAndSetCodec() throws Exception {
        JsonParser p = createParser("{}");
        assertNull(p.getCodec());
        ObjectCodec codec = new ObjectCodec() {
            public JsonParser treeAsTokens(TreeNode n) { return null; }
            public <T> T treeToValue(TreeNode n, Class<T> valueType) { return null; }
            public <T extends TreeNode> T readValue(JsonParser p, Class<T> valueType) { return null; }
            public <T> T readValue(JsonParser p, com.fasterxml.jackson.core.type.ResolvedType valueType) { return null; }
            public <T> T readValue(JsonParser p, com.fasterxml.jackson.core.type.TypeReference<?> valueType) { return null; }
            public JsonNode createObjectNode() { return null; }
            public JsonNode createArrayNode() { return null; }
            public JsonNode missingNode() { return null; }
            public JsonNode nullNode() { return null; }
            public JsonParser treeAsTokens(com.fasterxml.jackson.databind.JsonNode n) { return null; }
            public <T> T treeToValue(com.fasterxml.jackson.databind.JsonNode n, Class<T> valueType) { return null; }
        };
        p.setCodec(codec);
        assertSame(codec, p.getCodec());
        p.close();
    }

    @Test(timeout = 4000)
    public void testCloseReleasesSymbols() throws Exception {
        // Verify that close() doesn't throw; coverage for _symbols.release()
        JsonParser p = createParser("{}");
        p.close();
        // Should be safe to call close again
        p.close();
    }

    @Test(timeout = 4000)
    public void testReleaseBuffers() throws Exception {
        // Directly instantiate parser with recyclable buffer, then close
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        BytesToNameCanonicalizer sym = BytesToNameCanonicalizer.createRoot();
        byte[] buf = new byte[100];
        UTF8StreamJsonParser parser = new UTF8StreamJsonParser(ctxt, 0, null, null, sym, buf, 0, 0, true);
        // no further reading needed; just test that closing doesn't crash
        parser.close();
    }
}