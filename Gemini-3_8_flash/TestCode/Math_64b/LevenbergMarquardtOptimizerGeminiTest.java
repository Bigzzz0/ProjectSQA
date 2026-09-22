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
package org.apache.commons.math.optimization.general;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.SimpleVectorialValueChecker;
import org.apache.commons.math.optimization.VectorialConvergenceChecker;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.apache.commons.math.util.MathUtils;
import org.junit.Test;

import java.io.Serializable;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: LevenbergMarquardtOptimizer
 *
 * Decision / Branch Points Targeted:
 * 1. doOptimize():
 *    - firstIteration scaling: xNorm == 0 vs xNorm != 0 (initialStepBoundFactor computation).
 *    - cost == 0 vs cost != 0; maxCosine computation.
 *    - maxCosine <= orthoTolerance (early exit on orthogonality).
 *    - ratio update & branch steps:
 *      * ratio <= 0.25 (with actRed < 0 vs >= 0; 0.1 * cost >= previousCost vs < previousCost; tmp < 0.1).
 *      * ratio >= 0.75 or lmPar == 0.
 *      * ratio >= 1.0e-4 (successful iteration) vs ratio < 1.0e-4 (failed iteration, revert point/residuals).
 *    - checker == null (default tolerance logic) vs checker != null (custom convergence checker).
 *    - Stringent tolerance triggers:
 *      * TOO_SMALL_COST_RELATIVE_TOLERANCE (actRed, preRed <= 2.2204e-16, ratio <= 2.0).
 *      * TOO_SMALL_PARAMETERS_RELATIVE_TOLERANCE (delta <= 2.2204e-16 * xNorm).
 *      * TOO_SMALL_ORTHOGONALITY_TOLERANCE (maxCosine <= 2.2204e-16).
 * 2. qrDecomposition():
 *    - norm2 infinite or NaN -> OptimizationException.
 *    - ak2 <= qrRankingThreshold -> rank deficiency branch (rank = k).
 *    - akk > 0 vs akk <= 0 (alpha calculation).
 * 3. determineLMParameter():
 *    - fp <= 0.1 * delta -> early exit (lmPar = 0).
 *    - rank == solvedCols (Newton step parl calculation) vs rank < solvedCols (parl = 0).
 *    - paru == 0 branch -> smallest positive real / min(delta, 0.1).
 *    - countdown loop termination on (Math.abs(fp) <= 0.1 * delta) or (parl == 0 && fp <= prevFP && prevFP < 0).
 *    - fp > 0 (parl update) vs fp < 0 (paru update).
 * 4. determineLMDirection():
 *    - dpj != 0 vs == 0.
 *    - Givens rotation branches: |rkk| < |lmDiag[k]| vs |rkk| >= |lmDiag[k]|.
 *    - singular system resolution (nSing < solvedCols).
 *
 * Defects4J Ground Truth Defect:
 * - testMinpackJennrichSampson and testMinpackFreudensteinRoth:
 *   In benchmark Minpack suite, parameters converge to specific values. Premature convergence
 *   or altered step-bounds can lead to incorrect parameter estimates (e.g. 11.412112 vs 11.413004).
 * ---------------------------------------------------------------------------------------------------------
 */
public class LevenbergMarquardtOptimizerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorAndParamSetters() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setInitialStepBoundFactor(50.0);
        optimizer.setCostRelativeTolerance(1e-8);
        optimizer.setParRelativeTolerance(1e-8);
        optimizer.setOrthoTolerance(1e-8);
        optimizer.setQRRankingThreshold(1e-12);
        optimizer.setMaxIterations(500);

        assertEquals(500, optimizer.getMaxIterations());
    }

    @Test(timeout = 4000)
    public void testSimpleLinearFit() throws OptimizationException, FunctionEvaluationException {
        // Linear problem: f(x) = [ 2*x0 - 4, 3*x1 - 9 ]
        // Target: [0, 0], Expected solution: [2.0, 3.0]
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { 2.0 * point[0] - 4.0, 3.0 * point[1] - 9.0 };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] {
                            { 2.0, 0.0 },
                            { 0.0, 3.0 }
                        };
                    }
                };
            }
        };

        VectorialPointValuePair optimum = optimizer.optimize(
            function,
            new double[] { 0.0, 0.0 },
            new double[] { 1.0, 1.0 },
            new double[] { 0.0, 0.0 }
        );

        assertNotNull(optimum);
        double[] point = optimum.getPointRef();
        assertEquals(2.0, point[0], 1e-6);
        assertEquals(3.0, point[1], 1e-6);
        assertEquals(0.0, optimizer.getRMS(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testOptimizationWithVectorialConvergenceChecker()
        throws OptimizationException, FunctionEvaluationException {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        VectorialConvergenceChecker checker = new SimpleVectorialValueChecker(1e-4, 1e-4);
        optimizer.setConvergenceChecker(checker);
        assertEquals(checker, optimizer.getConvergenceChecker());

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0] * point[0] - 4.0 };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 2.0 * point[0] } };
                    }
                };
            }
        };

        VectorialPointValuePair optimum = optimizer.optimize(
            function,
            new double[] { 0.0 },
            new double[] { 1.0 },
            new double[] { 1.0 }
        );

        assertEquals(2.0, optimum.getPointRef()[0], 1e-3);
    }

    @Test(timeout = 4000)
    public void testZeroInitialResidualCostZero() throws OptimizationException, FunctionEvaluationException {
        // Starting point already at optimum: cost == 0, maxCosine remains 0 <= orthoTolerance
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0] - 5.0 };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 1.0 } };
                    }
                };
            }
        };

        VectorialPointValuePair optimum = optimizer.optimize(
            function,
            new double[] { 0.0 },
            new double[] { 1.0 },
            new double[] { 5.0 }
        );

        assertEquals(5.0, optimum.getPointRef()[0], 1e-10);
        assertEquals(0.0, optimizer.getCost(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testZeroStartingPointInitialStepBound()
        throws OptimizationException, FunctionEvaluationException {
        // xNorm == 0 at initial step bound calculation: delta = initialStepBoundFactor
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setInitialStepBoundFactor(1.0);

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0] - 10.0, point[1] - 20.0 };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 1.0, 0.0 }, { 0.0, 1.0 } };
                    }
                };
            }
        };

        VectorialPointValuePair optimum = optimizer.optimize(
            function,
            new double[] { 0.0, 0.0 },
            new double[] { 1.0, 1.0 },
            new double[] { 0.0, 0.0 }
        );

        assertEquals(10.0, optimum.getPointRef()[0], 1e-7);
        assertEquals(20.0, optimum.getPointRef()[1], 1e-7);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testRankDeficientJacobian() throws OptimizationException, FunctionEvaluationException {
        // Under-determined / Rank deficient: 2 variables, but both equations only depend on (x0 + x1)
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setQRRankingThreshold(1e-7);

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                double val = point[0] + point[1] - 2.0;
                return new double[] { val, val };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] {
                            { 1.0, 1.0 },
                            { 1.0, 1.0 }
                        };
                    }
                };
            }
        };

        VectorialPointValuePair optimum = optimizer.optimize(
            function,
            new double[] { 0.0, 0.0 },
            new double[] { 1.0, 1.0 },
            new double[] { 0.5, 0.5 }
        );

        double[] pt = optimum.getPointRef();
        assertEquals(2.0, pt[0] + pt[1], 1e-5);
    }

    @Test(timeout = 4000)
    public void testAllZeroJacobianRankZero() throws OptimizationException, FunctionEvaluationException {
        // All zero columns in Jacobian -> rank = 0 immediately during QR decomposition
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setQRRankingThreshold(1e-10);

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { 1.0, 2.0 };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 0.0 }, { 0.0 } };
                    }
                };
            }
        };

        // When Jacobian is identically 0, orthogonality maxCosine = 0 <= orthoTolerance
        VectorialPointValuePair optimum = optimizer.optimize(
            function,
            new double[] { 0.0, 0.0 },
            new double[] { 1.0, 1.0 },
            new double[] { 3.0 }
        );

        assertEquals(3.0, optimum.getPointRef()[0], 1e-10);
    }

    @Test(timeout = 4000)
    public void testOverDeterminedSystem() throws OptimizationException, FunctionEvaluationException {
        // More equations than variables: 3 equations, 1 variable
        // f1 = x - 1, f2 = x - 2, f3 = x - 3
        // Least squares min for (x-1)^2 + (x-2)^2 + (x-3)^2 is x = 2
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] {
                    point[0] - 1.0,
                    point[0] - 2.0,
                    point[0] - 3.0
                };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 1.0 }, { 1.0 }, { 1.0 } };
                    }
                };
            }
        };

        VectorialPointValuePair optimum = optimizer.optimize(
            function,
            new double[] { 0.0, 0.0, 0.0 },
            new double[] { 1.0, 1.0, 1.0 },
            new double[] { 0.0 }
        );

        assertEquals(2.0, optimum.getPointRef()[0], 1e-6);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Minpack Tests)
    // =========================================================================

    /**
     * Targets Defects4J known failure:
     * MinpackTest::testMinpackFreudensteinRoth
     * Expected x1: 11.41300466147456, but buggy LM optimizer returns 11.4121122022341
     */
    @Test(timeout = 4000)
    public void testMinpackFreudensteinRoth() throws OptimizationException, FunctionEvaluationException {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        DifferentiableMultivariateVectorialFunction freudensteinRoth =
            new DifferentiableMultivariateVectorialFunction() {
                public double[] value(double[] x) {
                    double x1 = x[0];
                    double x2 = x[1];
                    double f1 = -13.0 + x1 + ((5.0 - x2) * x2 - 2.0) * x2;
                    double f2 = -29.0 + x1 + ((x2 + 1.0) * x2 - 14.0) * x2;
                    return new double[] { f1, f2 };
                }

                public MultivariateMatrixFunction jacobian() {
                    return new MultivariateMatrixFunction() {
                        public double[][] value(double[] x) {
                            double x2 = x[1];
                            return new double[][] {
                                { 1.0, (10.0 - 3.0 * x2) * x2 - 2.0 },
                                { 1.0, (3.0 * x2 + 2.0) * x2 - 14.0 }
                            };
                        }
                    };
                }
            };

        double[] startPoint = new double[] { 0.5, -2.0 };
        double[] target = new double[] { 0.0, 0.0 };
        double[] weights = new double[] { 1.0, 1.0 };

        VectorialPointValuePair result = optimizer.optimize(freudensteinRoth, target, weights, startPoint);
        double[] point = result.getPoint();

        // Exact expected value from Minpack standard benchmark
        assertEquals(11.41300466147456, point[0], 1e-5);
        assertEquals(-0.896805253274477, point[1], 1e-5);
    }

    /**
     * Targets Defects4J known failure:
     * MinpackTest::testMinpackJennrichSampson
     * Expected x1: 0.2578199266368004, but was 0.2578330049004441
     */
    @Test(timeout = 4000)
    public void testMinpackJennrichSampson() throws OptimizationException, FunctionEvaluationException {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        final int m = 10;
        DifferentiableMultivariateVectorialFunction jennrichSampson =
            new DifferentiableMultivariateVectorialFunction() {
                public double[] value(double[] x) {
                    double[] f = new double[m];
                    for (int i = 1; i <= m; ++i) {
                        f[i - 1] = 2.0 + 2.0 * i - (Math.exp(i * x[0]) + Math.exp(i * x[1]));
                    }
                    return f;
                }

                public MultivariateMatrixFunction jacobian() {
                    return new MultivariateMatrixFunction() {
                        public double[][] value(double[] x) {
                            double[][] jac = new double[m][2];
                            for (int i = 1; i <= m; ++i) {
                                jac[i - 1][0] = -i * Math.exp(i * x[0]);
                                jac[i - 1][1] = -i * Math.exp(i * x[1]);
                            }
                            return jac;
                        }
                    };
                }
            };

        double[] startPoint = new double[] { 0.3, 0.4 };
        double[] target = new double[m];
        double[] weights = new double[m];
        for (int i = 0; i < m; ++i) {
            target[i] = 0.0;
            weights[i] = 1.0;
        }

        VectorialPointValuePair result = optimizer.optimize(jennrichSampson, target, weights, startPoint);
        double[] point = result.getPoint();

        // Exact expected value from Minpack standard benchmark
        assertEquals(0.2578199266368004, point[0], 1e-5);
        assertEquals(0.2578199266368004, point[1], 1e-5);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = OptimizationException.class, timeout = 4000)
    public void testDecompositionFailsOnNaN() throws OptimizationException, FunctionEvaluationException {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        DifferentiableMultivariateVectorialFunction nanFunction = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { 1.0 };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { Double.NaN } };
                    }
                };
            }
        };

        optimizer.optimize(
            nanFunction,
            new double[] { 0.0 },
            new double[] { 1.0 },
            new double[] { 0.0 }
        );
    }

    @Test(expected = OptimizationException.class, timeout = 4000)
    public void testDecompositionFailsOnInfinite() throws OptimizationException, FunctionEvaluationException {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        DifferentiableMultivariateVectorialFunction infFunction = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { 1.0 };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { Double.POSITIVE_INFINITY } };
                    }
                };
            }
        };

        optimizer.optimize(
            infFunction,
            new double[] { 0.0 },
            new double[] { 1.0 },
            new double[] { 0.0 }
        );
    }

    @Test(expected = OptimizationException.class, timeout = 4000)
    public void testTooSmallCostToleranceException() throws OptimizationException, FunctionEvaluationException {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        // Set tolerances absurdly small to trigger TOO_SMALL_COST_RELATIVE_TOLERANCE
        optimizer.setCostRelativeTolerance(1e-30);
        optimizer.setParRelativeTolerance(1e-30);
        optimizer.setOrthoTolerance(1e-30);

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                // Minimum at x = 1, but with non-zero residual to force iterations
                return new double[] { point[0] - 1.0, 1.0 };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 1.0 }, { 0.0 } };
                    }
                };
            }
        };

        optimizer.optimize(
            function,
            new double[] { 0.0, 0.0 },
            new double[] { 1.0, 1.0 },
            new double[] { 0.0 }
        );
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMismatchedTargetAndWeightDimensions() throws OptimizationException, FunctionEvaluationException {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0] };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 1.0 } };
                    }
                };
            }
        };

        // Target length 2, but Weights length 1 -> triggers IllegalArgumentException in AbstractLeastSquaresOptimizer
        optimizer.optimize(
            function,
            new double[] { 0.0, 0.0 },
            new double[] { 1.0 },
            new double[] { 0.0 }
        );
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Complex Geometries
    // =========================================================================

    @Test(timeout = 4000)
    public void testRosenbrockFunction() throws OptimizationException, FunctionEvaluationException {
        // Rosenbrock banana function: f1 = 10*(x1 - x0^2), f2 = 1 - x0
        // Optimum at (1, 1), target [0, 0]
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        DifferentiableMultivariateVectorialFunction rosenbrock = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                double x0 = point[0];
                double x1 = point[1];
                return new double[] {
                    10.0 * (x1 - x0 * x0),
                    1.0 - x0
                };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        double x0 = point[0];
                        return new double[][] {
                            { -20.0 * x0, 10.0 },
                            { -1.0,        0.0 }
                        };
                    }
                };
            }
        };

        VectorialPointValuePair optimum = optimizer.optimize(
            rosenbrock,
            new double[] { 0.0, 0.0 },
            new double[] { 1.0, 1.0 },
            new double[] { -1.2, 1.0 }
        );

        assertEquals(1.0, optimum.getPointRef()[0], 1e-4);
        assertEquals(1.0, optimum.getPointRef()[1], 1e-4);
        assertEquals(0.0, optimizer.getRMS(), 1e-4);
        assertTrue(optimizer.getIterations() > 0);
        assertTrue(optimizer.getEvaluations() > 0);
        assertTrue(optimizer.getJacobianEvaluations() > 0);
    }

    @Test(timeout = 4000)
    public void testCircleFittingProblem() throws OptimizationException, FunctionEvaluationException {
        // Fit circle centered at (x_c, y_c) = (2, 3) with radius R = 5
        final double[][] points = new double[][] {
            { 2.0 + 5.0, 3.0 },
            { 2.0 - 5.0, 3.0 },
            { 2.0, 3.0 + 5.0 },
            { 2.0, 3.0 - 5.0 },
            { 2.0 + 3.0, 3.0 + 4.0 }
        };

        DifferentiableMultivariateVectorialFunction circle = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] params) {
                double cx = params[0];
                double cy = params[1];
                double r  = params[2];
                double[] residuals = new double[points.length];
                for (int i = 0; i < points.length; ++i) {
                    double dx = points[i][0] - cx;
                    double dy = points[i][1] - cy;
                    residuals[i] = Math.sqrt(dx * dx + dy * dy) - r;
                }
                return residuals;
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] params) {
                        double cx = params[0];
                        double cy = params[1];
                        double[][] jac = new double[points.length][3];
                        for (int i = 0; i < points.length; ++i) {
                            double dx = points[i][0] - cx;
                            double dy = points[i][1] - cy;
                            double dist = Math.sqrt(dx * dx + dy * dy);
                            jac[i][0] = -dx / dist;
                            jac[i][1] = -dy / dist;
                            jac[i][2] = -1.0;
                        }
                        return jac;
                    }
                };
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        double[] target = new double[points.length];
        double[] weights = new double[points.length];
        for (int i = 0; i < points.length; ++i) {
            weights[i] = 1.0;
        }

        VectorialPointValuePair optimum = optimizer.optimize(
            circle,
            target,
            weights,
            new double[] { 0.0, 0.0, 1.0 }
        );

        double[] found = optimum.getPointRef();
        assertEquals(2.0, found[0], 1e-5);
        assertEquals(3.0, found[1], 1e-5);
        assertEquals(5.0, found[2], 1e-5);
    }
}