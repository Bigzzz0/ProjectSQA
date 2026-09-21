package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

/* [Branch & Defect Analysis Matrix]
 * 
 * **Defect targeting**:
 * - `getSubMatrix(int, int, int, int)` (line ~368): The validation allows `endRow > data.length` instead of `endRow >= data.length`.
 *   When `data.length = 2` and `endRow = 2`, the condition `endRow > data.length` is `false`, so the submatrix extraction proceeds,
 *   causing `ArrayIndexOutOfBoundsException`. Correct behavior is to throw `MatrixIndexException`.
 * 
 * **Key branches to cover**:
 * - Constructors: `RealMatrixImpl()`, `RealMatrixImpl(int,int)`, `RealMatrixImpl(double[][])`, `RealMatrixImpl(double[][],boolean)`, `RealMatrixImpl(double[])`
 * - `add(RealMatrix)` (with and without cast), `add(RealMatrixImpl)`
 * - `subtract` (similar)
 * - `multiply` (similar)
 * - `scalarAdd`, `scalarMultiply`
 * - `transpose`, `getNorm`, `getTrace`, `operate`, `preMultiply`
 * - `getData`, `getDataRef`, `copy`, `toString`, `equals`, `hashCode`
 * - `getSubMatrix(int,int,int,int)` – including boundary cases and the defect case
 * - `getSubMatrix(int[],int[])` – including empty array and invalid index
 * - `setSubMatrix` – including null data, invalid row/col, non-rectangular
 * - `getRow(int)`, `getColumn(int)`, `getRowMatrix`, `getColumnMatrix`, `getEntry`
 * - `isSquare`, `isSingular`, `getDeterminant`, `inverse`, `solve`, `luDecompose`
 * - Methods that throw `IllegalArgumentException`, `InvalidMatrixException`, `MatrixIndexException`
 */
