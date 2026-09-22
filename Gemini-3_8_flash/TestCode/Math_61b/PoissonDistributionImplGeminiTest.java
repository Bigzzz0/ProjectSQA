package org.apache.commons.math.distribution;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.math.MathException;
import org.apache.commons.math.exception.NotStrictlyPositiveException;
import org.apache.commons.math.util.FastMath;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.math.distribution.PoissonDistributionImpl
 *
 * 1. Constructor Branches:
 *    - p <= 0: Throws NotStrictlyPositiveException (Defects4J Math-61 ground truth).
 *    - p > 0: Successfully creates instance; initializes mean, normal (mean, sqrt(mean)),
 *             epsilon, and maxIterations across all 4 overloaded constructors.
 *
 * 2. probability(int x) Branches:
 *    - Branch 1: (x < 0 || x == Integer.MAX_VALUE) -> returns 0.0
 *      - Sub-conditions: x = -1, x = Integer.MIN_VALUE, x = Integer.MAX_VALUE
 *    - Branch 2: (x == 0) -> returns FastMath.exp(-mean)
 *    - Branch 3: (x > 0 && x != Integer.MAX_VALUE) -> calculates SaddlePointExpansion probability
 *
 * 3. cumulativeProbability(int x) Branches:
 *    - Branch 1: (x < 0) -> returns 0.0 (x = -1, x = Integer.MIN_VALUE)
 *    - Branch 2: (x == Integer.MAX_VALUE) -> returns 1.0
 *    - Branch 3: (0 <= x < Integer.MAX_VALUE) -> calculates Gamma.regularizedGammaQ((double) x + 1, mean, epsilon, maxIterations)
 *
 * 4. normalApproximateProbability(int x):
 *    - Computes half-corrected normal cumulative probability: normal.cumulativeProbability(x + 0.5).
 *
 * 5. sample():
 *    - Small mean simulation branch and large mean rejection branch via randomData.nextPoisson(mean).
 *    - Bounded by Integer.MAX_VALUE.
 *
 * 6. Domain Bounds:
 *    - getDomainLowerBound(p) -> 0
 *    - getDomainUpperBound(p) -> Integer.MAX_VALUE
 *
 * 7. Defect Math-61 Specific:
 *    - Method: testMean()
 *    - Ground Truth Defect: Constructor with non-positive mean (-1) must throw NotStrictlyPositiveException.
 *      Buggy version threw MathRuntimeException$4, escaping the catch block.
 */
public class PoissonDistributionImplGeminiTest {

    private static final double EPSILON = 1e-12;

    /* =========================================================================
     * Partition A: Core Functional Logic & State Transitions
     * ========================================================================= */

