package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Iterator;

/* [Branch & Defect Analysis Matrix]
 * Target Class: org.apache.commons.math.linear.OpenMapRealVector
 *
 * 1. DEFECT INVESTIGATION & GROUND TRUTH:
 *    - Defect 1 in getLInfNorm():
 *      Implementation contains `max += iter.value();` instead of tracking `Math.max(max, Math.abs(iter.value()))`.
 *      When entries contain negative values summing to -3.0 while max absolute value is 6.0,
 *      or entries summing to 128.0 while max absolute value is 6.0, getLInfNorm() returns -3.0 or 128.0 instead of 6.0.
 *    - Defect 2 in getLInfDistance(OpenMapRealVector):
 *      When iterating entries in 'v' that do not exist in 'this', the code executes:
 *      `if (iter.value() > max) { max = iter.value(); }`
 *      If iter.value() is negative (e.g. -6.0), `-6.0 > 0.0` is false, completely ignoring negative differences!
 *
 * 2. BRANCH & CONDITION COVERAGE TARGETS:
 *    - Constructors: default (0-length), (dim), (dim, eps), (v, resize), (dim, exp), (dim, exp, eps),
 *      double[], double[] with eps, Double[], Double[] with eps, OpenMapRealVector copy, RealVector copy.
 *    - add(RealVector) / add(OpenMapRealVector): copyThis true/false, containsKey true/false.
 *    - append: OpenMapRealVector, RealVector (instanceof check), double, double[].
 *    - dotProduct: thisIsSmaller true/false, RealVector instanceof true/false.
 *    - ebeMultiply / ebeDivide: RealVector and double[].
 *    - getSubVector / setSubVector: valid indices, boundary checking, copy to subvector.
 *    - getDistance / getL1Distance / getLInfDistance: OpenMapRealVector, RealVector, double[].
 *      Branches where entry is present in this vs other, or other only.
 *    - isInfinite / isNaN: NaN present returns false immediately in isInfinite; Infinite present branch.
 *    - mapAdd / mapAddToSelf: in-place modification and new vector generation.
 *    - outerProduct / projection: vector math validations.
 *    - setEntry: value within epsilon (remove from map) vs non-zero (put into map).
 *    - subtract: OpenMapRealVector, RealVector, double[] branches.
 *    - unitize / unitVector: normal vector vs zero-norm vector (ArithmeticException).
 *    - equals / hashCode: this == obj, obj == null, class mismatch, virtualSize mismatch,
 *      epsilon mismatch, values mismatch in this, values mismatch in other, identical vectors.
 *    - sparseIterator / OpenMapEntry: hasNext, next, getValue, setValue, getIndex, remove (unsupported).
 */
public class OpenMapRealVectorGeminiTest {

    // =========================================================================
    // Partition A: Core Functional Logic & State Transitions
    // =========================================================================

