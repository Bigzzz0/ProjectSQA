package com.fasterxml.jackson.core.json;

import static org.junit.Assert.*;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.BufferRecycler;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - writeStartArray/writeEndArray, writeStartObject/writeEndObject
 *   - writeFieldName (String, SerializableString)
 *   - writeString (String, char[], SerializableString)
 *   - writeNumber (short, int, long, BigInteger, double, float, BigDecimal, String)
 *   - writeBoolean, writeNull
 *   - writeRaw (String, char[], char, SerializableString)
 *   - writeRawUTF8String, writeUTF8String
 *   - writeBinary (byte[], InputStream)
 *   - flush, close, getOutputTarget, getOutputBuffered
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - null arguments (writeString(null), writeNumber(null BigInteger/BigDecimal))
 *   - empty strings, very long strings (> _outputMaxContiguous)
 *   - special characters (quotes, backslash, control chars, surrogates)
 *   - numbers: Short.MIN_VALUE, Integer.MAX_VALUE, Long.MIN_VALUE, Double.NaN, Float.POSITIVE_INFINITY
 *   - BigDecimal with huge exponent (1E+10000) – defect target
 *   - Base64 encoding with line length boundaries
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - writeNumber(BigDecimal) with exponent > 9999 should throw IOException
 *   - The bug: no validation, writes huge number without exception
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - writeFieldName when expecting value (STATUS_EXPECT_VALUE)
 *   - writeEndArray when not in array
 *   - writeEndObject when not in object
 *   - writeValue after field name (STATUS_EXPECT_NAME)
 *   - Invalid custom escape definitions
 *   - Split surrogate in writeRaw
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - close with AUTO_CLOSE_JSON_CONTENT
 *   - release buffers after close
 *   - getOutputTarget returns OutputStream
 *   - getOutputBuffered returns tail
 */
public class UTF8JsonGeneratorDeepseekTest {

    private UTF8JsonGenerator createGenerator(OutputStream out) throws IOException {
        JsonFactory factory = new JsonFactory();
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        return (UTF8JsonGenerator) gen;
    }

    private UTF8JsonGenerator createGenerator(OutputStream out, int features) throws IOException {
        JsonFactory factory = new JsonFactory();
        factory.enable(features);
        JsonGenerator gen = factory.createGenerator(out, JsonEncoding.UTF8);
        return (UTF8JsonGenerator) gen;
    }

    // ===== Partition A: Core Functional Logic =====

