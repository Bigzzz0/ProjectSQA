package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for SourceFile.
 */
public class SourceFile_IPOTest {
    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_001() throws Exception {
        // Combination: receiver__fileName="", lineno=0
        try {
            (new SourceFile("")).getLineOffset(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_002() throws Exception {
        // Combination: receiver__fileName=" ", lineno=0
        try {
            (new SourceFile(" ")).getLineOffset(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_003() throws Exception {
        // Combination: receiver__fileName="a", lineno=0
        try {
            (new SourceFile("a")).getLineOffset(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_004() throws Exception {
        // Combination: receiver__fileName="test123", lineno=0
        try {
            (new SourceFile("test123")).getLineOffset(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_005() throws Exception {
        // Combination: receiver__fileName="!@#", lineno=0
        try {
            (new SourceFile("!@#")).getLineOffset(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_006() throws Exception {
        // Combination: receiver__fileName="0", lineno=0
        try {
            (new SourceFile("0")).getLineOffset(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_007() throws Exception {
        // Combination: receiver__fileName="-1", lineno=0
        try {
            (new SourceFile("-1")).getLineOffset(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_008() throws Exception {
        // Combination: receiver__fileName="1.5", lineno=0
        try {
            (new SourceFile("1.5")).getLineOffset(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_009() throws Exception {
        // Combination: receiver__fileName="9223372036854775807", lineno=0
        try {
            (new SourceFile("9223372036854775807")).getLineOffset(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_010() throws Exception {
        // Combination: receiver__fileName="9223372036854775808", lineno=0
        try {
            (new SourceFile("9223372036854775808")).getLineOffset(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_011() throws Exception {
        // Combination: receiver__fileName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineno=0
        try {
            (new SourceFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getLineOffset(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_012() throws Exception {
        // Combination: receiver__fileName="", lineno=1
        try {
            (new SourceFile("")).getLineOffset(1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_013() throws Exception {
        // Combination: receiver__fileName=" ", lineno=1
        try {
            (new SourceFile(" ")).getLineOffset(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_014() throws Exception {
        // Combination: receiver__fileName="a", lineno=1
        try {
            (new SourceFile("a")).getLineOffset(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_015() throws Exception {
        // Combination: receiver__fileName="test123", lineno=1
        try {
            (new SourceFile("test123")).getLineOffset(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_016() throws Exception {
        // Combination: receiver__fileName="!@#", lineno=1
        try {
            (new SourceFile("!@#")).getLineOffset(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_017() throws Exception {
        // Combination: receiver__fileName="0", lineno=1
        try {
            (new SourceFile("0")).getLineOffset(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_018() throws Exception {
        // Combination: receiver__fileName="-1", lineno=1
        try {
            (new SourceFile("-1")).getLineOffset(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_019() throws Exception {
        // Combination: receiver__fileName="1.5", lineno=1
        try {
            (new SourceFile("1.5")).getLineOffset(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_020() throws Exception {
        // Combination: receiver__fileName="9223372036854775807", lineno=1
        try {
            (new SourceFile("9223372036854775807")).getLineOffset(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_021() throws Exception {
        // Combination: receiver__fileName="9223372036854775808", lineno=1
        try {
            (new SourceFile("9223372036854775808")).getLineOffset(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_022() throws Exception {
        // Combination: receiver__fileName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineno=1
        try {
            (new SourceFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getLineOffset(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_023() throws Exception {
        // Combination: receiver__fileName="", lineno=-1
        try {
            (new SourceFile("")).getLineOffset(-1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_024() throws Exception {
        // Combination: receiver__fileName=" ", lineno=-1
        try {
            (new SourceFile(" ")).getLineOffset(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_025() throws Exception {
        // Combination: receiver__fileName="a", lineno=-1
        try {
            (new SourceFile("a")).getLineOffset(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_026() throws Exception {
        // Combination: receiver__fileName="test123", lineno=-1
        try {
            (new SourceFile("test123")).getLineOffset(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_027() throws Exception {
        // Combination: receiver__fileName="!@#", lineno=-1
        try {
            (new SourceFile("!@#")).getLineOffset(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_028() throws Exception {
        // Combination: receiver__fileName="0", lineno=-1
        try {
            (new SourceFile("0")).getLineOffset(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_029() throws Exception {
        // Combination: receiver__fileName="-1", lineno=-1
        try {
            (new SourceFile("-1")).getLineOffset(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_030() throws Exception {
        // Combination: receiver__fileName="1.5", lineno=-1
        try {
            (new SourceFile("1.5")).getLineOffset(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_031() throws Exception {
        // Combination: receiver__fileName="9223372036854775807", lineno=-1
        try {
            (new SourceFile("9223372036854775807")).getLineOffset(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_032() throws Exception {
        // Combination: receiver__fileName="9223372036854775808", lineno=-1
        try {
            (new SourceFile("9223372036854775808")).getLineOffset(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_033() throws Exception {
        // Combination: receiver__fileName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineno=-1
        try {
            (new SourceFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getLineOffset(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_034() throws Exception {
        // Combination: receiver__fileName="", lineno=Integer.MAX_VALUE
        try {
            (new SourceFile("")).getLineOffset(Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_035() throws Exception {
        // Combination: receiver__fileName=" ", lineno=Integer.MAX_VALUE
        try {
            (new SourceFile(" ")).getLineOffset(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_036() throws Exception {
        // Combination: receiver__fileName="a", lineno=Integer.MAX_VALUE
        try {
            (new SourceFile("a")).getLineOffset(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_037() throws Exception {
        // Combination: receiver__fileName="test123", lineno=Integer.MAX_VALUE
        try {
            (new SourceFile("test123")).getLineOffset(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_038() throws Exception {
        // Combination: receiver__fileName="!@#", lineno=Integer.MAX_VALUE
        try {
            (new SourceFile("!@#")).getLineOffset(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_039() throws Exception {
        // Combination: receiver__fileName="0", lineno=Integer.MAX_VALUE
        try {
            (new SourceFile("0")).getLineOffset(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_040() throws Exception {
        // Combination: receiver__fileName="-1", lineno=Integer.MAX_VALUE
        try {
            (new SourceFile("-1")).getLineOffset(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_041() throws Exception {
        // Combination: receiver__fileName="1.5", lineno=Integer.MAX_VALUE
        try {
            (new SourceFile("1.5")).getLineOffset(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_042() throws Exception {
        // Combination: receiver__fileName="9223372036854775807", lineno=Integer.MAX_VALUE
        try {
            (new SourceFile("9223372036854775807")).getLineOffset(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_043() throws Exception {
        // Combination: receiver__fileName="9223372036854775808", lineno=Integer.MAX_VALUE
        try {
            (new SourceFile("9223372036854775808")).getLineOffset(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_044() throws Exception {
        // Combination: receiver__fileName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineno=Integer.MAX_VALUE
        try {
            (new SourceFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getLineOffset(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_045() throws Exception {
        // Combination: receiver__fileName="", lineno=Integer.MIN_VALUE
        try {
            (new SourceFile("")).getLineOffset(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_046() throws Exception {
        // Combination: receiver__fileName=" ", lineno=Integer.MIN_VALUE
        try {
            (new SourceFile(" ")).getLineOffset(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_047() throws Exception {
        // Combination: receiver__fileName="a", lineno=Integer.MIN_VALUE
        try {
            (new SourceFile("a")).getLineOffset(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_048() throws Exception {
        // Combination: receiver__fileName="test123", lineno=Integer.MIN_VALUE
        try {
            (new SourceFile("test123")).getLineOffset(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_049() throws Exception {
        // Combination: receiver__fileName="!@#", lineno=Integer.MIN_VALUE
        try {
            (new SourceFile("!@#")).getLineOffset(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_050() throws Exception {
        // Combination: receiver__fileName="0", lineno=Integer.MIN_VALUE
        try {
            (new SourceFile("0")).getLineOffset(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_051() throws Exception {
        // Combination: receiver__fileName="-1", lineno=Integer.MIN_VALUE
        try {
            (new SourceFile("-1")).getLineOffset(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_052() throws Exception {
        // Combination: receiver__fileName="1.5", lineno=Integer.MIN_VALUE
        try {
            (new SourceFile("1.5")).getLineOffset(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_053() throws Exception {
        // Combination: receiver__fileName="9223372036854775807", lineno=Integer.MIN_VALUE
        try {
            (new SourceFile("9223372036854775807")).getLineOffset(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_054() throws Exception {
        // Combination: receiver__fileName="9223372036854775808", lineno=Integer.MIN_VALUE
        try {
            (new SourceFile("9223372036854775808")).getLineOffset(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLineOffset_pairwise_055() throws Exception {
        // Combination: receiver__fileName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineno=Integer.MIN_VALUE
        try {
            (new SourceFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getLineOffset(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_056() throws Exception {
        // Combination: receiver__fileName="", lineNumber=0
        try {
            (new SourceFile("")).getLine(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_057() throws Exception {
        // Combination: receiver__fileName=" ", lineNumber=0
        try {
            (new SourceFile(" ")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_058() throws Exception {
        // Combination: receiver__fileName="a", lineNumber=0
        try {
            (new SourceFile("a")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_059() throws Exception {
        // Combination: receiver__fileName="test123", lineNumber=0
        try {
            (new SourceFile("test123")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_060() throws Exception {
        // Combination: receiver__fileName="!@#", lineNumber=0
        try {
            (new SourceFile("!@#")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_061() throws Exception {
        // Combination: receiver__fileName="0", lineNumber=0
        try {
            (new SourceFile("0")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_062() throws Exception {
        // Combination: receiver__fileName="-1", lineNumber=0
        try {
            (new SourceFile("-1")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_063() throws Exception {
        // Combination: receiver__fileName="1.5", lineNumber=0
        try {
            (new SourceFile("1.5")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_064() throws Exception {
        // Combination: receiver__fileName="9223372036854775807", lineNumber=0
        try {
            (new SourceFile("9223372036854775807")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_065() throws Exception {
        // Combination: receiver__fileName="9223372036854775808", lineNumber=0
        try {
            (new SourceFile("9223372036854775808")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_066() throws Exception {
        // Combination: receiver__fileName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineNumber=0
        try {
            (new SourceFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_067() throws Exception {
        // Combination: receiver__fileName="", lineNumber=1
        try {
            (new SourceFile("")).getLine(1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_068() throws Exception {
        // Combination: receiver__fileName=" ", lineNumber=1
        try {
            (new SourceFile(" ")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_069() throws Exception {
        // Combination: receiver__fileName="a", lineNumber=1
        try {
            (new SourceFile("a")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_070() throws Exception {
        // Combination: receiver__fileName="test123", lineNumber=1
        try {
            (new SourceFile("test123")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_071() throws Exception {
        // Combination: receiver__fileName="!@#", lineNumber=1
        try {
            (new SourceFile("!@#")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_072() throws Exception {
        // Combination: receiver__fileName="0", lineNumber=1
        try {
            (new SourceFile("0")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_073() throws Exception {
        // Combination: receiver__fileName="-1", lineNumber=1
        try {
            (new SourceFile("-1")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_074() throws Exception {
        // Combination: receiver__fileName="1.5", lineNumber=1
        try {
            (new SourceFile("1.5")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_075() throws Exception {
        // Combination: receiver__fileName="9223372036854775807", lineNumber=1
        try {
            (new SourceFile("9223372036854775807")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_076() throws Exception {
        // Combination: receiver__fileName="9223372036854775808", lineNumber=1
        try {
            (new SourceFile("9223372036854775808")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_077() throws Exception {
        // Combination: receiver__fileName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineNumber=1
        try {
            (new SourceFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_078() throws Exception {
        // Combination: receiver__fileName="", lineNumber=-1
        try {
            (new SourceFile("")).getLine(-1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_079() throws Exception {
        // Combination: receiver__fileName=" ", lineNumber=-1
        try {
            (new SourceFile(" ")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_080() throws Exception {
        // Combination: receiver__fileName="a", lineNumber=-1
        try {
            (new SourceFile("a")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_081() throws Exception {
        // Combination: receiver__fileName="test123", lineNumber=-1
        try {
            (new SourceFile("test123")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_082() throws Exception {
        // Combination: receiver__fileName="!@#", lineNumber=-1
        try {
            (new SourceFile("!@#")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_083() throws Exception {
        // Combination: receiver__fileName="0", lineNumber=-1
        try {
            (new SourceFile("0")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_084() throws Exception {
        // Combination: receiver__fileName="-1", lineNumber=-1
        try {
            (new SourceFile("-1")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_085() throws Exception {
        // Combination: receiver__fileName="1.5", lineNumber=-1
        try {
            (new SourceFile("1.5")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_086() throws Exception {
        // Combination: receiver__fileName="9223372036854775807", lineNumber=-1
        try {
            (new SourceFile("9223372036854775807")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_087() throws Exception {
        // Combination: receiver__fileName="9223372036854775808", lineNumber=-1
        try {
            (new SourceFile("9223372036854775808")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_088() throws Exception {
        // Combination: receiver__fileName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineNumber=-1
        try {
            (new SourceFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_089() throws Exception {
        // Combination: receiver__fileName="", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_090() throws Exception {
        // Combination: receiver__fileName=" ", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile(" ")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_091() throws Exception {
        // Combination: receiver__fileName="a", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("a")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_092() throws Exception {
        // Combination: receiver__fileName="test123", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("test123")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_093() throws Exception {
        // Combination: receiver__fileName="!@#", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("!@#")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_094() throws Exception {
        // Combination: receiver__fileName="0", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("0")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_095() throws Exception {
        // Combination: receiver__fileName="-1", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("-1")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_096() throws Exception {
        // Combination: receiver__fileName="1.5", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("1.5")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_097() throws Exception {
        // Combination: receiver__fileName="9223372036854775807", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("9223372036854775807")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_098() throws Exception {
        // Combination: receiver__fileName="9223372036854775808", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("9223372036854775808")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_099() throws Exception {
        // Combination: receiver__fileName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_100() throws Exception {
        // Combination: receiver__fileName="", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_101() throws Exception {
        // Combination: receiver__fileName=" ", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile(" ")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_102() throws Exception {
        // Combination: receiver__fileName="a", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("a")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_103() throws Exception {
        // Combination: receiver__fileName="test123", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("test123")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_104() throws Exception {
        // Combination: receiver__fileName="!@#", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("!@#")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_105() throws Exception {
        // Combination: receiver__fileName="0", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("0")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_106() throws Exception {
        // Combination: receiver__fileName="-1", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("-1")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_107() throws Exception {
        // Combination: receiver__fileName="1.5", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("1.5")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_108() throws Exception {
        // Combination: receiver__fileName="9223372036854775807", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("9223372036854775807")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_109() throws Exception {
        // Combination: receiver__fileName="9223372036854775808", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("9223372036854775808")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_110() throws Exception {
        // Combination: receiver__fileName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
