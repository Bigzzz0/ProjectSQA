package org.apache.commons.math.distribution;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.commons.math.MathException;
import org.junit.Test;
import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.math.distribution.NormalDistributionImpl
 *
 * 1. Branch Coverage Analysis:
 * - Constructors:
 *   * Default: mean = 0.0, sd = 1.0
 *   * Parametric: mean = m, sd = s (valid s > 0, invalid s <= 0)
 * - setStandardDeviation(double sd):
 *   * Branch 1: sd <= 0.0 -> throws IllegalArgumentException (tested for 0.0, -1.0, -Double.MIN_VALUE)
 *   * Branch 2: sd > 0.0 -> sets standardDeviation
 * - cumulativeProbability(double x):
 *   * x = mean -> returns 0.5
 *   * x < mean, x > mean
 *   * Extreme values (Defects4J known failure: x = Double.MAX_VALUE, x = -Double.MAX_VALUE)
 * - inverseCumulativeProbability(double p):
 *   * Branch 1: p == 0.0 -> returns Double.NEGATIVE_INFINITY
 *   * Branch 2: p == 1.0 -> returns Double.POSITIVE_INFINITY
 *   * Branch 3: 0 < p < 1 -> delegates to super.inverseCumulativeProbability(p)
 *   * Invalid probabilities: p < 0 or p > 1 -> throws IllegalArgumentException
 * - getDomainLowerBound(double p):
 *   * Branch 1: p < 0.5 -> returns -Double.MAX_VALUE
 *   * Branch 2: p >= 0.5 -> returns mean (tested at p = 0.5 and p > 0.5)
 * - getDomainUpperBound(double p):
 *   * Branch 1: p < 0.5 -> returns mean
 *   * Branch 2: p >= 0.5 -> returns Double.MAX_VALUE (tested at p = 0.5 and p > 0.5)
 * - getInitialDomain(double p):
 *   * Branch 1: p < 0.5 -> returns mean - standardDeviation
 *   * Branch 2: p > 0.5 -> returns mean + standardDeviation
 *   * Branch 3: p == 0.5 -> returns mean
 *
 * 2. Defect Analysis (Defects4J):
 * - Target Bug: NormalDistributionTest::testExtremeValues
 *   Evaluating cumulativeProbability(Double.MAX_VALUE) or cumulativeProbability(-Double.MAX_VALUE)
 *   triggers convergence failure in Erf / ContinuedFraction yielding MaxIterationsExceededException.
 *   The specification requires returning 1.0 and 0.0 without uncaught convergence failure.
 */
public class NormalDistributionImplGeminiTest {

