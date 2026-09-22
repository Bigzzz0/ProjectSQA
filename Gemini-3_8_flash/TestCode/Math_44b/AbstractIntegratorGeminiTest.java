package org.apache.commons.math.ode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math.analysis.solvers.BracketingNthOrderBrentSolver;
import org.apache.commons.math.analysis.solvers.BrentSolver;
import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.MaxCountExceededException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math.ode.nonstiff.EulerIntegrator;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.apache.commons.math.util.FastMath;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.math.ode.AbstractIntegrator
 *
 * 1. Targeted Decisions & Branches:
 *    - Constructor variants (named vs default null name).
 *    - setMaxEvaluations: maxEvaluations < 0 ? Integer.MAX_VALUE : maxEvaluations (branch T/F).
 *    - computeDerivatives: increment evaluation counter, delegate to expandable, throw MaxCountExceededException.
 *    - integrate(FirstOrderDifferentialEquations, t0, y0, t, y):
 *        * y0.length != equations.getDimension() -> DimensionMismatchException.
 *        * y.length != equations.getDimension() -> DimensionMismatchException.
 *    - sanityChecks(ExpandableStatefulODE, t):
 *        * dt <= threshold -> NumberIsTooSmallException (T/F boundary at threshold).
 *    - StepHandlers & EventHandlers management:
 *        * add, get (unmodifiable collection check), clear.
 *        * addEventHandler with default BracketingNthOrderBrentSolver vs custom UnivariateRealSolver.
 *    - acceptStep:
 *        * statesInitialized: false -> lazy reinitializeBegin, true -> skipped.
 *        * orderingSign: forward (+1) vs backward (-1).
 *        * occuringEvents: empty vs multiple sorted events chronologically.
 *        * isLastStep: currentEvent.stop() vs Precision.equals(currentT, tEnd, 1).
 *        * currentEvent.reset(eventT, eventY): true -> recompute derivatives, resetOccurred = true.
 *        * evaluateStep on remaining part of the step: event occurs again -> re-added to occuringEvents.
 *
 * 2. Ground Truth Defect Targeted:
 *    - Defect ID: MATH-695 / testIssue695
 *    - Symptoms: AssertionFailedError: going backard in time! (7.796578226186635 < 10.99)
 *    - Cause: EventState reset and step re-evaluation in acceptStep leading to backwards time evaluation.
 */
public class AbstractIntegratorGeminiTest {

    // Concrete mock subclass to expose protected methods of AbstractIntegrator for white-box testing
    private static class DummyIntegrator extends AbstractIntegrator {

        public DummyIntegrator(String name) {
            super(name);
        }

        public DummyIntegrator() {
            super();
        }

        @Override
        public void integrate(ExpandableStatefulODE equations, double t) {
            setEquations(equations);
            sanityChecks(equations, t);
        }

        public void callSanityChecks(ExpandableStatefulODE equations, double t) {
            sanityChecks(equations, t);
        }

        public void callSetEquations(ExpandableStatefulODE equations) {
            setEquations(equations);
        }

        public void callResetEvaluations() {
            resetEvaluations();
        }

        public void callSetStateInitialized(boolean stateInitialized) {
            setStateInitialized(stateInitialized);
        }
    }

    // Simple test ODE: dy/dt = 1.0, y(0) = 0
    private static class LinearODE implements FirstOrderDifferentialEquations {
        private final int dimension;

        public LinearODE(int dimension) {
            this.dimension = dimension;
        }

        public int getDimension() {
            return dimension;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            for (int i = 0; i < dimension; i++) {
                yDot[i] = 1.0;
            }
        }
    }

    // Event handler for testing issue 695 and reset state behavior
    private static class ResettingEvent implements EventHandler {
        private static double lastTriggerTime = Double.NEGATIVE_INFINITY;
        private final double tEvent;

        public ResettingEvent(double tEvent) {
            this.tEvent = tEvent;
        }

        public void init(double t0, double[] y0, double t) {
        }

        public double g(double t, double[] y) {
            return t - tEvent;
        }

        public Action eventOccurred(double t, double[] y, boolean increasing) {
            if (t < lastTriggerTime) {
                fail("going backard in time! (" + t + " < " + lastTriggerTime + ")");
            }
            lastTriggerTime = t;
            return Action.RESET_STATE;
        }

        public void resetState(double t, double[] y) {
            y[0] += 1.0;
        }
    }

