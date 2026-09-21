package org.apache.commons.math.distribution;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Test class for FDistributionImpl.
 * 
 * [Branch & Defect Analysis Matrix]
 * 
 * Partition A: Core Functional Logic & State Transitions
 *   - cumulativeProbability(x <= 0) -> 0.0
 *   - cumulativeProbability(x > 0) -> Beta.regularizedBeta(...)
 *   - inverseCumulativeProbability(p == 0) -> 0.0
 *   - inverseCumulativeProbability(p == 1) -> Double.POSITIVE_INFINITY
 *   - inverseCumulativeProbability(0 < p < 1) -> super.inverseCumulativeProbability(p)
 *   - getDomainLowerBound(p) -> 0.0
 *   - getDomainUpperBound(p) -> Double.MAX_VALUE
 *   - getInitialDomain(p) -> d / (d - 2.0)  (defect: for d <= 2, returns negative or infinite)
 * 
 * Partition B: Boundary Value Analysis & Extremes
 *   - Degrees of freedom: very small (1.0, 1.0), large (1e10), just above zero (1e-10)
 *   - x values: 0.0, positive, negative, Double.MAX_VALUE
 *   - p values: 0.0, 1.0, 0.5, 0.0 + epsilon, 1.0 - epsilon
 * 
 * Partition C: Defect-Targeted Branch Zone
 *   - Known defect: inverseCumulativeProbability with small denominator df (e.g., 1.0)
 *     causes IllegalArgumentException due to invalid bracket [0.0, initial=-1.0, upperBound=Double.MAX_VALUE]
 * 
 * Partition D: Exception & Defensive Guard Paths
 *   - setNumeratorDegreesOfFreedom(<=0) -> IllegalArgumentException
 *   - setDenominatorDegreesOfFreedom(<=0) -> IllegalArgumentException
 *   - Constructor with invalid degrees of freedom -> IllegalArgumentException
 * 
 * Partition E: Object Lifecycle & Contract Integrity
 *   - Constructor sets state correctly
 *   - Getters return set values
 *   - Serialization (implicit via Serializable)
 */
public class FDistributionImplDeepseekTest {

    // ---------- Partition A: Core Functional Logic ----------

