package org.joda.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for LocalDate.
 */
public class LocalDate_IPOTest {
    @Test(timeout = 4000)
    public void test_toString_pairwise_001() throws Exception {
        // Combination: pattern="", locale=java.util.Locale.ROOT
        try {
            (new LocalDate()).toString("", java.util.Locale.ROOT);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_002() throws Exception {
        // Combination: pattern=" ", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString(" ", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_003() throws Exception {
        // Combination: pattern="a", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString("a", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\ufffd", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_004() throws Exception {
        // Combination: pattern="test123", locale=java.util.Locale.ROOT
        try {
            (new LocalDate()).toString("test123", java.util.Locale.ROOT);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_005() throws Exception {
        // Combination: pattern="!@#", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString("!@#", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_006() throws Exception {
        // Combination: pattern="0", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString("0", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_007() throws Exception {
        // Combination: pattern="-1", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString("-1", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_008() throws Exception {
        // Combination: pattern="1.5", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString("1.5", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_009() throws Exception {
        // Combination: pattern="9223372036854775807", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString("9223372036854775807", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_010() throws Exception {
        // Combination: pattern="9223372036854775808", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString("9223372036854775808", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_011() throws Exception {
        // Combination: pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", locale=java.util.Locale.ROOT
        Object actual = (new LocalDate()).toString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.Locale.ROOT);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\ufffd", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_012() throws Exception {
        // Combination: pattern="", locale=java.util.Locale.US
        try {
            (new LocalDate()).toString("", java.util.Locale.US);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_013() throws Exception {
        // Combination: pattern=" ", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString(" ", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_014() throws Exception {
        // Combination: pattern="a", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString("a", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\ufffd", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_015() throws Exception {
        // Combination: pattern="test123", locale=java.util.Locale.US
        try {
            (new LocalDate()).toString("test123", java.util.Locale.US);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_016() throws Exception {
        // Combination: pattern="!@#", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString("!@#", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_017() throws Exception {
        // Combination: pattern="0", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString("0", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_018() throws Exception {
        // Combination: pattern="-1", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString("-1", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_019() throws Exception {
        // Combination: pattern="1.5", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString("1.5", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_020() throws Exception {
        // Combination: pattern="9223372036854775807", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString("9223372036854775807", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_021() throws Exception {
        // Combination: pattern="9223372036854775808", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString("9223372036854775808", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_022() throws Exception {
        // Combination: pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", locale=java.util.Locale.US
        Object actual = (new LocalDate()).toString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.Locale.US);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\ufffd", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_023() throws Exception {
        // Combination: pattern="", locale=java.util.Locale.JAPAN
        try {
            (new LocalDate()).toString("", java.util.Locale.JAPAN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_024() throws Exception {
        // Combination: pattern=" ", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString(" ", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_025() throws Exception {
        // Combination: pattern="a", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString("a", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\ufffd", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_026() throws Exception {
        // Combination: pattern="test123", locale=java.util.Locale.JAPAN
        try {
            (new LocalDate()).toString("test123", java.util.Locale.JAPAN);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_027() throws Exception {
        // Combination: pattern="!@#", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString("!@#", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_028() throws Exception {
        // Combination: pattern="0", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString("0", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_029() throws Exception {
        // Combination: pattern="-1", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString("-1", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_030() throws Exception {
        // Combination: pattern="1.5", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString("1.5", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_031() throws Exception {
        // Combination: pattern="9223372036854775807", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString("9223372036854775807", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_032() throws Exception {
        // Combination: pattern="9223372036854775808", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString("9223372036854775808", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_033() throws Exception {
        // Combination: pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", locale=java.util.Locale.JAPAN
        Object actual = (new LocalDate()).toString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.Locale.JAPAN);
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\ufffd", String.valueOf(actual));
    }

}
