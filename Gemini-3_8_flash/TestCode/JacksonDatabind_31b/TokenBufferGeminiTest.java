package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.ByteArrayBuilder;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingJsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Targeted Defects4J Defect:
 * - TestTokenBuffer::testOutputContext:
 *   JsonWriteContext vs JsonParser context state misalignment when fields and child contexts are written.
 *   Targeted in Partition C (testOutputContextDefectTrackingFieldNames).
 *
 * Branch & Coverage Matrix:
 * 1. Segment Spanning & Linked List Growth:
 *    - Segment capacity is 16 tokens. Exceeding 16 tokens triggers next Segment allocation in Segment.append().
 *    - Tested across > 16, > 32 tokens, traversing multiple segments via Parser and serialize().
 * 2. Token Types & Value Payloads:
 *    - Structural: START_OBJECT, END_OBJECT, START_ARRAY, END_ARRAY (including unbalanced end guards).
 *    - Scalar Strings: String, SerializableString, null strings.
 *    - Numbers: short, int, long, BigInteger, float, double, BigDecimal, numeric strings.
 *    - Booleans & Nulls: true, false, null.
 *    - Embedded Objects: byte[], RawValue, generic POJOs (with and without ObjectCodec).
 * 3. Native Type and Object IDs:
 *    - canWriteTypeId, canWriteObjectId, canReadTypeId, canReadObjectId.
 *    - Assigning and retrieving native Object/Type IDs across segments.
 *    - toString() with native IDs.
 * 4. Parser Operations & Conversions:
 *    - nextToken(), nextFieldName(), peekNextToken(), overrideCurrentName().
 *    - Number accessors: getIntValue(), getLongValue(), getDoubleValue(), getFloatValue(),
 *      getDecimalValue(), getBigIntegerValue(), getNumberType().
 *    - Binary access: getBinaryValue(), readBinaryValue().
 *    - Context navigation: getCurrentName() for START_OBJECT/START_ARRAY parent naming.
 * 5. Feature Flags & Configuration:
 *    - enable(), disable(), isEnabled(), setFeatureMask().
 *    - forceUseOfBigDecimal(boolean).
 * 6. Defensive Paths & Unsupported Operations:
 *    - writeRaw, writeRawUTF8String, writeUTF8String, writeBinary(InputStream).
 */
public class TokenBufferGeminiTest {

