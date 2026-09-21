package org.apache.commons.lang3;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Method Under Test      | Branch / Condition Targeted                       | Test Strategy
 * ----------------------------------------------------------------------------------------------------
 * SerializationUtils()   | Public constructor contract (JavaBean compatibility| Instantiation assertion
 * ----------------------------------------------------------------------------------------------------
 * clone(T)               | object == null                                    | Assert returns null
 * clone(T)               | object != null (Standard reference types)         | Deep clone comparison & identity check
 * clone(T)               | object contains non-serializable fields           | SerializationException (wrapped IOException)
 * clone(T)               | ClassNotFoundException during stream read         | SerializationException wrapped CNFE
 * clone(T) [DEFECT TARGET| object is primitive Class (e.g. int.class)        | Defect LANG-626: ClassLoaderAware-
 *                        |                                                   | ObjectInputStream fails on primitive
 *                        |                                                   | types ("int", "boolean", etc.)
 * ----------------------------------------------------------------------------------------------------
 * serialize(obj, stream) | outputStream == null                              | IllegalArgumentException guard
 * serialize(obj, stream) | obj == null, valid stream                         | Verify null serialized properly
 * serialize(obj, stream) | IOException during ObjectOutputStream write       | SerializationException wrapped IOException
 * serialize(obj, stream) | IOException during ObjectOutputStream close       | Swallowed silently (NOPMD verify)
 * ----------------------------------------------------------------------------------------------------
 * serialize(obj)         | Round-trip byte[] conversion                      | Assert byte[] length > 0
 * ----------------------------------------------------------------------------------------------------
 * deserialize(stream)    | inputStream == null                               | IllegalArgumentException guard
 * deserialize(stream)    | IOException during ObjectInputStream read         | SerializationException wrapped IOException
 * deserialize(stream)    | ClassNotFoundException during resolution          | SerializationException wrapped CNFE
 * deserialize(stream)    | IOException during ObjectInputStream close        | Swallowed silently (NOPMD verify)
 * ----------------------------------------------------------------------------------------------------
 * deserialize(byte[])    | objectData == null                                | IllegalArgumentException guard
 * deserialize(byte[])    | Corrupt / truncated byte[]                        | SerializationException wrapped IOException
 * ----------------------------------------------------------------------------------------------------
 * ClassLoaderAwareObject | Class resolved by custom ClassLoader              | Custom ClassLoader loads class
 * InputStream            | Class fails custom ClassLoader -> fallback to TCCL| Thread context ClassLoader resolution
 *                        | Class fails both ClassLoaders                     | ClassNotFoundException propagated
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;

public class SerializationUtilsGeminiTest {

    // Helper static serializable bean for testing deep cloning and stream manipulation
    static class TestBean implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String text;
        private final int number;

        public TestBean(String text, int number) {
            this.text = text;
            this.number = number;
        }

        public String getText() {
            return text;
        }

        public int getNumber() {
            return number;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TestBean testBean = (TestBean) o;
            if (number != testBean.number) return false;
            return text != null ? text.equals(testBean.text) : testBean.text == null;
        }

        @Override
        public int hashCode() {
            int result = text != null ? text.hashCode() : 0;
            result = 31 * result + number;
            return result;
        }
    }

    // Helper non-serializable class
    static class NonSerializableEntry {
        @SuppressWarnings("unused")
        private final String data = "unsupported";
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorPublicContract() {
        SerializationUtils utils = new SerializationUtils();
        assertNotNull("Public constructor must instantiate instance for JavaBean compliance", utils);
    }

    @Test(timeout = 4000)
    public void testCloneObjectNormal() {
        TestBean original = new TestBean("commons-lang", 42);
        TestBean cloned = SerializationUtils.clone(original);

        assertNotNull("Cloned object should not be null", cloned);
        assertNotSame("Cloned object must not be the exact same memory instance", original, cloned);
        assertEquals("Cloned object must be logically equal to the original", original, cloned);
        assertEquals("commons-lang", cloned.getText());
        assertEquals(42, cloned.getNumber());
    }

    @Test(timeout = 4000)
    public void testSerializeAndDeserializeByteArrayRoundTrip() {
        ArrayList<String> originalList = new ArrayList<String>();
        originalList.add("first");
        originalList.add("second");

        byte[] serializedData = SerializationUtils.serialize(originalList);
        assertNotNull("Serialized byte array must not be null", serializedData);
        assertTrue("Serialized byte array length must be greater than 0", serializedData.length > 0);

        @SuppressWarnings("unchecked")
        ArrayList<String> deserializedList = (ArrayList<String>) SerializationUtils.deserialize(serializedData);
        assertNotNull("Deserialized object must not be null", deserializedList);
        assertNotSame("Deserialized list must not be identical instance", originalList, deserializedList);
        assertEquals("Deserialized list content must equal original list", originalList, deserializedList);
    }

    @Test(timeout = 4000)
    public void testSerializeAndDeserializeStreamRoundTrip() {
        String originalMessage = "Defects4J Automated White-Box Test";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        SerializationUtils.serialize(originalMessage, baos);
        byte[] bytes = baos.toByteArray();
        assertTrue("Output stream should have captured serialized bytes", bytes.length > 0);

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Object deserialized = SerializationUtils.deserialize(bais);

        assertEquals("Deserialized stream data must equal original", originalMessage, deserialized);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCloneNullReturnsNull() {
        Serializable nullInput = null;
        Serializable result = SerializationUtils.clone(nullInput);
        assertNull("Cloning null should immediately return null without processing", result);
    }

    @Test(timeout = 4000)
    public void testSerializeNullObject() {
        byte[] bytes = SerializationUtils.serialize(null);
        assertNotNull("Serializing null should produce a valid stream representing null", bytes);

        Object result = SerializationUtils.deserialize(bytes);
        assertNull("Deserializing null-serialized byte array must yield null", result);
    }

    @Test(timeout = 4000)
    public void testSerializeNullObjectToStream() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SerializationUtils.serialize(null, baos);

        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        Object result = SerializationUtils.deserialize(bais);
        assertNull("Deserializing null-serialized stream must yield null", result);
    }

    @Test(timeout = 4000)
    public void testCloneEmptyDataStructures() {
        HashMap<String, String> emptyMap = new HashMap<String, String>();
        HashMap<String, String> clonedMap = SerializationUtils.clone(emptyMap);

        assertNotNull(clonedMap);
        assertNotSame(emptyMap, clonedMap);
        assertTrue("Cloned empty map should remain empty", clonedMap.isEmpty());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets known defect: org.apache.commons.lang3.SerializationUtilsTest::testPrimitiveTypeClassSerialization
     * In defective versions of SerializationUtils, ClassLoaderAwareObjectInputStream fails
     * with ClassNotFoundException when trying to resolve primitive type Class objects (e.g., int.class),
     * because Class.forName() does not resolve primitive type names ("int", "boolean", etc.).
     */
    @Test(timeout = 4000)
    public void testPrimitiveTypeClassSerialization() {
        Class<?>[] primitiveTypes = new Class<?>[] {
            byte.class,
            short.class,
            int.class,
            long.class,
            float.class,
            double.class,
            boolean.class,
            char.class,
            void.class
        };

        for (Class<?> primitiveType : primitiveTypes) {
            Class<?> cloned = SerializationUtils.clone(primitiveType);
            assertEquals("Cloned primitive Class<?> object must equal original primitive class",
                    primitiveType, cloned);
            assertSame("Cloned primitive Class<?> object must be the same singleton Class instance",
                    primitiveType, cloned);
        }
    }

    @Test(timeout = 4000)
    public void testPrimitiveTypeArrayClassSerialization() {
        Class<?>[] primitiveTypes = new Class<?>[] {
            int.class,
            boolean.class,
            double.class
        };

        Class<?>[] cloned = SerializationUtils.clone(primitiveTypes);
        assertNotNull("Cloned primitive class array must not be null", cloned);
        assertNotSame("Cloned array must be a new array instance", primitiveTypes, cloned);
        assertEquals("Array lengths must match", primitiveTypes.length, cloned.length);
        for (int i = 0; i < primitiveTypes.length; i++) {
            assertSame("Elements within cloned primitive class array must match", primitiveTypes[i], cloned[i]);
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializeNullOutputStreamThrowsIllegalArgumentException() {
        try {
            SerializationUtils.serialize("ValidObject", null);
            fail("Expected IllegalArgumentException when outputStream is null");
        } catch (IllegalArgumentException ex) {
            assertEquals("The OutputStream must not be null", ex.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeNullInputStreamThrowsIllegalArgumentException() {
        try {
            SerializationUtils.deserialize((InputStream) null);
            fail("Expected IllegalArgumentException when inputStream is null");
        } catch (IllegalArgumentException ex) {
            assertEquals("The InputStream must not be null", ex.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeNullByteArrayThrowsIllegalArgumentException() {
        try {
            SerializationUtils.deserialize((byte[]) null);
            fail("Expected IllegalArgumentException when byte[] is null");
        } catch (IllegalArgumentException ex) {
            assertEquals("The byte[] must not be null", ex.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSerializeOutputStreamIOExceptionThrowsSerializationException() {
        OutputStream failingOut = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("Simulated network/disk write failure");
            }
        };

        try {
            SerializationUtils.serialize("TestData", failingOut);
            fail("Expected SerializationException wrapping write IOException");
        } catch (SerializationException ex) {
            assertNotNull(ex.getCause());
            assertTrue("Cause should be IOException", ex.getCause() instanceof IOException);
            assertEquals("Simulated network/disk write failure", ex.getCause().getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSerializeIgnoresOutputStreamCloseException() {
        final boolean[] streamClosed = new boolean[] { false };
        OutputStream closeFailingOut = new ByteArrayOutputStream() {
            @Override
            public void close() throws IOException {
                streamClosed[0] = true;
                throw new IOException("Simulated close failure");
            }
        };

        // Must succeed without throwing SerializationException even if close() fails
        SerializationUtils.serialize("ValidString", closeFailingOut);
        assertTrue("Stream close method should have been invoked", streamClosed[0]);
    }

    @Test(timeout = 4000)
    public void testDeserializeInputStreamIOExceptionThrowsSerializationException() {
        InputStream failingIn = new InputStream() {
            @Override
            public int read() throws IOException {
                throw new IOException("Simulated network/disk read failure");
            }
        };

        try {
            SerializationUtils.deserialize(failingIn);
            fail("Expected SerializationException wrapping read IOException");
        } catch (SerializationException ex) {
            assertNotNull(ex.getCause());
            assertTrue("Cause should be IOException", ex.getCause() instanceof IOException);
            assertEquals("Simulated network/disk read failure", ex.getCause().getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeIgnoresInputStreamCloseException() {
        byte[] validBytes = SerializationUtils.serialize("TestCloseIgnored");
        final boolean[] streamClosed = new boolean[] { false };

        InputStream closeFailingIn = new ByteArrayInputStream(validBytes) {
            @Override
            public void close() throws IOException {
                streamClosed[0] = true;
                throw new IOException("Simulated close failure");
            }
        };

        Object result = SerializationUtils.deserialize(closeFailingIn);
        assertEquals("TestCloseIgnored", result);
        assertTrue("Stream close method should have been invoked", streamClosed[0]);
    }

    @Test(timeout = 4000)
    public void testDeserializeCorruptStreamThrowsSerializationException() {
        byte[] invalidData = new byte[] { (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF };
        try {
            SerializationUtils.deserialize(invalidData);
            fail("Expected SerializationException for corrupted stream header");
        } catch (SerializationException ex) {
            assertNotNull(ex.getCause());
            assertTrue("Cause should be IOException", ex.getCause() instanceof IOException);
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeEmptyByteArrayThrowsSerializationException() {
        try {
            SerializationUtils.deserialize(new byte[0]);
            fail("Expected SerializationException for empty byte array");
        } catch (SerializationException ex) {
            assertNotNull(ex.getCause());
            assertTrue("Cause should be IOException", ex.getCause() instanceof IOException);
        }
    }

    @Test(timeout = 4000)
    public void testCloneNonSerializableObjectGraphThrowsSerializationException() {
        HashMap<String, Object> mapWithUnserializable = new HashMap<String, Object>();
        mapWithUnserializable.put("key", new NonSerializableEntry());

        try {
            SerializationUtils.clone(mapWithUnserializable);
            fail("Expected SerializationException when cloning graph with non-serializable object");
        } catch (SerializationException ex) {
            assertNotNull(ex.getCause());
            assertTrue("Cause should be IOException (NotSerializableException)", ex.getCause() instanceof IOException);
        }
    }

    @Test(timeout = 4000)
    public void testDeserializeClassNotFoundThrowsSerializationException() {
        byte[] validData = SerializationUtils.serialize(new TestBean("payload", 999));
        // Mutate the class name in the serialized stream to a non-existent class
        byte[] origName = "TestBean".getBytes(StandardCharsets.UTF_8);
        byte[] fakeName = "FakeBean".getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i <= validData.length - origName.length; i++) {
            boolean matches = true;
            for (int j = 0; j < origName.length; j++) {
                if (validData[i + j] != origName[j]) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                System.arraycopy(fakeName, 0, validData, i, fakeName.length);
                break;
            }
        }

        try {
            SerializationUtils.deserialize(validData);
            fail("Expected SerializationException wrapping ClassNotFoundException");
        } catch (SerializationException ex) {
            assertNotNull(ex.getCause());
            assertTrue("Cause must be ClassNotFoundException", ex.getCause() instanceof ClassNotFoundException);
        }
    }

    // =========================================================================
    // Partition E: ClassLoaderAwareObjectInputStream Resolution Logic
    // =========================================================================

    @Test(timeout = 4000)
    public void testClassLoaderAwareObjectInputStreamWithContextFallback() throws Exception {
        TestBean sample = new TestBean("ClassLoaderFallback", 100);
        byte[] sampleBytes = SerializationUtils.serialize(sample);

        // ClassLoader that delegates to null bootstrap, failing on application classes
        ClassLoader isolatedLoader = new ClassLoader(null) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                throw new ClassNotFoundException(name);
            }
        };

        ByteArrayInputStream bais = new ByteArrayInputStream(sampleBytes);
        SerializationUtils.ClassLoaderAwareObjectInputStream in =
                new SerializationUtils.ClassLoaderAwareObjectInputStream(bais, isolatedLoader);

        try {
            // resolveClass should fail with isolatedLoader and fallback to Thread.currentThread().getContextClassLoader()
            Object resolved = in.readObject();
            assertEquals("Should resolve successfully through Thread Context ClassLoader fallback", sample, resolved);
        } finally {
            in.close();
        }
    }

    @Test(timeout = 4000)
    public void testClassLoaderAwareObjectInputStreamFailsBothLoaders() throws Exception {
        byte[] validData = SerializationUtils.serialize(new TestBean("BothFail", 101));
        byte[] origName = "TestBean".getBytes(StandardCharsets.UTF_8);
        byte[] fakeName = "FakeBean".getBytes(StandardCharsets.UTF_8);

        for (int i = 0; i <= validData.length - origName.length; i++) {
            boolean matches = true;
            for (int j = 0; j < origName.length; j++) {
                if (validData[i + j] != origName[j]) {
                    matches = false;
                    break;
                }
            }
            if (matches) {
                System.arraycopy(fakeName, 0, validData, i, fakeName.length);
                break;
            }
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(validData);
        ClassLoader isolatedLoader = new ClassLoader(null) {
            @Override
            protected Class<?> findClass(String name) throws ClassNotFoundException {
                throw new ClassNotFoundException(name);
            }
        };

        SerializationUtils.ClassLoaderAwareObjectInputStream in =
                new SerializationUtils.ClassLoaderAwareObjectInputStream(bais, isolatedLoader);

        try {
            in.readObject();
            fail("Expected ClassNotFoundException when neither classloader can resolve class");
        } catch (ClassNotFoundException expected) {
            // Success: ClassNotFoundException thrown when both loaders fail
        } finally {
            in.close();
        }
    }
}