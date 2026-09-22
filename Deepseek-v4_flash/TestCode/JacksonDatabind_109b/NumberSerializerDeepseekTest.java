package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonFormat.Shape;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializer;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.*;

/**
 * Branch & Defect Analysis Matrix:
 *
 * Target: NumberSerializer (com.fasterxml.jackson.databind.ser.std)
 * Known Defect: [databind#2264] – BigDecimal serialized in scientific notation (5E-10) instead of plain decimal (0.0000000005)
 *
 * Decision branches to cover:
 *   Constructor: rawType == BigInteger.class → _isInt = true; else false
 *   createContextual(): format != null && shape == STRING → return ToStringSerializer; else return this
 *   serialize():
 *     - value instanceof BigDecimal
 *     - value instanceof BigInteger
 *     - value instanceof Long
 *     - value instanceof Double
 *     - value instanceof Float
 *     - value instanceof Integer || Byte || Short
 *     - fallback value.toString()
 *   getSchema(): _isInt → "integer" / "number"
 *   acceptJsonFormatVisitor():
 *     - _isInt true → visitIntFormat
 *     - handledType() == BigDecimal.class → visitFloatFormat
 *     - else → visitor.expectNumberFormat
 *
 * Boundary conditions:
 *   - Null value → NullPointerException (defensive)
 *   - BigDecimal plain representation (defect trigger: 0.0000000005 vs 5E-10)
 *   - BigInteger edge values (0, negative, MAX_VALUE)
 *   - Double/NaN/Infinity
 *   - Custom Number subclass (AtomicLong)
 *   - @JsonFormat(shape=STRING) on Number fields
 */
public class NumberSerializerDeepseekTest {

    // =====================================================
    // Partition A: Core Functional Logic & State Transitions
    // =====================================================

    @Test(timeout = 4000)
    public void testConstructorForBigIntegerSetsIsIntTrue() {
        NumberSerializer serializer = new NumberSerializer(BigInteger.class);
        assertTrue("_isInt should be true for BigInteger.class", serializer._isInt);
    }

    @Test(timeout = 4000)
    public void testConstructorForNumberClassSetsIsIntFalse() {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        assertFalse("_isInt should be false for Number.class", serializer._isInt);
    }

    @Test(timeout = 4000)
    public void testStaticInstanceIsNotNull() {
        assertNotNull(NumberSerializer.instance);
    }

    @Test(timeout = 4000)
    public void testSerializeBigDecimal() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();

