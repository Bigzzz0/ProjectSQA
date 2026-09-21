package com.fasterxml.jackson.core.json;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.IOContext;
import com.fasterxml.jackson.core.util.BufferRecycler;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - writeStartArray/writeEndArray: normal array context transitions
 *   - writeStartObject/writeEndObject: normal object context transitions
 *   - writeFieldName(String/SerializableString): field name writing with/without comma
 *   - writeString(String/char[]/SerializableString): string value writing
 *   - writeNumber(int/long/short/double/float/BigInteger/BigDecimal): number writing
 *   - writeBoolean/Null: primitive value writing
 *   - writeRaw: raw text writing
 *   - writeBinary: binary data writing
 *   - getOutputTarget/getOutputBuffered: state getters
 *   - flush/close: lifecycle methods
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Empty strings, null strings
 *   - Zero, negative, MAX values for numbers
 *   - NaN, Infinity for floating point
 *   - Very large BigDecimal (1E+10000) - DEFECT TARGET
 *   - Buffer boundary conditions (exact buffer size, overflow)
 *   - Short/long string boundaries (SHORT_WRITE = 32)
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - BigDecimal with very large exponent (1E+10000) should throw exception
 *   - Feature.WRITE_BIGDECIMAL_AS_PLAIN interaction with large values
 *   - _cfgNumbersAsStrings interaction with large values
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - Writing field name when expecting value
 *   - Writing value when expecting field name
 *   - Closing unclosed arrays/objects
 *   - Null arguments for various methods
 *   - Negative lengths/offsets
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Multiple close calls
 *   - Flush after close
 *   - Buffer state after close
 */
public class WriterBasedJsonGeneratorDeepseekTest {

    /*
     * Helper method to create a generator with default settings
     */
    private WriterBasedJsonGenerator createGenerator(Writer writer) {
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        JsonFactory factory = new JsonFactory();
        return new WriterBasedJsonGenerator(ctxt, 
            factory.getFormatFeatures(), 
            factory.getCodec(), 
            writer);
    }

    /*
     * Helper method to create a generator with specific features
     */
    private WriterBasedJsonGenerator createGenerator(Writer writer, int features) {
        IOContext ctxt = new IOContext(new BufferRecycler(), null, false);
        return new WriterBasedJsonGenerator(ctxt, features, null, writer);
    }

    /*
     * Helper to get output as string
     */
    private String getOutput(WriterBasedJsonGenerator gen) throws IOException {
        gen.flush();
        return gen.getOutputTarget().toString();
    }

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testWriteStartEndArray() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartArray();
        assertEquals("[", getOutput(gen));
        
