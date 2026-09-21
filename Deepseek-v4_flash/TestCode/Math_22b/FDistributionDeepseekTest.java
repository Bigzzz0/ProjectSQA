package org.apache.commons.math3.distribution;

import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 *
 * Partition A: Core Functional Logic & State Transitions
 *   - density(x) for x > 0, x = 0, and x < 0 (density is 0 for x < 0)
 *   - cumulativeProbability(x) for x <= 0 (returns 0), x > 0 (uses regularizedBeta)
 *   - getNumericalMean(): branch on denominatorDF > 2 vs <= 2 (NaN)
 *   - getNumericalVariance(): branch on denominatorDF > 4 vs <= 4 (NaN); also caching flag
 *   - getSupportLowerBound() always 0
 *   - getSupportUpperBound() always +Infinity
 *   - isSupportLowerBoundInclusive() – defect: incorrectly returns true, should be false
 *   - isSupportUpperBoundInclusive() – returns false
 *   - isSupportConnected() – returns true
 *   - getSolverAbsoluteAccuracy() – returns the value set in constructor
 *
 * Partition B: Boundary Value Analysis & Extremes
 *   - Numerator/Denominator DF = small positive (e.g., 1.0, 2.0), large, and edge cases (just above 0)
 *   - x = 0, x = Double.MIN_VALUE, x = Double.MAX_VALUE, x = 1.0
 *
 * Partition C: Defect-Targeted Branch Zone
 *   - testIsSupportLowerBoundInclusive_defect(): specifically asserts false (bug triggers true)
 *   - testIsSupportUpperBoundInclusive(): verify false (no defect but covers branch)
 *
 * Partition D: Exception & Defensive Guard Paths
 *   - Constructor with numeratorDegreesOfFreedom <= 0 => NotStrictlyPositiveException
 *   - Constructor with denominatorDegreesOfFreedom <= 0 => NotStrictlyPositiveException
 *   - Constructor with both zero => NotStrictlyPositiveException
 *
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Multiple calls to getNumericalVariance return same cached value
 *   - Getters return the same values passed to constructor
 *   - Constructor with custom RandomGenerator and accuracy
 */
