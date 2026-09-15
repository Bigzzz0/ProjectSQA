package org.apache.commons.math3.distribution;

import org.apache.commons.math3.distribution.HypergeometricDistribution;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;
import org.junit.Assert;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 *
 * Target Class: HypergeometricDistribution
 * Defects4J / Bug Tracker ID: MATH-1021 (Defects4J Math-2)
 *
 * Branch & Condition Coverage Matrix:
 * 1. Constructor Validation:
 *    - populationSize <= 0 [populationSize = 0, populationSize = -10] -> NotStrictlyPositiveException
 *    - numberOfSuccesses < 0 [numberOfSuccesses = -1] -> NotPositiveException
 *    - sampleSize < 0 [sampleSize = -1] -> NotPositiveException
 *    - numberOfSuccesses > populationSize [m > N] -> NumberIsTooLargeException
 *    - sampleSize > populationSize [n > N] -> NumberIsTooLargeException
 *    - RandomGenerator overload: custom Well19937c seeded generator.
 *
 * 2. Domain & Bounds:
 *    - getLowerDomain: FastMath.max(0, m - (n - k))
 *      - Case 1: m - (N - k) <= 0 -> lowerDomain = 0
 *      - Case 2: m - (N - k) > 0  -> lowerDomain = m + k - N
 *    - getUpperDomain: FastMath.min(k, m)
 *      - Case 1: k < m -> upperDomain = k
 *      - Case 2: m <= k -> upperDomain = m
 *    - getSupportLowerBound() == max(0, sampleSize + numberOfSuccesses - populationSize)
 *    - getSupportUpperBound() == min(numberOfSuccesses, sampleSize)
 *    - isSupportConnected() == true
 *
 * 3. Probability Mass Function (probability):
 *    - x < domain[0] -> returns 0.0
 *    - x > domain[1] -> returns 0.0
 *    - domain[0] <= x <= domain[1] -> SaddlePointExpansion computation
 *
 * 4. Cumulative Probability (cumulativeProbability & upperCumulativeProbability):
 *    - cumulativeProbability:
 *      - x < domain[0] -> returns 0.0
 *      - x >= domain[1] -> returns 1.0
 *      - domain[0] <= x < domain[1] -> innerCumulativeProbability(domain[0], x, 1)
 *    - upperCumulativeProbability:
 *      - x <= domain[0] -> returns 1.0
 *      - x > domain[1] -> returns 0.0
 *      - domain[0] < x <= domain[1] -> innerCumulativeProbability(domain[1], x, -1)
 *
 * 5. Statistical Moments & Latent Defects:
 *    - getNumericalMean: (sampleSize * numberOfSuccesses) / populationSize.
 *      CRITICAL DEFECT (MATH-1021): sampleSize * numberOfSuccesses can overflow 32-bit signed
 *      integer when sampleSize * numberOfSuccesses > 2^31 - 1, producing negative values for mean,
 *      which corrupts inverse cumulative probability during sampling (yielding negative samples).
 *    - getNumericalVariance: calculates variance and verifies caching mechanism.
 */
public class HypergeometricDistributionGeminiTest {

    private static final double EPSILON = 1e-12;

