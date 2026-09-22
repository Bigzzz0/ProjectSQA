package org.apache.commons.math3.optim.nonlinear.vector.jacobian;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.apache.commons.math3.exception.ConvergenceException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.PointVectorValuePair;
import org.apache.commons.math3.optim.SimplePointChecker;
import org.apache.commons.math3.optim.SimpleVectorValueChecker;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunction;
import org.apache.commons.math3.optim.nonlinear.vector.ModelFunctionJacobian;
import org.apache.commons.math3.optim.nonlinear.vector.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.vector.Target;
import org.apache.commons.math3.optim.nonlinear.vector.Weight;
import org.junit.Assert;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: GaussNewtonOptimizer
 *
 * 1. Defect Analysis (Ground Truth - testGetIterations):
 *    - In `doOptimize()`, the optimizer tracks local variable `iter` for convergence checker,
 *      but fails to call `incrementIterationCount()`. As a result, `optimizer.getIterations()`
 *      remains 0 after optimization rather than reflecting the actual number of iterations executed.
 *    - Target Test: `testIterationsCountUpdated()` asserts that `optimizer.getIterations() > 0`
 *      and matches the iteration progression.
 *
 * 2. Decision Branches and Pathways:
 *    - Constructor selection:
 *      * GaussNewtonOptimizer(ConvergenceChecker) [defaults useLU = true]
 *      * GaussNewtonOptimizer(boolean, ConvergenceChecker) [supports LU or QR decomposition]
 *    - checkParameters() validation:
 *      * Bounds present (lower != null || upper != null) -> throws MathUnsupportedOperationException
 *      * Bounds absent -> proceeds normally
 *    - ConvergenceChecker null check:
 *      * checker == null -> throws NullArgumentException
 *      * checker != null -> proceeds
 *    - Matrix Decomposition branch (useLU):
 *      * useLU == true -> LUDecomposition solver branch
 *      * useLU == false -> QRDecomposition solver branch
 *    - Singular problem handling:
 *      * SingularMatrixException during solve -> throws ConvergenceException
 *    - Convergence loop & Cost computation:
 *      * First iteration: previous == null -> no checker call, continues loop
 *      * Subsequent iterations: previous != null -> evaluates checker.converged(...)
 *      * On convergence -> computes cost and returns PointVectorValuePair
 *    - Evaluation limit handling:
 *      * Exceeding MaxEval -> throws TooManyEvaluationsException
 */
public class GaussNewtonOptimizerGeminiTest {

    // Simple linear model: f_0(x) = x_0 + x_1, f_1(x) = 2 * x_0 - x_1
    private static class LinearProblem {
        public ModelFunction getModelFunction() {
            return new ModelFunction(new MultivariateVectorFunction() {
                @Override
                public double[] value(double[] point) {
                    return new double[] {
                        point[0] + point[1],
                        2.0 * point[0] - point[1]
                    };
                }
            });
        }

