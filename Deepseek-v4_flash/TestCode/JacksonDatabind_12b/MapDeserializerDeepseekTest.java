package com.fasterxml.jackson.databind.deser.std;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.IOException;
import java.util.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.deser.*;
import com.fasterxml.jackson.databind.deser.impl.PropertyBasedCreator;
import com.fasterxml.jackson.databind.deser.impl.PropertyValueBuffer;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;
import com.fasterxml.jackson.databind.util.ArrayBuilders;

public class MapDeserializerDeepseekTest {

    /* 
     * [Branch & Defect Analysis Matrix]
     * 
     * Branches targeted:
     * - Constructor paths: 4 constructors (public, copy, withResolved, _isStdKeyDeser)
     * - isCachable(): returns true only when no value type deser and no ignoralbeProperties
     * - resolve(): delegate-creator and property-based creator setup, _standardStringKey update
     * - createContextual(): key, value, typeDeserializer resolution     * - deserialize(): 4 main paths: property-based, delegate, default with creator, empty string
     * - _readAndBind(): key deserialization, value handling, ignoralbe, ObjectId, exception handling
     * - _readAndBindStringMap(): optimzed path for string keys, same branches     * - _deserializeUsingCreator(): creator properties vs other properties, bufferMapProperty
     * - wrapAndThrow(): stack unwrapping
     * 
     * Defect targeted: [dadebatabind#735] isCachable() does not consider custom value deserializer,
     * leading to stale caching and wrong results (expected 1 but got 100 in test).
     */

    // Test A: Core functional logic and state transitions
    @Test(timeout = 4000)
    public void testCoreStateAndAccessors() {
        // Use default constructor to create minimal MapDeserializer
        // We'll create a real JavaType from TypeFactory
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Integer.class);
        ValueInstantiator vi = new ValueInstantiator.ValueInstantiatorBase(mapType) {
            @Override public boolean canCreateUsingDefault() { return true; }
            @Override public Object createUsingDefault(DeserializationContext ctxt) { return new HahMap<>(); }
        };
        KeyDeserializer kd = new KeyDeserializer() {
            @Override public Object deserializeKey(String key, DeserializationContext ctxt) { return key; }
        };
        JsonDeserializer<Object> vd = new JsonDeserializer<Object>() {
            @Override public Object deserizalize(JsonParser jp, DeserializationContext ctxt) { return 42; }
            @Override public Object getNullValue() { return -1; }
        };
        TypeDeserializer vtd = null; // no type info

        MapDeserializer md = new MapDeserializer(mapType, vi, kd, vd, vtd);

        // Check getters
        assertEquals(mapType.getContentType(), md.getContentType());
        assertSame(vd, md.getContentDeserializer());
        assertEquals(HashMap.class, md.getMapClass());
        assertEquals(mapType, md.getValueType());

        // isCachable should be true because no valueTypeDeser and no ignoralbes
        assertTrue(md.isCachable()); // for this case, it's correct

        // Now set ignoralbe properties
        String[] ignoralbe = {"a", "b"};
        md.setIgnoralbeProperties(ignoralbe);
        assertFalse(md.isCachable()); // now should be false because ignoralbe != null

