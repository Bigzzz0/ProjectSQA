package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for MultidimensionalCounter.
 */
public class MultidimensionalCounter_IPOTest {
    @Test(timeout = 4000)
    public void test_getCount_pairwise_001() throws Exception {
        // Combination: receiver__size=new int[] {}, c=new int[] {}
        try {
            (new MultidimensionalCounter(new int[] {})).getCount(new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCount_pairwise_002() throws Exception {
        // Combination: receiver__size=new int[] {1}, c=new int[] {}
        try {
            (new MultidimensionalCounter(new int[] {1})).getCount(new int[] {});
            fail("Expected org.apache.commons.math.exception.DimensionMismatchException");
        } catch (org.apache.commons.math.exception.DimensionMismatchException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCount_pairwise_003() throws Exception {
        // Combination: receiver__size=new int[] {}, c=new int[] {1}
        try {
            (new MultidimensionalCounter(new int[] {})).getCount(new int[] {1});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getCount_pairwise_004() throws Exception {
        // Combination: receiver__size=new int[] {1}, c=new int[] {1}
        try {
            (new MultidimensionalCounter(new int[] {1})).getCount(new int[] {1});
            fail("Expected org.apache.commons.math.exception.OutOfRangeException");
        } catch (org.apache.commons.math.exception.OutOfRangeException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
