package com.fasterxml.jackson.core.json;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. DEFECT UNDER TEST (Defects4J):
 *    - Target: writeNumber(BigDecimal value)
 *    - Root cause: When Feature.WRITE_BIGDECIMAL_AS_PLAIN is enabled, UTF8JsonGenerator
 *      directly invokes `value.toPlainString()` instead of `_asString(value)`, bypassing
 *      the scale limit verification implemented in JsonGeneratorImpl._asString(BigDecimal).
 *      As a result, writing numbers with huge scale (e.g., 1E+10000) does not throw the
 *      expected JsonGenerationException.
 *
 * 2. STRUCTURAL BRANCH COVERAGE TARGETS:
 *    - writeFieldName(String / SerializableString):
 *        * Context check: STATUS_EXPECT_VALUE -> JsonGenerationException.
 *        * Comma insertion: STATUS_OK_AFTER_COMMA.
 *        * Pretty printer vs fast-path.
 *        * Quoted vs Unquoted field names (_cfgUnqNames).
 *        * Short names (<= _outputMaxContiguous) vs segmented names (> _outputMaxContiguous).
 *        * Names larger than _charBufferLength.
 *        * SerializableString.appendQuotedUTF8: len >= 0 vs len < 0 fallback.
 *    - writeStartArray / writeEndArray & writeStartObject / writeEndObject:
 *        * Context mismatch checks (ending array in object or object in array).
 *        * Pretty printer callbacks vs standard brackets/braces.
 *    - writeString(String / char[] / SerializableString / byte[]):
 *        * Null strings -> _writeNull().
 *        * ASCII fast path, 2-byte UTF-8, 3-byte UTF-8, surrogate pairs (4-byte).
 *        * Escaped chars (control characters, backslash, quote).
 *        * Feature.ESCAPE_NON_ASCII enabled (_writeStringSegmentASCII2).
 *        * Custom CharacterEscapes (_writeCustomStringSegment2, long custom escapes > 6 bytes).
 *        * Missing custom escape definition error path.
 *    - writeRaw(String / char[] / char / SerializableString):
 *        * Small buffers vs large buffers exceeding _charBuffer.
 *        * Segmentation and surrogate split handling at chunk boundary.
 *        * Surrogate pair decoding and split surrogate reporting.
 *        * Raw byte writes exceeding MAX_BYTES_TO_BUFFER (512 bytes).
 *    - writeBinary(Base64Variant, byte[] / InputStream):
 *        * Empty, 1-byte, 2-byte, full 3-byte triplets.
 *        * Multiline output with Base64 maxLineLength and LF insertion.
 *        * InputStream with known length vs unknown length (-1).
 *        * InputStream underflow (too few bytes available).
 *    - writeNumber (short, int, long, BigInteger, double, float, BigDecimal, encoded String):
 *        * Feature.WRITE_NUMBERS_AS_STRINGS enabled vs disabled.
 *        * NaN and Infinite double/float with Feature.QUOTE_NON_NUMERIC_NUMBERS.
 *        * Null BigInteger and BigDecimal.
 *        * Normal and extreme BigDecimal values (plain vs standard).
 *    - Lifecycle & Buffer Management:
 *        * AUTO_CLOSE_JSON_CONTENT auto-closing open scopes on close().
 *        * AUTO_CLOSE_TARGET closing underlying stream.
 *        * Buffer recycling (_bufferRecyclable true vs false).
 *        * Flush buffer on demand and when full.
 */
public class UTF8JsonGeneratorGeminiTest {

    private UTF8JsonGenerator createGenerator(ByteArrayOutputStream out) {
        IOContext ctxt = new IOContext(new BufferRecycler(), out, false);
        return new UTF8JsonGenerator(ctxt, 0, null, out);
    }

