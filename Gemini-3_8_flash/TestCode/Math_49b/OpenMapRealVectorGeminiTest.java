package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;
import org.apache.commons.math.exception.MathArithmeticException;
import org.apache.commons.math.exception.DimensionMismatchException;
import org.apache.commons.math.exception.OutOfRangeException;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: OpenMapRealVector and nested OpenMapSparseIterator / OpenMapEntry.
 *
 * Key Defects & Edge Conditions Targeted:
 * 1. DEFECT SparseRealVectorTest::testConcurrentModification:
 *    - In ebeMultiply(RealVector/double[]) and ebeDivide(RealVector/double[]), iterating over res.entries
 *      while modifying res via setEntry(...) removes entries if the result is 0.0 (or within epsilon),
 *      causing OpenIntToDoubleHashMap to throw ConcurrentModificationException (MathRuntimeException$6).
 * 2. Add / Subtract / DotProduct branches:
 *    - copyThis condition: entries.size() > v.entries.size() vs entries.size() <= v.entries.size().
 *    - Overlapping vs disjoint sparsity patterns in add, subtract, and dotProduct.
 * 3. Distance branches:
 *    - L1, L2, and LInf distance calculations where entries exist in 'this' only, 'v' only, or both.
 * 4. Boundary and Special Double Values:
 *    - isNaN() with positive/negative NaN.
 *    - isInfinite() with infinity, NaN combinations.
 *    - zero norm handling in unitize() throwing MathArithmeticException.
 * 5. Equality and HashCode:
 *    - Comparison with self, null, different types, different dimension, different epsilon.
 *    - Exact bitwise equality of entries across differing sparsity layouts.
 * 6. SparseIterator & Entry:
 *    - Iteration, Entry.getIndex(), Entry.getValue(), Entry.setValue(), and UnsupportedOperationException on remove().
 */
public class OpenMapRealVectorGeminiTest {

