/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math.ode.nonstiff;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * Target Class: EmbeddedRungeKuttaIntegrator
 *
 * Decision / Condition Coverage Targets:
 * 1. Constructor initializations (scalar vs vector tolerances, algorithm control parameter defaults).
 * 2. Parameter bounds & getters/setters: safety, minReduction, maxGrowth.
 * 3. Array aliasing: y == y0 (in-place) vs y != y0.
 * 4. Step direction: forward (t > t0) vs backward (t < t0).
 * 5. Interpolator selection: requiresDenseOutput() || !eventsHandlersManager.isEmpty() (RungeKuttaStepInterpolator)
 *    vs pure non-dense (DummyStepInterpolator).
 * 6. FSAL (First Same As Last) branching:
 *    - fsal == true (e.g. DormandPrince54, DormandPrince853): reuse last stage derivative, skip first stage evaluation.
 *    - fsal == false (e.g. HighamHall54): evaluate first stage at each step start.
 * 7. Step size error control loop:
 *    - error <= 1.0 (step acceptance) vs error > 1.0 (step rejection, step reduction).
 *    - Safety / minReduction / maxGrowth bounds saturation in stepsize factor.
 * 8. Discrete events handling:
 *    - Event triggered during step (evaluateStep == true).
 *    - Boundary condition: Math.abs(dt) <= Math.ulp(stepStart) (artificial 0-size step branch).
 *    - Standard truncation to event time: hNew = dt.
 *    - Event reset triggers: reset(stepStart, y) requiring derivative re-computation.
 *    - Event stop triggers: manager.stop() terminating the integration early.
 * 9. Known Defect Reproduction (Defects4J):
 *    - testMissedEndEvent: DormandPrince853 misses the final end event / integration stop time
 *      due to step management past final integration horizon t.
 * 10. Defensive guard & exception paths:
 *    - Dimension mismatches (equations vs y0, y0 vs y, vector tolerance dimensions).
 *    - Integration interval of zero length (t0 == t).
 *    - Propagation of DerivativeException from RHS evaluation.
 */
public class EmbeddedRungeKuttaIntegratorGeminiTest {

    // --- Helper Differential Equations ---

    private static class Linear1D implements FirstOrderDifferentialEquations {
        public int getDimension() {
            return 1;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = y[0]; // Solution: y(t) = y0 * exp(t - t0)
        }
    }

    private static class HarmonicOscillator implements FirstOrderDifferentialEquations {
        public int getDimension() {
            return 2;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = y[1];
            yDot[1] = -y[0];
        }
    }

    private static class StiffRHS implements FirstOrderDifferentialEquations {
        public int getDimension() {
            return 1;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) {
            yDot[0] = -50.0 * y[0];
        }
    }

    private static class FaultyRHS implements FirstOrderDifferentialEquations {
        public int getDimension() {
            return 1;
        }

        public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
            throw new DerivativeException("Simulated derivative computation failure", new Object[0]);
        }
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultControlParametersAndMutators() {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        assertEquals(0.9, integrator.getSafety(), 1.0e-12);
        assertEquals(0.2, integrator.getMinReduction(), 1.0e-12);
        assertEquals(10.0, integrator.getMaxGrowth(), 1.0e-12);

        integrator.setSafety(0.85);
        integrator.setMinReduction(0.15);
        integrator.setMaxGrowth(8.5);

        assertEquals(0.85, integrator.getSafety(), 1.0e-12);
        assertEquals(0.15, integrator.getMinReduction(), 1.0e-12);
        assertEquals(8.5, integrator.getMaxGrowth(), 1.0e-12);
    }

