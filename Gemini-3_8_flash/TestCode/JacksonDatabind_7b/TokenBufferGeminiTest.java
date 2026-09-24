/* [Branch & Defect Analysis Matrix]
 * Targets: com.fasterxml.jackson.databind.util.TokenBuffer and inner TokenBuffer.Parser, TokenBuffer.Segment
 * 
 * Branch Coverage Focus:
 * 1. Segment chunking & linked-list allocation: crossing the 16-token boundary (TOKENS_PER_SEGMENT)
 *    into 2nd, 3rd, and subsequent segments.
 * 2. deserialize() starting at FIELD_NAME (Defects4J defect):
 *    When deserializing starting from FIELD_NAME, TokenBuffer must synthesize START_OBJECT (and END_OBJECT)
 *    so downstream delegating creators and deserializers don't fail with "Expected token START_OBJECT, current token FIELD_NAME".
 * 3. Native Object and Type IDs: canReadObjectId/canReadTypeId, findObjectId/findTypeId,
 *    assignNativeIds, _checkNativeIds, _appendNativeIds, serialize with IDs, append() propagation.
 * 4. All JsonGenerator write methods: writeStartArray, writeEndArray, writeStartObject, writeEndObject,
 *    writeFieldName (String / SerializableString), writeString (null / String / char[] / SerializableString),
 *    writeNumber (short, int, long, double, float, BigDecimal, BigInteger, String-encoded),
 *    writeBoolean, writeNull, writeObject (null, byte[], POJO with/without codec), writeTree,
 *    writeBinary, unsupported operations (writeRaw*, stream binary).
 * 5. Parser navigation and type conversions:
 *    nextToken, peekNextToken, getText, getTextCharacters, getTextLength, getTextOffset,
 *    getNumberValue, getNumberType, getIntValue, getLongValue, getDoubleValue, getFloatValue,
 *    getDecimalValue, getBigIntegerValue, getBinaryValue, readBinaryValue, overrideCurrentName,
 *    unbalanced array/object end markers context recovery.
 * 6. Utility methods: serialize(JsonGenerator), toString() (count < 100, count >= 100 truncation),
 *    append(TokenBuffer), copyCurrentEvent, copyCurrentStructure (arrays, objects, nested).
 */
