package org.apache.commons.math3.distribution;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for FDistribution.
 */
public class FDistribution_IPOTest {
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
    public void test_density_pairwise_001() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d, x=0.0d
        try {
            (new FDistribution(0.0d, 0.0d)).density(0.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_002() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d, x=1.0d
        try {
            (new FDistribution(1.0d, 0.0d)).density(1.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_003() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d, x=-1.0d
        try {
            (new FDistribution(-1.0d, 0.0d)).density(-1.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_004() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d, x=Double.NaN
        try {
            (new FDistribution(Double.NaN, 0.0d)).density(Double.NaN);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_005() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d, x=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, 0.0d)).density(Double.POSITIVE_INFINITY);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_006() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d, x=1.0d
        try {
            (new FDistribution(0.0d, 1.0d)).density(1.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_007() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d, x=0.0d
        Object actual = (new FDistribution(1.0d, 1.0d)).density(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_008() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d, x=Double.NaN
        try {
            (new FDistribution(-1.0d, 1.0d)).density(Double.NaN);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_009() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d, x=-1.0d
        Object actual = (new FDistribution(Double.NaN, 1.0d)).density(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_010() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d, x=0.0d
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, 1.0d)).density(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_011() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d, x=-1.0d
        try {
            (new FDistribution(0.0d, -1.0d)).density(-1.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_012() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, x=Double.NaN
        try {
            (new FDistribution(1.0d, -1.0d)).density(Double.NaN);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_013() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, x=0.0d
        try {
            (new FDistribution(-1.0d, -1.0d)).density(0.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_014() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d, x=1.0d
        try {
            (new FDistribution(Double.NaN, -1.0d)).density(1.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_015() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d, x=1.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, -1.0d)).density(1.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_016() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, x=Double.NaN
        try {
            (new FDistribution(0.0d, Double.NaN)).density(Double.NaN);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_017() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, x=-1.0d
        Object actual = (new FDistribution(1.0d, Double.NaN)).density(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_018() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, x=1.0d
        try {
            (new FDistribution(-1.0d, Double.NaN)).density(1.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_019() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN, x=0.0d
        Object actual = (new FDistribution(Double.NaN, Double.NaN)).density(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_020() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN, x=-1.0d
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.NaN)).density(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_021() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, x=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(0.0d, Double.POSITIVE_INFINITY)).density(Double.POSITIVE_INFINITY);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_022() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, x=0.0d
        Object actual = (new FDistribution(1.0d, Double.POSITIVE_INFINITY)).density(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_023() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, x=1.0d
        try {
            (new FDistribution(-1.0d, Double.POSITIVE_INFINITY)).density(1.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_024() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, x=-1.0d
        Object actual = (new FDistribution(Double.NaN, Double.POSITIVE_INFINITY)).density(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_025() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, x=Double.NaN
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).density(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_026() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d, x=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(1.0d, 1.0d)).density(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_027() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, x=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(-1.0d, -1.0d)).density(Double.POSITIVE_INFINITY);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_028() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN, x=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.NaN, Double.NaN)).density(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_029() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d, x=0.0d
        try {
            (new FDistribution(0.0d, 0.0d)).cumulativeProbability(0.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_030() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d, x=1.0d
        try {
            (new FDistribution(1.0d, 0.0d)).cumulativeProbability(1.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_031() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d, x=-1.0d
        try {
            (new FDistribution(-1.0d, 0.0d)).cumulativeProbability(-1.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_032() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d, x=Double.NaN
        try {
            (new FDistribution(Double.NaN, 0.0d)).cumulativeProbability(Double.NaN);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_033() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d, x=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, 0.0d)).cumulativeProbability(Double.POSITIVE_INFINITY);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_034() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d, x=1.0d
        try {
            (new FDistribution(0.0d, 1.0d)).cumulativeProbability(1.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_035() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d, x=0.0d
        Object actual = (new FDistribution(1.0d, 1.0d)).cumulativeProbability(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_036() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d, x=Double.NaN
        try {
            (new FDistribution(-1.0d, 1.0d)).cumulativeProbability(Double.NaN);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_037() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d, x=-1.0d
        Object actual = (new FDistribution(Double.NaN, 1.0d)).cumulativeProbability(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_038() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d, x=0.0d
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, 1.0d)).cumulativeProbability(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_039() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d, x=-1.0d
        try {
            (new FDistribution(0.0d, -1.0d)).cumulativeProbability(-1.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_040() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, x=Double.NaN
        try {
            (new FDistribution(1.0d, -1.0d)).cumulativeProbability(Double.NaN);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_041() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, x=0.0d
        try {
            (new FDistribution(-1.0d, -1.0d)).cumulativeProbability(0.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_042() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d, x=1.0d
        try {
            (new FDistribution(Double.NaN, -1.0d)).cumulativeProbability(1.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_043() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d, x=1.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, -1.0d)).cumulativeProbability(1.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_044() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, x=Double.NaN
        try {
            (new FDistribution(0.0d, Double.NaN)).cumulativeProbability(Double.NaN);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_045() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, x=-1.0d
        Object actual = (new FDistribution(1.0d, Double.NaN)).cumulativeProbability(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_046() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, x=1.0d
        try {
            (new FDistribution(-1.0d, Double.NaN)).cumulativeProbability(1.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_047() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN, x=0.0d
        Object actual = (new FDistribution(Double.NaN, Double.NaN)).cumulativeProbability(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_048() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN, x=-1.0d
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.NaN)).cumulativeProbability(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_049() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, x=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(0.0d, Double.POSITIVE_INFINITY)).cumulativeProbability(Double.POSITIVE_INFINITY);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_050() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, x=0.0d
        Object actual = (new FDistribution(1.0d, Double.POSITIVE_INFINITY)).cumulativeProbability(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_051() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, x=1.0d
        try {
            (new FDistribution(-1.0d, Double.POSITIVE_INFINITY)).cumulativeProbability(1.0d);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_052() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, x=-1.0d
        Object actual = (new FDistribution(Double.NaN, Double.POSITIVE_INFINITY)).cumulativeProbability(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_053() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, x=Double.NaN
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).cumulativeProbability(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_054() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d, x=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(1.0d, 1.0d)).cumulativeProbability(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_055() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, x=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(-1.0d, -1.0d)).cumulativeProbability(Double.POSITIVE_INFINITY);
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_056() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN, x=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.NaN, Double.NaN)).cumulativeProbability(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_057() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(0.0d, 0.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_058() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(1.0d, 0.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_059() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(-1.0d, 0.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_060() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.NaN, 0.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_061() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, 0.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_062() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(0.0d, 1.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_063() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(1.0d, 1.0d)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_064() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(-1.0d, 1.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_065() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.NaN, 1.0d)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_066() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, 1.0d)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_067() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(0.0d, -1.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_068() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(1.0d, -1.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_069() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(-1.0d, -1.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_070() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.NaN, -1.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_071() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, -1.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_072() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(0.0d, Double.NaN)).getNumeratorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_073() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(1.0d, Double.NaN)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_074() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(-1.0d, Double.NaN)).getNumeratorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_075() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.NaN, Double.NaN)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_076() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.NaN)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_077() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(0.0d, Double.POSITIVE_INFINITY)).getNumeratorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_078() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(1.0d, Double.POSITIVE_INFINITY)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_079() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(-1.0d, Double.POSITIVE_INFINITY)).getNumeratorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_080() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.NaN, Double.POSITIVE_INFINITY)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_081() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_082() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(0.0d, 0.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_083() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(1.0d, 0.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_084() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(-1.0d, 0.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_085() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.NaN, 0.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_086() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, 0.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_087() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(0.0d, 1.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_088() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(1.0d, 1.0d)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_089() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(-1.0d, 1.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_090() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.NaN, 1.0d)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_091() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, 1.0d)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_092() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(0.0d, -1.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_093() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(1.0d, -1.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_094() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(-1.0d, -1.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_095() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.NaN, -1.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_096() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, -1.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_097() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(0.0d, Double.NaN)).getDenominatorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_098() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(1.0d, Double.NaN)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_099() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(-1.0d, Double.NaN)).getDenominatorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_100() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.NaN, Double.NaN)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_101() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.NaN)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_102() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(0.0d, Double.POSITIVE_INFINITY)).getDenominatorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_103() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(1.0d, Double.POSITIVE_INFINITY)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_104() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(-1.0d, Double.POSITIVE_INFINITY)).getDenominatorDegreesOfFreedom();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_105() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.NaN, Double.POSITIVE_INFINITY)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_106() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_107() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(0.0d, 0.0d)).getSolverAbsoluteAccuracy();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_108() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(1.0d, 0.0d)).getSolverAbsoluteAccuracy();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_109() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(-1.0d, 0.0d)).getSolverAbsoluteAccuracy();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_110() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.NaN, 0.0d)).getSolverAbsoluteAccuracy();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_111() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, 0.0d)).getSolverAbsoluteAccuracy();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_112() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(0.0d, 1.0d)).getSolverAbsoluteAccuracy();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_113() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(1.0d, 1.0d)).getSolverAbsoluteAccuracy();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0E-9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_114() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(-1.0d, 1.0d)).getSolverAbsoluteAccuracy();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_115() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.NaN, 1.0d)).getSolverAbsoluteAccuracy();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0E-9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_116() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, 1.0d)).getSolverAbsoluteAccuracy();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0E-9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_117() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(0.0d, -1.0d)).getSolverAbsoluteAccuracy();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_118() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(1.0d, -1.0d)).getSolverAbsoluteAccuracy();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_119() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(-1.0d, -1.0d)).getSolverAbsoluteAccuracy();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_120() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.NaN, -1.0d)).getSolverAbsoluteAccuracy();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_121() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, -1.0d)).getSolverAbsoluteAccuracy();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_122() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(0.0d, Double.NaN)).getSolverAbsoluteAccuracy();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_123() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(1.0d, Double.NaN)).getSolverAbsoluteAccuracy();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0E-9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_124() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(-1.0d, Double.NaN)).getSolverAbsoluteAccuracy();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_125() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.NaN, Double.NaN)).getSolverAbsoluteAccuracy();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0E-9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_126() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.NaN)).getSolverAbsoluteAccuracy();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0E-9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_127() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(0.0d, Double.POSITIVE_INFINITY)).getSolverAbsoluteAccuracy();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_128() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(1.0d, Double.POSITIVE_INFINITY)).getSolverAbsoluteAccuracy();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0E-9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_129() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(-1.0d, Double.POSITIVE_INFINITY)).getSolverAbsoluteAccuracy();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_130() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.NaN, Double.POSITIVE_INFINITY)).getSolverAbsoluteAccuracy();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0E-9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSolverAbsoluteAccuracy_pairwise_131() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getSolverAbsoluteAccuracy();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0E-9", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_132() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(0.0d, 0.0d)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_133() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(1.0d, 0.0d)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_134() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(-1.0d, 0.0d)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_135() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.NaN, 0.0d)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_136() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, 0.0d)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_137() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(0.0d, 1.0d)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_138() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(1.0d, 1.0d)).getNumericalMean();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_139() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(-1.0d, 1.0d)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_140() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.NaN, 1.0d)).getNumericalMean();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_141() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, 1.0d)).getNumericalMean();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_142() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(0.0d, -1.0d)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_143() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(1.0d, -1.0d)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_144() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(-1.0d, -1.0d)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_145() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.NaN, -1.0d)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_146() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, -1.0d)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_147() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(0.0d, Double.NaN)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_148() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(1.0d, Double.NaN)).getNumericalMean();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_149() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(-1.0d, Double.NaN)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_150() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.NaN, Double.NaN)).getNumericalMean();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_151() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.NaN)).getNumericalMean();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_152() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(0.0d, Double.POSITIVE_INFINITY)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_153() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(1.0d, Double.POSITIVE_INFINITY)).getNumericalMean();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_154() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(-1.0d, Double.POSITIVE_INFINITY)).getNumericalMean();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_155() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.NaN, Double.POSITIVE_INFINITY)).getNumericalMean();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalMean_pairwise_156() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getNumericalMean();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_157() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(0.0d, 0.0d)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_158() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(1.0d, 0.0d)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_159() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(-1.0d, 0.0d)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_160() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.NaN, 0.0d)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_161() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, 0.0d)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_162() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(0.0d, 1.0d)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_163() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(1.0d, 1.0d)).getNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_164() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(-1.0d, 1.0d)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_165() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.NaN, 1.0d)).getNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_166() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, 1.0d)).getNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_167() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(0.0d, -1.0d)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_168() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(1.0d, -1.0d)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_169() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(-1.0d, -1.0d)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_170() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.NaN, -1.0d)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_171() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, -1.0d)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_172() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(0.0d, Double.NaN)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_173() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(1.0d, Double.NaN)).getNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_174() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(-1.0d, Double.NaN)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_175() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.NaN, Double.NaN)).getNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_176() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.NaN)).getNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_177() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(0.0d, Double.POSITIVE_INFINITY)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_178() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(1.0d, Double.POSITIVE_INFINITY)).getNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_179() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(-1.0d, Double.POSITIVE_INFINITY)).getNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_180() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.NaN, Double.POSITIVE_INFINITY)).getNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumericalVariance_pairwise_181() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_182() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(0.0d, 0.0d)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_183() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(1.0d, 0.0d)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_184() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(-1.0d, 0.0d)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_185() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.NaN, 0.0d)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_186() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, 0.0d)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_187() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(0.0d, 1.0d)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_188() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(1.0d, 1.0d)).calculateNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_189() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(-1.0d, 1.0d)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_190() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.NaN, 1.0d)).calculateNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_191() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, 1.0d)).calculateNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_192() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(0.0d, -1.0d)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_193() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(1.0d, -1.0d)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_194() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(-1.0d, -1.0d)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_195() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.NaN, -1.0d)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_196() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, -1.0d)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_197() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(0.0d, Double.NaN)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_198() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(1.0d, Double.NaN)).calculateNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_199() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(-1.0d, Double.NaN)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_200() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.NaN, Double.NaN)).calculateNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_201() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.NaN)).calculateNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_202() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(0.0d, Double.POSITIVE_INFINITY)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_203() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(1.0d, Double.POSITIVE_INFINITY)).calculateNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_204() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(-1.0d, Double.POSITIVE_INFINITY)).calculateNumericalVariance();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_205() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.NaN, Double.POSITIVE_INFINITY)).calculateNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_calculateNumericalVariance_pairwise_206() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).calculateNumericalVariance();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_207() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(0.0d, 0.0d)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_208() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(1.0d, 0.0d)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_209() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(-1.0d, 0.0d)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_210() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.NaN, 0.0d)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_211() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, 0.0d)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_212() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(0.0d, 1.0d)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_213() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(1.0d, 1.0d)).getSupportLowerBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_214() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(-1.0d, 1.0d)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_215() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.NaN, 1.0d)).getSupportLowerBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_216() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, 1.0d)).getSupportLowerBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_217() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(0.0d, -1.0d)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_218() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(1.0d, -1.0d)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_219() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(-1.0d, -1.0d)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_220() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.NaN, -1.0d)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_221() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, -1.0d)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_222() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(0.0d, Double.NaN)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_223() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(1.0d, Double.NaN)).getSupportLowerBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_224() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(-1.0d, Double.NaN)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_225() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.NaN, Double.NaN)).getSupportLowerBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_226() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.NaN)).getSupportLowerBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_227() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(0.0d, Double.POSITIVE_INFINITY)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_228() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(1.0d, Double.POSITIVE_INFINITY)).getSupportLowerBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_229() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(-1.0d, Double.POSITIVE_INFINITY)).getSupportLowerBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_230() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.NaN, Double.POSITIVE_INFINITY)).getSupportLowerBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportLowerBound_pairwise_231() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getSupportLowerBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_232() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(0.0d, 0.0d)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_233() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(1.0d, 0.0d)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_234() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(-1.0d, 0.0d)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_235() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.NaN, 0.0d)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_236() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, 0.0d)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_237() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(0.0d, 1.0d)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_238() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(1.0d, 1.0d)).getSupportUpperBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_239() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(-1.0d, 1.0d)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_240() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.NaN, 1.0d)).getSupportUpperBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_241() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, 1.0d)).getSupportUpperBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_242() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(0.0d, -1.0d)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_243() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(1.0d, -1.0d)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_244() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(-1.0d, -1.0d)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_245() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.NaN, -1.0d)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_246() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, -1.0d)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_247() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(0.0d, Double.NaN)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_248() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(1.0d, Double.NaN)).getSupportUpperBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_249() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(-1.0d, Double.NaN)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_250() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.NaN, Double.NaN)).getSupportUpperBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_251() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.NaN)).getSupportUpperBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_252() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(0.0d, Double.POSITIVE_INFINITY)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_253() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(1.0d, Double.POSITIVE_INFINITY)).getSupportUpperBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_254() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(-1.0d, Double.POSITIVE_INFINITY)).getSupportUpperBound();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_255() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.NaN, Double.POSITIVE_INFINITY)).getSupportUpperBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getSupportUpperBound_pairwise_256() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getSupportUpperBound();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_257() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(0.0d, 0.0d)).isSupportLowerBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_258() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(1.0d, 0.0d)).isSupportLowerBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_259() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(-1.0d, 0.0d)).isSupportLowerBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_260() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.NaN, 0.0d)).isSupportLowerBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_261() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, 0.0d)).isSupportLowerBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_262() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(0.0d, 1.0d)).isSupportLowerBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_263() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(1.0d, 1.0d)).isSupportLowerBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_264() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(-1.0d, 1.0d)).isSupportLowerBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_265() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.NaN, 1.0d)).isSupportLowerBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_266() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, 1.0d)).isSupportLowerBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_267() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(0.0d, -1.0d)).isSupportLowerBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_268() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(1.0d, -1.0d)).isSupportLowerBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_269() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(-1.0d, -1.0d)).isSupportLowerBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_270() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.NaN, -1.0d)).isSupportLowerBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_271() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, -1.0d)).isSupportLowerBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_272() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(0.0d, Double.NaN)).isSupportLowerBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_273() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(1.0d, Double.NaN)).isSupportLowerBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_274() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(-1.0d, Double.NaN)).isSupportLowerBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_275() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.NaN, Double.NaN)).isSupportLowerBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_276() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.NaN)).isSupportLowerBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_277() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(0.0d, Double.POSITIVE_INFINITY)).isSupportLowerBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_278() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(1.0d, Double.POSITIVE_INFINITY)).isSupportLowerBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_279() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(-1.0d, Double.POSITIVE_INFINITY)).isSupportLowerBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_280() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.NaN, Double.POSITIVE_INFINITY)).isSupportLowerBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportLowerBoundInclusive_pairwise_281() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).isSupportLowerBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_282() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(0.0d, 0.0d)).isSupportUpperBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_283() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(1.0d, 0.0d)).isSupportUpperBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_284() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(-1.0d, 0.0d)).isSupportUpperBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_285() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.NaN, 0.0d)).isSupportUpperBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_286() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, 0.0d)).isSupportUpperBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_287() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(0.0d, 1.0d)).isSupportUpperBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_288() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(1.0d, 1.0d)).isSupportUpperBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_289() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(-1.0d, 1.0d)).isSupportUpperBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_290() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.NaN, 1.0d)).isSupportUpperBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_291() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, 1.0d)).isSupportUpperBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_292() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(0.0d, -1.0d)).isSupportUpperBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_293() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(1.0d, -1.0d)).isSupportUpperBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_294() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(-1.0d, -1.0d)).isSupportUpperBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_295() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.NaN, -1.0d)).isSupportUpperBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_296() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, -1.0d)).isSupportUpperBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_297() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(0.0d, Double.NaN)).isSupportUpperBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_298() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(1.0d, Double.NaN)).isSupportUpperBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_299() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(-1.0d, Double.NaN)).isSupportUpperBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_300() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.NaN, Double.NaN)).isSupportUpperBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_301() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.NaN)).isSupportUpperBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_302() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(0.0d, Double.POSITIVE_INFINITY)).isSupportUpperBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_303() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(1.0d, Double.POSITIVE_INFINITY)).isSupportUpperBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_304() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(-1.0d, Double.POSITIVE_INFINITY)).isSupportUpperBoundInclusive();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_305() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.NaN, Double.POSITIVE_INFINITY)).isSupportUpperBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportUpperBoundInclusive_pairwise_306() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).isSupportUpperBoundInclusive();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_307() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(0.0d, 0.0d)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_308() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(1.0d, 0.0d)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_309() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(-1.0d, 0.0d)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_310() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.NaN, 0.0d)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_311() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, 0.0d)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_312() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(0.0d, 1.0d)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_313() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(1.0d, 1.0d)).isSupportConnected();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_314() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistribution(-1.0d, 1.0d)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_315() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.NaN, 1.0d)).isSupportConnected();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_316() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, 1.0d)).isSupportConnected();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_317() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(0.0d, -1.0d)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_318() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(1.0d, -1.0d)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_319() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(-1.0d, -1.0d)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_320() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.NaN, -1.0d)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_321() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistribution(Double.POSITIVE_INFINITY, -1.0d)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_322() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(0.0d, Double.NaN)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_323() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(1.0d, Double.NaN)).isSupportConnected();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_324() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistribution(-1.0d, Double.NaN)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_325() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.NaN, Double.NaN)).isSupportConnected();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_326() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.NaN)).isSupportConnected();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_327() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(0.0d, Double.POSITIVE_INFINITY)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_328() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(1.0d, Double.POSITIVE_INFINITY)).isSupportConnected();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_329() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistribution(-1.0d, Double.POSITIVE_INFINITY)).isSupportConnected();
            fail("Expected org.apache.commons.math3.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math3.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_330() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.NaN, Double.POSITIVE_INFINITY)).isSupportConnected();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_isSupportConnected_pairwise_331() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistribution(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).isSupportConnected();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

}
