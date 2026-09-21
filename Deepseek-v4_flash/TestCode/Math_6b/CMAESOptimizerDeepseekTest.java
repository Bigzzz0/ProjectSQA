package org.apache.commons.math3.optim.nonlinear.scalar.noderiv;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.optim.InitialGuess;
import org.apache.commons.math3.optim.MaxEval;
import org.apache.commons.math3.optim.MaxIter;
import org.apache.commons.math3.optim.OptimizationData;
import org.apache.commons.math3.optim.PointValuePair;
import org.apache.commons.math3.optim.SimpleBounds;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: CMAESOptimizer.doOptimize() and supporting methods
 * 
 * Key branches targeted:
 * 1. diagonalOnly <= 0 vs > 0 in offspring generation (line ~200)
 * 2. hsig flag computation in updateEvolutionPaths (line ~280)
 * 3. isActiveCMA branch in updateCovariance (line ~340)
 * 4. negccov computation and clamping (line ~350-370)
 * 5. updateBD conditional execution (line ~400)
 * 6. Termination criteria: stopFitness, stopTolX, stopTolUpX, stopTolFun, stopTolHistFun
 * 7. Condition number check (max(diagD)/min(diagD) > 1e7)
 * 8. Flat fitness sigma adjustment (line ~260-270)
 * 9. checkFeasableCount regeneration loop (line ~190-200)
 * 10. FitnessFunction penalty computation with isRepairMode
 * 
 * Defect targeting: The known defect causes AssertionFailedError in multiple test methods.
 * The root cause is likely in the covariance update logic (updateCovariance or updateBD)
 * where negative eigenvalues or numerical instability can occur. The test below specifically
 * targets the condition where diagD entries become negative or zero, and the subsequent
 * repair logic in updateBD.
 */
public class CMAESOptimizerDeepseekTest {

