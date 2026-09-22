package org.apache.commons.math.ode.nonstiff;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.ContinuousOutputModel;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: EmbeddedRungeKuttaIntegrator
 *
 * 1. Constructor Variations:
 *    - Scalar absolute/relative tolerances.
 *    - Vector absolute/relative tolerances (vecAbsoluteTolerance != null branch).
 *
 * 2. Parameter Control (Safety, MinReduction, MaxGrowth):
 *    - Default initialization checks (safety = 0.9, minReduction = 0.2, maxGrowth = 10.0).
 *    - Custom getter / setter mutation tests.
 *
 * 3. Array & State Tracking Branches in integrate():
 *    - Output array identity: y == y0 vs y != y0.
 *    - Dense output requirement:
 *      * Handler requires dense output (ContinuousOutputModel).
 *      * Event handler present (!eventsHandlersManager.isEmpty()).
 *      * Neither required -> DummyStepInterpolator path.
 *    - Direction: Forward integration (t > t0) vs Backward integration (t < t0).
 *    - FSAL vs Non-FSAL stages:
 *      * FSAL method (e.g. DormandPrince54, DormandPrince853): last stage reused as first stage.
 *      * Non-FSAL method (e.g. HighamHall54): first stage computed every step.
 *
 * 4. Error Control & Step Acceptance/Rejection Branches:
 *    - Error <= 1.0 (step accepted).
 *    - Error > 1.0 (step rejected, stepsize reduction factor applied).
 *    - Discrete event handling:
 *      * evaluateStep(...) returns true.
 *      * Sub-branch: Math.abs(dt) <= Math.ulp(stepStart) (step accepted despite event).
 *      * Sub-branch: dt > Math.ulp(stepStart) (step rejected to match event switch time).
 *      * Event reset triggering derivative recomputation (manager.reset(...) == true).
 *      * Event stop terminating the loop (lastStep = manager.stop()).
 *
 * 5. Boundary & Robustness Paths:
 *    - Dimensions mismatch in equations vs arrays.
 *    - Min step size violations when step cannot be reduced further.
 *    - Integration with exact polynomial differentials to ensure order convergence.
 */
public class EmbeddedRungeKuttaIntegratorGeminiTest {

    // Concrete test class to access protected methods and test non-FSAL/custom configurations
    private static class DummyEmbeddedRKIntegrator extends EmbeddedRungeKuttaIntegrator {
        private final int order;
        private double forcedError = 0.0;
        private int errorEvalCount = 0;

        public DummyEmbeddedRKIntegrator(String name, boolean fsal, double[] c, double[][] a, double[] b,
                                         RungeKuttaStepInterpolator prototype,
                                         double minStep, double maxStep,
                                         double scalAbsTol, double scalRelTol, int order) {
            super(name, fsal, c, a, b, prototype, minStep, maxStep, scalAbsTol, scalRelTol);
            this.order = order;
        }

        public DummyEmbeddedRKIntegrator(String name, boolean fsal, double[] c, double[][] a, double[] b,
                                         RungeKuttaStepInterpolator prototype,
                                         double minStep, double maxStep,
                                         double[] vecAbsTol, double[] vecRelTol, int order) {
            super(name, fsal, c, a, b, prototype, minStep, maxStep, vecAbsTol, vecRelTol);
            this.order = order;
        }

        public void setForcedError(double error) {
            this.forcedError = error;
        }

        public int getErrorEvalCount() {
            return errorEvalCount;
        }

        @Override
        public int getOrder() {
            return order;
        }

        @Override
        protected double estimateError(double[][] yDotK, double[] y0, double[] y1, double h) {
            errorEvalCount++;
            if (forcedError > 0) {
                double err = forcedError;
                forcedError = 0; // Return high once then proceed normally
                return err;
            }
            return 0.1; // Acceptable step
        }
    }

    // Simple Linear ODE: y' = y
    private static class LinearEquations implements FirstOrderDifferentialEquations {
        @Override
        public int getDimension() {
            return 1;
        }

