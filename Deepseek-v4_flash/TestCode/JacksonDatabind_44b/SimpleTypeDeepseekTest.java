package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

/**
 * [Branch & Defect Analysis Matrix]
 * Targeted branches:
 * - constructUnsafe: null TypeBindings path (leads to NPE in equals/buildCanonicalName)
 * - construct: validation of Map, Collection, array classes
 * - withTypeHandler/withValueHandler: identity shortcut vs new instance
 * - withStaticTyping: _asStatic shortcut
 * - withContentType/withContentTypeHandler/withContentValueHandler: always throws
 * - isContainerType: always false
 * - getErasedSignature: delegates to _classSignature
 * - getGenericSignature: handles zero vs non-zero bindings
 * - refine: always returns null
 * - toString: builds canonical name
 * - equals: class check, TypeBindings comparison (null-safety bug)
 * Defect targeted: UnrecognizedPropertyException due to incorrect type identity.
 *   The bug: equals() ignores _typeHandler, _valueHandler, and crashes on null _bindings.
 *   Test verifies that types with different handlers are NOT equal, and that
 *   constructing via constructUnsafe does not break equals or toString.
 */
public class SimpleTypeDeepseekTest {

    // =============================
    // Partition A: Core functional & state transitions
    // =============================

    @Test(timeout = 4000)
    public void testConstructUnsafeBasic() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        assertNotNull(st);
        assertFalse(st.isContainerType());
        assertNull(st.containedType(0)); // no content
        assertEquals(String.class, st.getRawClass());
    }

    @Test(timeout = 4000)
    public void testConstructValid() {
        SimpleType st = SimpleType.construct(String.class);
        assertNotNull(st);
        assertFalse(st.isContainerType());
    }

    @Test(timeout = 4000)
    public void testWithTypeHandlerSameReturnsThis() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        SimpleType same = st.withTypeHandler(st._typeHandler); // can't access private, so use null
        // Actually we call withTypeHandler(null) since _typeHandler is private; withTypeHandler(null) should return same because _typeHandler == null
        SimpleType result = st.withTypeHandler(null);
        assertSame(st, result);
    }

    @Test(timeout = 4000)
    public void testWithTypeHandlerDifferentReturnsNew() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        SimpleType changed = st.withTypeHandler("customHandler");
        assertNotNull(changed);
        assertNotSame(st, changed);
    }

    @Test(timeout = 4000)
    public void testWithValueHandlerSameReturnsThis() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        SimpleType same = st.withValueHandler(null);
        assertSame(st, same);
    }

    @Test(timeout = 4000)
    public void testWithValueHandlerDifferentReturnsNew() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        SimpleType changed = st.withValueHandler("customValue");
        assertNotNull(changed);
        assertNotSame(st, changed);
    }

    @Test(timeout = 4000)
    public void testWithStaticTypingAlreadyStatic() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        // st._asStatic is false, so withStaticTyping returns new
        SimpleType staticOne = st.withStaticTyping();
        assertNotNull(staticOne);
        assertNotSame(st, staticOne);
        // Second call on static instance returns itself
        SimpleType staticAgain = staticOne.withStaticTyping();
        assertSame(staticOne, staticAgain);
    }

    @Test(timeout = 4000)
    public void testRefineReturnsNull() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        assertNull(st.refine(String.class, TypeBindings.emptyBindings(), null, null));
    }

    // =============================
    // Partition B: Boundary values & extremes
    // =============================

    @Test(timeout = 4000)
    public void testGetErasedSignature() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        StringBuilder sb = new StringBuilder();
        StringBuilder result = st.getErasedSignature(sb);
        assertSame(sb, result);
        assertTrue(result.toString().endsWith(";"));
    }

    @Test(timeout = 4000)
    public void testGetGenericSignature() {
        // use construct() to get non-null bindings (empty but not null)
        SimpleType st = SimpleType.construct(String.class);
        StringBuilder sb = new StringBuilder();
        StringBuilder result = st.getGenericSignature(sb);
        assertSame(sb, result);
        assertTrue(result.toString().endsWith(";"));
    }

    @Test(timeout = 4000)
    public void testToString() {
        SimpleType st = SimpleType.construct(String.class);
        String str = st.toString();
        assertTrue(str.startsWith("[simple type, class "));
        assertTrue(str.endsWith("]"));
    }

    @Test(timeout = 4000)
    public void testContainedTypeWithNoBindings() {
        SimpleType st = SimpleType.construct(String.class);
        assertNull(st.containedType(0));
        assertNull(st.containedType(1));
        assertEquals(0, st.containedTypeCount());
    }

    // =============================
    // Partition C: Defect-targeted (UnrecognizedPropertyException bug)
    // =============================

    @Test(timeout = 4000)
    public void testEqualsWithSameClassAndBindings() {
        SimpleType t1 = SimpleType.constructUnsafe(String.class);
        SimpleType t2 = SimpleType.constructUnsafe(String.class);
        // Ensure no NullPointerException; on buggy version, null _bindings cause NPE.
        try {
            assertTrue(t1.equals(t2));
        } catch (NullPointerException e) {
            fail("equals() should handle null TypeBindings without NPE");
        }
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentHandlersShouldNotBeEqual() {
        // Bug: equals() ignores _typeHandler and _valueHandler, so two types with different handlers
        // are considered equal, which can cause type identity issues leading to UnrecognizedPropertyException.
        SimpleType t1 = SimpleType.constructUnsafe(String.class);
        SimpleType t2 = t1.withTypeHandler("handler1");
        SimpleType t1b = t1.withTypeHandler("handler2");
        // t2 and t1b have same class and bindings (both null) but different handlers.
        // On buggy version, equals returns true; on fixed version should return false.
        assertNotEquals("Types with different type handlers must not be equal", t2, t1b);
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentValueHandlersNotEqual() {
        SimpleType t1 = SimpleType.constructUnsafe(String.class);
        SimpleType t2 = t1.withValueHandler("v1");
        SimpleType t1b = t1.withValueHandler("v2");
        assertNotEquals("Types with different value handlers must not be equal", t2, t1b);
    }

    @Test(timeout = 4000)
    public void testEqualsStaticVsNonStaticNotEqual() {
        SimpleType t1 = SimpleType.constructUnsafe(String.class);
        SimpleType t2 = t1.withStaticTyping();
        // t2 is static, t1 is not
        assertNotEquals(t1, t2);
    }

    // =============================
    // Partition D: Exception & defensive paths
    // =============================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructWithMapClass() {
        SimpleType.construct(HashMap.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructWithCollectionClass() {
        SimpleType.construct(ArrayList.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructWithArrayClass() {
        SimpleType.construct(int[].class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithContentTypeThrows() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        st.withContentType(TypeFactory.unknownType());
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithContentTypeHandlerThrows() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        st.withContentTypeHandler("handler");
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithContentValueHandlerThrows() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        st.withContentValueHandler("handler");
    }

    @Test(timeout = 4000)
    public void testBuildCanonicalNameWithNullBindings() {
        // constructUnsafe gives null _bindings; buildCanonicalName should not NPE.
        SimpleType st = SimpleType.constructUnsafe(String.class);
        // We access via toString which calls buildCanonicalName
        try {
            String s = st.toString();
            assertNotNull(s);
        } catch (NullPointerException e) {
            fail("buildCanonicalName should handle null TypeBindings");
        }
    }

    @Test(timeout = 4000)
    public void testConstructWithNullClass() {
        try {
            SimpleType.construct(null);
            fail("Should throw IllegalArgumentException for null class");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    // =============================
    // Partition E: Object contract (equals, hashCode)
    // =============================

    @Test(timeout = 4000)
    public void testEqualsReflexive() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        assertTrue(st.equals(st));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        assertFalse(st.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        Object other = new Object();
        assertFalse(st.equals(other));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClassType() {
        // SimpleType and some other type (e.g., from TypeFactory) should not be equal
        SimpleType st = SimpleType.constructUnsafe(String.class);
        // Construct a ReferenceType or ArrayType? But we only have SimpleType.
        // Use a SimpleType with same class but different bindings to test inequality.
        // We can't easily get different bindings via public API, but we can create with construct()
        // which gives empty bindings vs constructUnsafe which gives null bindings.
        SimpleType withBindings = SimpleType.construct(String.class);
        // These have same class but bindings are different (empty vs null)
        // On fixed version, equals may return false; on buggy, NPE.
        try {
            boolean eq = st.equals(withBindings);
            // If no NPE, we can assert something. For now, just ensure no crash.
        } catch (NullPointerException e) {
            // This is expected on buggy version, but we want to catch and fail in this test?
            // Actually we want to reveal the bug, so we should expect no exception.
            fail("equals() should handle different binding states");
        }
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistent() {
        SimpleType st1 = SimpleType.construct(String.class);
        SimpleType st2 = SimpleType.construct(String.class);
        assertEquals(st1.hashCode(), st2.hashCode());
    }
}