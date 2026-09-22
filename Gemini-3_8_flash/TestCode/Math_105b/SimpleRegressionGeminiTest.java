/* [Branch & Defect Analysis Matrix]
 * -----------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.math.stat.regression.SimpleRegression
 *
 * Branch & Condition Coverage Targets:
 * - addData(x, y):
 *     * Branch n == 0: initial assignment (xbar, ybar)
 *     * Branch n > 0: incremental update of sumXX, sumYY, sumXY, xbar, ybar
 * - addData(double[][]):
 *     * Array traversal and bulk ingestion
 * - clear():
 *     * Reset of state variables (sumX, sumXX, sumY, sumYY, sumXY, n)
 * - getSlope():
 *     * Condition n < 2 -> Double.NaN
 *     * Condition Math.abs(sumXX) < 10 * Double.MIN_VALUE -> Double.NaN
 *     * Normal calculation -> sumXY / sumXX
 * - getTotalSumSquares():
 *     * Condition n < 2 -> Double.NaN
 *     * Normal calculation -> sumYY
 * - getMeanSquareError():
 *     * Condition n < 3 -> Double.NaN
 *     * Normal calculation -> getSumSquaredErrors() / (n - 2)
 * - getR():
 *     * Condition b1 < 0 -> returns -sqrt(RSquare)
 *     * Condition b1 >= 0 -> returns +sqrt(RSquare)
 * - getSlopeConfidenceInterval(alpha):
 *     * Condition alpha >= 1 -> IllegalArgumentException
 *     * Condition alpha <= 0 -> IllegalArgumentException
 *     * Valid alpha -> getSlopeStdErr() * tDist.inverseCumulativeProbability(1 - alpha/2)
 * - getSlopeConfidenceInterval():
 *     * Default 0.05 alpha overload
 * - getSignificance():
 *     * 2 * (1 - tDist.cumulativeProbability(|b1| / getSlopeStdErr()))
 * - Defect Target:
 *     * testSSENonNegative: Javadoc contract specifies getSumSquaredErrors() must return >= 0.0
 *       even with precision roundoff on collinear data. Defective code omits non-negative
 *       constraint (Math.max(0d, ...)), yielding negative SSE and failing the assertion.
 * -----------------------------------------------------------------------------------------
 */

package org.apache.commons.math.stat.regression;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.apache.commons.math.MathException;

public class SimpleRegressionGeminiTest {

    private static final double EPSILON = 1e-10;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testStandardLinearFitAndPredictions() {
        SimpleRegression regression = new SimpleRegression();
        // y = 2x + 1
        regression.addData(1.0, 3.0);
        regression.addData(2.0, 5.0);
        regression.addData(3.0, 7.0);
        regression.addData(4.0, 9.0);

        assertEquals(4L, regression.getN());
        assertEquals(2.0, regression.getSlope(), EPSILON);
        assertEquals(1.0, regression.getIntercept(), EPSILON);

        // predict(x)
        assertEquals(11.0, regression.predict(5.0), EPSILON);
        assertEquals(1.0, regression.predict(0.0), EPSILON);
        assertEquals(-1.0, regression.predict(-1.0), EPSILON);

        // SSR, SSTO, SSE, R, RSquare
        // x values: 1, 2, 3, 4 (mean 2.5), sumXX = 5
        // y values: 3, 5, 7, 9 (mean 6), sumYY = 20
        assertEquals(20.0, regression.getTotalSumSquares(), EPSILON);
        assertEquals(20.0, regression.getRegressionSumSquares(), EPSILON);
        assertEquals(0.0, regression.getSumSquaredErrors(), EPSILON);
        assertEquals(1.0, regression.getRSquare(), EPSILON);
        assertEquals(1.0, regression.getR(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testNegativeSlopeAndR() {
        SimpleRegression regression = new SimpleRegression();
        // y = -3x + 10
        double[][] data = {
            {1.0, 7.0},
            {2.0, 4.0},
            {3.0, 1.0},
            {4.0, -2.0}
        };
        regression.addData(data);

        assertEquals(4L, regression.getN());
        assertEquals(-3.0, regression.getSlope(), EPSILON);
        assertEquals(10.0, regression.getIntercept(), EPSILON);
        assertEquals(-1.0, regression.getR(), EPSILON);
        assertEquals(1.0, regression.getRSquare(), EPSILON);
    }

    @Test(timeout = 4000)
    public void testClearAndReaccumulate() {
        SimpleRegression regression = new SimpleRegression();
        regression.addData(1.0, 2.0);
        regression.addData(2.0, 4.0);
        regression.addData(3.0, 6.0);
        assertEquals(3L, regression.getN());

        regression.clear();
        assertEquals(0L, regression.getN());
        assertTrue(Double.isNaN