package org.apache.commons.math3.optim.nonlinear.vector;

import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DiagonalMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.NonSquareMatrixException;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.optim.OptimizationData;
import org.junit.Test;

import static org.junit.Assert.*;

/*
 * [Branch & Defect Analysis Matrix]
 * ----------------------------------------------------------------------------------------------------
 * Target Class: org.apache.commons.math3.optim.nonlinear.vector.Weight
 *
 * Target Defect (Defects4J / MATH-913):
 * - PolynomialFitterTest::testLargeSample -> OutOfMemoryError: Java heap space.
 *   The constructor Weight(double[]) improperly instantiated a dense RealMatrix via
 *   MatrixUtils.createRealMatrix(dim, dim) taking O(N^2) memory rather than utilizing DiagonalMatrix(weight)
 *   which requires O(N) memory.
 *
 * Decision / Condition Matrix:
 * 1. Weight(double[]):
 *    - Valid vector -> Matrix entries (i, i) == weight[i], off-diagonals == 0.0.
 *    - Memory profile / Representation -> Must be DiagonalMatrix instance, not dense Array2DRowRealMatrix.
 *    - Boundary: length = 0 -> Throws MathIllegalArgumentException / NotStrictlyPositiveException.
 *    - Boundary: length = 1 -> 1x1 matrix.
 *    - Null check: weight == null -> Throws NullPointerException / NullArgumentException.
 *
 * 2. Weight(RealMatrix):
 *    - Branch: weight.getColumnDimension() != weight.getRowDimension() (Non-square matrix)
 *      - columns > rows -> NonSquareMatrixException thrown with dimension details.
 *      - rows > columns -> NonSquareMatrixException thrown with dimension details.
 *    - Branch: weight.getColumnDimension() == weight.getRowDimension() (Square matrix)
 *      - 1x1, 2x2, symmetric, asymmetric -> weightMatrix copied successfully.
 *    - Null check: weight == null -> Throws NullPointerException.
 *
 * 3. getWeight():
 *    - Returns independent copy -> Mutating returned RealMatrix must not mutate internal state.
 *
 * 4. Immutability & Defensive Copying:
 *    - Mutating input array after Weight(double[]) does not affect internal state.
 *    - Mutating input RealMatrix after Weight(RealMatrix) does not affect internal state.
 * ----------------------------------------------------------------------------------------------------
 */
public class WeightGeminiTest {

    private static final double EPSILON = 1e-12;

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testVectorConstructorNormalOperation() {
        double[] diagonalValues = new double[] { 1.5, 2.5, 3.5 };
        Weight weight = new Weight(diagonalValues);

        RealMatrix matrix = weight.getWeight();
        assertNotNull("Returned weight matrix should not be null", matrix);
        assertEquals("Row dimension must match array length", 3, matrix.getRowDimension());
        assertEquals("Column dimension must match array length", 3, matrix.getColumnDimension());

        // Verify diagonal values
        assertEquals(1.5, matrix.getEntry(0, 0), EPSILON);
        assertEquals(2.5, matrix.getEntry(1, 1), EPSILON);
        assertEquals(3.5, matrix.getEntry(2, 2), EPSILON);

        // Verify off-diagonal values are zero
        assertEquals(0.0, matrix.getEntry(0, 1), EPSILON);
        assertEquals(0.0, matrix.getEntry(0, 2), EPSILON);
        assertEquals(0.0, matrix.getEntry(1, 0), EPSILON);
        assertEquals(0.0, matrix.getEntry(1, 2), EPSILON);
        assertEquals(0.0, matrix.getEntry(2, 0), EPSILON);
        assertEquals(0.0, matrix.getEntry(2, 1), EPSILON);
    }

