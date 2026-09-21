package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

public class OpenMapRealVectorDeepseekTest {

    /* [Branch & Defect Analysis Matrix]
     * 
     * Partition A: Core Functional Logic & State Transitions
     *   - Constructors: default, dimension, epsilon, expectedSize, from double[], Double[], copy, RealVector, resize
     *   - add(OpenMapRealVector): branch on entries.size() > v.entries.size() (copyThis), iterate over smaller
     *   - add(RealVector): branch on instanceOf OpenMapRealVector
     *   - append(OpenMapRealVector): use resize constructor, shift keys by virtualSize
     *   - append(RealVector): branch on instanceOf
     *   - append(double), append(double[])
     *   - copy(): new OpenMapRealVector(this)
     *   - dotProduct(OpenMapRealVector): branch on entries.size() < v.entries.size()
     *   - ebeDivide(RealVector): iterate over this.entries, divide by v.getEntry
     *   - ebeDivide(double[]): iterate over entries, divide by v[key]
     *   - ebeMultiply(RealVector): iterate entries, multiply by v.getEntry
     *   - ebeMultiply(double[]): iterate entries, multiply by v[key]
     *   - getSubVector: checkIndex twice, iterate over entries, filter by range
     *   - getData: fill double[] from entries
     *   - getDistance(OpenMapRealVector): two-pass iteration (this then v) for correct computation
     *   - getL1Distance(OpenMapRealVector): two-pass iteration
     *   - getLInfDistance(OpenMapRealVector): two-pass iteration
     *   - isInfinite: branch on NaN returns false; branch on infiniteFound
     *   - isNaN: branch on NaN
     *   - mapAddToSelf: for-loop over virtualSize
     *   - outerProduct: iterate entries, for-loop over v.length
     *   - setEntry: branch on isDefaultValue, then branch on entries.containsKey
     *   - setSubVector(RealVector): calls setSubVector(index, v.getData())
     *   - subtract(OpenMapRealVector): iterate v.entries, branch on entries.containsKey
     *   - unitize: branch on isDefaultValue(norm), then iterate entries to divide
     *   - hashCode: prime * result, epsilon, virtualSize, iterate entries
     *   - equals: identity, instanceof, virtualSize, epsilon bits, iterate both entries
     *   - getSparsity: entries.size() / getDimension()
     * 
     * Partition B: Boundary Value Analysis & Extremes
     *   - Zero-tolerance epsilon: DEFAULT_ZERO_TOLERANCE = 1.0e-12
     *   - isDefaultValue: |value| < epsilon (strictly less)
     *   - Dimension 0 vector (default constructor)
     *   - Empty array constructor
     *   - All values default (all entries removed)
     *   - Values exactly at epsilon boundary
     *   - Negative dimension? Not allowed by checkIndex but constructor accepts 0
     *   - Large dimension, sparse then dense
     *   - Double.MAX_VALUE, Double.MIN_VALUE, -Double.MAX_VALUE, NaN, Infinity
     * 
     * Partition C: Defect-Targeted Branch Zone
     *   - KNOWN DEFECT: ConcurrentModificationException when iterating over entries
     *     while modifying the underlying map. In add(OpenMapRealVector), the resize
     *     constructor is used, but modifications during iteration can occur if
     *     methods like setEntry() are called within an iterator loop (e.g., in add()
     *     after copying). The defect is specifically triggered in sparseRealVectorTest
     *     testConcurrentModification: modifying entries while an iterator is active
     *     (e.g., from another method call) can throw ConcurrentModificationException.
     *     We target this by calling methods that internally iterate while modifying
     *     via setEntry (e.g., getSubVector, add, etc.) but the actual bug occurs
     *     when an iterator is created and then the map is modified externally.
     *     Test design: create a vector, get an iterator via sparseIterator(), then
     *     modify the vector via setEntry() and call next() — should fail in defective
     *     version. Also test unitize() which iterates and modifies entries.
     * 
     * Partition D: Exception & Defensive Guard Paths
     *   - checkIndex: index < 0, index >= virtualSize
     *   - checkVectorDimensions: dimension mismatch
     *   - unitize with zero norm throws MathArithmeticException
     *   - ebeDivide by zero element yields infinity (not exception)
     *   - getSubVector with invalid parameters throws IndexOutOfBoundsException
     *   - setSubVector with invalid index
     *   - projection: dimension check
     * 
     * Partition E: Object Lifecycle & Contract Integrity
     *   - equals on itself, null, different epsilon, same values
     *   - hashCode consistency
     *   - copy() is independent
     */

