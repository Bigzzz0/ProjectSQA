package org.apache.commons.lang3;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

import org.junit.Test;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Targeted branches and boundary conditions:
 * 
 * - clone(T): null input -> returns null (branch B1)
 * - clone(T): normal serialization roundtrip (branch B2)
 * - clone(T): object that triggers IOException during serialization (branch B3)
 * - clone(T): object that triggers ClassNotFoundException during deserialization (branch B4, defect target)
 * 
 * - serialize(Serializable, OutputStream): null OutputStream -> IllegalArgumentException (branch B5)
 * - serialize(Serializable, OutputStream): normal path (branch B6)
 * - serialize(Serializable): normal path (branch B7)
 * 
 * - deserialize(InputStream): null InputStream -> IllegalArgumentException (branch B8)
 * - deserialize(InputStream): normal path (branch B9)
 * - deserialize(byte[]): null array -> IllegalArgumentException (branch B10)
 * - deserialize(byte[]): normal path (branch B11)
 * - deserialize(byte[]): invalid/corrupt data -> SerializationException (branch B12)
 * 
 * - Constructor: instantiate SerializationUtils (branch B13)
 * 
 * Defect targeted: testPrimitiveTypeClassSerialization
 *   - clone(int.class) fails with ClassNotFoundException when the custom ClassLoaderAwareObjectInputStream
 *     cannot resolve the primitive type class "int". The test asserts that the clone returns the same Class object,
 *     which fails on the defective version.
 */
public class SerializationUtilsDeepseekTest {

    // ------------------------------------------------------------------ //
    // Inner helper class for testing IOException during serialization   //
    // ------------------------------------------------------------------ //
    private static class WithNonSerializableField implements Serializable {
        private static final long serialVersionUID = 1L;
        @SuppressWarnings("unused")
        private final Object nonSerializable = new Object(); // java.lang.Object is not Serializable
    }

    // ------------------------------------------------------------------ //
    // Constructor coverage (B13)                                       //
    // ------------------------------------------------------------------ //
    @Test(timeout = 4000)
    public void testConstructor() {
        new SerializationUtils();
    }

    // ------------------------------------------------------------------ //
    // clone(T) – null input (B1)                                        //
    // ------------------------------------------------------------------ //
    @Test(timeout = 4000)
    public void testCloneNull() {
        assertNull("clone(null) should return null", SerializationUtils.clone(null));
    }

    // ------------------------------------------------------------------ //
    // clone(T) – normal roundtrip (B2)                                 //
    // ------------------------------------------------------------------ //
    @Test(timeout = 4000)
    public void testCloneString() {
        String original = "Hello, World!";
        String cloned = SerializationUtils.clone(original);
        assertEquals("Cloned string must be equal to original", original, cloned);
        assertNotSame("Cloned string must be a different object", original, cloned);
    }

    // ------------------------------------------------------------------ //
    // clone(T) – object that causes IOException during serialization (B3)//
    // ------------------------------------------------------------------ //
    @Test(timeout = 4000)
    public void testCloneObjectWithNonSerializableField() {
        WithNonSerializableField obj = new WithNonSerializableField();
        try {
            SerializationUtils.clone(obj);
            fail("Expected SerializationException due to NotSerializableException");
        } catch (SerializationException e) {
            // expected
            assertTrue("Cause should be IOException", e.getCause() instanceof java.io.IOException);
        }
    }

    // ------------------------------------------------------------------ //
    // clone(T) – object that triggers ClassNotFoundException (B4)        //
    // This test directly targets the known defect.                       //
    // On the defective version, cloning a primitive type class like      //
    // int.class will throw ClassNotFoundException.                       //
    // On the fixed version, it should succeed.                           //
    // ------------------------------------------------------------------ //
    @Test(timeout = 4000)
    public void testClonePrimitiveTypeClass() {
        Class<?> clazz = Integer.TYPE; // int.class
        Class<?> cloned = SerializationUtils.clone(clazz);
        assertSame("Cloned primitive type class should be the same as original", clazz, cloned);
    }

    // ------------------------------------------------------------------ //
    // serialize(Serializable, OutputStream) – null OutputStream (B5)     //
    // ------------------------------------------------------------------ //
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSerializeOutputStreamWithNullOutput() {
        SerializationUtils.serialize("test", null);
    }

    // ------------------------------------------------------------------ //
    // serialize(Serializable, OutputStream) – normal (B6)                //
    // ------------------------------------------------------------------ //
    @Test(timeout = 4000)
    public void testSerializeOutputStreamNormal() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SerializationUtils.serialize("Hello", baos);
        byte[] bytes = baos.toByteArray();
        assertTrue("Serialized bytes should not be empty", bytes.length > 0);
    }

    // ------------------------------------------------------------------ //
    // serialize(Serializable) – normal (B7)                              //
    // ------------------------------------------------------------------ //
    @Test(timeout = 4000)
    public void testSerializeToBytesNormal() {
        byte[] bytes = SerializationUtils.serialize("Hello");
        assertTrue("Serialized bytes should not be empty", bytes.length > 0);
    }

    // ------------------------------------------------------------------ //
    // deserialize(InputStream) – null InputStream (B8)                  //
    // ------------------------------------------------------------------ //
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDeserializeInputStreamNull() {
        SerializationUtils.deserialize((java.io.InputStream) null);
    }

    // ------------------------------------------------------------------ //
    // deserialize(InputStream) – normal (B9)                              //
    // ------------------------------------------------------------------ //
    @Test(timeout = 4000)
    public void testDeserializeInputStreamNormal() throws Exception {
        byte[] data = SerializationUtils.serialize(Integer.valueOf(42));
        java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(data);
        Object result = SerializationUtils.deserialize(bais);
        assertEquals("Deserialized object should be Integer(42)", Integer.valueOf(42), result);
    }

    // ------------------------------------------------------------------ //
    // deserialize(byte[]) – null array (B10)                             //
    // ------------------------------------------------------------------ //
    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDeserializeBytesNull() {
        SerializationUtils.deserialize((byte[]) null);
    }

    // ------------------------------------------------------------------ //
    // deserialize(byte[]) – normal (B11)                                 //
    // ------------------------------------------------------------------ //
    @Test(timeout = 4000)
    public void testDeserializeBytesNormal() {
        byte[] data = SerializationUtils.serialize("World");
        Object result = SerializationUtils.deserialize(data);
        assertEquals("Deserialized object should be 'World'", "World", result);
    }

    // ------------------------------------------------------------------ //
    // deserialize(byte[]) – invalid/corrupt data (IOException) (B12)     //
    // ------------------------------------------------------------------ //
    @Test(timeout = 4000, expected = SerializationException.class)
    public void testDeserializeInvalidBytes() {
        byte[] invalidData = { 0x00, 0x01, 0x02, 0x03 }; // not a valid serialized stream
        SerializationUtils.deserialize(invalidData);
    }
}