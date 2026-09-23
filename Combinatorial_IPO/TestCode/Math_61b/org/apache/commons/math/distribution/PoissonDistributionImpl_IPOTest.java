package org.apache.commons.math.distribution;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for PoissonDistributionImpl.
 */
public class PoissonDistributionImpl_IPOTest {
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
    public void test_probability_pairwise_001() throws Exception {
        // Combination: receiver__p=0.0d, x=0
        try {
            (new PoissonDistributionImpl(0.0d)).probability(0);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_002() throws Exception {
        // Combination: receiver__p=0.0d, x=1
        try {
            (new PoissonDistributionImpl(0.0d)).probability(1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_003() throws Exception {
        // Combination: receiver__p=0.0d, x=-1
        try {
            (new PoissonDistributionImpl(0.0d)).probability(-1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_004() throws Exception {
        // Combination: receiver__p=0.0d, x=Integer.MAX_VALUE
        try {
            (new PoissonDistributionImpl(0.0d)).probability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_005() throws Exception {
        // Combination: receiver__p=0.0d, x=Integer.MIN_VALUE
        try {
            (new PoissonDistributionImpl(0.0d)).probability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_006() throws Exception {
        // Combination: receiver__p=1.0d, x=0
        Object actual = (new PoissonDistributionImpl(1.0d)).probability(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.36787944117144233", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_007() throws Exception {
        // Combination: receiver__p=1.0d, x=1
        Object actual = (new PoissonDistributionImpl(1.0d)).probability(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.36787944117144233", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_008() throws Exception {
        // Combination: receiver__p=1.0d, x=-1
        Object actual = (new PoissonDistributionImpl(1.0d)).probability(-1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_009() throws Exception {
        // Combination: receiver__p=1.0d, x=Integer.MAX_VALUE
        Object actual = (new PoissonDistributionImpl(1.0d)).probability(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_010() throws Exception {
        // Combination: receiver__p=1.0d, x=Integer.MIN_VALUE
        Object actual = (new PoissonDistributionImpl(1.0d)).probability(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_011() throws Exception {
        // Combination: receiver__p=-1.0d, x=0
        try {
            (new PoissonDistributionImpl(-1.0d)).probability(0);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_012() throws Exception {
        // Combination: receiver__p=-1.0d, x=1
        try {
            (new PoissonDistributionImpl(-1.0d)).probability(1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_013() throws Exception {
        // Combination: receiver__p=-1.0d, x=-1
        try {
            (new PoissonDistributionImpl(-1.0d)).probability(-1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_014() throws Exception {
        // Combination: receiver__p=-1.0d, x=Integer.MAX_VALUE
        try {
            (new PoissonDistributionImpl(-1.0d)).probability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_015() throws Exception {
        // Combination: receiver__p=-1.0d, x=Integer.MIN_VALUE
        try {
            (new PoissonDistributionImpl(-1.0d)).probability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_016() throws Exception {
        // Combination: receiver__p=Double.NaN, x=0
        Object actual = (new PoissonDistributionImpl(Double.NaN)).probability(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_017() throws Exception {
        // Combination: receiver__p=Double.NaN, x=1
        Object actual = (new PoissonDistributionImpl(Double.NaN)).probability(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_018() throws Exception {
        // Combination: receiver__p=Double.NaN, x=-1
        Object actual = (new PoissonDistributionImpl(Double.NaN)).probability(-1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_019() throws Exception {
        // Combination: receiver__p=Double.NaN, x=Integer.MAX_VALUE
        Object actual = (new PoissonDistributionImpl(Double.NaN)).probability(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_020() throws Exception {
        // Combination: receiver__p=Double.NaN, x=Integer.MIN_VALUE
        Object actual = (new PoissonDistributionImpl(Double.NaN)).probability(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_021() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, x=0
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).probability(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_022() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, x=1
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).probability(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_023() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, x=-1
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).probability(-1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_024() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, x=Integer.MAX_VALUE
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).probability(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_probability_pairwise_025() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, x=Integer.MIN_VALUE
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).probability(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_026() throws Exception {
        // Combination: receiver__p=0.0d, x=0
        try {
            (new PoissonDistributionImpl(0.0d)).cumulativeProbability(0);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_027() throws Exception {
        // Combination: receiver__p=0.0d, x=1
        try {
            (new PoissonDistributionImpl(0.0d)).cumulativeProbability(1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_028() throws Exception {
        // Combination: receiver__p=0.0d, x=-1
        try {
            (new PoissonDistributionImpl(0.0d)).cumulativeProbability(-1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_029() throws Exception {
        // Combination: receiver__p=0.0d, x=Integer.MAX_VALUE
        try {
            (new PoissonDistributionImpl(0.0d)).cumulativeProbability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_030() throws Exception {
        // Combination: receiver__p=0.0d, x=Integer.MIN_VALUE
        try {
            (new PoissonDistributionImpl(0.0d)).cumulativeProbability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_031() throws Exception {
        // Combination: receiver__p=1.0d, x=0
        Object actual = (new PoissonDistributionImpl(1.0d)).cumulativeProbability(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.36787944117146065", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_032() throws Exception {
        // Combination: receiver__p=1.0d, x=1
        Object actual = (new PoissonDistributionImpl(1.0d)).cumulativeProbability(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.7357588823428858", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_033() throws Exception {
        // Combination: receiver__p=1.0d, x=-1
        Object actual = (new PoissonDistributionImpl(1.0d)).cumulativeProbability(-1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_034() throws Exception {
        // Combination: receiver__p=1.0d, x=Integer.MAX_VALUE
        Object actual = (new PoissonDistributionImpl(1.0d)).cumulativeProbability(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_035() throws Exception {
        // Combination: receiver__p=1.0d, x=Integer.MIN_VALUE
        Object actual = (new PoissonDistributionImpl(1.0d)).cumulativeProbability(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_036() throws Exception {
        // Combination: receiver__p=-1.0d, x=0
        try {
            (new PoissonDistributionImpl(-1.0d)).cumulativeProbability(0);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_037() throws Exception {
        // Combination: receiver__p=-1.0d, x=1
        try {
            (new PoissonDistributionImpl(-1.0d)).cumulativeProbability(1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_038() throws Exception {
        // Combination: receiver__p=-1.0d, x=-1
        try {
            (new PoissonDistributionImpl(-1.0d)).cumulativeProbability(-1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_039() throws Exception {
        // Combination: receiver__p=-1.0d, x=Integer.MAX_VALUE
        try {
            (new PoissonDistributionImpl(-1.0d)).cumulativeProbability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_040() throws Exception {
        // Combination: receiver__p=-1.0d, x=Integer.MIN_VALUE
        try {
            (new PoissonDistributionImpl(-1.0d)).cumulativeProbability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_041() throws Exception {
        // Combination: receiver__p=Double.NaN, x=0
        Object actual = (new PoissonDistributionImpl(Double.NaN)).cumulativeProbability(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_042() throws Exception {
        // Combination: receiver__p=Double.NaN, x=1
        Object actual = (new PoissonDistributionImpl(Double.NaN)).cumulativeProbability(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_043() throws Exception {
        // Combination: receiver__p=Double.NaN, x=-1
        Object actual = (new PoissonDistributionImpl(Double.NaN)).cumulativeProbability(-1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_044() throws Exception {
        // Combination: receiver__p=Double.NaN, x=Integer.MAX_VALUE
        Object actual = (new PoissonDistributionImpl(Double.NaN)).cumulativeProbability(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_045() throws Exception {
        // Combination: receiver__p=Double.NaN, x=Integer.MIN_VALUE
        Object actual = (new PoissonDistributionImpl(Double.NaN)).cumulativeProbability(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_046() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, x=0
        try {
            (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).cumulativeProbability(0);
            fail("Expected org.apache.commons.math.ConvergenceException");
        } catch (org.apache.commons.math.ConvergenceException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_047() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, x=1
        try {
            (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).cumulativeProbability(1);
            fail("Expected org.apache.commons.math.ConvergenceException");
        } catch (org.apache.commons.math.ConvergenceException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_048() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, x=-1
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).cumulativeProbability(-1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_049() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, x=Integer.MAX_VALUE
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).cumulativeProbability(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_050() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, x=Integer.MIN_VALUE
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).cumulativeProbability(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_051() throws Exception {
        // Combination: receiver__p=0.0d, x=0
        try {
            (new PoissonDistributionImpl(0.0d)).normalApproximateProbability(0);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_052() throws Exception {
        // Combination: receiver__p=0.0d, x=1
        try {
            (new PoissonDistributionImpl(0.0d)).normalApproximateProbability(1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_053() throws Exception {
        // Combination: receiver__p=0.0d, x=-1
        try {
            (new PoissonDistributionImpl(0.0d)).normalApproximateProbability(-1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_054() throws Exception {
        // Combination: receiver__p=0.0d, x=Integer.MAX_VALUE
        try {
            (new PoissonDistributionImpl(0.0d)).normalApproximateProbability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_055() throws Exception {
        // Combination: receiver__p=0.0d, x=Integer.MIN_VALUE
        try {
            (new PoissonDistributionImpl(0.0d)).normalApproximateProbability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_056() throws Exception {
        // Combination: receiver__p=1.0d, x=0
        Object actual = (new PoissonDistributionImpl(1.0d)).normalApproximateProbability(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.308537538725987", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_057() throws Exception {
        // Combination: receiver__p=1.0d, x=1
        Object actual = (new PoissonDistributionImpl(1.0d)).normalApproximateProbability(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.691462461274013", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_058() throws Exception {
        // Combination: receiver__p=1.0d, x=-1
        Object actual = (new PoissonDistributionImpl(1.0d)).normalApproximateProbability(-1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.06680720126885803", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_059() throws Exception {
        // Combination: receiver__p=1.0d, x=Integer.MAX_VALUE
        Object actual = (new PoissonDistributionImpl(1.0d)).normalApproximateProbability(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_060() throws Exception {
        // Combination: receiver__p=1.0d, x=Integer.MIN_VALUE
        Object actual = (new PoissonDistributionImpl(1.0d)).normalApproximateProbability(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_061() throws Exception {
        // Combination: receiver__p=-1.0d, x=0
        try {
            (new PoissonDistributionImpl(-1.0d)).normalApproximateProbability(0);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_062() throws Exception {
        // Combination: receiver__p=-1.0d, x=1
        try {
            (new PoissonDistributionImpl(-1.0d)).normalApproximateProbability(1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_063() throws Exception {
        // Combination: receiver__p=-1.0d, x=-1
        try {
            (new PoissonDistributionImpl(-1.0d)).normalApproximateProbability(-1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_064() throws Exception {
        // Combination: receiver__p=-1.0d, x=Integer.MAX_VALUE
        try {
            (new PoissonDistributionImpl(-1.0d)).normalApproximateProbability(Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_065() throws Exception {
        // Combination: receiver__p=-1.0d, x=Integer.MIN_VALUE
        try {
            (new PoissonDistributionImpl(-1.0d)).normalApproximateProbability(Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_066() throws Exception {
        // Combination: receiver__p=Double.NaN, x=0
        Object actual = (new PoissonDistributionImpl(Double.NaN)).normalApproximateProbability(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_067() throws Exception {
        // Combination: receiver__p=Double.NaN, x=1
        Object actual = (new PoissonDistributionImpl(Double.NaN)).normalApproximateProbability(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_068() throws Exception {
        // Combination: receiver__p=Double.NaN, x=-1
        Object actual = (new PoissonDistributionImpl(Double.NaN)).normalApproximateProbability(-1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_069() throws Exception {
        // Combination: receiver__p=Double.NaN, x=Integer.MAX_VALUE
        Object actual = (new PoissonDistributionImpl(Double.NaN)).normalApproximateProbability(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_070() throws Exception {
        // Combination: receiver__p=Double.NaN, x=Integer.MIN_VALUE
        Object actual = (new PoissonDistributionImpl(Double.NaN)).normalApproximateProbability(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_071() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, x=0
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).normalApproximateProbability(0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_072() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, x=1
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).normalApproximateProbability(1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_073() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, x=-1
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).normalApproximateProbability(-1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_074() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, x=Integer.MAX_VALUE
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).normalApproximateProbability(Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_normalApproximateProbability_pairwise_075() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, x=Integer.MIN_VALUE
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).normalApproximateProbability(Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_076() throws Exception {
        // Combination: receiver__p=0.0d, p=0.0d
        try {
            (new PoissonDistributionImpl(0.0d)).getDomainLowerBound(0.0d);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_077() throws Exception {
        // Combination: receiver__p=1.0d, p=0.0d
        Object actual = (new PoissonDistributionImpl(1.0d)).getDomainLowerBound(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_078() throws Exception {
        // Combination: receiver__p=-1.0d, p=0.0d
        try {
            (new PoissonDistributionImpl(-1.0d)).getDomainLowerBound(0.0d);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_079() throws Exception {
        // Combination: receiver__p=Double.NaN, p=0.0d
        Object actual = (new PoissonDistributionImpl(Double.NaN)).getDomainLowerBound(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_080() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, p=0.0d
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).getDomainLowerBound(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_081() throws Exception {
        // Combination: receiver__p=0.0d, p=1.0d
        try {
            (new PoissonDistributionImpl(0.0d)).getDomainLowerBound(1.0d);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_082() throws Exception {
        // Combination: receiver__p=1.0d, p=1.0d
        Object actual = (new PoissonDistributionImpl(1.0d)).getDomainLowerBound(1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_083() throws Exception {
        // Combination: receiver__p=-1.0d, p=1.0d
        try {
            (new PoissonDistributionImpl(-1.0d)).getDomainLowerBound(1.0d);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_084() throws Exception {
        // Combination: receiver__p=Double.NaN, p=1.0d
        Object actual = (new PoissonDistributionImpl(Double.NaN)).getDomainLowerBound(1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_085() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, p=1.0d
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).getDomainLowerBound(1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_086() throws Exception {
        // Combination: receiver__p=0.0d, p=-1.0d
        try {
            (new PoissonDistributionImpl(0.0d)).getDomainLowerBound(-1.0d);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_087() throws Exception {
        // Combination: receiver__p=1.0d, p=-1.0d
        Object actual = (new PoissonDistributionImpl(1.0d)).getDomainLowerBound(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_088() throws Exception {
        // Combination: receiver__p=-1.0d, p=-1.0d
        try {
            (new PoissonDistributionImpl(-1.0d)).getDomainLowerBound(-1.0d);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_089() throws Exception {
        // Combination: receiver__p=Double.NaN, p=-1.0d
        Object actual = (new PoissonDistributionImpl(Double.NaN)).getDomainLowerBound(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_090() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, p=-1.0d
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).getDomainLowerBound(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_091() throws Exception {
        // Combination: receiver__p=0.0d, p=Double.NaN
        try {
            (new PoissonDistributionImpl(0.0d)).getDomainLowerBound(Double.NaN);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_092() throws Exception {
        // Combination: receiver__p=1.0d, p=Double.NaN
        Object actual = (new PoissonDistributionImpl(1.0d)).getDomainLowerBound(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_093() throws Exception {
        // Combination: receiver__p=-1.0d, p=Double.NaN
        try {
            (new PoissonDistributionImpl(-1.0d)).getDomainLowerBound(Double.NaN);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_094() throws Exception {
        // Combination: receiver__p=Double.NaN, p=Double.NaN
        Object actual = (new PoissonDistributionImpl(Double.NaN)).getDomainLowerBound(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_095() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, p=Double.NaN
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).getDomainLowerBound(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_096() throws Exception {
        // Combination: receiver__p=0.0d, p=Double.POSITIVE_INFINITY
        try {
            (new PoissonDistributionImpl(0.0d)).getDomainLowerBound(Double.POSITIVE_INFINITY);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_097() throws Exception {
        // Combination: receiver__p=1.0d, p=Double.POSITIVE_INFINITY
        Object actual = (new PoissonDistributionImpl(1.0d)).getDomainLowerBound(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_098() throws Exception {
        // Combination: receiver__p=-1.0d, p=Double.POSITIVE_INFINITY
        try {
            (new PoissonDistributionImpl(-1.0d)).getDomainLowerBound(Double.POSITIVE_INFINITY);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_099() throws Exception {
        // Combination: receiver__p=Double.NaN, p=Double.POSITIVE_INFINITY
        Object actual = (new PoissonDistributionImpl(Double.NaN)).getDomainLowerBound(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_100() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, p=Double.POSITIVE_INFINITY
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).getDomainLowerBound(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_101() throws Exception {
        // Combination: receiver__p=0.0d, p=0.0d
        try {
            (new PoissonDistributionImpl(0.0d)).getDomainUpperBound(0.0d);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_102() throws Exception {
        // Combination: receiver__p=1.0d, p=0.0d
        Object actual = (new PoissonDistributionImpl(1.0d)).getDomainUpperBound(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_103() throws Exception {
        // Combination: receiver__p=-1.0d, p=0.0d
        try {
            (new PoissonDistributionImpl(-1.0d)).getDomainUpperBound(0.0d);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_104() throws Exception {
        // Combination: receiver__p=Double.NaN, p=0.0d
        Object actual = (new PoissonDistributionImpl(Double.NaN)).getDomainUpperBound(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_105() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, p=0.0d
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).getDomainUpperBound(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_106() throws Exception {
        // Combination: receiver__p=0.0d, p=1.0d
        try {
            (new PoissonDistributionImpl(0.0d)).getDomainUpperBound(1.0d);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_107() throws Exception {
        // Combination: receiver__p=1.0d, p=1.0d
        Object actual = (new PoissonDistributionImpl(1.0d)).getDomainUpperBound(1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_108() throws Exception {
        // Combination: receiver__p=-1.0d, p=1.0d
        try {
            (new PoissonDistributionImpl(-1.0d)).getDomainUpperBound(1.0d);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_109() throws Exception {
        // Combination: receiver__p=Double.NaN, p=1.0d
        Object actual = (new PoissonDistributionImpl(Double.NaN)).getDomainUpperBound(1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_110() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, p=1.0d
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).getDomainUpperBound(1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_111() throws Exception {
        // Combination: receiver__p=0.0d, p=-1.0d
        try {
            (new PoissonDistributionImpl(0.0d)).getDomainUpperBound(-1.0d);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_112() throws Exception {
        // Combination: receiver__p=1.0d, p=-1.0d
        Object actual = (new PoissonDistributionImpl(1.0d)).getDomainUpperBound(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_113() throws Exception {
        // Combination: receiver__p=-1.0d, p=-1.0d
        try {
            (new PoissonDistributionImpl(-1.0d)).getDomainUpperBound(-1.0d);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_114() throws Exception {
        // Combination: receiver__p=Double.NaN, p=-1.0d
        Object actual = (new PoissonDistributionImpl(Double.NaN)).getDomainUpperBound(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_115() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, p=-1.0d
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).getDomainUpperBound(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_116() throws Exception {
        // Combination: receiver__p=0.0d, p=Double.NaN
        try {
            (new PoissonDistributionImpl(0.0d)).getDomainUpperBound(Double.NaN);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_117() throws Exception {
        // Combination: receiver__p=1.0d, p=Double.NaN
        Object actual = (new PoissonDistributionImpl(1.0d)).getDomainUpperBound(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_118() throws Exception {
        // Combination: receiver__p=-1.0d, p=Double.NaN
        try {
            (new PoissonDistributionImpl(-1.0d)).getDomainUpperBound(Double.NaN);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_119() throws Exception {
        // Combination: receiver__p=Double.NaN, p=Double.NaN
        Object actual = (new PoissonDistributionImpl(Double.NaN)).getDomainUpperBound(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_120() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, p=Double.NaN
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).getDomainUpperBound(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_121() throws Exception {
        // Combination: receiver__p=0.0d, p=Double.POSITIVE_INFINITY
        try {
            (new PoissonDistributionImpl(0.0d)).getDomainUpperBound(Double.POSITIVE_INFINITY);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_122() throws Exception {
        // Combination: receiver__p=1.0d, p=Double.POSITIVE_INFINITY
        Object actual = (new PoissonDistributionImpl(1.0d)).getDomainUpperBound(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_123() throws Exception {
        // Combination: receiver__p=-1.0d, p=Double.POSITIVE_INFINITY
        try {
            (new PoissonDistributionImpl(-1.0d)).getDomainUpperBound(Double.POSITIVE_INFINITY);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_124() throws Exception {
        // Combination: receiver__p=Double.NaN, p=Double.POSITIVE_INFINITY
        Object actual = (new PoissonDistributionImpl(Double.NaN)).getDomainUpperBound(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_125() throws Exception {
        // Combination: receiver__p=Double.POSITIVE_INFINITY, p=Double.POSITIVE_INFINITY
        Object actual = (new PoissonDistributionImpl(Double.POSITIVE_INFINITY)).getDomainUpperBound(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

}
