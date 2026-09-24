package com.fasterxml.jackson.databind.ser.std;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyMetadata;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.ser.std.NumberSerializers
 *
 * Decision / Condition Branch Analysis:
 * 1. Base._isInt flag evaluation:
 *    - True for INT, LONG, BIG_INTEGER -> directs to visitIntFormat in acceptJsonFormatVisitor.
 *    - False for FLOAT, DOUBLE, etc. -> directs to visitFloatFormat in acceptJsonFormatVisitor.
 * 2. Base.createContextual branches:
 *    - format == null -> returns this
 *    - format != null, format.getShape() == Shape.STRING -> returns ToStringSerializer.instance
 *    - format != null, format.getShape() != Shape.STRING -> returns this
 * 3. Specific Concrete Serializers serialize & serializeWithType:
 *    - ShortSerializer: shortValue cast and generation
 *    - IntegerSerializer: intValue cast, custom serializeWithType ignores type serializer wrapper
 *    - IntLikeSerializer: Number.intValue() conversion
 *    - LongSerializer: longValue cast and generation
 *    - FloatSerializer: floatValue cast and generation
 *    - DoubleSerializer: doubleValue cast, custom serializeWithType ignores type serializer wrapper
 * 4. NumberSerializers.addAll:
 *    - Registration of wrapper and primitive keys for Integer, Long, Byte, Short, Double, Float.
 *
 * Known Defect Ground Truth:
 * - Defects4J issue where Shape.STRING formatting or plain formatting overrides standard numeric
 *   serialization behavior (e.g. BigDecimalPlain2230Test / WRITE_BIG_DECIMAL_AS_PLAIN with STRING shape).
 */
public class NumberSerializersGeminiTest {

    // Helper recording visitor for format inspection without mocking frameworks
    private static class RecordingFormatVisitor extends JsonFormatVisitorWrapper.Base {
        boolean visitedInt = false;
        boolean visitedFloat = false;
        JsonParser.NumberType recordedNumberType = null;

        @Override
        public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
            visitedInt = true;
            return new JsonIntegerFormatVisitor.Base() {
                @Override
                public void numberType(JsonParser.NumberType type) {
                    recordedNumberType = type;
                }
            };
        }

