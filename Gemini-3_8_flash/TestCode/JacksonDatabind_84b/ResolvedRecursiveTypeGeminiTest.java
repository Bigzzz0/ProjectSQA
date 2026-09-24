package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/*
 * [Branch & Defect Analysis Matrix]
 * =========================================================================================
 * Target Class: com.fasterxml.jackson.databind.type.ResolvedRecursiveType
 *
 * Decision / Branch Matrix:
 * 1. setReference(JavaType ref):
 *    - Branch A1 (_referencedType != null): throws IllegalStateException (re-set guard)
 *    - Branch A2 (_referencedType == null): assigns _referencedType successfully
 *
 * 2. getGenericSignature(StringBuilder) & getErasedSignature(StringBuilder):
 *    - Branch B1 (_referencedType == null): throws NullPointerException (defensive uninitialized state)
 *    - Branch B2 (_referencedType != null): delegates to _referencedType and returns StringBuilder
 *
 * 3. toString():
 *    - Branch C1 (_referencedType == null): appends "UNRESOLVED"
 *    - Branch C2 (_referencedType != null): appends _referencedType.getRawClass().getName()
 *
 * 4. equals(Object o):
 *    - Branch D1 (o == this): returns true (identity)
 *    - Branch D2 (o == null): returns false
 *    - Branch D3 (_referencedType == null): returns false (never match unresolved reference)
 *    - Branch D4 (o.getClass() != getClass()): returns false
 *    - Branch D5 (o.getClass() == getClass() && _referencedType.equals(o.ref)): returns true
 *    - Branch D6 (o.getClass() == getClass() && !_referencedType.equals(o.ref)): returns false
 *    - Branch D7 (o.getClass() == getClass() && o.ref == null): returns false
 *
 * 5. Fluent Mutation & Handler Methods (withContentType, withTypeHandler, withValueHandler, etc.):
 *    - Verify all return `this` directly (identity preserving, no-op mutations)
 *    - refine(...) returns null
 *    - isContainerType() returns false
 *    - _narrow(...) returns `this`
 *
 * 6. Defect-Targeted Branch Zone (Jackson Databind Recursive Type Superclass Resolution):
 *    - Defects4J Defect: TestTypeFactoryWithRecursiveTypes::testBasePropertiesIncludedWhenSerializingSubWhenSubTypeLoadedAfterBaseType
 *    - Defect mechanism: ResolvedRecursiveType failed to override getSuperClass() (or delegate it
 *      to _referencedType), returning null instead. This breaks base property serialization when
 *      subtypes are loaded after recursive base types.
 * =========================================================================================
 */
public class ResolvedRecursiveTypeGeminiTest {

    // --- Defect Reproduction Data Fixtures ---
    static abstract class RecursiveBase<T extends RecursiveBase<T>> {
        public int base = 1;
    }

    static class RecursiveSub extends RecursiveBase<RecursiveSub> {
        public int sub = 2;
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialUnresolvedState() {
        TypeBindings bindings = TypeBindings.emptyBindings();
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, bindings);

        assertEquals(String.class, type.getRawClass());
        assertSame(bindings, type.getBindings());
        assertNull(type.getSelfReferencedType());
        assertFalse(type.isContainerType());
        assertEquals("[recursive type; UNRESOLVED", type.toString());
    }

    @Test(timeout = 4000)
    public void testSetReferenceSuccessAndGetters() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType refType = tf.constructType(String.class);

        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        type.setReference(refType);