    @Test(timeout = 4000)
    public void testForwardIntegrationScalarTolerance() throws IntegratorException, DerivativeException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-8, 1.0e-8);

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        double t0 = 0.0;
        double t1 = 2.0;

        double stopTime = integrator.integrate(new Linear1D(), t0, y0, t1, y);

        assertEquals(t1, stopTime, 1.0e-10);
        assertEquals(Math.exp(2.0), y[0], 1.0e-5);
    }

    @Test(timeout = 4000)
    public void testBackwardIntegration() throws IntegratorException, DerivativeException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-8, 1.0e-8);

        double[] y0 = new double[]{Math.exp(2.0)};
        double[] y = new double[1];
        double t0 = 2.0;
        double t1 = 0.0;

        double stopTime = integrator.integrate(new Linear1D(), t0, y0, t1, y);

        assertEquals(t1, stopTime, 1.0e-10);
        assertEquals(1.0, y[0], 1.0e-5);
    }

    @Test(timeout = 4000)
    public void testVectorTolerancesBranch() throws IntegratorException, DerivativeException {
        double[] vecAbsTol = new double[]{1.0e-7, 1.0e-7};
        double[] vecRelTol = new double[]{1.0e-7, 1.0e-7};
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-4, 1.0, vecAbsTol, vecRelTol);

        double[] y0 = new double[]{0.0, 1.0}; // sin(0)=0, cos(0)=1
        double[] y = new double[2];
        double t0 = 0.0;
        double t1 = Math.PI;

        double stopTime = integrator.integrate(new HarmonicOscillator(), t0, y0, t1, y);

        assertEquals(t1, stopTime, 1.0e-10);
        assertEquals(0.0, y[0], 1.0e-4); // sin(PI) ~ 0
        assertEquals(-1.0, y[1], 1.0e-4); // cos(PI) ~ -1
    }

    @Test(timeout = 4000)
    public void testInPlaceIntegrationStateAliasing() throws IntegratorException, DerivativeException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-8, 1.0e-8);

        double[] y = new double[]{1.0};
        double stopTime = integrator.integrate(new Linear1D(), 0.0, y, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-10);
        assertEquals(Math.exp(1.0), y[0], 1.0e-5);
    }

    @Test(timeout = 4000)
    public void testNonFsalBranchExecution() throws IntegratorException, DerivativeException {
        // HighamHall54Integrator has fsal = false
        HighamHall54Integrator nonFsalIntegrator =
                new HighamHall54Integrator(1.0e-4, 1.0, 1.0e-8, 1.0e-8);

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        double stopTime = nonFsalIntegrator.integrate(new Linear1D(), 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-10);
        assertEquals(Math.exp(1.0), y[0], 1.0e-5);
    }

    @Test(timeout = 4000)
    public void testDummyStepInterpolatorBranch() throws IntegratorException, DerivativeException {
        // No handlers, no event managers -> dummy interpolator branch
        DormandPrince54Integrator integrator =
                new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-8, 1.0e-8);

        integrator.clearStepHandlers();
        integrator.clearEventHandlers();

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        integrator.integrate(new Linear1D(), 0.0, y0, 0.5, y);

        assertEquals(Math.exp(0.5), y[0], 1.0e-5);
    }

    @Test(timeout = 4000)
    public void testDenseOutputStepInterpolatorBranch() throws IntegratorException, DerivativeException {
        DormandPrince54Integrator integrator =
                new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-8, 1.0e-8);

        final int[] stepsCount = new int[]{0};
        integrator.addStepHandler(new StepHandler() {
            public boolean requiresDenseOutput() {
                return true;
            }

            public void reset() {
            }

            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                stepsCount[0]++;
                double currentT = interpolator.getCurrentTime();
                double prevT = interpolator.getPreviousTime();
                assertTrue(currentT >= prevT);
            }
        });

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        integrator.integrate(new Linear1D(), 0.0, y0, 1.0, y);

        assertTrue("Step handler should have been called at least once", stepsCount[0] > 0);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Event Handling
    // =========================================================================

    @Test(timeout = 4000)
    public void testStepRejectionAndAdaptation() throws IntegratorException, DerivativeException {
        // Stiff equation with tight tolerance forces step rejection and reduction
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-8, 1.0, 1.0e-12, 1.0e-12);

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        integrator.integrate(new StiffRHS(), 0.0, y0, 0.5, y);

        assertEquals(Math.exp(-25.0), y[0], 1.0e-5);
    }

    @Test(timeout = 4000)
    public void testDiscreteEventStopsIntegration() throws IntegratorException, DerivativeException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        final double stopTarget = 0.5;
        integrator.addEventHandler(new EventHandler() {
            public double g(double t, double[] y) {
                return t - stopTarget;
            }

            public int eventOccurred(double t, double[] y, boolean increasing) {
                return EventHandler.STOP;
            }

            public void resetState(double t, double[] y) {
            }
        }, 1.0, 1.0e-8, 100);

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        double stopTime = integrator.integrate(new Linear1D(), 0.0, y0, 2.0, y);

        assertEquals(stopTarget, stopTime, 1.0e-6);
        assertEquals(Math.exp(stopTarget), y[0], 1.0e-4);
    }

    @Test(timeout = 4000)
    public void testDiscreteEventStateReset() throws IntegratorException, DerivativeException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        final double switchTime = 0.5;
        integrator.addEventHandler(new EventHandler() {
            private boolean triggered = false;

            public double g(double t, double[] y) {
                return t - switchTime;
            }

            public int eventOccurred(double t, double[] y, boolean increasing) {
                return EventHandler.RESET_STATE;
            }

            public void resetState(double t, double[] y) {
                if (!triggered) {
                    y[0] = 0.0;
                    triggered = true;
                }
            }
        }, 1.0, 1.0e-8, 100);

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        double stopTime = integrator.integrate(new Linear1D(), 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-6);
        // After switchTime, y is set to 0. Derivative of 0 is 0. Final y remains 0.
        assertEquals(0.0, y[0], 1.0e-4);
    }

    @Test(timeout = 4000)
    public void testDiscreteEventDerivativesReset() throws IntegratorException, DerivativeException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        final double switchTime = 0.5;
        integrator.addEventHandler(new EventHandler() {
            public double g(double t, double[] y) {
                return t - switchTime;
            }

            public int eventOccurred(double t, double[] y, boolean increasing) {
                return EventHandler.RESET_DERIVATIVES;
            }

            public void resetState(double t, double[] y) {
            }
        }, 1.0, 1.0e-8, 100);

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        double stopTime = integrator.integrate(new Linear1D(), 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-6);
        assertEquals(Math.exp(1.0), y[0], 1.0e-4);
    }

    @Test(timeout = 4000)
    public void testEventAtIntegrationStartTriggersZeroStepBranch() throws IntegratorException, DerivativeException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        // Event occurs exactly at t = 0.0 (t0), forcing Math.abs(dt) <= Math.ulp(stepStart)
        integrator.addEventHandler(new EventHandler() {
            public double g(double t, double[] y) {
                return t;
            }

            public int eventOccurred(double t, double[] y, boolean increasing) {
                return EventHandler.CONTINUE;
            }

            public void resetState(double t, double[] y) {
            }
        }, 1.0, 1.0e-8, 100);

        double[] y0 = new double[]{1.0};
        double[] y = new double[1];
        double stopTime = integrator.integrate(new Linear1D(), 0.0, y0, 1.0, y);

        assertEquals(1.0, stopTime, 1.0e-6);
        assertEquals(Math.exp(1.0), y[0], 1.0e-4);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Reproduction)
    // =========================================================================

    /**
     * Exact reproducer for the defect documented in:
     * - DormandPrince853IntegratorTest::testMissedEndEvent
     *
     * In the defective implementation, when integrating over large time stamps where the
     * end event is near a large floating point time horizon, the integrator oversteps and
     * fails to stop at the requested final time t.
     */
    @Test(timeout = 4000)
    public void testMissedEndEventDefectDormandPrince853() throws IntegratorException, DerivativeException {
        final double t0 = 1878250320.0000029;
        final double t = 1878250379.9999986;
        FirstOrderDifferentialEquations equations = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }

            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };

        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince853Integrator(0.0, 100.0, 1.0e-10, 1.0e-10);

        double[] y0 = {0.0};
        double[] y = new double[1];
        double finalT = integrator.integrate(equations, t0, y0, t, y);

        assertEquals(t, finalT, 1.0e-10);
        assertEquals(t - t0, y[0], 1.0e-10);
    }

    @Test(timeout = 4000)
    public void testMissedEndEventDefectDormandPrince54() throws IntegratorException, DerivativeException {
        final double t0 = 1878250320.0000029;
        final double t = 1878250379.9999986;
        FirstOrderDifferentialEquations equations = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }

            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };

        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(0.0, 100.0, 1.0e-10, 1.0e-10);

        double[] y0 = {0.0};
        double[] y = new double[1];
        double finalT = integrator.integrate(equations, t0, y0, t, y);

        assertEquals(t, finalT, 1.0e-10);
        assertEquals(t - t0, y[0], 1.0e-10);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IntegratorException.class, timeout = 4000)
    public void testSanityChecksEquationDimensionMismatch() throws IntegratorException, DerivativeException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        // Equation expects dimension 2, but y0 has dimension 1
        integrator.integrate(new HarmonicOscillator(), 0.0, new double[1], 1.0, new double[1]);
    }

    @Test(expected = IntegratorException.class, timeout = 4000)
    public void testSanityChecksOutputDimensionMismatch() throws IntegratorException, DerivativeException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        // y0 has dimension 2, but y has dimension 1
        integrator.integrate(new HarmonicOscillator(), 0.0, new double[2], 1.0, new double[1]);
    }

    @Test(expected = IntegratorException.class, timeout = 4000)
    public void testSanityChecksZeroIntegrationInterval() throws IntegratorException, DerivativeException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        // t0 == t1
        integrator.integrate(new Linear1D(), 1.0, new double[1], 1.0, new double[1]);
    }

    @Test(expected = IntegratorException.class, timeout = 4000)
    public void testSanityChecksVectorToleranceMismatch() throws IntegratorException, DerivativeException {
        // Tolerances have dimension 1, but equation has dimension 2
        double[] vecAbsTol = new double[]{1.0e-6};
        double[] vecRelTol = new double[]{1.0e-6};
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-4, 1.0, vecAbsTol, vecRelTol);

        integrator.integrate(new HarmonicOscillator(), 0.0, new double[2], 1.0, new double[2]);
    }

    @Test(expected = DerivativeException.class, timeout = 4000)
    public void testDerivativeExceptionPropagation() throws IntegratorException, DerivativeException {
        EmbeddedRungeKuttaIntegrator integrator =
                new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        integrator.integrate(new FaultyRHS(), 0.0, new double[1], 1.0, new double[1]);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Subclass Properties
    // =========================================================================

    @Test(timeout = 4000)
    public void testOrderAndIdentityProperties() {
        EmbeddedRungeKuttaIntegrator dp54 =
                new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);
        assertEquals(5, dp54.getOrder());
        assertEquals("Dormand-Prince 5(4)", dp54.getName());

        EmbeddedRungeKuttaIntegrator dp853 =
                new DormandPrince853Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);
        assertEquals(8, dp853.getOrder());
        assertEquals("Dormand-Prince 8 (5, 3)", dp853.getName());

        EmbeddedRungeKuttaIntegrator hh54 =
                new HighamHall54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);
        assertEquals(5, hh54.getOrder());
        assertEquals("Higham-Hall 5(4)", hh54.getName());
    }
}