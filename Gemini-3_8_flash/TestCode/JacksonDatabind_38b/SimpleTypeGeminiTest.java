package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.databind.JavaType;

/*
 * [Branch & Defect Analysis Matrix]
 * =================================================================================================
 * Target Class: com.fasterxml.jackson.databind.type.SimpleType
 * Defects4J Defect: Issue 1102 (DeprecatedTypeHandling1102Test: testPOJOSubType, testExplicitMapType,
 *                   testExplicitCollectionType).
 * Root Cause: Deprecated SimpleType.construct(Class<?>) historically omitted super-class resolution
 *             (passing null for superClass), causing serializer/deserializer property inspection
 *             failures on POJO subtypes because inherited properties were invisible. The fix relies
 *             on _bogusSuperClass(cls) to build the skeletal super-type hierarchy.
 *
 * Decision / Branch Coverage Target:
 * 1. construct(Class<?> cls):
 *    - Map.class.isAssignableFrom(cls) -> TRUE (throw IAE), FALSE
 *    - Collection.class.isAssignableFrom(cls) -> TRUE (throw IAE), FALSE
 *    - cls.isArray() -> TRUE (throw IAE), FALSE
 *    - Hierarchy resolution via _bogusSuperClass(cls): Object vs. derived class.
 * 2. _narrow(Class<?> subclass):
 *    - _class == subclass -> TRUE (return this), FALSE (new SimpleType with this as superClass)
 * 3. withTypeHandler(Object h) & withValueHandler(Object h):
 *    - _handler == h -> TRUE (return this), FALSE (return new SimpleType)
 * 4. withStaticTyping():
 *    - _asStatic -> TRUE (return this), FALSE (return new SimpleType with true)
 * 5. buildCanonicalName() & getGenericSignature(StringBuilder):
 *    - _bindings.size() == 0 -> simple class name / signature
 *    - _bindings.size() > 0 -> loop through bindings with delimiter handling (i > 0)
 * 6. equals(Object o):
 *    - o == this -> TRUE
 *    - o == null -> FALSE
 *    - o.getClass() != getClass() -> FALSE
 *    - other._class != this._class -> FALSE
 *    - _bindings.equals(other._bindings) -> TRUE/FALSE
 * 7. Unsupported operations:
 *    - withContentType, withContentTypeHandler, withContentValueHandler -> throw IAE
 * =================================================================================================
 */
public class SimpleTypeGeminiTest {

    // Helper POJO hierarchy to verify super-type chain traversal
    static class GrandParentClass {
        public int gp;
    }

    static class ParentClass extends GrandParentClass {
        public int p;
    }

    static class ChildClass extends ParentClass {
        public int c;
    }

    // Helper generic classes with declared type parameters for TypeBindings
    static class SingleParamHolder<T> {
        public T value;
    }

    static class TwoParamHolder<K, V> {
        public K key;
        public V val;
    }

    // Subclass to test equals contract when o.getClass() != getClass()
    static class SubSimpleType extends SimpleType {
        private static final long serialVersionUID = 1L;

        public SubSimpleType(Class<?> cls) {
            super(cls);
        }
    }

    /*
     *************************************************************************************************
     * Partition A: Core Functional Logic & State Transitions
     *************************************************************************************************
     */

