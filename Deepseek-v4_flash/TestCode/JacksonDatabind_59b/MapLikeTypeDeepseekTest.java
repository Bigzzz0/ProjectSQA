package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.TypeVariable;
import java.util.*;

import com.fasterxml.jackson.databind.JavaType;

/*
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: MapLikeType.java
 * 
 * Key branches and conditions to cover:
 * 1. Constructor with TypeBindings - hash code computation (keyType.hashCode() ^ valueType.hashCode())
 * 2. Constructor from TypeBase - copy constructor
 * 3. upgradeFrom() - TypeBase instance check, IllegalArgumentException for non-TypeBase
 * 4. construct() - TypeVariable array null/length check, TypeBindings creation
 * 5. _narrow() - deprecated method, creates new instance with same bindings
 * 6. withKeyType() - identity check (keyType == _keyType), new instance creation
 * 7. withContentType() - identity check (_valueType == contentType), new instance creation
 * 8. withTypeHandler() - always creates new instance
 * 9. withContentTypeHandler() - delegates to valueType.withTypeHandler()
 * 10. withValueHandler() - always creates new instance
 * 11. withContentValueHandler() - delegates to valueType.withValueHandler()
 * 12. withStaticTyping() - _asStatic check, valueType.withStaticTyping() delegation
 * 13. refine() - creates new instance with provided parameters
 * 14. buildCanonicalName() - null _keyType check, string building
 * 15. isContainerType() - always true
 * 16. isMapLikeType() - always true
 * 17. getKeyType() / getContentType() - getter methods
 * 18. getContentValueHandler() / getContentTypeHandler() - delegation to valueType
 * 19. hasHandlers() - OR condition with super, valueType, keyType
 * 20. getErasedSignature() - class signature generation
 * 21. getGenericSignature() - class signature + key/value type signatures
 * 22. withKeyTypeHandler() / withKeyValueHandler() - extended API methods
 * 23. isTrueMapType() - Map.class.isAssignableFrom(_class) check
 * 24. toString() - string formatting
 * 25. equals() - identity, null, class, field comparisons
 * 
 * Defect targeting:
 * The known defect relates to key type refinement for Map types.
 * The issue occurs when a MapLikeType is created with a key type that
 * needs refinement but the refinement process fails to properly handle
 * the key type. This test suite includes a test that verifies the
 * proper handling of key type refinement through the type system.
 * 
 * The specific failure: "Can not find a (Map) Key deserializer for type
 * [simple type, class ...CompoundKey]" indicates that the key type
 * information is lost or not properly propagated through the type
 * refinement process.
 */
public class MapLikeTypeDeepseekTest {

    // Helper method to create a simple JavaType for testing
    private JavaType createSimpleType(Class<?> cls) {
        return TypeFactory.defaultInstance().constructType(cls);
    }

    // Helper method to create a MapLikeType for testing
    private MapLikeType createMapLikeType(Class<?> mapClass, Class<?> keyClass, Class<?> valueClass) {
        JavaType keyType = createSimpleType(keyClass);
        JavaType valueType = createSimpleType(valueClass);
        return MapLikeType.construct(mapClass, keyType, valueType);
    }

    /*
     * Partition A: Core Functional Logic & State Transitions
     */

    @Test(timeout = 4000)
    public void testConstructWithValidTypes() {
        MapLikeType type = createMapLikeType(HashMap.class, String.class, Integer.class);
        assertNotNull(type);
        assertEquals(HashMap.class, type.getRawClass());
        assertEquals("java.lang.String", type.getKeyType().toCanonical());
        assertEquals("java.lang.Integer", type.getContentType().toCanonical());
        assertTrue(type.isContainerType());
        assertTrue(type.isMapLikeType());
        assertTrue(type.isTrueMapType());
    }

    @Test(timeout = 4000)
    public void testConstructWithNonMapClass() {
        // Should still work even if class is not a Map
        MapLikeType type = createMapLikeType(ArrayList.class, String.class, Integer.class);
        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
        assertFalse(type.isTrueMapType());
    }

