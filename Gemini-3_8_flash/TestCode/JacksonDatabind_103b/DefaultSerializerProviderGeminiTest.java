package com.fasterxml.jackson.databind.ser;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.annotation.SimpleObjectIdResolver;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.AnnotatedClass;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonschema.JsonSchema;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.WritableObjectId;
import com.fasterxml.jackson.databind.type.TypeFactory;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: com.fasterxml.jackson.databind.ser.DefaultSerializerProvider
 *
 * Partition A: Core Functional Logic & State Transitions
 * - Impl constructors, copy(), and createInstance(SerializationConfig, SerializerFactory)
 * - Cached serializer counting and cache flushing
 * - Finding object IDs (first seen, repeat access, generator recycling)
 * - Map type determination based on SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID
 * - Generator accessor (getGenerator) tracking during serialization lifecycle
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - Null serializer definitions, null filter instances, null types
 * - Empty root name vs explicit root name vs null root name wrapping
 * - Null value serialization invoking default null value serializer
 * - Schema generation on supported Object vs unsupported types (primitives/arrays)
 * - Object.class inspection under FAIL_ON_EMPTY_BEANS toggles
 *
 * Partition C: Defect-Targeted Branch Zone
 * - BasicException and wrapAsIOE verification: ensuring proper error propagation
 *   and preventing spurious location duplicates or message corruption.
 * - Subclassing DefaultSerializerProvider directly without overriding copy().
 * - Faulty filter.equals(null) throwing RuntimeException caught and wrapped into JsonMappingException.
 *
 * Partition D: Exception & Defensive Guard Paths
 * - serializerInstance validation: non-class/non-serializer def, bogus class, JsonSerializer.None
 * - Incompatible root type assertion during serializeValue and serializePolymorphic
 * - HandlerInstantiator delegating vs falling back to ClassUtil.createInstance
 * - Visitor invocation on null JavaType (expecting IllegalArgumentException)
 *
 * Partition E: Object Lifecycle & Contract Integrity
 * - Impl copy() immutability and state segregation
 */
public class DefaultSerializerProviderGeminiTest {

