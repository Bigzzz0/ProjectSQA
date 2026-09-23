package org.apache.commons.math.stat.correlation;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for PearsonsCorrelation.
 */
public class PearsonsCorrelation_IPOTest {
    @Test(timeout = 4000)
    public void test_correlation_pairwise_001() throws Exception {
        // Combination: xArray=new double[] {}, yArray=new double[] {}
        try {
            (new PearsonsCorrelation()).correlation(new double[] {}, new double[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_correlation_pairwise_002() throws Exception {
        // Combination: xArray=new double[] {}, yArray=new double[] {1}
        try {
            (new PearsonsCorrelation()).correlation(new double[] {}, new double[] {1});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_correlation_pairwise_003() throws Exception {
        // Combination: xArray=new double[] {1}, yArray=new double[] {}
        try {
            (new PearsonsCorrelation()).correlation(new double[] {1}, new double[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_correlation_pairwise_004() throws Exception {
        // Combination: xArray=new double[] {1}, yArray=new double[] {1}
        try {
            (new PearsonsCorrelation()).correlation(new double[] {1}, new double[] {1});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
