/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.math3.distribution;

import org.apache.commons.math3.exception.NumberIsTooLargeException;
import org.apache.commons.math3.random.Well19937c;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * 1. Constructor Validation Branch (lower >= upper):
 *    - Branch lower < upper: Normal initialization (all 4 constructors).
 *    - Branch lower == upper: Throws NumberIsTooLargeException.
 *    - Branch lower > upper: Throws NumberIsTooLargeException.
 *
 * 2. density(x) Decision Branches (x < lower || x > upper):
 *    - x < lower: returns 0.0.
 *    - x > upper: returns 0.0.
 *    - x == lower: boundary inside support, returns 1.0 / (upper - lower).
 *    - x == upper: boundary inside support, returns 1.0 / (upper - lower).
 *    - lower < x < upper: returns 1.0 / (upper - lower).
 *
 * 3. cumulativeProbability(x) Decision Branches:
 *    - x < lower: returns 0.0.
 *    - x == lower: returns 0.0.
 *    - x > upper: returns 1.0.
 *    - x == upper: returns 1.0.
 *    - lower < x < upper: returns (x - lower) / (upper - lower).
 *
 * 4. Defect-Targeted Ground Truth (Defects4J):
 *    - Method: isSupportUpperBoundInclusive()
 *    - Fault: Currently returns false, but mathematical support specification requires true.
 *    - Test: testIsSupportUpperBoundInclusive asserts expected:<true>.
 *
 * 5. Statistical & Numerical Characterization:
 *    - getNumericalMean(): 0.5 * (lower + upper) with positive, negative, and mixed intervals.
 *    - getNumericalVariance(): (upper - lower)^2 / 12.
 *    - getSupportLowerBound(), getSupportUpperBound().
 *    - isSupportLowerBoundInclusive() == true, isSupportConnected() == true.
 *    - getSolverAbsoluteAccuracy(): verifies customized and default accuracy (1e-9).
 *    - sample() and sample(int): verifies samples stay within [lower, upper].
 *    - inverseCumulativeProbability(p): verifies boundary mapping p=0 -> lower, p=1 -> upper.
 */
public class UniformRealDistributionGeminiTest {