    private UTF8JsonGenerator createGenerator(ByteArrayOutputStream out, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), out, false);
        return new UTF8JsonGenerator(ctxt, features, null, out);
    }

    private UTF8JsonGenerator createSmallBufferGenerator(ByteArrayOutputStream out, int bufferSize) {
        IOContext ctxt = new IOContext(new BufferRecycler(), out, false);
        byte[] buf = new byte[bufferSize];
        return new UTF8JsonGenerator(ctxt, 0, null, out, buf, 0, false);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Defect)
    // =========================================================================

    /**
     * Targets the defect where UTF8JsonGenerator.writeNumber(BigDecimal) bypasses
     * `_asString(BigDecimal)` check when WRITE_BIGDECIMAL_AS_PLAIN is enabled,
     * failing to throw an exception for excessively large scale/exponent.
     */
    @Test(timeout = 4000)
    public void testDefectTooBigBigDecimalWithPlainEnabled() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int features = JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.getMask();
        UTF8JsonGenerator gen = createGenerator(out, features);

        BigDecimal tooBig = new BigDecimal("1E+10000");
        try {
            gen.writeNumber(tooBig);
            fail("Should not have written without exception: 1E+10000");
        } catch (JsonGenerationException e) {
            assertTrue(e.getMessage().contains("BigDecimal") || e.getMessage().contains("scale")
                    || e.getMessage().contains("plain"));
        } finally {
            gen.close();
        }
    }

    @Test(timeout = 4000)
    public void testDefectTooBigBigDecimalWithPlainAndNumbersAsStrings() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int features = JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.getMask()
                | JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask();
        UTF8JsonGenerator gen = createGenerator(out, features);

        BigDecimal tooBig = new BigDecimal("1E+10000");
        try {
            gen.writeNumber(tooBig);
            fail("Should not have written without exception: 1E+10000");
        } catch (JsonGenerationException e) {
            assertTrue(e.getMessage().contains("BigDecimal") || e.getMessage().contains("scale")
                    || e.getMessage().contains("plain"));
        } finally {
            gen.close();
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testBasicObjectAndArrayLifecycle() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        assertEquals(out, gen.getOutputTarget());
        assertEquals(0, gen.getOutputBuffered());

        gen.writeStartObject();
        gen.writeFieldName("num");
        gen.writeNumber(42);
        gen.writeFieldName("boolTrue");
        gen.writeBoolean(true);
        gen.writeFieldName("boolFalse");
        gen.writeBoolean(false);
        gen.writeFieldName("nil");
        gen.writeNull();

        gen.writeFieldName("arr");
        gen.writeStartArray();
        gen.writeNumber((short) 1);
        gen.writeNumber(100L);
        gen.writeNumber(3.14159);
        gen.writeNumber(2.71828f);
        gen.writeEndArray();

        gen.writeEndObject();
        gen.close();

        String json = out.toString("UTF-8");
        assertEquals("{\"num\":42,\"boolTrue\":true,\"boolFalse\":false,\"nil\":null,\"arr\":[1,100,3.14159,2.71828]}", json);
    }

    @Test(timeout = 4000)
    public void testWriteFieldNameVariations() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartObject();
        gen.writeFieldName("first");
        gen.writeString("value1");
        // Second field tests comma insertion
        gen.writeFieldName(new SerializedString("second"));
        gen.writeString("value2");

        // SerializableString with negative append length fallback
        SerializableString fallbackStr = new SerializedString("third") {
            @Override
            public int appendQuotedUTF8(byte[] buffer, int offset) {
                return -1;
            }
        };
        gen.writeFieldName(fallbackStr);
        gen.writeString("value3");

        gen.writeEndObject();
        gen.close();

        assertEquals("{\"first\":\"value1\",\"second\":\"value2\",\"third\":\"value3\"}", out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testUnquotedFieldNames() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);

        gen.writeStartObject();
        gen.writeFieldName("unquoted1");
        gen.writeNumber(1);
        gen.writeFieldName(new SerializedString("unquoted2"));
        gen.writeNumber(2);

        SerializableString fallbackStr = new SerializedString("unquoted3") {
            @Override
            public int appendQuotedUTF8(byte[] buffer, int offset) {
                return -1;
            }
        };
        gen.writeFieldName(fallbackStr);
        gen.writeNumber(3);
        gen.writeEndObject();
        gen.close();

        assertEquals("{unquoted1:1,unquoted2:2,unquoted3:3}", out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testPrettyPrinterPaths() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.setPrettyPrinter(new DefaultPrettyPrinter());

        gen.writeStartObject();
        gen.writeFieldName("ppField1");
        gen.writeNumber(10);
        gen.writeFieldName(new SerializedString("ppField2"));
        gen.writeStartArray();
        gen.writeString("elem");
        gen.writeEndArray();
        gen.writeEndObject();
        gen.close();

        String json = out.toString("UTF-8");
        assertTrue(json.contains("\n"));
        assertTrue(json.contains("ppField1"));
        assertTrue(json.contains("ppField2"));
    }

    @Test(timeout = 4000)
    public void testPrettyPrinterUnquotedFieldNames() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.setPrettyPrinter(new DefaultPrettyPrinter());
        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);

        gen.writeStartObject();
        gen.writeFieldName("rawField1");
        gen.writeNumber(1);
        gen.writeFieldName(new SerializedString("rawField2"));
        gen.writeNumber(2);
        gen.writeEndObject();
        gen.close();

        String json = out.toString("UTF-8");
        assertTrue(json.contains("rawField1 : 1"));
    }

    @Test(timeout = 4000)
    public void testNumbersAsStrings() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int features = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask();
        UTF8JsonGenerator gen = createGenerator(out, features);

        gen.writeStartArray();
        gen.writeNumber((short) 12);
        gen.writeNumber(12345);
        gen.writeNumber(9876543210L);
        gen.writeNumber(new BigInteger("99999999999999999999"));
        gen.writeNumber(1.5);
        gen.writeNumber(2.5f);
        gen.writeNumber(new BigDecimal("123.45"));
        gen.writeNumber("987.65");
        gen.writeEndArray();
        gen.close();

        String expected = "[\"12\",\"12345\",\"9876543210\",\"99999999999999999999\",\"1.5\",\"2.5\",\"123.45\",\"987.65\"]";
        assertEquals(expected, out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testNonNumericDoublesAndFloats() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.enable(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS);

        gen.writeStartArray();
        gen.writeNumber(Double.NaN);
        gen.writeNumber(Double.POSITIVE_INFINITY);
        gen.writeNumber(Float.NaN);
        gen.writeNumber(Float.NEGATIVE_INFINITY);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"NaN\",\"Infinity\",\"NaN\",\"-Infinity\"]", out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNumberNulls() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        gen.writeNumber((BigInteger) null);
        gen.writeNumber((BigDecimal) null);
        gen.writeEndArray();
        gen.close();

        assertEquals("[null,null]", out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteStringVariousEncodings() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        gen.writeString((String) null);
        gen.writeString("Hello World!");
        gen.writeString("Quote: \", Backslash: \\, Escapes: \n\r\t\b\f\u0000\u001F");
        gen.writeString("2-byte UTF8: \u00C4\u00E9, 3-byte UTF8: \u4E2D\u6587");
        gen.writeString("4-byte Surrogate: \uD83D\uDE00"); // 😀

        char[] charBuf = "charBufferString".toCharArray();
        gen.writeString(charBuf, 0, charBuf.length);

        gen.writeString(new SerializedString("serializableText"));
        SerializableString negFallback = new SerializedString("fallbackText") {
            @Override
            public int appendQuotedUTF8(byte[] buffer, int offset) {
                return -1;
            }
        };
        gen.writeString(negFallback);

        byte[] rawUtf8 = "rawUtf8String".getBytes("UTF-8");
        gen.writeRawUTF8String(rawUtf8, 0, rawUtf8.length);

        byte[] utf8WithEscapes = "utf8\"needs\\escapes\n".getBytes("UTF-8");
        gen.writeUTF8String(utf8WithEscapes, 0, utf8WithEscapes.length);

        gen.writeEndArray();
        gen.close();

        String json = out.toString("UTF-8");
        assertTrue(json.contains("null"));
        assertTrue(json.contains("Hello World!"));
        assertTrue(json.contains("\\\""));
        assertTrue(json.contains("\\n"));
        assertTrue(json.contains("charBufferString"));
        assertTrue(json.contains("serializableText"));
        assertTrue(json.contains("fallbackText"));
        assertTrue(json.contains("rawUtf8String"));
        assertTrue(json.contains("utf8\\\"needs\\\\escapes\\n"));
    }

    @Test(timeout = 4000)
    public void testEscapeNonAsciiFeature() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int features = JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        UTF8JsonGenerator gen = createGenerator(out, features);

        gen.writeStartArray();
        gen.writeString("\u00E9\u4E2D");
        char[] chars = "\u00E9\u4E2D".toCharArray();
        gen.writeString(chars, 0, chars.length);
        gen.writeEndArray();
        gen.close();

        String json = out.toString("UTF-8");
        assertTrue(json.contains("\\u00e9"));
        assertTrue(json.contains("\\u4e2d"));
    }

    @Test(timeout = 4000)
    public void testCustomCharacterEscapes() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        CharacterEscapes customEscapes = new CharacterEscapes() {
            private final int[] ascii;
            {
                ascii = CharacterEscapes.standardAsciiEscapesForJSON();
                ascii['a'] = CharacterEscapes.ESCAPE_CUSTOM;
            }

            @Override
            public int[] getEscapeCodesForAscii() {
                return ascii;
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {
                if (ch == 'a') {
                    return new SerializedString("[CUSTOM_A]");
                }
                if (ch == 0x4E2D) {
                    return new SerializedString("[LONG_CUSTOM_ESCAPE_FOR_CHINESE]");
                }
                return null;
            }
        };

        gen.setCharacterEscapes(customEscapes);
        gen.writeStartArray();
        gen.writeString("cat");
        gen.writeString("\u4E2D");
        char[] chars = "apple\u4E2D".toCharArray();
        gen.writeString(chars, 0, chars.length);
        gen.writeEndArray();
        gen.close();

        String json = out.toString("UTF-8");
        assertTrue(json.contains("c[CUSTOM_A]t"));
        assertTrue(json.contains("[LONG_CUSTOM_ESCAPE_FOR_CHINESE]"));
        assertTrue(json.contains("[CUSTOM_A]pple[LONG_CUSTOM_ESCAPE_FOR_CHINESE]"));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Buffer Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSmallBufferTriggersSegmentedStringWrites() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        // Buffer size 64 -> max contiguous is 8 bytes
        UTF8JsonGenerator gen = createSmallBufferGenerator(out, 64);

        gen.writeStartArray();
        String text = "This is a longer string designed to exceed the contiguous buffer boundary easily.";
        gen.writeString(text);
        char[] chars = text.toCharArray();
        gen.writeString(chars, 0, chars.length);

        byte[] utf8 = text.getBytes("UTF-8");
        gen.writeUTF8String(utf8, 0, utf8.length);

        gen.writeEndArray();
        gen.close();

        String json = out.toString("UTF-8");
        assertTrue(json.contains(text));
    }

    @Test(timeout = 4000)
    public void testWriteRawSegmentedAndSurrogates() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createSmallBufferGenerator(out, 32);

        gen.writeRaw('[');
        // Multi-byte 2-byte char, 3-byte char
        gen.writeRaw('\u00A2');
        gen.writeRaw('\u20AC');

        // Raw char array with surrogate pair
        char[] surrogates = new char[] { '\uD83D', '\uDE00', 'a', 'b', 'c' };
        gen.writeRaw(surrogates, 0, surrogates.length);

        // Segmented raw write across buffer
        char[] largeRaw = new char[100];
        for (int i = 0; i < largeRaw.length; i++) {
            largeRaw[i] = (char) ('0' + (i % 10));
        }
        gen.writeRaw(largeRaw, 0, largeRaw.length);

        gen.writeRaw(']');
        gen.close();

        String result = out.toString("UTF-8");
        assertTrue(result.contains("\u00A2"));
        assertTrue(result.contains("\u20AC"));
        assertTrue(result.contains("\uD83D\uDE00"));
        assertTrue(result.contains("0123456789"));
    }

    @Test(timeout = 4000)
    public void testWriteRawStringLongerThanConcatBuffer() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3000; i++) {
            sb.append((char) ('A' + (i % 26)));
        }
        // Include surrogate near chunk boundary to test split surrogate guard
        sb.insert(1000, "\uD83D\uDE00");

        gen.writeStartArray();
        gen.writeRaw(sb.toString());
        gen.writeEndArray();
        gen.close();

        String result = out.toString("UTF-8");
        assertTrue(result.contains("\uD83D\uDE00"));
        assertEquals(3004, result.length()); // [ + 3002 chars + ]
    }

    @Test(timeout = 4000)
    public void testWriteRawSerializableStrings() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        gen.writeRaw(new SerializedString("123,"));
        gen.writeRawValue(new SerializedString("456"));
        gen.writeRaw(new SerializedString("")); // Empty writeRaw check
        gen.writeRawValue(new SerializedString("")); // Empty writeRawValue check
        gen.writeEndArray();
        gen.close();

        assertEquals("[123,456]", out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testLargeRawUtf8BytesDirectStreamWrite() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        // Byte array > MAX_BYTES_TO_BUFFER (512)
        byte[] bytes = new byte[1024];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) 'X';
        }
        gen.writeStartArray();
        gen.writeRawUTF8String(bytes, 0, bytes.length);
        gen.writeEndArray();
        gen.close();

        String result = out.toString("UTF-8");
        assertTrue(result.startsWith("[\""));
        assertTrue(result.endsWith("\"]"));
        assertEquals(1028, result.length());
    }

    @Test(timeout = 4000)
    public void testBinaryEncodingVariants() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        Base64Variant variant = Base64Variants.MIME;
        byte[] data = new byte[] { 1, 2, 3, 4, 5, 6, 7 };

        gen.writeStartArray();
        // Byte array chunks: partial leftover 1, leftover 2, full triplet
        gen.writeBinary(variant, data, 0, 0); // 0 bytes
        gen.writeBinary(variant, data, 0, 1); // 1 byte
        gen.writeBinary(variant, data, 0, 2); // 2 bytes
        gen.writeBinary(variant, data, 0, 3); // 3 bytes
        gen.writeBinary(variant, data, 0, 7); // 7 bytes

        // Long binary causing line feeds (MIME line length = 76)
        byte[] longData = new byte[200];
        for (int i = 0; i < longData.length; i++) {
            longData[i] = (byte) i;
        }
        gen.writeBinary(variant, longData, 0, longData.length);

        // InputStream variant: known length
        InputStream in1 = new ByteArrayInputStream(data);
        int written1 = gen.writeBinary(variant, in1, 5);
        assertEquals(5, written1);

        // InputStream variant: unknown length (-1)
        InputStream in2 = new ByteArrayInputStream(data);
        int written2 = gen.writeBinary(variant, in2, -1);
        assertEquals(7, written2);

        gen.writeEndArray();
        gen.close();

        String json = out.toString("UTF-8");
        assertTrue(json.contains("\"\""));
        assertTrue(json.contains("\\n")); // Verifies base64 line break insertion
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testErrorWritingFieldNameInValueContext() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        try {
            gen.writeFieldName("invalid");
            fail("Expected JsonGenerationException when writing field name in array");
        } catch (JsonGenerationException expected) {
            assertTrue(expected.getMessage().contains("Can not write a field name"));
        } finally {
            gen.close();
        }
    }

    @Test(timeout = 4000)
    public void testErrorWritingValueWhenExpectingFieldName() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartObject();
        try {
            gen.writeString("orphanValue");
            fail("Expected JsonGenerationException when writing value without field name");
        } catch (JsonGenerationException expected) {
            assertTrue(expected.getMessage().contains("expecting field name"));
        } finally {
            gen.close();
        }
    }

    @Test(timeout = 4000)
    public void testErrorEndingArrayWhenInObjectContext() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartObject();
        try {
            gen.writeEndArray();
            fail("Expected JsonGenerationException when ending array inside object context");
        } catch (JsonGenerationException expected) {
            assertTrue(expected.getMessage().contains("Current context not an ARRAY but OBJECT"));
        } finally {
            gen.close();
        }
    }

    @Test(timeout = 4000)
    public void testErrorEndingObjectWhenInArrayContext() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        gen.writeStartArray();
        try {
            gen.writeEndObject();
            fail("Expected JsonGenerationException when ending object inside array context");
        } catch (JsonGenerationException expected) {
            assertTrue(expected.getMessage().contains("Current context not an object but ARRAY"));
        } finally {
            gen.close();
        }
    }

    @Test(timeout = 4000)
    public void testErrorSplitSurrogateInWriteRaw() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        try {
            gen.writeRaw(new char[] { '\uD83D' }, 0, 1);
            fail("Expected JsonGenerationException for lone lead surrogate");
        } catch (JsonGenerationException expected) {
            assertTrue(expected.getMessage().contains("Split surrogate on writeRaw() input"));
        } finally {
            gen.close();
        }
    }

    @Test(timeout = 4000)
    public void testErrorMissingCustomEscapeSequence() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        CharacterEscapes faultyEscapes = new CharacterEscapes() {
            @Override
            public int[] getEscapeCodesForAscii() {
                int[] ascii = CharacterEscapes.standardAsciiEscapesForJSON();
                ascii['z'] = CharacterEscapes.ESCAPE_CUSTOM;
                return ascii;
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {
                return null; // Violates contract for 'z'
            }
        };

        gen.setCharacterEscapes(faultyEscapes);
        try {
            gen.writeString("zoo");
            fail("Expected JsonGenerationException when custom escape sequence is null");
        } catch (JsonGenerationException expected) {
            assertTrue(expected.getMessage().contains("Invalid custom escape definitions"));
        } finally {
            gen.close();
        }
    }

    @Test(timeout = 4000)
    public void testErrorBinaryInputStreamUnderflow() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);

        byte[] data = new byte[] { 1, 2 };
        InputStream in = new ByteArrayInputStream(data);

        try {
            gen.writeBinary(Base64Variants.MIME, in, 10);
            fail("Expected JsonGenerationException for stream missing expected bytes");
        } catch (JsonGenerationException expected) {
            assertTrue(expected.getMessage().contains("Too few bytes available"));
        } finally {
            gen.close();
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Buffer/Stream Guards
    // =========================================================================

    @Test(timeout = 4000)
    public void testAutoCloseJsonContent() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = createGenerator(out);
        gen.enable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);

        gen.writeStartObject();
        gen.writeFieldName("unclosedArray");
        gen.writeStartArray();
        gen.writeNumber(1);

        // Closing generator should automatically close the open array and object
        gen.close();

        assertEquals("{\"unclosedArray\":[1]}", out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testFlushAndStreamClosure() throws Exception {
        final boolean[] streamClosed = new boolean[] { false };
        final boolean[] streamFlushed = new boolean[] { false };

        ByteArrayOutputStream out = new ByteArrayOutputStream() {
            @Override
            public void flush() throws IOException {
                super.flush();
                streamFlushed[0] = true;
            }

            @Override
            public void close() throws IOException {
                super.close();
                streamClosed[0] = true;
            }
        };

        int features = JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM.getMask()
                | JsonGenerator.Feature.AUTO_CLOSE_TARGET.getMask();
        UTF8JsonGenerator gen = createGenerator(out, features);

        gen.writeStartArray();
        gen.writeNumber(99);
        gen.flush();
        assertTrue(streamFlushed[0]);

        gen.writeEndArray();
        gen.close();
        assertTrue(streamClosed[0]);
    }
}