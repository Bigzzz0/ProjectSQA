package org.apache.commons.math3.linear;

import org.junit.Test;
import static org.junit.Assert.*;

import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.util.OpenIntToDoubleHashMap;

/**
 * DeepSeek test class for OpenMapRealVector.
 *
 * [Branch & Defect Analysis Matrix]
 *
 * Target branches and boundary conditions:
 *  - Partition A: Core functional logic (add, subtract, dotProduct, ebeDivide, ebeMultiply, etc.)
 *  - Partition B: Boundary value analysis (zero tolerance, empty arrays, zero dimension, negative indices, null arguments)
 *  - Partition C: Defect-targeted tests for ebeDivide (0/0 => NaN) and ebeMultiply (0*Inf => NaN, 0*NaN => NaN)
 *  - Partition D: Exception paths (index out of bounds, negative subvector length, zero norm unitize)
 *  - Partition E: Object contract (equals, hashCode, copy, getSparsity, sparseIterator)
 *
 * Known defects (Defects4J):
 *   - ebeDivide: when both entries are 0.0, expected NaN but got 0.0
 *   - ebeMultiply: when this entry is 0.0 and other entry is Infinity/NaN, expected NaN but got 0.0
 *
 * All tests are deterministic, use only JUnit 4 assertions, and have a 4000 ms timeout.
 */
public class OpenMapRealVectorDeepseekTest {

    // ----------------------------------------------------------------
    // Partition A: Core Functional Logic & State Transitions
    // ----------------------------------------------------------------