    // =========================================================================
    // Partition A: Parameter Validation & Edge Construction
    // =========================================================================

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructor_PopulationSizeZero() {
        new HypergeometricDistribution(0, 0, 0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructor_PopulationSizeNegative() {
        new HypergeometricDistribution(-5, 0, 0);
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testConstructor_NumberOfSuccessesNegative() {
        new HypergeometricDistribution(10, -1, 5);
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testConstructor_SampleSizeNegative() {
        new HypergeometricDistribution(10, 5, -1);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testConstructor_SuccessesLargerThanPopulation() {
        new HypergeometricDistribution(10, 11, 5);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testConstructor_SampleSizeLargerThanPopulation() {
        new HypergeometricDistribution(10, 5, 11);
    }

    @Test(timeout = 4000)
    public void testConstructor_CustomRngAndGetters() {
        Well19937c rng = new Well19937c(42L);
        HypergeometricDistribution dist = new HypergeometricDistribution(rng, 100, 30, 20);

        assertEquals(100, dist.getPopulationSize());
        assertEquals(30, dist.getNumberOfSuccesses());
        assertEquals(20, dist.getSampleSize());
        assertTrue(dist.isSupportConnected());
    }

    // =========================================================================
    // Partition B: Probability Mass Function & Support Bounds
    // =========================================================================

    @Test(timeout = 4000)
    public void testDomainAndBounds_ZeroLowerBound() {
        // N = 10, m = 3, n = 4 -> lower = max(0, 3 - (10 - 4)) = max(0, -3) = 0
        // upper = min(4, 3) = 3
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 3, 4);

        assertEquals(0, dist.getSupportLowerBound());
        assertEquals(3, dist.getSupportUpperBound());
    }

    @Test(timeout = 4000)
    public void testDomainAndBounds_PositiveLowerBound() {
        // N = 10, m = 8, n = 7 -> lower = max(0, 8 - (10 - 7)) = max(0, 5) = 5
        // upper = min(7, 8) = 7
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 8, 7);

        assertEquals(5, dist.getSupportLowerBound());
        assertEquals(7, dist.getSupportUpperBound());
    }

    @Test(timeout = 4000)
    public void testProbability_OutOfBounds() {
        // Domain is [1, 4] for N=10, m=7, n=7 -> lower = 7 - 3 = 4, upper = 7
        // Let's use N=10, m=6, n=6 -> lower = max(0, 6 - 4) = 2, upper = 6
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 6, 6);

        assertEquals(0.0, dist.probability(1), EPSILON); // below lowerDomain (2)
        assertEquals(0.0, dist.probability(-5), EPSILON); // far below
        assertEquals(0.0, dist.probability(7), EPSILON); // above upperDomain (6)
        assertEquals(0.0, dist.probability(15), EPSILON); // far above
    }

    @Test(timeout = 4000)
    public void testProbability_ExactValues() {
        // Simple case: N = 5, m = 2, n = 3
        // Combinations: C(5, 3) = 10
        // P(X = 0) = C(2, 0)*C(3, 3) / 10 = 1 * 1 / 10 = 0.1
        // P(X = 1) = C(2, 1)*C(3, 2) / 10 = 2 * 3 / 10 = 0.6
        // P(X = 2) = C(2, 2)*C(3, 1) / 10 = 1 * 3 / 10 = 0.3
        HypergeometricDistribution dist = new HypergeometricDistribution(5, 2, 3);

        assertEquals(0.1, dist.probability(0), EPSILON);
        assertEquals(0.6, dist.probability(1), EPSILON);
        assertEquals(0.3, dist.probability(2), EPSILON);
    }

    @Test(timeout = 4000)
    public void testDegenerateDistribution_ZeroSuccesses() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 0, 5);

        assertEquals(0, dist.getSupportLowerBound());
        assertEquals(0, dist.getSupportUpperBound());
        assertEquals(1.0, dist.probability(0), EPSILON);
        assertEquals(0.0, dist.probability(1), EPSILON);
    }

    @Test(timeout = 4000)
    public void testDegenerateDistribution_ZeroSamples() {
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 5, 0);

        assertEquals(0, dist.getSupportLowerBound());
        assertEquals(0, dist.getSupportUpperBound());
        assertEquals(1.0, dist.probability(0), EPSILON);
        assertEquals(0.0, dist.probability(1), EPSILON);
    }

    // =========================================================================
    // Partition C: Cumulative & Upper Cumulative Probabilities
    // =========================================================================

    @Test(timeout = 4000)
    public void testCumulativeProbability_BoundsAndBranches() {
        // N = 10, m = 8, n = 7. Domain: [5, 7]
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 8, 7);

        // x < lowerDomain -> 0.0
        assertEquals(0.0, dist.cumulativeProbability(4), EPSILON);
        assertEquals(0.0, dist.cumulativeProbability(0), EPSILON);

        // x >= upperDomain -> 1.0
        assertEquals(1.0, dist.cumulativeProbability(7), EPSILON);
        assertEquals(1.0, dist.cumulativeProbability(8), EPSILON);

