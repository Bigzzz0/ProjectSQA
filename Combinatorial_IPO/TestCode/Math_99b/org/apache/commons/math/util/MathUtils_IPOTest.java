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
    public void test_equals_pairwise_101() throws Exception {
        // Combination: x=0.0d, y=0.0d
        Object actual = MathUtils.equals(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_102() throws Exception {
        // Combination: x=0.0d, y=1.0d
        Object actual = MathUtils.equals(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_103() throws Exception {
        // Combination: x=0.0d, y=-1.0d
        Object actual = MathUtils.equals(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_104() throws Exception {
        // Combination: x=0.0d, y=Double.NaN
        Object actual = MathUtils.equals(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_105() throws Exception {
        // Combination: x=0.0d, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_106() throws Exception {
        // Combination: x=1.0d, y=0.0d
        Object actual = MathUtils.equals(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_107() throws Exception {
        // Combination: x=1.0d, y=1.0d
        Object actual = MathUtils.equals(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_108() throws Exception {
        // Combination: x=1.0d, y=-1.0d
        Object actual = MathUtils.equals(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_109() throws Exception {
        // Combination: x=1.0d, y=Double.NaN
        Object actual = MathUtils.equals(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_110() throws Exception {
        // Combination: x=1.0d, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_111() throws Exception {
        // Combination: x=-1.0d, y=0.0d
        Object actual = MathUtils.equals(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_112() throws Exception {
        // Combination: x=-1.0d, y=1.0d
        Object actual = MathUtils.equals(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_113() throws Exception {
        // Combination: x=-1.0d, y=-1.0d
        Object actual = MathUtils.equals(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_114() throws Exception {
        // Combination: x=-1.0d, y=Double.NaN
        Object actual = MathUtils.equals(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_115() throws Exception {
        // Combination: x=-1.0d, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_116() throws Exception {
        // Combination: x=Double.NaN, y=0.0d
        Object actual = MathUtils.equals(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_117() throws Exception {
        // Combination: x=Double.NaN, y=1.0d
        Object actual = MathUtils.equals(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_118() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d
        Object actual = MathUtils.equals(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_119() throws Exception {
        // Combination: x=Double.NaN, y=Double.NaN
        Object actual = MathUtils.equals(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_120() throws Exception {
        // Combination: x=Double.NaN, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_121() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=0.0d
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_122() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=1.0d
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_123() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=-1.0d
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_124() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.NaN
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_125() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_126() throws Exception {
        // Combination: x=0.0d, y=0.0d, eps=0.0d
        Object actual = MathUtils.equals(0.0d, 0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_127() throws Exception {
        // Combination: x=1.0d, y=1.0d, eps=0.0d
        Object actual = MathUtils.equals(1.0d, 1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_128() throws Exception {
        // Combination: x=-1.0d, y=-1.0d, eps=0.0d
        Object actual = MathUtils.equals(-1.0d, -1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_129() throws Exception {
        // Combination: x=Double.NaN, y=Double.NaN, eps=0.0d
        Object actual = MathUtils.equals(Double.NaN, Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_130() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.POSITIVE_INFINITY, eps=0.0d
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_131() throws Exception {
        // Combination: x=0.0d, y=1.0d, eps=1.0d
        Object actual = MathUtils.equals(0.0d, 1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_132() throws Exception {
        // Combination: x=1.0d, y=0.0d, eps=1.0d
        Object actual = MathUtils.equals(1.0d, 0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_133() throws Exception {
        // Combination: x=-1.0d, y=Double.NaN, eps=1.0d
        Object actual = MathUtils.equals(-1.0d, Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_134() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d, eps=1.0d
        Object actual = MathUtils.equals(Double.NaN, -1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_135() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=0.0d, eps=1.0d
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, 0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_136() throws Exception {
        // Combination: x=0.0d, y=-1.0d, eps=-1.0d
        Object actual = MathUtils.equals(0.0d, -1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_137() throws Exception {
        // Combination: x=1.0d, y=Double.NaN, eps=-1.0d
        Object actual = MathUtils.equals(1.0d, Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_138() throws Exception {
        // Combination: x=-1.0d, y=0.0d, eps=-1.0d
        Object actual = MathUtils.equals(-1.0d, 0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_139() throws Exception {
        // Combination: x=Double.NaN, y=1.0d, eps=-1.0d
        Object actual = MathUtils.equals(Double.NaN, 1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_140() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=1.0d, eps=-1.0d
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, 1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_141() throws Exception {
        // Combination: x=0.0d, y=Double.NaN, eps=Double.NaN
        Object actual = MathUtils.equals(0.0d, Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_142() throws Exception {
        // Combination: x=1.0d, y=-1.0d, eps=Double.NaN
        Object actual = MathUtils.equals(1.0d, -1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_143() throws Exception {
        // Combination: x=-1.0d, y=1.0d, eps=Double.NaN
        Object actual = MathUtils.equals(-1.0d, 1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_144() throws Exception {
        // Combination: x=Double.NaN, y=0.0d, eps=Double.NaN
        Object actual = MathUtils.equals(Double.NaN, 0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_145() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=-1.0d, eps=Double.NaN
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, -1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_146() throws Exception {
        // Combination: x=0.0d, y=Double.POSITIVE_INFINITY, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(0.0d, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_147() throws Exception {
        // Combination: x=1.0d, y=0.0d, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(1.0d, 0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_148() throws Exception {
        // Combination: x=-1.0d, y=1.0d, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(-1.0d, 1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_149() throws Exception {
        // Combination: x=Double.NaN, y=-1.0d, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(Double.NaN, -1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_150() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, y=Double.NaN, eps=Double.POSITIVE_INFINITY
        Object actual = MathUtils.equals(Double.POSITIVE_INFINITY, Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_151() throws Exception {
        // Combination: x=1.0d, y=Double.POSITIVE_INFINITY, eps=1.0d
        Object actual = MathUtils.equals(1.0d, Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_152() throws Exception {
        // Combination: x=-1.0d, y=Double.POSITIVE_INFINITY, eps=-1.0d
        Object actual = MathUtils.equals(-1.0d, Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_153() throws Exception {
        // Combination: x=Double.NaN, y=Double.POSITIVE_INFINITY, eps=Double.NaN
        Object actual = MathUtils.equals(Double.NaN, Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_154() throws Exception {
        // Combination: x=new double[] {}, y=new double[] {}
        Object actual = MathUtils.equals(new double[] {}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_155() throws Exception {
        // Combination: x=new double[] {}, y=new double[] {1}
        Object actual = MathUtils.equals(new double[] {}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_156() throws Exception {
        // Combination: x=new double[] {1}, y=new double[] {}
        Object actual = MathUtils.equals(new double[] {1}, new double[] {});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("false", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_equals_pairwise_157() throws Exception {
        // Combination: x=new double[] {1}, y=new double[] {1}
        Object actual = MathUtils.equals(new double[] {1}, new double[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.Boolean", actual.getClass().getName());
        assertEquals("true", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_158() throws Exception {
        // Combination: p=0, q=0
        Object actual = MathUtils.gcd(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_159() throws Exception {
        // Combination: p=0, q=1
        Object actual = MathUtils.gcd(0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_160() throws Exception {
        // Combination: p=0, q=-1
        Object actual = MathUtils.gcd(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_161() throws Exception {
        // Combination: p=0, q=Integer.MAX_VALUE
        Object actual = MathUtils.gcd(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_162() throws Exception {
        // Combination: p=0, q=Integer.MIN_VALUE
        try {
            MathUtils.gcd(0, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_163() throws Exception {
        // Combination: p=1, q=0
        Object actual = MathUtils.gcd(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_164() throws Exception {
        // Combination: p=1, q=1
        Object actual = MathUtils.gcd(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_165() throws Exception {
        // Combination: p=1, q=-1
        Object actual = MathUtils.gcd(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_166() throws Exception {
        // Combination: p=1, q=Integer.MAX_VALUE
        Object actual = MathUtils.gcd(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_167() throws Exception {
        // Combination: p=1, q=Integer.MIN_VALUE
        Object actual = MathUtils.gcd(1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_168() throws Exception {
        // Combination: p=-1, q=0
        Object actual = MathUtils.gcd(-1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_169() throws Exception {
        // Combination: p=-1, q=1
        Object actual = MathUtils.gcd(-1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_170() throws Exception {
        // Combination: p=-1, q=-1
        Object actual = MathUtils.gcd(-1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_171() throws Exception {
        // Combination: p=-1, q=Integer.MAX_VALUE
        Object actual = MathUtils.gcd(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_172() throws Exception {
        // Combination: p=-1, q=Integer.MIN_VALUE
        Object actual = MathUtils.gcd(-1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_173() throws Exception {
        // Combination: p=Integer.MAX_VALUE, q=0
        Object actual = MathUtils.gcd(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_174() throws Exception {
        // Combination: p=Integer.MAX_VALUE, q=1
        Object actual = MathUtils.gcd(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_175() throws Exception {
        // Combination: p=Integer.MAX_VALUE, q=-1
        Object actual = MathUtils.gcd(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_176() throws Exception {
        // Combination: p=Integer.MAX_VALUE, q=Integer.MAX_VALUE
        Object actual = MathUtils.gcd(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_177() throws Exception {
        // Combination: p=Integer.MAX_VALUE, q=Integer.MIN_VALUE
        Object actual = MathUtils.gcd(Integer.MAX_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_178() throws Exception {
        // Combination: p=Integer.MIN_VALUE, q=0
        try {
            MathUtils.gcd(Integer.MIN_VALUE, 0);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_179() throws Exception {
        // Combination: p=Integer.MIN_VALUE, q=1
        Object actual = MathUtils.gcd(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_180() throws Exception {
        // Combination: p=Integer.MIN_VALUE, q=-1
        Object actual = MathUtils.gcd(Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_181() throws Exception {
        // Combination: p=Integer.MIN_VALUE, q=Integer.MAX_VALUE
        Object actual = MathUtils.gcd(Integer.MIN_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_gcd_pairwise_182() throws Exception {
        // Combination: p=Integer.MIN_VALUE, q=Integer.MIN_VALUE
        try {
            MathUtils.gcd(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_183() throws Exception {
        // Combination: a=0, b=0
        Object actual = MathUtils.lcm(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_184() throws Exception {
        // Combination: a=0, b=1
        Object actual = MathUtils.lcm(0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_185() throws Exception {
        // Combination: a=0, b=-1
        Object actual = MathUtils.lcm(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_186() throws Exception {
        // Combination: a=0, b=Integer.MAX_VALUE
        Object actual = MathUtils.lcm(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_187() throws Exception {
        // Combination: a=0, b=Integer.MIN_VALUE
        Object actual = MathUtils.lcm(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_188() throws Exception {
        // Combination: a=1, b=0
        Object actual = MathUtils.lcm(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_189() throws Exception {
        // Combination: a=1, b=1
        Object actual = MathUtils.lcm(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_190() throws Exception {
        // Combination: a=1, b=-1
        Object actual = MathUtils.lcm(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_191() throws Exception {
        // Combination: a=1, b=Integer.MAX_VALUE
        Object actual = MathUtils.lcm(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_192() throws Exception {
        // Combination: a=1, b=Integer.MIN_VALUE
        try {
            MathUtils.lcm(1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_193() throws Exception {
        // Combination: a=-1, b=0
        Object actual = MathUtils.lcm(-1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_194() throws Exception {
        // Combination: a=-1, b=1
        Object actual = MathUtils.lcm(-1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_195() throws Exception {
        // Combination: a=-1, b=-1
        Object actual = MathUtils.lcm(-1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_196() throws Exception {
        // Combination: a=-1, b=Integer.MAX_VALUE
        Object actual = MathUtils.lcm(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_197() throws Exception {
        // Combination: a=-1, b=Integer.MIN_VALUE
        try {
            MathUtils.lcm(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_198() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=0
        Object actual = MathUtils.lcm(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_199() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=1
        Object actual = MathUtils.lcm(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_200() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=-1
        Object actual = MathUtils.lcm(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_201() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MAX_VALUE
        Object actual = MathUtils.lcm(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_202() throws Exception {
        // Combination: a=Integer.MAX_VALUE, b=Integer.MIN_VALUE
        try {
            MathUtils.lcm(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_203() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=0
        Object actual = MathUtils.lcm(Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_204() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=1
        try {
            MathUtils.lcm(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_205() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=-1
        try {
            MathUtils.lcm(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_206() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MAX_VALUE
        try {
            MathUtils.lcm(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_lcm_pairwise_207() throws Exception {
        // Combination: a=Integer.MIN_VALUE, b=Integer.MIN_VALUE
        try {
            MathUtils.lcm(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_208() throws Exception {
        // Combination: base=0.0d, x=0.0d
        Object actual = MathUtils.log(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_209() throws Exception {
        // Combination: base=0.0d, x=1.0d
        Object actual = MathUtils.log(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_210() throws Exception {
        // Combination: base=0.0d, x=-1.0d
        Object actual = MathUtils.log(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_211() throws Exception {
        // Combination: base=0.0d, x=Double.NaN
        Object actual = MathUtils.log(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_212() throws Exception {
        // Combination: base=0.0d, x=Double.POSITIVE_INFINITY
        Object actual = MathUtils.log(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_213() throws Exception {
        // Combination: base=1.0d, x=0.0d
        Object actual = MathUtils.log(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_214() throws Exception {
        // Combination: base=1.0d, x=1.0d
        Object actual = MathUtils.log(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_215() throws Exception {
        // Combination: base=1.0d, x=-1.0d
        Object actual = MathUtils.log(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_216() throws Exception {
        // Combination: base=1.0d, x=Double.NaN
        Object actual = MathUtils.log(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_217() throws Exception {
        // Combination: base=1.0d, x=Double.POSITIVE_INFINITY
        Object actual = MathUtils.log(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_218() throws Exception {
        // Combination: base=-1.0d, x=0.0d
        Object actual = MathUtils.log(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_219() throws Exception {
        // Combination: base=-1.0d, x=1.0d
        Object actual = MathUtils.log(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_220() throws Exception {
        // Combination: base=-1.0d, x=-1.0d
        Object actual = MathUtils.log(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_221() throws Exception {
        // Combination: base=-1.0d, x=Double.NaN
        Object actual = MathUtils.log(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_222() throws Exception {
        // Combination: base=-1.0d, x=Double.POSITIVE_INFINITY
        Object actual = MathUtils.log(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_223() throws Exception {
        // Combination: base=Double.NaN, x=0.0d
        Object actual = MathUtils.log(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_224() throws Exception {
        // Combination: base=Double.NaN, x=1.0d
        Object actual = MathUtils.log(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_225() throws Exception {
        // Combination: base=Double.NaN, x=-1.0d
        Object actual = MathUtils.log(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_226() throws Exception {
        // Combination: base=Double.NaN, x=Double.NaN
        Object actual = MathUtils.log(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_227() throws Exception {
        // Combination: base=Double.NaN, x=Double.POSITIVE_INFINITY
        Object actual = MathUtils.log(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_228() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=0.0d
        Object actual = MathUtils.log(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_229() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=1.0d
        Object actual = MathUtils.log(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_230() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=-1.0d
        Object actual = MathUtils.log(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_231() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=Double.NaN
        Object actual = MathUtils.log(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_log_pairwise_232() throws Exception {
        // Combination: base=Double.POSITIVE_INFINITY, x=Double.POSITIVE_INFINITY
        Object actual = MathUtils.log(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_233() throws Exception {
        // Combination: x=0, y=0
        Object actual = MathUtils.mulAndCheck(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_234() throws Exception {
        // Combination: x=0, y=1
        Object actual = MathUtils.mulAndCheck(0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_235() throws Exception {
        // Combination: x=0, y=-1
        Object actual = MathUtils.mulAndCheck(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_236() throws Exception {
        // Combination: x=0, y=Integer.MAX_VALUE
        Object actual = MathUtils.mulAndCheck(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_237() throws Exception {
        // Combination: x=0, y=Integer.MIN_VALUE
        Object actual = MathUtils.mulAndCheck(0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_238() throws Exception {
        // Combination: x=1, y=0
        Object actual = MathUtils.mulAndCheck(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_239() throws Exception {
        // Combination: x=1, y=1
        Object actual = MathUtils.mulAndCheck(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_240() throws Exception {
        // Combination: x=1, y=-1
        Object actual = MathUtils.mulAndCheck(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_241() throws Exception {
        // Combination: x=1, y=Integer.MAX_VALUE
        Object actual = MathUtils.mulAndCheck(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_242() throws Exception {
        // Combination: x=1, y=Integer.MIN_VALUE
        Object actual = MathUtils.mulAndCheck(1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_243() throws Exception {
        // Combination: x=-1, y=0
        Object actual = MathUtils.mulAndCheck(-1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_244() throws Exception {
        // Combination: x=-1, y=1
        Object actual = MathUtils.mulAndCheck(-1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_245() throws Exception {
        // Combination: x=-1, y=-1
        Object actual = MathUtils.mulAndCheck(-1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_246() throws Exception {
        // Combination: x=-1, y=Integer.MAX_VALUE
        Object actual = MathUtils.mulAndCheck(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_247() throws Exception {
        // Combination: x=-1, y=Integer.MIN_VALUE
        try {
            MathUtils.mulAndCheck(-1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_248() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=0
        Object actual = MathUtils.mulAndCheck(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_249() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=1
        Object actual = MathUtils.mulAndCheck(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_250() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=-1
        Object actual = MathUtils.mulAndCheck(Integer.MAX_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_251() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=Integer.MAX_VALUE
        try {
            MathUtils.mulAndCheck(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_252() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=Integer.MIN_VALUE
        try {
            MathUtils.mulAndCheck(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_253() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=0
        Object actual = MathUtils.mulAndCheck(Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_254() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=1
        Object actual = MathUtils.mulAndCheck(Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_255() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=-1
        try {
            MathUtils.mulAndCheck(Integer.MIN_VALUE, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_256() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=Integer.MAX_VALUE
        try {
            MathUtils.mulAndCheck(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_257() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=Integer.MIN_VALUE
        try {
            MathUtils.mulAndCheck(Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_258() throws Exception {
        // Combination: a=0L, b=0L
        Object actual = MathUtils.mulAndCheck(0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_259() throws Exception {
        // Combination: a=0L, b=1L
        Object actual = MathUtils.mulAndCheck(0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_260() throws Exception {
        // Combination: a=0L, b=-1L
        Object actual = MathUtils.mulAndCheck(0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_261() throws Exception {
        // Combination: a=0L, b=Long.MAX_VALUE
        Object actual = MathUtils.mulAndCheck(0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_262() throws Exception {
        // Combination: a=0L, b=Long.MIN_VALUE
        Object actual = MathUtils.mulAndCheck(0L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_263() throws Exception {
        // Combination: a=1L, b=0L
        Object actual = MathUtils.mulAndCheck(1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_264() throws Exception {
        // Combination: a=1L, b=1L
        Object actual = MathUtils.mulAndCheck(1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_265() throws Exception {
        // Combination: a=1L, b=-1L
        Object actual = MathUtils.mulAndCheck(1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_266() throws Exception {
        // Combination: a=1L, b=Long.MAX_VALUE
        Object actual = MathUtils.mulAndCheck(1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_267() throws Exception {
        // Combination: a=1L, b=Long.MIN_VALUE
        Object actual = MathUtils.mulAndCheck(1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_268() throws Exception {
        // Combination: a=-1L, b=0L
        Object actual = MathUtils.mulAndCheck(-1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_269() throws Exception {
        // Combination: a=-1L, b=1L
        Object actual = MathUtils.mulAndCheck(-1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_270() throws Exception {
        // Combination: a=-1L, b=-1L
        Object actual = MathUtils.mulAndCheck(-1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_271() throws Exception {
        // Combination: a=-1L, b=Long.MAX_VALUE
        Object actual = MathUtils.mulAndCheck(-1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_272() throws Exception {
        // Combination: a=-1L, b=Long.MIN_VALUE
        try {
            MathUtils.mulAndCheck(-1L, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_273() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=0L
        Object actual = MathUtils.mulAndCheck(Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_274() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=1L
        Object actual = MathUtils.mulAndCheck(Long.MAX_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_275() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=-1L
        Object actual = MathUtils.mulAndCheck(Long.MAX_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_276() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE
        try {
            MathUtils.mulAndCheck(Long.MAX_VALUE, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_277() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MIN_VALUE
        try {
            MathUtils.mulAndCheck(Long.MAX_VALUE, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_278() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=0L
        Object actual = MathUtils.mulAndCheck(Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_279() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=1L
        Object actual = MathUtils.mulAndCheck(Long.MIN_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_280() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=-1L
        try {
            MathUtils.mulAndCheck(Long.MIN_VALUE, -1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_281() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MAX_VALUE
        try {
            MathUtils.mulAndCheck(Long.MIN_VALUE, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_mulAndCheck_pairwise_282() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MIN_VALUE
        try {
            MathUtils.mulAndCheck(Long.MIN_VALUE, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_283() throws Exception {
        // Combination: d=0.0d, direction=0.0d
        Object actual = MathUtils.nextAfter(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("4.9E-324", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_284() throws Exception {
        // Combination: d=0.0d, direction=1.0d
        Object actual = MathUtils.nextAfter(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("4.9E-324", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_285() throws Exception {
        // Combination: d=0.0d, direction=-1.0d
        Object actual = MathUtils.nextAfter(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-4.9E-324", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_286() throws Exception {
        // Combination: d=0.0d, direction=Double.NaN
        Object actual = MathUtils.nextAfter(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("4.9E-324", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_287() throws Exception {
        // Combination: d=0.0d, direction=Double.POSITIVE_INFINITY
        Object actual = MathUtils.nextAfter(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("4.9E-324", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_288() throws Exception {
        // Combination: d=1.0d, direction=0.0d
        Object actual = MathUtils.nextAfter(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.9999999999999999", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_289() throws Exception {
        // Combination: d=1.0d, direction=1.0d
        Object actual = MathUtils.nextAfter(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0000000000000002", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_290() throws Exception {
        // Combination: d=1.0d, direction=-1.0d
        Object actual = MathUtils.nextAfter(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.9999999999999999", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_291() throws Exception {
        // Combination: d=1.0d, direction=Double.NaN
        Object actual = MathUtils.nextAfter(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.9999999999999999", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_292() throws Exception {
        // Combination: d=1.0d, direction=Double.POSITIVE_INFINITY
        Object actual = MathUtils.nextAfter(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0000000000000002", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_293() throws Exception {
        // Combination: d=-1.0d, direction=0.0d
        Object actual = MathUtils.nextAfter(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.9999999999999999", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_294() throws Exception {
        // Combination: d=-1.0d, direction=1.0d
        Object actual = MathUtils.nextAfter(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.9999999999999999", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_295() throws Exception {
        // Combination: d=-1.0d, direction=-1.0d
        Object actual = MathUtils.nextAfter(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0000000000000002", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_296() throws Exception {
        // Combination: d=-1.0d, direction=Double.NaN
        Object actual = MathUtils.nextAfter(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.9999999999999999", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_297() throws Exception {
        // Combination: d=-1.0d, direction=Double.POSITIVE_INFINITY
        Object actual = MathUtils.nextAfter(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.9999999999999999", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_298() throws Exception {
        // Combination: d=Double.NaN, direction=0.0d
        Object actual = MathUtils.nextAfter(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_299() throws Exception {
        // Combination: d=Double.NaN, direction=1.0d
        Object actual = MathUtils.nextAfter(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_300() throws Exception {
        // Combination: d=Double.NaN, direction=-1.0d
        Object actual = MathUtils.nextAfter(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_301() throws Exception {
        // Combination: d=Double.NaN, direction=Double.NaN
        Object actual = MathUtils.nextAfter(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_302() throws Exception {
        // Combination: d=Double.NaN, direction=Double.POSITIVE_INFINITY
        Object actual = MathUtils.nextAfter(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_303() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, direction=0.0d
        Object actual = MathUtils.nextAfter(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_304() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, direction=1.0d
        Object actual = MathUtils.nextAfter(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_305() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, direction=-1.0d
        Object actual = MathUtils.nextAfter(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_306() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, direction=Double.NaN
        Object actual = MathUtils.nextAfter(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_nextAfter_pairwise_307() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, direction=Double.POSITIVE_INFINITY
        Object actual = MathUtils.nextAfter(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_308() throws Exception {
        // Combination: d=0.0d, scaleFactor=0
        Object actual = MathUtils.scalb(0.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_309() throws Exception {
        // Combination: d=0.0d, scaleFactor=1
        Object actual = MathUtils.scalb(0.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_310() throws Exception {
        // Combination: d=0.0d, scaleFactor=-1
        Object actual = MathUtils.scalb(0.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_311() throws Exception {
        // Combination: d=0.0d, scaleFactor=Integer.MAX_VALUE
        Object actual = MathUtils.scalb(0.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_312() throws Exception {
        // Combination: d=0.0d, scaleFactor=Integer.MIN_VALUE
        Object actual = MathUtils.scalb(0.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_313() throws Exception {
        // Combination: d=1.0d, scaleFactor=0
        Object actual = MathUtils.scalb(1.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_314() throws Exception {
        // Combination: d=1.0d, scaleFactor=1
        Object actual = MathUtils.scalb(1.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("2.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_315() throws Exception {
        // Combination: d=1.0d, scaleFactor=-1
        Object actual = MathUtils.scalb(1.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_316() throws Exception {
        // Combination: d=1.0d, scaleFactor=Integer.MAX_VALUE
        Object actual = MathUtils.scalb(1.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_317() throws Exception {
        // Combination: d=1.0d, scaleFactor=Integer.MIN_VALUE
        Object actual = MathUtils.scalb(1.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_318() throws Exception {
        // Combination: d=-1.0d, scaleFactor=0
        Object actual = MathUtils.scalb(-1.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_319() throws Exception {
        // Combination: d=-1.0d, scaleFactor=1
        Object actual = MathUtils.scalb(-1.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-2.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_320() throws Exception {
        // Combination: d=-1.0d, scaleFactor=-1
        Object actual = MathUtils.scalb(-1.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_321() throws Exception {
        // Combination: d=-1.0d, scaleFactor=Integer.MAX_VALUE
        Object actual = MathUtils.scalb(-1.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-0.5", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_322() throws Exception {
        // Combination: d=-1.0d, scaleFactor=Integer.MIN_VALUE
        Object actual = MathUtils.scalb(-1.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_323() throws Exception {
        // Combination: d=Double.NaN, scaleFactor=0
        Object actual = MathUtils.scalb(Double.NaN, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_324() throws Exception {
        // Combination: d=Double.NaN, scaleFactor=1
        Object actual = MathUtils.scalb(Double.NaN, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_325() throws Exception {
        // Combination: d=Double.NaN, scaleFactor=-1
        Object actual = MathUtils.scalb(Double.NaN, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_326() throws Exception {
        // Combination: d=Double.NaN, scaleFactor=Integer.MAX_VALUE
        Object actual = MathUtils.scalb(Double.NaN, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_327() throws Exception {
        // Combination: d=Double.NaN, scaleFactor=Integer.MIN_VALUE
        Object actual = MathUtils.scalb(Double.NaN, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_328() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, scaleFactor=0
        Object actual = MathUtils.scalb(Double.POSITIVE_INFINITY, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_329() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, scaleFactor=1
        Object actual = MathUtils.scalb(Double.POSITIVE_INFINITY, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_330() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, scaleFactor=-1
        Object actual = MathUtils.scalb(Double.POSITIVE_INFINITY, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_331() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, scaleFactor=Integer.MAX_VALUE
        Object actual = MathUtils.scalb(Double.POSITIVE_INFINITY, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_scalb_pairwise_332() throws Exception {
        // Combination: d=Double.POSITIVE_INFINITY, scaleFactor=Integer.MIN_VALUE
        Object actual = MathUtils.scalb(Double.POSITIVE_INFINITY, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_333() throws Exception {
        // Combination: a=0.0d, center=0.0d
        Object actual = MathUtils.normalizeAngle(0.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_334() throws Exception {
        // Combination: a=0.0d, center=1.0d
        Object actual = MathUtils.normalizeAngle(0.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_335() throws Exception {
        // Combination: a=0.0d, center=-1.0d
        Object actual = MathUtils.normalizeAngle(0.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_336() throws Exception {
        // Combination: a=0.0d, center=Double.NaN
        Object actual = MathUtils.normalizeAngle(0.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_337() throws Exception {
        // Combination: a=0.0d, center=Double.POSITIVE_INFINITY
        Object actual = MathUtils.normalizeAngle(0.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_338() throws Exception {
        // Combination: a=1.0d, center=0.0d
        Object actual = MathUtils.normalizeAngle(1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_339() throws Exception {
        // Combination: a=1.0d, center=1.0d
        Object actual = MathUtils.normalizeAngle(1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_340() throws Exception {
        // Combination: a=1.0d, center=-1.0d
        Object actual = MathUtils.normalizeAngle(1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_341() throws Exception {
        // Combination: a=1.0d, center=Double.NaN
        Object actual = MathUtils.normalizeAngle(1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_342() throws Exception {
        // Combination: a=1.0d, center=Double.POSITIVE_INFINITY
        Object actual = MathUtils.normalizeAngle(1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_343() throws Exception {
        // Combination: a=-1.0d, center=0.0d
        Object actual = MathUtils.normalizeAngle(-1.0d, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_344() throws Exception {
        // Combination: a=-1.0d, center=1.0d
        Object actual = MathUtils.normalizeAngle(-1.0d, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_345() throws Exception {
        // Combination: a=-1.0d, center=-1.0d
        Object actual = MathUtils.normalizeAngle(-1.0d, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_346() throws Exception {
        // Combination: a=-1.0d, center=Double.NaN
        Object actual = MathUtils.normalizeAngle(-1.0d, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_347() throws Exception {
        // Combination: a=-1.0d, center=Double.POSITIVE_INFINITY
        Object actual = MathUtils.normalizeAngle(-1.0d, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_348() throws Exception {
        // Combination: a=Double.NaN, center=0.0d
        Object actual = MathUtils.normalizeAngle(Double.NaN, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_349() throws Exception {
        // Combination: a=Double.NaN, center=1.0d
        Object actual = MathUtils.normalizeAngle(Double.NaN, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_350() throws Exception {
        // Combination: a=Double.NaN, center=-1.0d
        Object actual = MathUtils.normalizeAngle(Double.NaN, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_351() throws Exception {
        // Combination: a=Double.NaN, center=Double.NaN
        Object actual = MathUtils.normalizeAngle(Double.NaN, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_352() throws Exception {
        // Combination: a=Double.NaN, center=Double.POSITIVE_INFINITY
        Object actual = MathUtils.normalizeAngle(Double.NaN, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_353() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, center=0.0d
        Object actual = MathUtils.normalizeAngle(Double.POSITIVE_INFINITY, 0.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_354() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, center=1.0d
        Object actual = MathUtils.normalizeAngle(Double.POSITIVE_INFINITY, 1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_355() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, center=-1.0d
        Object actual = MathUtils.normalizeAngle(Double.POSITIVE_INFINITY, -1.0d);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_356() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, center=Double.NaN
        Object actual = MathUtils.normalizeAngle(Double.POSITIVE_INFINITY, Double.NaN);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_normalizeAngle_pairwise_357() throws Exception {
        // Combination: a=Double.POSITIVE_INFINITY, center=Double.POSITIVE_INFINITY
        Object actual = MathUtils.normalizeAngle(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_358() throws Exception {
        // Combination: x=0.0d, scale=0
        Object actual = MathUtils.round(0.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_359() throws Exception {
        // Combination: x=1.0d, scale=0
        Object actual = MathUtils.round(1.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_360() throws Exception {
        // Combination: x=-1.0d, scale=0
        Object actual = MathUtils.round(-1.0d, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_361() throws Exception {
        // Combination: x=Double.NaN, scale=0
        Object actual = MathUtils.round(Double.NaN, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_362() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=0
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_363() throws Exception {
        // Combination: x=0.0d, scale=1
        Object actual = MathUtils.round(0.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_364() throws Exception {
        // Combination: x=1.0d, scale=1
        Object actual = MathUtils.round(1.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_365() throws Exception {
        // Combination: x=-1.0d, scale=1
        Object actual = MathUtils.round(-1.0d, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_366() throws Exception {
        // Combination: x=Double.NaN, scale=1
        Object actual = MathUtils.round(Double.NaN, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_367() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=1
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_368() throws Exception {
        // Combination: x=0.0d, scale=-1
        Object actual = MathUtils.round(0.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_369() throws Exception {
        // Combination: x=1.0d, scale=-1
        Object actual = MathUtils.round(1.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_370() throws Exception {
        // Combination: x=-1.0d, scale=-1
        Object actual = MathUtils.round(-1.0d, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_371() throws Exception {
        // Combination: x=Double.NaN, scale=-1
        Object actual = MathUtils.round(Double.NaN, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_372() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=-1
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_373() throws Exception {
        // Combination: x=0.0d, scale=Integer.MAX_VALUE
        Object actual = MathUtils.round(0.0d, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_374() throws Exception {
        // Combination: x=1.0d, scale=Integer.MAX_VALUE
        try {
            MathUtils.round(1.0d, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_375() throws Exception {
        // Combination: x=-1.0d, scale=Integer.MAX_VALUE
        try {
            MathUtils.round(-1.0d, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_376() throws Exception {
        // Combination: x=Double.NaN, scale=Integer.MAX_VALUE
        Object actual = MathUtils.round(Double.NaN, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_377() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=Integer.MAX_VALUE
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_378() throws Exception {
        // Combination: x=0.0d, scale=Integer.MIN_VALUE
        Object actual = MathUtils.round(0.0d, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_379() throws Exception {
        // Combination: x=1.0d, scale=Integer.MIN_VALUE
        try {
            MathUtils.round(1.0d, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_380() throws Exception {
        // Combination: x=-1.0d, scale=Integer.MIN_VALUE
        try {
            MathUtils.round(-1.0d, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_381() throws Exception {
        // Combination: x=Double.NaN, scale=Integer.MIN_VALUE
        Object actual = MathUtils.round(Double.NaN, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_382() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=Integer.MIN_VALUE
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_383() throws Exception {
        // Combination: x=0.0d, scale=0, roundingMethod=0
        Object actual = MathUtils.round(0.0d, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_384() throws Exception {
        // Combination: x=1.0d, scale=1, roundingMethod=0
        Object actual = MathUtils.round(1.0d, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_385() throws Exception {
        // Combination: x=-1.0d, scale=-1, roundingMethod=0
        Object actual = MathUtils.round(-1.0d, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("-10.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_386() throws Exception {
        // Combination: x=Double.NaN, scale=Integer.MAX_VALUE, roundingMethod=0
        Object actual = MathUtils.round(Double.NaN, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_387() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=Integer.MIN_VALUE, roundingMethod=0
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_388() throws Exception {
        // Combination: x=1.0d, scale=0, roundingMethod=1
        Object actual = MathUtils.round(1.0d, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_389() throws Exception {
        // Combination: x=0.0d, scale=1, roundingMethod=1
        Object actual = MathUtils.round(0.0d, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_390() throws Exception {
        // Combination: x=Double.NaN, scale=-1, roundingMethod=1
        Object actual = MathUtils.round(Double.NaN, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_391() throws Exception {
        // Combination: x=-1.0d, scale=Integer.MAX_VALUE, roundingMethod=1
        try {
            MathUtils.round(-1.0d, Integer.MAX_VALUE, 1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_392() throws Exception {
        // Combination: x=0.0d, scale=Integer.MIN_VALUE, roundingMethod=1
        Object actual = MathUtils.round(0.0d, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_393() throws Exception {
        // Combination: x=-1.0d, scale=0, roundingMethod=-1
        try {
            MathUtils.round(-1.0d, 0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_394() throws Exception {
        // Combination: x=Double.NaN, scale=1, roundingMethod=-1
        Object actual = MathUtils.round(Double.NaN, 1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_395() throws Exception {
        // Combination: x=0.0d, scale=-1, roundingMethod=-1
        try {
            MathUtils.round(0.0d, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_396() throws Exception {
        // Combination: x=1.0d, scale=Integer.MAX_VALUE, roundingMethod=-1
        try {
            MathUtils.round(1.0d, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_397() throws Exception {
        // Combination: x=1.0d, scale=Integer.MIN_VALUE, roundingMethod=-1
        try {
            MathUtils.round(1.0d, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_398() throws Exception {
        // Combination: x=Double.NaN, scale=0, roundingMethod=Integer.MAX_VALUE
        Object actual = MathUtils.round(Double.NaN, 0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_399() throws Exception {
        // Combination: x=-1.0d, scale=1, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(-1.0d, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_400() throws Exception {
        // Combination: x=1.0d, scale=-1, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(1.0d, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_401() throws Exception {
        // Combination: x=0.0d, scale=Integer.MAX_VALUE, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(0.0d, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_402() throws Exception {
        // Combination: x=-1.0d, scale=Integer.MIN_VALUE, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(-1.0d, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_403() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=0, roundingMethod=Integer.MIN_VALUE
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, 0, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_404() throws Exception {
        // Combination: x=0.0d, scale=1, roundingMethod=Integer.MIN_VALUE
        try {
            MathUtils.round(0.0d, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_405() throws Exception {
        // Combination: x=1.0d, scale=-1, roundingMethod=Integer.MIN_VALUE
        try {
            MathUtils.round(1.0d, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_406() throws Exception {
        // Combination: x=-1.0d, scale=Integer.MAX_VALUE, roundingMethod=Integer.MIN_VALUE
        try {
            MathUtils.round(-1.0d, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_407() throws Exception {
        // Combination: x=Double.NaN, scale=Integer.MIN_VALUE, roundingMethod=Integer.MIN_VALUE
        Object actual = MathUtils.round(Double.NaN, Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_408() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=1, roundingMethod=1
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_409() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=-1, roundingMethod=-1
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, -1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_410() throws Exception {
        // Combination: x=Double.POSITIVE_INFINITY, scale=Integer.MAX_VALUE, roundingMethod=Integer.MAX_VALUE
        Object actual = MathUtils.round(Double.POSITIVE_INFINITY, Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Double", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_411() throws Exception {
        // Combination: x=0.0f, scale=0
        Object actual = MathUtils.round(0.0f, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_412() throws Exception {
        // Combination: x=1.0f, scale=0
        Object actual = MathUtils.round(1.0f, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_413() throws Exception {
        // Combination: x=-1.0f, scale=0
        Object actual = MathUtils.round(-1.0f, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_414() throws Exception {
        // Combination: x=Float.NaN, scale=0
        Object actual = MathUtils.round(Float.NaN, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_415() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=0
        Object actual = MathUtils.round(Float.POSITIVE_INFINITY, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_416() throws Exception {
        // Combination: x=0.0f, scale=1
        Object actual = MathUtils.round(0.0f, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_417() throws Exception {
        // Combination: x=1.0f, scale=1
        Object actual = MathUtils.round(1.0f, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_418() throws Exception {
        // Combination: x=-1.0f, scale=1
        Object actual = MathUtils.round(-1.0f, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_419() throws Exception {
        // Combination: x=Float.NaN, scale=1
        Object actual = MathUtils.round(Float.NaN, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_420() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=1
        Object actual = MathUtils.round(Float.POSITIVE_INFINITY, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_421() throws Exception {
        // Combination: x=0.0f, scale=-1
        Object actual = MathUtils.round(0.0f, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_422() throws Exception {
        // Combination: x=1.0f, scale=-1
        Object actual = MathUtils.round(1.0f, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_423() throws Exception {
        // Combination: x=-1.0f, scale=-1
        Object actual = MathUtils.round(-1.0f, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_424() throws Exception {
        // Combination: x=Float.NaN, scale=-1
        Object actual = MathUtils.round(Float.NaN, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_425() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=-1
        Object actual = MathUtils.round(Float.POSITIVE_INFINITY, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_426() throws Exception {
        // Combination: x=0.0f, scale=Integer.MAX_VALUE
        Object actual = MathUtils.round(0.0f, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_427() throws Exception {
        // Combination: x=1.0f, scale=Integer.MAX_VALUE
        Object actual = MathUtils.round(1.0f, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_428() throws Exception {
        // Combination: x=-1.0f, scale=Integer.MAX_VALUE
        Object actual = MathUtils.round(-1.0f, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_429() throws Exception {
        // Combination: x=Float.NaN, scale=Integer.MAX_VALUE
        Object actual = MathUtils.round(Float.NaN, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_430() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=Integer.MAX_VALUE
        Object actual = MathUtils.round(Float.POSITIVE_INFINITY, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_431() throws Exception {
        // Combination: x=0.0f, scale=Integer.MIN_VALUE
        Object actual = MathUtils.round(0.0f, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_432() throws Exception {
        // Combination: x=1.0f, scale=Integer.MIN_VALUE
        Object actual = MathUtils.round(1.0f, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_433() throws Exception {
        // Combination: x=-1.0f, scale=Integer.MIN_VALUE
        Object actual = MathUtils.round(-1.0f, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_434() throws Exception {
        // Combination: x=Float.NaN, scale=Integer.MIN_VALUE
        Object actual = MathUtils.round(Float.NaN, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_435() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=Integer.MIN_VALUE
        Object actual = MathUtils.round(Float.POSITIVE_INFINITY, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_436() throws Exception {
        // Combination: x=0.0f, scale=0, roundingMethod=0
        Object actual = MathUtils.round(0.0f, 0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_437() throws Exception {
        // Combination: x=1.0f, scale=1, roundingMethod=0
        Object actual = MathUtils.round(1.0f, 1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("1.1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_438() throws Exception {
        // Combination: x=-1.0f, scale=-1, roundingMethod=0
        Object actual = MathUtils.round(-1.0f, -1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-10.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_439() throws Exception {
        // Combination: x=Float.NaN, scale=Integer.MAX_VALUE, roundingMethod=0
        Object actual = MathUtils.round(Float.NaN, Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_440() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=Integer.MIN_VALUE, roundingMethod=0
        Object actual = MathUtils.round(Float.POSITIVE_INFINITY, Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_441() throws Exception {
        // Combination: x=1.0f, scale=0, roundingMethod=1
        Object actual = MathUtils.round(1.0f, 0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("0.0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_442() throws Exception {
        // Combination: x=0.0f, scale=1, roundingMethod=1
        Object actual = MathUtils.round(0.0f, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-0.1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_443() throws Exception {
        // Combination: x=Float.NaN, scale=-1, roundingMethod=1
        Object actual = MathUtils.round(Float.NaN, -1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_444() throws Exception {
        // Combination: x=-1.0f, scale=Integer.MAX_VALUE, roundingMethod=1
        Object actual = MathUtils.round(-1.0f, Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("NaN", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_445() throws Exception {
        // Combination: x=0.0f, scale=Integer.MIN_VALUE, roundingMethod=1
        Object actual = MathUtils.round(0.0f, Integer.MIN_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("-Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_446() throws Exception {
        // Combination: x=-1.0f, scale=0, roundingMethod=-1
        try {
            MathUtils.round(-1.0f, 0, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_447() throws Exception {
        // Combination: x=Float.NaN, scale=1, roundingMethod=-1
        try {
            MathUtils.round(Float.NaN, 1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_448() throws Exception {
        // Combination: x=0.0f, scale=-1, roundingMethod=-1
        try {
            MathUtils.round(0.0f, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_449() throws Exception {
        // Combination: x=1.0f, scale=Integer.MAX_VALUE, roundingMethod=-1
        try {
            MathUtils.round(1.0f, Integer.MAX_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_450() throws Exception {
        // Combination: x=1.0f, scale=Integer.MIN_VALUE, roundingMethod=-1
        try {
            MathUtils.round(1.0f, Integer.MIN_VALUE, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_451() throws Exception {
        // Combination: x=Float.NaN, scale=0, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(Float.NaN, 0, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_452() throws Exception {
        // Combination: x=-1.0f, scale=1, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(-1.0f, 1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_453() throws Exception {
        // Combination: x=1.0f, scale=-1, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(1.0f, -1, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_454() throws Exception {
        // Combination: x=0.0f, scale=Integer.MAX_VALUE, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(0.0f, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_455() throws Exception {
        // Combination: x=-1.0f, scale=Integer.MIN_VALUE, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(-1.0f, Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_456() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=0, roundingMethod=Integer.MIN_VALUE
        try {
            MathUtils.round(Float.POSITIVE_INFINITY, 0, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_457() throws Exception {
        // Combination: x=0.0f, scale=1, roundingMethod=Integer.MIN_VALUE
        try {
            MathUtils.round(0.0f, 1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_458() throws Exception {
        // Combination: x=1.0f, scale=-1, roundingMethod=Integer.MIN_VALUE
        try {
            MathUtils.round(1.0f, -1, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_459() throws Exception {
        // Combination: x=-1.0f, scale=Integer.MAX_VALUE, roundingMethod=Integer.MIN_VALUE
        try {
            MathUtils.round(-1.0f, Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_460() throws Exception {
        // Combination: x=Float.NaN, scale=Integer.MIN_VALUE, roundingMethod=Integer.MIN_VALUE
        try {
            MathUtils.round(Float.NaN, Integer.MIN_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_461() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=1, roundingMethod=1
        Object actual = MathUtils.round(Float.POSITIVE_INFINITY, 1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Float", actual.getClass().getName());
        assertEquals("Infinity", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_462() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=-1, roundingMethod=-1
        try {
            MathUtils.round(Float.POSITIVE_INFINITY, -1, -1);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_round_pairwise_463() throws Exception {
        // Combination: x=Float.POSITIVE_INFINITY, scale=Integer.MAX_VALUE, roundingMethod=Integer.MAX_VALUE
        try {
            MathUtils.round(Float.POSITIVE_INFINITY, Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_464() throws Exception {
        // Combination: x=0, y=0
        Object actual = MathUtils.subAndCheck(0, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_465() throws Exception {
        // Combination: x=0, y=1
        Object actual = MathUtils.subAndCheck(0, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_466() throws Exception {
        // Combination: x=0, y=-1
        Object actual = MathUtils.subAndCheck(0, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_467() throws Exception {
        // Combination: x=0, y=Integer.MAX_VALUE
        Object actual = MathUtils.subAndCheck(0, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_468() throws Exception {
        // Combination: x=0, y=Integer.MIN_VALUE
        try {
            MathUtils.subAndCheck(0, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_469() throws Exception {
        // Combination: x=1, y=0
        Object actual = MathUtils.subAndCheck(1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_470() throws Exception {
        // Combination: x=1, y=1
        Object actual = MathUtils.subAndCheck(1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_471() throws Exception {
        // Combination: x=1, y=-1
        Object actual = MathUtils.subAndCheck(1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_472() throws Exception {
        // Combination: x=1, y=Integer.MAX_VALUE
        Object actual = MathUtils.subAndCheck(1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483646", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_473() throws Exception {
        // Combination: x=1, y=Integer.MIN_VALUE
        try {
            MathUtils.subAndCheck(1, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_474() throws Exception {
        // Combination: x=-1, y=0
        Object actual = MathUtils.subAndCheck(-1, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_475() throws Exception {
        // Combination: x=-1, y=1
        Object actual = MathUtils.subAndCheck(-1, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_476() throws Exception {
        // Combination: x=-1, y=-1
        Object actual = MathUtils.subAndCheck(-1, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_477() throws Exception {
        // Combination: x=-1, y=Integer.MAX_VALUE
        Object actual = MathUtils.subAndCheck(-1, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_478() throws Exception {
        // Combination: x=-1, y=Integer.MIN_VALUE
        Object actual = MathUtils.subAndCheck(-1, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_479() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=0
        Object actual = MathUtils.subAndCheck(Integer.MAX_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_480() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=1
        Object actual = MathUtils.subAndCheck(Integer.MAX_VALUE, 1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("2147483646", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_481() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=-1
        try {
            MathUtils.subAndCheck(Integer.MAX_VALUE, -1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_482() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=Integer.MAX_VALUE
        Object actual = MathUtils.subAndCheck(Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_483() throws Exception {
        // Combination: x=Integer.MAX_VALUE, y=Integer.MIN_VALUE
        try {
            MathUtils.subAndCheck(Integer.MAX_VALUE, Integer.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_484() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=0
        Object actual = MathUtils.subAndCheck(Integer.MIN_VALUE, 0);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483648", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_485() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=1
        try {
            MathUtils.subAndCheck(Integer.MIN_VALUE, 1);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_486() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=-1
        Object actual = MathUtils.subAndCheck(Integer.MIN_VALUE, -1);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("-2147483647", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_487() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=Integer.MAX_VALUE
        try {
            MathUtils.subAndCheck(Integer.MIN_VALUE, Integer.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_488() throws Exception {
        // Combination: x=Integer.MIN_VALUE, y=Integer.MIN_VALUE
        Object actual = MathUtils.subAndCheck(Integer.MIN_VALUE, Integer.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Integer", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_489() throws Exception {
        // Combination: a=0L, b=0L
        Object actual = MathUtils.subAndCheck(0L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_490() throws Exception {
        // Combination: a=0L, b=1L
        Object actual = MathUtils.subAndCheck(0L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_491() throws Exception {
        // Combination: a=0L, b=-1L
        Object actual = MathUtils.subAndCheck(0L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_492() throws Exception {
        // Combination: a=0L, b=Long.MAX_VALUE
        Object actual = MathUtils.subAndCheck(0L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_493() throws Exception {
        // Combination: a=0L, b=Long.MIN_VALUE
        try {
            MathUtils.subAndCheck(0L, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_494() throws Exception {
        // Combination: a=1L, b=0L
        Object actual = MathUtils.subAndCheck(1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_495() throws Exception {
        // Combination: a=1L, b=1L
        Object actual = MathUtils.subAndCheck(1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_496() throws Exception {
        // Combination: a=1L, b=-1L
        Object actual = MathUtils.subAndCheck(1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_497() throws Exception {
        // Combination: a=1L, b=Long.MAX_VALUE
        Object actual = MathUtils.subAndCheck(1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775806", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_498() throws Exception {
        // Combination: a=1L, b=Long.MIN_VALUE
        try {
            MathUtils.subAndCheck(1L, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_499() throws Exception {
        // Combination: a=-1L, b=0L
        Object actual = MathUtils.subAndCheck(-1L, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-1", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_500() throws Exception {
        // Combination: a=-1L, b=1L
        Object actual = MathUtils.subAndCheck(-1L, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-2", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_501() throws Exception {
        // Combination: a=-1L, b=-1L
        Object actual = MathUtils.subAndCheck(-1L, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_502() throws Exception {
        // Combination: a=-1L, b=Long.MAX_VALUE
        Object actual = MathUtils.subAndCheck(-1L, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_503() throws Exception {
        // Combination: a=-1L, b=Long.MIN_VALUE
        Object actual = MathUtils.subAndCheck(-1L, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_504() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=0L
        Object actual = MathUtils.subAndCheck(Long.MAX_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_505() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=1L
        Object actual = MathUtils.subAndCheck(Long.MAX_VALUE, 1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("9223372036854775806", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_506() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=-1L
        try {
            MathUtils.subAndCheck(Long.MAX_VALUE, -1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_507() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MAX_VALUE
        Object actual = MathUtils.subAndCheck(Long.MAX_VALUE, Long.MAX_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_508() throws Exception {
        // Combination: a=Long.MAX_VALUE, b=Long.MIN_VALUE
        try {
            MathUtils.subAndCheck(Long.MAX_VALUE, Long.MIN_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_509() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=0L
        Object actual = MathUtils.subAndCheck(Long.MIN_VALUE, 0L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775808", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_510() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=1L
        try {
            MathUtils.subAndCheck(Long.MIN_VALUE, 1L);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_511() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=-1L
        Object actual = MathUtils.subAndCheck(Long.MIN_VALUE, -1L);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("-9223372036854775807", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_512() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MAX_VALUE
        try {
            MathUtils.subAndCheck(Long.MIN_VALUE, Long.MAX_VALUE);
            fail("Expected java.lang.ArithmeticException");
        } catch (java.lang.ArithmeticException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_subAndCheck_pairwise_513() throws Exception {
        // Combination: a=Long.MIN_VALUE, b=Long.MIN_VALUE
        Object actual = MathUtils.subAndCheck(Long.MIN_VALUE, Long.MIN_VALUE);
        assertNotNull(actual);
        assertEquals("java.lang.Long", actual.getClass().getName());
        assertEquals("0", String.valueOf(actual));
    }

}
