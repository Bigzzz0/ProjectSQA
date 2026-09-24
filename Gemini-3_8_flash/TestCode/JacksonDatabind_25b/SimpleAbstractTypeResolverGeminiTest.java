package com.fasterxml.jackson.databind.module;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

/* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.module.SimpleAbstractTypeResolver
 *
 * Branch Matrix:
 * 1. addMapping(Class<T> superType, Class<? extends T> subType):
 *    - Condition (superType == subType):
 *        * Branch TRUE -> IllegalArgumentException ("Can not add mapping from class to itself")
 *        * Branch FALSE -> proceeds to next check
 *    - Condition (!superType.isAssignableFrom(subType)):
 *        * Branch TRUE -> IllegalArgumentException ("not a subtype of former")
 *        * Branch FALSE -> proceeds to next check
 *    - Condition (!Modifier.isAbstract(superType.getModifiers())):
 *        * Branch TRUE -> IllegalArgumentException ("since it is not abstract")
 *        * Branch FALSE -> succeeds, puts mapping in _mappings map, returns 'this'
 *
 * 2. findTypeMapping(DeserializationConfig config, JavaType type):
 *    - Lookup in _mappings by ClassKey(type.getRawClass()):
 *        * Branch dst == null -> returns null
 *        * Branch dst != null -> converts type to target dst
 *
 * 3. resolveAbstractType(DeserializationConfig config, JavaType type):
 *    - Unconditional -> returns null
 *
 * Known Defect (Defects4J / Jackson Issue #890):
 * - TestArrayDeserialization::testByteArrayTypeOverride890
 * - In findTypeMapping(), calling type.narrowBy(dst) on an abstract type mapped to an array
 *   (e.g., Serializable -> byte[]) produces a SimpleType([B) rather than an ArrayType([B).
 * - Deserialization subsequently fails with:
 *     "JsonMappingException: Can not deserialize Class [B (of type array) as a Bean".
 * - Correct behavior constructs an ArrayType via TypeFactory, allowing proper array deserializers.
 */
public class SimpleAbstractTypeResolverGeminiTest {

    // Helper concrete subclass of non-abstract class for testing boundary validation
    private static class ConcreteArrayListSubclass extends ArrayList<Object> {
        private static final long serialVersionUID = 1L;
    }

    // Helper abstract base class
    private abstract static class CustomAbstractClass<T> {
        public T item;
    }

    // Helper concrete implementation
    private static class CustomConcreteClass extends CustomAbstractClass<String> {
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddMappingAndFindTypeMappingBasic() {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        assertSame("addMapping should return this for chaining",
                resolver, resolver.addMapping(List.class, LinkedList.class));

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType listType = mapper.getTypeFactory().constructType(List.class);

        JavaType resolvedType = resolver.findTypeMapping(config, listType);
        assertNotNull("Resolved type must not be null for registered mapping", resolvedType);
        assertEquals("Target raw class must match mapped subtype",
                LinkedList.class, resolvedType.getRawClass());
    }

    @Test(timeout = 4000)
    public void testAddMappingChainingAndMultipleLookups() {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(List.class, ArrayList.class)
                .addMapping(Map.class, HashMap.class)
                .addMapping(Collection.class, LinkedList.class);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        TypeFactory tf = mapper.getTypeFactory();

        JavaType resolvedList = resolver.findTypeMapping(config, tf.constructType(List.class));
        assertNotNull(resolvedList);
        assertEquals(ArrayList.class, resolvedList.getRawClass());

        JavaType resolvedMap = resolver.findTypeMapping(config, tf.constructType(Map.class));
        assertNotNull(resolvedMap);
        assertEquals(HashMap.class, resolvedMap.getRawClass());

        JavaType resolvedCollection = resolver.findTypeMapping(config, tf.constructType(Collection.class));
        assertNotNull(resolvedCollection);
        assertEquals(LinkedList.class, resolvedCollection.getRawClass());
    }

