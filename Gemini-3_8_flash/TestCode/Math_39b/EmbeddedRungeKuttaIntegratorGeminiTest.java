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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.MathIllegalArgumentException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.ode.ExpandableStatefulODE;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.apache.commons.math.ode.events.EventHandler;
import org.apache.commons.math.ode.sampling.StepHandler;
import org.apache.commons.math.ode.sampling.StepInterpolator;
import org.apache.commons.math.util.FastMath;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: EmbeddedRungeKuttaIntegrator
 *
 * Decision / Condition Coverage Targets:
 * 1. Constructor Branching:
 *    - Scalar vs Vector tolerances (vecAbsoluteTolerance == null vs != null).
 *    - Default algorithmic control parameters (safety=0.9, minReduction=0.2, maxGrowth=10.0, exp=-1/order).
 * 2. Getters & Setters:
 *    - safety, minReduction, maxGrowth mutating and retrieving state.
 * 3. Integration Loop Branches:
 *    - forward integration (t > equations.getTime()) vs backward integration (t < equations.getTime()).
 *    - firstTime condition: initial step computation via initializeStep, vector vs scalar error scaling.
 *    - fsal (First Same As Last) handling:
 *         * fsal == true (e.g. DormandPrince54, DormandPrince853): step rejected vs accepted, reusing yDotK[0].
 *         * fsal == false (e.g. HighamHall54): recomputing derivatives for stage 0 on each step.
 *    - Error check loop:
 *         * error >= 1.0 (step rejected): step size reduced, filtered step recalculated, retry while loop.
 *         * error < 1.0 (step accepted): interpolator shift, acceptStep invoked.
 *    - Step control post-acceptance:
 *         * isLastStep == true: loop termination.
 *         * isLastStep == false: nextIsLast calculation, filteredNextIsLast clamp (hNew = t - stepStart).
 * 4. Defect-Targeted Branch Zone (Defects4J ground truth: DormandPrince853IntegratorTest::testTooLargeFirstStep):
 *    - Initial step size larger than the integration range (e.g. 2 * (end - start)).
 *    - Verifies that derivative evaluation time t never overshoots the integration boundary [start, end].
 * 5. Defensive & Guard Paths:
 *    - Dimension mismatch in ODE vs tolerances.
 *    - Backward integration with inverted time steps and tolerances.
 *    - Event handling triggering step truncation and state reset.
 */
public class EmbeddedRungeKuttaIntegratorGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testAlgorithmControlParametersGettersAndSetters() {
        EmbeddedRungeKuttaIntegrator integ =
            new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        assertEquals(0.9, integ.getSafety(), 1.0e-12);
        assertEquals(0.2, integ.getMinReduction(), 1.0e-12);
        assertEquals(10.0, integ.getMaxGrowth(), 1.0e-12);
        assertEquals(5, integ.getOrder());

        integ.setSafety(0.85);
        integ.setMinReduction(0.15);
        integ.setMaxGrowth(8.0);

