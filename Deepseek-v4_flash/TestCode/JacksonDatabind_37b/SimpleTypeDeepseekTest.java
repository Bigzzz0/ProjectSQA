package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.*;

import com.fasterxml.jackson.databind.JavaType;

public class SimpleTypeDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target class: SimpleType (extends TypeBase)
     * 
     * Defect: In Objecid1083Test::testSimple, an UnrecognizedPropertyException is thrown
     * for field "name" on class JsonMapSchema. This indicates that the type resolution
     * for a simple type is not properly handling generic bindings or super-type information,
     * likely due to the `refine()` method returning null (which causes the type to be
     * treated as a raw/unresolved type), or due to `constructUnsafe()` not properly
     * initializing bindings (passing null instead of empty bindings).
     * 
     * Key branches to test:
     * 1. constructUnsafe(Class) - with null bindings (potential NPE in buildCanonicalName/getGenericSignature)
     * 2. construct(Class) - validation for Map, Collection, array types
     * 3. _narrow(Class) - when subclass == _class (returns this) vs different class
     * 4. withTypeHandler/withValueHandler - identity check (returns this if same handler)
     * 5. withStaticTyping - returns this if already static
     * 6. refine() - always returns null (defect zone)
     * 7. buildCanonicalName() - with 0, 1, multiple bindings
     * 8. getGenericSignature() - with 0, 1, multiple bindings
     * 9. equals() - class comparison, bindings comparison
     * 10. withContentType/withContentTypeHandler/withContentValueHandler - always throw
     * 
     * The defect is specifically triggered when a SimpleType is created via constructUnsafe
     * (which passes null bindings) and then used in a context where generic signature or
     * canonical name is built, causing NPE. Also, refine() returning null may cause
     * downstream issues.
     */

    // Helper to create a SimpleType instance (since constructors are protected)
    private static class TestSimpleType extends SimpleType {
        public TestSimpleType(Class<?> cls) {
            super(cls);
        }
        public TestSimpleType(Class<?> cls, TypeBindings bindings) {
            super(cls, bindings, null, null);
        }
        public TestSimpleType(Class<?> cls, TypeBindings bindings, JavaType superClass, JavaType[] superInts) {
            super(cls, bindings, superClass, superInts);
        }
        public TestSimpleType(TypeBase base) {
            super(base);
        }
        public TestSimpleType(Class<?> cls, TypeBindings bindings, JavaType superClass, JavaType[] superInts,
                Object valueHandler, Object typeHandler, boolean asStatic) {
            super(cls, bindings, superClass, superInts, valueHandler, typeHandler, asStatic);
        }
        public TestSimpleType(Class<?> cls, TypeBindings bindings, JavaType superClass, JavaType[] superInts,
                int extraHash, Object valueHandler, Object typeHandler, boolean asStatic) {
            super(cls, bindings, superClass, superInts, extraHash, valueHandler, typeHandler, asStatic);
        }
    }

    // ========== Partition A: Core Functional Logic & State Transitions ==========

    @Test(timeout = 4000)
    public void testConstructUnsafeWithNullBindings() {
        // constructUnsafe passes null bindings - this is the defect zone
        SimpleType type = SimpleType.constructUnsafe(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        // This should not throw NPE even with null bindings (but may in defective version)
        // The defect is that buildCanonicalName() calls _bindings.size() which would NPE
        try {
            String canonical = type.toCanonical();
            assertNotNull(canonical);
            assertEquals("java.lang.String", canonical);
        } catch (NullPointerException e) {
            fail("NPE thrown when accessing canonical name with null bindings: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testConstructUnsafeWithBindings() {
        // Create a type with actual bindings
        TypeBindings bindings = TypeBindings.create(String.class, new JavaType[] { 
            TypeFactory.defaultInstance().constructType(Integer.class) 
        });
        SimpleType type = new TestSimpleType(String.class, bindings);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        assertEquals(1, type.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testConstructValidClass() {
        SimpleType type = SimpleType.construct(String.class);
        assertNotNull(type);
        assertEquals(String.class, type.getRawClass());
        assertEquals("[simple type, class java.lang.String]", type.toString());
    }

    @Test(timeout = 4000)
    public void testConstructWithGenericClass() {
        // Should work for non-Map/Collection/array types
        SimpleType type = SimpleType.construct(ArrayList.class);
        assertNotNull(type);
        assertEquals(ArrayList.class, type.getRawClass());
    }

    @Test(timeout = 4000)
    public void testNarrowSameClass() {
        SimpleType type = SimpleType.construct(String.class);
        JavaType result = type._narrow(String.class);
        assertSame(type, result); // Should return this when same class
    }

    @Test(timeout = 4000)
    public void testNarrowDifferentClass() {
        SimpleType type = SimpleType.construct(Object.class);
        JavaType result = type._narrow(String.class);
        assertNotNull(result);
        assertEquals(String.class, result.getRawClass());
        assertNotSame(type, result);
    }

    @Test(timeout = 4000)
    public void testWithTypeHandlerSameInstance() {
        SimpleType type = SimpleType.construct(String.class);
        Object handler = new Object();
        SimpleType withHandler = type.withTypeHandler(handler);
        assertNotSame(type, withHandler);
        assertSame(handler, withHandler.getTypeHandler());
        
        // Setting same handler should return this
        SimpleType sameHandler = withHandler.withTypeHandler(handler);
        assertSame(withHandler, sameHandler);
    }

    @Test(timeout = 4000)
    public void testWithValueHandlerSameInstance() {
        SimpleType type = SimpleType.construct(String.class);
        Object handler = new Object();
        SimpleType withHandler = type.withValueHandler(handler);
        assertNotSame(type, withHandler);
        assertSame(handler, withHandler.getValueHandler());
        
        // Setting same handler should return this
        SimpleType sameHandler = withHandler.withValueHandler(handler);
        assertSame(withHandler, sameHandler);
    }

    @Test(timeout = 4000)
    public void testWithStaticTyping() {
        SimpleType type = SimpleType.construct(String.class);
        SimpleType staticType = type.withStaticTyping();
        assertNotSame(type, staticType);
        assertTrue(staticType.isStatic());
        
        // Calling again should return this
        SimpleType staticAgain = staticType.withStaticTyping();
        assertSame(staticType, staticAgain);
    }

    @Test(timeout = 4000)
    public void testRefineReturnsNull() {
        SimpleType type = SimpleType.construct(String.class);
        JavaType result = type.refine(String.class, TypeBindings.emptyBindings(), null, null);
        assertNull(result); // refine() always returns null for SimpleType
    }

    @Test(timeout = 4000)
    public void testIsContainerType() {
        SimpleType type = SimpleType.construct(String.class);
        assertFalse(type.isContainerType());
    }

    @Test(timeout = 4000)
    public void testGetErasedSignature() {
        SimpleType type = SimpleType.construct(String.class);
        StringBuilder sb = new StringBuilder();
        StringBuilder result = type.getErasedSignature(sb);
        assertSame(sb, result);
        assertEquals("Ljava/lang/String;", result.toString());
    }

    @Test(timeout = 4000)
    public void testGetGenericSignatureNoBindings() {
        SimpleType type = SimpleType.construct(String.class);
        StringBuilder sb = new StringBuilder();
        StringBuilder result = type.getGenericSignature(sb);
        assertSame(sb, result);
        assertEquals("Ljava/lang/String;", result.toString());
    }

    @Test(timeout = 4000)
    public void testGetGenericSignatureWithBindings() {
        TypeBindings bindings = TypeBindings.create(String.class, new JavaType[] { 
            TypeFactory.defaultInstance().constructType(Integer.class) 
        });
        SimpleType type = new TestSimpleType(String.class, bindings);
        StringBuilder sb = new StringBuilder();
        StringBuilder result = type.getGenericSignature(sb);
        assertSame(sb, result);
        assertEquals("Ljava/lang/String<Ljava/lang/Integer;>;", result.toString());
    }

    @Test(timeout = 4000)
    public void testToString() {
        SimpleType type = SimpleType.construct(String.class);
        assertEquals("[simple type, class java.lang.String]", type.toString());
    }

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        SimpleType type = SimpleType.construct(String.class);
        assertTrue(type.equals(type));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        SimpleType type = SimpleType.construct(String.class);
        assertFalse(type.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        SimpleType type = SimpleType.construct(String.class);
        assertFalse(type.equals(new Object()));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentRawClass() {
        SimpleType type1 = SimpleType.construct(String.class);
        SimpleType type2 = SimpleType.construct(Integer.class);
        assertFalse(type1.equals(type2));
    }

    @Test(timeout = 4000)
    public void testEqualsSameClassSameBindings() {
        SimpleType type1 = SimpleType.construct(String.class);
        SimpleType type2 = SimpleType.construct(String.class);
        assertTrue(type1.equals(type2));
    }

    @Test(timeout = 4000)
    public void testEqualsSameClassDifferentBindings() {
        TypeBindings bindings1 = TypeBindings.create(String.class, new JavaType[] { 
            TypeFactory.defaultInstance().constructType(Integer.class) 
        });
        TypeBindings bindings2 = TypeBindings.create(String.class, new JavaType[] { 
            TypeFactory.defaultInstance().constructType(Long.class) 
        });
        SimpleType type1 = new TestSimpleType(String.class, bindings1);
        SimpleType type2 = new TestSimpleType(String.class, bindings2);
        assertFalse(type1.equals(type2));
    }

    // ========== Partition B: Boundary Value Analysis (BVA) & Extremes ==========

    @Test(timeout = 4000)
    public void testConstructUnsafeNullClass() {
        // Should not throw NPE at construction, but may fail later
        try {
            SimpleType type = SimpleType.constructUnsafe(null);
            assertNotNull(type);
            // Accessing raw class may throw
            type.getRawClass();
            fail("Expected exception for null class");
        } catch (Exception e) {
            // Expected - either NPE or IllegalArgumentException
        }
    }

    @Test(timeout = 4000)
    public void testConstructWithMapClass() {
        try {
            SimpleType.construct(HashMap.class);
            fail("Expected IllegalArgumentException for Map class");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Map"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructWithCollectionClass() {
        try {
            SimpleType.construct(ArrayList.class);
            fail("Expected IllegalArgumentException for Collection class");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("Collection"));
        }
    }

    @Test(timeout = 4000)
    public void testConstructWithArrayClass() {
        try {
            SimpleType.construct(String[].class);
            fail("Expected IllegalArgumentException for array class");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("array"));
        }
    }

    @Test(timeout = 4000)
    public void testBuildCanonicalNameNoBindings() {
        SimpleType type = SimpleType.construct(String.class);
        assertEquals("java.lang.String", type.toCanonical());
    }

    @Test(timeout = 4000)
    public void testBuildCanonicalNameWithBindings() {
        TypeBindings bindings = TypeBindings.create(String.class, new JavaType[] { 
            TypeFactory.defaultInstance().constructType(Integer.class) 
        });
        SimpleType type = new TestSimpleType(String.class, bindings);
        assertEquals("java.lang.String<java.lang.Integer>", type.toCanonical());
    }

    @Test(timeout = 4000)
    public void testBuildCanonicalNameWithMultipleBindings() {
        TypeBindings bindings = TypeBindings.create(String.class, new JavaType[] { 
            TypeFactory.defaultInstance().constructType(Integer.class),
            TypeFactory.defaultInstance().constructType(Long.class)
        });
        SimpleType type = new TestSimpleType(String.class, bindings);
        assertEquals("java.lang.String<java.lang.Integer,java.lang.Long>", type.toCanonical());
    }

    @Test(timeout = 4000)
    public void testGetGenericSignatureWithMultipleBindings() {
        TypeBindings bindings = TypeBindings.create(String.class, new JavaType[] { 
            TypeFactory.defaultInstance().constructType(Integer.class),
            TypeFactory.defaultInstance().constructType(Long.class)
        });
        SimpleType type = new TestSimpleType(String.class, bindings);
        StringBuilder sb = new StringBuilder();
        type.getGenericSignature(sb);
        assertEquals("Ljava/lang/String<Ljava/lang/Integer;Ljava/lang/Long;>;", sb.toString());
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    @Test(timeout = 4000)
    public void testDefectConstructUnsafeWithNullBindingsAndCanonicalName() {
        // This directly targets the defect: constructUnsafe passes null bindings
        // which causes NPE when buildCanonicalName() is called
        SimpleType type = SimpleType.constructUnsafe(String.class);
        try {
            String canonical = type.toCanonical();
            // In the fixed version, this should work (empty bindings)
            assertEquals("java.lang.String", canonical);
        } catch (NullPointerException e) {
            fail("NPE thrown when building canonical name with null bindings - this is the defect!");
        }
    }

    @Test(timeout = 4000)
    public void testDefectConstructUnsafeWithNullBindingsAndGenericSignature() {
        // Another manifestation of the same defect
        SimpleType type = SimpleType.constructUnsafe(String.class);
        try {
            StringBuilder sb = new StringBuilder();
            type.getGenericSignature(sb);
            assertEquals("Ljava/lang/String;", sb.toString());
        } catch (NullPointerException e) {
            fail("NPE thrown when building generic signature with null bindings - this is the defect!");
        }
    }

    @Test(timeout = 4000)
    public void testDefectRefineReturnsNullCausesIssues() {
        // refine() returning null may cause downstream NPE in type resolution
        SimpleType type = SimpleType.construct(String.class);
        JavaType refined = type.refine(String.class, TypeBindings.emptyBindings(), null, null);
        assertNull("refine() should return null for SimpleType", refined);
        
        // In the defective scenario, this null may propagate and cause
        // UnrecognizedPropertyException when used in deserialization context
        // We simulate by checking that the type cannot be refined
        // (This is the root cause of the Objecid1083Test failure)
    }

    @Test(timeout = 4000)
    public void testDefectWithContentTypeThrows() {
        SimpleType type = SimpleType.construct(String.class);
        try {
            type.withContentType(TypeFactory.defaultInstance().constructType(Integer.class));
            fail("Expected IllegalArgumentException for withContentType");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("no content types"));
        }
    }

    @Test(timeout = 4000)
    public void testDefectWithContentTypeHandlerThrows() {
        SimpleType type = SimpleType.construct(String.class);
        try {
            type.withContentTypeHandler(new Object());
            fail("Expected IllegalArgumentException for withContentTypeHandler");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("no content types"));
        }
    }

    @Test(timeout = 4000)
    public void testDefectWithContentValueHandlerThrows() {
        SimpleType type = SimpleType.construct(String.class);
        try {
            type.withContentValueHandler(new Object());
            fail("Expected IllegalArgumentException for withContentValueHandler");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("no content types"));
        }
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000)
    public void testConstructWithNullClass() {
        try {
            SimpleType.construct(null);
            fail("Expected IllegalArgumentException for null class");
        } catch (IllegalArgumentException e) {
            // Expected - null is not a valid class
        }
    }

    @Test(timeout = 4000)
    public void testWithTypeHandlerNull() {
        SimpleType type = SimpleType.construct(String.class);
        SimpleType result = type.withTypeHandler(null);
        assertNotNull(result);
        assertNull(result.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testWithValueHandlerNull() {
        SimpleType type = SimpleType.construct(String.class);
        SimpleType result = type.withValueHandler(null);
        assertNotNull(result);
        assertNull(result.getValueHandler());
    }

    @Test(timeout = 4000)
    public void testEqualsWithDifferentBindingsNull() {
        SimpleType type1 = SimpleType.construct(String.class);
        SimpleType type2 = new TestSimpleType(String.class, null);
        // Should handle null bindings gracefully
        assertFalse(type1.equals(type2));
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        SimpleType type = SimpleType.construct(String.class);
        int hash1 = type.hashCode();
        int hash2 = type.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testEqualsConsistency() {
        SimpleType type1 = SimpleType.construct(String.class);
        SimpleType type2 = SimpleType.construct(String.class);
        assertTrue(type1.equals(type2));
        assertTrue(type2.equals(type1));
        assertEquals(type1.hashCode(), type2.hashCode());
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        SimpleType type = SimpleType.construct(String.class);
        // Simple types should be serializable
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(type);
        oos.close();
        
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        SimpleType deserialized = (SimpleType) ois.readObject();
        ois.close();
        
        assertEquals(type, deserialized);
        assertEquals(type.toCanonical(), deserialized.toCanonical());
    }

    @Test(timeout = 4000)
    public void testCopyConstructor() {
        SimpleType original = SimpleType.construct(String.class);
        SimpleType copy = new TestSimpleType((TypeBase) original);
        assertEquals(original, copy);
        assertEquals(original.toCanonical(), copy.toCanonical());
    }

    @Test(timeout = 4000)
    public void testGetErasedSignatureWithBindings() {
        TypeBindings bindings = TypeBindings.create(String.class, new JavaType[] { 
            TypeFactory.defaultInstance().constructType(Integer.class) 
        });
        SimpleType type = new TestSimpleType(String.class, bindings);
        StringBuilder sb = new StringBuilder();
        type.getErasedSignature(sb);
        // Erased signature ignores bindings
        assertEquals("Ljava/lang/String;", sb.toString());
    }

    @Test(timeout = 4000)
    public void testGetGenericSignatureWithEmptyBindings() {
        SimpleType type = SimpleType.construct(String.class);
        StringBuilder sb = new StringBuilder();
        type.getGenericSignature(sb);
        assertEquals("Ljava/lang/String;", sb.toString());
    }

    @Test(timeout = 4000)
    public void testBuildCanonicalNameWithEmptyBindings() {
        SimpleType type = SimpleType.construct(String.class);
        assertEquals("java.lang.String", type.toCanonical());
    }

    @Test(timeout = 4000)
    public void testWithStaticTypingPreservesBindings() {
        TypeBindings bindings = TypeBindings.create(String.class, new JavaType[] { 
            TypeFactory.defaultInstance().constructType(Integer.class) 
        });
        SimpleType type = new TestSimpleType(String.class, bindings);
        SimpleType staticType = type.withStaticTyping();
        assertEquals(type.toCanonical(), staticType.toCanonical());
        assertTrue(staticType.isStatic());
    }

    @Test(timeout = 4000)
    public void testWithTypeHandlerPreservesBindings() {
        TypeBindings bindings = TypeBindings.create(String.class, new JavaType[] { 
            TypeFactory.defaultInstance().constructType(Integer.class) 
        });
        SimpleType type = new TestSimpleType(String.class, bindings);
        Object handler = new Object();
        SimpleType withHandler = type.withTypeHandler(handler);
        assertEquals(type.toCanonical(), withHandler.toCanonical());
        assertSame(handler, withHandler.getTypeHandler());
    }

    @Test(timeout = 4000)
    public void testWithValueHandlerPreservesBindings() {
        TypeBindings bindings = TypeBindings.create(String.class, new JavaType[] { 
            TypeFactory.defaultInstance().constructType(Integer.class) 
        });
        SimpleType type = new TestSimpleType(String.class, bindings);
        Object handler = new Object();
        SimpleType withHandler = type.withValueHandler(handler);
        assertEquals(type.toCanonical(), withHandler.toCanonical());
        assertSame(handler, withHandler.getValueHandler());
    }

    @Test(timeout = 4000)
    public void testNarrowPreservesBindings() {
        TypeBindings bindings = TypeBindings.create(Object.class, new JavaType[] { 
            TypeFactory.defaultInstance().constructType(Integer.class) 
        });
        SimpleType type = new TestSimpleType(Object.class, bindings);
        JavaType narrowed = type._narrow(String.class);
        assertNotNull(narrowed);
        assertEquals(String.class, narrowed.getRawClass());
        // Bindings should be preserved
        assertEquals(type.toCanonical().replace("java.lang.Object", "java.lang.String"), 
                     narrowed.toCanonical());
    }

    @Test(timeout = 4000)
    public void testEqualsWithNullBindings() {
        SimpleType type1 = new TestSimpleType(String.class, null);
        SimpleType type2 = new TestSimpleType(String.class, null);
        // Both have null bindings - should be equal
        assertTrue(type1.equals(type2));
    }

    @Test(timeout = 4000)
    public void testEqualsWithOneNullBindings() {
        SimpleType type1 = SimpleType.construct(String.class); // empty bindings
        SimpleType type2 = new TestSimpleType(String.class, null); // null bindings
        assertFalse(type1.equals(type2));
    }
}