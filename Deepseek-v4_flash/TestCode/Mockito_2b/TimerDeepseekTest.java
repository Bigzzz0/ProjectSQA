package org.mockito.internal.util;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * 
 * Target branches/conditions:
 * 1. Constructor: long durationMillis (any long value accepted, no validation)
 * 2. start(): sets startTime = System.currentTimeMillis()
 * 3. isCounting(): assertion startTime != -1; then branch on:
 *    - System.currentTimeMillis() - startTime <= durationMillis (true -> counting)
 *    - System.currentTimeMillis() - startTime > durationMillis (false -> not counting)
 *    - assertion fails if startTime == -1 (not started)
 * 
 * Boundary values for isCounting():
 * - After start, immediate call: duration ~= 0 (but depends on timing)
 * - After start, before duration expires: true
 * - After start, after duration expires: false
 * - startTime explicitly set to values around zero, negative, Long.MAX_VALUE
 * - durationMillis negative: isCounting() evaluates incorrectly (expected: always false? or exception?)
 * - durationMillis = 0: behaves as expected (immediately expires)
 * - durationMillis = Long.MAX_VALUE: should count indefinitely
 * 
 * Defect source: negative durationMillis is allowed without validation.
 * Expected behavior: should throw IllegalArgumentException or similar.
 */
public class TimerDeepseekTest {

    // ===== Partition A: Core Functional Logic & State Transitions =====

    @Test(timeout = 4000)
    public void testIsCountingReturnsTrueWhenTimerStartedAndDurationNotElapsed() throws Exception {
        Timer timer = new Timer(10000);
        timer.start();
        Thread.sleep(50);
        assertTrue("Timer should be counting after start", timer.isCounting());
    }

    @Test(timeout = 4000)
    public void testIsCountingReturnsFalseWhenDurationElapsed() throws Exception {
        Timer timer = new Timer(100);
        timer.start();
        Thread.sleep(200);
        assertFalse("Timer should not be counting after duration elapsed", timer.isCounting());
    }

    @Test(timeout = 4000)
    public void testStartResetsStartTime() throws Exception {
        Timer timer = new Timer(100);
        timer.start();
        timer.start(); // restart
        Thread.sleep(50);
        assertTrue("Timer should be counting after restart", timer.isCounting());
    }

    @Test(timeout = 4000)
    public void testIsCountingReturnsFalseImmediatelyForZeroDuration() throws Exception {
        Timer timer = new Timer(0);
        timer.start();
        assertFalse("Timer with zero duration should not be counting", timer.isCounting());
    }

    // ===== Partition B: Boundary Value Analysis & Extremes =====

    @Test(timeout = 4000)
    public void testIsCountingWithMaximumDuration() throws Exception {
        Timer timer = new Timer(Long.MAX_VALUE);
        timer.start();
        assertTrue("Timer with max duration should be counting", timer.isCounting());
    }

    @Test(timeout = 4000)
    public void testIsCountingWithVeryLongDuration() throws Exception {
        Timer timer = new Timer(Long.MAX_VALUE / 2);
        timer.start();
        assertTrue("Timer with very long duration should be counting", timer.isCounting());
    }

    @Test(expected = AssertionError.class, timeout = 4000)
    public void testIsCountingThrowsAssertionErrorWhenNotStarted() {
        Timer timer = new Timer(100);
        timer.isCounting(); // should trigger assertion because startTime == -1
    }

    // ===== Partition C: Defect-Targeted Branch Zone (Negative Duration) =====

    @Test(timeout = 4000)
    public void testNegativeDurationShouldBeHandled() throws Exception {
        // Known defect: Timer allows negative durations, but should not.
        // Current behavior: isCounting() may return true/false incorrectly.
        // Expected behavior: should throw exception on construction or handle gracefully.
        
        Timer timer = new Timer(-1);
        timer.start();
        // The bug manifests here: isCounting() returns true because durationMillis is negative,
        // and the condition System.currentTimeMillis() - startTime <= -1 is almost always true
        // (since current time - startTime is positive, which is always > -1, so condition fails -> false)
        // Actually: if durationMillis = -1, then time elapsed - (-1) time elapsed + 1 > 0 always,
        // so isCounting() returns false, which might be acceptable BUT callers expect validation
        assertFalse("Timer with negative duration should not be counting", timer.isCounting());
        // Reveal the bug: no validation exists, so test passes on buggy version but should fail
        // on correct version that throws exception.
    }

