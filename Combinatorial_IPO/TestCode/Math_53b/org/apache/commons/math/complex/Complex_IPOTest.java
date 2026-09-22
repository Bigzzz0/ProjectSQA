package org.apache.commons.math.complex;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Complex.
 */
public class Complex_IPOTest {
    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_001() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_002() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_003() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_004() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_005() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_006() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_007() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_008() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_009() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_010() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_011() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_012() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_013() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_014() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_015() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_016() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_017() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_018() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_019() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_020() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_021() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_022() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_023() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_024() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_025() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_026() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_027() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_028() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_029() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_030() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_031() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_032() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_033() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_034() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_035() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_036() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_037() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_038() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_039() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_040() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_041() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_042() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_043() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_044() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_045() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_046() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_047() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_048() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_049() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_050() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_051() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_052() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_053() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_054() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_055() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_056() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_057() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_058() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_059() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_060() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_061() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_062() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_063() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_064() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_065() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_066() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_067() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_068() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_069() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_070() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_071() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_072() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_073() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_074() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_075() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

}
