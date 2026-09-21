package com.fasterxml.jackson.databind.module;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.util.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target class: SimpleAbstractTypeResolver
 * 
 * Branches covered:
 * 1. addMapping:
 *    - superType == subType -> IllegalArgumentException
 *    - !superType.isAssignableFrom(subType) -> IllegalArgumentException
 *    - !Modifier.isAbstract(superType) -> IllegalArgumentException
 *    - Valid mapping -> added to mappings, returns this
 * 2. findTypeMapping:
 *    - mapping for raw class exists -> return type.narrowBy(dst)
 *    - mapping for raw class does NOT exist -> return null
 * 3. resolveAbstractType:
 *    - Always returns null
 *
 * Boundary conditions:
 * - null arguments (NPE, not explicitly handled)
 * - Array classes: byte[].class is not abstract; attempts to add mapping for array types trigger the abstract check.
 * - Concrete final class as supertype (e.g., String) triggers not-abstract check.
 * - Self-mapping and unrelated type mapping.
 *
 * Defect targeting (Defects4J #890):
 * - Issue: "Can not deserialize Class [B (of type array) as a Bean"
 * - The resolver should prevent adding mappings for array types because they are not abstract.
 * - Test verifies that addMapping for byte[] throws IllegalArgumentException (correct behavior in fixed version).
 * - Additional test ensures findTypeMapping for array types returns null, avoiding array-to-bean misinterpretation.
 */
public class SimpleAbstractTypeResolverDeepseekTest {

    private final SimpleAbstractTypeResolver resolver = new SimpleAbstractTypeResolver();

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAddMappingValid() {
        SimpleAbstractTypeResolver result = resolver.addMapping(Collection.class, ArrayList.class);
        assertSame("addMapping should return this for chaining", resolver, result);
    }

    @Test(timeout = 4000)
    public void testFindTypeMappingExisting() {
        resolver.addMapping(Collection.class, ArrayList.class);
        JavaType inputType = TypeFactory.defaultInstance().constructType(Collection.class);
        JavaType narrowed = resolver.findTypeMapping(null, inputType);
        assertNotNull("findTypeMapping should return a type for existing mapping", narrowed);
        assertEquals("Narrowed type should be ArrayList", ArrayList.class, narrowed.getRawClass());
    }

    @Test(timeout = 4000)
    public void testFindTypeMappingNonExisting() {
        JavaType inputType = TypeFactory.defaultInstance().constructType(String.class);
        JavaType result = resolver.findTypeMapping(null, inputType);
        assertNull("findTypeMapping should return null for unmapped types", result);
    }

    @Test(timeout = 4000)
    public void testResolveAbstractTypeAlwaysNull() {
        JavaType inputType = TypeFactory.defaultInstance().constructType(Collection.class);
        JavaType result = resolver.resolveAbstractType(null, inputType);
        assertNull("resolveAbstractType should always return null", result);
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAddMappingSelfMapping() {
        try {
            resolver.addMapping(String.class, String.class);
            fail("Should throw IllegalArgumentException for self-mapping");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("from class to itself"));
        }
    }

    @Test(timeout = 4000)
    public void testAddMappingNotAssignable() {
        try {
            resolver.addMapping(String.class, Integer.class);
            fail("Should throw IllegalArgumentException for non-subtype relation");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not a subtype of former"));
        }
    }

    @Test(timeout = 4000)
    public void testAddMappingNotAbstract() {
        // HashMap is concrete, LinkedHashMap is a subclass but HashMap is not abstract
        try {
            resolver.addMapping(HashMap.class, LinkedHashMap.class);
            fail("Should throw IllegalArgumentException for concrete supertype");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("not abstract"));
        }
    }

    @Test(timeout = 4000)
    public void testFindTypeMappingNullConfig() {
        // findTypeMapping should work with null config (config is not used)
        resolver.addMapping(Map.class, HashMap.class);
        JavaType input = TypeFactory.defaultInstance().constructType(Map.class);
        JavaType result = resolver.findTypeMapping(null, input);
        assertNotNull(result);
        assertEquals(HashMap.class, result.getRawClass());
    }

    @Test(timeout = 4000)
    public void testFindTypeMappingWithNullType() {
        // This will trigger NPE since type is used without null check; it's not handled but we must cover it
        try {
            resolver.findTypeMapping(null, null);
            fail("Should throw NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J #890)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testByteArrayTypeOverride890_targetDefect() {
        // Attempt to add mapping from byte[] (array type, not abstract) to a concrete type
        // In the defective version, this might be allowed inadvertently; the fixed version rejects it.
        try {
            resolver.addMapping(byte[].class, String.class); // not assignable anyway, but first check is abstract
            fail("Should throw IllegalArgumentException for array supertype (not abstract)");
        } catch (IllegalArgumentException e) {
            // Expected: byte[] is not abstract
            assertTrue(e.getMessage().contains("not abstract"));
        }
    }

    @Test(timeout = 4000)
    public void testFindTypeMappingForArrayType() {
        // When no mapping exists for an array type, findTypeMapping should return null
        // This prevents later deserialization from treating the array as a bean.
        JavaType arrayType = TypeFactory.defaultInstance().constructType(byte[].class);
        JavaType result = resolver.findTypeMapping(null, arrayType);
        assertNull("findTypeMapping for array type should return null when no mapping exists", result);
    }

    @Test(timeout = 4000)
    public void testResolveAbstractTypeForByteArray() {
        // The resolver should not attempt to resolve array types; return null is correct.
        JavaType arrayType = TypeFactory.defaultInstance().constructType(byte[].class);
        JavaType result = resolver.resolveAbstractType(null, arrayType);
        assertNull("resolveAbstractType for array type should return null", result);
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAddMappingWithNullSuperType() {
        try {
            resolver.addMapping(null, String.class);
            fail("Should throw NullPointerException for null superType");
        } catch (NullPointerException e) {
            // expected due to method internal calls like superType.getModifiers()
        }
    }

    @Test(timeout = 4000)
    public void testAddMappingWithNullSubType() {
        try {
            resolver.addMapping(String.class, null);
            fail("Should throw NullPointerException for null subType");
        } catch (NullPointerException e) {
            // expected due to method internal calls like subType.getName()
        }
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testChainingMultipleMappings() {
        resolver.addMapping(Collection.class, ArrayList.class)
                .addMapping(List.class, LinkedList.class);
        JavaType collectionType = TypeFactory.defaultInstance().constructType(Collection.class);
        JavaType listType = TypeFactory.defaultInstance().constructType(List.class);
        assertEquals(ArrayList.class, resolver.findTypeMapping(null, collectionType).getRawClass());
        assertEquals(LinkedList.class, resolver.findTypeMapping(null, listType).getRawClass());
    }

    @Test(timeout = 4000)
    public void testNoSideEffectsOnAddMappingFailure() {
        // After a failed addMapping, the resolver should remain unchanged
        resolver.addMapping(Collection.class, ArrayList.class);
        try {
            resolver.addMapping(Collection.class, String.class); // not assignable
        } catch (IllegalArgumentException ignored) {}
        JavaType input = TypeFactory.defaultInstance().constructType(Collection.class);
        JavaType result = resolver.findTypeMapping(null, input);
        assertNotNull(result);
        assertEquals(ArrayList.class, result.getRawClass());
    }
}