    @Test(timeout = 4000)
    public void testWriteStartArrayEndArray() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeStartArray();
        gen.writeEndArray();
        gen.close();
        assertEquals("[]", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteStartObjectEndObject() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeStartObject();
        gen.writeEndObject();
        gen.close();
        assertEquals("{}", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteFieldNameString() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeStartObject();
        gen.writeFieldName("foo");
        gen.writeNumber(1);
        gen.writeEndObject();
        gen.close();
        assertEquals("{\"foo\":1}", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteFieldNameSerializableString() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeStartObject();
        gen.writeFieldName(new SerializedString("bar"));
        gen.writeBoolean(true);
        gen.writeEndObject();
        gen.close();
        assertEquals("{\"bar\":true}", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteStringNull() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeString((String) null);
        gen.close();
        assertEquals("null", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteStringEmpty() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeString("");
        gen.close();
        assertEquals("\"\"", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteStringSimple() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeString("hello");
        gen.close();
        assertEquals("\"hello\"", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteStringWithSpecialChars() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeString("a\"b\\c\td\ne");
        gen.close();
        assertEquals("\"a\\\"b\\\\c\\td\\ne\"", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteStringLong() throws IOException {
        // Build a string longer than _outputMaxContiguous (typically 512/8=64)
        StringBuilder sb = new StringBuilder(200);
        for (int i = 0; i < 200; i++) {
            sb.append('x');
        }
        String longStr = sb.toString();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeString(longStr);
        gen.close();
        assertEquals("\"" + longStr + "\"", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteStringCharArray() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        char[] text = "world".toCharArray();
        gen.writeString(text, 0, text.length);
        gen.close();
        assertEquals("\"world\"", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteStringSerializableString() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeString(new SerializedString("test"));
        gen.close();
        assertEquals("\"test\"", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteRawUTF8String() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        byte[] raw = "raw".getBytes("UTF-8");
        gen.writeRawUTF8String(raw, 0, raw.length);
        gen.close();
        assertEquals("\"raw\"", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteUTF8String() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        byte[] utf8 = "utf8".getBytes("UTF-8");
        gen.writeUTF8String(utf8, 0, utf8.length);
        gen.close();
        assertEquals("\"utf8\"", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteRawString() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeRaw("raw content");
        gen.close();
        assertEquals("raw content", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteRawCharArray() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        char[] chars = "raw chars".toCharArray();
        gen.writeRaw(chars, 0, chars.length);
        gen.close();
        assertEquals("raw chars", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteRawChar() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeRaw('A');
        gen.close();
        assertEquals("A", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberShort() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeNumber((short) 42);
        gen.close();
        assertEquals("42", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberInt() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeNumber(-1234567890);
        gen.close();
        assertEquals("-1234567890", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberLong() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeNumber(9876543210L);
        gen.close();
        assertEquals("9876543210", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberBigInteger() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeNumber(new BigInteger("12345678901234567890"));
        gen.close();
        assertEquals("12345678901234567890", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberBigIntegerNull() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeNumber((BigInteger) null);
        gen.close();
        assertEquals("null", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberDouble() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeNumber(3.14159);
        gen.close();
        assertTrue(bos.toString("UTF-8").contains("3.14159"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberDoubleNaN() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeNumber(Double.NaN);
        gen.close();
        assertEquals("\"NaN\"", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberFloat() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeNumber(2.718f);
        gen.close();
        assertTrue(bos.toString("UTF-8").contains("2.718"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberFloatInfinity() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeNumber(Float.POSITIVE_INFINITY);
        gen.close();
        assertEquals("\"Infinity\"", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberBigDecimal() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeNumber(new BigDecimal("123.456"));
        gen.close();
        assertEquals("123.456", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberBigDecimalNull() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeNumber((BigDecimal) null);
        gen.close();
        assertEquals("null", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberString() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeNumber("42");
        gen.close();
        assertEquals("42", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteBooleanTrue() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeBoolean(true);
        gen.close();
        assertEquals("true", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteBooleanFalse() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeBoolean(false);
        gen.close();
        assertEquals("false", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNull() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeNull();
        gen.close();
        assertEquals("null", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testFlush() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeStartArray();
        gen.flush();
        assertEquals("[", bos.toString("UTF-8"));
        gen.writeEndArray();
        gen.close();
    }

    @Test(timeout = 4000)
    public void testClose() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeStartObject();
        gen.writeFieldName("a");
        gen.writeNumber(1);
        gen.close();
        assertEquals("{\"a\":1}", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testGetOutputTarget() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        assertSame(bos, gen.getOutputTarget());
        gen.close();
    }

    @Test(timeout = 4000)
    public void testGetOutputBuffered() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        assertEquals(0, gen.getOutputBuffered());
        gen.writeStartArray();
        assertTrue(gen.getOutputBuffered() > 0);
        gen.close();
    }

    // ===== Partition B: Boundary Value Analysis =====

    @Test(timeout = 4000)
    public void testWriteStringWithControlChars() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeString("\u0000\u001f\u007f");
        gen.close();
        assertEquals("\"\\u0000\\u001f\\u007f\"", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteStringWithSurrogates() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        // Emoji: U+1F600 (😀) as surrogate pair
        gen.writeString("\uD83D\uDE00");
        gen.close();
        // Expected: escaped? Actually, by default surrogates are escaped as \uXXXX
        // The generator will escape each surrogate individually
        assertEquals("\"\\uD83D\\uDE00\"", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberShortMin() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeNumber(Short.MIN_VALUE);
        gen.close();
        assertEquals("-32768", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberIntMax() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeNumber(Integer.MAX_VALUE);
        gen.close();
        assertEquals("2147483647", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberLongMin() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeNumber(Long.MIN_VALUE);
        gen.close();
        assertEquals("-9223372036854775808", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberAsStringsEnabled() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos, JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask());
        gen.writeNumber(42);
        gen.close();
        assertEquals("\"42\"", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteFieldNameUnquoted() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        // Disable quote field names
        JsonFactory factory = new JsonFactory();
        factory.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        UTF8JsonGenerator gen = (UTF8JsonGenerator) factory.createGenerator(bos, JsonEncoding.UTF8);
        gen.writeStartObject();
        gen.writeFieldName("unquoted");
        gen.writeNumber(1);
        gen.writeEndObject();
        gen.close();
        assertEquals("{unquoted:1}", bos.toString("UTF-8"));
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    @Test(timeout = 4000, expected = IOException.class)
    public void testWriteNumberBigDecimalHugeExponent() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        // This should throw an exception because the exponent is too large
        gen.writeNumber(new BigDecimal("1E+10000"));
        gen.close();
        // If we reach here, the bug is present (no exception thrown)
        fail("Should have thrown IOException for huge exponent");
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000, expected = JsonGenerationException.class)
    public void testWriteFieldNameExpectingValue() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeStartObject();
        gen.writeFieldName("a");
        gen.writeFieldName("b"); // should fail: expecting value
        gen.close();
    }

    @Test(timeout = 4000, expected = JsonGenerationException.class)
    public void testWriteEndArrayNotInArray() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeEndArray(); // not in array
        gen.close();
    }

    @Test(timeout = 4000, expected = JsonGenerationException.class)
    public void testWriteEndObjectNotInObject() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeEndObject(); // not in object
        gen.close();
    }

    @Test(timeout = 4000, expected = JsonGenerationException.class)
    public void testWriteValueAfterFieldName() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeStartObject();
        gen.writeFieldName("a");
        gen.writeStartObject(); // should fail: expecting value, not start object
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteBinary() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        byte[] data = {1, 2, 3};
        gen.writeBinary(Base64Variants.getDefaultVariant(), data, 0, data.length);
        gen.close();
        String result = bos.toString("UTF-8");
        assertTrue(result.startsWith("\"") && result.endsWith("\""));
        // Base64 encoding of {1,2,3} is "AQID"
        assertEquals("\"AQID\"", result);
    }

    @Test(timeout = 4000)
    public void testWriteBinaryStream() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        byte[] data = {10, 20, 30, 40};
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        int written = gen.writeBinary(Base64Variants.getDefaultVariant(), bais, data.length);
        gen.close();
        assertEquals(data.length, written);
        String result = bos.toString("UTF-8");
        assertTrue(result.startsWith("\"") && result.endsWith("\""));
    }

    @Test(timeout = 4000)
    public void testWriteBinaryStreamUnknownLength() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        byte[] data = {100, 200};
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        int written = gen.writeBinary(Base64Variants.getDefaultVariant(), bais, -1);
        gen.close();
        assertEquals(data.length, written);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testCloseWithAutoCloseContent() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        JsonFactory factory = new JsonFactory();
        factory.enable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        UTF8JsonGenerator gen = (UTF8JsonGenerator) factory.createGenerator(bos, JsonEncoding.UTF8);
        gen.writeStartArray();
        gen.writeNumber(1);
        gen.close(); // should auto-close array
        assertEquals("[1]", bos.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testReleaseBuffers() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        gen.writeString("test");
        gen.close();
        // After close, buffers should be released (internal state)
        // We can't directly test, but no exception should occur
    }

    @Test(timeout = 4000)
    public void testWriteRawWithSurrogateSplit() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        // Write a high surrogate without low surrogate
        try {
            gen.writeRaw(new char[]{'\uD800'}, 0, 1);
            fail("Should have thrown IOException for split surrogate");
        } catch (IOException e) {
            // expected
        }
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteStringWithCustomEscapes() throws IOException {
        // This test requires setting custom character escapes
        // For simplicity, we test that the method is reachable
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(bos);
        // Set a custom escape for 'a' -> "\\u0061" (already default, but just to exercise)
        // We'll use a simple CharacterEscapes implementation
        gen.setCharacterEscapes(new CharacterEscapes() {
            @Override
            public int[] getEscapeCodesForAscii() {
                int[] esc = CharacterEscapes.standardAsciiEscapesForJSON();
                esc['a'] = CharacterEscapes.ESCAPE_CUSTOM;
                return esc;
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {
                if (ch == 'a') {
                    return new SerializedString("\\u0061");
                }
                return null;
            }
        });
        gen.writeString("a");
        gen.close();
        assertEquals("\"\\u0061\"", bos.toString("UTF-8"));
    }
}