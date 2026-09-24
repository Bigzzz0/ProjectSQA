package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: ResolvedRecursiveType
 *
 * Partition A: Core Functional Logic & State Transitions
 * - constructor(Class<?>, TypeBindings): initializes state, super constructor invocation.
 * - setReference(JavaType) & getSelfReferencedType(): successful single invocation, reference retrieval.
 * - getGenericSignature(StringBuilder) & getErasedSignature(StringBuilder): delegating to _referencedType.
 * - Immutable builder/mutator methods: withContentType, withTypeHandler, withContentTypeHandler,
 *   withValueHandler, withContentValueHandler, withStaticTyping, _narrow. Verify identity return (this).
 * - refine(...): returns null as designed.
 * - isContainerType(): returns false.
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - toString():
 *   - Branch 1: _referencedType == null -> "[recursive type; UNRESOLVED"
 *   - Branch 2: _referencedType != null -> "[recursive type; " + rawClass.getName()
 * - getGenericSignature / getErasedSignature with pre-allocated and empty StringBuilders.
 *
 * Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
 * - Known Defect: RecursiveTypeTest::testSuperClassWithReferencedJavaType
 *   When ResolvedRecursiveType is resolved via setReference(ref), calls to getSuperClass() must reflect
 *   the referenced JavaType's superclass rather than returning null from unpopulated TypeBase._superClass.
 *   This is essential for polymorphic property resolution of recursive types (e.g., base and sub properties).
 *
 * Partition D: Exception & Defensive Guard Paths
 * - setReference(JavaType) called twice: triggers IllegalStateException("Trying to re-set self reference...").
 * - NullPointerException handling when calling signature methods on unresolved recursive types.
 *
 * Partition E: Object Lifecycle & Contract Integrity
 * - equals(Object):
 *   - o == this -> true
 *   - o == null -> false
 *   - _referencedType == null -> false (even if compared to self or another unresolved type)
 *   - o.getClass() != getClass() -> false
 *   - other._referencedType equals / does not equal this._referencedType
 */
public class ResolvedRecursiveTypeGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialStateAndSetReference() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(ArrayList.class, bindings);

        assertNull("Initial referenced type must be null", type.getSelfReferencedType());
        assertFalse("ResolvedRecursiveType should never report being a container type directly", type.isContainerType());

        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType refType = tf.constructType(ArrayList.class);
        type.setReference(refType);

        assertSame("getSelfReferencedType should return the set reference", refType, type.getSelfReferencedType());
    }

    @Test(timeout = 4000)
    public void testSignaturesDelegation() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType refType = tf.constructType(String.class);

        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        type.setReference(refType);

        StringBuilder sbErased = new StringBuilder();
        StringBuilder resErased = type.getErasedSignature(sbErased);
        assertSame(sbErased, resErased);
        assertEquals("Ljava/lang/String;", resErased.toString());

        StringBuilder sbGeneric = new StringBuilder();
        StringBuilder resGeneric = type.getGenericSignature(sbGeneric);
        assertSame(sbGeneric, resGeneric);
        assertEquals("Ljava/lang/String;", resGeneric.toString());
    }

    @Test(timeout = 4000)
    public void testSelfReturningMutators() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType dummyType = tf.constructType(String.class);

        assertSame(type, type.withContentType(dummyType));
        assertSame(type, type.withTypeHandler("dummyHandler"));
        assertSame(type, type.withContentTypeHandler("dummyHandler"));
        assertSame(type, type.withValueHandler("dummyValueHandler"));
        assertSame(type, type.withContentValueHandler("dummyValueHandler"));
        assertSame(type, type.withStaticTyping());
        assertSame(type, type._narrow(String.class));
    }

    @Test(timeout = 4000)
    public void testRefineReturnsNull() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertNull(type.refine(String.class, TypeBindings.emptyBindings(), null, new JavaType[0]));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testToStringUnresolved() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(HashMap.class, TypeBindings.emptyBindings());
        assertEquals("[recursive type; UNRESOLVED", type.toString());
    }

    @Test(timeout = 4000)
    public void testToStringResolved() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType refType = tf.constructType(HashMap.class);

        ResolvedRecursiveType type = new ResolvedRecursiveType(HashMap.class, TypeBindings.emptyBindings());
        type.setReference(refType);

        assertEquals("[recursive type; java.util.HashMap", type.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J bug:
     * com.fasterxml.jackson.databind.type.RecursiveTypeTest::testSuperClassWithReferencedJavaType
     *
     * In defective versions, ResolvedRecursiveType inherits getSuperClass() from TypeBase which returns
     * null because TypeBase._superClass was initialized to null. Once resolved, it MUST return
     * the super class of the referenced JavaType to ensure base properties/types are discoverable.
     */
    @Test(timeout = 4000)
    public void testSuperClassWithReferencedJavaType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        ResolvedRecursiveType type = new ResolvedRecursiveType(ArrayList.class, TypeBindings.emptyBindings());
        JavaType refType = tf.constructType(ArrayList.class);
        type.setReference(refType);

        JavaType expectedSuper = refType.getSuperClass();
        assertNotNull("Referenced JavaType (ArrayList) must have a super class (AbstractList)", expectedSuper);

        JavaType actualSuper = type.getSuperClass();
        assertNotNull("ResolvedRecursiveType must delegate getSuperClass() to the referenced type when set", actualSuper);
        assertEquals("getSuperClass() must match the referenced JavaType's super class", expectedSuper, actualSuper);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetReferenceMultipleTimesThrowsException() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType refType1 = tf.constructType(Number.class);
        JavaType refType2 = tf.constructType(Integer.class);

        ResolvedRecursiveType type = new ResolvedRecursiveType(Number.class, TypeBindings.emptyBindings());
        type.setReference(refType1);

        try {
            type.setReference(refType2);
            fail("Expected IllegalStateException when calling setReference more than once");
        } catch (IllegalStateException e) {
            assertTrue("Exception message should mention re-set self reference",
                    e.getMessage().contains("Trying to re-set self reference"));
            assertTrue("Exception message should show old and new values",
                    e.getMessage().contains("old value =") && e.getMessage().contains("new ="));
        }
    }

    @Test(timeout = 4000)
    public void testSignaturesOnUnresolvedThrowNpe() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        try {
            type.getGenericSignature(new StringBuilder());
            fail("Expected NullPointerException when generic signature is requested on unresolved type");
        } catch (NullPointerException expected) {
            // expected behavior since _referencedType is null
        }

        try {
            type.getErasedSignature(new StringBuilder());
            fail("Expected NullPointerException when erased signature is requested on unresolved type");
        } catch (NullPointerException expected) {
            // expected behavior since _referencedType is null
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsContract() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType stringType = tf.constructType(String.class);
        JavaType intType = tf.constructType(Integer.class);

        ResolvedRecursiveType unres1 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType unres2 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());

        // Unresolved references must never match equals, even to themselves or null
        assertFalse("Unresolved recursive type must not equal null", unres1.equals(null));
        assertFalse("Unresolved recursive type must not equal itself per specification", unres1.equals(unres1));
        assertFalse("Two unresolved recursive types must not equal each other", unres1.equals(unres2));

        ResolvedRecursiveType res1A = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        res1A.setReference(stringType);

        ResolvedRecursiveType res1B = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        res1B.setReference(stringType);

        ResolvedRecursiveType res2 = new ResolvedRecursiveType(Integer.class, TypeBindings.emptyBindings());
        res2.setReference(intType);

        // Reflexive
        assertTrue("Resolved type must equal itself", res1A.equals(res1A));

        // Symmetric
        assertTrue("Resolved types with identical referenced type must be equal", res1A.equals(res1B));
        assertTrue("Symmetric equality must hold", res1B.equals(res1A));

        // Inequality
        assertFalse("Different referenced types must not be equal", res1A.equals(res2));
        assertFalse("Resolved type must not equal null", res1A.equals(null));
        assertFalse("Resolved type must not equal unresolved type", res1A.equals(unres1));
        assertFalse("Resolved type must not equal different class type", res1A.equals("NotAType"));
    }
}