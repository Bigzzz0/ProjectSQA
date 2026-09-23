package org.apache.commons.math3.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for FastMath.
 */
public class FastMath_IPOTest {
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
    public void test_log_pairwise_001() throws Exception {
        // Combination: base=0.0d, x=0.0d
        Object actual = FastMath.log(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_002() throws Exception {
        // Combination: base=0.0d, x=1.0d
        Object actual = FastMath.log(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_003() throws Exception {
        // Combination: base=0.0d, x=-1.0d
        Object actual = FastMath.log(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_004() throws Exception {
        // Combination: base=0.0d, x=Double.NaN
        Object actual = FastMath.log(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_005() throws Exception {
        // Combination: base=0.0d, x=Double.POSITIVE_INFINITY
        Object actual = FastMath.log(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_006() throws Exception {
        // Combination: base=1.0d, x=0.0d
        Object actual = FastMath.log(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_007() throws Exception {
        // Combination: base=1.0d, x=1.0d
        Object actual = FastMath.log(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_008() throws Exception {
        // Combination: base=1.0d, x=-1.0d
        Object actual = FastMath.log(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_009() throws Exception {
        // Combination: base=1.0d, x=Double.NaN
        Object actual = FastMath.log(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_010() throws Exception {
        // Combination: base=1.0d, x=Double.POSITIVE_INFINITY
        Object actual = FastMath.log(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_011() throws Exception {
        // Combination: base=-1.0d, x=0.0d
        Object actual = FastMath.log(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_012() throws Exception {
        // Combination: base=-1.0d, x=1.0d
        Object actual = FastMath.log(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_013() throws Exception {
        // Combination: base=-1.0d, x=-1.0d
        Object actual = FastMath.log(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_014() throws Exception {
        // Combination: base=-1.0d, x=Double.NaN
        Object actual = FastMath.log(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_015() throws Exception {
        // Combination: base=-1.0d, x=Double.POSITIVE_INFINITY
        Object actual = FastMath.log(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_016() throws Exception {
        // Combination: base=Double.NaN, x=0.0d
        Object actual = FastMath.log(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_017() throws Exception {
        // Combination: base=Double.NaN, x=1.0d
        Object actual = FastMath.log(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_018() throws Exception {
        // Combination: base=Double.NaN, x=-1.0d
        Object actual = FastMath.log(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_019() throws Exception {
        // Combination: base=Double.NaN, x=Double.NaN
        Object actual = FastMath.log(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_020() throws Exception {
        // Combination: base=Double.NaN, x=Double.POSITIVE_INFINITY
        Object actual = FastMath.log(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_021() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=0.0d
        Object actual = FastMath.log(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_022() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=1.0d
        Object actual = FastMath.log(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_023() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=-1.0d
        Object actual = FastMath.log(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_024() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=Double.NaN
        Object actual = FastMath.log(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_025() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=Double.POSITIVE_INFINITY
        Object actual = FastMath.log(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_026() throws Exception {
        // Combination: x=0.0d, y=0.0d
        Object actual = FastMath.pow(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_027() throws Exception {
        // Combination: x=0.0d, y=1.0d
        Object actual = FastMath.pow(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_028() throws Exception {
        // Combination: x=0.0d, y=-1.0d
        Object actual = FastMath.pow(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_029() throws Exception {
        // Combination: x=0.0d, y=Double.NaN
        Object actual = FastMath.pow(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_030() throws Exception {
        // Combination: x=0.0d, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.pow(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_031() throws Exception {
        // Combination: x=1.0d, y=0.0d
        Object actual = FastMath.pow(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_032() throws Exception {
        // Combination: x=1.0d, y=1.0d
        Object actual = FastMath.pow(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_033() throws Exception {
        // Combination: x=1.0d, y=-1.0d
        Object actual = FastMath.pow(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_034() throws Exception {
        // Combination: x=1.0d, y=Double.NaN
        Object actual = FastMath.pow(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_035() throws Exception {
        // Combination: x=1.0d, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.pow(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_036() throws Exception {
        // Combination: x=-1.0d, y=0.0d
        Object actual = FastMath.pow(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_037() throws Exception {
        // Combination: x=-1.0d, y=1.0d
        Object actual = FastMath.pow(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_038() throws Exception {
        // Combination: x=-1.0d, y=-1.0d
        Object actual = FastMath.pow(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_039() throws Exception {
        // Combination: x=-1.0d, y=Double.NaN
        Object actual = FastMath.pow(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_040() throws Exception {
        // Combination: x=-1.0d, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.pow(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_041() throws Exception {
        // Combination: x=Double.NaN, y=0.0d
        Object actual = FastMath.pow(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_042() throws Exception {
        // Combination: x=Double.NaN, y=1.0d
        Object actual = FastMath.pow(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_043() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d
        Object actual = FastMath.pow(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_044() throws Exception {
        // Combination: x=Double.NaN, y=Double.NaN
        Object actual = FastMath.pow(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_045() throws Exception {
        // Combination: x=Double.NaN, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.pow(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_046() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=0.0d
        Object actual = FastMath.pow(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_047() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=1.0d
        Object actual = FastMath.pow(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_048() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=-1.0d
        Object actual = FastMath.pow(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_049() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.NaN
        Object actual = FastMath.pow(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_050() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.pow(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_051() throws Exception {
        // Combination: y=0.0d, x=0.0d
        Object actual = FastMath.atan2(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_052() throws Exception {
        // Combination: y=1.0d, x=0.0d
        Object actual = FastMath.atan2(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5707963267948966", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_053() throws Exception {
        // Combination: y=-1.0d, x=0.0d
        Object actual = FastMath.atan2(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.5707963267948966", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_054() throws Exception {
        // Combination: y=Double.NaN, x=0.0d
        Object actual = FastMath.atan2(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_055() throws Exception {
        // Combination: y=Double.POSITIVE_INFINITY, x=0.0d
        Object actual = FastMath.atan2(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5707963267948966", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_056() throws Exception {
        // Combination: y=0.0d, x=1.0d
        Object actual = FastMath.atan2(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_057() throws Exception {
        // Combination: y=1.0d, x=1.0d
        Object actual = FastMath.atan2(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.7853981633974483", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_058() throws Exception {
        // Combination: y=-1.0d, x=1.0d
        Object actual = FastMath.atan2(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.7853981633974483", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_059() throws Exception {
        // Combination: y=Double.NaN, x=1.0d
        Object actual = FastMath.atan2(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_060() throws Exception {
        // Combination: y=Double.POSITIVE_INFINITY, x=1.0d
        Object actual = FastMath.atan2(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5707963267948966", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_061() throws Exception {
        // Combination: y=0.0d, x=-1.0d
        Object actual = FastMath.atan2(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("3.141592653589793", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_062() throws Exception {
        // Combination: y=1.0d, x=-1.0d
        Object actual = FastMath.atan2(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("2.356194490192345", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_063() throws Exception {
        // Combination: y=-1.0d, x=-1.0d
        Object actual = FastMath.atan2(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-2.356194490192345", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_064() throws Exception {
        // Combination: y=Double.NaN, x=-1.0d
        Object actual = FastMath.atan2(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_065() throws Exception {
        // Combination: y=Double.POSITIVE_INFINITY, x=-1.0d
        Object actual = FastMath.atan2(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5707963267948966", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_066() throws Exception {
        // Combination: y=0.0d, x=Double.NaN
        Object actual = FastMath.atan2(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_067() throws Exception {
        // Combination: y=1.0d, x=Double.NaN
        Object actual = FastMath.atan2(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_068() throws Exception {
        // Combination: y=-1.0d, x=Double.NaN
        Object actual = FastMath.atan2(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_069() throws Exception {
        // Combination: y=Double.NaN, x=Double.NaN
        Object actual = FastMath.atan2(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_070() throws Exception {
        // Combination: y=Double.POSITIVE_INFINITY, x=Double.NaN
        Object actual = FastMath.atan2(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_071() throws Exception {
        // Combination: y=0.0d, x=Double.POSITIVE_INFINITY
        Object actual = FastMath.atan2(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_072() throws Exception {
        // Combination: y=1.0d, x=Double.POSITIVE_INFINITY
        Object actual = FastMath.atan2(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_073() throws Exception {
        // Combination: y=-1.0d, x=Double.POSITIVE_INFINITY
        Object actual = FastMath.atan2(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_074() throws Exception {
        // Combination: y=Double.NaN, x=Double.POSITIVE_INFINITY
        Object actual = FastMath.atan2(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_075() throws Exception {
        // Combination: y=Double.POSITIVE_INFINITY, x=Double.POSITIVE_INFINITY
        Object actual = FastMath.atan2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.7853981633974483", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_076() throws Exception {
        // Combination: d=0.0d, n=0
        Object actual = FastMath.scalb(0.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_077() throws Exception {
        // Combination: d=0.0d, n=1
        Object actual = FastMath.scalb(0.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_078() throws Exception {
        // Combination: d=0.0d, n=-1
        Object actual = FastMath.scalb(0.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_079() throws Exception {
        // Combination: d=0.0d, n=Integer.MAX_VALUE
        Object actual = FastMath.scalb(0.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_080() throws Exception {
        // Combination: d=0.0d, n=Integer.MIN_VALUE
        Object actual = FastMath.scalb(0.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_081() throws Exception {
        // Combination: d=1.0d, n=0
        Object actual = FastMath.scalb(1.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_082() throws Exception {
        // Combination: d=1.0d, n=1
        Object actual = FastMath.scalb(1.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("2.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_083() throws Exception {
        // Combination: d=1.0d, n=-1
        Object actual = FastMath.scalb(1.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_084() throws Exception {
        // Combination: d=1.0d, n=Integer.MAX_VALUE
        Object actual = FastMath.scalb(1.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_085() throws Exception {
        // Combination: d=1.0d, n=Integer.MIN_VALUE
        Object actual = FastMath.scalb(1.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_086() throws Exception {
        // Combination: d=-1.0d, n=0
        Object actual = FastMath.scalb(-1.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_087() throws Exception {
        // Combination: d=-1.0d, n=1
        Object actual = FastMath.scalb(-1.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-2.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_088() throws Exception {
        // Combination: d=-1.0d, n=-1
        Object actual = FastMath.scalb(-1.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_089() throws Exception {
        // Combination: d=-1.0d, n=Integer.MAX_VALUE
        Object actual = FastMath.scalb(-1.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_090() throws Exception {
        // Combination: d=-1.0d, n=Integer.MIN_VALUE
        Object actual = FastMath.scalb(-1.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_091() throws Exception {
        // Combination: d=Double.NaN, n=0
        Object actual = FastMath.scalb(Double.NaN, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_092() throws Exception {
        // Combination: d=Double.NaN, n=1
        Object actual = FastMath.scalb(Double.NaN, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_093() throws Exception {
        // Combination: d=Double.NaN, n=-1
        Object actual = FastMath.scalb(Double.NaN, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_094() throws Exception {
        // Combination: d=Double.NaN, n=Integer.MAX_VALUE
        Object actual = FastMath.scalb(Double.NaN, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_095() throws Exception {
        // Combination: d=Double.NaN, n=Integer.MIN_VALUE
        Object actual = FastMath.scalb(Double.NaN, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_096() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, n=0
        Object actual = FastMath.scalb(Double.POSITIVE_INFINITY, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_097() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, n=1
        Object actual = FastMath.scalb(Double.POSITIVE_INFINITY, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_098() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, n=-1
        Object actual = FastMath.scalb(Double.POSITIVE_INFINITY, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_099() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, n=Integer.MAX_VALUE
        Object actual = FastMath.scalb(Double.POSITIVE_INFINITY, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_100() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, n=Integer.MIN_VALUE
        Object actual = FastMath.scalb(Double.POSITIVE_INFINITY, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_101() throws Exception {
        // Combination: f=0.0f, n=0
        Object actual = FastMath.scalb(0.0f, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_102() throws Exception {
        // Combination: f=0.0f, n=1
        Object actual = FastMath.scalb(0.0f, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_103() throws Exception {
        // Combination: f=0.0f, n=-1
        Object actual = FastMath.scalb(0.0f, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_104() throws Exception {
        // Combination: f=0.0f, n=Integer.MAX_VALUE
        Object actual = FastMath.scalb(0.0f, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_105() throws Exception {
        // Combination: f=0.0f, n=Integer.MIN_VALUE
        Object actual = FastMath.scalb(0.0f, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_106() throws Exception {
        // Combination: f=1.0f, n=0
        Object actual = FastMath.scalb(1.0f, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_107() throws Exception {
        // Combination: f=1.0f, n=1
        Object actual = FastMath.scalb(1.0f, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("2.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_108() throws Exception {
        // Combination: f=1.0f, n=-1
        Object actual = FastMath.scalb(1.0f, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_109() throws Exception {
        // Combination: f=1.0f, n=Integer.MAX_VALUE
        Object actual = FastMath.scalb(1.0f, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_110() throws Exception {
        // Combination: f=1.0f, n=Integer.MIN_VALUE
        Object actual = FastMath.scalb(1.0f, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_111() throws Exception {
        // Combination: f=-1.0f, n=0
        Object actual = FastMath.scalb(-1.0f, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_112() throws Exception {
        // Combination: f=-1.0f, n=1
        Object actual = FastMath.scalb(-1.0f, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-2.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_113() throws Exception {
        // Combination: f=-1.0f, n=-1
        Object actual = FastMath.scalb(-1.0f, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-0.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_114() throws Exception {
        // Combination: f=-1.0f, n=Integer.MAX_VALUE
        Object actual = FastMath.scalb(-1.0f, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_115() throws Exception {
        // Combination: f=-1.0f, n=Integer.MIN_VALUE
        Object actual = FastMath.scalb(-1.0f, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_116() throws Exception {
        // Combination: f=Float.NaN, n=0
        Object actual = FastMath.scalb(Float.NaN, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_117() throws Exception {
        // Combination: f=Float.NaN, n=1
        Object actual = FastMath.scalb(Float.NaN, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_118() throws Exception {
        // Combination: f=Float.NaN, n=-1
        Object actual = FastMath.scalb(Float.NaN, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_119() throws Exception {
        // Combination: f=Float.NaN, n=Integer.MAX_VALUE
        Object actual = FastMath.scalb(Float.NaN, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_120() throws Exception {
        // Combination: f=Float.NaN, n=Integer.MIN_VALUE
        Object actual = FastMath.scalb(Float.NaN, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_121() throws Exception {
        // Combination: f=Float.POSITIVE_INFINITY, n=0
        Object actual = FastMath.scalb(Float.POSITIVE_INFINITY, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_122() throws Exception {
        // Combination: f=Float.POSITIVE_INFINITY, n=1
        Object actual = FastMath.scalb(Float.POSITIVE_INFINITY, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_123() throws Exception {
        // Combination: f=Float.POSITIVE_INFINITY, n=-1
        Object actual = FastMath.scalb(Float.POSITIVE_INFINITY, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_124() throws Exception {
        // Combination: f=Float.POSITIVE_INFINITY, n=Integer.MAX_VALUE
        Object actual = FastMath.scalb(Float.POSITIVE_INFINITY, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_125() throws Exception {
        // Combination: f=Float.POSITIVE_INFINITY, n=Integer.MIN_VALUE
        Object actual = FastMath.scalb(Float.POSITIVE_INFINITY, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_126() throws Exception {
        // Combination: d=0.0d, direction=0.0d
        Object actual = FastMath.nextAfter(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_127() throws Exception {
        // Combination: d=0.0d, direction=1.0d
        Object actual = FastMath.nextAfter(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("4.9E-324", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_128() throws Exception {
        // Combination: d=0.0d, direction=-1.0d
        Object actual = FastMath.nextAfter(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-4.9E-324", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_129() throws Exception {
        // Combination: d=0.0d, direction=Double.NaN
        Object actual = FastMath.nextAfter(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_130() throws Exception {
        // Combination: d=0.0d, direction=Double.POSITIVE_INFINITY
        Object actual = FastMath.nextAfter(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("4.9E-324", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_131() throws Exception {
        // Combination: d=1.0d, direction=0.0d
        Object actual = FastMath.nextAfter(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.9999999999999999", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_132() throws Exception {
        // Combination: d=1.0d, direction=1.0d
        Object actual = FastMath.nextAfter(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_133() throws Exception {
        // Combination: d=1.0d, direction=-1.0d
        Object actual = FastMath.nextAfter(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.9999999999999999", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_134() throws Exception {
        // Combination: d=1.0d, direction=Double.NaN
        Object actual = FastMath.nextAfter(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_135() throws Exception {
        // Combination: d=1.0d, direction=Double.POSITIVE_INFINITY
        Object actual = FastMath.nextAfter(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0000000000000002", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_136() throws Exception {
        // Combination: d=-1.0d, direction=0.0d
        Object actual = FastMath.nextAfter(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.9999999999999999", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_137() throws Exception {
        // Combination: d=-1.0d, direction=1.0d
        Object actual = FastMath.nextAfter(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.9999999999999999", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_138() throws Exception {
        // Combination: d=-1.0d, direction=-1.0d
        Object actual = FastMath.nextAfter(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_139() throws Exception {
        // Combination: d=-1.0d, direction=Double.NaN
        Object actual = FastMath.nextAfter(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_140() throws Exception {
        // Combination: d=-1.0d, direction=Double.POSITIVE_INFINITY
        Object actual = FastMath.nextAfter(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.9999999999999999", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_141() throws Exception {
        // Combination: d=Double.NaN, direction=0.0d
        Object actual = FastMath.nextAfter(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_142() throws Exception {
        // Combination: d=Double.NaN, direction=1.0d
        Object actual = FastMath.nextAfter(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_143() throws Exception {
        // Combination: d=Double.NaN, direction=-1.0d
        Object actual = FastMath.nextAfter(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_144() throws Exception {
        // Combination: d=Double.NaN, direction=Double.NaN
        Object actual = FastMath.nextAfter(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_145() throws Exception {
        // Combination: d=Double.NaN, direction=Double.POSITIVE_INFINITY
        Object actual = FastMath.nextAfter(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_146() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, direction=0.0d
        Object actual = FastMath.nextAfter(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_147() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, direction=1.0d
        Object actual = FastMath.nextAfter(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_148() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, direction=-1.0d
        Object actual = FastMath.nextAfter(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_149() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, direction=Double.NaN
        Object actual = FastMath.nextAfter(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_150() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, direction=Double.POSITIVE_INFINITY
        Object actual = FastMath.nextAfter(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_151() throws Exception {
        // Combination: f=0.0f, direction=0.0d
        Object actual = FastMath.nextAfter(0.0f, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_152() throws Exception {
        // Combination: f=1.0f, direction=0.0d
        Object actual = FastMath.nextAfter(1.0f, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.99999994", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_153() throws Exception {
        // Combination: f=-1.0f, direction=0.0d
        Object actual = FastMath.nextAfter(-1.0f, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-0.99999994", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_154() throws Exception {
        // Combination: f=Float.NaN, direction=0.0d
        Object actual = FastMath.nextAfter(Float.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_155() throws Exception {
        // Combination: f=Float.POSITIVE_INFINITY, direction=0.0d
        Object actual = FastMath.nextAfter(Float.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("3.4028235E38", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_156() throws Exception {
        // Combination: f=0.0f, direction=1.0d
        Object actual = FastMath.nextAfter(0.0f, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.4E-45", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_157() throws Exception {
        // Combination: f=1.0f, direction=1.0d
        Object actual = FastMath.nextAfter(1.0f, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_158() throws Exception {
        // Combination: f=-1.0f, direction=1.0d
        Object actual = FastMath.nextAfter(-1.0f, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-0.99999994", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_159() throws Exception {
        // Combination: f=Float.NaN, direction=1.0d
        Object actual = FastMath.nextAfter(Float.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_160() throws Exception {
        // Combination: f=Float.POSITIVE_INFINITY, direction=1.0d
        Object actual = FastMath.nextAfter(Float.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("3.4028235E38", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_161() throws Exception {
        // Combination: f=0.0f, direction=-1.0d
        Object actual = FastMath.nextAfter(0.0f, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.4E-45", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_162() throws Exception {
        // Combination: f=1.0f, direction=-1.0d
        Object actual = FastMath.nextAfter(1.0f, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.99999994", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_163() throws Exception {
        // Combination: f=-1.0f, direction=-1.0d
        Object actual = FastMath.nextAfter(-1.0f, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_164() throws Exception {
        // Combination: f=Float.NaN, direction=-1.0d
        Object actual = FastMath.nextAfter(Float.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_165() throws Exception {
        // Combination: f=Float.POSITIVE_INFINITY, direction=-1.0d
        Object actual = FastMath.nextAfter(Float.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("3.4028235E38", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_166() throws Exception {
        // Combination: f=0.0f, direction=Double.NaN
        Object actual = FastMath.nextAfter(0.0f, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_167() throws Exception {
        // Combination: f=1.0f, direction=Double.NaN
        Object actual = FastMath.nextAfter(1.0f, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_168() throws Exception {
        // Combination: f=-1.0f, direction=Double.NaN
        Object actual = FastMath.nextAfter(-1.0f, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_169() throws Exception {
        // Combination: f=Float.NaN, direction=Double.NaN
        Object actual = FastMath.nextAfter(Float.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_170() throws Exception {
        // Combination: f=Float.POSITIVE_INFINITY, direction=Double.NaN
        Object actual = FastMath.nextAfter(Float.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_171() throws Exception {
        // Combination: f=0.0f, direction=Double.POSITIVE_INFINITY
        Object actual = FastMath.nextAfter(0.0f, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.4E-45", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_172() throws Exception {
        // Combination: f=1.0f, direction=Double.POSITIVE_INFINITY
        Object actual = FastMath.nextAfter(1.0f, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0000001", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_173() throws Exception {
        // Combination: f=-1.0f, direction=Double.POSITIVE_INFINITY
        Object actual = FastMath.nextAfter(-1.0f, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-0.99999994", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_174() throws Exception {
        // Combination: f=Float.NaN, direction=Double.POSITIVE_INFINITY
        Object actual = FastMath.nextAfter(Float.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_175() throws Exception {
        // Combination: f=Float.POSITIVE_INFINITY, direction=Double.POSITIVE_INFINITY
        Object actual = FastMath.nextAfter(Float.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_176() throws Exception {
        // Combination: a=0, b=0
        Object actual = FastMath.min(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_177() throws Exception {
        // Combination: a=0, b=1
        Object actual = FastMath.min(0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_178() throws Exception {
        // Combination: a=0, b=-1
        Object actual = FastMath.min(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_179() throws Exception {
        // Combination: a=0, b=Integer.MAX_VALUE
        Object actual = FastMath.min(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_180() throws Exception {
        // Combination: a=0, b=Integer.MIN_VALUE
        Object actual = FastMath.min(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_181() throws Exception {
        // Combination: a=1, b=0
        Object actual = FastMath.min(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_182() throws Exception {
        // Combination: a=1, b=1
        Object actual = FastMath.min(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_183() throws Exception {
        // Combination: a=1, b=-1
        Object actual = FastMath.min(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_184() throws Exception {
        // Combination: a=1, b=Integer.MAX_VALUE
        Object actual = FastMath.min(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_185() throws Exception {
        // Combination: a=1, b=Integer.MIN_VALUE
        Object actual = FastMath.min(1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_186() throws Exception {
        // Combination: a=-1, b=0
        Object actual = FastMath.min(-1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_187() throws Exception {
        // Combination: a=-1, b=1
        Object actual = FastMath.min(-1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_188() throws Exception {
        // Combination: a=-1, b=-1
        Object actual = FastMath.min(-1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_189() throws Exception {
        // Combination: a=-1, b=Integer.MAX_VALUE
        Object actual = FastMath.min(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_190() throws Exception {
        // Combination: a=-1, b=Integer.MIN_VALUE
        Object actual = FastMath.min(-1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_191() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=0
        Object actual = FastMath.min(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_192() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=1
        Object actual = FastMath.min(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_193() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=-1
        Object actual = FastMath.min(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_194() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MAX_VALUE
        Object actual = FastMath.min(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_195() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MIN_VALUE
        Object actual = FastMath.min(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_196() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=0
        Object actual = FastMath.min(Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_197() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=1
        Object actual = FastMath.min(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_198() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=-1
        Object actual = FastMath.min(Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_199() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MAX_VALUE
        Object actual = FastMath.min(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_200() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MIN_VALUE
        Object actual = FastMath.min(Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_201() throws Exception {
        // Combination: a=0L, b=0L
        Object actual = FastMath.min(0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_202() throws Exception {
        // Combination: a=0L, b=1L
        Object actual = FastMath.min(0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_203() throws Exception {
        // Combination: a=0L, b=-1L
        Object actual = FastMath.min(0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_204() throws Exception {
        // Combination: a=0L, b=Long.MAX_VALUE
        Object actual = FastMath.min(0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_205() throws Exception {
        // Combination: a=0L, b=Long.MIN_VALUE
        Object actual = FastMath.min(0L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_206() throws Exception {
        // Combination: a=1L, b=0L
        Object actual = FastMath.min(1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_207() throws Exception {
        // Combination: a=1L, b=1L
        Object actual = FastMath.min(1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_208() throws Exception {
        // Combination: a=1L, b=-1L
        Object actual = FastMath.min(1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_209() throws Exception {
        // Combination: a=1L, b=Long.MAX_VALUE
        Object actual = FastMath.min(1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_210() throws Exception {
        // Combination: a=1L, b=Long.MIN_VALUE
        Object actual = FastMath.min(1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_211() throws Exception {
        // Combination: a=-1L, b=0L
        Object actual = FastMath.min(-1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_212() throws Exception {
        // Combination: a=-1L, b=1L
        Object actual = FastMath.min(-1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_213() throws Exception {
        // Combination: a=-1L, b=-1L
        Object actual = FastMath.min(-1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_214() throws Exception {
        // Combination: a=-1L, b=Long.MAX_VALUE
        Object actual = FastMath.min(-1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_215() throws Exception {
        // Combination: a=-1L, b=Long.MIN_VALUE
        Object actual = FastMath.min(-1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_216() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=0L
        Object actual = FastMath.min(Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_217() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=1L
        Object actual = FastMath.min(Long.MAX_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_218() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=-1L
        Object actual = FastMath.min(Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_219() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE
        Object actual = FastMath.min(Long.MAX_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_220() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MIN_VALUE
        Object actual = FastMath.min(Long.MAX_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_221() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=0L
        Object actual = FastMath.min(Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_222() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=1L
        Object actual = FastMath.min(Long.MIN_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_223() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=-1L
        Object actual = FastMath.min(Long.MIN_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_224() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MAX_VALUE
        Object actual = FastMath.min(Long.MIN_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_225() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MIN_VALUE
        Object actual = FastMath.min(Long.MIN_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_226() throws Exception {
        // Combination: a=0.0f, b=0.0f
        Object actual = FastMath.min(0.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_227() throws Exception {
        // Combination: a=0.0f, b=1.0f
        Object actual = FastMath.min(0.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_228() throws Exception {
        // Combination: a=0.0f, b=-1.0f
        Object actual = FastMath.min(0.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_229() throws Exception {
        // Combination: a=0.0f, b=Float.NaN
        Object actual = FastMath.min(0.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_230() throws Exception {
        // Combination: a=0.0f, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.min(0.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_231() throws Exception {
        // Combination: a=1.0f, b=0.0f
        Object actual = FastMath.min(1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_232() throws Exception {
        // Combination: a=1.0f, b=1.0f
        Object actual = FastMath.min(1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_233() throws Exception {
        // Combination: a=1.0f, b=-1.0f
        Object actual = FastMath.min(1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_234() throws Exception {
        // Combination: a=1.0f, b=Float.NaN
        Object actual = FastMath.min(1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_235() throws Exception {
        // Combination: a=1.0f, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.min(1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_236() throws Exception {
        // Combination: a=-1.0f, b=0.0f
        Object actual = FastMath.min(-1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_237() throws Exception {
        // Combination: a=-1.0f, b=1.0f
        Object actual = FastMath.min(-1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_238() throws Exception {
        // Combination: a=-1.0f, b=-1.0f
        Object actual = FastMath.min(-1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_239() throws Exception {
        // Combination: a=-1.0f, b=Float.NaN
        Object actual = FastMath.min(-1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_240() throws Exception {
        // Combination: a=-1.0f, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.min(-1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_241() throws Exception {
        // Combination: a=Float.NaN, b=0.0f
        Object actual = FastMath.min(Float.NaN, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_242() throws Exception {
        // Combination: a=Float.NaN, b=1.0f
        Object actual = FastMath.min(Float.NaN, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_243() throws Exception {
        // Combination: a=Float.NaN, b=-1.0f
        Object actual = FastMath.min(Float.NaN, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_244() throws Exception {
        // Combination: a=Float.NaN, b=Float.NaN
        Object actual = FastMath.min(Float.NaN, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_245() throws Exception {
        // Combination: a=Float.NaN, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.min(Float.NaN, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_246() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=0.0f
        Object actual = FastMath.min(Float.POSITIVE_INFINITY, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_247() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=1.0f
        Object actual = FastMath.min(Float.POSITIVE_INFINITY, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_248() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=-1.0f
        Object actual = FastMath.min(Float.POSITIVE_INFINITY, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_249() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=Float.NaN
        Object actual = FastMath.min(Float.POSITIVE_INFINITY, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_250() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.min(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_251() throws Exception {
        // Combination: a=0.0d, b=0.0d
        Object actual = FastMath.min(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_252() throws Exception {
        // Combination: a=0.0d, b=1.0d
        Object actual = FastMath.min(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_253() throws Exception {
        // Combination: a=0.0d, b=-1.0d
        Object actual = FastMath.min(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_254() throws Exception {
        // Combination: a=0.0d, b=Double.NaN
        Object actual = FastMath.min(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_255() throws Exception {
        // Combination: a=0.0d, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.min(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_256() throws Exception {
        // Combination: a=1.0d, b=0.0d
        Object actual = FastMath.min(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_257() throws Exception {
        // Combination: a=1.0d, b=1.0d
        Object actual = FastMath.min(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_258() throws Exception {
        // Combination: a=1.0d, b=-1.0d
        Object actual = FastMath.min(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_259() throws Exception {
        // Combination: a=1.0d, b=Double.NaN
        Object actual = FastMath.min(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_260() throws Exception {
        // Combination: a=1.0d, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.min(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_261() throws Exception {
        // Combination: a=-1.0d, b=0.0d
        Object actual = FastMath.min(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_262() throws Exception {
        // Combination: a=-1.0d, b=1.0d
        Object actual = FastMath.min(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_263() throws Exception {
        // Combination: a=-1.0d, b=-1.0d
        Object actual = FastMath.min(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_264() throws Exception {
        // Combination: a=-1.0d, b=Double.NaN
        Object actual = FastMath.min(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_265() throws Exception {
        // Combination: a=-1.0d, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.min(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_266() throws Exception {
        // Combination: a=Double.NaN, b=0.0d
        Object actual = FastMath.min(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_267() throws Exception {
        // Combination: a=Double.NaN, b=1.0d
        Object actual = FastMath.min(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_268() throws Exception {
        // Combination: a=Double.NaN, b=-1.0d
        Object actual = FastMath.min(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_269() throws Exception {
        // Combination: a=Double.NaN, b=Double.NaN
        Object actual = FastMath.min(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_270() throws Exception {
        // Combination: a=Double.NaN, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.min(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_271() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=0.0d
        Object actual = FastMath.min(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_272() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=1.0d
        Object actual = FastMath.min(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_273() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=-1.0d
        Object actual = FastMath.min(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_274() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=Double.NaN
        Object actual = FastMath.min(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_275() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.min(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_276() throws Exception {
        // Combination: a=0, b=0
        Object actual = FastMath.max(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_277() throws Exception {
        // Combination: a=0, b=1
        Object actual = FastMath.max(0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_278() throws Exception {
        // Combination: a=0, b=-1
        Object actual = FastMath.max(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_279() throws Exception {
        // Combination: a=0, b=Integer.MAX_VALUE
        Object actual = FastMath.max(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_280() throws Exception {
        // Combination: a=0, b=Integer.MIN_VALUE
        Object actual = FastMath.max(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_281() throws Exception {
        // Combination: a=1, b=0
        Object actual = FastMath.max(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_282() throws Exception {
        // Combination: a=1, b=1
        Object actual = FastMath.max(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_283() throws Exception {
        // Combination: a=1, b=-1
        Object actual = FastMath.max(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_284() throws Exception {
        // Combination: a=1, b=Integer.MAX_VALUE
        Object actual = FastMath.max(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_285() throws Exception {
        // Combination: a=1, b=Integer.MIN_VALUE
        Object actual = FastMath.max(1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_286() throws Exception {
        // Combination: a=-1, b=0
        Object actual = FastMath.max(-1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_287() throws Exception {
        // Combination: a=-1, b=1
        Object actual = FastMath.max(-1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_288() throws Exception {
        // Combination: a=-1, b=-1
        Object actual = FastMath.max(-1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_289() throws Exception {
        // Combination: a=-1, b=Integer.MAX_VALUE
        Object actual = FastMath.max(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_290() throws Exception {
        // Combination: a=-1, b=Integer.MIN_VALUE
        Object actual = FastMath.max(-1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_291() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=0
        Object actual = FastMath.max(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_292() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=1
        Object actual = FastMath.max(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_293() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=-1
        Object actual = FastMath.max(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_294() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MAX_VALUE
        Object actual = FastMath.max(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_295() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MIN_VALUE
        Object actual = FastMath.max(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_296() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=0
        Object actual = FastMath.max(Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_297() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=1
        Object actual = FastMath.max(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_298() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=-1
        Object actual = FastMath.max(Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_299() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MAX_VALUE
        Object actual = FastMath.max(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_300() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MIN_VALUE
        Object actual = FastMath.max(Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_301() throws Exception {
        // Combination: a=0L, b=0L
        Object actual = FastMath.max(0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_302() throws Exception {
        // Combination: a=0L, b=1L
        Object actual = FastMath.max(0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_303() throws Exception {
        // Combination: a=0L, b=-1L
        Object actual = FastMath.max(0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_304() throws Exception {
        // Combination: a=0L, b=Long.MAX_VALUE
        Object actual = FastMath.max(0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_305() throws Exception {
        // Combination: a=0L, b=Long.MIN_VALUE
        Object actual = FastMath.max(0L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_306() throws Exception {
        // Combination: a=1L, b=0L
        Object actual = FastMath.max(1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_307() throws Exception {
        // Combination: a=1L, b=1L
        Object actual = FastMath.max(1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_308() throws Exception {
        // Combination: a=1L, b=-1L
        Object actual = FastMath.max(1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_309() throws Exception {
        // Combination: a=1L, b=Long.MAX_VALUE
        Object actual = FastMath.max(1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_310() throws Exception {
        // Combination: a=1L, b=Long.MIN_VALUE
        Object actual = FastMath.max(1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_311() throws Exception {
        // Combination: a=-1L, b=0L
        Object actual = FastMath.max(-1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_312() throws Exception {
        // Combination: a=-1L, b=1L
        Object actual = FastMath.max(-1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_313() throws Exception {
        // Combination: a=-1L, b=-1L
        Object actual = FastMath.max(-1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_314() throws Exception {
        // Combination: a=-1L, b=Long.MAX_VALUE
        Object actual = FastMath.max(-1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_315() throws Exception {
        // Combination: a=-1L, b=Long.MIN_VALUE
        Object actual = FastMath.max(-1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_316() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=0L
        Object actual = FastMath.max(Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_317() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=1L
        Object actual = FastMath.max(Long.MAX_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_318() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=-1L
        Object actual = FastMath.max(Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_319() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE
        Object actual = FastMath.max(Long.MAX_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_320() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MIN_VALUE
        Object actual = FastMath.max(Long.MAX_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_321() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=0L
        Object actual = FastMath.max(Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_322() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=1L
        Object actual = FastMath.max(Long.MIN_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_323() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=-1L
        Object actual = FastMath.max(Long.MIN_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_324() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MAX_VALUE
        Object actual = FastMath.max(Long.MIN_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_325() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MIN_VALUE
        Object actual = FastMath.max(Long.MIN_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_326() throws Exception {
        // Combination: a=0.0f, b=0.0f
        Object actual = FastMath.max(0.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_327() throws Exception {
        // Combination: a=0.0f, b=1.0f
        Object actual = FastMath.max(0.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_328() throws Exception {
        // Combination: a=0.0f, b=-1.0f
        Object actual = FastMath.max(0.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_329() throws Exception {
        // Combination: a=0.0f, b=Float.NaN
        Object actual = FastMath.max(0.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_330() throws Exception {
        // Combination: a=0.0f, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.max(0.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_331() throws Exception {
        // Combination: a=1.0f, b=0.0f
        Object actual = FastMath.max(1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_332() throws Exception {
        // Combination: a=1.0f, b=1.0f
        Object actual = FastMath.max(1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_333() throws Exception {
        // Combination: a=1.0f, b=-1.0f
        Object actual = FastMath.max(1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_334() throws Exception {
        // Combination: a=1.0f, b=Float.NaN
        Object actual = FastMath.max(1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_335() throws Exception {
        // Combination: a=1.0f, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.max(1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_336() throws Exception {
        // Combination: a=-1.0f, b=0.0f
        Object actual = FastMath.max(-1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_337() throws Exception {
        // Combination: a=-1.0f, b=1.0f
        Object actual = FastMath.max(-1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_338() throws Exception {
        // Combination: a=-1.0f, b=-1.0f
        Object actual = FastMath.max(-1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_339() throws Exception {
        // Combination: a=-1.0f, b=Float.NaN
        Object actual = FastMath.max(-1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_340() throws Exception {
        // Combination: a=-1.0f, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.max(-1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_341() throws Exception {
        // Combination: a=Float.NaN, b=0.0f
        Object actual = FastMath.max(Float.NaN, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_342() throws Exception {
        // Combination: a=Float.NaN, b=1.0f
        Object actual = FastMath.max(Float.NaN, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_343() throws Exception {
        // Combination: a=Float.NaN, b=-1.0f
        Object actual = FastMath.max(Float.NaN, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_344() throws Exception {
        // Combination: a=Float.NaN, b=Float.NaN
        Object actual = FastMath.max(Float.NaN, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_345() throws Exception {
        // Combination: a=Float.NaN, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.max(Float.NaN, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_346() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=0.0f
        Object actual = FastMath.max(Float.POSITIVE_INFINITY, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_347() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=1.0f
        Object actual = FastMath.max(Float.POSITIVE_INFINITY, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_348() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=-1.0f
        Object actual = FastMath.max(Float.POSITIVE_INFINITY, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_349() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=Float.NaN
        Object actual = FastMath.max(Float.POSITIVE_INFINITY, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_350() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.max(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_351() throws Exception {
        // Combination: a=0.0d, b=0.0d
        Object actual = FastMath.max(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_352() throws Exception {
        // Combination: a=0.0d, b=1.0d
        Object actual = FastMath.max(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_353() throws Exception {
        // Combination: a=0.0d, b=-1.0d
        Object actual = FastMath.max(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_354() throws Exception {
        // Combination: a=0.0d, b=Double.NaN
        Object actual = FastMath.max(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_355() throws Exception {
        // Combination: a=0.0d, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.max(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_356() throws Exception {
        // Combination: a=1.0d, b=0.0d
        Object actual = FastMath.max(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_357() throws Exception {
        // Combination: a=1.0d, b=1.0d
        Object actual = FastMath.max(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_358() throws Exception {
        // Combination: a=1.0d, b=-1.0d
        Object actual = FastMath.max(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_359() throws Exception {
        // Combination: a=1.0d, b=Double.NaN
        Object actual = FastMath.max(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_360() throws Exception {
        // Combination: a=1.0d, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.max(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_361() throws Exception {
        // Combination: a=-1.0d, b=0.0d
        Object actual = FastMath.max(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_362() throws Exception {
        // Combination: a=-1.0d, b=1.0d
        Object actual = FastMath.max(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_363() throws Exception {
        // Combination: a=-1.0d, b=-1.0d
        Object actual = FastMath.max(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_364() throws Exception {
        // Combination: a=-1.0d, b=Double.NaN
        Object actual = FastMath.max(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_365() throws Exception {
        // Combination: a=-1.0d, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.max(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_366() throws Exception {
        // Combination: a=Double.NaN, b=0.0d
        Object actual = FastMath.max(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_367() throws Exception {
        // Combination: a=Double.NaN, b=1.0d
        Object actual = FastMath.max(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_368() throws Exception {
        // Combination: a=Double.NaN, b=-1.0d
        Object actual = FastMath.max(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_369() throws Exception {
        // Combination: a=Double.NaN, b=Double.NaN
        Object actual = FastMath.max(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_370() throws Exception {
        // Combination: a=Double.NaN, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.max(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_371() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=0.0d
        Object actual = FastMath.max(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_372() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=1.0d
        Object actual = FastMath.max(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_373() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=-1.0d
        Object actual = FastMath.max(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_374() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=Double.NaN
        Object actual = FastMath.max(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_375() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.max(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_376() throws Exception {
        // Combination: x=0.0d, y=0.0d
        Object actual = FastMath.hypot(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_377() throws Exception {
        // Combination: x=0.0d, y=1.0d
        Object actual = FastMath.hypot(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_378() throws Exception {
        // Combination: x=0.0d, y=-1.0d
        Object actual = FastMath.hypot(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_379() throws Exception {
        // Combination: x=0.0d, y=Double.NaN
        Object actual = FastMath.hypot(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_380() throws Exception {
        // Combination: x=0.0d, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.hypot(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_381() throws Exception {
        // Combination: x=1.0d, y=0.0d
        Object actual = FastMath.hypot(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_382() throws Exception {
        // Combination: x=1.0d, y=1.0d
        Object actual = FastMath.hypot(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.4142135623730951", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_383() throws Exception {
        // Combination: x=1.0d, y=-1.0d
        Object actual = FastMath.hypot(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.4142135623730951", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_384() throws Exception {
        // Combination: x=1.0d, y=Double.NaN
        Object actual = FastMath.hypot(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_385() throws Exception {
        // Combination: x=1.0d, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.hypot(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_386() throws Exception {
        // Combination: x=-1.0d, y=0.0d
        Object actual = FastMath.hypot(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_387() throws Exception {
        // Combination: x=-1.0d, y=1.0d
        Object actual = FastMath.hypot(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.4142135623730951", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_388() throws Exception {
        // Combination: x=-1.0d, y=-1.0d
        Object actual = FastMath.hypot(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.4142135623730951", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_389() throws Exception {
        // Combination: x=-1.0d, y=Double.NaN
        Object actual = FastMath.hypot(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_390() throws Exception {
        // Combination: x=-1.0d, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.hypot(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_391() throws Exception {
        // Combination: x=Double.NaN, y=0.0d
        Object actual = FastMath.hypot(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_392() throws Exception {
        // Combination: x=Double.NaN, y=1.0d
        Object actual = FastMath.hypot(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_393() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d
        Object actual = FastMath.hypot(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_394() throws Exception {
        // Combination: x=Double.NaN, y=Double.NaN
        Object actual = FastMath.hypot(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_395() throws Exception {
        // Combination: x=Double.NaN, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.hypot(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_396() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=0.0d
        Object actual = FastMath.hypot(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_397() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=1.0d
        Object actual = FastMath.hypot(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_398() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=-1.0d
        Object actual = FastMath.hypot(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_399() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.NaN
        Object actual = FastMath.hypot(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hypot_pairwise_400() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.hypot(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_401() throws Exception {
        // Combination: dividend=0.0d, divisor=0.0d
        Object actual = FastMath.IEEEremainder(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_402() throws Exception {
        // Combination: dividend=0.0d, divisor=1.0d
        Object actual = FastMath.IEEEremainder(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_403() throws Exception {
        // Combination: dividend=0.0d, divisor=-1.0d
        Object actual = FastMath.IEEEremainder(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_404() throws Exception {
        // Combination: dividend=0.0d, divisor=Double.NaN
        Object actual = FastMath.IEEEremainder(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_405() throws Exception {
        // Combination: dividend=0.0d, divisor=Double.POSITIVE_INFINITY
        Object actual = FastMath.IEEEremainder(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_406() throws Exception {
        // Combination: dividend=1.0d, divisor=0.0d
        Object actual = FastMath.IEEEremainder(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_407() throws Exception {
        // Combination: dividend=1.0d, divisor=1.0d
        Object actual = FastMath.IEEEremainder(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_408() throws Exception {
        // Combination: dividend=1.0d, divisor=-1.0d
        Object actual = FastMath.IEEEremainder(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_409() throws Exception {
        // Combination: dividend=1.0d, divisor=Double.NaN
        Object actual = FastMath.IEEEremainder(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_410() throws Exception {
        // Combination: dividend=1.0d, divisor=Double.POSITIVE_INFINITY
        Object actual = FastMath.IEEEremainder(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_411() throws Exception {
        // Combination: dividend=-1.0d, divisor=0.0d
        Object actual = FastMath.IEEEremainder(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_412() throws Exception {
        // Combination: dividend=-1.0d, divisor=1.0d
        Object actual = FastMath.IEEEremainder(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_413() throws Exception {
        // Combination: dividend=-1.0d, divisor=-1.0d
        Object actual = FastMath.IEEEremainder(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_414() throws Exception {
        // Combination: dividend=-1.0d, divisor=Double.NaN
        Object actual = FastMath.IEEEremainder(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_415() throws Exception {
        // Combination: dividend=-1.0d, divisor=Double.POSITIVE_INFINITY
        Object actual = FastMath.IEEEremainder(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_416() throws Exception {
        // Combination: dividend=Double.NaN, divisor=0.0d
        Object actual = FastMath.IEEEremainder(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_417() throws Exception {
        // Combination: dividend=Double.NaN, divisor=1.0d
        Object actual = FastMath.IEEEremainder(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_418() throws Exception {
        // Combination: dividend=Double.NaN, divisor=-1.0d
        Object actual = FastMath.IEEEremainder(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_419() throws Exception {
        // Combination: dividend=Double.NaN, divisor=Double.NaN
        Object actual = FastMath.IEEEremainder(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_420() throws Exception {
        // Combination: dividend=Double.NaN, divisor=Double.POSITIVE_INFINITY
        Object actual = FastMath.IEEEremainder(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_421() throws Exception {
        // Combination: dividend=Double.POSITIVE_INFINITY, divisor=0.0d
        Object actual = FastMath.IEEEremainder(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_422() throws Exception {
        // Combination: dividend=Double.POSITIVE_INFINITY, divisor=1.0d
        Object actual = FastMath.IEEEremainder(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_423() throws Exception {
        // Combination: dividend=Double.POSITIVE_INFINITY, divisor=-1.0d
        Object actual = FastMath.IEEEremainder(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_424() throws Exception {
        // Combination: dividend=Double.POSITIVE_INFINITY, divisor=Double.NaN
        Object actual = FastMath.IEEEremainder(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_IEEEremainder_pairwise_425() throws Exception {
        // Combination: dividend=Double.POSITIVE_INFINITY, divisor=Double.POSITIVE_INFINITY
        Object actual = FastMath.IEEEremainder(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_426() throws Exception {
        // Combination: magnitude=0.0d, sign=0.0d
        Object actual = FastMath.copySign(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_427() throws Exception {
        // Combination: magnitude=0.0d, sign=1.0d
        Object actual = FastMath.copySign(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_428() throws Exception {
        // Combination: magnitude=0.0d, sign=-1.0d
        Object actual = FastMath.copySign(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_429() throws Exception {
        // Combination: magnitude=0.0d, sign=Double.NaN
        Object actual = FastMath.copySign(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_430() throws Exception {
        // Combination: magnitude=0.0d, sign=Double.POSITIVE_INFINITY
        Object actual = FastMath.copySign(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_431() throws Exception {
        // Combination: magnitude=1.0d, sign=0.0d
        Object actual = FastMath.copySign(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_432() throws Exception {
        // Combination: magnitude=1.0d, sign=1.0d
        Object actual = FastMath.copySign(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_433() throws Exception {
        // Combination: magnitude=1.0d, sign=-1.0d
        Object actual = FastMath.copySign(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_434() throws Exception {
        // Combination: magnitude=1.0d, sign=Double.NaN
        Object actual = FastMath.copySign(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_435() throws Exception {
        // Combination: magnitude=1.0d, sign=Double.POSITIVE_INFINITY
        Object actual = FastMath.copySign(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_436() throws Exception {
        // Combination: magnitude=-1.0d, sign=0.0d
        Object actual = FastMath.copySign(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_437() throws Exception {
        // Combination: magnitude=-1.0d, sign=1.0d
        Object actual = FastMath.copySign(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_438() throws Exception {
        // Combination: magnitude=-1.0d, sign=-1.0d
        Object actual = FastMath.copySign(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_439() throws Exception {
        // Combination: magnitude=-1.0d, sign=Double.NaN
        Object actual = FastMath.copySign(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_440() throws Exception {
        // Combination: magnitude=-1.0d, sign=Double.POSITIVE_INFINITY
        Object actual = FastMath.copySign(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_441() throws Exception {
        // Combination: magnitude=Double.NaN, sign=0.0d
        Object actual = FastMath.copySign(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_442() throws Exception {
        // Combination: magnitude=Double.NaN, sign=1.0d
        Object actual = FastMath.copySign(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_443() throws Exception {
        // Combination: magnitude=Double.NaN, sign=-1.0d
        Object actual = FastMath.copySign(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_444() throws Exception {
        // Combination: magnitude=Double.NaN, sign=Double.NaN
        Object actual = FastMath.copySign(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_445() throws Exception {
        // Combination: magnitude=Double.NaN, sign=Double.POSITIVE_INFINITY
        Object actual = FastMath.copySign(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_446() throws Exception {
        // Combination: magnitude=Double.POSITIVE_INFINITY, sign=0.0d
        Object actual = FastMath.copySign(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_447() throws Exception {
        // Combination: magnitude=Double.POSITIVE_INFINITY, sign=1.0d
        Object actual = FastMath.copySign(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_448() throws Exception {
        // Combination: magnitude=Double.POSITIVE_INFINITY, sign=-1.0d
        Object actual = FastMath.copySign(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_449() throws Exception {
        // Combination: magnitude=Double.POSITIVE_INFINITY, sign=Double.NaN
        Object actual = FastMath.copySign(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_450() throws Exception {
        // Combination: magnitude=Double.POSITIVE_INFINITY, sign=Double.POSITIVE_INFINITY
        Object actual = FastMath.copySign(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_451() throws Exception {
        // Combination: magnitude=0.0f, sign=0.0f
        Object actual = FastMath.copySign(0.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_452() throws Exception {
        // Combination: magnitude=0.0f, sign=1.0f
        Object actual = FastMath.copySign(0.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_453() throws Exception {
        // Combination: magnitude=0.0f, sign=-1.0f
        Object actual = FastMath.copySign(0.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_454() throws Exception {
        // Combination: magnitude=0.0f, sign=Float.NaN
        Object actual = FastMath.copySign(0.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_455() throws Exception {
        // Combination: magnitude=0.0f, sign=Float.POSITIVE_INFINITY
        Object actual = FastMath.copySign(0.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_456() throws Exception {
        // Combination: magnitude=1.0f, sign=0.0f
        Object actual = FastMath.copySign(1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_457() throws Exception {
        // Combination: magnitude=1.0f, sign=1.0f
        Object actual = FastMath.copySign(1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_458() throws Exception {
        // Combination: magnitude=1.0f, sign=-1.0f
        Object actual = FastMath.copySign(1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_459() throws Exception {
        // Combination: magnitude=1.0f, sign=Float.NaN
        Object actual = FastMath.copySign(1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_460() throws Exception {
        // Combination: magnitude=1.0f, sign=Float.POSITIVE_INFINITY
        Object actual = FastMath.copySign(1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_461() throws Exception {
        // Combination: magnitude=-1.0f, sign=0.0f
        Object actual = FastMath.copySign(-1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_462() throws Exception {
        // Combination: magnitude=-1.0f, sign=1.0f
        Object actual = FastMath.copySign(-1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_463() throws Exception {
        // Combination: magnitude=-1.0f, sign=-1.0f
        Object actual = FastMath.copySign(-1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_464() throws Exception {
        // Combination: magnitude=-1.0f, sign=Float.NaN
        Object actual = FastMath.copySign(-1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_465() throws Exception {
        // Combination: magnitude=-1.0f, sign=Float.POSITIVE_INFINITY
        Object actual = FastMath.copySign(-1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_466() throws Exception {
        // Combination: magnitude=Float.NaN, sign=0.0f
        Object actual = FastMath.copySign(Float.NaN, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_467() throws Exception {
        // Combination: magnitude=Float.NaN, sign=1.0f
        Object actual = FastMath.copySign(Float.NaN, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_468() throws Exception {
        // Combination: magnitude=Float.NaN, sign=-1.0f
        Object actual = FastMath.copySign(Float.NaN, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_469() throws Exception {
        // Combination: magnitude=Float.NaN, sign=Float.NaN
        Object actual = FastMath.copySign(Float.NaN, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_470() throws Exception {
        // Combination: magnitude=Float.NaN, sign=Float.POSITIVE_INFINITY
        Object actual = FastMath.copySign(Float.NaN, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_471() throws Exception {
        // Combination: magnitude=Float.POSITIVE_INFINITY, sign=0.0f
        Object actual = FastMath.copySign(Float.POSITIVE_INFINITY, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_472() throws Exception {
        // Combination: magnitude=Float.POSITIVE_INFINITY, sign=1.0f
        Object actual = FastMath.copySign(Float.POSITIVE_INFINITY, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_473() throws Exception {
        // Combination: magnitude=Float.POSITIVE_INFINITY, sign=-1.0f
        Object actual = FastMath.copySign(Float.POSITIVE_INFINITY, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_474() throws Exception {
        // Combination: magnitude=Float.POSITIVE_INFINITY, sign=Float.NaN
        Object actual = FastMath.copySign(Float.POSITIVE_INFINITY, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_copySign_pairwise_475() throws Exception {
        // Combination: magnitude=Float.POSITIVE_INFINITY, sign=Float.POSITIVE_INFINITY
        Object actual = FastMath.copySign(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

}
