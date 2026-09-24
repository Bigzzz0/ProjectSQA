package com.fasterxml.jackson.databind.jsontype.impl;

/*
 * [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Method Under Test            | Decision / Condition Branch                      | Target Test Method
 * ====================================================================================================
 * getMechanism()               | Unconditional -> CLASS                           | testMechanismAndMetadata
 * getDescForKnownTypeIds()     | Unconditional -> String description              | testMechanismAndMetadata
 * registerSubtype()            | No-op branch                                     | testRegisterSubtypeNoOp
 * ----------------------------------------------------------------------------------------------------
 * _idFrom()                    | Enum check: cls.isEnum() == true                 | testIdFromEnumStandard
 * _idFrom()                    | Enum check: !cls.isEnum() (anonymous sub-class)  | testIdFromEnumWithClassBody
 * _idFrom()                    | java.util: EnumSet<?>                            | testIdFromEnumSet
 * _idFrom()                    | java.util: EnumMap<?,?>                          | testIdFromEnumMap
 * _idFrom()                    | java.util: Arrays$ArrayList (contains "List")    | testIdFromArraysAsList
 * _idFrom()                    | java.util: Collections$List wrappers             | testIdFromCollectionsLists
 * _idFrom()                    | java.util: Collections wrapper not List (Map/Set)| testIdFromCollectionsNonListWrappers
 * _idFrom()                    | java.util: Regular utility classes (HashMap)     | testIdFromStandardJavaUtilClasses
 * _idFrom()                    | Inner class: static nested (outer == null)       | testIdFromStaticNestedClass
 * _idFrom()                    | Inner class: non-static member (outer != null)   | testIdFromNonStaticInnerClassWithTopLevelBase
 * _idFrom()                    | Inner class: non-static base (outer != null)     | testIdFromNonStaticInnerClassWithInnerBase
 * idFromValueAndType()         | Explicit type override (value == null / typed)   | testIdFromValueAndType
 * ----------------------------------------------------------------------------------------------------
 * _typeFromId()                | id.indexOf('<') > 0: Valid generic type syntax   | testTypeFromIdWithGenericsValid
 * _typeFromId() (DEFECT #1735) | id.indexOf('<') > 0: Incompatible generic subtype| testNestedTypeCheck1735DefectTarget
 * _typeFromId() (DEFECT #1735) | id.indexOf('<') > 0: Direct incompatible generic | testTypeFromIdGenericTypeCompatibilityTarget
 * _typeFromId()                | No '<': ClassNotFound & ctxt is DeserContext     | testTypeFromIdClassNotFoundWithDeserContext
 * _typeFromId()                | No '<': ClassNotFound & ctxt NOT DeserContext    | testTypeFromIdClassNotFoundWithNonDeserContext
 * _typeFromId()                | No '<': Specialization mismatch (not subtype)    | testTypeFromIdSpecializationMismatch
 * ====================================================================================================
 */

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ClassNameIdResolverGeminiTest {

    // -------------------------------------------------------------------------
    // Test Fixtures & Stub Classes
    // -------------------------------------------------------------------------

    public enum TestEnum {
        CONSTANT_A {
            @Override
            public String toString() {
                return "A";
            }
        },
        CONSTANT_B
    }

    public static class StaticNestedPayload {
        public int id;
    }

    public class NonStaticInnerPayload {
        public String name;
    }

    public class NonStaticInnerSub extends NonStaticInnerPayload {
        public double value;
    }

    static abstract class BasePayload1735 {
    }

    static class Payload1735 extends BasePayload1735 {
        public int x;
    }

    static class Wrapper1735 {
        @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
        public Payload1735 w;
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testMechanismAndMetadata() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(Object.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, tf);

        assertEquals("Mechanism must be CLASS", JsonTypeInfo.Id.CLASS, resolver.getMechanism());
        assertEquals("Description must match", "class name used as type id", resolver.getDescForKnownTypeIds());
    }

    @Test(timeout = 4000)
    public void testRegisterSubtypeNoOp() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ClassNameIdResolver resolver = new ClassNameIdResolver(tf.constructType(Object.class), tf);
        // registerSubtype is a no-op; must execute cleanly without exception or side-effect
        resolver.registerSubtype(String.class, "string");
        resolver.registerSubtype(null, null);
    }

    @Test(timeout = 4000)
    public void testIdFromValueAndType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ClassNameIdResolver resolver = new ClassNameIdResolver(tf.constructType(Object.class), tf);

        String idNullVal = resolver.idFromValueAndType(null, String.class);
        assertEquals("java.lang.String", idNullVal);

        String idTyped = resolver.idFromValueAndType(new ArrayList<String>(), List.class);
        assertEquals("java.util.List", idTyped);
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testIdFromEnumStandard() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ClassNameIdResolver resolver = new ClassNameIdResolver(tf.constructType(TestEnum.class), tf);

        String id = resolver.idFromValue(TestEnum.CONSTANT_B);
        assertEquals(TestEnum.class.getName(), id);
    }

    @Test(timeout = 4000)
    public void testIdFromEnumWithClassBody() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ClassNameIdResolver resolver = new ClassNameIdResolver(tf.constructType(TestEnum.class), tf);

        // CONSTANT_A has an anonymous class body; isEnum() is false, requires cls.getSuperclass()
        assertFalse("Anonymous subclass of enum is not itself an enum class", TestEnum.CONSTANT_A.getClass().isEnum());
        String id = resolver.idFromValue(TestEnum.CONSTANT_A);
        assertEquals("Resolved type id must normalize back to base Enum class", TestEnum.class.getName(), id);
    }

    @Test(timeout = 4000)
    public void testIdFromEnumSet() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ClassNameIdResolver resolver = new ClassNameIdResolver(tf.constructType(EnumSet.class), tf);

        EnumSet<TestEnum> set = EnumSet.of(TestEnum.CONSTANT_B);
        String id = resolver.idFromValue(set);

        assertNotNull(id);
        assertTrue("EnumSet canonical name should start with java.util.EnumSet<", id.startsWith("java.util.EnumSet<"));
        assertTrue("EnumSet canonical name should contain enum class", id.contains(TestEnum.class.getName()));
    }

    @Test(timeout = 4000)
    public void testIdFromEnumMap() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ClassNameIdResolver resolver = new ClassNameIdResolver(tf.constructType(EnumMap.class), tf);

        EnumMap<TestEnum, String> map = new EnumMap<TestEnum, String>(TestEnum.class);
        map.put(TestEnum.CONSTANT_B, "val");
        String id = resolver.idFromValue(map);

        assertNotNull(id);
        assertTrue("EnumMap canonical name should start with java.util.EnumMap<", id.startsWith("java.util.EnumMap<"));
        assertTrue("EnumMap canonical name should contain enum key type", id.contains(TestEnum.class.getName()));
        assertTrue("EnumMap canonical name should contain Object value type", id.contains("java.lang.Object"));
    }

    @Test(timeout = 4000)
    public void testIdFromArraysAsList() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ClassNameIdResolver resolver = new ClassNameIdResolver(tf.constructType(List.class), tf);

        List<String> list = Arrays.asList("1", "2");
        String id = resolver.idFromValue(list);
        assertEquals("Arrays.asList() wrapper must map to java.util.ArrayList", "java.util.ArrayList", id);
    }

    @Test(timeout = 4000)
    public void testIdFromCollectionsLists() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ClassNameIdResolver resolver = new ClassNameIdResolver(tf.constructType(List.class), tf);

        assertEquals("java.util.ArrayList", resolver.idFromValue(Collections.singletonList("item")));
        assertEquals("java.util.ArrayList", resolver.idFromValue(Collections.unmodifiableList(new ArrayList<String>())));
        assertEquals("java.util.ArrayList", resolver.idFromValue(Collections.emptyList()));
        assertEquals("java.util.ArrayList", resolver.idFromValue(Collections.synchronizedList(new ArrayList<String>())));
    }

    @Test(timeout = 4000)
    public void testIdFromCollectionsNonListWrappers() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ClassNameIdResolver resolver = new ClassNameIdResolver(tf.constructType(Map.class), tf);

        Map<String, String> singletonMap = Collections.singletonMap("k", "v");
        String idMap = resolver.idFromValue(singletonMap);
        assertEquals("Collections.singletonMap must preserve its own class name", singletonMap.getClass().getName(), idMap);

        Set<String> singletonSet = Collections.singleton("k");
        String idSet = resolver.idFromValue(singletonSet);
        assertEquals("Collections.singletonSet must preserve its own class name", singletonSet.getClass().getName(), idSet);
    }

    @Test(timeout = 4000)
    public void testIdFromStandardJavaUtilClasses() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ClassNameIdResolver resolver = new ClassNameIdResolver(tf.constructType(Object.class), tf);

        assertEquals("java.util.HashMap", resolver.idFromValue(new HashMap<String, String>()));
        assertEquals("java.util.ArrayList", resolver.idFromValue(new ArrayList<String>()));
        assertEquals("java.util.HashSet", resolver.idFromValue(new HashSet<String>()));
    }

    @Test(timeout = 4000)
    public void testIdFromStaticNestedClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ClassNameIdResolver resolver = new ClassNameIdResolver(tf.constructType(Object.class), tf);

        StaticNestedPayload staticNested = new StaticNestedPayload();
        String id = resolver.idFromValue(staticNested);
        assertEquals("Static nested class outer is null; must retain full class name",
                StaticNestedPayload.class.getName(), id);
    }

    @Test(timeout = 4000)
    public void testIdFromNonStaticInnerClassWithTopLevelBase() {
        TypeFactory tf = TypeFactory.defaultInstance();
        // Base type is top-level (Object.class has no outer class)
        ClassNameIdResolver resolver = new ClassNameIdResolver(tf.constructType(Object.class), tf);

        NonStaticInnerPayload inner = new NonStaticInnerPayload();
        String id = resolver.idFromValue(inner);
        assertEquals("Non-static inner class generalized to base type when base has no outer class",
                Object.class.getName(), id);
    }

    @Test(timeout = 4000)
    public void testIdFromNonStaticInnerClassWithInnerBase() {
        TypeFactory tf = TypeFactory.defaultInstance();
        // Base type is ALSO non-static inner class (outer != null)
        JavaType baseType = tf.constructType(NonStaticInnerPayload.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, tf);

        NonStaticInnerSub innerSub = new NonStaticInnerSub();
        String id = resolver.idFromValue(innerSub);
        assertEquals("Non-static inner class NOT generalized when base type itself has an outer class",
                NonStaticInnerSub.class.getName(), id);
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (databind#1735)
    // -------------------------------------------------------------------------

    /**
     * Targets known defect databind#1735 / GenericTypeId1735Test:
     * When generic type ID like 'java.util.HashMap<String, String>' is passed for a field
     * whose base type is Payload1735, the resolver must ensure assignment compatibility
     * and fail type resolution with an exception containing 'not subtype of'.
     */
    @Test(timeout = 4000)
    public void testNestedTypeCheck1735DefectTarget() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"w\":{\"@class\":\"java.util.HashMap<java.lang.String,java.lang.String>\"}}";
        try {
            mapper.readValue(json, Wrapper1735.class);
            fail("Expected JsonMappingException because HashMap is not a subtype of Payload1735");
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue("Exception message must indicate assignment incompatibility ('not subtype of'), but got: " + msg,
                    msg != null && msg.contains("not subtype of"));
        }
    }

    /**
     * Direct unit-level test targeting assignment validation inside _typeFromId
     * when id.indexOf('<') > 0.
     */
    @Test(timeout = 4000)
    public void testTypeFromIdGenericTypeCompatibilityTarget() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = mapper.getTypeFactory();
        JavaType baseType = tf.constructType(Payload1735.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, tf);

        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), mapper.getInjectableValues());

        try {
            JavaType resolved = resolver.typeFromId(ctxt, "java.util.HashMap<java.lang.String,java.lang.String>");
            fail("Expected type resolution failure for incompatible generic type, but got: " + resolved);
        } catch (JsonMappingException e) {
            String msg = e.getMessage();
            assertTrue("Expected 'not subtype of' in error message: " + msg,
                    msg != null && msg.contains("not subtype of"));
        }
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testTypeFromIdWithGenericsValid() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = mapper.getTypeFactory();
        JavaType baseType = tf.constructType(List.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, tf);

        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), mapper.getInjectableValues());

        JavaType resolved = resolver.typeFromId(ctxt, "java.util.List<java.lang.String>");
        assertNotNull(resolved);
        assertEquals(List.class, resolved.getRawClass());
        assertEquals(String.class, resolved.containedType(0).getRawClass());
    }

    @Test(timeout = 4000)
    public void testTypeFromIdClassNotFoundWithDeserContext() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = mapper.getTypeFactory();
        JavaType baseType = tf.constructType(Object.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, tf);

        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), mapper.getInjectableValues());

        try {
            resolver.typeFromId(ctxt, "com.nonexistent.NoSuchTypeClass");
            fail("Expected JsonMappingException for non-existent class id");
        } catch (JsonMappingException expected) {
            String msg = expected.getMessage();
            assertTrue(msg.contains("no such class found"));
        }
    }

    @Test(timeout = 4000)
    public void testTypeFromIdClassNotFoundWithNonDeserContext() throws IOException {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(Object.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, tf);

        DatabindContext nonDeserContext = new DatabindContext() {
            @Override
            public JavaType constructType(java.lang.reflect.Type type) {
                return TypeFactory.defaultInstance().constructType(type);
            }

            @Override
            public JavaType constructSpecializedType(JavaType base, Class<?> subclass) {
                return TypeFactory.defaultInstance().constructSpecializedType(base, subclass);
            }

            @Override
            public TypeFactory getTypeFactory() {
                return TypeFactory.defaultInstance();
            }

            @Override
            public com.fasterxml.jackson.databind.cfg.MapperConfig<?> getConfig() {
                return null;
            }
        };

        JavaType result = resolver.typeFromId(nonDeserContext, "com.nonexistent.BogusClassXYZ");
        assertNull("When context is not DeserializationContext, return null on ClassNotFoundException", result);
    }

    @Test(timeout = 4000)
    public void testTypeFromIdSpecializationMismatch() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = mapper.getTypeFactory();
        // Base type is String; resolving Integer must fail specialization check
        JavaType baseType = tf.constructType(String.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, tf);

        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), mapper.getInjectableValues());

        try {
            resolver.typeFromId(ctxt, "java.lang.Integer");
            fail("Expected IllegalArgumentException when resolving specialized type incompatible with base type");
        } catch (IllegalArgumentException e) {
            assertTrue("Message must indicate assignment failure: " + e.getMessage(),
                    e.getMessage().contains("not subtype"));
        }
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testTypeFromIdSpecializedSubclassSuccess() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeFactory tf = mapper.getTypeFactory();
        JavaType baseType = tf.constructType(Number.class);
        ClassNameIdResolver resolver = new ClassNameIdResolver(baseType, tf);

        DeserializationContext ctxt = ((DefaultDeserializationContext) mapper.getDeserializationContext())
                .createInstance(mapper.getDeserializationConfig(), mapper.getFactory().createParser("{}"), mapper.getInjectableValues());

        JavaType intType = resolver.typeFromId(ctxt, "java.lang.Integer");
        assertNotNull(intType);
        assertEquals(Integer.class, intType.getRawClass());
        assertTrue(Number.class.isAssignableFrom(intType.getRawClass()));
    }
}