        BigDecimal value = new BigDecimal("123.456");
        serializer.serialize(value, gen, prov);
        gen.flush();
        assertEquals("123.456", sw.toString().trim());
    }

    @Test(timeout = 4000)
    public void testSerializeBigInteger() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();

        BigInteger value = new BigInteger("9876543210");
        serializer.serialize(value, gen, prov);
        gen.flush();
        assertEquals("9876543210", sw.toString().trim());
    }

    @Test(timeout = 4000)
    public void testSerializeLong() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();

        Long value = 42L;
        serializer.serialize(value, gen, prov);
        gen.flush();
        assertEquals("42", sw.toString().trim());
    }

    @Test(timeout = 4000)
    public void testSerializeDouble() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();

        Double value = 3.14;
        serializer.serialize(value, gen, prov);
        gen.flush();
        assertEquals("3.14", sw.toString().trim());
    }

    @Test(timeout = 4000)
    public void testSerializeFloat() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();

        Float value = 2.718f;
        serializer.serialize(value, gen, prov);
        gen.flush();
        assertTrue(sw.toString().trim().startsWith("2.718"));
    }

    @Test(timeout = 4000)
    public void testSerializeInteger() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();

        Integer value = 100;
        serializer.serialize(value, gen, prov);
        gen.flush();
        assertEquals("100", sw.toString().trim());
    }

    @Test(timeout = 4000)
    public void testSerializeByte() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();

        Byte value = 7;
        serializer.serialize(value, gen, prov);
        gen.flush();
        assertEquals("7", sw.toString().trim());
    }

    @Test(timeout = 4000)
    public void testSerializeShort() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();

        Short value = 32767;
        serializer.serialize(value, gen, prov);
        gen.flush();
        assertEquals("32767", sw.toString().trim());
    }

    @Test(timeout = 4000)
    public void testSerializeCustomNumberFallbackToString() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();

        AtomicLong value = new AtomicLong(999);
        serializer.serialize(value, gen, prov);
        gen.flush();
        // Fallback uses value.toString() which returns "999" for AtomicLong
        assertEquals("999", sw.toString().trim());
    }

    // =====================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =====================================================

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testSerializeNullThrowsNPE() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();

        serializer.serialize(null, gen, prov);
    }

    @Test(timeout = 4000)
    public void testSerializeBigDecimalZero() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();

        serializer.serialize(BigDecimal.ZERO, gen, prov);
        gen.flush();
        assertEquals("0", sw.toString().trim());
    }

    @Test(timeout = 4000)
    public void testSerializeBigIntegerNegative() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();

        BigInteger value = new BigInteger("-9999999999999999999");
        serializer.serialize(value, gen, prov);
        gen.flush();
        assertEquals("-9999999999999999999", sw.toString().trim());
    }

    @Test(timeout = 4000)
    public void testSerializeDoubleNaN() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();

        serializer.serialize(Double.NaN, gen, prov);
        gen.flush();
        assertEquals("NaN", sw.toString().trim());
    }

    @Test(timeout = 4000)
    public void testSerializeDoubleInfinity() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();

        serializer.serialize(Double.POSITIVE_INFINITY, gen, prov);
        gen.flush();
        assertEquals("Infinity", sw.toString().trim());
    }

    // =====================================================
    // Partition C: Defect-Targeted Branch Zone
    // =====================================================

    /**
     * Targets the known defect: [databind#2264] where BigDecimal is serialized
     * in scientific notation (e.g., 5E-10) instead of plain decimal (0.0000000005).
     * The test asserts that the output matches the plain decimal representation.
     */
    @Test(timeout = 4000)
    public void testSerializeBigDecimalPlainRepresentation() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProvider();

        BigDecimal value = new BigDecimal("0.0000000005");
        serializer.serialize(value, gen, prov);
        gen.flush();
        // This will fail on the defective version (which outputs "5E-10") and pass on a fixed version.
        assertEquals("0.0000000005", sw.toString().trim());
    }

    // =====================================================
    // Partition D: Exception & Defensive Guard Paths
    // =====================================================

    @Test(timeout = 4000)
    public void testCreateContextualReturnsThisForNullFormat() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        ObjectMapper mapper = new ObjectMapper();
        // null property is acceptable; without a property, format is null
        JsonSerializer<?> contextual = serializer.createContextual(mapper.getSerializerProvider(), null);
        assertSame(serializer, contextual);
    }

    @Test(timeout = 4000)
    public void testCreateContextualStringShapeReturnsToStringSerializer() throws Exception {
        // We need a bean property with @JsonFormat(shape=STRING)
        // Use a simple wrapper class to simulate
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unused")
        class BeanWithStringShape {
            @JsonFormat(shape = Shape.STRING)
            public Number value;
        }
        // Obtain the serializer via ObjectMapper – it will contextualize
        String json = mapper.writeValueAsString(new BeanWithStringShape() {{ value = BigDecimal.ONE; }});
        // Since shape=STRING, the value should be quoted
        assertTrue(json.contains("\"1\""));
    }

    @Test(timeout = 4000)
    public void testGetSchemaForIntType() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigInteger.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode schema = serializer.getSchema(mapper.getSerializerProvider(), null);
        assertEquals("integer", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testGetSchemaForNonIntType() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode schema = serializer.getSchema(mapper.getSerializerProvider(), null);
        assertEquals("number", schema.get("type").asText());
    }

    // Custom JsonFormatVisitorWrapper to verify acceptJsonFormatVisitor behavior
    private static class TestJsonFormatVisitorWrapper extends JsonFormatVisitorWrapper.Base {
        boolean intVisited = false;
        boolean floatVisited = false;
        boolean numberVisited = false;

        @Override
        public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
            intVisited = true;
            return null;
        }

        @Override
        public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
            numberVisited = true;
            return null;
        }

        @Override
        public JsonNumberFormatVisitor expectFloatFormat(JavaType type) {
            floatVisited = true;
            return null;
        }
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorForBigInteger() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigInteger.class);
        TestJsonFormatVisitorWrapper visitor = new TestJsonFormatVisitorWrapper();
        serializer.acceptJsonFormatVisitor(visitor, null);
        assertTrue("Expected integer format for BigInteger", visitor.intVisited);
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorForBigDecimal() throws Exception {
        NumberSerializer serializer = new NumberSerializer(BigDecimal.class);
        TestJsonFormatVisitorWrapper visitor = new TestJsonFormatVisitorWrapper();
        serializer.acceptJsonFormatVisitor(visitor, null);
        assertTrue("Expected float format for BigDecimal", visitor.floatVisited);
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorForOtherNumber() throws Exception {
        NumberSerializer serializer = new NumberSerializer(Number.class);
        TestJsonFormatVisitorWrapper visitor = new TestJsonFormatVisitorWrapper();
        serializer.acceptJsonFormatVisitor(visitor, null);
        assertTrue("Expected number format for generic Number", visitor.numberVisited);
    }

    // =====================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =====================================================

    @Test(timeout = 4000)
    public void testInstanceReuseSingleton() {
        assertSame(NumberSerializer.instance, new NumberSerializer(Number.class));
    }

    @Test(timeout = 4000)
    public void testToStringSerializerIsUsedForStringShape() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unused")
        class Bean {
            @JsonFormat(shape = Shape.STRING)
            public Number value = new BigDecimal("123.456");
        }
        String json = mapper.writeValueAsString(new Bean());
        assertEquals("{\"value\":\"123.456\"}", json);
    }

    @Test(timeout = 4000)
    public void testSerializeBigDecimalWithPlainStringViaToStringSerializer() throws Exception {
        // When shape=STRING, the serializer used is ToStringSerializer, which uses toString().
        // toString() for 0.0000000005 returns "5E-10" – the known defect occurs here.
        // This test verifies that even with STRING shape, the output is "5E-10" (defective) or "0.0000000005" (fixed).
        // Since we are targeting the defect, we assert the expected plain decimal representation.
        ObjectMapper mapper = new ObjectMapper();
        @SuppressWarnings("unused")
        class Bean {
            @JsonFormat(shape = Shape.STRING)
            public Number value = new BigDecimal("0.0000000005");
        }
        String json = mapper.writeValueAsString(new Bean());
        // In a fixed version, this would be "{\"value\":\"0.0000000005\"}".
        // For the defective version, it's "{\"value\":\"5E-10\"}".
        // We assert the fixed behavior (the test reveals the defect by failing on defective).
        assertEquals("{\"value\":\"0.0000000005\"}", json);
    }
}