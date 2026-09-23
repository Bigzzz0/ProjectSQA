package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for SingularValueDecompositionImpl.
 */
public class SingularValueDecompositionImpl_IPOTest {
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
    public void test_getCovariance_pairwise_001() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), minSingularValue=0.0d
        Object actual = (new SingularValueDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}))).getCovariance(0.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{0.0,0.0},{0.0,2.0}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getCovariance_pairwise_002() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), minSingularValue=1.0d
        Object actual = (new SingularValueDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}))).getCovariance(1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{0.0,0.0},{0.0,2.0}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getCovariance_pairwise_003() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), minSingularValue=-1.0d
        Object actual = (new SingularValueDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}))).getCovariance(-1.0d);
        assertNotNull(actual);
        assertEquals("org.apache.commons.math.linear.Array2DRowRealMatrix", actual.getClass().getName());
        assertEquals("Array2DRowRealMatrix{{0.0,0.0},{0.0,2.0}}", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getCovariance_pairwise_004() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), minSingularValue=Double.NaN
        try {
            (new SingularValueDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}))).getCovariance(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCovariance_pairwise_005() throws Exception {
        // Combination: receiver__matrix=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}), minSingularValue=Double.POSITIVE_INFINITY
        try {
            (new SingularValueDecompositionImpl(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}))).getCovariance(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
