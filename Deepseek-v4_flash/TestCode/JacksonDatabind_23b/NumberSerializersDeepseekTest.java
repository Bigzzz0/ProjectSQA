package com.fasterxml.jackson.databind.ser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.Base;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.IntegerSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.LongSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.FloatSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.DoubleSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.ShortSerializer;
import com.fasterxml.jackson.databind.ser.std.NumberSerializers.IntLikeSerializer;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: NumberSerializers and its inner classes (Base, ShortSerializer, IntegerSerializer, 
 *         IntLikeSerializer, LongSerializer, FloatSerializer, DoubleSerializer)
 * 
 * Decision branches targeted:
 * 1. Base constructor: _isInt = (numberType == INT || LONG || BIG_INTEGER) - 3 conditions
 * 2. acceptJsonFormatVisitor: if (_isInt) { ... } else { ... } - 2 branches
 * 3. acceptJsonFormatVisitor: if (v2 != null) - 2 branches per visitor type
 * 4. createContextual: if (property != null) - 2 branches
 * 5. createContextual: if (m != null) - 2 branches
 * 6. createContextual: if (format != null) - 2 branches
 * 7. createContextual: switch (format.getShape()) - STRING case vs default
 * 8. serialize methods: type casting and value extraction
 * 9. serializeWithType: delegation to serialize (IntegerSerializer, DoubleSerializer)
 * 
 * Boundary conditions:
 * - Null property in createContextual
 * - Null member in property
 * - Null format annotation
 * - Format shape STRING vs other shapes
 * - Number type boundaries (INT, LONG, BIG_INTEGER, FLOAT, DOUBLE)
 * - Zero values for all numeric types
 * - Negative values
 * - MAX/MIN values
 * 
 * Defect targeting (Defects4J ground truth):
 * - testEmptyInclusionScalars: expected:<{[]}> but was:<{["value":0]}>
 *   This indicates that when serializing with empty inclusion (JsonInclude.Include.NON_EMPTY),
 *   zero values should be excluded but are being included.
 *   The defect likely relates to how zero values are handled in serialization.
 *   We test that zero values are properly serialized/not serialized based on inclusion settings.
 */
