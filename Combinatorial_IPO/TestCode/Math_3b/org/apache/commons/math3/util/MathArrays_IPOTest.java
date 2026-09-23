package org.apache.commons.math3.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for MathArrays.
 */
public class MathArrays_IPOTest {
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
    public void test_scale_pairwise_001() throws Exception {
        // Combination: val=0.0d, arr=new double[] {}
        Object actual = MathArrays.scale(0.0d, new double[] {});
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scale_pairwise_002() throws Exception {
        // Combination: val=1.0d, arr=new double[] {}
        Object actual = MathArrays.scale(1.0d, new double[] {});
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scale_pairwise_003() throws Exception {
        // Combination: val=-1.0d, arr=new double[] {}
        Object actual = MathArrays.scale(-1.0d, new double[] {});
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scale_pairwise_004() throws Exception {
        // Combination: val=Double.NaN, arr=new double[] {}
        Object actual = MathArrays.scale(Double.NaN, new double[] {});
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scale_pairwise_005() throws Exception {
        // Combination: val=Double.POSITIVE_INFINITY, arr=new double[] {}
        Object actual = MathArrays.scale(Double.POSITIVE_INFINITY, new double[] {});
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scale_pairwise_006() throws Exception {
        // Combination: val=0.0d, arr=new double[] {1}
        Object actual = MathArrays.scale(0.0d, new double[] {1});
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[0.0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scale_pairwise_007() throws Exception {
        // Combination: val=1.0d, arr=new double[] {1}
        Object actual = MathArrays.scale(1.0d, new double[] {1});
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[1.0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scale_pairwise_008() throws Exception {
        // Combination: val=-1.0d, arr=new double[] {1}
        Object actual = MathArrays.scale(-1.0d, new double[] {1});
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[-1.0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scale_pairwise_009() throws Exception {
        // Combination: val=Double.NaN, arr=new double[] {1}
        Object actual = MathArrays.scale(Double.NaN, new double[] {1});
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[NaN]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scale_pairwise_010() throws Exception {
        // Combination: val=Double.POSITIVE_INFINITY, arr=new double[] {1}
        Object actual = MathArrays.scale(Double.POSITIVE_INFINITY, new double[] {1});
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[Infinity]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distance1_pairwise_011() throws Exception {
        // Combination: p1=new double[] {}, p2=new double[] {}
        Object actual = MathArrays.distance1(new double[] {}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distance1_pairwise_012() throws Exception {
        // Combination: p1=new double[] {}, p2=new double[] {1}
        Object actual = MathArrays.distance1(new double[] {}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distance1_pairwise_013() throws Exception {
        // Combination: p1=new double[] {1}, p2=new double[] {}
        try {
            MathArrays.distance1(new double[] {1}, new double[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_distance1_pairwise_014() throws Exception {
        // Combination: p1=new double[] {1}, p2=new double[] {1}
        Object actual = MathArrays.distance1(new double[] {1}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distance1_pairwise_015() throws Exception {
        // Combination: p1=new int[] {}, p2=new int[] {}
        Object actual = MathArrays.distance1(new int[] {}, new int[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distance1_pairwise_016() throws Exception {
        // Combination: p1=new int[] {}, p2=new int[] {1}
        Object actual = MathArrays.distance1(new int[] {}, new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distance1_pairwise_017() throws Exception {
        // Combination: p1=new int[] {1}, p2=new int[] {}
        try {
            MathArrays.distance1(new int[] {1}, new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_distance1_pairwise_018() throws Exception {
        // Combination: p1=new int[] {1}, p2=new int[] {1}
        Object actual = MathArrays.distance1(new int[] {1}, new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_019() throws Exception {
        // Combination: p1=new double[] {}, p2=new double[] {}
        Object actual = MathArrays.distance(new double[] {}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_020() throws Exception {
        // Combination: p1=new double[] {}, p2=new double[] {1}
        Object actual = MathArrays.distance(new double[] {}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_021() throws Exception {
        // Combination: p1=new double[] {1}, p2=new double[] {}
        try {
            MathArrays.distance(new double[] {1}, new double[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_022() throws Exception {
        // Combination: p1=new double[] {1}, p2=new double[] {1}
        Object actual = MathArrays.distance(new double[] {1}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_023() throws Exception {
        // Combination: p1=new int[] {}, p2=new int[] {}
        Object actual = MathArrays.distance(new int[] {}, new int[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_024() throws Exception {
        // Combination: p1=new int[] {}, p2=new int[] {1}
        Object actual = MathArrays.distance(new int[] {}, new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_025() throws Exception {
        // Combination: p1=new int[] {1}, p2=new int[] {}
        try {
            MathArrays.distance(new int[] {1}, new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_026() throws Exception {
        // Combination: p1=new int[] {1}, p2=new int[] {1}
        Object actual = MathArrays.distance(new int[] {1}, new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distanceInf_pairwise_027() throws Exception {
        // Combination: p1=new double[] {}, p2=new double[] {}
        Object actual = MathArrays.distanceInf(new double[] {}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distanceInf_pairwise_028() throws Exception {
        // Combination: p1=new double[] {}, p2=new double[] {1}
        Object actual = MathArrays.distanceInf(new double[] {}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distanceInf_pairwise_029() throws Exception {
        // Combination: p1=new double[] {1}, p2=new double[] {}
        try {
            MathArrays.distanceInf(new double[] {1}, new double[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_distanceInf_pairwise_030() throws Exception {
        // Combination: p1=new double[] {1}, p2=new double[] {1}
        Object actual = MathArrays.distanceInf(new double[] {1}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distanceInf_pairwise_031() throws Exception {
        // Combination: p1=new int[] {}, p2=new int[] {}
        Object actual = MathArrays.distanceInf(new int[] {}, new int[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distanceInf_pairwise_032() throws Exception {
        // Combination: p1=new int[] {}, p2=new int[] {1}
        Object actual = MathArrays.distanceInf(new int[] {}, new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_distanceInf_pairwise_033() throws Exception {
        // Combination: p1=new int[] {1}, p2=new int[] {}
        try {
            MathArrays.distanceInf(new int[] {1}, new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_distanceInf_pairwise_034() throws Exception {
        // Combination: p1=new int[] {1}, p2=new int[] {1}
        Object actual = MathArrays.distanceInf(new int[] {1}, new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_035() throws Exception {
        // Combination: source=new int[] {}, len=0
        Object actual = MathArrays.copyOf(new int[] {}, 0);
        assertNotNull(actual);
        assertEquals("[I", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_036() throws Exception {
        // Combination: source=new int[] {1}, len=0
        Object actual = MathArrays.copyOf(new int[] {1}, 0);
        assertNotNull(actual);
        assertEquals("[I", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_037() throws Exception {
        // Combination: source=new int[] {}, len=1
        Object actual = MathArrays.copyOf(new int[] {}, 1);
        assertNotNull(actual);
        assertEquals("[I", actual.getClass().getName());
        assertEquals("[0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_038() throws Exception {
        // Combination: source=new int[] {1}, len=1
        Object actual = MathArrays.copyOf(new int[] {1}, 1);
        assertNotNull(actual);
        assertEquals("[I", actual.getClass().getName());
        assertEquals("[1]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_039() throws Exception {
        // Combination: source=new int[] {}, len=-1
        try {
            MathArrays.copyOf(new int[] {}, -1);
            fail("Expected java.lang.NegativeArraySizeException");
        } catch (java.lang.NegativeArraySizeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_040() throws Exception {
        // Combination: source=new int[] {1}, len=-1
        try {
            MathArrays.copyOf(new int[] {1}, -1);
            fail("Expected java.lang.NegativeArraySizeException");
        } catch (java.lang.NegativeArraySizeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_041() throws Exception {
        // Combination: source=new int[] {}, len=Integer.MAX_VALUE
        try {
            MathArrays.copyOf(new int[] {}, Integer.MAX_VALUE);
            fail("Expected java.lang.OutOfMemoryError");
        } catch (java.lang.OutOfMemoryError expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_042() throws Exception {
        // Combination: source=new int[] {1}, len=Integer.MAX_VALUE
        try {
            MathArrays.copyOf(new int[] {1}, Integer.MAX_VALUE);
            fail("Expected java.lang.OutOfMemoryError");
        } catch (java.lang.OutOfMemoryError expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_043() throws Exception {
        // Combination: source=new int[] {}, len=Integer.MIN_VALUE
        try {
            MathArrays.copyOf(new int[] {}, Integer.MIN_VALUE);
            fail("Expected java.lang.NegativeArraySizeException");
        } catch (java.lang.NegativeArraySizeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_044() throws Exception {
        // Combination: source=new int[] {1}, len=Integer.MIN_VALUE
        try {
            MathArrays.copyOf(new int[] {1}, Integer.MIN_VALUE);
            fail("Expected java.lang.NegativeArraySizeException");
        } catch (java.lang.NegativeArraySizeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_045() throws Exception {
        // Combination: source=new double[] {}, len=0
        Object actual = MathArrays.copyOf(new double[] {}, 0);
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_046() throws Exception {
        // Combination: source=new double[] {1}, len=0
        Object actual = MathArrays.copyOf(new double[] {1}, 0);
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_047() throws Exception {
        // Combination: source=new double[] {}, len=1
        Object actual = MathArrays.copyOf(new double[] {}, 1);
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[0.0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_048() throws Exception {
        // Combination: source=new double[] {1}, len=1
        Object actual = MathArrays.copyOf(new double[] {1}, 1);
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[1.0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_049() throws Exception {
        // Combination: source=new double[] {}, len=-1
        try {
            MathArrays.copyOf(new double[] {}, -1);
            fail("Expected java.lang.NegativeArraySizeException");
        } catch (java.lang.NegativeArraySizeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_050() throws Exception {
        // Combination: source=new double[] {1}, len=-1
        try {
            MathArrays.copyOf(new double[] {1}, -1);
            fail("Expected java.lang.NegativeArraySizeException");
        } catch (java.lang.NegativeArraySizeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_051() throws Exception {
        // Combination: source=new double[] {}, len=Integer.MAX_VALUE
        try {
            MathArrays.copyOf(new double[] {}, Integer.MAX_VALUE);
            fail("Expected java.lang.OutOfMemoryError");
        } catch (java.lang.OutOfMemoryError expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_052() throws Exception {
        // Combination: source=new double[] {1}, len=Integer.MAX_VALUE
        try {
            MathArrays.copyOf(new double[] {1}, Integer.MAX_VALUE);
            fail("Expected java.lang.OutOfMemoryError");
        } catch (java.lang.OutOfMemoryError expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_053() throws Exception {
        // Combination: source=new double[] {}, len=Integer.MIN_VALUE
        try {
            MathArrays.copyOf(new double[] {}, Integer.MIN_VALUE);
            fail("Expected java.lang.NegativeArraySizeException");
        } catch (java.lang.NegativeArraySizeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_copyOf_pairwise_054() throws Exception {
        // Combination: source=new double[] {1}, len=Integer.MIN_VALUE
        try {
            MathArrays.copyOf(new double[] {1}, Integer.MIN_VALUE);
            fail("Expected java.lang.NegativeArraySizeException");
        } catch (java.lang.NegativeArraySizeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_055() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=0.0d, b2=0.0d
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_056() throws Exception {
        // Combination: a1=0.0d, b1=1.0d, a2=1.0d, b2=1.0d
        Object actual = MathArrays.linearCombination(0.0d, 1.0d, 1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_057() throws Exception {
        // Combination: a1=0.0d, b1=-1.0d, a2=-1.0d, b2=-1.0d
        Object actual = MathArrays.linearCombination(0.0d, -1.0d, -1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_058() throws Exception {
        // Combination: a1=0.0d, b1=Double.NaN, a2=Double.NaN, b2=Double.NaN
        Object actual = MathArrays.linearCombination(0.0d, Double.NaN, Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_059() throws Exception {
        // Combination: a1=0.0d, b1=Double.POSITIVE_INFINITY, a2=Double.POSITIVE_INFINITY, b2=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(0.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_060() throws Exception {
        // Combination: a1=1.0d, b1=1.0d, a2=0.0d, b2=-1.0d
        Object actual = MathArrays.linearCombination(1.0d, 1.0d, 0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_061() throws Exception {
        // Combination: a1=1.0d, b1=0.0d, a2=1.0d, b2=Double.NaN
        Object actual = MathArrays.linearCombination(1.0d, 0.0d, 1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_062() throws Exception {
        // Combination: a1=1.0d, b1=Double.NaN, a2=-1.0d, b2=0.0d
        Object actual = MathArrays.linearCombination(1.0d, Double.NaN, -1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_063() throws Exception {
        // Combination: a1=1.0d, b1=-1.0d, a2=Double.NaN, b2=1.0d
        Object actual = MathArrays.linearCombination(1.0d, -1.0d, Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_064() throws Exception {
        // Combination: a1=1.0d, b1=0.0d, a2=Double.POSITIVE_INFINITY, b2=1.0d
        Object actual = MathArrays.linearCombination(1.0d, 0.0d, Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_065() throws Exception {
        // Combination: a1=-1.0d, b1=-1.0d, a2=0.0d, b2=Double.NaN
        Object actual = MathArrays.linearCombination(-1.0d, -1.0d, 0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_066() throws Exception {
        // Combination: a1=-1.0d, b1=Double.NaN, a2=1.0d, b2=-1.0d
        Object actual = MathArrays.linearCombination(-1.0d, Double.NaN, 1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_067() throws Exception {
        // Combination: a1=-1.0d, b1=0.0d, a2=-1.0d, b2=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(-1.0d, 0.0d, -1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_068() throws Exception {
        // Combination: a1=-1.0d, b1=1.0d, a2=Double.NaN, b2=0.0d
        Object actual = MathArrays.linearCombination(-1.0d, 1.0d, Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_069() throws Exception {
        // Combination: a1=-1.0d, b1=1.0d, a2=Double.POSITIVE_INFINITY, b2=Double.NaN
        Object actual = MathArrays.linearCombination(-1.0d, 1.0d, Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_070() throws Exception {
        // Combination: a1=Double.NaN, b1=Double.NaN, a2=0.0d, b2=1.0d
        Object actual = MathArrays.linearCombination(Double.NaN, Double.NaN, 0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_071() throws Exception {
        // Combination: a1=Double.NaN, b1=-1.0d, a2=1.0d, b2=0.0d
        Object actual = MathArrays.linearCombination(Double.NaN, -1.0d, 1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_072() throws Exception {
        // Combination: a1=Double.NaN, b1=1.0d, a2=-1.0d, b2=Double.NaN
        Object actual = MathArrays.linearCombination(Double.NaN, 1.0d, -1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_073() throws Exception {
        // Combination: a1=Double.NaN, b1=0.0d, a2=Double.NaN, b2=-1.0d
        Object actual = MathArrays.linearCombination(Double.NaN, 0.0d, Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_074() throws Exception {
        // Combination: a1=Double.NaN, b1=-1.0d, a2=Double.POSITIVE_INFINITY, b2=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(Double.NaN, -1.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_075() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=Double.POSITIVE_INFINITY, a2=0.0d, b2=0.0d
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_076() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=0.0d, a2=1.0d, b2=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, 0.0d, 1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_077() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=1.0d, a2=-1.0d, b2=1.0d
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, 1.0d, -1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_078() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=-1.0d, a2=Double.NaN, b2=-1.0d
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, -1.0d, Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_079() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=Double.NaN, a2=Double.POSITIVE_INFINITY, b2=0.0d
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, Double.NaN, Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_080() throws Exception {
        // Combination: a1=1.0d, b1=Double.POSITIVE_INFINITY, a2=1.0d, b2=1.0d
        Object actual = MathArrays.linearCombination(1.0d, Double.POSITIVE_INFINITY, 1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_081() throws Exception {
        // Combination: a1=-1.0d, b1=Double.POSITIVE_INFINITY, a2=-1.0d, b2=1.0d
        Object actual = MathArrays.linearCombination(-1.0d, Double.POSITIVE_INFINITY, -1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_082() throws Exception {
        // Combination: a1=Double.NaN, b1=Double.POSITIVE_INFINITY, a2=Double.NaN, b2=-1.0d
        Object actual = MathArrays.linearCombination(Double.NaN, Double.POSITIVE_INFINITY, Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_083() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=Double.POSITIVE_INFINITY, b2=-1.0d
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_084() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=Double.POSITIVE_INFINITY, a2=0.0d, b2=Double.NaN
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_085() throws Exception {
        // Combination: a1=1.0d, b1=1.0d, a2=0.0d, b2=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(1.0d, 1.0d, 0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_086() throws Exception {
        // Combination: a1=0.0d, b1=Double.NaN, a2=Double.NaN, b2=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(0.0d, Double.NaN, Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_087() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=0.0d, b2=0.0d, a3=0.0d, b3=0.0d
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_088() throws Exception {
        // Combination: a1=0.0d, b1=1.0d, a2=1.0d, b2=1.0d, a3=1.0d, b3=1.0d
        Object actual = MathArrays.linearCombination(0.0d, 1.0d, 1.0d, 1.0d, 1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("2.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_089() throws Exception {
        // Combination: a1=0.0d, b1=-1.0d, a2=-1.0d, b2=-1.0d, a3=-1.0d, b3=-1.0d
        Object actual = MathArrays.linearCombination(0.0d, -1.0d, -1.0d, -1.0d, -1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("2.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_090() throws Exception {
        // Combination: a1=0.0d, b1=Double.NaN, a2=Double.NaN, b2=Double.NaN, a3=Double.NaN, b3=Double.NaN
        Object actual = MathArrays.linearCombination(0.0d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_091() throws Exception {
        // Combination: a1=0.0d, b1=Double.POSITIVE_INFINITY, a2=Double.POSITIVE_INFINITY, b2=Double.POSITIVE_INFINITY, a3=Double.POSITIVE_INFINITY, b3=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(0.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_092() throws Exception {
        // Combination: a1=1.0d, b1=-1.0d, a2=0.0d, b2=Double.NaN, a3=1.0d, b3=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(1.0d, -1.0d, 0.0d, Double.NaN, 1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_093() throws Exception {
        // Combination: a1=1.0d, b1=Double.NaN, a2=1.0d, b2=-1.0d, a3=0.0d, b3=0.0d
        Object actual = MathArrays.linearCombination(1.0d, Double.NaN, 1.0d, -1.0d, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_094() throws Exception {
        // Combination: a1=1.0d, b1=0.0d, a2=-1.0d, b2=1.0d, a3=Double.NaN, b3=1.0d
        Object actual = MathArrays.linearCombination(1.0d, 0.0d, -1.0d, 1.0d, Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_095() throws Exception {
        // Combination: a1=1.0d, b1=1.0d, a2=Double.NaN, b2=0.0d, a3=-1.0d, b3=-1.0d
        Object actual = MathArrays.linearCombination(1.0d, 1.0d, Double.NaN, 0.0d, -1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_096() throws Exception {
        // Combination: a1=1.0d, b1=1.0d, a2=Double.POSITIVE_INFINITY, b2=Double.NaN, a3=0.0d, b3=Double.NaN
        Object actual = MathArrays.linearCombination(1.0d, 1.0d, Double.POSITIVE_INFINITY, Double.NaN, 0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_097() throws Exception {
        // Combination: a1=-1.0d, b1=Double.NaN, a2=0.0d, b2=1.0d, a3=-1.0d, b3=1.0d
        Object actual = MathArrays.linearCombination(-1.0d, Double.NaN, 0.0d, 1.0d, -1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_098() throws Exception {
        // Combination: a1=-1.0d, b1=-1.0d, a2=1.0d, b2=0.0d, a3=Double.NaN, b3=Double.NaN
        Object actual = MathArrays.linearCombination(-1.0d, -1.0d, 1.0d, 0.0d, Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_099() throws Exception {
        // Combination: a1=-1.0d, b1=Double.POSITIVE_INFINITY, a2=-1.0d, b2=Double.NaN, a3=0.0d, b3=0.0d
        Object actual = MathArrays.linearCombination(-1.0d, Double.POSITIVE_INFINITY, -1.0d, Double.NaN, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_100() throws Exception {
        // Combination: a1=-1.0d, b1=0.0d, a2=Double.NaN, b2=-1.0d, a3=1.0d, b3=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(-1.0d, 0.0d, Double.NaN, -1.0d, 1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_101() throws Exception {
        // Combination: a1=-1.0d, b1=Double.NaN, a2=Double.POSITIVE_INFINITY, b2=0.0d, a3=1.0d, b3=-1.0d
        Object actual = MathArrays.linearCombination(-1.0d, Double.NaN, Double.POSITIVE_INFINITY, 0.0d, 1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_102() throws Exception {
        // Combination: a1=Double.NaN, b1=1.0d, a2=0.0d, b2=-1.0d, a3=Double.NaN, b3=0.0d
        Object actual = MathArrays.linearCombination(Double.NaN, 1.0d, 0.0d, -1.0d, Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_103() throws Exception {
        // Combination: a1=Double.NaN, b1=0.0d, a2=1.0d, b2=Double.NaN, a3=-1.0d, b3=-1.0d
        Object actual = MathArrays.linearCombination(Double.NaN, 0.0d, 1.0d, Double.NaN, -1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_104() throws Exception {
        // Combination: a1=Double.NaN, b1=Double.NaN, a2=-1.0d, b2=Double.POSITIVE_INFINITY, a3=1.0d, b3=Double.NaN
        Object actual = MathArrays.linearCombination(Double.NaN, Double.NaN, -1.0d, Double.POSITIVE_INFINITY, 1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_105() throws Exception {
        // Combination: a1=Double.NaN, b1=-1.0d, a2=Double.NaN, b2=1.0d, a3=0.0d, b3=1.0d
        Object actual = MathArrays.linearCombination(Double.NaN, -1.0d, Double.NaN, 1.0d, 0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_106() throws Exception {
        // Combination: a1=Double.NaN, b1=Double.POSITIVE_INFINITY, a2=Double.POSITIVE_INFINITY, b2=0.0d, a3=-1.0d, b3=1.0d
        Object actual = MathArrays.linearCombination(Double.NaN, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d, -1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_107() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=0.0d, a2=0.0d, b2=Double.POSITIVE_INFINITY, a3=Double.POSITIVE_INFINITY, b3=-1.0d
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, 0.0d, 0.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_108() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=Double.POSITIVE_INFINITY, a2=1.0d, b2=1.0d, a3=0.0d, b3=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d, 1.0d, 0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_109() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=1.0d, a2=-1.0d, b2=0.0d, a3=1.0d, b3=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, 1.0d, -1.0d, 0.0d, 1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_110() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=-1.0d, a2=Double.NaN, b2=Double.POSITIVE_INFINITY, a3=-1.0d, b3=0.0d
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, -1.0d, Double.NaN, Double.POSITIVE_INFINITY, -1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_111() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=0.0d, a2=Double.POSITIVE_INFINITY, b2=-1.0d, a3=Double.NaN, b3=Double.NaN
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, 0.0d, Double.POSITIVE_INFINITY, -1.0d, Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_112() throws Exception {
        // Combination: a1=1.0d, b1=1.0d, a2=1.0d, b2=Double.POSITIVE_INFINITY, a3=Double.POSITIVE_INFINITY, b3=1.0d
        Object actual = MathArrays.linearCombination(1.0d, 1.0d, 1.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_113() throws Exception {
        // Combination: a1=-1.0d, b1=1.0d, a2=-1.0d, b2=0.0d, a3=Double.POSITIVE_INFINITY, b3=0.0d
        Object actual = MathArrays.linearCombination(-1.0d, 1.0d, -1.0d, 0.0d, Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_114() throws Exception {
        // Combination: a1=Double.NaN, b1=-1.0d, a2=Double.NaN, b2=1.0d, a3=Double.POSITIVE_INFINITY, b3=Double.NaN
        Object actual = MathArrays.linearCombination(Double.NaN, -1.0d, Double.NaN, 1.0d, Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_115() throws Exception {
        // Combination: a1=0.0d, b1=-1.0d, a2=Double.POSITIVE_INFINITY, b2=1.0d, a3=0.0d, b3=0.0d
        Object actual = MathArrays.linearCombination(0.0d, -1.0d, Double.POSITIVE_INFINITY, 1.0d, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_116() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=Double.NaN, a2=0.0d, b2=Double.NaN, a3=Double.POSITIVE_INFINITY, b3=1.0d
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, Double.NaN, 0.0d, Double.NaN, Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_117() throws Exception {
        // Combination: a1=1.0d, b1=Double.POSITIVE_INFINITY, a2=0.0d, b2=-1.0d, a3=1.0d, b3=Double.NaN
        Object actual = MathArrays.linearCombination(1.0d, Double.POSITIVE_INFINITY, 0.0d, -1.0d, 1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_118() throws Exception {
        // Combination: a1=0.0d, b1=Double.POSITIVE_INFINITY, a2=Double.NaN, b2=Double.POSITIVE_INFINITY, a3=Double.NaN, b3=-1.0d
        Object actual = MathArrays.linearCombination(0.0d, Double.POSITIVE_INFINITY, Double.NaN, Double.POSITIVE_INFINITY, Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_119() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=0.0d, b2=-1.0d, a3=Double.POSITIVE_INFINITY, b3=1.0d
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, 0.0d, -1.0d, Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_120() throws Exception {
        // Combination: a1=-1.0d, b1=0.0d, a2=0.0d, b2=Double.POSITIVE_INFINITY, a3=0.0d, b3=-1.0d
        Object actual = MathArrays.linearCombination(-1.0d, 0.0d, 0.0d, Double.POSITIVE_INFINITY, 0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_121() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=0.0d, b2=0.0d, a3=1.0d, b3=0.0d
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, 0.0d, 0.0d, 1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_122() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=0.0d, b2=1.0d, a3=0.0d, b3=-1.0d
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, 0.0d, 1.0d, 0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_123() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=0.0d, b2=0.0d, a3=-1.0d, b3=Double.NaN
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, 0.0d, 0.0d, -1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_124() throws Exception {
        // Combination: a1=Double.NaN, b1=Double.NaN, a2=0.0d, b2=0.0d, a3=-1.0d, b3=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(Double.NaN, Double.NaN, 0.0d, 0.0d, -1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_125() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=0.0d, b2=0.0d, a3=Double.NaN, b3=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, 0.0d, 0.0d, Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_126() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=0.0d, b2=0.0d, a3=0.0d, b3=0.0d, a4=0.0d, b4=0.0d
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_127() throws Exception {
        // Combination: a1=0.0d, b1=1.0d, a2=1.0d, b2=1.0d, a3=1.0d, b3=1.0d, a4=1.0d, b4=1.0d
        Object actual = MathArrays.linearCombination(0.0d, 1.0d, 1.0d, 1.0d, 1.0d, 1.0d, 1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("3.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_128() throws Exception {
        // Combination: a1=0.0d, b1=-1.0d, a2=-1.0d, b2=-1.0d, a3=-1.0d, b3=-1.0d, a4=-1.0d, b4=-1.0d
        Object actual = MathArrays.linearCombination(0.0d, -1.0d, -1.0d, -1.0d, -1.0d, -1.0d, -1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("3.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_129() throws Exception {
        // Combination: a1=0.0d, b1=Double.NaN, a2=Double.NaN, b2=Double.NaN, a3=Double.NaN, b3=Double.NaN, a4=Double.NaN, b4=Double.NaN
        Object actual = MathArrays.linearCombination(0.0d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_130() throws Exception {
        // Combination: a1=0.0d, b1=Double.POSITIVE_INFINITY, a2=Double.POSITIVE_INFINITY, b2=Double.POSITIVE_INFINITY, a3=Double.POSITIVE_INFINITY, b3=Double.POSITIVE_INFINITY, a4=Double.POSITIVE_INFINITY, b4=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(0.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_131() throws Exception {
        // Combination: a1=1.0d, b1=Double.NaN, a2=0.0d, b2=Double.POSITIVE_INFINITY, a3=1.0d, b3=0.0d, a4=-1.0d, b4=1.0d
        Object actual = MathArrays.linearCombination(1.0d, Double.NaN, 0.0d, Double.POSITIVE_INFINITY, 1.0d, 0.0d, -1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_132() throws Exception {
        // Combination: a1=1.0d, b1=-1.0d, a2=1.0d, b2=0.0d, a3=0.0d, b3=Double.POSITIVE_INFINITY, a4=Double.NaN, b4=-1.0d
        Object actual = MathArrays.linearCombination(1.0d, -1.0d, 1.0d, 0.0d, 0.0d, Double.POSITIVE_INFINITY, Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_133() throws Exception {
        // Combination: a1=1.0d, b1=1.0d, a2=-1.0d, b2=1.0d, a3=Double.NaN, b3=-1.0d, a4=0.0d, b4=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(1.0d, 1.0d, -1.0d, 1.0d, Double.NaN, -1.0d, 0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_134() throws Exception {
        // Combination: a1=1.0d, b1=0.0d, a2=Double.NaN, b2=-1.0d, a3=-1.0d, b3=1.0d, a4=1.0d, b4=0.0d
        Object actual = MathArrays.linearCombination(1.0d, 0.0d, Double.NaN, -1.0d, -1.0d, 1.0d, 1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_135() throws Exception {
        // Combination: a1=1.0d, b1=Double.NaN, a2=Double.POSITIVE_INFINITY, b2=Double.NaN, a3=0.0d, b3=-1.0d, a4=1.0d, b4=Double.NaN
        Object actual = MathArrays.linearCombination(1.0d, Double.NaN, Double.POSITIVE_INFINITY, Double.NaN, 0.0d, -1.0d, 1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_136() throws Exception {
        // Combination: a1=-1.0d, b1=1.0d, a2=0.0d, b2=1.0d, a3=-1.0d, b3=0.0d, a4=Double.NaN, b4=Double.NaN
        Object actual = MathArrays.linearCombination(-1.0d, 1.0d, 0.0d, 1.0d, -1.0d, 0.0d, Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_137() throws Exception {
        // Combination: a1=-1.0d, b1=0.0d, a2=1.0d, b2=Double.NaN, a3=Double.NaN, b3=Double.POSITIVE_INFINITY, a4=-1.0d, b4=0.0d
        Object actual = MathArrays.linearCombination(-1.0d, 0.0d, 1.0d, Double.NaN, Double.NaN, Double.POSITIVE_INFINITY, -1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_138() throws Exception {
        // Combination: a1=-1.0d, b1=Double.NaN, a2=-1.0d, b2=0.0d, a3=0.0d, b3=1.0d, a4=Double.POSITIVE_INFINITY, b4=1.0d
        Object actual = MathArrays.linearCombination(-1.0d, Double.NaN, -1.0d, 0.0d, 0.0d, 1.0d, Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_139() throws Exception {
        // Combination: a1=-1.0d, b1=-1.0d, a2=Double.NaN, b2=Double.POSITIVE_INFINITY, a3=1.0d, b3=Double.NaN, a4=0.0d, b4=-1.0d
        Object actual = MathArrays.linearCombination(-1.0d, -1.0d, Double.NaN, Double.POSITIVE_INFINITY, 1.0d, Double.NaN, 0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_140() throws Exception {
        // Combination: a1=-1.0d, b1=0.0d, a2=Double.POSITIVE_INFINITY, b2=-1.0d, a3=1.0d, b3=-1.0d, a4=Double.NaN, b4=1.0d
        Object actual = MathArrays.linearCombination(-1.0d, 0.0d, Double.POSITIVE_INFINITY, -1.0d, 1.0d, -1.0d, Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_141() throws Exception {
        // Combination: a1=Double.NaN, b1=-1.0d, a2=0.0d, b2=0.0d, a3=Double.NaN, b3=0.0d, a4=1.0d, b4=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(Double.NaN, -1.0d, 0.0d, 0.0d, Double.NaN, 0.0d, 1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_142() throws Exception {
        // Combination: a1=Double.NaN, b1=Double.NaN, a2=1.0d, b2=-1.0d, a3=-1.0d, b3=Double.POSITIVE_INFINITY, a4=0.0d, b4=Double.NaN
        Object actual = MathArrays.linearCombination(Double.NaN, Double.NaN, 1.0d, -1.0d, -1.0d, Double.POSITIVE_INFINITY, 0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_143() throws Exception {
        // Combination: a1=Double.NaN, b1=Double.POSITIVE_INFINITY, a2=-1.0d, b2=Double.NaN, a3=1.0d, b3=1.0d, a4=Double.NaN, b4=0.0d
        Object actual = MathArrays.linearCombination(Double.NaN, Double.POSITIVE_INFINITY, -1.0d, Double.NaN, 1.0d, 1.0d, Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_144() throws Exception {
        // Combination: a1=Double.NaN, b1=1.0d, a2=Double.NaN, b2=1.0d, a3=0.0d, b3=Double.NaN, a4=-1.0d, b4=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(Double.NaN, 1.0d, Double.NaN, 1.0d, 0.0d, Double.NaN, -1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_145() throws Exception {
        // Combination: a1=Double.NaN, b1=0.0d, a2=Double.POSITIVE_INFINITY, b2=1.0d, a3=-1.0d, b3=Double.NaN, a4=Double.POSITIVE_INFINITY, b4=-1.0d
        Object actual = MathArrays.linearCombination(Double.NaN, 0.0d, Double.POSITIVE_INFINITY, 1.0d, -1.0d, Double.NaN, Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_146() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=Double.POSITIVE_INFINITY, a2=0.0d, b2=-1.0d, a3=Double.POSITIVE_INFINITY, b3=Double.NaN, a4=0.0d, b4=1.0d
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d, -1.0d, Double.POSITIVE_INFINITY, Double.NaN, 0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_147() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=1.0d, a2=1.0d, b2=Double.POSITIVE_INFINITY, a3=0.0d, b3=-1.0d, a4=Double.POSITIVE_INFINITY, b4=0.0d
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, 1.0d, 1.0d, Double.POSITIVE_INFINITY, 0.0d, -1.0d, Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_148() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=0.0d, a2=-1.0d, b2=Double.POSITIVE_INFINITY, a3=1.0d, b3=Double.POSITIVE_INFINITY, a4=1.0d, b4=Double.NaN
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, 0.0d, -1.0d, Double.POSITIVE_INFINITY, 1.0d, Double.POSITIVE_INFINITY, 1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_149() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=Double.POSITIVE_INFINITY, a2=Double.NaN, b2=0.0d, a3=-1.0d, b3=0.0d, a4=-1.0d, b4=-1.0d
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NaN, 0.0d, -1.0d, 0.0d, -1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_150() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=-1.0d, a2=Double.POSITIVE_INFINITY, b2=Double.NaN, a3=Double.NaN, b3=1.0d, a4=0.0d, b4=1.0d
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, -1.0d, Double.POSITIVE_INFINITY, Double.NaN, Double.NaN, 1.0d, 0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_151() throws Exception {
        // Combination: a1=1.0d, b1=Double.POSITIVE_INFINITY, a2=1.0d, b2=1.0d, a3=Double.POSITIVE_INFINITY, b3=Double.NaN, a4=1.0d, b4=0.0d
        Object actual = MathArrays.linearCombination(1.0d, Double.POSITIVE_INFINITY, 1.0d, 1.0d, Double.POSITIVE_INFINITY, Double.NaN, 1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_152() throws Exception {
        // Combination: a1=-1.0d, b1=0.0d, a2=-1.0d, b2=0.0d, a3=Double.POSITIVE_INFINITY, b3=0.0d, a4=1.0d, b4=-1.0d
        Object actual = MathArrays.linearCombination(-1.0d, 0.0d, -1.0d, 0.0d, Double.POSITIVE_INFINITY, 0.0d, 1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_153() throws Exception {
        // Combination: a1=Double.NaN, b1=1.0d, a2=Double.NaN, b2=Double.NaN, a3=Double.POSITIVE_INFINITY, b3=-1.0d, a4=-1.0d, b4=1.0d
        Object actual = MathArrays.linearCombination(Double.NaN, 1.0d, Double.NaN, Double.NaN, Double.POSITIVE_INFINITY, -1.0d, -1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_154() throws Exception {
        // Combination: a1=0.0d, b1=1.0d, a2=Double.POSITIVE_INFINITY, b2=0.0d, a3=0.0d, b3=0.0d, a4=-1.0d, b4=Double.NaN
        Object actual = MathArrays.linearCombination(0.0d, 1.0d, Double.POSITIVE_INFINITY, 0.0d, 0.0d, 0.0d, -1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_155() throws Exception {
        // Combination: a1=Double.POSITIVE_INFINITY, b1=Double.NaN, a2=0.0d, b2=1.0d, a3=Double.POSITIVE_INFINITY, b3=1.0d, a4=Double.NaN, b4=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(Double.POSITIVE_INFINITY, Double.NaN, 0.0d, 1.0d, Double.POSITIVE_INFINITY, 1.0d, Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_156() throws Exception {
        // Combination: a1=1.0d, b1=-1.0d, a2=0.0d, b2=Double.NaN, a3=1.0d, b3=0.0d, a4=Double.POSITIVE_INFINITY, b4=-1.0d
        Object actual = MathArrays.linearCombination(1.0d, -1.0d, 0.0d, Double.NaN, 1.0d, 0.0d, Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_157() throws Exception {
        // Combination: a1=0.0d, b1=Double.POSITIVE_INFINITY, a2=Double.NaN, b2=-1.0d, a3=Double.NaN, b3=0.0d, a4=Double.POSITIVE_INFINITY, b4=Double.NaN
        Object actual = MathArrays.linearCombination(0.0d, Double.POSITIVE_INFINITY, Double.NaN, -1.0d, Double.NaN, 0.0d, Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_158() throws Exception {
        // Combination: a1=0.0d, b1=-1.0d, a2=0.0d, b2=1.0d, a3=Double.POSITIVE_INFINITY, b3=Double.POSITIVE_INFINITY, a4=0.0d, b4=Double.NaN
        Object actual = MathArrays.linearCombination(0.0d, -1.0d, 0.0d, 1.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_159() throws Exception {
        // Combination: a1=-1.0d, b1=Double.POSITIVE_INFINITY, a2=0.0d, b2=-1.0d, a3=0.0d, b3=-1.0d, a4=0.0d, b4=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(-1.0d, Double.POSITIVE_INFINITY, 0.0d, -1.0d, 0.0d, -1.0d, 0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_160() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=0.0d, b2=0.0d, a3=1.0d, b3=-1.0d, a4=0.0d, b4=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, 0.0d, 0.0d, 1.0d, -1.0d, 0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_161() throws Exception {
        // Combination: a1=0.0d, b1=1.0d, a2=0.0d, b2=-1.0d, a3=0.0d, b3=Double.POSITIVE_INFINITY, a4=0.0d, b4=1.0d
        Object actual = MathArrays.linearCombination(0.0d, 1.0d, 0.0d, -1.0d, 0.0d, Double.POSITIVE_INFINITY, 0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_162() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=0.0d, b2=Double.NaN, a3=-1.0d, b3=0.0d, a4=0.0d, b4=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, 0.0d, Double.NaN, -1.0d, 0.0d, 0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_163() throws Exception {
        // Combination: a1=Double.NaN, b1=0.0d, a2=0.0d, b2=Double.POSITIVE_INFINITY, a3=-1.0d, b3=1.0d, a4=Double.NaN, b4=1.0d
        Object actual = MathArrays.linearCombination(Double.NaN, 0.0d, 0.0d, Double.POSITIVE_INFINITY, -1.0d, 1.0d, Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_164() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=0.0d, b2=Double.POSITIVE_INFINITY, a3=Double.NaN, b3=0.0d, a4=0.0d, b4=-1.0d
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, 0.0d, Double.POSITIVE_INFINITY, Double.NaN, 0.0d, 0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_165() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=1.0d, b2=0.0d, a3=0.0d, b3=0.0d, a4=0.0d, b4=Double.POSITIVE_INFINITY
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, 1.0d, 0.0d, 0.0d, 0.0d, 0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_166() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=0.0d, b2=0.0d, a3=0.0d, b3=1.0d, a4=-1.0d, b4=-1.0d
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 1.0d, -1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_167() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=-1.0d, b2=0.0d, a3=0.0d, b3=Double.NaN, a4=0.0d, b4=0.0d
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, -1.0d, 0.0d, 0.0d, Double.NaN, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_168() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=Double.NaN, b2=0.0d, a3=0.0d, b3=Double.POSITIVE_INFINITY, a4=0.0d, b4=0.0d
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, Double.NaN, 0.0d, 0.0d, Double.POSITIVE_INFINITY, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_169() throws Exception {
        // Combination: a1=0.0d, b1=-1.0d, a2=Double.POSITIVE_INFINITY, b2=0.0d, a3=0.0d, b3=0.0d, a4=0.0d, b4=0.0d
        Object actual = MathArrays.linearCombination(0.0d, -1.0d, Double.POSITIVE_INFINITY, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_170() throws Exception {
        // Combination: a1=0.0d, b1=Double.NaN, a2=0.0d, b2=0.0d, a3=0.0d, b3=0.0d, a4=0.0d, b4=0.0d
        Object actual = MathArrays.linearCombination(0.0d, Double.NaN, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_171() throws Exception {
        // Combination: a1=0.0d, b1=1.0d, a2=0.0d, b2=0.0d, a3=0.0d, b3=0.0d, a4=0.0d, b4=-1.0d
        Object actual = MathArrays.linearCombination(0.0d, 1.0d, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_172() throws Exception {
        // Combination: a1=0.0d, b1=Double.NaN, a2=0.0d, b2=0.0d, a3=0.0d, b3=0.0d, a4=0.0d, b4=-1.0d
        Object actual = MathArrays.linearCombination(0.0d, Double.NaN, 0.0d, 0.0d, 0.0d, 0.0d, 0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_linearCombination_pairwise_173() throws Exception {
        // Combination: a1=0.0d, b1=0.0d, a2=0.0d, b2=0.0d, a3=0.0d, b3=1.0d, a4=0.0d, b4=Double.NaN
        Object actual = MathArrays.linearCombination(0.0d, 0.0d, 0.0d, 0.0d, 0.0d, 1.0d, 0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_174() throws Exception {
        // Combination: x=new float[] {}, y=new float[] {}
        Object actual = MathArrays.equals(new float[] {}, new float[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_175() throws Exception {
        // Combination: x=new float[] {}, y=new float[] {1}
        Object actual = MathArrays.equals(new float[] {}, new float[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_176() throws Exception {
        // Combination: x=new float[] {1}, y=new float[] {}
        Object actual = MathArrays.equals(new float[] {1}, new float[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_177() throws Exception {
        // Combination: x=new float[] {1}, y=new float[] {1}
        Object actual = MathArrays.equals(new float[] {1}, new float[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_178() throws Exception {
        // Combination: x=new float[] {}, y=new float[] {}
        Object actual = MathArrays.equalsIncludingNaN(new float[] {}, new float[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_179() throws Exception {
        // Combination: x=new float[] {}, y=new float[] {1}
        Object actual = MathArrays.equalsIncludingNaN(new float[] {}, new float[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_180() throws Exception {
        // Combination: x=new float[] {1}, y=new float[] {}
        Object actual = MathArrays.equalsIncludingNaN(new float[] {1}, new float[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_181() throws Exception {
        // Combination: x=new float[] {1}, y=new float[] {1}
        Object actual = MathArrays.equalsIncludingNaN(new float[] {1}, new float[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_182() throws Exception {
        // Combination: x=new double[] {}, y=new double[] {}
        Object actual = MathArrays.equals(new double[] {}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_183() throws Exception {
        // Combination: x=new double[] {}, y=new double[] {1}
        Object actual = MathArrays.equals(new double[] {}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_184() throws Exception {
        // Combination: x=new double[] {1}, y=new double[] {}
        Object actual = MathArrays.equals(new double[] {1}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_185() throws Exception {
        // Combination: x=new double[] {1}, y=new double[] {1}
        Object actual = MathArrays.equals(new double[] {1}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_186() throws Exception {
        // Combination: x=new double[] {}, y=new double[] {}
        Object actual = MathArrays.equalsIncludingNaN(new double[] {}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_187() throws Exception {
        // Combination: x=new double[] {}, y=new double[] {1}
        Object actual = MathArrays.equalsIncludingNaN(new double[] {}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_188() throws Exception {
        // Combination: x=new double[] {1}, y=new double[] {}
        Object actual = MathArrays.equalsIncludingNaN(new double[] {1}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_189() throws Exception {
        // Combination: x=new double[] {1}, y=new double[] {1}
        Object actual = MathArrays.equalsIncludingNaN(new double[] {1}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

}
