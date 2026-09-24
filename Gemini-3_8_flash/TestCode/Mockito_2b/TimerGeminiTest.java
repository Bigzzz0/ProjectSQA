package org.mockito.internal.util;

import org.junit.Test;
import org.mockito.exceptions.misusing.FriendlyReminderException;

import static org.junit.Assert.*;

/**
 * /* [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------------------------------
 * Target Class: org.mockito.internal.util.Timer
 * Target Defect: Timer accepts negative duration without validation, failing to throw FriendlyReminderException.
 *
 * Decision / Branch Matrix:
 * 1. Constructor Validation (Defect Zone):
 *    - durationMillis < 0 (e.g., -1L, -100L, Long.MIN_VALUE) -> MUST throw FriendlyReminderException.
 *    - durationMillis == 0 -> Valid boundary, timer initialized.
 *    - durationMillis > 0 (e.g., 1L, 1000L, Long.MAX_VALUE) -> Valid operational state.
 * 2. Timer#start():
 *    - Updates internal startTime from -1 to System.currentTimeMillis().
 *    - Multiple start() calls -> resets startTime baseline.
 * 3. Timer#isCounting():
 *    - Branch: assert startTime != -1 -> Triggers AssertionError if assertions are enabled and start() wasn't called.
 *    - Branch: (System.currentTimeMillis() - startTime <= durationMillis)
 *        - True: Elapsed time <= durationMillis (still active / counting).
 *        - False: Elapsed time > durationMillis (expired / finished).
 * -----------------------------------------------------------------------------------------------------------------
 */
public class TimerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testTimerStartsAndIsCountingImmediately() {
        Timer timer = new Timer(5000L);
        timer.start();
        assertTrue("Timer should be actively counting immediately after start()", timer.isCounting());
    }

    @Test(timeout = 4000)
    public void testTimerExpirationAfterDurationElapsed() throws InterruptedException {
        Timer timer = new Timer(10L);
        timer.start();
        Thread.sleep(25L);
        assertFalse("Timer should report not counting after duration has elapsed", timer.isCounting());
    }

    @Test(timeout = 4000)
    public void testRestartTimerResetsCountingState() throws InterruptedException {
        Timer timer = new Timer(20L);
        timer.start();
        Thread.sleep(30L);
        assertFalse("Timer should expire after the delay", timer.isCounting());

        timer.start();
        assertTrue("Restarted timer should be actively counting again", timer.isCounting());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testZeroDurationTimerCreation() {
        Timer timer = new Timer(0L);
        assertNotNull("Timer with 0 duration should instantiate successfully", timer);
    }

    @Test(timeout = 4000)
    public void testZeroDurationTimerExpiresImmediatelyAfterMinimalDelay() throws InterruptedException {
        Timer timer = new Timer(0L);
        timer.start();
        Thread.sleep(5L);
        assertFalse("Timer with duration 0 must not be counting after positive elapsed time", timer.isCounting());
    }

    @Test(timeout = 4000)
    public void testMaxLongDurationTimer() {
        Timer timer = new Timer(Long.MAX_VALUE);
        timer.start();
        assertTrue("Timer with Long.MAX_VALUE duration should be counting", timer.isCounting());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
    // =========================================================================

    @Test(timeout = 4000)
    public void should_throw_friendly_reminder_exception_when_duration_is_negative() {
        try {
            new Timer(-1L);
            fail("It is forbidden to create timer with negative value of timer's duration.");
        } catch (FriendlyReminderException expected) {
            assertEquals(
                "It is forbidden to create timer with negative value of timer's duration.",
                expected.getMessage()
            );
        }
    }

    @Test(timeout = 4000)
    public void testNegativeDurationBoundaryMinusOneThrowsException() {
        try {
            new Timer(-1L);
            fail("It is forbidden to create timer with negative value of timer's duration.");
        } catch (FriendlyReminderException expected) {
            assertNotNull("Exception message should be populated", expected.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testNegativeDurationMinValueThrowsException() {
        try {
            new Timer(Long.MIN_VALUE);
            fail("It is forbidden to create timer with negative value of timer's duration.");
        } catch (FriendlyReminderException expected) {
            assertEquals(
                "It is forbidden to create timer with negative value of timer's duration.",
                expected.getMessage()
            );
        }
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testNegativeDurationArbitraryThrowsException() {
        try {
            new Timer(-500L);
            fail("It is forbidden to create timer with negative value of timer's duration.");
        } catch (FriendlyReminderException expected) {
            assertEquals(
                "It is forbidden to create timer with negative value of timer's duration.",
                expected.getMessage()
            );
        }
    }

    @Test(timeout = 4000)
    public void testIsCountingBeforeStartFailsAssertionIfEnabled() {
        boolean assertionsEnabled = false;
        assert assertionsEnabled = true; // Intentional side-effect to check if -ea is active

        if (assertionsEnabled) {
            try {
                Timer timer = new Timer(1000L);
                timer.isCounting();
                fail("Expected AssertionError since start() was never invoked");
            } catch (AssertionError expected) {
                // Expected behaviour when assertions are turned on
            }
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Instance Independence
    // =========================================================================

    @Test(timeout = 4000)
    public void testMultipleTimerInstancesOperateIndependently() throws InterruptedException {
        Timer timerShort = new Timer(10L);
        Timer timerLong = new Timer(10000L);

        timerShort.start();
        timerLong.start();

        Thread.sleep(25L);

        assertFalse("Short timer should have expired", timerShort.isCounting());
        assertTrue("Long timer should still be counting", timerLong.isCounting());
    }
}