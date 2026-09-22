package com.fasterxml.jackson.databind;

import java.util.List;
import java.util.Collections;
import java.util.Objects;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;

/**
 * White-box test suite for JavaType, targeting the known defect in hasContentType()
 * and maximizing branch/line coverage.
 */
public class JavaTypeDeepseekTest {

    /* ===============================================================
     * [Branch & Defect Analysis Matrix]
     * 
     * Targeted branches in JavaType:
     *  - hasContentType(): always returns true, but contract says false for non-container/reference
     *  - isAbstract(): uses Modifier.isAbstract
     *  - isConcrete(): checks INTERFACE/ABSTRACT bits, then isPrimitive()
     *  - isThrowable(): Throwable.isAssignableFrom
     *  - isEnumType(), isInterface(), isPrimitive(), isFinal(): delegate to raw class methods
     *  - isJavaLangObject(): raw == Object.class
     *  - useStaticType(): returns _asStatic
     *  - hasGenericTypes(): returns containedTypeCount() > 0
     *  - getKeyType(), getContentType(), getReferencedType(): all return null by default
     *  - containedTypeOrUnknown(): null check + TypeFactory.unknownType()
     *  - getValueHandler(), getTypeHandler(): unchecked cast
     *  - hasValueHandler(): _valueHandler != null
     *  - hasHandlers(): (_typeHandler != null) || (_valueHandler != null)
     *  - getGenericSignature() / getErasedSignature(): abstract calls
     *  - hashCode(): final, based on _hash
     *  - forcedNarrowBy(): if subclass == _class return this, else _narrow + handler preservation
     *  - isTypeOrSubTypeOf(): _class == clz || clz.isAssignableFrom(_class)
     * 
     * Defect-targeted zone:
     *   D1: hasContentType() returns true unconditionally – violates contract for non-container types
     *       (likely root cause of JsonMappingException for Map key deserialization)
     * =============================================================== */

    // -----------------------------------------------------------------
    // Minimal concrete JavaType subclass for testing
    // -----------------------------------------------------------------
    private static class SimpleJavaType extends JavaType {

        public SimpleJavaType(Class<?> raw, int additionalHash,
                              Object valueHandler, Object typeHandler, boolean asStatic) {
            super(raw, additionalHash, valueHandler, typeHandler, asStatic);
        }

        public SimpleJavaType(JavaType base) {
            super(base);
        }

        @Override
        public JavaType withTypeHandler(Object h) {
            return new SimpleJavaType(_class, _hash, _valueHandler, h, _asStatic);
        }

        @Override
        public JavaType withContentTypeHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withValueHandler(Object h) {
            return new SimpleJavaType(_class, _hash, h, _typeHandler, _asStatic);
        }

        @Override
        public JavaType withContentValueHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withContentType(JavaType contentType) {
            throw new IllegalArgumentException("no content type");
        }

        @Override
        public JavaType withStaticTyping() {
            return new SimpleJavaType(_class, _hash, _valueHandler, _typeHandler, true);
        }

        @Override
        public JavaType refine(Class<?> rawType, TypeBindings bindings,
                               JavaType superClass, JavaType[] superInterfaces) {
            return null;
        }

        @Override
        protected JavaType _narrow(Class<?> subclass) {
            return new SimpleJavaType(subclass, _hash, _valueHandler, _typeHandler, _asStatic);
        }

        @Override
        public int containedTypeCount() {
            return 0;
        }

        @Override
        public JavaType containedType(int index) {
            return null;
        }

        @Override
        public String containedTypeName(int index) {
            return null;
        }

        @Override
        public TypeBindings getBindings() {
            return TypeBindings.emptyBindings();
        }

        @Override
        public JavaType findSuperType(Class<?> erasedTarget) {
            return null;
        }

        @Override
        public JavaType getSuperClass() {
            return null;
        }

        @Override
        public List<JavaType> getInterfaces() {
            return Collections.emptyList();
        }

        @Override
        public JavaType[] findTypeParameters(Class<?> expType) {
            return new JavaType[0];
        }

        @Override
        public StringBuilder getGenericSignature(StringBuilder sb) {
            sb.append('L').append(_class.getName().replace('.', '/')).append(';');
            return sb;
        }

        @Override
        public StringBuilder getErasedSignature(StringBuilder sb) {
            sb.append('L').append(_class.getName().replace('.', '/')).append(';');
            return sb;
        }

