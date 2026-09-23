package org.apache.commons.math.complex;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Complex.
 */
public class Complex_IPOTest {
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
    public void test_abs_pairwise_001() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_002() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_003() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_004() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_005() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_006() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_007() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.4142135623730951", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_008() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.4142135623730951", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_009() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_010() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_011() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_012() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.4142135623730951", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_013() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.4142135623730951", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_014() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_015() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_016() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_017() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_018() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_019() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_020() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_021() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_022() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_023() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_024() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_025() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_026() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, 0.0d)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_027() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, 0.0d)).add(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_028() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, 0.0d)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@40000000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_029() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, 0.0d)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_030() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@bdb00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_031() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(0.0d, 1.0d)).add(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@18b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_032() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(1.0d, 1.0d)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@93a80000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_033() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(-1.0d, 1.0d)).add(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_034() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(Double.NaN, 1.0d)).add(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_035() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).add(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@96600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_036() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, -1.0d)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_037() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, -1.0d)).add(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_038() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, -1.0d)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@18b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_039() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, -1.0d)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_040() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@96600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_041() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, Double.NaN)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_042() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, Double.NaN)).add(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_043() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, Double.NaN)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_044() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, Double.NaN)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_045() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_046() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@96600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_047() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).add(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@96600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_048() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@58b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_049() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_050() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_051() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@80000000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_052() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@bdb00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_053() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_054() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_055() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@fdb00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_056() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@98b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_057() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_058() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_059() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_060() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@16600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_061() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@18b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_062() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_063() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_064() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_065() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@96600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_066() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_067() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_068() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_069() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_070() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_071() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d8b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_072() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@16600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_073() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@96600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_074() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_075() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_076() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, 0.0d)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_077() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, 0.0d)).divide(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_078() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, 0.0d)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@4d007ce", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_079() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, 0.0d)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_080() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_081() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(0.0d, 1.0d)).divide(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_082() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(1.0d, 1.0d)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@fca0075f", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_083() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(-1.0d, 1.0d)).divide(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_084() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(Double.NaN, 1.0d)).divide(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_085() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).divide(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_086() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, -1.0d)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@a9d007ce", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_087() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, -1.0d)).divide(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_088() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, -1.0d)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@fca0075f", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_089() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, -1.0d)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_090() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_091() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, Double.NaN)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_092() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, Double.NaN)).divide(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_093() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, Double.NaN)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_094() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, Double.NaN)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_095() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_096() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_097() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).divide(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_098() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_099() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_100() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).divide(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_101() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d, other=new Object()
        Object actual = (new Complex(0.0d, 0.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_102() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d, other=new Object()
        Object actual = (new Complex(1.0d, 1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_103() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d, other=new Object()
        Object actual = (new Complex(-1.0d, -1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_104() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN, other=new Object()
        Object actual = (new Complex(Double.NaN, Double.NaN)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_105() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY, other=new Object()
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_106() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d, other="sample_str"
        Object actual = (new Complex(1.0d, 0.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_107() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d, other="sample_str"
        Object actual = (new Complex(0.0d, 1.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_108() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d, other="sample_str"
        Object actual = (new Complex(Double.NaN, -1.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_109() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN, other="sample_str"
        Object actual = (new Complex(-1.0d, Double.NaN)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_110() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY, other="sample_str"
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_111() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d, other=Integer.valueOf(1)
        Object actual = (new Complex(-1.0d, 0.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_112() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d, other=Integer.valueOf(1)
        Object actual = (new Complex(Double.NaN, 1.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_113() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d, other=Integer.valueOf(1)
        Object actual = (new Complex(0.0d, -1.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_114() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN, other=Integer.valueOf(1)
        Object actual = (new Complex(1.0d, Double.NaN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_115() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, other=Integer.valueOf(1)
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_116() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN, other=new Object()
        Object actual = (new Complex(0.0d, Double.NaN)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_117() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d, other=new Object()
        Object actual = (new Complex(1.0d, -1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_118() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d, other=new Object()
        Object actual = (new Complex(-1.0d, 1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_119() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, other=new Object()
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_120() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d, other=new Object()
        Object actual = (new Complex(Double.NaN, 0.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_121() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY, other=new Object()
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_122() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d, other="sample_str"
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_123() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d, other=Integer.valueOf(1)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_124() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d, other=new Object()
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_125() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN, other=new Object()
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_126() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_127() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1034944512", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_128() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1112539136", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_129() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_130() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2108686336", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_131() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("414187520", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_132() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1449132032", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_133() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-698351616", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_134() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_135() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1772093440", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_136() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1733296128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_137() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-698351616", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_138() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1449132032", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_139() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_140() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("375390208", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_141() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_142() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_143() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_144() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_145() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_146() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1487929344", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_147() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1772093440", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_148() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("375390208", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_149() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_150() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-698351616", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_151() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_152() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_153() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_154() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_155() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_156() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_157() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_158() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_159() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_160() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_161() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_162() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_163() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_164() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_165() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_166() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_167() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_168() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_169() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_170() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_171() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_172() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_173() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_174() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_175() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_176() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_177() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_178() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_179() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_180() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_181() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_182() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_183() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_184() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_185() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_186() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_187() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_188() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_189() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_190() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_191() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_192() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_193() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_194() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_195() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_196() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_197() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_198() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_199() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_200() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_201() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_202() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_203() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_204() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_205() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_206() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_207() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_208() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_209() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_210() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_211() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_212() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_213() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_214() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_215() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_216() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_217() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_218() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_219() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_220() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_221() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_222() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_223() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_224() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_225() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_226() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_227() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_228() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_229() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_230() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_231() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_232() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_233() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_234() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_235() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_236() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_237() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_238() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_239() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_240() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_241() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_242() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_243() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_244() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_245() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_246() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_247() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_248() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_249() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_250() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_251() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, 0.0d)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_252() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, 0.0d)).multiply(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_253() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, 0.0d)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_254() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, 0.0d)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_255() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_256() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(0.0d, 1.0d)).multiply(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_257() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(1.0d, 1.0d)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@11580000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_258() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(-1.0d, 1.0d)).multiply(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@80000000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_259() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(Double.NaN, 1.0d)).multiply(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_260() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).multiply(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_261() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, -1.0d)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d8b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_262() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, -1.0d)).multiply(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_263() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, -1.0d)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@11580000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_264() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, -1.0d)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_265() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_266() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, Double.NaN)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_267() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, Double.NaN)).multiply(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_268() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, Double.NaN)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_269() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, Double.NaN)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_270() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_271() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_272() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).multiply(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_273() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_274() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_275() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).multiply(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_276() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_277() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_278() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@bdb00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_279() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_280() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_281() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@18b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_282() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_283() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_284() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_285() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@96600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_286() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@98b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_287() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_288() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_289() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_290() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@16600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_291() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_292() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_293() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_294() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_295() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_296() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@58b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_297() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@96600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_298() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@16600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_299() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_300() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_301() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, 0.0d)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_302() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, 0.0d)).subtract(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_303() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, 0.0d)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@80000000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_304() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, 0.0d)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_305() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_306() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(0.0d, 1.0d)).subtract(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@18b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_307() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(1.0d, 1.0d)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@98b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_308() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(-1.0d, 1.0d)).subtract(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_309() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(Double.NaN, 1.0d)).subtract(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_310() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).subtract(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@96600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_311() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, -1.0d)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@91580000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_312() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, -1.0d)).subtract(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_313() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, -1.0d)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@93a80000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_314() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, -1.0d)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_315() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@51580000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_316() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, Double.NaN)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_317() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, Double.NaN)).subtract(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_318() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, Double.NaN)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_319() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, Double.NaN)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_320() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_321() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@16600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_322() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).subtract(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@96600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_323() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@18b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_324() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_325() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).subtract(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_326() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@1252dccf", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_327() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@80000000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_328() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@6f22dccf", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_329() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_330() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_331() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7892641f", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_332() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@b9de33ed", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_333() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7dae3058", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_334() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_335() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_336() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@f8926b7e", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_337() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@39de3662", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_338() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@fdae32cd", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_339() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_340() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_341() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_342() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_343() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_344() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_345() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_346() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_347() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_348() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_349() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_350() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_351() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@80000000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_352() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@1252dccf", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_353() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@1252dccf", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_354() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_355() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_356() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@663f8eaf", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_357() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@17eb05e1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_358() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@97eb05e1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_359() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_360() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_361() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@e63f8750", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_362() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@97eb036c", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_363() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@17eb036c", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_364() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_365() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_366() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_367() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_368() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_369() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_370() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_371() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_372() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_373() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_374() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_375() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_376() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_377() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@9002dccf", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_378() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@1002dccf", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_379() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_380() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_381() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_382() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@1a79f965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_383() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@9a79f965", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_384() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_385() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_386() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_387() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@9a79f206", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_388() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@1a79f206", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_389() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_390() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_391() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_392() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_393() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_394() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_395() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_396() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_397() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_398() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_399() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_400() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_401() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@bdb00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_402() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7c2f13b4", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_403() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@fc2f13b4", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_404() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_405() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_406() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@6acefe59", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_407() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7034abe0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_408() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@f034abe0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_409() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_410() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_411() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@eacefe59", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_412() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@f034abe0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_413() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7034abe0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_414() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_415() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_416() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_417() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_418() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_419() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_420() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_421() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_422() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_423() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_424() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_425() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_426() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_427() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@eacefe59", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_428() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@6acefe59", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_429() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_430() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_431() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@fc2f13b4", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_432() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@f034abe0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_433() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7034abe0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_434() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_435() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_436() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7c2f13b4", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_437() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7034abe0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_438() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@f034abe0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_439() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_440() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_441() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_442() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_443() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_444() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_445() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_446() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_447() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_448() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_449() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_450() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_451() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_452() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@5996964f", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_453() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@715583e6", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_454() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_455() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_456() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@9c18b1b6", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_457() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@2fbbf133", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_458() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@f7fc7b42", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_459() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_460() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_461() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@1c18b1b6", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_462() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@afbbf133", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_463() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@77fc7b42", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_464() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_465() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_466() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_467() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_468() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_469() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_470() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_471() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_472() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_473() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_474() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_475() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_476() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@fdb00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_477() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_478() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@e150a9bf", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_479() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_480() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_481() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@b780a9bf", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_482() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7b901679", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_483() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@31277040", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_484() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_485() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_486() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3780a9bf", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_487() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@fb901679", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_488() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@b1277040", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_489() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_490() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@fdb00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_491() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_492() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_493() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_494() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_495() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_496() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3530a9bf", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_497() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3530a9bf", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_498() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3530a9bf", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_499() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_500() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@de0a9bf", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_501() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, 0.0d)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_502() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d, x=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, 0.0d)).pow(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_503() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, 0.0d)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@afa780f8", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_504() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, 0.0d)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_505() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_506() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d, x=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(0.0d, 1.0d)).pow(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_507() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(1.0d, 1.0d)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@66cc9d7e", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_508() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d, x=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(-1.0d, 1.0d)).pow(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_509() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d, x=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(Double.NaN, 1.0d)).pow(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_510() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d, x=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).pow(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_511() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, -1.0d)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@ee275d0f", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_512() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d, x=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, -1.0d)).pow(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_513() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, -1.0d)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@dc72026c", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_514() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, -1.0d)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_515() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_516() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, Double.NaN)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_517() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN, x=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, Double.NaN)).pow(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_518() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, Double.NaN)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_519() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, Double.NaN)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_520() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_521() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_522() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, x=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).pow(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_523() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_524() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_525() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY, x=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).pow(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_526() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_527() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@90ef9fe2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_528() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@10ef9fe2", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_529() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_530() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_531() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@86094b41", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_532() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@911c9f75", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_533() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@111c9f75", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_534() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_535() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_536() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@6094b41", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_537() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@111c9f75", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_538() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@911c9f75", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_539() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_540() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_541() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_542() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_543() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_544() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_545() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_546() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_547() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_548() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_549() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_550() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_551() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_552() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@623cc831", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_553() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@e23cc831", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_554() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_555() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_556() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@9fe99e02", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_557() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@cf69b9a5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_558() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@4f69b9a5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_559() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_560() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_561() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@1fe99e02", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_562() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@4f69b9a5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_563() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@cf69b9a5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_564() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_565() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_566() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_567() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_568() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_569() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_570() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_571() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_572() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_573() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_574() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_575() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_576() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_577() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_578() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@18b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_579() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_580() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_581() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@199e1379", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_582() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@9f033e9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_583() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@255661d9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_584() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_585() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_586() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@999e1379", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_587() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@89f033e9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_588() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@a55661d9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_589() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_590() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@fdb00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_591() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_592() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_593() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_594() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_595() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_596() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_597() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_598() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_599() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_600() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_601() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_602() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_603() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_604() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_605() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_606() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@f0e372ff", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_607() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@113e23c4", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_608() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@913e23c4", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_609() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_610() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_611() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@f0e372ff", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_612() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@913e23c4", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_613() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@113e23c4", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_614() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_615() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_616() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_617() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_618() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_619() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_620() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_621() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_622() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_623() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_624() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_625() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_626() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_627() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@591f3aa5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_628() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d91f3aa5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_629() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_630() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_631() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7ed621bb", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_632() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@59d2d691", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_633() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d9d2d691", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_634() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_635() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_636() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@fed621bb", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_637() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d9d2d691", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_638() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@59d2d691", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_639() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_640() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_641() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_642() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_643() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_644() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_645() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_646() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_647() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_648() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_649() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_650() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_651() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_652() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@1685110b", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_653() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@9685110b", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_654() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_655() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_656() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@eb12e4f5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_657() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@1b69b601", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_658() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@9b69b601", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_659() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_660() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_661() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@6b12e4f5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_662() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@9b69b601", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_663() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@1b69b601", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_664() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_665() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_666() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_667() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_668() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_669() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_670() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_671() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_672() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_673() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_674() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_675() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_676() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d, real=0.0d, imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).createComplex(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_677() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d, real=1.0d, imaginary=0.0d
        Object actual = (new Complex(1.0d, 1.0d)).createComplex(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@3db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_678() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d, real=-1.0d, imaginary=0.0d
        Object actual = (new Complex(-1.0d, -1.0d)).createComplex(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@bdb00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_679() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN, real=Double.NaN, imaginary=0.0d
        Object actual = (new Complex(Double.NaN, Double.NaN)).createComplex(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_680() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY, real=Double.POSITIVE_INFINITY, imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).createComplex(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_681() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d, real=0.0d, imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).createComplex(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@18b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_682() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d, real=1.0d, imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).createComplex(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_683() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN, real=-1.0d, imaginary=1.0d
        Object actual = (new Complex(0.0d, Double.NaN)).createComplex(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_684() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d, real=Double.NaN, imaginary=1.0d
        Object actual = (new Complex(1.0d, -1.0d)).createComplex(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_685() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d, real=Double.POSITIVE_INFINITY, imaginary=1.0d
        Object actual = (new Complex(1.0d, 0.0d)).createComplex(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@96600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_686() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d, real=0.0d, imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).createComplex(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@98b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_687() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN, real=1.0d, imaginary=-1.0d
        Object actual = (new Complex(-1.0d, Double.NaN)).createComplex(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_688() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d, real=-1.0d, imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).createComplex(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_689() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d, real=Double.NaN, imaginary=-1.0d
        Object actual = (new Complex(0.0d, 1.0d)).createComplex(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_690() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d, real=Double.POSITIVE_INFINITY, imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).createComplex(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@16600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_691() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN, real=0.0d, imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).createComplex(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_692() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d, real=1.0d, imaginary=Double.NaN
        Object actual = (new Complex(0.0d, -1.0d)).createComplex(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_693() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d, real=-1.0d, imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, 1.0d)).createComplex(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_694() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d, real=Double.NaN, imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, 0.0d)).createComplex(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_695() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d, real=Double.POSITIVE_INFINITY, imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).createComplex(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_696() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY, real=0.0d, imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).createComplex(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@58b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_697() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d, real=1.0d, imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).createComplex(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@96600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_698() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d, real=-1.0d, imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, 1.0d)).createComplex(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@16600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_699() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d, real=Double.NaN, imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, -1.0d)).createComplex(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_700() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN, real=Double.POSITIVE_INFINITY, imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.NaN)).createComplex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@d6600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_701() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, real=1.0d, imaginary=1.0d
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).createComplex(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_702() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, real=-1.0d, imaginary=-1.0d
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).createComplex(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@56600000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_703() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, real=Double.NaN, imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).createComplex(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_704() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d, real=Double.POSITIVE_INFINITY, imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).createComplex(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7db00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_705() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY, real=0.0d, imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).createComplex(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@58b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_706() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d, real=0.0d, imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).createComplex(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@18b00000", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_707() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN, real=Double.NaN, imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).createComplex(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("org.apache.commons.math.complex.Complex@7", formatValue(actual));
    }

}
