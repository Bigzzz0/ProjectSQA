/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.math.stat.descriptive.SummaryStatistics
 *
 * Decision Branches & Logic Paths Covered:
 * 1. addValue(double):
 *    - Default state updates (sum, sumsq, min, max, sumLog, secondMoment).
 *    - Branch: !(meanImpl instanceof Mean) -> true vs false (Defects4J defect!).
 *    - Branch: !(varianceImpl instanceof Variance) -> true vs false (Defects4J defect!).
 *    - Branch: !(geoMeanImpl instanceof GeometricMean) -> true vs false (Defects4J defect!).
 * 2. getStandardDeviation():
 *    - Branch: getN() == 0 -> returns NaN.
 *    - Branch: getN() == 1 -> returns 0.0.
 *    - Branch: getN() > 1 -> returns FastMath.sqrt(getVariance()).
 * 3. getPopulationVariance():
 *    - Instantiates Variance(secondMoment) with biasCorrected = false.
 * 4. clear():
 *    - Branch: meanImpl != mean vs meanImpl == mean.
 *    - Branch: varianceImpl != variance vs varianceImpl == variance.
 * 5. equals(Object) & hashCode():
 *    - Branch: object == this (identity).
 *    - Branch: !(object instanceof SummaryStatistics) (type check).
 *    - Branches in Precision.equalsIncludingNaN for all stats (geoMean, max, mean, min, n, sum, sumsq, variance).
 * 6. checkEmpty() guard in all 8 setters:
 *    - Branch: n == 0 (allowed).
 *    - Branch: n > 0 (throws MathIllegalStateException).
 * 7. copy(SummaryStatistics source, SummaryStatistics dest) & constructors:
 *    - MathUtils.checkNotNull on source and dest (null checks).
 *    - Branches for stats with embedded moments: varianceImpl instanceof Variance, meanImpl instanceof Mean, geoMeanImpl instanceof GeometricMean.
 *    - Branches for stat == statImpl references: geoMean, max, mean, min, sum, variance, sumLog, sumsq.
 *
 * Defects4J Bug Matrix:
 * - When meanImpl, varianceImpl, or geoMeanImpl are overridden with Commons-Math class instances
 *   (e.g., new Mean(), new Variance(), new GeometricMean()), addValue() uses `instanceof` check instead
 *   of reference equality `meanImpl != mean`. Because they ARE instances of Mean/Variance/GeometricMean,
 *   their increment() method is skipped, leaving them un-updated and returning NaN instead of correct stats.
 */
package org.apache.commons.math.stat.descriptive;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.commons.math.exception.MathIllegalStateException;
import org.apache.commons.math.exception.NullArgumentException;
import org.apache.commons.math.stat.descriptive.moment.GeometricMean;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.descriptive.moment.Variance;
import org.apache.commons.math.stat.descriptive.rank.Max;
import org.apache.commons.math.stat.descriptive.rank.Min;
import org.apache.commons.math.stat.descriptive.summary.Sum;
import org.apache.commons.math.stat.descriptive.summary.SumOfLogs;
import org.apache.commons.math.stat.descriptive.summary.SumOfSquares;
import org.apache.commons.math.util.FastMath;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Robust JUnit 4 white-box test suite for {@link SummaryStatistics}.
 */
public class SummaryStatisticsGeminiTest {

    private static final double TOLERANCE = 1E-10;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testCoreStatisticsComputation() {
        SummaryStatistics stats = new SummaryStatistics();
        double[] values = { 1.25, 2.5, 3.75, 4.5, 8.0 };

        for (double v : values) {
            stats.addValue(v);
        }

        assertEquals(5, stats.getN());
        assertEquals(1.25, stats.getMin(), TOLERANCE);
        assertEquals(8.0, stats.getMax(), TOLERANCE);
        assertEquals(20.0, stats.getSum(), TOLERANCE);
        assertEquals(4.0, stats.getMean(), TOLERANCE);

        // Sum of squares: 1.25^2 + 2.5^2 + 3.75^2 + 4.5^2 + 8^2 = 1.5625 + 6.25 + 14.0625 + 20.25 + 64 = 106.125
        assertEquals(106.125, stats.getSumsq(), TOLERANCE);

        // Second moment: sum((x - 4)^2) = (-2.75)^2 + (-1.5)^2 + (-0.25)^2 + (0.5)^2 + 4^2 = 7.5625 + 2.25 + 0.0625 + 0.25 + 16 = 26.125
        assertEquals(26.125, stats.getSecondMoment(), TOLERANCE);

        // Sample variance: 26.125 / (5 - 1) = 6.53125
        assertEquals(6.53125, stats.getVariance(), TOLERANCE);

        // Population variance: 26.125 / 5 = 5.225
        assertEquals(5.225, stats.getPopulationVariance(), TOLERANCE);

        // Standard deviation: sqrt(6.53125)
        assertEquals(FastMath.sqrt(6.53125), stats.getStandardDeviation(), TOLERANCE);

        // Geometric mean
        double expectedGeoMean = FastMath.exp((FastMath.log(1.25) + FastMath.log(2.5) + FastMath.log(3.75)
                + FastMath.log(4.5) + FastMath.log(8.0)) / 5.0);
        assertEquals(expectedGeoMean, stats.getGeometricMean(), TOLERANCE);

        // StatisticalSummary export
        StatisticalSummary summary = stats.getSummary();
        assertNotNull(summary);
        assertEquals(stats.getMean(), summary.getMean(), TOLERANCE);
        assertEquals(stats.getVariance(), summary.getVariance(), TOLERANCE);
        assertEquals(stats.getN(), summary.getN());
        assertEquals(stats.getMax(), summary.getMax(), TOLERANCE);
        assertEquals(stats.getMin(), summary.getMin(), TOLERANCE);
        assertEquals(stats.getSum(), summary.getSum(), TOLERANCE);
    }

