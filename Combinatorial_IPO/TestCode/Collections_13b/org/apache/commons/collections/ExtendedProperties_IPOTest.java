package org.apache.commons.collections;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for ExtendedProperties.
 */
public class ExtendedProperties_IPOTest {
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
    public void test_interpolateHelper_pairwise_001() throws Exception {
        // Combination: base="", priorVariables=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).interpolateHelper("", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_002() throws Exception {
        // Combination: base="", priorVariables=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).interpolateHelper("", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_003() throws Exception {
        // Combination: base=" ", priorVariables=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).interpolateHelper(" ", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_004() throws Exception {
        // Combination: base=" ", priorVariables=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).interpolateHelper(" ", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_005() throws Exception {
        // Combination: base="a", priorVariables=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).interpolateHelper("a", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_006() throws Exception {
        // Combination: base="a", priorVariables=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).interpolateHelper("a", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_007() throws Exception {
        // Combination: base="test123", priorVariables=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).interpolateHelper("test123", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_008() throws Exception {
        // Combination: base="test123", priorVariables=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).interpolateHelper("test123", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_009() throws Exception {
        // Combination: base="!@#", priorVariables=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).interpolateHelper("!@#", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_010() throws Exception {
        // Combination: base="!@#", priorVariables=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).interpolateHelper("!@#", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_011() throws Exception {
        // Combination: base="0", priorVariables=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).interpolateHelper("0", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_012() throws Exception {
        // Combination: base="0", priorVariables=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).interpolateHelper("0", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_013() throws Exception {
        // Combination: base="-1", priorVariables=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).interpolateHelper("-1", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_014() throws Exception {
        // Combination: base="-1", priorVariables=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).interpolateHelper("-1", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_015() throws Exception {
        // Combination: base="1.5", priorVariables=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).interpolateHelper("1.5", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_016() throws Exception {
        // Combination: base="1.5", priorVariables=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).interpolateHelper("1.5", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_017() throws Exception {
        // Combination: base="9223372036854775807", priorVariables=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).interpolateHelper("9223372036854775807", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_018() throws Exception {
        // Combination: base="9223372036854775807", priorVariables=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).interpolateHelper("9223372036854775807", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_019() throws Exception {
        // Combination: base="9223372036854775808", priorVariables=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).interpolateHelper("9223372036854775808", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_020() throws Exception {
        // Combination: base="9223372036854775808", priorVariables=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).interpolateHelper("9223372036854775808", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_021() throws Exception {
        // Combination: base="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", priorVariables=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).interpolateHelper("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_interpolateHelper_pairwise_022() throws Exception {
        // Combination: base="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", priorVariables=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).interpolateHelper("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_023() throws Exception {
        // Combination: key="", defaultValue=""
        Object actual = (new ExtendedProperties()).getString("", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_024() throws Exception {
        // Combination: key=" ", defaultValue=""
        Object actual = (new ExtendedProperties()).getString(" ", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_025() throws Exception {
        // Combination: key="a", defaultValue=""
        Object actual = (new ExtendedProperties()).getString("a", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_026() throws Exception {
        // Combination: key="test123", defaultValue=""
        Object actual = (new ExtendedProperties()).getString("test123", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_027() throws Exception {
        // Combination: key="!@#", defaultValue=""
        Object actual = (new ExtendedProperties()).getString("!@#", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_028() throws Exception {
        // Combination: key="0", defaultValue=""
        Object actual = (new ExtendedProperties()).getString("0", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_029() throws Exception {
        // Combination: key="-1", defaultValue=""
        Object actual = (new ExtendedProperties()).getString("-1", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_030() throws Exception {
        // Combination: key="1.5", defaultValue=""
        Object actual = (new ExtendedProperties()).getString("1.5", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_031() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=""
        Object actual = (new ExtendedProperties()).getString("9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_032() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=""
        Object actual = (new ExtendedProperties()).getString("9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_033() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=""
        Object actual = (new ExtendedProperties()).getString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_034() throws Exception {
        // Combination: key="", defaultValue=" "
        Object actual = (new ExtendedProperties()).getString("", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_035() throws Exception {
        // Combination: key=" ", defaultValue=" "
        Object actual = (new ExtendedProperties()).getString(" ", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_036() throws Exception {
        // Combination: key="a", defaultValue=" "
        Object actual = (new ExtendedProperties()).getString("a", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_037() throws Exception {
        // Combination: key="test123", defaultValue=" "
        Object actual = (new ExtendedProperties()).getString("test123", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_038() throws Exception {
        // Combination: key="!@#", defaultValue=" "
        Object actual = (new ExtendedProperties()).getString("!@#", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_039() throws Exception {
        // Combination: key="0", defaultValue=" "
        Object actual = (new ExtendedProperties()).getString("0", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_040() throws Exception {
        // Combination: key="-1", defaultValue=" "
        Object actual = (new ExtendedProperties()).getString("-1", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_041() throws Exception {
        // Combination: key="1.5", defaultValue=" "
        Object actual = (new ExtendedProperties()).getString("1.5", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_042() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=" "
        Object actual = (new ExtendedProperties()).getString("9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_043() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=" "
        Object actual = (new ExtendedProperties()).getString("9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_044() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=" "
        Object actual = (new ExtendedProperties()).getString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals(" ", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_045() throws Exception {
        // Combination: key="", defaultValue="a"
        Object actual = (new ExtendedProperties()).getString("", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_046() throws Exception {
        // Combination: key=" ", defaultValue="a"
        Object actual = (new ExtendedProperties()).getString(" ", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_047() throws Exception {
        // Combination: key="a", defaultValue="a"
        Object actual = (new ExtendedProperties()).getString("a", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_048() throws Exception {
        // Combination: key="test123", defaultValue="a"
        Object actual = (new ExtendedProperties()).getString("test123", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_049() throws Exception {
        // Combination: key="!@#", defaultValue="a"
        Object actual = (new ExtendedProperties()).getString("!@#", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_050() throws Exception {
        // Combination: key="0", defaultValue="a"
        Object actual = (new ExtendedProperties()).getString("0", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_051() throws Exception {
        // Combination: key="-1", defaultValue="a"
        Object actual = (new ExtendedProperties()).getString("-1", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_052() throws Exception {
        // Combination: key="1.5", defaultValue="a"
        Object actual = (new ExtendedProperties()).getString("1.5", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_053() throws Exception {
        // Combination: key="9223372036854775807", defaultValue="a"
        Object actual = (new ExtendedProperties()).getString("9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_054() throws Exception {
        // Combination: key="9223372036854775808", defaultValue="a"
        Object actual = (new ExtendedProperties()).getString("9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_055() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="a"
        Object actual = (new ExtendedProperties()).getString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("a", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_056() throws Exception {
        // Combination: key="", defaultValue="test123"
        Object actual = (new ExtendedProperties()).getString("", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_057() throws Exception {
        // Combination: key=" ", defaultValue="test123"
        Object actual = (new ExtendedProperties()).getString(" ", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_058() throws Exception {
        // Combination: key="a", defaultValue="test123"
        Object actual = (new ExtendedProperties()).getString("a", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_059() throws Exception {
        // Combination: key="test123", defaultValue="test123"
        Object actual = (new ExtendedProperties()).getString("test123", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_060() throws Exception {
        // Combination: key="!@#", defaultValue="test123"
        Object actual = (new ExtendedProperties()).getString("!@#", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_061() throws Exception {
        // Combination: key="0", defaultValue="test123"
        Object actual = (new ExtendedProperties()).getString("0", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_062() throws Exception {
        // Combination: key="-1", defaultValue="test123"
        Object actual = (new ExtendedProperties()).getString("-1", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_063() throws Exception {
        // Combination: key="1.5", defaultValue="test123"
        Object actual = (new ExtendedProperties()).getString("1.5", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_064() throws Exception {
        // Combination: key="9223372036854775807", defaultValue="test123"
        Object actual = (new ExtendedProperties()).getString("9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_065() throws Exception {
        // Combination: key="9223372036854775808", defaultValue="test123"
        Object actual = (new ExtendedProperties()).getString("9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_066() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="test123"
        Object actual = (new ExtendedProperties()).getString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("test123", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_067() throws Exception {
        // Combination: key="", defaultValue="!@#"
        Object actual = (new ExtendedProperties()).getString("", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_068() throws Exception {
        // Combination: key=" ", defaultValue="!@#"
        Object actual = (new ExtendedProperties()).getString(" ", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_069() throws Exception {
        // Combination: key="a", defaultValue="!@#"
        Object actual = (new ExtendedProperties()).getString("a", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_070() throws Exception {
        // Combination: key="test123", defaultValue="!@#"
        Object actual = (new ExtendedProperties()).getString("test123", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_071() throws Exception {
        // Combination: key="!@#", defaultValue="!@#"
        Object actual = (new ExtendedProperties()).getString("!@#", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_072() throws Exception {
        // Combination: key="0", defaultValue="!@#"
        Object actual = (new ExtendedProperties()).getString("0", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_073() throws Exception {
        // Combination: key="-1", defaultValue="!@#"
        Object actual = (new ExtendedProperties()).getString("-1", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_074() throws Exception {
        // Combination: key="1.5", defaultValue="!@#"
        Object actual = (new ExtendedProperties()).getString("1.5", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_075() throws Exception {
        // Combination: key="9223372036854775807", defaultValue="!@#"
        Object actual = (new ExtendedProperties()).getString("9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_076() throws Exception {
        // Combination: key="9223372036854775808", defaultValue="!@#"
        Object actual = (new ExtendedProperties()).getString("9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_077() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="!@#"
        Object actual = (new ExtendedProperties()).getString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("!@#", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_078() throws Exception {
        // Combination: key="", defaultValue="0"
        Object actual = (new ExtendedProperties()).getString("", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_079() throws Exception {
        // Combination: key=" ", defaultValue="0"
        Object actual = (new ExtendedProperties()).getString(" ", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_080() throws Exception {
        // Combination: key="a", defaultValue="0"
        Object actual = (new ExtendedProperties()).getString("a", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_081() throws Exception {
        // Combination: key="test123", defaultValue="0"
        Object actual = (new ExtendedProperties()).getString("test123", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_082() throws Exception {
        // Combination: key="!@#", defaultValue="0"
        Object actual = (new ExtendedProperties()).getString("!@#", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_083() throws Exception {
        // Combination: key="0", defaultValue="0"
        Object actual = (new ExtendedProperties()).getString("0", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_084() throws Exception {
        // Combination: key="-1", defaultValue="0"
        Object actual = (new ExtendedProperties()).getString("-1", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_085() throws Exception {
        // Combination: key="1.5", defaultValue="0"
        Object actual = (new ExtendedProperties()).getString("1.5", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_086() throws Exception {
        // Combination: key="9223372036854775807", defaultValue="0"
        Object actual = (new ExtendedProperties()).getString("9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_087() throws Exception {
        // Combination: key="9223372036854775808", defaultValue="0"
        Object actual = (new ExtendedProperties()).getString("9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_088() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="0"
        Object actual = (new ExtendedProperties()).getString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_089() throws Exception {
        // Combination: key="", defaultValue="-1"
        Object actual = (new ExtendedProperties()).getString("", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_090() throws Exception {
        // Combination: key=" ", defaultValue="-1"
        Object actual = (new ExtendedProperties()).getString(" ", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_091() throws Exception {
        // Combination: key="a", defaultValue="-1"
        Object actual = (new ExtendedProperties()).getString("a", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_092() throws Exception {
        // Combination: key="test123", defaultValue="-1"
        Object actual = (new ExtendedProperties()).getString("test123", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_093() throws Exception {
        // Combination: key="!@#", defaultValue="-1"
        Object actual = (new ExtendedProperties()).getString("!@#", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_094() throws Exception {
        // Combination: key="0", defaultValue="-1"
        Object actual = (new ExtendedProperties()).getString("0", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_095() throws Exception {
        // Combination: key="-1", defaultValue="-1"
        Object actual = (new ExtendedProperties()).getString("-1", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_096() throws Exception {
        // Combination: key="1.5", defaultValue="-1"
        Object actual = (new ExtendedProperties()).getString("1.5", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_097() throws Exception {
        // Combination: key="9223372036854775807", defaultValue="-1"
        Object actual = (new ExtendedProperties()).getString("9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_098() throws Exception {
        // Combination: key="9223372036854775808", defaultValue="-1"
        Object actual = (new ExtendedProperties()).getString("9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_099() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="-1"
        Object actual = (new ExtendedProperties()).getString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_100() throws Exception {
        // Combination: key="", defaultValue="1.5"
        Object actual = (new ExtendedProperties()).getString("", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_101() throws Exception {
        // Combination: key=" ", defaultValue="1.5"
        Object actual = (new ExtendedProperties()).getString(" ", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_102() throws Exception {
        // Combination: key="a", defaultValue="1.5"
        Object actual = (new ExtendedProperties()).getString("a", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_103() throws Exception {
        // Combination: key="test123", defaultValue="1.5"
        Object actual = (new ExtendedProperties()).getString("test123", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_104() throws Exception {
        // Combination: key="!@#", defaultValue="1.5"
        Object actual = (new ExtendedProperties()).getString("!@#", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_105() throws Exception {
        // Combination: key="0", defaultValue="1.5"
        Object actual = (new ExtendedProperties()).getString("0", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_106() throws Exception {
        // Combination: key="-1", defaultValue="1.5"
        Object actual = (new ExtendedProperties()).getString("-1", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_107() throws Exception {
        // Combination: key="1.5", defaultValue="1.5"
        Object actual = (new ExtendedProperties()).getString("1.5", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_108() throws Exception {
        // Combination: key="9223372036854775807", defaultValue="1.5"
        Object actual = (new ExtendedProperties()).getString("9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_109() throws Exception {
        // Combination: key="9223372036854775808", defaultValue="1.5"
        Object actual = (new ExtendedProperties()).getString("9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_110() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="1.5"
        Object actual = (new ExtendedProperties()).getString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("1.5", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_111() throws Exception {
        // Combination: key="", defaultValue="9223372036854775807"
        Object actual = (new ExtendedProperties()).getString("", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_112() throws Exception {
        // Combination: key=" ", defaultValue="9223372036854775807"
        Object actual = (new ExtendedProperties()).getString(" ", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_113() throws Exception {
        // Combination: key="a", defaultValue="9223372036854775807"
        Object actual = (new ExtendedProperties()).getString("a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_114() throws Exception {
        // Combination: key="test123", defaultValue="9223372036854775807"
        Object actual = (new ExtendedProperties()).getString("test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_115() throws Exception {
        // Combination: key="!@#", defaultValue="9223372036854775807"
        Object actual = (new ExtendedProperties()).getString("!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_116() throws Exception {
        // Combination: key="0", defaultValue="9223372036854775807"
        Object actual = (new ExtendedProperties()).getString("0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_117() throws Exception {
        // Combination: key="-1", defaultValue="9223372036854775807"
        Object actual = (new ExtendedProperties()).getString("-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_118() throws Exception {
        // Combination: key="1.5", defaultValue="9223372036854775807"
        Object actual = (new ExtendedProperties()).getString("1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_119() throws Exception {
        // Combination: key="9223372036854775807", defaultValue="9223372036854775807"
        Object actual = (new ExtendedProperties()).getString("9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_120() throws Exception {
        // Combination: key="9223372036854775808", defaultValue="9223372036854775807"
        Object actual = (new ExtendedProperties()).getString("9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_121() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="9223372036854775807"
        Object actual = (new ExtendedProperties()).getString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_122() throws Exception {
        // Combination: key="", defaultValue="9223372036854775808"
        Object actual = (new ExtendedProperties()).getString("", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_123() throws Exception {
        // Combination: key=" ", defaultValue="9223372036854775808"
        Object actual = (new ExtendedProperties()).getString(" ", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_124() throws Exception {
        // Combination: key="a", defaultValue="9223372036854775808"
        Object actual = (new ExtendedProperties()).getString("a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_125() throws Exception {
        // Combination: key="test123", defaultValue="9223372036854775808"
        Object actual = (new ExtendedProperties()).getString("test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_126() throws Exception {
        // Combination: key="!@#", defaultValue="9223372036854775808"
        Object actual = (new ExtendedProperties()).getString("!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_127() throws Exception {
        // Combination: key="0", defaultValue="9223372036854775808"
        Object actual = (new ExtendedProperties()).getString("0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_128() throws Exception {
        // Combination: key="-1", defaultValue="9223372036854775808"
        Object actual = (new ExtendedProperties()).getString("-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_129() throws Exception {
        // Combination: key="1.5", defaultValue="9223372036854775808"
        Object actual = (new ExtendedProperties()).getString("1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_130() throws Exception {
        // Combination: key="9223372036854775807", defaultValue="9223372036854775808"
        Object actual = (new ExtendedProperties()).getString("9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_131() throws Exception {
        // Combination: key="9223372036854775808", defaultValue="9223372036854775808"
        Object actual = (new ExtendedProperties()).getString("9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_132() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="9223372036854775808"
        Object actual = (new ExtendedProperties()).getString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_133() throws Exception {
        // Combination: key="", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new ExtendedProperties()).getString("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_134() throws Exception {
        // Combination: key=" ", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new ExtendedProperties()).getString(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_135() throws Exception {
        // Combination: key="a", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new ExtendedProperties()).getString("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_136() throws Exception {
        // Combination: key="test123", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new ExtendedProperties()).getString("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_137() throws Exception {
        // Combination: key="!@#", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new ExtendedProperties()).getString("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_138() throws Exception {
        // Combination: key="0", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new ExtendedProperties()).getString("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_139() throws Exception {
        // Combination: key="-1", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new ExtendedProperties()).getString("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_140() throws Exception {
        // Combination: key="1.5", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new ExtendedProperties()).getString("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_141() throws Exception {
        // Combination: key="9223372036854775807", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new ExtendedProperties()).getString("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_142() throws Exception {
        // Combination: key="9223372036854775808", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new ExtendedProperties()).getString("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getString_pairwise_143() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new ExtendedProperties()).getString("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_144() throws Exception {
        // Combination: key="", defaultValue=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).getList("", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.Collections$EmptyList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_145() throws Exception {
        // Combination: key=" ", defaultValue=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).getList(" ", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.Collections$EmptyList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_146() throws Exception {
        // Combination: key="a", defaultValue=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).getList("a", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.Collections$EmptyList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_147() throws Exception {
        // Combination: key="test123", defaultValue=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).getList("test123", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.Collections$EmptyList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_148() throws Exception {
        // Combination: key="!@#", defaultValue=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).getList("!@#", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.Collections$EmptyList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_149() throws Exception {
        // Combination: key="0", defaultValue=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).getList("0", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.Collections$EmptyList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_150() throws Exception {
        // Combination: key="-1", defaultValue=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).getList("-1", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.Collections$EmptyList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_151() throws Exception {
        // Combination: key="1.5", defaultValue=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).getList("1.5", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.Collections$EmptyList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_152() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).getList("9223372036854775807", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.Collections$EmptyList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_153() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).getList("9223372036854775808", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.Collections$EmptyList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_154() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=java.util.Collections.emptyList()
        Object actual = (new ExtendedProperties()).getList("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.Collections.emptyList());
        assertNotNull(actual);
        assertEquals("java.util.Collections$EmptyList", actual.getClass().getName());
        assertEquals("[]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_155() throws Exception {
        // Combination: key="", defaultValue=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).getList("", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.Arrays$ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_156() throws Exception {
        // Combination: key=" ", defaultValue=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).getList(" ", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.Arrays$ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_157() throws Exception {
        // Combination: key="a", defaultValue=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).getList("a", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.Arrays$ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_158() throws Exception {
        // Combination: key="test123", defaultValue=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).getList("test123", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.Arrays$ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_159() throws Exception {
        // Combination: key="!@#", defaultValue=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).getList("!@#", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.Arrays$ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_160() throws Exception {
        // Combination: key="0", defaultValue=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).getList("0", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.Arrays$ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_161() throws Exception {
        // Combination: key="-1", defaultValue=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).getList("-1", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.Arrays$ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_162() throws Exception {
        // Combination: key="1.5", defaultValue=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).getList("1.5", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.Arrays$ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_163() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).getList("9223372036854775807", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.Arrays$ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_164() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).getList("9223372036854775808", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.Arrays$ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getList_pairwise_165() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=java.util.Arrays.asList("a", "b")
        Object actual = (new ExtendedProperties()).getList("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", java.util.Arrays.asList("a", "b"));
        assertNotNull(actual);
        assertEquals("java.util.Arrays$ArrayList", actual.getClass().getName());
        assertEquals("[a, b]", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_166() throws Exception {
        // Combination: key="", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_167() throws Exception {
        // Combination: key=" ", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean(" ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_168() throws Exception {
        // Combination: key="a", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_169() throws Exception {
        // Combination: key="test123", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_170() throws Exception {
        // Combination: key="!@#", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_171() throws Exception {
        // Combination: key="0", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_172() throws Exception {
        // Combination: key="-1", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_173() throws Exception {
        // Combination: key="1.5", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_174() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_175() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_176() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_177() throws Exception {
        // Combination: key="", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_178() throws Exception {
        // Combination: key=" ", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean(" ", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_179() throws Exception {
        // Combination: key="a", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("a", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_180() throws Exception {
        // Combination: key="test123", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("test123", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_181() throws Exception {
        // Combination: key="!@#", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("!@#", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_182() throws Exception {
        // Combination: key="0", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("0", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_183() throws Exception {
        // Combination: key="-1", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("-1", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_184() throws Exception {
        // Combination: key="1.5", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("1.5", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_185() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("9223372036854775807", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_186() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("9223372036854775808", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_187() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_188() throws Exception {
        // Combination: key="", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_189() throws Exception {
        // Combination: key=" ", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean(" ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_190() throws Exception {
        // Combination: key="a", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_191() throws Exception {
        // Combination: key="test123", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_192() throws Exception {
        // Combination: key="!@#", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_193() throws Exception {
        // Combination: key="0", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_194() throws Exception {
        // Combination: key="-1", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_195() throws Exception {
        // Combination: key="1.5", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_196() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_197() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_198() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=true
        Object actual = (new ExtendedProperties()).getBoolean("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_199() throws Exception {
        // Combination: key="", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_200() throws Exception {
        // Combination: key=" ", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean(" ", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_201() throws Exception {
        // Combination: key="a", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("a", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_202() throws Exception {
        // Combination: key="test123", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("test123", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_203() throws Exception {
        // Combination: key="!@#", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("!@#", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_204() throws Exception {
        // Combination: key="0", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("0", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_205() throws Exception {
        // Combination: key="-1", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("-1", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_206() throws Exception {
        // Combination: key="1.5", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("1.5", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_207() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("9223372036854775807", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_208() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("9223372036854775808", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getBoolean_pairwise_209() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=false
        Object actual = (new ExtendedProperties()).getBoolean("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_210() throws Exception {
        // Combination: key="", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_211() throws Exception {
        // Combination: key=" ", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte(" ", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_212() throws Exception {
        // Combination: key="a", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("a", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_213() throws Exception {
        // Combination: key="test123", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("test123", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_214() throws Exception {
        // Combination: key="!@#", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("!@#", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_215() throws Exception {
        // Combination: key="0", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("0", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_216() throws Exception {
        // Combination: key="-1", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("-1", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_217() throws Exception {
        // Combination: key="1.5", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("1.5", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_218() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("9223372036854775807", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_219() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("9223372036854775808", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_220() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_221() throws Exception {
        // Combination: key="", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_222() throws Exception {
        // Combination: key=" ", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte(" ", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_223() throws Exception {
        // Combination: key="a", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("a", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_224() throws Exception {
        // Combination: key="test123", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("test123", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_225() throws Exception {
        // Combination: key="!@#", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("!@#", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_226() throws Exception {
        // Combination: key="0", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("0", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_227() throws Exception {
        // Combination: key="-1", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("-1", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_228() throws Exception {
        // Combination: key="1.5", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("1.5", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_229() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("9223372036854775807", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_230() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("9223372036854775808", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_231() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_232() throws Exception {
        // Combination: key="", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_233() throws Exception {
        // Combination: key=" ", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte(" ", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_234() throws Exception {
        // Combination: key="a", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("a", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_235() throws Exception {
        // Combination: key="test123", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("test123", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_236() throws Exception {
        // Combination: key="!@#", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("!@#", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_237() throws Exception {
        // Combination: key="0", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("0", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_238() throws Exception {
        // Combination: key="-1", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("-1", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_239() throws Exception {
        // Combination: key="1.5", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("1.5", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_240() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("9223372036854775807", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_241() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("9223372036854775808", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_242() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_243() throws Exception {
        // Combination: key="", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_244() throws Exception {
        // Combination: key=" ", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte(" ", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_245() throws Exception {
        // Combination: key="a", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("a", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_246() throws Exception {
        // Combination: key="test123", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("test123", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_247() throws Exception {
        // Combination: key="!@#", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("!@#", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_248() throws Exception {
        // Combination: key="0", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("0", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_249() throws Exception {
        // Combination: key="-1", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("-1", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_250() throws Exception {
        // Combination: key="1.5", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("1.5", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_251() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("9223372036854775807", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_252() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("9223372036854775808", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_253() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_254() throws Exception {
        // Combination: key="", defaultValue=Byte.MIN_VALUE
        Object actual = (new ExtendedProperties()).getByte("", Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_255() throws Exception {
        // Combination: key=" ", defaultValue=Byte.MIN_VALUE
        Object actual = (new ExtendedProperties()).getByte(" ", Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_256() throws Exception {
        // Combination: key="a", defaultValue=Byte.MIN_VALUE
        Object actual = (new ExtendedProperties()).getByte("a", Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_257() throws Exception {
        // Combination: key="test123", defaultValue=Byte.MIN_VALUE
        Object actual = (new ExtendedProperties()).getByte("test123", Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_258() throws Exception {
        // Combination: key="!@#", defaultValue=Byte.MIN_VALUE
        Object actual = (new ExtendedProperties()).getByte("!@#", Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_259() throws Exception {
        // Combination: key="0", defaultValue=Byte.MIN_VALUE
        Object actual = (new ExtendedProperties()).getByte("0", Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_260() throws Exception {
        // Combination: key="-1", defaultValue=Byte.MIN_VALUE
        Object actual = (new ExtendedProperties()).getByte("-1", Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_261() throws Exception {
        // Combination: key="1.5", defaultValue=Byte.MIN_VALUE
        Object actual = (new ExtendedProperties()).getByte("1.5", Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_262() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Byte.MIN_VALUE
        Object actual = (new ExtendedProperties()).getByte("9223372036854775807", Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_263() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Byte.MIN_VALUE
        Object actual = (new ExtendedProperties()).getByte("9223372036854775808", Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_264() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Byte.MIN_VALUE
        Object actual = (new ExtendedProperties()).getByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Byte.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-128", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_265() throws Exception {
        // Combination: key="", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_266() throws Exception {
        // Combination: key=" ", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte(" ", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_267() throws Exception {
        // Combination: key="a", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("a", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_268() throws Exception {
        // Combination: key="test123", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("test123", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_269() throws Exception {
        // Combination: key="!@#", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("!@#", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_270() throws Exception {
        // Combination: key="0", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("0", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_271() throws Exception {
        // Combination: key="-1", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("-1", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_272() throws Exception {
        // Combination: key="1.5", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("1.5", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_273() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("9223372036854775807", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_274() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("9223372036854775808", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_275() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=(byte) 0
        Object actual = (new ExtendedProperties()).getByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", (byte) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_276() throws Exception {
        // Combination: key="", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_277() throws Exception {
        // Combination: key=" ", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte(" ", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_278() throws Exception {
        // Combination: key="a", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("a", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_279() throws Exception {
        // Combination: key="test123", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("test123", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_280() throws Exception {
        // Combination: key="!@#", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("!@#", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_281() throws Exception {
        // Combination: key="0", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("0", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_282() throws Exception {
        // Combination: key="-1", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("-1", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_283() throws Exception {
        // Combination: key="1.5", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("1.5", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_284() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("9223372036854775807", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_285() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("9223372036854775808", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_286() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=(byte) 1
        Object actual = (new ExtendedProperties()).getByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", (byte) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_287() throws Exception {
        // Combination: key="", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_288() throws Exception {
        // Combination: key=" ", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte(" ", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_289() throws Exception {
        // Combination: key="a", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("a", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_290() throws Exception {
        // Combination: key="test123", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("test123", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_291() throws Exception {
        // Combination: key="!@#", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("!@#", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_292() throws Exception {
        // Combination: key="0", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("0", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_293() throws Exception {
        // Combination: key="-1", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("-1", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_294() throws Exception {
        // Combination: key="1.5", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("1.5", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_295() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("9223372036854775807", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_296() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("9223372036854775808", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_297() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=(byte) -1
        Object actual = (new ExtendedProperties()).getByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", (byte) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_298() throws Exception {
        // Combination: key="", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_299() throws Exception {
        // Combination: key=" ", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte(" ", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_300() throws Exception {
        // Combination: key="a", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("a", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_301() throws Exception {
        // Combination: key="test123", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("test123", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_302() throws Exception {
        // Combination: key="!@#", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("!@#", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_303() throws Exception {
        // Combination: key="0", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("0", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_304() throws Exception {
        // Combination: key="-1", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("-1", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_305() throws Exception {
        // Combination: key="1.5", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("1.5", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_306() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("9223372036854775807", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_307() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("9223372036854775808", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getByte_pairwise_308() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Byte.MAX_VALUE
        Object actual = (new ExtendedProperties()).getByte("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Byte.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Byte", actual.getClass().getName());
        assertEquals("127", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_309() throws Exception {
        // Combination: key="", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_310() throws Exception {
        // Combination: key=" ", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort(" ", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_311() throws Exception {
        // Combination: key="a", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("a", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_312() throws Exception {
        // Combination: key="test123", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("test123", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_313() throws Exception {
        // Combination: key="!@#", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("!@#", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_314() throws Exception {
        // Combination: key="0", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("0", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_315() throws Exception {
        // Combination: key="-1", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("-1", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_316() throws Exception {
        // Combination: key="1.5", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("1.5", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_317() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("9223372036854775807", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_318() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("9223372036854775808", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_319() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_320() throws Exception {
        // Combination: key="", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_321() throws Exception {
        // Combination: key=" ", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort(" ", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_322() throws Exception {
        // Combination: key="a", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("a", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_323() throws Exception {
        // Combination: key="test123", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("test123", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_324() throws Exception {
        // Combination: key="!@#", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("!@#", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_325() throws Exception {
        // Combination: key="0", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("0", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_326() throws Exception {
        // Combination: key="-1", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("-1", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_327() throws Exception {
        // Combination: key="1.5", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("1.5", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_328() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("9223372036854775807", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_329() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("9223372036854775808", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_330() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_331() throws Exception {
        // Combination: key="", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_332() throws Exception {
        // Combination: key=" ", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort(" ", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_333() throws Exception {
        // Combination: key="a", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("a", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_334() throws Exception {
        // Combination: key="test123", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("test123", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_335() throws Exception {
        // Combination: key="!@#", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("!@#", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_336() throws Exception {
        // Combination: key="0", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("0", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_337() throws Exception {
        // Combination: key="-1", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("-1", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_338() throws Exception {
        // Combination: key="1.5", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("1.5", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_339() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("9223372036854775807", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_340() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("9223372036854775808", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_341() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_342() throws Exception {
        // Combination: key="", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_343() throws Exception {
        // Combination: key=" ", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort(" ", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_344() throws Exception {
        // Combination: key="a", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("a", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_345() throws Exception {
        // Combination: key="test123", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("test123", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_346() throws Exception {
        // Combination: key="!@#", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("!@#", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_347() throws Exception {
        // Combination: key="0", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("0", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_348() throws Exception {
        // Combination: key="-1", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("-1", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_349() throws Exception {
        // Combination: key="1.5", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("1.5", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_350() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("9223372036854775807", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_351() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("9223372036854775808", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_352() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_353() throws Exception {
        // Combination: key="", defaultValue=Short.MIN_VALUE
        Object actual = (new ExtendedProperties()).getShort("", Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_354() throws Exception {
        // Combination: key=" ", defaultValue=Short.MIN_VALUE
        Object actual = (new ExtendedProperties()).getShort(" ", Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_355() throws Exception {
        // Combination: key="a", defaultValue=Short.MIN_VALUE
        Object actual = (new ExtendedProperties()).getShort("a", Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_356() throws Exception {
        // Combination: key="test123", defaultValue=Short.MIN_VALUE
        Object actual = (new ExtendedProperties()).getShort("test123", Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_357() throws Exception {
        // Combination: key="!@#", defaultValue=Short.MIN_VALUE
        Object actual = (new ExtendedProperties()).getShort("!@#", Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_358() throws Exception {
        // Combination: key="0", defaultValue=Short.MIN_VALUE
        Object actual = (new ExtendedProperties()).getShort("0", Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_359() throws Exception {
        // Combination: key="-1", defaultValue=Short.MIN_VALUE
        Object actual = (new ExtendedProperties()).getShort("-1", Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_360() throws Exception {
        // Combination: key="1.5", defaultValue=Short.MIN_VALUE
        Object actual = (new ExtendedProperties()).getShort("1.5", Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_361() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Short.MIN_VALUE
        Object actual = (new ExtendedProperties()).getShort("9223372036854775807", Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_362() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Short.MIN_VALUE
        Object actual = (new ExtendedProperties()).getShort("9223372036854775808", Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_363() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Short.MIN_VALUE
        Object actual = (new ExtendedProperties()).getShort("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Short.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-32768", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_364() throws Exception {
        // Combination: key="", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_365() throws Exception {
        // Combination: key=" ", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort(" ", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_366() throws Exception {
        // Combination: key="a", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("a", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_367() throws Exception {
        // Combination: key="test123", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("test123", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_368() throws Exception {
        // Combination: key="!@#", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("!@#", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_369() throws Exception {
        // Combination: key="0", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("0", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_370() throws Exception {
        // Combination: key="-1", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("-1", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_371() throws Exception {
        // Combination: key="1.5", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("1.5", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_372() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("9223372036854775807", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_373() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("9223372036854775808", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_374() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=(short) 0
        Object actual = (new ExtendedProperties()).getShort("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", (short) 0);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_375() throws Exception {
        // Combination: key="", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_376() throws Exception {
        // Combination: key=" ", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort(" ", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_377() throws Exception {
        // Combination: key="a", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("a", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_378() throws Exception {
        // Combination: key="test123", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("test123", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_379() throws Exception {
        // Combination: key="!@#", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("!@#", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_380() throws Exception {
        // Combination: key="0", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("0", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_381() throws Exception {
        // Combination: key="-1", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("-1", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_382() throws Exception {
        // Combination: key="1.5", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("1.5", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_383() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("9223372036854775807", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_384() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("9223372036854775808", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_385() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=(short) 1
        Object actual = (new ExtendedProperties()).getShort("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", (short) 1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_386() throws Exception {
        // Combination: key="", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_387() throws Exception {
        // Combination: key=" ", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort(" ", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_388() throws Exception {
        // Combination: key="a", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("a", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_389() throws Exception {
        // Combination: key="test123", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("test123", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_390() throws Exception {
        // Combination: key="!@#", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("!@#", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_391() throws Exception {
        // Combination: key="0", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("0", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_392() throws Exception {
        // Combination: key="-1", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("-1", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_393() throws Exception {
        // Combination: key="1.5", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("1.5", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_394() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("9223372036854775807", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_395() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("9223372036854775808", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_396() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=(short) -1
        Object actual = (new ExtendedProperties()).getShort("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", (short) -1);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_397() throws Exception {
        // Combination: key="", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_398() throws Exception {
        // Combination: key=" ", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort(" ", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_399() throws Exception {
        // Combination: key="a", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("a", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_400() throws Exception {
        // Combination: key="test123", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("test123", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_401() throws Exception {
        // Combination: key="!@#", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("!@#", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_402() throws Exception {
        // Combination: key="0", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("0", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_403() throws Exception {
        // Combination: key="-1", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("-1", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_404() throws Exception {
        // Combination: key="1.5", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("1.5", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_405() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("9223372036854775807", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_406() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("9223372036854775808", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getShort_pairwise_407() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Short.MAX_VALUE
        Object actual = (new ExtendedProperties()).getShort("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Short.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Short", actual.getClass().getName());
        assertEquals("32767", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_408() throws Exception {
        // Combination: name="", def=0
        Object actual = (new ExtendedProperties()).getInt("", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_409() throws Exception {
        // Combination: name=" ", def=0
        Object actual = (new ExtendedProperties()).getInt(" ", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_410() throws Exception {
        // Combination: name="a", def=0
        Object actual = (new ExtendedProperties()).getInt("a", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_411() throws Exception {
        // Combination: name="test123", def=0
        Object actual = (new ExtendedProperties()).getInt("test123", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_412() throws Exception {
        // Combination: name="!@#", def=0
        Object actual = (new ExtendedProperties()).getInt("!@#", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_413() throws Exception {
        // Combination: name="0", def=0
        Object actual = (new ExtendedProperties()).getInt("0", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_414() throws Exception {
        // Combination: name="-1", def=0
        Object actual = (new ExtendedProperties()).getInt("-1", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_415() throws Exception {
        // Combination: name="1.5", def=0
        Object actual = (new ExtendedProperties()).getInt("1.5", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_416() throws Exception {
        // Combination: name="9223372036854775807", def=0
        Object actual = (new ExtendedProperties()).getInt("9223372036854775807", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_417() throws Exception {
        // Combination: name="9223372036854775808", def=0
        Object actual = (new ExtendedProperties()).getInt("9223372036854775808", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_418() throws Exception {
        // Combination: name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", def=0
        Object actual = (new ExtendedProperties()).getInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_419() throws Exception {
        // Combination: name="", def=1
        Object actual = (new ExtendedProperties()).getInt("", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_420() throws Exception {
        // Combination: name=" ", def=1
        Object actual = (new ExtendedProperties()).getInt(" ", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_421() throws Exception {
        // Combination: name="a", def=1
        Object actual = (new ExtendedProperties()).getInt("a", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_422() throws Exception {
        // Combination: name="test123", def=1
        Object actual = (new ExtendedProperties()).getInt("test123", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_423() throws Exception {
        // Combination: name="!@#", def=1
        Object actual = (new ExtendedProperties()).getInt("!@#", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_424() throws Exception {
        // Combination: name="0", def=1
        Object actual = (new ExtendedProperties()).getInt("0", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_425() throws Exception {
        // Combination: name="-1", def=1
        Object actual = (new ExtendedProperties()).getInt("-1", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_426() throws Exception {
        // Combination: name="1.5", def=1
        Object actual = (new ExtendedProperties()).getInt("1.5", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_427() throws Exception {
        // Combination: name="9223372036854775807", def=1
        Object actual = (new ExtendedProperties()).getInt("9223372036854775807", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_428() throws Exception {
        // Combination: name="9223372036854775808", def=1
        Object actual = (new ExtendedProperties()).getInt("9223372036854775808", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_429() throws Exception {
        // Combination: name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", def=1
        Object actual = (new ExtendedProperties()).getInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_430() throws Exception {
        // Combination: name="", def=-1
        Object actual = (new ExtendedProperties()).getInt("", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_431() throws Exception {
        // Combination: name=" ", def=-1
        Object actual = (new ExtendedProperties()).getInt(" ", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_432() throws Exception {
        // Combination: name="a", def=-1
        Object actual = (new ExtendedProperties()).getInt("a", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_433() throws Exception {
        // Combination: name="test123", def=-1
        Object actual = (new ExtendedProperties()).getInt("test123", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_434() throws Exception {
        // Combination: name="!@#", def=-1
        Object actual = (new ExtendedProperties()).getInt("!@#", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_435() throws Exception {
        // Combination: name="0", def=-1
        Object actual = (new ExtendedProperties()).getInt("0", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_436() throws Exception {
        // Combination: name="-1", def=-1
        Object actual = (new ExtendedProperties()).getInt("-1", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_437() throws Exception {
        // Combination: name="1.5", def=-1
        Object actual = (new ExtendedProperties()).getInt("1.5", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_438() throws Exception {
        // Combination: name="9223372036854775807", def=-1
        Object actual = (new ExtendedProperties()).getInt("9223372036854775807", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_439() throws Exception {
        // Combination: name="9223372036854775808", def=-1
        Object actual = (new ExtendedProperties()).getInt("9223372036854775808", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_440() throws Exception {
        // Combination: name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", def=-1
        Object actual = (new ExtendedProperties()).getInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_441() throws Exception {
        // Combination: name="", def=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInt("", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_442() throws Exception {
        // Combination: name=" ", def=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInt(" ", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_443() throws Exception {
        // Combination: name="a", def=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInt("a", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_444() throws Exception {
        // Combination: name="test123", def=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInt("test123", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_445() throws Exception {
        // Combination: name="!@#", def=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInt("!@#", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_446() throws Exception {
        // Combination: name="0", def=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInt("0", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_447() throws Exception {
        // Combination: name="-1", def=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInt("-1", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_448() throws Exception {
        // Combination: name="1.5", def=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInt("1.5", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_449() throws Exception {
        // Combination: name="9223372036854775807", def=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInt("9223372036854775807", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_450() throws Exception {
        // Combination: name="9223372036854775808", def=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInt("9223372036854775808", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_451() throws Exception {
        // Combination: name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", def=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_452() throws Exception {
        // Combination: name="", def=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInt("", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_453() throws Exception {
        // Combination: name=" ", def=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInt(" ", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_454() throws Exception {
        // Combination: name="a", def=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInt("a", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_455() throws Exception {
        // Combination: name="test123", def=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInt("test123", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_456() throws Exception {
        // Combination: name="!@#", def=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInt("!@#", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_457() throws Exception {
        // Combination: name="0", def=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInt("0", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_458() throws Exception {
        // Combination: name="-1", def=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInt("-1", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_459() throws Exception {
        // Combination: name="1.5", def=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInt("1.5", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_460() throws Exception {
        // Combination: name="9223372036854775807", def=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInt("9223372036854775807", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_461() throws Exception {
        // Combination: name="9223372036854775808", def=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInt("9223372036854775808", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInt_pairwise_462() throws Exception {
        // Combination: name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", def=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_463() throws Exception {
        // Combination: key="", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_464() throws Exception {
        // Combination: key=" ", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger(" ", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_465() throws Exception {
        // Combination: key="a", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("a", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_466() throws Exception {
        // Combination: key="test123", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("test123", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_467() throws Exception {
        // Combination: key="!@#", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("!@#", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_468() throws Exception {
        // Combination: key="0", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("0", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_469() throws Exception {
        // Combination: key="-1", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("-1", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_470() throws Exception {
        // Combination: key="1.5", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("1.5", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_471() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775807", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_472() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775808", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_473() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_474() throws Exception {
        // Combination: key="", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_475() throws Exception {
        // Combination: key=" ", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger(" ", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_476() throws Exception {
        // Combination: key="a", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("a", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_477() throws Exception {
        // Combination: key="test123", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("test123", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_478() throws Exception {
        // Combination: key="!@#", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("!@#", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_479() throws Exception {
        // Combination: key="0", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("0", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_480() throws Exception {
        // Combination: key="-1", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("-1", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_481() throws Exception {
        // Combination: key="1.5", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("1.5", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_482() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775807", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_483() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775808", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_484() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_485() throws Exception {
        // Combination: key="", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_486() throws Exception {
        // Combination: key=" ", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger(" ", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_487() throws Exception {
        // Combination: key="a", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("a", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_488() throws Exception {
        // Combination: key="test123", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("test123", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_489() throws Exception {
        // Combination: key="!@#", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("!@#", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_490() throws Exception {
        // Combination: key="0", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("0", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_491() throws Exception {
        // Combination: key="-1", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("-1", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_492() throws Exception {
        // Combination: key="1.5", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("1.5", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_493() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775807", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_494() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775808", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_495() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_496() throws Exception {
        // Combination: key="", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_497() throws Exception {
        // Combination: key=" ", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger(" ", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_498() throws Exception {
        // Combination: key="a", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("a", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_499() throws Exception {
        // Combination: key="test123", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("test123", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_500() throws Exception {
        // Combination: key="!@#", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("!@#", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_501() throws Exception {
        // Combination: key="0", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("0", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_502() throws Exception {
        // Combination: key="-1", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("-1", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_503() throws Exception {
        // Combination: key="1.5", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("1.5", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_504() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775807", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_505() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775808", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_506() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_507() throws Exception {
        // Combination: key="", defaultValue=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInteger("", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_508() throws Exception {
        // Combination: key=" ", defaultValue=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInteger(" ", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_509() throws Exception {
        // Combination: key="a", defaultValue=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInteger("a", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_510() throws Exception {
        // Combination: key="test123", defaultValue=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInteger("test123", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_511() throws Exception {
        // Combination: key="!@#", defaultValue=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInteger("!@#", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_512() throws Exception {
        // Combination: key="0", defaultValue=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInteger("0", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_513() throws Exception {
        // Combination: key="-1", defaultValue=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInteger("-1", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_514() throws Exception {
        // Combination: key="1.5", defaultValue=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInteger("1.5", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_515() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775807", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_516() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775808", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_517() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Integer.MIN_VALUE
        Object actual = (new ExtendedProperties()).getInteger("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_518() throws Exception {
        // Combination: key="", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_519() throws Exception {
        // Combination: key=" ", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger(" ", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_520() throws Exception {
        // Combination: key="a", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("a", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_521() throws Exception {
        // Combination: key="test123", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("test123", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_522() throws Exception {
        // Combination: key="!@#", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("!@#", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_523() throws Exception {
        // Combination: key="0", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("0", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_524() throws Exception {
        // Combination: key="-1", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("-1", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_525() throws Exception {
        // Combination: key="1.5", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("1.5", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_526() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775807", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_527() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775808", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_528() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0
        Object actual = (new ExtendedProperties()).getInteger("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_529() throws Exception {
        // Combination: key="", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_530() throws Exception {
        // Combination: key=" ", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger(" ", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_531() throws Exception {
        // Combination: key="a", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("a", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_532() throws Exception {
        // Combination: key="test123", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("test123", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_533() throws Exception {
        // Combination: key="!@#", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("!@#", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_534() throws Exception {
        // Combination: key="0", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("0", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_535() throws Exception {
        // Combination: key="-1", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("-1", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_536() throws Exception {
        // Combination: key="1.5", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("1.5", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_537() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775807", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_538() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775808", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_539() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1
        Object actual = (new ExtendedProperties()).getInteger("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_540() throws Exception {
        // Combination: key="", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_541() throws Exception {
        // Combination: key=" ", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger(" ", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_542() throws Exception {
        // Combination: key="a", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("a", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_543() throws Exception {
        // Combination: key="test123", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("test123", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_544() throws Exception {
        // Combination: key="!@#", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("!@#", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_545() throws Exception {
        // Combination: key="0", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("0", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_546() throws Exception {
        // Combination: key="-1", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("-1", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_547() throws Exception {
        // Combination: key="1.5", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("1.5", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_548() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775807", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_549() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775808", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_550() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1
        Object actual = (new ExtendedProperties()).getInteger("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_551() throws Exception {
        // Combination: key="", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_552() throws Exception {
        // Combination: key=" ", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger(" ", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_553() throws Exception {
        // Combination: key="a", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("a", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_554() throws Exception {
        // Combination: key="test123", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("test123", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_555() throws Exception {
        // Combination: key="!@#", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("!@#", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_556() throws Exception {
        // Combination: key="0", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("0", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_557() throws Exception {
        // Combination: key="-1", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("-1", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_558() throws Exception {
        // Combination: key="1.5", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("1.5", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_559() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775807", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_560() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("9223372036854775808", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getInteger_pairwise_561() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Integer.MAX_VALUE
        Object actual = (new ExtendedProperties()).getInteger("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_562() throws Exception {
        // Combination: key="", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_563() throws Exception {
        // Combination: key=" ", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong(" ", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_564() throws Exception {
        // Combination: key="a", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("a", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_565() throws Exception {
        // Combination: key="test123", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("test123", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_566() throws Exception {
        // Combination: key="!@#", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("!@#", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_567() throws Exception {
        // Combination: key="0", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("0", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_568() throws Exception {
        // Combination: key="-1", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("-1", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_569() throws Exception {
        // Combination: key="1.5", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("1.5", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_570() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("9223372036854775807", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_571() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("9223372036854775808", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_572() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_573() throws Exception {
        // Combination: key="", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_574() throws Exception {
        // Combination: key=" ", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong(" ", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_575() throws Exception {
        // Combination: key="a", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("a", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_576() throws Exception {
        // Combination: key="test123", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("test123", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_577() throws Exception {
        // Combination: key="!@#", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("!@#", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_578() throws Exception {
        // Combination: key="0", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("0", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_579() throws Exception {
        // Combination: key="-1", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("-1", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_580() throws Exception {
        // Combination: key="1.5", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("1.5", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_581() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("9223372036854775807", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_582() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("9223372036854775808", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_583() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_584() throws Exception {
        // Combination: key="", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_585() throws Exception {
        // Combination: key=" ", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong(" ", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_586() throws Exception {
        // Combination: key="a", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("a", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_587() throws Exception {
        // Combination: key="test123", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("test123", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_588() throws Exception {
        // Combination: key="!@#", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("!@#", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_589() throws Exception {
        // Combination: key="0", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("0", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_590() throws Exception {
        // Combination: key="-1", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("-1", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_591() throws Exception {
        // Combination: key="1.5", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("1.5", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_592() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("9223372036854775807", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_593() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("9223372036854775808", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_594() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_595() throws Exception {
        // Combination: key="", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_596() throws Exception {
        // Combination: key=" ", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong(" ", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_597() throws Exception {
        // Combination: key="a", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("a", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_598() throws Exception {
        // Combination: key="test123", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("test123", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_599() throws Exception {
        // Combination: key="!@#", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("!@#", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_600() throws Exception {
        // Combination: key="0", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("0", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_601() throws Exception {
        // Combination: key="-1", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("-1", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_602() throws Exception {
        // Combination: key="1.5", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("1.5", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_603() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("9223372036854775807", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_604() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("9223372036854775808", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_605() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_606() throws Exception {
        // Combination: key="", defaultValue=Long.MIN_VALUE
        Object actual = (new ExtendedProperties()).getLong("", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_607() throws Exception {
        // Combination: key=" ", defaultValue=Long.MIN_VALUE
        Object actual = (new ExtendedProperties()).getLong(" ", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_608() throws Exception {
        // Combination: key="a", defaultValue=Long.MIN_VALUE
        Object actual = (new ExtendedProperties()).getLong("a", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_609() throws Exception {
        // Combination: key="test123", defaultValue=Long.MIN_VALUE
        Object actual = (new ExtendedProperties()).getLong("test123", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_610() throws Exception {
        // Combination: key="!@#", defaultValue=Long.MIN_VALUE
        Object actual = (new ExtendedProperties()).getLong("!@#", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_611() throws Exception {
        // Combination: key="0", defaultValue=Long.MIN_VALUE
        Object actual = (new ExtendedProperties()).getLong("0", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_612() throws Exception {
        // Combination: key="-1", defaultValue=Long.MIN_VALUE
        Object actual = (new ExtendedProperties()).getLong("-1", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_613() throws Exception {
        // Combination: key="1.5", defaultValue=Long.MIN_VALUE
        Object actual = (new ExtendedProperties()).getLong("1.5", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_614() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Long.MIN_VALUE
        Object actual = (new ExtendedProperties()).getLong("9223372036854775807", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_615() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Long.MIN_VALUE
        Object actual = (new ExtendedProperties()).getLong("9223372036854775808", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_616() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Long.MIN_VALUE
        Object actual = (new ExtendedProperties()).getLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_617() throws Exception {
        // Combination: key="", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_618() throws Exception {
        // Combination: key=" ", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong(" ", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_619() throws Exception {
        // Combination: key="a", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("a", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_620() throws Exception {
        // Combination: key="test123", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("test123", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_621() throws Exception {
        // Combination: key="!@#", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("!@#", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_622() throws Exception {
        // Combination: key="0", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("0", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_623() throws Exception {
        // Combination: key="-1", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("-1", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_624() throws Exception {
        // Combination: key="1.5", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("1.5", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_625() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("9223372036854775807", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_626() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("9223372036854775808", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_627() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0L
        Object actual = (new ExtendedProperties()).getLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_628() throws Exception {
        // Combination: key="", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_629() throws Exception {
        // Combination: key=" ", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong(" ", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_630() throws Exception {
        // Combination: key="a", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("a", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_631() throws Exception {
        // Combination: key="test123", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("test123", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_632() throws Exception {
        // Combination: key="!@#", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("!@#", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_633() throws Exception {
        // Combination: key="0", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("0", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_634() throws Exception {
        // Combination: key="-1", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("-1", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_635() throws Exception {
        // Combination: key="1.5", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("1.5", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_636() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("9223372036854775807", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_637() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("9223372036854775808", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_638() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1L
        Object actual = (new ExtendedProperties()).getLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_639() throws Exception {
        // Combination: key="", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_640() throws Exception {
        // Combination: key=" ", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong(" ", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_641() throws Exception {
        // Combination: key="a", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("a", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_642() throws Exception {
        // Combination: key="test123", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("test123", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_643() throws Exception {
        // Combination: key="!@#", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("!@#", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_644() throws Exception {
        // Combination: key="0", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("0", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_645() throws Exception {
        // Combination: key="-1", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("-1", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_646() throws Exception {
        // Combination: key="1.5", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("1.5", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_647() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("9223372036854775807", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_648() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("9223372036854775808", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_649() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1L
        Object actual = (new ExtendedProperties()).getLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_650() throws Exception {
        // Combination: key="", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_651() throws Exception {
        // Combination: key=" ", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong(" ", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_652() throws Exception {
        // Combination: key="a", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("a", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_653() throws Exception {
        // Combination: key="test123", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("test123", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_654() throws Exception {
        // Combination: key="!@#", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("!@#", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_655() throws Exception {
        // Combination: key="0", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("0", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_656() throws Exception {
        // Combination: key="-1", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("-1", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_657() throws Exception {
        // Combination: key="1.5", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("1.5", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_658() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("9223372036854775807", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_659() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("9223372036854775808", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getLong_pairwise_660() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Long.MAX_VALUE
        Object actual = (new ExtendedProperties()).getLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_661() throws Exception {
        // Combination: key="", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_662() throws Exception {
        // Combination: key=" ", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat(" ", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_663() throws Exception {
        // Combination: key="a", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("a", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_664() throws Exception {
        // Combination: key="test123", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("test123", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_665() throws Exception {
        // Combination: key="!@#", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("!@#", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_666() throws Exception {
        // Combination: key="0", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("0", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_667() throws Exception {
        // Combination: key="-1", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("-1", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_668() throws Exception {
        // Combination: key="1.5", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("1.5", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_669() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775807", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_670() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775808", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_671() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_672() throws Exception {
        // Combination: key="", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_673() throws Exception {
        // Combination: key=" ", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat(" ", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_674() throws Exception {
        // Combination: key="a", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("a", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_675() throws Exception {
        // Combination: key="test123", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("test123", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_676() throws Exception {
        // Combination: key="!@#", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("!@#", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_677() throws Exception {
        // Combination: key="0", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("0", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_678() throws Exception {
        // Combination: key="-1", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("-1", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_679() throws Exception {
        // Combination: key="1.5", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("1.5", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_680() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775807", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_681() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775808", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_682() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_683() throws Exception {
        // Combination: key="", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_684() throws Exception {
        // Combination: key=" ", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat(" ", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_685() throws Exception {
        // Combination: key="a", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("a", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_686() throws Exception {
        // Combination: key="test123", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("test123", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_687() throws Exception {
        // Combination: key="!@#", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("!@#", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_688() throws Exception {
        // Combination: key="0", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("0", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_689() throws Exception {
        // Combination: key="-1", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("-1", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_690() throws Exception {
        // Combination: key="1.5", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("1.5", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_691() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775807", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_692() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775808", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_693() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_694() throws Exception {
        // Combination: key="", defaultValue=Float.NaN
        Object actual = (new ExtendedProperties()).getFloat("", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_695() throws Exception {
        // Combination: key=" ", defaultValue=Float.NaN
        Object actual = (new ExtendedProperties()).getFloat(" ", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_696() throws Exception {
        // Combination: key="a", defaultValue=Float.NaN
        Object actual = (new ExtendedProperties()).getFloat("a", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_697() throws Exception {
        // Combination: key="test123", defaultValue=Float.NaN
        Object actual = (new ExtendedProperties()).getFloat("test123", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_698() throws Exception {
        // Combination: key="!@#", defaultValue=Float.NaN
        Object actual = (new ExtendedProperties()).getFloat("!@#", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_699() throws Exception {
        // Combination: key="0", defaultValue=Float.NaN
        Object actual = (new ExtendedProperties()).getFloat("0", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_700() throws Exception {
        // Combination: key="-1", defaultValue=Float.NaN
        Object actual = (new ExtendedProperties()).getFloat("-1", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_701() throws Exception {
        // Combination: key="1.5", defaultValue=Float.NaN
        Object actual = (new ExtendedProperties()).getFloat("1.5", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_702() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Float.NaN
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775807", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_703() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Float.NaN
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775808", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_704() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Float.NaN
        Object actual = (new ExtendedProperties()).getFloat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Float.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_705() throws Exception {
        // Combination: key="", defaultValue=Float.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getFloat("", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_706() throws Exception {
        // Combination: key=" ", defaultValue=Float.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getFloat(" ", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_707() throws Exception {
        // Combination: key="a", defaultValue=Float.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getFloat("a", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_708() throws Exception {
        // Combination: key="test123", defaultValue=Float.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getFloat("test123", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_709() throws Exception {
        // Combination: key="!@#", defaultValue=Float.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getFloat("!@#", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_710() throws Exception {
        // Combination: key="0", defaultValue=Float.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getFloat("0", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_711() throws Exception {
        // Combination: key="-1", defaultValue=Float.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getFloat("-1", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_712() throws Exception {
        // Combination: key="1.5", defaultValue=Float.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getFloat("1.5", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_713() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Float.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775807", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_714() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Float.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775808", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_715() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Float.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getFloat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Float.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_716() throws Exception {
        // Combination: key="", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_717() throws Exception {
        // Combination: key=" ", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat(" ", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_718() throws Exception {
        // Combination: key="a", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("a", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_719() throws Exception {
        // Combination: key="test123", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("test123", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_720() throws Exception {
        // Combination: key="!@#", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("!@#", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_721() throws Exception {
        // Combination: key="0", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("0", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_722() throws Exception {
        // Combination: key="-1", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("-1", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_723() throws Exception {
        // Combination: key="1.5", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("1.5", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_724() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775807", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_725() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775808", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_726() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0.0f
        Object actual = (new ExtendedProperties()).getFloat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_727() throws Exception {
        // Combination: key="", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_728() throws Exception {
        // Combination: key=" ", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat(" ", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_729() throws Exception {
        // Combination: key="a", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("a", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_730() throws Exception {
        // Combination: key="test123", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("test123", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_731() throws Exception {
        // Combination: key="!@#", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("!@#", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_732() throws Exception {
        // Combination: key="0", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("0", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_733() throws Exception {
        // Combination: key="-1", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("-1", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_734() throws Exception {
        // Combination: key="1.5", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("1.5", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_735() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775807", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_736() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775808", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_737() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1.0f
        Object actual = (new ExtendedProperties()).getFloat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_738() throws Exception {
        // Combination: key="", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_739() throws Exception {
        // Combination: key=" ", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat(" ", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_740() throws Exception {
        // Combination: key="a", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("a", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_741() throws Exception {
        // Combination: key="test123", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("test123", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_742() throws Exception {
        // Combination: key="!@#", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("!@#", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_743() throws Exception {
        // Combination: key="0", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("0", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_744() throws Exception {
        // Combination: key="-1", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("-1", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_745() throws Exception {
        // Combination: key="1.5", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("1.5", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_746() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775807", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_747() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775808", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_748() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1.0f
        Object actual = (new ExtendedProperties()).getFloat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1.0f);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_749() throws Exception {
        // Combination: key="", defaultValue=Float.MAX_VALUE
        Object actual = (new ExtendedProperties()).getFloat("", Float.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("3.4028235E38", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_750() throws Exception {
        // Combination: key=" ", defaultValue=Float.MAX_VALUE
        Object actual = (new ExtendedProperties()).getFloat(" ", Float.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("3.4028235E38", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_751() throws Exception {
        // Combination: key="a", defaultValue=Float.MAX_VALUE
        Object actual = (new ExtendedProperties()).getFloat("a", Float.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("3.4028235E38", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_752() throws Exception {
        // Combination: key="test123", defaultValue=Float.MAX_VALUE
        Object actual = (new ExtendedProperties()).getFloat("test123", Float.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("3.4028235E38", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_753() throws Exception {
        // Combination: key="!@#", defaultValue=Float.MAX_VALUE
        Object actual = (new ExtendedProperties()).getFloat("!@#", Float.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("3.4028235E38", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_754() throws Exception {
        // Combination: key="0", defaultValue=Float.MAX_VALUE
        Object actual = (new ExtendedProperties()).getFloat("0", Float.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("3.4028235E38", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_755() throws Exception {
        // Combination: key="-1", defaultValue=Float.MAX_VALUE
        Object actual = (new ExtendedProperties()).getFloat("-1", Float.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("3.4028235E38", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_756() throws Exception {
        // Combination: key="1.5", defaultValue=Float.MAX_VALUE
        Object actual = (new ExtendedProperties()).getFloat("1.5", Float.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("3.4028235E38", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_757() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Float.MAX_VALUE
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775807", Float.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("3.4028235E38", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_758() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Float.MAX_VALUE
        Object actual = (new ExtendedProperties()).getFloat("9223372036854775808", Float.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("3.4028235E38", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getFloat_pairwise_759() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Float.MAX_VALUE
        Object actual = (new ExtendedProperties()).getFloat("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Float.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("3.4028235E38", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_760() throws Exception {
        // Combination: key="", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_761() throws Exception {
        // Combination: key=" ", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble(" ", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_762() throws Exception {
        // Combination: key="a", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("a", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_763() throws Exception {
        // Combination: key="test123", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("test123", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_764() throws Exception {
        // Combination: key="!@#", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("!@#", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_765() throws Exception {
        // Combination: key="0", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("0", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_766() throws Exception {
        // Combination: key="-1", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("-1", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_767() throws Exception {
        // Combination: key="1.5", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("1.5", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_768() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775807", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_769() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775808", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_770() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_771() throws Exception {
        // Combination: key="", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_772() throws Exception {
        // Combination: key=" ", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble(" ", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_773() throws Exception {
        // Combination: key="a", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("a", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_774() throws Exception {
        // Combination: key="test123", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("test123", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_775() throws Exception {
        // Combination: key="!@#", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("!@#", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_776() throws Exception {
        // Combination: key="0", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("0", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_777() throws Exception {
        // Combination: key="-1", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("-1", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_778() throws Exception {
        // Combination: key="1.5", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("1.5", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_779() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775807", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_780() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775808", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_781() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_782() throws Exception {
        // Combination: key="", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_783() throws Exception {
        // Combination: key=" ", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble(" ", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_784() throws Exception {
        // Combination: key="a", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("a", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_785() throws Exception {
        // Combination: key="test123", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("test123", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_786() throws Exception {
        // Combination: key="!@#", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("!@#", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_787() throws Exception {
        // Combination: key="0", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("0", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_788() throws Exception {
        // Combination: key="-1", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("-1", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_789() throws Exception {
        // Combination: key="1.5", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("1.5", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_790() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775807", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_791() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775808", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_792() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_793() throws Exception {
        // Combination: key="", defaultValue=Double.NaN
        Object actual = (new ExtendedProperties()).getDouble("", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_794() throws Exception {
        // Combination: key=" ", defaultValue=Double.NaN
        Object actual = (new ExtendedProperties()).getDouble(" ", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_795() throws Exception {
        // Combination: key="a", defaultValue=Double.NaN
        Object actual = (new ExtendedProperties()).getDouble("a", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_796() throws Exception {
        // Combination: key="test123", defaultValue=Double.NaN
        Object actual = (new ExtendedProperties()).getDouble("test123", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_797() throws Exception {
        // Combination: key="!@#", defaultValue=Double.NaN
        Object actual = (new ExtendedProperties()).getDouble("!@#", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_798() throws Exception {
        // Combination: key="0", defaultValue=Double.NaN
        Object actual = (new ExtendedProperties()).getDouble("0", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_799() throws Exception {
        // Combination: key="-1", defaultValue=Double.NaN
        Object actual = (new ExtendedProperties()).getDouble("-1", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_800() throws Exception {
        // Combination: key="1.5", defaultValue=Double.NaN
        Object actual = (new ExtendedProperties()).getDouble("1.5", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_801() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Double.NaN
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775807", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_802() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Double.NaN
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775808", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_803() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Double.NaN
        Object actual = (new ExtendedProperties()).getDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_804() throws Exception {
        // Combination: key="", defaultValue=Double.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getDouble("", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_805() throws Exception {
        // Combination: key=" ", defaultValue=Double.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getDouble(" ", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_806() throws Exception {
        // Combination: key="a", defaultValue=Double.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getDouble("a", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_807() throws Exception {
        // Combination: key="test123", defaultValue=Double.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getDouble("test123", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_808() throws Exception {
        // Combination: key="!@#", defaultValue=Double.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getDouble("!@#", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_809() throws Exception {
        // Combination: key="0", defaultValue=Double.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getDouble("0", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_810() throws Exception {
        // Combination: key="-1", defaultValue=Double.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getDouble("-1", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_811() throws Exception {
        // Combination: key="1.5", defaultValue=Double.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getDouble("1.5", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_812() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Double.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775807", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_813() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Double.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775808", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_814() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Double.POSITIVE_INFINITY
        Object actual = (new ExtendedProperties()).getDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_815() throws Exception {
        // Combination: key="", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_816() throws Exception {
        // Combination: key=" ", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble(" ", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_817() throws Exception {
        // Combination: key="a", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("a", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_818() throws Exception {
        // Combination: key="test123", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("test123", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_819() throws Exception {
        // Combination: key="!@#", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("!@#", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_820() throws Exception {
        // Combination: key="0", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("0", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_821() throws Exception {
        // Combination: key="-1", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("-1", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_822() throws Exception {
        // Combination: key="1.5", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("1.5", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_823() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775807", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_824() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775808", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_825() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0.0d
        Object actual = (new ExtendedProperties()).getDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_826() throws Exception {
        // Combination: key="", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_827() throws Exception {
        // Combination: key=" ", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble(" ", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_828() throws Exception {
        // Combination: key="a", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("a", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_829() throws Exception {
        // Combination: key="test123", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("test123", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_830() throws Exception {
        // Combination: key="!@#", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("!@#", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_831() throws Exception {
        // Combination: key="0", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("0", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_832() throws Exception {
        // Combination: key="-1", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("-1", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_833() throws Exception {
        // Combination: key="1.5", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("1.5", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_834() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775807", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_835() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775808", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_836() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1.0d
        Object actual = (new ExtendedProperties()).getDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_837() throws Exception {
        // Combination: key="", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_838() throws Exception {
        // Combination: key=" ", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble(" ", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_839() throws Exception {
        // Combination: key="a", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("a", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_840() throws Exception {
        // Combination: key="test123", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("test123", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_841() throws Exception {
        // Combination: key="!@#", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("!@#", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_842() throws Exception {
        // Combination: key="0", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("0", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_843() throws Exception {
        // Combination: key="-1", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("-1", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_844() throws Exception {
        // Combination: key="1.5", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("1.5", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_845() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775807", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_846() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775808", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_847() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1.0d
        Object actual = (new ExtendedProperties()).getDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_848() throws Exception {
        // Combination: key="", defaultValue=Double.MAX_VALUE
        Object actual = (new ExtendedProperties()).getDouble("", Double.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_849() throws Exception {
        // Combination: key=" ", defaultValue=Double.MAX_VALUE
        Object actual = (new ExtendedProperties()).getDouble(" ", Double.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_850() throws Exception {
        // Combination: key="a", defaultValue=Double.MAX_VALUE
        Object actual = (new ExtendedProperties()).getDouble("a", Double.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_851() throws Exception {
        // Combination: key="test123", defaultValue=Double.MAX_VALUE
        Object actual = (new ExtendedProperties()).getDouble("test123", Double.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_852() throws Exception {
        // Combination: key="!@#", defaultValue=Double.MAX_VALUE
        Object actual = (new ExtendedProperties()).getDouble("!@#", Double.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_853() throws Exception {
        // Combination: key="0", defaultValue=Double.MAX_VALUE
        Object actual = (new ExtendedProperties()).getDouble("0", Double.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_854() throws Exception {
        // Combination: key="-1", defaultValue=Double.MAX_VALUE
        Object actual = (new ExtendedProperties()).getDouble("-1", Double.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_855() throws Exception {
        // Combination: key="1.5", defaultValue=Double.MAX_VALUE
        Object actual = (new ExtendedProperties()).getDouble("1.5", Double.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_856() throws Exception {
        // Combination: key="9223372036854775807", defaultValue=Double.MAX_VALUE
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775807", Double.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_857() throws Exception {
        // Combination: key="9223372036854775808", defaultValue=Double.MAX_VALUE
        Object actual = (new ExtendedProperties()).getDouble("9223372036854775808", Double.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_getDouble_pairwise_858() throws Exception {
        // Combination: key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Double.MAX_VALUE
        Object actual = (new ExtendedProperties()).getDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Double.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.7976931348623157E308", formatValue(actual));
    }

}
