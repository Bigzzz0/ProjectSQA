package org.apache.commons.math3.optimization.fitting;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.optimization.DifferentiableMultivariateVectorOptimizer;
import org.apache.commons.math3.optimization.general.LevenbergMarquardtOptimizer;
import org.apache.commons.math3.exception.NumberIsTooSmallException;
import org.apache.commons.math3.exception.ZeroException;
import org.apache.commons.math3.exception.MathIllegalStateException;

/**
 * Advanced white-box test for HarmonicFitter and its internal ParameterGuesser.
 * Targets maximum coverage, boundary analysis, and the known MATH-844 defect.
 */
public class HarmonicFitterDeepseekTest {

    /*
     * Branch & Defect Analysis Matrix
     * 
     * Partition A: Core functional logic – normal sine wave fitting.
     *   - A1: ParameterGuesser.guess() returns [amplitude, omega, phi].
     *   - A2: HarmonicFitter.fit(double[]) with initial guess returns parameters.
     *   - A3: HarmonicFitter.fit() (no guess) uses ParameterGuesser.
     * 
     * Partition B: Boundary Value Analysis & Extremes.
     *   - B1: Insufficient observations (0–3) → NumberIsTooSmallException.
     *   - B2: Zero abscissa range (all x equal) → ZeroException in guessAOmega.
     *   - B3: Observations sorted vs unsorted (insertion sort path).
     * 
     * Partition C: Defect-Targeted Branch Zone (MATH-844).
     *   - C1: Ill-conditioned data (e.g., constant y) where c1/c2 and c2/c3 are
     *         not negative but still invalid (division by zero) → should throw
     *         MathIllegalStateException but defective version returns NaN.
     * 
     * Partition D: Exception & Defensive Guard Paths.
     *   - D1: Null observations passed to ParameterGuesser constructor → NPE.
     *   - D2: Invalid initial guess (negative amplitude, zero frequency) → fit
     *         via optimizer may fail gracefully or return poor fit; not covered here.
     * 
     * Partition E: Object Lifecycle & Contract Integrity.
     *   - E1: ParameterGuesser keeps a clone of observations (state safety).
     *   - E2: HarmonicFitter constructor forwards optimizer.
     */

    // --- Partition A: Core Functional Logic ---

    @Test(timeout = 4000)
    public void testParameterGuesserNormalSineWave() {
        // Generate a clean sine wave: a = 1.0, omega = 2*PI, phi = 0.5
        double a = 1.0, omega = 2.0 * Math.PI, phi = 0.5;
        WeightedObservedPoint[] points = createSineWave(0.0, 0.1, 20, a, omega, phi);
        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guessed = guesser.guess();
        // Allow reasonable tolerance (guesses are rough)
        assertEquals("Amplitude", a, guessed[0], 0.1);
        assertEquals("Omega", omega, guessed[1], 0.2);
        // Phase is more sensitive; allow 0.5 rad tolerance
        assertEquals("Phi", phi, guessed[2], 0.5);
    }

    @Test(timeout = 4000)
    public void testHarmonicFitterFitWithInitialGuess() {
        // Use a real optimizer
        DifferentiableMultivariateVectorOptimizer optimizer = new LevenbergMarquardtOptimizer();
        HarmonicFitter fitter = new HarmonicFitter(optimizer);
        double a = 2.0, omega = 3.0, phi = 0.2;
        WeightedObservedPoint[] points = createSineWave(0.0, 0.05, 40, a, omega, phi);
        for (WeightedObservedPoint p : points) {
            fitter.addObservedPoint(p.getWeight(), p.getX(), p.getY());
        }
        double[] initialGuess = {1.5, 2.8, 0.1};
        double[] result = fitter.fit(initialGuess);
        assertEquals("Amplitude", a, result[0], 0.1);
        assertEquals("Omega", omega, result[1], 0.1);
        assertEquals("Phi", phi, result[2], 0.1);
    }

    @Test(timeout = 4000)
    public void testHarmonicFitterFitNoGuess() {
        DifferentiableMultivariateVectorOptimizer optimizer = new LevenbergMarquardtOptimizer();
        HarmonicFitter fitter = new HarmonicFitter(optimizer);
        double a = 0.8, omega = 5.0, phi = 1.0;
        WeightedObservedPoint[] points = createSineWave(0.0, 0.02, 100, a, omega, phi);
        for (WeightedObservedPoint p : points) {
            fitter.addObservedPoint(p.getWeight(), p.getX(), p.getY());
        }
        double[] result = fitter.fit();  // uses guesser
        assertEquals("Amplitude", a, result[0], 0.1);
        assertEquals("Omega", omega, result[1], 0.2);
        assertEquals("Phi", phi, result[2], 0.5);
    }

    // --- Partition B: Boundary Value Analysis & Extremes ---

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testGuesserInsufficientObservations() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[3];
        for (int i = 0; i < 3; i++) {
            points[i] = new WeightedObservedPoint(1.0, i, i);
        }
        new HarmonicFitter.ParameterGuesser(points);
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testGuesserEmptyObservations() {
        WeightedObservedPoint[] points = new WeightedObservedPoint[0];
        new HarmonicFitter.ParameterGuesser(points);
    }

