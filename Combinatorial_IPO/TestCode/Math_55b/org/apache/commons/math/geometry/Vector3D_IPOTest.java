package org.apache.commons.math.geometry;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Vector3D.
 */
public class Vector3D_IPOTest {
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
    public void test_getX_pairwise_001() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(0.0d, 0.0d)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_002() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(0.0d, 1.0d)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.5403023058681398", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_003() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(0.0d, -1.0d)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.5403023058681398", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_004() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(0.0d, Double.NaN)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_005() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(0.0d, Double.POSITIVE_INFINITY)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_006() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(1.0d, 0.0d)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.5403023058681398", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_007() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(1.0d, 1.0d)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.2919265817264289", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_008() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(1.0d, -1.0d)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.2919265817264289", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_009() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(1.0d, Double.NaN)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_010() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(1.0d, Double.POSITIVE_INFINITY)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_011() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(-1.0d, 0.0d)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.5403023058681398", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_012() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(-1.0d, 1.0d)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.2919265817264289", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_013() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(-1.0d, -1.0d)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.2919265817264289", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_014() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(-1.0d, Double.NaN)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_015() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(-1.0d, Double.POSITIVE_INFINITY)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_016() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.NaN, 0.0d)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_017() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.NaN, 1.0d)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_018() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.NaN, -1.0d)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_019() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.NaN, Double.NaN)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_020() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.NaN, Double.POSITIVE_INFINITY)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_021() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 0.0d)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_022() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 1.0d)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_023() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, -1.0d)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_024() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.NaN)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getX_pairwise_025() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getX();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_026() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(0.0d, 0.0d)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_027() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(0.0d, 1.0d)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_028() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(0.0d, -1.0d)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_029() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(0.0d, Double.NaN)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_030() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(0.0d, Double.POSITIVE_INFINITY)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_031() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(1.0d, 0.0d)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_032() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(1.0d, 1.0d)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.4546487134128409", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_033() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(1.0d, -1.0d)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.4546487134128409", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_034() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(1.0d, Double.NaN)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_035() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(1.0d, Double.POSITIVE_INFINITY)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_036() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(-1.0d, 0.0d)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_037() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(-1.0d, 1.0d)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.4546487134128409", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_038() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(-1.0d, -1.0d)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.4546487134128409", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_039() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(-1.0d, Double.NaN)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_040() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(-1.0d, Double.POSITIVE_INFINITY)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_041() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.NaN, 0.0d)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_042() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.NaN, 1.0d)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_043() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.NaN, -1.0d)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_044() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.NaN, Double.NaN)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_045() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.NaN, Double.POSITIVE_INFINITY)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_046() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 0.0d)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_047() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 1.0d)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_048() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, -1.0d)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_049() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.NaN)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getY_pairwise_050() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getY();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_051() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(0.0d, 0.0d)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_052() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(0.0d, 1.0d)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_053() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(0.0d, -1.0d)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_054() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(0.0d, Double.NaN)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_055() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(0.0d, Double.POSITIVE_INFINITY)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_056() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(1.0d, 0.0d)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_057() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(1.0d, 1.0d)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_058() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(1.0d, -1.0d)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_059() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(1.0d, Double.NaN)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_060() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(1.0d, Double.POSITIVE_INFINITY)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_061() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(-1.0d, 0.0d)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_062() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(-1.0d, 1.0d)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_063() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(-1.0d, -1.0d)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_064() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(-1.0d, Double.NaN)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_065() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(-1.0d, Double.POSITIVE_INFINITY)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_066() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.NaN, 0.0d)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_067() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.NaN, 1.0d)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_068() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.NaN, -1.0d)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_069() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.NaN, Double.NaN)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_070() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.NaN, Double.POSITIVE_INFINITY)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_071() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 0.0d)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_072() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 1.0d)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_073() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, -1.0d)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_074() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.NaN)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getZ_pairwise_075() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getZ();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_076() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(0.0d, 0.0d)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_077() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(0.0d, 1.0d)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.3817732906760363", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_078() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(0.0d, -1.0d)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.3817732906760363", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_079() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(0.0d, Double.NaN)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_080() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(0.0d, Double.POSITIVE_INFINITY)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_081() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(1.0d, 0.0d)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.3817732906760363", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_082() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(1.0d, 1.0d)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5880462799471662", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_083() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(1.0d, -1.0d)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5880462799471662", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_084() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(1.0d, Double.NaN)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_085() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(1.0d, Double.POSITIVE_INFINITY)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_086() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(-1.0d, 0.0d)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.3817732906760363", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_087() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(-1.0d, 1.0d)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5880462799471662", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_088() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(-1.0d, -1.0d)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5880462799471662", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_089() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(-1.0d, Double.NaN)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_090() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(-1.0d, Double.POSITIVE_INFINITY)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_091() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.NaN, 0.0d)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_092() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.NaN, 1.0d)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_093() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.NaN, -1.0d)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_094() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.NaN, Double.NaN)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_095() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.NaN, Double.POSITIVE_INFINITY)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_096() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 0.0d)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_097() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 1.0d)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_098() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, -1.0d)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_099() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.NaN)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm1_pairwise_100() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getNorm1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_101() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(0.0d, 0.0d)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_102() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(0.0d, 1.0d)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_103() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(0.0d, -1.0d)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_104() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(0.0d, Double.NaN)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_105() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(0.0d, Double.POSITIVE_INFINITY)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_106() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(1.0d, 0.0d)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_107() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(1.0d, 1.0d)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_108() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(1.0d, -1.0d)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_109() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(1.0d, Double.NaN)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_110() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(1.0d, Double.POSITIVE_INFINITY)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_111() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(-1.0d, 0.0d)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_112() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(-1.0d, 1.0d)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_113() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(-1.0d, -1.0d)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_114() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(-1.0d, Double.NaN)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_115() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(-1.0d, Double.POSITIVE_INFINITY)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_116() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.NaN, 0.0d)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_117() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.NaN, 1.0d)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_118() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.NaN, -1.0d)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_119() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.NaN, Double.NaN)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_120() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.NaN, Double.POSITIVE_INFINITY)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_121() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 0.0d)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_122() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 1.0d)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_123() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, -1.0d)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_124() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.NaN)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNorm_pairwise_125() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getNorm();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_126() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(0.0d, 0.0d)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_127() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(0.0d, 1.0d)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_128() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(0.0d, -1.0d)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_129() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(0.0d, Double.NaN)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_130() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(0.0d, Double.POSITIVE_INFINITY)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_131() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(1.0d, 0.0d)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_132() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(1.0d, 1.0d)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_133() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(1.0d, -1.0d)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_134() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(1.0d, Double.NaN)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_135() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(1.0d, Double.POSITIVE_INFINITY)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_136() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(-1.0d, 0.0d)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_137() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(-1.0d, 1.0d)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_138() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(-1.0d, -1.0d)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_139() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(-1.0d, Double.NaN)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_140() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(-1.0d, Double.POSITIVE_INFINITY)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_141() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.NaN, 0.0d)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_142() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.NaN, 1.0d)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_143() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.NaN, -1.0d)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_144() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.NaN, Double.NaN)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_145() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.NaN, Double.POSITIVE_INFINITY)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_146() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 0.0d)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_147() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 1.0d)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_148() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, -1.0d)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_149() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.NaN)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormSq_pairwise_150() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getNormSq();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_151() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(0.0d, 0.0d)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_152() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(0.0d, 1.0d)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_153() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(0.0d, -1.0d)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_154() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(0.0d, Double.NaN)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_155() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(0.0d, Double.POSITIVE_INFINITY)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_156() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(1.0d, 0.0d)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_157() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(1.0d, 1.0d)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_158() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(1.0d, -1.0d)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_159() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(1.0d, Double.NaN)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_160() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(1.0d, Double.POSITIVE_INFINITY)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_161() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(-1.0d, 0.0d)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_162() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(-1.0d, 1.0d)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_163() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(-1.0d, -1.0d)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.8414709848078965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_164() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(-1.0d, Double.NaN)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_165() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(-1.0d, Double.POSITIVE_INFINITY)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_166() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.NaN, 0.0d)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_167() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.NaN, 1.0d)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_168() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.NaN, -1.0d)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_169() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.NaN, Double.NaN)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_170() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.NaN, Double.POSITIVE_INFINITY)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_171() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 0.0d)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_172() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 1.0d)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_173() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, -1.0d)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_174() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.NaN)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNormInf_pairwise_175() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getNormInf();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_176() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(0.0d, 0.0d)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_177() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(0.0d, 1.0d)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_178() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(0.0d, -1.0d)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_179() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(0.0d, Double.NaN)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_180() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(0.0d, Double.POSITIVE_INFINITY)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_181() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(1.0d, 0.0d)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_182() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(1.0d, 1.0d)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_183() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(1.0d, -1.0d)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_184() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(1.0d, Double.NaN)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_185() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(1.0d, Double.POSITIVE_INFINITY)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_186() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(-1.0d, 0.0d)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_187() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(-1.0d, 1.0d)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_188() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(-1.0d, -1.0d)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_189() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(-1.0d, Double.NaN)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_190() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(-1.0d, Double.POSITIVE_INFINITY)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_191() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.NaN, 0.0d)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_192() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.NaN, 1.0d)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_193() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.NaN, -1.0d)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_194() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.NaN, Double.NaN)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_195() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.NaN, Double.POSITIVE_INFINITY)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_196() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 0.0d)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_197() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 1.0d)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_198() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, -1.0d)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_199() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.NaN)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getAlpha_pairwise_200() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getAlpha();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_201() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(0.0d, 0.0d)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_202() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(0.0d, 1.0d)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_203() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(0.0d, -1.0d)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_204() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(0.0d, Double.NaN)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_205() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(0.0d, Double.POSITIVE_INFINITY)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_206() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(1.0d, 0.0d)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_207() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(1.0d, 1.0d)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_208() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(1.0d, -1.0d)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_209() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(1.0d, Double.NaN)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_210() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(1.0d, Double.POSITIVE_INFINITY)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_211() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(-1.0d, 0.0d)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_212() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(-1.0d, 1.0d)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_213() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(-1.0d, -1.0d)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_214() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(-1.0d, Double.NaN)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_215() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(-1.0d, Double.POSITIVE_INFINITY)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_216() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.NaN, 0.0d)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_217() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.NaN, 1.0d)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_218() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.NaN, -1.0d)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_219() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.NaN, Double.NaN)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_220() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.NaN, Double.POSITIVE_INFINITY)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_221() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 0.0d)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_222() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 1.0d)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_223() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, -1.0d)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_224() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.NaN)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDelta_pairwise_225() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getDelta();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_226() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(0.0d, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_227() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(0.0d, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_228() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(0.0d, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_229() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(0.0d, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_230() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(0.0d, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_231() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(1.0d, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_232() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(1.0d, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_233() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(1.0d, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_234() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(1.0d, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_235() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(1.0d, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_236() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(-1.0d, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_237() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(-1.0d, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_238() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(-1.0d, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_239() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(-1.0d, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_240() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(-1.0d, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_241() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.NaN, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_242() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.NaN, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_243() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.NaN, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_244() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.NaN, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_245() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.NaN, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_246() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_247() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_248() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_249() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_250() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_251() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(0.0d, 0.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_252() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(0.0d, 1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_253() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(0.0d, -1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_254() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(0.0d, Double.NaN)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_255() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(0.0d, Double.POSITIVE_INFINITY)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_256() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(1.0d, 0.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_257() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(1.0d, 1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_258() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(1.0d, -1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_259() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(1.0d, Double.NaN)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_260() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(1.0d, Double.POSITIVE_INFINITY)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_261() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(-1.0d, 0.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_262() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(-1.0d, 1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_263() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(-1.0d, -1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_264() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(-1.0d, Double.NaN)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_265() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(-1.0d, Double.POSITIVE_INFINITY)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_266() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.NaN, 0.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_267() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.NaN, 1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_268() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.NaN, -1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_269() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.NaN, Double.NaN)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_270() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.NaN, Double.POSITIVE_INFINITY)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_271() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 0.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_272() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_273() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, -1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_274() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.NaN)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_275() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_276() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=0.0d, other=new Object()
        Object actual = (new Vector3D(0.0d, 0.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_277() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=1.0d, other=new Object()
        Object actual = (new Vector3D(1.0d, 1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_278() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=-1.0d, other=new Object()
        Object actual = (new Vector3D(-1.0d, -1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_279() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.NaN, other=new Object()
        Object actual = (new Vector3D(Double.NaN, Double.NaN)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_280() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.POSITIVE_INFINITY, other=new Object()
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_281() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=1.0d, other="sample_str"
        Object actual = (new Vector3D(0.0d, 1.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_282() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=0.0d, other="sample_str"
        Object actual = (new Vector3D(1.0d, 0.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_283() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.NaN, other="sample_str"
        Object actual = (new Vector3D(-1.0d, Double.NaN)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_284() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=-1.0d, other="sample_str"
        Object actual = (new Vector3D(Double.NaN, -1.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_285() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=0.0d, other="sample_str"
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 0.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_286() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=-1.0d, other=Integer.valueOf(1)
        Object actual = (new Vector3D(0.0d, -1.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_287() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.NaN, other=Integer.valueOf(1)
        Object actual = (new Vector3D(1.0d, Double.NaN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_288() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=0.0d, other=Integer.valueOf(1)
        Object actual = (new Vector3D(-1.0d, 0.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_289() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=1.0d, other=Integer.valueOf(1)
        Object actual = (new Vector3D(Double.NaN, 1.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_290() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=1.0d, other=Integer.valueOf(1)
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 1.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_291() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=0.0d, other=new Object()
        Object actual = (new Vector3D(Double.NaN, 0.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_292() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=1.0d, other=new Object()
        Object actual = (new Vector3D(-1.0d, 1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_293() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=-1.0d, other=new Object()
        Object actual = (new Vector3D(1.0d, -1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_294() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=-1.0d, other=new Object()
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, -1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_295() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.NaN, other=new Object()
        Object actual = (new Vector3D(0.0d, Double.NaN)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_296() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.NaN, other=new Object()
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.NaN)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_297() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.POSITIVE_INFINITY, other="sample_str"
        Object actual = (new Vector3D(0.0d, Double.POSITIVE_INFINITY)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_298() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.POSITIVE_INFINITY, other=Integer.valueOf(1)
        Object actual = (new Vector3D(1.0d, Double.POSITIVE_INFINITY)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_299() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.POSITIVE_INFINITY, other=new Object()
        Object actual = (new Vector3D(-1.0d, Double.POSITIVE_INFINITY)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_300() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.POSITIVE_INFINITY, other=new Object()
        Object actual = (new Vector3D(Double.NaN, Double.POSITIVE_INFINITY)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_301() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(0.0d, 0.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("326107136", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_302() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(0.0d, 1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("107072074", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_303() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(0.0d, -1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2040411574", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_304() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(0.0d, Double.NaN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_305() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(0.0d, Double.POSITIVE_INFINITY)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_306() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(1.0d, 0.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1760047690", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_307() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(1.0d, 1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1897134674", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_308() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(1.0d, -1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("250348974", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_309() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(1.0d, Double.NaN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_310() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(1.0d, Double.POSITIVE_INFINITY)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_311() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(-1.0d, 0.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("387435958", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_312() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(-1.0d, 1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("250348974", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_313() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(-1.0d, -1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1897134674", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_314() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(-1.0d, Double.NaN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_315() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(-1.0d, Double.POSITIVE_INFINITY)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_316() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.NaN, 0.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_317() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.NaN, 1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_318() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.NaN, -1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_319() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.NaN, Double.NaN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_320() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.NaN, Double.POSITIVE_INFINITY)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_321() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 0.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_322() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_323() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, -1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_324() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.NaN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_325() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_326() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(0.0d, 0.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{1; 0; 0}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_327() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(0.0d, 1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{0.54; 0; 0.84}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_328() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(0.0d, -1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{0.54; 0; -0.84}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_329() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(0.0d, Double.NaN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{(NaN); (NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_330() throws Exception {
        // Combination: receiver__alpha=0.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(0.0d, Double.POSITIVE_INFINITY)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{(NaN); (NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_331() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(1.0d, 0.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{0.54; 0.84; 0}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_332() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(1.0d, 1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{0.29; 0.45; 0.84}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_333() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(1.0d, -1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{0.29; 0.45; -0.84}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_334() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(1.0d, Double.NaN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{(NaN); (NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_335() throws Exception {
        // Combination: receiver__alpha=1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(1.0d, Double.POSITIVE_INFINITY)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{(NaN); (NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_336() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=0.0d
        Object actual = (new Vector3D(-1.0d, 0.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{0.54; -0.84; 0}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_337() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=1.0d
        Object actual = (new Vector3D(-1.0d, 1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{0.29; -0.45; 0.84}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_338() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=-1.0d
        Object actual = (new Vector3D(-1.0d, -1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{0.29; -0.45; -0.84}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_339() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.NaN
        Object actual = (new Vector3D(-1.0d, Double.NaN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{(NaN); (NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_340() throws Exception {
        // Combination: receiver__alpha=-1.0d, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(-1.0d, Double.POSITIVE_INFINITY)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{(NaN); (NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_341() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.NaN, 0.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{(NaN); (NaN); 0}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_342() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.NaN, 1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{(NaN); (NaN); 0.84}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_343() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.NaN, -1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{(NaN); (NaN); -0.84}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_344() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.NaN, Double.NaN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{(NaN); (NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_345() throws Exception {
        // Combination: receiver__alpha=Double.NaN, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.NaN, Double.POSITIVE_INFINITY)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{(NaN); (NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_346() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=0.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 0.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{(NaN); (NaN); 0}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_347() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, 1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{(NaN); (NaN); 0.84}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_348() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=-1.0d
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, -1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{(NaN); (NaN); -0.84}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_349() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.NaN
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.NaN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{(NaN); (NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_350() throws Exception {
        // Combination: receiver__alpha=Double.POSITIVE_INFINITY, receiver__delta=Double.POSITIVE_INFINITY
        Object actual = (new Vector3D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("{(NaN); (NaN); (NaN)}", formatValue(actual));
    }

}
