package org.apache.commons.math3.optimization.fitting;

import static org.junit.Assert.*;
import org.apache.commons.math3.analysis.function.HarmonicOscillator;
import org.apache.commons.math3.exception.MathIllegalStateException;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.ZeroException;
import org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.util.FastMath;
import org.junit.Test;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.math3.optimization.fitting.HarmonicFitter
 * Inner Class:  org.apache.commons.math3.optimization.fitting.HarmonicFitter.ParameterGuesser
 *
 * Decision / Branch Coverage Targets:
 * 1. ParameterGuesser constructor:
 *    - Branch: observations.length < 4 -> throws NumberIsTooSmallException (len = 0, 1, 2, 3).
 *    - Branch: observations.length >= 4 -> array cloned and initialized.
 * 2. ParameterGuesser.sortObservations():
 *    - Branch: curr.getX() < prec.getX() [true] (unsorted / descending / middle insertion).
 *    - Branch: curr.getX() < prec.getX() [false] (already sorted, equal abscissae).
 *    - Branch: while (i >= 0 && curr.getX() < mI.getX())
 *        - Exit on i < 0 (curr becomes head, i-- != 0 evaluated false when i == 0).
 *        - Exit on curr.getX() >= mI.getX() (curr placed inside list).
 * 3. ParameterGuesser.guessAOmega():
 *    - Branch: (c1 / c2 < 0) || (c2 / c3 < 0)
 *        - Sub-branch: xRange == 0 -> throws ZeroException (abscissa range zero).
 *        - Sub-branch: y < yMin, y > yMax updates.
 *        - Sub-branch: ill-conditioned samples leading to failure (MATH-844).
 *    - Branch: else path where c1/c2 >= 0 and c2/c3 >= 0 -> FastMath.sqrt calculations.
 * 4. ParameterGuesser.guessPhi():
 *    - Normal sinusoidal phase derivation using mean sine/cosine projections.
 * 5. HarmonicFitter lifecycle:
 *    - fit(double[] initialGuess) via parametric oscillator fitting.
 *    - fit() auto-guessing coefficients from observations.
 *
 * Defect Ground Truth:
 * - MATH-844 (Defects4J Math-25): Ill-conditioned observations in guesser failed to trigger
 *   expected MathIllegalStateException when quadratic integrals produce non-sensible results.
 */
public class HarmonicFitterGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testFitWithExplicitInitialGuess() {
        HarmonicFitter fitter = new HarmonicFitter(new LevenbergMarquardtOptimizer());
        final double a = 2.5;
        final double omega = 1.3;
        final double phi = 0.4;
        HarmonicOscillator osc = new HarmonicOscillator(a, omega, phi);

        for (int i = 0; i < 30; ++i) {
            double x = i * 0.2;
            fitter.addObservedPoint(1.0, x, osc.value(x));
        }

