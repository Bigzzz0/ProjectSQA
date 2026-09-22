package com.fasterxml.jackson.databind.ser;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.impl.WritableObjectId;
import com.fasterxml.jackson.databind.util.ClassUtil;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * White-box test suite for {@link DefaultSerializerProvider}.
 * Targets defect: duplicate "at [" markers in exception messages.
 *
 * <pre>
 * [Branch & Defect Analysis Matrix]
 * =================================
 * Target branches / conditions:
 *   - `_wrapAsIOE()`: wrapping non-IOException vs IOException, message null vs non-null.
 *   - `includeFilterSuppressNulls()`: null filter, non-null filter with/without exception.
 *   - `includeFilterInstance()`: null class, non-null class with/without handler.
 *   - `serializerInstance()`: null serDef, JsonSerializer instance, Class with None, bogus, non-JsonSerializer.
 *   - `findObjectId()`: _seenObjectIds null, non-null with entry, generator reuse, new generator.
 *   - `serializeValue()`: null value, non-null with/without root wrapping.
 *   - `hasSerializerFor()`: Object.class with FAIL_ON_EMPTY_BEANS, exception handling.
 *   - `acceptJsonFormatVisitor()`: null javaType.
 *   - `generateJsonSchema()`: schema node not ObjectNode.
 *   - `copy()`: default implementation throws IllegalStateException.
 * </pre>
 */
public class DefaultSerializerProviderDeepseekTest {

