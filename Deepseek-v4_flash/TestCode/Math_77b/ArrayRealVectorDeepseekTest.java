package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Advanced white-box test suite for ArrayRealVector.
 * Targets the known defect in getLInfNorm (buggy accumulation instead of max)
 * and achieves high line/branch coverage.
 *
 * [Branch & Defect Analysis Matrix]
 * - getLInfNorm: defect in line "max += Math.max(max, Math.abs(a))" should be "max = Math.max(max, Math.abs(a))"
 *   -> Test: vector with L1 != LInf, e.g., [1,2,3,4,5,6] -> expected LInf=6, buggy returns 21.
 * - Constructors: all variants, including null/empty checks, copy vs reference, subarray bounds.
 * - Arithmetic: add, subtract, ebeMultiply, ebeDivide, dotProduct, getDistance, getL1Distance, getLInfDistance.
 * - Map functions: mapAddToSelf, mapMultiplyToSelf, etc. (test a representative subset).
 * - Norms: getNorm, getL1Norm, getLInfNorm.
 * - Unit vector / unitize: zero norm exception.
 * - Projection: basic projection.
 * - Outer product: basic.
 * - get/set entry, subvector, append.
 * - equals/hashCode: NaN handling, dimension mismatch, exact equality.
 * - isNaN / isInfinite.
 * - Exception paths: checkVectorDimensions, index out of bounds, zero norm.
 */
public class ArrayRealVectorDeepseekTest {

    // ==================== Partition A: Core Functional Logic ====================

