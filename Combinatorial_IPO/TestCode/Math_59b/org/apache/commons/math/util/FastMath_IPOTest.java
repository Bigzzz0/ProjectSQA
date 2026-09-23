package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for FastMath.
 */
public class FastMath_IPOTest {
    @Test(timeout = 4000)
    public void test_pow_pairwise_001() throws Exception {
        // Combination: x=0.0d, y=0.0d
        Object actual = FastMath.pow(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_002() throws Exception {
        // Combination: x=0.0d, y=1.0d
        Object actual = FastMath.pow(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_003() throws Exception {
        // Combination: x=0.0d, y=-1.0d
        Object actual = FastMath.pow(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_004() throws Exception {
        // Combination: x=0.0d, y=Double.NaN
        Object actual = FastMath.pow(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_005() throws Exception {
        // Combination: x=0.0d, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.pow(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_006() throws Exception {
        // Combination: x=1.0d, y=0.0d
        Object actual = FastMath.pow(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_007() throws Exception {
        // Combination: x=1.0d, y=1.0d
        Object actual = FastMath.pow(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_008() throws Exception {
        // Combination: x=1.0d, y=-1.0d
        Object actual = FastMath.pow(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_009() throws Exception {
        // Combination: x=1.0d, y=Double.NaN
        Object actual = FastMath.pow(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_010() throws Exception {
        // Combination: x=1.0d, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.pow(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_011() throws Exception {
        // Combination: x=-1.0d, y=0.0d
        Object actual = FastMath.pow(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_012() throws Exception {
        // Combination: x=-1.0d, y=1.0d
        Object actual = FastMath.pow(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_013() throws Exception {
        // Combination: x=-1.0d, y=-1.0d
        Object actual = FastMath.pow(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_014() throws Exception {
        // Combination: x=-1.0d, y=Double.NaN
        Object actual = FastMath.pow(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_015() throws Exception {
        // Combination: x=-1.0d, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.pow(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_016() throws Exception {
        // Combination: x=Double.NaN, y=0.0d
        Object actual = FastMath.pow(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_017() throws Exception {
        // Combination: x=Double.NaN, y=1.0d
        Object actual = FastMath.pow(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_018() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d
        Object actual = FastMath.pow(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_019() throws Exception {
        // Combination: x=Double.NaN, y=Double.NaN
        Object actual = FastMath.pow(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_020() throws Exception {
        // Combination: x=Double.NaN, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.pow(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_021() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=0.0d
        Object actual = FastMath.pow(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_022() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=1.0d
        Object actual = FastMath.pow(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_023() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=-1.0d
        Object actual = FastMath.pow(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_024() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.NaN
        Object actual = FastMath.pow(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_025() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.POSITIVE_INFINITY
        Object actual = FastMath.pow(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_026() throws Exception {
        // Combination: y=0.0d, x=0.0d
        Object actual = FastMath.atan2(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_027() throws Exception {
        // Combination: y=1.0d, x=0.0d
        Object actual = FastMath.atan2(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5707963267948966", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_028() throws Exception {
        // Combination: y=-1.0d, x=0.0d
        Object actual = FastMath.atan2(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.5707963267948966", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_029() throws Exception {
        // Combination: y=Double.NaN, x=0.0d
        Object actual = FastMath.atan2(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_030() throws Exception {
        // Combination: y=Double.POSITIVE_INFINITY, x=0.0d
        Object actual = FastMath.atan2(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5707963267948966", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_031() throws Exception {
        // Combination: y=0.0d, x=1.0d
        Object actual = FastMath.atan2(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_032() throws Exception {
        // Combination: y=1.0d, x=1.0d
        Object actual = FastMath.atan2(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.7853981633974483", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_033() throws Exception {
        // Combination: y=-1.0d, x=1.0d
        Object actual = FastMath.atan2(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.7853981633974483", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_034() throws Exception {
        // Combination: y=Double.NaN, x=1.0d
        Object actual = FastMath.atan2(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_035() throws Exception {
        // Combination: y=Double.POSITIVE_INFINITY, x=1.0d
        Object actual = FastMath.atan2(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5707963267948966", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_036() throws Exception {
        // Combination: y=0.0d, x=-1.0d
        Object actual = FastMath.atan2(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("3.141592653589793", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_037() throws Exception {
        // Combination: y=1.0d, x=-1.0d
        Object actual = FastMath.atan2(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("2.356194490192345", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_038() throws Exception {
        // Combination: y=-1.0d, x=-1.0d
        Object actual = FastMath.atan2(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-2.356194490192345", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_039() throws Exception {
        // Combination: y=Double.NaN, x=-1.0d
        Object actual = FastMath.atan2(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_040() throws Exception {
        // Combination: y=Double.POSITIVE_INFINITY, x=-1.0d
        Object actual = FastMath.atan2(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5707963267948966", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_041() throws Exception {
        // Combination: y=0.0d, x=Double.NaN
        Object actual = FastMath.atan2(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_042() throws Exception {
        // Combination: y=1.0d, x=Double.NaN
        Object actual = FastMath.atan2(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_043() throws Exception {
        // Combination: y=-1.0d, x=Double.NaN
        Object actual = FastMath.atan2(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_044() throws Exception {
        // Combination: y=Double.NaN, x=Double.NaN
        Object actual = FastMath.atan2(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_045() throws Exception {
        // Combination: y=Double.POSITIVE_INFINITY, x=Double.NaN
        Object actual = FastMath.atan2(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_046() throws Exception {
        // Combination: y=0.0d, x=Double.POSITIVE_INFINITY
        Object actual = FastMath.atan2(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_047() throws Exception {
        // Combination: y=1.0d, x=Double.POSITIVE_INFINITY
        Object actual = FastMath.atan2(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_048() throws Exception {
        // Combination: y=-1.0d, x=Double.POSITIVE_INFINITY
        Object actual = FastMath.atan2(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_049() throws Exception {
        // Combination: y=Double.NaN, x=Double.POSITIVE_INFINITY
        Object actual = FastMath.atan2(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan2_pairwise_050() throws Exception {
        // Combination: y=Double.POSITIVE_INFINITY, x=Double.POSITIVE_INFINITY
        Object actual = FastMath.atan2(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.7853981633974483", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_051() throws Exception {
        // Combination: d=0.0d, direction=0.0d
        Object actual = FastMath.nextAfter(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("4.9E-324", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_052() throws Exception {
        // Combination: d=0.0d, direction=1.0d
        Object actual = FastMath.nextAfter(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("4.9E-324", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_053() throws Exception {
        // Combination: d=0.0d, direction=-1.0d
        Object actual = FastMath.nextAfter(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-4.9E-324", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_054() throws Exception {
        // Combination: d=0.0d, direction=Double.NaN
        Object actual = FastMath.nextAfter(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("4.9E-324", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_055() throws Exception {
        // Combination: d=0.0d, direction=Double.POSITIVE_INFINITY
        Object actual = FastMath.nextAfter(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("4.9E-324", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_056() throws Exception {
        // Combination: d=1.0d, direction=0.0d
        Object actual = FastMath.nextAfter(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.9999999999999999", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_057() throws Exception {
        // Combination: d=1.0d, direction=1.0d
        Object actual = FastMath.nextAfter(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0000000000000002", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_058() throws Exception {
        // Combination: d=1.0d, direction=-1.0d
        Object actual = FastMath.nextAfter(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.9999999999999999", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_059() throws Exception {
        // Combination: d=1.0d, direction=Double.NaN
        Object actual = FastMath.nextAfter(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.9999999999999999", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_060() throws Exception {
        // Combination: d=1.0d, direction=Double.POSITIVE_INFINITY
        Object actual = FastMath.nextAfter(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0000000000000002", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_061() throws Exception {
        // Combination: d=-1.0d, direction=0.0d
        Object actual = FastMath.nextAfter(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.9999999999999999", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_062() throws Exception {
        // Combination: d=-1.0d, direction=1.0d
        Object actual = FastMath.nextAfter(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.9999999999999999", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_063() throws Exception {
        // Combination: d=-1.0d, direction=-1.0d
        Object actual = FastMath.nextAfter(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0000000000000002", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_064() throws Exception {
        // Combination: d=-1.0d, direction=Double.NaN
        Object actual = FastMath.nextAfter(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.9999999999999999", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_065() throws Exception {
        // Combination: d=-1.0d, direction=Double.POSITIVE_INFINITY
        Object actual = FastMath.nextAfter(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.9999999999999999", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_066() throws Exception {
        // Combination: d=Double.NaN, direction=0.0d
        Object actual = FastMath.nextAfter(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_067() throws Exception {
        // Combination: d=Double.NaN, direction=1.0d
        Object actual = FastMath.nextAfter(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_068() throws Exception {
        // Combination: d=Double.NaN, direction=-1.0d
        Object actual = FastMath.nextAfter(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_069() throws Exception {
        // Combination: d=Double.NaN, direction=Double.NaN
        Object actual = FastMath.nextAfter(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_070() throws Exception {
        // Combination: d=Double.NaN, direction=Double.POSITIVE_INFINITY
        Object actual = FastMath.nextAfter(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_071() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, direction=0.0d
        Object actual = FastMath.nextAfter(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_072() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, direction=1.0d
        Object actual = FastMath.nextAfter(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_073() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, direction=-1.0d
        Object actual = FastMath.nextAfter(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_074() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, direction=Double.NaN
        Object actual = FastMath.nextAfter(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_075() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, direction=Double.POSITIVE_INFINITY
        Object actual = FastMath.nextAfter(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_076() throws Exception {
        // Combination: a=0, b=0
        Object actual = FastMath.min(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_077() throws Exception {
        // Combination: a=0, b=1
        Object actual = FastMath.min(0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_078() throws Exception {
        // Combination: a=0, b=-1
        Object actual = FastMath.min(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_079() throws Exception {
        // Combination: a=0, b=Integer.MAX_VALUE
        Object actual = FastMath.min(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_080() throws Exception {
        // Combination: a=0, b=Integer.MIN_VALUE
        Object actual = FastMath.min(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_081() throws Exception {
        // Combination: a=1, b=0
        Object actual = FastMath.min(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_082() throws Exception {
        // Combination: a=1, b=1
        Object actual = FastMath.min(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_083() throws Exception {
        // Combination: a=1, b=-1
        Object actual = FastMath.min(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_084() throws Exception {
        // Combination: a=1, b=Integer.MAX_VALUE
        Object actual = FastMath.min(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_085() throws Exception {
        // Combination: a=1, b=Integer.MIN_VALUE
        Object actual = FastMath.min(1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_086() throws Exception {
        // Combination: a=-1, b=0
        Object actual = FastMath.min(-1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_087() throws Exception {
        // Combination: a=-1, b=1
        Object actual = FastMath.min(-1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_088() throws Exception {
        // Combination: a=-1, b=-1
        Object actual = FastMath.min(-1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_089() throws Exception {
        // Combination: a=-1, b=Integer.MAX_VALUE
        Object actual = FastMath.min(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_090() throws Exception {
        // Combination: a=-1, b=Integer.MIN_VALUE
        Object actual = FastMath.min(-1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_091() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=0
        Object actual = FastMath.min(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_092() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=1
        Object actual = FastMath.min(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_093() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=-1
        Object actual = FastMath.min(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_094() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MAX_VALUE
        Object actual = FastMath.min(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_095() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MIN_VALUE
        Object actual = FastMath.min(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_096() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=0
        Object actual = FastMath.min(Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_097() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=1
        Object actual = FastMath.min(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_098() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=-1
        Object actual = FastMath.min(Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_099() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MAX_VALUE
        Object actual = FastMath.min(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_100() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MIN_VALUE
        Object actual = FastMath.min(Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_101() throws Exception {
        // Combination: a=0L, b=0L
        Object actual = FastMath.min(0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_102() throws Exception {
        // Combination: a=0L, b=1L
        Object actual = FastMath.min(0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_103() throws Exception {
        // Combination: a=0L, b=-1L
        Object actual = FastMath.min(0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_104() throws Exception {
        // Combination: a=0L, b=Long.MAX_VALUE
        Object actual = FastMath.min(0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_105() throws Exception {
        // Combination: a=0L, b=Long.MIN_VALUE
        Object actual = FastMath.min(0L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_106() throws Exception {
        // Combination: a=1L, b=0L
        Object actual = FastMath.min(1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_107() throws Exception {
        // Combination: a=1L, b=1L
        Object actual = FastMath.min(1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_108() throws Exception {
        // Combination: a=1L, b=-1L
        Object actual = FastMath.min(1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_109() throws Exception {
        // Combination: a=1L, b=Long.MAX_VALUE
        Object actual = FastMath.min(1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_110() throws Exception {
        // Combination: a=1L, b=Long.MIN_VALUE
        Object actual = FastMath.min(1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_111() throws Exception {
        // Combination: a=-1L, b=0L
        Object actual = FastMath.min(-1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_112() throws Exception {
        // Combination: a=-1L, b=1L
        Object actual = FastMath.min(-1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_113() throws Exception {
        // Combination: a=-1L, b=-1L
        Object actual = FastMath.min(-1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_114() throws Exception {
        // Combination: a=-1L, b=Long.MAX_VALUE
        Object actual = FastMath.min(-1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_115() throws Exception {
        // Combination: a=-1L, b=Long.MIN_VALUE
        Object actual = FastMath.min(-1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_116() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=0L
        Object actual = FastMath.min(Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_117() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=1L
        Object actual = FastMath.min(Long.MAX_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_118() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=-1L
        Object actual = FastMath.min(Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_119() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE
        Object actual = FastMath.min(Long.MAX_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_120() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MIN_VALUE
        Object actual = FastMath.min(Long.MAX_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_121() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=0L
        Object actual = FastMath.min(Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_122() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=1L
        Object actual = FastMath.min(Long.MIN_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_123() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=-1L
        Object actual = FastMath.min(Long.MIN_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_124() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MAX_VALUE
        Object actual = FastMath.min(Long.MIN_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_125() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MIN_VALUE
        Object actual = FastMath.min(Long.MIN_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_126() throws Exception {
        // Combination: a=0.0f, b=0.0f
        Object actual = FastMath.min(0.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_127() throws Exception {
        // Combination: a=0.0f, b=1.0f
        Object actual = FastMath.min(0.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_128() throws Exception {
        // Combination: a=0.0f, b=-1.0f
        Object actual = FastMath.min(0.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_129() throws Exception {
        // Combination: a=0.0f, b=Float.NaN
        Object actual = FastMath.min(0.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_130() throws Exception {
        // Combination: a=0.0f, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.min(0.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_131() throws Exception {
        // Combination: a=1.0f, b=0.0f
        Object actual = FastMath.min(1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_132() throws Exception {
        // Combination: a=1.0f, b=1.0f
        Object actual = FastMath.min(1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_133() throws Exception {
        // Combination: a=1.0f, b=-1.0f
        Object actual = FastMath.min(1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_134() throws Exception {
        // Combination: a=1.0f, b=Float.NaN
        Object actual = FastMath.min(1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_135() throws Exception {
        // Combination: a=1.0f, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.min(1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_136() throws Exception {
        // Combination: a=-1.0f, b=0.0f
        Object actual = FastMath.min(-1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_137() throws Exception {
        // Combination: a=-1.0f, b=1.0f
        Object actual = FastMath.min(-1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_138() throws Exception {
        // Combination: a=-1.0f, b=-1.0f
        Object actual = FastMath.min(-1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_139() throws Exception {
        // Combination: a=-1.0f, b=Float.NaN
        Object actual = FastMath.min(-1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_140() throws Exception {
        // Combination: a=-1.0f, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.min(-1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_141() throws Exception {
        // Combination: a=Float.NaN, b=0.0f
        Object actual = FastMath.min(Float.NaN, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_142() throws Exception {
        // Combination: a=Float.NaN, b=1.0f
        Object actual = FastMath.min(Float.NaN, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_143() throws Exception {
        // Combination: a=Float.NaN, b=-1.0f
        Object actual = FastMath.min(Float.NaN, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_144() throws Exception {
        // Combination: a=Float.NaN, b=Float.NaN
        Object actual = FastMath.min(Float.NaN, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_145() throws Exception {
        // Combination: a=Float.NaN, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.min(Float.NaN, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_146() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=0.0f
        Object actual = FastMath.min(Float.POSITIVE_INFINITY, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_147() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=1.0f
        Object actual = FastMath.min(Float.POSITIVE_INFINITY, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_148() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=-1.0f
        Object actual = FastMath.min(Float.POSITIVE_INFINITY, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_149() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=Float.NaN
        Object actual = FastMath.min(Float.POSITIVE_INFINITY, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_150() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.min(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_151() throws Exception {
        // Combination: a=0.0d, b=0.0d
        Object actual = FastMath.min(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_152() throws Exception {
        // Combination: a=0.0d, b=1.0d
        Object actual = FastMath.min(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_153() throws Exception {
        // Combination: a=0.0d, b=-1.0d
        Object actual = FastMath.min(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_154() throws Exception {
        // Combination: a=0.0d, b=Double.NaN
        Object actual = FastMath.min(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_155() throws Exception {
        // Combination: a=0.0d, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.min(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_156() throws Exception {
        // Combination: a=1.0d, b=0.0d
        Object actual = FastMath.min(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_157() throws Exception {
        // Combination: a=1.0d, b=1.0d
        Object actual = FastMath.min(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_158() throws Exception {
        // Combination: a=1.0d, b=-1.0d
        Object actual = FastMath.min(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_159() throws Exception {
        // Combination: a=1.0d, b=Double.NaN
        Object actual = FastMath.min(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_160() throws Exception {
        // Combination: a=1.0d, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.min(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_161() throws Exception {
        // Combination: a=-1.0d, b=0.0d
        Object actual = FastMath.min(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_162() throws Exception {
        // Combination: a=-1.0d, b=1.0d
        Object actual = FastMath.min(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_163() throws Exception {
        // Combination: a=-1.0d, b=-1.0d
        Object actual = FastMath.min(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_164() throws Exception {
        // Combination: a=-1.0d, b=Double.NaN
        Object actual = FastMath.min(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_165() throws Exception {
        // Combination: a=-1.0d, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.min(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_166() throws Exception {
        // Combination: a=Double.NaN, b=0.0d
        Object actual = FastMath.min(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_167() throws Exception {
        // Combination: a=Double.NaN, b=1.0d
        Object actual = FastMath.min(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_168() throws Exception {
        // Combination: a=Double.NaN, b=-1.0d
        Object actual = FastMath.min(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_169() throws Exception {
        // Combination: a=Double.NaN, b=Double.NaN
        Object actual = FastMath.min(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_170() throws Exception {
        // Combination: a=Double.NaN, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.min(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_171() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=0.0d
        Object actual = FastMath.min(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_172() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=1.0d
        Object actual = FastMath.min(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_173() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=-1.0d
        Object actual = FastMath.min(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_174() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=Double.NaN
        Object actual = FastMath.min(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_min_pairwise_175() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.min(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_176() throws Exception {
        // Combination: a=0, b=0
        Object actual = FastMath.max(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_177() throws Exception {
        // Combination: a=0, b=1
        Object actual = FastMath.max(0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_178() throws Exception {
        // Combination: a=0, b=-1
        Object actual = FastMath.max(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_179() throws Exception {
        // Combination: a=0, b=Integer.MAX_VALUE
        Object actual = FastMath.max(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_180() throws Exception {
        // Combination: a=0, b=Integer.MIN_VALUE
        Object actual = FastMath.max(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_181() throws Exception {
        // Combination: a=1, b=0
        Object actual = FastMath.max(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_182() throws Exception {
        // Combination: a=1, b=1
        Object actual = FastMath.max(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_183() throws Exception {
        // Combination: a=1, b=-1
        Object actual = FastMath.max(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_184() throws Exception {
        // Combination: a=1, b=Integer.MAX_VALUE
        Object actual = FastMath.max(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_185() throws Exception {
        // Combination: a=1, b=Integer.MIN_VALUE
        Object actual = FastMath.max(1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_186() throws Exception {
        // Combination: a=-1, b=0
        Object actual = FastMath.max(-1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_187() throws Exception {
        // Combination: a=-1, b=1
        Object actual = FastMath.max(-1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_188() throws Exception {
        // Combination: a=-1, b=-1
        Object actual = FastMath.max(-1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_189() throws Exception {
        // Combination: a=-1, b=Integer.MAX_VALUE
        Object actual = FastMath.max(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_190() throws Exception {
        // Combination: a=-1, b=Integer.MIN_VALUE
        Object actual = FastMath.max(-1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_191() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=0
        Object actual = FastMath.max(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_192() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=1
        Object actual = FastMath.max(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_193() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=-1
        Object actual = FastMath.max(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_194() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MAX_VALUE
        Object actual = FastMath.max(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_195() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MIN_VALUE
        Object actual = FastMath.max(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_196() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=0
        Object actual = FastMath.max(Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_197() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=1
        Object actual = FastMath.max(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_198() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=-1
        Object actual = FastMath.max(Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_199() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MAX_VALUE
        Object actual = FastMath.max(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_200() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MIN_VALUE
        Object actual = FastMath.max(Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_201() throws Exception {
        // Combination: a=0L, b=0L
        Object actual = FastMath.max(0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_202() throws Exception {
        // Combination: a=0L, b=1L
        Object actual = FastMath.max(0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_203() throws Exception {
        // Combination: a=0L, b=-1L
        Object actual = FastMath.max(0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_204() throws Exception {
        // Combination: a=0L, b=Long.MAX_VALUE
        Object actual = FastMath.max(0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_205() throws Exception {
        // Combination: a=0L, b=Long.MIN_VALUE
        Object actual = FastMath.max(0L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_206() throws Exception {
        // Combination: a=1L, b=0L
        Object actual = FastMath.max(1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_207() throws Exception {
        // Combination: a=1L, b=1L
        Object actual = FastMath.max(1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_208() throws Exception {
        // Combination: a=1L, b=-1L
        Object actual = FastMath.max(1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_209() throws Exception {
        // Combination: a=1L, b=Long.MAX_VALUE
        Object actual = FastMath.max(1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_210() throws Exception {
        // Combination: a=1L, b=Long.MIN_VALUE
        Object actual = FastMath.max(1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_211() throws Exception {
        // Combination: a=-1L, b=0L
        Object actual = FastMath.max(-1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_212() throws Exception {
        // Combination: a=-1L, b=1L
        Object actual = FastMath.max(-1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_213() throws Exception {
        // Combination: a=-1L, b=-1L
        Object actual = FastMath.max(-1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_214() throws Exception {
        // Combination: a=-1L, b=Long.MAX_VALUE
        Object actual = FastMath.max(-1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_215() throws Exception {
        // Combination: a=-1L, b=Long.MIN_VALUE
        Object actual = FastMath.max(-1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_216() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=0L
        Object actual = FastMath.max(Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_217() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=1L
        Object actual = FastMath.max(Long.MAX_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_218() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=-1L
        Object actual = FastMath.max(Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_219() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE
        Object actual = FastMath.max(Long.MAX_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_220() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MIN_VALUE
        Object actual = FastMath.max(Long.MAX_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_221() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=0L
        Object actual = FastMath.max(Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_222() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=1L
        Object actual = FastMath.max(Long.MIN_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_223() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=-1L
        Object actual = FastMath.max(Long.MIN_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_224() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MAX_VALUE
        Object actual = FastMath.max(Long.MIN_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_225() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MIN_VALUE
        Object actual = FastMath.max(Long.MIN_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_226() throws Exception {
        // Combination: a=0.0f, b=0.0f
        Object actual = FastMath.max(0.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_227() throws Exception {
        // Combination: a=0.0f, b=1.0f
        Object actual = FastMath.max(0.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_228() throws Exception {
        // Combination: a=0.0f, b=-1.0f
        Object actual = FastMath.max(0.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_229() throws Exception {
        // Combination: a=0.0f, b=Float.NaN
        Object actual = FastMath.max(0.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_230() throws Exception {
        // Combination: a=0.0f, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.max(0.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_231() throws Exception {
        // Combination: a=1.0f, b=0.0f
        Object actual = FastMath.max(1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_232() throws Exception {
        // Combination: a=1.0f, b=1.0f
        Object actual = FastMath.max(1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_233() throws Exception {
        // Combination: a=1.0f, b=-1.0f
        Object actual = FastMath.max(1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_234() throws Exception {
        // Combination: a=1.0f, b=Float.NaN
        Object actual = FastMath.max(1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_235() throws Exception {
        // Combination: a=1.0f, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.max(1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_236() throws Exception {
        // Combination: a=-1.0f, b=0.0f
        Object actual = FastMath.max(-1.0f, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_237() throws Exception {
        // Combination: a=-1.0f, b=1.0f
        Object actual = FastMath.max(-1.0f, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_238() throws Exception {
        // Combination: a=-1.0f, b=-1.0f
        Object actual = FastMath.max(-1.0f, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_239() throws Exception {
        // Combination: a=-1.0f, b=Float.NaN
        Object actual = FastMath.max(-1.0f, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_240() throws Exception {
        // Combination: a=-1.0f, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.max(-1.0f, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_241() throws Exception {
        // Combination: a=Float.NaN, b=0.0f
        Object actual = FastMath.max(Float.NaN, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_242() throws Exception {
        // Combination: a=Float.NaN, b=1.0f
        Object actual = FastMath.max(Float.NaN, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_243() throws Exception {
        // Combination: a=Float.NaN, b=-1.0f
        Object actual = FastMath.max(Float.NaN, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_244() throws Exception {
        // Combination: a=Float.NaN, b=Float.NaN
        Object actual = FastMath.max(Float.NaN, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_245() throws Exception {
        // Combination: a=Float.NaN, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.max(Float.NaN, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_246() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=0.0f
        Object actual = FastMath.max(Float.POSITIVE_INFINITY, 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_247() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=1.0f
        Object actual = FastMath.max(Float.POSITIVE_INFINITY, 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_248() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=-1.0f
        Object actual = FastMath.max(Float.POSITIVE_INFINITY, -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_249() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=Float.NaN
        Object actual = FastMath.max(Float.POSITIVE_INFINITY, Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_250() throws Exception {
        // Combination: a=Float.POSITIVE_INFINITY, b=Float.POSITIVE_INFINITY
        Object actual = FastMath.max(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_251() throws Exception {
        // Combination: a=0.0d, b=0.0d
        Object actual = FastMath.max(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_252() throws Exception {
        // Combination: a=0.0d, b=1.0d
        Object actual = FastMath.max(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_253() throws Exception {
        // Combination: a=0.0d, b=-1.0d
        Object actual = FastMath.max(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_254() throws Exception {
        // Combination: a=0.0d, b=Double.NaN
        Object actual = FastMath.max(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_255() throws Exception {
        // Combination: a=0.0d, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.max(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_256() throws Exception {
        // Combination: a=1.0d, b=0.0d
        Object actual = FastMath.max(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_257() throws Exception {
        // Combination: a=1.0d, b=1.0d
        Object actual = FastMath.max(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_258() throws Exception {
        // Combination: a=1.0d, b=-1.0d
        Object actual = FastMath.max(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_259() throws Exception {
        // Combination: a=1.0d, b=Double.NaN
        Object actual = FastMath.max(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_260() throws Exception {
        // Combination: a=1.0d, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.max(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_261() throws Exception {
        // Combination: a=-1.0d, b=0.0d
        Object actual = FastMath.max(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_262() throws Exception {
        // Combination: a=-1.0d, b=1.0d
        Object actual = FastMath.max(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_263() throws Exception {
        // Combination: a=-1.0d, b=-1.0d
        Object actual = FastMath.max(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_264() throws Exception {
        // Combination: a=-1.0d, b=Double.NaN
        Object actual = FastMath.max(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_265() throws Exception {
        // Combination: a=-1.0d, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.max(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_266() throws Exception {
        // Combination: a=Double.NaN, b=0.0d
        Object actual = FastMath.max(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_267() throws Exception {
        // Combination: a=Double.NaN, b=1.0d
        Object actual = FastMath.max(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_268() throws Exception {
        // Combination: a=Double.NaN, b=-1.0d
        Object actual = FastMath.max(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_269() throws Exception {
        // Combination: a=Double.NaN, b=Double.NaN
        Object actual = FastMath.max(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_270() throws Exception {
        // Combination: a=Double.NaN, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.max(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_271() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=0.0d
        Object actual = FastMath.max(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_272() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=1.0d
        Object actual = FastMath.max(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_273() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=-1.0d
        Object actual = FastMath.max(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_274() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=Double.NaN
        Object actual = FastMath.max(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_max_pairwise_275() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, b=Double.POSITIVE_INFINITY
        Object actual = FastMath.max(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

}