    @Test(timeout = 4000)
    public void testAddOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        v1.setEntry(2, 3.0);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v2.setEntry(1, 2.0);
        v2.setEntry(2, -1.0);
        OpenMapRealVector sum = v1.add(v2);
        assertEquals(1.0, sum.getEntry(0), 0.0);
        assertEquals(2.0, sum.getEntry(1), 0.0);
        assertEquals(2.0, sum.getEntry(2), 0.0);
        assertEquals(3, sum.getDimension());
        assertTrue(sum.getEntry(2) != 0.0);
    }

    @Test(timeout = 4000)
    public void testAddGenericRealVector() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 2.0);
        v1.setEntry(2, 4.0);
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1.0, 0.0, -2.0});
        RealVector sum = v1.add((RealVector) v2);
        assertEquals(3.0, sum.getEntry(0), 0.0);
        assertEquals(0.0, sum.getEntry(1), 0.0);
        assertEquals(2.0, sum.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testSubtractOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(4);
        v1.setEntry(0, 5.0);
        v1.setEntry(2, 3.0);
        OpenMapRealVector v2 = new OpenMapRealVector(4);
        v2.setEntry(0, 2.0);
        v2.setEntry(1, 4.0);
        v2.setEntry(3, 1.0);
        OpenMapRealVector diff = v1.subtract(v2);
        assertEquals(3.0, diff.getEntry(0), 0.0);
        assertEquals(-4.0, diff.getEntry(1), 0.0);
        assertEquals(3.0, diff.getEntry(2), 0.0);
        assertEquals(-1.0, diff.getEntry(3), 0.0);
    }

    @Test(timeout = 4000)
    public void testSubtractGeneric() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        v1.setEntry(0, 10.0);
        ArrayRealVector v2 = new ArrayRealVector(new double[]{0.0, 5.0});
        RealVector diff = v1.subtract((RealVector) v2);
        assertEquals(10.0, diff.getEntry(0), 0.0);
        assertEquals(-5.0, diff.getEntry(1), 0.0);
    }

    @Test(timeout = 4000)
    public void testDotProductOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(5);
        v1.setEntry(0, 2.0);
        v1.setEntry(3, 4.0);
        OpenMapRealVector v2 = new OpenMapRealVector(5);
        v2.setEntry(0, 3.0);
        v2.setEntry(2, 1.0);
        v2.setEntry(3, -1.0);
        double dot = v1.dotProduct(v2);
        assertEquals(2.0 * 3.0 + 4.0 * (-1.0), dot, 0.0);
    }

    @Test(timeout = 4000)
    public void testDotProductGeneric() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 1.0);
        v1.setEntry(2, 2.0);
        ArrayRealVector v2 = new ArrayRealVector(new double[]{0.0, 0.0, 1.0});
        double dot = v1.dotProduct((RealVector) v2);
        assertEquals(2.0, dot, 0.0);
    }

    @Test(timeout = 4000)
    public void testAppendDouble() {
        OpenMapRealVector v = new OpenMapRealVector(2);
        v.setEntry(0, 1.0);
        v.setEntry(1, 2.0);
        OpenMapRealVector appended = v.append(3.0);
        assertEquals(3, appended.getDimension());
        assertEquals(1.0, appended.getEntry(0), 0.0);
        assertEquals(2.0, appended.getEntry(1), 0.0);
        assertEquals(3.0, appended.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testAppendOpenMapVector() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        v1.setEntry(0, 1.0);
        OpenMapRealVector v2 = new OpenMapRealVector(2);
        v2.setEntry(1, 4.0);
        OpenMapRealVector appended = v1.append(v2);
        assertEquals(4, appended.getDimension());
        assertEquals(1.0, appended.getEntry(0), 0.0);
        assertEquals(0.0, appended.getEntry(1), 0.0);
        assertEquals(0.0, appended.getEntry(2), 0.0);
        assertEquals(4.0, appended.getEntry(3), 0.0);
    }

    @Test(timeout = 4000)
    public void testAppendGenericRealVector() {
        OpenMapRealVector v = new OpenMapRealVector(1);
        v.setEntry(0, 5.0);
        ArrayRealVector toAppend = new ArrayRealVector(new double[]{1.0, 2.0});
        OpenMapRealVector appended = v.append((RealVector) toAppend);
        assertEquals(3, appended.getDimension());
        assertEquals(5.0, appended.getEntry(0), 0.0);
        assertEquals(1.0, appended.getEntry(1), 0.0);
        assertEquals(2.0, appended.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testCopy() {
        OpenMapRealVector v = new OpenMapRealVector(4);
        v.setEntry(1, 3.0);
        OpenMapRealVector copy = v.copy();
        assertEquals(v.getDimension(), copy.getDimension());
        assertEquals(v.getEntry(1), copy.getEntry(1), 0.0);
        assertEquals(0.0, copy.getEntry(0), 0.0);
        // Modify copy does not affect original
        copy.setEntry(1, 10.0);
        assertEquals(3.0, v.getEntry(1), 0.0);
    }

    // ----------------------------------------------------------------
    // Partition B: Boundary Value Analysis & Extremes
    // ----------------------------------------------------------------

    @Test(timeout = 4000)
    public void testZeroLengthVector() {
        OpenMapRealVector v = new OpenMapRealVector();
        assertEquals(0, v.getDimension());
        assertEquals(0.0, v.getSparsity(), 0.0);
        assertEquals(0, v.toArray().length);
    }

    @Test(timeout = 4000)
    public void testLargeDimension() {
        int dim = 10000;
        OpenMapRealVector v = new OpenMapRealVector(dim);
        assertEquals(dim, v.getDimension());
        v.setEntry(dim - 1, 42.0);
        assertEquals(42.0, v.getEntry(dim - 1), 0.0);
        assertEquals(0.0, v.getEntry(0), 0.0);
    }

    @Test(timeout = 4000)
    public void testZeroToleranceBoundary() {
        OpenMapRealVector v = new OpenMapRealVector(3, 1e-10);
        v.setEntry(0, 0.5e-10);  // slightly below tolerance
        assertTrue(v.isDefaultValue(0.5e-10)); // 0.5e-10 < 1e-10 => true
        v.setEntry(1, 1.5e-10);
        assertFalse(v.isDefaultValue(1.5e-10));
    }

    @Test(timeout = 4000)
    public void testConstructorFromDoubleArrayWithNonZeroOnly() {
        double[] values = new double[]{0.0, 2.0, 0.0, 3.0};
        OpenMapRealVector v = new OpenMapRealVector(values, 1e-12);
        assertEquals(4, v.getDimension());
        assertEquals(0.0, v.getEntry(0), 0.0);
        assertEquals(2.0, v.getEntry(1), 0.0);
        assertEquals(0.0, v.getEntry(2), 0.0);
        assertEquals(3.0, v.getEntry(3), 0.0);
    }

    @Test(timeout = 4000)
    public void testConstructorFromDoubleArrayWithExpectedSize() {
        OpenMapRealVector v = new OpenMapRealVector(5, 3, 1e-8);
        assertEquals(5, v.getDimension());
        v.setEntry(2, 0.0); // should be ignored if within epsilon?
    }

    @Test(timeout = 4000)
    public void testCopyConstructor() {
        OpenMapRealVector original = new OpenMapRealVector(3);
        original.setEntry(0, 123.0);
        OpenMapRealVector copy = new OpenMapRealVector(original);
        assertEquals(123.0, copy.getEntry(0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGenericCopyConstructor() {
        ArrayRealVector arr = new ArrayRealVector(new double[]{0.0, 5.0, 0.0});
        OpenMapRealVector v = new OpenMapRealVector((RealVector) arr);
        assertEquals(3, v.getDimension());
        assertEquals(0.0, v.getEntry(0), 0.0);
        assertEquals(5.0, v.getEntry(1), 0.0);
    }

    @Test(timeout = 4000)
    public void testSetEntryRemoveWhenDefault() {
        OpenMapRealVector v = new OpenMapRealVector(5, 1e-12);
        v.setEntry(2, 10.0);
        assertTrue(v.getEntry(2) != 0.0);
        v.setEntry(2, 0.0);
        assertEquals(0.0, v.getEntry(2), 0.0);
        // After setting to zero, the entry should be removed from the map.
        // This can be checked indirectly: dotProduct with a vector that has non-zero at that index should not include this term.
        v.setEntry(2, 0.0);
        OpenMapRealVector other = new OpenMapRealVector(5);
        other.setEntry(2, 1.0);
        assertEquals(0.0, v.dotProduct(other), 0.0);
    }

    // ----------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (ebeDivide and ebeMultiply)
    // ----------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEbeDivideBothZero() {
        // Defect: when both this[i] and v[i] are 0.0, expected NaN but got 0.0
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 0.0); // not stored due to default tolerance; must set explicitly with high tolerance?
        // The default tolerance is 1e-12. Setting to 0.0 will cause entry to be removed.
        // So we need a vector where entry is exactly 0.0 but still stored? Actually the setEntry method removes if value is default.
        // To get a zero entry that is in the map, we can construct with a tolerance that is so high that 0.0 is not default,
        // but then the value 0.0 is not considered default. Alternatively, we can use OpenMapRealVector with a negative epsilon (unusual) or
        // directly manipulate entries via reflection? Not allowed. Instead, we can use a vector constructed from an array where 0.0 is explicitly stored
        // because the constructor uses isDefaultValue check. If we use epsilon = -1 (negative), then abs(value) < epsilon is false for any value,
        // so 0.0 will be stored. But that's not typical. However, the defect is triggered by ebeDivide where this[i] is not in map (i.e., default 0)
        // and v[i] is also 0.0 (as a non-zero entry? Actually v.getEntry(i) returns 0.0 even if not stored).
        // The ebeDivide method iterates over entries of this. If this[i] is not stored, it is skipped. So the result entry remains 0.0 (since res is a copy of this with entries).
        // But the correct result should be NaN because 0/0 = NaN.
        // To trigger the bug, we need v1 to have an entry that is stored as 0.0? Actually if v1 has no entry at index i, then the loop over v1.entries will not touch that index,
        // so res will still have its original value from copy, which is 0 (since copy copies entries, but if v1 has no entry, copy also has no entry, so res.getEntry(i) returns 0.0).
        // And v.getEntry(i) for a RealVector that has no entry at i also returns 0.0. So the result is 0.0 instead of NaN.
        // So we need a scenario where both this[i] and v[i] are 0.0. Since v is a generic RealVector (ArrayRealVector), not OpenMap, the ebeDivide method iterates over this.entries only.
        // We need this to have no entry at index i, but v to have 0.0 stored explicitly? Actually ArrayRealVector stores all entries, so v.getEntry(i) returns 0.0.
        // So creating an OpenMapRealVector that has no entry at some index, and an ArrayRealVector with 0.0 at that index will demonstrate the bug.
        // Let's create:
        OpenMapRealVector v1 = new OpenMapRealVector(5);
        // v1 has no entries (all zero)
        ArrayRealVector v2 = new ArrayRealVector(new double[]{0.0, 0.0, 0.0, 0.0, 0.0});
        RealVector result = v1.ebeDivide((RealVector) v2);
        // At each index, 0.0/0.0 should be NaN
        for (int i = 0; i < 5; i++) {
            assertEquals("entry #" + i, Double.NaN, result.getEntry(i), 0.0);
        }
    }

    @Test(timeout = 4000)
    public void testEbeDivideOpenMapBothZero() {
        // Same test but with both OpenMapRealVectors
        OpenMapRealVector v1 = new OpenMapRealVector(4);
        OpenMapRealVector v2 = new OpenMapRealVector(4);
        // Both have no entries, so all imaginary zeros
        OpenMapRealVector result = v1.ebeDivide((RealVector) v2);
        for (int i = 0; i < 4; i++) {
            assertEquals(Double.NaN, result.getEntry(i), 0.0);
        }
    }

    @Test(timeout = 4000)
    public void testEbeMultiplyZeroTimesInfinity() {
        // Defect: when this[i] is 0 and v[i] is Infinity (or -Infinity or NaN), result should be NaN.
        // Again, we need OpenMapRealVector with no entry at index, and ArrayRealVector with Infinity.
        OpenMapRealVector v1 = new OpenMapRealVector(5);
        double[] vals = {Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN, 1.0, 0.0};
        ArrayRealVector v2 = new ArrayRealVector(vals);
        RealVector result = v1.ebeMultiply((RealVector) v2);
        // For indices 0,1,2: 0.0 * Inf/NaN should be NaN
        assertEquals(Double.NaN, result.getEntry(0), 0.0);
        assertEquals(Double.NaN, result.getEntry(1), 0.0);
        assertEquals(Double.NaN, result.getEntry(2), 0.0);
        // For index 3: 0.0 * 1.0 = 0.0
        assertEquals(0.0, result.getEntry(3), 0.0);
        // For index 4: 0.0 * 0.0 = 0.0
        assertEquals(0.0, result.getEntry(4), 0.0);
    }

    @Test(timeout = 4000)
    public void testEbeMultiplyOpenMapZeroTimesInfinity() {
        // Both OpenMapRealVector: v1 has no entries, v2 has Inf at some index
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v2.setEntry(1, Double.POSITIVE_INFINITY);
        OpenMapRealVector result = v1.ebeMultiply((RealVector) v2);
        assertEquals(Double.NaN, result.getEntry(1), 0.0);
        assertEquals(0.0, result.getEntry(0), 0.0);
        assertEquals(0.0, result.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testEbeDivideMixedTypesNonZero() {
        // Additional case: non-zero entries are divided correctly
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 10.0);
        v1.setEntry(2, 6.0);
        ArrayRealVector v2 = new ArrayRealVector(new double[]{2.0, 1.0, 3.0});
        RealVector result = v1.ebeDivide((RealVector) v2);
        assertEquals(5.0, result.getEntry(0), 1e-12);
        assertEquals(0.0, result.getEntry(1), 0.0); // 0.0/1.0 = 0.0
        assertEquals(2.0, result.getEntry(2), 1e-12);
    }

    // ----------------------------------------------------------------
    // Partition D: Exception & Defensive Guard Paths
    // ----------------------------------------------------------------

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetEntryNegativeIndex() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.getEntry(-1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetEntryOutOfRange() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.getEntry(3);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testSetEntryNegativeIndex() {
        OpenMapRealVector v = new OpenMapRealVector(2);
        v.setEntry(-1, 1.0);
    }

    @Test(timeout = 4000, expected = NotPositiveException.class)
    public void testGetSubVectorNegativeN() {
        OpenMapRealVector v = new OpenMapRealVector(10);
        v.getSubVector(0, -1);
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testGetSubVectorInvalidIndex() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.getSubVector(4, 2); // index 4 + 2 -1 = 5, out of bounds
    }

    @Test(timeout = 4000, expected = IndexOutOfBoundsException.class)
    public void testSetSubVectorInvalidIndex() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.setSubVector(4, new ArrayRealVector(new double[]{1.0, 2.0}));
    }

    @Test(timeout = 4000, expected = MathArithmeticException.class)
    public void testUnitizeZeroNorm() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.unitize();
    }

    @Test(timeout = 4000, expected = MathArithmeticException.class)
    public void testUnitVectorZeroNorm() {
        OpenMapRealVector v = new OpenMapRealVector(4);
        v.unitVector();
    }

    @Test(timeout = 4000)
    public void testGetSubVectorNormal() {
        OpenMapRealVector v = new OpenMapRealVector(10);
        v.setEntry(2, 3.0);
        v.setEntry(5, 7.0);
        OpenMapRealVector sub = v.getSubVector(2, 4);
        assertEquals(4, sub.getDimension());
        assertEquals(3.0, sub.getEntry(0), 0.0);
        assertEquals(0.0, sub.getEntry(1), 0.0);
        assertEquals(0.0, sub.getEntry(2), 0.0);
        assertEquals(7.0, sub.getEntry(3), 0.0);
    }

    // ----------------------------------------------------------------
    // Partition E: Object Lifecycle & Contract Integrity
    // ----------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEqualsSameObject() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        assertTrue(v.equals(v));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentClass() {
        OpenMapRealVector v = new OpenMapRealVector(2);
        assertFalse(v.equals("string"));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentDimension() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        assertFalse(v1.equals(v2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentEpsilon() {
        OpenMapRealVector v1 = new OpenMapRealVector(2, 1e-12);
        OpenMapRealVector v2 = new OpenMapRealVector(2, 1e-10);
        assertFalse(v1.equals(v2));
    }

    @Test(timeout = 4000)
    public void testEqualsDifferentEntries() {
        OpenMapRealVector v1 = new OpenMapRealVector(4);
        v1.setEntry(0, 1.0);
        OpenMapRealVector v2 = new OpenMapRealVector(4);
        v2.setEntry(0, 2.0);
        assertFalse(v1.equals(v2));
    }

    @Test(timeout = 4000)
    public void testEqualsEqualVectors() {
        OpenMapRealVector v1 = new OpenMapRealVector(5);
        v1.setEntry(1, 3.0);
        v1.setEntry(3, 4.5);
        OpenMapRealVector v2 = new OpenMapRealVector(5);
        v2.setEntry(1, 3.0);
        v2.setEntry(3, 4.5);
        assertTrue(v1.equals(v2));
        assertEquals(v1.hashCode(), v2.hashCode());
    }

    @Test(timeout = 4000)
    public void testHashCodeConsistency() {
        OpenMapRealVector v = new OpenMapRealVector(2);
        v.setEntry(0, 1.0);
        int hash1 = v.hashCode();
        int hash2 = v.hashCode();
        assertEquals(hash1, hash2);
    }

    @Test(timeout = 4000)
    public void testToArray() {
        OpenMapRealVector v = new OpenMapRealVector(4);
        v.setEntry(1, 2.0);
        v.setEntry(3, -1.0);
        double[] arr = v.toArray();
        assertArrayEquals(new double[]{0.0, 2.0, 0.0, -1.0}, arr, 0.0);
    }

    @Test(timeout = 4000)
    public void testSet() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.set(3.0);
        for (int i = 0; i < 5; i++) {
            assertEquals(3.0, v.getEntry(i), 0.0);
        }
    }

    @Test(timeout = 4000)
    public void testSparsity() {
        OpenMapRealVector v = new OpenMapRealVector(10);
        v.setEntry(0, 1.0);
        v.setEntry(5, 2.0);
        assertEquals(0.2, v.getSparsity(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testIsNaN() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        assertFalse(v.isNaN());
        v.setEntry(1, Double.NaN);
        assertTrue(v.isNaN());
    }

    @Test(timeout = 4000)
    public void testIsInfinite() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        assertFalse(v.isInfinite());
        v.setEntry(0, Double.POSITIVE_INFINITY);
        assertTrue(v.isInfinite());
        v.setEntry(1, Double.NaN);
        assertFalse(v.isInfinite()); // NaN makes it not infinite
    }

    @Test(timeout = 4000)
    public void testIsInfiniteWithNaNThenInf() {
        OpenMapRealVector v = new OpenMapRealVector(2);
        v.setEntry(0, Double.NaN);
        v.setEntry(1, Double.POSITIVE_INFINITY);
        assertFalse(v.isInfinite()); // because NaN entry sets infiniteFound false and returns early
    }

    @Test(timeout = 4000)
    public void testProjection() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 2.0);
        v1.setEntry(2, 1.0);
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1.0, 0.0, 0.0});
        RealVector proj = v1.projection((RealVector) v2);
        // projection of v1 onto x-axis: (2.0,0,1.0) onto (1,0,0) => (2.0,0,0)
        assertEquals(2.0, proj.getEntry(0), 1e-12);
        assertEquals(0.0, proj.getEntry(1), 0.0);
        assertEquals(0.0, proj.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testMapAddToSelf() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(0, 1.0);
        v.setEntry(2, 2.0);
        v.mapAddToSelf(10.0);
        assertEquals(11.0, v.getEntry(0), 0.0);
        assertEquals(10.0, v.getEntry(1), 0.0);
        assertEquals(12.0, v.getEntry(2), 0.0);
    }

    @Test(timeout = 4000)
    public void testMapAdd() {
        OpenMapRealVector v = new OpenMapRealVector(2);
        v.setEntry(0, 3.0);
        OpenMapRealVector result = v.mapAdd(5.0);
        assertEquals(8.0, result.getEntry(0), 0.0);
        assertEquals(5.0, result.getEntry(1), 0.0);
        // original unchanged
        assertEquals(3.0, v.getEntry(0), 0.0);
    }

    @Test(timeout = 4000)
    public void testGetL1DistanceOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(4);
        v1.setEntry(0, 1.0);
        v1.setEntry(2, -2.0);
        OpenMapRealVector v2 = new OpenMapRealVector(4);
        v2.setEntry(0, 4.0);
        v2.setEntry(3, 5.0);
        double dist = v1.getL1Distance(v2);
        // |1-4| =3, |0-0|=0, |-2-0|=2, |0-5|=5 => total 10
        assertEquals(10.0, dist, 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetL1DistanceGeneric() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 2.0);
        ArrayRealVector v2 = new ArrayRealVector(new double[]{0.0, -3.0, 4.0});
        double dist = v1.getL1Distance((RealVector) v2);
        assertEquals(9.0, dist, 1e-12); // |2-0| + |0-(-3)| + |0-4| = 2+3+4=9
    }

    @Test(timeout = 4000)
    public void testGetLInfDistanceOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(4);
        v1.setEntry(0, 10.0);
        v1.setEntry(2, 5.0);
        OpenMapRealVector v2 = new OpenMapRealVector(4);
        v2.setEntry(0, 2.0);
        v2.setEntry(1, 8.0);
        double dist = v1.getLInfDistance(v2);
        // max(|10-2|, |0-8|, |5-0|, |0-0|) = max(8,8,5,0) = 8
        assertEquals(8.0, dist, 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetLInfDistanceGeneric() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, -1.0);
        ArrayRealVector v2 = new ArrayRealVector(new double[]{3.0, 0.0, 0.0});
        double dist = v1.getLInfDistance((RealVector) v2);
        assertEquals(4.0, dist, 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetDistanceOpenMap() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.setEntry(0, 3.0);
        v1.setEntry(2, 4.0);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v2.setEntry(0, 0.0);
        v2.setEntry(1, 1.0);
        double dist = v1.getDistance(v2);
        double expected = Math.sqrt(9 + 1 + 16); // 3^2 + (0-1)^2? Actually (3-0)^2 + (0-1)^2 + (4-0)^2 = 9+1+16=26
        assertEquals(Math.sqrt(26), dist, 1e-12);
    }

    @Test(timeout = 4000)
    public void testGetDistanceGeneric() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        v1.setEntry(1, 2.0);
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1.0, 0.0});
        double dist = v1.getDistance((RealVector) v2);
        assertEquals(Math.sqrt(1 + 4), dist, 1e-12); // (0-1)^2 + (2-0)^2 =1+4=5
    }

    @Test(timeout = 4000)
    public void testSparseIterator() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.setEntry(1, 3.0);
        v.setEntry(3, 7.0);
        java.util.Iterator<RealVector.Entry> it = v.sparseIterator();
        assertTrue(it.hasNext());
        RealVector.Entry e = it.next();
        assertEquals(1, e.getIndex());
        assertEquals(3.0, e.getValue(), 0.0);
        assertTrue(it.hasNext());
        e = it.next();
        assertEquals(3, e.getIndex());
        assertEquals(7.0, e.getValue(), 0.0);
        assertFalse(it.hasNext());
    }

    @Test(timeout = 4000, expected = UnsupportedOperationException.class)
    public void testSparseIteratorRemoveNotSupported() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(0, 1.0);
        java.util.Iterator<RealVector.Entry> it = v.sparseIterator();
        it.next();
        it.remove();
    }

    // ----------------------------------------------------------------
    // Additional helper tests: dimension mismatch exceptions
    // ----------------------------------------------------------------

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.DimensionMismatchException.class)
    public void testAddDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        OpenMapRealVector v2 = new OpenMapRealVector(4);
        v1.add(v2);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.DimensionMismatchException.class)
    public void testDotDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1.0, 2.0, 3.0});
        v1.dotProduct((RealVector) v2);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.DimensionMismatchException.class)
    public void testEbeDivideDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(1);
        ArrayRealVector v2 = new ArrayRealVector(new double[]{0.0, 0.0});
        v1.ebeDivide((RealVector) v2);
    }

    @Test(timeout = 4000, expected = org.apache.commons.math3.exception.DimensionMismatchException.class)
    public void testEbeMultiplyDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        ArrayRealVector v2 = new ArrayRealVector(new double[]{1.0});
        v1.ebeMultiply((RealVector) v2);
    }
}