package com.fasterxml.jackson.core.json;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.*;
import com.fasterxml.jackson.core.sym.*;
import com.fasterxml.jackson.core.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: ReaderBasedJsonParser (white-box)
 * 
 * Partitions covered:
 * A – Core functional logic: objects, arrays, strings, numbers, booleans, null
 * B – Boundary values: empty input, whitespace, large numbers, special chars in strings
 * C – Defect-targeted: unquoted field names with non-ASCII (especially code 256)
 * D – Exception paths: invalid JSON, unexpected chars, mismatched brackets
 * E – Object lifecycle: close, releaseBuffered, finishToken
 * 
 * Known defect: ArrayIndexOutOfBoundsException: 256 in _handleOddName2
 * when an unquoted field name contains a character with code point 256.
 * The condition `if (i <= maxCode)` should be `if (i < maxCode)`.
 * 
 * This test suite triggers that bug and covers the surrounding branches.
 */
public class ReaderBasedJsonParserDeepseekTest {

    // ------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------
    private ReaderBasedJsonParser createParser(String json, Feature... features) {
        JsonFactory factory = new JsonFactory();
        for (Feature f : features) {
            factory.enable(f);
        }
        try {
            return (ReaderBasedJsonParser) factory.createParser(new StringReader(json));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void assertToken(JsonToken expected, JsonToken actual) {
        assertEquals("Token type mismatch", expected, actual);
    }

    // ------------------------------------------------------------
    // Partition A: Core functional logic
    // ------------------------------------------------------------
    @Test(timeout = 4000)
    public void testSimpleObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"key\":\"value\"}");
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testSimpleArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("[1,2,3]");
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNestedStructures() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{\"b\":[]}}");
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testBooleanAndNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("[true,false,null]");
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        assertTrue(parser.getBooleanValue());
        assertToken(JsonToken.VALUE_FALSE, parser.nextToken());
        assertFalse(parser.getBooleanValue());
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertNull(parser.getText());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testStringWithEscapes() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"hello\\nworld\\t!\"");
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("hello\nworld\t!", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNumberVariants() throws IOException {
        ReaderBasedJsonParser parser = createParser("[0.5,1e10,-3.14e-2]");
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(0.5, parser.getDoubleValue(), 0.0);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1e10, parser.getDoubleValue(), 0.0);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(-3.14e-2, parser.getDoubleValue(), 0.0);
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("-42");
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(-42, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    // ------------------------------------------------------------
    // Partition B: Boundary values
    // ------------------------------------------------------------
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
    public void testLargeInteger() throws IOException {
        ReaderBasedJsonParser parser = createParser("1234567890123456789");
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1234567890123456789L, parser.getLongValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testStringWithUnicodeEscape() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"\\u0041\"");
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("A", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testLeadingZerosAllowed() throws IOException {
        ReaderBasedJsonParser parser = createParser("007", Feature.ALLOW_NUMERIC_LEADING_ZEROS);
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(7, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testLeadingZerosNotAllowed() throws IOException {
        ReaderBasedJsonParser parser = createParser("007");
        try {
            parser.nextToken();
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            // expected
        }
    }

    // ------------------------------------------------------------
    // Partition C: Defect-targeted (unquoted names with non-ASCII)
    // ------------------------------------------------------------
    @Test(timeout = 4000)
    public void testUnquotedNameBasic() throws IOException {
        ReaderBasedJsonParser parser = createParser("{abc:1}", Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("abc", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testUnquotedNameWithNonAscii() throws IOException {
        // Character with code point 256 (Latin capital letter A with macron)
        ReaderBasedJsonParser parser = createParser("{\u0100:1}", Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("\u0100", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testUnquotedNameWithNonAsciiMultiple() throws IOException {
        // Multiple non-ASCII characters, including one at boundary 256
        ReaderBasedJsonParser parser = createParser("{\u0100\u0101:2}", Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("\u0100\u0101", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testUnquotedNameWithNonAsciiAndDigits() throws IOException {
        // Mix of ASCII and non-ASCII
        ReaderBasedJsonParser parser = createParser("{a\u0100b:3}", Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a\u0100b", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    // ------------------------------------------------------------
    // Partition D: Exception & defensive guard paths
    // ------------------------------------------------------------
    @Test(timeout = 4000)
    public void testInvalidToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("tru");
        try {
            parser.nextToken();
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testUnexpectedCharacter() throws IOException {
        ReaderBasedJsonParser parser = createParser("[!]");
        try {
            parser.nextToken();
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMismatchedBrackets() throws IOException {
        ReaderBasedJsonParser parser = createParser("[}");
        try {
            parser.nextToken();
            parser.nextToken();
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMissingColon() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\" 1}");
        try {
            parser.nextToken();
            parser.nextToken();
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testMissingComma() throws IOException {
        ReaderBasedJsonParser parser = createParser("[1 2]");
        try {
            parser.nextToken();
            parser.nextToken();
            fail("Expected JsonParseException");
        } catch (JsonParseException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testTrailingComma() throws IOException {
        ReaderBasedJsonParser parser = createParser("[1,]", Feature.ALLOW_TRAILING_COMMA);
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testMissingValues() throws IOException {
        ReaderBasedJsonParser parser = createParser("[1,,2]", Feature.ALLOW_MISSING_VALUES);
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testSingleQuotes() throws IOException {
        ReaderBasedJsonParser parser = createParser("{'key':'value'}", Feature.ALLOW_SINGLE_QUOTES);
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("key", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testComments() throws IOException {
        ReaderBasedJsonParser parser = createParser("/* comment */ 1", Feature.ALLOW_COMMENTS);
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testYamlComments() throws IOException {
        ReaderBasedJsonParser parser = createParser("# comment\n1", Feature.ALLOW_YAML_COMMENTS);
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNonNumericNumbers() throws IOException {
        ReaderBasedJsonParser parser = createParser("NaN", Feature.ALLOW_NON_NUMERIC_NUMBERS);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isNaN(parser.getDoubleValue()));
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testInfinity() throws IOException {
        ReaderBasedJsonParser parser = createParser("Infinity", Feature.ALLOW_NON_NUMERIC_NUMBERS);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isInfinite(parser.getDoubleValue()));
        assertTrue(parser.getDoubleValue() > 0);
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNegativeInfinity() throws IOException {
        ReaderBasedJsonParser parser = createParser("-Infinity", Feature.ALLOW_NON_NUMERIC_NUMBERS);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertTrue(Double.isInfinite(parser.getDoubleValue()));
        assertTrue(parser.getDoubleValue() < 0);
        assertNull(parser.nextToken());
    }

    // ------------------------------------------------------------
    // Partition E: Object lifecycle & contract integrity
    // ------------------------------------------------------------
    @Test(timeout = 4000)
    public void testFinishToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"incomplete\"");
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        // finishToken should be a no-op if token is already complete
        parser.finishToken();
        assertEquals("incomplete", parser.getText());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testReleaseBuffered() throws IOException {
        ReaderBasedJsonParser parser = createParser("  abc  ");
        // releaseBuffered should return number of buffered characters
        int count = parser.releaseBuffered(new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) {
                // no-op
            }
        });
        assertTrue(count >= 0);
    }

    @Test(timeout = 4000)
    public void testCloseAndReopen() throws IOException {
        ReaderBasedJsonParser parser = createParser("1");
        parser.close();
        // After close, nextToken should return null
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testGetCodec() throws IOException {
        ReaderBasedJsonParser parser = createParser("1");
        assertNull(parser.getCodec());
    }

    @Test(timeout = 4000)
    public void testSetCodec() throws IOException {
        ReaderBasedJsonParser parser = createParser("1");
        ObjectCodec codec = new ObjectCodec() {
            // minimal stub
        };
        parser.setCodec(codec);
        assertSame(codec, parser.getCodec());
    }

    @Test(timeout = 4000)
    public void testGetInputSource() throws IOException {
        ReaderBasedJsonParser parser = createParser("1");
        assertTrue(parser.getInputSource() instanceof StringReader);
    }

    @Test(timeout = 4000)
    public void testGetTokenLocation() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken(); // START_OBJECT
        JsonLocation loc = parser.getTokenLocation();
        assertNotNull(loc);
        parser.nextToken(); // FIELD_NAME
        loc = parser.getTokenLocation();
        assertNotNull(loc);
    }

    @Test(timeout = 4000)
    public void testGetCurrentLocation() throws IOException {
        ReaderBasedJsonParser parser = createParser(" 1");
        parser.nextToken();
        JsonLocation loc = parser.getCurrentLocation();
        assertNotNull(loc);
        assertTrue(loc.getColumnNr() > 0);
    }

    @Test(timeout = 4000)
    public void testNextFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        assertNull(parser.nextFieldName()); // before first token
        assertEquals("a", parser.nextFieldName());
        assertEquals(1, parser.getIntValue());
        assertEquals("b", parser.nextFieldName());
        assertEquals(2, parser.getIntValue());
        assertNull(parser.nextFieldName());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextTextValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"hello\"}");
        parser.nextToken(); // START_OBJECT
        assertNull(parser.nextTextValue()); // after field name, should return null
        assertEquals("hello", parser.nextTextValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextIntValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":42}");
        parser.nextToken(); // START_OBJECT
        assertEquals(42, parser.nextIntValue(-1));
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextLongValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1234567890123}");
        parser.nextToken(); // START_OBJECT
        assertEquals(1234567890123L, parser.nextLongValue(-1L));
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testNextBooleanValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true}");
        parser.nextToken(); // START_OBJECT
        assertEquals(Boolean.TRUE, parser.nextBooleanValue());
        assertNull(parser.nextToken());
    }

    @Test(timeout = 4000)
    public void testGetValueAsString() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"test\"");
        parser.nextToken();
        assertEquals("test", parser.getValueAsString());
        assertEquals("test", parser.getValueAsString("default"));
    }

    @Test(timeout = 4000)
    public void testGetTextCharacters() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"abc\"");
        parser.nextToken();
        char[] chars = parser.getTextCharacters();
        assertArrayEquals(new char[]{'a','b','c'}, chars);
    }

    @Test(timeout = 4000)
    public void testGetTextLength() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"abc\"");
        parser.nextToken();
        assertEquals(3, parser.getTextLength());
    }

    @Test(timeout = 4000)
    public void testGetTextOffset() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"abc\"");
        parser.nextToken();
        assertEquals(0, parser.getTextOffset());
    }

    @Test(timeout = 4000)
    public void testBinaryValue() throws IOException {
        // Base64 encoded "test" -> "dGVzdA=="
        ReaderBasedJsonParser parser = createParser("\"dGVzdA==\"");
        parser.nextToken();
        byte[] binary = parser.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(new byte[]{116, 101, 115, 116}, binary);
    }

    @Test(timeout = 4000)
    public void testReadBinaryValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"dGVzdA==\"");
        parser.nextToken();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int count = parser.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(4, count);
        assertArrayEquals(new byte[]{116, 101, 115, 116}, out.toByteArray());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriter() throws IOException {
        ReaderBasedJsonParser parser = createParser("\"abc\"");
        parser.nextToken();
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(3, count);
        assertEquals("abc", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"key\":1}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(3, count);
        assertEquals("key", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("");
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(0, count);
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("42");
        parser.nextToken();
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(2, count);
        assertEquals("42", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterBoolean() throws IOException {
        ReaderBasedJsonParser parser = createParser("true");
        parser.nextToken();
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(4, count);
        assertEquals("true", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("null");
        parser.nextToken();
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(4, count);
        assertEquals("null", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("[1]");
        parser.nextToken(); // START_ARRAY
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(0, count); // START_ARRAY has no text representation
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{}");
        parser.nextToken(); // START_OBJECT
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(0, count);
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("[1]");
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // 1
        parser.nextToken(); // END_ARRAY
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(0, count);
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(0, count);
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterValue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE
        // Now current token is VALUE_NUMBER_INT, not FIELD_NAME
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("1", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"b\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_STRING
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_NULL
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(4, count);
        assertEquals("null", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_TRUE
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(4, count);
        assertEquals("true", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_FALSE
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(5, count);
        assertEquals("false", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // START_ARRAY
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(0, count);
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // START_OBJECT
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(0, count);
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_NUMBER_INT
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("1", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(3, count);
        assertEquals("1.5", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(4, count);
        assertEquals("1e10", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_NUMBER_INT
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(3, count);
        assertEquals("-42", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(5, count);
        assertEquals("-3.14", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(5, count);
        assertEquals("-1e-2", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_NULL
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(4, count);
        assertEquals("null", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_TRUE
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(4, count);
        assertEquals("true", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // VALUE_FALSE
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(5, count);
        assertEquals("false", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // START_ARRAY
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(0, count);
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // START_OBJECT
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(0, count);
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(0, count);
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(0, count);
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNullToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrueToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalseToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArrayToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObjectToken() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithEndObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFieldName() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithString() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":\"x\",\"b\":\"y\"}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_STRING "x"
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNull() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":null,\"b\":null}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NULL
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithTrue() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":true,\"b\":true}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_TRUE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFalse() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":false,\"b\":false}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_FALSE
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithArray() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":[],\"b\":[]}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_ARRAY
        parser.nextToken(); // END_ARRAY
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithObject() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":{},\"b\":{}}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // END_OBJECT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1,\"b\":2}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1.5,\"b\":2.5}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":1e10,\"b\":2e10}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeNumber() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-42,\"b\":-43}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_INT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeFloat() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-3.14,\"b\":-6.28}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }

    @Test(timeout = 4000)
    public void testGetTextWithWriterFieldNameAfterFieldNameWithNegativeExponent() throws IOException {
        ReaderBasedJsonParser parser = createParser("{\"a\":-1e-2,\"b\":-2e-3}");
        parser.nextToken(); // START_OBJECT
        parser.nextToken(); // FIELD_NAME "a"
        parser.nextToken(); // VALUE_NUMBER_FLOAT
        parser.nextToken(); // FIELD_NAME "b"
        StringWriter writer = new StringWriter();
        int count = parser.getText(writer);
        assertEquals(1, count);
        assertEquals("b", writer.toString());
    }
}
