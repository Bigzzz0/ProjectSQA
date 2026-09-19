package org.apache.commons.codec.language;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for DoubleMetaphone.
 */
public class DoubleMetaphone_IPOTest {
    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_001() throws Exception {
        // Combination: value1="", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_002() throws Exception {
        // Combination: value1=" ", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_003() throws Exception {
        // Combination: value1="a", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_004() throws Exception {
        // Combination: value1="test123", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_005() throws Exception {
        // Combination: value1="!@#", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_006() throws Exception {
        // Combination: value1="0", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_007() throws Exception {
        // Combination: value1="-1", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_008() throws Exception {
        // Combination: value1="1.5", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_009() throws Exception {
        // Combination: value1="9223372036854775807", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_010() throws Exception {
        // Combination: value1="9223372036854775808", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_011() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_012() throws Exception {
        // Combination: value1="", value2=" ", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("", " ", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_013() throws Exception {
        // Combination: value1=" ", value2="", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_014() throws Exception {
        // Combination: value1="a", value2="test123", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "test123", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_015() throws Exception {
        // Combination: value1="test123", value2="a", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "a", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_016() throws Exception {
        // Combination: value1="!@#", value2="0", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "0", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_017() throws Exception {
        // Combination: value1="0", value2="!@#", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "!@#", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_018() throws Exception {
        // Combination: value1="-1", value2="1.5", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "1.5", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_019() throws Exception {
        // Combination: value1="1.5", value2="-1", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "-1", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_020() throws Exception {
        // Combination: value1="9223372036854775807", value2="9223372036854775808", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "9223372036854775808", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_021() throws Exception {
        // Combination: value1="9223372036854775808", value2="9223372036854775807", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "9223372036854775807", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_022() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_023() throws Exception {
        // Combination: value1="a", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_024() throws Exception {
        // Combination: value1="test123", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_025() throws Exception {
        // Combination: value1="!@#", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_026() throws Exception {
        // Combination: value1="0", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_027() throws Exception {
        // Combination: value1="-1", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_028() throws Exception {
        // Combination: value1="1.5", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_029() throws Exception {
        // Combination: value1="9223372036854775807", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_030() throws Exception {
        // Combination: value1="9223372036854775808", value2="", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_031() throws Exception {
        // Combination: value1="a", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_032() throws Exception {
        // Combination: value1="test123", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_033() throws Exception {
        // Combination: value1="!@#", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_034() throws Exception {
        // Combination: value1="0", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_035() throws Exception {
        // Combination: value1="-1", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_036() throws Exception {
        // Combination: value1="1.5", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_037() throws Exception {
        // Combination: value1="9223372036854775807", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_038() throws Exception {
        // Combination: value1="9223372036854775808", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_039() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2=" ", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_040() throws Exception {
        // Combination: value1="", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_041() throws Exception {
        // Combination: value1=" ", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_042() throws Exception {
        // Combination: value1="!@#", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_043() throws Exception {
        // Combination: value1="0", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_044() throws Exception {
        // Combination: value1="-1", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_045() throws Exception {
        // Combination: value1="1.5", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_046() throws Exception {
        // Combination: value1="9223372036854775807", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_047() throws Exception {
        // Combination: value1="9223372036854775808", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_048() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="a", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_049() throws Exception {
        // Combination: value1="", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_050() throws Exception {
        // Combination: value1=" ", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_051() throws Exception {
        // Combination: value1="!@#", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_052() throws Exception {
        // Combination: value1="0", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_053() throws Exception {
        // Combination: value1="-1", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_054() throws Exception {
        // Combination: value1="1.5", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_055() throws Exception {
        // Combination: value1="9223372036854775807", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_056() throws Exception {
        // Combination: value1="9223372036854775808", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_057() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="test123", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_058() throws Exception {
        // Combination: value1="", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_059() throws Exception {
        // Combination: value1=" ", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_060() throws Exception {
        // Combination: value1="a", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_061() throws Exception {
        // Combination: value1="test123", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_062() throws Exception {
        // Combination: value1="-1", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_063() throws Exception {
        // Combination: value1="1.5", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_064() throws Exception {
        // Combination: value1="9223372036854775807", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_065() throws Exception {
        // Combination: value1="9223372036854775808", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_066() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="!@#", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_067() throws Exception {
        // Combination: value1="", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_068() throws Exception {
        // Combination: value1=" ", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_069() throws Exception {
        // Combination: value1="a", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_070() throws Exception {
        // Combination: value1="test123", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_071() throws Exception {
        // Combination: value1="-1", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_072() throws Exception {
        // Combination: value1="1.5", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_073() throws Exception {
        // Combination: value1="9223372036854775807", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_074() throws Exception {
        // Combination: value1="9223372036854775808", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_075() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="0", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_076() throws Exception {
        // Combination: value1="", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_077() throws Exception {
        // Combination: value1=" ", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_078() throws Exception {
        // Combination: value1="a", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_079() throws Exception {
        // Combination: value1="test123", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_080() throws Exception {
        // Combination: value1="!@#", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_081() throws Exception {
        // Combination: value1="0", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_082() throws Exception {
        // Combination: value1="9223372036854775807", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_083() throws Exception {
        // Combination: value1="9223372036854775808", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_084() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="-1", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_085() throws Exception {
        // Combination: value1="", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_086() throws Exception {
        // Combination: value1=" ", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_087() throws Exception {
        // Combination: value1="a", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_088() throws Exception {
        // Combination: value1="test123", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_089() throws Exception {
        // Combination: value1="!@#", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_090() throws Exception {
        // Combination: value1="0", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_091() throws Exception {
        // Combination: value1="9223372036854775807", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_092() throws Exception {
        // Combination: value1="9223372036854775808", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_093() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="1.5", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_094() throws Exception {
        // Combination: value1="", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_095() throws Exception {
        // Combination: value1=" ", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_096() throws Exception {
        // Combination: value1="a", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_097() throws Exception {
        // Combination: value1="test123", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_098() throws Exception {
        // Combination: value1="!@#", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_099() throws Exception {
        // Combination: value1="0", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_100() throws Exception {
        // Combination: value1="-1", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_101() throws Exception {
        // Combination: value1="1.5", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_102() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="9223372036854775807", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_103() throws Exception {
        // Combination: value1="", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_104() throws Exception {
        // Combination: value1=" ", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_105() throws Exception {
        // Combination: value1="a", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_106() throws Exception {
        // Combination: value1="test123", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_107() throws Exception {
        // Combination: value1="!@#", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_108() throws Exception {
        // Combination: value1="0", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_109() throws Exception {
        // Combination: value1="-1", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_110() throws Exception {
        // Combination: value1="1.5", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_111() throws Exception {
        // Combination: value1="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", value2="9223372036854775808", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_112() throws Exception {
        // Combination: value1="", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=false
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", false);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_113() throws Exception {
        // Combination: value1=" ", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_114() throws Exception {
        // Combination: value1="a", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_115() throws Exception {
        // Combination: value1="test123", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_116() throws Exception {
        // Combination: value1="!@#", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_117() throws Exception {
        // Combination: value1="0", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_118() throws Exception {
        // Combination: value1="-1", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_119() throws Exception {
        // Combination: value1="1.5", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_120() throws Exception {
        // Combination: value1="9223372036854775807", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_isDoubleMetaphoneEqual_pairwise_121() throws Exception {
        // Combination: value1="9223372036854775808", value2="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", alternate=true
        Object actual = (new DoubleMetaphone()).isDoubleMetaphoneEqual("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", true);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

}