    @Test(timeout = 4000)
    public void testClearResetsAllState() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(10.0);
        stats.addValue(20.0);
        assertEquals(2, stats.getN());

        stats.clear();
        assertEquals(0, stats.getN());
        assertTrue(Double.isNaN(stats.getSum()));
        assertTrue(Double.isNaN(stats.getMean()));
        assertTrue(Double.isNaN(stats.getMin()));
        assertTrue(Double.isNaN(stats.getMax()));
        assertTrue(Double.isNaN(stats.getVariance()));
        assertTrue(Double.isNaN(stats.getGeometricMean()));
        assertTrue(Double.isNaN(stats.getStandardDeviation()));
        assertTrue(Double.isNaN(stats.getSumOfLogs()));
        assertTrue(Double.isNaN(stats.getSumsq()));
        assertTrue(Double.isNaN(stats.getSecondMoment()));
    }

    @Test(timeout = 4000)
    public void testClearWithOverriddenMeanAndVariance() {
        SummaryStatistics stats = new SummaryStatistics();
        Mean customMean = new Mean();
        Variance customVariance = new Variance();
        stats.setMeanImpl(customMean);
        stats.setVarianceImpl(customVariance);

        stats.addValue(5.0);
        stats.clear();

        assertEquals(0, stats.getN());
        assertTrue(Double.isNaN(stats.getMean()));
        assertTrue(Double.isNaN(stats.getVariance()));
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testEmptyStatisticsBoundaries() {
        SummaryStatistics stats = new SummaryStatistics();

        assertEquals(0L, stats.getN());
        assertTrue(Double.isNaN(stats.getSum()));
        assertTrue(Double.isNaN(stats.getSumsq()));
        assertTrue(Double.isNaN(stats.getMean()));
        assertTrue(Double.isNaN(stats.getStandardDeviation()));
        assertTrue(Double.isNaN(stats.getVariance()));
        assertTrue(Double.isNaN(stats.getPopulationVariance()));
        assertTrue(Double.isNaN(stats.getMax()));
        assertTrue(Double.isNaN(stats.getMin()));
        assertTrue(Double.isNaN(stats.getGeometricMean()));
        assertTrue(Double.isNaN(stats.getSumOfLogs()));
        assertTrue(Double.isNaN(stats.getSecondMoment()));
    }

    @Test(timeout = 4000)
    public void testSingleValueBoundaries() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(42.0);

        assertEquals(1L, stats.getN());
        assertEquals(42.0, stats.getSum(), TOLERANCE);
        assertEquals(42.0 * 42.0, stats.getSumsq(), TOLERANCE);
        assertEquals(42.0, stats.getMean(), TOLERANCE);
        assertEquals(42.0, stats.getMin(), TOLERANCE);
        assertEquals(42.0, stats.getMax(), TOLERANCE);
        assertEquals(42.0, stats.getGeometricMean(), TOLERANCE);
        assertEquals(FastMath.log(42.0), stats.getSumOfLogs(), TOLERANCE);
        assertEquals(0.0, stats.getSecondMoment(), TOLERANCE);
        assertEquals(0.0, stats.getStandardDeviation(), TOLERANCE);
        assertEquals(0.0, stats.getVariance(), TOLERANCE);
        assertEquals(0.0, stats.getPopulationVariance(), TOLERANCE);
    }

    @Test(timeout = 4000)
    public void testNegativeAndZeroValues() {
        SummaryStatistics stats = new SummaryStatistics();
        stats.addValue(-10.0);
        stats.addValue(0.0);
        stats.addValue(10.0);

        assertEquals(3L, stats.getN());
        assertEquals(-10.0, stats.getMin(), TOLERANCE);
        assertEquals(10.0, stats.getMax(), TOLERANCE);
        assertEquals(0.0, stats.getSum(), TOLERANCE);
        assertEquals(0.0, stats.getMean(), TOLERANCE);
        assertEquals(200.0, stats.getSumsq(), TOLERANCE);
        assertEquals(100.0, stats.getVariance(), TOLERANCE);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testOverrideMeanWithMathClass() {
        double[] scores = { 1.0, 2.0, 3.0