        // Intermediate values (innerCumulativeProbability with dx = 1)
        double p5 = dist.probability(5);
        double p6 = dist.probability(6);
        assertEquals(p5, dist.cumulativeProbability(5), EPSILON);
        assertEquals(p5 + p6, dist.cumulativeProbability(6), EPSILON);
    }

    @Test(timeout = 4000)
    public void testUpperCumulativeProbability_BoundsAndBranches() {
        // N = 10, m = 8, n = 7. Domain: [5, 7]
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 8, 7);

        // x <= lowerDomain -> 1.0
        assertEquals(1.0, dist.upperCumulativeProbability(5), EPSILON);
        assertEquals(1.0, dist.upperCumulativeProbability(4), EPSILON);
        assertEquals(1.0, dist.upperCumulativeProbability(0), EPSILON);

        // x > upperDomain -> 0.0
        assertEquals(0.0, dist.upperCumulativeProbability(8), EPSILON);
        assertEquals(0.0, dist.upperCumulativeProbability(12), EPSILON);

        // Intermediate values (innerCumulativeProbability with dx = -1)
        double p6 = dist.probability(6);
        double p7 = dist.probability(7);
        assertEquals(p7, dist.upperCumulativeProbability(7), EPSILON);
        assertEquals(p6 + p7, dist.upperCumulativeProbability(6), EPSILON);
    }

    @Test(timeout = 4000)
    public void testCumulativeComplementarity() {
        // For integer distributions, P(X <= x) + P(X >= x + 1) == 1.0
        HypergeometricDistribution dist = new HypergeometricDistribution(20, 10, 8);

        for (int x = dist.getSupportLowerBound() - 1; x <= dist.getSupportUpperBound() + 1; ++x) {
            double cdf = dist.cumulativeProbability(x);
            double upperCdfNext = dist.upperCumulativeProbability(x + 1);
            assertEquals(1.0, cdf + upperCdfNext, 1e-10);
        }
    }

    // =========================================================================
    // Partition D: Statistical Moments & Caching
    // =========================================================================

    @Test(timeout = 4000)
    public void testMomentsCalculationAndCaching() {
        // N = 10, m = 5, n = 4
        // Mean = n * m / N = 4 * 5 / 10 = 2.0
        // Variance = [n * m * (N - n) * (N - m)] / [N^2 * (N - 1)]
        //          = [4 * 5 * 6 * 5] / [100 * 9] = 600 / 900 = 2/3 ≈ 0.6666666666666666
        HypergeometricDistribution dist = new HypergeometricDistribution(10, 5, 4);

        assertEquals(2.0, dist.getNumericalMean(), EPSILON);

        double varianceFirstCall = dist.getNumericalVariance();
        assertEquals(2.0 / 3.0, varianceFirstCall, EPSILON);

        // Second call tests the cached branch: if (!numericalVarianceIsCalculated)
        double varianceSecondCall = dist.getNumericalVariance();
        assertEquals(varianceFirstCall, varianceSecondCall, 0.0);

        // Also test the protected calculateNumericalVariance method directly
        double calculatedVariance = dist.calculateNumericalVariance();
        assertEquals(2.0 / 3.0, calculatedVariance, EPSILON);
    }

    // =========================================================================
    // Partition E: Sampling & Inversion Method (Defect Zone MATH-1021)
    // =========================================================================

    @Test(timeout = 4000)
    public void testSampleDistributionConsistency() {
        Well19937c rng = new Well19937c(12345L);
        HypergeometricDistribution dist = new HypergeometricDistribution(rng, 50, 15, 10);

        int lower = dist.getSupportLowerBound();
        int upper = dist.getSupportUpperBound();

        for (int i = 0; i < 100; ++i) {
            int val = dist.sample();
            assertTrue("Sample " + val + " must be >= lower bound " + lower, val >= lower);
            assertTrue("Sample " + val + " must be <= upper bound " + upper, val <= upper);
        }
    }

    /**
     * Dedicated defect test targeting MATH-1021 / Defects4J Math-2.
     * With sampleSize * numberOfSuccesses exceeding Integer.MAX_VALUE,
     * 32-bit signed integer overflow in getNumericalMean() causes negative mean
     * and invalid negative sampled values.
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

        for (int i = 0; i < 100; ++i) {
            int sample = dist.sample();
            assertTrue("sample must be >= 0, but was: " + sample, 0 <= sample);
            assertTrue("sample must be <= numberOfSuccesses, but was: " + sample, sample <= numberOfSuccesses);
        }
    }
}