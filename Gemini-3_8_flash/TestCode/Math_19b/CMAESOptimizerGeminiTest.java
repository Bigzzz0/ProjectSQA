package org.apache.commons.math3.optimization.direct;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.analysis.MultivariateFunction;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathUnsupportedOperationException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.exception.TooManyEvaluationsException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optimization.GoalType;
import org.apache.commons.math3.optimization.PointValuePair;
import org.apache.commons.math3.optimization.SimpleValueChecker;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * -------------------------------------------------------------------------------------------------
 * 1. Target Defect: Boundary range overflow leading to NumberIsTooLargeException.
 *    - In CMAESOptimizer#checkParameters(), when (boundaries[1][i] - boundaries[0][i]) > Double.MAX_VALUE,
 *      a NumberIsTooLargeException is expected rather than an unexpected exception or overflow state.
 * 2. Parameter Validation Branches:
 *    - hasFiniteBounds & hasInfiniteBounds: mixed infinite/finite bounds triggers MathUnsupportedOperationException.
 *    - inputSigma.length != init.length: triggers DimensionMismatchException.
 *    - inputSigma[i] < 0: triggers NotPositiveException.
 *    - inputSigma[i] > boundaries[1][i] - boundaries[0][i]: triggers OutOfRangeException.
 * 3. Algorithm Operational Branches:
 *    - GoalType: MINIMIZE vs MAXIMIZE.
 *    - isActiveCMA: true (active covariance adaptation) vs false (standard adaptation).
 *    - diagonalOnly: <= 0 (full covariance) vs > 0 (diagonal covariance update paths).
 *    - checkFeasableCount: 0 vs > 0 (re-sampling infeasible offspring).
 *    - Convergence conditions: stopFitness reached, stopTolX, stopTolUpX, stopTolFun, stopTolHistFun,
 *      covariance condition number (> 1e7), maxIterations reached, ConvergenceChecker triggered.
 *    - Statistics collection: generateStatistics = true populates sigma, fitness, mean, and D history.
 *    - DoubleIndex helper: compareTo, equals (same, diff, non-instance), and hashCode consistency.
 * -------------------------------------------------------------------------------------------------
 */
public class CMAESOptimizerGeminiTest {

    private static class SphereFunction implements MultivariateFunction {
        public double value(double[] point) {
            double sum = 0.0;
            for (double v : point) {
                sum += v * v;
            }
            return sum;
        }
    }

    private static class InfeasiblePenaltyFunction implements MultivariateFunction {
        public double value(double[] point) {
            double sum = 0.0;
            for (double v : point) {
                sum += (v - 0.5) * (v - 0.5);
            }
            return sum;
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Targets Defects4J known failure:
     * When upper bound minus lower bound exceeds Double.MAX_VALUE (e.g. -Double.MAX_VALUE / 1.5
     * to Double.MAX_VALUE / 1.5), checkParameters must signal NumberIsTooLargeException.
     */
    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testBoundaryRangeTooLarge() {
        final double[] startPoint = new double[] { 0.5 };
        final double[] inSigma = new double[] { 0.1 };
        final double[] lowerBound = new double[] { -Double.MAX_VALUE / 1.5 };
        final double[] upperBound = new double[] { Double.MAX_VALUE / 1.5 };

        CMAESOptimizer optimizer = new CMAESOptimizer(10, inSigma);
        optimizer.optimize(100, new SphereFunction(), GoalType.MINIMIZE,
                startPoint, lowerBound, upperBound);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @