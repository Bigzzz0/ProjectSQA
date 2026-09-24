package com.fasterxml.jackson.databind.ser.std;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonschema.SchemaAware;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.ResolvableSerializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.Converter;
import com.fasterxml.jackson.databind.util.StdConverter;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.ser.std.StdDelegatingSerializer
 * Primary Defect: [databind#731] Dynamic delegate serializer resolution failure when output type is
 *                 Object.class or missing generics. Eager lookup in createContextual() throws
 *                 JsonMappingException ("No serializer found for class ... and no properties discovered")
 *                 instead of delaying serializer resolution to runtime based on the converted value's type.
 *
 * Branch & Condition Coverage:
 * 1. Constructor Overloads:
 *    - StdDelegatingSerializer(Converter<?,?>) -> delegateType=null, delegateSerializer=null, handledType=Object.class
 *    - StdDelegatingSerializer(Class<T>, Converter<T,?>) -> handledType=cls, delegateType=null, delegateSerializer=null
 *    - StdDelegatingSerializer(Converter, JavaType, JsonSerializer) -> full state initialization
 * 2. withDelegate:
 *    - getClass() == StdDelegatingSerializer.class -> creates new StdDelegatingSerializer
 *    - getClass() != StdDelegatingSerializer.class -> throws IllegalStateException
 * 3. resolve(SerializerProvider):
 *    - _delegateSerializer == null -> no-op
 *    - _delegateSerializer != null && !(delSer instanceof ResolvableSerializer) -> no-op
 *    - _delegateSerializer != null && (delSer instanceof ResolvableSerializer) -> resolves delegate
 * 4. createContextual(SerializerProvider, BeanProperty):
 *    - _delegateSerializer == null:
 *        - _delegateType == null -> resolves delegateType via _converter.getOutputType()
 *        - locates delSer via provider.findValueSerializer(delegateType)
 *        - [DEFECT ZONE databind#731]: When delegateType is Object.class, dynamic resolution must be handled
 *    - delSer instanceof ContextualSerializer -> invokes provider.handleSecondaryContextualization
 *    - delSer == _delegateSerializer -> returns this
 *    - delSer != _delegateSerializer -> returns withDelegate(...)
 * 5. serialize(Object, JsonGenerator, SerializerProvider):
 *    - convertValue(value) == null -> calls provider.defaultSerializeNull(gen)
 *    - convertValue(value) != null -> delegates to _delegateSerializer.serialize(...)
 * 6. serializeWithType(Object, JsonGenerator, SerializerProvider, TypeSerializer):
 *    - converts value and delegates to _delegateSerializer.serializeWithType(...)
 * 7. isEmpty(Object) and isEmpty(SerializerProvider, Object):
 *    - converts value and delegates to _delegateSerializer.isEmpty(...)
 * 8. getSchema(SerializerProvider, Type) & getSchema(SerializerProvider, Type, boolean):
 *    - _delegateSerializer instanceof SchemaAware -> calls delegate getSchema(...)
 *    - !(_delegateSerializer instanceof SchemaAware) -> falls back to super.getSchema(...)
 * 9. acceptJsonFormatVisitor(JsonFormatVisitorWrapper, JavaType):
 *    - delegates to _delegateSerializer.acceptJsonFormatVisitor(...)
 */
public class StdDelegatingSerializerGeminiTest {

    // =========================================================================
    // Test Doubles & Helpers
    // =========================================================================

    static class TestResolvableContextualSerializer extends JsonSerializer<Object>
            implements ResolvableSerializer, ContextualSerializer, SchemaAware {
        boolean resolved = false;
        boolean contextualized = false;
        boolean schemaCalled = false;
        boolean schemaOptionalCalled = false;
        boolean acceptVisitorCalled = false;

        @Override
        public void resolve(SerializerProvider provider) {
            this.resolved = true;
        }

        @Override
        public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
            this.contextualized = true;
            return this;
        }

        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(String.valueOf(value));
        }

        @Override
        public void serializeWithType(Object value, JsonGenerator gen, SerializerProvider serializers,
                                      TypeSerializer typeSer) throws IOException {
            gen.writeString("type:" + value);
        }

        @Override
        public boolean isEmpty(Object value) {
            return value == null || "".equals(value);
        }

        @Override
        public boolean isEmpty(SerializerProvider prov, Object value) {
            return isEmpty(value);
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint) {
            schemaCalled = true;
            return provider.getNodeFactory().textNode("customSchema");
        }

        @Override
        public JsonNode getSchema(SerializerProvider provider, Type typeHint, boolean isOptional) {
            schemaOptionalCalled = true;
            return provider.getNodeFactory().textNode("customSchemaOptional");
        }

        @Override
        public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) {
            acceptVisitorCalled = true;
        }
    }

    static class NonSchemaSerializer extends JsonSerializer<Object> {
        @Override
        public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString("nonSchema:" + value);
        }
    }

    static class SubclassWithoutWithDelegateOverride extends StdDelegatingSerializer {
        public SubclassWithoutWithDelegateOverride(Converter<?, ?> converter) {
            super(converter);
        }

        public StdDelegatingSerializer triggerWithDelegate(Converter<Object, ?> conv,
                                                           JavaType type, JsonSerializer<?> ser) {
            return super.withDelegate(conv, type, ser);
        }
    }

    // Ground truth defect #731 target classes
    @JsonSerialize(converter = DummyConverter731.class)
    static class DummyBean {
    }

    static class DummyConverter731 extends StdConverter<DummyBean, Object> {
        @Override
        public Object convert(DummyBean value) {
            Map<String, Object> map = new LinkedHashMap<String, Object>();
            map.put("key", "value");
            return map;
        }
    }

    @JsonSerialize(converter = StringConverter731.class)
    static class WrapperBean731 {
        public int id;

        public WrapperBean731(int id) {
            this.id = id;
        }
    }

    static class StringConverter731 extends StdConverter<WrapperBean731, Object> {
        @Override
        public Object convert(WrapperBean731 value) {
            return "wrapped:" + value.id;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndAccessors() {
        Converter<String, Integer> converter = new StdConverter<String, Integer>() {
            @Override
            public Integer convert(String value) {
                return Integer.parseInt(value);
            }
        };

        // 1. Single-arg constructor
        StdDelegatingSerializer ser1 = new StdDelegatingSerializer(converter);
        assertEquals(Object.class, ser1.handledType());
        assertSame(converter, ser1.getConverter());
        assertNull(ser1.getDelegatee());

        // 2. Class + Converter constructor
        StdDelegatingSerializer ser2 = new StdDelegatingSerializer(String.class, converter);
        assertEquals(String.class, ser2.handledType());
        assertSame(converter, ser2.getConverter());
        assertNull(ser2.getDelegatee());

        // 3. Full constructor
        JavaType intType = TypeFactory.defaultInstance().constructType(Integer.class);
        NonSchemaSerializer delegateSer = new NonSchemaSerializer();
        StdDelegatingSerializer ser3 = new StdDelegatingSerializer(
                (Converter<Object, ?>) (Converter<?, ?>) converter, intType, delegateSer);

        assertEquals(Integer.class, ser3.handledType());
        assertSame(converter, ser3.getConverter());
        assertSame(delegateSer, ser3.getDelegatee());
    }

    @Test(timeout = 4000)
    public void testWithDelegateDirectCall() {
        Converter<Object, Object> conv = new StdConverter<Object, Object>() {
            @Override
            public Object convert(Object value) {
                return value;
            }
        };
        StdDelegatingSerializer base = new StdDelegatingSerializer(conv);
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        NonSchemaSerializer del = new NonSchemaSerializer();

        StdDelegatingSerializer created = base.withDelegate(conv, strType, del);
        assertNotNull(created);
        assertNotSame(base, created);
        assertSame(conv, created.getConverter());
        assertSame(del, created.getDelegatee());
        assertEquals(String.class, created.handledType());
    }

    @Test(timeout = 4000)
    public void testConvertValueCallsUnderlyingConverter() {
        Converter<Object, String> conv = new StdConverter<Object, String>() {
            @Override
            public String convert(Object value) {
                return "prefix_" + value;
            }
        };
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv);
        Object result = ser.convertValue("test");
        assertEquals("prefix_test", result);
    }

    @Test(timeout = 4000)
    public void testSerializeNonNullValue() throws Exception {
        Converter<Object, String> conv = new StdConverter<Object, String>() {
            @Override
            public String convert(Object value) {
                return "converted:" + value;
            }
        };
        TestResolvableContextualSerializer delegate = new TestResolvableContextualSerializer();
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv, strType, delegate);

        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        ser.serialize("hello", gen, prov);
        gen.flush();
        assertEquals("\"converted:hello\"", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeWithType() throws Exception {
        Converter<Object, String> conv = new StdConverter<Object, String>() {
            @Override
            public String convert(Object value) {
                return "typed:" + value;
            }
        };
        TestResolvableContextualSerializer delegate = new TestResolvableContextualSerializer();
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv, strType, delegate);

        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        ser.serializeWithType("data", gen, prov, null);
        gen.flush();
        assertEquals("\"type:typed:data\"", sw.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializeNullConvertedValue() throws Exception {
        Converter<Object, String> nullConverter = new StdConverter<Object, String>() {
            @Override
            public String convert(Object value) {
                return null;
            }
        };
        TestResolvableContextualSerializer delegate = new TestResolvableContextualSerializer();
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(nullConverter, strType, delegate);

        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = mapper.getFactory().createGenerator(sw);
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        ser.serialize("nonNullInput", gen, prov);
        gen.flush();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testIsEmptyOverloads() {
        Converter<Object, String> conv = new StdConverter<Object, String>() {
            @Override
            public String convert(Object value) {
                return (value == null) ? "" : value.toString();
            }
        };
        TestResolvableContextualSerializer delegate = new TestResolvableContextualSerializer();
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv, strType, delegate);

        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        // Deprecated isEmpty(value)
        assertTrue(ser.isEmpty(null));
        assertTrue(ser.isEmpty(""));
        assertFalse(ser.isEmpty("text"));

        // Contextual isEmpty(prov, value)
        assertTrue(ser.isEmpty(prov, null));
        assertTrue(ser.isEmpty(prov, ""));
        assertFalse(ser.isEmpty(prov, "text"));
    }

    @Test(timeout = 4000)
    public void testResolveBranches() throws Exception {
        Converter<Object, Object> conv = new StdConverter<Object, Object>() {
            @Override
            public Object convert(Object value) {
                return value;
            }
        };
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        // Branch 1: delegateSerializer is null
        StdDelegatingSerializer serNull = new StdDelegatingSerializer(conv);
        serNull.resolve(prov); // Must execute without exception

        // Branch 2: delegateSerializer is NOT ResolvableSerializer
        NonSchemaSerializer nonResolvable = new NonSchemaSerializer();
        StdDelegatingSerializer serNonRes = new StdDelegatingSerializer(conv, null, nonResolvable);
        serNonRes.resolve(prov); // Must execute without exception

        // Branch 3: delegateSerializer IS ResolvableSerializer
        TestResolvableContextualSerializer resolvable = new TestResolvableContextualSerializer();
        StdDelegatingSerializer serRes = new StdDelegatingSerializer(conv, null, resolvable);
        assertFalse(resolvable.resolved);
        serRes.resolve(prov);
        assertTrue(resolvable.resolved);
    }

    @Test(timeout = 4000)
    public void testGetSchemaBranches() throws Exception {
        Converter<Object, String> conv = new StdConverter<Object, String>() {
            @Override
            public String convert(Object value) {
                return String.valueOf(value);
            }
        };
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        // Branch 1: delegate IS SchemaAware
        TestResolvableContextualSerializer schemaAwareDel = new TestResolvableContextualSerializer();
        StdDelegatingSerializer serSchemaAware = new StdDelegatingSerializer(conv, strType, schemaAwareDel);

        JsonNode schema1 = serSchemaAware.getSchema(prov, String.class);
        assertTrue(schemaAwareDel.schemaCalled);
        assertEquals("customSchema", schema1.textValue());

        JsonNode schemaOpt1 = serSchemaAware.getSchema(prov, String.class, false);
        assertTrue(schemaAwareDel.schemaOptionalCalled);
        assertEquals("customSchemaOptional", schemaOpt1.textValue());

        // Branch 2: delegate is NOT SchemaAware (falls back to super.getSchema)
        NonSchemaSerializer nonSchemaDel = new NonSchemaSerializer();
        StdDelegatingSerializer serNonSchema = new StdDelegatingSerializer(conv, strType, nonSchemaDel);

        JsonNode schemaFallback = serNonSchema.getSchema(prov, String.class);
        assertNotNull(schemaFallback);
        assertTrue(schemaFallback.isObject());

        JsonNode schemaOptFallback = serNonSchema.getSchema(prov, String.class, true);
        assertNotNull(schemaOptFallback);
        assertTrue(schemaOptFallback.isObject());
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorDelegates() throws Exception {
        Converter<Object, String> conv = new StdConverter<Object, String>() {
            @Override
            public String convert(Object value) {
                return String.valueOf(value);
            }
        };
        TestResolvableContextualSerializer delegate = new TestResolvableContextualSerializer();
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv, strType, delegate);

        ser.acceptJsonFormatVisitor(null, strType);
        assertTrue(delegate.acceptVisitorCalled);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (databind#731)
    // =========================================================================

    /**
     * Targets Defects4J ground-truth failure in TestConvertingSerializer::testIssue731.
     * When a converter produces an Object (empty/dynamic type) and the source is an empty bean,
     * StdDelegatingSerializer must dynamically resolve the delegate serializer at runtime rather
     * than failing with FAIL_ON_EMPTY_BEANS during createContextual eager lookup of Object.class.
     */
    @Test(timeout = 4000)
    public void testIssue731DynamicLookupWithEmptyBean() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new DummyBean());
        assertEquals("{\"key\":\"value\"}", json);
    }

    /**
     * Targets dynamic resolution where output type is Object and converts to a primitive/String wrapper.
     */
    @Test(timeout = 4000)
    public void testIssue731DynamicLookupWithObjectReturnType() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new WrapperBean731(101));
        assertEquals("\"wrapped:101\"", json);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testSubclassMustOverrideWithDelegate() {
        Converter<Object, Object> conv = new StdConverter<Object, Object>() {
            @Override
            public Object convert(Object value) {
                return value;
            }
        };
        SubclassWithoutWithDelegateOverride sub = new SubclassWithoutWithDelegateOverride(conv);
        try {
            sub.triggerWithDelegate(conv, null, null);
            fail("Expected IllegalStateException for un-overridden withDelegate in subclass");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Sub-class"));
            assertTrue(e.getMessage().contains("must override 'withDelegate'"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contextual Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCreateContextualWhenSameSerializerReturnsThis() throws Exception {
        Converter<Object, String> conv = new StdConverter<Object, String>() {
            @Override
            public String convert(Object value) {
                return String.valueOf(value);
            }
        };
        NonSchemaSerializer nonContextual = new NonSchemaSerializer();
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv, strType, nonContextual);

        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonSerializer<?> contextual = ser.createContextual(prov, null);
        assertSame("Should return 'this' when delegate is non-contextual and already assigned", ser, contextual);
    }

    @Test(timeout = 4000)
    public void testCreateContextualWithContextualDelegate() throws Exception {
        Converter<Object, String> conv = new StdConverter<Object, String>() {
            @Override
            public String convert(Object value) {
                return String.valueOf(value);
            }
        };
        TestResolvableContextualSerializer contextualDelegate = new TestResolvableContextualSerializer();
        JavaType strType = TypeFactory.defaultInstance().constructType(String.class);
        StdDelegatingSerializer ser = new StdDelegatingSerializer(conv, strType, contextualDelegate);

        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonSerializer<?> result = ser.createContextual(prov, null);
        assertNotNull(result);
        assertTrue(contextualDelegate.contextualized);
    }

    @Test(timeout = 4000)
    public void testCreateContextualResolvesDelegateTypeFromConverterWhenNull() throws Exception {
        Converter<Object, String> conv = new StdConverter<Object, String>() {
            @Override
            public String convert(Object value) {
                return String.valueOf(value);
            }
        };
        // Construct with null delegateType and null delegateSerializer
        StdDelegatingSerializer uninitialized = new StdDelegatingSerializer(conv);

        ObjectMapper mapper = new ObjectMapper();
        SerializerProvider prov = mapper.getSerializerProviderInstance();

        JsonSerializer<?> result = uninitialized.createContextual(prov, null);
        assertNotNull(result);
        assertTrue(result instanceof StdDelegatingSerializer);
        StdDelegatingSerializer resolved = (StdDelegatingSerializer) result;
        assertNotNull(resolved.getDelegatee());
        assertEquals(String.class, resolved.handledType());
    }
}