package org.jfree.data.statistics;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for DefaultBoxAndWhiskerCategoryDataset.
 */
public class DefaultBoxAndWhiskerCategoryDataset_IPOTest {
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
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(0, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_002() throws Exception {
        // Combination: row=1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_003() throws Exception {
        // Combination: row=-1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(-1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_004() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_005() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_006() throws Exception {
        // Combination: row=0, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(0, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_007() throws Exception {
        // Combination: row=1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_008() throws Exception {
        // Combination: row=-1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(-1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_009() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_010() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_011() throws Exception {
        // Combination: row=0, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(0, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_012() throws Exception {
        // Combination: row=1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_013() throws Exception {
        // Combination: row=-1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(-1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_014() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_015() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_016() throws Exception {
        // Combination: row=0, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_017() throws Exception {
        // Combination: row=1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_018() throws Exception {
        // Combination: row=-1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_019() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_020() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_021() throws Exception {
        // Combination: row=0, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_022() throws Exception {
        // Combination: row=1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_023() throws Exception {
        // Combination: row=-1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_024() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_025() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_026() throws Exception {
        // Combination: rowKey="sample_str", columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue("sample_str", "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_027() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_028() throws Exception {
        // Combination: rowKey="sample_str", columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue("sample_str", Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_029() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getValue(Integer.valueOf(1), Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_030() throws Exception {
        // Combination: row=0, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(0, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_031() throws Exception {
        // Combination: row=1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_032() throws Exception {
        // Combination: row=-1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(-1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_033() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_034() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_035() throws Exception {
        // Combination: row=0, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(0, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_036() throws Exception {
        // Combination: row=1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_037() throws Exception {
        // Combination: row=-1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(-1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_038() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_039() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_040() throws Exception {
        // Combination: row=0, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(0, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_041() throws Exception {
        // Combination: row=1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_042() throws Exception {
        // Combination: row=-1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(-1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_043() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_044() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_045() throws Exception {
        // Combination: row=0, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_046() throws Exception {
        // Combination: row=1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_047() throws Exception {
        // Combination: row=-1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_048() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_049() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_050() throws Exception {
        // Combination: row=0, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_051() throws Exception {
        // Combination: row=1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_052() throws Exception {
        // Combination: row=-1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_053() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_054() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_055() throws Exception {
        // Combination: rowKey="sample_str", columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue("sample_str", "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_056() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_057() throws Exception {
        // Combination: rowKey="sample_str", columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue("sample_str", Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeanValue_pairwise_058() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMeanValue(Integer.valueOf(1), Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_059() throws Exception {
        // Combination: row=0, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(0, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_060() throws Exception {
        // Combination: row=1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_061() throws Exception {
        // Combination: row=-1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(-1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_062() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_063() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_064() throws Exception {
        // Combination: row=0, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(0, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_065() throws Exception {
        // Combination: row=1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_066() throws Exception {
        // Combination: row=-1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(-1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_067() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_068() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_069() throws Exception {
        // Combination: row=0, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(0, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_070() throws Exception {
        // Combination: row=1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_071() throws Exception {
        // Combination: row=-1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(-1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_072() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_073() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_074() throws Exception {
        // Combination: row=0, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_075() throws Exception {
        // Combination: row=1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_076() throws Exception {
        // Combination: row=-1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_077() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_078() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_079() throws Exception {
        // Combination: row=0, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_080() throws Exception {
        // Combination: row=1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_081() throws Exception {
        // Combination: row=-1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_082() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_083() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_084() throws Exception {
        // Combination: rowKey="sample_str", columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue("sample_str", "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_085() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_086() throws Exception {
        // Combination: rowKey="sample_str", columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue("sample_str", Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMedianValue_pairwise_087() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMedianValue(Integer.valueOf(1), Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_088() throws Exception {
        // Combination: row=0, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(0, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_089() throws Exception {
        // Combination: row=1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_090() throws Exception {
        // Combination: row=-1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(-1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_091() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_092() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_093() throws Exception {
        // Combination: row=0, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(0, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_094() throws Exception {
        // Combination: row=1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_095() throws Exception {
        // Combination: row=-1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(-1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_096() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_097() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_098() throws Exception {
        // Combination: row=0, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(0, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_099() throws Exception {
        // Combination: row=1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_100() throws Exception {
        // Combination: row=-1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(-1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_101() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_102() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_103() throws Exception {
        // Combination: row=0, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_104() throws Exception {
        // Combination: row=1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_105() throws Exception {
        // Combination: row=-1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_106() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_107() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_108() throws Exception {
        // Combination: row=0, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_109() throws Exception {
        // Combination: row=1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_110() throws Exception {
        // Combination: row=-1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_111() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_112() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_113() throws Exception {
        // Combination: rowKey="sample_str", columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value("sample_str", "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_114() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_115() throws Exception {
        // Combination: rowKey="sample_str", columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value("sample_str", Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ1Value_pairwise_116() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ1Value(Integer.valueOf(1), Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_117() throws Exception {
        // Combination: row=0, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(0, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_118() throws Exception {
        // Combination: row=1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_119() throws Exception {
        // Combination: row=-1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(-1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_120() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_121() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_122() throws Exception {
        // Combination: row=0, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(0, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_123() throws Exception {
        // Combination: row=1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_124() throws Exception {
        // Combination: row=-1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(-1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_125() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_126() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_127() throws Exception {
        // Combination: row=0, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(0, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_128() throws Exception {
        // Combination: row=1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_129() throws Exception {
        // Combination: row=-1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(-1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_130() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_131() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_132() throws Exception {
        // Combination: row=0, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_133() throws Exception {
        // Combination: row=1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_134() throws Exception {
        // Combination: row=-1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_135() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_136() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_137() throws Exception {
        // Combination: row=0, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_138() throws Exception {
        // Combination: row=1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_139() throws Exception {
        // Combination: row=-1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_140() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_141() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_142() throws Exception {
        // Combination: rowKey="sample_str", columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value("sample_str", "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_143() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_144() throws Exception {
        // Combination: rowKey="sample_str", columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value("sample_str", Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getQ3Value_pairwise_145() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getQ3Value(Integer.valueOf(1), Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_146() throws Exception {
        // Combination: row=0, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(0, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_147() throws Exception {
        // Combination: row=1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_148() throws Exception {
        // Combination: row=-1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(-1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_149() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_150() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_151() throws Exception {
        // Combination: row=0, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(0, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_152() throws Exception {
        // Combination: row=1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_153() throws Exception {
        // Combination: row=-1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(-1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_154() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_155() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_156() throws Exception {
        // Combination: row=0, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(0, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_157() throws Exception {
        // Combination: row=1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_158() throws Exception {
        // Combination: row=-1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(-1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_159() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_160() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_161() throws Exception {
        // Combination: row=0, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_162() throws Exception {
        // Combination: row=1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_163() throws Exception {
        // Combination: row=-1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_164() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_165() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_166() throws Exception {
        // Combination: row=0, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_167() throws Exception {
        // Combination: row=1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_168() throws Exception {
        // Combination: row=-1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_169() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_170() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_171() throws Exception {
        // Combination: rowKey="sample_str", columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue("sample_str", "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_172() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_173() throws Exception {
        // Combination: rowKey="sample_str", columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue("sample_str", Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinRegularValue_pairwise_174() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinRegularValue(Integer.valueOf(1), Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_175() throws Exception {
        // Combination: row=0, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(0, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_176() throws Exception {
        // Combination: row=1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_177() throws Exception {
        // Combination: row=-1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(-1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_178() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_179() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_180() throws Exception {
        // Combination: row=0, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(0, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_181() throws Exception {
        // Combination: row=1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_182() throws Exception {
        // Combination: row=-1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(-1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_183() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_184() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_185() throws Exception {
        // Combination: row=0, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(0, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_186() throws Exception {
        // Combination: row=1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_187() throws Exception {
        // Combination: row=-1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(-1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_188() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_189() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_190() throws Exception {
        // Combination: row=0, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_191() throws Exception {
        // Combination: row=1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_192() throws Exception {
        // Combination: row=-1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_193() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_194() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_195() throws Exception {
        // Combination: row=0, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_196() throws Exception {
        // Combination: row=1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_197() throws Exception {
        // Combination: row=-1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_198() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_199() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_200() throws Exception {
        // Combination: rowKey="sample_str", columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue("sample_str", "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_201() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_202() throws Exception {
        // Combination: rowKey="sample_str", columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue("sample_str", Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxRegularValue_pairwise_203() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxRegularValue(Integer.valueOf(1), Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_204() throws Exception {
        // Combination: row=0, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(0, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_205() throws Exception {
        // Combination: row=1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_206() throws Exception {
        // Combination: row=-1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(-1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_207() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_208() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_209() throws Exception {
        // Combination: row=0, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(0, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_210() throws Exception {
        // Combination: row=1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_211() throws Exception {
        // Combination: row=-1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(-1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_212() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_213() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_214() throws Exception {
        // Combination: row=0, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(0, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_215() throws Exception {
        // Combination: row=1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_216() throws Exception {
        // Combination: row=-1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(-1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_217() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_218() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_219() throws Exception {
        // Combination: row=0, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_220() throws Exception {
        // Combination: row=1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_221() throws Exception {
        // Combination: row=-1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_222() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_223() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_224() throws Exception {
        // Combination: row=0, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_225() throws Exception {
        // Combination: row=1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_226() throws Exception {
        // Combination: row=-1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_227() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_228() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_229() throws Exception {
        // Combination: rowKey="sample_str", columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier("sample_str", "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_230() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_231() throws Exception {
        // Combination: rowKey="sample_str", columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier("sample_str", Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMinOutlier_pairwise_232() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMinOutlier(Integer.valueOf(1), Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_233() throws Exception {
        // Combination: row=0, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(0, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_234() throws Exception {
        // Combination: row=1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_235() throws Exception {
        // Combination: row=-1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(-1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_236() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_237() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_238() throws Exception {
        // Combination: row=0, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(0, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_239() throws Exception {
        // Combination: row=1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_240() throws Exception {
        // Combination: row=-1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(-1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_241() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_242() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_243() throws Exception {
        // Combination: row=0, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(0, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_244() throws Exception {
        // Combination: row=1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_245() throws Exception {
        // Combination: row=-1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(-1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_246() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_247() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_248() throws Exception {
        // Combination: row=0, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_249() throws Exception {
        // Combination: row=1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_250() throws Exception {
        // Combination: row=-1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_251() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_252() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_253() throws Exception {
        // Combination: row=0, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_254() throws Exception {
        // Combination: row=1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_255() throws Exception {
        // Combination: row=-1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_256() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_257() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_258() throws Exception {
        // Combination: rowKey="sample_str", columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier("sample_str", "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_259() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_260() throws Exception {
        // Combination: rowKey="sample_str", columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier("sample_str", Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMaxOutlier_pairwise_261() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getMaxOutlier(Integer.valueOf(1), Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_262() throws Exception {
        // Combination: row=0, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(0, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_263() throws Exception {
        // Combination: row=1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_264() throws Exception {
        // Combination: row=-1, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(-1, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_265() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_266() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=0
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_267() throws Exception {
        // Combination: row=0, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(0, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_268() throws Exception {
        // Combination: row=1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_269() throws Exception {
        // Combination: row=-1, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(-1, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_270() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_271() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_272() throws Exception {
        // Combination: row=0, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(0, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_273() throws Exception {
        // Combination: row=1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_274() throws Exception {
        // Combination: row=-1, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(-1, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_275() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_276() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=-1
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_277() throws Exception {
        // Combination: row=0, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_278() throws Exception {
        // Combination: row=1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_279() throws Exception {
        // Combination: row=-1, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_280() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_281() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MAX_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_282() throws Exception {
        // Combination: row=0, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_283() throws Exception {
        // Combination: row=1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_284() throws Exception {
        // Combination: row=-1, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_285() throws Exception {
        // Combination: row=Integer.MAX_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_286() throws Exception {
        // Combination: row=Integer.MIN_VALUE, column=Integer.MIN_VALUE
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IndexOutOfBoundsException");
        } catch (java.lang.IndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_287() throws Exception {
        // Combination: rowKey="sample_str", columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers("sample_str", "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_288() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey="sample_str"
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_289() throws Exception {
        // Combination: rowKey="sample_str", columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers("sample_str", Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getOutliers_pairwise_290() throws Exception {
        // Combination: rowKey=Integer.valueOf(1), columnKey=Integer.valueOf(1)
        try {
            (new DefaultBoxAndWhiskerCategoryDataset()).getOutliers(Integer.valueOf(1), Integer.valueOf(1));
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
