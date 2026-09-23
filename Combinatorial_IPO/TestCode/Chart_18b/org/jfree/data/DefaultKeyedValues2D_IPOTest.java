package org.jfree.data;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for DefaultKeyedValues2D.
 */
public class DefaultKeyedValues2D_IPOTest {
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
    public void test_getValue_pairwise_001() throws Exception {
        // Combination: row=0, column=0
        try {
            (new DefaultKeyedValues2D()).getValue(0, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_002() throws Exception {
        // Combination: row=1, column=0
        try {
            (new DefaultKeyedValues2D()).getValue(1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_003() throws Exception {
        // Combination: row=-1, column=0
        try {
            (new DefaultKeyedValues2D()).getValue(-1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_004() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=0
        try {
            (new DefaultKeyedValues2D()).getValue(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_005() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=0
        try {
            (new DefaultKeyedValues2D()).getValue(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_006() throws Exception {
        // Combination: row=0, column=1
        try {
            (new DefaultKeyedValues2D()).getValue(0, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_007() throws Exception {
        // Combination: row=1, column=1
        try {
            (new DefaultKeyedValues2D()).getValue(1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_008() throws Exception {
        // Combination: row=-1, column=1
        try {
            (new DefaultKeyedValues2D()).getValue(-1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_009() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=1
        try {
            (new DefaultKeyedValues2D()).getValue(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_010() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=1
        try {
            (new DefaultKeyedValues2D()).getValue(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_011() throws Exception {
        // Combination: row=0, column=-1
        try {
            (new DefaultKeyedValues2D()).getValue(0, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_012() throws Exception {
        // Combination: row=1, column=-1
        try {
            (new DefaultKeyedValues2D()).getValue(1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_013() throws Exception {
        // Combination: row=-1, column=-1
        try {
            (new DefaultKeyedValues2D()).getValue(-1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_014() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=-1
        try {
            (new DefaultKeyedValues2D()).getValue(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_015() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=-1
        try {
            (new DefaultKeyedValues2D()).getValue(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_016() throws Exception {
        // Combination: row=0, column=Integer.MAX_VALUE
        try {
            (new DefaultKeyedValues2D()).getValue(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_017() throws Exception {
        // Combination: row=1, column=Integer.MAX_VALUE
        try {
            (new DefaultKeyedValues2D()).getValue(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_018() throws Exception {
        // Combination: row=-1, column=Integer.MAX_VALUE
        try {
            (new DefaultKeyedValues2D()).getValue(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_019() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultKeyedValues2D()).getValue(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_020() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultKeyedValues2D()).getValue(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_021() throws Exception {
        // Combination: row=0, column=Integer.MIN_VALUE
        try {
            (new DefaultKeyedValues2D()).getValue(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_022() throws Exception {
        // Combination: row=1, column=Integer.MIN_VALUE
        try {
            (new DefaultKeyedValues2D()).getValue(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_023() throws Exception {
        // Combination: row=-1, column=Integer.MIN_VALUE
        try {
            (new DefaultKeyedValues2D()).getValue(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_024() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultKeyedValues2D()).getValue(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_025() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultKeyedValues2D()).getValue(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_026() throws Exception {
        // Combination: rowKey="sample_str", columnKey="sample_str"
        try {
            (new DefaultKeyedValues2D()).getValue("sample_str", "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_027() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey="sample_str"
        try {
            (new DefaultKeyedValues2D()).getValue(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_028() throws Exception {
        // Combination: rowKey="sample_str", columnKey=Integer.valueOf(1)
        try {
            (new DefaultKeyedValues2D()).getValue("sample_str", Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_029() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey=Integer.valueOf(1)
        try {
            (new DefaultKeyedValues2D()).getValue(Integer.valueOf(1), Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