    private static final double EPS = 1e-12;

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Concurrent Modification Defect)
    // =========================================================================

    /**
     * Targets Defects4J bug where ebeMultiply modifies entries while iterating over res.entries.
     * When an element becomes 0 (isDefaultValue), res.setEntry removes the entry, altering map count.
     */
    @Test(timeout = 4000)
    public void testConcurrentModificationOnEbeMultiplyRealVector() {
        double[] uData = new double[] { 2.0, 3.0, 4.0 };
        double[] vData = new double[] { 0.0, 5.0, 0.0 }; // Causes index 0 and 2 to become zero
        OpenMapRealVector u = new OpenMapRealVector(uData);
        OpenMapRealVector v = new OpenMapRealVector(vData);

        RealVector result = u.ebeMultiply(v);

        assertEquals(0.0, result.getEntry(0), EPS);
        assertEquals(15.0, result.getEntry(1), EPS);
        assertEquals(0.0, result.getEntry(2), EPS);
    }

    /**
     * Targets Defects4J bug where ebeMultiply(double[]) modifies entries while iterating over res.entries.
     */
    @Test(timeout = 4000)
    public void testConcurrentModificationOnEbeMultiplyDoubleArray() {
        double[] uData = new double[] { 7.0, 8.0 };
        double[] vData = new double[] { 0.0, 2.0 };
        OpenMapRealVector u = new OpenMapRealVector(uData);

        OpenMapRealVector result = u.ebeMultiply(vData);

        assertEquals(0.0, result.getEntry(0), EPS);
        assertEquals(16.0, result.getEntry(1), EPS);
    }

    /**
     * Targets Defects4J bug in ebeDivide(RealVector) when setting entries to 0 or altering backing map.
     */
    @Test(timeout = 4000)
    public void testConcurrentModificationOnEbeDivideRealVector() {
        double[] uData = new double[] { 10.0, 20.0 };
        OpenMapRealVector u = new OpenMapRealVector(uData);
        // v has non-zero entries
        RealVector v = new ArrayRealVector(new double[] { 1e15, 2.0 });

        // 10.0 / 1e15 is 1e-14 < 1e-12 (EPS), triggering removal in setEntry
        RealVector result = u.ebeDivide(v);

        assertEquals(0.0, result.getEntry(0), EPS);
        assertEquals(10.0, result.getEntry(1), EPS);
    }

    /**
     * Targets Defects4J bug in ebeDivide(double[]) when setting entries to zero.
     */
    @Test(timeout = 4000)
    public void testConcurrentModificationOnEbeDivideDoubleArray() {
        double[] uData = new double[] { 5.0, 15.0 };
        OpenMapRealVector u = new OpenMapRealVector(uData);
        double[] vData = new double[] { 1e20, 3.0 };

        OpenMapRealVector result = u.ebeDivide(vData);

        assertEquals(0.0, result.getEntry(0), EPS);
        assertEquals(5.0, result.getEntry(1), EPS);
    }

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndDimension() {
        OpenMapRealVector v0 = new OpenMapRealVector();
        assertEquals(0, v0.getDimension());

        OpenMapRealVector v1 = new OpenMapRealVector(5);
        assertEquals(5, v1.getDimension());

        OpenMapRealVector v2 = new OpenMapRealVector(7, 1e-5);
        assertEquals(7, v2.getDimension());

        OpenMapRealVector v3 = new OpenMapRealVector(10, 3);
        assertEquals(10, v3.getDimension());

        OpenMapRealVector v4 = new OpenMapRealVector(10, 3, 1e-4);
        assertEquals(10, v4.getDimension());

        Double[] objData = new Double[] { 1.0, 0.0, 2.5 };
        OpenMapRealVector v5 = new OpenMapRealVector(objData);
        assertEquals(3, v5.getDimension());
        assertEquals(1.0, v5.getEntry(0), EPS);
        assertEquals(0.0, v5.getEntry(1), EPS);
        assertEquals(2.5, v5.getEntry(2), EPS);

        OpenMapRealVector v6 = new OpenMapRealVector(objData, 1e-3);
        assertEquals(3, v6.getDimension());

        OpenMapRealVector copyOfV5 = new OpenMapRealVector(v5);
        assertEquals(v5.getDimension(), copyOfV5.getDimension());
        assertEquals(v5.getEntry(2), copyOfV5.getEntry(2), EPS);

        RealVector genericVector = new ArrayRealVector(new double[] { 0.0, 9.0, 0.0 });
        OpenMapRealVector fromGeneric = new OpenMapRealVector(genericVector);
        assertEquals(3, fromGeneric.getDimension());
        assertEquals(9.0, fromGeneric.getEntry(1), EPS);
    }

    @Test(timeout = 4000)
    public void testGetAndSetEntry() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(0, 5.0);
        v.setEntry(1, 0.0);
        v.setEntry(2, 7.0);

        assertEquals(5.0, v.getEntry(0), EPS);
        assertEquals(0.0, v.getEntry(1), EPS);
        assertEquals(7.0, v.getEntry(2), EPS);

        // Overwrite non-zero with default (zero) to remove it
        v.setEntry(0, 0.0);
        assertEquals(0.0, v.getEntry(0), EPS);

        // Setting default on an already zero index
        v.setEntry(1, 0.0);
        assertEquals(0.0, v.getEntry(1), EPS);
    }

    @Test(timeout = 4000)
    public void testAddBranches() {
        // Branch 1: this.size > v.size
        OpenMapRealVector v1 = new OpenMapRealVector(new double[] { 1.0, 2.0, 3.0 });
        OpenMapRealVector v2 = new OpenMapRealVector(new double[] { 0.0, 5.0, 0.0 });
        OpenMapRealVector res1 = v1.add(v2);
        assertArrayEquals(new double[] { 1.0, 7.0, 3.0 }, res1.getData(), EPS);

        // Branch 2: this.size <= v.size
        OpenMapRealVector res2 = v2.add(v1);
        assertArrayEquals(new double[] { 1.0, 7.0, 3.0 }, res2.getData(), EPS);

        // RealVector general add (v is OpenMapRealVector)
        RealVector res3 = v1.add((RealVector) v2);
        assertTrue(res3 instanceof OpenMapRealVector);
        assertArrayEquals(new double[] { 1.0, 7.0, 3.0 }, res3.getData(), EPS);

        // RealVector general add (v is ArrayRealVector)
        RealVector genericV = new ArrayRealVector(new double[] { 1.0, 1.0, 1.0 });
        RealVector res4 = v1.add(genericV);
        assertArrayEquals(new double[] { 2.0, 3.0, 4.0 }, res4.getData(), EPS);
    }

    @Test(timeout = 4000)
    public void testSubtractBranches() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[] { 5.0, 0.0, 8.0 });
        OpenMapRealVector v2 = new OpenMapRealVector(new double[] { 2.0, 3.0, 0.0 });

        // Branch in subtract(OpenMapRealVector): key in entries vs not in entries
        OpenMapRealVector res = v1.subtract(v2);
        assertArrayEquals(new double[] { 3.0, -3.0, 8.0 }, res.getData(), EPS);

        // subtract(RealVector) dispatch
        OpenMapRealVector resGeneric = v1.subtract((RealVector) v2);
        assertArrayEquals(new double[] { 3.0, -3.0, 8.0 }, resGeneric.getData(), EPS);

        RealVector arrayVec = new ArrayRealVector(new double[] { 1.0, 1.0, 1.0 });
        OpenMapRealVector resArrVec = v1.subtract(arrayVec);
        assertArrayEquals(new double[] { 4.0, -1.0, 7.0 }, resArrVec.getData(), EPS);

        // subtract(double[])
        OpenMapRealVector resArr = v1.subtract(new double[] { 2.0, 3.0, 1.0 });
        assertArrayEquals(new double[] { 3.0, -3.0, 7.0 }, resArr.getData(), EPS);
    }

    @Test(timeout = 4000)
    public void testDotProductBranches() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[] { 1.0, 0.0, 3.0 });
        OpenMapRealVector v2 = new OpenMapRealVector(new double[] { 0.0, 2.0, 4.0, 5.0 });
        v2 = new OpenMapRealVector(new double[] { 0.0, 2.0, 4.0 });

        // thisIsSmaller (v1 size: 2, v2 size: 2) -> false or true
        double dot1 = v1.dotProduct(v2);
        assertEquals(12.0, dot1, EPS);

        OpenMapRealVector vSingle = new OpenMapRealVector(new double[] { 0.0, 0.0, 5.0 });
        // vSingle size is 1, v1 size is 2 => thisIsSmaller == false
        double dot2 = v1.dotProduct(vSingle);
        assertEquals(15.0, dot2, EPS);

        // vSingle size 1, v1 size 2 => thisIsSmaller == true
        double dot3 = vSingle.dotProduct(v1);
        assertEquals(15.0, dot3, EPS);

        // dotProduct(RealVector)
        assertEquals(12.0, v1.dotProduct((RealVector) v2), EPS);
        ArrayRealVector dense = new ArrayRealVector(new double[] { 2.0, 3.0, 4.0 });
        assertEquals(14.0, v1.dotProduct((RealVector) dense), EPS);
    }

    @Test(timeout = 4000)
    public void testAppendMethods() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[] { 1.0, 2.0 });
        OpenMapRealVector v2 = new OpenMapRealVector(new double[] { 3.0 });

        OpenMapRealVector appended1 = v1.append(v2);
        assertArrayEquals(new double[] { 1.0, 2.0, 3.0 }, appended1.getData(), EPS);

        OpenMapRealVector appended2 = v1.append((RealVector) v2);
        assertArrayEquals(new double[] { 1.0, 2.0, 3.0 }, appended2.getData(), EPS);

        RealVector dense = new ArrayRealVector(new double[] { 4.0, 5.0 });
        OpenMapRealVector appendedDense = v1.append(dense);
        assertArrayEquals(new double[] { 1.0, 2.0, 4.0, 5.0 }, appendedDense.getData(), EPS);

        OpenMapRealVector appendedScalar = v1.append(9.0);
        assertArrayEquals(new double[] { 1.0, 2.0, 9.0 }, appendedScalar.getData(), EPS);

        OpenMapRealVector appendedArr = v1.append(new double[] { 7.0, 8.0 });
        assertArrayEquals(new double[] { 1.0, 2.0, 7.0, 8.0 }, appendedArr.getData(), EPS);
    }

    @Test(timeout = 4000)
    public void testSubVectorOperations() {
        OpenMapRealVector v = new OpenMapRealVector(new double[] { 1.0, 0.0, 3.0, 4.0, 0.0 });
        OpenMapRealVector sub = v.getSubVector(1, 3);
        assertEquals(3, sub.getDimension());
        assertArrayEquals(new double[] { 0.0, 3.0, 4.0 }, sub.getData(), EPS);

        // setSubVector
        OpenMapRealVector target = new OpenMapRealVector(5);
        target.setSubVector(1, new double[] { 8.0, 9.0 });
        assertArrayEquals(new double[] { 0.0, 8.0, 9.0, 0.0, 0.0 }, target.getData(), EPS);

        target.setSubVector(0, new ArrayRealVector(new double[] { 2.0, 3.0 }));
        assertArrayEquals(new double[] { 2.0, 3.0, 9.0, 0.0, 0.0 }, target.getData(), EPS);
    }

    @Test(timeout = 4000)
    public void testDistances() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[] { 1.0, 0.0, 3.0 });
        OpenMapRealVector v2 = new OpenMapRealVector(new double[] { 0.0, 4.0, 3.0 });

        // L2 Distance (OpenMapRealVector, RealVector, double[])
        double expectedL2 = FastMath.sqrt(1.0 + 16.0 + 0.0);
        assertEquals(expectedL2, v1.getDistance(v2), EPS);
        assertEquals(expectedL2, v1.getDistance((RealVector) v2), EPS);
        assertEquals(expectedL2, v1.getDistance(new double[] { 0.0, 4.0, 3.0 }), EPS);
        assertEquals(expectedL2, v1.getDistance((RealVector) new ArrayRealVector(new double[] { 0.0, 4.0, 3.0 })), EPS);

        // L1 Distance
        double expectedL1 = 1.0 + 4.0 + 0.0;
        assertEquals(expectedL1, v1.getL1Distance(v2), EPS);
        assertEquals(expectedL1, v1.getL1Distance((RealVector) v2), EPS);
        assertEquals(expectedL1, v1.getL1Distance(new double[] { 0.0, 4.0, 3.0 }), EPS);
        assertEquals(expectedL1, v1.getL1Distance((RealVector) new ArrayRealVector(new double[] { 0.0, 4.0, 3.0 })), EPS);

        // LInf Distance
        double expectedLInf = 4.0;
        assertEquals(expectedLInf, v1.getLInfDistance((RealVector) v2), EPS);
        assertEquals(expectedLInf, v1.getLInfDistance(new double[] { 0.0, 4.0, 3.0 }), EPS);
        assertEquals(expectedLInf, v1.getLInfDistance((RealVector) new ArrayRealVector(new double[] { 0.0, 4.0, 3.0 })), EPS);

        // LInf with iter.value() > max when !entries.containsKey(key)
        OpenMapRealVector v3 = new OpenMapRealVector(new double[] { 0.0, 0.0 });
        OpenMapRealVector v4 = new OpenMapRealVector(new double[] { 10.0, 20.0 });
        assertEquals(20.0, v3.getLInfDistance((RealVector) v4), EPS);
    }

    @Test(timeout = 4000)
    public void testMapAddAndSet() {
        OpenMapRealVector v = new OpenMapRealVector(new double[] { 1.0, 0.0, 3.0 });
        OpenMapRealVector mapped = v.mapAdd(2.0);
        assertArrayEquals(new double[] { 3.0, 2.0, 5.0 }, mapped.getData(), EPS);
        // Original remains unchanged
        assertArrayEquals(new double[] { 1.0, 0.0, 3.0 }, v.getData(), EPS);

        v.set(5.5);
        assertArrayEquals(new double[] { 5.5, 5.5, 5.5 }, v.getData(), EPS);
    }

    @Test(timeout = 4000)
    public void testOuterProductAndProjection() {
        OpenMapRealVector v = new OpenMapRealVector(new double[] { 2.0, 0.0 });
        RealMatrix matrix = v.outerProduct(new double[] { 3.0, 4.0 });
        assertEquals(2, matrix.getRowDimension());
        assertEquals(2, matrix.getColumnDimension());
        assertEquals(6.0, matrix.getEntry(0, 0), EPS);
        assertEquals(8.0, matrix.getEntry(0, 1), EPS);
        assertEquals(0.0, matrix.getEntry(1, 0), EPS);
        assertEquals(0.0, matrix.getEntry(1, 1), EPS);

        // Projection
        OpenMapRealVector a = new OpenMapRealVector(new double[] { 1.0, 2.0 });
        OpenMapRealVector b = new OpenMapRealVector(new double[] { 2.0, 0.0 });
        RealVector proj = a.projection(b);
        assertArrayEquals(new double[] { 1.0, 0.0 }, proj.getData(), EPS);

        OpenMapRealVector projArr = a.projection(new double[] { 2.0, 0.0 });
        assertArrayEquals(new double[] { 1.0, 0.0 }, projArr.getData(), EPS);
    }

    @Test(timeout = 4000)
    public void testUnitVectorAndUnitize() {
        OpenMapRealVector v = new OpenMapRealVector(new double[] { 3.0, 4.0 });
        OpenMapRealVector unit = v.unitVector();
        assertArrayEquals(new double[] { 0.6, 0.8 }, unit.getData(), EPS);

        v.unitize();
        assertArrayEquals(new double[] { 0.6, 0.8 }, v.getData(), EPS);
    }

    @Test(timeout = 4000)
    public void testSparsityAndToArray() {
        OpenMapRealVector v = new OpenMapRealVector(new double[] { 0.0, 2.0, 0.0, 4.0 });
        assertEquals(0.5, v.getSparsity(), EPS);
        assertArrayEquals(new double[] { 0.0, 2.0, 0.0, 4.0 }, v.toArray(), EPS);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsNaNAndIsInfinite() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        assertFalse(v.isNaN());
        assertFalse(v.isInfinite());

        v.setEntry(0, Double.NaN);
        assertTrue(v.isNaN());
        assertFalse(v.isInfinite());

        OpenMapRealVector vInf = new OpenMapRealVector(3);
        vInf.setEntry(1, Double.POSITIVE_INFINITY);
        assertFalse(vInf.isNaN());
        assertTrue(vInf.isInfinite());

        // NaN takes precedence over infinite in isInfinite()
        vInf.setEntry(2, Double.NaN);
        assertFalse(vInf.isInfinite());
        assertTrue(vInf.isNaN());
    }

    @Test(timeout = 4000)
    public void testZeroToleranceBoundaries() {
        OpenMapRealVector v = new OpenMapRealVector(2, 1e-4);
        v.setEntry(0, 1e-5); // Below epsilon -> considered zero, not stored
        assertEquals(0.0, v.getEntry(0), EPS);
        assertEquals(0, v.getSparsity(), EPS);

        v.setEntry(1, 1e-3); // Above epsilon -> stored
        assertEquals(1e-3, v.getEntry(1), EPS);
        assertEquals(0.5, v.getSparsity(), EPS);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testUnitizeZeroNormThrows() {
        OpenMapRealVector v = new OpenMapRealVector(new double[] { 0.0, 0.0 });
        v.unitize();
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testAddDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.add(v2);
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testSubtractDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        v1.subtract(new double[] { 1.0, 2.0, 3.0 });
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testDotProductDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        OpenMapRealVector v2 = new OpenMapRealVector(3);
        v1.dotProduct(v2);
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testGetDistanceDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        v1.getDistance(new double[] { 1.0 });
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testGetL1DistanceDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        v1.getL1Distance(new double[] { 1.0 });
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testGetLInfDistanceDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        v1.getLInfDistance(new double[] { 1.0 });
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testEbeMultiplyDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        v1.ebeMultiply(new double[] { 1.0 });
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testEbeDivideDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(2);
        v1.ebeDivide(new double[] { 1.0 });
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testGetEntryNegativeIndex() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.getEntry(-1);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testGetEntryIndexOutOfBounds() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.getEntry(3);
    }

    @Test(expected = OutOfRangeException.class, timeout = 4000)
    public void testSubVectorOutOfBounds() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.getSubVector(2, 2); // 2 + 2 - 1 = 3 >= dimension
    }

    // =========================================================================
    // Partition E: Object Lifecycle, Iterators & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testSparseIteratorAndEntry() {
        OpenMapRealVector v = new OpenMapRealVector(new double[] { 0.0, 10.0, 0.0, 20.0 });
        Iterator<RealVector.Entry> it = v.sparseIterator();

        int count = 0;
        while (it.hasNext()) {
            RealVector.Entry entry = it.next();
            if (entry.getIndex() == 1) {
                assertEquals(10.0, entry.getValue(), EPS);
                entry.setValue(15.0);
            } else if (entry.getIndex() == 3) {
                assertEquals(20.0, entry.getValue(), EPS);
            } else {
                fail("Unexpected non-zero index: " + entry.getIndex());
            }
            count++;
        }
        assertEquals(2, count);
        assertEquals(15.0, v.getEntry(1), EPS);
    }

    @Test(expected = UnsupportedOperationException.class, timeout = 4000)
    public void testSparseIteratorRemoveThrows() {
        OpenMapRealVector v = new OpenMapRealVector(new double[] { 1.0 });
        Iterator<RealVector.Entry> it = v.sparseIterator();
        it.remove();
    }

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[] { 1.0, 2.0, 0.0 });
        OpenMapRealVector v2 = new OpenMapRealVector(new double[] { 1.0, 2.0, 0.0 });
        OpenMapRealVector v3 = new OpenMapRealVector(new double[] { 1.0, 0.0, 2.0 });
        OpenMapRealVector v4 = new OpenMapRealVector(new double[] { 1.0, 2.0, 0.0 }, 1e-5);
        OpenMapRealVector vDiffDim = new OpenMapRealVector(new double[] { 1.0, 2.0 });

        // Reflexive
        assertTrue(v1.equals(v1));
        // Symmetric
        assertTrue(v1.equals(v2));
        assertTrue(v2.equals(v1));
        assertEquals(v1.hashCode(), v2.hashCode());

        // Null and type checks
        assertFalse(v1.equals(null));
        assertFalse(v1.equals("Some String"));

        // Value differences
        assertFalse(v1.equals(v3));
        // Epsilon differences
        assertFalse(v1.equals(v4));
        // Dimension differences
        assertFalse(v1.equals(vDiffDim));

        // Different sparsity structure with different entries
        OpenMapRealVector v5 = new OpenMapRealVector(new double[] { 1.0, 2.0, 3.0 });
        OpenMapRealVector v6 = new OpenMapRealVector(new double[] { 1.0, 2.0, 4.0 });
        assertFalse(v5.equals(v6));
        assertFalse(v6.equals(v5));
    }

    @Test(timeout = 4000)
    public void testCopyIndependence() {
        OpenMapRealVector original = new OpenMapRealVector(new double[] { 1.0, 2.0 });
        OpenMapRealVector copy = original.copy();

        assertEquals(original.getDimension(), copy.getDimension());
        assertEquals(original.getEntry(0), copy.getEntry(0), EPS);

        copy.setEntry(0, 100.0);
        assertEquals(100.0, copy.getEntry(0), EPS);
        assertEquals(1.0, original.getEntry(0), EPS);
    }
}