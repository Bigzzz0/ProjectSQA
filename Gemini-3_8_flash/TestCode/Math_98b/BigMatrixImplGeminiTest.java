package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;

/* [Branch & Defect Analysis Matrix]
 *
 * Known Defect MATH-209:
 * - In BigMatrixImpl.operate(BigDecimal[] v) or related methods:
 *   `final BigDecimal[] out = new BigDecimal[v.length];`
 *   When multiplying a non-square matrix where rowDimension != columnDimension,
 *   e.g. 2x3 matrix with vector length 3:
 *   The loop runs `for (int row = 0; row < nRows; row++)` (nRows = 2), but if
 *   nRows > v.length (e.g. 3x2 matrix with vector length 2), `out` was allocated
 *   with `v.length` (2), whereas the result vector must have length `nRows` (3).
 *   Accessing `out[row]` when `row == 2` throws `ArrayIndexOutOfBoundsException: 2`.
 *   Targeted test: `testMath209OperateNonSquareDimensionMismatch()`.
 *
 * Partition A: Core Functional Logic & State Transitions
 * - Constructors: BigMatrixImpl(), BigMatrixImpl(r, c), BigMatrixImpl(BigDecimal[][]),
 *   BigMatrixImpl(BigDecimal[][], boolean), BigMatrixImpl(double[][]),
 *   BigMatrixImpl(String[][]), BigMatrixImpl(BigDecimal[]).
 * - Matrix operations: add, subtract, multiply, preMultiply (both BigMatrix & BigMatrixImpl types).
 * - Scalar ops: scalarAdd, scalarMultiply.
 * - Decompositions & solvers: LU decomposition, solve(BigMatrix), solve(BigDecimal[]),
 *   solve(double[]), inverse, getDeterminant, getTrace, getNorm.
 * - Accessors: getEntry, getEntryAsDouble, getRow, getColumn, getRowMatrix, getColumnMatrix,
 *   getRowAsDoubleArray, getColumnAsDoubleArray, getData, getDataRef, getDataAsDoubleArray.
 * - Configurations: scale and roundingMode getters and setters.
 * - Submatrix extraction and mutation: getSubMatrix, setSubMatrix.
 * - Structural queries: isSquare, isSingular, getRowDimension, getColumnDimension.
 *
 * Partition B: Boundary Value Analysis (BVA) & Extremes
 * - 1x1 matrix determinants, inverses, traces, operations.
 * - Permutations with positive and negative parities during LU decomposition.
 * - Zero determinant and singular matrices.
 *
 * Partition C: Defect-Targeted Branch Zone
 * - MATH-209: Non-square operate(BigDecimal[]) and operate(double[]) where nRows > nCols.
 *
 * Partition D: Exception & Defensive Guard Paths
 * - Zero / negative dimensions in constructors.
 * - Non-rectangular or empty matrix inputs in constructors and setSubMatrix.
 * - Matrix dimension mismatches in add, subtract, multiply, solve, operate, preMultiply.
 * - Non-invertible or singular matrix solving and LU decomposition.
 * - MatrixIndexException bounds on getEntry, getSubMatrix, getRow, getColumn, etc.
 *
 * Partition E: Object Lifecycle & Contract Integrity
 * - toString representation check.
 * - equals & hashCode contract (reflexive, symmetric, dimension mismatch, value mismatch).
 */
public class BigMatrixImplGeminiTest {

    private static final BigDecimal ZERO = BigDecimal.ZERO;
    private static final BigDecimal ONE = BigDecimal.ONE;

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (MATH-209)
    // -------------------------------------------------------------------------

    /**
     * Targets MATH-209: operate on a non-square matrix where rowDimension > columnDimension.
     * In defective code, `out` array was allocated with size `v.length` (columnDimension)
     * instead of `nRows` (rowDimension), resulting in ArrayIndexOutOfBoundsException.
     */
    @Test(timeout = 4000)
    public void testMath209OperateNonSquare() {
        // 3 rows, 2 columns -> output vector should have length 3
        double[][] data = {
            {1.0, 2.0},
            {3.0, 4.0},
            {5.0, 6.0}
        };
        BigMatrix matrix = new BigMatrixImpl(data);
        BigDecimal[] v = new BigDecimal[] { new BigDecimal(1), new BigDecimal(2) };

        BigDecimal[] result = matrix.operate(v);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(new BigDecimal(5), result[0]);   // 1*1 + 2*2 = 5
        assertEquals(new BigDecimal(11), result[1]);  // 3*1 + 4*2 = 11
        assertEquals(new BigDecimal(17), result[2]);  // 5*1 + 6*2 = 17
    }

    @Test(timeout = 4000)
    public void testMath209OperateDoubleArrayNonSquare() {
        double[][] data = {
            {1.0, 2.0},
            {3.0, 4.0},
            {5.0, 6.0}
        };
        BigMatrix matrix = new BigMatrixImpl(data);
        double[] v = new double[] { 1.0, 2.0 };

        BigDecimal[] result = matrix.operate(v);
        assertNotNull(result);
        assertEquals(3, result.length);
        assertEquals(new BigDecimal(5.0), result[0]);
        assertEquals(new BigDecimal(11.0), result[1]);
        assertEquals(new BigDecimal(17.0), result[2]);
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConstructorsAndGetters() {
        BigMatrixImpl mEmpty = new BigMatrixImpl();
        assertNull(mEmpty.getDataRef());

        BigMatrixImpl mDim = new BigMatrixImpl(2, 3);
        assertEquals(2, mDim.getRowDimension());
        assertEquals(3, mDim.getColumnDimension());
        assertFalse(mDim.isSquare());

        String[][] strData = {{"1", "2"}, {"3", "4"}};
        BigMatrixImpl mStr = new BigMatrixImpl(strData);
        assertEquals(new BigDecimal("1"), mStr.getEntry(0, 0));
        assertEquals(new BigDecimal("4"), mStr.getEntry(1, 1));
        assertTrue(mStr.isSquare());

        BigDecimal[] colVec = new BigDecimal[] { new BigDecimal("10"), new BigDecimal("20") };
        BigMatrixImpl mVec = new BigMatrixImpl(colVec);
        assertEquals(2, mVec.getRowDimension());
        assertEquals(1, mVec.getColumnDimension());
        assertEquals(new BigDecimal("10"), mVec.getEntry(0, 0));
        assertEquals(new BigDecimal("20"), mVec.getEntry(1, 0));

        BigDecimal[][] raw = {{ new BigDecimal("5") }};
        BigMatrixImpl mRef = new BigMatrixImpl(raw, false);
        assertSame(raw, mRef.getDataRef());

        BigMatrixImpl mCopy = new BigMatrixImpl(raw, true);
        assertNotSame(raw, mCopy.getDataRef());
    }

    @Test(timeout = 4000)
    public void testMatrixCopyAndDataExtraction() {
        double[][] d = {{1.5, 2.5}, {3.5, 4.5}};
        BigMatrixImpl matrix = new BigMatrixImpl(d);

        BigMatrix copy = matrix.copy();
        assertEquals(matrix, copy);
        assertNotSame(matrix.getDataRef(), ((BigMatrixImpl) copy).getDataRef());

        BigDecimal[][] data = matrix.getData();
        assertEquals(new BigDecimal(1.5), data[0][0]);
        assertNotSame(data, matrix.getDataRef());

        double[][] doubleData = matrix.getDataAsDoubleArray();
        assertEquals(1.5, doubleData[0][0], 1e-9);
        assertEquals(4