    @Test(timeout = 4000)
    public void testMatrixConstructorSquareMatrix() {
        double[][] data = new double[][] {
            { 1.0, 2.0 },
            { 3.0, 4.0 }
        };
        RealMatrix inputMatrix = new Array2DRowRealMatrix(data);
        Weight weight = new Weight(inputMatrix);

        RealMatrix result = weight.getWeight();
        assertEquals(2, result.getRowDimension());
        assertEquals(2, result.getColumnDimension());
        assertEquals(1.0, result.getEntry(0, 0), EPSILON);
        assertEquals(2.0, result.getEntry(0, 1), EPSILON);
        assertEquals(3.0, result.getEntry(1, 0), EPSILON);
        assertEquals(4.0, result.getEntry(1, 1), EPSILON);
    }

    @Test(timeout = 4000)
    public void testGetWeightReturnsDefensiveCopy() {
        double[] diagonal = new double[] { 10.0, 20.0 };
        Weight weight = new Weight(diagonal);

        RealMatrix copy1 = weight.getWeight();
        copy1.setEntry(0, 0, 999.0);

        RealMatrix copy2 = weight.getWeight();
        assertEquals("Internal state must remain unmodified when mutating returned copy",
                     10.0, copy2.getEntry(0, 0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testVectorConstructorDefensiveCopy() {
        double[] diagonal = new double[] { 5.0, 6.0 };
        Weight weight = new Weight(diagonal);

        diagonal[0] = -100.0;
        assertEquals("Mutating the input array after construction should not affect Weight",
                     5.0, weight.getWeight().getEntry(0, 0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testMatrixConstructorDefensiveCopy() {
        RealMatrix original = MatrixUtils.createRealIdentityMatrix(2);
        Weight weight = new Weight(original);

        original.setEntry(0, 0, 42.0);
        assertEquals("Mutating the input matrix after construction should not affect Weight",
                     1.0, weight.getWeight().getEntry(0, 0), EPSILON);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testSingleElementVector() {
        double[] single = new double[] { 42.0 };
        Weight weight = new Weight(single);

        RealMatrix matrix = weight.getWeight();
        assertEquals(1, matrix.getRowDimension());
        assertEquals(1, matrix.getColumnDimension());
        assertEquals(42.0, matrix.getEntry(0, 0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testSingleElementMatrix() {
        RealMatrix singleMatrix = new Array2DRowRealMatrix(new double[][] { { -7.5 } });
        Weight weight = new Weight(singleMatrix);

        RealMatrix matrix = weight.getWeight();
        assertEquals(1, matrix.getRowDimension());
        assertEquals(1, matrix.getColumnDimension());
        assertEquals(-7.5, matrix.getEntry(0, 0), EPSILON);
    }

    @Test(timeout = 4000)
    public void testVectorWithZeroAndNegativeValues() {
        double[] values = new double[] { 0.0, -1.0, Double.MAX_VALUE, Double.MIN_VALUE };
        Weight weight = new Weight(values);

        RealMatrix matrix = weight.getWeight();
        assertEquals(0.0, matrix.getEntry(0, 0), EPSILON);
        assertEquals(-1.0, matrix.getEntry(1, 1), EPSILON);
        assertEquals(Double.MAX_VALUE, matrix.getEntry(2, 2), EPSILON);
        assertEquals(Double.MIN_VALUE, matrix.getEntry(3, 3), EPSILON);
    }

    @Test(timeout = 4000, expected = MathIllegalArgumentException.class)
    public void testEmptyVectorThrowsException() {
        new Weight(new double[0]);
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (MATH-913 / Large Sample OOM)
    // =========================================================================

    /**
     * Targets the root defect of MATH-913:
     * Weight(double[]) must use DiagonalMatrix internally rather than a dense RealMatrix.
     * On the defective code, this test will fail because getWeight() returns Array2DRowRealMatrix.
     */
    @Test(timeout = 4000)
    public void testWeightVectorUsesDiagonalMatrix() {
        double[] diagonal = new double[] { 1.0, 2.0, 3.0 };
        Weight weight = new Weight(diagonal);
        RealMatrix matrix = weight.getWeight();

        assertTrue("Weight created from a 1D vector MUST be backed by DiagonalMatrix for O(N) memory efficiency",
                   matrix instanceof DiagonalMatrix);
    }

    /**
     * Directly reproduces the OutOfMemoryError observed in PolynomialFitterTest::testLargeSample.
     * A large vector (e.g. 40,000 samples) should only allocate O(N) memory when backed by DiagonalMatrix (~320 KB).
     * On the defective version, allocating a 40000x40000 dense matrix requires ~12.8 GB, triggering OutOfMemoryError.
     */
    @Test(timeout = 4000)
    public void testLargeSampleMemoryEfficiency() {
        int sampleSize = 40000;
        double[] largeSample = new double[sampleSize];
        largeSample[0] = 1.0;
        largeSample[sampleSize - 1] = 2.0;

        Weight weight = new Weight(largeSample);
        RealMatrix matrix = weight.getWeight();

        assertEquals(sampleSize, matrix.getRowDimension());
        assertEquals(sampleSize, matrix.getColumnDimension());
        assertEquals(1.0, matrix.getEntry(0, 0), EPSILON);
        assertEquals(2.0, matrix.getEntry(sampleSize - 1, sampleSize - 1), EPSILON);
        assertTrue("Underlying matrix for large sample must be a DiagonalMatrix",
                   matrix instanceof DiagonalMatrix);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(timeout = 4000)
    public void testNonSquareMatrixMoreColumnsThrowsException() {
        // 2 rows, 3 columns
        RealMatrix nonSquare = MatrixUtils.createRealMatrix(2, 3);
        try {
            new Weight(nonSquare);
            fail("Expected NonSquareMatrixException when columns > rows");
        } catch (NonSquareMatrixException ex) {
            assertEquals("Wrong row dimension parameter check", 3, ex.getWrongRowDimension());
            assertEquals("Wrong column dimension parameter check", 2, ex.getWrongColumnDimension());
        }
    }

    @Test(timeout = 4000)
    public void testNonSquareMatrixMoreRowsThrowsException() {
        // 3 rows, 1 column
        RealMatrix nonSquare = MatrixUtils.createRealMatrix(3, 1);
        try {
            new Weight(nonSquare);
            fail("Expected NonSquareMatrixException when rows > columns");
        } catch (NonSquareMatrixException ex) {
            assertEquals("Wrong row dimension parameter check", 1, ex.getWrongRowDimension());
            assertEquals("Wrong column dimension parameter check", 3, ex.getWrongColumnDimension());
        }
    }

    @Test(timeout = 4000)
    public void testNullVectorThrowsException() {
        try {
            new Weight((double[]) null);
            fail("Expected NullPointerException or NullArgumentException on null double[]");
        } catch (NullPointerException | NullArgumentException expected) {
            // Success
        }
    }

    @Test(timeout = 4000)
    public void testNullMatrixThrowsException() {
        try {
            new Weight((RealMatrix) null);
            fail("Expected NullPointerException or NullArgumentException on null RealMatrix");
        } catch (NullPointerException | NullArgumentException expected) {
            // Success
        }
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testImplementsOptimizationData() {
        Weight weight = new Weight(new double[] { 1.0 });
        assertTrue("Weight must implement OptimizationData marker interface",
                   weight instanceof OptimizationData);
    }

    @Test(timeout = 4000)
    public void testMultipleGetWeightCallsProduceIndependentCopies() {
        Weight weight = new Weight(new double[] { 2.0, 4.0 });
        RealMatrix m1 = weight.getWeight();
        RealMatrix m2 = weight.getWeight();

        assertNotSame("Multiple calls to getWeight() must return different object instances", m1, m2);
        assertEquals(m1.getEntry(0, 0), m2.getEntry(0, 0), EPSILON);
        assertEquals(m1.getEntry(1, 1), m2.getEntry(1, 1), EPSILON);
    }
}