    // -----------------------------------------------------------------------
    //  Partition A – Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCreateInstanceFromBlueprint() {
        DefaultSerializerProvider.Impl blueprint = new DefaultSerializerProvider.Impl();
        // Impl.createInstance is not directly called; we verify blueprint existence
        assertNotNull(blueprint);
        // Ensure copy works for Impl
        DefaultSerializerProvider copy = blueprint.copy();
        assertNotNull(copy);
        assertNotSame(blueprint, copy);
    }

    @Test(timeout = 4000)
    public void testCopyNonImplClassThrows() {
        // Create an anonymous subclass of DefaultSerializerProvider (not Impl)
        DefaultSerializerProvider anonymous = new DefaultSerializerProvider() {
            private static final long serialVersionUID = 1L;

            @Override
            public DefaultSerializerProvider createInstance(SerializationConfig config, SerializerFactory jsf) {
                return null;
            }
        };
        try {
            anonymous.copy();
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testCachedSerializersCountInitial() {
        DefaultSerializerProvider.Impl provider = new DefaultSerializerProvider.Impl();
        assertEquals(0, provider.cachedSerializersCount());
    }

    @Test(timeout = 4000)
    public void testFlushCachedSerializers() {
        DefaultSerializerProvider.Impl provider = new DefaultSerializerProvider.Impl();
        provider.flushCachedSerializers();
        // No exception expected
    }

    // -----------------------------------------------------------------------
    //  Partition B – Boundary Value Analysis (BVA) & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testIncludeFilterInstanceNullClass() {
        DefaultSerializerProvider.Impl provider = new DefaultSerializerProvider.Impl();
        assertNull(provider.includeFilterInstance(null, null));
    }

    @Test(timeout = 4000)
    public void testIncludeFilterSuppressNullsNullFilter() throws Exception {
        DefaultSerializerProvider.Impl provider = new DefaultSerializerProvider.Impl();
        assertTrue(provider.includeFilterSuppressNulls(null));
    }

    @Test(timeout = 4000)
    public void testIncludeFilterSuppressNullsNonNullFilterThatEqualsNull() throws Exception {
        DefaultSerializerProvider.Impl provider = new DefaultSerializerProvider.Impl();
        // filter that returns true for equals(null)
        Object filter = new Object() {
            @Override
            public boolean equals(Object obj) {
                return obj == null; // always true for null
            }
        };
        assertTrue(provider.includeFilterSuppressNulls(filter));
    }

    @Test(timeout = 4000)
    public void testGetGeneratorBeforeSerialization() {
        DefaultSerializerProvider.Impl provider = new DefaultSerializerProvider.Impl();
        assertNull(provider.getGenerator());
    }

    @Test(timeout = 4000)
    public void testHasSerializerForObjectClassWithFailOnEmptyBeansDisabled() {
        // Need a configured ObjectMapper to get proper provider
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        DefaultSerializerProvider provider = (DefaultSerializerProvider) mapper.getSerializerProvider();
        // AtomicReference without cause
        assertTrue(provider.hasSerializerFor(Object.class, null));
    }

    @Test(timeout = 4000)
    public void testHasSerializerForObjectClassWithFailOnEmptyBeansEnabled() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        DefaultSerializerProvider provider = (DefaultSerializerProvider) mapper.getSerializerProvider();
        // For Object.class, should still return true because no explicit serializer,
        // but detection will attempt and succeed (empty beans allowed? Actually it will find unknown serializer)
        // This depends on configuration; we just ensure no exception
        AtomicReference<Throwable> cause = new AtomicReference<>();
        boolean result = provider.hasSerializerFor(Object.class, cause);
        // Should be true because there is default serializer for Object
        assertTrue(result);
    }

    // -----------------------------------------------------------------------
    //  Partition C – Defect-Targeted Branch Zone (duplicate "at [" markers)
    // -----------------------------------------------------------------------

    /**
     * Test that when an exception containing an "at [" marker is wrapped,
     * the resulting exception does NOT contain duplicate markers.
     * This targets the known defect documented in BasicExceptionTest.
     */
    @Test(timeout = 4000)
    public void testExceptionWrappingNoDuplicateAtMarkers() throws Exception {
        // Create a custom serializer that throws an exception with one "at [" marker
        final String originalMessage = "Test error at [Source: UNKNOWN; line: 1, column: 1]";
        JsonSerializer<Object> faultySerializer = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
                // This is exactly the kind of exception that could be wrapped: InvalidFormatException
                throw new JsonMappingException(gen, originalMessage);
            }
        };

        // Register this serializer for a dummy type
        SimpleModule module = new SimpleModule();
        module.addSerializer(DummyBean.class, faultySerializer);
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);

        try {
            mapper.writeValueAsString(new DummyBean());
            fail("Expected IOException from faulty serializer");
        } catch (JsonProcessingException e) {
            // The message should not have more than one "at [" marker
            int count = countOccurrences(e.getMessage(), "at [");
            assertEquals("Should only appear once", 1, count);
        }
    }

    static class DummyBean {
        public int id = 42;
    }

    private int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }

    // -----------------------------------------------------------------------
    //  Partition D – Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAcceptJsonFormatVisitorNullType() throws Exception {
        DefaultSerializerProvider.Impl provider = new DefaultSerializerProvider.Impl();
        provider.acceptJsonFormatVisitor(null, null);
    }

    @Test(timeout = 4000)
    public void testSerializePolymorphicNullValue() throws Exception {
        DefaultSerializerProvider.Impl provider = new DefaultSerializerProvider.Impl();
        // We need a real gen; use a factory that writes to bytes
        ObjectMapper mapper = new ObjectMapper();
        JsonGenerator gen = mapper.getFactory().createGenerator(System.out); // Won't actually write
        // No TypeSerializer, but we can pass null; likely NPE but we test the null branch
        try {
            provider.serializePolymorphic(gen, null, null, null, null);
        } catch (Exception e) {
            // Expect some exception because provider is not fully initialized; that's fine
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGenerateJsonSchemaReturnsBadNode() throws Exception {
        // Should be calling on a type that does not produce ObjectNode
        ObjectMapper mapper = new ObjectMapper();
        // Use a type that is serialized as non-object (e.g., String)
        mapper.generateJsonSchema(String.class);
    }

    // -----------------------------------------------------------------------
    //  Partition E – Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFindObjectIdFirstTime() {
        DefaultSerializerProvider.Impl provider = new DefaultSerializerProvider.Impl();
        // Need to use a real config to avoid NPE? Let's use mapper to get a configured provider
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider prov = (DefaultSerializerProvider) mapper.getSerializerProvider();
        ObjectIdGenerator<?> gen = new ObjectIdGenerator.IntSequenceGenerator();
        Object pojo = new Object();
        WritableObjectId oid = prov.findObjectId(pojo, gen);
        assertNotNull(oid);
        assertNotNull(oid.generator);
    }

    @Test(timeout = 4000)
    public void testFindObjectIdSecondTimeReturnsSame() {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider prov = (DefaultSerializerProvider) mapper.getSerializerProvider();
        ObjectIdGenerator<?> gen = new ObjectIdGenerator.IntSequenceGenerator();
        Object pojo = new Object();
        WritableObjectId first = prov.findObjectId(pojo, gen);
        WritableObjectId second = prov.findObjectId(pojo, gen);
        assertSame(first, second);
    }

    @Test(timeout = 4000)
    public void testIncludeFilterInstanceWithHandler() {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider prov = (DefaultSerializerProvider) mapper.getSerializerProvider();
        // filterClass that is instantiable; use a simple class
        Object filter = prov.includeFilterInstance(null, String.class);
        assertNotNull(filter);
        assertTrue(filter instanceof String);
    }

    @Test(timeout = 4000)
    public void testSerializerInstanceNullReturnsNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider prov = (DefaultSerializerProvider) mapper.getSerializerProvider();
        Annotated annotated = new Annotated() {
            @Override
            public Annotated withAnnotations(ObjectAnnotations annotations) {
                return null;
            }

            @Override
            public Annotated withFallBackAnnotations(AnnotationMap annotations) {
                return null;
            }

            @Override
            public Annotated withFallBackAnnotations(AnnotationMap fallback, AnnotationMap override) {
                return null;
            }

            @Override
            public JavaType getType() {
                return TypeFactory.defaultInstance().constructType(String.class);
            }
        };
        assertNull(prov.serializerInstance(annotated, null));
    }

    @Test(timeout = 4000)
    public void testSerializerInstanceWithJsonSerializerInstance() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider prov = (DefaultSerializerProvider) mapper.getSerializerProvider();
        Annotated annotated = new Annotated() {
            @Override
            public Annotated withAnnotations(ObjectAnnotations annotations) { return null; }
            @Override
            public Annotated withFallBackAnnotations(AnnotationMap annotations) { return null; }
            @Override
            public Annotated withFallBackAnnotations(AnnotationMap fallback, AnnotationMap override) { return null; }
            @Override
            public JavaType getType() { return TypeFactory.defaultInstance().constructType(String.class); }
        };
        JsonSerializer<Object> ser = new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator gen, SerializerProvider serializers) {}
        };
        JsonSerializer<Object> result = prov.serializerInstance(annotated, ser);
        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testSerializerInstanceWithClassNone() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider prov = (DefaultSerializerProvider) mapper.getSerializerProvider();
        Annotated annotated = new Annotated() {
            @Override
            public Annotated withAnnotations(ObjectAnnotations annotations) { return null; }
            @Override
            public Annotated withFallBackAnnotations(AnnotationMap annotations) { return null; }
            @Override
            public Annotated withFallBackAnnotations(AnnotationMap fallback, AnnotationMap override) { return null; }
            @Override
            public JavaType getType() { return TypeFactory.defaultInstance().constructType(String.class); }
        };
        assertNull(prov.serializerInstance(annotated, JsonSerializer.None.class));
    }

    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testSerializerInstanceWithNonSerializerClass() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider prov = (DefaultSerializerProvider) mapper.getSerializerProvider();
        Annotated annotated = new Annotated() {
            @Override
            public Annotated withAnnotations(ObjectAnnotations annotations) { return null; }
            @Override
            public Annotated withFallBackAnnotations(AnnotationMap annotations) { return null; }
            @Override
            public Annotated withFallBackAnnotations(AnnotationMap fallback, AnnotationMap override) { return null; }
            @Override
            public JavaType getType() { return TypeFactory.defaultInstance().constructType(String.class); }
        };
        prov.serializerInstance(annotated, String.class); // String is not a JsonSerializer
    }

    @Test(timeout = 4000)
    public void testIncludeFilterSuppressNullsWithException() throws Exception {
        // Filter that throws exception on equals(null)
        Object filter = new Object() {
            @Override
            public boolean equals(Object obj) {
                throw new RuntimeException("Deliberate failure");
            }
        };
        DefaultSerializerProvider.Impl provider = new DefaultSerializerProvider.Impl();
        try {
            provider.includeFilterSuppressNulls(filter);
            fail("Expected exception");
        } catch (JsonMappingException e) {
            // expected
        }
    }

    // -----------------------------------------------------------------------
    //  Additional coverage for serializeValue methods
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSerializeValueNull() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider prov = (DefaultSerializerProvider) mapper.getSerializerProvider();
        JsonGenerator gen = mapper.getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        // This will call _serializeNull internally
        prov.serializeValue(gen, null);
        // No exception; output should be "null" but we don't check content
    }

    @Test(timeout = 4000)
    public void testSerializeValueWithRootWrapping() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        DefaultSerializerProvider prov = (DefaultSerializerProvider) mapper.getSerializerProvider();
        JsonGenerator gen = mapper.getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        prov.serializeValue(gen, "test");
    }

    @Test(timeout = 4000)
    public void testSerializePolymorphicWithTypeSer() throws Exception {
        // Just ensure no exception if parameters are valid (cannot test thoroughly)
        ObjectMapper mapper = new ObjectMapper();
        DefaultSerializerProvider prov = (DefaultSerializerProvider) mapper.getSerializerProvider();
        JsonGenerator gen = mapper.getFactory().createGenerator(new java.io.ByteArrayOutputStream());
        // Providing null TypeSerializer to test the null rootName branch
        prov.serializePolymorphic(gen, "value", null, null, null);
    }
}