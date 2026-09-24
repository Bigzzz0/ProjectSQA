package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonIntegerFormatVisitor;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonNumberFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: com.fasterxml.jackson.databind.ser.std.NumberSerializers
 *
 * 1. Base Class Constructor Branching (_isInt determination):
 *    - numberType == INT: _isInt = true (IntegerSerializer, ShortSerializer, IntLikeSerializer)
 *    - numberType == LONG: _isInt = true (LongSerializer)
 *    - numberType == BIG_INTEGER: _isInt = true (custom Base subclass test)
 *    - numberType == FLOAT / DOUBLE / other: _isInt = false (FloatSerializer, DoubleSerializer)
 *
 * 2. acceptJsonFormatVisitor Branching:
 *    - _isInt == true -> visitor.expectIntegerFormat(typeHint)
 *        - child visitor != null -> v2.numberType(_numberType)
 *        - child visitor == null -> no-op / safe execution
 *    - _isInt == false -> visitor.expectNumberFormat(typeHint)
 *        - child visitor != null -> v2.numberType(_numberType)
 *        - child visitor == null -> no-op / safe execution
 *
 * 3. createContextual Branching:
 *    - property == null -> return this
 *    - property != null, property.getMember() == null -> return this
 *    - property.getMember() != null, findFormat() == null -> return this
 *    - findFormat().getShape() == JsonFormat.Shape.STRING -> return ToStringSerializer.instance
 *    - findFormat().getShape() != JsonFormat.Shape.STRING (e.g. NUMBER, ANY) -> return this
 *
 * 4. Serialization & serializeWithType:
 *    - ShortSerializer: shortValue() output
 *    - IntegerSerializer: intValue() direct and serializeWithType() delegating without type wrapper
 *    - IntLikeSerializer: Number.intValue() output for Byte / Short / Number types
 *    - LongSerializer: longValue() output
 *    - FloatSerializer: floatValue() output
 *    - DoubleSerializer: doubleValue() direct and serializeWithType() delegating without type wrapper
 *
 * 5. Defect-Targeted Ground Truth (Defects4J testEmptyInclusionScalars):
 *    - Empty scalar inclusion: @JsonInclude(Include.NON_EMPTY) on numerical scalars (e.g. int value = 0).
 *    - Defects4J failure condition: expected <{}> but produced <{"value":0}> when 0 is serialized.
 */
public class NumberSerializersGeminiTest {

    private final ObjectMapper mapper = new ObjectMapper();
    private final JsonFactory jsonFactory = new JsonFactory();

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddAllPopulatesExpectedMappings() {
        Map<String, JsonSerializer<?>> map = new HashMap<String, JsonSerializer<?>>();
        NumberSerializers.addAll(map);

        assertEquals(12, map.size());

        assertTrue(map.get(Integer.class.getName()) instanceof NumberSerializers.IntegerSerializer);
        assertTrue(map.get(Integer.TYPE.getName()) instanceof NumberSerializers.IntegerSerializer);

        assertSame(NumberSerializers.LongSerializer.instance, map.get(Long.class.getName()));
        assertSame(NumberSerializers.LongSerializer.instance, map.get(Long.TYPE.getName()));

        assertSame(NumberSerializers.IntLikeSerializer.instance, map.get(Byte.class.getName()));
        assertSame(NumberSerializers.IntLikeSerializer.instance, map.get(Byte.TYPE.getName()));

        assertSame(NumberSerializers.ShortSerializer.instance, map.get(Short.class.getName()));
        assertSame(NumberSerializers.ShortSerializer.instance, map.get(Short.TYPE.getName()));

        assertSame(NumberSerializers.FloatSerializer.instance, map.get(Float.class.getName()));
        assertSame(NumberSerializers.FloatSerializer.instance, map.get(Float.TYPE.getName()));

        assertSame(NumberSerializers.DoubleSerializer.instance, map.get(Double.class.getName()));
        assertSame(NumberSerializers.DoubleSerializer.instance, map.get(Double.TYPE.getName()));
    }

