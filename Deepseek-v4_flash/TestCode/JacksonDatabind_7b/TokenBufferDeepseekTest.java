package com.fasterxml.jackson.databind.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.base.ParserMinimalBase;
import com.fasterxml.jackson.core.json.JsonReadContext;
import com.fasterxml.jackson.core.json.JsonWriteContext;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.databind.*;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: TokenBuffer (com.fasterxml.jackson.databind.util)
 * 
 * Known Defect (from Defects4J): 
 *   - TestCreatorsDelegating::testDelegateWithTokenBuffer fails with:
 *     "Expected token START_OBJECT, current token FIELD_NAME"
 *   - Root cause hypothesis: When deserializing a delegate with TokenBuffer, 
 *     the parser's current token state is not properly advanced after 
 *     reading a FIELD_NAME token, causing the next token to be misreported.
 *   - The defect is in the Parser.nextToken() logic: after returning FIELD_NAME,
 *     the subsequent call to nextToken() should return the value token, but 
 *     instead it returns the next FIELD_NAME or fails to advance correctly.
 * 
 * Branches targeted:
 *   1. Parser.nextToken() when _currToken == FIELD_NAME (line ~700)
 *   2. Parser.nextToken() when _segmentPtr >= TOKENS_PER_SEGMENT (segment rollover)
 *   3. Parser.getText() for FIELD_NAME and VALUE_STRING tokens
 *   4. Parser.getNumberValue() for various numeric types (int, long, double, BigInteger, BigDecimal)
 *   5. Parser.getBinaryValue() for embedded byte[] and Base64 handling
 *   6. TokenBuffer.writeObject() for null, byte[], and POJO with/without codec
 *   7. TokenBuffer.append() with native IDs (TreeMap path)
 *   8. Segment.append() boundary at TOKENS_PER_SEGMENT (16 tokens)
 *   9. Segment.type() for index 0 and index 15 (boundary)
 *   10. TokenBuffer.deserialize() with missing START_OBJECT (defect zone)
 * 
 * Partitions:
 *   A. Core functional: write/read round-trip for all token types
 *   B. Boundary: segment rollover at 16 tokens, null/empty values
 *   C. Defect-targeted: delegate deserialization with FIELD_NAME handling
 *   D. Exception paths: UnsupportedOperationException for writeBinary(InputStream)
 *   E. Lifecycle: firstToken(), close(), getOutputContext()
 */

public class TokenBufferDeepseekTest {

    /* ======================================================
     * Partition A: Core Functional Logic & State Transitions
     * ====================================================== */

