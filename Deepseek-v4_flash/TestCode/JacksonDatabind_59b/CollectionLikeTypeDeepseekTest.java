package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.CollectionLikeType;
import com.fasterxml.jackson.databind.type.TypeBindings;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.SimpleType;

public class CollectionLikeTypeDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: CollectionLikeType.java
     * 
     * Branches/conditions exercised:
     * - construct(Class, TypeBindings, JavaType, JavaType[], JavaType): normal path
     * - construct(Class, JavaType): deprecated path with TypeVariable handling (vars null/empty vs length==1)
     * - upgradeFrom(JavaType, JavaType): TypeBase vs non-TypeBase (IllegalArgumentException)
     * - _narrow(Class): deprecated narrowing
     * - withContentType: identity when same element type, new instance otherwise
     * - withTypeHandler / withContentTypeHandler / withValueHandler / withContentValueHandler: new instances
     * - withStaticTyping: identity when already static, new instance otherwise
     * - refine: new instance with provided parameters
     * - isContainerType / isCollectionLikeType: always true
     * - getContentType / getContentValueHandler / getContentTypeHandler: delegation
     * - hasHandlers: super OR element handlers
     * - getErasedSignature / getGenericSignature: signature building
     * - buildCanonicalName: with/without element type
     * - isTrueCollectionType: Collection assignable vs not
     * - equals: same object, null, different class, equal/different class & element
     * - toString: format
     * 
     * Defect-targeted test (TypeRefinementForMapTest::testMapKeyRefinement1384):
     * - The bug occurs when a Map-like type is refined via TypeModifier, and the
     *   resulting CollectionLikeType does not properly propagate the content type
     *   handler/value handler during refinement, causing "Can not find a (Map) Key
     *   deserializer" for custom key types.
     * - The test below verifies that after refine() with a custom element type that
     *   has a content type handler, the refined type retains the handler.
     */
    
    private static class TestTypeBase extends TypeBase {
        private static final long serialVersionUID = 1L;
        
        protected TestTypeBase(Class<?> raw, TypeBindings bindings,
                JavaType superClass, JavaType[] superInts, int hash,
                Object valueHandler, Object typeHandler, boolean asStatic) {
            super(raw, bindings, superClass, superInts, hash, valueHandler, typeHandler, asStatic);
        }
        
        @Override
        public JavaType withContentType(JavaType contentType) {
            return null;
        }
        
        @Override
        public JavaType withTypeHandler(Object h) {
            return null;
        }
        
        @Override
        public JavaType withContentTypeHandler(Object h) {
            return null;
        }
        
        @Override
        public JavaType withValueHandler(Object h) {
            return null;
        }
        
        @Override
        public JavaType withContentValueHandler(Object h) {
            return null;
        }
        
        @Override
        public JavaType withStaticTyping() {
            return null;
        }
        
        @Override
        public JavaType refine(Class<?> rawType, TypeBindings bindings,
                JavaType superClass, JavaType[] superInterfaces) {
            return null;
        }
        
        @Override
        protected String buildCanonicalName() {
            return null;
        }
        
        @Override
        public boolean isContainerType() {
            return false;
        }
        
        @Override
        public int hashCode() {
            return 0;
        }
        
        @Override
        public boolean equals(Object o) {
            return false;
        }
        
        @Override
        public String toString() {
            return "test";
        }
    }
    
    private static class TestJavaType extends JavaType {
        private static final long serialVersionUID = 1L;
        
        private final Object valueHandler;
        private final Object typeHandler;
        
        protected TestJavaType(Class<?> raw, TypeBindings bindings,
                JavaType superClass, JavaType[] superInts, int hash,
                Object valueHandler, Object typeHandler, boolean asStatic) {
            super(raw, bindings, superClass, superInts, hash, valueHandler, typeHandler, asStatic);
            this.valueHandler = valueHandler;
            this.typeHandler = typeHandler;
        }
        
        @Override
        public JavaType withContentType(JavaType contentType) {
            return null;
        }
        
        @Override
        public JavaType withTypeHandler(Object h) {
            return new TestJavaType(_class, _bindings, _superClass, _superInterfaces,
                    _hash, _valueHandler, h, _asStatic);
        }
        
        @Override
        public JavaType withContentTypeHandler(Object h) {
            return new TestJavaType(_class, _bindings, _superClass, _superInterfaces,
                    _hash, _valueHandler, h, _asStatic);
        }
        
        @Override
        public JavaType withValueHandler(Object h) {
            return new TestJavaType(_class, _bindings, _superClass, _superInterfaces,
                    _hash, h, _typeHandler, _asStatic);
        }
        
        @Override
        public JavaType withContentValueHandler(Object h) {
            return new TestJavaType(_class, _bindings, _superClass, _superInterfaces,
                    _hash, h, _typeHandler, _asStatic);
        }
        
        @Override
        public JavaType withStaticTyping() {
            return new TestJavaType(_class, _bindings, _superClass, _superInterfaces,
                    _hash, _valueHandler, _typeHandler, true);
        }
        
        @Override
        public JavaType refine(Class<?> rawType, TypeBindings bindings,
                JavaType superClass, JavaType[] superInterfaces) {
            return new TestJavaType(rawType, bindings, superClass, superInterfaces,
                    _hash, _valueHandler, _typeHandler, _asStatic);
        }
        
        @Override
        protected String buildCanonicalName() {
            return _class.getName();
        }
        
        @Override
        public boolean isContainerType() {
            return false;
        }
        
        @Override
        public int hashCode() {
            return _hash;
        }
        
        @Override
        public boolean equals(Object o) {
            if (o == this) return true;
            if (o == null) return false;
            if (o.getClass() != getClass()) return false;
            TestJavaType other = (TestJavaType) o;
            return _class == other._class && _hash == other._hash;
        }
        
        @Override
        public String toString() {
            return _class.getName();
        }
        
        @Override
        public Object getValueHandler() {
            return valueHandler;
        }
        
        @Override
        public Object getTypeHandler() {
            return typeHandler;
        }
        
        @Override
        public boolean hasHandlers() {
            return valueHandler != null || typeHandler != null;
        }
    }
    
    // ===== Partition A: Core Functional Logic & State Transitions =====
    
    @Test(timeout = 4000)
    public void testConstructWithBindings() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        TypeBindings bindings = TypeBindings.emptyBindings();
        CollectionLikeType type = CollectionLikeType.construct(List.class, bindings,
                null, null, elemType);
        
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        assertEquals(elemType, type.getContentType());
        assertTrue(type.isContainerType());
        assertTrue(type.isCollectionLikeType());
        assertFalse(type.isTrueCollectionType()); // List is Collection
    }
    
    @Test(timeout = 4000)
    public void testConstructDeprecatedWithTypeVariable() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        assertNotNull(type);
        assertEquals(List.class, type.getRawClass());
        assertEquals(elemType, type.getContentType());
    }
    
    @Test(timeout = 4000)
    public void testConstructDeprecatedNoTypeVariable() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(MyCollection.class, elemType);
        
        assertNotNull(type);
        assertEquals(MyCollection.class, type.getRawClass());
        assertEquals(elemType, type.getContentType());
    }
    
    @Test(timeout = 4000)
    public void testUpgradeFromTypeBase() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(ArrayList.class);
        JavaType elemType = tf.constructType(String.class);
        
        CollectionLikeType type = CollectionLikeType.upgradeFrom(baseType, elemType);
        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
        assertEquals(elemType, type.getContentType());
    }
    
    @Test(timeout = 4000)
    public void testUpgradeFromNonTypeBase() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(String.class);
        JavaType elemType = tf.constructType(String.class);
        
        try {
            CollectionLikeType.upgradeFrom(baseType, elemType);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testNarrow() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        CollectionLikeType narrowed = type._narrow(ArrayList.class);
        assertNotNull(narrowed);
        assertEquals(ArrayList.class, narrowed.getRawClass());
        assertEquals(elemType, narrowed.getContentType());
    }
    
    @Test(timeout = 4000)
    public void testWithContentTypeSame() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        assertSame(type, type.withContentType(elemType));
    }
    
    @Test(timeout = 4000)
    public void testWithContentTypeDifferent() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType1 = tf.constructType(String.class);
        JavaType elemType2 = tf.constructType(Integer.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType1);
        
        CollectionLikeType newType = type.withContentType(elemType2);
        assertNotSame(type, newType);
        assertEquals(elemType2, newType.getContentType());
    }
    
    @Test(timeout = 4000)
    public void testWithTypeHandler() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        Object handler = new Object();
        CollectionLikeType newType = type.withTypeHandler(handler);
        assertNotSame(type, newType);
        assertEquals(handler, newType.getTypeHandler());
    }
    
    @Test(timeout = 4000)
    public void testWithContentTypeHandler() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        Object handler = new Object();
        CollectionLikeType newType = type.withContentTypeHandler(handler);
        assertNotSame(type, newType);
        assertEquals(handler, newType.getContentTypeHandler());
    }
    
    @Test(timeout = 4000)
    public void testWithValueHandler() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        Object handler = new Object();
        CollectionLikeType newType = type.withValueHandler(handler);
        assertNotSame(type, newType);
        assertEquals(handler, newType.getValueHandler());
    }
    
    @Test(timeout = 4000)
    public void testWithContentValueHandler() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        Object handler = new Object();
        CollectionLikeType newType = type.withContentValueHandler(handler);
        assertNotSame(type, newType);
        assertEquals(handler, newType.getContentValueHandler());
    }
    
    @Test(timeout = 4000)
    public void testWithStaticTypingAlreadyStatic() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        CollectionLikeType staticType = type.withStaticTyping();
        
        assertSame(staticType, staticType.withStaticTyping());
    }
    
    @Test(timeout = 4000)
    public void testWithStaticTypingNotStatic() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        CollectionLikeType staticType = type.withStaticTyping();
        assertNotSame(type, staticType);
        assertTrue(staticType.isStaticTyping());
    }
    
    @Test(timeout = 4000)
    public void testRefine() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        TypeBindings bindings = TypeBindings.emptyBindings();
        CollectionLikeType refined = type.refine(ArrayList.class, bindings, null, null);
        assertNotNull(refined);
        assertEquals(ArrayList.class, refined.getRawClass());
        assertEquals(elemType, refined.getContentType());
    }
    
    // ===== Partition B: Boundary Value Analysis (BVA) & Extremes =====
    
    @Test(timeout = 4000)
    public void testNullElementType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        try {
            CollectionLikeType.construct(List.class, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testNullRawType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        try {
            CollectionLikeType.construct(null, elemType);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testEmptyBindings() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        TypeBindings bindings = TypeBindings.emptyBindings();
        CollectionLikeType type = CollectionLikeType.construct(List.class, bindings,
                null, null, elemType);
        assertNotNull(type);
    }
    
    @Test(timeout = 4000)
    public void testNullSuperClassAndInterfaces() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        TypeBindings bindings = TypeBindings.emptyBindings();
        CollectionLikeType type = CollectionLikeType.construct(List.class, bindings,
                null, null, elemType);
        assertNotNull(type);
    }
    
    @Test(timeout = 4000)
    public void testIsTrueCollectionTypeTrue() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(ArrayList.class, elemType);
        assertTrue(type.isTrueCollectionType());
    }
    
    @Test(timeout = 4000)
    public void testIsTrueCollectionTypeFalse() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(MyCollection.class, elemType);
        assertFalse(type.isTrueCollectionType());
    }
    
    // ===== Partition C: Defect-Targeted Branch Zone =====
    
    /**
     * Targets the known defect from TypeRefinementForMapTest::testMapKeyRefinement1384.
     * The bug occurs when a Map-like type is refined and the content type handler
     * is lost during refinement, causing "Can not find a (Map) Key deserializer".
     * This test verifies that after refine(), the content type handler is preserved.
     */
    @Test(timeout = 4000)
    public void testRefinePreservesContentTypeHandler() {
        // Create a custom element type with a content type handler
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseElemType = tf.constructType(String.class);
        
        // Create a custom JavaType that has a content type handler
        Object handler = new Object();
        TestJavaType customElemType = new TestJavaType(
                String.class, TypeBindings.emptyBindings(), null, null,
                baseElemType.hashCode(), null, handler, false);
        
        // Create a CollectionLikeType with this custom element type
        CollectionLikeType type = CollectionLikeType.construct(List.class, customElemType);
        
        // Refine the type (simulating what TypeModifier does)
        TypeBindings bindings = TypeBindings.emptyBindings();
        CollectionLikeType refined = type.refine(ArrayList.class, bindings, null, null);
        
        // The refined type must preserve the content type handler
        assertNotNull("Refined type should have content type handler", 
                refined.getContentTypeHandler());
        assertEquals("Content type handler should be preserved", 
                handler, refined.getContentTypeHandler());
    }
    
    @Test(timeout = 4000)
    public void testRefinePreservesContentValueHandler() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseElemType = tf.constructType(String.class);
        
        Object handler = new Object();
        TestJavaType customElemType = new TestJavaType(
                String.class, TypeBindings.emptyBindings(), null, null,
                baseElemType.hashCode(), handler, null, false);
        
        CollectionLikeType type = CollectionLikeType.construct(List.class, customElemType);
        
        TypeBindings bindings = TypeBindings.emptyBindings();
        CollectionLikeType refined = type.refine(ArrayList.class, bindings, null, null);
        
        assertNotNull("Refined type should have content value handler", 
                refined.getContentValueHandler());
        assertEquals("Content value handler should be preserved", 
                handler, refined.getContentValueHandler());
    }
    
    // ===== Partition D: Exception & Defensive Guard Paths =====
    
    @Test(timeout = 4000)
    public void testUpgradeFromNullBaseType() {
        JavaType elemType = TypeFactory.defaultInstance().constructType(String.class);
        try {
            CollectionLikeType.upgradeFrom(null, elemType);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testUpgradeFromNullElementType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType baseType = tf.constructType(ArrayList.class);
        try {
            CollectionLikeType.upgradeFrom(baseType, null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
    
    @Test(timeout = 4000)
    public void testWithContentTypeNull() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        try {
            type.withContentType(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }
    
    // ===== Partition E: Object Lifecycle & Contract Integrity =====
    
    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        assertTrue(type.equals(type));
    }
    
    @Test(timeout = 4000)
    public void testEqualsNull() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        assertFalse(type.equals(null));
    }
    
    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        assertFalse(type.equals(new Object()));
    }
    
    @Test(timeout = 4000)
    public void testEqualsEqualTypes() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type1 = CollectionLikeType.construct(List.class, elemType);
        CollectionLikeType type2 = CollectionLikeType.construct(List.class, elemType);
        
        assertTrue(type1.equals(type2));
        assertTrue(type2.equals(type1));
        assertEquals(type1.hashCode(), type2.hashCode());
    }
    
    @Test(timeout = 4000)
    public void testEqualsDifferentElementType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType1 = tf.constructType(String.class);
        JavaType elemType2 = tf.constructType(Integer.class);
        CollectionLikeType type1 = CollectionLikeType.construct(List.class, elemType1);
        CollectionLikeType type2 = CollectionLikeType.construct(List.class, elemType2);
        
        assertFalse(type1.equals(type2));
    }
    
    @Test(timeout = 4000)
    public void testEqualsDifferentRawType() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type1 = CollectionLikeType.construct(List.class, elemType);
        CollectionLikeType type2 = CollectionLikeType.construct(ArrayList.class, elemType);
        
        assertFalse(type1.equals(type2));
    }
    
    @Test(timeout = 4000)
    public void testToString() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        String str = type.toString();
        assertNotNull(str);
        assertTrue(str.contains("collection-like type"));
        assertTrue(str.contains(List.class.getName()));
        assertTrue(str.contains(elemType.toString()));
    }
    
    @Test(timeout = 4000)
    public void testGetErasedSignature() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        StringBuilder sb = new StringBuilder();
        StringBuilder result = type.getErasedSignature(sb);
        assertSame(sb, result);
        assertTrue(sb.toString().contains(List.class.getName()));
    }
    
    @Test(timeout = 4000)
    public void testGetGenericSignature() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        StringBuilder sb = new StringBuilder();
        StringBuilder result = type.getGenericSignature(sb);
        assertSame(sb, result);
        String sig = sb.toString();
        assertTrue(sig.contains(List.class.getName()));
        assertTrue(sig.contains("<"));
        assertTrue(sig.contains(">"));
    }
    
    @Test(timeout = 4000)
    public void testBuildCanonicalNameWithElement() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        String canonical = type.buildCanonicalName();
        assertNotNull(canonical);
        assertTrue(canonical.contains(List.class.getName()));
        assertTrue(canonical.contains("<"));
        assertTrue(canonical.contains(">"));
    }
    
    @Test(timeout = 4000)
    public void testHasHandlersNoHandlers() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        assertFalse(type.hasHandlers());
    }
    
    @Test(timeout = 4000)
    public void testHasHandlersWithTypeHandler() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        CollectionLikeType withHandler = type.withTypeHandler(new Object());
        
        assertTrue(withHandler.hasHandlers());
    }
    
    @Test(timeout = 4000)
    public void testHasHandlersWithContentTypeHandler() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        CollectionLikeType withHandler = type.withContentTypeHandler(new Object());
        
        assertTrue(withHandler.hasHandlers());
    }
    
    @Test(timeout = 4000)
    public void testGetContentValueHandlerNull() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        assertNull(type.getContentValueHandler());
    }
    
    @Test(timeout = 4000)
    public void testGetContentTypeHandlerNull() {
        TypeFactory tf = TypeFactory.defaultInstance();
        JavaType elemType = tf.constructType(String.class);
        CollectionLikeType type = CollectionLikeType.construct(List.class, elemType);
        
        assertNull(type.getContentTypeHandler());
    }
    
    // Helper class for testing non-Collection-like types
    private static class MyCollection {
        // Not a Collection subclass
    }
}