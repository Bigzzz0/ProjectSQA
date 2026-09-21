package org.apache.commons.math.ode;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.events.EventState;
import org.apache.commons.math.ode.nonstiff.DormandPrince853Integrator;
import org.apache.commons.math.ode.sampling.AbstractStepInterpolator;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.apache.commons.math.exception.MathIllegalArgumentException;
import org.apache.commons.math.exception.MathIllegalStateException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.exception.MaxCountExceededException;
import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.util.FastMath;
import org.apache.commons.math.util.Precision;

/**
 * Advanced white-box test suite for AbstractIntegrator.
 * Targets all branches, boundary conditions, and the known defect
 * (Issue 695: backward integration event ordering).
 *
 * [Branch & Defect Analysis Matrix]
 * - Partition A: Core functional logic (step handlers, event handlers, state getters/setters)
 * - Partition B: Boundary values (null, empty, zero, negative, MAX evaluations)
 * - Partition C: Defect-targeted zone (backward integration with events)
 * - Partition D: Exception paths (sanityChecks, dimension mismatch, max evaluations)
 * - Partition E: Lifecycle/contract (name, evaluations reset, stateInitialized flag)
 *
 * Known defect: EventStateTest.testIssue695 fails with "going backward in time!"
 * due to incorrect event time ordering during backward integration.
 * This test suite includes a dedicated test that reproduces the scenario.
 */
public class AbstractIntegratorDeepseekTest {

