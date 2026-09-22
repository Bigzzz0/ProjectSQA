package org.apache.commons.math3.ode;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math3.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MaxCountExceededException;
import org.apache.commons.math3.exception.NoBracketingException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.ode.events.EventHandler;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math3.ode.sampling.AbstractStepInterpolator;
import org.apache.commons.math3.ode.sampling.StepHandler;
import org.apache.commons.math3.ode.sampling.StepInterpolator;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.math3.ode.AbstractIntegrator
 *
 * Branch & Scenario Matrix:
 * 1. Constructor: with custom name and default constructor (null name).
 * 2. Step Handlers: addStepHandler, getStepHandlers (unmodifiable), clearStepHandlers.
 * 3. Event Handlers: addEventHandler (with default solver and custom solver), getEventHandlers, clearEventHandlers.
 * 4. Step/Time Inspection: getCurrentStepStart, getCurrentSignedStepsize.
 * 5. Evaluations Counter: setMaxEvaluations (negative -> Integer.MAX_VALUE, non-negative), getMaxEvaluations, getEvaluations,
 *    computeDerivatives exceeding limit (MaxCountExceededException), resetting count during initIntegration.
 * 6. Sanity Checks:
 *    - integrate(FirstOrderDifferentialEquations, ...) with dimension mismatch in y0 vs equations.
 *    - integrate(FirstOrderDifferentialEquations, ...) with dimension mismatch in y vs equations.
 *    - sanityChecks(...) interval too small (|equations.getTime() - t| <= threshold).
 * 7. acceptStep Logic & Branches:
 *    - statesInitialized: false -> triggers state.reinitializeBegin, transitions to true.
 *    - Forward integration (orderingSign = +1) vs Backward integration (orderingSign = -1).
 *    - No occurring events path (straight to last part of step).
 *    - Events occurring:
 *      * Event asks to STOP (isLastStep = true): copies eventY, accepts remaining events, early return.
 *      * Event triggers RESET_STATE / RESET_DERIVATIVES (needReset = true): recomputes derivatives, early return.
 *      * Event CONTINUE (needReset = false): continues step, checks if event reoccurs in remaining interval.
 *    - End of step check: Precision.equals(currentT, tEnd, 1) -> marks isLastStep true.
 *    - StepHandler invocations during intermediate event stops and final step acceptance.
 * 8. Defects4J Known Defect (DormandPrince853IntegratorTest::testEventsScheduling):
 *    - Multiple events occurring close to each other or simultaneously. Event scheduling must properly handle
 *      events occurring concurrently and maintain proper ordering, step acceptance, and state updates without
 *      skipping or misordering pending events.
 * --------------------------------------------------------------------------------------------------------------------
 */
public class AbstractIntegratorGeminiTest {

    // Concrete dummy implementation of AbstractIntegrator for white-box unit testing
    private static class DummyIntegrator extends AbstractIntegrator {
        public DummyIntegrator(String name) {
            super(name);
        }

        public DummyIntegrator() {
            super();
        }

        @Override
        public void integrate(ExpandableStatefulODE equations, double t) {
            this.setEquations(equations);
            this.sanityChecks(equations, t);
        }

        public void publicInitIntegration(double t0, double[] y0, double t) {
            initIntegration(t0, y0, t);
        }

        public void publicSetEquations(ExpandableStatefulODE equations) {
            setEquations(equations);
        }

        public void publicSanityChecks(ExpandableStatefulODE equations, double t) {
            sanityChecks(equations, t);
        }

        public double publicAcceptStep(AbstractStepInterpolator interpolator, double[] y, double[] yDot, double tEnd) {
            return acceptStep(interpolator, y, yDot, tEnd);
        }

        public void publicSetStepStart(double t) {
            this.stepStart = t;
        }

        public void publicSetStepSize(double h) {
            this.stepSize = h;
        }

        public boolean isResetOccurred() {
            return this.resetOccurred;
        }

        public boolean isLastStep() {
            return this.isLastStep;
        }
    }

    // Concrete dummy implementation of AbstractStepInterpolator for step acceptance testing
    private static class DummyStepInterpolator extends AbstractStepInterpolator {
        private double[] testState;

        public DummyStepInterpolator() {
            super();
            this.testState = new double[1];
        }

        public DummyStepInterpolator(double[] y, boolean forward) {
            super();
            this.testState = y.clone();
            this.currentState = y.clone();
            this.interpolatedState = y.clone();
            this.interpolatedDerivatives = new double[y.length];
            setForward(forward);
        }

