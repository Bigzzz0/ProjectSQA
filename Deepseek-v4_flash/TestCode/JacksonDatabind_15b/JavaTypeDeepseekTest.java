package com.fasterxml.jackson.databind;

import org.junit.Test;
import java.lang.reflect.Modifier;
import java.util.*;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 * - Constructor parameter combinations and hash computation
 * - _assertSubclass valid/invalid class relationships
 * - narrowBy/forcedNarrowBy/widenBy with compatible/incompatible types
 * - Copy methods (withTypeHandler, withValueHandler, etc.) state preservation
 * 
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - null handlers, empty handlers
 * - Same class narrowing/widening (returns 'this')
 * - Primitives, interfaces, abstract classes, final classes
 * - Enum types, array types, throwable types
 * 
 * Partition C: Defect-Targeted Branch Zone
 * - Issue #731: Static typing and handler propagation during type resolution
 * - NarrowBy/forcedNarrowBy with handler mismatch scenarios
 * - Value handler preservation during type coercion
 * 
 * Partition D: Exception & Defensive Guard Paths
 * - Incompatible subclass in _assertSubclass
 * - IllegalArgumentException from narrowBy/widenBy with wrong direction
 * - Abstract/interface concrete checks
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 * - hashCode() consistency with equals()
 * - isConcrete() for primitives, abstract, interfaces
 * - useStaticType() flag propagation
 * 
 * Decision branches targeted:
 * 1. `_class == subclass` in narrowBy/widenBy (returns this)
 * 2. Modifier.isAbstract | Modifier.INTERFACE + primitive fallback in isConcrete
 * 3. valueHandler/typeHandler null checks in narrowBy/forcedNarrowBy
 * 4. _assertSubclass assignability check branching
 * 5. isEnumType/isInterface/isPrimitive/isFinal delegates
 * 6. containedType* default return null/0
 * 7. getGenericSignature/getErasedSignature delegation to abstract methods
 */
public class JavaTypeDeepseekTest {

    // ============================================================
    // Helper: Concrete subclass of JavaType for testing
    // ============================================================
    private static class TestJavaType extends JavaType {
        private static final long serialVersionUID = 1L;
        
        private final JavaType _superClass;
        private final List<JavaType> _typeParams = new ArrayList<>();
        
        public TestJavaType(Class<?> raw, int additionalHash,
                            Object valueHandler, Object typeHandler, boolean asStatic) {
            super(raw, additionalHash, valueHandler, typeHandler, asStatic);
            _superClass = null;
        }
        
        public TestJavaType(Class<?> raw, int additionalHash,
                            Object valueHandler, Object typeHandler, boolean asStatic,
                            JavaType superClass) {
            super(raw, additionalHash, valueHandler, typeHandler, asStatic);
            _superClass = superClass;
        }

        @Override
        public JavaType withTypeHandler(Object h) {
            return new TestJavaType(_class, 0, _valueHandler, h, _asStatic);
        }

        @Override
        public JavaType withContentTypeHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withValueHandler(Object h) {
            return new TestJavaType(_class, 0, h, _typeHandler, _asStatic);
        }

        @Override
        public JavaType withContentValueHandler(Object h) {
            return this;
        }

        @Override
        public JavaType withStaticTyping() {
            return new TestJavaType(_class, 0, _valueHandler, _typeHandler, true);
        }

        @Override
        protected JavaType _narrow(Class<?> subclass) {
            return new TestJavaType(subclass, 0, _valueHandler, _typeHandler, _asStatic);
        }

        @Override
        public JavaType narrowContentsBy(Class<?> contentClass) {
            return this;
        }

        @Override
        public JavaType widenContentsBy(Class<?> contentClass) {
            return this;
        }

        @Override
        public boolean isContainerType() {
            return false;
        }

        @Override
        public Class<?> getParameterSource() {
            return null;
        }

