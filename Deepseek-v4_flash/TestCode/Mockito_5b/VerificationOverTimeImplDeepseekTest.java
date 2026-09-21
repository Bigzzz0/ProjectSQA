package org.mockito.internal.verification;

import org.junit.Test;
import static org.junit.Assert.*;
import org.mockito.internal.verification.api.VerificationData;
import org.mockito.internal.util.Timer;
import org.mockito.exceptions.base.MockitoAssertionError;
import org.mockito.exceptions.verification.junit.ArgumentsAreDifferent;
import java.lang.reflect.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * ====== Decision / Condition Branch Targets ======
 * 
 * 1. Constructor branches:
 *    - Delegation to (long, long, VerificationMode, boolean) -> second constructor
 *    - Assignment of all fields
 * 
 * 2. verify(VerificationData) branches:
 *    - Branch on timer.isCounting() (loop condition)
 *    - Branch on delegate.verify(data) success vs. exception (MockitoAssertionError / ArgumentsAreDifferent)
 *    - Branch on returnOnSuccess (if true -> early return; if false -> reset error to null)
 *    - Branch on final error != null -> throw error
 * 
 * 3. handleVerifyException(AssertionError) branches:
 *    - Branch on canRecoverFromFailure(delegate) -> true: sleep and return error
 *    - Branch on canRecoverFromFailure(delegate) -> false: rethrow e immediately
 * 
 * 4. canRecoverFromFailure(VerificationMode) branches:
 *    - Branch on verificationMode instanceof AtMost or NoMoreInteractions -> false
 *    - Otherwise -> true
 * 
 * 5. sleep(long) branches:
 *    - Normal sleep completion
 *    - InterruptedException (catch and ignore)
 * 
 * ====== Boundary & Exception Paths ======
 * - Null delegate -> NPE in verify()
 * - Null data -> passed to delegate.verify() (delegate may handle or not)
 * - pollingPeriodMillis = 0, negative, very large
 * - durationMillis = 0, negative, very large
 * - Delegate throws plain AssertionError (not caught by special handlers)
 * - Delegate throws MockitoAssertionError with non-recoverable mode (AtMost, NoMoreInteractions)
 * - Timer counts exactly to zero (timeout)
 * 
 * ====== Defect-Targeted (Defects4J) ======
 * - Verify that class does NOT have a direct dependency on org.junit.* types
 *   (using reflection to inspect fields, methods, constructors and their signatures)
 *   This reveals the bug: if the class references JUnit classes (e.g., junit.framework.AssertionFailedError)
 *   the test will fail, exposing the unwanted dependency.
 * 
 * ====== Coverage Strategy ======
 * - Use custom timer (FakeTimer) to control loop iterations without real delays.
 * - Implement inner VerificationMode classes to produce success, MockitoAssertionError,
 *   ArgumentsAreDifferent, plain AssertionError.
 * - Test both returnOnSuccess = true and false.
 * - Test recoverable and non-recoverable delegates.
 * - Directly call protected method canRecoverFromFailure (same package access).
 */
public class VerificationOverTimeImplDeepseekTest {

    // --- Custom Timer for fast, deterministic tests ---
    private static class FakeTimer extends Timer {
        private int maxChecks;
        private int count = 0;

        public FakeTimer(long durationMillis) {
            super(durationMillis);
            this.maxChecks = 1;
        }

        public void setMaxChecks(int maxChecks) {
            this.maxChecks = maxChecks;
        }

        @Override
        public void start() {
            count = 0;
        }

        @Override
        public boolean isCounting() {
            count++;
            return count <= maxChecks;
        }
    }

    // --- Custom VerificationMode implementations ---
    private static class SuccessVerificationMode implements VerificationMode {
        @Override
        public void verify(VerificationData data) {
            // always succeeds
        }
    }

    private static class FailingMockitoMode implements VerificationMode {
        private final boolean canRecover;

        public FailingMockitoMode(boolean canRecover) {
            this.canRecover = canRecover;
        }

        @Override
        public void verify(VerificationData data) {
            throw new MockitoAssertionError("mockito failure");
        }
    }

    private static class FailingArgumentsMode implements VerificationMode {
        @Override
        public void verify(VerificationData data) {
            throw new ArgumentsAreDifferent("arguments different");
        }
    }

    private static class FailingPlainAssertionMode implements VerificationMode {
        @Override
        public void verify(VerificationData data) {
            throw new AssertionError("plain assertion failure");
        }
    }

    // ======= Partition A: Core Functional Logic & State Transitions =======

    @Test(timeout = 4000)
    public void testConstructorAndGetters() {
        long polling = 10;
        long duration = 100;
        VerificationMode delegate = new SuccessVerificationMode();
        boolean returnOnSuccess = true;
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(polling, duration, delegate, returnOnSuccess);
        assertEquals(polling, impl.getPollingPeriod());
        assertEquals(duration, impl.getDuration());
        assertSame(delegate, impl.getDelegate());
    }

    @Test(timeout = 4000)
    public void testConstructorWithTimer() {
        long polling = 10;
        long duration = 100;
        VerificationMode delegate = new SuccessVerificationMode();
        boolean returnOnSuccess = false;
        FakeTimer timer = new FakeTimer(duration);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(polling, duration, delegate, returnOnSuccess, timer);
        assertEquals(polling, impl.getPollingPeriod());
        assertEquals(duration, impl.getDuration());
        assertSame(delegate, impl.getDelegate());
    }