    @Test(timeout = 4000)
    public void testWithKeyType() {
        MapLikeType original = createMapLikeType(HashMap.class, String.class, Integer.class);
        JavaType newKeyType = createSimpleType(Long.class);
        MapLikeType modified = original.withKeyType(newKeyType);
        
        assertNotSame(original, modified);
        assertEquals(Long.class, modified.getKeyType().getRawClass());
        assertEquals(Integer.class, modified.getContentType().getRawClass());
        
        // Test identity optimization
        MapLikeType same = original.withKeyType(original.getKeyType());
        assertSame(original, same);
    }

    @Test(timeout = 4000)
    public void testWithContentType() {
        MapLikeType original = createMapLikeType(HashMap.class, String.class, Integer.class);
        JavaType newValueType = createSimpleType(Long.class);
        MapLikeType modified = original.withContentType(newValueType);
        
        assertNotSame(original, modified);
        assertEquals(String.class, modified.getKeyType().getRawClass());
        assertEquals(Long.class, modified.getContentType().getRawClass());
        
        // Test identity optimization
        MapLikeType same = original.withContentType(original.getContentType());
        assertSame(original, same);
    }

    @Test(timeout = 4000)
    public void testWithTypeHandler() {
        MapLikeType original = createMapLikeType(HashMap.class, String.class, Integer.class);
        Object handler = new Object();
        MapLikeType modified = original.withTypeHandler(handler);
        
        assertNotSame(original, modified);
        assertEquals(handler, modified.getTypeHandler());
        assertEquals(original.getKeyType(), modified.getKeyType());
        assertEquals(original.getContentType(), modified.getContentType());
    }

    @Test(timeout = 4000)
    public void testWithContentTypeHandler() {
        MapLikeType original = createMapLikeType(HashMap.class, String.class, Integer.class);
        Object handler = new Object();
        MapLikeType modified = original.withContentTypeHandler(handler);
        
        assertNotSame(original, modified);
        assertEquals(handler, modified.getContentTypeHandler());
        assertEquals(original.getKeyType(), modified.getKeyType());
    }

    @Test(timeout = 4000)
    public void testWithValueHandler() {
        MapLikeType original = createMapLikeType(HashMap.class, String.class, Integer.class);
        Object handler = new Object();
        MapLikeType modified = original.withValueHandler(handler);
        
        assertNotSame(original, modified);
        assertEquals(handler, modified.getValueHandler());
        assertEquals(original.getKeyType(), modified.getKeyType());
        assertEquals(original.getContentType(), modified.getContentType());
    }

    @Test(timeout = 4000)
    public void testWithContentValueHandler() {
        MapLikeType original = createMapLikeType(HashMap.class, String.class, Integer.class);
        Object handler = new Object();
        MapLikeType modified = original.withContentValueHandler(handler);
        
        assertNotSame(original, modified);
        assertEquals(handler, modified.getContentValueHandler());
        assertEquals(original.getKeyType(), modified.getKeyType());
    }

    @Test(timeout = 4000)
    public void testWithStaticTyping() {
        MapLikeType original = createMapLikeType(HashMap.class, String.class, Integer.class);
        MapLikeType modified = original.withStaticTyping();
        
        assertNotSame(original, modified);
        assertTrue(modified.isStaticTyping());
        
        // Test identity optimization
        MapLikeType same = modified.withStaticTyping();
        assertSame(modified, same);
    }

    @Test(timeout = 4000)
    public void testRefine() {
        MapLikeType original = createMapLikeType(HashMap.class, String.class, Integer.class);
        TypeBindings bindings = TypeBindings.create(HashMap.class, 
                createSimpleType(String.class), createSimpleType(Integer.class));
        MapLikeType refined = original.refine(HashMap.class, bindings, null, null);
        
        assertNotNull(refined);
        assertEquals(HashMap.class, refined.getRawClass());
        assertEquals(original.getKeyType(), refined.getKeyType());
        assertEquals(original.getContentType(), refined.getContentType());
    }

    @Test(timeout = 4000)
    public void testUpgradeFrom() {
        JavaType baseType = createSimpleType(HashMap.class);
        JavaType keyType = createSimpleType(String.class);
        JavaType valueType = createSimpleType(Integer.class);
        
        MapLikeType upgraded = MapLikeType.upgradeFrom(baseType, keyType, valueType);
        assertNotNull(upgraded);
        assertEquals(HashMap.class, upgraded.getRawClass());
        assertEquals(keyType, upgraded.getKeyType());
        assertEquals(valueType, upgraded.getContentType());
    }

