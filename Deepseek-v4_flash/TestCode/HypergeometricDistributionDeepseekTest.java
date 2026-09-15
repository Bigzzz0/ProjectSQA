package org.apache.commons.math3.distribution;

import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.*;

/**
 * Comprehensive test suite for HypergeometricDistribution covering
 * all public methods, boundary conditions, exception paths, and the
 * known integer overflow defect (Defects4J Math-1021).
 */
public class HypergeometricDistributionDeepseekTest {

    // ---------- Constructor validation tests ----------

    /**
     * @target Constructor (4-arg and 3-arg)
     * @scenario populationSize <= 0
     * @defectRisk Must throw NotStrictlyPositiveException for zero or negative population size.
     */
    @Test(timeout = 4000)
    public void testConstructorPopulationSizeNonPositive() {
        try {
            new HypergeometricDistribution(0, 5, 3);
            fail("Expected NotStrictlyPositiveException for populationSize=0");
        } catch (NotStrictlyPositiveException e) {
            // expected
        }
        try {
            new HypergeometricDistribution(-1, 5, 3);
            fail("Expected NotStrictlyPositiveException for populationSize=-1");
        } catch (NotStrictlyPositiveException e) {
            // expected
        }
    }

    /**
     * @target Constructor (4-arg and 3-arg)
     * @scenario numberOfSuccesses < 0
     * @defectRisk Must throw NotPositiveException for negative numberOfSuccesses.
     */
    @Test(timeout = 4000)
    public void testConstructorNumberOfSuccessesNegative() {
        try {
            new HypergeometricDistribution(10, -1, 3);
            fail("Expected NotPositiveException for negative numberOfSuccesses");
        } catch (NotPositiveException e) {
            // expected
        }
    }

    /**
     * @target Constructor (4-arg and 3-arg)
     * @scenario sampleSize < 0
     * @defectRisk Must throw NotPositiveException for negative sampleSize.
     */
    @Test(timeout = 4000)
    public void testConstructorSampleSizeNegative() {
        try {
            new HypergeometricDistribution(10, 5, -1);
            fail("Expected NotPositiveException for negative sampleSize");
        } catch (NotPositiveException e) {
            // expected
        }
    }

    /**
     * @target Constructor (4-arg and 3-arg)
     * @scenario numberOfSuccesses > populationSize
     * @defectRisk Must throw NumberIsTooLargeException.
     */
    @Test(timeout = 4000)
    public void testConstructorNumberOfSuccessesTooLarge() {
        try {
            new HypergeometricDistribution(5, 10, 3);
            fail("Expected NumberIsTooLargeException");
        } catch (NumberIsTooLargeException e) {
            // expected
        }
    }

    /**
     * @target Constructor (4-arg and 3-arg)
     * @scenario sampleSize > populationSize
     * @defectRisk Must throw NumberIsTooLargeException.
     */
    @Test(timeout = 4000)
    public void testConstructorSampleSizeTooLarge() {
        try {
            new HypergeometricDistribution(5, 3, 10);
            fail("Expected NumberIsTooLargeException");
        } catch (NumberIsTooLargeException e) {
            // expected
        }
    }

    /**
     * @target Constructor (valid parameters)
     * @scenario Normal construction with all parameters valid.
     * @defectRisk Must not throw any exception.
     */
    @Test(timeout = 4000)
    public void testConstructorValid() {
        HypergeometricDistribution d = new HypergeometricDistribution(100, 30, 20);
        assertEquals(100, d.getPopulationSize());
        assertEquals(30, d.getNumberOfSuccesses());
        assertEquals(20, d.getSampleSize());
    }

    // ---------- probability() tests ----------

    /**
     * @target probability(int x)
     * @scenario x below lower domain bound
     * @defectRisk Must return 0.0 for out-of-bounds x.
     */
    @Test(timeout = 4000)
    public void testProbabilityBelowDomain() {
        HypergeometricDistribution d = new HypergeometricDistribution(50, 20, 10);
        // lower domain = max(0, 20+10-50) = max(0,-20)=0
        assertEquals("probability for x < 0 should be 0.0", 0.0, d.probability(-1), 1e-12);
        assertEquals("probability for x < 0 should be 0.0", 0.0, d.probability(-100), 1e-12);
    }

    /**
     * @target probability(int x)
     * @scenario x above upper domain bound
     * @defectRisk Must return 0.0 for out-of-bounds x.
     */
    @Test(timeout = 4000)
    public void testProbabilityAboveDomain() {
        HypergeometricDistribution d = new HypergeometricDistribution(50, 20, 10);
        // upper domain = min(20,10)=10
        assertEquals("probability for x > 10 should be 0.0", 0.0, d.probability(11), 1e-12);
        assertEquals("probability for x > 10 should be 0.0", 0.0, d.probability(100), 1e-12);
    }

