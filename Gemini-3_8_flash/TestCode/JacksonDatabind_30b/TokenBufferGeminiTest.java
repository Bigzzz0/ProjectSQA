package com.fasterxml.jackson.databind.util;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.core.io.SerializedString;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * TARGET CLASS: TokenBuffer, TokenBuffer.Parser, TokenBuffer.Segment
 * KNOWN DEFECT: TestExternalId::testBigDecimal965 (Jackson databind issue #965)
 *               copyCurrentEvent(JsonParser) coercing floating-point tokens (VALUE_NUMBER_FLOAT)
 *               to double instead of preserving full BigDecimal precision (-10000000000.0000000001 -> -1.0E+10).
 *
 * COVERAGE PARTITIONS & BRANCH TARGETS:
 * 1. Partition A: Core Functional Logic & State Transitions
 *    - Full token sequences: START_OBJECT, END_OBJECT, START_ARRAY, END_ARRAY, FIELD_NAME, primitives.
 *    - Segment boundary crossing: TOKENS_PER_SEGMENT (16 tokens limit per segment linked-list chunk).
 *    - Parser iteration: nextToken(), nextFieldName(), peekNextToken(), getParsingContext().
 *    - State serialization: serialize(JsonGenerator) traversing single and multi-segment buffers.
 *
 * 2. Partition B: Boundary Value Analysis (BVA) & Extremes
 *    - Empty buffers: firstToken() == null, nextToken() == null.
 *    - String & number boundaries: null strings, BigDecimal, BigInteger, float, double, short, long, int.
 *    - Buffer append: appending empty, non-empty, and native-id carrying TokenBuffers.
 *    - Raw values: writeRawValue(String), writeRawValue(String, offset, len), writeRawValue(char[], offset, len).
 *    - toString() truncation: > 100 entries truncation branch.
 *
 * 3. Partition C: Defect-Targeted Branch Zone (Jackson Databind Issue #965)
 *    - Exact reproduction: testDefectBigDecimal965PrecisionLoss & testDefectBigDecimal965StructureCopy.
 *    - Asserts exact preservation of high-precision BigDecimal values through copyCurrentEvent/copyCurrentStructure.
 *
 * 4. Partition D: Exception & Defensive Guard Paths
 *    - Unsupported generator operations: writeRaw(), writeUTF8String(), writeBinary(stream).
 *    - Parser error conditions: _checkIsNumber() on non-numeric, getBinaryValue() on non-binary/string.
 *    - Illegal numeric state: getNumberValue() on invalid embedded type.
 *    - TokenBuffer.deserialize(): malformed stream missing END_OBJECT triggering mappingException.
 *    - serialize(): unrecognized VALUE_NUMBER_FLOAT type triggering JsonGenerationException.
 *
 * 5. Partition E: Object Lifecycle & Contract Integrity
 *    - Native Object & Type IDs: canReadObjectId(), canReadTypeId(), writeObjectId(), writeTypeId().
 *    - Generator configuration: enable(), disable(), isEnabled(), setFeatureMask(), getOutputContext().
 *    - Version & Codec binding: version(), setCodec(), getCodec().
 *    - Unbalanced contexts: writeEndObject() and writeEndArray() without root context exhaustion.
 * ----------------------------------------------------------------------------------------------------
 */
public class TokenBufferGeminiTest {

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testDefectBigDecimal965PrecisionLoss() throws Exception {
        String numStr = "-10000000000.0000000001";
        JsonFactory f = new JsonFactory();
        JsonParser p = f.createParser(numStr);
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());

        TokenBuffer tb = new TokenBuffer(p);
        tb.copyCurrentEvent(p);

        JsonParser reader = tb.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, reader.nextToken());

        BigDecimal expected = new BigDecimal(numStr);
        BigDecimal actual = reader.getDecimalValue();
        assertEquals("Expected BigDecimal = " + expected + "; got back BigDecimal = " + actual,
                expected, actual);
        p.close();
        reader.close();
        tb.close();
    }

    @Test(timeout = 4000)
    public void testDefectBigDecimal965StructureCopy() throws Exception {
        String json = "{\"val\":-10000000000.0000000001}";
        JsonFactory f = new JsonFactory();
        JsonParser p = f.createParser(json);
        p.nextToken(); // START_OBJECT

        TokenBuffer tb = new TokenBuffer(p);
        tb.copyCurrentStructure(p);

        JsonParser reader = tb.asParser();
        assertEquals(JsonToken.START_OBJECT, reader.nextToken());
        assertEquals(JsonToken.FIELD_NAME, reader.nextToken());
        assertEquals("val", reader.getCurrentName());
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, reader.nextToken());

        BigDecimal expected = new BigDecimal("-10000000000.0000000001");
        BigDecimal actual = reader.getDecimalValue();
        assertEquals("Expected BigDecimal = " + expected + "; got back BigDecimal = " + actual,
                expected, actual);
        p.close();
        reader.close();
        tb.close();
    }

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testBasicWritingAndReading() throws Exception {
        TokenBuffer tb = new TokenBuffer((ObjectCodec) null, false);
        assertFalse(tb.isClosed());
        assertEquals(JsonToken.NOT_AVAILABLE, tb.firstToken() == null ? JsonToken.NOT_AVAILABLE : tb.firstToken());

        tb.writeStartObject();
        tb.writeFieldName("strField");
        tb.writeString("hello");
        tb.writeFieldName(new SerializedString("intField"));
        tb.writeNumber(123);
        tb.writeFieldName("boolField");
        tb.writeBoolean(true);
        tb.writeFieldName("nullField");
        tb.writeNull();
        tb.writeEndObject();

        assertEquals(JsonToken.START_OBJECT, tb.firstToken());

        JsonParser p = tb.asParser();
        assertFalse(p.isClosed());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertNull(p.getCurrentName());

        assertEquals("strField", p.nextFieldName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("hello", p.getText());
        assertArrayEquals("hello".toCharArray(), p.getTextCharacters());
        assertEquals(5, p.getTextLength());
        assertEquals(0, p.getTextOffset());
        assertFalse(p.hasTextCharacters());

        assertEquals("intField", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(123, p.getIntValue());
        assertEquals(123L, p.getLongValue());
        assertEquals(123.0, p.getDoubleValue(), 0.0001);
        assertEquals(123.0f, p.getFloatValue(), 0.0001f);
        assertEquals(JsonParser.NumberType.INT, p.getNumberType());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("boolField", p.getCurrentName());
        assertEquals(JsonToken.VALUE_TRUE, p.nextToken());
        assertEquals("true", p.getText());

        assertEquals("nullField", p.nextFieldName());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals("null", p.getText());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());

        p.close();
        assertTrue(p.isClosed());
        tb.close();
        assertTrue(tb.isClosed());
    }

    @Test(timeout = 4000)
    public void testMultiSegmentCrossing() throws Exception {
        TokenBuffer tb = new TokenBuffer((ObjectCodec) null);
        // Write 35 tokens to force spanning 3 segments (16 per segment)
        tb.writeStartArray();
        for (int i = 0; i < 33; i++) {
            tb.writeNumber(i);
        }
        tb.writeEndArray();

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        for (int i = 0; i < 33; i++) {
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.peekNextToken());
            assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
            assertEquals(i, p.getIntValue());
        }
        assertEquals(JsonToken.END_ARRAY, p.peekNextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.peekNextToken());
        assertNull(p.nextToken());
        p.close();

        // Also test serialize over multiple segments
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        tb.serialize(gen);
        gen.close();
        assertTrue(sw.toString().startsWith("[0,1,2"));
        assertTrue(sw.toString().endsWith("31,32]"));
        tb.close();
    }

    @Test(timeout = 4000)
    public void testParserContextAndCurrentNameOverride() throws Exception {
        TokenBuffer tb = new TokenBuffer((ObjectCodec) null);
        tb.writeStartObject();
        tb.writeFieldName("rootField");
        tb.writeStartObject();
        tb.writeFieldName("childField");
        tb.writeString("childVal");
        tb.writeEndObject();
        tb.writeFieldName("arrayField");
        tb.writeStartArray();
        tb.writeEndArray();
        tb.writeEndObject();

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        p.overrideCurrentName("overriddenRoot");
        assertNull(p.getCurrentName()); // root context has no parent name

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("rootField", p.getCurrentName());
        p.overrideCurrentName("renamedRootField");
        assertEquals("renamedRootField", p.getCurrentName());

        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals("renamedRootField", p.getCurrentName()); // reads from parent context
        p.overrideCurrentName("parentRenamedAgain");
        assertEquals("parentRenamedAgain", p.getCurrentName());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("childField", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());

        assertEquals(JsonToken.END_OBJECT, p.nextToken());

        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("arrayField", p.getCurrentName());
        assertEquals(JsonToken.START_ARRAY, p.nextToken());
        assertEquals("arrayField", p.getCurrentName());

        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        tb.close();
    }

    @Test(timeout = 4000)
    public void testUnbalancedEndObjectAndArray() throws Exception {
        TokenBuffer tb = new TokenBuffer((ObjectCodec) null);
        // Writing unbalanced close marks should not crash or throw exception
        tb.writeEndObject();
        tb.writeEndArray();

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertEquals(JsonToken.END_ARRAY, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        tb.close();
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Extremes
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testNumericBoundaryTypes() throws Exception {
        TokenBuffer tb = new TokenBuffer((ObjectCodec) null);
        tb.writeNumber((short) 10);
        tb.writeNumber(Integer.MAX_VALUE);
        tb.writeNumber(Long.MIN_VALUE);
        tb.writeNumber(3.14159f);
        tb.writeNumber(2.718281828459045);
        tb.writeNumber(new BigInteger("123456789012345678901234567890"));
        tb.writeNumber(new BigDecimal("999999999.9999999999999"));
        tb.writeNumber("42.5");
        tb.writeNumber((BigDecimal) null);
        tb.writeNumber((BigInteger) null);

        JsonParser p = tb.asParser();
        // short
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(10, p.getIntValue());
        assertEquals(JsonParser.NumberType.INT, p.getNumberType());
        // max int
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(Integer.MAX_VALUE, p.getIntValue());
        assertEquals(JsonParser.NumberType.INT, p.getNumberType());
        // min long
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(Long.MIN_VALUE, p.getLongValue());
        assertEquals(JsonParser.NumberType.LONG, p.getNumberType());
        // float
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(3.14159f, p.getFloatValue(), 0.0001f);
        assertEquals(JsonParser.NumberType.FLOAT, p.getNumberType());
        // double
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(2.718281828459045, p.getDoubleValue(), 0.000000001);
        assertEquals(JsonParser.NumberType.DOUBLE, p.getNumberType());
        // BigInteger
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(new BigInteger("123456789012345678901234567890"), p.getBigIntegerValue());
        assertEquals(JsonParser.NumberType.BIG_INTEGER, p.getNumberType());
        // BigDecimal
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(new BigDecimal("999999999.9999999999999"), p.getDecimalValue());
        assertEquals(new BigInteger("999999999"), p.getBigIntegerValue());
        assertEquals(JsonParser.NumberType.BIG_DECIMAL, p.getNumberType());
        // Encoded string number
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals("42.5", p.getText());
        assertEquals(42.5, p.getDoubleValue(), 0.001);
        // null BigDecimal / BigInteger become VALUE_NULL
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        tb.close();
    }

    @Test(timeout = 4000)
    public void testStringsAndRawValues() throws Exception {
        TokenBuffer tb = new TokenBuffer((ObjectCodec) null);
        tb.writeString((String) null);
        tb.writeString((SerializableString) null);
        tb.writeString(new SerializedString("serializable"));
        tb.writeString(new char[]{'a', 'b', 'c', 'd'}, 1, 2);

        tb.writeRawValue("raw1");
        tb.writeRawValue("prefix_raw2_suffix", 7, 4);
        tb.writeRawValue("exact", 0, 5);
        tb.writeRawValue(new char[]{'x', 'y', 'z'}, 0, 3);

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("serializable", p.getText());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("bc", p.getText());

        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertTrue(p.getEmbeddedObject() instanceof RawValue);
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals("raw2", p.getEmbeddedObject().toString());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals("exact", p.getEmbeddedObject().toString());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        assertEquals("xyz", p.getEmbeddedObject());

        assertNull(p.nextToken());
        p.close();
        tb.close();
    }

    @Test(timeout = 4000)
    public void testBinaryHandling() throws Exception {
        TokenBuffer tb = new TokenBuffer((ObjectCodec) null);
        byte[] data = new byte[]{1, 2, 3, 4, 5, 127, -128};
        tb.writeBinary(Base64Variants.MIME, data, 1, 5);

        // Also write string base64 for getBinaryValue text path
        String b64 = Base64Variants.MIME.encode(data);
        tb.writeString(b64);

        JsonParser p = tb.asParser();
        // Embedded byte[]
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p.nextToken());
        byte[] read1 = p.getBinaryValue(Base64Variants.MIME);
        assertEquals(5, read1.length);
        assertEquals(2, read1[0]);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead = p.readBinaryValue(Base64Variants.MIME, baos);
        assertEquals(5, bytesRead);
        assertArrayEquals(read1, baos.toByteArray());

        // Base64 decoded string
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        byte[] read2 = p.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(data, read2);

        // Builder reuse path (second call to getBinaryValue on same parser instance)
        byte[] read3 = p.getBinaryValue(Base64Variants.MIME);
        assertArrayEquals(data, read3);

        p.close();
        tb.close();
    }

    @Test(timeout = 4000)
    public void testToStringAndTruncation() throws Exception {
        TokenBuffer tb = new TokenBuffer((ObjectCodec) null);
        tb.writeStartObject();
        tb.writeFieldName("f");
        tb.writeString("v");
        tb.writeEndObject();
        String strSmall = tb.toString();
        assertTrue(strSmall.contains("START_OBJECT"));
        assertTrue(strSmall.contains("FIELD_NAME(f)"));
        assertTrue(strSmall.contains("VALUE_STRING"));

        // Trigger > 100 entries truncation
        TokenBuffer tbLarge = new TokenBuffer((ObjectCodec) null);
        for (int i = 0; i < 105; i++) {
            tbLarge.writeNumber(i);
        }
        String strLarge = tbLarge.toString();
        assertTrue(strLarge.contains("... (truncated 5 entries)"));
        tb.close();
        tbLarge.close();
    }

    @Test(timeout = 4000)
    public void testAppendBuffers() throws Exception {
        TokenBuffer tb1 = new TokenBuffer((ObjectCodec) null, false);
        tb1.writeNumber(1);

        TokenBuffer tb2 = new TokenBuffer((ObjectCodec) null, true);
        tb2.writeTypeId("myTypeId");
        tb2.writeObjectId("myObjectId");
        tb2.writeString("two");

        tb1.append(tb2);

        JsonParser p = tb1.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(1, p.getIntValue());

        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("two", p.getText());
        assertEquals("myTypeId", p.getTypeId());
        assertEquals("myObjectId", p.getObjectId());

        assertNull(p.nextToken());
        p.close();
        tb1.close();
        tb2.close();
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testUnsupportedRawWrites() throws Exception {
        TokenBuffer tb = new TokenBuffer((ObjectCodec) null);

        try {
            tb.writeRawUTF8String(new byte[2], 0, 2);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeUTF8String(new byte[2], 0, 2);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeRaw("test");
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeRaw("test", 0, 2);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeRaw(new SerializedString("test"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeRaw(new char[]{'a'}, 0, 1);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeRaw('a');
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}

        try {
            tb.writeBinary(Base64Variants.MIME, (InputStream) null, 10);
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {}

        tb.close();
    }

    @Test(timeout = 4000)
    public void testParserNumericAndBinaryErrors() throws Exception {
        TokenBuffer tb = new TokenBuffer((ObjectCodec) null);
        tb.writeString("notANumber");
        tb.writeBoolean(false);

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());

        try {
            p.getIntValue();
            fail("Expected JsonParseException when requesting int from non-numeric token");
        } catch (JsonParseException expected) {}

        try {
            p.getDecimalValue();
            fail("Expected JsonParseException");
        } catch (JsonParseException expected) {}

        assertEquals(JsonToken.VALUE_FALSE, p.nextToken());
        try {
            p.getBinaryValue(Base64Variants.MIME);
            fail("Expected JsonParseException when reading binary from boolean");
        } catch (JsonParseException expected) {}

        p.close();
        tb.close();
    }

    @Test(timeout = 4000)
    public void testParserIllegalNumberStringConversion() throws Exception {
        TokenBuffer tb = new TokenBuffer((ObjectCodec) null);
        tb._appendRaw(JsonToken.VALUE_NUMBER_INT.ordinal(), Boolean.TRUE);

        JsonParser p = tb.asParser();
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        try {
            p.getNumberValue();
            fail("Expected IllegalStateException for invalid entry in segment");
        } catch (IllegalStateException expected) {
            assertTrue(expected.getMessage().contains("entry should be a Number"));
        }
        p.close();
        tb.close();
    }

    @Test(timeout = 4000)
    public void testSerializeUnrecognizedFloatType() throws Exception {
        TokenBuffer tb = new TokenBuffer((ObjectCodec) null);
        // Force an unrecognized object under VALUE_NUMBER_FLOAT
        tb._appendRaw(JsonToken.VALUE_NUMBER_FLOAT.ordinal(), Boolean.TRUE);

        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        try {
            tb.serialize(gen);
            fail("Expected JsonGenerationException for unrecognized float object");
        } catch (JsonGenerationException expected) {
            assertTrue(expected.getMessage().contains("Unrecognized value type for VALUE_NUMBER_FLOAT"));
        }
        gen.close();
        tb.close();
    }

    @Test(timeout = 4000)
    public void testDeserializeStartingFromFieldNameError() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        // JSON that is missing END_OBJECT after field
        JsonParser p = mapper.getFactory().createParser("{\"a\":1");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME "a"

        TokenBuffer tb = new TokenBuffer(mapper);
        try {
            tb.deserialize(p, mapper.getDeserializationContext());
            fail("Expected JsonMappingException for missing END_OBJECT");
        } catch (JsonMappingException expected) {
            assertTrue(expected.getMessage().contains("Expected END_OBJECT"));
        }
        p.close();
        tb.close();
    }

    /*
    /**********************************************************
    /* Partition E: Object Lifecycle & Contract Integrity
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testGeneratorConfigurationAndLifecycle() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer tb = new TokenBuffer(mapper, true);
        assertNotNull(tb.version());
        assertSame(mapper, tb.getCodec());

        tb.setCodec(null);
        assertNull(tb.getCodec());

        assertTrue(tb.canWriteBinaryNatively());
        assertTrue(tb.canWriteTypeId());
        assertTrue(tb.canWriteObjectId());

        tb.enable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        assertTrue(tb.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));

        tb.disable(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);
        assertFalse(tb.isEnabled(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN));

        int mask = tb.getFeatureMask();
        tb.setFeatureMask(mask | JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION.getMask());
        assertTrue(tb.isEnabled(JsonGenerator.Feature.STRICT_DUPLICATE_DETECTION));

        assertSame(tb, tb.useDefaultPrettyPrinter());
        assertNotNull(tb.getOutputContext());

        tb.flush(); // no-op
        assertFalse(tb.isClosed());
        tb.close();
        assertTrue(tb.isClosed());
    }

    @Test(timeout = 4000)
    public void testWriteObjectAndWriteTreeBranches() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TokenBuffer tbWithCodec = new TokenBuffer(mapper);

        // writeObject null
        tbWithCodec.writeObject(null);
        // writeTree null
        tbWithCodec.writeTree(null);

        // writeObject POJO via codec
        tbWithCodec.writeObject(Integer.valueOf(777));
        // writeTree via codec
        ObjectNode node = JsonNodeFactory.instance.objectNode();
        node.put("k", "v");
        tbWithCodec.writeTree(node);

        JsonParser p = tbWithCodec.asParser();
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals(JsonToken.VALUE_NULL, p.nextToken());
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(777, p.getIntValue());
        assertEquals(JsonToken.START_OBJECT, p.nextToken());
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals("k", p.getCurrentName());
        assertEquals(JsonToken.VALUE_STRING, p.nextToken());
        assertEquals("v", p.getText());
        assertEquals(JsonToken.END_OBJECT, p.nextToken());
        assertNull(p.nextToken());
        p.close();
        tbWithCodec.close();

        // Without codec: should buffer as VALUE_EMBEDDED_OBJECT
        TokenBuffer tbNoCodec = new TokenBuffer((ObjectCodec) null);
        tbNoCodec.writeObject("myEmbeddedObject");
        tbNoCodec.writeTree(node);

        JsonParser p2 = tbNoCodec.asParser();
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p2.nextToken());
        assertEquals("myEmbeddedObject", p2.getEmbeddedObject());
        assertEquals(JsonToken.VALUE_EMBEDDED_OBJECT, p2.nextToken());
        assertSame(node, p2.getEmbeddedObject());
        p2.close();
        tbNoCodec.close();
    }

    @Test(timeout = 4000)
    public void testParserLocationsAndSource() throws Exception {
        JsonFactory f = new JsonFactory();
        JsonParser src = f.createParser("{\"k\": 1}");
        src.nextToken();

        TokenBuffer tb = new TokenBuffer(src, null);
        tb.copyCurrentStructure(src);

        JsonParser p = tb.asParser(src);
        assertNotNull(p.getTokenLocation());
        assertNotNull(p.getCurrentLocation());

        p.setLocation(new JsonLocation("testSource", 100L, 1, 10));
        assertEquals(100L, p.getCurrentLocation().getByteOffset());

        p.close();
        src.close();
        tb.close();
    }

    @Test(timeout = 4000)
    public void testSerializeAllTokenBranches() throws Exception {
        TokenBuffer tb = new TokenBuffer((ObjectCodec) null);
        tb.writeStartObject();
        tb.writeFieldName(new SerializedString("str"));
        tb.writeString(new SerializedString("val"));
        tb.writeFieldName("short");
        tb.writeNumber((short) 1);
        tb.writeFieldName("long");
        tb.writeNumber(2L);
        tb.writeFieldName("bigInt");
        tb.writeNumber(BigInteger.valueOf(3));
        tb.writeFieldName("byte");
        tb._appendRaw(JsonToken.VALUE_NUMBER_INT.ordinal(), Byte.valueOf((byte) 4));
        tb.writeFieldName("float");
        tb.writeNumber(5.5f);
        tb.writeFieldName("double");
        tb.writeNumber(6.6d);
        tb.writeFieldName("bigDec");
        tb.writeNumber(new BigDecimal("7.7"));
        tb.writeFieldName("numStr");
        tb.writeNumber("8.8");
        tb.writeFieldName("nullNum");
        tb._appendRaw(JsonToken.VALUE_NUMBER_FLOAT.ordinal(), null);
        tb.writeFieldName("rawVal");
        tb.writeObject(new RawValue("{\"nested\":true}"));
        tb.writeFieldName("boolF");
        tb.writeBoolean(false);
        tb.writeEndObject();

        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        tb.serialize(gen);
        gen.close();

        String json = sw.toString();
        assertTrue(json.contains("\"str\":\"val\""));
        assertTrue(json.contains("\"short\":1"));
        assertTrue(json.contains("\"long\":2"));
        assertTrue(json.contains("\"bigInt\":3"));
        assertTrue(json.contains("\"byte\":4"));
        assertTrue(json.contains("\"float\":5.5"));
        assertTrue(json.contains("\"double\":6.6"));
        assertTrue(json.contains("\"bigDec\":7.7"));
        assertTrue(json.contains("\"numStr\":8.8"));
        assertTrue(json.contains("\"nullNum\":null"));
        assertTrue(json.contains("\"rawVal\":{\"nested\":true}"));
        assertTrue(json.contains("\"boolF\":false"));
        tb.close();
    }

    @Test(timeout = 4000)
    public void testSegmentDirectManipulations() throws Exception {
        TokenBuffer.Segment seg = new TokenBuffer.Segment();
        assertFalse(seg.hasIds());
        assertNull(seg.findObjectId(0));
        assertNull(seg.findTypeId(0));
        assertNull(seg.next());

        TokenBuffer.Segment nextSeg = seg.append(TokenBuffer.Segment.TOKENS_PER_SEGMENT, JsonToken.START_OBJECT);
        assertNotNull(nextSeg);
        assertEquals(JsonToken.START_OBJECT, nextSeg.type(0));
        assertEquals(JsonToken.START_OBJECT.ordinal(), nextSeg.rawType(0));

        // Append with native IDs
        TokenBuffer.Segment nextSegWithIds = seg.append(TokenBuffer.Segment.TOKENS_PER_SEGMENT,
                JsonToken.VALUE_STRING, "val", "objId", "typeId");
        assertNotNull(nextSegWithIds);
        assertTrue(nextSegWithIds.hasIds());
        assertEquals("objId", nextSegWithIds.findObjectId(0));
        assertEquals("typeId", nextSegWithIds.findTypeId(0));
        assertEquals("val", nextSegWithIds.get(0));

        // AppendRaw
        TokenBuffer.Segment rawSeg = seg.appendRaw(TokenBuffer.Segment.TOKENS_PER_SEGMENT,
                JsonToken.VALUE_NUMBER_INT.ordinal(), 999, "rawObj", "rawType");
        assertNotNull(rawSeg);
        assertEquals(JsonToken.VALUE_NUMBER_INT, rawSeg.type(0));
        assertEquals("rawObj", rawSeg.findObjectId(0));
        assertEquals("rawType", rawSeg.findTypeId(0));
    }
}