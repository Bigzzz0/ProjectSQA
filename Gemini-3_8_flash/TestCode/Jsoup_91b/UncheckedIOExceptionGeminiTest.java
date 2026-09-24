package org.jsoup;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.SocketTimeoutException;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.jsoup.UncheckedIOException
 *
 * ---------------------------------------------------------------------------------------------------------
 * Partition / Branch Target            | Input Condition                 | Expected Outcome
 * ---------------------------------------------------------------------------------------------------------
 * Partition A: Core Functional Logic   | Valid IOException instance      | getCause() == ioException() == cause
 *                                      | Custom IOException message      | Message propagates to wrapper
 * ---------------------------------------------------------------------------------------------------------
 * Partition B: Boundary Value Analysis | cause = null                    | getCause() == null, ioException() == null
 *                                      | cause with empty message ""     | ioException().getMessage().equals("")
 *                                      | cause with null message         | ioException().getMessage() == null
 * ---------------------------------------------------------------------------------------------------------
 * Partition C: Defect-Targeted Zone    | Constructor (String) presence   | Exposes defect where (String) constructor
 * (Ground truth: ConnectTest /         | Constructor (String) with msg   | was missing for binary data handling
 *  ParseTest binary throws failure)    |                                 | (e.g., "Input is binary and unsupported")
 * ---------------------------------------------------------------------------------------------------------
 * Partition D: Exception Guard Paths   | Subtypes (FileNotFoundException)| Preserved exact subtype in ioException()
 *                                      | Subtypes (SocketTimeoutException| Preserved exact subtype in ioException()
 *                                      | Subtypes (EOFException)         | Preserved exact subtype in ioException()
 *                                      | Nested causes in IOException    | Root cause chain intact
 * ---------------------------------------------------------------------------------------------------------
 * Partition E: Lifecycle & Contracts   | Serializable round-trip         | Cause and state reconstructed correctly
 *                                      | RuntimeException hierarchy      | instanceof RuntimeException == true
 * ---------------------------------------------------------------------------------------------------------
 */
public class UncheckedIOExceptionGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardIOExceptionWrapping() {
        IOException cause = new IOException("Disk read error");
        UncheckedIOException unchecked = new UncheckedIOException(cause);

        assertSame("ioException() must return the exact cause passed to the constructor",
                cause, unchecked.ioException());
        assertSame("getCause() must match ioException()",
                cause, unchecked.getCause());
        assertNotNull("getMessage() should not be null", unchecked.getMessage());
        assertTrue("getMessage() must contain the underlying error message",
                unchecked.getMessage().contains("Disk read error"));
    }

    @Test(timeout = 4000)
    public void testIOExceptionGetterReturnsCorrectInstance() {
        IOException originalIoException = new IOException("Network reset");
        UncheckedIOException unchecked = new UncheckedIOException(originalIoException);

        IOException retrieved = unchecked.ioException();
        assertNotNull("Retrieved IOException should never be null when initialized with one", retrieved);
        assertEquals("Retrieved IOException should match the original message",
                "Network reset", retrieved.getMessage());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorWithNullCause() {
        UncheckedIOException unchecked = new UncheckedIOException((IOException) null);

        assertNull("getCause() must be null when null is supplied", unchecked.getCause());
        assertNull("ioException() must return null when cause is null", unchecked.ioException());
        assertNull("getMessage() should be null when initialized with null cause and no message",
                unchecked.getMessage());
    }

    @Test(timeout = 4000)
    public void testConstructorWithEmptyMessageCause() {
        IOException emptyMsgCause = new IOException("");
        UncheckedIOException unchecked = new UncheckedIOException(emptyMsgCause);

        assertNotNull("ioException() must not be null", unchecked.ioException());
        assertEquals("Cause message should be empty string", "", unchecked.ioException().getMessage());
    }

    @Test(timeout = 4000)
    public void testConstructorWithNullMessageCause() {
        IOException nullMsgCause = new IOException((String) null);
        UncheckedIOException unchecked = new UncheckedIOException(nullMsgCause);

        assertNotNull("ioException() must not be null", unchecked.ioException());
        assertNull("Cause message should be null", unchecked.ioException().getMessage());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // Defects4J Ground Truth: ConnectTest & ParseTest failed when binary input
    // handling required throwing UncheckedIOException with a String message:
    // "Input is binary and unsupported".
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectTargetStringMessageConstructorPresenceAndContract() throws Throwable {
        Constructor<UncheckedIOException> stringConstructor;
        try {
            stringConstructor = UncheckedIOException.class.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            fail("Defect Detected: UncheckedIOException must provide a constructor accepting a String message "
                    + "for binary stream validation (e.g. 'Input is binary and unsupported').");
            return;
        }

        try {
            String errorMsg = "Input is binary and unsupported";
            UncheckedIOException instance = stringConstructor.newInstance(errorMsg);

            assertNotNull("Constructed instance must not be null", instance);
            assertNotNull("Underlying ioException() must not be null when created from String",
                    instance.ioException());
            assertEquals("Underlying IOException must retain the string message",
                    errorMsg, instance.ioException().getMessage());
            assertNotNull("Wrapper getMessage() must be populated", instance.getMessage());
            assertTrue("Wrapper getMessage() must contain the error string",
                    instance.getMessage().contains(errorMsg));
        } catch (InvocationTargetException ite) {
            throw ite.getCause();
        }
    }

    @Test(timeout = 4000)
    public void testDefectTargetStringMessageConstructorEdgeCases() throws Throwable {
        Constructor<UncheckedIOException> stringConstructor;
        try {
            stringConstructor = UncheckedIOException.class.getConstructor(String.class);
        } catch (NoSuchMethodException e) {
            fail("Defect Detected: UncheckedIOException(String) constructor is missing.");
            return;
        }

        try {
            // Test with empty string
            UncheckedIOException emptyInstance = stringConstructor.newInstance("");
            assertNotNull(emptyInstance.ioException());
            assertEquals("", emptyInstance.ioException().getMessage());

            // Test with null string
            UncheckedIOException nullInstance = stringConstructor.newInstance((String) null);
            assertNotNull(nullInstance.ioException());
            assertNull(nullInstance.ioException().getMessage());
        } catch (InvocationTargetException ite) {
            throw ite.getCause();
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testSubclassesOfIOExceptionPreserved() {
        FileNotFoundException fnf = new FileNotFoundException("file.txt not found");
        UncheckedIOException uncheckedFnf = new UncheckedIOException(fnf);
        assertTrue("ioException() must preserve FileNotFoundException",
                uncheckedFnf.ioException() instanceof FileNotFoundException);
        assertSame(fnf, uncheckedFnf.ioException());

        SocketTimeoutException timeout = new SocketTimeoutException("Read timed out");
        UncheckedIOException uncheckedTimeout = new UncheckedIOException(timeout);
        assertTrue("ioException() must preserve SocketTimeoutException",
                uncheckedTimeout.ioException() instanceof SocketTimeout