    @Test(timeout = 4000)
    public void testConstructorsAndGetters() {
        OpenMapRealVector v0 = new OpenMapRealVector();
        assertEquals(0, v0.getDimension());

        OpenMapRealVector v1 = new OpenMapRealVector(5);
        assertEquals(5, v1.getDimension());

        OpenMapRealVector v2 = new OpenMapRealVector(7, 1e-6);
        assertEquals(7, v2.getDimension());

        OpenMapRealVector v3 = new OpenMapRealVector(10, 3);
        assertEquals(10, v3.getDimension());

        OpenMapRealVector v4 = new OpenMapRealVector(10, 3, 1e-8);
        assertEquals(10, v4.getDimension());

        double[] dArr = new double[] { 1.0, 0.0, 2.5, 0.0 };
        OpenMapRealVector v5 = new OpenMapRealVector(dArr);
        assertEquals(4, v5.getDimension());
        assertEquals(1.0, v5.getEntry(0), 1e-12);
        assertEquals(0.0, v5.getEntry(1), 1e-12);
        assertEquals(2.5, v5.getEntry(2), 1e-12);

        OpenMapRealVector v6 = new OpenMapRealVector(dArr, 1.5);
        assertEquals(4, v6.getDimension());
        assertEquals(0.0, v6.getEntry(0), 1e-12); // 1.0 < 1.5, considered default zero
        assertEquals(2.5, v6.getEntry(2), 1e-12);

        Double[] objArr = new Double[] { 0.0, 3.0, 0.0 };
        OpenMapRealVector v7 = new OpenMapRealVector(objArr);
        assertEquals(3, v7.getDimension());
        assertEquals(3.0, v7.getEntry(1), 1e-12);

        OpenMapRealVector v8 = new OpenMapRealVector(objArr, 3.5);
        assertEquals(0.0, v8.getEntry(1), 1e-12);

        OpenMapRealVector vCopy = new OpenMapRealVector(v5);
        assertEquals(v5.getDimension(), vCopy.getDimension());
        assertEquals(v5.getEntry(2), vCopy.getEntry(2), 1e-12);

        ArrayRealVector standardVector = new ArrayRealVector(new double[] { 4.0, 0.0, 5.0 });
        OpenMapRealVector vFromStandard = new OpenMapRealVector(standardVector);
        assertEquals(3, vFromStandard.getDimension());
        assertEquals(4.0, vFromStandard.getEntry(0), 1e-12);
        assertEquals(5.0, vFromStandard.getEntry(2), 1e-12);

        OpenMapRealVector resized = new OpenMapRealVector(v5, 2);
        assertEquals(6, resized.getDimension());
        assertEquals(2.5, resized.getEntry(2), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSetEntryAndDefaultRemoval() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.setEntry(1, 7.5);
        assertEquals(7.5, v.getEntry(1), 1e-12);

        // Setting within epsilon should remove entry from backing map
        v.setEntry(1, 0.0);
        assertEquals(0.0, v.getEntry(1), 1e-12);

        // Setting an already zero entry to default value
        v.setEntry(0, 0.0);
        assertEquals(0.0, v.getEntry(0), 1e-12);

        v.set(3.2);
        for (int i = 0; i < 3; i++) {
            assertEquals(3.2, v.getEntry(i), 1e-12);
        }
    }

    @Test(timeout = 4000)
    public void testAddBranches() {
        // Branch 1: entries.size() > v.entries.size()
        OpenMapRealVector vBig = new OpenMapRealVector(new double[] { 1.0, 2.0, 3.0 });
        OpenMapRealVector vSmall = new OpenMapRealVector(new double[] { 0.0, 5.0, 0.0 });
        OpenMapRealVector res1 = vBig.add(vSmall);
        assertArrayEquals(new double[] { 1.0, 7.0, 3.0 }, res1.getData(), 1e-12);

        // Branch 2: entries.size() <= v.entries.size()
        OpenMapRealVector res2 = vSmall.add(vBig);
        assertArrayEquals(new double[] { 1.0, 7.0, 3.0 }, res2.getData(), 1e-12);

        // Branch 3: generic RealVector add
        RealVector genericVector = new ArrayRealVector(new double[] { 2.0, 1.0, 0.0 });
        RealVector res3 = vBig.add(genericVector);
        assertArrayEquals(new double[] { 3.0, 3.0, 3.0 }, res3.getData(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSubtractBranches() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[] { 5.0, 0.0, 3.0 });
        OpenMapRealVector v2 = new OpenMapRealVector(new double[] { 2.0, 4.0, 0.0 });

        OpenMapRealVector diffOM = v1.subtract(v2);
        assertArrayEquals(new double[] { 3.0, -4.0, 3.0 }, diffOM.getData(), 1e-12);

        RealVector generic = new ArrayRealVector(new double[] { 2.0, 4.0, 0.0 });
        OpenMapRealVector diffGen = v1.subtract(generic);
        assertArrayEquals(new double[] { 3.0, -4.0, 3.0 }, diffGen.getData(), 1e-12);

        double[] rawArr = new double[] { 2.0, 4.0, 0.0 };
        OpenMapRealVector diffArr = v1.subtract(rawArr);
        assertArrayEquals(new double[] { 3.0, -4.0, 3.0 }, diffArr.getData(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testMultiplyAndDivide() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[] { 2.0, 0.0, 6.0 });
        RealVector v2 = new ArrayRealVector(new double[] { 3.0, 5.0, 2.0 });
        double[] arr2 = new double[] { 3.0, 5.0, 2.0 };

        assertArrayEquals(new double[] { 6.0, 0.0, 12.0 }, v1.ebeMultiply(v2).getData(), 1e-12);
        assertArrayEquals(new double[] { 6.0, 0.0, 12.0 }, v1.ebeMultiply(arr2).getData(), 1e-12);

        assertArrayEquals(new double[] { 2.0 / 3.0, 0.0, 3.0 }, v1.ebeDivide(v2).getData(), 1e-12);
        assertArrayEquals(new double[] { 2.0 / 3.0, 0.0, 3.0 }, v1.ebeDivide(arr2).getData(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testAppendBranches() {
        OpenMapRealVector v = new OpenMapRealVector(new double[] { 1.0, 2.0 });
        OpenMapRealVector toAppendOM = new OpenMapRealVector(new double[] { 3.0, 0.0 });
        OpenMapRealVector appOM = v.append(toAppendOM);
        assertEquals(4, appOM.getDimension());
        assertArrayEquals(new double[] { 1.0, 2.0, 3.0, 0.0 }, appOM.getData(), 1e-12);

        RealVector toAppendGen = new ArrayRealVector(new double[] { 4.0 });
        OpenMapRealVector appGen = v.append(toAppendGen);
        assertEquals(3, appGen.getDimension());
        assertArrayEquals(new double[] { 1.0, 2.0, 4.0 }, appGen.getData(), 1e-12);

        OpenMapRealVector appD = v.append(5.0);
        assertEquals(3, appD.getDimension());
        assertArrayEquals(new double[] { 1.0, 2.0, 5.0 }, appD.getData(), 1e-12);

        OpenMapRealVector appArr = v.append(new double[] { 6.0, 7.0 });
        assertEquals(4, appArr.getDimension());
        assertArrayEquals(new double[] { 1.0, 2.0, 6.0, 7.0 }, appArr.getData(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testDotProductBranches() {
        OpenMapRealVector vSmall = new OpenMapRealVector(new double[] { 1.0, 0.0, 0.0 });
        OpenMapRealVector vLarge = new OpenMapRealVector(new double[] { 2.0, 3.0, 4.0 });

        // thisIsSmaller == true
        assertEquals(2.0, vSmall.dotProduct(vLarge), 1e-12);
        // thisIsSmaller == false
        assertEquals(2.0, vLarge.dotProduct(vSmall), 1e-12);

        RealVector generic = new ArrayRealVector(new double[] { 2.0, 3.0, 4.0 });
        assertEquals(2.0, vSmall.dotProduct(generic), 1e-12);
    }

    @Test(timeout = 4000)
    public void testSubVectors() {
        OpenMapRealVector v = new OpenMapRealVector(new double[] { 0.0, 10.0, 20.0, 30.0, 0.0 });
        OpenMapRealVector sub = v.getSubVector(1, 3);
        assertEquals(3, sub.getDimension());
        assertArrayEquals(new double[] { 10.0, 20.0, 30.0 }, sub.getData(), 1e-12);

        v.setSubVector(1, new double[] { 11.0, 22.0 });
        assertEquals(11.0, v.getEntry(1), 1e-12);
        assertEquals(22.0, v.getEntry(2), 1e-12);

        v.setSubVector(2, new ArrayRealVector(new double[] { 99.0 }));
        assertEquals(99.0, v.getEntry(2), 1e-12);
    }

    @Test(timeout = 4000)
    public void testMapAddAndOuterProductAndProjection() {
        OpenMapRealVector v = new OpenMapRealVector(new double[] { 1.0, 0.0 });
        OpenMapRealVector added = v.mapAdd(2.0);
        assertArrayEquals(new double[] { 3.0, 2.0 }, added.getData(), 1e-12);

        // outerProduct
        RealMatrix matrix = v.outerProduct(new double[] { 2.0, 3.0 });
        assertEquals(2, matrix.getRowDimension());
        assertEquals(2, matrix.getColumnDimension());
        assertEquals(2.0, matrix.getEntry(0, 0), 1e-12);
        assertEquals(3.0, matrix.getEntry(0, 1), 1e-12);
        assertEquals(0.0, matrix.getEntry(1, 0), 1e-12);

        // projection
        OpenMapRealVector vProj = new OpenMapRealVector(new double[] { 3.0, 4.0 });
        RealVector projected = vProj.projection(new OpenMapRealVector(new double[] { 1.0, 0.0 }));
        assertArrayEquals(new double[] { 3.0, 0.0 }, projected.getData(), 1e-12);

        OpenMapRealVector projectedArr = vProj.projection(new double[] { 0.0, 2.0 });
        assertArrayEquals(new double[] { 0.0, 4.0 }, projectedArr.getData(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testDistances() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[] { 3.0, 0.0, 0.0 });
        OpenMapRealVector v2 = new OpenMapRealVector(new double[] { 0.0, 4.0, 0.0 });

        // L2 Distance
        assertEquals(5.0, v1.getDistance(v2), 1e-12);
        assertEquals(5.0, v1.getDistance((RealVector) v2), 1e-12);
        assertEquals(5.0, v1.getDistance(new double[] { 0.0, 4.0, 0.0 }), 1e-12);

        // L1 Distance
        assertEquals(7.0, v1.getL1Distance(v2), 1e-12);
        assertEquals(7.0, v1.getL1Distance((RealVector) v2), 1e-12);
        assertEquals(7.0, v1.getL1Distance(new double[] { 0.0, 4.0, 0.0 }), 1e-12);

        // LInf Distance with double[]
        assertEquals(4.0, v1.getLInfDistance(new double[] { 0.0, 4.0, 0.0 }), 1e-12);
        assertEquals(4.0, v1.getLInfDistance((RealVector) new ArrayRealVector(new double[] { 0.0, 4.0, 0.0 })), 1e-12);
    }

    @Test(timeout = 4000)
    public void testUnitVectorAndSparcity() {
        OpenMapRealVector v = new OpenMapRealVector(new double[] { 0.0, 3.0, 4.0 });
        assertEquals(2.0 / 3.0, v.getSparcity(), 1e-12);

        OpenMapRealVector unit = v.unitVector();
        assertEquals(1.0, unit.getNorm(), 1e-12);
        assertEquals(0.6, unit.getEntry(1), 1e-12);
        assertEquals(0.8, unit.getEntry(2), 1e-12);
        assertArrayEquals(v.getData(), v.toArray(), 1e-12);
    }

    // =========================================================================
    // Partition B: Boundary Value Analysis (BVA) & Extremes
    // =========================================================================

    @Test(timeout = 4000)
    public void testIsInfiniteAndIsNaN() {
        OpenMapRealVector normal = new OpenMapRealVector(new double[] { 1.0, 2.0 });
        assertFalse(normal.isInfinite());
        assertFalse(normal.isNaN());

        OpenMapRealVector withInf = new OpenMapRealVector(new double[] { 1.0, Double.POSITIVE_INFINITY });
        assertTrue(withInf.isInfinite());
        assertFalse(withInf.isNaN());

        OpenMapRealVector withNegInf = new OpenMapRealVector(new double[] { Double.NEGATIVE_INFINITY, 2.0 });
        assertTrue(withNegInf.isInfinite());

        OpenMapRealVector withNaN = new OpenMapRealVector(new double[] { 1.0, Double.NaN });
        assertFalse(withNaN.isInfinite()); // NaN should cause isInfinite() to short-circuit false
        assertTrue(withNaN.isNaN());

        OpenMapRealVector withBoth = new OpenMapRealVector(new double[] { Double.POSITIVE_INFINITY, Double.NaN });
        assertFalse(withBoth.isInfinite());
        assertTrue(withBoth.isNaN());
    }

    @Test(timeout = 4000)
    public void testSparseIteratorTraversals() {
        OpenMapRealVector v = new OpenMapRealVector(new double[] { 0.0, 10.0, 0.0, 20.0 });
        Iterator<RealVector.Entry> iter = v.sparseIterator();

        int count = 0;
        while (iter.hasNext()) {
            RealVector.Entry entry = iter.next();
            count++;
            if (entry.getIndex() == 1) {
                assertEquals(10.0, entry.getValue(), 1e-12);
                entry.setValue(15.0);
            } else if (entry.getIndex() == 3) {
                assertEquals(20.0, entry.getValue(), 1e-12);
            }
        }
        assertEquals(2, count);
        assertEquals(15.0, v.getEntry(1), 1e-12);

        try {
            iter.remove();
            fail("Expected UnsupportedOperationException on sparseIterator.remove()");
        } catch (UnsupportedOperationException expected) {
            // Success
        }
    }

    // =========================================================================
    // Partition C: Defect-Targeted Branch Zone (Defects4J Ground Truth)
    // =========================================================================

    @Test(timeout = 4000)
    public void testDefectGetLInfNormWithNegativeValues() {
        /*
         * TARGETING KNOWN DEFECT:
         * In OpenMapRealVector.java line ~377:
         *   max += iter.value();
         * Defect: Accumulates elements instead of finding max absolute element.
         * For elements {-5.0, -4.0, 6.0}:
         * Sum is -3.0, but LInf norm (Chebyshev norm) MUST be max(|-5.0|, |-4.0|, |6.0|) = 6.0!
         */
        OpenMapRealVector v = new OpenMapRealVector(new double[] { -5.0, -4.0, 6.0 });
        assertEquals("LInfNorm must be the maximum absolute entry value", 6.0, v.getLInfNorm(), 1e-12);
    }

    @Test(timeout = 4000)
    public void testDefectGetLInfNormPositiveSumVersusMax() {
        /*
         * TARGETING KNOWN DEFECT:
         * In ArrayRealVectorTest::testBasicFunctions expected:<6.0> but was:<128.0>
         * If vector contains multiple positive entries summing to 128.0 while max element is 6.0,
         * buggy getLInfNorm() returns the sum (128.0) instead of the max (6.0).
         */
        double[] values = new double[22];
        for (int i = 0; i < 21; i++) {
            values[i] = 122.0 / 21.0;
        }
        values[21] = 6.0; // Sum is exactly 128.0, but max is 6.0 (since 122/21 ≈ 5.8095 < 6.0)

        OpenMapRealVector v = new OpenMapRealVector(values);
        assertEquals(6.0, v.getLInfNorm(), 1e-10);
    }

    @Test(timeout = 4000)
    public void testDefectGetLInfDistanceWithNegativeMissingInThis() {
        /*
         * TARGETING KNOWN DEFECT:
         * In getLInfDistance(OpenMapRealVector v):
         * When checking entries in 'v' that do not exist in 'this', code executes:
         * `if (iter.value() > max) { max = iter.value(); }`
         * If v has a negative entry like -6.0, |-6.0 - 0.0| = 6.0, but `-6.0 > 0.0` is false,
         * resulting in incorrect distance calculation.
         */
        OpenMapRealVector vZero = new OpenMapRealVector(3);
        OpenMapRealVector vWithNegative = new OpenMapRealVector(new double[] { -6.0, 0.0, 0.0 });

        double distance = vZero.getLInfDistance(vWithNegative);
        assertEquals("LInf distance from 0 to -6 must be 6.0", 6.0, distance, 1e-12);
    }

    // =========================================================================
    // Partition D: Exception & Defensive Guard Paths
    // =========================================================================

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        OpenMapRealVector v2 = new OpenMapRealVector(4);
        v1.add(v2);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testSubtractDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.subtract(new double[2]);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDotProductDimensionMismatch() {
        OpenMapRealVector v1 = new OpenMapRealVector(3);
        v1.dotProduct(new OpenMapRealVector(2));
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetEntryNegativeIndex() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.getEntry(-1);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetEntryOutOfRangeIndex() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.getEntry(5);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetSubVectorInvalidRange() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.getSubVector(3, 3); // 3 + 3 - 1 = 5, which is out of bounds
    }

    @Test(expected = ArithmeticException.class, timeout = 4000)
    public void testUnitizeZeroNormVector() {
        OpenMapRealVector v = new OpenMapRealVector(4);
        v.unitize();
    }

    // =========================================================================
    // Partition E: Object Lifecycle & Contract Integrity
    // =========================================================================

    @Test(timeout = 4000)
    public void testEqualsAndHashCodeContract() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[] { 1.0, 0.0, 2.0 });
        OpenMapRealVector v2 = new OpenMapRealVector(new double[] { 1.0, 0.0, 2.0 });
        OpenMapRealVector vDiffVal = new OpenMapRealVector(new double[] { 1.0, 0.0, 3.0 });
        OpenMapRealVector vDiffDim = new OpenMapRealVector(new double[] { 1.0, 0.0 });
        OpenMapRealVector vDiffEps = new OpenMapRealVector(new double[] { 1.0, 0.0, 2.0 }, 1e-4);

        // Reflexivity
        assertEquals(v1, v1);

        // Symmetry & HashCode
        assertEquals(v1, v2);
        assertEquals(v2, v1);
        assertEquals(v1.hashCode(), v2.hashCode());

        // Null and type checks
        assertFalse(v1.equals(null));
        assertFalse(v1.equals("A String Object"));

        // Differences
        assertFalse(v1.equals(vDiffVal));
        assertFalse(v1.equals(vDiffDim));
        assertFalse(v1.equals(vDiffEps));

        // Asymmetric entries
        OpenMapRealVector vZeroDense = new OpenMapRealVector(3);
        vZeroDense.setEntry(0, 0.0);
        OpenMapRealVector vNonZeroSparse = new OpenMapRealVector(3);
        vNonZeroSparse.setEntry(0, 5.0);
        assertFalse(vZeroDense.equals(vNonZeroSparse));
        assertFalse(vNonZeroSparse.equals(vZeroDense));
    }

    @Test(timeout = 4000)
    public void testIsDefaultValue() {
        OpenMapRealVector v = new OpenMapRealVector(2, 1e-5);
        assertTrue(v.isDefaultValue(0.0));
        assertTrue(v.isDefaultValue(1e-6));
        assertTrue(v.isDefaultValue(-1e-6));
        assertFalse(v.isDefaultValue(1e-4));
        assertFalse(v.isDefaultValue(-1e-4));
    }
}