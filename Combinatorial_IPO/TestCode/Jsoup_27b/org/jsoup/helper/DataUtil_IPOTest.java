package org.jsoup.helper;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for DataUtil.
 */
public class DataUtil_IPOTest {
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
    public void test_load_pairwise_001() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri=""
        try {
            DataUtil.load(new java.io.File("temp.txt"), "", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_002() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri=""
        try {
            DataUtil.load(new java.io.File("."), " ", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_003() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri=""
        try {
            DataUtil.load(new java.io.File("temp.txt"), "a", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_004() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri=""
        try {
            DataUtil.load(new java.io.File("temp.txt"), "test123", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_005() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri=""
        try {
            DataUtil.load(new java.io.File("temp.txt"), "!@#", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_006() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri=""
        try {
            DataUtil.load(new java.io.File("temp.txt"), "0", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_007() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri=""
        try {
            DataUtil.load(new java.io.File("temp.txt"), "-1", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_008() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri=""
        try {
            DataUtil.load(new java.io.File("temp.txt"), "1.5", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_009() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri=""
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775807", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_010() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri=""
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775808", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_011() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=""
        try {
            DataUtil.load(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_012() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="", baseUri=" "
        try {
            DataUtil.load(new java.io.File("."), "", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_013() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName=" ", baseUri=" "
        try {
            DataUtil.load(new java.io.File("temp.txt"), " ", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_014() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="a", baseUri=" "
        try {
            DataUtil.load(new java.io.File("."), "a", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_015() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="test123", baseUri=" "
        try {
            DataUtil.load(new java.io.File("."), "test123", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_016() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="!@#", baseUri=" "
        try {
            DataUtil.load(new java.io.File("."), "!@#", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_017() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="0", baseUri=" "
        try {
            DataUtil.load(new java.io.File("."), "0", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_018() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="-1", baseUri=" "
        try {
            DataUtil.load(new java.io.File("."), "-1", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_019() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="1.5", baseUri=" "
        try {
            DataUtil.load(new java.io.File("."), "1.5", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_020() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="9223372036854775807", baseUri=" "
        try {
            DataUtil.load(new java.io.File("."), "9223372036854775807", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_021() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="9223372036854775808", baseUri=" "
        try {
            DataUtil.load(new java.io.File("."), "9223372036854775808", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_022() throws Exception {
        // Combination: in=new java.io.File("."), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=" "
        try {
            DataUtil.load(new java.io.File("."), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_023() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="a"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_024() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="a"
        try {
            DataUtil.load(new java.io.File("."), " ", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_025() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="a"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "a", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_026() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="a"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "test123", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_027() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="a"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "!@#", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_028() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="a"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "0", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_029() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="a"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "-1", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_030() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="a"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "1.5", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_031() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="a"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775807", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_032() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="a"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775808", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_033() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="a"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_034() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="test123"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_035() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="test123"
        try {
            DataUtil.load(new java.io.File("."), " ", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_036() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="test123"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "a", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_037() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="test123"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "test123", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_038() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="test123"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "!@#", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_039() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="test123"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "0", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_040() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="test123"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "-1", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_041() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="test123"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "1.5", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_042() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="test123"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775807", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_043() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="test123"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775808", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_044() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="test123"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_045() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="!@#"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_046() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="!@#"
        try {
            DataUtil.load(new java.io.File("."), " ", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_047() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="!@#"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "a", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_048() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="!@#"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "test123", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_049() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="!@#"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "!@#", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_050() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="!@#"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "0", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_051() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="!@#"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "-1", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_052() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="!@#"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "1.5", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_053() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="!@#"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775807", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_054() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="!@#"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775808", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_055() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="!@#"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_056() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="0"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_057() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="0"
        try {
            DataUtil.load(new java.io.File("."), " ", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_058() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="0"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "a", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_059() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="0"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "test123", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_060() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="0"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "!@#", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_061() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="0"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "0", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_062() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="0"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "-1", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_063() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="0"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "1.5", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_064() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="0"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775807", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_065() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="0"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775808", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_066() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="0"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_067() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="-1"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_068() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="-1"
        try {
            DataUtil.load(new java.io.File("."), " ", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_069() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="-1"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "a", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_070() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="-1"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "test123", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_071() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="-1"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "!@#", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_072() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="-1"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "0", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_073() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="-1"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "-1", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_074() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="-1"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "1.5", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_075() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="-1"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775807", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_076() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="-1"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775808", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_077() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="-1"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_078() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="1.5"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_079() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="1.5"
        try {
            DataUtil.load(new java.io.File("."), " ", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_080() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="1.5"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "a", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_081() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="1.5"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "test123", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_082() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="1.5"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "!@#", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_083() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="1.5"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "0", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_084() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="1.5"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "-1", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_085() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="1.5"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "1.5", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_086() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="1.5"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775807", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_087() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="1.5"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775808", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_088() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="1.5"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_089() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_090() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.File("."), " ", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_091() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "a", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_092() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "test123", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_093() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "!@#", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_094() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "0", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_095() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "-1", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_096() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "1.5", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_097() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775807", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_098() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775808", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_099() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_100() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_101() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.File("."), " ", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_102() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "a", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_103() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "test123", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_104() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "!@#", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_105() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "0", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_106() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "-1", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_107() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "1.5", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_108() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775807", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_109() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775808", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_110() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_111() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_112() throws Exception {
        // Combination: in=new java.io.File("."), charsetName=" ", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.File("."), " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_113() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="a", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_114() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="test123", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_115() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="!@#", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_116() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="0", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_117() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="-1", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_118() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="1.5", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_119() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775807", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_120() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="9223372036854775808", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_121() throws Exception {
        // Combination: in=new java.io.File("temp.txt"), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.File("temp.txt"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.io.FileNotFoundException");
        } catch (java.io.FileNotFoundException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_122() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri=""
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "", "");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_123() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri=""
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_124() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri=""
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "a", "");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_125() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri=""
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_126() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri=""
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_127() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri=""
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "0", "");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_128() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri=""
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_129() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri=""
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_130() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri=""
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_131() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri=""
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_132() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=""
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_133() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="", baseUri=" "
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), "", " ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_134() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName=" ", baseUri=" "
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), " ", " ");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_135() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="a", baseUri=" "
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), "a", " ");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_136() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="test123", baseUri=" "
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), "test123", " ");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_137() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="!@#", baseUri=" "
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), "!@#", " ");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_138() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="0", baseUri=" "
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), "0", " ");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_139() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="-1", baseUri=" "
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), "-1", " ");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_140() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="1.5", baseUri=" "
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), "1.5", " ");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_141() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="9223372036854775807", baseUri=" "
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), "9223372036854775807", " ");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_142() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="9223372036854775808", baseUri=" "
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), "9223372036854775808", " ");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_143() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri=" "
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_144() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="a"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "", "a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_145() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="a"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "a");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_146() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="a"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "a", "a");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_147() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="a"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "a");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_148() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="a"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "a");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_149() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="a"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "0", "a");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_150() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="a"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "a");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_151() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="a"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "a");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_152() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="a"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "a");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_153() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="a"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "a");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_154() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="a"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_155() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="test123"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "", "test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_156() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="test123"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "test123");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_157() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="test123"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "a", "test123");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_158() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="test123"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "test123");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_159() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="test123"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "test123");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_160() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="test123"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "0", "test123");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_161() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="test123"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "test123");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_162() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="test123"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "test123");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_163() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="test123"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "test123");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_164() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="test123"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "test123");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_165() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="test123"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_166() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="!@#"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "", "!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_167() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="!@#"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "!@#");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_168() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="!@#"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "a", "!@#");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_169() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="!@#"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "!@#");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_170() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="!@#"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "!@#");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_171() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="!@#"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "0", "!@#");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_172() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="!@#"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "!@#");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_173() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="!@#"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "!@#");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_174() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="!@#"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "!@#");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_175() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="!@#"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "!@#");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_176() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="!@#"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_177() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="0"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "", "0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_178() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="0"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "0");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_179() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="0"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "a", "0");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_180() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="0"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "0");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_181() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="0"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "0");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_182() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="0"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "0", "0");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_183() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="0"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "0");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_184() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="0"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "0");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_185() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="0"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "0");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_186() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="0"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "0");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_187() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="0"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_188() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="-1"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "", "-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_189() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="-1"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "-1");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_190() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="-1"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "a", "-1");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_191() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="-1"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "-1");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_192() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="-1"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "-1");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_193() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="-1"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "0", "-1");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_194() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="-1"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "-1");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_195() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="-1"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "-1");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_196() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="-1"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "-1");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_197() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="-1"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "-1");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_198() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="-1"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_199() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="1.5"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "", "1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_200() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="1.5"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "1.5");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_201() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="1.5"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "a", "1.5");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_202() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="1.5"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "1.5");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_203() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="1.5"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "1.5");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_204() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="1.5"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "0", "1.5");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_205() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="1.5"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "1.5");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_206() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="1.5"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "1.5");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_207() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="1.5"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "1.5");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_208() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="1.5"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "1.5");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_209() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="1.5"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_210() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "", "9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_211() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "9223372036854775807");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_212() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "a", "9223372036854775807");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_213() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "9223372036854775807");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_214() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "9223372036854775807");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_215() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "0", "9223372036854775807");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_216() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "9223372036854775807");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_217() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "9223372036854775807");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_218() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "9223372036854775807");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_219() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "9223372036854775807");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_220() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775807"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_221() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "", "9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_222() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "9223372036854775808");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_223() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "a", "9223372036854775808");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_224() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "9223372036854775808");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_225() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "9223372036854775808");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_226() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "0", "9223372036854775808");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_227() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "9223372036854775808");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_228() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "9223372036854775808");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_229() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "9223372036854775808");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_230() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "9223372036854775808");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_231() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="9223372036854775808"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_232() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_233() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {1}), charsetName=" ", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {1}), " ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_234() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="a", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_235() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="test123", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_236() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="!@#", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_237() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="0", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_238() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="-1", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.IllegalCharsetNameException");
        } catch (java.nio.charset.IllegalCharsetNameException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_239() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="1.5", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_240() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775807", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_241() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="9223372036854775808", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_load_pairwise_242() throws Exception {
        // Combination: in=new java.io.ByteArrayInputStream(new byte[] {}), charsetName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", baseUri="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            DataUtil.load(new java.io.ByteArrayInputStream(new byte[] {}), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.nio.charset.UnsupportedCharsetException");
        } catch (java.nio.charset.UnsupportedCharsetException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
