package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

import java.nio.ByteBuffer;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * /* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class : com.fasterxml.jackson.databind.deser.std.JdkDeserializers
 * Methods      : find(Class<?> rawType, String clsName), <init>()
 * ---------------------------------------------------------------------------------------------------------
 * Branch 1: _classNames.contains(clsName) == false
 *   - Condition: clsName is unknown, empty, or null -> Returns null.
 * Branch 2: _classNames.contains(clsName) == true
 *   - Sub-branch 2a: FromStringDeserializer.findDeserializer(rawType) != null -> Returns FromStringDeserializer.
 *   - Sub-branch 2b: rawType == UUID.class -> Returns UUIDDeserializer.
 *   - Sub-branch 2c: rawType == StackTraceElement.class -> Returns StackTraceElementDeserializer.
 *   - Sub-branch 2d: rawType == AtomicBoolean.class -> Returns AtomicBooleanDeserializer.
 *   - Sub-branch 2e: rawType == ByteBuffer.class -> Returns ByteBufferDeserializer.
 *   - Sub-branch 2f: Fall-through when clsName matches but rawType does not match expected type -> Returns null.
 *
 * Known Defect Target:
 *   - Defect: Jackson fails to deserialize Void / java.lang.Void scalars. JdkDeserializers must map
 *     java.lang.Void to NullifyingDeserializer.instance, but the defective version omits Void.class from
 *     _classNames and the find() dispatch chain, returning null and causing MismatchedInputException.
 * ---------------------------------------------------------------------------------------------------------
 */
public class JdkDeserializersGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorInstantiation() {
        JdkDeserializers deserializers = new JdkDeserializers();
        assertNotNull("JdkDeserializers instance should be constructible", deserializers);
    }

    @Test(timeout = 4000)
    public void testFindUUIDDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(UUID.class, UUID.class.getName());
        assertNotNull("Deserializer for UUID should not be null", deser);
        assertTrue("Expected instance of UUIDDeserializer", deser instanceof UUIDDeserializer);
    }

    @Test(timeout = 4000)
    public void testFindStackTraceElementDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(StackTraceElement.class, StackTraceElement.class.getName());
        assertNotNull("Deserializer for StackTraceElement should not be null", deser);
        assertTrue("Expected instance of StackTraceElementDeserializer", deser instanceof StackTraceElementDeserializer);
    }

    @Test(timeout = 4000)
    public void testFindAtomicBooleanDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(AtomicBoolean.class, AtomicBoolean.class.getName());
        assertNotNull("Deserializer for AtomicBoolean should not be null", deser);
        assertTrue("Expected instance of AtomicBooleanDeserializer", deser instanceof AtomicBooleanDeserializer);
    }

    @Test(timeout = 4000)
    public void testFindByteBufferDeserializer() {
        JsonDeserializer<?> deser = JdkDeserializers.find(ByteBuffer.class, ByteBuffer.class.getName());
        assertNotNull("Deserializer for ByteBuffer should not be null", deser);
        assertTrue("Expected instance of ByteBufferDeserializer", deser instanceof ByteBufferDeserializer);
    }

    @Test(timeout = 4000)
    public void testFindAllFromStringDeserializerTypes() {
        Class<?>[] types = FromStringDeserializer.types();
        for (Class<?> type : types) {
            JsonDeserializer<?> deser = JdkDeserializers.find(type, type.getName());
            assertNotNull("FromStringDeserializer type " + type.getName() + " should be resolved", deser);
            assertTrue("Expected instance of FromStringDeserializer for " + type.getName(),
                    deser instanceof FromStringDeserializer);
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testFindWithUnknownClassName() {
        JsonDeserializer<?> deser = JdkDeserializers.find(String.class, "com.example.NonExistentClass");
        assertNull("Unknown class name should return null", deser);
    }

    @Test(timeout = 4000)
    public void testFindWithStandardJdkClassNotManaged() {
        JsonDeserializer<?> deser = JdkDeserializers.find(Integer.class, Integer.class.getName());
        assertNull("Standard unmanaged JDK class should return null", deser);
    }

    @Test(timeout = 4000)
    public void testFindWithNullClassName() {
        JsonDeserializer<?> deser = JdkDeserializers.find(UUID.class, null);
        assertNull("Null class name should return null", deser);
    }

    @Test(timeout = 4000)
    public void testFindWithEmptyClassName() {
        JsonDeserializer<?> deser = JdkDeserializers.find(UUID.class, "");
        assertNull("Empty class name should return null", deser);
    }

    @Test(timeout = 4000)
    public void testFindWithNullRawTypeAndKnownClassName() {
        JsonDeserializer<?> deser = JdkDeserializers.find(null, UUID.class.getName());
        assertNull("Null rawType even with known class name should evaluate to null without NPE", deser);
    }

    @Test(timeout = 4000)
    public void testFindWithNullRawTypeAndNullClassName() {
        JsonDeserializer<?> deser = JdkDeserializers.find(null, null);
        assertNull("Both null arguments should return null", deser);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J testVoidDeser Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectVoidClassDeserializerMappedToNullifying() {
        // Direct target for Defects4J JDKScalarsTest::testVoidDeser defect
        // When Void.class is requested, JdkDeserializers must map it to NullifyingDeserializer.instance
        JsonDeserializer<?> deser = JdkDeserializers.find(Void.class, Void.class.getName());
        assertNotNull("JdkDeserializers must support java.lang.Void", deser);
        assertSame("java.lang.Void must resolve to NullifyingDeserializer.instance",
                NullifyingDeserializer.instance, deser);
    }

    // =========================================================================
    // Partition D: Decision/Condition Branch Coverage & Mismatched Fall-Through
    // =========================================================================

    @Test(timeout = 4000)
    public void testKnownClassNameWithMismatchedRawTypeForUUID() {
        // clsName is in _classNames, but rawType is mismatched (Object.class)
        JsonDeserializer<?> deser = JdkDeserializers.find(Object.class, UUID.class.getName());
        assertNull("Mismatched rawType for UUID class name should return null", deser);
    }

    @Test(timeout = 4000)
    public void testKnownClassNameWithMismatchedRawTypeForStackTraceElement() {
        // clsName is in _classNames, but rawType is mismatched (String.class)
        JsonDeserializer<?> deser = JdkDeserializers.find(String.class, StackTraceElement.class.getName());
        assertNull("Mismatched rawType for StackTraceElement class name should return null", deser);
    }

    @Test(timeout = 4000)
    public void testKnownClassNameWithMismatchedRawTypeForAtomicBoolean() {
        // clsName is in _classNames, but rawType is mismatched (Boolean.class)
        JsonDeserializer<?> deser = JdkDeserializers.find(Boolean.class, AtomicBoolean.class.getName());
        assertNull("Mismatched rawType for AtomicBoolean class name should return null", deser);
    }

    @Test(timeout = 4000)
    public void testKnownClassNameWithMismatchedRawTypeForByteBuffer() {
        // clsName is in _classNames, but rawType is mismatched (byte[].class)
        JsonDeserializer<?> deser = JdkDeserializers.find(byte[].class, ByteBuffer.class.getName());
        assertNull("Mismatched rawType for ByteBuffer class name should return null", deser);
    }

    @Test(timeout = 4000)
    public void testKnownClassNameWithCrossMatchedRawType() {
        // clsName matches AtomicBoolean, but rawType is StackTraceElement.class
        JsonDeserializer<?> deser = JdkDeserializers.find(StackTraceElement.class, AtomicBoolean.class.getName());
        assertNull("Cross-matched type and name should not resolve to a deserializer", deser);
    }
}