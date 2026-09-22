package org.apache.commons.math.stat.correlation;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math.MathException;
import org.apache.commons.math.linear.BlockRealMatrix;
import org.apache.commons.math.linear.RealMatrix;

/*
 * [Branch & Defect Analysis Matrix]
 *
 * Targeted Class: org.apache.commons.math.stat.correlation.PearsonsCorrelation
 *
 * Core Decision Branches & Boundary Conditions:
 * 1. Constructor Coverage:
 *    - Default constructor PearsonsCorrelation(): correlationMatrix = null, nObs = 0.
 *    - PearsonsCorrelation(double[][]): delegates to BlockRealMatrix, checks data sufficiency.
 *    - PearsonsCorrelation(RealMatrix): checks sufficiency, sets nObs, calls computeCorrelationMatrix.
 *    - PearsonsCorrelation(Covariance): checks null covarianceMatrix branch; computes correlation.
 *    - PearsonsCorrelation(RealMatrix, int): sets nObs, calls covarianceToCorrelation.
 *
 * 2. checkSufficientData(RealMatrix):
 *    - nRows < 2 && nCols >= 2 -> throws IllegalArgumentException.
 *    - nRows >= 2 && nCols < 2 -> throws IllegalArgumentException.
 *    - nRows < 2 && nCols < 2 -> throws IllegalArgumentException.
 *    - nRows >= 2 && nCols >= 2 -> valid pass-through.
 *
 * 3. correlation(double[], double[]):
 *    - xArray.length == yArray.length && xArray.length > 1 -> executes SimpleRegression.getR().
 *    - xArray.length != yArray.length -> throws IllegalArgumentException.
 *    - xArray.length == yArray.length && xArray.length <= 1 (lengths 0, 1) -> throws IllegalArgumentException.
 *
 * 4. computeCorrelationMatrix(RealMatrix / double[][]):
 *    - Diagonal entries set to 1.0 (i == j).
 *    - Off-diagonal symmetric computation (i != j, outMatrix[i][j] == outMatrix[j][i]).
 *
 * 5. covarianceToCorrelation(RealMatrix):
 *    - Diagonal entries set to 1.0.
 *    - Scaling formula: cov(i, j) / (sqrt(cov(i, i)) * sqrt(cov(j, j))).
 *
 * 6. getCorrelationStandardErrors():
 *    - Formula: sqrt((1 - r^2) / (n - 2)).
 *    - Diagonal entries: r = 1.0 => standard error = 0.0.
 *
 * 7. getCorrelationPValues():
 *    - Diagonal (i == j): set to 0.0.
 *    - Off-diagonal (i != j): 2 * (1 - tDist.cumulativeProbability(t)).
 *
 * [DEFECT-TARGETED GROUND TRUTH (Defects4J MATH-371)]:
 * - org.apache.commons.math.stat.correlation.PearsonsCorrelationTest::testPValueNearZero
 * - Ground Truth Cause: For correlation coefficients r -> 1.0 or -1.0 with large sample size,
 *   t is large. The expression `2 * (1 - tDistribution.cumulativeProbability(t))` suffers from
 *   catastrophic cancellation because `cumulativeProbability(t)` evaluates to 1.0 in standard double
 *   precision, incorrectly producing a p-value of 0.0 instead of a small strictly positive probability.
 *   The test `testPValueNearZero` verifies that the computed p-value is strictly greater than 0.0.
 */
public class PearsonsCorrelationGeminiTest {

    private static final double EPSILON = 1e-12;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefaultConstructorAndStatelessMethods() {
        PearsonsCorrelation correlation = new PearsonsCorrelation();
        assertNull("Default constructor should leave correlationMatrix null", correlation.getCorrelationMatrix());

        double[] x = new double[] { 1.0, 2.0, 3.0, 4.0, 5.0 };
        double[] y = new double[] { 2.0, 4.0, 6.0, 8.0, 10.0 };
        double r = correlation.correlation(x, y);
        assertEquals("Perfect positive linear correlation should be 1.0", 1.0, r, EPSILON);

        double[][] data = new double[][] {
            { 1.0, 2.0 },
            { 2.0, 4.0 },
            { 3.0, 6.0 }
        };
        RealMatrix matrix = correlation.computeCorrelationMatrix(data);
        assertNotNull(matrix);
        assertEquals(2, matrix.getRowDimension());
        assertEquals(2, matrix.getColumnDimension());
        assertEquals(1.0, matrix.getEntry(0, 0), EPSILON);
        assertEquals(1.0, matrix.getEntry(1, 1), EPSILON);
        assertEquals(1.0, matrix.getEntry(0, 1), EPSILON);
        assertEquals(1.0, matrix.getEntry(1, 0), EPSILON);
    }