    /*
     * =========================================================================
     * Partition A: Core Functional Logic & State Transitions
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testBasicWriteAndParseStructure() throws IOException {
        TokenBuffer tb = new TokenBuffer((ObjectCodec) null);
        assertFalse(tb.isClosed());
        tb.writeStartObject();
        tb.writeFieldName("num");
        tb.writeNumber(100);
        tb.writeFieldName("str");
        tb.writeString("hello");
        tb.writeFieldName("bool");
        tb.writeBoolean(true);
        tb.writeFieldName("nullVal");
        tb.writeNull();
        tb.writeEndObject();

        assertEquals(JsonToken.START_OBJECT, tb.firstToken());

        JsonParser p = tb.asParser();
        assertNull(p.getCurrentToken());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("num", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(100, p.getIntValue());
        assertEquals(JsonParser.NumberType.INT, p.getNumberType());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("str", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("bool", p.getCurrentName());
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("nullVal", p.getCurrentName());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        assertTrue(p.isClosed());
    }

    @Test(timeout = 4000)
    public void testSegmentBoundaryCrossing() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        // Exceed TOKENS_PER_SEGMENT (16 tokens) to cross into multiple segments
        tb.writeStartArray();
        for (int i = 0; i < 40; i++) {
            tb.writeNumber(i);
        }
        tb.writeEndArray();

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        for (int i = 0; i < 40; i++) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(i, p.getIntValue());
        }
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNumericTypesAndConversions() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeStartArray();
        tb.writeNumber((short) 12);
        tb.writeNumber(34L);
        tb.writeNumber(56.78d);
        tb.writeNumber(90.12f);
        tb.writeNumber(new BigInteger("12345678901234567890"));
        tb.writeNumber(new BigDecimal("9876.54321"));
        tb.writeNumber("42");
        tb.writeNumber("42.5");
        tb.writeEndArray();

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonParser.NumberType.INT, p.getNumberType());
        assertEquals(12, p.getIntValue());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonParser.NumberType.LONG, p.getNumberType());
        assertEquals(34L, p.getLongValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonParser.NumberType.DOUBLE, p.getNumberType());
        assertEquals(56.78d, p.getDoubleValue(), 0.0001);

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonParser.NumberType.FLOAT, p.getNumberType());
        assertEquals(90.12f, p.getFloatValue(), 0.0001f);

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, p.getNumberType());
        assertEquals(new BigInteger("12345678901234567890"), p.getBigIntegerValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, p.getNumberType());
        assertEquals(new BigDecimal("9876.54321"), p.getDecimalValue());

        // Numeric string integer fallback
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(42, p.getIntValue());

        // Numeric string float fallback
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(42.5d, p.getDoubleValue(), 0.0001);

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testPeekNextTokenAndNextFieldName() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeStartObject();
        tb.writeFieldName("propA");
        tb.writeString("valA");
        tb.writeFieldName("propB");
        tb.writeString("valB");
        tb.writeEndObject();

        TokenBuffer.Parser p = (TokenBuffer.Parser) tb.asParser();
        assertEquals(JsonToken.START_OBJECT, p.peekNextToken());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());

        assertEquals("propA", p.nextFieldName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("valA", p.getText());

        assertEquals("propB", p.nextFieldName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("valB", p.getText());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextFieldName());
        assertNull(p.peekNextToken());
        p.close();
    }

    /*
     * =========================================================================
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testEmptyBufferState() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        assertNull(tb.firstToken());
        JsonParser p = tb.asParser();
        assertNull(p.nextToken());
        assertNull(p.getText());
        assertNull(p.getTextCharacters());
        assertEquals(0, p.getTextLength());
        assertEquals(0, p.getTextOffset());
        assertFalse(p.hasTextCharacters());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNullWritingVariants() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeStartArray();
        tb.writeString((String) null);
        tb.writeString((SerializableString) null);
        tb.writeNumber((BigDecimal) null);
        tb.writeNumber((BigInteger) null);
        tb.writeObject(null);
        tb.writeTree(null);
        tb.writeEndArray();

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        for (int i = 0; i < 6; i++) {
            assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        }
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testUnbalancedEndStructures() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        // Extra closing should not throw and should keep root context
        tb.writeEndArray();
        tb.writeEndObject();
        assertNotNull(tb.getOutputContext());
        assertTrue(tb.getOutputContext().inRoot());

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testBinaryAndRawDataHandling() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        assertTrue(tb.canWriteBinaryNatively());
        byte[] rawBytes = new byte[]{1, 2, 3, 4, 5, 6, 7};
        tb.writeBinary(Base64Variants.MIME, rawBytes, 1, 4);

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        byte[] readBack = p.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(new byte[]{2, 3, 4, 5}, readBack);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int bytesWritten = p.readBinaryValue(Base64Variants.MIME, out);
        assertEquals(4, bytesWritten);
        assertArrayEquals(new byte[]{2, 3, 4, 5}, out.toByteArray());
        p.close();
    }

    @Test(timeout = 4000)
    public void testBinaryFromStringBase64() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeString("AQIDBA=="); // Base64 of {1, 2, 3, 4}

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] decoded = p.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(new byte[]{1, 2, 3, 4}, decoded);
        p.close();
    }

    /*
     * =========================================================================
     * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
     * =========================================================================
     */

