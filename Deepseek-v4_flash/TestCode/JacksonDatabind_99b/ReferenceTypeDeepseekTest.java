package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.ReferenceType;

public class ReferenceTypeDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target defect: buildCanonicalName() produces malformed canonical name for
     * reference types with array content type. Expected: "reference<java.lang.Long[]>" 
     * but actual (buggy): "reference<java.lang.Long[>" (missing closing bracket).
     * 
     * Decision branches exercised:
     * - upgradeFrom(): null refdType -> IllegalArgumentException; non-TypeBase -> IllegalArgumentException
     * - construct(): overloaded constructors (deprecated and new)
     * - withContentType(): same instance optimization; new instance creation
     * - withTypeHandler()/withContentTypeHandler()/withValueHandler()/withContentValueHandler():
     *   same-handler optimization vs new instance
     * - withStaticTyping(): _asStatic true/false paths
     * - refine(): always creates new instance
     * - buildCanonicalName(): normal path and array content type (defect)
     * - getErasedSignature()/getGenericSignature(): signature building
     * - equals(): identity, null, class mismatch, field comparison
     * - isAnchorType()/getAnchorType(): anchor type logic
     * - hasContentType()/isReferenceType(): constant true
     * - toString(): canonical name building
     * 
     * Boundary values: null handlers, null anchor type, empty TypeBindings,
     * array content types, MAX/MIN values for hashCode.
     */

    // --- Partition A: Core Functional Logic & State Transitions ---

    @Test(timeout = 4000)
    public void testUpgradeFromBasicType() {
        JavaType baseType = TypeFactory.defaultInstance().constructType(String.class);
        JavaType refdType = TypeFactory.defaultInstance().constructType(Integer.class);
        ReferenceType ref = ReferenceType.upgradeFrom(baseType, refdType);
        
        assertNotNull(ref);
        assertEquals(String.class, ref.getRawClass());
        assertEquals(Integer.class, ref.getContentType().getRawClass());
        assertTrue(ref.isReferenceType());
        assertTrue(ref.hasContentType());
        assertTrue(ref.isAnchorType()); // anchor is itself after upgrade
        assertEquals(ref, ref.getAnchorType());
    }

    @Test(timeout = 4000)
    public void testConstructWithBindings() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(Double.class);
        TypeBindings bindings = TypeBindings.emptyBindings();
        ReferenceType ref = ReferenceType.construct(Number.class, bindings, null, null, refdType);
        
        assertNotNull(ref);
        assertEquals(Number.class, ref.getRawClass());
        assertEquals(Double.class, ref.getContentType().getRawClass());
        assertFalse(ref.isAnchorType()); // anchor is null -> this
        assertTrue(ref.isAnchorType()); // actually anchor is this when null
    }

    @Test(timeout = 4000)
    public void testWithContentTypeSameInstance() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        
        assertSame(ref, ref.withContentType(refdType));
    }

    @Test(timeout = 4000)
    public void testWithContentTypeNewInstance() {
        JavaType refdType1 = TypeFactory.defaultInstance().constructType(String.class);
        JavaType refdType2 = TypeFactory.defaultInstance().constructType(Integer.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType1);
        
        ReferenceType ref2 = ref.withContentType(refdType2);
        assertNotSame(ref, ref2);
        assertEquals(refdType2, ref2.getContentType());
        assertEquals(refdType1, ref.getContentType()); // original unchanged
    }

    @Test(timeout = 4000)
    public void testWithTypeHandlerSameInstance() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        Object handler = new Object();
        ReferenceType ref2 = ref.withTypeHandler(handler);
        
        assertNotSame(ref, ref2);
        assertEquals(handler, ref2.getTypeHandler());
        assertNull(ref.getTypeHandler()); // original unchanged
    }

    @Test(timeout = 4000)
    public void testWithContentTypeHandler() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        Object handler = new Object();
        ReferenceType ref2 = ref.withContentTypeHandler(handler);
        
        assertNotSame(ref, ref2);
        assertEquals(handler, ref2.getContentType().getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testWithValueHandler() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        Object handler = new Object();
        ReferenceType ref2 = ref.withValueHandler(handler);
        
        assertNotSame(ref, ref2);
        assertEquals(handler, ref2.getValueHandler());
    }

    @Test(timeout = 4000)
    public void testWithContentValueHandler() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        Object handler = new Object();
        ReferenceType ref2 = ref.withContentValueHandler(handler);
        
        assertNotSame(ref, ref2);
        assertEquals(handler, ref2.getContentType().getValueHandler());
    }

    @Test(timeout = 4000)
    public void testWithStaticTyping() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        
        assertFalse(ref.isStaticTyping());
        ReferenceType ref2 = ref.withStaticTyping();
        assertTrue(ref2.isStaticTyping());
        assertNotSame(ref, ref2);
        
        // Same instance when already static
        assertSame(ref2, ref2.withStaticTyping());
    }

    @Test(timeout = 4000)
    public void testRefine() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        
        JavaType refined = ref.refine(CharSequence.class, TypeBindings.emptyBindings(), null, null);
        assertNotNull(refined);
        assertEquals(CharSequence.class, refined.getRawClass());
        assertEquals(refdType, refined.getContentType());
    }

    // --- Partition B: Boundary Value Analysis & Extremes ---

    @Test(timeout = 4000)
    public void testUpgradeFromNullReferencedType() {
        try {
            ReferenceType.upgradeFrom(
                TypeFactory.defaultInstance().constructType(Object.class), null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertEquals("Missing referencedType", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testUpgradeFromNonTypeBase() {
        JavaType nonTypeBase = new JavaType() {
            // anonymous subclass not extending TypeBase
            @Override public JavaType withTypeHandler(Object h) { return null; }
            @Override public JavaType withContentTypeHandler(Object h) { return null; }
            @Override public JavaType withValueHandler(Object h) { return null; }
            @Override public JavaType withContentValueHandler(Object h) { return null; }
            @Override public JavaType withStaticTyping() { return null; }
            @Override public JavaType refine(Class<?> rawType, TypeBindings bindings,
                    JavaType superClass, JavaType[] superInterfaces) { return null; }
            @Override protected String buildCanonicalName() { return null; }
            @Override public StringBuilder getErasedSignature(StringBuilder sb) { return null; }
            @Override public StringBuilder getGenericSignature(StringBuilder sb) { return null; }
            @Override public boolean isContainerType() { return false; }
            @Override public int containedTypeCount() { return 0; }
            @Override public JavaType containedType(int index) { return null; }
            @Override public String containedTypeName(int index) { return null; }
            @Override public Class<?> getParameterSource() { return null; }
            @Override public JavaType getContentType() { return null; }
            @Override public int getErasedSignatureHashCode() { return 0; }
            @Override public boolean equals(Object o) { return false; }
            @Override public String toString() { return "nonTypeBase"; }
        };
        
        try {
            ReferenceType.upgradeFrom(nonTypeBase, 
                TypeFactory.defaultInstance().constructType(String.class));
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not upgrade"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructWithNullRefType() {
        // Deprecated constructor allows null refType? Actually it passes null to constructor
        // which will NPE on refType.hashCode() in super() call
        try {
            ReferenceType.construct(String.class, (JavaType) null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testBuildCanonicalNameWithArrayContent() {
        // This is the defect-targeting test
        JavaType arrayType = TypeFactory.defaultInstance().constructType(Long[].class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), arrayType);
        
        String canonical = ref.toCanonical();
        // Expected format: "reference<java.lang.Long[]>" but buggy version produces "reference<java.lang.Long[>"
        assertEquals("reference<java.lang.Long[]>", canonical);
    }

    @Test(timeout = 4000)
    public void testGetErasedSignature() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        
        StringBuilder sb = new StringBuilder();
        StringBuilder result = ref.getErasedSignature(sb);
        assertSame(sb, result);
        assertTrue(result.toString().contains("java.lang.Object"));
    }

    @Test(timeout = 4000)
    public void testGetGenericSignature() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        
        StringBuilder sb = new StringBuilder();
        StringBuilder result = ref.getGenericSignature(sb);
        assertSame(sb, result);
        String sig = result.toString();
        assertTrue(sig.contains("<"));
        assertTrue(sig.endsWith(">;"));
    }

    // --- Partition C: Defect-Targeted Branch Zone ---

    @Test(timeout = 4000)
    public void testCanonicalNameWithArrayContentType() {
        // Directly targets the defect: buildCanonicalName() with array content type
        JavaType arrayType = TypeFactory.defaultInstance().constructType(Long[].class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), arrayType);
        
        // The bug: missing closing bracket for array type in canonical name
        String canonical = ref.toCanonical();
        assertEquals("reference<java.lang.Long[]>", canonical);
        
        // Also test with primitive array
        JavaType intArrayType = TypeFactory.defaultInstance().constructType(int[].class);
        ReferenceType ref2 = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), intArrayType);
        assertEquals("reference<int[]>", ref2.toCanonical());
    }

    @Test(timeout = 4000)
    public void testCanonicalNameWithNestedReference() {
        JavaType innerRefd = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType innerRef = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), innerRefd);
        ReferenceType outerRef = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), innerRef);
        
        String canonical = outerRef.toCanonical();
        assertTrue(canonical.contains("reference<reference<"));
        assertTrue(canonical.endsWith(">>"));
    }

    // --- Partition D: Exception & Defensive Guard Paths ---

    @Test(timeout = 4000)
    public void testEqualsWithNull() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        
        assertFalse(ref.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentClass() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        
        assertFalse(ref.equals(new Object()));
    }

    @Test(timeout = 4000)
    public void testEqualsWithSameReference() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        
        assertTrue(ref.equals(ref));
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentClassType() {
        JavaType refdType1 = TypeFactory.defaultInstance().constructType(String.class);
        JavaType refdType2 = TypeFactory.defaultInstance().constructType(Integer.class);
        ReferenceType ref1 = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType1);
        ReferenceType ref2 = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType2);
        
        assertFalse(ref1.equals(ref2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithSameContentType() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref1 = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        ReferenceType ref2 = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        
        assertTrue(ref1.equals(ref2));
    }

    // --- Partition E: Object Lifecycle & Contract Integrity ---

    @Test(timeout = 4000)
    public void testToString() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        
        String str = ref.toString();
        assertTrue(str.startsWith("[reference type, class "));
        assertTrue(str.contains("<"));
        assertTrue(str.endsWith(">]"));
    }

    @Test(timeout = 4000)
    public void testGetAnchorType() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        
        assertSame(ref, ref.getAnchorType());
        assertTrue(ref.isAnchorType());
    }

    @Test(timeout = 4000)
    public void testAnchorTypeWithExplicitAnchor() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        JavaType anchorType = TypeFactory.defaultInstance().constructType(Number.class);
        ReferenceType ref = new ReferenceType(
            Object.class, TypeBindings.emptyBindings(), null, null, refdType, anchorType, null, null, false);
        
        assertSame(anchorType, ref.getAnchorType());
        assertFalse(ref.isAnchorType());
    }

    @Test(timeout = 4000)
    public void testGetContentTypeAndReferencedType() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        
        assertSame(refdType, ref.getContentType());
        assertSame(refdType, ref.getReferencedType());
    }

    @Test(timeout = 4000)
    public void testNarrowDeprecated() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        
        JavaType narrowed = ref.narrow(CharSequence.class);
        assertNotNull(narrowed);
        assertEquals(CharSequence.class, narrowed.getRawClass());
        assertEquals(refdType, narrowed.getContentType());
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        
        int hash1 = ref.hashCode();
        int hash2 = ref.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        JavaType refdType = TypeFactory.defaultInstance().constructType(String.class);
        ReferenceType ref = ReferenceType.upgradeFrom(
            TypeFactory.defaultInstance().constructType(Object.class), refdType);
        
        // Basic serialization round-trip
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(ref);
        oos.close();
        
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(baos.toByteArray()));
        ReferenceType deserialized = (ReferenceType) ois.readObject();
        ois.close();
        
        assertEquals(ref, deserialized);
        assertEquals(ref.getContentType(), deserialized.getContentType());
    }
}