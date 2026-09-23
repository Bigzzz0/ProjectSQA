package org.apache.commons.math.stat.descriptive.moment;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Variance.
 */
public class Variance_IPOTest {
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
    public void test_evaluate_pairwise_001() throws Exception {
        // Combination: values=new double[] {}, begin=0, length=0
        Object actual = (new Variance()).evaluate(new double[] {}, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_002() throws Exception {
        // Combination: values=new double[] {1}, begin=0, length=1
        Object actual = (new Variance()).evaluate(new double[] {1}, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_003() throws Exception {
        // Combination: values=new double[] {}, begin=0, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, 0, -1);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_004() throws Exception {
        // Combination: values=new double[] {}, begin=0, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, 0, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_005() throws Exception {
        // Combination: values=new double[] {}, begin=0, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, 0, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_006() throws Exception {
        // Combination: values=new double[] {1}, begin=1, length=0
        Object actual = (new Variance()).evaluate(new double[] {1}, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_007() throws Exception {
        // Combination: values=new double[] {}, begin=1, length=1
        try {
            (new Variance()).evaluate(new double[] {}, 1, 1);
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_008() throws Exception {
        // Combination: values=new double[] {1}, begin=1, length=-1
        try {
            (new Variance()).evaluate(new double[] {1}, 1, -1);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_009() throws Exception {
        // Combination: values=new double[] {1}, begin=1, length=Integer.MAX_VALUE
        Object actual = (new Variance()).evaluate(new double[] {1}, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_010() throws Exception {
        // Combination: values=new double[] {1}, begin=1, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {1}, 1, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_011() throws Exception {
        // Combination: values=new double[] {}, begin=-1, length=0
        try {
            (new Variance()).evaluate(new double[] {}, -1, 0);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_012() throws Exception {
        // Combination: values=new double[] {1}, begin=-1, length=1
        try {
            (new Variance()).evaluate(new double[] {1}, -1, 1);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_013() throws Exception {
        // Combination: values=new double[] {}, begin=-1, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, -1, -1);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_014() throws Exception {
        // Combination: values=new double[] {}, begin=-1, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, -1, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_015() throws Exception {
        // Combination: values=new double[] {}, begin=-1, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, -1, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_016() throws Exception {
        // Combination: values=new double[] {}, begin=Integer.MAX_VALUE, length=0
        try {
            (new Variance()).evaluate(new double[] {}, Integer.MAX_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_017() throws Exception {
        // Combination: values=new double[] {1}, begin=Integer.MAX_VALUE, length=1
        Object actual = (new Variance()).evaluate(new double[] {1}, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_018() throws Exception {
        // Combination: values=new double[] {}, begin=Integer.MAX_VALUE, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, Integer.MAX_VALUE, -1);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_019() throws Exception {
        // Combination: values=new double[] {}, begin=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        Object actual = (new Variance()).evaluate(new double[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_020() throws Exception {
        // Combination: values=new double[] {}, begin=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_021() throws Exception {
        // Combination: values=new double[] {}, begin=Integer.MIN_VALUE, length=0
        try {
            (new Variance()).evaluate(new double[] {}, Integer.MIN_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_022() throws Exception {
        // Combination: values=new double[] {1}, begin=Integer.MIN_VALUE, length=1
        try {
            (new Variance()).evaluate(new double[] {1}, Integer.MIN_VALUE, 1);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_023() throws Exception {
        // Combination: values=new double[] {}, begin=Integer.MIN_VALUE, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, Integer.MIN_VALUE, -1);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_024() throws Exception {
        // Combination: values=new double[] {}, begin=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_025() throws Exception {
        // Combination: values=new double[] {}, begin=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_026() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, begin=0, length=0
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 0, 0);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_027() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, begin=0, length=1
        Object actual = (new Variance()).evaluate(new double[] {1}, new double[] {1}, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_028() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {1}, begin=0, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {1}, 0, -1);
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_029() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, begin=0, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_030() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, begin=0, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 0, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_031() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {}, begin=1, length=0
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {}, 1, 0);
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_032() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, begin=1, length=1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_033() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {}, begin=1, length=-1
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {}, 1, -1);
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_034() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, begin=1, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {1}, 1, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_035() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, begin=1, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {1}, 1, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_036() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {1}, begin=-1, length=0
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {1}, -1, 0);
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_037() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {}, begin=-1, length=1
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {}, -1, 1);
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_038() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, begin=-1, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, -1, -1);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_039() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, begin=-1, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_040() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, begin=-1, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_041() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, begin=Integer.MAX_VALUE, length=0
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Integer.MAX_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_042() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, begin=Integer.MAX_VALUE, length=1
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {1}, Integer.MAX_VALUE, 1);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_043() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, begin=Integer.MAX_VALUE, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Integer.MAX_VALUE, -1);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_044() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, begin=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_045() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, begin=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_046() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, begin=Integer.MIN_VALUE, length=0
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Integer.MIN_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_047() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, begin=Integer.MIN_VALUE, length=1
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {1}, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_048() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, begin=Integer.MIN_VALUE, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_049() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, begin=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_050() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, begin=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_051() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {});
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_052() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {1}
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {1});
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_053() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {}
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {});
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_054() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}
        Object actual = (new Variance()).evaluate(new double[] {1}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_055() throws Exception {
        // Combination: values=new double[] {}, mean=0.0d, begin=0, length=0
        Object actual = (new Variance()).evaluate(new double[] {}, 0.0d, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_056() throws Exception {
        // Combination: values=new double[] {1}, mean=1.0d, begin=0, length=1
        Object actual = (new Variance()).evaluate(new double[] {1}, 1.0d, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_057() throws Exception {
        // Combination: values=new double[] {}, mean=-1.0d, begin=0, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, -1.0d, 0, -1);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_058() throws Exception {
        // Combination: values=new double[] {}, mean=Double.NaN, begin=0, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, Double.NaN, 0, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_059() throws Exception {
        // Combination: values=new double[] {}, mean=Double.POSITIVE_INFINITY, begin=0, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, Double.POSITIVE_INFINITY, 0, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_060() throws Exception {
        // Combination: values=new double[] {}, mean=1.0d, begin=1, length=0
        try {
            (new Variance()).evaluate(new double[] {}, 1.0d, 1, 0);
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_061() throws Exception {
        // Combination: values=new double[] {1}, mean=0.0d, begin=1, length=1
        try {
            (new Variance()).evaluate(new double[] {1}, 0.0d, 1, 1);
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_062() throws Exception {
        // Combination: values=new double[] {1}, mean=Double.NaN, begin=1, length=-1
        try {
            (new Variance()).evaluate(new double[] {1}, Double.NaN, 1, -1);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_063() throws Exception {
        // Combination: values=new double[] {1}, mean=-1.0d, begin=1, length=Integer.MAX_VALUE
        Object actual = (new Variance()).evaluate(new double[] {1}, -1.0d, 1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_064() throws Exception {
        // Combination: values=new double[] {1}, mean=0.0d, begin=1, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {1}, 0.0d, 1, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_065() throws Exception {
        // Combination: values=new double[] {1}, mean=-1.0d, begin=-1, length=0
        try {
            (new Variance()).evaluate(new double[] {1}, -1.0d, -1, 0);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_066() throws Exception {
        // Combination: values=new double[] {}, mean=Double.NaN, begin=-1, length=1
        try {
            (new Variance()).evaluate(new double[] {}, Double.NaN, -1, 1);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_067() throws Exception {
        // Combination: values=new double[] {}, mean=0.0d, begin=-1, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, 0.0d, -1, -1);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_068() throws Exception {
        // Combination: values=new double[] {}, mean=1.0d, begin=-1, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, 1.0d, -1, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_069() throws Exception {
        // Combination: values=new double[] {}, mean=1.0d, begin=-1, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, 1.0d, -1, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_070() throws Exception {
        // Combination: values=new double[] {}, mean=Double.NaN, begin=Integer.MAX_VALUE, length=0
        try {
            (new Variance()).evaluate(new double[] {}, Double.NaN, Integer.MAX_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_071() throws Exception {
        // Combination: values=new double[] {1}, mean=-1.0d, begin=Integer.MAX_VALUE, length=1
        Object actual = (new Variance()).evaluate(new double[] {1}, -1.0d, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_072() throws Exception {
        // Combination: values=new double[] {}, mean=1.0d, begin=Integer.MAX_VALUE, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, 1.0d, Integer.MAX_VALUE, -1);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_073() throws Exception {
        // Combination: values=new double[] {}, mean=0.0d, begin=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        Object actual = (new Variance()).evaluate(new double[] {}, 0.0d, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_074() throws Exception {
        // Combination: values=new double[] {}, mean=-1.0d, begin=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, -1.0d, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_075() throws Exception {
        // Combination: values=new double[] {1}, mean=Double.POSITIVE_INFINITY, begin=Integer.MIN_VALUE, length=0
        try {
            (new Variance()).evaluate(new double[] {1}, Double.POSITIVE_INFINITY, Integer.MIN_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_076() throws Exception {
        // Combination: values=new double[] {}, mean=0.0d, begin=Integer.MIN_VALUE, length=1
        try {
            (new Variance()).evaluate(new double[] {}, 0.0d, Integer.MIN_VALUE, 1);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_077() throws Exception {
        // Combination: values=new double[] {}, mean=1.0d, begin=Integer.MIN_VALUE, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, 1.0d, Integer.MIN_VALUE, -1);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_078() throws Exception {
        // Combination: values=new double[] {}, mean=-1.0d, begin=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, -1.0d, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_079() throws Exception {
        // Combination: values=new double[] {}, mean=Double.NaN, begin=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, Double.NaN, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_080() throws Exception {
        // Combination: values=new double[] {}, mean=Double.POSITIVE_INFINITY, begin=1, length=1
        try {
            (new Variance()).evaluate(new double[] {}, Double.POSITIVE_INFINITY, 1, 1);
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_081() throws Exception {
        // Combination: values=new double[] {}, mean=Double.POSITIVE_INFINITY, begin=-1, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, Double.POSITIVE_INFINITY, -1, -1);
            fail("Expected org.apache.commons.math.exception.NotPositiveException");
        } catch (org.apache.commons.math.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_082() throws Exception {
        // Combination: values=new double[] {}, mean=Double.POSITIVE_INFINITY, begin=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        Object actual = (new Variance()).evaluate(new double[] {}, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_083() throws Exception {
        // Combination: values=new double[] {}, mean=0.0d
        Object actual = (new Variance()).evaluate(new double[] {}, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_084() throws Exception {
        // Combination: values=new double[] {1}, mean=0.0d
        Object actual = (new Variance()).evaluate(new double[] {1}, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_085() throws Exception {
        // Combination: values=new double[] {}, mean=1.0d
        Object actual = (new Variance()).evaluate(new double[] {}, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_086() throws Exception {
        // Combination: values=new double[] {1}, mean=1.0d
        Object actual = (new Variance()).evaluate(new double[] {1}, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_087() throws Exception {
        // Combination: values=new double[] {}, mean=-1.0d
        Object actual = (new Variance()).evaluate(new double[] {}, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_088() throws Exception {
        // Combination: values=new double[] {1}, mean=-1.0d
        Object actual = (new Variance()).evaluate(new double[] {1}, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_089() throws Exception {
        // Combination: values=new double[] {}, mean=Double.NaN
        Object actual = (new Variance()).evaluate(new double[] {}, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_090() throws Exception {
        // Combination: values=new double[] {1}, mean=Double.NaN
        Object actual = (new Variance()).evaluate(new double[] {1}, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_091() throws Exception {
        // Combination: values=new double[] {}, mean=Double.POSITIVE_INFINITY
        Object actual = (new Variance()).evaluate(new double[] {}, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_092() throws Exception {
        // Combination: values=new double[] {1}, mean=Double.POSITIVE_INFINITY
        Object actual = (new Variance()).evaluate(new double[] {1}, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_093() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=0.0d, begin=0, length=0
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 0.0d, 0, 0);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_094() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, mean=1.0d, begin=0, length=1
        Object actual = (new Variance()).evaluate(new double[] {1}, new double[] {1}, 1.0d, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_095() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {1}, mean=-1.0d, begin=0, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {1}, -1.0d, 0, -1);
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_096() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.NaN, begin=0, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.NaN, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_097() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.POSITIVE_INFINITY, begin=0, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.POSITIVE_INFINITY, 0, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_098() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=1.0d, begin=1, length=0
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 1.0d, 1, 0);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_099() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {}, mean=0.0d, begin=1, length=1
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {}, 0.0d, 1, 1);
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_100() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, mean=Double.NaN, begin=1, length=-1
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {1}, Double.NaN, 1, -1);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_101() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {}, mean=-1.0d, begin=1, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {}, -1.0d, 1, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_102() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, mean=0.0d, begin=1, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {1}, 0.0d, 1, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_103() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, mean=-1.0d, begin=-1, length=0
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {1}, -1.0d, -1, 0);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_104() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.NaN, begin=-1, length=1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.NaN, -1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_105() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=0.0d, begin=-1, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 0.0d, -1, -1);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_106() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {1}, mean=1.0d, begin=-1, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {1}, 1.0d, -1, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_107() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=1.0d, begin=-1, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 1.0d, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_108() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.NaN, begin=Integer.MAX_VALUE, length=0
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.NaN, Integer.MAX_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_109() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, mean=-1.0d, begin=Integer.MAX_VALUE, length=1
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {1}, -1.0d, Integer.MAX_VALUE, 1);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_110() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=1.0d, begin=Integer.MAX_VALUE, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 1.0d, Integer.MAX_VALUE, -1);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_111() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=0.0d, begin=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 0.0d, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_112() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=-1.0d, begin=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, -1.0d, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_113() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, mean=Double.POSITIVE_INFINITY, begin=Integer.MIN_VALUE, length=0
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {1}, Double.POSITIVE_INFINITY, Integer.MIN_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_114() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=0.0d, begin=Integer.MIN_VALUE, length=1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 0.0d, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_115() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=1.0d, begin=Integer.MIN_VALUE, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 1.0d, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_116() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=-1.0d, begin=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, -1.0d, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_117() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.NaN, begin=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.NaN, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_118() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.POSITIVE_INFINITY, begin=1, length=1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.POSITIVE_INFINITY, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_119() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.POSITIVE_INFINITY, begin=-1, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.POSITIVE_INFINITY, -1, -1);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_120() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.POSITIVE_INFINITY, begin=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_121() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=0.0d
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 0.0d);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_122() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, mean=0.0d
        Object actual = (new Variance()).evaluate(new double[] {1}, new double[] {1}, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_123() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {1}, mean=1.0d
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {1}, 1.0d);
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_124() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {}, mean=1.0d
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {}, 1.0d);
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_125() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=-1.0d
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, -1.0d);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_126() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, mean=-1.0d
        Object actual = (new Variance()).evaluate(new double[] {1}, new double[] {1}, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_127() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.NaN
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.NaN);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_128() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, mean=Double.NaN
        Object actual = (new Variance()).evaluate(new double[] {1}, new double[] {1}, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_129() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.POSITIVE_INFINITY
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.POSITIVE_INFINITY);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_130() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, mean=Double.POSITIVE_INFINITY
        Object actual = (new Variance()).evaluate(new double[] {1}, new double[] {1}, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

}
