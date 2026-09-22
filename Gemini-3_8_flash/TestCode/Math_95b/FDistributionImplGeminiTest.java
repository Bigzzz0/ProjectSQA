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
 * Target Class: org.apache.commons.math.distribution.FDistributionImpl
 * ---------------------------------------------------------------------------------------------------------
 * Method                               | Branch / Condition                          | Test Target
 * ---------------------------------------------------------------------------------------------------------
 * Constructor                          | numDF <= 0, denDF <= 0                      | testConstructorInvalidParameters
 *                                      | numDF > 0, denDF > 0                        | testConstructorValidParameters
 * ---------------------------------------------------------------------------------------------------------
 * cumulativeProbability(double)        | x <= 0.0 (x < 0, x == 0)                    | testCumulativeProbabilityNonPositive
 *                                      | x > 0.0                                     | testCumulativeProbabilityPositive
 * ---------------------------------------------------------------------------------------------------------
 * inverseCumulativeProbability(double) | p == 0.0                                    | testInverseCumulativeProbabilityZero
 *                                      | p == 1.0                                    | testInverseCumulativeProbabilityOne
 *                                      | p < 0.0 || p > 1.0                          | testInverseCumulativeProbabilityInvalid
 *                                      | 0.0 < p < 1.0 (denDF > 2.0)                 | testInverseCumulativeProbabilityNormal
 *                                      | 0.0 < p < 1.0 (denDF <= 2.0, e.g., 1.0/2.0)| testSmallDegreesOfFreedomInverseCdfDefect
 *                                      |                                             | [Defects4J Target: Math-227]
 * ---------------------------------------------------------------------------------------------------------
 * getDomainLowerBound(double)          | Any p                                       | testGetDomainLowerBound
 * ---------------------------------------------------------------------------------------------------------
 * getDomainUpperBound(double)          | Any p                                       | testGetDomainUpperBound
 * ---------------------------------------------------------------------------------------------------------
 * getInitialDomain(double)             | d = denDF > 2.0                             | testGetInitialDomainStandard
 *                                      | d <= 2.0 (Defect: leads to negative/NaN/Inf)| testGetInitialDomainSmallDf
 * ---------------------------------------------------------------------------------------------------------
 * setNumeratorDegreesOfFreedom(double) | df <= 0.0                                   | testSetNumeratorDegreesOfFreedomInvalid
 *                                      | df > 0.0                                    | testSetNumeratorDegreesOfFreedomValid
 * ---------------------------------------------------------------------------------------------------------
 * setDenominatorDegreesOfFreedom(double)| df <= 0.0                                  | testSetDenominatorDegreesOfFreedomInvalid
 *                                      | df > 0.0                                    | testSetDenominatorDegreesOfFreedomValid
 * ---------------------------------------------------------------------------------------------------------
 * Serialization                        | Serializable implementation check           | testSerializationIntegrity
 * ---------------------------------------------------------------------------------------------------------
 */
public class FDistributionImplGeminiTest {

