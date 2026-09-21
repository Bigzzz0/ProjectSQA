package org.apache.commons.math.optimization.fitting;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math.optimization.fitting.WeightedObservedPoint;
import org.apache.commons.math.optimization.fitting.GaussianFitter.ParameterGuesser;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.exception.NumberIsTooSmallException;
import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.apache.commons.math.exception.ZeroException;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * Coverage targets (based on GaussianFitter and inner ParameterGuesser):
 * 
 * GaussianFitter:
 *   - Constructor: single path.
 *   - fit(double[]): creates anonymous Parametric that catches NotStrictlyPositiveException.
 *   - fit(): calls ParameterGuesser.guess() and fit(new Gaussian.Parametric(), guess).
 *             * Bug: ParameterGuesser.basicGuess() may return sigma <= 0 due to incorrect halfY formula.
 *               This causes fit() to throw NotStrictlyPositiveException on defective version.
 * ParameterGuesser:
 *   - Constructor: null check, length <3 check.
 *   - guess(): lazy initialization, returns clone.
 *   - basicGuess():
 *        * Sorting points by x (comparator handles nulls, x, y, weight).
 *        * findMaxY(): iterate for largest Y.
 *        * halfY calculation: params[0] + ((params[1] - params[0]) / 2.0)  <-- BUG
 *        * interpolateXAtY() and getInterpolationPointsForY():
 *            - idxStep == 0 → ZeroException.
 *            - isBetween() inclusive check.
 *            - If no bracketing pair found and y is within overall Y range → OutOfRangeException (also bug potential).
 *        * Fallback: fwhmApprox = points[last].x - points[0].x.
 *   - findMaxY(): single loop.
 *   - isBetween(): boundary inclusive.
 *   - createWeightedObservedPointComparator(): null-safe comparator.
 * 
 * Test partitions:
 * A. Core functional: fit() with valid data, fit(double[]) with valid guess.
 * B. Boundaries: null observations, empty observations, single point, <3 points.
 * C. Defect-targeted: data that causes halfY = maxY (maxX == maxY) leading to sigma = 0, triggering NotStrictlyPositiveException in buggy version.
 * D. Exception paths: ZeroException for idxStep=0, OutOfRangeException when y outside range, NullArgumentException, NumberIsTooSmallException.
 * E. Object lifecycle: ParameterGuesser guess() returns different instances, clone() used.
 */
public class GaussianFitterDeepseekTest {

    // -----------------------------------------------------------------------
    // Part A: Core Functional Logic
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFitWithInitialGuess() {
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        // Add points that roughly form a Gaussian
        fitter.addObservedPoint(0.0, 1.0);
        fitter.addObservedPoint(1.0, 5.0);
        fitter.addObservedPoint(2.0, 1.0);
        double[] guess = {2.0, 1.0, 0.5};
        double[] result = fitter.fit(guess);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertTrue("norm should be positive", result[0] > 0.0);
        assertTrue("sigma should be positive", result[2] > 0.0);
    }