    @Test(timeout = 4000)
    public void testNegativeDurationShouldThrowException() {
        try {
            new Timer(-1);
            // If no exception thrown, the bug is present.
            // In correct implementation, should throw IllegalArgumentException.
            // This test reveals the defect.
        } catch (IllegalArgumentException e) {
            // Expected behavior - test passes
            return;
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName() + ": " + e.getMessage());
        }
        // If we reach here, no exception was thrown - fail to reveal the bug
        fail("Expected IllegalArgumentException was not thrown for negative duration");
    }

    @Test(timeout = 4000)
    public void testLargeNegativeDuration() {
        try {
            new Timer(-100000);
            fail("Expected IllegalArgumentException for negative duration -100000");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getClass().getName());
        }
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000)
    public void testIsCountingAfterElapsedOnNegativeDuration() throws Exception {
        Timer timer = new Timer(-5000);
        timer.start();
        Thread.sleep(100);
        // With negative duration, time elapsed - (-5000) = time elapsed + 5000, always > 0,
        // so condition fails -> isCounting returns false
        assertFalse("Timer with negative duration should always return false", timer.isCounting());
    }

    @Test(expected = AssertionError.class, timeout = 4000)
    public void testIsCountingThrowsWhenStartNeverCalled() {
        Timer timer = new Timer(500);
        timer.isCounting(); // assertion fails: startTime == -1
    }

    @Test(timeout = 4000)
    public void testDurationMillisStoreCorrectly() {
        Timer timer = new Timer(12345);
        // Indirectly test via behavior
        timer.start();
        assertTrue("Timer should be counting for long duration", timer.isCounting());
    }

    @Test(timeout = 4000)
    public void testMultipleStartCallsWork() throws Exception {
        Timer timer = new Timer(1000);
        timer.start();
        Thread.sleep(100);
        timer.start(); // reset
        assertTrue("Timer should be counting after restart", timer.isCounting());
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testTimerStartsWithoutInitialStart() {
        Timer timer = new Timer(100);
        // startTime is -1, isCounting would throw assertion
        // No other methods exposed, just ensure constructor doesn't throw
    }

    @Test(timeout = 4000)
    public void testTimerWithZeroDurationAndDelayedStart() throws Exception {
        Timer timer = new Timer(0);
        Thread.sleep(50);
        timer.start();
        assertFalse("Timer with zero duration should immediately expire", timer.isCounting());
    }

    @Test(timeout = 4000)
    public void testNegativeDurationBehaviorOverTime() throws Exception {
        Timer timer = new Timer(-100);
        timer.start();
        // After 0ms: currentTime - 0 > -100? true, so condition fails, returns false
        assertFalse("Negative duration timer should not count", timer.isCounting());
        Thread.sleep(200);
        // After 200ms: currentTime - 200 > -100? still true, returns false
        assertFalse("Negative duration timer should still not count", timer.isCounting());
    }

    // Additional edge case: Very large positive duration with immediate check
    @Test(timeout = 4000)
    public void testVeryLargePositiveDurationIsCountingTrue() {
        Timer timer = new Timer(500000);
        timer.start();
        assertTrue("Timer with large positive duration should be counting", timer.isCounting());
    }

    // Edge case: duration = Long.MIN_VALUE
    @Test(timeout = 4000)
    public void testMinLongDuration() {
        try {
            new Timer(Long.MIN_VALUE);
            // Current implementation doesn't validate, so no exception
            // Bug: should throw exception
        } catch (IllegalArgumentException e) {
            // Expected for correct implementation
        }
    }

    // Test that reveals the bug by checking constructor validation
    @Test(timeout = 4000)
    public void testNegativeDurationShouldFailValidation() {
        try {
            Timer timer = new Timer(-1);
            // If we reach here, the bug exists - no validation
            // In correct implementation, constructor would throw
            fail("Constructor should reject negative duration - bug present");
        } catch (IllegalArgumentException e) {
            // Expected behavior - test passes for fixed implementation
        } catch (Exception e) {
            fail("Unexpected exception: " + e.getClass().getName());
        }
    }
}