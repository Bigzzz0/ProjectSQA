package org.apache.commons.lang.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for StopWatch.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Decision branches covered:
 * - start(): runningState == STATE_STOPPED -> throw; runningState != STATE_UNSTARTED -> throw; else start.
 * - stop(): runningState != STATE_RUNNING && runningState != STATE_SUSPENDED -> throw; else stop.
 * - reset(): unconditional reset.
 * - split(): runningState != STATE_RUNNING -> throw; else split (no check on splitState – known defect).
 * - unsplit(): splitState != STATE_SPLIT -> throw; else unsplit.
 * - suspend(): runningState != STATE_RUNNING -> throw; else suspend.
 * - resume(): runningState != STATE_SUSPENDED -> throw; else resume.
 * - getTime(): STATE_STOPPED/SUSPENDED -> stopTime-startTime; STATE_UNSTARTED -> 0; STATE_RUNNING -> current-startTime; else throw.
 * - getSplitTime(): splitState != STATE_SPLIT -> throw; else stopTime-startTime.
 * 
 * Boundary conditions:
 * - State transitions: UNSTARTED -> RUNNING -> STOPPED; RUNNING -> SUSPENDED -> RUNNING; RUNNING -> SPLIT -> UNSPLIT.
 * - Multiple calls to split() without unsplit (defect LANG-315).
 * - getTime() after reset, after stop, after suspend, after resume.
 * - getSplitTime() after split and after unsplit.
 * 
 * Defect-targeted: LANG-315 – split() does not prevent being called twice.
 * The test expects an IllegalStateException on second split; defective version allows it.
 */
public class StopWatchDeepseekTest {

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testStartStop() {
        StopWatch watch = new StopWatch();
        assertEquals("Initial state should be unstarted", 0, watch.getTime());
        watch.start();
        assertTrue("Time should increase after start", watch.getTime() >= 0);
        watch.stop();
        long time = watch.getTime();
        assertTrue("Stopped time should be non-negative", time >= 0);
    }

    @Test(timeout = 4000)
    public void testReset() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.reset();
        assertEquals("After reset, time should be 0", 0, watch.getTime());
        // Should be able to start again
        watch.start();
        assertTrue("Can restart after reset", watch.getTime() >= 0);
        watch.stop();
    }

    @Test(timeout = 4000)
    public void testSplitUnsplit() {
        StopWatch watch = new StopWatch();
        watch.start();
        // Let some time pass
        sleep(10);
        watch.split();
        long splitTime = watch.getSplitTime();
        assertTrue("Split time should be positive", splitTime > 0);
        // Unsplit
        watch.unsplit();
        // After unsplit, getSplitTime should throw
        try {
            watch.getSplitTime();
            fail("Expected IllegalStateException after unsplit");
        } catch (IllegalStateException e) {
            // expected
        }
        watch.stop();
    }

    @Test(timeout = 4000)
    public void testSuspendResume() {
        StopWatch watch = new StopWatch();
        watch.start();
        sleep(10);
        watch.suspend();
        long suspendedTime = watch.getTime();
        assertTrue("Suspended time should be positive", suspendedTime > 0);
        sleep(10); // This time should not be counted
        watch.resume();
        sleep(10);
        watch.stop();
        long totalTime = watch.getTime();
        // Total should be roughly suspendedTime + 10ms (the second sleep after resume)
        assertTrue("Total time should be > suspendedTime", totalTime > suspendedTime);
        // But less than suspendedTime + 20ms (since the 10ms during suspend are excluded)
        assertTrue("Total time should be less than suspendedTime + 20", totalTime < suspendedTime + 20);
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testGetTimeUnstarted() {
        StopWatch watch = new StopWatch();
        assertEquals("Unstarted watch returns 0", 0, watch.getTime());
    }

    @Test(timeout = 4000)
    public void testGetTimeRunning() {
        StopWatch watch = new StopWatch();
        watch.start();
        long t1 = watch.getTime();
        sleep(5);
        long t2 = watch.getTime();
        assertTrue("Time should increase while running", t2 > t1);
        watch.stop();
    }

    @Test(timeout = 4000)
    public void testGetTimeStopped() {
        StopWatch watch = new StopWatch();
        watch.start();
        sleep(5);
        watch.stop();
        long stoppedTime = watch.getTime();
        // After stop, time should remain constant
        sleep(5);
        assertEquals("Stopped time should not change", stoppedTime, watch.getTime());
    }

    @Test(timeout = 4000)
    public void testGetTimeSuspended() {
        StopWatch watch = new StopWatch();
        watch.start();
        sleep(5);
        watch.suspend();
        long suspendedTime = watch.getTime();
        sleep(5);
        assertEquals("Suspended time should not change", suspendedTime, watch.getTime());
        watch.resume();
        watch.stop();
    }

    // ==================== Partition C: Defect-Targeted Branch Zone (LANG-315) ====================

    @Test(timeout = 4000)
    public void testLang315DoubleSplit() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.split(); // first split – should succeed
        // Second split should throw IllegalStateException according to spec,
        // but defective version does not check splitState.
        try {
            watch.split(); // In defective version, this succeeds (bug)
            // If we reach here, the bug is present – we should fail the test
            fail("Expected IllegalStateException on second split, but no exception thrown (defect present)");
        } catch (IllegalStateException e) {
            // In fixed version, this is expected; test passes.
        }
        watch.unsplit();
        watch.stop();
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testStartWhenAlreadyRunning() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.start(); // should throw
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testStartWhenStoppedWithoutReset() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.start(); // should throw because not reset
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testStopWhenNotRunning() {
        StopWatch watch = new StopWatch();
        watch.stop(); // unstarted
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testSplitWhenNotRunning() {
        StopWatch watch = new StopWatch();
        watch.split();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testUnsplitWhenNotSplit() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.unsplit(); // never split
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testSuspendWhenNotRunning() {
        StopWatch watch = new StopWatch();
        watch.suspend();
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testResumeWhenNotSuspended() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.resume(); // not suspended
    }

    @Test(timeout = 4000, expected = IllegalStateException.class)
    public void testGetSplitTimeWhenNotSplit() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.getSplitTime();
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testFullLifecycle() {
        StopWatch watch = new StopWatch();
        // start -> split -> unsplit -> suspend -> resume -> stop -> reset -> start -> stop
        watch.start();
        sleep(5);
        watch.split();
        watch.unsplit();
        watch.suspend();
        watch.resume();
        sleep(5);
        watch.stop();
        long time1 = watch.getTime();
        assertTrue("Time should be positive", time1 > 0);
        watch.reset();
        assertEquals("After reset, time is 0", 0, watch.getTime());
        watch.start();
        sleep(5);
        watch.stop();
        long time2 = watch.getTime();
        assertTrue("Second timing should work", time2 > 0);
    }

    @Test(timeout = 4000)
    public void testToString() {
        StopWatch watch = new StopWatch();
        watch.start();
        sleep(10);
        watch.stop();
        String str = watch.toString();
        assertNotNull("toString should not be null", str);
        assertTrue("toString should contain colons", str.contains(":"));
    }

    @Test(timeout = 4000)
    public void testToSplitString() {
        StopWatch watch = new StopWatch();
        watch.start();
        sleep(10);
        watch.split();
        String str = watch.toSplitString();
        assertNotNull("toSplitString should not be null", str);
        assertTrue("toSplitString should contain colons", str.contains(":"));
        watch.unsplit();
        watch.stop();
    }

    // Helper to sleep without throwing InterruptedException
    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}