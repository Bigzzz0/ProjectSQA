package org.apache.commons.math.ode.events;

/* [Branch & Defect Analysis Matrix]
 * ------------------------------------------------------------------------------------------------------
 * Branch / Condition                                | Target Method / Partition     | Tested In Method
 * ------------------------------------------------------------------------------------------------------
 * Constructor abs(convergence)                      | Partition B: BVA              | testConstructorAbsConvergence
 * Getters initial dummy values & accessors          | Partition A: Core Logic       | testConstructorAndGetters
 * reinitializeBegin (g0 >= 0 vs g0 < 0)             | Partition A: Core Logic       | testReinitializeBeginSigns
 * reinitializeBegin EventException propagation      | Partition D: Defensive Paths  | testReinitializeBeginPropagatesException
 * evaluateStep: forward integration (isForward=true)| Partition A: Core Logic       | testEvaluateStepForwardSingleEvent
 * evaluateStep: backward integration (isForward=false)| Partition A: Core Logic     | testEvaluateStepBackwardIntegration
 * evaluateStep: solver root (ta <= tb vs ta > tb)   | Partition A / B: Boundaries   | testEvaluateStepBackwardIntegration
 * evaluateStep: no sign change across substeps      | Partition A: Core Logic       | testEvaluateStepNoSignChange
 * evaluateStep: multiple substeps (n > 1)           | Partition A: Core Logic       | testEvaluateStepMultipleSubsteps
 * evaluateStep: duplicate past event near ta/prev   | Partition B: Boundaries       | testPastEventIgnoredNearTaAndPreviousEvent
 * evaluateStep: pendingEvent re-evaluated at t1     | Partition B: Boundaries       | testPendingEventAcceptedWhenStepEndsAtEvent
 * evaluateStep: DerivativeException unwrapped       | Partition D: Defensive Paths  | testEvaluateStepUnwrapsDerivativeException
 * evaluateStep: EventException unwrapped in solver  | Partition D: Defensive Paths  | testEvaluateStepUnwrapsEventException
 * evaluateStep: ConvergenceException exceeded       | Partition D: Defensive Paths  | testEvaluateStepConvergenceException
 * stepAccepted: pendingEvent true vs false          | Partition A: Core Logic       | testStepAcceptedPendingEventVariants
 * stepAccepted: eventOccurred increasing flag logic | Partition A: Core Logic       | testStepAcceptedIncreasingFlagCombinations
 * stop() query based on nextAction                  | Partition A: Core Logic       | testStopQuery
 * reset() with !pendingEvent                        | Partition A: Core Logic       | testResetWithoutPendingEvent
 * reset() with RESET_STATE vs RESET_DERIVATIVES     | Partition A: Core Logic       | testResetStateAndDerivatives
 * reset() with CONTINUE / STOP                      | Partition A: Core Logic       | testResetContinueOrStop
 * DEFECT DEFECTS4J (closeEvents bracketing failure) | Partition C: Defect Target    | testCloseEventsDefect
 * ------------------------------------------------------------------------------------------------------
 */

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.ConvergenceException;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.sampling.DummyStepInterpolator;