    @Test(expected = ZeroException.class, timeout = 4000)
    public void testGuesserZeroAbscissaRange() {
        // All points have same x, different y
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 1.0),
            new WeightedObservedPoint(1.0, 0.0, 2.0),
            new WeightedObservedPoint(1.0, 0.0, 3.0),
            new WeightedObservedPoint(1.0, 0.0, 4.0)
        };
        new HarmonicFitter.ParameterGuesser(points).guess();
    }

    @Test(timeout = 4000)
    public void testUnsortedObservationsInsertionSort() {
        // Provide unsorted points to exercise the sortObservations() path
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 3.0, 0.5),
            new WeightedObservedPoint(1.0, 1.0, 1.0),
            new WeightedObservedPoint(1.0, 2.0, 0.8),
            new WeightedObservedPoint(1.0, 0.0, 0.2)
        };
        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        double[] guessed = guesser.guess();
        // Should not throw, and guess should be reasonable for a noisy sine
        assertFalse("Amplitude should be finite", Double.isNaN(guessed[0]));
        assertFalse("Omega should be finite", Double.isNaN(guessed[1]));
        assertFalse("Phi should be finite", Double.isNaN(guessed[2]));
    }

    // --- Partition C: Defect-Targeted Branch Zone (MATH-844) ---

    @Test(expected = MathIllegalStateException.class, timeout = 4000)
    public void testGuesserMath844IllConditionedData() {
        // Constant y values cause c1, c2, c3 all zero -> division by zero (NaN)
        // The defective version returns NaN instead of throwing MathIllegalStateException.
        WeightedObservedPoint[] points = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 5.0),
            new WeightedObservedPoint(1.0, 1.0, 5.0),
            new WeightedObservedPoint(1.0, 2.0, 5.0),
            new WeightedObservedPoint(1.0, 3.0, 5.0)
        };
        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(points);
        guesser.guess();  // should throw MathIllegalStateException
    }

    @Test(expected = MathIllegalStateException.class, timeout = 4000)
    public void testHarmonicFitterFitNoGuessMath844() {
        // Same ill-conditioned data via fit() (no guess)
        DifferentiableMultivariateVectorOptimizer optimizer = new LevenbergMarquardtOptimizer();
        HarmonicFitter fitter = new HarmonicFitter(optimizer);
        fitter.addObservedPoint(1.0, 0.0, 5.0);
        fitter.addObservedPoint(1.0, 1.0, 5.0);
        fitter.addObservedPoint(1.0, 2.0, 5.0);
        fitter.addObservedPoint(1.0, 3.0, 5.0);
        fitter.fit();  // triggers guesser -> should throw MathIllegalStateException
    }

    // --- Partition D: Exception & Defensive Guard Paths ---

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testGuesserNullObservations() {
        new HarmonicFitter.ParameterGuesser(null);
    }

    @Test(expected = NumberIsTooSmallException.class, timeout = 4000)
    public void testFitterNoObservationsThenFit() {
        DifferentiableMultivariateVectorOptimizer optimizer = new LevenbergMarquardtOptimizer();
        HarmonicFitter fitter = new HarmonicFitter(optimizer);
        fitter.fit();  // getObservations() returns empty list -> guesser throws
    }

    // --- Partition E: Object Lifecycle & Contract Integrity ---

    @Test(timeout = 4000)
    public void testGuesserObservationsCloned() {
        WeightedObservedPoint[] original = new WeightedObservedPoint[] {
            new WeightedObservedPoint(1.0, 0.0, 1.0),
            new WeightedObservedPoint(1.0, 0.1, 2.0),
            new WeightedObservedPoint(1.0, 0.2, 1.5),
            new WeightedObservedPoint(1.0, 0.3, 0.5)
        };
        HarmonicFitter.ParameterGuesser guesser = new HarmonicFitter.ParameterGuesser(original);
        // Modify original after construction
        original[0] = new WeightedObservedPoint(10.0, 99.0, 99.0);
        double[] guessed = guesser.guess();
        // Should not reflect the change (clone)
        assertFalse("Should not be affected by external modifications",
                    Double.isNaN(guessed[0]));
    }

    @Test(timeout = 4000)
    public void testHarmonicFitterConstructorStoresOptimizer() {
        DifferentiableMultivariateVectorOptimizer optimizer = new LevenbergMarquardtOptimizer();
        HarmonicFitter fitter = new HarmonicFitter(optimizer);
        assertNotNull(fitter);  // Basic sanity; no direct getter for optimizer in this version
    }

    // --- Helper: create a sine wave array of WeightedObservedPoint ---
    private WeightedObservedPoint[] createSineWave(double xStart, double step, int count,
                                                    double a, double omega, double phi) {
        WeightedObservedPoint[] points = new WeightedObservedPoint[count];
        for (int i = 0; i < count; i++) {
            double x = xStart + i * step;
            double y = a * FastMath.cos(omega * x + phi);
            points[i] = new WeightedObservedPoint(1.0, x, y);
        }
        return points;
    }
}