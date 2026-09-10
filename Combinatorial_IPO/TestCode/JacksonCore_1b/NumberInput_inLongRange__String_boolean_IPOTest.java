package com.fasterxml.jackson.core.io;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for NumberInput.
 */
public class NumberInput_inLongRange__String_boolean_IPOTest {
    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_001() throws Exception {
        // Combination: numberStr=null, negative=true
        try {
            NumberInput.inLongRange(null, true);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_002() throws Exception {
        // Combination: numberStr=null, negative=false
        try {
            NumberInput.inLongRange(null, false);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_003() throws Exception {
        // Combination: numberStr="", negative=true
        Object actual = NumberInput.inLongRange("", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_004() throws Exception {
        // Combination: numberStr="", negative=false
        Object actual = NumberInput.inLongRange("", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_005() throws Exception {
        // Combination: numberStr=" ", negative=true
        Object actual = NumberInput.inLongRange(" ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_006() throws Exception {
        // Combination: numberStr=" ", negative=false
        Object actual = NumberInput.inLongRange(" ", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_007() throws Exception {
        // Combination: numberStr="a", negative=true
        Object actual = NumberInput.inLongRange("a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_008() throws Exception {
        // Combination: numberStr="a", negative=false
        Object actual = NumberInput.inLongRange("a", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_009() throws Exception {
        // Combination: numberStr="test123", negative=true
        Object actual = NumberInput.inLongRange("test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_010() throws Exception {
        // Combination: numberStr="test123", negative=false
        Object actual = NumberInput.inLongRange("test123", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_011() throws Exception {
        // Combination: numberStr="!@#", negative=true
        Object actual = NumberInput.inLongRange("!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_012() throws Exception {
        // Combination: numberStr="!@#", negative=false
        Object actual = NumberInput.inLongRange("!@#", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_013() throws Exception {
        // Combination: numberStr="0", negative=true
        Object actual = NumberInput.inLongRange("0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_014() throws Exception {
        // Combination: numberStr="0", negative=false
        Object actual = NumberInput.inLongRange("0", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_015() throws Exception {
        // Combination: numberStr="-1", negative=true
        Object actual = NumberInput.inLongRange("-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_016() throws Exception {
        // Combination: numberStr="-1", negative=false
        Object actual = NumberInput.inLongRange("-1", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_017() throws Exception {
        // Combination: numberStr="1.5", negative=true
        Object actual = NumberInput.inLongRange("1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_018() throws Exception {
        // Combination: numberStr="1.5", negative=false
        Object actual = NumberInput.inLongRange("1.5", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_019() throws Exception {
        // Combination: numberStr="9223372036854775807", negative=true
        Object actual = NumberInput.inLongRange("9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_020() throws Exception {
        // Combination: numberStr="9223372036854775807", negative=false
        Object actual = NumberInput.inLongRange("9223372036854775807", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_021() throws Exception {
        // Combination: numberStr="9223372036854775808", negative=true
        Object actual = NumberInput.inLongRange("9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_022() throws Exception {
        // Combination: numberStr="9223372036854775808", negative=false
        Object actual = NumberInput.inLongRange("9223372036854775808", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_023() throws Exception {
        // Combination: numberStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", negative=true
        Object actual = NumberInput.inLongRange("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_inLongRange_pairwise_024() throws Exception {
        // Combination: numberStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", negative=false
        Object actual = NumberInput.inLongRange("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

}
