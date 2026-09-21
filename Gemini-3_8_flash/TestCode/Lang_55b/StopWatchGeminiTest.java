package org.apache.commons.lang.time;

import org.junit.Test;
import static org.junit.Assert.*;
import java.lang.reflect.Field;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.lang.time.StopWatch
 *
 * Methods & Decision Branches Analyzed:
 * 1. start():
 *    - Branch 1 (runningState == STATE_STOPPED): throws IllegalStateException ("Stopwatch must be reset...")
 *    - Branch 2 (runningState != STATE_UNSTARTED): throws IllegalStateException ("Stopwatch already started...")
 *    - Branch 3 (Normal start): sets stopTime = -1, startTime = now, runningState = STATE_RUNNING
 * 2. stop():
 *    - Branch 1 (runningState != STATE_RUNNING && runningState != STATE_SUSPENDED): throws IllegalStateException
 *    - Branch 2 (Normal stop from STATE_RUNNING): records stopTime, runningState = STATE_STOPPED
 *    - Branch 3 (Stop from STATE_SUSPENDED) [CRITICAL DEFECT LANG-315]:
 *        In buggy code, stopTime is erroneously overwritten with current time, corrupting suspended duration!
 * 3. reset():
 *    - Unconditionally clears state to STATE_UNSTARTED, STATE_UNSPLIT, startTime = -1, stopTime = -1
 * 4. split():
 *    - Branch 1 (runningState != STATE_RUNNING): throws IllegalStateException
 *    - Branch 2 (Normal split): records stopTime, sets splitState = STATE_SPLIT
 * 5. unsplit():
 *    - Branch 1 (splitState != STATE_SPLIT): throws IllegalStateException
 *    - Branch 2 (Normal unsplit): sets stopTime = -1, splitState = STATE_UNSPLIT
 * 6. suspend():
 *    - Branch 1 (runningState != STATE_RUNNING): throws IllegalStateException
 *    - Branch 2 (Normal suspend): records stopTime, sets runningState = STATE_SUSPENDED
 * 7. resume():
 *    - Branch 1 (runningState != STATE_SUSPENDED): throws IllegalStateException
 *    - Branch 2 (Normal resume): adjusts startTime by (now - stopTime), resets stopTime = -1, runningState = STATE_RUNNING
 * 8. getTime():
 *    - Branch 1 (runningState == STATE_STOPPED || runningState == STATE_SUSPENDED): returns stopTime - startTime
 *    - Branch 2 (runningState == STATE_UNSTARTED): returns 0
 *    - Branch 3 (runningState == STATE_RUNNING): returns now - startTime
 *    - Branch 4 (Default / Invalid State): throws RuntimeException("Illegal running state has occured. ")
 * 9. getSplitTime():
 *    - Branch 1 (splitState != STATE_SPLIT): throws IllegalStateException
 *    - Branch 2 (Normal getSplitTime): returns stopTime - startTime
 * 10. toString() & toSplitString():
 *    - Delegates to DurationFormatUtils.formatDurationHMS with respective duration
 * -------------------------------------------------------------------------------------------------------
 */
public class StopWatchGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardLifecycleStartStopReset() throws InterruptedException {
        StopWatch watch = new StopWatch();
        assertEquals("Initial time before start must be 0", 0L, watch.getTime());

        watch.start();
        Thread.sleep(50);
        assertTrue("Time while running should advance", watch.getTime() >= 40L);

        watch.stop();
        long stoppedTime = watch.getTime();
        Thread.sleep(30);
        assertEquals("Time after stop must remain constant", stoppedTime, watch.getTime());

        watch.reset();
        assertEquals("Time after reset must return to 0", 0L, watch.getTime());

