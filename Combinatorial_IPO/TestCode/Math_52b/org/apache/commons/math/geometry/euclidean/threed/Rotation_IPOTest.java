package org.apache.commons.math.geometry.euclidean.threed;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Rotation.
 */
public class Rotation_IPOTest {
    @Test(timeout = 4000)
    public void test_getQ0_pairwise_001() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=0.0d, receiver__q2=0.0d, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, 0.0d, 0.0d, 0.0d, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_002() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=1.0d, receiver__q2=1.0d, receiver__q3=1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, 1.0d, 1.0d, 1.0d, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_003() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=-1.0d, receiver__q2=-1.0d, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(-1.0d, -1.0d, -1.0d, -1.0d, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_004() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=Double.NaN, receiver__q2=Double.NaN, receiver__q3=Double.NaN, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, Double.NaN, Double.NaN, Double.NaN, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_005() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_006() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=1.0d, receiver__q2=-1.0d, receiver__q3=Double.NaN, receiver__needsNormalization=false
        Object actual = (new Rotation(0.0d, 1.0d, -1.0d, Double.NaN, false)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_007() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=0.0d, receiver__q2=Double.NaN, receiver__q3=-1.0d, receiver__needsNormalization=false
        Object actual = (new Rotation(1.0d, 0.0d, Double.NaN, -1.0d, false)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_008() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=Double.NaN, receiver__q2=0.0d, receiver__q3=1.0d, receiver__needsNormalization=false
        Object actual = (new Rotation(-1.0d, Double.NaN, 0.0d, 1.0d, false)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_009() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=-1.0d, receiver__q2=1.0d, receiver__q3=0.0d, receiver__needsNormalization=false
        Object actual = (new Rotation(Double.NaN, -1.0d, 1.0d, 0.0d, false)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_010() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=0.0d, receiver__q2=1.0d, receiver__q3=Double.NaN, receiver__needsNormalization=false
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, 0.0d, 1.0d, Double.NaN, false)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_011() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=0.0d, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(-1.0d, 0.0d, Double.POSITIVE_INFINITY, 0.0d, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_012() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=0.0d, receiver__q2=-1.0d, receiver__q3=1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, 0.0d, -1.0d, 1.0d, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_013() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=1.0d, receiver__q2=Double.NaN, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(-1.0d, 1.0d, Double.NaN, Double.POSITIVE_INFINITY, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_014() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=1.0d, receiver__q2=0.0d, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, 1.0d, 0.0d, -1.0d, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_015() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=1.0d, receiver__q2=0.0d, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, 1.0d, 0.0d, 0.0d, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_016() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=-1.0d, receiver__q2=Double.NaN, receiver__q3=1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, -1.0d, Double.NaN, 1.0d, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_017() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=-1.0d, receiver__q2=0.0d, receiver__q3=Double.NaN, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, -1.0d, 0.0d, Double.NaN, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_018() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=-1.0d, receiver__q2=-1.0d, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, -1.0d, -1.0d, Double.POSITIVE_INFINITY, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_019() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=Double.NaN, receiver__q2=1.0d, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, Double.NaN, 1.0d, -1.0d, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_020() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=Double.NaN, receiver__q2=-1.0d, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, Double.NaN, -1.0d, 0.0d, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_021() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=Double.NaN, receiver__q2=Double.NaN, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, Double.NaN, Double.NaN, 0.0d, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_022() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=1.0d, receiver__needsNormalization=false
        Object actual = (new Rotation(0.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d, false)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_023() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=0.0d, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, Double.POSITIVE_INFINITY, 0.0d, Double.POSITIVE_INFINITY, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_024() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=1.0d, receiver__q3=Double.NaN, receiver__needsNormalization=true
        Object actual = (new Rotation(-1.0d, Double.POSITIVE_INFINITY, 1.0d, Double.NaN, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_025() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=-1.0d, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, Double.POSITIVE_INFINITY, -1.0d, 0.0d, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_026() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=Double.NaN, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, Double.POSITIVE_INFINITY, Double.NaN, -1.0d, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_027() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=1.0d, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, 1.0d, Double.POSITIVE_INFINITY, -1.0d, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_028() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=-1.0d, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=Double.NaN, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, -1.0d, Double.POSITIVE_INFINITY, Double.NaN, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_029() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=Double.NaN, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, Double.NaN, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_030() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=0.0d, receiver__q2=0.0d, receiver__q3=1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, 0.0d, 0.0d, 1.0d, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_031() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=0.0d, receiver__q2=0.0d, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, 0.0d, 0.0d, -1.0d, true)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ0_pairwise_032() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=0.0d, receiver__q2=1.0d, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=false
        Object actual = (new Rotation(Double.NaN, 0.0d, 1.0d, Double.POSITIVE_INFINITY, false)).getQ0();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_033() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=0.0d, receiver__q2=0.0d, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, 0.0d, 0.0d, 0.0d, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_034() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=1.0d, receiver__q2=1.0d, receiver__q3=1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, 1.0d, 1.0d, 1.0d, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_035() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=-1.0d, receiver__q2=-1.0d, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(-1.0d, -1.0d, -1.0d, -1.0d, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_036() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=Double.NaN, receiver__q2=Double.NaN, receiver__q3=Double.NaN, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, Double.NaN, Double.NaN, Double.NaN, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_037() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_038() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=1.0d, receiver__q2=-1.0d, receiver__q3=Double.NaN, receiver__needsNormalization=false
        Object actual = (new Rotation(0.0d, 1.0d, -1.0d, Double.NaN, false)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_039() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=0.0d, receiver__q2=Double.NaN, receiver__q3=-1.0d, receiver__needsNormalization=false
        Object actual = (new Rotation(1.0d, 0.0d, Double.NaN, -1.0d, false)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_040() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=Double.NaN, receiver__q2=0.0d, receiver__q3=1.0d, receiver__needsNormalization=false
        Object actual = (new Rotation(-1.0d, Double.NaN, 0.0d, 1.0d, false)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_041() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=-1.0d, receiver__q2=1.0d, receiver__q3=0.0d, receiver__needsNormalization=false
        Object actual = (new Rotation(Double.NaN, -1.0d, 1.0d, 0.0d, false)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_042() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=0.0d, receiver__q2=1.0d, receiver__q3=Double.NaN, receiver__needsNormalization=false
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, 0.0d, 1.0d, Double.NaN, false)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_043() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=0.0d, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(-1.0d, 0.0d, Double.POSITIVE_INFINITY, 0.0d, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_044() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=0.0d, receiver__q2=-1.0d, receiver__q3=1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, 0.0d, -1.0d, 1.0d, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_045() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=1.0d, receiver__q2=Double.NaN, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(-1.0d, 1.0d, Double.NaN, Double.POSITIVE_INFINITY, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_046() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=1.0d, receiver__q2=0.0d, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, 1.0d, 0.0d, -1.0d, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_047() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=1.0d, receiver__q2=0.0d, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, 1.0d, 0.0d, 0.0d, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_048() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=-1.0d, receiver__q2=Double.NaN, receiver__q3=1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, -1.0d, Double.NaN, 1.0d, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_049() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=-1.0d, receiver__q2=0.0d, receiver__q3=Double.NaN, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, -1.0d, 0.0d, Double.NaN, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_050() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=-1.0d, receiver__q2=-1.0d, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, -1.0d, -1.0d, Double.POSITIVE_INFINITY, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_051() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=Double.NaN, receiver__q2=1.0d, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, Double.NaN, 1.0d, -1.0d, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_052() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=Double.NaN, receiver__q2=-1.0d, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, Double.NaN, -1.0d, 0.0d, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_053() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=Double.NaN, receiver__q2=Double.NaN, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, Double.NaN, Double.NaN, 0.0d, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_054() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=1.0d, receiver__needsNormalization=false
        Object actual = (new Rotation(0.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d, false)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_055() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=0.0d, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, Double.POSITIVE_INFINITY, 0.0d, Double.POSITIVE_INFINITY, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_056() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=1.0d, receiver__q3=Double.NaN, receiver__needsNormalization=true
        Object actual = (new Rotation(-1.0d, Double.POSITIVE_INFINITY, 1.0d, Double.NaN, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_057() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=-1.0d, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, Double.POSITIVE_INFINITY, -1.0d, 0.0d, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_058() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=Double.NaN, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, Double.POSITIVE_INFINITY, Double.NaN, -1.0d, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_059() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=1.0d, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, 1.0d, Double.POSITIVE_INFINITY, -1.0d, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_060() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=-1.0d, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=Double.NaN, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, -1.0d, Double.POSITIVE_INFINITY, Double.NaN, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_061() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=Double.NaN, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, Double.NaN, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_062() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=0.0d, receiver__q2=0.0d, receiver__q3=1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, 0.0d, 0.0d, 1.0d, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_063() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=0.0d, receiver__q2=0.0d, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, 0.0d, 0.0d, -1.0d, true)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ1_pairwise_064() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=0.0d, receiver__q2=1.0d, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=false
        Object actual = (new Rotation(Double.NaN, 0.0d, 1.0d, Double.POSITIVE_INFINITY, false)).getQ1();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_065() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=0.0d, receiver__q2=0.0d, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, 0.0d, 0.0d, 0.0d, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_066() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=1.0d, receiver__q2=1.0d, receiver__q3=1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, 1.0d, 1.0d, 1.0d, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_067() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=-1.0d, receiver__q2=-1.0d, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(-1.0d, -1.0d, -1.0d, -1.0d, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_068() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=Double.NaN, receiver__q2=Double.NaN, receiver__q3=Double.NaN, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, Double.NaN, Double.NaN, Double.NaN, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_069() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_070() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=1.0d, receiver__q2=-1.0d, receiver__q3=Double.NaN, receiver__needsNormalization=false
        Object actual = (new Rotation(0.0d, 1.0d, -1.0d, Double.NaN, false)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_071() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=0.0d, receiver__q2=Double.NaN, receiver__q3=-1.0d, receiver__needsNormalization=false
        Object actual = (new Rotation(1.0d, 0.0d, Double.NaN, -1.0d, false)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_072() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=Double.NaN, receiver__q2=0.0d, receiver__q3=1.0d, receiver__needsNormalization=false
        Object actual = (new Rotation(-1.0d, Double.NaN, 0.0d, 1.0d, false)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_073() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=-1.0d, receiver__q2=1.0d, receiver__q3=0.0d, receiver__needsNormalization=false
        Object actual = (new Rotation(Double.NaN, -1.0d, 1.0d, 0.0d, false)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_074() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=0.0d, receiver__q2=1.0d, receiver__q3=Double.NaN, receiver__needsNormalization=false
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, 0.0d, 1.0d, Double.NaN, false)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_075() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=0.0d, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(-1.0d, 0.0d, Double.POSITIVE_INFINITY, 0.0d, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_076() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=0.0d, receiver__q2=-1.0d, receiver__q3=1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, 0.0d, -1.0d, 1.0d, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_077() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=1.0d, receiver__q2=Double.NaN, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(-1.0d, 1.0d, Double.NaN, Double.POSITIVE_INFINITY, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_078() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=1.0d, receiver__q2=0.0d, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, 1.0d, 0.0d, -1.0d, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_079() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=1.0d, receiver__q2=0.0d, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, 1.0d, 0.0d, 0.0d, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_080() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=-1.0d, receiver__q2=Double.NaN, receiver__q3=1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, -1.0d, Double.NaN, 1.0d, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_081() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=-1.0d, receiver__q2=0.0d, receiver__q3=Double.NaN, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, -1.0d, 0.0d, Double.NaN, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_082() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=-1.0d, receiver__q2=-1.0d, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, -1.0d, -1.0d, Double.POSITIVE_INFINITY, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_083() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=Double.NaN, receiver__q2=1.0d, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, Double.NaN, 1.0d, -1.0d, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_084() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=Double.NaN, receiver__q2=-1.0d, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, Double.NaN, -1.0d, 0.0d, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_085() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=Double.NaN, receiver__q2=Double.NaN, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, Double.NaN, Double.NaN, 0.0d, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_086() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=1.0d, receiver__needsNormalization=false
        Object actual = (new Rotation(0.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d, false)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_087() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=0.0d, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, Double.POSITIVE_INFINITY, 0.0d, Double.POSITIVE_INFINITY, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_088() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=1.0d, receiver__q3=Double.NaN, receiver__needsNormalization=true
        Object actual = (new Rotation(-1.0d, Double.POSITIVE_INFINITY, 1.0d, Double.NaN, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_089() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=-1.0d, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, Double.POSITIVE_INFINITY, -1.0d, 0.0d, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_090() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=Double.NaN, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, Double.POSITIVE_INFINITY, Double.NaN, -1.0d, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_091() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=1.0d, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, 1.0d, Double.POSITIVE_INFINITY, -1.0d, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_092() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=-1.0d, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=Double.NaN, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, -1.0d, Double.POSITIVE_INFINITY, Double.NaN, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_093() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=Double.NaN, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, Double.NaN, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_094() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=0.0d, receiver__q2=0.0d, receiver__q3=1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, 0.0d, 0.0d, 1.0d, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_095() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=0.0d, receiver__q2=0.0d, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, 0.0d, 0.0d, -1.0d, true)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ2_pairwise_096() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=0.0d, receiver__q2=1.0d, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=false
        Object actual = (new Rotation(Double.NaN, 0.0d, 1.0d, Double.POSITIVE_INFINITY, false)).getQ2();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_097() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=0.0d, receiver__q2=0.0d, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, 0.0d, 0.0d, 0.0d, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_098() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=1.0d, receiver__q2=1.0d, receiver__q3=1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, 1.0d, 1.0d, 1.0d, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_099() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=-1.0d, receiver__q2=-1.0d, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(-1.0d, -1.0d, -1.0d, -1.0d, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_100() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=Double.NaN, receiver__q2=Double.NaN, receiver__q3=Double.NaN, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, Double.NaN, Double.NaN, Double.NaN, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_101() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_102() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=1.0d, receiver__q2=-1.0d, receiver__q3=Double.NaN, receiver__needsNormalization=false
        Object actual = (new Rotation(0.0d, 1.0d, -1.0d, Double.NaN, false)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_103() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=0.0d, receiver__q2=Double.NaN, receiver__q3=-1.0d, receiver__needsNormalization=false
        Object actual = (new Rotation(1.0d, 0.0d, Double.NaN, -1.0d, false)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_104() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=Double.NaN, receiver__q2=0.0d, receiver__q3=1.0d, receiver__needsNormalization=false
        Object actual = (new Rotation(-1.0d, Double.NaN, 0.0d, 1.0d, false)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_105() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=-1.0d, receiver__q2=1.0d, receiver__q3=0.0d, receiver__needsNormalization=false
        Object actual = (new Rotation(Double.NaN, -1.0d, 1.0d, 0.0d, false)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_106() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=0.0d, receiver__q2=1.0d, receiver__q3=Double.NaN, receiver__needsNormalization=false
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, 0.0d, 1.0d, Double.NaN, false)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_107() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=0.0d, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(-1.0d, 0.0d, Double.POSITIVE_INFINITY, 0.0d, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_108() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=0.0d, receiver__q2=-1.0d, receiver__q3=1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, 0.0d, -1.0d, 1.0d, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_109() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=1.0d, receiver__q2=Double.NaN, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(-1.0d, 1.0d, Double.NaN, Double.POSITIVE_INFINITY, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_110() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=1.0d, receiver__q2=0.0d, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, 1.0d, 0.0d, -1.0d, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_111() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=1.0d, receiver__q2=0.0d, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, 1.0d, 0.0d, 0.0d, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_112() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=-1.0d, receiver__q2=Double.NaN, receiver__q3=1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, -1.0d, Double.NaN, 1.0d, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_113() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=-1.0d, receiver__q2=0.0d, receiver__q3=Double.NaN, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, -1.0d, 0.0d, Double.NaN, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_114() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=-1.0d, receiver__q2=-1.0d, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, -1.0d, -1.0d, Double.POSITIVE_INFINITY, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_115() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=Double.NaN, receiver__q2=1.0d, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, Double.NaN, 1.0d, -1.0d, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_116() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=Double.NaN, receiver__q2=-1.0d, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, Double.NaN, -1.0d, 0.0d, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_117() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=Double.NaN, receiver__q2=Double.NaN, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, Double.NaN, Double.NaN, 0.0d, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_118() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=1.0d, receiver__needsNormalization=false
        Object actual = (new Rotation(0.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d, false)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_119() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=0.0d, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, Double.POSITIVE_INFINITY, 0.0d, Double.POSITIVE_INFINITY, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_120() throws Exception {
        // Combination: receiver__q0=-1.0d, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=1.0d, receiver__q3=Double.NaN, receiver__needsNormalization=true
        Object actual = (new Rotation(-1.0d, Double.POSITIVE_INFINITY, 1.0d, Double.NaN, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_121() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=-1.0d, receiver__q3=0.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, Double.POSITIVE_INFINITY, -1.0d, 0.0d, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_122() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=Double.POSITIVE_INFINITY, receiver__q2=Double.NaN, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, Double.POSITIVE_INFINITY, Double.NaN, -1.0d, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_123() throws Exception {
        // Combination: receiver__q0=1.0d, receiver__q1=1.0d, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(1.0d, 1.0d, Double.POSITIVE_INFINITY, -1.0d, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_124() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=-1.0d, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=Double.NaN, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.NaN, -1.0d, Double.POSITIVE_INFINITY, Double.NaN, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_125() throws Exception {
        // Combination: receiver__q0=0.0d, receiver__q1=Double.NaN, receiver__q2=Double.POSITIVE_INFINITY, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=true
        Object actual = (new Rotation(0.0d, Double.NaN, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_126() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=0.0d, receiver__q2=0.0d, receiver__q3=1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, 0.0d, 0.0d, 1.0d, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_127() throws Exception {
        // Combination: receiver__q0=Double.POSITIVE_INFINITY, receiver__q1=0.0d, receiver__q2=0.0d, receiver__q3=-1.0d, receiver__needsNormalization=true
        Object actual = (new Rotation(Double.POSITIVE_INFINITY, 0.0d, 0.0d, -1.0d, true)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getQ3_pairwise_128() throws Exception {
        // Combination: receiver__q0=Double.NaN, receiver__q1=0.0d, receiver__q2=1.0d, receiver__q3=Double.POSITIVE_INFINITY, receiver__needsNormalization=false
        Object actual = (new Rotation(Double.NaN, 0.0d, 1.0d, Double.POSITIVE_INFINITY, false)).getQ3();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

}
