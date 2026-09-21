package org.apache.commons.math.ode.nonstiff;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: EmbeddedRungeKuttaIntegrator (abstract)
 * 
 * Decision branches covered:
 * - Constructor with scalar/vector tolerances
 * - getSafety/setSafety, getMinReduction/setMinReduction, getMaxGrowth/setMaxGrowth
 * - integrate() method:
 *   * forward vs backward (t > t0)
 *   * fsal == true vs false (firstTime branch, reuse of yDotK[0])
 *   * firstTime == true (initial step size computation)
 *   * error <= 1.0 (step acceptance) vs error > 1.0 (step rejection)
 *   * event handling: evaluateStep returns true/false, dt near zero, step rejection
 *   * step acceptance: arraycopy, manager.stepAccepted, lastStep, handler.handleStep
 *   * fsal save of last evaluation
 *   * manager.reset triggers recomputation of derivatives
 *   * stepSize == 0 after event (filterStep reset)
 *   * next step size control (scaledH, nextIsLast)
 * 
 * Boundary conditions:
 * - safety = 0, negative, large
 * - minReduction = 0, negative, >1
 * - maxGrowth = 0, negative, <1
 * - minStep <= 0 (constructor validation)
 * - maxStep < minStep
 * - null arrays in constructor (should not happen, but defensive)
 * 
 * Defect-targeted (from Defects4J ground truth):
 * - AdamsMoultonIntegratorTest::polynomial failure indicates a bug in step size control
 *   or error estimation in the base class. We reproduce with a polynomial ODE and
 *   a concrete subclass (DormandPrince853Integrator) to trigger the defect.
 */
public class EmbeddedRungeKuttaIntegratorDeepseekTest {

    // ---------- Helper classes ----------
    
