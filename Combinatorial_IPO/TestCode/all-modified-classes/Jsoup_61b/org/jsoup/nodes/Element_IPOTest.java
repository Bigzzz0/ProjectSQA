package org.jsoup.nodes;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Element.
 */
public class Element_IPOTest {
    @Test(timeout = 4000)
    public void test_hasClass_pairwise_001() throws Exception {
        // Combination: receiver__tag="", className=""
        try {
            (new Element("")).hasClass("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_002() throws Exception {
        // Combination: receiver__tag=" ", className=""
        try {
            (new Element(" ")).hasClass("");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_003() throws Exception {
        // Combination: receiver__tag="a", className=""
        Object actual = (new Element("a")).hasClass("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_004() throws Exception {
        // Combination: receiver__tag="test123", className=""
        Object actual = (new Element("test123")).hasClass("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_005() throws Exception {
        // Combination: receiver__tag="!@#", className=""
        Object actual = (new Element("!@#")).hasClass("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_006() throws Exception {
        // Combination: receiver__tag="0", className=""
        Object actual = (new Element("0")).hasClass("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_007() throws Exception {
        // Combination: receiver__tag="-1", className=""
        Object actual = (new Element("-1")).hasClass("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_008() throws Exception {
        // Combination: receiver__tag="1.5", className=""
        Object actual = (new Element("1.5")).hasClass("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_009() throws Exception {
        // Combination: receiver__tag="9223372036854775807", className=""
        Object actual = (new Element("9223372036854775807")).hasClass("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_010() throws Exception {
        // Combination: receiver__tag="9223372036854775808", className=""
        Object actual = (new Element("9223372036854775808")).hasClass("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_011() throws Exception {
        // Combination: receiver__tag="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", className=""
        Object actual = (new Element("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hasClass("");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_012() throws Exception {
        // Combination: receiver__tag="", className=" "
        try {
            (new Element("")).hasClass(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_013() throws Exception {
        // Combination: receiver__tag=" ", className=" "
        try {
            (new Element(" ")).hasClass(" ");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_014() throws Exception {
        // Combination: receiver__tag="a", className=" "
        Object actual = (new Element("a")).hasClass(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_015() throws Exception {
        // Combination: receiver__tag="test123", className=" "
        Object actual = (new Element("test123")).hasClass(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_016() throws Exception {
        // Combination: receiver__tag="!@#", className=" "
        Object actual = (new Element("!@#")).hasClass(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_017() throws Exception {
        // Combination: receiver__tag="0", className=" "
        Object actual = (new Element("0")).hasClass(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_018() throws Exception {
        // Combination: receiver__tag="-1", className=" "
        Object actual = (new Element("-1")).hasClass(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_019() throws Exception {
        // Combination: receiver__tag="1.5", className=" "
        Object actual = (new Element("1.5")).hasClass(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_020() throws Exception {
        // Combination: receiver__tag="9223372036854775807", className=" "
        Object actual = (new Element("9223372036854775807")).hasClass(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_021() throws Exception {
        // Combination: receiver__tag="9223372036854775808", className=" "
        Object actual = (new Element("9223372036854775808")).hasClass(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_022() throws Exception {
        // Combination: receiver__tag="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", className=" "
        Object actual = (new Element("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hasClass(" ");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_023() throws Exception {
        // Combination: receiver__tag="", className="a"
        try {
            (new Element("")).hasClass("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_024() throws Exception {
        // Combination: receiver__tag=" ", className="a"
        try {
            (new Element(" ")).hasClass("a");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_025() throws Exception {
        // Combination: receiver__tag="a", className="a"
        Object actual = (new Element("a")).hasClass("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_026() throws Exception {
        // Combination: receiver__tag="test123", className="a"
        Object actual = (new Element("test123")).hasClass("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_027() throws Exception {
        // Combination: receiver__tag="!@#", className="a"
        Object actual = (new Element("!@#")).hasClass("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_028() throws Exception {
        // Combination: receiver__tag="0", className="a"
        Object actual = (new Element("0")).hasClass("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_029() throws Exception {
        // Combination: receiver__tag="-1", className="a"
        Object actual = (new Element("-1")).hasClass("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_030() throws Exception {
        // Combination: receiver__tag="1.5", className="a"
        Object actual = (new Element("1.5")).hasClass("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_031() throws Exception {
        // Combination: receiver__tag="9223372036854775807", className="a"
        Object actual = (new Element("9223372036854775807")).hasClass("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_032() throws Exception {
        // Combination: receiver__tag="9223372036854775808", className="a"
        Object actual = (new Element("9223372036854775808")).hasClass("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_033() throws Exception {
        // Combination: receiver__tag="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", className="a"
        Object actual = (new Element("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hasClass("a");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_034() throws Exception {
        // Combination: receiver__tag="", className="test123"
        try {
            (new Element("")).hasClass("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_035() throws Exception {
        // Combination: receiver__tag=" ", className="test123"
        try {
            (new Element(" ")).hasClass("test123");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_036() throws Exception {
        // Combination: receiver__tag="a", className="test123"
        Object actual = (new Element("a")).hasClass("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_037() throws Exception {
        // Combination: receiver__tag="test123", className="test123"
        Object actual = (new Element("test123")).hasClass("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_038() throws Exception {
        // Combination: receiver__tag="!@#", className="test123"
        Object actual = (new Element("!@#")).hasClass("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_039() throws Exception {
        // Combination: receiver__tag="0", className="test123"
        Object actual = (new Element("0")).hasClass("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_040() throws Exception {
        // Combination: receiver__tag="-1", className="test123"
        Object actual = (new Element("-1")).hasClass("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_041() throws Exception {
        // Combination: receiver__tag="1.5", className="test123"
        Object actual = (new Element("1.5")).hasClass("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_042() throws Exception {
        // Combination: receiver__tag="9223372036854775807", className="test123"
        Object actual = (new Element("9223372036854775807")).hasClass("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_043() throws Exception {
        // Combination: receiver__tag="9223372036854775808", className="test123"
        Object actual = (new Element("9223372036854775808")).hasClass("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_044() throws Exception {
        // Combination: receiver__tag="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", className="test123"
        Object actual = (new Element("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hasClass("test123");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_045() throws Exception {
        // Combination: receiver__tag="", className="!@#"
        try {
            (new Element("")).hasClass("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_046() throws Exception {
        // Combination: receiver__tag=" ", className="!@#"
        try {
            (new Element(" ")).hasClass("!@#");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_047() throws Exception {
        // Combination: receiver__tag="a", className="!@#"
        Object actual = (new Element("a")).hasClass("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_048() throws Exception {
        // Combination: receiver__tag="test123", className="!@#"
        Object actual = (new Element("test123")).hasClass("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_049() throws Exception {
        // Combination: receiver__tag="!@#", className="!@#"
        Object actual = (new Element("!@#")).hasClass("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_050() throws Exception {
        // Combination: receiver__tag="0", className="!@#"
        Object actual = (new Element("0")).hasClass("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_051() throws Exception {
        // Combination: receiver__tag="-1", className="!@#"
        Object actual = (new Element("-1")).hasClass("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_052() throws Exception {
        // Combination: receiver__tag="1.5", className="!@#"
        Object actual = (new Element("1.5")).hasClass("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_053() throws Exception {
        // Combination: receiver__tag="9223372036854775807", className="!@#"
        Object actual = (new Element("9223372036854775807")).hasClass("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_054() throws Exception {
        // Combination: receiver__tag="9223372036854775808", className="!@#"
        Object actual = (new Element("9223372036854775808")).hasClass("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_055() throws Exception {
        // Combination: receiver__tag="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", className="!@#"
        Object actual = (new Element("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hasClass("!@#");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_056() throws Exception {
        // Combination: receiver__tag="", className="0"
        try {
            (new Element("")).hasClass("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_057() throws Exception {
        // Combination: receiver__tag=" ", className="0"
        try {
            (new Element(" ")).hasClass("0");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_058() throws Exception {
        // Combination: receiver__tag="a", className="0"
        Object actual = (new Element("a")).hasClass("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_059() throws Exception {
        // Combination: receiver__tag="test123", className="0"
        Object actual = (new Element("test123")).hasClass("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_060() throws Exception {
        // Combination: receiver__tag="!@#", className="0"
        Object actual = (new Element("!@#")).hasClass("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_061() throws Exception {
        // Combination: receiver__tag="0", className="0"
        Object actual = (new Element("0")).hasClass("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_062() throws Exception {
        // Combination: receiver__tag="-1", className="0"
        Object actual = (new Element("-1")).hasClass("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_063() throws Exception {
        // Combination: receiver__tag="1.5", className="0"
        Object actual = (new Element("1.5")).hasClass("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_064() throws Exception {
        // Combination: receiver__tag="9223372036854775807", className="0"
        Object actual = (new Element("9223372036854775807")).hasClass("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_065() throws Exception {
        // Combination: receiver__tag="9223372036854775808", className="0"
        Object actual = (new Element("9223372036854775808")).hasClass("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_066() throws Exception {
        // Combination: receiver__tag="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", className="0"
        Object actual = (new Element("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hasClass("0");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_067() throws Exception {
        // Combination: receiver__tag="", className="-1"
        try {
            (new Element("")).hasClass("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_068() throws Exception {
        // Combination: receiver__tag=" ", className="-1"
        try {
            (new Element(" ")).hasClass("-1");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_069() throws Exception {
        // Combination: receiver__tag="a", className="-1"
        Object actual = (new Element("a")).hasClass("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_070() throws Exception {
        // Combination: receiver__tag="test123", className="-1"
        Object actual = (new Element("test123")).hasClass("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_071() throws Exception {
        // Combination: receiver__tag="!@#", className="-1"
        Object actual = (new Element("!@#")).hasClass("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_072() throws Exception {
        // Combination: receiver__tag="0", className="-1"
        Object actual = (new Element("0")).hasClass("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_073() throws Exception {
        // Combination: receiver__tag="-1", className="-1"
        Object actual = (new Element("-1")).hasClass("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_074() throws Exception {
        // Combination: receiver__tag="1.5", className="-1"
        Object actual = (new Element("1.5")).hasClass("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_075() throws Exception {
        // Combination: receiver__tag="9223372036854775807", className="-1"
        Object actual = (new Element("9223372036854775807")).hasClass("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_076() throws Exception {
        // Combination: receiver__tag="9223372036854775808", className="-1"
        Object actual = (new Element("9223372036854775808")).hasClass("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_077() throws Exception {
        // Combination: receiver__tag="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", className="-1"
        Object actual = (new Element("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hasClass("-1");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_078() throws Exception {
        // Combination: receiver__tag="", className="1.5"
        try {
            (new Element("")).hasClass("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_079() throws Exception {
        // Combination: receiver__tag=" ", className="1.5"
        try {
            (new Element(" ")).hasClass("1.5");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_080() throws Exception {
        // Combination: receiver__tag="a", className="1.5"
        Object actual = (new Element("a")).hasClass("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_081() throws Exception {
        // Combination: receiver__tag="test123", className="1.5"
        Object actual = (new Element("test123")).hasClass("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_082() throws Exception {
        // Combination: receiver__tag="!@#", className="1.5"
        Object actual = (new Element("!@#")).hasClass("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_083() throws Exception {
        // Combination: receiver__tag="0", className="1.5"
        Object actual = (new Element("0")).hasClass("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_084() throws Exception {
        // Combination: receiver__tag="-1", className="1.5"
        Object actual = (new Element("-1")).hasClass("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_085() throws Exception {
        // Combination: receiver__tag="1.5", className="1.5"
        Object actual = (new Element("1.5")).hasClass("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_086() throws Exception {
        // Combination: receiver__tag="9223372036854775807", className="1.5"
        Object actual = (new Element("9223372036854775807")).hasClass("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_087() throws Exception {
        // Combination: receiver__tag="9223372036854775808", className="1.5"
        Object actual = (new Element("9223372036854775808")).hasClass("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_088() throws Exception {
        // Combination: receiver__tag="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", className="1.5"
        Object actual = (new Element("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hasClass("1.5");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_089() throws Exception {
        // Combination: receiver__tag="", className="9223372036854775807"
        try {
            (new Element("")).hasClass("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_090() throws Exception {
        // Combination: receiver__tag=" ", className="9223372036854775807"
        try {
            (new Element(" ")).hasClass("9223372036854775807");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_091() throws Exception {
        // Combination: receiver__tag="a", className="9223372036854775807"
        Object actual = (new Element("a")).hasClass("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_092() throws Exception {
        // Combination: receiver__tag="test123", className="9223372036854775807"
        Object actual = (new Element("test123")).hasClass("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_093() throws Exception {
        // Combination: receiver__tag="!@#", className="9223372036854775807"
        Object actual = (new Element("!@#")).hasClass("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_094() throws Exception {
        // Combination: receiver__tag="0", className="9223372036854775807"
        Object actual = (new Element("0")).hasClass("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_095() throws Exception {
        // Combination: receiver__tag="-1", className="9223372036854775807"
        Object actual = (new Element("-1")).hasClass("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_096() throws Exception {
        // Combination: receiver__tag="1.5", className="9223372036854775807"
        Object actual = (new Element("1.5")).hasClass("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_097() throws Exception {
        // Combination: receiver__tag="9223372036854775807", className="9223372036854775807"
        Object actual = (new Element("9223372036854775807")).hasClass("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_098() throws Exception {
        // Combination: receiver__tag="9223372036854775808", className="9223372036854775807"
        Object actual = (new Element("9223372036854775808")).hasClass("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_099() throws Exception {
        // Combination: receiver__tag="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", className="9223372036854775807"
        Object actual = (new Element("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hasClass("9223372036854775807");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_100() throws Exception {
        // Combination: receiver__tag="", className="9223372036854775808"
        try {
            (new Element("")).hasClass("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_101() throws Exception {
        // Combination: receiver__tag=" ", className="9223372036854775808"
        try {
            (new Element(" ")).hasClass("9223372036854775808");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_102() throws Exception {
        // Combination: receiver__tag="a", className="9223372036854775808"
        Object actual = (new Element("a")).hasClass("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_103() throws Exception {
        // Combination: receiver__tag="test123", className="9223372036854775808"
        Object actual = (new Element("test123")).hasClass("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_104() throws Exception {
        // Combination: receiver__tag="!@#", className="9223372036854775808"
        Object actual = (new Element("!@#")).hasClass("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_105() throws Exception {
        // Combination: receiver__tag="0", className="9223372036854775808"
        Object actual = (new Element("0")).hasClass("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_106() throws Exception {
        // Combination: receiver__tag="-1", className="9223372036854775808"
        Object actual = (new Element("-1")).hasClass("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_107() throws Exception {
        // Combination: receiver__tag="1.5", className="9223372036854775808"
        Object actual = (new Element("1.5")).hasClass("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_108() throws Exception {
        // Combination: receiver__tag="9223372036854775807", className="9223372036854775808"
        Object actual = (new Element("9223372036854775807")).hasClass("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_109() throws Exception {
        // Combination: receiver__tag="9223372036854775808", className="9223372036854775808"
        Object actual = (new Element("9223372036854775808")).hasClass("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_110() throws Exception {
        // Combination: receiver__tag="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", className="9223372036854775808"
        Object actual = (new Element("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hasClass("9223372036854775808");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_111() throws Exception {
        // Combination: receiver__tag="", className="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Element("")).hasClass("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_112() throws Exception {
        // Combination: receiver__tag=" ", className="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        try {
            (new Element(" ")).hasClass("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_113() throws Exception {
        // Combination: receiver__tag="a", className="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Element("a")).hasClass("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_114() throws Exception {
        // Combination: receiver__tag="test123", className="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Element("test123")).hasClass("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_115() throws Exception {
        // Combination: receiver__tag="!@#", className="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Element("!@#")).hasClass("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_116() throws Exception {
        // Combination: receiver__tag="0", className="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Element("0")).hasClass("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_117() throws Exception {
        // Combination: receiver__tag="-1", className="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Element("-1")).hasClass("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_118() throws Exception {
        // Combination: receiver__tag="1.5", className="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Element("1.5")).hasClass("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_119() throws Exception {
        // Combination: receiver__tag="9223372036854775807", className="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Element("9223372036854775807")).hasClass("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_120() throws Exception {
        // Combination: receiver__tag="9223372036854775808", className="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Element("9223372036854775808")).hasClass("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_hasClass_pairwise_121() throws Exception {
        // Combination: receiver__tag="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", className="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
        Object actual = (new Element("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).hasClass("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

}
