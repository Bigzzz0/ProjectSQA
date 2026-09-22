package org.apache.commons.math3.distribution;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.random.Well19937c;
import org.apache.commons.math3.util.FastMath;

/* [Branch & Defect Analysis Matrix]
 * ====================================================================================================
 * Class Under Test: org.apache.commons.math3.distribution.FDistribution
 *
 * 1. Constructor Branch Coverage:
 *    - FDistribution(double, double): delegates to (n, m, DEFAULT_INVERSE_ABSOLUTE_ACCURACY).
 *    - FDistribution(double, double, double): delegates to (rng, n, m, acc).
 *    - FDistribution(RandomGenerator, double, double, double):
 *        * Branch: numeratorDegreesOfFreedom <= 0 -> throws NotStrictlyPositiveException.
 *        * Branch: numeratorDegreesOfFreedom > 0 -> proceeds to denominator check.
 *        * Branch: denominatorDegreesOfFreedom <= 0 -> throws NotStrictlyPositiveException.
 *        * Branch: denominatorDegreesOfFreedom > 0 -> completes initialization.
 *
 * 2. Cumulative Probability:
 *    - Branch: x <= 0 (e.g. x = -5.0, x = 0.0) -> returns exactly 0.0.
 *    - Branch: x > 0 -> computes Beta.regularizedBeta((n * x) / (m + n * x), 0.5 * n, 0.5 * m).
 *
 * 3. Density Computation:
 *    - Computes logarithmic density formulation and exponentiates. Validated across standard points.
 *
 * 4. Mean & Variance Branch Logic:
 *    - getNumericalMean:
 *        * Branch: denominatorDF > 2 -> returns denominatorDF / (denominatorDF - 2).
 *        * Branch: denominatorDF <= 2 (e.g., 2.0, 1.0) -> returns Double.NaN.
 *    - getNumericalVariance & calculateNumericalVariance:
 *        * Branch: numericalVarianceIsCalculated == false -> triggers calculateNumericalVariance().
 *        * Branch: numericalVarianceIsCalculated == true -> returns cached numericalVariance.
 *        * Branch: denominatorDF > 4 -> computes closed-form variance.
 *        * Branch: denominatorDF <= 4 (e.g., 4.0, 2.0) -> returns Double.NaN.
 *
 * 5. Support Limits & Continuity:
 *    - getSupportLowerBound() -> returns 0.
 *    - getSupportUpperBound() -> returns Double.POSITIVE_INFINITY.
 *    - isSupportUpperBoundInclusive() -> returns false.
 *    - isSupportConnected() -> returns true.
 *
 * 6. Defects4J Known Defect Under Test:
 *    - Method: isSupportLowerBoundInclusive()
 *    - Expected: The support of the F-distribution is strictly (0, +infinity). Thus, 0 is excluded
 *      from the open support domain; expected isSupportLowerBoundInclusive() == false.
 *    - Actual (Defective): Returns true.
 * ====================================================================================================
 */
public class FDistributionGeminiTest {

