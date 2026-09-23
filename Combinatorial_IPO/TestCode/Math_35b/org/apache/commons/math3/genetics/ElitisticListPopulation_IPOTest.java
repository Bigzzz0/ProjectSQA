package org.apache.commons.math3.genetics;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for ElitisticListPopulation.
 */
public class ElitisticListPopulation_IPOTest {
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
    public void test_getElitismRate_pairwise_001() throws Exception {
        // Combination: receiver__populationLimit=0, receiver__elitismRate=0.0d
        try {
            (new ElitisticListPopulation(0, 0.0d)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_002() throws Exception {
        // Combination: receiver__populationLimit=1, receiver__elitismRate=0.0d
        Object actual = (new ElitisticListPopulation(1, 0.0d)).getElitismRate();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_003() throws Exception {
        // Combination: receiver__populationLimit=-1, receiver__elitismRate=0.0d
        try {
            (new ElitisticListPopulation(-1, 0.0d)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_004() throws Exception {
        // Combination: receiver__populationLimit=Integer.MAX_VALUE, receiver__elitismRate=0.0d
        try {
            (new ElitisticListPopulation(Integer.MAX_VALUE, 0.0d)).getElitismRate();
            fail("Expected java.lang.OutOfMemoryError");
        } catch (java.lang.OutOfMemoryError expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_005() throws Exception {
        // Combination: receiver__populationLimit=Integer.MIN_VALUE, receiver__elitismRate=0.0d
        try {
            (new ElitisticListPopulation(Integer.MIN_VALUE, 0.0d)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_006() throws Exception {
        // Combination: receiver__populationLimit=0, receiver__elitismRate=1.0d
        try {
            (new ElitisticListPopulation(0, 1.0d)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_007() throws Exception {
        // Combination: receiver__populationLimit=1, receiver__elitismRate=1.0d
        Object actual = (new ElitisticListPopulation(1, 1.0d)).getElitismRate();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_008() throws Exception {
        // Combination: receiver__populationLimit=-1, receiver__elitismRate=1.0d
        try {
            (new ElitisticListPopulation(-1, 1.0d)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_009() throws Exception {
        // Combination: receiver__populationLimit=Integer.MAX_VALUE, receiver__elitismRate=1.0d
        try {
            (new ElitisticListPopulation(Integer.MAX_VALUE, 1.0d)).getElitismRate();
            fail("Expected java.lang.OutOfMemoryError");
        } catch (java.lang.OutOfMemoryError expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_010() throws Exception {
        // Combination: receiver__populationLimit=Integer.MIN_VALUE, receiver__elitismRate=1.0d
        try {
            (new ElitisticListPopulation(Integer.MIN_VALUE, 1.0d)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_011() throws Exception {
        // Combination: receiver__populationLimit=0, receiver__elitismRate=-1.0d
        try {
            (new ElitisticListPopulation(0, -1.0d)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_012() throws Exception {
        // Combination: receiver__populationLimit=1, receiver__elitismRate=-1.0d
        try {
            (new ElitisticListPopulation(1, -1.0d)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.OutOfRangeException");
        } catch (org.apache.commons.math3.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_013() throws Exception {
        // Combination: receiver__populationLimit=-1, receiver__elitismRate=-1.0d
        try {
            (new ElitisticListPopulation(-1, -1.0d)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_014() throws Exception {
        // Combination: receiver__populationLimit=Integer.MAX_VALUE, receiver__elitismRate=-1.0d
        try {
            (new ElitisticListPopulation(Integer.MAX_VALUE, -1.0d)).getElitismRate();
            fail("Expected java.lang.OutOfMemoryError");
        } catch (java.lang.OutOfMemoryError expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_015() throws Exception {
        // Combination: receiver__populationLimit=Integer.MIN_VALUE, receiver__elitismRate=-1.0d
        try {
            (new ElitisticListPopulation(Integer.MIN_VALUE, -1.0d)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_016() throws Exception {
        // Combination: receiver__populationLimit=0, receiver__elitismRate=Double.NaN
        try {
            (new ElitisticListPopulation(0, Double.NaN)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_017() throws Exception {
        // Combination: receiver__populationLimit=1, receiver__elitismRate=Double.NaN
        Object actual = (new ElitisticListPopulation(1, Double.NaN)).getElitismRate();
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_018() throws Exception {
        // Combination: receiver__populationLimit=-1, receiver__elitismRate=Double.NaN
        try {
            (new ElitisticListPopulation(-1, Double.NaN)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_019() throws Exception {
        // Combination: receiver__populationLimit=Integer.MAX_VALUE, receiver__elitismRate=Double.NaN
        try {
            (new ElitisticListPopulation(Integer.MAX_VALUE, Double.NaN)).getElitismRate();
            fail("Expected java.lang.OutOfMemoryError");
        } catch (java.lang.OutOfMemoryError expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_020() throws Exception {
        // Combination: receiver__populationLimit=Integer.MIN_VALUE, receiver__elitismRate=Double.NaN
        try {
            (new ElitisticListPopulation(Integer.MIN_VALUE, Double.NaN)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_021() throws Exception {
        // Combination: receiver__populationLimit=0, receiver__elitismRate=Double.POSITIVE_INFINITY
        try {
            (new ElitisticListPopulation(0, Double.POSITIVE_INFINITY)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_022() throws Exception {
        // Combination: receiver__populationLimit=1, receiver__elitismRate=Double.POSITIVE_INFINITY
        try {
            (new ElitisticListPopulation(1, Double.POSITIVE_INFINITY)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.OutOfRangeException");
        } catch (org.apache.commons.math3.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_023() throws Exception {
        // Combination: receiver__populationLimit=-1, receiver__elitismRate=Double.POSITIVE_INFINITY
        try {
            (new ElitisticListPopulation(-1, Double.POSITIVE_INFINITY)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_024() throws Exception {
        // Combination: receiver__populationLimit=Integer.MAX_VALUE, receiver__elitismRate=Double.POSITIVE_INFINITY
        try {
            (new ElitisticListPopulation(Integer.MAX_VALUE, Double.POSITIVE_INFINITY)).getElitismRate();
            fail("Expected java.lang.OutOfMemoryError");
        } catch (java.lang.OutOfMemoryError expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getElitismRate_pairwise_025() throws Exception {
        // Combination: receiver__populationLimit=Integer.MIN_VALUE, receiver__elitismRate=Double.POSITIVE_INFINITY
        try {
            (new ElitisticListPopulation(Integer.MIN_VALUE, Double.POSITIVE_INFINITY)).getElitismRate();
            fail("Expected org.apache.commons.math3.exception.NotPositiveException");
        } catch (org.apache.commons.math3.exception.NotPositiveException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
