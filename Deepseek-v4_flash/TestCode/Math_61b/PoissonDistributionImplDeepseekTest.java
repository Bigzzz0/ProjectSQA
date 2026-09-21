package org.apache.commons.math.distribution;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target class: PoissonDistributionImpl
 * 
 * Decision branches covered:
 * - Constructor: p <= 0 (throw exception) vs p > 0 (normal path)
 * - probability(int x): x < 0 || x == Integer.MAX_VALUE -> return 0.0
 *                      x == 0 -> return exp(-mean)
 *                      else -> saddle point approximation
 * - cumulativeProbability(int x): x < 0 -> return 0
 *                                 x == Integer.MAX_VALUE -> return 1
 *                                 else -> Gamma.regularizedGammaQ(x+1, mean, epsilon, maxIterations)
 * - normalApproximateProbability(int x): delegates to normal.cumulativeProbability(x+0.5)
 * - sample(): delegates to randomData.nextPoisson(mean) (not tested directly due to randomness)
 * - getDomainLowerBound(p): always returns 0
 * - getDomainUpperBound(p): always returns Integer.MAX_VALUE
 * 
 * Boundary conditions:
 * - mean: positive, zero (invalid), negative (invalid), very large
 * - x: negative, 0, 1, Integer.MAX_VALUE, large positive
 * - epsilon: default, custom positive, zero (invalid? but not checked)
 * - maxIterations: default, custom positive, zero (invalid? but not checked)
 * 
 * Known defect (Defects4J): Constructor with negative mean throws MathRuntimeException instead of IllegalArgumentException.
 * We target this by expecting IllegalArgumentException.
 */
public class PoissonDistributionImplDeepseekTest {

