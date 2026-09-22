package org.apache.commons.math3.optimization.direct;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optimization.ConvergenceChecker;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: CMAESOptimizer
 *
 * 1. Defect-Targeted Branch Zone:
 *    - Defect: testFitAccuracyDependsOnBoundary (Defects4J ground truth)
 *      Root Cause: Premature termination / step size scaling discrepancy when boundaries are provided
 *      versus unbounded optimization for identical objective landscape, yielding vastly inaccurate results.
 *      Target Test: testFitAccuracyDependsOnBoundary asserts convergence parity within bounds.
 *
 * 2. Parameter Validation Guard Paths (checkParameters):
 *    - Boundaries: all infinite (boundaries=null), mixed finite/infinite (throws MathUnsupportedOperationException),
 *      range overflow (boundaries[1][i] - boundaries[0][i] == Infinity -> throws NumberIsTooLargeException).
 *    - InputSigma: dimension mismatch (throws DimensionMismatchException), negative sigma values
 *      (throws NotPositiveException), sigma > boundary range (throws OutOfRangeException).
 *
 * 3. Algorithmic State Transitions & Optimization Logic:
 *    - GoalType: MINIMIZE vs MAXIMIZE fitness evaluation and decoding inversion.
 *    - Covariance Mechanisms: Active CMA (isActiveCMA=true) vs Standard CMA (isActiveCMA=false).
 *    - Matrix Topology: diagonalOnly=0 (full covariance via EigenDecomposition), diagonalOnly=1 (diagonal only),
 *      and dynamic transition diagonalOnly > 1 when iterations > diagonalOnly.
 *    - Feasibility & Boundary Handling: checkFeasableCount retries, penalty calculation, repairAndDecode.
 *    - Termination Conditions: stopFitness threshold, stopTolFun, stopTolHistFun, stopTolX, stopTolUpX,
 *      max evaluations exhausted (TooManyEvaluationsException handled cleanly).
 *    - Flat Fitness Detection: step-size boost when fitness values stagnate.
 *    - Diagnostic History & Statistics: generation and population of sigma, fitness, mean, and D matrices.
 *    - DoubleIndex Comparator & Contracts: equals, hashCode, compareTo via reflection.
 */
