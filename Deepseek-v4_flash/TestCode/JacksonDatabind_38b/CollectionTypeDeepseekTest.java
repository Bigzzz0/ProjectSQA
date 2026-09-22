package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Branch coverage targets:
 * - construct(Class, TypeBindings, JavaType, JavaType[], JavaType): normal, null bindings, null superClass, null superInts
 * - construct(Class, JavaType) [deprecated]: null bindings, bogus superClass
 * - _narrow(Class) [deprecated]: subclass narrowing
 * - withContentType(JavaType): same, different, null contentType
 * - withTypeHandler(Object), withContentTypeHandler(Object), withValueHandler(Object), withContentValueHandler(Object): null, non-null handlers
 * - withStaticTyping(): already static, not static
 * - refine(Class, TypeBindings, JavaType, JavaType[]): normal, null superInts
 * - toString(): elementType null, non-null
 *
 * Boundary conditions:
 * - null elementType in constructors and with* methods
 * - empty TypeBindings, empty superInterfaces array
 * - Object handlers as null
 *
 * Defect-specific (databind#1102):
 * - Deprecated construct(Class, JavaType) must preserve raw type and content type
 *   This test directly triggers the bug where fabricated TypeBindings causes
 *   incorrect type resolution (e.g., raw class becomes LinkedHashMap instead of custom type).
 *   We assert that getRawClass() returns the exact class passed and that
 *   getContentType() matches the provided elementType.
 */
public class CollectionTypeDeepseekTest {

    // Helper to create a SimpleType (assumed public)
    private JavaType simpleType(Class<?> cls) {
        // Use SimpleType static factory (available in Jackson 2.x)
        return SimpleType.constructUnsafe(cls);
    }

    // A custom collection type for defect testing
    static class MyCollection extends java.util.ArrayList<Object> {
        private static final long serialVersionUID = 1L;
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testConstructNormal() {
        JavaType elemType = simpleType(String.class);
        JavaType superClass = simpleType(Object.class);
        JavaType[] superInts = new JavaType[] { simpleType(java.util.Collection.class) };
        TypeBindings bindings = TypeBindings.emptyBindings();
        CollectionType ct = CollectionType.construct(MyCollection.class, bindings, superClass, superInts, elemType);
        assertNotNull(ct);
        assertEquals(MyCollection.class, ct.getRawClass());
        assertSame(elemType, ct.getContentType());
        assertSame(bindings, ct.getBindings());
        assertSame(superClass, ct.getSuperClass());
        assertArrayEquals(superInts, ct.getSuperInterfaces());
        assertFalse(ct.isAbstract());
    }

    @Test(timeout = 4000)
    public void testConstructWithNullBindings() {
        JavaType elemType = simpleType(Integer.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, null, null, null, elemType);
        assertNotNull(ct);
        assertEquals(MyCollection.class, ct.getRawClass());
        assertSame(elemType, ct.getContentType());
        assertNull(ct.getSuperClass());
        assertNull(ct.getSuperInterfaces());
        // Bindings may be null or empty depending on implementation; we just verify no crash
    }

    @Test(timeout = 4000)
    public void testWithContentTypeSameReturnsThis() {
        JavaType elemType = simpleType(String.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType);
        assertSame(ct, ct.withContentType(elemType));
    }

    @Test(timeout = 4000)
    public void testWithContentTypeDifferent() {
        JavaType elemType1 = simpleType(String.class);
        JavaType elemType2 = simpleType(Integer.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType1);
        CollectionType ct2 = ct.withContentType(elemType2);
        assertNotSame(ct, ct2);
        assertSame(elemType2, ct2.getContentType());
        // Ensure other fields preserved
        assertEquals(MyCollection.class, ct2.getRawClass());
    }

    @Test(timeout = 4000)
    public void testWithContentTypeNull() {
        JavaType elemType = simpleType(Double.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType);
        CollectionType ct2 = ct.withContentType(null);
        assertNotSame(ct, ct2);
        assertNull(ct2.getContentType());
    }

    @Test(timeout = 4000)
    public void testWithTypeHandler() {
        JavaType elemType = simpleType(String.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType);
        Object handler = new Object();
        CollectionType ct2 = ct.withTypeHandler(handler);
        assertNotSame(ct, ct2);
        assertSame(handler, ct2.getTypeHandler());
        // other fields unchanged
        assertSame(elemType, ct2.getContentType());
    }

    @Test(timeout = 4000)
    public void testWithContentTypeHandler() {
        JavaType elemType = simpleType(String.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType);
        Object handler = new Object();
        CollectionType ct2 = ct.withContentTypeHandler(handler);
        assertNotSame(ct, ct2);
        // Content type should have handler set
        assertSame(handler, ct2.getContentType().getTypeHandler());
        // original content type unchanged
        assertNull(elemType.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testWithValueHandler() {
        JavaType elemType = simpleType(String.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType);
        Object handler = new Object();
        CollectionType ct2 = ct.withValueHandler(handler);
        assertNotSame(ct, ct2);
        assertSame(handler, ct2.getValueHandler());
    }

    @Test(timeout = 4000)
    public void testWithContentValueHandler() {
        JavaType elemType = simpleType(String.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType);
        Object handler = new Object();
        CollectionType ct2 = ct.withContentValueHandler(handler);
        assertNotSame(ct, ct2);
        assertSame(handler, ct2.getContentType().getValueHandler());
        assertNull(elemType.getValueHandler());
    }

    @Test(timeout = 4000)
    public void testWithStaticTypingAlreadyStatic() {
        JavaType elemType = simpleType(String.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType);
        // First make static
        CollectionType staticCt = ct.withStaticTyping();
        assertNotSame(ct, staticCt);
        assertTrue(staticCt.isStatic());
        // Calling again should return same
        assertSame(staticCt, staticCt.withStaticTyping());
    }

    @Test(timeout = 4000)
    public void testRefine() {
        JavaType elemType = simpleType(String.class);
        JavaType superClass = simpleType(java.util.AbstractCollection.class);
        JavaType[] superInts = new JavaType[] { simpleType(java.util.List.class) };
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType);
        CollectionType refined = (CollectionType) ct.refine(java.util.ArrayList.class, TypeBindings.emptyBindings(), superClass, superInts);
        assertNotNull(refined);
        assertEquals(java.util.ArrayList.class, refined.getRawClass());
        assertSame(elemType, refined.getContentType());
        assertSame(superClass, refined.getSuperClass());
        assertArrayEquals(superInts, refined.getSuperInterfaces());
    }

    @Test(timeout = 4000)
    public void testToStringWithNonNullElementType() {
        JavaType elemType = simpleType(String.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType);
        String str = ct.toString();
        assertTrue(str.contains(MyCollection.class.getName()));
        assertTrue(str.contains("String"));
    }

    @Test(timeout = 4000)
    public void testToStringWithNullElementType() {
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, null);
        String str = ct.toString();
        assertTrue(str.contains(MyCollection.class.getName()));
        // should handle null element type gracefully
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testConstructWithEmptySuperInterfaces() {
        JavaType elemType = simpleType(String.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, new JavaType[0], elemType);
        assertNotNull(ct);
        assertNotNull(ct.getSuperInterfaces());
        assertEquals(0, ct.getSuperInterfaces().length);
    }

    @Test(timeout = 4000)
    public void testConstructWithNullElementType() {
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, null);
        assertNotNull(ct);
        assertNull(ct.getContentType());
    }

    @Test(timeout = 4000)
    public void testDeprecatedConstructWithNullBindingsDeprecatedMethod() {
        // This tests the deprecated single-arg constructor via the public static method
        JavaType elemType = simpleType(String.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, elemType);
        assertNotNull(ct);
        assertNull(ct.getBindings()); // deprecated version sets bindings to null
        // Raw class should be MyCollection.class (but defect may cause wrong class)
        assertEquals(MyCollection.class, ct.getRawClass());
        assertSame(elemType, ct.getContentType());
    }

    @Test(timeout = 4000)
    public void testDeprecatedNarrow() {
        JavaType elemType = simpleType(String.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType);
        // _narrow is protected but accessible in same package
        JavaType narrowed = ct._narrow(java.util.ArrayList.class);
        assertTrue(narrowed instanceof CollectionType);
        assertEquals(java.util.ArrayList.class, narrowed.getRawClass());
        // Content type should be preserved
        assertSame(elemType, narrowed.getContentType());
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========
    // This test directly targets the databind#1102 defect.
    // The deprecated construct(Class, JavaType) must produce a CollectionType
    // whose raw class is the provided class, not a bogus one (e.g., LinkedHashMap).
    @Test(timeout = 4000)
    public void testDeprecatedConstructPreservesRawClass() {
        // Use a custom class to clearly see if bug exists (expected vs actual raw class)
        JavaType elemType = simpleType(Point.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, elemType);
        // Assert the raw class is exactly MyCollection.class
        assertEquals("Raw class should be the one passed, not a bogus type",
                MyCollection.class, ct.getRawClass());
        // Additionally, content type must be the provided elemType
        assertSame("Content type must be preserved", elemType, ct.getContentType());
    }

    // Inner class used only for this test
    static class Point {
        public int x, y;
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========
    // The class does not throw explicit exceptions for invalid args, but we test resilience.

    @Test(timeout = 4000)
    public void testWithTypeHandlerNull() {
        JavaType elemType = simpleType(String.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType);
        CollectionType ct2 = ct.withTypeHandler(null);
        assertNotSame(ct, ct2);
        assertNull(ct2.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testWithContentTypeHandlerNull() {
        JavaType elemType = simpleType(String.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType);
        CollectionType ct2 = ct.withContentTypeHandler(null);
        assertNotNull(ct2);
        // content type handler should be null
        assertNull(ct2.getContentType().getTypeHandler());
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========
    // equals and hashCode are inherited from Object, so we test identity.

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        JavaType elemType = simpleType(String.class);
        CollectionType ct1 = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType);
        CollectionType ct2 = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType);
        // Not equal because different instances
        assertNotEquals(ct1, ct2);
        // But same instance equals itself
        assertEquals(ct1, ct1);
        // hashCode consistency
        assertEquals(ct1.hashCode(), ct1.hashCode());
    }

    // Test protected constructor (TypeBase base, JavaType elemT)
    @Test(timeout = 4000)
    public void testProtectedConstructor() {
        // Create a TypeBase instance (SimpleType is a TypeBase)
        SimpleType base = (SimpleType) simpleType(MyCollection.class);
        JavaType elemType = simpleType(String.class);
        CollectionType ct = new CollectionType(base, elemType);
        assertNotNull(ct);
        assertEquals(MyCollection.class, ct.getRawClass());
        assertSame(elemType, ct.getContentType());
    }

    // Additional boundary: Construct with null superClass and null superInts
    @Test(timeout = 4000)
    public void testConstructWithNullSuperClassAndNullSuperInts() {
        JavaType elemType = simpleType(String.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType);
        assertNull(ct.getSuperClass());
        assertNull(ct.getSuperInterfaces());
    }

    // Test refine with null superInts
    @Test(timeout = 4000)
    public void testRefineWithNullSuperInts() {
        JavaType elemType = simpleType(String.class);
        CollectionType ct = CollectionType.construct(MyCollection.class, TypeBindings.emptyBindings(), null, null, elemType);
        CollectionType refined = (CollectionType) ct.refine(java.util.ArrayList.class, TypeBindings.emptyBindings(), null, null);
        assertNotNull(refined);
        assertEquals(java.util.ArrayList.class, refined.getRawClass());
        assertNull(refined.getSuperInterfaces());
    }
}