        assertSame(refType, type.getSelfReferencedType());
        assertEquals("[recursive type; " + String.class.getName(), type.toString());
    }

    @Test(timeout = 4000)
    public void testSignatureDelegation() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType refType = tf.constructType(Integer.class);

        ResolvedRecursiveType type = new ResolvedRecursiveType(Integer.class, TypeBindings.emptyBindings());
        type.setReference(refType);

        StringBuilder sbGeneric = new StringBuilder("prefix_");
        StringBuilder retGeneric = type.getGenericSignature(sbGeneric);
        assertSame(sbGeneric, retGeneric);
        assertEquals("prefix_" + refType.getGenericSignature(), retGeneric.toString());

        StringBuilder sbErased = new StringBuilder("prefix_");
        StringBuilder retErased = type.getErasedSignature(sbErased);
        assertSame(sbErased, retErased);
        assertEquals("prefix_" + refType.getErasedSignature(), retErased.toString());
    }

    @Test(timeout = 4000)
    public void testNoOpMutatorsReturnSameInstance() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Number.class, TypeBindings.emptyBindings());
        JavaType dummyContentType = SimpleType.constructUnsafe(String.class);

        assertSame(type, type.withContentType(dummyContentType));
        assertSame(type, type.withTypeHandler("typeHandler"));
        assertSame(type, type.withContentTypeHandler("contentTypeHandler"));
        assertSame(type, type.withValueHandler("valueHandler"));
        assertSame(type, type.withContentValueHandler("contentValueHandler"));
        assertSame(type, type.withStaticTyping());
        @SuppressWarnings("deprecation")
        JavaType narrowed = type._narrow(Integer.class);
        assertSame(type, narrowed);
    }

    @Test(timeout = 4000)
    public void testRefineReturnsNull() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        JavaType refined = type.refine(Object.class, TypeBindings.emptyBindings(), null, new JavaType[0]);
        assertNull(refined);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFluentMutatorsWithNullArguments() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());

        assertSame(type, type.withContentType(null));
        assertSame(type, type.withTypeHandler(null));
        assertSame(type, type.withContentTypeHandler(null));
        assertSame(type, type.withValueHandler(null));
        assertSame(type, type.withContentValueHandler(null));
    }

    @Test(timeout = 4000)
    public void testRefineWithAllNullArguments() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertNull(type.refine(null, null, null, null));
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Ground Truth Defect)
    // =========================================================================

    /**
     * Direct unit-level defect check:
     * ResolvedRecursiveType must delegate getSuperClass() to its referenced type
     * when resolved, rather than returning null.
     */
    @Test(timeout = 4000)
    public void testGetSuperClassDelegationToReferencedType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType subJavaType = tf.constructType(RecursiveSub.class);

        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(RecursiveSub.class, TypeBindings.emptyBindings());
        recursiveType.setReference(subJavaType);

        JavaType superClass = recursiveType.getSuperClass();
        assertNotNull("ResolvedRecursiveType must delegate getSuperClass() to referenced type rather than returning null", superClass);
        assertEquals(subJavaType.getSuperClass(), superClass);
    }

    /**
     * Defects4J Integration-level Defect Test:
     * com.fasterxml.jackson.databind.type.TestTypeFactoryWithRecursiveTypes
     * ::testBasePropertiesIncludedWhenSerializingSubWhenSubTypeLoadedAfterBaseType
     *
     * In defective versions, serializing RecursiveSub results in '{"sub":2}' because
     * the recursive base type resolution loses superclass properties. Expected is '{"base":1,"sub":2}'.
     */
    @Test(timeout = 4000)
    public void testBasePropertiesIncludedWhenSerializingSubWhenSubTypeLoadedAfterBaseType() throws Exception {
        TypeFactory tf = TypeFactory.defaultInstance();
        tf.constructType(RecursiveBase.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setTypeFactory(tf);

        String json = mapper.writeValueAsString(new RecursiveSub());
        assertEquals("{\"base\":1,\"sub\":2}", json);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testSetReferenceThrowsWhenCalledMultipleTimes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType ref1 = tf.constructType(String.class);
        JavaType ref2 = tf.constructType(Integer.class);

        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        type.setReference(ref1);

        try {
            type.setReference(ref2);
            fail("Expected IllegalStateException when re-setting self reference");
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().contains("Trying to re-set self reference"));
            assertTrue(e.getMessage().contains("old value = " + ref1));
            assertTrue(e.getMessage().contains("new = " + ref2));
        }
    }

    @Test(timeout = 4000)
    public void testSignaturesThrowNpeWhenUnresolved() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());

        try {
            type.getGenericSignature(new StringBuilder());
            fail("Expected NullPointerException when generic signature called on unresolved type");
        } catch (NullPointerException expected) {
            // Success
        }

        try {
            type.getErasedSignature(new StringBuilder());
            fail("Expected NullPointerException when erased signature called on unresolved type");
        } catch (NullPointerException expected) {
            // Success
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (equals, hashCode)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsContractIdentityAndNull() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());

        // Identity (Branch D1)
        assertTrue(type.equals(type));

        // Null comparison (Branch D2)
        assertFalse(type.equals(null));

        // Different class comparison (Branch D4)
        assertFalse(type.equals("not-a-type"));
        assertFalse(type.equals(SimpleType.constructUnsafe(String.class)));
    }

    @Test(timeout = 4000)
    public void testEqualsWithUnresolvedReferences() {
        ResolvedRecursiveType unres1 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType unres2 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());

        // Unresolved types should never match another instance (Branch D3)
        assertFalse(unres1.equals(unres2));

        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType ref = tf.constructType(String.class);
        ResolvedRecursiveType resolved = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        resolved.setReference(ref);

        assertFalse(unres1.equals(resolved));
        // Resolved compared to unresolved where other.getSelfReferencedType() == null
        assertFalse(resolved.equals(unres1));
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeWithResolvedReferences() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType refString1 = tf.constructType(String.class);
        JavaType refString2 = tf.constructType(String.class);
        JavaType refInt = tf.constructType(Integer.class);

        ResolvedRecursiveType type1 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        type1.setReference(refString1);

        ResolvedRecursiveType type2 = new ResolvedRecursiveType(String.class, TypeBindings.emptyBindings());
        type2.setReference(refString2);

        ResolvedRecursiveType type3 = new ResolvedRecursiveType(Integer.class, TypeBindings.emptyBindings());
        type3.setReference(refInt);

        // Reflexive & Symmetric equality (Branch D5)
        assertTrue(type1.equals(type2));
        assertTrue(type2.equals(type1));
        assertEquals(type1.hashCode(), type2.hashCode());

        // Inequality with different referenced type (Branch D6)
        assertFalse(type1.equals(type3));
        assertFalse(type3.equals(type1));
    }
}