public class CMAESOptimizerGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets known defect: optimization accuracy should not break or prematurely
     * terminate far from optimum when boundaries are specified.
     */
    @Test(timeout = 4000)
    public void testFitAccuracyDependsOnBoundary() {
        final MultivariateFunction fitnessFunction = new MultivariateFunction() {
            public double value(double[] parameters) {
                final double target = 11.1;
                final double error = target - parameters[0];
                return error * error;
            }
        };

        final double[] start = { 0 };
        final double[] lower = { -100 };
        final double[] upper = { 100 };

        final PointValuePair resultWithout = new CMAESOptimizer().optimize(
            20000, fitnessFunction, GoalType.MINIMIZE, start);
        final PointValuePair resultWith = new CMAESOptimizer().optimize(
            20000, fitnessFunction, GoalType.MINIMIZE, start, lower, upper);

        assertEquals(resultWithout.getPoint()[0], resultWith.getPoint()[0], 1e-1);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testOptimizeSphereFunctionMinimization() {
        final MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] x) {
                double sum = 0;
                for (double v : x) {
                    sum += v * v;
                }
                return sum;
            }
        };

        final double[] start = new double[] { 1.5, -2.0, 3.0 };
        final double[] sigma = new double[] { 0.5, 0.5, 0.5 };
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            10, sigma, 1000, 1e-9, true, 0, 0, new MersenneTwister(42L), false);

        final PointValuePair optimum = optimizer.optimize(
            5000, sphere, GoalType.MINIMIZE, start);

        assertNotNull(optimum);
        assertEquals(0.0, optimum.getValue(), 1e-2);
        for (double coord : optimum.getPoint()) {
            assertEquals(0.0, coord, 1e-1);
        }
    }

    @Test(timeout = 4000)
    public void testOptimizeMaximizationGoal() {
        final MultivariateFunction invertedParabola = new MultivariateFunction() {
            public double value(double[] x) {
                return 10.0 - (x[0] - 2.0) * (x[0] - 2.0) - (x[1] + 3.0) * (x[1] + 3.0);
            }
        };

        final double[] start = new double[] { 0.0, 0.0 };
        final double[] sigma = new double[] { 1.0, 1.0 };
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            8, sigma, 1000, -100.0, true, 0, 0, new MersenneTwister(1337L), false);

        final PointValuePair optimum = optimizer.optimize(
            5000, invertedParabola, GoalType.MAXIMIZE, start);

        assertNotNull(optimum);
        assertEquals(10.0, optimum.getValue(), 1e-1);
        assertEquals(2.0, optimum.getPoint()[0], 1e-1);
        assertEquals(-3.0, optimum.getPoint()[1], 1e-1);
    }

    @Test(timeout = 4000)
    public void testOptimizeNonActiveCMA() {
        final MultivariateFunction quad = new MultivariateFunction() {
            public double value(double[] x) {
                return (x[0] - 1.0) * (x[0] - 1.0) + (x[1] - 2.0) * (x[1] - 2.0);
            }
        };

        final double[] start = new double[] { 0.0, 0.0 };
        final double[] sigma = new double[] { 0.5, 0.5 };
        // isActiveCMA = false exercises non-active CMA covariance update
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            8, sigma, 1000, 1e-8, false, 0, 0, new MersenneTwister(999L), false);

        final PointValuePair optimum = optimizer.optimize(
            5000, quad, GoalType.MINIMIZE, start);

        assertNotNull(optimum);
        assertEquals(1.0, optimum.getPoint()[0], 1e-1);
        assertEquals(2.0, optimum.getPoint()[1], 1e-1);
    }

    @Test(timeout = 4000)
    public void testOptimizeDiagonalOnlyMode() {
        final MultivariateFunction quad = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0] + 2.0 * x[1] * x[1] + 3.0 * x[2] * x[2];
            }
        };

        final double[] start = new double[] { 1.0, 1.0, 1.0 };
        final double[] sigma = new double[] { 0.3, 0.3, 0.3 };
        // diagonalOnly = 1 keeps covariance matrix strictly diagonal
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            8, sigma, 500, 1e-7, true, 1, 0, new MersenneTwister(1234L), false);

        final PointValuePair optimum = optimizer.optimize(
            5000, quad, GoalType.MINIMIZE, start);

        assertNotNull(optimum);
        assertEquals(0.0, optimum.getValue(), 1e-2);
    }

    @Test(timeout = 4000)
    public void testOptimizeDiagonalTransitionToFullCovariance() {
        final MultivariateFunction quad = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0] + x[1] * x[1];
            }
        };

        final double[] start = new double[] { 2.0, -2.0 };
        final double[] sigma = new double[] { 0.5, 0.5 };
        // diagonalOnly = 2 exercises: if (diagonalOnly > 1 && iterations > diagonalOnly) -> diagonalOnly = 0
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            8, sigma, 100, 1e-7, true, 2, 0, new MersenneTwister(5678L), false);

        final PointValuePair optimum = optimizer.optimize(
            3000, quad, GoalType.MINIMIZE, start);

        assertNotNull(optimum);
        assertEquals(0.0, optimum.getValue(), 1e-2);
    }

    @Test(timeout = 4000)
    public void testOptimizeWithBoundariesAndFeasibleRetries() {
        final MultivariateFunction quad = new MultivariateFunction() {
            public double value(double[] x) {
                return (x[0] - 0.5) * (x[0] - 0.5);
            }
        };

        final double[] start = new double[] { 0.2 };
        final double[] lower = new double[] { 0.0 };
        final double[] upper = new double[] { 1.0 };
        final double[] sigma = new double[] { 0.1 };

        // checkFeasableCount = 5 exercises retry feasibility branch
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            6, sigma, 500, 1e-8, true, 0, 5, new MersenneTwister(777L), false);

        final PointValuePair optimum = optimizer.optimize(
            3000, quad, GoalType.MINIMIZE, start, lower, upper);

        assertNotNull(optimum);
        assertEquals(0.5, optimum.getPoint()[0], 1e-2);
    }

    @Test(timeout = 4000)
    public void testStatisticsGeneration() {
        final MultivariateFunction quad = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0];
            }
        };

        final double[] start = new double[] { 1.0 };
        final double[] sigma = new double[] { 0.2 };
        // generateStatistics = true
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            6, sigma, 50, 1e-6, true, 0, 0, new MersenneTwister(888L), true);

        optimizer.optimize(500, quad, GoalType.MINIMIZE, start);

        final List<Double> sigmaHist = optimizer.getStatisticsSigmaHistory();
        final List<Double> fitnessHist = optimizer.getStatisticsFitnessHistory();
        final List<RealMatrix> meanHist = optimizer.getStatisticsMeanHistory();
        final List<RealMatrix> dHist = optimizer.getStatisticsDHistory();

        assertNotNull(sigmaHist);
        assertNotNull(fitnessHist);
        assertNotNull(meanHist);
        assertNotNull(dHist);

        assertFalse(sigmaHist.isEmpty());
        assertFalse(fitnessHist.isEmpty());
        assertFalse(meanHist.isEmpty());
        assertFalse(dHist.isEmpty());

        assertEquals(sigmaHist.size(), fitnessHist.size());
        assertEquals(sigmaHist.size(), meanHist.size());
        assertEquals(sigmaHist.size(), dHist.size());
    }

    @Test(timeout = 4000)
    public void testStopFitnessTerminationThreshold() {
        final MultivariateFunction quad = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0];
            }
        };

        final double[] start = new double[] { 5.0 };
        final double[] sigma = new double[] { 0.5 };
        final double stopFitness = 0.5;

        final CMAESOptimizer optimizer = new CMAESOptimizer(
            6, sigma, 10000, stopFitness, true, 0, 0, new MersenneTwister(111L), false);

        final PointValuePair optimum = optimizer.optimize(
            10000, quad, GoalType.MINIMIZE, start);

        assertNotNull(optimum);
        assertTrue(optimum.getValue() <= stopFitness);
    }

    @Test(timeout = 4000)
    public void testFlatFitnessStepSizeAdjustment() {
        // Flat fitness function returns constant value
        final MultivariateFunction flatFunction = new MultivariateFunction() {
            public double value(double[] x) {
                return 42.0;
            }
        };

        final double[] start = new double[] { 1.0, 2.0 };
        final double[] sigma = new double[] { 0.5, 0.5 };

        final CMAESOptimizer optimizer = new CMAESOptimizer(
            8, sigma, 15, 0.0, true, 0, 0, new MersenneTwister(222L), false);

        // Should complete without error, exercising flat fitness branch
        final PointValuePair optimum = optimizer.optimize(
            500, flatFunction, GoalType.MINIMIZE, start);

        assertNotNull(optimum);
        assertEquals(42.0, optimum.getValue(), 1e-6);
    }

    @Test(timeout = 4000)
    public void testCustomConvergenceChecker() {
        final MultivariateFunction quad = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0];
            }
        };

        final double[] start = new double[] { 2.0 };
        final double[] sigma = new double[] { 0.5 };
        final ConvergenceChecker<PointValuePair> checker = new SimpleValueChecker(1e-1, 1e-1);

        final CMAESOptimizer optimizer = new CMAESOptimizer(
            6, sigma, 1000, 0.0, true, 0, 0, new MersenneTwister(333L), false, checker);

        final PointValuePair optimum = optimizer.optimize(
            2000, quad, GoalType.MINIMIZE, start);

        assertNotNull(optimum);
        assertTrue(optimum.getValue() < 1.0);
    }

    @Test(timeout = 4000)
    public void testMaxEvaluationsExhaustionGracefulReturn() {
        final MultivariateFunction slow = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0] + 100.0;
            }
        };

        final double[] start = new double[] { 10.0 };
        final double[] sigma = new double[] { 0.5 };
        final CMAESOptimizer optimizer = new CMAESOptimizer(
            10, sigma, 10000, 0.0, true, 0, 0, new MersenneTwister(444L), false);

        // Evaluates fewer points than required for full convergence
        final PointValuePair optimum = optimizer.optimize(
            15, slow, GoalType.MINIMIZE, start);

        assertNotNull(optimum);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testAllInfiniteBoundsEquivalentToNoBounds() {
        final MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0];
            }
        };

        final double[] start = new double[] { 1.0 };
        final double[] lower = new double[] { Double.NEGATIVE_INFINITY };
        final double[] upper = new double[] { Double.POSITIVE_INFINITY };
        final double[] sigma = new double[] { 0.2 };

        final CMAESOptimizer optimizer = new CMAESOptimizer(
            6, sigma, 500, 1e-6, true, 0, 0, new MersenneTwister(555L), false);

        // When both bounds are infinite, boundaries internal array becomes null
        final PointValuePair optimum = optimizer.optimize(
            2000, sphere, GoalType.MINIMIZE, start, lower, upper);

        assertNotNull(optimum);
        assertEquals(0.0, optimum.getValue(), 1e-2);
    }

    @Test(timeout = 4000)
    public void testDefaultConstructorAndLambdaCalculation() {
        final MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0];
            }
        };

        // Default constructor sets lambda = 0 (triggers automatic lambda formula)
        final CMAESOptimizer optimizer = new CMAESOptimizer();
        final PointValuePair optimum = optimizer.optimize(
            1000, sphere, GoalType.MINIMIZE, new double[] { 2.0 });

        assertNotNull(optimum);
        assertEquals(0.0, optimum.getValue(), 1e-2);
    }

    @Test(timeout = 4000)
    public void testSingleArgAndTwoArgConstructors() {
        final MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0];
            }
        };

        final CMAESOptimizer opt1 = new CMAESOptimizer(6);
        assertNotNull(opt1);
        final PointValuePair res1 = opt1.optimize(1000, sphere, GoalType.MINIMIZE, new double[] { 1.0 });
        assertNotNull(res1);

        final CMAESOptimizer opt2 = new CMAESOptimizer(6, new double[] { 0.2 });
        assertNotNull(opt2);
        final PointValuePair res2 = opt2.optimize(1000, sphere, GoalType.MINIMIZE, new double[] { 1.0 });
        assertNotNull(res2);
    }

    @SuppressWarnings("deprecation")
    @Test(timeout = 4000)
    public void testDeprecatedConstructor() {
        final MultivariateFunction sphere = new MultivariateFunction() {
            public double value(double[] x) {
                return x[0] * x[0];
            }
        };

        final CMAESOptimizer optimizer = new CMAESOptimizer(
            6, new double[] { 0.2 }, 100, 0.0, true, 0, 0, new MersenneTwister(666L), false);

        final PointValuePair optimum = optimizer.optimize(
            1000, sphere, GoalType.MINIMIZE, new double[] { 1.0 });
        assertNotNull(optimum);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testCheckParametersInputSigmaDimensionMismatch() {
        final MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] x) { return 0; }
        };

        final double[] start = new double[] { 1.0, 2.0 };
        final double[] wrongDimSigma = new double[] { 0.5 }; // length 1 != 2

        final CMAESOptimizer optimizer = new CMAESOptimizer(
            6, wrongDimSigma, 100, 0.0, true, 0, 0, new MersenneTwister(1L), false);

        optimizer.optimize(100, func, GoalType.MINIMIZE, start);
    }

    @Test(timeout = 4000, expected = NotPositiveException.class)
    public void testCheckParametersNegativeInputSigma() {
        final MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] x) { return 0; }
        };

        final double[] start = new double[] { 1.0, 2.0 };
        final double[] negativeSigma = new double[] { 0.5, -0.1 };

        final CMAESOptimizer optimizer = new CMAESOptimizer(
            6, negativeSigma, 100, 0.0, true, 0, 0, new MersenneTwister(1L), false);

        optimizer.optimize(100, func, GoalType.MINIMIZE, start);
    }

    @Test(timeout = 4000, expected = OutOfRangeException.class)
    public void testCheckParametersSigmaExceedsBoundaryRange() {
        final MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] x) { return 0; }
        };

        final double[] start = new double[] { 0.5 };
        final double[] lower = new double[] { 0.0 };
        final double[] upper = new double[] { 1.0 };
        final double[] tooLargeSigma = new double[] { 1.5 }; // 1.5 > (1.0 - 0.0)

        final CMAESOptimizer optimizer = new CMAESOptimizer(
            6, tooLargeSigma, 100, 0.0, true, 0, 0, new MersenneTwister(1L), false);

        optimizer.optimize(100, func, GoalType.MINIMIZE, start, lower, upper);
    }

    @Test(timeout = 4000, expected = MathUnsupportedOperationException.class)
    public void testCheckParametersMixedInfiniteAndFiniteLowerBound() {
        final MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] x) { return 0; }
        };

        final double[] start = new double[] { 0.0, 0.0 };
        final double[] lower = new double[] { Double.NEGATIVE_INFINITY, -5.0 }; // Mixed infinite and finite
        final double[] upper = new double[] { 10.0, 10.0 };

        final CMAESOptimizer optimizer = new CMAESOptimizer();
        optimizer.optimize(100, func, GoalType.MINIMIZE, start, lower, upper);
    }

    @Test(timeout = 4000, expected = MathUnsupportedOperationException.class)
    public void testCheckParametersMixedInfiniteAndFiniteUpperBound() {
        final MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] x) { return 0; }
        };

        final double[] start = new double[] { 0.0, 0.0 };
        final double[] lower = new double[] { -10.0, -10.0 };
        final double[] upper = new double[] { Double.POSITIVE_INFINITY, 10.0 }; // Mixed infinite and finite

        final CMAESOptimizer optimizer = new CMAESOptimizer();
        optimizer.optimize(100, func, GoalType.MINIMIZE, start, lower, upper);
    }

    @Test(timeout = 4000, expected = NumberIsTooLargeException.class)
    public void testCheckParametersBoundaryOverflow() {
        final MultivariateFunction func = new MultivariateFunction() {
            public double value(double[] x) { return 0; }
        };

        final double[] start = new double[] { 0.0 };
        // Difference Double.MAX_VALUE - (-Double.MAX_VALUE) overflows to Infinity
        final double[] lower = new double[] { -Double.MAX_VALUE };
        final double[] upper = new double[] { Double.MAX_VALUE };

        final CMAESOptimizer optimizer = new CMAESOptimizer();
        optimizer.optimize(100, func, GoalType.MINIMIZE, start, lower, upper);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity (DoubleIndex Inner Class)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDoubleIndexContractIntegrity() throws Exception {
        final Class<?> doubleIndexClass = Class.forName(
            "org.apache.commons.math3.optimization.direct.CMAESOptimizer$DoubleIndex");
        final Constructor<?> constructor = doubleIndexClass.getDeclaredConstructor(double.class, int.class);
        constructor.setAccessible(true);

        final Object idx1a = constructor.newInstance(1.23, 0);
        final Object idx1b = constructor.newInstance(1.23, 1);
        final Object idx2 = constructor.newInstance(4.56, 2);

        // Reflexivity
        assertEquals(idx1a, idx1a);

        // Symmetry & equality by value
        assertEquals(idx1a, idx1b);
        assertEquals(idx1b, idx1a);
        assertNotEquals(idx1a, idx2);

        // Non-nullity & foreign class equality
        assertNotNull(idx1a);
        assertFalse(idx1a.equals(null));
        assertFalse(idx1a.equals("notADoubleIndex"));

        // Hashcode consistency
        assertEquals(idx1a.hashCode(), idx1b.hashCode());

        // Comparable contract
        final Method compareToMethod = doubleIndexClass.getMethod("compareTo", doubleIndexClass);
        final int compEqual = (Integer) compareToMethod.invoke(idx1a, idx1b);
        final int compLess = (Integer) compareToMethod.invoke(idx1a, idx2);
        final int compGreater = (Integer) compareToMethod.invoke(idx2, idx1a);

        assertEquals(0, compEqual);
        assertTrue(compLess < 0);
        assertTrue(compGreater > 0);
    }
}