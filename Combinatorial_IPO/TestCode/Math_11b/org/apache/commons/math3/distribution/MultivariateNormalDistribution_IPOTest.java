package org.apache.commons.math3.distribution;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for MultivariateNormalDistribution.
 */
public class MultivariateNormalDistribution_IPOTest {
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
    public void test_getMeans_pairwise_001() throws Exception {
        // Combination: receiver__means=new double[] {}, receiver__covariances=new double[][] {}
        try {
            (new MultivariateNormalDistribution(new double[] {}, new double[][] {})).getMeans();
            fail("Expected org.apache.commons.math3.exception.NoDataException");
        } catch (org.apache.commons.math3.exception.NoDataException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeans_pairwise_002() throws Exception {
        // Combination: receiver__means=new double[] {1}, receiver__covariances=new double[][] {}
        try {
            (new MultivariateNormalDistribution(new double[] {1}, new double[][] {})).getMeans();
            fail("Expected org.apache.commons.math3.exception.DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeans_pairwise_003() throws Exception {
        // Combination: receiver__means=new double[] {}, receiver__covariances=new double[][] {{}}
        try {
            (new MultivariateNormalDistribution(new double[] {}, new double[][] {{}})).getMeans();
            fail("Expected org.apache.commons.math3.exception.DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getMeans_pairwise_004() throws Exception {
        // Combination: receiver__means=new double[] {1}, receiver__covariances=new double[][] {{}}
        try {
            (new MultivariateNormalDistribution(new double[] {1}, new double[][] {{}})).getMeans();
            fail("Expected org.apache.commons.math3.exception.DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCovariances_pairwise_005() throws Exception {
        // Combination: receiver__means=new double[] {}, receiver__covariances=new double[][] {}
        try {
            (new MultivariateNormalDistribution(new double[] {}, new double[][] {})).getCovariances();
            fail("Expected org.apache.commons.math3.exception.NoDataException");
        } catch (org.apache.commons.math3.exception.NoDataException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCovariances_pairwise_006() throws Exception {
        // Combination: receiver__means=new double[] {1}, receiver__covariances=new double[][] {}
        try {
            (new MultivariateNormalDistribution(new double[] {1}, new double[][] {})).getCovariances();
            fail("Expected org.apache.commons.math3.exception.DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCovariances_pairwise_007() throws Exception {
        // Combination: receiver__means=new double[] {}, receiver__covariances=new double[][] {{}}
        try {
            (new MultivariateNormalDistribution(new double[] {}, new double[][] {{}})).getCovariances();
            fail("Expected org.apache.commons.math3.exception.DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCovariances_pairwise_008() throws Exception {
        // Combination: receiver__means=new double[] {1}, receiver__covariances=new double[][] {{}}
        try {
            (new MultivariateNormalDistribution(new double[] {1}, new double[][] {{}})).getCovariances();
            fail("Expected org.apache.commons.math3.exception.DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_009() throws Exception {
        // Combination: receiver__means=new double[] {}, receiver__covariances=new double[][] {}, vals=new double[] {}
        try {
            (new MultivariateNormalDistribution(new double[] {}, new double[][] {})).density(new double[] {});
            fail("Expected org.apache.commons.math3.exception.NoDataException");
        } catch (org.apache.commons.math3.exception.NoDataException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_010() throws Exception {
        // Combination: receiver__means=new double[] {1}, receiver__covariances=new double[][] {}, vals=new double[] {1}
        try {
            (new MultivariateNormalDistribution(new double[] {1}, new double[][] {})).density(new double[] {1});
            fail("Expected org.apache.commons.math3.exception.DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_011() throws Exception {
        // Combination: receiver__means=new double[] {}, receiver__covariances=new double[][] {{}}, vals=new double[] {1}
        try {
            (new MultivariateNormalDistribution(new double[] {}, new double[][] {{}})).density(new double[] {1});
            fail("Expected org.apache.commons.math3.exception.DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_density_pairwise_012() throws Exception {
        // Combination: receiver__means=new double[] {1}, receiver__covariances=new double[][] {{}}, vals=new double[] {}
        try {
            (new MultivariateNormalDistribution(new double[] {1}, new double[][] {{}})).density(new double[] {});
            fail("Expected org.apache.commons.math3.exception.DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStandardDeviations_pairwise_013() throws Exception {
        // Combination: receiver__means=new double[] {}, receiver__covariances=new double[][] {}
        try {
            (new MultivariateNormalDistribution(new double[] {}, new double[][] {})).getStandardDeviations();
            fail("Expected org.apache.commons.math3.exception.NoDataException");
        } catch (org.apache.commons.math3.exception.NoDataException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStandardDeviations_pairwise_014() throws Exception {
        // Combination: receiver__means=new double[] {1}, receiver__covariances=new double[][] {}
        try {
            (new MultivariateNormalDistribution(new double[] {1}, new double[][] {})).getStandardDeviations();
            fail("Expected org.apache.commons.math3.exception.DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStandardDeviations_pairwise_015() throws Exception {
        // Combination: receiver__means=new double[] {}, receiver__covariances=new double[][] {{}}
        try {
            (new MultivariateNormalDistribution(new double[] {}, new double[][] {{}})).getStandardDeviations();
            fail("Expected org.apache.commons.math3.exception.DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getStandardDeviations_pairwise_016() throws Exception {
        // Combination: receiver__means=new double[] {1}, receiver__covariances=new double[][] {{}}
        try {
            (new MultivariateNormalDistribution(new double[] {1}, new double[][] {{}})).getStandardDeviations();
            fail("Expected org.apache.commons.math3.exception.DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_sample_pairwise_017() throws Exception {
        // Combination: receiver__means=new double[] {}, receiver__covariances=new double[][] {}
        try {
            (new MultivariateNormalDistribution(new double[] {}, new double[][] {})).sample();
            fail("Expected org.apache.commons.math3.exception.NoDataException");
        } catch (org.apache.commons.math3.exception.NoDataException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_sample_pairwise_018() throws Exception {
        // Combination: receiver__means=new double[] {1}, receiver__covariances=new double[][] {}
        try {
            (new MultivariateNormalDistribution(new double[] {1}, new double[][] {})).sample();
            fail("Expected org.apache.commons.math3.exception.DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_sample_pairwise_019() throws Exception {
        // Combination: receiver__means=new double[] {}, receiver__covariances=new double[][] {{}}
        try {
            (new MultivariateNormalDistribution(new double[] {}, new double[][] {{}})).sample();
            fail("Expected org.apache.commons.math3.exception.DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_sample_pairwise_020() throws Exception {
        // Combination: receiver__means=new double[] {1}, receiver__covariances=new double[][] {{}}
        try {
            (new MultivariateNormalDistribution(new double[] {1}, new double[][] {{}})).sample();
            fail("Expected org.apache.commons.math3.exception.DimensionMismatchException");
        } catch (org.apache.commons.math3.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
