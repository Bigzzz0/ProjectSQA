package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for OpenMapRealVector.
 */
public class OpenMapRealVector_IPOTest {
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
    public void test_getSubVector_pairwise_001() throws Exception {
        // Combination: index=0, n=0
        try {
            (new OpenMapRealVector()).getSubVector(0, 0);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_002() throws Exception {
        // Combination: index=0, n=1
        try {
            (new OpenMapRealVector()).getSubVector(0, 1);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_003() throws Exception {
        // Combination: index=0, n=-1
        try {
            (new OpenMapRealVector()).getSubVector(0, -1);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_004() throws Exception {
        // Combination: index=0, n=Integer.MAX_VALUE
        try {
            (new OpenMapRealVector()).getSubVector(0, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_005() throws Exception {
        // Combination: index=0, n=Integer.MIN_VALUE
        try {
            (new OpenMapRealVector()).getSubVector(0, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_006() throws Exception {
        // Combination: index=1, n=0
        try {
            (new OpenMapRealVector()).getSubVector(1, 0);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_007() throws Exception {
        // Combination: index=1, n=1
        try {
            (new OpenMapRealVector()).getSubVector(1, 1);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_008() throws Exception {
        // Combination: index=1, n=-1
        try {
            (new OpenMapRealVector()).getSubVector(1, -1);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_009() throws Exception {
        // Combination: index=1, n=Integer.MAX_VALUE
        try {
            (new OpenMapRealVector()).getSubVector(1, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_010() throws Exception {
        // Combination: index=1, n=Integer.MIN_VALUE
        try {
            (new OpenMapRealVector()).getSubVector(1, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_011() throws Exception {
        // Combination: index=-1, n=0
        try {
            (new OpenMapRealVector()).getSubVector(-1, 0);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_012() throws Exception {
        // Combination: index=-1, n=1
        try {
            (new OpenMapRealVector()).getSubVector(-1, 1);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_013() throws Exception {
        // Combination: index=-1, n=-1
        try {
            (new OpenMapRealVector()).getSubVector(-1, -1);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_014() throws Exception {
        // Combination: index=-1, n=Integer.MAX_VALUE
        try {
            (new OpenMapRealVector()).getSubVector(-1, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_015() throws Exception {
        // Combination: index=-1, n=Integer.MIN_VALUE
        try {
            (new OpenMapRealVector()).getSubVector(-1, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_016() throws Exception {
        // Combination: index=Integer.MAX_VALUE, n=0
        try {
            (new OpenMapRealVector()).getSubVector(Integer.MAX_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_017() throws Exception {
        // Combination: index=Integer.MAX_VALUE, n=1
        try {
            (new OpenMapRealVector()).getSubVector(Integer.MAX_VALUE, 1);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_018() throws Exception {
        // Combination: index=Integer.MAX_VALUE, n=-1
        try {
            (new OpenMapRealVector()).getSubVector(Integer.MAX_VALUE, -1);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_019() throws Exception {
        // Combination: index=Integer.MAX_VALUE, n=Integer.MAX_VALUE
        try {
            (new OpenMapRealVector()).getSubVector(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_020() throws Exception {
        // Combination: index=Integer.MAX_VALUE, n=Integer.MIN_VALUE
        try {
            (new OpenMapRealVector()).getSubVector(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_021() throws Exception {
        // Combination: index=Integer.MIN_VALUE, n=0
        try {
            (new OpenMapRealVector()).getSubVector(Integer.MIN_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_022() throws Exception {
        // Combination: index=Integer.MIN_VALUE, n=1
        try {
            (new OpenMapRealVector()).getSubVector(Integer.MIN_VALUE, 1);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_023() throws Exception {
        // Combination: index=Integer.MIN_VALUE, n=-1
        try {
            (new OpenMapRealVector()).getSubVector(Integer.MIN_VALUE, -1);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_024() throws Exception {
        // Combination: index=Integer.MIN_VALUE, n=Integer.MAX_VALUE
        try {
            (new OpenMapRealVector()).getSubVector(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSubVector_pairwise_025() throws Exception {
        // Combination: index=Integer.MIN_VALUE, n=Integer.MIN_VALUE
        try {
            (new OpenMapRealVector()).getSubVector(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