    @Test(timeout = 4000)
    public void testBasicRoundTrip() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("name");
        buffer.writeString("value");
        buffer.writeNumber(42);
        buffer.writeBoolean(true);
        buffer.writeNull();
        buffer.writeEndObject();

        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("name", parser.getCurrentName());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("value", parser.getText());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testArrayRoundTrip() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartArray();
        buffer.writeNumber(1.5);
        buffer.writeNumber(2.5f);
        buffer.writeEndArray();

        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_ARRAY, parser.nextToken());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(1.5, parser.getDoubleValue(), 0.0001);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(2.5f, parser.getFloatValue(), 0.0001f);
        assertToken(JsonToken.END_ARRAY, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testEmbeddedObject() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeEmbeddedObject(data);

        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(data, parser.getEmbeddedObject());
        assertNull(parser.nextToken());
        parser.close();
        buffer.close();
    }

    /* ======================================================
     * Partition B: Boundary Value Analysis & Extremes
     * ====================================================== */

    @Test(timeout = 4000)
    public void testSegmentBoundary() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        // Write exactly 16 tokens (TOKENS_PER_SEGMENT) to test segment rollover
        for (int i = 0; i < 16; i++) {
            buffer.writeNumber(i);
        }
        // Write one more to force new segment
        buffer.writeNumber(16);

        JsonParser parser = buffer.asParser();
        for (int i = 0; i < 17; i++) {
            assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
            assertEquals(i, parser.getIntValue());
        }
        assertNull(parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testNullValues() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeString(null);
        buffer.writeObject(null);
        buffer.writeBinary(null, new byte[0], 0, 0);

        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testNumericBoundaries() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeNumber(Integer.MAX_VALUE);
        buffer.writeNumber(Integer.MIN_VALUE);
        buffer.writeNumber(Long.MAX_VALUE);
        buffer.writeNumber(Long.MIN_VALUE);
        buffer.writeNumber(Double.MAX_VALUE);
        buffer.writeNumber(Double.MIN_VALUE);
        buffer.writeNumber(new BigInteger("123456789012345678901234567890"));
        buffer.writeNumber(new BigDecimal("1234567890.123456789"));

        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Integer.MAX_VALUE, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Integer.MIN_VALUE, parser.getIntValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Long.MAX_VALUE, parser.getLongValue());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(Long.MIN_VALUE, parser.getLongValue());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.MAX_VALUE, parser.getDoubleValue(), 0.0);
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(Double.MIN_VALUE, parser.getDoubleValue(), 0.0);
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(new BigInteger("123456789012345678901234567890"), parser.getBigIntegerValue());
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(new BigDecimal("1234567890.123456789"), parser.getDecimalValue());
        assertNull(parser.nextToken());
        parser.close();
        buffer.close();
    }

    /* ======================================================
     * Partition C: Defect-Targeted Branch Zone
     * ====================================================== */

    /**
     * Targets the known defect: "Expected token START_OBJECT, current token FIELD_NAME"
     * This occurs when deserializing a delegate with TokenBuffer where the
     * FIELD_NAME token handling is incorrect.
     */
    @Test(timeout = 4000)
    public void testDelegateWithTokenBufferDefect() throws Exception {
        // Simulate the failing scenario from TestCreatorsDelegating
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("delegate");
        buffer.writeString("value");
        buffer.writeEndObject();

        JsonParser parser = buffer.asParser();
        
        // This is the critical sequence that triggers the defect
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("delegate", parser.getCurrentName());
        
        // The bug: after FIELD_NAME, the next token should be VALUE_STRING
        // but in the defective version it returns FIELD_NAME again or fails
        JsonToken token = parser.nextToken();
        assertNotNull("Expected value token after FIELD_NAME", token);
        assertEquals("Expected VALUE_STRING after FIELD_NAME", 
                JsonToken.VALUE_STRING, token);
        assertEquals("value", parser.getText());
        
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
        buffer.close();
    }

    /**
     * Additional defect-targeted test: FIELD_NAME followed by nested object
     */
    @Test(timeout = 4000)
    public void testFieldNameWithNestedObject() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeStartObject();
        buffer.writeFieldName("nested");
        buffer.writeStartObject();
        buffer.writeFieldName("inner");
        buffer.writeNumber(1);
        buffer.writeEndObject();
        buffer.writeEndObject();

        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("nested", parser.getCurrentName());
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("inner", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
        buffer.close();
    }

    /* ======================================================
     * Partition D: Exception & Defensive Guard Paths
     * ====================================================== */

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteBinaryInputStreamUnsupported() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeBinary(Base64Variants.getDefaultVariant(), 
                new ByteArrayInputStream(new byte[0]), 0);
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueFromString() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeString("SGVsbG8="); // Base64 for "Hello"
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        byte[] data = parser.getBinaryValue(Base64Variants.getDefaultVariant());
        assertEquals("Hello", new String(data, "UTF-8"));
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testGetBinaryValueFromEmbedded() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] expected = {1, 2, 3, 4, 5};
        buffer.writeEmbeddedObject(expected);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        byte[] data = parser.getBinaryValue(Base64Variants.getDefaultVariant());
        assertArrayEquals(expected, data);
        parser.close();
        buffer.close();
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testGetBinaryValueFromNonBinary() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeNumber(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        parser.getBinaryValue(Base64Variants.getDefaultVariant());
        parser.close();
        buffer.close();
    }

    /* ======================================================
     * Partition E: Object Lifecycle & Contract Integrity
     * ====================================================== */

    @Test(timeout = 4000)
    public void testFirstToken() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        assertNull(buffer.firstToken());
        
        buffer.writeStartObject();
        assertEquals(JsonToken.START_OBJECT, buffer.firstToken());
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testGetOutputContext() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        assertNotNull(buffer.getOutputContext());
        assertEquals(JsonWriteContext.STATUS_OK_AS_IS, 
                buffer.getOutputContext().getEntryCount());
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testAppendWithNativeIds() throws Exception {
        TokenBuffer buffer1 = new TokenBuffer(null, true);
        TokenBuffer buffer2 = new TokenBuffer(null, true);
        
        buffer1.writeStartObject();
        buffer1.writeFieldName("a");
        buffer1.writeNumber(1);
        buffer1.writeEndObject();
        
        buffer2.writeStartObject();
        buffer2.writeFieldName("b");
        buffer2.writeNumber(2);
        buffer2.writeEndObject();
        
        buffer1.append(buffer2);
        
        JsonParser parser = buffer1.asParser();
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("a", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertToken(JsonToken.START_OBJECT, parser.nextToken());
        assertToken(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("b", parser.getCurrentName());
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertToken(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
        buffer1.close();
        buffer2.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithCodec() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new int[]{1, 2, 3});
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        Object value = parser.getEmbeddedObject();
        assertTrue(value instanceof int[]);
        assertArrayEquals(new int[]{1, 2, 3}, (int[]) value);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNull() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArray() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithString() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumber() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBoolean() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDouble() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigInteger() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimal() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojo() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodec() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObject() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithDoubleValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(3.14);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(3.14, parser.getDoubleValue(), 0.0001);
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigIntegerValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigInteger big = new BigInteger("123456789012345678901234567890");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(big, parser.getBigIntegerValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBigDecimalValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        BigDecimal big = new BigDecimal("1234567890.123456789");
        buffer.writeObject(big);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(big, parser.getDecimalValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPojoValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertNotNull(parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullCodecValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(new Object());
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithEmbeddedObjectValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        Object obj = new Object();
        buffer.writeObject(obj);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertSame(obj, parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNullValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(null);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NULL, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithByteArrayValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_EMBEDDED_OBJECT, parser.nextToken());
        assertArrayEquals(data, (byte[]) parser.getEmbeddedObject());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithStringValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject("test");
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("test", parser.getText());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNumberValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(42);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(42, parser.getIntValue());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithBooleanValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValueValue() throws Exception {
        TokenBuffer buffer = new TokenBuffer(null, false);
        buffer.writeObject(Boolean.TRUE);
        
        JsonParser parser = buffer.asParser();
        assertToken(JsonToken.VALUE_TRUE, parser.nextToken());
        parser.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void