        // Restarting after reset is permitted
        watch.start();
        Thread.sleep(20);
        assertTrue("Time after restart must advance", watch.getTime() >= 15L);
        watch.stop();
    }

    @Test(timeout = 4000)
    public void testSplitAndUnsplitLifecycle() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(50);

        watch.split();
        long splitTime1 = watch.getSplitTime();
        assertTrue("Split time must be positive", splitTime1 >= 40L);

        Thread.sleep(50);
        long splitTime2 = watch.getSplitTime();
        assertEquals("Split time must remain frozen while split", splitTime1, splitTime2);
        assertTrue("Running time must continue to accumulate beyond split time", watch.getTime() > splitTime1);

        String splitStr = watch.toSplitString();
        assertNotNull("toSplitString should produce a valid string", splitStr);
        assertTrue("Split string should not be empty", splitStr.length() > 0);

        watch.unsplit();
        assertTrue("Time after unsplit should reflect total elapsed time", watch.getTime() >= splitTime1);

        watch.stop();
    }

    @Test(timeout = 4000)
    public void testSuspendAndResumeLifecycle() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(50);

        watch.suspend();
        long suspendTime = watch.getTime();
        Thread.sleep(60);
        assertEquals("Time must freeze while suspended", suspendTime, watch.getTime());

        watch.resume();
        Thread.sleep(50);
        long resumedRunningTime = watch.getTime();
        assertTrue("Resumed running time must exclude the suspended interval",
                resumedRunningTime < (suspendTime + 60 + 50));
        assertTrue("Resumed running time must advance by active run time",
                resumedRunningTime >= (suspendTime + 40));

        watch.stop();
    }

    @Test(timeout = 4000)
    public void testMultipleSplitsWhileRunning() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(30);

        watch.split();
        long split1 = watch.getSplitTime();

        Thread.sleep(30);
        watch.split(); // re-split while running
        long split2 = watch.getSplitTime();

        assertTrue("Successive split time must be greater than previous split time", split2 >= split1);
        watch.unsplit();
        watch.stop();
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testImmediateStopAfterStart() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        long elapsed = watch.getTime();
        assertTrue("Immediate stop duration should be non-negative", elapsed >= 0L);
    }

    @Test(timeout = 4000)
    public void testImmediateSplitAndUnsplit() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.split();
        long splitTime = watch.getSplitTime();
        assertTrue("Immediate split duration should be non-negative", splitTime >= 0L);
        watch.unsplit();
        watch.stop();
    }

    @Test(timeout = 4000)
    public void testImmediateSuspendAndResume() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.suspend();
        long suspendedTime = watch.getTime();
        assertTrue("Immediate suspended duration should be non-negative", suspendedTime >= 0L);
        watch.resume();
        watch.stop();
    }

    @Test(timeout = 4000)
    public void testMultipleResetsInAnyState() {
        StopWatch watch = new StopWatch();
        watch.reset();
        assertEquals(0L, watch.getTime());

        watch.start();
        watch.reset();
        assertEquals(0L, watch.getTime());

        watch.start();
        watch.suspend();
        watch.reset();
        assertEquals(0L, watch.getTime());

        watch.start();
        watch.stop();
        watch.reset();
        watch.reset();
        assertEquals(0L, watch.getTime());
    }

    @Test(timeout = 4000)
    public void testToStringRepresentations() {
        StopWatch watch = new StopWatch();
        String initialStr = watch.toString();
        assertNotNull(initialStr);
        assertEquals("0:00:00.000", initialStr);

        watch.start();
        assertNotNull(watch.toString());
        watch.stop();
        assertNotNull(watch.toString());
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J LANG-315)
    // =========================================================================

    /**
     * LANG-315: Calling stop() after suspend() corrupts the recorded time because
     * stop() mistakenly overrides stopTime with System.currentTimeMillis().
     * The elapsed time recorded at suspend() must equal the final time recorded at stop().
     */
    @Test(timeout = 4000)
    public void testLang315() throws InterruptedException {
        StopWatch watch = new StopWatch();
        watch.start();
        Thread.sleep(100);
        watch.suspend();
        long suspendTime = watch.getTime();
        Thread.sleep(100);
        watch.stop();
        long totalTime = watch.getTime();
        assertEquals("Total time after stopping a suspended watch must match the suspend time",
                suspendTime, totalTime);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testStartWhenAlreadyRunningThrowsException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.start();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testStartWhenStoppedWithoutResetThrowsException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.start();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testStartWhenSuspendedThrowsException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.suspend();
        watch.start();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testStopWhenUnstartedThrowsException() {
        StopWatch watch = new StopWatch();
        watch.stop();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testStopWhenAlreadyStoppedThrowsException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.stop();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testSplitWhenUnstartedThrowsException() {
        StopWatch watch = new StopWatch();
        watch.split();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testSplitWhenStoppedThrowsException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.split();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testSplitWhenSuspendedThrowsException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.suspend();
        watch.split();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testUnsplitWhenNeverSplitThrowsException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.unsplit();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testUnsplitWhenUnstartedThrowsException() {
        StopWatch watch = new StopWatch();
        watch.unsplit();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testUnsplitTwiceThrowsException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.split();
        watch.unsplit();
        watch.unsplit();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testSuspendWhenUnstartedThrowsException() {
        StopWatch watch = new StopWatch();
        watch.suspend();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testSuspendWhenStoppedThrowsException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.suspend();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testSuspendWhenAlreadySuspendedThrowsException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.suspend();
        watch.suspend();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testResumeWhenUnstartedThrowsException() {
        StopWatch watch = new StopWatch();
        watch.resume();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testResumeWhenRunningThrowsException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.resume();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testResumeWhenStoppedThrowsException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.stop();
        watch.resume();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testGetSplitTimeWhenUnsplitThrowsException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.getSplitTime();
    }

    @Test(expected = IllegalStateException.class, timeout = 4000)
    public void testToSplitStringWhenUnsplitThrowsException() {
        StopWatch watch = new StopWatch();
        watch.start();
        watch.toSplitString();
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(expected = RuntimeException.class, timeout = 4000)
    public void testGetTimeCorruptedRunningStateThrowsRuntimeException() throws Exception {
        StopWatch watch = new StopWatch();
        Field stateField = StopWatch.class.getDeclaredField("runningState");
        stateField.setAccessible(true);
        stateField.setInt(watch, 999); // Invalid running state

        watch.getTime();
    }

    @Test(timeout = 4000)
    public void testGetTimeBranchCoverage() {
        StopWatch watch = new StopWatch();
        // Branch 1: STATE_UNSTARTED
        assertEquals(0L, watch.getTime());

        // Branch 2: STATE_RUNNING
        watch.start();
        assertTrue(watch.getTime() >= 0L);

        // Branch 3: STATE_SUSPENDED
        watch.suspend();
        long suspendedTime = watch.getTime();
        assertTrue(suspendedTime >= 0L);

        // Branch 4: STATE_STOPPED
        watch.resume();
        watch.stop();
        long stoppedTime = watch.getTime();
        assertTrue(stoppedTime >= suspendedTime);
    }
}