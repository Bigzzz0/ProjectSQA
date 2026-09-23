package org.apache.commons.math.util;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for MathUtils.
 */
public class MathUtils_IPOTest {
    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_001() throws Exception {
        // Combination: x=0, y=0
        Object actual = MathUtils.addAndCheck(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_002() throws Exception {
        // Combination: x=0, y=1
        Object actual = MathUtils.addAndCheck(0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_003() throws Exception {
        // Combination: x=0, y=-1
        Object actual = MathUtils.addAndCheck(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_004() throws Exception {
        // Combination: x=0, y=Integer.MAX_VALUE
        Object actual = MathUtils.addAndCheck(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_005() throws Exception {
        // Combination: x=0, y=Integer.MIN_VALUE
        Object actual = MathUtils.addAndCheck(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_006() throws Exception {
        // Combination: x=1, y=0
        Object actual = MathUtils.addAndCheck(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_007() throws Exception {
        // Combination: x=1, y=1
        Object actual = MathUtils.addAndCheck(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_008() throws Exception {
        // Combination: x=1, y=-1
        Object actual = MathUtils.addAndCheck(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_009() throws Exception {
        // Combination: x=1, y=Integer.MAX_VALUE
        try {
            MathUtils.addAndCheck(1, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_010() throws Exception {
        // Combination: x=1, y=Integer.MIN_VALUE
        Object actual = MathUtils.addAndCheck(1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_011() throws Exception {
        // Combination: x=-1, y=0
        Object actual = MathUtils.addAndCheck(-1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_012() throws Exception {
        // Combination: x=-1, y=1
        Object actual = MathUtils.addAndCheck(-1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_013() throws Exception {
        // Combination: x=-1, y=-1
        Object actual = MathUtils.addAndCheck(-1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_014() throws Exception {
        // Combination: x=-1, y=Integer.MAX_VALUE
        Object actual = MathUtils.addAndCheck(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483646", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_015() throws Exception {
        // Combination: x=-1, y=Integer.MIN_VALUE
        try {
            MathUtils.addAndCheck(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_016() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=0
        Object actual = MathUtils.addAndCheck(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_017() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=1
        try {
            MathUtils.addAndCheck(Integer.MAX_VALUE, 1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_018() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=-1
        Object actual = MathUtils.addAndCheck(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483646", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_019() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=Integer.MAX_VALUE
        try {
            MathUtils.addAndCheck(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_020() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=Integer.MIN_VALUE
        Object actual = MathUtils.addAndCheck(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_021() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=0
        Object actual = MathUtils.addAndCheck(Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_022() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=1
        Object actual = MathUtils.addAndCheck(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_023() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=-1
        try {
            MathUtils.addAndCheck(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_024() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=Integer.MAX_VALUE
        Object actual = MathUtils.addAndCheck(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_025() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=Integer.MIN_VALUE
        try {
            MathUtils.addAndCheck(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_026() throws Exception {
        // Combination: a=0L, b=0L
        Object actual = MathUtils.addAndCheck(0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_027() throws Exception {
        // Combination: a=0L, b=1L
        Object actual = MathUtils.addAndCheck(0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_028() throws Exception {
        // Combination: a=0L, b=-1L
        Object actual = MathUtils.addAndCheck(0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_029() throws Exception {
        // Combination: a=0L, b=Long.MAX_VALUE
        Object actual = MathUtils.addAndCheck(0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_030() throws Exception {
        // Combination: a=0L, b=Long.MIN_VALUE
        Object actual = MathUtils.addAndCheck(0L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_031() throws Exception {
        // Combination: a=1L, b=0L
        Object actual = MathUtils.addAndCheck(1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_032() throws Exception {
        // Combination: a=1L, b=1L
        Object actual = MathUtils.addAndCheck(1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_033() throws Exception {
        // Combination: a=1L, b=-1L
        Object actual = MathUtils.addAndCheck(1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_034() throws Exception {
        // Combination: a=1L, b=Long.MAX_VALUE
        try {
            MathUtils.addAndCheck(1L, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_035() throws Exception {
        // Combination: a=1L, b=Long.MIN_VALUE
        Object actual = MathUtils.addAndCheck(1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_036() throws Exception {
        // Combination: a=-1L, b=0L
        Object actual = MathUtils.addAndCheck(-1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_037() throws Exception {
        // Combination: a=-1L, b=1L
        Object actual = MathUtils.addAndCheck(-1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_038() throws Exception {
        // Combination: a=-1L, b=-1L
        Object actual = MathUtils.addAndCheck(-1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_039() throws Exception {
        // Combination: a=-1L, b=Long.MAX_VALUE
        Object actual = MathUtils.addAndCheck(-1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775806", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_040() throws Exception {
        // Combination: a=-1L, b=Long.MIN_VALUE
        try {
            MathUtils.addAndCheck(-1L, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_041() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=0L
        Object actual = MathUtils.addAndCheck(Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_042() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=1L
        try {
            MathUtils.addAndCheck(Long.MAX_VALUE, 1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_043() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=-1L
        Object actual = MathUtils.addAndCheck(Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775806", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_044() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE
        try {
            MathUtils.addAndCheck(Long.MAX_VALUE, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_045() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MIN_VALUE
        Object actual = MathUtils.addAndCheck(Long.MAX_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_046() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=0L
        Object actual = MathUtils.addAndCheck(Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_047() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=1L
        Object actual = MathUtils.addAndCheck(Long.MIN_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_048() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=-1L
        try {
            MathUtils.addAndCheck(Long.MIN_VALUE, -1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_049() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MAX_VALUE
        Object actual = MathUtils.addAndCheck(Long.MIN_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_addAndCheck_pairwise_050() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MIN_VALUE
        try {
            MathUtils.addAndCheck(Long.MIN_VALUE, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_051() throws Exception {
        // Combination: n=0, k=0
        Object actual = MathUtils.binomialCoefficient(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_052() throws Exception {
        // Combination: n=1, k=0
        Object actual = MathUtils.binomialCoefficient(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_053() throws Exception {
        // Combination: n=-1, k=0
        try {
            MathUtils.binomialCoefficient(-1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_054() throws Exception {
        // Combination: n=Integer.MAX_VALUE, k=0
        Object actual = MathUtils.binomialCoefficient(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_055() throws Exception {
        // Combination: n=Integer.MIN_VALUE, k=0
        try {
            MathUtils.binomialCoefficient(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_056() throws Exception {
        // Combination: n=0, k=1
        try {
            MathUtils.binomialCoefficient(0, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_057() throws Exception {
        // Combination: n=1, k=1
        Object actual = MathUtils.binomialCoefficient(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_058() throws Exception {
        // Combination: n=-1, k=1
        try {
            MathUtils.binomialCoefficient(-1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_059() throws Exception {
        // Combination: n=Integer.MAX_VALUE, k=1
        Object actual = MathUtils.binomialCoefficient(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_060() throws Exception {
        // Combination: n=Integer.MIN_VALUE, k=1
        try {
            MathUtils.binomialCoefficient(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_061() throws Exception {
        // Combination: n=0, k=-1
        Object actual = MathUtils.binomialCoefficient(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_062() throws Exception {
        // Combination: n=1, k=-1
        Object actual = MathUtils.binomialCoefficient(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_063() throws Exception {
        // Combination: n=-1, k=-1
        try {
            MathUtils.binomialCoefficient(-1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_064() throws Exception {
        // Combination: n=Integer.MAX_VALUE, k=-1
        Object actual = MathUtils.binomialCoefficient(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_065() throws Exception {
        // Combination: n=Integer.MIN_VALUE, k=-1
        try {
            MathUtils.binomialCoefficient(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_066() throws Exception {
        // Combination: n=0, k=Integer.MAX_VALUE
        try {
            MathUtils.binomialCoefficient(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_067() throws Exception {
        // Combination: n=1, k=Integer.MAX_VALUE
        try {
            MathUtils.binomialCoefficient(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_068() throws Exception {
        // Combination: n=-1, k=Integer.MAX_VALUE
        try {
            MathUtils.binomialCoefficient(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_069() throws Exception {
        // Combination: n=Integer.MAX_VALUE, k=Integer.MAX_VALUE
        Object actual = MathUtils.binomialCoefficient(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_070() throws Exception {
        // Combination: n=Integer.MIN_VALUE, k=Integer.MAX_VALUE
        try {
            MathUtils.binomialCoefficient(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_071() throws Exception {
        // Combination: n=0, k=Integer.MIN_VALUE
        Object actual = MathUtils.binomialCoefficient(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_072() throws Exception {
        // Combination: n=1, k=Integer.MIN_VALUE
        Object actual = MathUtils.binomialCoefficient(1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_073() throws Exception {
        // Combination: n=-1, k=Integer.MIN_VALUE
        try {
            MathUtils.binomialCoefficient(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_074() throws Exception {
        // Combination: n=Integer.MAX_VALUE, k=Integer.MIN_VALUE
        Object actual = MathUtils.binomialCoefficient(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficient_pairwise_075() throws Exception {
        // Combination: n=Integer.MIN_VALUE, k=Integer.MIN_VALUE
        try {
            MathUtils.binomialCoefficient(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_076() throws Exception {
        // Combination: n=0, k=0
        Object actual = MathUtils.binomialCoefficientDouble(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_077() throws Exception {
        // Combination: n=1, k=0
        Object actual = MathUtils.binomialCoefficientDouble(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_078() throws Exception {
        // Combination: n=-1, k=0
        try {
            MathUtils.binomialCoefficientDouble(-1, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_079() throws Exception {
        // Combination: n=Integer.MAX_VALUE, k=0
        Object actual = MathUtils.binomialCoefficientDouble(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_080() throws Exception {
        // Combination: n=Integer.MIN_VALUE, k=0
        try {
            MathUtils.binomialCoefficientDouble(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_081() throws Exception {
        // Combination: n=0, k=1
        try {
            MathUtils.binomialCoefficientDouble(0, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_082() throws Exception {
        // Combination: n=1, k=1
        Object actual = MathUtils.binomialCoefficientDouble(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_083() throws Exception {
        // Combination: n=-1, k=1
        try {
            MathUtils.binomialCoefficientDouble(-1, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_084() throws Exception {
        // Combination: n=Integer.MAX_VALUE, k=1
        Object actual = MathUtils.binomialCoefficientDouble(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("2.147483647E9", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_085() throws Exception {
        // Combination: n=Integer.MIN_VALUE, k=1
        try {
            MathUtils.binomialCoefficientDouble(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_086() throws Exception {
        // Combination: n=0, k=-1
        Object actual = MathUtils.binomialCoefficientDouble(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_087() throws Exception {
        // Combination: n=1, k=-1
        Object actual = MathUtils.binomialCoefficientDouble(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_088() throws Exception {
        // Combination: n=-1, k=-1
        try {
            MathUtils.binomialCoefficientDouble(-1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_089() throws Exception {
        // Combination: n=Integer.MAX_VALUE, k=-1
        Object actual = MathUtils.binomialCoefficientDouble(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_090() throws Exception {
        // Combination: n=Integer.MIN_VALUE, k=-1
        try {
            MathUtils.binomialCoefficientDouble(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_091() throws Exception {
        // Combination: n=0, k=Integer.MAX_VALUE
        try {
            MathUtils.binomialCoefficientDouble(0, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_092() throws Exception {
        // Combination: n=1, k=Integer.MAX_VALUE
        try {
            MathUtils.binomialCoefficientDouble(1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_093() throws Exception {
        // Combination: n=-1, k=Integer.MAX_VALUE
        try {
            MathUtils.binomialCoefficientDouble(-1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_094() throws Exception {
        // Combination: n=Integer.MAX_VALUE, k=Integer.MAX_VALUE
        Object actual = MathUtils.binomialCoefficientDouble(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_095() throws Exception {
        // Combination: n=Integer.MIN_VALUE, k=Integer.MAX_VALUE
        try {
            MathUtils.binomialCoefficientDouble(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_096() throws Exception {
        // Combination: n=0, k=Integer.MIN_VALUE
        Object actual = MathUtils.binomialCoefficientDouble(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_097() throws Exception {
        // Combination: n=1, k=Integer.MIN_VALUE
        Object actual = MathUtils.binomialCoefficientDouble(1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_098() throws Exception {
        // Combination: n=-1, k=Integer.MIN_VALUE
        try {
            MathUtils.binomialCoefficientDouble(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_099() throws Exception {
        // Combination: n=Integer.MAX_VALUE, k=Integer.MIN_VALUE
        Object actual = MathUtils.binomialCoefficientDouble(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_binomialCoefficientDouble_pairwise_100() throws Exception {
        // Combination: n=Integer.MIN_VALUE, k=Integer.MIN_VALUE
        try {
            MathUtils.binomialCoefficientDouble(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_101() throws Exception {
        // Combination: x=0.0d, y=0.0d, eps=0.0d
        Object actual = MathUtils.compareTo(0.0d, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_102() throws Exception {
        // Combination: x=1.0d, y=1.0d, eps=0.0d
        Object actual = MathUtils.compareTo(1.0d, 1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_103() throws Exception {
        // Combination: x=-1.0d, y=-1.0d, eps=0.0d
        Object actual = MathUtils.compareTo(-1.0d, -1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_104() throws Exception {
        // Combination: x=Double.NaN, y=Double.NaN, eps=0.0d
        Object actual = MathUtils.compareTo(Double.NaN, Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_105() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.POSITIVE_INFINITY, eps=0.0d
        Object actual = MathUtils.compareTo(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_106() throws Exception {
        // Combination: x=0.0d, y=1.0d, eps=1.0d
        Object actual = MathUtils.compareTo(0.0d, 1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_107() throws Exception {
        // Combination: x=1.0d, y=0.0d, eps=1.0d
        Object actual = MathUtils.compareTo(1.0d, 0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_108() throws Exception {
        // Combination: x=-1.0d, y=Double.NaN, eps=1.0d
        Object actual = MathUtils.compareTo(-1.0d, Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_109() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d, eps=1.0d
        Object actual = MathUtils.compareTo(Double.NaN, -1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_110() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=0.0d, eps=1.0d
        Object actual = MathUtils.compareTo(Double.POSITIVE_INFINITY, 0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_111() throws Exception {
        // Combination: x=0.0d, y=-1.0d, eps=-1.0d
        Object actual = MathUtils.compareTo(0.0d, -1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_112() throws Exception {
        // Combination: x=1.0d, y=Double.NaN, eps=-1.0d
        Object actual = MathUtils.compareTo(1.0d, Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_113() throws Exception {
        // Combination: x=-1.0d, y=0.0d, eps=-1.0d
        Object actual = MathUtils.compareTo(-1.0d, 0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_114() throws Exception {
        // Combination: x=Double.NaN, y=1.0d, eps=-1.0d
        Object actual = MathUtils.compareTo(Double.NaN, 1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_115() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=1.0d, eps=-1.0d
        Object actual = MathUtils.compareTo(Double.POSITIVE_INFINITY, 1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_116() throws Exception {
        // Combination: x=0.0d, y=Double.NaN, eps=Double.NaN
        Object actual = MathUtils.compareTo(0.0d, Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_117() throws Exception {
        // Combination: x=1.0d, y=-1.0d, eps=Double.NaN
        Object actual = MathUtils.compareTo(1.0d, -1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_118() throws Exception {
        // Combination: x=-1.0d, y=1.0d, eps=Double.NaN
        Object actual = MathUtils.compareTo(-1.0d, 1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_119() throws Exception {
        // Combination: x=Double.NaN, y=0.0d, eps=Double.NaN
        Object actual = MathUtils.compareTo(Double.NaN, 0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_120() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=-1.0d, eps=Double.NaN
        Object actual = MathUtils.compareTo(Double.POSITIVE_INFINITY, -1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_121() throws Exception {
        // Combination: x=0.0d, y=Double.POSITIVE_INFINITY, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.compareTo(0.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_122() throws Exception {
        // Combination: x=1.0d, y=0.0d, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.compareTo(1.0d, 0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_123() throws Exception {
        // Combination: x=-1.0d, y=1.0d, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.compareTo(-1.0d, 1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_124() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.compareTo(Double.NaN, -1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_125() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.NaN, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.compareTo(Double.POSITIVE_INFINITY, Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_126() throws Exception {
        // Combination: x=1.0d, y=Double.POSITIVE_INFINITY, eps=1.0d
        Object actual = MathUtils.compareTo(1.0d, Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_127() throws Exception {
        // Combination: x=-1.0d, y=Double.POSITIVE_INFINITY, eps=-1.0d
        Object actual = MathUtils.compareTo(-1.0d, Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_compareTo_pairwise_128() throws Exception {
        // Combination: x=Double.NaN, y=Double.POSITIVE_INFINITY, eps=Double.NaN
        Object actual = MathUtils.compareTo(Double.NaN, Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_129() throws Exception {
        // Combination: x=0.0d, y=0.0d
        Object actual = MathUtils.equals(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_130() throws Exception {
        // Combination: x=0.0d, y=1.0d
        Object actual = MathUtils.equals(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_131() throws Exception {
        // Combination: x=0.0d, y=-1.0d
        Object actual = MathUtils.equals(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_132() throws Exception {
        // Combination: x=0.0d, y=Double.NaN
        Object actual = MathUtils.equals(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_133() throws Exception {
        // Combination: x=0.0d, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_134() throws Exception {
        // Combination: x=1.0d, y=0.0d
        Object actual = MathUtils.equals(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_135() throws Exception {
        // Combination: x=1.0d, y=1.0d
        Object actual = MathUtils.equals(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_136() throws Exception {
        // Combination: x=1.0d, y=-1.0d
        Object actual = MathUtils.equals(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_137() throws Exception {
        // Combination: x=1.0d, y=Double.NaN
        Object actual = MathUtils.equals(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_138() throws Exception {
        // Combination: x=1.0d, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_139() throws Exception {
        // Combination: x=-1.0d, y=0.0d
        Object actual = MathUtils.equals(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_140() throws Exception {
        // Combination: x=-1.0d, y=1.0d
        Object actual = MathUtils.equals(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_141() throws Exception {
        // Combination: x=-1.0d, y=-1.0d
        Object actual = MathUtils.equals(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_142() throws Exception {
        // Combination: x=-1.0d, y=Double.NaN
        Object actual = MathUtils.equals(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_143() throws Exception {
        // Combination: x=-1.0d, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_144() throws Exception {
        // Combination: x=Double.NaN, y=0.0d
        Object actual = MathUtils.equals(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_145() throws Exception {
        // Combination: x=Double.NaN, y=1.0d
        Object actual = MathUtils.equals(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_146() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d
        Object actual = MathUtils.equals(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_147() throws Exception {
        // Combination: x=Double.NaN, y=Double.NaN
        Object actual = MathUtils.equals(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_148() throws Exception {
        // Combination: x=Double.NaN, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_149() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=0.0d
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_150() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=1.0d
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_151() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=-1.0d
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_152() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.NaN
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_153() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_154() throws Exception {
        // Combination: x=0.0d, y=0.0d
        Object actual = MathUtils.equalsIncludingNaN(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_155() throws Exception {
        // Combination: x=0.0d, y=1.0d
        Object actual = MathUtils.equalsIncludingNaN(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_156() throws Exception {
        // Combination: x=0.0d, y=-1.0d
        Object actual = MathUtils.equalsIncludingNaN(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_157() throws Exception {
        // Combination: x=0.0d, y=Double.NaN
        Object actual = MathUtils.equalsIncludingNaN(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_158() throws Exception {
        // Combination: x=0.0d, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equalsIncludingNaN(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_159() throws Exception {
        // Combination: x=1.0d, y=0.0d
        Object actual = MathUtils.equalsIncludingNaN(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_160() throws Exception {
        // Combination: x=1.0d, y=1.0d
        Object actual = MathUtils.equalsIncludingNaN(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_161() throws Exception {
        // Combination: x=1.0d, y=-1.0d
        Object actual = MathUtils.equalsIncludingNaN(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_162() throws Exception {
        // Combination: x=1.0d, y=Double.NaN
        Object actual = MathUtils.equalsIncludingNaN(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_163() throws Exception {
        // Combination: x=1.0d, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equalsIncludingNaN(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_164() throws Exception {
        // Combination: x=-1.0d, y=0.0d
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_165() throws Exception {
        // Combination: x=-1.0d, y=1.0d
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_166() throws Exception {
        // Combination: x=-1.0d, y=-1.0d
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_167() throws Exception {
        // Combination: x=-1.0d, y=Double.NaN
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_168() throws Exception {
        // Combination: x=-1.0d, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_169() throws Exception {
        // Combination: x=Double.NaN, y=0.0d
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_170() throws Exception {
        // Combination: x=Double.NaN, y=1.0d
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_171() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_172() throws Exception {
        // Combination: x=Double.NaN, y=Double.NaN
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_173() throws Exception {
        // Combination: x=Double.NaN, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_174() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=0.0d
        Object actual = MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_175() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=1.0d
        Object actual = MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_176() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=-1.0d
        Object actual = MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_177() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.NaN
        Object actual = MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_178() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_179() throws Exception {
        // Combination: x=0.0d, y=0.0d, eps=0.0d
        Object actual = MathUtils.equals(0.0d, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_180() throws Exception {
        // Combination: x=1.0d, y=1.0d, eps=0.0d
        Object actual = MathUtils.equals(1.0d, 1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_181() throws Exception {
        // Combination: x=-1.0d, y=-1.0d, eps=0.0d
        Object actual = MathUtils.equals(-1.0d, -1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_182() throws Exception {
        // Combination: x=Double.NaN, y=Double.NaN, eps=0.0d
        Object actual = MathUtils.equals(Double.NaN, Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_183() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.POSITIVE_INFINITY, eps=0.0d
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_184() throws Exception {
        // Combination: x=0.0d, y=1.0d, eps=1.0d
        Object actual = MathUtils.equals(0.0d, 1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_185() throws Exception {
        // Combination: x=1.0d, y=0.0d, eps=1.0d
        Object actual = MathUtils.equals(1.0d, 0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_186() throws Exception {
        // Combination: x=-1.0d, y=Double.NaN, eps=1.0d
        Object actual = MathUtils.equals(-1.0d, Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_187() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d, eps=1.0d
        Object actual = MathUtils.equals(Double.NaN, -1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_188() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=0.0d, eps=1.0d
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, 0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_189() throws Exception {
        // Combination: x=0.0d, y=-1.0d, eps=-1.0d
        Object actual = MathUtils.equals(0.0d, -1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_190() throws Exception {
        // Combination: x=1.0d, y=Double.NaN, eps=-1.0d
        Object actual = MathUtils.equals(1.0d, Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_191() throws Exception {
        // Combination: x=-1.0d, y=0.0d, eps=-1.0d
        Object actual = MathUtils.equals(-1.0d, 0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_192() throws Exception {
        // Combination: x=Double.NaN, y=1.0d, eps=-1.0d
        Object actual = MathUtils.equals(Double.NaN, 1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_193() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=1.0d, eps=-1.0d
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, 1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_194() throws Exception {
        // Combination: x=0.0d, y=Double.NaN, eps=Double.NaN
        Object actual = MathUtils.equals(0.0d, Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_195() throws Exception {
        // Combination: x=1.0d, y=-1.0d, eps=Double.NaN
        Object actual = MathUtils.equals(1.0d, -1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_196() throws Exception {
        // Combination: x=-1.0d, y=1.0d, eps=Double.NaN
        Object actual = MathUtils.equals(-1.0d, 1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_197() throws Exception {
        // Combination: x=Double.NaN, y=0.0d, eps=Double.NaN
        Object actual = MathUtils.equals(Double.NaN, 0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_198() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=-1.0d, eps=Double.NaN
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, -1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_199() throws Exception {
        // Combination: x=0.0d, y=Double.POSITIVE_INFINITY, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(0.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_200() throws Exception {
        // Combination: x=1.0d, y=0.0d, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(1.0d, 0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_201() throws Exception {
        // Combination: x=-1.0d, y=1.0d, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(-1.0d, 1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_202() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(Double.NaN, -1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_203() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.NaN, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_204() throws Exception {
        // Combination: x=1.0d, y=Double.POSITIVE_INFINITY, eps=1.0d
        Object actual = MathUtils.equals(1.0d, Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_205() throws Exception {
        // Combination: x=-1.0d, y=Double.POSITIVE_INFINITY, eps=-1.0d
        Object actual = MathUtils.equals(-1.0d, Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_206() throws Exception {
        // Combination: x=Double.NaN, y=Double.POSITIVE_INFINITY, eps=Double.NaN
        Object actual = MathUtils.equals(Double.NaN, Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_207() throws Exception {
        // Combination: x=0.0d, y=0.0d, eps=0.0d
        Object actual = MathUtils.equalsIncludingNaN(0.0d, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_208() throws Exception {
        // Combination: x=1.0d, y=1.0d, eps=0.0d
        Object actual = MathUtils.equalsIncludingNaN(1.0d, 1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_209() throws Exception {
        // Combination: x=-1.0d, y=-1.0d, eps=0.0d
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, -1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_210() throws Exception {
        // Combination: x=Double.NaN, y=Double.NaN, eps=0.0d
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_211() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.POSITIVE_INFINITY, eps=0.0d
        Object actual = MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_212() throws Exception {
        // Combination: x=0.0d, y=1.0d, eps=1.0d
        Object actual = MathUtils.equalsIncludingNaN(0.0d, 1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_213() throws Exception {
        // Combination: x=1.0d, y=0.0d, eps=1.0d
        Object actual = MathUtils.equalsIncludingNaN(1.0d, 0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_214() throws Exception {
        // Combination: x=-1.0d, y=Double.NaN, eps=1.0d
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_215() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d, eps=1.0d
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, -1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_216() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=0.0d, eps=1.0d
        Object actual = MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, 0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_217() throws Exception {
        // Combination: x=0.0d, y=-1.0d, eps=-1.0d
        Object actual = MathUtils.equalsIncludingNaN(0.0d, -1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_218() throws Exception {
        // Combination: x=1.0d, y=Double.NaN, eps=-1.0d
        Object actual = MathUtils.equalsIncludingNaN(1.0d, Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_219() throws Exception {
        // Combination: x=-1.0d, y=0.0d, eps=-1.0d
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, 0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_220() throws Exception {
        // Combination: x=Double.NaN, y=1.0d, eps=-1.0d
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, 1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_221() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=1.0d, eps=-1.0d
        Object actual = MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, 1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_222() throws Exception {
        // Combination: x=0.0d, y=Double.NaN, eps=Double.NaN
        Object actual = MathUtils.equalsIncludingNaN(0.0d, Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_223() throws Exception {
        // Combination: x=1.0d, y=-1.0d, eps=Double.NaN
        Object actual = MathUtils.equalsIncludingNaN(1.0d, -1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_224() throws Exception {
        // Combination: x=-1.0d, y=1.0d, eps=Double.NaN
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, 1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_225() throws Exception {
        // Combination: x=Double.NaN, y=0.0d, eps=Double.NaN
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, 0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_226() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=-1.0d, eps=Double.NaN
        Object actual = MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, -1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_227() throws Exception {
        // Combination: x=0.0d, y=Double.POSITIVE_INFINITY, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equalsIncludingNaN(0.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_228() throws Exception {
        // Combination: x=1.0d, y=0.0d, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equalsIncludingNaN(1.0d, 0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_229() throws Exception {
        // Combination: x=-1.0d, y=1.0d, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, 1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_230() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, -1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_231() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.NaN, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_232() throws Exception {
        // Combination: x=1.0d, y=Double.POSITIVE_INFINITY, eps=1.0d
        Object actual = MathUtils.equalsIncludingNaN(1.0d, Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_233() throws Exception {
        // Combination: x=-1.0d, y=Double.POSITIVE_INFINITY, eps=-1.0d
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_234() throws Exception {
        // Combination: x=Double.NaN, y=Double.POSITIVE_INFINITY, eps=Double.NaN
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_235() throws Exception {
        // Combination: x=0.0d, y=0.0d, maxUlps=0
        Object actual = MathUtils.equals(0.0d, 0.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_236() throws Exception {
        // Combination: x=1.0d, y=1.0d, maxUlps=0
        Object actual = MathUtils.equals(1.0d, 1.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_237() throws Exception {
        // Combination: x=-1.0d, y=-1.0d, maxUlps=0
        Object actual = MathUtils.equals(-1.0d, -1.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_238() throws Exception {
        // Combination: x=Double.NaN, y=Double.NaN, maxUlps=0
        Object actual = MathUtils.equals(Double.NaN, Double.NaN, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_239() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.POSITIVE_INFINITY, maxUlps=0
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_240() throws Exception {
        // Combination: x=0.0d, y=1.0d, maxUlps=1
        Object actual = MathUtils.equals(0.0d, 1.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_241() throws Exception {
        // Combination: x=1.0d, y=0.0d, maxUlps=1
        Object actual = MathUtils.equals(1.0d, 0.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_242() throws Exception {
        // Combination: x=-1.0d, y=Double.NaN, maxUlps=1
        Object actual = MathUtils.equals(-1.0d, Double.NaN, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_243() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d, maxUlps=1
        Object actual = MathUtils.equals(Double.NaN, -1.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_244() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=0.0d, maxUlps=1
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, 0.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_245() throws Exception {
        // Combination: x=0.0d, y=-1.0d, maxUlps=-1
        Object actual = MathUtils.equals(0.0d, -1.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_246() throws Exception {
        // Combination: x=1.0d, y=Double.NaN, maxUlps=-1
        Object actual = MathUtils.equals(1.0d, Double.NaN, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_247() throws Exception {
        // Combination: x=-1.0d, y=0.0d, maxUlps=-1
        Object actual = MathUtils.equals(-1.0d, 0.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_248() throws Exception {
        // Combination: x=Double.NaN, y=1.0d, maxUlps=-1
        Object actual = MathUtils.equals(Double.NaN, 1.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_249() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=1.0d, maxUlps=-1
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, 1.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_250() throws Exception {
        // Combination: x=0.0d, y=Double.NaN, maxUlps=Integer.MAX_VALUE
        Object actual = MathUtils.equals(0.0d, Double.NaN, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_251() throws Exception {
        // Combination: x=1.0d, y=-1.0d, maxUlps=Integer.MAX_VALUE
        Object actual = MathUtils.equals(1.0d, -1.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_252() throws Exception {
        // Combination: x=-1.0d, y=1.0d, maxUlps=Integer.MAX_VALUE
        Object actual = MathUtils.equals(-1.0d, 1.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_253() throws Exception {
        // Combination: x=Double.NaN, y=0.0d, maxUlps=Integer.MAX_VALUE
        Object actual = MathUtils.equals(Double.NaN, 0.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_254() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=-1.0d, maxUlps=Integer.MAX_VALUE
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, -1.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_255() throws Exception {
        // Combination: x=0.0d, y=Double.POSITIVE_INFINITY, maxUlps=Integer.MIN_VALUE
        Object actual = MathUtils.equals(0.0d, Double.POSITIVE_INFINITY, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_256() throws Exception {
        // Combination: x=1.0d, y=0.0d, maxUlps=Integer.MIN_VALUE
        Object actual = MathUtils.equals(1.0d, 0.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_257() throws Exception {
        // Combination: x=-1.0d, y=1.0d, maxUlps=Integer.MIN_VALUE
        Object actual = MathUtils.equals(-1.0d, 1.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_258() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d, maxUlps=Integer.MIN_VALUE
        Object actual = MathUtils.equals(Double.NaN, -1.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_259() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.NaN, maxUlps=Integer.MIN_VALUE
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, Double.NaN, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_260() throws Exception {
        // Combination: x=1.0d, y=Double.POSITIVE_INFINITY, maxUlps=1
        Object actual = MathUtils.equals(1.0d, Double.POSITIVE_INFINITY, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_261() throws Exception {
        // Combination: x=-1.0d, y=Double.POSITIVE_INFINITY, maxUlps=-1
        Object actual = MathUtils.equals(-1.0d, Double.POSITIVE_INFINITY, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_262() throws Exception {
        // Combination: x=Double.NaN, y=Double.POSITIVE_INFINITY, maxUlps=Integer.MAX_VALUE
        Object actual = MathUtils.equals(Double.NaN, Double.POSITIVE_INFINITY, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_263() throws Exception {
        // Combination: x=0.0d, y=0.0d, maxUlps=0
        Object actual = MathUtils.equalsIncludingNaN(0.0d, 0.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_264() throws Exception {
        // Combination: x=1.0d, y=1.0d, maxUlps=0
        Object actual = MathUtils.equalsIncludingNaN(1.0d, 1.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_265() throws Exception {
        // Combination: x=-1.0d, y=-1.0d, maxUlps=0
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, -1.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_266() throws Exception {
        // Combination: x=Double.NaN, y=Double.NaN, maxUlps=0
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, Double.NaN, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_267() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.POSITIVE_INFINITY, maxUlps=0
        Object actual = MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_268() throws Exception {
        // Combination: x=0.0d, y=1.0d, maxUlps=1
        Object actual = MathUtils.equalsIncludingNaN(0.0d, 1.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_269() throws Exception {
        // Combination: x=1.0d, y=0.0d, maxUlps=1
        Object actual = MathUtils.equalsIncludingNaN(1.0d, 0.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_270() throws Exception {
        // Combination: x=-1.0d, y=Double.NaN, maxUlps=1
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, Double.NaN, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_271() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d, maxUlps=1
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, -1.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_272() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=0.0d, maxUlps=1
        Object actual = MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, 0.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_273() throws Exception {
        // Combination: x=0.0d, y=-1.0d, maxUlps=-1
        Object actual = MathUtils.equalsIncludingNaN(0.0d, -1.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_274() throws Exception {
        // Combination: x=1.0d, y=Double.NaN, maxUlps=-1
        Object actual = MathUtils.equalsIncludingNaN(1.0d, Double.NaN, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_275() throws Exception {
        // Combination: x=-1.0d, y=0.0d, maxUlps=-1
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, 0.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_276() throws Exception {
        // Combination: x=Double.NaN, y=1.0d, maxUlps=-1
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, 1.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_277() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=1.0d, maxUlps=-1
        Object actual = MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, 1.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_278() throws Exception {
        // Combination: x=0.0d, y=Double.NaN, maxUlps=Integer.MAX_VALUE
        Object actual = MathUtils.equalsIncludingNaN(0.0d, Double.NaN, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_279() throws Exception {
        // Combination: x=1.0d, y=-1.0d, maxUlps=Integer.MAX_VALUE
        Object actual = MathUtils.equalsIncludingNaN(1.0d, -1.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_280() throws Exception {
        // Combination: x=-1.0d, y=1.0d, maxUlps=Integer.MAX_VALUE
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, 1.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_281() throws Exception {
        // Combination: x=Double.NaN, y=0.0d, maxUlps=Integer.MAX_VALUE
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, 0.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_282() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=-1.0d, maxUlps=Integer.MAX_VALUE
        Object actual = MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, -1.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_283() throws Exception {
        // Combination: x=0.0d, y=Double.POSITIVE_INFINITY, maxUlps=Integer.MIN_VALUE
        Object actual = MathUtils.equalsIncludingNaN(0.0d, Double.POSITIVE_INFINITY, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_284() throws Exception {
        // Combination: x=1.0d, y=0.0d, maxUlps=Integer.MIN_VALUE
        Object actual = MathUtils.equalsIncludingNaN(1.0d, 0.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_285() throws Exception {
        // Combination: x=-1.0d, y=1.0d, maxUlps=Integer.MIN_VALUE
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, 1.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_286() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d, maxUlps=Integer.MIN_VALUE
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, -1.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_287() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.NaN, maxUlps=Integer.MIN_VALUE
        Object actual = MathUtils.equalsIncludingNaN(Double.POSITIVE_INFINITY, Double.NaN, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_288() throws Exception {
        // Combination: x=1.0d, y=Double.POSITIVE_INFINITY, maxUlps=1
        Object actual = MathUtils.equalsIncludingNaN(1.0d, Double.POSITIVE_INFINITY, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_289() throws Exception {
        // Combination: x=-1.0d, y=Double.POSITIVE_INFINITY, maxUlps=-1
        Object actual = MathUtils.equalsIncludingNaN(-1.0d, Double.POSITIVE_INFINITY, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_290() throws Exception {
        // Combination: x=Double.NaN, y=Double.POSITIVE_INFINITY, maxUlps=Integer.MAX_VALUE
        Object actual = MathUtils.equalsIncludingNaN(Double.NaN, Double.POSITIVE_INFINITY, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_291() throws Exception {
        // Combination: x=new double[] {}, y=new double[] {}
        Object actual = MathUtils.equals(new double[] {}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_292() throws Exception {
        // Combination: x=new double[] {}, y=new double[] {1}
        Object actual = MathUtils.equals(new double[] {}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_293() throws Exception {
        // Combination: x=new double[] {1}, y=new double[] {}
        Object actual = MathUtils.equals(new double[] {1}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_294() throws Exception {
        // Combination: x=new double[] {1}, y=new double[] {1}
        Object actual = MathUtils.equals(new double[] {1}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_295() throws Exception {
        // Combination: x=new double[] {}, y=new double[] {}
        Object actual = MathUtils.equalsIncludingNaN(new double[] {}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_296() throws Exception {
        // Combination: x=new double[] {}, y=new double[] {1}
        Object actual = MathUtils.equalsIncludingNaN(new double[] {}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_297() throws Exception {
        // Combination: x=new double[] {1}, y=new double[] {}
        Object actual = MathUtils.equalsIncludingNaN(new double[] {1}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equalsIncludingNaN_pairwise_298() throws Exception {
        // Combination: x=new double[] {1}, y=new double[] {1}
        Object actual = MathUtils.equalsIncludingNaN(new double[] {1}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_299() throws Exception {
        // Combination: p=0, q=0
        Object actual = MathUtils.gcd(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_300() throws Exception {
        // Combination: p=0, q=1
        Object actual = MathUtils.gcd(0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_301() throws Exception {
        // Combination: p=0, q=-1
        Object actual = MathUtils.gcd(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_302() throws Exception {
        // Combination: p=0, q=Integer.MAX_VALUE
        Object actual = MathUtils.gcd(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_303() throws Exception {
        // Combination: p=0, q=Integer.MIN_VALUE
        try {
            MathUtils.gcd(0, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_304() throws Exception {
        // Combination: p=1, q=0
        Object actual = MathUtils.gcd(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_305() throws Exception {
        // Combination: p=1, q=1
        Object actual = MathUtils.gcd(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_306() throws Exception {
        // Combination: p=1, q=-1
        Object actual = MathUtils.gcd(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_307() throws Exception {
        // Combination: p=1, q=Integer.MAX_VALUE
        Object actual = MathUtils.gcd(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_308() throws Exception {
        // Combination: p=1, q=Integer.MIN_VALUE
        Object actual = MathUtils.gcd(1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_309() throws Exception {
        // Combination: p=-1, q=0
        Object actual = MathUtils.gcd(-1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_310() throws Exception {
        // Combination: p=-1, q=1
        Object actual = MathUtils.gcd(-1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_311() throws Exception {
        // Combination: p=-1, q=-1
        Object actual = MathUtils.gcd(-1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_312() throws Exception {
        // Combination: p=-1, q=Integer.MAX_VALUE
        Object actual = MathUtils.gcd(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_313() throws Exception {
        // Combination: p=-1, q=Integer.MIN_VALUE
        Object actual = MathUtils.gcd(-1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_314() throws Exception {
        // Combination: p=Integer.MAX_VALUE, q=0
        Object actual = MathUtils.gcd(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_315() throws Exception {
        // Combination: p=Integer.MAX_VALUE, q=1
        Object actual = MathUtils.gcd(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_316() throws Exception {
        // Combination: p=Integer.MAX_VALUE, q=-1
        Object actual = MathUtils.gcd(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_317() throws Exception {
        // Combination: p=Integer.MAX_VALUE, q=Integer.MAX_VALUE
        Object actual = MathUtils.gcd(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_318() throws Exception {
        // Combination: p=Integer.MAX_VALUE, q=Integer.MIN_VALUE
        Object actual = MathUtils.gcd(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_319() throws Exception {
        // Combination: p=Integer.MIN_VALUE, q=0
        try {
            MathUtils.gcd(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_320() throws Exception {
        // Combination: p=Integer.MIN_VALUE, q=1
        Object actual = MathUtils.gcd(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_321() throws Exception {
        // Combination: p=Integer.MIN_VALUE, q=-1
        Object actual = MathUtils.gcd(Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_322() throws Exception {
        // Combination: p=Integer.MIN_VALUE, q=Integer.MAX_VALUE
        Object actual = MathUtils.gcd(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_323() throws Exception {
        // Combination: p=Integer.MIN_VALUE, q=Integer.MIN_VALUE
        try {
            MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_324() throws Exception {
        // Combination: p=0L, q=0L
        Object actual = MathUtils.gcd(0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_325() throws Exception {
        // Combination: p=0L, q=1L
        Object actual = MathUtils.gcd(0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_326() throws Exception {
        // Combination: p=0L, q=-1L
        Object actual = MathUtils.gcd(0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_327() throws Exception {
        // Combination: p=0L, q=Long.MAX_VALUE
        Object actual = MathUtils.gcd(0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_328() throws Exception {
        // Combination: p=0L, q=Long.MIN_VALUE
        try {
            MathUtils.gcd(0L, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_329() throws Exception {
        // Combination: p=1L, q=0L
        Object actual = MathUtils.gcd(1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_330() throws Exception {
        // Combination: p=1L, q=1L
        Object actual = MathUtils.gcd(1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_331() throws Exception {
        // Combination: p=1L, q=-1L
        Object actual = MathUtils.gcd(1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_332() throws Exception {
        // Combination: p=1L, q=Long.MAX_VALUE
        Object actual = MathUtils.gcd(1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_333() throws Exception {
        // Combination: p=1L, q=Long.MIN_VALUE
        Object actual = MathUtils.gcd(1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_334() throws Exception {
        // Combination: p=-1L, q=0L
        Object actual = MathUtils.gcd(-1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_335() throws Exception {
        // Combination: p=-1L, q=1L
        Object actual = MathUtils.gcd(-1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_336() throws Exception {
        // Combination: p=-1L, q=-1L
        Object actual = MathUtils.gcd(-1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_337() throws Exception {
        // Combination: p=-1L, q=Long.MAX_VALUE
        Object actual = MathUtils.gcd(-1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_338() throws Exception {
        // Combination: p=-1L, q=Long.MIN_VALUE
        Object actual = MathUtils.gcd(-1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_339() throws Exception {
        // Combination: p=Long.MAX_VALUE, q=0L
        Object actual = MathUtils.gcd(Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_340() throws Exception {
        // Combination: p=Long.MAX_VALUE, q=1L
        Object actual = MathUtils.gcd(Long.MAX_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_341() throws Exception {
        // Combination: p=Long.MAX_VALUE, q=-1L
        Object actual = MathUtils.gcd(Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_342() throws Exception {
        // Combination: p=Long.MAX_VALUE, q=Long.MAX_VALUE
        Object actual = MathUtils.gcd(Long.MAX_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_343() throws Exception {
        // Combination: p=Long.MAX_VALUE, q=Long.MIN_VALUE
        Object actual = MathUtils.gcd(Long.MAX_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_344() throws Exception {
        // Combination: p=Long.MIN_VALUE, q=0L
        try {
            MathUtils.gcd(Long.MIN_VALUE, 0L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_345() throws Exception {
        // Combination: p=Long.MIN_VALUE, q=1L
        Object actual = MathUtils.gcd(Long.MIN_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_346() throws Exception {
        // Combination: p=Long.MIN_VALUE, q=-1L
        Object actual = MathUtils.gcd(Long.MIN_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_347() throws Exception {
        // Combination: p=Long.MIN_VALUE, q=Long.MAX_VALUE
        Object actual = MathUtils.gcd(Long.MIN_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_348() throws Exception {
        // Combination: p=Long.MIN_VALUE, q=Long.MIN_VALUE
        try {
            MathUtils.gcd(Long.MIN_VALUE, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_349() throws Exception {
        // Combination: a=0, b=0
        Object actual = MathUtils.lcm(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_350() throws Exception {
        // Combination: a=0, b=1
        Object actual = MathUtils.lcm(0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_351() throws Exception {
        // Combination: a=0, b=-1
        Object actual = MathUtils.lcm(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_352() throws Exception {
        // Combination: a=0, b=Integer.MAX_VALUE
        Object actual = MathUtils.lcm(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_353() throws Exception {
        // Combination: a=0, b=Integer.MIN_VALUE
        Object actual = MathUtils.lcm(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_354() throws Exception {
        // Combination: a=1, b=0
        Object actual = MathUtils.lcm(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_355() throws Exception {
        // Combination: a=1, b=1
        Object actual = MathUtils.lcm(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_356() throws Exception {
        // Combination: a=1, b=-1
        Object actual = MathUtils.lcm(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_357() throws Exception {
        // Combination: a=1, b=Integer.MAX_VALUE
        Object actual = MathUtils.lcm(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_358() throws Exception {
        // Combination: a=1, b=Integer.MIN_VALUE
        try {
            MathUtils.lcm(1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_359() throws Exception {
        // Combination: a=-1, b=0
        Object actual = MathUtils.lcm(-1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_360() throws Exception {
        // Combination: a=-1, b=1
        Object actual = MathUtils.lcm(-1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_361() throws Exception {
        // Combination: a=-1, b=-1
        Object actual = MathUtils.lcm(-1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_362() throws Exception {
        // Combination: a=-1, b=Integer.MAX_VALUE
        Object actual = MathUtils.lcm(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_363() throws Exception {
        // Combination: a=-1, b=Integer.MIN_VALUE
        try {
            MathUtils.lcm(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_364() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=0
        Object actual = MathUtils.lcm(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_365() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=1
        Object actual = MathUtils.lcm(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_366() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=-1
        Object actual = MathUtils.lcm(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_367() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MAX_VALUE
        Object actual = MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_368() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MIN_VALUE
        try {
            MathUtils.lcm(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_369() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=0
        Object actual = MathUtils.lcm(Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_370() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=1
        try {
            MathUtils.lcm(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_371() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=-1
        try {
            MathUtils.lcm(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_372() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MAX_VALUE
        try {
            MathUtils.lcm(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_373() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MIN_VALUE
        try {
            MathUtils.lcm(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_374() throws Exception {
        // Combination: a=0L, b=0L
        Object actual = MathUtils.lcm(0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_375() throws Exception {
        // Combination: a=0L, b=1L
        Object actual = MathUtils.lcm(0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_376() throws Exception {
        // Combination: a=0L, b=-1L
        Object actual = MathUtils.lcm(0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_377() throws Exception {
        // Combination: a=0L, b=Long.MAX_VALUE
        Object actual = MathUtils.lcm(0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_378() throws Exception {
        // Combination: a=0L, b=Long.MIN_VALUE
        Object actual = MathUtils.lcm(0L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_379() throws Exception {
        // Combination: a=1L, b=0L
        Object actual = MathUtils.lcm(1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_380() throws Exception {
        // Combination: a=1L, b=1L
        Object actual = MathUtils.lcm(1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_381() throws Exception {
        // Combination: a=1L, b=-1L
        Object actual = MathUtils.lcm(1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_382() throws Exception {
        // Combination: a=1L, b=Long.MAX_VALUE
        Object actual = MathUtils.lcm(1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_383() throws Exception {
        // Combination: a=1L, b=Long.MIN_VALUE
        try {
            MathUtils.lcm(1L, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_384() throws Exception {
        // Combination: a=-1L, b=0L
        Object actual = MathUtils.lcm(-1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_385() throws Exception {
        // Combination: a=-1L, b=1L
        Object actual = MathUtils.lcm(-1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_386() throws Exception {
        // Combination: a=-1L, b=-1L
        Object actual = MathUtils.lcm(-1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_387() throws Exception {
        // Combination: a=-1L, b=Long.MAX_VALUE
        Object actual = MathUtils.lcm(-1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_388() throws Exception {
        // Combination: a=-1L, b=Long.MIN_VALUE
        try {
            MathUtils.lcm(-1L, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_389() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=0L
        Object actual = MathUtils.lcm(Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_390() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=1L
        Object actual = MathUtils.lcm(Long.MAX_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_391() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=-1L
        Object actual = MathUtils.lcm(Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_392() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE
        Object actual = MathUtils.lcm(Long.MAX_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_393() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MIN_VALUE
        try {
            MathUtils.lcm(Long.MAX_VALUE, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_394() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=0L
        Object actual = MathUtils.lcm(Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_395() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=1L
        try {
            MathUtils.lcm(Long.MIN_VALUE, 1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_396() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=-1L
        try {
            MathUtils.lcm(Long.MIN_VALUE, -1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_397() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MAX_VALUE
        try {
            MathUtils.lcm(Long.MIN_VALUE, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_398() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MIN_VALUE
        try {
            MathUtils.lcm(Long.MIN_VALUE, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_399() throws Exception {
        // Combination: base=0.0d, x=0.0d
        Object actual = MathUtils.log(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_400() throws Exception {
        // Combination: base=0.0d, x=1.0d
        Object actual = MathUtils.log(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_401() throws Exception {
        // Combination: base=0.0d, x=-1.0d
        Object actual = MathUtils.log(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_402() throws Exception {
        // Combination: base=0.0d, x=Double.NaN
        Object actual = MathUtils.log(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_403() throws Exception {
        // Combination: base=0.0d, x=Double.POSITIVE_INFINITY
        Object actual = MathUtils.log(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_404() throws Exception {
        // Combination: base=1.0d, x=0.0d
        Object actual = MathUtils.log(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_405() throws Exception {
        // Combination: base=1.0d, x=1.0d
        Object actual = MathUtils.log(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_406() throws Exception {
        // Combination: base=1.0d, x=-1.0d
        Object actual = MathUtils.log(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_407() throws Exception {
        // Combination: base=1.0d, x=Double.NaN
        Object actual = MathUtils.log(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_408() throws Exception {
        // Combination: base=1.0d, x=Double.POSITIVE_INFINITY
        Object actual = MathUtils.log(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_409() throws Exception {
        // Combination: base=-1.0d, x=0.0d
        Object actual = MathUtils.log(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_410() throws Exception {
        // Combination: base=-1.0d, x=1.0d
        Object actual = MathUtils.log(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_411() throws Exception {
        // Combination: base=-1.0d, x=-1.0d
        Object actual = MathUtils.log(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_412() throws Exception {
        // Combination: base=-1.0d, x=Double.NaN
        Object actual = MathUtils.log(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_413() throws Exception {
        // Combination: base=-1.0d, x=Double.POSITIVE_INFINITY
        Object actual = MathUtils.log(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_414() throws Exception {
        // Combination: base=Double.NaN, x=0.0d
        Object actual = MathUtils.log(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_415() throws Exception {
        // Combination: base=Double.NaN, x=1.0d
        Object actual = MathUtils.log(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_416() throws Exception {
        // Combination: base=Double.NaN, x=-1.0d
        Object actual = MathUtils.log(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_417() throws Exception {
        // Combination: base=Double.NaN, x=Double.NaN
        Object actual = MathUtils.log(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_418() throws Exception {
        // Combination: base=Double.NaN, x=Double.POSITIVE_INFINITY
        Object actual = MathUtils.log(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_419() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=0.0d
        Object actual = MathUtils.log(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_420() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=1.0d
        Object actual = MathUtils.log(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_421() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=-1.0d
        Object actual = MathUtils.log(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_422() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=Double.NaN
        Object actual = MathUtils.log(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_423() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=Double.POSITIVE_INFINITY
        Object actual = MathUtils.log(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_424() throws Exception {
        // Combination: x=0, y=0
        Object actual = MathUtils.mulAndCheck(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_425() throws Exception {
        // Combination: x=0, y=1
        Object actual = MathUtils.mulAndCheck(0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_426() throws Exception {
        // Combination: x=0, y=-1
        Object actual = MathUtils.mulAndCheck(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_427() throws Exception {
        // Combination: x=0, y=Integer.MAX_VALUE
        Object actual = MathUtils.mulAndCheck(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_428() throws Exception {
        // Combination: x=0, y=Integer.MIN_VALUE
        Object actual = MathUtils.mulAndCheck(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_429() throws Exception {
        // Combination: x=1, y=0
        Object actual = MathUtils.mulAndCheck(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_430() throws Exception {
        // Combination: x=1, y=1
        Object actual = MathUtils.mulAndCheck(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_431() throws Exception {
        // Combination: x=1, y=-1
        Object actual = MathUtils.mulAndCheck(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_432() throws Exception {
        // Combination: x=1, y=Integer.MAX_VALUE
        Object actual = MathUtils.mulAndCheck(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_433() throws Exception {
        // Combination: x=1, y=Integer.MIN_VALUE
        Object actual = MathUtils.mulAndCheck(1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_434() throws Exception {
        // Combination: x=-1, y=0
        Object actual = MathUtils.mulAndCheck(-1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_435() throws Exception {
        // Combination: x=-1, y=1
        Object actual = MathUtils.mulAndCheck(-1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_436() throws Exception {
        // Combination: x=-1, y=-1
        Object actual = MathUtils.mulAndCheck(-1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_437() throws Exception {
        // Combination: x=-1, y=Integer.MAX_VALUE
        Object actual = MathUtils.mulAndCheck(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_438() throws Exception {
        // Combination: x=-1, y=Integer.MIN_VALUE
        try {
            MathUtils.mulAndCheck(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_439() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=0
        Object actual = MathUtils.mulAndCheck(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_440() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=1
        Object actual = MathUtils.mulAndCheck(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_441() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=-1
        Object actual = MathUtils.mulAndCheck(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_442() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=Integer.MAX_VALUE
        try {
            MathUtils.mulAndCheck(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_443() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=Integer.MIN_VALUE
        try {
            MathUtils.mulAndCheck(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_444() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=0
        Object actual = MathUtils.mulAndCheck(Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_445() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=1
        Object actual = MathUtils.mulAndCheck(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_446() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=-1
        try {
            MathUtils.mulAndCheck(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_447() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=Integer.MAX_VALUE
        try {
            MathUtils.mulAndCheck(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_448() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=Integer.MIN_VALUE
        try {
            MathUtils.mulAndCheck(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_449() throws Exception {
        // Combination: a=0L, b=0L
        Object actual = MathUtils.mulAndCheck(0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_450() throws Exception {
        // Combination: a=0L, b=1L
        Object actual = MathUtils.mulAndCheck(0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_451() throws Exception {
        // Combination: a=0L, b=-1L
        Object actual = MathUtils.mulAndCheck(0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_452() throws Exception {
        // Combination: a=0L, b=Long.MAX_VALUE
        Object actual = MathUtils.mulAndCheck(0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_453() throws Exception {
        // Combination: a=0L, b=Long.MIN_VALUE
        Object actual = MathUtils.mulAndCheck(0L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_454() throws Exception {
        // Combination: a=1L, b=0L
        Object actual = MathUtils.mulAndCheck(1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_455() throws Exception {
        // Combination: a=1L, b=1L
        Object actual = MathUtils.mulAndCheck(1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_456() throws Exception {
        // Combination: a=1L, b=-1L
        Object actual = MathUtils.mulAndCheck(1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_457() throws Exception {
        // Combination: a=1L, b=Long.MAX_VALUE
        Object actual = MathUtils.mulAndCheck(1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_458() throws Exception {
        // Combination: a=1L, b=Long.MIN_VALUE
        Object actual = MathUtils.mulAndCheck(1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_459() throws Exception {
        // Combination: a=-1L, b=0L
        Object actual = MathUtils.mulAndCheck(-1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_460() throws Exception {
        // Combination: a=-1L, b=1L
        Object actual = MathUtils.mulAndCheck(-1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_461() throws Exception {
        // Combination: a=-1L, b=-1L
        Object actual = MathUtils.mulAndCheck(-1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_462() throws Exception {
        // Combination: a=-1L, b=Long.MAX_VALUE
        Object actual = MathUtils.mulAndCheck(-1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_463() throws Exception {
        // Combination: a=-1L, b=Long.MIN_VALUE
        try {
            MathUtils.mulAndCheck(-1L, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_464() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=0L
        Object actual = MathUtils.mulAndCheck(Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_465() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=1L
        Object actual = MathUtils.mulAndCheck(Long.MAX_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_466() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=-1L
        Object actual = MathUtils.mulAndCheck(Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_467() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE
        try {
            MathUtils.mulAndCheck(Long.MAX_VALUE, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_468() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MIN_VALUE
        try {
            MathUtils.mulAndCheck(Long.MAX_VALUE, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_469() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=0L
        Object actual = MathUtils.mulAndCheck(Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_470() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=1L
        Object actual = MathUtils.mulAndCheck(Long.MIN_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_471() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=-1L
        try {
            MathUtils.mulAndCheck(Long.MIN_VALUE, -1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_472() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MAX_VALUE
        try {
            MathUtils.mulAndCheck(Long.MIN_VALUE, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_473() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MIN_VALUE
        try {
            MathUtils.mulAndCheck(Long.MIN_VALUE, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_474() throws Exception {
        // Combination: d=0.0d, scaleFactor=0
        Object actual = MathUtils.scalb(0.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_475() throws Exception {
        // Combination: d=0.0d, scaleFactor=1
        Object actual = MathUtils.scalb(0.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_476() throws Exception {
        // Combination: d=0.0d, scaleFactor=-1
        Object actual = MathUtils.scalb(0.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_477() throws Exception {
        // Combination: d=0.0d, scaleFactor=Integer.MAX_VALUE
        Object actual = MathUtils.scalb(0.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_478() throws Exception {
        // Combination: d=0.0d, scaleFactor=Integer.MIN_VALUE
        Object actual = MathUtils.scalb(0.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_479() throws Exception {
        // Combination: d=1.0d, scaleFactor=0
        Object actual = MathUtils.scalb(1.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_480() throws Exception {
        // Combination: d=1.0d, scaleFactor=1
        Object actual = MathUtils.scalb(1.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("2.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_481() throws Exception {
        // Combination: d=1.0d, scaleFactor=-1
        Object actual = MathUtils.scalb(1.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_482() throws Exception {
        // Combination: d=1.0d, scaleFactor=Integer.MAX_VALUE
        Object actual = MathUtils.scalb(1.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_483() throws Exception {
        // Combination: d=1.0d, scaleFactor=Integer.MIN_VALUE
        Object actual = MathUtils.scalb(1.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_484() throws Exception {
        // Combination: d=-1.0d, scaleFactor=0
        Object actual = MathUtils.scalb(-1.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_485() throws Exception {
        // Combination: d=-1.0d, scaleFactor=1
        Object actual = MathUtils.scalb(-1.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-2.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_486() throws Exception {
        // Combination: d=-1.0d, scaleFactor=-1
        Object actual = MathUtils.scalb(-1.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_487() throws Exception {
        // Combination: d=-1.0d, scaleFactor=Integer.MAX_VALUE
        Object actual = MathUtils.scalb(-1.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_488() throws Exception {
        // Combination: d=-1.0d, scaleFactor=Integer.MIN_VALUE
        Object actual = MathUtils.scalb(-1.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_489() throws Exception {
        // Combination: d=Double.NaN, scaleFactor=0
        Object actual = MathUtils.scalb(Double.NaN, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_490() throws Exception {
        // Combination: d=Double.NaN, scaleFactor=1
        Object actual = MathUtils.scalb(Double.NaN, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_491() throws Exception {
        // Combination: d=Double.NaN, scaleFactor=-1
        Object actual = MathUtils.scalb(Double.NaN, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_492() throws Exception {
        // Combination: d=Double.NaN, scaleFactor=Integer.MAX_VALUE
        Object actual = MathUtils.scalb(Double.NaN, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_493() throws Exception {
        // Combination: d=Double.NaN, scaleFactor=Integer.MIN_VALUE
        Object actual = MathUtils.scalb(Double.NaN, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_494() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, scaleFactor=0
        Object actual = MathUtils.scalb(Double.POSITIVE_INFINITY, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_495() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, scaleFactor=1
        Object actual = MathUtils.scalb(Double.POSITIVE_INFINITY, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_496() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, scaleFactor=-1
        Object actual = MathUtils.scalb(Double.POSITIVE_INFINITY, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_497() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, scaleFactor=Integer.MAX_VALUE
        Object actual = MathUtils.scalb(Double.POSITIVE_INFINITY, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_498() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, scaleFactor=Integer.MIN_VALUE
        Object actual = MathUtils.scalb(Double.POSITIVE_INFINITY, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_499() throws Exception {
        // Combination: a=0.0d, center=0.0d
        Object actual = MathUtils.normalizeAngle(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_500() throws Exception {
        // Combination: a=0.0d, center=1.0d
        Object actual = MathUtils.normalizeAngle(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_501() throws Exception {
        // Combination: a=0.0d, center=-1.0d
        Object actual = MathUtils.normalizeAngle(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_502() throws Exception {
        // Combination: a=0.0d, center=Double.NaN
        Object actual = MathUtils.normalizeAngle(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_503() throws Exception {
        // Combination: a=0.0d, center=Double.POSITIVE_INFINITY
        Object actual = MathUtils.normalizeAngle(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_504() throws Exception {
        // Combination: a=1.0d, center=0.0d
        Object actual = MathUtils.normalizeAngle(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_505() throws Exception {
        // Combination: a=1.0d, center=1.0d
        Object actual = MathUtils.normalizeAngle(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_506() throws Exception {
        // Combination: a=1.0d, center=-1.0d
        Object actual = MathUtils.normalizeAngle(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_507() throws Exception {
        // Combination: a=1.0d, center=Double.NaN
        Object actual = MathUtils.normalizeAngle(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_508() throws Exception {
        // Combination: a=1.0d, center=Double.POSITIVE_INFINITY
        Object actual = MathUtils.normalizeAngle(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_509() throws Exception {
        // Combination: a=-1.0d, center=0.0d
        Object actual = MathUtils.normalizeAngle(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_510() throws Exception {
        // Combination: a=-1.0d, center=1.0d
        Object actual = MathUtils.normalizeAngle(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_511() throws Exception {
        // Combination: a=-1.0d, center=-1.0d
        Object actual = MathUtils.normalizeAngle(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_512() throws Exception {
        // Combination: a=-1.0d, center=Double.NaN
        Object actual = MathUtils.normalizeAngle(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_513() throws Exception {
        // Combination: a=-1.0d, center=Double.POSITIVE_INFINITY
        Object actual = MathUtils.normalizeAngle(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_514() throws Exception {
        // Combination: a=Double.NaN, center=0.0d
        Object actual = MathUtils.normalizeAngle(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_515() throws Exception {
        // Combination: a=Double.NaN, center=1.0d
        Object actual = MathUtils.normalizeAngle(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_516() throws Exception {
        // Combination: a=Double.NaN, center=-1.0d
        Object actual = MathUtils.normalizeAngle(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_517() throws Exception {
        // Combination: a=Double.NaN, center=Double.NaN
        Object actual = MathUtils.normalizeAngle(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_518() throws Exception {
        // Combination: a=Double.NaN, center=Double.POSITIVE_INFINITY
        Object actual = MathUtils.normalizeAngle(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_519() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, center=0.0d
        Object actual = MathUtils.normalizeAngle(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_520() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, center=1.0d
        Object actual = MathUtils.normalizeAngle(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_521() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, center=-1.0d
        Object actual = MathUtils.normalizeAngle(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_522() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, center=Double.NaN
        Object actual = MathUtils.normalizeAngle(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_523() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, center=Double.POSITIVE_INFINITY
        Object actual = MathUtils.normalizeAngle(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_524() throws Exception {
        // Combination: x=0.0d, scale=0
        Object actual = MathUtils.round(0.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_525() throws Exception {
        // Combination: x=1.0d, scale=0
        Object actual = MathUtils.round(1.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_526() throws Exception {
        // Combination: x=-1.0d, scale=0
        Object actual = MathUtils.round(-1.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_527() throws Exception {
        // Combination: x=Double.NaN, scale=0
        Object actual = MathUtils.round(Double.NaN, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_528() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=0
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_529() throws Exception {
        // Combination: x=0.0d, scale=1
        Object actual = MathUtils.round(0.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_530() throws Exception {
        // Combination: x=1.0d, scale=1
        Object actual = MathUtils.round(1.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_531() throws Exception {
        // Combination: x=-1.0d, scale=1
        Object actual = MathUtils.round(-1.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_532() throws Exception {
        // Combination: x=Double.NaN, scale=1
        Object actual = MathUtils.round(Double.NaN, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_533() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=1
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_534() throws Exception {
        // Combination: x=0.0d, scale=-1
        Object actual = MathUtils.round(0.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_535() throws Exception {
        // Combination: x=1.0d, scale=-1
        Object actual = MathUtils.round(1.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_536() throws Exception {
        // Combination: x=-1.0d, scale=-1
        Object actual = MathUtils.round(-1.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_537() throws Exception {
        // Combination: x=Double.NaN, scale=-1
        Object actual = MathUtils.round(Double.NaN, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_538() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=-1
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_539() throws Exception {
        // Combination: x=0.0d, scale=Integer.MAX_VALUE
        Object actual = MathUtils.round(0.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_540() throws Exception {
        // Combination: x=1.0d, scale=Integer.MAX_VALUE
        try {
            MathUtils.round(1.0d, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_541() throws Exception {
        // Combination: x=-1.0d, scale=Integer.MAX_VALUE
        try {
            MathUtils.round(-1.0d, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_542() throws Exception {
        // Combination: x=Double.NaN, scale=Integer.MAX_VALUE
        Object actual = MathUtils.round(Double.NaN, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_543() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=Integer.MAX_VALUE
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_544() throws Exception {
        // Combination: x=0.0d, scale=Integer.MIN_VALUE
        Object actual = MathUtils.round(0.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_545() throws Exception {
        // Combination: x=1.0d, scale=Integer.MIN_VALUE
        try {
            MathUtils.round(1.0d, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_546() throws Exception {
        // Combination: x=-1.0d, scale=Integer.MIN_VALUE
        try {
            MathUtils.round(-1.0d, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_547() throws Exception {
        // Combination: x=Double.NaN, scale=Integer.MIN_VALUE
        Object actual = MathUtils.round(Double.NaN, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_548() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=Integer.MIN_VALUE
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_549() throws Exception {
        // Combination: x=0.0d, scale=0, roundingMethod=0
        Object actual = MathUtils.round(0.0d, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_550() throws Exception {
        // Combination: x=1.0d, scale=1, roundingMethod=0
        Object actual = MathUtils.round(1.0d, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_551() throws Exception {
        // Combination: x=-1.0d, scale=-1, roundingMethod=0
        Object actual = MathUtils.round(-1.0d, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-10.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_552() throws Exception {
        // Combination: x=Double.NaN, scale=Integer.MAX_VALUE, roundingMethod=0
        Object actual = MathUtils.round(Double.NaN, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_553() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=Integer.MIN_VALUE, roundingMethod=0
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_554() throws Exception {
        // Combination: x=1.0d, scale=0, roundingMethod=1
        Object actual = MathUtils.round(1.0d, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_555() throws Exception {
        // Combination: x=0.0d, scale=1, roundingMethod=1
        Object actual = MathUtils.round(0.0d, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_556() throws Exception {
        // Combination: x=Double.NaN, scale=-1, roundingMethod=1
        Object actual = MathUtils.round(Double.NaN, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_557() throws Exception {
        // Combination: x=-1.0d, scale=Integer.MAX_VALUE, roundingMethod=1
        try {
            MathUtils.round(-1.0d, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_558() throws Exception {
        // Combination: x=0.0d, scale=Integer.MIN_VALUE, roundingMethod=1
        Object actual = MathUtils.round(0.0d, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_559() throws Exception {
        // Combination: x=-1.0d, scale=0, roundingMethod=-1
        try {
            MathUtils.round(-1.0d, 0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_560() throws Exception {
        // Combination: x=Double.NaN, scale=1, roundingMethod=-1
        Object actual = MathUtils.round(Double.NaN, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_561() throws Exception {
        // Combination: x=0.0d, scale=-1, roundingMethod=-1
        try {
            MathUtils.round(0.0d, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_562() throws Exception {
        // Combination: x=1.0d, scale=Integer.MAX_VALUE, roundingMethod=-1
        try {
            MathUtils.round(1.0d, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_563() throws Exception {
        // Combination: x=1.0d, scale=Integer.MIN_VALUE, roundingMethod=-1
        try {
            MathUtils.round(1.0d, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_564() throws Exception {
        // Combination: x=Double.NaN, scale=0, roundingMethod=Integer.MAX_VALUE
        Object actual = MathUtils.round(Double.NaN, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_565() throws Exception {
        // Combination: x=-1.0d, scale=1, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(-1.0d, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_566() throws Exception {
        // Combination: x=1.0d, scale=-1, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(1.0d, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_567() throws Exception {
        // Combination: x=0.0d, scale=Integer.MAX_VALUE, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(0.0d, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_568() throws Exception {
        // Combination: x=-1.0d, scale=Integer.MIN_VALUE, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(-1.0d, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_569() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=0, roundingMethod=Integer.MIN_VALUE
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_570() throws Exception {
        // Combination: x=0.0d, scale=1, roundingMethod=Integer.MIN_VALUE
        try {
            MathUtils.round(0.0d, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_571() throws Exception {
        // Combination: x=1.0d, scale=-1, roundingMethod=Integer.MIN_VALUE
        try {
            MathUtils.round(1.0d, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_572() throws Exception {
        // Combination: x=-1.0d, scale=Integer.MAX_VALUE, roundingMethod=Integer.MIN_VALUE
        try {
            MathUtils.round(-1.0d, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_573() throws Exception {
        // Combination: x=Double.NaN, scale=Integer.MIN_VALUE, roundingMethod=Integer.MIN_VALUE
        Object actual = MathUtils.round(Double.NaN, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_574() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=1, roundingMethod=1
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_575() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=-1, roundingMethod=-1
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_576() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=Integer.MAX_VALUE, roundingMethod=Integer.MAX_VALUE
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_577() throws Exception {
        // Combination: x=0.0f, scale=0
        Object actual = MathUtils.round(0.0f, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_578() throws Exception {
        // Combination: x=1.0f, scale=0
        Object actual = MathUtils.round(1.0f, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_579() throws Exception {
        // Combination: x=-1.0f, scale=0
        Object actual = MathUtils.round(-1.0f, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_580() throws Exception {
        // Combination: x=Float.NaN, scale=0
        Object actual = MathUtils.round(Float.NaN, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_581() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=0
        Object actual = MathUtils.round(Float.POSITIVE_INFINITY, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_582() throws Exception {
        // Combination: x=0.0f, scale=1
        Object actual = MathUtils.round(0.0f, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_583() throws Exception {
        // Combination: x=1.0f, scale=1
        Object actual = MathUtils.round(1.0f, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_584() throws Exception {
        // Combination: x=-1.0f, scale=1
        Object actual = MathUtils.round(-1.0f, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_585() throws Exception {
        // Combination: x=Float.NaN, scale=1
        Object actual = MathUtils.round(Float.NaN, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_586() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=1
        Object actual = MathUtils.round(Float.POSITIVE_INFINITY, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_587() throws Exception {
        // Combination: x=0.0f, scale=-1
        Object actual = MathUtils.round(0.0f, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_588() throws Exception {
        // Combination: x=1.0f, scale=-1
        Object actual = MathUtils.round(1.0f, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_589() throws Exception {
        // Combination: x=-1.0f, scale=-1
        Object actual = MathUtils.round(-1.0f, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_590() throws Exception {
        // Combination: x=Float.NaN, scale=-1
        Object actual = MathUtils.round(Float.NaN, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_591() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=-1
        Object actual = MathUtils.round(Float.POSITIVE_INFINITY, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_592() throws Exception {
        // Combination: x=0.0f, scale=Integer.MAX_VALUE
        Object actual = MathUtils.round(0.0f, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_593() throws Exception {
        // Combination: x=1.0f, scale=Integer.MAX_VALUE
        Object actual = MathUtils.round(1.0f, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_594() throws Exception {
        // Combination: x=-1.0f, scale=Integer.MAX_VALUE
        Object actual = MathUtils.round(-1.0f, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_595() throws Exception {
        // Combination: x=Float.NaN, scale=Integer.MAX_VALUE
        Object actual = MathUtils.round(Float.NaN, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_596() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=Integer.MAX_VALUE
        Object actual = MathUtils.round(Float.POSITIVE_INFINITY, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_597() throws Exception {
        // Combination: x=0.0f, scale=Integer.MIN_VALUE
        Object actual = MathUtils.round(0.0f, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_598() throws Exception {
        // Combination: x=1.0f, scale=Integer.MIN_VALUE
        Object actual = MathUtils.round(1.0f, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_599() throws Exception {
        // Combination: x=-1.0f, scale=Integer.MIN_VALUE
        Object actual = MathUtils.round(-1.0f, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_600() throws Exception {
        // Combination: x=Float.NaN, scale=Integer.MIN_VALUE
        Object actual = MathUtils.round(Float.NaN, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_601() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=Integer.MIN_VALUE
        Object actual = MathUtils.round(Float.POSITIVE_INFINITY, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_602() throws Exception {
        // Combination: x=0.0f, scale=0, roundingMethod=0
        Object actual = MathUtils.round(0.0f, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_603() throws Exception {
        // Combination: x=1.0f, scale=1, roundingMethod=0
        Object actual = MathUtils.round(1.0f, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_604() throws Exception {
        // Combination: x=-1.0f, scale=-1, roundingMethod=0
        Object actual = MathUtils.round(-1.0f, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-10.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_605() throws Exception {
        // Combination: x=Float.NaN, scale=Integer.MAX_VALUE, roundingMethod=0
        Object actual = MathUtils.round(Float.NaN, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_606() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=Integer.MIN_VALUE, roundingMethod=0
        Object actual = MathUtils.round(Float.POSITIVE_INFINITY, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_607() throws Exception {
        // Combination: x=1.0f, scale=0, roundingMethod=1
        Object actual = MathUtils.round(1.0f, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_608() throws Exception {
        // Combination: x=0.0f, scale=1, roundingMethod=1
        Object actual = MathUtils.round(0.0f, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-0.1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_609() throws Exception {
        // Combination: x=Float.NaN, scale=-1, roundingMethod=1
        Object actual = MathUtils.round(Float.NaN, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_610() throws Exception {
        // Combination: x=-1.0f, scale=Integer.MAX_VALUE, roundingMethod=1
        Object actual = MathUtils.round(-1.0f, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_611() throws Exception {
        // Combination: x=0.0f, scale=Integer.MIN_VALUE, roundingMethod=1
        Object actual = MathUtils.round(0.0f, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_612() throws Exception {
        // Combination: x=-1.0f, scale=0, roundingMethod=-1
        try {
            MathUtils.round(-1.0f, 0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_613() throws Exception {
        // Combination: x=Float.NaN, scale=1, roundingMethod=-1
        try {
            MathUtils.round(Float.NaN, 1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_614() throws Exception {
        // Combination: x=0.0f, scale=-1, roundingMethod=-1
        try {
            MathUtils.round(0.0f, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_615() throws Exception {
        // Combination: x=1.0f, scale=Integer.MAX_VALUE, roundingMethod=-1
        try {
            MathUtils.round(1.0f, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_616() throws Exception {
        // Combination: x=1.0f, scale=Integer.MIN_VALUE, roundingMethod=-1
        try {
            MathUtils.round(1.0f, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_617() throws Exception {
        // Combination: x=Float.NaN, scale=0, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(Float.NaN, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_618() throws Exception {
        // Combination: x=-1.0f, scale=1, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(-1.0f, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_619() throws Exception {
        // Combination: x=1.0f, scale=-1, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(1.0f, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_620() throws Exception {
        // Combination: x=0.0f, scale=Integer.MAX_VALUE, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(0.0f, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_621() throws Exception {
        // Combination: x=-1.0f, scale=Integer.MIN_VALUE, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(-1.0f, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_622() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=0, roundingMethod=Integer.MIN_VALUE
        try {
            MathUtils.round(Float.POSITIVE_INFINITY, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_623() throws Exception {
        // Combination: x=0.0f, scale=1, roundingMethod=Integer.MIN_VALUE
        try {
            MathUtils.round(0.0f, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_624() throws Exception {
        // Combination: x=1.0f, scale=-1, roundingMethod=Integer.MIN_VALUE
        try {
            MathUtils.round(1.0f, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_625() throws Exception {
        // Combination: x=-1.0f, scale=Integer.MAX_VALUE, roundingMethod=Integer.MIN_VALUE
        try {
            MathUtils.round(-1.0f, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_626() throws Exception {
        // Combination: x=Float.NaN, scale=Integer.MIN_VALUE, roundingMethod=Integer.MIN_VALUE
        try {
            MathUtils.round(Float.NaN, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_627() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=1, roundingMethod=1
        Object actual = MathUtils.round(Float.POSITIVE_INFINITY, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_628() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=-1, roundingMethod=-1
        try {
            MathUtils.round(Float.POSITIVE_INFINITY, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_629() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=Integer.MAX_VALUE, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(Float.POSITIVE_INFINITY, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_630() throws Exception {
        // Combination: x=0, y=0
        Object actual = MathUtils.subAndCheck(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_631() throws Exception {
        // Combination: x=0, y=1
        Object actual = MathUtils.subAndCheck(0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_632() throws Exception {
        // Combination: x=0, y=-1
        Object actual = MathUtils.subAndCheck(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_633() throws Exception {
        // Combination: x=0, y=Integer.MAX_VALUE
        Object actual = MathUtils.subAndCheck(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_634() throws Exception {
        // Combination: x=0, y=Integer.MIN_VALUE
        try {
            MathUtils.subAndCheck(0, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_635() throws Exception {
        // Combination: x=1, y=0
        Object actual = MathUtils.subAndCheck(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_636() throws Exception {
        // Combination: x=1, y=1
        Object actual = MathUtils.subAndCheck(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_637() throws Exception {
        // Combination: x=1, y=-1
        Object actual = MathUtils.subAndCheck(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_638() throws Exception {
        // Combination: x=1, y=Integer.MAX_VALUE
        Object actual = MathUtils.subAndCheck(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483646", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_639() throws Exception {
        // Combination: x=1, y=Integer.MIN_VALUE
        try {
            MathUtils.subAndCheck(1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_640() throws Exception {
        // Combination: x=-1, y=0
        Object actual = MathUtils.subAndCheck(-1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_641() throws Exception {
        // Combination: x=-1, y=1
        Object actual = MathUtils.subAndCheck(-1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_642() throws Exception {
        // Combination: x=-1, y=-1
        Object actual = MathUtils.subAndCheck(-1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_643() throws Exception {
        // Combination: x=-1, y=Integer.MAX_VALUE
        Object actual = MathUtils.subAndCheck(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_644() throws Exception {
        // Combination: x=-1, y=Integer.MIN_VALUE
        Object actual = MathUtils.subAndCheck(-1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_645() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=0
        Object actual = MathUtils.subAndCheck(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_646() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=1
        Object actual = MathUtils.subAndCheck(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483646", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_647() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=-1
        try {
            MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_648() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=Integer.MAX_VALUE
        Object actual = MathUtils.subAndCheck(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_649() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=Integer.MIN_VALUE
        try {
            MathUtils.subAndCheck(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_650() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=0
        Object actual = MathUtils.subAndCheck(Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_651() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=1
        try {
            MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_652() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=-1
        Object actual = MathUtils.subAndCheck(Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_653() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=Integer.MAX_VALUE
        try {
            MathUtils.subAndCheck(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_654() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=Integer.MIN_VALUE
        Object actual = MathUtils.subAndCheck(Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_655() throws Exception {
        // Combination: a=0L, b=0L
        Object actual = MathUtils.subAndCheck(0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_656() throws Exception {
        // Combination: a=0L, b=1L
        Object actual = MathUtils.subAndCheck(0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_657() throws Exception {
        // Combination: a=0L, b=-1L
        Object actual = MathUtils.subAndCheck(0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_658() throws Exception {
        // Combination: a=0L, b=Long.MAX_VALUE
        Object actual = MathUtils.subAndCheck(0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_659() throws Exception {
        // Combination: a=0L, b=Long.MIN_VALUE
        try {
            MathUtils.subAndCheck(0L, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_660() throws Exception {
        // Combination: a=1L, b=0L
        Object actual = MathUtils.subAndCheck(1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_661() throws Exception {
        // Combination: a=1L, b=1L
        Object actual = MathUtils.subAndCheck(1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_662() throws Exception {
        // Combination: a=1L, b=-1L
        Object actual = MathUtils.subAndCheck(1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_663() throws Exception {
        // Combination: a=1L, b=Long.MAX_VALUE
        Object actual = MathUtils.subAndCheck(1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775806", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_664() throws Exception {
        // Combination: a=1L, b=Long.MIN_VALUE
        try {
            MathUtils.subAndCheck(1L, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_665() throws Exception {
        // Combination: a=-1L, b=0L
        Object actual = MathUtils.subAndCheck(-1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_666() throws Exception {
        // Combination: a=-1L, b=1L
        Object actual = MathUtils.subAndCheck(-1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_667() throws Exception {
        // Combination: a=-1L, b=-1L
        Object actual = MathUtils.subAndCheck(-1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_668() throws Exception {
        // Combination: a=-1L, b=Long.MAX_VALUE
        Object actual = MathUtils.subAndCheck(-1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_669() throws Exception {
        // Combination: a=-1L, b=Long.MIN_VALUE
        Object actual = MathUtils.subAndCheck(-1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_670() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=0L
        Object actual = MathUtils.subAndCheck(Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_671() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=1L
        Object actual = MathUtils.subAndCheck(Long.MAX_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775806", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_672() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=-1L
        try {
            MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_673() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE
        Object actual = MathUtils.subAndCheck(Long.MAX_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_674() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MIN_VALUE
        try {
            MathUtils.subAndCheck(Long.MAX_VALUE, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_675() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=0L
        Object actual = MathUtils.subAndCheck(Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_676() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=1L
        try {
            MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_677() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=-1L
        Object actual = MathUtils.subAndCheck(Long.MIN_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_678() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MAX_VALUE
        try {
            MathUtils.subAndCheck(Long.MIN_VALUE, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_679() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MIN_VALUE
        Object actual = MathUtils.subAndCheck(Long.MIN_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distance1_pairwise_680() throws Exception {
        // Combination: p1=new double[] {}, p2=new double[] {}
        Object actual = MathUtils.distance1(new double[] {}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distance1_pairwise_681() throws Exception {
        // Combination: p1=new double[] {}, p2=new double[] {1}
        Object actual = MathUtils.distance1(new double[] {}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distance1_pairwise_682() throws Exception {
        // Combination: p1=new double[] {1}, p2=new double[] {}
        try {
            MathUtils.distance1(new double[] {1}, new double[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_distance1_pairwise_683() throws Exception {
        // Combination: p1=new double[] {1}, p2=new double[] {1}
        Object actual = MathUtils.distance1(new double[] {1}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distance1_pairwise_684() throws Exception {
        // Combination: p1=new int[] {}, p2=new int[] {}
        Object actual = MathUtils.distance1(new int[] {}, new int[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distance1_pairwise_685() throws Exception {
        // Combination: p1=new int[] {}, p2=new int[] {1}
        Object actual = MathUtils.distance1(new int[] {}, new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distance1_pairwise_686() throws Exception {
        // Combination: p1=new int[] {1}, p2=new int[] {}
        try {
            MathUtils.distance1(new int[] {1}, new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_distance1_pairwise_687() throws Exception {
        // Combination: p1=new int[] {1}, p2=new int[] {1}
        Object actual = MathUtils.distance1(new int[] {1}, new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_688() throws Exception {
        // Combination: p1=new double[] {}, p2=new double[] {}
        Object actual = MathUtils.distance(new double[] {}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_689() throws Exception {
        // Combination: p1=new double[] {}, p2=new double[] {1}
        Object actual = MathUtils.distance(new double[] {}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_690() throws Exception {
        // Combination: p1=new double[] {1}, p2=new double[] {}
        try {
            MathUtils.distance(new double[] {1}, new double[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_691() throws Exception {
        // Combination: p1=new double[] {1}, p2=new double[] {1}
        Object actual = MathUtils.distance(new double[] {1}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_692() throws Exception {
        // Combination: p1=new int[] {}, p2=new int[] {}
        Object actual = MathUtils.distance(new int[] {}, new int[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_693() throws Exception {
        // Combination: p1=new int[] {}, p2=new int[] {1}
        Object actual = MathUtils.distance(new int[] {}, new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_694() throws Exception {
        // Combination: p1=new int[] {1}, p2=new int[] {}
        try {
            MathUtils.distance(new int[] {1}, new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_distance_pairwise_695() throws Exception {
        // Combination: p1=new int[] {1}, p2=new int[] {1}
        Object actual = MathUtils.distance(new int[] {1}, new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distanceInf_pairwise_696() throws Exception {
        // Combination: p1=new double[] {}, p2=new double[] {}
        Object actual = MathUtils.distanceInf(new double[] {}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distanceInf_pairwise_697() throws Exception {
        // Combination: p1=new double[] {}, p2=new double[] {1}
        Object actual = MathUtils.distanceInf(new double[] {}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distanceInf_pairwise_698() throws Exception {
        // Combination: p1=new double[] {1}, p2=new double[] {}
        try {
            MathUtils.distanceInf(new double[] {1}, new double[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_distanceInf_pairwise_699() throws Exception {
        // Combination: p1=new double[] {1}, p2=new double[] {1}
        Object actual = MathUtils.distanceInf(new double[] {1}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distanceInf_pairwise_700() throws Exception {
        // Combination: p1=new int[] {}, p2=new int[] {}
        Object actual = MathUtils.distanceInf(new int[] {}, new int[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distanceInf_pairwise_701() throws Exception {
        // Combination: p1=new int[] {}, p2=new int[] {1}
        Object actual = MathUtils.distanceInf(new int[] {}, new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_distanceInf_pairwise_702() throws Exception {
        // Combination: p1=new int[] {1}, p2=new int[] {}
        try {
            MathUtils.distanceInf(new int[] {1}, new int[] {});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_distanceInf_pairwise_703() throws Exception {
        // Combination: p1=new int[] {1}, p2=new int[] {1}
        Object actual = MathUtils.distanceInf(new int[] {1}, new int[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

}
