package org.apache.commons.math3.stat.inference;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.TiesStrategy;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target class: MannWhitneyUTest
 * 
 * Branches covered:
 * - Constructor default vs. parameterized (NaNStrategy, TiesStrategy)
 * - ensureDataConformance: null x, null y, empty x, empty y
 * - concatenateSamples: correct array copy
 * - mannWhitneyU: sumRankX loop, U1 calculation, U2 calculation, FastMath.max
 * - mannWhitneyUTest: calls mannWhitneyU, computes Umin, calls calculateAsymptoticPValue
 * - calculateAsymptoticPValue: EU, VarU, z, NormalDistribution.cumulativeProbability
 * 
 * Boundary/Extreme conditions:
 * - Large sample sizes (n1, n2 > 46340) causing integer overflow in n1*n2, n1*(n1+1)/2
 * - Zero-length arrays (NoDataException)
 * - Null arrays (NullArgumentException)
 * - Single-element arrays
 * - Ties (identical values)
 * - NaN values (handled by NaturalRanking with NaNStrategy.FIXED)
 * 
 * Defect targeted (Defects4J testBigDataSet):
 * - Integer overflow in mannWhitneyU when computing U1 = sumRankX - (x.length * (x.length + 1)) / 2
 *   and U2 = x.length * y.length - U1. For large arrays (e.g., 100000 elements), the product
 *   exceeds Integer.MAX_VALUE, causing incorrect U statistic and p-value.
 * - Test: use two large arrays with distinct values to compute expected U = n1*n2 exactly.
 *   On buggy version, overflow yields wrong result; on fixed version, correct double value.
 */
public class MannWhitneyUTestDeepseekTest {

    // -----------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        // Just ensure no exception; internal NaturalRanking uses FIXED and AVERAGE
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 4.0};
        double u = test.mannWhitneyU(x, y);
        assertTrue("U should be positive", u > 0);
    }

    @Test(timeout = 4000)
    public void testParameterizedConstructor() {
        MannWhitneyUTest test = new MannWhitneyUTest(NaNStrategy.REMOVED, TiesStrategy.MAXIMUM);
        double[] x = {1.0, 2.0};
        double[] y = {3.0, 4.0};
        double u = test.mannWhitneyU(x, y);
        assertTrue("U should be positive", u > 0);
    }

    // -----------------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNullX() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        try {
            test.mannWhitneyU(null, new double[]{1.0});
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testNullY() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        try {
            test.mannWhitneyU(new double[]{1.0}, null);
            fail("Expected NullArgumentException");
        } catch (NullArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEmptyX() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        try {
            test.mannWhitneyU(new double[]{}, new double[]{1.0});
            fail("Expected NoDataException");
        } catch (NoDataException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testEmptyY() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        try {
            test.mannWhitneyU(new double[]{1.0}, new double[]{});
            fail("Expected NoDataException");
        } catch (NoDataException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSingleElementEach() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        double[] x = {1.0};
        double[] y = {2.0};
        double u = test.mannWhitneyU(x, y);
        assertEquals("U should be 1", 1.0, u, 0.0);
        double p = test.mannWhitneyUTest(x, y);
        // p-value should be > 0 (asymptotic approximation)
        assertTrue("p-value should be positive", p > 0);
    }

    @Test(timeout = 4000)
    public void testIdenticalSamples() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        double[] x = {1.0, 2.0, 3.0};
        double[] y = {1.0, 2.0, 3.0};
        double u = test.mannWhitneyU(x, y);
        // For identical samples, U should be n1*n2/2 = 4.5
        assertEquals("U should be 4.5", 4.5, u, 1e-12);
        double p = test.mannWhitneyUTest(x, y);
        // p-value should be close to 1 (since samples are identical)
        assertTrue("p-value should be near 1", p > 0.9);
    }

    @Test(timeout = 4000)
    public void testAllTies() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        double[] x = {5.0, 5.0, 5.0};
        double[] y = {5.0, 5.0};
        double u = test.mannWhitneyU(x, y);
        // All values equal: ranks are all average = (1+2+3+4+5)/5 = 3.0
        // sumRankX = 3*3 = 9, U1 = 9 - (3*4)/2 = 9 - 6 = 3, U2 = 3*2 - 3 = 3, Umax = 3
        assertEquals("U should be 3", 3.0, u, 1e-12);
    }

    // -----------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Big Data Overflow)
    // -----------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testLargeSampleOverflow() {
        // Use arrays of size 100000 to trigger integer overflow in n1*(n1+1)/2 and n1*n2
        int n = 100000;
        double[] x = new double[n];
        double[] y = new double[n];
        // Fill x with 1..n, y with n+1..2n so that ranks are deterministic
        for (int i = 0; i < n; i++) {
            x[i] = i + 1;
            y[i] = i + n + 1;
        }
        MannWhitneyUTest test = new MannWhitneyUTest();
        double u = test.mannWhitneyU(x, y);
        // Expected U = n * n = 1e10 (since all x ranks are 1..n, sumRankX = n*(n+1)/2,
        // U1 = sumRankX - n*(n+1)/2 = 0, U2 = n*n - 0 = n*n, Umax = n*n)
        double expectedU = (double) n * n; // 1e10 exactly representable
        assertEquals("U statistic should be n*n", expectedU, u, 0.0);
    }

    @Test(timeout = 4000)
    public void testLargeSamplePValue() {
        // Same large sample, p-value should be extremely small (samples are separated)
        int n = 100000;
        double[] x = new double[n];
        double[] y = new double[n];
        for (int i = 0; i < n; i++) {
            x[i] = i + 1;
            y[i] = i + n + 1;
        }
        MannWhitneyUTest test = new MannWhitneyUTest();
        double p = test.mannWhitneyUTest(x, y);
        // p-value should be very close to 0 (asymptotic approximation)
        assertTrue("p-value should be very small", p < 1e-10);
    }

    // -----------------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // -----------------------------------------------------------------------

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testMannWhitneyUTestNullX() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyUTest(null, new double[]{1.0});
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testMannWhitneyUTestNullY() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyUTest(new double[]{1.0}, null);
    }

    @Test(timeout = 4000, expected = NoDataException.class)
    public void testMannWhitneyUTestEmptyX() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyUTest(new double[]{}, new double[]{1.0});
    }

    @Test(timeout = 4000, expected = NoDataException.class)
    public void testMannWhitneyUTestEmptyY() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyUTest(new double[]{1.0}, new double[]{});
    }

    // -----------------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity (not applicable)
    // -----------------------------------------------------------------------

    // No equals/hashCode/clone/serialization in this class.
}