package org.apache.commons.lang3.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for FastDateParser.
 */
public class FastDateParser_IPOTest {
    @Test(timeout = 4000)
    public void test_getPattern_pairwise_001() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_002() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_003() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_004() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_005() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_006() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_007() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_008() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_009() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_010() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_011() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_012() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_013() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_014() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("a", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_015() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_016() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_017() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("0", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_018() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("-1", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_019() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_020() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_021() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_022() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_023() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_024() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_025() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_026() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_027() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_028() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_029() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_030() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_031() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_032() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_033() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_034() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_035() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj=new Object()
        Object actual = (new FastDateParser(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_036() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=new Object()
        Object actual = (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_037() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj="sample_str"
        Object actual = (new FastDateParser(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_038() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj="sample_str"
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_039() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.JAPAN, obj="sample_str"
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.JAPAN)).equals("sample_str");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_040() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.ROOT, obj=Integer.valueOf(1)
        Object actual = (new FastDateParser("a", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.ROOT)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_041() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US, obj=Integer.valueOf(1)
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_042() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_043() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateParser(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_044() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_045() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_046() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        Object actual = (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_047() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_048() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_049() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        Object actual = (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_050() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateParser("0", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_051() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_052() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        Object actual = (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_053() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateParser("-1", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_054() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_055() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        Object actual = (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_056() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_057() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_058() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        Object actual = (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_059() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_060() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_061() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        Object actual = (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_062() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_063() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_064() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        Object actual = (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_065() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_066() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_067() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_068() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("32", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_069() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("97", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_070() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_071() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("33732", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_072() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("48", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_073() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1444", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_074() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("48568", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_075() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1773151198", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_076() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1773151197", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_077() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-474938880", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_078() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_079() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-848234911", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_080() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("a", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-848234846", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_081() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_082() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-848201211", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_083() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("0", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-848234895", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_084() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("-1", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-848233499", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_085() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-848186375", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_086() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1673581155", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_087() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1673581156", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_088() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1323173823", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_089() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_090() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-135112709", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_091() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-135112644", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_092() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_093() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-135079009", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_094() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-135112693", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_095() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-135111297", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_096() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-135064173", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_097() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1908263939", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_098() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1908263938", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_099() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-610051621", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_100() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_101() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[ ,,GMT]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_102() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[a,,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_103() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_104() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[!@#,,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_105() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[0,,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_106() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[-1,,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_107() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[1.5,,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_108() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[9223372036854775807,,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_109() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[9223372036854775808,,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_110() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_111() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_112() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[ ,en_US,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_113() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("a", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[a,en_US,GMT]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_114() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_115() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[!@#,en_US,GMT]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_116() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("0", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[0,en_US,GMT]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_117() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("-1", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[-1,en_US,GMT]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_118() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[1.5,en_US,GMT]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_119() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[9223372036854775807,en_US,GMT]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_120() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[9223372036854775808,en_US,GMT]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_121() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,en_US,GMT]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_122() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_123() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[ ,ja_JP,GMT]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_124() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[a,ja_JP,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_125() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_126() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[!@#,ja_JP,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_127() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[0,ja_JP,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_128() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[-1,ja_JP,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_129() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[1.5,ja_JP,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_130() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[9223372036854775807,ja_JP,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_131() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[9223372036854775808,ja_JP,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_132() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateParser[aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa,ja_JP,UTC]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_133() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source=""
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_134() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.ROOT, source=" "
        Object actual = (new FastDateParser(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.ROOT)).parse(" ");
        assertNotNull(actual);
        assertEquals("java.util.Date", actual.getClass().getName());
        assertEquals("Thu Jan 01 00:00:00 UTC 1970", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_135() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="a"
        try {
            (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("a");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_136() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="test123"
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_137() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="!@#"
        Object actual = (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("!@#");
        assertNotNull(actual);
        assertEquals("java.util.Date", actual.getClass().getName());
        assertEquals("Thu Jan 01 00:00:00 UTC 1970", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_138() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="0"
        Object actual = (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("0");
        assertNotNull(actual);
        assertEquals("java.util.Date", actual.getClass().getName());
        assertEquals("Thu Jan 01 00:00:00 UTC 1970", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_139() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="-1"
        Object actual = (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("-1");
        assertNotNull(actual);
        assertEquals("java.util.Date", actual.getClass().getName());
        assertEquals("Thu Jan 01 00:00:00 UTC 1970", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_140() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="1.5"
        Object actual = (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("1.5");
        assertNotNull(actual);
        assertEquals("java.util.Date", actual.getClass().getName());
        assertEquals("Thu Jan 01 00:00:00 UTC 1970", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_141() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775807"
        Object actual = (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.util.Date", actual.getClass().getName());
        assertEquals("Thu Jan 01 00:00:00 UTC 1970", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_142() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775808"
        Object actual = (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.util.Date", actual.getClass().getName());
        assertEquals("Thu Jan 01 00:00:00 UTC 1970", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_143() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_144() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, source="a"
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parse("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_145() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US, source=""
        try {
            (new FastDateParser(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).parse("");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_146() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, source="test123"
        try {
            (new FastDateParser("a", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parse("test123");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_147() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, source="!@#"
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parse("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_148() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, source="0"
        try {
            (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parse("0");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_149() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, source="-1"
        try {
            (new FastDateParser("0", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parse("-1");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_150() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, source="1.5"
        try {
            (new FastDateParser("-1", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parse("1.5");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_151() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, source="9223372036854775807"
        try {
            (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parse("9223372036854775807");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_152() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, source="9223372036854775808"
        try {
            (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parse("9223372036854775808");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_153() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, source="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_154() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, source=""
        try {
            (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parse("");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_155() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, source=" "
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parse(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_156() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.JAPAN, source="a"
        try {
            (new FastDateParser(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.JAPAN)).parse("a");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_157() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, source=""
        try {
            (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parse("");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_158() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, source="0"
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parse("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_159() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, source="test123"
        try {
            (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parse("test123");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_160() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, source="!@#"
        try {
            (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parse("!@#");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_161() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, source="9223372036854775807"
        try {
            (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parse("9223372036854775807");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_162() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, source="-1"
        try {
            (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parse("-1");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_163() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, source="1.5"
        try {
            (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parse("1.5");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_164() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, source=""
        try {
            (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parse("");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_165() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, source="9223372036854775808"
        try {
            (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parse("9223372036854775808");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_166() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source=""
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_167() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source=""
        try {
            (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_168() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source=""
        try {
            (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_169() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source=""
        try {
            (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_170() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source=""
        try {
            (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_171() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source=""
        try {
            (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_172() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US, source=" "
        try {
            (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).parse(" ");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_173() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source=" "
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_174() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source=" "
        try {
            (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse(" ");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_175() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source=" "
        try {
            (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse(" ");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_176() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source=" "
        try {
            (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse(" ");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_177() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source=" "
        try {
            (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse(" ");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_178() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source=" "
        try {
            (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse(" ");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_179() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source=" "
        try {
            (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse(" ");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_180() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source=" "
        try {
            (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse(" ");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_181() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="a"
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_182() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="a"
        try {
            (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("a");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_183() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="a"
        try {
            (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("a");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_184() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="a"
        try {
            (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("a");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_185() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="a"
        try {
            (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("a");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_186() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="a"
        try {
            (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("a");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_187() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="a"
        try {
            (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("a");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_188() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="a"
        try {
            (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("a");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_189() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="test123"
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_190() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="test123"
        try {
            (new FastDateParser(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("test123");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_191() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="test123"
        try {
            (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("test123");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_192() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="test123"
        try {
            (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("test123");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_193() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="test123"
        try {
            (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("test123");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_194() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="test123"
        try {
            (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("test123");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_195() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="test123"
        try {
            (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("test123");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_196() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="test123"
        try {
            (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("test123");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_197() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="!@#"
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_198() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="!@#"
        try {
            (new FastDateParser(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("!@#");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_199() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="!@#"
        try {
            (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("!@#");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_200() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="!@#"
        try {
            (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("!@#");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_201() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="!@#"
        try {
            (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("!@#");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_202() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="!@#"
        try {
            (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("!@#");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_203() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="!@#"
        try {
            (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("!@#");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_204() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="!@#"
        try {
            (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("!@#");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_205() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="0"
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_206() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="0"
        try {
            (new FastDateParser(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("0");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_207() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="0"
        try {
            (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("0");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_208() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="0"
        try {
            (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("0");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_209() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="0"
        try {
            (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("0");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_210() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="0"
        try {
            (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("0");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_211() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="0"
        try {
            (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("0");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_212() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="0"
        try {
            (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("0");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_213() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="-1"
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_214() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="-1"
        try {
            (new FastDateParser(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("-1");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_215() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="-1"
        try {
            (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("-1");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_216() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="-1"
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_217() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="-1"
        try {
            (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("-1");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_218() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="-1"
        try {
            (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("-1");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_219() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="-1"
        try {
            (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("-1");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_220() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="-1"
        try {
            (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("-1");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_221() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="1.5"
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_222() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="1.5"
        try {
            (new FastDateParser(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("1.5");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_223() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="1.5"
        try {
            (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("1.5");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_224() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="1.5"
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_225() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="1.5"
        try {
            (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("1.5");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_226() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="1.5"
        try {
            (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("1.5");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_227() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="1.5"
        try {
            (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("1.5");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_228() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="1.5"
        try {
            (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("1.5");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_229() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775807"
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_230() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775807"
        try {
            (new FastDateParser(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775807");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_231() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775807"
        try {
            (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775807");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_232() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775807"
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_233() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775807"
        try {
            (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775807");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_234() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775807"
        try {
            (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775807");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_235() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775807"
        try {
            (new FastDateParser("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775807");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_236() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775807"
        try {
            (new FastDateParser("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775807");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_237() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775808"
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_238() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775808"
        try {
            (new FastDateParser(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775808");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_239() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775808"
        try {
            (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775808");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_240() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775808"
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_241() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775808"
        try {
            (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775808");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_242() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775808"
        try {
            (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775808");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_243() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775808"
        try {
            (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775808");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_244() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="9223372036854775808"
        try {
            (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("9223372036854775808");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_245() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, source="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new FastDateParser("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_246() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new FastDateParser(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_247() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new FastDateParser("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_248() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new FastDateParser("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_249() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new FastDateParser("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_250() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new FastDateParser("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_251() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new FastDateParser("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_252() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new FastDateParser("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parse_pairwise_253() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, source="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new FastDateParser("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parse("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.text.ParseException");
        } catch (java.text.ParseException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