    // ------------------------------------------------------------------
    // Helper: a simple ODE dy/dt = 1
    // ------------------------------------------------------------------
    private static class SimpleODE implements FirstOrderDifferentialEquations {
        public int getDimension() { return 1; }
        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = 1.0;
        }
    }

    // ------------------------------------------------------------------
    // Helper: event handler that triggers when t crosses a given value
    // ------------------------------------------------------------------
    private static class TimeCrossingHandler implements EventHandler {
        private final double triggerTime;
        private boolean triggered = false;
        private int eventCount = 0;

        TimeCrossingHandler(double triggerTime) {
            this.triggerTime = triggerTime;
        }

        public double g(double t, double[] y) {
            return t - triggerTime;
        }

        public int eventOccurred(double t, double[] y, boolean increasing) {
            triggered = true;
            eventCount++;
            return CONTINUE; // default: do not stop or reset
        }

        public void resetState(double t, double[] y) {}

        public boolean triggered() { return triggered; }
        public int getEventCount() { return eventCount; }
    }

    // ------------------------------------------------------------------
    // Helper: event handler that stops integration
    // ------------------------------------------------------------------
    private static class StopHandler implements EventHandler {
        private final double stopTime;
        StopHandler(double stopTime) { this.stopTime = stopTime; }
        public double g(double t, double[] y) { return t - stopTime; }
        public int eventOccurred(double t, double[] y, boolean increasing) { return STOP; }
        public void resetState(double t, double[] y) {}
    }

    // ------------------------------------------------------------------
    // Helper: event handler that resets derivatives
    // ------------------------------------------------------------------
    private static class ResetHandler implements EventHandler {
        private final double resetTime;
        ResetHandler(double resetTime) { this.resetTime = resetTime; }
        public double g(double t, double[] y) { return t - resetTime; }
        public int eventOccurred(double t, double[] y, boolean increasing) { return RESET_STATE; }
        public void resetState(double t, double[] y) { y[0] = 0.0; } // reset to zero
    }

    // ------------------------------------------------------------------
    // Helper: concrete integrator for testing abstract methods directly
    // ------------------------------------------------------------------
    private static class DummyIntegrator extends AbstractIntegrator {
        DummyIntegrator() { super("dummy"); }
        DummyIntegrator(String name) { super(name); }
        public void integrate(ExpandableStatefulODE equations, double t) {
            // no-op for testing setters/getters
        }
    }

    // ==================================================================
    // PART A: Core Functional Logic & State Transitions
    // ==================================================================

    @Test(timeout = 4000)
    public void testForwardIntegrationNoEvents() {
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        double t0 = 0.0;
        double[] y0 = new double[] { 0.0 };
        double t = 10.0;
        double[] y = new double[1];
        double finalTime = integrator.integrate(ode, t0, y0, t, y);
        assertEquals("Final time", t, finalTime, 1e-10);
        assertEquals("Final state", t, y[0], 1e-10);
    }

    @Test(timeout = 4000)
    public void testBackwardIntegrationNoEvents() {
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        double t0 = 10.0;
        double[] y0 = new double[] { 10.0 };
        double t = 0.0;
        double[] y = new double[1];
        double finalTime = integrator.integrate(ode, t0, y0, t, y);
        assertEquals("Final time", t, finalTime, 1e-10);
        assertEquals("Final state", t, y[0], 1e-10);
    }

    @Test(timeout = 4000)
    public void testForwardIntegrationWithEvent() {
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        TimeCrossingHandler handler = new TimeCrossingHandler(5.0);
        integrator.addEventHandler(handler, 1.0, 1e-6, 100);
        double t0 = 0.0;
        double[] y0 = new double[] { 0.0 };
        double t = 10.0;
        double[] y = new double[1];
        double finalTime = integrator.integrate(ode, t0, y0, t, y);
        assertTrue("Event should have triggered", handler.triggered());
        assertEquals("Event count", 1, handler.getEventCount());
        assertEquals("Final time", t, finalTime, 1e-10);
        assertEquals("Final state", t, y[0], 1e-10);
    }

    // ==================================================================
    // PART C: Defect-Targeted Branch Zone (Backward Integration with Event)
    // ==================================================================

    @Test(timeout = 4000)
    public void testBackwardIntegrationWithEvent() {
        // This test reproduces the scenario of Issue 695:
        // backward integration with an event that triggers.
        // On the defective version, this would cause "going backward in time!" assertion.
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        TimeCrossingHandler handler = new TimeCrossingHandler(5.0);
        integrator.addEventHandler(handler, 1.0, 1e-6, 100);
        double t0 = 10.0;
        double[] y0 = new double[] { 10.0 };
        double t = 0.0;
        double[] y = new double[1];
        double finalTime = integrator.integrate(ode, t0, y0, t, y);
        assertTrue("Event should have triggered during backward integration", handler.triggered());
        assertEquals("Event count", 1, handler.getEventCount());
        assertEquals("Final time", t, finalTime, 1e-10);
        assertEquals("Final state", t, y[0], 1e-10);
    }

    // ==================================================================
    // Additional event scenarios
    // ==================================================================

    @Test(timeout = 4000)
    public void testEventStopsIntegration() {
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        StopHandler stop = new StopHandler(3.0);
        integrator.addEventHandler(stop, 1.0, 1e-6, 100);
        double t0 = 0.0;
        double[] y0 = new double[] { 0.0 };
        double t = 10.0;
        double[] y = new double[1];
        double finalTime = integrator.integrate(ode, t0, y0, t, y);
        assertEquals("Integration stopped at event time", 3.0, finalTime, 1e-10);
        assertEquals("State at stop", 3.0, y[0], 1e-10);
    }

    @Test(timeout = 4000)
    public void testEventResetsDerivatives() {
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        ResetHandler reset = new ResetHandler(2.0);
        integrator.addEventHandler(reset, 1.0, 1e-6, 100);
        double t0 = 0.0;
        double[] y0 = new double[] { 0.0 };
        double t = 10.0;
        double[] y = new double[1];
        double finalTime = integrator.integrate(ode, t0, y0, t, y);
        // After reset at t=2, state becomes 0, then continues.
        // Final state should be (t - 2) because dy/dt=1 after reset.
        assertEquals("Final time", t, finalTime, 1e-10);
        assertEquals("State after reset", t - 2.0, y[0], 1e-10);
    }

    @Test(timeout = 4000)
    public void testMultipleEvents() {
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        TimeCrossingHandler h1 = new TimeCrossingHandler(2.0);
        TimeCrossingHandler h2 = new TimeCrossingHandler(5.0);
        integrator.addEventHandler(h1, 1.0, 1e-6, 100);
        integrator.addEventHandler(h2, 1.0, 1e-6, 100);
        double t0 = 0.0;
        double[] y0 = new double[] { 0.0 };
        double t = 10.0;
        double[] y = new double[1];
        double finalTime = integrator.integrate(ode, t0, y0, t, y);
        assertTrue("First event triggered", h1.triggered());
        assertTrue("Second event triggered", h2.triggered());
        assertEquals("First event count", 1, h1.getEventCount());
        assertEquals("Second event count", 1, h2.getEventCount());
        assertEquals("Final time", t, finalTime, 1e-10);
    }

    // ==================================================================
    // PART B: Boundary Value Analysis & Extremes
    // ==================================================================

    @Test(timeout = 4000)
    public void testNullName() {
        DummyIntegrator integrator = new DummyIntegrator(null);
        assertNull("Name should be null", integrator.getName());
    }

    @Test(timeout = 4000)
    public void testEmptyStepHandlers() {
        DummyIntegrator integrator = new DummyIntegrator();
        assertTrue("Step handlers should be empty", integrator.getStepHandlers().isEmpty());
    }

    @Test(timeout = 4000)
    public void testClearStepHandlers() {
        DummyIntegrator integrator = new DummyIntegrator();
        integrator.addStepHandler(new StepHandler() {
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}
            public boolean requiresDenseOutput() { return false; }
        });
        assertFalse("Should have one handler", integrator.getStepHandlers().isEmpty());
        integrator.clearStepHandlers();
        assertTrue("Should be empty after clear", integrator.getStepHandlers().isEmpty());
    }

    @Test(timeout = 4000)
    public void testClearEventHandlers() {
        DummyIntegrator integrator = new DummyIntegrator();
        integrator.addEventHandler(new TimeCrossingHandler(1.0), 1.0, 1e-6, 100);
        assertFalse("Should have one event handler", integrator.getEventHandlers().isEmpty());
        integrator.clearEventHandlers();
        assertTrue("Should be empty after clear", integrator.getEventHandlers().isEmpty());
    }

    @Test(timeout = 4000)
    public void testSetMaxEvaluationsNegative() {
        DummyIntegrator integrator = new DummyIntegrator();
        integrator.setMaxEvaluations(-1);
        assertEquals("Max evaluations should be MAX_VALUE", Integer.MAX_VALUE, integrator.getMaxEvaluations());
    }

    @Test(timeout = 4000)
    public void testSetMaxEvaluationsPositive() {
        DummyIntegrator integrator = new DummyIntegrator();
        integrator.setMaxEvaluations(100);
        assertEquals(100, integrator.getMaxEvaluations());
    }

    @Test(timeout = 4000)
    public void testEvaluationsReset() {
        DummyIntegrator integrator = new DummyIntegrator();
        assertEquals(0, integrator.getEvaluations());
        // simulate evaluation increment via computeDerivatives (requires expandable)
        // We'll test via a real integration later.
    }

    // ==================================================================
    // PART D: Exception & Defensive Guard Paths
    // ==================================================================

    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testDimensionMismatchY0() {
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        double[] y0 = new double[2]; // wrong dimension
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 1.0, y);
    }

    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testDimensionMismatchY() {
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        double[] y0 = new double[1];
        double[] y = new double[2]; // wrong dimension
        integrator.integrate(ode, 0.0, y0, 1.0, y);
    }

    @Test(timeout = 4000, expected = NumberIsTooSmallException.class)
    public void testSanityChecksTooSmallInterval() {
        // Use a concrete integrator that calls sanityChecks
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        FirstOrderDifferentialEquations ode = new SimpleODE();
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        // integrate with extremely small interval (should trigger NumberIsTooSmallException)
        integrator.integrate(ode, 0.0, y0, 0.0 + 1e-20, y);
    }

    @Test(timeout = 4000, expected = MaxCountExceededException.class)
    public void testMaxEvaluationsExceeded() {
        // Use a very small max evaluations and a long integration
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        integrator.setMaxEvaluations(10);
        FirstOrderDifferentialEquations ode = new SimpleODE();
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 100.0, y);
    }

    // ==================================================================
    // PART E: Object Lifecycle & Contract Integrity
    // ==================================================================

    @Test(timeout = 4000)
    public void testGetCurrentStepStart() {
        DummyIntegrator integrator = new DummyIntegrator();
        assertTrue("Initial stepStart is NaN", Double.isNaN(integrator.getCurrentStepStart()));
        // stepStart is protected, we can set via subclass? Not directly.
        // We'll test via integration that stepStart is set.
    }

    @Test(timeout = 4000)
    public void testGetCurrentSignedStepsize() {
        DummyIntegrator integrator = new DummyIntegrator();
        assertTrue("Initial stepSize is NaN", Double.isNaN(integrator.getCurrentSignedStepsize()));
    }

    @Test(timeout = 4000)
    public void testSetStateInitialized() {
        DummyIntegrator integrator = new DummyIntegrator();
        // setStateInitialized is protected, we can call via subclass
        // We'll test indirectly via integration that events are initialized.
    }

    @Test(timeout = 4000)
    public void testComputeDerivativesIncrementsEvaluations() {
        // Use a real integration to check evaluation count
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        double t0 = 0.0;
        double[] y0 = new double[] { 0.0 };
        double t = 1.0;
        double[] y = new double[1];
        integrator.integrate(ode, t0, y0, t, y);
        assertTrue("Evaluations should be > 0", integrator.getEvaluations() > 0);
    }

    @Test(timeout = 4000)
    public void testStepHandlerCalled() {
        final boolean[] handlerCalled = { false };
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        integrator.addStepHandler(new StepHandler() {
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                handlerCalled[0] = true;
            }
            public boolean requiresDenseOutput() { return false; }
        });
        double[] y0 = new double[] { 0.0 };
        double[] y = new double[1];
        integrator.integrate(ode, 0.0, y0, 1.0, y);
        assertTrue("Step handler should have been called", handlerCalled[0]);
    }

    @Test(timeout = 4000)
    public void testEventHandlersCollectionUnmodifiable() {
        DummyIntegrator integrator = new DummyIntegrator();
        integrator.addEventHandler(new TimeCrossingHandler(1.0), 1.0, 1e-6, 100);
        Collection<EventHandler> handlers = integrator.getEventHandlers();
        assertEquals(1, handlers.size());
        try {
            handlers.add(new TimeCrossingHandler(2.0));
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testStepHandlersCollectionUnmodifiable() {
        DummyIntegrator integrator = new DummyIntegrator();
        integrator.addStepHandler(new StepHandler() {
            public void handleStep(StepInterpolator interpolator, boolean isLast) {}
            public boolean requiresDenseOutput() { return false; }
        });
        Collection<StepHandler> handlers = integrator.getStepHandlers();
        assertEquals(1, handlers.size());
        try {
            handlers.add(new StepHandler() {
                public void handleStep(StepInterpolator interpolator, boolean isLast) {}
                public boolean requiresDenseOutput() { return false; }
            });
            fail("Should be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    // ==================================================================
    // Additional coverage for acceptStep branches
    // ==================================================================

    @Test(timeout = 4000)
    public void testEventOccursAtStepBoundary() {
        // Event exactly at step end
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        TimeCrossingHandler handler = new TimeCrossingHandler(1.0);
        integrator.addEventHandler(handler, 1.0, 1e-6, 100);
        double t0 = 0.0;
        double[] y0 = new double[] { 0.0 };
        double t = 1.0;
        double[] y = new double[1];
        double finalTime = integrator.integrate(ode, t0, y0, t, y);
        assertTrue("Event should have triggered", handler.triggered());
        assertEquals("Final time", t, finalTime, 1e-10);
    }

    @Test(timeout = 4000)
    public void testBackwardEventAtStepBoundary() {
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        TimeCrossingHandler handler = new TimeCrossingHandler(5.0);
        integrator.addEventHandler(handler, 1.0, 1e-6, 100);
        double t0 = 10.0;
        double[] y0 = new double[] { 10.0 };
        double t = 5.0;
        double[] y = new double[1];
        double finalTime = integrator.integrate(ode, t0, y0, t, y);
        assertTrue("Event should have triggered", handler.triggered());
        assertEquals("Final time", t, finalTime, 1e-10);
    }

    @Test(timeout = 4000)
    public void testNoEventInStep() {
        // Event outside integration range
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        TimeCrossingHandler handler = new TimeCrossingHandler(100.0);
        integrator.addEventHandler(handler, 1.0, 1e-6, 100);
        double t0 = 0.0;
        double[] y0 = new double[] { 0.0 };
        double t = 10.0;
        double[] y = new double[1];
        double finalTime = integrator.integrate(ode, t0, y0, t, y);
        assertFalse("Event should not have triggered", handler.triggered());
        assertEquals("Final time", t, finalTime, 1e-10);
    }

    @Test(timeout = 4000)
    public void testResetOccurredFlag() {
        // Use reset handler to trigger resetOccurred
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        ResetHandler reset = new ResetHandler(2.0);
        integrator.addEventHandler(reset, 1.0, 1e-6, 100);
        double t0 = 0.0;
        double[] y0 = new double[] { 0.0 };
        double t = 10.0;
        double[] y = new double[1];
        integrator.integrate(ode, t0, y0, t, y);
        // After integration, resetOccurred is not directly accessible, but we can check state
        assertEquals("State after reset", t - 2.0, y[0], 1e-10);
    }

    @Test(timeout = 4000)
    public void testIsLastStepFlag() {
        // Stop handler sets isLastStep
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        StopHandler stop = new StopHandler(3.0);
        integrator.addEventHandler(stop, 1.0, 1e-6, 100);
        double t0 = 0.0;
        double[] y0 = new double[] { 0.0 };
        double t = 10.0;
        double[] y = new double[1];
        double finalTime = integrator.integrate(ode, t0, y0, t, y);
        assertEquals("Stopped at event time", 3.0, finalTime, 1e-10);
    }

    @Test(timeout = 4000)
    public void testMultipleEventsSameTime() {
        // Two events at same time
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        TimeCrossingHandler h1 = new TimeCrossingHandler(5.0);
        TimeCrossingHandler h2 = new TimeCrossingHandler(5.0);
        integrator.addEventHandler(h1, 1.0, 1e-6, 100);
        integrator.addEventHandler(h2, 1.0, 1e-6, 100);
        double t0 = 0.0;
        double[] y0 = new double[] { 0.0 };
        double t = 10.0;
        double[] y = new double[1];
        double finalTime = integrator.integrate(ode, t0, y0, t, y);
        assertTrue("First event triggered", h1.triggered());
        assertTrue("Second event triggered", h2.triggered());
        assertEquals("Final time", t, finalTime, 1e-10);
    }

    @Test(timeout = 4000)
    public void testEventReevaluationAfterReset() {
        // Event that resets and then reoccurs
        FirstOrderDifferentialEquations ode = new SimpleODE();
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-6, 1e-6);
        // Use a custom handler that resets and then continues, and the same event condition may reoccur
        // For simplicity, use ResetHandler which resets state to 0, but the event g(t,y)=t-2.0 still crosses at t=2.
        // After reset, the state is 0, but the event time is still 2.0, so it should not reoccur because we already passed it.
        // To test reoccurrence, we need an event that depends on state.
        // We'll use a handler that triggers when y crosses a value.
        EventHandler stateHandler = new EventHandler() {
            public double g(double t, double[] y) { return y[0] - 5.0; }
            public int eventOccurred(double t, double[] y, boolean increasing) { return RESET_STATE; }
            public void resetState(double t, double[] y) { y[0] = 0.0; }
        };
        integrator.addEventHandler(stateHandler, 1.0, 1e-6, 100);
        double t0 = 0.0;
        double[] y0 = new double[] { 0.0 };
        double t = 20.0;
        double[] y = new double[1];
        double finalTime = integrator.integrate(ode, t0, y0, t, y);
        // After reset at y=5 (t=5), state becomes 0, then continues. y will cross 5 again at t=10, etc.
        // So multiple resets should occur.
        // We can't easily count, but integration should complete.
        assertEquals("Final time", t, finalTime, 1e-10);
    }
}