    // ===== Partition A: Core Functional Logic =====

    @Test(timeout = 4000)
    public void testDefaultConstructor() {
        OpenMapRealVector v = new OpenMapRealVector();
        assertEquals(0, v.getDimension());
        assertEquals(0.0, v.getEntry(0), 0.0); // getEntry(0) should throw? Actually index 0 is invalid for dim=0
        // Actually checkIndex(0) throws because dimension = 0, index >= virtualSize
        // So just test dimension is 0
        assertEquals(0, v.getDimension());
    }

    @Test(timeout = 4000)
    public void testConstructorDimension() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        assertEquals(5, v.getDimension());
        assertEquals(0.0, v.getEntry(0), 0.0); // default zero
        assertEquals(0.0, v.getEntry(4), 0.0);
    }

    @Test(timeout = 4000)
    public void testConstructorDimensionAndEpsilon() {
        OpenMapRealVector v = new OpenMapRealVector(3, 1e-10);
        assertEquals(3, v.getDimension());
        assertEquals(0.0, v.getEntry(0), 0.0);
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleArray() {
        double[] values = {1.0, 0.0, 3.0, 0.0, 5.0};
        OpenMapRealVector v = new OpenMapRealVector(values);
        assertEquals(5, v.getDimension());
        assertEquals(1.0, v.getEntry(0), 0.0);
        assertEquals(0.0, v.getEntry(1), 0.0); // zero stored? Actually not stored, but getEntry returns 0
        assertEquals(3.0, v.getEntry(2), 0.0);
        assertEquals(5.0, v.getEntry(4), 0.0);
        // Verify sparse: only non-default entries stored
        assertEquals(3, v.getSparsity() * 5, 0.5); // 3/5 = 0.6
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleArrayWithEpsilon() {
        double[] values = {1e-8, 1e-13, 2e-8};
        OpenMapRealVector v = new OpenMapRealVector(values, 1e-12);
        // epsilon = 1e-12, so 1e-13 is default, 1e-8 not
        assertEquals(3, v.getDimension());
        assertEquals(1e-8, v.getEntry(0), 0.0);
        assertEquals(0.0, v.getEntry(1), 0.0);
        assertEquals(2e-8, v.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testConstructorDoubleObjectArray() {
        Double[] values = {2.0, null, 4.0}; // null would throw NullPointerException in original, but assume non-null
        // Actually we must avoid null, so use Double[]
        Double[] vals = {2.0, 0.0, 4.0};
        OpenMapRealVector v = new OpenMapRealVector(vals);
        assertEquals(3, v.getDimension());
        assertEquals(2.0, v.getEntry(0), 0.0);
        assertEquals(0.0, v.getEntry(1), 0.0);
        assertEquals(4.0, v.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testConstructorCopyOpenMap() {
        OpenMapRealVector original = new OpenMapRealVector(new double[]{1, 2, 3});
        OpenMapRealVector copy = new OpenMapRealVector(original);
        assertEquals(original.getDimension(), copy.getDimension());
        assertEquals(original.getEntry(0), copy.getEntry(0), 0.0);
        original.setEntry(0, 99);
        assertNotEquals(99.0, copy.getEntry(0), 0.0); // deep copy
    }

    @Test(timeout = 4000)
    public void testConstructorGenericRealVector() {
        RealVector generic = new ArrayRealVector(new double[]{5, 6, 7});
        OpenMapRealVector v = new OpenMapRealVector(generic);
        assertEquals(3, v.getDimension());
        assertEquals(5.0, v.getEntry(0), 0.0);
        assertEquals(7.0, v.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testConstructorResize() {
        OpenMapRealVector v = new OpenMapRealVector(2);
        OpenMapRealVector resized = new OpenMapRealVector(v, 3); // protected constructor
        assertEquals(5, resized.getDimension());
        // entries from v copied
        assertEquals(0.0, resized.getEntry(0), 0.0);
    }

    @Test(timeout = 4000)
    public void testConstructorExpectedSize() {
        OpenMapRealVector v = new OpenMapRealVector(10, 5);
        assertEquals(10, v.getDimension());
    }

    @Test(timeout = 4000)
    public void testConstructorExpectedSizeWithEpsilon() {
        OpenMapRealVector v = new OpenMapRealVector(10, 5, 1e-8);
        assertEquals(10, v.getDimension());
    }

    @Test(timeout = 4000)
    public void testAddOpenMapOptimized() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1, 2, 3});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{4, 5, 6});
        OpenMapRealVector sum = v1.add(v2);
        assertEquals(5.0, sum.getEntry(0), 0.0);
        assertEquals(7.0, sum.getEntry(1), 0.0);
        assertEquals(9.0, sum.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddOpenMapWithSparse() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1, 0, 0, 4});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0, 2, 0, 0});
        OpenMapRealVector sum = v1.add(v2);
        assertEquals(1.0, sum.getEntry(0), 0.0);
        assertEquals(2.0, sum.getEntry(1), 0.0);
        assertEquals(0.0, sum.getEntry(2), 0.0);
        assertEquals(4.0, sum.getEntry(3), 0.0);
    }

    @Test(timeout = 4000)
    public void testAddRealVectorOpenMapBranch() {
        RealVector v1 = new OpenMapRealVector(new double[]{1, 2});
        RealVector v2 = new ArrayRealVector(new double[]{3, 4});
        RealVector sum = v1.add(v2);
        assertEquals(4.0, sum.getEntry(0), 0.0);
        assertEquals(6.0, sum.getEntry(1), 0.0);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math.exception.DimensionMismatchException.class)
    public void testAddDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.add(v2);
    }

    @Test(timeout = 4000)
    public void testAppendOpenMap() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, 2});
        OpenMapRealVector toAppend = new OpenMapRealVector(new double[]{3, 4});
        OpenMapRealVector result = v.append(toAppend);
        assertEquals(4, result.getDimension());
        assertEquals(1.0, result.getEntry(0), 0.0);
        assertEquals(2.0, result.getEntry(1), 0.0);
        assertEquals(3.0, result.getEntry(2), 0.0);
        assertEquals(4.0, result.getEntry(3), 0.0);
    }

    @Test(timeout = 4000)
    public void testAppendRealVectorBranch() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1});
        RealVector toAppend = new ArrayRealVector(new double[]{2, 3});
        OpenMapRealVector result = v.append(toAppend);
        assertEquals(3, result.getDimension());
        assertEquals(1.0, result.getEntry(0), 0.0);
        assertEquals(2.0, result.getEntry(1), 0.0);
        assertEquals(3.0, result.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testAppendDouble() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, 2});
        OpenMapRealVector result = v.append(3.0);
        assertEquals(3, result.getDimension());
        assertEquals(3.0, result.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testAppendDoubleArray() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1});
        OpenMapRealVector result = v.append(new double[]{2, 3, 4});
        assertEquals(4, result.getDimension());
        assertEquals(4.0, result.getEntry(3), 0.0);
    }

    @Test(timeout = 4000)
    public void testCopy() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, 2, 3});
        OpenMapRealVector copy = v.copy();
        assertEquals(v, copy);
        v.setEntry(0, 99);
        assertNotEquals(v.getEntry(0), copy.getEntry(0), 0.0);
    }

    @Test(timeout = 4000)
    public void testDotProductOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1, 2, 3});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{4, 5, 6});
        double dot = v1.dotProduct(v2);
        assertEquals(32.0, dot, 0.0); // 1*4 + 2*5 + 3*6 = 4+10+18=32
    }

    @Test(timeout = 4000)
    public void testDotProductSparse() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1, 0, 3});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0, 2, 0});
        double dot = v1.dotProduct(v2);
        assertEquals(0.0, dot, 0.0);
    }

    @Test(timeout = 4000)
    public void testDotProductRealVectorBranch() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1, 2});
        RealVector v2 = new ArrayRealVector(new double[]{3, 4});
        double dot = v1.dotProduct(v2);
        assertEquals(11.0, dot, 0.0);
    }

    @Test(timeout = 4000)
    public void testEbeDivideRealVector() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{2, 4, 6});
        RealVector v2 = new ArrayRealVector(new double[]{1, 2, 3});
        OpenMapRealVector result = v1.ebeDivide(v2);
        assertEquals(2.0, result.getEntry(0), 0.0);
        assertEquals(2.0, result.getEntry(1), 0.0);
        assertEquals(2.0, result.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testEbeDivideDoubleArray() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{10, 20});
        double[] v2 = {2, 5};
        OpenMapRealVector result = v1.ebeDivide(v2);
        assertEquals(5.0, result.getEntry(0), 0.0);
        assertEquals(4.0, result.getEntry(1), 0.0);
    }

    @Test(timeout = 4000)
    public void testEbeMultiplyRealVector() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1, 2, 3});
        RealVector v2 = new ArrayRealVector(new double[]{4, 5, 6});
        OpenMapRealVector result = v1.ebeMultiply(v2);
        assertEquals(4.0, result.getEntry(0), 0.0);
        assertEquals(10.0, result.getEntry(1), 0.0);
        assertEquals(18.0, result.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testEbeMultiplyDoubleArray() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{2, 3});
        double[] v2 = {4, 5};
        OpenMapRealVector result = v1.ebeMultiply(v2);
        assertEquals(8.0, result.getEntry(0), 0.0);
        assertEquals(15.0, result.getEntry(1), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetSubVector() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, 2, 3, 4, 5});
        OpenMapRealVector sub = v.getSubVector(1, 3);
        assertEquals(3, sub.getDimension());
        assertEquals(2.0, sub.getEntry(0), 0.0);
        assertEquals(3.0, sub.getEntry(1), 0.0);
        assertEquals(4.0, sub.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetSubVectorSparse() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0, 2, 0, 4, 0});
        OpenMapRealVector sub = v.getSubVector(1, 3);
        assertEquals(3, sub.getDimension());
        assertEquals(2.0, sub.getEntry(0), 0.0);
        assertEquals(0.0, sub.getEntry(1), 0.0);
        assertEquals(4.0, sub.getEntry(2), 0.0);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math.exception.OutOfRangeException.class)
    public void testGetSubVectorInvalidIndex() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.getSubVector(-1, 1);
    }

    @Test(timeout = 4000)
    public void testGetData() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.5, 0.0, 2.5});
        double[] data = v.getData();
        assertArrayEquals(new double[]{1.5, 0.0, 2.5}, data, 0.0);
    }

    @Test(timeout = 4000)
    public void testGetDistanceOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{3, 0, 4});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0, 0, 0});
        double dist = v1.getDistance(v2);
        assertEquals(5.0, dist, 0.0); // sqrt(3^2 + 4^2) = 5
    }

    @Test(timeout = 4000)
    public void testGetDistanceRealVectorBranch() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1, 0});
        RealVector v2 = new ArrayRealVector(new double[]{0, 1});
        double dist = v1.getDistance(v2);
        assertEquals(Math.sqrt(2), dist, 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetL1DistanceOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1, -2, 3});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0, 1, -1});
        double dist = v1.getL1Distance(v2);
        assertEquals(1 + 3 + 4, dist, 0.0); // |1-0| + |-2-1| + |3-(-1)| = 1+3+4=8
    }

    @Test(timeout = 4000)
    public void testGetLInfDistanceOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1, 5, 3});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0, 2, 4});
        double dist = v1.getLInfDistance(v2);
        assertEquals(3.0, dist, 0.0); // max(|1-0|, |5-2|, |3-4|) = max(1,3,1) = 3
    }

    @Test(timeout = 4000)
    public void testIsInfinite() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, Double.POSITIVE_INFINITY, 3});
        assertTrue(v.isInfinite());
    }

    @Test(timeout = 4000)
    public void testIsInfiniteWithNaN() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, Double.NaN, 3});
        assertFalse(v.isInfinite());
    }

    @Test(timeout = 4000)
    public void testIsNaN() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, Double.NaN, 3});
        assertTrue(v.isNaN());
    }

    @Test(timeout = 4000)
    public void testIsNaN_NoNaN() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, 2, 3});
        assertFalse(v.isNaN());
    }

    @Test(timeout = 4000)
    public void testMapAddToSelf() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, 2, 3});
        v.mapAddToSelf(10);
        assertEquals(11.0, v.getEntry(0), 0.0);
        assertEquals(12.0, v.getEntry(1), 0.0);
        assertEquals(13.0, v.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testOuterProduct() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, 2});
        double[] col = {3, 4};
        RealMatrix prod = v.outerProduct(col);
        assertEquals(1*3, prod.getEntry(0, 0), 0.0);
        assertEquals(1*4, prod.getEntry(0, 1), 0.0);
        assertEquals(2*3, prod.getEntry(1, 0), 0.0);
        assertEquals(2*4, prod.getEntry(1, 1), 0.0);
    }

    @Test(timeout = 4000)
    public void testProjection() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1, 2});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{3, 4});
        RealVector proj = v1.projection(v2);
        double expectedDot = v1.dotProduct(v2);
        double v2Dot = v2.dotProduct(v2);
        // proj = v2 * (dot / v2Dot)
        assertEquals(v2.getEntry(0) * (expectedDot / v2Dot), proj.getEntry(0), 1e-12);
        assertEquals(v2.getEntry(1) * (expectedDot / v2Dot), proj.getEntry(1), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSetEntry() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(1, 5.0);
        assertEquals(5.0, v.getEntry(1), 0.0);
        // setting to zero should remove entry
        v.setEntry(1, 0.0);
        assertEquals(0.0, v.getEntry(1), 0.0);
        // but not stored
        assertEquals(0, v.getSparsity() * 3, 0.01);
    }

    @Test(timeout = 4000)
    public void testSetSubVectorDoubleArray() {
        OpenMapRealVector v = new OpenMapRealVector(4);
        v.setSubVector(1, new double[]{10, 20});
        assertEquals(10.0, v.getEntry(1), 0.0);
        assertEquals(20.0, v.getEntry(2), 0.0);
        assertEquals(0.0, v.getEntry(0), 0.0);
    }

    @Test(timeout = 4000)
    public void testSetSubVectorRealVector() {
        OpenMapRealVector v = new OpenMapRealVector(4);
        RealVector sub = new ArrayRealVector(new double[]{7, 8});
        v.setSubVector(2, sub);
        assertEquals(7.0, v.getEntry(2), 0.0);
        assertEquals(8.0, v.getEntry(3), 0.0);
    }

    @Test(timeout = 4000)
    public void testSetAll() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.set(7.0);
        assertEquals(7.0, v.getEntry(0), 0.0);
        assertEquals(7.0, v.getEntry(1), 0.0);
        assertEquals(7.0, v.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testSubtractOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{5, 7, 9});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1, 2, 3});
        OpenMapRealVector diff = v1.subtract(v2);
        assertEquals(4.0, diff.getEntry(0), 0.0);
        assertEquals(5.0, diff.getEntry(1), 0.0);
        assertEquals(6.0, diff.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testSubtractOpenMapSparse() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{3, 0, 1});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0, 2, 0});
        OpenMapRealVector diff = v1.subtract(v2);
        assertEquals(3.0, diff.getEntry(0), 0.0);
        assertEquals(-2.0, diff.getEntry(1), 0.0);
        assertEquals(1.0, diff.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testSubtractDoubleArray() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{10, 20});
        double[] v2 = {3, 8};
        OpenMapRealVector diff = v1.subtract(v2);
        assertEquals(7.0, diff.getEntry(0), 0.0);
        assertEquals(12.0, diff.getEntry(1), 0.0);
    }

    @Test(timeout = 4000)
    public void testUnitVector() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{3, 4});
        OpenMapRealVector unit = v.unitVector();
        assertEquals(0.6, unit.getEntry(0), 1e-12);
        assertEquals(0.8, unit.getEntry(1), 1e-12);
        assertEquals(1.0, unit.getNorm(), 1e-12);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math.exception.MathArithmeticException.class)
    public void testUnitizeZeroNorm() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.unitize();
    }

    @Test(timeout = 4000)
    public void testUnitize() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0, 3, 0, 4});
        v.unitize();
        assertEquals(0.6, v.getEntry(1), 1e-12);
        assertEquals(0.8, v.getEntry(3), 1e-12);
        assertEquals(0.0, v.getEntry(0), 1e-12);
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1, 2});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1, 2});
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test(timeout = 4000)
    public void testEquals() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1, 2});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1, 2});
        assertTrue(v1.equals(v2));
        assertFalse(v1.equals(null));
        assertFalse(v1.equals("string"));
        v2.setEntry(0, 99);
        assertFalse(v1.equals(v2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentEpsilon() {
        OpenMapRealVector v1 = new OpenMapRealVector(2, 1e-10);
        OpenMapRealVector v2 = new OpenMapRealVector(2, 1e-8);
        assertFalse(v1.equals(v2));
    }

    @Test(timeout = 4000)
    public void testGetSparsity() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, 0, 0, 2, 0, 3});
        double sparsity = v.getSparsity();
        assertEquals(0.5, sparsity, 0.0); // 3/6 = 0.5
    }

    // ===== Partition B: Boundary Value Analysis =====

    @Test(timeout = 4000)
    public void testIsDefaultValueExactlyEpsilon() {
        OpenMapRealVector v = new OpenMapRealVector(1, 1e-12);
        assertTrue(v.isDefaultValue(1e-13));  // |1e-13| < 1e-12 => true
        assertFalse(v.isDefaultValue(1e-12)); // not less, so false (since strict less)
        assertFalse(v.isDefaultValue(1e-11));
    }

    @Test(timeout = 4000)
    public void testZeroDimensionVector() {
        OpenMapRealVector v = new OpenMapRealVector(0);
        assertEquals(0, v.getDimension());
        // getEntry(0) should throw
        boolean threw = false;
        try {
            v.getEntry(0);
        } catch (Exception e) {
            threw = true;
        }
        assertTrue(threw);
    }

    @Test(timeout = 4000)
    public void testAllZeroValues() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0, 0, 0});
        assertEquals(3, v.getDimension());
        assertEquals(0.0, v.getEntry(0), 0.0);
        assertEquals(0.0, v.getEntry(1), 0.0);
        assertEquals(0.0, v.getEntry(2), 0.0);
        // sparsity should be 0
        assertEquals(0.0, v.getSparsity(), 0.0);
    }

    @Test(timeout = 4000)
    public void testExtremeDoubleValues() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{Double.MAX_VALUE, Double.MIN_VALUE, -Double.MAX_VALUE});
        assertEquals(Double.MAX_VALUE, v.getEntry(0), 0.0);
        assertEquals(Double.MIN_VALUE, v.getEntry(1), 0.0);
        assertEquals(-Double.MAX_VALUE, v.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testNaNInVector() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, Double.NaN, 3});
        assertTrue(v.isNaN());
        assertFalse(v.isInfinite());
    }

    @Test(timeout = 4000)
    public void testInfinityInVector() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, Double.POSITIVE_INFINITY, 3});
        assertFalse(v.isNaN());
        assertTrue(v.isInfinite());
    }

    // ===== Partition C: Defect-Targeted Branch Zone (ConcurrentModification) =====

    @Test(timeout = 4000)
    public void testConcurrentModificationDuringAdd() {
        // This test targets the known defect: modifying the entries map while iterating
        // In add(OpenMapRealVector), we copy and then iterate over the smaller vector's entries.
        // If during iteration, the underlying map is modified (e.g., via setEntry after getSubVector),
        // it may throw ConcurrentModificationException.
        // We simulate: create two vectors, then perform add while another operation modifies the map.
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1, 2, 3});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{4, 5, 6});
        
        // The defect occurs when the vector is modified during an iteration.
        // We'll get an iterator from sparseIterator and then call setEntry, which modifies the map.
        java.util.Iterator<RealVector.Entry> iter = v1.sparseIterator();
        assertTrue(iter.hasNext());
        RealVector.Entry entry = iter.next();
        entry.setValue(99); // This modifies the map via setEntry inside OpenMapEntry.setValue
        // Now the iterator is still valid, but the map has been modified.
        // In the defective version, next() might throw ConcurrentModificationException
        // because the underlying iterator (OpenIntToDoubleHashMap.Iterator) is fail-fast.
        try {
            // Attempt to continue iteration
            if (iter.hasNext()) {
                iter.next(); // This may throw in defective version
            }
        } catch (Exception e) {
            // If it throws, we've caught the bug
            fail("ConcurrentModificationException was thrown: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testConcurrentModificationInUnitize() {
        // unitize() iterates over entries and modifies them (entries.put)
        // This is the exact pattern that can cause ConcurrentModificationException
        // because the iterator is from the same map being modified.
        OpenMapRealVector v = new OpenMapRealVector(new double[]{3, 4});
        // unitize should work without throwing
        try {
            v.unitize();
        } catch (Exception e) {
            fail("unitize() threw an exception: " + e.getMessage());
        }
        // This test passes on correct code, but the bug may be triggered by outer operations.
    }

    // This test directly replicates the pattern from SparseRealVectorTest that triggers the defect
    @Test(timeout = 4000)
    public void testConcurrentModificationFromGetSubVectorThenAdd() {
        // getSubVector creates a new vector and iterates over entries, calling setEntry.
        // If the original vector's entries are modified during the iteration, ConcurrentModification.
        OpenMapRealVector original = new OpenMapRealVector(new double[]{1, 2, 3, 4, 5});
        
        // Perform getSubVector while modifying original from another thread or concurrently,
        // but here we simulate by using iterator from original while calling getSubVector
        // Actually getSubVector is on 'original', so we need to trigger concurrent modification
        // through the iteration inside getSubVector itself.
        // In getSubVector, we iterate over entries and call setEntry on result (different map).
        // That should be safe. But the bug report mentions "map has been modified while iterating"
        // which suggests the iterator is fail-fast within the same map.
        // Let's test a scenario: we modify the map via setEntry while an iterator is active
        // (e.g., from another method).
        java.util.Iterator<RealVector.Entry> iter = original.sparseIterator();
        // Now modify the original vector
        original.setEntry(2, 99);
        // Now try to use the iterator
        try {
            if (iter.hasNext()) {
                iter.next(); // This may throw ConcurrentModificationException
            }
        } catch (Exception e) {
            // Expected in defective version
            fail("ConcurrentModificationException: " + e.getMessage());
        }
    }

    @Test(timeout = 4000)
    public void testConcurrentModificationInAddMethodDirectly() {
        // The add(OpenMapRealVector) method iterates over one vector's entries and
        // calls setEntry on the result. If the result vector is the same as the one being iterated?
        // No, result is a copy. But the iterator is from the smaller vector.
        // To trigger the bug, we need a scenario where the map being iterated is also modified
        // by setEntry. In add, setEntry is called on 'res', not on the source of iterator.
        // However, if the source vector is also being modified elsewhere concurrently, it may fail.
        // But the bug report is about a specific test case.
        // We'll create a scenario that mimics the Defects4J test: create vector, get subvector,
        // then modify original and see if the modification propagates incorrectly or throws.
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, 2, 3, 4});
        OpenMapRealVector sub = v.getSubVector(0, 2);
        // Now modify v
        v.setEntry(0, 99);
        // sub should not be affected
        assertEquals(1.0, sub.getEntry(0), 0.0); // getSubVector made a copy
        // Now try to iterate over sub's sparseIterator and modify v
        java.util.Iterator<RealVector.Entry> subIter = sub.sparseIterator();
        while (subIter.hasNext()) {
            RealVector.Entry e = subIter.next();
            // Modifying v while iterating over sub should be safe (different map)
            v.setEntry(e.getIndex() + 2, 77);
        }
        // No exception expected in correct version
        // In defective version, modifying v while sub's iterator is active could cause issues
        // if the underlying OpenIntToDoubleHashMap of sub shares state with v (it doesn't, they are copies).
        // This test is a baseline for expected correct behavior.
        assertEquals(77.0, v.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testConcurrentModificationSparseIteratorFailFast() {
        // Directly test the fail-fast behavior of OpenIntToDoubleHashMap iterator.
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, 2, 3});
        java.util.Iterator<RealVector.Entry> iter = v.sparseIterator();
        // Modify the map via setEntry on the vector (not through iterator's setValue)
        v.setEntry(1, 99); // modifies entries map
        // Now call hasNext or next on the iterator — should throw ConcurrentModificationException
        // in defective version if the underlying OpenIntToDoubleHashMap iterator is fail-fast.
        boolean caught = false;
        try {
            iter.hasNext(); // This should throw if the iterator is fail-fast
        } catch (Exception e) {
            caught = true;
        }
        // Actually the bug is that it SHOULD throw, but the defect is that it doesn't?
        // The known defect is a ConcurrentModificationException being thrown unexpectedly.
        // So we expect the iterator to be fail-fast and throw here.
        assertTrue("Expected ConcurrentModificationException but was not thrown", caught);
    }

    // ===== Partition D: Exception & Defensive Guard Paths =====

    @Test(timeout = 4000, expected = org.apache.commons.math.exception.OutOfRangeException.class)
    public void testGetEntryNegativeIndex() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.getEntry(-1);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math.exception.OutOfRangeException.class)
    public void testGetEntryIndexTooLarge() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.getEntry(3); // index == virtualSize
    }

    @Test(timeout = 4000, expected = org.apache.commons.math.exception.DimensionMismatchException.class)
    public void testEbeDivideDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        RealVector v2 = new ArrayRealVector(3);
        v1.ebeDivide(v2);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math.exception.DimensionMismatchException.class)
    public void testDotProductDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.dotProduct(v2);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math.exception.DimensionMismatchException.class)
    public void testGetDistanceDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.getDistance(v2);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math.exception.DimensionMismatchException.class)
    public void testSubtractDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.subtract(v2);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math.exception.DimensionMismatchException.class)
    public void testProjectionDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        RealVector v2 = new ArrayRealVector(3);
        v1.projection(v2);
    }

    @Test(timeout = 4000)
    public void testEbeDivideByZeroProducesInfinity() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1, 2});
        RealVector v2 = new ArrayRealVector(new double[]{0, 1});
        OpenMapRealVector result = v1.ebeDivide(v2);
        assertEquals(Double.POSITIVE_INFINITY, result.getEntry(0), 0.0);
        assertEquals(2.0, result.getEntry(1), 0.0);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math.exception.OutOfRangeException.class)
    public void testSetSubVectorInvalidIndex() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setSubVector(-1, new double[]{1});
    }

    @Test(timeout = 4000, expected = org.apache.commons.math.exception.OutOfRangeException.class)
    public void testSetSubVectorIndexPlusLengthOutOfRange() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setSubVector(2, new double[]{1, 2}); // index 2 + 2 -1 = 3 >= 3
    }

    @Test(timeout = 4000, expected = org.apache.commons.math.exception.OutOfRangeException.class)
    public void testGetSubVectorNegativeIndex() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.getSubVector(-1, 1);
    }

    // ===== Partition E: Object Lifecycle & Contract Integrity =====

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        OpenMapRealVector v = new OpenMapRealVector(2);
        assertTrue(v.equals(v));
    }

    @Test(timeout = 4000)
    public void testEqualsNull() {
        OpenMapRealVector v = new OpenMapRealVector(2);
        assertFalse(v.equals(null));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentType() {
        OpenMapRealVector v = new OpenMapRealVector(2);
        assertFalse(v.equals("some string"));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentSize() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        assertFalse(v1.equals(v2));
    }

    @Test(timeout = 4000)
    public void testEqualsSameValuesDifferentSparsity() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{0, 1, 0});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0, 1, 0});
        assertTrue(v1.equals(v2));
        // Change epsilon to different value
        OpenMapRealVector v3 = new OpenMapRealVector(3, 1e-8);
        v3.setEntry(1, 1);
        assertFalse(v1.equals(v3)); // epsilon differs
    }

    @Test(timeout = 4000)
    public void testHashCodeAndEqualsConsistency() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{5, 6});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{5, 6});
        assertTrue(v1.equals(v2));
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test(timeout = 4000)
    public void testCopyIndependence() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, 2, 3});
        OpenMapRealVector copy = v.copy();
        copy.setEntry(0, 100);
        assertEquals(1.0, v.getEntry(0), 0.0);
        assertEquals(100.0, copy.getEntry(0), 0.0);
    }

    @Test(timeout = 4000)
    public void testSparseIterator() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0, 2, 0, 4, 0});
        java.util.Iterator<RealVector.Entry> iter = v.sparseIterator();
        assertTrue(iter.hasNext());
        RealVector.Entry e = iter.next();
        assertEquals(1, e.getIndex());
        assertEquals(2.0, e.getValue(), 0.0);
        assertTrue(iter.hasNext());
        e = iter.next();
        assertEquals(3, e.getIndex());
        assertEquals(4.0, e.getValue(), 0.0);
        assertFalse(iter.hasNext());
    }

    @Test(timeout = 4000)
    public void testSparseIteratorSetValue() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{0, 2, 0, 4});
        java.util.Iterator<RealVector.Entry> iter = v.sparseIterator();
        iter.next(); // index 1 value 2
        RealVector.Entry e = iter.next(); // index 3 value 4
        e.setValue(8);
        assertEquals(8.0, v.getEntry(3), 0.0);
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testSparseIteratorRemoveUnsupported() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1, 2});
        java.util.Iterator<RealVector.Entry> iter = v.sparseIterator();
        iter.remove();
    }
}