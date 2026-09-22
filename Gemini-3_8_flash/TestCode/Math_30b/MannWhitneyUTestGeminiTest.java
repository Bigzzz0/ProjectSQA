package org.apache.commons.math3.stat.inference;

import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.TiesStrategy;
import org.junit.Test;

import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * ---------------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.math3.stat.inference.MannWhitneyUTest
 *
 * 1. Branch / Condition Coverage:
 *    - ensureDataConformance:
 *        * x == null (T/F), y == null (T/F) -> NullArgumentException
 *        * x.length == 0 (T/F), y.length == 0 (T/F) -> NoDataException
 *    - mannWhitneyU:
 *        * Concatenation and ranking correctly preserving sample ordering
 *        * FastMath.max(U1, U2) -> Branch where U1 > U2, branch where U2 > U1, branch where U1 == U2
 *    - calculateAsymptoticPValue:
 *        * Standard asymptotic normal approximation: z-score calculation and 2-sided tail probability
 *    - Constructors:
 *        * Default constructor (NaNStrategy.FIXED, TiesStrategy.AVERAGE)
 *        * Custom constructor (explicit NaNStrategy and TiesStrategy)
 *
 * 2. Defect Analysis (Defects4J - MannWhitneyUTest integer overflow):
 *    - Failure: testBigDataSet -> AssertionFailedError
 *    - Root Cause: In calculateAsymptoticPValue(), `n1n2prod * (n1 + n2 + 1)` executes with 32-bit integer
 *      multiplication. When samples are large (e.g., n1 = 1500, n2 = 1500), 2250000 * 3001 = 6,752,250,000,
 *      which exceeds Integer.MAX_VALUE (2,147,483,647), overflowing to a negative integer (-1,837,684,592).
 *      VarU becomes negative, FastMath.sqrt(VarU) returns NaN, and calculateAsymptoticPValue returns NaN.
 *    - Fix Verification: Assert that p-value for large samples is finite, strictly positive, and not NaN.
 * ---------------------------------------------------------------------------------------------------------
 */
public class MannWhitneyUTestGeminiTest {

    private static final double EPSILON = 1e-6;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorAndStandardCalculation() {
        MannWhitneyUTest test = new MannWhitneyUTest();

        final double[] x = {19.0, 22.0, 16.0, 29.0, 24.0};
        final double[] y = {20.0, 11.0, 17.0, 12.0};

        // For x and y, U1 and U2 can be computed deterministically
        double u = test.mannWhitneyU(x, y);
        assertTrue("U must be >= 0", u >= 0.0);
        // U must be max(U1, U2). For n1=5, n2=4, n1*n2 = 20, max >= 10
        assertTrue("U statistic must be at least n1*n2/2", u >= 10.0);

        double p = test.mannWhitneyUTest(x, y);
        assertTrue("p-value must be between 0 and 1", p >= 0.0 && p <= 1.0);
    }

    @Test(timeout = 4000)
    public void testCustomConstructorWithDifferentStrategies() {
        MannWhitneyUTest test = new MannWhitneyUTest(NaNStrategy.MINIMAL, TiesStrategy.MAXIMUM);

        final double[] x = {Double.NaN, 2.0, 3.0};
        final double[] y = {1.0, 5.0, 6.0};

        double u = test.mannWhitneyU(x, y);
        assertTrue(u >= 0.0);

        double p = test.mannWhitneyUTest(x, y);
        assertTrue(p >= 0.0 && p <= 1.0);
    }

    @Test(timeout = 4000)
    public void testUStatisticSymmetryAndInvariance() {
        MannWhitneyUTest test = new MannWhitneyUTest();

        final double[] x = {1.0, 3.0, 5.0, 7.0};
        final double[] y = {2.0, 4.0, 6.0, 8.0};

        // mannWhitneyU returns max(U1, U2), which is symmetric with respect to argument order
        double uXY = test.mannWhitneyU(x, y);
        double uYX = test.mannWhitneyU(y, x);
        assertEquals("U(x, y) must equal U(y, x)", uXY, uYX, EPSILON);

        double pXY = test.mannWhitneyUTest(x, y);
        double pYX = test.mannWhitneyUTest(y, x);
        assertEquals("p-value must be symmetric", pXY, pYX, EPSILON);
    }

    @Test(timeout = 4000)
    public void testCompleteSeparationSamples() {
        MannWhitneyUTest test = new MannWhitneyUTest();

        // Sample X is strictly smaller than sample Y
        final double[] x = {1.0, 2.0, 3.0, 4.0};
        final double[] y = {10.0, 20.0, 30.0, 40.0};

        // n1 = 4, n2 = 4 -> n1 * n2 = 16.
        // sumRankX = 1 + 2 + 3 + 4 = 10
        // U1 = 10 - 4*5/2 = 0
        // U2 = 16 - 0 = 16 -> max(U1, U2) = 16
        double u = test.mannWhitneyU(x, y);
        assertEquals(16.0, u, EPSILON);

        double p = test.mannWhitneyUTest(x, y);
        assertTrue("p-value should indicate significant difference", p < 0.05);
    }

