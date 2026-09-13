package org.apache.commons.math3.distribution;

import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.*;

/**
 * JUnit 4 test suite for {@link HypergeometricDistribution}.
 */
public class HypergeometricDistributionClaudeTest {

    private static final double DELTA = 1e-9;

    // ---------------------------------------------------------------
    // Constructor validation tests
    // ---------------------------------------------------------------

    /**
     * @target HypergeometricDistribution constructor
     * @scenario populationSize <= 0 (zero)
     * @defectRisk should throw NotStrictlyPositiveException
     */
    @Test(timeout = 4000)
    public void testConstructorPopulationSizeZeroThrows() {
        try {
            new HypergeometricDistribution(0, 0, 0);
            fail("Expected NotStrictlyPositiveException");
        } catch (NotStrictlyPositiveException e) {
            // expected
        }
    }

    /**
     * @target HypergeometricDistribution constructor
     * @scenario populationSize negative
     * @defectRisk should throw NotStrictlyPositiveException
     */
    @Test(timeout = 4000)
    public void testConstructorPopulationSizeNegativeThrows() {
        try {
            new HypergeometricDistribution(-5, 0, 0);
            fail("Expected NotStrictlyPositiveException");
        } catch (NotStrictlyPositiveException e) {
            // expected
        }
    }

    /**
     * @target HypergeometricDistribution constructor
     * @scenario numberOfSuccesses < 0
     * @defectRisk should throw NotPositiveException
     */
    @Test(timeout = 4000)
    public void testConstructorNumberOfSuccessesNegativeThrows() {
        try {
            new HypergeometricDistribution(10, -1, 5);
            fail("Expected NotPositiveException");
        } catch (NotPositiveException e) {
            // expected
        }
    }

    /**
     * @target HypergeometricDistribution constructor
     * @scenario sampleSize < 0
     * @defectRisk should throw NotPositiveException
     */
    @Test(timeout = 4000)
    public void testConstructorSampleSizeNegativeThrows() {
        try {
            new HypergeometricDistribution(10, 5, -1);
            fail("Expected NotPositiveException");
        } catch (NotPositiveException e) {
            // expected
        }
    }

    /**
     * @target HypergeometricDistribution constructor
     * @scenario numberOfSuccesses > populationSize
     * @defectRisk should throw NumberIsTooLargeException
     */
    @Test(timeout = 4000)
    public void testConstructorNumberOfSuccessesTooLargeThrows() {
        try {
            new HypergeometricDistribution(10, 11, 5);
            fail("Expected NumberIsTooLargeException");
        } catch (NumberIsTooLargeException e) {
            // expected
        }
    }

    /**
     * @target HypergeometricDistribution constructor
     * @scenario sampleSize > populationSize
     * @defectRisk should throw NumberIsTooLargeException
     */
    @Test(timeout = 4000)
    public void testConstructorSampleSizeTooLargeThrows() {
        try {
            new HypergeometricDistribution(10, 5, 11);
            fail("Expected NumberIsTooLargeException");
        } catch (NumberIsTooLargeException e) {
            // expected
        }
    }