        public void setBounds(double tPrev, double tCurr) {
            this.previousTime = tPrev;
            this.currentTime = tCurr;
            this.softPreviousTime = tPrev;
            this.softCurrentTime = tCurr;
            this.h = tCurr - tPrev;
        }

        @Override
        protected void computeInterpolatedStateAndDerivatives(double theta, double oneMinusThetaH) {
            if (currentState != null) {
                System.arraycopy(currentState, 0, interpolatedState, 0, currentState.length);
            }
        }

        @Override
        public StepInterpolator doCopy() {
            return this;
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
        }
    }

    /* -------------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * ------------------------------------------------------------------------- */

    @Test(timeout = 4000)
    public void testConstructorsAndName() {
        DummyIntegrator integrator1 = new DummyIntegrator("CustomIntegrator");
        assertEquals("CustomIntegrator", integrator1.getName());
        assertTrue(Double.isNaN(integrator1.getCurrentStepStart()));
        assertTrue(Double.isNaN(integrator1.getCurrentSignedStepsize()));

        DummyIntegrator integrator2 = new DummyIntegrator();
        assertNull(integrator2.getName());
    }

    @Test(timeout = 4000)
    public void testStepHandlerManagement() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        assertEquals(0, integrator.getStepHandlers().size());

        StepHandler handler1 = new StepHandler() {
            public void init(double t0, double[] y0, double t) {}
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}
        };
        integrator.addStepHandler(handler1);

        Collection<StepHandler> handlers = integrator.getStepHandlers();
        assertEquals(1, handlers.size());
        assertTrue(handlers.contains(handler1));

        try {
            handlers.add(handler1);
            fail("Collection returned by getStepHandlers() should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }

        integrator.clearStepHandlers();
        assertEquals(0, integrator.getStepHandlers().size());
    }

    @Test(timeout = 4000)
    public void testEventHandlerManagement() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        assertEquals(0, integrator.getEventHandlers().size());

        EventHandler handler1 = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return 0; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.CONTINUE; }
            public void resetState(double t, double[] y) {}
        };

        integrator.addEventHandler(handler1, 1.0, 1e-4, 100);
        assertEquals(1, integrator.getEventHandlers().size());

        integrator.addEventHandler(handler1, 1.0, 1e-4, 100, new BracketingNthOrderBrentSolver(1e-4, 5));
        assertEquals(2, integrator.getEventHandlers().size());

        Collection<EventHandler> eventHandlers = integrator.getEventHandlers();
        try {
            eventHandlers.add(handler1);
            fail("Collection returned by getEventHandlers() should be unmodifiable");
        } catch (UnsupportedOperationException expected) {
            // expected
        }

        integrator.clearEventHandlers();
        assertEquals(0, integrator.getEventHandlers().size());
    }

    @Test(timeout = 4000)
    public void testStepSizeAndStepStartGetters() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        integrator.publicSetStepStart(2.5);
        integrator.publicSetStepSize(0.125);
        assertEquals(2.5, integrator.getCurrentStepStart(), 1e-15);
        assertEquals(0.125, integrator.getCurrentSignedStepsize(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testInitIntegrationInitializesHandlers() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        final boolean[] handlerInited = new boolean[] { false, false };

        integrator.addStepHandler(new StepHandler() {
            public void init(double t0, double[] y0, double t) {
                handlerInited[0] = true;
            }
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}
        });

        integrator.addEventHandler(new EventHandler() {
            public void init(double t0, double[] y0, double t) {
                handlerInited[1] = true;
            }
            public double g(double t, double[] y) { return 0; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.CONTINUE; }
            public void resetState(double t, double[] y) {}
        }, 1.0, 1e-3, 10);

        integrator.publicInitIntegration(0.0, new double[] { 1.0 }, 10.0);
        assertTrue(handlerInited[0]);
        assertTrue(handlerInited[1]);
        assertEquals(0, integrator.getEvaluations());
    }

    /* -------------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * ------------------------------------------------------------------------- */

    @Test(timeout = 4000)
    public void testMaxEvaluationsBoundaries() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        // default was setMaxEvaluations(-1) -> Integer.MAX_VALUE
        assertEquals(Integer.MAX_VALUE, integrator.getMaxEvaluations());

        integrator.setMaxEvaluations(-5);
        assertEquals(Integer.MAX_VALUE, integrator.getMaxEvaluations());

        integrator.setMaxEvaluations(0);
        assertEquals(0, integrator.getMaxEvaluations());

        integrator.setMaxEvaluations(50);
        assertEquals(50, integrator.getMaxEvaluations());
    }

    @Test(timeout = 4000)
    public void testSanityChecksIntervalTooSmall() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) { yDot[0] = 0; }
        };
        ExpandableStatefulODE expandable = new ExpandableStatefulODE(ode);
        expandable.setTime(10.0);
        expandable.setPrimaryState(new double[] { 1.0 });

        // Identical time -> dt = 0 <= threshold
        try {
            integrator.publicSanityChecks(expandable, 10.0);
            fail("Expected NumberIsTooSmallException for t == t0");
        } catch (NumberIsTooSmallException e) {
            // Success
        }

        // Extremely close time within 1000 * ulp
        double tinyOffset = Math.ulp(10.0);
        try {
            integrator.publicSanityChecks(expandable, 10.0 + tinyOffset);
            fail("Expected NumberIsTooSmallException for dt <= threshold");
        } catch (NumberIsTooSmallException e) {
            // Success
        }

        // Outside threshold -> should pass
        integrator.publicSanityChecks(expandable, 10.0 + 1.0);
    }

    /* -------------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone (DormandPrince853 / Events Scheduling)
     * ------------------------------------------------------------------------- */

    /**
     * Targets the known defect where event scheduling fails to execute in proper
     * chronological sequence when events occur close together or multiple events exist.
     */
    @Test(timeout = 4000)
    public void testEventsSchedulingDefect() {
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };

        // DormandPrince853 uses AbstractIntegrator.acceptStep under the hood
        DormandPrince853Integrator integ = new DormandPrince853Integrator(1e-4, 100.0, 1e-6, 1e-6);

        final List<Double> eventsTriggered = new ArrayList<Double>();

        EventHandler event1 = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) {
                return t - 8.0;
            }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                eventsTriggered.add(t);
                return Action.CONTINUE;
            }
            public void resetState(double t, double[] y) {}
        };

        EventHandler event2 = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) {
                return t - 8.5;
            }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                eventsTriggered.add(t);
                return Action.CONTINUE;
            }
            public void resetState(double t, double[] y) {}
        };

        integ.addEventHandler(event1, 1.0, 1e-8, 100);
        integ.addEventHandler(event2, 1.0, 1e-8, 100);

        double[] y = new double[] { 0.0 };
        integ.integrate(ode, 0.0, y, 10.0, y);

        assertEquals(2, eventsTriggered.size());
        assertEquals(8.0, eventsTriggered.get(0), 1e-6);
        assertEquals(8.5, eventsTriggered.get(1), 1e-6);
        assertEquals(10.0, y[0], 1e-6);
    }

    /* -------------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * ------------------------------------------------------------------------- */

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testIntegrateDimensionMismatchY0() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 2; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {}
        };
        integrator.integrate(ode, 0.0, new double[1], 1.0, new double[2]);
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testIntegrateDimensionMismatchY() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 2; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {}
        };
        integrator.integrate(ode, 0.0, new double[2], 1.0, new double[1]);
    }

    @Test(expected = MaxCountExceededException.class, timeout = 4000)
    public void testComputeDerivativesExceedsMaxEvaluations() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        integrator.setMaxEvaluations(2);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 2.0;
            }
        };

        ExpandableStatefulODE expandable = new ExpandableStatefulODE(ode);
        expandable.setTime(0.0);
        expandable.setPrimaryState(new double[] { 0.0 });
        integrator.publicSetEquations(expandable);

        double[] y = new double[1];
        double[] yDot = new double[1];

        assertEquals(0, integrator.getEvaluations());
        integrator.computeDerivatives(0.0, y, yDot);
        assertEquals(1, integrator.getEvaluations());
        integrator.computeDerivatives(0.5, y, yDot);
        assertEquals(2, integrator.getEvaluations());
        // 3rd call must exceed max evaluations (2)
        integrator.computeDerivatives(1.0, y, yDot);
    }

    @Test(timeout = 4000)
    public void testIntegrateNormalCompletionCopiesState() {
        DummyIntegrator integrator = new DummyIntegrator("test") {
            @Override
            public void integrate(ExpandableStatefulODE equations, double t) {
                equations.setTime(t);
                equations.setPrimaryState(new double[] { 42.0 });
            }
        };

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {}
        };

        double[] y = new double[1];
        double tFinal = integrator.integrate(ode, 0.0, new double[] { 0.0 }, 5.0, y);
        assertEquals(5.0, tFinal, 1e-15);
        assertEquals(42.0, y[0], 1e-15);
    }

    /* -------------------------------------------------------------------------
     * Partition E: acceptStep Deep White-Box & Event Handling Branches
     * ------------------------------------------------------------------------- */

    @Test(timeout = 4000)
    public void testAcceptStepNoEventsForwardAndBackward() {
        DummyIntegrator integrator = new DummyIntegrator("test");
        final boolean[] stepHandled = new boolean[] { false };

        integrator.addStepHandler(new StepHandler() {
            public void init(double t0, double[] y0, double t) {}
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                stepHandled[0] = true;
                assertTrue(isLast);
            }
        });

        // Forward integration: tEnd == currentTime
        DummyStepInterpolator interpolatorForward = new DummyStepInterpolator(new double[] { 1.0 }, true);
        interpolatorForward.setBounds(0.0, 1.0);

        double[] y = new double[1];
        double[] yDot = new double[1];
        double tResult = integrator.publicAcceptStep(interpolatorForward, y, yDot, 1.0);

        assertEquals(1.0, tResult, 1e-15);
        assertTrue(stepHandled[0]);
        assertTrue(integrator.isLastStep());

        // Backward integration: isForward = false
        stepHandled[0] = false;
        DummyStepInterpolator interpolatorBackward = new DummyStepInterpolator(new double[] { 1.0 }, false);
        interpolatorBackward.setBounds(1.0, 0.0);
        double tBack = integrator.publicAcceptStep(interpolatorBackward, y, yDot, 0.0);
        assertEquals(0.0, tBack, 1e-15);
        assertTrue(stepHandled[0]);
    }

    @Test(timeout = 4000)
    public void testAcceptStepWithEventStop() {
        DummyIntegrator integrator = new DummyIntegrator("test");

        EventHandler stopHandler = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) {
                return t - 0.5; // root at 0.5
            }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }
            public void resetState(double t, double[] y) {}
        };

        integrator.addEventHandler(stopHandler, 0.1, 1e-6, 100);

        DummyStepInterpolator interpolator = new DummyStepInterpolator(new double[] { 2.0 }, true);
        interpolator.setBounds(0.0, 1.0);

        double[] y = new double[] { 0.0 };
        double[] yDot = new double[1];
        double stopTime = integrator.publicAcceptStep(interpolator, y, yDot, 1.0);

        assertEquals(0.5, stopTime, 1e-5);
        assertTrue(integrator.isLastStep());
        assertEquals(2.0, y[0], 1e-15);
    }

    @Test(timeout = 4000)
    public void testAcceptStepWithResetState() {
        DummyIntegrator integrator = new DummyIntegrator("test");

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 99.0;
            }
        };
        ExpandableStatefulODE expandable = new ExpandableStatefulODE(ode);
        expandable.setTime(0.0);
        expandable.setPrimaryState(new double[] { 0.0 });
        integrator.publicSetEquations(expandable);

        EventHandler resetHandler = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) {
                return t - 0.4;
            }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.RESET_STATE;
            }
            public void resetState(double t, double[] y) {
                y[0] = 77.0;
            }
        };

        integrator.addEventHandler(resetHandler, 0.1, 1e-6, 100);

        DummyStepInterpolator interpolator = new DummyStepInterpolator(new double[] { 10.0 }, true);
        interpolator.setBounds(0.0, 1.0);

        double[] y = new double[1];
        double[] yDot = new double[1];
        double resetTime = integrator.publicAcceptStep(interpolator, y, yDot, 1.0);

        assertEquals(0.4, resetTime, 1e-5);
        assertTrue(integrator.isResetOccurred());
        assertEquals(77.0, y[0], 1e-5);
        assertEquals(99.0, yDot[0], 1e-5);
    }

    @Test(timeout = 4000)
    public void testAcceptStepWithResetDerivatives() {
        DummyIntegrator integrator = new DummyIntegrator("test");

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() { return 1; }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 123.0;
            }
        };
        ExpandableStatefulODE expandable = new ExpandableStatefulODE(ode);
        expandable.setTime(0.0);
        expandable.setPrimaryState(new double[] { 0.0 });
        integrator.publicSetEquations(expandable);

        EventHandler resetDerivHandler = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) {
                return t - 0.6;
            }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.RESET_DERIVATIVES;
            }
            public void resetState(double t, double[] y) {}
        };

        integrator.addEventHandler(resetDerivHandler, 0.1, 1e-6, 100);

        DummyStepInterpolator interpolator = new DummyStepInterpolator(new double[] { 5.0 }, true);
        interpolator.setBounds(0.0, 1.0);

        double[] y = new double[1];
        double[] yDot = new double[1];
        double resetTime = integrator.publicAcceptStep(interpolator, y, yDot, 1.0);

        assertEquals(0.6, resetTime, 1e-5);
        assertTrue(integrator.isResetOccurred());
        assertEquals(123.0, yDot[0], 1e-5);
    }
}