    @Test(timeout = 4000)
    public void testGetSchemaForAllConcreteSerializers() throws Exception {
        SerializerProvider provider = mapper.getSerializerProviderInstance();

        NumberSerializers.ShortSerializer shortSer = new NumberSerializers.ShortSerializer();
        JsonNode shortSchema = shortSer.getSchema(provider, Short.TYPE);
        assertEquals("number", shortSchema.get("type").asText());
        assertTrue(shortSchema.get("required").asBoolean());

        NumberSerializers.IntegerSerializer intSer = new NumberSerializers.IntegerSerializer();
        JsonNode intSchema = intSer.getSchema(provider, Integer.TYPE);
        assertEquals("integer", intSchema.get("type").asText());
        assertTrue(intSchema.get("required").asBoolean());

        NumberSerializers.IntLikeSerializer intLikeSer = NumberSerializers.IntLikeSerializer.instance;
        JsonNode intLikeSchema = intLikeSer.getSchema(provider, Byte.TYPE);
        assertEquals("integer", intLikeSchema.get("type").asText());
        assertTrue(intLikeSchema.get("required").asBoolean());

        NumberSerializers.LongSerializer longSer = NumberSerializers.LongSerializer.instance;
        JsonNode longSchema = longSer.getSchema(provider, Long.TYPE);
        assertEquals("number", longSchema.get("type").asText());
        assertTrue(longSchema.get("required").asBoolean());

        NumberSerializers.FloatSerializer floatSer = NumberSerializers.FloatSerializer.instance;
        JsonNode floatSchema = floatSer.getSchema(provider, Float.TYPE);
        assertEquals("number", floatSchema.get("type").asText());
        assertTrue(floatSchema.get("required").asBoolean());

        NumberSerializers.DoubleSerializer doubleSer = NumberSerializers.DoubleSerializer.instance;
        JsonNode doubleSchema = doubleSer.getSchema(provider, Double.TYPE);
        assertEquals("number", doubleSchema.get("type").asText());
        assertTrue(doubleSchema.get("required").asBoolean());
    }

