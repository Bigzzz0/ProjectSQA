package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * /* [Branch & Defect Analysis Matrix]
 * Target Class: com.fasterxml.jackson.databind.type.SimpleType
 *
 * 1. Factory Methods & Sanity Guards:
 *    - construct(Class<?> cls):
 *      - Branch Map.class.isAssignableFrom(cls) -> throws IllegalArgumentException
 *      - Branch Collection.class.isAssignableFrom(cls) -> throws IllegalArgumentException
 *      - Branch cls.isArray() -> throws IllegalArgumentException
 *      - Normal Class -> constructs SimpleType with skeletal superclasses via _buildSuperClass
 *    - _buildSuperClass(Class<?> superClass, TypeBindings b):
 *      - superClass == null -> null
 *      - superClass == Object.class -> TypeFactory.unknownType()
 *      - superClass in deep hierarchy -> recursively constructs SimpleType chain
 *    - constructUnsafe(Class<?> raw): creates SimpleType with empty supertypes/bindings
 *
 * 2. Immutable Modifier Methods:
 *    - withTypeHandler(Object h): identity check (h == _typeHandler) vs new SimpleType with handler
 *    - withValueHandler(Object h): identity check (h == _valueHandler) vs new SimpleType with handler
 *    - withStaticTyping(): _asStatic true -> this; false -> new SimpleType with asStatic=true
 *    - Unsupported container operations:
 *      - withContentType(JavaType) -> throws IllegalArgumentException
 *      - withContentTypeHandler(Object) -> throws IllegalArgumentException
 *      - withContentValueHandler(Object) -> throws IllegalArgumentException
 *
 * 3. Type Resolution & Narrowing:
 *    - _narrow(Class<?> subclass): subclass == _class -> this; otherwise new SimpleType with this as superClass
 *    - refine(Class<?>, TypeBindings, JavaType, JavaType[]): returns null (SimpleType is not specialized)
 *    - isContainerType(): returns false
 *
 * 4. Signatures & Canonical Names:
 *    - buildCanonicalName(): no generic bindings vs with TypeBindings
 *    - getErasedSignature(StringBuilder): calls _classSignature(cls, sb, true)
 *    - getGenericSignature(StringBuilder): calls _classSignature(cls, sb, false), iterates bindings if any
 *
 * 5. Lifecycle & Contract:
 *    - equals: identical ref -> true; null -> false; different class -> false;
 *              different raw class -> false; identical raw class + different/same bindings
 *    - toString(): formatted representation check
 *
 * 6. Defect-Targeted Zone (Jackson Issue #1125 / Defects4J):
 *    - TestSubtypes::testIssue1125WithDefault: polymorphic type resolution with defaultImpl
 *      subclassing base class where defaultImpl has additional properties ("b").
 */
