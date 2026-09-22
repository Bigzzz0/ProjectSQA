package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * 
 * Target: SimpleType.java (Defects4J)
 * 
 * Decision branches covered:
 * 1. construct(): three guard conditions for Map/Collection/array → throws IllegalArgumentException (targets stay)
 * 2. constructUnsafe(): null _bindings → potential NPE in toString(), equals(), buildCanonicalName(), getGenericSignature()
 * 3. equals(): identity (==), null, class mismatch, _class mismatch, bindings mismatch, bindings equality (null special)
 * 4. withTypeHandler/withValueHandler: handler identity vs new instance
 * 5. withStaticTyping: _asStatic flag identity vs new instance
 * 6. _narrow(): subclass identity vs new instance (note: no subclass check enforced, bug?)
 * 7. isContainerType(): always false
 * 8. toString()/buildCanonicalName(): simple class name case, bindings empty/non-empty
 * 9. getErasedSignature/getGenericSignature: basic class representation
 * 10. Exception methods: withContentType, withContentTypeHandler, withContentValueHandler → IllegalArgumentException
 * 
 * Defect-specific targeting:
 * - The failing tests show that SimpleType is incorrectly used for Map/Collection subtypes.
 *   Bug: construct() originally did NOT throw for Map/Collection/array; fixed version does.
 *   Additional bug: constructUnsafe() leaves _bindings == null, causing NPE in methods accessing bindings.
 *   Test suite verifies both fix and exposes the null-bindings issue.
 */

