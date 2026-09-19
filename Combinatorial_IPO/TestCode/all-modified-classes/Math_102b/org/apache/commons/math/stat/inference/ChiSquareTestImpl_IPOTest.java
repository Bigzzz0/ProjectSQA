package org.apache.commons.math.stat.inference;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for ChiSquareTestImpl.
 */
public class ChiSquareTestImpl_IPOTest {
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

}
