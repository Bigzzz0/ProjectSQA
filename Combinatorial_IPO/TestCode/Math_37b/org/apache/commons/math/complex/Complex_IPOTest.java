package org.apache.commons.math.complex;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Complex.
 */
public class Complex_IPOTest {
    @Test(timeout = 4000)
    public void test_add_pairwise_001() throws Exception {
        // Combination: receiver__real=0.0d, addend=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(0.0d)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 2.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_002() throws Exception {
        // Combination: receiver__real=1.0d, addend=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(1.0d)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(2.0, 2.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_003() throws Exception {
        // Combination: receiver__real=-1.0d, addend=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(-1.0d)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 2.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_004() throws Exception {
        // Combination: receiver__real=Double.NaN, addend=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.NaN)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_005() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, addend=new org.apache.commons.math.complex.Complex(1.0, 2.0)
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).add(new org.apache.commons.math.complex.Complex(1.0, 2.0));
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 2.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_006() throws Exception {
        // Combination: receiver__real=0.0d, addend=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(0.0d)).add(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_007() throws Exception {
        // Combination: receiver__real=1.0d, addend=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(1.0d)).add(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_008() throws Exception {
        // Combination: receiver__real=-1.0d, addend=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(-1.0d)).add(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_009() throws Exception {
        // Combination: receiver__real=Double.NaN, addend=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(Double.NaN)).add(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_010() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, addend=org.apache.commons.math.complex.Complex.ZERO
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).add(org.apache.commons.math.complex.Complex.ZERO);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_011() throws Exception {
        // Combination: receiver__real=0.0d, addend=0.0d
        Object actual = (new Complex(0.0d)).add(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_012() throws Exception {
        // Combination: receiver__real=1.0d, addend=0.0d
        Object actual = (new Complex(1.0d)).add(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_013() throws Exception {
        // Combination: receiver__real=-1.0d, addend=0.0d
        Object actual = (new Complex(-1.0d)).add(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_014() throws Exception {
        // Combination: receiver__real=Double.NaN, addend=0.0d
        Object actual = (new Complex(Double.NaN)).add(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_015() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, addend=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).add(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_016() throws Exception {
        // Combination: receiver__real=0.0d, addend=1.0d
        Object actual = (new Complex(0.0d)).add(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_017() throws Exception {
        // Combination: receiver__real=1.0d, addend=1.0d
        Object actual = (new Complex(1.0d)).add(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(2.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_018() throws Exception {
        // Combination: receiver__real=-1.0d, addend=1.0d
        Object actual = (new Complex(-1.0d)).add(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_019() throws Exception {
        // Combination: receiver__real=Double.NaN, addend=1.0d
        Object actual = (new Complex(Double.NaN)).add(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_020() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, addend=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).add(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_021() throws Exception {
        // Combination: receiver__real=0.0d, addend=-1.0d
        Object actual = (new Complex(0.0d)).add(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_022() throws Exception {
        // Combination: receiver__real=1.0d, addend=-1.0d
        Object actual = (new Complex(1.0d)).add(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_023() throws Exception {
        // Combination: receiver__real=-1.0d, addend=-1.0d
        Object actual = (new Complex(-1.0d)).add(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-2.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_024() throws Exception {
        // Combination: receiver__real=Double.NaN, addend=-1.0d
        Object actual = (new Complex(Double.NaN)).add(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_025() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, addend=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).add(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_026() throws Exception {
        // Combination: receiver__real=0.0d, addend=Double.NaN
        Object actual = (new Complex(0.0d)).add(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_027() throws Exception {
        // Combination: receiver__real=1.0d, addend=Double.NaN
        Object actual = (new Complex(1.0d)).add(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_028() throws Exception {
        // Combination: receiver__real=-1.0d, addend=Double.NaN
        Object actual = (new Complex(-1.0d)).add(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_029() throws Exception {
        // Combination: receiver__real=Double.NaN, addend=Double.NaN
        Object actual = (new Complex(Double.NaN)).add(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_030() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, addend=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).add(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_031() throws Exception {
        // Combination: receiver__real=0.0d, addend=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d)).add(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_032() throws Exception {
        // Combination: receiver__real=1.0d, addend=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d)).add(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_033() throws Exception {
        // Combination: receiver__real=-1.0d, addend=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d)).add(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_034() throws Exception {
        // Combination: receiver__real=Double.NaN, addend=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN)).add(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_add_pairwise_035() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, addend=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).add(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_036() throws Exception {
        // Combination: receiver__real=0.0d, divisor=0.0d
        Object actual = (new Complex(0.0d)).divide(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_037() throws Exception {
        // Combination: receiver__real=1.0d, divisor=0.0d
        Object actual = (new Complex(1.0d)).divide(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_038() throws Exception {
        // Combination: receiver__real=-1.0d, divisor=0.0d
        Object actual = (new Complex(-1.0d)).divide(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_039() throws Exception {
        // Combination: receiver__real=Double.NaN, divisor=0.0d
        Object actual = (new Complex(Double.NaN)).divide(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_040() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, divisor=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).divide(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_041() throws Exception {
        // Combination: receiver__real=0.0d, divisor=1.0d
        Object actual = (new Complex(0.0d)).divide(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_042() throws Exception {
        // Combination: receiver__real=1.0d, divisor=1.0d
        Object actual = (new Complex(1.0d)).divide(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_043() throws Exception {
        // Combination: receiver__real=-1.0d, divisor=1.0d
        Object actual = (new Complex(-1.0d)).divide(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_044() throws Exception {
        // Combination: receiver__real=Double.NaN, divisor=1.0d
        Object actual = (new Complex(Double.NaN)).divide(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_045() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, divisor=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).divide(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_046() throws Exception {
        // Combination: receiver__real=0.0d, divisor=-1.0d
        Object actual = (new Complex(0.0d)).divide(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_047() throws Exception {
        // Combination: receiver__real=1.0d, divisor=-1.0d
        Object actual = (new Complex(1.0d)).divide(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_048() throws Exception {
        // Combination: receiver__real=-1.0d, divisor=-1.0d
        Object actual = (new Complex(-1.0d)).divide(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_049() throws Exception {
        // Combination: receiver__real=Double.NaN, divisor=-1.0d
        Object actual = (new Complex(Double.NaN)).divide(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_050() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, divisor=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).divide(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-Infinity, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_051() throws Exception {
        // Combination: receiver__real=0.0d, divisor=Double.NaN
        Object actual = (new Complex(0.0d)).divide(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_052() throws Exception {
        // Combination: receiver__real=1.0d, divisor=Double.NaN
        Object actual = (new Complex(1.0d)).divide(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_053() throws Exception {
        // Combination: receiver__real=-1.0d, divisor=Double.NaN
        Object actual = (new Complex(-1.0d)).divide(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_054() throws Exception {
        // Combination: receiver__real=Double.NaN, divisor=Double.NaN
        Object actual = (new Complex(Double.NaN)).divide(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_055() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, divisor=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).divide(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_056() throws Exception {
        // Combination: receiver__real=0.0d, divisor=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d)).divide(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_057() throws Exception {
        // Combination: receiver__real=1.0d, divisor=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d)).divide(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_058() throws Exception {
        // Combination: receiver__real=-1.0d, divisor=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d)).divide(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_059() throws Exception {
        // Combination: receiver__real=Double.NaN, divisor=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN)).divide(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_divide_pairwise_060() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, divisor=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).divide(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_061() throws Exception {
        // Combination: receiver__real=0.0d, other=new Object()
        Object actual = (new Complex(0.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_062() throws Exception {
        // Combination: receiver__real=1.0d, other=new Object()
        Object actual = (new Complex(1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_063() throws Exception {
        // Combination: receiver__real=-1.0d, other=new Object()
        Object actual = (new Complex(-1.0d)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_064() throws Exception {
        // Combination: receiver__real=Double.NaN, other=new Object()
        Object actual = (new Complex(Double.NaN)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_065() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, other=new Object()
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_066() throws Exception {
        // Combination: receiver__real=0.0d, other="sample_str"
        Object actual = (new Complex(0.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_067() throws Exception {
        // Combination: receiver__real=1.0d, other="sample_str"
        Object actual = (new Complex(1.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_068() throws Exception {
        // Combination: receiver__real=-1.0d, other="sample_str"
        Object actual = (new Complex(-1.0d)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_069() throws Exception {
        // Combination: receiver__real=Double.NaN, other="sample_str"
        Object actual = (new Complex(Double.NaN)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_070() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, other="sample_str"
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_071() throws Exception {
        // Combination: receiver__real=0.0d, other=Integer.valueOf(1)
        Object actual = (new Complex(0.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_072() throws Exception {
        // Combination: receiver__real=1.0d, other=Integer.valueOf(1)
        Object actual = (new Complex(1.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_073() throws Exception {
        // Combination: receiver__real=-1.0d, other=Integer.valueOf(1)
        Object actual = (new Complex(-1.0d)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_074() throws Exception {
        // Combination: receiver__real=Double.NaN, other=Integer.valueOf(1)
        Object actual = (new Complex(Double.NaN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_075() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, other=Integer.valueOf(1)
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_076() throws Exception {
        // Combination: receiver__real=0.0d, factor=0
        Object actual = (new Complex(0.0d)).multiply(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_077() throws Exception {
        // Combination: receiver__real=1.0d, factor=0
        Object actual = (new Complex(1.0d)).multiply(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_078() throws Exception {
        // Combination: receiver__real=-1.0d, factor=0
        Object actual = (new Complex(-1.0d)).multiply(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_079() throws Exception {
        // Combination: receiver__real=Double.NaN, factor=0
        Object actual = (new Complex(Double.NaN)).multiply(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_080() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, factor=0
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).multiply(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_081() throws Exception {
        // Combination: receiver__real=0.0d, factor=1
        Object actual = (new Complex(0.0d)).multiply(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_082() throws Exception {
        // Combination: receiver__real=1.0d, factor=1
        Object actual = (new Complex(1.0d)).multiply(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_083() throws Exception {
        // Combination: receiver__real=-1.0d, factor=1
        Object actual = (new Complex(-1.0d)).multiply(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_084() throws Exception {
        // Combination: receiver__real=Double.NaN, factor=1
        Object actual = (new Complex(Double.NaN)).multiply(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_085() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, factor=1
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).multiply(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_086() throws Exception {
        // Combination: receiver__real=0.0d, factor=-1
        Object actual = (new Complex(0.0d)).multiply(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_087() throws Exception {
        // Combination: receiver__real=1.0d, factor=-1
        Object actual = (new Complex(1.0d)).multiply(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_088() throws Exception {
        // Combination: receiver__real=-1.0d, factor=-1
        Object actual = (new Complex(-1.0d)).multiply(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_089() throws Exception {
        // Combination: receiver__real=Double.NaN, factor=-1
        Object actual = (new Complex(Double.NaN)).multiply(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_090() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, factor=-1
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).multiply(-1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_091() throws Exception {
        // Combination: receiver__real=0.0d, factor=Integer.MAX_VALUE
        Object actual = (new Complex(0.0d)).multiply(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_092() throws Exception {
        // Combination: receiver__real=1.0d, factor=Integer.MAX_VALUE
        Object actual = (new Complex(1.0d)).multiply(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(2.147483647E9, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_093() throws Exception {
        // Combination: receiver__real=-1.0d, factor=Integer.MAX_VALUE
        Object actual = (new Complex(-1.0d)).multiply(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-2.147483647E9, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_094() throws Exception {
        // Combination: receiver__real=Double.NaN, factor=Integer.MAX_VALUE
        Object actual = (new Complex(Double.NaN)).multiply(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_095() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, factor=Integer.MAX_VALUE
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).multiply(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_096() throws Exception {
        // Combination: receiver__real=0.0d, factor=Integer.MIN_VALUE
        Object actual = (new Complex(0.0d)).multiply(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_097() throws Exception {
        // Combination: receiver__real=1.0d, factor=Integer.MIN_VALUE
        Object actual = (new Complex(1.0d)).multiply(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-2.147483648E9, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_098() throws Exception {
        // Combination: receiver__real=-1.0d, factor=Integer.MIN_VALUE
        Object actual = (new Complex(-1.0d)).multiply(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(2.147483648E9, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_099() throws Exception {
        // Combination: receiver__real=Double.NaN, factor=Integer.MIN_VALUE
        Object actual = (new Complex(Double.NaN)).multiply(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_100() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, factor=Integer.MIN_VALUE
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).multiply(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_101() throws Exception {
        // Combination: receiver__real=0.0d, factor=0.0d
        Object actual = (new Complex(0.0d)).multiply(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_102() throws Exception {
        // Combination: receiver__real=1.0d, factor=0.0d
        Object actual = (new Complex(1.0d)).multiply(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_103() throws Exception {
        // Combination: receiver__real=-1.0d, factor=0.0d
        Object actual = (new Complex(-1.0d)).multiply(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_104() throws Exception {
        // Combination: receiver__real=Double.NaN, factor=0.0d
        Object actual = (new Complex(Double.NaN)).multiply(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_105() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, factor=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).multiply(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_106() throws Exception {
        // Combination: receiver__real=0.0d, factor=1.0d
        Object actual = (new Complex(0.0d)).multiply(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_107() throws Exception {
        // Combination: receiver__real=1.0d, factor=1.0d
        Object actual = (new Complex(1.0d)).multiply(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_108() throws Exception {
        // Combination: receiver__real=-1.0d, factor=1.0d
        Object actual = (new Complex(-1.0d)).multiply(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_109() throws Exception {
        // Combination: receiver__real=Double.NaN, factor=1.0d
        Object actual = (new Complex(Double.NaN)).multiply(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_110() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, factor=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).multiply(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_111() throws Exception {
        // Combination: receiver__real=0.0d, factor=-1.0d
        Object actual = (new Complex(0.0d)).multiply(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-0.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_112() throws Exception {
        // Combination: receiver__real=1.0d, factor=-1.0d
        Object actual = (new Complex(1.0d)).multiply(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_113() throws Exception {
        // Combination: receiver__real=-1.0d, factor=-1.0d
        Object actual = (new Complex(-1.0d)).multiply(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_114() throws Exception {
        // Combination: receiver__real=Double.NaN, factor=-1.0d
        Object actual = (new Complex(Double.NaN)).multiply(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_115() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, factor=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).multiply(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_116() throws Exception {
        // Combination: receiver__real=0.0d, factor=Double.NaN
        Object actual = (new Complex(0.0d)).multiply(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_117() throws Exception {
        // Combination: receiver__real=1.0d, factor=Double.NaN
        Object actual = (new Complex(1.0d)).multiply(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_118() throws Exception {
        // Combination: receiver__real=-1.0d, factor=Double.NaN
        Object actual = (new Complex(-1.0d)).multiply(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_119() throws Exception {
        // Combination: receiver__real=Double.NaN, factor=Double.NaN
        Object actual = (new Complex(Double.NaN)).multiply(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_120() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, factor=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).multiply(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_121() throws Exception {
        // Combination: receiver__real=0.0d, factor=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d)).multiply(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_122() throws Exception {
        // Combination: receiver__real=1.0d, factor=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d)).multiply(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_123() throws Exception {
        // Combination: receiver__real=-1.0d, factor=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d)).multiply(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_124() throws Exception {
        // Combination: receiver__real=Double.NaN, factor=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN)).multiply(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_125() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, factor=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).multiply(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_126() throws Exception {
        // Combination: receiver__real=0.0d, subtrahend=0.0d
        Object actual = (new Complex(0.0d)).subtract(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_127() throws Exception {
        // Combination: receiver__real=0.0d, subtrahend=1.0d
        Object actual = (new Complex(0.0d)).subtract(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_128() throws Exception {
        // Combination: receiver__real=0.0d, subtrahend=-1.0d
        Object actual = (new Complex(0.0d)).subtract(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_129() throws Exception {
        // Combination: receiver__real=0.0d, subtrahend=Double.NaN
        Object actual = (new Complex(0.0d)).subtract(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_130() throws Exception {
        // Combination: receiver__real=0.0d, subtrahend=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d)).subtract(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_131() throws Exception {
        // Combination: receiver__real=1.0d, subtrahend=0.0d
        Object actual = (new Complex(1.0d)).subtract(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_132() throws Exception {
        // Combination: receiver__real=1.0d, subtrahend=1.0d
        Object actual = (new Complex(1.0d)).subtract(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_133() throws Exception {
        // Combination: receiver__real=1.0d, subtrahend=-1.0d
        Object actual = (new Complex(1.0d)).subtract(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(2.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_134() throws Exception {
        // Combination: receiver__real=1.0d, subtrahend=Double.NaN
        Object actual = (new Complex(1.0d)).subtract(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_135() throws Exception {
        // Combination: receiver__real=1.0d, subtrahend=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d)).subtract(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_136() throws Exception {
        // Combination: receiver__real=-1.0d, subtrahend=0.0d
        Object actual = (new Complex(-1.0d)).subtract(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_137() throws Exception {
        // Combination: receiver__real=-1.0d, subtrahend=1.0d
        Object actual = (new Complex(-1.0d)).subtract(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-2.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_138() throws Exception {
        // Combination: receiver__real=-1.0d, subtrahend=-1.0d
        Object actual = (new Complex(-1.0d)).subtract(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_139() throws Exception {
        // Combination: receiver__real=-1.0d, subtrahend=Double.NaN
        Object actual = (new Complex(-1.0d)).subtract(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_140() throws Exception {
        // Combination: receiver__real=-1.0d, subtrahend=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d)).subtract(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_141() throws Exception {
        // Combination: receiver__real=Double.NaN, subtrahend=0.0d
        Object actual = (new Complex(Double.NaN)).subtract(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_142() throws Exception {
        // Combination: receiver__real=Double.NaN, subtrahend=1.0d
        Object actual = (new Complex(Double.NaN)).subtract(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_143() throws Exception {
        // Combination: receiver__real=Double.NaN, subtrahend=-1.0d
        Object actual = (new Complex(Double.NaN)).subtract(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_144() throws Exception {
        // Combination: receiver__real=Double.NaN, subtrahend=Double.NaN
        Object actual = (new Complex(Double.NaN)).subtract(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_145() throws Exception {
        // Combination: receiver__real=Double.NaN, subtrahend=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN)).subtract(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_146() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, subtrahend=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).subtract(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_147() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, subtrahend=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).subtract(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_148() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, subtrahend=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).subtract(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_149() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, subtrahend=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).subtract(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subtract_pairwise_150() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, subtrahend=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).subtract(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_151() throws Exception {
        // Combination: receiver__real=0.0d, x=0.0d
        Object actual = (new Complex(0.0d)).pow(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_152() throws Exception {
        // Combination: receiver__real=0.0d, x=1.0d
        Object actual = (new Complex(0.0d)).pow(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_153() throws Exception {
        // Combination: receiver__real=0.0d, x=-1.0d
        Object actual = (new Complex(0.0d)).pow(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_154() throws Exception {
        // Combination: receiver__real=0.0d, x=Double.NaN
        Object actual = (new Complex(0.0d)).pow(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_155() throws Exception {
        // Combination: receiver__real=0.0d, x=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d)).pow(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_156() throws Exception {
        // Combination: receiver__real=1.0d, x=0.0d
        Object actual = (new Complex(1.0d)).pow(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_157() throws Exception {
        // Combination: receiver__real=1.0d, x=1.0d
        Object actual = (new Complex(1.0d)).pow(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_158() throws Exception {
        // Combination: receiver__real=1.0d, x=-1.0d
        Object actual = (new Complex(1.0d)).pow(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, -0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_159() throws Exception {
        // Combination: receiver__real=1.0d, x=Double.NaN
        Object actual = (new Complex(1.0d)).pow(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_160() throws Exception {
        // Combination: receiver__real=1.0d, x=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d)).pow(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_161() throws Exception {
        // Combination: receiver__real=-1.0d, x=0.0d
        Object actual = (new Complex(-1.0d)).pow(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_162() throws Exception {
        // Combination: receiver__real=-1.0d, x=1.0d
        Object actual = (new Complex(-1.0d)).pow(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 1.2246467991473532E-16)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_163() throws Exception {
        // Combination: receiver__real=-1.0d, x=-1.0d
        Object actual = (new Complex(-1.0d)).pow(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, -1.2246467991473532E-16)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_164() throws Exception {
        // Combination: receiver__real=-1.0d, x=Double.NaN
        Object actual = (new Complex(-1.0d)).pow(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_165() throws Exception {
        // Combination: receiver__real=-1.0d, x=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d)).pow(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_166() throws Exception {
        // Combination: receiver__real=Double.NaN, x=0.0d
        Object actual = (new Complex(Double.NaN)).pow(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_167() throws Exception {
        // Combination: receiver__real=Double.NaN, x=1.0d
        Object actual = (new Complex(Double.NaN)).pow(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_168() throws Exception {
        // Combination: receiver__real=Double.NaN, x=-1.0d
        Object actual = (new Complex(Double.NaN)).pow(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_169() throws Exception {
        // Combination: receiver__real=Double.NaN, x=Double.NaN
        Object actual = (new Complex(Double.NaN)).pow(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_170() throws Exception {
        // Combination: receiver__real=Double.NaN, x=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN)).pow(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_171() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, x=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).pow(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_172() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, x=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).pow(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_173() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, x=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).pow(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_174() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, x=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).pow(Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_pow_pairwise_175() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, x=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).pow(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_176() throws Exception {
        // Combination: receiver__real=0.0d, realPart=0.0d, imaginaryPart=0.0d
        Object actual = (new Complex(0.0d)).createComplex(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_177() throws Exception {
        // Combination: receiver__real=1.0d, realPart=1.0d, imaginaryPart=0.0d
        Object actual = (new Complex(1.0d)).createComplex(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_178() throws Exception {
        // Combination: receiver__real=-1.0d, realPart=-1.0d, imaginaryPart=0.0d
        Object actual = (new Complex(-1.0d)).createComplex(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_179() throws Exception {
        // Combination: receiver__real=Double.NaN, realPart=Double.NaN, imaginaryPart=0.0d
        Object actual = (new Complex(Double.NaN)).createComplex(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_180() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, realPart=Double.POSITIVE_INFINITY, imaginaryPart=0.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).createComplex(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_181() throws Exception {
        // Combination: receiver__real=1.0d, realPart=0.0d, imaginaryPart=1.0d
        Object actual = (new Complex(1.0d)).createComplex(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_182() throws Exception {
        // Combination: receiver__real=0.0d, realPart=1.0d, imaginaryPart=1.0d
        Object actual = (new Complex(0.0d)).createComplex(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_183() throws Exception {
        // Combination: receiver__real=Double.NaN, realPart=-1.0d, imaginaryPart=1.0d
        Object actual = (new Complex(Double.NaN)).createComplex(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_184() throws Exception {
        // Combination: receiver__real=-1.0d, realPart=Double.NaN, imaginaryPart=1.0d
        Object actual = (new Complex(-1.0d)).createComplex(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_185() throws Exception {
        // Combination: receiver__real=0.0d, realPart=Double.POSITIVE_INFINITY, imaginaryPart=1.0d
        Object actual = (new Complex(0.0d)).createComplex(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_186() throws Exception {
        // Combination: receiver__real=-1.0d, realPart=0.0d, imaginaryPart=-1.0d
        Object actual = (new Complex(-1.0d)).createComplex(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_187() throws Exception {
        // Combination: receiver__real=Double.NaN, realPart=1.0d, imaginaryPart=-1.0d
        Object actual = (new Complex(Double.NaN)).createComplex(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_188() throws Exception {
        // Combination: receiver__real=0.0d, realPart=-1.0d, imaginaryPart=-1.0d
        Object actual = (new Complex(0.0d)).createComplex(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_189() throws Exception {
        // Combination: receiver__real=1.0d, realPart=Double.NaN, imaginaryPart=-1.0d
        Object actual = (new Complex(1.0d)).createComplex(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_190() throws Exception {
        // Combination: receiver__real=1.0d, realPart=Double.POSITIVE_INFINITY, imaginaryPart=-1.0d
        Object actual = (new Complex(1.0d)).createComplex(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_191() throws Exception {
        // Combination: receiver__real=Double.NaN, realPart=0.0d, imaginaryPart=Double.NaN
        Object actual = (new Complex(Double.NaN)).createComplex(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_192() throws Exception {
        // Combination: receiver__real=-1.0d, realPart=1.0d, imaginaryPart=Double.NaN
        Object actual = (new Complex(-1.0d)).createComplex(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_193() throws Exception {
        // Combination: receiver__real=1.0d, realPart=-1.0d, imaginaryPart=Double.NaN
        Object actual = (new Complex(1.0d)).createComplex(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_194() throws Exception {
        // Combination: receiver__real=0.0d, realPart=Double.NaN, imaginaryPart=Double.NaN
        Object actual = (new Complex(0.0d)).createComplex(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_195() throws Exception {
        // Combination: receiver__real=-1.0d, realPart=Double.POSITIVE_INFINITY, imaginaryPart=Double.NaN
        Object actual = (new Complex(-1.0d)).createComplex(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_196() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, realPart=0.0d, imaginaryPart=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).createComplex(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_197() throws Exception {
        // Combination: receiver__real=0.0d, realPart=1.0d, imaginaryPart=Double.POSITIVE_INFINITY
        Object actual = (new Complex(0.0d)).createComplex(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_198() throws Exception {
        // Combination: receiver__real=1.0d, realPart=-1.0d, imaginaryPart=Double.POSITIVE_INFINITY
        Object actual = (new Complex(1.0d)).createComplex(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_199() throws Exception {
        // Combination: receiver__real=-1.0d, realPart=Double.NaN, imaginaryPart=Double.POSITIVE_INFINITY
        Object actual = (new Complex(-1.0d)).createComplex(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_200() throws Exception {
        // Combination: receiver__real=Double.NaN, realPart=Double.POSITIVE_INFINITY, imaginaryPart=Double.POSITIVE_INFINITY
        Object actual = (new Complex(Double.NaN)).createComplex(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_201() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, realPart=1.0d, imaginaryPart=1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).createComplex(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_202() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, realPart=-1.0d, imaginaryPart=-1.0d
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).createComplex(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_createComplex_pairwise_203() throws Exception {
        // Combination: receiver__real=Double.POSITIVE_INFINITY, realPart=Double.NaN, imaginaryPart=Double.NaN
        Object actual = (new Complex(Double.POSITIVE_INFINITY)).createComplex(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_204() throws Exception {
        // Combination: realPart=0.0d, imaginaryPart=0.0d
        Object actual = Complex.valueOf(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_205() throws Exception {
        // Combination: realPart=1.0d, imaginaryPart=0.0d
        Object actual = Complex.valueOf(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_206() throws Exception {
        // Combination: realPart=-1.0d, imaginaryPart=0.0d
        Object actual = Complex.valueOf(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_207() throws Exception {
        // Combination: realPart=Double.NaN, imaginaryPart=0.0d
        Object actual = Complex.valueOf(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_208() throws Exception {
        // Combination: realPart=Double.POSITIVE_INFINITY, imaginaryPart=0.0d
        Object actual = Complex.valueOf(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 0.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_209() throws Exception {
        // Combination: realPart=0.0d, imaginaryPart=1.0d
        Object actual = Complex.valueOf(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_210() throws Exception {
        // Combination: realPart=1.0d, imaginaryPart=1.0d
        Object actual = Complex.valueOf(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_211() throws Exception {
        // Combination: realPart=-1.0d, imaginaryPart=1.0d
        Object actual = Complex.valueOf(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_212() throws Exception {
        // Combination: realPart=Double.NaN, imaginaryPart=1.0d
        Object actual = Complex.valueOf(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_213() throws Exception {
        // Combination: realPart=Double.POSITIVE_INFINITY, imaginaryPart=1.0d
        Object actual = Complex.valueOf(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, 1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_214() throws Exception {
        // Combination: realPart=0.0d, imaginaryPart=-1.0d
        Object actual = Complex.valueOf(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_215() throws Exception {
        // Combination: realPart=1.0d, imaginaryPart=-1.0d
        Object actual = Complex.valueOf(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_216() throws Exception {
        // Combination: realPart=-1.0d, imaginaryPart=-1.0d
        Object actual = Complex.valueOf(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_217() throws Exception {
        // Combination: realPart=Double.NaN, imaginaryPart=-1.0d
        Object actual = Complex.valueOf(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_218() throws Exception {
        // Combination: realPart=Double.POSITIVE_INFINITY, imaginaryPart=-1.0d
        Object actual = Complex.valueOf(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, -1.0)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_219() throws Exception {
        // Combination: realPart=0.0d, imaginaryPart=Double.NaN
        Object actual = Complex.valueOf(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_220() throws Exception {
        // Combination: realPart=1.0d, imaginaryPart=Double.NaN
        Object actual = Complex.valueOf(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_221() throws Exception {
        // Combination: realPart=-1.0d, imaginaryPart=Double.NaN
        Object actual = Complex.valueOf(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_222() throws Exception {
        // Combination: realPart=Double.NaN, imaginaryPart=Double.NaN
        Object actual = Complex.valueOf(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_223() throws Exception {
        // Combination: realPart=Double.POSITIVE_INFINITY, imaginaryPart=Double.NaN
        Object actual = Complex.valueOf(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_224() throws Exception {
        // Combination: realPart=0.0d, imaginaryPart=Double.POSITIVE_INFINITY
        Object actual = Complex.valueOf(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(0.0, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_225() throws Exception {
        // Combination: realPart=1.0d, imaginaryPart=Double.POSITIVE_INFINITY
        Object actual = Complex.valueOf(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(1.0, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_226() throws Exception {
        // Combination: realPart=-1.0d, imaginaryPart=Double.POSITIVE_INFINITY
        Object actual = Complex.valueOf(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(-1.0, Infinity)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_227() throws Exception {
        // Combination: realPart=Double.NaN, imaginaryPart=Double.POSITIVE_INFINITY
        Object actual = Complex.valueOf(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(NaN, NaN)", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_valueOf_pairwise_228() throws Exception {
        // Combination: realPart=Double.POSITIVE_INFINITY, imaginaryPart=Double.POSITIVE_INFINITY
        Object actual = Complex.valueOf(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.complex.Complex", actual.getClass().getName());
        assertEquals("(Infinity, Infinity)", String.valueOf(actual));
    }

}
