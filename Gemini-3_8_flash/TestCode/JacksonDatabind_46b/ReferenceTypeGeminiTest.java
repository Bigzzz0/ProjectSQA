package com.fasterxml.jackson.databind.type;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: com.fasterxml.jackson.databind.type.ReferenceType
 *
 * 1. DEFECT-TARGETED BRANCH:
 *    - Method: getGenericSignature(StringBuilder sb)
 *    - Defect ID: Defects4J JacksonDatabind (TestJavaType::testGenericSignature1195)
 *    - Failure Signature: expected: ...<Ljava/lang/String;[>];> but was: ...<Ljava/lang/String;[];>
 *    - Cause: getGenericSignature appends ';' directly after referenced type signature instead of '>;'.
 *    - Test: testGenericSignatureDefectTargetsMissingClosingBracket
 *
 * 2. BRANCH & EQUIVALENCE PARTITIONS:
 *    - Partition A (Construction & Core Properties):
 *      * construct() factory vs protected constructor
 *      * isReferenceType() -> true
 *      * containedTypeCount() -> 1
 *      * containedType(0) -> _referencedType, containedType(x != 0) -> null
 *      * containedTypeName(0) -> "T", containedTypeName(x != 0) -> null
 *      * getParameterSource() -> _class
 *      * getReferencedType() -> _referencedType
 *
 *    - Partition B (Immutability & Handler Transitions):
 *      * withTypeHandler: same instance (h == _typeHandler) vs new instance
 *      * withContentTypeHandler: same instance (h == _ref.getTypeHandler()) vs new instance
 *      * withValueHandler: same instance (h == _valueHandler) vs new instance
 *      * withContentValueHandler: same instance (h == _ref.getValueHandler()) vs new instance
 *      * withStaticTyping: already static (_asStatic == true) vs newly static
 *
 *    - Partition C (Signatures & Canonical Forms):
 *      * buildCanonicalName() / toCanonical()
 *      * getErasedSignature()
 *      * getGenericSignature()
 *      * toString() format
 *
 *    - Partition D (Subtyping & Narrowing):
 *      * _narrow(Class<?> subclass) creates new ReferenceType with updated class
 *
 *    - Partition E (Contract Integrity):
 *      * equals: identity (o == this)
 *      * equals: null (o == null)
 *      * equals: wrong class (o.getClass() != getClass())
 *      * equals: different raw class (other._class != _class)
 *      * equals: different referenced type (!equals)
 *      * equals: identical state -> true
 *      * hashCode consistency
 */
public class ReferenceTypeGeminiTest {

    private final TypeFactory _typeFactory = TypeFactory.defaultInstance();
    private final JavaType _stringType = _typeFactory.constructType(String.class);
    private final JavaType _integerType = _typeFactory.constructType(Integer.class);

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (JacksonDatabind Defect)
    // =========================================================================

