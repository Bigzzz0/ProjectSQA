package org.apache.commons.lang3.time;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for FastDateFormat.
 */
public class FastDateFormat_IPOTest {
    @Test(timeout = 4000)
    public void test_parseToken_pairwise_001() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="", indexRef=new int[] {}
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_002() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, pattern=" ", indexRef=new int[] {}
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parseToken(" ", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_003() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, pattern="a", indexRef=new int[] {}
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parseToken("a", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_004() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.ROOT, pattern="test123", indexRef=new int[] {}
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.ROOT)).parseToken("test123", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_005() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="!@#", indexRef=new int[] {}
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("!@#", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_006() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="0", indexRef=new int[] {}
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("0", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_007() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="-1", indexRef=new int[] {}
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("-1", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_008() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="1.5", indexRef=new int[] {}
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("1.5", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_009() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775807", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775807", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_010() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775808", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775808", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_011() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", indexRef=new int[] {}
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_012() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, pattern="", indexRef=new int[] {1}
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parseToken("", new int[] {1});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_013() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern=" ", indexRef=new int[] {1}
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken(" ", new int[] {1});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_014() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="a", indexRef=new int[] {1}
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("a", new int[] {1});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_015() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.JAPAN, pattern="test123", indexRef=new int[] {1}
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.JAPAN)).parseToken("test123", new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("e", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_016() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US, pattern="!@#", indexRef=new int[] {1}
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).parseToken("!@#", new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("'@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_017() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, pattern="0", indexRef=new int[] {1}
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parseToken("0", new int[] {1});
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_018() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, pattern="-1", indexRef=new int[] {1}
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parseToken("-1", new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("'1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_019() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, pattern="1.5", indexRef=new int[] {1}
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parseToken("1.5", new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("'.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_020() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, pattern="9223372036854775807", indexRef=new int[] {1}
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parseToken("9223372036854775807", new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("'223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_021() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, pattern="9223372036854775808", indexRef=new int[] {1}
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parseToken("9223372036854775808", new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("'223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_022() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", indexRef=new int[] {1}
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parseToken("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_023() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, pattern="a", indexRef=new int[] {}
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).parseToken("a", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_024() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US, pattern="test123", indexRef=new int[] {}
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).parseToken("test123", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_025() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, pattern="", indexRef=new int[] {}
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parseToken("", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_026() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, pattern=" ", indexRef=new int[] {}
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parseToken(" ", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_027() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.JAPAN, pattern="!@#", indexRef=new int[] {}
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.JAPAN)).parseToken("!@#", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_028() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, pattern="0", indexRef=new int[] {}
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parseToken("0", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_029() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, pattern="-1", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parseToken("-1", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_030() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, pattern="1.5", indexRef=new int[] {}
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parseToken("1.5", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_031() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, pattern="9223372036854775807", indexRef=new int[] {}
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parseToken("9223372036854775807", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_032() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, pattern="9223372036854775808", indexRef=new int[] {}
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parseToken("9223372036854775808", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_033() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", indexRef=new int[] {}
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parseToken("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_034() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="0", indexRef=new int[] {}
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("0", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_035() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="-1", indexRef=new int[] {}
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("-1", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_036() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="1.5", indexRef=new int[] {}
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("1.5", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_037() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775807", indexRef=new int[] {}
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775807", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_038() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="test123", indexRef=new int[] {}
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("test123", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_039() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="!@#", indexRef=new int[] {}
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("!@#", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_040() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="0", indexRef=new int[] {}
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("0", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_041() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="-1", indexRef=new int[] {}
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("-1", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_042() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="1.5", indexRef=new int[] {}
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("1.5", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_043() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775807", indexRef=new int[] {}
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775807", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_044() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775808", indexRef=new int[] {}
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775808", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_045() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", indexRef=new int[] {}
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_046() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern=" ", indexRef=new int[] {}
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken(" ", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_047() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="test123", indexRef=new int[] {}
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("test123", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_048() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="!@#", indexRef=new int[] {}
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("!@#", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_049() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="0", indexRef=new int[] {}
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("0", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_050() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="-1", indexRef=new int[] {}
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("-1", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_051() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="1.5", indexRef=new int[] {}
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("1.5", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_052() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775807", indexRef=new int[] {}
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775807", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_053() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775808", indexRef=new int[] {}
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775808", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_054() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", indexRef=new int[] {}
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_055() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="", indexRef=new int[] {}
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_056() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="a", indexRef=new int[] {}
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("a", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_057() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="0", indexRef=new int[] {}
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("0", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_058() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="-1", indexRef=new int[] {}
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("-1", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_059() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="1.5", indexRef=new int[] {}
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("1.5", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_060() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775807", indexRef=new int[] {}
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775807", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_061() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775808", indexRef=new int[] {}
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775808", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_062() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", indexRef=new int[] {}
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_063() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US, pattern="", indexRef=new int[] {}
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).parseToken("", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_064() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern=" ", indexRef=new int[] {}
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken(" ", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_065() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="a", indexRef=new int[] {}
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("a", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_066() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="0", indexRef=new int[] {}
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("0", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_067() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="-1", indexRef=new int[] {}
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("-1", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_068() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="1.5", indexRef=new int[] {}
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("1.5", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_069() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775807", indexRef=new int[] {}
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775807", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_070() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775808", indexRef=new int[] {}
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775808", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_071() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", indexRef=new int[] {}
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_072() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="", indexRef=new int[] {}
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_073() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern=" ", indexRef=new int[] {}
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken(" ", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_074() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="a", indexRef=new int[] {}
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("a", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_075() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="test123", indexRef=new int[] {}
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("test123", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_076() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="!@#", indexRef=new int[] {}
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("!@#", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_077() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775807", indexRef=new int[] {}
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775807", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_078() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775808", indexRef=new int[] {}
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775808", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_079() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", indexRef=new int[] {}
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_080() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="", indexRef=new int[] {}
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_081() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern=" ", indexRef=new int[] {}
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken(" ", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_082() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="a", indexRef=new int[] {}
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("a", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_083() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="test123", indexRef=new int[] {}
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("test123", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_084() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="!@#", indexRef=new int[] {}
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("!@#", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_085() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="1.5", indexRef=new int[] {}
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("1.5", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_086() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775808", indexRef=new int[] {}
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775808", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_087() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", indexRef=new int[] {}
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_088() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="", indexRef=new int[] {}
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_089() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern=" ", indexRef=new int[] {}
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken(" ", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_090() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="a", indexRef=new int[] {}
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("a", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_091() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="test123", indexRef=new int[] {}
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("test123", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_092() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="!@#", indexRef=new int[] {}
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("!@#", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_093() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="-1", indexRef=new int[] {}
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("-1", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_094() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775808", indexRef=new int[] {}
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775808", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_095() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", indexRef=new int[] {}
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_096() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_097() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern=" ", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken(" ", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_098() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="a", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("a", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_099() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="test123", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("test123", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_100() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="!@#", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("!@#", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_101() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="0", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("0", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_102() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775808", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775808", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_103() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_104() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, pattern="", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parseToken("", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_105() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern=" ", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken(" ", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_106() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="a", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("a", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_107() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="test123", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("test123", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_108() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="!@#", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("!@#", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_109() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="0", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("0", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_110() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="-1", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("-1", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_111() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="1.5", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("1.5", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_112() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775807", indexRef=new int[] {}
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775807", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_113() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, pattern="", indexRef=new int[] {}
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).parseToken("", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_114() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern=" ", indexRef=new int[] {}
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken(" ", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_115() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="a", indexRef=new int[] {}
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("a", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_116() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="test123", indexRef=new int[] {}
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("test123", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_117() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="!@#", indexRef=new int[] {}
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("!@#", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_118() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="0", indexRef=new int[] {}
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("0", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_119() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="-1", indexRef=new int[] {}
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("-1", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_120() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="1.5", indexRef=new int[] {}
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("1.5", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_parseToken_pairwise_121() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, pattern="9223372036854775807", indexRef=new int[] {}
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).parseToken("9223372036854775807", new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_122() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=0L
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(0L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_123() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, millis=0L
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(0L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_124() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, millis=0L
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(0L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_125() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=1L
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_126() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, millis=1L
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_127() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.JAPAN, millis=1L
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.JAPAN)).format(1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_128() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.ROOT, millis=-1L
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.ROOT)).format(-1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_129() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US, millis=-1L
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).format(-1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_130() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, millis=-1L
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(-1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_131() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MAX_VALUE
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_132() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, millis=Long.MAX_VALUE
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(Long.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_133() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, millis=Long.MAX_VALUE
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(Long.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_134() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MIN_VALUE
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_135() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, millis=Long.MIN_VALUE
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(Long.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_136() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, millis=Long.MIN_VALUE
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(Long.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_137() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MAX_VALUE
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_138() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MIN_VALUE
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_139() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=-1L
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(-1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_140() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MIN_VALUE
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_141() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=1L
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_142() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MIN_VALUE
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_143() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=0L
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(0L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_144() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MIN_VALUE
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_145() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, millis=0L
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(0L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_146() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, millis=1L
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_147() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=-1L
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(-1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_148() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MAX_VALUE
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_149() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=0L
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(0L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_150() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, millis=1L
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_151() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=-1L
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(-1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_152() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MAX_VALUE
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_153() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.ROOT, millis=0L
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.ROOT)).format(0L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_154() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US, millis=1L
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).format(1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_155() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=-1L
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(-1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_156() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MAX_VALUE
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_157() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=0L
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(0L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_158() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, millis=1L
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_159() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, millis=-1L
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(-1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_160() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MAX_VALUE
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_161() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MIN_VALUE
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_162() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=0L
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(0L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_163() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, millis=1L
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_164() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, millis=-1L
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(-1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_165() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MAX_VALUE
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_166() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MIN_VALUE
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_167() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=0L
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(0L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_168() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, millis=1L
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_169() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, millis=-1L
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(-1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_170() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MAX_VALUE
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_171() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MIN_VALUE
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_172() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=0L
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(0L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_173() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, millis=1L
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_174() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, millis=-1L
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(-1L);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_175() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MAX_VALUE
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_176() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, millis=Long.MIN_VALUE
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(Long.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_177() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_178() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, date=new java.util.Date(0L)
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_179() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_180() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, date=new java.util.Date(1000000000000L)
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(new java.util.Date(1000000000000L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_181() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, date=new java.util.Date(1000000000000L)
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(new java.util.Date(1000000000000L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_182() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.JAPAN, date=new java.util.Date(1000000000000L)
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.JAPAN)).format(new java.util.Date(1000000000000L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_183() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_184() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, date=new java.util.Date(0L)
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_185() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.ROOT, date=new java.util.Date(1000000000000L)
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.ROOT)).format(new java.util.Date(1000000000000L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_186() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_187() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_188() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_189() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_190() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, date=new java.util.Date(1000000000000L)
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(new java.util.Date(1000000000000L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_191() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_192() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_193() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, date=new java.util.Date(1000000000000L)
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(new java.util.Date(1000000000000L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_194() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_195() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_196() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, date=new java.util.Date(1000000000000L)
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(new java.util.Date(1000000000000L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_197() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_198() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_199() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, date=new java.util.Date(1000000000000L)
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(new java.util.Date(1000000000000L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_200() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_201() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_202() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, date=new java.util.Date(1000000000000L)
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(new java.util.Date(1000000000000L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_203() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_204() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_205() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, date=new java.util.Date(1000000000000L)
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(new java.util.Date(1000000000000L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_206() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_207() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_208() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, date=new java.util.Date(1000000000000L)
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(new java.util.Date(1000000000000L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_209() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, date=new java.util.Date(0L)
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(new java.util.Date(0L));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_210() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_211() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_212() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_213() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_214() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.JAPAN, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.JAPAN)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_215() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_216() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_217() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.ROOT, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.ROOT)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_218() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_219() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_220() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_221() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_222() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_223() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_224() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_225() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_226() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_227() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_228() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_229() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_230() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_231() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_232() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_233() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_234() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_235() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_236() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_237() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_238() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_239() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_240() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_241() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_format_pairwise_242() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, calendar=java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
        try {
            (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).format(java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")));
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_243() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_244() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_245() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_246() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_247() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_248() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_249() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_250() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_251() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_252() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_253() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_254() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_255() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_256() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_257() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_258() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_259() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_260() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_261() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_262() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_263() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_264() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_265() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_266() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_267() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_268() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_269() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_270() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_271() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_272() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_273() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_274() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getPattern_pairwise_275() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getPattern();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_276() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_277() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.ROOT)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_278() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_279() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_280() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_281() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_282() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_283() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_284() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_285() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_286() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_287() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_288() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_289() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_290() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_291() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_292() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_293() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_294() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_295() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_296() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_297() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_298() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_299() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.JAPAN)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_300() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_301() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_302() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_303() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_304() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_305() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_306() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_307() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getTimeZoneOverridesCalendar_pairwise_308() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getTimeZoneOverridesCalendar();
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_309() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_310() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.ROOT)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_311() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_312() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_313() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_314() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_315() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_316() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_317() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_318() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_319() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_320() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_321() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_322() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_323() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_324() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_325() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_326() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_327() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_328() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_329() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_330() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_331() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_332() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.JAPAN)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_333() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_334() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_335() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_336() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_337() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_338() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_339() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_340() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_getMaxLengthEstimate_pairwise_341() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).getMaxLengthEstimate();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_342() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_343() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj=new Object()
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_344() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=new Object()
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_345() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj="sample_str"
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_346() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_347() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.JAPAN, obj="sample_str"
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.JAPAN)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_348() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.ROOT, obj=Integer.valueOf(1)
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.ROOT)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_349() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US, obj=Integer.valueOf(1)
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_350() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_351() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_352() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_353() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_354() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_355() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_356() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_357() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_358() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_359() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_360() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_361() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_362() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_363() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_364() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_365() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_366() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_367() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_368() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_369() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_370() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_371() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_372() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT, obj=new Object()
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).equals(new Object());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_373() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US, obj="sample_str"
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).equals("sample_str");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_374() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN, obj=Integer.valueOf(1)
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).equals(Integer.valueOf(1));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_375() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_376() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("34", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_377() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("99", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_378() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1422501790", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_379() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("33734", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_380() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("50", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_381() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1446", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_382() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("48570", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_383() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1773151196", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_384() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1773151195", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_385() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-474938878", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_386() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("96636891", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_387() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("96636923", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_388() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("96636988", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_389() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1325864901", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_390() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("96670623", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_391() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("96636939", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_392() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("96638335", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_393() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("96685459", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_394() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1676514307", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_395() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1676514306", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_396() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-378301989", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_397() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("100856549", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_398() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("100856581", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_399() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("100856646", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_400() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1321645243", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_401() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("100890281", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_402() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("100856597", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_403() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("100857993", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_404() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("100905117", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_405() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1672294649", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_406() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1672294648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hashCode_pairwise_407() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).hashCode();
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-374082331", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_408() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_409() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[ ]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_410() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[a]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_411() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[test123]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_412() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[!@#]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_413() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[0]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_414() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[-1]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_415() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[1.5]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_416() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[9223372036854775807]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_417() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[9223372036854775808]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_418() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.ROOT
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.ROOT)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_419() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_420() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[ ]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_421() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[a]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_422() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[test123]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_423() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[!@#]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_424() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[0]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_425() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[-1]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_426() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[1.5]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_427() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[9223372036854775807]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_428() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[9223372036854775808]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_429() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.US
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.US)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_430() throws Exception {
        // Combination: receiver__pattern="", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_431() throws Exception {
        // Combination: receiver__pattern=" ", receiver__timeZone=java.util.TimeZone.getTimeZone("GMT"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat(" ", java.util.TimeZone.getTimeZone("GMT"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[ ]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_432() throws Exception {
        // Combination: receiver__pattern="a", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("a", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[a]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_433() throws Exception {
        // Combination: receiver__pattern="test123", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("test123", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[test123]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_434() throws Exception {
        // Combination: receiver__pattern="!@#", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("!@#", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[!@#]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_435() throws Exception {
        // Combination: receiver__pattern="0", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("0", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[0]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_436() throws Exception {
        // Combination: receiver__pattern="-1", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("-1", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[-1]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_437() throws Exception {
        // Combination: receiver__pattern="1.5", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("1.5", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[1.5]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_438() throws Exception {
        // Combination: receiver__pattern="9223372036854775807", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("9223372036854775807", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[9223372036854775807]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_439() throws Exception {
        // Combination: receiver__pattern="9223372036854775808", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("9223372036854775808", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[9223372036854775808]", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_toString_pairwise_440() throws Exception {
        // Combination: receiver__pattern="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__timeZone=java.util.TimeZone.getTimeZone("UTC"), receiver__locale=java.util.Locale.JAPAN
        Object actual = (new FastDateFormat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.TimeZone.getTimeZone("UTC"), java.util.Locale.JAPAN)).toString();
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("FastDateFormat[aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa]", String.valueOf(actual));
    }

}
