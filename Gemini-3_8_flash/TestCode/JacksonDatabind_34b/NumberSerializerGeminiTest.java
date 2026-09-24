package com.fasterxml.jackson.databind.ser.std;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * /* [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.ser.std.NumberSerializer
 *
 * Decision / Condition Matrix:
 * 1. Constructor:
 *    - rawType == BigInteger.class -> _isInt = true
 *    - rawType != BigInteger.class -> _isInt = false (e.g. BigDecimal, Number, Integer, Double, etc.)
 *
 * 2. serialize(Number, JsonGenerator, SerializerProvider):
 *    - Branch 1: value instanceof BigDecimal -> writeNumber(BigDecimal)
 *    - Branch 2: value instanceof BigInteger -> writeNumber(BigInteger)
 *    - Branch 3: value instanceof Integer    -> writeNumber(int)
 *    - Branch 4: value instanceof Long       -> writeNumber(long)
 *    - Branch 5: value instanceof Double     -> writeNumber(double)
 *    - Branch 6: value instanceof Float      -> writeNumber(float)
 *    - Branch 7: value instanceof Byte       -> writeNumber(int)
 *    - Branch 8: value instanceof Short      -> writeNumber(int)
 *    - Branch 9: Fallback (custom Number)    -> writeNumber(String) via value.toString()
 *
 * 3. getSchema(SerializerProvider, Type):
 *    - _isInt == true  -> "integer" schema node
 *    - _isInt == false -> "number" schema node
 *
 * 4. acceptJsonFormatVisitor(JsonFormatVisitorWrapper, JavaType):
 *    - Branch A: _isInt == true -> visitIntFormat(visitor, typeHint, JsonParser.NumberType.BIG_INTEGER)
 *    - Branch B: _isInt == false && handledType == BigDecimal.class
 *                -> [DEFECT LOCATION] Defective code passes JsonParser.NumberType.BIG_INTEGER
 *                   to visitFloatFormat instead of JsonParser.NumberType.BIG_DECIMAL!
 *    - Branch C: _isInt == false && handledType != BigDecimal.class
 *                -> visitor.expectNumberFormat(typeHint)
 */
