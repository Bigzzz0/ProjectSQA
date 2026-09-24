package com.fasterxml.jackson.databind;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.type.SimpleType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.StdConverter;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.JavaType
 *
 * Targeted Decision Branches & Boundary Conditions:
 * 1. narrowBy(Class<?>):
 *    - Branch: subclass == _class -> return this
 *    - Branch: subclass is not assignable -> throws IllegalArgumentException
 *    - Branch: subclass is assignable -> _narrow called, handlers (_valueHandler, _typeHandler) preserved
 * 2. forcedNarrowBy(Class<?>):
 *    - Branch: subclass == _class -> return this
 *    - Branch: subclass != _class -> _narrow called, handlers preserved
 * 3. widenBy(Class<?>):
 *    - Branch: superclass == _class -> return this
 *    - Branch: superclass is not assignable from _class -> throws IllegalArgumentException
 *    - Branch: superclass is assignable -> _widen called
 * 4. isConcrete():
 *    - Branch: non-abstract class -> true
 *    - Branch: interface -> false
 *    - Branch: abstract class -> false
 *    - Branch: primitive type -> true (special branch: primitives have abstract flag set in reflection)
 * 5. Type erasure & classification queries:
 *    - isThrowable(): Throwable subclasses vs non-throwable
 *    - isInterface(): interface vs class
 *    - isEnumType(): Enum vs non-enum
 *    - isPrimitive(): primitive vs object
 *    - isFinal(): final class vs non-final
 *    - hasRawClass(Class<?>): exact identity check
 *    - useStaticType(): boolean flag accessor
 * 6. Contained types & generic signatures:
 *    - containedTypeOrUnknown(int): returns contained type if present, or TypeFactory.unknownType() if null
 *    - hasGenericTypes(): containedTypeCount() > 0 vs 0
 *    - Default fallbacks for getKeyType(), getContentType(), containedTypeName()
 * 7. Known Defect Ground Truth:
 *    - TestConvertingSerializer::testIssue731: Empty bean annotated with @JsonSerialize(converter=...)
 *      must be converted and serialized properly without failing on FAIL_ON_EMPTY_BEANS.
 */
public class JavaTypeGeminiTest {

    // Custom concrete subclass to directly test protected/default implementations of JavaType
    private static class ConcreteTestType extends JavaType {
        private static final long serialVersionUID = 1L;

        private final JavaType _containedType;

        protected ConcreteTestType(Class<?> raw, int extraHash, Object vh, Object th, boolean asStatic, JavaType contained) {
            super(raw, extraHash, vh, th, asStatic);
            _containedType = contained;
        }

        public static ConcreteTestType construct(Class<?> raw) {
            return new ConcreteTestType(raw, 0, null, null, false, null);
        }

        public static ConcreteTestType construct(Class<?> raw, Object vh, Object th, boolean asStatic) {
            return new ConcreteTestType(raw, 0, vh, th, asStatic, null);
        }

        public static ConcreteTestType construct(Class<?> raw, JavaType contained) {
            return new ConcreteTestType(raw, 0, null, null, false, contained);
        }

        @Override
        public JavaType withTypeHandler(Object h) {
            return new ConcreteTestType(_class, _hash - _class.getName().hashCode(), _valueHandler, h, _asStatic, _containedType);
        }

        @Override
        public JavaType withContentTypeHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withValueHandler(Object h) {
            return new ConcreteTestType(_class, _hash - _class.getName().hashCode(), h, _typeHandler, _asStatic, _containedType);
        }

        @Override
        public JavaType withContentValueHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withStaticTyping() {
            return new ConcreteTestType(_class, _hash - _class.getName().hashCode(), _valueHandler, _typeHandler, true, _containedType);
        }

        @Override
        protected JavaType _narrow(Class<?> subclass) {
            return new ConcreteTestType(subclass, 0, null, null, _asStatic, _containedType);
        }

        @Override
        public JavaType narrowContentsBy(Class<?> contentClass) {
            return this;
        }

        @Override
        public JavaType widenContentsBy(Class<?> contentClass) {
            return this;
        }

        @Override
        public boolean isContainerType() {
            return false;
        }

        @Override
        public Class<?> getParameterSource() {
            return _class;
        }

