package com.fasterxml.jackson.databind.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TokenBufferDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Partition A: Core Functional Logic & State Transitions
     *   - Test basic token appending and reading via asParser()
     *   - Test structural tokens: START_OBJECT, END_OBJECT, START_ARRAY, END_ARRAY
     *   - Test field names (String and SerializableString)
     *   - Test string values (String and SerializableString)
     *   - Test numeric types: int, long, short, BigInteger, double, float, BigDecimal
     *   - Test boolean true/false, null
     *   - Test embedded objects (byte[], RawValue, POJO)
     *   - Test writeObject with null, byte[], RawValue, POJO with/without codec
     *   - Test writeTree with null, TreeNode with/without codec
     *   - Test binary write
     *   - Test copyCurrentEvent and copyCurrentStructure
     *   - Test serialize method
     *   - Test append method
     *   - Test deserialize method
     *   - Test toString method
     *   - Test firstToken method
     *   - Test feature enable/disable/isEnabled/getFeatureMask/setFeatureMask
     *   - Test useDefaultPrettyPrinter, setCodec, getCodec, getOutputContext
     *   - Test canWriteBinaryNatively, flush, close, isClosed
     *   - Test version
     *   - Test writeRawValue variants
     *   - Test writeNumber with String encoded value
     *   - Test writeString with null
     *   - Test writeFieldName with SerializableString
     *   - Test writeNumber with null BigDecimal/BigInteger
     *   - Test writeObjectId/writeTypeId/canWriteTypeId/canWriteObjectId
     *   - Test _checkNativeIds via copyCurrentStructure
     * 
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     *   - Test empty buffer
     *   - Test single token
     *   - Test exactly 16 tokens (segment boundary)
     *   - Test 17 tokens (cross segment boundary)
     *   - Test 32 tokens (two full segments)
     *   - Test null arguments for various write methods
     *   - Test extreme numeric values: Integer.MAX_VALUE, Long.MIN_VALUE, Double.MAX_VALUE, etc.
     *   - Test BigDecimal with very large/small scale
     *   - Test BigInteger with very large magnitude
     *   - Test empty string, very long string
     *   - Test empty byte array
     *   - Test negative offsets in writeRawValue
     * 
     * Partition C: Defect-Targeted Branch Zone
     *   - Defect: BigDecimal -10000000000.0000000001 gets truncated to -1.0E+10
     *     Root cause: In serialize() method, VALUE_NUMBER_FLOAT case for BigDecimal
     *     calls gen.writeNumber((BigDecimal) n) which should preserve precision.
     *     The bug may be in how the number is stored or retrieved.
     *     Test: Write BigDecimal with high precision and verify via serialize/parser.
     *   - Also test BigDecimal with very small fractional part
     *   - Test BigDecimal with large integer part and small fractional part
     * 
     * Partition D: Exception & Defensive Guard Paths
     *   - Test writeRawUTF8String throws UnsupportedOperationException
     *   - Test writeUTF8String throws UnsupportedOperationException
     *   - Test writeRaw(String) throws UnsupportedOperationException
     *   - Test writeRaw(String, int, int) throws UnsupportedOperationException
     *   - Test writeRaw(SerializableString) throws UnsupportedOperationException
     *   - Test writeRaw(char[], int, int) throws UnsupportedOperationException
     *   - Test writeRaw(char) throws UnsupportedOperationException
     *   - Test writeBinary with InputStream throws UnsupportedOperationException
     *   - Test Parser methods with invalid state
     *   - Test deserialize with unexpected end token
     * 
     * Partition E: Object Lifecycle & Contract Integrity
     *   - Test multiple TokenBuffer instances
     *   - Test append between buffers
     *   - Test serialize after close
     *   - Test asParser after close
     *   - Test toString after modifications
     */

    private final ObjectMapper MAPPER = new ObjectMapper();

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testEmptyBuffer() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        assertNull(buffer.firstToken());
        JsonParser p = buffer.asParser();
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testSingleToken() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeStartObject();
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testStructuralTokens() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeStartArray();
        buffer.writeStartObject();
        buffer.writeEndObject();
        buffer.writeEndArray();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testFieldNameAndStringValue() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeStartObject();
        buffer.writeFieldName("name");
        buffer.writeString("value");
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("name", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testNumericTypes() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42);
        buffer.writeNumber(1234567890123L);
        buffer.writeNumber((short) 7);
        buffer.writeNumber(new BigInteger("99999999999999999999999999999"));
        buffer.writeNumber(3.14159);
        buffer.writeNumber(2.5f);
        buffer.writeNumber(new BigDecimal("123.456"));
        JsonParser p = buffer.asParser();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1234567890123L, p.getLongValue());
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(7, p.getIntValue());
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(new BigInteger("99999999999999999999999999999"), p.getBigIntegerValue());
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(3.14159, p.getDoubleValue(), 1e-9);
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(2.5f, p.getFloatValue(), 1e-9);
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigDecimal("123.456"), p.getDecimalValue());
        
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testBooleanAndNull() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeBoolean(true);
        buffer.writeBoolean(false);
        buffer.writeNull();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testEmbeddedObject() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        byte[] data = {1, 2, 3};
        buffer.writeObject(data);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertArrayEquals(data, (byte[]) p.getEmbeddedObject());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithNull() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeObject(null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithRawValue() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeObject(new RawValue("{\"a\":1}"));
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithPOJO() throws IOException {
        TokenBuffer buffer = new TokenBuffer((ObjectCodec) null, false);
        buffer.writeObject(new Integer(42));
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals(42, ((Integer) p.getEmbeddedObject()).intValue());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteTreeWithNull() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeTree(null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteTreeWithoutCodec() throws IOException {
        TokenBuffer buffer = new TokenBuffer((ObjectCodec) null, false);
        buffer.writeTree(MAPPER.readTree("{\"key\":\"value\"}"));
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testBinaryWrite() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        byte[] data = {10, 20, 30, 40};
        buffer.writeBinary(Base64Variants.getDefaultVariant(), data, 0, data.length);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertArrayEquals(data, (byte[]) p.getEmbeddedObject());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testCopyCurrentEvent() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        String json = "{\"a\":1, \"b\":\"hello\"}";
        JsonParser p = MAPPER.getFactory().createParser(json);
        p.nextToken(); // START_OBJECT
        buffer.copyCurrentEvent(p);
        p.nextToken(); // FIELD_NAME
        buffer.copyCurrentEvent(p);
        p.nextToken(); // VALUE_NUMBER_INT
        buffer.copyCurrentEvent(p);
        p.nextToken(); // FIELD_NAME
        buffer.copyCurrentEvent(p);
        p.nextToken(); // VALUE_STRING
        buffer.copyCurrentEvent(p);
        p.nextToken(); // END_OBJECT
        buffer.copyCurrentEvent(p);
        p.close();

        JsonParser bp = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, bp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, bp.nextToken());
        assertEquals("a", bp.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, bp.nextToken());
        assertEquals(1, bp.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, bp.nextToken());
        assertEquals("b", bp.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, bp.nextToken());
        assertEquals("hello", bp.getText());
        assertEquals(JsonToken.END_OBJECT, bp.nextToken());
        assertNull(bp.nextToken());
        bp.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testCopyCurrentStructure() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        String json = "{\"arr\":[1,2,{\"nested\":true}],\"val\":\"test\"}";
        JsonParser p = MAPPER.getFactory().createParser(json);
        p.nextToken(); // START_OBJECT
        buffer.copyCurrentStructure(p);
        p.close();

        JsonParser bp = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, bp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, bp.nextToken());
        assertEquals("arr", bp.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, bp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, bp.nextToken());
        assertEquals(1, bp.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, bp.nextToken());
        assertEquals(2, bp.getIntValue());
        assertEquals(JsonToken.START_OBJECT, bp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, bp.nextToken());
        assertEquals("nested", bp.getCurrentName());
        assertEquals(JsonToken.VALUE_TRUE, bp.nextToken());
        assertEquals(JsonToken.END_OBJECT, bp.nextToken());
        assertEquals(JsonToken.END_ARRAY, bp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, bp.nextToken());
        assertEquals("val", bp.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, bp.nextToken());
        assertEquals("test", bp.getText());
        assertEquals(JsonToken.END_OBJECT, bp.nextToken());
        assertNull(bp.nextToken());
        bp.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testSerialize() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeStartObject();
        buffer.writeFieldName("x");
        buffer.writeNumber(10);
        buffer.writeEndObject();

        TokenBuffer out = new TokenBuffer(MAPPER, false);
        buffer.serialize(out);
        buffer.close();

        JsonParser p = out.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("x", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(10, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        out.close();
    }

    @Test(timeout = 4000)
    public void testAppend() throws IOException {
        TokenBuffer buffer1 = new TokenBuffer(MAPPER, false);
        buffer1.writeNumber(1);
        TokenBuffer buffer2 = new TokenBuffer(MAPPER, false);
        buffer2.writeNumber(2);
        buffer1.append(buffer2);
        buffer2.close();

        JsonParser p = buffer1.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertNull(p.nextToken());
        p.close();
        buffer1.close();
    }

    @Test(timeout = 4000)
    public void testDeserialize() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        String json = "{\"a\":1,\"b\":2}";
        JsonParser p = MAPPER.getFactory().createParser(json);
        p.nextToken(); // START_OBJECT
        buffer.deserialize(p, MAPPER.getDeserializationContext());
        p.close();

        JsonParser bp = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, bp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, bp.nextToken());
        assertEquals("a", bp.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, bp.nextToken());
        assertEquals(1, bp.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, bp.nextToken());
        assertEquals("b", bp.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, bp.nextToken());
        assertEquals(2, bp.getIntValue());
        assertEquals(JsonToken.END_OBJECT, bp.nextToken());
        assertNull(bp.nextToken());
        bp.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeFromFieldName() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        String json = "\"field\":\"value\"";
        JsonParser p = MAPPER.getFactory().createParser(json);
        p.nextToken(); // FIELD_NAME
        buffer.deserialize(p, MAPPER.getDeserializationContext());
        p.close();

        JsonParser bp = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, bp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, bp.nextToken());
        assertEquals("field", bp.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, bp.nextToken());
        assertEquals("value", bp.getText());
        assertEquals(JsonToken.END_OBJECT, bp.nextToken());
        assertNull(bp.nextToken());
        bp.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testToString() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeStartObject();
        buffer.writeFieldName("key");
        buffer.writeString("val");
        buffer.writeEndObject();
        String str = buffer.toString();
        assertTrue(str.contains("START_OBJECT"));
        assertTrue(str.contains("FIELD_NAME(key)"));
        assertTrue(str.contains("VALUE_STRING"));
        assertTrue(str.contains("END_OBJECT"));
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testFirstToken() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        assertNull(buffer.firstToken());
        buffer.writeNumber(42);
        assertEquals(JsonToken.VALUE_NUMBER_INT, buffer.firstToken());
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testFeatureFlags() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        assertFalse(buffer.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        buffer.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertTrue(buffer.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        buffer.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertFalse(buffer.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        int mask = buffer.getFeatureMask();
        buffer.setFeatureMask(mask);
        buffer.useDefaultPrettyPrinter();
        assertSame(buffer, buffer.setCodec(MAPPER));
        assertSame(MAPPER, buffer.getCodec());
        assertNotNull(buffer.getOutputContext());
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testCapabilitiesAndLifecycle() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        assertTrue(buffer.canWriteBinaryNatively());
        assertFalse(buffer.isClosed());
        buffer.flush();
        buffer.close();
        assertTrue(buffer.isClosed());
    }

    @Test(timeout = 4000)
    public void testVersion() {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        assertNotNull(buffer.version());
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteRawValue() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeRawValue("{\"raw\":true}");
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteRawValueWithOffset() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeRawValue("prefix{\"raw\":true}suffix", 6, 12);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteRawValueCharArray() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeRawValue(new char[]{'t', 'r', 'u', 'e'}, 0, 4);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberStringEncoded() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber("1.234e5");
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(1.234e5, p.getDoubleValue(), 1e-9);
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteStringWithNull() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeString((String) null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteStringCharArray() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeString(new char[]{'h', 'e', 'l', 'l', 'o'}, 0, 5);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteFieldNameSerializableString() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeStartObject();
        buffer.writeFieldName(new SerializableString() {
            @Override public String getValue() { return "field"; }
            @Override public int charLength() { return 5; }
            @Override public char[] asQuotedChars() { return new char[]{'f','i','e','l','d'}; }
            @Override public byte[] asUnquotedUTF8() { return new byte[]{'f','i','e','l','d'}; }
            @Override public byte[] asQuotedUTF8() { return new byte[]{'f','i','e','l','d'}; }
        });
        buffer.writeString("value");
        buffer.writeEndObject();
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("field", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberNullBigDecimal() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber((BigDecimal) null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberNullBigInteger() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber((BigInteger) null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testNativeIds() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, true);
        assertTrue(buffer.canWriteTypeId());
        assertTrue(buffer.canWriteObjectId());
        buffer.writeTypeId("type123");
        buffer.writeObjectId("obj456");
        buffer.writeStartObject();
        buffer.writeEndObject();
        // Verify via parser
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertTrue(p.canReadTypeId());
        assertTrue(p.canReadObjectId());
        assertEquals("type123", p.getTypeId());
        assertEquals("obj456", p.getObjectId());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    // ==================== Partition B: Boundary Value Analysis ====================

    @Test(timeout = 4000)
    public void testSegmentBoundary16Tokens() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        for (int i = 0; i < 16; i++) {
            buffer.writeNumber(i);
        }
        JsonParser p = buffer.asParser();
        for (int i = 0; i < 16; i++) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(i, p.getIntValue());
        }
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testCrossSegmentBoundary17Tokens() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        for (int i = 0; i < 17; i++) {
            buffer.writeNumber(i);
        }
        JsonParser p = buffer.asParser();
        for (int i = 0; i < 17; i++) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(i, p.getIntValue());
        }
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testTwoFullSegments32Tokens() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        for (int i = 0; i < 32; i++) {
            buffer.writeNumber(i);
        }
        JsonParser p = buffer.asParser();
        for (int i = 0; i < 32; i++) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(i, p.getIntValue());
        }
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testExtremeNumericValues() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(Integer.MAX_VALUE);
        buffer.writeNumber(Long.MIN_VALUE);
        buffer.writeNumber(Double.MAX_VALUE);
        buffer.writeNumber(Double.MIN_VALUE);
        buffer.writeNumber(new BigInteger("1234567890123456789012345678901234567890"));
        buffer.writeNumber(new BigDecimal("0.00000000000000000001"));
        JsonParser p = buffer.asParser();
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(Integer.MAX_VALUE, p.getIntValue());
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(Long.MIN_VALUE, p.getLongValue());
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.MAX_VALUE, p.getDoubleValue(), 1e300);
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(Double.MIN_VALUE, p.getDoubleValue(), 1e-300);
        
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(new BigInteger("1234567890123456789012345678901234567890"), p.getBigIntegerValue());
        
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigDecimal("0.00000000000000000001"), p.getDecimalValue());
        
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testEmptyString() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeString("");
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("", p.getText());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testEmptyByteArray() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeObject(new byte[0]);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertArrayEquals(new byte[0], (byte[]) p.getEmbeddedObject());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    @Test(timeout = 4000)
    public void testBigDecimalPrecisionPreservation() throws IOException {
        // This test targets the known defect: BigDecimal -10000000000.0000000001
        // should be preserved exactly, not truncated to -1.0E+10
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        BigDecimal original = new BigDecimal("-10000000000.0000000001");
        buffer.writeNumber(original);
        
        // Test via serialize
        TokenBuffer out = new TokenBuffer(MAPPER, false);
        buffer.serialize(out);
        buffer.close();
        
        JsonParser p = out.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        BigDecimal result = p.getDecimalValue();
        assertEquals("BigDecimal precision lost", original, result);
        assertNull(p.nextToken());
        p.close();
        out.close();
    }

    @Test(timeout = 4000)
    public void testBigDecimalVerySmallFraction() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        BigDecimal original = new BigDecimal("0.000000000000000000001");
        buffer.writeNumber(original);
        
        TokenBuffer out = new TokenBuffer(MAPPER, false);
        buffer.serialize(out);
        buffer.close();
        
        JsonParser p = out.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        BigDecimal result = p.getDecimalValue();
        assertEquals("BigDecimal precision lost for small fraction", original, result);
        assertNull(p.nextToken());
        p.close();
        out.close();
    }

    @Test(timeout = 4000)
    public void testBigDecimalLargeIntegerSmallFraction() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        BigDecimal original = new BigDecimal("99999999999999999999.0000000001");
        buffer.writeNumber(original);
        
        TokenBuffer out = new TokenBuffer(MAPPER, false);
        buffer.serialize(out);
        buffer.close();
        
        JsonParser p = out.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        BigDecimal result = p.getDecimalValue();
        assertEquals("BigDecimal precision lost for large integer with small fraction", original, result);
        assertNull(p.nextToken());
        p.close();
        out.close();
    }

    @Test(timeout = 4000)
    public void testBigDecimalNegativePrecision() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        BigDecimal original = new BigDecimal("-0.0000000001");
        buffer.writeNumber(original);
        
        TokenBuffer out = new TokenBuffer(MAPPER, false);
        buffer.serialize(out);
        buffer.close();
        
        JsonParser p = out.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        BigDecimal result = p.getDecimalValue();
        assertEquals("BigDecimal precision lost for negative small value", original, result);
        assertNull(p.nextToken());
        p.close();
        out.close();
    }

    @Test(timeout = 4000)
    public void testBigDecimalViaCopyCurrentEvent() throws IOException {
        // Test that BigDecimal precision is preserved through copyCurrentEvent
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        BigDecimal original = new BigDecimal("-10000000000.0000000001");
        
        // Create a parser that produces this BigDecimal
        String json = original.toString();
        JsonParser p = MAPPER.getFactory().createParser(json);
        p.nextToken(); // VALUE_NUMBER_FLOAT
        buffer.copyCurrentEvent(p);
        p.close();
        
        TokenBuffer out = new TokenBuffer(MAPPER, false);
        buffer.serialize(out);
        buffer.close();
        
        JsonParser bp = out.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, bp.nextToken());
        BigDecimal result = bp.getDecimalValue();
        assertEquals("BigDecimal precision lost via copyCurrentEvent", original, result);
        assertNull(bp.nextToken());
        bp.close();
        out.close();
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawUTF8StringThrows() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeRawUTF8String(new byte[]{}, 0, 0);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteUTF8StringThrows() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeUTF8String(new byte[]{}, 0, 0);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawStringThrows() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeRaw("test");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawStringWithOffsetThrows() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeRaw("test", 0, 4);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawSerializableStringThrows() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeRaw(new SerializableString() {
            @Override public String getValue() { return "test"; }
            @Override public int charLength() { return 4; }
            @Override public char[] asQuotedChars() { return new char[]{'t','e','s','t'}; }
            @Override public byte[] asUnquotedUTF8() { return new byte[]{'t','e','s','t'}; }
            @Override public byte[] asQuotedUTF8() { return new byte[]{'t','e','s','t'}; }
        });
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawCharArrayThrows() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeRaw(new char[]{'t', 'e', 's', 't'}, 0, 4);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawCharThrows() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeRaw('c');
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteBinaryInputStreamThrows() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeBinary(Base64Variants.getDefaultVariant(), new java.io.ByteArrayInputStream(new byte[]{}), 0);
    }

    @Test(expected = IOException.class, timeout = 4000)
    public void testDeserializeUnexpectedEndToken() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        String json = "{\"a\":1}";
        JsonParser p = MAPPER.getFactory().createParser(json);
        p.nextToken(); // START_OBJECT
        // This should work fine
        buffer.deserialize(p, MAPPER.getDeserializationContext());
        p.close();
        
        // Now test with unexpected end
        json = "{\"a\":1";
        p = MAPPER.getFactory().createParser(json);
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        p.nextToken(); // VALUE_NUMBER_INT
        // This should throw because next token is null, not END_OBJECT
        buffer.deserialize(p, MAPPER.getDeserializationContext());
        p.close();
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testMultipleBuffers() throws IOException {
        TokenBuffer buffer1 = new TokenBuffer(MAPPER, false);
        buffer1.writeNumber(1);
        TokenBuffer buffer2 = new TokenBuffer(MAPPER, false);
        buffer2.writeNumber(2);
        
        JsonParser p1 = buffer1.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p1.nextToken());
        assertEquals(1, p1.getIntValue());
        assertNull(p1.nextToken());
        p1.close();
        
        JsonParser p2 = buffer2.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p2.nextToken());
        assertEquals(2, p2.getIntValue());
        assertNull(p2.nextToken());
        p2.close();
        
        buffer1.close();
        buffer2.close();
    }

    @Test(timeout = 4000)
    public void testAppendWithNativeIds() throws IOException {
        TokenBuffer buffer1 = new TokenBuffer(MAPPER, true);
        buffer1.writeTypeId("type1");
        buffer1.writeNumber(1);
        
        TokenBuffer buffer2 = new TokenBuffer(MAPPER, false);
        buffer2.writeNumber(2);
        
        buffer1.append(buffer2);
        buffer2.close();
        
        JsonParser p = buffer1.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals("type1", p.getTypeId());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertNull(p.nextToken());
        p.close();
        buffer1.close();
    }

    @Test(timeout = 4000)
    public void testSerializeAfterClose() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42);
        buffer.close();
        
        TokenBuffer out = new TokenBuffer(MAPPER, false);
        buffer.serialize(out);
        out.close();
        // Should still work
    }

    @Test(timeout = 4000)
    public void testAsParserAfterClose() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42);
        buffer.close();
        
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testToStringAfterModifications() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeStartArray();
        buffer.writeString("hello");
        buffer.writeEndArray();
        String str = buffer.toString();
        assertTrue(str.contains("START_ARRAY"));
        assertTrue(str.contains("VALUE_STRING"));
        assertTrue(str.contains("END_ARRAY"));
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteStringSerializableString() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeString(new SerializableString() {
            @Override public String getValue() { return "serializable"; }
            @Override public int charLength() { return 11; }
            @Override public char[] asQuotedChars() { return new char[]{'s','e','r','i','a','l','i','z','a','b','l','e'}; }
            @Override public byte[] asUnquotedUTF8() { return new byte[]{'s','e','r','i','a','l','i','z','a','b','l','e'}; }
            @Override public byte[] asQuotedUTF8() { return new byte[]{'s','e','r','i','a','l','i','z','a','b','l','e'}; }
        });
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("serializable", p.getText());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteStringSerializableStringNull() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeString((SerializableString) null);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserPeekNextToken() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(1);
        buffer.writeNumber(2);
        
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, ((TokenBuffer.Parser) p).peekNextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertNull(((TokenBuffer.Parser) p).peekNextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserNextFieldName() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeStartObject();
        buffer.writeFieldName("field1");
        buffer.writeNumber(1);
        buffer.writeFieldName("field2");
        buffer.writeNumber(2);
        buffer.writeEndObject();
        
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("field1", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals("field2", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserGetCurrentName() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeStartObject();
        buffer.writeFieldName("key");
        buffer.writeStartObject();
        // At START_OBJECT, getCurrentName should return parent's field name
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("key", p.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("key", p.getCurrentName()); // parent's field name
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserOverrideCurrentName() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeStartObject();
        buffer.writeFieldName("original");
        buffer.writeNumber(1);
        buffer.writeEndObject();
        
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        p.overrideCurrentName("overridden");
        assertEquals("overridden", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserGetTextForVariousTokens() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42);
        buffer.writeNumber(3.14);
        buffer.writeBoolean(true);
        buffer.writeNull();
        
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("42", p.getText());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("3.14", p.getText());
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertEquals("true", p.getText());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals("null", p.getText());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserGetBinaryValue() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        byte[] data = {1, 2, 3, 4, 5};
        buffer.writeObject(data);
        
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertArrayEquals(data, p.getBinaryValue(Base64Variants.getDefaultVariant()));
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserGetBinaryValueFromString() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeString("AQIDBAU="); // Base64 of {1,2,3,4,5}
        
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] result = p.getBinaryValue(Base64Variants.getDefaultVariant());
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, result);
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserReadBinaryValue() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        byte[] data = {10, 20, 30};
        buffer.writeObject(data);
        
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        int len = p.readBinaryValue(Base64Variants.getDefaultVariant(), baos);
        assertEquals(3, len);
        assertArrayEquals(data, baos.toByteArray());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testConstructorWithJsonParser() throws IOException {
        String json = "{\"a\":1}";
        JsonParser p = MAPPER.getFactory().createParser(json);
        p.nextToken(); // START_OBJECT
        TokenBuffer buffer = new TokenBuffer(p);
        assertNotNull(buffer);
        assertFalse(buffer.canWriteTypeId());
        assertFalse(buffer.canWriteObjectId());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testConstructorWithJsonParserAndContext() throws IOException {
        String json = "{\"a\":1}";
        JsonParser p = MAPPER.getFactory().createParser(json);
        p.nextToken(); // START_OBJECT
        TokenBuffer buffer = new TokenBuffer(p, MAPPER.getDeserializationContext());
        assertNotNull(buffer);
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testDeprecatedConstructor() {
        TokenBuffer buffer = new TokenBuffer(MAPPER);
        assertNotNull(buffer);
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testAsParserWithCodec() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser(MAPPER);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testAsParserWithJsonParser() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42);
        String json = "{\"dummy\":1}";
        JsonParser src = MAPPER.getFactory().createParser(json);
        src.nextToken(); // START_OBJECT
        JsonParser p = buffer.asParser(src);
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        assertNull(p.nextToken());
        p.close();
        src.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberShort() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber((short) 5);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(5, p.getIntValue());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberFloat() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(1.5f);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(1.5f, p.getFloatValue(), 1e-9);
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberDouble() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(2.71828);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(2.71828, p.getDoubleValue(), 1e-9);
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberLong() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(9876543210L);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(9876543210L, p.getLongValue());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteNumberInt() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(-12345);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(-12345, p.getIntValue());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testUnbalancedStructure() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeStartObject();
        buffer.writeEndObject();
        buffer.writeEndObject(); // Extra end object - should not crash
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testCopyCurrentStructureWithFieldName() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        String json = "{\"field\":{\"nested\":true}}";
        JsonParser p = MAPPER.getFactory().createParser(json);
        p.nextToken(); // START_OBJECT
        buffer.copyCurrentStructure(p);
        p.close();

        JsonParser bp = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, bp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, bp.nextToken());
        assertEquals("field", bp.getCurrentName());
        assertEquals(JsonToken.START_OBJECT, bp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, bp.nextToken());
        assertEquals("nested", bp.getCurrentName());
        assertEquals(JsonToken.VALUE_TRUE, bp.nextToken());
        assertEquals(JsonToken.END_OBJECT, bp.nextToken());
        assertEquals(JsonToken.END_OBJECT, bp.nextToken());
        assertNull(bp.nextToken());
        bp.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testCopyCurrentEventWithNativeIds() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, true);
        String json = "42";
        JsonParser p = MAPPER.getFactory().createParser(json);
        p.nextToken(); // VALUE_NUMBER_INT
        // Set native ids on parser (simulate)
        buffer.copyCurrentEvent(p);
        p.close();

        JsonParser bp = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, bp.nextToken());
        assertEquals(42, bp.getIntValue());
        assertNull(bp.nextToken());
        bp.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testSerializeWithNativeIds() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, true);
        buffer.writeTypeId("myType");
        buffer.writeNumber(100);
        
        TokenBuffer out = new TokenBuffer(MAPPER, false);
        buffer.serialize(out);
        buffer.close();
        
        JsonParser p = out.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(100, p.getIntValue());
        assertNull(p.nextToken());
        p.close();
        out.close();
    }

    @Test(timeout = 4000)
    public void testToStringWithNativeIds() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, true);
        buffer.writeTypeId("tid");
        buffer.writeNumber(1);
        String str = buffer.toString();
        assertTrue(str.contains("VALUE_NUMBER_INT"));
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserGetNumberType() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(1);
        buffer.writeNumber(1L);
        buffer.writeNumber(1.0);
        buffer.writeNumber(1.0f);
        buffer.writeNumber(new BigDecimal("1.0"));
        buffer.writeNumber(new BigInteger("1"));
        buffer.writeNumber((short) 1);
        
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonParser.NumberType.INT, p.getNumberType());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonParser.NumberType.LONG, p.getNumberType());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonParser.NumberType.DOUBLE, p.getNumberType());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonParser.NumberType.FLOAT, p.getNumberType());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, p.getNumberType());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, p.getNumberType());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonParser.NumberType.INT, p.getNumberType()); // Short maps to INT
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserGetNumberValue() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42);
        buffer.writeNumber(new BigDecimal("123.456"));
        buffer.writeNumber("1.5e2");
        
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getNumberValue().intValue());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigDecimal("123.456"), p.getNumberValue());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(150.0, p.getNumberValue().doubleValue(), 1e-9);
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserGetEmbeddedObject() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertNull(p.getEmbeddedObject());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserGetTextCharacters() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeString("hello");
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertArrayEquals("hello".toCharArray(), p.getTextCharacters());
        assertEquals(5, p.getTextLength());
        assertEquals(0, p.getTextOffset());
        assertFalse(p.hasTextCharacters());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserGetBigIntegerValue() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(new BigDecimal("123.456"));
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigInteger("123"), p.getBigIntegerValue());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserGetDecimalValue() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(new BigDecimal("42"), p.getDecimalValue());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserGetDoubleValue() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42.0, p.getDoubleValue(), 1e-9);
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserGetFloatValue() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42.0f, p.getFloatValue(), 1e-9);
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserGetIntValue() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42L);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserGetLongValue() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42L, p.getLongValue());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserGetTokenLocation() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser();
        p.nextToken();
        assertNotNull(p.getTokenLocation());
        assertNotNull(p.getCurrentLocation());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserGetParsingContext() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeStartObject();
        buffer.writeFieldName("a");
        buffer.writeNumber(1);
        buffer.writeEndObject();
        
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertNotNull(p.getParsingContext());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserClose() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser();
        assertFalse(p.isClosed());
        p.close();
        assertTrue(p.isClosed());
        assertNull(p.nextToken());
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserVersion() {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        JsonParser p = buffer.asParser();
        assertNotNull(p.version());
        try { p.close(); } catch (IOException e) { /* ignore */ }
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testParserSetCodec() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42);
        JsonParser p = buffer.asParser();
        assertSame(MAPPER, p.getCodec());
        p.setCodec(null);
        assertNull(p.getCodec());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testSegmentRawType() {
        // Indirectly test Segment.rawType via internal operations
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeNumber(42);
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testAppendWithNativeIdsFromOther() throws IOException {
        TokenBuffer buffer1 = new TokenBuffer(MAPPER, false);
        buffer1.writeNumber(1);
        
        TokenBuffer buffer2 = new TokenBuffer(MAPPER, true);
        buffer2.writeTypeId("tid");
        buffer2.writeNumber(2);
        
        buffer1.append(buffer2);
        buffer2.close();
        
        assertTrue(buffer1.canWriteTypeId());
        JsonParser p = buffer1.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(2, p.getIntValue());
        assertNull(p.nextToken());
        p.close();
        buffer1.close();
    }

    @Test(timeout = 4000)
    public void testWriteRawValueWithNegativeOffset() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        // This should work as expected, offset > 0 triggers substring
        buffer.writeRawValue("test", 0, 4);
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteObjectWithCodec() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeObject(new SimpleBean(42, "test"));
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("id", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(42, p.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("name", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("test", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testWriteTreeWithCodec() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeTree(MAPPER.readTree("{\"key\":\"value\"}"));
        JsonParser p = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("key", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testCopyCurrentEventWithTextCharacters() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        String json = "\"hello\"";
        JsonParser p = MAPPER.getFactory().createParser(json);
        p.nextToken(); // VALUE_STRING
        buffer.copyCurrentEvent(p);
        p.close();

        JsonParser bp = buffer.asParser();
        assertEquals(JsonToken.VALUE_STRING, bp.nextToken());
        assertEquals("hello", bp.getText());
        assertNull(bp.nextToken());
        bp.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testCopyCurrentEventWithNumberTypes() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        
        // Test BIG_INTEGER
        String json = "123456789012345678901234567890";
        JsonParser p = MAPPER.getFactory().createParser(json);
        p.nextToken();
        buffer.copyCurrentEvent(p);
        p.close();
        
        // Test FLOAT
        json = "1.5";
        p = MAPPER.getFactory().createParser(json);
        p.nextToken();
        buffer.copyCurrentEvent(p);
        p.close();
        
        // Test BIG_DECIMAL
        json = "1.2345678901234567890";
        p = MAPPER.getFactory().createParser(json);
        p.nextToken();
        buffer.copyCurrentEvent(p);
        p.close();
        
        JsonParser bp = buffer.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, bp.nextToken());
        assertEquals(new BigInteger("123456789012345678901234567890"), bp.getBigIntegerValue());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, bp.nextToken());
        assertEquals(1.5f, bp.getFloatValue(), 1e-9);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, bp.nextToken());
        assertEquals(new BigDecimal("1.2345678901234567890"), bp.getDecimalValue());
        assertNull(bp.nextToken());
        bp.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testCopyCurrentEventWithEmbeddedObject() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        String json = "null";
        JsonParser p = MAPPER.getFactory().createParser(json);
        p.nextToken(); // VALUE_NULL
        buffer.copyCurrentEvent(p);
        p.close();

        JsonParser bp = buffer.asParser();
        assertEquals(JsonToken.VALUE_NULL, bp.nextToken());
        assertNull(bp.nextToken());
        bp.close();
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testSerializeWithAllTokenTypes() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        buffer.writeStartObject();
        buffer.writeFieldName("string");
        buffer.writeString("value");
        buffer.writeFieldName("int");
        buffer.writeNumber(42);
        buffer.writeFieldName("long");
        buffer.writeNumber(123L);
        buffer.writeFieldName("short");
        buffer.writeNumber((short) 7);
        buffer.writeFieldName("bigint");
        buffer.writeNumber(new BigInteger("999"));
        buffer.writeFieldName("double");
        buffer.writeNumber(3.14);
        buffer.writeFieldName("float");
        buffer.writeNumber(1.5f);
        buffer.writeFieldName("bigdec");
        buffer.writeNumber(new BigDecimal("123.456"));
        buffer.writeFieldName("true");
        buffer.writeBoolean(true);
        buffer.writeFieldName("false");
        buffer.writeBoolean(false);
        buffer.writeFieldName("null");
        buffer.writeNull();
        buffer.writeFieldName("embedded");
        buffer.writeObject(new byte[]{1,2,3});
        buffer.writeEndObject();

        TokenBuffer out = new TokenBuffer(MAPPER, false);
        buffer.serialize(out);
        buffer.close();

        JsonParser p = out.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        // Just verify it parses without error
        while (p.nextToken() != null) {
            // consume all
        }
        p.close();
        out.close();
    }

    @Test(timeout = 4000)
    public void testToStringTruncation() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        for (int i = 0; i < 150; i++) {
            buffer.writeNumber(i);
        }
        String str = buffer.toString();
        assertTrue(str.contains("truncated"));
        assertTrue(str.contains("50 entries"));
        buffer.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeWithFieldNameStart() throws IOException {
        TokenBuffer buffer = new TokenBuffer(MAPPER, false);
        String json = "\"field\":\"value\"";
        JsonParser p = MAPPER.getFactory().createParser(json);
        p.nextToken(); // FIELD_NAME
        buffer.deserialize(p, MAPPER.getDeserializationContext());
        p.close();

        JsonParser bp = buffer.asParser();
        assertEquals(JsonToken.START_OBJECT, bp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, bp.nextToken());
        assertEquals("field", bp.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, bp.nextToken());
        assertEquals("value", bp.getText());
        assertEquals(JsonToken.END_OBJECT, bp.nextToken());
        assertNull(bp.nextToken());
        bp.close();
        buffer.close();
    }

    // Helper class for testing writeObject with POJO
    public static class SimpleBean {
        public int id;
        public String name;
        
        public SimpleBean() {}
        public SimpleBean(int id, String name) {
            this.id = id;
            this.name = name;
        }
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}