    @Test(timeout = 4000)
    public void testConstructorsAndGetMean() {
        PoissonDistributionImpl dist1 = new PoissonDistributionImpl(5.0);
        assertEquals(5.0, dist1.getMean(), EPSILON);

        PoissonDistributionImpl dist2 = new PoissonDistributionImpl(7.5, 1e-8);
        assertEquals(7.5, dist2.getMean(), EPSILON);

        PoissonDistributionImpl dist3 = new PoissonDistributionImpl(10.0, 500);
        assertEquals(10.0, dist3.getMean(), EPSILON);

        PoissonDistributionImpl dist4 = new PoissonDistributionImpl(12.5, 1e-9, 2000);
        assertEquals(12.5, dist4.getMean(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testProbabilityStandardValues() {
        double mean = 4.0;
        PoissonDistributionImpl dist = new PoissonDistributionImpl(mean);

        // P(X = 0) = e^(-4)
        double p0Expected = FastMath.exp(-mean);
        assertEquals(p0Expected, dist.probability(0), EPSILON);

        // P(X = 1) = 4 * e^(-4)
        double p1Expected = 4.0 * FastMath.exp(-mean);
        assertEquals(p1Expected, dist.probability(1), 1e-10);

        // P(X = 2) = (4^2 / 2!) * e^(-4) = 8 * e^(-4)
        double p2Expected = 8.0 * FastMath.exp(-mean);
        assertEquals(p2Expected, dist.probability(2), 1e-10);

        // P(X = 4) = (4^4 / 24) * e^(-4) = (32 / 3) * e^(-4)
        double p4Expected = (32.0 / 3.0) * FastMath.exp(-mean);
        assertEquals(p4Expected, dist.probability(4), 1e-10);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityStandardValues() throws MathException {
        double mean = 3.0;
        PoissonDistributionImpl dist = new PoissonDistributionImpl(mean);

        // F(0) = P(X = 0) = e^(-3)
        assertEquals(FastMath.exp(-mean), dist.cumulativeProbability(0), EPSILON);

        // F(1) = P(X = 0) + P(X = 1) = e^(-3) + 3 * e^(-3) = 4 * e^(-3)
        double f1Expected = 4.0 * FastMath.exp(-mean);
        assertEquals(f1Expected, dist.cumulativeProbability(1), 1e-10);

        // F(2) = F(1) + (9/2) * e^(-3) = 8.5 * e^(-3)
        double f2Expected = 8.5 * FastMath.exp(-mean);
        assertEquals(f2Expected, dist.cumulativeProbability(2), 1e-10);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityInterval() throws MathException {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(2.5);
        double p1 = dist.cumulativeProbability(1);
        double p3 = dist.cumulativeProbability(3);
        double interval = dist.cumulativeProbability(1, 3);
        assertEquals(p3 - p1, interval, 1e-10);
    }

    @Test(timeout = 4000)
    public void testNormalApproximateProbability() throws MathException {
        // For large mean, normal approximation should be very close to cumulative probability
        double mean = 100.0;
        PoissonDistributionImpl dist = new PoissonDistributionImpl(mean);

        double approxP = dist.normalApproximateProbability(100);
        double exactP = dist.cumulativeProbability(100);

        // Normal approximation evaluated at x + 0.5: N(100, 10).cdf(100.5)
        assertTrue(approxP > 0.0 && approxP < 1.0);
        assertEquals(exactP, approxP, 0.02);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbability() throws MathException {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(5.0);

        assertEquals(0, dist.inverseCumulativeProbability(0.0));
        assertEquals(Integer.MAX_VALUE, dist.inverseCumulativeProbability(1.0));

        int x = dist.inverseCumulativeProbability(0.5);
        assertTrue(x >= 0 && x < 20);
        assertTrue(dist.cumulativeProbability(x) >= 0.5);
        if (x > 0) {
            assertTrue(dist.cumulativeProbability(x - 1) < 0.5);
        }
    }

    @Test(timeout = 4000)
    public void testDomainBounds() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(4.0);
        assertEquals(0, dist.getDomainLowerBound(0.0));
        assertEquals(0, dist.getDomainLowerBound(0.5));
        assertEquals(0, dist.getDomainLowerBound(1.0));

        assertEquals(Integer.MAX_VALUE, dist.getDomainUpperBound(0.0));
        assertEquals(Integer.MAX_VALUE, dist.getDomainUpperBound(0.5));
        assertEquals(Integer.MAX_VALUE, dist.getDomainUpperBound(1.0));
    }

    @Test(timeout = 4000)
    public void testSample() throws MathException {
        // Small mean sampling (Uniform simulation process)
        PoissonDistributionImpl distSmall = new PoissonDistributionImpl(2.0);
        for (int i = 0; i < 20; i++) {
            int s = distSmall.sample();
            assertTrue("Sample should be non-negative", s >= 0);
        }

        // Large mean sampling (Devroye rejection algorithm)
        PoissonDistributionImpl distLarge = new PoissonDistributionImpl(50.0);
        for (int i = 0; i < 20; i++) {
            int s = distLarge.sample();
            assertTrue("Sample should be non-negative", s >= 0);
        }
    }

    /* =========================================================================
     * Partition B: Boundary Value Analysis (BVA) & Extremes
     * ========================================================================= */

    @Test(timeout = 4000)
    public void testProbabilityNegativeXReturnsZero() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(3.0);
        assertEquals(0.0, dist.probability(-1), 0.0);
        assertEquals(0.0, dist.probability(-100), 0.0);
        assertEquals(0.0, dist.probability(Integer.MIN_VALUE), 0.0);
    }

    @Test(timeout = 4000)
    public void testProbabilityMaxIntegerReturnsZero() {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(3.0);
        assertEquals(0.0, dist.probability(Integer.MAX_VALUE), 0.0);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityNegativeXReturnsZero() throws MathException {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(3.0);
        assertEquals(0.0, dist.cumulativeProbability(-1), 0.0);
        assertEquals(0.0, dist.cumulativeProbability(-50), 0.0);
        assertEquals(0.0, dist.cumulativeProbability(Integer.MIN_VALUE), 0.0);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityMaxIntegerReturnsOne() throws MathException {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(3.0);
        assertEquals(1.0, dist.cumulativeProbability(Integer.MAX_VALUE), 0.0);
    }

    @Test(timeout = 4000)
    public void testVerySmallAndLargeMeanValues() throws MathException {
        // Extremely small mean
        PoissonDistributionImpl distSmall = new PoissonDistributionImpl(1e-10);
        assertEquals(1e-10, distSmall.getMean(), EPSILON);
        assertEquals(1.0, distSmall.probability(0), 1e-9);
        assertEquals(1.0, distSmall.cumulativeProbability(0), 1e-9);

        // Moderately large mean
        PoissonDistributionImpl distLarge = new PoissonDistributionImpl(1000.0);
        assertEquals(1000.0, distLarge.getMean(), EPSILON);
        assertTrue(distLarge.probability(1000) > 0.0);
    }

    /* =========================================================================
     * Partition C: Defect-Targeted Branch Zone (Defects4J Math-61 Ground Truth)
     * ========================================================================= */

    /**
     * Defects4J Math-61 ground-truth defect:
     * When mean is negative (-1), PoissonDistributionImpl must throw NotStrictlyPositiveException.
     * In the defective implementation, MathRuntimeException.createIllegalArgumentException was used,
     * which threw MathRuntimeException$4 instead of NotStrictlyPositiveException.
     */
    @Test(timeout = 4000)
    public void testMean() {
        try {
            new PoissonDistributionImpl(-1);
            fail("negative mean. NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException ex) {
            // Expected correct behavior in fixed version
            assertEquals(-1.0, ex.getArgument().doubleValue(), EPSILON);
        }
    }

    @Test(timeout = 4000)
    public void testMeanZeroTargetingDefect() {
        try {
            new PoissonDistributionImpl(0.0);
            fail("zero mean. NotStrictlyPositiveException expected");
        } catch (NotStrictlyPositiveException ex) {
            // Expected correct behavior in fixed version
            assertEquals(0.0, ex.getArgument().doubleValue(), EPSILON);
        }
    }

    /* =========================================================================
     * Partition D: Exception & Defensive Guard Paths
     * ========================================================================= */

    @Test(timeout = 4000)
    public void testConstructorsNegativeMeanThrowIllegalArgumentException() {
        try {
            new PoissonDistributionImpl(-5.0);
            fail("Expected IllegalArgumentException for mean = -5.0");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().length() > 0);
        }

        try {
            new PoissonDistributionImpl(-2.0, 1e-6);
            fail("Expected IllegalArgumentException for mean = -2.0");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().length() > 0);
        }

        try {
            new PoissonDistributionImpl(-3.0, 100);
            fail("Expected IllegalArgumentException for mean = -3.0");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().length() > 0);
        }

        try {
            new PoissonDistributionImpl(-4.0, 1e-6, 100);
            fail("Expected IllegalArgumentException for mean = -4.0");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().length() > 0);
        }
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityInvalidArgs() throws MathException {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(2.0);

        try {
            dist.inverseCumulativeProbability(-0.01);
            fail("Expected IllegalArgumentException for p < 0");
        } catch (IllegalArgumentException expected) {
            // Expected
        }

        try {
            dist.inverseCumulativeProbability(1.01);
            fail("Expected IllegalArgumentException for p > 1");
        } catch (IllegalArgumentException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityInvalidIntervalThrows() throws MathException {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(2.0);
        try {
            dist.cumulativeProbability(5, 2);
            fail("Expected IllegalArgumentException when x0 > x1");
        } catch (IllegalArgumentException expected) {
            // Expected
        }
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityNumericalConvergenceFailure() {
        // Exceedingly small max iterations (1) with high precision requirement
        // Gamma evaluation will fail to converge within 1 iteration.
        PoissonDistributionImpl dist = new PoissonDistributionImpl(20.0, 1e-15, 1);
        try {
            dist.cumulativeProbability(10);
            fail("Expected MathException due to iteration limit");
        } catch (MathException expected) {
            // Expected convergence exception
            assertNotNull(expected.getMessage());
        }
    }

    /* =========================================================================
     * Partition E: Object Lifecycle & Contract Integrity
     * ========================================================================= */

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        PoissonDistributionImpl dist = new PoissonDistributionImpl(4.5, 1e-7, 5000);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(dist);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object deserialized = ois.readObject();
        ois.close();

        assertTrue(deserialized instanceof PoissonDistributionImpl);
        PoissonDistributionImpl restored = (PoissonDistributionImpl) deserialized;
        assertEquals(dist.getMean(), restored.getMean(), EPSILON);
        assertEquals(dist.probability(3), restored.probability(3), EPSILON);
        assertEquals(dist.cumulativeProbability(3), restored.cumulativeProbability(3), EPSILON);
    }

    @Test(timeout = 4000)
    public void testPublicConstants() {
        assertEquals(10000000, PoissonDistributionImpl.DEFAULT_MAX_ITERATIONS);
        assertEquals(1E-12, PoissonDistributionImpl.DEFAULT_EPSILON, EPSILON);
    }
}