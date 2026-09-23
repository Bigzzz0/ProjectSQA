package org.apache.commons.math.complex;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Complex.
 */
public class Complex_IPOTest {
    @Test(timeout = 4000)
    public void test_abs_pairwise_001() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_002() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_003() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_004() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_005() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_006() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_007() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.4142135623730951", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_008() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.4142135623730951", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_009() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_010() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_011() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_012() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.4142135623730951", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_013() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.4142135623730951", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_014() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_015() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_016() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_017() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_018() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_019() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_020() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_021() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_022() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_023() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_024() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abs_pairwise_025() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).abs();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_026() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_027() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_028() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_029() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_030() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_031() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_032() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_033() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_034() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_035() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_036() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_037() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_038() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_039() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_040() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_041() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_042() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_043() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_044() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_045() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_046() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_047() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_048() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_049() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_conjugate_pairwise_050() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).conjugate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_051() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d, other=new Object()
        Object actual = (new Complex(0.0d, 0.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_052() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d, other=new Object()
        Object actual = (new Complex(1.0d, 1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_053() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d, other=new Object()
        Object actual = (new Complex(-1.0d, -1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_054() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN, other=new Object()
        Object actual = (new Complex(Double.NaN, Double.NaN)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_055() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY, other=new Object()
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_056() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d, other="sample_str"
        Object actual = (new Complex(1.0d, 0.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_057() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d, other="sample_str"
        Object actual = (new Complex(0.0d, 1.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_058() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d, other="sample_str"
        Object actual = (new Complex(Double.NaN, -1.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_059() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN, other="sample_str"
        Object actual = (new Complex(-1.0d, Double.NaN)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_060() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY, other="sample_str"
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_061() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d, other=Integer.valueOf(1)
        Object actual = (new Complex(-1.0d, 0.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_062() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d, other=Integer.valueOf(1)
        Object actual = (new Complex(Double.NaN, 1.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_063() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d, other=Integer.valueOf(1)
        Object actual = (new Complex(0.0d, -1.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_064() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN, other=Integer.valueOf(1)
        Object actual = (new Complex(1.0d, Double.NaN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_065() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, other=Integer.valueOf(1)
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_066() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN, other=new Object()
        Object actual = (new Complex(0.0d, Double.NaN)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_067() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d, other=new Object()
        Object actual = (new Complex(1.0d, -1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_068() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d, other=new Object()
        Object actual = (new Complex(-1.0d, 1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_069() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, other=new Object()
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_070() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d, other=new Object()
        Object actual = (new Complex(Double.NaN, 0.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_071() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY, other=new Object()
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_072() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d, other="sample_str"
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_073() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d, other=Integer.valueOf(1)
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_074() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d, other=new Object()
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_075() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN, other=new Object()
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_076() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_077() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1034944512", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_078() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1112539136", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_079() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_080() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2108686336", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_081() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("414187520", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_082() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1449132032", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_083() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-698351616", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_084() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_085() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1772093440", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_086() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1733296128", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_087() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-698351616", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_088() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1449132032", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_089() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_090() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("375390208", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_091() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_092() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_093() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_094() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_095() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_096() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1487929344", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_097() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1772093440", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_098() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("375390208", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_099() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("7", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_100() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-698351616", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_101() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_102() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_103() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_104() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_105() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_106() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_107() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_108() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_109() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_110() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_111() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_112() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_113() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_114() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_115() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_116() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_117() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_118() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_119() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_120() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_121() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_122() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_123() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_124() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getImaginary_pairwise_125() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getImaginary();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_126() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_127() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_128() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_129() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_130() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_131() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_132() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_133() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_134() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_135() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_136() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_137() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_138() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_139() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_140() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_141() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_142() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_143() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_144() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_145() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_146() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_147() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_148() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_149() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getReal_pairwise_150() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getReal();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_151() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_152() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_153() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_154() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_155() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_156() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_157() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_158() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_159() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_160() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_161() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_162() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_163() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_164() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_165() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_166() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_167() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_168() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_169() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_170() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_171() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_172() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_173() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_174() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isNaN_pairwise_175() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).isNaN();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_176() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_177() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_178() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_179() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_180() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_181() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_182() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_183() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_184() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_185() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_186() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_187() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_188() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_189() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_190() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_191() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_192() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_193() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_194() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_195() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_196() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_197() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_198() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_199() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isInfinite_pairwise_200() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).isInfinite();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_201() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d, rhs=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).multiply(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_202() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d, rhs=1.0d
        Object actual = (new Complex(1.0d, 0.0d)).multiply(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_203() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d, rhs=-1.0d
        Object actual = (new Complex(-1.0d, 0.0d)).multiply(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_204() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d, rhs=Double.NaN
        Object actual = (new Complex(Double.NaN, 0.0d)).multiply(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_205() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d, rhs=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).multiply(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_206() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d, rhs=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).multiply(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_207() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d, rhs=0.0d
        Object actual = (new Complex(1.0d, 1.0d)).multiply(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_208() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d, rhs=Double.NaN
        Object actual = (new Complex(-1.0d, 1.0d)).multiply(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_209() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d, rhs=-1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).multiply(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_210() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d, rhs=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).multiply(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_211() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d, rhs=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).multiply(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_212() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d, rhs=Double.NaN
        Object actual = (new Complex(1.0d, -1.0d)).multiply(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_213() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d, rhs=0.0d
        Object actual = (new Complex(-1.0d, -1.0d)).multiply(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_214() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d, rhs=1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).multiply(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_215() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d, rhs=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).multiply(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_216() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN, rhs=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).multiply(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_217() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN, rhs=-1.0d
        Object actual = (new Complex(1.0d, Double.NaN)).multiply(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_218() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN, rhs=1.0d
        Object actual = (new Complex(-1.0d, Double.NaN)).multiply(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_219() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN, rhs=0.0d
        Object actual = (new Complex(Double.NaN, Double.NaN)).multiply(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_220() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN, rhs=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).multiply(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_221() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).multiply(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_222() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=0.0d
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).multiply(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_223() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=1.0d
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).multiply(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_224() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=-1.0d
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).multiply(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_225() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY, rhs=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).multiply(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_226() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d, rhs=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, 1.0d)).multiply(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_227() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d, rhs=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, -1.0d)).multiply(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_228() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN, rhs=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.NaN)).multiply(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_229() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_230() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_231() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_232() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_233() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-Infinity, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_234() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_235() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_236() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_237() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_238() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-Infinity, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_239() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_240() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_241() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_242() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_243() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-Infinity, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_244() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_245() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_246() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_247() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_248() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_249() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.0, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_250() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_251() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_252() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_negate_pairwise_253() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).negate();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-Infinity, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_254() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.5707963267948966, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_255() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_256() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(3.141592653589793, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_257() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_258() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_259() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.5707963267948966, -0.8813735870195429)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_260() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.9045568943023813, -1.0612750619050357)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_261() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(2.2370357592874117, -1.0612750619050357)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_262() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_263() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_264() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.5707963267948966, 0.8813735870195428)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_265() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.9045568943023814, 1.0612750619050355)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_266() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(2.2370357592874117, 1.0612750619050355)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_267() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_268() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_269() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_270() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_271() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_272() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_273() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_274() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_275() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_276() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_277() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_acos_pairwise_278() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).acos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_279() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_280() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.5707963267948966, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_281() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.5707963267948966, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_282() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_283() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_284() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.8813735870195428)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_285() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.6662394324925153, 1.0612750619050355)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_286() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.6662394324925153, 1.0612750619050355)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_287() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_288() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_289() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, -0.8813735870195429)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_290() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.6662394324925153, -1.0612750619050357)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_291() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.6662394324925153, -1.0612750619050357)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_292() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_293() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_294() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_295() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_296() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_297() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_298() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_299() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_300() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_301() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_302() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_asin_pairwise_303() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).asin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_304() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_305() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.7853981633974483, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_306() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.7853981633974483, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_307() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_308() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_309() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_310() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0172219678978514, 0.4023594781085251)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_311() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0172219678978514, 0.4023594781085251)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_312() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_313() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_314() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_315() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0172219678978514, -0.40235947810852507)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_316() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0172219678978514, -0.40235947810852507)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_317() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_318() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_319() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_320() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_321() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_322() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_323() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_324() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_325() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_326() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_327() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_atan_pairwise_328() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).atan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_329() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_330() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.5403023058681398, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_331() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.5403023058681398, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_332() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_333() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_334() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.5430806348152437, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_335() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.8337300251311491, -0.9888977057628651)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_336() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.8337300251311491, 0.9888977057628651)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_337() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_338() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_339() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.5430806348152437, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_340() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.8337300251311491, 0.9888977057628651)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_341() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.8337300251311491, -0.9888977057628651)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_342() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_343() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_344() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_345() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_346() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_347() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_348() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_349() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_350() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_351() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_352() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cos_pairwise_353() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).cos();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_354() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_355() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.5430806348152437, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_356() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.5430806348152437, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_357() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_358() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_359() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.5403023058681398, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_360() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.8337300251311491, 0.9888977057628651)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_361() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.8337300251311491, -0.9888977057628651)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_362() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_363() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_364() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.5403023058681398, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_365() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.8337300251311491, -0.9888977057628651)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_366() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.8337300251311491, 0.9888977057628651)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_367() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_368() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_369() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_370() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_371() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_372() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_373() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_374() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_375() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_376() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_377() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cosh_pairwise_378() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).cosh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_379() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_380() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(2.718281828459045, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_381() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.36787944117144233, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_382() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_383() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_384() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.5403023058681398, 0.8414709848078965)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_385() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.4686939399158851, 2.2873552871788423)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_386() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.19876611034641298, 0.3095598756531122)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_387() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_388() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_389() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.5403023058681398, -0.8414709848078965)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_390() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.4686939399158851, -2.2873552871788423)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_391() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.19876611034641298, -0.3095598756531122)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_392() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_393() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_394() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_395() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_396() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_397() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_398() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_399() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_400() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_401() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_402() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_exp_pairwise_403() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).exp();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_404() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_405() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_406() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 3.141592653589793)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_407() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_408() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_409() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 1.5707963267948966)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_410() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.3465735902799727, 0.7853981633974483)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_411() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.3465735902799727, 2.356194490192345)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_412() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_413() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_414() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, -1.5707963267948966)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_415() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.3465735902799727, -0.7853981633974483)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_416() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.3465735902799727, -2.356194490192345)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_417() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_418() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_419() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_420() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_421() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_422() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_423() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_424() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 1.5707963267948966)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_425() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 1.5707963267948966)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_426() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 1.5707963267948966)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_427() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_428() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).log();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.7853981633974483)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_429() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_430() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.8414709848078965, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_431() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.8414709848078965, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_432() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_433() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_434() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 1.1752011936438014)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_435() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.2984575814159773, 0.6349639147847361)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_436() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.2984575814159773, 0.6349639147847361)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_437() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_438() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_439() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, -1.1752011936438014)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_440() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.2984575814159773, -0.6349639147847361)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_441() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.2984575814159773, -0.6349639147847361)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_442() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_443() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_444() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_445() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_446() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_447() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_448() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_449() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_450() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_451() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_452() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sin_pairwise_453() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).sin();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_454() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_455() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.1752011936438014, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_456() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.1752011936438014, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_457() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_458() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_459() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.8414709848078965)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_460() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.6349639147847361, 1.2984575814159773)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_461() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.6349639147847361, 1.2984575814159773)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_462() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_463() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_464() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, -0.8414709848078965)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_465() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.6349639147847361, -1.2984575814159773)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_466() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.6349639147847361, -1.2984575814159773)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_467() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_468() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_469() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_470() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_471() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_472() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_473() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_474() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_475() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_476() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_477() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sinh_pairwise_478() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).sinh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_479() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_480() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_481() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_482() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_483() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_484() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.7071067811865476, 0.7071067811865475)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_485() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.09868411346781, 0.45508986056222733)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_486() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.45508986056222733, 1.09868411346781)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_487() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_488() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_489() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.7071067811865476, -0.7071067811865475)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_490() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.09868411346781, -0.45508986056222733)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_491() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.45508986056222733, -1.09868411346781)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_492() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_493() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_494() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_495() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_496() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_497() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_498() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_499() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_500() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_501() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_502() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt_pairwise_503() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).sqrt();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_504() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_505() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_506() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_507() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_508() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_509() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.4142135623730951, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_510() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.272019649514069, -0.7861513777574233)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_511() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.272019649514069, 0.7861513777574233)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_512() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_513() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_514() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.4142135623730951, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_515() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.272019649514069, 0.7861513777574233)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_516() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.272019649514069, -0.7861513777574233)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_517() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_518() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_519() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_520() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_521() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_522() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_523() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_524() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_525() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_526() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_527() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_sqrt1z_pairwise_528() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).sqrt1z();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, -Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_529() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_530() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.557407724654902, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_531() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.557407724654902, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_532() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_533() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_534() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.761594155955765)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_535() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.27175258531951174, 1.0839233273386948)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_536() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.27175258531951174, 1.0839233273386948)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_537() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_538() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_539() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, -0.761594155955765)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_540() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.27175258531951174, -1.0839233273386948)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_541() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.27175258531951174, -1.0839233273386948)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_542() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_543() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_544() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_545() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_546() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_547() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_548() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_549() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_550() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_551() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.0, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_552() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tan_pairwise_553() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).tan();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_554() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_555() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.761594155955765, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_556() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.761594155955765, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_557() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_558() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_559() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 1.557407724654902)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_560() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0839233273386948, 0.27175258531951174)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_561() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0839233273386948, 0.27175258531951174)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_562() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_563() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_564() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, -1.557407724654902)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_565() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0839233273386948, -0.27175258531951174)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_566() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0839233273386948, -0.27175258531951174)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_567() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_568() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_569() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_570() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_571() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_572() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_573() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_574() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_575() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_576() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_577() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_tanh_pairwise_578() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).tanh();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_579() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_580() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_581() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("3.141592653589793", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_582() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_583() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_584() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5707963267948966", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_585() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.7853981633974483", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_586() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("2.356194490192345", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_587() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_588() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_589() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.5707963267948966", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_590() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.7853981633974483", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_591() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-2.356194490192345", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_592() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_593() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_594() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_595() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_596() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_597() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_598() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_599() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5707963267948966", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_600() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5707963267948966", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_601() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5707963267948966", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_602() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getArgument_pairwise_603() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getArgument();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.7853981633974483", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_604() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d, realPart=0.0d, imaginaryPart=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).createComplex(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_605() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d, realPart=1.0d, imaginaryPart=0.0d
        Object actual = (new Complex(1.0d, 1.0d)).createComplex(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_606() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d, realPart=-1.0d, imaginaryPart=0.0d
        Object actual = (new Complex(-1.0d, -1.0d)).createComplex(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_607() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN, realPart=Double.NaN, imaginaryPart=0.0d
        Object actual = (new Complex(Double.NaN, Double.NaN)).createComplex(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_608() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY, realPart=Double.POSITIVE_INFINITY, imaginaryPart=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).createComplex(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_609() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d, realPart=0.0d, imaginaryPart=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).createComplex(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_610() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d, realPart=1.0d, imaginaryPart=1.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).createComplex(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_611() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN, realPart=-1.0d, imaginaryPart=1.0d
        Object actual = (new Complex(0.0d, Double.NaN)).createComplex(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_612() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d, realPart=Double.NaN, imaginaryPart=1.0d
        Object actual = (new Complex(1.0d, -1.0d)).createComplex(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_613() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d, realPart=Double.POSITIVE_INFINITY, imaginaryPart=1.0d
        Object actual = (new Complex(1.0d, 0.0d)).createComplex(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_614() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d, realPart=0.0d, imaginaryPart=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).createComplex(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_615() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN, realPart=1.0d, imaginaryPart=-1.0d
        Object actual = (new Complex(-1.0d, Double.NaN)).createComplex(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_616() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d, realPart=-1.0d, imaginaryPart=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).createComplex(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_617() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d, realPart=Double.NaN, imaginaryPart=-1.0d
        Object actual = (new Complex(0.0d, 1.0d)).createComplex(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_618() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d, realPart=Double.POSITIVE_INFINITY, imaginaryPart=-1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).createComplex(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_619() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN, realPart=0.0d, imaginaryPart=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).createComplex(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_620() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d, realPart=1.0d, imaginaryPart=Double.NaN
        Object actual = (new Complex(0.0d, -1.0d)).createComplex(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_621() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d, realPart=-1.0d, imaginaryPart=Double.NaN
        Object actual = (new Complex(Double.NaN, 1.0d)).createComplex(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_622() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d, realPart=Double.NaN, imaginaryPart=Double.NaN
        Object actual = (new Complex(-1.0d, 0.0d)).createComplex(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_623() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d, realPart=Double.POSITIVE_INFINITY, imaginaryPart=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).createComplex(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_624() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY, realPart=0.0d, imaginaryPart=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).createComplex(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_625() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d, realPart=1.0d, imaginaryPart=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).createComplex(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_626() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d, realPart=-1.0d, imaginaryPart=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, 1.0d)).createComplex(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_627() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d, realPart=Double.NaN, imaginaryPart=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, -1.0d)).createComplex(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_628() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN, realPart=Double.POSITIVE_INFINITY, imaginaryPart=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.NaN)).createComplex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_629() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, realPart=1.0d, imaginaryPart=1.0d
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).createComplex(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_630() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, realPart=-1.0d, imaginaryPart=-1.0d
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).createComplex(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_631() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY, realPart=Double.NaN, imaginaryPart=Double.NaN
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).createComplex(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_632() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d, realPart=Double.POSITIVE_INFINITY, imaginaryPart=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).createComplex(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_633() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY, realPart=0.0d, imaginaryPart=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).createComplex(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_634() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d, realPart=0.0d, imaginaryPart=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).createComplex(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_635() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN, realPart=Double.NaN, imaginaryPart=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).createComplex(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_636() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(0.0d, 0.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_637() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(1.0d, 0.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_638() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=0.0d
        Object actual = (new Complex(-1.0d, 0.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(-1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_639() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.NaN, 0.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(NaN, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_640() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 0.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_641() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(0.0d, 1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(0.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_642() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(1.0d, 1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(1.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_643() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=1.0d
        Object actual = (new Complex(-1.0d, 1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(-1.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_644() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.NaN, 1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(NaN, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_645() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, 1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(Infinity, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_646() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(0.0d, -1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(0.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_647() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(1.0d, -1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(1.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_648() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=-1.0d
        Object actual = (new Complex(-1.0d, -1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(-1.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_649() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.NaN, -1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(NaN, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_650() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY, -1.0d)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(Infinity, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_651() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(0.0d, Double.NaN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(0.0, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_652() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(1.0d, Double.NaN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(1.0, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_653() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.NaN
        Object actual = (new Complex(-1.0d, Double.NaN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(-1.0, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_654() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.NaN, Double.NaN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_655() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.NaN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(Infinity, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_656() throws Exception {
        // Combination: receiver__real=0.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d, Double.POSITIVE_INFINITY)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(0.0, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_657() throws Exception {
        // Combination: receiver__real=1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d, Double.POSITIVE_INFINITY)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(1.0, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_658() throws Exception {
        // Combination: receiver__real=-1.0d, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d, Double.POSITIVE_INFINITY)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(-1.0, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_659() throws Exception {
        // Combination: receiver__real=Double.NaN, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN, Double.POSITIVE_INFINITY)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(NaN, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_660() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, receiver__imaginary=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

}
