package org.jfree.data.category;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for DefaultIntervalCategoryDataset.
 */
public class DefaultIntervalCategoryDataset_IPOTest {
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
    public void test_getSeriesCount_pairwise_001() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getSeriesCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSeriesCount_pairwise_002() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getSeriesCount();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSeriesCount_pairwise_003() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getSeriesCount();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSeriesCount_pairwise_004() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getSeriesCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSeriesIndex_pairwise_005() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, seriesKey="sample_str"
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getSeriesIndex("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSeriesIndex_pairwise_006() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}, seriesKey=Integer.valueOf(1)
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getSeriesIndex(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSeriesIndex_pairwise_007() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}, seriesKey=Integer.valueOf(1)
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getSeriesIndex(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSeriesIndex_pairwise_008() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, seriesKey="sample_str"
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getSeriesIndex("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getCategoryCount_pairwise_009() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getCategoryCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getCategoryCount_pairwise_010() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getCategoryCount();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCategoryCount_pairwise_011() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getCategoryCount();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCategoryCount_pairwise_012() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getCategoryCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getColumnKeys_pairwise_013() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getColumnKeys();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getColumnKeys_pairwise_014() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getColumnKeys();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnKeys_pairwise_015() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getColumnKeys();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnKeys_pairwise_016() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getColumnKeys();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_017() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series="sample_str", category="sample_str"
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue("sample_str", "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_018() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, series=Integer.valueOf(1), category="sample_str"
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getValue(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_019() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}, series="sample_str", category=Integer.valueOf(1)
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getValue("sample_str", Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_020() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}, series=Integer.valueOf(1), category=Integer.valueOf(1)
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getValue(Integer.valueOf(1), Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_021() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}, series="sample_str", category="sample_str"
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getValue("sample_str", "sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_022() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.valueOf(1), category="sample_str"
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_023() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=0, category=0
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(0, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_024() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, series=1, category=0
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getValue(1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_025() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}, series=-1, category=1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getValue(-1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_026() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}, series=Integer.MAX_VALUE, category=1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getValue(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_027() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=1, category=-1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_028() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, series=0, category=-1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getValue(0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_029() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MIN_VALUE, category=Integer.MAX_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_030() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, series=-1, category=Integer.MAX_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getValue(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_031() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=-1, category=Integer.MIN_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_032() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, series=Integer.MIN_VALUE, category=Integer.MIN_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getValue(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_033() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=0, category=1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(0, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_034() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=0, category=Integer.MAX_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_035() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=0, category=Integer.MIN_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_036() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=1, category=1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_037() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=1, category=Integer.MAX_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_038() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=1, category=Integer.MIN_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_039() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=-1, category=0
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(-1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_040() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=-1, category=-1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(-1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_041() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}, series=Integer.MAX_VALUE, category=0
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getValue(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_042() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MAX_VALUE, category=-1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_043() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MAX_VALUE, category=Integer.MAX_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_044() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MAX_VALUE, category=Integer.MIN_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_045() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MIN_VALUE, category=0
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_046() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MIN_VALUE, category=1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_047() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MIN_VALUE, category=-1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getValue(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_048() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series="sample_str", category="sample_str"
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue("sample_str", "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_049() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, series=Integer.valueOf(1), category="sample_str"
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getStartValue(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_050() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}, series="sample_str", category=Integer.valueOf(1)
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getStartValue("sample_str", Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_051() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}, series=Integer.valueOf(1), category=Integer.valueOf(1)
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getStartValue(Integer.valueOf(1), Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_052() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}, series="sample_str", category="sample_str"
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getStartValue("sample_str", "sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_053() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.valueOf(1), category="sample_str"
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_054() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=0, category=0
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(0, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_055() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, series=1, category=0
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getStartValue(1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_056() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}, series=-1, category=1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getStartValue(-1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_057() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}, series=Integer.MAX_VALUE, category=1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getStartValue(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_058() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=1, category=-1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_059() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, series=0, category=-1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getStartValue(0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_060() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MIN_VALUE, category=Integer.MAX_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_061() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, series=-1, category=Integer.MAX_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getStartValue(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_062() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=-1, category=Integer.MIN_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_063() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, series=Integer.MIN_VALUE, category=Integer.MIN_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getStartValue(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_064() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=0, category=1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(0, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_065() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=0, category=Integer.MAX_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_066() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=0, category=Integer.MIN_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_067() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=1, category=1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_068() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=1, category=Integer.MAX_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_069() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=1, category=Integer.MIN_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_070() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=-1, category=0
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(-1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_071() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=-1, category=-1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(-1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_072() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}, series=Integer.MAX_VALUE, category=0
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getStartValue(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_073() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MAX_VALUE, category=-1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_074() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MAX_VALUE, category=Integer.MAX_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_075() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MAX_VALUE, category=Integer.MIN_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_076() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MIN_VALUE, category=0
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_077() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MIN_VALUE, category=1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStartValue_pairwise_078() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MIN_VALUE, category=-1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getStartValue(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_079() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series="sample_str", category="sample_str"
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue("sample_str", "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_080() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, series=Integer.valueOf(1), category="sample_str"
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getEndValue(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_081() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}, series="sample_str", category=Integer.valueOf(1)
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getEndValue("sample_str", Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_082() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}, series=Integer.valueOf(1), category=Integer.valueOf(1)
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getEndValue(Integer.valueOf(1), Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_083() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}, series="sample_str", category="sample_str"
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getEndValue("sample_str", "sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_084() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.valueOf(1), category="sample_str"
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(Integer.valueOf(1), "sample_str");
            fail("Expected org.jfree.data.UnknownKeyException");
        } catch (org.jfree.data.UnknownKeyException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_085() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=0, category=0
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(0, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_086() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, series=1, category=0
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getEndValue(1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_087() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}, series=-1, category=1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getEndValue(-1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_088() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}, series=Integer.MAX_VALUE, category=1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getEndValue(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_089() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=1, category=-1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_090() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, series=0, category=-1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getEndValue(0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_091() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MIN_VALUE, category=Integer.MAX_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_092() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, series=-1, category=Integer.MAX_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getEndValue(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_093() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=-1, category=Integer.MIN_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_094() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, series=Integer.MIN_VALUE, category=Integer.MIN_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getEndValue(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_095() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=0, category=1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(0, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_096() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=0, category=Integer.MAX_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_097() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=0, category=Integer.MIN_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(0, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_098() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=1, category=1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_099() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=1, category=Integer.MAX_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_100() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=1, category=Integer.MIN_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_101() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=-1, category=0
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(-1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_102() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=-1, category=-1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(-1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_103() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}, series=Integer.MAX_VALUE, category=0
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getEndValue(Integer.MAX_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_104() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MAX_VALUE, category=-1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_105() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MAX_VALUE, category=Integer.MAX_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_106() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MAX_VALUE, category=Integer.MIN_VALUE
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_107() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MIN_VALUE, category=0
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_108() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MIN_VALUE, category=1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEndValue_pairwise_109() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, series=Integer.MIN_VALUE, category=-1
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getEndValue(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCategoryIndex_pairwise_110() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, category="sample_str"
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getCategoryIndex("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getCategoryIndex_pairwise_111() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, category="sample_str"
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getCategoryIndex("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getCategoryIndex_pairwise_112() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}, category=Integer.valueOf(1)
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getCategoryIndex(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCategoryIndex_pairwise_113() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}, category=Integer.valueOf(1)
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getCategoryIndex(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnIndex_pairwise_114() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, columnKey="sample_str"
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getColumnIndex("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getColumnIndex_pairwise_115() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, columnKey="sample_str"
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getColumnIndex("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getColumnIndex_pairwise_116() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}, columnKey=Integer.valueOf(1)
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getColumnIndex(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnIndex_pairwise_117() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}, columnKey=Integer.valueOf(1)
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getColumnIndex(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowIndex_pairwise_118() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, rowKey="sample_str"
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getRowIndex("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRowIndex_pairwise_119() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}, rowKey=Integer.valueOf(1)
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getRowIndex(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowIndex_pairwise_120() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}, rowKey=Integer.valueOf(1)
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getRowIndex(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowIndex_pairwise_121() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, rowKey="sample_str"
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getRowIndex("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRowKeys_pairwise_122() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getRowKeys();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRowKeys_pairwise_123() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getRowKeys();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowKeys_pairwise_124() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getRowKeys();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowKeys_pairwise_125() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getRowKeys();
        assertNotNull(actual);
        assertEquals("java.util.Collections$UnmodifiableRandomAccessList", actual.getClass().getName());
        assertEquals("[Series 1]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getColumnCount_pairwise_126() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getColumnCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getColumnCount_pairwise_127() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getColumnCount();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnCount_pairwise_128() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getColumnCount();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnCount_pairwise_129() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getColumnCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRowCount_pairwise_130() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).getRowCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRowCount_pairwise_131() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).getRowCount();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowCount_pairwise_132() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).getRowCount();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowCount_pairwise_133() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).getRowCount();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_134() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, obj=new Object()
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_135() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, obj=new Object()
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_136() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {}, obj="sample_str"
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {})).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_137() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {{}}, obj="sample_str"
        try {
            (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {{}})).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_138() throws Exception {
        // Combination: receiver__starts=new double[][] {}, receiver__ends=new double[][] {}, obj=Integer.valueOf(1)
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {}, new double[][] {})).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_139() throws Exception {
        // Combination: receiver__starts=new double[][] {{}}, receiver__ends=new double[][] {{}}, obj=Integer.valueOf(1)
        Object actual = (new DefaultIntervalCategoryDataset(new double[][] {{}}, new double[][] {{}})).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

}
