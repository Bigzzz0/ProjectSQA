package com.google.javascript.jscomp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for SourceFile.
 */
public class SourceFile_IPOTest {
    @Test(timeout = 4000)
    public void test_getLine_pairwise_001() throws Exception {
        // Combination: receiver__fileName="", lineNumber=0
        try {
            (new SourceFile("")).getLine(0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_002() throws Exception {
        // Combination: receiver__fileName=" ", lineNumber=0
        try {
            (new SourceFile(" ")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_003() throws Exception {
        // Combination: receiver__fileName="a", lineNumber=0
        try {
            (new SourceFile("a")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_004() throws Exception {
        // Combination: receiver__fileName="test123", lineNumber=0
        try {
            (new SourceFile("test123")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_005() throws Exception {
        // Combination: receiver__fileName="!@#", lineNumber=0
        try {
            (new SourceFile("!@#")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_006() throws Exception {
        // Combination: receiver__fileName="0", lineNumber=0
        try {
            (new SourceFile("0")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_007() throws Exception {
        // Combination: receiver__fileName="-1", lineNumber=0
        try {
            (new SourceFile("-1")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_008() throws Exception {
        // Combination: receiver__fileName="1.5", lineNumber=0
        try {
            (new SourceFile("1.5")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_009() throws Exception {
        // Combination: receiver__fileName="9223372036854775807", lineNumber=0
        try {
            (new SourceFile("9223372036854775807")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_010() throws Exception {
        // Combination: receiver__fileName="9223372036854775808", lineNumber=0
        try {
            (new SourceFile("9223372036854775808")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_011() throws Exception {
        // Combination: receiver__fileName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineNumber=0
        try {
            (new SourceFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getLine(0);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_012() throws Exception {
        // Combination: receiver__fileName="", lineNumber=1
        try {
            (new SourceFile("")).getLine(1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_013() throws Exception {
        // Combination: receiver__fileName=" ", lineNumber=1
        try {
            (new SourceFile(" ")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_014() throws Exception {
        // Combination: receiver__fileName="a", lineNumber=1
        try {
            (new SourceFile("a")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_015() throws Exception {
        // Combination: receiver__fileName="test123", lineNumber=1
        try {
            (new SourceFile("test123")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_016() throws Exception {
        // Combination: receiver__fileName="!@#", lineNumber=1
        try {
            (new SourceFile("!@#")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_017() throws Exception {
        // Combination: receiver__fileName="0", lineNumber=1
        try {
            (new SourceFile("0")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_018() throws Exception {
        // Combination: receiver__fileName="-1", lineNumber=1
        try {
            (new SourceFile("-1")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_019() throws Exception {
        // Combination: receiver__fileName="1.5", lineNumber=1
        try {
            (new SourceFile("1.5")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_020() throws Exception {
        // Combination: receiver__fileName="9223372036854775807", lineNumber=1
        try {
            (new SourceFile("9223372036854775807")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_021() throws Exception {
        // Combination: receiver__fileName="9223372036854775808", lineNumber=1
        try {
            (new SourceFile("9223372036854775808")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_022() throws Exception {
        // Combination: receiver__fileName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineNumber=1
        try {
            (new SourceFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getLine(1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_023() throws Exception {
        // Combination: receiver__fileName="", lineNumber=-1
        try {
            (new SourceFile("")).getLine(-1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_024() throws Exception {
        // Combination: receiver__fileName=" ", lineNumber=-1
        try {
            (new SourceFile(" ")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_025() throws Exception {
        // Combination: receiver__fileName="a", lineNumber=-1
        try {
            (new SourceFile("a")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_026() throws Exception {
        // Combination: receiver__fileName="test123", lineNumber=-1
        try {
            (new SourceFile("test123")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_027() throws Exception {
        // Combination: receiver__fileName="!@#", lineNumber=-1
        try {
            (new SourceFile("!@#")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_028() throws Exception {
        // Combination: receiver__fileName="0", lineNumber=-1
        try {
            (new SourceFile("0")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_029() throws Exception {
        // Combination: receiver__fileName="-1", lineNumber=-1
        try {
            (new SourceFile("-1")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_030() throws Exception {
        // Combination: receiver__fileName="1.5", lineNumber=-1
        try {
            (new SourceFile("1.5")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_031() throws Exception {
        // Combination: receiver__fileName="9223372036854775807", lineNumber=-1
        try {
            (new SourceFile("9223372036854775807")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_032() throws Exception {
        // Combination: receiver__fileName="9223372036854775808", lineNumber=-1
        try {
            (new SourceFile("9223372036854775808")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_033() throws Exception {
        // Combination: receiver__fileName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineNumber=-1
        try {
            (new SourceFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getLine(-1);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_034() throws Exception {
        // Combination: receiver__fileName="", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_035() throws Exception {
        // Combination: receiver__fileName=" ", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile(" ")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_036() throws Exception {
        // Combination: receiver__fileName="a", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("a")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_037() throws Exception {
        // Combination: receiver__fileName="test123", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("test123")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_038() throws Exception {
        // Combination: receiver__fileName="!@#", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("!@#")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_039() throws Exception {
        // Combination: receiver__fileName="0", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("0")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_040() throws Exception {
        // Combination: receiver__fileName="-1", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("-1")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_041() throws Exception {
        // Combination: receiver__fileName="1.5", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("1.5")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_042() throws Exception {
        // Combination: receiver__fileName="9223372036854775807", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("9223372036854775807")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_043() throws Exception {
        // Combination: receiver__fileName="9223372036854775808", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("9223372036854775808")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_044() throws Exception {
        // Combination: receiver__fileName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineNumber=Integer.MAX_VALUE
        try {
            (new SourceFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getLine(Integer.MAX_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_045() throws Exception {
        // Combination: receiver__fileName="", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_046() throws Exception {
        // Combination: receiver__fileName=" ", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile(" ")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_047() throws Exception {
        // Combination: receiver__fileName="a", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("a")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_048() throws Exception {
        // Combination: receiver__fileName="test123", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("test123")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_049() throws Exception {
        // Combination: receiver__fileName="!@#", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("!@#")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_050() throws Exception {
        // Combination: receiver__fileName="0", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("0")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_051() throws Exception {
        // Combination: receiver__fileName="-1", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("-1")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_052() throws Exception {
        // Combination: receiver__fileName="1.5", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("1.5")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_053() throws Exception {
        // Combination: receiver__fileName="9223372036854775807", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("9223372036854775807")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_054() throws Exception {
        // Combination: receiver__fileName="9223372036854775808", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("9223372036854775808")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_getLine_pairwise_055() throws Exception {
        // Combination: receiver__fileName="aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", lineNumber=Integer.MIN_VALUE
        try {
            (new SourceFile("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa")).getLine(Integer.MIN_VALUE);
            fail("Expected java.lang.NullPointerException");
        } catch (java.lang.NullPointerException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
