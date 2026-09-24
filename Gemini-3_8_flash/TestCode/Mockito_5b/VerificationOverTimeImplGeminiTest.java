package org.mockito.internal.verification;

import org.junit.Test;
import static org.junit.Assert.*;

import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.internal.util.Timer;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.verification.VerificationMode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.mockito.internal.verification.VerificationOverTimeImpl
 *
 * 1. Branch Analysis:
 *   - verify(VerificationData):
 *       * timer.isCounting() -> true (loop executes) vs false (loop skips).
 *       * delegate.verify(data) -> success vs MockitoAssertionError vs generic AssertionError vs RuntimeException.
 *       * returnOnSuccess -> true (immediate return upon first success) vs false (wait full timer duration).
 *       * final error check -> error != null (throws error) vs error == null (normal completion).
 *   - handleVerifyException(AssertionError):
 *       * canRecoverFromFailure(delegate) -> true (sleep & return error to retry) vs false (re-throw immediately).
 *   - canRecoverFromFailure(VerificationMode):
 *       * delegate is AtMost -> returns false.
 *       * delegate is NoMoreInteractions -> returns false.
 *       * delegate is other VerificationMode (e.g. Times, custom) -> returns true.
 *       * delegate is null -> returns true.
 *   - sleep(long):
 *       * Normal sleep completion vs InterruptedException handling.
 *
 * 2. Defects4J Ground Truth Defect:
 *   - Defect: pure_mockito_should_not_depend_JUnit
 *       VerificationOverTimeImpl historically had a direct dependency on JUnit through catching
 *       `org.mockito.exceptions.verification.junit.ArgumentsAreDifferent` instead of standard `AssertionError`.
 *       This introduced a direct bytecode dependency on JUnit in pure Mockito environments and prevented
 *       recovering from generic AssertionErrors.
 *   - Targeted Tests:
 *       * pure_mockito_should_not_depend_JUnit: Verifies bytecode does not contain any JUnit references.
 *       * testVerifyRecoversFromGenericAssertionError: Verifies that generic AssertionError is handled and retried.
 */
public class VerificationOverTimeImplGeminiTest {

    // =========================================================================
    // Helpers & Test Doubles
    // =========================================================================

    private static class FakeTimer extends Timer {
        private int remainingTicks;

        FakeTimer(int ticks) {
            super(0);
            this.remainingTicks = ticks;
        }

        @Override
        public void start() {
            // No-op for deterministic control
        }

        @Override
        public boolean isCounting() {
            return remainingTicks-- > 0;
        }
    }

    private static class CountingVerificationMode implements VerificationMode {
        int invocations = 0;
        private final int failTimes;
        private final AssertionError errorToThrow;

        CountingVerificationMode(int failTimes) {
            this(failTimes, new MockitoAssertionError("Delegate verification failed"));
        }

        CountingVerificationMode(int failTimes, AssertionError errorToThrow) {
            this.failTimes = failTimes;
            this.errorToThrow = errorToThrow;
        }