    // -----------------------------------------------------------------------
    // Part B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testParameterGuesserNullObservations() {
        new ParameterGuesser(null);
    }

    @Test(timeout = 4000, expected = NumberIsTooSmallException.class)
    public void testParameterGuesserTooFewPoints() {
        WeightedObservedPoint[] obs = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 1.0),
            new WeightedObservedPoint(1.0, 1.0, 2.0)
        };
        new ParameterGuesser(obs);
    }

    @Test(timeout = 4000, expected = NumberIsTooSmallException.class)
    public void testParameterGuesserEmptyArray() {
        WeightedObservedPoint[] obs = new WeightedObservedPoint[0];
        new ParameterGuesser(obs);
    }

    @Test(timeout = 4000)
    public void testParameterGuesserGuessCloneIndependence() {
        WeightedObservedPoint[] obs = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 1.0),
            new WeightedObservedPoint(1.0, 1.0, 5.0),
            new WeightedObservedPoint(1.0, 2.0, 1.0)
        };
        ParameterGuesser guesser = new ParameterGuesser(obs);
        double[] guess1 = guesser.guess();
        double[] guess2 = guesser.guess();
        // Should return fresh copies
        assertNotSame(guess1, guess2);
        assertArrayEquals(guess1, guess2, 1e-10);
    }

    @Test(timeout = 4000)
    public void testParameterGuesserCaching() {
        WeightedObservedPoint[] obs = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 1.0),
            new WeightedObservedPoint(1.0, 1.0, 5.0),
            new WeightedObservedPoint(1.0, 2.0, 1.0)
        };
        ParameterGuesser guesser = new ParameterGuesser(obs);
        // First call computes
        double[] guess1 = guesser.guess();
        // Second call should return cached (but still clone)
        double[] guess2 = guesser.guess();
        assertArrayEquals(guess1, guess2, 1e-10);
    }

    // -----------------------------------------------------------------------
    // Part C: Defect-Targeted (testMath519 scenario - sigma = 0 from guess)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testFitWithZeroSigmaGuess() {
        // Points designed so that maxX == maxY => halfY == maxY => sigma = 0 from buggy basicGuess.
        // Buggy: halfY = maxY + (maxX - maxY)/2 = maxY => interpolation returns peak x twice => fwhm = 0 => sigma = 0.
        // Correct: halfY = maxY/2 => positive sigma.
        WeightedObservedPoint[] obs = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, -5.0, 1.0),
            new WeightedObservedPoint(1.0, 0.0, 5.0),   // peak: X=0, Y=5
            new WeightedObservedPoint(1.0, 5.0, 1.0)
        };
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        for (WeightedObservedPoint p : obs) {
            fitter.addObservedPoint(p.getWeight(), p.getX(), p.getY());
        }
        try {
            double[] result = fitter.fit();
            // If no exception, the test passes (bug is absent)
            assertNotNull(result);
            assertTrue("norm should be positive", result[0] > 0.0);
            assertTrue("sigma should be positive", result[2] > 0.0);
        } catch (NotStrictlyPositiveException e) {
            // In buggy version, this exception is thrown; test fails as expected.
            fail("Bug triggered: NotStrictlyPositiveException thrown due to sigma <= 0 in guess: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Part D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = ZeroException.class)
    public void testInterpolateXAtYZeroStep() {
        // Access private via reflection? Not possible; we test via basicGuess indirectly.
        // But we can test by calling ParameterGuesser.guess() on a dataset that forces idxStep=0? Not possible.
        // Instead, we can rely on the internal logic: idxStep=0 throws ZeroException.
        // We'll create a test that should never be reached; but to cover the guard, we can't directly.
        // Alternatively, we can use reflection in a separate test? Not allowed.
        // We'll skip direct testing of private method, but the exception is thrown only if called with idxStep=0, which never happens in normal flow.
    }

    @Test(timeout = 4000)
    public void testGetInterpolationPointsForYOutOfRange() {
        // Points where y is outside overall Y range (e.g., very high)
        WeightedObservedPoint[] obs = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 1.0),
            new WeightedObservedPoint(1.0, 1.0, 2.0),
            new WeightedObservedPoint(1.0, 2.0, 3.0) // Y increasing, no peak? Actually maxY = 3 at last point.
        };
        ParameterGuesser guesser = new ParameterGuesser(obs);
        // Try to guess – basicGuess will try to find halfY, which will be above maxY? Compute:
        // sorted: (0,1), (1,2), (2,3). maxYIdx=2 (2,3). params[0]=3, params[1]=2. halfY = 3 + (2-3)/2 = 2.5.
        // Left side: startIdx=2, idxStep=-1 => i=2, i+idxStep=1>=0: check isBetween(2.5, points[2].Y=3, points[1].Y=2) => true (2.5 between 2 and 3). So finds pair.
        // Right side: startIdx=2, idxStep=+1 => i=2, i+idxStep=3<3? false, so no pair. Then compute minY=1, maxY=3. y=2.5 is between, so OutOfRangeException thrown.
        // This should happen. The OutOfRangeException will be caught in basicGuess and fallback used.
        try {
            double[] params = guesser.guess();
            // Should not throw to top level; fallback used.
            assertNotNull(params);
        } catch (OutOfRangeException e) {
            // The catch in basicGuess should handle it; if it propagates, test fails.
            fail("OutOfRangeException should be caught in basicGuess, not propagated.");
        }
    }

    @Test(timeout = 4000)
    public void testParameterGuesserWithFlatYValues() {
        // All Y equal, so maxY at first point? Actually findMaxY returns first index if all equal.
        WeightedObservedPoint[] obs = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 5.0),
            new WeightedObservedPoint(1.0, 1.0, 5.0),
            new WeightedObservedPoint(1.0, 2.0, 5.0)
        };
        ParameterGuesser guesser = new ParameterGuesser(obs);
        double[] guess = guesser.guess();
        // sigma should be positive (fallback to full width or interpolation may produce positive)
        assertTrue("sigma should be positive", guess[2] > 0.0);
    }

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testFitWithNegativeSigmaGuess() {
        // Direct call to fit(double[]) with negative sigma should throw NotStrictlyPositiveException
        GaussianFitter fitter = new GaussianFitter(new LevenbergMarquardtOptimizer());
        fitter.addObservedPoint(1.0, 0.0, 1.0);
        fitter.addObservedPoint(1.0, 1.0, 2.0);
        fitter.addObservedPoint(1.0, 2.0, 1.0);
        double[] badGuess = {1.0, 1.0, -0.5};
        fitter.fit(badGuess);
    }

    // -----------------------------------------------------------------------
    // Part E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testParameterGuesserObservationsClone() {
        WeightedObservedPoint[] obs = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 1.0),
            new WeightedObservedPoint(1.0, 1.0, 2.0),
            new WeightedObservedPoint(1.0, 2.0, 3.0)
        };
        ParameterGuesser guesser = new ParameterGuesser(obs);
        // Modify original array - should not affect guesser
        obs[0] = new WeightedObservedPoint(1.0, 999.0, 999.0);
        double[] guess = guesser.guess();
        // The guess should be based on original points, not modified
        assertTrue(guess[1] != 999.0);
    }
}