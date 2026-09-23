package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for CommandLine.
 */
public class CommandLine_IPOTest {
    private static String formatValue(Object value) {
        if (value == null) return "null";
        if (value instanceof Object[]) return java.util.Arrays.deepToString((Object[]) value);
        if (value instanceof byte[]) return java.util.Arrays.toString((byte[]) value);
        if (value instanceof short[]) return java.util.Arrays.toString((short[]) value);
        if (value instanceof int[]) return java.util.Arrays.toString((int[]) value);
        if (value instanceof long[]) return java.util.Arrays.toString((long[]) value);
        if (value instanceof char[]) return java.util.Arrays.toString((char[]) value);
        if (value instanceof float[]) return java.util.Arrays.toString((float[]) value);
        if (value instanceof double[]) return java.util.Arrays.toString((double[]) value);
        if (value instanceof boolean[]) return java.util.Arrays.toString((boolean[]) value);
        return String.valueOf(value);
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_001() throws Exception {
        // Combination: opt="", defaultValue=""
        Object actual = (new CommandLine()).getOptionValue("", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_002() throws Exception {
        // Combination: opt=" ", defaultValue=""
        Object actual = (new CommandLine()).getOptionValue(" ", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_003() throws Exception {
        // Combination: opt="a", defaultValue=""
        Object actual = (new CommandLine()).getOptionValue("a", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_004() throws Exception {
        // Combination: opt="test123", defaultValue=""
        Object actual = (new CommandLine()).getOptionValue("test123", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_005() throws Exception {
        // Combination: opt="!@#", defaultValue=""
        Object actual = (new CommandLine()).getOptionValue("!@#", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_006() throws Exception {
        // Combination: opt="0", defaultValue=""
        Object actual = (new CommandLine()).getOptionValue("0", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_007() throws Exception {
        // Combination: opt="-1", defaultValue=""
        Object actual = (new CommandLine()).getOptionValue("-1", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_008() throws Exception {
        // Combination: opt="1.5", defaultValue=""
        Object actual = (new CommandLine()).getOptionValue("1.5", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_009() throws Exception {
        // Combination: opt="9223372036854775807", defaultValue=""
        Object actual = (new CommandLine()).getOptionValue("9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_010() throws Exception {
        // Combination: opt="9223372036854775808", defaultValue=""
        Object actual = (new CommandLine()).getOptionValue("9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_011() throws Exception {
        // Combination: opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=""
        Object actual = (new CommandLine()).getOptionValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_012() throws Exception {
        // Combination: opt="", defaultValue=" "
        Object actual = (new CommandLine()).getOptionValue("", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_013() throws Exception {
        // Combination: opt=" ", defaultValue=" "
        Object actual = (new CommandLine()).getOptionValue(" ", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_014() throws Exception {
        // Combination: opt="a", defaultValue=" "
        Object actual = (new CommandLine()).getOptionValue("a", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_015() throws Exception {
        // Combination: opt="test123", defaultValue=" "
        Object actual = (new CommandLine()).getOptionValue("test123", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_016() throws Exception {
        // Combination: opt="!@#", defaultValue=" "
        Object actual = (new CommandLine()).getOptionValue("!@#", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_017() throws Exception {
        // Combination: opt="0", defaultValue=" "
        Object actual = (new CommandLine()).getOptionValue("0", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_018() throws Exception {
        // Combination: opt="-1", defaultValue=" "
        Object actual = (new CommandLine()).getOptionValue("-1", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_019() throws Exception {
        // Combination: opt="1.5", defaultValue=" "
        Object actual = (new CommandLine()).getOptionValue("1.5", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_020() throws Exception {
        // Combination: opt="9223372036854775807", defaultValue=" "
        Object actual = (new CommandLine()).getOptionValue("9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_021() throws Exception {
        // Combination: opt="9223372036854775808", defaultValue=" "
        Object actual = (new CommandLine()).getOptionValue("9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_022() throws Exception {
        // Combination: opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=" "
        Object actual = (new CommandLine()).getOptionValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_023() throws Exception {
        // Combination: opt="", defaultValue="a"
        Object actual = (new CommandLine()).getOptionValue("", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_024() throws Exception {
        // Combination: opt=" ", defaultValue="a"
        Object actual = (new CommandLine()).getOptionValue(" ", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_025() throws Exception {
        // Combination: opt="a", defaultValue="a"
        Object actual = (new CommandLine()).getOptionValue("a", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_026() throws Exception {
        // Combination: opt="test123", defaultValue="a"
        Object actual = (new CommandLine()).getOptionValue("test123", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_027() throws Exception {
        // Combination: opt="!@#", defaultValue="a"
        Object actual = (new CommandLine()).getOptionValue("!@#", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_028() throws Exception {
        // Combination: opt="0", defaultValue="a"
        Object actual = (new CommandLine()).getOptionValue("0", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_029() throws Exception {
        // Combination: opt="-1", defaultValue="a"
        Object actual = (new CommandLine()).getOptionValue("-1", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_030() throws Exception {
        // Combination: opt="1.5", defaultValue="a"
        Object actual = (new CommandLine()).getOptionValue("1.5", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_031() throws Exception {
        // Combination: opt="9223372036854775807", defaultValue="a"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_032() throws Exception {
        // Combination: opt="9223372036854775808", defaultValue="a"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_033() throws Exception {
        // Combination: opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="a"
        Object actual = (new CommandLine()).getOptionValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_034() throws Exception {
        // Combination: opt="", defaultValue="test123"
        Object actual = (new CommandLine()).getOptionValue("", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_035() throws Exception {
        // Combination: opt=" ", defaultValue="test123"
        Object actual = (new CommandLine()).getOptionValue(" ", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_036() throws Exception {
        // Combination: opt="a", defaultValue="test123"
        Object actual = (new CommandLine()).getOptionValue("a", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_037() throws Exception {
        // Combination: opt="test123", defaultValue="test123"
        Object actual = (new CommandLine()).getOptionValue("test123", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_038() throws Exception {
        // Combination: opt="!@#", defaultValue="test123"
        Object actual = (new CommandLine()).getOptionValue("!@#", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_039() throws Exception {
        // Combination: opt="0", defaultValue="test123"
        Object actual = (new CommandLine()).getOptionValue("0", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_040() throws Exception {
        // Combination: opt="-1", defaultValue="test123"
        Object actual = (new CommandLine()).getOptionValue("-1", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_041() throws Exception {
        // Combination: opt="1.5", defaultValue="test123"
        Object actual = (new CommandLine()).getOptionValue("1.5", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_042() throws Exception {
        // Combination: opt="9223372036854775807", defaultValue="test123"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_043() throws Exception {
        // Combination: opt="9223372036854775808", defaultValue="test123"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_044() throws Exception {
        // Combination: opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="test123"
        Object actual = (new CommandLine()).getOptionValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_045() throws Exception {
        // Combination: opt="", defaultValue="!@#"
        Object actual = (new CommandLine()).getOptionValue("", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_046() throws Exception {
        // Combination: opt=" ", defaultValue="!@#"
        Object actual = (new CommandLine()).getOptionValue(" ", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_047() throws Exception {
        // Combination: opt="a", defaultValue="!@#"
        Object actual = (new CommandLine()).getOptionValue("a", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_048() throws Exception {
        // Combination: opt="test123", defaultValue="!@#"
        Object actual = (new CommandLine()).getOptionValue("test123", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_049() throws Exception {
        // Combination: opt="!@#", defaultValue="!@#"
        Object actual = (new CommandLine()).getOptionValue("!@#", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_050() throws Exception {
        // Combination: opt="0", defaultValue="!@#"
        Object actual = (new CommandLine()).getOptionValue("0", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_051() throws Exception {
        // Combination: opt="-1", defaultValue="!@#"
        Object actual = (new CommandLine()).getOptionValue("-1", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_052() throws Exception {
        // Combination: opt="1.5", defaultValue="!@#"
        Object actual = (new CommandLine()).getOptionValue("1.5", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_053() throws Exception {
        // Combination: opt="9223372036854775807", defaultValue="!@#"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_054() throws Exception {
        // Combination: opt="9223372036854775808", defaultValue="!@#"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_055() throws Exception {
        // Combination: opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="!@#"
        Object actual = (new CommandLine()).getOptionValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_056() throws Exception {
        // Combination: opt="", defaultValue="0"
        Object actual = (new CommandLine()).getOptionValue("", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_057() throws Exception {
        // Combination: opt=" ", defaultValue="0"
        Object actual = (new CommandLine()).getOptionValue(" ", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_058() throws Exception {
        // Combination: opt="a", defaultValue="0"
        Object actual = (new CommandLine()).getOptionValue("a", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_059() throws Exception {
        // Combination: opt="test123", defaultValue="0"
        Object actual = (new CommandLine()).getOptionValue("test123", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_060() throws Exception {
        // Combination: opt="!@#", defaultValue="0"
        Object actual = (new CommandLine()).getOptionValue("!@#", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_061() throws Exception {
        // Combination: opt="0", defaultValue="0"
        Object actual = (new CommandLine()).getOptionValue("0", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_062() throws Exception {
        // Combination: opt="-1", defaultValue="0"
        Object actual = (new CommandLine()).getOptionValue("-1", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_063() throws Exception {
        // Combination: opt="1.5", defaultValue="0"
        Object actual = (new CommandLine()).getOptionValue("1.5", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_064() throws Exception {
        // Combination: opt="9223372036854775807", defaultValue="0"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_065() throws Exception {
        // Combination: opt="9223372036854775808", defaultValue="0"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_066() throws Exception {
        // Combination: opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="0"
        Object actual = (new CommandLine()).getOptionValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_067() throws Exception {
        // Combination: opt="", defaultValue="-1"
        Object actual = (new CommandLine()).getOptionValue("", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_068() throws Exception {
        // Combination: opt=" ", defaultValue="-1"
        Object actual = (new CommandLine()).getOptionValue(" ", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_069() throws Exception {
        // Combination: opt="a", defaultValue="-1"
        Object actual = (new CommandLine()).getOptionValue("a", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_070() throws Exception {
        // Combination: opt="test123", defaultValue="-1"
        Object actual = (new CommandLine()).getOptionValue("test123", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_071() throws Exception {
        // Combination: opt="!@#", defaultValue="-1"
        Object actual = (new CommandLine()).getOptionValue("!@#", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_072() throws Exception {
        // Combination: opt="0", defaultValue="-1"
        Object actual = (new CommandLine()).getOptionValue("0", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_073() throws Exception {
        // Combination: opt="-1", defaultValue="-1"
        Object actual = (new CommandLine()).getOptionValue("-1", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_074() throws Exception {
        // Combination: opt="1.5", defaultValue="-1"
        Object actual = (new CommandLine()).getOptionValue("1.5", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_075() throws Exception {
        // Combination: opt="9223372036854775807", defaultValue="-1"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_076() throws Exception {
        // Combination: opt="9223372036854775808", defaultValue="-1"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_077() throws Exception {
        // Combination: opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="-1"
        Object actual = (new CommandLine()).getOptionValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_078() throws Exception {
        // Combination: opt="", defaultValue="1.5"
        Object actual = (new CommandLine()).getOptionValue("", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_079() throws Exception {
        // Combination: opt=" ", defaultValue="1.5"
        Object actual = (new CommandLine()).getOptionValue(" ", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_080() throws Exception {
        // Combination: opt="a", defaultValue="1.5"
        Object actual = (new CommandLine()).getOptionValue("a", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_081() throws Exception {
        // Combination: opt="test123", defaultValue="1.5"
        Object actual = (new CommandLine()).getOptionValue("test123", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_082() throws Exception {
        // Combination: opt="!@#", defaultValue="1.5"
        Object actual = (new CommandLine()).getOptionValue("!@#", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_083() throws Exception {
        // Combination: opt="0", defaultValue="1.5"
        Object actual = (new CommandLine()).getOptionValue("0", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_084() throws Exception {
        // Combination: opt="-1", defaultValue="1.5"
        Object actual = (new CommandLine()).getOptionValue("-1", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_085() throws Exception {
        // Combination: opt="1.5", defaultValue="1.5"
        Object actual = (new CommandLine()).getOptionValue("1.5", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_086() throws Exception {
        // Combination: opt="9223372036854775807", defaultValue="1.5"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_087() throws Exception {
        // Combination: opt="9223372036854775808", defaultValue="1.5"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_088() throws Exception {
        // Combination: opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="1.5"
        Object actual = (new CommandLine()).getOptionValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_089() throws Exception {
        // Combination: opt="", defaultValue="9223372036854775807"
        Object actual = (new CommandLine()).getOptionValue("", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_090() throws Exception {
        // Combination: opt=" ", defaultValue="9223372036854775807"
        Object actual = (new CommandLine()).getOptionValue(" ", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_091() throws Exception {
        // Combination: opt="a", defaultValue="9223372036854775807"
        Object actual = (new CommandLine()).getOptionValue("a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_092() throws Exception {
        // Combination: opt="test123", defaultValue="9223372036854775807"
        Object actual = (new CommandLine()).getOptionValue("test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_093() throws Exception {
        // Combination: opt="!@#", defaultValue="9223372036854775807"
        Object actual = (new CommandLine()).getOptionValue("!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_094() throws Exception {
        // Combination: opt="0", defaultValue="9223372036854775807"
        Object actual = (new CommandLine()).getOptionValue("0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_095() throws Exception {
        // Combination: opt="-1", defaultValue="9223372036854775807"
        Object actual = (new CommandLine()).getOptionValue("-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_096() throws Exception {
        // Combination: opt="1.5", defaultValue="9223372036854775807"
        Object actual = (new CommandLine()).getOptionValue("1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_097() throws Exception {
        // Combination: opt="9223372036854775807", defaultValue="9223372036854775807"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_098() throws Exception {
        // Combination: opt="9223372036854775808", defaultValue="9223372036854775807"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_099() throws Exception {
        // Combination: opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="9223372036854775807"
        Object actual = (new CommandLine()).getOptionValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_100() throws Exception {
        // Combination: opt="", defaultValue="9223372036854775808"
        Object actual = (new CommandLine()).getOptionValue("", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_101() throws Exception {
        // Combination: opt=" ", defaultValue="9223372036854775808"
        Object actual = (new CommandLine()).getOptionValue(" ", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_102() throws Exception {
        // Combination: opt="a", defaultValue="9223372036854775808"
        Object actual = (new CommandLine()).getOptionValue("a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_103() throws Exception {
        // Combination: opt="test123", defaultValue="9223372036854775808"
        Object actual = (new CommandLine()).getOptionValue("test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_104() throws Exception {
        // Combination: opt="!@#", defaultValue="9223372036854775808"
        Object actual = (new CommandLine()).getOptionValue("!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_105() throws Exception {
        // Combination: opt="0", defaultValue="9223372036854775808"
        Object actual = (new CommandLine()).getOptionValue("0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_106() throws Exception {
        // Combination: opt="-1", defaultValue="9223372036854775808"
        Object actual = (new CommandLine()).getOptionValue("-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_107() throws Exception {
        // Combination: opt="1.5", defaultValue="9223372036854775808"
        Object actual = (new CommandLine()).getOptionValue("1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_108() throws Exception {
        // Combination: opt="9223372036854775807", defaultValue="9223372036854775808"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_109() throws Exception {
        // Combination: opt="9223372036854775808", defaultValue="9223372036854775808"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_110() throws Exception {
        // Combination: opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="9223372036854775808"
        Object actual = (new CommandLine()).getOptionValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_111() throws Exception {
        // Combination: opt="", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new CommandLine()).getOptionValue("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_112() throws Exception {
        // Combination: opt=" ", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new CommandLine()).getOptionValue(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_113() throws Exception {
        // Combination: opt="a", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new CommandLine()).getOptionValue("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_114() throws Exception {
        // Combination: opt="test123", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new CommandLine()).getOptionValue("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_115() throws Exception {
        // Combination: opt="!@#", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new CommandLine()).getOptionValue("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_116() throws Exception {
        // Combination: opt="0", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new CommandLine()).getOptionValue("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_117() throws Exception {
        // Combination: opt="-1", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new CommandLine()).getOptionValue("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_118() throws Exception {
        // Combination: opt="1.5", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new CommandLine()).getOptionValue("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_119() throws Exception {
        // Combination: opt="9223372036854775807", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_120() throws Exception {
        // Combination: opt="9223372036854775808", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new CommandLine()).getOptionValue("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_121() throws Exception {
        // Combination: opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new CommandLine()).getOptionValue("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_122() throws Exception {
        // Combination: opt='\0', defaultValue=""
        Object actual = (new CommandLine()).getOptionValue('\0', "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_123() throws Exception {
        // Combination: opt='a', defaultValue=""
        Object actual = (new CommandLine()).getOptionValue('a', "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_124() throws Exception {
        // Combination: opt='0', defaultValue=""
        Object actual = (new CommandLine()).getOptionValue('0', "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_125() throws Exception {
        // Combination: opt=Character.MIN_VALUE, defaultValue=""
        Object actual = (new CommandLine()).getOptionValue(Character.MIN_VALUE, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_126() throws Exception {
        // Combination: opt=Character.MAX_VALUE, defaultValue=""
        Object actual = (new CommandLine()).getOptionValue(Character.MAX_VALUE, "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_127() throws Exception {
        // Combination: opt='\0', defaultValue=" "
        Object actual = (new CommandLine()).getOptionValue('\0', " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_128() throws Exception {
        // Combination: opt='a', defaultValue=" "
        Object actual = (new CommandLine()).getOptionValue('a', " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_129() throws Exception {
        // Combination: opt='0', defaultValue=" "
        Object actual = (new CommandLine()).getOptionValue('0', " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_130() throws Exception {
        // Combination: opt=Character.MIN_VALUE, defaultValue=" "
        Object actual = (new CommandLine()).getOptionValue(Character.MIN_VALUE, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_131() throws Exception {
        // Combination: opt=Character.MAX_VALUE, defaultValue=" "
        Object actual = (new CommandLine()).getOptionValue(Character.MAX_VALUE, " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_132() throws Exception {
        // Combination: opt='\0', defaultValue="a"
        Object actual = (new CommandLine()).getOptionValue('\0', "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_133() throws Exception {
        // Combination: opt='a', defaultValue="a"
        Object actual = (new CommandLine()).getOptionValue('a', "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_134() throws Exception {
        // Combination: opt='0', defaultValue="a"
        Object actual = (new CommandLine()).getOptionValue('0', "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_135() throws Exception {
        // Combination: opt=Character.MIN_VALUE, defaultValue="a"
        Object actual = (new CommandLine()).getOptionValue(Character.MIN_VALUE, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_136() throws Exception {
        // Combination: opt=Character.MAX_VALUE, defaultValue="a"
        Object actual = (new CommandLine()).getOptionValue(Character.MAX_VALUE, "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_137() throws Exception {
        // Combination: opt='\0', defaultValue="test123"
        Object actual = (new CommandLine()).getOptionValue('\0', "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_138() throws Exception {
        // Combination: opt='a', defaultValue="test123"
        Object actual = (new CommandLine()).getOptionValue('a', "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_139() throws Exception {
        // Combination: opt='0', defaultValue="test123"
        Object actual = (new CommandLine()).getOptionValue('0', "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_140() throws Exception {
        // Combination: opt=Character.MIN_VALUE, defaultValue="test123"
        Object actual = (new CommandLine()).getOptionValue(Character.MIN_VALUE, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_141() throws Exception {
        // Combination: opt=Character.MAX_VALUE, defaultValue="test123"
        Object actual = (new CommandLine()).getOptionValue(Character.MAX_VALUE, "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_142() throws Exception {
        // Combination: opt='\0', defaultValue="!@#"
        Object actual = (new CommandLine()).getOptionValue('\0', "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_143() throws Exception {
        // Combination: opt='a', defaultValue="!@#"
        Object actual = (new CommandLine()).getOptionValue('a', "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_144() throws Exception {
        // Combination: opt='0', defaultValue="!@#"
        Object actual = (new CommandLine()).getOptionValue('0', "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_145() throws Exception {
        // Combination: opt=Character.MIN_VALUE, defaultValue="!@#"
        Object actual = (new CommandLine()).getOptionValue(Character.MIN_VALUE, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_146() throws Exception {
        // Combination: opt=Character.MAX_VALUE, defaultValue="!@#"
        Object actual = (new CommandLine()).getOptionValue(Character.MAX_VALUE, "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_147() throws Exception {
        // Combination: opt='\0', defaultValue="0"
        Object actual = (new CommandLine()).getOptionValue('\0', "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_148() throws Exception {
        // Combination: opt='a', defaultValue="0"
        Object actual = (new CommandLine()).getOptionValue('a', "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_149() throws Exception {
        // Combination: opt='0', defaultValue="0"
        Object actual = (new CommandLine()).getOptionValue('0', "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_150() throws Exception {
        // Combination: opt=Character.MIN_VALUE, defaultValue="0"
        Object actual = (new CommandLine()).getOptionValue(Character.MIN_VALUE, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_151() throws Exception {
        // Combination: opt=Character.MAX_VALUE, defaultValue="0"
        Object actual = (new CommandLine()).getOptionValue(Character.MAX_VALUE, "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_152() throws Exception {
        // Combination: opt='\0', defaultValue="-1"
        Object actual = (new CommandLine()).getOptionValue('\0', "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_153() throws Exception {
        // Combination: opt='a', defaultValue="-1"
        Object actual = (new CommandLine()).getOptionValue('a', "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_154() throws Exception {
        // Combination: opt='0', defaultValue="-1"
        Object actual = (new CommandLine()).getOptionValue('0', "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_155() throws Exception {
        // Combination: opt=Character.MIN_VALUE, defaultValue="-1"
        Object actual = (new CommandLine()).getOptionValue(Character.MIN_VALUE, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_156() throws Exception {
        // Combination: opt=Character.MAX_VALUE, defaultValue="-1"
        Object actual = (new CommandLine()).getOptionValue(Character.MAX_VALUE, "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_157() throws Exception {
        // Combination: opt='\0', defaultValue="1.5"
        Object actual = (new CommandLine()).getOptionValue('\0', "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_158() throws Exception {
        // Combination: opt='a', defaultValue="1.5"
        Object actual = (new CommandLine()).getOptionValue('a', "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_159() throws Exception {
        // Combination: opt='0', defaultValue="1.5"
        Object actual = (new CommandLine()).getOptionValue('0', "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_160() throws Exception {
        // Combination: opt=Character.MIN_VALUE, defaultValue="1.5"
        Object actual = (new CommandLine()).getOptionValue(Character.MIN_VALUE, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_161() throws Exception {
        // Combination: opt=Character.MAX_VALUE, defaultValue="1.5"
        Object actual = (new CommandLine()).getOptionValue(Character.MAX_VALUE, "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_162() throws Exception {
        // Combination: opt='\0', defaultValue="9223372036854775807"
        Object actual = (new CommandLine()).getOptionValue('\0', "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_163() throws Exception {
        // Combination: opt='a', defaultValue="9223372036854775807"
        Object actual = (new CommandLine()).getOptionValue('a', "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_164() throws Exception {
        // Combination: opt='0', defaultValue="9223372036854775807"
        Object actual = (new CommandLine()).getOptionValue('0', "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_165() throws Exception {
        // Combination: opt=Character.MIN_VALUE, defaultValue="9223372036854775807"
        Object actual = (new CommandLine()).getOptionValue(Character.MIN_VALUE, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_166() throws Exception {
        // Combination: opt=Character.MAX_VALUE, defaultValue="9223372036854775807"
        Object actual = (new CommandLine()).getOptionValue(Character.MAX_VALUE, "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_167() throws Exception {
        // Combination: opt='\0', defaultValue="9223372036854775808"
        Object actual = (new CommandLine()).getOptionValue('\0', "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_168() throws Exception {
        // Combination: opt='a', defaultValue="9223372036854775808"
        Object actual = (new CommandLine()).getOptionValue('a', "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_169() throws Exception {
        // Combination: opt='0', defaultValue="9223372036854775808"
        Object actual = (new CommandLine()).getOptionValue('0', "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_170() throws Exception {
        // Combination: opt=Character.MIN_VALUE, defaultValue="9223372036854775808"
        Object actual = (new CommandLine()).getOptionValue(Character.MIN_VALUE, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_171() throws Exception {
        // Combination: opt=Character.MAX_VALUE, defaultValue="9223372036854775808"
        Object actual = (new CommandLine()).getOptionValue(Character.MAX_VALUE, "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_172() throws Exception {
        // Combination: opt='\0', defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new CommandLine()).getOptionValue('\0', "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_173() throws Exception {
        // Combination: opt='a', defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new CommandLine()).getOptionValue('a', "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_174() throws Exception {
        // Combination: opt='0', defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new CommandLine()).getOptionValue('0', "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_175() throws Exception {
        // Combination: opt=Character.MIN_VALUE, defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new CommandLine()).getOptionValue(Character.MIN_VALUE, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getOptionValue_pairwise_176() throws Exception {
        // Combination: opt=Character.MAX_VALUE, defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new CommandLine()).getOptionValue(Character.MAX_VALUE, "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

}
