package org.apache.commons.math.distribution;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for FDistributionImpl.
 */
public class FDistributionImpl_IPOTest {
    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_001() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d, x=0.0d
        try {
            (new FDistributionImpl(0.0d, 0.0d)).cumulativeProbability(0.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_002() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d, x=1.0d
        try {
            (new FDistributionImpl(1.0d, 0.0d)).cumulativeProbability(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_003() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d, x=-1.0d
        try {
            (new FDistributionImpl(-1.0d, 0.0d)).cumulativeProbability(-1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_004() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d, x=Double.NaN
        try {
            (new FDistributionImpl(Double.NaN, 0.0d)).cumulativeProbability(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_005() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d, x=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(Double.POSITIVE_INFINITY, 0.0d)).cumulativeProbability(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_006() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d, x=1.0d
        try {
            (new FDistributionImpl(0.0d, 1.0d)).cumulativeProbability(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_007() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d, x=0.0d
        Object actual = (new FDistributionImpl(1.0d, 1.0d)).cumulativeProbability(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_008() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d, x=Double.NaN
        try {
            (new FDistributionImpl(-1.0d, 1.0d)).cumulativeProbability(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_009() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d, x=-1.0d
        Object actual = (new FDistributionImpl(Double.NaN, 1.0d)).cumulativeProbability(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_010() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d, x=0.0d
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, 1.0d)).cumulativeProbability(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_011() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d, x=-1.0d
        try {
            (new FDistributionImpl(0.0d, -1.0d)).cumulativeProbability(-1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_012() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, x=Double.NaN
        try {
            (new FDistributionImpl(1.0d, -1.0d)).cumulativeProbability(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_013() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, x=0.0d
        try {
            (new FDistributionImpl(-1.0d, -1.0d)).cumulativeProbability(0.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_014() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d, x=1.0d
        try {
            (new FDistributionImpl(Double.NaN, -1.0d)).cumulativeProbability(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_015() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d, x=1.0d
        try {
            (new FDistributionImpl(Double.POSITIVE_INFINITY, -1.0d)).cumulativeProbability(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_016() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, x=Double.NaN
        try {
            (new FDistributionImpl(0.0d, Double.NaN)).cumulativeProbability(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_017() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, x=-1.0d
        Object actual = (new FDistributionImpl(1.0d, Double.NaN)).cumulativeProbability(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_018() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, x=1.0d
        try {
            (new FDistributionImpl(-1.0d, Double.NaN)).cumulativeProbability(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_019() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN, x=0.0d
        Object actual = (new FDistributionImpl(Double.NaN, Double.NaN)).cumulativeProbability(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_020() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN, x=-1.0d
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, Double.NaN)).cumulativeProbability(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_021() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, x=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(0.0d, Double.POSITIVE_INFINITY)).cumulativeProbability(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_022() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, x=0.0d
        Object actual = (new FDistributionImpl(1.0d, Double.POSITIVE_INFINITY)).cumulativeProbability(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_023() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, x=1.0d
        try {
            (new FDistributionImpl(-1.0d, Double.POSITIVE_INFINITY)).cumulativeProbability(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_024() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, x=-1.0d
        Object actual = (new FDistributionImpl(Double.NaN, Double.POSITIVE_INFINITY)).cumulativeProbability(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_025() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, x=Double.NaN
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).cumulativeProbability(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_026() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d, x=Double.POSITIVE_INFINITY
        Object actual = (new FDistributionImpl(1.0d, 1.0d)).cumulativeProbability(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_027() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, x=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(-1.0d, -1.0d)).cumulativeProbability(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_cumulativeProbability_pairwise_028() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN, x=Double.POSITIVE_INFINITY
        Object actual = (new FDistributionImpl(Double.NaN, Double.NaN)).cumulativeProbability(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_029() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d, p=0.0d
        try {
            (new FDistributionImpl(0.0d, 0.0d)).getDomainLowerBound(0.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_030() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d, p=0.0d
        Object actual = (new FDistributionImpl(1.0d, 1.0d)).getDomainLowerBound(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_031() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, p=0.0d
        try {
            (new FDistributionImpl(-1.0d, -1.0d)).getDomainLowerBound(0.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_032() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN, p=0.0d
        Object actual = (new FDistributionImpl(Double.NaN, Double.NaN)).getDomainLowerBound(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_033() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=0.0d
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getDomainLowerBound(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_034() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d, p=1.0d
        try {
            (new FDistributionImpl(1.0d, 0.0d)).getDomainLowerBound(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_035() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d, p=1.0d
        try {
            (new FDistributionImpl(0.0d, 1.0d)).getDomainLowerBound(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_036() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d, p=1.0d
        try {
            (new FDistributionImpl(Double.NaN, -1.0d)).getDomainLowerBound(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_037() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, p=1.0d
        try {
            (new FDistributionImpl(-1.0d, Double.NaN)).getDomainLowerBound(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_038() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=1.0d
        try {
            (new FDistributionImpl(0.0d, Double.POSITIVE_INFINITY)).getDomainLowerBound(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_039() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d, p=-1.0d
        try {
            (new FDistributionImpl(-1.0d, 0.0d)).getDomainLowerBound(-1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_040() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d, p=-1.0d
        Object actual = (new FDistributionImpl(Double.NaN, 1.0d)).getDomainLowerBound(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_041() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d, p=-1.0d
        try {
            (new FDistributionImpl(0.0d, -1.0d)).getDomainLowerBound(-1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_042() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, p=-1.0d
        Object actual = (new FDistributionImpl(1.0d, Double.NaN)).getDomainLowerBound(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_043() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=-1.0d
        Object actual = (new FDistributionImpl(1.0d, Double.POSITIVE_INFINITY)).getDomainLowerBound(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_044() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d, p=Double.NaN
        try {
            (new FDistributionImpl(Double.NaN, 0.0d)).getDomainLowerBound(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_045() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d, p=Double.NaN
        try {
            (new FDistributionImpl(-1.0d, 1.0d)).getDomainLowerBound(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_046() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, p=Double.NaN
        try {
            (new FDistributionImpl(1.0d, -1.0d)).getDomainLowerBound(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_047() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, p=Double.NaN
        try {
            (new FDistributionImpl(0.0d, Double.NaN)).getDomainLowerBound(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_048() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=Double.NaN
        try {
            (new FDistributionImpl(-1.0d, Double.POSITIVE_INFINITY)).getDomainLowerBound(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_049() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d, p=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(Double.POSITIVE_INFINITY, 0.0d)).getDomainLowerBound(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_050() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d, p=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(0.0d, 1.0d)).getDomainLowerBound(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_051() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, p=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(1.0d, -1.0d)).getDomainLowerBound(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_052() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, p=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(-1.0d, Double.NaN)).getDomainLowerBound(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_053() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=Double.POSITIVE_INFINITY
        Object actual = (new FDistributionImpl(Double.NaN, Double.POSITIVE_INFINITY)).getDomainLowerBound(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_054() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d, p=1.0d
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, 1.0d)).getDomainLowerBound(1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_055() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d, p=-1.0d
        try {
            (new FDistributionImpl(Double.POSITIVE_INFINITY, -1.0d)).getDomainLowerBound(-1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainLowerBound_pairwise_056() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN, p=Double.NaN
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, Double.NaN)).getDomainLowerBound(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_057() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d, p=0.0d
        try {
            (new FDistributionImpl(0.0d, 0.0d)).getDomainUpperBound(0.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_058() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d, p=0.0d
        Object actual = (new FDistributionImpl(1.0d, 1.0d)).getDomainUpperBound(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_059() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, p=0.0d
        try {
            (new FDistributionImpl(-1.0d, -1.0d)).getDomainUpperBound(0.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_060() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN, p=0.0d
        Object actual = (new FDistributionImpl(Double.NaN, Double.NaN)).getDomainUpperBound(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_061() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=0.0d
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getDomainUpperBound(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_062() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d, p=1.0d
        try {
            (new FDistributionImpl(1.0d, 0.0d)).getDomainUpperBound(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_063() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d, p=1.0d
        try {
            (new FDistributionImpl(0.0d, 1.0d)).getDomainUpperBound(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_064() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d, p=1.0d
        try {
            (new FDistributionImpl(Double.NaN, -1.0d)).getDomainUpperBound(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_065() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, p=1.0d
        try {
            (new FDistributionImpl(-1.0d, Double.NaN)).getDomainUpperBound(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_066() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=1.0d
        try {
            (new FDistributionImpl(0.0d, Double.POSITIVE_INFINITY)).getDomainUpperBound(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_067() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d, p=-1.0d
        try {
            (new FDistributionImpl(-1.0d, 0.0d)).getDomainUpperBound(-1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_068() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d, p=-1.0d
        Object actual = (new FDistributionImpl(Double.NaN, 1.0d)).getDomainUpperBound(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_069() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d, p=-1.0d
        try {
            (new FDistributionImpl(0.0d, -1.0d)).getDomainUpperBound(-1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_070() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, p=-1.0d
        Object actual = (new FDistributionImpl(1.0d, Double.NaN)).getDomainUpperBound(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_071() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=-1.0d
        Object actual = (new FDistributionImpl(1.0d, Double.POSITIVE_INFINITY)).getDomainUpperBound(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_072() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d, p=Double.NaN
        try {
            (new FDistributionImpl(Double.NaN, 0.0d)).getDomainUpperBound(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_073() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d, p=Double.NaN
        try {
            (new FDistributionImpl(-1.0d, 1.0d)).getDomainUpperBound(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_074() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, p=Double.NaN
        try {
            (new FDistributionImpl(1.0d, -1.0d)).getDomainUpperBound(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_075() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, p=Double.NaN
        try {
            (new FDistributionImpl(0.0d, Double.NaN)).getDomainUpperBound(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_076() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=Double.NaN
        try {
            (new FDistributionImpl(-1.0d, Double.POSITIVE_INFINITY)).getDomainUpperBound(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_077() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d, p=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(Double.POSITIVE_INFINITY, 0.0d)).getDomainUpperBound(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_078() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d, p=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(0.0d, 1.0d)).getDomainUpperBound(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_079() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, p=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(1.0d, -1.0d)).getDomainUpperBound(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_080() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, p=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(-1.0d, Double.NaN)).getDomainUpperBound(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_081() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=Double.POSITIVE_INFINITY
        Object actual = (new FDistributionImpl(Double.NaN, Double.POSITIVE_INFINITY)).getDomainUpperBound(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_082() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d, p=1.0d
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, 1.0d)).getDomainUpperBound(1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_083() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d, p=-1.0d
        try {
            (new FDistributionImpl(Double.POSITIVE_INFINITY, -1.0d)).getDomainUpperBound(-1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDomainUpperBound_pairwise_084() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN, p=Double.NaN
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, Double.NaN)).getDomainUpperBound(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_085() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d, p=0.0d
        try {
            (new FDistributionImpl(0.0d, 0.0d)).getInitialDomain(0.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_086() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d, p=0.0d
        Object actual = (new FDistributionImpl(1.0d, 1.0d)).getInitialDomain(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_087() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, p=0.0d
        try {
            (new FDistributionImpl(-1.0d, -1.0d)).getInitialDomain(0.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_088() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN, p=0.0d
        Object actual = (new FDistributionImpl(Double.NaN, Double.NaN)).getInitialDomain(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_089() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=0.0d
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getInitialDomain(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_090() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d, p=1.0d
        try {
            (new FDistributionImpl(1.0d, 0.0d)).getInitialDomain(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_091() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d, p=1.0d
        try {
            (new FDistributionImpl(0.0d, 1.0d)).getInitialDomain(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_092() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d, p=1.0d
        try {
            (new FDistributionImpl(Double.NaN, -1.0d)).getInitialDomain(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_093() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, p=1.0d
        try {
            (new FDistributionImpl(-1.0d, Double.NaN)).getInitialDomain(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_094() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=1.0d
        try {
            (new FDistributionImpl(0.0d, Double.POSITIVE_INFINITY)).getInitialDomain(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_095() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d, p=-1.0d
        try {
            (new FDistributionImpl(-1.0d, 0.0d)).getInitialDomain(-1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_096() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d, p=-1.0d
        Object actual = (new FDistributionImpl(Double.NaN, 1.0d)).getInitialDomain(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_097() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d, p=-1.0d
        try {
            (new FDistributionImpl(0.0d, -1.0d)).getInitialDomain(-1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_098() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, p=-1.0d
        Object actual = (new FDistributionImpl(1.0d, Double.NaN)).getInitialDomain(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_099() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=-1.0d
        Object actual = (new FDistributionImpl(1.0d, Double.POSITIVE_INFINITY)).getInitialDomain(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_100() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d, p=Double.NaN
        try {
            (new FDistributionImpl(Double.NaN, 0.0d)).getInitialDomain(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_101() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d, p=Double.NaN
        try {
            (new FDistributionImpl(-1.0d, 1.0d)).getInitialDomain(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_102() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, p=Double.NaN
        try {
            (new FDistributionImpl(1.0d, -1.0d)).getInitialDomain(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_103() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, p=Double.NaN
        try {
            (new FDistributionImpl(0.0d, Double.NaN)).getInitialDomain(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_104() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=Double.NaN
        try {
            (new FDistributionImpl(-1.0d, Double.POSITIVE_INFINITY)).getInitialDomain(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_105() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d, p=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(Double.POSITIVE_INFINITY, 0.0d)).getInitialDomain(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_106() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d, p=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(0.0d, 1.0d)).getInitialDomain(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_107() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, p=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(1.0d, -1.0d)).getInitialDomain(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_108() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, p=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(-1.0d, Double.NaN)).getInitialDomain(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_109() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=Double.POSITIVE_INFINITY
        Object actual = (new FDistributionImpl(Double.NaN, Double.POSITIVE_INFINITY)).getInitialDomain(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_110() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d, p=1.0d
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, 1.0d)).getInitialDomain(1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_111() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d, p=-1.0d
        try {
            (new FDistributionImpl(Double.POSITIVE_INFINITY, -1.0d)).getInitialDomain(-1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_112() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN, p=Double.NaN
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, Double.NaN)).getInitialDomain(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_113() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistributionImpl(0.0d, 0.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_114() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistributionImpl(1.0d, 0.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_115() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistributionImpl(-1.0d, 0.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_116() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistributionImpl(Double.NaN, 0.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_117() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistributionImpl(Double.POSITIVE_INFINITY, 0.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_118() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistributionImpl(0.0d, 1.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_119() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistributionImpl(1.0d, 1.0d)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_120() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistributionImpl(-1.0d, 1.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_121() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistributionImpl(Double.NaN, 1.0d)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_122() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, 1.0d)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_123() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistributionImpl(0.0d, -1.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_124() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistributionImpl(1.0d, -1.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_125() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistributionImpl(-1.0d, -1.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_126() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistributionImpl(Double.NaN, -1.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_127() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistributionImpl(Double.POSITIVE_INFINITY, -1.0d)).getNumeratorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_128() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistributionImpl(0.0d, Double.NaN)).getNumeratorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_129() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistributionImpl(1.0d, Double.NaN)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_130() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistributionImpl(-1.0d, Double.NaN)).getNumeratorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_131() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistributionImpl(Double.NaN, Double.NaN)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_132() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, Double.NaN)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_133() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(0.0d, Double.POSITIVE_INFINITY)).getNumeratorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_134() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistributionImpl(1.0d, Double.POSITIVE_INFINITY)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_135() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(-1.0d, Double.POSITIVE_INFINITY)).getNumeratorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_136() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistributionImpl(Double.NaN, Double.POSITIVE_INFINITY)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getNumeratorDegreesOfFreedom_pairwise_137() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getNumeratorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_138() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistributionImpl(0.0d, 0.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_139() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistributionImpl(1.0d, 0.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_140() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistributionImpl(-1.0d, 0.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_141() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistributionImpl(Double.NaN, 0.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_142() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d
        try {
            (new FDistributionImpl(Double.POSITIVE_INFINITY, 0.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_143() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistributionImpl(0.0d, 1.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_144() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistributionImpl(1.0d, 1.0d)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_145() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d
        try {
            (new FDistributionImpl(-1.0d, 1.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_146() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistributionImpl(Double.NaN, 1.0d)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_147() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, 1.0d)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_148() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistributionImpl(0.0d, -1.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_149() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistributionImpl(1.0d, -1.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_150() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistributionImpl(-1.0d, -1.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_151() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistributionImpl(Double.NaN, -1.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_152() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d
        try {
            (new FDistributionImpl(Double.POSITIVE_INFINITY, -1.0d)).getDenominatorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_153() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistributionImpl(0.0d, Double.NaN)).getDenominatorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_154() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistributionImpl(1.0d, Double.NaN)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_155() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN
        try {
            (new FDistributionImpl(-1.0d, Double.NaN)).getDenominatorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_156() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistributionImpl(Double.NaN, Double.NaN)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_157() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, Double.NaN)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_158() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(0.0d, Double.POSITIVE_INFINITY)).getDenominatorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_159() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistributionImpl(1.0d, Double.POSITIVE_INFINITY)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_160() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(-1.0d, Double.POSITIVE_INFINITY)).getDenominatorDegreesOfFreedom();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_161() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistributionImpl(Double.NaN, Double.POSITIVE_INFINITY)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getDenominatorDegreesOfFreedom_pairwise_162() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getDenominatorDegreesOfFreedom();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

}
