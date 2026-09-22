package com.fasterxml.jackson.databind.jsontype.impl;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.impl.ClassNameIdResolver;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ClassNameIdResolverDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target class: ClassNameIdResolver
     * 
     * Branches identified:
     * 1. idFromValue / idFromValueAndType -> _idFrom
     *    - Enum.class.isAssignableFrom(cls) true/false
     *    - cls.isEnum() true/false (subclass of enum)
     *    - str.startsWith("java.util") true/false
     *    - value instanceof EnumSet true/false
     *    - value instanceof EnumMap true/false
     *    - end.startsWith(".Arrays$") || end.startsWith(".Collections$") && str.indexOf("List") >= 0
     *    - str.indexOf('$') >= 0
     *    - outer != null
     *    - ClassUtil.getOuterClass(staticType) == null
     * 
     * 2. _typeFromId
     *    - id.indexOf('<') > 0
     *    - ClassNotFoundException catch
     *    - Exception catch (IllegalArgumentException)
     *    - normal return path
     * 
     * 3. registerSubtype - no-op, should not throw
     * 4. getMechanism - returns JsonTypeInfo.Id.CLASS
     * 5. getDescForKnownTypeIds - returns "class name used as type id"
     * 
     * Defect targeted (from GenericTypeId1735Test):
     * When deserializing a type id that is a generic type (contains '<'),
     * the resolver does NOT check that the constructed type is a subtype of
     * the base type. This allows deserialization of arbitrary types (e.g.,
     * java.util.HashMap) where a specific subtype is expected, leading to
     * a security/type-safety issue.
     * 
     * The test testNestedTypeCheck1735 expects an exception when a type id
     * resolves to a type that is not a subtype of the base type.
     */

    // Test helper to create a resolver with a specific base type
    private ClassNameIdResolver createResolver(Class<?> baseClass) {
        JavaType baseType = TypeFactory.defaultInstance().constructType(baseClass);
        return new ClassNameIdResolver(baseType, TypeFactory.defaultInstance());
    }

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testGetMechanism() {
        ClassNameIdResolver resolver = createResolver(Object.class);
        assertEquals(JsonTypeInfo.Id.CLASS, resolver.getMechanism());
    }

    @Test(timeout = 4000)
    public void testGetDescForKnownTypeIds() {
        ClassNameIdResolver resolver = createResolver(Object.class);
        assertEquals("class name used as type id", resolver.getDescForKnownTypeIds());
    }

    @Test(timeout = 4000)
    public void testRegisterSubtypeDoesNotThrow() {
        ClassNameIdResolver resolver = createResolver(Object.class);
        resolver.registerSubtype(String.class, "test");
        // Should not throw
    }

    @Test(timeout = 4000)
    public void testIdFromValueSimpleClass() {
        ClassNameIdResolver resolver = createResolver(Object.class);
        String id = resolver.idFromValue("hello");
        assertEquals(String.class.getName(), id);
    }

    @Test(timeout = 4000)
    public void testIdFromValueAndType() {
        ClassNameIdResolver resolver = createResolver(Object.class);
        String id = resolver.idFromValueAndType("hello", String.class);
        assertEquals(String.class.getName(), id);
    }

    @Test(timeout = 4000)
    public void testIdFromValueEnum() {
        ClassNameIdResolver resolver = createResolver(Object.class);
        String id = resolver.idFromValue(TestEnum.VALUE);
        assertEquals(TestEnum.class.getName(), id);
    }

    @Test(timeout = 4000)
    public void testIdFromValueEnumSubclass() {
        ClassNameIdResolver resolver = createResolver(Object.class);
        // Anonymous subclass of enum
        TestEnum value = TestEnum.VALUE {
            @Override
            public String toString() { return "sub"; }
        };
        String id = resolver.idFromValue(value);
        assertEquals(TestEnum.class.getName(), id);
    }

    @Test(timeout = 4000)
    public void testIdFromValueEnumSet() {
        ClassNameIdResolver resolver = createResolver(Object.class);
        EnumSet<TestEnum> enumSet = EnumSet.of(TestEnum.VALUE);
        String id = resolver.idFromValue(enumSet);
        assertTrue(id.contains("EnumSet"));
        assertTrue(id.contains(TestEnum.class.getName()));
    }

    @Test(timeout = 4000)
    public void testIdFromValueEnumMap() {
        ClassNameIdResolver resolver = createResolver(Object.class);
        EnumMap<TestEnum, String> enumMap = new EnumMap<>(TestEnum.class);
        enumMap.put(TestEnum.VALUE, "test");
        String id = resolver.idFromValue(enumMap);
        assertTrue(id.contains("EnumMap"));
        assertTrue(id.contains(TestEnum.class.getName()));
    }

    @Test(timeout = 4000)
    public void testIdFromValueArraysAsList() {
        ClassNameIdResolver resolver = createResolver(Object.class);
        List<String> list = Arrays.asList("a", "b");
        String id = resolver.idFromValue(list);
        assertEquals("java.util.ArrayList", id);
    }

    @Test(timeout = 4000)
    public void testIdFromValueInnerClass() {
        ClassNameIdResolver resolver = createResolver(Object.class);
        InnerClass inner = new InnerClass();
        String id = resolver.idFromValue(inner);
        // Should fall back to base type raw class
        assertEquals(Object.class.getName(), id);
    }

    @Test(timeout = 4000)
    public void testIdFromValueStaticInnerClass() {
        ClassNameIdResolver resolver = createResolver(Object.class);
        StaticInnerClass inner = new StaticInnerClass();
        String id = resolver.idFromValue(inner);
        assertEquals(StaticInnerClass.class.getName(), id);
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testIdFromValueNull() {
        ClassNameIdResolver resolver = createResolver(Object.class);
        try {
            resolver.idFromValue(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIdFromValueAndTypeNullValue() {
        ClassNameIdResolver resolver = createResolver(Object.class);
        try {
            resolver.idFromValueAndType(null, String.class);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testTypeFromIdNullId() throws IOException {
        ClassNameIdResolver resolver = createResolver(Object.class);
        DatabindContext context = new DeserializationContext(null, null, null, null) {
            @Override
            public TypeFactory getTypeFactory() {
                return TypeFactory.defaultInstance();
            }
        };
        try {
            resolver.typeFromId(context, null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testTypeFromIdEmptyString() throws IOException {
        ClassNameIdResolver resolver = createResolver(Object.class);
        DatabindContext context = new DeserializationContext(null, null, null, null) {
            @Override
            public TypeFactory getTypeFactory() {
                return TypeFactory.defaultInstance();
            }
        };
        try {
            resolver.typeFromId(context, "");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testTypeFromIdGenericType() throws IOException {
        ClassNameIdResolver resolver = createResolver(Object.class);
        DatabindContext context = new DeserializationContext(null, null, null, null) {
            @Override
            public TypeFactory getTypeFactory() {
                return TypeFactory.defaultInstance();
            }
        };
        JavaType result = resolver.typeFromId(context, "java.util.List<java.lang.String>");
        assertNotNull(result);
        assertEquals(List.class, result.getRawClass());
    }

    @Test(timeout = 4000)
    public void testTypeFromIdSimpleClass() throws IOException {
        ClassNameIdResolver resolver = createResolver(Object.class);
        DatabindContext context = new DeserializationContext(null, null, null, null) {
            @Override
            public TypeFactory getTypeFactory() {
                return TypeFactory.defaultInstance();
            }
        };
        JavaType result = resolver.typeFromId(context, String.class.getName());
        assertNotNull(result);
        assertEquals(String.class, result.getRawClass());
    }

    @Test(timeout = 4000)
    public void testTypeFromIdClassNotFound() throws IOException {
        ClassNameIdResolver resolver = createResolver(Object.class);
        DatabindContext context = new DeserializationContext(null, null, null, null) {
            @Override
            public TypeFactory getTypeFactory() {
                return TypeFactory.defaultInstance();
            }
        };
        JavaType result = resolver.typeFromId(context, "com.nonexistent.NonExistentClass");
        assertNull(result);
    }

    @Test(timeout = 4000)
    public void testTypeFromIdInvalidClass() throws IOException {
        ClassNameIdResolver resolver = createResolver(Object.class);
        DatabindContext context = new DeserializationContext(null, null, null, null) {
            @Override
            public TypeFactory getTypeFactory() {
                return TypeFactory.defaultInstance();
            }
        };
        try {
            resolver.typeFromId(context, "not a valid class name");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====

    /**
     * Defect test: When the type id contains generics (e.g., "java.util.HashMap"),
     * the resolver should verify that the resolved type is a subtype of the base type.
     * The bug allows deserialization of arbitrary types, which should be rejected.
     */
    @Test(timeout = 4000)
    public void testNestedTypeCheck1735() throws IOException {
        // Base type is Payload1735
        ClassNameIdResolver resolver = createResolver(Payload1735.class);
        
        DatabindContext context = new DeserializationContext(null, null, null, null) {
            @Override
            public TypeFactory getTypeFactory() {
                return TypeFactory.defaultInstance();
            }
        };
        
        // This type id resolves to java.util.HashMap which is NOT a subtype of Payload1735
        // The defect allows this to pass without error
        try {
            JavaType result = resolver.typeFromId(context, "java.util.HashMap");
            // If we get here, the defect is present - the type is not a subtype
            // but no exception was thrown
            fail("Expected IllegalArgumentException for non-subtype type id");
        } catch (IllegalArgumentException e) {
            // Expected - the fix should throw this
            assertTrue(e.getMessage().contains("not subtype of"));
        }
    }

    @Test(timeout = 4000)
    public void testTypeFromIdSubtype() throws IOException {
        ClassNameIdResolver resolver = createResolver(Payload1735.class);
        
        DatabindContext context = new DeserializationContext(null, null, null, null) {
            @Override
            public TypeFactory getTypeFactory() {
                return TypeFactory.defaultInstance();
            }
        };
        
        // Subtype should work fine
        JavaType result = resolver.typeFromId(context, SubPayload1735.class.getName());
        assertNotNull(result);
        assertEquals(SubPayload1735.class, result.getRawClass());
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testTypeFromIdWithDeserializationContext() throws IOException {
        ClassNameIdResolver resolver = createResolver(Object.class);
        
        // Create a mock DeserializationContext that handles unknown types
        DeserializationContext context = new DeserializationContext(null, null, null, null) {
            @Override
            public TypeFactory getTypeFactory() {
                return TypeFactory.defaultInstance();
            }
            
            @Override
            public JavaType handleUnknownTypeId(JavaType baseType, String id, TypeIdResolver idResolver, String extraDesc) {
                return TypeFactory.defaultInstance().constructType(String.class);
            }
        };
        
        JavaType result = resolver.typeFromId(context, "com.nonexistent.NonExistentClass");
        assertNotNull(result);
        assertEquals(String.class, result.getRawClass());
    }

    @Test(timeout = 4000)
    public void testTypeFromIdWithDeserializationContextNoHandler() throws IOException {
        ClassNameIdResolver resolver = createResolver(Object.class);
        
        DeserializationContext context = new DeserializationContext(null, null, null, null) {
            @Override
            public TypeFactory getTypeFactory() {
                return TypeFactory.defaultInstance();
            }
        };
        
        JavaType result = resolver.typeFromId(context, "com.nonexistent.NonExistentClass");
        assertNull(result);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testIdFromValueAndTypeConsistency() {
        ClassNameIdResolver resolver = createResolver(Object.class);
        String value = "test";
        String id1 = resolver.idFromValue(value);
        String id2 = resolver.idFromValueAndType(value, String.class);
        assertEquals(id1, id2);
    }

    @Test(timeout = 4000)
    public void testTypeFromIdRoundTrip() throws IOException {
        ClassNameIdResolver resolver = createResolver(Object.class);
        DatabindContext context = new DeserializationContext(null, null, null, null) {
            @Override
            public TypeFactory getTypeFactory() {
                return TypeFactory.defaultInstance();
            }
        };
        
        String id = resolver.idFromValue("hello");
        JavaType type = resolver.typeFromId(context, id);
        assertEquals(String.class, type.getRawClass());
    }

    // Test helper classes
    private enum TestEnum {
        VALUE
    }

    private class InnerClass {
        // Inner class (non-static)
    }

    private static class StaticInnerClass {
        // Static inner class
    }

    // Classes for defect test
    private static class Payload1735 {
        // Base payload class
    }

    private static class SubPayload1735 extends Payload1735 {
        // Subtype of payload
    }
}