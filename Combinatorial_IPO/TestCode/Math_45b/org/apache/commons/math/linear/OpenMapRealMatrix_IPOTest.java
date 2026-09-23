package org.apache.commons.math.linear;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for OpenMapRealMatrix.
 */
public class OpenMapRealMatrix_IPOTest {
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
    public void test_getColumnDimension_pairwise_001() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=0
        try {
            (new OpenMapRealMatrix(0, 0)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_002() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=0
        try {
            (new OpenMapRealMatrix(1, 0)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_003() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=0
        try {
            (new OpenMapRealMatrix(-1, 0)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_004() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=0
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, 0)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_005() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=0
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, 0)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_006() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=1
        try {
            (new OpenMapRealMatrix(0, 1)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_007() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=1
        Object actual = (new OpenMapRealMatrix(1, 1)).getColumnDimension();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_008() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=1
        try {
            (new OpenMapRealMatrix(-1, 1)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_009() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=1
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, 1)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_010() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=1
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, 1)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_011() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=-1
        try {
            (new OpenMapRealMatrix(0, -1)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_012() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=-1
        try {
            (new OpenMapRealMatrix(1, -1)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_013() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=-1
        try {
            (new OpenMapRealMatrix(-1, -1)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_014() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=-1
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, -1)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_015() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=-1
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, -1)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_016() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=Integer.MAX_VALUE
        try {
            (new OpenMapRealMatrix(0, Integer.MAX_VALUE)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_017() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=Integer.MAX_VALUE
        try {
            (new OpenMapRealMatrix(1, Integer.MAX_VALUE)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_018() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=Integer.MAX_VALUE
        try {
            (new OpenMapRealMatrix(-1, Integer.MAX_VALUE)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_019() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=Integer.MAX_VALUE
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, Integer.MAX_VALUE)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_020() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=Integer.MAX_VALUE
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, Integer.MAX_VALUE)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_021() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=Integer.MIN_VALUE
        try {
            (new OpenMapRealMatrix(0, Integer.MIN_VALUE)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_022() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=Integer.MIN_VALUE
        try {
            (new OpenMapRealMatrix(1, Integer.MIN_VALUE)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_023() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=Integer.MIN_VALUE
        try {
            (new OpenMapRealMatrix(-1, Integer.MIN_VALUE)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_024() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=Integer.MIN_VALUE
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, Integer.MIN_VALUE)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getColumnDimension_pairwise_025() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=Integer.MIN_VALUE
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, Integer.MIN_VALUE)).getColumnDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_026() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=0, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(0, 0)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_027() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=1, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(1, 1)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_028() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=-1, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(-1, -1)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_029() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=Integer.MAX_VALUE, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, Integer.MAX_VALUE)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_030() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=Integer.MIN_VALUE, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, Integer.MIN_VALUE)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_031() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=1, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(0, 1)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_032() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=-1, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(0, -1)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_033() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=Integer.MAX_VALUE, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(0, Integer.MAX_VALUE)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_034() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=Integer.MIN_VALUE, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(0, Integer.MIN_VALUE)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_035() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=0, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(1, 0)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_036() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=-1, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(1, -1)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_037() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=Integer.MAX_VALUE, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(1, Integer.MAX_VALUE)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_038() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=Integer.MIN_VALUE, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(1, Integer.MIN_VALUE)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_039() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=0, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(-1, 0)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_040() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=1, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(-1, 1)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_041() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=Integer.MAX_VALUE, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(-1, Integer.MAX_VALUE)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_042() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=Integer.MIN_VALUE, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(-1, Integer.MIN_VALUE)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_043() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=0, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, 0)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_044() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=1, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, 1)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_045() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=-1, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, -1)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_046() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=Integer.MIN_VALUE, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, Integer.MIN_VALUE)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_047() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=0, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, 0)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_048() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=1, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, 1)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_049() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=-1, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, -1)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_multiply_pairwise_050() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=Integer.MAX_VALUE, m=new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}})
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, Integer.MAX_VALUE)).multiply(new org.apache.commons.math.linear.Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}}));
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_051() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=0, row=0, column=0
        try {
            (new OpenMapRealMatrix(0, 0)).getEntry(0, 0);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_052() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=1, row=1, column=0
        try {
            (new OpenMapRealMatrix(1, 1)).getEntry(1, 0);
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_053() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=-1, row=-1, column=0
        try {
            (new OpenMapRealMatrix(-1, -1)).getEntry(-1, 0);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_054() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=Integer.MAX_VALUE, row=Integer.MAX_VALUE, column=0
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, Integer.MAX_VALUE)).getEntry(Integer.MAX_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_055() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=Integer.MIN_VALUE, row=Integer.MIN_VALUE, column=0
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, Integer.MIN_VALUE)).getEntry(Integer.MIN_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_056() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=0, row=-1, column=1
        try {
            (new OpenMapRealMatrix(1, 0)).getEntry(-1, 1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_057() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=1, row=Integer.MAX_VALUE, column=1
        try {
            (new OpenMapRealMatrix(0, 1)).getEntry(Integer.MAX_VALUE, 1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_058() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=-1, row=0, column=1
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, -1)).getEntry(0, 1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_059() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=Integer.MAX_VALUE, row=1, column=1
        try {
            (new OpenMapRealMatrix(-1, Integer.MAX_VALUE)).getEntry(1, 1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_060() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=Integer.MIN_VALUE, row=1, column=1
        try {
            (new OpenMapRealMatrix(0, Integer.MIN_VALUE)).getEntry(1, 1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_061() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=0, row=Integer.MAX_VALUE, column=-1
        try {
            (new OpenMapRealMatrix(-1, 0)).getEntry(Integer.MAX_VALUE, -1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_062() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=1, row=-1, column=-1
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, 1)).getEntry(-1, -1);
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_063() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=-1, row=Integer.MIN_VALUE, column=-1
        try {
            (new OpenMapRealMatrix(0, -1)).getEntry(Integer.MIN_VALUE, -1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_064() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=Integer.MAX_VALUE, row=0, column=-1
        try {
            (new OpenMapRealMatrix(1, Integer.MAX_VALUE)).getEntry(0, -1);
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_065() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=Integer.MIN_VALUE, row=Integer.MAX_VALUE, column=-1
        try {
            (new OpenMapRealMatrix(1, Integer.MIN_VALUE)).getEntry(Integer.MAX_VALUE, -1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_066() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=0, row=1, column=Integer.MAX_VALUE
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, 0)).getEntry(1, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_067() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=1, row=0, column=Integer.MAX_VALUE
        try {
            (new OpenMapRealMatrix(-1, 1)).getEntry(0, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_068() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=-1, row=Integer.MAX_VALUE, column=Integer.MAX_VALUE
        try {
            (new OpenMapRealMatrix(1, -1)).getEntry(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_069() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=Integer.MAX_VALUE, row=-1, column=Integer.MAX_VALUE
        try {
            (new OpenMapRealMatrix(0, Integer.MAX_VALUE)).getEntry(-1, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_070() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=Integer.MIN_VALUE, row=Integer.MIN_VALUE, column=Integer.MAX_VALUE
        try {
            (new OpenMapRealMatrix(-1, Integer.MIN_VALUE)).getEntry(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_071() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=0, row=0, column=Integer.MIN_VALUE
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, 0)).getEntry(0, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_072() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=1, row=Integer.MIN_VALUE, column=Integer.MIN_VALUE
        try {
            (new OpenMapRealMatrix(0, 1)).getEntry(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_073() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=-1, row=1, column=Integer.MIN_VALUE
        try {
            (new OpenMapRealMatrix(1, -1)).getEntry(1, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_074() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=Integer.MAX_VALUE, row=-1, column=Integer.MIN_VALUE
        try {
            (new OpenMapRealMatrix(-1, Integer.MAX_VALUE)).getEntry(-1, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_075() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=Integer.MIN_VALUE, row=0, column=Integer.MIN_VALUE
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, Integer.MIN_VALUE)).getEntry(0, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_076() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=1, row=1, column=1
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, 1)).getEntry(1, 1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_077() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=-1, row=1, column=-1
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, -1)).getEntry(1, -1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_078() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=Integer.MAX_VALUE, row=-1, column=Integer.MAX_VALUE
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, Integer.MAX_VALUE)).getEntry(-1, Integer.MAX_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_079() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=Integer.MIN_VALUE, row=-1, column=0
        try {
            (new OpenMapRealMatrix(0, Integer.MIN_VALUE)).getEntry(-1, 0);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_080() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=0, row=Integer.MAX_VALUE, column=Integer.MIN_VALUE
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, 0)).getEntry(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_081() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=0, row=Integer.MIN_VALUE, column=1
        try {
            (new OpenMapRealMatrix(1, 0)).getEntry(Integer.MIN_VALUE, 1);
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getEntry_pairwise_082() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=Integer.MAX_VALUE, row=Integer.MIN_VALUE, column=0
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, Integer.MAX_VALUE)).getEntry(Integer.MIN_VALUE, 0);
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_083() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=0
        try {
            (new OpenMapRealMatrix(0, 0)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_084() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=0
        try {
            (new OpenMapRealMatrix(1, 0)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_085() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=0
        try {
            (new OpenMapRealMatrix(-1, 0)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_086() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=0
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, 0)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_087() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=0
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, 0)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_088() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=1
        try {
            (new OpenMapRealMatrix(0, 1)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_089() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=1
        Object actual = (new OpenMapRealMatrix(1, 1)).getRowDimension();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_090() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=1
        try {
            (new OpenMapRealMatrix(-1, 1)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_091() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=1
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, 1)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_092() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=1
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, 1)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_093() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=-1
        try {
            (new OpenMapRealMatrix(0, -1)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_094() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=-1
        try {
            (new OpenMapRealMatrix(1, -1)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_095() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=-1
        try {
            (new OpenMapRealMatrix(-1, -1)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_096() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=-1
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, -1)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_097() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=-1
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, -1)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_098() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=Integer.MAX_VALUE
        try {
            (new OpenMapRealMatrix(0, Integer.MAX_VALUE)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_099() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=Integer.MAX_VALUE
        try {
            (new OpenMapRealMatrix(1, Integer.MAX_VALUE)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_100() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=Integer.MAX_VALUE
        try {
            (new OpenMapRealMatrix(-1, Integer.MAX_VALUE)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_101() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=Integer.MAX_VALUE
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, Integer.MAX_VALUE)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NumberIsTooLargeException");
        } catch (org.apache.commons.math.exception.NumberIsTooLargeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_102() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=Integer.MAX_VALUE
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, Integer.MAX_VALUE)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_103() throws Exception {
        // Combination: receiver__rowDimension=0, receiver__columnDimension=Integer.MIN_VALUE
        try {
            (new OpenMapRealMatrix(0, Integer.MIN_VALUE)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_104() throws Exception {
        // Combination: receiver__rowDimension=1, receiver__columnDimension=Integer.MIN_VALUE
        try {
            (new OpenMapRealMatrix(1, Integer.MIN_VALUE)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_105() throws Exception {
        // Combination: receiver__rowDimension=-1, receiver__columnDimension=Integer.MIN_VALUE
        try {
            (new OpenMapRealMatrix(-1, Integer.MIN_VALUE)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_106() throws Exception {
        // Combination: receiver__rowDimension=Integer.MAX_VALUE, receiver__columnDimension=Integer.MIN_VALUE
        try {
            (new OpenMapRealMatrix(Integer.MAX_VALUE, Integer.MIN_VALUE)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getRowDimension_pairwise_107() throws Exception {
        // Combination: receiver__rowDimension=Integer.MIN_VALUE, receiver__columnDimension=Integer.MIN_VALUE
        try {
            (new OpenMapRealMatrix(Integer.MIN_VALUE, Integer.MIN_VALUE)).getRowDimension();
            fail("Expected org.apache.commons.math.exception.NotStrictlyPositiveException");
        } catch (org.apache.commons.math.exception.NotStrictlyPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
