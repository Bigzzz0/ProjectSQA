package com.fasterxml.jackson.core.json;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.BufferRecycler;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------
 * Target Class: UTF8JsonGenerator
 * Defect Reference: RawValueWithSurrogatesTest::testRawWithSurrogatesString
 * Root Cause: In writeRaw(String, int, int), text is segmented into chunks of _charBuffer.length.
 *             When a surrogate pair straddles the boundary (high surrogate at chunk end), passing
 *             it to writeRaw(char[], int, int) causes _outputRawMultiByteChar to detect a missing
 *             low surrogate and throw JsonGenerationException ("Split surrogate on writeRaw() input").
 *
 * Branch & Condition Coverage Matrix:
 * - writeRaw(String, int, int) / Surrogates across buffer boundaries (Ground truth defect target)
 * - writeRaw(char[], int, int) & _writeSegmentedRaw: ASCII, 2-byte, 3-byte, and surrogate pairs
 * - writeRaw(char): 1-byte, 2-byte, 3-byte, buffer flush triggers
 * - writeString(String) & writeString(char[], int, int):
 *     - null text -> _writeNull
 *     - short ASCII (< _outputMaxContiguous)
 *     - long ASCII (> _outputMaxContiguous) -> segmented writes
 *     - 2-byte UTF-8, 3-byte UTF-8, surrogate pairs
 *     - Control characters needing hex escapes (\u00XX)
 *     - Custom CharacterEscapes (standard, generic, and custom serializable escape)
 *     - Long custom escape sequences exceeding 6 bytes (_handleLongCustomEscape)
 *     - Feature.ESCAPE_NON_ASCII enabled (ASCII-only output)
 * - writeFieldName(String) & writeFieldName(SerializableString):
 *     - Pretty printing mode vs compact mode
 *     - Status checks: STATUS_EXPECT_VALUE error, STATUS_OK_AFTER_COMMA
 *     - Unquoted field names (_cfgUnqNames)
 *     - Fast path (< _outputMaxContiguous) vs multi-segment (> _charBufferLength)
 * - writeStartArray / writeEndArray / writeStartObject / writeEndObject:
 *     - Scope mismatch validations (calling writeEndObject inside array, etc.)
 *     - Auto-close open scopes on close() with Feature.AUTO_CLOSE_JSON_CONTENT
 * - writeNumber overloads:
 *     - short, int, long, BigInteger, BigDecimal, double, float, encoded String
 *     - Feature.WRITE_NUMBERS_AS_STRINGS quoting
 *     - NaN and Infinity quoting with Feature.QUOTE_NON_NUMERIC_NUMBERS
 *     - BigDecimal with Feature.WRITE_BIGDECIMAL_AS_PLAIN
 * - writeBinary overloads:
 *     - byte[] full and partial triplets (lengths % 3 == 0, 1, 2)
 *     - InputStream variant with known length, unknown length (-1), and underflow error
 * - Lifecycle & Buffer Management:
 *     - Buffer recycling, flush, close with AUTO_CLOSE_TARGET on/off
 *     - getOutputTarget, getOutputBuffered
 * ---------------------------------------------------------------------------------------------
 */
public class UTF8JsonGeneratorGeminiTest {