public class RealMatrixImplDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testConstructorEmpty() {
        RealMatrixImpl m = new RealMatrixImpl();
        assertEquals(0, m.getRowDimension());
        // data is null, so columnDimension throws NullPointerException – not tested here
    }

    @Test(timeout = 4000)
    public void testConstructorDimensions() {
        RealMatrixImpl m = new RealMatrixImpl(3, 2);
        assertEquals(3, m.getRowDimension());
        assertEquals(2, m.getColumnDimension());
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 2; j++)
                assertEquals(0.0, m.getEntry(i, j), 0.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorInvalidDimensionsZeroRows() {
        new RealMatrixImpl(0, 1);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorInvalidDimensionsNegativeCols() {
        new RealMatrixImpl(2, -1);
    }

    @Test(timeout = 4000)
    public void testConstructorFromArrayCopy() {
        double[][] d = {{1, 2}, {3, 4}};
        RealMatrixImpl m = new RealMatrixImpl(d);
        assertNotSame(d, m.getDataRef());
        assertEquals(1.0, m.getEntry(0, 0), 0.0);
        assertEquals(4.0, m.getEntry(1, 1), 0.0);
    }

    @Test(timeout = 4000)
    public void testConstructorFromArrayRef() {
        double[][] d = {{1, 2}, {3, 4}};
        RealMatrixImpl m = new RealMatrixImpl(d, false);
        assertSame(d, m.getDataRef());
        d[0][0] = 100;
        assertEquals(100.0, m.getEntry(0, 0), 0.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorFromArrayNonRectangular() {
        new RealMatrixImpl(new double[][]{{1}, {2, 3}});
    }

    @Test(expected = NullPointerException.class, timeout = 4000)
    public void testConstructorFromArrayNull() {
        new RealMatrixImpl((double[][]) null);
    }

    @Test(timeout = 4000)
    public void testConstructorFromVector() {
        double[] v = {1.5, 2.5, 3.5};
        RealMatrixImpl m = new RealMatrixImpl(v);
        assertEquals(3, m.getRowDimension());
        assertEquals(1, m.getColumnDimension());
        assertEquals(1.5, m.getEntry(0, 0), 0.0);
    }

    @Test(timeout = 4000)
    public void testAdd() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        RealMatrixImpl m2 = new RealMatrixImpl(new double[][]{{5, 6}, {7, 8}});
        RealMatrix result = m1.add(m2);
        double[][] expected = {{6, 8}, {10, 12}};
        assertMatrixEquals(expected, (RealMatrixImpl) result);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddMismatchedDimensions() {
        RealMatrixImpl m1 = new RealMatrixImpl(2, 3);
        RealMatrixImpl m2 = new RealMatrixImpl(3, 2);
        m1.add(m2);
    }

    @Test(timeout = 4000)
    public void testAddWithNonImpl() {
        final RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        RealMatrix m2 = new RealMatrix() {
            public RealMatrix createMatrix(int r, int c) { return null; }
            public RealMatrix copy() { return null; }
            public double[][] getData() { return null; }
            public int getRowDimension() { return 2; }
            public int getColumnDimension() { return 2; }
            public double getEntry(int r, int c) { return 1.0; }
            // other methods not needed for this test
        };
        RealMatrix result = m1.add(m2);
        assertEquals(2.0, result.getEntry(0, 0), 0.0);
        assertEquals(2.0, result.getEntry(1, 1), 0.0);
    }

    @Test(timeout = 4000)
    public void testSubtract() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{5, 6}, {7, 8}});
        RealMatrixImpl m2 = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        RealMatrix result = m1.subtract(m2);
        double[][] expected = {{4, 4}, {4, 4}};
        assertMatrixEquals(expected, (RealMatrixImpl) result);
    }

    @Test(timeout = 4000)
    public void testScalarAdd() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        RealMatrix result = m.scalarAdd(10);
        double[][] expected = {{11, 12}, {13, 14}};
        assertMatrixEquals(expected, (RealMatrixImpl) result);
    }

    @Test(timeout = 4000)
    public void testScalarMultiply() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        RealMatrix result = m.scalarMultiply(2.0);
        double[][] expected = {{2, 4}, {6, 8}};
        assertMatrixEquals(expected, (RealMatrixImpl) result);
    }

    @Test(timeout = 4000)
    public void testMultiply() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        RealMatrixImpl m2 = new RealMatrixImpl(new double[][]{{0, 1}, {1, 0}});
        RealMatrix result = m1.multiply(m2);
        double[][] expected = {{2, 1}, {4, 3}};
        assertMatrixEquals(expected, (RealMatrixImpl) result);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testMultiplyIncompatible() {
        RealMatrixImpl m1 = new RealMatrixImpl(2, 3);
        RealMatrixImpl m2 = new RealMatrixImpl(2, 2);
        m1.multiply(m2);
    }

    @Test(timeout = 4000)
    public void testPreMultiplyMatrix() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        RealMatrixImpl m2 = new RealMatrixImpl(new double[][]{{0, 1}, {1, 0}});
        RealMatrix result = m1.preMultiply(m2);
        double[][] expected = {{3, 4}, {1, 2}}; // m2 * m1
        assertMatrixEquals(expected, (RealMatrixImpl) result);
    }

    @Test(timeout = 4000)
    public void testTranspose() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}, {5, 6}});
        RealMatrix t = m.transpose();
        assertEquals(2, t.getRowDimension());
        assertEquals(3, t.getColumnDimension());
        assertEquals(1, t.getEntry(0, 0), 0.0);
        assertEquals(6, t.getEntry(1, 2), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetNorm() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, -2}, {-3, 4}});
        // column sums: |1|+|-3|=4, |-2|+|4|=6 -> max = 6
        assertEquals(6.0, m.getNorm(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetTraceSquare() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        assertEquals(5.0, m.getTrace(), 0.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testGetTraceNonSquare() {
        RealMatrixImpl m = new RealMatrixImpl(2, 3);
        m.getTrace();
    }

    @Test(timeout = 4000)
    public void testOperate() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        double[] v = {2, 1};
        double[] result = m.operate(v);
        assertArrayEquals(new double[]{4, 10}, result, 1e-12);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testOperateWrongLength() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.operate(new double[3]);
    }

    @Test(timeout = 4000)
    public void testPreMultiplyVector() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        double[] v = {1, 2};
        double[] result = m.preMultiply(v);
        assertArrayEquals(new double[]{7, 10}, result, 1e-12);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testPreMultiplyVectorWrongLength() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.preMultiply(new double[3]);
    }

    @Test(timeout = 4000)
    public void testGetDataAndGetDataRef() {
        double[][] d = {{1, 2}, {3, 4}};
        RealMatrixImpl m = new RealMatrixImpl(d, false);
        assertSame(d, m.getDataRef());
        double[][] copy = m.getData();
        assertNotSame(d, copy);
        assertMatrixEquals(d, copy);
    }

    @Test(timeout = 4000)
    public void testCopy() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        RealMatrix copy = m.copy();
        assertTrue(copy instanceof RealMatrixImpl);
        assertEquals(2, copy.getRowDimension());
        assertEquals(2, copy.getColumnDimension());
        assertEquals(1, copy.getEntry(0, 0), 0.0);
    }

    @Test(timeout = 4000)
    public void testToString() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1.5, -2}, {0.0, 3}});
        String s = m.toString();
        assertTrue(s.contains("RealMatrixImpl"));
        assertTrue(s.contains("1.5"));
        assertTrue(s.contains("-2"));
    }

    @Test(timeout = 4000)
    public void testEquals() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        RealMatrixImpl m2 = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        RealMatrixImpl m3 = new RealMatrixImpl(new double[][]{{1, 2}, {3, 5}});
        assertTrue(m1.equals(m2));
        assertTrue(m1.equals(m1));
        assertFalse(m1.equals(null));
        assertFalse(m1.equals("string"));
        assertFalse(m1.equals(m3));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        RealMatrixImpl m2 = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    // ========== Partition B: Boundary Value & Edge Cases ==========

    @Test(timeout = 4000)
    public void testGetSubMatrixValid() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1,2,3},{4,5,6},{7,8,9}});
        RealMatrix sub = m.getSubMatrix(0, 1, 1, 2);
        assertEquals(2, sub.getRowDimension());
        assertEquals(2, sub.getColumnDimension());
        assertEquals(2, sub.getEntry(0, 0), 0.0);
        assertEquals(6, sub.getEntry(1, 1), 0.0);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetSubMatrixInvalidStartRow() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1,2},{3,4}});
        m.getSubMatrix(-1, 1, 0, 1);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetSubMatrixInvalidEndRow() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1,2},{3,4}});
        m.getSubMatrix(0, 2, 0, 1);  // data.length = 2, endRow=2 => defect trigger
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetSubMatrixInvalidStartCol() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1,2},{3,4}});
        m.getSubMatrix(0, 1, -1, 1);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetSubMatrixInvalidEndCol() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1,2},{3,4}});
        m.getSubMatrix(0, 1, 0, 2);
    }

    @Test(timeout = 4000)
    public void testGetSubMatrixWithIndexArraysValid() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1,2,3},{4,5,6},{7,8,9}});
        RealMatrix sub = m.getSubMatrix(new int[]{0, 2}, new int[]{1, 2});
        assertEquals(2, sub.getRowDimension());
        assertEquals(2, sub.getColumnDimension());
        assertEquals(2, sub.getEntry(0, 0), 0.0);
        assertEquals(9, sub.getEntry(1, 1), 0.0);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetSubMatrixWithIndexArraysEmpty() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1}});
        m.getSubMatrix(new int[]{}, new int[]{0});
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetSubMatrixWithIndexArrayOutOfBounds() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1,2},{3,4}});
        m.getSubMatrix(new int[]{0, 2}, new int[]{0, 1});  // row 2 not exist
    }

    @Test(timeout = 4000)
    public void testSetSubMatrix() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1,2,3},{4,5,6},{7,8,9}});
        m.setSubMatrix(new double[][]{{-1, -2}, {-3, -4}}, 1, 1);
        assertEquals(1, m.getEntry(0,0), 0.0);
        assertEquals(-1, m.getEntry(1,1), 0.0);
        assertEquals(-4, m.getEntry(2,2), 0.0);
        assertEquals(3, m.getEntry(0,2), 0.0);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testSetSubMatrixNegativeRow() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.setSubMatrix(new double[][]{{1}}, -1, 0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSetSubMatrixEmptySubMatrix() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.setSubMatrix(new double[0][0], 0, 0);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testSetSubMatrixOutOfBounds() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.setSubMatrix(new double[][]{{1,2}}, 1, 1); // needs 1 row + 1 row = 2 rows, fits; col: 1 + 2 cols = 3 > 2
    }

    @Test(timeout = 4000)
    public void testSetSubMatrixOnNullData() {
        RealMatrixImpl m = new RealMatrixImpl();
        m.setSubMatrix(new double[][]{{1,2},{3,4}}, 0, 0);
        assertEquals(2, m.getRowDimension());
        assertEquals(2, m.getColumnDimension());
        assertEquals(1, m.getEntry(0,0), 0.0);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testSetSubMatrixOnNullDataWithNonZeroOrigin() {
        RealMatrixImpl m = new RealMatrixImpl();
        m.setSubMatrix(new double[][]{{1}}, 1, 0);
    }

    // ========== Partition C: Defect-Targeted Branch Zone ==========

    // This test directly targets the known defect: getSubMatrix with endRow == rowDimension causes AIOOB.
    // The correct behavior is to throw MatrixIndexException.
    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testMath209Defect() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        m.getSubMatrix(0, 2, 0, 1);  // endRow=2, rowDimension=2 -> bug if not caught
    }

    // Additional edge: endRow == data.length - 1 should work
    @Test(timeout = 4000)
    public void testGetSubMatrixLastRow() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        RealMatrix sub = m.getSubMatrix(1, 1, 0, 1);
        assertEquals(1, sub.getRowDimension());
        assertEquals(2, sub.getColumnDimension());
        assertEquals(3, sub.getEntry(0, 0), 0.0);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorFromNullRef() {
        new RealMatrixImpl(null, false);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testConstructorFromEmptyArrayRef() {
        new RealMatrixImpl(new double[0][0], false);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testIsSquareFalse() {
        RealMatrixImpl m = new RealMatrixImpl(2, 3);
        assertFalse(m.isSquare());
    }

    @Test(timeout = 4000)
    public void testIsSquareTrue() {
        RealMatrixImpl m = new RealMatrixImpl(3, 3);
        assertTrue(m.isSquare());
    }

    @Test(timeout = 4000)
    public void testIsSingularNonSingular() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 0}, {0, 1}});
        assertFalse(m.isSingular());
    }

    @Test(timeout = 4000)
    public void testIsSingularSingular() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {2, 4}});
        assertTrue(m.isSingular());
    }

    @Test(timeout = 4000)
    public void testGetDeterminantNonSingular() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        double det = m.getDeterminant();
        assertEquals(-2.0, det, 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetDeterminantSingular() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {2, 4}});
        double det = m.getDeterminant();
        assertEquals(0.0, det, 1e-12);
    }

    @Test(expected = InvalidMatrixException.class, timeout = 4000)
    public void testGetDeterminantNonSquare() {
        RealMatrixImpl m = new RealMatrixImpl(2, 3);
        m.getDeterminant();
    }

    @Test(timeout = 4000)
    public void testInverse() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        RealMatrix inv = m.inverse();
        RealMatrix product = m.multiply(inv);
        assertMatrixEquals(new double[][]{{1, 0}, {0, 1}}, (RealMatrixImpl) product);
    }

    @Test(expected = InvalidMatrixException.class, timeout = 4000)
    public void testInverseSingular() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {2, 4}});
        m.inverse();
    }

    @Test(expected = InvalidMatrixException.class, timeout = 4000)
    public void testInverseNonSquare() {
        RealMatrixImpl m = new RealMatrixImpl(2, 3);
        m.inverse();
    }

    @Test(timeout = 4000)
    public void testSolveVector() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        double[] b = {5, 11};
        double[] x = m.solve(b);
        assertArrayEquals(new double[]{3, 1}, x, 1e-12);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveVectorWrongLength() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.solve(new double[3]);
    }

    @Test(expected = InvalidMatrixException.class, timeout = 4000)
    public void testSolveSingular() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {2, 4}});
        m.solve(new double[]{1, 2});
    }

    @Test(expected = InvalidMatrixException.class, timeout = 4000)
    public void testSolveMatrixNonSquareCoefficient() {
        RealMatrixImpl m = new RealMatrixImpl(2, 3);
        m.solve(new RealMatrixImpl(2, 2));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSolveMatrixIncompatibleRows() {
        RealMatrixImpl m = new RealMatrixImpl(3, 3);
        m.solve(new RealMatrixImpl(2, 3));
    }

    @Test(timeout = 4000)
    public void testLuDecomposeSquare() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{2, 3}, {5, 7}});
        m.luDecompose();
        assertNotNull(m.getLUMatrix());
        assertArrayEquals(new int[]{0, 1}, m.getPermutation());
    }

    @Test(expected = InvalidMatrixException.class, timeout = 4000)
    public void testLuDecomposeNonSquare() {
        RealMatrixImpl m = new RealMatrixImpl(2, 3);
        m.luDecompose();
    }

    @Test(expected = InvalidMatrixException.class, timeout = 4000)
    public void testLuDecomposeSingular() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {2, 4}});
        m.luDecompose();
    }

    @Test(timeout = 4000)
    public void testGetRowMatrixValid() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        RealMatrix row = m.getRowMatrix(1);
        assertEquals(1, row.getRowDimension());
        assertEquals(2, row.getColumnDimension());
        assertEquals(3, row.getEntry(0, 0), 0.0);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetRowMatrixInvalid() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getRowMatrix(2);
    }

    @Test(timeout = 4000)
    public void testGetColumnMatrixValid() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        RealMatrix col = m.getColumnMatrix(0);
        assertEquals(2, col.getRowDimension());
        assertEquals(1, col.getColumnDimension());
        assertEquals(1, col.getEntry(0, 0), 0.0);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetColumnMatrixInvalid() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getColumnMatrix(2);
    }

    @Test(timeout = 4000)
    public void testGetRowValid() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        double[] row = m.getRow(1);
        assertArrayEquals(new double[]{3, 4}, row, 0.0);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetRowInvalid() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getRow(-1);
    }

    @Test(timeout = 4000)
    public void testGetColumnValid() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        double[] col = m.getColumn(1);
        assertArrayEquals(new double[]{2, 4}, col, 0.0);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetColumnInvalid() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getColumn(2);
    }

    @Test(timeout = 4000)
    public void testGetEntryValid() {
        RealMatrixImpl m = new RealMatrixImpl(new double[][]{{1, 2}, {3, 4}});
        assertEquals(4.0, m.getEntry(1, 1), 0.0);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetEntryInvalidRow() {
        RealMatrixImpl m = new RealMatrixImpl(2, 2);
        m.getEntry(2, 0);
    }

    // ========== Partition E: Object Lifecycle & Contract ==========

    @Test(timeout = 4000)
    public void testSerializationContract() {
        // Not directly testing serialization, but checking equals/hashCode consistency
        RealMatrixImpl m1 = new RealMatrixImpl(new double[][]{{0.5, 1.5}, {2.5, 3.5}});
        RealMatrixImpl m2 = new RealMatrixImpl(new double[][]{{0.5, 1.5}, {2.5, 3.5}});
        assertTrue(m1.equals(m2));
        assertEquals(m1.hashCode(), m2.hashCode());
    }

    // ========== Helper Methods ==========

    private void assertMatrixEquals(double[][] expected, RealMatrixImpl actual) {
        assertEquals(expected.length, actual.getRowDimension());
        assertEquals(expected[0].length, actual.getColumnDimension());
        for (int i = 0; i < expected.length; i++) {
            for (int j = 0; j < expected[0].length; j++) {
                assertEquals(expected[i][j], actual.getEntry(i, j), 1e-12);
            }
        }
    }
}