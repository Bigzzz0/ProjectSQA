package org.apache.commons.math.stat.inference;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.ChiSquaredDistribution;
import org.apache.commons.math.distribution.ChiSquaredDistributionImpl;
import org.apache.commons.math.distribution.DistributionFactory;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Target Class: org.apache.commons.math.stat.inference.ChiSquareTestImpl
 *
 * 1. Targeted Defects (Defects4J Math-102):
 *    - Specification explicitly mandates: "rescales the expected array if necessary to ensure that the
 *      sum of the expected and observed counts are equal."
 *    - Defective implementation directly computes chi-square without rescaling expected counts when
 *      sum(expected) != sum(observed), yielding massive statistical distortions (e.g. 16.413 vs 9.023,
 *      3624883.34 vs 114875.90).
 *    - Targeted Tests: testChiSquareRescalingIdenticalProportions, testChiSquareRescalingUnequalTotals,
 *      testChiSquareTestRescalingPValue.
 *
 * 2. Decision Branches Covered:
 *    - chiSquare(double[], long[]):
 *      * expected.length < 2 (true/false)
 *      * expected.length != observed.length (true/false)
 *      * !isPositive(expected) (true with 0.0, true with -1.0, false)
 *      * !isNonNegative(observed) (true with -1L, false with 0L and positive)
 *    - chiSquareTest(double[], long[], double alpha):
 *      * alpha <= 0 (true/false)
 *      * alpha > 0.5 (true/false)
 *      * boundary: alpha = 0.5, alpha = 0.001
 *    - checkArray(long[][]):
 *      * in.length < 2 (true/false)
 *      * in[0].length < 2 (true/false)
 *      * !isRectangular(in) (true/false)
 *      * !isNonNegative(in) (true/false)
 *    - chiSquareDataSetsComparison(long[], long[]):
 *      * observed1.length < 2 (true/false)
 *      * observed1.length != observed2.length (true/false)
 *      * !isNonNegative(observed1) / !isNonNegative(observed2)
 *      * countSum1 * countSum2 == 0 (sum1 = 0, sum2 = 0)
 *      * unequalCounts: countSum1 != countSum2 (true -> apply weight, false -> dev = obs1 - obs2)
 *      * observed1[i] == 0 && observed2[i] == 0 (true -> exception, false -> normal dev)
 *    - Setter & Constructor dependencies:
 *      * Default constructor, custom distribution constructor, setDistribution(), getDistributionFactory().
 */
