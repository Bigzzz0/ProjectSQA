package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

import java.math.BigDecimal;

/**
 * Advanced White-Box test suite for BigMatrixImpl.
 * Targets line/branch coverage and the known Defects4J defect (testMath209).
 *
 * [Branch & Defect Analysis Matrix]
 * - Constructors: empty, non-rectangular, zero dimensions, null, copy vs reference
 * - add/subtract: dimension mismatch, same class vs interface, element-wise
 * - multiply: dimension mismatch, zero matrices, identity
 * - scalarAdd/scalarMultiply: zero, positive, negative
 * - getSubMatrix: valid, invalid indices (start>end, out of bounds, empty arrays)
 * - setSubMatrix: null, empty, non-rectangular, out of bounds, uninitialized matrix
 * - getRow/getColumn: valid, invalid indices
 * - getEntry: valid, out of bounds
 * - transpose, inverse, solve: square vs non-square, singular, non-singular
 * - getDeterminant: square singular/non-singular, non-square
 * - isSingular: LU decomposition caching, singular matrix
 * - luDecompose: non-square, singular, normal
 * - operate/preMultiply: vector length mismatch
 * - getNorm, getTrace: square vs non-square
 * - equals/hashCode: same, different dimensions, different values
 * - Defect target: ArrayIndexOutOfBoundsException in solve/getSubMatrix (testMath209)
 */
public class BigMatrixImplDeepseekTest {

    // Helper to create a 2x2 matrix
    private BigMatrixImpl create2x2() {
        BigDecimal[][] d = {{BigDecimal.ONE, BigDecimal.ZERO}, {BigDecimal.ZERO, BigDecimal.ONE}};
        return new BigMatrixImpl(d);
    }

