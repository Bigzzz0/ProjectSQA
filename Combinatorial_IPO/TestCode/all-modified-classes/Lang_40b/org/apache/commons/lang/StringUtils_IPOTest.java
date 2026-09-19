package org.apache.commons.lang;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for StringUtils.
 */
public class StringUtils_IPOTest {
    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_001() throws Exception {
        // Combination: str="", searchStr=""
        Object actual = StringUtils.containsIgnoreCase("", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_002() throws Exception {
        // Combination: str=" ", searchStr=""
        Object actual = StringUtils.containsIgnoreCase(" ", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_003() throws Exception {
        // Combination: str="a", searchStr=""
        Object actual = StringUtils.containsIgnoreCase("a", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_004() throws Exception {
        // Combination: str="test123", searchStr=""
        Object actual = StringUtils.containsIgnoreCase("test123", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_005() throws Exception {
        // Combination: str="!@#", searchStr=""
        Object actual = StringUtils.containsIgnoreCase("!@#", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_006() throws Exception {
        // Combination: str="0", searchStr=""
        Object actual = StringUtils.containsIgnoreCase("0", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_007() throws Exception {
        // Combination: str="-1", searchStr=""
        Object actual = StringUtils.containsIgnoreCase("-1", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_008() throws Exception {
        // Combination: str="1.5", searchStr=""
        Object actual = StringUtils.containsIgnoreCase("1.5", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_009() throws Exception {
        // Combination: str="9223372036854775807", searchStr=""
        Object actual = StringUtils.containsIgnoreCase("9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_010() throws Exception {
        // Combination: str="9223372036854775808", searchStr=""
        Object actual = StringUtils.containsIgnoreCase("9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_011() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", searchStr=""
        Object actual = StringUtils.containsIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_012() throws Exception {
        // Combination: str="", searchStr=" "
        Object actual = StringUtils.containsIgnoreCase("", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_013() throws Exception {
        // Combination: str=" ", searchStr=" "
        Object actual = StringUtils.containsIgnoreCase(" ", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_014() throws Exception {
        // Combination: str="a", searchStr=" "
        Object actual = StringUtils.containsIgnoreCase("a", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_015() throws Exception {
        // Combination: str="test123", searchStr=" "
        Object actual = StringUtils.containsIgnoreCase("test123", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_016() throws Exception {
        // Combination: str="!@#", searchStr=" "
        Object actual = StringUtils.containsIgnoreCase("!@#", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_017() throws Exception {
        // Combination: str="0", searchStr=" "
        Object actual = StringUtils.containsIgnoreCase("0", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_018() throws Exception {
        // Combination: str="-1", searchStr=" "
        Object actual = StringUtils.containsIgnoreCase("-1", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_019() throws Exception {
        // Combination: str="1.5", searchStr=" "
        Object actual = StringUtils.containsIgnoreCase("1.5", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_020() throws Exception {
        // Combination: str="9223372036854775807", searchStr=" "
        Object actual = StringUtils.containsIgnoreCase("9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_021() throws Exception {
        // Combination: str="9223372036854775808", searchStr=" "
        Object actual = StringUtils.containsIgnoreCase("9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_022() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", searchStr=" "
        Object actual = StringUtils.containsIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_023() throws Exception {
        // Combination: str="", searchStr="a"
        Object actual = StringUtils.containsIgnoreCase("", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_024() throws Exception {
        // Combination: str=" ", searchStr="a"
        Object actual = StringUtils.containsIgnoreCase(" ", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_025() throws Exception {
        // Combination: str="a", searchStr="a"
        Object actual = StringUtils.containsIgnoreCase("a", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_026() throws Exception {
        // Combination: str="test123", searchStr="a"
        Object actual = StringUtils.containsIgnoreCase("test123", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_027() throws Exception {
        // Combination: str="!@#", searchStr="a"
        Object actual = StringUtils.containsIgnoreCase("!@#", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_028() throws Exception {
        // Combination: str="0", searchStr="a"
        Object actual = StringUtils.containsIgnoreCase("0", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_029() throws Exception {
        // Combination: str="-1", searchStr="a"
        Object actual = StringUtils.containsIgnoreCase("-1", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_030() throws Exception {
        // Combination: str="1.5", searchStr="a"
        Object actual = StringUtils.containsIgnoreCase("1.5", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_031() throws Exception {
        // Combination: str="9223372036854775807", searchStr="a"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_032() throws Exception {
        // Combination: str="9223372036854775808", searchStr="a"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_033() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", searchStr="a"
        Object actual = StringUtils.containsIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_034() throws Exception {
        // Combination: str="", searchStr="test123"
        Object actual = StringUtils.containsIgnoreCase("", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_035() throws Exception {
        // Combination: str=" ", searchStr="test123"
        Object actual = StringUtils.containsIgnoreCase(" ", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_036() throws Exception {
        // Combination: str="a", searchStr="test123"
        Object actual = StringUtils.containsIgnoreCase("a", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_037() throws Exception {
        // Combination: str="test123", searchStr="test123"
        Object actual = StringUtils.containsIgnoreCase("test123", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_038() throws Exception {
        // Combination: str="!@#", searchStr="test123"
        Object actual = StringUtils.containsIgnoreCase("!@#", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_039() throws Exception {
        // Combination: str="0", searchStr="test123"
        Object actual = StringUtils.containsIgnoreCase("0", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_040() throws Exception {
        // Combination: str="-1", searchStr="test123"
        Object actual = StringUtils.containsIgnoreCase("-1", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_041() throws Exception {
        // Combination: str="1.5", searchStr="test123"
        Object actual = StringUtils.containsIgnoreCase("1.5", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_042() throws Exception {
        // Combination: str="9223372036854775807", searchStr="test123"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_043() throws Exception {
        // Combination: str="9223372036854775808", searchStr="test123"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_044() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", searchStr="test123"
        Object actual = StringUtils.containsIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_045() throws Exception {
        // Combination: str="", searchStr="!@#"
        Object actual = StringUtils.containsIgnoreCase("", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_046() throws Exception {
        // Combination: str=" ", searchStr="!@#"
        Object actual = StringUtils.containsIgnoreCase(" ", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_047() throws Exception {
        // Combination: str="a", searchStr="!@#"
        Object actual = StringUtils.containsIgnoreCase("a", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_048() throws Exception {
        // Combination: str="test123", searchStr="!@#"
        Object actual = StringUtils.containsIgnoreCase("test123", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_049() throws Exception {
        // Combination: str="!@#", searchStr="!@#"
        Object actual = StringUtils.containsIgnoreCase("!@#", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_050() throws Exception {
        // Combination: str="0", searchStr="!@#"
        Object actual = StringUtils.containsIgnoreCase("0", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_051() throws Exception {
        // Combination: str="-1", searchStr="!@#"
        Object actual = StringUtils.containsIgnoreCase("-1", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_052() throws Exception {
        // Combination: str="1.5", searchStr="!@#"
        Object actual = StringUtils.containsIgnoreCase("1.5", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_053() throws Exception {
        // Combination: str="9223372036854775807", searchStr="!@#"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_054() throws Exception {
        // Combination: str="9223372036854775808", searchStr="!@#"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_055() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", searchStr="!@#"
        Object actual = StringUtils.containsIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_056() throws Exception {
        // Combination: str="", searchStr="0"
        Object actual = StringUtils.containsIgnoreCase("", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_057() throws Exception {
        // Combination: str=" ", searchStr="0"
        Object actual = StringUtils.containsIgnoreCase(" ", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_058() throws Exception {
        // Combination: str="a", searchStr="0"
        Object actual = StringUtils.containsIgnoreCase("a", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_059() throws Exception {
        // Combination: str="test123", searchStr="0"
        Object actual = StringUtils.containsIgnoreCase("test123", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_060() throws Exception {
        // Combination: str="!@#", searchStr="0"
        Object actual = StringUtils.containsIgnoreCase("!@#", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_061() throws Exception {
        // Combination: str="0", searchStr="0"
        Object actual = StringUtils.containsIgnoreCase("0", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_062() throws Exception {
        // Combination: str="-1", searchStr="0"
        Object actual = StringUtils.containsIgnoreCase("-1", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_063() throws Exception {
        // Combination: str="1.5", searchStr="0"
        Object actual = StringUtils.containsIgnoreCase("1.5", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_064() throws Exception {
        // Combination: str="9223372036854775807", searchStr="0"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_065() throws Exception {
        // Combination: str="9223372036854775808", searchStr="0"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_066() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", searchStr="0"
        Object actual = StringUtils.containsIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_067() throws Exception {
        // Combination: str="", searchStr="-1"
        Object actual = StringUtils.containsIgnoreCase("", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_068() throws Exception {
        // Combination: str=" ", searchStr="-1"
        Object actual = StringUtils.containsIgnoreCase(" ", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_069() throws Exception {
        // Combination: str="a", searchStr="-1"
        Object actual = StringUtils.containsIgnoreCase("a", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_070() throws Exception {
        // Combination: str="test123", searchStr="-1"
        Object actual = StringUtils.containsIgnoreCase("test123", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_071() throws Exception {
        // Combination: str="!@#", searchStr="-1"
        Object actual = StringUtils.containsIgnoreCase("!@#", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_072() throws Exception {
        // Combination: str="0", searchStr="-1"
        Object actual = StringUtils.containsIgnoreCase("0", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_073() throws Exception {
        // Combination: str="-1", searchStr="-1"
        Object actual = StringUtils.containsIgnoreCase("-1", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_074() throws Exception {
        // Combination: str="1.5", searchStr="-1"
        Object actual = StringUtils.containsIgnoreCase("1.5", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_075() throws Exception {
        // Combination: str="9223372036854775807", searchStr="-1"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_076() throws Exception {
        // Combination: str="9223372036854775808", searchStr="-1"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_077() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", searchStr="-1"
        Object actual = StringUtils.containsIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_078() throws Exception {
        // Combination: str="", searchStr="1.5"
        Object actual = StringUtils.containsIgnoreCase("", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_079() throws Exception {
        // Combination: str=" ", searchStr="1.5"
        Object actual = StringUtils.containsIgnoreCase(" ", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_080() throws Exception {
        // Combination: str="a", searchStr="1.5"
        Object actual = StringUtils.containsIgnoreCase("a", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_081() throws Exception {
        // Combination: str="test123", searchStr="1.5"
        Object actual = StringUtils.containsIgnoreCase("test123", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_082() throws Exception {
        // Combination: str="!@#", searchStr="1.5"
        Object actual = StringUtils.containsIgnoreCase("!@#", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_083() throws Exception {
        // Combination: str="0", searchStr="1.5"
        Object actual = StringUtils.containsIgnoreCase("0", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_084() throws Exception {
        // Combination: str="-1", searchStr="1.5"
        Object actual = StringUtils.containsIgnoreCase("-1", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_085() throws Exception {
        // Combination: str="1.5", searchStr="1.5"
        Object actual = StringUtils.containsIgnoreCase("1.5", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_086() throws Exception {
        // Combination: str="9223372036854775807", searchStr="1.5"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_087() throws Exception {
        // Combination: str="9223372036854775808", searchStr="1.5"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_088() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", searchStr="1.5"
        Object actual = StringUtils.containsIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_089() throws Exception {
        // Combination: str="", searchStr="9223372036854775807"
        Object actual = StringUtils.containsIgnoreCase("", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_090() throws Exception {
        // Combination: str=" ", searchStr="9223372036854775807"
        Object actual = StringUtils.containsIgnoreCase(" ", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_091() throws Exception {
        // Combination: str="a", searchStr="9223372036854775807"
        Object actual = StringUtils.containsIgnoreCase("a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_092() throws Exception {
        // Combination: str="test123", searchStr="9223372036854775807"
        Object actual = StringUtils.containsIgnoreCase("test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_093() throws Exception {
        // Combination: str="!@#", searchStr="9223372036854775807"
        Object actual = StringUtils.containsIgnoreCase("!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_094() throws Exception {
        // Combination: str="0", searchStr="9223372036854775807"
        Object actual = StringUtils.containsIgnoreCase("0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_095() throws Exception {
        // Combination: str="-1", searchStr="9223372036854775807"
        Object actual = StringUtils.containsIgnoreCase("-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_096() throws Exception {
        // Combination: str="1.5", searchStr="9223372036854775807"
        Object actual = StringUtils.containsIgnoreCase("1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_097() throws Exception {
        // Combination: str="9223372036854775807", searchStr="9223372036854775807"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_098() throws Exception {
        // Combination: str="9223372036854775808", searchStr="9223372036854775807"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_099() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", searchStr="9223372036854775807"
        Object actual = StringUtils.containsIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_100() throws Exception {
        // Combination: str="", searchStr="9223372036854775808"
        Object actual = StringUtils.containsIgnoreCase("", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_101() throws Exception {
        // Combination: str=" ", searchStr="9223372036854775808"
        Object actual = StringUtils.containsIgnoreCase(" ", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_102() throws Exception {
        // Combination: str="a", searchStr="9223372036854775808"
        Object actual = StringUtils.containsIgnoreCase("a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_103() throws Exception {
        // Combination: str="test123", searchStr="9223372036854775808"
        Object actual = StringUtils.containsIgnoreCase("test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_104() throws Exception {
        // Combination: str="!@#", searchStr="9223372036854775808"
        Object actual = StringUtils.containsIgnoreCase("!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_105() throws Exception {
        // Combination: str="0", searchStr="9223372036854775808"
        Object actual = StringUtils.containsIgnoreCase("0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_106() throws Exception {
        // Combination: str="-1", searchStr="9223372036854775808"
        Object actual = StringUtils.containsIgnoreCase("-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_107() throws Exception {
        // Combination: str="1.5", searchStr="9223372036854775808"
        Object actual = StringUtils.containsIgnoreCase("1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_108() throws Exception {
        // Combination: str="9223372036854775807", searchStr="9223372036854775808"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_109() throws Exception {
        // Combination: str="9223372036854775808", searchStr="9223372036854775808"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_110() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", searchStr="9223372036854775808"
        Object actual = StringUtils.containsIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_111() throws Exception {
        // Combination: str="", searchStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtils.containsIgnoreCase("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_112() throws Exception {
        // Combination: str=" ", searchStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtils.containsIgnoreCase(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_113() throws Exception {
        // Combination: str="a", searchStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtils.containsIgnoreCase("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_114() throws Exception {
        // Combination: str="test123", searchStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtils.containsIgnoreCase("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_115() throws Exception {
        // Combination: str="!@#", searchStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtils.containsIgnoreCase("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_116() throws Exception {
        // Combination: str="0", searchStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtils.containsIgnoreCase("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_117() throws Exception {
        // Combination: str="-1", searchStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtils.containsIgnoreCase("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_118() throws Exception {
        // Combination: str="1.5", searchStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtils.containsIgnoreCase("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_119() throws Exception {
        // Combination: str="9223372036854775807", searchStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_120() throws Exception {
        // Combination: str="9223372036854775808", searchStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtils.containsIgnoreCase("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_containsIgnoreCase_pairwise_121() throws Exception {
        // Combination: str="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", searchStr="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = StringUtils.containsIgnoreCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

}
