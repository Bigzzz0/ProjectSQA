package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: ResolvedRecursiveType
 * 
 * Decision Branches:
 * 1. setReference(): _referencedType != null (IllegalStateException) vs null (set)
 * 2. toString(): _referencedType == null ("UNRESOLVED") vs != null (raw class name)
 * 3. equals(): o == this (true), o == null (false), _referencedType == null (false),
 *              o.getClass() == getClass() && _referencedType.equals(other.getSelfReferencedType())
 * 4. getGenericSignature(): delegates to _referencedType (requires non-null)
 * 5. getErasedSignature(): delegates to _referencedType (requires non-null)
 * 6. withContentType(), withTypeHandler(), etc.: return this (no-op)
 * 7. isContainerType(): return false
 * 8. refine(): return null
 * 9. _narrow(): return this
 * 
 * Boundary Conditions:
 * - Null _referencedType (unresolved state)
 * - Non-null _referencedType (resolved state)
 * - Double setReference() call
 * - equals() with null, same object, different types, different referenced types
 * - toString() with null and non-null _referencedType
 * 
 * Defect Targeting (Defects4J #1301):
 * - The defect involves serialization of recursive types where base properties are lost
 * - This is related to the toString() method's handling of self-referencing types
 * - The test verifies that when a recursive type is resolved, its string representation
 *   correctly includes the referenced type's raw class name
 */
public class ResolvedRecursiveTypeDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testSetReferenceAndGetSelfReferencedType() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertNull("Initially unresolved", type.getSelfReferencedType());
        
        JavaType ref = TypeFactory.defaultInstance().constructType(String.class);
        type.setReference(ref);
        assertSame("Referenced type should be set", ref, type.getSelfReferencedType());
    }

    @Test(timeout = 4000)
    public void testSetReferenceThrowsOnDoubleSet() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        JavaType ref = TypeFactory.defaultInstance().constructType(String.class);
        type.setReference(ref);
        
        try {
            type.setReference(ref);
            fail("Should throw IllegalStateException on double set");
        } catch (IllegalStateException e) {
            assertTrue("Exception message should contain old value", e.getMessage().contains("old value"));
        }
    }

    @Test(timeout = 4000)
    public void testWithMethodsReturnThis() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        JavaType contentType = TypeFactory.defaultInstance().constructType(Integer.class);
        
        assertSame("withContentType should return this", type, type.withContentType(contentType));
        assertSame("withTypeHandler should return this", type, type.withTypeHandler("handler"));
        assertSame("withContentTypeHandler should return this", type, type.withContentTypeHandler("handler"));
        assertSame("withValueHandler should return this", type, type.withValueHandler("handler"));
        assertSame("withContentValueHandler should return this", type, type.withContentValueHandler("handler"));
        assertSame("withStaticTyping should return this", type, type.withStaticTyping());
    }

    @Test(timeout = 4000)
    public void testIsContainerTypeReturnsFalse() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertFalse("isContainerType should return false", type.isContainerType());
    }

    @Test(timeout = 4000)
    public void testRefineReturnsNull() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertNull("refine should return null", type.refine(Object.class, TypeBindings.emptyBindings(), null, null));
    }

    @Test(timeout = 4000)
    public void testNarrowReturnsThis() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertSame("_narrow should return this", type, type._narrow(Object.class));
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testToStringUnresolved() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        String str = type.toString();
        assertTrue("Unresolved toString should contain UNRESOLVED", str.contains("UNRESOLVED"));
        assertTrue("toString should start with [recursive type", str.startsWith("[recursive type"));
    }

    @Test(timeout = 4000)
    public void testToStringResolved() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        JavaType ref = TypeFactory.defaultInstance().constructType(String.class);
        type.setReference(ref);
        
        String str = type.toString();
        assertTrue("Resolved toString should contain raw class name", str.contains("java.lang.String"));
        assertFalse("Resolved toString should not contain UNRESOLVED", str.contains("UNRESOLVED"));
    }

    @Test(timeout = 4000)
    public void testEqualsWithNull() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertFalse("equals(null) should return false", type.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsWithSameObject() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertTrue("equals(this) should return true", type.equals(type));
    }

    @Test(timeout = 4000)
    public void testEqualsWithUnresolvedType() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertFalse("Unresolved types should not be equal", type1.equals(type2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentClass() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        assertFalse("equals with different class should return false", type.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsWithResolvedTypes() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        
        JavaType ref = TypeFactory.defaultInstance().constructType(String.class);
        type1.setReference(ref);
        type2.setReference(ref);
        
        assertTrue("Resolved types with same reference should be equal", type1.equals(type2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentReferencedTypes() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        
        type1.setReference(TypeFactory.defaultInstance().constructType(String.class));
        type2.setReference(TypeFactory.defaultInstance().constructType(Integer.class));
        
        assertFalse("Types with different references should not be equal", type1.equals(type2));
    }

    // ===== Partition C: Defect-Targeted Branch Zone =====
    // Targets Defects4J defect #1301: Recursive type serialization losing base properties

    @Test(timeout = 4000)
    public void testDefect1301_RecursiveTypeToStringIncludesReferencedType() {
        // This test targets the defect where recursive types lose base properties during serialization
        // The toString() method should correctly represent the resolved type
        
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        
        // Create a type that simulates a self-referencing structure
        JavaType baseType = TypeFactory.defaultInstance().constructType(Object.class);
        recursiveType.setReference(baseType);
        
        String toString = recursiveType.toString();
        // The toString should include the raw class name of the referenced type
        assertTrue("toString should contain referenced type's raw class name", 
                   toString.contains("java.lang.Object"));
        
        // Verify the referenced type is properly accessible
        JavaType selfRef = recursiveType.getSelfReferencedType();
        assertNotNull("Self-referenced type should not be null", selfRef);
        assertEquals("Self-referenced type should match", baseType, selfRef);
    }

    @Test(timeout = 4000)
    public void testDefect1301_RecursiveTypeSignatureDelegation() {
        // Test that signature methods delegate correctly to referenced type
        ResolvedRecursiveType recursiveType = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        JavaType refType = TypeFactory.defaultInstance().constructType(String.class);
        recursiveType.setReference(refType);
        
        StringBuilder sb1 = new StringBuilder();
        StringBuilder sb2 = new StringBuilder();
        
        String genericSig = recursiveType.getGenericSignature(sb1).toString();
        String erasedSig = recursiveType.getErasedSignature(sb2).toString();
        
        // Verify signatures are delegated
        assertNotNull("Generic signature should not be null", genericSig);
        assertNotNull("Erased signature should not be null", erasedSig);
        assertTrue("Generic signature should contain 'String'", genericSig.contains("String"));
    }

    @Test(timeout = 4000)
    public void testDefect1301_RecursiveTypeEqualsWithResolvedChain() {
        // Test equals behavior with resolved recursive types that might form chains
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        
        // Create a chain: type1 -> type2 -> String
        JavaType stringType = TypeFactory.defaultInstance().constructType(String.class);
        type2.setReference(stringType);
        type1.setReference(type2);
        
        // type1 should not equal type2 since they reference different things
        assertFalse("Types with different reference chains should not be equal", type1.equals(type2));
        
        // Verify the chain is properly maintained
        assertSame("type1 should reference type2", type2, type1.getSelfReferencedType());
        assertSame("type2 should reference String", stringType, type2.getSelfReferencedType());
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testSetReferenceThrowsOnDoubleSetWithDifferentRef() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        type.setReference(TypeFactory.defaultInstance().constructType(String.class));
        type.setReference(TypeFactory.defaultInstance().constructType(Integer.class));
    }

    @Test(timeout = 4000)
    public void testGetGenericSignatureWithNullRef() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        try {
            type.getGenericSignature(new StringBuilder());
            fail("Should throw NullPointerException when _referencedType is null");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testGetErasedSignatureWithNullRef() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        try {
            type.getErasedSignature(new StringBuilder());
            fail("Should throw NullPointerException when _referencedType is null");
        } catch (NullPointerException e) {
            // Expected
        }
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testEqualsConsistency() {
        ResolvedRecursiveType type1 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type2 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        ResolvedRecursiveType type3 = new ResolvedRecursiveType(Object.class, TypeBindings.emptyBindings());
        
        JavaType ref = TypeFactory.defaultInstance().constructType(Double.class);
        type1.setReference(ref);
        type2.setReference(ref);
        type3.setReference(ref);
        
        // Test symmetry
        assertEquals("Equals should be symmetric", type1.equals(type2), type2.equals(type1));
        
        // Test transitivity
        if (type1.equals(type2) && type2.equals(type3)) {
            assertTrue("Equals should be transitive", type1.equals(type3));
        }
        
        // Test consistency
        assertTrue("Equals should be consistent", type1.equals(type2));
        assertTrue("Equals should be consistent on second call", type1.equals(type2));
    }

    @Test(timeout = 4000)
    public void testConstructorAndInitialState() {
        ResolvedRecursiveType type = new ResolvedRecursiveType(Integer.class, TypeBindings.emptyBindings());
        assertNull("_referencedType should be null initially", type.getSelfReferencedType());
        assertEquals("erasedType should be Integer", Integer.class, type.getRawClass());
    }
}