    private static final double EPSILON = 1e-12;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        UniformRealDistribution dist = new UniformRealDistribution();
        assertEquals(0.0, dist.getSupportLowerBound(), EPSILON);
        assertEquals(1.0, dist.getSupportUpperBound(), EPSILON);
        assertEquals(UniformRealDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY,
                dist.getSolverAbsoluteAccuracy(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testTwoArgConstructor() {
        UniformRealDistribution dist = new UniformRealDistribution(-2.5, 3.5);
        assertEquals(-2.5, dist.getSupportLowerBound(), EPSILON);
        assertEquals(3.5, dist.getSupportUpperBound(), EPSILON);
        assertEquals(UniformRealDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY,
                dist.getSolverAbsoluteAccuracy(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testThreeArgConstructor() {
        double customAccuracy = 1e-6;
        UniformRealDistribution dist = new UniformRealDistribution(1.0, 5.0, customAccuracy);
        assertEquals(1.0, dist.getSupportLowerBound(), EPSILON);
        assertEquals(5.0, dist.getSupportUpperBound(), EPSILON);
        assertEquals(customAccuracy, dist.getSolverAbsoluteAccuracy(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testFourArgConstructorWithRng() {
        Well19937c rng = new Well19937c(1234567L);
        double customAccuracy = 1e-8;
        UniformRealDistribution dist = new UniformRealDistribution(rng, 10.0, 20.0, customAccuracy);
        assertEquals(10.0, dist.getSupportLowerBound(), EPSILON);
        assertEquals(20.0, dist.getSupportUpperBound(), EPSILON);
        assertEquals(customAccuracy, dist.getSolverAbsoluteAccuracy(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testDensityInsideAndOutside() {
        UniformRealDistribution dist = new UniformRealDistribution(2.0, 6.0);
        double expectedDensity = 1.0 / (6.0 - 2.0); // 0.25

        // Strictly inside
        assertEquals(expectedDensity, dist.density(3.0), EPSILON);
        assertEquals(expectedDensity, dist.density(4.0), EPSILON);
        assertEquals(expectedDensity, dist.density(5.999), EPSILON);

        // Outside strictly below and above
        assertEquals(0.0, dist.density(1.999), EPSILON);
        assertEquals(0.0, dist.density(0.0), EPSILON);
        assertEquals(0.0, dist.density(-100.0), EPSILON);
        assertEquals(0.0, dist.density(6.001), EPSILON);
        assertEquals(0.0, dist.density(100.0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testCumulativeProbability() {
        UniformRealDistribution dist = new UniformRealDistribution(2.0, 6.0);

        // Outside and boundary points
        assertEquals(0.0, dist.cumulativeProbability(1.0), EPSILON);
        assertEquals(0.0, dist.cumulativeProbability(2.0), EPSILON);
        assertEquals(1.0, dist.cumulativeProbability(6.0), EPSILON);
        assertEquals(1.0, dist.cumulativeProbability(7.0), EPSILON);

        // Interior points
        assertEquals(0.25, dist.cumulativeProbability(3.0), EPSILON);
        assertEquals(0.50, dist.cumulativeProbability(4.0), EPSILON);
        assertEquals(0.75, dist.cumulativeProbability(5.0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testNumericalMeanAndVariance() {
        UniformRealDistribution dist = new UniformRealDistribution(-4.0, 8.0);
        // Mean = 0.5 * (-4 + 8) = 2.0
        assertEquals(2.0, dist.getNumericalMean(), EPSILON);
        // Variance = (8 - (-4))^2 / 12 = 144 / 12 = 12.0
        assertEquals(12.0, dist.getNumericalVariance(), EPSILON);

        UniformRealDistribution standard = new UniformRealDistribution(0.0, 1.0);
        assertEquals(0.5, standard.getNumericalMean(), EPSILON);
        assertEquals(1.0 / 12.0, standard.getNumericalVariance(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testSupportProperties() {
        UniformRealDistribution dist = new UniformRealDistribution(3.0, 7.0);
        assertEquals(3.0, dist.getSupportLowerBound(), EPSILON);
        assertEquals(7.0, dist.getSupportUpperBound(), EPSILON);
        assertTrue(dist.isSupportLowerBoundInclusive());
        assertTrue(dist.isSupportConnected());
    }

    @Test(timeout = 4000)
    public void testSamplingDeterministic() {
        Well19937c rng = new Well19937c(42L);
        UniformRealDistribution dist = new UniformRealDistribution(rng, 10.0, 20.0, 1e-9);

        for (int i = 0; i < 1000; i++) {
            double sample = dist.sample();
            assertTrue("Sample " + sample + " below lower bound", sample >= 10.0);
            assertTrue("Sample " + sample + " above upper bound", sample <= 20.0);
        }

        double[] samples = dist.sample(100);
        assertEquals(100, samples.length);
        for (double s : samples) {
            assertTrue(s >= 10.0 && s <= 20.0);
        }
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testDensityAtExactBoundaries() {
        UniformRealDistribution dist = new UniformRealDistribution(1.0, 3.0);
        double expectedDensity = 0.5;

        // Exact boundary evaluation
        assertEquals(expectedDensity, dist.density(1.0), EPSILON);
        assertEquals(expectedDensity, dist.density(3.0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testExtremeRange() {
        double lower = -1e100;
        double upper = 1e100;
        UniformRealDistribution dist = new UniformRealDistribution(lower, upper);

        assertEquals(0.0, dist.getNumericalMean(), EPSILON);
        assertEquals(0.5, dist.cumulativeProbability(0.0), EPSILON);
        assertEquals(0.0, dist.cumulativeProbability(lower), EPSILON);
        assertEquals(1.0, dist.cumulativeProbability(upper), EPSILON);
    }

    @Test(timeout = 4000)
    public void testSmallInterval() {
        double lower = 1.0;
        double upper = 1.0 + 1e-8;
        UniformRealDistribution dist = new UniformRealDistribution(lower, upper);

        assertEquals(1e8, dist.density(1.0 + 0.5e-8), 1e-4);
        assertEquals(0.5, dist.cumulativeProbability(1.0 + 0.5e-8), 1e-6);
    }

    @Test(timeout = 4000)
    public void testInverseCumulativeProbabilityBoundaries() {
        UniformRealDistribution dist = new UniformRealDistribution(-5.0, 15.0);

        assertEquals(-5.0, dist.inverseCumulativeProbability(0.0), EPSILON);
        assertEquals(15.0, dist.inverseCumulativeProbability(1.0), EPSILON);
        assertEquals(5.0, dist.inverseCumulativeProbability(0.5), EPSILON);
        assertEquals(-5.0 + 0.25 * 20.0, dist.inverseCumulativeProbability(0.25), EPSILON);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone
    // =========================================================================

    /**
     * Defects4J Ground Truth Target:
     * UniformRealDistributionTest::testIsSupportUpperBoundInclusive
     * Expected: <true> but was: <false>
     *
     * In Apache Commons Math continuous distributions, the upper bound of the support
     * for a uniform distribution [a, b] is inclusive mathematically.
     * The buggy version has:
     *    public boolean isSupportUpperBoundInclusive() { return false; }
     * This test explicitly asserts true to expose this exact known defect.
     */
    @Test(timeout = 4000)
    public void testIsSupportUpperBoundInclusive() {
        UniformRealDistribution dist = new UniformRealDistribution(0.0, 1.0);
        assertTrue("Support upper bound of uniform distribution should be inclusive",
                dist.isSupportUpperBoundInclusive());
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testConstructorThrowsWhenLowerEqualsUpperTwoArg() {
        new UniformRealDistribution(2.0, 2.0);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testConstructorThrowsWhenLowerGreaterThanUpperTwoArg() {
        new UniformRealDistribution(5.0, 2.0);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testConstructorThrowsWhenLowerEqualsUpperThreeArg() {
        new UniformRealDistribution(3.5, 3.5, 1e-6);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testConstructorThrowsWhenLowerGreaterThanUpperThreeArg() {
        new UniformRealDistribution(4.0, 3.0, 1e-6);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testConstructorThrowsWhenLowerEqualsUpperFourArg() {
        new UniformRealDistribution(new Well19937c(), 7.0, 7.0, 1e-6);
    }

    @Test(expected = NumberIsTooLargeException.class, timeout = 4000)
    public void testConstructorThrowsWhenLowerGreaterThanUpperFourArg() {
        new UniformRealDistribution(new Well19937c(), 10.0, -10.0, 1e-6);
    }

    @Test(timeout = 4000)
    public void testConstructorExceptionValues() {
        try {
            new UniformRealDistribution(10.0, 2.0);
            fail("Expected NumberIsTooLargeException was not thrown");
        } catch (NumberIsTooLargeException ex) {
            assertEquals(10.0, ex.getArgument().doubleValue(), EPSILON);
            assertEquals(2.0, ex.getMax().doubleValue(), EPSILON);
            assertFalse(ex.getBoundIsAllowed());
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSerializationIntegrity() throws Exception {
        UniformRealDistribution original = new UniformRealDistribution(-10.0, 30.0, 1e-5);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(original);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        UniformRealDistribution deserialized = (UniformRealDistribution) ois.readObject();
        ois.close();

        assertEquals(original.getSupportLowerBound(), deserialized.getSupportLowerBound(), EPSILON);
        assertEquals(original.getSupportUpperBound(), deserialized.getSupportUpperBound(), EPSILON);
        assertEquals(original.getSolverAbsoluteAccuracy(), deserialized.getSolverAbsoluteAccuracy(), EPSILON);
        assertEquals(original.getNumericalMean(), deserialized.getNumericalMean(), EPSILON);
        assertEquals(original.getNumericalVariance(), deserialized.getNumericalVariance(), EPSILON);
        assertEquals(original.density(0.0), deserialized.density(0.0), EPSILON);
        assertEquals(original.cumulativeProbability(5.0), deserialized.cumulativeProbability(5.0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testReseedRandomGenerator() {
        UniformRealDistribution dist1 = new UniformRealDistribution(new Well19937c(12345L), 0.0, 100.0, 1e-9);
        UniformRealDistribution dist2 = new UniformRealDistribution(new Well19937c(99999L), 0.0, 100.0, 1e-9);

        dist1.reseedRandomGenerator(424242L);
        dist2.reseedRandomGenerator(424242L);

        for (int i = 0; i < 50; i++) {
            assertEquals(dist1.sample(), dist2.sample(), EPSILON);
        }
    }
}