package org.apache.commons.math3.util;

/*
 * [Branch & Defect Analysis Matrix]
 * Targets: org.apache.commons.math3.util.MathArrays
 *
 * Targeted Branches and Defect Zone:
 * 1. DEFECT ZONE (Defects4J Math-Unknown / Regression):
 *    - linearCombination(double[], double[]) with single-element arrays (len == 1).
 *      In defective versions, accessing prodHigh[1] causes ArrayIndexOutOfBoundsException: 1.
 * 2. Partition A: Core Functional Logic & State Transitions:
 *    - scale, scaleInPlace (positive, negative, zero, identity scaling)
 *    - ebeAdd, ebeSubtract, ebeMultiply, ebeDivide (element-by-element vector arithmetic)
 *    - distance1, distance, distanceInf for double[] and int[]
 *    - isMonotonic (Object[] and double[]), checkOrder with OrderDirection (INCREASING, DECREASING, strict / non-strict)
 *    - safeNorm covering dwarf-dominated (s3), giant-dominated (s1), intermediate (s2), s2 >= x3max, s2 < x3max, all zeros
 *    - sortInPlace (INCREASING and DECREASING, multiple paired arrays, preserving order)
 *    - copyOf (int[] and double[], exact length, truncation, and zero-padding)
 *    - linearCombination (2, 3, 4 scalar terms and multi-element arrays; NaN/Infinite fallback branches)
 *    - equals, equalsIncludingNaN for float[] and double[] (null variations, length mismatches, IEEE equality vs NaN equivalence)
 *    - normalizeArray (standard scaling, NaN preservation)
 *    - buildArray (1D array, 2D rectangular array, 2D jagged/partial array with columns < 0)
 *    - convolve (standard mathematical discrete convolution check)
 * 3. Partition B: Boundary Value Analysis & Extremes:
 *    - Empty arrays, arrays with NaN, arrays with +/- Infinity, values close to 0, rdwarf and rgiant thresholds in safeNorm
 * 4. Partition C: Defensive & Exception Guard Paths:
 *    - DimensionMismatchException in ebeAdd, ebeSubtract, ebeMultiply, ebeDivide, linearCombination, sortInPlace, checkRectangular
 *    - NullArgumentException in sortInPlace, checkRectangular, convolve
 *    - NoDataException in convolve (empty inputs)
 *    - NonMonotonicSequenceException in checkOrder (when abort is true)
 *    - NotStrictlyPositiveException in checkPositive
 *    - NotPositiveException in checkNonNegative (1D and 2D arrays)
 *    - MathIllegalArgumentException and MathArithmeticException in normalizeArray (sum = 0, target is NaN/Inf, element is Inf)
 */

import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Arrays;
import org.apache.commons.math3.Field;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.MathArithmeticException;
import org.apache.commons.math3.exception.MathIllegalArgumentException;
import org.apache.commons.math3.exception.NoDataException;
import org.apache.commons.math3.exception.NonMonotonicSequenceException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.NotStrictlyPositiveException;
import org.apache.commons.math3.exception.NullArgumentException;

public class MathArraysGeminiTest {

    // -------------------------------------------------------------------------
    // Partition C: Defect-Targeted Branch Zone (Defects4J Known Bug)
    // -------------------------------------------------------------------------

    /**
     * Targets Defects4J known bug:
     * linearCombination(double[], double[]) fails with ArrayIndexOutOfBoundsException: 1
     * when arrays have length 1 due to unconditional access to prodHigh[1].
     */
    @Test(timeout = 4000)
    public void testLinearCombinationWithSingleElementArray() {
        final double[] a = new double[] { 3.5 };
        final double[] b = new double[] { -2.0 };
        final double expected = -7.0;
        final double actual = MathArrays.linearCombination(a, b);
        assertEquals("linearCombination with 1-element array should evaluate to a[0]*b[0]",
                     expected, actual, 1e-15);
    }

