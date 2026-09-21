package org.apache.commons.math3.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * [Branch & Defect Analysis Matrix]
 * 
 * Target: org.apache.commons.math3.util.MathArrays
 * Known Defect: linearCombination(double[], double[]) throws ArrayIndexOutOfBoundsException
 *               when input arrays have length 1 (accesses prodHigh[1] without bounds check).
 * 
 * Branch coverage targets:
 * - linearCombination: len == 1 (defect), len > 1, NaN/inf fallback
 * - safeNorm: all three scaling branches (xabs < rdwarf, xabs > agiant, else)
 * - checkOrder: all direction/strict combinations, early exit vs full loop
 * - sortInPlace: null checks, dimension mismatch, increasing/decreasing
 * - normalizeArray: infinite/nan target sum, infinite elements, sum zero, NaN elements
 * - convolve: null/empty checks, normal convolution
 * - equals/equalsIncludingNaN: null, length mismatch, element equality
 * - ebe*: dimension mismatch, normal operation
 * - scale, scaleInPlace: normal
 * - copyOf: truncation/padding
 * - buildArray: field, length, rows/columns
 * - checkRectangular: null, non-rectangular
 * - checkPositive: non-positive values
 * - checkNonNegative: negative values
 * - isMonotonic: generic and double versions
 * - distance*: various array lengths
 * 
 * Test partitions:
 * A: Core functional logic (linearCombination, safeNorm, convolve, sortInPlace)
 * B: Boundary values (empty arrays, single element, null, extremes)
 * C: Defect-targeted (single-element linearCombination)
 * D: Exception paths (dimension mismatch, illegal arguments, null)
 * E: Object lifecycle (equals, copyOf)
 */
public class MathArraysDeepseekTest {

    // ========== Partition A: Core Functional Logic ==========

    @Test(timeout = 4000)
    public void testLinearCombinationTwoTerms() {
        double result = MathArrays.linearCombination(1.0, 2.0, 3.0, 4.0);
        assertEquals(1.0*2.0 + 3.0*4.0, result, 1e-15);
    }

    @Test(timeout = 4000)
    public void testLinearCombinationThreeTerms() {
        double result = MathArrays.linearCombination(1.0, 2.0, 3.0, 4.0, 5.0, 6.0);
        assertEquals(1.0*2.0 + 3.0*4.0 + 5.0*6.0, result, 1e-15);
    }

    @Test(timeout = 4000)
    public void testLinearCombinationFourTerms() {
        double result = MathArrays.linearCombination(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0);
        assertEquals(1.0*2.0 + 3.0*4.0 + 5.0*6.0 + 7.0*8.0, result, 1e-15);
    }

    @Test(timeout = 4000)
    public void testLinearCombinationArrayNormal() {
        double[] a = {1.0, 2.0, 3.0};
        double[] b = {4.0, 5.0, 6.0};
        double result = MathArrays.linearCombination(a, b);
        assertEquals(1.0*4.0 + 2.0*5.0 + 3.0*6.0, result, 1e-15);
    }

    @Test(timeout = 4000)
    public void testLinearCombinationArrayNaN() {
        double[] a = {Double.NaN, 2.0};
        double[] b = {1.0, 3.0};
        double result = MathArrays.linearCombination(a, b);
        assertTrue(Double.isNaN(result));
    }

    @Test(timeout = 4000)
    public void testLinearCombinationArrayInfinity() {
        double[] a = {Double.POSITIVE_INFINITY, 2.0};
        double[] b = {1.0, 3.0};
        double result = MathArrays.linearCombination(a, b);
        assertTrue(Double.isInfinite(result));
    }

    @Test(timeout = 4000)
    public void testSafeNorm() {
        double[] v = {3.0, 4.0};
        double norm = MathArrays.safeNorm(v);
        assertEquals(5.0, norm, 1e-15);
    }

    @Test(timeout = 4000)
    public void testSafeNormLargeValues() {
        double[] v = {1.0e200, 1.0e200};
        double norm = MathArrays.safeNorm(v);
        assertEquals(Math.sqrt(2.0)*1.0e200, norm, 1.0e195);
    }