    // Helper to create a 3x3 matrix
    private BigMatrixImpl create3x3() {
        BigDecimal[][] d = {
            {new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3")},
            {new BigDecimal("4"), new BigDecimal("5"), new BigDecimal("6")},
            {new BigDecimal("7"), new BigDecimal("8"), new BigDecimal("9")}
        };
        return new BigMatrixImpl(d);
    }

    // ==================== Partition A: Core Functional Logic & State Transitions ====================

    @Test(timeout = 4000)
    public void testAddSameClass() {
        BigMatrixImpl m1 = create2x2();
        BigMatrixImpl m2 = create2x2();
        BigMatrixImpl result = m1.add(m2);
        assertEquals(new BigDecimal("2"), result.getEntry(0, 0));
        assertEquals(new BigDecimal("2"), result.getEntry(1, 1));
    }

    @Test(timeout = 4000)
    public void testAddInterface() {
        BigMatrixImpl m1 = create2x2();
        BigMatrix m2 = new BigMatrixImpl(new BigDecimal[][]{{BigDecimal.ONE, BigDecimal.ONE}, {BigDecimal.ONE, BigDecimal.ONE}});
        BigMatrix result = m1.add(m2);
        assertEquals(new BigDecimal("2"), result.getEntry(0, 0));
        assertEquals(new BigDecimal("2"), result.getEntry(1, 1));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddDimensionMismatch() {
        BigMatrixImpl m1 = create2x2();
        BigMatrixImpl m2 = new BigMatrixImpl(2, 3);
        m1.add(m2);
    }

    @Test(timeout = 4000)
    public void testSubtractSameClass() {
        BigMatrixImpl m1 = create2x2();
        BigMatrixImpl m2 = create2x2();
        BigMatrixImpl result = m1.subtract(m2);
        assertEquals(BigDecimal.ZERO, result.getEntry(0, 0));
    }

    @Test(timeout = 4000)
    public void testMultiplySameClass() {
        BigMatrixImpl m1 = create2x2();
        BigMatrixImpl m2 = new BigMatrixImpl(new BigDecimal[][]{{new BigDecimal("2"), BigDecimal.ZERO}, {BigDecimal.ZERO, new BigDecimal("3")}});
        BigMatrixImpl result = m1.multiply(m2);
        assertEquals(new BigDecimal("2"), result.getEntry(0, 0));
        assertEquals(new BigDecimal("3"), result.getEntry(1, 1));
    }

    @Test(timeout = 4000)
    public void testMultiplyInterface() {
        BigMatrixImpl m1 = create2x2();
        BigMatrix m2 = new BigMatrixImpl(new BigDecimal[][]{{new BigDecimal("2"), BigDecimal.ZERO}, {BigDecimal.ZERO, new BigDecimal("3")}});
        BigMatrix result = m1.multiply(m2);
        assertEquals(new BigDecimal("2"), result.getEntry(0, 0));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testMultiplyDimensionMismatch() {
        BigMatrixImpl m1 = create2x2();
        BigMatrixImpl m2 = new BigMatrixImpl(3, 2);
        m1.multiply(m2);
    }

    @Test(timeout = 4000)
    public void testScalarAdd() {
        BigMatrixImpl m = create2x2();
        BigMatrix result = m.scalarAdd(BigDecimal.ONE);
        assertEquals(new BigDecimal("2"), result.getEntry(0, 0));
        assertEquals(new BigDecimal("2"), result.getEntry(1, 1));
    }

    @Test(timeout = 4000)
    public void testScalarMultiply() {
        BigMatrixImpl m = create2x2();
        BigMatrix result = m.scalarMultiply(new BigDecimal("2"));
        assertEquals(new BigDecimal("2"), result.getEntry(0, 0));
    }

    @Test(timeout = 4000)
    public void testTranspose() {
        BigMatrixImpl m = new BigMatrixImpl(new BigDecimal[][]{{BigDecimal.ONE, new BigDecimal("2")}, {new BigDecimal("3"), new BigDecimal("4")}});
        BigMatrix t = m.transpose();
        assertEquals(new BigDecimal("2"), t.getEntry(1, 0));
        assertEquals(new BigDecimal("3"), t.getEntry(0, 1));
    }

    @Test(timeout = 4000)
    public void testGetNorm() {
        BigMatrixImpl m = new BigMatrixImpl(new BigDecimal[][]{{new BigDecimal("1"), new BigDecimal("-2")}, {new BigDecimal("-3"), new BigDecimal("4")}});
        assertEquals(new BigDecimal("6"), m.getNorm()); // max column sum: col0=4, col1=6
    }

    @Test(timeout = 4000)
    public void testGetTrace() {
        BigMatrixImpl m = create2x2();
        assertEquals(new BigDecimal("2"), m.getTrace());
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetTraceNonSquare() {
        BigMatrixImpl m = new BigMatrixImpl(2, 3);
        m.getTrace();
    }

    @Test(timeout = 4000)
    public void testOperateBigDecimalArray() {
        BigMatrixImpl m = create2x2();
        BigDecimal[] v = {BigDecimal.ONE, BigDecimal.ONE};
        BigDecimal[] result = m.operate(v);
        assertEquals(BigDecimal.ONE, result[0]);
        assertEquals(BigDecimal.ONE, result[1]);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testOperateWrongLength() {
        BigMatrixImpl m = create2x2();
        BigDecimal[] v = {BigDecimal.ONE};
        m.operate(v);
    }

    @Test(timeout = 4000)
    public void testOperateDoubleArray() {
        BigMatrixImpl m = create2x2();
        double[] v = {1.0, 1.0};
        BigDecimal[] result = m.operate(v);
        assertEquals(BigDecimal.ONE, result[0]);
    }

    @Test(timeout = 4000)
    public void testPreMultiplyBigDecimalArray() {
        BigMatrixImpl m = create2x2();
        BigDecimal[] v = {BigDecimal.ONE, BigDecimal.ONE};
        BigDecimal[] result = m.preMultiply(v);
        assertEquals(BigDecimal.ONE, result[0]);
        assertEquals(BigDecimal.ONE, result[1]);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testPreMultiplyWrongLength() {
        BigMatrixImpl m = create2x2();
        BigDecimal[] v = {BigDecimal.ONE};
        m.preMultiply(v);
    }

    // ==================== Partition B: Boundary Value Analysis & Extremes ====================

    @Test(timeout = 4000)
    public void testConstructorZeroDimension() {
        try {
            new BigMatrixImpl(0, 1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorEmptyArray() {
        try {
            new BigMatrixImpl(new BigDecimal[0][0]);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorNonRectangular() {
        try {
            new BigMatrixImpl(new BigDecimal[][]{{BigDecimal.ONE}, {BigDecimal.ONE, BigDecimal.ZERO}});
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testConstructorCopyFalse() {
        BigDecimal[][] d = {{BigDecimal.ONE, BigDecimal.ZERO}, {BigDecimal.ZERO, BigDecimal.ONE}};
        BigMatrixImpl m = new BigMatrixImpl(d, false);
        assertSame(d, m.getDataRef());
    }

    @Test(timeout = 4000)
    public void testConstructorCopyTrue() {
        BigDecimal[][] d = {{BigDecimal.ONE, BigDecimal.ZERO}, {BigDecimal.ZERO, BigDecimal.ONE}};
        BigMatrixImpl m = new BigMatrixImpl(d, true);
        assertNotSame(d, m.getDataRef());
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleArray() {
        double[][] d = {{1.0, 0.0}, {0.0, 1.0}};
        BigMatrixImpl m = new BigMatrixImpl(d);
        assertEquals(BigDecimal.ONE, m.getEntry(0, 0));
    }

    @Test(timeout = 4000)
    public void testConstructorStringArray() {
        String[][] d = {{"1", "0"}, {"0", "1"}};
        BigMatrixImpl m = new BigMatrixImpl(d);
        assertEquals(BigDecimal.ONE, m.getEntry(0, 0));
    }

    @Test(timeout = 4000)
    public void testConstructorBigDecimalVector() {
        BigDecimal[] v = {BigDecimal.ONE, BigDecimal.ONE};
        BigMatrixImpl m = new BigMatrixImpl(v);
        assertEquals(2, m.getRowDimension());
        assertEquals(1, m.getColumnDimension());
    }

    @Test(timeout = 4000)
    public void testGetRowAsDoubleArray() {
        BigMatrixImpl m = create2x2();
        double[] row = m.getRowAsDoubleArray(0);
        assertEquals(1.0, row[0], 1e-15);
        assertEquals(0.0, row[1], 1e-15);
    }

    @Test(timeout = 4000, expected = MatrixIndexException.class)
    public void testGetRowInvalid() {
        BigMatrixImpl m = create2x2();
        m.getRow(2);
    }

    @Test(timeout = 4000)
    public void testGetColumnAsDoubleArray() {
        BigMatrixImpl m = create2x2();
        double[] col = m.getColumnAsDoubleArray(0);
        assertEquals(1.0, col[0], 1e-15);
        assertEquals(0.0, col[1], 1e-15);
    }

    @Test(timeout = 4000, expected = MatrixIndexException.class)
    public void testGetColumnInvalid() {
        BigMatrixImpl m = create2x2();
        m.getColumn(2);
    }

    @Test(timeout = 4000)
    public void testGetEntryAsDouble() {
        BigMatrixImpl m = create2x2();
        assertEquals(1.0, m.getEntryAsDouble(0, 0), 1e-15);
    }

    @Test(timeout = 4000, expected = MatrixIndexException.class)
    public void testGetEntryOutOfBounds() {
        BigMatrixImpl m = create2x2();
        m.getEntry(2, 0);
    }

    // ==================== Partition C: Defect-Targeted Branch Zone (testMath209) ====================

    /**
     * This test targets the known Defects4J defect (testMath209).
     * The defect causes an ArrayIndexOutOfBoundsException when performing
     * operations on a matrix with certain dimensions, likely in solve or getSubMatrix.
     * We reproduce a scenario that triggers the bug: a non-square matrix used in solve,
     * or a submatrix extraction with invalid indices.
     * Based on the error "ArrayIndexOutOfBoundsException: 2", we suspect a 2x2 matrix
     * with an operation that accesses index 2. We'll test solve with a 2x2 matrix
     * that is singular or has special values, and also test getSubMatrix with endRow > data.length.
     */
    @Test(timeout = 4000)
    public void testMath209Defect() {
        // Scenario: Create a 2x2 matrix and attempt to solve with a vector of length 2.
        // The defect may occur when the matrix is singular or when LU decomposition fails.
        // We'll use a singular matrix (all zeros) to trigger the defect path.
        BigMatrixImpl singular = new BigMatrixImpl(new BigDecimal[][]{
            {BigDecimal.ZERO, BigDecimal.ZERO},
            {BigDecimal.ZERO, BigDecimal.ZERO}
        });
        BigDecimal[] b = {BigDecimal.ONE, BigDecimal.ONE};
        try {
            singular.solve(b);
            fail("Expected InvalidMatrixException for singular matrix");
        } catch (InvalidMatrixException e) {
            // Expected: matrix is singular
        } catch (ArrayIndexOutOfBoundsException e) {
            // This is the defect: should not happen; if it does, test fails
            fail("Defect triggered: ArrayIndexOutOfBoundsException should not occur");
        }
    }

    @Test(timeout = 4000)
    public void testMath209DefectSubMatrix() {
        // Another possible trigger: getSubMatrix with endRow > data.length
        BigMatrixImpl m = create2x2();
        try {
            m.getSubMatrix(0, 2, 0, 1); // endRow = 2, but data.length = 2, so endRow > data.length-1
            fail("Expected MatrixIndexException");
        } catch (MatrixIndexException e) {
            // Expected
        } catch (ArrayIndexOutOfBoundsException e) {
            // Defect: should be caught by the method's own check
            fail("Defect triggered: ArrayIndexOutOfBoundsException should not occur");
        }
    }

    // ==================== Partition D: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorNegativeDimension() {
        new BigMatrixImpl(-1, 2);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testConstructorNullArray() {
        new BigMatrixImpl((BigDecimal[][]) null);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorEmptyRow() {
        new BigMatrixImpl(new BigDecimal[][]{{}});
    }

    @Test(timeout = 4000)
    public void testSetSubMatrixNull() {
        BigMatrixImpl m = create2x2();
        try {
            m.setSubMatrix(null, 0, 0);
            fail("Expected NullPointerException");
        } catch (NullPointerException e) {
            // expected
        }
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSetSubMatrixEmpty() {
        BigMatrixImpl m = create2x2();
        m.setSubMatrix(new BigDecimal[0][0], 0, 0);
    }

    @Test(timeout = 4000, expected = MatrixIndexException.class)
    public void testSetSubMatrixOutOfBounds() {
        BigMatrixImpl m = create2x2();
        BigDecimal[][] sub = {{BigDecimal.ONE}};
        m.setSubMatrix(sub, 2, 0);
    }

    @Test(timeout = 4000)
    public void testSetSubMatrixUninitialized() {
        BigMatrixImpl m = new BigMatrixImpl(); // no data
        BigDecimal[][] sub = {{BigDecimal.ONE}};
        try {
            m.setSubMatrix(sub, 1, 0);
            fail("Expected MatrixIndexException");
        } catch (MatrixIndexException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testSetSubMatrixValid() {
        BigMatrixImpl m = create2x2();
        BigDecimal[][] sub = {{new BigDecimal("5")}};
        m.setSubMatrix(sub, 0, 0);
        assertEquals(new BigDecimal("5"), m.getEntry(0, 0));
        assertNull(m.lu); // LU should be invalidated
    }

    @Test(timeout = 4000, expected = MatrixIndexException.class)
    public void testGetSubMatrixInvalidStartEnd() {
        BigMatrixImpl m = create2x2();
        m.getSubMatrix(1, 0, 0, 1);
    }

    @Test(timeout = 4000, expected = MatrixIndexException.class)
    public void testGetSubMatrixEmptySelectedArrays() {
        BigMatrixImpl m = create2x2();
        m.getSubMatrix(new int[]{}, new int[]{1});
    }

    @Test(timeout = 4000)
    public void testGetSubMatrixValidSelected() {
        BigMatrixImpl m = create3x3();
        BigMatrix sub = m.getSubMatrix(new int[]{0, 2}, new int[]{1, 2});
        assertEquals(new BigDecimal("2"), sub.getEntry(0, 0));
        assertEquals(new BigDecimal("3"), sub.getEntry(0, 1));
        assertEquals(new BigDecimal("8"), sub.getEntry(1, 0));
        assertEquals(new BigDecimal("9"), sub.getEntry(1, 1));
    }

    @Test(timeout = 4000, expected = MatrixIndexException.class)
    public void testGetSubMatrixSelectedOutOfBounds() {
        BigMatrixImpl m = create2x2();
        m.getSubMatrix(new int[]{0, 2}, new int[]{0});
    }

    @Test(timeout = 4000)
    public void testIsSingularTrue() {
        BigMatrixImpl m = new BigMatrixImpl(new BigDecimal[][]{
            {BigDecimal.ZERO, BigDecimal.ZERO},
            {BigDecimal.ZERO, BigDecimal.ZERO}
        });
        assertTrue(m.isSingular());
    }

    @Test(timeout = 4000)
    public void testIsSingularFalse() {
        BigMatrixImpl m = create2x2();
        assertFalse(m.isSingular());
    }

    @Test(timeout = 4000)
    public void testIsSingularCachesLU() {
        BigMatrixImpl m = create2x2();
        m.isSingular(); // triggers LU
        assertNotNull(m.lu);
    }

    @Test(timeout = 4000)
    public void testGetDeterminantNonSingular() {
        BigMatrixImpl m = new BigMatrixImpl(new BigDecimal[][]{
            {new BigDecimal("1"), new BigDecimal("2")},
            {new BigDecimal("3"), new BigDecimal("4")}
        });
        assertEquals(new BigDecimal("-2"), m.getDeterminant());
    }

    @Test(timeout = 4000)
    public void testGetDeterminantSingular() {
        BigMatrixImpl m = new BigMatrixImpl(new BigDecimal[][]{
            {BigDecimal.ONE, BigDecimal.ONE},
            {BigDecimal.ONE, BigDecimal.ONE}
        });
        assertEquals(BigDecimal.ZERO, m.getDeterminant());
    }

    @Test(timeout = 4000, expected = InvalidMatrixException.class)
    public void testGetDeterminantNonSquare() {
        BigMatrixImpl m = new BigMatrixImpl(2, 3);
        m.getDeterminant();
    }

    @Test(timeout = 4000)
    public void testInverse() {
        BigMatrixImpl m = new BigMatrixImpl(new BigDecimal[][]{
            {new BigDecimal("1"), new BigDecimal("2")},
            {new BigDecimal("3"), new BigDecimal("4")}
        });
        BigMatrix inv = m.inverse();
        BigMatrix product = m.multiply(inv);
        assertEquals(BigDecimal.ONE, product.getEntry(0, 0));
        assertEquals(BigDecimal.ONE, product.getEntry(1, 1));
    }

    @Test(timeout = 4000, expected = InvalidMatrixException.class)
    public void testInverseSingular() {
        BigMatrixImpl m = new BigMatrixImpl(new BigDecimal[][]{
            {BigDecimal.ONE, BigDecimal.ONE},
            {BigDecimal.ONE, BigDecimal.ONE}
        });
        m.inverse();
    }

    @Test(timeout = 4000)
    public void testSolveBigMatrix() {
        BigMatrixImpl m = create2x2();
        BigMatrix b = new BigMatrixImpl(new BigDecimal[][]{{BigDecimal.ONE}, {BigDecimal.ZERO}});
        BigMatrix solution = m.solve(b);
        assertEquals(BigDecimal.ONE, solution.getEntry(0, 0));
        assertEquals(BigDecimal.ZERO, solution.getEntry(1, 0));
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testSolveWrongRowDimension() {
        BigMatrixImpl m = create2x2();
        BigMatrix b = new BigMatrixImpl(3, 1);
        m.solve(b);
    }

    @Test(timeout = 4000, expected = InvalidMatrixException.class)
    public void testSolveNonSquare() {
        BigMatrixImpl m = new BigMatrixImpl(2, 3);
        BigMatrix b = new BigMatrixImpl(2, 1);
        m.solve(b);
    }

    @Test(timeout = 4000)
    public void testSolveDoubleArray() {
        BigMatrixImpl m = create2x2();
        double[] b = {1.0, 0.0};
        BigDecimal[] solution = m.solve(b);
        assertEquals(BigDecimal.ONE, solution[0]);
        assertEquals(BigDecimal.ZERO, solution[1]);
    }

    @Test(timeout = 4000)
    public void testLuDecompose() {
        BigMatrixImpl m = create2x2();
        m.luDecompose();
        assertNotNull(m.lu);
        assertEquals(2, m.permutation.length);
    }

    @Test(timeout = 4000, expected = InvalidMatrixException.class)
    public void testLuDecomposeNonSquare() {
        BigMatrixImpl m = new BigMatrixImpl(2, 3);
        m.luDecompose();
    }

    @Test(timeout = 4000, expected = InvalidMatrixException.class)
    public void testLuDecomposeSingular() {
        BigMatrixImpl m = new BigMatrixImpl(new BigDecimal[][]{
            {BigDecimal.ZERO, BigDecimal.ZERO},
            {BigDecimal.ZERO, BigDecimal.ZERO}
        });
        m.luDecompose();
    }

    // ==================== Partition E: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testCopy() {
        BigMatrixImpl m = create2x2();
        BigMatrix copy = m.copy();
        assertTrue(m.equals(copy));
        assertNotSame(m.getDataRef(), ((BigMatrixImpl) copy).getDataRef());
    }

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        BigMatrixImpl m = create2x2();
        assertTrue(m.equals(m));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        BigMatrixImpl m = create2x2();
        assertFalse(m.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentDimensions() {
        BigMatrixImpl m1 = create2x2();
        BigMatrixImpl m2 = new BigMatrixImpl(3, 3);
        assertFalse(m1.equals(m2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentValues() {
        BigMatrixImpl m1 = create2x2();
        BigMatrixImpl m2 = new BigMatrixImpl(new BigDecimal[][]{
            {BigDecimal.ONE, BigDecimal.ONE},
            {BigDecimal.ONE, BigDecimal.ONE}
        });
        assertFalse(m1.equals(m2));
    }

    @Test(timeout = 4000)
    public void testEqualsSameValues() {
        BigMatrixImpl m1 = create2x2();
        BigMatrixImpl m2 = create2x2();
        assertTrue(m1.equals(m2));
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        BigMatrixImpl m = create2x2();
        int h1 = m.hashCode();
        int h2 = m.hashCode();
        assertEquals(h1, h2);
    }

    @Test(timeout = 4000)
    public void testHashCodeDifferent() {
        BigMatrixImpl m1 = create2x2();
        BigMatrixImpl m2 = new BigMatrixImpl(new BigDecimal[][]{
            {BigDecimal.ONE, BigDecimal.ONE},
            {BigDecimal.ONE, BigDecimal.ONE}
        });
        assertFalse(m1.hashCode() == m2.hashCode());
    }

    @Test(timeout = 4000)
    public void testToString() {
        BigMatrixImpl m = create2x2();
        String s = m.toString();
        assertTrue(s.contains("BigMatrixImpl"));
        assertTrue(s.contains("1"));
    }

    @Test(timeout = 4000)
    public void testGetRoundingModeAndScale() {
        BigMatrixImpl m = new BigMatrixImpl();
        assertEquals(BigDecimal.ROUND_HALF_UP, m.getRoundingMode());
        assertEquals(64, m.getScale());
        m.setRoundingMode(BigDecimal.ROUND_DOWN);
        m.setScale(32);
        assertEquals(BigDecimal.ROUND_DOWN, m.getRoundingMode());
        assertEquals(32, m.getScale());
    }

    @Test(timeout = 4000)
    public void testGetLUMatrix() {
        BigMatrixImpl m = create2x2();
        BigMatrix luMatrix = m.getLUMatrix();
        assertNotNull(luMatrix);
        // LU should be cached
        assertNotNull(m.lu);
    }

    @Test(timeout = 4000)
    public void testGetPermutation() {
        BigMatrixImpl m = create2x2();
        m.luDecompose();
        int[] perm = m.getPermutation();
        assertNotNull(perm);
        assertEquals(2, perm.length);
    }

    @Test(timeout = 4000)
    public void testGetDataAsDoubleArray() {
        BigMatrixImpl m = create2x2();
        double[][] d = m.getDataAsDoubleArray();
        assertEquals(1.0, d[0][0], 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetDataRef() {
        BigMatrixImpl m = create2x2();
        BigDecimal[][] ref = m.getDataRef();
        assertSame(m.data, ref);
    }

    @Test(timeout = 4000)
    public void testPreMultiplyBigMatrix() {
        BigMatrixImpl m = create2x2();
        BigMatrix premult = new BigMatrixImpl(new BigDecimal[][]{
            {new BigDecimal("2"), BigDecimal.ZERO},
            {BigDecimal.ZERO, new BigDecimal("3")}
        });
        BigMatrix result = m.preMultiply(premult);
        assertEquals(new BigDecimal("2"), result.getEntry(0, 0));
        assertEquals(new BigDecimal("3"), result.getEntry(1, 1));
    }

    @Test(timeout = 4000)
    public void testIsSquare() {
        assertTrue(create2x2().isSquare());
        assertFalse(new BigMatrixImpl(2, 3).isSquare());
    }
}