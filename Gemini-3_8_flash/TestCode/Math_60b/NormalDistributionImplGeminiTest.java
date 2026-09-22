package org.apache.commons.math.distribution;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.commons.math.MathException;
import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.apache.commons.math.exception.OutOfRangeException;
import org.apache.commons.math.util.FastMath;

/**
 * /* [Branch & Defect Analysis Matrix]
 * =========================================================================
 * Target: org.apache.commons.math.distribution.NormalDistributionImpl
 * 
 * Branch Coverage Targets:
 * 1. Constructor sd validation:
 *    - sd <= 0 (throws NotStrictlyPositiveException) [sd = 0.0, sd < 0]
 *    - sd > 0 (successful instantiation)
 * 2. density(double x):
 *    - standard points (x == mean, x > mean, x < mean, extremes)
 * 3. cumulativeProbability(double x):
 *    - normal evaluations (Erf convergence path)
 *    - catch MaxIterationsExceededException branches:
 *      * x < mean - 20 * sd -> return 0.0
 *      * x > mean + 20 * sd -> return 1.0
 *      * within 20 * sd -> rethrow ex
 *    - DEFECT TARGET: Extreme values (e.g., x = Double.MAX_VALUE and -Double.MAX_VALUE)
 *      triggering ConvergenceException in Erf/Gamma fraction evaluation rather
 *      than returning 1.0 or 0.0 safely.
 * 4. inverseCumulativeProbability(double p):
 *    - p == 0 -> Double.NEGATIVE_INFINITY
 *    - p == 1 -> Double.POSITIVE_INFINITY
 *    - p < 0 or p > 1 -> OutOfRangeException (via super)
 *    - 0 < p < 1 -> super.inverseCumulativeProbability(p)
 * 5. getDomainLowerBound(double p):
 *    - p < 0.5 -> -Double.MAX_VALUE
 *    - p >= 0.5 -> mean
 * 6. getDomainUpperBound(double p):
 *    - p < 0.5 -> mean
 *    - p >= 0.5 -> Double.MAX_VALUE
 * 7. getInitialDomain(double p):
 *    - p < 0.5 -> mean - sd
 *    - p > 0.5 -> mean + sd
 *    - p == 0.5 -> mean
 * 8. sample():
 *    - Deterministic random sampling via seeded generator
 * 9. Serialization integrity:
 *    - Preserves mean, sd, and solverAbsoluteAccuracy state
 * =========================================================================
 */
public class NormalDistributionImplGeminiTest {

