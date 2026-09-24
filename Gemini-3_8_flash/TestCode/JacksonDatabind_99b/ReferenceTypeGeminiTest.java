package com.fasterxml.jackson.databind.type;

import com.fasterxml.jackson.databind.JavaType;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Class under test: com.fasterxml.jackson.databind.type.ReferenceType
 *
 * 1. DEFECT UNDER TEST:
 *    - Defects4J JacksonDatabind bug where ReferenceType#buildCanonicalName() fails to append
 *      the closing angle bracket '>' to the canonical representation.
 *    - Target: buildCanonicalName() -> toCanonical() MUST end with '>' enclosing referenced type.
 *
 * 2. BRANCH & CONDITION COVERAGE:
 *    - Constructors & Anchor logic:
 *      * anchorType == null -> defaults to `this`
 *      * anchorType != null -> retains specified anchorType
 *      * isAnchorType(): returns true when _anchorType == this; false otherwise
 *    - upgradeFrom(JavaType baseType, JavaType refdType):
 *      * refdType == null -> IllegalArgumentException("Missing referencedType")
 *      * baseType instanceof TypeBase -> success
 *      * !(baseType instanceof TypeBase) -> IllegalArgumentException("Can not upgrade from...")
 *    - withContentType(JavaType):
 *      * contentType == _referencedType -> returns this
 *      * contentType != _referencedType -> returns new ReferenceType with updated content type
 *    - withTypeHandler(Object):
 *      * h == _typeHandler -> returns this
 *      * h != _typeHandler -> returns new ReferenceType with updated type handler
 *    - withContentTypeHandler(Object):
 *      * h == _referencedType.getTypeHandler() -> returns this
 *      * h != _referencedType.getTypeHandler() -> updates refType with type handler
 *    - withValueHandler(Object):
 *      * h == _valueHandler -> returns this
 *      * h != _valueHandler -> returns new ReferenceType with updated value handler
 *    - withContentValueHandler(Object):
 *      * h == _referencedType.getValueHandler() -> returns this
 *      * h != _referencedType.getValueHandler() -> updates refType with value handler
 *    - withStaticTyping():
 *      * _asStatic is true -> returns this
 *      * _asStatic is false -> returns new ReferenceType with static typing enabled
 *    - refine(Class, TypeBindings, JavaType, JavaType[]): returns new ReferenceType instance
 *    - _narrow(Class): returns narrowed ReferenceType instance
 *    - Type inquiry:
 *      * getContentType(), getReferencedType(), hasContentType(), isReferenceType()
 *    - Signatures:
 *      * getErasedSignature(StringBuilder), getGenericSignature(StringBuilder)
 *    - Object Contract:
 *      * equals(): identity, null, different class, different raw class, different refType, same refType
 *      * toString(): string verification containing reference type markers
 */
