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
    public void test_getInitialDomain_pairwise_029() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=0.0d, p=0.0d
        try {
            (new FDistributionImpl(0.0d, 0.0d)).getInitialDomain(0.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_030() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=1.0d, p=0.0d
        Object actual = (new FDistributionImpl(1.0d, 1.0d)).getInitialDomain(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_031() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, p=0.0d
        try {
            (new FDistributionImpl(-1.0d, -1.0d)).getInitialDomain(0.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_032() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.NaN, p=0.0d
        Object actual = (new FDistributionImpl(Double.NaN, Double.NaN)).getInitialDomain(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_033() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=0.0d
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)).getInitialDomain(0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_034() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=0.0d, p=1.0d
        try {
            (new FDistributionImpl(1.0d, 0.0d)).getInitialDomain(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_035() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d, p=1.0d
        try {
            (new FDistributionImpl(0.0d, 1.0d)).getInitialDomain(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_036() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=-1.0d, p=1.0d
        try {
            (new FDistributionImpl(Double.NaN, -1.0d)).getInitialDomain(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_037() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, p=1.0d
        try {
            (new FDistributionImpl(-1.0d, Double.NaN)).getInitialDomain(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_038() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=1.0d
        try {
            (new FDistributionImpl(0.0d, Double.POSITIVE_INFINITY)).getInitialDomain(1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_039() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=0.0d, p=-1.0d
        try {
            (new FDistributionImpl(-1.0d, 0.0d)).getInitialDomain(-1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_040() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=1.0d, p=-1.0d
        Object actual = (new FDistributionImpl(Double.NaN, 1.0d)).getInitialDomain(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_041() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=-1.0d, p=-1.0d
        try {
            (new FDistributionImpl(0.0d, -1.0d)).getInitialDomain(-1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_042() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, p=-1.0d
        Object actual = (new FDistributionImpl(1.0d, Double.NaN)).getInitialDomain(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_043() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=-1.0d
        Object actual = (new FDistributionImpl(1.0d, Double.POSITIVE_INFINITY)).getInitialDomain(-1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_044() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=0.0d, p=Double.NaN
        try {
            (new FDistributionImpl(Double.NaN, 0.0d)).getInitialDomain(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_045() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=1.0d, p=Double.NaN
        try {
            (new FDistributionImpl(-1.0d, 1.0d)).getInitialDomain(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_046() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, p=Double.NaN
        try {
            (new FDistributionImpl(1.0d, -1.0d)).getInitialDomain(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_047() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, p=Double.NaN
        try {
            (new FDistributionImpl(0.0d, Double.NaN)).getInitialDomain(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_048() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=Double.NaN
        try {
            (new FDistributionImpl(-1.0d, Double.POSITIVE_INFINITY)).getInitialDomain(Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_049() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=0.0d, p=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(Double.POSITIVE_INFINITY, 0.0d)).getInitialDomain(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_050() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=0.0d, receiver__denominatorDegreesOfFreedom=1.0d, p=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(0.0d, 1.0d)).getInitialDomain(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_051() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=1.0d, receiver__denominatorDegreesOfFreedom=-1.0d, p=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(1.0d, -1.0d)).getInitialDomain(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_052() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=-1.0d, receiver__denominatorDegreesOfFreedom=Double.NaN, p=Double.POSITIVE_INFINITY
        try {
            (new FDistributionImpl(-1.0d, Double.NaN)).getInitialDomain(Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_053() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.NaN, receiver__denominatorDegreesOfFreedom=Double.POSITIVE_INFINITY, p=Double.POSITIVE_INFINITY
        Object actual = (new FDistributionImpl(Double.NaN, Double.POSITIVE_INFINITY)).getInitialDomain(Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_054() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=1.0d, p=1.0d
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, 1.0d)).getInitialDomain(1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_055() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=-1.0d, p=-1.0d
        try {
            (new FDistributionImpl(Double.POSITIVE_INFINITY, -1.0d)).getInitialDomain(-1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getInitialDomain_pairwise_056() throws Exception {
        // Combination: receiver__numeratorDegreesOfFreedom=Double.POSITIVE_INFINITY, receiver__denominatorDegreesOfFreedom=Double.NaN, p=Double.NaN
        Object actual = (new FDistributionImpl(Double.POSITIVE_INFINITY, Double.NaN)).getInitialDomain(Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

}