    /* Partition A: Core Functional Logic & State Transitions */
    @Test(timeout = 4000)
    public void testConstructorAndGetMean() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(5.0);
        assertEquals("Mean should be 5.0", 5.0, dist.getMean(), 1e-12);
        // Verify default epsilon and maxIterations
        assertEquals("Default epsilon", PoissonDistributionImpl.DEFAULT_EPSILON, dist.epsilon, 1e-12);
        assertEquals("Default maxIterations", PoissonDistributionImpl.DEFAULT_MAX_ITERATIONS, dist.maxIterations);
    }

    @Test(timeout = 4000)
    public void testConstructorWithEpsilonAndMaxIterations() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(2.0, 1e-10, 5000);
        assertEquals(2.0, dist.getMean(), 1e-12);
        assertEquals(1e-10, dist.epsilon, 1e-12);
        assertEquals(5000, dist.maxIterations);
    }

    @Test(timeout = 4000)
    public void testConstructorWithEpsilonOnly() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(3.0, 1e-8);
        assertEquals(3.0, dist.getMean(), 1e-12);
        assertEquals(1e-8, dist.epsilon, 1e-12);
        assertEquals(PoissonDistributionImpl.DEFAULT_MAX_ITERATIONS, dist.maxIterations);
    }

    @Test(timeout = 4000)
    public void testConstructorWithMaxIterationsOnly() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(4.0, 1000);
        assertEquals(4.0, dist.getMean(), 1e-12);
        assertEquals(PoissonDistributionImpl.DEFAULT_EPSILON, dist.epsilon, 1e-12);
        assertEquals(1000, dist.maxIterations);
    }

    /* Partition B: Boundary Value Analysis & Extremes */
    @Test(timeout = 4000)
    public void testProbabilityNegativeX() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(1.0);
        assertEquals("Probability for x < 0 should be 0", 0.0, dist.probability(-1), 1e-12);
        assertEquals("Probability for x = -100", 0.0, dist.probability(-100), 1e-12);
    }

    @Test(timeout = 4000)
    public void testProbabilityXIsMaxValue() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(1.0);
        assertEquals("Probability for x = Integer.MAX_VALUE should be 0", 0.0, dist.probability(Integer.MAX_VALUE), 1e-12);
    }

    @Test(timeout = 4000)
    public void testProbabilityXZero() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(2.0);
        double expected = Math.exp(-2.0);
        assertEquals("Probability for x=0", expected, dist.probability(0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testProbabilityPositiveX() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(5.0);
        double prob = dist.probability(3);
        // Expected value from Poisson PMF: e^-5 * 5^3 / 6 = 0.140373...
        double expected = Math.exp(-5) * 125 / 6.0;
        assertEquals("Probability for x=3, mean=5", expected, prob, 1e-10);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityNegativeX() throws Exception {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(1.0);
        assertEquals("CDF for x < 0 should be 0", 0.0, dist.cumulativeProbability(-5), 1e-12);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityXIsMaxValue() throws Exception {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(1.0);
        assertEquals("CDF for x = Integer.MAX_VALUE should be 1", 1.0, dist.cumulativeProbability(Integer.MAX_VALUE), 1e-12);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityNormalValues() throws Exception {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(2.0);
        // For x=0: P(X<=0) = e^-2 ≈ 0.135335
        double cdf0 = dist.cumulativeProbability(0);
        assertEquals(0.1353352832366127, cdf0, 1e-10);
        // For x=1: P(X<=1) = e^-2 + 2e^-2 = 3e^-2 ≈ 0.406005
        double cdf1 = dist.cumulativeProbability(1);
        assertEquals(0.4060058497098381, cdf1, 1e-10);
    }

    @Test(timeout = 4000)
    public void testNormalApproximateProbability() throws Exception {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(10.0);
        double approx = dist.normalApproximateProbability(5);
        // Normal approximation with half-correction: N(10, sqrt(10)) at 5.5
        // We just check it's between 0 and 1
        assertTrue("Normal approx should be between 0 and 1", approx >= 0.0 && approx <= 1.0);
    }

    @Test(timeout = 4000)
    public void testGetDomainLowerBound() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(1.0);
        assertEquals("Domain lower bound always 0", 0, dist.getDomainLowerBound(0.5));
        assertEquals("Domain lower bound always 0", 0, dist.getDomainLowerBound(0.0));
        assertEquals("Domain lower bound always 0", 0, dist.getDomainLowerBound(1.0));
    }

    @Test(timeout = 4000)
    public void testGetDomainUpperBound() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(1.0);
        assertEquals("Domain upper bound always Integer.MAX_VALUE", Integer.MAX_VALUE, dist.getDomainUpperBound(0.5));
    }

    /* Partition C: Defect-Targeted Branch Zone */
    @Test(timeout = 4000)
    public void testConstructorNegativeMeanThrowsIllegalArgumentException() {
        // Known defect: defective version throws MathRuntimeException instead of IllegalArgumentException
        try {
            new PoissonDistributionImpl(-1.0);
            fail("Expected IllegalArgumentException for negative mean");
        } catch (IllegalArgumentException e) {
            // Expected: correct behavior
            assertTrue("Exception message should contain 'Poisson mean'", e.getMessage().contains("Poisson mean"));
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName());
        }
    }

    @Test(timeout = 4000)
    public void testConstructorZeroMeanThrowsIllegalArgumentException() {
        try {
            new PoissonDistributionImpl(0.0);
            fail("Expected IllegalArgumentException for zero mean");
        } catch (IllegalArgumentException e) {
            // Expected
        } catch (Exception e) {
            fail("Unexpected exception type: " + e.getClass().getName());
        }
    }

    /* Partition D: Exception & Defensive Guard Paths */
    @Test(timeout = 4000)
    public void testCumulativeProbabilityThrowsMathExceptionForInvalidArgs() throws Exception {
        // The method itself does not throw for invalid x, but Gamma.regularizedGammaQ might throw
        // We test with a very large mean and x to potentially trigger convergence issues
        // This is a stress test; if it throws, we catch and verify it's a MathException
        PoissonDistributionImpl dist = new PoissonDistributionImpl(1000.0, 1e-12, 100);
        try {
            double cdf = dist.cumulativeProbability(500);
            // If no exception, just check value is reasonable
            assertTrue("CDF should be between 0 and 1", cdf >= 0.0 && cdf <= 1.0);
        } catch (org.apache.commons.math.MathException e) {
            // Acceptable if convergence fails
        }
    }

    @Test(timeout = 4000)
    public void testProbabilityLargeX() {
        // For large x, saddle point approximation should still work
        PoissonDistributionImpl dist = new PoissonDistributionImpl(100.0);
        double prob = dist.probability(200);
        // Probability should be very small but positive
        assertTrue("Probability should be positive", prob > 0.0);
        assertTrue("Probability should be less than 1", prob < 1.0);
    }

    /* Partition E: Object Lifecycle & Contract Integrity */
    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(3.5);
        // Serialize and deserialize (using Java serialization)
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(dist);
        oos.flush();
        java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(bis);
        PoissonDistributionImpl deserialized = (PoissonDistributionImpl) ois.readObject();
        assertEquals("Mean after deserialization", dist.getMean(), deserialized.getMean(), 1e-12);
        assertEquals("Epsilon after deserialization", dist.epsilon, deserialized.epsilon, 1e-12);
        assertEquals("MaxIterations after deserialization", dist.maxIterations, deserialized.maxIterations);
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        // PoissonDistributionImpl does not override equals/hashCode, so default reference equality
        PoissonDistributionImpl dist1 = new PoissonDistributionImpl(2.0);
        PoissonDistributionImpl dist2 = new PoissonDistributionImpl(2.0);
        assertFalse("Different objects should not be equal", dist1.equals(dist2));
        assertTrue("Same object should be equal", dist1.equals(dist1));
        // HashCode consistency
        assertEquals("HashCode should be same for same object", dist1.hashCode(), dist1.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(1.5);
        String str = dist.toString();
        assertNotNull("toString should not be null", str);
        assertTrue("toString should contain mean", str.contains("1.5"));
    }
}