public class SimpleTypeDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A: Core functional logic & state transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructUnsafeNormalClass() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        assertNotNull(st);
        assertFalse(st.isContainerType());
        assertTrue(st.toString().contains("java.lang.String"));
    }

    @Test(timeout = 4000)
    public void testConstructDeprecatedNormalClass() {
        SimpleType st = SimpleType.construct(Integer.class);
        assertNotNull(st);
        assertFalse(st.isContainerType());
        assertTrue(st.toString().contains("java.lang.Integer"));
    }

    @Test(timeout = 4000)
    public void testWithTypeHandlerSameHandler() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        SimpleType st2 = st.withTypeHandler(null);
        assertSame(st, st2); // same handler → returns this
    }

    @Test(timeout = 4000)
    public void testWithTypeHandlerDifferentHandler() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        Object handler = new Object();
        SimpleType st2 = st.withTypeHandler(handler);
        assertNotSame(st, st2);
        assertNotNull(st2);
    }

    @Test(timeout = 4000)
    public void testWithValueHandlerSameHandler() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        SimpleType st2 = st.withValueHandler(null);
        assertSame(st, st2);
    }

    @Test(timeout = 4000)
    public void testWithValueHandlerDifferentHandler() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        Object handler = new Object();
        SimpleType st2 = st.withValueHandler(handler);
        assertNotSame(st, st2);
        assertNotNull(st2);
    }

    @Test(timeout = 4000)
    public void testWithStaticTypingAlreadyStatic() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        SimpleType staticSt = st.withStaticTyping();
        SimpleType staticSt2 = staticSt.withStaticTyping();
        assertSame(staticSt, staticSt2); // already static → returns this
    }

    @Test(timeout = 4000)
    public void testWithStaticTypingNew() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        SimpleType staticSt = st.withStaticTyping();
        assertNotSame(st, staticSt);
        assertNotNull(staticSt);
    }

    @Test(timeout = 4000)
    public void testIsContainerType() {
        SimpleType st = SimpleType.constructUnsafe(Double.class);
        assertFalse(st.isContainerType());
    }

    @Test(timeout = 4000)
    public void testGetErasedSignature() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        StringBuilder sb = new StringBuilder();
        sb = st.getErasedSignature(sb);
        assertTrue(sb.toString().contains("java.lang.String"));
    }

    @Test(timeout = 4000)
    public void testGetGenericSignature() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        StringBuilder sb = new StringBuilder();
        sb = st.getGenericSignature(sb);
        assertTrue(sb.toString().contains("java.lang.String"));
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructUnsafePrimitiveWrapper() {
        SimpleType st = SimpleType.constructUnsafe(Integer.class);
        assertTrue(st.toString().contains("java.lang.Integer"));
        assertFalse(st.isContainerType());
    }

    @Test(timeout = 4000)
    public void testConstructUnsafeVoidClass() {
        SimpleType st = SimpleType.constructUnsafe(Void.class);
        assertNotNull(st);
        assertEquals(Void.class, st.getClass()); // just checking, not exact
        assertTrue(st.toString().contains("java.lang.Void"));
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructForMapThrows() {
        SimpleType.construct(java.util.LinkedHashMap.class);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructForCollectionThrows() {
        SimpleType.construct(java.util.ArrayList.class);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructForArrayThrows() {
        SimpleType.construct(int[].class);
    }

    // The following tests expose the null-bindings bug in constructUnsafe.
    // On the fixed version they pass, on the buggy version they fail with NPE.

    @Test(timeout = 4000)
    public void testConstructUnsafeToStringNoNPE() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        // Should not throw NullPointerException (bug if it does)
        String str = st.toString();
        assertNotNull(str);
        assertTrue(str.contains("java.lang.String"));
    }

    @Test(timeout = 4000)
    public void testConstructUnsafeEqualsNoNPE() {
        SimpleType st1 = SimpleType.constructUnsafe(String.class);
        SimpleType st2 = SimpleType.constructUnsafe(String.class);
        // Should not throw NPE; both have same class and (null) bindings → equals?
        // On fixed with emptyBindings, they are equal; on buggy, NPE.
        assertEquals(st1, st2);
    }

    @Test(timeout = 4000)
    public void testConstructUnsafeMapSubclassNoThrow() {
        // constructUnsafe does not guard, so should not throw
        SimpleType st = SimpleType.constructUnsafe(java.util.LinkedHashMap.class);
        assertNotNull(st);
        // In the buggy version, calling toString may NPE; but the creation itself is allowed.
        // We simply assert it's a SimpleType.
        assertTrue(st instanceof SimpleType);
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithContentTypeThrows() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        st.withContentType(null); // always throws
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithContentTypeHandlerThrows() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        st.withContentTypeHandler(null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testWithContentValueHandlerThrows() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        st.withContentValueHandler(null);
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEqualsSameInstance() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        assertEquals(st, st);
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        assertFalse(st.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        SimpleType st = SimpleType.constructUnsafe(String.class);
        // compare to a non-SimpleType object
        assertFalse(st.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClassType() {
        SimpleType st1 = SimpleType.constructUnsafe(String.class);
        SimpleType st2 = SimpleType.constructUnsafe(Integer.class);
        assertFalse(st1.equals(st2));
    }

    @Test(timeout = 4000)
    public void testEqualsSameClassSameBindings() {
        SimpleType st1 = SimpleType.constructUnsafe(String.class);
        SimpleType st2 = SimpleType.constructUnsafe(String.class);
        assertEquals(st1, st2);
    }

    @Test(timeout = 4000)
    public void testEqualsSameClassDifferentBindings() throws Exception {
        // Need two SimpleType with different bindings. Use construct() with a class that has no bindings
        // Actually construct() creates with emptyBindings, constructUnsafe creates with null.
        // They are different. So we can compare constructUnsafe and construct.
        SimpleType stNull = SimpleType.constructUnsafe(String.class);
        SimpleType stEmpty = SimpleType.construct(String.class);
        assertNotEquals(stNull, stEmpty); // null bindings != empty bindings
    }

    @Test(timeout = 4000)
    public void testNarrowSameClass() {
        SimpleType st = SimpleType.constructUnsafe(Number.class);
        SimpleType narrowed = st._narrow(Number.class);
        assertSame(st, narrowed);
    }

    @Test(timeout = 4000)
    public void testNarrowDifferentSubclass() {
        SimpleType st = SimpleType.constructUnsafe(Number.class);
        SimpleType narrowed = st._narrow(Integer.class);
        assertNotSame(st, narrowed);
        assertNotNull(narrowed);
        // The narrowed type should have _class = Integer.class
        assertEquals(Integer.class, narrowed.getRawClass()); // getRawClass() is inherited
    }
}