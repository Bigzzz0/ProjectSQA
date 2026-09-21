package org.apache.commons.math3.distribution;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.random.RandomGenerator;

/**
 * Branch & Defect Analysis Matrix:
 * 
 * UniformRealDistribution methods and their decision branches:
 * - Constructor: lower >= upper -> throw NumberIsTooLargeException
 * - density(x): x < lower || x > upper -> 0.0; else 1/(upper-lower)
 * - cumulativeProbability(x): x <= lower -> 0.0; x >= upper -> 1.0; else (x-lower)/(upper-lower)
 * - getNumericalMean(): 0.5*(lower+upper)
 * - getNumericalVariance(): (upper-lower)^2 / 12
 * - getSupportLowerBound(): lower
 * - getSupportUpperBound(): upper
 * - isSupportLowerBoundInclusive(): true
 * - isSupportUpperBoundInclusive(): true (expected, but defective version returns false)
 * - isSupportConnected(): true
 * - sample(): u*upper + (1-u)*lower where u = random.nextDouble()
 * - getSolverAbsoluteAccuracy(): solverAbsoluteAccuracy
 *
 * Target Defect:
 *   isSupportUpperBoundInclusive() returns false instead of true.
 *   We assertTrue(isSupportUpperBoundInclusive()) to expose the bug.
 */
public class UniformRealDistributionDeepseekTest {

    private static final double EPS = 1e-12;

