package org.apache.commons.codec.language;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Metaphone.
 */
public class Metaphone_IPOTest {
    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_001() throws Exception {
        // Combination: str1="", str2=""
        Object actual = (new Metaphone()).isMetaphoneEqual("", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_002() throws Exception {
        // Combination: str1="", str2=" "
        Object actual = (new Metaphone()).isMetaphoneEqual("", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_003() throws Exception {
        // Combination: str1="", str2="a"
        Object actual = (new Metaphone()).isMetaphoneEqual("", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_004() throws Exception {
        // Combination: str1="", str2="test123"
        Object actual = (new Metaphone()).isMetaphoneEqual("", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_005() throws Exception {
        // Combination: str1="", str2="!@#"
        Object actual = (new Metaphone()).isMetaphoneEqual("", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_006() throws Exception {
        // Combination: str1="", str2="0"
        Object actual = (new Metaphone()).isMetaphoneEqual("", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_007() throws Exception {
        // Combination: str1="", str2="-1"
        Object actual = (new Metaphone()).isMetaphoneEqual("", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_008() throws Exception {
        // Combination: str1="", str2="1.5"
        Object actual = (new Metaphone()).isMetaphoneEqual("", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_009() throws Exception {
        // Combination: str1="", str2="9223372036854775807"
        Object actual = (new Metaphone()).isMetaphoneEqual("", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_010() throws Exception {
        // Combination: str1="", str2="9223372036854775808"
        Object actual = (new Metaphone()).isMetaphoneEqual("", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_011() throws Exception {
        // Combination: str1="", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Metaphone()).isMetaphoneEqual("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_012() throws Exception {
        // Combination: str1=" ", str2=""
        Object actual = (new Metaphone()).isMetaphoneEqual(" ", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_013() throws Exception {
        // Combination: str1=" ", str2=" "
        Object actual = (new Metaphone()).isMetaphoneEqual(" ", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_014() throws Exception {
        // Combination: str1=" ", str2="a"
        Object actual = (new Metaphone()).isMetaphoneEqual(" ", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_015() throws Exception {
        // Combination: str1=" ", str2="test123"
        Object actual = (new Metaphone()).isMetaphoneEqual(" ", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_016() throws Exception {
        // Combination: str1=" ", str2="!@#"
        Object actual = (new Metaphone()).isMetaphoneEqual(" ", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_017() throws Exception {
        // Combination: str1=" ", str2="0"
        Object actual = (new Metaphone()).isMetaphoneEqual(" ", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_018() throws Exception {
        // Combination: str1=" ", str2="-1"
        Object actual = (new Metaphone()).isMetaphoneEqual(" ", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_019() throws Exception {
        // Combination: str1=" ", str2="1.5"
        Object actual = (new Metaphone()).isMetaphoneEqual(" ", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_020() throws Exception {
        // Combination: str1=" ", str2="9223372036854775807"
        Object actual = (new Metaphone()).isMetaphoneEqual(" ", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_021() throws Exception {
        // Combination: str1=" ", str2="9223372036854775808"
        Object actual = (new Metaphone()).isMetaphoneEqual(" ", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_022() throws Exception {
        // Combination: str1=" ", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Metaphone()).isMetaphoneEqual(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_023() throws Exception {
        // Combination: str1="a", str2=""
        Object actual = (new Metaphone()).isMetaphoneEqual("a", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_024() throws Exception {
        // Combination: str1="a", str2=" "
        Object actual = (new Metaphone()).isMetaphoneEqual("a", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_025() throws Exception {
        // Combination: str1="a", str2="a"
        Object actual = (new Metaphone()).isMetaphoneEqual("a", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_026() throws Exception {
        // Combination: str1="a", str2="test123"
        Object actual = (new Metaphone()).isMetaphoneEqual("a", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_027() throws Exception {
        // Combination: str1="a", str2="!@#"
        Object actual = (new Metaphone()).isMetaphoneEqual("a", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_028() throws Exception {
        // Combination: str1="a", str2="0"
        Object actual = (new Metaphone()).isMetaphoneEqual("a", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_029() throws Exception {
        // Combination: str1="a", str2="-1"
        Object actual = (new Metaphone()).isMetaphoneEqual("a", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_030() throws Exception {
        // Combination: str1="a", str2="1.5"
        Object actual = (new Metaphone()).isMetaphoneEqual("a", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_031() throws Exception {
        // Combination: str1="a", str2="9223372036854775807"
        Object actual = (new Metaphone()).isMetaphoneEqual("a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_032() throws Exception {
        // Combination: str1="a", str2="9223372036854775808"
        Object actual = (new Metaphone()).isMetaphoneEqual("a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_033() throws Exception {
        // Combination: str1="a", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Metaphone()).isMetaphoneEqual("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_034() throws Exception {
        // Combination: str1="test123", str2=""
        Object actual = (new Metaphone()).isMetaphoneEqual("test123", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_035() throws Exception {
        // Combination: str1="test123", str2=" "
        Object actual = (new Metaphone()).isMetaphoneEqual("test123", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_036() throws Exception {
        // Combination: str1="test123", str2="a"
        Object actual = (new Metaphone()).isMetaphoneEqual("test123", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_037() throws Exception {
        // Combination: str1="test123", str2="test123"
        Object actual = (new Metaphone()).isMetaphoneEqual("test123", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_038() throws Exception {
        // Combination: str1="test123", str2="!@#"
        Object actual = (new Metaphone()).isMetaphoneEqual("test123", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_039() throws Exception {
        // Combination: str1="test123", str2="0"
        Object actual = (new Metaphone()).isMetaphoneEqual("test123", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_040() throws Exception {
        // Combination: str1="test123", str2="-1"
        Object actual = (new Metaphone()).isMetaphoneEqual("test123", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_041() throws Exception {
        // Combination: str1="test123", str2="1.5"
        Object actual = (new Metaphone()).isMetaphoneEqual("test123", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_042() throws Exception {
        // Combination: str1="test123", str2="9223372036854775807"
        Object actual = (new Metaphone()).isMetaphoneEqual("test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_043() throws Exception {
        // Combination: str1="test123", str2="9223372036854775808"
        Object actual = (new Metaphone()).isMetaphoneEqual("test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_044() throws Exception {
        // Combination: str1="test123", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Metaphone()).isMetaphoneEqual("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_045() throws Exception {
        // Combination: str1="!@#", str2=""
        Object actual = (new Metaphone()).isMetaphoneEqual("!@#", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_046() throws Exception {
        // Combination: str1="!@#", str2=" "
        Object actual = (new Metaphone()).isMetaphoneEqual("!@#", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_047() throws Exception {
        // Combination: str1="!@#", str2="a"
        Object actual = (new Metaphone()).isMetaphoneEqual("!@#", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_048() throws Exception {
        // Combination: str1="!@#", str2="test123"
        Object actual = (new Metaphone()).isMetaphoneEqual("!@#", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_049() throws Exception {
        // Combination: str1="!@#", str2="!@#"
        Object actual = (new Metaphone()).isMetaphoneEqual("!@#", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_050() throws Exception {
        // Combination: str1="!@#", str2="0"
        Object actual = (new Metaphone()).isMetaphoneEqual("!@#", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_051() throws Exception {
        // Combination: str1="!@#", str2="-1"
        Object actual = (new Metaphone()).isMetaphoneEqual("!@#", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_052() throws Exception {
        // Combination: str1="!@#", str2="1.5"
        Object actual = (new Metaphone()).isMetaphoneEqual("!@#", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_053() throws Exception {
        // Combination: str1="!@#", str2="9223372036854775807"
        Object actual = (new Metaphone()).isMetaphoneEqual("!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_054() throws Exception {
        // Combination: str1="!@#", str2="9223372036854775808"
        Object actual = (new Metaphone()).isMetaphoneEqual("!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_055() throws Exception {
        // Combination: str1="!@#", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Metaphone()).isMetaphoneEqual("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_056() throws Exception {
        // Combination: str1="0", str2=""
        Object actual = (new Metaphone()).isMetaphoneEqual("0", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_057() throws Exception {
        // Combination: str1="0", str2=" "
        Object actual = (new Metaphone()).isMetaphoneEqual("0", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_058() throws Exception {
        // Combination: str1="0", str2="a"
        Object actual = (new Metaphone()).isMetaphoneEqual("0", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_059() throws Exception {
        // Combination: str1="0", str2="test123"
        Object actual = (new Metaphone()).isMetaphoneEqual("0", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_060() throws Exception {
        // Combination: str1="0", str2="!@#"
        Object actual = (new Metaphone()).isMetaphoneEqual("0", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_061() throws Exception {
        // Combination: str1="0", str2="0"
        Object actual = (new Metaphone()).isMetaphoneEqual("0", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_062() throws Exception {
        // Combination: str1="0", str2="-1"
        Object actual = (new Metaphone()).isMetaphoneEqual("0", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_063() throws Exception {
        // Combination: str1="0", str2="1.5"
        Object actual = (new Metaphone()).isMetaphoneEqual("0", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_064() throws Exception {
        // Combination: str1="0", str2="9223372036854775807"
        Object actual = (new Metaphone()).isMetaphoneEqual("0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_065() throws Exception {
        // Combination: str1="0", str2="9223372036854775808"
        Object actual = (new Metaphone()).isMetaphoneEqual("0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_066() throws Exception {
        // Combination: str1="0", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Metaphone()).isMetaphoneEqual("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_067() throws Exception {
        // Combination: str1="-1", str2=""
        Object actual = (new Metaphone()).isMetaphoneEqual("-1", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_068() throws Exception {
        // Combination: str1="-1", str2=" "
        Object actual = (new Metaphone()).isMetaphoneEqual("-1", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_069() throws Exception {
        // Combination: str1="-1", str2="a"
        Object actual = (new Metaphone()).isMetaphoneEqual("-1", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_070() throws Exception {
        // Combination: str1="-1", str2="test123"
        Object actual = (new Metaphone()).isMetaphoneEqual("-1", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_071() throws Exception {
        // Combination: str1="-1", str2="!@#"
        Object actual = (new Metaphone()).isMetaphoneEqual("-1", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_072() throws Exception {
        // Combination: str1="-1", str2="0"
        Object actual = (new Metaphone()).isMetaphoneEqual("-1", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_073() throws Exception {
        // Combination: str1="-1", str2="-1"
        Object actual = (new Metaphone()).isMetaphoneEqual("-1", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_074() throws Exception {
        // Combination: str1="-1", str2="1.5"
        Object actual = (new Metaphone()).isMetaphoneEqual("-1", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_075() throws Exception {
        // Combination: str1="-1", str2="9223372036854775807"
        Object actual = (new Metaphone()).isMetaphoneEqual("-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_076() throws Exception {
        // Combination: str1="-1", str2="9223372036854775808"
        Object actual = (new Metaphone()).isMetaphoneEqual("-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_077() throws Exception {
        // Combination: str1="-1", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Metaphone()).isMetaphoneEqual("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_078() throws Exception {
        // Combination: str1="1.5", str2=""
        Object actual = (new Metaphone()).isMetaphoneEqual("1.5", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_079() throws Exception {
        // Combination: str1="1.5", str2=" "
        Object actual = (new Metaphone()).isMetaphoneEqual("1.5", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_080() throws Exception {
        // Combination: str1="1.5", str2="a"
        Object actual = (new Metaphone()).isMetaphoneEqual("1.5", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_081() throws Exception {
        // Combination: str1="1.5", str2="test123"
        Object actual = (new Metaphone()).isMetaphoneEqual("1.5", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_082() throws Exception {
        // Combination: str1="1.5", str2="!@#"
        Object actual = (new Metaphone()).isMetaphoneEqual("1.5", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_083() throws Exception {
        // Combination: str1="1.5", str2="0"
        Object actual = (new Metaphone()).isMetaphoneEqual("1.5", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_084() throws Exception {
        // Combination: str1="1.5", str2="-1"
        Object actual = (new Metaphone()).isMetaphoneEqual("1.5", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_085() throws Exception {
        // Combination: str1="1.5", str2="1.5"
        Object actual = (new Metaphone()).isMetaphoneEqual("1.5", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_086() throws Exception {
        // Combination: str1="1.5", str2="9223372036854775807"
        Object actual = (new Metaphone()).isMetaphoneEqual("1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_087() throws Exception {
        // Combination: str1="1.5", str2="9223372036854775808"
        Object actual = (new Metaphone()).isMetaphoneEqual("1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_088() throws Exception {
        // Combination: str1="1.5", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Metaphone()).isMetaphoneEqual("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_089() throws Exception {
        // Combination: str1="9223372036854775807", str2=""
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_090() throws Exception {
        // Combination: str1="9223372036854775807", str2=" "
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_091() throws Exception {
        // Combination: str1="9223372036854775807", str2="a"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_092() throws Exception {
        // Combination: str1="9223372036854775807", str2="test123"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_093() throws Exception {
        // Combination: str1="9223372036854775807", str2="!@#"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_094() throws Exception {
        // Combination: str1="9223372036854775807", str2="0"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_095() throws Exception {
        // Combination: str1="9223372036854775807", str2="-1"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_096() throws Exception {
        // Combination: str1="9223372036854775807", str2="1.5"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_097() throws Exception {
        // Combination: str1="9223372036854775807", str2="9223372036854775807"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_098() throws Exception {
        // Combination: str1="9223372036854775807", str2="9223372036854775808"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_099() throws Exception {
        // Combination: str1="9223372036854775807", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_100() throws Exception {
        // Combination: str1="9223372036854775808", str2=""
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_101() throws Exception {
        // Combination: str1="9223372036854775808", str2=" "
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_102() throws Exception {
        // Combination: str1="9223372036854775808", str2="a"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_103() throws Exception {
        // Combination: str1="9223372036854775808", str2="test123"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_104() throws Exception {
        // Combination: str1="9223372036854775808", str2="!@#"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_105() throws Exception {
        // Combination: str1="9223372036854775808", str2="0"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_106() throws Exception {
        // Combination: str1="9223372036854775808", str2="-1"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_107() throws Exception {
        // Combination: str1="9223372036854775808", str2="1.5"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_108() throws Exception {
        // Combination: str1="9223372036854775808", str2="9223372036854775807"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_109() throws Exception {
        // Combination: str1="9223372036854775808", str2="9223372036854775808"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_110() throws Exception {
        // Combination: str1="9223372036854775808", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Metaphone()).isMetaphoneEqual("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_111() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2=""
        Object actual = (new Metaphone()).isMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_112() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2=" "
        Object actual = (new Metaphone()).isMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_113() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="a"
        Object actual = (new Metaphone()).isMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_114() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="test123"
        Object actual = (new Metaphone()).isMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_115() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="!@#"
        Object actual = (new Metaphone()).isMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_116() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="0"
        Object actual = (new Metaphone()).isMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_117() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="-1"
        Object actual = (new Metaphone()).isMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_118() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="1.5"
        Object actual = (new Metaphone()).isMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_119() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="9223372036854775807"
        Object actual = (new Metaphone()).isMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_120() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="9223372036854775808"
        Object actual = (new Metaphone()).isMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isMetaphoneEqual_pairwise_121() throws Exception {
        // Combination: str1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", str2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Metaphone()).isMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

}
