package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for WordUtils.
 */
public class WordUtils_IPOTest {
    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_001() throws Exception {
        // Combination: str="", lower=0, upper=0, appendToEnd=""
        Object actual = WordUtils.abbreviate("", 0, 0, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_002() throws Exception {
        // Combination: str=" ", lower=1, upper=1, appendToEnd=""
        Object actual = WordUtils.abbreviate(" ", 1, 1, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_003() throws Exception {
        // Combination: str="a", lower=-1, upper=-1, appendToEnd=""
        Object actual = WordUtils.abbreviate("a", -1, -1, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_004() throws Exception {
        // Combination: str="test123", lower=Integer.MAX_VALUE, upper=Integer.MAX_VALUE, appendToEnd=""
        Object actual = WordUtils.abbreviate("test123", Integer.MAX_VALUE, Integer.MAX_VALUE, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_005() throws Exception {
        // Combination: str="!@#", lower=Integer.MIN_VALUE, upper=Integer.MIN_VALUE, appendToEnd=""
        try {
            WordUtils.abbreviate("!@#", Integer.MIN_VALUE, Integer.MIN_VALUE, "");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_006() throws Exception {
        // Combination: str=" ", lower=0, upper=-1, appendToEnd=" "
        Object actual = WordUtils.abbreviate(" ", 0, -1, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_007() throws Exception {
        // Combination: str="", lower=1, upper=Integer.MAX_VALUE, appendToEnd=" "
        Object actual = WordUtils.abbreviate("", 1, Integer.MAX_VALUE, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_008() throws Exception {
        // Combination: str="test123", lower=-1, upper=0, appendToEnd=" "
        Object actual = WordUtils.abbreviate("test123", -1, 0, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_009() throws Exception {
        // Combination: str="a", lower=Integer.MAX_VALUE, upper=1, appendToEnd=" "
        Object actual = WordUtils.abbreviate("a", Integer.MAX_VALUE, 1, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_010() throws Exception {
        // Combination: str="0", lower=Integer.MIN_VALUE, upper=0, appendToEnd=" "
        Object actual = WordUtils.abbreviate("0", Integer.MIN_VALUE, 0, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_011() throws Exception {
        // Combination: str="a", lower=0, upper=Integer.MAX_VALUE, appendToEnd="a"
        Object actual = WordUtils.abbreviate("a", 0, Integer.MAX_VALUE, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_012() throws Exception {
        // Combination: str="test123", lower=1, upper=-1, appendToEnd="a"
        Object actual = WordUtils.abbreviate("test123", 1, -1, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_013() throws Exception {
        // Combination: str="", lower=-1, upper=1, appendToEnd="a"
        Object actual = WordUtils.abbreviate("", -1, 1, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_014() throws Exception {
        // Combination: str=" ", lower=Integer.MAX_VALUE, upper=0, appendToEnd="a"
        Object actual = WordUtils.abbreviate(" ", Integer.MAX_VALUE, 0, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_015() throws Exception {
        // Combination: str="-1", lower=Integer.MIN_VALUE, upper=1, appendToEnd="a"
        Object actual = WordUtils.abbreviate("-1", Integer.MIN_VALUE, 1, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_016() throws Exception {
        // Combination: str="test123", lower=0, upper=1, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("test123", 0, 1, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("ttest123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_017() throws Exception {
        // Combination: str="a", lower=1, upper=0, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("a", 1, 0, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_018() throws Exception {
        // Combination: str=" ", lower=-1, upper=Integer.MAX_VALUE, appendToEnd="test123"
        Object actual = WordUtils.abbreviate(" ", -1, Integer.MAX_VALUE, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_019() throws Exception {
        // Combination: str="", lower=Integer.MAX_VALUE, upper=-1, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("", Integer.MAX_VALUE, -1, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_020() throws Exception {
        // Combination: str="1.5", lower=Integer.MIN_VALUE, upper=-1, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("1.5", Integer.MIN_VALUE, -1, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_021() throws Exception {
        // Combination: str="!@#", lower=0, upper=0, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("!@#", 0, 0, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_022() throws Exception {
        // Combination: str="0", lower=1, upper=Integer.MIN_VALUE, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("0", 1, Integer.MIN_VALUE, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_023() throws Exception {
        // Combination: str="-1", lower=-1, upper=-1, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("-1", -1, -1, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_024() throws Exception {
        // Combination: str="1.5", lower=Integer.MAX_VALUE, upper=1, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("1.5", Integer.MAX_VALUE, 1, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_025() throws Exception {
        // Combination: str="", lower=Integer.MIN_VALUE, upper=Integer.MAX_VALUE, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("", Integer.MIN_VALUE, Integer.MAX_VALUE, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_026() throws Exception {
        // Combination: str="0", lower=0, upper=1, appendToEnd="0"
        Object actual = WordUtils.abbreviate("0", 0, 1, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_027() throws Exception {
        // Combination: str="!@#", lower=1, upper=-1, appendToEnd="0"
        Object actual = WordUtils.abbreviate("!@#", 1, -1, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_028() throws Exception {
        // Combination: str="1.5", lower=-1, upper=Integer.MIN_VALUE, appendToEnd="0"
        try {
            WordUtils.abbreviate("1.5", -1, Integer.MIN_VALUE, "0");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_029() throws Exception {
        // Combination: str="-1", lower=Integer.MAX_VALUE, upper=0, appendToEnd="0"
        Object actual = WordUtils.abbreviate("-1", Integer.MAX_VALUE, 0, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_030() throws Exception {
        // Combination: str=" ", lower=Integer.MIN_VALUE, upper=Integer.MAX_VALUE, appendToEnd="0"
        Object actual = WordUtils.abbreviate(" ", Integer.MIN_VALUE, Integer.MAX_VALUE, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_031() throws Exception {
        // Combination: str="-1", lower=0, upper=Integer.MIN_VALUE, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("-1", 0, Integer.MIN_VALUE, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_032() throws Exception {
        // Combination: str="1.5", lower=1, upper=0, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("1.5", 1, 0, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_033() throws Exception {
        // Combination: str="!@#", lower=-1, upper=1, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("!@#", -1, 1, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_034() throws Exception {
        // Combination: str="0", lower=Integer.MAX_VALUE, upper=-1, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("0", Integer.MAX_VALUE, -1, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_035() throws Exception {
        // Combination: str="a", lower=Integer.MIN_VALUE, upper=Integer.MAX_VALUE, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("a", Integer.MIN_VALUE, Integer.MAX_VALUE, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_036() throws Exception {
        // Combination: str="1.5", lower=0, upper=Integer.MAX_VALUE, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("1.5", 0, Integer.MAX_VALUE, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_037() throws Exception {
        // Combination: str="-1", lower=1, upper=0, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("-1", 1, 0, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_038() throws Exception {
        // Combination: str="0", lower=-1, upper=1, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("0", -1, 1, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_039() throws Exception {
        // Combination: str="!@#", lower=Integer.MAX_VALUE, upper=Integer.MIN_VALUE, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("!@#", Integer.MAX_VALUE, Integer.MIN_VALUE, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_040() throws Exception {
        // Combination: str="test123", lower=Integer.MIN_VALUE, upper=-1, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("test123", Integer.MIN_VALUE, -1, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_041() throws Exception {
        // Combination: str="9223372036854775807", lower=0, upper=0, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("9223372036854775807", 0, 0, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_042() throws Exception {
        // Combination: str="9223372036854775808", lower=1, upper=1, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("9223372036854775808", 1, 1, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("99223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_043() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=-1, upper=-1, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1, -1, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_044() throws Exception {
        // Combination: str="", lower=Integer.MAX_VALUE, upper=Integer.MIN_VALUE, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("", Integer.MAX_VALUE, Integer.MIN_VALUE, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_045() throws Exception {
        // Combination: str=" ", lower=Integer.MIN_VALUE, upper=Integer.MAX_VALUE, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate(" ", Integer.MIN_VALUE, Integer.MAX_VALUE, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_046() throws Exception {
        // Combination: str="9223372036854775808", lower=0, upper=0, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("9223372036854775808", 0, 0, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_047() throws Exception {
        // Combination: str="9223372036854775807", lower=1, upper=1, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("9223372036854775807", 1, 1, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("99223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_048() throws Exception {
        // Combination: str="", lower=-1, upper=-1, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("", -1, -1, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_049() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=Integer.MAX_VALUE, upper=Integer.MAX_VALUE, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE, Integer.MAX_VALUE, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_050() throws Exception {
        // Combination: str=" ", lower=Integer.MIN_VALUE, upper=Integer.MIN_VALUE, appendToEnd="9223372036854775808"
        try {
            WordUtils.abbreviate(" ", Integer.MIN_VALUE, Integer.MIN_VALUE, "9223372036854775808");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_051() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=0, upper=0, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_052() throws Exception {
        // Combination: str="", lower=1, upper=1, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("", 1, 1, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_053() throws Exception {
        // Combination: str="9223372036854775807", lower=-1, upper=-1, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("9223372036854775807", -1, -1, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_054() throws Exception {
        // Combination: str="9223372036854775808", lower=Integer.MAX_VALUE, upper=Integer.MAX_VALUE, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("9223372036854775808", Integer.MAX_VALUE, Integer.MAX_VALUE, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_055() throws Exception {
        // Combination: str=" ", lower=Integer.MIN_VALUE, upper=Integer.MIN_VALUE, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            WordUtils.abbreviate(" ", Integer.MIN_VALUE, Integer.MIN_VALUE, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_056() throws Exception {
        // Combination: str="", lower=0, upper=0, appendToEnd="0"
        Object actual = WordUtils.abbreviate("", 0, 0, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_057() throws Exception {
        // Combination: str="", lower=0, upper=0, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("", 0, 0, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_058() throws Exception {
        // Combination: str="", lower=0, upper=0, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("", 0, 0, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_059() throws Exception {
        // Combination: str=" ", lower=0, upper=0, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate(" ", 0, 0, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_060() throws Exception {
        // Combination: str=" ", lower=0, upper=0, appendToEnd="-1"
        Object actual = WordUtils.abbreviate(" ", 0, 0, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_061() throws Exception {
        // Combination: str=" ", lower=0, upper=0, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate(" ", 0, 0, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_062() throws Exception {
        // Combination: str="a", lower=0, upper=Integer.MIN_VALUE, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("a", 0, Integer.MIN_VALUE, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_063() throws Exception {
        // Combination: str="a", lower=0, upper=0, appendToEnd="0"
        Object actual = WordUtils.abbreviate("a", 0, 0, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_064() throws Exception {
        // Combination: str="a", lower=0, upper=0, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("a", 0, 0, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_065() throws Exception {
        // Combination: str="a", lower=0, upper=0, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("a", 0, 0, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_066() throws Exception {
        // Combination: str="a", lower=0, upper=0, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("a", 0, 0, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_067() throws Exception {
        // Combination: str="a", lower=0, upper=0, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("a", 0, 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_068() throws Exception {
        // Combination: str="test123", lower=0, upper=Integer.MIN_VALUE, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("test123", 0, Integer.MIN_VALUE, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_069() throws Exception {
        // Combination: str="test123", lower=0, upper=0, appendToEnd="0"
        Object actual = WordUtils.abbreviate("test123", 0, 0, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_070() throws Exception {
        // Combination: str="test123", lower=0, upper=0, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("test123", 0, 0, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_071() throws Exception {
        // Combination: str="test123", lower=0, upper=0, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("test123", 0, 0, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_072() throws Exception {
        // Combination: str="test123", lower=0, upper=0, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("test123", 0, 0, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_073() throws Exception {
        // Combination: str="test123", lower=0, upper=0, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("test123", 0, 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_074() throws Exception {
        // Combination: str="!@#", lower=0, upper=Integer.MAX_VALUE, appendToEnd=" "
        Object actual = WordUtils.abbreviate("!@#", 0, Integer.MAX_VALUE, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_075() throws Exception {
        // Combination: str="!@#", lower=0, upper=Integer.MIN_VALUE, appendToEnd="a"
        Object actual = WordUtils.abbreviate("!@#", 0, Integer.MIN_VALUE, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_076() throws Exception {
        // Combination: str="!@#", lower=0, upper=Integer.MIN_VALUE, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("!@#", 0, Integer.MIN_VALUE, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_077() throws Exception {
        // Combination: str="!@#", lower=0, upper=0, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("!@#", 0, 0, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_078() throws Exception {
        // Combination: str="!@#", lower=0, upper=0, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("!@#", 0, 0, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_079() throws Exception {
        // Combination: str="!@#", lower=0, upper=0, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("!@#", 0, 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_080() throws Exception {
        // Combination: str="0", lower=0, upper=Integer.MAX_VALUE, appendToEnd=""
        Object actual = WordUtils.abbreviate("0", 0, Integer.MAX_VALUE, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_081() throws Exception {
        // Combination: str="0", lower=0, upper=0, appendToEnd="a"
        Object actual = WordUtils.abbreviate("0", 0, 0, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_082() throws Exception {
        // Combination: str="0", lower=0, upper=0, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("0", 0, 0, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_083() throws Exception {
        // Combination: str="0", lower=0, upper=0, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("0", 0, 0, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_084() throws Exception {
        // Combination: str="0", lower=0, upper=0, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("0", 0, 0, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_085() throws Exception {
        // Combination: str="0", lower=0, upper=0, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("0", 0, 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_086() throws Exception {
        // Combination: str="-1", lower=0, upper=Integer.MAX_VALUE, appendToEnd=""
        Object actual = WordUtils.abbreviate("-1", 0, Integer.MAX_VALUE, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_087() throws Exception {
        // Combination: str="-1", lower=0, upper=Integer.MIN_VALUE, appendToEnd=" "
        Object actual = WordUtils.abbreviate("-1", 0, Integer.MIN_VALUE, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_088() throws Exception {
        // Combination: str="-1", lower=0, upper=0, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("-1", 0, 0, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_089() throws Exception {
        // Combination: str="-1", lower=0, upper=0, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("-1", 0, 0, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_090() throws Exception {
        // Combination: str="-1", lower=0, upper=0, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("-1", 0, 0, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_091() throws Exception {
        // Combination: str="-1", lower=0, upper=0, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("-1", 0, 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_092() throws Exception {
        // Combination: str="1.5", lower=0, upper=0, appendToEnd=""
        Object actual = WordUtils.abbreviate("1.5", 0, 0, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_093() throws Exception {
        // Combination: str="1.5", lower=0, upper=0, appendToEnd=" "
        Object actual = WordUtils.abbreviate("1.5", 0, 0, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_094() throws Exception {
        // Combination: str="1.5", lower=0, upper=0, appendToEnd="a"
        Object actual = WordUtils.abbreviate("1.5", 0, 0, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_095() throws Exception {
        // Combination: str="1.5", lower=0, upper=0, appendToEnd="9223372036854775807"
        Object actual = WordUtils.abbreviate("1.5", 0, 0, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_096() throws Exception {
        // Combination: str="1.5", lower=0, upper=0, appendToEnd="9223372036854775808"
        Object actual = WordUtils.abbreviate("1.5", 0, 0, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_097() throws Exception {
        // Combination: str="1.5", lower=0, upper=0, appendToEnd="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = WordUtils.abbreviate("1.5", 0, 0, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_098() throws Exception {
        // Combination: str="9223372036854775807", lower=Integer.MAX_VALUE, upper=Integer.MAX_VALUE, appendToEnd=""
        Object actual = WordUtils.abbreviate("9223372036854775807", Integer.MAX_VALUE, Integer.MAX_VALUE, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_099() throws Exception {
        // Combination: str="9223372036854775807", lower=Integer.MIN_VALUE, upper=Integer.MIN_VALUE, appendToEnd=" "
        try {
            WordUtils.abbreviate("9223372036854775807", Integer.MIN_VALUE, Integer.MIN_VALUE, " ");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_100() throws Exception {
        // Combination: str="9223372036854775807", lower=0, upper=0, appendToEnd="a"
        Object actual = WordUtils.abbreviate("9223372036854775807", 0, 0, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_101() throws Exception {
        // Combination: str="9223372036854775807", lower=0, upper=0, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("9223372036854775807", 0, 0, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_102() throws Exception {
        // Combination: str="9223372036854775807", lower=0, upper=0, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("9223372036854775807", 0, 0, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_103() throws Exception {
        // Combination: str="9223372036854775807", lower=0, upper=0, appendToEnd="0"
        Object actual = WordUtils.abbreviate("9223372036854775807", 0, 0, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_104() throws Exception {
        // Combination: str="9223372036854775807", lower=0, upper=0, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("9223372036854775807", 0, 0, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_105() throws Exception {
        // Combination: str="9223372036854775807", lower=0, upper=0, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("9223372036854775807", 0, 0, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_106() throws Exception {
        // Combination: str="9223372036854775808", lower=-1, upper=-1, appendToEnd=""
        Object actual = WordUtils.abbreviate("9223372036854775808", -1, -1, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_107() throws Exception {
        // Combination: str="9223372036854775808", lower=Integer.MIN_VALUE, upper=Integer.MIN_VALUE, appendToEnd=" "
        try {
            WordUtils.abbreviate("9223372036854775808", Integer.MIN_VALUE, Integer.MIN_VALUE, " ");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_108() throws Exception {
        // Combination: str="9223372036854775808", lower=0, upper=0, appendToEnd="a"
        Object actual = WordUtils.abbreviate("9223372036854775808", 0, 0, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_109() throws Exception {
        // Combination: str="9223372036854775808", lower=0, upper=0, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("9223372036854775808", 0, 0, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_110() throws Exception {
        // Combination: str="9223372036854775808", lower=0, upper=0, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("9223372036854775808", 0, 0, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_111() throws Exception {
        // Combination: str="9223372036854775808", lower=0, upper=0, appendToEnd="0"
        Object actual = WordUtils.abbreviate("9223372036854775808", 0, 0, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_112() throws Exception {
        // Combination: str="9223372036854775808", lower=0, upper=0, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("9223372036854775808", 0, 0, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_113() throws Exception {
        // Combination: str="9223372036854775808", lower=0, upper=0, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("9223372036854775808", 0, 0, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_114() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=1, upper=1, appendToEnd=""
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1, 1, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_115() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=Integer.MIN_VALUE, upper=Integer.MIN_VALUE, appendToEnd=" "
        try {
            WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE, Integer.MIN_VALUE, " ");
            fail("Expected java.lang.StringIndexOutOfBoundsException");
        } catch (java.lang.StringIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_116() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=0, upper=0, appendToEnd="a"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_117() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=0, upper=0, appendToEnd="test123"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_118() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=0, upper=0, appendToEnd="!@#"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_119() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=0, upper=0, appendToEnd="0"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_120() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=0, upper=0, appendToEnd="-1"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_abbreviate_pairwise_121() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lower=0, upper=0, appendToEnd="1.5"
        Object actual = WordUtils.abbreviate("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0, 0, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

}