    /**
     * @target HypergeometricDistribution constructor with valid params
     * @scenario valid parameters must construct successfully and store values correctly
     * @defectRisk incorrect field assignment
     */
    @Test(timeout = 4000)
    public void testConstructorValidParameters() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        assertEquals(50, dist.getPopulationSize());
        assertEquals(15, dist.getNumberOfSuccesses());
        assertEquals(20, dist.getSampleSize());
    }

    /**
     * @target HypergeometricDistribution(RandomGenerator, int, int, int) constructor
     * @scenario constructing with custom RandomGenerator and valid parameters
     * @defectRisk incorrect RNG wiring
     */
    @Test(timeout = 4000)
    public void testConstructorWithCustomRandomGenerator() {
        RandomGenerator rng = new Well19937c(1234L);
        HypergeometricDistribution dist =
            new HypergeometricDistribution(rng, 50, 15, 20);
        assertEquals(50, dist.getPopulationSize());
        assertEquals(15, dist.getNumberOfSuccesses());
        assertEquals(20, dist.getSampleSize());
    }

    // ---------------------------------------------------------------
    // probability(x) tests
    // ---------------------------------------------------------------

    /**
     * @target probability(int x)
     * @scenario x below lowerDomain returns 0.0
     * @defectRisk boundary check for lower domain
     */
    @Test(timeout = 4000)
    public void testProbabilityBelowLowerDomain() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        // lowerDomain = max(0, 15-(50-20)) = 0
        assertEquals(0.0, dist.probability(-1), DELTA);
    }

    /**
     * @target probability(int x)
     * @scenario x above upperDomain returns 0.0
     * @defectRisk boundary check for upper domain
     */
    @Test(timeout = 4000)
    public void testProbabilityAboveUpperDomain() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        // upperDomain = min(20,15) = 15
        assertEquals(0.0, dist.probability(16), DELTA);
        assertEquals(0.0, dist.probability(100), DELTA);
    }

    /**
     * @target probability(int x)
     * @scenario valid x within domain returns non-zero probability in [0,1]
     * @defectRisk incorrect saddle point expansion computation
     */
    @Test(timeout = 4000)
    public void testProbabilityWithinDomain() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        for (int x = 0; x <= 15; x++) {
            double p = dist.probability(x);
            assertTrue("probability at x=" + x + " out of range: " + p,
                       p >= 0.0 && p <= 1.0 + DELTA);
        }
    }

    /**
     * @target probability(int x)
     * @scenario sum of all probability values over domain approx equals 1.0
     * @defectRisk incorrect normalization
     */
    @Test(timeout = 4000)
    public void testProbabilitySumEqualsOne() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        double sum = 0.0;
        for (int x = 0; x <= 15; x++) {
            sum += dist.probability(x);
        }
        assertEquals(1.0, sum, 1e-6);
    }

    // ---------------------------------------------------------------
    // cumulativeProbability(x) tests
    // ---------------------------------------------------------------

    /**
     * @target cumulativeProbability(int x)
     * @scenario x below lowerDomain returns 0.0
     * @defectRisk boundary branch for lower domain
     */
    @Test(timeout = 4000)
    public void testCumulativeProbabilityBelowLowerDomain() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        assertEquals(0.0, dist.cumulativeProbability(-1), DELTA);
    }

    /**
     * @target cumulativeProbability(int x)
     * @scenario x equal to and greater than upperDomain returns 1.0
     * @defectRisk boundary branch for upper domain (>=)
     */
    @Test(timeout = 4000)
    public void testCumulativeProbabilityAtAndAboveUpperDomain() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        assertEquals(1.0, dist.cumulativeProbability(15), DELTA);
        assertEquals(1.0, dist.cumulativeProbability(20), DELTA);
        assertEquals(1.0, dist.cumulativeProbability(100), DELTA);
    }

    /**
     * @target cumulativeProbability(int x)
     * @scenario x strictly inside domain triggers inner summation loop
     * @defectRisk incorrect summation direction/logic in innerCumulativeProbability
     */
    @Test(timeout = 4000)
    public void testCumulativeProbabilityInsideDomain() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        double cdf5 = dist.cumulativeProbability(5);

        double manualSum = 0.0;
        for (int x = 0; x <= 5; x++) {
            manualSum += dist.probability(x);
        }
        assertEquals(manualSum, cdf5, DELTA);
        assertTrue(cdf5 > 0.0 && cdf5 < 1.0);
    }

    /**
     * @target cumulativeProbability(int x)
     * @scenario x exactly at lowerDomain, exercising trivial single-point inner loop
     * @defectRisk off-by-one in initial ret assignment inside innerCumulativeProbability
     */
    @Test(timeout = 4000)
    public void testCumulativeProbabilityAtLowerDomain() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        double cdf0 = dist.cumulativeProbability(0);
        assertEquals(dist.probability(0), cdf0, DELTA);
    }

    // ---------------------------------------------------------------
    // upperCumulativeProbability(x) tests
    // ---------------------------------------------------------------

    /**
     * @target upperCumulativeProbability(int x)
     * @scenario x <= lowerDomain returns 1.0
     * @defectRisk boundary branch (<=)
     */
    @Test(timeout = 4000)
    public void testUpperCumulativeProbabilityAtAndBelowLowerDomain() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        assertEquals(1.0, dist.upperCumulativeProbability(0), DELTA);
        assertEquals(1.0, dist.upperCumulativeProbability(-5), DELTA);
    }

    /**
     * @target upperCumulativeProbability(int x)
     * @scenario x > upperDomain returns 0.0
     * @defectRisk boundary branch for upper domain
     */
    @Test(timeout = 4000)
    public void testUpperCumulativeProbabilityAboveUpperDomain() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        assertEquals(0.0, dist.upperCumulativeProbability(16), DELTA);
        assertEquals(0.0, dist.upperCumulativeProbability(100), DELTA);
    }

    /**
     * @target upperCumulativeProbability(int x)
     * @scenario x strictly inside domain triggers inner summation loop (descending direction)
     * @defectRisk incorrect dx direction (-1) computation
     */
    @Test(timeout = 4000)
    public void testUpperCumulativeProbabilityInsideDomain() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        double ucdf5 = dist.upperCumulativeProbability(5);

        double manualSum = 0.0;
        for (int x = 5; x <= 15; x++) {
            manualSum += dist.probability(x);
        }
        assertEquals(manualSum, ucdf5, DELTA);
        assertTrue(ucdf5 > 0.0 && ucdf5 < 1.0);
    }

    /**
     * @target upperCumulativeProbability(int x)
     * @scenario x exactly at upperDomain triggers trivial single-point inner loop
     * @defectRisk off-by-one in initial ret assignment inside innerCumulativeProbability
     */
    @Test(timeout = 4000)
    public void testUpperCumulativeProbabilityAtUpperDomain() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        double ucdf15 = dist.upperCumulativeProbability(15);
        assertEquals(dist.probability(15), ucdf15, DELTA);
    }

    /**
     * @target cumulativeProbability & upperCumulativeProbability consistency
     * @scenario CDF(x) + upperCDF(x+1) should equal 1.0 for x within domain
     * @defectRisk inconsistent summation logic between the two cumulative methods
     */
    @Test(timeout = 4000)
    public void testCumulativeAndUpperCumulativeConsistency() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        for (int x = 0; x < 15; x++) {
            double sum = dist.cumulativeProbability(x) + dist.upperCumulativeProbability(x + 1);
            assertEquals("Inconsistent at x=" + x, 1.0, sum, 1e-6);
        }
    }

    // ---------------------------------------------------------------
    // Statistical properties
    // ---------------------------------------------------------------

    /**
     * @target getNumericalMean()
     * @scenario standard valid parameters
     * @defectRisk incorrect mean formula n*m/N
     */
    @Test(timeout = 4000)
    public void testGetNumericalMean() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        assertEquals(6.0, dist.getNumericalMean(), DELTA);
    }

    /**
     * @target getNumericalVariance()
     * @scenario standard valid parameters, first call computes and caches
     * @defectRisk incorrect variance formula
     */
    @Test(timeout = 4000)
    public void testGetNumericalVariance() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        double expected = (20.0 * 15.0 * (50.0 - 20.0) * (50.0 - 15.0))
                           / (50.0 * 50.0 * (50.0 - 1.0));
        assertEquals(expected, dist.getNumericalVariance(), 1e-9);
    }

    /**
     * @target getNumericalVariance() caching behavior
     * @scenario call getNumericalVariance() multiple times, verifying cached value stays consistent
     * @defectRisk caching flag not properly toggling, or recomputation discrepancy
     */
    @Test(timeout = 4000)
    public void testGetNumericalVarianceCaching() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        double firstCall = dist.getNumericalVariance();
        double secondCall = dist.getNumericalVariance();
        assertEquals(firstCall, secondCall, DELTA);
    }

    /**
     * @target getSupportLowerBound()
     * @scenario standard valid parameters where n+m-N is negative -> bound is 0
     * @defectRisk incorrect max(0, ...) computation
     */
    @Test(timeout = 4000)
    public void testGetSupportLowerBoundZeroCase() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        assertEquals(0, dist.getSupportLowerBound());
    }

    /**
     * @target getSupportLowerBound()
     * @scenario parameters where n+m-N is positive -> bound is n+m-N
     * @defectRisk incorrect max(0, ...) computation for positive branch
     */
    @Test(timeout = 4000)
    public void testGetSupportLowerBoundPositiveCase() {
        // N=10, m=8, n=9 => n+m-N = 7
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 8, 9);
        assertEquals(7, dist.getSupportLowerBound());
    }

    /**
     * @target getSupportUpperBound()
     * @scenario standard valid parameters, min(m, n)
     * @defectRisk incorrect min(...) computation
     */
    @Test(timeout = 4000)
    public void testGetSupportUpperBound() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        assertEquals(15, dist.getSupportUpperBound());

        HypergeometricDistribution dist2 = new HypergeometricDistribution(50, 25, 10);
        assertEquals(10, dist2.getSupportUpperBound());
    }

    /**
     * @target isSupportConnected()
     * @scenario always returns true
     * @defectRisk hardcoded return value regression
     */
    @Test(timeout = 4000)
    public void testIsSupportConnected() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        assertTrue(dist.isSupportConnected());
    }

    // ---------------------------------------------------------------
    // Sampling tests
    // ---------------------------------------------------------------

    /**
     * @target sample() with custom RandomGenerator
     * @scenario repeated sampling produces values within valid support bounds
     * @defectRisk sample generation algorithm producing out-of-range values
     */
    @Test(timeout = 4000)
    public void testSampleWithCustomRandomGenerator() {
        RandomGenerator rng = new Well19937c(42L);
        HypergeometricDistribution dist = new HypergeometricDistribution(rng, 50, 15, 20);
        int lower = dist.getSupportLowerBound();
        int upper = dist.getSupportUpperBound();
        for (int i = 0; i < 50; i++) {
            int sample = dist.sample();
            assertTrue("sample out of lower bound: " + sample, sample >= lower);
            assertTrue("sample out of upper bound: " + sample, sample <= upper);
        }
    }

    /**
     * @target reseedRandomGenerator(long)
     * @scenario reseeding with the same seed twice should produce identical sample sequences
     * @defectRisk reseeding logic not properly propagated to underlying RNG
     */
    @Test(timeout = 4000)
    public void testReseedRandomGeneratorReproducibility() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);

        dist.reseedRandomGenerator(123456789L);
        int[] firstRun = new int[20];
        for (int i = 0; i < 20; i++) {
            firstRun[i] = dist.sample();
        }

        dist.reseedRandomGenerator(123456789L);
        int[] secondRun = new int[20];
        for (int i = 0; i < 20; i++) {
            secondRun[i] = dist.sample();
        }

        Assert.assertArrayEquals(firstRun, secondRun);
    }

    /**
     * @target sample(int sampleSize) / sample() array generation via inherited method
     * @scenario generating multiple samples via sample(int) verifies all values within domain
     * @defectRisk underlying sample() called repeatedly producing invalid values
     */
    @Test(timeout = 4000)
    public void testMultipleSampleArray() {
        HypergeometricDistribution dist = new HypergeometricDistribution(50, 15, 20);
        int[] samples = dist.sample(30);
        assertEquals(30, samples.length);
        for (int s : samples) {
            assertTrue(s >= dist.getSupportLowerBound());
            assertTrue(s <= dist.getSupportUpperBound());
        }
    }

    // ---------------------------------------------------------------
    // Additional edge-case parameter combinations
    // ---------------------------------------------------------------

    /**
     * @target Full lifecycle with minimal valid parameters (populationSize = 1)
     * @scenario smallest possible valid distribution
     * @defectRisk edge-case division by (N-1) = 0 in variance formula
     */
    @Test(timeout = 4000)
    public void testMinimalPopulationSizeVariance() {
        HypergeometricDistribution dist = new HypergeometricDistribution(1, 1, 1);
        // N=1 leads to division by zero (N-1=0) in variance -> NaN or Infinite expected
        double variance = dist.getNumericalVariance();
        assertTrue(Double.isNaN(variance) || Double.isInfinite(variance) || variance == 0.0);
    }

    /**
     * @target probability(), cumulativeProbability(), upperCumulativeProbability() with numberOfSuccesses = 0
     * @scenario boundary case where numberOfSuccesses is zero, domain collapses to single point 0
     * @defectRisk incorrect domain calculation for degenerate distribution
     */
    @Test(timeout = 4000)
    public void testDegenerateDistributionZeroSuccesses() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 0, 5);
        assertEquals(0, dist.getSupportLowerBound());
        assertEquals(0, dist.getSupportUpperBound());
        assertEquals(1.0, dist.probability(0), DELTA);
        assertEquals(1.0, dist.cumulativeProbability(0), DELTA);
        assertEquals(1.0, dist.upperCumulativeProbability(0), DELTA);
        assertEquals(0.0, dist.probability(1), DELTA);
    }

    /**
     * @target probability(), domain when sampleSize equals populationSize
     * @scenario sampleSize == populationSize forces all successes to be picked
     * @defectRisk incorrect lowerDomain computation when (n - k) = 0
     */
    @Test(timeout = 4000)
    public void testSampleSizeEqualsPopulationSize() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 4, 10);
        // lowerDomain = max(0, 4 - (10-10)) = 4
        // upperDomain = min(10, 4) = 4
        assertEquals(4, dist.getSupportLowerBound());
        assertEquals(4, dist.getSupportUpperBound());
        assertEquals(1.0, dist.probability(4), DELTA);
        assertEquals(0.0, dist.probability(3), DELTA);
    }

    // ---------------------------------------------------------------
    // KNOWN DEFECT TEST: MATH-1021 Integer Overflow
    // ---------------------------------------------------------------

    /**
     * @target getNumericalMean() and sample() - integer overflow defect (MATH-1021)
     * @scenario Large population/sample/success parameters cause 32-bit integer overflow
     * in intermediate computation (sampleSize * numberOfSuccesses), producing a negative
     * mean and consequently invalid (negative) sample values.
     * @defectRisk On the defective version, getSampleSize() * getNumberOfSuccesses() overflows
     * a 32-bit int (28975 * 76182 = 2,207,373,450 > Integer.MAX_VALUE), causing getNumericalMean()
     * to be negative, and the sample() algorithm to return negative values (e.g. sample=-50).
     * On a fixed version this computation should use long arithmetic (or otherwise avoid overflow),
     * so the mean is non-negative and every sampled value lies within [0, numberOfSuccesses].
     */
    @Test(timeout = 4000)
    public void testMath1021_IntegerOverflowInNumericalMean() {
        final int populationSize = 1437651;
        final int numberOfSuccesses = 28975;
        final int sampleSize = 76182;
        // Note: 28975 * 76182 = 2,207,373,450 > Integer.MAX_VALUE (2,147,483,647)

        HypergeometricDistribution dist =
            new HypergeometricDistribution(populationSize, numberOfSuccesses, sampleSize);

        double mean = dist.getNumericalMean();
        assertTrue("Numerical mean must be >= 0, but was: " + mean, mean >= 0.0);

        for (int i = 0; i < 100; ++i) {
            int sample = dist.sample();
            assertTrue("sample must be >= 0, but was: " + sample, 0 <= sample);
            assertTrue("sample must be <= numberOfSuccesses, but was: " + sample,
                       sample <= numberOfSuccesses);
        }
    }

    /**
     * @target getNumericalVariance() - integer overflow defect context (MATH-1021)
     * @scenario Same large parameters as testMath1021, verifying variance is also
     * a sane non-negative finite number despite large intermediate products.
     * @defectRisk Overflow in intermediate multiplication within variance formula
     * could yield negative or NaN variance on the defective implementation.
     */
    @Test(timeout = 4000)
    public void testMath1021_NumericalVarianceSanity() {
        final int populationSize = 1437651;
        final int numberOfSuccesses = 28975;
        final int sampleSize = 76182;

        HypergeometricDistribution dist =
            new HypergeometricDistribution(populationSize, numberOfSuccesses, sampleSize);

        double variance = dist.getNumericalVariance();
        assertFalse("Variance should not be NaN", Double.isNaN(variance));
        assertTrue("Variance must be >= 0, but was: " + variance, variance >= 0.0);
    }

    /**
     * @target getSupportLowerBound()/getSupportUpperBound() - large-parameter sanity check (MATH-1021 context)
     * @scenario Same large parameters, ensuring the support bounds themselves remain
     * within valid non-negative int range (no overflow expected here since these use max/min
     * on ints directly, but verifies no unexpected regression side effects).
     * @defectRisk Regression in support bound calculation combined with overflow fix.
     */
    @Test(timeout = 4000)
    public void testMath1021_SupportBoundsSanity() {
        final int populationSize = 1437651;
        final int numberOfSuccesses = 28975;
        final int sampleSize = 76182;

        HypergeometricDistribution dist =
            new HypergeometricDistribution(populationSize, numberOfSuccesses, sampleSize);

        int lower = dist.getSupportLowerBound();
        int upper = dist.getSupportUpperBound();

        assertTrue("Lower bound must be >= 0, but was: " + lower, lower >= 0);
        assertTrue("Upper bound must be <= numberOfSuccesses, but was: " + upper,
                   upper <= numberOfSuccesses);
        assertTrue("Lower bound must be <= upper bound", lower <= upper);
    }
}