        @Override
        public StringBuilder getGenericSignature(StringBuilder sb) {
            sb.append("L").append(_class.getName().replace('.', '/')).append(";");
            return sb;
        }

        @Override
        public StringBuilder getErasedSignature(StringBuilder sb) {
            sb.append("L").append(_class.getName().replace('.', '/')).append(";");
            return sb;
        }

        @Override
        public String toString() {
            return "[ConcreteTestType: " + _class.getName() + "]";
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null || o.getClass() != getClass()) return false;
            ConcreteTestType other = (ConcreteTestType) o;
            return other._class == _class;
        }

        @Override
        public int containedTypeCount() {
            return _containedType == null ? 0 : 1;
        }

        @Override
        public JavaType containedType(int index) {
            return index == 0 ? _containedType : null;
        }
    }

    private enum SampleEnum {
        A, B
    }

    private static abstract class AbstractSample {
    }

    private static final class FinalSample {
    }

    /*
     * -------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * -------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testRawClassAndIdentity() {
        JavaType type = ConcreteTestType.construct(String.class);
        assertSame(String.class, type.getRawClass());
        assertTrue(type.hasRawClass(String.class));
        assertFalse(type.hasRawClass(Integer.class));
        assertFalse(type.hasRawClass(null));
    }

    @Test(timeout = 4000)
    public void testModifiersAndTypeQueries() {
        JavaType strType = ConcreteTestType.construct(String.class);
        assertTrue(strType.isConcrete());
        assertFalse(strType.isAbstract());
        assertFalse(strType.isInterface());
        assertFalse(strType.isPrimitive());
        assertTrue(strType.isFinal());
        assertFalse(strType.isEnumType());
        assertFalse(strType.isThrowable());
        assertFalse(strType.isArrayType());
        assertFalse(strType.isContainerType());
        assertFalse(strType.isCollectionLikeType());
        assertFalse(strType.isMapLikeType());

        JavaType absType = ConcreteTestType.construct(AbstractSample.class);
        assertFalse(absType.isConcrete());
        assertTrue(absType.isAbstract());
        assertFalse(absType.isFinal());

        JavaType ifaceType = ConcreteTestType.construct(List.class);
        assertFalse(ifaceType.isConcrete());
        assertTrue(ifaceType.isAbstract());
        assertTrue(ifaceType.isInterface());

        JavaType finalType = ConcreteTestType.construct(FinalSample.class);
        assertTrue(finalType.isConcrete());
        assertTrue(finalType.isFinal());

        JavaType enumType = ConcreteTestType.construct(SampleEnum.class);
        assertTrue(enumType.isEnumType());

        JavaType throwableType = ConcreteTestType.construct(Exception.class);
        assertTrue(throwableType.isThrowable());

        JavaType errorType = ConcreteTestType.construct(Error.class);
        assertTrue(errorType.isThrowable());
    }

    @Test(timeout = 4000)
    public void testPrimitiveIsConcreteBranch() {
        // Primitives have Modifier.ABSTRACT set in reflection, but isConcrete() must return true
        JavaType intType = ConcreteTestType.construct(int.class);
        assertTrue(intType.isPrimitive());
        assertTrue(intType.isConcrete());
        assertFalse(intType.isAbstract());

        JavaType voidType = ConcreteTestType.construct(void.class);
        assertTrue(voidType.isPrimitive());
        assertTrue(voidType.isConcrete());
        assertFalse(voidType.isAbstract());
    }

    @Test(timeout = 4000)
    public void testNarrowBySelfReturnsSameInstance() {
        JavaType type = ConcreteTestType.construct(CharSequence.class);
        JavaType result = type.narrowBy(CharSequence.class);
        assertSame(type, result);
    }

    @Test(timeout = 4000)
    public void testForcedNarrowBySelfReturnsSameInstance() {
        JavaType type = ConcreteTestType.construct(CharSequence.class);
        JavaType result = type.forcedNarrowBy(CharSequence.class);
        assertSame(type, result);
    }

    @Test(timeout = 4000)
    public void testNarrowBySubclassPreservesHandlers() {
        Object vh = "valueHandlerString";
        Object th = "typeHandlerString";
        JavaType base = ConcreteTestType.construct(CharSequence.class, vh, th, false);

        JavaType narrowed = base.narrowBy(String.class);
        assertEquals(String.class, narrowed.getRawClass());
        assertEquals(vh, narrowed.getValueHandler());
        assertEquals(th, narrowed.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testForcedNarrowBySubclassPreservesHandlers() {
        Object vh = 12345;
        Object th = 67890;
        JavaType base = ConcreteTestType.construct(Number.class, vh, th, false);

        JavaType narrowed = base.forcedNarrowBy(Integer.class);
        assertEquals(Integer.class, narrowed.getRawClass());
        assertEquals(vh, narrowed.getValueHandler());
        assertEquals(th, narrowed.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testWidenBySelfReturnsSameInstance() {
        JavaType type = ConcreteTestType.construct(String.class);
        JavaType result = type.widenBy(String.class);
        assertSame(type, result);
    }

    @Test(timeout = 4000)
    public void testWidenBySuperclass() {
        JavaType type = ConcreteTestType.construct(String.class);
        JavaType widened = type.widenBy(CharSequence.class);
        assertEquals(CharSequence.class, widened.getRawClass());

        JavaType widenedToObject = type.widenBy(Object.class);
        assertEquals(Object.class, widenedToObject.getRawClass());
    }

    @Test(timeout = 4000)
    public void testStaticTypingAccessorAndModifier() {
        JavaType dynamicType = ConcreteTestType.construct(String.class, null, null, false);
        assertFalse(dynamicType.useStaticType());

        JavaType staticType = dynamicType.withStaticTyping();
        assertTrue(staticType.useStaticType());
    }

    /*
     * -------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * -------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testGenericTypeHandlingNoGenerics() {
        JavaType type = ConcreteTestType.construct(String.class);
        assertFalse(type.hasGenericTypes());
        assertNull(type.getKeyType());
        assertNull(type.getContentType());
        assertEquals(0, type.containedTypeCount());
        assertNull(type.containedType(0));
        assertNull(type.containedType(-1));
        assertNull(type.containedType(999));
        assertNull(type.containedTypeName(0));

        // containedTypeOrUnknown returns TypeFactory.unknownType() when index is out of bounds or null
        JavaType unknown = type.containedTypeOrUnknown(0);
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());
    }

    @Test(timeout = 4000)
    public void testGenericTypeHandlingWithContainedType() {
        JavaType inner = ConcreteTestType.construct(Integer.class);
        JavaType type = ConcreteTestType.construct(List.class, inner);

        assertTrue(type.hasGenericTypes());
        assertEquals(1, type.containedTypeCount());
        assertSame(inner, type.containedType(0));
        assertSame(inner, type.containedTypeOrUnknown(0));

        // Out of bounds index
        assertNull(type.containedType(1));
        JavaType unknown = type.containedTypeOrUnknown(1);
        assertNotNull(unknown);
        assertEquals(Object.class, unknown.getRawClass());
    }

    @Test(timeout = 4000)
    public void testSignaturesErasedAndGeneric() {
        JavaType type = ConcreteTestType.construct(String.class);
        String erased = type.getErasedSignature();
        String generic = type.getGenericSignature();

        assertEquals("Ljava/lang/String;", erased);
        assertEquals("Ljava/lang/String;", generic);
    }

    /*
     * -------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone (TestConvertingSerializer::testIssue731)
     * -------------------------------------------------------------------
     */

    @JsonSerialize(converter = Issue731Converter.class)
    public static class Issue731DummyBean {
        // Deliberately empty bean: should NOT fail with FAIL_ON_EMPTY_BEANS when converter is present
    }

    public static class Issue731Converter extends StdConverter<Issue731DummyBean, Map<String, Object>> {
        @Override
        public Map<String, Object> convert(Issue731DummyBean value) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("convertedKey", "convertedValue");
            return map;
        }
    }

    @Test(timeout = 4000)
    public void testIssue731ConvertingSerializerOnEmptyBean() throws Exception {
        // Ground truth defect: TestConvertingSerializer::testIssue731
        // Empty bean with @JsonSerialize(converter=...) must successfully convert and serialize to JSON
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(new Issue731DummyBean());
        assertEquals("{\"convertedKey\":\"convertedValue\"}", json);
    }

    @Test(timeout = 4000)
    public void testNarrowByWithHandlersAlreadyMatching() {
        // Test branch where _narrow returns instance with same handlers
        JavaType base = ConcreteTestType.construct(Number.class, "vHandler", "tHandler", false);
        JavaType customNarrowed = new ConcreteTestType(Integer.class, 0, "vHandler", "tHandler", false, null);

        JavaType mockJavaType = new ConcreteTestType(Number.class, 0, "vHandler", "tHandler", false, null) {
            private static final long serialVersionUID = 1L;

            @Override
            protected JavaType _narrow(Class<?> subclass) {
                return customNarrowed;
            }
        };

        JavaType result = mockJavaType.narrowBy(Integer.class);
        assertSame(customNarrowed, result);
    }

    /*
     * -------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * -------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testNarrowByIncompatibleClassThrowsException() {
        JavaType strType = ConcreteTestType.construct(String.class);
        try {
            strType.narrowBy(Integer.class);
            fail("Expected IllegalArgumentException when narrowing String to Integer");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("is not assignable to"));
            assertTrue(e.getMessage().contains("java.lang.Integer"));
            assertTrue(e.getMessage().contains("java.lang.String"));
        }
    }

    @Test(timeout = 4000)
    public void testWidenByIncompatibleClassThrowsException() {
        JavaType intType = ConcreteTestType.construct(Integer.class);
        try {
            intType.widenBy(String.class);
            fail("Expected IllegalArgumentException when widening Integer to String");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("is not assignable to"));
            assertTrue(e.getMessage().contains("java.lang.Integer"));
            assertTrue(e.getMessage().contains("java.lang.String"));
        }
    }

    @Test(timeout = 4000)
    public void testAssertSubclassWithIncompatibleHierarchy() {
        ConcreteTestType type = ConcreteTestType.construct(ArrayList.class);
        try {
            type._assertSubclass(HashMap.class, ArrayList.class);
            fail("Expected IllegalArgumentException for unrelated classes");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Class java.util.HashMap is not assignable to java.util.ArrayList"));
        }
    }

    /*
     * -------------------------------------------------------------------
     * Partition E: Object Lifecycle & Contract Integrity
     * -------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testHashCodeContract() {
        int extraHash = 42;
        ConcreteTestType type1 = new ConcreteTestType(String.class, extraHash, null, null, false, null);
        int expectedHash = String.class.getName().hashCode() + extraHash;
        assertEquals(expectedHash, type1.hashCode());
    }

    @Test(timeout = 4000)
    public void testTypeFactoryConstructedJavaTypeIntegrity() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType simpleType = tf.constructType(String.class);

        assertNotNull(simpleType);
        assertEquals(String.class, simpleType.getRawClass());
        assertTrue(simpleType instanceof SimpleType);
        assertNull(simpleType.getValueHandler());
        assertNull(simpleType.getTypeHandler());

        JavaType withVal = simpleType.withValueHandler("valCodec");
        assertEquals("valCodec", withVal.getValueHandler());

        JavaType withType = simpleType.withTypeHandler("typeCodec");
        assertEquals("typeCodec", withType.getTypeHandler());

        assertEquals(simpleType.getRawClass(), withVal.getRawClass());
        assertEquals(simpleType.getRawClass(), withType.getRawClass());
    }

    @Test(timeout = 4000)
    public void testTypeFactoryContainerTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType listType = tf.constructCollectionType(ArrayList.class, String.class);

        assertTrue(listType.isContainerType());
        assertTrue(listType.isCollectionLikeType());
        assertFalse(listType.isMapLikeType());
        assertEquals(1, listType.containedTypeCount());
        assertEquals(String.class, listType.getContentType().getRawClass());

        JavaType mapType = tf.constructMapType(HashMap.class, String.class, Integer.class);
        assertTrue(mapType.isContainerType());
        assertTrue(mapType.isMapLikeType());
        assertFalse(mapType.isCollectionLikeType());
        assertEquals(2, mapType.containedTypeCount());
        assertEquals(String.class, mapType.getKeyType().getRawClass());
        assertEquals(Integer.class, mapType.getContentType().getRawClass());
    }

    @Test(timeout = 4000)
    public void testSerializationSupport() {
        JavaType type = ConcreteTestType.construct(String.class);
        assertTrue(type instanceof Serializable);
        assertTrue(type instanceof java.lang.reflect.Type);
    }
}