    /**
     * Targets JacksonDatabind defect where getGenericSignature() does not append
     * the closing '>' before the terminal ';' of the enclosing type parameter list.
     * Expected: Ljava/util/concurrent/atomic/AtomicReference<Ljava/lang/String;>;
     */
    @Test(timeout = 4000)
    public void testGenericSignatureDefectTargetsMissingClosingBracket() {
        ReferenceType refType = ReferenceType.construct(AtomicReference.class, _stringType, null, null);

        StringBuilder sb = new StringBuilder();
        refType.getGenericSignature(sb);
        String genericSig = sb.toString();

        assertEquals("Ljava/util/concurrent/atomic/AtomicReference<Ljava/lang/String;>;", genericSig);
        assertEquals("Ljava/util/concurrent/atomic/AtomicReference<Ljava/lang/String;>;", refType.getGenericSignature());
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFactoryConstructionAndBaseProperties() {
        ReferenceType refType = ReferenceType.construct(AtomicReference.class, _stringType, null, null);

        assertTrue(refType.isReferenceType());
        assertSame(AtomicReference.class, refType.getRawClass());
        assertSame(_stringType, refType.getReferencedType());
        assertSame(_stringType, refType.getContentType());
        assertSame(AtomicReference.class, refType.getParameterSource());
        assertFalse(refType.useStaticTyping());
        assertNull(refType.getValueHandler());
        assertNull(refType.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testContainedTypeCountAndAccess() {
        ReferenceType refType = ReferenceType.construct(AtomicReference.class, _stringType, null, null);

        assertEquals(1, refType.containedTypeCount());
        assertSame(_stringType, refType.containedType(0));
        assertNull(refType.containedType(1));
        assertNull(refType.containedType(-1));

        assertEquals("T", refType.containedTypeName(0));
        assertNull(refType.containedTypeName(1));
        assertNull(refType.containedTypeName(-1));
    }

    @Test(timeout = 4000)
    public void testWithTypeHandlerBranches() {
        ReferenceType original = ReferenceType.construct(AtomicReference.class, _stringType, null, null);

        Object handler = "typeHandler1";
        ReferenceType modified = original.withTypeHandler(handler);

        assertNotSame(original, modified);
        assertSame(handler, modified.getTypeHandler());
        assertSame(original.getRawClass(), modified.getRawClass());
        assertSame(original.getReferencedType(), modified.getReferencedType());

        // Branch check: returns this when same handler
        ReferenceType sameHandler = modified.withTypeHandler(handler);
        assertSame(modified, sameHandler);
    }

    @Test(timeout = 4000)
    public void testWithValueHandlerBranches() {
        ReferenceType original = ReferenceType.construct(AtomicReference.class, _stringType, null, null);

        Object handler = "valHandler1";
        ReferenceType modified = original.withValueHandler(handler);

        assertNotSame(original, modified);
        assertSame(handler, modified.getValueHandler());
        assertSame(original.getRawClass(), modified.getRawClass());
        assertSame(original.getReferencedType(), modified.getReferencedType());

        // Branch check: returns this when same handler
        ReferenceType sameHandler = modified.withValueHandler(handler);
        assertSame(modified, sameHandler);
    }

    @Test(timeout = 4000)
    public void testWithContentTypeHandlerBranches() {
        ReferenceType original = ReferenceType.construct(AtomicReference.class, _stringType, null, null);

        Object handler = "contentTypeHandler1";
        ReferenceType modified = original.withContentTypeHandler(handler);

        assertNotSame(original, modified);
        assertSame(handler, modified.getReferencedType().getTypeHandler());

        // Branch check: returns this when same handler already present on referenced type
        ReferenceType same = modified.withContentTypeHandler(handler);
        assertSame(modified, same);
    }

    @Test(timeout = 4000)
    public void testWithContentValueHandlerBranches() {
        ReferenceType original = ReferenceType.construct(AtomicReference.class, _stringType, null, null);

        Object handler = "contentValHandler1";
        ReferenceType modified = original.withContentValueHandler(handler);

        assertNotSame(original, modified);
        assertSame(handler, modified.getReferencedType().getValueHandler());

        // Branch check: returns this when same handler already present on referenced type
        ReferenceType same = modified.withContentValueHandler(handler);
        assertSame(modified, same);
    }

    @Test(timeout = 4000)
    public void testWithStaticTypingBranches() {
        ReferenceType dynamicRef = new ReferenceType(AtomicReference.class, _stringType, null, null, false);
        assertFalse(dynamicRef.useStaticTyping());

        ReferenceType staticRef = dynamicRef.withStaticTyping();
        assertNotSame(dynamicRef, staticRef);
        assertTrue(staticRef.useStaticTyping());
        assertTrue(staticRef.getReferencedType().useStaticTyping());

        // Branch check: if already static, returns this
        ReferenceType sameStatic = staticRef.withStaticTyping();
        assertSame(staticRef, sameStatic);
    }

    @Test(timeout = 4000)
    public void testNarrow() {
        class CustomRef<T> extends AtomicReference<T> {
            private static final long serialVersionUID = 1L;
        }

        ReferenceType ref = ReferenceType.construct(AtomicReference.class, _stringType, null, null);
        JavaType narrowed = ref._narrow(CustomRef.class);

        assertTrue(narrowed instanceof ReferenceType);
        assertSame(CustomRef.class, narrowed.getRawClass());
        assertSame(_stringType, ((ReferenceType) narrowed).getReferencedType());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Canonical/Signatures
    // =========================================================================

    @Test(timeout = 4000)
    public void testErasedSignature() {
        ReferenceType refType = ReferenceType.construct(AtomicReference.class, _stringType, null, null);
        StringBuilder sb = new StringBuilder();
        StringBuilder res = refType.getErasedSignature(sb);

        assertSame(sb, res);
        assertEquals("Ljava/util/concurrent/atomic/AtomicReference;", sb.toString());
        assertEquals("Ljava/util/concurrent/atomic/AtomicReference;", refType.getErasedSignature());
    }

    @Test(timeout = 4000)
    public void testBuildCanonicalNameAndToString() {
        ReferenceType refType = ReferenceType.construct(AtomicReference.class, _stringType, null, null);
        String canonical = refType.buildCanonicalName();

        assertEquals("java.util.concurrent.atomic.AtomicReference<java.lang.String", canonical);

        String str = refType.toString();
        assertNotNull(str);
        assertTrue(str.contains("reference type"));
        assertTrue(str.contains(AtomicReference.class.getName()));
        assertTrue(str.contains(_stringType.toString()));
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (equals, hashCode)
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        ReferenceType ref1 = ReferenceType.construct(AtomicReference.class, _stringType, null, null);
        ReferenceType ref2 = ReferenceType.construct(AtomicReference.class, _stringType, null, null);
        ReferenceType refDifferentType = ReferenceType.construct(AtomicReference.class, _integerType, null, null);

        class OtherRef<T> extends AtomicReference<T> {
            private static final long serialVersionUID = 1L;
        }
        ReferenceType refDifferentClass = ReferenceType.construct(OtherRef.class, _stringType, null, null);

        // Reflexive
        assertTrue(ref1.equals(ref1));

        // Symmetric and equal state
        assertTrue(ref1.equals(ref2));
        assertTrue(ref2.equals(ref1));
        assertEquals(ref1.hashCode(), ref2.hashCode());

        // Null comparison
        assertFalse(ref1.equals(null));

        // Different class type
        assertFalse(ref1.equals("NotAType"));
        assertFalse(ref1.equals(_stringType));

        // Different raw class
        assertFalse(ref1.equals(refDifferentClass));

        // Different referenced type
        assertFalse(ref1.equals(refDifferentType));
    }
}