    @Test(timeout = 4000)
    public void testFindTypeMappingWithGenericsPreserved() {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(List.class, LinkedList.class);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType listOfStrings = mapper.getTypeFactory().constructCollectionType(List.class, String.class);

        JavaType resolved = resolver.findTypeMapping(config, listOfStrings);
        assertNotNull(resolved);
        assertEquals(LinkedList.class, resolved.getRawClass());
        assertNotNull(resolved.getContentType());
        assertEquals(String.class, resolved.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testAddMappingAbstractSuperclassNonInterface() {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(CustomAbstractClass.class, CustomConcreteClass.class);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType abstractType = mapper.getTypeFactory().constructType(CustomAbstractClass.class);

        JavaType resolved = resolver.findTypeMapping(config, abstractType);
        assertNotNull(resolved);
        assertEquals(CustomConcreteClass.class, resolved.getRawClass());
    }

    @Test(timeout = 4000)
    public void testAddMappingAbstractListToSubclass() {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(AbstractList.class, ArrayList.class);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType abstractListType = mapper.getTypeFactory().constructType(AbstractList.class);

        JavaType resolved = resolver.findTypeMapping(config, abstractListType);
        assertNotNull(resolved);
        assertEquals(ArrayList.class, resolved.getRawClass());
    }

    @Test(timeout = 4000)
    public void testResolveAbstractTypeAlwaysReturnsNull() {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(List.class, ArrayList.class);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType listType = mapper.getTypeFactory().constructType(List.class);

        assertNull("resolveAbstractType must unconditionally return null",
                resolver.resolveAbstractType(config, listType));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFindTypeMappingUnmappedTypeReturnsNull() {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(List.class, ArrayList.class);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType setType = mapper.getTypeFactory().constructType(Set.class);

        assertNull("Unmapped type must return null from findTypeMapping",
                resolver.findTypeMapping(config, setType));
    }

    @Test(timeout = 4000)
    public void testFindTypeMappingEmptyResolverReturnsNull() {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType mapType = mapper.getTypeFactory().constructType(Map.class);

        assertNull("Empty resolver must return null for any type lookup",
                resolver.findTypeMapping(config, mapType));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Jackson Issue #890 / D4J)
    // =========================================================================

    @Test(timeout = 4000)
    public void testByteArrayTypeOverride890_DirectTypeCheck() {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(Serializable.class, byte[].class);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType serializableType = mapper.getTypeFactory().constructType(Serializable.class);

        JavaType resolved = resolver.findTypeMapping(config, serializableType);
        assertNotNull("Resolved type must not be null for Serializable -> byte[] mapping", resolved);
        assertTrue("Resolved type for byte[] must report isArrayType() == true", resolved.isArrayType());
        assertEquals("Raw class must be byte[]", byte[].class, resolved.getRawClass());
    }

    @Test(timeout = 4000)
    public void testByteArrayTypeOverride890_SerializableDeserialization() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(Serializable.class, byte[].class);

        SimpleModule module = new SimpleModule();
        module.setAbstractTypes(resolver);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);

        byte[] original = new byte[] { 1, 2, 3, 4, 127, -128 };
        String json = mapper.writeValueAsString(original);

        Object deserialized = mapper.readValue(json, Serializable.class);
        assertNotNull("Deserialized object must not be null", deserialized);
        assertTrue("Deserialized object must be byte[]", deserialized instanceof byte[]);
        assertArrayEquals(original, (byte[]) deserialized);
    }

    @Test(timeout = 4000)
    public void testByteArrayTypeOverride890_CloneableDeserialization() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(Cloneable.class, byte[].class);

        SimpleModule module = new SimpleModule();
        module.setAbstractTypes(resolver);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);

        byte[] original = new byte[] { 42, 84, 0, -1 };
        String json = mapper.writeValueAsString(original);

        Object deserialized = mapper.readValue(json, Cloneable.class);
        assertNotNull("Deserialized object must not be null", deserialized);
        assertTrue("Deserialized object must be byte[]", deserialized instanceof byte[]);
        assertArrayEquals(original, (byte[]) deserialized);
    }

