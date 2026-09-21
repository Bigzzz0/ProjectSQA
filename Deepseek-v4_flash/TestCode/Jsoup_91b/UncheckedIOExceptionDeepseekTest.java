package org.jsoup;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test suite for UncheckedIOException.
 * 
 * [Branch & Defect Analysis Matrix]
 * - Constructor: accepts IOException cause (null allowed)
 *   - Branch: cause == null -> super(null) -> getCause() returns null
 *   - Branch: cause != null -> super(cause) -> getCause() returns cause
 * - ioException(): returns (IOException) getCause()
 *   - Branch: getCause() == null -> returns null
 *   - Branch: getCause() instanceof IOException -> returns casted cause
 *   - (No ClassCastException because constructor ensures type safety)
 * 
 * Defect-targeted scenario: Integration tests (e.g., testBinaryThrowsException)
 * expect that when an IOException is wrapped, ioException() returns the exact
 * same IOException instance. If the cause is lost or incorrectly stored, the
 * assertion fails. We test this directly.
 */
public class UncheckedIOExceptionDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testConstructorWithNonNullCause() {
        IOException original = new IOException("test");
        UncheckedIOException ex = new UncheckedIOException(original);
        assertNotNull("Exception should not be null", ex);
        assertTrue("Should be RuntimeException", ex instanceof RuntimeException);
        assertNull("Message should be null (no message provided)", ex.getMessage());
        assertSame("Cause should be the original IOException", original, ex.getCause());
    }

    @Test(timeout = 4000)
    public void testIoExceptionReturnsCause() {
        IOException original = new IOException("data");
        UncheckedIOException ex = new UncheckedIOException(original);
        IOException retrieved = ex.ioException();
        assertSame("ioException() should return the exact cause", original, retrieved);
    }

    @Test(timeout = 4000)
    public void testExceptionCanBeThrownAndCaught() {
        IOException original = new IOException("thrown");
        UncheckedIOException ex = new UncheckedIOException(original);
        try {
            throw ex;
        } catch (UncheckedIOException e) {
            assertSame("Caught exception should be the same instance", ex, e);
            assertSame("Cause should be preserved", original, e.ioException());
        }
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testConstructorWithNullCause() {
        UncheckedIOException ex = new UncheckedIOException(null);
        assertNotNull("Exception should not be null", ex);
        assertNull("Cause should be null", ex.getCause());
        assertNull("ioException() should return null when cause is null", ex.ioException());
    }

    @Test(timeout = 4000)
    public void testIoExceptionWithNullCause() {
        UncheckedIOException ex = new UncheckedIOException(null);
        IOException result = ex.ioException();
        assertNull("ioException() should return null", result);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone ====================

    /**
     * Directly targets the defect scenario from integration tests:
     * - Create an IOException, wrap it in UncheckedIOException.
     * - Verify that ioException() returns the exact same IOException instance.
     * - If the cause is lost (e.g., due to incorrect super() call or missing
     *   cause storage), this test will fail, revealing the defect.
     */
    @Test(timeout = 4000)
    public void testDefectTargetedCausePreservation() {
        IOException cause = new IOException("binary content error");
        UncheckedIOException uio = new UncheckedIOException(cause);
        // The integration tests expect that after catching UncheckedIOException,
        // calling ioException() yields the original IOException.
        IOException retrieved = uio.ioException();
        assertSame("Defect: ioException() must return the exact cause instance", cause, retrieved);
        // Also verify that the cause is not null (defect might set cause to null)
        assertNotNull("Defect: cause should not be null", retrieved);
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000)
    public void testNoMessageConstructor() {
        IOException cause = new IOException("msg");
        UncheckedIOException ex = new UncheckedIOException(cause);
        assertNull("Message should be null because no message argument was provided", ex.getMessage());
    }

    @Test(timeout = 4000)
    public void testCauseIsIOException() {
        IOException cause = new IOException();
        UncheckedIOException ex = new UncheckedIOException(cause);
        assertTrue("Cause must be an IOException", ex.getCause() instanceof IOException);
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testToStringContainsCause() {
        IOException cause = new IOException("root");
        UncheckedIOException ex = new UncheckedIOException(cause);
        String toString = ex.toString();
        assertTrue("toString should contain the cause class name", toString.contains("IOException"));
        assertTrue("toString should contain the cause message", toString.contains("root"));
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeNotOverridden() {
        // Since UncheckedIOException does not override equals/hashCode,
        // identity-based comparison is used.
        IOException cause = new IOException("a");
        UncheckedIOException ex1 = new UncheckedIOException(cause);
        UncheckedIOException ex2 = new UncheckedIOException(cause);
        assertFalse("Two different instances should not be equal", ex1.equals(ex2));
        assertNotEquals("Hash codes of different instances may differ", ex1.hashCode(), ex2.hashCode());
    }

    @Test(timeout = 4000)
    public void testSerializationRoundTrip() throws Exception {
        // UncheckedIOException extends RuntimeException which is serializable.
        IOException cause = new IOException("serial");
        UncheckedIOException original = new UncheckedIOException(cause);
        // Serialize to byte array
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(original);
        oos.close();
        // Deserialize
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        UncheckedIOException deserialized = (UncheckedIOException) ois.readObject();
        ois.close();
        // Verify cause is preserved
        assertNotNull("Deserialized exception should have a cause", deserialized.getCause());
        assertTrue("Cause should be IOException", deserialized.getCause() instanceof IOException);
        assertEquals("Cause message should be preserved", "serial", deserialized.getCause().getMessage());
        // ioException() should work
        IOException retrieved = deserialized.ioException();
        assertNotNull("ioException() should not return null after deserialization", retrieved);
        assertEquals("ioException() message should match", "serial", retrieved.getMessage());
    }
}