package org.apache.commons.math.stat.inference;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for ChiSquareTestImpl.
 */
public class ChiSquareTestImpl_IPOTest {
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
    public void test_chiSquareTest_pairwise_001() throws Exception {
        // Combination: expected=new double[] {}, observed=new long[] {}, alpha=0.0d
        try {
            (new ChiSquareTestImpl()).chiSquareTest(new double[] {}, new long[] {}, 0.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTest_pairwise_002() throws Exception {
        // Combination: expected=new double[] {1}, observed=new long[] {1}, alpha=0.0d
        try {
            (new ChiSquareTestImpl()).chiSquareTest(new double[] {1}, new long[] {1}, 0.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTest_pairwise_003() throws Exception {
        // Combination: expected=new double[] {}, observed=new long[] {1}, alpha=1.0d
        try {
            (new ChiSquareTestImpl()).chiSquareTest(new double[] {}, new long[] {1}, 1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTest_pairwise_004() throws Exception {
        // Combination: expected=new double[] {1}, observed=new long[] {}, alpha=1.0d
        try {
            (new ChiSquareTestImpl()).chiSquareTest(new double[] {1}, new long[] {}, 1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTest_pairwise_005() throws Exception {
        // Combination: expected=new double[] {}, observed=new long[] {}, alpha=-1.0d
        try {
            (new ChiSquareTestImpl()).chiSquareTest(new double[] {}, new long[] {}, -1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTest_pairwise_006() throws Exception {
        // Combination: expected=new double[] {1}, observed=new long[] {1}, alpha=-1.0d
        try {
            (new ChiSquareTestImpl()).chiSquareTest(new double[] {1}, new long[] {1}, -1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTest_pairwise_007() throws Exception {
        // Combination: expected=new double[] {}, observed=new long[] {}, alpha=Double.NaN
        try {
            (new ChiSquareTestImpl()).chiSquareTest(new double[] {}, new long[] {}, Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTest_pairwise_008() throws Exception {
        // Combination: expected=new double[] {1}, observed=new long[] {1}, alpha=Double.NaN
        try {
            (new ChiSquareTestImpl()).chiSquareTest(new double[] {1}, new long[] {1}, Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTest_pairwise_009() throws Exception {
        // Combination: expected=new double[] {}, observed=new long[] {}, alpha=Double.POSITIVE_INFINITY
        try {
            (new ChiSquareTestImpl()).chiSquareTest(new double[] {}, new long[] {}, Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTest_pairwise_010() throws Exception {
        // Combination: expected=new double[] {1}, observed=new long[] {1}, alpha=Double.POSITIVE_INFINITY
        try {
            (new ChiSquareTestImpl()).chiSquareTest(new double[] {1}, new long[] {1}, Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTestDataSetsComparison_pairwise_011() throws Exception {
        // Combination: observed1=new long[] {}, observed2=new long[] {}, alpha=0.0d
        try {
            (new ChiSquareTestImpl()).chiSquareTestDataSetsComparison(new long[] {}, new long[] {}, 0.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTestDataSetsComparison_pairwise_012() throws Exception {
        // Combination: observed1=new long[] {1}, observed2=new long[] {1}, alpha=0.0d
        try {
            (new ChiSquareTestImpl()).chiSquareTestDataSetsComparison(new long[] {1}, new long[] {1}, 0.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTestDataSetsComparison_pairwise_013() throws Exception {
        // Combination: observed1=new long[] {}, observed2=new long[] {1}, alpha=1.0d
        try {
            (new ChiSquareTestImpl()).chiSquareTestDataSetsComparison(new long[] {}, new long[] {1}, 1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTestDataSetsComparison_pairwise_014() throws Exception {
        // Combination: observed1=new long[] {1}, observed2=new long[] {}, alpha=1.0d
        try {
            (new ChiSquareTestImpl()).chiSquareTestDataSetsComparison(new long[] {1}, new long[] {}, 1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTestDataSetsComparison_pairwise_015() throws Exception {
        // Combination: observed1=new long[] {}, observed2=new long[] {}, alpha=-1.0d
        try {
            (new ChiSquareTestImpl()).chiSquareTestDataSetsComparison(new long[] {}, new long[] {}, -1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTestDataSetsComparison_pairwise_016() throws Exception {
        // Combination: observed1=new long[] {1}, observed2=new long[] {1}, alpha=-1.0d
        try {
            (new ChiSquareTestImpl()).chiSquareTestDataSetsComparison(new long[] {1}, new long[] {1}, -1.0d);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTestDataSetsComparison_pairwise_017() throws Exception {
        // Combination: observed1=new long[] {}, observed2=new long[] {}, alpha=Double.NaN
        try {
            (new ChiSquareTestImpl()).chiSquareTestDataSetsComparison(new long[] {}, new long[] {}, Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTestDataSetsComparison_pairwise_018() throws Exception {
        // Combination: observed1=new long[] {1}, observed2=new long[] {1}, alpha=Double.NaN
        try {
            (new ChiSquareTestImpl()).chiSquareTestDataSetsComparison(new long[] {1}, new long[] {1}, Double.NaN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTestDataSetsComparison_pairwise_019() throws Exception {
        // Combination: observed1=new long[] {}, observed2=new long[] {}, alpha=Double.POSITIVE_INFINITY
        try {
            (new ChiSquareTestImpl()).chiSquareTestDataSetsComparison(new long[] {}, new long[] {}, Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_chiSquareTestDataSetsComparison_pairwise_020() throws Exception {
        // Combination: observed1=new long[] {1}, observed2=new long[] {1}, alpha=Double.POSITIVE_INFINITY
        try {
            (new ChiSquareTestImpl()).chiSquareTestDataSetsComparison(new long[] {1}, new long[] {1}, Double.POSITIVE_INFINITY);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