    @Test(timeout = 4000)
    public void test2DArrayConstructor() {
        double[][] data = new double[][] {
            { 1.0, 5.0, 10.0 },
            { 2.0, 4.0, 20.0 },
            { 3.0, 3.0, 30.0 },
            { 4.0, 2.0, 40.0 },
            { 5.0, 1.0, 50.0 }
        };
        PearsonsCorrelation correlation = new PearsonsCorrelation(data);
        RealMatrix matrix = correlation.getCorrelationMatrix();
        assertNotNull("Correlation matrix must not be null", matrix);
        assertEquals(3, matrix.getRowDimension());
        assertEquals(3, matrix.getColumnDimension());

        // Col 0 vs Col 1 is perfectly negatively correlated: r = -1.0
        assertEquals(-1.0, matrix.getEntry(0, 1), EPSILON);
        assertEquals(-1.0, matrix.getEntry(1, 0), EPSILON);

        // Col 0 vs Col 2 is perfectly positively correlated: r = 1.0
        assertEquals(1.0, matrix.getEntry(0, 2), EPSILON);
        assertEquals(1.0, matrix.getEntry(2, 0), EPSILON);

        // Diagonals must be 1.0
        assertEquals(1.0, matrix.getEntry(0, 0), EPSILON);
        assertEquals(1.0, matrix.getEntry(1, 1), EPSILON);
        assertEquals(1.0, matrix.getEntry(2, 2), EPSILON);
    }

