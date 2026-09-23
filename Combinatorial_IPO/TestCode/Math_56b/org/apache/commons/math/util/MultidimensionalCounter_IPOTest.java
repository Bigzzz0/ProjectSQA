package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for MultidimensionalCounter.
 */
public class MultidimensionalCounter_IPOTest {
    private static String formatValue(Object value) {
        if (value == null) return "null";
        if (value instanceof Object[]) return java.util.Arrays.deepToString((Object[]) value);
        if (value instanceof byte[]) return java.util.Arrays.toString((byte[]) value);
        if (value instanceof short[]) return java.util.Arrays.toString((short[]) value);
        if (value instanceof int[]) return java.util.Arrays.toString((int[]) value);
        if (value instanceof long[]) return java.util.Arrays.toString((long[]) value);
        if (value instanceof char[]) return java.util.Arrays.toString((char[]) value);
        if (value instanceof float[]) return java.util.Arrays.toString((float[]) value);
        if (value instanceof double[]) return java.util.Arrays.toString((double[]) value);
        if (value instanceof boolean[]) return java.util.Arrays.toString((boolean[]) value);
        return String.valueOf(value);
    }

    @Test(timeout = 4000)
    public void test_getCounts_pairwise_001() throws Exception {
        // Combination: receiver__size=new int[] {}, index=0
        try {
            (new MultidimensionalCounter(new int[] {})).getCounts(0);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCounts_pairwise_002() throws Exception {
        // Combination: receiver__size=new int[] {1}, index=0
        Object actual = (new MultidimensionalCounter(new int[] {1})).getCounts(0);
        assertNotNull(actual);
        assertEquals("[I", actual.getClass().getName());
        assertEquals("[0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getCounts_pairwise_003() throws Exception {
        // Combination: receiver__size=new int[] {}, index=1
        try {
            (new MultidimensionalCounter(new int[] {})).getCounts(1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCounts_pairwise_004() throws Exception {
        // Combination: receiver__size=new int[] {1}, index=1
        try {
            (new MultidimensionalCounter(new int[] {1})).getCounts(1);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCounts_pairwise_005() throws Exception {
        // Combination: receiver__size=new int[] {}, index=-1
        try {
            (new MultidimensionalCounter(new int[] {})).getCounts(-1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCounts_pairwise_006() throws Exception {
        // Combination: receiver__size=new int[] {1}, index=-1
        try {
            (new MultidimensionalCounter(new int[] {1})).getCounts(-1);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCounts_pairwise_007() throws Exception {
        // Combination: receiver__size=new int[] {}, index=Integer.MAX_VALUE
        try {
            (new MultidimensionalCounter(new int[] {})).getCounts(Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCounts_pairwise_008() throws Exception {
        // Combination: receiver__size=new int[] {1}, index=Integer.MAX_VALUE
        try {
            (new MultidimensionalCounter(new int[] {1})).getCounts(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCounts_pairwise_009() throws Exception {
        // Combination: receiver__size=new int[] {}, index=Integer.MIN_VALUE
        try {
            (new MultidimensionalCounter(new int[] {})).getCounts(Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCounts_pairwise_010() throws Exception {
        // Combination: receiver__size=new int[] {1}, index=Integer.MIN_VALUE
        try {
            (new MultidimensionalCounter(new int[] {1})).getCounts(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCount_pairwise_011() throws Exception {
        // Combination: receiver__size=new int[] {}, c=new int[] {}
        try {
            (new MultidimensionalCounter(new int[] {})).getCount(new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCount_pairwise_012() throws Exception {
        // Combination: receiver__size=new int[] {1}, c=new int[] {}
        try {
            (new MultidimensionalCounter(new int[] {1})).getCount(new int[] {});
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCount_pairwise_013() throws Exception {
        // Combination: receiver__size=new int[] {}, c=new int[] {1}
        try {
            (new MultidimensionalCounter(new int[] {})).getCount(new int[] {1});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCount_pairwise_014() throws Exception {
        // Combination: receiver__size=new int[] {1}, c=new int[] {1}
        try {
            (new MultidimensionalCounter(new int[] {1})).getCount(new int[] {1});
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
