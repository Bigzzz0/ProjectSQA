package org.apache.commons.math.optimization.fitting;

/*
 * [Branch & Defect Analysis Matrix]
 * =================================================================================================
 * Target Class: org.apache.commons.math.optimization.fitting.GaussianFitter & ParameterGuesser
 * Known Defect: Defects4J MATH-519 (GaussianFitter.fit() bypassed exception-safe wrapper function,
 *               passing raw Gaussian.Parametric() instead of delegating to fit(guess), triggering
 *               NotStrictlyPositiveException on non-positive sigma during optimization iterations).
 * -------------------------------------------------------------------------------------------------
 * Partition A: Core Functional & Algorithmic Logic
 * - fit(initialGuess): Evaluates fitted bell curves with user initial guesses.
 * - fit(): ParameterGuesser pipeline end-to-end fitting.
 * - ParameterGuesser.guess(): Primary estimation of norm, mean, and sigma; cache idempotency check.
 * - Interpolation logic: Linear interpolation between two adjacent points on rising and falling edges.
 *
 * Partition B: Boundary Value Analysis (BVA) & Interpolation Extremes
 * - Exact interpolation match: y == pointA.getY(), y == pointB.getY().
 * - OutOfRangeException catch branch in basicGuess: halfY outside point bounds, falling back to
 *   fwhmApprox = points[last].getX() - points[0].getX().
 * - idxStep == 0: Triggers ZeroException in interpolateXAtY and getInterpolationPointsForY.
 * - isBetween boundaries: value == boundary1, value == boundary2, ascending and descending boundaries.
 *
 * Partition C: Defect-Targeted Branch Zone (MATH-519)
 * - testMath519: Observed points from MATH-519 issue report where LevenbergMarquardtOptimizer
 *   iterates through non-positive sigma values. In the defective code, fitter.fit() directly passes
 *   new Gaussian.Parametric(), throwing NotStrictlyPositiveException instead of catching it.
 *
 * Partition D: Exception & Defensive Guard Paths
 * - ParameterGuesser(null) -> NullArgumentException
 * - ParameterGuesser(observations.length < 3) -> NumberIsTooSmallException (tested for N = 0, 1, 2)
 * - Anonymous function in fit(initialGuess):
 *   * value(x, p) with sigma <= 0 returns Double.POSITIVE_INFINITY (NotStrictlyPositiveException caught).
 *   * gradient(x, p) with sigma <= 0 returns array of POSITIVE_INFINITY (caught).
 *
 * Partition E: Comparator Branch Completeness
 * - Comparator in createWeightedObservedPointComparator:
 *   * p1 == null && p2 == null -> 0
 *   * p1 == null -> -1; p2 == null -> 1
 *   * p1.getX() < p2.getX() -> -1; p1.getX() > p2.getX() -> 1
 *   * p1.getY() < p2.getY() -> -1; p1.getY() > p2.getY() -> 1
 *   * p1.getWeight() < p2.getWeight() -> -1; p1.getWeight() > p2.getWeight() -> 1
 *   * p1 equals p2 -> 0
 * =================================================================================================
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Comparator;

import org.apache.commons.math.analysis.DifferentiableMultivariateVectorialFunction;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.apache.commons.math.exception.ZeroException;
import org.apache.commons.math.optimization.ConvergenceChecker;
import org.apache.commons.math.optimization.DifferentiableMultivariateVectorialOptimizer;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;

public class GaussianFitterGeminiTest {

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-519 Ground Truth)
    // =========================================================================

    /**
     * Targets the MATH-519 defect: fitter.fit() without arguments should use the
     * exception-safe wrapper function around Gaussian.Parametric to tolerate
     * intermediate non-strictly positive sigma values during optimization iterations.
     * On the defective code, this test throws NotStrictlyPositiveException.
     */
    @Test(timeout = 4000)
    public void testMath519() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        fitter.addObservedPoint(4.0254623,  531026.0);
        fitter.addObservedPoint(4.03128248, 984167.0);
        fitter.addObservedPoint(4.03839603, 1887233.0);
        fitter.addObservedPoint(4.04421621, 2687152.0);
        fitter.addObservedPoint(4.05132976, 3461228.0);
        fitter.addObservedPoint(4.05326982, 3580526.0);
        fitter.addObservedPoint(4.05779662, 3439750.0);
        fitter.addObservedPoint(4.0636168,  2877648.0);
        fitter.addObservedPoint(4.06943698, 2175960.0);
        fitter.addObservedPoint(4.07525716, 1447024.0);
        fitter.addObservedPoint(4.08237071, 717104.0);
        fitter.addObservedPoint(4.08366408, 620014.0);

        double[] parameters = fitter.fit();
        assertNotNull(parameters);
        assertEquals(3, parameters.length);
        assertEquals(3604085.059, parameters[0], 1.0);
        assertEquals(4.054378, parameters[1], 1e-4);
        assertEquals(0.018269, parameters[2], 1e-4);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFitWithInitialGuess() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        fitter.addObservedPoint(4.0254623,  531026.0);
        fitter.addObservedPoint(4.03128248, 984167.0);
        fitter.addObservedPoint(4.03839603, 1887233.0);
        fitter.addObservedPoint(4.04421621, 2687152.0);
        fitter.addObservedPoint(4.05132976, 3461228.0);
        fitter.addObservedPoint(4.05326982, 3580526.0);
        fitter.addObservedPoint(4.05779662, 3439750.0);
        fitter.addObservedPoint(4.0636168,  2877648.0);
        fitter.addObservedPoint(4.06943698, 2175960.0);
        fitter.addObservedPoint(4.07525716, 1447024.0);
        fitter.addObservedPoint(4.08237071, 717104.0);
        fitter.addObservedPoint(4.08366408, 620014.0);

        double[] initialGuess = new double[] { 3600000.0, 4.05, 0.02 };
        double[] parameters = fitter.fit(initialGuess);

        assertNotNull(parameters);
        assertEquals(3, parameters.length);
        assertEquals(3604085.059, parameters[0], 1.0);
        assertEquals(4.054378, parameters[1], 1e-4);
        assertEquals(0.018269, parameters[2], 1e-4);
    }

    @Test(timeout = 4000)
    public void testFitSimpleBellCurve() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        // Gaussian: norm = 4.0, mean = 2.0, sigma = 1.5
        for (double x = -3.0; x <= 7.0; x += 0.5) {
            double y = 4.0 * Math.exp(-0.5 * Math.pow((x - 2.0) / 1.5, 2));
            fitter.addObservedPoint(x, y);
        }

        double[] result = fitter.fit(new double[] { 3.5, 2.1, 1.4 });
        assertNotNull(result);
        assertEquals(4.0, result[0], 1e-3);
        assertEquals(2.0, result[1], 1e-3);
        assertEquals(1.5, result[2], 1e-3);
    }

    @Test(timeout = 4000)
    public void testParameterGuesserCaching() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 2.0),
            new WeightedObservedPoint(1.0, 2.0, 10.0),
            new WeightedObservedPoint(1.0, 3.0, 2.0)
        };
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(points);
        double[] guess1 = guesser.guess();
        double[] guess2 = guesser.guess();

        assertNotNull(guess1);
        assertNotNull(guess2);
        assertNotSame("guess() should return a clone each time", guess1, guess2);
        assertArrayEquals(guess1, guess2, 1e-12);
    }

    @Test(timeout = 4000)
    public void testParameterGuesserSuccessfulInterpolation() {
        // Construct points where halfY = params[0] + (params[1] - params[0]) / 2.0
        // is strictly between points on left and right sides.
        // maxYIdx will be at (5.0, 10.0): params[0] = 10.0, params[1] = 5.0.
        // halfY = 10.0 + (5.0 - 10.0) / 2.0 = 7.5.
        // Left side: (1.0, 2.0) to (5.0, 10.0) contains 7.5.
        // Right side: (5.0, 10.0) to (9.0, 2.0) contains 7.5.
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 2.0),
            new WeightedObservedPoint(1.0, 5.0, 10.0),
            new WeightedObservedPoint(1.0, 9.0, 2.0)
        };
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();

        assertEquals(10.0, guess[0], 1e-9);
        assertEquals(5.0, guess[1], 1e-9);
        // Interpolation on left: 1.0 + (7.5 - 2.0)*(5.0 - 1.0)/(10.0 - 2.0) = 1.0 + 5.5 * 4.0 / 8.0 = 3.75
        // Interpolation on right: 5.0 + (7.5 - 10.0)*(9.0 - 5.0)/(2.0 - 10.0) = 5.0 + (-2.5)*4.0 / (-8.0) = 6.25
        // fwhmApprox = 6.25 - 3.75 = 2.5
        // sigma = 2.5 / (2.0 * sqrt(2.0 * ln(2.0))) ~ 1.06165
        double expectedSigma = 2.5 / (2.0 * Math.sqrt(2.0 * Math.log(2.0)));
        assertEquals(expectedSigma, guess[2], 1e-5);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Interpolation Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testParameterGuesserExactYMatchOnInterpolationPoints() {
        // Construct points where halfY == maxY.Y:
        // params[0] = 10.0, params[1] = 10.0 -> halfY = 10.0 + (10.0 - 10.0) / 2.0 = 10.0.
        // Left search: pointA.getY() != 10.0, pointB.getY() == 10.0 == y (pointB.getY() == y branch)
        // Right search: pointA.getY() == 10.0 == y (pointA.getY() == y branch)
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 5.0, 2.0),
            new WeightedObservedPoint(1.0, 10.0, 10.0),
            new WeightedObservedPoint(1.0, 15.0, 2.0)
        };
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();

        assertEquals(10.0, guess[0], 1e-9);
        assertEquals(10.0, guess[1], 1e-9);
        // Both left and right interpolateXAtY return 10.0, fwhmApprox = 0.0 -> sigma = 0.0
        assertEquals(0.0, guess[2], 1e-9);
    }

    @Test(timeout = 4000)
    public void testParameterGuesserOutOfRangeExceptionFallback() {
        // When halfY is completely outside the range of Y values:
        // params[0] = 10.0 (Y), params[1] = 1000.0 (X).
        // halfY = 10.0 + (1000.0 - 10.0) / 2.0 = 505.0.
        // All Y values are <= 10.0, so getInterpolationPointsForY throws OutOfRangeException.
        // The catch block sets fwhmApprox = points[last].getX() - points[0].getX() = 1002.0 - 998.0 = 4.0.
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 998.0, 5.0),
            new WeightedObservedPoint(1.0, 1000.0, 10.0),
            new WeightedObservedPoint(1.0, 1002.0, 5.0)
        };
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();

        assertEquals(10.0, guess[0], 1e-9);
        assertEquals(1000.0, guess[1], 1e-9);
        double expectedSigma = 4.0 / (2.0 * Math.sqrt(2.0 * Math.log(2.0)));
        assertEquals(expectedSigma, guess[2], 1e-5);
    }

    @Test(timeout = 4000)
    public void testInterpolateXAtYZeroExceptionViaReflection() throws Exception {
        Method m = GaussianFitter.ParameterGuesser.class.getDeclaredMethod(
            "interpolateXAtY", WeightedObservedPoint[].class, int.class, int.class, double.class);
        m.setAccessible(true);

        WeightedObservedPoint[] pts = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, 2.0),
            new WeightedObservedPoint(1.0, 3.0, 1.0)
        };
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(pts);

        try {
            m.invoke(guesser, pts, 1, 0, 1.5);
            fail("Expected ZeroException wrapped in InvocationTargetException");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof ZeroException);
        }
    }

    @Test(timeout = 4000)
    public void testGetInterpolationPointsForYZeroExceptionViaReflection() throws Exception {
        Method m = GaussianFitter.ParameterGuesser.class.getDeclaredMethod(
            "getInterpolationPointsForY", WeightedObservedPoint[].class, int.class, int.class, double.class);
        m.setAccessible(true);

        WeightedObservedPoint[] pts = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, 2.0),
            new WeightedObservedPoint(1.0, 3.0, 1.0)
        };
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(pts);

        try {
            m.invoke(guesser, pts, 1, 0, 1.5);
            fail("Expected ZeroException wrapped in InvocationTargetException");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof ZeroException);
        }
    }

    @Test(timeout = 4000)
    public void testGetInterpolationPointsForYOutOfRangeExceptionDirect() throws Exception {
        Method m = GaussianFitter.ParameterGuesser.class.getDeclaredMethod(
            "getInterpolationPointsForY", WeightedObservedPoint[].class, int.class, int.class, double.class);
        m.setAccessible(true);

        WeightedObservedPoint[] pts = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 10.0),
            new WeightedObservedPoint(1.0, 2.0, 20.0),
            new WeightedObservedPoint(1.0, 3.0, 15.0)
        };
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(pts);

        try {
            m.invoke(guesser, pts, 1, 1, 999.0);
            fail("Expected OutOfRangeException wrapped in InvocationTargetException");
        } catch (InvocationTargetException e) {
            assertTrue(e.getCause() instanceof OutOfRangeException);
        }
    }

    @Test(timeout = 4000)
    public void testIsBetweenViaReflection() throws Exception {
        Method m = GaussianFitter.ParameterGuesser.class.getDeclaredMethod(
            "isBetween", double.class, double.class, double.class);
        m.setAccessible(true);

        WeightedObservedPoint[] pts = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, 2.0),
            new WeightedObservedPoint(1.0, 3.0, 1.0)
        };
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(pts);

        // boundary1 <= boundary2
        assertTrue((Boolean) m.invoke(guesser, 5.0, 1.0, 10.0));
        assertTrue((Boolean) m.invoke(guesser, 1.0, 1.0, 10.0));
        assertTrue((Boolean) m.invoke(guesser, 10.0, 1.0, 10.0));
        assertFalse((Boolean) m.invoke(guesser, 0.9, 1.0, 10.0));
        assertFalse((Boolean) m.invoke(guesser, 10.1, 1.0, 10.0));

        // boundary2 <= boundary1
        assertTrue((Boolean) m.invoke(guesser, 5.0, 10.0, 1.0));
        assertTrue((Boolean) m.invoke(guesser, 1.0, 10.0, 1.0));
        assertTrue((Boolean) m.invoke(guesser, 10.0, 10.0, 1.0));
        assertFalse((Boolean) m.invoke(guesser, 0.9, 10.0, 1.0));
        assertFalse((Boolean) m.invoke(guesser, 10.1, 10.0, 1.0));
    }

    @Test(timeout = 4000)
    public void testFindMaxYViaReflection() throws Exception {
        Method m = GaussianFitter.ParameterGuesser.class.getDeclaredMethod(
            "findMaxY", WeightedObservedPoint[].class);
        m.setAccessible(true);

        WeightedObservedPoint[] pts = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, 2.0),
            new WeightedObservedPoint(1.0, 3.0, 1.0)
        };
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(pts);

        // Max at index 0
        WeightedObservedPoint[] pts0 = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 50.0),
            new WeightedObservedPoint(1.0, 2.0, 10.0),
            new WeightedObservedPoint(1.0, 3.0, 20.0)
        };
        assertEquals(0, (int) m.invoke(guesser, (Object) pts0));

        // Max at index 2
        WeightedObservedPoint[] pts2 = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 10.0),
            new WeightedObservedPoint(1.0, 2.0, 20.0),
            new WeightedObservedPoint(1.0, 3.0, 50.0)
        };
        assertEquals(2, (int) m.invoke(guesser, (Object) pts2));
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testParameterGuesserNullPoints() {
        new GaussianFitter.ParameterGuesser(null);
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testParameterGuesserEmptyPoints() {
        new GaussianFitter.ParameterGuesser(new WeightedObservedPoint[0]);
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testParameterGuesserOnePoint() {
        new GaussianFitter.ParameterGuesser(new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 1.0)
        });
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testParameterGuesserTwoPoints() {
        new GaussianFitter.ParameterGuesser(new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, 2.0)
        });
    }

    /**
     * Exercises the exception-handling wrapper function generated inside
     * GaussianFitter.fit(double[] initialGuess).
     * Ensures value() and gradient() catch NotStrictlyPositiveException and return POSITIVE_INFINITY.
     */
    @Test(timeout = 4000)
    public void testAnonymousFunctionExceptionHandlingInFitWithGuess() {
        final double[][] interceptedValueHolder = new double[2][];
        final double[][][] interceptedJacobianHolder = new double[2][][];

        DifferentiableMultivariateVectorialOptimizer dummyOptimizer =
            new DifferentiableMultivariateVectorialOptimizer() {
                public int getMaxEvaluations() { return 100; }
                public int getEvaluations() { return 0; }
                public int getMaxIterations() { return 100; }
                public int getIterations() { return 0; }
                public ConvergenceChecker<VectorialPointValuePair> getConvergenceChecker() { return null; }
                public void setMaxEvaluations(int maxEvaluations) {}
                public void setMaxIterations(int maxIterations) {}
                public void setConvergenceChecker(ConvergenceChecker<VectorialPointValuePair> checker) {}

                public VectorialPointValuePair optimize(
                        DifferentiableMultivariateVectorialFunction f,
                        double[] target, double[] weights, double[] startPoint) {
                    // 1. Evaluate with positive sigma (normal path)
                    interceptedValueHolder[0] = f.value(new double[] { 2.0, 1.0, 1.0 });
                    interceptedJacobianHolder[0] = f.jacobian().value(new double[] { 2.0, 1.0, 1.0 });

                    // 2. Evaluate with non-positive sigma <= 0 (triggers NotStrictlyPositiveException catch path)
                    interceptedValueHolder[1] = f.value(new double[] { 2.0, 1.0, -0.5 });
                    interceptedJacobianHolder[1] = f.jacobian().value(new double[] { 2.0, 1.0, -0.5 });

                    return new VectorialPointValuePair(startPoint, interceptedValueHolder[0]);
                }
            };

        GaussianFitter fitter = new GaussianFitter(dummyOptimizer);
        fitter.addObservedPoint(1.0, 2.0);

        fitter.fit(new double[] { 2.0, 1.0, 1.0 });

        // Normal path assertions
        assertNotNull(interceptedValueHolder[0]);
        assertFalse(Double.isInfinite(interceptedValueHolder[0][0]));
        assertNotNull(interceptedJacobianHolder[0]);
        assertFalse(Double.isInfinite(interceptedJacobianHolder[0][0][0]));

        // Exception-caught path assertions: must be POSITIVE_INFINITY
        assertNotNull(interceptedValueHolder[1]);
        assertEquals(Double.POSITIVE_INFINITY, interceptedValueHolder[1][0], 0.0);

        assertNotNull(interceptedJacobianHolder[1]);
        assertEquals(Double.POSITIVE_INFINITY, interceptedJacobianHolder[1][0][0], 0.0);
        assertEquals(Double.POSITIVE_INFINITY, interceptedJacobianHolder[1][0][1], 0.0);
        assertEquals(Double.POSITIVE_INFINITY, interceptedJacobianHolder[1][0][2], 0.0);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Comparator Completeness
    // =========================================================================

    @Test(timeout = 4000)
    @SuppressWarnings("unchecked")
    public void testWeightedObservedPointComparatorBranches() throws Exception {
        Method m = GaussianFitter.ParameterGuesser.class.getDeclaredMethod(
            "createWeightedObservedPointComparator");
        m.setAccessible(true);

        WeightedObservedPoint[] pts = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, 2.0),
            new WeightedObservedPoint(1.0, 3.0, 1.0)
        };
        GaussianFitter.ParameterGuesser guesser = new GaussianFitter.ParameterGuesser(pts);
        Comparator<WeightedObservedPoint> comp =
            (Comparator<WeightedObservedPoint>) m.invoke(guesser);

        // Branch: null checks
        assertEquals(0, comp.compare(null, null));
        WeightedObservedPoint base = new WeightedObservedPoint(1.0, 5.0, 10.0);
        assertEquals(-1, comp.compare(null, base));
        assertEquals(1, comp.compare(base, null));

        // Branch: p1.getX() < p2.getX() and >
        WeightedObservedPoint pXLess = new WeightedObservedPoint(1.0, 4.0, 10.0);
        WeightedObservedPoint pXMore = new WeightedObservedPoint(1.0, 6.0, 10.0);
        assertEquals(-1, comp.compare(pXLess, base));
        assertEquals(1, comp.compare(pXMore, base));

        // Branch: same X, p1.getY() < p2.getY() and >
        WeightedObservedPoint pYLess = new WeightedObservedPoint(1.0, 5.0, 9.0);
        WeightedObservedPoint pYMore = new WeightedObservedPoint(1.0, 5.0, 11.0);
        assertEquals(-1, comp.compare(pYLess, base));
        assertEquals(1, comp.compare(pYMore, base));

        // Branch: same X and Y, p1.getWeight() < p2.getWeight() and >
        WeightedObservedPoint pWLess = new WeightedObservedPoint(0.5, 5.0, 10.0);
        WeightedObservedPoint pWMore = new WeightedObservedPoint(1.5, 5.0, 10.0);
        assertEquals(-1, comp.compare(pWLess, base));
        assertEquals(1, comp.compare(pWMore, base));

        // Branch: completely equal -> returns 0
        WeightedObservedPoint pEqual = new WeightedObservedPoint(1.0, 5.0, 10.0);
        assertEquals(0, comp.compare(base, pEqual));
    }
}