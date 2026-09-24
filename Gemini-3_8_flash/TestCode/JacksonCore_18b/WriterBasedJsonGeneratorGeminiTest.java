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

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: WriterBasedJsonGenerator
 * -----------------------------------------------------------------------------------------
 * Defect Under Test (Defects4J):
 * - Target: writeNumber(BigDecimal value) with Feature.WRITE_BIGDECIMAL_AS_PLAIN enabled.
 * - Fault: Calling writeNumber(new BigDecimal("1E+10000")) with WRITE_BIGDECIMAL_AS_PLAIN
 *   directly invokes value.toPlainString(), bypassing the scale limits checked in _asString(value),
 *   which should throw a JsonGenerationException ("Attempt to write plain `BigDecimal`").
 * -----------------------------------------------------------------------------------------
 * Coverage Partitions:
 * - Partition A: Structural writes (start/end Object, start/end Array, fieldNames with
 *   regular strings, SerializedStrings, unquoted field names, and pretty printing).
 * - Partition B: Primitive & Object values (short, int, long, BigInteger, double, float,
 *   BigDecimal, boolean, null, numbers-as-strings, non-numeric quoting).
 * - Partition C: Text and Raw writes (writeString String/char[]/SerializableString, long strings
 *   exceeding buffer capacity, raw long text exceeding buffer, raw chars).
 * - Partition D: Character Escaping branches (ASCII maxNonEscaped, custom CharacterEscapes,
 *   2-char escapes, unicode 8-bit \u00XX, unicode 16-bit \uXXXX, prepending vs appending).
 * - Partition E: Binary data writes (byte arrays, InputStreams with known/unknown length,
 *   Base64 chunks, partial chunks, line break insertions).
 * - Partition F: Defensive contexts, lifecycle, buffer release, flush, and auto-closing.
 */
public class WriterBasedJsonGeneratorGeminiTest {