    // -------------------- Partition A: Core Functional Logic & State Transitions --------------------

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        UniformRealDistribution d = new UniformRealDistribution();
        assertEquals(0.0, d.getSupportLowerBound(), EPS);
        assertEquals(1.0, d.getSupportUpperBound(), EPS);
        assertEquals(0.5, d.getNumericalMean(), EPS);
        assertEquals(1.0 / 12, d.getNumericalVariance(), 1e-15);
        assertEquals(1e-9, d.getSolverAbsoluteAccuracy(), EPS);
    }

    @Test(timeout = 4000)
    public void testCustomBounds() {
        UniformRealDistribution d = new UniformRealDistribution(2.0, 5.0);
        assertEquals(2.0, d.getSupportLowerBound(), EPS);
        assertEquals(5.0, d.getSupportUpperBound(), EPS);
        assertEquals(3.5, d.getNumericalMean(), EPS);
        assertEquals(9.0 / 12, d.getNumericalVariance(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testCustomBoundsAndAccuracy() {
        UniformRealDistribution d = new UniformRealDistribution(-1.0, 3.0, 1e-5);
        assertEquals(-1.0, d.getSupportLowerBound(), EPS);
        assertEquals(3.0, d.getSupportUpperBound(), EPS);
        assertEquals(1.0, d.getNumericalMean(), EPS);
        assertEquals(16.0 / 12, d.getNumericalVariance(), 1e-15);
        assertEquals(1e-5, d.getSolverAbsoluteAccuracy(), EPS);
    }

    @Test(timeout = 4000)
    public void testSupportBoundsAndConnected() {
        UniformRealDistribution d = new UniformRealDistribution(0.0, 10.0);
        assertTrue(d.isSupportLowerBoundInclusive());
        assertTrue(d.isSupportUpperBoundInclusive());  // <-- Targets the defect: defective returns false
        assertTrue(d.isSupportConnected());
    }

    @Test(timeout = 4000)
    public void testGetSupportLowerBound() {
        UniformRealDistribution d = new UniformRealDistribution(-5.0, 5.0);
        assertEquals(-5.0, d.getSupportLowerBound(), EPS);
    }

    @Test(timeout = 4000)
    public void testGetSupportUpperBound() {
        UniformRealDistribution d = new UniformRealDistribution(-5.0, 5.0);
        assertEquals(5.0, d.getSupportUpperBound(), EPS);
    }

    // -------------------- Partition B: Boundary Value Analysis & Extremes --------------------

    @Test(timeout = 4000)
    public void testDensityAtLowerBound() {
        UniformRealDistribution d = new UniformRealDistribution(1.0, 4.0);
        double expected = 1.0 / 3.0;
        assertEquals(expected, d.density(1.0), EPS);   // x == lower
    }

    @Test(timeout = 4000)
    public void testDensityAtUpperBound() {
        UniformRealDistribution d = new UniformRealDistribution(1.0, 4.0);
        double expected = 1.0 / 3.0;
        assertEquals(expected, d.density(4.0), EPS);   // x == upper (density positive)
    }

    @Test(timeout = 4000)
    public void testDensityBelowLower() {
        UniformRealDistribution d = new UniformRealDistribution(1.0, 4.0);
        assertEquals(0.0, d.density(0.9), EPS);
    }

    @Test(timeout = 4000)
    public void testDensityAboveUpper() {
        UniformRealDistribution d = new UniformRealDistribution(1.0, 4.0);
        assertEquals(0.0, d.density(4.1), EPS);
    }

    @Test(timeout = 4000)
    public void testDensityInside() {
        UniformRealDistribution d = new UniformRealDistribution(1.0, 4.0);
        double expected = 1.0 / 3.0;
        assertEquals(expected, d.density(2.5), EPS);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityAtLower() {
        UniformRealDistribution d = new UniformRealDistribution(2.0, 5.0);
        assertEquals(0.0, d.cumulativeProbability(2.0), EPS);  // x == lower -> 0
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityBelowLower() {
        UniformRealDistribution d = new UniformRealDistribution(2.0, 5.0);
        assertEquals(0.0, d.cumulativeProbability(1.0), EPS);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityAtUpper() {
        UniformRealDistribution d = new UniformRealDistribution(2.0, 5.0);
        assertEquals(1.0, d.cumulativeProbability(5.0), EPS);  // x == upper -> 1
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityAboveUpper() {
        UniformRealDistribution d = new UniformRealDistribution(2.0, 5.0);
        assertEquals(1.0, d.cumulativeProbability(6.0), EPS);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityInside() {
        UniformRealDistribution d = new UniformRealDistribution(2.0, 5.0);
        // (3.5-2)/(5-2) = 1.5/3 = 0.5
        assertEquals(0.5, d.cumulativeProbability(3.5), EPS);
    }

    @Test(timeout = 4000)
    public void testNumericalMean() {
        UniformRealDistribution d = new UniformRealDistribution(-10.0, 20.0);
        assertEquals(5.0, d.getNumericalMean(), EPS);
    }

    @Test(timeout = 4000)
    public void testNumericalVariance() {
        UniformRealDistribution d = new UniformRealDistribution(0.0, 6.0);
        // (6-0)^2/12 = 36/12 = 3
        assertEquals(3.0, d.getNumericalVariance(), EPS);
    }

    // -------------------- Partition C: Defect-Targeted Branch Zone --------------------
    // Specific test for the known defect: isSupportUpperBoundInclusive should be true

    @Test(timeout = 4000)
    public void testIsSupportUpperBoundInclusiveDefect() {
        UniformRealDistribution d = new UniformRealDistribution(0.0, 1.0);
        assertTrue("Defect: isSupportUpperBoundInclusive should return true but returned false",
                   d.isSupportUpperBoundInclusive());
    }

    @Test(timeout = 4000)
    public void testIsSupportLowerBoundInclusiveDefect() {
        UniformRealDistribution d = new UniformRealDistribution(0.0, 1.0);
        assertTrue(d.isSupportLowerBoundInclusive());
    }

    // -------------------- Partition D: Exception & Defensive Guard Paths --------------------

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.NumberIsTooLargeException.class)
    public void testConstructorLowerGreaterThanUpper() {
        new UniformRealDistribution(5.0, 3.0);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.NumberIsTooLargeException.class)
    public void testConstructorLowerEqualToUpper() {
        new UniformRealDistribution(2.0, 2.0);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.NumberIsTooLargeException.class)
    public void testConstructorWithAccuracyLowerGreaterThanUpper() {
        new UniformRealDistribution(1.0, 0.5, 1e-9);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.NumberIsTooLargeException.class)
    public void testConstructorWithRngLowerEqualToUpper() {
        new UniformRealDistribution(new Well19937c(), 7.0, 7.0, 1e-9);
    }

    // -------------------- Partition E: Object Lifecycle & Contract Integrity --------------------

    @Test(timeout = 4000)
    public void testSolverAbsoluteAccuracy() {
        UniformRealDistribution d = new UniformRealDistribution(0.0, 1.0, 1e-8);
        assertEquals(1e-8, d.getSolverAbsoluteAccuracy(), EPS);
    }

    @Test(timeout = 4000)
    public void testSampleDeterministic() {
        // Use a custom RandomGenerator that always returns 0.5
        RandomGenerator fixed = new RandomGenerator() {
            public void setSeed(int seed) {}
            public void setSeed(int[] seed) {}
            public void setSeed(long seed) {}
            public void nextBytes(byte[] bytes) {}
            public int nextInt() { return 0; }
            public int nextInt(int n) { return 0; }
            public long nextLong() { return 0L; }
            public boolean nextBoolean() { return false; }
            public float nextFloat() { return 0.5f; }
            public double nextDouble() { return 0.5; }
            public double nextGaussian() { return 0.0; }
        };
        UniformRealDistribution d = new UniformRealDistribution(fixed, 10.0, 20.0, 1e-9);
        double sample = d.sample();
        // sample = 0.5*20 + 0.5*10 = 15
        assertEquals(15.0, sample, EPS);
    }

    @Test(timeout = 4000)
    public void testSampleDeterministicLowerBound() {
        // nextDouble = 0.0 => sample = 0*upper + 1*lower = lower
        RandomGenerator fixed = new RandomGenerator() {
            public void setSeed(int seed) {}
            public void setSeed(int[] seed) {}
            public void setSeed(long seed) {}
            public void nextBytes(byte[] bytes) {}
            public int nextInt() { return 0; }
            public int nextInt(int n) { return 0; }
            public long nextLong() { return 0L; }
            public boolean nextBoolean() { return false; }
            public float nextFloat() { return 0.0f; }
            public double nextDouble() { return 0.0; }
            public double nextGaussian() { return 0.0; }
        };
        UniformRealDistribution d = new UniformRealDistribution(fixed, 5.0, 8.0, 1e-9);
        assertEquals(5.0, d.sample(), EPS);
    }

    @Test(timeout = 4000)
    public void testSampleDeterministicUpperBound() {
        // nextDouble = 1.0 => sample = 1*upper + 0*lower = upper
        RandomGenerator fixed = new RandomGenerator() {
            public void setSeed(int seed) {}
            public void setSeed(int[] seed) {}
            public void setSeed(long seed) {}
            public void nextBytes(byte[] bytes) {}
            public int nextInt() { return 0; }
            public int nextInt(int n) { return 0; }
            public long nextLong() { return 0L; }
            public boolean nextBoolean() { return false; }
            public float nextFloat() { return 1.0f; }
            public double nextDouble() { return 1.0; }
            public double nextGaussian() { return 0.0; }
        };
        UniformRealDistribution d = new UniformRealDistribution(fixed, 5.0, 8.0, 1e-9);
        assertEquals(8.0, d.sample(), EPS);
    }
}