    // -------------------------------------------------------------------------
    // Partition A: Core Vector & Arithmetic Operations
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testScaleAndScaleInPlace() {
        final double[] orig = new double[] { 1.5, -2.0, 0.0 };
        final double[] scaled = MathArrays.scale(2.0, orig);
        assertArrayEquals(new double[] { 3.0, -4.0, 0.0 }, scaled, 1e-15);
        assertArrayEquals(new double[] { 1.5, -2.0, 0.0 }, orig, 1e-15); // Unchanged original

        MathArrays.scaleInPlace(-3.0, orig);
        assertArrayEquals(new double[] { -4.5, 6.0, -0.0 }, orig, 1e-15);
    }

    @Test(timeout = 4000)
    public void testEbeOperations() {
        final double[] a = new double[] { 10.0, 20.0, 30.0 };
        final double[] b = new double[] { 2.0, 5.0, 10.0 };

        assertArrayEquals(new double[] { 12.0, 25.0, 40.0 }, MathArrays.ebeAdd(a, b), 1e-15);
        assertArrayEquals(new double[] { 8.0, 15.0, 20.0 }, MathArrays.ebeSubtract(a, b), 1e-15);
        assertArrayEquals(new double[] { 20.0, 100.0, 300.0 }, MathArrays.ebeMultiply(a, b), 1e-15);
        assertArrayEquals(new double[] { 5.0, 4.0, 3.0 }, MathArrays.ebeDivide(a, b), 1e-15);
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testEbeAddDimensionMismatch() {
        MathArrays.ebeAdd(new double[2], new double[3]);
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testEbeSubtractDimensionMismatch() {
        MathArrays.ebeSubtract(new double[3], new double[2]);
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testEbeMultiplyDimensionMismatch() {
        MathArrays.ebeMultiply(new double[1], new double[2]);
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testEbeDivideDimensionMismatch() {
        MathArrays.ebeDivide(new double[4], new double[2]);
    }

    // -------------------------------------------------------------------------
    // Distance Metric Tests
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testDistancesDouble() {
        final double[] p1 = new double[] { 1.0, -2.0, 3.0 };
        final double[] p2 = new double[] { 4.0, 2.0, 3.0 };

        assertEquals(7.0, MathArrays.distance1(p1, p2), 1e-15);
        assertEquals(5.0, MathArrays.distance(p1, p2), 1e-15);
        assertEquals(4.0, MathArrays.distanceInf(p1, p2), 1e-15);
    }

    @Test(timeout = 4000)
    public void testDistancesInt() {
        final int[] p1 = new int[] { 1, -2, 3 };
        final int[] p2 = new int[] { 4, 2, 3 };

        assertEquals(7, MathArrays.distance1(p1, p2));
        assertEquals(5.0, MathArrays.distance(p1, p2), 1e-15);
        assertEquals(4, MathArrays.distanceInf(p1, p2));
    }

    // -------------------------------------------------------------------------
    // Monotonicity and Ordering Tests
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testIsMonotonicComparable() {
        final Integer[] incStrict = new Integer[] { 1, 3, 5 };
        final Integer[] incNonStrict = new Integer[] { 1, 3, 3, 5 };
        final Integer[] decStrict = new Integer[] { 5, 3, 1 };
        final Integer[] decNonStrict = new Integer[] { 5, 3, 3, 1 };
        final Integer[] unordered = new Integer[] { 1, 5, 2 };

        assertTrue(MathArrays.isMonotonic(incStrict, MathArrays.OrderDirection.INCREASING, true));
        assertFalse(MathArrays.isMonotonic(incNonStrict, MathArrays.OrderDirection.INCREASING, true));
        assertTrue(MathArrays.isMonotonic(incNonStrict, MathArrays.OrderDirection.INCREASING, false));

        assertTrue(MathArrays.isMonotonic(decStrict, MathArrays.OrderDirection.DECREASING, true));
        assertFalse(MathArrays.isMonotonic(decNonStrict, MathArrays.OrderDirection.DECREASING, true));
        assertTrue(MathArrays.isMonotonic(decNonStrict, MathArrays.OrderDirection.DECREASING, false));

        assertFalse(MathArrays.isMonotonic(unordered, MathArrays.OrderDirection.INCREASING, false));
        assertFalse(MathArrays.isMonotonic(unordered, MathArrays.OrderDirection.DECREASING, false));
    }

    @Test(timeout = 4000)
    public void testCheckOrderDouble() {
        final double[] incStrict = new double[] { 1.0, 2.0, 4.0 };
        final double[] incNonStrict = new double[] { 1.0, 2.0, 2.0, 4.0 };
        final double[] decStrict = new double[] { 4.0, 2.0, 1.0 };
        final double[] decNonStrict = new double[] { 4.0, 2.0, 2.0, 1.0 };
        final double[] bad = new double[] { 1.0, 0.0, 3.0 };

        assertTrue(MathArrays.isMonotonic(incStrict, MathArrays.OrderDirection.INCREASING, true));
        assertFalse(MathArrays.isMonotonic(incNonStrict, MathArrays.OrderDirection.INCREASING, true));
        assertTrue(MathArrays.isMonotonic(incNonStrict, MathArrays.OrderDirection.INCREASING, false));

        assertTrue(MathArrays.checkOrder(decStrict, MathArrays.OrderDirection.DECREASING, true, false));
        assertFalse(MathArrays.checkOrder(decNonStrict, MathArrays.OrderDirection.DECREASING, true, false));
        assertTrue(MathArrays.checkOrder(decNonStrict, MathArrays.OrderDirection.DECREASING, false, false));
        assertFalse(MathArrays.checkOrder(bad, MathArrays.OrderDirection.INCREASING, false, false));

        // Overload calls
        MathArrays.checkOrder(incStrict);
        MathArrays.checkOrder(decStrict, MathArrays.OrderDirection.DECREASING, true);
    }

    @Test(expected = NonMonotonicSequenceException.class, timeout = 4000)
    public void testCheckOrderThrowsExceptionOnFailure() {
        MathArrays.checkOrder(new double[] { 1.0, 3.0, 2.0 });
    }

    // -------------------------------------------------------------------------
    // Validation Guards (checkRectangular, checkPositive, checkNonNegative)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCheckRectangular() {
        final long[][] rect = new long[][] {
            { 1L, 2L },
            { 3L, 4L }
        };
        MathArrays.checkRectangular(rect);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testCheckRectangularNull() {
        MathArrays.checkRectangular(null);
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testCheckRectangularJagged() {
        final long[][] jagged = new long[][] {
            { 1L, 2L },
            { 3L }
        };
        MathArrays.checkRectangular(jagged);
    }

    @Test(timeout = 4000)
    public void testCheckPositiveValid() {
        MathArrays.checkPositive(new double[] { 0.1, 100.0, Double.MAX_VALUE });
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testCheckPositiveWithZero() {
        MathArrays.checkPositive(new double[] { 1.0, 0.0, 2.0 });
    }

    @Test(expected = NotStrictlyPositiveException.class, timeout = 4000)
    public void testCheckPositiveWithNegative() {
        MathArrays.checkPositive(new double[] { 1.0, -0.5, 2.0 });
    }

    @Test(timeout = 4000)
    public void testCheckNonNegative1D() {
        MathArrays.checkNonNegative(new long[] { 0L, 1L, 100L });
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testCheckNonNegative1DFailure() {
        MathArrays.checkNonNegative(new long[] { 0L, -1L, 2L });
    }

    @Test(timeout = 4000)
    public void testCheckNonNegative2D() {
        MathArrays.checkNonNegative(new long[][] {
            { 0L, 5L },
            { 10L, 0L }
        });
    }

    @Test(expected = NotPositiveException.class, timeout = 4000)
    public void testCheckNonNegative2DFailure() {
        MathArrays.checkNonNegative(new long[][] {
            { 0L, 5L },
            { 10L, -1L }
        });
    }

    // -------------------------------------------------------------------------
    // SafeNorm Branch Coverage
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSafeNormBranches() {
        // 1. Zero vector
        assertEquals(0.0, MathArrays.safeNorm(new double[] { 0.0, 0.0 }), 1e-15);

        // 2. Standard values (intermediate range)
        assertEquals(5.0, MathArrays.safeNorm(new double[] { 3.0, 4.0 }), 1e-15);

        // 3. Giant numbers triggering s1 > 0
        final double giant = 1e20;
        final double[] giantVec = new double[] { giant, giant, giant * 0.5 };
        assertTrue(MathArrays.safeNorm(giantVec) > giant);

        // 4. Tiny numbers (dwarf) triggering s3
        final double dwarf = 1e-25;
        final double[] dwarfVec = new double[] { dwarf, 2 * dwarf, 0.0 };
        assertEquals(Math.sqrt(5.0) * dwarf, MathArrays.safeNorm(dwarfVec), 1e-35);

        // 5. Mixed intermediate and dwarf where s2 >= x3max
        final double[] mixed1 = new double[] { 1.0, 1e-25 };
        assertEquals(1.0, MathArrays.safeNorm(mixed1), 1e-15);

        // 6. Mixed where s2 < x3max: s2 is very small non-zero, dwarf x3max is larger
        final double[] mixed2 = new double[] { 1e-21, 1e-22 };
        assertTrue(MathArrays.safeNorm(mixed2) > 0.0);
    }

    // -------------------------------------------------------------------------
    // sortInPlace Coverage
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testSortInPlaceIncreasingAndDecreasing() {
        final double[] x1 = new double[] { 3.0, 1.0, 2.0 };
        final double[] y1 = new double[] { 30.0, 10.0, 20.0 };
        final double[] z1 = new double[] { 300.0, 100.0, 200.0 };

        MathArrays.sortInPlace(x1, y1, z1);
        assertArrayEquals(new double[] { 1.0, 2.0, 3.0 }, x1, 1e-15);
        assertArrayEquals(new double[] { 10.0, 20.0, 30.0 }, y1, 1e-15);
        assertArrayEquals(new double[] { 100.0, 200.0, 300.0 }, z1, 1e-15);

        final double[] x2 = new double[] { 3.0, 1.0, 2.0 };
        final double[] y2 = new double[] { 30.0, 10.0, 20.0 };
        MathArrays.sortInPlace(x2, MathArrays.OrderDirection.DECREASING, y2);
        assertArrayEquals(new double[] { 3.0, 2.0, 1.0 }, x2, 1e-15);
        assertArrayEquals(new double[] { 30.0, 20.0, 10.0 }, y2, 1e-15);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testSortInPlaceNullX() {
        MathArrays.sortInPlace(null, new double[2]);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testSortInPlaceNullY() {
        MathArrays.sortInPlace(new double[2], (double[]) null);
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testSortInPlaceDimensionMismatch() {
        MathArrays.sortInPlace(new double[2], new double[3]);
    }

    // -------------------------------------------------------------------------
    // copyOf Overloads
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testCopyOfInt() {
        final int[] src = new int[] { 10, 20, 30 };
        assertArrayEquals(src, MathArrays.copyOf(src));

        final int[] truncated = MathArrays.copyOf(src, 2);
        assertArrayEquals(new int[] { 10, 20 }, truncated);

        final int[] padded = MathArrays.copyOf(src, 5);
        assertArrayEquals(new int[] { 10, 20, 30, 0, 0 }, padded);
    }

    @Test(timeout = 4000)
    public void testCopyOfDouble() {
        final double[] src = new double[] { 1.1, 2.2, 3.3 };
        assertArrayEquals(src, MathArrays.copyOf(src), 1e-15);

        final double[] truncated = MathArrays.copyOf(src, 2);
        assertArrayEquals(new double[] { 1.1, 2.2 }, truncated, 1e-15);

        final double[] padded = MathArrays.copyOf(src, 5);
        assertArrayEquals(new double[] { 1.1, 2.2, 3.3, 0.0, 0.0 }, padded, 1e-15);
    }

    // -------------------------------------------------------------------------
    // Accurate Linear Combinations (2, 3, 4 terms and arrays)
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testLinearCombinationArray() {
        final double[] a = new double[] { 1.0, 2.0, 3.0, 4.0 };
        final double[] b = new double[] { 10.0, 20.0, 30.0, 40.0 };
        // 10 + 40 + 90 + 160 = 300
        assertEquals(300.0, MathArrays.linearCombination(a, b), 1e-15);

        // NaN fallback branch: infinite factors
        final double[] aInf = new double[] { Double.POSITIVE_INFINITY, 2.0 };
        final double[] bInf = new double[] { 1.0, 2.0 };
        assertTrue(Double.isInfinite(MathArrays.linearCombination(aInf, bInf)));
    }

    @Test(expected = DimensionMismatchException.class, timeout = 4000)
    public void testLinearCombinationArrayMismatch() {
        MathArrays.linearCombination(new double[2], new double[3]);
    }

    @Test(timeout = 4000)
    public void testLinearCombinationScalars() {
        // 2 factors
        assertEquals(11.0, MathArrays.linearCombination(2.0, 3.0, 1.0, 5.0), 1e-15);
        assertTrue(Double.isNaN(MathArrays.linearCombination(Double.NaN, 1.0, 2.0, 3.0)));

        // 3 factors
        assertEquals(23.0, MathArrays.linearCombination(2.0, 3.0, 1.0, 5.0, 4.0, 3.0), 1e-15);
        assertTrue(Double.isNaN(MathArrays.linearCombination(1.0, 2.0, Double.NaN, 4.0, 5.0, 6.0)));

        // 4 factors
        assertEquals(43.0, MathArrays.linearCombination(2.0, 3.0, 1.0, 5.0, 4.0, 3.0, 2.0, 10.0), 1e-15);
        assertTrue(Double.isNaN(MathArrays.linearCombination(1.0, 2.0, 3.0, 4.0, 5.0, 6.0, Double.NaN, 8.0)));
    }

    // -------------------------------------------------------------------------
    // Equality and NaN Handling
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testEqualsFloat() {
        assertTrue(MathArrays.equals((float[]) null, (float[]) null));
        assertFalse(MathArrays.equals(new float[1], null));
        assertFalse(MathArrays.equals(null, new float[1]));
        assertFalse(MathArrays.equals(new float[1], new float[2]));

        final float[] f1 = new float[] { 1.0f, 2.5f, Float.NaN };
        final float[] f2 = new float[] { 1.0f, 2.5f, Float.NaN };
        final float[] f3 = new float[] { 1.0f, 2.5f, 3.0f };

        assertFalse(MathArrays.equals(f1, f2)); // IEEE: NaN != NaN
        assertTrue(MathArrays.equalsIncludingNaN(f1, f2));
        assertFalse(MathArrays.equalsIncludingNaN(f1, f3));
        assertFalse(MathArrays.equals(new float[] { 1.0f }, new float[] { 2.0f }));
    }

    @Test(timeout = 4000)
    public void testEqualsDouble() {
        assertTrue(MathArrays.equals((double[]) null, (double[]) null));
        assertFalse(MathArrays.equals(new double[1], null));
        assertFalse(MathArrays.equals(null, new double[1]));
        assertFalse(MathArrays.equals(new double[1], new double[2]));

        final double[] d1 = new double[] { 1.0, 2.5, Double.NaN };
        final double[] d2 = new double[] { 1.0, 2.5, Double.NaN };
        final double[] d3 = new double[] { 1.0, 2.5, 3.0 };

        assertFalse(MathArrays.equals(d1, d2)); // IEEE: NaN != NaN
        assertTrue(MathArrays.equalsIncludingNaN(d1, d2));
        assertFalse(MathArrays.equalsIncludingNaN(d1, d3));
        assertFalse(MathArrays.equals(new double[] { 1.0 }, new double[] { 2.0 }));
    }

    // -------------------------------------------------------------------------
    // normalizeArray Coverage
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testNormalizeArrayNormal() {
        final double[] values = new double[] { 1.0, 2.0, 3.0, Double.NaN };
        final double[] norm = MathArrays.normalizeArray(values, 12.0);

        assertEquals(2.0, norm[0], 1e-15);
        assertEquals(4.0, norm[1], 1e-15);
        assertEquals(6.0, norm[2], 1e-15);
        assertTrue(Double.isNaN(norm[3]));
    }

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testNormalizeArrayInfiniteTarget() {
        MathArrays.normalizeArray(new double[] { 1.0 }, Double.POSITIVE_INFINITY);
    }

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testNormalizeArrayNaNTarget() {
        MathArrays.normalizeArray(new double[] { 1.0 }, Double.NaN);
    }

    @Test(expected = MathIllegalArgumentException.class, timeout = 4000)
    public void testNormalizeArrayInfiniteElement() {
        MathArrays.normalizeArray(new double[] { 1.0, Double.NEGATIVE_INFINITY }, 10.0);
    }

    @Test(expected = MathArithmeticException.class, timeout = 4000)
    public void testNormalizeArrayZeroSum() {
        MathArrays.normalizeArray(new double[] { 1.0, -1.0 }, 10.0);
    }

    // -------------------------------------------------------------------------
    // buildArray Coverage
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void testBuildArray() {
        final Field dummyField = new Field() {
            public Object getZero() { return "ZERO"; }
            public Object getOne() { return "ONE"; }
            public Class getRuntimeClass() { return String.class; }
        };

        final String[] arr1D = (String[]) MathArrays.buildArray(dummyField, 3);
        assertEquals(3, arr1D.length);
        assertEquals("ZERO", arr1D[0]);
        assertEquals("ZERO", arr1D[1]);
        assertEquals("ZERO", arr1D[2]);

        final String[][] arr2D = (String[][]) MathArrays.buildArray(dummyField, 2, 4);
        assertEquals(2, arr2D.length);
        assertEquals(4, arr2D[0].length);
        assertEquals("ZERO", arr2D[0][0]);

        // Negative column creates dummy partial row
        final String[][] arrPartial = (String[][]) MathArrays.buildArray(dummyField, 3, -1);
        assertEquals(3, arrPartial.length);
        assertNull(arrPartial[0]);
    }

    // -------------------------------------------------------------------------
    // Convolution Tests
    // -------------------------------------------------------------------------

    @Test(timeout = 4000)
    public void testConvolve() {
        final double[] x = new double[] { 1.0, 2.0, 3.0 };
        final double[] h = new double[] { 0.5, 1.0 };
        // Convolution length: 3 + 2 - 1 = 4
        // y[0] = 1*0.5 = 0.5
        // y[1] = 1*1.0 + 2*0.5 = 2.0
        // y[2] = 2*1.0 + 3*0.5 = 3.5
        // y[3] = 3*1.0 = 3.0
        final double[] y = MathArrays.convolve(x, h);
        assertArrayEquals(new double[] { 0.5, 2.0, 3.5, 3.0 }, y, 1e-15);
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testConvolveNullX() {
        MathArrays.convolve(null, new double[] { 1.0 });
    }

    @Test(expected = NullArgumentException.class, timeout = 4000)
    public void testConvolveNullH() {
        MathArrays.convolve(new double[] { 1.0 }, null);
    }

    @Test(expected = NoDataException.class, timeout = 4000)
    public void testConvolveEmptyX() {
        MathArrays.convolve(new double[0], new double[] { 1.0 });
    }

    @Test(expected = NoDataException.class, timeout = 4000)
    public void testConvolveEmptyH() {
        MathArrays.convolve(new double[] { 1.0 }, new double[0]);
    }
}