        @Override
        public void verify(VerificationData data) {
            invocations++;
            if (invocations <= failTimes) {
                throw errorToThrow;
            }
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testVerifyImmediateSuccessWithReturnOnSuccessTrue() {
        FakeTimer timer = new FakeTimer(5);
        CountingVerificationMode mode = new CountingVerificationMode(0);
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(0, 100, mode, true, timer);

        overTime.verify(null);

        assertEquals(1, mode.invocations);
    }

    @Test(timeout = 4000)
    public void testVerifyRecoversAfterFailureWithReturnOnSuccessTrue() {
        FakeTimer timer = new FakeTimer(5);
        CountingVerificationMode mode = new CountingVerificationMode(2);
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(0, 100, mode, true, timer);

        overTime.verify(null);

        assertEquals(3, mode.invocations);
    }

    @Test(timeout = 4000)
    public void testVerifyFailsWhenTimerRunsOut() {
        FakeTimer timer = new FakeTimer(3);
        CountingVerificationMode mode = new CountingVerificationMode(10);
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(0, 100, mode, true, timer);

        try {
            overTime.verify(null);
            fail("Expected MockitoAssertionError when duration expires without success");
        } catch (MockitoAssertionError e) {
            assertEquals("Delegate verification failed", e.getMessage());
            assertEquals(3, mode.invocations);
        }
    }

    @Test(timeout = 4000)
    public void testVerifyReturnOnSuccessFalseWaitsFullDurationWhenSuccessful() {
        FakeTimer timer = new FakeTimer(4);
        CountingVerificationMode mode = new CountingVerificationMode(0);
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(0, 100, mode, false, timer);

        overTime.verify(null);

        assertEquals(4, mode.invocations);
    }

    @Test(timeout = 4000)
    public void testVerifyReturnOnSuccessFalseRecoversIfEarlierIterationFailed() {
        FakeTimer timer = new FakeTimer(3);
        CountingVerificationMode mode = new CountingVerificationMode(1);
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(0, 100, mode, false, timer);

        overTime.verify(null);

        assertEquals(3, mode.invocations);
    }

    @Test(timeout = 4000)
    public void testVerifyReturnOnSuccessFalseThrowsIfFinalIterationFailed() {
        FakeTimer timer = new FakeTimer(2);
        VerificationMode mode = new VerificationMode() {
            private int callCount = 0;

            @Override
            public void verify(VerificationData data) {
                callCount++;
                if (callCount == 2) {
                    throw new MockitoAssertionError("Failed on last tick");
                }
            }
        };
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(0, 100, mode, false, timer);

        try {
            overTime.verify(null);
            fail("Expected MockitoAssertionError when last iteration fails");
        } catch (MockitoAssertionError e) {
            assertEquals("Failed on last tick", e.getMessage());
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testTimerInitiallyNotCountingExecutesZeroIterations() {
        FakeTimer timer = new FakeTimer(0);
        CountingVerificationMode mode = new CountingVerificationMode(0);
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(0, 100, mode, true, timer);

        overTime.verify(null);

        assertEquals(0, mode.invocations);
    }

    @Test(timeout = 4000)
    public void testFourArgConstructorInitializesStateProperly() {
        CountingVerificationMode mode = new CountingVerificationMode(0);
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(10L, 200L, mode, true);

        assertEquals(10L, overTime.getPollingPeriod());
        assertEquals(200L, overTime.getDuration());
        assertSame(mode, overTime.getDelegate());
    }

    @Test(timeout = 4000)
    public void testFiveArgConstructorInitializesStateProperly() {
        CountingVerificationMode mode = new CountingVerificationMode(0);
        Timer customTimer = new Timer(50L);
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(15L, 50L, mode, false, customTimer);

        assertEquals(15L, overTime.getPollingPeriod());
        assertEquals(50L, overTime.getDuration());
        assertSame(mode, overTime.getDelegate());
    }

    @Test(timeout = 4000)
    public void testRealTimerWithImmediateSuccess() {
        CountingVerificationMode mode = new CountingVerificationMode(0);
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(1L, 100L, mode, true);

        overTime.verify(null);

        assertEquals(1, mode.invocations);
    }

    @Test(timeout = 4000)
    public void testRealTimerWithEventualFailure() {
        CountingVerificationMode mode = new CountingVerificationMode(1000);
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(1L, 10L, mode, true);

        try {
            overTime.verify(null);
            fail("Expected MockitoAssertionError on timeout");
        } catch (MockitoAssertionError e) {
            assertTrue(mode.invocations >= 1);
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Ground Truth Defect: VerificationOverTimeImpl must not depend on JUnit.
     * Checks class constant pool / bytecode to ensure no references to "junit".
     */
    @Test(timeout = 4000)
    public void pure_mockito_should_not_depend_JUnit() throws IOException {
        String resourcePath = VerificationOverTimeImpl.class.getName().replace('.', '/') + ".class";
        InputStream is = VerificationOverTimeImpl.class.getClassLoader().getResourceAsStream(resourcePath);
        assertNotNull("Bytecode resource stream should not be null", is);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read;
        while ((read = is.read(buffer)) != -1) {
            baos.write(buffer, 0, read);
        }
        is.close();

        String bytecodeString = new String(baos.toByteArray(), "ISO-8859-1");
        assertFalse("VerificationOverTimeImpl should not depend on JUnit",
                bytecodeString.contains("junit"));
    }

    /**
     * Ground Truth Functional Defect:
     * VerificationOverTimeImpl should recover from any generic AssertionError,
     * not strictly MockitoAssertionError or ArgumentsAreDifferent.
     */
    @Test(timeout = 4000)
    public void testVerifyRecoversFromGenericAssertionError() {
        FakeTimer timer = new FakeTimer(3);
        AssertionError genericAssertionError = new AssertionError("Custom assertion failure");
        CountingVerificationMode mode = new CountingVerificationMode(1, genericAssertionError);
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(0, 100, mode, true, timer);

        overTime.verify(null);

        assertEquals(2, mode.invocations);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testAtMostFailureCannotRecoverAndThrowsImmediately() {
        FakeTimer timer = new FakeTimer(5);
        final int[] calls = new int[1];
        AtMost atMost = new AtMost(1) {
            @Override
            public void verify(VerificationData data) {
                calls[0]++;
                throw new MockitoAssertionError("AtMost validation failed");
            }
        };
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(0, 100, atMost, true, timer);

        try {
            overTime.verify(null);
            fail("Expected MockitoAssertionError for AtMost");
        } catch (MockitoAssertionError e) {
            assertEquals("AtMost validation failed", e.getMessage());
            assertEquals(1, calls[0]);
        }
    }

    @Test(timeout = 4000)
    public void testNoMoreInteractionsFailureCannotRecoverAndThrowsImmediately() {
        FakeTimer timer = new FakeTimer(5);
        final int[] calls = new int[1];
        NoMoreInteractions nmi = new NoMoreInteractions() {
            @Override
            public void verify(VerificationData data) {
                calls[0]++;
                throw new MockitoAssertionError("NoMoreInteractions validation failed");
            }
        };
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(0, 100, nmi, true, timer);

        try {
            overTime.verify(null);
            fail("Expected MockitoAssertionError for NoMoreInteractions");
        } catch (MockitoAssertionError e) {
            assertEquals("NoMoreInteractions validation failed", e.getMessage());
            assertEquals(1, calls[0]);
        }
    }

    @Test(timeout = 4000)
    public void testCanRecoverFromFailureBranches() {
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(0, 100, new CountingVerificationMode(0), true);

        assertTrue("Custom verification mode can recover",
                overTime.canRecoverFromFailure(new CountingVerificationMode(0)));
        assertFalse("AtMost cannot recover",
                overTime.canRecoverFromFailure(new AtMost(1)));
        assertFalse("NoMoreInteractions cannot recover",
                overTime.canRecoverFromFailure(new NoMoreInteractions()));
        assertTrue("Null mode can recover by default branch",
                overTime.canRecoverFromFailure(null));
    }

    @Test(timeout = 4000)
    public void testDelegateThrowsRuntimeExceptionPropagatesImmediately() {
        FakeTimer timer = new FakeTimer(3);
        VerificationMode mode = new VerificationMode() {
            @Override
            public void verify(VerificationData data) {
                throw new IllegalArgumentException("Invalid state in verification");
            }
        };
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(0, 100, mode, true, timer);

        try {
            overTime.verify(null);
            fail("Expected IllegalArgumentException to propagate without retry");
        } catch (IllegalArgumentException e) {
            assertEquals("Invalid state in verification", e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testSleepHandlesInterruptedExceptionGracefully() {
        FakeTimer timer = new FakeTimer(2);
        CountingVerificationMode mode = new CountingVerificationMode(1);
        VerificationOverTimeImpl overTime = new VerificationOverTimeImpl(100L, 1000L, mode, true, timer);

        Thread.currentThread().interrupt();
        try {
            overTime.verify(null);
            assertEquals(2, mode.invocations);
        } finally {
            Thread.interrupted(); // Clear interrupted status
        }
    }
}