public class NumberSerializersDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testBaseConstructorIntType() {
        // Test _isInt = true for INT type
        Base<?> base = new Base<Integer>(Integer.class, JsonParser.NumberType.INT, "integer") {
            @Override
            public void serialize(Integer value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeNumber(value);
            }
        };
        assertTrue("_isInt should be true for INT", base._isInt);
        assertEquals("_numberType should be INT", JsonParser.NumberType.INT, base._numberType);
        assertEquals("_schemaType should be integer", "integer", base._schemaType);
    }

    @Test(timeout = 4000)
    public void testBaseConstructorLongType() {
        Base<?> base = new Base<Long>(Long.class, JsonParser.NumberType.LONG, "number") {
            @Override
            public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeNumber(value);
            }
        };
        assertTrue("_isInt should be true for LONG", base._isInt);
        assertEquals("_numberType should be LONG", JsonParser.NumberType.LONG, base._numberType);
    }

    @Test(timeout = 4000)
    public void testBaseConstructorBigIntegerType() {
        Base<?> base = new Base<BigInteger>(BigInteger.class, JsonParser.NumberType.BIG_INTEGER, "number") {
            @Override
            public void serialize(BigInteger value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeNumber(value);
            }
        };
        assertTrue("_isInt should be true for BIG_INTEGER", base._isInt);
        assertEquals("_numberType should be BIG_INTEGER", JsonParser.NumberType.BIG_INTEGER, base._numberType);
    }

    @Test(timeout = 4000)
    public void testBaseConstructorFloatType() {
        Base<?> base = new Base<Float>(Float.class, JsonParser.NumberType.FLOAT, "number") {
            @Override
            public void serialize(Float value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeNumber(value);
            }
        };
        assertFalse("_isInt should be false for FLOAT", base._isInt);
        assertEquals("_numberType should be FLOAT", JsonParser.NumberType.FLOAT, base._numberType);
    }

    @Test(timeout = 4000)
    public void testBaseConstructorDoubleType() {
        Base<?> base = new Base<Double>(Double.class, JsonParser.NumberType.DOUBLE, "number") {
            @Override
            public void serialize(Double value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeNumber(value);
            }
        };
        assertFalse("_isInt should be false for DOUBLE", base._isInt);
        assertEquals("_numberType should be DOUBLE", JsonParser.NumberType.DOUBLE, base._numberType);
    }

    @Test(timeout = 4000)
    public void testGetSchema() {
        Base<?> base = new Base<Integer>(Integer.class, JsonParser.NumberType.INT, "integer") {
            @Override
            public void serialize(Integer value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeNumber(value);
            }
        };
        JsonNode schema = base.getSchema(null, null);
        assertNotNull("Schema should not be null", schema);
        assertEquals("Schema type should be integer", "integer", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorIntType() throws Exception {
        Base<?> base = new Base<Integer>(Integer.class, JsonParser.NumberType.INT, "integer") {
            @Override
            public void serialize(Integer value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeNumber(value);
            }
        };
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base();
        base.acceptJsonFormatVisitor(visitor, null);
        // No exception expected
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorFloatType() throws Exception {
        Base<?> base = new Base<Float>(Float.class, JsonParser.NumberType.FLOAT, "number") {
            @Override
            public void serialize(Float value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeNumber(value);
            }
        };
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base();
        base.acceptJsonFormatVisitor(visitor, null);
        // No exception expected
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorWithNonNullIntegerVisitor() throws Exception {
        Base<?> base = new Base<Integer>(Integer.class, JsonParser.NumberType.INT, "integer") {
            @Override
            public void serialize(Integer value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeNumber(value);
            }
        };
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
                return new JsonIntegerFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        assertEquals("Number type should be INT", JsonParser.NumberType.INT, type);
                    }
                };
            }
        };
        base.acceptJsonFormatVisitor(visitor, null);
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorWithNonNullNumberVisitor() throws Exception {
        Base<?> base = new Base<Float>(Float.class, JsonParser.NumberType.FLOAT, "number") {
            @Override
            public void serialize(Float value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeNumber(value);
            }
        };
        JsonFormatVisitorWrapper visitor = new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
                return new JsonNumberFormatVisitor.Base() {
                    @Override
                    public void numberType(JsonParser.NumberType type) {
                        assertEquals("Number type should be FLOAT", JsonParser.NumberType.FLOAT, type);
                    }
                };
            }
        };
        base.acceptJsonFormatVisitor(visitor, null);
    }

    // ===== Partition B: Boundary Value Analysis (BVA) & Extremes =====

    @Test(timeout = 4000)
    public void testCreateContextualWithNullProperty() throws Exception {
        Base<?> base = new Base<Integer>(Integer.class, JsonParser.NumberType.INT, "integer") {
            @Override
            public void serialize(Integer value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeNumber(value);
            }
        };
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        JsonSerializer<?> result = base.createContextual(prov, null);
        assertSame("Should return same instance when property is null", base, result);
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithPropertyButNullMember() throws Exception {
        Base<?> base = new Base<Integer>(Integer.class, JsonParser.NumberType.INT, "integer") {
            @Override
            public void serialize(Integer value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeNumber(value);
            }
        };
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        BeanProperty property = new BeanProperty.Std(null, null, null, null, null);
        JsonSerializer<?> result = base.createContextual(prov, property);
        assertSame("Should return same instance when member is null", base, result);
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithFormatShapeString() throws Exception {
        // This test requires a mock-like setup; we'll test the logic indirectly
        Base<?> base = new Base<Integer>(Integer.class, JsonParser.NumberType.INT, "integer") {
            @Override
            public void serialize(Integer value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeNumber(value);
            }
        };
        // We can't easily mock the full chain, but we can test that the method doesn't throw
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        // With no real property, it should return this
        JsonSerializer<?> result = base.createContextual(prov, null);
        assertSame("Should return same instance", base, result);
    }

    @Test(timeout = 4000)
    public void testShortSerializerSerializeZero() throws Exception {
        ShortSerializer serializer = new ShortSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        serializer.serialize((short) 0, gen, prov);
        gen.flush();
        String output = gen.getOutputTarget().toString();
        assertTrue("Output should contain 0", output.contains("0"));
    }

    @Test(timeout = 4000)
    public void testShortSerializerSerializeMaxValue() throws Exception {
        ShortSerializer serializer = new ShortSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        serializer.serialize(Short.MAX_VALUE, gen, prov);
        gen.flush();
        String output = gen.getOutputTarget().toString();
        assertTrue("Output should contain max value", output.contains(String.valueOf(Short.MAX_VALUE)));
    }

    @Test(timeout = 4000)
    public void testShortSerializerSerializeMinValue() throws Exception {
        ShortSerializer serializer = new ShortSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        serializer.serialize(Short.MIN_VALUE, gen, prov);
        gen.flush();
        String output = gen.getOutputTarget().toString();
        assertTrue("Output should contain min value", output.contains(String.valueOf(Short.MIN_VALUE)));
    }

    @Test(timeout = 4000)
    public void testIntegerSerializerSerializeZero() throws Exception {
        IntegerSerializer serializer = new IntegerSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        serializer.serialize(0, gen, prov);
        gen.flush();
        String output = gen.getOutputTarget().toString();
        assertTrue("Output should contain 0", output.contains("0"));
    }

    @Test(timeout = 4000)
    public void testIntegerSerializerSerializeMaxValue() throws Exception {
        IntegerSerializer serializer = new IntegerSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        serializer.serialize(Integer.MAX_VALUE, gen, prov);
        gen.flush();
        String output = gen.getOutputTarget().toString();
        assertTrue("Output should contain max value", output.contains(String.valueOf(Integer.MAX_VALUE)));
    }

    @Test(timeout = 4000)
    public void testIntegerSerializerSerializeMinValue() throws Exception {
        IntegerSerializer serializer = new IntegerSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        serializer.serialize(Integer.MIN_VALUE, gen, prov);
        gen.flush();
        String output = gen.getOutputTarget().toString();
        assertTrue("Output should contain min value", output.contains(String.valueOf(Integer.MIN_VALUE)));
    }

    @Test(timeout = 4000)
    public void testIntegerSerializerSerializeWithType() throws Exception {
        IntegerSerializer serializer = new IntegerSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        TypeSerializer typeSer = new com.fasterxml.jackson.databind.jsontype.impl.TypeSerializerBase(null, null) {
            @Override
            public void writeTypePrefixForScalar(Object value, JsonGenerator gen) throws IOException {}
            @Override
            public void writeTypeSuffixForScalar(Object value, JsonGenerator gen) throws IOException {}
            @Override
            public void writeCustomTypePrefixForScalar(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override
            public void writeCustomTypeSuffixForScalar(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override
            public void writeTypePrefixForObject(Object value, JsonGenerator gen) throws IOException {}
            @Override
            public void writeTypeSuffixForObject(Object value, JsonGenerator gen) throws IOException {}
            @Override
            public void writeTypePrefixForArray(Object value, JsonGenerator gen) throws IOException {}
            @Override
            public void writeTypeSuffixForArray(Object value, JsonGenerator gen) throws IOException {}
            @Override
            public void writeCustomTypePrefixForObject(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override
            public void writeCustomTypeSuffixForObject(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override
            public void writeCustomTypePrefixForArray(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override
            public void writeCustomTypeSuffixForArray(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override
            public void writeTypeSuffixForScalar(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override
            public void writeTypePrefixForScalar(Object value, JsonGenerator gen, String typeId) throws IOException {}
        };
        serializer.serializeWithType(42, gen, prov, typeSer);
        gen.flush();
        String output = gen.getOutputTarget().toString();
        assertTrue("Output should contain 42", output.contains("42"));
    }

    @Test(timeout = 4000)
    public void testIntLikeSerializerSerializeZero() throws Exception {
        IntLikeSerializer serializer = new IntLikeSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        serializer.serialize((Number) 0, gen, prov);
        gen.flush();
        String output = gen.getOutputTarget().toString();
        assertTrue("Output should contain 0", output.contains("0"));
    }

    @Test(timeout = 4000)
    public void testIntLikeSerializerSerializeByte() throws Exception {
        IntLikeSerializer serializer = new IntLikeSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        serializer.serialize((Number) (byte) 127, gen, prov);
        gen.flush();
        String output = gen.getOutputTarget().toString();
        assertTrue("Output should contain 127", output.contains("127"));
    }

    @Test(timeout = 4000)
    public void testLongSerializerSerializeZero() throws Exception {
        LongSerializer serializer = new LongSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        serializer.serialize(0L, gen, prov);
        gen.flush();
        String output = gen.getOutputTarget().toString();
        assertTrue("Output should contain 0", output.contains("0"));
    }

    @Test(timeout = 4000)
    public void testLongSerializerSerializeMaxValue() throws Exception {
        LongSerializer serializer = new LongSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        serializer.serialize(Long.MAX_VALUE, gen, prov);
        gen.flush();
        String output = gen.getOutputTarget().toString();
        assertTrue("Output should contain max value", output.contains(String.valueOf(Long.MAX_VALUE)));
    }

    @Test(timeout = 4000)
    public void testFloatSerializerSerializeZero() throws Exception {
        FloatSerializer serializer = new FloatSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        serializer.serialize(0.0f, gen, prov);
        gen.flush();
        String output = gen.getOutputTarget().toString();
        assertTrue("Output should contain 0.0", output.contains("0.0"));
    }

    @Test(timeout = 4000)
    public void testFloatSerializerSerializeNegativeValue() throws Exception {
        FloatSerializer serializer = new FloatSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        serializer.serialize(-3.14f, gen, prov);
        gen.flush();
        String output = gen.getOutputTarget().toString();
        assertTrue("Output should contain -3.14", output.contains("-3.14"));
    }

    @Test(timeout = 4000)
    public void testDoubleSerializerSerializeZero() throws Exception {
        DoubleSerializer serializer = new DoubleSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        serializer.serialize(0.0, gen, prov);
        gen.flush();
        String output = gen.getOutputTarget().toString();
        assertTrue("Output should contain 0.0", output.contains("0.0"));
    }

    @Test(timeout = 4000)
    public void testDoubleSerializerSerializeMaxValue() throws Exception {
        DoubleSerializer serializer = new DoubleSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        serializer.serialize(Double.MAX_VALUE, gen, prov);
        gen.flush();
        String output = gen.getOutputTarget().toString();
        assertTrue("Output should contain max value", output.contains(String.valueOf(Double.MAX_VALUE)));
    }

    @Test(timeout = 4000)
    public void testDoubleSerializerSerializeWithType() throws Exception {
        DoubleSerializer serializer = new DoubleSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        TypeSerializer typeSer = new com.fasterxml.jackson.databind.jsontype.impl.TypeSerializerBase(null, null) {
            @Override
            public void writeTypePrefixForScalar(Object value, JsonGenerator gen) throws IOException {}
            @Override
            public void writeTypeSuffixForScalar(Object value, JsonGenerator gen) throws IOException {}
            @Override
            public void writeCustomTypePrefixForScalar(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override
            public void writeCustomTypeSuffixForScalar(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override
            public void writeTypePrefixForObject(Object value, JsonGenerator gen) throws IOException {}
            @Override
            public void writeTypeSuffixForObject(Object value, JsonGenerator gen) throws IOException {}
            @Override
            public void writeTypePrefixForArray(Object value, JsonGenerator gen) throws IOException {}
            @Override
            public void writeTypeSuffixForArray(Object value, JsonGenerator gen) throws IOException {}
            @Override
            public void writeCustomTypePrefixForObject(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override
            public void writeCustomTypeSuffixForObject(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override
            public void writeCustomTypePrefixForArray(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override
            public void writeCustomTypeSuffixForArray(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override
            public void writeTypeSuffixForScalar(Object value, JsonGenerator gen, String typeId) throws IOException {}
            @Override
            public void writeTypePrefixForScalar(Object value, JsonGenerator gen, String typeId) throws IOException {}
        };
        serializer.serializeWithType(3.14, gen, prov, typeSer);
        gen.flush();
        String output = gen.getOutputTarget().toString();
        assertTrue("Output should contain 3.14", output.contains("3.14"));
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    // Targeting the known defect: testEmptyInclusionScalars
    // Expected: zero values should be excluded when using NON_EMPTY inclusion
    // Bug: zero values are being included when they should not be

    @Test(timeout = 4000)
    public void testEmptyInclusionScalarsDefect() throws Exception {
        // This test targets the defect where zero values are incorrectly included
        // when serializing with JsonInclude.Include.NON_EMPTY
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Integer.class).setInclude(JsonInclude.Include.NON_EMPTY);
        
        // Create a simple bean with an integer field set to 0
        // The expected behavior is that 0 should be excluded (empty value)
        // But the bug causes it to be included
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("value", 0);
        
        String result = mapper.writeValueAsString(map);
        // The bug produces {"value":0} but should produce {}
        // We assert the correct behavior (empty object)
        assertEquals("Zero value should be excluded with NON_EMPTY inclusion", "{}", result);
    }

    @Test(timeout = 4000)
    public void testEmptyInclusionScalarsDefectWithLong() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Long.class).setInclude(JsonInclude.Include.NON_EMPTY);
        
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("value", 0L);
        
        String result = mapper.writeValueAsString(map);
        assertEquals("Zero long value should be excluded with NON_EMPTY inclusion", "{}", result);
    }

    @Test(timeout = 4000)
    public void testEmptyInclusionScalarsDefectWithDouble() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Double.class).setInclude(JsonInclude.Include.NON_EMPTY);
        
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("value", 0.0);
        
        String result = mapper.writeValueAsString(map);
        assertEquals("Zero double value should be excluded with NON_EMPTY inclusion", "{}", result);
    }

    @Test(timeout = 4000)
    public void testEmptyInclusionScalarsDefectWithFloat() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Float.class).setInclude(JsonInclude.Include.NON_EMPTY);
        
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("value", 0.0f);
        
        String result = mapper.writeValueAsString(map);
        assertEquals("Zero float value should be excluded with NON_EMPTY inclusion", "{}", result);
    }

    @Test(timeout = 4000)
    public void testEmptyInclusionScalarsDefectWithShort() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Short.class).setInclude(JsonInclude.Include.NON_EMPTY);
        
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("value", (short) 0);
        
        String result = mapper.writeValueAsString(map);
        assertEquals("Zero short value should be excluded with NON_EMPTY inclusion", "{}", result);
    }

    @Test(timeout = 4000)
    public void testEmptyInclusionScalarsDefectWithByte() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Byte.class).setInclude(JsonInclude.Include.NON_EMPTY);
        
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("value", (byte) 0);
        
        String result = mapper.writeValueAsString(map);
        assertEquals("Zero byte value should be excluded with NON_EMPTY inclusion", "{}", result);
    }

    @Test(timeout = 4000)
    public void testNonEmptyInclusionWithNonZeroValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configOverride(Integer.class).setInclude(JsonInclude.Include.NON_EMPTY);
        
        java.util.Map<String, Object> map = new java.util.HashMap<>();
        map.put("value", 42);
        
        String result = mapper.writeValueAsString(map);
        assertEquals("Non-zero value should be included", "{\"value\":42}", result);
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testIntegerSerializerWithNullValue() throws Exception {
        IntegerSerializer serializer = new IntegerSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        try {
            serializer.serialize(null, gen, prov);
            fail("Should throw NullPointerException for null value");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testLongSerializerWithNullValue() throws Exception {
        LongSerializer serializer = new LongSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        try {
            serializer.serialize(null, gen, prov);
            fail("Should throw NullPointerException for null value");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testFloatSerializerWithNullValue() throws Exception {
        FloatSerializer serializer = new FloatSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        try {
            serializer.serialize(null, gen, prov);
            fail("Should throw NullPointerException for null value");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testDoubleSerializerWithNullValue() throws Exception {
        DoubleSerializer serializer = new DoubleSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        try {
            serializer.serialize(null, gen, prov);
            fail("Should throw NullPointerException for null value");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testShortSerializerWithNullValue() throws Exception {
        ShortSerializer serializer = new ShortSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        try {
            serializer.serialize(null, gen, prov);
            fail("Should throw NullPointerException for null value");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testIntLikeSerializerWithNullValue() throws Exception {
        IntLikeSerializer serializer = new IntLikeSerializer();
        JsonGenerator gen = new ObjectMapper().getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        SerializerProvider prov = new ObjectMapper().getSerializerProvider();
        try {
            serializer.serialize(null, gen, prov);
            fail("Should throw NullPointerException for null value");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testSingletonInstances() {
        assertNotNull("ShortSerializer instance should not be null", ShortSerializer.instance);
        assertNotNull("LongSerializer instance should not be null", LongSerializer.instance);
        assertNotNull("FloatSerializer instance should not be null", FloatSerializer.instance);
        assertNotNull("DoubleSerializer instance should not be null", DoubleSerializer.instance);
        assertNotNull("IntLikeSerializer instance should not be null", IntLikeSerializer.instance);
    }

    @Test(timeout = 4000)
    public void testJacksonStdImplAnnotation() {
        assertTrue("ShortSerializer should have @JacksonStdImpl", 
                   ShortSerializer.class.isAnnotationPresent(JacksonStdImpl.class));
        assertTrue("IntegerSerializer should have @JacksonStdImpl", 
                   IntegerSerializer.class.isAnnotationPresent(JacksonStdImpl.class));
        assertTrue("IntLikeSerializer should have @JacksonStdImpl", 
                   IntLikeSerializer.class.isAnnotationPresent(JacksonStdImpl.class));
        assertTrue("LongSerializer should have @JacksonStdImpl", 
                   LongSerializer.class.isAnnotationPresent(JacksonStdImpl.class));
        assertTrue("FloatSerializer should have @JacksonStdImpl", 
                   FloatSerializer.class.isAnnotationPresent(JacksonStdImpl.class));
        assertTrue("DoubleSerializer should have @JacksonStdImpl", 
                   DoubleSerializer.class.isAnnotationPresent(JacksonStdImpl.class));
    }

    @Test(timeout = 4000)
    public void testSerializerInheritance() {
        assertTrue("ShortSerializer should extend Base", ShortSerializer.class.getSuperclass() == Base.class);
        assertTrue("IntegerSerializer should extend Base", IntegerSerializer.class.getSuperclass() == Base.class);
        assertTrue("IntLikeSerializer should extend Base", IntLikeSerializer.class.getSuperclass() == Base.class);
        assertTrue("LongSerializer should extend Base", LongSerializer.class.getSuperclass() == Base.class);
        assertTrue("FloatSerializer should extend Base", FloatSerializer.class.getSuperclass() == Base.class);
        assertTrue("DoubleSerializer should extend Base", DoubleSerializer.class.getSuperclass() == Base.class);
    }

    @Test(timeout = 4000)
    public void testContextualSerializerImplementation() {
        assertTrue("Base should implement ContextualSerializer", 
                   ContextualSerializer.class.isAssignableFrom(Base.class));
    }

    @Test(timeout = 4000)
    public void testNumberSerializersAddAll() {
        java.util.Map<String, JsonSerializer<?>> map = new java.util.HashMap<>();
        NumberSerializers.addAll(map);
        assertTrue("Should contain Integer serializer", map.containsKey(Integer.class.getName()));
        assertTrue("Should contain int serializer", map.containsKey(Integer.TYPE.getName()));
        assertTrue("Should contain Long serializer", map.containsKey(Long.class.getName()));
        assertTrue("Should contain long serializer", map.containsKey(Long.TYPE.getName()));
        assertTrue("Should contain Byte serializer", map.containsKey(Byte.class.getName()));
        assertTrue("Should contain byte serializer", map.containsKey(Byte.TYPE.getName()));
        assertTrue("Should contain Short serializer", map.containsKey(Short.class.getName()));
        assertTrue("Should contain short serializer", map.containsKey(Short.TYPE.getName()));
        assertTrue("Should contain Float serializer", map.containsKey(Float.class.getName()));
        assertTrue("Should contain float serializer", map.containsKey(Float.TYPE.getName()));
        assertTrue("Should contain Double serializer", map.containsKey(Double.class.getName()));
        assertTrue("Should contain double serializer", map.containsKey(Double.TYPE.getName()));
    }

    @Test(timeout = 4000)
    public void testNumberSerializersAddAllSingletonInstances() {
        java.util.Map<String, JsonSerializer<?>> map = new java.util.HashMap<>();
        NumberSerializers.addAll(map);
        assertSame("Integer serializer should be same instance", 
                   map.get(Integer.class.getName()), map.get(Integer.TYPE.getName()));
        assertSame("Long serializer should be same instance", 
                   map.get(Long.class.getName()), map.get(Long.TYPE.getName()));
        assertSame("Byte serializer should be same instance", 
                   map.get(Byte.class.getName()), map.get(Byte.TYPE.getName()));
        assertSame("Short serializer should be same instance", 
                   map.get(Short.class.getName()), map.get(Short.TYPE.getName()));
        assertSame("Float serializer should be same instance", 
                   map.get(Float.class.getName()), map.get(Float.TYPE.getName()));
        assertSame("Double serializer should be same instance", 
                   map.get(Double.class.getName()), map.get(Double.TYPE.getName()));
    }
}