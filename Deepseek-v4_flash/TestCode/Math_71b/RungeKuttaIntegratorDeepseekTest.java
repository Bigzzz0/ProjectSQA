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
 * Branch & Defect Analysis Matrix:
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - Forward integration (t > t0) and backward integration (t < t0)
 *   - y != y0 array copy branch
 *   - Dense output vs dummy interpolator selection (requiresDenseOutput() or events present)
 *   - Main integration loop with stage computations
 *   - Step acceptance and state update
 *   - Step size reset to default after each step
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Zero step size (handled via Math.abs in constructor)
 *   - Very small step size near machine epsilon
 *   - Negative step (backward)
 *   - Null equations, null y0, null y (sanity checks)
 *   - Empty arrays (c.length = 0? But Butcher arrays always have at least one stage)
 * 
 * Partition C: Defect-Targeted Branch Zone (Missed End Event)
 *   - Event occurring exactly at step boundary (dt == 0) triggers artificial zero-size step
 *   - Event occurring very close to step start (Math.abs(dt) <= Math.ulp(stepStart)) leads to loop=false and state copy
 *   - Event occurring after step start but before end leads to stepSize = dt and loop continues
 *   - The known defect: event near end time is missed due to floating-point precision in step size adjustment
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - DerivativeException during computeDerivatives
 *   - IntegratorException from event handling
 *   - IllegalArgumentException from sanity checks
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Step handler reset called at start
 *   - Step handler handleStep called with correct lastStep flag
 *   - Event handler reset, evaluateStep, stepAccepted, stop, reset called in correct order
 *   - Interpolator storeTime and shift called appropriately
 */
public class RungeKuttaIntegratorDeepseekTest {

