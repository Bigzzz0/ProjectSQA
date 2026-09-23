package org.apache.commons.math3.distribution;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for HypergeometricDistribution.
 */
public class HypergeometricDistribution_IPOTest {
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
    public void test_cumulativeProbability_pairwise_001() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=0, receiver__sampleSize=0, x=0
        try {
            (new HypergeometricDistribution(0, 0, 0)).cumulativeProbability(0);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_002() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=0, receiver__sampleSize=1, x=1
        Object actual = (new HypergeometricDistribution(1, 0, 1)).cumulativeProbability(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_003() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=0, receiver__sampleSize=-1, x=-1
        try {
            (new HypergeometricDistribution(-1, 0, -1)).cumulativeProbability(-1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_004() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MAX_VALUE, x=Integer.MAX_VALUE
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)).cumulativeProbability(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_005() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MIN_VALUE, x=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 0, Integer.MIN_VALUE)).cumulativeProbability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_006() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=1, receiver__sampleSize=1, x=-1
        try {
            (new HypergeometricDistribution(0, 1, 1)).cumulativeProbability(-1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_007() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=0, x=Integer.MAX_VALUE
        Object actual = (new HypergeometricDistribution(1, 1, 0)).cumulativeProbability(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_008() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MAX_VALUE, x=0
        try {
            (new HypergeometricDistribution(-1, 1, Integer.MAX_VALUE)).cumulativeProbability(0);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_009() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=-1, x=1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, 1, -1)).cumulativeProbability(1);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_010() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=0, x=1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 1, 0)).cumulativeProbability(1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_011() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=-1, receiver__sampleSize=-1, x=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(0, -1, -1)).cumulativeProbability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_012() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MAX_VALUE, x=-1
        try {
            (new HypergeometricDistribution(1, -1, Integer.MAX_VALUE)).cumulativeProbability(-1);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_013() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=0, x=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(-1, -1, 0)).cumulativeProbability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_014() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1, x=0
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, -1, 1)).cumulativeProbability(0);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_015() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1, x=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, -1, 1)).cumulativeProbability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_016() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MAX_VALUE, x=1
        try {
            (new HypergeometricDistribution(0, Integer.MAX_VALUE, Integer.MAX_VALUE)).cumulativeProbability(1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_017() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1, x=0
        try {
            (new HypergeometricDistribution(1, Integer.MAX_VALUE, -1)).cumulativeProbability(0);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_018() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=1, x=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(-1, Integer.MAX_VALUE, 1)).cumulativeProbability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_019() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=0, x=-1
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, 0)).cumulativeProbability(-1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_020() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1, x=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MAX_VALUE, -1)).cumulativeProbability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_021() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MIN_VALUE, x=0
        try {
            (new HypergeometricDistribution(0, Integer.MIN_VALUE, Integer.MIN_VALUE)).cumulativeProbability(0);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_022() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=0, x=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(1, Integer.MIN_VALUE, 0)).cumulativeProbability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_023() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=1, x=1
        try {
            (new HypergeometricDistribution(-1, Integer.MIN_VALUE, 1)).cumulativeProbability(1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_024() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=-1, x=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MIN_VALUE, -1)).cumulativeProbability(-1);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_025() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MAX_VALUE, x=0
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)).cumulativeProbability(0);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_026() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MIN_VALUE, x=1
        try {
            (new HypergeometricDistribution(1, 1, Integer.MIN_VALUE)).cumulativeProbability(1);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_027() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MIN_VALUE, x=1
        try {
            (new HypergeometricDistribution(-1, -1, Integer.MIN_VALUE)).cumulativeProbability(1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_028() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MIN_VALUE, x=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE)).cumulativeProbability(-1);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_029() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=0, x=-1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 0, 0)).cumulativeProbability(-1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_030() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MIN_VALUE, x=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MIN_VALUE, Integer.MIN_VALUE)).cumulativeProbability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_031() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=1, receiver__sampleSize=1, x=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(0, 1, 1)).cumulativeProbability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_032() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MAX_VALUE, x=Integer.MIN_VALUE
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)).cumulativeProbability(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_033() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=0, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(0, 0, 0)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_034() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=0, receiver__sampleSize=1
        Object actual = (new HypergeometricDistribution(1, 0, 1)).getNumberOfSuccesses();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_035() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=0, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(-1, 0, -1)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_036() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MAX_VALUE
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)).getNumberOfSuccesses();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_037() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 0, Integer.MIN_VALUE)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_038() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(0, 1, 1)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_039() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(1, 1, 0)).getNumberOfSuccesses();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_040() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(-1, 1, Integer.MAX_VALUE)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_041() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, 1, -1)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_042() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 1, 0)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_043() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=-1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(0, -1, -1)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_044() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(1, -1, Integer.MAX_VALUE)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_045() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(-1, -1, 0)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_046() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, -1, 1)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_047() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, -1, 1)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_048() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MAX_VALUE, Integer.MAX_VALUE)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_049() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(1, Integer.MAX_VALUE, -1)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_050() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MAX_VALUE, 1)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_051() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, 0)).getNumberOfSuccesses();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_052() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MAX_VALUE, -1)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_053() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MIN_VALUE, Integer.MIN_VALUE)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_054() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(1, Integer.MIN_VALUE, 0)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_055() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MIN_VALUE, 1)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_056() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MIN_VALUE, -1)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_057() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_058() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(1, 1, Integer.MIN_VALUE)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_059() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(-1, -1, Integer.MIN_VALUE)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumberOfSuccesses_pairwise_060() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE)).getNumberOfSuccesses();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_061() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=0, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(0, 0, 0)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_062() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=0, receiver__sampleSize=1
        Object actual = (new HypergeometricDistribution(1, 0, 1)).getPopulationSize();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_063() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=0, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(-1, 0, -1)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_064() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MAX_VALUE
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)).getPopulationSize();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_065() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 0, Integer.MIN_VALUE)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_066() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(0, 1, 1)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_067() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(1, 1, 0)).getPopulationSize();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_068() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(-1, 1, Integer.MAX_VALUE)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_069() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, 1, -1)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_070() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 1, 0)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_071() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=-1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(0, -1, -1)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_072() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(1, -1, Integer.MAX_VALUE)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_073() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(-1, -1, 0)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_074() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, -1, 1)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_075() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, -1, 1)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_076() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MAX_VALUE, Integer.MAX_VALUE)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_077() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(1, Integer.MAX_VALUE, -1)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_078() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MAX_VALUE, 1)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_079() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, 0)).getPopulationSize();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_080() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MAX_VALUE, -1)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_081() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MIN_VALUE, Integer.MIN_VALUE)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_082() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(1, Integer.MIN_VALUE, 0)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_083() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MIN_VALUE, 1)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_084() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MIN_VALUE, -1)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_085() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_086() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(1, 1, Integer.MIN_VALUE)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_087() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(-1, -1, Integer.MIN_VALUE)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPopulationSize_pairwise_088() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE)).getPopulationSize();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_089() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=0, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(0, 0, 0)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_090() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=0, receiver__sampleSize=1
        Object actual = (new HypergeometricDistribution(1, 0, 1)).getSampleSize();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_091() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=0, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(-1, 0, -1)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_092() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MAX_VALUE
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)).getSampleSize();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_093() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 0, Integer.MIN_VALUE)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_094() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(0, 1, 1)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_095() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(1, 1, 0)).getSampleSize();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_096() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(-1, 1, Integer.MAX_VALUE)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_097() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, 1, -1)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_098() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 1, 0)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_099() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=-1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(0, -1, -1)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_100() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(1, -1, Integer.MAX_VALUE)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_101() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(-1, -1, 0)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_102() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, -1, 1)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_103() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, -1, 1)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_104() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MAX_VALUE, Integer.MAX_VALUE)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_105() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(1, Integer.MAX_VALUE, -1)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_106() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MAX_VALUE, 1)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_107() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, 0)).getSampleSize();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_108() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MAX_VALUE, -1)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_109() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MIN_VALUE, Integer.MIN_VALUE)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_110() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(1, Integer.MIN_VALUE, 0)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_111() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MIN_VALUE, 1)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_112() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MIN_VALUE, -1)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_113() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_114() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(1, 1, Integer.MIN_VALUE)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_115() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(-1, -1, Integer.MIN_VALUE)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSampleSize_pairwise_116() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE)).getSampleSize();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_117() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=0, receiver__sampleSize=0, x=0
        try {
            (new HypergeometricDistribution(0, 0, 0)).probability(0);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_118() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=0, receiver__sampleSize=1, x=1
        Object actual = (new HypergeometricDistribution(1, 0, 1)).probability(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_119() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=0, receiver__sampleSize=-1, x=-1
        try {
            (new HypergeometricDistribution(-1, 0, -1)).probability(-1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_120() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MAX_VALUE, x=Integer.MAX_VALUE
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)).probability(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_121() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MIN_VALUE, x=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 0, Integer.MIN_VALUE)).probability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_122() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=1, receiver__sampleSize=1, x=-1
        try {
            (new HypergeometricDistribution(0, 1, 1)).probability(-1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_123() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=0, x=Integer.MAX_VALUE
        Object actual = (new HypergeometricDistribution(1, 1, 0)).probability(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_124() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MAX_VALUE, x=0
        try {
            (new HypergeometricDistribution(-1, 1, Integer.MAX_VALUE)).probability(0);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_125() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=-1, x=1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, 1, -1)).probability(1);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_126() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=0, x=1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 1, 0)).probability(1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_127() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=-1, receiver__sampleSize=-1, x=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(0, -1, -1)).probability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_128() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MAX_VALUE, x=-1
        try {
            (new HypergeometricDistribution(1, -1, Integer.MAX_VALUE)).probability(-1);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_129() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=0, x=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(-1, -1, 0)).probability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_130() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1, x=0
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, -1, 1)).probability(0);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_131() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1, x=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, -1, 1)).probability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_132() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MAX_VALUE, x=1
        try {
            (new HypergeometricDistribution(0, Integer.MAX_VALUE, Integer.MAX_VALUE)).probability(1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_133() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1, x=0
        try {
            (new HypergeometricDistribution(1, Integer.MAX_VALUE, -1)).probability(0);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_134() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=1, x=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(-1, Integer.MAX_VALUE, 1)).probability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_135() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=0, x=-1
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, 0)).probability(-1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_136() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1, x=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MAX_VALUE, -1)).probability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_137() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MIN_VALUE, x=0
        try {
            (new HypergeometricDistribution(0, Integer.MIN_VALUE, Integer.MIN_VALUE)).probability(0);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_138() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=0, x=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(1, Integer.MIN_VALUE, 0)).probability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_139() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=1, x=1
        try {
            (new HypergeometricDistribution(-1, Integer.MIN_VALUE, 1)).probability(1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_140() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=-1, x=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MIN_VALUE, -1)).probability(-1);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_141() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MAX_VALUE, x=0
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)).probability(0);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_142() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MIN_VALUE, x=1
        try {
            (new HypergeometricDistribution(1, 1, Integer.MIN_VALUE)).probability(1);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_143() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MIN_VALUE, x=1
        try {
            (new HypergeometricDistribution(-1, -1, Integer.MIN_VALUE)).probability(1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_144() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MIN_VALUE, x=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE)).probability(-1);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_145() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=0, x=-1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 0, 0)).probability(-1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_146() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MIN_VALUE, x=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MIN_VALUE, Integer.MIN_VALUE)).probability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_147() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=1, receiver__sampleSize=1, x=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(0, 1, 1)).probability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_148() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MAX_VALUE, x=Integer.MIN_VALUE
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)).probability(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_149() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=0, receiver__sampleSize=0, x=0
        try {
            (new HypergeometricDistribution(0, 0, 0)).upperCumulativeProbability(0);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_150() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=0, receiver__sampleSize=1, x=1
        Object actual = (new HypergeometricDistribution(1, 0, 1)).upperCumulativeProbability(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_151() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=0, receiver__sampleSize=-1, x=-1
        try {
            (new HypergeometricDistribution(-1, 0, -1)).upperCumulativeProbability(-1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_152() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MAX_VALUE, x=Integer.MAX_VALUE
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)).upperCumulativeProbability(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_153() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MIN_VALUE, x=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 0, Integer.MIN_VALUE)).upperCumulativeProbability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_154() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=1, receiver__sampleSize=1, x=-1
        try {
            (new HypergeometricDistribution(0, 1, 1)).upperCumulativeProbability(-1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_155() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=0, x=Integer.MAX_VALUE
        Object actual = (new HypergeometricDistribution(1, 1, 0)).upperCumulativeProbability(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_156() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MAX_VALUE, x=0
        try {
            (new HypergeometricDistribution(-1, 1, Integer.MAX_VALUE)).upperCumulativeProbability(0);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_157() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=-1, x=1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, 1, -1)).upperCumulativeProbability(1);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_158() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=0, x=1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 1, 0)).upperCumulativeProbability(1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_159() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=-1, receiver__sampleSize=-1, x=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(0, -1, -1)).upperCumulativeProbability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_160() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MAX_VALUE, x=-1
        try {
            (new HypergeometricDistribution(1, -1, Integer.MAX_VALUE)).upperCumulativeProbability(-1);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_161() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=0, x=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(-1, -1, 0)).upperCumulativeProbability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_162() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1, x=0
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, -1, 1)).upperCumulativeProbability(0);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_163() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1, x=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, -1, 1)).upperCumulativeProbability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_164() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MAX_VALUE, x=1
        try {
            (new HypergeometricDistribution(0, Integer.MAX_VALUE, Integer.MAX_VALUE)).upperCumulativeProbability(1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_165() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1, x=0
        try {
            (new HypergeometricDistribution(1, Integer.MAX_VALUE, -1)).upperCumulativeProbability(0);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_166() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=1, x=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(-1, Integer.MAX_VALUE, 1)).upperCumulativeProbability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_167() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=0, x=-1
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, 0)).upperCumulativeProbability(-1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_168() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1, x=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MAX_VALUE, -1)).upperCumulativeProbability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_169() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MIN_VALUE, x=0
        try {
            (new HypergeometricDistribution(0, Integer.MIN_VALUE, Integer.MIN_VALUE)).upperCumulativeProbability(0);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_170() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=0, x=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(1, Integer.MIN_VALUE, 0)).upperCumulativeProbability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_171() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=1, x=1
        try {
            (new HypergeometricDistribution(-1, Integer.MIN_VALUE, 1)).upperCumulativeProbability(1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_172() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=-1, x=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MIN_VALUE, -1)).upperCumulativeProbability(-1);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_173() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MAX_VALUE, x=0
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)).upperCumulativeProbability(0);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_174() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MIN_VALUE, x=1
        try {
            (new HypergeometricDistribution(1, 1, Integer.MIN_VALUE)).upperCumulativeProbability(1);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_175() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MIN_VALUE, x=1
        try {
            (new HypergeometricDistribution(-1, -1, Integer.MIN_VALUE)).upperCumulativeProbability(1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_176() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MIN_VALUE, x=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE)).upperCumulativeProbability(-1);
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_177() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=0, x=-1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 0, 0)).upperCumulativeProbability(-1);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_178() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MIN_VALUE, x=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MIN_VALUE, Integer.MIN_VALUE)).upperCumulativeProbability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_179() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=1, receiver__sampleSize=1, x=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(0, 1, 1)).upperCumulativeProbability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_upperCumulativeProbability_pairwise_180() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MAX_VALUE, x=Integer.MIN_VALUE
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)).upperCumulativeProbability(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_181() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=0, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(0, 0, 0)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_182() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=0, receiver__sampleSize=1
        Object actual = (new HypergeometricDistribution(1, 0, 1)).getNumericalMean();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_183() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=0, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(-1, 0, -1)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_184() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MAX_VALUE
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)).getNumericalMean();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_185() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 0, Integer.MIN_VALUE)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_186() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(0, 1, 1)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_187() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(1, 1, 0)).getNumericalMean();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_188() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(-1, 1, Integer.MAX_VALUE)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_189() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, 1, -1)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_190() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 1, 0)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_191() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=-1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(0, -1, -1)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_192() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(1, -1, Integer.MAX_VALUE)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_193() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(-1, -1, 0)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_194() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, -1, 1)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_195() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, -1, 1)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_196() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MAX_VALUE, Integer.MAX_VALUE)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_197() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(1, Integer.MAX_VALUE, -1)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_198() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MAX_VALUE, 1)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_199() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, 0)).getNumericalMean();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_200() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MAX_VALUE, -1)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_201() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MIN_VALUE, Integer.MIN_VALUE)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_202() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(1, Integer.MIN_VALUE, 0)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_203() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MIN_VALUE, 1)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_204() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MIN_VALUE, -1)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_205() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_206() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(1, 1, Integer.MIN_VALUE)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_207() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(-1, -1, Integer.MIN_VALUE)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_208() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_209() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=0, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(0, 0, 0)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_210() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=0, receiver__sampleSize=1
        Object actual = (new HypergeometricDistribution(1, 0, 1)).getNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_211() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=0, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(-1, 0, -1)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_212() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MAX_VALUE
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)).getNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_213() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 0, Integer.MIN_VALUE)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_214() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(0, 1, 1)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_215() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(1, 1, 0)).getNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_216() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(-1, 1, Integer.MAX_VALUE)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_217() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, 1, -1)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_218() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 1, 0)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_219() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=-1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(0, -1, -1)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_220() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(1, -1, Integer.MAX_VALUE)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_221() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(-1, -1, 0)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_222() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, -1, 1)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_223() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, -1, 1)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_224() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MAX_VALUE, Integer.MAX_VALUE)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_225() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(1, Integer.MAX_VALUE, -1)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_226() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MAX_VALUE, 1)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_227() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, 0)).getNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_228() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MAX_VALUE, -1)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_229() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MIN_VALUE, Integer.MIN_VALUE)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_230() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(1, Integer.MIN_VALUE, 0)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_231() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MIN_VALUE, 1)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_232() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MIN_VALUE, -1)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_233() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_234() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(1, 1, Integer.MIN_VALUE)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_235() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(-1, -1, Integer.MIN_VALUE)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_236() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_237() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=0, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(0, 0, 0)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_238() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=0, receiver__sampleSize=1
        Object actual = (new HypergeometricDistribution(1, 0, 1)).calculateNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_239() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=0, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(-1, 0, -1)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_240() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MAX_VALUE
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)).calculateNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_241() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 0, Integer.MIN_VALUE)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_242() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(0, 1, 1)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_243() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(1, 1, 0)).calculateNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_244() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(-1, 1, Integer.MAX_VALUE)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_245() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, 1, -1)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_246() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 1, 0)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_247() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=-1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(0, -1, -1)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_248() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(1, -1, Integer.MAX_VALUE)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_249() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(-1, -1, 0)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_250() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, -1, 1)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_251() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, -1, 1)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_252() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MAX_VALUE, Integer.MAX_VALUE)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_253() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(1, Integer.MAX_VALUE, -1)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_254() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MAX_VALUE, 1)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_255() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, 0)).calculateNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_256() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MAX_VALUE, -1)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_257() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MIN_VALUE, Integer.MIN_VALUE)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_258() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(1, Integer.MIN_VALUE, 0)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_259() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MIN_VALUE, 1)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_260() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MIN_VALUE, -1)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_261() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_262() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(1, 1, Integer.MIN_VALUE)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_263() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(-1, -1, Integer.MIN_VALUE)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_264() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_265() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=0, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(0, 0, 0)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_266() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=0, receiver__sampleSize=1
        Object actual = (new HypergeometricDistribution(1, 0, 1)).getSupportLowerBound();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_267() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=0, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(-1, 0, -1)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_268() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MAX_VALUE
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)).getSupportLowerBound();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_269() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 0, Integer.MIN_VALUE)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_270() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(0, 1, 1)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_271() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(1, 1, 0)).getSupportLowerBound();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_272() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(-1, 1, Integer.MAX_VALUE)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_273() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, 1, -1)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_274() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 1, 0)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_275() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=-1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(0, -1, -1)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_276() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(1, -1, Integer.MAX_VALUE)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_277() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(-1, -1, 0)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_278() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, -1, 1)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_279() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, -1, 1)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_280() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MAX_VALUE, Integer.MAX_VALUE)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_281() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(1, Integer.MAX_VALUE, -1)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_282() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MAX_VALUE, 1)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_283() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, 0)).getSupportLowerBound();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_284() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MAX_VALUE, -1)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_285() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MIN_VALUE, Integer.MIN_VALUE)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_286() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(1, Integer.MIN_VALUE, 0)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_287() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MIN_VALUE, 1)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_288() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MIN_VALUE, -1)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_289() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_290() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(1, 1, Integer.MIN_VALUE)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_291() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(-1, -1, Integer.MIN_VALUE)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_292() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_293() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=0, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(0, 0, 0)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_294() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=0, receiver__sampleSize=1
        Object actual = (new HypergeometricDistribution(1, 0, 1)).getSupportUpperBound();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_295() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=0, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(-1, 0, -1)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_296() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MAX_VALUE
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)).getSupportUpperBound();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_297() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 0, Integer.MIN_VALUE)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_298() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(0, 1, 1)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_299() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(1, 1, 0)).getSupportUpperBound();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_300() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(-1, 1, Integer.MAX_VALUE)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_301() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, 1, -1)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_302() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 1, 0)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_303() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=-1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(0, -1, -1)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_304() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(1, -1, Integer.MAX_VALUE)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_305() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(-1, -1, 0)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_306() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, -1, 1)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_307() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, -1, 1)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_308() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MAX_VALUE, Integer.MAX_VALUE)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_309() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(1, Integer.MAX_VALUE, -1)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_310() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MAX_VALUE, 1)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_311() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, 0)).getSupportUpperBound();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_312() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MAX_VALUE, -1)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_313() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MIN_VALUE, Integer.MIN_VALUE)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_314() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(1, Integer.MIN_VALUE, 0)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_315() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MIN_VALUE, 1)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_316() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MIN_VALUE, -1)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_317() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_318() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(1, 1, Integer.MIN_VALUE)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_319() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(-1, -1, Integer.MIN_VALUE)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_320() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_321() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=0, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(0, 0, 0)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_322() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=0, receiver__sampleSize=1
        Object actual = (new HypergeometricDistribution(1, 0, 1)).isSupportConnected();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_323() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=0, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(-1, 0, -1)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_324() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MAX_VALUE
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, 0, Integer.MAX_VALUE)).isSupportConnected();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_325() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=0, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 0, Integer.MIN_VALUE)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_326() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(0, 1, 1)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_327() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(1, 1, 0)).isSupportConnected();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_328() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(-1, 1, Integer.MAX_VALUE)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_329() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, 1, -1)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_330() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, 1, 0)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_331() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=-1, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(0, -1, -1)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_332() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(1, -1, Integer.MAX_VALUE)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_333() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(-1, -1, 0)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_334() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, -1, 1)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_335() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=-1, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, -1, 1)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_336() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MAX_VALUE, Integer.MAX_VALUE)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_337() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(1, Integer.MAX_VALUE, -1)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_338() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MAX_VALUE, 1)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_339() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=0
        Object actual = (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, 0)).isSupportConnected();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_340() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MAX_VALUE, -1)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_341() throws Exception {
        // Combination: receiver__populationSize=0, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(0, Integer.MIN_VALUE, Integer.MIN_VALUE)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_342() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=0
        try {
            (new HypergeometricDistribution(1, Integer.MIN_VALUE, 0)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_343() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=1
        try {
            (new HypergeometricDistribution(-1, Integer.MIN_VALUE, 1)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_344() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=-1
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MIN_VALUE, -1)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_345() throws Exception {
        // Combination: receiver__populationSize=Integer.MIN_VALUE, receiver__numberOfSuccesses=Integer.MIN_VALUE, receiver__sampleSize=Integer.MAX_VALUE
        try {
            (new HypergeometricDistribution(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MAX_VALUE)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_346() throws Exception {
        // Combination: receiver__populationSize=1, receiver__numberOfSuccesses=1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(1, 1, Integer.MIN_VALUE)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_347() throws Exception {
        // Combination: receiver__populationSize=-1, receiver__numberOfSuccesses=-1, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(-1, -1, Integer.MIN_VALUE)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_348() throws Exception {
        // Combination: receiver__populationSize=Integer.MAX_VALUE, receiver__numberOfSuccesses=Integer.MAX_VALUE, receiver__sampleSize=Integer.MIN_VALUE
        try {
            (new HypergeometricDistribution(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