public class ReferenceTypeGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Defects4J ComparisonFailure:
     * expected:<...rence<java.lang.Long[>]> but was:<...rence<java.lang.Long[]>
     * Verifies that buildCanonicalName() correctly appends the closing bracket '>'.
     */
    @Test(timeout = 4000)
    public void testCanonicalNameEnclosesReferencedTypeWithClosingBracket() {
        JavaType refdType = SimpleType.constructUnsafe(Long.class);
        JavaType baseType = SimpleType.constructUnsafe(AtomicReference.class);
        ReferenceType rt = ReferenceType.upgradeFrom(baseType, refdType);

        String expected = "java.util.concurrent.atomic.AtomicReference<java.lang.Long>";
        assertEquals(expected, rt.toCanonical());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testUpgradeFromAndBasicProperties() {
        JavaType baseType = SimpleType.constructUnsafe(AtomicReference.class);
        JavaType refdType = SimpleType.constructUnsafe(String.class);

        ReferenceType rt = ReferenceType.upgradeFrom(baseType, refdType);

        assertSame(refdType, rt.getContentType());
        assertSame(refdType, rt.getReferencedType());
        assertTrue(rt.hasContentType());
        assertTrue(rt.isReferenceType());
        assertTrue(rt.isAnchorType());
        assertSame(rt, rt.getAnchorType());
    }

    @Test(timeout = 4000)
    public void testConstructorsAndAnchorTypeRetained() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        JavaType customAnchor = SimpleType.constructUnsafe(Object.class);

        ReferenceType rtWithAnchor = new ReferenceType(
                AtomicReference.class,
                TypeBindings.emptyBindings(),
                null, null,
                refdType,
                customAnchor,
                null, null, false
        );

        assertFalse(rtWithAnchor.isAnchorType());
        assertSame(customAnchor, rtWithAnchor.getAnchorType());

        ReferenceType rtDefaultAnchor = new ReferenceType(
                AtomicReference.class,
                TypeBindings.emptyBindings(),
                null, null,
                refdType,
                null,
                null, null, false
        );
        assertTrue(rtDefaultAnchor.isAnchorType());
        assertSame(rtDefaultAnchor, rtDefaultAnchor.getAnchorType());
    }

    @Test(timeout = 4000)
    public void testConstructFactoryMethods() {
        JavaType refdType = SimpleType.constructUnsafe(Integer.class);
        ReferenceType rt1 = ReferenceType.construct(
                AtomicReference.class,
                TypeBindings.emptyBindings(),
                null,
                null,
                refdType
        );

        assertNotNull(rt1);
        assertEquals(AtomicReference.class, rt1.getRawClass());
        assertSame(refdType, rt1.getContentType());
        assertTrue(rt1.isAnchorType());

        @SuppressWarnings("deprecation")
        ReferenceType rt2 = ReferenceType.construct(AtomicReference.class, refdType);
        assertNotNull(rt2);
        assertEquals(AtomicReference.class, rt2.getRawClass());
        assertSame(refdType, rt2.getContentType());
    }

    @Test(timeout = 4000)
    public void testWithContentTypeBranches() {
        JavaType refdType1 = SimpleType.constructUnsafe(String.class);
        JavaType refdType2 = SimpleType.constructUnsafe(Integer.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), refdType1);

        // Branch 1: Same instance -> return this
        assertSame(rt, rt.withContentType(refdType1));

        // Branch 2: Different instance -> return new ReferenceType
        JavaType updated = rt.withContentType(refdType2);
        assertNotSame(rt, updated);
        assertSame(refdType2, updated.getContentType());
    }

    @Test(timeout = 4000)
    public void testWithTypeHandlerBranches() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), refdType);

        // Initially null handler
        assertSame(rt, rt.withTypeHandler(null));

        Object handler = "customTypeHandler";
        ReferenceType withHandler = rt.withTypeHandler(handler);
        assertNotSame(rt, withHandler);
        assertEquals(handler, withHandler.getTypeHandler());

        // Same handler -> return this
        assertSame(withHandler, withHandler.withTypeHandler(handler));
    }

    @Test(timeout = 4000)
    public void testWithValueHandlerBranches() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), refdType);

        // Initially null handler
        assertSame(rt, rt.withValueHandler(null));

        Object handler = "customValueHandler";
        ReferenceType withHandler = rt.withValueHandler(handler);
        assertNotSame(rt, withHandler);
        assertEquals(handler, withHandler.getValueHandler());

        // Same handler -> return this
        assertSame(withHandler, withHandler.withValueHandler(handler));
    }

    @Test(timeout = 4000)
    public void testWithContentTypeHandlerBranches() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), refdType);

        // Initially refdType has no typeHandler
        assertSame(rt, rt.withContentTypeHandler(null));

        Object contentHandler = "contentTHandler";
        ReferenceType withContentHandler = rt.withContentTypeHandler(contentHandler);
        assertNotSame(rt, withContentHandler);
        assertEquals(contentHandler, withContentHandler.getContentType().getTypeHandler());

        // No-op when re-setting the same content type handler
        assertSame(withContentHandler, withContentHandler.withContentTypeHandler(contentHandler));
    }

    @Test(timeout = 4000)
    public void testWithContentValueHandlerBranches() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), refdType);

        // Initially refdType has no valueHandler
        assertSame(rt, rt.withContentValueHandler(null));

        Object contentValHandler = "contentVHandler";
        ReferenceType withContentValHandler = rt.withContentValueHandler(contentValHandler);
        assertNotSame(rt, withContentValHandler);
        assertEquals(contentValHandler, withContentValHandler.getContentType().getValueHandler());

        // No-op when re-setting the same content value handler
        assertSame(withContentValHandler, withContentValHandler.withContentValueHandler(contentValHandler));
    }

    @Test(timeout = 4000)
    public void testWithStaticTypingBranches() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), refdType);

        assertFalse(rt.useStaticType());

        ReferenceType staticRt = rt.withStaticTyping();
        assertNotSame(rt, staticRt);
        assertTrue(staticRt.useStaticType());

        // When already static -> return this
        assertSame(staticRt, staticRt.withStaticTyping());
    }

    @Test(timeout = 4000)
    public void testRefineAndNarrow() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), refdType);

        JavaType refined = rt.refine(AtomicReference.class, TypeBindings.emptyBindings(), null, null);
        assertNotNull(refined);
        assertTrue(refined instanceof ReferenceType);
        assertSame(refdType, refined.getContentType());

        @SuppressWarnings("deprecation")
        JavaType narrowed = rt._narrow(AtomicReference.class);
        assertNotNull(narrowed);
        assertTrue(narrowed instanceof ReferenceType);
        assertSame(refdType, narrowed.getContentType());
    }

    @Test(timeout = 4000)
    public void testSignatures() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), refdType);

        StringBuilder erasedSb = new StringBuilder();
        rt.getErasedSignature(erasedSb);
        assertEquals("Ljava/util/concurrent/atomic/AtomicReference;", erasedSb.toString());

        StringBuilder genericSb = new StringBuilder();
        rt.getGenericSignature(genericSb);
        assertEquals("Ljava/util/concurrent/atomic/AtomicReference<Ljava/lang/String;>;", genericSb.toString());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Partition D: Defensive Guards
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testUpgradeFromNullReferencedTypeThrowsException() {
        JavaType baseType = SimpleType.constructUnsafe(AtomicReference.class);
        ReferenceType.upgradeFrom(baseType, null);
    }

    @Test(timeout = 4000)
    public void testUpgradeFromNonTypeBaseThrowsException() {
        // Construct an anonymous JavaType extending JavaType directly (not TypeBase)
        JavaType nonTypeBase = new JavaType(Object.class, 0, null, null, false) {
            private static final long serialVersionUID = 1L;
            @Override public JavaType withContentType(JavaType contentType) { return this; }
            @Override public JavaType withTypeHandler(Object h) { return this; }
            @Override public JavaType withContentTypeHandler(Object h) { return this; }
            @Override public JavaType withValueHandler(Object h) { return this; }
            @Override public JavaType withContentValueHandler(Object h) { return this; }
            @Override public JavaType withStaticTyping() { return this; }
            @Override public JavaType refine(Class<?> rawType, TypeBindings bindings, JavaType superClass, JavaType[] superInterfaces) { return this; }
            @Override public int containedTypeCount() { return 0; }
            @Override public JavaType containedType(int index) { return null; }
            @Override public String containedTypeName(int index) { return null; }
            @Override public StringBuilder getGenericSignature(StringBuilder sb) { return sb; }
            @Override public StringBuilder getErasedSignature(StringBuilder sb) { return sb; }
            @Override public JavaType getContentType() { return null; }
            @Override public boolean isContainerType() { return false; }
            @Override public String toString() { return "custom"; }
            @Override public boolean equals(Object o) { return o == this; }
        };

        try {
            ReferenceType.upgradeFrom(nonTypeBase, SimpleType.constructUnsafe(String.class));
            fail("Expected IllegalArgumentException when baseType is not an instance of TypeBase");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Can not upgrade from an instance of"));
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (equals, toString, etc.)
    // =========================================================================

    @Test(timeout = 4000)
    public void testToStringContainsReferenceMarker() {
        JavaType refdType = SimpleType.constructUnsafe(String.class);
        ReferenceType rt = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), refdType);

        String str = rt.toString();
        assertTrue("toString should contain '[reference type'", str.contains("[reference type"));
        assertTrue("toString should contain class name", str.contains("AtomicReference"));
        assertTrue("toString should contain referenced type", str.contains("String"));
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        JavaType refdType1 = SimpleType.constructUnsafe(String.class);
        JavaType refdType2 = SimpleType.constructUnsafe(String.class);
        JavaType refdType3 = SimpleType.constructUnsafe(Integer.class);

        ReferenceType rt1 = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), refdType1);
        ReferenceType rt2 = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), refdType2);
        ReferenceType rt3 = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(AtomicReference.class), refdType3);
        ReferenceType rtDiffClass = ReferenceType.upgradeFrom(SimpleType.constructUnsafe(Object.class), refdType1);

        // Reflexivity
        assertEquals(rt1, rt1);

        // Null check
        assertFalse(rt1.equals(null));

        // Different type of object
        assertFalse(rt1.equals("A String"));

        // Symmetry & equality for identical structural definition
        assertEquals(rt1, rt2);
        assertEquals(rt2, rt1);
        assertEquals(rt1.hashCode(), rt2.hashCode());

        // Inequity due to different referenced type
        assertNotEquals(rt1, rt3);

        // Inequity due to different raw class
        assertNotEquals(rt1, rtDiffClass);
    }
}