    /** Simple ODE: y' = 2*t, solution y = t^2 + C */
    private static class PolynomialODE implements FirstOrderDifferentialEquations {
        public int getDimension() { return 1; }
        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = 2.0 * t;
        }
    }

    /** Step handler that records the last step time and state */
    private static class RecordingStepHandler implements StepHandler {
        double lastTime = Double.NaN;
        double[] lastState = null;
        boolean lastStep = false;
        public void reset() { lastTime = Double.NaN; lastState = null; lastStep = false; }
        public void handleStep(StepInterpolator interpolator, boolean isLast) throws DerivativeException {
            lastTime = interpolator.getPreviousTime();
            lastState = interpolator.getInterpolatedState();
            lastStep = isLast;
        }
    }

    /** Event handler that triggers at a specific time */
    private static class FixedTimeEvent implements EventHandler {
        private final double triggerTime;
        private boolean triggered = false;
        public FixedTimeEvent(double t) { this.triggerTime = t; }
        public double g(double t, double[] y) { return t - triggerTime; }
        public int eventOccurred(double t, double[] y, boolean increasing) { return STOP; }
        public void resetState(double t, double[] y) { triggered = true; }
    }

    // ---------- Concrete subclass for testing (simple embedded RK) ----------
    // Use a 2-stage embedded method: Heun's method with error estimation
    // Butcher tableau:
    // 0 |
    // 1 | 1
    // --+--------
    //   | 1/2  1/2   (b)
    //   | 1    0     (b') -> error = b - b' = [-1/2, 1/2]
    // Order = 2
    private static class SimpleEmbeddedRK extends EmbeddedRungeKuttaIntegrator {
        public SimpleEmbeddedRK(boolean fsal, double minStep, double maxStep,
                                double scalAbsTol, double scalRelTol) {
            super("Simple", fsal,
                  new double[] { 1.0 },           // c
                  new double[][] { { 1.0 } },     // a
                  new double[] { 0.5, 0.5 },      // b
                  new DummyStepInterpolator(),    // prototype (not used)
                  minStep, maxStep, scalAbsTol, scalRelTol);
        }
        public int getOrder() { return 2; }
        protected double estimateError(double[][] yDotK, double[] y0, double[] y1, double h) {
            // error = |y1 - y1_low| / (scalAbsTol + scalRelTol * max(|y0|,|y1|))
            double err = 0;
            for (int i = 0; i < y0.length; i++) {
                double y0i = Math.abs(y0[i]);
                double y1i = Math.abs(y1[i]);
                double scale = 1.0e-6 + 1.0e-6 * Math.max(y0i, y1i);
                // low-order estimate: y1_low = y0 + h * (1*yDotK[0] + 0*yDotK[1]) = y0 + h*yDotK[0]
                double y1Low = y0[i] + h * yDotK[0][i];
                double diff = y1[i] - y1Low;
                err += (diff * diff) / (scale * scale);
            }
            return Math.sqrt(err / y0.length);
        }
    }

    // ---------- Tests for getters/setters ----------

    @Test(timeout = 4000)
    public void testSafetyGetterSetter() {
        SimpleEmbeddedRK integrator = new SimpleEmbeddedRK(false, 1e-6, 1.0, 1e-6, 1e-6);
        assertEquals(0.9, integrator.getSafety(), 1e-15);
        integrator.setSafety(0.5);
        assertEquals(0.5, integrator.getSafety(), 1e-15);
        integrator.setSafety(0.0);
        assertEquals(0.0, integrator.getSafety(), 1e-15);
        integrator.setSafety(-1.0);
        assertEquals(-1.0, integrator.getSafety(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMinReductionGetterSetter() {
        SimpleEmbeddedRK integrator = new SimpleEmbeddedRK(false, 1e-6, 1.0, 1e-6, 1e-6);
        assertEquals(0.2, integrator.getMinReduction(), 1e-15);
        integrator.setMinReduction(0.1);
        assertEquals(0.1, integrator.getMinReduction(), 1e-15);
        integrator.setMinReduction(0.0);
        assertEquals(0.0, integrator.getMinReduction(), 1e-15);
        integrator.setMinReduction(-0.5);
        assertEquals(-0.5, integrator.getMinReduction(), 1e-15);
        integrator.setMinReduction(1.5);
        assertEquals(1.5, integrator.getMinReduction(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMaxGrowthGetterSetter() {
        SimpleEmbeddedRK integrator = new SimpleEmbeddedRK(false, 1e-6, 1.0, 1e-6, 1e-6);
        assertEquals(10.0, integrator.getMaxGrowth(), 1e-15);
        integrator.setMaxGrowth(5.0);
        assertEquals(5.0, integrator.getMaxGrowth(), 1e-15);
        integrator.setMaxGrowth(0.0);
        assertEquals(0.0, integrator.getMaxGrowth(), 1e-15);
        integrator.setMaxGrowth(-2.0);
        assertEquals(-2.0, integrator.getMaxGrowth(), 1e-15);
        integrator.setMaxGrowth(0.5);
        assertEquals(0.5, integrator.getMaxGrowth(), 1e-15);
    }

    // ---------- Tests for integrate() ----------

    @Test(timeout = 4000)
    public void testIntegrateForwardSimple() throws DerivativeException, IntegratorException {
        SimpleEmbeddedRK integrator = new SimpleEmbeddedRK(false, 1e-6, 1.0, 1e-6, 1e-6);
        PolynomialODE ode = new PolynomialODE();
        double[] y = new double[] { 0.0 };
        double tEnd = integrator.integrate(ode, 0.0, y, 1.0, y);
        assertEquals(1.0, tEnd, 1e-12);
        assertEquals(1.0, y[0], 1e-6); // y(1) = 1^2 = 1
    }

    @Test(timeout = 4000)
    public void testIntegrateBackward() throws DerivativeException, IntegratorException {
        SimpleEmbeddedRK integrator = new SimpleEmbeddedRK(false, 1e-6, 1.0, 1e-6, 1e-6);
        PolynomialODE ode = new PolynomialODE();
        double[] y = new double[] { 1.0 };
        double tEnd = integrator.integrate(ode, 1.0, y, 0.0, y);
        assertEquals(0.0, tEnd, 1e-12);
        assertEquals(0.0, y[0], 1e-6); // y(0) = 0^2 = 0
    }

    @Test(timeout = 4000)
    public void testIntegrateWithFsal() throws DerivativeException, IntegratorException {
        // fsal = true
        SimpleEmbeddedRK integrator = new SimpleEmbeddedRK(true, 1e-6, 1.0, 1e-6, 1e-6);
        PolynomialODE ode = new PolynomialODE();
        double[] y = new double[] { 0.0 };
        double tEnd = integrator.integrate(ode, 0.0, y, 1.0, y);
        assertEquals(1.0, tEnd, 1e-12);
        assertEquals(1.0, y[0], 1e-6);
    }

    @Test(timeout = 4000)
    public void testIntegrateWithStepHandler() throws DerivativeException, IntegratorException {
        SimpleEmbeddedRK integrator = new SimpleEmbeddedRK(false, 1e-6, 1.0, 1e-6, 1e-6);
        RecordingStepHandler handler = new RecordingStepHandler();
        integrator.addStepHandler(handler);
        PolynomialODE ode = new PolynomialODE();
        double[] y = new double[] { 0.0 };
        integrator.integrate(ode, 0.0, y, 1.0, y);
        assertTrue(handler.lastStep);
        assertEquals(1.0, handler.lastTime, 1e-12);
        assertNotNull(handler.lastState);
        assertEquals(1.0, handler.lastState[0], 1e-6);
    }

    @Test(timeout = 4000)
    public void testIntegrateWithEvent() throws DerivativeException, IntegratorException {
        SimpleEmbeddedRK integrator = new SimpleEmbeddedRK(false, 1e-6, 1.0, 1e-6, 1e-6);
        FixedTimeEvent event = new FixedTimeEvent(0.5);
        integrator.addEventHandler(event, 1.0, 1e-6, 100);
        PolynomialODE ode = new PolynomialODE();
        double[] y = new double[] { 0.0 };
        double tEnd = integrator.integrate(ode, 0.0, y, 1.0, y);
        assertEquals(1.0, tEnd, 1e-12);
        assertEquals(1.0, y[0], 1e-6);
        assertTrue(event.triggered);
    }

    @Test(timeout = 4000)
    public void testIntegrateWithEventAtStart() throws DerivativeException, IntegratorException {
        // Event at t=0 should cause stepSize=0 and then filterStep reset
        SimpleEmbeddedRK integrator = new SimpleEmbeddedRK(false, 1e-6, 1.0, 1e-6, 1e-6);
        FixedTimeEvent event = new FixedTimeEvent(0.0);
        integrator.addEventHandler(event, 1.0, 1e-6, 100);
        PolynomialODE ode = new PolynomialODE();
        double[] y = new double[] { 0.0 };
        double tEnd = integrator.integrate(ode, 0.0, y, 1.0, y);
        assertEquals(1.0, tEnd, 1e-12);
        assertEquals(1.0, y[0], 1e-6);
    }

    @Test(timeout = 4000)
    public void testIntegrateWithStepRejection() throws DerivativeException, IntegratorException {
        // Force step rejection by setting very small minReduction and large initial step
        // Use a simple ODE that will cause error > 1 initially
        SimpleEmbeddedRK integrator = new SimpleEmbeddedRK(false, 1e-6, 10.0, 1e-6, 1e-6);
        integrator.setSafety(0.9);
        integrator.setMinReduction(0.2);
        integrator.setMaxGrowth(10.0);
        // The initial step size will be large, causing error > 1, triggering rejection
        PolynomialODE ode = new PolynomialODE();
        double[] y = new double[] { 0.0 };
        double tEnd = integrator.integrate(ode, 0.0, y, 1.0, y);
        assertEquals(1.0, tEnd, 1e-12);
        assertEquals(1.0, y[0], 1e-6);
    }

    // ---------- Defect-targeted test (polynomial integration) ----------

    @Test(timeout = 4000)
    public void testPolynomialIntegrationWithHighOrder() throws DerivativeException, IntegratorException {
        // Use DormandPrince853Integrator (concrete subclass) to integrate a polynomial ODE
        // This test is designed to reveal the defect that caused AdamsMoultonIntegratorTest::polynomial to fail.
        // The defect is in the base class step size control or error estimation.
        DormandPrince853Integrator integrator = new DormandPrince853Integrator(1e-8, 1.0, 1e-10, 1e-10);
        PolynomialODE ode = new PolynomialODE();
        double[] y = new double[] { 0.0 };
        double tEnd = integrator.integrate(ode, 0.0, y, 1.0, y);
        assertEquals(1.0, tEnd, 1e-12);
        // Expected y(1) = 1.0, with high accuracy
        assertEquals(1.0, y[0], 1e-10);
    }

    // ---------- Additional boundary tests ----------

    @Test(timeout = 4000)
    public void testConstructorWithVectorTolerances() {
        double[] absTol = { 1e-6 };
        double[] relTol = { 1e-6 };
        // Use anonymous subclass to test vector constructor
        EmbeddedRungeKuttaIntegrator integrator = new EmbeddedRungeKuttaIntegrator(
                "Test", false, new double[]{1.0}, new double[][]{{1.0}}, new double[]{0.5,0.5},
                new DummyStepInterpolator(), 1e-6, 1.0, absTol, relTol) {
            public int getOrder() { return 2; }
            protected double estimateError(double[][] yDotK, double[] y0, double[] y1, double h) { return 0; }
        };
        assertNotNull(integrator);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorWithNegativeMinStep() {
        new SimpleEmbeddedRK(false, -1.0, 1.0, 1e-6, 1e-6);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorWithMinStepGreaterThanMaxStep() {
        new SimpleEmbeddedRK(false, 2.0, 1.0, 1e-6, 1e-6);
    }

    @Test(timeout = 4000)
    public void testIntegrateWithZeroTolerance() throws DerivativeException, IntegratorException {
        // Zero tolerance should not cause division by zero (scale = 0)
        SimpleEmbeddedRK integrator = new SimpleEmbeddedRK(false, 1e-6, 1.0, 0.0, 0.0);
        PolynomialODE ode = new PolynomialODE();
        double[] y = new double[] { 0.0 };
        double tEnd = integrator.integrate(ode, 0.0, y, 1.0, y);
        assertEquals(1.0, tEnd, 1e-12);
        // Result may be less accurate due to zero tolerance, but should not crash
        assertTrue(Double.isFinite(y[0]));
    }

    @Test(timeout = 4000)
    public void testIntegrateWithMultipleSteps() throws DerivativeException, IntegratorException {
        // Use very small maxStep to force many steps
        SimpleEmbeddedRK integrator = new SimpleEmbeddedRK(false, 1e-6, 0.01, 1e-6, 1e-6);
        PolynomialODE ode = new PolynomialODE();
        double[] y = new double[] { 0.0 };
        double tEnd = integrator.integrate(ode, 0.0, y, 1.0, y);
        assertEquals(1.0, tEnd, 1e-12);
        assertEquals(1.0, y[0], 1e-4); // looser tolerance due to many steps
    }

    @Test(timeout = 4000)
    public void testIntegrateWithNoEventsAndDenseOutput() throws DerivativeException, IntegratorException {
        // requiresDenseOutput() returns true if step handlers are present
        SimpleEmbeddedRK integrator = new SimpleEmbeddedRK(false, 1e-6, 1.0, 1e-6, 1e-6);
        integrator.addStepHandler(new RecordingStepHandler());
        PolynomialODE ode = new PolynomialODE();
        double[] y = new double[] { 0.0 };
        double tEnd = integrator.integrate(ode, 0.0, y, 1.0, y);
        assertEquals(1.0, tEnd, 1e-12);
        assertEquals(1.0, y[0], 1e-6);
    }

    @Test(timeout = 4000)
    public void testIntegrateWithReset() throws DerivativeException, IntegratorException {
        // Event that triggers reset (resetState sets a flag, but we just need to cover the branch)
        SimpleEmbeddedRK integrator = new SimpleEmbeddedRK(false, 1e-6, 1.0, 1e-6, 1e-6);
        EventHandler resetEvent = new EventHandler() {
            public double g(double t, double[] y) { return t - 0.5; }
            public int eventOccurred(double t, double[] y, boolean increasing) { return RESET_STATE; }
            public void resetState(double t, double[] y) { /* no-op */ }
        };
        integrator.addEventHandler(resetEvent, 1.0, 1e-6, 100);
        PolynomialODE ode = new PolynomialODE();
        double[] y = new double[] { 0.0 };
        double tEnd = integrator.integrate(ode, 0.0, y, 1.0, y);
        assertEquals(1.0, tEnd, 1e-12);
        assertEquals(1.0, y[0], 1e-6);
    }
}