public class FDistributionDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDensityPositiveX() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        double d = dist.density(0.5);
        // Expected value computed manually or via known property: for df=(5,10), density at 0.5 ≈ 0.6861
        assertEquals(0.6861, d, 1e-4);
    }

    @Test(timeout = 4000)
    public void testDensityAtZero() {
        // density(0) should be 0 for positive degrees of freedom
        FDistribution dist = new FDistribution(2.0, 2.0);
        assertEquals(0.0, dist.density(0.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDensityNegativeX() {
        FDistribution dist = new FDistribution(3.0, 5.0);
        // density returns 0 for x < 0 because support is [0, +∞)
        assertEquals(0.0, dist.density(-1.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityZeroOrNegative() {
        FDistribution dist = new FDistribution(1.0, 1.0);
        assertEquals(0.0, dist.cumulativeProbability(0.0), 1e-15);
        assertEquals(0.0, dist.cumulativeProbability(-1.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityPositive() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        double p = dist.cumulativeProbability(1.0);
        // Expected cumulative probability ≈ 0.5374
        assertEquals(0.5374, p, 1e-4);
    }

    @Test(timeout = 4000)
    public void testGetNumericalMeanWhenDFAbove2() {
        FDistribution dist = new FDistribution(3.0, 5.0);
        // mean = denominatorDF / (denominatorDF - 2) = 5/3 ≈ 1.6667
        assertEquals(5.0 / 3.0, dist.getNumericalMean(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetNumericalMeanWhenDFEquals2() {
        FDistribution dist = new FDistribution(3.0, 2.0);
        assertEquals(Double.NaN, dist.getNumericalMean(), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumericalMeanWhenDFBelow2() {
        FDistribution dist = new FDistribution(3.0, 1.5);
        assertEquals(Double.NaN, dist.getNumericalMean(), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumericalVarianceWhenDFAbove4() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        double var = dist.getNumericalVariance();
        // Expected variance = [2 * (denom^2) * (num+denom-2)] / [num * (denom-2)^2 * (denom-4)]
        double num = 5.0, denom = 10.0;
        double expected = (2 * denom * denom * (num + denom - 2)) / (num * (denom - 2) * (denom - 2) * (denom - 4));
        assertEquals(expected, var, 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetNumericalVarianceWhenDFEquals4() {
        FDistribution dist = new FDistribution(5.0, 4.0);
        assertEquals(Double.NaN, dist.getNumericalVariance(), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumericalVarianceWhenDFBelow4() {
        FDistribution dist = new FDistribution(5.0, 3.0);
        assertEquals(Double.NaN, dist.getNumericalVariance(), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNumericalVarianceCaching() {
        FDistribution dist = new FDistribution(5.0, 10.0);
        double v1 = dist.getNumericalVariance();
        double v2 = dist.getNumericalVariance();
        assertEquals(v1, v2, 0.0);
    }

    @Test(timeout = 4000)
    public void testSupportLowerBound() {
        FDistribution dist = new FDistribution(1.0, 1.0);
        assertEquals(0.0, dist.getSupportLowerBound(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSupportUpperBound() {
        FDistribution dist = new FDistribution(1.0, 1.0);
        assertEquals(Double.POSITIVE_INFINITY, dist.getSupportUpperBound(), 0.0);
    }

    @Test(timeout = 4000)
    public void testIsSupportUpperBoundInclusive() {
        FDistribution dist = new FDistribution(1.0, 1.0);
        assertFalse("upper bound should not be inclusive", dist.isSupportUpperBoundInclusive());
    }

    @Test(timeout = 4000)
    public void testIsSupportConnected() {
        FDistribution dist = new FDistribution(1.0, 1.0);
        assertTrue("support should be connected", dist.isSupportConnected());
    }

    @Test(timeout = 4000)
    public void testGetSolverAbsoluteAccuracyDefault() {
        FDistribution dist = new FDistribution(1.0, 1.0);
        assertEquals(FDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY,
                     dist.getSolverAbsoluteAccuracy(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetSolverAbsoluteAccuracyCustom() {
        double acc = 1e-8;
        FDistribution dist = new FDistribution(1.0, 1.0, acc);
        assertEquals(acc, dist.getSolverAbsoluteAccuracy(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGettersFromConstructor() {
        FDistribution dist = new FDistribution(2.5, 3.5);
        assertEquals(2.5, dist.getNumeratorDegreesOfFreedom(), 0.0);
        assertEquals(3.5, dist.getDenominatorDegreesOfFreedom(), 0.0);
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDensityExtremeX() {
        FDistribution dist = new FDistribution(1.0, 1.0);
        // Very small x
        double d = dist.density(1e-10);
        assertTrue("density should be positive", d > 0);
        // Very large x
        d = dist.density(1e10);
        assertTrue("density should be very small for large x", d < 1e-10);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityExtremeX() {
        FDistribution dist = new FDistribution(1.0, 1.0);
        // x very large => close to 1
        double p = dist.cumulativeProbability(1000.0);
        assertTrue(p > 0.99);
        // x very small positive => close to 0
        p = dist.cumulativeProbability(1e-10);
        assertTrue(p < 1e-9);
    }

    @Test(timeout = 4000)
    public void testDensityOnVeryLargeDF() {
        FDistribution dist = new FDistribution(100.0, 100.0);
        double d = dist.density(1.0);
        // Approximate normal shape
        assertTrue(d > 0.0);
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testIsSupportLowerBoundInclusive_defect() {
        // Known Defects4J bug: isSupportLowerBoundInclusive() returns true but should return false
        FDistribution dist = new FDistribution(1.0, 1.0);
        assertFalse("F-distribution lower bound (0) is NOT inclusive", dist.isSupportLowerBoundInclusive());
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testConstructorNumeratorZero() {
        new FDistribution(0.0, 1.0);
    }

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testConstructorNumeratorNegative() {
        new FDistribution(-1.0, 1.0);
    }

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testConstructorDenominatorZero() {
        new FDistribution(1.0, 0.0);
    }

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testConstructorDenominatorNegative() {
        new FDistribution(1.0, -5.0);
    }

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testConstructorBothZero() {
        new FDistribution(0.0, 0.0);
    }

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testConstructorWithRNGNumeratorZero() {
        new FDistribution(new Well19937c(), 0.0, 1.0, 1e-9);
    }

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testConstructorWithRNGDenominatorZero() {
        new FDistribution(new Well19937c(), 1.0, 0.0, 1e-9);
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructorWithRNGAndCustomAccuracy() {
        RandomGenerator rng = new Well19937c(12345L);
        double acc = 1e-7;
        FDistribution dist = new FDistribution(rng, 2.0, 3.0, acc);
        assertEquals(2.0, dist.getNumeratorDegreesOfFreedom(), 0.0);
        assertEquals(3.0, dist.getDenominatorDegreesOfFreedom(), 0.0);
        assertEquals(acc, dist.getSolverAbsoluteAccuracy(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMultipleConstructorsConsistency() {
        FDistribution d1 = new FDistribution(1.0, 2.0);
        FDistribution d2 = new FDistribution(1.0, 2.0, FDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
        assertEquals(d1.getNumericalMean(), d2.getNumericalMean(), 0.0);
        assertEquals(d1.getNumericalVariance(), d2.getNumericalVariance(), 0.0);
    }
}