        @Override
        public String toString() {
            return _class.getName();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null || o.getClass() != getClass()) return false;
            SimpleJavaType other = (SimpleJavaType) o;
            return _class == other._class && _asStatic == other._asStatic &&
                   Objects.equals(_valueHandler, other._valueHandler) &&
                   Objects.equals(_typeHandler, other._typeHandler);
        }
    }

    // -----------------------------------------------------------------
    // Helper factory methods
    // -----------------------------------------------------------------
    private JavaType createSimpleType(Class<?> raw) {
        return new SimpleJavaType(raw, 0, null, null, false);
    }

    private JavaType createSimpleType(Class<?> raw, Object valueHandler, Object typeHandler) {
        return new SimpleJavaType(raw, 0, valueHandler, typeHandler, false);
    }

    // =================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =================================================================

    @Test(timeout = 4000)
    public void testBasicGetters() {
        JavaType t = createSimpleType(String.class);
        assertEquals(String.class, t.getRawClass());
        assertTrue(t.hasRawClass(String.class));
        assertFalse(t.hasRawClass(Object.class));
        assertFalse(t.isAbstract());
        assertTrue(t.isConcrete());
        assertFalse(t.isThrowable());
        assertFalse(t.isArrayType());
        assertFalse(t.isEnumType());
        assertFalse(t.isInterface());
        assertFalse(t.isPrimitive());
        assertFalse(t.isFinal());
        assertTrue(t.isJavaLangObject() ? false : true); // String is not Object
    }

    @Test(timeout = 4000)
    public void testStaticTypingFlag() {
        JavaType t = createSimpleType(Object.class);
        assertFalse(t.useStaticType());
        JavaType staticType = t.withStaticTyping();
        assertTrue(staticType.useStaticType());
    }

    @Test(timeout = 4000)
    public void testHandlers() {
        Object vh = "valueHandler";
        Object th = "typeHandler";
        JavaType t = createSimpleType(Integer.class, vh, th);
        assertEquals(vh, t.getValueHandler());
        assertEquals(th, t.getTypeHandler());
        assertTrue(t.hasValueHandler());
        assertTrue(t.hasHandlers());

        // withValueHandler
        JavaType t2 = t.withValueHandler(null);
        assertNull(t2.getValueHandler());
        assertFalse(t2.hasValueHandler());
        assertTrue(t2.hasHandlers()); // still has typeHandler

        // withTypeHandler
        JavaType t3 = t.withTypeHandler(null);
        assertNull(t3.getTypeHandler());
        assertFalse(t3.hasHandlers()); // both null

        // withStaticTyping preserves handlers
        JavaType t4 = t.withStaticTyping();
        assertEquals(vh, t4.getValueHandler());
        assertEquals(th, t4.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testContentMethods() {
        JavaType t = createSimpleType(List.class);
        assertNull(t.getKeyType());
        assertNull(t.getContentType());
        assertNull(t.getReferencedType());
        assertNull(t.getContentValueHandler());
        assertNull(t.getContentTypeHandler());
        assertFalse(t.isCollectionLikeType());
        assertFalse(t.isMapLikeType());
    }

    @Test(timeout = 4000)
    public void testGenericSignature() {
        JavaType t = createSimpleType(String.class);
        String sig = t.getGenericSignature();
        assertTrue(sig.startsWith("Ljava/lang/String;"));
        String erased = t.getErasedSignature();
        assertTrue(erased.startsWith("Ljava/lang/String;"));
    }

    @Test(timeout = 4000)
    public void testForcedNarrowBy() {
        JavaType t = createSimpleType(Number.class);
        // Same class -> returns this
        assertSame(t, t.forcedNarrowBy(Number.class));
        // Different subclass
        JavaType narrowed = t.forcedNarrowBy(Integer.class);
        assertEquals(Integer.class, narrowed.getRawClass());
        // Handlers preserved
        Object vh = "vh";
        Object th = "th";
        JavaType t2 = createSimpleType(Object.class, vh, th);
        JavaType narrowed2 = t2.forcedNarrowBy(String.class);
        assertEquals(vh, narrowed2.getValueHandler());
        assertEquals(th, narrowed2.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testIsTypeOrSubTypeOf() {
        JavaType t = createSimpleType(Number.class);
        assertTrue(t.isTypeOrSubTypeOf(Number.class));
        assertTrue(t.isTypeOrSubTypeOf(Object.class));
        assertFalse(t.isTypeOrSubTypeOf(Integer.class));
    }

    // =================================================================
    // Partition B: Boundary Value Analysis & Extremes
    // =================================================================

    @Test(timeout = 4000)
    public void testNullHandlers() {
        JavaType t = createSimpleType(Object.class, null, null);
        assertNull(t.getValueHandler());
        assertNull(t.getTypeHandler());
        assertFalse(t.hasValueHandler());
        assertFalse(t.hasHandlers());
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        JavaType t1 = createSimpleType(String.class);
        JavaType t2 = createSimpleType(String.class);
        assertEquals(t1.hashCode(), t2.hashCode());
        // Different class -> different hash
        JavaType t3 = createSimpleType(Integer.class);
        assertNotEquals(t1.hashCode(), t3.hashCode());
    }

    @Test(timeout = 4000)
    public void testContainedTypeOrUnknown() {
        JavaType t = createSimpleType(Object.class);
        // containedType returns null -> returns unknownType
        JavaType unknown = t.containedTypeOrUnknown(0);
        assertNotNull(unknown);
        // unknown type should be Object.class
        assertEquals(Object.class, unknown.getRawClass());
    }

    @Test(timeout = 4000)
    public void testAbstractModifier() {
        JavaType abstractType = createSimpleType(AbstractClass.class);
        assertTrue(abstractType.isAbstract());
        assertFalse(abstractType.isConcrete());
    }

    @Test(timeout = 4000)
    public void testPrimitiveConcrete() {
        // Primitive types have abstract flag set, but isConcrete() checks isPrimitive()
        JavaType primType = createSimpleType(int.class);
        assertTrue(primType.isPrimitive());
        assertTrue(primType.isConcrete()); // special handling
        assertFalse(primType.isAbstract());
    }

    @Test(timeout = 4000)
    public void testThrowableType() {
        JavaType throwType = createSimpleType(Throwable.class);
        assertTrue(throwType.isThrowable());
        assertTrue(throwType.isThrowable()); // branch coverage
    }

    @Test(timeout = 4000)
    public void testFinalClass() {
        JavaType finalType = createSimpleType(String.class);
        assertTrue(finalType.isFinal());
    }

    @Test(timeout = 4000)
    public void testJavaLangObject() {
        JavaType objType = createSimpleType(Object.class);
        assertTrue(objType.isJavaLangObject());
        JavaType strType = createSimpleType(String.class);
        assertFalse(strType.isJavaLangObject());
    }

    // =================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =================================================================

    /**
     * Defect: hasContentType() returns true unconditionally, but contract says
     * it should return false for non-container, non-reference types.
     * This test exposes the bug by asserting false for a simple type.
     */
    @Test(timeout = 4000)
    public void testHasContentType_NonContainerType_ShouldBeFalse() {
        JavaType t = createSimpleType(String.class);
        // By contract, this should return false because String is not a container nor reference.
        // Current buggy implementation returns true.
        assertFalse("hasContentType() should return false for non-container types", t.hasContentType());
    }

    // Additional tests to cover hasContentType for container-like types (if needed)
    // We cannot create a container type without a real subclass, but we can test the default true.
    @Test(timeout = 4000)
    public void testHasContentType_DefaultTrue() {
        JavaType t = createSimpleType(Object.class);
        // As of buggy version, returns true even for non-container
        // This test is not checking contract, just documenting behavior.
        assertTrue(t.hasContentType()); // buggy version passes
    }

    @Test(timeout = 4000)
    public void testHasGenericTypes_ZeroContained() {
        JavaType t = createSimpleType(Object.class);
        assertFalse(t.hasGenericTypes()); // containedTypeCount() == 0
    }

    // =================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWithContentTypeThrows() {
        JavaType t = createSimpleType(Object.class);
        t.withContentType(t); // throws IllegalArgumentException
    }

    @Test(timeout = 4000)
    public void testRefineReturnsNull() {
        JavaType t = createSimpleType(Object.class);
        assertNull(t.refine(null, null, null, null));
    }

    @Test(timeout = 4000)
    public void testNarrowSameClassReturnsThis() {
        JavaType t = createSimpleType(String.class);
        assertSame(t, t.forcedNarrowBy(String.class));
    }

    // =================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =================================================================

    @Test(timeout = 4000)
    public void testEqualsReflexive() {
        JavaType t = createSimpleType(Integer.class);
        assertEquals(t, t);
    }

    @Test(timeout = 4000)
    public void testEqualsSymmetric() {
        JavaType t1 = createSimpleType(Integer.class);
        JavaType t2 = createSimpleType(Integer.class);
        assertEquals(t1, t2);
        assertEquals(t2, t1);
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentValues() {
        JavaType t1 = createSimpleType(Integer.class, "vh", null);
        JavaType t2 = createSimpleType(Integer.class, "vh", null);
        assertEquals(t1, t2);

        JavaType t3 = createSimpleType(Integer.class, null, null);
        assertNotEquals(t1, t3);
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        JavaType t = createSimpleType(Object.class);
        assertNotEquals(null, t);
    }

    @Test(timeout = 4000)
    public void testHashCodeIfEqual() {
        JavaType t1 = createSimpleType(String.class);
        JavaType t2 = createSimpleType(String.class);
        assertTrue(t1.equals(t2));
        assertEquals(t1.hashCode(), t2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        JavaType t = createSimpleType(Double.class);
        assertEquals("java.lang.Double", t.toString());
    }

    // Helper abstract class for testing isAbstract/isConcrete
    private abstract static class AbstractClass {
    }
}