public class EventStateGeminiTest {

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAndGetters() {
        EventHandler handler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return EventHandler.CONTINUE;
            }
            public double g(double t, double[] y) {
                return 0.0;
            }
            public void resetState(double t, double[] y) {}
        };

        EventState es = new EventState(handler, 12.5, 1e-6, 150);

        assertSame(handler, es.getEventHandler());
        assertEquals(12.5, es.getMaxCheckInterval(), 1e-12);
        assertEquals(1e-6, es.getConvergence(), 1e-12);
        assertEquals(150, es.getMaxIterationCount());
        assertTrue(Double.isNaN(es.getEventTime()));
        assertFalse(es.stop());
    }

    @Test(timeout = 4000)
    public void testReinitializeBeginSigns() throws EventException {
        EventHandler positiveHandler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) { return EventHandler.CONTINUE; }
            public double g(double t, double[] y) { return 5.0; }
            public void resetState(double t, double[] y) {}
        };
        EventState esPositive = new EventState(positiveHandler, 10.0, 1e-6, 100);
        esPositive.reinitializeBegin(0.0, new double[] { 1.0 });

        EventHandler negativeHandler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) { return EventHandler.CONTINUE; }
            public double g(double t, double[] y) { return -3.0; }
            public void resetState(double t, double[] y) {}
        };
        EventState esNegative = new EventState(negativeHandler, 10.0, 1e-6, 100);
        esNegative.reinitializeBegin(0.0, new double[] { 1.0 });
    }

    @Test(timeout = 4000)
    public void testEvaluateStepNoSignChange() throws Exception {
        EventHandler handler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) { return EventHandler.CONTINUE; }
            public double g(double t, double[] y) { return 10.0; } // constant positive
            public void resetState(double t, double[] y) {}
        };

        EventState es = new EventState(handler, 5.0, 1e-6, 100);
        DummyStepInterpolator interpolator = new DummyStepInterpolator(new double[] { 0.0 }, true);
        interpolator.storeTime(0.0);
        es.reinitializeBegin(0.0, new double[] { 0.0 });

        interpolator.shift();
        interpolator.storeTime(20.0);

        boolean eventTriggered = es.evaluateStep(interpolator);
        assertFalse(eventTriggered);
        assertTrue(Double.isNaN(es.getEventTime()));

        // Also test stepAccepted when pendingEvent is false
        es.stepAccepted(20.0, new double[] { 0.0 });
        assertFalse(es.stop());
    }

    @Test(timeout = 4000)
    public void testEvaluateStepForwardSingleEvent() throws Exception {
        EventHandler handler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) { return EventHandler.CONTINUE; }
            public double g(double t, double[] y) { return t - 3.5; }
            public void resetState(double t, double[] y) {}
        };

        EventState es = new EventState(handler, 10.0, 1e-6, 100);
        DummyStepInterpolator interpolator = new DummyStepInterpolator(new double[] { 0.0 }, true);
        interpolator.storeTime(0.0);
        es.reinitializeBegin(0.0, new double[] { 0.0 });

        interpolator.shift();
        interpolator.storeTime(5.0);

        boolean eventTriggered = es.evaluateStep(interpolator);
        assertTrue(eventTriggered);
        assertEquals(3.5, es.getEventTime(), 1e-5);
    }

    @Test(timeout = 4000)
    public void testEvaluateStepMultipleSubsteps() throws Exception {
        EventHandler handler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) { return EventHandler.CONTINUE; }
            // Root at 25.0
            public double g(double t, double[] y) { return t - 25.0; }
            public void resetState(double t, double[] y) {}
        };

        // maxCheckInterval = 5.0, step interval = 30.0 -> n = 6 substeps
        EventState es = new EventState(handler, 5.0, 1e-6, 100);
        DummyStepInterpolator interpolator = new DummyStepInterpolator(new double[] { 0.0 }, true);
        interpolator.storeTime(0.0);
        es.reinitializeBegin(0.0, new double[] { 0.0 });

        interpolator.shift();
        interpolator.storeTime(30.0);

        boolean eventTriggered = es.evaluateStep(interpolator);
        assertTrue(eventTriggered);
        assertEquals(25.0, es.getEventTime(), 1e-5);
    }

    @Test(timeout = 4000)
    public void testEvaluateStepBackwardIntegration() throws Exception {
        EventHandler handler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) { return EventHandler.CONTINUE; }
            public double g(double t, double[] y) { return t - 45.0; }
            public void resetState(double t, double[] y) {}
        };

        EventState es = new EventState(handler, 10.0, 1e-6, 100);
        DummyStepInterpolator interpolator = new DummyStepInterpolator(new double[] { 0.0 }, false);
        interpolator.storeTime(100.0);
        es.reinitializeBegin(100.0, new double[] { 0.0 });

        interpolator.shift();
        interpolator.storeTime(0.0);

        // backward step triggers ta > tb branch in evaluateStep: solver.solve(f, tb, ta)
        boolean eventTriggered = es.evaluateStep(interpolator);
        assertTrue(eventTriggered);
        assertEquals(45.0, es.getEventTime(), 1e-5);

        es.stepAccepted(es.getEventTime(), new double[] { 0.0 });
        assertFalse(es.stop());
    }

    @Test(timeout = 4000)
    public void testStepAcceptedPendingEventVariants() throws Exception {
        final int[] actionToReturn = new int[] { EventHandler.CONTINUE };
        EventHandler handler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return actionToReturn[0];
            }
            public double g(double t, double[] y) { return t - 5.0; }
            public void resetState(double t, double[] y) {}
        };

        EventState es = new EventState(handler, 10.0, 1e-6, 100);
        DummyStepInterpolator interpolator = new DummyStepInterpolator(new double[] { 0.0 }, true);
        interpolator.storeTime(0.0);
        es.reinitializeBegin(0.0, new double[] { 0.0 });

        interpolator.shift();
        interpolator.storeTime(10.0);
        es.evaluateStep(interpolator);

        actionToReturn[0] = EventHandler.STOP;
        es.stepAccepted(5.0, new double[] { 0.0 });
        assertTrue(es.stop());
    }

    @Test(timeout = 4000)
    public void testStepAcceptedIncreasingFlagCombinations() throws Exception {
        final boolean[] receivedIncreasing = new boolean[1];

        // Case 1: forward = true, increasing = true (g goes from negative to positive)
        EventHandler handlerIncreasing = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                receivedIncreasing[0] = increasing;
                return EventHandler.CONTINUE;
            }
            public double g(double t, double[] y) { return t - 5.0; }
            public void resetState(double t, double[] y) {}
        };
        EventState es1 = new EventState(handlerIncreasing, 10.0, 1e-6, 100);
        DummyStepInterpolator interp1 = new DummyStepInterpolator(new double[] { 0.0 }, true);
        interp1.storeTime(0.0);
        es1.reinitializeBegin(0.0, new double[] { 0.0 });
        interp1.shift();
        interp1.storeTime(10.0);
        es1.evaluateStep(interp1);
        es1.stepAccepted(5.0, new double[] { 0.0 });
        assertTrue(receivedIncreasing[0]);

        // Case 2: forward = true, increasing = false (g goes from positive to negative)
        EventHandler handlerDecreasing = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                receivedIncreasing[0] = increasing;
                return EventHandler.CONTINUE;
            }
            public double g(double t, double[] y) { return 5.0 - t; }
            public void resetState(double t, double[] y) {}
        };
        EventState es2 = new EventState(handlerDecreasing, 10.0, 1e-6, 100);
        DummyStepInterpolator interp2 = new DummyStepInterpolator(new double[] { 0.0 }, true);
        interp2.storeTime(0.0);
        es2.reinitializeBegin(0.0, new double[] { 0.0 });
        interp2.shift();
        interp2.storeTime(10.0);
        es2.evaluateStep(interp2);
        es2.stepAccepted(5.0, new double[] { 0.0 });
        assertFalse(receivedIncreasing[0]);
    }

    @Test(timeout = 4000)
    public void testStopQuery() throws Exception {
        EventHandler stopHandler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) { return EventHandler.STOP; }
            public double g(double t, double[] y) { return t - 2.0; }
            public void resetState(double t, double[] y) {}
        };
        EventState es = new EventState(stopHandler, 10.0, 1e-6, 100);
        DummyStepInterpolator interpolator = new DummyStepInterpolator(new double[] { 0.0 }, true);
        interpolator.storeTime(0.0);
        es.reinitializeBegin(0.0, new double[] { 0.0 });
        interpolator.shift();
        interpolator.storeTime(5.0);
        es.evaluateStep(interpolator);

        assertFalse(es.stop());
        es.stepAccepted(2.0, new double[] { 0.0 });
        assertTrue(es.stop());
    }

    @Test(timeout = 4000)
    public void testResetWithoutPendingEvent() throws EventException {
        EventHandler handler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) { return EventHandler.CONTINUE; }
            public double g(double t, double[] y) { return 1.0; }
            public void resetState(double t, double[] y) {}
        };
        EventState es = new EventState(handler, 10.0, 1e-6, 100);
        assertFalse(es.reset(0.0, new double[] { 0.0 }));
    }

    @Test(timeout = 4000)
    public void testResetStateAndDerivatives() throws Exception {
        final boolean[] stateResetCalled = new boolean[1];
        EventHandler resetStateHandler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return EventHandler.RESET_STATE;
            }
            public double g(double t, double[] y) { return t - 5.0; }
            public void resetState(double t, double[] y) {
                stateResetCalled[0] = true;
                y[0] = 99.0;
            }
        };

        EventState esState = new EventState(resetStateHandler, 10.0, 1e-6, 100);
        DummyStepInterpolator interp1 = new DummyStepInterpolator(new double[] { 0.0 }, true);
        interp1.storeTime(0.0);
        esState.reinitializeBegin(0.0, new double[] { 0.0 });
        interp1.shift();
        interp1.storeTime(10.0);
        esState.evaluateStep(interp1);
        esState.stepAccepted(5.0, new double[] { 0.0 });

        double[] y1 = new double[] { 0.0 };
        boolean resetResult1 = esState.reset(5.0, y1);
        assertTrue(resetResult1);
        assertTrue(stateResetCalled[0]);
        assertEquals(99.0, y1[0], 1e-12);
        assertTrue(Double.isNaN(esState.getEventTime()));

        // Now test RESET_DERIVATIVES (resetState must not be called)
        EventHandler resetDerivHandler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return EventHandler.RESET_DERIVATIVES;
            }
            public double g(double t, double[] y) { return t - 5.0; }
            public void resetState(double t, double[] y) {
                fail("resetState must not be called for RESET_DERIVATIVES");
            }
        };
        EventState esDeriv = new EventState(resetDerivHandler, 10.0, 1e-6, 100);
        DummyStepInterpolator interp2 = new DummyStepInterpolator(new double[] { 0.0 }, true);
        interp2.storeTime(0.0);
        esDeriv.reinitializeBegin(0.0, new double[] { 0.0 });
        interp2.shift();
        interp2.storeTime(10.0);
        esDeriv.evaluateStep(interp2);
        esDeriv.stepAccepted(5.0, new double[] { 0.0 });

        boolean resetResult2 = esDeriv.reset(5.0, new double[] { 0.0 });
        assertTrue(resetResult2);
        assertTrue(Double.isNaN(esDeriv.getEventTime()));
    }

    @Test(timeout = 4000)
    public void testResetContinueOrStop() throws Exception {
        EventHandler continueHandler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return EventHandler.CONTINUE;
            }
            public double g(double t, double[] y) { return t - 5.0; }
            public void resetState(double t, double[] y) {}
        };

        EventState es = new EventState(continueHandler, 10.0, 1e-6, 100);
        DummyStepInterpolator interpolator = new DummyStepInterpolator(new double[] { 0.0 }, true);
        interpolator.storeTime(0.0);
        es.reinitializeBegin(0.0, new double[] { 0.0 });
        interpolator.shift();
        interpolator.storeTime(10.0);
        es.evaluateStep(interpolator);
        es.stepAccepted(5.0, new double[] { 0.0 });

        boolean resetResult = es.reset(5.0, new double[] { 0.0 });
        assertFalse(resetResult);
        assertTrue(Double.isNaN(es.getEventTime()));
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorAbsConvergence() {
        EventHandler handler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) { return EventHandler.CONTINUE; }
            public double g(double t, double[] y) { return 0.0; }
            public void resetState(double t, double[] y) {}
        };

        EventState es = new EventState(handler, 1.0, -0.05, 10);
        assertEquals(0.05, es.getConvergence(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testPendingEventAcceptedWhenStepEndsAtEvent() throws Exception {
        EventHandler handler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) { return EventHandler.CONTINUE; }
            public double g(double t, double[] y) { return t - 5.0; }
            public void resetState(double t, double[] y) {}
        };

        EventState es = new EventState(handler, 10.0, 1e-4, 100);
        DummyStepInterpolator interpolator = new DummyStepInterpolator(new double[] { 0.0 }, true);
        interpolator.storeTime(0.0);
        es.reinitializeBegin(0.0, new double[] { 0.0 });

        // First proposal: step ends at 10.0, event found at 5.0
        interpolator.shift();
        interpolator.storeTime(10.0);
        boolean firstCall = es.evaluateStep(interpolator);
        assertTrue(firstCall);
        double eventTime = es.getEventTime();
        assertEquals(5.0, eventTime, 1e-4);

        // Second proposal: integrator reduces step to end exactly at the event time
        DummyStepInterpolator shortStep = new DummyStepInterpolator(new double[] { 0.0 }, true);
        shortStep.storeTime(0.0);
        shortStep.shift();
        shortStep.storeTime(eventTime);

        // Sub-branch: pendingEvent && (Math.abs(t1 - pendingEventTime) <= convergence) -> returns false
        boolean secondCall = es.evaluateStep(shortStep);
        assertFalse(secondCall);
    }

    @Test(timeout = 4000)
    public void testPastEventIgnoredNearTaAndPreviousEvent() throws Exception {
        final double root1 = 10.0;
        final double root2 = 25.0;

        EventHandler handler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) { return EventHandler.CONTINUE; }
            public double g(double t, double[] y) {
                // negative before 10, positive between 10 and 25, negative after 25
                return (t - root1) * (root2 - t);
            }
            public void resetState(double t, double[] y) {}
        };

        final double convergence = 0.05;
        EventState es = new EventState(handler, 20.0, convergence, 100);
        DummyStepInterpolator interpolator = new DummyStepInterpolator(new double[] { 0.0 }, true);
        interpolator.storeTime(0.0);
        es.reinitializeBegin(0.0, new double[] { 0.0 });

        // Step 1: find root at 10.0
        interpolator.shift();
        interpolator.storeTime(15.0);
        boolean event1 = es.evaluateStep(interpolator);
        assertTrue(event1);
        double foundRoot1 = es.getEventTime();
        assertEquals(root1, foundRoot1, convergence);

        es.stepAccepted(foundRoot1, new double[] { 0.0 });

        // Step 2: proposed from foundRoot1 to 30.0
        // Near ta, the root1 is found again, which matches previousEventTime within convergence
        interpolator.shift();
        interpolator.storeTime(30.0);
        boolean event2 = es.evaluateStep(interpolator);
        assertTrue(event2);
        assertEquals(root2, es.getEventTime(), convergence);
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the documented Defects4J failure:
     * org.apache.commons.math.ode.events.EventStateTest::closeEvents
     * Failure symptom:
     * MathRuntimeException: function values at endpoints do not have different signs.
     * Endpoints: [89.999, 153.1], Values: [-0.066, -1,142.11]
     */
    @Test(timeout = 4000)
    public void testCloseEventsDefect() throws Exception {
        final double r1 = 90.0;
        final double r2 = 135.0;
        EventHandler closeHandler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return EventHandler.CONTINUE;
            }
            public double g(double t, double[] y) {
                return (t - r1) * (r2 - t);
            }
            public void resetState(double t, double[] y) {}
        };

        final double tolerance = 0.1;
        EventState es = new EventState(closeHandler, 15.0, tolerance, 100);

        DummyStepInterpolator interpolator = new DummyStepInterpolator(new double[] { 0.0 }, true);
        interpolator.storeTime(0.0);
        es.reinitializeBegin(0.0, new double[] { 0.0 });

        // Step 1 to 153.1: should detect first root around 90.0
        interpolator.shift();
        interpolator.storeTime(153.1);
        boolean step1Event = es.evaluateStep(interpolator);
        assertTrue(step1Event);
        assertEquals(r1, es.getEventTime(), tolerance);
        es.stepAccepted(es.getEventTime(), new double[] { 0.0 });

        // Step 2 to 153.1: on defective implementation, ta and tb endpoints have the same sign
        // triggering MathRuntimeException in BrentSolver. The fixed version brackets and finds r2.
        interpolator.shift();
        interpolator.storeTime(153.1);
        boolean step2Event = es.evaluateStep(interpolator);
        assertTrue(step2Event);
        assertEquals(r2, es.getEventTime(), tolerance);
        es.stepAccepted(es.getEventTime(), new double[] { 0.0 });
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    private static class FaultyStepInterpolator extends DummyStepInterpolator {
        private static final long serialVersionUID = 1L;
        private boolean failOnState = false;

        public FaultyStepInterpolator(double[] y, boolean forward) {
            super(y, forward);
        }

        @Override
        public double[] getInterpolatedState() throws DerivativeException {
            if (failOnState) {
                throw new DerivativeException(new RuntimeException("Simulated derivative failure"));
            }
            return super.getInterpolatedState();
        }
    }

    @Test(expected = DerivativeException.class, timeout = 4000)
    public void testEvaluateStepUnwrapsDerivativeException() throws Exception {
        EventHandler handler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) { return EventHandler.CONTINUE; }
            public double g(double t, double[] y) { return t - 5.0; }
            public void resetState(double t, double[] y) {}
        };

        EventState es = new EventState(handler, 10.0, 1e-6, 100);
        FaultyStepInterpolator interpolator = new FaultyStepInterpolator(new double[] { 0.0 }, true);
        interpolator.storeTime(0.0);
        es.reinitializeBegin(0.0, new double[] { 0.0 });

        interpolator.shift();
        interpolator.storeTime(10.0);

        // Enable failure when BrentSolver queries the state function
        interpolator.failOnState = true;
        es.evaluateStep(interpolator);
    }

    @Test(expected = EventException.class, timeout = 4000)
    public void testEvaluateStepUnwrapsEventException() throws Exception {
        EventHandler handler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) { return EventHandler.CONTINUE; }
            public double g(double t, double[] y) throws EventException {
                // Allow evaluation at 0.0 and 10.0 so bracketing starts, then fail inside BrentSolver
                if (t > 1.0 && t < 9.0) {
                    throw new EventException(new RuntimeException("Simulated event evaluation failure"));
                }
                return t - 5.0;
            }
            public void resetState(double t, double[] y) {}
        };

        EventState es = new EventState(handler, 10.0, 1e-6, 100);
        DummyStepInterpolator interpolator = new DummyStepInterpolator(new double[] { 0.0 }, true);
        interpolator.storeTime(0.0);
        es.reinitializeBegin(0.0, new double[] { 0.0 });

        interpolator.shift();
        interpolator.storeTime(10.0);
        es.evaluateStep(interpolator);
    }

    @Test(expected = EventException.class, timeout = 4000)
    public void testReinitializeBeginPropagatesException() throws EventException {
        EventHandler handler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) { return EventHandler.CONTINUE; }
            public double g(double t, double[] y) throws EventException {
                throw new EventException(new RuntimeException("Reinitialize failure"));
            }
            public void resetState(double t, double[] y) {}
        };

        EventState es = new EventState(handler, 10.0, 1e-6, 100);
        es.reinitializeBegin(0.0, new double[] { 0.0 });
    }

    @Test(expected = ConvergenceException.class, timeout = 4000)
    public void testEvaluateStepConvergenceException() throws Exception {
        EventHandler handler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) { return EventHandler.CONTINUE; }
            public double g(double t, double[] y) { return t - 5.0; }
            public void resetState(double t, double[] y) {}
        };

        // maxIterationCount = 1 forces BrentSolver to exceed iterations
        EventState es = new EventState(handler, 10.0, 1e-15, 1);
        DummyStepInterpolator interpolator = new DummyStepInterpolator(new double[] { 0.0 }, true);
        interpolator.storeTime(0.0);
        es.reinitializeBegin(0.0, new double[] { 0.0 });

        interpolator.shift();
        interpolator.storeTime(10.0);
        es.evaluateStep(interpolator);
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & State Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testInitialStateIntegrity() {
        EventHandler handler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) { return EventHandler.CONTINUE; }
            public double g(double t, double[] y) { return 0.0; }
            public void resetState(double t, double[] y) {}
        };

        EventState es = new EventState(handler, 10.0, 1e-6, 100);
        assertTrue(Double.isNaN(es.getEventTime()));
        assertFalse(es.stop());
    }
}