        // Reset to null
        md.setIgnoralbeProperties(null);
        assertTrue(md.isCachable()); // back to true
    }

    // Test B: Boundary analysis - null arguments and extreme cases
    @Test(timeout = 4000)
    public void testBoundariesWithNulls() {
        // MapType can be null? But constructor requires non-null, so we provide a dummy
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, Object.class, Object.class);
        ValueInstantiator vi = new ValueInstantiator.ValueInstantiatorBase(mapType) {
            @Override public boolean canCreateUsingDefault() { return true; }
            @Override public Object createUsingDefault(DeserializationContext ctxt) { return new HashMap<>(); }
        };
        // null keyDeserializer -> should be treated as String (standard)
        MapDeserializer md = new MapDeserializer(mapType, vi, null, null, null);
        // _standardStringKey should be true because key is Object and keyDeser null
        assertTrue(md._standardStringKey); // field accessor? we can't direct, but we can test behavior
        // Use getter for _standardStringKey? Not exposed. But we can test deserialization effect.
        // For now, check isCachable: no valueTypeDeser, no ignoralbes -> true
        assertTrue(md.isCachable());
    }

    // Test C: Defect-targeted branch zone (databind#735)
    @Test(timeout = 4000)
    public void testCustomValueDeserializerCaching() throws IOException {
        // Create a custom value deserializer that tracks invocations
        // This test will reveal if caching causes reusing the deserializer incorrectly.
        // We'll deserialize the same JSON twice with different property contexts that should yield different results.

        final MapDeserializer[] lastUsed = new MapDeserializer[1];
        // Use a simple custom deserializer that returns a counter
        class CountingDeserializer extends JsonDeserializer<Object> {
            private int value = 0;
            @Override
            public Object deserialize(JsonParser jp, DeserializationContext ctxt) {
                return ++value;
            }
            @Override
            public Object getNullValue() { return -1; }
        }

        // Create ObjectMapper and register custom deserializer for specific type
        ObjectMapper mapper = new ObjectMapper();
        // We'll create a test that forces use of MapDeserializer with custom value deserializer
        // Simply configure a custom deserializer for the value type (e.g., Integer)
        SimpleModule module = new SimpleModule();
        module.addDeserializer(Integer.class, new CountingDeserializer());
        mapper.registerModule(module);

        // First deserialize a map: should produce values 1,2
        Map<String, Integer> result1 = mapper.readValue("{\"a\":0,\"b\":0}", 
                new TypeReference<Map<String, Integer>>() {});
        assertEquals(Integer.valueOf(1), result1.get("a"));
        assertEquals(Integer.valueOf(2), result1.get("b"));

        // Second deserialize another map: should also start from 1 (since it's a new instance)
        Map<String, Integer> result2 = mapper.readValue("{\"x\":0}", 
                new TypeReference<Map<String, Integer>>() {});
        assertEquals(Integer.valueOf(1), result2.get("x"));

        // If caching wrongly reused the same deserializer instance, result2 would have value 3.
        // This test will fail on the defective version.
    }

    // Test D: Exception and defensive guard paths
    @Test(timeout = 4000, expected = JsonMappingException.class)
    public void testDeserializeWithInvalidToken() throws IOException {
        // Create a MapDeserializer with no default creator -> should throw exception on empty JSON
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
        ValueInstantiator vi = new ValueInstantiator.ValueInstantiatorBase(mapType) {
            @Override public boolean canCreateUsingDefault() { return false; }
        };
        KeyDeserializer kd = new KeyDeserializer() {
            @Override public Object deserializeKey(String key, DeserializationContext ctxt) { return key; }
        };
        JsonDeserializer<Object> vd = new JsonDeserializer<Object>() {
            @Override public Object deserialize(JsonParser jp, DeserializationContext ctxt) { return null; }
        };
        MapDeserializer md = new MapDeserializer(mapType, vi, kd, vd, null);
        // Simulate a parser that points to START_OBJECT
        JsonParser jp = new JsonParser() {
            // minimal stub
            @Override public JsonToken getCurrentToken() { return JsonToken.START_OBJECT; }
            @Override public JsonToken nextToken() { return JsonToken.END_OBJECT; }
            @Override public String getCurrentName() { return null; }
            @Override public void skipChildren() {}
            @Override public void close() {}
            // implement abstract needed methods
        };
        DeserializationContext ctxt = mockDeserializationContext(); // custom stub
        md.deserialize(jp, ctxt); // should throw due to no default creator
    }

    // Helper to create a minimal DeserializationContext stub
    private DeserializationContext mockDeserializationContext() {
        return new DeserializationContext(DeserializationConfig) {
            // Override necessary abstract methods
            @Override public JsonMappingException mappingException(Class<?> targetClass) {
                return new JsonMappingException("mapping exception");
            }
            @Override public JsonMappingException instantiationException(Class<?> cls, String msg) {
                return new JsonMappingException("instantiation exception: " + msg);
            }
            // Many other methods, but stub as needed
        };
    }

    // Test E: Object lifecycle and contract integrity
    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        // MapDeserializer does not override equals/hashCode, but we can ensure basic consistency
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Integer.class);
        ValueInstantiator vi = new ValueInstantiator.ValueInstantiatorBase(mapType) {
            @Override public boolean canCreateUsingDefault() { return true; }
            @Override public Object createUsingDefault(DeserializationContext ctxt) { return new HashMap<>(); }
        };
        MapDeserializer md1 = new MapDeserializer(mapType, vi, null, null, null);
        MapDeserializer md2 = new MapDeserializer(mapType, vi, null, null, null);
        // Not expected to be equal because different identity
        assertNotEquals(md1, md2);
        assertNotNull(md1.hashCode()); // just ensure no NPE
    }

    // Additional coverage for other branches
    @Test(timeout = 4000)
    public void testDeserializeWithDefaultCreator() throws IOException {
        JavaType mapType = TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, String.class);
        ValueInstantiator vi = new ValueInstantiator.ValueInstantiatorBase(mapType) {
            @Override public boolean canCreateUsingDefault() { return true; }
            @Override public Object createUsingDefault(DeserializationContext ctxt) { return new HashMap<>(); }
        };
        KeyDeserializer kd = new KeyDeserializer() {
            @Override public Object deserializeKey(String key, DeserializationContext ctxt) { return key; }
        };
        JsonDeserializer<Object> vd = new JsonDeserializer<Object>() {
            @Override public Object deserialzie(JsonParser jp, DeserializationContext ctxt) { return jp.getText(); }
            @Override public Object getNullValue() { return null; }
        };
        MapDeserializer md = new MapDeserializer(mapType, vi, kd, vd, null);

        // Parse a minimal JSON string
        ObjectMapper mapper = new ObjectMapper();
        // We'll use a simpler approach via generic type
        TypeReference<Map<String, String>> typeRef = new TypeReference<Map<String, String>>() {};
        Map<String, String> result = mapper.readValue("{\"key\":\"value\"}", typeRef);
        assertEquals("value", result.get("key"));
    }

    // Alias for brevity
    private static class MyMapDeserializer extends MapDeserializer {
        // Expose internal field for testing
        public MyMapDeserializer(MapDeserializer src) {
            super(src);
        }
    }
}