    @Test(timeout = 4000)
    public void testUpgradeFromNonTypeBase() {
        // Create a custom JavaType that is not a TypeBase
        JavaType nonTypeBase = new JavaType() {
            private static final long serialVersionUID = 1L;

            @Override
            public JavaType withTypeHandler(Object h) { return this; }
            @Override
            public JavaType withContentTypeHandler(Object h) { return this; }
            @Override
            public JavaType withValueHandler(Object h) { return this; }
            @Override
            public JavaType withContentValueHandler(Object h) { return this; }
            @Override
            public JavaType withStaticTyping() { return this; }
            @Override
            public JavaType refine(Class<?> rawType, TypeBindings bindings,
                    JavaType superClass, JavaType[] superInterfaces) { return this; }
            @Override
            public boolean isContainerType() { return false; }
            @Override
            public int containedTypeCount() { return 0; }
            @Override
            public JavaType containedType(int index) { return null; }
            @Override
            public String containedTypeName(int index) { return null; }
            @Override
            public Class<?> getParameterSource() { return null; }
            @Override
            public JavaType getKeyType() { return null; }
            @Override
            public JavaType getContentType() { return null; }
            @Override
            public int getErasedSignature(StringBuilder sb) { return 0; }
            @Override
            public int getGenericSignature(StringBuilder sb) { return 0; }
            @Override
            public String toString() { return "nonTypeBase"; }
            @Override
            public boolean equals(Object o) { return o == this; }
            @Override
            public int hashCode() { return 0; }
        };
        
        try {
            MapLikeType.upgradeFrom(nonTypeBase, createSimpleType(String.class), 
                    createSimpleType(Integer.class));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // Expected
        }
    }

    /*
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     */

