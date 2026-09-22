package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Precision;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: LevenbergMarquardtOptimizer
 *
 * 1. Defect MATH-842 / MATH-849:
 *    - In doOptimize(), '++iter' is incremented locally without calling 'incrementIterationCount()'.
 *    - As a result, getIterations() returns 0 instead of > 0 after optimization.
 *    - Target method: testGetIterationsTargetingDefect() directly asserts getIterations() > 0.
 *
 * 2. Parameter Validation & Defensive Branches:
 *    - checkParameters(): Presence of lower and/or upper bounds must throw MathUnsupportedOperationException.
 *    - qrDecomposition(): Infinite / NaN norm check throwing ConvergenceException.
 *
 * 3. Constructor Coverage:
 *    - Default constructor
 *    - Constructor with custom ConvergenceChecker
 *    - 3-parameter constructor (costRelativeTolerance, parRelativeTolerance, orthoTolerance)
 *    - 5-parameter constructor with initialStepBoundFactor and threshold
 *    - 6-parameter full constructor
 *
 * 4. Algorithm & Numerical Control Branches:
 *    - QR decomposition: column pivoting, rank deficiency (ak2 <= qrRankingThreshold).
 *    - Orthogonality termination (maxCosine <= orthoTolerance).
 *    - Cost & parameter reduction convergence (actRed, preRed, ratio comparisons: ratio <= 0.25, ratio >= 0.75, ratio >= 1e-4).
 *    - LM parameter search: determineLMParameter (fp <= 0.1 * delta, rank == solvedCols vs rank deficient).
 *    - Custom convergence checker path (checker != null && checker.converged()).
 *    - Weight matrix scaling: Diagonal and non-identity square root weight matrices.
 */
public class LevenbergMarquardtOptimizerGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & Standard Optimization
    // =========================================================================

    @Test(timeout = 4000)
    public void testCircleFittingStandardConvergence() {
        // Fits points (1, 0), (0, 1), (-1, 0), (0, -1) to circle centered at (0, 0) radius 1
        final double[][] points = {
            { 1.0, 0.0 },
            { 0.0, 1.0 },
            { -1.0, 0.0 },
            { 0.0, -1.0 }
        };

        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] params) {
                double cx = params[0];
                double cy = params[1];
                double r = params[2];
                double[] values = new double[points.length];
                for (int i = 0; i < points.length; i++) {
                    double dx = points[i][0] - cx;
                    double dy = points[i][1] - cy;
                    values[i] = FastMath.sqrt(dx * dx + dy * dy) - r;
                }
                return values;
            }
        };

        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] params) {
                double cx = params[0];
                double cy = params[1];
                double[][] jac = new double[points.length][3];
                for (int i = 0; i < points.length; i++) {
                    double dx = points[i][0] - cx;
                    double dy = points[i][1] - cy;
                    double dist = FastMath.sqrt(dx * dx + dy * dy);
                    jac[i][0] = -dx / dist;
                    jac[i][1] = -dy / dist;
                    jac[i][2] = -1.0;
                }
                return jac;
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(200),
            new Target(new double[points.length]),
            new Weight(new double[] { 1, 1, 1, 1 }),
            new InitialGuess(new double[] { 0.1, 0.1, 1.2 }),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian)
        );

        assertNotNull(optimum);
        double[] params = optimum.getPoint();
        assertEquals(0.0, params[0], 1e-4);
        assertEquals(0.0, params[1], 1e-4);
        assertEquals(1.0, params[2], 1e-4);
        assertTrue(optimizer.getCost() < 1e-8);
    }

    @Test(timeout = 4000)
    public void testLinearOverdeterminedFit() {
        // y = a * x + b with points (1, 2), (2, 4), (3, 6), exact solution a = 2, b = 0
        final double[] x = { 1.0, 2.0, 3.0 };
        final double[] y = { 2.0, 4.0, 6.0 };

        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] p) {
                double[] res = new double[x.length];
                for (int i = 0; i < x.length; i++) {
                    res[i] = p[0] * x[i] + p[1];
                }
                return res;
            }
        };

        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] p) {
                double[][] jac = new double[x.length][2];
                for (int i = 0; i < x.length; i++) {
                    jac[i][0] = x[i];
                    jac[i][1] = 1.0;
                }
                return jac;
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(1e-12, 1e-12, 1e-12);
        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new Target(y),
            new Weight(new double[] { 1.0, 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian)
        );

        double[] params = optimum.getPoint();
        assertEquals(2.0, params[0], 1e-8);
        assertEquals(0.0, params[1], 1e-8);
        assertEquals(0.0, optimizer.getCost(), 1e-8);
    }

    // =========================================================================
    // Partition B: Defect-Targeted Verification (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetIterationsTargetingDefect() {
        // In the defective version, doOptimize() forgets to call incrementIterationCount().
        // Thus, getIterations() returns 0, failing this assertion.
        final double[] target = { 3.0, 7.0 };
        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] p) {
                return new double[] { p[0] + 1.0, p[1] * 2.0 + 1.0 };
            }
        };
        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] p) {
                return new double[][] {
                    { 1.0, 0.0 },
                    { 0.0, 2.0 }
                };
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(
            new MaxEval(100),
            new Target(target),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian)
        );

        assertTrue("Defect MATH-842 / MATH-849: getIterations() must be strictly positive after optimization",
                   optimizer.getIterations() > 0);
    }

    // =========================================================================
    // Partition C: Defensive Guard Paths & Constraints
    // =========================================================================

    @Test(expected = MathUnsupportedOperationException.class, timeout = 4000)
    public void testBoundsConstraintThrowsException() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[] { 1.0 }),
            new Weight(new double[] { 1.0 }),
            new InitialGuess(new double[] { 0.0 }),
            new SimpleBounds(new double[] { -1.0 }, new double[] { 1.0 }),
            new ModelFunction(new MultivariateVectorFunction() {
                public double[] value(double[] point) { return point; }
            }),
            new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                public double[][] value(double[] point) { return new double[][] { { 1.0 } }; }
            })
        );
    }

    @Test(timeout = 4000)
    public void testLowerBoundOnlyThrowsException() {
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        try {
            optimizer.optimize(
                new MaxEval(100),
                new Target(new double[] { 1.0 }),
                new Weight(new double[] { 1.0 }),
                new InitialGuess(new double[] { 0.0 }),
                new SimpleBounds(new double[] { -1.0 }, new double[] { Double.POSITIVE_INFINITY }),
                new ModelFunction(new MultivariateVectorFunction() {
                    public double[] value(double[] point) { return point; }
                }),
                new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                    public double[][] value(double[] point) { return new double[][] { { 1.0 } }; }
                })
            );
            fail("Expected MathUnsupportedOperationException due to constraints");
        } catch (MathUnsupportedOperationException expected) {
            assertNotNull(expected.getMessage());
        }
    }

    @Test(expected = ConvergenceException.class, timeout = 4000)
    public void testQrDecompositionWithNaNJacobianThrowsConvergenceException() {
        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] p) {
                return new double[] { 1.0, 2.0 };
            }
        };
        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] p) {
                return new double[][] {
                    { Double.NaN, 0.0 },
                    { 1.0, 1.0 }
                };
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(
            new MaxEval(50),
            new Target(new double[] { 0.0, 0.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian)
        );
    }

    @Test(expected = ConvergenceException.class, timeout = 4000)
    public void testQrDecompositionWithInfiniteJacobianThrowsConvergenceException() {
        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] p) {
                return new double[] { 1.0 };
            }
        };
        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] p) {
                return new double[][] { { Double.POSITIVE_INFINITY } };
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        optimizer.optimize(
            new MaxEval(50),
            new Target(new double[] { 0.0 }),
            new Weight(new double[] { 1.0 }),
            new InitialGuess(new double[] { 0.0 }),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian)
        );
    }

    // =========================================================================
    // Partition D: Rank-Deficient & Boundary Value Analysis (BVA)
    // =========================================================================

    @Test(timeout = 4000)
    public void testRankDeficientJacobian() {
        // Two parameters, but both columns in jacobian are identical or one is zero
        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] p) {
                // Only depends on p[0], p[1] has no impact
                return new double[] { p[0], 2.0 * p[0] };
            }
        };
        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] p) {
                return new double[][] {
                    { 1.0, 0.0 },
                    { 2.0, 0.0 }
                };
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(
            100.0, 1e-10, 1e-10, 1e-10, 1e-6
        );

        PointVectorValuePair pair = optimizer.optimize(
            new MaxEval(100),
            new Target(new double[] { 2.0, 4.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian)
        );

        assertNotNull(pair);
        assertEquals(2.0, pair.getPoint()[0], 1e-6);
        // p[1] should remain close to initial guess since it has 0 gradient
        assertEquals(0.0, pair.getPoint()[1], 1e-6);
    }

    @Test(timeout = 4000)
    public void testAlreadyAtOptimumInitialGuess() {
        // Starts at exact target: residuals and cost are immediately zero
        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] p) {
                return new double[] { p[0], p[1] };
            }
        };
        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] p) {
                return new double[][] {
                    { 1.0, 0.0 },
                    { 0.0, 1.0 }
                };
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        PointVectorValuePair result = optimizer.optimize(
            new MaxEval(50),
            new Target(new double[] { 5.0, 10.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 5.0, 10.0 }),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian)
        );

        assertEquals(0.0, optimizer.getCost(), 1e-12);
        assertEquals(5.0, result.getPoint()[0], 1e-12);
        assertEquals(10.0, result.getPoint()[1], 1e-12);
    }

    @Test(timeout = 4000)
    public void testCustomConvergenceCheckerVectorial() {
        // Use custom SimpleVectorValueChecker
        ConvergenceChecker<PointVectorValuePair> checker = new SimpleVectorValueChecker(1e-3, 1e-3);
        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer(checker);

        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] p) {
                return new double[] { p[0] * p[0], p[1] * p[1] };
            }
        };
        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] p) {
                return new double[][] {
                    { 2.0 * p[0], 0.0 },
                    { 0.0, 2.0 * p[1] }
                };
            }
        };

        PointVectorValuePair pair = optimizer.optimize(
            new MaxEval(100),
            new Target(new double[] { 4.0, 9.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 1.5, 2.5 }),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian)
        );

        assertNotNull(pair);
        assertEquals(2.0, pair.getPoint()[0], 0.1);
        assertEquals(3.0, pair.getPoint()[1], 0.1);
    }

    // =========================================================================
    // Partition E: Constructors & Parameter Variation Coverage
    // =========================================================================

    @Test(timeout = 4000)
    public void testAllConstructorsInstantiation() {
        LevenbergMarquardtOptimizer opt1 = new LevenbergMarquardtOptimizer();
        assertNotNull(opt1);

        LevenbergMarquardtOptimizer opt2 = new LevenbergMarquardtOptimizer(new SimplePointChecker<PointVectorValuePair>(1e-4, 1e-4));
        assertNotNull(opt2);

        LevenbergMarquardtOptimizer opt3 = new LevenbergMarquardtOptimizer(1e-8, 1e-8, 1e-8);
        assertNotNull(opt3);

        LevenbergMarquardtOptimizer opt4 = new LevenbergMarquardtOptimizer(50.0, 1e-8, 1e-8, 1e-8, 1e-12);
        assertNotNull(opt4);

        LevenbergMarquardtOptimizer opt5 = new LevenbergMarquardtOptimizer(
            50.0,
            new SimplePointChecker<PointVectorValuePair>(1e-4, 1e-4),
            1e-8,
            1e-8,
            1e-8,
            1e-12
        );
        assertNotNull(opt5);
    }

    @Test(timeout = 4000)
    public void testNonTrivialWeights() {
        // Points with different weights: weight on second point is much larger
        MultivariateVectorFunction model = new MultivariateVectorFunction() {
            public double[] value(double[] p) {
                return new double[] { p[0], p[0] };
            }
        };
        MultivariateMatrixFunction jacobian = new MultivariateMatrixFunction() {
            public double[][] value(double[] p) {
                return new double[][] {
                    { 1.0 },
                    { 1.0 }
                };
            }
        };

        LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
        PointVectorValuePair opt = optimizer.optimize(
            new MaxEval(100),
            new Target(new double[] { 10.0, 20.0 }),
            new Weight(new double[] { 1.0, 9.0 }), // Weighted towards 20.0
            new InitialGuess(new double[] { 0.0 }),
            new ModelFunction(model),
            new ModelFunctionJacobian(jacobian)
        );

        // Optimal p[0] minimizing 1*(p - 10)^2 + 9*(p - 20)^2 is (10 + 180) / 10 = 19.0
        assertEquals(19.0, opt.getPoint()[0], 1e-6);
    }
}