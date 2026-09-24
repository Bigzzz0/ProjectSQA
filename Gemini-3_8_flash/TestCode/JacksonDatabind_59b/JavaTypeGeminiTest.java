package com.fasterxml.jackson.databind;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.JavaType
 * Benchmark Defect: TypeRefinementForMapTest::testMapKeyRefinement1384
 *
 * Branch & Condition Coverage Map:
 * 1. forcedNarrowBy(Class<?> subclass):
 *    - Branch A: subclass == _class -> return this (Optimization path)
 *    - Branch B: subclass != _class -> invoke _narrow(subclass)
 *    - Branch C1: _valueHandler != result.getValueHandler() -> result.withValueHandler
 *    - Branch C2: _valueHandler == result.getValueHandler() -> skip
 *    - Branch D1: _typeHandler != result.getTypeHandler() -> result.withTypeHandler
 *    - Branch D2: _typeHandler == result.getTypeHandler() -> skip
 * 2. isConcrete():
 *    - Branch A: (mod & (INTERFACE | ABSTRACT)) == 0 -> return true
 *    - Branch B: interface or abstract, but _class.isPrimitive() -> return true (Primitive edge-case)
 *    - Branch C: interface or abstract, not primitive -> return false
 * 3. isTypeOrSubTypeOf(Class<?> clz):
 *    - Branch A: _class == clz -> return true
 *    - Branch B: clz.isAssignableFrom(_class) -> return true
 *    - Branch C: false
 * 4. hasHandlers():
 *    - Branch A: _typeHandler != null -> true
 *    - Branch B: _valueHandler != null -> true
 *    - Branch C: both null -> false
 * 5. containedTypeOrUnknown(int index):
 *    - Branch A: containedType(index) != null -> return type
 *    - Branch B: containedType(index) == null -> return TypeFactory.unknownType()
 * 6. Defect Target (JacksonDatabind-67 / Issue #1384):
 *    - Key type refinement in Map deserialization where key class declares @JsonDeserialize(keyUsing = ...).
 *    - In defective versions, key deserializer discovery failed after keyAs type refinement.
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.AbstractList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class JavaTypeGeminiTest {

    /*
    /**********************************************************
    /* Test Helper Classes & Subtypes
    /**********************************************************
     */

    public static class AnnotatedCompoundKey {
        private final String a;
        private final String b;

        public AnnotatedCompoundKey(String a, String b) {
            this.a = a;
            this.b = b;
        }

        public String getA() { return a; }
        public String getB() { return b; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AnnotatedCompoundKey)) return false;
            AnnotatedCompoundKey other = (AnnotatedCompoundKey) o;
            return (a == null ? other.a == null : a.equals(other.a)) &&
                   (b == null ? other.b == null : b.equals(other.b));
        }

        @Override
        public int hashCode() {
            return (a != null ? a.hashCode() : 0) * 31 + (b != null ? b.hashCode() : 0);
        }
    }

    public static class CompoundKeyDeserializer extends KeyDeserializer {
        @Override
        public Object deserializeKey(String key, DeserializationContext ctxt) {
            String[] parts = key.split(":");
            return new AnnotatedCompoundKey(parts[0], parts[1]);
        }
    }

    public static class MapWrapper1384 {
        @JsonDeserialize(keyAs = AnnotatedCompoundKey.class)
        public Map<Object, String> map;
    }

    @JsonDeserialize(keyUsing = CompoundKeyDeserializer.class)
    public static class KeyAnnotatedOnClass {
        private final String val;
        public KeyAnnotatedOnClass(String val) { this.val = val; }
        public String getVal() { return val; }
    }

    public static class DirectMapWrapper {
        public Map<KeyAnnotatedOnClass, String> items;
    }

    /**
     * Concrete test double subclass exposing JavaType's base methods.
     */
    static class DummyJavaType extends JavaType {
        private static final long serialVersionUID = 1L;
        private final JavaType _contained;

        public DummyJavaType(Class<?> raw) {
            this(raw, 0, null, null, false, null);
        }

        public DummyJavaType(Class<?> raw, int additionalHash, Object valueHandler,
                             Object typeHandler, boolean asStatic, JavaType contained) {
            super(raw, additionalHash, valueHandler, typeHandler, asStatic);
            _contained = contained;
        }

        public DummyJavaType(DummyJavaType base) {
            super(base);
            _contained = base._contained;
        }

        @Override
        public JavaType withTypeHandler(Object h) {
            return new DummyJavaType(_class, _hash - _class.getName().hashCode(), _valueHandler, h, _asStatic, _contained);
        }

        @Override
        public JavaType withContentTypeHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withValueHandler(Object h) {
            return new DummyJavaType(_class, _hash - _class.getName().hashCode(), h, _typeHandler, _asStatic, _contained);
        }

        @Override
        public JavaType withContentValueHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withContentType(JavaType contentType) {
            return new DummyJavaType(_class, _hash - _class.getName().hashCode(), _valueHandler, _typeHandler, _asStatic, contentType);
        }

        @Override
        public JavaType withStaticTyping() {
            return new DummyJavaType(_class, _hash - _class.getName().hashCode(), _valueHandler, _typeHandler, true, _contained);
        }

        @Override
        public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) {
            return this;
        }

        @Override
        protected JavaType _narrow(Class<?> subclass) {
            return new DummyJavaType(subclass, _hash - _class.getName().hashCode(), null, null, _asStatic, _contained);
        }

        @Override
        public boolean isContainerType() {
            return false;
        }

        @Override
        public int containedTypeCount() {
            return _contained == null ? 0 : 1;
        }

        @Override
        public JavaType containedType(int index) {
            return index == 0 ? _contained : null;
        }

        @Deprecated
        @Override
        public String containedTypeName(int index) {
            return null;
        }

        @Override
        public TypeBindings getBindings() {
            return TypeBindings.emptyBindings();
        }

        @Override
        public JavaType findSuperType(Class<?> erasedTarget) {
            return null;
        }

        @Override
        public JavaType getSuperClass() {
            return null;
        }

        @Override
        public List<JavaType> getInterfaces() {
            return Collections.emptyList();
        }

        @Override
        public JavaType[] findTypeParameters(Class<?> expType) {
            return new JavaType[0];
        }

        @Override
        public StringBuilder getGenericSignature(StringBuilder sb) {
            sb.append("L").append(_class.getName().replace('.', '/')).append(";");
            return sb;
        }

        @Override
        public StringBuilder getErasedSignature(StringBuilder sb) {
            sb.append(_class.getName());
            return sb;
        }

        @Override
        public String toString() {
            return "[DummyJavaType " + _class.getName() + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null || o.getClass() != getClass()) return false;
            DummyJavaType other = (DummyJavaType) o;
            return other._class == _class;
        }
    }

    /*
    /**********************************************************
    /* Partition A: Core Functional Logic & State Transitions
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testRawClassAndIdentity() {
        DummyJavaType type = new DummyJavaType(String.class);
        assertEquals(String.class, type.getRawClass());
        assertTrue(type.hasRawClass(String.class));
        assertFalse(type.hasRawClass(Integer.class));
        assertFalse(type.hasRawClass(null));
        assertEquals(String.class.getName().hashCode(), type.hashCode());
    }

    @Test(timeout = 4000)
    public void testCopyConstructor() {
        DummyJavaType base = new DummyJavaType(String.class, 42, "valHandler", "typeHandler", true, null);
        DummyJavaType copy = new DummyJavaType(base);

        assertEquals(base.getRawClass(), copy.getRawClass());
        assertEquals(base.hashCode(), copy.hashCode());
        assertEquals("valHandler", copy.getValueHandler());
        assertEquals("typeHandler", copy.getTypeHandler());
        assertTrue(copy.useStaticType());
    }

    @Test(timeout = 4000)
    public void testForcedNarrowByIdentityOptimization() {
        DummyJavaType type = new DummyJavaType(CharSequence.class);
        JavaType narrowed = type.forcedNarrowBy(CharSequence.class);
        assertSame(type, narrowed);
    }

    @Test(timeout = 4000)
    public void testForcedNarrowByWithHandlersPreserved() {
        DummyJavaType base = new DummyJavaType(CharSequence.class, 10, "valH", "typeH", false, null);
        JavaType narrowed = base.forcedNarrowBy(String.class);

        assertNotSame(base, narrowed);
        assertEquals(String.class, narrowed.getRawClass());
        assertEquals("valH", narrowed.getValueHandler());
        assertEquals("typeH", narrowed.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testTypeModifiersAndPredicates() {
        DummyJavaType strType = new DummyJavaType(String.class);
        assertFalse(strType.isAbstract());
        assertTrue(strType.isConcrete());
        assertTrue(strType.isFinal());
        assertFalse(strType.isInterface());
        assertFalse(strType.isPrimitive());
        assertFalse(strType.isEnumType());
        assertFalse(strType.isThrowable());
        assertFalse(strType.isJavaLangObject());

        DummyJavaType absType = new DummyJavaType(AbstractList.class);
        assertTrue(absType.isAbstract());
        assertFalse(absType.isConcrete());
        assertFalse(absType.isFinal());

        DummyJavaType ifaceType = new DummyJavaType(List.class);
        assertTrue(ifaceType.isInterface());
        assertTrue(ifaceType.isAbstract());
        assertFalse(ifaceType.isConcrete());

        DummyJavaType enumType = new DummyJavaType(TimeUnit.class);
        assertTrue(enumType.isEnumType());

        DummyJavaType throwableType = new DummyJavaType(IllegalArgumentException.class);
        assertTrue(throwableType.isThrowable());

        DummyJavaType objType = new DummyJavaType(Object.class);
        assertTrue(objType.isJavaLangObject());
    }

    @Test(timeout = 4000)
    public void testPrimitiveIsConcreteBranch() {
        DummyJavaType intType = new DummyJavaType(int.class);
        assertTrue(intType.isPrimitive());
        assertTrue(intType.isConcrete());
    }

    @Test(timeout = 4000)
    public void testIsTypeOrSubTypeOf() {
        DummyJavaType type = new DummyJavaType(String.class);
        assertTrue(type.isTypeOrSubTypeOf(String.class));
        assertTrue(type.isTypeOrSubTypeOf(CharSequence.class));
        assertTrue(type.isTypeOrSubTypeOf(Object.class));
        assertFalse(type.isTypeOrSubTypeOf(Integer.class));
    }

    @Test(timeout = 4000)
    public void testHandlersPresence() {
        DummyJavaType noHandlers = new DummyJavaType(String.class, 0, null, null, false, null);
        assertFalse(noHandlers.hasValueHandler());
        assertFalse(noHandlers.hasHandlers());

        DummyJavaType valOnly = new DummyJavaType(String.class, 0, "val", null, false, null);
        assertTrue(valOnly.hasValueHandler());
        assertTrue(valOnly.hasHandlers());

        DummyJavaType typeOnly = new DummyJavaType(String.class, 0, null, "type", false, null);
        assertFalse(typeOnly.hasValueHandler());
        assertTrue(typeOnly.hasHandlers());

        DummyJavaType both = new DummyJavaType(String.class, 0, "val", "type", false, null);
        assertTrue(both.hasValueHandler());
        assertTrue(both.hasHandlers());
    }

    @Test(timeout = 4000)
    public void testDefaultPassThroughMethods() {
        DummyJavaType type = new DummyJavaType(String.class);
        assertFalse(type.isArrayType());
        assertFalse(type.isCollectionLikeType());
        assertFalse(type.isMapLikeType());
        assertTrue(type.hasContentType());
        assertNull(type.getKeyType());
        assertNull(type.getContentType());
        assertNull(type.getReferencedType());
        assertNull(type.getContentValueHandler());
        assertNull(type.getContentTypeHandler());
        assertNull(type.getParameterSource());
    }

    @Test(timeout = 4000)
    public void testContainedTypeAndGenericTypes() {
        DummyJavaType simple = new DummyJavaType(String.class);
        assertFalse(simple.hasGenericTypes());
        assertEquals(0, simple.containedTypeCount());
        assertNull(simple.containedType(0));
        assertEquals(TypeFactory.unknownType(), simple.containedTypeOrUnknown(0));

        DummyJavaType generic = new DummyJavaType(List.class, 0, null, null, false, simple);
        assertTrue(generic.hasGenericTypes());
        assertEquals(1, generic.containedTypeCount());
        assertSame(simple, generic.containedType(0));
        assertSame(simple, generic.containedTypeOrUnknown(0));
        assertEquals(TypeFactory.unknownType(), generic.containedTypeOrUnknown(1));
    }

    @Test(timeout = 4000)
    public void testSignaturesProduction() {
        DummyJavaType type = new DummyJavaType(String.class);
        assertEquals("Ljava/lang/String;", type.getGenericSignature());
        assertEquals("java.lang.String", type.getErasedSignature());
    }

    /*
    /**********************************************************
    /* Partition B: Boundary Value Analysis (BVA) & Extremes
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testHashCodeCalculationExtremes() {
        DummyJavaType minHash = new DummyJavaType(String.class, Integer.MIN_VALUE, null, null, false, null);
        assertEquals(String.class.getName().hashCode() + Integer.MIN_VALUE, minHash.hashCode());

        DummyJavaType maxHash = new DummyJavaType(String.class, Integer.MAX_VALUE, null, null, false, null);
        assertEquals(String.class.getName().hashCode() + Integer.MAX_VALUE, maxHash.hashCode());

        DummyJavaType zeroHash = new DummyJavaType(String.class, 0, null, null, false, null);
        assertEquals(String.class.getName().hashCode(), zeroHash.hashCode());
    }

    @Test(timeout = 4000)
    public void testContainedTypeOrUnknownNegativeIndex() {
        DummyJavaType type = new DummyJavaType(String.class);
        JavaType unknown = type.containedTypeOrUnknown(-1);
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());
    }

    @Test(timeout = 4000)
    public void testWithStaticTypingStateTransition() {
        DummyJavaType dynamicType = new DummyJavaType(String.class, 0, null, null, false, null);
        assertFalse(dynamicType.useStaticType());

        JavaType staticType = dynamicType.withStaticTyping();
        assertTrue(staticType.useStaticType());
    }

    /*
    /**********************************************************
    /* Partition C: Defect-Targeted Branch Zone (JacksonDatabind-67 / #1384)
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testMapKeyRefinement1384() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"map\":{\"partA:partB\":\"resolvedValue\"}}";
        MapWrapper1384 result = mapper.readValue(json, MapWrapper1384.class);

        assertNotNull("Deserialized wrapper must not be null", result);
        assertNotNull("Deserialized map must not be null", result.map);
        assertEquals(1, result.map.size());

        Map.Entry<Object, String> entry = result.map.entrySet().iterator().next();
        assertTrue("Refined key must be an instance of AnnotatedCompoundKey",
                entry.getKey() instanceof AnnotatedCompoundKey);

        AnnotatedCompoundKey key = (AnnotatedCompoundKey) entry.getKey();
        assertEquals("partA", key.getA());
        assertEquals("partB", key.getB());
        assertEquals("resolvedValue", entry.getValue());
    }

    @Test(timeout = 4000)
    public void testMapKeyDeserializerDirectKeyClassAnnotation() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"items\":{\"customKey\":\"val\"}}";
        DirectMapWrapper wrapper = mapper.readValue(json, DirectMapWrapper.class);

        assertNotNull(wrapper);
        assertNotNull(wrapper.items);
        assertEquals(1, wrapper.items.size());
        Map.Entry<KeyAnnotatedOnClass, String> entry = wrapper.items.entrySet().iterator().next();
        assertNotNull(entry.getKey());
    }

    @Test(timeout = 4000)
    public void testTypeFactoryConstructMapTypeKeyRefinement() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType keyType = tf.constructType(AnnotatedCompoundKey.class);
        JavaType valueType = tf.constructType(String.class);
        MapType mapType = tf.constructMapType(Map.class, keyType, valueType);

        assertNotNull(mapType.getKeyType());
        assertEquals(AnnotatedCompoundKey.class, mapType.getKeyType().getRawClass());
        assertTrue(mapType.isMapLikeType());
        assertTrue(mapType.isContainerType());
    }

    /*
    /**********************************************************
    /* Partition D: Exception & Defensive Guard Paths
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testForcedNarrowByNoOpWhenAlreadyMatched() {
        DummyJavaType base = new DummyJavaType(CharSequence.class) {
            private static final long serialVersionUID = 1L;
            @Override
            protected JavaType _narrow(Class<?> subclass) {
                return new DummyJavaType(subclass, 0, "presetVal", "presetType", false, null);
            }
        };

        DummyJavaType configured = new DummyJavaType(CharSequence.class, 0, "presetVal", "presetType", false, null) {
            private static final long serialVersionUID = 1L;
            @Override
            protected JavaType _narrow(Class<?> subclass) {
                return new DummyJavaType(subclass, 0, "presetVal", "presetType", false, null);
            }
        };

        JavaType result = configured.forcedNarrowBy(String.class);
        assertEquals(String.class, result.getRawClass());
        assertEquals("presetVal", result.getValueHandler());
        assertEquals("presetType", result.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testSimpleTypeWithContentTypeThrowsIAE() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType simpleType = tf.constructType(String.class);
        try {
            simpleType.withContentType(tf.constructType(Integer.class));
            fail("Expected IllegalArgumentException when calling withContentType on SimpleType");
        } catch (IllegalArgumentException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    /*
    /**********************************************************
    /* Partition E: Object Lifecycle & Contract Integrity
    /**********************************************************
     */

    @Test(timeout = 4000)
    public void testJavaTypeIsReflectType() {
        DummyJavaType type = new DummyJavaType(String.class);
        assertTrue("JavaType must implement java.lang.reflect.Type", type instanceof java.lang.reflect.Type);
    }

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType original = tf.constructType(new TypeReference<List<String>>() {});

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        JavaType deserialized;
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            deserialized = (JavaType) ois.readObject();
        }

        assertEquals(original, deserialized);
        assertEquals(original.getRawClass(), deserialized.getRawClass());
        assertEquals(original.hashCode(), deserialized.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        DummyJavaType type1a = new DummyJavaType(String.class);
        DummyJavaType type1b = new DummyJavaType(String.class);
        DummyJavaType type2 = new DummyJavaType(Integer.class);

        assertEquals(type1a, type1b);
        assertEquals(type1b, type1a);
        assertEquals(type1a.hashCode(), type1b.hashCode());

        assertNotEquals(type1a, type2);
        assertFalse(type1a.equals(null));
        assertFalse(type1a.equals("not-a-java-type"));
    }
}