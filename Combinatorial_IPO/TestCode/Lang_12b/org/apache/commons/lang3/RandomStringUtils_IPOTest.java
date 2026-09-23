package org.apache.commons.lang3;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Automatically generated pairwise test suite for RandomStringUtils.
 */
public class RandomStringUtils_IPOTest {
    @Test(timeout = 4000)
    public void test_random_pairwise_001() throws Exception {
        // Combination: count=0, start=0, end=0, letters=true, numbers=true, chars=new char[] {}
        Object actual = RandomStringUtils.random(0, 0, 0, true, true, new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_002() throws Exception {
        // Combination: count=1, start=1, end=1, letters=false, numbers=false, chars=new char[] {}
        try {
            RandomStringUtils.random(1, 1, 1, false, false, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_003() throws Exception {
        // Combination: count=-1, start=-1, end=-1, letters=true, numbers=false, chars=new char[] {}
        try {
            RandomStringUtils.random(-1, -1, -1, true, false, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_004() throws Exception {
        // Combination: count=Integer.MAX_VALUE, start=Integer.MAX_VALUE, end=Integer.MAX_VALUE, letters=true, numbers=true, chars=new char[] {}
        try {
            RandomStringUtils.random(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_005() throws Exception {
        // Combination: count=Integer.MIN_VALUE, start=Integer.MIN_VALUE, end=Integer.MIN_VALUE, letters=true, numbers=true, chars=new char[] {}
        try {
            RandomStringUtils.random(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE, true, true, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_006() throws Exception {
        // Combination: count=0, start=1, end=1, letters=true, numbers=true, chars=new char[] {1}
        Object actual = RandomStringUtils.random(0, 1, 1, true, true, new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_007() throws Exception {
        // Combination: count=1, start=-1, end=0, letters=false, numbers=true, chars=new char[] {1}
        try {
            RandomStringUtils.random(1, -1, 0, false, true, new char[] {1});
            fail("Expected java.lang.ArrayIndexOutOfBoundsException");
        } catch (java.lang.ArrayIndexOutOfBoundsException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_008() throws Exception {
        // Combination: count=-1, start=0, end=Integer.MAX_VALUE, letters=false, numbers=false, chars=new char[] {1}
        try {
            RandomStringUtils.random(-1, 0, Integer.MAX_VALUE, false, false, new char[] {1});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_009() throws Exception {
        // Combination: count=Integer.MAX_VALUE, start=Integer.MIN_VALUE, end=-1, letters=false, numbers=true, chars=new char[] {1}
        try {
            RandomStringUtils.random(Integer.MAX_VALUE, Integer.MIN_VALUE, -1, false, true, new char[] {1});
            fail("Expected java.lang.OutOfMemoryError");
        } catch (java.lang.OutOfMemoryError expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_010() throws Exception {
        // Combination: count=Integer.MIN_VALUE, start=Integer.MAX_VALUE, end=0, letters=false, numbers=false, chars=new char[] {1}
        try {
            RandomStringUtils.random(Integer.MIN_VALUE, Integer.MAX_VALUE, 0, false, false, new char[] {1});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_011() throws Exception {
        // Combination: count=-1, start=1, end=0, letters=true, numbers=true, chars=new char[] {}
        try {
            RandomStringUtils.random(-1, 1, 0, true, true, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_012() throws Exception {
        // Combination: count=Integer.MAX_VALUE, start=Integer.MIN_VALUE, end=0, letters=true, numbers=false, chars=new char[] {}
        try {
            RandomStringUtils.random(Integer.MAX_VALUE, Integer.MIN_VALUE, 0, true, false, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_013() throws Exception {
        // Combination: count=-1, start=Integer.MAX_VALUE, end=1, letters=true, numbers=true, chars=new char[] {}
        try {
            RandomStringUtils.random(-1, Integer.MAX_VALUE, 1, true, true, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_014() throws Exception {
        // Combination: count=Integer.MAX_VALUE, start=0, end=1, letters=true, numbers=true, chars=new char[] {}
        try {
            RandomStringUtils.random(Integer.MAX_VALUE, 0, 1, true, true, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_015() throws Exception {
        // Combination: count=Integer.MIN_VALUE, start=-1, end=1, letters=true, numbers=true, chars=new char[] {}
        try {
            RandomStringUtils.random(Integer.MIN_VALUE, -1, 1, true, true, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_016() throws Exception {
        // Combination: count=0, start=Integer.MAX_VALUE, end=-1, letters=false, numbers=false, chars=new char[] {}
        Object actual = RandomStringUtils.random(0, Integer.MAX_VALUE, -1, false, false, new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_017() throws Exception {
        // Combination: count=1, start=0, end=-1, letters=true, numbers=true, chars=new char[] {}
        try {
            RandomStringUtils.random(1, 0, -1, true, true, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_018() throws Exception {
        // Combination: count=Integer.MIN_VALUE, start=1, end=-1, letters=true, numbers=true, chars=new char[] {}
        try {
            RandomStringUtils.random(Integer.MIN_VALUE, 1, -1, true, true, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_019() throws Exception {
        // Combination: count=0, start=-1, end=Integer.MAX_VALUE, letters=true, numbers=true, chars=new char[] {}
        Object actual = RandomStringUtils.random(0, -1, Integer.MAX_VALUE, true, true, new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_020() throws Exception {
        // Combination: count=1, start=Integer.MIN_VALUE, end=Integer.MAX_VALUE, letters=true, numbers=true, chars=new char[] {}
        try {
            RandomStringUtils.random(1, Integer.MIN_VALUE, Integer.MAX_VALUE, true, true, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_021() throws Exception {
        // Combination: count=Integer.MIN_VALUE, start=0, end=Integer.MAX_VALUE, letters=true, numbers=true, chars=new char[] {}
        try {
            RandomStringUtils.random(Integer.MIN_VALUE, 0, Integer.MAX_VALUE, true, true, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_022() throws Exception {
        // Combination: count=0, start=0, end=Integer.MIN_VALUE, letters=false, numbers=false, chars=new char[] {1}
        Object actual = RandomStringUtils.random(0, 0, Integer.MIN_VALUE, false, false, new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_023() throws Exception {
        // Combination: count=1, start=Integer.MAX_VALUE, end=Integer.MIN_VALUE, letters=true, numbers=true, chars=new char[] {}
        try {
            RandomStringUtils.random(1, Integer.MAX_VALUE, Integer.MIN_VALUE, true, true, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_024() throws Exception {
        // Combination: count=-1, start=1, end=Integer.MIN_VALUE, letters=true, numbers=true, chars=new char[] {}
        try {
            RandomStringUtils.random(-1, 1, Integer.MIN_VALUE, true, true, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_025() throws Exception {
        // Combination: count=Integer.MAX_VALUE, start=-1, end=Integer.MIN_VALUE, letters=true, numbers=true, chars=new char[] {}
        try {
            RandomStringUtils.random(Integer.MAX_VALUE, -1, Integer.MIN_VALUE, true, true, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_026() throws Exception {
        // Combination: count=Integer.MAX_VALUE, start=1, end=Integer.MAX_VALUE, letters=true, numbers=true, chars=new char[] {}
        try {
            RandomStringUtils.random(Integer.MAX_VALUE, 1, Integer.MAX_VALUE, true, true, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_027() throws Exception {
        // Combination: count=0, start=Integer.MIN_VALUE, end=1, letters=true, numbers=true, chars=new char[] {}
        Object actual = RandomStringUtils.random(0, Integer.MIN_VALUE, 1, true, true, new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_028() throws Exception {
        // Combination: count=-1, start=Integer.MIN_VALUE, end=0, letters=true, numbers=true, chars=new char[] {}
        try {
            RandomStringUtils.random(-1, Integer.MIN_VALUE, 0, true, true, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_029() throws Exception {
        // Combination: count=0, chars=new char[] {}
        Object actual = RandomStringUtils.random(0, new char[] {});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_030() throws Exception {
        // Combination: count=1, chars=new char[] {}
        try {
            RandomStringUtils.random(1, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_031() throws Exception {
        // Combination: count=-1, chars=new char[] {}
        try {
            RandomStringUtils.random(-1, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_032() throws Exception {
        // Combination: count=Integer.MAX_VALUE, chars=new char[] {}
        try {
            RandomStringUtils.random(Integer.MAX_VALUE, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_033() throws Exception {
        // Combination: count=Integer.MIN_VALUE, chars=new char[] {}
        try {
            RandomStringUtils.random(Integer.MIN_VALUE, new char[] {});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_034() throws Exception {
        // Combination: count=0, chars=new char[] {1}
        Object actual = RandomStringUtils.random(0, new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_035() throws Exception {
        // Combination: count=1, chars=new char[] {1}
        Object actual = RandomStringUtils.random(1, new char[] {1});
        assertNotNull(actual);
        assertEquals("java.lang.String", actual.getClass().getName());
        assertEquals("\u0001", String.valueOf(actual));
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_036() throws Exception {
        // Combination: count=-1, chars=new char[] {1}
        try {
            RandomStringUtils.random(-1, new char[] {1});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_037() throws Exception {
        // Combination: count=Integer.MAX_VALUE, chars=new char[] {1}
        try {
            RandomStringUtils.random(Integer.MAX_VALUE, new char[] {1});
            fail("Expected java.lang.OutOfMemoryError");
        } catch (java.lang.OutOfMemoryError expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

    @Test(timeout = 4000)
    public void test_random_pairwise_038() throws Exception {
        // Combination: count=Integer.MIN_VALUE, chars=new char[] {1}
        try {
            RandomStringUtils.random(Integer.MIN_VALUE, new char[] {1});
            fail("Expected java.lang.IllegalArgumentException");
        } catch (java.lang.IllegalArgumentException expected) {
            // Expected outcome recorded from the fixed version.
        }
    }

}