        @Override
        public StringBuilder getGenericSignature(StringBuilder sb) {
            return sb.append("L").append(_class.getName().replace('.', '/')).append(";");
        }

        @Override
        public StringBuilder getErasedSignature(StringBuilder sb) {
            return sb.append("L").append(_class.getName().replace('.', '/')).append(";");
        }

        @Override
        public String toString() {
            return _class.getName();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null || o.getClass() != getClass()) return false;
            TestJavaType other = (TestJavaType) o;
            return _class == other._class && _asStatic == other._asStatic
                    && Objects.equals(_valueHandler, other._valueHandler)
                    && Objects.equals(_typeHandler, other._typeHandler);
        }
    }

    // ============================================================
    // Partition A: Core Functional Logic & State Transitions
    // ============================================================

    @Test(timeout = 4000)
    public void testConstructorAndHash() {
        TestJavaType type = new TestJavaType(String.class, 42, null, null, false);
        assertEquals(String.class, type.getRawClass());
        assertEquals(String.class.getName().hashCode() + 42, type.hashCode());
        assertFalse(type.useStaticType());
    }

    @Test(timeout = 4000)
    public void testWithTypeHandler() {
        TestJavaType base = new TestJavaType(Integer.class, 0, null, null, false);
        Object handler = new Object();
        JavaType modified = base.withTypeHandler(handler);
        assertEquals(handler, modified.getTypeHandler());
        assertNull(modified.getValueHandler());
        assertNotSame(base, modified);
    }

    @Test(timeout = 4000)
    public void testWithValueHandler() {
        TestJavaType base = new TestJavaType(Double.class, 0, null, null, false);
        Object handler = "value-handler";
        JavaType modified = base.withValueHandler(handler);
        assertEquals(handler, modified.getValueHandler());
        assertNull(modified.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testWithStaticTyping() {
        TestJavaType base = new TestJavaType(Long.class, 0, null, null, false);
        JavaType staticTyped = base.withStaticTyping();
        assertTrue(staticTyped.useStaticType());
    }

    @Test(timeout = 4000)
    public void testNarrowBySameClassReturnsThis() {
        TestJavaType base = new TestJavaType(Number.class, 0, null, null, false);
        assertSame(base, base.narrowBy(Number.class));
    }

    @Test(timeout = 4000)
    public void testNarrowByAssignableSubclass() {
        TestJavaType base = new TestJavaType(Number.class, 0, "vh", "th", false);
        JavaType narrowed = base.narrowBy(Integer.class);
        assertEquals(Integer.class, narrowed.getRawClass());
        // Handlers should propagate
        assertEquals("vh", narrowed.getValueHandler());
        assertEquals("th", narrowed.getTypeHandler());
        assertNotSame(base, narrowed);
    }

    @Test(timeout = 4000)
    public void testWidenBySameClassReturnsThis() {
        TestJavaType base = new TestJavaType(Integer.class, 0, null, null, false);
        assertSame(base, base.widenBy(Integer.class));
    }

    @Test(timeout = 4000)
    public void testWidenBySuperclass() {
        TestJavaType base = new TestJavaType(Integer.class, 0, null, null, false);
        JavaType widened = base.widenBy(Number.class);
        assertEquals(Number.class, widened.getRawClass());
    }

    @Test(timeout = 4000)
    public void testForcedNarrowBySameClassReturnsThis() {
        TestJavaType base = new TestJavaType(String.class, 0, null, null, false);
        assertSame(base, base.forcedNarrowBy(String.class));
    }

    @Test(timeout = 4000)
    public void testForcedNarrowByDifferentClass() {
        TestJavaType base = new TestJavaType(Object.class, 0, null, null, false);
        JavaType forced = base.forcedNarrowBy(String.class);
        assertEquals(String.class, forced.getRawClass());
    }

    @Test(timeout = 4000)
    public void testNarrowByHandlerMismatch() {
        // When _narrow creates a new type with different handler, original should propagate
        TestJavaType base = new TestJavaType(Number.class, 0, "original-vh", "original-th", false) {
            @Override
            protected JavaType _narrow(Class<?> subclass) {
                return new TestJavaType(subclass, 0, "new-vh", "new-th", _asStatic);
            }
        };
        JavaType narrowed = base.narrowBy(Integer.class);
        // Handlers from original should override due to the != check
        assertEquals("original-vh", narrowed.getValueHandler());
        assertEquals("original-th", narrowed.getTypeHandler());
    }

    // ============================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // ============================================================

    @Test(timeout = 4000)
    public void testNullHandlers() {
        TestJavaType type = new TestJavaType(String.class, 0, null, null, false);
        assertNull(type.getValueHandler());
        assertNull(type.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testPrimitiveChecks() {
        TestJavaType intType = new TestJavaType(int.class, 0, null, null, false);
        assertTrue(intType.isPrimitive());
        assertTrue(intType.isConcrete());  // Primitive is concrete despite abstract flag
        assertFalse(intType.isAbstract());
        assertFalse(intType.isEnumType());
        assertFalse(intType.isInterface());
    }

    @Test(timeout = 4000)
    public void testAbstractClass() {
        TestJavaType abstractType = new TestJavaType(Number.class, 0, null, null, false);
        assertTrue(abstractType.isAbstract());
        assertFalse(abstractType.isConcrete());  // Abstract class, not primitive
        assertFalse(abstractType.isPrimitive());
        assertFalse(abstractType.isFinal());
    }

    @Test(timeout = 4000)
    public void testFinalClass() {
        TestJavaType finalType = new TestJavaType(String.class, 0, null, null, false);
        assertTrue(finalType.isFinal());
        assertFalse(finalType.isAbstract());
    }

    @Test(timeout = 4000)
    public void testInterfaceType() {
        TestJavaType interfaceType = new TestJavaType(List.class, 0, null, null, false);
        assertTrue(interfaceType.isInterface());
        assertTrue(interfaceType.isAbstract());
        assertFalse(interfaceType.isConcrete());
    }

    @Test(timeout = 4000)
    public void testEnumType() {
        TestJavaType enumType = new TestJavaType(java.util.concurrent.TimeUnit.class, 0, null, null, false);
        assertTrue(enumType.isEnumType());
    }

    @Test(timeout = 4000)
    public void testThrowableType() {
        TestJavaType throwableType = new TestJavaType(RuntimeException.class, 0, null, null, false);
        assertTrue(throwableType.isThrowable());
        
        TestJavaType notThrowable = new TestJavaType(String.class, 0, null, null, false);
        assertFalse(notThrowable.isThrowable());
    }

    @Test(timeout = 4000)
    public void testArrayTypeDefault() {
        TestJavaType type = new TestJavaType(String[].class, 0, null, null, false);
        assertFalse(type.isArrayType());  // Default implementation returns false
    }

    @Test(timeout = 4000)
    public void testHasRawClass() {
        TestJavaType stringType = new TestJavaType(String.class, 0, null, null, false);
        assertTrue(stringType.hasRawClass(String.class));
        assertFalse(stringType.hasRawClass(Object.class));
    }

    @Test(timeout = 4000)
    public void testHasGenericTypesDefault() {
        TestJavaType type = new TestJavaType(String.class, 0, null, null, false);
        assertFalse(type.hasGenericTypes());  // containedTypeCount() == 0
    }

    @Test(timeout = 4000)
    public void testContainedTypeDefaults() {
        TestJavaType type = new TestJavaType(String.class, 0, null, null, false);
        assertNull(type.getKeyType());
        assertNull(type.getContentType());
        assertEquals(0, type.containedTypeCount());
        assertNull(type.containedType(0));
        assertNull(type.containedTypeName(0));
    }

    @Test(timeout = 4000)
    public void testContainedTypeOrUnknown() {
        TestJavaType type = new TestJavaType(String.class, 0, null, null, false);
        // Since containedType returns null, should get unknown type
        assertNotNull(type.containedTypeOrUnknown(0));
    }

    // ============================================================
    // Partition C: Defect-Targeted Branch Zone
    // ============================================================

    @Test(timeout = 4000)
    public void testNarrowByPreservesHandlersWhenNewTypeHasNullHandlers() {
        // Simulate case where _narrow returns type WITHOUT handlers,
        // but we need to propagate original handlers (as in narrowBy logic)
        TestJavaType base = new TestJavaType(Number.class, 0, "handler", "typeHandler", false) {
            @Override
            protected JavaType _narrow(Class<?> subclass) {
                // Return new type WITHOUT handlers (null)
                return new TestJavaType(subclass, 0, null, null, _asStatic);
            }
        };
        JavaType narrowed = base.narrowBy(Integer.class);
        // Original handlers should be set via withValueHandler/withTypeHandler
        assertEquals("handler", narrowed.getValueHandler());
        assertEquals("typeHandler", narrowed.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testDefectTargeted_Issue731_StaticTypingValueHandlerPropagation() {
        // The original defect: when resolving types with static typing flag,
        // handlers should be preserved during forcedNarrowBy operations
        // that may be triggered during serializer resolution.
        
        // Simulate a scenario where a type with static typing and handlers
        // goes through forcedNarrowBy
        Object valueHandler = new Object();
        Object typeHandler = new Object();
        
        TestJavaType sourceType = new TestJavaType(Object.class, 0, valueHandler, typeHandler, false);
        JavaType forced = sourceType.forcedNarrowBy(String.class);
        
        // Handlers MUST be preserved on the result
        assertEquals("Value handler should be preserved during forcedNarrowBy", 
                     valueHandler, forced.getValueHandler());
        assertEquals("Type handler should be preserved during forcedNarrowBy", 
                     typeHandler, forced.getTypeHandler());
        assertEquals(String.class, forced.getRawClass());
    }

    @Test(timeout = 4000)
    public void testNarrowByHandlersPreservedWithNullOriginal() {
        // Edge case: original has null handlers, _narrow returns non-null handlers
        TestJavaType base = new TestJavaType(Number.class, 0, null, null, false) {
            @Override
            protected JavaType _narrow(Class<?> subclass) {
                return new TestJavaType(subclass, 0, "new-vh", "new-th", _asStatic);
            }
        };
        JavaType narrowed = base.narrowBy(Integer.class);
        // Since original handlers are null, the != check fails for null,
        // so new type handlers should remain (null != "new-vh" is true, but
        // the check is: _valueHandler != result.getValueHandler())
        // Since _valueHandler is null and result.getValueHandler() is "new-vh",
        // condition is true, so it will override with original's null handlers
        assertNull("Original null handlers should override non-null ones", narrowed.getValueHandler());
        assertNull("Original null type handlers should override non-null ones", narrowed.getTypeHandler());
    }

    // ============================================================
    // Partition D: Exception & Defensive Guard Paths
    // ============================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNarrowByIncompatibleSubclass() {
        TestJavaType base = new TestJavaType(String.class, 0, null, null, false);
        base.narrowBy(Integer.class);  // Integer not assignable to String
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testWidenByIncompatibleSuperclass() {
        TestJavaType base = new TestJavaType(String.class, 0, null, null, false);
        base.widenBy(Integer.class);  // String not assignable from Integer
    }

    @Test(timeout = 4000)
    public void testAssertSubclassValid() {
        TestJavaType base = new TestJavaType(Number.class, 0, null, null, false);
        // Should not throw for valid subclass
        base._assertSubclass(Integer.class, Number.class);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAssertSubclassInvalid() {
        TestJavaType base = new TestJavaType(String.class, 0, null, null, false);
        base._assertSubclass(Integer.class, String.class);  // Should throw
    }

    @Test(timeout = 4000)
    public void testIsConcreteWithPrimitive() {
        TestJavaType primitive = new TestJavaType(int.class, 0, null, null, false);
        assertTrue(primitive.isConcrete());
    }

    @Test(timeout = 4000)
    public void testIsConcreteWithInterface() {
        TestJavaType iface = new TestJavaType(Cloneable.class, 0, null, null, false);
        assertFalse(iface.isConcrete());
    }

    @Test(timeout = 4000)
    public void testIsCollectionLikeDefault() {
        TestJavaType type = new TestJavaType(String.class, 0, null, null, false);
        assertFalse(type.isCollectionLikeType());
    }

    @Test(timeout = 4000)
    public void testIsMapLikeDefault() {
        TestJavaType type = new TestJavaType(String.class, 0, null, null, false);
        assertFalse(type.isMapLikeType());
    }

    @Test(timeout = 4000)
    public void testGetParameterSourceDefault() {
        TestJavaType type = new TestJavaType(String.class, 0, null, null, false);
        assertNull(type.getParameterSource());
    }

    // ============================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // ============================================================

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        TestJavaType type1 = new TestJavaType(String.class, 10, null, null, false);
        TestJavaType type2 = new TestJavaType(String.class, 10, null, null, false);
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEqualsReflexive() {
        TestJavaType type = new TestJavaType(String.class, 0, null, null, false);
        assertEquals(type, type);
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        TestJavaType type = new TestJavaType(String.class, 0, null, null, false);
        assertFalse(type.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        TestJavaType type = new TestJavaType(String.class, 0, null, null, false);
        assertFalse(type.equals("not a JavaType"));
    }

    @Test(timeout = 4000)
    public void testEqualsWithHandlers() {
        Object handler = new Object();
        TestJavaType type1 = new TestJavaType(String.class, 0, handler, null, false);
        TestJavaType type2 = new TestJavaType(String.class, 0, handler, null, false);
        assertEquals(type1, type2);
    }

    @Test(timeout = 4000)
    public void testToString() {
        TestJavaType type = new TestJavaType(String.class, 0, null, null, false);
        assertEquals("java.lang.String", type.toString());
    }

    @Test(timeout = 4000)
    public void testGetGenericSignature() {
        TestJavaType type = new TestJavaType(String.class, 0, null, null, false);
        String sig = type.getGenericSignature();
        assertEquals("Ljava/lang/String;", sig);
    }

    @Test(timeout = 4000)
    public void testGetErasedSignature() {
        TestJavaType type = new TestJavaType(Integer.class, 0, null, null, false);
        String sig = type.getErasedSignature();
        assertEquals("Ljava/lang/Integer;", sig);
    }

    @Test(timeout = 4000)
    public void testUseStaticType() {
        TestJavaType staticType = new TestJavaType(String.class, 0, null, null, true);
        assertTrue(staticType.useStaticType());
        
        TestJavaType nonStaticType = new TestJavaType(String.class, 0, null, null, false);
        assertFalse(nonStaticType.useStaticType());
    }

    @Test(timeout = 4000)
    public void testEqualityWithSameState() {
        TestJavaType type1 = new TestJavaType(String.class, 5, null, null, true);
        TestJavaType type2 = new TestJavaType(String.class, 5, null, null, true);
        assertTrue(type1.equals(type2));
        // hashCode must match if equals returns true
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    @Test(timeout = 4000)
    public void testInequalityWithDifferentClass() {
        TestJavaType stringType = new TestJavaType(String.class, 0, null, null, false);
        TestJavaType intType = new TestJavaType(Integer.class, 0, null, null, false);
        assertFalse(stringType.equals(intType));
    }

    @Test(timeout = 4000)
    public void testInequalityWithDifferentStaticTyping() {
        TestJavaType type1 = new TestJavaType(String.class, 0, null, null, false);
        TestJavaType type2 = new TestJavaType(String.class, 0, null, null, true);
        assertFalse(type1.equals(type2));
    }
}