    /**
     * Test that triggers the known defect by running optimization on Rosenbrock function
     * with specific parameters that expose numerical instability in covariance update.
     */
    @Test(timeout = 4000)
    public void testRosenbrockWithActiveCMA() {
        // Rosenbrock function: f(x,y) = 100*(y-x^2)^2 + (1-x)^2
        MultivariateFunction rosenbrock = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double x = point[0];
                double y = point[1];
                return 100 * (y - x * x) * (y - x * x) + (1 - x) * (1 - x);
            }
        };

        RandomGenerator random = new JDKRandomGenerator();
        random.setSeed(42);

        CMAESOptimizer optimizer = new CMAESOptimizer(
            10000,                    // maxIterations
            1e-10,                    // stopFitness
            true,                     // isActiveCMA
            0,                        // diagonalOnly
            0,                        // checkFeasableCount
            random,                   // random generator
            false,                    // generateStatistics
            null                      // convergence checker
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(20000),
            new MaxIter(10000),
            GoalType.MINIMIZE,
            new ObjectiveFunction(rosenbrock),
            new InitialGuess(new double[] { -1.0, 1.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
            new CMAESOptimizer.PopulationSize(10),
            new SimpleBounds(new double[] { -10, -10 }, new double[] { 10, 10 })
        );

        double[] point = result.getPoint();
        double value = result.getValue();

        // The expected optimum is at (1,1) with value 0
        // Allow some tolerance due to stochastic nature
        assertNotNull("Result should not be null", result);
        assertTrue("Point should be finite", Double.isFinite(point[0]) && Double.isFinite(point[1]));
        assertTrue("Value should be finite", Double.isFinite(value));
        
        // The optimizer should find a reasonable solution
        assertTrue("Should find near-optimal solution, value=" + value, value < 1e-2);
    }

    /**
     * Test with diagonalOnly > 0 to exercise that code path.
     */
    @Test(timeout = 4000)
    public void testDiagonalOnlyMode() {
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double x : point) {
                    sum += x * x;
                }
                return sum;
            }
        };

        RandomGenerator random = new JDKRandomGenerator();
        random.setSeed(123);

        CMAESOptimizer optimizer = new CMAESOptimizer(
            5000,
            1e-8,
            false,
            10,   // diagonalOnly = 10 (will switch to full after 10 iterations)
            0,
            random,
            false,
            null
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(10000),
            new MaxIter(5000),
            GoalType.MINIMIZE,
            new ObjectiveFunction(sphere),
            new InitialGuess(new double[] { 3.0, 4.0, 5.0 }),
            new CMAESOptimizer.Sigma(new double[] { 1.0, 1.0, 1.0 }),
            new CMAESOptimizer.PopulationSize(15),
            new SimpleBounds(new double[] { -10, -10, -10 }, new double[] { 10, 10, 10 })
        );

        double[] point = result.getPoint();
        double value = result.getValue();

        assertNotNull("Result should not be null", result);
        assertTrue("Value should be small for sphere function, value=" + value, value < 1e-4);
    }

    /**
     * Test with isActiveCMA=false to exercise the non-active covariance update path.
     */
    @Test(timeout = 4000)
    public void testNonActiveCMA() {
        MultivariateFunction cigar = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double first = point[0] * point[0];
                double rest = 0;
                for (int i = 1; i < point.length; i++) {
                    rest += point[i] * point[i];
                }
                return first + 1e6 * rest;
            }
        };

        RandomGenerator random = new JDKRandomGenerator();
        random.setSeed(456);

        CMAESOptimizer optimizer = new CMAESOptimizer(
            10000,
            1e-10,
            false,  // isActiveCMA = false
            0,
            0,
            random,
            false,
            null
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(20000),
            new MaxIter(10000),
            GoalType.MINIMIZE,
            new ObjectiveFunction(cigar),
            new InitialGuess(new double[] { 2.0, -2.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.3, 0.3 }),
            new CMAESOptimizer.PopulationSize(10),
            new SimpleBounds(new double[] { -5, -5 }, new double[] { 5, 5 })
        );

        double value = result.getValue();
        assertTrue("Cigar function should converge, value=" + value, value < 1e-3);
    }

    /**
     * Test with checkFeasableCount > 0 to exercise the regeneration loop.
     */
    @Test(timeout = 4000)
    public void testWithFeasabilityCheck() {
        MultivariateFunction ackley = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum1 = 0;
                double sum2 = 0;
                for (double x : point) {
                    sum1 += x * x;
                    sum2 += FastMath.cos(2 * FastMath.PI * x);
                }
                int n = point.length;
                return -20 * FastMath.exp(-0.2 * FastMath.sqrt(sum1 / n)) 
                     - FastMath.exp(sum2 / n) + 20 + FastMath.E;
            }
        };

        RandomGenerator random = new JDKRandomGenerator();
        random.setSeed(789);

        CMAESOptimizer optimizer = new CMAESOptimizer(
            5000,
            1e-6,
            true,
            0,
            5,  // checkFeasableCount = 5
            random,
            false,
            null
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(10000),
            new MaxIter(5000),
            GoalType.MINIMIZE,
            new ObjectiveFunction(ackley),
            new InitialGuess(new double[] { 1.0, 1.0 }),
            new CMAESOptimizer.Sigma(new double[] { 0.5, 0.5 }),
            new CMAESOptimizer.PopulationSize(10),
            new SimpleBounds(new double[] { -5, -5 }, new double[] { 5, 5 })
        );

        double value = result.getValue();
        // Ackley optimum is 0 at (0,0)
        assertTrue("Ackley should converge near 0, value=" + value, value < 1.0);
    }

    /**
     * Test maximize mode to exercise the isMinimize flag.
     */
    @Test(timeout = 4000)
    public void testMaximizeMode() {
        // Simple quadratic: f(x) = -(x-2)^2, maximum at x=2
        MultivariateFunction quadratic = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double x = point[0];
                return -(x - 2) * (x - 2);
            }
        };

        RandomGenerator random = new JDKRandomGenerator();
        random.setSeed(111);

        CMAESOptimizer optimizer = new CMAESOptimizer(
            5000,
            1e-10,
            true,
            0,
            0,
            random,
            false,
            null
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(10000),
            new MaxIter(5000),
            GoalType.MAXIMIZE,
            new ObjectiveFunction(quadratic),
            new InitialGuess(new double[] { 0.0 }),
            new CMAESOptimizer.Sigma(new double[] { 1.0 }),
            new CMAESOptimizer.PopulationSize(10),
            new SimpleBounds(new double[] { -10 }, new double[] { 10 })
        );

        double[] point = result.getPoint();
        double value = result.getValue();

        // Maximum should be near x=2, value near 0
        assertTrue("Point should be near 2, got " + point[0], FastMath.abs(point[0] - 2) < 0.5);
        assertTrue("Value should be near 0, got " + value, FastMath.abs(value) < 0.1);
    }

    /**
     * Test with generateStatistics=true to exercise statistics collection.
     */
    @Test(timeout = 4000)
    public void testStatisticsGeneration() {
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double x : point) {
                    sum += x * x;
                }
                return sum;
            }
        };

        RandomGenerator random = new JDKRandomGenerator();
        random.setSeed(222);

        CMAESOptimizer optimizer = new CMAESOptimizer(
            1000,
            1e-8,
            true,
            0,
            0,
            random,
            true,  // generateStatistics = true
            null
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(5000),
            new MaxIter(1000),
            GoalType.MINIMIZE,
            new ObjectiveFunction(sphere),
            new InitialGuess(new double[] { 5.0, -3.0 }),
            new CMAESOptimizer.Sigma(new double[] { 1.0, 1.0 }),
            new CMAESOptimizer.PopulationSize(10),
            new SimpleBounds(new double[] { -10, -10 }, new double[] { 10, 10 })
        );

        // Verify statistics are collected
        assertFalse("Sigma history should not be empty", optimizer.getStatisticsSigmaHistory().isEmpty());
        assertFalse("Fitness history should not be empty", optimizer.getStatisticsFitnessHistory().isEmpty());
        assertFalse("Mean history should not be empty", optimizer.getStatisticsMeanHistory().isEmpty());
        assertFalse("D history should not be empty", optimizer.getStatisticsDHistory().isEmpty());
        
        double value = result.getValue();
        assertTrue("Sphere should converge, value=" + value, value < 1e-4);
    }

    /**
     * Test with invalid Sigma values (negative) to exercise exception handling.
     */
    @Test(expected = org.apache.commons.math3.exception.NotPositiveException.class, timeout = 4000)
    public void testInvalidSigma() {
        new CMAESOptimizer.Sigma(new double[] { 1.0, -0.5 });
    }

    /**
     * Test with invalid PopulationSize (zero) to exercise exception handling.
     */
    @Test(expected = org.apache.commons.math3.exception.NotStrictlyPositiveException.class, timeout = 4000)
    public void testInvalidPopulationSize() {
        new CMAESOptimizer.PopulationSize(0);
    }

    /**
     * Test with invalid PopulationSize (negative) to exercise exception handling.
     */
    @Test(expected = org.apache.commons.math3.exception.NotStrictlyPositiveException.class, timeout = 4000)
    public void testNegativePopulationSize() {
        new CMAESOptimizer.PopulationSize(-5);
    }

    /**
     * Test with dimension mismatch between Sigma and initial guess.
     */
    @Test(expected = org.apache.commons.math3.exception.DimensionMismatchException.class, timeout = 4000)
    public void testSigmaDimensionMismatch() {
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double x : point) {
                    sum += x * x;
                }
                return sum;
            }
        };

        RandomGenerator random = new JDKRandomGenerator();
        random.setSeed(333);

        CMAESOptimizer optimizer = new CMAESOptimizer(
            1000,
            1e-8,
            true,
            0,
            0,
            random,
            false,
            null
        );

        optimizer.optimize(
            new MaxEval(1000),
            new MaxIter(1000),
            GoalType.MINIMIZE,
            new ObjectiveFunction(sphere),
            new InitialGuess(new double[] { 1.0, 2.0 }),  // 2D
            new CMAESOptimizer.Sigma(new double[] { 0.5 }),  // 1D - mismatch!
            new CMAESOptimizer.PopulationSize(10),
            new SimpleBounds(new double[] { -10, -10 }, new double[] { 10, 10 })
        );
    }

    /**
     * Test with convergence checker to exercise that code path.
     */
    @Test(timeout = 4000)
    public void testWithConvergenceChecker() {
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double x : point) {
                    sum += x * x;
                }
                return sum;
            }
        };

        RandomGenerator random = new JDKRandomGenerator();
        random.setSeed(444);

        // Simple convergence checker that stops after small relative change
        org.apache.commons.math3.optim.ConvergenceChecker<PointValuePair> checker =
            new org.apache.commons.math3.optim.SimpleValueChecker(1e-8, 1e-8);

        CMAESOptimizer optimizer = new CMAESOptimizer(
            10000,
            0,  // no stopFitness
            true,
            0,
            0,
            random,
            false,
            checker
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(20000),
            new MaxIter(10000),
            GoalType.MINIMIZE,
            new ObjectiveFunction(sphere),
            new InitialGuess(new double[] { 10.0, -10.0 }),
            new CMAESOptimizer.Sigma(new double[] { 2.0, 2.0 }),
            new CMAESOptimizer.PopulationSize(10),
            new SimpleBounds(new double[] { -20, -20 }, new double[] { 20, 20 })
        );

        double value = result.getValue();
        assertTrue("Should converge with checker, value=" + value, value < 1e-4);
    }

    /**
     * Test with tight bounds to exercise boundary handling in FitnessFunction.
     */
    @Test(timeout = 4000)
    public void testTightBounds() {
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double x : point) {
                    sum += x * x;
                }
                return sum;
            }
        };

        RandomGenerator random = new JDKRandomGenerator();
        random.setSeed(555);

        CMAESOptimizer optimizer = new CMAESOptimizer(
            5000,
            1e-8,
            true,
            0,
            0,
            random,
            false,
            null
        );

        // Very tight bounds around the optimum
        PointValuePair result = optimizer.optimize(
            new MaxEval(10000),
            new MaxIter(5000),
            GoalType.MINIMIZE,
            new ObjectiveFunction(sphere),
            new InitialGuess(new double[] { 0.5, -0.5 }),
            new CMAESOptimizer.Sigma(new double[] { 0.1, 0.1 }),
            new CMAESOptimizer.PopulationSize(10),
            new SimpleBounds(new double[] { -1, -1 }, new double[] { 1, 1 })
        );

        double value = result.getValue();
        assertTrue("Should converge within tight bounds, value=" + value, value < 1e-4);
    }

    /**
     * Test that specifically targets the defect by using parameters that
     * are known to cause numerical issues in the covariance update.
     * This test uses a high-dimensional problem with active CMA to stress
     * the updateCovariance and updateBD methods.
     */
    @Test(timeout = 4000)
    public void testHighDimensionalActiveCMA() {
        // Rastrigin function scaled for 5 dimensions
        final int n = 5;
        MultivariateFunction rastrigin = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 10 * n;
                for (int i = 0; i < n; i++) {
                    sum += point[i] * point[i] - 10 * FastMath.cos(2 * FastMath.PI * point[i]);
                }
                return sum;
            }
        };

        RandomGenerator random = new JDKRandomGenerator();
        random.setSeed(666);

        CMAESOptimizer optimizer = new CMAESOptimizer(
            10000,
            1e-6,
            true,   // active CMA
            0,
            0,
            random,
            false,
            null
        );

        double[] guess = new double[n];
        double[] sigma = new double[n];
        double[] lower = new double[n];
        double[] upper = new double[n];
        for (int i = 0; i < n; i++) {
            guess[i] = 2.0 * (i % 2 == 0 ? 1 : -1);
            sigma[i] = 0.5;
            lower[i] = -5.12;
            upper[i] = 5.12;
        }

        PointValuePair result = optimizer.optimize(
            new MaxEval(50000),
            new MaxIter(10000),
            GoalType.MINIMIZE,
            new ObjectiveFunction(rastrigin),
            new InitialGuess(guess),
            new CMAESOptimizer.Sigma(sigma),
            new CMAESOptimizer.PopulationSize(20),
            new SimpleBounds(lower, upper)
        );

        double value = result.getValue();
        // Rastrigin optimum is 0 at origin
        assertTrue("Rastrigin should converge, value=" + value, value < 10.0);
    }

    /**
     * Test with very small initial sigma to exercise early termination paths.
     */
    @Test(timeout = 4000)
    public void testSmallInitialSigma() {
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double x : point) {
                    sum += x * x;
                }
                return sum;
            }
        };

        RandomGenerator random = new JDKRandomGenerator();
        random.setSeed(777);

        CMAESOptimizer optimizer = new CMAESOptimizer(
            1000,
            1e-8,
            true,
            0,
            0,
            random,
            false,
            null
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(5000),
            new MaxIter(1000),
            GoalType.MINIMIZE,
            new ObjectiveFunction(sphere),
            new InitialGuess(new double[] { 1.0, 1.0 }),
            new CMAESOptimizer.Sigma(new double[] { 1e-6, 1e-6 }),  // very small sigma
            new CMAESOptimizer.PopulationSize(10),
            new SimpleBounds(new double[] { -10, -10 }, new double[] { 10, 10 })
        );

        // Should still produce a finite result
        assertNotNull("Result should not be null", result);
        assertTrue("Value should be finite", Double.isFinite(result.getValue()));
    }

    /**
     * Test with large population size to exercise different mu computation.
     */
    @Test(timeout = 4000)
    public void testLargePopulation() {
        MultivariateFunction sphere = new MultivariateFunction() {
            @Override
            public double value(double[] point) {
                double sum = 0;
                for (double x : point) {
                    sum += x * x;
                }
                return sum;
            }
        };

        RandomGenerator random = new JDKRandomGenerator();
        random.setSeed(888);

        CMAESOptimizer optimizer = new CMAESOptimizer(
            5000,
            1e-8,
            true,
            0,
            0,
            random,
            false,
            null
        );

        PointValuePair result = optimizer.optimize(
            new MaxEval(20000),
            new MaxIter(5000),
            GoalType.MINIMIZE,
            new ObjectiveFunction(sphere),
            new InitialGuess(new double[] { 3.0, -2.0 }),
            new CMAESOptimizer.Sigma(new double[] { 1.0, 1.0 }),
            new CMAESOptimizer.PopulationSize(50),  // large population
            new SimpleBounds(new double[] { -10, -10 }, new double[] { 10, 10 })
        );

        double value = result.getValue();
        assertTrue("Should converge with large population, value=" + value, value < 1e-4);
    }
}