    @Test(timeout = 4000)
    public void testSerializeShort() throws Exception {
        NumberSerializers.ShortSerializer serializer = new NumberSerializers.ShortSerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        serializer.serialize((short) 42, gen, prov);
        gen.flush();
        assertEquals("42", sw.toString());

        sw.getBuffer().setLength(0);
        serializer.serialize(Short.MIN_VALUE, gen, prov);
        gen.flush();
        assertEquals(String.valueOf(Short.MIN_VALUE), sw.toString());

        sw.getBuffer().setLength(0);
        serializer.serialize(Short.MAX_VALUE, gen, prov);
        gen.flush();
        assertEquals(String.valueOf(Short.MAX_VALUE), sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeIntegerAndSerializeWithType() throws Exception {
        NumberSerializers.IntegerSerializer serializer = new NumberSerializers.IntegerSerializer();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        serializer.serialize(1234, gen, prov);
        gen.flush();
        assertEquals("1234", sw.toString());

        sw.getBuffer().setLength(0);
        // serializeWithType should behave as normal serialization without type metadata
        serializer.serializeWithType(Integer.MAX_VALUE, gen, prov, null);
        gen.flush();
        assertEquals(String.valueOf(Integer.MAX_VALUE), sw.toString());

        sw.getBuffer().setLength(0);
        serializer.serialize(Integer.MIN_VALUE, gen, prov);
        gen.flush();
        assertEquals(String.valueOf(Integer.MIN_VALUE), sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeIntLike() throws Exception {
        NumberSerializers.IntLikeSerializer serializer = NumberSerializers.IntLikeSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        serializer.serialize((byte) 7, gen, prov);
        gen.flush();
        assertEquals("7", sw.toString());

        sw.getBuffer().setLength(0);
        serializer.serialize(BigDecimal.valueOf(999), gen, prov);
        gen.flush();
        assertEquals("999", sw.toString());

        sw.getBuffer().setLength(0);
        serializer.serialize(Byte.MIN_VALUE, gen, prov);
        gen.flush();
        assertEquals(String.valueOf(Byte.MIN_VALUE), sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeLong() throws Exception {
        NumberSerializers.LongSerializer serializer = NumberSerializers.LongSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        serializer.serialize(1234567890123L, gen, prov);
        gen.flush();
        assertEquals("1234567890123", sw.toString());

        sw.getBuffer().setLength(0);
        serializer.serialize(Long.MIN_VALUE, gen, prov);
        gen.flush();
        assertEquals(String.valueOf(Long.MIN_VALUE), sw.toString());

        sw.getBuffer().setLength(0);
        serializer.serialize(Long.MAX_VALUE, gen, prov);
        gen.flush();
        assertEquals(String.valueOf(Long.MAX_VALUE), sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeFloat() throws Exception {
        NumberSerializers.FloatSerializer serializer = NumberSerializers.FloatSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        serializer.serialize(3.25f, gen, prov);
        gen.flush();
        assertEquals("3.25", sw.toString());

        sw.getBuffer().setLength(0);
        serializer.serialize(-0.5f, gen, prov);
        gen.flush();
        assertEquals("-0.5", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeDoubleAndSerializeWithType() throws Exception {
        NumberSerializers.DoubleSerializer serializer = NumberSerializers.DoubleSerializer.instance;
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        serializer.serialize(123.456d, gen, prov);
        gen.flush();
        assertEquals("123.456", sw.toString());

        sw.getBuffer().setLength(0);
        serializer.serializeWithType(Double.valueOf(789.012d), gen, prov, null);
        gen.flush();
        assertEquals("789.012", sw.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Format Visitor Branching
    // =========================================================================

    private static class MockIntegerVisitor implements JsonIntegerFormatVisitor {
        JsonParser.NumberType recordedType;
        @Override
        public void numberType(JsonParser.NumberType type) {
            this.recordedType = type;
        }
    }

    private static class MockNumberVisitor implements JsonNumberFormatVisitor {
        JsonParser.NumberType recordedType;
        @Override
        public void numberType(JsonParser.NumberType type) {
            this.recordedType = type;
        }
    }

    private static class MockFormatVisitorWrapper extends JsonFormatVisitorWrapper.Base {
        final MockIntegerVisitor intVisitor;
        final MockNumberVisitor numVisitor;
        final boolean returnNullChildren;

        MockFormatVisitorWrapper(MockIntegerVisitor intVisitor, MockNumberVisitor numVisitor, boolean returnNullChildren) {
            this.intVisitor = intVisitor;
            this.numVisitor = numVisitor;
            this.returnNullChildren = returnNullChildren;
        }

        @Override
        public JsonIntegerFormatVisitor expectIntegerFormat(JavaType type) {
            return returnNullChildren ? null : intVisitor;
        }

        @Override
        public JsonNumberFormatVisitor expectNumberFormat(JavaType type) {
            return returnNullChildren ? null : numVisitor;
        }
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorIntegerPath() throws Exception {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Integer.class);
        MockIntegerVisitor intVisitor = new MockIntegerVisitor();
        MockNumberVisitor numVisitor = new MockNumberVisitor();

        // 1. Visitor provides valid child integer visitor
        MockFormatVisitorWrapper wrapper1 = new MockFormatVisitorWrapper(intVisitor, numVisitor, false);
        NumberSerializers.IntegerSerializer intSer = new NumberSerializers.IntegerSerializer();
        intSer.acceptJsonFormatVisitor(wrapper1, javaType);
        assertEquals(JsonParser.NumberType.INT, intVisitor.recordedType);
        assertNull(numVisitor.recordedType);

        // 2. Visitor returns null child integer visitor
        MockFormatVisitorWrapper wrapperNull = new MockFormatVisitorWrapper(intVisitor, numVisitor, true);
        intSer.acceptJsonFormatVisitor(wrapperNull, javaType); // ensures null guard does not throw NPE
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorLongPath() throws Exception {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Long.class);
        MockIntegerVisitor intVisitor = new MockIntegerVisitor();
        MockNumberVisitor numVisitor = new MockNumberVisitor();

        MockFormatVisitorWrapper wrapper = new MockFormatVisitorWrapper(intVisitor, numVisitor, false);
        NumberSerializers.LongSerializer.instance.acceptJsonFormatVisitor(wrapper, javaType);
        assertEquals(JsonParser.NumberType.LONG, intVisitor.recordedType);
        assertNull(numVisitor.recordedType);
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorFloatPath() throws Exception {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Float.class);
        MockIntegerVisitor intVisitor = new MockIntegerVisitor();
        MockNumberVisitor numVisitor = new MockNumberVisitor();

        // 1. Visitor provides valid child number visitor
        MockFormatVisitorWrapper wrapper1 = new MockFormatVisitorWrapper(intVisitor, numVisitor, false);
        NumberSerializers.FloatSerializer.instance.acceptJsonFormatVisitor(wrapper1, javaType);
        assertEquals(JsonParser.NumberType.FLOAT, numVisitor.recordedType);
        assertNull(intVisitor.recordedType);

        // 2. Visitor returns null child number visitor
        MockFormatVisitorWrapper wrapperNull = new MockFormatVisitorWrapper(intVisitor, numVisitor, true);
        NumberSerializers.FloatSerializer.instance.acceptJsonFormatVisitor(wrapperNull, javaType);
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorDoublePath() throws Exception {
        JavaType javaType = TypeFactory.defaultInstance().constructType(Double.class);
        MockIntegerVisitor intVisitor = new MockIntegerVisitor();
        MockNumberVisitor numVisitor = new MockNumberVisitor();

        MockFormatVisitorWrapper wrapper = new MockFormatVisitorWrapper(intVisitor, numVisitor, false);
        NumberSerializers.DoubleSerializer.instance.acceptJsonFormatVisitor(wrapper, javaType);
        assertEquals(JsonParser.NumberType.DOUBLE, numVisitor.recordedType);
        assertNull(intVisitor.recordedType);
    }

    @Test(timeout = 4000)
    public void testCustomBigIntegerBaseVisitor() throws Exception {
        // Explicitly hit the (numberType == JsonParser.NumberType.BIG_INTEGER) branch in Base constructor
        NumberSerializers.Base<BigInteger> bigIntBase = new NumberSerializers.Base<BigInteger>(
                BigInteger.class, JsonParser.NumberType.BIG_INTEGER, "integer") {
            @Override
            public void serialize(BigInteger value, JsonGenerator gen, SerializerProvider provider) throws IOException {
                gen.writeNumber(value);
            }
        };

        assertTrue(bigIntBase._isInt);

        JavaType javaType = TypeFactory.defaultInstance().constructType(BigInteger.class);
        MockIntegerVisitor intVisitor = new MockIntegerVisitor();
        MockNumberVisitor numVisitor = new MockNumberVisitor();
        MockFormatVisitorWrapper wrapper = new MockFormatVisitorWrapper(intVisitor, numVisitor, false);

        bigIntBase.acceptJsonFormatVisitor(wrapper, javaType);
        assertEquals(JsonParser.NumberType.BIG_INTEGER, intVisitor.recordedType);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    static class EmptyScalars {
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public int value = 0;
    }

    static class EmptyScalarsWrapper {
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public Integer boxedValue = 0;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public long longValue = 0L;

        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        public double doubleValue = 0.0d;
    }

    /**
     * Target Defect Ground Truth:
     * Defects4J TestJsonSerialize2::testEmptyInclusionScalars
     * When @JsonInclude(JsonInclude.Include.NON_EMPTY) is applied to numeric scalar properties with value 0,
     * they must be treated as empty and excluded, resulting in empty JSON "{}".
     */
    @Test(timeout = 4000)
    public void testEmptyInclusionScalarsDefect() throws Exception {
        String json = mapper.writeValueAsString(new EmptyScalars());
        assertEquals("{}", json);
    }

    @Test(timeout = 4000)
    public void testEmptyInclusionBoxedAndOtherScalars() throws Exception {
        String json = mapper.writeValueAsString(new EmptyScalarsWrapper());
        assertEquals("{}", json);
    }

    // =========================================================================
    // Partition D: ContextualSerializer & Annotation Shape Branching
    // =========================================================================

    static class FormattedBean {
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public int asStringInt = 123;

        @JsonFormat(shape = JsonFormat.Shape.NUMBER)
        public int asNumberInt = 456;

        @JsonFormat(shape = JsonFormat.Shape.STRING)
        public double asStringDouble = 78.5;

        public int normalInt = 789;
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithShapeString() throws Exception {
        String json = mapper.writeValueAsString(new FormattedBean());
        assertTrue(json.contains("\"asStringInt\":\"123\""));
        assertTrue(json.contains("\"asNumberInt\":456"));
        assertTrue(json.contains("\"asStringDouble\":\"78.5\""));
        assertTrue(json.contains("\"normalInt\":789"));
    }

    @Test(timeout = 4000)
    public void testCreateContextualNullGuards() throws Exception {
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        NumberSerializers.IntegerSerializer intSer = new NumberSerializers.IntegerSerializer();

        // 1. property == null
        JsonSerializer<?> resultNullProp = intSer.createContextual(prov, null);
        assertSame(intSer, resultNullProp);

        // 2. property != null but property.getMember() == null
        BeanProperty.Std propNoMember = new BeanProperty.Std(
                new PropertyName("noMember"),
                TypeFactory.defaultInstance().constructType(Integer.class),
                null, null, null, false);
        JsonSerializer<?> resultNoMember = intSer.createContextual(prov, propNoMember);
        assertSame(intSer, resultNoMember);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testInstantiationAndBaseProtectedAccess() {
        NumberSerializers container = new NumberSerializers() {};
        assertNotNull(container);

        NumberSerializers.ShortSerializer shortSer = new NumberSerializers.ShortSerializer();
        assertEquals(Short.class, shortSer.handledType());

        NumberSerializers.IntegerSerializer intSer = new NumberSerializers.IntegerSerializer();
        assertEquals(Integer.class, intSer.handledType());

        assertEquals(Number.class, NumberSerializers.IntLikeSerializer.instance.handledType());
        assertEquals(Long.class, NumberSerializers.LongSerializer.instance.handledType());
        assertEquals(Float.class, NumberSerializers.FloatSerializer.instance.handledType());
        assertEquals(Double.class, NumberSerializers.DoubleSerializer.instance.handledType());
    }

    @Test(timeout = 4000)
    public void testIntLikeSerializerWithVariousNumberSubclasses() throws Exception {
        NumberSerializers.IntLikeSerializer serializer = NumberSerializers.IntLikeSerializer.instance;
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        // AtomicInteger
        StringWriter sw = new StringWriter();
        JsonGenerator gen = jsonFactory.createGenerator(sw);
        serializer.serialize(new java.util.concurrent.atomic.AtomicInteger(55), gen, prov);
        gen.flush();
        assertEquals("55", sw.toString());

        // Short passed to IntLikeSerializer
        sw.getBuffer().setLength(0);
        serializer.serialize((short) 101, gen, prov);
        gen.flush();
        assertEquals("101", sw.toString());
    }
}