    private static final double EPSILON = 1e-9;

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testGettersAndConstructors() {
        FDistribution dist = new FDistribution(5.0, 6.0);
        assertEquals(5.0, dist.getNumeratorDegreesOfFreedom(), EPSILON);
        assertEquals(6.0, dist.getDenominatorDegreesOfFreedom(), EPSILON);
        assertEquals(FDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, dist.getSolverAbsoluteAccuracy(), EPSILON);

        FDistribution distCustomAcc = new FDistribution(10.0, 12.0, 1e-6);
        assertEquals(10.0, distCustomAcc.getNumeratorDegreesOfFreedom(), EPSILON);
        assertEquals(12.0, distCustomAcc.getDenominatorDegreesOfFreedom(), EPSILON);
        assertEquals(1e-6, distCustomAcc.getSolverAbsoluteAccuracy(), EPSILON);

        Well19937c rng = new Well19937c(42L);
        FDistribution distWithRng = new FDistribution(rng, 8.0, 9.0, 1e-8);
        assertEquals(8.0, distWithRng.getNumeratorDegreesOfFreedom(), EPSILON);
        assertEquals(9.0, distWithRng.getDenominatorDegreesOfFreedom(), EPSILON);
        assertEquals(1e-8, distWithRng.getSolverAbsoluteAccuracy(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testDensityValidPoint() {
        // F(2, 2) has closed form density: f(x) = 1 / (1 + x)^2 for x > 0
        FDistribution dist = new FDistribution(2.0, 2.0);
        assertEquals(1.0 / FastMath.pow(1.0 + 1.0, 2.0), dist.density(1.0), EPSILON);
        assertEquals(1.0 / FastMath.pow(1.0 + 2.0, 2.0), dist.density(2.0), EPSILON);
        assertEquals(1.0 / FastMath.pow(1.0 + 0.5, 2.0), dist.density(0.5), EPSILON);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityPositive() {
        // F(2, 2): CDF(x) = x / (1 + x) for x > 0
        FDistribution dist = new FDistribution(2.0, 2.0);
        assertEquals(0.5, dist.cumulativeProbability(1.0), EPSILON);
        assertEquals(2.0 / 3.0, dist.cumulativeProbability(2.0), EPSILON);
        assertEquals(0.5 / 1.5, dist.cumulativeProbability(0.5), EPSILON);
    }

    @Test(timeout = 4000)
    public void testNumericalMeanWhenValid() {
        // Mean = b / (b - 2) for b > 2
        FDistribution dist = new FDistribution(5.0, 6.0);
        assertEquals(6.0 / (6.0 - 2.0), dist.getNumericalMean(), EPSILON);

        FDistribution distLargeDen = new FDistribution(1.0, 10.0);
        assertEquals(10.0 / (10.0 - 2.0), distLargeDen.getNumericalMean(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testNumericalVarianceWhenValidAndCaching() {
        // Variance = [2 * b^2 * (a + b - 2)] / [a * (b - 2)^2 * (b - 4)] for b > 4
        // For a = 4, b = 6:
        // Numerator = 2 * 36 * (4 + 6 - 2) = 72 * 8 = 576
        // Denominator = 4 * (4)^2 * (2) = 4 * 16 * 2 = 128
        // Variance = 576 / 128 = 4.5
        FDistribution dist = new FDistribution(4.0, 6.0);
        double expectedVariance = 4.5;

        // First call triggers calculation and caching
        assertEquals(expectedVariance, dist.getNumericalVariance(), EPSILON);
        // Second call tests cached branch
        assertEquals(expectedVariance, dist.getNumericalVariance(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testSamplingConsistency() {
        Well19937c rng = new Well19937c(123456L);
        FDistribution dist = new FDistribution(rng, 5.0, 10.0, 1e-9);

        double sample = dist.sample();
        assertTrue("Sample must be strictly positive", sample > 0);

        double[] samples = dist.sample(10);
        assertEquals(10, samples.length);
        for (double s : samples) {
            assertTrue("Sample elements must be strictly positive", s > 0);
        }
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbability() {
        FDistribution dist = new FDistribution(2.0, 2.0);
        assertEquals(0.0, dist.inverseCumulativeProbability(0.0), EPSILON);
        assertEquals(1.0, dist.inverseCumulativeProbability(0.5), 1e-6);
        assertEquals(Double.POSITIVE_INFINITY, dist.inverseCumulativeProbability(1.0), EPSILON);
    }

    // -------------------------------------------------------------------------
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCumulativeProbabilityNonPositiveBoundaries() {
        FDistribution dist = new FDistribution(5.0, 5.0);
        assertEquals(0.0, dist.cumulativeProbability(0.0), EPSILON);
        assertEquals(0.0, dist.cumulativeProbability(-0.0), EPSILON);
        assertEquals(0.0, dist.cumulativeProbability(-1.0), EPSILON);
        assertEquals(0.0, dist.cumulativeProbability(Double.NEGATIVE_INFINITY), EPSILON);
    }

    @Test(timeout = 4000)
    public void testNumericalMeanBoundary() {
        // Denominator exactly 2.0 -> NaN
        FDistribution distBoundary = new FDistribution(4.0, 2.0);
        assertTrue(Double.isNaN(distBoundary.getNumericalMean()));

        // Denominator < 2.0 -> NaN
        FDistribution distBelow = new FDistribution(4.0, 1.5);
        assertTrue(Double.isNaN(distBelow.getNumericalMean()));

        // Denominator strictly > 2.0 -> finite
        FDistribution distAbove = new FDistribution(4.0, 2.0001);
        assertFalse(Double.isNaN(distAbove.getNumericalMean()));
        assertEquals(2.0001 / 0.0001, distAbove.getNumericalMean(), 1e-3);
    }

    @Test(timeout = 4000)
    public void testNumericalVarianceBoundary() {
        // Denominator exactly 4.0 -> NaN
        FDistribution distFour = new FDistribution(4.0, 4.0);
        assertTrue(Double.isNaN(distFour.getNumericalVariance()));

        // Denominator < 4.0 -> NaN
        FDistribution distThree = new FDistribution(4.0, 3.0);
        assertTrue(Double.isNaN(distThree.getNumericalVariance()));

        // Denominator strictly > 4.0 -> finite
        FDistribution distAbove = new FDistribution(4.0, 4.001);
        assertFalse(Double.isNaN(distAbove.getNumericalVariance()));
    }

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // -------------------------------------------------------------------------

    /**
     * Targets Defects4J known bug:
     * F-distribution support is (0, +infinity).
     * The lower bound (0) is NOT inclusive; hence isSupportLowerBoundInclusive() must return false.
     */
    @Test(timeout = 4000)
    public void testIsSupportLowerBoundInclusive() {
        FDistribution dist = new FDistribution(5.0, 5.0);
        assertFalse("Support lower bound (0) for F-distribution should not be inclusive",
                    dist.isSupportLowerBoundInclusive());
    }

    // -------------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -------------------------------------------------------------------------

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorZeroNumeratorDegreesOfFreedom() {
        new FDistribution(0.0, 5.0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorNegativeNumeratorDegreesOfFreedom() {
        new FDistribution(-1.0, 5.0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorZeroDenominatorDegreesOfFreedom() {
        new FDistribution(5.0, 0.0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorNegativeDenominatorDegreesOfFreedom() {
        new FDistribution(5.0, -2.5);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorWithRngZeroNumeratorDegreesOfFreedom() {
        new FDistribution(new Well19937c(1L), 0.0, 5.0, 1e-9);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorWithRngZeroDenominatorDegreesOfFreedom() {
        new FDistribution(new Well19937c(1L), 5.0, 0.0, 1e-9);
    }

    // -------------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSupportBoundsAndConnectivity() {
        FDistribution dist = new FDistribution(3.0, 3.0);
        assertEquals(0.0, dist.getSupportLowerBound(), EPSILON);
        assertEquals(Double.POSITIVE_INFINITY, dist.getSupportUpperBound(), EPSILON);
        assertFalse(dist.isSupportUpperBoundInclusive());
        assertTrue(dist.isSupportConnected());
    }

    @Test(timeout = 4000)
    public void testProtectedCalculateNumericalVarianceDirectly() {
        FDistribution dist = new FDistribution(2.0, 6.0) {
            @Override
            public double calculateNumericalVariance() {
                return super.calculateNumericalVariance();
            }
        };
        // Denominator is 6 (> 4), so calculateNumericalVariance must return finite value
        double variance = dist.calculateNumericalVariance();
        assertFalse(Double.isNaN(variance));
        assertTrue(variance > 0);
    }
}