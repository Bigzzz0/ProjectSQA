package org.apache.commons.math.ode.nonstiff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target: EmbeddedRungeKuttaIntegrator.integrate()
 *
 * Defect scenario: an event occurring exactly on the boundary/start of an
 * accepted step is mishandled.  The artificial zero-step branch sets
 * loop = false without shrinking stepSize to dt, so the integrator overruns
 * the event time and returns the end time (1.878250439999994E9 instead of
 * 1.8782503799999986E9).
 *
 * Branches exercised by this suite:
 *  - scalar tolerance vs vector tolerance
 *  - y == y0 vs y != y0
 *  - dense output (RungeKuttaStepInterpolator) vs DummyStepInterpolator
 *  - event evaluation false/true, event truncation, event STOP/RESET_STATE
 *  - event time exactly at stepStart (defect-targeting ulp branch)
 *  - fsal derivative reuse
 *  - backward integration
 *  - rejected-step error control
 *  - safety/minReduction/maxGrowth setters/getters
 */
public class EmbeddedRungeKuttaIntegratorDeepseekTest {

    private static final double DEFECT_EVENT_TIME = 1.8782503799999986E9;
    private static final double DEFECT_END_TIME   = 1.878250439999994E9;

    private static class ConstantODE implements FirstOrderDifferentialEquations {
        public int getDimension() {
            return 1;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = 1.0;
        }
    }

    private static class ExpODE implements FirstOrderDifferentialEquations {
        public int getDimension() {
            return 1;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = y[0];
        }
    }

    private static class PiecewiseODE implements FirstOrderDifferentialEquations {
        public int getDimension() {
            return 1;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = t < 0.5 ? 0.0 : 1000.0;
        }
    }

    @Test(timeout = 4000)
    public void testScalarToleranceIntegrationSameArray() throws Exception {
        DormandPrince54Integrator integrator =
            new DormandPrince54Integrator(1.0e-12, 1.0, 1.0e-12, 1.0e-12);
        double[] y = { 1.0 };

        double stopTime = integrator.integrate(new ExpODE(), 0.0, y, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-9);
        assertEquals(Math.exp(1.0), y[0], 1.0e-9);
        assertTrue(integrator.getEvaluations() > 0);
    }

    @Test(timeout = 4000)
    public void testVectorToleranceWithDenseOutputHandler() throws Exception {
        double[] absTol = { 1.0e-12 };
        double[] relTol = { 1.0e-12 };
        DormandPrince54Integrator integrator =
            new DormandPrince54Integrator(1.0e-12, 1.0, absTol, relTol);
        integrator.addStepHandler(new StepHandler() {
            public void reset() {
            }

            public boolean requiresDenseOutput() {
                return true;
            }

            public void handleStep(StepInterpolator interpolator, boolean isLast) {
            }
        });

        double[] y0 = { 1.0 };
        double[] y = { 1.0 };
        double stopTime = integrator.integrate(new ExpODE(), 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-9);
        assertEquals(Math.exp(1.0), y[0], 1.0e-8);
    }

