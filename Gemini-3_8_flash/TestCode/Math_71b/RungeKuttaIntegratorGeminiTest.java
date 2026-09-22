package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.ContinuousOutputModel;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * --------------------------------------------------------------------------------------------------
 * Class Under Test: RungeKuttaIntegrator (Abstract fixed-step Runge-Kutta ODE solver)
 * Target Decisions & Condition Branches:
 *   1. integrate() sanity checks (dimension check, t vs t0 check).
 *   2. Forward vs backward integration (forward = t > t0 ? step : -step).
 *   3. y == y0 vs y != y0 array copy optimization.
 *   4. Interpolator selection: requiresDenseOutput() || (!eventsHandlersManager.isEmpty())
 *      -> RungeKuttaStepInterpolator branch vs DummyStepInterpolator branch.
 *   5. Multi-stage Butcher array evaluation loops (k from 1 to stages, dot products).
 *   6. Discrete event handling:
 *      - manager.evaluateStep(interpolator) == true vs false.
 *      - Event step truncation: Math.abs(dt) <= Math.ulp(stepStart) (artificial zero size step).
 *      - Event step rejection: stepSize = dt (match event switch time).
 *   7. Event action handling:
 *      - EventHandler.STOP -> terminates loop via manager.stop().
 *      - EventHandler.RESET_STATE -> modifies state vector y and triggers state reset.
 *      - EventHandler.RESET_DERIVATIVES -> recomputes derivatives (manager.reset() == true).
 *      - EventHandler.CONTINUE -> normal continuation.
 *   8. StepHandler dispatch: reset(), handleStep(interpolator, isLast).
 *   9. Post-integration cleanup: stepStart = Double.NaN, stepSize = Double.NaN.
 *  10. Defect Missed End Event (Ground Truth):
 *      - ClassicalRungeKuttaIntegrator missed event occurring close to step end boundary
 *        (expected 1.8782503799999986E9 vs delayed 1.878250439999994E9).
 * --------------------------------------------------------------------------------------------------
 */
public class RungeKuttaIntegratorGeminiTest {

    // =========================================================================
    // Test Utility Classes & Differential Equations
    // =========================================================================

    /** Simple linear differential equations: dy/dt = -y (solution: y(t) = y0 * exp(-t)). */
    private static class LinearODE implements FirstOrderDifferentialEquations {
        private final int dimension;
        private final double rate;

        public LinearODE(int dimension, double rate) {
            this.dimension = dimension;
            this.rate = rate;
        }

        public int getDimension() {
            return dimension;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            for (int i = 0; i < dimension; ++i) {
                yDot[i] = rate * y[i];
            }
        }
    }

    /** Differential equation that intentionally throws DerivativeException. */
    private static class FaultyODE implements FirstOrderDifferentialEquations {
        public int getDimension() {
            return 1;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
            throw new DerivativeException("Simulated derivative failure at t = {0}", t);
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the known defect where an event occurring near the end of a step
     * was missed and deferred to the subsequent step.
     */
    @Test(timeout = 4000)
    public void testMissedEndEvent() throws IntegratorException, DerivativeException {
        final double t0 = 1878250320.0000029;
        final double tEvent = 1878250379.9999986;
        final double[] k = {1.0e-4, 1.0e-5, 1.0e-6};

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return k.length;
            }

            public void computeDerivatives(double t, double[] y, double[] yDot) {
                for (int i = 0; i < y.length; ++i) {
                    yDot[i] = k[i] * y[i];
                }
            }
        };

        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(60.0);

        EventHandler closeHandler = new EventHandler() {
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return STOP;
            }

            public double g(double t, double[] y) {
                return t - tEvent;
            }

            public void resetState(double t, double[] y) {
            }
        };

        integrator.addEventHandler(closeHandler, 60.0, 1.0e-9, 100);

        double[] y0 = new double[]{2.0, 2.0, 2.0};
        double[] y = new double[k.length];
        double tEnd = integrator.integrate(ode, t0, y0, tEvent + 120.0, y);