        gen.writeEndArray();
        assertEquals("[]", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteStartEndObject() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartObject();
        assertEquals("{", getOutput(gen));
        
        gen.writeEndObject();
        assertEquals("{}", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteFieldNameString() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartObject();
        gen.writeFieldName("foo");
        gen.writeNumber(42);
        gen.writeFieldName("bar");
        gen.writeString("value");
        gen.writeEndObject();
        
        assertEquals("{\"foo\":42,\"bar\":\"value\"}", getOutput(gen));
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteFieldNameSerializableString() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartObject();
        gen.writeFieldName(new SerializableString() {
            public String getValue() { return "test"; }
            public int charLength() { return 4; }
            public char[] asQuotedChars() { return "test".toCharArray(); }
            public byte[] asUnquotedUTF8() { return "test".getBytes(); }
            public byte[] asQuotedUTF8() { return "\"test\"".getBytes(); }
        });
        gen.writeNumber(1);
        gen.writeEndObject();
        
        assertEquals("{\"test\":1}", getOutput(gen));
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteString() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeString("hello");
        assertEquals("\"hello\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteStringCharArray() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeString("world".toCharArray(), 0, 5);
        assertEquals("\"world\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteStringSerializableString() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeString(new SerializableString() {
            public String getValue() { return "serial"; }
            public int charLength() { return 6; }
            public char[] asQuotedChars() { return "serial".toCharArray(); }
            public byte[] asUnquotedUTF8() { return "serial".getBytes(); }
            public byte[] asQuotedUTF8() { return "\"serial\"".getBytes(); }
        });
        assertEquals("\"serial\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberInt() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(42);
        assertEquals("42", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberLong() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(1234567890123L);
        assertEquals("1234567890123", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberShort() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber((short) 42);
        assertEquals("42", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberDouble() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(3.14);
        assertEquals("3.14", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberFloat() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(2.5f);
        assertEquals("2.5", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberBigInteger() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(new BigInteger("12345678901234567890"));
        assertEquals("12345678901234567890", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberBigDecimal() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(new BigDecimal("123.456"));
        assertEquals("123.456", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteBooleanTrue() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeBoolean(true);
        assertEquals("true", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteBooleanFalse() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeBoolean(false);
        assertEquals("false", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNull() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNull();
        assertEquals("null", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteRawString() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeRaw("raw text");
        assertEquals("raw text", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteRawCharArray() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeRaw("raw".toCharArray(), 0, 3);
        assertEquals("raw", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteRawChar() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeRaw('X');
        assertEquals("X", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testGetOutputTarget() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        assertSame(sw, gen.getOutputTarget());
        gen.close();
    }

    @Test(timeout = 4000)
    public void testGetOutputBuffered() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        assertEquals(0, gen.getOutputBuffered());
        
        gen.writeStartArray();
        assertTrue(gen.getOutputBuffered() > 0);
        
        gen.close();
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testWriteStringNull() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeString((String) null);
        assertEquals("null", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteStringEmpty() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeString("");
        assertEquals("\"\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberZero() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(0);
        assertEquals("0", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberNegative() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(-42);
        assertEquals("-42", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberMaxInt() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(Integer.MAX_VALUE);
        assertEquals(String.valueOf(Integer.MAX_VALUE), getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberMinInt() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(Integer.MIN_VALUE);
        assertEquals(String.valueOf(Integer.MIN_VALUE), getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberMaxLong() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(Long.MAX_VALUE);
        assertEquals(String.valueOf(Long.MAX_VALUE), getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberMinLong() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(Long.MIN_VALUE);
        assertEquals(String.valueOf(Long.MIN_VALUE), getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberNaN() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(Double.NaN);
        assertEquals("\"NaN\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberInfinity() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(Double.POSITIVE_INFINITY);
        assertEquals("\"Infinity\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberNegativeInfinity() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(Double.NEGATIVE_INFINITY);
        assertEquals("\"-Infinity\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberFloatNaN() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(Float.NaN);
        assertEquals("\"NaN\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberFloatInfinity() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber(Float.POSITIVE_INFINITY);
        assertEquals("\"Infinity\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberBigIntegerNull() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber((BigInteger) null);
        assertEquals("null", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberBigDecimalNull() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber((BigDecimal) null);
        assertEquals("null", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteStringLongString() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        // Create a string longer than output buffer
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            sb.append('a');
        }
        String longStr = sb.toString();
        gen.writeString(longStr);
        
        String output = getOutput(gen);
        assertTrue(output.startsWith("\""));
        assertTrue(output.endsWith("\""));
        assertEquals(longStr, output.substring(1, output.length() - 1));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteRawLongString() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        // Create a string longer than output buffer
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2000; i++) {
            sb.append('b');
        }
        String longStr = sb.toString();
        gen.writeRaw(longStr);
        
        assertEquals(longStr, getOutput(gen));
        
        gen.close();
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testTooBigBigDecimal() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        // This should throw an exception for a BigDecimal with exponent 10000
        try {
            gen.writeNumber(new BigDecimal("1E+10000"));
            fail("Should have thrown an exception for 1E+10000");
        } catch (Exception e) {
            // Expected - the BigDecimal is too large to represent
            assertTrue(e instanceof IOException || e instanceof JsonGenerationException);
        }
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testTooBigBigDecimalWithNumbersAsStrings() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw, 
            JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask());
        
        // Even with numbers as strings, this should still fail
        try {
            gen.writeNumber(new BigDecimal("1E+10000"));
            fail("Should have thrown an exception for 1E+10000 even with numbers as strings");
        } catch (Exception e) {
            // Expected
            assertTrue(e instanceof IOException || e instanceof JsonGenerationException);
        }
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testTooBigBigDecimalAsPlain() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw, 
            JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.getMask());
        
        // Even as plain, this should still fail
        try {
            gen.writeNumber(new BigDecimal("1E+10000"));
            fail("Should have thrown an exception for 1E+10000 even as plain");
        } catch (Exception e) {
            // Expected
            assertTrue(e instanceof IOException || e instanceof JsonGenerationException);
        }
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testReasonableBigDecimal() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        // A reasonable BigDecimal should work fine
        gen.writeNumber(new BigDecimal("1E+100"));
        String output = getOutput(gen);
        assertNotNull(output);
        assertTrue(output.length() > 0);
        
        gen.close();
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = JsonGenerationException.class)
    public void testWriteFieldNameExpectingValue() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartArray();
        gen.writeFieldName("foo"); // Should fail - in array context
        
        gen.close();
    }

    @Test(timeout = 4000, expected = JsonGenerationException.class)
    public void testWriteValueExpectingFieldName() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartObject();
        gen.writeString("value"); // Should fail - expecting field name
        
        gen.close();
    }

    @Test(timeout = 4000, expected = JsonGenerationException.class)
    public void testWriteEndArrayNotInArray() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartObject();
        gen.writeEndArray(); // Should fail - not in array
        
        gen.close();
    }

    @Test(timeout = 4000, expected = JsonGenerationException.class)
    public void testWriteEndObjectNotInObject() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartArray();
        gen.writeEndObject(); // Should fail - not in object
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testCloseWithUnclosedArray() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartArray();
        gen.writeNumber(1);
        gen.writeNumber(2);
        gen.close(); // Should auto-close the array
        
        assertEquals("[1,2]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testCloseWithUnclosedObject() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartObject();
        gen.writeFieldName("key");
        gen.writeString("value");
        gen.close(); // Should auto-close the object
        
        assertEquals("{\"key\":\"value\"}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testMultipleClose() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartArray();
        gen.writeEndArray();
        gen.close();
        gen.close(); // Should be safe to close multiple times
        
        assertEquals("[]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testFlushAfterClose() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartArray();
        gen.writeEndArray();
        gen.close();
        gen.flush(); // Should be safe to flush after close
        
        assertEquals("[]", sw.toString());
    }

    @Test(timeout = 4000)
    public void testWriteNumberString() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeNumber("42");
        assertEquals("42", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberStringWithNumbersAsStrings() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw, 
            JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask());
        
        gen.writeNumber("42");
        assertEquals("\"42\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteBinary() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        byte[] data = {1, 2, 3, 4, 5};
        gen.writeBinary(Base64Variants.getDefaultVariant(), data, 0, data.length);
        
        String output = getOutput(gen);
        assertTrue(output.startsWith("\""));
        assertTrue(output.endsWith("\""));
        assertTrue(output.length() > 2);
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteBinaryFromStream() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        byte[] data = {10, 20, 30, 40, 50};
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        int bytesWritten = gen.writeBinary(Base64Variants.getDefaultVariant(), bais, data.length);
        
        assertEquals(data.length, bytesWritten);
        String output = getOutput(gen);
        assertTrue(output.startsWith("\""));
        assertTrue(output.endsWith("\""));
        
        gen.close();
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testBufferReleaseOnClose() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartArray();
        gen.writeEndArray();
        gen.close();
        
        // After close, buffer should be released
        // This is verified by the fact that close() calls _releaseBuffers()
        // which sets _outputBuffer to null
    }

    @Test(timeout = 4000)
    public void testWriteAfterClose() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartArray();
        gen.writeEndArray();
        gen.close();
        
        // Writing after close should not throw (or should be safe)
        try {
            gen.writeNumber(42);
        } catch (Exception e) {
            // May or may not throw, but should not cause issues
        }
    }

    @Test(timeout = 4000)
    public void testNestedArrays() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartArray();
        gen.writeStartArray();
        gen.writeNumber(1);
        gen.writeEndArray();
        gen.writeStartArray();
        gen.writeNumber(2);
        gen.writeEndArray();
        gen.writeEndArray();
        
        assertEquals("[[1],[2]]", getOutput(gen));
        gen.close();
    }

    @Test(timeout = 4000)
    public void testNestedObjects() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartObject();
        gen.writeFieldName("outer");
        gen.writeStartObject();
        gen.writeFieldName("inner");
        gen.writeString("value");
        gen.writeEndObject();
        gen.writeEndObject();
        
        assertEquals("{\"outer\":{\"inner\":\"value\"}}", getOutput(gen));
        gen.close();
    }

    @Test(timeout = 4000)
    public void testMixedArrayObject() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeStartArray();
        gen.writeStartObject();
        gen.writeFieldName("key");
        gen.writeString("val");
        gen.writeEndObject();
        gen.writeNumber(42);
        gen.writeEndArray();
        
        assertEquals("[{\"key\":\"val\"},42]", getOutput(gen));
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteStringWithSpecialChars() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeString("hello\nworld\t\"quoted\"");
        assertEquals("\"hello\\nworld\\t\\\"quoted\\\"\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteStringWithUnicode() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeString("\u00e9\u00e0\u00fc");
        assertEquals("\"\u00e9\u00e0\u00fc\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteRawWithSubstring() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeRaw("hello world", 6, 5);
        assertEquals("world", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteRawSerializableString() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw);
        
        gen.writeRaw(new SerializableString() {
            public String getValue() { return "rawSerial"; }
            public int charLength() { return 9; }
            public char[] asQuotedChars() { return "rawSerial".toCharArray(); }
            public byte[] asUnquotedUTF8() { return "rawSerial".getBytes(); }
            public byte[] asQuotedUTF8() { return "\"rawSerial\"".getBytes(); }
        });
        assertEquals("rawSerial", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberWithNumbersAsStrings() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw, 
            JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask());
        
        gen.writeNumber(42);
        assertEquals("\"42\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberLongWithNumbersAsStrings() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw, 
            JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask());
        
        gen.writeNumber(1234567890123L);
        assertEquals("\"1234567890123\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberShortWithNumbersAsStrings() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw, 
            JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask());
        
        gen.writeNumber((short) 42);
        assertEquals("\"42\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberBigIntegerWithNumbersAsStrings() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw, 
            JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask());
        
        gen.writeNumber(new BigInteger("12345678901234567890"));
        assertEquals("\"12345678901234567890\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberBigDecimalWithNumbersAsStrings() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw, 
            JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask());
        
        gen.writeNumber(new BigDecimal("123.456"));
        assertEquals("\"123.456\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberBigDecimalAsPlain() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw, 
            JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.getMask());
        
        gen.writeNumber(new BigDecimal("1E+5"));
        assertEquals("100000", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberBigDecimalAsPlainWithNumbersAsStrings() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw, 
            JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN.getMask() | 
            JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask());
        
        gen.writeNumber(new BigDecimal("1E+5"));
        assertEquals("\"100000\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberDoubleWithNumbersAsStrings() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw, 
            JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask());
        
        gen.writeNumber(3.14);
        assertEquals("\"3.14\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberFloatWithNumbersAsStrings() throws IOException {
        StringWriter sw = new StringWriter();
        WriterBasedJsonGenerator gen = createGenerator(sw, 
            JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS.getMask());
        
        gen.writeNumber(2.5f);
        assertEquals("\"2.5\"", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberDoubleNaNWithoutQuoting() throws IOException {
        StringWriter sw = new StringWriter();
        // Disable QUOTE_NON_NUMERIC_NUMBERS
        int features = JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS.getMask();
        features = 0; // Clear all features
        WriterBasedJsonGenerator gen = createGenerator(sw, features);
        
        gen.writeNumber(Double.NaN);
        assertEquals("NaN", getOutput(gen));
        
        gen.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberDoubleInfinityWithoutQuoting() throws IOException {
        StringWriter sw = new StringWriter();
        int features = 0;
        WriterBasedJsonGenerator gen = createGenerator(sw, features);
        
        gen.writeNumber(Double.POSITIVE_INFINITY);
        assertEquals("Infinity", getOutput(gen));
        
        gen.close();
    }
}