    private static final double TOLERANCE = 1e-6;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorValidParameters() {
        FDistributionImpl dist = new FDistributionImpl(5.0, 6.0);
        assertEquals(5.0, dist.getNumeratorDegreesOfFreedom(), 0.0);
        assertEquals(6.0, dist.getDenominatorDegreesOfFreedom(), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetAndSetDegreesOfFreedom() {
        FDistributionImpl dist = new FDistributionImpl(10.0, 20.0);
        
        dist.setNumeratorDegreesOfFreedom(2.5);
        assertEquals(2.5, dist.getNumeratorDegreesOfFreedom(), 0.0);

        dist.setDenominatorDegreesOfFreedom(8.5);
        assertEquals(8.5, dist.getDenominatorDegreesOfFreedom(), 0.0);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbabilityPositive() throws MathException {
        // F-distribution with numDF = 5, denDF = 10
        FDistributionImpl dist = new FDistributionImpl(5.0, 10.0);
        
        // At x = 1.0, theoretical CDF
        double prob = dist.cumulativeProbability(1.0);
        assertTrue("Cumulative probability must be strictly between 0 and 1", prob > 0.0 && prob < 1.0);
        assertEquals(0.53483, prob, 1e-4);

        // Monotonicity check
        double probHigher = dist.cumulativeProbability(2.0);
        assertTrue("Cumulative probability must strictly increase with x", probHigher > prob);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityNormal() throws MathException {
        FDistributionImpl dist = new FDistributionImpl(5.0, 10.0);
        
        double originalX = 1.5;
        double p = dist.cumulativeProbability(originalX);
        double recoveredX = dist.inverseCumulativeProbability(p);

        assertEquals(originalX, recoveredX, TOLERANCE);
    }

    @Test(timeout = 4000)
    public void testGetDomainLowerBound() {
        FDistributionImpl dist = new FDistributionImpl(4.0, 4.0);
        assertEquals(0.0, dist.getDomainLowerBound(0.25), 0.0);
        assertEquals(0.0, dist.getDomainLowerBound(0.75), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetDomainUpperBound() {
        FDistributionImpl dist = new FDistributionImpl(4.0, 4.0);
        assertEquals(Double.MAX_VALUE, dist.getDomainUpperBound(0.25), 0.0);
        assertEquals(Double.MAX_VALUE, dist.getDomainUpperBound(0.75), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetInitialDomainStandard() {
        FDistributionImpl dist = new FDistributionImpl(4.0, 4.0);
        // initial domain formula: d / (d - 2.0) = 4 / (4 - 2) = 2.0
        assertEquals(2.0, dist.getInitialDomain(0.5), TOLERANCE);

        dist.setDenominatorDegreesOfFreedom(6.0);
        // 6 / (6 - 2) = 1.5
        assertEquals(1.5, dist.getInitialDomain(0.5), TOLERANCE);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testCumulativeProbabilityNonPositive() throws MathException {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        
        assertEquals(0.0, dist.cumulativeProbability(0.0), 0.0);
        assertEquals(0.0, dist.cumulativeProbability(-0.0001), 0.0);
        assertEquals(0.0, dist.cumulativeProbability(-100.0), 0.0);
        assertEquals(0.0, dist.cumulativeProbability(Double.NEGATIVE_INFINITY), 0.0);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityZero() throws MathException {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        assertEquals(0.0, dist.inverseCumulativeProbability(0.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityOne() throws MathException {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        assertEquals(Double.POSITIVE_INFINITY, dist.inverseCumulativeProbability(1.0), 0.0);
    }

    @Test(timeout = 4000)
    public void testLargeDegreesOfFreedom() throws MathException {
        FDistributionImpl dist = new FDistributionImpl(1000.0, 1000.0);
        double p = dist.cumulativeProbability(1.0);
        assertEquals(0.5, p, 0.02);
        double x = dist.inverseCumulativeProbability(0.5);
        assertEquals(1.0, x, 0.02);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Fault)
    // =========================================================================

    /**
     * TARGET DEFECT: When denominator degrees of freedom <= 2.0,
     * getInitialDomain(p) calculates d / (d - 2.0).
     * For d = 1.0: 1.0 / (1.0 - 2.0) = -1.0.
     * When inverseCumulativeProbability root-finder initializes, lowerBound = 0.0,
     * but initial = -1.0, which violates lowerBound <= initial <= upperBound,
     * throwing IllegalArgumentException: Invalid endpoint parameters: lowerBound=0.0 initial=-1.0.
     *
     * This test directly triggers the known bug on defective versions.
     */
    @Test(timeout = 4000)
    public void testSmallDegreesOfFreedomDenominatorOne() throws MathException {
        FDistributionImpl dist = new FDistributionImpl(1.0, 1.0);
        double targetX = 0.975;
        double p = dist.cumulativeProbability(targetX);
        
        // This invocation MUST successfully compute the root without throwing
        // IllegalArgumentException: Invalid endpoint parameters
        double recoveredX = dist.inverseCumulativeProbability(p);
        assertEquals(targetX, recoveredX, 1e-4);
    }

    /**
     * TARGET DEFECT: When denominator degrees of freedom is exactly 2.0,
     * getInitialDomain(p) calculates 2.0 / (2.0 - 2.0) = POSITIVE_INFINITY.
     * Inverse CDF solver endpoint verification must not fail with invalid parameters.
     */
    @Test(timeout = 4000)
    public void testSmallDegreesOfFreedomDenominatorTwo() throws MathException {
        FDistributionImpl dist = new FDistributionImpl(2.0, 2.0);
        double p = 0.5;
        double x = dist.inverseCumulativeProbability(p);
        
        assertTrue("Calculated critical point x must be positive", x > 0.0);
        assertEquals(p, dist.cumulativeProbability(x), 1e-4);
    }

    @Test(timeout = 4000)
    public void testSmallDegreesOfFreedomAsymmetric() throws MathException {
        FDistributionImpl dist = new FDistributionImpl(5.0, 1.0);
        double p = 0.8;
        double x = dist.inverseCumulativeProbability(p);
        assertTrue("Calculated critical point x must be positive", x > 0.0);
        assertEquals(p, dist.cumulativeProbability(x), 1e-4);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorZeroNumeratorDegreesOfFreedom() {
        new FDistributionImpl(0.0, 5.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNegativeNumeratorDegreesOfFreedom() {
        new FDistributionImpl(-1.0, 5.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorZeroDenominatorDegreesOfFreedom() {
        new FDistributionImpl(5.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorNegativeDenominatorDegreesOfFreedom() {
        new FDistributionImpl(5.0, -2.5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetNumeratorDegreesOfFreedomZero() {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        dist.setNumeratorDegreesOfFreedom(0.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetNumeratorDegreesOfFreedomNegative() {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        dist.setNumeratorDegreesOfFreedom(-0.5);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDenominatorDegreesOfFreedomZero() {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        dist.setDenominatorDegreesOfFreedom(0.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetDenominatorDegreesOfFreedomNegative() {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        dist.setDenominatorDegreesOfFreedom(-10.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInverseCumulativeProbabilityNegativeP() throws MathException {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        dist.inverseCumulativeProbability(-0.01);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInverseCumulativeProbabilityGreaterThanOne() throws MathException {
        FDistributionImpl dist = new FDistributionImpl(5.0, 5.0);
        dist.inverseCumulativeProbability(1.01);
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        FDistributionImpl original = new FDistributionImpl(4.5, 7.5);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        FDistributionImpl deserialized = (FDistributionImpl) ois.readObject();
        ois.close();

        assertNotNull(deserialized);
        assertEquals(original.getNumeratorDegreesOfFreedom(), deserialized.getNumeratorDegreesOfFreedom(), 0.0);
        assertEquals(original.getDenominatorDegreesOfFreedom(), deserialized.getDenominatorDegreesOfFreedom(), 0.0);
        assertEquals(original.cumulativeProbability(1.2), deserialized.cumulativeProbability(1.2), TOLERANCE);
    }
}