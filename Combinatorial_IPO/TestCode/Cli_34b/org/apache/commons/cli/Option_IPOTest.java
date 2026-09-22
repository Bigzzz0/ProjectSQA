package org.apache.commons.cli;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for Option.
 */
public class Option_IPOTest {
    @Test(timeout = 4000)
    public void test_getValue_pairwise_001() throws Exception {
        // Combination: receiver__opt="", receiver__description="", index=0
        assertNull((new Option("", "")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_002() throws Exception {
        // Combination: receiver__opt=" ", receiver__description=" ", index=0
        try {
            (new Option(" ", " ")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_003() throws Exception {
        // Combination: receiver__opt="a", receiver__description="a", index=0
        assertNull((new Option("a", "a")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_004() throws Exception {
        // Combination: receiver__opt="test123", receiver__description="test123", index=0
        assertNull((new Option("test123", "test123")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_005() throws Exception {
        // Combination: receiver__opt="!@#", receiver__description="!@#", index=0
        try {
            (new Option("!@#", "!@#")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_006() throws Exception {
        // Combination: receiver__opt="0", receiver__description="0", index=0
        assertNull((new Option("0", "0")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_007() throws Exception {
        // Combination: receiver__opt="-1", receiver__description="-1", index=0
        try {
            (new Option("-1", "-1")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_008() throws Exception {
        // Combination: receiver__opt="1.5", receiver__description="1.5", index=0
        try {
            (new Option("1.5", "1.5")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_009() throws Exception {
        // Combination: receiver__opt="9223372036854775807", receiver__description="9223372036854775807", index=0
        assertNull((new Option("9223372036854775807", "9223372036854775807")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_010() throws Exception {
        // Combination: receiver__opt="9223372036854775808", receiver__description="9223372036854775808", index=0
        assertNull((new Option("9223372036854775808", "9223372036854775808")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_011() throws Exception {
        // Combination: receiver__opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", index=0
        assertNull((new Option("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_012() throws Exception {
        // Combination: receiver__opt=" ", receiver__description="", index=1
        try {
            (new Option(" ", "")).getValue(1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_013() throws Exception {
        // Combination: receiver__opt="", receiver__description=" ", index=1
        assertNull((new Option("", " ")).getValue(1));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_014() throws Exception {
        // Combination: receiver__opt="test123", receiver__description="a", index=1
        assertNull((new Option("test123", "a")).getValue(1));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_015() throws Exception {
        // Combination: receiver__opt="a", receiver__description="test123", index=1
        assertNull((new Option("a", "test123")).getValue(1));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_016() throws Exception {
        // Combination: receiver__opt="0", receiver__description="!@#", index=1
        assertNull((new Option("0", "!@#")).getValue(1));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_017() throws Exception {
        // Combination: receiver__opt="!@#", receiver__description="0", index=1
        try {
            (new Option("!@#", "0")).getValue(1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_018() throws Exception {
        // Combination: receiver__opt="1.5", receiver__description="-1", index=1
        try {
            (new Option("1.5", "-1")).getValue(1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_019() throws Exception {
        // Combination: receiver__opt="-1", receiver__description="1.5", index=1
        try {
            (new Option("-1", "1.5")).getValue(1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_020() throws Exception {
        // Combination: receiver__opt="9223372036854775808", receiver__description="9223372036854775807", index=1
        assertNull((new Option("9223372036854775808", "9223372036854775807")).getValue(1));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_021() throws Exception {
        // Combination: receiver__opt="9223372036854775807", receiver__description="9223372036854775808", index=1
        assertNull((new Option("9223372036854775807", "9223372036854775808")).getValue(1));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_022() throws Exception {
        // Combination: receiver__opt="", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", index=1
        assertNull((new Option("", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue(1));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_023() throws Exception {
        // Combination: receiver__opt="a", receiver__description="", index=-1
        assertNull((new Option("a", "")).getValue(-1));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_024() throws Exception {
        // Combination: receiver__opt="test123", receiver__description=" ", index=-1
        assertNull((new Option("test123", " ")).getValue(-1));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_025() throws Exception {
        // Combination: receiver__opt="", receiver__description="a", index=-1
        assertNull((new Option("", "a")).getValue(-1));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_026() throws Exception {
        // Combination: receiver__opt=" ", receiver__description="test123", index=-1
        try {
            (new Option(" ", "test123")).getValue(-1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_027() throws Exception {
        // Combination: receiver__opt="-1", receiver__description="!@#", index=-1
        try {
            (new Option("-1", "!@#")).getValue(-1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_028() throws Exception {
        // Combination: receiver__opt="1.5", receiver__description="0", index=-1
        try {
            (new Option("1.5", "0")).getValue(-1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_029() throws Exception {
        // Combination: receiver__opt="!@#", receiver__description="-1", index=-1
        try {
            (new Option("!@#", "-1")).getValue(-1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_030() throws Exception {
        // Combination: receiver__opt="0", receiver__description="1.5", index=-1
        assertNull((new Option("0", "1.5")).getValue(-1));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_031() throws Exception {
        // Combination: receiver__opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775807", index=-1
        assertNull((new Option("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775807")).getValue(-1));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_032() throws Exception {
        // Combination: receiver__opt="", receiver__description="9223372036854775808", index=-1
        assertNull((new Option("", "9223372036854775808")).getValue(-1));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_033() throws Exception {
        // Combination: receiver__opt="9223372036854775807", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", index=-1
        assertNull((new Option("9223372036854775807", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue(-1));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_034() throws Exception {
        // Combination: receiver__opt="test123", receiver__description="", index=Integer.MAX_VALUE
        assertNull((new Option("test123", "")).getValue(Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_035() throws Exception {
        // Combination: receiver__opt="a", receiver__description=" ", index=Integer.MAX_VALUE
        assertNull((new Option("a", " ")).getValue(Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_036() throws Exception {
        // Combination: receiver__opt=" ", receiver__description="a", index=Integer.MAX_VALUE
        try {
            (new Option(" ", "a")).getValue(Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_037() throws Exception {
        // Combination: receiver__opt="", receiver__description="test123", index=Integer.MAX_VALUE
        assertNull((new Option("", "test123")).getValue(Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_038() throws Exception {
        // Combination: receiver__opt="1.5", receiver__description="!@#", index=Integer.MAX_VALUE
        try {
            (new Option("1.5", "!@#")).getValue(Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_039() throws Exception {
        // Combination: receiver__opt="-1", receiver__description="0", index=Integer.MAX_VALUE
        try {
            (new Option("-1", "0")).getValue(Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_040() throws Exception {
        // Combination: receiver__opt="0", receiver__description="-1", index=Integer.MAX_VALUE
        assertNull((new Option("0", "-1")).getValue(Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_041() throws Exception {
        // Combination: receiver__opt="!@#", receiver__description="1.5", index=Integer.MAX_VALUE
        try {
            (new Option("!@#", "1.5")).getValue(Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_042() throws Exception {
        // Combination: receiver__opt="", receiver__description="9223372036854775807", index=Integer.MAX_VALUE
        assertNull((new Option("", "9223372036854775807")).getValue(Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_043() throws Exception {
        // Combination: receiver__opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="9223372036854775808", index=Integer.MAX_VALUE
        assertNull((new Option("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "9223372036854775808")).getValue(Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_044() throws Exception {
        // Combination: receiver__opt="9223372036854775808", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", index=Integer.MAX_VALUE
        assertNull((new Option("9223372036854775808", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue(Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_045() throws Exception {
        // Combination: receiver__opt="!@#", receiver__description="", index=Integer.MIN_VALUE
        try {
            (new Option("!@#", "")).getValue(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_046() throws Exception {
        // Combination: receiver__opt="0", receiver__description=" ", index=Integer.MIN_VALUE
        assertNull((new Option("0", " ")).getValue(Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_047() throws Exception {
        // Combination: receiver__opt="-1", receiver__description="a", index=Integer.MIN_VALUE
        try {
            (new Option("-1", "a")).getValue(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_048() throws Exception {
        // Combination: receiver__opt="1.5", receiver__description="test123", index=Integer.MIN_VALUE
        try {
            (new Option("1.5", "test123")).getValue(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_049() throws Exception {
        // Combination: receiver__opt="", receiver__description="!@#", index=Integer.MIN_VALUE
        assertNull((new Option("", "!@#")).getValue(Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_050() throws Exception {
        // Combination: receiver__opt=" ", receiver__description="0", index=Integer.MIN_VALUE
        try {
            (new Option(" ", "0")).getValue(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_051() throws Exception {
        // Combination: receiver__opt="a", receiver__description="-1", index=Integer.MIN_VALUE
        assertNull((new Option("a", "-1")).getValue(Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_052() throws Exception {
        // Combination: receiver__opt="test123", receiver__description="1.5", index=Integer.MIN_VALUE
        assertNull((new Option("test123", "1.5")).getValue(Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_053() throws Exception {
        // Combination: receiver__opt=" ", receiver__description="9223372036854775807", index=Integer.MIN_VALUE
        try {
            (new Option(" ", "9223372036854775807")).getValue(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_054() throws Exception {
        // Combination: receiver__opt=" ", receiver__description="9223372036854775808", index=Integer.MIN_VALUE
        try {
            (new Option(" ", "9223372036854775808")).getValue(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_055() throws Exception {
        // Combination: receiver__opt=" ", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", index=Integer.MIN_VALUE
        try {
            (new Option(" ", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_056() throws Exception {
        // Combination: receiver__opt="", receiver__description="0", index=0
        assertNull((new Option("", "0")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_057() throws Exception {
        // Combination: receiver__opt="", receiver__description="-1", index=0
        assertNull((new Option("", "-1")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_058() throws Exception {
        // Combination: receiver__opt="", receiver__description="1.5", index=0
        assertNull((new Option("", "1.5")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_059() throws Exception {
        // Combination: receiver__opt=" ", receiver__description="!@#", index=0
        try {
            (new Option(" ", "!@#")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_060() throws Exception {
        // Combination: receiver__opt=" ", receiver__description="-1", index=0
        try {
            (new Option(" ", "-1")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_061() throws Exception {
        // Combination: receiver__opt=" ", receiver__description="1.5", index=0
        try {
            (new Option(" ", "1.5")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_062() throws Exception {
        // Combination: receiver__opt="a", receiver__description="!@#", index=0
        assertNull((new Option("a", "!@#")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_063() throws Exception {
        // Combination: receiver__opt="a", receiver__description="0", index=0
        assertNull((new Option("a", "0")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_064() throws Exception {
        // Combination: receiver__opt="a", receiver__description="1.5", index=0
        assertNull((new Option("a", "1.5")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_065() throws Exception {
        // Combination: receiver__opt="a", receiver__description="9223372036854775807", index=0
        assertNull((new Option("a", "9223372036854775807")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_066() throws Exception {
        // Combination: receiver__opt="a", receiver__description="9223372036854775808", index=0
        assertNull((new Option("a", "9223372036854775808")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_067() throws Exception {
        // Combination: receiver__opt="a", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", index=0
        assertNull((new Option("a", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_068() throws Exception {
        // Combination: receiver__opt="test123", receiver__description="!@#", index=0
        assertNull((new Option("test123", "!@#")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_069() throws Exception {
        // Combination: receiver__opt="test123", receiver__description="0", index=0
        assertNull((new Option("test123", "0")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_070() throws Exception {
        // Combination: receiver__opt="test123", receiver__description="-1", index=0
        assertNull((new Option("test123", "-1")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_071() throws Exception {
        // Combination: receiver__opt="test123", receiver__description="9223372036854775807", index=0
        assertNull((new Option("test123", "9223372036854775807")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_072() throws Exception {
        // Combination: receiver__opt="test123", receiver__description="9223372036854775808", index=0
        assertNull((new Option("test123", "9223372036854775808")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_073() throws Exception {
        // Combination: receiver__opt="test123", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", index=0
        assertNull((new Option("test123", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_074() throws Exception {
        // Combination: receiver__opt="!@#", receiver__description=" ", index=0
        try {
            (new Option("!@#", " ")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_075() throws Exception {
        // Combination: receiver__opt="!@#", receiver__description="a", index=0
        try {
            (new Option("!@#", "a")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_076() throws Exception {
        // Combination: receiver__opt="!@#", receiver__description="test123", index=0
        try {
            (new Option("!@#", "test123")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_077() throws Exception {
        // Combination: receiver__opt="!@#", receiver__description="9223372036854775807", index=0
        try {
            (new Option("!@#", "9223372036854775807")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_078() throws Exception {
        // Combination: receiver__opt="!@#", receiver__description="9223372036854775808", index=0
        try {
            (new Option("!@#", "9223372036854775808")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_079() throws Exception {
        // Combination: receiver__opt="!@#", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", index=0
        try {
            (new Option("!@#", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_080() throws Exception {
        // Combination: receiver__opt="0", receiver__description="", index=0
        assertNull((new Option("0", "")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_081() throws Exception {
        // Combination: receiver__opt="0", receiver__description="a", index=0
        assertNull((new Option("0", "a")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_082() throws Exception {
        // Combination: receiver__opt="0", receiver__description="test123", index=0
        assertNull((new Option("0", "test123")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_083() throws Exception {
        // Combination: receiver__opt="0", receiver__description="9223372036854775807", index=0
        assertNull((new Option("0", "9223372036854775807")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_084() throws Exception {
        // Combination: receiver__opt="0", receiver__description="9223372036854775808", index=0
        assertNull((new Option("0", "9223372036854775808")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_085() throws Exception {
        // Combination: receiver__opt="0", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", index=0
        assertNull((new Option("0", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_086() throws Exception {
        // Combination: receiver__opt="-1", receiver__description="", index=0
        try {
            (new Option("-1", "")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_087() throws Exception {
        // Combination: receiver__opt="-1", receiver__description=" ", index=0
        try {
            (new Option("-1", " ")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_088() throws Exception {
        // Combination: receiver__opt="-1", receiver__description="test123", index=0
        try {
            (new Option("-1", "test123")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_089() throws Exception {
        // Combination: receiver__opt="-1", receiver__description="9223372036854775807", index=0
        try {
            (new Option("-1", "9223372036854775807")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_090() throws Exception {
        // Combination: receiver__opt="-1", receiver__description="9223372036854775808", index=0
        try {
            (new Option("-1", "9223372036854775808")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_091() throws Exception {
        // Combination: receiver__opt="-1", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", index=0
        try {
            (new Option("-1", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_092() throws Exception {
        // Combination: receiver__opt="1.5", receiver__description="", index=0
        try {
            (new Option("1.5", "")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_093() throws Exception {
        // Combination: receiver__opt="1.5", receiver__description=" ", index=0
        try {
            (new Option("1.5", " ")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_094() throws Exception {
        // Combination: receiver__opt="1.5", receiver__description="a", index=0
        try {
            (new Option("1.5", "a")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_095() throws Exception {
        // Combination: receiver__opt="1.5", receiver__description="9223372036854775807", index=0
        try {
            (new Option("1.5", "9223372036854775807")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_096() throws Exception {
        // Combination: receiver__opt="1.5", receiver__description="9223372036854775808", index=0
        try {
            (new Option("1.5", "9223372036854775808")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_097() throws Exception {
        // Combination: receiver__opt="1.5", receiver__description="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", index=0
        try {
            (new Option("1.5", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getValue(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_098() throws Exception {
        // Combination: receiver__opt="9223372036854775807", receiver__description="", index=Integer.MAX_VALUE
        assertNull((new Option("9223372036854775807", "")).getValue(Integer.MAX_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_099() throws Exception {
        // Combination: receiver__opt="9223372036854775807", receiver__description=" ", index=Integer.MIN_VALUE
        assertNull((new Option("9223372036854775807", " ")).getValue(Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_100() throws Exception {
        // Combination: receiver__opt="9223372036854775807", receiver__description="a", index=0
        assertNull((new Option("9223372036854775807", "a")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_101() throws Exception {
        // Combination: receiver__opt="9223372036854775807", receiver__description="test123", index=0
        assertNull((new Option("9223372036854775807", "test123")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_102() throws Exception {
        // Combination: receiver__opt="9223372036854775807", receiver__description="!@#", index=0
        assertNull((new Option("9223372036854775807", "!@#")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_103() throws Exception {
        // Combination: receiver__opt="9223372036854775807", receiver__description="0", index=0
        assertNull((new Option("9223372036854775807", "0")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_104() throws Exception {
        // Combination: receiver__opt="9223372036854775807", receiver__description="-1", index=0
        assertNull((new Option("9223372036854775807", "-1")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_105() throws Exception {
        // Combination: receiver__opt="9223372036854775807", receiver__description="1.5", index=0
        assertNull((new Option("9223372036854775807", "1.5")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_106() throws Exception {
        // Combination: receiver__opt="9223372036854775808", receiver__description="", index=-1
        assertNull((new Option("9223372036854775808", "")).getValue(-1));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_107() throws Exception {
        // Combination: receiver__opt="9223372036854775808", receiver__description=" ", index=Integer.MIN_VALUE
        assertNull((new Option("9223372036854775808", " ")).getValue(Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_108() throws Exception {
        // Combination: receiver__opt="9223372036854775808", receiver__description="a", index=0
        assertNull((new Option("9223372036854775808", "a")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_109() throws Exception {
        // Combination: receiver__opt="9223372036854775808", receiver__description="test123", index=0
        assertNull((new Option("9223372036854775808", "test123")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_110() throws Exception {
        // Combination: receiver__opt="9223372036854775808", receiver__description="!@#", index=0
        assertNull((new Option("9223372036854775808", "!@#")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_111() throws Exception {
        // Combination: receiver__opt="9223372036854775808", receiver__description="0", index=0
        assertNull((new Option("9223372036854775808", "0")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_112() throws Exception {
        // Combination: receiver__opt="9223372036854775808", receiver__description="-1", index=0
        assertNull((new Option("9223372036854775808", "-1")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_113() throws Exception {
        // Combination: receiver__opt="9223372036854775808", receiver__description="1.5", index=0
        assertNull((new Option("9223372036854775808", "1.5")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_114() throws Exception {
        // Combination: receiver__opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="", index=1
        assertNull((new Option("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "")).getValue(1));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_115() throws Exception {
        // Combination: receiver__opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description=" ", index=Integer.MIN_VALUE
        assertNull((new Option("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", " ")).getValue(Integer.MIN_VALUE));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_116() throws Exception {
        // Combination: receiver__opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="a", index=0
        assertNull((new Option("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "a")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_117() throws Exception {
        // Combination: receiver__opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="test123", index=0
        assertNull((new Option("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "test123")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_118() throws Exception {
        // Combination: receiver__opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="!@#", index=0
        assertNull((new Option("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "!@#")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_119() throws Exception {
        // Combination: receiver__opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="0", index=0
        assertNull((new Option("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_120() throws Exception {
        // Combination: receiver__opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="-1", index=0
        assertNull((new Option("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "-1")).getValue(0));
    }

    @Test(timeout = 4000)
    public void test_getValue_pairwise_121() throws Exception {
        // Combination: receiver__opt="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", receiver__description="1.5", index=0
        assertNull((new Option("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "1.5")).getValue(0));
    }

}
