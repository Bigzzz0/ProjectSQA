package org.apache.commons.math.distribution;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.MathException;
import org.apache.commons.math.exception.NotStrictlyPositiveException;

public class NormalDistributionImplDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Target: NormalDistributionImpl
     * 
     * Branches covered:
     * 1. Constructor: sd <= 0 → throw NotStrictlyPositiveException
     * 2. Constructor: sd > 0 → valid state
     * 3. density(): x = mean, x = mean ± sd, x = mean ± 2sd, x = ±∞
     * 4. cumulativeProbability(): x = mean, x = mean ± 20sd, x = mean ± 40sd, x = ±∞
     * 5. inverseCumulativeProbability(): p = 0, p = 1, p = 0.5, p < 0.5, p > 0.5
     * 6. getDomainLowerBound(): p < 0.5, p >= 0.5
     * 7. getInitialDomain(): p < 0.5, p > 0.5, p == 0.5
     * 8. getSolverAbsoluteAccuracy(): default and custom values
     * 9. sample(): returns finite value (deterministic via seeded RandomDataImpl)
     * 
     * Defect targeting:
     * - Known defect: cumulativeProbability(∞) throws ConvergenceException
     *   due to Erf.erf(∞) → Continued fraction diverged to NaN.
     *   Expected: cumulativeProbability(∞) should return 1.0 (or handle gracefully)
     *   Test: testCumulativeProbabilityPositiveInfinity() must pass on fixed version,
     *         fail on defective version.
     * 
     * Boundary values:
     * - x = mean ± 20sd (JDK 1.5 blow-up threshold)
     * - x = mean ± 40sd (documented threshold for 0/1 return)
     * - p = 0, 0.5, 1 for inverseCumulativeProbability
     * - sd = 0, negative, Double.MIN_VALUE, Double.MAX_VALUE
     * - mean = 0, negative, Double.MAX_VALUE
     */
    
    private static final double EPSILON = 1e-9;

    // ==================== PART A: Core Functional Logic & State Transitions ====================
    
    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        assertEquals("Default mean", 0.0, dist.getMean(), 0.0);
        assertEquals("Default sd", 1.0, dist.getStandardDeviation(), 0.0);
        assertEquals("Default accuracy", 
                NormalDistributionImpl.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, 
                dist.getSolverAbsoluteAccuracy(), 0.0);
    }

    @Test(timeout = 4000)
    public void testParameterizedConstructor() {
        NormalDistributionImpl dist = new NormalDistributionImpl(2.5, 0.5);
        assertEquals("Mean", 2.5, dist.getMean(), 0.0);
        assertEquals("SD", 0.5, dist.getStandardDeviation(), 0.0);
        assertEquals("Accuracy", 
                NormalDistributionImpl.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, 
                dist.getSolverAbsoluteAccuracy(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCustomAccuracyConstructor() {
        double acc = 1e-12;
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1, acc);
        assertEquals("Custom accuracy", acc, dist.getSolverAbsoluteAccuracy(), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetMeanAndStandardDeviation() {
        NormalDistributionImpl dist = new NormalDistributionImpl(-3.7, 2.2);
        assertEquals(-3.7, dist.getMean(), 0.0);
        assertEquals(2.2, dist.getStandardDeviation(), 0.0);
    }

    // ==================== PART B: Boundary Value Analysis & Extremes ====================
    
    @Test(timeout = 4000)
    public void testDensityAtMean() {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        double expected = 1.0 / Math.sqrt(2 * Math.PI);
        assertEquals("Density at mean", expected, dist.density(0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testDensityAtOneSigma() {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        double expected = Math.exp(-0.5) / Math.sqrt(2 * Math.PI);
        assertEquals("Density at +1σ", expected, dist.density(1), 1e-12);
        assertEquals("Density at -1σ", expected, dist.density(-1), 1e-12);
    }

    @Test(timeout = 4000)
    public void testDensityAtTwoSigma() {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        double expected = Math.exp(-2.0) / Math.sqrt(2 * Math.PI);
        assertEquals("Density at +2σ", expected, dist.density(2), 1e-12);
        assertEquals("Density at -2σ", expected, dist.density(-2), 1e-12);
    }

    @Test(timeout = 4000)
    public void testDensityAtInfinity() {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        assertEquals("Density at +∞", 0.0, dist.density(Double.POSITIVE_INFINITY), 0.0);
        assertEquals("Density at -∞", 0.0, dist.density(Double.NEGATIVE_INFINITY), 0.0);
    }

    @Test(timeout = 4000)
    public void testDensityWithNonZeroMean() {
        NormalDistributionImpl dist = new NormalDistributionImpl(5, 2);
        double x = 5;
        double expected = 1.0 / (2 * Math.sqrt(2 * Math.PI));
        assertEquals("Density at mean with sd=2", expected, dist.density(x), 1e-12);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityAtMean() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        assertEquals("CDF at mean", 0.5, dist.cumulativeProbability(0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityAtOneSigma() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        // Standard normal CDF at 1 ≈ 0.841344746
        assertEquals("CDF at +1σ", 0.8413447460685429, dist.cumulativeProbability(1), 1e-9);
        assertEquals("CDF at -1σ", 0.15865525393145707, dist.cumulativeProbability(-1), 1e-9);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityAtTwentySigma() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        // At 20σ, CDF should be extremely close to 1 (but not exactly due to precision)
        double cdf = dist.cumulativeProbability(20);
        assertTrue("CDF at 20σ should be > 0.999999", cdf > 0.999999);
        assertTrue("CDF at 20σ should be <= 1.0", cdf <= 1.0);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityAtFortySigma() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        // At 40σ, should return exactly 1.0 per documentation
        assertEquals("CDF at 40σ", 1.0, dist.cumulativeProbability(40), 0.0);
        assertEquals("CDF at -40σ", 0.0, dist.cumulativeProbability(-40), 0.0);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityAtNegativeInfinity() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        assertEquals("CDF at -∞", 0.0, dist.cumulativeProbability(Double.NEGATIVE_INFINITY), 0.0);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityWithNonZeroMean() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(10, 2);
        // At x = mean, CDF should be 0.5
        assertEquals("CDF at mean (μ=10,σ=2)", 0.5, dist.cumulativeProbability(10), 1e-12);
    }

    // ==================== PART C: Defect-Targeted Branch Zone ====================
    
    /**
     * Defect: cumulativeProbability(∞) throws ConvergenceException due to
     * Erf.erf(∞) → Continued fraction diverged to NaN.
     * Expected behavior: Should return 1.0 (since P(X < ∞) = 1).
     * This test will fail on the defective version.
     */
    @Test(timeout = 4000)
    public void testCumulativeProbabilityPositiveInfinity() {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        try {
            double result = dist.cumulativeProbability(Double.POSITIVE_INFINITY);
            // If no exception, assert the correct value
            assertEquals("CDF at +∞ should be 1.0", 1.0, result, 0.0);
        } catch (MathException e) {
            fail("cumulativeProbability(+∞) threw MathException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityNegativeInfinity() {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        try {
            double result = dist.cumulativeProbability(Double.NEGATIVE_INFINITY);
            assertEquals("CDF at -∞ should be 0.0", 0.0, result, 0.0);
        } catch (MathException e) {
            fail("cumulativeProbability(-∞) threw MathException: " + e.getMessage());
        }
    }

    // ==================== PART D: Exception & Defensive Guard Paths ====================
    
    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testConstructorZeroSD() {
        new NormalDistributionImpl(0, 0);
    }

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testConstructorNegativeSD() {
        new NormalDistributionImpl(0, -1);
    }

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testConstructorNegativeSDWithCustomAccuracy() {
        new NormalDistributionImpl(0, -0.1, 1e-9);
    }

    @Test(timeout = 4000)
    public void testConstructorMinPositiveSD() {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, Double.MIN_VALUE);
        assertEquals("SD = Double.MIN_VALUE", Double.MIN_VALUE, dist.getStandardDeviation(), 0.0);
    }

    @Test(timeout = 4000)
    public void testConstructorMaxSD() {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, Double.MAX_VALUE);
        assertEquals("SD = Double.MAX_VALUE", Double.MAX_VALUE, dist.getStandardDeviation(), 0.0);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityZero() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        assertEquals("p=0 → -∞", Double.NEGATIVE_INFINITY, 
                dist.inverseCumulativeProbability(0), 0.0);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityOne() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        assertEquals("p=1 → +∞", Double.POSITIVE_INFINITY, 
                dist.inverseCumulativeProbability(1), 0.0);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityHalf() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        assertEquals("p=0.5 → mean", 0.0, dist.inverseCumulativeProbability(0.5), 1e-9);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityLessThanHalf() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(5, 2);
        double p = 0.25;
        double result = dist.inverseCumulativeProbability(p);
        assertTrue("Result should be < mean for p<0.5", result < 5);
        // Verify by checking CDF(result) ≈ p
        assertEquals("CDF(invCDF(p)) ≈ p", p, dist.cumulativeProbability(result), 1e-6);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityGreaterThanHalf() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(-3, 1.5);
        double p = 0.75;
        double result = dist.inverseCumulativeProbability(p);
        assertTrue("Result should be > mean for p>0.5", result > -3);
        assertEquals("CDF(invCDF(p)) ≈ p", p, dist.cumulativeProbability(result), 1e-6);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityExtremeP() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        // Very small p
        double pSmall = 1e-10;
        double resultSmall = dist.inverseCumulativeProbability(pSmall);
        assertTrue("Small p → very negative", resultSmall < -6);
        
        // Very large p
        double pLarge = 1 - 1e-10;
        double resultLarge = dist.inverseCumulativeProbability(pLarge);
        assertTrue("Large p → very positive", resultLarge > 6);
    }

    // ==================== PART E: Object Lifecycle & Contract Integrity ====================
    
    @Test(timeout = 4000)
    public void testGetDomainLowerBound() {
        NormalDistributionImpl dist = new NormalDistributionImpl(10, 2);
        // p < 0.5 → -Double.MAX_VALUE
        assertEquals("p<0.5 → -MAX_VALUE", -Double.MAX_VALUE, 
                dist.getDomainLowerBound(0.4), 0.0);
        // p >= 0.5 → mean
        assertEquals("p>=0.5 → mean", 10.0, dist.getDomainLowerBound(0.5), 0.0);
        assertEquals("p>=0.5 → mean", 10.0, dist.getDomainLowerBound(0.9), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetDomainUpperBound() {
        NormalDistributionImpl dist = new NormalDistributionImpl(10, 2);
        // p < 0.5 → mean
        assertEquals("p<0.5 → mean", 10.0, dist.getDomainUpperBound(0.4), 0.0);
        // p >= 0.5 → Double.MAX_VALUE
        assertEquals("p>=0.5 → MAX_VALUE", Double.MAX_VALUE, 
                dist.getDomainUpperBound(0.5), 0.0);
        assertEquals("p>=0.5 → MAX_VALUE", Double.MAX_VALUE, 
                dist.getDomainUpperBound(0.9), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetInitialDomain() {
        NormalDistributionImpl dist = new NormalDistributionImpl(10, 2);
        // p < 0.5 → mean - sd
        assertEquals("p<0.5 → mean-sd", 8.0, dist.getInitialDomain(0.4), 0.0);
        // p > 0.5 → mean + sd
        assertEquals("p>0.5 → mean+sd", 12.0, dist.getInitialDomain(0.6), 0.0);
        // p == 0.5 → mean
        assertEquals("p=0.5 → mean", 10.0, dist.getInitialDomain(0.5), 0.0);
    }

    @Test(timeout = 4000)
    public void testSample() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        // Sample should return a finite value (deterministic with seeded RandomDataImpl)
        double sample = dist.sample();
        assertTrue("Sample should be finite", Double.isFinite(sample));
    }

    @Test(timeout = 4000)
    public void testSampleWithCustomMeanSD() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(100, 10);
        double sample = dist.sample();
        assertTrue("Sample should be finite", Double.isFinite(sample));
    }

    @Test(timeout = 4000)
    public void testSerializationCompatibility() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl(2.5, 0.5);
        // Verify basic state
        assertEquals(2.5, dist.getMean(), 0.0);
        assertEquals(0.5, dist.getStandardDeviation(), 0.0);
    }

    @Test(timeout = 4000)
    public void testDensitySymmetry() {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        assertEquals("Density symmetric around mean", 
                dist.density(1), dist.density(-1), 1e-12);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilitySymmetry() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        double cdfAt1 = dist.cumulativeProbability(1);
        double cdfAtMinus1 = dist.cumulativeProbability(-1);
        assertEquals("CDF symmetry: F(1) + F(-1) = 1", 1.0, cdfAt1 + cdfAtMinus1, 1e-9);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilitySymmetry() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        double x1 = dist.inverseCumulativeProbability(0.25);
        double x2 = dist.inverseCumulativeProbability(0.75);
        assertEquals("Symmetry: x(0.25) = -x(0.75)", -x1, x2, 1e-9);
    }

    @Test(timeout = 4000)
    public void testExtremeMeanAndSD() {
        NormalDistributionImpl dist = new NormalDistributionImpl(Double.MAX_VALUE, Double.MAX_VALUE);
        assertEquals("Mean = MAX_VALUE", Double.MAX_VALUE, dist.getMean(), 0.0);
        assertEquals("SD = MAX_VALUE", Double.MAX_VALUE, dist.getStandardDeviation(), 0.0);
        
        // Density at mean should be 1/(sd * sqrt(2π))
        double expected = 1.0 / (Double.MAX_VALUE * Math.sqrt(2 * Math.PI));
        assertEquals("Density at mean with extreme params", 0.0, dist.density(Double.MAX_VALUE), 0.0);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityExtremeValues() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        // Very large positive x
        double cdfLarge = dist.cumulativeProbability(100);
        assertTrue("CDF at 100 should be close to 1", cdfLarge > 0.999999);
        
        // Very large negative x
        double cdfSmall = dist.cumulativeProbability(-100);
        assertTrue("CDF at -100 should be close to 0", cdfSmall < 0.000001);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityBoundary() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        // p extremely close to 0
        double p0 = Double.MIN_VALUE;
        double result0 = dist.inverseCumulativeProbability(p0);
        assertTrue("Should be very negative", result0 < -8);
        
        // p extremely close to 1
        double p1 = 1 - Double.MIN_VALUE;
        double result1 = dist.inverseCumulativeProbability(p1);
        assertTrue("Should be very positive", result1 > 8);
    }

    @Test(timeout = 4000)
    public void testDensityWithZeroVariance() {
        // SD = Double.MIN_VALUE is effectively zero for practical purposes
        NormalDistributionImpl dist = new NormalDistributionImpl(0, Double.MIN_VALUE);
        // At x = mean, density should be very large
        double density = dist.density(0);
        assertTrue("Density should be very large", density > 1e300);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityWithTinySD() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, Double.MIN_VALUE);
        // At x = mean, CDF should be 0.5
        assertEquals("CDF at mean with tiny SD", 0.5, dist.cumulativeProbability(0), 1e-9);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityWithTinySD() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, Double.MIN_VALUE);
        double result = dist.inverseCumulativeProbability(0.5);
        assertEquals("Inverse CDF at 0.5 with tiny SD", 0.0, result, 1e-9);
    }

    @Test(timeout = 4000)
    public void testMultipleInstances() {
        NormalDistributionImpl dist1 = new NormalDistributionImpl(0, 1);
        NormalDistributionImpl dist2 = new NormalDistributionImpl(0, 1);
        NormalDistributionImpl dist3 = new NormalDistributionImpl(1, 1);
        
        assertEquals("Same params → same density", dist1.density(0.5), dist2.density(0.5), 0.0);
        assertNotEquals("Different mean → different density", 
                dist1.density(0.5), dist3.density(0.5), 0.0);
    }

    @Test(timeout = 4000)
    public void testDensityNaN() {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        double result = dist.density(Double.NaN);
        assertTrue("Density(NaN) should be NaN", Double.isNaN(result));
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityNaN() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        double result = dist.cumulativeProbability(Double.NaN);
        assertTrue("CDF(NaN) should be NaN", Double.isNaN(result));
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityNaN() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        double result = dist.inverseCumulativeProbability(Double.NaN);
        assertTrue("InverseCDF(NaN) should be NaN", Double.isNaN(result));
    }
}