        @Override
        public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
            visitedFloat = true;
            return new JsonNumberFormatVisitor.Base() {
                @Override
                public void numberType(JsonParser.NumberType type) {
                    recordedNumberType = type;
                }
            };
        }
    }

    // Dummy BeanProperty that returns specific JsonFormat configurations
    private static class DummyProperty extends BeanProperty.Std {
        private final JsonFormat.Value _format;

        public DummyProperty(String name, JavaType type, JsonFormat.Value format) {
            super(PropertyName.construct(name), type, null, null, PropertyMetadata.STD_REQUIRED);
            _format = format;
        }

        @Override
        public JsonFormat.Value findPropertyFormat(SerializationConfig config, Class<?> baseType) {
            return _format;
        }
    }

    // Dummy BeanProperty that delegates to annotations on a member
    private static class AnnotatedProperty extends BeanProperty.Std {
        public AnnotatedProperty(PropertyName name, JavaType type, AnnotatedMember member) {
            super(name, type, null, member, PropertyMetadata.STD_REQUIRED);
        }
    }

    // Bean targeting defect condition with plain BigDecimal and STRING shape
    static class BigDecimalWrapper {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public BigDecimal value;

        public BigDecimalWrapper(BigDecimal v) {
            this.value = v;
        }
    }

    // POJOs for contextual shape annotation tests
    static class StringShapeShortBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public short val = 42;
    }

    static class StringShapeIntBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int val = 12345;
    }

    static class StringShapeLongBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public long val = 9876543210L;
    }

    static class StringShapeDoubleBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public double val = 3.14159;
    }

    static class StringShapeFloatBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public float val = 2.718f;
    }

    static class PolymorphicNumberHolder {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY)
        public Object value;

        public PolymorphicNumberHolder(Object v) {
            this.value = v;
        }
    }

    /*
     * ----------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testNumberSerializersConstructor() {
        NumberSerializers serializers = new NumberSerializers();
        assertNotNull("Instance of NumberSerializers should be creatable", serializers);
    }

    @Test(timeout = 4000)
    public void testAddAllRegistersAllPrimitivesAndWrappers() {
        Map<String, JsonSerializer<?>> map = new HashMap<String, JsonSerializer<?>>();
        NumberSerializers.addAll(map);

        assertEquals(12, map.size());
        assertTrue(map.get(Integer.class.getName()) instanceof NumberSerializers.IntegerSerializer);
        assertTrue(map.get(Integer.TYPE.getName()) instanceof NumberSerializers.IntegerSerializer);
        assertTrue(map.get(Long.class.getName()) instanceof NumberSerializers.LongSerializer);
        assertTrue(map.get(Long.TYPE.getName()) instanceof NumberSerializers.LongSerializer);
        assertSame(NumberSerializers.IntLikeSerializer.instance, map.get(Byte.class.getName()));
        assertSame(NumberSerializers.IntLikeSerializer.instance, map.get(Byte.TYPE.getName()));
        assertSame(NumberSerializers.ShortSerializer.instance, map.get(Short.class.getName()));
        assertSame(NumberSerializers.ShortSerializer.instance, map.get(Short.TYPE.getName()));
        assertTrue(map.get(Double.class.getName()) instanceof NumberSerializers.DoubleSerializer);
        assertTrue(map.get(Double.TYPE.getName()) instanceof NumberSerializers.DoubleSerializer);
        assertSame(NumberSerializers.FloatSerializer.instance, map.get(Float.class.getName()));
        assertSame(NumberSerializers.FloatSerializer.instance, map.get(Float.TYPE.getName()));
    }

    @Test(timeout = 4000)
    public void testShortSerializerDirectSerialization() throws IOException {
        NumberSerializers.ShortSerializer ser = NumberSerializers.ShortSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        ser.serialize((short) 32767, gen, prov);
        gen.flush();
        assertEquals("32767", sw.toString());

        JsonNode schema = ser.getSchema(prov, Short.class);
        assertEquals("number", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testIntegerSerializerDirectSerialization() throws IOException {
        NumberSerializers.IntegerSerializer ser = new NumberSerializers.IntegerSerializer(Integer.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        ser.serialize(2147483647, gen, prov);
        gen.flush();
        assertEquals("2147483647", sw.toString());

        JsonNode schema = ser.getSchema(prov, Integer.class);
        assertEquals("integer", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testIntLikeSerializerDirectSerialization() throws IOException {
        NumberSerializers.IntLikeSerializer ser = NumberSerializers.IntLikeSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        ser.serialize((byte) 127, gen, prov);
        gen.flush();
        assertEquals("127", sw.toString());

        JsonNode schema = ser.getSchema(prov, Byte.class);
        assertEquals("integer", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testLongSerializerDirectSerialization() throws IOException {
        NumberSerializers.LongSerializer ser = new NumberSerializers.LongSerializer(Long.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        ser.serialize(9223372036854775807L, gen, prov);
        gen.flush();
        assertEquals("9223372036854775807", sw.toString());

        JsonNode schema = ser.getSchema(prov, Long.class);
        assertEquals("number", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testFloatSerializerDirectSerialization() throws IOException {
        NumberSerializers.FloatSerializer ser = NumberSerializers.FloatSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        ser.serialize(1.5f, gen, prov);
        gen.flush();
        assertEquals("1.5", sw.toString());

        JsonNode schema = ser.getSchema(prov, Float.class);
        assertEquals("number", schema.get("type").asText());
    }

    @Test(timeout = 4000)
    public void testDoubleSerializerDirectSerialization() throws IOException {
        NumberSerializers.DoubleSerializer ser = new NumberSerializers.DoubleSerializer(Double.class);
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        ser.serialize(12345.6789, gen, prov);
        gen.flush();
        assertEquals("12345.6789", sw.toString());

        JsonNode schema = ser.getSchema(prov, Double.class);
        assertEquals("number", schema.get("type").asText());
    }

    /*
     * ----------------------------------------------------------------------
     * Partition B: Boundary Value Analysis & Format Visitors
     * ----------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testVisitorForIntTypes() throws Exception {
        JavaType intType = TypeFactory.defaultInstance().constructType(Integer.class);
        RecordingFormatVisitor visitor = new RecordingFormatVisitor();

        NumberSerializers.IntegerSerializer intSer = new NumberSerializers.IntegerSerializer(Integer.class);
        intSer.acceptJsonFormatVisitor(visitor, intType);
        assertTrue(visitor.visitedInt);
        assertFalse(visitor.visitedFloat);
        assertEquals(JsonParser.NumberType.INT, visitor.recordedNumberType);

        visitor = new RecordingFormatVisitor();
        NumberSerializers.LongSerializer longSer = new NumberSerializers.LongSerializer(Long.class);
        longSer.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Long.class));
        assertTrue(visitor.visitedInt);
        assertFalse(visitor.visitedFloat);
        assertEquals(JsonParser.NumberType.LONG, visitor.recordedNumberType);

        visitor = new RecordingFormatVisitor();
        NumberSerializers.ShortSerializer shortSer = NumberSerializers.ShortSerializer.instance;
        shortSer.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Short.class));
        assertTrue(visitor.visitedInt);
        assertFalse(visitor.visitedFloat);
        assertEquals(JsonParser.NumberType.INT, visitor.recordedNumberType);

        visitor = new RecordingFormatVisitor();
        NumberSerializers.IntLikeSerializer intLikeSer = NumberSerializers.IntLikeSerializer.instance;
        intLikeSer.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Byte.class));
        assertTrue(visitor.visitedInt);
        assertFalse(visitor.visitedFloat);
        assertEquals(JsonParser.NumberType.INT, visitor.recordedNumberType);
    }

    @Test(timeout = 4000)
    public void testVisitorForFloatTypes() throws Exception {
        RecordingFormatVisitor visitor = new RecordingFormatVisitor();
        NumberSerializers.FloatSerializer floatSer = NumberSerializers.FloatSerializer.instance;
        floatSer.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Float.class));
        assertFalse(visitor.visitedInt);
        assertTrue(visitor.visitedFloat);
        assertEquals(JsonParser.NumberType.FLOAT, visitor.recordedNumberType);

        visitor = new RecordingFormatVisitor();
        NumberSerializers.DoubleSerializer doubleSer = new NumberSerializers.DoubleSerializer(Double.class);
        doubleSer.acceptJsonFormatVisitor(visitor, TypeFactory.defaultInstance().constructType(Double.class));
        assertFalse(visitor.visitedInt);
        assertTrue(visitor.visitedFloat);
        assertEquals(JsonParser.NumberType.DOUBLE, visitor.recordedNumberType);
    }

    @Test(timeout = 4000)
    public void testBoundaryNumericValues() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        assertEquals(String.valueOf(Short.MIN_VALUE), mapper.writeValueAsString(Short.MIN_VALUE));
        assertEquals(String.valueOf(Short.MAX_VALUE), mapper.writeValueAsString(Short.MAX_VALUE));
        assertEquals(String.valueOf(Integer.MIN_VALUE), mapper.writeValueAsString(Integer.MIN_VALUE));
        assertEquals(String.valueOf(Integer.MAX_VALUE), mapper.writeValueAsString(Integer.MAX_VALUE));
        assertEquals(String.valueOf(Long.MIN_VALUE), mapper.writeValueAsString(Long.MIN_VALUE));
        assertEquals(String.valueOf(Long.MAX_VALUE), mapper.writeValueAsString(Long.MAX_VALUE));
        assertEquals(String.valueOf(Byte.MIN_VALUE), mapper.writeValueAsString(Byte.MIN_VALUE));
        assertEquals