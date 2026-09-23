package com.google.javascript.rhino;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for JSDocInfoBuilder.
 */
public class JSDocInfoBuilder_IPOTest {
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
    public void test_recordBlockDescription_pairwise_001() throws Exception {
        // Combination: receiver__parseDocumentation=true, description=""
        Object actual = (new JSDocInfoBuilder(true)).recordBlockDescription("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_002() throws Exception {
        // Combination: receiver__parseDocumentation=false, description=""
        Object actual = (new JSDocInfoBuilder(false)).recordBlockDescription("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_003() throws Exception {
        // Combination: receiver__parseDocumentation=true, description=" "
        Object actual = (new JSDocInfoBuilder(true)).recordBlockDescription(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_004() throws Exception {
        // Combination: receiver__parseDocumentation=false, description=" "
        Object actual = (new JSDocInfoBuilder(false)).recordBlockDescription(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_005() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="a"
        Object actual = (new JSDocInfoBuilder(true)).recordBlockDescription("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_006() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="a"
        Object actual = (new JSDocInfoBuilder(false)).recordBlockDescription("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_007() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordBlockDescription("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_008() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="test123"
        Object actual = (new JSDocInfoBuilder(false)).recordBlockDescription("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_009() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordBlockDescription("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_010() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="!@#"
        Object actual = (new JSDocInfoBuilder(false)).recordBlockDescription("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_011() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="0"
        Object actual = (new JSDocInfoBuilder(true)).recordBlockDescription("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_012() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="0"
        Object actual = (new JSDocInfoBuilder(false)).recordBlockDescription("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_013() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordBlockDescription("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_014() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="-1"
        Object actual = (new JSDocInfoBuilder(false)).recordBlockDescription("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_015() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordBlockDescription("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_016() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="1.5"
        Object actual = (new JSDocInfoBuilder(false)).recordBlockDescription("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_017() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordBlockDescription("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_018() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(false)).recordBlockDescription("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_019() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordBlockDescription("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_020() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(false)).recordBlockDescription("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_021() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordBlockDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordBlockDescription_pairwise_022() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(false)).recordBlockDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_023() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="", description=""
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_024() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName=" ", description=""
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription(" ", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_025() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="a", description=""
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("a", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_026() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="test123", description=""
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("test123", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_027() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="!@#", description=""
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("!@#", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_028() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="0", description=""
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("0", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_029() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="-1", description=""
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("-1", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_030() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="1.5", description=""
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("1.5", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_031() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775807", description=""
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775807", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_032() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775808", description=""
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775808", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_033() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", description=""
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_034() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName="", description=" "
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription("", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_035() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName=" ", description=" "
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription(" ", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_036() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName="a", description=" "
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription("a", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_037() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName="test123", description=" "
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription("test123", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_038() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName="!@#", description=" "
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription("!@#", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_039() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName="0", description=" "
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription("0", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_040() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName="-1", description=" "
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription("-1", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_041() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName="1.5", description=" "
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription("1.5", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_042() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName="9223372036854775807", description=" "
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription("9223372036854775807", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_043() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName="9223372036854775808", description=" "
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription("9223372036854775808", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_044() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", description=" "
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_045() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="", description="a"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_046() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName=" ", description="a"
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription(" ", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_047() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="a", description="a"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("a", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_048() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="test123", description="a"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("test123", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_049() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="!@#", description="a"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("!@#", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_050() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="0", description="a"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("0", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_051() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="-1", description="a"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("-1", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_052() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="1.5", description="a"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("1.5", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_053() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775807", description="a"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775807", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_054() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775808", description="a"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775808", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_055() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", description="a"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_056() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="", description="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_057() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName=" ", description="test123"
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription(" ", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_058() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="a", description="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("a", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_059() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="test123", description="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("test123", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_060() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="!@#", description="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("!@#", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_061() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="0", description="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("0", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_062() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="-1", description="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("-1", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_063() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="1.5", description="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("1.5", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_064() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775807", description="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775807", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_065() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775808", description="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775808", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_066() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", description="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_067() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="", description="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_068() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName=" ", description="!@#"
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription(" ", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_069() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="a", description="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("a", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_070() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="test123", description="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("test123", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_071() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="!@#", description="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("!@#", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_072() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="0", description="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("0", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_073() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="-1", description="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("-1", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_074() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="1.5", description="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("1.5", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_075() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775807", description="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775807", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_076() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775808", description="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775808", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_077() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", description="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_078() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="", description="0"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_079() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName=" ", description="0"
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription(" ", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_080() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="a", description="0"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("a", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_081() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="test123", description="0"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("test123", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_082() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="!@#", description="0"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("!@#", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_083() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="0", description="0"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("0", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_084() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="-1", description="0"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("-1", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_085() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="1.5", description="0"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("1.5", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_086() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775807", description="0"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775807", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_087() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775808", description="0"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775808", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_088() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", description="0"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_089() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="", description="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_090() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName=" ", description="-1"
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription(" ", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_091() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="a", description="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("a", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_092() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="test123", description="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("test123", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_093() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="!@#", description="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("!@#", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_094() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="0", description="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("0", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_095() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="-1", description="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("-1", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_096() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="1.5", description="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("1.5", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_097() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775807", description="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775807", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_098() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775808", description="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775808", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_099() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", description="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_100() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="", description="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_101() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName=" ", description="1.5"
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription(" ", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_102() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="a", description="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("a", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_103() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="test123", description="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("test123", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_104() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="!@#", description="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("!@#", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_105() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="0", description="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("0", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_106() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="-1", description="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("-1", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_107() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="1.5", description="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("1.5", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_108() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775807", description="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775807", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_109() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775808", description="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775808", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_110() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", description="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_111() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="", description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_112() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName=" ", description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription(" ", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_113() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="a", description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("a", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_114() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="test123", description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("test123", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_115() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="!@#", description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("!@#", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_116() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="0", description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("0", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_117() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="-1", description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("-1", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_118() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="1.5", description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("1.5", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_119() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775807", description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775807", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_120() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775808", description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775808", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_121() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_122() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="", description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_123() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName=" ", description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription(" ", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_124() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="a", description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("a", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_125() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="test123", description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("test123", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_126() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="!@#", description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("!@#", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_127() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="0", description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("0", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_128() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="-1", description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("-1", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_129() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="1.5", description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("1.5", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_130() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775807", description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775807", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_131() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775808", description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775808", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_132() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_133() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="", description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_134() throws Exception {
        // Combination: receiver__parseDocumentation=false, parameterName=" ", description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(false)).recordParameterDescription(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_135() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="a", description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_136() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="test123", description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_137() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="!@#", description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_138() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="0", description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_139() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="-1", description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_140() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="1.5", description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_141() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775807", description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_142() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="9223372036854775808", description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordParameterDescription_pairwise_143() throws Exception {
        // Combination: receiver__parseDocumentation=true, parameterName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordParameterDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_144() throws Exception {
        // Combination: receiver__parseDocumentation=true, name=""
        Object actual = (new JSDocInfoBuilder(true)).recordTemplateTypeName("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_145() throws Exception {
        // Combination: receiver__parseDocumentation=false, name=""
        Object actual = (new JSDocInfoBuilder(false)).recordTemplateTypeName("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_146() throws Exception {
        // Combination: receiver__parseDocumentation=true, name=" "
        Object actual = (new JSDocInfoBuilder(true)).recordTemplateTypeName(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_147() throws Exception {
        // Combination: receiver__parseDocumentation=false, name=" "
        Object actual = (new JSDocInfoBuilder(false)).recordTemplateTypeName(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_148() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="a"
        Object actual = (new JSDocInfoBuilder(true)).recordTemplateTypeName("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_149() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="a"
        Object actual = (new JSDocInfoBuilder(false)).recordTemplateTypeName("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_150() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordTemplateTypeName("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_151() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="test123"
        Object actual = (new JSDocInfoBuilder(false)).recordTemplateTypeName("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_152() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordTemplateTypeName("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_153() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="!@#"
        Object actual = (new JSDocInfoBuilder(false)).recordTemplateTypeName("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_154() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="0"
        Object actual = (new JSDocInfoBuilder(true)).recordTemplateTypeName("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_155() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="0"
        Object actual = (new JSDocInfoBuilder(false)).recordTemplateTypeName("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_156() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordTemplateTypeName("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_157() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="-1"
        Object actual = (new JSDocInfoBuilder(false)).recordTemplateTypeName("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_158() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordTemplateTypeName("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_159() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="1.5"
        Object actual = (new JSDocInfoBuilder(false)).recordTemplateTypeName("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_160() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordTemplateTypeName("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_161() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(false)).recordTemplateTypeName("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_162() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordTemplateTypeName("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_163() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(false)).recordTemplateTypeName("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_164() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordTemplateTypeName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordTemplateTypeName_pairwise_165() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(false)).recordTemplateTypeName("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_166() throws Exception {
        // Combination: receiver__parseDocumentation=true, author=""
        Object actual = (new JSDocInfoBuilder(true)).addAuthor("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_167() throws Exception {
        // Combination: receiver__parseDocumentation=false, author=""
        Object actual = (new JSDocInfoBuilder(false)).addAuthor("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_168() throws Exception {
        // Combination: receiver__parseDocumentation=true, author=" "
        Object actual = (new JSDocInfoBuilder(true)).addAuthor(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_169() throws Exception {
        // Combination: receiver__parseDocumentation=false, author=" "
        Object actual = (new JSDocInfoBuilder(false)).addAuthor(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_170() throws Exception {
        // Combination: receiver__parseDocumentation=true, author="a"
        Object actual = (new JSDocInfoBuilder(true)).addAuthor("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_171() throws Exception {
        // Combination: receiver__parseDocumentation=false, author="a"
        Object actual = (new JSDocInfoBuilder(false)).addAuthor("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_172() throws Exception {
        // Combination: receiver__parseDocumentation=true, author="test123"
        Object actual = (new JSDocInfoBuilder(true)).addAuthor("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_173() throws Exception {
        // Combination: receiver__parseDocumentation=false, author="test123"
        Object actual = (new JSDocInfoBuilder(false)).addAuthor("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_174() throws Exception {
        // Combination: receiver__parseDocumentation=true, author="!@#"
        Object actual = (new JSDocInfoBuilder(true)).addAuthor("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_175() throws Exception {
        // Combination: receiver__parseDocumentation=false, author="!@#"
        Object actual = (new JSDocInfoBuilder(false)).addAuthor("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_176() throws Exception {
        // Combination: receiver__parseDocumentation=true, author="0"
        Object actual = (new JSDocInfoBuilder(true)).addAuthor("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_177() throws Exception {
        // Combination: receiver__parseDocumentation=false, author="0"
        Object actual = (new JSDocInfoBuilder(false)).addAuthor("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_178() throws Exception {
        // Combination: receiver__parseDocumentation=true, author="-1"
        Object actual = (new JSDocInfoBuilder(true)).addAuthor("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_179() throws Exception {
        // Combination: receiver__parseDocumentation=false, author="-1"
        Object actual = (new JSDocInfoBuilder(false)).addAuthor("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_180() throws Exception {
        // Combination: receiver__parseDocumentation=true, author="1.5"
        Object actual = (new JSDocInfoBuilder(true)).addAuthor("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_181() throws Exception {
        // Combination: receiver__parseDocumentation=false, author="1.5"
        Object actual = (new JSDocInfoBuilder(false)).addAuthor("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_182() throws Exception {
        // Combination: receiver__parseDocumentation=true, author="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).addAuthor("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_183() throws Exception {
        // Combination: receiver__parseDocumentation=false, author="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(false)).addAuthor("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_184() throws Exception {
        // Combination: receiver__parseDocumentation=true, author="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).addAuthor("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_185() throws Exception {
        // Combination: receiver__parseDocumentation=false, author="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(false)).addAuthor("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_186() throws Exception {
        // Combination: receiver__parseDocumentation=true, author="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).addAuthor("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addAuthor_pairwise_187() throws Exception {
        // Combination: receiver__parseDocumentation=false, author="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(false)).addAuthor("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_188() throws Exception {
        // Combination: receiver__parseDocumentation=true, reference=""
        Object actual = (new JSDocInfoBuilder(true)).addReference("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_189() throws Exception {
        // Combination: receiver__parseDocumentation=true, reference=" "
        Object actual = (new JSDocInfoBuilder(true)).addReference(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_190() throws Exception {
        // Combination: receiver__parseDocumentation=true, reference="a"
        Object actual = (new JSDocInfoBuilder(true)).addReference("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_191() throws Exception {
        // Combination: receiver__parseDocumentation=true, reference="test123"
        Object actual = (new JSDocInfoBuilder(true)).addReference("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_192() throws Exception {
        // Combination: receiver__parseDocumentation=true, reference="!@#"
        Object actual = (new JSDocInfoBuilder(true)).addReference("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_193() throws Exception {
        // Combination: receiver__parseDocumentation=true, reference="0"
        Object actual = (new JSDocInfoBuilder(true)).addReference("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_194() throws Exception {
        // Combination: receiver__parseDocumentation=true, reference="-1"
        Object actual = (new JSDocInfoBuilder(true)).addReference("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_195() throws Exception {
        // Combination: receiver__parseDocumentation=true, reference="1.5"
        Object actual = (new JSDocInfoBuilder(true)).addReference("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_196() throws Exception {
        // Combination: receiver__parseDocumentation=true, reference="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).addReference("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_197() throws Exception {
        // Combination: receiver__parseDocumentation=true, reference="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).addReference("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_198() throws Exception {
        // Combination: receiver__parseDocumentation=true, reference="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).addReference("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_199() throws Exception {
        // Combination: receiver__parseDocumentation=false, reference=""
        Object actual = (new JSDocInfoBuilder(false)).addReference("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_200() throws Exception {
        // Combination: receiver__parseDocumentation=false, reference=" "
        Object actual = (new JSDocInfoBuilder(false)).addReference(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_201() throws Exception {
        // Combination: receiver__parseDocumentation=false, reference="a"
        Object actual = (new JSDocInfoBuilder(false)).addReference("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_202() throws Exception {
        // Combination: receiver__parseDocumentation=false, reference="test123"
        Object actual = (new JSDocInfoBuilder(false)).addReference("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_203() throws Exception {
        // Combination: receiver__parseDocumentation=false, reference="!@#"
        Object actual = (new JSDocInfoBuilder(false)).addReference("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_204() throws Exception {
        // Combination: receiver__parseDocumentation=false, reference="0"
        Object actual = (new JSDocInfoBuilder(false)).addReference("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_205() throws Exception {
        // Combination: receiver__parseDocumentation=false, reference="-1"
        Object actual = (new JSDocInfoBuilder(false)).addReference("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_206() throws Exception {
        // Combination: receiver__parseDocumentation=false, reference="1.5"
        Object actual = (new JSDocInfoBuilder(false)).addReference("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_207() throws Exception {
        // Combination: receiver__parseDocumentation=false, reference="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(false)).addReference("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_208() throws Exception {
        // Combination: receiver__parseDocumentation=false, reference="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(false)).addReference("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_addReference_pairwise_209() throws Exception {
        // Combination: receiver__parseDocumentation=false, reference="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(false)).addReference("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_210() throws Exception {
        // Combination: receiver__parseDocumentation=true, version=""
        Object actual = (new JSDocInfoBuilder(true)).recordVersion("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_211() throws Exception {
        // Combination: receiver__parseDocumentation=true, version=" "
        Object actual = (new JSDocInfoBuilder(true)).recordVersion(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_212() throws Exception {
        // Combination: receiver__parseDocumentation=true, version="a"
        Object actual = (new JSDocInfoBuilder(true)).recordVersion("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_213() throws Exception {
        // Combination: receiver__parseDocumentation=true, version="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordVersion("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_214() throws Exception {
        // Combination: receiver__parseDocumentation=true, version="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordVersion("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_215() throws Exception {
        // Combination: receiver__parseDocumentation=true, version="0"
        Object actual = (new JSDocInfoBuilder(true)).recordVersion("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_216() throws Exception {
        // Combination: receiver__parseDocumentation=true, version="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordVersion("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_217() throws Exception {
        // Combination: receiver__parseDocumentation=true, version="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordVersion("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_218() throws Exception {
        // Combination: receiver__parseDocumentation=true, version="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordVersion("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_219() throws Exception {
        // Combination: receiver__parseDocumentation=true, version="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordVersion("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_220() throws Exception {
        // Combination: receiver__parseDocumentation=true, version="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordVersion("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_221() throws Exception {
        // Combination: receiver__parseDocumentation=false, version=""
        Object actual = (new JSDocInfoBuilder(false)).recordVersion("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_222() throws Exception {
        // Combination: receiver__parseDocumentation=false, version=" "
        Object actual = (new JSDocInfoBuilder(false)).recordVersion(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_223() throws Exception {
        // Combination: receiver__parseDocumentation=false, version="a"
        Object actual = (new JSDocInfoBuilder(false)).recordVersion("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_224() throws Exception {
        // Combination: receiver__parseDocumentation=false, version="test123"
        Object actual = (new JSDocInfoBuilder(false)).recordVersion("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_225() throws Exception {
        // Combination: receiver__parseDocumentation=false, version="!@#"
        Object actual = (new JSDocInfoBuilder(false)).recordVersion("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_226() throws Exception {
        // Combination: receiver__parseDocumentation=false, version="0"
        Object actual = (new JSDocInfoBuilder(false)).recordVersion("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_227() throws Exception {
        // Combination: receiver__parseDocumentation=false, version="-1"
        Object actual = (new JSDocInfoBuilder(false)).recordVersion("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_228() throws Exception {
        // Combination: receiver__parseDocumentation=false, version="1.5"
        Object actual = (new JSDocInfoBuilder(false)).recordVersion("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_229() throws Exception {
        // Combination: receiver__parseDocumentation=false, version="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(false)).recordVersion("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_230() throws Exception {
        // Combination: receiver__parseDocumentation=false, version="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(false)).recordVersion("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordVersion_pairwise_231() throws Exception {
        // Combination: receiver__parseDocumentation=false, version="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(false)).recordVersion("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_232() throws Exception {
        // Combination: receiver__parseDocumentation=true, reason=""
        Object actual = (new JSDocInfoBuilder(true)).recordDeprecationReason("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_233() throws Exception {
        // Combination: receiver__parseDocumentation=false, reason=""
        Object actual = (new JSDocInfoBuilder(false)).recordDeprecationReason("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_234() throws Exception {
        // Combination: receiver__parseDocumentation=true, reason=" "
        Object actual = (new JSDocInfoBuilder(true)).recordDeprecationReason(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_235() throws Exception {
        // Combination: receiver__parseDocumentation=false, reason=" "
        Object actual = (new JSDocInfoBuilder(false)).recordDeprecationReason(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_236() throws Exception {
        // Combination: receiver__parseDocumentation=true, reason="a"
        Object actual = (new JSDocInfoBuilder(true)).recordDeprecationReason("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_237() throws Exception {
        // Combination: receiver__parseDocumentation=false, reason="a"
        Object actual = (new JSDocInfoBuilder(false)).recordDeprecationReason("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_238() throws Exception {
        // Combination: receiver__parseDocumentation=true, reason="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordDeprecationReason("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_239() throws Exception {
        // Combination: receiver__parseDocumentation=false, reason="test123"
        Object actual = (new JSDocInfoBuilder(false)).recordDeprecationReason("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_240() throws Exception {
        // Combination: receiver__parseDocumentation=true, reason="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordDeprecationReason("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_241() throws Exception {
        // Combination: receiver__parseDocumentation=false, reason="!@#"
        Object actual = (new JSDocInfoBuilder(false)).recordDeprecationReason("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_242() throws Exception {
        // Combination: receiver__parseDocumentation=true, reason="0"
        Object actual = (new JSDocInfoBuilder(true)).recordDeprecationReason("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_243() throws Exception {
        // Combination: receiver__parseDocumentation=false, reason="0"
        Object actual = (new JSDocInfoBuilder(false)).recordDeprecationReason("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_244() throws Exception {
        // Combination: receiver__parseDocumentation=true, reason="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordDeprecationReason("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_245() throws Exception {
        // Combination: receiver__parseDocumentation=false, reason="-1"
        Object actual = (new JSDocInfoBuilder(false)).recordDeprecationReason("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_246() throws Exception {
        // Combination: receiver__parseDocumentation=true, reason="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordDeprecationReason("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_247() throws Exception {
        // Combination: receiver__parseDocumentation=false, reason="1.5"
        Object actual = (new JSDocInfoBuilder(false)).recordDeprecationReason("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_248() throws Exception {
        // Combination: receiver__parseDocumentation=true, reason="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordDeprecationReason("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_249() throws Exception {
        // Combination: receiver__parseDocumentation=false, reason="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(false)).recordDeprecationReason("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_250() throws Exception {
        // Combination: receiver__parseDocumentation=true, reason="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordDeprecationReason("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_251() throws Exception {
        // Combination: receiver__parseDocumentation=false, reason="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(false)).recordDeprecationReason("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_252() throws Exception {
        // Combination: receiver__parseDocumentation=true, reason="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordDeprecationReason("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDeprecationReason_pairwise_253() throws Exception {
        // Combination: receiver__parseDocumentation=false, reason="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(false)).recordDeprecationReason("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordSuppressions_pairwise_254() throws Exception {
        // Combination: receiver__parseDocumentation=true, suppressions=java.util.Collections.<String>emptySet()
        Object actual = (new JSDocInfoBuilder(true)).recordSuppressions(java.util.Collections.<String>emptySet());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordSuppressions_pairwise_255() throws Exception {
        // Combination: receiver__parseDocumentation=true, suppressions=java.util.Collections.singleton("a")
        Object actual = (new JSDocInfoBuilder(true)).recordSuppressions(java.util.Collections.singleton("a"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordSuppressions_pairwise_256() throws Exception {
        // Combination: receiver__parseDocumentation=false, suppressions=java.util.Collections.<String>emptySet()
        Object actual = (new JSDocInfoBuilder(false)).recordSuppressions(java.util.Collections.<String>emptySet());
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordSuppressions_pairwise_257() throws Exception {
        // Combination: receiver__parseDocumentation=false, suppressions=java.util.Collections.singleton("a")
        Object actual = (new JSDocInfoBuilder(false)).recordSuppressions(java.util.Collections.singleton("a"));
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_258() throws Exception {
        // Combination: receiver__parseDocumentation=true, description=""
        Object actual = (new JSDocInfoBuilder(true)).recordReturnDescription("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_259() throws Exception {
        // Combination: receiver__parseDocumentation=false, description=""
        Object actual = (new JSDocInfoBuilder(false)).recordReturnDescription("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_260() throws Exception {
        // Combination: receiver__parseDocumentation=true, description=" "
        Object actual = (new JSDocInfoBuilder(true)).recordReturnDescription(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_261() throws Exception {
        // Combination: receiver__parseDocumentation=false, description=" "
        Object actual = (new JSDocInfoBuilder(false)).recordReturnDescription(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_262() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="a"
        Object actual = (new JSDocInfoBuilder(true)).recordReturnDescription("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_263() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="a"
        Object actual = (new JSDocInfoBuilder(false)).recordReturnDescription("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_264() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordReturnDescription("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_265() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="test123"
        Object actual = (new JSDocInfoBuilder(false)).recordReturnDescription("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_266() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordReturnDescription("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_267() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="!@#"
        Object actual = (new JSDocInfoBuilder(false)).recordReturnDescription("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_268() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="0"
        Object actual = (new JSDocInfoBuilder(true)).recordReturnDescription("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_269() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="0"
        Object actual = (new JSDocInfoBuilder(false)).recordReturnDescription("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_270() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordReturnDescription("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_271() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="-1"
        Object actual = (new JSDocInfoBuilder(false)).recordReturnDescription("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_272() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordReturnDescription("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_273() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="1.5"
        Object actual = (new JSDocInfoBuilder(false)).recordReturnDescription("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_274() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordReturnDescription("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_275() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(false)).recordReturnDescription("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_276() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordReturnDescription("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_277() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(false)).recordReturnDescription("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_278() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordReturnDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordReturnDescription_pairwise_279() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(false)).recordReturnDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_280() throws Exception {
        // Combination: receiver__parseDocumentation=true, description=""
        Object actual = (new JSDocInfoBuilder(true)).recordDescription("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_281() throws Exception {
        // Combination: receiver__parseDocumentation=false, description=""
        Object actual = (new JSDocInfoBuilder(false)).recordDescription("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_282() throws Exception {
        // Combination: receiver__parseDocumentation=true, description=" "
        Object actual = (new JSDocInfoBuilder(true)).recordDescription(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_283() throws Exception {
        // Combination: receiver__parseDocumentation=false, description=" "
        Object actual = (new JSDocInfoBuilder(false)).recordDescription(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_284() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="a"
        Object actual = (new JSDocInfoBuilder(true)).recordDescription("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_285() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="a"
        Object actual = (new JSDocInfoBuilder(false)).recordDescription("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_286() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordDescription("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_287() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="test123"
        Object actual = (new JSDocInfoBuilder(false)).recordDescription("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_288() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordDescription("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_289() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="!@#"
        Object actual = (new JSDocInfoBuilder(false)).recordDescription("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_290() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="0"
        Object actual = (new JSDocInfoBuilder(true)).recordDescription("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_291() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="0"
        Object actual = (new JSDocInfoBuilder(false)).recordDescription("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_292() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordDescription("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_293() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="-1"
        Object actual = (new JSDocInfoBuilder(false)).recordDescription("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_294() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordDescription("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_295() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="1.5"
        Object actual = (new JSDocInfoBuilder(false)).recordDescription("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_296() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordDescription("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_297() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(false)).recordDescription("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_298() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordDescription("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_299() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(false)).recordDescription("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_300() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordDescription_pairwise_301() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(false)).recordDescription("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_302() throws Exception {
        // Combination: receiver__parseDocumentation=true, description=""
        Object actual = (new JSDocInfoBuilder(true)).recordFileOverview("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_303() throws Exception {
        // Combination: receiver__parseDocumentation=false, description=""
        Object actual = (new JSDocInfoBuilder(false)).recordFileOverview("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_304() throws Exception {
        // Combination: receiver__parseDocumentation=true, description=" "
        Object actual = (new JSDocInfoBuilder(true)).recordFileOverview(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_305() throws Exception {
        // Combination: receiver__parseDocumentation=false, description=" "
        Object actual = (new JSDocInfoBuilder(false)).recordFileOverview(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_306() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="a"
        Object actual = (new JSDocInfoBuilder(true)).recordFileOverview("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_307() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="a"
        Object actual = (new JSDocInfoBuilder(false)).recordFileOverview("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_308() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="test123"
        Object actual = (new JSDocInfoBuilder(true)).recordFileOverview("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_309() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="test123"
        Object actual = (new JSDocInfoBuilder(false)).recordFileOverview("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_310() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="!@#"
        Object actual = (new JSDocInfoBuilder(true)).recordFileOverview("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_311() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="!@#"
        Object actual = (new JSDocInfoBuilder(false)).recordFileOverview("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_312() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="0"
        Object actual = (new JSDocInfoBuilder(true)).recordFileOverview("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_313() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="0"
        Object actual = (new JSDocInfoBuilder(false)).recordFileOverview("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_314() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="-1"
        Object actual = (new JSDocInfoBuilder(true)).recordFileOverview("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_315() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="-1"
        Object actual = (new JSDocInfoBuilder(false)).recordFileOverview("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_316() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="1.5"
        Object actual = (new JSDocInfoBuilder(true)).recordFileOverview("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_317() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="1.5"
        Object actual = (new JSDocInfoBuilder(false)).recordFileOverview("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_318() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).recordFileOverview("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_319() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(false)).recordFileOverview("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_320() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).recordFileOverview("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_321() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(false)).recordFileOverview("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_322() throws Exception {
        // Combination: receiver__parseDocumentation=true, description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).recordFileOverview("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_recordFileOverview_pairwise_323() throws Exception {
        // Combination: receiver__parseDocumentation=false, description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(false)).recordFileOverview("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_324() throws Exception {
        // Combination: receiver__parseDocumentation=true, name=""
        Object actual = (new JSDocInfoBuilder(true)).hasParameter("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_325() throws Exception {
        // Combination: receiver__parseDocumentation=false, name=""
        Object actual = (new JSDocInfoBuilder(false)).hasParameter("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_326() throws Exception {
        // Combination: receiver__parseDocumentation=true, name=" "
        Object actual = (new JSDocInfoBuilder(true)).hasParameter(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_327() throws Exception {
        // Combination: receiver__parseDocumentation=false, name=" "
        Object actual = (new JSDocInfoBuilder(false)).hasParameter(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_328() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="a"
        Object actual = (new JSDocInfoBuilder(true)).hasParameter("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_329() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="a"
        Object actual = (new JSDocInfoBuilder(false)).hasParameter("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_330() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="test123"
        Object actual = (new JSDocInfoBuilder(true)).hasParameter("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_331() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="test123"
        Object actual = (new JSDocInfoBuilder(false)).hasParameter("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_332() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="!@#"
        Object actual = (new JSDocInfoBuilder(true)).hasParameter("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_333() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="!@#"
        Object actual = (new JSDocInfoBuilder(false)).hasParameter("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_334() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="0"
        Object actual = (new JSDocInfoBuilder(true)).hasParameter("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_335() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="0"
        Object actual = (new JSDocInfoBuilder(false)).hasParameter("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_336() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="-1"
        Object actual = (new JSDocInfoBuilder(true)).hasParameter("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_337() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="-1"
        Object actual = (new JSDocInfoBuilder(false)).hasParameter("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_338() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="1.5"
        Object actual = (new JSDocInfoBuilder(true)).hasParameter("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_339() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="1.5"
        Object actual = (new JSDocInfoBuilder(false)).hasParameter("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_340() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(true)).hasParameter("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_341() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="9223372036854775807"
        Object actual = (new JSDocInfoBuilder(false)).hasParameter("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_342() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(true)).hasParameter("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_343() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="9223372036854775808"
        Object actual = (new JSDocInfoBuilder(false)).hasParameter("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_344() throws Exception {
        // Combination: receiver__parseDocumentation=true, name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(true)).hasParameter("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

    @Test(timeout = 4000)
    public void test_hasParameter_pairwise_345() throws Exception {
        // Combination: receiver__parseDocumentation=false, name="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new JSDocInfoBuilder(false)).hasParameter("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", formatValue(actual));
    }

}