        double[] fitted = fitter.fit(new double[] { 2.0, 1.0, 0.2 });
        assertNotNull(fitted);
        assertEquals(3, fitted.length);
        assertEquals(a, fitted[0], 1e-4);
        assertEquals(omega, fitted[1], 1e-4);
        assertEquals(phi, fitted[2], 1e-4);
    }

    @Test(timeout = 4000)
    public void testFitWithAutoComputedInitialGuess() {
        HarmonicFitter fitter = new HarmonicFitter(new LevenbergMarquardtOptimizer());
        final double a = 3.2;
        final double omega = 0.85;
        final double phi = 1.1;
        HarmonicOscillator osc = new HarmonicOscillator(a, omega, phi);

        for (int i = 0; i < 50; ++i) {
            double x = i * 0.15;
            fitter.addObservedPoint(x, osc.value(x));
        }

        double[] fitted = fitter.fit();
        assertNotNull(fitted);
        assertEquals(3, fitted.length);
        assertEquals(a, fitted[0], 1e-3);
        assertEquals(omega, fitted[1], 1e-3);
        assertEquals(phi, fitted[2], 1e-3);
    }

    @Test(timeout = 4000)
    public void testParameterGuesserPureHarmonic() {
        final double a = 1.8;
        final double omega = 2.4;
        final double phi = 0.7;
        HarmonicOscillator osc = new HarmonicOscillator(a, omega, phi);

        WeightedObservedPoint[] points = new WeightedObservedPoint[60];
        for (int i = 0; i < points.length; ++i) {
            double x = i * 0.1;
            points[i] = new WeightedObservedPoint(1.0, x, osc.value(x));
        }

        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();

        assertNotNull(guess);
        assertEquals(3, guess.length);
        assertEquals(a, guess[0], 0.1);
        assertEquals(omega, guess[1], 0.1);

        double normalizedExpectedPhi = phi % (2 * FastMath.PI);
        double normalizedActualPhi = guess[2] % (2 * FastMath.PI);
        if (normalizedActualPhi < 0) {
            normalizedActualPhi += 2 * FastMath.PI;
        }
        if (normalizedExpectedPhi < 0) {
            normalizedExpectedPhi += 2 * FastMath.PI;
        }
        assertEquals(normalizedExpectedPhi, normalizedActualPhi, 0.2);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis & Sorting Branches
    // =========================================================================

    @Test(timeout = 4000)
    public void testParameterGuesserReverseSortedInput() {
        // Targets sortObservations insertion at beginning: i reaches 0 and decrements to -1
        HarmonicOscillator osc = new HarmonicOscillator(2.0, 1.5, 0.2);
        final int len = 25;
        WeightedObservedPoint[] points = new WeightedObservedPoint[len];
        for (int i = 0; i < len; ++i) {
            double x = (len - 1 - i) * 0.2;
            points[i] = new WeightedObservedPoint(1.0, x, osc.value(x));
        }

        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();
        assertNotNull(guess);
        assertEquals(3, guess.length);
        assertEquals(2.0, guess[0], 0.25);
        assertEquals(1.5, guess[1], 0.25);
    }

    @Test(timeout = 4000)
    public void testParameterGuesserMiddleInsertionSort() {
        // Targets sortObservations while condition exit on curr.getX() >= mI.getX()
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 1.0),
            new WeightedObservedPoint(1.0, 10.0, -1.0),
            new WeightedObservedPoint(1.0, 5.0, 0.0),
            new WeightedObservedPoint(1.0, 2.5, 0.5),
            new WeightedObservedPoint(1.0, 7.5, -0.5)
        };

        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();
        assertNotNull(guess);
        assertEquals(3, guess.length);
        assertTrue(guess[0] >= 0);
        assertTrue(guess[1] > 0);
    }

    @Test(timeout = 4000)
    public void testParameterGuesserWithIdenticalAbscissaeInSample() {
        // Non-strict monotonic sequence to exercise sorting stability with identical X
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 0.0),
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, 0.0),
            new WeightedObservedPoint(1.0, 3.0, -1.0)
        };

        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();
        assertNotNull(guess);
        assertEquals(3, guess.length);
    }

    @Test(timeout = 4000)
    public void testParameterGuesserConstantYObservations() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 4.0),
            new WeightedObservedPoint(1.0, 1.0, 4.0),
            new WeightedObservedPoint(1.0, 2.0, 4.0),
            new WeightedObservedPoint(1.0, 3.0, 4.0)
        };

        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guess = guesser.guess();
        assertNotNull(guess);
        assertEquals(3, guess.length);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-844 / Defects4J Math-25)
    // =========================================================================

    /**
     * Dedicated defect reproduction test for MATH-844.
     * When sample points are ill-conditioned such that quadratic integrals yield
     * non-positive ratios (or zero denominator), ParameterGuesser must throw
     * MathIllegalStateException rather than producing invalid/bogus coefficients.
     */
    @Test(expected = MathIllegalStateException.class, timeout = 4000)
    public void testMath844() {
        final double[] y = {
            0, 1, 2, 3, 2, 1,
            0, -1, -2, -3, -2, -1,
            0, 1, 2, 3, 2, 1,
            0, -1, -2, -3, -2, -1,
            0, 1, 2, 3, 2, 1, 0
        };
        final int len = y.length;
        final WeightedObservedPoint[] points = new WeightedObservedPoint[len];
        for (int i = 0; i < len; i++) {
            points[i] = new WeightedObservedPoint(1.0, i, y[i]);
        }
        new HarmonicFitter.ParameterGuesser(points).guess();
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testGuesserPreconditionsLengthZero() {
        new HarmonicFitter.ParameterGuesser(new WeightedObservedPoint[0]);
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testGuesserPreconditionsLengthThree() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 0.0),
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, 0.0)
        };
        new HarmonicFitter.ParameterGuesser(points);
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testHarmonicFitterFitTooFewObservations() {
        HarmonicFitter fitter = new HarmonicFitter(new LevenbergMarquardtOptimizer());
        fitter.addObservedPoint(1.0, 0.0);
        fitter.addObservedPoint(2.0, 1.0);
        fitter.addObservedPoint(3.0, 0.0);
        fitter.fit();
    }

    // =========================================================================
    // Partition E: Object Contract, Defensive Copy & Fitting Nuances
    // =========================================================================

    @Test(timeout = 4000)
    public void testGuesserClonesObservationsDefensively() {
        WeightedObservedPoint p1 = new WeightedObservedPoint(1.0, 0.0, 0.0);
        WeightedObservedPoint p2 = new WeightedObservedPoint(1.0, 1.0, 1.0);
        WeightedObservedPoint p3 = new WeightedObservedPoint(1.0, 2.0, 0.0);
        WeightedObservedPoint p4 = new WeightedObservedPoint(1.0, 3.0, -1.0);

        WeightedObservedPoint[] points = new WeightedObservedPoint[] { p1, p2, p3, p4 };
        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);

        // Mutate original array
        points[0] = new WeightedObservedPoint(1.0, 999.0, 999.0);

        // ParameterGuesser internal clone should be unaffected and executable
        double[] guess = guesser.guess();
        assertNotNull(guess);
        assertEquals(3, guess.length);
    }

    @Test(timeout = 4000)
    public void testFitterWithNonUniformWeights() {
        HarmonicFitter fitter = new HarmonicFitter(new LevenbergMarquardtOptimizer());
        final double a = 2.0;
        final double omega = 1.5;
        final double phi = 0.5;
        HarmonicOscillator osc = new HarmonicOscillator(a, omega, phi);

        for (int i = 0; i < 40; ++i) {
            double x = i * 0.1;
            double weight = (i % 2 == 0) ? 2.5 : 0.5;
            fitter.addObservedPoint(weight, x, osc.value(x));
        }

        double[] fitted = fitter.fit();
        assertEquals(a, fitted[0], 1e-3);
        assertEquals(omega, fitted[1], 1e-3);
        assertEquals(phi, fitted[2], 1e-3);
    }

    @Test(timeout = 4000)
    public void testTinyVariationsData() {
        HarmonicFitter fitter = new HarmonicFitter(new LevenbergMarquardtOptimizer());
        fitter.addObservedPoint(1.0, 0.0, 1.0);
        fitter.addObservedPoint(1.0, 1.0, 1.0 + 1e-11);
        fitter.addObservedPoint(1.0, 2.0, 1.0);
        fitter.addObservedPoint(1.0, 3.0, 1.0 - 1e-11);

        double[] fitted = fitter.fit(new double[] { 1e-11, 1.57, 0.0 });
        assertNotNull(fitted);
        assertEquals(3, fitted.length);
        assertFalse(Double.isNaN(fitted[0]));
        assertFalse(Double.isNaN(fitted[1]));
        assertFalse(Double.isNaN(fitted[2]));
    }
}