        assertEquals(tEvent, tEnd, 1.0e-9);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    /**
     * Verifies forward integration using EulerIntegrator with no handlers
     * (executing the DummyStepInterpolator path where requiresDenseOutput is false).
     */
    @Test(timeout = 4000)
    public void testForwardIntegrationWithDummyInterpolator() throws IntegratorException, DerivativeException {
        EulerIntegrator integrator = new EulerIntegrator(0.1);
        LinearODE ode = new LinearODE(1, 1.0); // y' = y -> y(t) = y0 * e^t

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-12);
        // Euler method with step 0.1 for 10 steps: (1 + 0.1)^10 ≈ 2.59374246
        assertEquals(Math.pow(1.1, 10), y[0], 1.0e-6);
        // After integration, internal stepStart and stepSize should be NaN
        assertTrue(Double.isNaN(integrator.getCurrentStepStart()));
        assertTrue(Double.isNaN(integrator.getCurrentSignedStepSize()));
    }

    /**
     * Verifies backward integration (t < t0) and confirms negative step size handling.
     */
    @Test(timeout = 4000)
    public void testBackwardIntegration() throws IntegratorException, DerivativeException {
        MidpointIntegrator integrator = new MidpointIntegrator(0.05);
        LinearODE ode = new LinearODE(1, -1.0);

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        double stopTime = integrator.integrate(ode, 1.0, y0, 0.0, y);

        assertEquals(0.0, stopTime, 1.0e-12);
        // Exact solution y(0) = y(1) * exp(1) ≈ 2.71828
        assertEquals(Math.exp(1.0), y[0], 1.0e-2);
    }

    /**
     * Verifies integration where destination array y is the same instance as y0.
     */
    @Test(timeout = 4000)
    public void testIntegrationSameStateArrayInstance() throws IntegratorException, DerivativeException {
        GillIntegrator integrator = new GillIntegrator(0.2);
        LinearODE ode = new LinearODE(2, -0.5);

        double[] y = new double[]{2.0, 4.0};
        double stopTime = integrator.integrate(ode, 0.0, y, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-12);
        assertEquals(2.0 * Math.exp(-0.5), y[0], 1.0e-3);
        assertEquals(4.0 * Math.exp(-0.5), y[1], 1.0e-3);
    }

    /**
     * Verifies dense output interpolator activation via ContinuousOutputModel.
     */
    @Test(timeout = 4000)
    public void testContinuousOutputModelWithRungeKuttaStepInterpolator() throws IntegratorException, DerivativeException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        ContinuousOutputModel model = new ContinuousOutputModel();
        integrator.addStepHandler(model);

        LinearODE ode = new LinearODE(1, 1.0);
        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(0.0, model.getInitialTime(), 1.0e-12);
        assertEquals(1.0, model.getFinalTime(), 1.0e-12);

        model.setInterpolatedTime(0.5);
        double[] interpolatedY = model.getInterpolatedState();
        assertEquals(Math.exp(0.5), interpolatedY[0], 1.0e-4);
    }

    /**
     * Verifies that StepHandler callbacks (reset and handleStep) are properly triggered.
     */
    @Test(timeout = 4000)
    public void testStepHandlerLifecycle() throws IntegratorException, DerivativeException {
        EulerIntegrator integrator = new EulerIntegrator(0.5);
        final int[] resetCalls = {0};
        final int[] stepCalls = {0};
        final int[] lastStepCalls = {0};

        integrator.addStepHandler(new StepHandler() {
            public boolean isFullStep() {
                return true;
            }

            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                stepCalls[0]++;
                if (isLast) {
                    lastStepCalls[0]++;
                }
            }

            public void reset() {
                resetCalls[0]++;
            }
        });

        LinearODE ode = new LinearODE(1, 0.0);
        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(1, resetCalls[0]);
        assertEquals(2, stepCalls[0]); // 0 -> 0.5 and 0.5 -> 1.0
        assertEquals(1, lastStepCalls[0]);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Event Actions
    // =========================================================================

    /**
     * Tests event handler returning RESET_DERIVATIVES, validating that derivatives
     * are recomputed and integration continues smoothly.
     */
    @Test(timeout = 4000)
    public void testEventResetDerivatives() throws IntegratorException, DerivativeException {
        EulerIntegrator integrator = new EulerIntegrator(0.25);
        LinearODE ode = new LinearODE(1, 1.0);

        final boolean[] eventTriggered = {false};
        EventHandler handler = new EventHandler() {
            public double g(double t, double[] y) {
                return t - 0.5;
            }

            public int eventOccurred(double t, double[] y, boolean increasing) {
                eventTriggered[0] = true;
                return RESET_DERIVATIVES;
            }

            public void resetState(double t, double[] y) {
            }
        };

        integrator.addEventHandler(handler, 0.5, 1.0e-6, 100);

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        double stop = integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(1.0, stop, 1.0e-12);
        assertTrue(eventTriggered[0]);
    }

    /**
     * Tests event handler returning RESET_STATE, modifying state y at the event instant.
     */
    @Test(timeout = 4000)
    public void testEventResetState() throws IntegratorException, DerivativeException {
        EulerIntegrator integrator = new EulerIntegrator(0.5);
        LinearODE ode = new LinearODE(1, 0.0); // y' = 0

        EventHandler handler = new EventHandler() {
            public double g(double t, double[] y) {
                return t - 0.5;
            }

            public int eventOccurred(double t, double[] y, boolean increasing) {
                return RESET_STATE;
            }

            public void resetState(double t, double[] y) {
                y[0] += 10.0;
            }
        };

        integrator.addEventHandler(handler, 0.5, 1.0e-6, 100);

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        double stop = integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(1.0, stop, 1.0e-12);
        assertEquals(11.0, y[0], 1.0e-6);
    }

    /**
     * Tests event handler returning CONTINUE, ensuring integration proceeds without pause.
     */
    @Test(timeout = 4000)
    public void testEventContinue() throws IntegratorException, DerivativeException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.2);
        LinearODE ode = new LinearODE(1, 1.0);

        final int[] occurrences = {0};
        EventHandler handler = new EventHandler() {
            public double g(double t, double[] y) {
                return t - 0.4;
            }

            public int eventOccurred(double t, double[] y, boolean increasing) {
                occurrences[0]++;
                return CONTINUE;
            }

            public void resetState(double t, double[] y) {
            }
        };

        integrator.addEventHandler(handler, 0.2, 1.0e-6, 100);

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        double stop = integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(1.0, stop, 1.0e-12);
        assertEquals(1, occurrences[0]);
    }

    /**
     * Tests an event occurring at dt <= Math.ulp(stepStart), triggering the
     * artificial zero-size step boundary branch (loop = false).
     */
    @Test(timeout = 4000)
    public void testEventAtExtremelySmallDtTriggeringUlpBranch() throws IntegratorException, DerivativeException {
        final double t0 = 100.0;
        final double tEvent = t0 + 0.5 * Math.ulp(t0);

        EulerIntegrator integrator = new EulerIntegrator(1.0);
        LinearODE ode = new LinearODE(1, 0.0);

        EventHandler handler = new EventHandler() {
            public double g(double t, double[] y) {
                return t - tEvent;
            }

            public int eventOccurred(double t, double[] y, boolean increasing) {
                return STOP;
            }

            public void resetState(double t, double[] y) {
            }
        };

        integrator.addEventHandler(handler, 1.0, 1.0e-15, 100);

        double[] y0 = new double[]{5.0};
        double[] y = new double[1];
        double stop = integrator.integrate(ode, t0, y0, t0 + 10.0, y);

        // The integrator terminates around t0 due to the immediate event
        assertTrue(Math.abs(stop - t0) <= 1.0);
        assertEquals(5.0, y[0], 1.0e-9);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    /**
     * Verifies that integration fails when dimension of y0 does not match ODE dimension.
     */
    @Test(expected = IntegratorException.class, timeout = 4000)
    public void testSanityCheckMismatchInitialDimension() throws IntegratorException, DerivativeException {
        EulerIntegrator integrator = new EulerIntegrator(0.1);
        LinearODE ode = new LinearODE(2, 1.0);

        double[] y0 = new double[]{1.0}; // Dimension 1 vs 2 expected
        double[] y = new double[2];
        integrator.integrate(ode, 0.0, y0, 1.0, y);
    }

    /**
     * Verifies that integration fails when dimension of y does not match ODE dimension.
     */
    @Test(expected = IntegratorException.class, timeout = 4000)
    public void testSanityCheckMismatchFinalDimension() throws IntegratorException, DerivativeException {
        EulerIntegrator integrator = new EulerIntegrator(0.1);
        LinearODE ode = new LinearODE(2, 1.0);

        double[] y0 = new double[]{1.0, 2.0};
        double[] y = new double[1]; // Dimension 1 vs 2 expected
        integrator.integrate(ode, 0.0, y0, 1.0, y);
    }

    /**
     * Verifies that integration fails when t is practically identical to t0 (|t - t0| <= 1e-12).
     */
    @Test(expected = IntegratorException.class, timeout = 4000)
    public void testSanityCheckTooSmallIntegrationInterval() throws IntegratorException, DerivativeException {
        EulerIntegrator integrator = new EulerIntegrator(0.1);
        LinearODE ode = new LinearODE(1, 1.0);

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        integrator.integrate(ode, 1.0, y0, 1.0 + 1.0e-14, y);
    }

    /**
     * Verifies that DerivativeException thrown by ODE is propagated properly.
     */
    @Test(expected = DerivativeException.class, timeout = 4000)
    public void testDerivativeExceptionPropagation() throws IntegratorException, DerivativeException {
        EulerIntegrator integrator = new EulerIntegrator(0.1);
        FaultyODE ode = new FaultyODE();

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 1.0, y);
    }

    // =========================================================================
    // Partition E: Method Names, Evaluation Counts & Multiple Stages
    // =========================================================================

    /**
     * Verifies evaluation counts and method naming on multi-stage Runge-Kutta integrators.
     */
    @Test(timeout = 4000)
    public void testIntegratorNamesAndEvaluationCounts() throws IntegratorException, DerivativeException {
        LinearODE ode = new LinearODE(1, 1.0);
        double[] y0 = new double[]{1.0};
        double[] y = new double[1];

        // Euler: 1 stage -> 1 eval per step
        EulerIntegrator euler = new EulerIntegrator(0.5);
        assertEquals("Euler", euler.getName());
        euler.integrate(ode, 0.0, y0, 1.0, y);
        assertEquals(2, euler.getEvaluations());

        // Midpoint: 2 stages -> 2 evals per step
        MidpointIntegrator midpoint = new MidpointIntegrator(0.5);
        assertEquals("midpoint", midpoint.getName());
        midpoint.integrate(ode, 0.0, y0, 1.0, y);
        assertEquals(4, midpoint.getEvaluations());

        // Classical RK4: 4 stages -> 4 evals per step
        ClassicalRungeKuttaIntegrator rk4 = new ClassicalRungeKuttaIntegrator(0.5);
        assertEquals("classical Runge-Kutta", rk4.getName());
        rk4.integrate(ode, 0.0, y0, 1.0, y);
        assertEquals(8, rk4.getEvaluations());

        // Gill: 4 stages -> 4 evals per step
        GillIntegrator gill = new GillIntegrator(0.5);
        assertEquals("Gill", gill.getName());
        gill.integrate(ode, 0.0, y0, 1.0, y);
        assertEquals(8, gill.getEvaluations());
    }

    /**
     * Verifies handler clearing logic and state cleanup between runs.
     */
    @Test(timeout = 4000)
    public void testClearEventHandlersAndStepHandlers() throws IntegratorException, DerivativeException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        assertEquals(0, integrator.getStepHandlers().size());
        assertEquals(0, integrator.getEventHandlers().size());

        ContinuousOutputModel model = new ContinuousOutputModel();
        integrator.addStepHandler(model);
        assertEquals(1, integrator.getStepHandlers().size());

        EventHandler handler = new EventHandler() {
            public double g(double t, double[] y) {
                return 0;
            }

            public int eventOccurred(double t, double[] y, boolean increasing) {
                return STOP;
            }

            public void resetState(double t, double[] y) {
            }
        };
        integrator.addEventHandler(handler, 0.1, 1.0e-3, 10);
        assertEquals(1, integrator.getEventHandlers().size());

        integrator.clearStepHandlers();
        integrator.clearEventHandlers();
        assertEquals(0, integrator.getStepHandlers().size());
        assertEquals(0, integrator.getEventHandlers().size());

        // Ensure integration still executes properly without any handlers
        LinearODE ode = new LinearODE(1, 0.0);
        double[] y0 = new double[]{3.0};
        double[] y = new double[1];
        double stop = integrator.integrate(ode, 0.0, y0, 1.0, y);
        assertEquals(1.0, stop, 1.0e-12);
        assertEquals(3.0, y[0], 1.0e-12);
    }
}