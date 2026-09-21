package org.apache.commons.math.ode.nonstiff;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.ode.ExpandableStatefulODE;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.FirstOrderIntegrator;
import org.apache.commons.math.ode.TestProblem1;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;

/**
 * White-box test suite for EmbeddedRungeKuttaIntegrator.
 * Targets line/branch coverage and the known defect from Defects4J
 * (DormandPrince853IntegratorTest.testTooLargeFirstStep).
 */
public class EmbeddedRungeKuttaIntegratorDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Partition A: Core Functional Logic & State Transitions
     *   - Constructor with scalar tolerances
     *   - Constructor with vector tolerances
     *   - getOrder() (abstract, tested via concrete subclass)
     *   - getSafety() / setSafety()
     *   - getMinReduction() / setMinReduction()
     *   - getMaxGrowth() / setMaxGrowth()
     *   - integrate() normal forward/backward integration
     *   - fsal flag behavior (first same as last)
     *   - Step acceptance and rejection loop
     *   - Error estimation (abstract, but called)
     * 
     * Partition B: Boundary Value Analysis & Extremes
     *   - minStep = 0, maxStep = Double.MAX_VALUE
     *   - Negative step sizes (backward integration)
     *   - Zero absolute/relative tolerance
     *   - Very large first step (defect trigger)
     *   - Very small first step
     *   - Null arrays (not possible due to constructor, but defensive)
     * 
     * Partition C: Defect-Targeted Branch Zone
     *   - The known defect: testTooLargeFirstStep fails with AssertionFailedError.
     *     Likely cause: initial step size calculation (initializeStep) produces
     *     a step that is too large, leading to error >= 1.0 and step rejection,
     *     but the subsequent step size adjustment may be incorrect or the
     *     integration may overshoot the end time. The test expects the integrator
     *     to handle a large initial step gracefully (e.g., by reducing it).
     *     We replicate the scenario from DormandPrince853IntegratorTest.
     * 
     * Partition D: Exception & Defensive Guard Paths
     *   - MathIllegalArgumentException from sanityChecks (e.g., negative maxStep)
     *   - MathIllegalStateException (e.g., events causing convergence failure)
     *   - Null arguments (not directly exposed, but via superclass)
     * 
     * Partition E: Object Lifecycle & Contract Integrity
     *   - clone() on step interpolator (called in integrate)
     *   - copy() on prototype (tested indirectly)
     *   - Serialization not applicable (no Serializable)
     */

    // ------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructorAndGettersScalar() {
        // Use DormandPrince853Integrator as concrete implementation
        double minStep = 1e-3;
        double maxStep = 1.0;
        double scalAbsTol = 1e-6;
        double scalRelTol = 1e-6;
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(minStep, maxStep, scalAbsTol, scalRelTol);
        assertNotNull(integrator);
        assertEquals(0.9, integrator.getSafety(), 1e-15);
        assertEquals(0.2, integrator.getMinReduction(), 1e-15);
        assertEquals(10.0, integrator.getMaxGrowth(), 1e-15);
        // Order of Dormand-Prince 8(5,3) is 8
        assertEquals(8, integrator.getOrder());
    }

    @Test(timeout = 4000)
    public void testConstructorAndGettersVector() {
        double minStep = 1e-3;
        double maxStep = 1.0;
        double[] vecAbsTol = {1e-6, 1e-6};
        double[] vecRelTol = {1e-6, 1e-6};
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(minStep, maxStep, vecAbsTol, vecRelTol);
        assertNotNull(integrator);
        assertEquals(0.9, integrator.getSafety(), 1e-15);
        assertEquals(0.2, integrator.getMinReduction(), 1e-15);
        assertEquals(10.0, integrator.getMaxGrowth(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSetSafety() {
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-3, 1.0, 1e-6, 1e-6);
        integrator.setSafety(0.5);
        assertEquals(0.5, integrator.getSafety(), 1e-15);
        integrator.setSafety(1.5);
        assertEquals(1.5, integrator.getSafety(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSetMinReduction() {
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-3, 1.0, 1e-6, 1e-6);
        integrator.setMinReduction(0.1);
        assertEquals(0.1, integrator.getMinReduction(), 1e-15);
        integrator.setMinReduction(0.9);
        assertEquals(0.9, integrator.getMinReduction(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSetMaxGrowth() {
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-3, 1.0, 1e-6, 1e-6);
        integrator.setMaxGrowth(5.0);
        assertEquals(5.0, integrator.getMaxGrowth(), 1e-15);
        integrator.setMaxGrowth(20.0);
        assertEquals(20.0, integrator.getMaxGrowth(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testIntegrateForwardSimple() {
        // Use TestProblem1 (simple ODE: y' = -y, y(0)=1, solution y=exp(-t))
        TestProblem1 pb = new TestProblem1();
        double rangeStart = 0.0;
        double rangeEnd = 1.0;
        double[] y = new double[1];
        y[0] = pb.getInitialState()[0];
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-10, 1e-10);
        integrator.integrate(pb, rangeStart, y, rangeEnd, new double[1]);
        // Expected y(1) = exp(-1) ≈ 0.36787944117144233
        assertEquals(0.36787944117144233, y[0], 1e-6);
    }

    @Test(timeout = 4000)
    public void testIntegrateBackwardSimple() {
        TestProblem1 pb = new TestProblem1();
        double rangeStart = 1.0;
        double rangeEnd = 0.0;
        double[] y = new double[1];
        y[0] = Math.exp(-1.0); // y(1) = exp(-1)
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-10, 1e-10);
        integrator.integrate(pb, rangeStart, y, rangeEnd, new double[1]);
        // Expected y(0) = 1.0
        assertEquals(1.0, y[0], 1e-6);
    }

    @Test(timeout = 4000)
    public void testFsalBehavior() {
        // DormandPrince853Integrator is not fsal, but we can test with a fsal method
        // Use HighamHall54Integrator (fsal = true)
        double minStep = 1e-8;
        double maxStep = 1.0;
        double scalAbsTol = 1e-10;
        double scalRelTol = 1e-10;
        HighamHall54Integrator integrator =
            new HighamHall54Integrator(minStep, maxStep, scalAbsTol, scalRelTol);
        TestProblem1 pb = new TestProblem1();
        double[] y = new double[1];
        y[0] = pb.getInitialState()[0];
        integrator.integrate(pb, 0.0, y, 1.0, new double[1]);
        assertEquals(0.36787944117144233, y[0], 1e-6);
    }

    // ------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testMinStepZero() {
        // minStep = 0 should be allowed (but may cause issues)
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(0.0, 1.0, 1e-10, 1e-10);
        TestProblem1 pb = new TestProblem1();
        double[] y = new double[1];
        y[0] = pb.getInitialState()[0];
        integrator.integrate(pb, 0.0, y, 1.0, new double[1]);
        assertEquals(0.36787944117144233, y[0], 1e-6);
    }

    @Test(timeout = 4000)
    public void testMaxStepVeryLarge() {
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, Double.MAX_VALUE, 1e-10, 1e-10);
        TestProblem1 pb = new TestProblem1();
        double[] y = new double[1];
        y[0] = pb.getInitialState()[0];
        integrator.integrate(pb, 0.0, y, 1.0, new double[1]);
        assertEquals(0.36787944117144233, y[0], 1e-6);
    }

    @Test(timeout = 4000)
    public void testZeroTolerances() {
        // Zero tolerances may cause division by zero in error estimation
        // but should be handled gracefully (error becomes infinite -> step rejection)
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 0.0, 0.0);
        TestProblem1 pb = new TestProblem1();
        double[] y = new double[1];
        y[0] = pb.getInitialState()[0];
        try {
            integrator.integrate(pb, 0.0, y, 1.0, new double[1]);
            // If it succeeds, result may be inaccurate but no exception
        } catch (Exception e) {
            // Acceptable if exception thrown due to tolerance issues
        }
    }

    @Test(timeout = 4000)
    public void testVerySmallFirstStep() {
        // Set initial step size very small via maxStep? Actually initial step is computed.
        // We can force small step by setting maxStep small.
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1e-6, 1e-10, 1e-10);
        TestProblem1 pb = new TestProblem1();
        double[] y = new double[1];
        y[0] = pb.getInitialState()[0];
        integrator.integrate(pb, 0.0, y, 1.0, new double[1]);
        assertEquals(0.36787944117144233, y[0], 1e-4); // looser tolerance due to many steps
    }

    // ------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (testTooLargeFirstStep)
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testTooLargeFirstStep() {
        // This test replicates the scenario from DormandPrince853IntegratorTest
        // that triggers the known defect. The integrator is given a very large
        // initial step (maxStep = 1.0, but the problem spans 0 to 1e-6).
        // The bug causes an assertion failure, likely because the integrator
        // overshoots or fails to reduce step size appropriately.
        // We expect the integration to complete successfully and reach the end time.
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-10, 1e-10);
        TestProblem1 pb = new TestProblem1();
        double[] y = new double[1];
        y[0] = pb.getInitialState()[0];
        // Integrate over a very short interval (0 to 1e-6)
        double endTime = 1e-6;
        integrator.integrate(pb, 0.0, y, endTime, new double[1]);
        // Expected y(endTime) ≈ exp(-1e-6) ≈ 0.9999990000005
        double expected = Math.exp(-endTime);
        assertEquals(expected, y[0], 1e-10);
    }

    @Test(timeout = 4000)
    public void testTooLargeFirstStepBackward() {
        // Similar but backward integration
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-10, 1e-10);
        TestProblem1 pb = new TestProblem1();
        double[] y = new double[1];
        y[0] = Math.exp(-1e-6); // start at t=1e-6
        double startTime = 1e-6;
        double endTime = 0.0;
        integrator.integrate(pb, startTime, y, endTime, new double[1]);
        assertEquals(1.0, y[0], 1e-10);
    }

    // ------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // ------------------------------------------------------------------

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testNegativeMaxStep() {
        // maxStep must be positive
        new DormandPrince853Integrator(1e-3, -1.0, 1e-6, 1e-6);
    }

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testMinStepGreaterThanMaxStep() {
        // minStep > maxStep should throw
        new DormandPrince853Integrator(1.0, 1e-3, 1e-6, 1e-6);
    }

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testNegativeToleranceScalar() {
        new DormandPrince853Integrator(1e-3, 1.0, -1e-6, 1e-6);
    }

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testNegativeToleranceVector() {
        double[] vecAbsTol = {-1e-6, 1e-6};
        double[] vecRelTol = {1e-6, 1e-6};
        new DormandPrince853Integrator(1e-3, 1.0, vecAbsTol, vecRelTol);
    }

    @Test(timeout = 4000)
    public void testEventHandling() {
        // Test that events are triggered correctly (branch in acceptStep)
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-10, 1e-10);
        TestProblem1 pb = new TestProblem1();
        double[] y = new double[1];
        y[0] = pb.getInitialState()[0];
        // Add an event at t=0.5
        integrator.addEventHandler(new EventHandler() {
            public double g(double t, double[] y) {
                return t - 0.5;
            }
            public int eventOccurred(double t, double[] y, boolean increasing) {
                return STOP;
            }
            public void resetState(double t, double[] y) {}
        }, 1e-6, 1e-6, 100);
        integrator.integrate(pb, 0.0, y, 1.0, new double[1]);
        // Integration should stop at t=0.5
        assertEquals(0.5, integrator.getCurrentTime(), 1e-6);
    }

    // ------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // ------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testStepInterpolatorCopy() {
        // The prototype is copied during integrate; ensure it works
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-10, 1e-10);
        TestProblem1 pb = new TestProblem1();
        double[] y = new double[1];
        y[0] = pb.getInitialState()[0];
        integrator.integrate(pb, 0.0, y, 1.0, new double[1]);
        // No exception means copy succeeded
        assertTrue(true);
    }

    @Test(timeout = 4000)
    public void testMultipleIntegrations() {
        // Ensure state is reset properly between integrations
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-10, 1e-10);
        TestProblem1 pb = new TestProblem1();
        double[] y = new double[1];
        y[0] = pb.getInitialState()[0];
        integrator.integrate(pb, 0.0, y, 1.0, new double[1]);
        assertEquals(0.36787944117144233, y[0], 1e-6);
        // Second integration
        y[0] = pb.getInitialState()[0];
        integrator.integrate(pb, 0.0, y, 2.0, new double[1]);
        assertEquals(Math.exp(-2.0), y[0], 1e-6);
    }

    @Test(timeout = 4000)
    public void testStepHandler() {
        // Test that step handlers are called (branch in acceptStep)
        DormandPrince853Integrator integrator =
            new DormandPrince853Integrator(1e-8, 1.0, 1e-10, 1e-10);
        TestProblem1 pb = new TestProblem1();
        double[] y = new double[1];
        y[0] = pb.getInitialState()[0];
        final int[] stepCount = {0};
        integrator.addStepHandler(new StepHandler() {
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                stepCount[0]++;
            }
            public void reset() {}
        });
        integrator.integrate(pb, 0.0, y, 1.0, new double[1]);
        assertTrue(stepCount[0] > 0);
    }
}