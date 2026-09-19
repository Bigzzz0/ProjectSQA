package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for MathUtils.
 */
public class MathUtils_IPOTest {
    @Test(timeout = 4000)
    public void test_distance_pairwise_001() throws Exception {
        // Combination: p1=new int[] {}, p2=new int[] {}
        Object actual = MathUtils.distance(new int[] {}, new int[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_002() throws Exception {
        // Combination: p1=new int[] {}, p2=new int[] {1}
        Object actual = MathUtils.distance(new int[] {}, new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_003() throws Exception {
        // Combination: p1=new int[] {1}, p2=new int[] {}
        try {
            MathUtils.distance(new int[] {1}, new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_004() throws Exception {
        // Combination: p1=new int[] {1}, p2=new int[] {1}
        Object actual = MathUtils.distance(new int[] {1}, new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

}