        assertEquals(0.85, integ.getSafety(), 1.0e-12);
        assertEquals(0.15, integ.getMinReduction(), 1.0e-12);
        assertEquals(8.0, integ.getMaxGrowth(), 1.0e-12);
    }

    @Test(timeout = 4000)
    public void testForwardIntegrationScalarTolerance() {
        EmbeddedRungeKuttaIntegrator integ =
            new DormandPrince54Integrator(1.0e-5, 1.0, 1.0e-8, 1.0e-8);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = -2.0 * y[0];
            }
        };

        ExpandableStatefulODE statefulODE = new ExpandableStatefulODE(ode);
        statefulODE.setTime(0.0);
        statefulODE.setCompleteState(new double[] { 3.0 });

        integ.integrate(statefulODE, 1.5);

        assertEquals(1.5, statefulODE.getTime(), 1.0e-12);
        double expected = 3.0 * FastMath.exp(-2.0 * 1.5);
        assertEquals(expected, statefulODE.getCompleteState()[0], 1.0e-5);
    }

    @Test(timeout = 4000)
    public void testForwardIntegrationVectorTolerance() {
        double[] absTol = new double[] { 1.0e-7, 1.0e-7 };
        double[] relTol = new double[] { 1.0e-7, 1.0e-7 };
        EmbeddedRungeKuttaIntegrator integ =
            new DormandPrince853Integrator(1.0e-5, 1.0, absTol, relTol);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 2;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = y[1];
                yDot[1] = -y[0];
            }
        };

        ExpandableStatefulODE statefulODE = new ExpandableStatefulODE(ode);
        statefulODE.setTime(0.0);
        statefulODE.setCompleteState(new double[] { 1.0, 0.0 });

        integ.integrate(statefulODE, FastMath.PI);

        assertEquals(FastMath.PI, statefulODE.getTime(), 1.0e-10);
        assertEquals(-1.0, statefulODE.getCompleteState()[0], 1.0e-5);
        assertEquals(0.0, statefulODE.getCompleteState()[1], 1.0e-5);
    }

    @Test(timeout = 4000)
    public void testNonFsalMethodIntegration() {
        // HighamHall54Integrator is non-fsal (fsal == false)
        EmbeddedRungeKuttaIntegrator integ =
            new HighamHall54Integrator(1.0e-5, 1.0, 1.0e-8, 1.0e-8);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = y[0];
            }
        };

        ExpandableStatefulODE statefulODE = new ExpandableStatefulODE(ode);
        statefulODE.setTime(0.0);
        statefulODE.setCompleteState(new double[] { 1.0 });

        integ.integrate(statefulODE, 1.0);

        assertEquals(1.0, statefulODE.getTime(), 1.0e-12);
        assertEquals(FastMath.E, statefulODE.getCompleteState()[0], 1.0e-5);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testBackwardIntegration() {
        EmbeddedRungeKuttaIntegrator integ =
            new DormandPrince54Integrator(1.0e-5, 1.0, 1.0e-8, 1.0e-8);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = -y[0];
            }
        };

        ExpandableStatefulODE statefulODE = new ExpandableStatefulODE(ode);
        statefulODE.setTime(2.0);
        statefulODE.setCompleteState(new double[] { FastMath.exp(-2.0) });

        integ.integrate(statefulODE, 0.5);

        assertEquals(0.5, statefulODE.getTime(), 1.0e-12);
        assertEquals(FastMath.exp(-0.5), statefulODE.getCompleteState()[0], 1.0e-5);
    }

    @Test(timeout = 4000)
    public void testBackwardIntegrationVectorTolerance() {
        double[] absTol = new double[] { 1.0e-8 };
        double[] relTol = new double[] { 1.0e-8 };
        EmbeddedRungeKuttaIntegrator integ =
            new HighamHall54Integrator(1.0e-5, 1.0, absTol, relTol);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = y[0];
            }
        };

        ExpandableStatefulODE statefulODE = new ExpandableStatefulODE(ode);
        statefulODE.setTime(1.0);
        statefulODE.setCompleteState(new double[] { FastMath.E });

        integ.integrate(statefulODE, 0.0);

        assertEquals(0.0, statefulODE.getTime(), 1.0e-12);
        assertEquals(1.0, statefulODE.getCompleteState()[0], 1.0e-5);
    }

    @Test(timeout = 4000)
    public void testIntegrationStepHandlers() {
        EmbeddedRungeKuttaIntegrator integ =
            new DormandPrince54Integrator(1.0e-4, 0.5, 1.0e-6, 1.0e-6);

        final int[] stepCount = new int[] { 0 };
        integ.addStepHandler(new StepHandler() {
            public void init(double t0, double[] y0, double t) {
                stepCount[0] = 0;
            }
            public void handleStep(StepInterpolator interpolator, boolean isLast) {
                stepCount[0]++;
                assertNotNull(interpolator.getCurrentTime());
            }
        });

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };

        ExpandableStatefulODE statefulODE = new ExpandableStatefulODE(ode);
        statefulODE.setTime(0.0);
        statefulODE.setCompleteState(new double[] { 0.0 });

        integ.integrate(statefulODE, 2.0);
        assertTrue("At least two steps must have occurred", stepCount[0] >= 2);
        assertEquals(2.0, statefulODE.getTime(), 1.0e-12);
        assertEquals(2.0, statefulODE.getCompleteState()[0], 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testEventHandlingTruncation() {
        EmbeddedRungeKuttaIntegrator integ =
            new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        final double targetEventTime = 0.75;
        integ.addEventHandler(new EventHandler() {
            public void init(double t0, double[] y0, double t) {}
            public double g(double t, double[] y) {
                return t - targetEventTime;
            }
            public Action eventOccurred(double t, double[] y, boolean increasing) {
                return Action.STOP;
            }
            public void resetState(double t, double[] y) {}
        }, 0.1, 1.0e-6, 100);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };

        ExpandableStatefulODE statefulODE = new ExpandableStatefulODE(ode);
        statefulODE.setTime(0.0);
        statefulODE.setCompleteState(new double[] { 0.0 });

        integ.integrate(statefulODE, 2.0);

        assertEquals(targetEventTime, statefulODE.getTime(), 1.0e-5);
        assertEquals(targetEventTime, statefulODE.getCompleteState()[0], 1.0e-5);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (DormandPrince853 / Too Large Step)
    // =========================================================================

    /**
     * Targets Defects4J defect: testTooLargeFirstStep
     * When user sets an initial step size that exceeds the integration span (end - start),
     * the integrator must not evaluate derivatives outside the [start, end] interval.
     */
    @Test(timeout = 4000)
    public void testTooLargeFirstStep() {
        AdaptiveStepsizeIntegrator integ =
            new DormandPrince853Integrator(0, Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
        final double start = 0.0;
        final double end   = 0.001;

        FirstOrderDifferentialEquations equations = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }

            public void computeDerivatives(double t, double[] y, double[] yDot) {
                assertTrue("Derivative evaluation t must not be before start",
                           t >= FastMath.nextAfter(start, Double.NEGATIVE_INFINITY));
                assertTrue("Derivative evaluation t must not exceed end: " + t + " > " + end,
                           t <= FastMath.nextAfter(end, Double.POSITIVE_INFINITY));
                yDot[0] = -100.0 * y[0];
            }
        };

        integ.setInitialStepSize(2.0 * (end - start));
        integ.integrate(equations, start, new double[] { 1.0 }, end, new double[1]);
    }

    @Test(timeout = 4000)
    public void testTooLargeFirstStepBackward() {
        AdaptiveStepsizeIntegrator integ =
            new DormandPrince853Integrator(0, Double.POSITIVE_INFINITY, Double.NaN, Double.NaN);
        final double start = 1.0;
        final double end   = 0.999;

        FirstOrderDifferentialEquations equations = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }

            public void computeDerivatives(double t, double[] y, double[] yDot) {
                assertTrue("Derivative evaluation t must not exceed start in backward mode",
                           t <= FastMath.nextAfter(start, Double.POSITIVE_INFINITY));
                assertTrue("Derivative evaluation t must not precede end in backward mode: " + t + " < " + end,
                           t >= FastMath.nextAfter(end, Double.NEGATIVE_INFINITY));
                yDot[0] = y[0];
            }
        };

        integ.setInitialStepSize(2.0 * (start - end));
        integ.integrate(equations, start, new double[] { 1.0 }, end, new double[1]);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testIntegrationSpanTooSmallException() {
        EmbeddedRungeKuttaIntegrator integ =
            new DormandPrince54Integrator(1.0e-4, 1.0, 1.0e-6, 1.0e-6);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 0.0;
            }
        };

        ExpandableStatefulODE statefulODE = new ExpandableStatefulODE(ode);
        statefulODE.setTime(1.0);
        statefulODE.setCompleteState(new double[] { 0.0 });

        // t == time -> span is 0, less than 1.0e-10 * |time|
        integ.integrate(statefulODE, 1.0);
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testVectorToleranceDimensionMismatch() {
        double[] absTol = new double[] { 1.0e-5, 1.0e-5 };
        double[] relTol = new double[] { 1.0e-5, 1.0e-5 };
        EmbeddedRungeKuttaIntegrator integ =
            new DormandPrince54Integrator(1.0e-4, 1.0, absTol, relTol);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1; // 1D ode vs 2D tolerances
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };

        ExpandableStatefulODE statefulODE = new ExpandableStatefulODE(ode);
        statefulODE.setTime(0.0);
        statefulODE.setCompleteState(new double[] { 0.0 });

        integ.integrate(statefulODE, 1.0);
    }

    @Test(timeout = 4000)
    public void testStepRejectionAndReductionPath() {
        // Force frequent step rejection by setting an exceedingly tight tolerance
        // and a large minimum reduction to exercise the error >= 1.0 loop
        EmbeddedRungeKuttaIntegrator integ =
            new HighamHall54Integrator(1.0e-10, 1.0, 1.0e-12, 1.0e-12);
        integ.setInitialStepSize(0.5);
        integ.setSafety(0.5);
        integ.setMinReduction(0.1);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                // Highly nonlinear derivative causing large local truncation error
                yDot[0] = 100.0 * FastMath.sin(50.0 * t);
            }
        };

        ExpandableStatefulODE statefulODE = new ExpandableStatefulODE(ode);
        statefulODE.setTime(0.0);
        statefulODE.setCompleteState(new double[] { 0.0 });

        integ.integrate(statefulODE, 0.1);
        assertEquals(0.1, statefulODE.getTime(), 1.0e-10);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testCustomEmbeddedRungeKuttaIntegratorSubclass() {
        // Verify custom embedded Runge-Kutta integrator with trivial Butcher array
        double[] c = new double[] { 0.5 };
        double[][] a = new double[][] { { 0.5 } };
        double[] b = new double[] { 0.0, 1.0 };
        DummyRungeKuttaInterpolator interpolator = new DummyRungeKuttaInterpolator();

        EmbeddedRungeKuttaIntegrator customInteg = new EmbeddedRungeKuttaIntegrator(
            "CustomERK", false, c, a, b, interpolator, 1.0e-4, 1.0, 1.0e-4, 1.0e-4) {
            @Override
            public int getOrder() {
                return 2;
            }

            @Override
            protected double estimateError(double[][] yDotK, double[] y0, double[] y1, double h) {
                return 0.5; // Always accept step
            }
        };

        assertEquals("CustomERK", customInteg.getName());
        assertEquals(2, customInteg.getOrder());
        assertEquals(0.9, customInteg.getSafety(), 1.0e-12);

        FirstOrderDifferentialEquations ode = new FirstOrderDifferentialEquations() {
            public int getDimension() {
                return 1;
            }
            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 1.0;
            }
        };

        ExpandableStatefulODE state = new ExpandableStatefulODE(ode);
        state.setTime(0.0);
        state.setCompleteState(new double[] { 0.0 });

        customInteg.integrate(state, 1.0);
        assertEquals(1.0, state.getTime(), 1.0e-12);
        assertEquals(1.0, state.getCompleteState()[0], 1.0e-3);
    }

    /**
     * Minimal RungeKuttaStepInterpolator implementation for testing the abstract class.
     */
    private static class DummyRungeKuttaInterpolator extends RungeKuttaStepInterpolator {
        private static final long serialVersionUID = 1L;

        public DummyRungeKuttaInterpolator() {
            super();
        }

        public DummyRungeKuttaInterpolator(DummyRungeKuttaInterpolator interpolator) {
            super(interpolator);
        }

        @Override
        protected StepInterpolator doCopy() {
            return new DummyRungeKuttaInterpolator(this);
        }

        @Override
        protected void computeInterpolatedStateAndDerivatives(double theta, double oneMinusThetaH) {
            final double oneMinusTheta = 1.0 - theta;
            for (int i = 0; i < interpolatedState.length; ++i) {
                interpolatedState[i] = currentState[i] - oneMinusThetaH * yDotK[0][i];
                interpolatedDerivatives[i] = yDotK[0][i];
            }
        }
    }
}