    @Test(timeout = 4000)
    public void testCumulativeProbabilityXLessThanOrEqualToZero() throws Exception {
        FDistributionImpl dist = new FDistributionImpl(1.0, 1.0);
        assertEquals(0.0, dist.cumulativeProbability(0.0), 1e-15);
        assertEquals(0.0, dist.cumulativeProbability(-1.0), 1e-15);
        assertEquals(0.0, dist.cumulativeProbability(-Double.MAX_VALUE), 1e-15);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityPositiveX() throws Exception {
        FDistributionImpl dist = new FDistributionImpl(10.0, 20.0);
        double x = 2.0;
        double expected = Beta.regularizedBeta((10.0 * x) / (20.0 + 10.0 * x), 0.5 * 10.0, 0.5 * 20.0);
        double actual = dist.cumulativeProbability(x);
        assertEquals(expected, actual, 1e-15);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityPZero() throws Exception {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        assertEquals(0.0, dist.inverseCumulativeProbability(0.0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityPOne() throws Exception {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        assertEquals(Double.POSITIVE_INFINITY, dist.inverseCumulativeProbability(1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityNormal() throws Exception {
        FDistributionImpl dist = new FDistributionImpl(10.0, 20.0);
        double p = 0.5;
        double result = dist.inverseCumulativeProbability(p);
        // Should return a finite positive value
        assertTrue("Result should be positive", result > 0.0);
        assertTrue("Result should be finite", Double.isFinite(result));
    }

    @Test(timeout = 4000)
    public void testGetDomainLowerBound() {
        FDistributionImpl dist = new FDistributionImpl(1.0, 1.0);
        assertEquals(0.0, dist.getDomainLowerBound(0.5), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetDomainUpperBound() {
        FDistributionImpl dist = new FDistributionImpl(1.0, 1.0);
        assertEquals(Double.MAX_VALUE, dist.getDomainUpperBound(0.5), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetInitialDomain() {
        // For denominator df = 5.0, initial = 5.0 / (5.0 - 2.0) = 5.0/3.0 ≈ 1.6667
        FDistributionImpl dist = new FDistributionImpl(10.0, 5.0);
        double expected = 5.0 / (5.0 - 2.0);
        assertEquals(expected, dist.getInitialDomain(0.5), 1e-15);
    }

    // ---------- Partition B: Boundary Value Analysis ----------

    @Test(timeout = 4000)
    public void testCumulativeProbabilityLargeX() throws Exception {
        FDistributionImpl dist = new FDistributionImpl(1.0, 1.0);
        double x = Double.MAX_VALUE;
        double result = dist.cumulativeProbability(x);
        // Should be close to 1.0
        assertTrue("CDF at large x should be near 1", result > 0.999);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityVerySmallPositiveX() throws Exception {
        FDistributionImpl dist = new FDistributionImpl(100.0, 100.0);
        double x = 1e-10;
        double result = dist.cumulativeProbability(x);
        // Should be very small but positive
        assertTrue("CDF at very small x should be > 0", result > 0.0);
        assertTrue("CDF at very small x should be < 0.5", result < 0.5);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityExtremeP() throws Exception {
        FDistributionImpl dist = new FDistributionImpl(10.0, 10.0);
        double pEpsilon = 1e-15;
        double result = dist.inverseCumulativeProbability(pEpsilon);
        assertTrue("Result for tiny p should be small", result > 0.0);
        assertTrue("Result for tiny p should be finite", Double.isFinite(result));

        double pNearOne = 1.0 - 1e-15;
        result = dist.inverseCumulativeProbability(pNearOne);
        assertTrue("Result for p near 1 should be large", result > 1.0);
        assertTrue("Result for p near 1 should be finite", Double.isFinite(result));
    }

    // ---------- Partition C: Defect-Targeted Branch Zone ----------

    /**
     * This test targets the known defect: inverseCumulativeProbability with
     * small denominator degrees of freedom (e.g., 1.0) causes
     * IllegalArgumentException because getInitialDomain returns a negative value
     * (d/(d-2) = 1/(1-2) = -1.0), leading to invalid bracket [0.0, -1.0, Double.MAX_VALUE].
     * The test expects no exception and a valid result.
     */
    @Test(timeout = 4000)
    public void testSmallDegreesOfFreedomInverseCumulativeProbability() throws Exception {
        FDistributionImpl dist = new FDistributionImpl(1.0, 1.0);
        try {
            double result = dist.inverseCumulativeProbability(0.5);
            // If we reach here, no exception occurred (fixed version)
            assertTrue("Result should be positive", result > 0.0);
            assertTrue("Result should be finite", Double.isFinite(result));
        } catch (IllegalArgumentException e) {
            // On defective version, this exception is thrown -> test fails
            fail("Should not throw IllegalArgumentException for small degrees of freedom: " + e.getMessage());
        } catch (Exception e) {
            // Any other exception is also a failure
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // Additional test with numerator df small as well
    @Test(timeout = 4000)
    public void testBothDegreesOfFreedomSmall() throws Exception {
        FDistributionImpl dist = new FDistributionImpl(1.5, 1.5);
        try {
            double result = dist.inverseCumulativeProbability(0.75);
            assertTrue("Result should be positive", result > 0.0);
            assertTrue("Result should be finite", Double.isFinite(result));
        } catch (IllegalArgumentException e) {
            fail("Should not throw IllegalArgumentException for small degrees of freedom: " + e.getMessage());
        }
    }

    // ---------- Partition D: Exception & Defensive Guard Paths ----------

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetNumeratorDegreesOfFreedomZero() {
        FDistributionImpl dist = new FDistributionImpl(1.0, 1.0);
        dist.setNumeratorDegreesOfFreedom(0.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetNumeratorDegreesOfFreedomNegative() {
        FDistributionImpl dist = new FDistributionImpl(1.0, 1.0);
        dist.setNumeratorDegreesOfFreedom(-1.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDenominatorDegreesOfFreedomZero() {
        FDistributionImpl dist = new FDistributionImpl(1.0, 1.0);
        dist.setDenominatorDegreesOfFreedom(0.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDenominatorDegreesOfFreedomNegative() {
        FDistributionImpl dist = new FDistributionImpl(1.0, 1.0);
        dist.setDenominatorDegreesOfFreedom(-1.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNumeratorZero() {
        new FDistributionImpl(0.0, 1.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorDenominatorZero() {
        new FDistributionImpl(1.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorBothNegative() {
        new FDistributionImpl(-1.0, -1.0);
    }

    // ---------- Partition E: Object Lifecycle & Contract Integrity ----------

    @Test(timeout = 4000)
    public void testConstructorAndGetters() {
        FDistributionImpl dist = new FDistributionImpl(3.0, 7.0);
        assertEquals(3.0, dist.getNumeratorDegreesOfFreedom(), 1e-15);
        assertEquals(7.0, dist.getDenominatorDegreesOfFreedom(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSettersAndGetters() {
        FDistributionImpl dist = new FDistributionImpl(1.0, 1.0);
        dist.setNumeratorDegreesOfFreedom(5.0);
        dist.setDenominatorDegreesOfFreedom(10.0);
        assertEquals(5.0, dist.getNumeratorDegreesOfFreedom(), 1e-15);
        assertEquals(10.0, dist.getDenominatorDegreesOfFreedom(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetInitialDomainWithDenominatorLessThanTwo() {
        // For d=1.0, initial = 1.0/(1.0-2.0) = -1.0 (defect source)
        FDistributionImpl dist = new FDistributionImpl(1.0, 1.0);
        double initial = dist.getInitialDomain(0.5);
        assertEquals(-1.0, initial, 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetInitialDomainWithDenominatorEqualToTwo() {
        // For d=2.0, initial = 2.0/(2.0-2.0) = Infinity
        FDistributionImpl dist = new FDistributionImpl(1.0, 2.0);
        double initial = dist.getInitialDomain(0.5);
        assertEquals(Double.POSITIVE_INFINITY, initial, 0.0);
    }

    @Test(timeout = 4000)
    public void testGetInitialDomainWithDenominatorGreaterThanTwo() {
        FDistributionImpl dist = new FDistributionImpl(1.0, 5.0);
        double expected = 5.0 / (5.0 - 2.0);
        assertEquals(expected, dist.getInitialDomain(0.5), 1e-15);
    }
}