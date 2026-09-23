package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for ProcessCommonJSModules.
 */
public class ProcessCommonJSModules_IPOTest {
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
    public void test_toModuleName_pairwise_001() throws Exception {
        // Combination: requiredFilename="", currentFilename=""
        Object actual = ProcessCommonJSModules.toModuleName("", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_002() throws Exception {
        // Combination: requiredFilename=" ", currentFilename=""
        Object actual = ProcessCommonJSModules.toModuleName(" ", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$ ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_003() throws Exception {
        // Combination: requiredFilename="a", currentFilename=""
        Object actual = ProcessCommonJSModules.toModuleName("a", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_004() throws Exception {
        // Combination: requiredFilename="test123", currentFilename=""
        Object actual = ProcessCommonJSModules.toModuleName("test123", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_005() throws Exception {
        // Combination: requiredFilename="!@#", currentFilename=""
        Object actual = ProcessCommonJSModules.toModuleName("!@#", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_006() throws Exception {
        // Combination: requiredFilename="0", currentFilename=""
        Object actual = ProcessCommonJSModules.toModuleName("0", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_007() throws Exception {
        // Combination: requiredFilename="-1", currentFilename=""
        Object actual = ProcessCommonJSModules.toModuleName("-1", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$_1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_008() throws Exception {
        // Combination: requiredFilename="1.5", currentFilename=""
        Object actual = ProcessCommonJSModules.toModuleName("1.5", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_009() throws Exception {
        // Combination: requiredFilename="9223372036854775807", currentFilename=""
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_010() throws Exception {
        // Combination: requiredFilename="9223372036854775808", currentFilename=""
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_011() throws Exception {
        // Combination: requiredFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", currentFilename=""
        Object actual = ProcessCommonJSModules.toModuleName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_012() throws Exception {
        // Combination: requiredFilename="", currentFilename=" "
        Object actual = ProcessCommonJSModules.toModuleName("", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_013() throws Exception {
        // Combination: requiredFilename=" ", currentFilename=" "
        Object actual = ProcessCommonJSModules.toModuleName(" ", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$ ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_014() throws Exception {
        // Combination: requiredFilename="a", currentFilename=" "
        Object actual = ProcessCommonJSModules.toModuleName("a", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_015() throws Exception {
        // Combination: requiredFilename="test123", currentFilename=" "
        Object actual = ProcessCommonJSModules.toModuleName("test123", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_016() throws Exception {
        // Combination: requiredFilename="!@#", currentFilename=" "
        Object actual = ProcessCommonJSModules.toModuleName("!@#", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_017() throws Exception {
        // Combination: requiredFilename="0", currentFilename=" "
        Object actual = ProcessCommonJSModules.toModuleName("0", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_018() throws Exception {
        // Combination: requiredFilename="-1", currentFilename=" "
        Object actual = ProcessCommonJSModules.toModuleName("-1", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$_1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_019() throws Exception {
        // Combination: requiredFilename="1.5", currentFilename=" "
        Object actual = ProcessCommonJSModules.toModuleName("1.5", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_020() throws Exception {
        // Combination: requiredFilename="9223372036854775807", currentFilename=" "
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_021() throws Exception {
        // Combination: requiredFilename="9223372036854775808", currentFilename=" "
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_022() throws Exception {
        // Combination: requiredFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", currentFilename=" "
        Object actual = ProcessCommonJSModules.toModuleName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_023() throws Exception {
        // Combination: requiredFilename="", currentFilename="a"
        Object actual = ProcessCommonJSModules.toModuleName("", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_024() throws Exception {
        // Combination: requiredFilename=" ", currentFilename="a"
        Object actual = ProcessCommonJSModules.toModuleName(" ", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$ ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_025() throws Exception {
        // Combination: requiredFilename="a", currentFilename="a"
        Object actual = ProcessCommonJSModules.toModuleName("a", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_026() throws Exception {
        // Combination: requiredFilename="test123", currentFilename="a"
        Object actual = ProcessCommonJSModules.toModuleName("test123", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_027() throws Exception {
        // Combination: requiredFilename="!@#", currentFilename="a"
        Object actual = ProcessCommonJSModules.toModuleName("!@#", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_028() throws Exception {
        // Combination: requiredFilename="0", currentFilename="a"
        Object actual = ProcessCommonJSModules.toModuleName("0", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_029() throws Exception {
        // Combination: requiredFilename="-1", currentFilename="a"
        Object actual = ProcessCommonJSModules.toModuleName("-1", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$_1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_030() throws Exception {
        // Combination: requiredFilename="1.5", currentFilename="a"
        Object actual = ProcessCommonJSModules.toModuleName("1.5", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_031() throws Exception {
        // Combination: requiredFilename="9223372036854775807", currentFilename="a"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_032() throws Exception {
        // Combination: requiredFilename="9223372036854775808", currentFilename="a"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_033() throws Exception {
        // Combination: requiredFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", currentFilename="a"
        Object actual = ProcessCommonJSModules.toModuleName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_034() throws Exception {
        // Combination: requiredFilename="", currentFilename="test123"
        Object actual = ProcessCommonJSModules.toModuleName("", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_035() throws Exception {
        // Combination: requiredFilename=" ", currentFilename="test123"
        Object actual = ProcessCommonJSModules.toModuleName(" ", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$ ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_036() throws Exception {
        // Combination: requiredFilename="a", currentFilename="test123"
        Object actual = ProcessCommonJSModules.toModuleName("a", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_037() throws Exception {
        // Combination: requiredFilename="test123", currentFilename="test123"
        Object actual = ProcessCommonJSModules.toModuleName("test123", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_038() throws Exception {
        // Combination: requiredFilename="!@#", currentFilename="test123"
        Object actual = ProcessCommonJSModules.toModuleName("!@#", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_039() throws Exception {
        // Combination: requiredFilename="0", currentFilename="test123"
        Object actual = ProcessCommonJSModules.toModuleName("0", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_040() throws Exception {
        // Combination: requiredFilename="-1", currentFilename="test123"
        Object actual = ProcessCommonJSModules.toModuleName("-1", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$_1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_041() throws Exception {
        // Combination: requiredFilename="1.5", currentFilename="test123"
        Object actual = ProcessCommonJSModules.toModuleName("1.5", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_042() throws Exception {
        // Combination: requiredFilename="9223372036854775807", currentFilename="test123"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_043() throws Exception {
        // Combination: requiredFilename="9223372036854775808", currentFilename="test123"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_044() throws Exception {
        // Combination: requiredFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", currentFilename="test123"
        Object actual = ProcessCommonJSModules.toModuleName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_045() throws Exception {
        // Combination: requiredFilename="", currentFilename="!@#"
        Object actual = ProcessCommonJSModules.toModuleName("", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_046() throws Exception {
        // Combination: requiredFilename=" ", currentFilename="!@#"
        Object actual = ProcessCommonJSModules.toModuleName(" ", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$ ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_047() throws Exception {
        // Combination: requiredFilename="a", currentFilename="!@#"
        Object actual = ProcessCommonJSModules.toModuleName("a", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_048() throws Exception {
        // Combination: requiredFilename="test123", currentFilename="!@#"
        Object actual = ProcessCommonJSModules.toModuleName("test123", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_049() throws Exception {
        // Combination: requiredFilename="!@#", currentFilename="!@#"
        Object actual = ProcessCommonJSModules.toModuleName("!@#", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_050() throws Exception {
        // Combination: requiredFilename="0", currentFilename="!@#"
        Object actual = ProcessCommonJSModules.toModuleName("0", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_051() throws Exception {
        // Combination: requiredFilename="-1", currentFilename="!@#"
        Object actual = ProcessCommonJSModules.toModuleName("-1", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$_1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_052() throws Exception {
        // Combination: requiredFilename="1.5", currentFilename="!@#"
        Object actual = ProcessCommonJSModules.toModuleName("1.5", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_053() throws Exception {
        // Combination: requiredFilename="9223372036854775807", currentFilename="!@#"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_054() throws Exception {
        // Combination: requiredFilename="9223372036854775808", currentFilename="!@#"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_055() throws Exception {
        // Combination: requiredFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", currentFilename="!@#"
        Object actual = ProcessCommonJSModules.toModuleName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_056() throws Exception {
        // Combination: requiredFilename="", currentFilename="0"
        Object actual = ProcessCommonJSModules.toModuleName("", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_057() throws Exception {
        // Combination: requiredFilename=" ", currentFilename="0"
        Object actual = ProcessCommonJSModules.toModuleName(" ", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$ ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_058() throws Exception {
        // Combination: requiredFilename="a", currentFilename="0"
        Object actual = ProcessCommonJSModules.toModuleName("a", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_059() throws Exception {
        // Combination: requiredFilename="test123", currentFilename="0"
        Object actual = ProcessCommonJSModules.toModuleName("test123", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_060() throws Exception {
        // Combination: requiredFilename="!@#", currentFilename="0"
        Object actual = ProcessCommonJSModules.toModuleName("!@#", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_061() throws Exception {
        // Combination: requiredFilename="0", currentFilename="0"
        Object actual = ProcessCommonJSModules.toModuleName("0", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_062() throws Exception {
        // Combination: requiredFilename="-1", currentFilename="0"
        Object actual = ProcessCommonJSModules.toModuleName("-1", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$_1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_063() throws Exception {
        // Combination: requiredFilename="1.5", currentFilename="0"
        Object actual = ProcessCommonJSModules.toModuleName("1.5", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_064() throws Exception {
        // Combination: requiredFilename="9223372036854775807", currentFilename="0"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_065() throws Exception {
        // Combination: requiredFilename="9223372036854775808", currentFilename="0"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_066() throws Exception {
        // Combination: requiredFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", currentFilename="0"
        Object actual = ProcessCommonJSModules.toModuleName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_067() throws Exception {
        // Combination: requiredFilename="", currentFilename="-1"
        Object actual = ProcessCommonJSModules.toModuleName("", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_068() throws Exception {
        // Combination: requiredFilename=" ", currentFilename="-1"
        Object actual = ProcessCommonJSModules.toModuleName(" ", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$ ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_069() throws Exception {
        // Combination: requiredFilename="a", currentFilename="-1"
        Object actual = ProcessCommonJSModules.toModuleName("a", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_070() throws Exception {
        // Combination: requiredFilename="test123", currentFilename="-1"
        Object actual = ProcessCommonJSModules.toModuleName("test123", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_071() throws Exception {
        // Combination: requiredFilename="!@#", currentFilename="-1"
        Object actual = ProcessCommonJSModules.toModuleName("!@#", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_072() throws Exception {
        // Combination: requiredFilename="0", currentFilename="-1"
        Object actual = ProcessCommonJSModules.toModuleName("0", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_073() throws Exception {
        // Combination: requiredFilename="-1", currentFilename="-1"
        Object actual = ProcessCommonJSModules.toModuleName("-1", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$_1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_074() throws Exception {
        // Combination: requiredFilename="1.5", currentFilename="-1"
        Object actual = ProcessCommonJSModules.toModuleName("1.5", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_075() throws Exception {
        // Combination: requiredFilename="9223372036854775807", currentFilename="-1"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_076() throws Exception {
        // Combination: requiredFilename="9223372036854775808", currentFilename="-1"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_077() throws Exception {
        // Combination: requiredFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", currentFilename="-1"
        Object actual = ProcessCommonJSModules.toModuleName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_078() throws Exception {
        // Combination: requiredFilename="", currentFilename="1.5"
        Object actual = ProcessCommonJSModules.toModuleName("", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_079() throws Exception {
        // Combination: requiredFilename=" ", currentFilename="1.5"
        Object actual = ProcessCommonJSModules.toModuleName(" ", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$ ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_080() throws Exception {
        // Combination: requiredFilename="a", currentFilename="1.5"
        Object actual = ProcessCommonJSModules.toModuleName("a", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_081() throws Exception {
        // Combination: requiredFilename="test123", currentFilename="1.5"
        Object actual = ProcessCommonJSModules.toModuleName("test123", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_082() throws Exception {
        // Combination: requiredFilename="!@#", currentFilename="1.5"
        Object actual = ProcessCommonJSModules.toModuleName("!@#", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_083() throws Exception {
        // Combination: requiredFilename="0", currentFilename="1.5"
        Object actual = ProcessCommonJSModules.toModuleName("0", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_084() throws Exception {
        // Combination: requiredFilename="-1", currentFilename="1.5"
        Object actual = ProcessCommonJSModules.toModuleName("-1", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$_1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_085() throws Exception {
        // Combination: requiredFilename="1.5", currentFilename="1.5"
        Object actual = ProcessCommonJSModules.toModuleName("1.5", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_086() throws Exception {
        // Combination: requiredFilename="9223372036854775807", currentFilename="1.5"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_087() throws Exception {
        // Combination: requiredFilename="9223372036854775808", currentFilename="1.5"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_088() throws Exception {
        // Combination: requiredFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", currentFilename="1.5"
        Object actual = ProcessCommonJSModules.toModuleName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_089() throws Exception {
        // Combination: requiredFilename="", currentFilename="9223372036854775807"
        Object actual = ProcessCommonJSModules.toModuleName("", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_090() throws Exception {
        // Combination: requiredFilename=" ", currentFilename="9223372036854775807"
        Object actual = ProcessCommonJSModules.toModuleName(" ", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$ ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_091() throws Exception {
        // Combination: requiredFilename="a", currentFilename="9223372036854775807"
        Object actual = ProcessCommonJSModules.toModuleName("a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_092() throws Exception {
        // Combination: requiredFilename="test123", currentFilename="9223372036854775807"
        Object actual = ProcessCommonJSModules.toModuleName("test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_093() throws Exception {
        // Combination: requiredFilename="!@#", currentFilename="9223372036854775807"
        Object actual = ProcessCommonJSModules.toModuleName("!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_094() throws Exception {
        // Combination: requiredFilename="0", currentFilename="9223372036854775807"
        Object actual = ProcessCommonJSModules.toModuleName("0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_095() throws Exception {
        // Combination: requiredFilename="-1", currentFilename="9223372036854775807"
        Object actual = ProcessCommonJSModules.toModuleName("-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$_1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_096() throws Exception {
        // Combination: requiredFilename="1.5", currentFilename="9223372036854775807"
        Object actual = ProcessCommonJSModules.toModuleName("1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_097() throws Exception {
        // Combination: requiredFilename="9223372036854775807", currentFilename="9223372036854775807"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_098() throws Exception {
        // Combination: requiredFilename="9223372036854775808", currentFilename="9223372036854775807"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_099() throws Exception {
        // Combination: requiredFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", currentFilename="9223372036854775807"
        Object actual = ProcessCommonJSModules.toModuleName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_100() throws Exception {
        // Combination: requiredFilename="", currentFilename="9223372036854775808"
        Object actual = ProcessCommonJSModules.toModuleName("", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_101() throws Exception {
        // Combination: requiredFilename=" ", currentFilename="9223372036854775808"
        Object actual = ProcessCommonJSModules.toModuleName(" ", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$ ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_102() throws Exception {
        // Combination: requiredFilename="a", currentFilename="9223372036854775808"
        Object actual = ProcessCommonJSModules.toModuleName("a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_103() throws Exception {
        // Combination: requiredFilename="test123", currentFilename="9223372036854775808"
        Object actual = ProcessCommonJSModules.toModuleName("test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_104() throws Exception {
        // Combination: requiredFilename="!@#", currentFilename="9223372036854775808"
        Object actual = ProcessCommonJSModules.toModuleName("!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_105() throws Exception {
        // Combination: requiredFilename="0", currentFilename="9223372036854775808"
        Object actual = ProcessCommonJSModules.toModuleName("0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_106() throws Exception {
        // Combination: requiredFilename="-1", currentFilename="9223372036854775808"
        Object actual = ProcessCommonJSModules.toModuleName("-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$_1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_107() throws Exception {
        // Combination: requiredFilename="1.5", currentFilename="9223372036854775808"
        Object actual = ProcessCommonJSModules.toModuleName("1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_108() throws Exception {
        // Combination: requiredFilename="9223372036854775807", currentFilename="9223372036854775808"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_109() throws Exception {
        // Combination: requiredFilename="9223372036854775808", currentFilename="9223372036854775808"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_110() throws Exception {
        // Combination: requiredFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", currentFilename="9223372036854775808"
        Object actual = ProcessCommonJSModules.toModuleName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_111() throws Exception {
        // Combination: requiredFilename="", currentFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ProcessCommonJSModules.toModuleName("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_112() throws Exception {
        // Combination: requiredFilename=" ", currentFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ProcessCommonJSModules.toModuleName(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$ ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_113() throws Exception {
        // Combination: requiredFilename="a", currentFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ProcessCommonJSModules.toModuleName("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_114() throws Exception {
        // Combination: requiredFilename="test123", currentFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ProcessCommonJSModules.toModuleName("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_115() throws Exception {
        // Combination: requiredFilename="!@#", currentFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ProcessCommonJSModules.toModuleName("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_116() throws Exception {
        // Combination: requiredFilename="0", currentFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ProcessCommonJSModules.toModuleName("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_117() throws Exception {
        // Combination: requiredFilename="-1", currentFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ProcessCommonJSModules.toModuleName("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$_1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_118() throws Exception {
        // Combination: requiredFilename="1.5", currentFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ProcessCommonJSModules.toModuleName("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_119() throws Exception {
        // Combination: requiredFilename="9223372036854775807", currentFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_120() throws Exception {
        // Combination: requiredFilename="9223372036854775808", currentFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ProcessCommonJSModules.toModuleName("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_toModuleName_pairwise_121() throws Exception {
        // Combination: requiredFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", currentFilename="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = ProcessCommonJSModules.toModuleName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("module$aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

}