    /**
     * Target Defect: testOutputContext
     * When writing fields and opening child contexts, the parser and output context
     * must accurately reflect the field name and nesting level.
     */
    @Test(timeout = 4000)
    public void testOutputContextDefectTrackingFieldNames() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeStartObject();
        tb.writeFieldName("a");
        tb.writeStartObject();
        tb.writeFieldName("b");
        tb.writeNumber(123);
        tb.writeEndObject();
        tb.writeEndObject();

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertNull(p.getCurrentName());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("a", p.getCurrentName());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("a", p.getCurrentName());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("b", p.getCurrentName());

        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals("b", p.getCurrentName());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testOverrideCurrentName() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeStartObject();
        tb.writeFieldName("original");
        tb.writeString("val");
        tb.writeEndObject();

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("original", p.getCurrentName());
        p.overrideCurrentName("altered");
        assertEquals("altered", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testAppendAndSerializeAcrossBuffers() throws IOException {
        TokenBuffer src = new TokenBuffer(null);
        src.writeStartObject();
        src.writeFieldName(new SerializedString("field1"));
        src.writeString(new SerializedString("val1"));
        src.writeEndObject();

        TokenBuffer dest = new TokenBuffer(null);
        dest.append(src);

        TokenBuffer serializedTarget = new TokenBuffer(null);
        dest.serialize(serializedTarget);

        JsonParser p = serializedTarget.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("field1", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("val1", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNativeTypeAndObjectIds() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, true);
        assertTrue(tb.canWriteTypeId());
        assertTrue(tb.canWriteObjectId());

        tb.writeTypeId("myTypeId");
        tb.writeObjectId("myObjectId");
        tb.writeStartObject();
        tb.writeFieldName("data");
        tb.writeBoolean(false);
        tb.writeEndObject();

        TokenBuffer.Parser p = (TokenBuffer.Parser) tb.asParser();
        assertTrue(p.canReadTypeId());
        assertTrue(p.canReadObjectId());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("myTypeId", p.getTypeId());
        assertEquals("myObjectId", p.getObjectId());

        String strRepresentation = tb.toString();
        assertTrue(strRepresentation.contains("myTypeId"));
        assertTrue(strRepresentation.contains("myObjectId"));
        p.close();
    }

    /*
     * =========================================================================
     * Partition D: Exception & Defensive Guard Paths
     * =========================================================================
     */

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawUnsupported1() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeRaw("test");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawUnsupported2() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeRaw("test", 0, 4);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawUnsupported3() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeRaw(new SerializedString("test"));
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawUnsupported4() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeRaw(new char[]{'a', 'b'}, 0, 2);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawUnsupported5() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeRaw('c');
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteUTF8StringUnsupported() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeUTF8String(new byte[]{1, 2}, 0, 2);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawUTF8StringUnsupported() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeRawUTF8String(new byte[]{1, 2}, 0, 2);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteBinaryWithInputStreamUnsupported() {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeBinary(Base64Variants.MIME, null, 10);
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testGetNumberValueThrowsWhenNonNumeric() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeString("non-numeric");
        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        p.getIntValue();
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testGetBinaryValueThrowsOnInvalidToken() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeNumber(12345);
        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        p.getBinaryValue(Base64Variants.MIME);
    }

    /*
     * =========================================================================
     * Partition E: Object Lifecycle, Codec & Feature Integrity
     * =========================================================================
     */

    @Test(timeout = 4000)
    public void testFeatureMaskAndToggling() {
        TokenBuffer tb = new TokenBuffer(null);
        assertTrue(tb.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

        tb.disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        assertFalse(tb.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

        tb.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        assertTrue(tb.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));

        int mask = tb.getFeatureMask();
        tb.setFeatureMask(0);
        assertEquals(0, tb.getFeatureMask());
        assertFalse(tb.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET));
        tb.setFeatureMask(mask);
        assertEquals(mask, tb.getFeatureMask());

        assertSame(tb, tb.useDefaultPrettyPrinter());
    }

    @Test(timeout = 4000)
    public void testCodecIntegrationAndEmbeddedObjects() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer tb = new TokenBuffer(mapper);
        assertSame(mapper, tb.getCodec());

        java.util.Map<String, String> map = new java.util.HashMap<String, String>();
        map.put("k", "v");
        tb.writeObject(map);

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("k", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("v", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();

        // Test without codec (fallback to embedded object)
        TokenBuffer noCodecTb = new TokenBuffer((ObjectCodec) null);
        noCodecTb.writeObject(map);
        JsonParser p2 = noCodecTb.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p2.nextToken());
        assertSame(map, p2.getEmbeddedObject());
        p2.close();
    }

    @Test(timeout = 4000)
    public void testForceBigDecimalAndVersion() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        assertNotNull(tb.version());

        tb.forceUseOfBigDecimal(true);
        tb.writeNumber(1.2345);

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonParser.NumberType.DOUBLE, p.getNumberType());
        p.close();
    }

    @Test(timeout = 4000)
    public void testRawValueSerialization() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.writeRawValue("{\"raw\":true}");
        tb.writeRawValue("rawText", 0, 7);
        tb.writeRawValue(new char[]{'r', 'a', 'w'}, 0, 3);

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testCloseAndFlushIntegrity() throws IOException {
        TokenBuffer tb = new TokenBuffer(null);
        tb.flush();
        assertFalse(tb.isClosed());
        tb.close();
        assertTrue(tb.isClosed());

        JsonParser p = tb.asParser();
        assertFalse(p.isClosed());
        p.close();
        assertTrue(p.isClosed());
        assertNull(p.nextToken());
    }
}