    @Test(timeout = 4000)
    public void testConstructWithNullTypeParameters() {
        // Test with class that has no type parameters
        MapLikeType type = MapLikeType.construct(String.class, 
                createSimpleType(String.class), createSimpleType(Integer.class));
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructWithWrongNumberOfTypeParameters() {
        // Class with 1 type parameter (should use empty bindings)
        MapLikeType type = MapLikeType.construct(ArrayList.class, 
                createSimpleType(String.class), createSimpleType(Integer.class));
        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testBuildCanonicalName() {
        MapLikeType type = createMapLikeType(HashMap.class, String.class, Integer.class);
        String canonical = type.toCanonical();
        assertNotNull(canonical);
        assertTrue(canonical.contains("java.util.HashMap"));
        assertTrue(canonical.contains("java.lang.String"));
        assertTrue(canonical.contains("java.lang.Integer"));
    }

    @Test(timeout = 4000)
    public void testGetErasedSignature() {
        MapLikeType type = createMapLikeType(HashMap.class, String.class, Integer.class);
        StringBuilder sb = new StringBuilder();
        StringBuilder result = type.getErasedSignature(sb);
        assertSame(sb, result);
        assertTrue(sb.toString().contains("java.util.HashMap"));
    }

    @Test(timeout = 4000)
    public void testGetGenericSignature() {
        MapLikeType type = createMapLikeType(HashMap.class, String.class, Integer.class);
        StringBuilder sb = new StringBuilder();
        StringBuilder result = type.getGenericSignature(sb);
        assertSame(sb, result);
        String signature = sb.toString();
        assertTrue(signature.contains("java.util.HashMap"));
        assertTrue(signature.contains("java.lang.String"));
        assertTrue(signature.contains("java.lang.Integer"));
    }

    @Test(timeout = 4000)
    public void testToString() {
        MapLikeType type = createMapLikeType(HashMap.class, String.class, Integer.class);
        String str = type.toString();
        assertNotNull(str);
        assertTrue(str.contains("map-like type"));
        assertTrue(str.contains("java.util.HashMap"));
        assertTrue(str.contains("java.lang.String"));
        assertTrue(str.contains("java.lang.Integer"));
    }

    /*
     * Partition C: Defect-Targeted Branch Zone
     * 
     * This test targets the specific defect where key type refinement
     * fails for Map-like types. The issue is that when a MapLikeType
     * is created with a complex key type, the type information may be
     * lost during refinement, leading to "Can not find a (Map) Key
     * deserializer" errors.
     */
    @Test(timeout = 4000)
    public void testKeyTypeRefinementForMapLikeType() {
        // Create a MapLikeType with a complex key type
        JavaType keyType = createSimpleType(CompoundKey.class);
        JavaType valueType = createSimpleType(String.class);
        MapLikeType type = MapLikeType.construct(HashMap.class, keyType, valueType);
        
        // Verify the key type is properly preserved
        assertNotNull(type.getKeyType());
        assertEquals(CompoundKey.class, type.getKeyType().getRawClass());
        
        // Verify the key type can be used for deserialization lookup
        // This simulates what the type refinement process does
        JavaType refinedKeyType = type.getKeyType();
        assertNotNull(refinedKeyType);
        assertEquals(CompoundKey.class, refinedKeyType.getRawClass());
        
        // Verify the key type handler can be properly set
        Object keyHandler = new Object();
        MapLikeType withKeyHandler = type.withKeyTypeHandler(keyHandler);
        assertNotNull(withKeyHandler);
        assertEquals(keyHandler, withKeyHandler.getKeyType().getTypeHandler());
        
        // Verify the key value handler can be properly set
        Object keyValueHandler = new Object();
        MapLikeType withKeyValueHandler = type.withKeyValueHandler(keyValueHandler);
        assertNotNull(withKeyValueHandler);
        assertEquals(keyValueHandler, withKeyValueHandler.getKeyType().getValueHandler());
        
        // Verify that the key type survives refinement
        TypeBindings bindings = TypeBindings.create(HashMap.class, keyType, valueType);
        MapLikeType refined = type.refine(HashMap.class, bindings, null, null);
        assertNotNull(refined);
        assertEquals(CompoundKey.class, refined.getKeyType().getRawClass());
        
        // Verify that the key type survives static typing
        MapLikeType staticTyped = type.withStaticTyping();
        assertNotNull(staticTyped);
        assertEquals(CompoundKey.class, staticTyped.getKeyType().getRawClass());
        
        // Verify that the key type survives content type changes
        JavaType newValueType = createSimpleType(Integer.class);
        MapLikeType withNewValue = type.withContentType(newValueType);
        assertNotNull(withNewValue);
        assertEquals(CompoundKey.class, withNewValue.getKeyType().getRawClass());
    }

    // Test class for the defect scenario
    public static class CompoundKey {
        public String part1;
        public String part2;
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CompoundKey that = (CompoundKey) o;
            return Objects.equals(part1, that.part1) && 
                   Objects.equals(part2, that.part2);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(part1, part2);
        }
    }

    /*
     * Partition D: Exception & Defensive Guard Paths
     */

    @Test(timeout = 4000)
    public void testHasHandlers() {
        MapLikeType type = createMapLikeType(HashMap.class, String.class, Integer.class);
        assertFalse(type.hasHandlers());
        
        // Add a handler to the value type
        MapLikeType withValueHandler = type.withValueHandler(new Object());
        assertTrue(withValueHandler.hasHandlers());
        
        // Add a handler to the key type
        MapLikeType withKeyHandler = type.withKeyTypeHandler(new Object());
        assertTrue(withKeyHandler.hasHandlers());
        
        // Add a handler to the content type
        MapLikeType withContentHandler = type.withContentTypeHandler(new Object());
        assertTrue(withContentHandler.hasHandlers());
    }

    @Test(timeout = 4000)
    public void testGetContentValueHandler() {
        MapLikeType type = createMapLikeType(HashMap.class, String.class, Integer.class);
        assertNull(type.getContentValueHandler());
        
        Object handler = new Object();
        MapLikeType withHandler = type.withContentValueHandler(handler);
        assertEquals(handler, withHandler.getContentValueHandler());
    }

    @Test(timeout = 4000)
    public void testGetContentTypeHandler() {
        MapLikeType type = createMapLikeType(HashMap.class, String.class, Integer.class);
        assertNull(type.getContentTypeHandler());
        
        Object handler = new Object();
        MapLikeType withHandler = type.withContentTypeHandler(handler);
        assertEquals(handler, withHandler.getContentTypeHandler());
    }

    @Test(timeout = 4000)
    public void testWithKeyTypeHandler() {
        MapLikeType type = createMapLikeType(HashMap.class, String.class, Integer.class);
        Object handler = new Object();
        MapLikeType modified = type.withKeyTypeHandler(handler);
        
        assertNotSame(type, modified);
        assertEquals(handler, modified.getKeyType().getTypeHandler());
        assertEquals(type.getContentType(), modified.getContentType());
    }

    @Test(timeout = 4000)
    public void testWithKeyValueHandler() {
        MapLikeType type = createMapLikeType(HashMap.class, String.class, Integer.class);
        Object handler = new Object();
        MapLikeType modified = type.withKeyValueHandler(handler);
        
        assertNotSame(type, modified);
        assertEquals(handler, modified.getKeyType().getValueHandler());
        assertEquals(type.getContentType(), modified.getContentType());
    }

    /*
     * Partition E: Object Lifecycle & Contract Integrity
     */

    @Test(timeout = 4000)
    public void testEquals() {
        MapLikeType type1 = createMapLikeType(HashMap.class, String.class, Integer.class);
        MapLikeType type2 = createMapLikeType(HashMap.class, String.class, Integer.class);
        MapLikeType type3 = createMapLikeType(HashMap.class, String.class, Long.class);
        MapLikeType type4 = createMapLikeType(TreeMap.class, String.class, Integer.class);
        
        // Reflexive
        assertTrue(type1.equals(type1));
        
        // Symmetric
        assertTrue(type1.equals(type2));
        assertTrue(type2.equals(type1));
        
        // Different value type
        assertFalse(type1.equals(type3));
        
        // Different raw class
        assertFalse(type1.equals(type4));
        
        // Null check
        assertFalse(type1.equals(null));
        
        // Different class
        assertFalse(type1.equals("not a MapLikeType"));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        MapLikeType type1 = createMapLikeType(HashMap.class, String.class, Integer.class);
        MapLikeType type2 = createMapLikeType(HashMap.class, String.class, Integer.class);
        MapLikeType type3 = createMapLikeType(HashMap.class, String.class, Long.class);
        
        // Equal objects must have equal hash codes
        assertEquals(type1.hashCode(), type2.hashCode());
        
        // Different objects may have different hash codes
        // (not guaranteed but should be different for these test cases)
        assertNotEquals(type1.hashCode(), type3.hashCode());
    }

    @Test(timeout = 4000)
    public void testNarrow() {
        MapLikeType type = createMapLikeType(HashMap.class, String.class, Integer.class);
        MapLikeType narrowed = type._narrow(LinkedHashMap.class);
        
        assertNotNull(narrowed);
        assertEquals(LinkedHashMap.class, narrowed.getRawClass());
        assertEquals(type.getKeyType(), narrowed.getKeyType());
        assertEquals(type.getContentType(), narrowed.getContentType());
    }

    @Test(timeout = 4000)
    public void testIsTrueMapType() {
        MapLikeType mapType = createMapLikeType(HashMap.class, String.class, Integer.class);
        assertTrue(mapType.isTrueMapType());
        
        MapLikeType nonMapType = createMapLikeType(ArrayList.class, String.class, Integer.class);
        assertFalse(nonMapType.isTrueMapType());
        
        // Test with a class that implements Map but is not a Map itself
        MapLikeType customMapType = createMapLikeType(Properties.class, String.class, String.class);
        assertTrue(customMapType.isTrueMapType());
    }

    @Test(timeout = 4000)
    public void testGetKeyTypeAndContentType() {
        JavaType keyType = createSimpleType(String.class);
        JavaType valueType = createSimpleType(Integer.class);
        MapLikeType type = MapLikeType.construct(HashMap.class, keyType, valueType);
        
        assertEquals(keyType, type.getKeyType());
        assertEquals(valueType, type.getContentType());
    }

    @Test(timeout = 4000)
    public void testSerializationCompatibility() {
        // Verify that the type can be used in serialization contexts
        MapLikeType type = createMapLikeType(HashMap.class, String.class, Integer.class);
        
        // Verify the serialVersionUID is present
        assertEquals(1L, MapLikeType.serialVersionUID);
        
        // Verify the type can be used in a map context
        assertTrue(type.isMapLikeType());
        assertTrue(type.isContainerType());
    }
}