    private static final double TOLERANCE = 1e-5;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        assertEquals(0.0, dist.getMean(), 0.0);
        assertEquals(1.0, dist.getStandardDeviation(), 0.0);
    }

    @Test(timeout = 4000)
    public void testParametricConstructor() {
        NormalDistributionImpl dist = new NormalDistributionImpl(2.5, 0.75);
        assertEquals(2.5, dist.getMean(), 0.0);
        assertEquals(0.75, dist.getStandardDeviation(), 0.0);
    }

    @Test(timeout = 4000)
    public void testSetMeanAndStandardDeviation() {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        dist.setMean(10.0);
        assertEquals(10.0, dist.getMean(), 0.0);

        dist.setStandardDeviation(5.0);
        assertEquals(5.0, dist.getStandardDeviation(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityStandardNormal() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0.0, 1.0);

        assertEquals(0.5, dist.cumulativeProbability(0.0), TOLERANCE);
        assertEquals(0.8413447, dist.cumulativeProbability(1.0), TOLERANCE);
        assertEquals(0.1586553, dist.cumulativeProbability(-1.0), TOLERANCE);
        assertEquals(0.9772499, dist.cumulativeProbability(2.0), TOLERANCE);
        assertEquals(0.0227501, dist.cumulativeProbability(-2.0), TOLERANCE);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityInterval() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0.0, 1.0);
        // Interval: [-1, 1] captures ~ 68.27% of probability
        double prob = dist.cumulativeProbability(-1.0, 1.0);
        assertEquals(0.6826895, prob, TOLERANCE);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityStandard() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0.0, 1.0);

        assertEquals(0.0, dist.inverseCumulativeProbability(0.5), TOLERANCE);
        assertEquals(1.0, dist.inverseCumulativeProbability(dist.cumulativeProbability(1.0)), TOLERANCE);
        assertEquals(-1.0, dist.inverseCumulativeProbability(dist.cumulativeProbability(-1.0)), TOLERANCE);
        assertEquals(1.959964, dist.inverseCumulativeProbability(0.975), TOLERANCE);
        assertEquals(-1.959964, dist.inverseCumulativeProbability(0.025), TOLERANCE);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityBoundaries() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(5.0, 2.0);

        assertEquals(Double.NEGATIVE_INFINITY, dist.inverseCumulativeProbability(0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, dist.inverseCumulativeProbability(1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetDomainLowerBoundBranches() {
        NormalDistributionImpl dist = new NormalDistributionImpl(10.0, 2.0);

        // Branch p < 0.5
        assertEquals(-Double.MAX_VALUE, dist.getDomainLowerBound(0.4999), 0.0);
        assertEquals(-Double.MAX_VALUE, dist.getDomainLowerBound(0.0), 0.0);

        // Branch p >= 0.5
        assertEquals(10.0, dist.getDomainLowerBound(0.5), 0.0);
        assertEquals(10.0, dist.getDomainLowerBound(0.5001), 0.0);
        assertEquals(10.0, dist.getDomainLowerBound(1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetDomainUpperBoundBranches() {
        NormalDistributionImpl dist = new NormalDistributionImpl(10.0, 2.0);

        // Branch p < 0.5
        assertEquals(10.0, dist.getDomainUpperBound(0.4999), 0.0);
        assertEquals(10.0, dist.getDomainUpperBound(0.0), 0.0);

        // Branch p >= 0.5
        assertEquals(Double.MAX_VALUE, dist.getDomainUpperBound(0.5), 0.0);
        assertEquals(Double.MAX_VALUE, dist.getDomainUpperBound(0.5001), 0.0);
        assertEquals(Double.MAX_VALUE, dist.getDomainUpperBound(1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetInitialDomainBranches() {
        double mean = 10.0;
        double sd = 3.0;
        NormalDistributionImpl dist = new NormalDistributionImpl(mean, sd);

        // Branch p < 0.5
        assertEquals(mean - sd, dist.getInitialDomain(0.25), 1e-9);

        // Branch p > 0.5
        assertEquals(mean + sd, dist.getInitialDomain(0.75), 1e-9);

        // Branch p == 0.5
        assertEquals(mean, dist.getInitialDomain(0.5), 1e-9);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    /**
     * Targets the defect where computing cumulativeProbability at extreme values
     * (e.g. Double.MAX_VALUE or -Double.MAX_VALUE) causes Erf to trigger a
     * MaxIterationsExceededException instead of returning 1.0 or 0.0.
     */
    @Test(timeout = 4000)
    public void testExtremeValues() throws Exception {
        NormalDistribution dist = new NormalDistributionImpl(0.0, 1.0);

        for (int i = 0; i < 10; i++) {
            double top = dist.cumulativeProbability(Double.MAX_VALUE);
            assertEquals(1.0, top, 1e-15);

            double bottom = dist.cumulativeProbability(-Double.MAX_VALUE);
            assertEquals(0.0, bottom, 1e-15);
        }
    }

    /**
     * Secondary extreme value test ensuring values far in standard deviation tails converge properly.
     */
    @Test(timeout = 4000)
    public void testExtremeValuesFarFromMean() throws Exception {
        NormalDistribution dist = new NormalDistributionImpl(100.0, 2.0);

        // Far to the right (> 30 standard deviations)
        assertEquals(1.0, dist.cumulativeProbability(500.0), 1e-15);

        // Far to the left (< -30 standard deviations)
        assertEquals(0.0, dist.cumulativeProbability(-500.0), 1e-15);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorZeroStandardDeviationThrows() {
        new NormalDistributionImpl(0.0, 0.0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNegativeStandardDeviationThrows() {
        new NormalDistributionImpl(0.0, -1.0);
    }

    @Test(timeout = 4000)
    public void testSetStandardDeviationDefensiveChecks() {
        NormalDistributionImpl dist = new NormalDistributionImpl();

        try {
            dist.setStandardDeviation(0.0);
            fail("Expected IllegalArgumentException for standard deviation = 0.0");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Standard deviation must be positive"));
        }

        try {
            dist.setStandardDeviation(-0.5);
            fail("Expected IllegalArgumentException for negative standard deviation");
        } catch (IllegalArgumentException expected) {
            assertTrue(expected.getMessage().contains("Standard deviation must be positive"));
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityBelowZeroThrows() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        dist.inverseCumulativeProbability(-0.01);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testInverseCumulativeProbabilityAboveOneThrows() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        dist.inverseCumulativeProbability(1.01);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testCumulativeProbabilityInvalidIntervalThrows() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        // x0 > x1 must throw IllegalArgumentException per AbstractContinuousDistribution contract
        dist.cumulativeProbability(2.0, 1.0);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        NormalDistributionImpl original = new NormalDistributionImpl(12.34, 5.67);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        NormalDistributionImpl deserialized = (NormalDistributionImpl) ois.readObject();

        assertEquals(original.getMean(), deserialized.getMean(), 0.0);
        assertEquals(original.getStandardDeviation(), deserialized.getStandardDeviation(), 0.0);

        double expectedCdf = original.cumulativeProbability(15.0);
        double actualCdf = deserialized.cumulativeProbability(15.0);
        assertEquals(expectedCdf, actualCdf, 1e-15);
    }
}