    @Test(timeout = 4000)
    public void testEventHandlerStopsAtSpecifiedTime() throws Exception {
        final double eventTime = 0.3;
        DormandPrince54Integrator integrator =
            new DormandPrince54Integrator(1.0e-12, 10.0, 1.0e-12, 1.0e-12);
        integrator.addEventHandler(new EventHandler() {
            public double g(double t, double[] y) {
                return t - eventTime;
            }

            public int eventOccurred(double t, double[] y, boolean increasing) {
                return EventHandler.STOP;
            }

            public void resetState(double t, double[] y) {
            }
        }, 10.0, 1.0e-9, 100);

        double[] y0 = { 0.0 };
        double[] y = { 0.0 };
        double stopTime = integrator.integrate(new ConstantODE(), 0.0, y0, 1.0, y);

        assertEquals(eventTime, stopTime, 1.0e-6);
        assertEquals(eventTime, y[0], 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testResetStateEventContinues() throws Exception {
        final double eventTime = 0.3;
        DormandPrince54Integrator integrator =
            new DormandPrince54Integrator(1.0e-12, 1.0, 1.0e-12, 1.0e-12);
        integrator.addEventHandler(new EventHandler() {
            public double g(double t, double[] y) {
                return t - eventTime;
            }

            public int eventOccurred(double t, double[] y, boolean increasing) {
                return EventHandler.RESET_STATE;
            }

            public void resetState(double t, double[] y) {
                y[0] = 0.0;
            }
        }, 1.0, 1.0e-9, 100);

        double[] y0 = { 1.0 };
        double[] y = { 1.0 };
        double stopTime = integrator.integrate(new ExpODE(), 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-9);
        assertEquals(Math.exp(0.7), y[0], 1.0e-8);
    }

    @Test(timeout = 4000)
    public void testBackwardIntegration() throws Exception {
        DormandPrince54Integrator integrator =
            new DormandPrince54Integrator(1.0e-12, 1.0, 1.0e-12, 1.0e-12);
        double[] y0 = { Math.exp(1.0) };
        double[] y = { Math.exp(1.0) };

        double stopTime = integrator.integrate(new ExpODE(), 1.0, y0, 0.0, y);

        assertEquals(0.0, stopTime, 1.0e-9);
        assertEquals(1.0, y[0], 1.0e-8);
    }

    @Test(timeout = 4000)
    public void testRejectedStepAtDiscontinuity() throws Exception {
        DormandPrince54Integrator integrator =
            new DormandPrince54Integrator(1.0e-12, 1000.0, 1.0e-10, 1.0e-10);
        double[] y0 = { 0.0 };
        double[] y = { 0.0 };

        double stopTime = integrator.integrate(new PiecewiseODE(), 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-9);
        assertEquals(500.0, y[0], 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testControlParameterSettersGetters() {
        EmbeddedRungeKuttaIntegrator integrator =
            new DormandPrince54Integrator(1.0e-12, 1.0, 1.0e-12, 1.0e-12);

        integrator.setSafety(0.75);
        assertEquals(0.75, integrator.getSafety(), 1.0e-12);

        integrator.setMinReduction(0.15);
        assertEquals(0.15, integrator.getMinReduction(), 1.0e-12);

        integrator.setMaxGrowth(12.0);
        assertEquals(12.0, integrator.getMaxGrowth(), 1.0e-12);

        assertTrue(integrator.getOrder() >= 5);
    }

    @Test(timeout = 4000)
    public void testMissedEndEvent() throws Exception {
        final double step = DEFECT_END_TIME - DEFECT_EVENT_TIME;
        final double startTime = DEFECT_EVENT_TIME - step;
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(step, step, 1.0e-12, 1.0e-12);
        integrator.addEventHandler(new EventHandler() {
            public double g(double t, double[] y) {
                return t - DEFECT_EVENT_TIME;
            }

            public int eventOccurred(double t, double[] y, boolean increasing) {
                return EventHandler.STOP;
            }

            public void resetState(double t, double[] y) {
            }
        }, step, 1.0e-6, 100);

        double[] y0 = { 0.0 };
        double[] y = { 0.0 };
        double stopTime = integrator.integrate(new ConstantODE(), startTime, y0, DEFECT_END_TIME, y);

        assertEquals(DEFECT_EVENT_TIME, stopTime, 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testEventExactlyAtStartIsNotSkipped() throws Exception {
        final double step = DEFECT_END_TIME - DEFECT_EVENT_TIME;
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(step, step, 1.0e-12, 1.0e-12);
        integrator.addEventHandler(new EventHandler() {
            public double g(double t, double[] y) {
                return t - DEFECT_EVENT_TIME;
            }

            public int eventOccurred(double t, double[] y, boolean increasing) {
                return EventHandler.STOP;
            }

            public void resetState(double t, double[] y) {
            }
        }, step, 1.0e-6, 100);

        double[] y0 = { 0.0 };
        double[] y = { 0.0 };
        double stopTime = integrator.integrate(new ConstantODE(), DEFECT_EVENT_TIME, y0, DEFECT_END_TIME, y);

        assertEquals(DEFECT_EVENT_TIME, stopTime, 1.0e-6);
    }
}