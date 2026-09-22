package com.fasterxml.jackson.databind.deser.std;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for StdKeyDeserializer.
 */
public class StdKeyDeserializer_IPOTest {
    @Test(timeout = 4000)
    public void test__parseInt_pairwise_001() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key=""
        try {
            (new StdKeyDeserializer(0, String.class))._parseInt("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_002() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key=""
        try {
            (new StdKeyDeserializer(1, Object.class))._parseInt("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_003() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Integer.class, key=""
        try {
            (new StdKeyDeserializer(-1, Integer.class))._parseInt("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_004() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=String.class, key=" "
        try {
            (new StdKeyDeserializer(1, String.class))._parseInt(" ");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_005() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=Object.class, key=" "
        try {
            (new StdKeyDeserializer(0, Object.class))._parseInt(" ");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_006() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=Integer.class, key=" "
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, Integer.class))._parseInt(" ");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_007() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=String.class, key="a"
        try {
            (new StdKeyDeserializer(-1, String.class))._parseInt("a");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_008() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=Object.class, key="a"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, Object.class))._parseInt("a");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_009() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=Integer.class, key="a"
        try {
            (new StdKeyDeserializer(0, Integer.class))._parseInt("a");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_010() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="test123"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseInt("test123");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_011() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Object.class, key="test123"
        try {
            (new StdKeyDeserializer(-1, Object.class))._parseInt("test123");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_012() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Integer.class, key="test123"
        try {
            (new StdKeyDeserializer(1, Integer.class))._parseInt("test123");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_013() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="!@#"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseInt("!@#");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_014() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=Object.class, key="!@#"
        try {
            (new StdKeyDeserializer(0, Object.class))._parseInt("!@#");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_015() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Integer.class, key="!@#"
        try {
            (new StdKeyDeserializer(1, Integer.class))._parseInt("!@#");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_016() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="0"
        Object actual = (new StdKeyDeserializer(0, String.class))._parseInt("0");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_017() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=Object.class, key="0"
        Object actual = (new StdKeyDeserializer(Integer.MIN_VALUE, Object.class))._parseInt("0");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_018() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Integer.class, key="0"
        Object actual = (new StdKeyDeserializer(1, Integer.class))._parseInt("0");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_019() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="-1"
        Object actual = (new StdKeyDeserializer(0, String.class))._parseInt("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_020() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key="-1"
        Object actual = (new StdKeyDeserializer(1, Object.class))._parseInt("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_021() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=Integer.class, key="-1"
        Object actual = (new StdKeyDeserializer(Integer.MIN_VALUE, Integer.class))._parseInt("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_022() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="1.5"
        try {
            (new StdKeyDeserializer(0, String.class))._parseInt("1.5");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_023() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key="1.5"
        try {
            (new StdKeyDeserializer(1, Object.class))._parseInt("1.5");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_024() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Integer.class, key="1.5"
        try {
            (new StdKeyDeserializer(-1, Integer.class))._parseInt("1.5");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_025() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="9223372036854775807"
        try {
            (new StdKeyDeserializer(0, String.class))._parseInt("9223372036854775807");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_026() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key="9223372036854775807"
        try {
            (new StdKeyDeserializer(1, Object.class))._parseInt("9223372036854775807");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_027() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Integer.class, key="9223372036854775807"
        try {
            (new StdKeyDeserializer(-1, Integer.class))._parseInt("9223372036854775807");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_028() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="9223372036854775808"
        try {
            (new StdKeyDeserializer(0, String.class))._parseInt("9223372036854775808");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_029() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key="9223372036854775808"
        try {
            (new StdKeyDeserializer(1, Object.class))._parseInt("9223372036854775808");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_030() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Integer.class, key="9223372036854775808"
        try {
            (new StdKeyDeserializer(-1, Integer.class))._parseInt("9223372036854775808");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_031() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new StdKeyDeserializer(0, String.class))._parseInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_032() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new StdKeyDeserializer(1, Object.class))._parseInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_033() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Integer.class, key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new StdKeyDeserializer(-1, Integer.class))._parseInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_034() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="test123"
        try {
            (new StdKeyDeserializer(0, String.class))._parseInt("test123");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_035() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=String.class, key="a"
        try {
            (new StdKeyDeserializer(1, String.class))._parseInt("a");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_036() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=String.class, key=" "
        try {
            (new StdKeyDeserializer(-1, String.class))._parseInt(" ");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_037() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=String.class, key="!@#"
        try {
            (new StdKeyDeserializer(-1, String.class))._parseInt("!@#");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_038() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=String.class, key="0"
        Object actual = (new StdKeyDeserializer(-1, String.class))._parseInt("0");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_039() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=String.class, key="-1"
        Object actual = (new StdKeyDeserializer(-1, String.class))._parseInt("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_040() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key=""
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseInt("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_041() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="!@#"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseInt("!@#");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_042() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="0"
        Object actual = (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseInt("0");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_043() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="-1"
        Object actual = (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseInt("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_044() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="1.5"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseInt("1.5");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_045() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="9223372036854775807"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseInt("9223372036854775807");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_046() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="9223372036854775808"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseInt("9223372036854775808");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_047() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_048() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key=""
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseInt("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_049() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key=" "
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseInt(" ");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_050() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="a"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseInt("a");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_051() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="test123"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseInt("test123");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_052() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="1.5"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseInt("1.5");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_053() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="9223372036854775807"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseInt("9223372036854775807");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_054() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="9223372036854775808"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseInt("9223372036854775808");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseInt_pairwise_055() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseInt("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_056() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key=""
        try {
            (new StdKeyDeserializer(0, String.class))._parseLong("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_057() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key=""
        try {
            (new StdKeyDeserializer(1, Object.class))._parseLong("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_058() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Integer.class, key=""
        try {
            (new StdKeyDeserializer(-1, Integer.class))._parseLong("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_059() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=String.class, key=" "
        try {
            (new StdKeyDeserializer(1, String.class))._parseLong(" ");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_060() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=Object.class, key=" "
        try {
            (new StdKeyDeserializer(0, Object.class))._parseLong(" ");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_061() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=Integer.class, key=" "
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, Integer.class))._parseLong(" ");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_062() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=String.class, key="a"
        try {
            (new StdKeyDeserializer(-1, String.class))._parseLong("a");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_063() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=Object.class, key="a"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, Object.class))._parseLong("a");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_064() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=Integer.class, key="a"
        try {
            (new StdKeyDeserializer(0, Integer.class))._parseLong("a");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_065() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="test123"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseLong("test123");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_066() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Object.class, key="test123"
        try {
            (new StdKeyDeserializer(-1, Object.class))._parseLong("test123");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_067() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Integer.class, key="test123"
        try {
            (new StdKeyDeserializer(1, Integer.class))._parseLong("test123");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_068() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="!@#"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseLong("!@#");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_069() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=Object.class, key="!@#"
        try {
            (new StdKeyDeserializer(0, Object.class))._parseLong("!@#");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_070() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Integer.class, key="!@#"
        try {
            (new StdKeyDeserializer(1, Integer.class))._parseLong("!@#");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_071() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="0"
        Object actual = (new StdKeyDeserializer(0, String.class))._parseLong("0");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_072() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=Object.class, key="0"
        Object actual = (new StdKeyDeserializer(Integer.MIN_VALUE, Object.class))._parseLong("0");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_073() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Integer.class, key="0"
        Object actual = (new StdKeyDeserializer(1, Integer.class))._parseLong("0");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_074() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="-1"
        Object actual = (new StdKeyDeserializer(0, String.class))._parseLong("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_075() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key="-1"
        Object actual = (new StdKeyDeserializer(1, Object.class))._parseLong("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_076() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=Integer.class, key="-1"
        Object actual = (new StdKeyDeserializer(Integer.MIN_VALUE, Integer.class))._parseLong("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_077() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="1.5"
        try {
            (new StdKeyDeserializer(0, String.class))._parseLong("1.5");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_078() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key="1.5"
        try {
            (new StdKeyDeserializer(1, Object.class))._parseLong("1.5");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_079() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Integer.class, key="1.5"
        try {
            (new StdKeyDeserializer(-1, Integer.class))._parseLong("1.5");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_080() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="9223372036854775807"
        Object actual = (new StdKeyDeserializer(0, String.class))._parseLong("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_081() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key="9223372036854775807"
        Object actual = (new StdKeyDeserializer(1, Object.class))._parseLong("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_082() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Integer.class, key="9223372036854775807"
        Object actual = (new StdKeyDeserializer(-1, Integer.class))._parseLong("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_083() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="9223372036854775808"
        try {
            (new StdKeyDeserializer(0, String.class))._parseLong("9223372036854775808");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_084() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key="9223372036854775808"
        try {
            (new StdKeyDeserializer(1, Object.class))._parseLong("9223372036854775808");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_085() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Integer.class, key="9223372036854775808"
        try {
            (new StdKeyDeserializer(-1, Integer.class))._parseLong("9223372036854775808");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_086() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new StdKeyDeserializer(0, String.class))._parseLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_087() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new StdKeyDeserializer(1, Object.class))._parseLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_088() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Integer.class, key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new StdKeyDeserializer(-1, Integer.class))._parseLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_089() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="test123"
        try {
            (new StdKeyDeserializer(0, String.class))._parseLong("test123");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_090() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=String.class, key="a"
        try {
            (new StdKeyDeserializer(1, String.class))._parseLong("a");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_091() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=String.class, key=" "
        try {
            (new StdKeyDeserializer(-1, String.class))._parseLong(" ");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_092() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=String.class, key="!@#"
        try {
            (new StdKeyDeserializer(-1, String.class))._parseLong("!@#");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_093() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=String.class, key="0"
        Object actual = (new StdKeyDeserializer(-1, String.class))._parseLong("0");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_094() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=String.class, key="-1"
        Object actual = (new StdKeyDeserializer(-1, String.class))._parseLong("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_095() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key=""
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseLong("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_096() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="!@#"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseLong("!@#");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_097() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="0"
        Object actual = (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseLong("0");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_098() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="-1"
        Object actual = (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseLong("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_099() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="1.5"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseLong("1.5");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_100() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="9223372036854775807"
        Object actual = (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseLong("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_101() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="9223372036854775808"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseLong("9223372036854775808");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_102() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_103() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key=""
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseLong("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_104() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key=" "
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseLong(" ");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_105() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="a"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseLong("a");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_106() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="test123"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseLong("test123");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_107() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="1.5"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseLong("1.5");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_108() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="9223372036854775807"
        Object actual = (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseLong("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_109() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="9223372036854775808"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseLong("9223372036854775808");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseLong_pairwise_110() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseLong("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_111() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key=""
        try {
            (new StdKeyDeserializer(0, String.class))._parseDouble("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_112() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key=""
        try {
            (new StdKeyDeserializer(1, Object.class))._parseDouble("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_113() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Integer.class, key=""
        try {
            (new StdKeyDeserializer(-1, Integer.class))._parseDouble("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_114() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=String.class, key=" "
        try {
            (new StdKeyDeserializer(1, String.class))._parseDouble(" ");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_115() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=Object.class, key=" "
        try {
            (new StdKeyDeserializer(0, Object.class))._parseDouble(" ");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_116() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=Integer.class, key=" "
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, Integer.class))._parseDouble(" ");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_117() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=String.class, key="a"
        try {
            (new StdKeyDeserializer(-1, String.class))._parseDouble("a");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_118() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=Object.class, key="a"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, Object.class))._parseDouble("a");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_119() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=Integer.class, key="a"
        try {
            (new StdKeyDeserializer(0, Integer.class))._parseDouble("a");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_120() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="test123"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseDouble("test123");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_121() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Object.class, key="test123"
        try {
            (new StdKeyDeserializer(-1, Object.class))._parseDouble("test123");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_122() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Integer.class, key="test123"
        try {
            (new StdKeyDeserializer(1, Integer.class))._parseDouble("test123");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_123() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="!@#"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseDouble("!@#");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_124() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=Object.class, key="!@#"
        try {
            (new StdKeyDeserializer(0, Object.class))._parseDouble("!@#");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_125() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Integer.class, key="!@#"
        try {
            (new StdKeyDeserializer(1, Integer.class))._parseDouble("!@#");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_126() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="0"
        Object actual = (new StdKeyDeserializer(0, String.class))._parseDouble("0");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_127() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=Object.class, key="0"
        Object actual = (new StdKeyDeserializer(Integer.MIN_VALUE, Object.class))._parseDouble("0");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_128() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Integer.class, key="0"
        Object actual = (new StdKeyDeserializer(1, Integer.class))._parseDouble("0");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_129() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="-1"
        Object actual = (new StdKeyDeserializer(0, String.class))._parseDouble("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_130() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key="-1"
        Object actual = (new StdKeyDeserializer(1, Object.class))._parseDouble("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_131() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=Integer.class, key="-1"
        Object actual = (new StdKeyDeserializer(Integer.MIN_VALUE, Integer.class))._parseDouble("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_132() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="1.5"
        Object actual = (new StdKeyDeserializer(0, String.class))._parseDouble("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_133() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key="1.5"
        Object actual = (new StdKeyDeserializer(1, Object.class))._parseDouble("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_134() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Integer.class, key="1.5"
        Object actual = (new StdKeyDeserializer(-1, Integer.class))._parseDouble("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_135() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="9223372036854775807"
        Object actual = (new StdKeyDeserializer(0, String.class))._parseDouble("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_136() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key="9223372036854775807"
        Object actual = (new StdKeyDeserializer(1, Object.class))._parseDouble("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_137() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Integer.class, key="9223372036854775807"
        Object actual = (new StdKeyDeserializer(-1, Integer.class))._parseDouble("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_138() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="9223372036854775808"
        Object actual = (new StdKeyDeserializer(0, String.class))._parseDouble("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_139() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key="9223372036854775808"
        Object actual = (new StdKeyDeserializer(1, Object.class))._parseDouble("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_140() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Integer.class, key="9223372036854775808"
        Object actual = (new StdKeyDeserializer(-1, Integer.class))._parseDouble("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_141() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new StdKeyDeserializer(0, String.class))._parseDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_142() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=Object.class, key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new StdKeyDeserializer(1, Object.class))._parseDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_143() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=Integer.class, key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new StdKeyDeserializer(-1, Integer.class))._parseDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_144() throws Exception {
        // Combination: receiver__kind=0, receiver__cls=String.class, key="test123"
        try {
            (new StdKeyDeserializer(0, String.class))._parseDouble("test123");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_145() throws Exception {
        // Combination: receiver__kind=1, receiver__cls=String.class, key="a"
        try {
            (new StdKeyDeserializer(1, String.class))._parseDouble("a");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_146() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=String.class, key=" "
        try {
            (new StdKeyDeserializer(-1, String.class))._parseDouble(" ");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_147() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=String.class, key="!@#"
        try {
            (new StdKeyDeserializer(-1, String.class))._parseDouble("!@#");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_148() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=String.class, key="0"
        Object actual = (new StdKeyDeserializer(-1, String.class))._parseDouble("0");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_149() throws Exception {
        // Combination: receiver__kind=-1, receiver__cls=String.class, key="-1"
        Object actual = (new StdKeyDeserializer(-1, String.class))._parseDouble("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_150() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key=""
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseDouble("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_151() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="!@#"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseDouble("!@#");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_152() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="0"
        Object actual = (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseDouble("0");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_153() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="-1"
        Object actual = (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseDouble("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_154() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="1.5"
        Object actual = (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseDouble("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_155() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="9223372036854775807"
        Object actual = (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseDouble("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_156() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="9223372036854775808"
        Object actual = (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseDouble("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_157() throws Exception {
        // Combination: receiver__kind=Integer.MAX_VALUE, receiver__cls=String.class, key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new StdKeyDeserializer(Integer.MAX_VALUE, String.class))._parseDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_158() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key=""
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseDouble("");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_159() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key=" "
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseDouble(" ");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_160() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="a"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseDouble("a");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_161() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="test123"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseDouble("test123");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_162() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="1.5"
        Object actual = (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseDouble("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_163() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="9223372036854775807"
        Object actual = (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseDouble("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_164() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="9223372036854775808"
        Object actual = (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseDouble("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("9.223372036854776E18", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test__parseDouble_pairwise_165() throws Exception {
        // Combination: receiver__kind=Integer.MIN_VALUE, receiver__cls=String.class, key="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new StdKeyDeserializer(Integer.MIN_VALUE, String.class))._parseDouble("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.NumberFormatException");
        } catch (java.lang.NumberFormatException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
