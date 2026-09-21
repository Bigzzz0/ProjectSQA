package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Comprehensive JUnit 4 test suite for OpenMapRealVector, targeting line/branch coverage and known defect.
 *
 * [Branch & Defect Analysis Matrix]
 * - Branch Coverage: isDefaultValue, setEntry (put/remove), add (copyThis branch, key existence), subtract (key existence),
 *   dotProduct (size comparison), getDistance (missing keys in both directions), getL1Distance/LInfDistance (two loops),
 *   isInfinite (NaN/Infinite), isNaN, unitize (zero norm), getSubVector (range check), set, mapAddToSelf, equals/hashCode.
 * - Defect Target: Known failure in testBasicFunctions where result expected 6.0 but got -3.0 (likely in dotProduct, L1 distance,
 *   or arithmetic). We directly test dotProduct with an all-ones vector and L1 distance to a zero vector to catch sign/sum errors.
 * - Boundary: epsilon tolerance, default zero, empty vector, single-element, positive/negative/NaN/Infinity entries.
 */
public class OpenMapRealVectorDeepseekTest {

    // ---------- Partition A: Core Functional Logic & State Transitions ----------

    @Test(timeout = 4000)
    public void testConstructorsAndDimension() {
        OpenMapRealVector v0 = new OpenMapRealVector();
        assertEquals(0, v0.getDimension());

        OpenMapRealVector v5 = new OpenMapRealVector(5);
        assertEquals(5, v5.getDimension());
        assertArrayEquals(new double[5], v5.getData(), 1e-15);

        OpenMapRealVector vTol = new OpenMapRealVector(3, 1e-10);
        assertEquals(3, vTol.getDimension());

        OpenMapRealVector vExp = new OpenMapRealVector(10, 5);
        assertEquals(10, vExp.getDimension());

        OpenMapRealVector vExpEps = new OpenMapRealVector(10, 5, 1e-8);
        assertEquals(10, vExpEps.getDimension());

        double[] data = {1.0, 0.0, 2.0, 0.0, 3.0};
        OpenMapRealVector vFromArray = new OpenMapRealVector(data);
        assertArrayEquals(data, vFromArray.getData(), 1e-15);
        assertEquals(5, vFromArray.getDimension());

        OpenMapRealVector vFromArrayEps = new OpenMapRealVector(data, 0.5);
        assertArrayEquals(data, vFromArrayEps.getData(), 1e-15);

        Double[] dataD = {1.0, 0.0, 2.0};
        OpenMapRealVector vFromDouble = new OpenMapRealVector(dataD);
        assertArrayEquals(new double[]{1,0,2}, vFromDouble.getData(), 1e-15);

        OpenMapRealVector copy = new OpenMapRealVector(vFromArray);
        assertEquals(vFromArray.getDimension(), copy.getDimension());
        assertArrayEquals(data, copy.getData(), 1e-15);

        RealVector rv = new ArrayRealVector(new double[]{4.0, 5.0, 6.0});
        OpenMapRealVector fromRV = new OpenMapRealVector(rv);
        assertArrayEquals(new double[]{4,5,6}, fromRV.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAdd() {
        double[] d1 = {1.0, 0.0, 2.0, 0.0, 3.0};
        double[] d2 = {0.0, 4.0, 0.0, 5.0, 0.0};
        OpenMapRealVector v1 = new OpenMapRealVector(d1);
        OpenMapRealVector v2 = new OpenMapRealVector(d2);
        // add OpenMap -> OpenMap
        OpenMapRealVector sum = v1.add(v2);
        assertArrayEquals(new double[]{1,4,2,5,3}, sum.getData(), 1e-15);
        // add RealVector (non-sparse)
        RealVector rv = new ArrayRealVector(new double[]{1,1,1,1,1});
        OpenMapRealVector sum2 = v1.add(rv);
        assertArrayEquals(new double[]{2,1,3,1,4}, sum2.getData(), 1e-15);
        // test copyThis branch (v1 has 3 non-zero, v2 has 2 non-zero – v1 larger)
        double[] d3 = {0,0,0,0,10};
        OpenMapRealVector v3 = new OpenMapRealVector(d3);
        OpenMapRealVector sum3 = v2.add(v3); // v2 has 2, v3 has 1 -> copy v2 (larger)
        assertArrayEquals(new double[]{0,4,0,5,10}, sum3.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSubtract() {
        double[] d1 = {1.0, 0.0, 2.0, 0.0, 3.0};
        double[] d2 = {0.0, 4.0, 0.0, 5.0, 0.0};
        OpenMapRealVector v1 = new OpenMapRealVector(d1);
        OpenMapRealVector v2 = new OpenMapRealVector(d2);
        // subtract OpenMap
        OpenMapRealVector diff = v1.subtract(v2);
        assertArrayEquals(new double[]{1,-4,2,-5,3}, diff.getData(), 1e-15);
        // subtract RealVector
        RealVector rv = new ArrayRealVector(new double[]{1,1,1,1,1});
        OpenMapRealVector diff2 = v1.subtract(rv);
        assertArrayEquals(new double[]{0,-1,1,-1,2}, diff2.getData(), 1e-15);
        // subtract double[] with missing entries in this
        double[] dv = {0,0,0,0,0};
        OpenMapRealVector diff3 = v1.subtract(dv);
        assertArrayEquals(d1, diff3.getData(), 1e-15);
        // case where v has key not in this
        OpenMapRealVector v3 = new OpenMapRealVector(new double[]{0,0,0,0,10});
        OpenMapRealVector diff4 = v1.subtract(v3);
        assertArrayEquals(new double[]{1,0,2,0,-7}, diff4.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testEbeMultiplyAndDivide() {
        double[] d1 = {1.0, 0.0, 2.0, 0.0, 3.0};
        OpenMapRealVector v = new OpenMapRealVector(d1);
        RealVector rv = new ArrayRealVector(new double[]{2,3,4,5,6});
        OpenMapRealVector mul = v.ebeMultiply(rv);
        assertArrayEquals(new double[]{2,0,8,0,18}, mul.getData(), 1e-15);
        double[] arr = {2,3,4,5,6};
        OpenMapRealVector mul2 = v.ebeMultiply(arr);
        assertArrayEquals(new double[]{2,0,8,0,18}, mul2.getData(), 1e-15);
        OpenMapRealVector div = v.ebeDivide(rv);
        assertArrayEquals(new double[]{0.5,0,0.5,0,0.5}, div.getData(), 1e-15);
        OpenMapRealVector div2 = v.ebeDivide(arr);
        assertArrayEquals(new double[]{0.5,0,0.5,0,0.5}, div2.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDotProduct() {
        double[] d1 = {1.0, 0.0, 2.0, 0.0, 3.0};
        OpenMapRealVector v1 = new OpenMapRealVector(d1);
        OpenMapRealVector v2 = new OpenMapRealVector(d1);
        assertEquals(14.0, v1.dotProduct(v2), 1e-15);
        // when this is smaller (3 vs 5 non-zero)
        double[] d3 = {0,0,0,0,10};
        OpenMapRealVector v3 = new OpenMapRealVector(d3);
        assertEquals(30.0, v1.dotProduct(v3), 1e-15);
        // dot product with non-sparse RealVector: all ones => sum = 1+0+2+0+3 = 6.0 -> target defect
        RealVector ones = new ArrayRealVector(new double[]{1,1,1,1,1});
        assertEquals("Dot with ones should be 6.0", 6.0, v1.dotProduct(ones), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetNormAndDistance() {
        double[] d1 = {1.0, 0.0, 2.0, 0.0, 3.0};
        OpenMapRealVector v = new OpenMapRealVector(d1);
        // L1 distance to zero vector = L1 norm = 6.0 -> target defect
        OpenMapRealVector zero = new OpenMapRealVector(5);
        assertEquals("L1 distance to zero should be 6.0", 6.0, v.getL1Distance(zero), 1e-15);
        assertEquals(6.0, v.getL1Distance((RealVector) zero), 1e-15);
        assertEquals(6.0, v.getL1Distance(new double[5]), 1e-15);
        // L2 distance to zero = sqrt(1+4+9)=sqrt(14)
        assertEquals(Math.sqrt(14.0), v.getDistance(zero), 1e-15);
        assertEquals(Math.sqrt(14.0), v.getDistance((RealVector) zero), 1e-15);
        assertEquals(Math.sqrt(14.0), v.getDistance(new double[5]), 1e-15);
        // LInf distance to zero = max(|1|,|0|,|2|,|0|,|3|) = 3
        assertEquals(3.0, v.getLInfDistance(zero), 1e-15);
        assertEquals(3.0, v.getLInfDistance((RealVector) zero), 1e-15);
        assertEquals(3.0, v.getLInfDistance(new double[5]), 1e-15);
        // Test getLInfNorm (known bug: returns sum instead of max)
        // Correct max absolute = 3, but bug returns 1+2+3=6. We assert correct behavior.
        assertEquals("getLInfNorm should be max absolute", 3.0, v.getLInfNorm(), 1e-15);
        // Additional distance between two non-zero vectors
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{0,4,0,5,0});
        assertEquals(15.0, v.getL1Distance(v2), 1e-15);
        assertEquals(Math.sqrt(55.0), v.getDistance(v2), 1e-15);
        assertEquals(5.0, v.getLInfDistance(v2), 1e-15);
        // distance when v2 has keys not in v1
        OpenMapRealVector v3 = new OpenMapRealVector(new double[]{10,0,0,0,0});
        assertEquals(21.0, v.getL1Distance(v3), 1e-15);
    }

    @Test(timeout = 4000)
    public void testGetSubVectorAndSetSubVector() {
        double[] d = {1.0, 0.0, 2.0, 0.0, 3.0};
        OpenMapRealVector v = new OpenMapRealVector(d);
        OpenMapRealVector sub = v.getSubVector(1, 3);
        assertArrayEquals(new double[]{0,2,0}, sub.getData(), 1e-15);
        // set subvector
        v.setSubVector(1, new double[]{9,9,9});
        assertArrayEquals(new double[]{1,9,9,9,3}, v.getData(), 1e-15);
        v.setSubVector(0, new OpenMapRealVector(new double[]{-1,-2}));
        assertArrayEquals(new double[]{-1,-2,9,9,3}, v.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testMapAddToSelfAndMapAdd() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1,2,3});
        v.mapAddToSelf(10.0);
        assertArrayEquals(new double[]{11,12,13}, v.getData(), 1e-15);
        OpenMapRealVector v2 = v.mapAdd(-5.0);
        assertArrayEquals(new double[]{6,7,8}, v2.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testAppend() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1,2});
        OpenMapRealVector app = v.append(new double[]{3,4});
        assertArrayEquals(new double[]{1,2,3,4}, app.getData(), 1e-15);
        OpenMapRealVector app2 = v.append(5.0);
        assertArrayEquals(new double[]{1,2,5}, app2.getData(), 1e-15);
        OpenMapRealVector app3 = v.append(new OpenMapRealVector(new double[]{6}));
        assertArrayEquals(new double[]{1,2,6}, app3.getData(), 1e-15);
        OpenMapRealVector app4 = v.append((RealVector) new ArrayRealVector(new double[]{7}));
        assertArrayEquals(new double[]{1,2,7}, app4.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testOuterProductAndProjection() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1,2,3});
        RealMatrix m = v.outerProduct(new double[]{4,5});
        assertEquals(3, m.getRowDimension());
        assertEquals(2, m.getColumnDimension());
        assertEquals(4, m.getEntry(0,0), 1e-15);
        assertEquals(10, m.getEntry(1,1), 1e-15);
        assertEquals(12, m.getEntry(2,1), 1e-15);
        // projection
        RealVector onto = new ArrayRealVector(new double[]{1,0,0});
        RealVector proj = v.projection(onto);
        assertArrayEquals(new double[]{1,0,0}, proj.getData(), 1e-15);
        OpenMapRealVector proj2 = v.projection(new double[]{0,1,0});
        assertArrayEquals(new double[]{0,2,0}, proj2.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSetAndGetEntry() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.setEntry(2, 10.0);
        assertEquals(10.0, v.getEntry(2), 1e-15);
        // set to zero -> should remove entry
        v.setEntry(2, 0.0);
        assertEquals(0.0, v.getEntry(2), 1e-15);
        assertFalse(v.getData()[2] == 10.0); // ensure removed
        // set with value exactly epsilon (1e-12) -> should not be stored (abs < epsilon)
        v.setEntry(0, 0.5e-12);
        assertEquals(0.0, v.getEntry(0), 1e-15);
        // set with value >= epsilon
        v.setEntry(0, 1.5e-12);
        assertEquals(1.5e-12, v.getEntry(0), 1e-20);
    }

    @Test(timeout = 4000)
    public void testSet() {
        OpenMapRealVector v = new OpenMapRealVector(3);
        v.set(5.0);
        assertArrayEquals(new double[]{5,5,5}, v.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testUnitVectorAndUnitize() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{3.0,0,4.0});
        OpenMapRealVector unit = v.unitVector();
        assertEquals(1.0, unit.getNorm(), 1e-15);
        assertArrayEquals(new double[]{0.6,0,0.8}, unit.getData(), 1e-15);
        // unitize in place
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{3.0,0,4.0});
        v2.unitize();
        assertArrayEquals(new double[]{0.6,0,0.8}, v2.getData(), 1e-15);
        // zero norm throws
        try {
            new OpenMapRealVector(3).unitize();
            fail("Expected ArithmeticException");
        } catch (ArithmeticException e) {
            // expected
        }
    }

    @Test(timeout = 4000)
    public void testIsNaNandIsInfinite() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, Double.NaN, 2.0});
        assertTrue(v.isNaN());
        assertFalse(v.isInfinite());
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1.0, Double.POSITIVE_INFINITY, 2.0});
        assertFalse(v2.isNaN());
        assertTrue(v2.isInfinite());
        OpenMapRealVector v3 = new OpenMapRealVector(new double[]{1.0, 2.0, 3.0});
        assertFalse(v3.isNaN());
        assertFalse(v3.isInfinite());
        // NaN makes isInfinite return false even if other entries are infinite
        OpenMapRealVector v4 = new OpenMapRealVector(new double[]{Double.NaN, Double.POSITIVE_INFINITY});
        assertTrue(v4.isNaN());
        assertFalse(v4.isInfinite());
    }

    @Test(timeout = 4000)
    public void testSparsity() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1,0,0,2,0});
        assertEquals(0.4, v.getSparcity(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSparseIteratorAndEntry() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{10,0,20,0,30});
        java.util.Iterator<RealVector.Entry> it = v.sparseIterator();
        double sum = 0;
        while (it.hasNext()) {
            RealVector.Entry e = it.next();
            sum += e.getValue();
            e.setValue(e.getValue() + 1);
        }
        assertEquals(60.0, sum, 1e-15);
        // verify modifications took effect
        assertArrayEquals(new double[]{11,0,21,0,31}, v.getData(), 1e-15);
    }

    // ---------- Partition B: Boundary Value Analysis & Extremes ----------

    @Test(timeout = 4000)
    public void testEmptyVector() {
        OpenMapRealVector empty = new OpenMapRealVector(0);
        assertEquals(0, empty.getDimension());
        assertArrayEquals(new double[0], empty.getData(), 1e-15);
        assertEquals(0.0, empty.getL1Distance(empty), 1e-15);
        assertEquals(0.0, empty.getDistance(empty), 1e-15);
        assertEquals(0.0, empty.getLInfDistance(empty), 1e-15);
        assertEquals(0.0, empty.getLInfNorm(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSingleElement() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{5.0});
        assertEquals(5.0, v.getEntry(0), 1e-15);
        assertEquals(5.0, v.getL1Distance(new OpenMapRealVector(1)), 1e-15);
        assertEquals(5.0, v.getNorm(), 1e-15);
        assertEquals(5.0, v.getLInfNorm(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testNegativeValues() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{-1.0, -2.0, -3.0});
        assertEquals(6.0, v.getL1Distance(new OpenMapRealVector(3)), 1e-15);
        assertEquals(3.0, v.getLInfNorm(), 1e-15);
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1,2,3});
        OpenMapRealVector sum = v.add(v2);
        assertArrayEquals(new double[]{0,0,0}, sum.getData(), 1e-15);
    }

    @Test(timeout = 4000)
    public void testExtremeTolerance() {
        OpenMapRealVector v1 = new OpenMapRealVector(3, 1e-10);
        double val = 0.5e-10; // less than epsilon, considered zero
        v1.setEntry(0, val);
        assertEquals(0.0, v1.getEntry(0), 1e-20);
        double val2 = 1.5e-10; // > epsilon, stored
        v1.setEntry(1, val2);
        assertEquals(val2, v1.getEntry(1), 1e-20);
    }

    // ---------- Partition C: Defect-Targeted Branch Zone ----------

    /**
     * Directly targets the known defect: testBasicFunctions failure where expected 6.0 but was -3.0.
     * We test dotProduct with all-ones vector (sum of entries = 6) and L1 distance to zero vector (also 6).
     * Both should yield 6.0; a sign or sum error would reveal the bug.
     */
    @Test(timeout = 4000)
    public void testDefectBasicFunctions() {
        // Vector with entries that sum to 6: [1,2,3] sparse
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1.0, 0.0, 2.0, 0.0, 3.0});
        // Dot product with all-ones vector: 1+2+3 = 6.0
        RealVector ones = new ArrayRealVector(new double[]{1,1,1,1,1});
        assertEquals("Dot product with ones should be 6.0", 6.0, v.dotProduct(ones), 1e-15);
        // L1 distance to zero vector: 1+2+3 = 6.0
        OpenMapRealVector zero = new OpenMapRealVector(5);
        assertEquals("L1 distance to zero should be 6.0", 6.0, v.getL1Distance(zero), 1e-15);
        // Additional check: sum of entries via mapAddToSelf? Not needed.
    }

    // ---------- Partition D: Exception & Defensive Guard Paths ----------

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetEntryNegativeIndex() {
        new OpenMapRealVector(5).getEntry(-1);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetEntryOutOfBounds() {
        new OpenMapRealVector(5).getEntry(5);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testSetEntryNegativeIndex() {
        new OpenMapRealVector(5).setEntry(-1, 1.0);
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testSetEntryOutOfBounds() {
        new OpenMapRealVector(5).setEntry(5, 1.0);
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testAddDimensionMismatch() {
        new OpenMapRealVector(3).add(new OpenMapRealVector(4));
    }

    @Test(expected = IllegalArgumentException.class, timeout = 4000)
    public void testDotProductDimensionMismatch() {
        new OpenMapRealVector(3).dotProduct(new OpenMapRealVector(4));
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testGetSubVectorInvalidRange() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.getSubVector(3, 5); // index+size-1 > dimension-1
    }

    @Test(expected = MatrixIndexException.class, timeout = 4000)
    public void testSetSubVectorInvalidRange() {
        OpenMapRealVector v = new OpenMapRealVector(5);
        v.setSubVector(3, new double[]{1,2,3}); // index+len-1 = 5 > 4
    }

    // ---------- Partition E: Object Lifecycle & Contract Integrity ----------

    @Test(timeout = 4000)
    public void testEqualsAndHashCode() {
        OpenMapRealVector v1 = new OpenMapRealVector(new double[]{1,0,2,0,3});
        OpenMapRealVector v2 = new OpenMapRealVector(new double[]{1,0,2,0,3});
        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());
        // different dimension
        OpenMapRealVector v3 = new OpenMapRealVector(new double[]{1,0,2});
        assertFalse(v1.equals(v3));
        // different epsilon
        OpenMapRealVector v4 = new OpenMapRealVector(new double[]{1,0,2,0,3}, 0.5);
        assertFalse(v1.equals(v4));
        // different entries
        OpenMapRealVector v5 = new OpenMapRealVector(new double[]{1,0,2,0,4});
        assertFalse(v1.equals(v5));
        // null
        assertFalse(v1.equals(null));
        // different type
        assertFalse(v1.equals("string"));
        // reflexivity
        assertEquals(v1, v1);
    }

    @Test(timeout = 4000)
    public void testCopy() {
        OpenMapRealVector v = new OpenMapRealVector(new double[]{1,0,2,0,3});
        OpenMapRealVector copy = v.copy();
        assertTrue(v.equals(copy));
        // modify copy, original unchanged
        copy.setEntry(0, 99);
        assertEquals(1.0, v.getEntry(0), 1e-15);
    }

    @Test(timeout = 4000)
    public void testSerialization() {
        // Not required by spec, but we can verify basic contract if needed.
        // OpenMapRealVector implements Serializable, but we skip actual serialization test.
    }
}