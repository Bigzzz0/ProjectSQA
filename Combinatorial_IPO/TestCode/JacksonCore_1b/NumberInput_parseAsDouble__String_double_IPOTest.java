package com.fasterxml.jackson.core.io;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for NumberInput.
 */
public class NumberInput_parseAsDouble__String_double_IPOTest {
    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_001() throws Exception {
        // Combination: input=null, defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble(null, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_002() throws Exception {
        // Combination: input=null, defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble(null, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_003() throws Exception {
        // Combination: input=null, defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble(null, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_004() throws Exception {
        // Combination: input=null, defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble(null, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_005() throws Exception {
        // Combination: input=null, defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble(null, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_006() throws Exception {
        // Combination: input="", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_007() throws Exception {
        // Combination: input="", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_008() throws Exception {
        // Combination: input="", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_009() throws Exception {
        // Combination: input="", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_010() throws Exception {
        // Combination: input="", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_011() throws Exception {
        // Combination: input=" ", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble(" ", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_012() throws Exception {
        // Combination: input=" ", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble(" ", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_013() throws Exception {
        // Combination: input=" ", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble(" ", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_014() throws Exception {
        // Combination: input=" ", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble(" ", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_015() throws Exception {
        // Combination: input=" ", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble(" ", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_016() throws Exception {
        // Combination: input="a", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("a", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_017() throws Exception {
        // Combination: input="a", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("a", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_018() throws Exception {
        // Combination: input="a", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("a", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_019() throws Exception {
        // Combination: input="a", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("a", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_020() throws Exception {
        // Combination: input="a", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("a", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_021() throws Exception {
        // Combination: input="test123", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("test123", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_022() throws Exception {
        // Combination: input="test123", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("test123", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_023() throws Exception {
        // Combination: input="test123", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("test123", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_024() throws Exception {
        // Combination: input="test123", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("test123", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_025() throws Exception {
        // Combination: input="test123", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("test123", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_026() throws Exception {
        // Combination: input="!@#", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("!@#", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_027() throws Exception {
        // Combination: input="!@#", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("!@#", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_028() throws Exception {
        // Combination: input="!@#", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("!@#", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_029() throws Exception {
        // Combination: input="!@#", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("!@#", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_030() throws Exception {
        // Combination: input="!@#", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("!@#", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_031() throws Exception {
        // Combination: input="0", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("0", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_032() throws Exception {
        // Combination: input="0", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("0", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_033() throws Exception {
        // Combination: input="0", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("0", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_034() throws Exception {
        // Combination: input="0", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("0", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_035() throws Exception {
        // Combination: input="0", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("0", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_036() throws Exception {
        // Combination: input="-1", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("-1", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_037() throws Exception {
        // Combination: input="-1", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("-1", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_038() throws Exception {
        // Combination: input="-1", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("-1", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_039() throws Exception {
        // Combination: input="-1", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("-1", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_040() throws Exception {
        // Combination: input="-1", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("-1", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_041() throws Exception {
        // Combination: input="1.5", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("1.5", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_042() throws Exception {
        // Combination: input="1.5", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("1.5", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_043() throws Exception {
        // Combination: input="1.5", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("1.5", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_044() throws Exception {
        // Combination: input="1.5", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("1.5", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_045() throws Exception {
        // Combination: input="1.5", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("1.5", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_046() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("9223372036854775807", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_047() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("9223372036854775807", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_048() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("9223372036854775807", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_049() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("9223372036854775807", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_050() throws Exception {
        // Combination: input="9223372036854775807", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("9223372036854775807", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_051() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("9223372036854775808", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_052() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("9223372036854775808", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_053() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("9223372036854775808", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_054() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("9223372036854775808", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_055() throws Exception {
        // Combination: input="9223372036854775808", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("9223372036854775808", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_056() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=0.0d
        Object actual = NumberInput.parseAsDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_057() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=1.0d
        Object actual = NumberInput.parseAsDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_058() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=-1.0d
        Object actual = NumberInput.parseAsDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_059() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Double.NaN
        Object actual = NumberInput.parseAsDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_parseAsDouble_pairwise_060() throws Exception {
        // Combination: input="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", defaultValue=Double.POSITIVE_INFINITY
        Object actual = NumberInput.parseAsDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

}