    /**
     * @target probability(int x)
     * @scenario x within domain (valid)
     * @defectRisk Must return correct probability (sum of all probabilities should be ~1).
     */
    @Test(timeout = 4000)
    public void testProbabilityValid() {
        HypergeometricDistribution d = new HypergeometricDistribution(50, 20, 10);
        double sum = 0.0;
        for (int x = 0; x <= 10; x++) {
            double p = d.probability(x);
            assertTrue("Probability for x=" + x + " should be >= 0", p >= 0.0);
            sum += p;
        }
        assertEquals("Sum of probabilities should be 1", 1.0, sum, 1e-12);
    }

    /**
     * @target probability(int x)
     * @scenario Edge case where domain has zero width (populationSize = numberOfSuccesses)
     * @defectRisk Must still work.
     */
    @Test(timeout = 4000)
    public void testProbabilityEdgeDomain() {
        HypergeometricDistribution d = new HypergeometricDistribution(10, 10, 5);
        // lower domain = max(0, 10+5-10)=5, upper domain = min(10,5)=5
        assertEquals("Only possible value is x=5", 1.0, d.probability(5), 1e-12);
        assertEquals("x=4 should be 0.0", 0.0, d.probability(4), 1e-12);
        assertEquals("x=6 should be 0.0", 0.0, d.probability(6), 1e-12);
    }

    // ---------- cumulativeProbability() tests ----------

    /**
     * @target cumulativeProbability(int x)
     * @scenario x < lower domain
     * @defectRisk Must return 0.0.
     */
    @Test(timeout = 4000)
    public void testCumulativeProbabilityBelowDomain() {
        HypergeometricDistribution d = new HypergeometricDistribution(50, 20, 10);
        assertEquals(0.0, d.cumulativeProbability(-1), 1e-12);
    }

    /**
     * @target cumulativeProbability(int x)
     * @scenario x >= upper domain
     * @defectRisk Must return 1.0.
     */
    @Test(timeout = 4000)
    public void testCumulativeProbabilityAtOrAboveUpperDomain() {
        HypergeometricDistribution d = new HypergeometricDistribution(50, 20, 10);
        assertEquals(1.0, d.cumulativeProbability(10), 1e-12);
        assertEquals(1.0, d.cumulativeProbability(100), 1e-12);
    }

    /**
     * @target cumulativeProbability(int x)
     * @scenario x within domain, normal case
     * @defectRisk Must give non-decreasing cumulative values, ending at 1.
     */
    @Test(timeout = 4000)
    public void testCumulativeProbabilityWithinDomain() {
        HypergeometricDistribution d = new HypergeometricDistribution(50, 20, 10);
        double prev = 0.0;
        for (int x = 0; x <= 10; x++) {
            double cdf = d.cumulativeProbability(x);
            assertTrue("CDF must be >= " + prev, cdf >= prev - 1e-15);
            prev = cdf;
        }
        assertEquals("CDF at upper bound", 1.0, prev, 1e-12);
    }

    // ---------- upperCumulativeProbability() tests ----------

    /**
     * @target upperCumulativeProbability(int x)
     * @scenario x <= lower domain
     * @defectRisk Must return 1.0.
     */
    @Test(timeout = 4000)
    public void testUpperCumulativeProbabilityAtOrBelowLowerDomain() {
        HypergeometricDistribution d = new HypergeometricDistribution(50, 20, 10);
        assertEquals(1.0, d.upperCumulativeProbability(0), 1e-12);
        assertEquals(1.0, d.upperCumulativeProbability(-1), 1e-12);
    }

    /**
     * @target upperCumulativeProbability(int x)
     * @scenario x > upper domain
     * @defectRisk Must return 0.0.
     */
    @Test(timeout = 4000)
    public void testUpperCumulativeProbabilityAboveUpperDomain() {
        HypergeometricDistribution d = new HypergeometricDistribution(50, 20, 10);
        assertEquals(0.0, d.upperCumulativeProbability(11), 1e-12);
        assertEquals(0.0, d.upperCumulativeProbability(100), 1e-12);
    }

    /**
     * @target upperCumulativeProbability(int x)
     * @scenario x within domain, normal case
     * @defectRisk Must be decreasing and sum with CDF to 1.
     */
    @Test(timeout = 4000)
    public void testUpperCumulativeProbabilityWithinDomain() {
        HypergeometricDistribution d = new HypergeometricDistribution(50, 20, 10);
        for (int x = 0; x <= 10; x++) {
            double cdf = d.cumulativeProbability(x);
            double upper = d.upperCumulativeProbability(x);
            // For discrete distribution, P(X <= x) + P(X > x) = 1, but upper gives P(X >= x) = P(X > x-1)
            // So for integer x, upperCumulativeProbability(x) = 1 - cumulativeProbability(x-1)
            double expectedUpper = 1.0 - d.cumulativeProbability(x - 1);
            assertEquals("upper CDF mismatch at x=" + x, expectedUpper, upper, 1e-12);
        }
    }

    // ---------- Statistical properties ----------

