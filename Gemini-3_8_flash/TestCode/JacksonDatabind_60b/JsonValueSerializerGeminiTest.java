package com.fasterxml.jackson.databind.ser.std;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Date;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonStringFormatVisitor;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Life-cycle & Immutability:
 *    - JsonValueSerializer(AnnotatedMethod, JsonSerializer)
 *    - withResolved: Reference equality check when args unchanged vs. clone on change (ser, property, forceTypeInfo).
 *    - toString(): String format inspection.
 *
 * 2. Contextualization (createContextual):
 *    - Branch: _valueSerializer != null -> calls handlePrimaryContextualization.
 *    - Branch: _valueSerializer == null and (isFinal() == true or USE_STATIC_TYPING enabled) -> statically resolves serializer.
 *    - Branch: _valueSerializer == null and isFinal() == false and USE_STATIC_TYPING disabled -> returns this.
 *
 * 3. Serialization Execution Paths (serialize):
 *    - Branch: value == null -> prov.defaultSerializeNull(gen).
 *    - Branch: ser == null -> dynamically finds typed value serializer.
 *    - Branch: ser != null -> serializes via ser.
 *    - Exception Paths: IOException -> rethrown verbatim.
 *    - Exception Paths: Error -> unwrapped and rethrown without JsonMappingException wrapping.
 *    - Exception Paths: RuntimeException/InvocationTargetException -> unwrapped and wrapped with path.
 *
 * 4. Polymorphic Type Serialization (serializeWithType):
 *    - Branch: value == null -> provider.defaultSerializeNull(gen).
 *    - Branch: ser != null && _forceTypeInformation == true -> writes scalar prefix/suffix around ser.serialize.
 *    - Branch: ser == null -> dynamically resolves value serializer.
 *    - Exception Paths: IOException, Error, Exception unwrapping.
 *
 * 5. Schema & Format Visitors:
 *    - getSchema: _valueSerializer instanceof SchemaAware vs default schema node.
 *    - acceptJsonFormatVisitor: Enum declaring class vs non-Enum declaring class.
 *    - _acceptJsonFormatVisitorForEnum: enumTypes collected, visitor null branch, InvocationTargetException unwrap, Error unwrap.
 *    - acceptJsonFormatVisitor: ser == null -> visitor.getProvider().findTypedValueSerializer -> ser == null calls expectAnyFormat.
 *
 * 6. Natural Type std handling logic (isNaturalTypeWithStdHandling):
 *    - Primitives: int, boolean, double vs. long, float, short, byte, char.
 *    - Wrappers: String, Integer, Boolean, Double vs. Long, Float, Object.
 *    - Custom/non-default serializer check.
 *
 * 7. Ground Truth Defects (Defects4J):
 *    - TestDefaultWithCreators::testWithCreatorAndJsonValue -> type info lost or corrupted to delegate type [B instead of Bean1385.
 *    - ExternalTypeIdTest::testWithAsValue -> external type property written as delegate type [date] instead of logical type [thingy].
 */
public class JsonValueSerializerGeminiTest {

    // =========================================================================
    // Test POJOs and Fixtures
    // =========================================================================

    static class StringPojo {
        protected final String _value;
        public StringPojo(String v) { _value = v; }
        @JsonValue
        public String getValue() { return _value; }
    }

    static class NonFinalPojo {
        protected final Number _number;
        public NonFinalPojo(Number n) { _number = n; }
        @JsonValue
        public Number getNumber() { return _number; }
    }

    static class NullPojo {
        @JsonValue
        public Object getNull() { return null; }
    }

    static class ExceptionPojo {
        @JsonValue
        public String fail() {
            throw new IllegalStateException("Deliberate failure in getter");
        }
    }

    static class CustomError extends Error {
        private static final long serialVersionUID = 1L;
        public CustomError(String msg) { super(msg); }
    }

    static class ErrorPojo {
        @JsonValue
        public String failWithError() {
            throw new CustomError("Fatal Error inside accessor");
        }
    }

    enum TestColor {
        RED("r"), GREEN("g");
        private final String code;
        TestColor(String c) { code = c; }
        @JsonValue
        public String getCode() { return code; }
    }

    enum FailingEnum {
        INSTANCE;
        @JsonValue
        public String fail() {
            throw new RuntimeException("FailingEnum exception");
        }
    }

    enum ErrorEnum {
        INSTANCE;
        @JsonValue
        public String error() {
            throw new CustomError("FailingEnum error");
        }
    }

    static class DummyNonSchemaSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(String.valueOf(value));
        }
    }

    static class DummySchemaSerializer extends JsonSerializer<Object> implements SchemaAware {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(String.valueOf(value));
        }
        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
            return JsonNodeFactory.instance.textNode("dummy-schema-node");
        }
    }

    static class ThrowingSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            throw new IOException("Custom serializer IO failure");
        }
        @Override
        public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider serializers,
                TypeSerializer typeSer) throws IOException {
            throw new IOException("Custom serializer IO failure in serializeWithType");
        }
    }

    // Defect reproduction POJOs
    static class Bean1385 {
        protected byte[] data;

        @JsonCreator
        public Bean1385(byte[] data) {
            this.data = data;
        }

        @JsonValue
        public byte[] getData() {
            return data;
        }
    }

    static class ExternalWrapper {
        @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "type")
        public Object value;

        public ExternalWrapper() {}
        public ExternalWrapper(Object v) { this.value = v; }
    }

    static class Thingy {
        private final Date date;
        public Thingy(long millis) { this.date = new Date(millis); }

        @JsonValue
        public Date asDate() { return date; }
    }

    private AnnotatedMethod findMethod(Class<?> cls, String methodName) {
        ObjectMapper mapper = new ObjectMapper();
        JavaType type = mapper.constructType(cls);
        BeanDescription desc = mapper.getSerializationConfig().introspect(type);
        for (AnnotatedMethod m : desc.findMethods()) {
            if (m.getName().equals(methodName)) {
                return m;
            }
        }
        fail("Could not find method " + methodName + " on class " + cls.getName());
        return null;
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDirectConstructionAndToString() {
        AnnotatedMethod method = findMethod(StringPojo.class, "getValue");
        JsonValueSerializer ser = new JsonValueSerializer(method, null);
        assertNotNull(ser.toString());
        assertTrue(ser.toString().contains("StringPojo#getValue"));
        assertTrue(ser.toString().startsWith("(@JsonValue serializer for method"));
    }

    @Test(timeout = 4000)
    public void testWithResolvedIdentityAndChanges() {
        AnnotatedMethod method = findMethod(StringPojo.class, "getValue");
        JsonValueSerializer ser = new JsonValueSerializer(method, null);

        // Same arguments must return identity 'this'
        JsonValueSerializer same = ser.withResolved(ser._property, ser._valueSerializer, ser._forceTypeInformation);
        assertSame(ser, same);

        // Changed forceTypeInfo must return a new instance
        JsonValueSerializer changedForce = ser.withResolved(ser._property, ser._valueSerializer, !ser._forceTypeInformation);
        assertNotSame(ser, changedForce);
        assertEquals(!ser._forceTypeInformation, changedForce._forceTypeInformation);

        // Changed serializer must return a new instance
        JsonSerializer<?> dummy = new DummyNonSchemaSerializer();
        JsonValueSerializer changedSer = ser.withResolved(ser._property, dummy, ser._forceTypeInformation);
        assertNotSame(ser, changedSer);
        assertSame(dummy, changedSer._valueSerializer);

        // Changed property must return a new instance
        BeanProperty.Bogus bogusProp = new BeanProperty.Bogus();
        JsonValueSerializer changedProp = ser.withResolved(bogusProp, ser._valueSerializer, ser._forceTypeInformation);
        assertNotSame(ser, changedProp);
        assertSame(bogusProp, changedProp._property);
    }

    @Test(timeout = 4000)
    public void testSerializationOfSimplePojo() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new StringPojo("hello-world"));
        assertEquals("\"hello-world\"", json);
    }

    @Test(timeout = 4000)
    public void testSerializationOfNullValue() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new NullPojo());
        assertEquals("null", json);
    }

    @Test(timeout = 4000)
    public void testSerializationWithNonFinalTypeDynamicResolution() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new NonFinalPojo(123));
        assertEquals("123", json);

        String jsonDouble = mapper.writeValueAsString(new NonFinalPojo(45.67));
        assertEquals("45.67", jsonDouble);
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithStaticTypingFeature() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(MapperFeature.USE_STATIC_TYPING);
        String json = mapper.writeValueAsString(new NonFinalPojo(999));
        assertEquals("999", json);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsNaturalTypeWithStdHandlingBranches() {
        AnnotatedMethod method = findMethod(StringPojo.class, "getValue");
        JsonValueSerializer ser = new JsonValueSerializer(method, null);

        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();
        JsonSerializer<Object> stdIntSer = null;
        JsonSerializer<Object> stdStringSer = null;
        try {
            stdIntSer = prov.findValueSerializer(Integer.class, null);
            stdStringSer = prov.findValueSerializer(String.class, null);
        } catch (JsonMappingException e) {
            fail("Failed to get std serializer: " + e.getMessage());
        }

        // Natural primitives with std serializer
        assertTrue(ser.isNaturalTypeWithStdHandling(Integer.TYPE, stdIntSer));
        assertTrue(ser.isNaturalTypeWithStdHandling(Boolean.TYPE, stdIntSer));
        assertTrue(ser.isNaturalTypeWithStdHandling(Double.TYPE, stdIntSer));

        // Non-natural primitives
        assertFalse(ser.isNaturalTypeWithStdHandling(Long.TYPE, stdIntSer));
        assertFalse(ser.isNaturalTypeWithStdHandling(Float.TYPE, stdIntSer));
        assertFalse(ser.isNaturalTypeWithStdHandling(Short.TYPE, stdIntSer));
        assertFalse(ser.isNaturalTypeWithStdHandling(Byte