    @Test(timeout = 4000)
    public void testVerifyReturnOnSuccessSuccess() {
        FakeTimer timer = new FakeTimer(100);
        timer.setMaxChecks(2); // loop checks twice, but first succeeds -> return early
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(10, 100, new SuccessVerificationMode(), true, timer);
        impl.verify(null); // should not throw
    }

    @Test(timeout = 4000)
    public void testVerifyReturnOnSuccessFalseAndSuccess() {
        FakeTimer timer = new FakeTimer(100);
        timer.setMaxChecks(3); // loop runs 3 times, delegate succeeds each time
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(10, 100, new SuccessVerificationMode(), false, timer);
        impl.verify(null); // no exception because final error is null
    }

    // ======= Partition B: Boundary, Extreme, and Null Values =======

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testVerifyWithNullDelegate() {
        FakeTimer timer = new FakeTimer(1);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(1, 1, null, true, timer);
        impl.verify(null);
    }

    @Test(timeout = 4000)
    public void testVerifyWithZeroPollingAndDuration() {
        FakeTimer timer = new FakeTimer(0); // duration = 0
        timer.setMaxChecks(1); // timer never counts -> loop doesn't execute
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(0, 0, new SuccessVerificationMode(), true, timer);
        impl.verify(null);
        // after loop, error is null -> returns normally
    }

    @Test(timeout = 4000)
    public void testVerifyWithNegativePollingAndDuration() {
        // Negative values are not validated; we just test they don't cause runtime issues.
        FakeTimer timer = new FakeTimer(-1);
        timer.setMaxChecks(1);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(-1, -1, new SuccessVerificationMode(), true, timer);
        impl.verify(null);
    }

    // ======= Partition C: Defect-Targeted Branch Zone =======
    // (Failing delegate paths, recoverable/non-recoverable, plain AssertionError)

    @Test(timeout = 4000)
    public void testVerifyReturnOnSuccessAndFailsRecoverable() {
        FakeTimer timer = new FakeTimer(100);
        timer.setMaxChecks(2); // loop twice, then timeout -> last error thrown
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(10, 100, new FailingMockitoMode(true), true, timer);
        try {
            impl.verify(null);
            fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testVerifyReturnOnSuccessAndFailsNonRecoverable() {
        // AtMost is non-recoverable -> should rethrow immediately
        AtMost atMost = new AtMost(1);
        FakeTimer timer = new FakeTimer(100);
        timer.setMaxChecks(2);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(10, 100, atMost, true, timer);
        try {
            impl.verify(null);
            fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testVerifyReturnOnSuccessFalseAndFails() {
        FakeTimer timer = new FakeTimer(100);
        timer.setMaxChecks(3);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(10, 100, new FailingMockitoMode(true), false, timer);
        try {
            impl.verify(null);
            fail("Expected MockitoAssertionError");
        } catch (MockitoAssertionError e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testVerifyWithArgumentsAreDifferent() {
        FakeTimer timer = new FakeTimer(100);
        timer.setMaxChecks(2);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(10, 100, new FailingArgumentsMode(), true, timer);
        try {
            impl.verify(null);
            fail("Expected ArgumentsAreDifferent");
        } catch (ArgumentsAreDifferent e) {
            // expected
        }
    }

    @Test(timeout = 4000, expected = AssertionError.class)
    public void testVerifyWithPlainAssertionError() {
        // Plain AssertionError is not caught by special handlers -> propagates directly
        FakeTimer timer = new FakeTimer(100);
        timer.setMaxChecks(1);
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(10, 100, new FailingPlainAssertionMode(), true, timer);
        impl.verify(null);
    }

    // ======= Partition D: Exception & Defensive Guard Paths =======
    // (canRecoverFromFailure, handleVerifyException indirectly tested above)

    @Test(timeout = 4000)
    public void testCanRecoverFromFailure() {
        VerificationOverTimeImpl impl = new VerificationOverTimeImpl(1, 1, new SuccessVerificationMode(), true);
        assertTrue(impl.canRecoverFromFailure(new SuccessVerificationMode()));
        assertFalse(impl.canRecoverFromFailure(new AtMost(1)));
        assertFalse(impl.canRecoverFromFailure(new NoMoreInteractions()));
    }

    // ======= Partition E: Object Lifecycle & Contract Integrity =======
    // (reflection-based dependency check – targets the known Defects4J defect)

    @Test(timeout = 4000)
    public void shouldNotDependOnJUnit() throws Exception {
        Class<?> clazz = VerificationOverTimeImpl.class;
        // Fields
        for (Field field : clazz.getDeclaredFields()) {
            assertFalse("Field " + field.getName() + " has JUnit type: " + field.getType().getName(),
                         field.getType().getName().startsWith("org.junit"));
        }
        // Constructors
        for (Constructor<?> ctor : clazz.getDeclaredConstructors()) {
            for (Class<?> param : ctor.getParameterTypes()) {
                assertFalse("Constructor parameter " + param.getName() + " is JUnit type",
                             param.getName().startsWith("org.junit"));
            }
        }
        // Methods
        for (Method method : clazz.getDeclaredMethods()) {
            assertFalse("Method " + method.getName() + " return type " + method.getReturnType().getName() + " is JUnit type",
                         method.getReturnType().getName().startsWith("org.junit"));
            for (Class<?> param : method.getParameterTypes()) {
                assertFalse("Method " + method.getName() + " parameter " + param.getName() + " is JUnit type",
                             param.getName().startsWith("org.junit"));
            }
            for (Class<?> exc : method.getExceptionTypes()) {
                assertFalse("Method " + method.getName() + " exception " + exc.getName() + " is JUnit type",
                             exc.getName().startsWith("org.junit"));
            }
        }
    }
}