    // Dummy helper classes for serializer tests
    public static class CustomStringSerializer extends JsonSerializer<String> {
        @Override
        public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString("custom:" + value);
        }
    }

    public static class BogusFilter {
        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                throw new IllegalStateException("Simulated filter failure on null check");
            }
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return 1;
        }
    }

    public static class NonOverridingProvider extends DefaultSerializerProvider {
        private static final long serialVersionUID = 1L;

        public NonOverridingProvider() {
            super();
        }

        public NonOverridingProvider(NonOverridingProvider src) {
            super(src);
        }

        @Override
        public DefaultSerializerProvider createInstance(SerializationConfig config, SerializerFactory jsf) {
            return this;
        }
    }

    private DefaultSerializerProvider.Impl createProvider() {
        ObjectMapper mapper = new ObjectMapper();
        return (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
    }

    private SerializationConfig getConfig() {
        return new ObjectMapper().getSerializationConfig();
    }

    /*
     * ------------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * ------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testImplConstructionAndCopy() {
        DefaultSerializerProvider.Impl impl = new DefaultSerializerProvider.Impl();
        assertNull(impl.getGenerator());

        DefaultSerializerProvider copy = impl.copy();
        assertNotNull(copy);
        assertTrue(copy instanceof DefaultSerializerProvider.Impl);
        assertNotSame(impl, copy);
    }

    @Test(timeout = 4000)
    public void testCachedSerializersCountAndFlush() {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();

        int initial = prov.cachedSerializersCount();
        assertTrue(initial >= 0);

        prov.flushCachedSerializers();
        assertEquals(0, prov.cachedSerializersCount());
    }

    @Test(timeout = 4000)
    public void testFindObjectIdIdentityVsEquality() {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();

        ObjectIdGenerator<?> gen1 = new ObjectIdGenerators.IntSequenceGenerator();
        Object key1 = new String("test_id");
        Object key2 = new String("test_id");

        WritableObjectId oid1 = prov.findObjectId(key1, gen1);
        assertNotNull(oid1);
        // By default, IdentityHashMap is used
        WritableObjectId oid2 = prov.findObjectId(key2, gen1);
        assertNotNull(oid2);
        assertNotSame(oid1, oid2);

        // When USE_EQUALITY_FOR_OBJECT_ID is enabled
        ObjectMapper eqMapper = new ObjectMapper();
        eqMapper.enable(SerializationFeature.USE_EQUALITY_FOR_OBJECT_ID);
        DefaultSerializerProvider.Impl eqProv = (DefaultSerializerProvider.Impl) eqMapper.getSerializerProviderInstance();

        WritableObjectId eqOid1 = eqProv.findObjectId(key1, gen1);
        WritableObjectId eqOid2 = eqProv.findObjectId(key2, gen1);
        assertSame(eqOid1, eqOid2);
    }

    @Test(timeout = 4000)
    public void testFindObjectIdGeneratorReuse() {
        DefaultSerializerProvider.Impl prov = createProvider();
        ObjectIdGenerator<?> genA = new ObjectIdGenerators.IntSequenceGenerator();
        ObjectIdGenerator<?> genB = new ObjectIdGenerators.IntSequenceGenerator();

        WritableObjectId oid1 = prov.findObjectId("obj1", genA);
        WritableObjectId oid2 = prov.findObjectId("obj2", genB);

        assertNotSame(oid1, oid2);
        assertSame(oid1.generator.getClass(), oid2.generator.getClass());
    }

    @Test(timeout = 4000)
    public void testGetGeneratorDuringSerialization() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        assertNull(prov.getGenerator());

        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        prov.serializeValue(gen, "value");
        assertSame(gen, prov.getGenerator());
    }

    /*
     * ------------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * ------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testSerializerInstanceWithNullAndSpecialMarkers() throws JsonMappingException {
        DefaultSerializerProvider.Impl prov = createProvider();

        // Null definition returns null
        assertNull(prov.serializerInstance(null, null));

        // Instance of JsonSerializer directly
        CustomStringSerializer directSer = new CustomStringSerializer();
        assertSame(directSer, prov.serializerInstance(null, directSer));

        // JsonSerializer.None marker returns null
        assertNull(prov.serializerInstance(null, JsonSerializer.None.class));
    }

    @Test(timeout = 4000)
    public void testIncludeFilterInstanceNullOrDirect() {
        DefaultSerializerProvider.Impl prov = createProvider();

        assertNull(prov.includeFilterInstance(null, null));

        Object filter = prov.includeFilterInstance(null, String.class);
        assertNotNull(filter);
        assertTrue(filter instanceof String);
    }

    @Test(timeout = 4000)
    public void testIncludeFilterSuppressNullsNormal() throws JsonMappingException {
        DefaultSerializerProvider.Impl prov = createProvider();

        // null filter always suppresses nulls
        assertTrue(prov.includeFilterSuppressNulls(null));

        // Standard object does not equal null -> returns false
        assertFalse(prov.includeFilterSuppressNulls("non-null-filter"));
    }

    @Test(timeout = 4000)
    public void testSerializeValueNullHandling() throws IOException {
        DefaultSerializerProvider.Impl prov = createProvider();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);

        prov.serializeValue(gen, null);
        gen.flush();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeValueNullWithRootType() throws IOException {
        DefaultSerializerProvider.Impl prov = createProvider();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);

        prov.serializeValue(gen, null, type);
        gen.flush();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeValueNullWithRootTypeAndSerializer() throws IOException {
        DefaultSerializerProvider.Impl prov = createProvider();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);

        prov.serializeValue(gen, null, type, null);
        gen.flush();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializePolymorphicNull() throws IOException {
        DefaultSerializerProvider.Impl prov = createProvider();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);

        prov.serializePolymorphic(gen, null, type, null, null);
        gen.flush();
        assertEquals("null", sw.toString());
    }

    @Test(timeout = 4000)
    public void testHasSerializerForObjectClass() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        DefaultSerializerProvider prov = (DefaultSerializerProvider) mapper.getSerializerProviderInstance();

        assertTrue(prov.hasSerializerFor(Object.class, null));

        mapper.enable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        DefaultSerializerProvider provStrict = (DefaultSerializerProvider) mapper.getSerializerProviderInstance();
        // Without properties, Object.class has no explicit serializer when failing on empty beans
        assertFalse(provStrict.hasSerializerFor(Object.class, null));
    }

    @Test(timeout = 4000)
    public void testHasSerializerForCommonClasses() {
        DefaultSerializerProvider prov = createProvider();
        AtomicReference<Throwable> cause = new AtomicReference<Throwable>();

        assertTrue(prov.hasSerializerFor(String.class, cause));
        assertNull(cause.get());

        assertTrue(prov.hasSerializerFor(Integer.class, cause));
        assertNull(cause.get());
    }

    /*
     * ------------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone
     * ------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testSubclassNotOverridingCopyThrowsIllegalState() {
        NonOverridingProvider provider = new NonOverridingProvider();
        try {
            provider.copy();
            fail("Expected IllegalStateException for subclass not overriding copy()");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("DefaultSerializerProvider sub-class not overriding copy()"));
        }
    }

    @Test(timeout = 4000)
    public void testIncludeFilterSuppressNullsExceptionHandling() {
        DefaultSerializerProvider.Impl prov = createProvider();
        BogusFilter filter = new BogusFilter();

        try {
            prov.includeFilterSuppressNulls(filter);
            fail("Expected JsonMappingException when filter.equals(null) throws Throwable");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Problem determining whether filter of type"));
            assertTrue(e.getMessage().contains("Simulated filter failure on null check"));
        }
    }

    @Test(timeout = 4000)
    public void testSerializeValueWithRootNameWrapping() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();

        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        prov.serializeValue(gen, "wrappedTest");
        gen.flush();

        assertEquals("{\"String\":\"wrappedTest\"}", sw.toString());
    }

    @Test(timeout = 4000)
    public void testSerializeValueWithExplicitRootName() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig config = mapper.getSerializationConfig().withRootName(PropertyName.construct("CustomRoot"));
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        DefaultSerializerProvider instance = prov.createInstance(config, mapper.getSerializerFactory());

        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        instance.serializeValue(gen, "hello");
        gen.flush();

        assertEquals("{\"CustomRoot\":\"hello\"}", sw.toString());
    }

    /*
     * ------------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * ------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testSerializerInstanceNotClassOrSerializerThrows() {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        JavaType type = TypeFactory.defaultInstance().constructType(String.class);
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, mapper.getSerializationConfig());

        try {
            prov.serializerInstance(ac, 12345);
            fail("Expected JsonMappingException for illegal serializer definition type");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("AnnotationIntrospector returned serializer definition of type java.lang.Integer"));
        }
    }

    @Test(timeout = 4000)
    public void testSerializerInstanceClassNotExtendingJsonSerializerThrows() {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();
        AnnotatedClass ac = AnnotatedClass.constructWithoutSuperTypes(String.class, mapper.getSerializationConfig());

        try {
            prov.serializerInstance(ac, File.class);
            fail("Expected JsonMappingException for class not implementing JsonSerializer");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("expected Class<JsonSerializer>"));
        }
    }

    @Test(timeout = 4000)
    public void testIncompatibleRootTypeInSerializeValue() throws IOException {
        DefaultSerializerProvider.Impl prov = createProvider();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        JavaType intType = TypeFactory.defaultInstance().constructType(Integer.class);

        try {
            // String value is incompatible with rootType Integer
            prov.serializeValue(gen, "not-an-integer", intType);
            fail("Expected JsonMappingException for incompatible root type");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Incompatible types"));
        }
    }

    @Test(timeout = 4000)
    public void testIncompatibleRootTypeInSerializeValueWithExplicitSer() throws IOException {
        DefaultSerializerProvider.Impl prov = createProvider();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        JavaType intType = TypeFactory.defaultInstance().constructType(Integer.class);

        try {
            prov.serializeValue(gen, "not-an-integer", intType, null);
            fail("Expected JsonMappingException for incompatible root type");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Incompatible types"));
        }
    }

    @Test(timeout = 4000)
    public void testIncompatibleRootTypeInSerializePolymorphic() throws IOException {
        DefaultSerializerProvider.Impl prov = createProvider();
        StringWriter sw = new StringWriter();
        JsonGenerator gen = new JsonFactory().createGenerator(sw);
        JavaType intType = TypeFactory.defaultInstance().constructType(Integer.class);

        try {
            prov.serializePolymorphic(gen, "not-an-integer", intType, null, null);
            fail("Expected JsonMappingException for incompatible root type");
        } catch (JsonMappingException e) {
            assertTrue(e.getMessage().contains("Incompatible types"));
        }
    }

    @Test(timeout = 4000)
    public void testAcceptJsonFormatVisitorNullTypeThrows() throws JsonMappingException {
        DefaultSerializerProvider.Impl prov = createProvider();
        try {
            prov.acceptJsonFormatVisitor(null, null);
            fail("Expected IllegalArgumentException when javaType is null");
        } catch (IllegalArgumentException e) {
            assertEquals("A class must be provided", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testGenerateJsonSchemaUnsupportedTypeThrows() {
        DefaultSerializerProvider.Impl prov = createProvider();
        try {
            // Primitive int cannot be serialized as a JSON object, so it will fail schema creation
            prov.generateJsonSchema(int.class);
            fail("Expected IllegalArgumentException for schema on primitive type");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("would not be serialized as a JSON object and therefore has no schema"));
        } catch (JsonMappingException e) {
            // Also acceptable if schema resolution failed
            assertNotNull(e);
        }
    }

    @Test(timeout = 4000)
    public void testGenerateJsonSchemaForBean() throws JsonMappingException {
        DefaultSerializerProvider.Impl prov = createProvider();
        JsonSchema schema = prov.generateJsonSchema(SimpleBean.class);
        assertNotNull(schema);
        JsonNode node = schema.getSchemaNode();
        assertNotNull(node);
        assertTrue(node.isObject());
    }

    public static class SimpleBean {
        public String name;
        public int age;
    }

    /*
     * ------------------------------------------------------------------------
     * Partition E: Object Lifecycle & Contract Integrity
     * ------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testHandlerInstantiatorCustomInstantiation() throws JsonMappingException {
        ObjectMapper mapper = new ObjectMapper();
        final CustomStringSerializer mySerializer = new CustomStringSerializer();
        final Object myFilter = new Object();

        mapper.setHandlerInstantiator(new HandlerInstantiator() {
            @Override
            public JsonIncludeFilter includeFilterInstance(SerializationConfig config, BeanPropertyDefinition forProperty, Class<?> filterClass) {
                return null;
            }

            @Override
            public Object includeFilterInstance(MapperConfig<?> config, BeanPropertyDefinition forProperty, Class<?> filterClass) {
                return myFilter;
            }

            @Override
            public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {
                if (serClass == CustomStringSerializer.class) {
                    return mySerializer;
                }
                return null;
            }

            @Override
            public com.fasterxml.jackson.databind.JsonDeserializer<?> deserializerInstance(com.fasterxml.jackson.databind.DeserializationConfig config, Annotated annotated, Class<?> deserClass) {
                return null;
            }

            @Override
            public com.fasterxml.jackson.databind.KeyDeserializer keyDeserializerInstance(com.fasterxml.jackson.databind.DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) {
                return null;
            }

            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
                return null;
            }

            @Override
            public com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) {
                return null;
            }
        });

        DefaultSerializerProvider.Impl prov = (DefaultSerializerProvider.Impl) mapper.getSerializerProviderInstance();

        JsonSerializer<?> resolvedSer = prov.serializerInstance(null, CustomStringSerializer.class);
        assertSame(mySerializer, resolvedSer);

        Object resolvedFilter = prov.includeFilterInstance(null, Object.class);
        assertSame(myFilter, resolvedFilter);
    }

    @Test(timeout = 4000)
    public void testSerializerInstanceCreatingNewInstance() throws JsonMappingException {
        DefaultSerializerProvider.Impl prov = createProvider();
        JsonSerializer<?> ser = prov.serializerInstance(null, CustomStringSerializer.class);
        assertNotNull(ser);
        assertTrue(ser instanceof CustomStringSerializer);
    }
}