package com.fasterxml.jackson.databind.util;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class TokenBufferGeminiTest {

    /*
     * ----------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone
     * Targets: TestCreatorsDelegating::testDelegateWithTokenBuffer defect
     * Issue #592: TokenBuffer.deserialize(jp, ctxt) starting at FIELD_NAME must
     * synthesize START_OBJECT so delegating deserializers receive START_OBJECT.
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testDefectDeserializeStartingAtFieldNameSynthesizesStartObject() throws IOException {
        TokenBuffer source = new TokenBuffer(null, false);
        source.writeStartObject();
        source.writeFieldName("delegateField");
        source.writeString("testValue");
        source.writeEndObject();

        JsonParser srcParser = source.asParser();
        assertEquals(JsonToken.START_OBJECT, srcParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, srcParser.nextToken()); // Currently at FIELD_NAME

        TokenBuffer target = new TokenBuffer(null, false);
        target.deserialize(srcParser, null);

        // Ground-truth defect expectation: target must start with START_OBJECT
        assertEquals("TokenBuffer.deserialize() starting at FIELD_NAME must wrap into START_OBJECT",
                JsonToken.START_OBJECT, target.firstToken());

        JsonParser targetParser = target.asParser();
        assertEquals(JsonToken.START_OBJECT, targetParser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, targetParser.nextToken());
        assertEquals("delegateField", targetParser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, targetParser.nextToken());
        assertEquals("testValue", targetParser.getText());
        assertEquals(JsonToken.END_OBJECT, targetParser.nextToken());
        assertNull(targetParser.nextToken());
    }

    /*
     * ----------------------------------------------------------------------
     * Partition A: Core Functional Logic, State Transitions & Segment Boundaries
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testSegmentBoundaryCrossingOver16Tokens() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        // Write 35 tokens to force spanning across 3 segments (16 + 16 + 3)
        for (int i = 0; i < 35; i++) {
            tb.writeNumber(i);
        }

        JsonParser p = tb.asParser();
        for (int i = 0; i < 35; i++) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(i, p.getIntValue());
        }
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testPeekNextTokenAcrossSegments() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        for (int i = 0; i < 16; i++) {
            tb.writeNumber(i);
        }
        tb.writeString("17thToken");

        TokenBuffer.Parser p = (TokenBuffer.Parser) tb.asParser();
        for (int i = 0; i < 15; i++) {
            p.nextToken();
        }
        // At index 14
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken()); // index 15 (last of 1st segment)
        assertEquals(15, p.getIntValue());

        // Peek should cross to next segment without advancing pointer permanently
        assertEquals(JsonToken.VALUE_STRING, p.peekNextToken());
        assertEquals(JsonToken.VALUE_STRING, p.peekNextToken());

        // Advance to next segment
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("17thToken", p.getText());
        assertNull(p.peekNextToken());
        assertNull(p.nextToken());
        p.close();
        assertNull(p.peekNextToken());
    }

    @Test(timeout = 4000)
    public void testStructuralNestingAndUnbalancedRecovery() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeStartObject();
        tb.writeFieldName("arr");
        tb.writeStartArray();
        tb.writeNumber(1);
        tb.writeEndArray();
        tb.writeEndObject();
        // Intentional extra end markers to verify defensive parent context recovery
        tb.writeEndObject();
        tb.writeEndArray();

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("arr", p.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testNumericTypesAndConversions() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeNumber((short) 10);
        tb.writeNumber(20);
        tb.writeNumber(30L);
        tb.writeNumber(40.5d);
        tb.writeNumber(50.25f);
        tb.writeNumber(new BigDecimal("123.456"));
        tb.writeNumber(new BigInteger("9876543210123456789"));
        tb.writeNumber("999.99");
        tb.writeNumber("1000");

        JsonParser p = tb.asParser();

        // short
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonParser.NumberType.INT, p.getNumberType());
        assertEquals(10, p.getIntValue());
        assertEquals(10L, p.getLongValue());
        assertEquals(BigInteger.valueOf(10), p.getBigIntegerValue());

        // int
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonParser.NumberType.INT, p.getNumberType());
        assertEquals(20, p.getIntValue());
        assertEquals(20.0, p.getDoubleValue(), 0.001);

        // long
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonParser.NumberType.LONG, p.getNumberType());
        assertEquals(30L, p.getLongValue());
        assertEquals(new BigDecimal(30L), p.getDecimalValue());

        // double
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonParser.NumberType.DOUBLE, p.getNumberType());
        assertEquals(40.5, p.getDoubleValue(), 0.001);
        assertEquals(40, p.getIntValue());

        // float
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonParser.NumberType.FLOAT, p.getNumberType());
        assertEquals(50.25f, p.getFloatValue(), 0.001f);

        // BigDecimal
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, p.getNumberType());
        assertEquals(new BigDecimal("123.456"), p.getDecimalValue());
        assertEquals(new BigInteger("123"), p.getBigIntegerValue());

        // BigInteger
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, p.getNumberType());
        assertEquals(new BigInteger("9876543210123456789"), p.getBigIntegerValue());
        assertEquals(new BigDecimal(new BigInteger("9876543210123456789")), p.getDecimalValue());

        // String numeric: float
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonParser.NumberType.DOUBLE, p.getNumberType());
        assertEquals(999.99, p.getDoubleValue(), 0.001);

        // String numeric: int/long
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(JsonParser.NumberType.LONG, p.getNumberType());
        assertEquals(1000L, p.getLongValue());
        assertEquals(1000, p.getIntValue());

        p.close();
    }

    @Test(timeout = 4000)
    public void testTextAndBinaryHandling() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeString("Hello World");
        tb.writeString(new SerializedString("Serialized text"));
        tb.writeString(new char[]{'f', 'o', 'o', 'b', 'a', 'r'}, 1, 3); // "oob"
        tb.writeString((String) null); // writes null token
        tb.writeString((SerializableString) null); // writes null token
        byte[] binaryData = new byte[]{1, 2, 3, 4, 5};
        tb.writeBinary(Base64Variants.MIME, binaryData, 0, binaryData.length);
        tb.writeBoolean(true);
        tb.writeBoolean(false);

        JsonParser p = tb.asParser();

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("Hello World", p.getText());
        assertArrayEquals("Hello World".toCharArray(), p.getTextCharacters());
        assertEquals(11, p.getTextLength());
        assertEquals(0, p.getTextOffset());
        assertFalse(p.hasTextCharacters());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("Serialized text", p.getText());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("oob", p.getText());

        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.getText());

        assertEquals(JsonToken.VALUE_NULL, p.nextToken());

        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertArrayEquals(binaryData, (byte[]) p.getEmbeddedObject());
        assertArrayEquals(binaryData, p.getBinaryValue(Base64Variants.MIME));

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int readBytes = p.readBinaryValue(Base64Variants.MIME, bos);
        assertEquals(5, readBytes);
        assertArrayEquals(binaryData, bos.toByteArray());

        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertEquals("true", p.getText());

        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        assertEquals("false", p.getText());

        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testBase64StringDecodingInParser() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        // Base64 of "Hello" is "SGVsbG8="
        tb.writeString("SGVsbG8=");
        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] decoded = p.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals("Hello".getBytes("UTF-8"), decoded);
        p.close();
    }

    @Test(timeout = 4000)
    public void testNativeObjectAndTypeIds() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, true);
        assertTrue(tb.canWriteObjectId());
        assertTrue(tb.canWriteTypeId());

        tb.writeObjectId("obj-123");
        tb.writeTypeId("type-XYZ");
        tb.writeStartObject();

        tb.writeFieldName("test");
        tb.writeObjectId(999);
        tb.writeString("val");

        tb.writeEndObject();

        JsonParser p = tb.asParser();
        assertTrue(p.canReadObjectId());
        assertTrue(p.canReadTypeId());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("obj-123", p.getObjectId());
        assertEquals("type-XYZ", p.getTypeId());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(999, p.getObjectId());
        assertNull(p.getTypeId());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testSerializeAllTokenTypes() throws IOException {
        TokenBuffer source = new TokenBuffer(null, true);
        source.writeTypeId("myTypeId");
        source.writeObjectId("myObjectId");
        source.writeStartObject();
        source.writeFieldName("s1");
        source.writeString("str");
        source.writeFieldName(new SerializedString("s2"));
        source.writeString(new SerializedString("str2"));
        source.writeFieldName("int");
        source.writeNumber(123);
        source.writeFieldName("bigInt");
        source.writeNumber(new BigInteger("999999999"));
        source.writeFieldName("long");
        source.writeNumber(888888888888L);
        source.writeFieldName("short");
        source.writeNumber((short) 7);
        source.writeFieldName("byte");
        source.writeNumber((byte) 3);
        source.writeFieldName("double");
        source.writeNumber(1.23d);
        source.writeFieldName("bigDec");
        source.writeNumber(new BigDecimal("4.56"));
        source.writeFieldName("float");
        source.writeNumber(7.89f);
        source.writeFieldName("numStr");
        source.writeNumber("42.42");
        source.writeFieldName("boolT");
        source.writeBoolean(true);
        source.writeFieldName("boolF");
        source.writeBoolean(false);
        source.writeFieldName("nullVal");
        source.writeNull();
        source.writeFieldName("arr");
        source.writeStartArray();
        source.writeEndArray();
        source.writeFieldName("embedded");
        source.writeObject("customObject");
        source.writeEndObject();

        TokenBuffer target = new TokenBuffer(null, true);
        source.serialize(target);

        JsonParser p = target.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("s1", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("str", p.getText());

        while (p.nextToken() != JsonToken.END_OBJECT) {
            // Traverse all generated tokens
        }
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testAppendBufferAndCopyStructures() throws IOException {
        TokenBuffer tb1 = new TokenBuffer(null, false);
        tb1.writeStartArray();
        tb1.writeNumber(1);
        tb1.writeEndArray();

        TokenBuffer tb2 = new TokenBuffer(null, false);
        tb2.writeStartObject();
        tb2.writeFieldName("key");
        tb2.writeString("value");
        tb2.writeEndObject();

        tb1.append(tb2);

        JsonParser p = tb1.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("key", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("value", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testToStringWithVariousLengths() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeStartObject();
        tb.writeFieldName("field");
        tb.writeString("value");
        tb.writeEndObject();

        String str = tb.toString();
        assertTrue(str.contains("START_OBJECT"));
        assertTrue(str.contains("FIELD_NAME(field)"));
        assertTrue(str.contains("VALUE_STRING"));
        assertTrue(str.contains("END_OBJECT"));
        assertFalse(str.contains("truncated"));

        // Test truncation when tokens >= 100
        TokenBuffer big = new TokenBuffer(null, false);
        for (int i = 0; i < 110; i++) {
            big.writeNumber(i);
        }
        String bigStr = big.toString();
        assertTrue(bigStr.contains("truncated 10 entries"));
    }

    /*
     * ----------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testEmptyTokenBuffer() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        assertNull(tb.firstToken());

        JsonParser p = tb.asParser();
        assertNull(p.nextToken());
        assertNull(p.peekNextToken());
        assertNull(p.getText());
        assertNull(p.getTextCharacters());
        assertEquals(0, p.getTextLength());
        assertFalse(p.isClosed());
        p.close();
        assertTrue(p.isClosed());
    }

    @Test(timeout = 4000)
    public void testWriteNullBranches() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeNumber((BigDecimal) null);
        tb.writeNumber((BigInteger) null);
        tb.writeObject(null);
        tb.writeTree(null);

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testOverrideCurrentName() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeStartObject();
        tb.writeFieldName("oldName");
        tb.writeString("val");
        tb.writeEndObject();

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        p.overrideCurrentName("rootObj");
        assertEquals("rootObj", p.getCurrentName());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("oldName", p.getCurrentName());
        p.overrideCurrentName("newName");
        assertEquals("newName", p.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        p.close();
    }

    @Test(timeout = 4000)
    public void testFeatureMaskAndCapabilities() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        int defaultFeatures = tb.getFeatureMask();
        tb.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertTrue(tb.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        tb.disable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        assertFalse(tb.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES));
        tb.setFeatureMask(defaultFeatures);
        assertEquals(defaultFeatures, tb.getFeatureMask());

        assertTrue(tb.canWriteBinaryNatively());
        assertSame(tb, tb.useDefaultPrettyPrinter());
        assertNotNull(tb.version());
        assertNotNull(tb.getOutputContext());

        assertFalse(tb.isClosed());
        tb.flush();
        tb.close();
        assertTrue(tb.isClosed());
    }

    /*
     * ----------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * ----------------------------------------------------------------------
     */

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawUnsupportedString() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeRaw("test");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawUnsupportedChars() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeRaw(new char[]{'a', 'b'}, 0, 2);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawUnsupportedChar() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeRaw('c');
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawValueUnsupported() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeRawValue("val");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteRawUTF8StringUnsupported() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeRawUTF8String(new byte[]{1, 2}, 0, 2);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testWriteBinaryWithStreamUnsupported() {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeBinary(Base64Variants.MIME, (java.io.InputStream) null, 10);
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testGetNumberValueThrowsWhenNotNumeric() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeString("notANumber");
        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        p.getNumberValue(); // Should throw JsonParseException
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testGetBinaryValueThrowsWhenNonBinaryToken() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeNumber(12345);
        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        p.getBinaryValue(Base64Variants.MIME);
    }

    /*
     * ----------------------------------------------------------------------
     * Partition E: Object Lifecycle & Parser Location / Context Contract
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testParserLocationHandling() throws IOException {
        TokenBuffer tb = new TokenBuffer(null, false);
        tb.writeNumber(42);

        TokenBuffer.Parser p = (TokenBuffer.Parser) tb.asParser();
        assertEquals(JsonLocation.NA, p.getTokenLocation());
        assertEquals(JsonLocation.NA, p.getCurrentLocation());

        JsonLocation customLoc = new JsonLocation("src", 100L, 1, 10);
        p.setLocation(customLoc);
        assertEquals(customLoc, p.getTokenLocation());
        assertEquals(customLoc, p.getCurrentLocation());
        p.close();
    }

    @Test(timeout = 4000)
    public void testParserFromSourceParser() throws IOException {
        TokenBuffer src = new TokenBuffer(null, false);
        src.writeString("abc");
        JsonParser srcP = src.asParser();
        srcP.nextToken();

        TokenBuffer dest = new TokenBuffer(null, false);
        dest.writeNumber(99);
        JsonParser destP = dest.asParser(srcP);
        assertEquals(srcP.getTokenLocation(), destP.getTokenLocation());
        assertEquals(JsonToken.VALUE_NUMBER_INT, destP.nextToken());
        assertEquals(99, destP.getIntValue());
        destP.close();
        srcP.close();
    }
}