    @Test(timeout = 4000)
    public void testSafeNormTinyValues() {
        double[] v = {1.0e-200, 1.0e-200};
        double norm = MathArrays.safeNorm(v);
        assertEquals(Math.sqrt(2.0)*1.0e-200, norm, 1.0e-215);
    }

    @Test(timeout = 4000)
    public void testConvolveNormal() {
        double[] x = {1.0, 2.0, 3.0};
        double[] h = {0.5, 1.0};
        double[] y = MathArrays.convolve(x, h);
        assertArrayEquals(new double[]{0.5, 2.0, 3.5, 3.0}, y, 1e-15);
    }

    @Test(timeout = 4000)
    public void testSortInPlaceIncreasing() {
        double[] x = {3.0, 1.0, 2.0};
        double[] y = {9.0, 7.0, 8.0};
        MathArrays.sortInPlace(x, y);
        assertArrayEquals(new double[]{1.0, 2.0, 3.0}, x, 1e-15);
        assertArrayEquals(new double[]{7.0, 8.0, 9.0}, y, 1e-15);
    }

    @Test(timeout = 4000)
    public void testSortInPlaceDecreasing() {
        double[] x = {3.0, 1.0, 2.0};
        double[] y = {9.0, 7.0, 8.0};
        MathArrays.sortInPlace(x, MathArrays.OrderDirection.DECREASING, y);
        assertArrayEquals(new double[]{3.0, 2.0, 1.0}, x, 1e-15);
        assertArrayEquals(new double[]{9.0, 8.0, 7.0}, y, 1e-15);
    }

    // ========== Partition B: Boundary Values & Extremes ==========

    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testEbeAddDimensionMismatch() {
        MathArrays.ebeAdd(new double[2], new double[3]);
    }

    @Test(timeout = 4000)
    public void testEbeAddNormal() {
        double[] a = {1.0, 2.0};
        double[] b = {3.0, 4.0};
        double[] result = MathArrays.ebeAdd(a, b);
        assertArrayEquals(new double[]{4.0, 6.0}, result, 1e-15);
    }

    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testEbeSubtractDimensionMismatch() {
        MathArrays.ebeSubtract(new double[2], new double[3]);
    }

    @Test(timeout = 4000)
    public void testEbeSubtractNormal() {
        double[] a = {5.0, 7.0};
        double[] b = {2.0, 3.0};
        double[] result = MathArrays.ebeSubtract(a, b);
        assertArrayEquals(new double[]{3.0, 4.0}, result, 1e-15);
    }

    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testEbeMultiplyDimensionMismatch() {
        MathArrays.ebeMultiply(new double[2], new double[3]);
    }

    @Test(timeout = 4000)
    public void testEbeMultiplyNormal() {
        double[] a = {2.0, 3.0};
        double[] b = {4.0, 5.0};
        double[] result = MathArrays.ebeMultiply(a, b);
        assertArrayEquals(new double[]{8.0, 15.0}, result, 1e-15);
    }

    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testEbeDivideDimensionMismatch() {
        MathArrays.ebeDivide(new double[2], new double[3]);
    }

    @Test(timeout = 4000)
    public void testEbeDivideNormal() {
        double[] a = {10.0, 12.0};
        double[] b = {2.0, 3.0};
        double[] result = MathArrays.ebeDivide(a, b);
        assertArrayEquals(new double[]{5.0, 4.0}, result, 1e-15);
    }