    @Test(timeout = 4000)
    public void testIdenticalDistributionsUStatistic() {
        MannWhitneyUTest test = new MannWhitneyUTest();

        final double[] x = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};
        final double[] y = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0};

        // Complete ties
        double u = test.mannWhitneyU(x, y);
        // n1=6, n2=6 -> n1*n2 = 36. Symmetric ranks yield U1 = U2 = 18.0
        assertEquals(18.0, u, EPSILON);

        double p = test.mannWhitneyUTest(x, y);
        // P-value for identical sets should be very close to 1.0
        assertEquals(1.0, p, 0.05);
    }

    @Test(timeout = 4000)
    public void testUnequalSampleSizes() {
        MannWhitneyUTest test = new MannWhitneyUTest();

        final double[] x = {1.0, 2.0};
        final double[] y = {3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0};

        double u = test.mannWhitneyU(x, y);
        // n1=2, n2=7, n1*n2 = 14
        // x ranks: 1, 2. sumRankX = 3. U1 = 3 - (2*3)/2 = 0. U2 = 14. max = 14
        assertEquals(14.0, u, EPSILON);

        double p = test.mannWhitneyUTest(x, y);
        assertTrue("p-value must be between 0 and 1", p >= 0.0 && p <= 1.0);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinimalSampleSizes() {
        MannWhitneyUTest test = new MannWhitneyUTest();

        final double[] x = {42.0};
        final double[] y = {100.0};

        // n1 = 1, n2 = 1, n1 * n2 = 1. U1 = 0, U2 = 1. max = 1.0
        double u = test.mannWhitneyU(x, y);
        assertEquals(1.0, u, EPSILON);

        double p = test.mannWhitneyUTest(x, y);
        assertTrue("p-value should be within [0, 1]", p >= 0.0 && p <= 1.0);
    }

    @Test(timeout = 4000)
    public void testExtremeValuesInfinity() {
        MannWhitneyUTest test = new MannWhitneyUTest();

        final double[] x = {Double.NEGATIVE_INFINITY, 0.0};
        final double[] y = {Double.POSITIVE_INFINITY, 100.0};

        double u = test.mannWhitneyU(x, y);
        // x ranks: 1, 2. y ranks: 4, 3.
        // sumRankX = 3. U1 = 3 - 3 = 0. U2 = 4. max = 4.0
        assertEquals(4.0, u, EPSILON);

        double p = test.mannWhitneyUTest(x, y);
        assertTrue("p-value must be valid", p >= 0.0 && p <= 1.0);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Integer Overflow on Big Data)
    // =========================================================================

    /**
     * Targets the defect where `n1n2prod * (n1 + n2 + 1)` overflows 32-bit signed integer.
     * When n1 = 1500 and n2 = 1500, n1n2prod = 2,250,000.
     * 2,250,000 * (1500 + 1500 + 1) = 6,752,250,000 > Integer.MAX_VALUE.
     * In defective code, this overflows to a negative integer, causing VarU < 0,
     * FastMath.sqrt(VarU) -> NaN, and asymptotic p-value -> NaN.
     */
    @Test(timeout = 4000)
    public void testBigDataSetIntegerOverflowDefect() {
        final MannWhitneyUTest test = new MannWhitneyUTest();

        final int size = 1500;
        final double[] d1 = new double[size];
        final double[] d2 = new double[size];

        for (int i = 0; i < size; ++i) {
            d1[i] = 2.0 * i;
            d2[i] = 2.0 * i + 1.0;
        }

        double u = test.mannWhitneyU(d1, d2);
        assertFalse("U statistic must not be NaN", Double.isNaN(u));
        assertTrue("U statistic must be positive", u > 0.0);

        double pValue = test.mannWhitneyUTest(d1, d2);
        assertFalse("P-value must not be NaN (reveals 32-bit int overflow in variance calculation)",
                Double.isNaN(pValue));
        assertTrue("Interleaved samples of size 1500 should yield high p-value", pValue > 0.1);
        assertTrue("P-value must be <= 1.0", pValue <= 1.0);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testEnsureDataConformanceNullFirstSampleU() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyU(null, new double[]{1.0, 2.0});
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testEnsureDataConformanceNullSecondSampleU() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyU(new double[]{1.0, 2.0}, null);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testEnsureDataConformanceBothNullU() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyU(null, null);
    }

    @Test(expected = NoDataException.class, timeout = 4000)
    public void testEnsureDataConformanceEmptyFirstSampleU() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyU(new double[]{}, new double[]{1.0, 2.0});
    }

    @Test(expected = NoDataException.class, timeout = 4000)
    public void testEnsureDataConformanceEmptySecondSampleU() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyU(new double[]{1.0, 2.0}, new double[]{});
    }

    @Test(expected = NoDataException.class, timeout = 4000)
    public void testEnsureDataConformanceBothEmptyU() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyU(new double[]{}, new double[]{});
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testEnsureDataConformanceNullFirstSamplePValue() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyUTest(null, new double[]{1.0, 2.0});
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testEnsureDataConformanceNullSecondSamplePValue() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyUTest(new double[]{1.0, 2.0}, null);
    }

    @Test(expected = NoDataException.class, timeout = 4000)
    public void testEnsureDataConformanceEmptyFirstSamplePValue() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyUTest(new double[]{}, new double[]{1.0});
    }

    @Test(expected = NoDataException.class, timeout = 4000)
    public void testEnsureDataConformanceEmptySecondSamplePValue() {
        MannWhitneyUTest test = new MannWhitneyUTest();
        test.mannWhitneyUTest(new double[]{1.0}, new double[]{});
    }
}