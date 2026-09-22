package org.apache.commons.math3.optim.nonlinear.scalar.noderiv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.optim.ConvergenceChecker;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: CMAESOptimizer (Apache Commons Math 3)
 *
 * Targeted Defects & Branches:
 * 1. Defects4J Math-6 Defect:
 *    - BaseOptimizer.getIterations() is never incremented during CMAESOptimizer.doOptimize()
 *      causing assertions like assertTrue(optimizer.getIterations() > 0) to fail across all
 *      test suites (CMAESOptimizerTest, SimplexOptimizerTest, etc.).
 *    - Target: testDefectMath6IterationsCountUpdated, testMaximizeSphere.
 * 2. Partition A (Core Functionality & Search Modes):
 *    - GoalType.MINIMIZE vs GoalType.MAXIMIZE: objective sign flips, fitness sorting.
 *    - Active CMA (isActiveCMA = true) vs Non-Active CMA (isActiveCMA = false).
 *    - Full Covariance (diagonalOnly <= 0) vs Diagonal Only (diagonalOnly > 0).
 *    - Diagonal-to-Full transition branch: (diagonalOnly > 1 && iterations > diagonalOnly).
 *    - Termination by stopFitness (stopFitness != 0 and threshold reached).
 *    - User-defined ConvergenceChecker termination.
 *    - Statistics collection enabled (generateStatistics = true) vs disabled (false).
 * 3. Partition B (Boundary Values & Penalties):
 *    - SimpleBounds constraints: feasible checking, out-of-bounds repair and penalty calculation.
 *    - checkFeasableCount regeneration loop (i >= checkFeasableCount vs fitfun.isFeasible).
 *    - Flat fitness adjustments (bestValue == fitness[...] and history min-max diff == 0).
 * 4. Partition C (Defensive Guard Paths & Exceptions):
 *    - Sigma: negative values throw NotPositiveException.
 *    - PopulationSize: zero/negative values throw NotStrictlyPositiveException.
 *    - Missing PopulationSize: initializeCMA lambda <= 0 throws NotStrictlyPositiveException.
 *    - Sigma dimension mismatch with InitialGuess throws DimensionMismatchException.
 *    - Sigma entry > (uB - lB) throws OutOfRangeException.
 *    - Low MaxEval throws TooManyEvaluationsException caught in offspring generation loop.
 * 5. Partition E (Data Encapsulation & Immutability):
 *    - Defensive array copy in Sigma constructor and getSigma().
 *    - PopulationSize getter integrity.
 */