    // =========================================================================
    // PARTITION A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndInitialState() {
        DummyIntegrator named = new DummyIntegrator("TestIntegrator");
        assertEquals("TestIntegrator", named.getName());
        assertTrue(Double.isNaN(named.getCurrentStepStart()));
        assertTrue(Double.isNaN(named.getCurrentSignedStepsize()));
        assertEquals(0, named.getEvaluations());
        assertEquals(Integer.MAX_VALUE, named.getMaxEvaluations());
        assertTrue(named.getStepHandlers().isEmpty());
        assertTrue(named.getEventHandlers().isEmpty());

        DummyIntegrator unnamed = new DummyIntegrator();
        assertNull(unnamed.getName());
    }

    @Test(timeout = 4000)
    public void testStepHandlerManagement() {
        AbstractIntegrator integrator = new DummyIntegrator("StepHandlerTest");
        StepHandler handler1 = new StepHandler() {
            public void init(double t0, double[] y0, double t) {}
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}
        };
        StepHandler handler2 = new StepHandler() {
            public void init(double t0, double[] y0, double t) {}
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}
        };

        integrator.addStepHandler(handler1);
        assertEquals(1, integrator.getStepHandlers().size());
        assertTrue(integrator.getStepHandlers().contains(handler1));

        integrator.addStepHandler(handler2);
        assertEquals(2, integrator.getStepHandlers().size());

        integrator.clearStepHandlers();
        assertEquals(0, integrator.getStepHandlers().size());
    }

    @Test(timeout = 4000)
    public void testEventHandlerManagement() {
        AbstractIntegrator integrator = new DummyIntegrator("EventHandlerTest");
        EventHandler event1 = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return 0; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.CONTINUE; }
            public void resetState(double t, double[] y) {}
        };
        EventHandler event2 = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return 0; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.CONTINUE; }
            public void resetState(double t, double[] y) {}
        };

        // Add with default BracketingNthOrderBrentSolver
        integrator.addEventHandler(event1, 1.0, 1e-4, 100);
        assertEquals(1, integrator.getEventHandlers().size());
        assertTrue(integrator.getEventHandlers().contains(event1));

        // Add with custom UnivariateRealSolver
        integrator.addEventHandler(event2, 0.5, 1e-6, 50, new BrentSolver(1e-6));
        assertEquals(2, integrator.getEventHandlers().size());
        assertTrue(integrator.getEventHandlers().contains(event2));

        integrator.clearEventHandlers();
        assertEquals(0, integrator.getEventHandlers().size());
    }

    @Test(timeout = 4000)
    public void testMaxEvaluationsConfiguration() {
        DummyIntegrator integrator = new DummyIntegrator("EvalTest");

        integrator.setMaxEvaluations(50);
        assertEquals(50, integrator.getMaxEvaluations());

        integrator.setMaxEvaluations(-1);
        assertEquals(Integer.MAX_VALUE, integrator.getMaxEvaluations());

        integrator.setMaxEvaluations(-999);
        assertEquals(Integer.MAX_VALUE, integrator.getMaxEvaluations());

        integrator.setMaxEvaluations(0);
        assertEquals(0, integrator.getMaxEvaluations());
    }

    @Test(timeout = 4000)
    public void testNormalIntegrationForward() {
        FirstOrderIntegrator integrator = new EulerIntegrator(0.1);
        LinearODE ode = new LinearODE(1);
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, new double[] { 0.0 }, 1.0, y);

        assertEquals(1.0, stopTime, 1e-12);
        assertEquals(1.0, y[0], 1e-12);
        assertTrue(integrator.getEvaluations() > 0);
    }

    @Test(timeout = 4000)
    public void testStepHandlerExecutionAndCurrentTimeInspection() {
        FirstOrderIntegrator integrator = new EulerIntegrator(0.2);
        final List<Double> stepStarts = new ArrayList<Double>();
        final List<Boolean> lastSteps = new ArrayList<Boolean>();

        integrator.addStepHandler(new StepHandler() {
            public void init(double t0, double[] y0, double t) {}

            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                stepStarts.add(interpolator.getPreviousTime());
                lastSteps.add(isLast);
            }
        });

        LinearODE ode = new LinearODE(1);
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, new double[] { 0.0 }, 1.0, y);

        assertFalse(stepStarts.isEmpty());
        assertEquals(Boolean.TRUE, lastSteps.get(lastSteps.size() - 1));
        for (int i = 0; i < lastSteps.size() - 1; i++) {
            assertEquals(Boolean.FALSE, lastSteps.get(i));
        }
    }

    // =========================================================================
    // PARTITION B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSanityChecksDtAtAndAroundThreshold() {
        DummyIntegrator integrator = new DummyIntegrator("SanityCheckTest");
        ExpandableStatefulODE ode = new ExpandableStatefulODE(new LinearODE(1));
        ode.setTime(10.0);

        double maxTime = FastMath.max(FastMath.abs(10.0), FastMath.abs(10.0));
        double threshold = 1000 * FastMath.ulp(maxTime);

        // Sub-case 1: dt exactly 0 <= threshold -> throws
        try {
            integrator.callSanityChecks(ode, 10.0);
            fail("Expected NumberIsTooSmallException when dt == 0");
        } catch (NumberIsTooSmallException expected) {
            assertEquals(0.0, expected.getNumber().doubleValue(), 1e-15);
        }

        // Sub-case 2: dt strictly inside threshold -> throws
        try {
            integrator.callSanityChecks(ode, 10.0 + 0.5 * threshold);
            fail("Expected NumberIsTooSmallException when dt < threshold");
        } catch (NumberIsTooSmallException expected) {
            assertTrue(expected.getNumber().doubleValue() <= threshold);
        }

        // Sub-case 3: dt safely above threshold -> succeeds
        integrator.callSanityChecks(ode, 10.0 + 2.0 * threshold);
    }

    @Test(timeout = 4000)
    public void testSanityChecksZeroTimes() {
        DummyIntegrator integrator = new DummyIntegrator("SanityCheckZero");
        ExpandableStatefulODE ode = new ExpandableStatefulODE(new LinearODE(1));
        ode.setTime(0.0);

        try {
            integrator.callSanityChecks(ode, 0.0);
            fail("Expected NumberIsTooSmallException when t0 == 0 and t == 0");
        } catch (NumberIsTooSmallException expected) {
            assertEquals(0.0, expected.getNumber().doubleValue(), 1e-20);
        }
    }

    @Test(timeout = 4000)
    public void testMultipleEventsWithinSingleStepChronologicalOrder() {
        FirstOrderIntegrator integrator = new EulerIntegrator(1.0);
        final List<Double> triggeredTimes = new ArrayList<Double>();

        EventHandler event1 = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return t - 0.25; }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                triggeredTimes.add(t);
                return Action.CONTINUE;
            }
            public void resetState(double t, double[] y) {}
        };

        EventHandler event2 = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return t - 0.75; }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                triggeredTimes.add(t);
                return Action.CONTINUE;
            }
            public void resetState(double t, double[] y) {}
        };

        integrator.addEventHandler(event1, 0.1, 1e-6, 100);
        integrator.addEventHandler(event2, 0.1, 1e-6, 100);

        integrator.integrate(new LinearODE(1), 0.0, new double[] { 0.0 }, 1.0, new double[1]);

        assertEquals(2, triggeredTimes.size());
        assertEquals(0.25, triggeredTimes.get(0), 1e-5);
        assertEquals(0.75, triggeredTimes.get(1), 1e-5);
    }

    @Test(timeout = 4000)
    public void testBackwardIntegrationWithChronologicalEvents() {
        FirstOrderIntegrator integrator = new EulerIntegrator(1.0);
        final List<Double> triggeredTimes = new ArrayList<Double>();

        EventHandler event1 = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return t - 0.75; }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                triggeredTimes.add(t);
                return Action.CONTINUE;
            }
            public void resetState(double t, double[] y) {}
        };

        EventHandler event2 = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return t - 0.25; }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                triggeredTimes.add(t);
                return Action.CONTINUE;
            }
            public void resetState(double t, double[] y) {}
        };

        integrator.addEventHandler(event1, 0.1, 1e-6, 100);
        integrator.addEventHandler(event2, 0.1, 1e-6, 100);

        // Backward integration from 1.0 to 0.0
        double[] y = new double[] { 1.0 };
        double stopTime = integrator.integrate(new LinearODE(1), 1.0, y, 0.0, new double[1]);

        assertEquals(0.0, stopTime, 1e-12);
        assertEquals(2, triggeredTimes.size());
        assertEquals(0.75, triggeredTimes.get(0), 1e-5);
        assertEquals(0.25, triggeredTimes.get(1), 1e-5);
    }

    // =========================================================================
    // PARTITION C: Defect-Targeted Branch Zone (MATH-695 / testIssue695)
    // =========================================================================

    @Test(timeout = 4000)
    public void testIssue695() {
        ResettingEvent.lastTriggerTime = Double.NEGATIVE_INFINITY;

        FirstOrderDifferentialEquations equation = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };

        DormandPrince853Integrator integ = new DormandPrince853Integrator(0.001, 1000, 1.0e-14, 1.0e-14);
        integ.addEventHandler(new ResettingEvent(10.99), 0.1, 1.0e-9, 1000);
        integ.addEventHandler(new ResettingEvent(11.01), 0.1, 1.0e-9, 1000);

        double[] y = new double[1];
        integ.integrate(equation, 0.0, new double[] { 0.0 }, 30.0, y);

        assertEquals(30.0, integ.getCurrentStepStart(), 1e-5);
    }

    @Test(timeout = 4000)
    public void testEventHandlerStopActionHaltsIntegration() {
        FirstOrderIntegrator integrator = new EulerIntegrator(0.1);
        EventHandler stopEvent = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return t - 0.45; }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }
            public void resetState(double t, double[] y) {}
        };

        integrator.addEventHandler(stopEvent, 0.05, 1e-6, 100);
        double[] y = new double[1];
        double stopTime = integrator.integrate(new LinearODE(1), 0.0, new double[] { 0.0 }, 1.0, y);

        assertEquals(0.45, stopTime, 1e-4);
        assertEquals(0.45, y[0], 1e-4);
    }

    @Test(timeout = 4000)
    public void testEventHandlerResetDerivativesAction() {
        FirstOrderIntegrator integrator = new EulerIntegrator(0.1);
        final boolean[] derivativesReset = new boolean[] { false };

        EventHandler resetDerivsEvent = new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return t - 0.35; }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                derivativesReset[0] = true;
                return Action.RESET_DERIVATIVES;
            }
            public void resetState(double t, double[] y) {}
        };

        integrator.addEventHandler(resetDerivsEvent, 0.05, 1e-6, 100);
        double[] y = new double[1];
        double stopTime = integrator.integrate(new LinearODE(1), 0.0, new double[] { 0.0 }, 1.0, y);

        assertTrue(derivativesReset[0]);
        assertEquals(1.0, stopTime, 1e-12);
    }

    // =========================================================================
    // PARTITION D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testIntegrateMismatchedY0DimensionThrows() {
        DummyIntegrator integrator = new DummyIntegrator("DimCheck");
        LinearODE ode = new LinearODE(2);
        integrator.integrate(ode, 0.0, new double[1], 1.0, new double[2]);
    }

    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testIntegrateMismatchedYDimensionThrows() {
        DummyIntegrator integrator = new DummyIntegrator("DimCheck");
        LinearODE ode = new LinearODE(2);
        integrator.integrate(ode, 0.0, new double[2], 1.0, new double[1]);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testStepHandlersCollectionIsUnmodifiable() {
        DummyIntegrator integrator = new DummyIntegrator();
        integrator.getStepHandlers().add(new StepHandler() {
            public void init(double t0, double[] y0, double t) {}
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}
        });
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testEventHandlersCollectionIsUnmodifiable() {
        DummyIntegrator integrator = new DummyIntegrator();
        integrator.getEventHandlers().add(new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) { return 0; }
            public Action eventOccurred(double t, double[] y, boolean increasing) { return Action.CONTINUE; }
            public void resetState(double t, double[] y) {}
        });
    }

    @Test(timeout = 4000)
    public void testMaxCountExceededExceptionThrownWhenEvaluationsExceeded() {
        DummyIntegrator integrator = new DummyIntegrator("CountTest");
        ExpandableStatefulODE ode = new ExpandableStatefulODE(new LinearODE(1));
        integrator.callSetEquations(ode);
        integrator.setMaxEvaluations(2);

        double[] y = new double[1];
        double[] yDot = new double[1];

        integrator.computeDerivatives(0.0, y, yDot);
        assertEquals(1, integrator.getEvaluations());

        integrator.computeDerivatives(0.1, y, yDot);
        assertEquals(2, integrator.getEvaluations());

        try {
            integrator.computeDerivatives(0.2, y, yDot);
            fail("Expected MaxCountExceededException after exceeding 2 evaluations");
        } catch (MaxCountExceededException expected) {
            assertEquals(2, expected.getMax());
        }
    }

    // =========================================================================
    // PARTITION E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testResetEvaluationsLifecycle() {
        DummyIntegrator integrator = new DummyIntegrator("ResetEvalTest");
        ExpandableStatefulODE ode = new ExpandableStatefulODE(new LinearODE(1));
        integrator.callSetEquations(ode);
        integrator.setMaxEvaluations(10);

        integrator.computeDerivatives(0.0, new double[1], new double[1]);
        assertEquals(1, integrator.getEvaluations());

        integrator.callResetEvaluations();
        assertEquals(0, integrator.getEvaluations());

        integrator.computeDerivatives(0.0, new double[1], new double[1]);
        assertEquals(1, integrator.getEvaluations());
    }

    @Test(timeout = 4000)
    public void testSetStateInitializedFlag() {
        DummyIntegrator integrator = new DummyIntegrator("StateInitTest");
        integrator.callSetStateInitialized(true);
        integrator.callSetStateInitialized(false);
    }
}