    @Test(timeout = 4000)
    public void testConstructUnsafeAndBasicProperties() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        assertNotNull(st);
        assertEquals(String.class, st.getRawClass());
        assertFalse(st.isContainerType());
        assertFalse(st.useStaticType());
        assertNull(st.getValueHandler());
        assertNull(st.getTypeHandler());
        assertNull(st.getContentType());
        assertEquals(0, st.containedTypeCount());
        assertNull(st.getSuperClass());
    }

    @Test(timeout = 4000)
    public void testWithTypeHandlerIdentityAndMutation() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        assertSame("Passing same null handler should return same instance", st, st.withTypeHandler(null));

        Object handler = "customTypeHandler";
        SimpleType withH = st.withTypeHandler(handler);
        assertNotSame(st, withH);
        assertEquals(handler, withH.getTypeHandler());
        assertSame("Passing same non-null handler should return same instance", withH, withH.withTypeHandler(handler));

        SimpleType withoutH = withH.withTypeHandler(null);
        assertNotSame(withH, withoutH);
        assertNull(withoutH.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testWithValueHandlerIdentityAndMutation() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        assertSame("Passing same null value handler should return same instance", st, st.withValueHandler(null));

        Object valHandler = "customValHandler";
        SimpleType withVH = st.withValueHandler(valHandler);
        assertNotSame(st, withVH);
        assertEquals(valHandler, withVH.getValueHandler());
        assertSame("Passing same non-null value handler should return same instance", withVH, withVH.withValueHandler(valHandler));

        SimpleType withoutVH = withVH.withValueHandler(null);
        assertNotSame(withVH, withoutVH);
        assertNull(withoutVH.getValueHandler());
    }

    @Test(timeout = 4000)
    public void testWithStaticTypingIdentityAndMutation() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        assertFalse(st.useStaticType());

        SimpleType staticType = st.withStaticTyping();
        assertNotSame(st, staticType);
        assertTrue(staticType.useStaticType());
        assertSame("Calling withStaticTyping when already static should return this",
                staticType, staticType.withStaticTyping());
    }

    @Test(timeout = 4000)
    public void testNarrowIdentityAndSubclass() {
        SimpleType parentType = SimpleType.constructUnsafe(ParentClass.class);
        assertSame("Narrowing to the same class should return identical instance",
                parentType, parentType._narrow(ParentClass.class));

        JavaType narrowed = parentType._narrow(ChildClass.class);
        assertNotSame(parentType, narrowed);
        assertEquals(ChildClass.class, narrowed.getRawClass());
        assertSame("Super class of narrowed type should point to original type",
                parentType, narrowed.getSuperClass());
    }

    @Test(timeout = 4000)
    public void testRefineReturnsNull() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        assertNull(st.refine(String.class, TypeBindings.emptyBindings(), null, null));
    }

    @Test(timeout = 4000)
    public void testSignaturesWithoutGenerics() {
        SimpleType st = SimpleType.constructUnsafe(String.class);

        StringBuilder erasedSb = new StringBuilder();
        st.getErasedSignature(erasedSb);
        assertEquals("Ljava/lang/String;", erasedSb.toString());

        StringBuilder genericSb = new StringBuilder();
        st.getGenericSignature(genericSb);
        assertEquals("Ljava/lang/String;", genericSb.toString());

        assertEquals("java.lang.String", st.buildCanonicalName());
        assertEquals("[simple type, class java.lang.String]", st.toString());
    }

    @Test(timeout = 4000)
    public void testGenericSignaturesAndCanonicalNameWithBindings() {
        JavaType param1 = SimpleType.constructUnsafe(String.class);
        JavaType param2 = SimpleType.constructUnsafe(Integer.class);
        TypeBindings bindings = TypeBindings.create(TwoParamHolder.class, new JavaType[] { param1, param2 });

        SimpleType boundType = new SimpleType(TwoParamHolder.class, bindings, null, null);
        assertEquals(2, boundType.containedTypeCount());
        assertEquals(param1, boundType.containedType(0));
        assertEquals(param2, boundType.containedType(1));
        assertNull(boundType.containedType(2));

        String expectedCanonical = TwoParamHolder.class.getName() + "<java.lang.String,java.lang.Integer>";
        assertEquals(expectedCanonical, boundType.buildCanonicalName());
        assertEquals("[simple type, class " + expectedCanonical + "]", boundType.toString());

        StringBuilder erasedSb = new StringBuilder();
        boundType.getErasedSignature(erasedSb);
        String internalTwoParamHolder = TwoParamHolder.class.getName().replace('.', '/');
        assertEquals("L" + internalTwoParamHolder + ";", erasedSb.toString());

        StringBuilder genericSb = new StringBuilder();
        boundType.getGenericSignature(genericSb);
        String expectedGeneric = "L" + internalTwoParamHolder + "<Ljava/lang/String;Ljava/lang/Integer;>;";
        assertEquals(expectedGeneric, genericSb.toString());
    }

    @Test(timeout = 4000)
    public void testCanonicalNameWithSingleGenericBinding() {
        JavaType param = SimpleType.constructUnsafe(Double.class);
        TypeBindings bindings = TypeBindings.create(SingleParamHolder.class, new JavaType[] { param });

        SimpleType boundType = new SimpleType(SingleParamHolder.class, bindings, null, null);
        String expectedCanonical = SingleParamHolder.class.getName() + "<java.lang.Double>";
        assertEquals(expectedCanonical, boundType.buildCanonicalName());
    }

    /*
     *************************************************************************************************
     * Partition B: Boundary Value Analysis & Constructor Variants
     *************************************************************************************************
     */

    @Test(timeout = 4000)
    public void testProtectedConstructorsDirectly() {
        // Constructor: SimpleType(Class<?> cls)
        SimpleType st1 = new SimpleType(Boolean.class);
        assertEquals(Boolean.class, st1.getRawClass());
        assertEquals(TypeBindings.emptyBindings(), st1.getBindings());
        assertNull(st1.getSuperClass());

        // Constructor: SimpleType(Class<?> cls, TypeBindings, JavaType, JavaType[])
        JavaType[] interfaces = new JavaType[] { SimpleType.constructUnsafe(Comparable.class) };
        JavaType superClass = SimpleType.constructUnsafe(Object.class);
        SimpleType st2 = new SimpleType(String.class, TypeBindings.emptyBindings(), superClass, interfaces);
        assertEquals(superClass, st2.getSuperClass());
        assertEquals(1, st2.getInterfaces().size());
        assertEquals(interfaces[0], st2.getInterfaces().get(0));

        // Constructor: SimpleType(TypeBase base) copy constructor
        SimpleType copy = new SimpleType(st2);
        assertEquals(st2, copy);
        assertEquals(superClass, copy.getSuperClass());

        // Constructor with extraHash
        SimpleType stWithHash = new SimpleType(String.class, TypeBindings.emptyBindings(),
                null, null, 12345, null, null, false);
        assertEquals(String.class, stWithHash.getRawClass());
        assertNotEquals(0, stWithHash.hashCode());
    }

    @Test(timeout = 4000)
    public void testConstructUnsafeAllowsMapAndCollectionAndArray() {
        // constructUnsafe does NOT guard against Map/Collection/Array
        SimpleType mapType = SimpleType.constructUnsafe(Map.class);
        assertEquals(Map.class, mapType.getRawClass());

        SimpleType colType = SimpleType.constructUnsafe(Collection.class);
        assertEquals(Collection.class, colType.getRawClass());

        SimpleType arrType = SimpleType.constructUnsafe(int[].class);
        assertEquals(int[].class, arrType.getRawClass());
    }

    /*
     *************************************************************************************************
     * Partition C: Defect-Targeted Branch Zone (Defects4J Issue 1102)
     *************************************************************************************************
     */

    /**
     * TARGETS DEFECT: Issue 1102 / DeprecatedTypeHandling1102Test.
     * Deprecated SimpleType.construct(cls) previously constructed the type without resolving its
     * super-class hierarchy (_bogusSuperClass), leading to null getSuperClass() and deserialization
     * errors when introspecting base class properties (e.g. UnrecognizedPropertyException: field "x").
     */
    @Test(timeout = 4000)
    public void testDefect1102SuperClassHierarchyPopulatedInConstruct() {
        SimpleType childType = SimpleType.construct(ChildClass.class);
        assertNotNull("SimpleType.construct must populate super-class hierarchy", childType.getSuperClass());
        assertEquals(ParentClass.class, childType.getSuperClass().getRawClass());

        JavaType grandParentType = childType.getSuperClass().getSuperClass();
        assertNotNull("Super-class hierarchy should extend to GrandParentClass", grandParentType);
        assertEquals(GrandParentClass.class, grandParentType.getRawClass());

        JavaType objectType = grandParentType.getSuperClass();
        assertNotNull("Super-class hierarchy should terminate at Object.class", objectType);
        assertEquals(Object.class, objectType.getRawClass());

        assertNull("Object's super-class must be null", objectType.getSuperClass());
    }

    @Test(timeout = 4000)
    public void testConstructOnObjectClassHasNullSuperClass() {
        SimpleType objectType = SimpleType.construct(Object.class);
        assertEquals(Object.class, objectType.getRawClass());
        assertNull("Object.class has no super class", objectType.getSuperClass());
    }

    /*
     *************************************************************************************************
     * Partition D: Exception & Defensive Guard Paths
     *************************************************************************************************
     */

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructFailsForMapInterface() {
        SimpleType.construct(Map.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructFailsForMapImplementation() {
        SimpleType.construct(HashMap.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructFailsForTreeMap() {
        SimpleType.construct(TreeMap.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructFailsForCollectionInterface() {
        SimpleType.construct(Collection.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructFailsForListInterface() {
        SimpleType.construct(List.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructFailsForArrayList() {
        SimpleType.construct(ArrayList.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructFailsForLinkedList() {
        SimpleType.construct(LinkedList.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructFailsForObjectArray() {
        SimpleType.construct(String[].class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructFailsForPrimitiveArray() {
        SimpleType.construct(int[].class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithContentTypeThrowsIllegalArgumentException() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        st.withContentType(SimpleType.constructUnsafe(Integer.class));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithContentTypeHandlerThrowsIllegalArgumentException() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        st.withContentTypeHandler("handler");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithContentValueHandlerThrowsIllegalArgumentException() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        st.withContentValueHandler("handler");
    }

    /*
     *************************************************************************************************
     * Partition E: Object Lifecycle & Contract Integrity (equals, hashCode)
     *************************************************************************************************
     */

    @Test(timeout = 4000)
    public void testEqualsContract() {
        SimpleType st1 = SimpleType.constructUnsafe(String.class);
        SimpleType st2 = SimpleType.constructUnsafe(String.class);
        SimpleType st3 = SimpleType.constructUnsafe(Integer.class);

        // Reflexive
        assertTrue(st1.equals(st1));

        // Symmetric
        assertTrue(st1.equals(st2));
        assertTrue(st2.equals(st1));

        // Null comparison
        assertFalse(st1.equals(null));

        // Different target classes
        assertFalse(st1.equals(st3));

        // Different object types
        assertFalse(st1.equals("Some String Object"));

        // Subclass comparison: other.getClass() != getClass()
        SubSimpleType subSt = new SubSimpleType(String.class);
        assertFalse("Comparison with a subclass of SimpleType must return false", st1.equals(subSt));
    }

    @Test(timeout = 4000)
    public void testEqualsWithGenericBindings() {
        JavaType strType = SimpleType.constructUnsafe(String.class);
        JavaType intType = SimpleType.constructUnsafe(Integer.class);

        TypeBindings b1 = TypeBindings.create(SingleParamHolder.class, new JavaType[] { strType });
        TypeBindings b2 = TypeBindings.create(SingleParamHolder.class, new JavaType[] { strType });
        TypeBindings b3 = TypeBindings.create(SingleParamHolder.class, new JavaType[] { intType });

        SimpleType t1 = new SimpleType(SingleParamHolder.class, b1, null, null);
        SimpleType t2 = new SimpleType(SingleParamHolder.class, b2, null, null);
        SimpleType t3 = new SimpleType(SingleParamHolder.class, b3, null, null);

        assertTrue("Identical bindings should equal", t1.equals(t2));
        assertEquals("Identical bindings must produce same hashCode", t1.hashCode(), t2.hashCode());

        assertFalse("Different type parameters should not equal", t1.equals(t3));
    }
}