public class CMAESOptimizerGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Math-6)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectMath6IterationsCountUpdated() {
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            50, 1e-10, true, 0, 0,
            new Well19937c(7654321L), false, null
        );

        final PointValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 1.0, 1.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
            new CMAESOptimizer.PopulationSize(8)
        );

        assertNotNull(result);
        assertTrue("Defects4J Math-6: optimizer.getIterations() must be strictly greater than 0",
                   optimizer.getIterations() > 0);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & Search Strategies
    // =========================================================================

    @Test(timeout = 4000)
    public void testOptimizeSphereMinimize() {
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            100, 1e-6, true, 0, 0,
            new Well19937c(42L), false, null
        );

        final PointValuePair result = optimizer.optimize(
            new MaxEval(2500),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 2.0, -2.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
            new CMAESOptimizer.PopulationSize(8)
        );

        assertNotNull(result);
        assertEquals(0.0, result.getPoint()[0], 0.1);
        assertEquals(0.0, result.getPoint()[1], 0.1);
        assertEquals(0.0, result.getValue(), 0.05);
    }

    @Test(timeout = 4000)
    public void testMaximizeSphere() {
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            100, 1e-6, true, 0, 0,
            new Well19937c(123456L), false, null
        );

        final PointValuePair result = optimizer.optimize(
            new MaxEval(2500),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    return -((point[0] - 1.0) * (point[0] - 1.0) + (point[1] - 2.0) * (point[1] - 2.0));
                }
            }),
            GoalType.MAXIMIZE,
            new InitialGuess(new double[] { 0.0, 0.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
            new CMAESOptimizer.PopulationSize(8)
        );

        assertNotNull(result);
        assertEquals(1.0, result.getPoint()[0], 0.1);
        assertEquals(2.0, result.getPoint()[1], 0.1);
        assertEquals(0.0, result.getValue(), 0.05);
        assertTrue("getIterations() should be > 0", optimizer.getIterations() > 0);
    }

    @Test(timeout = 4000)
    public void testNonActiveCMAUpdate() {
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            50, 0.0, false, 0, 0,
            new Well19937c(98765L), false, null
        );

        final PointValuePair result = optimizer.optimize(
            new MaxEval(1500),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 1.5, 1.5 }),
            new CMAESOptimizer.Sigma(new double[] { 0.3, 0.3 }),
            new CMAESOptimizer.PopulationSize(6)
        );

        assertNotNull(result);
        assertTrue(result.getValue() < 1.0);
    }

    @Test(timeout = 4000)
    public void testDiagonalOnlyModeWithTransitionToFullCMA() {
        // diagonalOnly = 2 with maxIterations = 8 exercises:
        // 1. Initial iterations where diagonalOnly > 0
        // 2. Iterations > diagonalOnly triggering transition: diagonalOnly = 0, full covariance
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            8, 0.0, true, 2, 0,
            new Well19937c(42L), true, null
        );

        final PointValuePair result = optimizer.optimize(
            new MaxEval(1000),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 2.0, 2.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
            new CMAESOptimizer.PopulationSize(6)
        );

        assertNotNull(result);
        assertFalse(optimizer.getStatisticsSigmaHistory().isEmpty());
        assertFalse(optimizer.getStatisticsMeanHistory().isEmpty());
        assertFalse(optimizer.getStatisticsFitnessHistory().isEmpty());
        assertFalse(optimizer.getStatisticsDHistory().isEmpty());
    }

    @Test(timeout = 4000)
    public void testEarlyTerminationByStopFitness() {
        final double stopThreshold = 2.0;
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            100, stopThreshold, true, 0, 0,
            new Well19937c(12345L), false, null
        );

        final PointValuePair result = optimizer.optimize(
            new MaxEval(2000),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 5.0, 5.0 }), // f(start) = 50.0
            new CMAESOptimizer.Sigma(new double[] { 1.0, 1.0 }),
            new CMAESOptimizer.PopulationSize(8)
        );

        assertNotNull(result);
        assertTrue("Terminated when fitness fell below stopFitness threshold",
                   result.getValue() <= stopThreshold);
    }

    @Test(timeout = 4000)
    public void testEarlyTerminationByConvergenceChecker() {
        final ConvergenceChecker<PointValuePair> customChecker = new ConvergenceChecker<PointValuePair>() {
            public boolean converged(int iteration, PointValuePair previous, PointValuePair current) {
                return iteration >= 3;
            }
        };

        final CMAESOptimizer optimizer = new CMAESOptimizer(
            100, 0.0, true, 0, 0,
            new Well19937c(42L), false, customChecker
        );

        final PointValuePair result = optimizer.optimize(
            new MaxEval(2000),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 3.0, 3.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
            new CMAESOptimizer.PopulationSize(6)
        );

        assertNotNull(result);
    }

    @Test(timeout = 4000)
    public void testStatisticsDisabledRemainsEmpty() {
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            4, 0.0, false, 0, 0,
            new Well19937c(42L), false, null
        );

        optimizer.optimize(
            new MaxEval(200),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0];
                }
            }),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 1.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.2 }),
            new CMAESOptimizer.PopulationSize(4)
        );

        assertTrue(optimizer.getStatisticsSigmaHistory().isEmpty());
        assertTrue(optimizer.getStatisticsFitnessHistory().isEmpty());
        assertTrue(optimizer.getStatisticsMeanHistory().isEmpty());
        assertTrue(optimizer.getStatisticsDHistory().isEmpty());
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA), Penalties & Flat Landscapes
    // =========================================================================

    @Test(timeout = 4000)
    public void testBoundariesFeasibilityAndCheckFeasibleCount() {
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            15, 0.0, true, 0, 4,
            new Well19937c(54321L), false, null
        );

        final SimpleBounds bounds = new SimpleBounds(
            new double[] { 1.0, 1.0 },
            new double[] { 3.0, 3.0 }
        );

        final PointValuePair result = optimizer.optimize(
            new MaxEval(1500),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    // Global minimum is at (0, 0) which is strictly outside bounds
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            GoalType.MINIMIZE,
            bounds,
            new InitialGuess(new double[] { 2.0, 2.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
            new CMAESOptimizer.PopulationSize(8)
        );

        assertNotNull(result);
        final double[] pt = result.getPoint();
        assertTrue("Point[0] must respect lower bound", pt[0] >= 1.0 - 1e-6);
        assertTrue("Point[0] must respect upper bound", pt[0] <= 3.0 + 1e-6);
        assertTrue("Point[1] must respect lower bound", pt[1] >= 1.0 - 1e-6);
        assertTrue("Point[1] must respect upper bound", pt[1] <= 3.0 + 1e-6);
    }

    @Test(timeout = 4000)
    public void testFlatFitnessFunctionStepSizeAdjustment() {
        // Constant function exercises the flat fitness branches
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            5, 0.0, true, 0, 0,
            new Well19937c(42L), false, null
        );

        final PointValuePair result = optimizer.optimize(
            new MaxEval(500),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    return 42.0;
                }
            }),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 1.0, 2.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.3, 0.3 }),
            new CMAESOptimizer.PopulationSize(8)
        );

        assertNotNull(result);
        assertEquals(42.0, result.getValue(), 1e-9);
    }

    @Test(timeout = 4000)
    public void testMaxEvaluationsBreakGenerationLoopGracefully() {
        // MaxEval set low triggers TooManyEvaluationsException inside generation loop,
        // which must be caught and gracefully terminate generation loop returning best so far
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            100, 0.0, true, 0, 0,
            new Well19937c(42L), false, null
        );

        final PointValuePair result = optimizer.optimize(
            new MaxEval(14),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) {
                    return point[0] * point[0] + point[1] * point[1];
                }
            }),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 2.0, 2.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
            new CMAESOptimizer.PopulationSize(10)
        );

        assertNotNull(result);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testSigmaNegativeThrowsException() {
        new CMAESOptimizer.Sigma(new double[] { 0.5, -0.0001 });
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testPopulationSizeZeroThrowsException() {
        new CMAESOptimizer.PopulationSize(0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testPopulationSizeNegativeThrowsException() {
        new CMAESOptimizer.PopulationSize(-5);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testMissingPopulationSizeDefaultsToZeroAndThrows() {
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            10, 0.0, true, 0, 0,
            new Well19937c(42L), false, null
        );

        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) { return 0.0; }
            }),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 1.0, 1.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.1, 0.1 })
            // PopulationSize omitted -> lambda remains 0 -> initializeCMA throws
        );
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testSigmaDimensionMismatchThrowsException() {
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            10, 0.0, true, 0, 0,
            new Well19937c(42L), false, null
        );

        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) { return 0.0; }
            }),
            GoalType.MINIMIZE,
            new InitialGuess(new double[] { 1.0, 2.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.1 }), // Dimension 1 vs 2
            new CMAESOptimizer.PopulationSize(6)
        );
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testSigmaExceedsBoundsRangeThrowsException() {
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            10, 0.0, true, 0, 0,
            new Well19937c(42L), false, null
        );

        final SimpleBounds bounds = new SimpleBounds(
            new double[] { 0.0, 0.0 },
            new double[] { 1.0, 1.0 }
        );

        optimizer.optimize(
            new MaxEval(100),
            new ObjectiveFunction(new MultivariateFunction() {
                public double value(double[] point) { return 0.0; }
            }),
            GoalType.MINIMIZE,
            bounds,
            new InitialGuess(new double[] { 0.5, 0.5 }),
            new CMAESOptimizer.Sigma(new double[] { 1.5, 0.5 }), // 1.5 > (1.0 - 0.0)
            new CMAESOptimizer.PopulationSize(6)
        );
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSigmaDefensiveCopyContract() {
        final double[] original = new double[] { 0.1, 0.2 };
        final CMAESOptimizer.Sigma sigma = new CMAESOptimizer.Sigma(original);

        // Modifying input array after construction must not affect Sigma
        original[0] = 99.0;
        assertEquals(0.1, sigma.getSigma()[0], 1e-9);

        // Modifying returned array must not affect internal state
        final double[] retrieved = sigma.getSigma();
        retrieved[1] = 88.0;
        assertEquals(0.2, sigma.getSigma()[1], 1e-9);
    }

    @Test(timeout = 4000)
    public void testPopulationSizeContract() {
        final CMAESOptimizer.PopulationSize popSize = new CMAESOptimizer.PopulationSize(14);
        assertEquals(14, popSize.getPopulationSize());
    }
}