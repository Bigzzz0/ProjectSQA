package org.apache.commons.math.distribution;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * White-box test suite for NormalDistributionImpl.
 * <p>
 * [Branch & Defect Analysis Matrix]
 * <ul>
 *   <li>Constructor: mean, sd -> setMean, setStandardDeviation</li>
 *   <li>setStandardDeviation: if sd <= 0 throw IllegalArgumentException</li>
 *   <li>cumulativeProbability: uses Erf.erf; branch at x - mean / (sd * sqrt(2))</li>
 *   <li>inverseCumulativeProbability: branches for p==0 (return -Inf), p==1 (return +Inf), else call super</li>
 *   <li>getDomainLowerBound: if p < 0.5 ret -Double.MAX_VALUE else ret mean</li>
 *   <li>getDomainUpperBound: if p < 0.5 ret mean else ret Double.MAX_VALUE</li>
 *   <li>getInitialDomain: if p < 0.5 ret mean - sd, else if p > 0.5 ret mean + sd, else ret mean</li>
 *   <li>Defect: Extreme values near 0 or 1 cause MaxIterationsExceededException in inverseCumulativeProbability</li>
 * </ul>
 * </p>
 */
public class NormalDistributionImplDeepseekTest {

    private static final double DELTA = 1e-12;

    // ========== Partition A: Core Functional & State Transitions ==========

    @Test(timeout = 4000)
    public void testConstructorAndGetters() {
        NormalDistributionImpl dist = new NormalDistributionImpl(2.0, 3.0);
        assertEquals("mean", 2.0, dist.getMean(), DELTA);
        assertEquals("sd", 3.0, dist.getStandardDeviation(), DELTA);
    }

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        assertEquals("mean default", 0.0, dist.getMean(), DELTA);
        assertEquals("sd default", 1.0, dist.getStandardDeviation(), DELTA);
    }

    @Test(timeout = 4000)
    public void testSetMeanAndSetters() {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        dist.setMean(5.0);
        assertEquals(5.0, dist.getMean(), DELTA);
        dist.setStandardDeviation(2.0);
        assertEquals(2.0, dist.getStandardDeviation(), DELTA);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetStandardDeviationNegative() {
        new NormalDistributionImpl(0, -1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetStandardDeviationZero() {
        new NormalDistributionImpl(0, 0);
    }

    // ========== Partition B: Boundary Value Analysis & Extremes ==========

    @Test(timeout = 4000)
    public void testCumulativeProbabilityAtMean() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl(10, 2);
        double p = dist.cumulativeProbability(10);
        assertEquals("CDF at mean", 0.5, p, DELTA);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityNegativeExtreme() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        double p = dist.cumulativeProbability(-1e10);
        assertEquals("CDF very negative", 0.0, p, 1e-15);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityPositiveExtreme() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        double p = dist.cumulativeProbability(1e10);
        assertEquals("CDF very positive", 1.0, p, 1e-15);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityOneSigma() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        double p = dist.cumulativeProbability(1.0);
        // Known: 0.841344746...
        assertEquals(0.8413447460685429, p, 1e-10);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityMinusOneSigma() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        double p = dist.cumulativeProbability(-1.0);
        assertEquals(0.15865525393145707, p, 1e-10);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    /**
     * Test inverseCumulativeProbability with p extremely close to 0.
     * This should NOT throw MaxIterationsExceededException on a fixed version.
     * On defective version it will throw, hence reveals the bug.
     */
    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityVerySmallP() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        double p = 1e-15;
        double x = dist.inverseCumulativeProbability(p);
        assertTrue("Inverse for very small p should be very negative", x < -7.0);
        // Also check that cumulativeProbability returns approximately p
        double cdf = dist.cumulativeProbability(x);
        assertEquals("Inverse/CDF round-trip", p, cdf, 1e-12);
    }

    /**
     * Test inverseCumulativeProbability with p extremely close to 1.
     */
    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityVeryLargeP() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        double p = 1 - 1e-15;
        double x = dist.inverseCumulativeProbability(p);
        assertTrue("Inverse for very large p should be very positive", x > 7.0);
        double cdf = dist.cumulativeProbability(x);
        assertEquals("Inverse/CDF round-trip", p, cdf, 1e-12);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityP0() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        double x = dist.inverseCumulativeProbability(0);
        assertEquals(Double.NEGATIVE_INFINITY, x, 0.0);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityP1() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl(0, 1);
        double x = dist.inverseCumulativeProbability(1);
        assertEquals(Double.POSITIVE_INFINITY, x, 0.0);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityP05() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl(5, 2);
        double x = dist.inverseCumulativeProbability(0.5);
        assertEquals("Inverse of 0.5 is mean", 5.0, x, 1e-10);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityInvalidPLow() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        dist.inverseCumulativeProbability(-0.1);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityInvalidPHigh() throws Exception {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        dist.inverseCumulativeProbability(1.1);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testGetDomainLowerBound() {
        NormalDistributionImpl dist = new NormalDistributionImpl(10, 2);
        double lowP = dist.getDomainLowerBound(0.3);
        assertEquals("domain lower bound p<0.5 should be -Double.MAX_VALUE", -Double.MAX_VALUE, lowP, 0.0);

        double highP = dist.getDomainLowerBound(0.7);
        assertEquals("domain lower bound p>=0.5 should be mean", 10.0, highP, DELTA);

        double exactP = dist.getDomainLowerBound(0.5);
        assertEquals("domain lower bound p=0.5 should be mean", 10.0, exactP, DELTA);
    }

    @Test(timeout = 4000)
    public void testGetDomainUpperBound() {
        NormalDistributionImpl dist = new NormalDistributionImpl(10, 2);
        double lowP = dist.getDomainUpperBound(0.3);
        assertEquals("domain upper bound p<0.5 should be mean", 10.0, lowP, DELTA);

        double highP = dist.getDomainUpperBound(0.7);
        assertEquals("domain upper bound p>=0.5 should be Double.MAX_VALUE", Double.MAX_VALUE, highP, 0.0);

        double exactP = dist.getDomainUpperBound(0.5);
        assertEquals("domain upper bound p=0.5 should be Double.MAX_VALUE", Double.MAX_VALUE, exactP, 0.0);
    }

    @Test(timeout = 4000)
    public void testGetInitialDomain() {
        NormalDistributionImpl dist = new NormalDistributionImpl(10, 2);
        double lowP = dist.getInitialDomain(0.3);
        assertEquals("initial domain p<0.5 should be mean - sd", 8.0, lowP, DELTA);

        double highP = dist.getInitialDomain(0.7);
        assertEquals("initial domain p>0.5 should be mean + sd", 12.0, highP, DELTA);

        double exactP = dist.getInitialDomain(0.5);
        assertEquals("initial domain p=0.5 should be mean", 10.0, exactP, DELTA);
    }
}