    @Test(timeout = 4000)
    public void testRealMatrixConstructor() {
        double[][] data = new double[][] {
            { 1.0, 2.0 },
            { 3.0, 5.0 },
            { 5.0, 6.0 },
            { 7.0, 8.0 }
        };
        RealMatrix input = new BlockRealMatrix(data);
        PearsonsCorrelation correlation = new PearsonsCorrelation(input);
        RealMatrix matrix = correlation.getCorrelationMatrix();

        assertNotNull(matrix);
        assertEquals(2, matrix.getRowDimension());
        assertEquals(2, matrix.getColumnDimension());
        assertEquals(1.0, matrix.getEntry(0, 0), EPSILON);
        assertEquals(1.0, matrix.getEntry(1, 1), EPSILON);
        assertTrue(matrix.getEntry(0, 1) > 0.95);
        assertEquals(matrix.getEntry(0, 1), matrix.getEntry(1, 0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testCovarianceConstructor() {
        double[][] data = new double[][] {
            { 1.0, 2.0 },
            { 2.0, 1.0 },
            { 3.0, 4.0 },
            { 4.0, 3.0 }
        };
        Covariance covariance = new Covariance(data);
        PearsonsCorrelation correlationFromCov = new PearsonsCorrelation(covariance);
        PearsonsCorrelation correlationFromData = new PearsonsCorrelation(data);

        RealMatrix m1 = correlationFromCov.getCorrelationMatrix();
        RealMatrix m2 = correlationFromData.getCorrelationMatrix();

        for (int i = 0; i < m1.getRowDimension(); i++) {
            for (int j = 0; j < m1.getColumnDimension(); j++) {
                assertEquals("Correlation from covariance must match correlation from raw data",
                        m2.getEntry(i, j), m1.getEntry(i, j), 1e-10);
            }
        }
    }

    @Test(timeout = 4000)
    public void testCovarianceMatrixAndObservationsConstructor() {
        double[][] covData = new double[][] {
            { 4.0, 2.0 },
            { 2.0, 9.0 }
        };
        RealMatrix covMatrix = new BlockRealMatrix(covData);
        PearsonsCorrelation pc = new PearsonsCorrelation(covMatrix, 10);
        RealMatrix rMatrix = pc.getCorrelationMatrix();

        // r = cov(X,Y) / (sigma_X * sigma_Y) = 2.0 / (2.0 * 3.0) = 1.0 / 3.0
        double expectedR = 1.0 / 3.0;
        assertEquals(1.0, rMatrix.getEntry(0, 0), EPSILON);
        assertEquals(1.0, rMatrix.getEntry(1, 1), EPSILON);
        assertEquals(expectedR, rMatrix.getEntry(0, 1), EPSILON);
        assertEquals(expectedR, rMatrix.getEntry(1, 0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testGetCorrelationStandardErrors() {
        double[][] data = new double[][] {
            { 1.0, 10.0 },
            { 2.0, 20.0 },
            { 3.0, 30.0 },
            { 4.0, 40.0 },
            { 5.0, 50.0 }
        };
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        RealMatrix stdErrors = pc.getCorrelationStandardErrors();

        assertNotNull(stdErrors);
        assertEquals(2, stdErrors.getRowDimension());
        assertEquals(2, stdErrors.getColumnDimension());

        // For perfect correlation r = 1.0, SE = sqrt((1 - 1) / (5 - 2)) = 0.0
        assertEquals(0.0, stdErrors.getEntry(0, 0), EPSILON);
        assertEquals(0.0, stdErrors.getEntry(1, 1), EPSILON);
        assertEquals(0.0, stdErrors.getEntry(0, 1), EPSILON);
        assertEquals(0.0, stdErrors.getEntry(1, 0), EPSILON);

        // General test case with n = 5, r = 0.5: SE = sqrt((1 - 0.25) / 3) = sqrt(0.25) = 0.5
        double[][] covData = new double[][] {
            { 1.0, 0.5 },
            { 0.5, 1.0 }
        };
        PearsonsCorrelation pcSynthetic = new PearsonsCorrelation(new BlockRealMatrix(covData), 5);
        RealMatrix seSynthetic = pcSynthetic.getCorrelationStandardErrors();
        assertEquals(0.5, seSynthetic.getEntry(0, 1), EPSILON);
        assertEquals(0.5, seSynthetic.getEntry(1, 0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testGetCorrelationPValuesNormal() throws MathException {
        double[][] data = new double[][] {
            { 1.0, 2.0 },
            { 2.0, 1.0 },
            { 3.0, 4.0 },
            { 4.0, 3.0 },
            { 5.0, 6.0 },
            { 6.0, 5.0 }
        };
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        RealMatrix pValues = pc.getCorrelationPValues();

        assertNotNull(pValues);
        assertEquals(2, pValues.getRowDimension());
        assertEquals(2, pValues.getColumnDimension());

        // Diagonals must be 0.0
        assertEquals(0.0, pValues.getEntry(0, 0), EPSILON);
        assertEquals(0.0, pValues.getEntry(1, 1), EPSILON);

        // Off-diagonals must be identical and in range (0, 1)
        double p01 = pValues.getEntry(0, 1);
        double p10 = pValues.getEntry(1, 0);
        assertEquals(p01, p10, EPSILON);
        assertTrue("p-value should be > 0.0", p01 > 0.0);
        assertTrue("p-value should be <= 1.0", p01 <= 1.0);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testMinimalSufficientDataBoundary2x2() {
        double[][] data = new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        };
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        RealMatrix r = pc.getCorrelationMatrix();
        assertEquals(2, r.getRowDimension());
        assertEquals(2, r.getColumnDimension());
        assertEquals(1.0, r.getEntry(0, 1), EPSILON);
    }

    @Test(timeout = 4000)
    public void testArrayCorrelationMinimalLength2() {
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double[] x = new double[] { 10.0, 20.0 };
        double[] y = new double[] { 5.0, 1.0 };
        double r = pc.correlation(x, y);
        assertEquals("Two points define a line: r = -1.0", -1.0, r, EPSILON);
    }

    @Test(timeout = 4000)
    public void testConstantSeriesProducesNaNCorrelation() {
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double[] x = new double[] { 3.0, 3.0, 3.0, 3.0 };
        double[] y = new double[] { 1.0, 2.0, 3.0, 4.0 };
        double r = pc.correlation(x, y);
        assertTrue("Correlation of constant series should be NaN", Double.isNaN(r));
    }

    @Test(timeout = 4000)
    public void testRectangularNonSquareData() {
        double[][] data = new double[][] {
            { 1.0, 2.0, 3.0 },
            { 2.0, 4.0, 6.0 },
            { 3.0, 6.0, 9.0 },
            { 4.0, 8.0, 12.0 }
        };
        PearsonsCorrelation pc = new PearsonsCorrelation(data);
        RealMatrix matrix = pc.getCorrelationMatrix();
        assertEquals(3, matrix.getRowDimension());
        assertEquals(3, matrix.getColumnDimension());
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(1.0, matrix.getEntry(i, j), EPSILON);
            }
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J testPValueNearZero)
    // =========================================================================

    /**
     * Targets Defects4J known fault:
     * PearsonsCorrelation.getCorrelationPValues() suffers from precision loss when
     * r is close to 1.0 (or -1.0) and degrees of freedom are sufficiently high.
     * The defective code computes:
     *   out[i][j] = 2 * (1 - tDistribution.cumulativeProbability(t));
     * When t is large, tDistribution.cumulativeProbability(t) evaluates to 1.0,
     * causing 2 * (1 - 1.0) = 0.0, violating the invariant that p > 0.
     */
    @Test(timeout = 4000)
    public void testPValueNearZero() throws Exception {
        double[][] data = new double[50][2];
        for (int i = 0; i < 50; i++) {
            data[i][0] = i + 1;
            data[i][1] = (i + 1) * 2.0;
        }
        // Introduce small perturbation so that r is very close to 1.0 but not exactly 1.0
        data[4][1] = 11.0;

        PearsonsCorrelation corr = new PearsonsCorrelation(data);
        RealMatrix pValues = corr.getCorrelationPValues();
        double p = pValues.getEntry(0, 1);

        // Verification of defect: p-value must be strictly positive (> 0.0) and very small (< 1e-10)
        assertTrue("p-value should be strictly positive (> 0.0) but was: " + p, p > 0.0);
        assertTrue("p-value should be extremely small (< 1e-10) but was: " + p, p < 1e-10);
    }

    @Test(timeout = 4000)
    public void testPValueNearZeroNegativeCorrelation() throws Exception {
        double[][] data = new double[50][2];
        for (int i = 0; i < 50; i++) {
            data[i][0] = i + 1;
            data[i][1] = -(i + 1) * 2.0;
        }
        data[4][1] = -11.0;

        PearsonsCorrelation corr = new PearsonsCorrelation(data);
        RealMatrix pValues = corr.getCorrelationPValues();
        double p = pValues.getEntry(0, 1);

        assertTrue("p-value for near -1.0 correlation should be strictly positive (> 0.0) but was: " + p, p > 0.0);
        assertTrue("p-value should be extremely small (< 1e-10) but was: " + p, p < 1e-10);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsufficientRowsThrowsException() {
        double[][] data = new double[][] {
            { 1.0, 2.0, 3.0 }
        };
        new PearsonsCorrelation(data);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsufficientColumnsThrowsException() {
        double[][] data = new double[][] {
            { 1.0 },
            { 2.0 },
            { 3.0 }
        };
        new PearsonsCorrelation(data);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testInsufficientData1x1MatrixThrowsException() {
        double[][] data = new double[][] {
            { 1.0 }
        };
        new PearsonsCorrelation(data);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testNullCovarianceMatrixThrowsException() {
        Covariance nullCovariance = new Covariance();
        new PearsonsCorrelation(nullCovariance);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCorrelationDifferentArrayLengthsThrowsException() {
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double[] x = new double[] { 1.0, 2.0, 3.0 };
        double[] y = new double[] { 1.0, 2.0 };
        pc.correlation(x, y);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCorrelationArrayLengthLessThanTwoThrowsException() {
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double[] x = new double[] { 1.0 };
        double[] y = new double[] { 2.0 };
        pc.correlation(x, y);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testCorrelationEmptyArraysThrowsException() {
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double[] x = new double[0];
        double[] y = new double[0];
        pc.correlation(x, y);
    }

    // =========================================================================
    // Partition E: Object Contract & Mathematical Invariance
    // =========================================================================

    @Test(timeout = 4000)
    public void testCovarianceToCorrelationMathematicalConsistency() {
        PearsonsCorrelation pc = new PearsonsCorrelation();
        double[][] covData = new double[][] {
            { 16.0, 8.0, -4.0 },
            { 8.0, 25.0, 0.0 },
            { -4.0, 0.0, 9.0 }
        };
        RealMatrix covMatrix = new BlockRealMatrix(covData);
        RealMatrix corrMatrix = pc.covarianceToCorrelation(covMatrix);

        assertEquals(1.0, corrMatrix.getEntry(0, 0), EPSILON);
        assertEquals(1.0, corrMatrix.getEntry(1, 1), EPSILON);
        assertEquals(1.0, corrMatrix.getEntry(2, 2), EPSILON);

        // cov(0, 1) / (sqrt(16) * sqrt(25)) = 8 / (4 * 5) = 8 / 20 = 0.4
        assertEquals(0.4, corrMatrix.getEntry(0, 1), EPSILON);
        assertEquals(0.4, corrMatrix.getEntry(1, 0), EPSILON);

        // cov(0, 2) / (sqrt(16) * sqrt(9)) = -4 / (4 * 3) = -4 / 12 = -1/3
        assertEquals(-1.0 / 3.0, corrMatrix.getEntry(0, 2), EPSILON);
        assertEquals(-1.0 / 3.0, corrMatrix.getEntry(2, 0), EPSILON);

        // cov(1, 2) / (sqrt(25) * sqrt(9)) = 0 / 15 = 0.0
        assertEquals(0.0, corrMatrix.getEntry(1, 2), EPSILON);
        assertEquals(0.0, corrMatrix.getEntry(2, 1), EPSILON);
    }

    @Test(timeout = 4000)
    public void testComputeCorrelationMatrixEquivalence() {
        double[][] data = new double[][] {
            { 1.0, 3.0, 7.0 },
            { 2.0, 8.0, 4.0 },
            { 4.0, 2.0, 1.0 },
            { 5.0, 9.0, 6.0 }
        };
        PearsonsCorrelation pc = new PearsonsCorrelation();
        RealMatrix m1 = pc.computeCorrelationMatrix(data);
        RealMatrix m2 = pc.computeCorrelationMatrix(new BlockRealMatrix(data));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                assertEquals(m1.getEntry(i, j), m2.getEntry(i, j), EPSILON);
            }
        }
    }
}