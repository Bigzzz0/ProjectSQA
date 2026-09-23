package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for EigenDecompositionImpl.
 */
public class EigenDecompositionImpl_IPOTest {
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
    public void test_getV_pairwise_001() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getV();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{NaN,NaN},{NaN,NaN}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getV_pairwise_002() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getV();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{NaN,NaN},{NaN,NaN}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getV_pairwise_003() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getV();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{NaN,NaN},{NaN,NaN}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getV_pairwise_004() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getV();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{NaN,NaN},{NaN,NaN}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getV_pairwise_005() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getV();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{NaN,NaN},{NaN,NaN}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getD_pairwise_006() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getD();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{1.0,0.0},{0.0,1.0}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getD_pairwise_007() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getD();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{1.0,0.0},{0.0,1.0}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getD_pairwise_008() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getD();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{1.0,0.0},{0.0,1.0}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getD_pairwise_009() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getD();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{1.0,0.0},{0.0,1.0}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getD_pairwise_010() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getD();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{1.0,0.0},{0.0,1.0}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getVT_pairwise_011() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getVT();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{NaN,NaN},{NaN,NaN}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getVT_pairwise_012() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getVT();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{NaN,NaN},{NaN,NaN}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getVT_pairwise_013() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getVT();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{NaN,NaN},{NaN,NaN}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getVT_pairwise_014() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getVT();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{NaN,NaN},{NaN,NaN}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getVT_pairwise_015() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getVT();
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{NaN,NaN},{NaN,NaN}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalues_pairwise_016() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getRealEigenvalues();
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[1.0, 1.0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalues_pairwise_017() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getRealEigenvalues();
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[1.0, 1.0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalues_pairwise_018() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getRealEigenvalues();
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[1.0, 1.0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalues_pairwise_019() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getRealEigenvalues();
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[1.0, 1.0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalues_pairwise_020() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getRealEigenvalues();
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[1.0, 1.0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_021() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d, i=0
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getRealEigenvalue(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_022() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d, i=1
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getRealEigenvalue(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_023() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d, i=-1
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getRealEigenvalue(-1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_024() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN, i=Integer.MAX_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getRealEigenvalue(Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_025() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY, i=Integer.MIN_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getRealEigenvalue(Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_026() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d, i=1
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getRealEigenvalue(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_027() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d, i=-1
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getRealEigenvalue(-1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_028() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d, i=Integer.MAX_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getRealEigenvalue(Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_029() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d, i=Integer.MIN_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getRealEigenvalue(Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_030() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d, i=0
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getRealEigenvalue(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_031() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d, i=-1
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getRealEigenvalue(-1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_032() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d, i=Integer.MAX_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getRealEigenvalue(Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_033() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d, i=Integer.MIN_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getRealEigenvalue(Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_034() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d, i=0
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getRealEigenvalue(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_035() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d, i=1
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getRealEigenvalue(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_036() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d, i=Integer.MAX_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getRealEigenvalue(Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_037() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d, i=Integer.MIN_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getRealEigenvalue(Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_038() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN, i=0
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getRealEigenvalue(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_039() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN, i=1
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getRealEigenvalue(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_040() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN, i=-1
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getRealEigenvalue(-1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_041() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN, i=Integer.MIN_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getRealEigenvalue(Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_042() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY, i=0
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getRealEigenvalue(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_043() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY, i=1
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getRealEigenvalue(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_044() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY, i=-1
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getRealEigenvalue(-1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRealEigenvalue_pairwise_045() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY, i=Integer.MAX_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getRealEigenvalue(Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalues_pairwise_046() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getImagEigenvalues();
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[0.0, 0.0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalues_pairwise_047() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getImagEigenvalues();
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[0.0, 0.0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalues_pairwise_048() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getImagEigenvalues();
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[0.0, 0.0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalues_pairwise_049() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getImagEigenvalues();
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[0.0, 0.0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalues_pairwise_050() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getImagEigenvalues();
        assertNotNull(actual);
        assertEquals("[D", actual.getClass().getName());
        assertEquals("[0.0, 0.0]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_051() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d, i=0
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getImagEigenvalue(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_052() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d, i=1
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getImagEigenvalue(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_053() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d, i=-1
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getImagEigenvalue(-1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_054() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN, i=Integer.MAX_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getImagEigenvalue(Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_055() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY, i=Integer.MIN_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getImagEigenvalue(Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_056() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d, i=1
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getImagEigenvalue(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_057() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d, i=-1
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getImagEigenvalue(-1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_058() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d, i=Integer.MAX_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getImagEigenvalue(Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_059() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d, i=Integer.MIN_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getImagEigenvalue(Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_060() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d, i=0
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getImagEigenvalue(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_061() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d, i=-1
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getImagEigenvalue(-1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_062() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d, i=Integer.MAX_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getImagEigenvalue(Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_063() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d, i=Integer.MIN_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getImagEigenvalue(Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_064() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d, i=0
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getImagEigenvalue(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_065() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d, i=1
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getImagEigenvalue(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_066() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d, i=Integer.MAX_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getImagEigenvalue(Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_067() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d, i=Integer.MIN_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getImagEigenvalue(Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_068() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN, i=0
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getImagEigenvalue(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_069() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN, i=1
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getImagEigenvalue(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_070() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN, i=-1
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getImagEigenvalue(-1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_071() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN, i=Integer.MIN_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getImagEigenvalue(Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_072() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY, i=0
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getImagEigenvalue(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_073() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY, i=1
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getImagEigenvalue(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_074() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY, i=-1
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getImagEigenvalue(-1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getImagEigenvalue_pairwise_075() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY, i=Integer.MAX_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getImagEigenvalue(Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_076() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d, i=0
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getEigenvector(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.ArrayRealVector", actual.getClass().getName());
        assertEquals("{(NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_077() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d, i=1
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getEigenvector(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.ArrayRealVector", actual.getClass().getName());
        assertEquals("{(NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_078() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d, i=-1
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getEigenvector(-1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_079() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN, i=Integer.MAX_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getEigenvector(Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_080() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY, i=Integer.MIN_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getEigenvector(Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_081() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d, i=1
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getEigenvector(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.ArrayRealVector", actual.getClass().getName());
        assertEquals("{(NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_082() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d, i=-1
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getEigenvector(-1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_083() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d, i=Integer.MAX_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getEigenvector(Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_084() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d, i=Integer.MIN_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getEigenvector(Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_085() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d, i=0
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getEigenvector(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.ArrayRealVector", actual.getClass().getName());
        assertEquals("{(NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_086() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d, i=-1
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getEigenvector(-1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_087() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d, i=Integer.MAX_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getEigenvector(Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_088() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d, i=Integer.MIN_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getEigenvector(Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_089() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d, i=0
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getEigenvector(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.ArrayRealVector", actual.getClass().getName());
        assertEquals("{(NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_090() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d, i=1
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getEigenvector(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.ArrayRealVector", actual.getClass().getName());
        assertEquals("{(NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_091() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d, i=Integer.MAX_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getEigenvector(Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_092() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d, i=Integer.MIN_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getEigenvector(Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_093() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN, i=0
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getEigenvector(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.ArrayRealVector", actual.getClass().getName());
        assertEquals("{(NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_094() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN, i=1
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getEigenvector(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.ArrayRealVector", actual.getClass().getName());
        assertEquals("{(NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_095() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN, i=-1
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getEigenvector(-1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_096() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN, i=Integer.MIN_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getEigenvector(Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_097() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY, i=0
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getEigenvector(0);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.ArrayRealVector", actual.getClass().getName());
        assertEquals("{(NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_098() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY, i=1
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getEigenvector(1);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.ArrayRealVector", actual.getClass().getName());
        assertEquals("{(NaN); (NaN)}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_099() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY, i=-1
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getEigenvector(-1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEigenvector_pairwise_100() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY, i=Integer.MAX_VALUE
        try {
            (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getEigenvector(Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDeterminant_pairwise_101() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=0.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 0.0d)).getDeterminant();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDeterminant_pairwise_102() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=1.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), 1.0d)).getDeterminant();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDeterminant_pairwise_103() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=-1.0d
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), -1.0d)).getDeterminant();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDeterminant_pairwise_104() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.NaN
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.NaN)).getDeterminant();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDeterminant_pairwise_105() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), receiver__splitTolerance=Double.POSITIVE_INFINITY
        Object actual = (new EigenDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), Double.POSITIVE_INFINITY)).getDeterminant();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

}