    private UTF8JsonGenerator _createGenerator(OutputStream out) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        return new UTF8JsonGenerator(ctxt, 0, null, out);
    }

    private UTF8JsonGenerator _createGenerator(OutputStream out, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        return new UTF8JsonGenerator(ctxt, features, null, out);
    }

    private UTF8JsonGenerator _createGenerator(OutputStream out, byte[] buf, int offset, boolean recyclable) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        return new UTF8JsonGenerator(ctxt, 0, null, out, buf, offset, recyclable);
    }

    /*
     * =========================================================================
     * Partition C: Defect-Targeted Branch Zone (Jackson Defects4J Ground Truth)
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testDefectRawWithSurrogatesStringAcrossSegmentBoundary() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        // Calculate size to position the high surrogate at the very last position of _charBuffer
        int bufLen = gen._charBufferLength;
        StringBuilder sb = new StringBuilder(bufLen + 10);
        for (int i = 0; i < bufLen - 1; ++i) {
            sb.append('a');
        }
        // Append surrogate pair: U+1D11E (Musical Symbol G Clef: \uD834\uDD1E)
        sb.append("\uD834\uDD1E");
        sb.append("tail");

        gen.writeStartArray();
        // This invocation triggers the defect if writeRaw(String) splits the surrogate pair across chunks
        gen.writeRaw(sb.toString());
        gen.writeEndArray();
        gen.close();

        byte[] resultBytes = out.toByteArray();
        String result = new String(resultBytes, "UTF-8");
        assertTrue(result.startsWith("[a"));
        assertTrue(result.contains("\uD834\uDD1Etail]"));
    }

    @Test(timeout = 4000)
    public void testDefectRawWithSurrogatesCharArray() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        gen.writeStartArray();
        char[] chars = "\uD83D\uDCA9".toCharArray(); // U+1F4A9 PILE OF POO
        gen.writeRaw(chars, 0, chars.length);
        gen.writeEndArray();
        gen.close();

        String result = out.toString("UTF-8");
        assertEquals("[\uD83D\uDCA9]", result);
    }

    /*
     * =========================================================================
     * Partition A: Core Functional Logic & State Transitions
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testWriteBasicArrayAndObject() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        gen.writeStartObject();
        gen.writeFieldName("field1");
        gen.writeString("value1");
        gen.writeFieldName(new SerializedString("field2"));
        gen.writeNumber(123);
        gen.writeFieldName("array");
        gen.writeStartArray();
        gen.writeBoolean(true);
        gen.writeBoolean(false);
        gen.writeNull();
        gen.writeEndArray();
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"field1\":\"value1\",\"field2\":123,\"array\":[true,false,null]}", out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testPrettyPrinterFormatting() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);
        gen.setPrettyPrinter(new DefaultPrettyPrinter());

        gen.writeStartObject();
        gen.writeFieldName("name");
        gen.writeString("test");
        gen.writeFieldName(new SerializedString("arr"));
        gen.writeStartArray();
        gen.writeNumber(1);
        gen.writeEndArray();
        gen.writeEndObject();
        gen.close();

        String json = out.toString("UTF-8");
        assertTrue(json.contains("\n"));
        assertTrue(json.contains("\"name\" : \"test\"") || json.contains("\"name\": \"test\""));
    }

    @Test(timeout = 4000)
    public void testPrettyPrinterUnquotedFieldNames() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int feat = JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        // create with QUOTE_FIELD_NAMES disabled (feat = 0)
        UTF8JsonGenerator gen = _createGenerator(out, 0);
        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        gen.setPrettyPrinter(new DefaultPrettyPrinter());

        gen.writeStartObject();
        gen.writeFieldName("unquotedKey");
        gen.writeNumber(42);
        gen.writeFieldName(new SerializedString("unquotedKey2"));
        gen.writeNumber(43);
        gen.writeEndObject();
        gen.close();

        String json = out.toString("UTF-8");
        assertTrue(json.contains("unquotedKey"));
        assertFalse(json.contains("\"unquotedKey\""));
        assertTrue(json.contains("unquotedKey2"));
    }

    @Test(timeout = 4000)
    public void testUnquotedFieldNamesStandard() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);
        gen.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);

        gen.writeStartObject();
        gen.writeFieldName("key1");
        gen.writeNumber(1);
        gen.writeFieldName(new SerializedString("key2"));
        gen.writeNumber(2);
        // Field name longer than char buffer length to hit offline write
        int largeLen = gen._charBufferLength + 10;
        StringBuilder largeKey = new StringBuilder();
        for (int i = 0; i < largeLen; ++i) {
            largeKey.append('k');
        }
        gen.writeFieldName(largeKey.toString());
        gen.writeNumber(3);
        gen.writeEndObject();
        gen.close();

        String result = out.toString("UTF-8");
        assertTrue(result.startsWith("{key1:1,key2:2,kkk"));
    }

    @Test(timeout = 4000)
    public void testRootValueSeparator() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);
        gen.setRootValueSeparator(new SerializedString(" | "));

        gen.writeNumber(1);
        gen.writeNumber(2);
        gen.writeString("abc");
        gen.close();

        assertEquals("1 | 2 | \"abc\"", out.toString("UTF-8"));
    }

    /*
     * =========================================================================
     * Partition B: Boundary Value Analysis & Numbers Quoting
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testWriteAllNumberTypesNormal() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        gen.writeStartArray();
        gen.writeNumber((short) -32768);
        gen.writeNumber((short) 32767);
        gen.writeNumber(-2147483648);
        gen.writeNumber(2147483647);
        gen.writeNumber(-9223372036854775808L);
        gen.writeNumber(9223372036854775807L);
        gen.writeNumber(new BigInteger("123456789012345678901234567890"));
        gen.writeNumber(new BigDecimal("12345.67890"));
        gen.writeNumber(1.25);
        gen.writeNumber(2.5f);
        gen.writeNumber("99999");
        gen.writeNumber((BigDecimal) null);
        gen.writeNumber((BigInteger) null);
        gen.writeEndArray();
        gen.close();

        String expected = "[-32768,32767,-2147483648,2147483647,-9223372036854775808,9223372036854775807,"
                + "123456789012345678901234567890,12345.67890,1.25,2.5,99999,null,null]";
        assertEquals(expected, out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteNumbersAsStringsFeature() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int feat = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask();
        UTF8JsonGenerator gen = _createGenerator(out, feat);

        gen.writeStartArray();
        gen.writeNumber((short) 12);
        gen.writeNumber(34);
        gen.writeNumber(56L);
        gen.writeNumber(new BigInteger("78"));
        gen.writeNumber(new BigDecimal("9.9"));
        gen.writeNumber(1.5);
        gen.writeNumber(2.5f);
        gen.writeNumber("100");
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"12\",\"34\",\"56\",\"78\",\"9.9\",\"1.5\",\"2.5\",\"100\"]", out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testSpecialFloatingPointNumbers() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int feat = JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.getMask();
        UTF8JsonGenerator gen = _createGenerator(out, feat);

        gen.writeStartArray();
        gen.writeNumber(Double.NaN);
        gen.writeNumber(Double.POSITIVE_INFINITY);
        gen.writeNumber(Double.NEGATIVE_INFINITY);
        gen.writeNumber(Float.NaN);
        gen.writeNumber(Float.POSITIVE_INFINITY);
        gen.writeNumber(Float.NEGATIVE_INFINITY);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"NaN\",\"Infinity\",\"-Infinity\",\"NaN\",\"Infinity\",\"-Infinity\"]", out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testBigDecimalAsPlain() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int feat = JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.getMask();
        UTF8JsonGenerator gen = _createGenerator(out, feat);

        BigDecimal bd = new BigDecimal("1E-7");
        gen.writeStartArray();
        gen.writeNumber(bd);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"0.0000001\"]".replaceAll("\"", ""), out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testBigDecimalAsPlainQuoted() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int feat = JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.getMask()
                | JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask();
        UTF8JsonGenerator gen = _createGenerator(out, feat);

        BigDecimal bd = new BigDecimal("1E-7");
        gen.writeStartArray();
        gen.writeNumber(bd);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"0.0000001\"]", out.toString("UTF-8"));
    }

    /*
     * =========================================================================
     * Partition B (Cont.): String Escapes & Character Handling
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testWriteStringEscapesAndControlChars() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        gen.writeStartArray();
        gen.writeString("Hello\n\t\r\b\f\"\\World");
        gen.writeString("\u0000\u001F"); // Generic escapes
        gen.writeString("Euro: \u20AC"); // 3-byte UTF-8
        gen.writeString("Latin: \u00F1"); // 2-byte UTF-8
        gen.writeString((String) null);
        gen.writeEndArray();
        gen.close();

        String res = out.toString("UTF-8");
        assertTrue(res.contains("\\n\\t\\r\\b\\f\\\"\\\\World"));
        assertTrue(res.contains("\\u0000\\u001f"));
        assertTrue(res.contains("\u20AC"));
        assertTrue(res.contains("\u00F1"));
        assertTrue(res.contains("null"));
    }

    @Test(timeout = 4000)
    public void testEscapeNonAsciiFeature() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int feat = JsonGenerator.Feature.ESCAPE_NON_ASCII.getMask();
        UTF8JsonGenerator gen = _createGenerator(out, feat);

        gen.writeStartArray();
        gen.writeString("Non-Ascii: \u00F1 \u20AC");
        gen.writeEndArray();
        gen.close();

        String res = out.toString("UTF-8");
        assertFalse(res.contains("\u00F1"));
        assertFalse(res.contains("\u20AC"));
        assertTrue(res.contains("\\u00f1"));
        assertTrue(res.contains("\\u20ac"));
    }

    @Test(timeout = 4000)
    public void testCustomCharacterEscapes() throws Exception {
        CharacterEscapes customEscapes = new CharacterEscapes() {
            private static final long serialVersionUID = 1L;

            @Override
            public int[] getEscapeCodesForAscii() {
                int[] ascii = standardAsciiEscapesForJSON();
                ascii['a'] = CharacterEscapes.ESCAPE_CUSTOM;
                return ascii;
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {
                if (ch == 'a') {
                    return new SerializedString("[ESCAPED_A]");
                }
                return null;
            }
        };

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);
        gen.setCharacterEscapes(customEscapes);

        gen.writeStartArray();
        gen.writeString("cat and bat");
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"c[ESCAPED_A]t [ESCAPED_A]nd b[ESCAPED_A]t\"]", out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testLongCustomCharacterEscape() throws Exception {
        CharacterEscapes longEscape = new CharacterEscapes() {
            private static final long serialVersionUID = 1L;

            @Override
            public int[] getEscapeCodesForAscii() {
                int[] ascii = standardAsciiEscapesForJSON();
                ascii['x'] = CharacterEscapes.ESCAPE_CUSTOM;
                return ascii;
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {
                if (ch == 'x') {
                    // > 6 bytes to trigger _handleLongCustomEscape
                    return new SerializedString("<<VERY_LONG_CUSTOM_ESCAPE_SEQUENCE>>");
                }
                return null;
            }
        };

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);
        gen.setCharacterEscapes(longEscape);

        gen.writeStartArray();
        gen.writeString("axb");
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"a<<VERY_LONG_CUSTOM_ESCAPE_SEQUENCE>>b\"]", out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteStringSegmentsLongString() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        // String larger than _outputMaxContiguous
        int count = gen._outputMaxContiguous * 3;
        StringBuilder sb = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            sb.append((char) ('a' + (i % 26)));
        }

        gen.writeStartArray();
        gen.writeString(sb.toString());
        gen.writeString(sb.toString().toCharArray(), 0, sb.length());
        gen.writeEndArray();
        gen.close();

        String res = out.toString("UTF-8");
        assertTrue(res.startsWith("[\"abcdef"));
        assertTrue(res.endsWith("\"]"));
    }

    @Test(timeout = 4000)
    public void testWriteUTF8StringAndRawUTF8String() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        byte[] rawAscii = "rawAsciiText".getBytes("UTF-8");
        byte[] rawEscaped = "hello \"quoted\" and \n newline".getBytes("UTF-8");

        gen.writeStartArray();
        gen.writeRawUTF8String(rawAscii, 0, rawAscii.length);
        gen.writeUTF8String(rawEscaped, 0, rawEscaped.length);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"rawAsciiText\",\"hello \\\"quoted\\\" and \\n newline\"]", out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteRawChars() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        gen.writeStartArray();
        gen.writeRaw(':');
        gen.writeRaw('\u00E9'); // 2-byte UTF-8
        gen.writeRaw('\u4E16'); // 3-byte UTF-8
        gen.writeRaw(new SerializedString("123"));
        gen.writeRawValue(new SerializedString("456"));
        gen.writeEndArray();
        gen.close();

        assertEquals("[:\u00E9\u4E16123,456]", out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteSegmentedRawMultiByte() throws Exception {
        // Small buffer to force _writeSegmentedRaw
        byte[] smallBuf = new byte[16];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out, smallBuf, 0, false);

        char[] chars = ("abcdefghijklmnopqrstuvwxyz\u00A2\u20AC\u4E16").toCharArray();
        gen.writeRaw(chars, 0, chars.length);
        gen.flush();

        assertEquals("abcdefghijklmnopqrstuvwxyz\u00A2\u20AC\u4E16", out.toString("UTF-8"));
    }

    /*
     * =========================================================================
     * Partition B (Cont.): Binary / Base64 Output
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testWriteBinaryByteArray() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        byte[] data1 = "abc".getBytes("UTF-8");      // 3 bytes -> exactly 4 base64 chars
        byte[] data2 = "abcd".getBytes("UTF-8");     // 4 bytes -> 1 leftover byte
        byte[] data3 = "abcde".getBytes("UTF-8");    // 5 bytes -> 2 leftover bytes

        gen.writeStartArray();
        gen.writeBinary(Base64Variants.MIME, data1, 0, data1.length);
        gen.writeBinary(Base64Variants.MIME, data2, 0, data2.length);
        gen.writeBinary(Base64Variants.MIME, data3, 0, data3.length);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"YWJj\",\"YWJjZA==\",\"YWJjZGU=\"]", out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testWriteBinaryInputStreamKnownLength() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        byte[] raw = "Jackson Base64 Stream Test Data".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(raw);

        gen.writeStartArray();
        int written = gen.writeBinary(Base64Variants.MIME_NO_LINEFEEDS, in, raw.length);
        gen.writeEndArray();
        gen.close();

        assertEquals(raw.length, written);
        assertTrue(out.toString("UTF-8").startsWith("[\"SmFja3Nvbg==".substring(0, 5)));
    }

    @Test(timeout = 4000)
    public void testWriteBinaryInputStreamUnknownLength() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        byte[] raw = "Another unknown length stream".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(raw);

        gen.writeStartArray();
        int written = gen.writeBinary(Base64Variants.MIME_NO_LINEFEEDS, in, -1);
        gen.writeEndArray();
        gen.close();

        assertEquals(raw.length, written);
        assertTrue(out.toString("UTF-8").contains("QW5vdGhlcg==".substring(0, 5)));
    }

    @Test(timeout = 4000)
    public void testWriteBinaryInputStreamTooFewBytes() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        byte[] raw = "short".getBytes("UTF-8");
        InputStream in = new ByteArrayInputStream(raw);

        try {
            gen.writeBinary(Base64Variants.MIME, in, 100);
            fail("Expected JsonGenerationException for too few bytes");
        } catch (JsonGenerationException e) {
            assertTrue(e.getMessage().contains("Too few bytes available"));
        }
    }

    /*
     * =========================================================================
     * Partition D: Exception & Defensive Guard Paths
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testFieldNameWhenExpectingValueThrowsException() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        gen.writeStartObject();
        gen.writeFieldName("key");
        try {
            gen.writeFieldName("duplicateKeyBeforeVal");
            fail("Should throw JsonGenerationException");
        } catch (JsonGenerationException e) {
            assertTrue(e.getMessage().contains("Can not write a field name, expecting a value"));
        }
    }

    @Test(timeout = 4000)
    public void testValueWhenExpectingFieldNameThrowsException() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        gen.writeStartObject();
        try {
            gen.writeString("nakedValue");
            fail("Should throw JsonGenerationException");
        } catch (JsonGenerationException e) {
            assertTrue(e.getMessage().contains("expecting field name"));
        }
    }

    @Test(timeout = 4000)
    public void testWriteEndArrayWhenInObjectThrowsException() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        gen.writeStartObject();
        try {
            gen.writeEndArray();
            fail("Should throw JsonGenerationException");
        } catch (JsonGenerationException e) {
            assertTrue(e.getMessage().contains("Current context not an ARRAY"));
        }
    }

    @Test(timeout = 4000)
    public void testWriteEndObjectWhenInArrayThrowsException() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        gen.writeStartArray();
        try {
            gen.writeEndObject();
            fail("Should throw JsonGenerationException");
        } catch (JsonGenerationException e) {
            assertTrue(e.getMessage().contains("Current context not an object"));
        }
    }

    @Test(timeout = 4000)
    public void testMissingCustomEscapeThrowsException() throws Exception {
        CharacterEscapes buggyEscapes = new CharacterEscapes() {
            private static final long serialVersionUID = 1L;

            @Override
            public int[] getEscapeCodesForAscii() {
                int[] ascii = standardAsciiEscapesForJSON();
                ascii['z'] = CharacterEscapes.ESCAPE_CUSTOM;
                return ascii;
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {
                // Buggy: claims ESCAPE_CUSTOM but returns null
                return null;
            }
        };

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);
        gen.setCharacterEscapes(buggyEscapes);

        try {
            gen.writeString("zoo");
            fail("Should throw JsonGenerationException for missing custom escape");
        } catch (JsonGenerationException e) {
            assertTrue(e.getMessage().contains("Invalid custom escape definitions"));
        }
    }

    /*
     * =========================================================================
     * Partition E: Lifecycle & Buffer Management
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testAutoCloseJsonContent() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);
        gen.enable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);

        gen.writeStartObject();
        gen.writeFieldName("unclosedArr");
        gen.writeStartArray();
        gen.writeNumber(1);
        // Do not explicitly call writeEndArray() or writeEndObject()
        gen.close();

        assertEquals("{\"unclosedArr\":[1]}", out.toString("UTF-8"));
    }

    @Test(timeout = 4000)
    public void testAutoCloseTargetFeature() throws Exception {
        final boolean[] streamClosed = new boolean[]{ false };
        OutputStream out = new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                streamClosed[0] = true;
                super.close();
            }
        };

        UTF8JsonGenerator gen = _createGenerator(out);
        gen.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        gen.writeNumber(123);
        gen.close();
        assertFalse("Stream should NOT be closed when AUTO_CLOSE_TARGET is disabled", streamClosed[0]);

        UTF8JsonGenerator gen2 = _createGenerator(out);
        gen2.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        gen2.writeNumber(456);
        gen2.close();
        assertTrue("Stream should be closed when AUTO_CLOSE_TARGET is enabled", streamClosed[0]);
    }

    @Test(timeout = 4000)
    public void testGetOutputTargetAndBuffered() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out);

        assertSame(out, gen.getOutputTarget());
        assertEquals(0, gen.getOutputBuffered());

        gen.writeRaw("12345");
        assertEquals(5, gen.getOutputBuffered());

        gen.flush();
        assertEquals(0, gen.getOutputBuffered());
        gen.close();
    }

    @Test(timeout = 4000)
    public void testNonRecyclableBuffer() throws Exception {
        byte[] customBuffer = new byte[128];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        UTF8JsonGenerator gen = _createGenerator(out, customBuffer, 0, false);

        gen.writeStartArray();
        gen.writeNumber(1);
        gen.writeEndArray();
        gen.close();

        assertEquals("[1]", out.toString("UTF-8"));
        // Buffer should not be cleared since recyclable is false
        assertNotNull(gen._outputBuffer);
    }
}