        @Override
        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = y[0];
        }
    }

    // 2D Independent ODE
    private static class 2DEquations implements FirstOrderDifferentialEquations {
        @Override
        public int getDimension() {
            return 2;
        }

        @Override
        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = 1.0;
            yDot[1] = 2.0 * t;
        }
    }

    // 3rd degree polynomial ODE: y' = 3 * t^2 -> y(t) = t^3
    private static class PolynomialEquations implements FirstOrderDifferentialEquations {
        @Override
        public int getDimension() {
            return 1;
        }

        @Override
        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = 3.0 * t * t;
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testGettersSettersDefaultParameters() {
        DormandPrince54Integrator integrator = new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        assertEquals(0.9, integrator.getSafety(), 1.0e-12);
        assertEquals(0.2, integrator.getMinReduction(), 1.0e-12);
        assertEquals(10.0, integrator.getMaxGrowth(), 1.0e-12);
        assertEquals(5, integrator.getOrder());

        integrator.setSafety(0.85);
        integrator.setMinReduction(0.15);
        integrator.setMaxGrowth(8.0);

        assertEquals(0.85, integrator.getSafety(), 1.0e-12);
        assertEquals(0.15, integrator.getMinReduction(), 1.0e-12);
        assertEquals(8.0, integrator.getMaxGrowth(), 1.0e-12);
    }

    @Test(timeout = 4000)
    public void testStandardForwardIntegrationWithSameInOutArrays() throws IntegratorException, DerivativeException {
        // Test when y == y0
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(1.0e-5, 1.0, 1.0e-8, 1.0e-8);
        LinearEquations ode = new LinearEquations();
        double[] y = new double[]{1.0};

        double tEnd = integrator.integrate(ode, 0.0, y, 1.0, y);

        assertEquals(1.0, tEnd, 1.0e-10);
        assertEquals(Math.E, y[0], 1.0e-5);
    }

    @Test(timeout = 4000)
    public void testStandardForwardIntegrationWithDifferentInOutArrays() throws IntegratorException, DerivativeException {
        // Test when y != y0
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(1.0e-5, 1.0, 1.0e-8, 1.0e-8);
        LinearEquations ode = new LinearEquations();
        double[] y0 = new double[]{1.0};
        double[] y = new double[1];

        double tEnd = integrator.integrate(ode, 0.0, y0, 1.0, y);

        assertEquals(1.0, tEnd, 1.0e-10);
        assertEquals(1.0, y0[0], 1.0e-12); // y0 must not be modified
        assertEquals(Math.E, y[0], 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testVectorTolerancesBranch() throws IntegratorException, DerivativeException {
        // vecAbsoluteTolerance != null branch in firstTime check
        double[] vecAbsTol = new double[]{1.0e-6, 1.0e-6};
        double[] vecRelTol = new double[]{1.0e-6, 1.0e-6};
        EmbeddedRungeKuttaIntegrator integrator = new HighamHall54Integrator(1.0e-4, 1.0, vecAbsTol, vecRelTol);

        2DEquations ode = new 2DEquations();
        double[] y0 = new double[]{0.0, 0.0};
        double[] y = new double[2];

        double stopTime = integrator.integrate(ode, 0.0, y0, 2.0, y);

        assertEquals(2.0, stopTime, 1.0e-10);
        assertEquals(2.0, y[0], 1.0e-4);
        assertEquals(4.0, y[1], 1.0e-4);
    }

    @Test(timeout = 4000)
    public void testDenseOutputRequirementBranches() throws IntegratorException, DerivativeException {
        // requiresDenseOutput() == true through ContinuousOutputModel
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);
        ContinuousOutputModel stepHandler = new ContinuousOutputModel();
        integrator.addStepHandler(stepHandler);

        LinearEquations ode = new LinearEquations();
        double[] y = new double[]{1.0};
        integrator.integrate(ode, 0.0, y, 1.0, y);

        stepHandler.setInterpolatedTime(0.5);
        double[] interpolatedState = stepHandler.getInterpolatedState();
        assertEquals(Math.exp(0.5), interpolatedState[0], 1.0e-3);
    }

    @Test(timeout = 4000)
    public void testDummyInterpolatorBranchWithoutHandlersOrEvents() throws IntegratorException, DerivativeException {
        // No step handlers requiring dense output and no events -> DummyStepInterpolator path
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        // Dummy step handler that does not require dense output
        final int[] stepsCounter = new int[1];
        integrator.addStepHandler(new StepHandler() {
            @Override
            public boolean isResetting() {
                return false;
            }

            @Override
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                stepsCounter[0]++;
            }

            @Override
            public void reset() {}
        });

        LinearEquations ode = new LinearEquations();
        double[] y = new double[]{1.0};
        integrator.integrate(ode, 0.0, y, 1.0, y);

        assertTrue(stepsCounter[0] > 0);
        assertEquals(Math.E, y[0], 1.0e-4);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testBackwardIntegration() throws IntegratorException, DerivativeException {
        // Forward flag is false (t < t0)
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-8, 1.0e-8);
        LinearEquations ode = new LinearEquations();
        double[] y = new double[]{Math.E};

        double stopTime = integrator.integrate(ode, 1.0, y, 0.0, y);

        assertEquals(0.0, stopTime, 1.0e-10);
        assertEquals(1.0, y[0], 1.0e-5);
    }

    @Test(timeout = 4000)
    public void testBackwardIntegrationWithHighamHallNonFSAL() throws IntegratorException, DerivativeException {
        // Non-FSAL method backward integration
        EmbeddedRungeKuttaIntegrator integrator = new HighamHall54Integrator(1.0e-4, 1.0, 1.0e-8, 1.0e-8);
        LinearEquations ode = new LinearEquations();
        double[] y = new double[]{Math.E};

        double stopTime = integrator.integrate(ode, 1.0, y, 0.0, y);

        assertEquals(0.0, stopTime, 1.0e-10);
        assertEquals(1.0, y[0], 1.0e-5);
    }

    @Test(timeout = 4000)
    public void testEventAtIntegrationStartExactUlpMatch() throws IntegratorException, DerivativeException {
        // Math.abs(dt) <= Math.ulp(stepStart) branch
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        // Event occurs immediately at t = 0.0
        integrator.addEventHandler(new EventHandler() {
            @Override
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return CONTINUE;
            }

            @Override
            public double g(double t, double[] y) {
                return t - 0.0;
            }

            @Override
            public void resetState(double t, double[] y) {}
        }, 0.1, 1.0e-15, 100);

        LinearEquations ode = new LinearEquations();
        double[] y = new double[]{1.0};
        double tStop = integrator.integrate(ode, 0.0, y, 1.0, y);

        assertEquals(1.0, tStop, 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testEventTriggeringDerivativeReset() throws IntegratorException, DerivativeException {
        // manager.reset(stepStart, y) returns true branch
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        integrator.addEventHandler(new EventHandler() {
            private boolean triggered = false;

            @Override
            public int eventOccurred(double t, double[] y, boolean increasing) {
                triggered = true;
                return RESET_STATE;
            }

            @Override
            public double g(double t, double[] y) {
                return t - 0.5;
            }

            @Override
            public void resetState(double t, double[] y) {
                y[0] += 10.0; // Perturb state
            }
        }, 0.1, 1.0e-8, 100);

        LinearEquations ode = new LinearEquations();
        double[] y = new double[]{1.0};
        integrator.integrate(ode, 0.0, y, 1.0, y);

        assertTrue("State should be boosted by resetState event", y[0] > 10.0);
    }

    @Test(timeout = 4000)
    public void testEventTriggeringStop() throws IntegratorException, DerivativeException {
        // manager.stop() returns true branch
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        integrator.addEventHandler(new EventHandler() {
            @Override
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return STOP;
            }

            @Override
            public double g(double t, double[] y) {
                return t - 0.35;
            }

            @Override
            public void resetState(double t, double[] y) {}
        }, 0.1, 1.0e-6, 100);

        LinearEquations ode = new LinearEquations();
        double[] y = new double[]{1.0};
        double tStop = integrator.integrate(ode, 0.0, y, 1.0, y);

        assertEquals(0.35, tStop, 1.0e-4);
    }

    // =========================================================================
    // Partition C: Defect-Targeted & Step Rejection Zone
    // =========================================================================

    @Test(timeout = 4000)
    public void testStepRejectionAndRecovery() throws IntegratorException, DerivativeException {
        // Explicitly trigger the `error > 1.0` branch in the main loop
        double[] c = new double[]{0.5};
        double[][] a = new double[][]{{0.5}};
        double[] b = new double[]{0.0, 1.0};
        RungeKuttaStepInterpolator dummyInterp = new DormandPrince54StepInterpolator();

        DummyEmbeddedRKIntegrator integrator = new DummyEmbeddedRKIntegrator(
                "dummy", false, c, a, b, dummyInterp, 1.0e-6, 1.0, 1.0e-6, 1.0e-6, 2);

        // Force a high error once to enter rejection branch
        integrator.setForcedError(2.5);

        LinearEquations ode = new LinearEquations();
        double[] y = new double[]{1.0};
        double stopTime = integrator.integrate(ode, 0.0, y, 0.1, y);

        assertEquals(0.1, stopTime, 1.0e-8);
        assertTrue(integrator.getErrorEvalCount() >= 2);
    }

    @Test(timeout = 4000)
    public void testVectorToleranceConstructorAndExecution() throws IntegratorException, DerivativeException {
        double[] c = new double[]{0.5};
        double[][] a = new double[][]{{0.5}};
        double[] b = new double[]{0.0, 1.0};
        RungeKuttaStepInterpolator dummyInterp = new DormandPrince54StepInterpolator();

        double[] vecAbsTol = new double[]{1.0e-5};
        double[] vecRelTol = new double[]{1.0e-5};

        DummyEmbeddedRKIntegrator integrator = new DummyEmbeddedRKIntegrator(
                "dummyVec", true, c, a, b, dummyInterp, 1.0e-5, 1.0, vecAbsTol, vecRelTol, 2);

        LinearEquations ode = new LinearEquations();
        double[] y = new double[]{1.0};
        double stopTime = integrator.integrate(ode, 0.0, y, 0.05, y);

        assertEquals(0.05, stopTime, 1.0e-8);
    }

    @Test(timeout = 4000)
    public void testPolynomialIntegrationOrderConvergence() throws IntegratorException, DerivativeException {
        // ODE: y' = 3*t^2, y(0)=0 => y(t) = t^3
        // Targeting polynomial integration accuracy (defect specification target)
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince853Integrator(1.0e-6, 10.0, 1.0e-10, 1.0e-10);
        PolynomialEquations ode = new PolynomialEquations();
        double[] y = new double[]{0.0};

        double tEnd = 2.0;
        double stopTime = integrator.integrate(ode, 0.0, y, tEnd, y);

        assertEquals(tEnd, stopTime, 1.0e-12);
        double expected = tEnd * tEnd * tEnd; // 2.0^3 = 8.0
        assertEquals(expected, y[0], 1.0e-8);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IntegratorException.class, timeout = 4000)
    public void testDimensionMismatchBetweenEquationsAndY0() throws IntegratorException, DerivativeException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);
        LinearEquations ode = new LinearEquations(); // dimension = 1
        double[] y0 = new double[]{1.0, 2.0}; // dimension = 2

        integrator.integrate(ode, 0.0, y0, 1.0, new double[2]);
    }

    @Test(expected = IntegratorException.class, timeout = 4000)
    public void testDimensionMismatchBetweenY0AndY() throws IntegratorException, DerivativeException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);
        LinearEquations ode = new LinearEquations();
        double[] y0 = new double[]{1.0};
        double[] y = new double[]{1.0, 2.0};

        integrator.integrate(ode, 0.0, y0, 1.0, y);
    }

    @Test(expected = IntegratorException.class, timeout = 4000)
    public void testIntegrationIntervalTooSmall() throws IntegratorException, DerivativeException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);
        LinearEquations ode = new LinearEquations();
        double[] y = new double[]{1.0};

        // t0 == t1 triggers sanityCheck IntegratorException
        integrator.integrate(ode, 1.0, y, 1.0, y);
    }

    @Test(expected = IntegratorException.class, timeout = 4000)
    public void testStepSizeUnderflowException() throws IntegratorException, DerivativeException {
        // If minStep is too large relative to the integration span, or error forces
        // step to fall below minStep
        double[] c = new double[]{0.5};
        double[][] a = new double[][]{{0.5}};
        double[] b = new double[]{0.0, 1.0};
        RungeKuttaStepInterpolator dummyInterp = new DormandPrince54StepInterpolator();

        DummyEmbeddedRKIntegrator integrator = new DummyEmbeddedRKIntegrator(
                "dummy", false, c, a, b, dummyInterp, 0.5, 1.0, 1.0e-10, 1.0e-10, 2) {
            @Override
            protected double estimateError(double[][] yDotK, double[] y0, double[] y1, double h) {
                return 1.0e8; // Always reject, forcing step reduction below minStep
            }
        };

        LinearEquations ode = new LinearEquations();
        double[] y = new double[]{1.0};
        integrator.integrate(ode, 0.0, y, 1.0, y);
    }

    // =========================================================================
    // Partition E: Handler Reset and Clean Lifecycle
    // =========================================================================

    @Test(timeout = 4000)
    public void testHandlersResetInvocation() throws IntegratorException, DerivativeException {
        EmbeddedRungeKuttaIntegrator integrator = new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        final boolean[] handlerResetCalled = new boolean[1];
        integrator.addStepHandler(new StepHandler() {
            @Override
            public boolean isResetting() {
                return false;
            }

            @Override
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}

            @Override
            public void reset() {
                handlerResetCalled[0] = true;
            }
        });

        LinearEquations ode = new LinearEquations();
        double[] y = new double[]{1.0};
        integrator.integrate(ode, 0.0, y, 0.1, y);

        assertTrue("StepHandler.reset() must be called prior to integration loop", handlerResetCalled[0]);
    }
}