public class NumberSerializerGeminiTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private JsonGenerator createGenerator(StringWriter writer) throws IOException {
        return mapper.getFactory().createGenerator(writer);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Serialization Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializeBigDecimal() throws IOException {
        NumberSerializer ser = new NumberSerializer(BigDecimal.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = createGenerator(sw);

        BigDecimal value = new BigDecimal("12345.67890");
        ser.serialize(value, gen, mapper.getSerializerProvider());
        gen.flush();

        assertEquals("12345.67890", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeBigInteger() throws IOException {
        NumberSerializer ser = new NumberSerializer(BigInteger.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = createGenerator(sw);

        BigInteger value = new BigInteger("98765432109876543210");
        ser.serialize(value, gen, mapper.getSerializerProvider());
        gen.flush();

        assertEquals("98765432109876543210", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeInteger() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = createGenerator(sw);

        ser.serialize(Integer.valueOf(42), gen, mapper.getSerializerProvider());
        gen.flush();

        assertEquals("42", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeLong() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = createGenerator(sw);

        ser.serialize(Long.valueOf(123456789012345L), gen, mapper.getSerializerProvider());
        gen.flush();

        assertEquals("123456789012345", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeDouble() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = createGenerator(sw);

        ser.serialize(Double.valueOf(3.14159), gen, mapper.getSerializerProvider());
        gen.flush();

        assertEquals("3.14159", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeFloat() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = createGenerator(sw);

        ser.serialize(Float.valueOf(1.25f), gen, mapper.getSerializerProvider());
        gen.flush();

        assertEquals("1.25", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeByte() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = createGenerator(sw);

        ser.serialize(Byte.valueOf((byte) 7), gen, mapper.getSerializerProvider());
        gen.flush();

        assertEquals("7", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeShort() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = createGenerator(sw);

        ser.serialize(Short.valueOf((short) 1024), gen, mapper.getSerializerProvider());
        gen.flush();

        assertEquals("1024", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeFallbackCustomNumber() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = createGenerator(sw);

        AtomicInteger customNumber = new AtomicInteger(555);
        ser.serialize(customNumber, gen, mapper.getSerializerProvider());
        gen.flush();

        assertEquals("555", sw.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Schema Invariants
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializeExtremeValues() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;

        // Long boundaries
        StringWriter sw = new StringWriter();
        JsonGenerator gen = createGenerator(sw);
        ser.serialize(Long.MAX_VALUE, gen, mapper.getSerializerProvider());
        ser.serialize(Long.MIN_VALUE, gen, mapper.getSerializerProvider());
        gen.flush();
        assertEquals(Long.MAX_VALUE + "" + Long.MIN_VALUE, sw.toString());

        // Integer boundaries
        sw = new StringWriter();
        gen = createGenerator(sw);
        ser.serialize(Integer.MAX_VALUE, gen, mapper.getSerializerProvider());
        ser.serialize(Integer.MIN_VALUE, gen, mapper.getSerializerProvider());
        gen.flush();
        assertEquals(Integer.MAX_VALUE + "" + Integer.MIN_VALUE, sw.toString());

        // Byte / Short boundaries
        sw = new StringWriter();
        gen = createGenerator(sw);
        ser.serialize(Byte.MAX_VALUE, gen, mapper.getSerializerProvider());
        ser.serialize(Short.MIN_VALUE, gen, mapper.getSerializerProvider());
        gen.flush();
        assertEquals(Byte.MAX_VALUE + "" + Short.MIN_VALUE, sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeZeroAndNegativeValues() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;

        StringWriter sw = new StringWriter();
        JsonGenerator gen = createGenerator(sw);
        ser.serialize(0, gen, mapper.getSerializerProvider());
        ser.serialize(-0.0, gen, mapper.getSerializerProvider());
        ser.serialize(BigDecimal.ZERO, gen, mapper.getSerializerProvider());
        ser.serialize(BigInteger.ZERO, gen, mapper.getSerializerProvider());
        gen.flush();

        assertEquals("0-0.000", sw.toString());
    }

    @Test(timeout = 4000)
    public void testGetSchemaForBigInteger() {
        NumberSerializer ser = new NumberSerializer(BigInteger.class);
        JsonNode schema = ser.getSchema(mapper.getSerializerProvider(), null);
        assertNotNull(schema);
        assertEquals("integer", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testGetSchemaForGeneralNumber() {
        NumberSerializer ser = NumberSerializer.instance;
        JsonNode schema = ser.getSchema(mapper.getSerializerProvider(), null);
        assertNotNull(schema);
        assertEquals("number", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testGetSchemaForBigDecimal() {
        NumberSerializer ser = new NumberSerializer(BigDecimal.class);
        JsonNode schema = ser.getSchema(mapper.getSerializerProvider(), null);
        assertNotNull(schema);
        assertEquals("number", schema.get("type").asText());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * TARGETED DEFECT TEST:
     * In NumberSerializer.acceptJsonFormatVisitor(), when handledType() is BigDecimal.class,
     * the serializer was erroneously calling:
     *   visitFloatFormat(visitor, typeHint, JsonParser.NumberType.BIG_INTEGER);
     * instead of:
     *   visitFloatFormat(visitor, typeHint, JsonParser.NumberType.BIG_DECIMAL);
     *
     * This test explicitly checks the NumberType passed to the visitor for BigDecimal.
     */
    @Test(timeout = 4000)
    public void testBigDecimalVisitorNumberTypeExactDefectTrigger() throws JsonMappingException {
        NumberSerializer serializer = new NumberSerializer(BigDecimal.class);
        JavaType javaType = TypeFactory.defaultInstance().constructType(BigDecimal.class);

        final JsonParser.NumberType[] recordedType = new JsonParser.NumberType[1];
        final boolean[] numberFormatVisited = new boolean[1];

        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                numberFormatVisited[0] = true;
                return new JsonNumberFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        recordedType[0] = type;
                    }
                };
            }
        };

        serializer.acceptJsonFormatVisitor(visitor, javaType);

        assertTrue("expectNumberFormat should have been called", numberFormatVisited[0]);
        // On the defective version, recordedType[0] will be BIG_INTEGER, causing this assertion to fail!
        assertEquals("BigDecimal must be visited with NumberType.BIG_DECIMAL",
                JsonParser.NumberType.BIG_DECIMAL, recordedType[0]);
    }

    // =========================================================================
    // Partition D: Visitor Branches for Other Types
    // =========================================================================

    @Test(timeout = 4000)
    public void testBigIntegerVisitorBranch() throws JsonMappingException {
        NumberSerializer serializer = new NumberSerializer(BigInteger.class);
        JavaType javaType = TypeFactory.defaultInstance().constructType(BigInteger.class);

        final JsonParser.NumberType[] recordedType = new JsonParser.NumberType[1];
        final boolean[] integerFormatVisited = new boolean[1];

        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                integerFormatVisited[0] = true;
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        recordedType[0] = type;
                    }
                };
            }
        };

        serializer.acceptJsonFormatVisitor(visitor, javaType);

        assertTrue("expectIntegerFormat should have been called for BigInteger", integerFormatVisited[0]);
        assertEquals("BigInteger must be visited with NumberType.BIG_INTEGER",
                JsonParser.NumberType.BIG_INTEGER, recordedType[0]);
    }

    @Test(timeout = 4000)
    public void testGeneralNumberVisitorBranch() throws JsonMappingException {
        NumberSerializer serializer = NumberSerializer.instance;
        JavaType javaType = TypeFactory.defaultInstance().constructType(Number.class);

        final boolean[] numberFormatVisited = new boolean[1];

        JsonFormatVisitorWrapper.Base visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                numberFormatVisited[0] = true;
                return new JsonNumberFormatVisitor.Base();
            }
        };

        serializer.acceptJsonFormatVisitor(visitor, javaType);

        assertTrue("expectNumberFormat should have been called for fallback Number.class", numberFormatVisited[0]);
    }

    @Test(timeout = 4000)
    public void testVisitorReturningNullSubVisitor() throws JsonMappingException {
        NumberSerializer bigDecimalSerializer = new NumberSerializer(BigDecimal.class);
        NumberSerializer bigIntegerSerializer = new NumberSerializer(BigInteger.class);
        NumberSerializer numberSerializer = NumberSerializer.instance;

        // Visitor that returns null sub-visitors (simulating null-safe visitor implementations)
        JsonFormatVisitorWrapper.Base nullVisitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                return null;
            }

            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                return null;
            }
        };

        // None of these should throw NullPointerException
        bigDecimalSerializer.acceptJsonFormatVisitor(nullVisitor, null);
        bigIntegerSerializer.acceptJsonFormatVisitor(nullVisitor, null);
        numberSerializer.acceptJsonFormatVisitor(nullVisitor, null);
    }

    // =========================================================================
    // Partition E: Object Contract & Custom Number Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testInstanceAndHandledTypes() {
        assertNotNull(NumberSerializer.instance);
        assertEquals(Number.class, NumberSerializer.instance.handledType());

        NumberSerializer bigIntSer = new NumberSerializer(BigInteger.class);
        assertEquals(BigInteger.class, bigIntSer.handledType());

        NumberSerializer bigDecSer = new NumberSerializer(BigDecimal.class);
        assertEquals(BigDecimal.class, bigDecSer.handledType());
    }

    @Test(timeout = 4000)
    public void testSerializeCustomNumberToStringFallback() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = createGenerator(sw);

        AtomicLong atomicLong = new AtomicLong(999999999999L);
        ser.serialize(atomicLong, gen, mapper.getSerializerProvider());
        gen.flush();

        assertEquals("999999999999", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeNullValueThrowsNPE() throws IOException {
        NumberSerializer ser = NumberSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = createGenerator(sw);

        try {
            ser.serialize(null, gen, mapper.getSerializerProvider());
            fail("Expected NullPointerException when serializing null Number");
        } catch (NullPointerException expected) {
            // Success
        }
    }
}