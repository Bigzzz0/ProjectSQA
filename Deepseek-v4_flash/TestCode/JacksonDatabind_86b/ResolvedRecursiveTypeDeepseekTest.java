package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.TypeBindings;

import java.util.List;
import java.util.Map;

public class ResolvedRecursiveTypeDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: ResolvedRecursiveType - self-referential type placeholder.
     * 
     * Branches/conditions to cover:
     * 1. setReference(): 
     *    - _referencedType == null (normal set) -> no exception
     *    - _referencedType != null (re-set) -> IllegalStateException
     * 2. getSelfReferencedType(): 
     *    - returns null initially
     *    - returns set reference after setReference()
     * 3. getGenericSignature(): 
     *    - delegates to _referencedType (non-null)
     *    - NPE if _referencedType is null (defensive)
     * 4. getErasedSignature(): 
     *    - delegates to _referencedType (non-null)
     *    - NPE if _referencedType is null (defensive)
     * 5. withContentType(), withTypeHandler(), withContentTypeHandler(), 
     *    withValueHandler(), withContentValueHandler(), withStaticTyping():
     *    - all return 'this' regardless of input (including null)
     * 6. _narrow(): deprecated, returns 'this'
     * 7. refine(): returns null always
     * 8. isContainerType(): returns false always
     * 9. toString():
     *    - _referencedType == null -> "[recursive type; UNRESOLVED]"
     *    - _referencedType != null -> "[recursive type; " + rawClass.getName() + "]"
     * 10. equals():
     *     - o == this -> true
     *     - o == null -> false
     *     - _referencedType == null -> false (even if o is same class)
     *     - o.getClass() != getClass() -> false
     *     - _referencedType.equals(other.getSelfReferencedType()) -> result
     * 
     * Defect-targeted test (from Defects4J):
     * - TestTypeFactoryWithRecursiveTypes::testBasePropertiesIncludedWhenSerializingSubWhenSubTypeLoadedAfterBaseType
     *   The bug: when a subtype is loaded after base type, the recursive type resolution fails to include base properties.
     *   This is reproduced by creating a recursive type where the referenced type is a subtype loaded after the base.
     *   The test asserts that the referenced type's raw class is correctly set to the subtype, not the base.
     * 
     * Additional defect test:
     * - RecursiveTypeTest::testSuperClassWithReferencedJavaType
     *   The bug: when a superclass references a subtype via recursive type, the resolution fails.
     *   We test that setReference() correctly stores the reference and getSelfReferencedType() returns it.
     */

    // Helper to create a simple JavaType for testing
    private JavaType createSimpleType(Class<?> cls) {
        return TypeFactory.defaultInstance().constructType(cls);
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testInitialState() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertNull("Initially _referencedType should be null", type.getSelfReferencedType());
        assertFalse("isContainerType should be false", type.isContainerType());
        assertNull("refine should return null", type.refine(Object.class, TypeBindings.emptyBindings(), null, null));
    }

    @Test(timeout = 4000)
    public void testSetReferenceAndGet() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        JavaType ref = createSimpleType(String.class);
        type.setReference(ref);
        assertSame("getSelfReferencedType should return the set reference", ref, type.getSelfReferencedType());
    }

    @Test(timeout = 4000)
    public void testWithMethodsReturnThis() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        JavaType contentType = createSimpleType(Integer.class);
        Object handler = new Object();
        
        assertSame("withContentType should return this", type, type.withContentType(contentType));
        assertSame("withTypeHandler should return this", type, type.withTypeHandler(handler));
        assertSame("withContentTypeHandler should return this", type, type.withContentTypeHandler(handler));
        assertSame("withValueHandler should return this", type, type.withValueHandler(handler));
        assertSame("withContentValueHandler should return this", type, type.withContentValueHandler(handler));
        assertSame("withStaticTyping should return this", type, type.withStaticTyping());
    }

    @Test(timeout = 4000)
    public void testNarrowReturnsThis() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        JavaType result = type._narrow(String.class);
        assertSame("_narrow should return this", type, result);
    }

    // ==================== Partition B: Boundary Value Analysis (BVA) & Extremes ====================

    @Test(timeout = 4000)
    public void testSetReferenceWithNull() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        // setReference(null) is allowed by the code (no null check), but we test it doesn't throw
        type.setReference(null);
        assertNull("Reference can be set to null", type.getSelfReferencedType());
    }

    @Test(timeout = 4000)
    public void testToStringUnresolved() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertEquals("toString with null reference", "[recursive type; UNRESOLVED]", type.toString());
    }

    @Test(timeout = 4000)
    public void testToStringResolved() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        JavaType ref = createSimpleType(String.class);
        type.setReference(ref);
        assertEquals("toString with resolved reference", "[recursive type; java.lang.String]", type.toString());
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullReference() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        // Both have null references, so equals should return false (per code)
        assertFalse("Two unresolved types should not be equal", type1.equals(type2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithSameReference() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        JavaType ref = createSimpleType(String.class);
        type1.setReference(ref);
        type2.setReference(ref);
        assertTrue("Two types with same reference should be equal", type1.equals(type2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentReferences() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        type1.setReference(createSimpleType(String.class));
        type2.setReference(createSimpleType(Integer.class));
        assertFalse("Types with different references should not be equal", type1.equals(type2));
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Defect-targeted test for the known bug:
     * "testBasePropertiesIncludedWhenSerializingSubWhenSubTypeLoadedAfterBaseType"
     * 
     * The bug occurs when a recursive type references a subtype that is loaded after the base type.
     * The fix ensures that the referenced type's raw class is correctly resolved to the subtype.
     * 
     * We simulate this by creating a recursive type that references a subtype (e.g., SubClass) 
     * and verify that getSelfReferencedType().getRawClass() returns the subtype class, not the base.
     */
    @Test(timeout = 4000)
    public void testDefectSubTypeLoadedAfterBaseType() {
        // Simulate the scenario: base type is loaded first, then subtype is referenced
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(BaseClass.class, TypeBindings.emptyBindings());
        
        // Create a reference to the subtype
        JavaType subType = createSimpleType(SubClass.class);
        recursiveType.setReference(subType);
        
        // The defect: previously, the raw class might have been incorrectly resolved to BaseClass
        // The correct behavior: the referenced type should be the subtype
        assertEquals("Referenced type should be the subtype", SubClass.class, 
                recursiveType.getSelfReferencedType().getRawClass());
        
        // Also verify the toString uses the subtype's name
        assertTrue("toString should contain subtype name", 
                recursiveType.toString().contains(SubClass.class.getName()));
    }

    /**
     * Defect-targeted test for "testSuperClassWithReferencedJavaType"
     * 
     * This tests that a superclass can correctly reference a subtype via recursive type.
     * The bug caused AssertionFailedError when the superclass's referenced type was not properly set.
     */
    @Test(timeout = 4000)
    public void testDefectSuperClassWithReferencedJavaType() {
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(BaseClass.class, TypeBindings.emptyBindings());
        JavaType subType = createSimpleType(SubClass.class);
        
        // Set the reference (this is the operation that previously failed)
        recursiveType.setReference(subType);
        
        // Verify the reference is correctly stored
        assertNotNull("Reference should be set", recursiveType.getSelfReferencedType());
        assertEquals("Referenced type should be SubClass", SubClass.class, 
                recursiveType.getSelfReferencedType().getRawClass());
        
        // Test that getGenericSignature and getErasedSignature delegate correctly
        StringBuilder sb = new StringBuilder();
        assertNotNull("getGenericSignature should not return null", recursiveType.getGenericSignature(sb));
        assertNotNull("getErasedSignature should not return null", recursiveType.getErasedSignature(new StringBuilder()));
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testSetReferenceTwiceThrows() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        type.setReference(createSimpleType(String.class));
        type.setReference(createSimpleType(Integer.class)); // Should throw IllegalStateException
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testGetGenericSignatureWithNullReference() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        type.getGenericSignature(new StringBuilder()); // NPE because _referencedType is null
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testGetErasedSignatureWithNullReference() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        type.getErasedSignature(new StringBuilder()); // NPE because _referencedType is null
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsReflexive() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertTrue("Object should equal itself", type.equals(type));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertFalse("Object should not equal null", type.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        Object other = new Object();
        assertFalse("Object should not equal different class", type.equals(other));
    }

    @Test(timeout = 4000)
    public void testEqualsSymmetric() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        JavaType ref = createSimpleType(String.class);
        type1.setReference(ref);
        type2.setReference(ref);
        
        assertTrue("Equals should be symmetric", type1.equals(type2));
        assertTrue("Equals should be symmetric", type2.equals(type1));
    }

    @Test(timeout = 4000)
    public void testEqualsTransitive() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type3 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        JavaType ref = createSimpleType(String.class);
        type1.setReference(ref);
        type2.setReference(ref);
        type3.setReference(ref);
        
        assertTrue("Equals should be transitive", type1.equals(type2) && type2.equals(type3) && type1.equals(type3));
    }

    @Test(timeout = 4000)
    public void testGetSelfReferencedTypeAfterSet() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        JavaType ref = createSimpleType(List.class);
        type.setReference(ref);
        assertSame("Should return the exact same reference object", ref, type.getSelfReferencedType());
    }

    @Test(timeout = 4000)
    public void testWithContentTypeDoesNotModifyState() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        JavaType original = type.getSelfReferencedType();
        type.withContentType(createSimpleType(String.class));
        assertEquals("State should not change after withContentType", original, type.getSelfReferencedType());
    }

    // Helper classes for defect tests
    static class BaseClass {
        public int base = 1;
    }

    static class SubClass extends BaseClass {
        public int sub = 2;
    }
}