    private static final double TOLERANCE = 1e-7;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        NormalDistributionImpl dist = new NormalDistributionImpl();
        assertEquals(0.0, dist.getMean(), 0.0);
        assertEquals(1.0, dist.getStandardDeviation(), 0.0);
        assertEquals(NormalDistributionImpl.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, dist.getSolverAbsoluteAccuracy(), 0.0);
    }

    @Test(timeout = 4000)
    public void testTwoArgConstructor() {
        NormalDistributionImpl dist = new NormalDistributionImpl(4.5, 2.5);
        assertEquals(4.5, dist.getMean(), 0.0);
        assertEquals(2.5, dist.getStandardDeviation(), 0.0);
        assertEquals(NormalDistributionImpl.DEFAULT_INVERSE_ABSOLUTE_ACCURACY, dist.getSolverAbsoluteAccuracy(), 0.0);
    }

    @Test(timeout = 4000)
    public void testThreeArgConstructor() {
        NormalDistributionImpl dist = new NormalDistributionImpl(-10.0, 0.5, 1e-12);
        assertEquals(-10.0, dist.getMean(), 0.0);
        assertEquals(0.5, dist.getStandardDeviation(), 0.0);
        assertEquals(1e-12, dist.getSolverAbsoluteAccuracy(), 0.0);
    }

    @Test(timeout = 4000)
    public void testDensityStandardNormal() {
        NormalDistributionImpl dist = new NormalDistributionImpl(0.0, 1.0);
        double expectedAtZero = 1.0 / FastMath.sqrt(2.0 * FastMath.PI);
        assertEquals(expectedAtZero, dist.density(0.0), TOLERANCE);

        double expectedAtOne = FastMath.exp(-0.5) / FastMath.sqrt(2.0 * FastMath.PI);
        assertEquals(expectedAtOne, dist.density(1.0), TOLERANCE);
        assertEquals(expectedAtOne, dist.density(-1.0), TOLERANCE);
    }

    @Test(timeout = 4000)
    public void testDensityShiftedAndScaled() {
        NormalDistributionImpl dist = new NormalDistributionImpl(10.0, 2.0);
        double expectedAtMean = 1.0 / (2.0 * FastMath.sqrt(2.0 * FastMath.PI));
        assertEquals(expectedAtMean, dist.density(10.0), TOLERANCE);

        double expectedAtOneSd = FastMath.exp(-0.5) / (2.0 * FastMath.sqrt(2.0 * FastMath.PI));
        assertEquals(expectedAtOneSd, dist.density(12.0), TOLERANCE);
        assertEquals(expectedAtOneSd, dist.density(8.0), TOLERANCE);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityStandard() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0.0, 1.0);
        assertEquals(0.5, dist.cumulativeProbability(0.0), TOLERANCE);
        assertEquals(0.8413447, dist.cumulativeProbability(1.0), TOLERANCE);
        assertEquals(0.1586553, dist.cumulativeProbability(-1.0), TOLERANCE);
        assertEquals(0.9772499, dist.cumulativeProbability(2.0), TOLERANCE);
        assertEquals(0.0227501, dist.cumulativeProbability(-2.0), TOLERANCE);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityStandard() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0.0, 1.0);
        assertEquals(0.0, dist.inverseCumulativeProbability(0.5), TOLERANCE);
        assertEquals(1.0, dist.inverseCumulativeProbability(0.8413447), TOLERANCE);
        assertEquals(-1.0, dist.inverseCumulativeProbability(0.1586553), TOLERANCE);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityInfiniteEndpoints() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(5.0, 3.0);
        assertEquals(Double.NEGATIVE_INFINITY, dist.inverseCumulativeProbability(0.0), 0.0);
        assertEquals(Double.POSITIVE_INFINITY, dist.inverseCumulativeProbability(1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testSample() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(100.0, 15.0);
        dist.reseedRandomGenerator(123456789L);
        double sample1 = dist.sample();
        double sample2 = dist.sample();
        assertFalse(Double.isNaN(sample1));
        assertFalse(Double.isInfinite(sample1));
        assertFalse(Double.isNaN(sample2));
        assertFalse(Double.isInfinite(sample2));
        assertNotEquals(sample1, sample2, 1e-6);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Internal Domain Bounds
    // =========================================================================

    @Test(timeout = 4000)
    public void testGetDomainLowerBound() {
        NormalDistributionImpl dist = new NormalDistributionImpl(12.5, 3.0);
        assertEquals(-Double.MAX_VALUE, dist.getDomainLowerBound(0.0), 0.0);
        assertEquals(-Double.MAX_VALUE, dist.getDomainLowerBound(0.25), 0.0);
        assertEquals(-Double.MAX_VALUE, dist.getDomainLowerBound(0.499999), 0.0);
        assertEquals(12.5, dist.getDomainLowerBound(0.5), 0.0);
        assertEquals(12.5, dist.getDomainLowerBound(0.75), 0.0);
        assertEquals(12.5, dist.getDomainLowerBound(1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetDomainUpperBound() {
        NormalDistributionImpl dist = new NormalDistributionImpl(12.5, 3.0);
        assertEquals(12.5, dist.getDomainUpperBound(0.0), 0.0);
        assertEquals(12.5, dist.getDomainUpperBound(0.25), 0.0);
        assertEquals(12.5, dist.getDomainUpperBound(0.499999), 0.0);
        assertEquals(Double.MAX_VALUE, dist.getDomainUpperBound(0.5), 0.0);
        assertEquals(Double.MAX_VALUE, dist.getDomainUpperBound(0.75), 0.0);
        assertEquals(Double.MAX_VALUE, dist.getDomainUpperBound(1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetInitialDomain() {
        double mean = 10.0;
        double sd = 2.0;
        NormalDistributionImpl dist = new NormalDistributionImpl(mean, sd);
        assertEquals(mean - sd, dist.getInitialDomain(0.25), 0.0);
        assertEquals(mean, dist.getInitialDomain(0.5), 0.0);
        assertEquals(mean + sd, dist.getInitialDomain(0.75), 0.0);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Regressions)
    // =========================================================================

    /**
     * Targets Defects4J known failure:
     * NormalDistributionTest::testExtremeValues
     * org.apache.commons.math.ConvergenceException: Continued fraction diverged to NaN for value ∞
     *
     * Evaluating cumulativeProbability at Double.MAX_VALUE and -Double.MAX_VALUE
     * must not crash with Divergence/ConvergenceException.
     */
    @Test(timeout = 4000)
    public void testExtremeValues() throws MathException {
        NormalDistribution distribution = new NormalDistributionImpl(0, 1);
        for (int i = 0; i < 10; i++) {
            double maxVal = distribution.cumulativeProbability(Double.MAX_VALUE);
            assertEquals("cumulative probability of Double.MAX_VALUE should be 1", 1.0, maxVal, 0.0);

            double minVal = distribution.cumulativeProbability(-Double.MAX_VALUE);
            assertEquals("cumulative probability of -Double.MAX_VALUE should be 0", 0.0, minVal, 0.0);
        }
    }

    @Test(timeout = 4000)
    public void testExtremeValuesWithCustomDistribution() throws MathException {
        NormalDistribution dist = new NormalDistributionImpl(1000.0, 50.0);
        assertEquals(1.0, dist.cumulativeProbability(Double.MAX_VALUE), 0.0);
        assertEquals(0.0, dist.cumulativeProbability(-Double.MAX_VALUE), 0.0);
        assertEquals(1.0, dist.cumulativeProbability(1000.0 + 50.0 * 50.0), 0.0);
        assertEquals(0.0, dist.cumulativeProbability(1000.0 - 50.0 * 50.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityVeryFarOutliers() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0.0, 1.0);
        assertEquals(0.0, dist.cumulativeProbability(-40.0), TOLERANCE);
        assertEquals(1.0, dist.cumulativeProbability(40.0), TOLERANCE);
        assertEquals(0.0, dist.cumulativeProbability(-100.0), TOLERANCE);
        assertEquals(1.0, dist.cumulativeProbability(100.0), TOLERANCE);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorZeroStandardDeviationThrows() {
        new NormalDistributionImpl(0.0, 0.0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testConstructorNegativeStandardDeviationThrows() {
        new NormalDistributionImpl(0.0, -1.0);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testThreeArgConstructorZeroSdThrows() {
        new NormalDistributionImpl(5.0, 0.0, 1e-8);
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testThreeArgConstructorNegativeSdThrows() {
        new NormalDistributionImpl(5.0, -0.0001, 1e-8);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testInverseCumulativeProbabilityNegativeThrows() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0.0, 1.0);
        dist.inverseCumulativeProbability(-0.01);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testInverseCumulativeProbabilityGreaterThanOneThrows() throws MathException {
        NormalDistributionImpl dist = new NormalDistributionImpl(0.0, 1.0);
        dist.inverseCumulativeProbability(1.01);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerialization() throws Exception {
        NormalDistributionImpl original = new NormalDistributionImpl(3.1415, 2.718, 1e-11);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        NormalDistributionImpl restored = (NormalDistributionImpl) ois.readObject();
        ois.close();

        assertEquals(original.getMean(), restored.getMean(), 0.0);
        assertEquals(original.getStandardDeviation(), restored.getStandardDeviation(), 0.0);
        assertEquals(original.getSolverAbsoluteAccuracy(), restored.getSolverAbsoluteAccuracy(), 0.0);

        assertEquals(original.cumulativeProbability(3.1415), restored.cumulativeProbability(3.1415), TOLERANCE);
        assertEquals(original.density(3.1415), restored.density(3.1415), TOLERANCE);
    }
}