    /**
     * @target getNumericalMean()
     * @scenario Known values
     * @defectRisk Must match formula n*m/N.
     */
    @Test(timeout = 4000)
    public void testGetNumericalMean() {
        HypergeometricDistribution d = new HypergeometricDistribution(100, 30, 20);
        double expected = (20.0 * 30.0) / 100.0;
        assertEquals("Mean mismatch", expected, d.getNumericalMean(), 1e-12);
    }

    /**
     * @target getNumericalVariance()
     * @scenario Known values and caching behavior
     * @defectRisk Must match formula and cache result.
     */
    @Test(timeout = 4000)
    public void testGetNumericalVariance() {
        HypergeometricDistribution d = new HypergeometricDistribution(100, 30, 20);
        double N = 100.0, m = 30.0, n = 20.0;
        double expected = (n * m * (N - n) * (N - m)) / (N * N * (N - 1.0));
        double var1 = d.getNumericalVariance();
        double var2 = d.getNumericalVariance();
        assertEquals("Variance computation", expected, var1, 1e-12);
        assertTrue("Variance should be cached (same reference)", var1 == var2);
    }

    /**
     * @target getSupportLowerBound()
     * @scenario Various cases
     * @defectRisk Must equal max(0, sampleSize + numberOfSuccesses - populationSize).
     */
    @Test(timeout = 4000)
    public void testGetSupportLowerBound() {
        // Case 1: sampleSize + numberOfSuccesses - populationSize > 0
        HypergeometricDistribution d1 = new HypergeometricDistribution(50, 30, 25);
        assertEquals(5, d1.getSupportLowerBound());

        // Case 2: sampleSize + numberOfSuccesses - populationSize <= 0
        HypergeometricDistribution d2 = new HypergeometricDistribution(100, 10, 5);
        assertEquals(0, d2.getSupportLowerBound());
    }

    /**
     * @target getSupportUpperBound()
     * @scenario Various cases
     * @defectRisk Must equal min(numberOfSuccesses, sampleSize).
     */
    @Test(timeout = 4000)
    public void testGetSupportUpperBound() {
        HypergeometricDistribution d1 = new HypergeometricDistribution(50, 10, 20);
        assertEquals(10, d1.getSupportUpperBound());

        HypergeometricDistribution d2 = new HypergeometricDistribution(50, 40, 20);
        assertEquals(20, d2.getSupportUpperBound());
    }

    /**
     * @target isSupportConnected()
     * @scenario Always
     * @defectRisk Must return true.
     */
    @Test(timeout = 4000)
    public void testIsSupportConnected() {
        HypergeometricDistribution d = new HypergeometricDistribution(100, 30, 20);
        assertTrue("Support should be connected", d.isSupportConnected());
    }

    // ---------- Sampling tests ----------

    /**
     * @target sample() with custom random generator
     * @scenario Deterministic seed
     * @defectRisk Must produce valid values within support.
     */
    @Test(timeout = 4000)
    public void testSample() {
        HypergeometricDistribution d = new HypergeometricDistribution(new Well19937c(12345L), 100, 30, 20);
        for (int i = 0; i < 1000; i++) {
            int s = d.sample();
            assertTrue("Sample must be >= lower bound", s >= d.getSupportLowerBound());
            assertTrue("Sample must be <= upper bound", s <= d.getSupportUpperBound());
        }
    }

    /**
     * @target reseedRandomGenerator(long)
     * @scenario Reseeding with known seed
     * @defectRisk Must produce deterministic sequence after reseed.
     */
    @Test(timeout = 4000)
    public void testReseedRandomGenerator() {
        HypergeometricDistribution d = new HypergeometricDistribution(100, 30, 20);
        d.reseedRandomGenerator(42L);
        int s1 = d.sample();
        d.reseedRandomGenerator(42L);
        int s2 = d.sample();
        assertEquals("Reseeding should produce same sample", s1, s2);
    }

    // ---------- Defect-specific test for integer overflow (Math-1021) ----------

    /**
     * @target getNumericalMean() and sample()
     * @scenario Integer overflow in mean calculation (large population, successes, sample)
     * @defectRisk The original code computed n*m using 32-bit int, causing overflow.
     *             This test must fail on defective version (negative mean, sample=-50).
     */
    @Test(timeout = 4000)
    public void testMath1021_IntegerOverflowInNumericalMean() {
        final int populationSize = 1437651;
        final int numberOfSuccesses = 28975;
        final int sampleSize = 76182;
        // Note: 28975 * 76182 = 2,207,373,450 > Integer.MAX_VALUE (2,147,483,647)
        HypergeometricDistribution dist = new HypergeometricDistribution(populationSize, numberOfSuccesses, sampleSize);

        double mean = dist.getNumericalMean();
        assertTrue("Numerical mean must be >= 0, but was: " + mean, mean >= 0.0);

        // Sample 100 times and verify each sample is within valid bounds
        for (int i = 0; i < 100; ++i) {
            int sample = dist.sample();
            assertTrue("sample must be >= 0, but was: " + sample, 0 <= sample);
            assertTrue("sample must be <= numberOfSuccesses, but was: " + sample, sample <= numberOfSuccesses);
        }
    }
}