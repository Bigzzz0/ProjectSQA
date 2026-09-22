package com.fasterxml.jackson.databind.type;

import org.junit.Test;
import static org.junit.Assert.*;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.type.ReferenceType;

public class ReferenceTypeDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: ReferenceType (extends SimpleType) - referential type wrapper
     * 
     * Key branches to cover:
     * 1. construct() - static factory, always creates new instance
     * 2. withTypeHandler() - returns this if same handler, else new instance
     * 3. withContentTypeHandler() - compares with referenced type's handler
     * 4. withValueHandler() - returns this if same handler, else new instance
     * 5. withContentValueHandler() - compares with referenced type's value handler
     * 6. withStaticTyping() - returns this if already static, else new instance
     * 7. buildCanonicalName() - builds canonical name with '<' and referenced type
     * 8. _narrow() - creates new ReferenceType with subclass
     * 9. getReferencedType() - returns _referencedType
    10. isReferenceType() - always true
    11. containedTypeCount() - always 1
    12. containedType(int) - returns referenced type if index==0, else null
    13. containedTypeName(int) - returns "T" if index==0, else null
    14. getParameterSource() - returns _class
    15. getErasedSignature() - delegates to _classSignature
    16. getGenericSignature() - builds generic signature with referenced type
    17. toString() - builds string representation
    18. equals() - compares class and referenced type
    19. hashCode() - inherited from SimpleType (uses refType.hashCode())
    20. serialVersionUID - serialization support
     * 
     * Defect targeted (from Defects4J):
     * - getGenericSignature() produces wrong output for array types
     *   Expected: "...e<Ljava/lang/String;[>;]" (array closing bracket before semicolon)
     *   Actual:   "...e<Ljava/lang/String;[]>;" (array closing bracket after semicolon)
     *   Root cause: missing handling for array types in generic signature construction
     *   The bug is in getGenericSignature() which appends ';' after the referenced type's
     *   signature, but for array types the ';' should come before the ']' 
     *   (i.e., the array closing bracket should be part of the type signature)
     */

    // Helper to create a ReferenceType for testing
    private ReferenceType createReferenceType(Class<?> cls, JavaType refType) {
        return ReferenceType.construct(cls, refType, null, null);
    }

    // Helper to create a simple JavaType (e.g., String)
    private JavaType createSimpleType(Class<?> cls) {
        return TypeFactory.defaultInstance().constructType(cls);
    }

    // Helper to create an array type
    private JavaType createArrayType(Class<?> componentType) {
        return TypeFactory.defaultInstance().constructArrayType(componentType);
    }

    /* ========== Partition A: Core Functional Logic & State Transitions ========== */

    @Test(timeout = 4000)
    public void testConstructAndBasicGetters() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);

        assertNotNull("ReferenceType should not be null", refType);
        assertEquals("Referenced type should match", stringType, refType.getReferencedType());
        assertTrue("isReferenceType() should return true", refType.isReferenceType());
        assertEquals("Class should match", String.class, refType.getRawClass());
        assertEquals("Content type should be referenced type", stringType, refType.getContentType());
    }

    @Test(timeout = 4000)
    public void testWithTypeHandler_SameHandler_ReturnsThis() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);
        Object handler = new Object();

        ReferenceType result = refType.withTypeHandler(handler);
        assertNotSame("Should return new instance when handler differs", refType, result);
        assertEquals("Handler should be set", handler, result.getTypeHandler());

        // Now test with same handler
        ReferenceType result2 = result.withTypeHandler(handler);
        assertSame("Should return same instance when handler is same", result, result2);
    }

    @Test(timeout = 4000)
    public void testWithContentTypeHandler() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);
        Object handler = new Object();

        ReferenceType result = refType.withContentTypeHandler(handler);
        assertNotSame("Should return new instance when content handler differs", refType, result);
        assertEquals("Content type handler should be set", handler, result.getContentType().getTypeHandler());

        // Test with same handler
        ReferenceType result2 = result.withContentTypeHandler(handler);
        assertSame("Should return same instance when content handler is same", result, result2);
    }

    @Test(timeout = 4000)
    public void testWithValueHandler() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);
        Object handler = new Object();

        ReferenceType result = refType.withValueHandler(handler);
        assertNotSame("Should return new instance when value handler differs", refType, result);
        assertEquals("Value handler should be set", handler, result.getValueHandler());

        // Test with same handler
        ReferenceType result2 = result.withValueHandler(handler);
        assertSame("Should return same instance when value handler is same", result, result2);
    }

    @Test(timeout = 4000)
    public void testWithContentValueHandler() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);
        Object handler = new Object();

        ReferenceType result = refType.withContentValueHandler(handler);
        assertNotSame("Should return new instance when content value handler differs", refType, result);
        assertEquals("Content value handler should be set", handler, result.getContentType().getValueHandler());

        // Test with same handler
        ReferenceType result2 = result.withContentValueHandler(handler);
        assertSame("Should return same instance when content value handler is same", result, result2);
    }

    @Test(timeout = 4000)
    public void testWithStaticTyping() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);

        ReferenceType result = refType.withStaticTyping();
        assertNotSame("Should return new instance when not static", refType, result);
        assertTrue("Should be static typing", result.isStaticTyping());

        // Test when already static
        ReferenceType result2 = result.withStaticTyping();
        assertSame("Should return same instance when already static", result, result2);
    }

    /* ========== Partition B: Boundary Value Analysis & Extremes ========== */

    @Test(timeout = 4000)
    public void testContainedType_IndexBoundaries() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);

        assertEquals("Index 0 should return referenced type", stringType, refType.containedType(0));
        assertNull("Negative index should return null", refType.containedType(-1));
        assertNull("Index 1 should return null", refType.containedType(1));
        assertNull("Index MAX_VALUE should return null", refType.containedType(Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testContainedTypeName_IndexBoundaries() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);

        assertEquals("Index 0 should return 'T'", "T", refType.containedTypeName(0));
        assertNull("Negative index should return null", refType.containedTypeName(-1));
        assertNull("Index 1 should return null", refType.containedTypeName(1));
        assertNull("Index MAX_VALUE should return null", refType.containedTypeName(Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void testContainedTypeCount() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);

        assertEquals("Should always return 1", 1, refType.containedTypeCount());
    }

    @Test(timeout = 4000)
    public void testGetParameterSource() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);

        assertEquals("Parameter source should be the raw class", String.class, refType.getParameterSource());
    }

    @Test(timeout = 4000)
    public void testBuildCanonicalName() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);

        String canonical = refType.toCanonical();
        assertNotNull("Canonical name should not be null", canonical);
        assertTrue("Canonical name should contain class name", canonical.contains(String.class.getName()));
        assertTrue("Canonical name should contain '<'", canonical.contains("<"));
        assertTrue("Canonical name should contain referenced type canonical", canonical.contains(stringType.toCanonical()));
    }

    /* ========== Partition C: Defect-Targeted Branch Zone ========== */

    @Test(timeout = 4000)
    public void testGetGenericSignature_WithArrayType_DefectTarget() {
        // This test targets the specific defect from Defects4J
        // The bug is in getGenericSignature() for array types
        // Expected: "...e<Ljava/lang/String;[>;]" (array closing bracket before semicolon)
        // Actual:   "...e<Ljava/lang/String;[]>;" (array closing bracket after semicolon)
        
        JavaType arrayType = createArrayType(String.class);
        ReferenceType refType = createReferenceType(String.class, arrayType);

        StringBuilder sb = new StringBuilder();
        StringBuilder result = refType.getGenericSignature(sb);

        String signature = result.toString();
        assertNotNull("Signature should not be null", signature);
        
        // The defect causes the signature to have '[]' instead of '[>'
        // The correct format should have the array closing bracket before the semicolon
        // that terminates the reference type's generic signature
        assertFalse("Signature should not contain '[];' (defect pattern)", 
                signature.contains("[];"));
        assertTrue("Signature should contain '[>;' for array types", 
                signature.contains("[>;"));
        
        // More specific assertions
        int arrayBracketIndex = signature.indexOf('[');
        int semicolonIndex = signature.indexOf(';', arrayBracketIndex);
        assertTrue("Array bracket should be before semicolon", 
                arrayBracketIndex < semicolonIndex);
        
        // The correct signature should have the array closing bracket as part of the 
        // referenced type's signature, not as a separate element
        String expectedEnding = "[>;";
        assertTrue("Signature should end with '[>;' but was: " + signature, 
                signature.endsWith(expectedEnding));
    }

    @Test(timeout = 4000)
    public void testGetGenericSignature_WithSimpleType() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);

        StringBuilder sb = new StringBuilder();
        StringBuilder result = refType.getGenericSignature(sb);

        String signature = result.toString();
        assertNotNull("Signature should not be null", signature);
        assertTrue("Signature should contain class name", signature.contains(String.class.getName()));
        assertTrue("Signature should contain '<'", signature.contains("<"));
        assertTrue("Signature should contain ';'", signature.contains(";"));
    }

    @Test(timeout = 4000)
    public void testGetErasedSignature() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);

        StringBuilder sb = new StringBuilder();
        StringBuilder result = refType.getErasedSignature(sb);

        String signature = result.toString();
        assertNotNull("Erased signature should not be null", signature);
        assertTrue("Erased signature should contain class name", signature.contains(String.class.getName()));
    }

    /* ========== Partition D: Exception & Defensive Guard Paths ========== */

    @Test(timeout = 4000)
    public void testEquals_NullObject() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);

        assertFalse("Should not equal null", refType.equals(null));
    }

    @Test(timeout = 4000)
    public void testEquals_DifferentClass() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);

        Object other = new Object();
        assertFalse("Should not equal object of different class", refType.equals(other));
    }

    @Test(timeout = 4000)
    public void testEquals_SameReference() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);

        assertTrue("Should equal itself", refType.equals(refType));
    }

    @Test(timeout = 4000)
    public void testEquals_DifferentClassType() {
        JavaType stringType = createSimpleType(String.class);
        JavaType intType = createSimpleType(Integer.class);
        ReferenceType refType1 = createReferenceType(String.class, stringType);
        ReferenceType refType2 = createReferenceType(String.class, intType);

        assertFalse("Should not equal when referenced types differ", refType1.equals(refType2));
    }

    @Test(timeout = 4000)
    public void testEquals_EqualObjects() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType1 = createReferenceType(String.class, stringType);
        ReferenceType refType2 = createReferenceType(String.class, stringType);

        assertTrue("Should equal when same class and referenced type", refType1.equals(refType2));
    }

    /* ========== Partition E: Object Lifecycle & Contract Integrity ========== */

    @Test(timeout = 4000)
    public void testHashCode_Consistency() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);

        int hash1 = refType.hashCode();
        int hash2 = refType.hashCode();
        assertEquals("Hash code should be consistent", hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testHashCode_EqualObjects() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType1 = createReferenceType(String.class, stringType);
        ReferenceType refType2 = createReferenceType(String.class, stringType);

        assertEquals("Equal objects should have same hash code", 
                refType1.hashCode(), refType2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString_ContainsReferenceType() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);

        String str = refType.toString();
        assertNotNull("toString should not be null", str);
        assertTrue("toString should contain 'reference type'", str.contains("reference type"));
        assertTrue("toString should contain class name", str.contains(String.class.getName()));
        assertTrue("toString should contain referenced type", str.contains(stringType.toString()));
    }

    @Test(timeout = 4000)
    public void testNarrow() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(CharSequence.class, stringType);

        JavaType narrowed = refType.narrowBy(String.class);
        assertNotNull("Narrowed type should not be null", narrowed);
        assertTrue("Narrowed type should be ReferenceType", narrowed instanceof ReferenceType);
        assertEquals("Narrowed type should have subclass", String.class, narrowed.getRawClass());
        assertEquals("Referenced type should be preserved", stringType, narrowed.getReferencedType());
    }

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);

        // Test serialization round-trip
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
        oos.writeObject(refType);
        oos.close();

        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(baos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bais);
        ReferenceType deserialized = (ReferenceType) ois.readObject();
        ois.close();

        assertEquals("Serialized object should be equal", refType, deserialized);
        assertEquals("Serialized object should have same referenced type", 
                refType.getReferencedType(), deserialized.getReferencedType());
    }

    @Test(timeout = 4000)
    public void testWithStaticTyping_PropagatesToReferencedType() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType refType = createReferenceType(String.class, stringType);

        ReferenceType result = refType.withStaticTyping();
        assertTrue("Referenced type should also be static", 
                result.getReferencedType().isStaticTyping());
    }

    @Test(timeout = 4000)
    public void testGetGenericSignature_WithNestedReferenceType() {
        JavaType stringType = createSimpleType(String.class);
        ReferenceType innerRef = createReferenceType(String.class, stringType);
        ReferenceType outerRef = createReferenceType(String.class, innerRef);

        StringBuilder sb = new StringBuilder();
        StringBuilder result = outerRef.getGenericSignature(sb);

        String signature = result.toString();
        assertNotNull("Signature should not be null", signature);
        assertTrue("Signature should contain nested reference", signature.contains("reference"));
    }
}