    private WriterBasedJsonGenerator createGenerator(Writer writer, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), "test", false);
        return new WriterBasedJsonGenerator(ctxt, features, null, writer);
    }

    private WriterBasedJsonGenerator createGenerator(Writer writer) {
        return createGenerator(writer, 0);
    }

    /*
     * =========================================================================
     * PARTITION C: DEFECT-TARGETED BRANCH ZONE (Defects4J Ground Truth)
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testTooBigBigDecimal() throws IOException {
        StringWriter sw = new StringWriter();
        int features = JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.getMask();
        WriterBasedJsonGenerator gen = createGenerator(sw, features);

        BigDecimal tooBig = new BigDecimal("1E+10000");
        try {
            gen.writeNumber(tooBig);
            fail("Should not have written without exception: 1E+10000");
        } catch (JsonGenerationException e) {
            String msg = e.getMessage();
            assertTrue("Expected scale exception message, got: " + msg,
                    msg != null && msg.contains("Attempt to write plain `BigDecimal`"));
        } finally {
            gen.close();
        }
    }

    @Test(timeout = 4000)
    public void testTooBigBigDecimalQuoted() throws IOException {
        StringWriter sw = new StringWriter();
        int features = JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.getMask()
                | JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask();
        WriterBasedJsonGenerator gen = createGenerator(sw, features);

        BigDecimal tooBig = new BigDecimal("1E+10000");
        try {
            gen.writeNumber(tooBig);
            fail("Should not have written without exception: 1E+10000");
        } catch (JsonGenerationException e) {
            String msg = e.getMessage();
            assertTrue("Expected scale exception message, got: " + msg,
                    msg != null && msg.contains("Attempt to write plain `BigDecimal`"));
        } finally {
            gen.close();
        }
    }

    /*
     * =========================================================================
     * PARTITION A: STRUCTURAL WRITES & STATE TRANSITIONS
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testBasicObjectAndArrayWriting() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        assertEquals(sw, gen.getOutputTarget());
        assertEquals(0, gen.getOutputBuffered());

        gen.writeStartObject();
        gen.writeFieldName("user");
        gen.writeString("Alice");
        gen.writeFieldName(new SerializedString("ids"));
        gen.writeStartArray();
        gen.writeNumber(100);
        gen.writeNumber(200);
        gen.writeEndArray();
        gen.writeEndObject();
        gen.close();

        assertEquals("{\"user\":\"Alice\",\"ids\":[100,200]}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testUnquotedFieldNames() throws IOException {
        StringWriter sw = new StringWriter();
        int features = JsonGenerator.Feature.QUOTE_FIELD_NAMES.getMask();
        // Quote field names is ON by default; let's create with 0 (unquoted)
        WriterBasedJsonGenerator gen = createGenerator(sw, 0);

        gen.writeStartObject();
        gen.writeFieldName("unquotedKey");
        gen.writeString("val1");
        gen.writeFieldName(new SerializedString("secondKey"));
        gen.writeNumber(42);
        gen.writeEndObject();
        gen.close();

        assertEquals("{unquotedKey:\"val1\",secondKey:42}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testPrettyPrintingPaths() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.setPrettyPrinter(new DefaultPrettyPrinter());

        gen.writeStartObject();
        gen.writeFieldName("k1");
        gen.writeString("v1");
        gen.writeFieldName(new SerializedString("k2"));
        gen.writeStartArray();
        gen.writeNumber(1);
        gen.writeEndArray();
        gen.writeEndObject();
        gen.close();

        String json = sw.toString();
        assertTrue(json.contains("\n"));
        assertTrue(json.contains("\"k1\" : \"v1\""));
    }

    @Test(timeout = 4000)
    public void testPrettyPrintingUnquotedFieldNames() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw, 0);
        gen.setPrettyPrinter(new DefaultPrettyPrinter());

        gen.writeStartObject();
        gen.writeFieldName("first");
        gen.writeNumber(1);
        gen.writeFieldName(new SerializedString("second"));
        gen.writeNumber(2);
        gen.writeEndObject();
        gen.close();

        String json = sw.toString();
        assertTrue(json.contains("first : 1"));
        assertTrue(json.contains("second : 2"));
    }

    /*
     * =========================================================================
     * PARTITION B: PRIMITIVE AND NUMBER TYPES
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testPrimitiveNumbersStandard() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        gen.writeStartArray();
        gen.writeNumber((short) -32768);
        gen.writeNumber(2147483647);
        gen.writeNumber(-9223372036854775808L);
        gen.writeNumber(new BigInteger("123456789012345678901234567890"));
        gen.writeNumber(new BigDecimal("123.456"));
        gen.writeNumber(1.25);
        gen.writeNumber(2.5f);
        gen.writeNumber("99999");
        gen.writeBoolean(true);
        gen.writeBoolean(false);
        gen.writeNull();
        gen.writeEndArray();
        gen.close();

        assertEquals("[-32768,2147483647,-9223372036854775808,123456789012345678901234567890,123.456,1.25,2.5,99999,true,false,null]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testNumbersAsStrings() throws IOException {
        StringWriter sw = new StringWriter();
        int features = JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask();
        WriterBasedJsonGenerator gen = createGenerator(sw, features);

        gen.writeStartArray();
        gen.writeNumber((short) 12);
        gen.writeNumber(1234);
        gen.writeNumber(9876543210L);
        gen.writeNumber(new BigInteger("999"));
        gen.writeNumber(new BigDecimal("12.50"));
        gen.writeNumber(3.14);
        gen.writeNumber(1.5f);
        gen.writeNumber("555");
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"12\",\"1234\",\"9876543210\",\"999\",\"12.50\",\"3.14\",\"1.5\",\"555\"]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSpecialDoublesAndFloats() throws IOException {
        StringWriter sw = new StringWriter();
        int features = JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.getMask();
        WriterBasedJsonGenerator gen = createGenerator(sw, features);

        gen.writeStartArray();
        gen.writeNumber(Double.NaN);
        gen.writeNumber(Double.POSITIVE_INFINITY);
        gen.writeNumber(Float.NaN);
        gen.writeNumber(Float.NEGATIVE_INFINITY);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"NaN\",\"Infinity\",\"NaN\",\"-Infinity\"]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testNullBigDecimalAndBigInteger() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        gen.writeStartArray();
        gen.writeNumber((BigDecimal) null);
        gen.writeNumber((BigInteger) null);
        gen.writeEndArray();
        gen.close();

        assertEquals("[null,null]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testBigDecimalAsPlain() throws IOException {
        StringWriter sw = new StringWriter();
        int features = JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.getMask();
        WriterBasedJsonGenerator gen = createGenerator(sw, features);

        gen.writeStartArray();
        gen.writeNumber(new BigDecimal("1E-3"));
        gen.writeEndArray();
        gen.close();

        assertEquals("[0.001]", sw.toString());
    }

    /*
     * =========================================================================
     * PARTITION C: TEXT, RAW STRINGS, AND BUFFER BOUNDARIES
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testWriteStringsVariants() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        gen.writeStartArray();
        gen.writeString((String) null);
        gen.writeString("simple");
        char[] chars = "hello world".toCharArray();
        gen.writeString(chars, 0, 5);
        gen.writeString(new SerializedString("serializable"));
        gen.writeEndArray();
        gen.close();

        assertEquals("[null,\"simple\",\"hello\",\"serializable\"]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testWriteLongStringExceedingBuffer() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 3000; i++) {
            sb.append("longTextValue_");
        }
        String bigStr = sb.toString();

        gen.writeStartArray();
        gen.writeString(bigStr);
        gen.writeEndArray();
        gen.close();

        String expected = "[\"" + bigStr + "\"]";
        assertEquals(expected, sw.toString());
    }

    @Test(timeout = 4000)
    public void testWriteRawOutputs() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        gen.writeStartArray();
        gen.writeRaw("1,");
        gen.writeRaw("2,3", 0, 2); // writes "2,"
        gen.writeRaw(new SerializedString("4,"));
        gen.writeRaw('5');
        gen.writeRaw(new char[]{',', '6'}, 0, 2);
        gen.writeEndArray();
        gen.close();

        assertEquals("[1,2,4,5,6]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testWriteRawLongExceedingBuffer() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4000; i++) {
            sb.append("abcdefghij");
        }
        String bigRaw = sb.toString();

        gen.writeRaw(bigRaw);
        gen.close();

        assertEquals(bigRaw, sw.toString());
    }

    /*
     * =========================================================================
     * PARTITION D: ESCAPES (ASCII, CUSTOM, CONTROL CHARS)
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testStandardControlEscapes() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        gen.writeString("line1\nline2\ttab\"quote\\slash\r\b\f\u0001\u001F");
        gen.close();

        assertEquals("\"line1\\nline2\\ttab\\\"quote\\\\slash\\r\\b\\f\\u0001\\u001f\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testHighestNonEscapedCharLimiting() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.setHighestNonEscapedChar(127); // Only 7-bit ASCII non-escaped

        gen.writeStartArray();
        gen.writeString("Hello \u00E9 (é) and \u4E2D (中)");
        char[] chars = "Chars: \u00E9 and \u4E2D".toCharArray();
        gen.writeString(chars, 0, chars.length);
        gen.writeEndArray();
        gen.close();

        String res = sw.toString();
        assertTrue(res.contains("\\u00e9"));
        assertTrue(res.contains("\\u4e2d"));
        assertFalse(res.contains("\u00E9"));
        assertFalse(res.contains("\u4E2D"));
    }

    @Test(timeout = 4000)
    public void testCustomCharacterEscapes() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        CharacterEscapes custom = new CharacterEscapes() {
            private final int[] asciiEscapes = standardAsciiEscapesForJSON();

            @Override
            public int[] getEscapeCodesForAscii() {
                return asciiEscapes;
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {
                if (ch == '@') {
                    return new SerializedString("[AT]");
                }
                return null;
            }
        };

        gen.setCharacterEscapes(custom);
        gen.writeStartArray();
        gen.writeString("user@domain.com");
        char[] chars = "test@domain".toCharArray();
        gen.writeString(chars, 0, chars.length);
        gen.writeEndArray();
        gen.close();

        assertEquals("[\"user[AT]domain.com\",\"test[AT]domain\"]", sw.toString());
    }

    /*
     * =========================================================================
     * PARTITION E: BASE64 BINARY DATA
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testBinaryByteArrays() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        byte[] data1 = new byte[]{1, 2, 3, 4, 5};
        byte[] data2 = new byte[]{10, 20};
        byte[] data3 = new byte[100];
        for (int i = 0; i < 100; i++) {
            data3[i] = (byte) i;
        }

        gen.writeStartArray();
        gen.writeBinary(Base64Variants.MIME, data1, 0, data1.length);
        gen.writeBinary(Base64Variants.MIME, data2, 0, data2.length);
        gen.writeBinary(Base64Variants.MIME, data3, 0, data3.length);
        gen.writeEndArray();
        gen.close();

        String json = sw.toString();
        assertTrue(json.startsWith("[\"AQIDBAU=\",\"ChQ=\",\""));
        assertTrue(json.endsWith("\"]"));
    }

    @Test(timeout = 4000)
    public void testBinaryInputStreamKnownLength() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        byte[] raw = new byte[]{1, 2, 3, 4, 5, 6, 7};
        ByteArrayInputStream bais = new ByteArrayInputStream(raw);

        gen.writeStartArray();
        int written = gen.writeBinary(Base64Variants.MIME, bais, raw.length);
        gen.writeEndArray();
        gen.close();

        assertEquals(raw.length, written);
        assertTrue(sw.toString().startsWith("[\"AQIDBAUG"));
    }

    @Test(timeout = 4000)
    public void testBinaryInputStreamUnknownLength() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        byte[] raw = new byte[]{7, 6, 5, 4, 3, 2, 1};
        ByteArrayInputStream bais = new ByteArrayInputStream(raw);

        gen.writeStartArray();
        int written = gen.writeBinary(Base64Variants.MIME, bais, -1);
        gen.writeEndArray();
        gen.close();

        assertEquals(raw.length, written);
        assertTrue(sw.toString().startsWith("[\"BwYFBAMCAQ==\"]"));
    }

    @Test(timeout = 4000)
    public void testBinaryInputStreamTooFewBytesThrowsException() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        byte[] raw = new byte[]{1, 2};
        ByteArrayInputStream bais = new ByteArrayInputStream(raw);

        try {
            gen.writeBinary(Base64Variants.MIME, bais, 10);
            fail("Expected exception for missing bytes");
        } catch (JsonParseException | JsonGenerationException expected) {
            assertTrue(expected.getMessage().contains("Too few bytes available"));
        } finally {
            gen.close();
        }
    }

    /*
     * =========================================================================
     * PARTITION F: EXCEPTION & DEFENSIVE GUARD PATHS
     * =========================================================================
     */

    @Test(expected = JsonGenerationException.class, timeout = 4000)
    public void testWriteFieldNameInArrayThrows() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        gen.writeStartArray();
        gen.writeFieldName("invalid");
        gen.close();
    }

    @Test(expected = JsonGenerationException.class, timeout = 4000)
    public void testWriteValueWhenExpectingFieldNameThrows() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        gen.writeStartObject();
        gen.writeString("valueWithoutKey");
        gen.close();
    }

    @Test(expected = JsonGenerationException.class, timeout = 4000)
    public void testEndObjectWhenInArrayThrows() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        gen.writeStartArray();
        gen.writeEndObject();
        gen.close();
    }

    @Test(expected = JsonGenerationException.class, timeout = 4000)
    public void testEndArrayWhenInObjectThrows() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        gen.writeStartObject();
        gen.writeEndArray();
        gen.close();
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteUTF8StringUnsupported() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.writeUTF8String(new byte[]{1, 2}, 0, 2);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawUTF8StringUnsupported() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.writeRawUTF8String(new byte[]{1, 2}, 0, 2);
    }

    /*
     * =========================================================================
     * PARTITION G: LIFECYCLE, AUTO-CLOSE, BUFFER MANAGEMENT
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testAutoCloseJsonContent() throws IOException {
        StringWriter sw = new StringWriter();
        int features = JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT.getMask();
        WriterBasedJsonGenerator gen = createGenerator(sw, features);

        gen.writeStartObject();
        gen.writeFieldName("arr");
        gen.writeStartArray();
        gen.writeNumber(1);
        // deliberately leave array and object open
        gen.close();

        assertEquals("{\"arr\":[1]}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testFlushAndBufferedState() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);

        gen.writeStartArray();
        assertTrue(gen.getOutputBuffered() > 0);
        gen.flush();
        assertEquals(0, gen.getOutputBuffered());
        gen.writeEndArray();
        gen.close();

        assertEquals("[]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testRootValueSeparator() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        gen.setRootValueSeparator(new SerializedString(" | "));

        gen.writeNumber(10);
        gen.writeNumber(20);
        gen.writeBoolean(true);
        gen.close();

        assertEquals("10 | 20 | true", sw.toString());
    }
}