public class SimpleTypeGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructAndBasicProperties() {
        SimpleType st = SimpleType.construct(String.class);
        assertNotNull("SimpleType should be created", st);
        assertEquals(String.class, st.getRawClass());
        assertFalse("SimpleType is not a container", st.isContainerType());
        assertNull("SimpleType has no content type", st.getContentType());
        assertEquals(0, st.containedTypeCount());
        assertNotNull("Super class should not be null for String", st.getSuperClass());
    }

    @Test(timeout = 4000)
    public void testWithTypeHandlerTransitions() {
        SimpleType st = SimpleType.construct(String.class);
        assertNull(st.getTypeHandler());

        Object handler1 = "handler1";
        SimpleType withHandler = st.withTypeHandler(handler1);
        assertNotSame(st, withHandler);
        assertSame(handler1, withHandler.getTypeHandler());

        // Identity check: same handler returns `this`
        SimpleType idempotent = withHandler.withTypeHandler(handler1);
        assertSame(withHandler, idempotent);

        // Transition back to null
        SimpleType backToNull = withHandler.withTypeHandler(null);
        assertNotSame(withHandler, backToNull);
        assertNull(backToNull.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testWithValueHandlerTransitions() {
        SimpleType st = SimpleType.construct(Integer.class);
        assertNull(st.getValueHandler());

        Object valHandler = new Object();
        SimpleType withHandler = st.withValueHandler(valHandler);
        assertNotSame(st, withHandler);
        assertSame(valHandler, withHandler.getValueHandler());

        // Identity check
        SimpleType idempotent = withHandler.withValueHandler(valHandler);
        assertSame(withHandler, idempotent);

        // Transition to another handler
        Object valHandler2 = new Object();
        SimpleType withHandler2 = withHandler.withValueHandler(valHandler2);
        assertNotSame(withHandler, withHandler2);
        assertSame(valHandler2, withHandler2.getValueHandler());
    }

    @Test(timeout = 4000)
    public void testWithStaticTyping() {
        SimpleType st = SimpleType.construct(Double.class);
        assertFalse(st.useStaticType());

        SimpleType staticType = st.withStaticTyping();
        assertNotSame(st, staticType);
        assertTrue(staticType.useStaticType());

        // Calling again should return `this`
        SimpleType idempotent = staticType.withStaticTyping();
        assertSame(staticType, idempotent);
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testNarrowing() {
        SimpleType base = SimpleType.construct(Number.class);

        // Narrow to same class returns `this`
        JavaType sameNarrow = base._narrow(Number.class);
        assertSame(base, sameNarrow);

        // Narrow to subclass
        JavaType subNarrow = base._narrow(Integer.class);
        assertNotSame(base, subNarrow);
        assertEquals(Integer.class, subNarrow.getRawClass());
        assertEquals(base, subNarrow.getSuperClass());
    }

    @Test(timeout = 4000)
    public void testRefineReturnsNull() {
        SimpleType st = SimpleType.construct(Number.class);
        JavaType refined = st.refine(Integer.class, TypeBindings.emptyBindings(), null, null);
        assertNull("SimpleType.refine should return null", refined);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    static class MultiLevelChild extends Number {
        private static final long serialVersionUID = 1L;
        @Override public int intValue() { return 0; }
        @Override public long longValue() { return 0; }
        @Override public float floatValue() { return 0; }
        @Override public double doubleValue() { return 0; }
    }

    @Test(timeout = 4000)
    public void testSuperClassHierarchyTraversal() {
        // MultiLevelChild -> Number -> Object
        SimpleType child = SimpleType.construct(MultiLevelChild.class);
        assertNotNull(child.getSuperClass());
        assertEquals(Number.class, child.getSuperClass().getRawClass());

        JavaType numberSuper = child.getSuperClass().getSuperClass();
        assertNotNull(numberSuper);
        assertEquals(Object.class, numberSuper.getRawClass());
        assertNull("Object superclass has no superclass", numberSuper.getSuperClass());
    }

    @Test(timeout = 4000)
    public void testConstructObjectDirectly() {
        SimpleType objType = SimpleType.construct(Object.class);
        assertEquals(Object.class, objType.getRawClass());
        assertNull("Object.class superclass must be null", objType.getSuperClass());
    }

    @Test(timeout = 4000)
    public void testConstructUnsafe() {
        SimpleType unsafe = SimpleType.constructUnsafe(CharSequence.class);
        assertEquals(CharSequence.class, unsafe.getRawClass());
        assertNull(unsafe.getSuperClass());
        assertEquals(0, unsafe.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testGenericSignatureAndCanonicalNameWithBindings() {
        JavaType stringType = SimpleType.construct(String.class);
        TypeBindings bindings = TypeBindings.create(Comparable.class, new JavaType[]{ stringType });
        SimpleType parameterized = new SimpleType(Comparable.class, bindings, null, null, null, null, false);

        assertEquals(1, parameterized.containedTypeCount());
        assertEquals(stringType, parameterized.containedType(0));

        String canonical = parameterized.buildCanonicalName();
        assertEquals("java.lang.Comparable<java.lang.String>", canonical);

        StringBuilder sbErased = new StringBuilder();
        parameterized.getErasedSignature(sbErased);
        assertEquals("Ljava/lang/Comparable;", sbErased.toString());

        StringBuilder sbGeneric = new StringBuilder();
        parameterized.getGenericSignature(sbGeneric);
        assertEquals("Ljava/lang/Comparable<Ljava/lang/String;>;", sbGeneric.toString());
    }

    @Test(timeout = 4000)
    public void testMultipleBindingsSignatures() {
        JavaType strType = SimpleType.construct(String.class);
        JavaType intType = SimpleType.construct(Integer.class);
        TypeBindings bindings = TypeBindings.create(Map.Entry.class, new JavaType[]{ strType, intType });
        SimpleType pairType = new SimpleType(Map.Entry.class, bindings, null, null, null, null, false);

        assertEquals("java.util.Map$Entry<java.lang.String,java.lang.Integer>", pairType.buildCanonicalName());

        StringBuilder sbGeneric = new StringBuilder();
        pairType.getGenericSignature(sbGeneric);
        assertEquals("Ljava/util/Map$Entry<Ljava/lang/String;Ljava/lang/Integer;>;", sbGeneric.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Issue #1125)
    // =========================================================================

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", defaultImpl = Default1125.class)
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Sub1125.class, name = "sub")
    })
    static class Poly1125 {
        public int a;
        public String def;
    }

    static class Sub1125 extends Poly1125 {
        public int subVal;
    }

    static class Default1125 extends Poly1125 {
        public int b;
    }

    /**
     * Targets Defects4J issue where defaultImpl (Default1125) subclassing Poly1125
     * fails during deserialization when fields from the defaultImpl subclass ("b")
     * are present in the JSON payload without an explicit type discriminator.
     */
    @Test(timeout = 4000)
    public void testIssue1125WithDefaultDeserialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String json = "{\"a\":1,\"b\":2,\"def\":\"test\"}";

        Poly1125 result = mapper.readValue(json, Poly1125.class);
        assertNotNull("Deserialized result should not be null", result);
        assertTrue("Expected instance of Default1125, got: " + result.getClass().getName(),
                result instanceof Default1125);

        Default1125 defResult = (Default1125) result;
        assertEquals(1, defResult.a);
        assertEquals(2, defResult.b);
        assertEquals("test", defResult.def);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructRejectsMap() {
        SimpleType.construct(Map.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructRejectsMapSubclass() {
        SimpleType.construct(HashMap.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructRejectsCollection() {
        SimpleType.construct(Collection.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructRejectsCollectionSubclass() {
        SimpleType.construct(ArrayList.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructRejectsArray() {
        SimpleType.construct(String[].class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithContentTypeThrows() {
        SimpleType st = SimpleType.construct(String.class);
        st.withContentType(SimpleType.construct(Integer.class));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithContentTypeHandlerThrows() {
        SimpleType st = SimpleType.construct(String.class);
        st.withContentTypeHandler("handler");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithContentValueHandlerThrows() {
        SimpleType st = SimpleType.construct(String.class);
        st.withContentValueHandler("handler");
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        SimpleType str1 = SimpleType.construct(String.class);
        SimpleType str2 = SimpleType.construct(String.class);
        SimpleType intType = SimpleType.construct(Integer.class);

        // Reflexive
        assertTrue(str1.equals(str1));

        // Symmetric
        assertTrue(str1.equals(str2));
        assertTrue(str2.equals(str1));
        assertEquals(str1.hashCode(), str2.hashCode());

        // Null and different class
        assertFalse(str1.equals(null));
        assertFalse(str1.equals("a string"));

        // Different raw classes
        assertFalse(str1.equals(intType));

        // Different bindings
        JavaType boundStr = SimpleType.construct(String.class);
        TypeBindings b1 = TypeBindings.create(Comparable.class, new JavaType[]{ boundStr });
        TypeBindings b2 = TypeBindings.emptyBindings();
        SimpleType parameterized1 = new SimpleType(Comparable.class, b1, null, null, null, null, false);
        SimpleType parameterized2 = new SimpleType(Comparable.class, b2, null, null, null, null, false);

        assertFalse(parameterized1.equals(parameterized2));
        assertFalse(parameterized2.equals(parameterized1));
    }

    @Test(timeout = 4000)
    public void testToStringFormat() {
        SimpleType st = SimpleType.construct(Long.class);
        String str = st.toString();
        assertNotNull(str);
        assertTrue("toString should contain class name", str.contains("java.lang.Long"));
        assertTrue("toString should follow '[simple type, class ...]' pattern",
                str.startsWith("[simple type, class ") && str.endsWith("]"));
    }

    @Test(timeout = 4000)
    public void testCopyConstructor() {
        SimpleType original = SimpleType.construct(Float.class);
        SimpleType copy = new SimpleType(original);
        assertEquals(original, copy);
        assertEquals(original.getRawClass(), copy.getRawClass());
    }

    @Test(timeout = 4000)
    public void testPassThroughConstructor() {
        JavaType[] interfaces = new JavaType[]{ SimpleType.construct(Comparable.class) };
        JavaType superCls = SimpleType.construct(Number.class);
        SimpleType custom = new SimpleType(
                Double.class, TypeBindings.emptyBindings(), superCls, interfaces,
                123, "valHandler", "typeHandler", true
        );

        assertEquals(Double.class, custom.getRawClass());
        assertEquals(superCls, custom.getSuperClass());
        assertEquals("valHandler", custom.getValueHandler());
        assertEquals("typeHandler", custom.getTypeHandler());
        assertTrue(custom.useStaticType());
        assertEquals(1, custom.getInterfaces().size());
    }
}