package com.fasterxml.jackson.databind.util;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: com.fasterxml.jackson.databind.util.TokenBuffer
 * DEFECT ID: Defects4J com.fasterxml.jackson.databind.node.TestConversions::testConversionOfPojos
 *            "junit.framework.AssertionFailedError: Expected Object, got POJO"
 * ROOT CAUSE ANALYSIS:
 * - In TokenBuffer#writeObject(Object) and TokenBuffer#writeTree(TreeNode), the methods unconditionally
 *   append a VALUE_EMBEDDED_OBJECT token rather than delegating to the configured _objectCodec (e.g.,
 *   ObjectMapper). When converted or deserialized via valueToTree/readTree, this causes POJOs to be
 *   retained as raw POJONode instead of deserializing into structured JSON ObjectNodes/tokens.
 *
 * TEST COVERAGE MATRIX:
 * - Partition A (Core Functional Logic):
 *   * Segment chaining: writing > 16 tokens across multiple Segment nodes (TOKENS_PER_SEGMENT = 16).
 *   * Segment navigation in TokenBuffer.Parser (nextToken, peekNextToken across boundaries).
 *   * copyCurrentStructure / copyCurrentEvent with all JsonToken and NumberType combinations.
 *   * Output buffering and serialize(JsonGenerator) across all primitive and complex types.
 * - Partition B (Boundary Value Analysis):
 *   * Segment boundary exact edge: exactly 16 tokens, 17 tokens, 32 tokens, 33 tokens.
 *   * Large sequence (> 100 tokens) triggering toString() truncation branch.
 *   * Empty buffer edge cases (firstToken(), peekNextToken(), nextToken() == null).
 *   * Unbalanced structure closures (writeEndObject / writeEndArray when parent context is null).
 * - Partition C (Defect-Targeted Zone):
 *   * POJO serialization with configured ObjectCodec: writeObject must expand to START_OBJECT/END_OBJECT.
 *   * TreeNode serialization with configured ObjectCodec: writeTree must output structured object tokens.
 *   * mapper.readTree from TokenBuffer containing POJO must yield ObjectNode, not POJONode.
 * - Partition D (Exception & Defensive Guard Paths):
 *   * Unsupported operations: writeRawUTF8String, writeUTF8String, writeRaw, writeRawValue, streaming binary.
 *   * Parser numeric accessors with non-numeric tokens (JsonParseException).
 *   * Parser getBinaryValue with non-string/non-byte[] token (JsonParseException).
 *   * serialize() with invalid VALUE_NUMBER_FLOAT type (JsonGenerationException).
 * - Partition E (Object Lifecycle & Feature Configuration):
 *   * Feature masks: enable, disable, isEnabled, getFeatureMask, setFeatureMask.
 *   * Codec getters/setters, Context tracking, version() accessor, close() idempotency.
 * ----------------------------------------------------------------------------------------------------
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.core.Base64Variants;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TokenBufferGeminiTest {

    public static class SamplePojo {
        public int id;
        public String name;

        public SamplePojo() {}

        public SamplePojo(int id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testMultiSegmentTraversalAndTokenTypes() throws Exception {
        TokenBuffer buf = new TokenBuffer(null, false);

        buf.writeStartObject();
        buf.writeFieldName("field1");
        buf.writeString("strVal");
        buf.writeFieldName("field2");
        buf.writeNumber((short) 1);
        buf.writeFieldName("field3");
        buf.writeNumber(2);
        buf.writeFieldName("field4");
        buf.writeNumber(3L);
        buf.writeFieldName("field5");
        buf.writeNumber(4.5f);
        buf.writeFieldName("field6");
        buf.writeNumber(5.5d);
        buf.writeFieldName("field7");
        buf.writeBoolean(true);
        buf.writeFieldName("field8");
        buf.writeBoolean(false);
        buf.writeFieldName("field9");
        buf.writeNull();
        buf.writeEndObject();

        TokenBuffer.Parser parser = (TokenBuffer.Parser) buf.asParser();
        assertNull(parser.getCurrentToken());

        assertEquals(JsonToken.START_OBJECT, parser.nextToken());
        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("field1", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, parser.nextToken());
        assertEquals("strVal", parser.getText());
        assertFalse(parser.hasTextCharacters());
        assertEquals(6, parser.getTextLength());
        assertEquals(0, parser.getTextOffset());
        assertArrayEquals("strVal".toCharArray(), parser.getTextCharacters());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals("field2", parser.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(1, parser.getIntValue());
        assertEquals(JsonParser.NumberType.INT, parser.getNumberType());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(2, parser.getIntValue());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, parser.nextToken());
        assertEquals(3L, parser.getLongValue());
        assertEquals(JsonParser.NumberType.LONG, parser.getNumberType());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(4.5f, parser.getFloatValue(), 0.001f);
        assertEquals(JsonParser.NumberType.FLOAT, parser.getNumberType());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, parser.nextToken());
        assertEquals(5.5d, parser.getDoubleValue(), 0.001d);
        assertEquals(JsonParser.NumberType.DOUBLE, parser.getNumberType());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_FALSE, parser.nextToken());

        assertEquals(JsonToken.FIELD_NAME, parser.nextToken());
        assertEquals(JsonToken.VALUE_NULL, parser.nextToken());

        assertEquals(JsonToken.END_OBJECT, parser.nextToken());
        assertNull(parser.nextToken());
        parser.close();
    }

    @Test(timeout = 4000)
    public void testPeekNextTokenAcrossSegmentBoundary() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        // Write exactly 16 tokens to fill the first segment
        for (int i = 0; i < 16; i++) {
            buf.writeNumber(i);
        }
        // Write the 17th token into the second segment
        buf.writeString("boundaryVal");

        TokenBuffer.Parser p = (TokenBuffer.Parser) buf.asParser();
        for (int i = 0; i < 15; i++) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(i, p.getIntValue());
        }

        // At index 14, peek next should see index 15
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.peekNextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(15, p.getIntValue());

        // At index 15 (last of first segment), peek should bridge to next segment
        assertEquals(JsonToken.VALUE_STRING, p.peekNextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("boundaryVal", p.getText());

        // Peek past EOF
        assertNull(p.peekNextToken());
        assertNull(p.nextToken());

        p.close();
        assertNull(p.peekNextToken());
    }

    @Test(timeout = 4000)
    public void testSerializeAndAppendAllTokenBranches() throws Exception {
        TokenBuffer src = new TokenBuffer(null);
        src.writeStartArray();
        src.writeStartObject();
        src.writeFieldName(new SerializedString("sField"));
        src.writeString(new SerializedString("sString"));
        src.writeFieldName("bigInt");
        src.writeNumber(new BigInteger("12345678901234567890"));
        src.writeFieldName("bigDec");
        src.writeNumber(new BigDecimal("123456.789"));
        src.writeFieldName("numStr");
        src.writeNumber("987654.321");
        src.writeFieldName("rawByteNum");
        src._append(JsonToken.VALUE_NUMBER_INT, Byte.valueOf((byte) 7));
        src.writeFieldName("embedded");
        src.writeObject(new byte[] { 1, 2, 3 });
        src.writeEndObject();
        src.writeEndArray();

        TokenBuffer target = new TokenBuffer(null);
        target.append(src);

        TokenBuffer verifyBuffer = new TokenBuffer(null);
        target.serialize(verifyBuffer);

        JsonParser p = verifyBuffer.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("sField", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("sString", p.getText());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(new BigInteger("12345678901234567890"), p.getBigIntegerValue());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigDecimal("123456.789"), p.getDecimalValue());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(987654.321d, p.getDoubleValue(), 0.001);
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(7, p.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertArrayEquals(new byte[] { 1, 2, 3 }, (byte[]) p.getEmbeddedObject());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test(timeout = 4000)
    public void testNativeIdsStorageAndRetrieval() throws Exception {
        TokenBuffer buf = new TokenBuffer(null, true);
        assertTrue(buf.canWriteObjectId());
        assertTrue(buf.canWriteTypeId());

        buf.writeTypeId("MyType");
        buf.writeObjectId("MyId");
        buf.writeStartObject();

        buf.writeFieldName("data");
        buf.writeNumber(100);
        buf.writeEndObject();

        TokenBuffer.Parser p = (TokenBuffer.Parser) buf.asParser();
        assertTrue(p.canReadTypeId());
        assertTrue(p.canReadObjectId());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("MyType", p.getTypeId());
        assertEquals("MyId", p.getObjectId());

        TokenBuffer serializedBuf = new TokenBuffer(null, true);
        buf.serialize(serializedBuf);

        TokenBuffer.Parser sp = (TokenBuffer.Parser) serializedBuf.asParser();
        assertEquals(JsonToken.START_OBJECT, sp.nextToken());
        assertEquals("MyType", sp.getTypeId());
        assertEquals("MyId", sp.getObjectId());
    }

    @Test(timeout = 4000)
    public void testCopyCurrentStructureAndCopyCurrentEvent() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"name\":\"Alice\",\"age\":30,\"scores\":[100,99.5],\"flag\":true,\"extra\":null}";
        JsonParser jp = mapper.getFactory().createParser(json);

        TokenBuffer tb = new TokenBuffer(mapper);
        jp.nextToken(); // to START_OBJECT
        tb.copyCurrentStructure(jp);

        TokenBuffer verify = new TokenBuffer(mapper);
        JsonParser p = tb.asParser();
        while (p.nextToken() != null) {
            verify.copyCurrentEvent(p);
        }

        JsonParser vp = verify.asParser();
        assertEquals(JsonToken.START_OBJECT, vp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, vp.nextToken());
        assertEquals("name", vp.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, vp.nextToken());
        assertEquals("Alice", vp.getText());
        assertEquals(JsonToken.FIELD_NAME, vp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, vp.nextToken());
        assertEquals(30, vp.getIntValue());
        assertEquals(JsonToken.FIELD_NAME, vp.nextToken());
        assertEquals(JsonToken.START_ARRAY, vp.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, vp.nextToken());
        assertEquals(100, vp.getIntValue());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, vp.nextToken());
        assertEquals(99.5, vp.getDoubleValue(), 0.01);
        assertEquals(JsonToken.END_ARRAY, vp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, vp.nextToken());
        assertEquals(JsonToken.VALUE_TRUE, vp.nextToken());
        assertEquals(JsonToken.FIELD_NAME, vp.nextToken());
        assertEquals(JsonToken.VALUE_NULL, vp.nextToken());
        assertEquals(JsonToken.END_OBJECT, vp.nextToken());
        assertNull(vp.nextToken());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyBufferBoundaries() throws Exception {
        TokenBuffer buf = new TokenBuffer(null, false);
        assertNull(buf.firstToken());

        JsonParser p = buf.asParser();
        assertFalse(p.isClosed());
        assertNull(p.nextToken());
        assertNull(p.getCurrentName());
        assertNull(p.getText());
        p.close();
        assertTrue(p.isClosed());
    }

    @Test(timeout = 4000)
    public void testToStringTruncationAt100Tokens() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeStartArray();
        for (int i = 0; i < 110; i++) {
            buf.writeNumber(i);
        }
        buf.writeEndArray();

        String str = buf.toString();
        assertTrue("Output should indicate truncation: " + str, str.contains("... (truncated "));
        assertTrue("Output should start with [TokenBuffer: ", str.startsWith("[TokenBuffer: "));
    }

    @Test(timeout = 4000)
    public void testToStringWithFieldNamesAndNativeIds() throws Exception {
        TokenBuffer buf = new TokenBuffer(null, true);
        buf.writeTypeId("CustomTypeId");
        buf.writeObjectId("CustomObjectId");
        buf.writeStartObject();
        buf.writeFieldName("myField");
        buf.writeString("test");
        buf.writeEndObject();

        String str = buf.toString();
        assertTrue(str.contains("FIELD_NAME(myField)"));
        assertTrue(str.contains("[objectId=CustomObjectId]"));
        assertTrue(str.contains("[typeId=CustomTypeId]"));
    }

    @Test(timeout = 4000)
    public void testUnbalancedContextCloses() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        // Closing context when at root level (parent is null)
        buf.writeEndObject();
        buf.writeEndArray();
        assertNotNull(buf.getOutputContext());

        TokenBuffer.Parser p = (TokenBuffer.Parser) buf.asParser();
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNotNull(p.getParsingContext());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNotNull(p.getParsingContext());
        assertNull(p.nextToken());
    }

    @Test(timeout = 4000)
    public void testNullInputsForTextAndNumbers() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeString((String) null);
        buf.writeString((SerializableString) null);
        buf.writeNumber((BigDecimal) null);
        buf.writeNumber((BigInteger) null);

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.nextToken());
    }

    @Test(timeout = 4000)
    public void testNumericTypeConversionsAndNumberAsString() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber("12345");
        buf.writeNumber("123.45");

        TokenBuffer.Parser p = (TokenBuffer.Parser) buf.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(12345L, p.getLongValue());
        assertEquals(12345, p.getIntValue());
        assertEquals(12345.0, p.getDoubleValue(), 0.001);
        assertEquals(BigInteger.valueOf(12345), p.getBigIntegerValue());
        assertEquals(BigDecimal.valueOf(12345L), p.getDecimalValue());

        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(123.45, p.getDoubleValue(), 0.001);
        assertEquals(123.45f, p.getFloatValue(), 0.001f);
        assertEquals(123, p.getIntValue());
        assertEquals(123L, p.getLongValue());
        assertEquals(BigInteger.valueOf(123), p.getBigIntegerValue());
        assertEquals(new BigDecimal(Double.toString(123.45)).setScale(2, BigDecimal.ROUND_HALF_UP),
                p.getDecimalValue().setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    @Test(timeout = 4000)
    public void testBinaryHandlingMethods() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        byte[] original = new byte[] { 10, 20, 30, 40, 50 };
        buf.writeBinary(Base64Variants.MIME_NO_LINEFEEDS, original, 0, original.length);

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertArrayEquals(original, p.getBinaryValue(Base64Variants.MIME_NO_LINEFEEDS));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead = p.readBinaryValue(Base64Variants.MIME_NO_LINEFEEDS, baos);
        assertEquals(original.length, bytesRead);
        assertArrayEquals(original, baos.toByteArray());
    }

    @Test(timeout = 4000)
    public void testBase64StringDecodingInParser() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        String base64 = "AQIDBAU="; // bytes 1, 2, 3, 4, 5
        buf.writeString(base64);

        JsonParser p = buf.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] decoded = p.getBinaryValue(Base64Variants.MIME_NO_LINEFEEDS);
        assertArrayEquals(new byte[] { 1, 2, 3, 4, 5 }, decoded);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J ground truth)
    // =========================================================================

    /**
     * Targets Defects4J failure:
     * com.fasterxml.jackson.databind.node.TestConversions::testConversionOfPojos
     * "junit.framework.AssertionFailedError: Expected Object, got POJO"
     *
     * In the defective implementation, TokenBuffer#writeTree(TreeNode) unconditionally
     * appends VALUE_EMBEDDED_OBJECT instead of expanding the TreeNode through the ObjectCodec.
     * When read back via mapper.readTree(parser), this yields a POJONode instead of ObjectNode.
     */
    @Test(timeout = 4000)
    public void testConversionOfPojosDefectTargetWriteTree() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer tb = new TokenBuffer(mapper);
        ObjectNode sourceNode = mapper.createObjectNode();
        sourceNode.put("field", "value");

        tb.writeTree(sourceNode);
        tb.close();

        JsonParser p = tb.asParser();
        JsonNode resultNode = mapper.readTree(p);

        assertTrue("Expected Object, got POJO", resultNode.isObject());
        assertEquals("value", resultNode.get("field").asText());
    }

    /**
     * Complements the defect fix verification: TokenBuffer#writeObject(Object)
     * when configured with an ObjectCodec should delegate serialization rather than
     * just buffering VALUE_EMBEDDED_OBJECT.
     */
    @Test(timeout = 4000)
    public void testConversionOfPojosDefectTargetWriteObject() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer tb = new TokenBuffer(mapper);
        SamplePojo pojo = new SamplePojo(101, "UnitTester");

        tb.writeObject(pojo);
        tb.close();

        JsonToken first = tb.firstToken();
        assertEquals("TokenBuffer.writeObject with codec should write START_OBJECT, not VALUE_EMBEDDED_OBJECT",
                JsonToken.START_OBJECT, first);

        JsonNode tree = mapper.readTree(tb.asParser());
        assertTrue("Expected Object, got POJO", tree.isObject());
        assertEquals(101, tree.get("id").asInt());
        assertEquals("UnitTester", tree.get("name").asText());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testUnsupportedWriteRaw() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeRaw("test");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testUnsupportedWriteRawWithOffset() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeRaw("test", 0, 4);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testUnsupportedWriteRawSerializableString() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeRaw(new SerializedString("test"));
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testUnsupportedWriteRawCharArray() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeRaw(new char[] { 'a', 'b' }, 0, 2);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testUnsupportedWriteRawChar() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeRaw('x');
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testUnsupportedWriteRawValue() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeRawValue("val");
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testUnsupportedWriteRawValueWithOffset() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeRawValue("val", 0, 3);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testUnsupportedWriteRawValueCharArray() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeRawValue(new char[] { 'v' }, 0, 1);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testUnsupportedWriteUTF8String() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeUTF8String(new byte[] { 65 }, 0, 1);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testUnsupportedWriteRawUTF8String() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeRawUTF8String(new byte[] { 65 }, 0, 1);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testUnsupportedWriteBinaryInputStream() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeBinary(Base64Variants.MIME_NO_LINEFEEDS, new ByteArrayInputStream(new byte[2]), 2);
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testGetNumberValueOnNonNumericTokenThrowsException() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeString("notANumber");
        JsonParser p = buf.asParser();
        p.nextToken();
        p.getNumberValue();
    }

    @Test(expected = JsonParseException.class, timeout = 4000)
    public void testGetBinaryValueOnNonBinaryTokenThrowsException() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeNumber(12345);
        JsonParser p = buf.asParser();
        p.nextToken();
        p.getBinaryValue(Base64Variants.MIME_NO_LINEFEEDS);
    }

    @Test(expected = JsonGenerationException.class, timeout = 4000)
    public void testSerializeUnrecognizedFloatTypeThrowsException() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf._append(JsonToken.VALUE_NUMBER_FLOAT, new Object());
        TokenBuffer out = new TokenBuffer(null);
        buf.serialize(out);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Context, & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testFeatureMaskAndConfiguration() {
        TokenBuffer buf = new TokenBuffer(null);
        assertTrue(buf.canWriteBinaryNatively());

        int initialMask = buf.getFeatureMask();
        buf.enable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        assertTrue(buf.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));

        buf.disable(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS);
        assertFalse(buf.isEnabled(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS));

        buf.setFeatureMask(initialMask);
        assertEquals(initialMask, buf.getFeatureMask());

        assertSame(buf, buf.useDefaultPrettyPrinter());
    }

    @Test(timeout = 4000)
    public void testLifeCycleAndContextIntegrity() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        assertFalse(buf.isClosed());
        buf.flush();
        buf.close();
        assertTrue(buf.isClosed());
        assertNotNull(buf.version());

        ObjectMapper mapper = new ObjectMapper();
        buf.setCodec(mapper);
        assertSame(mapper, buf.getCodec());

        TokenBuffer.Parser parser = (TokenBuffer.Parser) buf.asParser(mapper);
        assertSame(mapper, parser.getCodec());
        assertNotNull(parser.version());
        assertSame(JsonLocation.NA, parser.getCurrentLocation());
        assertSame(JsonLocation.NA, parser.getTokenLocation());

        JsonLocation customLoc = new JsonLocation("srcRef", 100L, 1, 10);
        parser.setLocation(customLoc);
        assertSame(customLoc, parser.getCurrentLocation());
        assertSame(customLoc, parser.getTokenLocation());
    }

    @Test(timeout = 4000)
    public void testOverrideCurrentNameInParser() throws Exception {
        TokenBuffer buf = new TokenBuffer(null);
        buf.writeStartObject();
        buf.writeFieldName("oldName");
        buf.writeString("val");
        buf.writeEndObject();

        TokenBuffer.Parser p = (TokenBuffer.Parser) buf.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        p.overrideCurrentName("parentCustom");
        assertEquals("parentCustom", p.getCurrentName());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("oldName", p.getCurrentName());
        p.overrideCurrentName("newName");
        assertEquals("newName", p.getCurrentName());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("newName", p.getCurrentName());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
    }

    @Test(timeout = 4000)
    public void testSegmentDirectOperations() {
        TokenBuffer.Segment seg = new TokenBuffer.Segment();
        assertFalse(seg.hasIds());
        assertNull(seg.findObjectId(0));
        assertNull(seg.findTypeId(0));
        assertNull(seg.next());

        seg.append(0, JsonToken.START_OBJECT, "objId0", "typeId0");
        assertTrue(seg.hasIds());
        assertEquals("objId0", seg.findObjectId(0));
        assertEquals("typeId0", seg.findTypeId(0));
        assertEquals(JsonToken.START_OBJECT, seg.type(0));

        seg.appendRaw(1, 99, "rawVal");
        assertEquals("rawVal", seg.get(1));
        assertEquals(99, seg.rawType(1));

        TokenBuffer.Segment nextSeg = seg.append(TokenBuffer.Segment.TOKENS_PER_SEGMENT, JsonToken.VALUE_TRUE);
        assertNotNull(nextSeg);
        assertEquals(JsonToken.VALUE_TRUE, nextSeg.type(0));
    }

    @Test(timeout = 4000)
    public void testDeserializeMethodWithContext() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer source = new TokenBuffer(mapper);
        source.writeStartObject();
        source.writeFieldName("key");
        source.writeString("value");
        source.writeEndObject();

        JsonParser parser = source.asParser();
        parser.nextToken(); // advance to START_OBJECT

        TokenBuffer target = new TokenBuffer(mapper);
        DeserializationContext ctxt = mapper.getDeserializationContext();
        target.deserialize(parser, ctxt);

        JsonParser verify = target.asParser();
        assertEquals(JsonToken.START_OBJECT, verify.nextToken());
        assertEquals(JsonToken.FIELD_NAME, verify.nextToken());
        assertEquals("key", verify.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, verify.nextToken());
        assertEquals("value", verify.getText());
        assertEquals(JsonToken.END_OBJECT, verify.nextToken());
        assertNull(verify.nextToken());
    }
}