    // A simple ODE: dy/dt = 1, so y = t + constant
    private static final FirstOrderDifferentialEquations SIMPLE_ODE = new FirstOrderDifferentialEquations() {
        @Override
        public int getDimension() {
            return 1;
        }

        @Override
        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = 1.0;
        }
    };

    // Event handler that triggers at a specific time
    private static class SimpleEvent implements EventHandler {
        private final double triggerTime;
        private boolean hit = false;

        SimpleEvent(double triggerTime) {
            this.triggerTime = triggerTime;
        }

        @Override
        public double g(double t, double[] y) {
            return t - triggerTime;
        }

        @Override
        public int eventOccurred(double t, double[] y, boolean increasing) {
            hit = true;
            return STOP;
        }

        @Override
        public void resetState(double t, double[] y) {
            // no state to reset
        }
    }

    // Step handler that records the last step time
    private static class RecordingStepHandler implements StepHandler {
        double lastTime = Double.NaN;
        boolean lastStep = false;

        @Override
        public void handleStep(StepInterpolator interpolator, boolean isLast) {
            lastTime = interpolator.getInterpolatedTime();
            lastStep = isLast;
        }

        @Override
        public boolean requiresDenseOutput() {
            return false;
        }

        @Override
        public void reset() {
            lastTime = Double.NaN;
            lastStep = false;
        }
    }

    // Test forward integration without events
    @Test(timeout = 4000)
    public void testForwardIntegration() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        double endTime = 1.0;
        double stopTime = integrator.integrate(SIMPLE_ODE, 0.0, y0, endTime, y);
        assertEquals(endTime, stopTime, 1e-12);
        assertEquals(endTime, y[0], 1e-12);
    }

    // Test backward integration
    @Test(timeout = 4000)
    public void testBackwardIntegration() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        double[] y0 = new double[] { 1.0 };
        double[] y = new double[1];
        double endTime = 0.0;
        double stopTime = integrator.integrate(SIMPLE_ODE, 1.0, y0, endTime, y);
        assertEquals(endTime, stopTime, 1e-12);
        assertEquals(endTime, y[0], 1e-12);
    }

    // Test event that triggers exactly at a step boundary
    @Test(timeout = 4000)
    public void testEventAtStepBoundary() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.2);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        double eventTime = 0.4; // exactly two steps
        SimpleEvent event = new SimpleEvent(eventTime);
        integrator.addEventHandler(event, 0.1, 1e-6, 100);
        double stopTime = integrator.integrate(SIMPLE_ODE, 0.0, y0, 1.0, y);
        assertEquals(eventTime, stopTime, 1e-12);
        assertTrue(event.hit);
    }

    // Test the known defect: missed end event due to floating-point precision
    @Test(timeout = 4000)
    public void testMissedEndEvent() throws DerivativeException, IntegratorException {
        // Reproduce the defect from ClassicalRungeKuttaIntegratorTest::testMissedEndEvent
        // The event is at a time that is not exactly representable, causing step size truncation issues
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        // Event at t = 1.8782503799999986E9 (from defect description)
        double eventTime = 1.8782503799999986E9;
        SimpleEvent event = new SimpleEvent(eventTime);
        integrator.addEventHandler(event, 0.1, 1e-6, 100);
        double stopTime = integrator.integrate(SIMPLE_ODE, 0.0, y0, eventTime + 1.0, y);
        // The correct behavior is to stop exactly at the event time
        assertEquals(eventTime, stopTime, 1e-6); // tolerance due to floating-point
        assertTrue("Event should have been triggered", event.hit);
    }

    // Test dense output path (step handler requires dense output)
    @Test(timeout = 4000)
    public void testDenseOutput() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        RecordingStepHandler handler = new RecordingStepHandler() {
            @Override
            public boolean requiresDenseOutput() {
                return true; // force dense interpolator
            }
        };
        integrator.addStepHandler(handler);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        double endTime = 0.5;
        integrator.integrate(SIMPLE_ODE, 0.0, y0, endTime, y);
        assertFalse(Double.isNaN(handler.lastTime));
        assertTrue(handler.lastStep);
    }

    // Test dummy interpolator path (no dense output, no events)
    @Test(timeout = 4000)
    public void testDummyInterpolator() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        RecordingStepHandler handler = new RecordingStepHandler(); // requiresDenseOutput returns false
        integrator.addStepHandler(handler);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        double endTime = 0.5;
        integrator.integrate(SIMPLE_ODE, 0.0, y0, endTime, y);
        assertFalse(Double.isNaN(handler.lastTime));
        assertTrue(handler.lastStep);
    }

    // Test step rejection due to event very close to step start (dt <= ulp)
    @Test(timeout = 4000)
    public void testStepRejectionNearStart() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.2);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        // Event at t = 0.0 + epsilon (just after start)
        double eventTime = Math.ulp(0.0);
        SimpleEvent event = new SimpleEvent(eventTime);
        integrator.addEventHandler(event, 0.1, 1e-6, 100);
        double stopTime = integrator.integrate(SIMPLE_ODE, 0.0, y0, 1.0, y);
        assertEquals(eventTime, stopTime, 1e-12);
        assertTrue(event.hit);
    }

    // Test event that triggers a reset of state
    @Test(timeout = 4000)
    public void testEventWithReset() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        EventHandler resetEvent = new EventHandler() {
            private boolean triggered = false;

            @Override
            public double g(double t, double[] y) {
                return t - 0.5;
            }

            @Override
            public int eventOccurred(double t, double[] y, boolean increasing) {
                triggered = true;
                return RESET_STATE;
            }

            @Override
            public void resetState(double t, double[] y) {
                y[0] = 100.0; // reset to a different value
            }
        };
        integrator.addEventHandler(resetEvent, 0.1, 1e-6, 100);
        double stopTime = integrator.integrate(SIMPLE_ODE, 0.0, y0, 1.0, y);
        // After reset, the ODE continues from new state
        assertEquals(1.0, stopTime, 1e-12);
        // y should be approximately 100.0 + (1.0 - 0.5) = 100.5
        assertEquals(100.5, y[0], 1e-10);
    }

    // Test multiple events
    @Test(timeout = 4000)
    public void testMultipleEvents() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        SimpleEvent event1 = new SimpleEvent(0.3);
        SimpleEvent event2 = new SimpleEvent(0.7);
        integrator.addEventHandler(event1, 0.1, 1e-6, 100);
        integrator.addEventHandler(event2, 0.1, 1e-6, 100);
        double stopTime = integrator.integrate(SIMPLE_ODE, 0.0, y0, 1.0, y);
        // Should stop at the first event (0.3)
        assertEquals(0.3, stopTime, 1e-12);
        assertTrue(event1.hit);
        assertFalse(event2.hit);
    }

    // Test exception from sanity checks (null equations)
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullEquations() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        integrator.integrate(null, 0.0, y0, 1.0, y);
    }

    // Test exception from sanity checks (null y0)
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullY0() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        double[] y = new double[1];
        integrator.integrate(SIMPLE_ODE, 0.0, null, 1.0, y);
    }

    // Test exception from sanity checks (null y)
    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullY() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        double[] y0 = new double[] { 0.0 };
        integrator.integrate(SIMPLE_ODE, 0.0, y0, 1.0, null);
    }

    // Test that step size is absolute value (positive step)
    @Test(timeout = 4000)
    public void testStepSizeAbsolute() throws DerivativeException, IntegratorException {
        // Negative step in constructor should be made positive
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(-0.1);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        double endTime = 1.0;
        double stopTime = integrator.integrate(SIMPLE_ODE, 0.0, y0, endTime, y);
        assertEquals(endTime, stopTime, 1e-12);
        assertEquals(endTime, y[0], 1e-12);
    }

    // Test that y != y0 branch is taken (copy array)
    @Test(timeout = 4000)
    public void testDifferentYArray() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        // y is different from y0, so copy should happen
        integrator.integrate(SIMPLE_ODE, 0.0, y0, 1.0, y);
        assertEquals(1.0, y[0], 1e-12);
        // y0 should remain unchanged
        assertEquals(0.0, y0[0], 1e-12);
    }

    // Test that y == y0 branch is taken (no copy)
    @Test(timeout = 4000)
    public void testSameYArray() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        double[] y0 = new double[] { 0.0 };
        // Use same array for y0 and y
        double stopTime = integrator.integrate(SIMPLE_ODE, 0.0, y0, 1.0, y0);
        assertEquals(1.0, stopTime, 1e-12);
        assertEquals(1.0, y0[0], 1e-12);
    }

    // Test that step handler reset is called at start
    @Test(timeout = 4000)
    public void testStepHandlerReset() throws DerivativeException, IntegratorException {
        ClassicalRungeKuttaIntegrator integrator = new ClassicalRungeKuttaIntegrator(0.1);
        final boolean[] resetCalled = { false };
        integrator.addStepHandler(new StepHandler() {
            @Override
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}

            @Override
            public boolean requiresDenseOutput() { return false; }

            @Override
            public void reset() { resetCalled[0] = true; }
        });
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        integrator.integrate(SIMPLE_ODE, 0.0, y0, 0.5, y);
        assertTrue(resetCalled[0]);
    }
}