    @Test(timeout = 4000)
    public void testDistance1Double() {
        double[] p1 = {1.0, 2.0};
        double[] p2 = {4.0, 6.0};
        assertEquals(7.0, MathArrays.distance1(p1, p2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDistance1Int() {
        int[] p1 = {1, 2};
        int[] p2 = {4, 6};
        assertEquals(7, MathArrays.distance1(p1, p2));
    }

    @Test(timeout = 4000)
    public void testDistanceDouble() {
        double[] p1 = {1.0, 2.0};
        double[] p2 = {4.0, 6.0};
        assertEquals(5.0, MathArrays.distance(p1, p2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDistanceInt() {
        int[] p1 = {1, 2};
        int[] p2 = {4, 6};
        assertEquals(5.0, MathArrays.distance(p1, p2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDistanceInfDouble() {
        double[] p1 = {1.0, 2.0};
        double[] p2 = {4.0, 6.0};
        assertEquals(4.0, MathArrays.distanceInf(p1, p2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDistanceInfInt() {
        int[] p1 = {1, 2};
        int[] p2 = {4, 6};
        assertEquals(4, MathArrays.distanceInf(p1, p2));
    }

    @Test(timeout = 4000)
    public void testScale() {
        double[] arr = {1.0, 2.0, 3.0};
        double[] scaled = MathArrays.scale(2.0, arr);
        assertArrayEquals(new double[]{2.0, 4.0, 6.0}, scaled, 1e-15);
    }

    @Test(timeout = 4000)
    public void testScaleInPlace() {
        double[] arr = {1.0, 2.0, 3.0};
        MathArrays.scaleInPlace(2.0, arr);
        assertArrayEquals(new double[]{2.0, 4.0, 6.0}, arr, 1e-15);
    }

    @Test(timeout = 4000)
    public void testCopyOfInt() {
        int[] source = {1, 2, 3};
        int[] copy = MathArrays.copyOf(source);
        assertArrayEquals(new int[]{1, 2, 3}, copy);
    }

    @Test(timeout = 4000)
    public void testCopyOfDouble() {
        double[] source = {1.0, 2.0};
        double[] copy = MathArrays.copyOf(source);
        assertArrayEquals(new double[]{1.0, 2.0}, copy, 1e-15);
    }

    @Test(timeout = 4000)
    public void testCopyOfIntWithLen() {
        int[] source = {1, 2, 3};
        int[] copy = MathArrays.copyOf(source, 5);
        assertArrayEquals(new int[]{1, 2, 3, 0, 0}, copy);
    }

    @Test(timeout = 4000)
    public void testCopyOfDoubleWithLen() {
        double[] source = {1.0, 2.0};
        double[] copy = MathArrays.copyOf(source, 1);
        assertArrayEquals(new double[]{1.0}, copy, 1e-15);
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleBothNull() {
        assertTrue(MathArrays.equals((double[])null, (double[])null));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleOneNull() {
        assertFalse(MathArrays.equals(new double[1], null));
        assertFalse(MathArrays.equals(null, new double[1]));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleLengthMismatch() {
        assertFalse(MathArrays.equals(new double[2], new double[3]));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleEqual() {
        double[] x = {1.0, 2.0};
        double[] y = {1.0, 2.0};
        assertTrue(MathArrays.equals(x, y));
    }

    @Test(timeout = 4000)
    public void testEqualsDoubleNotEqual() {
        double[] x = {1.0, 2.0};
        double[] y = {1.0, 3.0};
        assertFalse(MathArrays.equals(x, y));
    }

    @Test(timeout = 4000)
    public void testEqualsIncludingNaNDouble() {
        double[] x = {Double.NaN, 1.0};
        double[] y = {Double.NaN, 1.0};
        assertTrue(MathArrays.equalsIncludingNaN(x, y));
    }

    @Test(timeout = 4000)
    public void testEqualsFloat() {
        float[] x = {1.0f, 2.0f};
        float[] y = {1.0f, 2.0f};
        assertTrue(MathArrays.equals(x, y));
    }

    @Test(timeout = 4000)
    public void testEqualsIncludingNaNFLoat() {
        float[] x = {Float.NaN, 1.0f};
        float[] y = {Float.NaN, 1.0f};
        assertTrue(MathArrays.equalsIncludingNaN(x, y));
    }

    @Test(timeout = 4000)
    public void testNormalizeArray() {
        double[] values = {1.0, 2.0, 3.0};
        double[] normalized = MathArrays.normalizeArray(values, 1.0);
        double sum = 0;
        for (double v : normalized) sum += v;
        assertEquals(1.0, sum, 1e-15);
    }

    @Test(timeout = 4000, expected = MathIllegalArgumentException.class)
    public void testNormalizeArrayInfiniteTarget() {
        MathArrays.normalizeArray(new double[1], Double.POSITIVE_INFINITY);
    }

    @Test(timeout = 4000, expected = MathIllegalArgumentException.class)
    public void testNormalizeArrayNaNTarget() {
        MathArrays.normalizeArray(new double[1], Double.NaN);
    }

    @Test(timeout = 4000, expected = MathIllegalArgumentException.class)
    public void testNormalizeArrayInfiniteElement() {
        MathArrays.normalizeArray(new double[]{Double.POSITIVE_INFINITY}, 1.0);
    }

    @Test(timeout = 4000, expected = MathArithmeticException.class)
    public void testNormalizeArraySumZero() {
        MathArrays.normalizeArray(new double[]{0.0, 0.0}, 1.0);
    }

    @Test(timeout = 4000)
    public void testNormalizeArrayWithNaN() {
        double[] values = {Double.NaN, 2.0};
        double[] normalized = MathArrays.normalizeArray(values, 1.0);
        assertTrue(Double.isNaN(normalized[0]));
        assertEquals(1.0, normalized[1], 1e-15);
    }

    @Test(timeout = 4000)
    public void testIsMonotonicGenericIncreasingStrict() {
        Integer[] val = {1, 2, 3};
        assertTrue(MathArrays.isMonotonic(val, MathArrays.OrderDirection.INCREASING, true));
    }

    @Test(timeout = 4000)
    public void testIsMonotonicGenericIncreasingNonStrict() {
        Integer[] val = {1, 2, 2};
        assertTrue(MathArrays.isMonotonic(val, MathArrays.OrderDirection.INCREASING, false));
    }

    @Test(timeout = 4000)
    public void testIsMonotonicGenericDecreasingStrict() {
        Integer[] val = {3, 2, 1};
        assertTrue(MathArrays.isMonotonic(val, MathArrays.OrderDirection.DECREASING, true));
    }

    @Test(timeout = 4000)
    public void testIsMonotonicDouble() {
        double[] val = {1.0, 2.0, 3.0};
        assertTrue(MathArrays.isMonotonic(val, MathArrays.OrderDirection.INCREASING, true));
    }

    @Test(timeout = 4000)
    public void testCheckOrderIncreasingStrict() {
        double[] val = {1.0, 2.0, 3.0};
        assertTrue(MathArrays.checkOrder(val, MathArrays.OrderDirection.INCREASING, true, false));
    }

    @Test(timeout = 4000, expected = NonMonotonicSequenceException.class)
    public void testCheckOrderIncreasingStrictAbort() {
        double[] val = {1.0, 2.0, 2.0};
        MathArrays.checkOrder(val, MathArrays.OrderDirection.INCREASING, true);
    }

    @Test(timeout = 4000)
    public void testCheckOrderDecreasingNonStrict() {
        double[] val = {3.0, 2.0, 2.0};
        assertTrue(MathArrays.checkOrder(val, MathArrays.OrderDirection.DECREASING, false, false));
    }

    @Test(timeout = 4000)
    public void testCheckRectangular() {
        long[][] in = {{1, 2}, {3, 4}};
        MathArrays.checkRectangular(in); // should not throw
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testCheckRectangularNull() {
        MathArrays.checkRectangular(null);
    }

    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testCheckRectangularNonRectangular() {
        long[][] in = {{1, 2}, {3}};
        MathArrays.checkRectangular(in);
    }

    @Test(timeout = 4000)
    public void testCheckPositive() {
        MathArrays.checkPositive(new double[]{1.0, 2.0});
    }

    @Test(timeout = 4000, expected = NotStrictlyPositiveException.class)
    public void testCheckPositiveNonPositive() {
        MathArrays.checkPositive(new double[]{0.0});
    }

    @Test(timeout = 4000)
    public void testCheckNonNegative1D() {
        MathArrays.checkNonNegative(new long[]{0, 1});
    }

    @Test(timeout = 4000, expected = NotPositiveException.class)
    public void testCheckNonNegative1DNegative() {
        MathArrays.checkNonNegative(new long[]{-1});
    }

    @Test(timeout = 4000)
    public void testCheckNonNegative2D() {
        MathArrays.checkNonNegative(new long[][]{{0, 1}, {2, 3}});
    }

    @Test(timeout = 4000, expected = NotPositiveException.class)
    public void testCheckNonNegative2DNegative() {
        MathArrays.checkNonNegative(new long[][]{{-1}});
    }

    @Test(timeout = 4000)
    public void testBuildArray1D() {
        Field<Double> field = new DummyField(Double.class);
        Double[] arr = MathArrays.buildArray(field, 3);
        assertEquals(3, arr.length);
        for (Double d : arr) assertEquals(0.0, d, 1e-15);
    }

    @Test(timeout = 4000)
    public void testBuildArray2D() {
        Field<Double> field = new DummyField(Double.class);
        Double[][] arr = MathArrays.buildArray(field, 2, 3);
        assertEquals(2, arr.length);
        assertEquals(3, arr[0].length);
        for (Double[] row : arr)
            for (Double d : row) assertEquals(0.0, d, 1e-15);
    }

    @Test(timeout = 4000)
    public void testBuildArray2DWithNegativeColumns() {
        Field<Double> field = new DummyField(Double.class);
        Double[][] arr = MathArrays.buildArray(field, 2, -1);
        assertEquals(2, arr.length);
        assertNull(arr[0]); // partially built
    }

    // ========== Partition C: Defect-Targeted (single-element linearCombination) ==========

    @Test(timeout = 4000)
    public void testLinearCombinationWithSingleElementArray() {
        // This test targets the known defect: ArrayIndexOutOfBoundsException when len == 1
        double[] a = {2.0};
        double[] b = {3.0};
        double result = MathArrays.linearCombination(a, b);
        assertEquals(6.0, result, 1e-15);
    }

    // ========== Partition D: Exception & Defensive Guard Paths ==========

    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testLinearCombinationArrayDimensionMismatch() {
        MathArrays.linearCombination(new double[2], new double[3]);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testSortInPlaceNullX() {
        MathArrays.sortInPlace(null, new double[1]);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testSortInPlaceNullY() {
        MathArrays.sortInPlace(new double[1], (double[])null);
    }

    @Test(timeout = 4000, expected = DimensionMismatchException.class)
    public void testSortInPlaceDimensionMismatch() {
        MathArrays.sortInPlace(new double[2], new double[3]);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testConvolveNullX() {
        MathArrays.convolve(null, new double[1]);
    }

    @Test(timeout = 4000, expected = NullArgumentException.class)
    public void testConvolveNullH() {
        MathArrays.convolve(new double[1], null);
    }

    @Test(timeout = 4000, expected = NoDataException.class)
    public void testConvolveEmptyX() {
        MathArrays.convolve(new double[0], new double[1]);
    }

    @Test(timeout = 4000, expected = NoDataException.class)
    public void testConvolveEmptyH() {
        MathArrays.convolve(new double[1], new double[0]);
    }

    // ========== Partition E: Object Lifecycle & Contract Integrity ==========

    @Test(timeout = 4000)
    public void testEqualsDoubleWithNaN() {
        double[] x = {Double.NaN};
        double[] y = {Double.NaN};
        assertFalse(MathArrays.equals(x, y)); // NaN != NaN
    }

    @Test(timeout = 4000)
    public void testEqualsIncludingNaNDoubleWithNaN() {
        double[] x = {Double.NaN};
        double[] y = {Double.NaN};
        assertTrue(MathArrays.equalsIncludingNaN(x, y));
    }

    @Test(timeout = 4000)
    public void testEqualsFloatWithNaN() {
        float[] x = {Float.NaN};
        float[] y = {Float.NaN};
        assertFalse(MathArrays.equals(x, y));
    }

    @Test(timeout = 4000)
    public void testEqualsIncludingNaNFLoatWithNaN() {
        float[] x = {Float.NaN};
        float[] y = {Float.NaN};
        assertTrue(MathArrays.equalsIncludingNaN(x, y));
    }

    // Helper class for buildArray tests
    private static class DummyField implements Field<Double> {
        private final Class<Double> runtimeClass;

        DummyField(Class<Double> runtimeClass) {
            this.runtimeClass = runtimeClass;
        }

        @Override
        public Double getZero() {
            return 0.0;
        }

        @Override
        public Class<Double> getRuntimeClass() {
            return runtimeClass;
        }
    }
}