public class ChiSquareTestImplGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testChiSquareGoodnessOfFitEqualSums() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        double[] expected = new double[]{10.0, 20.0, 30.0, 40.0};
        long[] observed = new long[]{10L, 20L, 30L, 40L};

        double stat = testStatistic.chiSquare(expected, observed);
        assertEquals(0.0, stat, 1e-9);
    }

    @Test(timeout = 4000)
    public void testChiSquareGoodnessOfFitNonZero() throws MathException {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        double[] expected = new double[]{20.0, 30.0, 50.0};
        long[] observed = new long[]{25L, 25L, 50L};

        // dev = (25-20)^2 / 20 + (25-30)^2 / 30 + (50-50)^2 / 50 = 25/20 + 25/30 + 0 = 1.25 + 0.8333333333333334 = 2.0833333333333335
        double stat = testStatistic.chiSquare(expected, observed);
        assertEquals(2.0833333333333335, stat, 1e-9);

        double p = testStatistic.chiSquareTest(expected, observed);
        assertTrue("p-value should be between 0 and 1", p > 0.0 && p < 1.0);

        boolean reject = testStatistic.chiSquareTest(expected, observed, 0.05);
        // p-value with df=2 at 2.0833 is ~0.3528 > 0.05, so do not reject null hypothesis
        assertFalse(reject);
    }

    @Test(timeout = 4000)
    public void testChiSquareContingencyTable2x2() throws MathException {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        // 2x2 contingency table
        long[][] counts = new long[][]{
            {10L, 20L},
            {30L, 40L}
        };
        // row sums: 30, 70; col sums: 40, 60; total: 100
        // expected: [12, 18], [28, 42]
        // sumSq = 4/12 + 4/18 + 4/28 + 4/42 = 1/3 + 2/9 + 1/7 + 2/21 = 0.7936507936507936
        double stat = testStatistic.chiSquare(counts);
        assertEquals(0.7936507936507936, stat, 1e-9);

        double p = testStatistic.chiSquareTest(counts);
        assertTrue(p > 0.0 && p < 1.0);

        boolean reject = testStatistic.chiSquareTest(counts, 0.5);
        // p with df=1 at 0.79365 is ~0.373 < 0.5
        assertTrue(reject);
    }

    @Test(timeout = 4000)
    public void testChiSquareContingencyTable3x2Independence() throws MathException {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        // Exact proportional counts (independence holds exactly)
        long[][] counts = new long[][]{
            {10L, 20L},
            {20L, 40L},
            {40L, 80L}
        };
        double stat = testStatistic.chiSquare(counts);
        assertEquals(0.0, stat, 1e-9);

        double p = testStatistic.chiSquareTest(counts);
        assertEquals(1.0, p, 1e-9);

        boolean reject = testStatistic.chiSquareTest(counts, 0.05);
        assertFalse(reject);
    }

    @Test(timeout = 4000)
    public void testDataSetsComparisonEqualTotals() throws MathException {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        long[] obs1 = new long[]{10L, 20L, 30L};
        long[] obs2 = new long[]{15L, 15L, 30L};
        // countSum1 = 60, countSum2 = 60 -> unequalCounts = false
        // dev: (10-15)=-5 -> 25/25 = 1.0; (20-15)=5 -> 25/35 = 5/7; (30-30)=0
        // stat = 1.0 + 5.0/7.0 = 12.0/7.0 = 1.7142857142857142
        double stat = testStatistic.chiSquareDataSetsComparison(obs1, obs2);
        assertEquals(12.0 / 7.0, stat, 1e-9);

        double p = testStatistic.chiSquareTestDataSetsComparison(obs1, obs2);
        assertTrue(p > 0.0 && p < 1.0);

        boolean reject = testStatistic.chiSquareTestDataSetsComparison(obs1, obs2, 0.05);
        assertFalse(reject);
    }

    @Test(timeout = 4000)
    public void testDataSetsComparisonUnequalTotalsProportional() throws MathException {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        long[] obs1 = new long[]{10L, 20L, 30L};
        long[] obs2 = new long[]{20L, 40L, 60L};
        // countSum1 = 60, countSum2 = 120 -> unequalCounts = true
        // weight = sqrt(60/120) = sqrt(0.5)
        // obs1/weight - obs2*weight = obs1*sqrt(2) - obs2/sqrt(2) = 0 for each i
        double stat = testStatistic.chiSquareDataSetsComparison(obs1, obs2);
        assertEquals(0.0, stat, 1e-9);

        double p = testStatistic.chiSquareTestDataSetsComparison(obs1, obs2);
        assertEquals(1.0, p, 1e-9);

        boolean reject = testStatistic.chiSquareTestDataSetsComparison(obs1, obs2, 0.05);
        assertFalse(reject);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Significance Level Limits
    // =========================================================================

    @Test(timeout = 4000)
    public void testSignificanceLevelBoundaryAlphaExactlyHalf() throws MathException {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        double[] expected = new double[]{10.0, 10.0};
        long[] observed = new long[]{10L, 10L};

        // alpha = 0.5 is upper boundary
        boolean reject = testStatistic.chiSquareTest(expected, observed, 0.5);
        assertFalse(reject);

        long[][] counts = new long[][]{{10L, 10L}, {10L, 10L}};
        assertFalse(testStatistic.chiSquareTest(counts, 0.5));

        long[] obs1 = new long[]{10L, 10L};
        long[] obs2 = new long[]{10L, 10L};
        assertFalse(testStatistic.chiSquareTestDataSetsComparison(obs1, obs2, 0.5));
    }

    @Test(timeout = 4000)
    public void testSignificanceLevelBoundarySmallPositiveAlpha() throws MathException {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        double[] expected = new double[]{10.0, 10.0};
        long[] observed = new long[]{10L, 10L};

        boolean reject = testStatistic.chiSquareTest(expected, observed, 0.0001);
        assertFalse(reject);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Math-102 Failure Paths)
    // =========================================================================

    /**
     * Targets Defects4J Math-102 bug:
     * When expected counts and observed counts do not have equal totals,
     * the implementation MUST rescale expected counts.
     * With expected = {10, 10, 10, 10} and observed = {20, 20, 20, 20},
     * the rescaled expected counts should be {20, 20, 20, 20}.
     * Correct statistic: 0.0.
     * Defective implementation yields: 40.0.
     */
    @Test(timeout = 4000)
    public void testChiSquareRescalingIdenticalProportions() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        double[] expected = new double[]{10.0, 10.0, 10.0, 10.0};
        long[] observed = new long[]{20L, 20L, 20L, 20L};

        double stat = testStatistic.chiSquare(expected, observed);
        assertEquals("Defect Math-102: Chi-square must rescale expected array when sums differ",
                0.0, stat, 1e-9);
    }

    /**
     * Targets Defects4J Math-102 bug:
     * sum(expected) = 60, sum(observed) = 120. Ratio = 2.0.
     * Rescaled expected = {20.0, 40.0, 60.0}.
     * Rescaled statistic = (25-20)^2/20 + (35-40)^2/40 + (60-60)^2/60 = 25/20 + 25/40 + 0 = 1.875.
     * Defective unscaled code produces: 63.75.
     */
    @Test(timeout = 4000)
    public void testChiSquareRescalingUnequalTotals() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        double[] expected = new double[]{10.0, 20.0, 30.0};
        long[] observed = new long[]{25L, 35L, 60L};

        double stat = testStatistic.chiSquare(expected, observed);
        assertEquals("Defect Math-102: Chi-square must rescale expected array to match observed total",
                1.875, stat, 1e-9);
    }

    /**
     * Targets Defects4J Math-102 p-value bug:
     * When proportions match perfectly despite unequal count sums,
     * rescaled statistic is 0.0, giving p-value = 1.0.
     */
    @Test(timeout = 4000)
    public void testChiSquareTestRescalingPValue() throws MathException {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        double[] expected = new double[]{5.0, 5.0, 5.0};
        long[] observed = new long[]{50L, 50L, 50L};

        double p = testStatistic.chiSquareTest(expected, observed);
        assertEquals("Defect Math-102: P-value must be 1.0 when observed perfectly matches rescaled expected",
                1.0, p, 1e-9);

        boolean reject = testStatistic.chiSquareTest(expected, observed, 0.05);
        assertFalse("Defect Math-102: Must not reject null hypothesis when samples match rescaled expected", reject);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testChiSquareExpectedLengthTooSmall() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquare(new double[]{1.0}, new long[]{1L});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testChiSquareLengthMismatch() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquare(new double[]{1.0, 2.0}, new long[]{1L, 2L, 3L});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testChiSquareExpectedContainsZero() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquare(new double[]{0.0, 2.0}, new long[]{1L, 2L});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testChiSquareExpectedContainsNegative() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquare(new double[]{-1.0, 2.0}, new long[]{1L, 2L});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testChiSquareObservedContainsNegative() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquare(new double[]{1.0, 2.0}, new long[]{-1L, 2L});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testChiSquareTestAlphaZero() throws MathException {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquareTest(new double[]{1.0, 2.0}, new long[]{1L, 2L}, 0.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testChiSquareTestAlphaNegative() throws MathException {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquareTest(new double[]{1.0, 2.0}, new long[]{1L, 2L}, -0.05);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testChiSquareTestAlphaGreaterThanHalf() throws MathException {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquareTest(new double[]{1.0, 2.0}, new long[]{1L, 2L}, 0.50001);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testContingencyTableTooFewRows() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquare(new long[][]{{1L, 2L}});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testContingencyTableTooFewColumns() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquare(new long[][]{{1L}, {2L}});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testContingencyTableNonRectangular() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquare(new long[][]{
            {1L, 2L},
            {1L, 2L, 3L}
        });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testContingencyTableNegativeEntries() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquare(new long[][]{
            {1L, 2L},
            {3L, -4L}
        });
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testContingencyTableTestAlphaZero() throws MathException {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquareTest(new long[][]{{1L, 2L}, {3L, 4L}}, 0.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testContingencyTableTestAlphaGreaterThanHalf() throws MathException {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquareTest(new long[][]{{1L, 2L}, {3L, 4L}}, 0.51);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDataSetsComparisonLengthTooSmall() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquareDataSetsComparison(new long[]{1L}, new long[]{1L});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDataSetsComparisonLengthMismatch() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquareDataSetsComparison(new long[]{1L, 2L}, new long[]{1L, 2L, 3L});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDataSetsComparisonObserved1Negative() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquareDataSetsComparison(new long[]{-1L, 2L}, new long[]{1L, 2L});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDataSetsComparisonObserved2Negative() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquareDataSetsComparison(new long[]{1L, 2L}, new long[]{1L, -2L});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDataSetsComparisonObserved1AllZero() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquareDataSetsComparison(new long[]{0L, 0L}, new long[]{1L, 2L});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDataSetsComparisonObserved2AllZero() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquareDataSetsComparison(new long[]{1L, 2L}, new long[]{0L, 0L});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDataSetsComparisonBothZeroAtSameBin() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquareDataSetsComparison(new long[]{1L, 0L, 3L}, new long[]{2L, 0L, 4L});
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDataSetsComparisonAlphaZero() throws MathException {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquareTestDataSetsComparison(new long[]{1L, 2L}, new long[]{2L, 1L}, 0.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDataSetsComparisonAlphaGreaterThanHalf() throws MathException {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquareTestDataSetsComparison(new long[]{1L, 2L}, new long[]{2L, 1L}, 0.500001);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testChiSquareNullExpected() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquare(null, new long[]{1L, 2L});
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testChiSquareNullObserved() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquare(new double[]{1.0, 2.0}, null);
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testContingencyTableNull() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        testStatistic.chiSquare((long[][]) null);
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Custom Distributions & Deprecated Factory
    // =========================================================================

    @Test(timeout = 4000)
    public void testCustomDistributionConstructorAndSetter() throws MathException {
        ChiSquaredDistribution customDist = new ChiSquaredDistributionImpl(2.0);
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl(customDist);

        double[] expected = new double[]{10.0, 10.0};
        long[] observed = new long[]{10L, 10L};
        double p = testStatistic.chiSquareTest(expected, observed);
        assertEquals(1.0, p, 1e-9);

        // Verify setDistribution
        ChiSquaredDistribution anotherDist = new ChiSquaredDistributionImpl(4.0);
        testStatistic.setDistribution(anotherDist);
        p = testStatistic.chiSquareTest(expected, observed);
        assertEquals(1.0, p, 1e-9);
    }

    @Test(timeout = 4000)
    @SuppressWarnings("deprecation")
    public void testGetDistributionFactory() {
        ChiSquareTestImpl testStatistic = new ChiSquareTestImpl();
        DistributionFactory factory = testStatistic.getDistributionFactory();
        assertNotNull("Distribution factory should not be null", factory);
    }
}