    @Test(timeout = 4000)
    public void testStringArrayTypeOverride() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(Serializable.class, String[].class);

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        JavaType serializableType = mapper.getTypeFactory().constructType(Serializable.class);

        JavaType resolved = resolver.findTypeMapping(config, serializableType);
        assertNotNull(resolved);
        assertTrue("Resolved type for String[] must be an array type", resolved.isArrayType());
        assertEquals(String[].class, resolved.getRawClass());

        SimpleModule module = new SimpleModule();
        module.setAbstractTypes(resolver);
        mapper.registerModule(module);

        String[] original = new String[] { "hello", "world" };
        String json = mapper.writeValueAsString(original);
        Object deserialized = mapper.readValue(json, Serializable.class);
        assertTrue(deserialized instanceof String[]);
        assertArrayEquals(original, (String[]) deserialized);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testAddMappingSameClassThrowsException() {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        try {
            resolver.addMapping(CharSequence.class, CharSequence.class);
            fail("Expected IllegalArgumentException when mapping class to itself");
        } catch (IllegalArgumentException e) {
            assertEquals("Can not add mapping from class to itself", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testAddMappingNotSubtypeThrowsException() {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        Class superType = List.class;
        Class nonSubType = String.class;
        try {
            resolver.addMapping(superType, nonSubType);
            fail("Expected IllegalArgumentException when target is not a subtype of source");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should indicate incompatibility",
                    e.getMessage().contains("as latter is not a subtype of former"));
        }
    }

    @Test(timeout = 4000)
    public void testAddMappingNonAbstractSuperTypeThrowsException() {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        try {
            resolver.addMapping(ArrayList.class, ConcreteArrayListSubclass.class);
            fail("Expected IllegalArgumentException when superType is not abstract");
        } catch (IllegalArgumentException e) {
            assertTrue("Exception message should indicate non-abstract super type",
                    e.getMessage().contains("since it is not abstract"));
        }
    }

    @Test(timeout = 4000)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testAddMappingNullSuperTypeThrowsNpe() {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        try {
            resolver.addMapping(null, (Class) ArrayList.class);
            fail("Expected NullPointerException when superType is null");
        } catch (NullPointerException expected) {
            // Expected behavior
        }
    }

    @Test(timeout = 4000)
    public void testAddMappingNullSubTypeThrowsNpe() {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        try {
            resolver.addMapping(List.class, null);
            fail("Expected NullPointerException when subType is null");
        } catch (NullPointerException expected) {
            // Expected behavior
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationRoundtrip() throws Exception {
        SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();
        resolver.addMapping(List.class, LinkedList.class)
                .addMapping(Map.class, HashMap.class);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(resolver);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserializedObj = ois.readObject();
        ois.close();

        assertNotNull(deserializedObj);
        assertTrue(deserializedObj instanceof SimpleAbstractTypeResolver);
        SimpleAbstractTypeResolver deserializedResolver = (SimpleAbstractTypeResolver) deserializedObj;

        ObjectMapper mapper = new ObjectMapper();
        DeserializationConfig config = mapper.getDeserializationConfig();
        TypeFactory tf = mapper.getTypeFactory();

        JavaType resolvedList = deserializedResolver.findTypeMapping(config, tf.constructType(List.class));
        assertNotNull(resolvedList);
        assertEquals(LinkedList.class, resolvedList.getRawClass());

        JavaType resolvedMap = deserializedResolver.findTypeMapping(config, tf.constructType(Map.class));
        assertNotNull(resolvedMap);
        assertEquals(HashMap.class, resolvedMap.getRawClass());
    }
}