    @Test(timeout = 4000)
    public void testConstructorFromDoubleArray() {
        double[] data = {1.0, 2.0, 3.0};
        ArrayRealVector v = new ArrayRealVector(data);
        assertArrayEquals(data, v.getData(), 1e-15);
        // ensure data is a copy
        data[0] = 99.0;
        assertEquals(1.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConstructorFromDoubleArrayWithCopyFlag() {
        double[] data = {1.0, 2.0, 3.0};
        ArrayRealVector v = new ArrayRealVector(data, false);
        assertSame(data, v.getDataRef()); // shallow copy
        assertEquals(1.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConstructorFromDoubleArrayWithCopyFlagDeep() {
        double[] data = {1.0, 2.0, 3.0};
        ArrayRealVector v = new ArrayRealVector(data, true);
        assertNotSame(data, v.getDataRef());
        assertArrayEquals(data, v.getData(), 1e-15);
    }

    @Test(timeout = 4000, expected = NullPointerException.class)
    public void testConstructorNullArray() {
        new ArrayRealVector(null, true);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorEmptyArray() {
        new ArrayRealVector(new double[0], true);
    }

    @Test(timeout = 4000)
    public void testConstructorFromSubArray() {
        double[] data = {1.0, 2.0, 3.0, 4.0, 5.0};
        ArrayRealVector v = new ArrayRealVector(data, 1, 3);
        assertEquals(3, v.getDimension());
        assertEquals(2.0, v.getEntry(0), 1e-15);
        assertEquals(3.0, v.getEntry(1), 1e-15);
        assertEquals(4.0, v.getEntry(2), 1e-15);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testConstructorFromSubArrayOutOfBounds() {
        double[] data = {1.0, 2.0, 3.0};
        new ArrayRealVector(data, 1, 3);
    }

    @Test(timeout = 4000)
    public void testConstructorFromDoubleObjectArray() {
        Double[] data = {1.0, 2.0, 3.0};
        ArrayRealVector v = new ArrayRealVector(data);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, v.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConstructorFromDoubleObjectSubArray() {
        Double[] data = {1.0, 2.0, 3.0, 4.0};
        ArrayRealVector v = new ArrayRealVector(data, 1, 2);
        assertEquals(2, v.getDimension());
        assertEquals(2.0, v.getEntry(0), 1e-15);
        assertEquals(3.0, v.getEntry(1), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConstructorFromRealVector() {
        ArrayRealVector base = new ArrayRealVector(new double[]{1.0, 2.0});
        RealVector v = new ArrayRealVector((RealVector) base);
        assertEquals(2, v.getDimension());
        assertEquals(1.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConstructorFromArrayRealVectorDeep() {
        ArrayRealVector base = new ArrayRealVector(new double[]{1.0, 2.0});
        ArrayRealVector v = new ArrayRealVector(base, true);
        base.setEntry(0, 99.0);
        assertEquals(1.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConstructorFromArrayRealVectorShallow() {
        ArrayRealVector base = new ArrayRealVector(new double[]{1.0, 2.0});
        ArrayRealVector v = new ArrayRealVector(base, false);
        base.setEntry(0, 99.0);
        assertEquals(99.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConstructorAppendArrayRealVectors() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{3.0, 4.0});
        ArrayRealVector v = new ArrayRealVector(v1, v2);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, v.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConstructorAppendArrayRealVectorAndRealVector() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        RealVector v2 = new ArrayRealVector(new double[]{3.0, 4.0});
        ArrayRealVector v = new ArrayRealVector(v1, v2);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, v.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConstructorAppendRealVectorAndArrayRealVector() {
        RealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{3.0, 4.0});
        ArrayRealVector v = new ArrayRealVector(v1, v2);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, v.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConstructorAppendArrayRealVectorAndDoubleArray() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        double[] v2 = {3.0, 4.0};
        ArrayRealVector v = new ArrayRealVector(v1, v2);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, v.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConstructorAppendDoubleArrayAndArrayRealVector() {
        double[] v1 = {1.0, 2.0};
        ArrayRealVector v2 = new ArrayRealVector(new double[]{3.0, 4.0});
        ArrayRealVector v = new ArrayRealVector(v1, v2);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, v.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testConstructorAppendDoubleArrays() {
        double[] v1 = {1.0, 2.0};
        double[] v2 = {3.0, 4.0};
        ArrayRealVector v = new ArrayRealVector(v1, v2);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, v.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAdd() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{4.0, 5.0, 6.0});
        ArrayRealVector result = v1.add(v2);
        assertArrayEquals(new double[]{5.0, 7.0, 9.0}, result.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAddDoubleArray() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        RealVector result = v.add(new double[]{3.0, 4.0});
        assertArrayEquals(new double[]{4.0, 6.0}, result.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAddRealVectorNonArray() {
        // Use a RealVector that is not ArrayRealVector (e.g., sparse)
        // We'll use a simple anonymous subclass for testing
        RealVector sparse = new RealVector() {
            @Override
            public int getDimension() { return 2; }
            @Override
            public double getEntry(int index) { return (index == 0) ? 10.0 : 20.0; }
            @Override
            public void setEntry(int index, double value) {}
            @Override
            public RealVector append(double d) { return null; }
            @Override
            public RealVector append(double[] d) { return null; }
            @Override
            public RealVector append(RealVector v) { return null; }
            @Override
            public RealVector getSubVector(int index, int n) { return null; }
            @Override
            public void setSubVector(int index, RealVector v) {}
            @Override
            public boolean isNaN() { return false; }
            @Override
            public boolean isInfinite() { return false; }
            @Override
            public Iterator<Entry> sparseIterator() {
                return java.util.Collections.emptyIterator();
            }
        };
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        RealVector result = v.add(sparse);
        assertArrayEquals(new double[]{11.0, 22.0}, result.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSubtract() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{5.0, 7.0, 9.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{4.0, 5.0, 6.0});
        ArrayRealVector result = v1.subtract(v2);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, result.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSubtractDoubleArray() {
        ArrayRealVector v = new ArrayRealVector(new double[]{5.0, 7.0});
        RealVector result = v.subtract(new double[]{3.0, 4.0});
        assertArrayEquals(new double[]{2.0, 3.0}, result.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapAddToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        v.mapAddToSelf(10.0);
        assertArrayEquals(new double[]{11.0, 12.0, 13.0}, v.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapMultiplyToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        v.mapMultiplyToSelf(2.0);
        assertArrayEquals(new double[]{2.0, 4.0, 6.0}, v.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapDivideToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{2.0, 4.0, 6.0});
        v.mapDivideToSelf(2.0);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, v.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testEbeMultiply() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{4.0, 5.0, 6.0});
        ArrayRealVector result = v1.ebeMultiply(v2);
        assertArrayEquals(new double[]{4.0, 10.0, 18.0}, result.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testEbeDivide() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{4.0, 10.0, 18.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{2.0, 5.0, 6.0});
        ArrayRealVector result = v1.ebeDivide(v2);
        assertArrayEquals(new double[]{2.0, 2.0, 3.0}, result.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDotProduct() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{4.0, 5.0, 6.0});
        double dot = v1.dotProduct(v2);
        assertEquals(32.0, dot, 1e-15);
    }

    @Test(timeout = 4000)
    public void testDotProductDoubleArray() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        double dot = v.dotProduct(new double[]{3.0, 4.0});
        assertEquals(11.0, dot, 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetNorm() {
        ArrayRealVector v = new ArrayRealVector(new double[]{3.0, 4.0});
        assertEquals(5.0, v.getNorm(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetL1Norm() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, -2.0, 3.0});
        assertEquals(6.0, v.getL1Norm(), 1e-15);
    }

    // ==================== Partition B: Boundary Value Analysis & Defect Targeting ====================

    @Test(timeout = 4000)
    public void testGetLInfNorm() {
        // This test targets the known defect: buggy implementation uses += instead of =
        // For vector [1,2,3,4,5,6], correct LInf = 6, buggy returns 21 (L1 sum)
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0, 3.0, 4.0, 5.0, 6.0});
        assertEquals(6.0, v.getLInfNorm(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetLInfNormWithNegative() {
        ArrayRealVector v = new ArrayRealVector(new double[]{-10.0, 5.0, -3.0});
        assertEquals(10.0, v.getLInfNorm(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetDistance() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{4.0, 5.0, 6.0});
        double dist = v1.getDistance(v2);
        assertEquals(Math.sqrt(27), dist, 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetL1Distance() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{4.0, 5.0, 6.0});
        double dist = v1.getL1Distance(v2);
        assertEquals(9.0, dist, 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetLInfDistance() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{4.0, 5.0, 6.0});
        double dist = v1.getLInfDistance(v2);
        assertEquals(3.0, dist, 1e-15);
    }

    @Test(timeout = 4000)
    public void testUnitVector() {
        ArrayRealVector v = new ArrayRealVector(new double[]{3.0, 0.0, 4.0});
        RealVector unit = v.unitVector();
        assertEquals(1.0, unit.getNorm(), 1e-15);
        assertEquals(0.6, unit.getEntry(0), 1e-15);
        assertEquals(0.0, unit.getEntry(1), 1e-15);
        assertEquals(0.8, unit.getEntry(2), 1e-15);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testUnitVectorZeroNorm() {
        ArrayRealVector v = new ArrayRealVector(new double[]{0.0, 0.0});
        v.unitVector();
    }

    @Test(timeout = 4000)
    public void testUnitize() {
        ArrayRealVector v = new ArrayRealVector(new double[]{3.0, 4.0});
        v.unitize();
        assertEquals(1.0, v.getNorm(), 1e-15);
        assertEquals(0.6, v.getEntry(0), 1e-15);
        assertEquals(0.8, v.getEntry(1), 1e-15);
    }

    @Test(timeout = 4000, expected = ArithmeticException.class)
    public void testUnitizeZeroNorm() {
        ArrayRealVector v = new ArrayRealVector(new double[]{0.0, 0.0});
        v.unitize();
    }

    @Test(timeout = 4000)
    public void testProjection() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1.0, 0.0, 0.0});
        RealVector proj = v1.projection(v2);
        assertEquals(1.0, proj.getEntry(0), 1e-15);
        assertEquals(0.0, proj.getEntry(1), 1e-15);
        assertEquals(0.0, proj.getEntry(2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testOuterProduct() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        RealMatrix m = v.outerProduct(v);
        assertEquals(1.0, m.getEntry(0, 0), 1e-15);
        assertEquals(2.0, m.getEntry(0, 1), 1e-15);
        assertEquals(2.0, m.getEntry(1, 0), 1e-15);
        assertEquals(4.0, m.getEntry(1, 1), 1e-15);
    }

    // ==================== Partition C: Exception & Defensive Guard Paths ====================

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testAddDimensionMismatch() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        v1.add(v2);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testDotProductDimensionMismatch() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        v1.dotProduct(new double[]{1.0, 2.0, 3.0});
    }

    @Test(timeout = 4000, expected = MatrixIndexException.class)
    public void testGetEntryOutOfBounds() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        v.getEntry(5);
    }

    @Test(timeout = 4000, expected = MatrixIndexException.class)
    public void testSetEntryOutOfBounds() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        v.setEntry(5, 10.0);
    }

    @Test(timeout = 4000, expected = IllegalArgumentException.class)
    public void testGetSubVectorOutOfBounds() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        v.getSubVector(1, 3);
    }

    @Test(timeout = 4000)
    public void testGetSubVectorNormal() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0, 3.0, 4.0});
        RealVector sub = v.getSubVector(1, 2);
        assertEquals(2, sub.getDimension());
        assertEquals(2.0, sub.getEntry(0), 1e-15);
        assertEquals(3.0, sub.getEntry(1), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSetSubVector() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0, 3.0, 4.0});
        v.setSubVector(1, new ArrayRealVector(new double[]{99.0, 100.0}));
        assertEquals(1.0, v.getEntry(0), 1e-15);
        assertEquals(99.0, v.getEntry(1), 1e-15);
        assertEquals(100.0, v.getEntry(2), 1e-15);
        assertEquals(4.0, v.getEntry(3), 1e-15);
    }

    @Test(timeout = 4000, expected = MatrixIndexException.class)
    public void testSetSubVectorOutOfBounds() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        v.setSubVector(1, new ArrayRealVector(new double[]{1.0, 2.0}));
    }

    @Test(timeout = 4000)
    public void testAppendDouble() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        RealVector result = v.append(3.0);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, result.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAppendDoubleArray() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        RealVector result = v.append(new double[]{3.0, 4.0});
        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, result.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAppendRealVector() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        RealVector result = v.append(new ArrayRealVector(new double[]{3.0, 4.0}));
        assertArrayEquals(new double[]{1.0, 2.0, 3.0, 4.0}, result.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSetAll() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        v.set(5.0);
        assertArrayEquals(new double[]{5.0, 5.0, 5.0}, v.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testToArray() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        double[] arr = v.toArray();
        assertArrayEquals(new double[]{1.0, 2.0}, arr, 1e-15);
        // ensure it's a copy
        arr[0] = 99.0;
        assertEquals(1.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testCopy() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        AbstractRealVector copy = v.copy();
        assertTrue(copy instanceof ArrayRealVector);
        assertArrayEquals(v.getData(), copy.getData(), 1e-15);
        v.setEntry(0, 99.0);
        assertEquals(1.0, copy.getEntry(0), 1e-15);
    }

    // ==================== Partition D: Object Lifecycle & Contract Integrity ====================

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        assertTrue(v.equals(v));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        assertFalse(v.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        assertFalse(v.equals("not a vector"));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentDimension() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        assertFalse(v1.equals(v2));
    }

    @Test(timeout = 4000)
    public void testEqualsExactMatch() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        assertTrue(v1.equals(v2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentValues() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1.0, 2.0, 4.0});
        assertFalse(v1.equals(v2));
    }

    @Test(timeout = 4000)
    public void testEqualsNaNHandling() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{Double.NaN, 2.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{Double.NaN, 2.0});
        // According to spec, NaN vectors are equal to each other
        assertTrue(v1.equals(v2));
        // Also, a NaN vector is not equal to a non-NaN vector
        ArrayRealVector v3 = new ArrayRealVector(new double[]{1.0, 2.0});
        assertFalse(v1.equals(v3));
    }

    @Test(timeout = 4000)
    public void testHashCode() {
        ArrayRealVector v1 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeNaN() {
        ArrayRealVector v = new ArrayRealVector(new double[]{Double.NaN, 2.0});
        assertEquals(9, v.hashCode());
    }

    @Test(timeout = 4000)
    public void testIsNaN() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, Double.NaN, 3.0});
        assertTrue(v.isNaN());
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        assertFalse(v2.isNaN());
    }

    @Test(timeout = 4000)
    public void testIsInfinite() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, Double.POSITIVE_INFINITY, 3.0});
        assertTrue(v.isInfinite());
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        assertFalse(v2.isInfinite());
        // If NaN present, isInfinite returns false
        ArrayRealVector v3 = new ArrayRealVector(new double[]{1.0, Double.NaN, Double.POSITIVE_INFINITY});
        assertFalse(v3.isInfinite());
    }

    @Test(timeout = 4000)
    public void testGetDimension() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        assertEquals(3, v.getDimension());
    }

    @Test(timeout = 4000)
    public void testGetDataRef() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        double[] ref = v.getDataRef();
        assertSame(v.data, ref);
    }

    @Test(timeout = 4000)
    public void testToString() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        String str = v.toString();
        assertNotNull(str);
        assertTrue(str.contains("1.0"));
    }

    // ==================== Partition E: Additional Map Functions (representative) ====================

    @Test(timeout = 4000)
    public void testMapExpToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{0.0, 1.0});
        v.mapExpToSelf();
        assertEquals(1.0, v.getEntry(0), 1e-15);
        assertEquals(Math.E, v.getEntry(1), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapLogToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, Math.E});
        v.mapLogToSelf();
        assertEquals(0.0, v.getEntry(0), 1e-15);
        assertEquals(1.0, v.getEntry(1), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapSqrtToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{4.0, 9.0});
        v.mapSqrtToSelf();
        assertEquals(2.0, v.getEntry(0), 1e-15);
        assertEquals(3.0, v.getEntry(1), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapAbsToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{-1.0, 2.0, -3.0});
        v.mapAbsToSelf();
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, v.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapSignumToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{-5.0, 0.0, 3.0});
        v.mapSignumToSelf();
        assertEquals(-1.0, v.getEntry(0), 1e-15);
        assertEquals(0.0, v.getEntry(1), 1e-15);
        assertEquals(1.0, v.getEntry(2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapInvToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{2.0, 4.0});
        v.mapInvToSelf();
        assertEquals(0.5, v.getEntry(0), 1e-15);
        assertEquals(0.25, v.getEntry(1), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapPowToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{2.0, 3.0});
        v.mapPowToSelf(2.0);
        assertEquals(4.0, v.getEntry(0), 1e-15);
        assertEquals(9.0, v.getEntry(1), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapSubtractToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{10.0, 20.0});
        v.mapSubtractToSelf(5.0);
        assertArrayEquals(new double[]{5.0, 15.0}, v.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapCeilToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.2, 2.7, -0.5});
        v.mapCeilToSelf();
        assertEquals(2.0, v.getEntry(0), 1e-15);
        assertEquals(3.0, v.getEntry(1), 1e-15);
        assertEquals(0.0, v.getEntry(2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapFloorToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.2, 2.7, -0.5});
        v.mapFloorToSelf();
        assertEquals(1.0, v.getEntry(0), 1e-15);
        assertEquals(2.0, v.getEntry(1), 1e-15);
        assertEquals(-1.0, v.getEntry(2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapRintToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.5, 2.5, -0.5});
        v.mapRintToSelf();
        assertEquals(2.0, v.getEntry(0), 1e-15);
        assertEquals(2.0, v.getEntry(1), 1e-15);
        assertEquals(0.0, v.getEntry(2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapUlpToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 2.0});
        v.mapUlpToSelf();
        assertEquals(Math.ulp(1.0), v.getEntry(0), 1e-15);
        assertEquals(Math.ulp(2.0), v.getEntry(1), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapCoshToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{0.0});
        v.mapCoshToSelf();
        assertEquals(1.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapSinhToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{0.0});
        v.mapSinhToSelf();
        assertEquals(0.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapTanhToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{0.0});
        v.mapTanhToSelf();
        assertEquals(0.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapCosToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{0.0});
        v.mapCosToSelf();
        assertEquals(1.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapSinToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{0.0});
        v.mapSinToSelf();
        assertEquals(0.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapTanToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{0.0});
        v.mapTanToSelf();
        assertEquals(0.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapAcosToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0});
        v.mapAcosToSelf();
        assertEquals(0.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapAsinToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{0.0});
        v.mapAsinToSelf();
        assertEquals(0.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapAtanToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{0.0});
        v.mapAtanToSelf();
        assertEquals(0.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapExpm1ToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{0.0});
        v.mapExpm1ToSelf();
        assertEquals(0.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapLog10ToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{1.0, 10.0});
        v.mapLog10ToSelf();
        assertEquals(0.0, v.getEntry(0), 1e-15);
        assertEquals(1.0, v.getEntry(1), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapLog1pToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{0.0, Math.E - 1});
        v.mapLog1pToSelf();
        assertEquals(0.0, v.getEntry(0), 1e-15);
        assertEquals(1.0, v.getEntry(1), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapCbrtToSelf() {
        ArrayRealVector v = new ArrayRealVector(new double[]{8.0, 27.0});
        v.mapCbrtToSelf();
        assertEquals(2.0, v.getEntry(0), 1e-15);
        assertEquals(3.0, v.getEntry(1), 1e-15);
    }
}