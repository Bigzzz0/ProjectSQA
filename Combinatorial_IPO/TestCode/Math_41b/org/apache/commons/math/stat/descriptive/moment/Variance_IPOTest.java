package org.apache.commons.math.stat.descriptive.moment;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Variance.
 */
public class Variance_IPOTest {
    @Test(timeout = 4000)
    public void test_evaluate_pairwise_001() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=0.0d, begin=0, length=0
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 0.0d, 0, 0);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_002() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, mean=1.0d, begin=0, length=1
        Object actual = (new Variance()).evaluate(new double[] {1}, new double[] {1}, 1.0d, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_003() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {1}, mean=-1.0d, begin=0, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {1}, -1.0d, 0, -1);
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_004() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.NaN, begin=0, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.NaN, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_005() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.POSITIVE_INFINITY, begin=0, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.POSITIVE_INFINITY, 0, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_006() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=1.0d, begin=1, length=0
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 1.0d, 1, 0);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_007() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {}, mean=0.0d, begin=1, length=1
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {}, 0.0d, 1, 1);
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_008() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, mean=Double.NaN, begin=1, length=-1
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {1}, Double.NaN, 1, -1);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_009() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {}, mean=-1.0d, begin=1, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {}, -1.0d, 1, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_010() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, mean=0.0d, begin=1, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {1}, 0.0d, 1, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_011() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, mean=-1.0d, begin=-1, length=0
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {1}, -1.0d, -1, 0);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_012() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.NaN, begin=-1, length=1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.NaN, -1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_013() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=0.0d, begin=-1, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 0.0d, -1, -1);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_014() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {1}, mean=1.0d, begin=-1, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {1}, 1.0d, -1, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_015() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=1.0d, begin=-1, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 1.0d, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_016() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.NaN, begin=Integer.MAX_VALUE, length=0
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.NaN, Integer.MAX_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_017() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, mean=-1.0d, begin=Integer.MAX_VALUE, length=1
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {1}, -1.0d, Integer.MAX_VALUE, 1);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_018() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=1.0d, begin=Integer.MAX_VALUE, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 1.0d, Integer.MAX_VALUE, -1);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_019() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=0.0d, begin=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 0.0d, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_020() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=-1.0d, begin=Integer.MAX_VALUE, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, -1.0d, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_021() throws Exception {
        // Combination: values=new double[] {1}, weights=new double[] {1}, mean=Double.POSITIVE_INFINITY, begin=Integer.MIN_VALUE, length=0
        try {
            (new Variance()).evaluate(new double[] {1}, new double[] {1}, Double.POSITIVE_INFINITY, Integer.MIN_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_022() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=0.0d, begin=Integer.MIN_VALUE, length=1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 0.0d, Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_023() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=1.0d, begin=Integer.MIN_VALUE, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, 1.0d, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_024() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=-1.0d, begin=Integer.MIN_VALUE, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, -1.0d, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_025() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.NaN, begin=Integer.MIN_VALUE, length=Integer.MIN_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.NaN, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_026() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.POSITIVE_INFINITY, begin=1, length=1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.POSITIVE_INFINITY, 1, 1);
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_027() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.POSITIVE_INFINITY, begin=-1, length=-1
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.POSITIVE_INFINITY, -1, -1);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_evaluate_pairwise_028() throws Exception {
        // Combination: values=new double[] {}, weights=new double[] {}, mean=Double.POSITIVE_INFINITY, begin=Integer.MAX_VALUE, length=Integer.MAX_VALUE
        try {
            (new Variance()).evaluate(new double[] {}, new double[] {}, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.MathIllegalArgumentException");
        } catch (org.apache.commons.math.exception.MathIllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
