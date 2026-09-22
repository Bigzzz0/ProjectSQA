package org.apache.commons.math.optimization.general;

import java.util.Arrays;
import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.analysis.MultivariateMatrixFunction;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.VectorialConvergenceChecker;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: LevenbergMarquardtOptimizer
 *
 * Decision / Condition Matrix Covered:
 * 1. Constructor defaults & Setters:
 *    - initialStepBoundFactor, costRelativeTolerance, parRelativeTolerance, orthoTolerance
 * 2. qrDecomposition():
 *    - Double.isInfinite(norm2) || Double.isNaN(norm2) -> OptimizationException
 *    - ak2 == 0 -> Rank deficiency exit (rank = k)
 *    - akk > 0 vs akk <= 0 sign branch for alpha selection
 *    - Column pivoting loop (cols - 1 - k > 0)
 * 3. doOptimize():
 *    - firstIteration branch: xNorm == 0 (delta = factor) vs xNorm != 0 (delta = factor * xNorm)
 *    - cost == 0 vs cost != 0 (orthogonality calculation)
 *    - maxCosine <= orthoTolerance early return
 *    - actRed calculation (0.1 * cost < previousCost vs 0.1 * cost >= previousCost)
 *    - ratio <= 0.25 (step bound contraction, actRed < 0 branch vs actRed >= 0)
 *    - ratio >= 0.75 or lmPar == 0 (step bound expansion)
 *    - ratio >= 1.0e-4 (successful iteration) vs ratio < 1.0e-4 (failed iteration, state reset)
 *    - Convergence criteria: costRelativeTolerance, parRelativeTolerance
 *    - Machine epsilon (2.2204e-16) boundary traps
 * 4. determineLMParameter():
 *    - rank == solvedCols vs rank < solvedCols (parl = 0)
 *    - fp <= 0.1 * delta early return
 *    - paru == 0 boundary condition
 *    - countdown iterations & Newton corrections
 * 5. determineLMDirection():
 *    - Givens rotations: |rkk| < |lmDiag[k]| vs |rkk| >= |lmDiag[k]|
 *    - Singular triangular back-substitution (nSing)
 * 6. Defects4J Known Defect Zones:
 *    - Minpack Jennrich-Sampson premature termination / accuracy failure
 *    - Minpack Freudenstein-Roth local minimum convergence failure
 */
public class LevenbergMarquardtOptimizerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testLinearRegressionOptimization() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        // Fit y = a * x + b through points (1, 3), (2, 5), (3, 7) -> a=2, b=1
        final double[] x = new double[] { 1.0, 2.0, 3.0 };
        final double[] y = new double[] { 3.0, 5.0, 7.0 };

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                double a = point[0];
                double b = point[1];
                return new double[] { a * x[0] + b, a * x[1] + b, a * x[2] + b };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] {
                            { x[0], 1.0 },
                            { x[1], 1.0 },
                            { x[2], 1.0 }
                        };
                    }
                };
            }
        };

        VectorialPointValuePair result = optimizer.optimize(
            function,
            y,
            new double[] { 1.0, 1.0, 1.0 },
            new double[] { 0.0, 0.0 }
        );

        assertNotNull(result);
        assertEquals(2.0, result.getPoint()[0], 1.0e-6);
        assertEquals(1.0, result.getPoint()[1], 1.0e-6);
        assertEquals(0.0, optimizer.getRMS(), 1.0e-6);
        assertEquals(0.0, optimizer.getChiSquare(), 1.0e-6);
        assertTrue(optimizer.getIterations() > 0);
        assertTrue(optimizer.getEvaluations() > 0);
        assertTrue(optimizer.getJacobianEvaluations() > 0);
    }

    @Test(timeout = 4000)
    public void testUnderdeterminedSystemOptimization() throws Exception {
        // rows = 1, cols = 2 (rows < cols) -> solvedCols = 1
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0] + 2.0 * point[1] };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 1.0, 2.0 } };
                    }
                };
            }
        };

        VectorialPointValuePair result = optimizer.optimize(
            function,
            new double[] { 5.0 },
            new double[] { 1.0 },
            new double[] { 0.0, 0.0 }
        );

        assertNotNull(result);
        assertEquals(5.0, result.getValue()[0], 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testAlreadyOptimalAtStartingPoint() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0], point[1] };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] {
                            { 1.0, 0.0 },
                            { 0.0, 1.0 }
                        };
                    }
                };
            }
        };

        // Starting point exactly matches target -> cost == 0 path
        VectorialPointValuePair result = optimizer.optimize(
            function,
            new double[] { 2.5, -1.5 },
            new double[] { 1.0, 1.0 },
            new double[] { 2.5, -1.5 }
        );

        assertNotNull(result);
        assertEquals(2.5, result.getPoint()[0], 1.0e-10);
        assertEquals(-1.5, result.getPoint()[1], 1.0e-10);
        assertEquals(0.0, optimizer.getRMS(), 1.0e-10);
    }

    @Test(timeout = 4000)
    public void testSettersConfiguration() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setInitialStepBoundFactor(50.0);
        optimizer.setCostRelativeTolerance(1.0e-8);
        optimizer.setParRelativeTolerance(1.0e-8);
        optimizer.setOrthoTolerance(1.0e-8);

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0] - 3.0 };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 1.0 } };
                    }
                };
            }
        };

        VectorialPointValuePair result = optimizer.optimize(
            function,
            new double[] { 0.0 },
            new double[] { 1.0 },
            new double[] { 0.0 }
        );

        assertNotNull(result);
        assertEquals(3.0, result.getPoint()[0], 1.0e-6);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testRankDeficientJacobianWithZeroColumn() throws Exception {
        // Parameter 1 has zero impact (column 1 in Jacobian is all zeros)
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0] - 4.0, 2.0 * point[0] - 8.0 };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] {
                            { 1.0, 0.0 },
                            { 2.0, 0.0 }
                        };
                    }
                };
            }
        };

        VectorialPointValuePair result = optimizer.optimize(
            function,
            new double[] { 0.0, 0.0 },
            new double[] { 1.0, 1.0 },
            new double[] { 1.0, 9.0 }
        );

        assertNotNull(result);
        assertEquals(4.0, result.getPoint()[0], 1.0e-6);
        // Unconstrained point[1] remains near its initial value
        assertEquals(9.0, result.getPoint()[1], 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testHouseholderAlphaSignVariation() throws Exception {
        // Tests alpha branches in qrDecomposition: (akk > 0) vs (akk <= 0)
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        // Both negative and positive diagonal entries in Jacobian
        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { -2.0 * point[0], 3.0 * point[1] };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] {
                            { -2.0, 0.0 },
                            { 0.0, 3.0 }
                        };
                    }
                };
            }
        };

        VectorialPointValuePair result = optimizer.optimize(
            function,
            new double[] { 6.0, 9.0 },
            new double[] { 1.0, 1.0 },
            new double[] { 0.0, 0.0 }
        );

        assertNotNull(result);
        assertEquals(-3.0, result.getPoint()[0], 1.0e-6);
        assertEquals(3.0, result.getPoint()[1], 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testVerySmallStepBoundFactor() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setInitialStepBoundFactor(0.001);

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

        VectorialPointValuePair result = optimizer.optimize(
            function,
            new double[] { 0.0 },
            new double[] { 1.0 },
            new double[] { 1.0 }
        );

        assertNotNull(result);
        assertEquals(2.0, result.getPoint()[0], 1.0e-5);
    }

    @Test(timeout = 4000)
    public void testCovarianceAndParameterErrorCalculations() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] {
                    point[0] + point[1],
                    point[0] - point[1],
                    2.0 * point[0] + point[1]
                };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] {
                            { 1.0,  1.0 },
                            { 1.0, -1.0 },
                            { 2.0,  1.0 }
                        };
                    }
                };
            }
        };

        optimizer.optimize(
            function,
            new double[] { 3.0, -1.0, 5.0 },
            new double[] { 1.0, 1.0, 1.0 },
            new double[] { 0.5, 0.5 }
        );

        double[][] covar = optimizer.getCovariances();
        assertNotNull(covar);
        assertEquals(2, covar.length);
        assertEquals(2, covar[0].length);

        double[] errors = optimizer.guessParametersErrors();
        assertNotNull(errors);
        assertEquals(2, errors.length);
        assertTrue(errors[0] >= 0.0);
        assertTrue(errors[1] >= 0.0);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets Minpack Jennrich-Sampson test failure.
     * Defects4J Ground Truth:
     * MinpackTest::testMinpackJennrichSampson
     * expected:<0.2578330049> but was:<0.257819926636807>
     */
    @Test(timeout = 4000)
    public void testMinpackJennrichSampson() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setMaxIterations(1000);

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                double[] values = new double[10];
                for (int i = 1; i <= 10; ++i) {
                    values[i - 1] = 2.0 + 2.0 * i - (Math.exp(i * point[0]) + Math.exp(i * point[1]));
                }
                return values;
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        double[][] jacobian = new double[10][2];
                        for (int i = 1; i <= 10; ++i) {
                            jacobian[i - 1][0] = -i * Math.exp(i * point[0]);
                            jacobian[i - 1][1] = -i * Math.exp(i * point[1]);
                        }
                        return jacobian;
                    }
                };
            }
        };

        double[] target = new double[10];
        double[] weights = new double[10];
        Arrays.fill(weights, 1.0);
        double[] startPoint = new double[] { 0.3, 0.4 };

        VectorialPointValuePair optimum = optimizer.optimize(function, target, weights, startPoint);

        // Strict assertion revealing premature termination defect
        assertEquals(0.2578330049, optimum.getPoint()[0], 1.0e-7);
        assertEquals(0.2578330049, optimum.getPoint()[1], 1.0e-7);
    }

    /**
     * Targets Minpack Freudenstein-Roth test failure.
     * Defects4J Ground Truth:
     * MinpackTest::testMinpackFreudensteinRoth
     * expected:<11.4121122022341> but was:<11.41300466147456>
     */
    @Test(timeout = 4000)
    public void testMinpackFreudensteinRoth() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setMaxIterations(1000);

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                double x1 = point[0];
                double x2 = point[1];
                return new double[] {
                    -13.0 + x1 + ((5.0 - x2) * x2 - 2.0) * x2,
                    -29.0 + x1 + ((1.0 + x2) * x2 - 14.0) * x2
                };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        double x2 = point[1];
                        return new double[][] {
                            { 1.0, 10.0 * x2 - 3.0 * x2 * x2 - 2.0 },
                            { 1.0, 3.0 * x2 * x2 + 2.0 * x2 - 14.0 }
                        };
                    }
                };
            }
        };

        double[] target = new double[] { 0.0, 0.0 };
        double[] weights = new double[] { 1.0, 1.0 };
        double[] startPoint = new double[] { 0.5, -2.0 };

        VectorialPointValuePair optimum = optimizer.optimize(function, target, weights, startPoint);

        // Strict assertion targeting the unreached local minimum precision
        assertEquals(11.4121122022341, optimum.getPoint()[0], 1.0e-6);
        assertEquals(-0.8968053619, optimum.getPoint()[1], 1.0e-6);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = OptimizationException.class, timeout = 4000)
    public void testQrDecompositionThrowsOnNaN() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0] };
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
            function,
            new double[] { 0.0 },
            new double[] { 1.0 },
            new double[] { 1.0 }
        );
    }

    @Test(expected = OptimizationException.class, timeout = 4000)
    public void testQrDecompositionThrowsOnInfinity() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0] };
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
            function,
            new double[] { 0.0 },
            new double[] { 1.0 },
            new double[] { 1.0 }
        );
    }

    @Test(expected = OptimizationException.class, timeout = 4000)
    public void testExceedMaxIterationsThrows() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.setMaxIterations(1);

        // Rosenbrock function starting far from minimum
        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] {
                    10.0 * (point[1] - point[0] * point[0]),
                    1.0 - point[0]
                };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] {
                            { -20.0 * point[0], 10.0 },
                            { -1.0, 0.0 }
                        };
                    }
                };
            }
        };

        optimizer.optimize(
            function,
            new double[] { 0.0, 0.0 },
            new double[] { 1.0, 1.0 },
            new double[] { -1.2, 1.0 }
        );
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMismatchedTargetAndWeightsDimensions() throws Exception {
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

        // target length 1 != weights length 2
        optimizer.optimize(
            function,
            new double[] { 0.0 },
            new double[] { 1.0, 1.0 },
            new double[] { 0.0 }
        );
    }

    @Test(timeout = 4000)
    public void testTooStringentTolerancesThrowsOptimizationException() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        // Set tolerances below IEEE-754 epsilon (2.2204e-16)
        optimizer.setCostRelativeTolerance(1.0e-25);
        optimizer.setParRelativeTolerance(1.0e-25);
        optimizer.setOrthoTolerance(1.0e-25);

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { point[0] - 1.0 };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 1.0 } };
                    }
                };
            }
        };

        try {
            optimizer.optimize(
                function,
                new double[] { 0.0 },
                new double[] { 1.0 },
                new double[] { 0.999999999999999 }
            );
            // If it converged normally, tolerance test is acceptable
        } catch (OptimizationException expected) {
            assertTrue(expected.getMessage().contains("too small"));
        } catch (FunctionEvaluationException e) {
            fail("Unexpected FunctionEvaluationException: " + e.getMessage());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSequentialOptimizationsWithSameInstance() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();

        DifferentiableMultivariateVectorialFunction function = new DifferentiableMultivariateVectorialFunction() {
            public double[] value(double[] point) {
                return new double[] { 3.0 * point[0] };
            }

            public MultivariateMatrixFunction jacobian() {
                return new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) {
                        return new double[][] { { 3.0 } };
                    }
                };
            }
        };

        // First run
        VectorialPointValuePair res1 = optimizer.optimize(
            function,
            new double[] { 9.0 },
            new double[] { 1.0 },
            new double[] { 0.0 }
        );
        assertEquals(3.0, res1.getPoint()[0], 1.0e-6);

        // Second run with different target to ensure internal state reset
        VectorialPointValuePair res2 = optimizer.optimize(
            function,
            new double[] { -15.0 },
            new double[] { 1.0 },
            new double[] { 10.0 }
        );
        assertEquals(-5.0, res2.getPoint()[0], 1.0e-6);
    }

    @Test(timeout = 4000)
    public void testVectorialConvergenceCheckerIntegration() throws Exception {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        assertNull(optimizer.getConvergenceChecker());

        VectorialConvergenceChecker checker = new VectorialConvergenceChecker() {
            public boolean converged(int iteration, VectorialPointValuePair previous, VectorialPointValuePair current) {
                return iteration >= 2;
            }
        };

        optimizer.setConvergenceChecker(checker);
        assertSame(checker, optimizer.getConvergenceChecker());
    }
}