        public ModelFunctionJacobian getModelFunctionJacobian() {
            return new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                @Override
                public double[][] value(double[] point) {
                    return new double[][] {
                        { 1.0, 1.0 },
                        { 2.0, -1.0 }
                    };
                }
            });
        }
    }

    // Singular problem model where Jacobian yields a singular normal equation (A^T A)
    private static class SingularProblem {
        public ModelFunction getModelFunction() {
            return new ModelFunction(new MultivariateVectorFunction() {
                @Override
                public double[] value(double[] point) {
                    return new double[] { point[0], point[0] };
                }
            });
        }

        public ModelFunctionJacobian getModelFunctionJacobian() {
            return new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                @Override
                public double[][] value(double[] point) {
                    // Two parameters, but both equations only depend on point[0]
                    // Column 1 is entirely 0, making normal matrix singular
                    return new double[][] {
                        { 1.0, 0.0 },
                        { 1.0, 0.0 }
                    };
                }
            });
        }
    }

    // Non-linear problem: Circle fitting
    private static class CircleProblem {
        private final double[][] points;

        public CircleProblem(double[][] points) {
            this.points = points;
        }

        public ModelFunction getModelFunction() {
            return new ModelFunction(new MultivariateVectorFunction() {
                @Override
                public double[] value(double[] params) {
                    double cx = params[0];
                    double cy = params[1];
                    double r = params[2];
                    double[] values = new double[points.length];
                    for (int i = 0; i < points.length; i++) {
                        double dx = points[i][0] - cx;
                        double dy = points[i][1] - cy;
                        values[i] = Math.sqrt(dx * dx + dy * dy) - r;
                    }
                    return values;
                }
            });
        }

        public ModelFunctionJacobian getModelFunctionJacobian() {
            return new ModelFunctionJacobian(new MultivariateMatrixFunction() {
                @Override
                public double[][] value(double[] params) {
                    double cx = params[0];
                    double cy = params[1];
                    double[][] jacobian = new double[points.length][3];
                    for (int i = 0; i < points.length; i++) {
                        double dx = points[i][0] - cx;
                        double dy = points[i][1] - cy;
                        double dist = Math.sqrt(dx * dx + dy * dy);
                        if (dist == 0) {
                            jacobian[i][0] = 0;
                            jacobian[i][1] = 0;
                        } else {
                            jacobian[i][0] = -dx / dist;
                            jacobian[i][1] = -dy / dist;
                        }
                        jacobian[i][2] = -1.0;
                    }
                    return jacobian;
                }
            });
        }
    }

    /*
     * -------------------------------------------------------------------------
     * Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug: getIterations)
     * -------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testGetIterations() {
        // Target defect: BaseOptimizer iteration counter is not incremented during optimize()
        LinearProblem problem = new LinearProblem();
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
            new SimplePointChecker<PointVectorValuePair>(1e-6, 1e-6)
        );

        optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(new double[] { 3.0, 0.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );

        // This assertion fails on the defective version because getIterations() returns 0
        Assert.assertTrue("Optimizer iterations must be incremented and > 0", optimizer.getIterations() > 0);
    }

    /*
     * -------------------------------------------------------------------------
     * Partition A: Core Functional Logic & State Transitions
     * -------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testSolveLinearProblemLU() {
        LinearProblem problem = new LinearProblem();
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
            true, new SimplePointChecker<PointVectorValuePair>(1e-8, 1e-8)
        );

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(new double[] { 3.0, 0.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );

        double[] point = optimum.getPoint();
        Assert.assertEquals(1.0, point[0], 1e-7);
        Assert.assertEquals(2.0, point[1], 1e-7);
        Assert.assertEquals(0.0, optimizer.getCost(), 1e-7);
    }

    @Test(timeout = 4000)
    public void testSolveLinearProblemQR() {
        LinearProblem problem = new LinearProblem();
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
            false, new SimpleVectorValueChecker(1e-8, 1e-8)
        );

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(new double[] { 3.0, 0.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );

        double[] point = optimum.getPoint();
        Assert.assertEquals(1.0, point[0], 1e-7);
        Assert.assertEquals(2.0, point[1], 1e-7);
        Assert.assertEquals(0.0, optimizer.getCost(), 1e-7);
    }

    @Test(timeout = 4000)
    public void testSolveCircleProblemNonLinear() {
        double[][] points = new double[][] {
            { 1.0, 0.0 },
            { 0.0, 1.0 },
            { -1.0, 0.0 },
            { 0.0, -1.0 }
        };
        CircleProblem circle = new CircleProblem(points);

        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
            new SimplePointChecker<PointVectorValuePair>(1e-10, 1e-10)
        );

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(new double[] { 0.0, 0.0, 0.0, 0.0 }),
            new Weight(new DiagonalMatrix(new double[] { 1.0, 1.0, 1.0, 1.0 })),
            new InitialGuess(new double[] { 0.1, -0.1, 0.8 }),
            circle.getModelFunction(),
            circle.getModelFunctionJacobian()
        );

        double[] centerAndRadius = optimum.getPoint();
        Assert.assertEquals(0.0, centerAndRadius[0], 1e-5);
        Assert.assertEquals(0.0, centerAndRadius[1], 1e-5);
        Assert.assertEquals(1.0, centerAndRadius[2], 1e-5);
        Assert.assertTrue(optimizer.getCost() < 1e-7);
    }

    /*
     * -------------------------------------------------------------------------
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * -------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testAlreadyConvergedAtStart() {
        LinearProblem problem = new LinearProblem();
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
            new SimplePointChecker<PointVectorValuePair>(1e-4, 1e-4)
        );

        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(new double[] { 3.0, 0.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 1.0, 2.0 }), // Already exact solution
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );

        Assert.assertEquals(1.0, optimum.getPoint()[0], 1e-7);
        Assert.assertEquals(2.0, optimum.getPoint()[1], 1e-7);
    }

    @Test(timeout = 4000)
    public void testWeightedResidualsImpact() {
        LinearProblem problem = new LinearProblem();
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
            new SimplePointChecker<PointVectorValuePair>(1e-6, 1e-6)
        );

        // Target cannot be exactly satisfied by both points: inconsistent system
        // f0 = x0 + x1 = 1, f1 = 2*x0 - x1 = 5
        // Weight 100 on first eq, 1 on second eq => solver prioritizes first eq
        PointVectorValuePair optimum = optimizer.optimize(
            new MaxEval(100),
            new MaxIter(100),
            new Target(new double[] { 1.0, 5.0 }),
            new Weight(new double[] { 100.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );

        double[] point = optimum.getPoint();
        Assert.assertNotNull(point);
        Assert.assertTrue(optimizer.getCost() > 0.0);
    }

    /*
     * -------------------------------------------------------------------------
     * Partition D: Exception & Defensive Guard Paths
     * -------------------------------------------------------------------------
     */

    @Test(expected = MathUnsupportedOperationException.class, timeout = 4000)
    public void testThrowsMathUnsupportedOperationExceptionWhenLowerBoundProvided() {
        LinearProblem problem = new LinearProblem();
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
            new SimplePointChecker<PointVectorValuePair>(1e-6, 1e-6)
        );

        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[] { 3.0, 0.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new SimpleBounds(new double[] { 0.0, 0.0 }, new double[] { 5.0, 5.0 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );
    }

    @Test(expected = MathUnsupportedOperationException.class, timeout = 4000)
    public void testThrowsMathUnsupportedOperationExceptionWhenUpperBoundProvided() {
        LinearProblem problem = new LinearProblem();
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
            new SimplePointChecker<PointVectorValuePair>(1e-6, 1e-6)
        );

        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[] { 3.0, 0.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new SimpleBounds(new double[] { Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY },
                             new double[] { 5.0, 5.0 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testThrowsNullArgumentExceptionWhenConvergenceCheckerIsNull() {
        LinearProblem problem = new LinearProblem();
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(null);

        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[] { 3.0, 0.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );
    }

    @Test(expected = ConvergenceException.class, timeout = 4000)
    public void testThrowsConvergenceExceptionOnSingularProblemLU() {
        SingularProblem problem = new SingularProblem();
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
            true, new SimplePointChecker<PointVectorValuePair>(1e-6, 1e-6)
        );

        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[] { 1.0, 1.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );
    }

    @Test(expected = ConvergenceException.class, timeout = 4000)
    public void testThrowsConvergenceExceptionOnSingularProblemQR() {
        SingularProblem problem = new SingularProblem();
        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(
            false, new SimplePointChecker<PointVectorValuePair>(1e-6, 1e-6)
        );

        optimizer.optimize(
            new MaxEval(100),
            new Target(new double[] { 1.0, 1.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );
    }

    @Test(expected = TooManyEvaluationsException.class, timeout = 4000)
    public void testThrowsTooManyEvaluationsExceptionWhenMaxEvalExceeded() {
        LinearProblem problem = new LinearProblem();
        // Custom checker that never signals convergence
        ConvergenceChecker<PointVectorValuePair> neverConverges = new ConvergenceChecker<PointVectorValuePair>() {
            @Override
            public boolean converged(int iteration, PointVectorValuePair previous, PointVectorValuePair current) {
                return false;
            }
        };

        GaussNewtonOptimizer optimizer = new GaussNewtonOptimizer(neverConverges);
        optimizer.optimize(
            new MaxEval(2),
            new Target(new double[] { 3.0, 0.0 }),
            new Weight(new double[] { 1.0, 1.0 }),
            new InitialGuess(new double[] { 0.0, 0.0 }),
            problem.getModelFunction(),
            problem.getModelFunctionJacobian()
        );
    }

    /*
     * -------------------------------------------------------------------------
     * Partition E: Object Lifecycle & Contract Integrity
     * -------------------------------------------------------------------------
     */

    @Test(timeout = 4000)
    public void testConstructorInvariants() {
        ConvergenceChecker<PointVectorValuePair> checker =
            new SimplePointChecker<PointVectorValuePair>(1e-3, 1e-3);

        GaussNewtonOptimizer optDefault = new GaussNewtonOptimizer(checker);
        Assert.assertSame(checker, optDefault.getConvergenceChecker());

        GaussNewtonOptimizer optQR = new GaussNewtonOptimizer(false, checker);
        Assert.assertSame(checker, optQR.getConvergenceChecker());
    }
}